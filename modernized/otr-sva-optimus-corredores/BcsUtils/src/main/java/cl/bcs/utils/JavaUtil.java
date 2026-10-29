package cl.bcs.utils;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * UTIL-40: default methods of the plugin contract (legacy {@code JavaUtil.java:10-44}; the same bodies were copied
 * into {@code JavaCall.java} and {@code JavaCallExtra.java}, which now inherit them from here).
 */
public interface JavaUtil {

    /**
     * Loads a resource of this implementation's class loader as UTF-8 text (the legacy used the platform default
     * charset, pinned as UTF-8). A path starting with {@code /}, or a missing resource, throws
     * {@link IllegalStateException}; a {@code null} path throws {@link NullPointerException}.
     */
    default String loadResourceToString(String path) {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("can't load resource:" + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** A new {@link Gson} on every call, configured as {@link Utils#getGsonBuilder()}. */
    default Gson getGsonBuilder() {
        return Utils.getGsonBuilder();
    }
}
