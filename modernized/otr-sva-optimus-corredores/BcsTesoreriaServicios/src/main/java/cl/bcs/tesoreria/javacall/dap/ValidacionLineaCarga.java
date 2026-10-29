package cl.bcs.tesoreria.javacall.dap;

import java.util.List;
import java.util.Set;

/**
 * Field validation of one 39-field line of the DAP/FFMM income/expense mass load (legacy {@code validarDetalles}, TES-B-181, 182, 189).
 * The text of every message is the legacy text (it is written to the output file) and the messages are concatenated in the legacy order.
 */
public final class ValidacionLineaCarga {

    private static final String FECHA = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final int CAMPOS = 39;

    private ValidacionLineaCarga() {
    }

    /**
     * @param lineas  the fields of the line (split with the limit -1: empty fields are kept)
     * @param conceptoValido     the concept of field 9 is configured and active for treasury income
     * @param tipoMovimientoConocido the movement type of field 1 exists for the treasury module
     * @return the concatenated messages, empty when the line is valid
     */
    public static String validar(List<String> lineas, boolean conceptoValido, boolean tipoMovimientoConocido) {
        if (lineas.size() < CAMPOS) {
            // LEGACY DEFECT kept (see README TES-B-182): the caller stores this text under index 0, not under the index of the line
            return "El registro no tiene la cantidad minima de valores. ";
        }
        StringBuilder v = new StringBuilder();
        if (lineas.get(7).contains(",") || lineas.get(19).contains(",")) {
            v.append("Deben utilizarse . para los separadores de decimales en los campos de Montos. ");
        }
        if (!lineas.get(8).matches(FECHA) || !lineas.get(21).matches(FECHA) || !lineas.get(25).matches(FECHA) || !lineas.get(37).matches(FECHA)) {
            v.append("Formato invalido de Fechas. Debe ser YYYY-MM-DD. ");
        }
        if (lineas.get(0).isEmpty()) {
            v.append("El campo NumeroReferencia (en la posicion 0) es obligatorio. ");
        }
        if (lineas.get(1).isEmpty()) {
            v.append("El campo IdTipoMovimiento (en la posicion 1) es obligatorio. ");
        }
        if (!lineas.get(1).isEmpty() && !tipoMovimientoConocido) {
            v.append("El Tipo de Movimiento no es valido. ");
        }
        vacio(v, lineas, 2, "IdTipoPersona");
        vacio(v, lineas, 3, "TipoDocumentoIdentidad");
        vacio(v, lineas, 4, "DocumentoIdentidad");
        vacio(v, lineas, 5, "CodigoPortafolio");
        vacio(v, lineas, 6, "IdMoneda");
        vacioOCero(v, lineas, 7, "Monto");
        vacioOCero(v, lineas, 8, "FechaLiquidacion");
        vacioOCero(v, lineas, 9, "IdConcepto");
        if (!conceptoValido) {
            v.append("El Concepto ").append(lineas.get(9)).append(" no se encuentra configurado o Vigente en el Sistema para Ingresos de Tesoreria. ");
        }
        vacio(v, lineas, 10, "SecuenciaAgrupacionComprobante");
        vacio(v, lineas, 11, "Glosa");
        siNo(v, lineas, 12, "GeneraComprobanteAutomatico");
        siNo(v, lineas, 13, "LiquidarComprobanteAutomatico");
        siNo(v, lineas, 14, "AgruparEnComprobante");
        vacio(v, lineas, 15, "NumeroReferenciaFormaDePago");
        if (!lineas.get(15).isEmpty() && !lineas.get(0).isEmpty() && !lineas.get(15).equals(lineas.get(0))) {
            v.append("El campo NumeroReferenciaFormaDePago debe ser igual al IdInterno del registro. ");
        }
        vacioOCero(v, lineas, 16, "IdMedioDePago");
        vacioOCero(v, lineas, 17, "IdModalidadDePago");
        vacioOCero(v, lineas, 18, "IdMonedaFormaDePago");
        vacioOCero(v, lineas, 19, "MontoFormaDePago");
        vacio(v, lineas, 20, "NumeroDocumentoCorredora");
        vacio(v, lineas, 21, "FechaDocumentoPagoCorredora");
        vacio(v, lineas, 24, "NumeroCuentaBancariaSocio");
        vacio(v, lineas, 25, "FechaDocumentoPagoSocio");
        vacioOCero(v, lineas, 26, "IdCuentaBancariaCorredora");
        vacioOCero(v, lineas, 34, "IdTipoProducto");
        vacioOCero(v, lineas, 35, "IdTipoOperacion");
        vacioOCero(v, lineas, 36, "FolioOperacion");
        vacioOCero(v, lineas, 37, "FechaOperacion");
        vacioOCero(v, lineas, 38, "IdCondicionDeLiquidacion");
        if (v.length() > 0) {
            v.append("Por lo que no se realizara el procesamiento del registro.");
        }
        return v.toString();
    }

    private static void vacio(StringBuilder v, List<String> lineas, int posicion, String campo) {
        if (lineas.get(posicion).isEmpty()) {
            obligatorio(v, posicion, campo);
        }
    }

    private static void vacioOCero(StringBuilder v, List<String> lineas, int posicion, String campo) {
        if (Set.of("", "0").contains(lineas.get(posicion))) {
            obligatorio(v, posicion, campo);
        }
    }

    private static void siNo(StringBuilder v, List<String> lineas, int posicion, String campo) {
        if (!"N".equals(lineas.get(posicion)) && !"S".equals(lineas.get(posicion))) {
            v.append("El Campo ").append(campo).append(" (en la posicion ").append(posicion).append(") permite valores S o N. ");
        }
    }

    private static void obligatorio(StringBuilder v, int posicion, String campo) {
        v.append("El campo ").append(campo).append(" (en la posicion ").append(posicion).append(") es obligatorio. ");
    }
}
