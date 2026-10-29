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
 * Spring @Service migrated from legacy cl.bcs.ordenes.CuadraturaDiaria (JavaCall).
 *
 * Migration status: MIGRATE — no unsatisfiable external service dependencies.
 * All SQL files loaded from resources/cl/bcs/ordenes/sql/.
 *
 * TODO: Validate execute() against integration tests.
 */
@Service
public class CuadraturaDiariaService {

    private static final Logger log = LoggerFactory.getLogger(CuadraturaDiariaService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_CUADRATURADIARIAOPERACIONES;
    private final String SQL_SELECT_TRANSACCIONES;
    private final String SQL_SELECT_TRANSACCIONESOTC;
    private final String SQL_SELECT_ORDENESPIVOT;
    private final String SQL_DELETE_CUADRATURADIARIAOPERACIONES;
    private final String SQL_INSERT_CUADRATURADIARIAOPERACIONES;
    private final String SQL_SELECT_DETALLE_CUADRATURADIARIAOPERACIONES;
    private final String SQL_SELECT_ASIGNACIONES;
    private final String SQL_SELECT_ASIGNACIONESOTC;
    private final String SQL_SELECT_MOVIMIENTOS_FACTURAR;
    private final String SQL_SELECT_MOVIMIENTOS_FACTURAROTC;
    private final String SQL_SELECT_CUADRATURA_CAVALI;
    private final String SQL_SELECT_TRANSACCIONES_FERIADO;
    private final String SQL_SELECT_EVENTOS_BURSATIL_RV;
    private final String SQL_SELECT_EVENTOS_BURSATIL_IF;
    private final String SQL_SELECT_EVENTOS_BURSATIL_RF;
    private final String SQL_SELECT_CODIGOCORREDORA;

    public CuadraturaDiariaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_CUADRATURADIARIAOPERACIONES = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectCuadraturaDiariaOperaciones.sql");
        this.SQL_SELECT_TRANSACCIONES = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransacciones.sql");
        this.SQL_SELECT_TRANSACCIONESOTC = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransaccionesOTC.sql");
        this.SQL_SELECT_ORDENESPIVOT = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_ConsultarConfiguracionOrdenesPivot.sql");
        this.SQL_DELETE_CUADRATURADIARIAOPERACIONES = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_DeleteCuadraturaDiariaOperaciones.sql");
        this.SQL_INSERT_CUADRATURADIARIAOPERACIONES = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_InsertCuadraturaDiariaOperaciones.sql");
        this.SQL_SELECT_DETALLE_CUADRATURADIARIAOPERACIONES = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectJSONCuadraturaDiariaOperaciones.sql");
        this.SQL_SELECT_ASIGNACIONES = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectAsignaciones.sql");
        this.SQL_SELECT_ASIGNACIONESOTC = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectAsignacionesOTC.sql");
        this.SQL_SELECT_MOVIMIENTOS_FACTURAR = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectMovimientoComprobante.sql");
        this.SQL_SELECT_MOVIMIENTOS_FACTURAROTC = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectMovimientoComprobanteOTC.sql");
        this.SQL_SELECT_CUADRATURA_CAVALI = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectCuadraturaCavali.sql");
        this.SQL_SELECT_TRANSACCIONES_FERIADO = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransaccionesFeriado.sql");
        this.SQL_SELECT_EVENTOS_BURSATIL_RV = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransaccionesRuebasRV.sql");
        this.SQL_SELECT_EVENTOS_BURSATIL_IF = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransaccionesRuebasIF.sql");
        this.SQL_SELECT_EVENTOS_BURSATIL_RF = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransaccionesRuebasRF.sql");
        this.SQL_SELECT_CODIGOCORREDORA = loadSql("cl/bcs/ordenes/sql/CuadraturaDiaria_SelectCodigoCorredora.sql");
    }

    /**
     * Executes the JavaCall equivalent of cl.bcs.ordenes.CuadraturaDiaria.call().
     *
     * The params JsonObject contains the same fields as the legacy Entrada inner class.
     * Returns the same structure as the legacy Salida inner class.
     */
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("CuadraturaDiariaService.execute() called");
        Gson gson = new GsonBuilder()
                .serializeSpecialFloatingPointValues()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .serializeNulls()
                .create();

        try {
            // TODO: Implement full logic migrated from legacy CuadraturaDiaria.logica()
            // Legacy class used: cl/bcs/ordenes/sql/CuadraturaDiaria_SelectCuadraturaDiariaOperaciones.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransacciones.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransaccionesOTC.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_ConsultarConfiguracionOrdenesPivot.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_DeleteCuadraturaDiariaOperaciones.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_InsertCuadraturaDiariaOperaciones.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_SelectJSONCuadraturaDiariaOperaciones.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_SelectAsignaciones.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_SelectAsignacionesOTC.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_SelectMovimientoComprobante.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_SelectMovimientoComprobanteOTC.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_SelectCuadraturaCavali.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransaccionesFeriado.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransaccionesRuebasRV.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransaccionesRuebasIF.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_SelectTransaccionesRuebasRF.sql, cl/bcs/ordenes/sql/CuadraturaDiaria_SelectCodigoCorredora.sql
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
            out.addProperty("_migration_status", "stub — implement from legacy CuadraturaDiaria.logica()");
            return out;
        } catch (Exception e) {
            log.error("CuadraturaDiariaService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String loadSql(String path) {
        try (var is = CuadraturaDiariaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
