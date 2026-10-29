package cl.bcs.facturacion.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.facturacion.ConsultarMovimientoComprobanteCobrosModificar (JavaCall).
 * Queries movement-voucher charges for a given cobro and a list of movement IDs.
 */
@Service
public class ConsultarMovimientoComprobanteCobrosModificarService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarMovimientoComprobanteCobrosModificarService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelect;

    public ConsultarMovimientoComprobanteCobrosModificarService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelect = loadSql("cl/bcs/facturacion/sql/ConsultarMovimientoComprobanteCobrosModificar_SelectMovimientoComprobanteCobros.sql");
    }

    /**
     * @param params JsonObject with: p_IdCobro (String), p_MovimientosComprobante (JsonArray of {IdMovimientoComprobante})
     * @return JsonObject with: P_OUT_ERRORESCONTADOR=0, P_OUT_MENSAJES=[], P_OUT_COBROS=[...]
     */
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ConsultarMovimientoComprobanteCobrosModificar");

        String idCobro = params.has("p_IdCobro") ? params.get("p_IdCobro").getAsString() : null;

        List<Long> idsMovimiento = new ArrayList<>();
        if (params.has("p_MovimientosComprobante")) {
            JsonArray arr = params.getAsJsonArray("p_MovimientosComprobante");
            for (JsonElement el : arr) {
                JsonObject mov = el.getAsJsonObject();
                if (mov.has("IdMovimientoComprobante")) {
                    idsMovimiento.add(mov.get("IdMovimientoComprobante").getAsLong());
                }
            }
        }

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        out.add("P_OUT_MENSAJES", new JsonArray());

        if (idsMovimiento.isEmpty()) {
            out.add("P_OUT_COBROS", new JsonArray());
            return out;
        }

        MapSqlParameterSource sqlParams = new MapSqlParameterSource()
                .addValue("IdCobro", idCobro)
                .addValue("IdMovimientoComprobante", idsMovimiento);

        List<Map<String, Object>> rows = jdbc.queryForList(sqlSelect, sqlParams);

        Gson gson = new Gson();
        out.add("P_OUT_COBROS", gson.toJsonTree(rows).getAsJsonArray());

        log.info("FIN::ConsultarMovimientoComprobanteCobrosModificar");
        return out;
    }

    private static String loadSql(String resourcePath) {
        try (InputStream is = ConsultarMovimientoComprobanteCobrosModificarService.class
                .getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + resourcePath);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + resourcePath, e);
        }
    }
}
