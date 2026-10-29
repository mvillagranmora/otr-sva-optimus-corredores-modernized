package cl.bcs.cuentainversion.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.domain.ConsultaMovimiento;
import cl.bcs.cuentainversion.domain.GoLiveRouting;
import cl.bcs.cuentainversion.domain.Movimiento;
import cl.bcs.cuentainversion.domain.MovimientoCuentaInversion;
import cl.bcs.cuentainversion.port.in.ConsultarMovimientoUseCase;
import cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort;
import cl.bcs.cuentainversion.port.out.DiccionarioMensajes;
import cl.bcs.cuentainversion.port.out.HanaSaldoPort;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Utils;

/**
 * Movements of a list of portfolio/currency queries (CTA-A-159..188, legacy ConsultarMovimientoCuentaInversion).
 *
 * <p>Each query is routed by the go-live date ({@code PLA.FechaGoLive}): entirely before it -> historic ledger; spanning it ->
 * historic part up to the day before the cut-off plus current part from it (hybrid); otherwise current ledger. The running
 * {@code SaldoSimulado} comes from the SQL; with {@code IncluyeSaldo = S} the historic part is wrapped in
 * "Saldo Inicial"/"Saldo Final" rows and the final historic balance seeds the current part.
 *
 * <p>B-CTA-07: Go-Live date routing HISTORICAL_ONLY / HYBRID / CURRENT_ONLY (per account use case).
 * B-CTA-11: Origen derivation (APV/DC prefix -> "APV", else "NOAPV").
 */
