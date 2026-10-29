package cl.bcs.contabilidad.service;

import cl.bcs.contabilidad.domain.model.EntradaCentralizacion;
import cl.bcs.contabilidad.domain.model.MensajeSalida;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.BcsCentralizacionService;
import cl.bcs.contabilidad.domain.service.CentralizaGenerico;
import cl.bcs.contabilidad.infrastructure.persistence.SqlResources;
import cl.bcs.contabilidad.infrastructure.persistence.SqlRow;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * JavaCall entry point of a (multi-type) centralization run.
 *
 * <p>Legacy {@code cl.bcs.contabilidad.CentralizaMultiSeleccion}. Implements CONT-G-460..464. Flow (legacy L171-424):
 * <ol>
 *   <li>read the configuration of the distinct requested types; unknown type -> ADVERTENCIA UI_CENTRALIZACION_NO_EXISTE,
 *       type without Tipo -> ADVERTENCIA UI_CENTRALIZACION_CONFIGURACION_NO_EXISTE; related types are added;</li>
 *   <li>real run in a CL installation: already centralized types are rejected, the valuation-report date is checked;</li>
 *   <li>each type runs as a stored procedure ({@code P}, followed by the datos-movimiento cleanup DELETE) or as a Java
 *       centralizer ({@code J}, a {@link BcsCentralizacionService} bean); a type that fails gets an error header
 *       ({@link CentralizaGeneraCabeceraService});</li>
 *   <li>the procedure messages of the processed types are mapped RECH -> ERROR (+1), ADVE/RAUT -> AUTORIZACION,
 *       anything else -> INFORMACION;</li>
 *   <li>real run ({@code p_SimulaOperacion = N}): each Java type persists its own tables through
 *       {@code ContabilidadRepository.persistirCentralizacion} (the CentralizaGenerico rules, legacy L411-417); the
 *       CENTRALIZACIONES_GENERADA_CORRECTAMENTE messages of the types that completed are appended last (L419-420).</li>
 * </ol>
 * Legacy persisted all Java types in one CentralizaGenerico call after the loop; here each type is persisted, in its
 * own transaction, when it runs. A type whose persistence fails is handled like any failed type (error header).
 */
@Service
public class CentralizaMultiSeleccionService {

    private static final Logger log = LoggerFactory.getLogger(CentralizaMultiSeleccionService.class);
    private static final Pattern NOMBRE_PROCEDIMIENTO = Pattern.compile("[A-Za-z0-9_.:]+");

    private final String SQL_SELECT_INFORMES = SqlResources.load("Centraliza_SelectInformesValorizados.sql");
    private final String SQL_SELECT_CONFIG = SqlResources.load("CentralizaMultiSeleccion_SelectConfiguraciones.sql");
    private final String SQL_SELECT_CALL_CENT = SqlResources.load("CentralizaMultiSeleccion_CallCentralizacion.sql");
    private final String SQL_SELECT_MENSAJES = SqlResources.load("CentralizaMultiSeleccion_SelectMensajes.sql");
    private final String SQL_SELECT_VIGENTES = SqlResources.load("CentralizaMultiSeleccion_SelectCentralizacionesVigentes.sql");
    private final String SQL_DELETE_MOVIMIENTOS = SqlResources.load("CentralizaMultiSeleccion_DeleteDatosMovimientos.sql");
    private final String SQL_SELECT_RUTAS = SqlResources.load("CentralizaMultiSeleccion_SelectRutaInformes.sql");

    private final NamedParameterJdbcTemplate jdbc;
    private final PlataformaPort plataforma;
    private final Map<String, BcsCentralizacionService> centralizadores;
    private final CentralizaGeneraCabeceraService cabecera;

    @Autowired
    public CentralizaMultiSeleccionService(NamedParameterJdbcTemplate jdbc,
                                           ObjectProvider<PlataformaPort> plataforma,
                                           Map<String, BcsCentralizacionService> centralizadores,
                                           CentralizaGeneraCabeceraService cabecera) {
        this(jdbc, plataforma.getIfAvailable(), centralizadores, cabecera);
    }

    /** Without platform access the system localization is unknown, so the CL-only checks are skipped. */
    public CentralizaMultiSeleccionService(NamedParameterJdbcTemplate jdbc) {
        this(jdbc, (PlataformaPort) null, Map.of(), new CentralizaGeneraCabeceraService(jdbc));
    }

    CentralizaMultiSeleccionService(NamedParameterJdbcTemplate jdbc, PlataformaPort plataforma,
                                    Map<String, BcsCentralizacionService> centralizadores,
                                    CentralizaGeneraCabeceraService cabecera) {
        this.jdbc = jdbc;
        this.plataforma = plataforma;
        this.centralizadores = centralizadores;
        this.cabecera = cabecera;
    }

