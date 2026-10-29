package cl.bcs.mesa.MRF;

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

public class EliminarRegistroCargaMasivaOperacionesTermino implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(EliminarRegistroCargaMasivaOperacionesTermino.class);
	
	private final String SQL_SELECT_OPERACION = loadResourceToString("cl/bcs/mesa/MRF/sql/EliminarRegistroCargaMasivaOperacionesTermino_SelectOperacion.sql");
	private final String SQL_SELECT_OPERACION_DETALLE = loadResourceToString("cl/bcs/mesa/MRF/sql/EliminarRegistroCargaMasivaOperacionesTermino_SelectOperacionDetalle.sql");
	private final String SQL_UPDATE_OPERACION = loadResourceToString("cl/bcs/mesa/MRF/sql/EliminarRegistroCargaMasivaOperacionesTermino_UpdateOperacionDetalle.sql");
	
	public static class EntradaEliminarRegistroCargaMasivaOperacionesTermino extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_SimularOperacion;
		public List<OperacionTermino> p_Operaciones;
		
		public EntradaEliminarRegistroCargaMasivaOperacionesTermino() {
			this.p_Operaciones = new ArrayList<OperacionTermino>();
		}
		
	}
	
	public static class OperacionTermino extends BcsPojo {
		public Long IdCargaMasivaDetalle;
	}
	
	public static class SalidaEliminarRegistroCargaMasivaOperacionesTermino extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaEliminarRegistroCargaMasivaOperacionesTermino() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEliminarRegistroCargaMasivaOperacionesTermino jin = gson.fromJson(in.toString(), EntradaEliminarRegistroCargaMasivaOperacionesTermino.class);
		SalidaEliminarRegistroCargaMasivaOperacionesTermino out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaEliminarRegistroCargaMasivaOperacionesTermino logica(Connection con, EntradaEliminarRegistroCargaMasivaOperacionesTermino entrada) throws Exception{
		logger.info("INICIO::EliminarRegistroCargaMasivaOperacionesTermino");
		SalidaEliminarRegistroCargaMasivaOperacionesTermino out = new SalidaEliminarRegistroCargaMasivaOperacionesTermino();
		
		out = validarOperaciones(con, entrada);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		List<OperacionTermino> operacionesTerminoEliminar = obtenerOperacionesEliminar(con, entrada.p_Operaciones);
		eliminarOperaciones(con, operacionesTerminoEliminar);
		
		logger.info("FIN::EliminarRegistroCargaMasivaOperacionesTermino");
		return out;
	}
	
	private SalidaEliminarRegistroCargaMasivaOperacionesTermino validarOperaciones(Connection con, EntradaEliminarRegistroCargaMasivaOperacionesTermino entrada) throws Exception{
		logger.trace("INICIO::EliminarRegistroCargaMasivaOperacionesTermino.validarOperaciones");
		SalidaEliminarRegistroCargaMasivaOperacionesTermino out = new SalidaEliminarRegistroCargaMasivaOperacionesTermino();
		
		List<Long> idOperaciones = entrada.p_Operaciones
				.stream()
				.map(operacion->operacion.IdCargaMasivaDetalle)
				.collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdCargaMasivaDetalle", idOperaciones);
		
		SQL sql = new SQL();
		List<OperacionTermino> operacionesEncontradas = sql.query(con, SQL_SELECT_OPERACION, parametros, OperacionTermino.class);
		Map<Long, OperacionTermino> operacionesEncontradasPorId = operacionesEncontradas
				.stream()
				.collect(Collectors.toMap(operacion->operacion.IdCargaMasivaDetalle, operacion->operacion));
		
		for(OperacionTermino operacion : entrada.p_Operaciones) {
			OperacionTermino operacionEncontrada = operacionesEncontradasPorId.get(operacion.IdCargaMasivaDetalle);
			if(operacionEncontrada == null) {
				HashMap<String,String> parametrosMensaje = new HashMap<String,String>(2);
				parametrosMensaje.put("IdCargaMasivaDetalle", String.valueOf(operacion.IdCargaMasivaDetalle));
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"OPERACION_TERMINO_CARGA_NO_ENCONTRADA",parametrosMensaje,con,entrada.p_IdEmpresa));
			}
		}
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		logger.trace("FIN::EliminarRegistroCargaMasivaOperacionesTermino.validarOperaciones");
		return out;
	}
	
	private List<OperacionTermino> obtenerOperacionesEliminar(Connection con, List<OperacionTermino> operaciones) throws Exception{
		logger.trace("INICIO::EliminarRegistroCargaMasivaOperacionesTermino.obtenerOperacionesEliminar");
		List<Long> idOperaciones = operaciones
				.stream()
				.map(operacion->operacion.IdCargaMasivaDetalle)
				.collect(Collectors.toList());
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdCargaMasivaDetalle", idOperaciones);
		
		SQL sql = new SQL();
		List<OperacionTermino> operacionesEncontradas = sql.query(con, SQL_SELECT_OPERACION_DETALLE, parametros, OperacionTermino.class);
		
		List<OperacionTermino> operacionesEliminar = new ArrayList<OperacionTermino>();
		operacionesEliminar.addAll(operacionesEncontradas);
		operacionesEliminar.addAll(operaciones);
		
		logger.trace("FIN::EliminarRegistroCargaMasivaOperacionesTermino.obtenerOperacionesEliminar");
		return operacionesEliminar;
	}
	
	private void eliminarOperaciones(Connection con, List<OperacionTermino> operaciones) throws Exception{
		logger.trace("INICIO::EliminarRegistroCargaMasivaOperacionesTermino.eliminarOperaciones");
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_OPERACION, operaciones, OperacionTermino.class);
		logger.trace("FIN::EliminarRegistroCargaMasivaOperacionesTermino.eliminarOperaciones");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		Gson gson = Utils.getGsonBuilder();			
		
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"MRF\",\"p_UsaReglasNegocio\":\"S\",\"p_SimularOperacion\":\"N\",\"p_Operaciones\":[{\"IdCargaMasivaDetalle\":55}]}";
		EntradaEliminarRegistroCargaMasivaOperacionesTermino Objin = gson.fromJson(json, EntradaEliminarRegistroCargaMasivaOperacionesTermino.class);
		EliminarRegistroCargaMasivaOperacionesTermino demo = new EliminarRegistroCargaMasivaOperacionesTermino();				
		try {
			SalidaEliminarRegistroCargaMasivaOperacionesTermino ObjOut = demo.logica(con, Objin);
			con.rollback();
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
