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
public class ModificarMovimientoComprobanteSPService {

    private static final Logger log = LoggerFactory.getLogger(ModificarMovimientoComprobanteSPService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlInsertMovimientoTemp;
    private final String sqlInsertFormaDePagoTemp;
    private final String sqlCallProcedimiento;
    private final String sqlSelectOutInformacion;
    private final String sqlDeleteReferencia;

    public ModificarMovimientoComprobanteSPService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlInsertMovimientoTemp  = loadSql(
            "cl/bcs/facturacion/sql/ModificarMovimientoComprobanteSP_InsertMovimientoTablaTemporal.sql");
        this.sqlInsertFormaDePagoTemp = loadSql(
            "cl/bcs/facturacion/sql/ModificarMovimientoComprobanteSP_InsertFormaDePagoTablaTemporal.sql");
        this.sqlCallProcedimiento     = loadSql(
            "cl/bcs/facturacion/sql/ModificarMovimientoComprobanteSP_CallProcedimiento.sql");
        this.sqlSelectOutInformacion  = loadSql(
            "cl/bcs/facturacion/sql/ModificarMovimientoComprobanteSP_SelectOUTInformacion.sql");
        this.sqlDeleteReferencia      = loadSql(
            "cl/bcs/facturacion/sql/ModificarMovimientoComprobanteSP_DeleteMovimientoComprobanteReferencia.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ModificarMovimientoComprobanteSP");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",                      getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",                      getL(params, "p_IdUsuario"))
            .addValue("IdModulo",                       getS(params, "p_IdModulo"))
            .addValue("IdComprobante",                  getL(params, "p_IdComprobante"))
            .addValue("SecuenciaMovimientoComprobante", getL(params, "p_SecuenciaMovimientoComprobante"))
            .addValue("SimularOperacion",               getS(params, "p_SimularOperacion", "N"))
            .addValue("UsaReglasNegocio",               getS(params, "p_UsaReglasNegocio", "N"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            jdbc.update(sqlInsertMovimientoTemp, p);
            jdbc.update(sqlInsertFormaDePagoTemp, p);
            jdbc.update(sqlCallProcedimiento, p);

            List<Map<String, Object>> outInfo = jdbc.queryForList(sqlSelectOutInformacion, p);
            jdbc.update(sqlDeleteReferencia, p);

            JsonArray arr = new JsonArray();
            outInfo.forEach(r -> arr.add(GSON.toJsonTree(r)));
            out.add("P_OUT_INFORMACION", arr);

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in ModificarMovimientoComprobanteSP", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in ModificarMovimientoComprobanteSP", ex);
            addError(out, "ERROR_MODIFICAR_SP", ex.getMessage());
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

    private static String getS(JsonObject p, String key, String def) {
        return p.has(key) && !p.get(key).isJsonNull() ? p.get(key).getAsString() : def;
    }

    private static String loadSql(String path) {
        try (InputStream is = ModificarMovimientoComprobanteSPService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
