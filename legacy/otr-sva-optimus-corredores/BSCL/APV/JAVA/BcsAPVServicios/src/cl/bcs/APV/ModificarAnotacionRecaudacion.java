package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

//Tesoreria
import cl.bcs.tesoreria.CrearIngresoEgreso;
import cl.bcs.APV.CambiarEstadoRecaudacion.ParametrosRecaudacionEstado;
import cl.bcs.APV.ProcesarDistribucion.DetalleCliente_CambiaEstado;
import cl.bcs.APV.ProcesarDistribucion.DetalleEmpleador_CambiaEstado;
import cl.bcs.APV.ProcesarDistribucion.ParametrosEntradaServicio;
import cl.bcs.APV.ProcesarDistribucion.ParametrosRecaudacion;
import cl.bcs.APV.ProcesarDistribucion.Recaudacion_CambiaEstado;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class ModificarAnotacionRecaudacion implements JavaCall   {

	/*
	 * El log
	 */	
	private final Log log = new Log("CrearAnotacionRecaudacion");

	/*
	 * Queries que van a ser utilizadas por el servicio. 
	 */	
	public String SQL_UPDATE_ANOTACION = loadResourceToString("cl/bcs/APV/sql/Anotacion_Update.sql");
	
	/********************
	 * Clases IN/OUT	*
	 ********************/

	public static class Anotacion {
			
			public Long IdRecaudacion	;
			public String Titulo		;
			public String Detalle		;
			public String IdMotivo		;
			public String IdAmbito		;
			public String IdRelevancia	;
			public String FechaDesde	;
			public String FechaHasta	;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};


	public static class Anotacion_Update{

		public Long   IdAnotacion   ;
		public String IdMotivo		;
		public String Titulo		;
		public String Detalle		;
		public String IdRelevancia	;
		public String IdAmbito		;
		public String FechaDesde	;
		public String FechaHasta	;
	}

	//  SERVICIO ENTRADA
	public static class ParametrosEntradaServicio {
		
		public 	Long									p_IdEmpresa;
		public	Long									p_IdUsuario;
		public 	String									p_IdModulo;
		//public  List<Anotacion>							p_Anotaciones;

		public Long p_IdAnotacion		;
		public String p_Titulo		;
		public String p_Detalle		;
		public String p_IdMotivo		;
		public String p_IdAmbito		;
		public String p_IdRelevancia	;
		public String p_FechaDesde	;
		public String p_FechaHasta	;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}			

	// SERVICIO SALIDA
	public static class ParametrosSalidaServicio {		
		public List<MensajeSalida>									P_OUT_MENSAJES;
		public Long													P_OUT_ERRORESCONTADOR;
		public Long													P_OUT_REFID;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ParametrosSalidaServicio(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};

	public static class ContratoConsultado {
		public  Long		IdContratoSAAPV;
	    public 	String		IdEstado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
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
		ParametrosSalidaServicio out = new ParametrosSalidaServicio();

		SQL sql = new SQL();
		ParametrosSalidaServicio 	salida 			= new ParametrosSalidaServicio ();
		//MensajeSalida               msg             = new MensajeSalida();
		Long contador = 1L;

		Long time = System.currentTimeMillis();
		java.sql.Date FechaSistema = new java.sql.Date(time);
		List<Long> idsRecaudacion = Utils.getSecuencia(con, "BSCL.APV.BD.Secuencias::REC.IdAnotacion", 1L);
		
		//for (Anotacion fila : in.p_Anotaciones){

				//Estado Recaudacion
					Anotacion_Update An_insert = new Anotacion_Update();
					List<Anotacion_Update> ListAn_insert = new ArrayList<Anotacion_Update>();
					
					An_insert.IdAnotacion   	= in.p_IdAnotacion;
					An_insert.IdMotivo			= in.p_IdAmbito;
					An_insert.Titulo			= in.p_Titulo;
					An_insert.Detalle			= in.p_Detalle;
					An_insert.IdRelevancia		= in.p_IdRelevancia;
					An_insert.IdAmbito			= in.p_IdAmbito;
					An_insert.FechaDesde		= in.p_FechaDesde;
					An_insert.FechaHasta		= in.p_FechaHasta;
					ListAn_insert.add(An_insert);
					
					sql.batch(con,SQL_UPDATE_ANOTACION,ListAn_insert,Anotacion_Update.class);
		//}
		out.P_OUT_REFID = idsRecaudacion.get(0);
		MensajeSalida msg= new MensajeSalida();
		msg.id = 1L;
		msg.tipo = MensajeSalida.MENSAJE_OK;
		msg.codigo = "OK";
		msg.descripcion = "Proceso ejecutado correctamente";
		out.P_OUT_MENSAJES.add(msg);
		return out;
	}
	/********************
	 * Método Principal *
	 ********************/

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				ModificarAnotacionRecaudacion demo = new ModificarAnotacionRecaudacion();	

				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":23,\"p_IdModulo\":\"POR\",\"p_UsaReglasNegocio\":\"N\",\"p_IdRecaudacion\":1,\"p_IdAnotacion\":39,\"p_Titulo\":\"QQQ\",\"p_Detalle\":\"ssswww\",\"p_IdMotivo\":\"FILE\",\"p_IdAmbito\":\"PUBLIC\",\"p_IdRelevancia\":\"ALTA\",\"p_FechaDesde\":\"2018-04-20\",\"p_FechaHasta\":\"2018-04-23\"}";
				ParametrosEntradaServicio Objin = gson.fromJson(json, ParametrosEntradaServicio.class);
				
	 			try { 
	 				ParametrosSalidaServicio ObjOut = demo.logica(con, Objin);
	 				con.commit();
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