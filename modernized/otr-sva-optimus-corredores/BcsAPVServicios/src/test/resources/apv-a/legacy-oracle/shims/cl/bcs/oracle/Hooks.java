package cl.bcs.oracle;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

/**
 * ORACLE SUPPORT (not a legacy class): the scripted answers and the call trace of the fake collaborators.
 * {@code colaboradores} is the "colaboradores" object of case.json; every fake service records {@code {servicio, entrada}}
 * in {@link #LLAMADAS}, which the runner writes to expected.json ("colaboradores" section) so the modern code can be
 * compared against what the legacy handed to each collaborator.
 */
public final class Hooks {
    public static JsonObject colaboradores = new JsonObject();
    public static final List<JsonObject> LLAMADAS = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();

    private Hooks() {
    }

    private static final java.util.Map<String, Integer> LLAMADAS_POR_SERVICIO = new java.util.HashMap<>();

    public static void reset(JsonObject c) {
        colaboradores = c == null ? new JsonObject() : c;
        LLAMADAS.clear();
        LLAMADAS_POR_SERVICIO.clear();
    }

    /**
     * Records {servicio, entrada} and answers with colaboradores.&lt;servicio&gt; decoded (platform Gson, Utils.getGsonBuilder) into the
     * legacy Salida class: a JSON object answers every call, a JSON array answers the n-th call (the last element repeats); with no script the
     * answer is an empty Salida (its default constructor).
     */
    public static <T> T responder(String servicio, Object entrada, Class<T> salida) throws Exception {
        registrar(servicio, entrada);
        int n = LLAMADAS_POR_SERVICIO.merge(servicio, 1, Integer::sum) - 1;
        JsonElement e = colaboradores.get(servicio);
        if (e != null && e.isJsonArray()) {
            JsonArray a = e.getAsJsonArray();
            e = a.size() == 0 ? null : a.get(Math.min(n, a.size() - 1));
        }
        if (e == null || e.isJsonNull()) {
            return salida.getDeclaredConstructor().newInstance();
        }
        T r = cl.bcs.utils.Utils.getGsonBuilder().fromJson(e, salida);
        internar(r);
        return r;
    }

    /**
     * The legacy collaborators build their MensajeSalida with the tipo constants, and MensajeSalida.getNumeroErrores compares tipo with ==
     * (PlataformaUtils.java:636). A message decoded from JSON would never be counted: its tipo is interned here, as the real services' literals are.
     */
    public static void internar(Object o) throws IllegalAccessException {
        if (o == null) return;
        for (java.lang.reflect.Field f : o.getClass().getFields()) {
            Object v = f.get(o);
            if (v instanceof java.util.List<?> l) {
                for (Object x : l) {
                    if (x instanceof MensajeSalida m && m.tipo != null) m.tipo = m.tipo.intern();
                }
            }
        }
    }

    public static void registrar(String servicio, Object entrada) {
        JsonObject o = new JsonObject();
        o.addProperty("servicio", servicio);
        o.add("entrada", GSON.toJsonTree(entrada));
        LLAMADAS.add(o);
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
}
