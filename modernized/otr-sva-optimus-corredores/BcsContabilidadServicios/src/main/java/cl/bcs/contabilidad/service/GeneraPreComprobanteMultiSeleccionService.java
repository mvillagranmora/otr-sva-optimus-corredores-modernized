package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.domain.port.out.PreComprobantePostProcesoPort;
import cl.bcs.contabilidad.infrastructure.persistence.HanaSequences;
import cl.bcs.contabilidad.infrastructure.persistence.SqlParams;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generates the accounting pre-vouchers (PreComprobante + asientos + their events) of the selected voucher types for an
 * accounting date, from the centralized movements of that date.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.GeneraPreComprobanteMultiSeleccion} (L423-978). Implements CONT-G-100..113.
 * <ol>
 *   <li>All catalogues are read once for the distinct requested types (batched lookups).</li>
 *   <li>A type is skipped with an ADVERTENCIA (id = IdTipoContabilidad) when it does not exist, has entry definitions
 *       whose account is not homologated (one NO_EXISTE_CUENTA_CONTABLE_ASIENTO each), has no centralization for the
 *       date, or already has a pre-voucher for the date and is not reversible.</li>
 *   <li>The movements of the type's centralization types that satisfy the voucher-level conditions are matched
 *       against each entry definition's conditions. The amount of an asiento is the sum of the matching EVENTS
 *       ({@code Evento.Monto}, absolute value, negated by operator {@code -}); its sign decides Debe or Haber.</li>
 *   <li>A pre-voucher whose debits and credits differ after rounding to 2 decimals (HALF_UP) is stored in Estado D.</li>
 *   <li>Reversible types reverse their previous pre-voucher and the new pre-vouchers are sent
 *       ({@link PreComprobantePostProcesoPort}); a pre-voucher without ADVERTENCIA/ERROR gets PRE_COMPROBANTE_GENERADO.</li>
 * </ol>
 * Condition semantics: per IdTipoDatoEvento, {@code =} alternatives are OR-ed and other operators AND-ed; string
 * {@code >}/{@code <} keep the legacy {@code compareTo(..) == 1 / == -1} test. Database failures propagate.
 */
@Service
public class GeneraPreComprobanteMultiSeleccionService {

    private static final String SEQ_PRECOMPROBANTE = "BSCL.Contabilidad.BD.Secuencias::IdPreComprobante";
    private static final String SEQ_PRECOMPROBANTE_ASIENTO = "BSCL.Contabilidad.BD.Secuencias::IdPreComprobanteAsiento";

    private final String SQL_SELECT_CENTRALIZACION = SqlResources.load("GeneraPreComprobanteMultiSeleccion_SelectCentralizacion.sql");
    private final String SQL_SELECT_TIPO_CONTABILIDAD = SqlResources.load("GeneraPreComprobanteMultiSeleccion_SelectTipoContabilidad.sql");
    private final String SQL_SELECT_PRECOMP_ULTIMO = SqlResources.load("GeneraPreComprobanteMultiSeleccion_SelectPrecomprobanteUltimo.sql");
    private final String SQL_SELECT_CONDICIONES_ASIENTO = SqlResources.load("GeneraPreComprobanteMultiSeleccion_SelectCondicionesAsiento.sql");
    private final String SQL_SELECT_MOVIMIENTOS_DATOS = SqlResources.load("GeneraPreComprobanteMultiSeleccion_SelectMovimientosDatos.sql");
    private final String SQL_SELECT_MOVIMIENTOS = SqlResources.load("GeneraPreComprobanteMultiSeleccion_SelectMovimientos.sql");
    private final String SQL_SELECT_EVENTOS = SqlResources.load("GeneraPreComprobanteMultiSeleccion_SelectEventos.sql");
    private final String SQL_SELECT_TIPO_GLOSA = SqlResources.load("GeneraPreComprobanteMultiSeleccion_SelectDefinicionTipoGlosa.sql");
    private final String SQL_SELECT_CONDICIONES_PRECOMP = SqlResources.load("GeneraPreComprobanteMultiSeleccion_SelectCondicionesAsientoPrecomprobante.sql");
    private final String SQL_SELECT_TC_CENTRALIZACION = SqlResources.load("GeneraPreComprobanteMultiSeleccion_SelectTipoContabilidadTipoCentralizacion.sql");
    private final String SQL_SELECT_ASIENTO = SqlResources.load("GeneraPreComprobanteMultiSeleccion_SelectAsiento.sql");
    private final String SQL_EVENTOS_ASIENTO = SqlResources.load("GeneraPreComprobanteMultiSeleccion_SelectEventosAsiento.sql");
    private final String SQL_INSERT_PRECOMPROBANTES = SqlResources.load("GeneraPreComprobanteMultiSeleccion_InsertPrecomprobantes.sql");
    private final String SQL_INSERT_PRECOMP_ASIENTOS = SqlResources.load("GeneraPreComprobanteMultiSeleccion_InsertPrecomprobantesAsientos.sql");
    private final String SQL_INSERT_EVENTO_PRECOMP = SqlResources.load("GeneraPreComprobanteMultiSeleccion_InsertEventoPrecomprobantesAsientos.sql");

