package cl.bcs.oracle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ORACLE INFRASTRUCTURE (not a legacy class). Holds the canned answers a case gives to the collaborators that live
 * in other legacy modules or in other legacy classes pinned elsewhere (ConsultarEmpresa, CalcularParidad,
 * ConsultarSaldoCuentaInversion, CrearMovimientoComprobante, EntregarPreciosTasas), and the log of every call the
 * legacy class made to them. The log is recorded in expected.json under "llamadasColaboradores" so the modern
 * implementation is held to the same collaborator contract.
 */
public final class Colaboradores {
    /** case.json "colaboradores" */
    public static JsonObject CONFIG = new JsonObject();
    /** {"servicio": ..., "entrada": ...} per call, in call order */
    public static final List<JsonObject> LLAMADAS = new ArrayList<>();
    /** per service: index of the next canned answer */
    public static final Map<String, Integer> INDICE = new HashMap<>();

    private Colaboradores() {
    }

    public static void reset(JsonObject config) {
        CONFIG = config == null ? new JsonObject() : config;
        LLAMADAS.clear();
        INDICE.clear();
    }

    public static void registrar(String servicio, Object entrada) {
        JsonObject o = new JsonObject();
        o.addProperty("servicio", servicio);
        o.add("entrada", cl.bcs.cuentainversion.reportes.support.Mask.llamada(new com.google.gson.GsonBuilder().serializeNulls().create().toJsonTree(entrada)));
        LLAMADAS.add(o);
    }

    /** Next canned answer of an array configured under CONFIG[servicio]; the last answer repeats; null when none. */
    public static JsonElement siguiente(String servicio) {
        if (!CONFIG.has(servicio)) return null;
        JsonElement e = CONFIG.get(servicio);
        if (!e.isJsonArray()) return e;
        JsonArray a = e.getAsJsonArray();
        if (a.size() == 0) return null;
        int i = INDICE.getOrDefault(servicio, 0);
        INDICE.put(servicio, i + 1);
        return a.get(Math.min(i, a.size() - 1));
    }

    /**
     * CONFIG[clave] (a JSON array of flat objects) as a list of maps: numbers become BigDecimal (the legacy casts them), texts stay Strings,
     * booleans Booleans, nulls null. Absent = empty list.
     */
    public static List<HashMap<String, Object>> listaMapas(String clave) {
        List<HashMap<String, Object>> out = new ArrayList<>();
        JsonElement e = CONFIG.get(clave);
        if (e == null || !e.isJsonArray()) return out;
        for (JsonElement it : e.getAsJsonArray()) {
            HashMap<String, Object> m = new HashMap<>();
            for (Map.Entry<String, JsonElement> en : it.getAsJsonObject().entrySet()) {
                JsonElement v = en.getValue();
                if (v.isJsonNull()) m.put(en.getKey(), null);
                else if (v.getAsJsonPrimitive().isNumber()) m.put(en.getKey(), v.getAsBigDecimal());
                else if (v.getAsJsonPrimitive().isBoolean()) m.put(en.getKey(), v.getAsBoolean());
                else m.put(en.getKey(), v.getAsString());
            }
            out.add(m);
        }
        return out;
    }
}
