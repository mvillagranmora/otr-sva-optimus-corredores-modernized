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
public class AjusteSencilloComprobanteService {

    private static final Logger log = LoggerFactory.getLogger(AjusteSencilloComprobanteService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectFormasPago;
    private final String sqlSelectOperacionesAsignadas;
    private final String sqlSelectParidadComprobante;
    private final String sqlSelectTransaccionMonto;
    private final String sqlUpdateAsignacion;
    private final String sqlUpdateComprobante;
    private final String sqlUpdateFormasPago;

    public AjusteSencilloComprobanteService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectFormasPago          = loadSql("cl/bcs/facturacion/sql/AjusteSencilloComprobante_SelectFormasdePago.sql");
        this.sqlSelectOperacionesAsignadas = loadSql("cl/bcs/facturacion/sql/AjusteSencilloComprobante_SelectOperacionesAsignadas.sql");
        this.sqlSelectParidadComprobante  = loadSql("cl/bcs/facturacion/sql/AjusteSencilloComprobante_SelectParidadComprobante.sql");
        this.sqlSelectTransaccionMonto    = loadSql("cl/bcs/facturacion/sql/AjusteSencilloComprobante_SelectTransaccionMonto.sql");
        this.sqlUpdateAsignacion          = loadSql("cl/bcs/facturacion/sql/AjusteSencilloComprobante_UpdateAsignacion.sql");
        this.sqlUpdateComprobante         = loadSql("cl/bcs/facturacion/sql/AjusteSencilloComprobante_UpdateComprobante.sql");
        this.sqlUpdateFormasPago          = loadSql("cl/bcs/facturacion/sql/AjusteSencilloComprobante_UpdateFormasdePago.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::AjusteSencilloComprobante");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",       getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",       getL(params, "p_IdUsuario"))
            .addValue("IdModulo",        getS(params, "p_IdModulo"))
            .addValue("IdComprobante",   getL(params, "p_IdComprobante"))
            .addValue("MontoBruto",      params.has("p_MontoBruto") ? params.get("p_MontoBruto").getAsBigDecimal() : null)
            .addValue("MontoNeto",       params.has("p_MontoNeto")  ? params.get("p_MontoNeto").getAsBigDecimal()  : null)
            .addValue("SimularOperacion",getS(params, "p_SimularOperacion", "N"))
            .addValue("UsaReglasNegocio",getS(params, "p_UsaReglasNegocio", "N"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            List<Map<String, Object>> formasPago   = jdbc.queryForList(sqlSelectFormasPago, p);
            List<Map<String, Object>> operaciones  = jdbc.queryForList(sqlSelectOperacionesAsignadas, p);
            List<Map<String, Object>> paridad      = jdbc.queryForList(sqlSelectParidadComprobante, p);
            List<Map<String, Object>> monto        = jdbc.queryForList(sqlSelectTransaccionMonto, p);

            // TODO: call CalcularParidad via RestClient (BcsPlataformaServicios)
            // endpoint: ${bcs.plataforma.url}/internal/java-call
            // class: cl.bcs.plataforma.pla.CalcularParidad

            jdbc.update(sqlUpdateComprobante, p);
            jdbc.update(sqlUpdateAsignacion, p);
            jdbc.update(sqlUpdateFormasPago, p);

            out.addProperty("P_OUT_FORMAS_PAGO", formasPago.size());
            out.addProperty("P_OUT_OPERACIONES", operaciones.size());

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in AjusteSencilloComprobante", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in AjusteSencilloComprobante", ex);
            addError(out, "ERROR_AJUSTE_SENCILLO", ex.getMessage());
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
        try (InputStream is = AjusteSencilloComprobanteService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
