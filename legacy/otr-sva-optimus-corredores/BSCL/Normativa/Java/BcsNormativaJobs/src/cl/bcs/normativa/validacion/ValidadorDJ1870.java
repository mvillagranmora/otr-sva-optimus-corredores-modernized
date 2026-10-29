package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;


import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1870.
 *
 * 
 *
 */
public class ValidadorDJ1870 extends Validador {

	private static final List<String> TIPO_TRANSACCION = Arrays.asList("1", "2", "3","4","5","6");
	private static final List<String> INSTRUCTIVO = Arrays.asList("1", "2", "3");
	private static final List<String> TIPO_DOCUMENTO = Arrays.asList("1", "2", "3","4","5","6","7","8");
	private static final List<String> MONEDA_AT = Arrays.asList("AFN","ALL","EUR","AOA","XCD","SAR","DZD","ARS","AMD","AWG","AUD","AZN","BSD","BHD","BDT","BBD",
																"BZD","XOF","BMD","BYR","BOB","USD","BAM","BWP","BRL","BND","BGN","BIF","BTN","CVE","KHR","XAF",
																"CAD","CLF","CLP","CNY","COP","KPW","KRW","CRC","HRK","CUP","ANG","DKK","EGP","AED","ERN","ETB",
																"RUB","FJD","PHP","XDR","GMD","GEL","GHS","GIP","GTQ","GBP","GNF","GYD","HTG","HNL","HKD","HUF",
																"INR","IDR","IRR","IQD","NOK","ISK","KYD","KMF","NZD","FKP","FKR","SBD","ILS","JMD","JPY","JEP",
																"JOD","KZT","KES","KGS","KWD","LAK","LSL","LBP","LRD","LYD","CHF","LTL","MOP","MKD","MGA","MYR",
																"MWK","MVR","MAD","MUR","MRO","MXN","MDL","MNT","MZN","MMK","NAD","NPR","NIO","NGN","XPF","OMR",
																"XUA","PKR","PAB","PGK","PYG","PEN","PLN","QAR","CZK","CDF","DOP","RWF","RON","WST","SHP","STD",
																"RSD","SCR","SLL","SGD","SYP","SOS","LKR","SZL","ZAR","SDG","SEK","SRD","THB","TWD","TZS","TJS",
																"TOP","TTD","TND","TMT","TRY","UAH","UGX","UYU","UZS","VUV","VEF","VND","YER","DJF","ZMW","ZWL",
																"ILE","MXP");
	private static final List<String> PAIS_RESIDENCIA = Arrays.asList(	"AF", "AL", "AD", "AO", "AI", "AQ", "AG", "AN", "SA", "DZ", "AR", "AM", "AW", "AU", "AT", "AZ", "BS", "BH",
																		"BD", "BB", "BY", "BE", "BZ", "BJ", "BM", "BT", "BO", "BA", "BW", "BR", "BN", "BG", "BF", "BI", "CV", "KH",
																		"CM", "CA", "TD", "CL", "CN", "CY", "CO", "KM", "CG", "CI", "CR", "HR", "CU", "DK", "DJ", "DM", "EC", "EG",
																		"SV", "AE", "ER", "SI", "ES", "VA", "US", "EE", "ET", "RU", "FJ", "PH", "FI", "FR", "FX", "GA", "GM", "GE", 
																		"GS", "GH", "GI", "GD", "GR", "GL", "GP", "GU", "GT", "GF", "GQ", "GY", "HT", "HN", "HK", "HU", "IN", "ID",
																		"IR", "IQ", "IE", "BV", "CX", "NU", "NF", "PN", "IS", "KY", "CC", "CK", "FO", "HM", "FK", "MP", "MH", "UM",
																		"SB", "SJ", "TC", "VG", "VI", "WF", "IL", "IT", "LY", "JM", "JP", "JO", "KZ", "KE", "KG", "KI", "KW", "LS",
																		"LV", "LB", "LR", "LI", "LT", "LU", "MO", "MG", "MY", "MW", "MV", "ML", "MT", "MA", "MQ", "MU", "MR", "YT",
																		"MX", "FM", "MD", "MC", "MN", "MS", "MZ", "MM", "NA", "NR", "NP", "NI", "NE", "NG", "NO", "NC", "NZ", "OM",
																		"NL", "PK", "PW", "PA", "PG", "PY", "PE", "PF", "PL", "PT", "PR", "QA", "GB", "SY", "CF", "CZ", "KR", "ZR",
																		"LA", "DO", "SK", "DE", "KP", "TZ", "RE", "RO", "RW", "EH", "KN", "WS", "AS", "SM", "PM", "VC", "SH", "LC",
																		"ST", "SN", "SC", "SL", "SG", "SO", "LK", "ZA", "SD", "SE", "CH", "SR", "SZ", "TH", "TW", "TJ", "IO", "TF",
																		"TP", "TG", "TK", "TO", "TT", "TN", "TM", "TR", "TV", "UA", "UG", "GN", "GW", "UY", "UZ", "VU", "VE", "VN",
																		"YE", "YU", "ZM","ZW");
	private final String version;
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1870(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H640.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH640(final Registro registro) {
    	final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
    	return TIPO_TRANSACCION.contains(c6);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H641.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH641(final Registro registro) {
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	return INSTRUCTIVO.contains(c7);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H642.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH642(final Registro registro) {
    	
    	
    	final String c8 = String.valueOf(registro.getPorCodigoSII("C8"));
    	final String c9 = String.valueOf(registro.getPorCodigoSII("C9"));
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	try{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
//	    	String anio = String.valueOf(Integer.parseInt(version)-1);
	    	String anio = String.valueOf(Integer.parseInt(version));
			final Date dateInicio = formatter.parse(anio + "-01-01");
			final Date dateFin = formatter.parse(anio + "-12-31");
			final Date fechaTransaccion = formatter.parse(c10 + "-" + c9 + "-" + c8);
			return (dateInicio.compareTo(fechaTransaccion) < 0 || dateInicio.compareTo(fechaTransaccion) == 0) && (dateFin.compareTo(fechaTransaccion) > 0 || dateFin.compareTo(fechaTransaccion) == 0);
		}catch(Exception e){
			return false;
		}
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H643.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH643(final Registro registro) {
    	final String c11 = String.valueOf(registro.getPorCodigoSII("C11"));
    	return TIPO_DOCUMENTO.contains(c11);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H644.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH644(final Registro registro) {
    	final String c13 = String.valueOf(registro.getPorCodigoSII("C13"));
    	return MONEDA_AT.contains(c13);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H645.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH645(final Registro registro) {
    	final long c14 = ((Number) registro.getPorCodigoSII("C14")).longValue();
    	final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
    	final long c16 = ((Number) registro.getPorCodigoSII("C16")).longValue();
    	return ((c14 > 0 && c15 == 0) || (c15 >0 && c14 == 0)) && c16 >0;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H647.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH647(final Registro registro) {
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	final String c1 = String.valueOf(registro.getPorCodigoSII("C1"));
    	if(c4.isEmpty()){
    		return !c1.isEmpty();
    	}else{
    		return PAIS_RESIDENCIA.contains(c4);
    	}
    }
}