    // ------------------------------------------------------------------ contract

    public record TipoSeleccionado(@SerializedName("IdTipoCentralizacion") Long idTipoCentralizacion) {}

    public record Entrada(@SerializedName("p_IdEmpresa") Long idEmpresa,
                          @SerializedName("p_IdUsuario") Long idUsuario,
                          @SerializedName("p_IdModulo") String idModulo,
                          @SerializedName("p_FechaContable") String fechaContable,
                          @SerializedName("p_FechaConsulta") String fechaConsulta,
                          @SerializedName("p_Ejercicio") Long ejercicio,
                          @SerializedName("p_SimulaOperacion") String simulaOperacion,
                          @SerializedName("p_in_TiposCentralizacion") List<TipoSeleccionado> tipos) {
        /** Legacy default {@code p_SimulaOperacion = "N"}. */
        boolean simula() { return !"N".equals(simulaOperacion == null ? "N" : simulaOperacion); }
        String simulaONo() { return simulaOperacion == null ? "N" : simulaOperacion; }
        List<TipoSeleccionado> tiposONada() { return tipos == null ? List.of() : tipos; }
    }

    /** One centralized movement, legacy {@code cl.bcs.contabilidad.pojo.ResultadoCentralizacion}. */
    public record Resultado(@SerializedName("IdTipoCentralizacion") Long idTipoCentralizacion,
                            @SerializedName("IdMovimiento") Long idMovimiento,
                            @SerializedName("IdTipoMovimiento") String idTipoMovimiento,
                            @SerializedName("IdEmpresa") Long idEmpresa,
                            @SerializedName("Numero") Long numero,
                            @SerializedName("Linea") Long linea,
                            @SerializedName("Fecha") String fecha,
                            @SerializedName("DocumentoIdentidad") String documentoIdentidad,
                            @SerializedName("IdPortafolio") Long idPortafolio,
                            @SerializedName("Ejercicio") String ejercicio,
                            @SerializedName("Periodo") String periodo,
                            @SerializedName("Monto") java.math.BigDecimal monto,
                            @SerializedName("IdMoneda") String idMoneda,
                            @SerializedName("Estado") String estado) {
        static Resultado of(cl.bcs.contabilidad.domain.model.ResultadoCentralizacion r) {
            return new Resultado(r.idTipoCentralizacion(), r.idMovimiento(), r.idTipoMovimiento(), r.idEmpresa(),
                    r.numero(), r.linea(), r.fecha(), r.documentoIdentidad(), r.idPortafolio(), r.ejercicio(),
                    r.periodo(), r.monto(), r.idMoneda(), r.estado());
        }

        static Resultado of(SqlRow r) {
            return new Resultado(r.lng("IdTipoCentralizacion"), r.lng("IdMovimiento"), r.str("IdTipoMovimiento"),
                    r.lng("IdEmpresa"), r.lng("Numero"), r.lng("Linea"), r.str("Fecha"), r.str("DocumentoIdentidad"),
                    r.lng("IdPortafolio"), r.str("Ejercicio"), r.str("Periodo"), r.dec("Monto"), r.str("IdMoneda"),
                    r.str("Estado"));
        }
    }

    /** Legacy {@code SalidaCentralizaMultiSeleccion}. */
    public static final class Salida {
        @SerializedName("P_OUT_ERRORESCONTADOR") long erroresContador;
        @SerializedName("P_OUT_MENSAJES") final List<MensajeSalida> mensajes = new ArrayList<>();
        @SerializedName("P_OUT_RESULTADO") final List<Resultado> resultado = new ArrayList<>();

        public long erroresContador() { return erroresContador; }
        public List<MensajeSalida> mensajes() { return mensajes; }
        public List<Resultado> resultado() { return resultado; }

        void error(MensajeSalida m) {
            erroresContador++;
            mensajes.add(m);
        }
    }

    record Configuracion(Long idTipoCentralizacion, String nombre, String servicio, String tipo,
                         Long idTipoCentralizacionOrigen, String requiereFechaConsulta) {}

    private record RutaInforme(Long idTipoCentralizacion, String informeAsociado, String llaveInformeAsociado) {}

    private record CodigoMensaje(Long idInterno, Long idSesion, String codigo, String codigoDato1, String valorDato1,
                                 String resultado) {}

    // ------------------------------------------------------------------ flow

