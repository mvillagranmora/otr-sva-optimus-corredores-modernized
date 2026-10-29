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
public class ModificarRetencionIgkService {

    private static final Logger log = LoggerFactory.getLogger(ModificarRetencionIgkService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectComprobante;
    private final String sqlSelectComprobanteDetalle;
    private final String sqlSelectComprobanteCargoAbono;
    private final String sqlBloqueaMovimiento;
    private final String sqlUpdateRetencion;
    private final String sqlUpdateRetencionDetalle;

    public ModificarRetencionIgkService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectComprobante        = loadSql("cl/bcs/facturacion/sql/ModificarRetencionIgk_SelectComprobante.sql");
        this.sqlSelectComprobanteDetalle = loadSql("cl/bcs/facturacion/sql/ModificarRetencionIgk_SelectComprobanteDetalle.sql");
        this.sqlSelectComprobanteCargoAbono = loadSql("cl/bcs/facturacion/sql/ModificarRetencionIgk_SelectComprobanteCargoAbono.sql");
        this.sqlBloqueaMovimiento        = loadSql("cl/bcs/facturacion/sql/ModificarRetencionIgk_BloqueaMovimiento.sql");
        this.sqlUpdateRetencion          = loadSql("cl/bcs/facturacion/sql/ModificarRetencionIgk_UpdateRetencion.sql");
        this.sqlUpdateRetencionDetalle   = loadSql("cl/bcs/facturacion/sql/ModificarRetencionIgk_UpdateRetencionDetalle.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ModificarRetencionIgk");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",     getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",     getL(params, "p_IdUsuario"))
            .addValue("IdModulo",      getS(params, "p_IdModulo"))
            .addValue("IdComprobante", getL(params, "p_IdComprobante"))
            .addValue("PorcentajeRetencionIGK", params.has("p_PorcentajeRetencionIGK")
                ? params.get("p_PorcentajeRetencionIGK").getAsBigDecimal() : null)
            .addValue("SimularOperacion", getS(params, "p_SimularOperacion", "N"))
            .addValue("UsaReglasNegocio", getS(params, "p_UsaReglasNegocio", "N"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            List<Map<String, Object>> comprobante = jdbc.queryForList(sqlSelectComprobante, p);
            if (comprobante.isEmpty()) {
                addError(out, "COMPROBANTE_NO_ENCONTRADO", "Comprobante no encontrado para IGK");
                return out;
            }

            jdbc.queryForList(sqlSelectComprobanteDetalle, p);
            jdbc.queryForList(sqlSelectComprobanteCargoAbono, p);
            jdbc.update(sqlBloqueaMovimiento, p);

            // TODO: call CrearCargoAbono via RestClient (BcsCuentaInversionServicios)
            // endpoint: ${bcs.cuentainversion.url}/internal/java-call
            // class: cl.bcs.cuentaInversion.CrearCargoAbono

            jdbc.update(sqlUpdateRetencion, p);
            jdbc.update(sqlUpdateRetencionDetalle, p);

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in ModificarRetencionIgk", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in ModificarRetencionIgk", ex);
            addError(out, "ERROR_MODIFICAR_RETENCION_IGK", ex.getMessage());
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
        try (InputStream is = ModificarRetencionIgkService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
