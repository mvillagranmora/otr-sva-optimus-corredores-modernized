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
public class AnularComprobanteNotaDeCreditoService {

    private static final Logger log = LoggerFactory.getLogger(AnularComprobanteNotaDeCreditoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectComprobante;
    private final String sqlSelectAsignacion;
    private final String sqlSelectCarterasCompra;
    private final String sqlSelectCarterasVenta;
    private final String sqlSelectCarterasNegativas;
    private final String sqlSelectComprobanteTesoreria;
    private final String sqlSelectEventoCapital;
    private final String sqlSelectTransitos;
    private final String sqlUpdateAsignacion;
    private final String sqlUpdateCarterasNegativas;
    private final String sqlUpdateComprobanteFactura;
    private final String sqlUpdateMovimientoComprobante;

    public AnularComprobanteNotaDeCreditoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectComprobante        = loadSql("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectComprobante.sql");
        this.sqlSelectAsignacion         = loadSql("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectAsignacion.sql");
        this.sqlSelectCarterasCompra     = loadSql("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectCarterasCompra.sql");
        this.sqlSelectCarterasVenta      = loadSql("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectCarterasVenta.sql");
        this.sqlSelectCarterasNegativas  = loadSql("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectCarterasNegativas.sql");
        this.sqlSelectComprobanteTesoreria = loadSql("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectComprobanteTesoreria.sql");
        this.sqlSelectEventoCapital      = loadSql("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectEventoCapital.sql");
        this.sqlSelectTransitos          = loadSql("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_SelectTransitos.sql");
        this.sqlUpdateAsignacion         = loadSql("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_UpdateAsignacion.sql");
        this.sqlUpdateCarterasNegativas  = loadSql("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_UpdateCarterasNegativas.sql");
        this.sqlUpdateComprobanteFactura = loadSql("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_UpdateComprobanteFactura.sql");
        this.sqlUpdateMovimientoComprobante = loadSql("cl/bcs/facturacion/sql/AnularComprobanteNotaDeCredito_UpdateMovimientoComprobante.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::AnularComprobanteNotaDeCredito");

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
                addError(out, "COMPROBANTE_NO_ENCONTRADO", "Comprobante nota de crédito no encontrado");
                return out;
            }

            jdbc.queryForList(sqlSelectAsignacion, p);
            jdbc.queryForList(sqlSelectCarterasCompra, p);
            jdbc.queryForList(sqlSelectCarterasVenta, p);
            jdbc.queryForList(sqlSelectCarterasNegativas, p);
            jdbc.queryForList(sqlSelectComprobanteTesoreria, p);
            jdbc.queryForList(sqlSelectEventoCapital, p);
            jdbc.queryForList(sqlSelectTransitos, p);

            // TODO: call AgregarLog via RestClient (BcsPlataformaServicios)

            jdbc.update(sqlUpdateAsignacion, p);
            jdbc.update(sqlUpdateCarterasNegativas, p);
            jdbc.update(sqlUpdateComprobanteFactura, p);
            jdbc.update(sqlUpdateMovimientoComprobante, p);

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in AnularComprobanteNotaDeCredito", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in AnularComprobanteNotaDeCredito", ex);
            addError(out, "ERROR_ANULAR_NOTA_CREDITO", ex.getMessage());
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
        try (InputStream is = AnularComprobanteNotaDeCreditoService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
