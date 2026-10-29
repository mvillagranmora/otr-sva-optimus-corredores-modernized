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
public class CrearComprobanteNotaDeDebitoService {

    private static final Logger log = LoggerFactory.getLogger(CrearComprobanteNotaDeDebitoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectMovimientos;
    private final String sqlSelectMovimientosComprobanteReferencia;
    private final String sqlSelectVerificarComprobante;
    private final String sqlSelectVerificarMovimiento;
    private final String sqlConsultaCustodia;

    public CrearComprobanteNotaDeDebitoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectMovimientos                     = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeDebito_SelectMovimientos.sql");
        this.sqlSelectMovimientosComprobanteReferencia = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeDebito_SelectMovimientosComprobanteReferencia.sql");
        this.sqlSelectVerificarComprobante            = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeDebito_SelectVerificarComprobante.sql");
        this.sqlSelectVerificarMovimiento             = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeDebito_SelectVerificarMovimiento.sql");
        this.sqlConsultaCustodia                      = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeDebito_ConsultaCustodia.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::CrearComprobanteNotaDeDebito");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",         getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",         getL(params, "p_IdUsuario"))
            .addValue("IdModulo",          getS(params, "p_IdModulo"))
            .addValue("IdComprobante",     getL(params, "p_IdComprobante"))
            .addValue("IdPortafolio",      getL(params, "p_IdPortafolio"))
            .addValue("FechaComprobante",  getS(params, "p_FechaComprobante"))
            .addValue("Glosa",             getS(params, "p_Glosa"))
            .addValue("SimularOperacion",  getS(params, "p_SimularOperacion", "N"))
            .addValue("UsaReglasNegocio",  getS(params, "p_UsaReglasNegocio", "N"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            List<Map<String, Object>> verificarComprobante = jdbc.queryForList(sqlSelectVerificarComprobante, p);
            if (!verificarComprobante.isEmpty()) {
                addError(out, "COMPROBANTE_YA_EXISTE", "El comprobante nota de débito ya existe");
                return out;
            }

            jdbc.queryForList(sqlSelectVerificarMovimiento, p);
            List<Map<String, Object>> movimientos = jdbc.queryForList(sqlSelectMovimientos, p);
            jdbc.queryForList(sqlSelectMovimientosComprobanteReferencia, p);
            jdbc.queryForList(sqlConsultaCustodia, p);

            // TODO: call CrearComprobante internally to create the nota de débito

            JsonArray movArr = new JsonArray();
            movimientos.forEach(r -> movArr.add(GSON.toJsonTree(r)));
            out.add("P_OUT_MOVIMIENTOS", movArr);

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in CrearComprobanteNotaDeDebito", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in CrearComprobanteNotaDeDebito", ex);
            addError(out, "ERROR_CREAR_NOTA_DEBITO", ex.getMessage());
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
        try (InputStream is = CrearComprobanteNotaDeDebitoService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
