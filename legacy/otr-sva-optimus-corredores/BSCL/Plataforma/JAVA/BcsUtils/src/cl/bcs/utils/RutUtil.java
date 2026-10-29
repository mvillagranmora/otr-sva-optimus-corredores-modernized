package cl.bcs.utils;

/**
 * Metodos utiles para el manejo de RUT's.
 *
 * @author fpino@email.com
 *
 */
public final class RutUtil {

    private static void caso(final String rut) {
        System.out.println(rut + ": " + validar(rut));
    }

    /**
     * Algunos ejemplos de prueba.
     *
     * @param args
     *            Argumentos.
     */
    public static void main(final String[] args) {
        caso("1-9");
        caso("23452345-5"); // DV=7
        caso("5.123.225-9");
        caso("9819078-3");
        caso("90249000-0");
        caso("97030000-7");
        caso("97036000-K");
        caso("97053000-2");
        caso("97018000-1");
        caso("97006000-6");
        caso("97080000-k");
        caso("76645030-K");
    }

    /**
     * V&aacute;lida que el RUT cumpla con formato y d&iacute;gito verificador.
     * <p>
     * Ignora los separadores de miles en la mantisa (solo comas).
     *
     * @param str
     *            El String a validar.
     * @return <code>true</code> si el RUT es valido, <code>false</code> en caso
     *         contrario.
     */
    public static boolean validar(final String str) {
        if (str == null) {
            return false;
        }

        final String s = str.trim();
        if (s.length() < 3) {
            return false;
        }

        final char sep = s.charAt(s.length() - 2);
        if (sep != '-') {
            return false;
        }

        final String mantisa = s.substring(0, s.length() - 2).replace(".", "");
        if (mantisa.isEmpty()) {
            return false;
        }

        int sum = 0;
        int mul = 2;
        for (int i = mantisa.length() - 1; i >= 0; i--) {
            final char ch = mantisa.charAt(i);
            if (ch < '0' || ch > '9') {
                return false;
            }

            sum += mul * (ch - '0');

            mul++;
            if (mul > 7) {
                mul = 2;
            }
        }

        final int n = 11 - sum % 11;
        char d;
        switch (n) {
        case 10:
            d = 'K';
            break;
        case 11:
            d = '0';
            break;
        default:
            d = (char) (n + '0');
            break;
        }

        final char dv = s.substring(s.length() - 1).toUpperCase().charAt(0);
        return d == dv;
    }

    private RutUtil() {
    }

}
