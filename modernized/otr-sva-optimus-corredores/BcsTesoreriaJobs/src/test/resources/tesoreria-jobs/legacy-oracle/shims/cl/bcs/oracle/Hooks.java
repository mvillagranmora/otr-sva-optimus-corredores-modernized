package cl.bcs.oracle;

import java.sql.Timestamp;
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
    /** The database UTC clock (CURRENT_UTCTIMESTAMP / CURRENT_DATE): case.json "reloj" (UTC, yyyy-MM-dd HH:mm:ss). */
    public static Timestamp UTC = Timestamp.valueOf("2025-04-01 10:15:30");
    public static JsonObject colaboradores = new JsonObject();
    public static final List<JsonObject> LLAMADAS = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").create();

    private Hooks() {
    }

    public static void reset(JsonObject c, String reloj) {
        colaboradores = c == null ? new JsonObject() : c;
        LLAMADAS.clear();
        UTC = Timestamp.valueOf(reloj == null ? "2025-04-01 10:15:30" : reloj);
    }

    public static void registrar(String servicio, Object entrada) {
        JsonObject o = new JsonObject();
        o.addProperty("servicio", servicio);
        o.add("entrada", GSON.toJsonTree(entrada));
        LLAMADAS.add(o);
    }

    public static void registrarJson(String servicio, JsonElement entrada) {
        JsonObject o = new JsonObject();
        o.addProperty("servicio", servicio);
        o.add("entrada", entrada);
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

    /** colaboradores.<k> as an object (empty when absent). */
    public static JsonObject objeto(String k) {
        JsonElement e = colaboradores.get(k);
        return e == null || e.isJsonNull() ? new JsonObject() : e.getAsJsonObject();
    }

    public static boolean tiene(String k) {
        JsonElement e = colaboradores.get(k);
        return e != null && !e.isJsonNull();
    }

    /** colaboradores.<k> when it is a JSON string "throw:<Class>:<message>" the fake throws it (see {@link #lanzaSiCorresponde}). */
    public static void lanzaSiCorresponde(String k) throws Exception {
        String t = texto(k, null);
        if (t == null || !t.startsWith("lanza:")) return;
        String[] p = t.split(":", 3);
        String msg = p.length > 2 ? p[2] : null;
        switch (p[1]) {
            case "Exception" -> throw new Exception(msg);
            case "RuntimeException" -> throw new RuntimeException(msg);
            case "IllegalStateException" -> throw new IllegalStateException(msg);
            case "SQLException" -> throw new java.sql.SQLException(msg);
            case "NullPointerException" -> throw new NullPointerException(msg);
            case "Error" -> throw new Error(msg);
            default -> throw new IllegalArgumentException("unknown scripted exception " + p[1]);
        }
    }

    /** A scripted message {id,tipo,codigo,descripcion}; 'tipo' is interned because the legacy compares it with == in some places. */
    public static cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida mensaje(JsonObject o) {
        return new cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida(o.has("id") && !o.get("id").isJsonNull() ? o.get("id").getAsLong() : null,
                o.has("tipo") && !o.get("tipo").isJsonNull() ? o.get("tipo").getAsString().intern() : null,
                o.has("codigo") && !o.get("codigo").isJsonNull() ? o.get("codigo").getAsString() : null,
                o.has("descripcion") && !o.get("descripcion").isJsonNull() ? o.get("descripcion").getAsString() : null);
    }
}
