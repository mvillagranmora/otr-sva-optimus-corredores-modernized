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
public class CrearCargaMasivaRefacturacionService {

    private static final Logger log = LoggerFactory.getLogger(CrearCargaMasivaRefacturacionService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectCliente;
    private final String sqlSelectClientePortafolio;
    private final String sqlSelectClienteCodigoPortafolio;
    private final String sqlSelectPortafolio;
    private final String sqlSelectComprobantes;
    private final String sqlSelectTransacciones;
    private final String sqlSelectTransacciones2;
    private final String sqlSelectTransaccionesPorFolio;
    private final String sqlInsertGlobalTransacciones;

    public CrearCargaMasivaRefacturacionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectCliente                 = loadSql("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_SelectCliente.sql");
        this.sqlSelectClientePortafolio       = loadSql("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_SelectClientePortafolio.sql");
        this.sqlSelectClienteCodigoPortafolio = loadSql("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_SelectClienteCodigoPortafolio.sql");
        this.sqlSelectPortafolio              = loadSql("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_SelectPortafolio.sql");
        this.sqlSelectComprobantes            = loadSql("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_SelectComprobantes.sql");
        this.sqlSelectTransacciones           = loadSql("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_SelectTransacciones.sql");
        this.sqlSelectTransacciones2          = loadSql("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_SelectTransaciones.sql");
        this.sqlSelectTransaccionesPorFolio   = loadSql("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_SelectTransaccionesPorFolio.sql");
        this.sqlInsertGlobalTransacciones     = loadSql("cl/bcs/facturacion/sql/CrearCargaMasivaRefacturacion_InsertGlobalTransacciones.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::CrearCargaMasivaRefacturacion");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",    getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",    getL(params, "p_IdUsuario"))
            .addValue("IdModulo",     getS(params, "p_IdModulo"))
            .addValue("FechaDesde",   getS(params, "p_FechaDesde"))
            .addValue("FechaHasta",   getS(params, "p_FechaHasta"))
            .addValue("IdCliente",    getL(params, "p_IdCliente"))
            .addValue("IdPortafolio", getL(params, "p_IdPortafolio"))
            .addValue("SimularOperacion", getS(params, "p_SimularOperacion", "N"))
            .addValue("UsaReglasNegocio", getS(params, "p_UsaReglasNegocio", "N"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            jdbc.queryForList(sqlSelectCliente, p);
            jdbc.queryForList(sqlSelectClientePortafolio, p);
            jdbc.queryForList(sqlSelectClienteCodigoPortafolio, p);
            jdbc.queryForList(sqlSelectPortafolio, p);
            List<Map<String, Object>> comprobantes = jdbc.queryForList(sqlSelectComprobantes, p);
            jdbc.queryForList(sqlSelectTransacciones, p);
            jdbc.queryForList(sqlSelectTransacciones2, p);
            jdbc.queryForList(sqlSelectTransaccionesPorFolio, p);

            jdbc.update(sqlInsertGlobalTransacciones, p);

            out.addProperty("P_OUT_COMPROBANTES_PROCESADOS", comprobantes.size());

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in CrearCargaMasivaRefacturacion", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in CrearCargaMasivaRefacturacion", ex);
            addError(out, "ERROR_CREAR_CARGA_MASIVA_REFACTURACION", ex.getMessage());
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
        try (InputStream is = CrearCargaMasivaRefacturacionService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
