package cl.bcs.socionegocio.service.soc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring @Service migrated from legacy:
 *   cl.bcs.socionegocio.soc.ConsultarEnrolamiento implements JavaCall
 *
 * Queries the SOC.Enrolamiento table for matching enrollment records.
 *
 * Input params (flat JsonObject):
 *   codigoPais                — String, used to conditionally filter by Tipo
 *   enrolamiento              — nested object with:
 *     IdTipoEntidad           — String
 *     IdTipoDocumentoIdentidad— String
 *     DocumentoIdentidad      — String
 *     Tipo                    — String
 *
 * Output JsonObject:
 *   enrolamientos             — array of enrollment records
 *
 * SQL: cl/bcs/socionegocio/sql/ConsultarEnrolamientoService.sql
 */
@Service
public class ConsultarEnrolamientoService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarEnrolamientoService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlQuery;

    public ConsultarEnrolamientoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlQuery = loadSql("cl/bcs/socionegocio/sql/ConsultarEnrolamientoService.sql");
    }

    /**
     * Execute the enrollment query.
     *
     * @param params flat JsonObject containing codigoPais and nested enrolamiento object
     * @return result JsonObject with an enrolamientos array
     */
    public JsonObject execute(JsonObject params) {
        log.debug("[INI] ConsultarEnrolamiento");

        String codigoPais = params.has("codigoPais")
                ? params.get("codigoPais").getAsString() : "";

        String idTipoEntidad = "";
        String idTipoDocumentoIdentidad = "";
        String documentoIdentidad = "";
        String tipo = "";

        if (params.has("enrolamiento") && params.get("enrolamiento").isJsonObject()) {
            JsonObject enr = params.getAsJsonObject("enrolamiento");
            idTipoEntidad          = getStr(enr, "IdTipoEntidad");
            idTipoDocumentoIdentidad = getStr(enr, "IdTipoDocumentoIdentidad");
            documentoIdentidad     = getStr(enr, "DocumentoIdentidad");
            tipo                   = getStr(enr, "Tipo");
        }

        MapSqlParameterSource paramMap = new MapSqlParameterSource()
                .addValue("IdTipoEntidad",             idTipoEntidad)
                .addValue("IdTipoDocumentoIdentidad",  idTipoDocumentoIdentidad)
                .addValue("DocumentoIdentidad",         documentoIdentidad)
                .addValue("Pais",                      codigoPais)
                .addValue("Tipo",                      tipo);

        Map<String, Object> out = new LinkedHashMap<>();
        List<Map<String, Object>> enrolamientos = new ArrayList<>();

        try {
            List<Map<String, Object>> rows = jdbc.queryForList(sqlQuery, paramMap);
            enrolamientos = rows;
        } catch (Exception e) {
            log.error("ERROR ConsultarEnrolamiento", e);
        }

        out.put("enrolamientos", enrolamientos);

        log.debug("[FIN] ConsultarEnrolamiento");
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .serializeSpecialFloatingPointValues()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .create();
        return JsonParser.parseString(gson.toJson(out)).getAsJsonObject();
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private static String getStr(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull()
                ? obj.get(key).getAsString() : "";
    }

    private static String loadSql(String path) {
        try (var is = ConsultarEnrolamientoService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + path);
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
