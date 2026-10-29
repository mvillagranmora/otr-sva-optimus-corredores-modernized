package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.math.BigDecimal;
import java.time.format.DateTimeParseException;

import cl.bcs.normativa.data.Registro;

/**
 * Implementación del validador de la DJ1962.
 *
 * 
 *
 */
public class ValidadorDJ1962 extends Validador {

   

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
    
    private static final List<String> MONEDA_TIPO_REAJUSTE = Arrays.asList("1", "2", "3", "4", "5", "6");
    private static final List<String> CUOTAS = Arrays.asList("1", "2", "3");
    private static final List<String> TIPO_FONDO = Arrays.asList("I", "F", "P");
	private static final List<String> TRIBUTASA = Arrays.asList("1", "2");
    private static final List<String> TIPO_CONTRIBUYENTE = Arrays.asList("1","2","3");
    private static final BigDecimal TOLERANCIA = new BigDecimal("5000000");
    private final String version;
    private int LimiteRutH3259;
    private int LimiteRutH3261;



    /**
     * Constructor.
     *
     * @param contexto
     *                 El contexto de validación.
     * @throws SQLException
     *                      En caso de error SQL.
     */
    public ValidadorDJ1962(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
        LimiteRutH3259=(int)contexto.getParametroEsquemaAsDouble("LimiteRutH3259");
        LimiteRutH3261=(int)contexto.getParametroEsquemaAsDouble("LimiteRutH3261");
    }

