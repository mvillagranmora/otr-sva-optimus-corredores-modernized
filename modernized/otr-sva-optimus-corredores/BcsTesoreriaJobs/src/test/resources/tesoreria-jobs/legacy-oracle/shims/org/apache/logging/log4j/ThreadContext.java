package org.apache.logging.log4j;

/** ORACLE SHIM of log4j ThreadContext (BcsJob.run puts the job id in the MDC). */
public class ThreadContext {
    public static void put(String k, String v) { }

    public static void clearMap() { }
}
