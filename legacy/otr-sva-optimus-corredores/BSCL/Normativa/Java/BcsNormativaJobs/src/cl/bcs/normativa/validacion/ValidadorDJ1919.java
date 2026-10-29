package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1919.
 *
 * 
 *
 */
public class ValidadorDJ1919 extends Validador {

	private static final List<String> PARTICIPACION_DIRECTA_INDIRECTA = Arrays.asList("1","2");
	private static final List<String> PAIS_RESIDENCIA = Arrays.asList("AF"	,"AL"	,"AT"	,"AU"	,"BE"	,"BM"	,"BO"	,"BR"	,"BS"	
																	 ,"CA"	,"CL"	,"CN"	,"CO"	,"CR"	,"CU"	,"CH"	,"DE"	,"DK"
																	 ,"DM"	,"DO"	,"EC"	,"ES"	,"FI"	,"FR"	,"IG"	,"GR"	,"HK"
																	 ,"ID"	,"IE"	,"IN"	,"KY"	,"IT"	,"JP"	,"KR"	,"LU"	,"NL"
																	 ,"MT"	,"MX"	,"MY"	,"NE"	,"NG"	,"NI"	,"NL"	,"NO"	,"NZ"
																	 ,"PA"	,"PE"	,"PH"	,"PK"	,"PL"	,"PR"	,"PT"	,"PY"	,"RO"
																	 ,"SA"	,"SE"	,"SG"	,"TR"	,"TT"	,"TW"	,"TZ"	,"UA"	,"UG"
																	 ,"US"	,"UY"	,"VE"	,"VN"	,"ZA"	,"HR"	,"CZ"	,"SK");
		
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1919(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1798.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1798(final Registro registro) {
    	final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
    	final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
    	final String c9 = String.valueOf(registro.getPorCodigoSII("C9"));
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	final String c12 = String.valueOf(registro.getPorCodigoSII("C12"));
    	final String c13 = String.valueOf(registro.getPorCodigoSII("C13"));
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	
    	final long c8 = ((Number) registro.getPorCodigoSII("C8")).longValue();
    	final Date c11 = (Date) registro.getPorCodigoSII("C11");
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
    	final long c16 = ((Number) registro.getPorCodigoSII("C16")).longValue();
    	final long c17 = ((Number) registro.getPorCodigoSII("C17")).longValue();
    	final long c18 = ((Number) registro.getPorCodigoSII("C18")).longValue();
    	final long c19 = ((Number) registro.getPorCodigoSII("C19")).longValue();
    	final long c20 = ((Number) registro.getPorCodigoSII("C20")).longValue();
    	
    	try{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			final Date date = formatter.parse("0001-01-01");
			return !c2.isEmpty() && !c3.isEmpty() && !c4.isEmpty() && !c5.isEmpty() && !c6.isEmpty() && !c9.isEmpty() && !c10.isEmpty()
	    			&& !c12.isEmpty() && !c13.isEmpty() && !"0".equalsIgnoreCase(c7) && c8 > 0 && date.compareTo(c11) != 0 && c14 > 0 && c15 > 0 && c16 > 0
	    			&& c17 > 0 && c18 > 0 && c19 > 0 && c20 > 0;
		}catch(Exception e){
			return false;
		}

    	
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1799.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1799(final Registro registro) {
    	final String c3 = (String) registro.getPorCodigoSII("C3");
    	final String c5 = (String) registro.getPorCodigoSII("C5");
    	if(!c3.isEmpty() && !c5.isEmpty()){
    		return validarRut(c3) && validarRut(c5);
    	}
    	return false;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1802.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1802(final Registro registro) {
    	final String c4 = (String) registro.getPorCodigoSII("C4");
    	return !"55555555-5".equalsIgnoreCase(c4);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1803.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1803(final Registro registro) {
    	final String c6 = (String) registro.getPorCodigoSII("C6");
    	return PAIS_RESIDENCIA.contains(c6);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1804.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1804(final Registro registro) {
    	final String c7 = (String) registro.getPorCodigoSII("C7");
    	return PAIS_RESIDENCIA.contains(c7);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1805.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1805(final Registro registro) {
    	final String c9 = String.valueOf(registro.getPorCodigoSII("C9"));
    	return PARTICIPACION_DIRECTA_INDIRECTA.contains(c9);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1808.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1808(final Registro registro) {
    	final long c16 = ((Number) registro.getPorCodigoSII("C16")).longValue();
    	final long c18 = ((Number) registro.getPorCodigoSII("C18")).longValue();
    	return c18 > c16;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1809.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1809(final Registro registro) {
    	final long c8 = ((Number) registro.getPorCodigoSII("C8")).longValue();
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
    	final long c16 = ((Number) registro.getPorCodigoSII("C16")).longValue();
    	final long c18 = ((Number) registro.getPorCodigoSII("C18")).longValue();
    	final long c20 = ((Number) registro.getPorCodigoSII("C20")).longValue();
    	final long c21 = ((Number) registro.getPorCodigoSII("C21")).longValue();
    	return c8 > 0 && c14 > 0 && c15 > 0 && c16 > 0 && c18 > 0 && c20 > 0 && c21 > 0;
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