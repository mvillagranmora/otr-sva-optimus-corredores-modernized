package cl.bcs.utils;

import com.google.gson.JsonObject;

import java.sql.Connection;

/**
 * UTIL-40: plugin contract of the platform services registered in {@code PLA.Servicio} (legacy
 * {@code JavaCall.java:10-53}). Contract kept: {@code call(java.sql.Connection, JsonObject) throws Exception}; the
 * default methods come from {@link JavaUtil}.
 */
@FunctionalInterface
public interface JavaCall extends JavaUtil {

    JsonObject call(Connection conn, JsonObject in) throws Exception;
}
