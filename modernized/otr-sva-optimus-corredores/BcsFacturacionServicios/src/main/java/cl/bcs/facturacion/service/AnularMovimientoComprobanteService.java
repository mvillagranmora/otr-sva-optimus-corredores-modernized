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
public class AnularMovimientoComprobanteService {

    private static final Logger log = LoggerFactory.getLogger(AnularMovimientoComprobanteService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectMovimientos;
    private final String sqlUpdateMovimientos;
    private final String sqlSelectSpotFFMM;

    public AnularMovimientoComprobanteService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectMovimientos = loadSql(
            "cl/bcs/facturacion/sql/AnularMovimientoComprobante_SelectMovimientos.sql");
        this.sqlUpdateMovimientos = loadSql(
            "cl/bcs/facturacion/sql/AnularMovimientoComprobante_UpdateMovimientos.sql");
        this.sqlSelectSpotFFMM = loadSql(
            "cl/bcs/facturacion/sql/AnularMovimientoComprobante_SelectSpotFFMM.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::AnularMovimientoComprobante");

        Long idEmpresa  = getL(params, "p_IdEmpresa");
        Long idUsuario  = getL(params, "p_IdUsuario");
        String idModulo = getS(params, "p_IdModulo");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",                      idEmpresa)
            .addValue("IdUsuario",                      idUsuario)
            .addValue("IdModulo",                       idModulo)
            .addValue("IdComprobante",                  getL(params, "p_IdComprobante"))
            .addValue("SecuenciaMovimientoComprobante", getL(params, "p_SecuenciaMovimientoComprobante"))
            .addValue("IdTipoComprobante",              getS(params, "p_IdTipoComprobante"))
            .addValue("Glosa",                          getS(params, "p_Glosa"))
            .addValue("EsIngresoAutomatico",            getS(params, "p_EsIngresoAutomatico", "N"))
            .addValue("SimularOperacion",               getS(params, "p_SimularOperacion", "N"))
            .addValue("UsaReglasNegocio",               getS(params, "p_UsaReglasNegocio", "N"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            List<Map<String, Object>> movimientos = jdbc.queryForList(sqlSelectMovimientos, p);
            if (movimientos.isEmpty()) {
                addError(out, "SIN_MOVIMIENTOS", "No se encontraron movimientos a anular");
                return out;
            }

            // TODO: call cl.bcs.plataforma.pla.RevisarRequisitosBasicos via RestClient
            //       (endpoint: ${bcs.plataforma.url}/internal/java-call)

            jdbc.update(sqlUpdateMovimientos, p);

            // TODO: call cl.bcs.ordenes.AnularMovFacturarAsignacion via RestClient
            //       (endpoint: ${bcs.ordenes.url}/internal/java-call) for each movimiento

            jdbc.queryForList(sqlSelectSpotFFMM, p); // informational

            // TODO: call cl.bcs.plataforma.pla.AgregarLog via RestClient

            JsonArray movArr = new JsonArray();
            movimientos.forEach(r -> movArr.add(GSON.toJsonTree(r)));
            out.add("P_OUT_MOVIMIENTOS", movArr);

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in AnularMovimientoComprobante", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in AnularMovimientoComprobante", ex);
            addError(out, "ERROR_ANULAR_MOVIMIENTO", ex.getMessage());
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
        try (InputStream is = AnularMovimientoComprobanteService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
