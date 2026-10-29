package cl.bcs.utils;

import java.util.Arrays;

/**
 * ORACLE SHIM of legacy cl.bcs.utils.Log (BcsUtils/src/cl/bcs/utils/Log.java). The real class appends to a file in the
 * user home with a timestamp prefix (Log.java:26-37, :59-72, :110-125). The shim keeps the message text (without
 * timestamp) in memory so the oracle can record what the legacy code logs; write(Object...) uses
 * Arrays.deepToString exactly like Log.java:62.
 */
public class Log {
    public static final java.util.List<String> LINES = new java.util.ArrayList<>();
    private final String name;

    public Log(String name) { this.name = name; }
    public Log(String name, String pathBase, String modulo) { this.name = name; }
    public Log(String name, String pathBase, String modulo, String mask) { this.name = name; }

    public String getAbsolutePath() { return "/LOG_" + name + ".log"; }
    public String getRelativePath() { return "/LOG_" + name + ".log"; }

    public void write(String msg) { LINES.add(name + ": " + msg); }

    public void write(Object... params) {
        LINES.add(name + ": " + (params == null ? "[]" : Arrays.deepToString(params)));
    }

    public void write(String msg, Exception er) { write(msg + " " + er); }

    public void write(Exception er) { write(er.getMessage(), er); }

    public void writeAsJson(Object obj) { LINES.add(name + ": " + Utils.printJson(obj, true)); }

    public void writeAsJson(String tmsg, Object obj) { LINES.add(name + ": " + tmsg + " " + (obj == null ? "[]" : Utils.printJson(obj, true))); }
}
