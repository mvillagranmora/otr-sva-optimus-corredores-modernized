package cl.bcs.mesa.MRF;

import java.sql.Connection;

import java.util.ArrayList;

import java.util.HashMap;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


//import cl.bcs.mesa.SPT.CrearOperacionSpot.OperacionObjOut;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class MarcarOperacionMandato implements JavaCall{
	
	private final Log log = new Log("MRF.MarcarOperacionMandato");

	private final String SQL_UPDATE_OPERACIONES = loadResourceToString("cl/bcs/mesa/MRF/sql/MarcarOperacionMandato_UpdateOperaciones.sql");
	
	public static class MarcarOperacionMandatoEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		
		public String p_TipoInformeGenerado; //ADENDUM-MANDATO
		public List<Operacion> 		p_Operacion;
		
		public MarcarOperacionMandatoEntrada() {
			this.p_Operacion 		= new ArrayList<Operacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class Operacion {
		public Long IdOperacion;//					BIGINT
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class MarcarOperacionMandatoSalida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		public Long						P_OUT_ADVERTENCIASCONTADOR;
		
		public MarcarOperacionMandatoSalida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RegistroActualizar{
		
		
		public Long 	IdEmpresa;
		public Long     IdOperacion;
		public String 	TipoInformeGenerado;
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		MarcarOperacionMandatoEntrada in = gson.fromJson(jin.toString(), MarcarOperacionMandatoEntrada.class);
		MarcarOperacionMandatoSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		long c = 0;
		for (MensajeSalida s : out.P_OUT_MENSAJES) {
			if (s.tipo.equals("AUTORIZACION"))
				c++;
		}
		out.P_OUT_ADVERTENCIASCONTADOR = c;
		
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public MarcarOperacionMandatoSalida logica(Connection conn, MarcarOperacionMandatoEntrada in) throws Exception {
		SQL _sql = new SQL();
		MarcarOperacionMandatoSalida out = new MarcarOperacionMandatoSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_ADVERTENCIASCONTADOR = 0L;
		Long Id = 0L;

		HashMap<String,String> mapMsg = new HashMap<>();
		
		log.write("INICIO :: MRF.MarcarOperacionMandato");
		
		try {
			
			
		
			/***** VARIABLES *****/
			
			List<RegistroActualizar> actualizar = new ArrayList<RegistroActualizar>();
			
			/***** DATOS DE ACTUALIZACION OPERACION  *****/
				for(Operacion solicitud : in.p_Operacion){
					RegistroActualizar reg = new RegistroActualizar();
					reg.IdEmpresa=in.p_IdEmpresa;
					reg.IdOperacion=	solicitud.IdOperacion;
					reg.TipoInformeGenerado=in.p_TipoInformeGenerado;
					actualizar.add(reg);
				}
			int[] resUpdate = _sql.batch(conn, SQL_UPDATE_OPERACIONES, actualizar, RegistroActualizar.class);
			if(resUpdate!=null && resUpdate.length>0 && resUpdate[0]>0){
				//mapMsg.put("Operacion", params.p_FechaOperacion);
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_CAMBIO_EXITO",mapMsg,conn,in.p_IdEmpresa));
				Id++;
			}else{
				if(resUpdate[0]==0){
				//mapMsg.put("FechaOperacion", params.p_FechaOperacion);
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_SIN_INFORMACION",mapMsg,conn,in.p_IdEmpresa));
				Id++;
				}else{
				//mapMsg.put("FechaOperacion", params.p_FechaOperacion);
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_CAMBIO_ERROR",mapMsg,conn,in.p_IdEmpresa));
				Id++;};
			}
			
			/***** SALIDA *****/
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
		}
		catch(Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			log.write(e);
			throw e;
		}
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
						
			String jsonDesa = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_Operacion\":[{\"IdOperacion\":2296}],\"p_TipoInformeGenerado\":\"MANDATO\"}";
			String jsonDesa2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_Operacion\":[{\"IdOperacion\":2296}],\"p_TipoInformeGenerado\":\"ADENDUM\"}";
			
			MarcarOperacionMandatoEntrada Objin = gson.fromJson(jsonDesa2, MarcarOperacionMandatoEntrada.class);
			
			MarcarOperacionMandato demo = new MarcarOperacionMandato();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				MarcarOperacionMandatoSalida ObjOut = demo.logica(con, Objin);
 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
			finally {
				con.close();
			}
	    }	
		System.out.println("== END ==");
	}
}