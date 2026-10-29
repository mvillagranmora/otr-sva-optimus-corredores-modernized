package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;

import cl.bcs.normativa.data.Registro;

import java.sql.Connection;
import cl.bcs.utils.Log;
import java.sql.CallableStatement;
import java.sql.Types;

/**
 * Implementaci&oacute;n del validador de la DJ1944.
 *
 * 
 *
 */
public class ValidadorDJ1944 extends Validador {

	private final String version;
	private static Connection cnn;
    private static final Log LOG = new Log("MER.ACTUALIZAR_PARAMETROS_FINANCIEROS");

	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1944(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
        cnn = contexto.getConnection();
    }
    
    /**
     * Implementacion de la regla de validacion H2449.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2449(final Registro registro) {
    	final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
    	final long c4 = ((Number) registro.getPorCodigoSII("C4")).longValue();
    	final long c5 = ((Number) registro.getPorCodigoSII("C5")).longValue();
    	final long c6 = ((Number) registro.getPorCodigoSII("C6")).longValue();
    	final long c9 = ((Number) registro.getPorCodigoSII("C9")).longValue();
    	final long c10 = ((Number) registro.getPorCodigoSII("C10")).longValue();
    	final long c11 = ((Number) registro.getPorCodigoSII("C11")).longValue();
    	final long c12 = ((Number) registro.getPorCodigoSII("C12")).longValue();
    	final long c13 = ((Number) registro.getPorCodigoSII("C13")).longValue();
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
    	
    	
    	if( c3 + c4 + c5 + c6 + c9 + c10 + c11 + c12 +c13 +c14 +c15 <= 0) {
    		return false;
    	}else{
    		return true;
        }
    }
    
    /**
     * Implementacion de la regla de validacion H2449.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2464(final Registro registro) {
    	
    	final long c11 = ((Number) registro.getPorCodigoSII("C11")).longValue();
    	final long c12 = ((Number) registro.getPorCodigoSII("C12")).longValue();
    	final long c13 = ((Number) registro.getPorCodigoSII("C13")).longValue();
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();


    	if(c11 != 0 && c12 +c13 +c14 +c15 <= 0) {
    		return false;
    	}else{
    		return true;
        }
    }
    
    public boolean validarH2465(final Registro registro) {
        final String c1 = String.valueOf(registro.getPorCodigoSII("C1")); // Rut cliente
        final String c2 = String.valueOf(registro.getPorCodigoSII("C2")); // Rut institucion
        String resultado;
                
        final String sqlCallValidar = "CALL \"BSCL.Normativa.BD.Servicios::TRI.DJ1944.ValidarH2465\" ('" + c1 + "','" + c2 + "',?);";
    
        try (CallableStatement statement = cnn.prepareCall(sqlCallValidar)) {
            statement.registerOutParameter(1, Types.VARCHAR);
            statement.execute();
            resultado = statement.getString(1);
    
        } catch (SQLException e) {
            LOG.write("Error al ejecutar la consulta SQL: " + e.getMessage());
            resultado = "N";
        }

        if(resultado.trim().toUpperCase().equals("N")) {
            return true;
        } else {
            return false;
        }
    }
}
