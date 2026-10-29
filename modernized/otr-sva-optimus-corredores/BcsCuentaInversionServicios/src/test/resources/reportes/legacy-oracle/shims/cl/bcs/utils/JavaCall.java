package cl.bcs.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * ORACLE SHIM of legacy cl.bcs.utils.JavaCall (BcsUtils/src/cl/bcs/utils/JavaCall.java:10-52). Same contract.
 * loadResourceToString reads the SQL text from the legacy source tree put on the classpath (JavaCall.java:29-43);
 * getGsonBuilder is the legacy configuration (JavaCall.java:45-52): date format, Timestamp/Date deserialisers,
 * serializeSpecialFloatingPointValues and serializeNulls.
 */
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

    default Gson getGsonBuilder() {
        GsonBuilder gbuild = new GsonBuilder();
        gbuild.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        gbuild.registerTypeAdapter(java.sql.Timestamp.class, new GsonTimestampDeserializer());
        gbuild.registerTypeAdapter(java.sql.Date.class, new GsonDateDeserializer());
        return gbuild.serializeSpecialFloatingPointValues().serializeNulls().create();
    }
}
