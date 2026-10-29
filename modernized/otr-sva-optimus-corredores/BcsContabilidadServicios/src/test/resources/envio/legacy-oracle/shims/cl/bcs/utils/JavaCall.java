package cl.bcs.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** ORACLE SHIM of legacy cl.bcs.utils.JavaCall (BcsUtils/src/cl/bcs/utils/JavaCall.java). Same contract, no framework. */
public interface JavaCall {
    com.google.gson.JsonObject call(java.sql.Connection conn, com.google.gson.JsonObject in) throws Exception;

    default String loadResourceToString(final String path) {
        try (var stream = this.getClass().getClassLoader().getResourceAsStream(path)) {
            if (stream == null) throw new IllegalStateException("can't load resource:" + path);
            return new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Legacy JavaCall.java:45-52: dateFormat + serializeSpecialFloatingPointValues + serializeNulls. */
    default Gson getGsonBuilder() {
        return new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .serializeSpecialFloatingPointValues().serializeNulls().create();
    }
}