        /**
    * Implementacion de la regla de validacion H3214.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3214(final Registro registro) {
            
            final String c1 = String.valueOf(registro.getPorCodigoSII("C1"));
            final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
            final String c27 = String.valueOf(registro.getPorCodigoSII("C27")); 
            return !"1".equals(c27) || !c1.equals(c2);
        }

    /**
    * Implementacion de la regla de validacion H3215.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3215(final Registro registro) {
        
            final BigDecimal c6 = new BigDecimal(registro.getPorCodigoSII("C6").toString());
            final BigDecimal c7 = new BigDecimal(registro.getPorCodigoSII("C7").toString());
            if (c7.compareTo(BigDecimal.ZERO) > 0){
                return c6.compareTo(BigDecimal.ZERO) == 0;
            }
            return true;
    }

    /**
    * Implementacion de la regla de validacion H3216.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3216(final Registro registro) {
            
            final BigDecimal c6 = new BigDecimal(registro.getPorCodigoSII("C6").toString());
            final BigDecimal c7 = new BigDecimal(registro.getPorCodigoSII("C7").toString());
            if (c6.compareTo(BigDecimal.ZERO) > 0){
                return c7.compareTo(BigDecimal.ZERO) == 0;
            }
            return true;
    }

    /**
    * Implementacion de la regla de validacion H3217.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3217(final Registro registro) {
        final BigDecimal c6 = new BigDecimal(registro.getPorCodigoSII("C6").toString());
        final BigDecimal c7 = new BigDecimal(registro.getPorCodigoSII("C7").toString());
        final BigDecimal c8 = new BigDecimal(registro.getPorCodigoSII("C8").toString());
        final BigDecimal c9 = new BigDecimal(registro.getPorCodigoSII("C9").toString());
        final BigDecimal c10 = new BigDecimal(registro.getPorCodigoSII("C10").toString());
        final BigDecimal c11 = new BigDecimal(registro.getPorCodigoSII("C11").toString());
        final BigDecimal c12 = new BigDecimal(registro.getPorCodigoSII("C12").toString());

        final BigDecimal sumaC6C7 = c6.add(c7);

        if (sumaC6C7.compareTo(BigDecimal.ZERO) > 0) {
            final BigDecimal sumaDetalle = c8.add(c9).add(c10).add(c11).add(c12);
            return sumaDetalle.compareTo(BigDecimal.ZERO) > 0;
        }

        return true;


    }

    /**
    * Implementacion de la regla de validacion H3220.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3220(final Registro registro) {
        final BigDecimal c7 = new BigDecimal(registro.getPorCodigoSII("C7").toString());
        final BigDecimal c8 = new BigDecimal(registro.getPorCodigoSII("C8").toString());
        final BigDecimal c9 = new BigDecimal(registro.getPorCodigoSII("C9").toString());
        final BigDecimal c10 = new BigDecimal(registro.getPorCodigoSII("C10").toString());
        final BigDecimal c11 = new BigDecimal(registro.getPorCodigoSII("C11").toString());
        final BigDecimal c12 = new BigDecimal(registro.getPorCodigoSII("C12").toString());

        if (c7.compareTo(BigDecimal.ZERO) > 0){
           final BigDecimal suma = c8.add(c9).add(c10).add(c11).subtract(c12);
            final BigDecimal absSuma = suma.abs();
            final BigDecimal diferencia = c7.subtract(absSuma).abs();
            return diferencia.compareTo(TOLERANCIA) <= 0;
        }

    return true;
    }

    /**
    * Implementacion de la regla de validacion H3221.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3221(final Registro registro) {
        final BigDecimal c6 = new BigDecimal(registro.getPorCodigoSII("C6").toString());
        final BigDecimal c8 = new BigDecimal(registro.getPorCodigoSII("C8").toString());
        final BigDecimal c9 = new BigDecimal(registro.getPorCodigoSII("C9").toString());
        final BigDecimal c10 = new BigDecimal(registro.getPorCodigoSII("C10").toString());
        final BigDecimal c11 = new BigDecimal(registro.getPorCodigoSII("C11").toString());
        final BigDecimal c12 = new BigDecimal(registro.getPorCodigoSII("C12").toString());

        if (c6.compareTo(BigDecimal.ZERO) > 0){
            final BigDecimal suma = c8.add(c9).add(c10).add(c11).subtract(c12);
            final BigDecimal absSuma = suma.abs();
            final BigDecimal diferencia = c6.subtract(absSuma).abs();
            return diferencia.compareTo(TOLERANCIA) <= 0;
        }

        return true;
    }

    /**
    * Implementacion de la regla de validacion H3223.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3223(final Registro registro) {
        final String c17 = String.valueOf(registro.getPorCodigoSII("C17")).trim();
        return !c17.equals("55555555-5");
    }

    /**
    * Implementacion de la regla de validacion H3224.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3224(final Registro registro) {
         final String c18 = String.valueOf(registro.getPorCodigoSII("C18"));
         return PAIS_RESIDENCIA.contains(c18.trim().toUpperCase());
    }

    /**
    * Implementacion de la regla de validacion H3251.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3251(final Registro registro) {
        final String c19 = String.valueOf(registro.getPorCodigoSII("C19"));
        return MONEDA_TIPO_REAJUSTE.contains(c19);
    }

    /**
    * Implementacion de la regla de validacion H3252.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3252(final Registro registro) {
        final BigDecimal c21 = new BigDecimal(registro.getPorCodigoSII("C21").toString());
        return c21.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
    * Implementacion de la regla de validacion H3253.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3253(final Registro registro) {
        final BigDecimal c25 = new BigDecimal(registro.getPorCodigoSII("C25").toString());
        return c25.compareTo(BigDecimal.ZERO) >= 0 && c25.compareTo(new BigDecimal("100")) <= 0;
    }

    /**
    * Implementacion de la regla de validacion H3254.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3254(final Registro registro) {
            final String c23 = String.valueOf(registro.getPorCodigoSII("C23"));
            return CUOTAS.contains(c23);
    }

    /**
    * Implementacion de la regla de validacion H3255.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3255(final Registro registro) {
        final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
        final String c27 = String.valueOf(registro.getPorCodigoSII("C27")); 
        return !"1".equals(c27) || TIPO_FONDO.contains(c4);
    }

    /**
    * Implementacion de la regla de validacion H3256.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3256(final Registro registro) {
        final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
        final String c27 = String.valueOf(registro.getPorCodigoSII("C27")); 
        return !"1".equals(c27) || TRIBUTASA.contains(c5);
    }

    /**
    * Implementacion de la regla de validacion H3257.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3257(final Registro registro) {
        final BigDecimal c6 = new BigDecimal(registro.getPorCodigoSII("C6").toString());
        final BigDecimal c7 = new BigDecimal(registro.getPorCodigoSII("C7").toString());
        final BigDecimal c8 = new BigDecimal(registro.getPorCodigoSII("C8").toString());
        final BigDecimal c9 = new BigDecimal(registro.getPorCodigoSII("C9").toString());
        final BigDecimal c10 = new BigDecimal(registro.getPorCodigoSII("C10").toString());
        final BigDecimal c11 = new BigDecimal(registro.getPorCodigoSII("C11").toString());
        final BigDecimal c12 = new BigDecimal(registro.getPorCodigoSII("C12").toString());
        return c6.compareTo(BigDecimal.ZERO) >= 0 && c7.compareTo(BigDecimal.ZERO) >= 0 && c8.compareTo(BigDecimal.ZERO) >= 0 && c9.compareTo(BigDecimal.ZERO) >= 0 && c10.compareTo(BigDecimal.ZERO) >= 0 && c11.compareTo(BigDecimal.ZERO) >= 0 && c12.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
    * Implementacion de la regla de validacion H3258.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3258(final Registro registro) {
        final String c27 = String.valueOf(registro.getPorCodigoSII("C27")); 
        return TIPO_CONTRIBUYENTE.contains(c27);
    }

    /**
    * Implementacion de la regla de validacion H3259.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3259(final Registro registro) {
        final String c1 = String.valueOf(registro.getPorCodigoSII("C1"));
        final String c27 = String.valueOf(registro.getPorCodigoSII("C27"));
        if ("1".equals(c27)){
            if ( (c1.isEmpty() || !validarRutModulo11(c1)) || !c1.matches("^\\d{7,8}-[0-9Kk]$")) {
            return false;   
            }
            final String rutNumerico = c1.substring(0, c1.length() - 2);
            final long numeroRut = Long.parseLong(rutNumerico);

            return numeroRut >= LimiteRutH3259;
        }
        else {
            return true;
        }
        

    }

    /**
    * Implementacion de la regla de validacion H3260.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */
    public boolean validarH3260(final Registro registro) {
        final String c15 = String.valueOf(registro.getPorCodigoSII("C15")); 
        if ( (c15.isEmpty() || !validarRutModulo11(c15)) || !c15.matches("^\\d{7,8}-[0-9Kk]$")) {
            return false;   
        }
        return true;
    }