    public JsonObject execute(JsonObject params) {
        Entrada entrada = JavaCallJson.read(JavaCallJson.legacy(), params, Entrada.class);
        return JavaCallJson.write(JavaCallJson.legacy(), centralizar(entrada));
    }

    public Salida centralizar(Entrada entrada) {
        Salida salida = new Salida();
        Map<Long, RutaInforme> rutas = rutasInformes();
        String localizacion = plataforma == null ? null : plataforma.obtenerLocalizacionSistema(entrada.idEmpresa());

        List<Long> solicitados = new ArrayList<>(new LinkedHashSet<>(
                entrada.tiposONada().stream().map(TipoSeleccionado::idTipoCentralizacion).toList()));
        List<Configuracion> configuraciones = configuraciones(solicitados);
        Map<Long, Configuracion> porId = new LinkedHashMap<>();
        for (Configuracion c : configuraciones) porId.put(c.idTipoCentralizacion(), c);

        List<Long> procesados = new ArrayList<>();
        List<Configuracion> aProcesar = new ArrayList<>();
        for (TipoSeleccionado t : entrada.tiposONada()) {
            Long id = t.idTipoCentralizacion();
            Configuracion c = porId.get(id);
            if (c == null) {
                salida.mensajes.add(MensajeSalida.advertencia(0L, "UI_CENTRALIZACION_NO_EXISTE", valores("Centralizacion", String.valueOf(id))));
            } else if (c.tipo() == null) {
                salida.mensajes.add(MensajeSalida.advertencia(0L, "UI_CENTRALIZACION_CONFIGURACION_NO_EXISTE", valores("Centralizacion", c.nombre())));
            } else if (!procesados.contains(id)) {
                procesados.add(id);
                aProcesar.add(c);
                for (Configuracion relacionada : configuraciones) {
                    if (id.equals(relacionada.idTipoCentralizacionOrigen()) && !procesados.contains(relacionada.idTipoCentralizacion())) {
                        procesados.add(relacionada.idTipoCentralizacion());
                        aProcesar.add(relacionada);
                    }
                }
            }
        }

        Map<Long, String> informes = new HashMap<>();
        if (!entrada.simula() && "CL".equals(localizacion)) {
            if (rechazaVigentes(entrada, procesados, aProcesar, salida)) return salida;
            validaInformes(entrada, configuraciones, rutas, aProcesar, informes, salida);
        }

        List<CentralizaGeneraCabeceraService.TipoSeleccionado> conError = new ArrayList<>();
        List<CentralizaGenerico.MensajeSalida> generico = new ArrayList<>();
        for (Configuracion conf : aProcesar) {
            try {
                if ("P".equals(conf.tipo())) ejecutarProcedimiento(entrada, conf, salida);
                if ("J".equals(conf.tipo())) {
                    List<CentralizaGenerico.MensajeSalida> delTipo = new ArrayList<>();
                    CentralizaGenerico.recogiendoMensajes(delTipo, () -> {
                        ejecutarJava(entrada, conf, informes.get(conf.idTipoCentralizacion()), salida);
                        return null;
                    });
                    generico.addAll(delTipo);
                }
            } catch (RuntimeException e) {
                log.error("CentralizaMultiSeleccion: tipo {} failed", conf.idTipoCentralizacion(), e);
                conError.add(new CentralizaGeneraCabeceraService.TipoSeleccionado(conf.idTipoCentralizacion()));
            }
        }
        if (!conError.isEmpty()) {
            salida.mensajes.addAll(cabecera.generar(new CentralizaGeneraCabeceraService.Entrada(entrada.idEmpresa(),
                    entrada.ejercicio(), entrada.fechaContable(), entrada.idUsuario(), conError)).mensajes());
        }

        agregarMensajesProcedimiento(procesados, porId, salida);
        // Legacy L419-420: the CentralizaGenerico messages (its error counter is always 0, so nothing is added).
        generico.forEach(m -> salida.mensajes.add(MensajeSalida.of(m.id(), m.tipo(), m.codigo(), m.valores())));
        return salida;
    }

    private Map<Long, RutaInforme> rutasInformes() {
        Map<Long, RutaInforme> rutas = new HashMap<>();
        jdbc.query(SQL_SELECT_RUTAS, new MapSqlParameterSource(), SqlRow.mapper(r ->
                        new RutaInforme(r.lng("IdTipoCentralizacion"), r.str("InformeAsociado"), r.str("LlaveInformeAsociado"))))
                .forEach(r -> rutas.put(r.idTipoCentralizacion(), r));
        return rutas;
    }

