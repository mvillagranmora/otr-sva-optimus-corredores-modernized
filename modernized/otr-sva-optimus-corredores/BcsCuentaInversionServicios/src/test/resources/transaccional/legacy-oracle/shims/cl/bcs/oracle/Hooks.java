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
}
