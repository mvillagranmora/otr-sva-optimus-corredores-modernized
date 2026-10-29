package cl.bcs.contabilidad.domain.service;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.soporte.Campo;
import cl.bcs.contabilidad.domain.service.soporte.Contexto;
import cl.bcs.contabilidad.domain.service.soporte.DatoRequeridoException;
import cl.bcs.contabilidad.domain.service.soporte.DatosMovimiento;
import cl.bcs.contabilidad.domain.service.soporte.Fila;
import cl.bcs.contabilidad.domain.service.soporte.Tablas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Treasury vouchers: legacy CentralizaTesoreria.java and CentralizaTesoreriaFormasDePago.java, which share this flow
 * and differ only in their movement / event / data SQL and one data entry (CONT-F-177..CONT-F-210).
 *
 * <p>Flow (legacy :128-368):
 * <ol>
 *   <li>movements of the type's own SQL;</li>
 *   <li>with CONTABILIDAD / CalculaParidadCLPCentralizacionTesoreria = "S" (default "N"), every non-CLP movement amount
 *       is converted to CLP ({@link #convertir}); a currency without exchange rate rejects the whole type: 1 error and
 *       one UI_PARAMETRO_FINANCIERO_FECHA_SOLICITADA message per (date, currency), and nothing else is produced;</li>
 *   <li>events (converted amounts for the voucher amount events), then the data rows of the type's SQL plus the
 *       document and request rows of the settled split vouchers (after filling their working table), with Monto and
 *       Valor converted as well.</li>
 * </ol>
 */
public abstract class CentralizacionTesoreria extends CentralizadorSql {

    private static final Logger log = LoggerFactory.getLogger(CentralizacionTesoreria.class);

    private static final String INSERT_COMPROBANTES_ASOCIADOS = "cl/bcs/contabilidad/sql/CentralizaTesoreria_Insert_Comprobantes_Asociados_Partidas.sql";
    private static final String SELECT_DATOS_DOCUMENTOS = "cl/bcs/contabilidad/sql/CentralizaTesoreria_SelectDatosMovimiento_Documentos_Partidas.sql";
    private static final String SELECT_DATOS_SOLICITUDES = "cl/bcs/contabilidad/sql/CentralizaTesoreria_SelectDatosMovimiento_Solicitudes_Partidas.sql";
    private static final String SELECT_PARIDAD = "cl/bcs/contabilidad/sql/CentralizaTesoreria_Select_Parametros_Tipo_Cambio_Paridad.sql";

    /** Legacy :49-50: the event amounts that are converted. */
    private static final Set<String> EVENTOS_CON_PARIDAD = Set.of(
            "SOLICITUD_MONTO_INGRESO", "SOLICITUD_MONTO_TRASPASO_INGRESO", "SOLICITUD_MONTO_EGRESO", "SOLICITUD_MONTO_TRASPASO_EGRESO",
            "DOCUMENTO_MONTO_INGRESO", "DOCUMENTO_MONTO_TRASPASO_INGRESO", "DOCUMENTO_MONTO_EGRESO", "DOCUMENTO_MONTO_TRASPASO_EGRESO");

    private static final String CLP = "CLP";
    private static final String USD = "USD";
    /** Observed dollar, the pivot rate of the conversion. */
    private static final String DOLAR_OBSERVADO = "DO";
    private static final LocalDate FECHA_NULA = LocalDate.of(1, 1, 1);
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String sqlMovimientos;
    private final String sqlEventos;
    private final String sqlDatos;
    private final List<Campo> campos;

    protected CentralizacionTesoreria(PlataformaPort plataforma, ContabilidadRepository repository,
                                      String sqlMovimientos, String sqlEventos, String sqlDatos, List<Campo> campos) {
        super(plataforma, repository);
        this.sqlMovimientos = sqlMovimientos;
        this.sqlEventos = sqlEventos;
        this.sqlDatos = sqlDatos;
        this.campos = campos;
    }

    /** Outcome of the CLP conversion of one movement (legacy CalculoSalida, reduced to what is used). */
    private record Conversion(String fecha, String moneda, BigDecimal montoDestino, boolean rechazo) {}

    @Override
    protected Tablas calcular(Contexto ctx) {
        boolean calculaParidad = "S".equalsIgnoreCase(
                ctx.parametro("CONTABILIDAD", "CalculaParidadCLPCentralizacionTesoreria", "N"));
        Tablas t = new Tablas();

        List<TabMovimientoRef> movimientos = movimientos(sqlMovimientos, parametros(
                "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                "IdTipoCentralizacion", ctx.idTipo(), "LocSistema", ctx.localizacion()));

        Map<Long, Conversion> conversiones = Map.of();
        if (calculaParidad && !movimientos.isEmpty()) {
            Map<Long, Conversion> calculadas = convertir(ctx, movimientos);
            Set<String> rechazos = new LinkedHashSet<>();
            calculadas.values().stream().filter(Conversion::rechazo)
                    .forEach(c -> rechazos.add(c.fecha() + ";" + c.moneda()));
            if (!rechazos.isEmpty()) {
                // legacy :198-215: the type stops here, with one message per rejected (date, currency)
                rechazos.forEach(r -> t.error("UI_PARAMETRO_FINANCIERO_FECHA_SOLICITADA", Map.of("Moneda", r.split(";")[1])));
                return t.sinResultados();
            }
            conversiones = calculadas;
        }
        Map<Long, Conversion> conv = conversiones;

        // :217-226 converted movement amounts
        t.movimientos(movimientos.stream().map(m -> {
            Conversion c = conv.get(m.idMovimiento());
            return c != null && !CLP.equalsIgnoreCase(m.idMoneda()) ? conMonto(m, redondeo(c.montoDestino())) : m;
        }).toList());

        // :236-262 events
        List<TabEventoRef> eventos = eventos(sqlEventos, parametros(
                "FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                "IdTipoCentralizacion", ctx.idTipo(), "LocSistema", ctx.localizacion()));
        t.eventos(eventos.stream().map(e -> {
            Conversion c = EVENTOS_CON_PARIDAD.contains(e.idEvento()) ? conv.get(e.idMovimiento()) : null;
            if (c == null || CLP.equalsIgnoreCase(requerido(e.codigoMoneda(), "CodigoMoneda"))) return e;
            return new TabEventoRef(e.idTipoCentralizacion(), e.idMovimiento(), e.idEvento(), e.nombre(),
                    c.montoDestino() == null ? BigDecimal.ZERO : redondeo(c.montoDestino()), e.codigoMoneda());
        }).toList());

        // :265-283 data rows: the type's own, then the split vouchers' documents and requests
        MapSqlParameterSource pDatos = parametros("FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa(),
                "FechaSistema", ctx.fechaSistema(), "LocSistema", ctx.localizacion());
        List<Fila> datos = new ArrayList<>(filas(sqlDatos, pDatos));
        repository.ejecutarSql(INSERT_COMPROBANTES_ASOCIADOS,
                parametros("FechaContable", ctx.fechaContable(), "IdEmpresa", ctx.idEmpresa()));
        datos.addAll(filas(SELECT_DATOS_DOCUMENTOS, pDatos));
        datos.addAll(filas(SELECT_DATOS_SOLICITUDES, pDatos));

        // :287-304 converted Monto / Valor
        List<Fila> convertidos = datos.stream().map(f -> {
            Conversion c = conv.get(f.entero("IdMovimiento"));
            if (c == null || CLP.equalsIgnoreCase(requerido(f.texto("Moneda"), "Moneda"))) return f;
            BigDecimal monto = c.montoDestino() == null ? BigDecimal.ZERO : redondeo(c.montoDestino());
            return f.con("Monto", monto).con("Valor", monto);
        }).toList();
        return t.datos(DatosMovimiento.de(ctx.idTipo(), convertidos, campos));
    }

    /**
     * CLP amount of every non-CLP movement (legacy calculoParidades, :371-484): factor = market exchange rate of the
     * observed dollar for USD, of the currency itself otherwise, searched in a window ending at the movement date and
     * starting PLA.DiasFechaMinimaCalculoParidad days (default 1095) before the earliest date. Without a positive
     * factor the movement is rejected.
     *
     * <p>Legacy defects kept: the window start is pushed back by the same number of days once more for every
     * movement processed; and a missing observed dollar (or amount) aborted the loop with a swallowed exception, so the
     * remaining movements are simply not converted.
     */
    private Map<Long, Conversion> convertir(Contexto ctx, List<TabMovimientoRef> movimientos) {
        long dias = diasVentana(ctx);
        Map<Long, Conversion> out = new LinkedHashMap<>();
        LocalDate fechaMaxima = null;
        LocalDate fechaMinima = null;
        for (TabMovimientoRef mov : movimientos) {
            if (CLP.equals(requerido(mov.idMoneda(), "IdMoneda"))) continue;
            LocalDate fecha = fecha(mov.fecha());
            if (fechaMaxima == null || fechaMaxima.isBefore(fecha)) fechaMaxima = fecha;
            if (fechaMinima == null || fechaMinima.isAfter(fecha)) fechaMinima = fecha;
            if (!fechaMinima.equals(FECHA_NULA)) fechaMinima = fechaMinima.minusDays(dias);

            BigDecimal dolar = tipoCambio(DOLAR_OBSERVADO, fechaMinima, fechaMaxima);
            BigDecimal moneda = tipoCambio(mov.idMoneda(), fechaMinima, fechaMaxima);
            if (dolar == null || mov.monto() == null) {
                log.warn("CentralizaTesoreria: sin dolar observado o monto para el movimiento {}; se detiene la conversion",
                        mov.idMovimiento());
                break;
            }
            BigDecimal factor = USD.equals(mov.idMoneda()) ? dolar : moneda == null ? BigDecimal.ZERO : moneda;
            boolean valido = factor.compareTo(BigDecimal.ZERO) > 0;
            BigDecimal destino = valido ? mov.monto().multiply(factor, MathContext.DECIMAL128) : null;
            out.put(mov.idMovimiento(), new Conversion(fecha.toString(), mov.idMoneda(), destino, !valido));
        }
        return out;
    }

    /** TipoCambioMercado of the latest non-zero rate of the parameter in the window (null when there is none). */
    private BigDecimal tipoCambio(String idParametroFinanciero, LocalDate desde, LocalDate hasta) {
        List<Fila> filas = filas(SELECT_PARIDAD, parametros("IdParametroFinanciero", idParametroFinanciero,
                "FechaMinima", java.sql.Date.valueOf(desde), "FechaMaxima", java.sql.Date.valueOf(hasta)));
        BigDecimal tipoCambio = null;
        for (Fila f : filas) {
            if (idParametroFinanciero.equals(f.texto("IdMoneda"))) tipoCambio = f.decimal("TipoCambioMercado");
        }
        return tipoCambio;
    }

    private static long diasVentana(Contexto ctx) {
        String v = ctx.parametro("PLATAFORMA", "PLA.DiasFechaMinimaCalculoParidad", "1095");
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            return 1095L; // legacy getParametroConfiguracionLong in a try/catch keeps the default
        }
    }

    /** Legacy Fecha.toDate: "yyyy-MM-dd HH:mm:ss" or "yyyy-MM-dd". */
    private static LocalDate fecha(String fecha) {
        String f = requerido(fecha, "Fecha");
        return f.length() == 19 ? LocalDateTime.parse(f, FECHA_HORA).toLocalDate() : LocalDate.parse(f);
    }

    private static BigDecimal redondeo(BigDecimal monto) {
        return monto.setScale(0, RoundingMode.HALF_UP);
    }

    private static TabMovimientoRef conMonto(TabMovimientoRef m, BigDecimal monto) {
        return new TabMovimientoRef(m.idTipoCentralizacion(), m.idMovimiento(), m.idTipoMovimiento(), m.idEmpresa(),
                m.numero(), m.linea(), m.fecha(), m.documentoIdentidad(), m.idPortafolio(), m.ejercicio(), m.periodo(),
                monto, m.idMoneda(), m.estado());
    }

    private static <T> T requerido(T valor, String columna) {
        if (valor == null) throw new DatoRequeridoException("paridad CLP", columna); // legacy x.equals(..) on null
        return valor;
    }
}
