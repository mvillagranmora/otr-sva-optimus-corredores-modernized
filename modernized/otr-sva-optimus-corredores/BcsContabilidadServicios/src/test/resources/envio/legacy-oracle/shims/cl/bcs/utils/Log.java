package cl.bcs.utils;

/** ORACLE SHIM of legacy cl.bcs.utils.Log: records lines in memory. */
public class Log {
    public static final java.util.List<String> LINES = new java.util.ArrayList<>();
    private final String name;

    public Log(String name) { this.name = name; }

    public void write(String msg) { LINES.add(name + ": " + msg); }

    public void write(Object... params) { LINES.add(name + ": " + java.util.Arrays.toString(params)); }
}
