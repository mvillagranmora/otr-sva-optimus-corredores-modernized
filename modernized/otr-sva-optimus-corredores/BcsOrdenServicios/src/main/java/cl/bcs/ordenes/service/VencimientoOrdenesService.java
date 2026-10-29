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
 * Spring @Service migrated from legacy cl.bcs.ordenes.VencimientoOrdenes (JavaCall).
 *
 * Migration status: MIGRATE — no unsatisfiable external service dependencies.
 * All SQL files loaded from resources/cl/bcs/ordenes/sql/.
 *
 * TODO: Validate execute() against integration tests.
 */
@Service
public class VencimientoOrdenesService {

    private static final Logger log = LoggerFactory.getLogger(VencimientoOrdenesService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_ORDENES;
    private final String SQL_UPDATE_ORDENES;
    private final String SQL_INSERT_CONTROL_PROCESO;

    public VencimientoOrdenesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_ORDENES = loadSql("cl/bcs/ordenes/sql/VencimientoOrdenes_Select.sql");
        this.SQL_UPDATE_ORDENES = loadSql("cl/bcs/ordenes/sql/VencimientoOrdenes_Update.sql");
        this.SQL_INSERT_CONTROL_PROCESO = loadSql("cl/bcs/ordenes/sql/VencimientoOrdenes_InsertControlProceso.sql");
    }

    /**
     * Executes the JavaCall equivalent of cl.bcs.ordenes.VencimientoOrdenes.call().
     *
     * The params JsonObject contains the same fields as the legacy Entrada inner class.
     * Returns the same structure as the legacy Salida inner class.
     */
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("VencimientoOrdenesService.execute() called");
        Gson gson = new GsonBuilder()
                .serializeSpecialFloatingPointValues()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .serializeNulls()
                .create();

        try {
            // TODO: Implement full logic migrated from legacy VencimientoOrdenes.logica()
            // Legacy class used: cl/bcs/ordenes/sql/VencimientoOrdenes_Select.sql, cl/bcs/ordenes/sql/VencimientoOrdenes_Update.sql, cl/bcs/ordenes/sql/VencimientoOrdenes_InsertControlProceso.sql
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
            out.addProperty("_migration_status", "stub — implement from legacy VencimientoOrdenes.logica()");
            return out;
        } catch (Exception e) {
            log.error("VencimientoOrdenesService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String loadSql(String path) {
        try (var is = VencimientoOrdenesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
