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
public class CrearComprobanteNotaDeCreditoService {

    private static final Logger log = LoggerFactory.getLogger(CrearComprobanteNotaDeCreditoService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectComprobantes;
    private final String sqlSelectMovimientos;
    private final String sqlSelectMovimientosCobros;
    private final String sqlSelectMovimientosFormasDePago;
    private final String sqlSelectMovimientosComprobanteReferencia;
    private final String sqlSelectIndicadoresPortafolio;
    private final String sqlSelectSpotFFMM;
    private final String sqlSelectCarterasCompra;
    private final String sqlSelectInformacionDetalle;
    private final String sqlSelectResultadoProceso;
    private final String sqlInsertComprobante;
    private final String sqlUpdateAsignacion;

    public CrearComprobanteNotaDeCreditoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectComprobantes                    = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectComprobantes.sql");
        this.sqlSelectMovimientos                     = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectMovimientos.sql");
        this.sqlSelectMovimientosCobros               = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectMovimientosCobros.sql");
        this.sqlSelectMovimientosFormasDePago         = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectMovimientosFormasDePago.sql");
        this.sqlSelectMovimientosComprobanteReferencia = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectMovimientosComprobanteReferencia.sql");
        this.sqlSelectIndicadoresPortafolio           = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectIndicadoresPortafolio.sql");
        this.sqlSelectSpotFFMM                        = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectSpotFFMM.sql");
        this.sqlSelectCarterasCompra                  = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectCarterasCompra.sql");
        this.sqlSelectInformacionDetalle              = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectInformacionDetalle.sql");
        this.sqlSelectResultadoProceso                = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_SelectResultadoProceso.sql");
        this.sqlInsertComprobante                     = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_InsertComprobante.sql");
        this.sqlUpdateAsignacion                      = loadSql("cl/bcs/facturacion/sql/CrearComprobanteNotaDeCredito_UpdateAsignacion.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::CrearComprobanteNotaDeCredito");

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
            List<Map<String, Object>> comprobantes = jdbc.queryForList(sqlSelectComprobantes, p);
            jdbc.queryForList(sqlSelectMovimientos, p);
            jdbc.queryForList(sqlSelectMovimientosCobros, p);
            jdbc.queryForList(sqlSelectMovimientosFormasDePago, p);
            jdbc.queryForList(sqlSelectMovimientosComprobanteReferencia, p);
            jdbc.queryForList(sqlSelectIndicadoresPortafolio, p);
            jdbc.queryForList(sqlSelectSpotFFMM, p);
            jdbc.queryForList(sqlSelectCarterasCompra, p);
            jdbc.queryForList(sqlSelectInformacionDetalle, p);

            if (comprobantes.isEmpty()) {
                addError(out, "COMPROBANTE_NO_ENCONTRADO", "No se encontró el comprobante de referencia");
                return out;
            }

            jdbc.update(sqlInsertComprobante, p);
            jdbc.update(sqlUpdateAsignacion, p);

            List<Map<String, Object>> resultado = jdbc.queryForList(sqlSelectResultadoProceso, p);
            JsonArray resArr = new JsonArray();
            resultado.forEach(r -> resArr.add(GSON.toJsonTree(r)));
            out.add("P_OUT_RESULTADO", resArr);

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in CrearComprobanteNotaDeCredito", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in CrearComprobanteNotaDeCredito", ex);
            addError(out, "ERROR_CREAR_NOTA_CREDITO", ex.getMessage());
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
        try (InputStream is = CrearComprobanteNotaDeCreditoService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