    private List<Configuracion> configuraciones(List<Long> ids) {
        if (ids.isEmpty()) return List.of(); // legacy binds IN (NULL): no rows
        return jdbc.query(SQL_SELECT_CONFIG, new MapSqlParameterSource("IdTipoCentralizacion", ids), SqlRow.mapper(r ->
                new Configuracion(r.lng("IdTipoCentralizacion"), r.str("Nombre"), r.str("Servicio"), r.str("Tipo"),
                        r.lng("IdTipoCentralizacionOrigen"), r.str("RequiereFechaConsulta"))));
    }

    /**
     * Types already centralized (Estado V) for the date are rejected with UI_CENTRALIZACION_CONTABILIZADA_ANULAR. With a
     * single requested type the run stops there (returns true); otherwise the type is just removed (legacy L240-263).
     */
    private boolean rechazaVigentes(Entrada entrada, List<Long> procesados, List<Configuracion> aProcesar, Salida salida) {
        if (procesados.isEmpty()) return false;
        List<Configuracion> vigentes = jdbc.query(SQL_SELECT_VIGENTES, new MapSqlParameterSource()
                        .addValue("IdTipoCentralizacion", procesados)
                        .addValue("FechaContable", entrada.fechaContable())
                        .addValue("IdEmpresa", entrada.idEmpresa()),
                SqlRow.mapper(r -> new Configuracion(r.lng("IdTipoCentralizacion"), r.str("Nombre"), null, null, null, null)));
        for (Configuracion vigente : vigentes) {
            salida.error(MensajeSalida.error(0L, "UI_CENTRALIZACION_CONTABILIZADA_ANULAR", valores("Centralizacion", vigente.nombre())));
            if (entrada.tiposONada().size() > 1) {
                aProcesar.removeIf(c -> c.idTipoCentralizacion().equals(vigente.idTipoCentralizacion()));
            } else {
                return true;
            }
        }
        return false;
    }

    /** Valuation-report checks for types that need a consultation date (legacy L266-294). */
    private void validaInformes(Entrada entrada, List<Configuracion> configuraciones, Map<Long, RutaInforme> rutas,
                                List<Configuracion> aProcesar, Map<Long, String> informes, Salida salida) {
        if (!fechasValidas(configuraciones, entrada.fechaConsulta(), entrada.fechaContable())) {
            // Legacy also tried to drop the report-based types here, but compared ids with names, so nothing is removed.
            salida.mensajes.add(MensajeSalida.advertencia(0L, "FECHA_VALORIZACION_INVALIDA", Map.of()));
            return;
        }
        if (entrada.fechaConsulta() == null || entrada.fechaConsulta().isEmpty()) return;
        for (Iterator<Configuracion> it = aProcesar.iterator(); it.hasNext(); ) {
            Configuracion conf = it.next();
            RutaInforme ruta = rutas.get(conf.idTipoCentralizacion());
            if (ruta == null) continue;
            String llave = ruta.llaveInformeAsociado() + entrada.fechaConsulta() + entrada.fechaContable();
            List<String> ids = jdbc.query(SQL_SELECT_INFORMES, new MapSqlParameterSource("key", llave),
                    SqlRow.mapper(r -> r.str("IdInforme")));
            if (ids.isEmpty()) {
                it.remove();
                Map<String, String> v = valores("Centralizacion", conf.nombre());
                v.put("Ruta", ruta.informeAsociado());
                salida.error(MensajeSalida.informacion(0L, "UI_CENTRALIZACION_INFORME_NO_EXISTE", v));
            } else {
                informes.put(conf.idTipoCentralizacion(), ids.get(0));
            }
        }
    }

    private static boolean fechasValidas(List<Configuracion> configuraciones, String fechaConsulta, String fechaContable) {
        boolean requiere = configuraciones.stream().anyMatch(c -> "S".equalsIgnoreCase(c.requiereFechaConsulta()));
        if (!requiere) return true;
        if (fechaConsulta == null) return false;
        return !LocalDate.parse(fechaConsulta).isAfter(LocalDate.parse(fechaContable));
    }

