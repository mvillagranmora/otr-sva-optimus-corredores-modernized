package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.math.BigDecimal;


import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1829.
 *
 * 
 *
 */
public class ValidadorDJ1829 extends Validador {
	
	private static final List<String> RELACION_CONTRAPARTE = Arrays.asList("1", "2", "3","4","99");
	private static final List<String> PAIS_RESIDENCIA = Arrays.asList("AF"	,"AL"	,"AT"	,"AU"	,"BE"	,"BM"	,"BO"	,"BR"	,"BS"	
																	 ,"CA"	,"CL"	,"CN"	,"CO"	,"CR"	,"CU"	,"CH"	,"DE"	,"DK"
																	 ,"DM"	,"DO"	,"EC"	,"ES"	,"FI"	,"FR"	,"IG"	,"GR"	,"HK"
																	 ,"ID"	,"IE"	,"IN"	,"KY"	,"IT"	,"JP"	,"KR"	,"LU"	,"NL"
																	 ,"MT"	,"MX"	,"MY"	,"NE"	,"NG"	,"NI"	,"NL"	,"NO"	,"NZ"
																	 ,"PA"	,"PE"	,"PH"	,"PK"	,"PL"	,"PR"	,"PT"	,"PY"	,"RO"
																	 ,"SA"	,"SE"	,"SG"	,"TR"	,"TT"	,"TW"	,"TZ"	,"UA"	,"UG"
																	 ,"US"	,"UY"	,"VE"	,"VN"	,"ZA"	,"HR"	,"CZ"	,"SK");
	private static final List<String> TIPO_ACUERDO_MARCO = Arrays.asList("1", "2", "3");
	private static final List<String> CONTRATO_VENCIDO_EJERCICIO = Arrays.asList("1", "2");
	private static final List<String> ESTADO_CONTRATO_CONFIRMACION = Arrays.asList("1","2","3","4");
	private static final List<String> TIPO_CONTRATO = Arrays.asList("1","2","3","4","5","6","7","8","9","10","11","12","13","14");
	private static final List<String> MODALIDAD_CUMPLIMIENTO = Arrays.asList("1","2");
	private static final List<String> POSICION_DECLARANTE = Arrays.asList("1","2");
	private static final List<String> POSICION_DECLARANTE_TIPO_CONTRATO = Arrays.asList("3","4","5");
	private static final List<String> TIPO_ACTIVO_SUBYACENTE = Arrays.asList("1","2","3","4","5","6","7");
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
	private static final List<String> CODIGO_ACTIVO_SUBYACENTE_TASA_INTERES = Arrays.asList("1","2");
	private static final List<String> CODIGO_ACTIVO_SUBYACENTE_PRODUCTO_BASICO = Arrays.asList("1","2","3","4","5","6","7","8","9","10","11","12","13","14");
	private static final List<String> TASAS_VARIABLES = Arrays.asList("1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32","33","34","35","36","37","38","39","40");
	private static final List<String> SEGUNDO_ACTIVO_SUBYACENTE = Arrays.asList("1","2","3","4","5","6","7");
	private static final List<String> CODIGO_PRECIO = Arrays.asList("1","2");
	private static final List<String> TABLA_UNIDAD = Arrays.asList("1","2","3","4","5","6","7","8","9","10","11","12","13");
	private static final List<String> TABLA_INSTRUIDA = Arrays.asList("1","2");
	private static final List<String> MODALIDAD_CONTRATO = Arrays.asList("1","2","3","4","5","6");
	private static final List<String> MODALIDAD_PAGO = Arrays.asList("1","2","3","4");
	private static final List<String> TIPO_CONTRATO_H1488 = Arrays.asList("12","13","14");
	private static final List<String> TIPO_CONTRATO_H1493 = Arrays.asList("6","7","8","9","10","11","12","13","14");
	private final String version;
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1829(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1451.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1451(final Registro registro) {
    	final String c11 = String.valueOf(registro.getPorCodigoSII("C11"));
    	final String c12 = String.valueOf(registro.getPorCodigoSII("C12"));
    	final String c13 = String.valueOf(registro.getPorCodigoSII("C13"));
    	final String c17 = String.valueOf(registro.getPorCodigoSII("C17"));
    	final Date c18 = (Date) registro.getPorCodigoSII("C18");
    	final String c19 =  String.valueOf(registro.getPorCodigoSII("C19"));
    	final String c21 = String.valueOf(registro.getPorCodigoSII("C21"));
    	final String c23 = String.valueOf(registro.getPorCodigoSII("C23"));
    	final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));
    	final String c36 = String.valueOf(registro.getPorCodigoSII("C36"));
    	final long c37 = ((Number) registro.getPorCodigoSII("C37")).longValue();
    	final Date c40 = (Date) registro.getPorCodigoSII("C40");
    	final String c42 = String.valueOf(registro.getPorCodigoSII("C42"));
    	final BigDecimal c43 = new BigDecimal(registro.getPorCodigoSII("C43").toString());
    	try{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			final Date date = formatter.parse("0001-01-01");
			return !c11.isEmpty() && !c12.equalsIgnoreCase("0") && !c13.equalsIgnoreCase("0") && !c17.isEmpty() && date.compareTo(c18) != 0 
					&& !c19.equalsIgnoreCase("0") && !c21.equalsIgnoreCase("0") && !c23.equalsIgnoreCase("0") && !c25.equalsIgnoreCase("0")
	    			&& !c36.equalsIgnoreCase("0") && c37 != 0 && date.compareTo(c40) != 0 && !c42.equalsIgnoreCase("0") && c43.compareTo(BigDecimal.ZERO) > 0;
		}catch(Exception e){
			return false;
		}
    	
    	
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1452.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1452(final Registro registro) {
    	final String c12 = String.valueOf(registro.getPorCodigoSII("C12"));
    	return RELACION_CONTRAPARTE.contains(c12);
    	
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1453.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1453(final Registro registro) {
    	final String c11 = String.valueOf(registro.getPorCodigoSII("C11"));
    	return PAIS_RESIDENCIA.contains(c11);
    	
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1454.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1454(final Registro registro) {
    	final String c14 = String.valueOf(registro.getPorCodigoSII("C14"));
    	if(!"0".equalsIgnoreCase(c14) && !c14.isEmpty()) return TIPO_ACUERDO_MARCO.contains(c14);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1455.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1455(final Registro registro) {
    	final String c19 = String.valueOf(registro.getPorCodigoSII("C19"));
    	return CONTRATO_VENCIDO_EJERCICIO.contains(c19);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1456.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1456(final Registro registro) {
      final String c19 = String.valueOf(registro.getPorCodigoSII("C19"));
    	final String c20 = String.valueOf(registro.getPorCodigoSII("C20"));
      if("2".equalsIgnoreCase(c19)){
    	  return ESTADO_CONTRATO_CONFIRMACION.contains(c20);
      }
      
      return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1457.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1457(final Registro registro) {
    	final String c21 = String.valueOf(registro.getPorCodigoSII("C21"));
    	return TIPO_CONTRATO.contains(c21);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1458.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1458(final Registro registro) {
    	final String c23 = String.valueOf(registro.getPorCodigoSII("C23"));
    	return MODALIDAD_CUMPLIMIENTO.contains(c23);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1459.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1459(final Registro registro) {
    	final String c21 = String.valueOf(registro.getPorCodigoSII("C21"));
    	final String c24 = String.valueOf(registro.getPorCodigoSII("C24"));
    	if(!POSICION_DECLARANTE_TIPO_CONTRATO.contains(c21)) return POSICION_DECLARANTE.contains(c24);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1460.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1460(final Registro registro) {
    	final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));
    	return TIPO_ACTIVO_SUBYACENTE.contains(c25);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1460.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1461(final Registro registro) {
    	final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));
    	final String c26 = String.valueOf(registro.getPorCodigoSII("C26"));
    	if("1".equalsIgnoreCase(c25)) return MONEDA_AT.contains(c26);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1462.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1462(final Registro registro) {
    	final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));
    	final String c26 = String.valueOf(registro.getPorCodigoSII("C26"));
    	if("2".equalsIgnoreCase(c25)) return CODIGO_ACTIVO_SUBYACENTE_TASA_INTERES.contains(c26);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1463.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1463(final Registro registro) {
    	final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));
    	final String c26 = String.valueOf(registro.getPorCodigoSII("C26"));
    	if("3".equalsIgnoreCase(c25)) return CODIGO_ACTIVO_SUBYACENTE_PRODUCTO_BASICO.contains(c26);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1464.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1464(final Registro registro) {
    	final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));
    	final String c26 = String.valueOf(registro.getPorCodigoSII("C26"));
    	final String c27 = String.valueOf(registro.getPorCodigoSII("C27"));
    	if("2".equalsIgnoreCase(c25) && "2".equalsIgnoreCase(c26)) return TASAS_VARIABLES.contains(c27);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1465.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1465(final Registro registro) {
    	final String c21 = String.valueOf(registro.getPorCodigoSII("C21"));
    	final String c29 = String.valueOf(registro.getPorCodigoSII("C29"));
    	if(POSICION_DECLARANTE_TIPO_CONTRATO.contains(c21)) return SEGUNDO_ACTIVO_SUBYACENTE.contains(c29);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1466.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1466(final Registro registro) {
    	final String c29 = String.valueOf(registro.getPorCodigoSII("C29"));
    	final String c30 = String.valueOf(registro.getPorCodigoSII("C30"));
    	if("1".equalsIgnoreCase(c29)) return MONEDA_AT.contains(c30);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1467.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1467(final Registro registro) {
    	final String c29 = String.valueOf(registro.getPorCodigoSII("C29"));
    	final String c30 = String.valueOf(registro.getPorCodigoSII("C30"));
    	if("2".equalsIgnoreCase(c29)) return CODIGO_ACTIVO_SUBYACENTE_TASA_INTERES.contains(c30);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1468.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1468(final Registro registro) {
    	final String c29 = String.valueOf(registro.getPorCodigoSII("C29"));
    	final String c30 = String.valueOf(registro.getPorCodigoSII("C30"));
    	final String c31 = String.valueOf(registro.getPorCodigoSII("C31"));
    	if("2".equalsIgnoreCase(c29) && "2".equalsIgnoreCase(c30)) return TASAS_VARIABLES.contains(c31);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1469.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1469(final Registro registro) {
    	final String c21 = String.valueOf(registro.getPorCodigoSII("C21"));
    	final String c33 = String.valueOf(registro.getPorCodigoSII("C33"));
    	if(!POSICION_DECLARANTE_TIPO_CONTRATO.contains(c21)) return CODIGO_PRECIO.contains(c33);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1470.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1470(final Registro registro) {
    	final String c35 = String.valueOf(registro.getPorCodigoSII("C35"));
    	if(!"0".equalsIgnoreCase(c35) && !c35.isEmpty()) return MONEDA_AT.contains(c35);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1471.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1471(final Registro registro) {
    	final String c36 = String.valueOf(registro.getPorCodigoSII("C36"));
    	if(!"0".equalsIgnoreCase(c36) && !c36.isEmpty()) return TABLA_UNIDAD.contains(c36);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1472.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1472(final Registro registro) {
    	final String c42 = String.valueOf(registro.getPorCodigoSII("C42"));
    	if(!"0".equalsIgnoreCase(c42) && !c42.isEmpty()) return TABLA_INSTRUIDA.contains(c42);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1473.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1473(final Registro registro) {
    	final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));
    	final String c26 = String.valueOf(registro.getPorCodigoSII("C26"));
    	final String c28 = String.valueOf(registro.getPorCodigoSII("C28"));
    	if("2".equalsIgnoreCase(c25) && "1".equalsIgnoreCase(c26)) return !"0".equalsIgnoreCase(c28);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1474.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1474(final Registro registro) {
    	final String c29 = String.valueOf(registro.getPorCodigoSII("C29"));
    	final String c30 = String.valueOf(registro.getPorCodigoSII("C30"));
    	final String c32 = String.valueOf(registro.getPorCodigoSII("C32"));
    	if("2".equalsIgnoreCase(c29) && "1".equalsIgnoreCase(c30)) return !"0".equalsIgnoreCase(c32);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1475.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1475(final Registro registro) {
    	final Date c18 = (Date) registro.getPorCodigoSII("C18");
    	try{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    	String anio = String.valueOf(Integer.parseInt(version));
			final Date dateFin = formatter.parse(anio + "-12-31");
			return (dateFin.compareTo(c18) > 0 || dateFin.compareTo(c18) == 0);
		}catch(Exception e){
			return false;
		}
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1477.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1477(final Registro registro) {
    	final String c13 = String.valueOf(registro.getPorCodigoSII("C13"));
    	return MODALIDAD_CONTRATO.contains(c13);
    	
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1478.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1478(final Registro registro) {
    	final long c54 = ((Number) registro.getPorCodigoSII("C54")).longValue();
    	final String c55 = String.valueOf(registro.getPorCodigoSII("C55"));
    	if (c54 > 0) return MODALIDAD_PAGO.contains(c55);
    	return true;
    	
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1481.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1481(final Registro registro) {
    	final String c8 = String.valueOf(registro.getPorCodigoSII("C8"));
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	if (c8.isEmpty()) return !c10.isEmpty();
    	return true;
    	
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1482.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1482(final Registro registro) {
    	final String c19 = String.valueOf(registro.getPorCodigoSII("C19"));
    	final String c20 = String.valueOf(registro.getPorCodigoSII("C20"));
    	if ("1".equalsIgnoreCase(c19)) return c20.isEmpty();
    	return true;
    	
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1483.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1483(final Registro registro) {
    	final String c21 = String.valueOf(registro.getPorCodigoSII("C21"));
    	final String c24 = String.valueOf(registro.getPorCodigoSII("C24"));
    	if (POSICION_DECLARANTE_TIPO_CONTRATO.contains(c21)) return c24.isEmpty();
    	return true;
    	
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1484.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1484(final Registro registro) {
    	final String c21 = String.valueOf(registro.getPorCodigoSII("C21"));
    	final String c29 = String.valueOf(registro.getPorCodigoSII("C29"));
    	final String c30 = String.valueOf(registro.getPorCodigoSII("C30"));
    	final String c31 = String.valueOf(registro.getPorCodigoSII("C31"));
    	final long c32 = ((Number) registro.getPorCodigoSII("C32")).longValue();
    	if (!POSICION_DECLARANTE_TIPO_CONTRATO.contains(c21)) return "0".equalsIgnoreCase(c29)&& c30.isEmpty() && c31.isEmpty()&& c32 == 0;
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1485.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1485(final Registro registro) {
    	final String c21 = String.valueOf(registro.getPorCodigoSII("C21"));
    	final String c33 = String.valueOf(registro.getPorCodigoSII("C33"));
    	final String c34 = String.valueOf(registro.getPorCodigoSII("C34"));
    	final String c35 = String.valueOf(registro.getPorCodigoSII("C35"));
    	if (POSICION_DECLARANTE_TIPO_CONTRATO.contains(c21)) return "0".equalsIgnoreCase(c33) && "0".equalsIgnoreCase(c34) && c35.isEmpty();
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1486.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1486(final Registro registro) {
    	final String c33 = String.valueOf(registro.getPorCodigoSII("C33"));
    	final String c34 = String.valueOf(registro.getPorCodigoSII("C34"));
    	final String c35 = String.valueOf(registro.getPorCodigoSII("C35"));
    	if ("2".equalsIgnoreCase(c33)) return "0".equalsIgnoreCase(c34) && c35.isEmpty();
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1487.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1487(final Registro registro) {
    	final String c8 = String.valueOf(registro.getPorCodigoSII("C8"));
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	final String[] numeroRut = c8.split("-");
    	return (!c8.isEmpty() && Integer.parseInt(numeroRut[0]) > 0) || !c10.isEmpty();
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1488.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1488(final Registro registro) {
    	final String c21 = String.valueOf(registro.getPorCodigoSII("C21"));
    	final String c22 = String.valueOf(registro.getPorCodigoSII("C22"));
    	if(TIPO_CONTRATO_H1488.contains(c21)) return c22.isEmpty();
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1489.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1489(final Registro registro) {
    	final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));
    	final String c27 = String.valueOf(registro.getPorCodigoSII("C27"));
    	if("7".equalsIgnoreCase(c25)) return !c27.isEmpty();
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1491.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1491(final Registro registro) {
    	final String c21 = String.valueOf(registro.getPorCodigoSII("C21"));
    	final String c38 = String.valueOf(registro.getPorCodigoSII("C38"));
    	final String c39 = String.valueOf(registro.getPorCodigoSII("C39"));
    	if(POSICION_DECLARANTE_TIPO_CONTRATO.contains(c21)) return !c38.isEmpty() && !c39.isEmpty();
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1492.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1492(final Registro registro) {
    	final String c42 = String.valueOf(registro.getPorCodigoSII("C42"));
    	final BigDecimal c43 = new BigDecimal(registro.getPorCodigoSII("C43").toString());
    	return !c42.equalsIgnoreCase("0") && c43.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1493.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1493(final Registro registro) {
    	final long c50 = ((Number) registro.getPorCodigoSII("C50")).longValue();
    	final String c21 = String.valueOf(registro.getPorCodigoSII("C21"));
    	if(c50 > 0) return TIPO_CONTRATO_H1493.contains(c21);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1494.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1494(final Registro registro) {
    	final String c36 = String.valueOf(registro.getPorCodigoSII("C36"));
    	final long c37 = ((Number) registro.getPorCodigoSII("C37")).longValue();
    	if(!c36.equalsIgnoreCase("0")) return c37 > 0;
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1495.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1495(final Registro registro) {
    	final String  c21 = String.valueOf(registro.getPorCodigoSII("C21"));
    	final String c38 = String.valueOf(registro.getPorCodigoSII("C38"));
    	final long c39 = ((Number) registro.getPorCodigoSII("C39")).longValue();
    	if(POSICION_DECLARANTE_TIPO_CONTRATO.contains(c21)) return !c38.equalsIgnoreCase("0") && c39 > 0;
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1496.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1496(final Registro registro) {
    	final String c45 = String.valueOf(registro.getPorCodigoSII("C45"));
    	final String c46 = String.valueOf(registro.getPorCodigoSII("C46"));
    	if(!"0".equalsIgnoreCase(c45)) return !c46.isEmpty() && !"0".equalsIgnoreCase(c46);
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1497.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1497(final Registro registro) {
    	final long c47 = ((Number) registro.getPorCodigoSII("C47")).longValue();
    	final String c48 = String.valueOf(registro.getPorCodigoSII("C48"));
    	if(c47 !=0) return !c48.isEmpty();
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1498.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1498(final Registro registro) {
    	final long c54 = ((Number) registro.getPorCodigoSII("C54")).longValue();
    	final String c55 = String.valueOf(registro.getPorCodigoSII("C55"));
    	if(c54 > 0) return !c55.equalsIgnoreCase("0");
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1499.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1499(final Registro registro) {
    	final String c19 = String.valueOf(registro.getPorCodigoSII("C19"));
    	final Date c41 = (Date) registro.getPorCodigoSII("C41");
    	if("1".equalsIgnoreCase(c19)){
    		try{
    			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    			final Date date = formatter.parse("0001-01-01");
    			return date.compareTo(c41) != 0;
    		}catch(Exception e){
    			return false;
    		}
    	}
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1500.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1500(final Registro registro) {
    	final Date c16 = (Date) registro.getPorCodigoSII("C16");
    	final Date c18 = (Date) registro.getPorCodigoSII("C18");
    	try{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			final Date date = formatter.parse("0001-01-01");
			if(date.compareTo(c16) != 0) return c18.compareTo(c16) > 0 || c18.compareTo(c16) == 0;
		}catch(Exception e){
			return false;
		}
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1501.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1501(final Registro registro) {
    	final Date c40 = (Date) registro.getPorCodigoSII("C40");
    	final Date c18 = (Date) registro.getPorCodigoSII("C18");
    	try{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			final Date date = formatter.parse("0001-01-01");
			if(date.compareTo(c40) != 0) return c40.compareTo(c18) > 0 || c40.compareTo(c18) == 0;
		}catch(Exception e){
			return false;
		}
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1502.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1502(final Registro registro) {
    	final Date c40 = (Date) registro.getPorCodigoSII("C40");
    	final Date c41 = (Date) registro.getPorCodigoSII("C41");
    	try{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			final Date date = formatter.parse("0001-01-01");
			if(date.compareTo(c40) != 0 && date.compareTo(c41) != 0) return c40.compareTo(c41) > 0 || c40.compareTo(c41) == 0;
		}catch(Exception e){
			return false;
		}
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1516.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1516(final Registro registro) {
    	final String c29 = String.valueOf(registro.getPorCodigoSII("C29"));
    	final String c30 = String.valueOf(registro.getPorCodigoSII("C30"));
    	if("3".equalsIgnoreCase(c29)) return CODIGO_ACTIVO_SUBYACENTE_PRODUCTO_BASICO.contains(c30);
    	return true;
    }
    
}