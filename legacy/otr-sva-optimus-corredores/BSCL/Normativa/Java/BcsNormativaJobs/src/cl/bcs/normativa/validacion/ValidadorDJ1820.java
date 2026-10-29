package cl.bcs.normativa.validacion;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.esquema.RangoFechas;

/**
 * Implementaci&oacute;n del validador de la DJ1820.
 *
 * 
 *
 */
public class ValidadorDJ1820 extends Validador {
	
    /**
     * Tipo de Relación con Contraparte (TRC) DJ1820.
     */
    private static final List<String> RELACION_CONTRAPARTE = Arrays.asList("1", "2", "3", "4", "99");
    private static final List<String> PAIS_RESIDENCIA = Arrays.asList("AF"	,"AL"	,"AT"	,"AU"	,"BE"	,"BM"	,"BO"	,"BR"	,"BS"	
    																 ,"CA"	,"CL"	,"CN"	,"CO"	,"CR"	,"CU"	,"CH"	,"DE"	,"DK"
    																 ,"DM"	,"DO"	,"EC"	,"ES"	,"FI"	,"FR"	,"IG"	,"GR"	,"HK"
    																 ,"ID"	,"IE"	,"IN"	,"KY"	,"IT"	,"JP"	,"KR"	,"LU"	,"NL"
    																 ,"MT"	,"MX"	,"MY"	,"NE"	,"NG"	,"NI"	,"NL"	,"NO"	,"NZ"
    																 ,"PA"	,"PE"	,"PH"	,"PK"	,"PL"	,"PR"	,"PT"	,"PY"	,"RO"
    																 ,"SA"	,"SE"	,"SG"	,"TR"	,"TT"	,"TW"	,"TZ"	,"UA"	,"UG"
    																 ,"US"	,"UY"	,"VE"	,"VN"	,"ZA"	,"HR"	,"CZ"	,"SK");

    private static final List<String> TIPO_ACUERDO_MARCO = Arrays.asList("1", "2", "3");
    private static final List<String> TIPO_EVENTO = Arrays.asList("1", "2", "3", "4", "5");
    private static final List<String> TIPO_CONTRATO = Arrays.asList("1", "2", "3", "4", "5","6", "7", "8", "9", "10","11", "12", "13", "14");
    private static final List<String> MODALIDAD_CUMPLIMIENTO = Arrays.asList("1", "2");
    private static final List<String> TIPO_ACTIVO_SUBYACENTE = Arrays.asList("1", "2", "3", "4", "5","6", "7");
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
    private static final List<String> CODIGO_TASA_INTERES = Arrays.asList("1", "2");
    private static final List<String> CODIGO_PRODUCTO_BASICO = Arrays.asList("1", "2", "3", "4", "5","6", "7", "8", "9", "10","11", "12", "13", "14");
    private static final List<String> CODIGO_TASA_VARIABLE = Arrays.asList("1", "2", "3", "4", "5","6", "7", "8", "9", "10","11", "12", "13", "14", "15","16", "17", "18", "19", "20","21", "22", "23", "24", "25","26", "27", "28", "29", "30","31", "32", "33", "34", "35","36", "37", "38", "39", "40");
    private static final List<String> CODIGO_PRECIO_FUTURO = Arrays.asList("1", "2");
    private static final List<String> TABLA_UNIDAD_MONETARIA= Arrays.asList("1", "2", "3", "4", "5","6", "7", "8", "9", "10","11", "12", "13");
    private static final List<String> MODALIDAD_CONTRATACION = Arrays.asList("1", "2", "3", "4", "5","6");
   
    
    
    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validaci&oacute;n.
     * @throws SQLException
     *             En caso de error SQL.
     */
    public ValidadorDJ1820(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
    }

