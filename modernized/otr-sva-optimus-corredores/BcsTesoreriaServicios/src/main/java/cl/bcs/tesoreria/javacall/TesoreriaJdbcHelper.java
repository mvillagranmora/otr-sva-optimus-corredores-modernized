package cl.bcs.tesoreria.javacall;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Shared utilities for Tesorería JavaCall Spring services.
 */
public final class TesoreriaJdbcHelper {

    static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            .serializeSpecialFloatingPointValues()
            .create();

    private TesoreriaJdbcHelper() {}

    public static String loadSql(String classpathPath) {
        try {
            var res = new ClassPathResource(classpathPath);
            return new String(res.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load SQL: " + classpathPath, e);
        }
    }

    public static JsonArray toJsonArray(List<Map<String, Object>> rows) {
        JsonArray arr = new JsonArray();
        for (Map<String, Object> row : rows) {
            arr.add(JsonParser.parseString(GSON.toJson(row)));
        }
        return arr;
    }

    public static JsonObject toJsonObject(Object obj) {
        return JsonParser.parseString(GSON.toJson(obj)).getAsJsonObject();
    }

    public static JsonArray queryToArray(NamedParameterJdbcTemplate jdbc, String sql, MapSqlParameterSource params) {
        return toJsonArray(jdbc.queryForList(sql, params));
    }

    /** Standard success output with no data rows. */
    public static JsonObject okEmpty() {
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.add("P_OUT_MENSAJES", new JsonArray());
        return out;
    }

    /** Standard success output with a result list. */
    public static JsonObject okWithResult(String resultKey, JsonArray data) {
        JsonObject out = okEmpty();
        out.add(resultKey, data);
        return out;
    }

    public static JsonObject errorOutput(String code, String msg) {
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
        JsonArray mensajes = new JsonArray();
        JsonObject m = new JsonObject();
        m.addProperty("tipo", "ERROR");
        m.addProperty("codigo", code);
        m.addProperty("descripcion", msg);
        mensajes.add(m);
        out.add("P_OUT_MENSAJES", mensajes);
        return out;
    }

    public static String str(JsonObject params, String key) {
        JsonElement el = params.get(key);
        return (el == null || el.isJsonNull()) ? "" : el.getAsString();
    }

    public static long lng(JsonObject params, String key) {
        JsonElement el = params.get(key);
        return (el == null || el.isJsonNull()) ? 0L : el.getAsLong();
    }

    public static boolean has(JsonObject params, String key) {
        return params.has(key) && !params.get(key).isJsonNull();
    }
}
