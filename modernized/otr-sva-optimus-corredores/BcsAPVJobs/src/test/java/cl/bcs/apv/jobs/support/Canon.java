package cl.bcs.apv.jobs.support;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * Canonical JSON used to compare the legacy goldens with the modern output.
 * <ul>
 *   <li>Object keys are order-insensitive; keys whose value is JSON null are dropped (the legacy JavaCall output uses
 *       serializeNulls, consumers treat an absent key and a null alike).</li>
 *   <li>Numbers are compared by value ({@code 1500.50 == 1500.5000000000000000}): the H2 column scale is not legacy behaviour.</li>
 *   <li>The JDK helpful NullPointerException text inside any string is replaced by {@code <NPE-MSG>} on both sides ({@link Dump#NPE_MSG}).</li>
 *   <li>Arrays keep their order, except under the keys passed as {@code sinOrden} (legacy SQL without ORDER BY).</li>
 * </ul>
 */
public final class Canon {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private Canon() {
    }

    public static JsonElement canon(JsonElement e, Set<String> sinOrden) {
        return canon(e, sinOrden, false);
    }

    private static JsonElement canon(JsonElement e, Set<String> sinOrden, boolean desordenar) {
        if (e == null || e.isJsonNull()) {
            return JsonNull.INSTANCE;
        }
        if (e.isJsonPrimitive()) {
            JsonPrimitive p = e.getAsJsonPrimitive();
            if (p.isNumber()) {
                BigDecimal bd = new BigDecimal(p.getAsString()).stripTrailingZeros();
                return new JsonPrimitive(new BigDecimal(bd.toPlainString()));
            }
            if (p.isString()) {
                return new JsonPrimitive(Dump.sinMensajeNpe(p.getAsString()));
            }
            return p;
        }
        if (e.isJsonArray()) {
            List<JsonElement> items = new ArrayList<>();
            for (JsonElement x : e.getAsJsonArray()) {
                items.add(canon(x, sinOrden, false));
            }
            if (desordenar) {
                items.sort(Comparator.comparing(JsonElement::toString));
            }
            JsonArray a = new JsonArray();
            items.forEach(a::add);
            return a;
        }
        TreeMap<String, JsonElement> sorted = new TreeMap<>();
        for (Map.Entry<String, JsonElement> en : e.getAsJsonObject().entrySet()) {
            JsonElement v = canon(en.getValue(), sinOrden, sinOrden.contains(en.getKey()));
            if (!v.isJsonNull()) {
                sorted.put(en.getKey(), v);
            }
        }
        JsonObject o = new JsonObject();
        sorted.forEach(o::add);
        return o;
    }

    public static String texto(JsonElement e, String... sinOrden) {
        return PRETTY.toJson(canon(e, Set.of(sinOrden)));
    }

    public static JsonElement parse(String json) {
        return JsonParser.parseString(json);
    }
}
