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
 * Spring @Service migrated from legacy cl.bcs.ordenes.CargaArchivoTransaccionBVL (JavaCall).
 *
 * Migration status: MIGRATE — no unsatisfiable external service dependencies.
 * All SQL files loaded from resources/cl/bcs/ordenes/sql/.
 *
 * TODO: Validate execute() against integration tests.
 */
@Service
public class CargaArchivoTransaccionBVLService {

    private static final Logger log = LoggerFactory.getLogger(CargaArchivoTransaccionBVLService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_INSERT_CARGA_TRANSACCION_BVL;
    private final String SQL_SELECT_INSTRUMENTOS;
    private final String SQL_SELECT_CORREDOR_CORRESPONSAL;
    private final String SQL_SELECT_LIBRO;

    public CargaArchivoTransaccionBVLService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_INSERT_CARGA_TRANSACCION_BVL = loadSql("cl/bcs/ordenes/sql/CargaArchivoTransaccionBVL_InsertCargaArchivoTransaccionBVL.sql");
        this.SQL_SELECT_INSTRUMENTOS = loadSql("cl/bcs/ordenes/sql/CargaArchivoTransaccionBVL_SelectInstrumentos.sql");
        this.SQL_SELECT_CORREDOR_CORRESPONSAL = loadSql("cl/bcs/ordenes/sql/CargaArchivoTransaccionBVL_SelectCorredorCorresponsal.sql");
        this.SQL_SELECT_LIBRO = loadSql("cl/bcs/ordenes/sql/CargaArchivoTransaccionBVL_SelectLibroOrdenes.sql");
    }

    /**
     * Executes the JavaCall equivalent of cl.bcs.ordenes.CargaArchivoTransaccionBVL.call().
     *
     * The params JsonObject contains the same fields as the legacy Entrada inner class.
     * Returns the same structure as the legacy Salida inner class.
     */
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("CargaArchivoTransaccionBVLService.execute() called");
        Gson gson = new GsonBuilder()
                .serializeSpecialFloatingPointValues()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .serializeNulls()
                .create();

        try {
            // TODO: Implement full logic migrated from legacy CargaArchivoTransaccionBVL.logica()
            // Legacy class used: cl/bcs/ordenes/sql/CargaArchivoTransaccionBVL_InsertCargaArchivoTransaccionBVL.sql, cl/bcs/ordenes/sql/CargaArchivoTransaccionBVL_SelectInstrumentos.sql, cl/bcs/ordenes/sql/CargaArchivoTransaccionBVL_SelectCorredorCorresponsal.sql, cl/bcs/ordenes/sql/CargaArchivoTransaccionBVL_SelectLibroOrdenes.sql
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
            out.addProperty("_migration_status", "stub — implement from legacy CargaArchivoTransaccionBVL.logica()");
            return out;
        } catch (Exception e) {
            log.error("CargaArchivoTransaccionBVLService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String loadSql(String path) {
        try (var is = CargaArchivoTransaccionBVLService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
