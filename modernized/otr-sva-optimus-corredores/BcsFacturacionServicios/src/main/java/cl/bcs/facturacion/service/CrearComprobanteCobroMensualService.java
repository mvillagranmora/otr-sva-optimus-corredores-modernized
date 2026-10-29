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
public class CrearComprobanteCobroMensualService {

    private static final Logger log = LoggerFactory.getLogger(CrearComprobanteCobroMensualService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectMovimientos;
    private final String sqlSelectMovimientosCobros;
    private final String sqlSelectMovimientosFormasDePago;
    private final String sqlValidacionCobroComision;
    private final String sqlUpdateComprobantes;

    public CrearComprobanteCobroMensualService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectMovimientos            = loadSql("cl/bcs/facturacion/sql/CrearComprobanteCobroMensual_SelectMovimientos.sql");
        this.sqlSelectMovimientosCobros      = loadSql("cl/bcs/facturacion/sql/CrearComprobanteCobroMensual_SelectMovimientosCobros.sql");
        this.sqlSelectMovimientosFormasDePago= loadSql("cl/bcs/facturacion/sql/CrearComprobanteCobroMensual_SelectMovimientosFormasDePago.sql");
        this.sqlValidacionCobroComision      = loadSql("cl/bcs/facturacion/sql/CrearComprobanteCobroMensual_ValidacionCobroComision.sql");
        this.sqlUpdateComprobantes           = loadSql("cl/bcs/facturacion/sql/CrearComprobanteCobroMensual_UpdateComprobantes.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::CrearComprobanteCobroMensual");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",    getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",    getL(params, "p_IdUsuario"))
            .addValue("IdModulo",     getS(params, "p_IdModulo"))
            .addValue("FechaCobro",   getS(params, "p_FechaCobro"))
            .addValue("IdPortafolio", getL(params, "p_IdPortafolio"))
            .addValue("SimularOperacion", getS(params, "p_SimularOperacion", "N"))
            .addValue("UsaReglasNegocio", getS(params, "p_UsaReglasNegocio", "N"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            List<Map<String, Object>> validacion = jdbc.queryForList(sqlValidacionCobroComision, p);
            List<Map<String, Object>> movimientos = jdbc.queryForList(sqlSelectMovimientos, p);
            jdbc.queryForList(sqlSelectMovimientosCobros, p);
            jdbc.queryForList(sqlSelectMovimientosFormasDePago, p);

            jdbc.update(sqlUpdateComprobantes, p);

            JsonArray movArr = new JsonArray();
            movimientos.forEach(r -> movArr.add(GSON.toJsonTree(r)));
            out.add("P_OUT_MOVIMIENTOS", movArr);

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in CrearComprobanteCobroMensual", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in CrearComprobanteCobroMensual", ex);
            addError(out, "ERROR_CREAR_COMPROBANTE_COBRO_MENSUAL", ex.getMessage());
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
        try (InputStream is = CrearComprobanteCobroMensualService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
