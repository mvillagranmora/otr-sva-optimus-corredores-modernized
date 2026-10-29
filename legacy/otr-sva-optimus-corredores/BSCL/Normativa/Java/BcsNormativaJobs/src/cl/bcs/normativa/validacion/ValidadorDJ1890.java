package cl.bcs.normativa.validacion;

import java.sql.SQLException;


import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1890.
 *
 * 
 *
 */
public class ValidadorDJ1890 extends Validador {
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1890(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H610.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH610(final Registro registro) {
    	final long c1 = ((Number) registro.getPorCodigoSII("C1")).longValue();
    	final long c2 = ((Number) registro.getPorCodigoSII("C2")).longValue();
    	final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
    	final long c4 = ((Number) registro.getPorCodigoSII("C4")).longValue();
    	final long c5 = ((Number) registro.getPorCodigoSII("C5")).longValue();
    	final long c6 = ((Number) registro.getPorCodigoSII("C6")).longValue();
    	final long c9 = ((Number) registro.getPorCodigoSII("C9")).longValue();
    	final long c10 = ((Number) registro.getPorCodigoSII("C10")).longValue();
    	
    	return c1 + c2 + c3 + c4 + c5 + c6 + c9 + c10 > 0;
    }
    
    
}