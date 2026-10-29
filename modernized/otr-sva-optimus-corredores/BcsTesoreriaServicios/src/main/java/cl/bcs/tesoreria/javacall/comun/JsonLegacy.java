package cl.bcs.tesoreria.javacall.comun;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cl.bcs.utils.Utils;

/**
 * The Gson settings of the legacy JavaCall contract. {@link #ENTRADA_SALIDA} is the one every {@code call} used to decode its input and encode its
 * output (nulls written, NaN/Infinity as literals, dates as {@code yyyy-MM-dd HH:mm:ss.SSS}); {@link #PLATAFORMA} is the platform one
 * ({@code Utils.getGsonBuilder()}: the same plus the lenient Timestamp/Date deserializers), used between services and for collaborator payloads.
 * Gson instances are thread safe.
 */
public final class JsonLegacy {

    public static final String FORMATO_FECHA = "yyyy-MM-dd HH:mm:ss.SSS";

    public static final Gson ENTRADA_SALIDA = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat(FORMATO_FECHA).serializeNulls().create();

    public static final Gson PLATAFORMA = Utils.getGsonBuilder();

    private JsonLegacy() {
    }
}
