package cl.bcs.oracle;

import java.util.ArrayList;
import java.util.List;

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
 */
public final class Hooks {
    public static JsonObject colaboradores = new JsonObject();
    public static final List<JsonObject> LLAMADAS = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();

    private Hooks() {
    }

    public static void reset(JsonObject c) {
        colaboradores = c == null ? new JsonObject() : c;
        LLAMADAS.clear();
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

    /** The Gson tree of a POJO exactly as {@link #registrar} serialises it. */
    public static JsonElement arbol(Object o) {
        return GSON.toJsonTree(o);
    }

    /** colaboradores.<k> as an object (empty when absent). */
    public static JsonObject objeto(String k) {
        JsonElement e = colaboradores.get(k);
        return e == null || e.isJsonNull() ? new JsonObject() : e.getAsJsonObject();
    }

    /** A scripted message {id,tipo,codigo,descripcion}; 'tipo' is interned because the legacy compares it with == (PlataformaUtils.MensajeSalida.getNumeroErrores). */
    public static cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida mensaje(JsonObject o) {
        return new cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida(o.has("id") && !o.get("id").isJsonNull() ? o.get("id").getAsLong() : null,
                o.get("tipo").getAsString().intern(), o.get("codigo").getAsString(), o.has("descripcion") && !o.get("descripcion").isJsonNull() ? o.get("descripcion").getAsString() : null);
    }

    /**
     * Masks the values of NumeroReferencia / NumeroReferenciaFormaDePago by their order of first appearance (1, 2, 3 ...): the legacy
     * ProcesarClearingFinanciero draws them from java.util.Random (GrupoCamara.NumeroReferencia), so the raw values are not reproducible.
     * The same function runs in the modern tests (support/Mascara).
     */
    public static JsonElement enmascararReferencias(JsonElement e) {
        java.util.Map<String, Integer> orden = new java.util.HashMap<>();
        masc(e, orden);
        return e;
    }

    private static void masc(JsonElement e, java.util.Map<String, Integer> orden) {
        if (e == null || e.isJsonNull()) return;
        if (e.isJsonArray()) {
            for (JsonElement x : e.getAsJsonArray()) masc(x, orden);
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