    private final NamedParameterJdbcTemplate jdbc;
    private final PreComprobantePostProcesoPort postProceso;
    /** False only in the generation-only mode of the convenience constructor. */
    private final boolean envioObligatorio;
    private final Clock clock;

    @Autowired
    public GeneraPreComprobanteMultiSeleccionService(NamedParameterJdbcTemplate jdbc,
                                                     ObjectProvider<PreComprobantePostProcesoPort> postProceso) {
        this(jdbc, postProceso.getIfAvailable(), true, Clock.systemDefaultZone());
    }

    /**
     * Generation only: the new pre-vouchers are stored but not sent (no {@link PreComprobantePostProcesoPort}); a run that
     * needs a reversal is refused.
     */
    public GeneraPreComprobanteMultiSeleccionService(NamedParameterJdbcTemplate jdbc) {
        this(jdbc, null, false, Clock.systemDefaultZone());
    }

    GeneraPreComprobanteMultiSeleccionService(NamedParameterJdbcTemplate jdbc, PreComprobantePostProcesoPort postProceso,
                                              boolean envioObligatorio, Clock clock) {
        this.jdbc = jdbc;
        this.postProceso = postProceso;
        this.envioObligatorio = envioObligatorio;
        this.clock = clock;
    }

    // ------------------------------------------------------------------ contract

    record TipoSeleccionado(@SerializedName("IdTipoContabilidad") Long idTipoContabilidad) {}

