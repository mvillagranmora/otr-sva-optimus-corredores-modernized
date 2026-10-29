package cl.bcs.plataforma.acc.utils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.Doc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ClaveProvisional {
	
	//-------------------------------------------------
	// CLASES DE ENTRADA 
	//-------------------------------------------------
	
	
    //-------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT	
	//-------------------------------------------------------------------------------------------
	
	public static class ClaveProvisoria_Salida{

		public String 								Clave ="";
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

	public static class Resultado {
		String res = "";
		
		@Override
		public String toString()
		{
			return Utils.printJson(this, false);
		}

	}

	//-------------------------------------------------
	// LOGICA DEL SERVICIO 
	//-------------------------------------------------
	public ClaveProvisoria_Salida logica() throws Exception
	{
		int numRandom;
		int letRandom;
		int numRandom2;
		int letRandom2;
		int letRandom3;
		int letRandom4;
		
		String [] abecedarioMayus = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
				"K", "L", "M","N","O","P","Q","R","S","T","U","V","W", "X","Y","Z" };
		String [] abecedarioMinus = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", 
				"k", "l", "m","n","o","p","q","e","s","t","u","v","w", "x","y","z" };
		String [] numeros = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

				numRandom = (int) Math.round(Math.random()*9);
				numRandom2 = (int) Math.round(Math.random()*9);
				letRandom = (int) Math.round(Math.random()*25);
				letRandom2 = (int) Math.round(Math.random()*25);
				letRandom3 = (int) Math.round(Math.random()*25);
				letRandom4 = (int) Math.round(Math.random()*25);
				/*
				System.out.println(numRandom);
				System.out.println(letRandom);
				System.out.println(numRandom2);
				System.out.println(letRandom2);
				System.out.println(letRandom3);
				System.out.println(letRandom4);
				System.out.println("----");
				
				
				
				
				
				System.out.println(numRandom);
				System.out.println(numeros[numRandom]);
				System.out.println(abecedarioMayus[letRandom3]);
				System.out.println(abecedarioMinus[letRandom]);
				System.out.println(numeros[numRandom2]);
				System.out.println(abecedarioMinus[letRandom2]);
				System.out.println(abecedarioMayus[letRandom4]);*/
				String ClaveP= numeros[numRandom] + "" + abecedarioMayus[letRandom3]  +""+ abecedarioMinus[letRandom] +""+ numeros[numRandom2] +""+ abecedarioMinus[letRandom2] +""+ abecedarioMayus[letRandom4];
						
		
		
		ClaveProvisoria_Salida salida = new ClaveProvisoria_Salida();
		
		Resultado res = new Resultado();

		salida.Clave = ClaveP;

		//salida.Clave=(res.toString()); 
		
		return salida;
				
	}
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	//-------------------------------------------------------------------------------------------
	
	
	//-------------------------------------------------
	// METODO PARA PRUEBAS LOCALES 
	//-------------------------------------------------
	public static void main(String[] args)
	{
		
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			SQL.exec(con, "set schema BSCL");
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			
			
			String jsonCL ="";
			
			
			ClaveProvisional demo = new ClaveProvisional();
			//System.out.println("ENTRADA");
			//System.out.println(Objin.toString());
			
			try {
				ClaveProvisoria_Salida ObjOut = demo.logica();
				con.rollback();
				con.commit();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	private void GeneraElementosError(Connection con, Long idEmpresa, String codMsg, List<MensajeSalida> P_OUT_MENSAJES) 
			throws Exception {
		
		String msg	=	Diccionario.getMensaje(con,idEmpresa,codMsg)
						.msg();
		
		P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,codMsg,msg));
		
	}

}