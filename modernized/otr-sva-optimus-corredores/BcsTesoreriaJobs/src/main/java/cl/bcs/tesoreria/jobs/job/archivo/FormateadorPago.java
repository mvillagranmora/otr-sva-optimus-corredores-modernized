package cl.bcs.tesoreria.jobs.job.archivo;

import cl.bcs.tesoreria.jobs.job.archivo.FormatoPago.Detalle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Field formatter of the payment file. Legacy: {@code generarArchivoPago.FormatoArchivo.buscarFormatoArchivoDetalle} (:260-284) and
 * {@code formatear} (:296-414), ported line by line. Every error is swallowed and the field is written empty (TES-J-DEF-05); the
 * decimal part is LEFT padded with zeros ({@code 1234.5} becomes {@code 1234.05}); a DEC without '.' becomes an empty field; a
 * field without definition prints empty.
 */
final class FormateadorPago {

    private FormateadorPago() {
    }

    /** :260-284 */
    private static Detalle buscar(List<Detalle> detalles, int tipoRegistro, String nombreCampo) {
        Detalle local = null;
        try {
            if (!detalles.isEmpty()) {
                for (Detalle fad : detalles) {
                    if (fad.tipoRegistro == tipoRegistro && fad.campo.equals(nombreCampo)) {
                        local = fad;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // legacy: printStackTrace
        }
        return local;
    }

    /** :296-414 */
    static String formatear(FormatoPago formato, List<Detalle> detalles, int tipoRegistro, String nombreCampo, String valor) {
        String valorFormateado = "";
        Integer largo = 0;
        String valorParteEntera = "0";
        String valorParteDecimal = "0";
        try {
            Detalle fad = buscar(detalles, tipoRegistro, nombreCampo);
            if (fad != null) {
                largo = fad.largo;
                valor = valor.trim();
                if (valor.length() < 1 && fad.valorDefecto.compareTo("") != 0) {
                    valor = fad.valorDefecto;
                }
                if (fad.tipoDato.compareTo("CHAR") == 0) {
                    if ("\\n".equals(valor)) {
                        valor = "\n";
                    }
                    if (valor.length() > largo) {
                        valor = valor.trim().substring(0, largo);
                    }
                    if (formato.getRellenarCampo().equalsIgnoreCase("S")) {
                        valorFormateado = String.format("%1$-" + largo + "s", valor);
                    } else {
                        valorFormateado = valor;
                    }
                }
                if (fad.tipoDato.compareTo("DATE") == 0) {
                    if (fad.formato != null && !fad.formato.equalsIgnoreCase("") && valor != null && !valor.equalsIgnoreCase("")) {
                        SimpleDateFormat parseador = new SimpleDateFormat("yyyy-MM-dd");
                        SimpleDateFormat formateador = new SimpleDateFormat(fad.formato);
                        Date date = parseador.parse(valor);
                        valorFormateado = String.format("%1$-" + largo + "s", formateador.format(date));
                    } else {
                        valorFormateado = String.format("%1$-" + largo + "s", valor);
                    }
                }
                if (fad.tipoDato.compareTo("NUM") == 0) {
                    if (formato.getRellenarCampo().equalsIgnoreCase("S")) {
                        valorFormateado = String.format("%1$" + largo + "s", valor).replace(' ', '0');
                    } else {
                        valorFormateado = valor.replace(' ', '0');
                    }
                }
                if (fad.tipoDato.compareTo("DEC") == 0) {
                    Integer largoParteEntera = largo - fad.decimales;
                    Integer largoParteDecimal = (fad.decimales != null) ? fad.decimales : 0;
                    if (valor.indexOf(".") == -1) {
                        valor = "0";
                    }
                    String[] partes = valor.split("\\.");
                    String parteEntera = (partes.length > 0) ? partes[0] : "0";
                    String parteDecimal = (partes.length > 0) ? partes[1] : "0";
                    if (parteDecimal.length() > largoParteDecimal) {
                        parteDecimal = parteDecimal.trim().substring(0, largoParteDecimal);
                    }
                    if (formato.getRellenarCampo().equalsIgnoreCase("S")) {
                        valorParteEntera = String.format("%1$" + largoParteEntera + "s", parteEntera).replace(' ', '0');
                        valorParteDecimal = String.format("%1$" + largoParteDecimal + "s", parteDecimal).replace(' ', '0');
                    } else {
                        valorParteEntera = parteEntera.replace(' ', '0');
                        valorParteDecimal = parteDecimal.replace(' ', '0');
                    }
                    valorFormateado = valorParteEntera + formato.getSeparadorDecimales() + valorParteDecimal;
                }
            }
        } catch (Exception e) {
            // legacy: printStackTrace; the field stays as it was (empty)
        }
        return valorFormateado;
    }
}
