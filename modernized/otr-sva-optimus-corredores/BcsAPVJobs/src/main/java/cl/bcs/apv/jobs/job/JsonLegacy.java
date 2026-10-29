package cl.bcs.apv.jobs.job;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The legacy platform Gson ({@code Utils.getGsonBuilder()}, BcsUtils Utils.java:1018-1024; {@code Utils.printJson}, :750-758) over the {@code Colaboradores}
 * port: the input of a collaborator is the legacy object written with nulls as JSON null and dates {@code yyyy-MM-dd HH:mm:ss.SSS}; its answer is read into the
 * legacy output class, so an ABSENT key keeps the constructor default and an explicit JSON null is null (a legacy {@code NullPointerException} when iterated).
 */
public final class JsonLegacy {

    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private JsonLegacy() {
    }

    /** Legacy object to its JSON payload. */
    public static JsonObject aJson(Object entrada) {
        return GSON.toJsonTree(entrada).getAsJsonObject();
    }

    /** Collaborator answer to the legacy output class; a null answer is an empty object (the class defaults). */
    public static <T> T de(JsonElement salida, Class<T> clase) {
        return GSON.fromJson(salida == null || salida.isJsonNull() ? new JsonObject() : salida, clase);
    }

    /** Legacy {@code Utils.printJson(obj, false)}: for the log only. */
    public static String texto(Object o) {
        return o == null ? "[]" : GSON.toJson(o);
    }

    public static Gson gson() {
        return GSON;
    }
}
