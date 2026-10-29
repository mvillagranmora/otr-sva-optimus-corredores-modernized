package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.SqlSinComentarios.cargar;

import java.util.ArrayList;
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

import cl.bcs.tesoreria.javacall.AnularRegistrosService.AnularRegistros_Entrada;
import cl.bcs.tesoreria.javacall.AnularRegistrosService.AnularRegistros_Salida;
import cl.bcs.tesoreria.javacall.AnularRegistrosService.Registro_Entrada;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;

/**
 * Legacy {@code cl.bcs.tesoreria.EjecutarAnulacionComprobantes}, behaviours TES-A-450..453: day-end job that annuls, through
 * {@link AnularRegistrosService}, the comprobantes in state ING whose liquidation date is the given date.
 */
@Service
public class EjecutarAnulacionComprobantesService {

    private static final Logger log = LoggerFactory.getLogger(EjecutarAnulacionComprobantesService.class);

    /** The legacy {@code call} used a plain Gson (nulls omitted, default date format). */
    private static final Gson GSON = new Gson();

    private static final String SQL_SELECT_COMPROBANTES = cargar("sql/EjecutarAnulacionComprobantes_SelectComprobantes.sql");

    // ------------------------------------------------------------------ JavaCall contract (legacy names)

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_FechaMovimiento;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR;
        public Long P_OUT_ADVERTENCIASCONTADOR;
        public String P_OUT_DESCRIPCION;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class comprobante_registro {
        public Long IdComprobante;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final AnularRegistrosService anularRegistros;

    public EjecutarAnulacionComprobantesService(NamedParameterJdbcTemplate jdbc, AnularRegistrosService anularRegistros) {
        this.jdbc = jdbc;
        this.anularRegistros = anularRegistros;
    }

    /** Legacy {@code call}. */
    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject entrada) throws Exception {
        return GSON.toJsonTree(logica(GSON.fromJson(entrada.toString(), Entrada.class))).getAsJsonObject();
    }

    /** Legacy typed {@code logica}. */
    @Transactional(rollbackFor = Exception.class)
    public Salida logica(Entrada entrada) throws Exception {
        log.info("INICIO::EjecutarAnulacionComprobantes");
        Salida out = new Salida();
        out.P_OUT_ERRORESCONTADOR = 0L;

        Map<String, Object> params = new HashMap<>();
        params.put("p_IdEmpresa", entrada.p_IdEmpresa);
        params.put("p_Fecha", entrada.p_FechaMovimiento);
        List<comprobante_registro> comprobantes = jdbc.query(SQL_SELECT_COMPROBANTES, params, Filas.de(comprobante_registro.class));
        if (comprobantes.isEmpty()) {
            out.P_OUT_DESCRIPCION = "No hay registros para anular. ";
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, MensajeSalida.MENSAJE_INFORMACION, out.P_OUT_DESCRIPCION));
            return out;
        }

        AnularRegistros_Entrada anular = new AnularRegistros_Entrada();
        anular.p_IdEmpresa = entrada.p_IdEmpresa;
        anular.p_IdUsuario = entrada.p_IdUsuario;
        anular.p_IdModulo = "TES";
        anular.p_UsaReglasNegocio = "S";
        anular.p_FechaMovimiento = entrada.p_FechaMovimiento;
        for (comprobante_registro comprobante : comprobantes) {
            Registro_Entrada registro = new Registro_Entrada();
            registro.Id = comprobante.IdComprobante;
            registro.Entidad = AnularRegistrosService.ENTIDAD_COMPROBANTE;
            anular.p_RegistrosAProcesar.add(registro);
        }

        AnularRegistros_Salida salida = anularRegistros.logica(anular);
        out.P_OUT_DESCRIPCION = salida.P_OUT_MENSAJES.size() + " comprobantes anulados. ";
        out.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;
        out.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR;
        log.info("FIN::EjecutarAnulacionComprobantes");
        return out;
    }
}
