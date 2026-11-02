package cl.bcs.fondomutuo.aportereinversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * How a run of the new code is compared with the golden RECORDED from the unmodified legacy (the same rules as the Normal and RescateReinversion suites): the output text (key order
 * and the scale of every BigDecimal count), the tables written, every collaborator call (name, COMPLETE arguments, answer, order), the sequences asked, the session and the decision of
 * {@code CallServlet}; an exception by CLASS (the message only for a plain {@code java.lang.Exception}). One section at a time, so that the 17 MB goldens of the lots of 2.000 do not
 * need two copies in memory.
 */
final class CsrCostura {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    private CsrCostura() {
    }

    static void comparar(String id, JsonObject expected, JsonObject actual) {
        for (String clave : new String[] { "decisionServlet", "salidaCruda", "tablas", "colaboradores", "secuenciasSolicitadas", "sesion" }) {
            assertEquals(texto(expected.get(clave)), texto(actual.get(clave)), id + " :: " + clave);
        }
        assertEquals(expected.has("excepcion"), actual.has("excepcion"), id + " :: exception presence: " + actual.get("excepcion"));
        if (expected.has("excepcion")) {
            String e = expected.get("excepcion").getAsString();
            String a = actual.get("excepcion").getAsString();
            String ec = e.substring(0, e.indexOf(':'));
            String ac = a.substring(0, a.indexOf(':'));
            if (ec.equals("Exception")) {
                assertEquals(e.substring(e.indexOf(':') + 1), a.substring(a.indexOf(':') + 1), id + " :: exception message");
            } else {
                assertEquals(ec, ac, id + " :: exception class");
            }
        }
        assertTrue(!actual.has("compuerta"), id + " :: a recorded case never meets a closed gate (profile ABIERTAS)");
    }

    static String texto(JsonElement e) {
        if (e == null) return "<absent>";
        if (e.isJsonArray() && !e.getAsJsonArray().isEmpty() && e.getAsJsonArray().get(0).isJsonObject() && e.getAsJsonArray().get(0).getAsJsonObject().has("colaborador")) {
            JsonArray n = new JsonArray();
            for (JsonElement x : e.getAsJsonArray()) {
                JsonObject o = x.getAsJsonObject();
                JsonObject m = new JsonObject();
                m.add("colaborador", o.get("colaborador"));
                m.add("argumentos", o.get("argumentos"));
                m.add("respuesta", o.get("respuesta"));
                n.add(m);
            }
            return PRETTY.toJson(canon(n));
        }
        if (e.isJsonPrimitive()) return e.getAsString();
        return PRETTY.toJson(canon(e));
    }

    /** The keys of an object in alphabetical order (the Gson of a Java class and of a database dump do not share key order; the lists keep theirs). */
    static JsonElement canon(JsonElement e) {
        if (e == null || e.isJsonNull()) return JsonNull.INSTANCE;
        if (e.isJsonArray()) {
            JsonArray a = new JsonArray();
            for (JsonElement x : e.getAsJsonArray()) a.add(canon(x));
            return a;
        }
        if (e.isJsonObject()) {
            TreeMap<String, JsonElement> m = new TreeMap<>();
            for (String k : e.getAsJsonObject().keySet()) m.put(k, canon(e.getAsJsonObject().get(k)));
            JsonObject o = new JsonObject();
            m.forEach(o::add);
            return o;
        }
        return e;
    }
}
