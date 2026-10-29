package cl.bcs.mesa.SPT;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AnularRegistroCargaMasivaOperacionSpot implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(AnularRegistroCargaMasivaOperacionSpot.class);
	
	private final String SQL_SELECT_OPERACION = loadResourceToString("cl/bcs/mesa/SPT/sql/AnularRegistroCargaMasivaSpot_SelectOperacion.sql");
	private final String SQL_UPDATE_OPERACION = loadResourceToString("cl/bcs/mesa/SPT/sql/AnularRegistroCargaMasivaSpot_Update.sql");
	
	public static class EntradaAnularRegistroCargaMasivaOperacionSpot extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<OperacionSpot> p_Operaciones;
		
		public EntradaAnularRegistroCargaMasivaOperacionSpot() {
			this.p_Operaciones = new ArrayList<OperacionSpot>();
		}
		
	}
	
	public static class OperacionSpot extends BcsPojo {
		public Long IdCargaMasiva;
	}
	public static class OperacionSpotDetalle extends BcsPojo {
		public Long IdCargaMasivaDetalle;
	}
	
	public static class SalidaAnularRegistroCargaMasivaOperacionSpot extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaAnularRegistroCargaMasivaOperacionSpot() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaAnularRegistroCargaMasivaOperacionSpot jin = gson.fromJson(in.toString(), EntradaAnularRegistroCargaMasivaOperacionSpot.class);
		SalidaAnularRegistroCargaMasivaOperacionSpot out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAnularRegistroCargaMasivaOperacionSpot logica(Connection con, EntradaAnularRegistroCargaMasivaOperacionSpot entrada) throws Exception{
		logger.info("INICIO::AnularRegistroCargaMasivaOperacionSpot");
		SalidaAnularRegistroCargaMasivaOperacionSpot out = new SalidaAnularRegistroCargaMasivaOperacionSpot();
		
		out = validarOperaciones(con, entrada);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<OperacionSpot> operacionesSpotAnular = obtenerOperacionesAnular(con, entrada.p_Operaciones);
		out = AnularOperaciones(con, operacionesSpotAnular);
		
		logger.info("FIN::AnularRegistroCargaMasivaOperacionSpot");
		return out;
	}
	
	private SalidaAnularRegistroCargaMasivaOperacionSpot validarOperaciones(Connection con, EntradaAnularRegistroCargaMasivaOperacionSpot entrada) throws Exception{
		logger.trace("INICIO::AnularRegistroCargaMasivaOperacionSpot.validarOperaciones");
		SalidaAnularRegistroCargaMasivaOperacionSpot out = new SalidaAnularRegistroCargaMasivaOperacionSpot();
		
		List<Long> idOperaciones = entrada.p_Operaciones.stream()
											.map(operacion->operacion.IdCargaMasiva)
											.collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdCargaMasiva", idOperaciones);
		
		SQL sql = new SQL();
		
		List<OperacionSpot> operacionesEncontradas = sql.query(con, SQL_SELECT_OPERACION, parametros, OperacionSpot.class);
		Map<Long, OperacionSpot> operacionesEncontradasPorId = operacionesEncontradas.stream()
															.collect(Collectors.toMap(operacion->operacion.IdCargaMasiva, operacion->operacion));
		for(OperacionSpot operacion : operacionesEncontradas) {
			OperacionSpot operacionEncontrada = operacionesEncontradasPorId.get(operacion.IdCargaMasiva);
			if(operacionEncontrada == null) {
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
				parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacion.IdCargaMasiva));
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"OPERACION_SPOT_CARGA_NO_ENCONTRADA",parametrosMensaje,con,entrada.p_IdEmpresa));
			}
		}
		

		
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		logger.trace("FIN::AnularRegistroCargaMasivaOperacionSpot.validarOperaciones");
		return out;
	}
	
	private List<OperacionSpot> obtenerOperacionesAnular(Connection con, List<OperacionSpot> operaciones) throws Exception{
		logger.trace("INICIO::AnularRegistroCargaMasivaOperacionSpot.obtenerOperacionesAnular");
		List<Long> idOperaciones = operaciones
				.stream()
				.map(operacion->operacion.IdCargaMasiva)
				.collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdCargaMasiva", idOperaciones);
		
		SQL sql = new SQL();
		List<OperacionSpot> operacionesEncontradas = sql.query(con, SQL_SELECT_OPERACION, parametros, OperacionSpot.class);
		
		List<OperacionSpot> operacionesAnular = new ArrayList<OperacionSpot>();
		operacionesAnular.addAll(operacionesEncontradas);
		operacionesAnular.addAll(operaciones);
		
		logger.trace("FIN::AnularRegistroCargaMasivaOperacionSpot.obtenerOperacionesAnular");
		return operacionesAnular;
	}
	
	
	
	private SalidaAnularRegistroCargaMasivaOperacionSpot AnularOperaciones(Connection con, List<OperacionSpot> operaciones) throws Exception{
		logger.trace("INICIO::AnularRegistroCargaMasivaOperacionSpot.AnularOperaciones");
		SalidaAnularRegistroCargaMasivaOperacionSpot out = new SalidaAnularRegistroCargaMasivaOperacionSpot();
		SQL sql = new SQL();
		MensajeSalida msg;
		try {
			int[] res = sql.batch(con, SQL_UPDATE_OPERACION, operaciones, OperacionSpot.class);
			
			if(res!=null && res.length>0 && res[0]==1){
				
				
				msg = new MensajeSalida();
				msg.id = 1L;
				msg.tipo = "INFORMACION";
				msg.codigo = "CARGA_MASIVA_OK";
				msg.descripcion = "Anulacion de carga masiva exitosa.";
				out.P_OUT_MENSAJES.add(msg);
				
				
			}else{
				msg = new MensajeSalida();
				msg.id = 1L;
				msg.tipo = "ERROR";
				msg.codigo = "CARGA_MASIVA_ERROR";
				msg.descripcion = "Error al Anular la Carga masiva.";
				out.P_OUT_MENSAJES.add(msg);
			}
		}
		catch(Exception e){
			msg = new MensajeSalida();
			msg.id = 1L;
			msg.tipo = "INFORMACION";
			msg.codigo = "ERROR_CONECCION";
			msg.descripcion = e.getMessage();
			out.P_OUT_MENSAJES.add(msg);
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		}
		logger.trace("FIN::AnularRegistroCargaMasivaOperacionSpot.AnularOperaciones");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();			
		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SPT\",\"p_UsaReglasNegocio\":\"S\",\"p_SimularOperacion\":\"N\",\"p_Operaciones\":[{\"IdCargaMasiva\":50}]}";
		EntradaAnularRegistroCargaMasivaOperacionSpot Objin = gson.fromJson(json, EntradaAnularRegistroCargaMasivaOperacionSpot.class);
		AnularRegistroCargaMasivaOperacionSpot demo = new AnularRegistroCargaMasivaOperacionSpot();				
		try {
			SalidaAnularRegistroCargaMasivaOperacionSpot ObjOut = demo.logica(con, Objin);
			con.commit();
//			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
						
    	};
		System.out.println("== END ==");
	}

}
