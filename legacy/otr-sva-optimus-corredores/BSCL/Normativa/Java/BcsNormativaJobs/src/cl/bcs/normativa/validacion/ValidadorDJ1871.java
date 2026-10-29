package cl.bcs.normativa.validacion;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.declaraciones.DJ1871.AjustarRescatesDelPeriodo.SaldoPeriodoAnterior;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;

/**
 * Implementaci&oacute;n del validador de la DJ1871.
 *
 * 
 *
 */
public class ValidadorDJ1871 extends Validador {
	private final String version;
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1871(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
    }
  
    
    /**
     * Implementacion de la regla de validacion H3.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1703(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	final long c4 = ((Number) registro.getPorCodigoSII("C4")).longValue();
    	final long c7 = ((Number) registro.getPorCodigoSII("C7")).longValue();
    	String anio = String.valueOf(Integer.parseInt(version));
    	
    	if( c3.equals(anio) && c4 == 0 && c7 > 0) {
    		return false;
    	}else
    		return true;
    }
    
    /**
     * Implementacion de la regla de validacion H3.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1704(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	final long c5 = ((Number) registro.getPorCodigoSII("C5")).longValue();
    	final long c8 = ((Number) registro.getPorCodigoSII("C8")).longValue();
    	String anio = String.valueOf(Integer.parseInt(version));
    	
    	if( c3.equals(anio) && c5 == 0 && c8 > 0) {
    		return false;
    	}else
    		return true;
    }
    
    /**
     * Implementacion de la regla de validacion H3.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1705(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	final long c6 = ((Number) registro.getPorCodigoSII("C6")).longValue();
    	final long c9 = ((Number) registro.getPorCodigoSII("C9")).longValue();
    	String anio = String.valueOf(Integer.parseInt(version));
    	
    	if( c3.equals(anio) && c6 == 0 && c9 > 0) {
    		return false;
    	}else
    		return true;
    }
    
    /**
     * Implementacion de la regla de validacion H3.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1706(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	final long c4 = ((Number) registro.getPorCodigoSII("C4")).longValue();
    	final long c7 = ((Number) registro.getPorCodigoSII("C7")).longValue();
    	String anio = String.valueOf(Integer.parseInt(version));
    	
    	if( c3.equals(anio) && c4 > 0 && c7 > c4) {
    		return false;
    	}else
    		return true;
    }
    /**
     * Implementacion de la regla de validacion H3.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    
    public boolean validarH1707(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	final long c5 = ((Number) registro.getPorCodigoSII("C5")).longValue();
    	final long c8 = ((Number) registro.getPorCodigoSII("C8")).longValue();
    	String anio = String.valueOf(Integer.parseInt(version));
    	
    	if( c3.equals(anio) && c5 > 0 && c8 > c5) {
    		return false;
    	}else
    		return true;
    }
    
    /**
     * Implementacion de la regla de validacion H3.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1708(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	final long c6 = ((Number) registro.getPorCodigoSII("C6")).longValue();
    	final long c9 = ((Number) registro.getPorCodigoSII("C9")).longValue();
    	String anio = String.valueOf(Integer.parseInt(version));
    	
    	if( c3.equals(anio) && c6 > 0 && c9 > c6) {
    		return false;
    	}else
    		return true;
    }
    
    public boolean validarH2724(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	final long c7 = ((Number) registro.getPorCodigoSII("C6")).longValue();
    	final long c8 = ((Number) registro.getPorCodigoSII("C9")).longValue();
    	final long c9 = ((Number) registro.getPorCodigoSII("C9")).longValue();
    	int anio = Integer.parseInt(version);
    	int columnaC3 = Integer.parseInt(c3);
    	
    	
    	
    	if( columnaC3<anio && c7+c8+c9>0) {
    		return false;
    	}else
    		return true;
    }
    

}