    /**
     * Implementaci&oacute;n de la regla de validaci&oacute;n H1411.
     *
     * @param registro
     *            El registro de datos a validar.
     * @return <code>true</code> si se cumple la regla de validaci&oacute;n o,
     *         <code>false</code> en caso contrario.
     */
    public boolean validarH1411(final Registro registro) {
    	
    	// Los campos Código país contraparte (C11), Tipo de Relación con contraparte (C12), Modalidad de contratación (C13), 
    	// Contrato / Confirmación: Número / Identificador (C17), Contrato / Confirmación: fecha suscripción (C18), Contrato / 
    	// Confirmación: Evento Informado (C19); Contrato / Confirmación: Tipo de Contrato (C21); Contrato / Confirmación: Modalidad 
    	// de cumplimiento (C23); Activo Subyacente: Tipo (C25); Unidad (C36), Monto/cantidad contratado o nocional (C37), fecha de 
    	// vencimiento (C40),  son de llenado obligatorio y con valores validos de acuerdo al  tipo de dato
    	
    	  final String C11 = (String) registro.getPorCodigoSII("C11");
    	  final String C12 = (String) registro.getPorCodigoSII("C12");
          final String C13 = (String) registro.getPorCodigoSII("C13");
          final long C17 = (Long) registro.getPorCodigoSII("C17");
          final Date C18 = (Date) registro.getPorCodigoSII("C18");
          final String C19 = (String) registro.getPorCodigoSII("C19");
          final String C21 = (String) registro.getPorCodigoSII("C21");
          final String C23 = (String) registro.getPorCodigoSII("C23");
          final String C25 = (String) registro.getPorCodigoSII("C25");
          final String C36 = (String) registro.getPorCodigoSII("C36");
          final Double C37 = ((Number) registro.getPorCodigoSII("C37")).doubleValue();
          
          final Date C40 = (Date) registro.getPorCodigoSII("C40");
                    
        return  C11 != null && C11 != "" &&
        		C12 != null && C12 != "" &&
        		C13 != null && C13 != "" &&
        		C17  >= 0 	&&
        		C18 != null	&&
        		C19 != null && C19 != "" &&
        		C21 != null && C21 != "" &&
        		C23 != null && C23 != "" &&
        		C25 != null && C25 != "" &&
        		C36 != null && C36 != "" &&
        		C37 >= 0 	&&
        		C40 != null;
    }

    
    public boolean validarH1412(final Registro registro) {
    	
    	//La columna relación de contraparte (C12) debe indicar valores según tabla “Tipo de Relación con Contraparte (TRC)”
          final String C12 = (String) registro.getPorCodigoSII("C12");
          return C12 != null && RELACION_CONTRAPARTE.contains(C12);
              
    }

    
    public boolean validarH1413(final Registro registro) {
    	
    	// La columna código País contraparte (C11) se debe indicar valores según listado de Códigos País de Residencia para el
    	// AT respectivo, según instrucciones del suplemento tributario
          final String C11 = (String) registro.getPorCodigoSII("C11");
          return C11 != null && PAIS_RESIDENCIA.contains(C11);
    }
    
    
   public boolean validarH1414(final Registro registro) {
   	
   	// Si existe valor distinto de cero o vacío en la columna tipo de acuerdo marco (C14) entonces solo deben ser valores 
	   // según tabla Acuerdo Marco, tipo (AMT) 
  
	  final String C14 = (String) registro.getPorCodigoSII("C14");

      if (C14 != "" && C14 != "0") {
          return C14 != null && TIPO_ACUERDO_MARCO.contains(C14);
      }
      return true;
   }
     
   
   public boolean validarH1415(final Registro registro) {
	   	
	   	// La columna Evento informado (C19) debe indicar valores según tabla Evento informado (EI) 
	   final String C19 = (String) registro.getPorCodigoSII("C19");
       return C19 != null && TIPO_EVENTO.contains(C19);
   }
	     
   public boolean validarH1416(final Registro registro) {
	   	
	   // La columna tipo de contrato (C21) debe indicar valores según tabla tipo de contrato (TC) 
	  final String C21 = (String) registro.getPorCodigoSII("C21");
      return C21 != null && TIPO_CONTRATO.contains(C21);
	             
  }  
    
   public boolean validarH1417(final Registro registro) {
	   	
	   // La columna Modalidad de cumplimiento (C23) debe indicar valores según tabla Modalidad de cumplimiento (MC) 
	  final String C23 = (String) registro.getPorCodigoSII("C23");
      return C23 != null && MODALIDAD_CUMPLIMIENTO.contains(C23);
	             
  }  
        
   public boolean validarH1418(final Registro registro) {
	   	
	   // Si el tipo de contrato es distinto de  Swap entonces la  columna Posición del declarante (C24) debe indicar valores 
	   // según tabla Posición del declarante (PD)  
	   
	   final String C21 = (String) registro.getPorCodigoSII("C21");
	   final String C24 = (String) registro.getPorCodigoSII("C24");

	      if (C21 != "3" && C21 !=  "4" && C21 !=  "5") {
	          return C24 != null && ("1".equals(C24) || "2".equals(C24));
	      }
	      return true;
	             
  }  
       
