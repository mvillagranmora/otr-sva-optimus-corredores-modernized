package cl.bcs.cuentainversion.jobs.programa;

/** Text rules of {@code BAT.Programa.Resultado} (NVARCHAR(2000)). */
public final class TextoResultado {

    private TextoResultado() {
    }

    /**
     * Legacy cut (TransactionManager.java:216-218): if {@code descripcion.trim().length() > 2000} the text is
     * {@code descripcion.substring(0, 1999)} (1999 characters, NOT trimmed), otherwise it is kept as is. A null text (the legacy threw a
     * NullPointerException before updating anything) is stored as "". One addition: a text with more than 2000 characters whose trimmed
     * form has at most 2000 (blanks around it) would overflow the column in the legacy; it is cut to 1999 too.
     */
    public static String truncar(String descripcion) {
        if (descripcion == null) {
            return "";
        }
        if (descripcion.length() > 2000) { // includes the legacy test trim().length() > 2000
            return descripcion.substring(0, 1999);
        }
        return descripcion;
    }

    /** {@code "Terminado con errores:" + mensaje} as the legacy listener built it (SB:150-173); a null message prints "null" as it did. */
    public static String conErrores(String mensaje) {
        return "Terminado con errores:" + mensaje;
    }
}
