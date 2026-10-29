package cl.bcs.tesoreria.javacall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;

/**
 * Legacy {@code cl.bcs.tesoreria.ActivarMandatosArchivoCargo} (TES-B-199..203): marks INA the active PAC mandates whose CSC / SAAPV contract has a
 * discount day other than the given one. The typed entry is {@link #logica(Long)}; the JavaCall entry {@link #execute(JsonObject)} is the legacy
 * {@code call()}, a stub.
 */
@Service
public class ActivarMandatosArchivoCargaService {

    private static final Logger log = LoggerFactory.getLogger(ActivarMandatosArchivoCargaService.class);

    private static final String SQL_SELECT_MANDATOS = SqlNombrado.recurso("ActivarMandatosArchivoCargo_SelectMandatos");
    private static final String SQL_UPDATE_MANDATOS = SqlNombrado.recurso("ActivarMandatosArchivoCargo_UpdateMandatos");

    public static class MandatosPAC {
        public Long IdMandatoPAC;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Object> P_OUT_INGRESOEGRESOGENERADO = new ArrayList<>();
        public List<Object> P_OUT_RESULTADOPROCESO = new ArrayList<>();
    }

    private final SqlNombrado sql;

    public ActivarMandatosArchivoCargaService(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    /** LEGACY DEFECT kept (see README TES-B-203): the JavaCall entry point does nothing and answers null; only {@link #logica(Long)} works. */
    public JsonObject execute(JsonObject entrada) {
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public Salida logica(Long diasDescuento) {
        Salida out = new Salida();
        try {
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("DiaDescuento", diasDescuento);
            List<MandatosPAC> mandatos = sql.consultar(SQL_SELECT_MANDATOS, parametros, MandatosPAC.class);
            if (!mandatos.isEmpty()) {
                sql.lote(SQL_UPDATE_MANDATOS, mandatos);
            }
            out.P_OUT_ERRORESCONTADOR = 0L;
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "Resultado", mandatos.size() + " Mandatos actualizados Correctamente."));
        } catch (Exception e) {
            log.error("ActivarMandatosArchivoCargo error", e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "Resultado", "Error al actualizar Mandatos."));
        }
        return out;
    }
}
