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
public class CrearComprobanteService {

    private static final Logger log = LoggerFactory.getLogger(CrearComprobanteService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectMovimientos;
    private final String sqlSelectMovimientosCobros;
    private final String sqlSelectMovimientosFormasDePago;
    private final String sqlSelectComprobantesExistentes;
    private final String sqlSelectConfiguracion;
    private final String sqlSelectCondicionDeLiquidacion;
    private final String sqlSelectCobroIva;
    private final String sqlSelectEstadoCajaSpot;
    private final String sqlSelectGlosaTipoInstrumento;
    private final String sqlSelectPreferenciasFacturacion;
    private final String sqlInsertComprobante;
    private final String sqlInsertComprobanteDetalle;
    private final String sqlInsertCobro;
    private final String sqlInsertFormaDePago;
    private final String sqlInsertMovimientoComprobanteDetalle;
    private final String sqlUpdateMovimientos;
    private final String sqlUpdateIngresoMovimientos;
    private final String sqlAnularMovimientoComprobanteCobro;
    private final String sqlBloqueaMovimientos;

    public CrearComprobanteService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectMovimientos               = loadSql("cl/bcs/facturacion/sql/CrearComprobante_SelectMovimientos.sql");
        this.sqlSelectMovimientosCobros         = loadSql("cl/bcs/facturacion/sql/CrearComprobante_SelectMovimientosCobros.sql");
        this.sqlSelectMovimientosFormasDePago   = loadSql("cl/bcs/facturacion/sql/CrearComprobante_SelectMovimientosFormasDePago.sql");
        this.sqlSelectComprobantesExistentes    = loadSql("cl/bcs/facturacion/sql/CrearComprobante_SelectComprobantesExistentes.sql");
        this.sqlSelectConfiguracion             = loadSql("cl/bcs/facturacion/sql/CrearComprobante_SelectConfiguracion.sql");
        this.sqlSelectCondicionDeLiquidacion    = loadSql("cl/bcs/facturacion/sql/CrearComprobante_SelectCondicionDeLiquidacion.sql");
        this.sqlSelectCobroIva                  = loadSql("cl/bcs/facturacion/sql/CrearComprobante_SelectCobroIva.sql");
        this.sqlSelectEstadoCajaSpot            = loadSql("cl/bcs/facturacion/sql/CrearComprobante_SelectEstadoCajaSpot.sql");
        this.sqlSelectGlosaTipoInstrumento      = loadSql("cl/bcs/facturacion/sql/CrearComprobante_SelectGlosaTipoInstrumento.sql");
        this.sqlSelectPreferenciasFacturacion   = loadSql("cl/bcs/facturacion/sql/CrearComprobante_SelectPreferenciasFacturacion.sql");
        this.sqlInsertComprobante               = loadSql("cl/bcs/facturacion/sql/CrearComprobante_InsertComprobante.sql");
        this.sqlInsertComprobanteDetalle        = loadSql("cl/bcs/facturacion/sql/CrearComprobante_InsertComprobanteDetalle.sql");
        this.sqlInsertCobro                     = loadSql("cl/bcs/facturacion/sql/CrearComprobante_InsertCobro.sql");
        this.sqlInsertFormaDePago               = loadSql("cl/bcs/facturacion/sql/CrearComprobante_InsertFormaDePago.sql");
        this.sqlInsertMovimientoComprobanteDetalle = loadSql("cl/bcs/facturacion/sql/CrearComprobante_InsertMovimientoComprobanteDetalle.sql");
        this.sqlUpdateMovimientos               = loadSql("cl/bcs/facturacion/sql/CrearComprobante_UpdateMovimientos.sql");
        this.sqlUpdateIngresoMovimientos        = loadSql("cl/bcs/facturacion/sql/CrearComprobante_UpdateIngresoMovimientos.sql");
        this.sqlAnularMovimientoComprobanteCobro= loadSql("cl/bcs/facturacion/sql/CrearComprobante_AnularMovimientoComprobanteCobro.sql");
        this.sqlBloqueaMovimientos              = loadSql("cl/bcs/facturacion/sql/CrearComprobante_BloqueaMovimientos.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::CrearComprobante");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",         getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",         getL(params, "p_IdUsuario"))
            .addValue("IdModulo",          getS(params, "p_IdModulo"))
            .addValue("IdTipoComprobante", getS(params, "p_IdTipoComprobante"))
            .addValue("FechaComprobante",  getS(params, "p_FechaComprobante"))
            .addValue("IdPortafolio",      getL(params, "p_IdPortafolio"))
            .addValue("Glosa",             getS(params, "p_Glosa"))
            .addValue("SimularOperacion",  getS(params, "p_SimularOperacion", "N"))
            .addValue("UsaReglasNegocio",  getS(params, "p_UsaReglasNegocio", "N"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            jdbc.queryForList(sqlSelectConfiguracion, p);
            jdbc.queryForList(sqlSelectCondicionDeLiquidacion, p);
            jdbc.queryForList(sqlSelectComprobantesExistentes, p);
            jdbc.queryForList(sqlSelectEstadoCajaSpot, p);
            jdbc.queryForList(sqlSelectPreferenciasFacturacion, p);
            jdbc.queryForList(sqlSelectCobroIva, p);
            jdbc.queryForList(sqlSelectGlosaTipoInstrumento, p);

            List<Map<String, Object>> movimientos = jdbc.queryForList(sqlSelectMovimientos, p);
            jdbc.queryForList(sqlSelectMovimientosCobros, p);
            jdbc.queryForList(sqlSelectMovimientosFormasDePago, p);

            if (movimientos.isEmpty()) {
                addError(out, "SIN_MOVIMIENTOS", "No hay movimientos para crear comprobante");
                return out;
            }

            jdbc.update(sqlBloqueaMovimientos, p);

            // TODO: call RevisarRequisitosBasicos via RestClient (BcsPlataformaServicios)
            // endpoint: ${bcs.plataforma.url}/internal/java-call

            jdbc.update(sqlInsertComprobante, p);
            jdbc.update(sqlInsertComprobanteDetalle, p);
            jdbc.update(sqlInsertCobro, p);
            jdbc.update(sqlInsertFormaDePago, p);
            jdbc.update(sqlInsertMovimientoComprobanteDetalle, p);
            jdbc.update(sqlUpdateMovimientos, p);
            jdbc.update(sqlUpdateIngresoMovimientos, p);
            jdbc.update(sqlAnularMovimientoComprobanteCobro, p);

            // TODO: call AgregarLog via RestClient (BcsPlataformaServicios)

            out.addProperty("P_OUT_MOVIMIENTOS_PROCESADOS", movimientos.size());

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in CrearComprobante", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in CrearComprobante", ex);
            addError(out, "ERROR_CREAR_COMPROBANTE", ex.getMessage());
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
        try (InputStream is = CrearComprobanteService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
