package cl.bcs.utils;

import com.google.gson.JsonObject;

import java.sql.Connection;

/**
 * UTIL-40: plugin contract that also receives the output object (legacy {@code JavaCallExtra.java:10-53}); the
 * default methods come from {@link JavaUtil}.
 */
@FunctionalInterface
public interface JavaCallExtra extends JavaUtil {

    JsonObject call(Connection conn, JsonObject in, JsonObject out) throws Exception;
}
