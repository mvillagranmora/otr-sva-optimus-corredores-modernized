package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;


import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ1932.
 *
 * 
 *
 */
public class ValidadorDJ1932 extends Validador {

	private static final List<String> NUMERO_MES = Arrays.asList("1","2","3","4","5","6","7","8","9","10","11","12","21","29");
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
    public ValidadorDJ1932(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
    }
    
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H2192.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2192(final Registro registro) {
    	final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
    	return NUMERO_MES.contains(c3);
    }
    
	
    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H2193.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2193(final Registro registro) {
    	final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
    	if(c6.isEmpty()){
    		return !c6.isEmpty();
    	}else{
    		return PAIS_RESIDENCIA.contains(c6);
    	}
    }
    
    
}