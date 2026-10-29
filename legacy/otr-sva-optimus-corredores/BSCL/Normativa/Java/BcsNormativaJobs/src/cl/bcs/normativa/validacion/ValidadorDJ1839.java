package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;


import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1839.
 *
 * 
 *
 */
public class ValidadorDJ1839 extends Validador {
	
	private static final List<String> PAIS_RESIDENCIA = Arrays.asList("AF"	,"AL"	,"AT"	,"AU"	,"BE"	,"BM"	,"BO"	,"BR"	,"BS"	
			 ,"CA"	,"CL"	,"CN"	,"CO"	,"CR"	,"CU"	,"CH"	,"DE"	,"DK"
			 ,"DM"	,"DO"	,"EC"	,"ES"	,"FI"	,"FR"	,"IG"	,"GR"	,"HK"
			 ,"ID"	,"IE"	,"IN"	,"KY"	,"IT"	,"JP"	,"KR"	,"LU"	,"NL"
			 ,"MT"	,"MX"	,"MY"	,"NE"	,"NG"	,"NI"	,"NL"	,"NO"	,"NZ"
			 ,"PA"	,"PE"	,"PH"	,"PK"	,"PL"	,"PR"	,"PT"	,"PY"	,"RO"
			 ,"SA"	,"SE"	,"SG"	,"TR"	,"TT"	,"TW"	,"TZ"	,"UA"	,"UG"
			 ,"US"	,"UY"	,"VE"	,"VN"	,"ZA"	,"HR"	,"CZ"	,"SK");
	private static final List<String> TIPO_ACUERDO_MARCO = Arrays.asList("1", "2", "3");
	private static final List<String> TIPO_CONTRATO = Arrays.asList("1","2","3","4","5","6","7","8","9","10","11","12","13","14");
	private static final List<String> TIPO_CONTRATO_OPCION_OTROS = Arrays.asList("12", "13", "14");

	
	private final String version;
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1839(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1503.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1503(final Registro registro) {
    	final String c20 = String.valueOf(registro.getPorCodigoSII("C20"));
    	final String c21 = String.valueOf(registro.getPorCodigoSII("C21"));
    	
    	return !c20.isEmpty() && !c21.isEmpty();
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1504.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1504(final Registro registro) {
    	final String c14 = String.valueOf(registro.getPorCodigoSII("C14"));    	
    	return PAIS_RESIDENCIA.contains(c14);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1505.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1505(final Registro registro) {
    	final String c15 = String.valueOf(registro.getPorCodigoSII("C15"));    	
    	return PAIS_RESIDENCIA.contains(c15);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1506.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1506(final Registro registro) {
    	final String c16 = String.valueOf(registro.getPorCodigoSII("C16"));    	
    	return TIPO_ACUERDO_MARCO.contains(c16);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1507.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1507(final Registro registro) {
    	final String c21 = String.valueOf(registro.getPorCodigoSII("C21"));    	
    	return TIPO_CONTRATO.contains(c21);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1509.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1509(final Registro registro) {
    	final Date c20 = (Date) registro.getPorCodigoSII("C20");    	
    	try{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String anio = String.valueOf(Integer.parseInt(version)-1);
			final Date dateFin = formatter.parse(anio + "-12-31");
			return (dateFin.compareTo(c20) > 0 || dateFin.compareTo(c20) == 0);
		}catch(Exception e){
			return false;
		}
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1510.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1510(final Registro registro) {
    	final String c8 = String.valueOf(registro.getPorCodigoSII("C8"));  
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));  
    	if("0".equalsIgnoreCase(c8)) return !"0".equalsIgnoreCase(c10);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1511.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1511(final Registro registro) {
    	final String c11 = String.valueOf(registro.getPorCodigoSII("C11"));  
    	final String c13 = String.valueOf(registro.getPorCodigoSII("C13"));  
    	if("0".equalsIgnoreCase(c11)) return !"0".equalsIgnoreCase(c13);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1512.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1512(final Registro registro) {
    	final String c8 = String.valueOf(registro.getPorCodigoSII("C8"));  
    	final String c10 =  String.valueOf(registro.getPorCodigoSII("C10"));  
    	return !c8.equalsIgnoreCase("0") || !c10.equalsIgnoreCase("0");
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1513.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1513(final Registro registro) {
    	final String c11 = String.valueOf(registro.getPorCodigoSII("C11"));  
    	final String c13 = String.valueOf(registro.getPorCodigoSII("C13"));
    	return !c11.equalsIgnoreCase("0") || !c13.equalsIgnoreCase("0");
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1514.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1514(final Registro registro) {
    	final String c21 = String.valueOf(registro.getPorCodigoSII("C11"));  
    	final String c22 =  String.valueOf(registro.getPorCodigoSII("C22")); 
    	if (TIPO_CONTRATO_OPCION_OTROS.contains(c21)) return c22.isEmpty();
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1515.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1515(final Registro registro) {
    	final Date c18 = (Date) registro.getPorCodigoSII("C18");  
    	final Date c20 = (Date) registro.getPorCodigoSII("C20"); 
		return c20.compareTo(c18) > 0 || c20.compareTo(c18) == 0;
    }
    
}