   public boolean validarH1419(final Registro registro) {
	   	
	   // La columna tipo de activo subyacente (C25) debe indicar valores según tabla tipo de activo subyacente (AS)
	   
	   final String C21 = (String) registro.getPorCodigoSII("C21");
	   return C21 != null && TIPO_ACTIVO_SUBYACENTE.contains(C21);
  }  
        
   public boolean validarH1420(final Registro registro) {
	   	
	   // Al indicar tipo de Activo Subyacente (C25) Moneda {1} Se debe indicar valores en (C26) según Tabla Códigos 
	   // Monedas AT respectivo.
	   
	   final String C25 = (String) registro.getPorCodigoSII("C25");
	   final String C26 = (String) registro.getPorCodigoSII("C26");
	   
	   if ("1".equals(C25)) {
	          return C26 != null  && MONEDA_AT.contains(C26);
	   }
	   return true;
  }  
   
   
   public boolean validarH1421(final Registro registro) {
	   	
	   // Si el campo Tipo de activo subyacente (C25) es 2 (Tasa de interés), en el campo Código activo subyacente
	   //  (C26) debe indicar valores según tabla instruida (1 o 2)
	   
	   final String C25 = (String) registro.getPorCodigoSII("C25");
	   final String C26 = (String) registro.getPorCodigoSII("C26");
	   
	   if ("2".equals(C25)) {
	          return C26 != null  && CODIGO_TASA_INTERES.contains(C26);
	   }
	   return true;
  }    
   
   
   public boolean validarH1422(final Registro registro) {
	   	
	   // Al indicar tipo de Activo Subyacente (C25) Producto Básico {3} debe indicar valores en (C26) según tabla producto básico (PB)
	   
	   final String C25 = (String) registro.getPorCodigoSII("C25");
	   final String C26 = (String) registro.getPorCodigoSII("C26");
	   
	   if ("3".equals(C25)) {
	          return C26 != null  && CODIGO_PRODUCTO_BASICO.contains(C26);
	   }
	   return true;
  }    
   
          
   public boolean validarH1423(final Registro registro) {
	   	
	   // Al indicar código Activo Subyacente (C26) la opción VARIABLE {2}, cuando tipo de Activo Subyacente (C25) es tasa de interés {2}
	   // en la columna otros (C27) se debe indicar valores según tabla de tasas variables (TIV). 
	   
	   final String C26 = (String) registro.getPorCodigoSII("C26");
	   final String C27 = (String) registro.getPorCodigoSII("C27");
	   
	   if ("2".equals(C26)) {
	          return C27 != null  && CODIGO_TASA_VARIABLE.contains(C27);
	   }
	   return true;
  }    
   
   public boolean validarH1424(final Registro registro) {
	   	
	   // Si el campo Tipo de contrato (C21) es Swap (3, 4 o 5), en el campo Segundo activo subyacente (C29), debe indicar valores según 
	   // tabla activo subyacente (1, 2, 3, 4, 5, 6 o 7)
	   
	   final String C21 = (String) registro.getPorCodigoSII("C21");
	   final String C29 = (String) registro.getPorCodigoSII("C29");
	   
	   if (("3".equals(C21) || "4".equals(C21)  || "5".equals(C21))) {
	          return C29 != null  && TIPO_ACTIVO_SUBYACENTE.contains(C29);
	   }
	   return true;
  }    
   
   
   public boolean validarH1425(final Registro registro) {
	   	
	   // Al indicar tipo de segundo activo Subyacente (C29) Moneda {1} Se debe indicar en (C30) valores según Tabla Códigos Monedas AT respectivo.
	   
	   final String C29 = (String) registro.getPorCodigoSII("C29");
	   final String C30 = (String) registro.getPorCodigoSII("C30");
	   
	   if ("1".equals(C29)) {
	          return C30 != null  && MONEDA_AT.contains(C30);
	   }
	   return true;
	   
  }
   
   public boolean validarH1426(final Registro registro) {
	   	
	   // Al indicar tipo de segundo activo Subyacente (C29) Tasa de interés {2} Se debe indicar valores según tabla general instruida para códigos del TI respectivo
	   
	   final String C29 = (String) registro.getPorCodigoSII("C29");
	   final String C30 = (String) registro.getPorCodigoSII("C30");
	   
	   if ("2".equals(C29)) {
	          return C30 != null  && CODIGO_TASA_INTERES.contains(C30);
	   }
	   return true;
	   
  }
     
