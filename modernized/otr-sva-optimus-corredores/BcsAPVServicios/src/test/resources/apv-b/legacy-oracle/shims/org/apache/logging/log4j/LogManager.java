package org.apache.logging.log4j;

/** ORACLE SHIM. */
public class LogManager {
    public static Logger getLogger(Class<?> c) { return new Logger(); }

    public static Logger getLogger(String name) { return new Logger(); }
}