    record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                   @SerializedName("p_IdUsuario") Long idUsuario,
                   @SerializedName("p_IdModulo") String idModulo,
                   @SerializedName("p_TipoContabilidad") List<TipoSeleccionado> tipos,
                   @SerializedName("p_FechaContable") String fechaContable,
                   @SerializedName("p_Ejercicio") Long ejercicio) {
        List<TipoSeleccionado> tiposONada() { return tipos == null ? List.of() : tipos; }
    }

    static final class Salida {
        @SerializedName("P_OUT_ERRORESCONTADOR") long erroresContador;
        @SerializedName("P_OUT_MENSAJES") final List<MensajeSalida> mensajes = new ArrayList<>();
    }

    // ------------------------------------------------------------------ rows

    private record Centralizacion(Long idTipoCentralizacion, Long idCentralizacion, Long idTipoContabilidad, String nombre) {}

    private record TipoContabilidad(Long idTipoContabilidad, String nombre, String nemo, String reversa) {}

    private record PrecomprobanteExistente(Long idPreComprobante, Long idTipoContabilidad, String fechaContable) {}

    private record Condicion(Long idDefinicionAsiento, Long idTipoContabilidad, String idTipoDatoEvento, String valorVarchar,
                             BigDecimal valorDecimal, String condicion) {}

    private record MovimientoDato(Long idMovimiento, String tipoDato, String idTipoDatoEvento, String valorVarchar,
                                  BigDecimal valorDecimal) {
        String valor() { return "V".equals(tipoDato) ? valorVarchar : valorDecimal.toPlainString(); }
    }

    private record Evento(Long idEvento, String idTipoEvento, Long idMovimiento, BigDecimal monto) {}

    private record Movimiento(Long idMovimiento, Long idTipoCentralizacion, String idTipoMovimiento, Long idEmpresa,
                              Long numero, Long linea, String fecha, String documentoIdentidad, Long cuenta, Long ejercicio,
                              String periodo, Long idCentralizacion, BigDecimal monto, String estado, String idMoneda) {
        /** Movement field named by a MOV glosa (legacy reflection on MovimientoProceso, L956-965); null if unknown. */
        Object campo(String nombre) {
            if (nombre == null) return null;
            return switch (nombre) {
                case "IdMovimiento" -> idMovimiento;
                case "IdTipoCentralizacion" -> idTipoCentralizacion;
                case "IdTipoMovimiento" -> idTipoMovimiento;
                case "IdEmpresa" -> idEmpresa;
                case "Numero" -> numero;
                case "Linea" -> linea;
                case "Fecha" -> fecha;
                case "DocumentoIdentidad" -> documentoIdentidad;
                case "Cuenta" -> cuenta;
                case "Ejercicio" -> ejercicio;
                case "Periodo" -> periodo;
                case "IdCentralizacion" -> idCentralizacion;
                case "Monto" -> monto;
                case "Estado" -> estado;
                case "IdMoneda" -> idMoneda;
                default -> null;
            };
        }

        boolean tieneCampo(String nombre) {
            return nombre != null && CAMPOS_MOVIMIENTO.contains(nombre);
        }
    }

    private static final Set<String> CAMPOS_MOVIMIENTO = Set.of("IdMovimiento", "IdTipoCentralizacion", "IdTipoMovimiento",
            "IdEmpresa", "Numero", "Linea", "Fecha", "DocumentoIdentidad", "Cuenta", "Ejercicio", "Periodo", "IdCentralizacion",
            "Monto", "Estado", "IdMoneda");

    /** A movement with its data and events (legacy MovimientoProceso). */
    private record MovimientoProceso(Movimiento movimiento, List<MovimientoDato> datos, List<Evento> eventos) {}

    private record EventoDefinicion(String idTipoEvento, Long idDefinicionAsiento, String operador) {}

    private record TipoGlosa(Long idTipoContabilidad, Long idDefinicionAsiento, String origen, String glosaCampo,
                             String nombreCampoMovimiento, String idTipoDatoEvento) {}

    private record DefinicionAsiento(Long idDefinicionAsiento, Long idTipoContabilidad, String idCuentaContable,
                                     String debeHaber, String validacion) {}

    private record TipoCentralizacionLink(Long idTipoContabilidad, Long idTipoCentralizacion) {}

    /** Catalogues read once for all requested types (legacy L430-510). */
    private record Catalogos(Map<Long, Centralizacion> centralizaciones,
                             Map<Long, TipoContabilidad> tipos,
                             Map<Long, PrecomprobanteExistente> ultimos,
                             Map<Long, List<TipoCentralizacionLink>> centralizacionesPorTipo,
                             Map<Long, List<Condicion>> condicionesPorTipo,
                             Map<Long, List<DefinicionAsiento>> asientosPorTipo,
                             Map<Long, List<EventoDefinicion>> eventosPorDefinicion,
                             Map<Long, List<Condicion>> condicionesPorDefinicion,
                             Map<Long, List<MovimientoProceso>> movimientosPorTipoCentralizacion,
                             List<TipoGlosa> glosas) {}

    /** Accumulated asiento of one definition while movements are matched (legacy PreComprobanteAsientoProceso). */
    private static final class AsientoEnProceso {
        final String idCuentaContable;
        BigDecimal monto = BigDecimal.ZERO;
        BigDecimal montoDebe = BigDecimal.ZERO;
        BigDecimal montoHaber = BigDecimal.ZERO;
        final List<EventoAsiento> eventos = new ArrayList<>();

        AsientoEnProceso(String idCuentaContable) { this.idCuentaContable = idCuentaContable; }
    }

    private record EventoAsiento(Long idEvento, String operador, String glosaDetalle, String debeHaber) {}

    private record PrecomprobanteEnProceso(Long idTipoContabilidad, String glosa, List<AsientoEnProceso> asientos) {}

    // ------------------------------------------------------------------ flow

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.legacy(), params, Entrada.class);
        return JavaCallJson.write(JavaCallJson.legacy(), generar(entrada));
    }

    Salida generar(Entrada entrada) {
        Salida salida = new Salida();
        List<Long> ids = entrada.tiposONada().stream().map(TipoSeleccionado::idTipoContabilidad).distinct().toList();
        Catalogos c = leerCatalogos(entrada, ids);

        List<Long> aProcesar = new ArrayList<>();
        for (TipoSeleccionado t : entrada.tiposONada()) {
            if (valida(entrada, t.idTipoContabilidad(), c, salida)) aProcesar.add(t.idTipoContabilidad());
        }
        if (aProcesar.isEmpty()) return salida;

        List<Long> aReversar = new ArrayList<>();
        List<PrecomprobanteEnProceso> generados = new ArrayList<>();
        for (Long idTipo : aProcesar) {
            TipoContabilidad tipo = c.tipos().get(idTipo);
            PrecomprobanteExistente anterior = c.ultimos().get(idTipo);
            if ("S".equals(tipo.reversa()) && anterior != null) aReversar.add(anterior.idPreComprobante());

            PrecomprobanteEnProceso p = construir(idTipo, tipo, c);
            if (p.asientos().isEmpty()) {
                salida.mensajes.add(MensajeSalida.advertencia(idTipo, "NO_EXISTEN_MOVIMIENTOS_CENTRALIZACIONES_ASOCIADAS", valoresTipo(tipo)));
            } else {
                generados.add(p);
            }
        }
        if (!aReversar.isEmpty() && postProceso == null) {
            throw new FuncionalidadNoMigradaException("GeneraPreComprobanteMultiSeleccion: ReversaPreComprobante not available");
        }
        if (!generados.isEmpty() && envioObligatorio && postProceso == null) {
            throw new FuncionalidadNoMigradaException("GeneraPreComprobanteMultiSeleccion: EnvioContabilidad not available");
        }
        guardarYEnviar(entrada, c, generados, aReversar, salida);
        return salida;
    }

    private Catalogos leerCatalogos(Entrada entrada, List<Long> ids) {
        MapSqlParameterSource porTipo = new MapSqlParameterSource("IdTipoContabilidad", ids);
        MapSqlParameterSource porTipoEmpresa = new MapSqlParameterSource()
                .addValue("IdTipoContabilidad", ids).addValue("IdEmpresa", entrada.idEmpresa());
        MapSqlParameterSource porTipoFecha = new MapSqlParameterSource()
                .addValue("IdTipoContabilidad", ids).addValue("FechaContable", entrada.fechaContable())
                .addValue("IdEmpresa", entrada.idEmpresa());

        Map<Long, Centralizacion> centralizaciones = unico(jdbc.query(SQL_SELECT_CENTRALIZACION, porTipoFecha, SqlRow.mapper(r ->
                new Centralizacion(r.lng("IdTipoCentralizacion"), r.lng("IdCentralizacion"), r.lng("IdTipoContabilidad"), r.str("Nombre")))),
                Centralizacion::idTipoContabilidad);
        Map<Long, TipoContabilidad> tipos = unico(jdbc.query(SQL_SELECT_TIPO_CONTABILIDAD, porTipoEmpresa, SqlRow.mapper(r ->
                new TipoContabilidad(r.lng("IdTipoContabilidad"), r.str("Nombre"), r.str("Nemo"), r.str("Reversa")))),
                TipoContabilidad::idTipoContabilidad);
        Map<Long, PrecomprobanteExistente> ultimos = unico(jdbc.query(SQL_SELECT_PRECOMP_ULTIMO, porTipoEmpresa, SqlRow.mapper(r ->
                new PrecomprobanteExistente(r.lng("IdPreComprobante"), r.lng("IdTipoContabilidad"), r.str("FechaContable")))),
                PrecomprobanteExistente::idTipoContabilidad);
        Map<Long, List<TipoCentralizacionLink>> centralizacionesPorTipo = agrupar(jdbc.query(SQL_SELECT_TC_CENTRALIZACION, porTipo,
                SqlRow.mapper(r -> new TipoCentralizacionLink(r.lng("IdTipoContabilidad"), r.lng("IdTipoCentralizacion")))),
                TipoCentralizacionLink::idTipoContabilidad);
        Map<Long, List<Condicion>> condicionesPorTipo = agrupar(jdbc.query(SQL_SELECT_CONDICIONES_ASIENTO, porTipo, CONDICION),
                Condicion::idTipoContabilidad);
        Map<Long, List<DefinicionAsiento>> asientosPorTipo = agrupar(jdbc.query(SQL_SELECT_ASIENTO, porTipo, SqlRow.mapper(r ->
                new DefinicionAsiento(r.lng("IdDefinicionAsiento"), r.lng("IdTipoContabilidad"), r.str("IdCuentaContable"),
                        r.str("DebeHaber"), r.str("Validacion")))), DefinicionAsiento::idTipoContabilidad);
        Map<Long, List<EventoDefinicion>> eventosPorDefinicion = agrupar(jdbc.query(SQL_EVENTOS_ASIENTO, porTipo, SqlRow.mapper(r ->
                new EventoDefinicion(r.str("IdTipoEvento"), r.lng("IdDefinicionAsiento"), r.str("Operador")))),
                EventoDefinicion::idDefinicionAsiento);
        Map<Long, List<Condicion>> condicionesPorDefinicion = agrupar(jdbc.query(SQL_SELECT_CONDICIONES_PRECOMP, porTipo, CONDICION),
                Condicion::idDefinicionAsiento);
        Map<Long, List<MovimientoDato>> datosPorMovimiento = agrupar(jdbc.query(SQL_SELECT_MOVIMIENTOS_DATOS, porTipoFecha, SqlRow.mapper(r ->
                new MovimientoDato(r.lng("IdMovimiento"), r.str("TipoDato"), r.str("IdTipoDatoEvento"), r.str("ValorVarchar"),
                        r.dec("ValorDecimal")))), MovimientoDato::idMovimiento);
        List<Movimiento> movimientos = jdbc.query(SQL_SELECT_MOVIMIENTOS, porTipoFecha, SqlRow.mapper(r -> new Movimiento(
                r.lng("IdMovimiento"), r.lng("IdTipoCentralizacion"), r.str("IdTipoMovimiento"), r.lng("IdEmpresa"), r.lng("Numero"),
                r.lng("Linea"), r.str("Fecha"), r.str("DocumentoIdentidad"), r.lng("Cuenta"), r.lng("Ejercicio"), r.str("Periodo"),
                r.lng("IdCentralizacion"), r.dec("Monto"), r.str("Estado"), r.str("IdMoneda"))));
        Map<Long, List<Evento>> eventosPorMovimiento = agrupar(jdbc.query(SQL_SELECT_EVENTOS, porTipoFecha, SqlRow.mapper(r ->
                new Evento(r.lng("IdEvento"), r.str("IdTipoEvento"), r.lng("IdMovimiento"), r.dec("Monto")))), Evento::idMovimiento);
        List<TipoGlosa> glosas = jdbc.query(SQL_SELECT_TIPO_GLOSA, porTipo, SqlRow.mapper(r -> new TipoGlosa(r.lng("IdTipoContabilidad"),
                r.lng("IdDefinicionAsiento"), r.str("Origen"), r.str("GlosaCampo"), r.str("NombreCampoMovimiento"), r.str("IdTipoDatoEvento"))));

        Map<Long, List<MovimientoProceso>> movimientosPorTipoCentralizacion = agrupar(movimientos.stream()
                .map(m -> new MovimientoProceso(m, datosPorMovimiento.getOrDefault(m.idMovimiento(), List.of()),
                        eventosPorMovimiento.getOrDefault(m.idMovimiento(), List.of())))
                .toList(), mp -> mp.movimiento().idTipoCentralizacion());

        return new Catalogos(centralizaciones, tipos, ultimos, centralizacionesPorTipo, condicionesPorTipo, asientosPorTipo,
                eventosPorDefinicion, condicionesPorDefinicion, movimientosPorTipoCentralizacion, glosas);
    }

    private static final RowMapper<Condicion> CONDICION = SqlRow.mapper(r -> new Condicion(r.lng("IdDefinicionAsiento"),
            r.lng("IdTipoContabilidad"), r.str("IdTipoDatoEvento"), r.str("ValorVarchar"), r.dec("ValorDecimal"), r.str("Condicion")));

    /** Legacy L513-549: the four reasons to skip a type, in this order. */
    private boolean valida(Entrada entrada, Long idTipo, Catalogos c, Salida salida) {
        TipoContabilidad tipo = c.tipos().get(idTipo);
        if (tipo == null) {
            salida.mensajes.add(MensajeSalida.advertencia(idTipo, "NO_EXISTE_TIPO_CONTABILIDAD", valores("TipoContabilidad", String.valueOf(idTipo))));
            return false;
        }
        List<String> cuentasInvalidas = c.asientosPorTipo().getOrDefault(idTipo, List.of()).stream()
                .filter(a -> "ERR".equals(a.validacion())).map(DefinicionAsiento::idCuentaContable).toList();
        if (!cuentasInvalidas.isEmpty()) {
            for (String cuenta : cuentasInvalidas) {
                Map<String, String> v = valoresTipo(tipo);
                v.put("CuentaContable", cuenta);
                salida.mensajes.add(MensajeSalida.advertencia(idTipo, "NO_EXISTE_CUENTA_CONTABLE_ASIENTO", v));
            }
            return false;
        }
        if (c.centralizaciones().get(idTipo) == null) {
            salida.mensajes.add(MensajeSalida.advertencia(idTipo, "NO_EXISTEN_CENTRALIZACIONES_ASOCIADAS", valoresTipo(tipo)));
            return false;
        }
        PrecomprobanteExistente existente = c.ultimos().get(idTipo);
        if (existente != null && entrada.fechaContable().equals(existente.fechaContable()) && "N".equals(tipo.reversa())) {
            Map<String, String> v = valores("NombreTipoContabilidad", tipo.nombre());
            v.put("IdPreComprobante", String.valueOf(existente.idPreComprobante()));
            salida.mensajes.add(MensajeSalida.advertencia(idTipo, "YA_EXISTE_PRECOMPROBANTE", v));
            return false;
        }
        return true;
    }

    /** Builds the asientos of one voucher type (legacy L587-783). */
    private PrecomprobanteEnProceso construir(Long idTipo, TipoContabilidad tipo, Catalogos c) {
        String glosa = tipo.nombre() + "/" + tipo.nemo();
        List<DefinicionAsiento> definiciones = c.asientosPorTipo().getOrDefault(idTipo, List.of());
        List<TipoGlosa> glosasPorDefecto = c.glosas().stream()
                .filter(g -> Long.valueOf(0L).equals(g.idDefinicionAsiento()) && idTipo.equals(g.idTipoContabilidad())).toList();

        Map<String, List<Condicion>> condicionesComprobante = porTipoDato(c.condicionesPorTipo().getOrDefault(idTipo, List.of()));
        Set<Long> vistos = new HashSet<>();
        List<MovimientoProceso> movimientos = new ArrayList<>();
        for (Long idTipoCentralizacion : c.centralizacionesPorTipo().getOrDefault(idTipo, List.of()).stream()
                .map(TipoCentralizacionLink::idTipoCentralizacion).distinct().toList()) {
            for (MovimientoProceso mp : c.movimientosPorTipoCentralizacion().getOrDefault(idTipoCentralizacion, List.of())) {
                if (!vistos.contains(mp.movimiento().idMovimiento()) && cumple(mp, condicionesComprobante)) {
                    movimientos.add(mp);
                    vistos.add(mp.movimiento().idMovimiento());
                }
            }
        }

        // Asientos in definition order; legacy kept them in a HashMap keyed by IdDefinicionAsiento.
        Map<Long, AsientoEnProceso> asientos = new LinkedHashMap<>();
        for (MovimientoProceso mp : movimientos) {
            Map<String, List<Evento>> eventosPorTipo = mp.eventos().stream()
                    .collect(Collectors.groupingBy(Evento::idTipoEvento, LinkedHashMap::new, Collectors.toList()));
            for (DefinicionAsiento d : definiciones) {
                AsientoEnProceso asiento = asientos.computeIfAbsent(d.idDefinicionAsiento(), k -> new AsientoEnProceso(d.idCuentaContable()));
                if (!cumple(mp, porTipoDato(c.condicionesPorDefinicion().getOrDefault(d.idDefinicionAsiento(), List.of())))) continue;

                List<TipoGlosa> glosasDefinicion = c.glosas().stream()
                        .filter(g -> Objects.equals(d.idDefinicionAsiento(), g.idDefinicionAsiento())).toList();
                String glosaDetalle = glosaDetalle(mp, glosasDefinicion.isEmpty() ? glosasPorDefecto : glosasDefinicion);
                BigDecimal montoMovimiento = BigDecimal.ZERO;
                for (EventoDefinicion ed : c.eventosPorDefinicion().getOrDefault(d.idDefinicionAsiento(), List.of())) {
                    for (Evento e : eventosPorTipo.getOrDefault(ed.idTipoEvento(), List.of())) {
                        BigDecimal monto = conOperador(ed.operador(), e.monto());
                        String lado = esDebe(d.debeHaber(), monto) ? "D" : esHaber(d.debeHaber(), monto) ? "H" : null;
                        asiento.eventos.add(new EventoAsiento(e.idEvento(), ed.operador(), glosaDetalle, lado));
                        montoMovimiento = montoMovimiento.add(monto, MathContext.DECIMAL128);
                    }
                }
                asiento.monto = asiento.monto.add(montoMovimiento, MathContext.DECIMAL128);
                if (esDebe(d.debeHaber(), asiento.monto)) {
                    asiento.montoDebe = asiento.monto.abs(MathContext.DECIMAL128);
                    asiento.montoHaber = BigDecimal.ZERO;
                }
                if (esHaber(d.debeHaber(), asiento.monto)) {
                    asiento.montoHaber = asiento.monto.abs(MathContext.DECIMAL128);
                    asiento.montoDebe = BigDecimal.ZERO;
                }
            }
        }
        List<AsientoEnProceso> conEventos = asientos.values().stream().filter(a -> !a.eventos.isEmpty()).toList();
        return new PrecomprobanteEnProceso(idTipo, glosa, conEventos);
    }

    /** Legacy L786-877: ids, Estado D on 2-decimal mismatch, reversal, inserts, sending, PRE_COMPROBANTE_GENERADO. */
    private void guardarYEnviar(Entrada entrada, Catalogos c, List<PrecomprobanteEnProceso> generados, List<Long> aReversar,
                                Salida salida) {
        int totalAsientos = generados.stream().mapToInt(p -> p.asientos().size()).sum();
        List<Long> idsPrecomprobante = HanaSequences.next(jdbc, SEQ_PRECOMPROBANTE, generados.size());
        List<Long> idsAsiento = HanaSequences.next(jdbc, SEQ_PRECOMPROBANTE_ASIENTO, totalAsientos);
        LocalDateTime ahora = LocalDateTime.now(clock);
        String fechaProceso = LocalDateTime.now(clock.withZone(ZoneOffset.UTC)).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String horaProceso = ahora.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        List<MapSqlParameterSource> precomprobantes = new ArrayList<>();
        List<MapSqlParameterSource> asientos = new ArrayList<>();
        List<MapSqlParameterSource> eventos = new ArrayList<>();
        Map<Long, Long> tipoPorPrecomprobante = new LinkedHashMap<>();
        int j = 0;
        for (int i = 0; i < generados.size(); i++) {
            PrecomprobanteEnProceso p = generados.get(i);
            Long idPrecomprobante = idsPrecomprobante.get(i);
            tipoPorPrecomprobante.put(idPrecomprobante, p.idTipoContabilidad());
            BigDecimal debe = BigDecimal.ZERO;
            BigDecimal haber = BigDecimal.ZERO;
            long linea = 1;
            for (AsientoEnProceso a : p.asientos()) {
                Long idAsiento = idsAsiento.get(j++);
                debe = debe.add(a.montoDebe, MathContext.DECIMAL128);
                haber = haber.add(a.montoHaber, MathContext.DECIMAL128);
                asientos.add(new MapSqlParameterSource()
                        .addValue("IdPreComprobanteAsiento", idAsiento)
                        .addValue("IdEmpresa", entrada.idEmpresa())
                        .addValue("IdPreComprobante", idPrecomprobante)
                        .addValue("IdCuentaContable", a.idCuentaContable)
                        .addValue("Ejercicio", entrada.ejercicio())
                        .addValue("Linea", linea++)
                        .addValue("MontoDebe", a.montoDebe)
                        .addValue("MontoHaber", a.montoHaber)
                        .addValue("FechaContable", entrada.fechaContable())
                        .addValue("Glosa", p.glosa())
                        .addValue("Estado", "V"));
                for (EventoAsiento e : a.eventos) {
                    eventos.add(new MapSqlParameterSource()
                            .addValue("IdPreComprobanteAsiento", idAsiento)
                            .addValue("IdEvento", e.idEvento())
                            .addValue("Operador", e.operador())
                            .addValue("Linea", 0L)
                            .addValue("Estado", "V")
                            .addValue("GlosaDetalle", e.glosaDetalle())
                            .addValue("DebeHaber", e.debeHaber()));
                }
            }
            boolean cuadrado = debe.setScale(2, RoundingMode.HALF_UP).compareTo(haber.setScale(2, RoundingMode.HALF_UP)) == 0;
            precomprobantes.add(new MapSqlParameterSource()
                    .addValue("IdPreComprobante", idPrecomprobante)
                    .addValue("IdEmpresa", entrada.idEmpresa())
                    .addValue("CodigoUsuario", String.valueOf(entrada.idUsuario()))
                    .addValue("IdTipoContabilidad", p.idTipoContabilidad())
                    .addValue("Ejercicio", entrada.ejercicio())
                    .addValue("FechaContable", entrada.fechaContable())
                    .addValue("Glosa", p.glosa())
                    .addValue("FechaProceso", fechaProceso)
                    .addValue("HoraProceso", horaProceso)
                    .addValue("FolioContabilidad", 0L)
                    .addValue("IdReversa", 0L)
                    .addValue("Estado", cuadrado ? "V" : "D"));
        }

        if (!aReversar.isEmpty()) {
            agregar(salida, postProceso.reversar(entrada.idEmpresa(), String.valueOf(entrada.idUsuario()), entrada.fechaContable(), aReversar));
        }
        jdbc.batchUpdate(SQL_INSERT_PRECOMPROBANTES, SqlParams.batch(precomprobantes));
        jdbc.batchUpdate(SQL_INSERT_PRECOMP_ASIENTOS, SqlParams.batch(asientos));
        jdbc.batchUpdate(SQL_INSERT_EVENTO_PRECOMP, SqlParams.batch(eventos));
        if (!idsPrecomprobante.isEmpty() && postProceso != null) {
            agregar(salida, postProceso.enviar(entrada.idEmpresa(), entrada.idUsuario(), entrada.idModulo(), "S", "B", idsPrecomprobante));
        }

        // Legacy matches message ids against pre-voucher ids, whatever the id stands for (L862-877).
        Set<Long> conProblemas = salida.mensajes.stream()
                .filter(m -> m.id() != null && (MensajeSalida.ADVERTENCIA.equals(m.tipo()) || MensajeSalida.ERROR.equals(m.tipo())))
                .map(MensajeSalida::id).collect(Collectors.toSet());
        for (Map.Entry<Long, Long> e : tipoPorPrecomprobante.entrySet()) {
            if (conProblemas.contains(e.getKey())) continue;
            Map<String, String> v = valores("NombreTipoContabilidad", c.tipos().get(e.getValue()).nombre());
            v.put("IdPreComprobante", String.valueOf(e.getKey()));
            salida.mensajes.add(MensajeSalida.informacion(e.getKey(), "PRE_COMPROBANTE_GENERADO", v));
        }
    }

    private static void agregar(Salida salida, PreComprobantePostProcesoPort.Resultado r) {
        salida.erroresContador += r.erroresContador();
        salida.mensajes.addAll(r.mensajes());
    }

    // ------------------------------------------------------------------ rules

    /**
     * Legacy {@code comparar} (L886-924): every IdTipoDatoEvento with conditions must be satisfied by the movement data.
     * Within a type, a condition is evaluated when there is no result yet, when the result is false and the condition
     * is {@code =} (OR), or when the result is true and the condition is not {@code =} (AND).
     */
    private static boolean cumple(MovimientoProceso mp, Map<String, List<Condicion>> condiciones) {
        if (condiciones.isEmpty()) return true;
        Map<String, Boolean> resultado = new HashMap<>();
        for (MovimientoDato dato : mp.datos()) {
            for (Condicion c : condiciones.getOrDefault(dato.idTipoDatoEvento(), List.of())) {
                Boolean previo = resultado.get(c.idTipoDatoEvento());
                boolean esOr = "=".equals(c.condicion());
                if (previo == null || (!previo && esOr) || (previo && !esOr)) {
                    resultado.put(c.idTipoDatoEvento(), evaluar(dato, c));
                }
            }
        }
        return condiciones.keySet().stream().allMatch(k -> Boolean.TRUE.equals(resultado.get(k)));
    }

    private static boolean evaluar(MovimientoDato dato, Condicion c) {
        boolean texto = "V".equals(dato.tipoDato());
        return switch (c.condicion() == null ? "" : c.condicion()) {
            case "=" -> texto ? Objects.equals(dato.valorVarchar(), c.valorVarchar()) : Objects.equals(dato.valorDecimal(), c.valorDecimal());
            case "<>" -> texto ? !Objects.equals(dato.valorVarchar(), c.valorVarchar()) : !Objects.equals(dato.valorDecimal(), c.valorDecimal());
            case ">" -> texto ? dato.valorVarchar().compareTo(c.valorVarchar()) == 1 : dato.valorDecimal().compareTo(c.valorDecimal()) == 1;
            case "<" -> texto ? dato.valorVarchar().compareTo(c.valorVarchar()) == -1 : dato.valorDecimal().compareTo(c.valorDecimal()) == -1;
            default -> false;
        };
    }

    /** Legacy {@code aplicarOperador}: absolute value, negated for {@code -}. */
    private static BigDecimal conOperador(String operador, BigDecimal monto) {
        BigDecimal abs = monto.abs(MathContext.DECIMAL128);
        return "-".equals(operador) ? abs.negate(MathContext.DECIMAL128) : abs;
    }

    private static boolean esDebe(String debeHaber, BigDecimal monto) {
        return ("D".equals(debeHaber) && monto.signum() > 0) || ("H".equals(debeHaber) && monto.signum() < 0);
    }

    private static boolean esHaber(String debeHaber, BigDecimal monto) {
        return ("H".equals(debeHaber) && monto.signum() > 0) || ("D".equals(debeHaber) && monto.signum() < 0);
    }

    /** Legacy {@code crearGlosa} (L939-970): " campo valor" per DAT field present, " campo valor" per known MOV field. */
    private static String glosaDetalle(MovimientoProceso mp, List<TipoGlosa> glosas) {
        Map<String, MovimientoDato> datos = new HashMap<>();
        for (MovimientoDato d : mp.datos()) if (d.idTipoDatoEvento() != null) datos.putIfAbsent(d.idTipoDatoEvento(), d);
        StringBuilder sb = new StringBuilder();
        for (TipoGlosa g : glosas) {
            if ("DAT".equals(g.origen())) {
                MovimientoDato d = datos.get(g.idTipoDatoEvento());
                if (d != null) sb.append(' ').append(g.glosaCampo()).append(' ').append(d.valor());
            }
            if ("MOV".equals(g.origen()) && mp.movimiento().tieneCampo(g.nombreCampoMovimiento())) {
                sb.append(' ').append(g.glosaCampo()).append(' ').append(mp.movimiento().campo(g.nombreCampoMovimiento()));
            }
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ helpers

    private static Map<String, List<Condicion>> porTipoDato(List<Condicion> condiciones) {
        return condiciones.stream().collect(Collectors.groupingBy(Condicion::idTipoDatoEvento, LinkedHashMap::new, Collectors.toList()));
    }

    private static <T> Map<Long, List<T>> agrupar(List<T> filas, Function<T, Long> clave) {
        Map<Long, List<T>> m = new LinkedHashMap<>();
        for (T f : filas) m.computeIfAbsent(clave.apply(f), k -> new ArrayList<>()).add(f);
        return m;
    }

    /** Legacy {@code Utils.List2Hash}: the key must be unique. */
    private static <T> Map<Long, T> unico(List<T> filas, Function<T, Long> clave) {
        Map<Long, T> m = new LinkedHashMap<>();
        for (T f : filas) {
            if (m.putIfAbsent(clave.apply(f), f) != null) throw new IllegalStateException("key field is not unique: " + clave.apply(f));
        }
        return m;
    }

    private static Map<String, String> valoresTipo(TipoContabilidad tipo) {
        Map<String, String> v = valores("NombreTipoContabilidad", tipo.nombre());
        v.put("TipoContabilidad", String.valueOf(tipo.idTipoContabilidad()));
        return v;
    }

    private static Map<String, String> valores(String clave, String valor) {
        Map<String, String> m = new HashMap<>();
        m.put(clave, valor);
        return m;
    }
}