   public boolean validarH1427(final Registro registro) {
	   	
	   // Si el campo Tipo de segundo activo subyacente (C29) es 3 (Producto básico), en el campo Código activo subyacente (C30) debe indicar valores según tabla 
	   //instruida (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 .o. 14)
	   
	   final String C29 = (String) registro.getPorCodigoSII("C29");
	   final String C30 = (String) registro.getPorCodigoSII("C30");
	   
	   if ("3".equals(C29)) {
	          return C30 != null  && CODIGO_PRODUCTO_BASICO.contains(C30);
	   }
	   return true;
	   
  }
   
   
   
   public boolean validarH1428(final Registro registro) {
	   	
	   // Si el campo Tipo de segundo activo subyacente (C29) es 2 (Tasa de interés) y campo Código activo subyacente (C30) es 2 (variable), en el 
	   // campo Otros/Segundo activo subyacente (C31) debe indicar valores según tabla de variables (1 al 40)
	   
	   final String C29 = (String) registro.getPorCodigoSII("C29");
	   final String C31 = (String) registro.getPorCodigoSII("C31");
	   
	   if ("2".equals(C29)) {
	          return C31 != null  && CODIGO_TASA_VARIABLE.contains(C31);
	   }
	   return true;
	   
  }
      
         
   public boolean validarH1429(final Registro registro) {
	   	
	   //Si tipo de contrato ( C21 ) es distinto de Swap la columna código de precio (C33) debe indicar valores según tabla Código de precio (CP). 
	   
	   final String C21 = (String) registro.getPorCodigoSII("C21");
	   final String C33 = (String) registro.getPorCodigoSII("C33");
	   
	   if (C21 != "3" && C21 != "4" && C21 != "5") {
	          return C33 != null  && CODIGO_PRECIO_FUTURO.contains(C33);
	   }
	   return true;
	   
  }
         
   public boolean validarH1430(final Registro registro) {
	   	
	   //Si existe valor distinto de cero o vacío  en la columna moneda (C35) entonces se debe indicar valores según Tabla Códigos Monedas AT respectivo.  
	   
	   final String C35 = (String) registro.getPorCodigoSII("C35");
	   
	   if (C35 != "" && C35 != "0") {
	          return C35 != null  && MONEDA_AT.contains(C35);
	   }
	   return true;
	   
  }   
   
   
   public boolean validarH1431(final Registro registro) {
	   	
	   //En la columna unidad (C36) Se debe indicar valores según tabla unidad (UM)   
	   
	   final String C36 = (String) registro.getPorCodigoSII("C36");
	   
	   if (C36 != "") {
	          return C36 != null  && TABLA_UNIDAD_MONETARIA.contains(C36);
	   }
	   return true;
	   
  }   
     
   public boolean validarH1432(final Registro registro) {
	   	
	   //Si el campo tipo de activo subyacente (C25) es 2 y el código del activo subyacente (C26) opción ”FIJA” {1}, la columna (C28) debe ser distinto de cero 
	   // ( puede ser con signo negativo) 
	   
	   final String C25 = (String) registro.getPorCodigoSII("C25");
	   final String C26 = (String) registro.getPorCodigoSII("C26");
	   final Double C28 = ((Number) registro.getPorCodigoSII("C28")).doubleValue();
	   
	   if (C25 == "2" && C26 == "1") {
	          return C28 != null  && C28 != 0;
	   }
	   return true;
	   
  }   
   
   public boolean validarH1433(final Registro registro) {
	   	
	   // Si el campo tipo de activo subyacente (C29) es 2 y el código del segundo activo subyacente (C30) opción ”FIJA”{1}, la columna (C32) debe ser distinta 
	   // de cero (puede ser negativo). 
	   
	   final String C29 = (String) registro.getPorCodigoSII("C29");
	   final String C30 = (String) registro.getPorCodigoSII("C30");
	   final Double C32 = ((Number) registro.getPorCodigoSII("C32")).doubleValue();
	   
	   if ("2".equals(C29) && "1".equals(C30) ) {
	          return C32 != null  && C32 != 0;
	   }
	   return true;
	   
  }   
         
