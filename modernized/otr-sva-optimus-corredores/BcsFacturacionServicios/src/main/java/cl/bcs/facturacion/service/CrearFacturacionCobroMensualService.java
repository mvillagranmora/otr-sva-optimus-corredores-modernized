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
public class CrearFacturacionCobroMensualService {

    private static final Logger log = LoggerFactory.getLogger(CrearFacturacionCobroMensualService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectMovimientoComprobante;
    private final String sqlDeleteMovimientosCobro;
    private final String sqlDeleteMovimientosCobroDetalle;

    public CrearFacturacionCobroMensualService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectMovimientoComprobante = loadSql(
            "cl/bcs/facturacion/sql/CrearFacturacionCobroMensual_SelectMovimientoComprobante.sql");
        this.sqlDeleteMovimientosCobro = loadSql(
            "cl/bcs/facturacion/sql/CrearFacturacionCobroMensual_DeleteMovimientosCobroComisionMensual.sql");
        this.sqlDeleteMovimientosCobroDetalle = loadSql(
            "cl/bcs/facturacion/sql/CrearFacturacionCobroMensual_DeleteMovimientosCobroComisionMensualDetalle.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::CrearFacturacionCobroMensual");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",  getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",  getL(params, "p_IdUsuario"))
            .addValue("IdModulo",   getS(params, "p_IdModulo"))
            .addValue("FechaDesde", getS(params, "p_FechaDesde"))
            .addValue("FechaHasta", getS(params, "p_FechaHasta"))
            .addValue("SimularOperacion", getS(params, "p_SimularOperacion", "N"))
            .addValue("UsaReglasNegocio", getS(params, "p_UsaReglasNegocio", "N"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            List<Map<String, Object>> movimientos = jdbc.queryForList(sqlSelectMovimientoComprobante, p);

            jdbc.update(sqlDeleteMovimientosCobroDetalle, p);
            jdbc.update(sqlDeleteMovimientosCobro, p);

            // TODO: for each movimiento call CrearComprobanteCobroMensual internally

            out.addProperty("P_OUT_MOVIMIENTOS_PROCESADOS", movimientos.size());

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in CrearFacturacionCobroMensual", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in CrearFacturacionCobroMensual", ex);
            addError(out, "ERROR_CREAR_FACTURACION_COBRO_MENSUAL", ex.getMessage());
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
        try (InputStream is = CrearFacturacionCobroMensualService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
