package cl.bcs.contabilidad.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * JSON conversion for the JavaCall services (legacy {@code cl.bcs.utils.JavaCall}).
 *
 * <p>Legacy services used one of two Gson set-ups and the choice is visible in their output:
 * {@code getGsonBuilder()} serialises nulls ({@link #legacy()}), a plain {@code new Gson()} omits them
 * ({@link #plain()}).
 */
final class JavaCallJson {

    private static final Gson LEGACY = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            .serializeSpecialFloatingPointValues()
            .serializeNulls()
            .create();

    private static final Gson PLAIN = new Gson();

    private JavaCallJson() {}

    /** Legacy {@code JavaCall.getGsonBuilder()}: nulls are written. */
    static Gson legacy() {
        return LEGACY;
    }

    /** Legacy {@code new Gson()}: nulls are omitted. */
    static Gson plain() {
        return PLAIN;
    }

    static <T> T read(Gson gson, JsonObject in, Class<T> type) {
        return gson.fromJson(in, type);
    }

    static JsonObject write(Gson gson, Object out) {
        return gson.toJsonTree(out).getAsJsonObject();
    }
}
