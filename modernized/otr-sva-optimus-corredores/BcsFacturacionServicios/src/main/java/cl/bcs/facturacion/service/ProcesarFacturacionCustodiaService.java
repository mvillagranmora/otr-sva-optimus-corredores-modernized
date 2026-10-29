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
public class ProcesarFacturacionCustodiaService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarFacturacionCustodiaService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectConcepto;
    private final String sqlSelectContraparteSimultanea;
    private final String sqlSelectContraparte;
    private final String sqlSelectIndicadorPortafolio;
    private final String sqlSelectGarantia;
    private final String sqlSelectGarantiaAnticipoParametro;
    private final String sqlSelectCarteraRelacionada;
    private final String sqlSelectFolioCarteraRelacionado;
    private final String sqlSelectPrestamoVentaCorta;
    private final String sqlSelectSubyacente;
    private final String sqlSelectOSAGarantia;
    private final String sqlSelectConceptoNominalCert;
    private final String sqlSelectCertificaEn;

    public ProcesarFacturacionCustodiaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectConcepto                = loadSql("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarConcepto.sql");
        this.sqlSelectContraparteSimultanea   = loadSql("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarContraparteSimultanea.sql");
        this.sqlSelectContraparte             = loadSql("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarContraparte.sql");
        this.sqlSelectIndicadorPortafolio     = loadSql("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarIndicadorPortafolio.sql");
        this.sqlSelectGarantia               = loadSql("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarGarantia.sql");
        this.sqlSelectGarantiaAnticipoParametro = loadSql("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarGarantiaAnticipoParametroEncendido.sql");
        this.sqlSelectCarteraRelacionada      = loadSql("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarCarteraRelacionada.sql");
        this.sqlSelectFolioCarteraRelacionado = loadSql("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarFolioCarteraRelacionado.sql");
        this.sqlSelectPrestamoVentaCorta      = loadSql("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarPrestamoVentaCorta.sql");
        this.sqlSelectSubyacente              = loadSql("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarSubyacente.sql");
        this.sqlSelectOSAGarantia             = loadSql("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarOSAGarantia.sql");
        this.sqlSelectConceptoNominalCert     = loadSql("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_ConsultarConceptoCLNominalCertificaAuto.sql");
        this.sqlSelectCertificaEn             = loadSql("cl/bcs/facturacion/sql/ProcesarFacturacionCustodia_SelectCertificaEn.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ProcesarFacturacionCustodia");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",     getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",     getL(params, "p_IdUsuario"))
            .addValue("IdModulo",      getS(params, "p_IdModulo"))
            .addValue("IdPortafolio",  getL(params, "p_IdPortafolio"))
            .addValue("IdInstrumento", getL(params, "p_IdInstrumento"))
            .addValue("FechaEjecucion",getS(params, "p_FechaEjecucion"))
            .addValue("SimularOperacion", getS(params, "p_SimularOperacion", "N"))
            .addValue("UsaReglasNegocio", getS(params, "p_UsaReglasNegocio", "N"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            List<Map<String, Object>> concepto = jdbc.queryForList(sqlSelectConcepto, p);
            jdbc.queryForList(sqlSelectIndicadorPortafolio, p);
            jdbc.queryForList(sqlSelectContraparteSimultanea, p);
            jdbc.queryForList(sqlSelectContraparte, p);
            jdbc.queryForList(sqlSelectGarantia, p);
            jdbc.queryForList(sqlSelectGarantiaAnticipoParametro, p);
            jdbc.queryForList(sqlSelectCarteraRelacionada, p);
            jdbc.queryForList(sqlSelectFolioCarteraRelacionado, p);
            jdbc.queryForList(sqlSelectPrestamoVentaCorta, p);
            jdbc.queryForList(sqlSelectSubyacente, p);
            jdbc.queryForList(sqlSelectOSAGarantia, p);
            jdbc.queryForList(sqlSelectConceptoNominalCert, p);
            jdbc.queryForList(sqlSelectCertificaEn, p);

            // TODO: call CrearMovimientoComprobante internally for each custodia concept
            // TODO: call CrearCargoAbono via RestClient (BcsCuentaInversionServicios)

            out.addProperty("P_OUT_CONCEPTOS_PROCESADOS", concepto.size());

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in ProcesarFacturacionCustodia", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in ProcesarFacturacionCustodia", ex);
            addError(out, "ERROR_PROCESAR_FACTURACION_CUSTODIA", ex.getMessage());
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
        try (InputStream is = ProcesarFacturacionCustodiaService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
