package cl.bcs.APV;

import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.APV.GrabarRecaudacionTraspaso.ParametrosEntradaServicio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CambiarEstadoTraspaso implements JavaCall   {

	/*
	 * El log
	 */	
	private final Log log = new Log("CambiarEstadoTraspaso");

	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_CAMBIAR_ESTADO_TRASPASO = loadResourceToString("cl/bcs/APV/sql/CambiarEstadoTraspasoAPV.sql");
	
	/********************
	 * Clases IN/OUT	*
	 ********************/
	public static class ParametrosTraspasoEstado{
		public Long IdContratoSAAPV;
		public Long IdEmpresa;
		public Long IdTraspaso;
		public String IdEstado;
	}
	
	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		public 	Long							p_IdEmpresa;
		public	Long							p_IdUsuario;
		public 	String							p_IdModulo;
		public  List<ParametrosTraspasoEstado>	p_Parametros;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}			

	public static class EstadoTraspaso {
		public String 		IdEstado;
		public Long			IdEmpresa;
		public Long			IdTraspaso;
		public Long			IdContratoSAAPV;	
		public Date 		CierreSolicitud;
	}
	
	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
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
		SQL sql = new SQL();
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		//MensajeSalida               msg             = new MensajeSalida();
		List<EstadoTraspaso> lstTraspaso = new ArrayList<EstadoTraspaso>();
		
		for (ParametrosTraspasoEstado fila : in.p_Parametros){
			//CAMBIO ESTADOS
			try {
				/*Se llena Lista para cambiar Estado Traspaso Egreso*/
				EstadoTraspaso Traspaso = new EstadoTraspaso();
				Traspaso.IdContratoSAAPV = fila.IdContratoSAAPV == null ? null : fila.IdContratoSAAPV;
				Traspaso.IdTraspaso = fila.IdTraspaso;
				Traspaso.IdEmpresa = fila.IdEmpresa;
				Traspaso.IdEstado = fila.IdEstado;
				Traspaso.CierreSolicitud = null;
				lstTraspaso.add(Traspaso);
				
			    int[] result = sql.batch(con,SQL_CAMBIAR_ESTADO_TRASPASO,lstTraspaso, EstadoTraspaso.class);
				if(result != null && result.length > 0 && result[0] == 1){
				    MensajeSalida msg = new MensajeSalida();
					msg.id = 1L;
					msg.tipo = "OK";
					msg.codigo = "CAMBIAR_ESTADO_TRASPASO";
					msg.descripcion = "Cambio de Estado de Traspaso exitoso. Folio: " + fila.IdTraspaso.toString();
					salida.P_OUT_MENSAJES.add(msg);
				}else {
					MensajeSalida msg = new MensajeSalida();
					msg.id = 1L;
					msg.tipo = "ADVERTENCIA";
					msg.codigo = "ADVERTENCIA_CAMBIAR_ESTADO_TRASPASO";
					msg.descripcion = "No existe traspaso para cambio de estado  Folio: " + fila.IdTraspaso.toString();
					salida.P_OUT_MENSAJES.add(msg);
				}	
			}catch(Exception error) {
				MensajeSalida msg = new MensajeSalida();
				msg.id = 1L;
				msg.tipo = "ERROR";
				msg.codigo = "ERROR_CAMBIAR_ESTADO_TRASPASO";
				msg.descripcion = "Problema al cambiar estado a traspaso Folio: " + fila.IdTraspaso.toString();
				salida.P_OUT_MENSAJES.add(msg);
			}
			 
		}
		return salida;
	}
	/********************
	 * Método Principal *
	 ********************/
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				ParametrosEntradaServicio Objin = new ParametrosEntradaServicio();
				CambiarEstadoTraspaso demo = new CambiarEstadoTraspaso();	
				
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();	
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":23,\"p_IdModulo\":\"APV\",\"p_Parametros\":[{\"IdEstado\":\"COM\",\"IdEmpresa\":1,\"IdTraspaso\":124,\"IdContratoSAAPV\":0}]}";
				
				ParametrosEntradaServicio Objin_json = gson.fromJson(json, ParametrosEntradaServicio.class);
				
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin_json);
	 				con.commit();
	 				//System.out.println(ObjOut.toString());
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