package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;


import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1914.
 *
 * 
 *
 */
public class ValidadorDJ1914 extends Validador {

	private static final List<String> TIPO_INSTRUMENTO = Arrays.asList("1", "2", "3","4","5");

	
	private final String version;
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1914(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1810.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1810(final Registro registro) {
    	final String c1 = String.valueOf(registro.getPorCodigoSII("C1"));
    	
        return (!c1.isEmpty() && !"0".equalsIgnoreCase(c1)) && validarRut(c1);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1811.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1811(final Registro registro) {
    	final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
    	
    	return TIPO_INSTRUMENTO.contains(c2);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1812.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1812(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));;
    	
    	return !c3.isEmpty();
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1813.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1813(final Registro registro) {
    	final Date c4 = (Date) registro.getPorCodigoSII("C4");
    	try{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    	String anio = String.valueOf(Integer.parseInt(version)-1);
			final Date dateInicio = formatter.parse(anio + "-01-01");
			final Date dateFin = formatter.parse(anio + "-12-31");
			return (dateInicio.compareTo(c4) < 0 || dateInicio.compareTo(c4) == 0) && (dateFin.compareTo(c4) > 0 || dateFin.compareTo(c4) == 0);
		}catch(Exception e){
			return false;
		}
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1814.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1814(final Registro registro) {
    	final long c8= ((Number) registro.getPorCodigoSII("C8")).longValue();
    	final long c9 = ((Number) registro.getPorCodigoSII("C9")).longValue();
    	final long c10 = ((Number) registro.getPorCodigoSII("C10")).longValue();
    	return c8 == 0 && c9 == 0 && c10 == 0;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1815.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1815(final Registro registro) {
    	final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
    	final long c5 = ((Number) registro.getPorCodigoSII("C10")).longValue();
    	if(TIPO_INSTRUMENTO.contains(c2)) return c5 > 0;
    	return true;
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