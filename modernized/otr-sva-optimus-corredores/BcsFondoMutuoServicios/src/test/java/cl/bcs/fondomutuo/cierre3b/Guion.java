package cl.bcs.fondomutuo.cierre3b;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * The scripted collaborators of a case, with the SAME semantics as the oracle's {@code cl.bcs.oracle.Doubles} (answers from
 * seed/doubles-base.json overridden by the case's "colaboradores": "respuestas" with "cuando" dotted paths and "una_vez", else "defecto", else the
 * call fails; every call is recorded as {colaborador, argumentos, respuesta}). The ports of the new code use it through {@link #responder}.
 */
public final class Guion {

    private final JsonObject config;
    private final JsonArray llamadas = new JsonArray();
    private final Set<JsonObject> usadas = new HashSet<>();

    public Guion(JsonObject config) {
        this.config = config;
    }

    public JsonArray llamadas() {
        return llamadas;
    }

    public JsonObject responder(String colaborador, JsonElement argumento) {
        JsonArray args = new JsonArray();
        args.add(argumento);
        JsonObject rec = new JsonObject();
        rec.addProperty("colaborador", colaborador);
        rec.add("argumentos", args);
        llamadas.add(rec);
        JsonObject c = config.has(colaborador) && config.get(colaborador).isJsonObject() ? config.getAsJsonObject(colaborador) : null;
        JsonObject hit = null;
        if (c != null && c.has("respuestas")) {
            for (JsonElement e : c.getAsJsonArray("respuestas")) {
                JsonObject r = e.getAsJsonObject();
                if (r.has("una_vez") && r.get("una_vez").getAsBoolean() && usadas.contains(r)) continue;
                if (coincide(r.has("cuando") ? r.getAsJsonObject("cuando") : null, args)) {
                    hit = r;
                    break;
                }
            }
        }
        if (hit != null) {
            usadas.add(hit);
            if (hit.has("error")) {
                rec.addProperty("respuesta", "error: " + hit.get("error").getAsString());
                throw new RuntimeException(hit.get("error").getAsString());
            }
            rec.add("respuesta", hit.get("respuesta"));
            return hit.get("respuesta").isJsonNull() ? null : hit.get("respuesta").getAsJsonObject().deepCopy();
        }
        if (c != null && c.has("defecto")) {
            rec.add("respuesta", c.get("defecto"));
            return c.get("defecto").isJsonNull() ? null : c.get("defecto").getAsJsonObject().deepCopy();
        }
        rec.addProperty("respuesta", "SIN RESPUESTA CONFIGURADA");
        throw new IllegalStateException("scripted collaborator " + colaborador + ": no canned answer for this call");
    }

    private static boolean coincide(JsonObject cuando, JsonArray args) {
        if (cuando == null) return true;
        for (String path : cuando.keySet()) {
            JsonElement cur = args;
            for (String p : path.split("\\.")) {
                if (cur == null || cur.isJsonNull()) {
                    cur = null;
                    break;
                }
                if (cur.isJsonArray()) {
                    int i = Integer.parseInt(p);
                    cur = i < cur.getAsJsonArray().size() ? cur.getAsJsonArray().get(i) : null;
                } else if (cur.isJsonObject()) {
                    cur = cur.getAsJsonObject().get(p);
                } else {
                    cur = null;
                }
            }
            JsonElement want = cuando.get(path);
            boolean wantNull = want == null || want.isJsonNull();
            boolean gotNull = cur == null || cur.isJsonNull();
            if (wantNull != gotNull) return false;
            if (!wantNull) {
                if (!(cur instanceof JsonPrimitive) || !(want instanceof JsonPrimitive)) return false;
                BigDecimal a = num(cur.getAsString());
                BigDecimal b = num(want.getAsString());
                if (a != null && b != null ? a.compareTo(b) != 0 : !cur.getAsString().equals(want.getAsString())) return false;
            }
        }
        return true;
    }

    private static BigDecimal num(String s) {
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
