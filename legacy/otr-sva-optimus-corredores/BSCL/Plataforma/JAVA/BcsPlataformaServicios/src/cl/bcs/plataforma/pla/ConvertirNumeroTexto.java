package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.dbcp2.BasicDataSource;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class ConvertirNumeroTexto {	
	
	private static final String[] decenas = {
		    "",
		    " diez",
		    " veinte",
		    " treinta",
		    " cuarenta",
		    " cincuenta",
		    " sesenta",
		    " setenta",
		    " ochenta",
		    " noventa"
		  };
	
	private static final String[] centenas = {
		    "",
		    " ciento",
		    " doscientos",
		    " trescientos",
		    " cuatrocientos",
		    " quinientos",
		    " seiscientos",
		    " setecientos",
		    " ochocientos",
		    " novecientos"
		  };
	
		  private static final String[] numeros = {
		    "",
		    " uno",
		    " dos",
		    " tres",
		    " cuatro",
		    " cinco",
		    " seis",
		    " siete",
		    " ocho",
		    " nueve",
		    " diez",
		    " once",
		    " doce",
		    " trece",
		    " catorce",
		    " quince",
		    " dieciseis",
		    " diecisiete",
		    " dieciocho",
		    " diecinueve",
		    " veinte",
		    " veintiuno",
		    " veintidos",
		    " veintitres",
		    " veinticuatro",
		    " veinticinco",
		    " veintiseis",
		    " veintisiete",
		    " veintiocho",
		    " veintinueve",
		  };
	
	private static String convertLessThanOneThousand(int number) {
	    String soFar;
	    
	    if (number == 100) {
	    	return "cien";
	    }
	    else if (number % 100 < 30){
	      soFar = numeros[number % 100];
	      number /= 100;
	    }
	    //> 20
	    else {
	      soFar = numeros[number % 10];
	      number /= 10;
	 
	      if (soFar.equalsIgnoreCase("")) {
		      soFar = decenas[number % 10];
		      number /= 10;	    	  
	      }
	      else {
	    	  soFar = decenas[number % 10] + " y"+soFar;
		      number /= 10;	
	      }
	    	  
	    }
	    if (number == 0) return soFar;
	    return centenas[number] + soFar;
	  }
	public static String convert(long numeroCompleto,String textoMoneda) {
		return convert(new BigDecimal(numeroCompleto),false,textoMoneda);
	}
	public static String convert(BigDecimal numeroCompleto) {
		return convert(numeroCompleto,false,"");
	}
	public static String convert(long numeroCompleto) {
		return convert(new BigDecimal(numeroCompleto),false,"");
	}
	public static String convert(BigDecimal numeroCompleto,boolean decimalesEnFraccion,String textoMoneda) {
	    // 0 to 999 999 999 999
		long number = numeroCompleto.longValue();
		BigDecimal decimales = numeroCompleto.setScale(2, RoundingMode.FLOOR).subtract(numeroCompleto.setScale(0, RoundingMode.FLOOR));
		decimales = decimales.movePointRight(decimales.scale());
		
	    if (number == 0) {
	    	return "cero";
	    }
	 
	    String snumber = Long.toString(number);
	 
	    // pad with "0"
	    String mask = "000000000000";
	    DecimalFormat df = new DecimalFormat(mask);
	    snumber = df.format(number);
	 
	    // XXXnnnnnnnnn
	    int billions = Integer.parseInt(snumber.substring(0,3));
	    // nnnXXXnnnnnn
	    int millions  = Integer.parseInt(snumber.substring(3,6));
	    // nnnnnnXXXnnn
	    int hundredThousands = Integer.parseInt(snumber.substring(6,9));
	    // nnnnnnnnnXXX
	    int thousands = Integer.parseInt(snumber.substring(9,12));    
	 
	    String tradBillions;
	    switch (billions) {
	    case 0:
	    	tradBillions = "";
	    	break;
	    case 1 :
	    	tradBillions = "un billon ";
	    	break;
	    default :
	    	tradBillions = convertLessThanOneThousand(billions) + " billones ";
	    }
	    String result =  tradBillions;
	 
	    String tradMillions;
	    switch (millions) {
	    case 0:
	    	tradMillions = "";
	    	break;
	    case 1 :
	    	tradMillions = "un millon ";
	    	break;
	    default :
	    	tradMillions = convertLessThanOneThousand(millions) + " millones ";
	    }
	    result =  result + tradMillions;
	 
	    String tradHundredThousands;
	    switch (hundredThousands) {
	    case 0:
	    	tradHundredThousands = "";
	    	break;
	    case 1 :
	    	tradHundredThousands = "mil ";
	    	break;
	    default :
	    	tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " mil ";
	    }
	    result =  result + tradHundredThousands;
	 
	    String tradThousand;
	    tradThousand = convertLessThanOneThousand(thousands);
	    result =  result + tradThousand;
	 
	    // remove extra spaces!
	    String res = result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
	    if (decimalesEnFraccion == true) {
	    	return res + " con " + decimales + "/100" + " " + textoMoneda;
	    }
	    else {
	    	return res + " " + textoMoneda;
	    }
	}
	
	/**********************
	 * main pruebas
	 **********************/
	public static void main(String[] args){
		//System.out.println("*** " + convert(0));
	    //System.out.println("*** " + convert(1));
	    //System.out.println("*** " + convert(16));
		System.out.println("*** " + convert(new BigDecimal(120.000)));
		/*System.out.println("*** " + convert(2000000));
		System.out.println("*** " + convert(new BigDecimal("100.11234234"),true,"SOLES"));
		System.out.println("*** " + convert(1101));
	    System.out.println("*** " + convert(1000));
	    System.out.println("*** " + convert(1001));
	    System.out.println("*** " + convert(219));
	    System.out.println("*** " + convert(800));
	    System.out.println("*** " + convert(801));
	    System.out.println("*** " + convert(1316));
	    
	    System.out.println("*** " + convert(2000000));
	    System.out.println("*** " + convert(3000200));
	    System.out.println("*** " + convert(700000));
	    System.out.println("*** " + convert(9000000));
	    System.out.println("*** " + convert(9001000));
	    System.out.println("*** " + convert(123456789));
	    System.out.println("*** " + convert(2147483647));
	    System.out.println("*** " + convert(3000000010L));*/
	}

	
}
