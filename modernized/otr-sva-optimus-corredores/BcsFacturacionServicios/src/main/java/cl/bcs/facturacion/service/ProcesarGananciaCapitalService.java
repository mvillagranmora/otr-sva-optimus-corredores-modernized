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
public class ProcesarGananciaCapitalService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarGananciaCapitalService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectComprobanteCargo;
    private final String sqlSelectComprobanteAbonoNC;
    private final String sqlSelectMovimientoComprobanteDetalle;
    private final String sqlSelectMovimientoComprobanteDetalleNC;
    private final String sqlSelectTesoreriaComprobante;
    private final String sqlSelectTesoreriaComprobanteDetalle;
    private final String sqlSelectTesoreriaComprobanteFormaDePago;
    private final String sqlSelectTesoreriaIngresoEgreso;
    private final String sqlSelectTesoreriaIngresoEgresoFormaDePago;
    private final String sqlUpdateComprobante;
    private final String sqlUpdateComprobanteDetalle;
    private final String sqlUpdateMovimientoComprobanteDetalle;
    private final String sqlUpdateMovimientoComprobanteDetalleNominal;

    public ProcesarGananciaCapitalService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectComprobanteCargo                   = loadSql("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectComprobanteCargo.sql");
        this.sqlSelectComprobanteAbonoNC                 = loadSql("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectComprobanteAbonoNC.sql");
        this.sqlSelectMovimientoComprobanteDetalle       = loadSql("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectMovimientoComprobanteDetalle.sql");
        this.sqlSelectMovimientoComprobanteDetalleNC     = loadSql("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectMovimientoComprobanteDetalleNC.sql");
        this.sqlSelectTesoreriaComprobante               = loadSql("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectTesoreriaComprobante.sql");
        this.sqlSelectTesoreriaComprobanteDetalle        = loadSql("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectTesoreriaComprobanteDetalle.sql");
        this.sqlSelectTesoreriaComprobanteFormaDePago    = loadSql("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectTesoreriaComprobanteFormaDePago.sql");
        this.sqlSelectTesoreriaIngresoEgreso             = loadSql("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectTesoreriaIngresoEgreso.sql");
        this.sqlSelectTesoreriaIngresoEgresoFormaDePago  = loadSql("cl/bcs/facturacion/sql/ProcesarGananciaCapital_SelectTesoreriaIngresoEgresoFormaDePago.sql");
        this.sqlUpdateComprobante                        = loadSql("cl/bcs/facturacion/sql/ProcesarGananciaCapital_UpdateComprobante.sql");
        this.sqlUpdateComprobanteDetalle                 = loadSql("cl/bcs/facturacion/sql/ProcesarGananciaCapital_UpdateComprobanteDetalle.sql");
        this.sqlUpdateMovimientoComprobanteDetalle       = loadSql("cl/bcs/facturacion/sql/ProcesarGananciaCapital_UpdateMovimientoComprobanteDetalle.sql");
        this.sqlUpdateMovimientoComprobanteDetalleNominal = loadSql("cl/bcs/facturacion/sql/ProcesarGananciaCapital_UpdateMovimientoComprobanteDetalleNominal.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ProcesarGananciaCapital");

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
            List<Map<String, Object>> comprobanteCargo = jdbc.queryForList(sqlSelectComprobanteCargo, p);
            jdbc.queryForList(sqlSelectComprobanteAbonoNC, p);
            jdbc.queryForList(sqlSelectMovimientoComprobanteDetalle, p);
            jdbc.queryForList(sqlSelectMovimientoComprobanteDetalleNC, p);
            jdbc.queryForList(sqlSelectTesoreriaComprobante, p);
            jdbc.queryForList(sqlSelectTesoreriaComprobanteDetalle, p);
            jdbc.queryForList(sqlSelectTesoreriaComprobanteFormaDePago, p);
            jdbc.queryForList(sqlSelectTesoreriaIngresoEgreso, p);
            jdbc.queryForList(sqlSelectTesoreriaIngresoEgresoFormaDePago, p);

            jdbc.update(sqlUpdateComprobante, p);
            jdbc.update(sqlUpdateComprobanteDetalle, p);
            jdbc.update(sqlUpdateMovimientoComprobanteDetalle, p);
            jdbc.update(sqlUpdateMovimientoComprobanteDetalleNominal, p);

            out.addProperty("P_OUT_COMPROBANTES_PROCESADOS", comprobanteCargo.size());

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in ProcesarGananciaCapital", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in ProcesarGananciaCapital", ex);
            addError(out, "ERROR_PROCESAR_GANANCIA_CAPITAL", ex.getMessage());
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
        try (InputStream is = ProcesarGananciaCapitalService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
