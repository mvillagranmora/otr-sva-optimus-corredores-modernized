package cl.bcs.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * ORACLE SHIM of legacy cl.bcs.utils.JavaCall (BcsUtils/src/cl/bcs/utils/JavaCall.java:1-52). Same contract; loadResourceToString
 * reads the classpath resource (legacy: commons-io IOUtils.toString), getGsonBuilder is the legacy one (lines 45-52).
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
