package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import cl.bcs.normativa.dal.ConfiguracionDAO;
import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1894.
 *
 * 
 *
 */
public class ValidadorDJ1922 extends Validador {

	private static final List<String> TIPO_FONDO = Arrays.asList("I", "F", "P");

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
	
	
	
	
	private static final List<String> TIPO_OPERACION = Arrays.asList("1", "2", "3", "4", "5", "6","7");
	private static List<String> TIPO_OPERACION_V2 = new ArrayList<>();
	private static final List<String> TIPO_OPERACION_H1843 = Arrays.asList("2", "3");
	private static final List<String> MONEDA_TIPO_REAJUSTE = Arrays.asList("1", "2", "3", "4", "5", "6");
	private static final List<String> PARTICIPE = Arrays.asList("1", "2", "3");
	private static final List<String> CUOTAS = Arrays.asList("1", "2", "3");
	private static final List<String> TRIBUTASA = Arrays.asList("1", "2");
    private static List<String> TIPO_CONTRIBUYENTE = new ArrayList<>();
	
	private final String version;
	private final double p84;
	private final double p31;
	private final double p85;
	private final int limiteRut;
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1922(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
        p84 = contexto.getParametroEsquemaAsDouble("P84");
        p31 = contexto.getParametroEsquemaAsDouble("P31");
        p85 = contexto.getParametroEsquemaAsDouble("P85");
        limiteRut = (int)contexto.getParametroEsquemaAsDouble("LimiteRut");
        TIPO_OPERACION_V2 = ConfiguracionDAO.buscarDiccionarioPorTipo(contexto.getConnection(), contexto.getSchema(), "TipoOperacionFondos1922V2","S","2025","");
        TIPO_CONTRIBUYENTE = ConfiguracionDAO.buscarDiccionarioPorTipo(contexto.getConnection(), contexto.getSchema(), "TipoContribuyente","S","2025","");
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1822.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1822(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	final String c80 = String.valueOf(registro.getPorCodigoSII("C80"));
    	final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
    	final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	final String c8 = String.valueOf(registro.getPorCodigoSII("C8"));
    	final String c9 = String.valueOf(registro.getPorCodigoSII("C9"));
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	final String c11 = String.valueOf(registro.getPorCodigoSII("C11"));
    	final String c12 = String.valueOf(registro.getPorCodigoSII("C12"));
        final String c65 = String.valueOf(registro.getPorCodigoSII("C65"));
        final String c139 = String.valueOf(registro.getPorCodigoSII("C139"));        
        
        if(Integer.parseInt(version) >= 2025) {
        	return (!c3.isEmpty() && !"0".equalsIgnoreCase(c3)) && (!c4.isEmpty() && !"0".equalsIgnoreCase(c4))
	    			&& (!c6.isEmpty() && !"0".equalsIgnoreCase(c6)) && ((!c7.isEmpty() && !"0".equalsIgnoreCase(c7)) || (!c8.isEmpty() && !"0".equalsIgnoreCase(c8)))
	    			&& (!c9.isEmpty() && !"0".equalsIgnoreCase(c9)) && (!c10.isEmpty() && !"0".equalsIgnoreCase(c10)) && (!c11.isEmpty() && !"0".equalsIgnoreCase(c11))    			
	                && (!c139.isEmpty() && !"0".equalsIgnoreCase(c139)); 
        }else {
        	return (!c3.isEmpty() && !"0".equalsIgnoreCase(c3)) && (!c4.isEmpty() && !"0".equalsIgnoreCase(c4)) && (!c5.isEmpty() && !"0".equalsIgnoreCase(c5))
	    			&& (!c6.isEmpty() && !"0".equalsIgnoreCase(c6)) && ((!c7.isEmpty() && !"0".equalsIgnoreCase(c7)) || (!c8.isEmpty() && !"0".equalsIgnoreCase(c8)))
	    			&& (!c9.isEmpty() && !"0".equalsIgnoreCase(c9)) && (!c10.isEmpty() && !"0".equalsIgnoreCase(c10)) && (!c11.isEmpty() && !"0".equalsIgnoreCase(c11))
	    			&& (!c12.isEmpty() && !"0".equalsIgnoreCase(c12)) && (!c80.isEmpty() && !"0".equalsIgnoreCase(c80))
	                && (!c65.isEmpty() && !"0".equalsIgnoreCase(c65));
        }
    }
    
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1839.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1839(final Registro registro) {
    	final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
    	return TIPO_FONDO.contains(c5);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1840.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1840(final Registro registro) {
    	final String c9 = String.valueOf(registro.getPorCodigoSII("C9"));
    	return PAIS_RESIDENCIA.contains(c9);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1841.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1841(final Registro registro) {
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	if(Integer.parseInt(version) >= 2025) {
    		return TIPO_OPERACION_V2.contains(c10);
        }else {
        	return TIPO_OPERACION.contains(c10);
        }
    }    
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1842.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1842(final Registro registro) {
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	
        final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
        final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
        final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
        final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
        final String c11 = String.valueOf(registro.getPorCodigoSII("C11"));
        final String c12 = String.valueOf(registro.getPorCodigoSII("C12"));
        final long c13 = ((Number) registro.getPorCodigoSII("C13")).longValue();
        final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
        final long c18 = ((Number) registro.getPorCodigoSII("C18")).longValue();
        final long c64 = ((Number) registro.getPorCodigoSII("C64")).longValue();
        
        final long c24 = ((Number) registro.getPorCodigoSII("C24")).longValue();
        final long c25 = ((Number) registro.getPorCodigoSII("C25")).longValue();
        final long c26 = ((Number) registro.getPorCodigoSII("C26")).longValue();
        final long c66 = ((Number) registro.getPorCodigoSII("C66")).longValue();
        final long c67 = ((Number) registro.getPorCodigoSII("C67")).longValue();
        final long c68 = ((Number) registro.getPorCodigoSII("C68")).longValue();
        final long c69 = ((Number) registro.getPorCodigoSII("C69")).longValue();
        final long c96 = ((Number) registro.getPorCodigoSII("C96")).longValue();
        final long c97 = ((Number) registro.getPorCodigoSII("C97")).longValue();
        final long c98 = ((Number) registro.getPorCodigoSII("C98")).longValue();
        final long c99 = ((Number) registro.getPorCodigoSII("C99")).longValue();
        final long c100 = ((Number) registro.getPorCodigoSII("C100")).longValue();
        final long c28 = ((Number) registro.getPorCodigoSII("C28")).longValue();
        final long c29 = ((Number) registro.getPorCodigoSII("C29")).longValue();
        final long c70 = ((Number) registro.getPorCodigoSII("C70")).longValue();
        final long c31 = ((Number) registro.getPorCodigoSII("C31")).longValue();
        final long c30 = ((Number) registro.getPorCodigoSII("C30")).longValue();
        final long c32 = ((Number) registro.getPorCodigoSII("C32")).longValue();
        final long c33 = ((Number) registro.getPorCodigoSII("C33")).longValue();
        final long c34 = ((Number) registro.getPorCodigoSII("C34")).longValue();

    	if("7".equalsIgnoreCase(c10)) 
    		return      
                    (!c3.isEmpty() && !"".equalsIgnoreCase(c3)) && (!c4.isEmpty() && !"".equalsIgnoreCase(c4))
                    && (!c6.isEmpty() && !"".equalsIgnoreCase(c6)) && (!c7.isEmpty() && !"".equalsIgnoreCase(c7))
        			&& !"1900-01-01".equalsIgnoreCase(c11)  && (!c12.isEmpty() && !"".equalsIgnoreCase(c12))
        			&& c13>0  && c15>0
        			&& c18>0  && c64>0
                    
                    
        			&& c24==0 && c25==0 && c26==0 && c66==0 && c67==0 && c68==0
        			&& c69==0 && c96==0 && c97==0 && c98==0 && c99==0 && c100==0
                    && c28==0 && c29==0 && c70==0 && c31==0 && c30==0 && c32==0
                    && c33==0 && c34==0
        			;
        	
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1843.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1843(final Registro registro) {
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	final long c24 = ((Number) registro.getPorCodigoSII("C24")).longValue();
    	final long c25 = ((Number) registro.getPorCodigoSII("C25")).longValue();
    	final long c26 = ((Number) registro.getPorCodigoSII("C26")).longValue();
    	if(Integer.parseInt(version) >= 2025) {
    		if(TIPO_OPERACION_H1843.contains(c10)) return c24 > 0 && c25 > 0;
        }else {
        	if(TIPO_OPERACION_H1843.contains(c10)) return c24 > 0 && c25 > 0 && c26 != 0;
        }
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1844.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1844(final Registro registro) {
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	final long c20 = ((Number) registro.getPorCodigoSII("C20")).longValue();
    	final long c21 = ((Number) registro.getPorCodigoSII("C21")).longValue();
    	final long c22 = ((Number)registro.getPorCodigoSII("C22")).longValue();
    	final long c23 = ((Number)registro.getPorCodigoSII("C23")).longValue();
    	if(Integer.parseInt(version) >= 2025) {
    		if("4".equalsIgnoreCase(c10)) return c20 > 0 && c21 > 0;
    	}else {
    		if("4".equalsIgnoreCase(c10)) return c20 > 0 && c21 > 0 && ((c22 + c23) != 0);
    	} 
    	return true;
    }    
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1845.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
   public boolean validarH1845(final Registro registro) {
   	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
   	if(Integer.parseInt(version) >= 2025) {
   		final long c34 =  ((Number) registro.getPorCodigoSII("C34")).longValue();	    	
    	if("6".equalsIgnoreCase(c10)) return c34 > 0;
   	}else {
   		final String c34 = String.valueOf(registro.getPorCodigoSII("C34"));
		if("6".equalsIgnoreCase(c10)) return !c34.isEmpty();
   	}
   	return true;  	
   }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1846.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1846(final Registro registro) {
    	final long c36 = ((Number) registro.getPorCodigoSII("C36")).longValue();
    	final long c37 = ((Number) registro.getPorCodigoSII("C37")).longValue();
    	final long c38 = ((Number) registro.getPorCodigoSII("C38")).longValue();
    	final long c39 = ((Number) registro.getPorCodigoSII("C39")).longValue();
    	final long c27 = ((Number) registro.getPorCodigoSII("C27")).longValue();
    	final long c28 = ((Number) registro.getPorCodigoSII("C28")).longValue();
    	final long c35 = ((Number) registro.getPorCodigoSII("C35")).longValue();
    	final long c40 = ((Number) registro.getPorCodigoSII("C40")).longValue();
    	return c36 + c37 + c38 + c39 - p31 <= Math.round(c27 + c28 + c35 + c40) * p84;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1846.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1847(final Registro registro) {
    	final long c36 = ((Number) registro.getPorCodigoSII("C36")).longValue();
    	final long c38 = ((Number) registro.getPorCodigoSII("C38")).longValue();
    	final long c27 = ((Number) registro.getPorCodigoSII("C27")).longValue();
    	final long c28 = ((Number) registro.getPorCodigoSII("C28")).longValue();
    	final long c35 = ((Number) registro.getPorCodigoSII("C35")).longValue();
    	return c36 + c38 - p31 <= Math.round(c27 + c28 + c35) * p84;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1848.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1848(final Registro registro) {
    	final long c35 = ((Number) registro.getPorCodigoSII("C35")).longValue();
    	final long c36 = ((Number) registro.getPorCodigoSII("C36")).longValue();
    	final long c37 = ((Number) registro.getPorCodigoSII("C37")).longValue();
    	final long c38 = ((Number) registro.getPorCodigoSII("C38")).longValue();
    	final long c39 = ((Number) registro.getPorCodigoSII("C39")).longValue();
    	return c35 - p31 <= c36 + c37 + c38 + c39;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1849.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1849(final Registro registro) {
    	final long c35 = ((Number) registro.getPorCodigoSII("C35")).longValue();
    	final long c36 = ((Number) registro.getPorCodigoSII("C36")).longValue();
    	final long c38 = ((Number) registro.getPorCodigoSII("C38")).longValue();
    	return c35 - p31 <= c36 + c38;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1850.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1850(final Registro registro) {
    	final long c35 = ((Number) registro.getPorCodigoSII("C35")).longValue();
    	final long c27 = ((Number) registro.getPorCodigoSII("C27")).longValue();
    	final long c28 = ((Number) registro.getPorCodigoSII("C28")).longValue();
    	final long c40 = ((Number) registro.getPorCodigoSII("C40")).longValue();
    	return c35 - p31 <= (c27 + c28 + c40) * p85;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1851.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1851(final Registro registro) {
    	final long c35 = ((Number) registro.getPorCodigoSII("C35")).longValue();
    	final long c27 = ((Number) registro.getPorCodigoSII("C27")).longValue();
    	final long c28 = ((Number) registro.getPorCodigoSII("C28")).longValue();
    	return c35 - p31 <= (c27 + c28) * p85;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1855.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1855(final Registro registro) {
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	final long c32 = ((Number) registro.getPorCodigoSII("C32")).longValue();
    	if("5".equalsIgnoreCase(c10)) return c32 > 0;
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1856.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1856(final Registro registro) {
    	final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
    	final long c42 = ((Number) registro.getPorCodigoSII("C42")).longValue();
    	if("F".equalsIgnoreCase(c5) || "P".equalsIgnoreCase(c5)) return c42 == 0;
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1858.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1858(final Registro registro) {
        final String c16 = String.valueOf(registro.getPorCodigoSII("C16"));
        final String c17 = String.valueOf(registro.getPorCodigoSII("C17"));
        final long c31 = ((Number) registro.getPorCodigoSII("C31")).longValue();
        
        if(c31 > 0) {
            return "1".equalsIgnoreCase(c16) && "1".equalsIgnoreCase(c17);
        }
        return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1859.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1859(final Registro registro) {
    	final String c3 = (String) registro.getPorCodigoSII("C3");
    	final String c4 = (String) registro.getPorCodigoSII("C4");
    	final String c7 = (String) registro.getPorCodigoSII("C7");
    	final String c80 = (String) registro.getPorCodigoSII("C80");
        final String c107 = (String) registro.getPorCodigoSII("C107");
    	return validarRut(c3) && validarRut(c4) && validarRut(c7) && validarRut(c80) && validarRut(c107);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1860.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1860(final Registro registro) {
    	final String c8 = (String) registro.getPorCodigoSII("C8");
    	return !"55555555-5".equalsIgnoreCase(c8);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1862.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1862(final Registro registro) {
    	final String c12 = (String) registro.getPorCodigoSII("C12");
    	return MONEDA_TIPO_REAJUSTE.contains(c12);
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1863.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1863(final Registro registro) {
    	final long c19 = ((Number) registro.getPorCodigoSII("C19")).longValue();
    	return c19 >= 0 && c19 <= 100;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1864.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1864(final Registro registro) {
    	final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
    	return c15 >= 0;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1865.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1865(final Registro registro) {
        final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
        final String c16 = String.valueOf(registro.getPorCodigoSII("C16"));
        if("7".equalsIgnoreCase(c10)){
            return PARTICIPE.contains(c16);
        }
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1866.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1866(final Registro registro) {
        final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
        final String c17 = String.valueOf(registro.getPorCodigoSII("C17"));
        if("7".equalsIgnoreCase(c10)){
            return CUOTAS.contains(c17);
        }
        return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H2043.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2043(final Registro registro) {
    	final long c36 = ((Number) registro.getPorCodigoSII("C36")).longValue();
    	final long c37 = ((Number) registro.getPorCodigoSII("C37")).longValue();
    	final long c27 = ((Number) registro.getPorCodigoSII("C27")).longValue();
    	if(c36 + c37 > 0) return c27 > 0;
    	return true;
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H2044.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2044(final Registro registro) {
    	final long c38 = ((Number) registro.getPorCodigoSII("C38")).longValue();
    	final long c39 = ((Number) registro.getPorCodigoSII("C39")).longValue();
    	final long c28 = ((Number) registro.getPorCodigoSII("C28")).longValue();
    	if(c38 + c39 > 0) return c28 > 0;
    	return true;
    }
    
    public boolean validarH2668(final Registro registro) {
    	final String c65 = String.valueOf(registro.getPorCodigoSII("C65"));
    	return TRIBUTASA.contains(c65);
    	
    }    
    
    public boolean validarH2924(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	// Validacion comun
	    if (c3.isEmpty() || "0".equalsIgnoreCase(c3) || !validarRut(c3)) {
	        return false;
	    }    	    
	    if (Integer.parseInt(version) >= 2025) {
	        try {
	            String[] partes = c3.split("-");
	            if (partes.length != 2) return false;
	            
	            int RutSinDv = Integer.parseInt(partes[0]);
	            return RutSinDv >= limiteRut;
	        } catch (NumberFormatException e) {
	            return false;
	        }
	    }    	    
	    return true;  	
    }
    
    public boolean validarH2925(final Registro registro) {
    	 final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));    	    
    	    // Validacion comun
    	    if (c4.isEmpty() || "0".equalsIgnoreCase(c4) || !validarRut(c4)) {
    	        return false;
    	    }    	    
    	    if (Integer.parseInt(version) >= 2025) {
    	        try {
    	            String[] partes = c4.split("-");
    	            if (partes.length != 2) return false;
    	            
    	            int RutSinDv = Integer.parseInt(partes[0]);
    	            return RutSinDv >= limiteRut;
    	        } catch (NumberFormatException e) {
    	            return false;
    	        }
    	    }    	    
    	    return true; 
    }
    
    public boolean validarH2926(final Registro registro) {
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));
    	
    	return (!c7.isEmpty() && !"0".equalsIgnoreCase(c7)) && validarRut(c7);
    }
    

    public boolean validarH3063(final Registro registro) {
    	final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
        final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
        final String c80 = String.valueOf(registro.getPorCodigoSII("C80"));
    	if("I".equalsIgnoreCase(c5) || "F".equalsIgnoreCase(c5)) {return !c4.equalsIgnoreCase(c80);}
    	return true;
    }

    public boolean validarH3064(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));

        return !c3.equalsIgnoreCase(c4);
    }

    public boolean validarH3065(final Registro registro) {
    	final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));

        return !c4.equalsIgnoreCase(c7);
    }

    public boolean validarH3066(final Registro registro) {
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	
        final long c13 = ((Number) registro.getPorCodigoSII("C13")).longValue();
        final String c138 = String.valueOf(registro.getPorCodigoSII("C138"));
        final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
        final String c16 = String.valueOf(registro.getPorCodigoSII("C16"));
        final String c17 = String.valueOf(registro.getPorCodigoSII("C17"));
        final long c18 = ((Number) registro.getPorCodigoSII("C18")).longValue();
        final long c19 = ((Number) registro.getPorCodigoSII("C19")).longValue();
        final long c64 = ((Number) registro.getPorCodigoSII("C64")).longValue();
        

    	if(!"7".equalsIgnoreCase(c10)) 
    		return      
                    (c138.isEmpty() || "".equalsIgnoreCase(c138) || "0".equalsIgnoreCase(c138)) && 
                    (c16.isEmpty() || "".equalsIgnoreCase(c16) || "0".equalsIgnoreCase(c16)) &&
                    (c17.isEmpty() || "".equalsIgnoreCase(c17) || "0".equalsIgnoreCase(c17)) &&
                    c13==0 && c15 == 0 && c18 == 0 && c19 == 0 && c64 == 0;
        	
    	return true;
    }

     public boolean validarH3067(final Registro registro) {
        final long c113 = ((Number) registro.getPorCodigoSII("C113")).longValue();
    	final long c133 = ((Number) registro.getPorCodigoSII("C133")).longValue();
    	final long c134 = ((Number) registro.getPorCodigoSII("C134")).longValue();
        final long c135 = ((Number) registro.getPorCodigoSII("C135")).longValue();
        final long c136 = ((Number) registro.getPorCodigoSII("C136")).longValue();
        final long c137 = ((Number) registro.getPorCodigoSII("C137")).longValue();

    	if((c133 + c134 + c135 + c136 - c137) > 0 ){
    		return (c113 == c133 + c134 + c135 + c136 - c137);
        }

    	return true;
    }


    public boolean validarH3068(final Registro registro) {
        final long c114 = ((Number) registro.getPorCodigoSII("C114")).longValue();
    	final long c133 = ((Number) registro.getPorCodigoSII("C133")).longValue();
    	final long c134 = ((Number) registro.getPorCodigoSII("C134")).longValue();
        final long c135 = ((Number) registro.getPorCodigoSII("C135")).longValue();
        final long c136 = ((Number) registro.getPorCodigoSII("C136")).longValue();
        final long c137 = ((Number) registro.getPorCodigoSII("C137")).longValue();

    	if((c133 + c134 + c135 + c136 - c137) < 0 ){
    		return (c114 == (-1 * (c133 + c134 + c135 + c136 - c137)));
        }

    	return true;
    }


    public boolean validarH3069(final Registro registro) {
        final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
        final Date c11 = (Date) registro.getPorCodigoSII("C11");

        if("7".equalsIgnoreCase(c10)){

            try {
                
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                String anoComercial = String.valueOf(Integer.parseInt(version));;
                final Date fechaComercial = formatter.parse(anoComercial + "-12-31");

                if( c11 != null){
                    return (fechaComercial.compareTo(c11) == 0); 
                }

            } catch (Exception e) {
                return false;
            }
        }
        
    	return true;
    }

    public boolean validarH3070(final Registro registro) {
        final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
    	final String c107 = String.valueOf(registro.getPorCodigoSII("C107"));

        
    	return c4.equalsIgnoreCase(c107);
    }

    public boolean validarH3071(final Registro registro) {
        try {
            final String c107 = String.valueOf(registro.getPorCodigoSII("C107"));

            // Verificar si c107 no está vacío y no es "0"
            if (!c107.isEmpty() && !"0".equalsIgnoreCase(c107)) {
                // Asignar un valor predeterminado ("") si c113 es nulo
                String c113 = (registro.getPorCodigoSII("C113") != null) ? String.valueOf(registro.getPorCodigoSII("C113")) : "";

                // Asignar un valor predeterminado ("") si c114 es nulo
                String c114 = (registro.getPorCodigoSII("C114") != null) ? String.valueOf(registro.getPorCodigoSII("C114")) : "";

                // Verificar que ni c113 ni c114 estén vacíos y no sean "0"
                return !(c113.isEmpty() || "0".equalsIgnoreCase(c113) || c114.isEmpty() || "0".equalsIgnoreCase(c114));
            }
        } catch (Exception ex) {
            // Manejo de excepciones
            return false;
        }

        return true;
    }
    
    public boolean validarH3072(final Registro registro) {
    	final String c5 = registro.getPorCodigoSII("C5") != null ? String.valueOf(registro.getPorCodigoSII("C5")): "";
        final String c80 = registro.getPorCodigoSII("C80") != null ? String.valueOf(registro.getPorCodigoSII("C80")): "";
        final String c139 = registro.getPorCodigoSII("C139") != null ? String.valueOf(registro.getPorCodigoSII("C139")): "";
        if(Integer.parseInt(version) >= 2025) {
        	return (!c139.isEmpty() && !"0".equalsIgnoreCase(c139));
        }else {
        	if (c80 != null && c80.contains("-")) {
                String[] partes = c80.split("-");
                String parteAntesGuion = partes[0];
            
                if("I".equalsIgnoreCase(c5) || "F".equalsIgnoreCase(c5)) {
                    return (parteAntesGuion.length() <= 6 && !c80.isEmpty() && !"".equalsIgnoreCase(c80));
                }
            }
    		return true;
        }   	
    }

    public boolean validarH3073(final Registro registro) {
    	final String c113 = String.valueOf(registro.getPorCodigoSII("C113"));
        final String c114 = String.valueOf(registro.getPorCodigoSII("C114"));
        final String c133 = String.valueOf(registro.getPorCodigoSII("C133"));
        final String c134 = String.valueOf(registro.getPorCodigoSII("C134"));
        final String c135 = String.valueOf(registro.getPorCodigoSII("C135"));
        final String c136 = String.valueOf(registro.getPorCodigoSII("C136"));
        final String c137 = String.valueOf(registro.getPorCodigoSII("C137"));
        
    	
        return  ((Double.parseDouble(c113) >= 0 && !c113.isEmpty() && !"".equalsIgnoreCase(c113)) &&
                (Double.parseDouble(c114) >= 0 && !c114.isEmpty() && !"".equalsIgnoreCase(c114)) &&
                (Double.parseDouble(c133) >= 0 && !c133.isEmpty() && !"".equalsIgnoreCase(c133)) &&
                (Double.parseDouble(c134) >= 0 && !c134.isEmpty() && !"".equalsIgnoreCase(c134)) &&
                (Double.parseDouble(c135) >= 0 && !c135.isEmpty() && !"".equalsIgnoreCase(c135)) &&
                (Double.parseDouble(c136) >= 0 && !c136.isEmpty() && !"".equalsIgnoreCase(c136)) &&
                (Double.parseDouble(c137) >= 0 && !c137.isEmpty() && !"".equalsIgnoreCase(c137)));
    }
    
    public boolean validarH3190(final Registro registro) {
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	final long c22 = ((Number) registro.getPorCodigoSII("C22")).longValue();
    	final long c23 = ((Number) registro.getPorCodigoSII("C23")).longValue();
    	if ("4".equalsIgnoreCase(c10)) return (c22 != 0 || c23 != 0);
    	return true;
    }
    
    public boolean validarH3191(final Registro registro) {
    	final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
    	final String c26 = String.valueOf(registro.getPorCodigoSII("C26"));
    	if(TIPO_OPERACION_H1843.contains(c10)) return !c26.isEmpty();
    	return true;
    }

    public boolean validarH3192(final Registro registro) {
    	final String c138 = String.valueOf(registro.getPorCodigoSII("C138"));
    	return TIPO_CONTRIBUYENTE.contains(c138);
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