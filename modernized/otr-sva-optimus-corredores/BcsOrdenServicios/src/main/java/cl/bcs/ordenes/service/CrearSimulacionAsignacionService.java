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
 * Spring @Service migrated from legacy cl.bcs.ordenes.CrearSimulacionAsignacion (JavaCall).
 *
 * Migration status: MIGRATE — no unsatisfiable external service dependencies.
 * All SQL files loaded from resources/cl/bcs/ordenes/sql/.
 *
 * TODO: Validate execute() against integration tests.
 */
@Service
public class CrearSimulacionAsignacionService {

    private static final Logger log = LoggerFactory.getLogger(CrearSimulacionAsignacionService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_INSERT_TRANSACCIONES;
    private final String SQL_INSERT_CARTERAS;
    private final String SQL_INSERT_ORDENES;
    private final String SQL_INSERT_PORTAFOLIOS;
    private final String SQL_CALL_SIMULACION_ASIGNACION;
    private final String SQL_SELECT_SALIDA;
    private final String SQL_SELECT_ORDENES;
    private final String SQL_SELECT_INDICADORES_PORTAFOLIO;
    private final String SQL_SELECT_PREFERENCIA_PAGO_DEFECTO;
    private final String SQL_SELECT_CUENTA_CUSTODIO;
    private final String SQL_SELECT_INFORMACION_ORDENES;
    private final String SQL_SELECT_INFORMACION_TRANSACCIONES;
    private final String SQL_SELECT_INFORMACION_CARTERAS;
    private final String SQL_SELECT_INSTRUMENTO_BY_NEMOTECNICO;

    public CrearSimulacionAsignacionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_INSERT_TRANSACCIONES = loadSql("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_InsertTransacciones.sql");
        this.SQL_INSERT_CARTERAS = loadSql("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_InsertCarteras.sql");
        this.SQL_INSERT_ORDENES = loadSql("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_InsertOrdenes.sql");
        this.SQL_INSERT_PORTAFOLIOS = loadSql("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_InsertPortafolios.sql");
        this.SQL_CALL_SIMULACION_ASIGNACION = loadSql("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_CallCrearSimulacionAsignacion.sql");
        this.SQL_SELECT_SALIDA = loadSql("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectSalida.sql");
        this.SQL_SELECT_ORDENES = loadSql("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectOrdenes.sql");
        this.SQL_SELECT_INDICADORES_PORTAFOLIO = loadSql("cl/bcs/ordenes/sql/Globales_SelectIndicadoresPortafolio.sql");
        this.SQL_SELECT_PREFERENCIA_PAGO_DEFECTO = loadSql("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectPreferenciaPagoDefecto.sql");
        this.SQL_SELECT_CUENTA_CUSTODIO = loadSql("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectCuentaCustodio.sql");
        this.SQL_SELECT_INFORMACION_ORDENES = loadSql("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectInformacionOrdenes.sql");
        this.SQL_SELECT_INFORMACION_TRANSACCIONES = loadSql("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectInformacionTransacciones.sql");
        this.SQL_SELECT_INFORMACION_CARTERAS = loadSql("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectInformacionCarteras.sql");
        this.SQL_SELECT_INSTRUMENTO_BY_NEMOTECNICO = loadSql("cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectInstrumentoByNemotecnico.sql");
    }

    /**
     * Executes the JavaCall equivalent of cl.bcs.ordenes.CrearSimulacionAsignacion.call().
     *
     * The params JsonObject contains the same fields as the legacy Entrada inner class.
     * Returns the same structure as the legacy Salida inner class.
     */
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("CrearSimulacionAsignacionService.execute() called");
        Gson gson = new GsonBuilder()
                .serializeSpecialFloatingPointValues()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .serializeNulls()
                .create();

        try {
            // TODO: Implement full logic migrated from legacy CrearSimulacionAsignacion.logica()
            // Legacy class used: cl/bcs/ordenes/sql/CrearSimulacionAsignacion_InsertTransacciones.sql, cl/bcs/ordenes/sql/CrearSimulacionAsignacion_InsertCarteras.sql, cl/bcs/ordenes/sql/CrearSimulacionAsignacion_InsertOrdenes.sql, cl/bcs/ordenes/sql/CrearSimulacionAsignacion_InsertPortafolios.sql, cl/bcs/ordenes/sql/CrearSimulacionAsignacion_CallCrearSimulacionAsignacion.sql, cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectSalida.sql, cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectOrdenes.sql, cl/bcs/ordenes/sql/Globales_SelectIndicadoresPortafolio.sql, cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectPreferenciaPagoDefecto.sql, cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectCuentaCustodio.sql, cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectInformacionOrdenes.sql, cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectInformacionTransacciones.sql, cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectInformacionCarteras.sql, cl/bcs/ordenes/sql/CrearSimulacionAsignacion_SelectInstrumentoByNemotecnico.sql
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
            out.addProperty("_migration_status", "stub — implement from legacy CrearSimulacionAsignacion.logica()");
            return out;
        } catch (Exception e) {
            log.error("CrearSimulacionAsignacionService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String loadSql(String path) {
        try (var is = CrearSimulacionAsignacionService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
