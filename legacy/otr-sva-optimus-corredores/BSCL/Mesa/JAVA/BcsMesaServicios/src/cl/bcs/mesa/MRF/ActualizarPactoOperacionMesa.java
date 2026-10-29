package cl.bcs.mesa.MRF;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.MES.IngresoOperacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarPactoOperacionMesa implements JavaCall{
	private static final Logger logger = LogManager.getLogger(ActualizarPactoOperacionMesa.class);
	private final String SQL_UPDATE_OPERACION_MESA = loadResourceToString("cl/bcs/mesa/sql/ModificarOperacion_UpdateOperacion.sql");
	
	public static class ActualizarPactoOperacionMesa_IN {
		public Long 				p_IdEmpresa;
		public Long 				p_IdUsuario;
		public String 				p_IdModulo; 
		public String				p_UsaReglasNegocio;
		public List<Operacion> 		p_Operaciones;
		
		public ActualizarPactoOperacionMesa_IN() {
			this.p_Operaciones 		= new ArrayList<Operacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class Operacion {
		public Long		IdOperacion;
		public String	ConfirmaAdemdum;
		public Long     IdUsuarioConfirma;
	}
		
	public static class ActualizarPactoOperacionMesa_OUT {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public ActualizarPactoOperacionMesa_OUT() {
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public ActualizarPactoOperacionMesa_OUT logica (Connection conn, ActualizarPactoOperacionMesa_IN in) throws Exception {
		SQL _sql = new SQL();
		logger.info("Inicio servicio MRF.ActualizarPactoOperacionMesa");
		
		HashMap<String,String> mapMsg = new HashMap<>();
		ActualizarPactoOperacionMesa_OUT out = new ActualizarPactoOperacionMesa_OUT();
		Long Id = 0L;
		
		for(int i=0;i<in.p_Operaciones.size();i++) {
			in.p_Operaciones.get(i).IdUsuarioConfirma = in.p_IdUsuario;
		}

	    /***** MODIFICACION OPERACION MESA *****/
		_sql.batch(conn, SQL_UPDATE_OPERACION_MESA, in.p_Operaciones,Operacion.class);
	    
		//out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION,"UI_MENSAJE_PACTO_CONFIRMACION",mapMsg,conn,in.p_IdEmpresa));
		out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_OPERACION_REALIZADA_EXITO",mapMsg,conn,in.p_IdEmpresa));
		
	    logger.info("Fin servicio MRF.ActualizarPactoOperacionMesa");
		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		ActualizarPactoOperacionMesa_IN jin = gson.fromJson(in.toString(), ActualizarPactoOperacionMesa_IN.class);
		ActualizarPactoOperacionMesa_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args)  throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			//datos entrada
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":6,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_Operaciones\":[{\"IdOperacion\":2851,\"ConfirmaAdemdum\":\"S\"}]}";
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":51,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_Operaciones\":[{\"IdOperacion\":11702,\"IdEstadoSiguiente\":\"ANU\"}]}";
			
			
			ActualizarPactoOperacionMesa_IN objIn = gson.fromJson(json, ActualizarPactoOperacionMesa_IN.class);
			//mientras pruebo hare el rollback
			ActualizarPactoOperacionMesa demo = new ActualizarPactoOperacionMesa();
			
			ActualizarPactoOperacionMesa_OUT ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			
			conn.commit();
			//conn.rollback();
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
		System.out.println("== END ==");

	};
	
}
