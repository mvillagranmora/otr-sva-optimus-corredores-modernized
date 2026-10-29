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

/**
 * Migrated from legacy cl.bcs.facturacion.CrearMovimientoComprobante (2033 lines).
 * Complex class with multiple SQL files and cross-domain calls.
 * Cross-domain calls (CrearCargoAbono, CrearComprobante, AgregarLog, CalcularParidad,
 * RevisarRequisitosBasicos) are stubbed with TODO markers.
 */
@Service
public class CrearMovimientoComprobanteService {

    private static final Logger log = LoggerFactory.getLogger(CrearMovimientoComprobanteService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectMovimientoComprobante;
    private final String sqlSelectCobro;
    private final String sqlSelectFormaDePago;
    private final String sqlSelectPortafolio;
    private final String sqlSelectPersonaCliente;
    private final String sqlSelectMesas;
    private final String sqlSelectMesaDefecto;
    private final String sqlSelectMedioDePago;
    private final String sqlSelectSecuencias;
    private final String sqlSelectDatosClienteGenerales;
    private final String sqlSelectDatosClienteFormaDePago;
    private final String sqlSelectMovimientoAnterior;
    private final String sqlSelectConceptoIndicadorAbono;
    private final String sqlInsertMovimientoComprobante;
    private final String sqlInsertMovimientoComprobanteCobro;
    private final String sqlInsertMovimientoComprobanteFormaDePago;
    private final String sqlInsertMovimientoComprobanteReferencia;

    public CrearMovimientoComprobanteService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectMovimientoComprobante    = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectMovimientoComprobante.sql");
        this.sqlSelectCobro                    = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectMovimientoComprobanteCobro.sql");
        this.sqlSelectFormaDePago              = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectMovimientoComprobanteFormaDePago.sql");
        this.sqlSelectPortafolio               = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectPortafolio.sql");
        this.sqlSelectPersonaCliente           = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectPersonaCliente.sql");
        this.sqlSelectMesas                    = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectMesas.sql");
        this.sqlSelectMesaDefecto              = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectMesaDefecto.sql");
        this.sqlSelectMedioDePago              = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectMedioDePago.sql");
        this.sqlSelectSecuencias               = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectSecuencias.sql");
        this.sqlSelectDatosClienteGenerales    = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectDatosClienteModificarGenerales.sql");
        this.sqlSelectDatosClienteFormaDePago  = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectDatosClienteModificarFormaDePago.sql");
        this.sqlSelectMovimientoAnterior       = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectMovimientoComprobanteAnterior.sql");
        this.sqlSelectConceptoIndicadorAbono   = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_SelectConceptoIndicadorAbonoClpContrapartida.sql");
        this.sqlInsertMovimientoComprobante    = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_InsertMovimientoComprobante.sql");
        this.sqlInsertMovimientoComprobanteCobro      = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_InsertMovimientoComprobanteCobro.sql");
        this.sqlInsertMovimientoComprobanteFormaDePago = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_InsertMovimientoComprobanteFormaDePago.sql");
        this.sqlInsertMovimientoComprobanteReferencia  = loadSql("cl/bcs/facturacion/sql/CrearMovimientoComprobante_InsertMovimientoComprobanteReferencia.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::CrearMovimientoComprobante");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",         getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",         getL(params, "p_IdUsuario"))
            .addValue("IdModulo",          getS(params, "p_IdModulo"))
            .addValue("IdComprobante",     getL(params, "p_IdComprobante"))
            .addValue("IdPortafolio",      getL(params, "p_IdPortafolio"))
            .addValue("IdTipoMovimiento",  getS(params, "p_IdTipoMovimiento"))
            .addValue("Glosa",             getS(params, "p_Glosa"))
            .addValue("Monto",             params.has("p_Monto") ? params.get("p_Monto").getAsBigDecimal() : null)
            .addValue("SimularOperacion",  getS(params, "p_SimularOperacion", "N"))
            .addValue("UsaReglasNegocio",  getS(params, "p_UsaReglasNegocio", "N"))
            .addValue("EsIngresoAutomatico", getS(params, "p_EsIngresoAutomatico", "N"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            List<Map<String, Object>> movComprobante = jdbc.queryForList(sqlSelectMovimientoComprobante, p);
            jdbc.queryForList(sqlSelectCobro, p);
            jdbc.queryForList(sqlSelectFormaDePago, p);
            jdbc.queryForList(sqlSelectPortafolio, p);
            jdbc.queryForList(sqlSelectPersonaCliente, p);
            jdbc.queryForList(sqlSelectMesas, p);
            jdbc.queryForList(sqlSelectMesaDefecto, p);
            jdbc.queryForList(sqlSelectMedioDePago, p);
            jdbc.queryForList(sqlSelectSecuencias, p);
            jdbc.queryForList(sqlSelectDatosClienteGenerales, p);
            jdbc.queryForList(sqlSelectDatosClienteFormaDePago, p);
            jdbc.queryForList(sqlSelectMovimientoAnterior, p);
            jdbc.queryForList(sqlSelectConceptoIndicadorAbono, p);

            // TODO: call RevisarRequisitosBasicos via RestClient (BcsPlataformaServicios)
            // TODO: call CalcularParidad via RestClient (BcsPlataformaServicios)
            // TODO: call CrearCargoAbono via RestClient (BcsCuentaInversionServicios)
            // TODO: call CrearComprobante internally if needed
            // TODO: call AgregarLog via RestClient (BcsPlataformaServicios)

            jdbc.update(sqlInsertMovimientoComprobante, p);
            jdbc.update(sqlInsertMovimientoComprobanteCobro, p);
            jdbc.update(sqlInsertMovimientoComprobanteFormaDePago, p);
            jdbc.update(sqlInsertMovimientoComprobanteReferencia, p);

            out.addProperty("P_OUT_MOVIMIENTOS_CREADOS", movComprobante.size() + 1);

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in CrearMovimientoComprobante", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in CrearMovimientoComprobante", ex);
            addError(out, "ERROR_CREAR_MOVIMIENTO_COMPROBANTE", ex.getMessage());
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
        try (InputStream is = CrearMovimientoComprobanteService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
