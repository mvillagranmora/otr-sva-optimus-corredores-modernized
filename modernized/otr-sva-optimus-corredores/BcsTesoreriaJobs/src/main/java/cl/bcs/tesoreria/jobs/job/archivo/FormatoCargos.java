package cl.bcs.tesoreria.jobs.job.archivo;

import cl.bcs.tesoreria.javacall.ConsultarFormatoArchivoService.FormatoArchivoDetalle;
import cl.bcs.tesoreria.javacall.ConsultarFormatoArchivoService.SalidaFormatoArchivo;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Field formatter of the PAC charge file. Legacy: {@code cl.bcs.tesoreria.generarArchivoCargos.FormatoArchivo} (106 lines), ported
 * method by method; its quirks are kept on purpose (README, TES-J-DEF-04): a DEC value without '.' is read as 0, {@code ValorDefecto}
 * is ignored for CHAR / NUM / DEC / DATE, DEC integer digits above the width lose their leading digits, a null value is a
 * {@link NullPointerException}, and {@code rellenarCampo} must be exactly "S". The debug {@code main()} is not ported (RULE-076).
 */
public final class FormatoCargos {

    private final SalidaFormatoArchivo formatoArchivo;

    public FormatoCargos(SalidaFormatoArchivo formatoArchivo) {
        this.formatoArchivo = formatoArchivo;
    }

    /** :20-29 */
    private FormatoArchivoDetalle getFormatoArchivoDetalle(int typeRegister, String fieldName) {
        if (this.formatoArchivo.Detalles.isEmpty()) {
            return null;
        }
        for (FormatoArchivoDetalle detalle : this.formatoArchivo.Detalles) {
            if (detalle.TipoRegistro == typeRegister && detalle.Campo.equals(fieldName)) {
                return detalle;
            }
        }
        return null;
    }

    /** :31-35 */
    private static String stringFormat(String value, Integer length) {
        if (value.length() > length) {
            value = value.substring(0, length);
        }
        return String.format("%1$-" + (length) + "s", value);
    }

    /** :37-45 */
    private static String dateFormat(String value, Integer length, String formatDetail) throws Exception {
        if (formatDetail == null || formatDetail.equals("")) {
            return String.format("%1$-" + length + "s", value);
        }
        SimpleDateFormat parseador = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formateador = new SimpleDateFormat(formatDetail);
        Date date = parseador.parse(value);
        return String.format("%1$-" + length + "s", formateador.format(date));
    }

    /** :47-49 */
    private static String numberFormat(String value, Integer length) {
        return String.format("%1$" + length + "s", value).replace(' ', '0');
    }

    /** :51-68 */
    private static String decimalFormat(String value, Integer length, Integer decimals, String fieldName) {
        if (value.indexOf(".") == -1) { // legacy: "OE-80" and every value without '.' becomes 0
            value = "0";
        }
        Double d = Double.parseDouble(value);
        DecimalFormat df = new DecimalFormat();
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        df.setGroupingUsed(false);
        df.setMinimumIntegerDigits(length - decimals);
        df.setMinimumFractionDigits(decimals);
        df.setMaximumIntegerDigits(length - decimals);
        df.setMaximumFractionDigits(decimals);
        df.setDecimalFormatSymbols(symbol);
        df.setRoundingMode(RoundingMode.DOWN);
        if (fieldName.equals("pac-monto-cargo")) {
            return df.format(d).replace(".", "");
        }
        return df.format(d);
    }

    /** :70-76 */
    private static String initializeValue(String value, String defaultValue) {
        if (value == null) {
            value = "";
        }
        if (value.length() < 1 && defaultValue.compareTo("") != 0) {
            return defaultValue.trim();
        }
        return value.trim();
    }

    /** :78-96 */
    public String formatear(Integer tipoRegistro, String nombreCampo, String valor, Boolean rellenarCampo) throws Exception {
        FormatoArchivoDetalle detalle = this.getFormatoArchivoDetalle(tipoRegistro, nombreCampo);
        if (detalle == null) {
            throw new Exception("NO EXISTE FORMATO DETALLE PARA EL ARCHIVO A CREAR");
        }
        Integer largo = detalle.Largo;
        String tipoDato = detalle.TipoDato;
        String valorFormateado = initializeValue(valor, detalle.ValorDefecto);
        if (rellenarCampo == false) {
            valorFormateado = valor;
        }
        if (tipoDato.equals("CHAR") && rellenarCampo) {
            valorFormateado = stringFormat(valor, largo);
        }
        if (tipoDato.equals("DATE")) {
            valorFormateado = dateFormat(valor, largo, detalle.Formato);
        }
        if (tipoDato.equals("NUM") && rellenarCampo) {
            valorFormateado = numberFormat(valor, largo);
        }
        if (tipoDato.equals("DEC") && rellenarCampo) {
            valorFormateado = decimalFormat(valor, largo, detalle.Decimales, nombreCampo);
        }
        return valorFormateado;
    }
}
