package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;


import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1944.
 *
 * 
 *
 */
public class ValidadorDJ1949 extends Validador {

	private final String version;
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1949(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
    }
    
    /**
     * Implementacion de la regla de validacion H2827.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2827(final Registro registro) {
    	final Date c1 = (Date) registro.getPorCodigoSII("C1");
    	
    	if(c1 == null) {
    		return false;
    	}
    	return true;
    }
    
    /**
     * Implementacion de la regla de validacion H2857.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2857(final Registro registro) {
    	
    	try{
    	
			final long c9 = ((Number) registro.getPorCodigoSII("C9")).longValue();
			final long c6 = ((Number) registro.getPorCodigoSII("C6")).longValue();
			final long c7 = ((Number) registro.getPorCodigoSII("C7")).longValue();
			final long c8 = ((Number) registro.getPorCodigoSII("C8")).longValue();
			final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
			final long c18 = ((Number) registro.getPorCodigoSII("C18")).longValue();
			final long c19 = ((Number) registro.getPorCodigoSII("C19")).longValue();
			final long c20 = ((Number) registro.getPorCodigoSII("C20")).longValue();
			final long c21 = ((Number) registro.getPorCodigoSII("C21")).longValue();
			final long c22 = ((Number) registro.getPorCodigoSII("C22")).longValue();
			final long c23 = ((Number) registro.getPorCodigoSII("C23")).longValue();
			final long c24 = ((Number) registro.getPorCodigoSII("C24")).longValue();
			final long c25 = ((Number) registro.getPorCodigoSII("C25")).longValue();
			final long c26 = ((Number) registro.getPorCodigoSII("C26")).longValue();
			final long c27 = ((Number) registro.getPorCodigoSII("C27")).longValue();
			final long c28 = ((Number) registro.getPorCodigoSII("C28")).longValue();
			final long c29 = ((Number) registro.getPorCodigoSII("C29")).longValue();
			final long c30 = ((Number) registro.getPorCodigoSII("C30")).longValue();
			final long c31 = ((Number) registro.getPorCodigoSII("C31")).longValue();
			final long c32 = ((Number) registro.getPorCodigoSII("C32")).longValue();
			
			if(c9 >0 && c6==0 && c7==0 && c8==0 && c15==0 && (c18+c19+c20+c21+c22+c23+c24+c25+c26+c27+c28+c29+c30+c31+c32)>0) {
				return false;
			}
			return true;
			
		} catch (Exception e) {
    		return false;
    	}
    }
    
    /**
     * Implementacion de la regla de validacion H2858.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2858(final Registro registro) {
		try{
			final long c10 = ((Number) registro.getPorCodigoSII("C10")).longValue();
			final long c11 = ((Number) registro.getPorCodigoSII("C11")).longValue();
			final long c12 = ((Number) registro.getPorCodigoSII("C12")).longValue();
			final long c13 = ((Number) registro.getPorCodigoSII("C13")).longValue();
			final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
			final long c16 = ((Number) registro.getPorCodigoSII("C16")).longValue();
			final long c6 = ((Number) registro.getPorCodigoSII("C6")).longValue();
			final long c7 = ((Number) registro.getPorCodigoSII("C7")).longValue();
			final long c8 = ((Number) registro.getPorCodigoSII("C8")).longValue();
			final long c9 = ((Number) registro.getPorCodigoSII("C9")).longValue();
			final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
			final long c18 = ((Number) registro.getPorCodigoSII("C18")).longValue();
			final long c19 = ((Number) registro.getPorCodigoSII("C19")).longValue();
			final long c20 = ((Number) registro.getPorCodigoSII("C20")).longValue();
			final long c21 = ((Number) registro.getPorCodigoSII("C21")).longValue();
			final long c22 = ((Number) registro.getPorCodigoSII("C22")).longValue();
			final long c23 = ((Number) registro.getPorCodigoSII("C23")).longValue();
			final long c24 = ((Number) registro.getPorCodigoSII("C24")).longValue();
			final long c25 = ((Number) registro.getPorCodigoSII("C25")).longValue();
			final long c26 = ((Number) registro.getPorCodigoSII("C26")).longValue();
			final long c27 = ((Number) registro.getPorCodigoSII("C27")).longValue();
			final long c28 = ((Number) registro.getPorCodigoSII("C28")).longValue();
			final long c29 = ((Number) registro.getPorCodigoSII("C29")).longValue();
			final long c30 = ((Number) registro.getPorCodigoSII("C30")).longValue();
			final long c31 = ((Number) registro.getPorCodigoSII("C31")).longValue();
			final long c32 = ((Number) registro.getPorCodigoSII("C32")).longValue();
			
			if((c10+c11+c12+c13+c14+c16)>0 && c6==0 && c7==0 && c8==0 && c9>=0 && c15==0 && (c18+c19+c19+c20+c21+c22+c23+c24+c25+c26+c27+c28+c29+c30+c31+c32)!=0) {
				return false;
			}
			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }
    
    /**
     * Implementacion de la regla de validacion H2859.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2859(final Registro registro) {
		try{
			final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
			final long c6 = ((Number) registro.getPorCodigoSII("C6")).longValue();
			final long c7 = ((Number) registro.getPorCodigoSII("C7")).longValue();
			final long c8 = ((Number) registro.getPorCodigoSII("C8")).longValue();
			final long c9 = ((Number) registro.getPorCodigoSII("C9")).longValue();
			final long c18 = ((Number) registro.getPorCodigoSII("C18")).longValue();
			final long c19 = ((Number) registro.getPorCodigoSII("C19")).longValue();
			final long c20 = ((Number) registro.getPorCodigoSII("C20")).longValue();
			final long c21 = ((Number) registro.getPorCodigoSII("C21")).longValue();
			final long c22 = ((Number) registro.getPorCodigoSII("C22")).longValue();
			final long c23 = ((Number) registro.getPorCodigoSII("C23")).longValue();
			final long c26 = ((Number) registro.getPorCodigoSII("C26")).longValue();
			final long c27 = ((Number) registro.getPorCodigoSII("C27")).longValue();
			final long c28 = ((Number) registro.getPorCodigoSII("C28")).longValue();
			final long c31 = ((Number) registro.getPorCodigoSII("C31")).longValue();
			final long c32 = ((Number) registro.getPorCodigoSII("C32")).longValue();
			
			if(c15>0 && c6==0 && c7==0 && c8==0 && c9==0 && (c18+c19+c20+c21+c22+c23+c26+c27+c28+c31+c32)!=0 ) {
				return false;
			}
			return true;
		} catch (Exception e) {
    		return false;
    	}
    }
    
    /**
     * Implementacion de la regla de validacion H2860.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2860(final Registro registro) {
    	try {
        	final long c17 = ((Number) registro.getPorCodigoSII("C17")).longValue();
        	final long c6 = ((Number) registro.getPorCodigoSII("C6")).longValue();
        	final long c7 = ((Number) registro.getPorCodigoSII("C7")).longValue();
        	final long c8 = ((Number) registro.getPorCodigoSII("C8")).longValue();
        	final long c18 = ((Number) registro.getPorCodigoSII("C18")).longValue();
        	final long c19 = ((Number) registro.getPorCodigoSII("C19")).longValue();
        	final long c20 = ((Number) registro.getPorCodigoSII("C20")).longValue();
        	final long c21 = ((Number) registro.getPorCodigoSII("C21")).longValue();
        	final long c22 = ((Number) registro.getPorCodigoSII("C22")).longValue();
        	final long c23 = ((Number) registro.getPorCodigoSII("C23")).longValue();
        	final long c26 = ((Number) registro.getPorCodigoSII("C26")).longValue();
        	final long c27 = ((Number) registro.getPorCodigoSII("C27")).longValue();
        	final long c28 = ((Number) registro.getPorCodigoSII("C28")).longValue();
        	final long c31 = ((Number) registro.getPorCodigoSII("C31")).longValue();
        	final long c32 = ((Number) registro.getPorCodigoSII("C32")).longValue();
        	
        	if(c17>0 && c6==0 && c7==0 && c8==0 && (c18+c19+c20+c21+c22+c23+c26+c27+c28+c31+c32)!=0  ) {
        		return false;
        	}
        	
    		return true;
    	} catch (Exception e) {
    		return false;
    	}
    }
    
    /**
     * Implementacion de la regla de validacion H2861.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2861(final Registro registro) {
    	final String c2 = (String) registro.getPorCodigoSII("C2");
  
		return validarRut(c2);
    }
    
    /**
     * Implementacion de la regla de validacion H2862.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2862(final Registro registro) {
    	
    	final String c4 = (String) registro.getPorCodigoSII("C4");
    	  
		return validarRut(c4);
    }
    
    /**
     * Implementacion de la regla de validacion H2863.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2863(final Registro registro) {
		try{
			final long c6 = ((Number) registro.getPorCodigoSII("C6")).longValue();
			final long c7 = ((Number) registro.getPorCodigoSII("C7")).longValue();
			final long c8 = ((Number) registro.getPorCodigoSII("C8")).longValue();
			final long c9 = ((Number) registro.getPorCodigoSII("C9")).longValue();
			final long c10 = ((Number) registro.getPorCodigoSII("C10")).longValue();
			final long c11 = ((Number) registro.getPorCodigoSII("C11")).longValue();
			final long c12 = ((Number) registro.getPorCodigoSII("C12")).longValue();
			final long c13 = ((Number) registro.getPorCodigoSII("C13")).longValue();
			final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
			final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
			final long c16 = ((Number) registro.getPorCodigoSII("C16")).longValue();
			final long c17 = ((Number) registro.getPorCodigoSII("C17")).longValue();
			final String c4 = (String) registro.getPorCodigoSII("C4");
			
			if((c6+c7+c8+c9+c10+c11+c12+c13+c14+c15+c16+c17)>0 && c4.isEmpty() ) {
				return false;
			}
			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }
    
    private boolean validarRut(String registro){
    	final String[] rut = registro.split("-");
    	if(rut.length != 2) return false;
    	List<String> numeros = Arrays.asList(rut[0].split(""));
    	String verificador = rut[1];
    	Collections.reverse(numeros);
    	int a = 2;
    	int resultado = 0;
    	for(String numero : numeros){
    		if(numero.isEmpty()) continue;
    		int num = Integer.parseInt(numero);
    		resultado += num * a;
    		if(a == 7){
    			a = 1;
    		}
    		a++;
    	}
    	int resto = resultado % 11;
    	int digito = 11 - resto;
    	String resultadoVerificador = String.valueOf(digito);
    	if(digito == 11) resultadoVerificador = "0";
    	if(digito == 10) resultadoVerificador = "k";
    	return resultadoVerificador.equalsIgnoreCase(verificador);
    }
    
    
    
    
}