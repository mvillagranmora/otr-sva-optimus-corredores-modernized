package cl.bcs.normativa.validacion;

import java.sql.SQLException;


import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1823.
 *
 * 
 *
 */
public class ValidadorDJ1823 extends Validador {
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1823(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H050.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH050(final Registro registro) {
    	final long c4 = ((Number) registro.getPorCodigoSII("C4")).longValue();
    	final long c5 = ((Number) registro.getPorCodigoSII("C5")).longValue();
    	if(c4 >0) return c5 == 0;
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H607.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH607(final Registro registro) {
    	final long c4 = ((Number) registro.getPorCodigoSII("C4")).longValue();
    	final long c5 = ((Number) registro.getPorCodigoSII("C5")).longValue();
    	if(c5 >0) return c4 == 0;
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1996.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1996(final Registro registro) {
    	final long c12 = ((Number) registro.getPorCodigoSII("C12")).longValue();
    	final long c13 = ((Number) registro.getPorCodigoSII("C13")).longValue();
    	if(c12 >0) return c13 == 0;
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1997.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1997(final Registro registro) {
    	final long c12 = ((Number) registro.getPorCodigoSII("C12")).longValue();
    	final long c13 = ((Number) registro.getPorCodigoSII("C13")).longValue();
    	if(c13 >0) return c12 == 0;
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1998.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1998(final Registro registro) {
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
    	if(c14 >0) return c15 == 0;
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1999.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1999(final Registro registro) {
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
    	if(c15 >0) return c14 == 0;
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H2011.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2011(final Registro registro) {
    	final long c17 = ((Number) registro.getPorCodigoSII("C17")).longValue();
    	final long c18 = ((Number) registro.getPorCodigoSII("C18")).longValue();
    	final long c19 = ((Number) registro.getPorCodigoSII("C19")).longValue();
    	final long c20 = ((Number) registro.getPorCodigoSII("C20")).longValue();
    	final long c21 = ((Number) registro.getPorCodigoSII("C21")).longValue();
    	if(c17 >0) return c18 + c19 + c20 + c21 > 0;
    	return true;
    }
    
}