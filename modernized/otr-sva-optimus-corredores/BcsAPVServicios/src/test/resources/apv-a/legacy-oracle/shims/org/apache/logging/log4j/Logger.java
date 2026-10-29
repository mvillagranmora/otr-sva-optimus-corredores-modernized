package org.apache.logging.log4j;

/** ORACLE SHIM: silent logger; prints to stderr when -Doracle.debug=true. */
public class Logger {
    private static final boolean DEBUG = Boolean.getBoolean("oracle.debug");

    private static void out(Object m, Object... a) {
        if (!DEBUG) return;
        System.err.println("[legacy-log] " + m);
        for (Object o : a) {
            if (o instanceof Throwable t) t.printStackTrace();
            else System.err.println("   " + o);
        }
    }

    public void info(Object m, Object... a) { out(m, a); }

    public void debug(Object m, Object... a) { out(m, a); }

    public void warn(Object m, Object... a) { out(m, a); }

    public void error(Object m, Object... a) { out(m, a); }

    public void trace(Object m, Object... a) { }
}
