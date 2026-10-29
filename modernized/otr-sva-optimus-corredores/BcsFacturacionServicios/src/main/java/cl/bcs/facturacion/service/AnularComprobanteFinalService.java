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
public class AnularComprobanteFinalService {

    private static final Logger log = LoggerFactory.getLogger(AnularComprobanteFinalService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectComprobante;
    private final String sqlSelectMovimientoComprobante;
    private final String sqlSelectCarteras;
    private final String sqlSelectCarterasCompra;
    private final String sqlSelectCarterasVenta;
    private final String sqlSelectCarterasNegativas;
    private final String sqlSelectCuentaInversion;
    private final String sqlSelectCustodia;
    private final String sqlSelectGarantiasCompra;
    private final String sqlSelectGarantiasAnticipo;
    private final String sqlSelectTransitos;
    private final String sqlSelectPeriodoCierre;
    private final String sqlUpdateComprobante;
    private final String sqlUpdateMovimientoComprobante;

    public AnularComprobanteFinalService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectComprobante          = loadSql("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectComprobante.sql");
        this.sqlSelectMovimientoComprobante= loadSql("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectMovimientoComprobante.sql");
        this.sqlSelectCarteras             = loadSql("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectCarteras.sql");
        this.sqlSelectCarterasCompra       = loadSql("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectCarterasCompra.sql");
        this.sqlSelectCarterasVenta        = loadSql("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectCarterasVenta.sql");
        this.sqlSelectCarterasNegativas    = loadSql("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectCarterasNegativas.sql");
        this.sqlSelectCuentaInversion      = loadSql("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectCuentaInversion.sql");
        this.sqlSelectCustodia             = loadSql("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectCustodia.sql");
        this.sqlSelectGarantiasCompra      = loadSql("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectGarantiasCompra.sql");
        this.sqlSelectGarantiasAnticipo    = loadSql("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectGarantiasAnticipo.sql");
        this.sqlSelectTransitos            = loadSql("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectTransitos.sql");
        this.sqlSelectPeriodoCierre        = loadSql("cl/bcs/facturacion/sql/AnularComprobanteFinal_SelectPeriodoCierre.sql");
        this.sqlUpdateComprobante          = loadSql("cl/bcs/facturacion/sql/AnularComprobanteFinal_UpdateComprobante.sql");
        this.sqlUpdateMovimientoComprobante= loadSql("cl/bcs/facturacion/sql/AnularComprobanteFinal_UpdateMovimientoComprobante.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::AnularComprobanteFinal");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",     getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",     getL(params, "p_IdUsuario"))
            .addValue("IdModulo",      getS(params, "p_IdModulo"))
            .addValue("IdComprobante", getL(params, "p_IdComprobante"))
            .addValue("SimularOperacion", getS(params, "p_SimularOperacion", "N"))
            .addValue("UsaReglasNegocio", getS(params, "p_UsaReglasNegocio", "N"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            List<Map<String, Object>> comprobante = jdbc.queryForList(sqlSelectComprobante, p);
            if (comprobante.isEmpty()) {
                addError(out, "COMPROBANTE_NO_ENCONTRADO", "Comprobante no encontrado");
                return out;
            }

            jdbc.queryForList(sqlSelectMovimientoComprobante, p);
            jdbc.queryForList(sqlSelectCarteras, p);
            jdbc.queryForList(sqlSelectCarterasCompra, p);
            jdbc.queryForList(sqlSelectCarterasVenta, p);
            jdbc.queryForList(sqlSelectCarterasNegativas, p);
            jdbc.queryForList(sqlSelectCuentaInversion, p);
            jdbc.queryForList(sqlSelectCustodia, p);
            jdbc.queryForList(sqlSelectGarantiasCompra, p);
            jdbc.queryForList(sqlSelectGarantiasAnticipo, p);
            jdbc.queryForList(sqlSelectTransitos, p);
            jdbc.queryForList(sqlSelectPeriodoCierre, p);

            // TODO: call RevisarRequisitosBasicos via RestClient (BcsPlataformaServicios)
            // TODO: call AgregarLog via RestClient (BcsPlataformaServicios)

            jdbc.update(sqlUpdateComprobante, p);
            jdbc.update(sqlUpdateMovimientoComprobante, p);

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in AnularComprobanteFinal", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in AnularComprobanteFinal", ex);
            addError(out, "ERROR_ANULAR_COMPROBANTE_FINAL", ex.getMessage());
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
        try (InputStream is = AnularComprobanteFinalService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
