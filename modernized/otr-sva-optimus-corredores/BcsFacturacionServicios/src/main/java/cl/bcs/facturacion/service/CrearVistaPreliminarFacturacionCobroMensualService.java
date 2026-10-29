package cl.bcs.facturacion.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class CrearVistaPreliminarFacturacionCobroMensualService {

    private static final Logger log = LoggerFactory.getLogger(CrearVistaPreliminarFacturacionCobroMensualService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectCobrosComprobantes;
    private final String sqlSelectResumenContrato;

    public CrearVistaPreliminarFacturacionCobroMensualService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectCobrosComprobantes = loadSql(
            "cl/bcs/facturacion/sql/CrearVistaPreliminarFacturacionCobroMensual_SelectCobrosComprobantes.sql");
        this.sqlSelectResumenContrato = loadSql(
            "cl/bcs/facturacion/sql/CrearVistaPreliminarFacturacionCobroMensual_SelectResumenContrato.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::CrearVistaPreliminarFacturacionCobroMensual");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",  getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",  getL(params, "p_IdUsuario"))
            .addValue("IdModulo",   getS(params, "p_IdModulo"))
            .addValue("FechaDesde", getS(params, "p_FechaDesde"))
            .addValue("FechaHasta", getS(params, "p_FechaHasta"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            List<Map<String, Object>> cobros  = jdbc.queryForList(sqlSelectCobrosComprobantes, p);
            List<Map<String, Object>> resumen = jdbc.queryForList(sqlSelectResumenContrato, p);

            JsonArray cobrosArr = new JsonArray();
            cobros.forEach(r -> cobrosArr.add(GSON.toJsonTree(r)));
            JsonArray resumenArr = new JsonArray();
            resumen.forEach(r -> resumenArr.add(GSON.toJsonTree(r)));

            out.add("P_OUT_COBROS_COMPROBANTES", cobrosArr);
            out.add("P_OUT_RESUMEN_CONTRATO", resumenArr);

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in CrearVistaPreliminarFacturacionCobroMensual", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in CrearVistaPreliminarFacturacionCobroMensual", ex);
            addError(out, "ERROR_VISTA_PRELIMINAR", ex.getMessage());
        }

        return out;
    }

    private static void addError(JsonObject out, String codigo, String desc) {
        long cnt = out.has("P_OUT_ERRORESCONTADOR") ? out.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
        out.addProperty("P_OUT_ERRORESCONTADOR", cnt + 1);
        JsonObject msg = new JsonObject();
        msg.addProperty("Codigo", codigo);
        msg.addProperty("Descripcion", desc);
        out.getAsJsonArray("P_OUT_MENSAJES").add(msg);
    }

    private static Long getL(JsonObject p, String key) {
        return p.has(key) && !p.get(key).isJsonNull() ? p.get(key).getAsLong() : null;
    }

    private static String getS(JsonObject p, String key) {
        return p.has(key) && !p.get(key).isJsonNull() ? p.get(key).getAsString() : null;
    }

    private static String loadSql(String path) {
        try (InputStream is = CrearVistaPreliminarFacturacionCobroMensualService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
