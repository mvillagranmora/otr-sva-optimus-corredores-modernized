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
public class WsConsultaComprobantesService {

    private static final Logger log = LoggerFactory.getLogger(WsConsultaComprobantesService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectComprobantes;
    private final String sqlSelectCobrosComprobantes;
    private final String sqlSelectDetallesComprobantes;
    private final String sqlSelectPortafolios;

    public WsConsultaComprobantesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectComprobantes       = loadSql("cl/bcs/facturacion/sql/WsConsultaComprobante_SelectComprobantes.sql");
        this.sqlSelectCobrosComprobantes = loadSql("cl/bcs/facturacion/sql/WsConsultaComprobante_SelectCobrosComprobantes.sql");
        this.sqlSelectDetallesComprobantes = loadSql("cl/bcs/facturacion/sql/WsConsultaComprobante_SelectDetallesComprobantes.sql");
        this.sqlSelectPortafolios        = loadSql("cl/bcs/facturacion/sql/WsConsultaComprobante_SelectPortafolios.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::WsConsultaComprobantes");

        // TODO: configurar endpoint WS para consulta externa de comprobantes
        // The WS layer exposes internal HANA data via WebService — now served via this REST endpoint
        // Callers should migrate to /internal/java-call protocol

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",     getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",     getL(params, "p_IdUsuario"))
            .addValue("IdModulo",      getS(params, "p_IdModulo"))
            .addValue("FechaDesde",    getS(params, "p_FechaDesde"))
            .addValue("FechaHasta",    getS(params, "p_FechaHasta"))
            .addValue("IdPortafolio",  getL(params, "p_IdPortafolio"))
            .addValue("IdComprobante", getL(params, "p_IdComprobante"))
            .addValue("RUT",           getS(params, "p_RUT"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            List<Map<String, Object>> comprobantes = jdbc.queryForList(sqlSelectComprobantes, p);
            List<Map<String, Object>> cobros       = jdbc.queryForList(sqlSelectCobrosComprobantes, p);
            List<Map<String, Object>> detalles     = jdbc.queryForList(sqlSelectDetallesComprobantes, p);
            jdbc.queryForList(sqlSelectPortafolios, p);

            JsonArray compArr = new JsonArray();
            comprobantes.forEach(r -> compArr.add(GSON.toJsonTree(r)));
            JsonArray cobrosArr = new JsonArray();
            cobros.forEach(r -> cobrosArr.add(GSON.toJsonTree(r)));
            JsonArray detArr = new JsonArray();
            detalles.forEach(r -> detArr.add(GSON.toJsonTree(r)));

            out.add("P_OUT_COMPROBANTES", compArr);
            out.add("P_OUT_COBROS", cobrosArr);
            out.add("P_OUT_DETALLES", detArr);

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in WsConsultaComprobantes", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in WsConsultaComprobantes", ex);
            addError(out, "ERROR_WS_CONSULTA_COMPROBANTES", ex.getMessage());
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
        try (InputStream is = WsConsultaComprobantesService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
