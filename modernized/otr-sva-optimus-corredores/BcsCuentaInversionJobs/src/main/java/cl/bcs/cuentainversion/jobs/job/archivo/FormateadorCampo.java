package cl.bcs.cuentainversion.jobs.job.archivo;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Field formatter of a generated file, bug for bug (legacy {@code FormatoArchivo.formatear} and {@code buscarFormatoArchivoDetalle},
 * FormatoArchivo.java:206-354). Every failure is swallowed and the value formatted so far (usually the empty text) is returned (CIJ-DEF-10):
 * DEC decimals are LEFT padded, a DEC without "." becomes 0, integer digits above the width are not cut, a width of 0 makes the field empty
 * (format exception), NUM is not truncated, dates use the lenient {@link SimpleDateFormat}, an unknown or lower-case {@code TipoDato} prints
 * nothing. Locale and zone are the JVM defaults, as the legacy.
 */
final class FormateadorCampo {

    private static final Logger log = LoggerFactory.getLogger(FormateadorCampo.class);

    private FormateadorCampo() {
    }

    /** Legacy :206-230. */
    private static FormatoArchivoDetalle buscar(FormatoArchivo formato, int tipoRegistro, String nombreCampo) {
        FormatoArchivoDetalle encontrado = null;
        try {
            if (!formato.detalle().isEmpty()) {
                for (FormatoArchivoDetalle fad : formato.detalle()) {
                    if (fad.getTipoRegistro() == tipoRegistro && fad.getCampo().equals(nombreCampo)) {
                        encontrado = fad;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.error("buscarFormatoArchivoDetalle: {}", e.toString(), e);
        }
        return encontrado;
    }

    /** Legacy :242-354. */
    static String formatear(FormatoArchivo formato, int tipoRegistro, String nombreCampo, String valor) {
        String valorFormateado = "";
        try {
            FormatoArchivoDetalle fad = buscar(formato, tipoRegistro, nombreCampo);
            if (fad != null) {
                Integer largo = fad.getLargo();
                valor = valor == null ? "" : valor.trim();
                if (valor.length() < 1 && fad.getValorDefecto().compareTo("") != 0) {
                    valor = fad.getValorDefecto();
                }
                if (fad.getTipoDato().compareTo("CHAR") == 0) {
                    valorFormateado = cadena(formato, largo, valor);
                }
                if (fad.getTipoDato().compareTo("DATE") == 0) {
                    valorFormateado = fecha(fad, largo, valor);
                }
                if (fad.getTipoDato().compareTo("NUM") == 0) {
                    if (formato.getRellenarCampo().equalsIgnoreCase("S")) {
                        valorFormateado = String.format("%1$" + largo + "s", valor).replace(' ', '0');
                    } else {
                        valorFormateado = valor.replace(' ', '0');
                    }
                }
                if (fad.getTipoDato().compareTo("DEC") == 0) {
                    valorFormateado = decimal(formato, fad, largo, valor);
                }
            }
        } catch (Exception e) {
            log.error("formatear: {}", e.toString(), e);
        }
        return valorFormateado;
    }

    /** CHAR: truncated to the width; padded with spaces on the right when RellenarCampo is S. */
    private static String cadena(FormatoArchivo formato, Integer largo, String valor) {
        if (valor.length() > largo) {
            valor = valor.trim().substring(0, largo);
        }
        if (formato.getRellenarCampo().equalsIgnoreCase("S")) {
            return String.format("%1$-" + largo + "s", valor);
        }
        return valor;
    }

    /** DATE: the value is yyyy-MM-dd, written with the configured format (lenient parser). */
    private static String fecha(FormatoArchivoDetalle fad, Integer largo, String valor) throws Exception {
        if (fad.getFormato() != null && !fad.getFormato().equalsIgnoreCase("") && valor != null && !valor.equalsIgnoreCase("")) {
            SimpleDateFormat parseador = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat formateador = new SimpleDateFormat(fad.getFormato());
            Date date = parseador.parse(valor);
            return String.format("%1$-" + largo + "s", formateador.format(date));
        }
        return String.format("%1$-" + largo + "s", valor);
    }

    /** DEC: integer and decimal parts padded (left, with zeros) apart; the decimal separator is the configured one. */
    private static String decimal(FormatoArchivo formato, FormatoArchivoDetalle fad, Integer largo, String valor) {
        Integer largoParteEntera = largo - fad.getDecimales();
        Integer largoParteDecimal = (fad.getDecimales() != null) ? fad.getDecimales() : 0;
        if (valor.indexOf(".") == -1) {
            valor = "0";
        }
        String[] partes = valor.split("\\.");
        String parteEntera = (partes.length > 0) ? partes[0] : "0";
        String parteDecimal = (partes.length > 1) ? partes[1] : "0";
        if (parteDecimal.length() > largoParteDecimal) {
            parteDecimal = parteDecimal.trim().substring(0, largoParteDecimal);
        }
        String valorParteEntera;
        String valorParteDecimal;
        if (formato.getRellenarCampo().equalsIgnoreCase("S")) {
            valorParteEntera = String.format("%1$" + largoParteEntera + "s", parteEntera).replace(' ', '0');
            valorParteDecimal = String.format("%1$" + largoParteDecimal + "s", parteDecimal).replace(' ', '0');
        } else {
            valorParteEntera = parteEntera.replace(' ', '0');
            valorParteDecimal = parteDecimal.replace(' ', '0');
        }
        return valorParteEntera + formato.getSeparadorDecimales() + valorParteDecimal;
    }
}
