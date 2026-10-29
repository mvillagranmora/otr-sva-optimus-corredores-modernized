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
public class GenerarInformePrefacturasService {

    private static final Logger log = LoggerFactory.getLogger(GenerarInformePrefacturasService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlConsultarPrefacturas;
    private final String sqlConsultarPrefacturasTodas;
    private final String sqlConsultarPrefacturasFAC;
    private final String sqlConsultarPrefacturasFACTodas;
    private final String sqlConsultarComprobante;
    private final String sqlConsultarLogoCorredora;
    private final String sqlConsultarRaizArchivo;
    private final String sqlConsultarTemplate;
    private final String sqlInsertPortafolioTablaPaso;
    private final String sqlDeletePortafolioTablaPaso;

    public GenerarInformePrefacturasService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlConsultarPrefacturas        = loadSql("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarPrefacturas.sql");
        this.sqlConsultarPrefacturasTodas   = loadSql("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarPrefacturasTodas.sql");
        this.sqlConsultarPrefacturasFAC     = loadSql("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarPrefacturasFAC.sql");
        this.sqlConsultarPrefacturasFACTodas= loadSql("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarPrefacturasFACTodas.sql");
        this.sqlConsultarComprobante        = loadSql("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarComprobante.sql");
        this.sqlConsultarLogoCorredora      = loadSql("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarLogoCorredora.sql");
        this.sqlConsultarRaizArchivo        = loadSql("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarRaizArchivo.sql");
        this.sqlConsultarTemplate           = loadSql("cl/bcs/facturacion/sql/GenerarInformePrefacturas_ConsultarTemplate.sql");
        this.sqlInsertPortafolioTablaPaso   = loadSql("cl/bcs/facturacion/sql/GenerarInformePrefacturas_InsertPortafolioTablaPaso.sql");
        this.sqlDeletePortafolioTablaPaso   = loadSql("cl/bcs/facturacion/sql/GenerarInformePrefacturas_DeletePortafolioTablaPaso.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::GenerarInformePrefacturas");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",  getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",  getL(params, "p_IdUsuario"))
            .addValue("IdModulo",   getS(params, "p_IdModulo"))
            .addValue("FechaDesde", getS(params, "p_FechaDesde"))
            .addValue("FechaHasta", getS(params, "p_FechaHasta"))
            .addValue("IdPortafolio",getL(params, "p_IdPortafolio"))
            .addValue("Todos",      getS(params, "p_Todos", "N"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            jdbc.update(sqlDeletePortafolioTablaPaso, p);
            jdbc.update(sqlInsertPortafolioTablaPaso, p);

            jdbc.queryForList(sqlConsultarLogoCorredora, p);
            jdbc.queryForList(sqlConsultarRaizArchivo, p);
            jdbc.queryForList(sqlConsultarTemplate, p);

            List<Map<String, Object>> prefacturas = "S".equals(getS(params, "p_Todos", "N"))
                ? jdbc.queryForList(sqlConsultarPrefacturasTodas, p)
                : jdbc.queryForList(sqlConsultarPrefacturas, p);

            List<Map<String, Object>> prefacturasFAC = "S".equals(getS(params, "p_Todos", "N"))
                ? jdbc.queryForList(sqlConsultarPrefacturasFACTodas, p)
                : jdbc.queryForList(sqlConsultarPrefacturasFAC, p);

            jdbc.queryForList(sqlConsultarComprobante, p);

            // TODO: generate PDF report with Crystal Reports or equivalent

            JsonArray pfArr = new JsonArray();
            prefacturas.forEach(r -> pfArr.add(GSON.toJsonTree(r)));
            out.add("P_OUT_PREFACTURAS", pfArr);
            out.addProperty("P_OUT_TOTAL_FAC", prefacturasFAC.size());

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in GenerarInformePrefacturas", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in GenerarInformePrefacturas", ex);
            addError(out, "ERROR_GENERAR_INFORME_PREFACTURAS", ex.getMessage());
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
        try (InputStream is = GenerarInformePrefacturasService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
