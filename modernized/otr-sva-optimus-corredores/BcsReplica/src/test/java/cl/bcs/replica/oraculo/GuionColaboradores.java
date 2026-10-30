package cl.bcs.replica.oraculo;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Runtime of the collaborator doubles of the BcsReplica oracle AND of its JUnit tests (one source file, byte for byte in both places): every call to a collaborator that is
 * not part of the module (Plataforma ObtenerDiaSiguiente / ConsultarEmpresa / Feriados / ConfiguracionSistema, BcsUtils Utils.getFechaSistema / getSecuencia) is RECORDED
 * (collaborator, method, arguments as JSON without the connection, nulls dropped) and answered from the case script:
 * <pre>
 * "colaboradores": { "Feriados.esDiaHabil": { "defecto": true,
 *      "respuestas": [ { "cuando": {"0": "2026-03-07"}, "respuesta": false, "una_vez": false }, { "error": "text" } ] } }
 * </pre>
 * The key is "Collaborator.method". "cuando" paths are dotted and start at the index of the argument (connection excluded): "0.p_fechas.0.fechaInicial". The first matching
 * entry wins; "error" makes the collaborator throw a plain Exception; with no matching entry and no "defecto" the call fails loudly (a case must script what it reaches).
 * Java 11 syntax on purpose (the oracle compiles with --release 11).
 */
public final class GuionColaboradores {
    private final JsonObject config;
    private final JsonArray llamadas = new JsonArray();
    private final Set<JsonObject> usados = Collections.newSetFromMap(new IdentityHashMap<JsonObject, Boolean>());

    public GuionColaboradores(JsonObject config) {
        this.config = config == null ? new JsonObject() : config;
    }

    public JsonArray llamadas() {
        return llamadas;
    }

    /** Records the call and answers it; throws the scripted error. The answer is a JsonElement (primitive, object, array) or JsonNull. */
    public JsonElement responder(String colaborador, String metodo, JsonArray argumentos) throws Exception {
        JsonObject rec = new JsonObject();
        rec.addProperty("colaborador", colaborador);
        rec.addProperty("metodo", metodo);
        JsonArray args = new JsonArray();
        for (JsonElement a : argumentos) args.add(sinNulos(a));
        rec.add("argumentos", args);
        llamadas.add(rec);
        JsonObject c = config.has(colaborador + "." + metodo) && config.get(colaborador + "." + metodo).isJsonObject() ? config.getAsJsonObject(colaborador + "." + metodo) : null;
        JsonObject hit = null;
        if (c != null && c.has("respuestas")) {
            for (JsonElement e : c.getAsJsonArray("respuestas")) {
                JsonObject r = e.getAsJsonObject();
                if (r.has("una_vez") && r.get("una_vez").getAsBoolean() && usados.contains(r)) continue;
                if (coincide(r.has("cuando") ? r.getAsJsonObject("cuando") : null, args)) {
                    hit = r;
                    break;
                }
            }
        }
        if (hit != null) {
            usados.add(hit);
            if (hit.has("error")) {
                rec.addProperty("respuesta", "error: " + hit.get("error").getAsString());
                throw new Exception(hit.get("error").getAsString());
            }
            JsonElement resp = hit.has("respuesta") ? hit.get("respuesta") : JsonNull.INSTANCE;
            rec.add("respuesta", sinNulos(resp));
            return resp;
        }
        if (c != null && c.has("defecto")) {
            rec.add("respuesta", sinNulos(c.get("defecto")));
            return c.get("defecto");
        }
        rec.addProperty("respuesta", "SIN RESPUESTA CONFIGURADA");
        throw new IllegalStateException("collaborator double " + colaborador + "." + metodo + ": no canned answer configured for this call");
    }

    /** Copy of the element without the members that are JSON null (an absent field and a null field are the same argument). */
    public static JsonElement sinNulos(JsonElement e) {
        if (e == null || e.isJsonNull()) return JsonNull.INSTANCE;
        if (e.isJsonObject()) {
            JsonObject o = new JsonObject();
            for (Map.Entry<String, JsonElement> m : e.getAsJsonObject().entrySet()) {
                if (!m.getValue().isJsonNull()) o.add(m.getKey(), sinNulos(m.getValue()));
            }
            return o;
        }
        if (e.isJsonArray()) {
            JsonArray a = new JsonArray();
            for (JsonElement x : e.getAsJsonArray()) a.add(sinNulos(x));
            return a;
        }
        return e;
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
                BigDecimal a = numero(cur.getAsString());
                BigDecimal b = numero(want.getAsString());
                if (a != null && b != null ? a.compareTo(b) != 0 : !cur.getAsString().equals(want.getAsString())) return false;
            }
        }
        return true;
    }

    private static BigDecimal numero(String s) {
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
