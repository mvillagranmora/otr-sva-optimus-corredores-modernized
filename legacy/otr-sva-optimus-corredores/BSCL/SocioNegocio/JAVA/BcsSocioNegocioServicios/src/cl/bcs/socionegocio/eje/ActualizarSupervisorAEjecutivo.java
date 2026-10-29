package cl.bcs.socionegocio.eje;

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

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarSupervisorAEjecutivo implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ActualizarSupervisorAEjecutivo.class);
	
	private String SQL_UPDATE_SUPERVISOR_A_EJECUTIVO = loadResourceToString("cl/bcs/socionegocio/eje/sql/ConsultaEjecutivos_UpdateSupervisorAEjecutivos.sql");
	
	public static class EntradaActualizarSupervisorAEjecutivo extends BcsPojo {

		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public EmpleadoEjecutivo p_EmpleadoEjecutivos;
		
		public EntradaActualizarSupervisorAEjecutivo() {
			
			p_EmpleadoEjecutivos = new EmpleadoEjecutivo();
		}
		
		public String toString() {
			
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class EmpleadoEjecutivo{
		
		public Long p_IdNuevoSupervisor;
		public List<EmpleadoEjecutivosActualizar> p_listadoEjecutivos;
		
		
		public EmpleadoEjecutivo() {
				
			p_IdNuevoSupervisor = 0L;
			p_listadoEjecutivos = new ArrayList < EmpleadoEjecutivosActualizar>();
		}
		
		
		public String toString() {
			
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class EmpleadoEjecutivosActualizar{
		
		public Long IdEmpleadoEjecutivo;
		public Long IdEmpleadoSupervisor;
		public String NombreCompleto;
		public String IdEstado;
		
		public EmpleadoEjecutivosActualizar(){
			
			IdEmpleadoEjecutivo = 0L;
			IdEmpleadoSupervisor = 0L;
			NombreCompleto = "";
			IdEstado = "";
		}
		
		public String toString() {
			
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public static class SalidactualizarSupervisorAEjecutivo extends BcsPojo {

		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public SalidactualizarSupervisorAEjecutivo() {
			
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	public SalidactualizarSupervisorAEjecutivo logica(Connection conexion, EntradaActualizarSupervisorAEjecutivo entrada ) throws Exception  {
		
		logger.info("INICIO::ActualizarSupervisorAEjecutivo");
		
		SalidactualizarSupervisorAEjecutivo salida = new SalidactualizarSupervisorAEjecutivo();

		SQL sql = new SQL();
		
		for(EmpleadoEjecutivosActualizar emple : entrada.p_EmpleadoEjecutivos.p_listadoEjecutivos) {
			
			emple.IdEmpleadoSupervisor = entrada.p_EmpleadoEjecutivos.p_IdNuevoSupervisor;
		}
		
		try {
			
			sql.batch(conexion, SQL_UPDATE_SUPERVISOR_A_EJECUTIVO, entrada.p_EmpleadoEjecutivos.p_listadoEjecutivos , EmpleadoEjecutivosActualizar.class);
			
			MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_ACTUALIZACION_SUPERVISOR_A_EJECUTIVO",
					new HashMap<>(), conexion, entrada.p_IdEmpresa);
			salida.P_OUT_MENSAJES.add(mensajeSalida);
			salida.P_OUT_ERRORESCONTADOR = 0L;

			
		}catch(SQLException e) {
			
			MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_ERROR_ACTUALIZAR",
					new HashMap<>(), conexion, entrada.p_IdEmpresa);
			salida.P_OUT_MENSAJES.add(mensajeSalida);
			salida.P_OUT_ERRORESCONTADOR ++;
			e.printStackTrace();
		}
		
		logger.info("FIN::ActualizarSupervisorAEjecutivo");
		return salida;
	}
	
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {

		Gson gson = Utils.getGsonBuilder();
		EntradaActualizarSupervisorAEjecutivo jin = gson.fromJson(in.toString(),
				EntradaActualizarSupervisorAEjecutivo.class);
		SalidactualizarSupervisorAEjecutivo out = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	}
	
}
