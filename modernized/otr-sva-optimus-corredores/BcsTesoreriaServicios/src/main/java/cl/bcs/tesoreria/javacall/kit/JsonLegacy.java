package cl.bcs.tesoreria.javacall.kit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.utils.Utils;

/** The two Gson configurations of the legacy JavaCall contract. */
public final class JsonLegacy {

    private static final String FORMATO_FECHA = "yyyy-MM-dd HH:mm:ss.SSS";

    private JsonLegacy() {
    }

    /** Decoder/encoder of request and response objects: legacy {@code Utils.getGsonBuilder()} (lenient Date/Timestamp readers, nulls serialised). */
    public static Gson gson() {
        return Utils.getGsonBuilder();
    }

    /** Encoder of what is handed to a collaborator: nulls serialised, dates as {@code yyyy-MM-dd HH:mm:ss.SSS}, no special-float literals. */
    public static Gson gsonColaborador() {
        return new GsonBuilder().serializeNulls().setDateFormat(FORMATO_FECHA).create();
    }

    public static JsonObject aJson(Object o) {
        return gsonColaborador().toJsonTree(o).getAsJsonObject();
    }

    public static JsonElement arbol(Object o) {
        return gson().toJsonTree(o);
    }

    public static <T> T de(JsonElement json, Class<T> tipo) {
        return gson().fromJson(json, tipo);
    }
}
