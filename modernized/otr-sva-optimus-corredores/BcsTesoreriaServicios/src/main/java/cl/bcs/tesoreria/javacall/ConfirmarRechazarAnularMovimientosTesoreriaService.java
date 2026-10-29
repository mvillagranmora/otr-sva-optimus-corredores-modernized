package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.SqlSinComentarios.cargar;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Colaborador;
import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.FechaSistema;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.javacall.contrato.AgregarLog;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;

/**
 * Legacy {@code cl.bcs.tesoreria.ConfirmarRechazarAnularMovimientosTesoreria}, behaviours TES-A-340..348 (TES-A-345 dropped from the proof): the
 * authorizer rejected the annulment of some movimientos, so the authorization event is marked rejected and the movimiento is released
 * (its event link is cleared). Reports one message per rejected movimiento and writes an audit log through the AgregarLog collaborator.
 *
 * <p>Like the legacy, {@link #logica} swallows every exception and reports it only as {@code P_OUT_ERRORESCONTADOR = 1}, so the work done before the
 * failure stays (the transaction commits).
 */
@Service
public class ConfirmarRechazarAnularMovimientosTesoreriaService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarRechazarAnularMovimientosTesoreriaService.class);

    private static final String DDL_MOVIMIENTOS = "CREATE LOCAL TEMPORARY TABLE #RechazarAnularMovimientosTesoreria (\"IdIngresoEgreso\" BIGINT)";
    private static final String SQL_INSERT_MOVIMIENTOS = "INSERT INTO #RechazarAnularMovimientosTesoreria (\"IdIngresoEgreso\") VALUES (:IdIngresoEgreso)";
    private static final String SQL_DROP_MOVIMIENTOS = "DROP TABLE #RechazarAnularMovimientosTesoreria";

    private static final String SQL_RECHAZAR_AUTORIZACION = cargar("sql/ConfirmarRechazarAnularMovimientos_UpdateRechazarAutorizacion.sql");
    private static final String SQL_SELECT_RECHAZADOS = cargar("sql/ConfirmarRechazarAnularMovimientos_SelectMovimientosRechazados.sql");
    private static final String SQL_RECHAZAR_MOVIMIENTOS = cargar("sql/ConfirmarRechazarAnularMovimientos_UpdateRechazarMovimientosIngresoEgreso.sql");

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class EntradaConfirmarRechazarAnularMovimientosTesoreria extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<MovimientosPorRechazar> p_Movimientos = new ArrayList<>();
    }

    public static class MovimientosPorRechazar extends BcsPojo {
        public Long IdIngresoEgreso;

        public MovimientosPorRechazar(Long idIngresoEgreso) {
            this.IdIngresoEgreso = idIngresoEgreso;
        }
    }

    public static class MovimientosRechazados extends BcsPojo {
        public Long IdIngresoEgreso;
    }

    public static class SalidaConfirmarRechazarAnularMovimientosTesoreria extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public ConfirmarRechazarAnularMovimientosTesoreriaService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion,
            DiccionarioMensajes diccionario, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    /**
     * Legacy {@code call}. LEGACY DEFECT kept (see README, ConfirmarRechazarAnularMovimientosTesoreria): the counter is recomputed from the
     * messages, so the 1 that {@link #logica} sets for a swallowed exception is lost (0 when there is no ERROR message).
     */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaConfirmarRechazarAnularMovimientosTesoreria in = JsonLegacy.PLATAFORMA.fromJson(entrada.toString(),
                EntradaConfirmarRechazarAnularMovimientosTesoreria.class);
        SalidaConfirmarRechazarAnularMovimientosTesoreria out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return JsonLegacy.PLATAFORMA.toJsonTree(out).getAsJsonObject();
    }

    /** Legacy typed {@code logica}: never throws; a failure is reported as counter 1 (TES-A-346, TES-A-348). */
    @Transactional(rollbackFor = Exception.class)
    public SalidaConfirmarRechazarAnularMovimientosTesoreria logica(EntradaConfirmarRechazarAnularMovimientosTesoreria in) {
        SalidaConfirmarRechazarAnularMovimientosTesoreria out = new SalidaConfirmarRechazarAnularMovimientosTesoreria();
        try {
            log.info("INICIO::ConfirmarRechazarAnularMovimientosTesoreria");
            rechazar(in, out);
        } catch (Exception e) {
            // LEGACY DEFECT kept (see README, ConfirmarRechazarAnularMovimientosTesoreria): every exception is swallowed
            log.warn("ConfirmarRechazarAnularMovimientosTesoreria failed", e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        } finally {
            soltarTablaTemporal();
        }
        log.info("TERMINO::ConfirmarRechazarAnularMovimientosTesoreria");
        return out;
    }

    private void rechazar(EntradaConfirmarRechazarAnularMovimientosTesoreria in, SalidaConfirmarRechazarAnularMovimientosTesoreria out) throws Exception {
        jdbc.getJdbcOperations().execute(DDL_MOVIMIENTOS);
        if (in.p_Movimientos == null) {
            throw new SQLException("Null parameters. If parameters aren't need, pass an empty");
        }
        Consultas.lote(jdbc, SQL_INSERT_MOVIMIENTOS, in.p_Movimientos.stream().map(m -> {
            Map<String, Object> fila = new HashMap<>();
            fila.put("IdIngresoEgreso", m.IdIngresoEgreso);
            return fila;
        }).toList());

        // rejects the authorization event, then keeps the movimientos whose event is now rejected
        MapSqlParameterSource usuario = new MapSqlParameterSource("p_IdUsuario", in.p_IdUsuario);
        jdbc.update(SQL_RECHAZAR_AUTORIZACION, usuario);
        List<MovimientosRechazados> rechazados = jdbc.query(SQL_SELECT_RECHAZADOS, Map.of(), Filas.de(MovimientosRechazados.class));

        Map<Long, Object> rechazadosPorId = Utils.List2Hash(rechazados, "IdIngresoEgreso");
        for (MovimientosPorRechazar mov : in.p_Movimientos) {
            if (!rechazadosPorId.containsKey(mov.IdIngresoEgreso)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida((long) out.P_OUT_MENSAJES.size() + 1, MensajeSalida.MENSAJE_ERROR, "UI_ESTADO_INCORRECTO",
                        diccionario.mensaje(in.p_IdEmpresa, "UI_ESTADO_INCORRECTO", new HashMap<>())));
            }
        }
        if (!out.P_OUT_MENSAJES.isEmpty()) {
            return; // a movimiento in a wrong state ends the flow
        }

        jdbc.update(SQL_RECHAZAR_MOVIMIENTOS, usuario);

        // result message and audit log of each movimiento
        AtomicLong contador = new AtomicLong();
        AgregarLog.InAgregarLog agregarLog = new AgregarLog.InAgregarLog();
        String fechaSistema = FechaSistema.fechaSistema(configuracion, in.p_IdEmpresa);
        for (MovimientosRechazados mov : rechazados) {
            Map<String, String> valores = new HashMap<>();
            valores.put("IdMovimientoComprobante", String.valueOf(mov.IdIngresoEgreso));
            String mensaje = diccionario.mensaje(in.p_IdEmpresa, "UI_MOVIMIENTO_COMPROBANTE_NO_ANULADO", valores);
            out.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdIngresoEgreso, MensajeSalida.MENSAJE_OK, "AUTORIZACION_RECHAZADA", mensaje));
            agregarLog.agregarLogBasico(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, mov.IdIngresoEgreso, contador.get(), "ANULAR", "INGRESO/EGRESO", fechaSistema,
                    mensaje);
            agregarLog.agregarDetalleLogBasico(mov.IdIngresoEgreso, contador.getAndIncrement(), "IdEstado", "INGRESADO", "INGRESADO");
        }
        Colaborador.invocar(colaboradores, "AgregarLog", agregarLog, AgregarLog.OutAgregarLog.class);
    }

    /** The legacy only emptied the table (it stayed for the rest of the connection); it is dropped so a pooled connection can run the service again. */
    private void soltarTablaTemporal() {
        try {
            jdbc.getJdbcOperations().execute(SQL_DROP_MOVIMIENTOS);
        } catch (RuntimeException e) {
            log.debug("temporary table was not dropped", e);
        }
    }
}