   public boolean validarH1434(final Registro registro) {
	   	
	   // La fecha de suscripción del contrato/confirmación (C18) debe ser anterior o igual al 31 de Diciembre del año tributario. 
	 //  private final RangoFechas rangoFechas;
	   
	   final Date C18 = (Date) registro.getPorCodigoSII("C18");
	   final RangoFechas rangoFechas =this.getContexto().getEsquema().getRangoFechas();
	   
	   final Date fechaHasta = rangoFechas.getFechaHasta();

		if (C18 != null) {
	  		final long com = C18.compareTo(fechaHasta);
			return (com) <= (0);
		  }
	    return true;
			   
  }   
         

   public boolean validarH1436(final Registro registro) {
	   	
	   // El campo Modalidad de contratación (C13) debe indicar valores según tabla instruida (1, 2, 3, 4, 5, 6)
	   
	   final String C13 = (String) registro.getPorCodigoSII("C13");
	   
	   if (C13 != "") {
	          return C13 != null  && MODALIDAD_CONTRATACION.contains(C13);
	   }
	   return true;
  }   
   
   public boolean validarH1437(final Registro registro) {
	   	
	   // Si no existe Rut de la contraparte (C8) debe existir Tax ID de la contraparte (C10)
	   
	   final String C8 = (String) registro.getPorCodigoSII("C8");
	   final String C10 = (String) registro.getPorCodigoSII("C10");
	   
	   if ("".equals(C8)) {
	          return C10 != null  && C10 != "";
	   }
	   return true;
  }      
   
   public boolean validarH1438(final Registro registro) {
	   	
	   // Si tipo de contrato (C21) es {3,4,5}, posición de declarante (C24) debe ser cero
	   
	   final String C21 = (String) registro.getPorCodigoSII("C21");
	   final String C24 = (String) registro.getPorCodigoSII("C24");
	   
	   if ("3".equals(C21) || "4".equals(C21) || "5".equals(C21)) {
	          return "0".equals(C24);
	   }
	   return true;
  }      

   
   public boolean validarH1439(final Registro registro) {
	   	
	   // Si tipo de contrato (C21) es distinto de swap {3,4,5}, las columnas del segundo
	   // activo subyacente (C29), (C30), (C31), (C32) son vacías o cero de acuerdo al tipo de dato
	   
	   final String C21 = (String) registro.getPorCodigoSII("C21");
	   final String C29 = (String) registro.getPorCodigoSII("C29");
	   final String C30 = (String) registro.getPorCodigoSII("C30");
	   final String C31 = (String) registro.getPorCodigoSII("C31");
	   final Double C32 = ((Number) registro.getPorCodigoSII("C32")).doubleValue();
	   
	   if (C21 != "3" && C21 != "4" && C21 != "5") {
	          return "0".equals(C29) && "".equals(C30) && "".equals(C31) && C32 == 0;
	   }
	   return true;
  }      

   public boolean validarH1440(final Registro registro) {
	   	
	   // Si tipo de contrato (C21) es swap {3,4,5}, las columnas asociadas a precios de futuros contratados (C33), (C34),(C35) son vacías. 
	   
	   final String C21 = (String) registro.getPorCodigoSII("C21");
	   final String C33 = (String) registro.getPorCodigoSII("C33");
	   final Double C34 = ((Number) registro.getPorCodigoSII("C34")).doubleValue();
	   final String C35 = (String) registro.getPorCodigoSII("C35");
	   	   
	   
	   if ("3".equals(C21) || "4".equals(C21) || "5".equals(C21)) {
	          return "".equals(C33) && C34==null  && "".equals(C35);
	   }
	   return true;
  }       
   
   public boolean validarH1441(final Registro registro) {
	   	
	   // Si código de precio (C33) es 2, no debe existir moneda (C35) ni precio (C34)  es decir C34 debe ser cero y C35 debe ser vacío 
	   
	   final String C33 = (String) registro.getPorCodigoSII("C33");
	   final Double C34 = ((Number) registro.getPorCodigoSII("C34")).doubleValue();
	   final String C35 = (String) registro.getPorCodigoSII("C35");
	   
	   	   	   
	   
	   if ("2".equals(C33)) {
	          return C34==2  && "".equals(C35);
	   }
	   return true;
  }       
   
