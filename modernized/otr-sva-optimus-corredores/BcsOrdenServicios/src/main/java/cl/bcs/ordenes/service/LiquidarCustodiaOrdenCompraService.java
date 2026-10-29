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
 * Spring @Service migrated from legacy cl.bcs.ordenes.LiquidarCustodiaOrdenCompra (JavaCall).
 *
 * Migration status: MIGRATE — no unsatisfiable external service dependencies.
 * All SQL files loaded from resources/cl/bcs/ordenes/sql/.
 *
 * TODO: Validate execute() against integration tests.
 */
@Service
public class LiquidarCustodiaOrdenCompraService {

    private static final Logger log = LoggerFactory.getLogger(LiquidarCustodiaOrdenCompraService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_SUBYACENTE;
    private final String SQL_SELECT_GARANTIA;
    private final String SQL_SELECT_CARTERA;
    private final String SQL_SELECT_CARTERA_RELACIONADA;
    private final String SQL_CONSULTAR_CONTRAPARTE_SIMULTANEA;

    public LiquidarCustodiaOrdenCompraService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_SUBYACENTE = loadSql("cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarSubyacente.sql");
        this.SQL_SELECT_GARANTIA = loadSql("cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarGarantia.sql");
        this.SQL_SELECT_CARTERA = loadSql("cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarCartera.sql");
        this.SQL_SELECT_CARTERA_RELACIONADA = loadSql("cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarCarteraRelacionada.sql");
        this.SQL_CONSULTAR_CONTRAPARTE_SIMULTANEA = loadSql("cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarContraparteSimultanea.sql");
    }

    /**
     * Executes the JavaCall equivalent of cl.bcs.ordenes.LiquidarCustodiaOrdenCompra.call().
     *
     * The params JsonObject contains the same fields as the legacy Entrada inner class.
     * Returns the same structure as the legacy Salida inner class.
     */
    public JsonObject execute(JsonObject params) {
        log.debug("LiquidarCustodiaOrdenCompraService.execute() called");
        Gson gson = new GsonBuilder()
                .serializeSpecialFloatingPointValues()
                .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .serializeNulls()
                .create();

        try {
            // TODO: Implement full logic migrated from legacy LiquidarCustodiaOrdenCompra.logica()
            // Legacy class used: cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarSubyacente.sql, cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarGarantia.sql, cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarCartera.sql, cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarCarteraRelacionada.sql, cl/bcs/ordenes/sql/LiquidarCustodiaOrden_ConsultarContraparteSimultanea.sql
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
            out.addProperty("_migration_status", "stub — implement from legacy LiquidarCustodiaOrdenCompra.logica()");
            return out;
        } catch (Exception e) {
            log.error("LiquidarCustodiaOrdenCompraService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String loadSql(String path) {
        try (var is = LiquidarCustodiaOrdenCompraService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
