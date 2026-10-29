package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;


import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1894.
 *
 * 
 *
 */
public class ValidadorDJ1894 extends Validador {

	private static final List<String> TIPO_OPERACION = Arrays.asList("1", "2", "3","4","5","6","7","8","9");
	private static final List<String> TIPO_OPERACION_INVERSION = Arrays.asList("1", "2", "3");
	private static final List<String> TIPO_OPERACION_REINVERSION = Arrays.asList("8", "9");
	private static final List<String> TIPO_OPERACION_RESCATE = Arrays.asList("4", "5","6","7");
	private static final List<String> TIPO_DECLARANTE = Arrays.asList("1","2");
	
	private final String version;
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1894(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H063.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH063(final Registro registro) {
    	final String c4 = (String) registro.getPorCodigoSII("C4");
    	final String[] rut = c4.toLowerCase().split("-");
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
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H064.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH064(final Registro registro) {
    	final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
    	return c5 != null && TIPO_OPERACION.contains(c5);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H259.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH259(final Registro registro) {
    	final long c7 = ((Number) registro.getPorCodigoSII("C7")).longValue();
    	final long c9 = ((Number) registro.getPorCodigoSII("C9")).longValue();
    	final long c10 = ((Number) registro.getPorCodigoSII("C10")).longValue();
    	final long c12 = ((Number) registro.getPorCodigoSII("C12")).longValue();
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	
    	return (c7 + c9 +c10 + c12 + c14) > 0;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H842.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH842(final Registro registro) {
    	final Date c6 = (Date) registro.getPorCodigoSII("C6");
    	final long c7 = ((Number) registro.getPorCodigoSII("C7")).longValue();
    	final long c9 = ((Number) registro.getPorCodigoSII("C9")).longValue();
    	final long c10 = ((Number) registro.getPorCodigoSII("C10")).longValue();
    	final long c12 = ((Number) registro.getPorCodigoSII("C12")).longValue();
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	
    	try{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			final Date date = formatter.parse("1900-01-01");
			if(date.compareTo(c6) != 0){
				return c7 > 0 && (c9 +c10 + c12 + c14) == 0;
			}
		}catch(Exception e){
			return false;
		}
		
    	return true;
		
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H843.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH843(final Registro registro) {
    	final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
    	final Date c6 = (Date) registro.getPorCodigoSII("C6");
    	
    	if(c5 != null && TIPO_OPERACION_INVERSION.contains(c5)){
    		try{
    			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    	    	//String anio = String.valueOf(Integer.parseInt(version)-1);
    	    	String anio = String.valueOf(Integer.parseInt(version));
    			final Date dateInicio = formatter.parse(anio + "-01-01");
    			final Date dateFin = formatter.parse(anio + "-12-31");
    			return (dateInicio.compareTo(c6) < 0 || dateInicio.compareTo(c6) == 0) && (dateFin.compareTo(c6) > 0 || dateFin.compareTo(c6) == 0);
    		}catch(Exception e){
    			return false;
    		}
    	}
    	
    	return true;
		
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H845.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH845(final Registro registro) {
    	final String tipoDeraclante = String.valueOf(registro.get("TipoDeclarante"));
    	return TIPO_DECLARANTE.contains(tipoDeraclante);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H962.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH962(final Registro registro) {
    	final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
    	final Date c6 = (Date) registro.getPorCodigoSII("C6");
    	final long c7 = ((Number) registro.getPorCodigoSII("C7")).longValue();    	
    	if(c5 != null && TIPO_OPERACION_INVERSION.contains(c5)){
    		try{
    			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    			final Date date = formatter.parse("0001-01-01");
    			return date.compareTo(c6) != 0 && c7 > 0;
    		}catch(Exception e){
    			return false;
    		}
    	}
    	
    	return true;
		
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H964.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH964(final Registro registro) {
    	final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	if(c5 != null && TIPO_OPERACION_REINVERSION.contains(c5)){
    		return c14 > 0;
    	}
    	
    	return true;
		
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H965.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH965(final Registro registro) {
    	final String c1 = (String) registro.getPorCodigoSII("C1");
    	
    	if(!c1.isEmpty()){
    		String[] arr = c1.split("-");
    		if(arr.length != 2) return false;
    		int rut = Integer.parseInt(arr[0]);
    		return rut >= 50000000;
    	}
    	
    	return true;
		
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H966.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH966(final Registro registro) {
    	final String c13 = (String) registro.getPorCodigoSII("C13");
    	
		if(!c13.isEmpty()){
    		String[] arr = c13.split("-");
    		if(arr.length != 2){
				return false;
			} else{
				int rut = Integer.parseInt(arr[0]);
				return rut >= 50000000;
			}
    	}
    	
    	return true;
		
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1291.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1291(final Registro registro) {
    	final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
    	final Date c8 = (Date) registro.getPorCodigoSII("C8");
    	
    	if(c5 != null && TIPO_OPERACION_RESCATE.contains(c5)){
    		try{
    			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    	    	//String anio = String.valueOf(Integer.parseInt(version)-1);
    	    	String anio = String.valueOf(Integer.parseInt(version));
    			final Date dateInicio = formatter.parse(anio + "-01-01");
    			final Date dateFin = formatter.parse(anio + "-12-31");
    			return (dateInicio.compareTo(c8) < 0 || dateInicio.compareTo(c8) == 0) &&(dateFin.compareTo(c8) > 0 || dateFin.compareTo(c8) == 0);
    		}catch(Exception e){
    			return false;
    		}
    	}
    	
    	return true;		
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1292.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1292(final Registro registro) {
    	final Date c8 = (Date) registro.getPorCodigoSII("C8");
    	final long c9 = ((Number) registro.getPorCodigoSII("C9")).longValue();
    	
    	try{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			final Date date = formatter.parse("1900-01-01");
			if(date.compareTo(c8) != 0 ){
					return c9 > 0;
			}	
		}catch(Exception e){
			return false;
		}
    	
    	
    	return true;
		
    }
    
    
}