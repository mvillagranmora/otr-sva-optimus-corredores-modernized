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
 * Legacy {@code cl.bcs.tesoreria.ConfirmarAutorizarAnularMovimientosTesoreria}, behaviours TES-A-350..357 (TES-A-355 dropped from the proof): the
 * authorizer approved the annulment of movimientos, so they are annulled (with their formas de pago), one message per annulled movimiento is
 * reported and an audit log is written through the AgregarLog collaborator.
 *
 * <p>Unlike its sibling "rechazar", exceptions are NOT swallowed here: they roll the transaction back.
 */
@Service
public class ConfirmarAutorizarAnularMovimientosTesoreriaService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarAutorizarAnularMovimientosTesoreriaService.class);

    private static final String DDL_MOVIMIENTOS = "CREATE LOCAL TEMPORARY TABLE #AutorizarAnularMovimientosTesoreria (\"IdIngresoEgreso\" BIGINT)";
    private static final String SQL_INSERT_MOVIMIENTOS = "INSERT INTO #AutorizarAnularMovimientosTesoreria (\"IdIngresoEgreso\") VALUES (:IdIngresoEgreso)";
    private static final String SQL_DROP_MOVIMIENTOS = "DROP TABLE #AutorizarAnularMovimientosTesoreria";

    private static final String SQL_SELECT_AUTORIZADOS = cargar("sql/ConfirmarAutorizarAnularMovimientos_SelectMovimientosAutorizados.sql");
    private static final String SQL_ANULAR_MOVIMIENTOS = cargar("sql/ConfirmarAutorizarAnularMovimientos_UpdateAnularMovimientosIngresoEgreso.sql");
    private static final String SQL_ANULAR_FORMAS_DE_PAGO = cargar("sql/ConfirmarAutorizarAnularMovimientos_UpdateAnularFormaDePagoMovimientos.sql");

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class EntradaConfirmarAutorizarAnularMovimientosTesoreria extends BcsPojo {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<MovimientosPorAutorizar> p_Movimientos = new ArrayList<>();
    }

    public static class MovimientosPorAutorizar extends BcsPojo {
        public Long IdIngresoEgreso;

        public MovimientosPorAutorizar(Long idIngresoEgreso) {
            this.IdIngresoEgreso = idIngresoEgreso;
        }
    }

    public static class MovimientosAutorizados extends BcsPojo {
        public Long IdIngresoEgreso;
    }

    public static class SalidaConfirmarAutorizarAnularMovimientosTesoreria extends BcsPojo {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public ConfirmarAutorizarAnularMovimientosTesoreriaService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion,
            DiccionarioMensajes diccionario, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    /** Legacy {@code call}: as {@link #logica} with the error counter recounted from the messages. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        EntradaConfirmarAutorizarAnularMovimientosTesoreria in = JsonLegacy.PLATAFORMA.fromJson(entrada.toString(),
                EntradaConfirmarAutorizarAnularMovimientosTesoreria.class);
        SalidaConfirmarAutorizarAnularMovimientosTesoreria out = logica(in);
        out.P_OUT_ERRORESCONTADOR = MensajeSalida.numeroErrores(out.P_OUT_MENSAJES);
        return JsonLegacy.PLATAFORMA.toJsonTree(out).getAsJsonObject();
    }

    /** Legacy typed {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaConfirmarAutorizarAnularMovimientosTesoreria logica(EntradaConfirmarAutorizarAnularMovimientosTesoreria in) throws Exception {
        log.info("INICIO::ConfirmarAutorizarAnularMovimientosTesoreria");
        SalidaConfirmarAutorizarAnularMovimientosTesoreria out = new SalidaConfirmarAutorizarAnularMovimientosTesoreria();

        jdbc.getJdbcOperations().execute(DDL_MOVIMIENTOS);
        if (in.p_Movimientos == null) {
            throw new SQLException("Null parameters. If parameters aren't need, pass an empty");
        }
        Consultas.lote(jdbc, SQL_INSERT_MOVIMIENTOS, in.p_Movimientos.stream().map(m -> {
            Map<String, Object> fila = new HashMap<>();
            fila.put("IdIngresoEgreso", m.IdIngresoEgreso);
            return fila;
        }).toList());

        // annuls the movimientos and their formas de pago, then keeps those that are now annulled
        jdbc.update(SQL_ANULAR_MOVIMIENTOS, new MapSqlParameterSource("p_IdUsuario", in.p_IdUsuario));
        jdbc.update(SQL_ANULAR_FORMAS_DE_PAGO, new MapSqlParameterSource());
        List<MovimientosAutorizados> autorizados = jdbc.query(SQL_SELECT_AUTORIZADOS, Map.of(), Filas.de(MovimientosAutorizados.class));

        Map<Long, Object> autorizadosPorId = Utils.List2Hash(autorizados, "IdIngresoEgreso");
        for (MovimientosPorAutorizar mov : in.p_Movimientos) {
            if (!autorizadosPorId.containsKey(mov.IdIngresoEgreso)) {
                out.P_OUT_MENSAJES.add(new MensajeSalida((long) out.P_OUT_MENSAJES.size() + 1, MensajeSalida.MENSAJE_ERROR, "UI_ESTADO_INCORRECTO",
                        diccionario.mensaje(in.p_IdEmpresa, "UI_ESTADO_INCORRECTO", new HashMap<>())));
            }
        }
        if (!out.P_OUT_MENSAJES.isEmpty()) {
            soltarTablaTemporal();
            return out; // a movimiento in a wrong state ends the flow (the annulment already done stays)
        }

        // result message and audit log of each annulled movimiento
        AtomicLong contador = new AtomicLong();
        AgregarLog.InAgregarLog agregarLog = new AgregarLog.InAgregarLog();
        String fechaSistema = FechaSistema.fechaSistema(configuracion, in.p_IdEmpresa);
        for (MovimientosAutorizados mov : autorizados) {
            Map<String, String> valores = new HashMap<>();
            valores.put("IdMovimientoComprobante", String.valueOf(mov.IdIngresoEgreso));
            String mensaje = diccionario.mensaje(in.p_IdEmpresa, "UI_MOVIMIENTO_COMPROBANTE_ANULADO", valores);
            out.P_OUT_MENSAJES.add(new MensajeSalida(mov.IdIngresoEgreso, MensajeSalida.MENSAJE_OK, "UI_MOVIMIENTO_COMPROBANTE_ANULADO", mensaje));
            agregarLog.agregarLogBasico(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, mov.IdIngresoEgreso, contador.get(), "ANULAR", "INGRESO/EGRESO",
                    fechaSistema, mensaje);
            agregarLog.agregarDetalleLogBasico(mov.IdIngresoEgreso, contador.getAndIncrement(), "IdEstado", "INGRESADO", "ANULADO");
        }
        Colaborador.invocar(colaboradores, "AgregarLog", agregarLog, AgregarLog.OutAgregarLog.class);

        soltarTablaTemporal();
        log.info("TERMINO::ConfirmarAutorizarAnularMovimientosTesoreria");
        return out;
    }

    /**
     * The legacy only emptied the table (it stayed for the rest of the connection); it is dropped on the paths that end normally so a pooled
     * connection can run the service again. Not done on failure: the DDL would commit the work that the rollback must undo.
     */
    private void soltarTablaTemporal() {
        try {
            jdbc.getJdbcOperations().execute(SQL_DROP_MOVIMIENTOS);
        } catch (RuntimeException e) {
            log.debug("temporary table was not dropped", e);
        }
    }
}
