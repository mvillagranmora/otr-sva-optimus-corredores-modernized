package cl.bcs.facturacion.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class ConsultarCobrosClientesConDiferenciaService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCobrosClientesConDiferenciaService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectComprobantes;
    private final String sqlSelectComprobanteCobro;

    public ConsultarCobrosClientesConDiferenciaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectComprobantes   = loadSql(
            "cl/bcs/facturacion/sql/ConsultarCobrosClientesConDiferencia_SelectComprobantes.sql");
        this.sqlSelectComprobanteCobro = loadSql(
            "cl/bcs/facturacion/sql/ConsultarCobrosClientesConDiferencia_SelectComprobanteCobro.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ConsultarCobrosClientesConDiferencia");

        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("IdEmpresa",   getL(params, "p_IdEmpresa"))
            .addValue("FechaDesde",  getS(params, "p_FechaDesde"))
            .addValue("FechaHasta",  getS(params, "p_FechaHasta"))
            .addValue("IdModulo",    getS(params, "p_IdModulo"));

        List<Map<String, Object>> comprobantes = jdbc.queryForList(sqlSelectComprobantes, p);
        List<Map<String, Object>> cobros = jdbc.queryForList(sqlSelectComprobanteCobro, p);

        // TODO: cross-domain call to ConsultarCobrosPortafolio (BcsFacturacionServicios internal)
        // via RestClient at ${bcs.facturacion.url}/internal/java-call

        JsonArray comprobantesArr = new JsonArray();
        comprobantes.forEach(r -> comprobantesArr.add(GSON.toJsonTree(r)));

        JsonArray cobrosArr = new JsonArray();
        cobros.forEach(r -> cobrosArr.add(GSON.toJsonTree(r)));

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());
        out.add("P_OUT_COMPROBANTES", comprobantesArr);
        out.add("P_OUT_COBROS", cobrosArr);
        return out;
    }

    private static Long getL(JsonObject p, String key) {
        return p.has(key) && !p.get(key).isJsonNull() ? p.get(key).getAsLong() : null;
    }

    private static String getS(JsonObject p, String key) {
        return p.has(key) && !p.get(key).isJsonNull() ? p.get(key).getAsString() : null;
    }

    private static String loadSql(String path) {
        try (InputStream is = ConsultarCobrosClientesConDiferenciaService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
