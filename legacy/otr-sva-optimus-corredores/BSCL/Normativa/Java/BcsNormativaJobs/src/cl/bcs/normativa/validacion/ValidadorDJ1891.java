package cl.bcs.normativa.validacion;

import java.util.Arrays;
import java.util.List;

import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1891.
 *
 * @author fpino@email.com
 *
 */
final class ValidadorDJ1891 extends Validador {

    /**
     * Tipos de documentos de la DJ1891.
     */
    private static final List<String> TIPOS_DOCUMENTOS = Arrays.asList("C", "D", "F", "O");

    /**
     * Tipos de transacciones de la DJ1891.
     */
    private static final List<String> TIPOS_TRANSACCIONES = Arrays.asList("00", "01", "02", "03",
            "04", "05", "06", "07", "08", "09", "10", "11", "21", "29", "31", "32", "33",
            "34", "35", "36", "37", "38", "39", "41", "44", "45", "51", "61", "62", "63", "64", "71","99");

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     */
    public ValidadorDJ1891(final ContextoValidacion contexto) {
        super(contexto);
    }

    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H115.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH115(final Registro registro) {

        // El numero de documento debe ser mayor a cero.

        final Long numeroDocumento = (Long) registro.get("NumeroDocumento");
        return numeroDocumento != null && numeroDocumento > 0;
    }

    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H116.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH116(final Registro registro) {

        // El tipo de Documento debe indicarse con las letras: F: Factura, C:
        // Nota de Credito, D: Nota de Debito, O: Otro (no debe considera
        // documentos nulos).

        final String tipoDocumento = (String) registro.get("TipoDocumento");
        return tipoDocumento != null && TIPOS_DOCUMENTOS.contains(tipoDocumento);
    }

    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H117.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH117(final Registro registro) {

        // El tipo de transaccion debe ser alguno de los siguientes numeros: 0=
        // Comisiones, 1= Acciones, 2= Dolares, 3= Oro, 4= Otras operaciones de
        // Futuro, 5= Opciones , 6= Documentos de Renta Fija, 7= Titulos
        // Emitidos por Bolsa de Productos Agropecuarios sobre Certificados de
        // Depositos de Productos, 8 Cuotas de Fondos de Inversion, 9= Otros
        // Instrumentos de Inversion, 10= Aporte y Adquisicion de Valores (ETF -
        // Exchange Traded Funds), 99= Otros que no sean Instrumentos de
        // Inversion

        final String tipoTransaccion = (String) registro.get("TipoTransaccion");
        return tipoTransaccion != null && TIPOS_TRANSACCIONES.contains(tipoTransaccion);
    }

    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1350.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1350(final Registro registro) {

        // El campo Nemotecnico (C13 ) es un campo obligatorio en caso que el
        // tipo de transaccion sea 1: Acciones o 8: Cuotas de Fondo de
        // Inversion.

        final String tipoTransaccion = (String) registro.get("TipoTransaccion");
        final String nemotecnico = (String) registro.get("Nemotecnico");

        if (tipoTransaccion != null
                && ("01".equals(tipoTransaccion) || "08".equals(tipoTransaccion))) {
            if (nemotecnico == null || nemotecnico.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H611.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH611(final Registro registro) {

        // La fecha del documento debe ser anterior al 01.01.AT y posterior al
        // 31.12.AT-2 y debe ser distinto de blanco.

        return validarFecha(getContexto().getEsquema(), registro);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H3005.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH3005(final Registro registro) {

        // Si el Monto de Adquisicion es mayor a 0.
    	// Entonces el tipo de Transaccion debe ser Acciones y
    	// El Monto de Venta debe ser mayor a 0
        final long montoAdquisicion = ((Number) registro.getPorCodigoSII("C17")).longValue();
        final long montoVenta = ((Number) registro.getPorCodigoSII("C7")).longValue();
        final String tipoTransaccion = (String) registro.get("TipoTransaccion");
        if(montoAdquisicion > 0) {
        	if("01".equals(tipoTransaccion) && montoVenta > 0) {
        		return true;
        	}else {
        		return false;
        	}
        }        
        return true;
    }

     /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H3005.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2211(final Registro registro) {

        // Si Tipo de transacción (C8) es Pacto de Retrocompra (11) , entonces deberá ingresar tanto el monto de la Compra (C6) y la Venta  (C7).

        final long montoCompra = ((Number) registro.getPorCodigoSII("C6")).longValue();
        final long montoVenta = ((Number) registro.getPorCodigoSII("C7")).longValue();
        final String tipoTransaccion = (String) registro.get("TipoTransaccion");
        if("11".equals(tipoTransaccion) ){
           if(montoCompra > 0 && montoVenta > 0){
                return true;
           }else
                return false;
        }
        return true;
    }

}
