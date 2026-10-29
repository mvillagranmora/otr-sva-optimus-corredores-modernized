package cl.bcs.utils;

/** ORACLE SHIM of legacy cl.bcs.utils.Fecha.toFormat (Fecha.java:62). */
public class Fecha {
    public static String toFormat(java.sql.Timestamp date, String format) {
        return new java.text.SimpleDateFormat(format).format(date);
    }
}
