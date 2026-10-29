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


import cl.bcs.normativa.data.Registro;

/**
 * Implementaci&oacute;n del validador de la DJ19446.
 *
 * 
 *
 */
public class ValidadorDJ1946 extends Validador {

    private static final List<String> OPERACION = Arrays.asList("6", "7", "8", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21");

    private static final List<String> TIPORENTA = Arrays.asList("1", "2", "4", "5", "6", "7", "8", "9", "10","11","12","13","14","15","16","17");

    private static final List<String> TIPOJURIDICO = Arrays.asList("1", "2", "3", "4", "5", "6", "7");

    private static final List<String> RELACION = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");

    private static final List<String> TIPODOMICILIO = Arrays.asList("1", "2", "3");

    private static final List<String> CODIGO = Arrays.asList(   "650", "653", "657", "660", "662", "602", "606", "600", "604", "26", "28",
                                                                "664", "667", "670", "673", "676", "679", "681", "686", "691", "696", "701",
                                                                "682", "712", "709", "714", "717", "721", "723", "50", "52", "54", "56", "729",
                                                                "732", "735", "738", "58", "741", "745", "749", "753", "742", "746", "750",
                                                                "743", "747", "751", "757", "759", "763", "765", "766", "768", "770", "774",
                                                                "771", "775", "778", "772", "776", "780");

    private static final List<String> MONEDA = Arrays.asList("AFN","ALL","EUR","AOA","XCD","SAR","DZD","ARS","AMD","AWG","AUD","AZN","BSD","BHD","BDT","BBD",
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

	private static final List<String> PAIS   = Arrays.asList(	"AF", "AL", "AD", "AO", "AI", "AQ", "AG", "AN", "SA", "DZ", "AR", "AM", "AW", "AU", "AT", "AZ", "BS", "BH",
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
                                                                "YE", "YU", "ZM", "ZW");

    private static final List<String> CODIGO_H2742 = Arrays.asList( "50", "52", "729", "732", "58", "719", "721", "706", "707", "709",
                                                                    "711", "712", "713", "714", "721", "244", "717" );

    private static final List<String> CODIGO_H2743 = Arrays.asList( "54", "56", "735", "738" );

    private static final List<String> CODIGO_H2744 = Arrays.asList( "650", "653", "657", "660", "759", "763" );

    private static final List<String> CODIGO_H2745 = Arrays.asList( "681", "682", "684", "686", "687", "689", "691", "692", "694",
                                                                    "696", "697", "699", "701", "702", "704", "765", "766", "768" );

    private static final List<String> CODIGO_H2746 = Arrays.asList( "662", "664", "602", "600", "607", "606", "604", "670", "26",
                                                                    "673", "28", "676" );

    private static final List<String> CODIGO_H2747 = Arrays.asList( "741", "742", "743", "745", "746", "747", "749", "750", "751", "753",
                                                                    "757", "770", "771", "772", "774", "775", "776", "778" );

    private static final List<String> CODIGO_H2748 = Arrays.asList( "272" );

    private static final List<String> CODIGO_H2749 = Arrays.asList( "780", "723" );

    private static final List<String> CODIGO_H2750 = Arrays.asList( "723" );

    private static final List<String> CODIGO_H2751 = Arrays.asList( "706", "707", "709", "711", "712", "714", "721", "244", "717" );

    private static final List<String> CODIGO_H2752 = Arrays.asList( "706", "707", "709", "711", "712", "714", "717" );

    private static final List<String> CODIGO_H2753 = Arrays.asList( "719", "721" );


	private final String version;
	
	/**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1946(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        version = contexto.getEsquema().getVersion();
    }
    

    
    
    /**
     * Implementacion de la regla de validacion H2490.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2490(final Registro registro) {
		try{
			
            final String c1 = String.valueOf(registro.getPorCodigoSII("C1"));
            final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));
            final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
            final String c4 = String.valueOf(registro.getPorCodigoSII("C4"));
            final String c7 = String.valueOf(registro.getPorCodigoSII("C7"));

            if((c1 == "0" || c1 == null || c1.isEmpty()) || (c2 == "0" || c2 == null || c2.isEmpty()) || (c3 == "0" || c3 == null || c3.isEmpty())
            && (c4 == "0" || c4 == null || c4.isEmpty()) || (c7 == "0" || c7 == null || c7.isEmpty())){
                return false;
            }

			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2491.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2491(final Registro registro) {

        final String c2 = String.valueOf(registro.getPorCodigoSII("C2"));

        return OPERACION.contains(c2);
			
			
    }

    /**
     * Implementacion de la regla de validacion H2492.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2492(final Registro registro) {
		
        final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));

        return TIPORENTA.contains(c3);
    }

    /**
     * Implementacion de la regla de validacion H2493.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2493(final Registro registro) {
		

        final long c5 = ((Number) registro.getPorCodigoSII("C5")).longValue();

        
        return c5 >= 0 && c5 <= 100;
        
			
			
    }
    
    /**
     * Implementacion de la regla de validacion H2494.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2494(final Registro registro) {
		try{
			
            final long c6 = ((Number) registro.getPorCodigoSII("C6")).longValue();

    
	        if ( c6 < 0){
	            return false;
	        }
            

			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2495.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2495(final Registro registro) {
		try{
			
            final long c9 = ((Number) registro.getPorCodigoSII("C9")).longValue();

            if ( c9 < 0){
                return false;
            }
            

			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2496.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2496(final Registro registro) {
		
        final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));

        return CODIGO.contains(c10);
    }

    /**
     * Implementacion de la regla de validacion H2497.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2497(final Registro registro) {
        // Si la fecha de vencimiento o liquidacion (C40) es distinta de  01011900; debe ser  mayor o igual a la fecha de suscripción del contrato (C18)     
	   
	   final Date c12 = (Date) registro.getPorCodigoSII("C12");

	   try {
   			
		  SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
  		  final Date date = formatter.parse("01-01-1900");
		   
   			if (date.compareTo(c12) == 0) {
   				return false;
			  }
		    return true;

				   
   		} catch (ParseException e) {
   			return false;
   		}	   
	  
    }

    /**
     * Implementacion de la regla de validacion H2498.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2498(final Registro registro) {
		try{

            final String c13 = String.valueOf(registro.getPorCodigoSII("C13"));

            return MONEDA.contains(c13);
			
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2499.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2499(final Registro registro) {
        
        
        final String c15 = String.valueOf(registro.getPorCodigoSII("C15"));

        return TIPOJURIDICO.contains(c15);    
    }
			

    /**
     * Implementacion de la regla de validacion H2500.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2500(final Registro registro) {
	
        final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
        final String c16 = String.valueOf(registro.getPorCodigoSII("C16"));
        final String c17 = String.valueOf(registro.getPorCodigoSII("C17"));
        final String c18 = String.valueOf(registro.getPorCodigoSII("C18"));
        final String c22 = String.valueOf(registro.getPorCodigoSII("C22"));
            
        if( c15 == 1){
            if( ( !c16.equals("0") && !c17.equals("0") ) && ( !c18.equals("0") && !c22.equals("0") ) ){
                return true;
            }else {
            	return false;
            }
        }else {
        	return true;
        }
    }

    /**
     * Implementacion de la regla de validacion H2501.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2501(final Registro registro) {

    	final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
        final String c16 = String.valueOf(registro.getPorCodigoSII("C16"));
        final String c18 = String.valueOf(registro.getPorCodigoSII("C18"));
        final String c22 = String.valueOf(registro.getPorCodigoSII("C22"));

        if( c15 != 1 ){
            if(  ( !c16.equals("0") && !c18.equals("0") ) && !c22.equals("0")){
                return true;
            }else {
            	return false;
            }
        }else {
        	return true;
        }   	
    }

    /**
     * Implementacion de la regla de validacion H2502.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2502(final Registro registro) {
		
        final String c22 = String.valueOf(registro.getPorCodigoSII("C22"));

        return RELACION.contains(c22);   
    	
    }

    /**
     * Implementacion de la regla de validacion H2503.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2503(final Registro registro) {
		try{

            final String c24 = String.valueOf(registro.getPorCodigoSII("C24"));

            return TIPOJURIDICO.contains(c24);   
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2504.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2504(final Registro registro) {
		try{

            final long c24 = ((Number) registro.getPorCodigoSII("C24")).longValue();
            final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));
            final String c26 = String.valueOf(registro.getPorCodigoSII("C26"));
            final String c27 = String.valueOf(registro.getPorCodigoSII("C27"));

            if( c24 == 1){
                if( (c25 == "0" || c26 == "0") || c27 == "0"  ){
                    return false;
                }
            }
			
			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2505.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2505(final Registro registro) {

        try{
			final long c24 = ((Number) registro.getPorCodigoSII("C24")).longValue();
            final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));
            final String c26 = String.valueOf(registro.getPorCodigoSII("C26"));

            if( c24 == 1){
                if( c25 == "0" || c26 == "0" ){
                    return false;
                }
            }

			return true;
    	} catch (Exception e) {
    		return false;
    	}
    
    }

    /**
     * Implementacion de la regla de validacion H2506.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2506(final Registro registro) {

        final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));
        return PAIS.contains(c25);
		
    }

    /**
     * Implementacion de la regla de validacion H2507.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2507(final Registro registro) {
		try{
			
            final String c32 = String.valueOf(registro.getPorCodigoSII("C32"));

            return TIPODOMICILIO.contains(c32);
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2508.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2508(final Registro registro) {

		final String c35 = String.valueOf(registro.getPorCodigoSII("C35"));
        return PAIS.contains(c35);
    }

    /**
     * Implementacion de la regla de validacion H2509.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2509(final Registro registro) {

		final String c36 = String.valueOf(registro.getPorCodigoSII("C36"));


        return TIPOJURIDICO.contains(c36);
    }

    /**
     * Implementacion de la regla de validacion H2510.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2510(final Registro registro) {

        final String c36 = String.valueOf(registro.getPorCodigoSII("C36"));
        final String c37 = String.valueOf(registro.getPorCodigoSII("C37"));
        final String c38 = String.valueOf(registro.getPorCodigoSII("C38"));
        final String c39 = String.valueOf(registro.getPorCodigoSII("C39"));
		
        if ( c36 == "1" ){
            if ( ( c37 == "0" || c38 == "0" ) || c39 == "0"  ){
                return false;  
            }
        }

		return true;
    	
    }

    /**
     * Implementacion de la regla de validacion H2511.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2511(final Registro registro) {
	
		final String c36 = String.valueOf(registro.getPorCodigoSII("C36"));
        final String c37 = String.valueOf(registro.getPorCodigoSII("C37"));
        final String c38 = String.valueOf(registro.getPorCodigoSII("C38"));
		
        if ( c36 != "1" ){
            if ( c37 == "0" ||  c38 == "0" ){
                return false;  
            }
        }
		
		return true;
    	
    }

    /**
     * Implementacion de la regla de validacion H2512.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2512(final Registro registro) {
		
        final String c37 = String.valueOf(registro.getPorCodigoSII("C37"));
        return PAIS.contains(c37);
    }

  

   
    /**
     * Implementacion de la regla de validacion H2740.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2740(final Registro registro) {
		try{
			
            final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
            final String c5 = String.valueOf(registro.getPorCodigoSII("C5"));
            final String c6 = String.valueOf(registro.getPorCodigoSII("C6"));
            final String c11 = String.valueOf(registro.getPorCodigoSII("C11"));


            if( c3 == 1 || c3 == 2){
                if( (c5.isEmpty() || c5 == null) && (c6.isEmpty() || c6 == null) && (c11.isEmpty() || c11 == null) ){
                    return false;
                }
            }

			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2741.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2741(final Registro registro) {
		try{
			final List<String> T_RENTA = Arrays.asList("3", "4", "5", "6", "7", "8", "9");
            final String c3 = String.valueOf(registro.getPorCodigoSII("C3"));
            final long c5 = ((Number) registro.getPorCodigoSII("C5")).longValue();;
            final long c6 = ((Number) registro.getPorCodigoSII("C6")).longValue();
            final long c10 = ((Number) registro.getPorCodigoSII("C10")).longValue();
            final long c11 = ((Number) registro.getPorCodigoSII("C11")).longValue();


        

            if( T_RENTA.contains(c3) ){
                
            	if(c5+c6+c10+c11 == 0) {
                    return true;
                }else{
                    return false;
                }
            }
            return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2742.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2742(final Registro registro) {
		try{

            final long c2 = ((Number) registro.getPorCodigoSII("C2")).longValue();
            final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
            final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
			
            if( (c3 == 1 || c3 == 2) && c2 == 7 ){
                return CODIGO_H2742.contains(c10);
            }else{
                return true;
            }

    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2743.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2743(final Registro registro) {
		try{

            final long c2 = ((Number) registro.getPorCodigoSII("C2")).longValue();
            final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
            final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
			
            if( (c3 == 1 || c3 == 2) && c2 == 8 ){
                return CODIGO_H2743.contains(c10);
            }else{
                return true;
            }
			
		
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2744.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2744(final Registro registro) {
		try{

            final long c2 = ((Number) registro.getPorCodigoSII("C2")).longValue();
            final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
            final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
			
            if( (c3 == 1 || c3 == 2) && c2 == 10 ){
                return CODIGO_H2744.contains(c10);
            }else{
                return true;
            }
			
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2745.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2745(final Registro registro) {
		try{

            final long c2 = ((Number) registro.getPorCodigoSII("C2")).longValue();
            final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
            final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
			
            if( (c3 == 1 || c3 == 2) && c2 == 11 ){
                return CODIGO_H2745.contains(c10);
            }else{
                return true;
            }
			
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2746.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2746(final Registro registro) {
		try{

            final long c2 = ((Number) registro.getPorCodigoSII("C2")).longValue();
            final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
            final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
			
            if( (c3 == 1 || c3 == 2) && c2 == 12 ){
                return CODIGO_H2746.contains(c10);
            }else{
                return true;
            }
			

    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2747.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2747(final Registro registro) {
		try{

            final long c2 = ((Number) registro.getPorCodigoSII("C2")).longValue();
            final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
            final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
			
            if( (c3 == 1 || c3 == 2) && c2 == 13 ){
                return CODIGO_H2747.contains(c10);
            }else{
                return true;
            }
			

    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2748.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2748(final Registro registro) {
		try{

            final long c2 = ((Number) registro.getPorCodigoSII("C2")).longValue();
            final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
            final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
			
            if( (c3 == 1 || c3 == 2) && c2 == 16 ){
                return CODIGO_H2748.contains(c10);
            }else{
                return true;
            }

    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2749.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2749(final Registro registro) {
		try{

            final long c2 = ((Number) registro.getPorCodigoSII("C2")).longValue();
            final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
            final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
			
            if( (c3 == 1 || c3 == 2) && c2 == 21 ){
                return CODIGO_H2749.contains(c10);
            }else{
                return true;
            }
			
		
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2750.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2750(final Registro registro) {
		try{

			final long c2 = ((Number) registro.getPorCodigoSII("C2")).longValue();
            final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
            final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
			
            if( (c3 == 1 || c3 == 2) && ( c2 == 6 || c2 == 18 ) && ( c2 == 19 || c2 == 20 ) ){
               return CODIGO_H2750.contains(c10);
            }else{
                return true;
            }


    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2751.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2751(final Registro registro) {
		try{

            final long c2 = ((Number) registro.getPorCodigoSII("C2")).longValue();
            final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
            final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
			
            if( (c3 == 1 || c3 == 2) && c2 == 14 ){
                return CODIGO_H2751.contains(c10);
            }else{
                return true;
            }

    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2752.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2752(final Registro registro) {
		try{

            final long c2 = ((Number) registro.getPorCodigoSII("C2")).longValue();
            final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
            final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));

            if( (c3 == 1 || c3 == 2) && c2 == 15 ){
                return CODIGO_H2752.contains(c10);
            }else{
                return true;
            }

    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2753.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2753(final Registro registro) {
		try{

            final long c2 = ((Number) registro.getPorCodigoSII("C2")).longValue();
            final long c3 = ((Number) registro.getPorCodigoSII("C3")).longValue();
            final String c10 = String.valueOf(registro.getPorCodigoSII("C10"));
			
            if( (c3 == 1 || c3 == 2) && c2 == 17 ){
                return CODIGO_H2753.contains(c10);
            }else{
                return true;
            }

    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2754.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2754(final Registro registro) {
		final Date c12 = (Date) registro.getPorCodigoSII("C12");

    	try{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    	String anio = String.valueOf(Integer.parseInt(version)+1);
			final Date dateFin = formatter.parse(anio + "-12-31");

            if( c12 != null){
                return (dateFin.compareTo(c12) > 0 || dateFin.compareTo(c12) == 0); 
            }
			
		}catch(Exception e){
			return false;
		}
		return false;   
    }

    /**
     * Implementacion de la regla de validacion H2755.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2755(final Registro registro) {
		try{

            final String c16 = String.valueOf(registro.getPorCodigoSII("C16"));
            final String c17 = String.valueOf(registro.getPorCodigoSII("C17"));
            final String c23 = String.valueOf(registro.getPorCodigoSII("C23"));

            if( ( ( !c16.isEmpty() || c16 != null ) || c16 != "0" || (( !c17.isEmpty() || c17 != null ) || c17 != "0") ) || 
            ( ( !c23.isEmpty() || c23 != null ) || c23 != "0" ) ){
                if ( ( c16.length() < 3 || c17.length() < 3 ) || c23.length() < 3){
                    return false;
                }
            }
            
			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2863.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2756(final Registro registro) {
		try{

            final String c26 = String.valueOf(registro.getPorCodigoSII("C26"));
            final String c27 = String.valueOf(registro.getPorCodigoSII("C27"));

            if( ( ( !c26.isEmpty() || c26 != null ) || c26 != "0" || (( !c27.isEmpty() || c27 != null ) || c27 != "0") ) ){
                if ( ( c26.length() < 3 || c27.length() < 3 ) ){
                    return false;
                }
            }
			
			
			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2863.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2757(final Registro registro) {
		try{

            final String c20 = String.valueOf(registro.getPorCodigoSII("C20"));
            final String c21 = String.valueOf(registro.getPorCodigoSII("C21"));

            if( (( !c20.isEmpty() || c20 != null ) || c20 != "0" ) || (( !c21.isEmpty() || c21 != null ) || c21 != "0" )  ){
                if ( c20.length() < 3 && c21.length() < 3){
                    return false;
                }
            }
			
			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2863.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2758(final Registro registro) {
		try{

            final String c33 = String.valueOf(registro.getPorCodigoSII("C33"));
            final String c34 = String.valueOf(registro.getPorCodigoSII("C34"));

            if( (( !c33.isEmpty() || c33 != null ) || c33 != "0" ) && (( !c34.isEmpty() || c34 != null ) || c34 != "0" )  ){
                if ( c33.length() < 3 && c34.length() < 3){
                    return false;
                }
            }
			
			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2863.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2759(final Registro registro) {
		try{
			final String c33 = String.valueOf(registro.getPorCodigoSII("C33"));
            final String c34 = String.valueOf(registro.getPorCodigoSII("C34"));
            final String c38 = String.valueOf(registro.getPorCodigoSII("C38"));
            final String c39 = String.valueOf(registro.getPorCodigoSII("C39"));

            if( (( !c38.isEmpty() || c38 != null ) || c38 != "0" ) || (( !c39.isEmpty() || c39 != null ) || c39 != "0" )  ){
                if ( c33.length() < 3 && c34.length() < 3){
                    return false;
                }
            }
			
			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2863.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2760(final Registro registro) {
		try{

            final long c8 = ((Number) registro.getPorCodigoSII("C8")).longValue();
            

            if(c8 < 0){
                return false;
            }
			
			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2863.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2761(final Registro registro) {
		// Si la fecha de vencimiento o liquidacion (C40) es distinta de  01011900; debe ser  mayor o igual a la fecha de suscripción del contrato (C18)     
	   
        final Date c31 = (Date) registro.getPorCodigoSII("C31");
    	try{
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	    	String anio = String.valueOf(Integer.parseInt(version)+1);
			final Date dateFin = formatter.parse(anio + "-12-31");
			return (dateFin.compareTo(c31) > 0 || dateFin.compareTo(c31) == 0);
		}catch(Exception e){
			return false;
		}   
	  
    }
    
    /**
     * Implementacion de la regla de validacion H2863.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2762(final Registro registro) {
		try{

            final long c24 = ((Number) registro.getPorCodigoSII("C24")).longValue();
            final String c27 = String.valueOf(registro.getPorCodigoSII("C27"));

            if( c24 != 1 ) {
                if( (c27 != null || c27 != "0") || c27.isEmpty() ){
                    return false;
                }
            }
			
			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2863.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2763(final Registro registro) {
		try{

            final long c36 = ((Number) registro.getPorCodigoSII("C36")).longValue();
            final String c39 = String.valueOf(registro.getPorCodigoSII("C39"));

            if( c36 != 1 ) {
                if( (c39 != null || c39 != "0" ) || c39.isEmpty() ){
                    return false;
                }
            }
			
			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2863.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2764(final Registro registro) {
		try{

            final long c15 = ((Number) registro.getPorCodigoSII("C15")).longValue();
            final String c17 = String.valueOf(registro.getPorCodigoSII("C17"));

            if( c15 != 1 ) {
                if( (c17 != null || c17 != "0" ) || c17.isEmpty() ){
                    return false;
                }
            }
			
			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2863.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2765(final Registro registro) {
		try{

            final String c37 = String.valueOf(registro.getPorCodigoSII("C37"));
            final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));
            final String c38 = String.valueOf(registro.getPorCodigoSII("C38"));
            final String c26 = String.valueOf(registro.getPorCodigoSII("C26"));

            if( c37.equals(c25) ){
                if( c38.equals(c26)){
                    return false;
                }
            }

			
			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2863.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2766(final Registro registro) {
		try{

            final String c37 = String.valueOf(registro.getPorCodigoSII("C37"));
            final String c25 = String.valueOf(registro.getPorCodigoSII("C25"));
            final String c39 = String.valueOf(registro.getPorCodigoSII("C39"));
            final String c27 = String.valueOf(registro.getPorCodigoSII("C27"));

            if( c37.equals(c25) ){
                if( c39.equals(c27)){
                    return false;
                }
            }
			
			return true;
    	} catch (Exception e) {
    		return false;
    	}
    }

    /**
     * Implementacion de la regla de validacion H2863.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validacion o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH2767(final Registro registro) {
		try{
			
			final String c36 = String.valueOf(registro.getPorCodigoSII("C36"));
            final String c37 = String.valueOf(registro.getPorCodigoSII("C37"));
            final String c38 = String.valueOf(registro.getPorCodigoSII("C38"));
            final String c39 = String.valueOf(registro.getPorCodigoSII("C39"));
            final String c40 = String.valueOf(registro.getPorCodigoSII("C40"));

            if ( (c37 != "0" || c38 != "0") || (c39 != "0" || c40 != "0")){
                if( c36 == "0"){
                    return false;
                }
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