    /**
    * Implementacion de la regla de validacion H3261.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    */

    public boolean validarH3261(final Registro registro) {
        final String c14 = String.valueOf(registro.getPorCodigoSII("C14")); 
        if  (c14.isEmpty() || !c14.matches("^\\d{7,8}-[0-9Kk]$") || !validarRutModulo11(c14))
        {
            return false;   
        }
        final String rutNumerico = c14.substring(0, c14.length() - 2);
        final long numeroRut = Long.parseLong(rutNumerico);

        return numeroRut >= LimiteRutH3261;
    }

    /**
    * Implementacion de la regla de validacion I001.
    *
    * @param registro
    *                 El registro de datos a validar.
    * @return <code>true</code> si se cumple la regla de validacion o,
    *         <code>false</code> en caso contrario.
    * Validar tipo contribuyente = 2 para los casos de DJ 1962 En Optimus Corredores
    */
    public boolean validarI001(final Registro registro) {
        final String c27 = String.valueOf(registro.getPorCodigoSII("C27")); 
        return "2".equals(c27);
    }

    private boolean validarRutModulo11(String rut) {

        if (rut == null) {
            return false;
        }
        rut = rut.trim().toUpperCase();
         if (!rut.matches("^\\d{4,8}-[0-9K]$")) {
        return false;
        }
        final String cuerpo = rut.substring(0, rut.length() - 2);
        final char dv = rut.charAt(rut.length() - 1);
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        char dvCalculado;
        if (resto == 11) {
            dvCalculado = '0';
        } else if (resto == 10) {
            dvCalculado = 'K';
        } else {
            dvCalculado = Character.forDigit(resto, 10);
        }
        return dv == dvCalculado;
    }


}




     
     