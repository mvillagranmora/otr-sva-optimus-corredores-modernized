package cl.bcs.fondomutuo.kit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The two Gson configurations of the legacy JavaCall classes. {@link #plano()} is {@code new Gson()}: what the classes use to read their input
 * (an absent field stays {@code null}: D-MAN-2) and to write their output (null keys omitted, no {@code serializeNulls}).
 * {@link #conNulos()} is {@code Utils.printJson} (used by {@code toString()}): special floating values, {@code yyyy-MM-dd HH:mm:ss.SSS}, nulls written.
 * Gson instances are thread-safe and hold no request state.
 */
public final class JsonLegacy {

    private static final Gson PLANO = new Gson();
    private static final Gson CON_NULOS = new GsonBuilder().serializeSpecialFloatingPointValues()
            .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private JsonLegacy() {
    }

    public static Gson plano() {
        return PLANO;
    }

    public static Gson conNulos() {
        return CON_NULOS;
    }
}
