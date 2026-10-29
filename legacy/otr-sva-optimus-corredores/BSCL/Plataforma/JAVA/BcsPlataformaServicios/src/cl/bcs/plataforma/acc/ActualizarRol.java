package cl.bcs.plataforma.acc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarRol implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ActualizarRol.class);

	private final String SQL_SELECT_ROL = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarRol_Select_Rol.sql");
	private final String SQL_UPDATE_ROL = loadResourceToString("cl/bcs/plataforma/acc/sql/ActualizarRol_Update_Rol.sql");

	public static class EntradaActualizarRol {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdRol;
		public String p_Nombre;
		public String p_IdEstado;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleLog {
		public String SubRegistro;
		public String CampoRegistro;
		public String ValorAntiguo;
		public String ValorNuevo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class UpdateRol
	{	
		public String Nombre;
		public String IdEstado;
		public Long IdRol;
		public Long IdEmpresa;
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}	
	}
	
	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;

		public ErrorDetalle(String codigo, String descripcion) {
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}

	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

		public AdvertenciaDetalle(String codigo, String requiereAutorizacion, String descripcion) {
			this.Codigo = codigo;
			this.RequiereAutorizacion = requiereAutorizacion;
			this.Descripcion = descripcion;
		}
	}

	public static class SalidaActualizarRol {
		public int P_OUT_UPDATE;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaActualizarRol() {
			this.P_OUT_UPDATE = 0;
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class RolResult {
		public String IdEstado;
	}

	public SalidaActualizarRol logica(Connection conn, EntradaActualizarRol in) throws Exception {
		logger.info("INIT ActualizarRol");
		SalidaActualizarRol salida = new SalidaActualizarRol();
		SQL sql = new SQL();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		
		try {
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdRol", in.p_IdRol);
			params.put("IdEmpresa", in.p_IdEmpresa);

			logger.debug("SELECT estado antiguo");
			List<RolResult> rolAntiguo = sql.query(conn, SQL_SELECT_ROL, params, RolResult.class);
			String v_EstadoAntiguo = (rolAntiguo != null && rolAntiguo.size() > 0) ? rolAntiguo.get(0).IdEstado : "";

			logger.debug("UPDATE rol");
			List<UpdateRol> paramsUpdateRol = new ArrayList<>();
			UpdateRol inUpdateRol = new UpdateRol();
			inUpdateRol.Nombre = in.p_Nombre;
			inUpdateRol.IdEstado = in.p_IdEstado;
			inUpdateRol.IdRol = in.p_IdRol;
			inUpdateRol.IdEmpresa = in.p_IdEmpresa;
			paramsUpdateRol.add(inUpdateRol);
			
			sql.batch(conn, SQL_UPDATE_ROL, paramsUpdateRol, UpdateRol.class);
			salida.P_OUT_UPDATE = 1;

			if (!v_EstadoAntiguo.equals(in.p_IdEstado)) {
				logger.debug("Estado cambio, agregar log");				
								
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdCabeceraLog = 159L;
				log.IdTipoLog = "ACTUALIZACION";
				log.IdModulo = "PLA";
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
				log.IdUsuarioAtendido = in.p_IdUsuario;
				log.IdTipoRegistro = "ROL";
				entradaAgregarLog.p_Log.add(log);
				
				
				AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
				detLog.IdCabeceraLog = 159L;
				detLog.IdTrabajo= 0L;
				detLog.SubRegistro = null;
				detLog.CampoRegistro = "CambioEstado";
				detLog.ValorAntiguo = v_EstadoAntiguo;
				detLog.ValorNuevo = in.p_IdEstado;
				entradaAgregarLog.p_LogDetalle.add(detLog);

				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(conn, entradaAgregarLog);
			}

		} catch (Exception ex) {
			logger.error("Error en ActualizarRol: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_ACTUALIZAR_ROL", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = (int) salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END ActualizarRol");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaActualizarRol in = gson.fromJson(jin.toString(), EntradaActualizarRol.class);
		SalidaActualizarRol out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"PLA\",\"p_IdRol\":46,\"p_Nombre\":\"TESTSP\",\"p_IdEstado\":\"ACT\"}";
			EntradaActualizarRol entrada = gson.fromJson(json, EntradaActualizarRol.class);
			ActualizarRol demo = new ActualizarRol();
			SalidaActualizarRol salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
