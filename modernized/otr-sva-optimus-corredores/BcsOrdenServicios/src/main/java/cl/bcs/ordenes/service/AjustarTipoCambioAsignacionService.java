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
 * Spring @Service migrated from legacy cl.bcs.ordenes.AjustarTipoCambioAsignacion (JavaCall).
 *
 * Migration status: MIGRATE — no unsatisfiable external service dependencies.
 * All SQL files loaded from resources/cl/bcs/ordenes/sql/.
 *
 * TODO: Validate execute() against integration tests.
 */
@Service
public class AjustarTipoCambioAsignacionService {

    private static final Logger log = LoggerFactory.getLogger(AjustarTipoCambioAsignacionService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_UPDATE_ASIGNACION;
    private final String SQL_SELECT_COBRO_ASIGNACION;
    private final String SQL_UPDATE_COBRO_ASIGNACION;
    private final String SQL_SELECT_ORDEN;
    private final String SQL_UPDATE_ORDEN;
    private final String SQL_SELECT_COBRO_ORDEN;
    private final String SQL_UPDATE_COBRO_ORDEN;
    private final String SQL_SELECT_DATOS_CLIENTE;
    private final String SQL_SELECT_ASIGNACION;

    public AjustarTipoCambioAsignacionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_UPDATE_ASIGNACION = loadSql("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_UpdateAsignacion.sql");
        this.SQL_SELECT_COBRO_ASIGNACION = loadSql("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_SelectCobroAsignacion.sql");
        this.SQL_UPDATE_COBRO_ASIGNACION = loadSql("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_UpdateCobroAsignacion.sql");
        this.SQL_SELECT_ORDEN = loadSql("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_SelectOrdenes.sql");
        this.SQL_UPDATE_ORDEN = loadSql("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_UpdateOrden.sql");
        this.SQL_SELECT_COBRO_ORDEN = loadSql("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_SelectCobroOrden.sql");
        this.SQL_UPDATE_COBRO_ORDEN = loadSql("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_UpdateCobroOrden.sql");
        this.SQL_SELECT_DATOS_CLIENTE = loadSql("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_SelectDatosClientes.sql");
        this.SQL_SELECT_ASIGNACION = loadSql("cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_SelectAsignacion.sql");
    }

    /**
     * Executes the JavaCall equivalent of cl.bcs.ordenes.AjustarTipoCambioAsignacion.call().
     *
     * The params JsonObject contains the same fields as the legacy Entrada inner class.
     * Returns the same structure as the legacy Salida inner class.
     */
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("AjustarTipoCambioAsignacionService.execute() called");
        Gson gson = new GsonBuilder()
                .serializeSpecialFloatingPointValues()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .serializeNulls()
                .create();

        try {
            // TODO: Implement full logic migrated from legacy AjustarTipoCambioAsignacion.logica()
            // Legacy class used: cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_UpdateAsignacion.sql, cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_SelectCobroAsignacion.sql, cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_UpdateCobroAsignacion.sql, cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_SelectOrdenes.sql, cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_UpdateOrden.sql, cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_SelectCobroOrden.sql, cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_UpdateCobroOrden.sql, cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_SelectDatosClientes.sql, cl/bcs/ordenes/sql/AjustarTipoCambioAsignacion_SelectAsignacion.sql
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
            out.addProperty("_migration_status", "stub — implement from legacy AjustarTipoCambioAsignacion.logica()");
            return out;
        } catch (Exception e) {
            log.error("AjustarTipoCambioAsignacionService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String loadSql(String path) {
        try (var is = AjustarTipoCambioAsignacionService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
