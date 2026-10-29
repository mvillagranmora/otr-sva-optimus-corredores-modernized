package cl.bcs.utils;

/**
 * UTIL-01: validation of a Chilean RUT with its modulo-11 check digit (legacy {@code RutUtil.java:46-98}).
 *
 * <p>Legacy behavior kept as the characterization tests pin it: only {@link String#trim()} whitespace is ignored,
 * dots are removed anywhere in the mantissa, any other non-ASCII-digit character rejects the RUT, the check digit is
 * case-insensitive ({@code k} == {@code K}) and RUT 0 ({@code "0-0"}) is accepted. The {@code main()} of the legacy
 * class is not ported (RULE-076).
 */
public final class RutUtil {

    private RutUtil() {
    }

    /** {@code true} when {@code rut} has the shape {@code <mantissa>-<check digit>} and the check digit matches. */
    public static boolean validar(String rut) {
        if (rut == null) {
            return false;
        }
        String s = rut.trim();
        if (s.length() < 3 || s.charAt(s.length() - 2) != '-') {
            return false;
        }
        String mantisa = s.substring(0, s.length() - 2).replace(".", "");
        if (mantisa.isEmpty()) {
            return false;
        }
        int suma = 0; // int on purpose: the legacy overflow on absurdly long mantissas is part of the pinned behavior
        int factor = 2;
        for (int i = mantisa.length() - 1; i >= 0; i--) {
            char c = mantisa.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
            suma += factor * (c - '0');
            factor = factor == 7 ? 2 : factor + 1;
        }
        return digitoVerificador(suma) == Character.toUpperCase(s.charAt(s.length() - 1));
    }

    private static char digitoVerificador(int suma) {
        int n = 11 - suma % 11;
        return switch (n) {
            case 10 -> 'K';
            case 11 -> '0';
            default -> (char) (n + '0');
        };
    }
}
