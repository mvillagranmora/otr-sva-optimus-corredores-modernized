package cl.bcs.APV;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ConsultarPreferenciaPagoPortafolio implements JavaCall   {
	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/APV/sql/ConsultarPreferenciasPagoPortafolioCliente.sql");

	/********************
	 * Clases IN/OUT	*
	 ********************/
	
	public static class Cliente{
		public Long 	IdCliente;
	}
	
	public static class PreferenciaPagoPortafolio{
		public Long		IdPreferenciaPagoPortafolio;
		public Long 	IdPortafolio;
	}
	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long					p_IdEmpresa;
		public	Long					p_IdUsuario;
		public 	String					p_IdModulo;
		public	Long					p_IdCliente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		public List<PreferenciaPagoPortafolio>	P_OUT_PREFERENCIA_PAGO_PORTAFOLIO;

		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_PREFERENCIA_PAGO_PORTAFOLIO = new ArrayList<PreferenciaPagoPortafolio>();
		}		
	};

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ParametrosEntradaServicio jin = gson.fromJson(in.toString() ,ParametrosEntradaServicio.class);
		ParametrosSalidaServicio out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	/************************
	 * Métodos Básicos	*	
	 ************************/
	
	public ParametrosSalidaServicio logica(Connection con,ParametrosEntradaServicio in)throws Exception {
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		MensajeSalida               msg             = new MensajeSalida();
		PreferenciaPagoPortafolio	PrePagPor		= new PreferenciaPagoPortafolio();
					
		SQL sql = new SQL();			
			
		HashMap<String,Object> paramsPreferenciaPP = new HashMap<String,Object>();
		paramsPreferenciaPP.put("IdCliente", in.p_IdCliente);
			
		try{				
			List<PreferenciaPagoPortafolio> preferenciasPagoPortafolio = sql.query(con, SQL_PREFERENCIA_PAGO_PORTAFOLIO, paramsPreferenciaPP, PreferenciaPagoPortafolio.class);
			
			for ( int i = 0; i < preferenciasPagoPortafolio.size(); i ++ ) {
				PrePagPor	= new PreferenciaPagoPortafolio();

				PrePagPor.IdPortafolio = preferenciasPagoPortafolio.get(i).IdPortafolio;
				PrePagPor.IdPreferenciaPagoPortafolio = preferenciasPagoPortafolio.get(i).IdPreferenciaPagoPortafolio;
				
				salida.P_OUT_PREFERENCIA_PAGO_PORTAFOLIO.add(PrePagPor); 
			}
			
			msg.id = 1L; 
			msg.tipo = "APV";
			msg.codigo = "OK";
			msg.descripcion = "Proceso Completado.";
			salida.P_OUT_MENSAJES.add(msg);
			salida.P_OUT_ERRORESCONTADOR = 0L;
			
		}catch(Exception e){
			msg.id = 1L; 
			msg.tipo = "APV";
			msg.codigo = "ERROR";
			msg.descripcion = e.getMessage();
			salida.P_OUT_MENSAJES.add(msg);
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}			

		return salida;
	}
	
	
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getUATConnection()) {
			try {				
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":39,\"p_IdModulo\":\"APV\",\"p_IdCliente\":100291}";
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);	
				
				ConsultarPreferenciaPagoPortafolio demo = new ConsultarPreferenciaPagoPortafolio();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
	 				//con.commit();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
	 			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
	
}