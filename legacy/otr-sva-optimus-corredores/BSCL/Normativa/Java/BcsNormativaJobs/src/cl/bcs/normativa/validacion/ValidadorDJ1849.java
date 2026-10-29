package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1849.
 *
 * 
 *
 */
public class ValidadorDJ1849 extends Validador {

	private static final List<String> TIPO_DECLARANTE = Arrays.asList("1","2","3");
	private static final List<String> RETENCION_LIBERACION = Arrays.asList("1","2");
	private static final List<String> TIPO_CONTRIBUYENTE_RUTTENEDOR = Arrays.asList("1","2");
		
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1849(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1273.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1273(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
    	return !c3.isEmpty() && !c4.isEmpty() && !c6.isEmpty();
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1274.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1274(final Registro registro) {
    	final long c7 = ((Number) registro.getPorCodigoSII("C7")).longValue();
    	return c7 > 0;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1275.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1275(final Registro registro) {
    	final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
    	return TIPO_DECLARANTE.contains(c2);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1278.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1278(final Registro registro) {
    	final String c8 = String.valueOf(registro.getPorCodigoSII("C8"));
    	return RETENCION_LIBERACION.contains(c8);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1279.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1279(final Registro registro) {
    	final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
    	return TIPO_CONTRIBUYENTE_RUTTENEDOR.contains(c5);
    }
    
}