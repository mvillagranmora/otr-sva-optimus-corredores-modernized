package cl.bcs.utils;

/**
 * ORACLE SHIM: the real cl.bcs.utils.Log appends to a file under user.home (and prints to stdout); the oracle must not write outside its
 * scratch dir. Same public signatures as the legacy class, no effect (the dump records DB state and collaborator calls, not log text).
 */
public class Log {
    private final String relativePath;

    public Log(String name, String pathBase, String modulo, String mask) { this.relativePath = "/LOG_" + name + ".log"; }

    public Log(String name) { this(name, "", "", ""); }

    public Log(String name, String pathBase, String modulo) { this(name, pathBase, modulo, ""); }

    public String getAbsolutePath() { return relativePath; }

    public String getRelativePath() { return relativePath; }

    public void write(Object... params) { }

    public void writeAsJson(String tmsg, Object obj) { }

    public void writeAsJson(Object obj) { }

    public void write(String msg) { }

    public void write(String msg, Exception er) { }

    public void write(Exception er) { }
}