   public boolean validarH1442(final Registro registro) {
	   	
	   // Debe existir al menos un valor mayor que cero en las columnas (C8) y (C10) 
	   
	   final String C8 = (String) registro.getPorCodigoSII("C8");
	   final String C10 = (String) registro.getPorCodigoSII("C10");
	  
	   return (C8 !="0" && C8 !="")  || (C8 !="0" &&  C10 !="");
	  
  }       
     
   public boolean validarH1443(final Registro registro) {
	   	
	   // Si tipo de contrato (C21) es igual a opción otros {12,13,14}, debe existir nombre del instrumento (C22)  
	   
	   final String C21 = (String) registro.getPorCodigoSII("C21");
	   final String C22 = (String) registro.getPorCodigoSII("C22");
	  

	   if ("12".equals(C21)||"13".equals(C21)||"14".equals(C21)) {
	          return C22 !="" && C22 != null ;
	   }
	   return true;
	  
  } 
   
   public boolean validarH1444(final Registro registro) {
	   	
	   // Si tipo de activo subyacente (C25) es igual a opción otros {7}, debe existir nombre del instrumento (C27)   
	   
	   final String C25 = (String) registro.getPorCodigoSII("C25");
	   final String C27 = (String) registro.getPorCodigoSII("C27");
	  

	   if ("7".equals(C25)) {
	          return C27 !="" && C27 != null ;
	   }
	   return true;
	  
  }   
   
   public boolean validarH1446(final Registro registro) {
	   	
	   // Al indicar tipo de contrato (C21) la opción Swap {3,4,5}  la columna Segunda unidad (solo Swap)  (C38)  
	   // y en monto segundo nocional swap (C39),  deben ser distinto de cero   
	   
	   final String C21 = (String) registro.getPorCodigoSII("C21");
	   final String C38 = (String) registro.getPorCodigoSII("C38");
	   final Double C39 = ((Number) registro.getPorCodigoSII("C39")).doubleValue();
	  

	   if ("3".equals(C21) || "4".equals(C21) || "5".equals(C21)) {
	          return C38 !="0" && C39 != 0 ;
	   }
	   return true;
	  
  }   
 
   public boolean validarH1447(final Registro registro) {
	   	
	   // Si existe unidad (C36) debe existir monto de unidad (C37)    
	   
	   final String C36 = (String) registro.getPorCodigoSII("C36");
	   final Double C37 = ((Number) registro.getPorCodigoSII("C37")).doubleValue();
	  

	   if (C36 !="" && C36 != null) {
	          return C37 != null;
	   }
	   return true;
	  
  }   
  
   public boolean validarH1448(final Registro registro) {
	   	
	   // Si tipo de contrato es swap (3 , 4, 5) y  existe segunda unidad (C38) debe existir segundo monto nocional (C39)   
	   
	   final String C21 = (String) registro.getPorCodigoSII("C21");
	   final String C38 = (String) registro.getPorCodigoSII("C38");
	   final Double C39 = ((Number) registro.getPorCodigoSII("C39")).doubleValue();

	   if (("3".equals(C21) || "4".equals(C21) || "5".equals(C21)) &&  C38  !="" && C38 != null ) {
	          return C39 != null;
	   }
	   return true;
	  
  }   

   public boolean validarH1449(final Registro registro) {
	   	
	   // Si existe fecha de suscripción del acuerdo marco (C16), debe ser anterior o igual a la fecha del contrato de confirmación. (C18)    
	   
	   final Date C16 = (Date) registro.getPorCodigoSII("C16");
	   final Date C18 = (Date) registro.getPorCodigoSII("C18");
	   
		if (C16 != null ) {
			return C16.compareTo(C18) <= 0;
		  }
	    return true;
				   
      
	  
  } 

     
   public boolean validarH1450(final Registro registro) {
	   	
	   // Si la fecha de vencimiento o liquidacion (C40) es distinta de  01011900; debe ser  mayor o igual a la fecha de suscripción del contrato (C18)     
	   
	   final Date C40 = (Date) registro.getPorCodigoSII("C40");
	   final Date C18 = (Date) registro.getPorCodigoSII("C18");

	   try {
   			
		  SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
  		  final Date date = formatter.parse("01-01-1900");
		   
   			if (C40 != date) {
   				//final long com = C40.compareTo(C18);
   				return  C40.compareTo(C18) >= 0;
			  }
		    return true;
				   
   		} catch (ParseException e) {
   			return false;
   		}	   
	  
  } 
   
   
}
