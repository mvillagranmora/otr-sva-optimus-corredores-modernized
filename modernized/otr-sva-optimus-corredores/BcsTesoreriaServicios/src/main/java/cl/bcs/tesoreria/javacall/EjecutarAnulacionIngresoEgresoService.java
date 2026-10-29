package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.SqlSinComentarios.cargar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/**
 * Legacy {@code cl.bcs.tesoreria.EjecutarAnulacionIngresoEgreso}, behaviours TES-A-440..444 (TES-A-443 dropped from the proof): day-end job that annuls the movimientos still in
 * state ING whose movement date is on or before the given date, together with their formas de pago.
 */
@Service
public class EjecutarAnulacionIngresoEgresoService {

    private static final Logger log = LoggerFactory.getLogger(EjecutarAnulacionIngresoEgresoService.class);

    /** The legacy {@code call} used a plain Gson (nulls omitted, default date format). */
    private static final Gson GSON = new Gson();

    private static final String SQL_SELECT_MOVIMIENTOS = cargar("sql/EjecutarAnulacionIngresoEgreso_SelectMovimientos.sql");
    private static final String SQL_UPDATE_MOVIMIENTOS = cargar("sql/EjecutarAnulacionIngresoEgreso_UpdateMovimientos.sql");
    private static final String SQL_UPDATE_FORMAS_DE_PAGO = cargar("sql/EjecutarAnulacionIngresoEgreso_UpdateMovimientosFormaDePago.sql");

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_Fecha;
        public String p_SimularOperacion;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR;
        public Long P_OUT_ADVERTENCIASCONTADOR;
        public String P_OUT_DESCRIPCION;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class movimientoTesoreria_Proceso {
        public Long IdIngresoEgreso;
        public Date FechaMovimiento;
        public Long IdUsuarioAnulacion;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final DiccionarioMensajes diccionario;

    public EjecutarAnulacionIngresoEgresoService(NamedParameterJdbcTemplate jdbc, DiccionarioMensajes diccionario) {
        this.jdbc = jdbc;
        this.diccionario = diccionario;
    }

    /** Legacy {@code call}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        return GSON.toJsonTree(logica(GSON.fromJson(entrada.toString(), Entrada.class))).getAsJsonObject();
    }

    /** Legacy typed {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public Salida logica(Entrada entrada) throws Exception {
        log.info("INICIO::EjecutarAnulacionIngresoEgreso");
        Salida out = new Salida();
        out.P_OUT_ERRORESCONTADOR = 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("p_IdEmpresa", entrada.p_IdEmpresa);
        params.put("p_Fecha", entrada.p_Fecha);
        List<movimientoTesoreria_Proceso> movimientos = jdbc.query(SQL_SELECT_MOVIMIENTOS, params, Filas.de(movimientoTesoreria_Proceso.class));
        if (movimientos.isEmpty()) {
            out.P_OUT_DESCRIPCION = "NO HAY REGISTROS PARA ANULAR";
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, MensajeSalida.MENSAJE_INFORMACION, out.P_OUT_DESCRIPCION));
            return out;
        }

        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
        List<Map<String, Object>> anular = new ArrayList<>();
        for (movimientoTesoreria_Proceso movimiento : movimientos) {
            formatoFecha.parse(entrada.p_Fecha); // a date that does not parse fails here, as the legacy did (TES-A-443)
            Map<String, Object> fila = new HashMap<>();
            fila.put("IdIngresoEgreso", movimiento.IdIngresoEgreso);
            fila.put("IdUsuarioAnulacion", entrada.p_IdUsuario);
            anular.add(fila);
        }
        Consultas.lote(jdbc, SQL_UPDATE_MOVIMIENTOS, anular);
        Consultas.lote(jdbc, SQL_UPDATE_FORMAS_DE_PAGO, anular);

        Map<String, String> valores = new HashMap<>();
        valores.put("Cantidad", String.valueOf(anular.size()));
        out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "MOVIMIENTOS_ANULADOS",
                diccionario.mensaje(entrada.p_IdEmpresa, "MOVIMIENTOS_ANULADOS", valores)));
        log.info("FIN::EjecutarAnulacionIngresoEgreso");
        return out;
    }
}
