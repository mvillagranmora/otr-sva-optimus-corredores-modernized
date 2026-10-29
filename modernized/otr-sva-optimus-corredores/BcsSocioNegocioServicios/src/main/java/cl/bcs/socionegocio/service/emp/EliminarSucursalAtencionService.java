package cl.bcs.socionegocio.service.emp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Spring @Service migrated from legacy:
 *   cl.bcs.socionegocio.emp.EliminarSucursalAtencion implements JavaCall
 *
 * Performs a soft-delete (Borrado='S') on the SucursalAtencion table for a
 * given (IdSucursalAtencion, IdEmpleado) combination.
 *
 * Input params (flat JsonObject):
 *   p_IdSucursalAtencion  Long   — mandatory
 *   p_IdEmpleado          Long   — mandatory
 *
 * Output JsonObject:
 *   P_OUT_ERRORESCONTADOR  Long   — 0 = success, 1 = error
 *   P_OUT_DESCRIPCION      String — error message when P_OUT_ERRORESCONTADOR > 0
 */
@Service
public class EliminarSucursalAtencionService {

    private static final Logger log = LoggerFactory.getLogger(EliminarSucursalAtencionService.class);

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Inline SQL — no file needed; copied verbatim from legacy source.
     * The legacy class used SQL.exec(conn, "SET SCHEMA " + Utils.getSchema()) before the update;
     * the schema is set at datasource level in application.yml.
     */
    private static final String SQL_DELETE =
            "UPDATE \"BSCL.Plataforma.BD::Empleado.SucursalAtencion\" " +
            "SET \"Borrado\" = 'S' " +
            "WHERE \"IdSucursalAtencion\" = :IdSucursalAtencion " +
            "  AND \"IdEmpleado\" = :IdEmpleado";

    public EliminarSucursalAtencionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Execute the soft-delete.
     *
     * @param params flat JsonObject with p_IdSucursalAtencion and p_IdEmpleado
     * @return result JsonObject
     */
    @Transactional
    public JsonObject execute(JsonObject params) {
        long pIdSucursalAtencion = params.has("p_IdSucursalAtencion")
                ? params.get("p_IdSucursalAtencion").getAsLong() : 0L;
        long pIdEmpleado = params.has("p_IdEmpleado")
                ? params.get("p_IdEmpleado").getAsLong() : 0L;

        log.debug("[INI] EliminarSucursalAtencion IdSucursalAtencion={} IdEmpleado={}",
                pIdSucursalAtencion, pIdEmpleado);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("P_OUT_ERRORESCONTADOR", 0L);
        out.put("P_OUT_DESCRIPCION", "");

        try {
            MapSqlParameterSource paramMap = new MapSqlParameterSource()
                    .addValue("IdSucursalAtencion", pIdSucursalAtencion)
                    .addValue("IdEmpleado", pIdEmpleado);
            jdbc.update(SQL_DELETE, paramMap);
        } catch (Exception e) {
            log.error("ERROR EliminarSucursalAtencion", e);
            out.put("P_OUT_ERRORESCONTADOR", 1L);
            out.put("P_OUT_DESCRIPCION", e.getMessage());
        }

        log.debug("[FIN] EliminarSucursalAtencion");
        Gson gson = new GsonBuilder().serializeNulls().create();
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }
}
