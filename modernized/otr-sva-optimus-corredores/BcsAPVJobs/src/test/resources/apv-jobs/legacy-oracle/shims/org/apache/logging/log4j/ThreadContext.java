package org.apache.logging.log4j;

/** ORACLE SHIM: BcsJob.run() tags the thread with the job id; nothing observable. */
public class ThreadContext {
    public static void put(String k, String v) { }

    public static void clearMap() { }
}
