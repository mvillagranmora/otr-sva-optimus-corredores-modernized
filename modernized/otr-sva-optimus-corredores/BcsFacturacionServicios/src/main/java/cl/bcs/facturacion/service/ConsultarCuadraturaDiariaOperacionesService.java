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

/**
 * Migrated from legacy cl.bcs.facturacion.ConsultarCuadraturaDiariaOperaciones (JavaCall).
 * Returns a summary of daily operation quadrature results for a given date and module.
 */
@Service
public class ConsultarCuadraturaDiariaOperacionesService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarCuadraturaDiariaOperacionesService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectCuadraturas;

    public ConsultarCuadraturaDiariaOperacionesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlSelectCuadraturas = loadSql("cl/bcs/facturacion/sql/ConsultarCuadraturaDiariaOperaciones_SelectCuadraturas.sql");
    }

    /**
     * @param params JsonObject with: p_IdEmpresa (Long), p_IdModuloCuadratura (String), p_FechaEjecucion (String)
     * @return JsonObject with: P_OUT_ERRORESCONTADOR (Long=0), P_OUT_MENSAJES ([]), P_OUT_RESUMEN ([...rows])
     */
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ConsultarCuadraturaDiariaOperaciones");

        Long idEmpresa       = params.has("p_IdEmpresa")       ? params.get("p_IdEmpresa").getAsLong()       : null;
        String moduloCuad    = params.has("p_IdModuloCuadratura") ? params.get("p_IdModuloCuadratura").getAsString() : null;
        String fechaEjecucion= params.has("p_FechaEjecucion")  ? params.get("p_FechaEjecucion").getAsString() : null;

        MapSqlParameterSource sqlParams = new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("IdModuloCuadratura", moduloCuad)
                .addValue("FechaEjecucion", fechaEjecucion);

        List<Map<String, Object>> rows = jdbc.queryForList(sqlSelectCuadraturas, sqlParams);

        Gson gson = new Gson();
        JsonArray resumen = gson.toJsonTree(rows).getAsJsonArray();

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        out.add("P_OUT_MENSAJES", new JsonArray());
        out.add("P_OUT_RESUMEN", resumen);

        log.info("FIN::ConsultarCuadraturaDiariaOperaciones");
        return out;
    }

    private static String loadSql(String resourcePath) {
        try (InputStream is = ConsultarCuadraturaDiariaOperacionesService.class
                .getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + resourcePath);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + resourcePath, e);
        }
    }
}
