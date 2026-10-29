package cl.bcs.normativa.generacion;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.Normalizer;

/**
 * Utiles para serializar datos.
 *
 * @author fpino@email.com
 *
 */
public final class Serializador {

    /**
     * Obtiene una secuencia de ceros de largo solicitado.
     *
     * @param n
     *            El largo de la secuencia requerida.
     * @return Una secuencia de ceros de largo solicitado.
     */
    public static String ceros(final int n) {
        return repetir("0", n);
    }

    /**
     * Obtiene una secuencia de espacios de largo solicitado.
     *
     * @param n
     *            El largo de la secuencia requerida.
     * @return Una secuencia de espacios de largo solicitado.
     */
    public static String espacios(final int n) {
        return repetir(" ", n);
    }

    /**
     * Obtiene un elemento del RUT (mantisa o digito verificador).
     * <p>
     * Asume "0" si el elemento no est&aacute;.
     *
     * @param rut
     *            El RUT.
     * @return Un elemento del RUT (mantisa o digito verificador).
     */
    private static String getElementoRut(final String rut) {
        final String r = rut.trim();
        return r.isEmpty() ? "0" : r;
    }

    /**
     * Obtiene una secuencia de repeticiones del {@link String} dado.
     *
     * @param s
     *            El {@link String} dado.
     * @param n
     *            La cantidad de repeticiones.
     * @return Una secuencia de repeticiones del {@link String} dado.
     */
    public static String repetir(final String s, final int n) {
        return new String(new char[n]).replace("\0", s);
    }

    /**
     * Serializa fecha seg&uacute;n formato en las declaraciones.
     *
     * @param date
     *            La fecha.
     * @return {@link String} de la fecha serializada.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public static String serializar(final Date date) throws SerializadorException {
        if (date == null) {
            throw new SerializadorException("fecha null");
        }

        final DateFormat df = new SimpleDateFormat("ddMMyyyy", new Locale("es"));
        return df.format(date);
    }

    public static String removerCaracteresEspeciales(String str)
    {
        // replace the given string
        // with empty string
        // except the pattern "[^a-zA-Z0-9]"
        String StringNormalizado = Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^a-zA-Z0-9]", "");
  
        // return string
        return StringNormalizado;
    } 

    /**
     * Serializa RUT seg&uacute;n formato en las declaraciones.
     *
     * @param rut
     *            El RUT.
     * @return {@link String} del RUT serializado.
     * @throws SerializadorException
     *             En caso de error al serializar.
     */
    public static String serializarRut(final String rut) throws SerializadorException {
        if (rut == null) {
            throw new SerializadorException("rut null");
        }

        final String[] arr = rut.split("-");
        if (arr.length != 2) {
            throw new SerializadorException("RUT mal conformado: " + rut);
        }

        try {
            final String m = getElementoRut(arr[0]);
            final String d = getElementoRut(arr[1]);
            final long n = Long.parseLong(m);
            return String.format("%08d%s", n, d);
        } catch (NumberFormatException ex) {
            throw new SerializadorException("RUT mal conformado: " + rut, ex);
        }
    }

    private Serializador() {
    }

}
