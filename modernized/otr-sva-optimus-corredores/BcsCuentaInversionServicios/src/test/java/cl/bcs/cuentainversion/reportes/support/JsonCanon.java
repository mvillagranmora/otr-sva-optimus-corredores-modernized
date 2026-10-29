package cl.bcs.cuentainversion.reportes.support;

import cl.bcs.cuentainversion.support.VolcadoEquivalencia;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Canonical JSON comparison used against the legacy goldens.
 *
 * <ul>
 *   <li>Object keys are compared order-insensitively; keys whose value is {@code null} are dropped (legacy JavaCall output
 *       uses serializeNulls; a consumer cannot tell an absent key from a null one).</li>
 *   <li>Numbers are compared by value ({@code 1500.50 == 1500.5000000000000000}); the H2 column scale is not a legacy behaviour.
 *       Where the legacy rounds on purpose (setScale(0, DOWN), setScale(2, HALF_EVEN)) the rounded VALUE is what is compared.</li>
 *   <li>Arrays keep their order, except under the keys passed as {@code sinOrden}.</li>
 * </ul>
 */
public final class JsonCanon {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private JsonCanon() {
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

    /** Asserts canonical equality and prints both sides on failure. */
    public static void assertEquivalente(String que, JsonElement esperado, JsonElement actual, String... sinOrden) {
        Set<String> s = Set.of(sinOrden);
        VolcadoEquivalencia.escribir("reportes", que, "canon", PRETTY.toJson(canon(esperado, s)), PRETTY.toJson(canon(actual, s)));
        assertThat(PRETTY.toJson(canon(actual, s))).as(que + " (legacy golden vs modern, canonical JSON)")
                .isEqualTo(PRETTY.toJson(canon(esperado, s)));
    }

    public static JsonElement parse(String json) {
        return JsonParser.parseString(json);
    }

    public static String pretty(JsonElement e) {
        return PRETTY.toJson(e);
    }
}
