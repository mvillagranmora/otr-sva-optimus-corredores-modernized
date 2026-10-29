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
public class CuadraturaCierreDiarioService {

    private static final Logger log = LoggerFactory.getLogger(CuadraturaCierreDiarioService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlDeleteCuadratura;
    private final String sqlInsertCuadratura;
    private final String sqlSelectCuadratura;
    private final String sqlSelectCuadraturasFacturacion;
    private final String sqlSelectJsonCuadratura;
    private final String sqlSelectMovimientoComprobante;
    private final String sqlSelectPrepagoBono;
    private final String sqlSelectPrestamoFacturacion;
    private final String sqlSelectSpotFacturacion;
    private final String sqlSelectSuscripcionFacturacion;
    private final String sqlSelectTransaccionFacturacionDTE;
    private final String sqlSelectTransaccionFacturacion;
    private final String sqlSelectTransaccionFacturacionPendiente;

    public CuadraturaCierreDiarioService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlDeleteCuadratura              = loadSql("cl/bcs/facturacion/sql/CuadraturaCierreDiario_DeleteCuadraturaDiariaOperaciones.sql");
        this.sqlInsertCuadratura              = loadSql("cl/bcs/facturacion/sql/CuadraturaCierreDiario_InsertCuadraturaDiariaOperaciones.sql");
        this.sqlSelectCuadratura              = loadSql("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectCuadraturaDiariaOperaciones.sql");
        this.sqlSelectCuadraturasFacturacion  = loadSql("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectCuadraturasFacturacion.sql");
        this.sqlSelectJsonCuadratura          = loadSql("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectJSONCuadraturaDiariaOperaciones.sql");
        this.sqlSelectMovimientoComprobante   = loadSql("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectMovimientoComprobante.sql");
        this.sqlSelectPrepagoBono             = loadSql("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectPrepagoBonoFacturacion.sql");
        this.sqlSelectPrestamoFacturacion     = loadSql("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectPrestamoFacturacionPendiente.sql");
        this.sqlSelectSpotFacturacion         = loadSql("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectSpotFacturacion.sql");
        this.sqlSelectSuscripcionFacturacion  = loadSql("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectSuscripcionFacturacion.sql");
        this.sqlSelectTransaccionFacturacionDTE = loadSql("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectTransaccionFacturacionPendiente_dte.sql");
        this.sqlSelectTransaccionFacturacion  = loadSql("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectTransaccionFacturacion.sql");
        this.sqlSelectTransaccionFacturacionPendiente = loadSql("cl/bcs/facturacion/sql/CuadraturaCierreDiario_SelectTransaccionFacturacionPendiente.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::CuadraturaCierreDiario");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",           getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",           getL(params, "p_IdUsuario"))
            .addValue("IdModulo",            getS(params, "p_IdModulo"))
            .addValue("FechaEjecucion",      getS(params, "p_FechaEjecucion"))
            .addValue("SimularOperacion",    getS(params, "p_SimularOperacion", "N"))
            .addValue("UsaReglasNegocio",    getS(params, "p_UsaReglasNegocio", "N"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            jdbc.queryForList(sqlSelectMovimientoComprobante, p);
            jdbc.queryForList(sqlSelectTransaccionFacturacion, p);
            jdbc.queryForList(sqlSelectTransaccionFacturacionPendiente, p);
            jdbc.queryForList(sqlSelectTransaccionFacturacionDTE, p);
            jdbc.queryForList(sqlSelectSpotFacturacion, p);
            jdbc.queryForList(sqlSelectSuscripcionFacturacion, p);
            jdbc.queryForList(sqlSelectPrepagoBono, p);
            jdbc.queryForList(sqlSelectPrestamoFacturacion, p);
            jdbc.queryForList(sqlSelectCuadraturasFacturacion, p);

            jdbc.update(sqlDeleteCuadratura, p);
            jdbc.update(sqlInsertCuadratura, p);

            List<Map<String, Object>> cuadratura = jdbc.queryForList(sqlSelectCuadratura, p);
            List<Map<String, Object>> jsonCuad   = jdbc.queryForList(sqlSelectJsonCuadratura, p);

            // TODO: call AgregarLog via RestClient (BcsPlataformaServicios)
            // endpoint: ${bcs.plataforma.url}/internal/java-call

            JsonArray cuadArr = new JsonArray();
            cuadratura.forEach(r -> cuadArr.add(GSON.toJsonTree(r)));
            out.add("P_OUT_CUADRATURA", cuadArr);

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in CuadraturaCierreDiario", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in CuadraturaCierreDiario", ex);
            addError(out, "ERROR_CUADRATURA_CIERRE_DIARIO", ex.getMessage());
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
        try (InputStream is = CuadraturaCierreDiarioService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