    /**
     * Stored-procedure type: {@code CALL "<Servicio>"(... p_out_Resultado => ?)}; the table output comes back as a result
     * set. Afterwards the procedure's working table of movement data is cleared (legacy L304-327).
     */
    private void ejecutarProcedimiento(Entrada entrada, Configuracion conf, Salida salida) {
        if (conf.servicio() == null || !NOMBRE_PROCEDIMIENTO.matcher(conf.servicio()).matches()) {
            throw new IllegalStateException("Invalid stored procedure name for tipo " + conf.idTipoCentralizacion() + ": " + conf.servicio());
        }
        String call = SQL_SELECT_CALL_CENT.replace(":SP_CENTRALIZACION", conf.servicio())
                .replaceAll(":p_[A-Za-z]+", "?")
                .replace("$p_out_Resultado", "?")
                .replace(";", "");
        Object[] in = {entrada.idEmpresa(), entrada.idUsuario(), entrada.idModulo(), entrada.simulaONo(),
                conf.idTipoCentralizacion(), entrada.fechaContable(), entrada.ejercicio()};
        List<Resultado> resultados = jdbc.getJdbcTemplate().execute((ConnectionCallback<List<Resultado>>) con -> {
            try (CallableStatement cs = con.prepareCall(call)) {
                for (int i = 0; i < in.length; i++) cs.setObject(i + 1, in[i]);
                boolean hayResultSet = cs.execute();
                List<Resultado> filas = new ArrayList<>();
                while (true) {
                    if (hayResultSet) {
                        try (ResultSet rs = cs.getResultSet()) {
                            var mapper = SqlRow.mapper(Resultado::of);
                            int n = 0;
                            while (rs.next()) filas.add(mapper.mapRow(rs, n++));
                        }
                        break; // the only table output is p_out_Resultado
                    }
                    if (cs.getUpdateCount() == -1) break;
                    hayResultSet = cs.getMoreResults();
                }
                return filas;
            }
        });
        salida.resultado.addAll(resultados);
        jdbc.update(SQL_DELETE_MOVIMIENTOS, new MapSqlParameterSource());
    }

    /** Java type: the centralizer bean named by the configuration (legacy Class.forName(Servicio), L330-360). */
    private void ejecutarJava(Entrada entrada, Configuracion conf, String idInforme, Salida salida) {
        BcsCentralizacionService centralizador = centralizador(conf.servicio());
        SalidaCentralizacion s = centralizador.centralizar(new EntradaCentralizacion(entrada.idEmpresa(), entrada.idUsuario(),
                entrada.idModulo(), conf.idTipoCentralizacion(), entrada.fechaContable(), idInforme, entrada.ejercicio(),
                entrada.simulaONo()));
        // Legacy assigns (does not add) the delegate's error count (L347).
        salida.erroresContador = s.erroresContador();
        s.mensajes().forEach(m -> salida.mensajes.add(MensajeSalida.of(0L, MensajeSalida.INFORMACION, m)));
        s.resultados().forEach(r -> salida.resultado.add(Resultado.of(r)));
    }

    private BcsCentralizacionService centralizador(String servicio) {
        if (servicio == null) throw new IllegalStateException("Java centralization without Servicio");
        BcsCentralizacionService s = centralizadores.get(servicio);
        if (s == null) {
            String simple = servicio.substring(servicio.lastIndexOf('.') + 1);
            s = simple.isEmpty() ? null : centralizadores.get(Character.toLowerCase(simple.charAt(0)) + simple.substring(1));
        }
        if (s == null) throw new IllegalStateException("Java centralization not available: " + servicio);
        return s;
    }

    private void agregarMensajesProcedimiento(List<Long> procesados, Map<Long, Configuracion> porId, Salida salida) {
        if (procesados.isEmpty()) return; // legacy binds IN (NULL): no rows
        List<CodigoMensaje> codigos = jdbc.query(SQL_SELECT_MENSAJES, new MapSqlParameterSource("IdTipoCentralizacion", procesados),
                SqlRow.mapper(r -> new CodigoMensaje(r.lng("IdInterno"), r.lng("IdSesion"), r.str("Codigo"),
                        r.str("CodigoDato1"), r.str("ValorDato1"), r.str("Resultado"))));
        for (CodigoMensaje c : codigos) {
            Map<String, String> v = new HashMap<>();
            Configuracion conf = porId.get(c.idSesion());
            if (conf != null) v.put("Centralizacion", conf.nombre());
            if (!"".equals(c.codigoDato1()) && c.valorDato1() != null) v.put(c.codigoDato1(), c.valorDato1());
            String tipo = switch (c.resultado() == null ? "" : c.resultado()) {
                case "RECH" -> MensajeSalida.ERROR;
                case "ADVE", "RAUT" -> MensajeSalida.AUTORIZACION;
                default -> MensajeSalida.INFORMACION;
            };
            MensajeSalida m = MensajeSalida.of(c.idInterno(), tipo, c.codigo(), v);
            if (m.esError()) salida.error(m); else salida.mensajes.add(m);
        }
    }

    private static Map<String, String> valores(String clave, String valor) {
        Map<String, String> m = new HashMap<>();
        m.put(clave, valor);
        return m;
    }
}
