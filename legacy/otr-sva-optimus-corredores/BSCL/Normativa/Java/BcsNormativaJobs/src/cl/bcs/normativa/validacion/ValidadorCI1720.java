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
final class ValidadorCI1720 extends Validador {

    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     */
    public ValidadorCI1720(final ContextoValidacion contexto) {
        super(contexto);
    }

    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n MTOVAL.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarMTOVAL(final Registro registro) {

        // La Valorizacion debe ser mayor a cero.

        final Long Valorizacion = (Long) registro.get("Valorizacion");
        return Valorizacion != null && Valorizacion > 0;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n CANTNEG.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarCANTNEG(final Registro registro) {

        // Ningun valor puede ser negativo.
    	
    	final Double CantidadUnidadesNomial =  ((Number) registro.get("CantidadUnidadesNominal")).doubleValue();
        final Long Valorizacion = (Long) registro.get("Valorizacion");
        final Double CustodiaPropia = ((Number) registro.get("CarteraPropia")).doubleValue();
        final Double CustodiaTerceros = ((Number) registro.get("CustodiaTerceros")).doubleValue();
        final Double CustodiaPropiaDCV = ((Number) registro.get("CustodiaPropiaDCV")).doubleValue();
        final Double CustodiaTercerosDCV = ((Number) registro.get("CustodiaTercerosDCV")).doubleValue();
        final Double CustodiaSociedadEmisora = ((Number) registro.get("CustodiaSociedadEmisora")).doubleValue();
        final Double CustodiaIntermediario = ((Number) registro.get("CustodiaIntermediario")).doubleValue();
        final Double CustodiaBolsas = ((Number) registro.get("CustodiaIntermediario")).doubleValue();
        final Double CustodiaOtros = ((Number) registro.get("CustodiaIntermediario")).doubleValue();
        

        return CantidadUnidadesNomial != null && CantidadUnidadesNomial >= 0 &&
        		Valorizacion != null && Valorizacion >= 0 &&
        		CustodiaPropia != null && CustodiaPropia >= 0 &&
        		CustodiaTerceros != null && CustodiaTerceros >= 0 &&
        		CustodiaPropiaDCV != null && CustodiaPropiaDCV >= 0 &&
        		CustodiaTercerosDCV != null && CustodiaTercerosDCV >= 0 &&
        		CustodiaSociedadEmisora != null && CustodiaSociedadEmisora >= 0 &&
        		CustodiaIntermediario != null && CustodiaIntermediario >= 0 &&
        		CustodiaBolsas != null && CustodiaBolsas >= 0 &&
        		CustodiaOtros != null && CustodiaOtros >= 0;
        		
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n CANTOTAL1.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarCANTOTAL1(final Registro registro) {

        //
    	
    	final Double CantidadUnidadesNomial = ((Number) registro.get("CantidadUnidadesNominal")).doubleValue();
        final Double CustodiaPropia = ((Number) registro.get("CarteraPropia")).doubleValue();
        final Double CustodiaTerceros = ((Number) registro.get("CustodiaTerceros")).doubleValue();

        return (CantidadUnidadesNomial) == (CustodiaPropia + CustodiaTerceros);
        		
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n CANTNEG.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarCANTOTAL2(final Registro registro) {

        // Ningun valor puede ser negativo.
    	
    	final Double CantidadUnidadesNomial = ((Number) registro.get("CantidadUnidadesNominal")).doubleValue();
        final Double CustodiaPropiaDCV = ((Number) registro.get("CustodiaPropiaDCV")).doubleValue();
        final Double CustodiaTercerosDCV = ((Number) registro.get("CustodiaTercerosDCV")).doubleValue();
        final Double CustodiaSociedadEmisora = ((Number) registro.get("CustodiaSociedadEmisora")).doubleValue();
        final Double CustodiaIntermediario = ((Number) registro.get("CustodiaIntermediario")).doubleValue();
        final Double CustodiaBolsas = ((Number) registro.get("CustodiaBolsas")).doubleValue();
        final Double CustodiaOtros = ((Number) registro.get("CustodiaOtros")).doubleValue();

        return (CantidadUnidadesNomial) == (CustodiaPropiaDCV + CustodiaTercerosDCV + CustodiaSociedadEmisora + 
        										CustodiaIntermediario + CustodiaBolsas + CustodiaOtros);
        		
    }

    

}
