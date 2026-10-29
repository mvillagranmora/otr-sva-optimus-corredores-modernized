package cl.bcs.oracle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * ORACLE SUPPORT (not a legacy class): the scripted answers and the call trace of the collaborators that live OUTSIDE the APV module
 * (Socio de Negocio, Plataforma, Tesoreria, Cuenta Inversion ... and the HANA stored procedures reached through SQL.call).
 *
 * <p>case.json {@code "colaboradores"} holds, per collaborator simple name, the script
 * {@code {"salidas": [out1, out2, ...]}} (the k-th call answers {@code salidas[k]}, the last one repeats), or {@code {"salida": out}} (every call),
 * or {@code {"lanza": "text"}} (every call throws {@code new Exception(text)}). {@code out} is the JSON of the legacy output object and must be
 * COMPLETE: the fake deserialises it into the legacy class and refuses a script that leaves a field to a Java default (so the modern double,
 * which answers the script verbatim, hands the modern code exactly what the legacy code received).
 * Stored procedures use the collaborator name {@code StoredProcedure} and one script per procedure name:
 * {@code "StoredProcedure": {"<BSCL.Portafolio.BD.Servicios::CrearPortafolio>": {"salidas": [...]}}}.
 * Every call is recorded as {@code {servicio, entrada}} in {@link #LLAMADAS}; the runner writes them to expected.json ("colaboradores").
 */
public final class Hooks {
    public static JsonObject colaboradores = new JsonObject();
    public static final List<JsonObject> LLAMADAS = new ArrayList<>();
    private static final Map<String, Integer> CONTADORES = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
    private static final Gson GSON_ENTRADA = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();

    /**
     * Answers used when the case does not script the collaborator (the two services nearly every APV class calls and whose answer is "nothing to report").
     * The modern test double (support/ColaboradoresDeCaso) holds the SAME table.
     */
    private static final Map<String, String> POR_DEFECTO = Map.of(
            "AgregarLog", "{\"P_OUT_ERRORESCONTADOR\":0}",
            "CrearEventoAutorizacionMasivo", "{\"P_OUT_IDEVENTO\":[],\"P_OUT_ADVERTENCIASCONTADOR\":0,\"P_OUT_ADVERTENCIASDETALLE\":[],\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_ERRORESDETALLE\":[]}");

    private Hooks() {
    }

    public static void reset(JsonObject c) {
        colaboradores = c == null ? new JsonObject() : c;
        LLAMADAS.clear();
        CONTADORES.clear();
    }

    public static void registrar(String servicio, Object entrada) {
        JsonObject o = new JsonObject();
        o.addProperty("servicio", servicio);
        o.add("entrada", GSON.toJsonTree(entrada));
        LLAMADAS.add(o);
    }

    /** Scripted answer of the k-th call of {@code servicio} (script = colaboradores.servicio): throws the scripted exception, or fails when no script exists. */
    public static JsonObject respuesta(String servicio, JsonObject guion) throws Exception {
        int k = CONTADORES.merge(servicio, 1, Integer::sum) - 1;
        if ((guion == null || guion.size() == 0) && POR_DEFECTO.containsKey(servicio)) {
            return parse(POR_DEFECTO.get(servicio));
        }
        if (guion == null || guion.size() == 0) {
            throw new IllegalStateException("case.json has no script colaboradores." + servicio + " (a collaborator outside APV answers only what the case scripts)");
        }
        if (guion.has("lanza")) {
            throw new Exception(guion.get("lanza").getAsString());
        }
        if (guion.has("salidas")) {
            JsonArray a = guion.getAsJsonArray("salidas");
            return a.get(Math.min(k, a.size() - 1)).getAsJsonObject();
        }
        if (guion.has("salida")) {
            return guion.getAsJsonObject("salida");
        }
        throw new IllegalStateException("script colaboradores." + servicio + " needs salida, salidas or lanza");
    }

    /** The legacy output object of collaborator {@code servicio}, built from its script; the script must list every field the legacy object serialises. */
    public static <T> T salida(String servicio, Class<T> tipo) throws Exception {
        JsonObject s = respuesta(servicio, objeto(servicio));
        T obj = GSON_ENTRADA.fromJson(s, tipo);
        JsonElement completo = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create().toJsonTree(obj);
        for (Map.Entry<String, JsonElement> e : completo.getAsJsonObject().entrySet()) {
            if (!e.getValue().isJsonNull() && !s.has(e.getKey())) {
                throw new IllegalStateException("script colaboradores." + servicio + " is incomplete: it omits " + e.getKey()
                        + " (the legacy class would fill " + e.getValue() + "); list every field");
            }
        }
        internarTipos(obj, new java.util.IdentityHashMap<>());
        return obj;
    }

    /**
     * The real in-process services build MensajeSalida with literal types ("ERROR"), which are interned, and the legacy counts errors with
     * {@code s.tipo == MENSAJE_ERROR} (PlataformaUtils.getNumeroErrores). A message that went through Gson would hold a new String and never be counted;
     * the fake restores the interning so the scripted answer behaves like the real service's.
     */
    private static void internarTipos(Object o, java.util.IdentityHashMap<Object, Boolean> vistos) {
        if (o == null || vistos.put(o, Boolean.TRUE) != null) {
            return;
        }
        if (o instanceof cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida m) {
            if (m.tipo != null) {
                m.tipo = m.tipo.intern();
            }
            return;
        }
        if (o instanceof Iterable<?> it) {
            for (Object x : it) {
                internarTipos(x, vistos);
            }
            return;
        }
        if (o instanceof Map<?, ?> mp) {
            for (Object x : mp.values()) {
                internarTipos(x, vistos);
            }
            return;
        }
        if (!o.getClass().getName().startsWith("cl.bcs.")) {
            return;
        }
        for (java.lang.reflect.Field f : o.getClass().getFields()) {
            try {
                if (!java.lang.reflect.Modifier.isStatic(f.getModifiers()) && !f.getType().isPrimitive()) {
                    internarTipos(f.get(o), vistos);
                }
            } catch (IllegalAccessException ignored) {
                // public fields only
            }
        }
    }

    /** Record the call and answer the script of the collaborator (shims copied from family A use this name): same as registrar + salida. */
    public static <T> T responder(String servicio, Object entrada, Class<T> tipo) throws Exception {
        registrar(servicio, entrada);
        return salida(servicio, tipo);
    }

    public static JsonArray lista(String k) {
        JsonElement e = colaboradores.get(k);
        return e == null || e.isJsonNull() ? new JsonArray() : e.getAsJsonArray();
    }

    public static long numero(String k, long def) {
        JsonElement e = colaboradores.get(k);
        return e == null || e.isJsonNull() ? def : e.getAsLong();
    }

    public static boolean bool(String k, boolean def) {
        JsonElement e = colaboradores.get(k);
        return e == null || e.isJsonNull() ? def : e.getAsBoolean();
    }

    public static String texto(String k, String def) {
        JsonElement e = colaboradores.get(k);
        return e == null || e.isJsonNull() ? def : e.getAsString();
    }

    /** The Gson tree of a POJO exactly as {@link #registrar} serialises it. */
    public static JsonElement arbol(Object o) {
        return GSON.toJsonTree(o);
    }

    /** colaboradores.<k> as an object (empty when absent). */
    public static JsonObject objeto(String k) {
        JsonElement e = colaboradores.get(k);
        return e == null || e.isJsonNull() ? new JsonObject() : e.getAsJsonObject();
    }

    public static JsonObject parse(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }
}
