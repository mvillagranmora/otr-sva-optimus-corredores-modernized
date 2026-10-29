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

/**
 * ORACLE SUPPORT (not a legacy class): the scripted answers and the call trace of the fake collaborators.
 * {@code colaboradores} is the "colaboradores" object of case.json; every fake service records {@code {servicio, entrada}}
 * in {@link #LLAMADAS}, which the runner writes to expected.json ("colaboradores" section) so the modern code can be
 * compared against what the legacy handed to each collaborator.
 *
 * <p>A scripted answer is a JSON object (it answers every call) or an array (it answers the n-th call; the last element repeats).
 * An answer {@code {"_lanza": {"clase": "SQLException", "mensaje": "boom"}}} makes the collaborator THROW that exception instead
 * (the same factory, {@link #excepcion(String, String)}, builds it in the modern test double).
 */
public final class Hooks {
    public static JsonObject colaboradores = new JsonObject();
    public static final List<JsonObject> LLAMADAS = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();
    private static final Map<String, Integer> LLAMADAS_POR_SERVICIO = new HashMap<>();

    private Hooks() {
    }

    public static void reset(JsonObject c) {
        colaboradores = c == null ? new JsonObject() : c;
        LLAMADAS.clear();
        LLAMADAS_POR_SERVICIO.clear();
    }

    /** The scripted answer of the n-th call of {@code servicio}, or null when none is scripted. */
    public static JsonElement guion(String servicio) {
        int n = LLAMADAS_POR_SERVICIO.merge(servicio, 1, Integer::sum) - 1;
        JsonElement e = colaboradores.get(servicio);
        if (e != null && e.isJsonArray()) {
            JsonArray a = e.getAsJsonArray();
            e = a.size() == 0 ? null : a.get(Math.min(n, a.size() - 1));
        }
        return e == null || e.isJsonNull() ? null : e;
    }

    /** Throws the scripted exception, when the answer is a {"_lanza": ...} object. */
    public static void lanzarSiCorresponde(JsonElement e) {
        if (e != null && e.isJsonObject() && e.getAsJsonObject().has("_lanza")) {
            JsonObject l = e.getAsJsonObject().getAsJsonObject("_lanza");
            Hooks.<RuntimeException>sneaky(excepcion(l.get("clase").getAsString(), l.has("mensaje") && !l.get("mensaje").isJsonNull() ? l.get("mensaje").getAsString() : null));
        }
    }

    /** Throws any Throwable without declaring it: the legacy collaborators declare `throws Exception` and the scripted failure may be an Error. */
    @SuppressWarnings("unchecked")
    public static <E extends Throwable> void sneaky(Throwable t) throws E {
        throw (E) t;
    }

    public static Throwable excepcion(String clase, String mensaje) {
        return switch (clase) {
            case "Exception" -> new Exception(mensaje);
            case "SQLException" -> new java.sql.SQLException(mensaje);
            case "RuntimeException" -> new RuntimeException(mensaje);
            case "IllegalStateException" -> new IllegalStateException(mensaje);
            case "IllegalArgumentException" -> new IllegalArgumentException(mensaje);
            case "NullPointerException" -> new NullPointerException(mensaje);
            case "ArithmeticException" -> new ArithmeticException(mensaje);
            case "Error" -> new Error(mensaje);
            case "AssertionError" -> new AssertionError(mensaje);
            default -> throw new IllegalArgumentException("unknown scripted exception class " + clase);
        };
    }

    /**
     * Records {servicio, entrada} and answers with colaboradores.&lt;servicio&gt; decoded (platform Gson, Utils.getGsonBuilder) into the
     * legacy Salida class; with no script the answer is an empty Salida (its default constructor).
     */
    public static <T> T responder(String servicio, Object entrada, Class<T> salida) throws Exception {
        registrar(servicio, entrada);
        JsonElement e = guion(servicio);
        lanzarSiCorresponde(e);
        if (e == null) {
            return salida.getDeclaredConstructor().newInstance();
        }
        T r = cl.bcs.utils.Utils.getGsonBuilder().fromJson(e, salida);
        internar(r);
        return r;
    }

    /**
     * The legacy collaborators build their MensajeSalida with the tipo constants and some callers compare tipo with == (PlataformaUtils.java:636).
     * A message decoded from JSON would never match: its tipo is interned here, as the real services' literals are.
     */
    public static void internar(Object o) throws IllegalAccessException {
        if (o == null) return;
        for (java.lang.reflect.Field f : o.getClass().getFields()) {
            Object v = f.get(o);
            if (v instanceof List<?> l) {
                for (Object x : l) {
                    if (x instanceof cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida m && m.tipo != null) m.tipo = m.tipo.intern();
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
}
