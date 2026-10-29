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
public class ProcesarCargaMasivaService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarCargaMasivaService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectCargaProcesarProceso;
    private final String sqlSelectDefinicionCobro;
    private final String sqlSelectDetalleCargaMovimiento;
    private final String sqlSelectDetalleCargaNotaCreditoParcial;
    private final String sqlSelectDetalleCargaNotaCredito;
    private final String sqlSelectFeriados;
    private final String sqlSelectTipoComprobanteFacturacion;
    private final String sqlSelectValorCobro;
    private final String sqlInsertCargaProcesarProceso;
    private final String sqlUpdateCargaMasivaDetalle;
    private final String sqlUpdateCargaMasiva;

    public ProcesarCargaMasivaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectCargaProcesarProceso         = loadSql("cl/bcs/facturacion/sql/ProcesarCargaMasiva_SelectCargaProcesarProceso.sql");
        this.sqlSelectDefinicionCobro              = loadSql("cl/bcs/facturacion/sql/ProcesarCargaMasiva_SelectDefinicionCobro.sql");
        this.sqlSelectDetalleCargaMovimiento       = loadSql("cl/bcs/facturacion/sql/ProcesarCargaMasiva_SelectDetalleCargaMovimiento.sql");
        this.sqlSelectDetalleCargaNotaCreditoParcial = loadSql("cl/bcs/facturacion/sql/ProcesarCargaMasiva_SelectDetalleCargaNotaCreditoParcial.sql");
        this.sqlSelectDetalleCargaNotaCredito      = loadSql("cl/bcs/facturacion/sql/ProcesarCargaMasiva_SelectDetalleCargaNotaCredito.sql");
        this.sqlSelectFeriados                     = loadSql("cl/bcs/facturacion/sql/ProcesarCargaMasiva_SelectFeriados.sql");
        this.sqlSelectTipoComprobanteFacturacion   = loadSql("cl/bcs/facturacion/sql/ProcesarCargaMasiva_SelectTipoComprobanteFacturacion.sql");
        this.sqlSelectValorCobro                   = loadSql("cl/bcs/facturacion/sql/ProcesarCargaMasiva_SelectValorCobro.sql");
        this.sqlInsertCargaProcesarProceso         = loadSql("cl/bcs/facturacion/sql/ProcesarCargaMasiva_InsertCargaProcesarProceso.sql");
        this.sqlUpdateCargaMasivaDetalle           = loadSql("cl/bcs/facturacion/sql/ProcesarCargaMasiva_UpdateCargaMasivaDetalle.sql");
        this.sqlUpdateCargaMasiva                  = loadSql("cl/bcs/facturacion/sql/ProcesarCargaMasiva_UpdateCargaMasiva.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ProcesarCargaMasiva");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",       getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",       getL(params, "p_IdUsuario"))
            .addValue("IdModulo",        getS(params, "p_IdModulo"))
            .addValue("IdCargaMasiva",   getL(params, "p_IdCargaMasiva"))
            .addValue("SimularOperacion",getS(params, "p_SimularOperacion", "N"))
            .addValue("UsaReglasNegocio",getS(params, "p_UsaReglasNegocio", "N"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            List<Map<String, Object>> cargaProceso = jdbc.queryForList(sqlSelectCargaProcesarProceso, p);
            jdbc.queryForList(sqlSelectDefinicionCobro, p);
            jdbc.queryForList(sqlSelectDetalleCargaMovimiento, p);
            jdbc.queryForList(sqlSelectDetalleCargaNotaCreditoParcial, p);
            jdbc.queryForList(sqlSelectDetalleCargaNotaCredito, p);
            jdbc.queryForList(sqlSelectFeriados, p);
            jdbc.queryForList(sqlSelectTipoComprobanteFacturacion, p);
            jdbc.queryForList(sqlSelectValorCobro, p);

            // TODO: for each row in cargaProceso call appropriate comprobante creation service

            jdbc.update(sqlInsertCargaProcesarProceso, p);
            jdbc.update(sqlUpdateCargaMasivaDetalle, p);
            jdbc.update(sqlUpdateCargaMasiva, p);

            out.addProperty("P_OUT_FILAS_PROCESADAS", cargaProceso.size());

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in ProcesarCargaMasiva", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in ProcesarCargaMasiva", ex);
            addError(out, "ERROR_PROCESAR_CARGA_MASIVA", ex.getMessage());
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
        try (InputStream is = ProcesarCargaMasivaService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
