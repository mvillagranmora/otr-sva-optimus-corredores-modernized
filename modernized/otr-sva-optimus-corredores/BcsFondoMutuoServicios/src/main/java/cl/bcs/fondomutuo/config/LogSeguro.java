package cl.bcs.fondomutuo.config;

/**
 * SEC-006 (CWE-117): text that may carry request values (an exception message) is logged only after CR, LF and every other control character are
 * replaced, and truncated, so a caller cannot forge log lines. The detail is for the server log only; it never goes into a response.
 */
public final class LogSeguro {

    public static final int MAXIMO = 300;

    private LogSeguro() {
    }

    public static String limpiar(String texto) {
        if (texto == null) return "null";
        StringBuilder sb = new StringBuilder(Math.min(texto.length(), MAXIMO) + 3);
        for (int i = 0; i < texto.length() && sb.length() < MAXIMO; i++) {
            char c = texto.charAt(i);
            sb.append(Character.isISOControl(c) || c == ' ' || c == ' ' ? '_' : c);
        }
        if (texto.length() > MAXIMO) sb.append("...");
        return sb.toString();
    }
}
