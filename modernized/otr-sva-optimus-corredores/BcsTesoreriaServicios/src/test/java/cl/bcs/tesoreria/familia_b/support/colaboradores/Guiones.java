package cl.bcs.tesoreria.familia_b.support.colaboradores;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** Helpers shared by the scripted collaborator doubles (the oracle Hooks of the legacy-oracle shims). */
public final class Guiones {

    private Guiones() {
    }

    /** colaboradores.&lt;clave&gt; as an object (empty when absent). */
    public static JsonObject objeto(JsonObject guiones, String clave) {
        JsonElement e = guiones.get(clave);
        return e == null || e.isJsonNull() ? new JsonObject() : e.getAsJsonObject();
    }

    /** colaboradores.&lt;clave&gt; as an array (empty when absent). */
    public static JsonArray lista(JsonObject guiones, String clave) {
        JsonElement e = guiones.get(clave);
        return e == null || e.isJsonNull() ? new JsonArray() : e.getAsJsonArray();
    }

    /** A scripted message {id,tipo,codigo,descripcion} (oracle Hooks.mensaje: id and descripcion may be absent, tipo and codigo may not). */
    public static JsonObject mensaje(JsonObject o) {
        JsonObject m = new JsonObject();
        m.add("id", o.has("id") && !o.get("id").isJsonNull() ? o.get("id") : com.google.gson.JsonNull.INSTANCE);
        m.addProperty("tipo", o.get("tipo").getAsString());
        m.addProperty("codigo", o.get("codigo").getAsString());
        m.add("descripcion", o.has("descripcion") && !o.get("descripcion").isJsonNull() ? o.get("descripcion") : com.google.gson.JsonNull.INSTANCE);
        return m;
    }

    public static JsonArray mensajes(JsonObject script) {
        JsonArray out = new JsonArray();
        if (script.has("mensajes")) {
            for (JsonElement e : script.getAsJsonArray("mensajes")) {
                out.add(mensaje(e.getAsJsonObject()));
            }
        }
        return out;
    }

    public static long contarErrores(JsonArray mensajes) {
        long n = 0;
        for (JsonElement e : mensajes) {
            if ("ERROR".equals(e.getAsJsonObject().get("tipo").getAsString())) {
                n++;
            }
        }
        return n;
    }

    /**
     * Masks NumeroReferencia / NumeroReferenciaFormaDePago by order of first appearance (1, 2, 3 ...): oracle Hooks.enmascararReferencias
     * (the legacy ProcesarClearingFinanciero draws them from java.util.Random). Works on a copy.
     */
    public static JsonElement enmascararReferencias(JsonElement original) {
        JsonElement e = original.deepCopy();
        masc(e, new HashMap<>());
        return e;
    }

    private static void masc(JsonElement e, Map<String, Integer> orden) {
        if (e == null || e.isJsonNull()) {
            return;
        }
        if (e.isJsonArray()) {
            for (JsonElement x : e.getAsJsonArray()) {
                masc(x, orden);
            }
        } else if (e.isJsonObject()) {
            JsonObject o = e.getAsJsonObject();
            for (String k : new java.util.ArrayList<>(o.keySet())) {
                JsonElement v = o.get(k);
                if ((k.equals("NumeroReferencia") || k.equals("NumeroReferenciaFormaDePago")) && v.isJsonPrimitive() && v.getAsJsonPrimitive().isNumber()) {
                    String raw = v.getAsString();
                    orden.putIfAbsent(raw, orden.size() + 1);
                    o.addProperty(k, orden.get(raw));
                } else {
                    masc(v, orden);
                }
            }
        }
    }
}
