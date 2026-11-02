package cl.bcs.sura.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/** Null-tolerant readers over the Gson trees the JavaCall dispatcher hands to the services. */
final class JsonCampos {

    private JsonCampos() {}

    /** Text of a field, "" when absent or null (legacy POJOs default their strings to ""). */
    static String texto(JsonObject o, String clave) {
        return texto(o, clave, "");
    }

    static String texto(JsonObject o, String clave, String porDefecto) {
        JsonElement e = o.get(clave);
        return (e == null || e.isJsonNull()) ? porDefecto : e.getAsString();
    }

    static long entero(JsonObject o, String clave, long porDefecto) {
        JsonElement e = o.get(clave);
        return (e == null || e.isJsonNull()) ? porDefecto : e.getAsLong();
    }

    /** Boxed long, null when absent (legacy Long fields are null until Gson fills them). */
    static Long enteroONulo(JsonObject o, String clave) {
        JsonElement e = o.get(clave);
        return (e == null || e.isJsonNull()) ? null : e.getAsLong();
    }

    /** Array of a field, empty (detached) when absent or not an array. */
    static JsonArray lista(JsonObject o, String clave) {
        JsonElement e = o.get(clave);
        return (e != null && e.isJsonArray()) ? e.getAsJsonArray() : new JsonArray();
    }

    static List<JsonObject> objetos(JsonObject o, String clave) {
        List<JsonObject> r = new ArrayList<>();
        for (JsonElement e : lista(o, clave)) r.add(e.getAsJsonObject());
        return r;
    }

    /** Array of the output tree; created and attached when the incoming jout did not carry it. */
    static JsonArray listaDeSalida(JsonObject salida, String clave) {
        JsonElement e = salida.get(clave);
        if (e != null && e.isJsonArray()) return e.getAsJsonArray();
        JsonArray nueva = new JsonArray();
        salida.add(clave, nueva);
        return nueva;
    }
}