@Service
public class ConsultarMovimientoCuentaInversionService implements ConsultarMovimientoUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConsultarMovimientoCuentaInversionService.class);
    private static final LocalDate SIN_FECHA_DE_CORTE = LocalDate.of(1, 1, 1);

    private final HanaSaldoPort hanaSaldo;
    private final ConfiguracionSistemaPort configuracion;
    private final DiccionarioMensajes diccionario;

    public ConsultarMovimientoCuentaInversionService(HanaSaldoPort hanaSaldo,
            ConfiguracionSistemaPort configuracion, DiccionarioMensajes diccionario) {
        this.hanaSaldo = hanaSaldo;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
    }

    /** Input; {@code p_Consulta} has no default, a missing list fails as in the legacy. */
    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<ConsultaMovimiento> p_Consulta;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<MovimientoCuentaInversion> P_OUT_MOVIMIENTOS = new ArrayList<>();
    }

    /** JavaCall entry point: JSON in, JSON out (same contract as the legacy Entrada/Salida). */
    public JsonObject execute(JsonObject params) {
        Gson gson = Utils.getGsonBuilder();
        return gson.toJsonTree(consultar(gson.fromJson(params, Entrada.class))).getAsJsonObject();
    }

    /** Typed entry point used in process by ConsultarCartolaDeMovimiento and the web service ConsultarMovimientos. */
    public Salida consultar(Entrada entrada) {
        log.info("INICIO::ConsultarMovimientoCuentaInversion");
        Salida out = new Salida();
        List<ConsultaMovimiento> consultasHistoricas = new ArrayList<>();
        List<ConsultaMovimiento> consultasActuales = new ArrayList<>();
        // LEGACY DEFECT kept (see README CTA-A-185): one shared holder for every historic query and the accumulated list is
        // never cleared, so later consultas repeat the rows of earlier ones
        ConsultaMovimiento historica = new ConsultaMovimiento();
        boolean incluyeSaldo = false;
        LocalDate corte = fechaDeCorte(entrada.p_IdEmpresa);

        for (ConsultaMovimiento c : entrada.p_Consulta) {
            // LEGACY DEFECT kept (see README CTA-A-173): a null IncluyeSaldo throws NullPointerException
            if (c.IncluyeSaldo.equals("S")) {
                incluyeSaldo = true;
            }
            LocalDate desde = Fecha.toDate(c.FechaDesde).toLocalDate();
            LocalDate hasta = Fecha.toDate(c.FechaHasta).toLocalDate();
            if (corte.isAfter(desde) && corte.isAfter(hasta)) {
                consultasHistoricas.add(c);
                out.P_OUT_MOVIMIENTOS.addAll(historicos(consultasHistoricas, incluyeSaldo));
            } else if (corte.isAfter(desde) && !hasta.isBefore(corte)) {
                copiar(c, historica);
                c.FechaDesde = corte.toString();
                consultasActuales.add(c);
                // the cut-off moves to the day before itself and stays there for the next queries
                // (legacy ObtenerDiaAnterior with business days off: plain minus one day)
                corte = corte.minusDays(1);
                historica.FechaHasta = corte.toString();
                consultasHistoricas.add(historica);

                List<MovimientoCuentaInversion> historicos = historicos(consultasHistoricas, incluyeSaldo);
                out.P_OUT_MOVIMIENTOS.addAll(historicos);
                if (!historicos.isEmpty()) {
                    for (ConsultaMovimiento actual : consultasActuales) {
                        // LEGACY DEFECT kept (see README CTA-A-180/181): index 1, not the last row; IndexOutOfBounds with one row
                        actual.SaldoInicial = historicos.get(1).SaldoSimulado;
                    }
                    if (!out.P_OUT_MOVIMIENTOS.isEmpty()) {
                        out.P_OUT_MOVIMIENTOS.remove(1);
                    }
                }
            } else {
                consultasActuales.add(c);
            }
        }
        out.P_OUT_MOVIMIENTOS.addAll(hanaSaldo.consultarMovimientosPorConsulta(consultasActuales));
        out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.OK, "CONSULTA_EXITOSA",
                diccionario.traducir(entrada.p_IdEmpresa, "CONSULTA_EXITOSA", new HashMap<>())));
        log.info("FIN::ConsultarMovimientoCuentaInversion");
        return out;
    }

    /** Historic rows of the accumulated queries; with balances requested, "Saldo Inicial" and "Saldo Final" rows go in front. */
    private List<MovimientoCuentaInversion> historicos(List<ConsultaMovimiento> consultas, boolean incluyeSaldo) {
        List<MovimientoCuentaInversion> filas = hanaSaldo.consultarMovimientosHistoricosPorConsulta(consultas);
        if (incluyeSaldo && !filas.isEmpty()) {
            MovimientoCuentaInversion fila = new MovimientoCuentaInversion();
            fila.Descripcion = "Saldo Final";
            fila.SaldoSimulado = filas.get(filas.size() - 1).SaldoSimulado;
            filas.add(0, fila);
            fila = new MovimientoCuentaInversion();
            fila.Descripcion = "Saldo Inicial";
            MovimientoCuentaInversion primera = filas.get(1);
            fila.SaldoSimulado = primera.SaldoSimulado.add(primera.Cargo.subtract(primera.Abono));
            filas.add(0, fila);
        }
        return filas;
    }

    /** PLA.FechaGoLive; a missing or non-date parameter means "no cut-off" (0001-01-01), so everything is current. */
    private LocalDate fechaDeCorte(Long idEmpresa) {
        LocalDate goLive = configuracion.getFechaGoLive(idEmpresa);
        return goLive == null ? SIN_FECHA_DE_CORTE : goLive;
    }

    private static void copiar(ConsultaMovimiento origen, ConsultaMovimiento destino) {
        destino.IdPortafolio = origen.IdPortafolio;
        destino.IdMoneda = origen.IdMoneda;
        destino.IdEstado = origen.IdEstado;
        destino.SaldoInicial = origen.SaldoInicial;
        destino.FechaDesde = origen.FechaDesde;
        destino.FechaHasta = origen.FechaHasta;
        destino.IdMatrizAgrupador = origen.IdMatrizAgrupador;
        destino.EsADC = origen.EsADC;
        destino.SaldoPorFechaEfectiva = origen.SaldoPorFechaEfectiva;
        destino.IncluyeSaldo = origen.IncluyeSaldo;
    }

    // ── Per-account use case (REST controller /api/v1/movimientos) ─────────────

    @Override
    public List<Movimiento> consultar(Long idEmpresa, Long idCuentaInversion,
            LocalDate fechaDesde, LocalDate fechaHasta, boolean incluyeAnulados) {
        Objects.requireNonNull(idEmpresa, "idEmpresa");
        Objects.requireNonNull(idCuentaInversion, "idCuentaInversion");
        Objects.requireNonNull(fechaDesde, "fechaDesde");
        Objects.requireNonNull(fechaHasta, "fechaHasta");

        return switch (resolverRouting(idEmpresa, fechaDesde, fechaHasta)) {
            case HISTORICAL_ONLY ->
                hanaSaldo.consultarMovimientosHistoricos(idCuentaInversion, fechaDesde, fechaHasta, incluyeAnulados);
            case CURRENT_ONLY ->
                hanaSaldo.consultarMovimientos(idCuentaInversion, fechaDesde, fechaHasta, incluyeAnulados);
            case HYBRID -> Stream.concat(
                    hanaSaldo.consultarMovimientosHistoricos(idCuentaInversion, fechaDesde, fechaHasta, incluyeAnulados).stream(),
                    hanaSaldo.consultarMovimientos(idCuentaInversion, fechaDesde, fechaHasta, incluyeAnulados).stream())
                    .toList();
        };
    }

    // B-CTA-07: routing logic - both before goLive -> HISTORICAL_ONLY
    GoLiveRouting resolverRouting(Long idEmpresa, LocalDate fechaDesde, LocalDate fechaHasta) {
        LocalDate goLive = configuracion.getFechaGoLive(idEmpresa);
        if (fechaDesde.isBefore(goLive) && fechaHasta.isBefore(goLive)) {
            return GoLiveRouting.HISTORICAL_ONLY;
        } else if (!fechaDesde.isBefore(goLive)) {
            return GoLiveRouting.CURRENT_ONLY;
        } else {
            return GoLiveRouting.HYBRID;
        }
    }

    // B-CTA-11: codigoPortafolio startsWith "APV" or "DC" -> "APV", else "NOAPV"
    String derivarOrigen(String codigoPortafolio) {
        return (codigoPortafolio.startsWith("APV") || codigoPortafolio.startsWith("DC"))
                ? "APV" : "NOAPV";
    }
}
