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
public class GenerarInformeIGKService {

    private static final Logger log = LoggerFactory.getLogger(GenerarInformeIGKService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlInformeIGKCall;
    private final String sqlSelectGrupoClientes;
    private final String sqlSelectImagenFirma;
    private final String sqlSelectLogoCorredora;
    private final String sqlSelectRaizArchivo;
    private final String sqlSelectReporte;

    public GenerarInformeIGKService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlInformeIGKCall    = loadSql("cl/bcs/facturacion/sql/GenerarInformeIGK_InformeIGK_Call.sql");
        this.sqlSelectGrupoClientes= loadSql("cl/bcs/facturacion/sql/GenerarInformeIGK_SelectGrupoClientes.sql");
        this.sqlSelectImagenFirma  = loadSql("cl/bcs/facturacion/sql/GenerarInformeIGK_SelectImagenFirma.sql");
        this.sqlSelectLogoCorredora= loadSql("cl/bcs/facturacion/sql/GenerarInformeIGK_SelectLogoCorredora.sql");
        this.sqlSelectRaizArchivo  = loadSql("cl/bcs/facturacion/sql/GenerarInformeIGK_SelectRaizArchivo.sql");
        this.sqlSelectReporte      = loadSql("cl/bcs/facturacion/sql/GenerarInformeIGK_SelectReporte.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::GenerarInformeIGK");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",  getL(params, "p_IdEmpresa"))
            .addValue("IdUsuario",  getL(params, "p_IdUsuario"))
            .addValue("IdModulo",   getS(params, "p_IdModulo"))
            .addValue("FechaDesde", getS(params, "p_FechaDesde"))
            .addValue("FechaHasta", getS(params, "p_FechaHasta"))
            .addValue("IdCliente",  getL(params, "p_IdCliente"));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());

        try {
            jdbc.queryForList(sqlSelectLogoCorredora, p);
            jdbc.queryForList(sqlSelectImagenFirma, p);
            jdbc.queryForList(sqlSelectRaizArchivo, p);
            List<Map<String, Object>> grupoClientes = jdbc.queryForList(sqlSelectGrupoClientes, p);
            List<Map<String, Object>> reporte       = jdbc.queryForList(sqlSelectReporte, p);
            jdbc.update(sqlInformeIGKCall, p); // CALL stored procedure for IGK

            // TODO: generate PDF report with Crystal Reports or equivalent

            JsonArray repArr = new JsonArray();
            reporte.forEach(r -> repArr.add(GSON.toJsonTree(r)));
            out.add("P_OUT_REPORTE", repArr);
            out.addProperty("P_OUT_TOTAL_CLIENTES", grupoClientes.size());

        } catch (CannotGetJdbcConnectionException ex) {
            log.error("HANA connection error in GenerarInformeIGK", ex);
            addError(out, "HANA_CONNECTION_ERROR", ex.getMessage());
        } catch (Exception ex) {
            log.error("Error in GenerarInformeIGK", ex);
            addError(out, "ERROR_GENERAR_INFORME_IGK", ex.getMessage());
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

    private static String loadSql(String path) {
        try (InputStream is = GenerarInformeIGKService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
