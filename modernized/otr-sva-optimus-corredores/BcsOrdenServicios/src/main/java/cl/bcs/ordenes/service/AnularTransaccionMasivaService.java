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
 * Spring @Service migrated from legacy cl.bcs.ordenes.AnularTransaccionMasiva (JavaCall).
 *
 * Migration status: MIGRATE — no unsatisfiable external service dependencies.
 * All SQL files loaded from resources/cl/bcs/ordenes/sql/.
 *
 * TODO: Validate execute() against integration tests.
 */
@Service
public class AnularTransaccionMasivaService {

    private static final Logger log = LoggerFactory.getLogger(AnularTransaccionMasivaService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_TRANSACCIONES;
    private final String SQL_SELECT_ASIGNACIONES;
    private final String SQL_SELECT_CIERRE_MODULOS;
    private final String SQL_UPDATE_TRANSACCIONES;
    private final String SQL_SELECT_TRANSACCIONES_ANULADAS;
    private final String SQL_SELECT_DATOS_LOG;

    public AnularTransaccionMasivaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_TRANSACCIONES = loadSql("cl/bcs/ordenes/sql/AnularTransaccionMasiva_SelectTransacciones.sql");
        this.SQL_SELECT_ASIGNACIONES = loadSql("cl/bcs/ordenes/sql/AnularTransaccionMasiva_SelectAsignaciones.sql");
        this.SQL_SELECT_CIERRE_MODULOS = loadSql("cl/bcs/ordenes/sql/AnularTransaccionMasiva_SelectCierreModulos.sql");
        this.SQL_UPDATE_TRANSACCIONES = loadSql("cl/bcs/ordenes/sql/AnularTransaccionMasiva_UpdateTransacciones.sql");
        this.SQL_SELECT_TRANSACCIONES_ANULADAS = loadSql("cl/bcs/ordenes/sql/AnularTransaccionMasiva_SelectTransaccionesAnuladas.sql");
        this.SQL_SELECT_DATOS_LOG = loadSql("cl/bcs/ordenes/sql/AnularTransaccionMasiva_SelectDatosLog.sql");
    }

    /**
     * Executes the JavaCall equivalent of cl.bcs.ordenes.AnularTransaccionMasiva.call().
     *
     * The params JsonObject contains the same fields as the legacy Entrada inner class.
     * Returns the same structure as the legacy Salida inner class.
     */
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("AnularTransaccionMasivaService.execute() called");
        Gson gson = new GsonBuilder()
                .serializeSpecialFloatingPointValues()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .serializeNulls()
                .create();

        try {
            // TODO: Implement full logic migrated from legacy AnularTransaccionMasiva.logica()
            // Legacy class used: cl/bcs/ordenes/sql/AnularTransaccionMasiva_SelectTransacciones.sql, cl/bcs/ordenes/sql/AnularTransaccionMasiva_SelectAsignaciones.sql, cl/bcs/ordenes/sql/AnularTransaccionMasiva_SelectCierreModulos.sql, cl/bcs/ordenes/sql/AnularTransaccionMasiva_UpdateTransacciones.sql, cl/bcs/ordenes/sql/AnularTransaccionMasiva_SelectTransaccionesAnuladas.sql, cl/bcs/ordenes/sql/AnularTransaccionMasiva_SelectDatosLog.sql
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
            out.addProperty("_migration_status", "stub — implement from legacy AnularTransaccionMasiva.logica()");
            return out;
        } catch (Exception e) {
            log.error("AnularTransaccionMasivaService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String loadSql(String path) {
        try (var is = AnularTransaccionMasivaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
