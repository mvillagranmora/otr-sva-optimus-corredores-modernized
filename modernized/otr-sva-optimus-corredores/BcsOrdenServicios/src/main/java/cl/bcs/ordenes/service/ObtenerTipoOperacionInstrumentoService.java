package cl.bcs.ordenes.service;

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

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Spring @Service migrated from legacy cl.bcs.ordenes.ObtenerTipoOperacionInstrumento (JavaCall).
 *
 * Migration status: MIGRATE — no unsatisfiable external service dependencies.
 * All SQL files loaded from resources/cl/bcs/ordenes/sql/.
 *
 * TODO: Validate execute() against integration tests.
 */
@Service
public class ObtenerTipoOperacionInstrumentoService {

    private static final Logger log = LoggerFactory.getLogger(ObtenerTipoOperacionInstrumentoService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_DETALLE_TIPOOPERACIONINSTRUMENTO;

    public ObtenerTipoOperacionInstrumentoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_DETALLE_TIPOOPERACIONINSTRUMENTO = loadSql("cl/bcs/ordenes/sql/ObtenerTipoOperacionInstrumento_SelectTipoOperacionInstrumento.sql");
    }

    /**
     * Executes the JavaCall equivalent of cl.bcs.ordenes.ObtenerTipoOperacionInstrumento.call().
     *
     * The params JsonObject contains the same fields as the legacy Entrada inner class.
     * Returns the same structure as the legacy Salida inner class.
     */
    public JsonObject execute(JsonObject params) {
        log.debug("ObtenerTipoOperacionInstrumentoService.execute() called");
        Gson gson = new GsonBuilder()
                .serializeSpecialFloatingPointValues()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .serializeNulls()
                .create();

        try {
            // TODO: Implement full logic migrated from legacy ObtenerTipoOperacionInstrumento.logica()
            // Legacy class used: cl/bcs/ordenes/sql/ObtenerTipoOperacionInstrumento_SelectTipoOperacionInstrumento.sql
            //
            // Pattern:
            //   MapSqlParameterSource sqlParams = new MapSqlParameterSource();
            //   sqlParams.addValues(gson.fromJson(params, Map.class));
            //   List<Map<String,Object>> result = jdbc.queryForList(SQL_XXX, sqlParams);
            //   ... build output JsonObject ...
            //
            // For now returns params echo + status to allow integration smoke-test.
            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
            out.addProperty("_migration_status", "stub — implement from legacy ObtenerTipoOperacionInstrumento.logica()");
            return out;
        } catch (Exception e) {
            log.error("ObtenerTipoOperacionInstrumentoService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String loadSql(String path) {
        try (var is = ObtenerTipoOperacionInstrumentoService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
