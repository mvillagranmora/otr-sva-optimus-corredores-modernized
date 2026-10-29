package cl.bcs.utils;

/** ORACLE SHIM of legacy cl.bcs.utils.Log (file logger): records lines in memory, no file is written. */
public class Log {
    public static final java.util.List<String> LINES = new java.util.ArrayList<>();
    private final String name;

    public Log(String name) { this.name = name; }

    public Log(String name, String pathBase, String modulo) { this.name = name; }

    public Log(String name, String pathBase, String modulo, String mask) { this.name = name; }

    public void write(String msg) { LINES.add(name + ": " + msg); }

    public void write(Object... params) { LINES.add(name + ": " + java.util.Arrays.toString(params)); }

    public String getAbsolutePath() { return "oracle-fake-log/" + name + ".log"; }
}
