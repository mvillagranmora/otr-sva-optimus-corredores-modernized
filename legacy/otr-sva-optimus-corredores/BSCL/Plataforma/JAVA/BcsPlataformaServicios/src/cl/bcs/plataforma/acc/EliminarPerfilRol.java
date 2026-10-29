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

public class EliminarPerfilRol implements JavaCall {
	private static final Logger logger = LogManager.getLogger(EliminarPerfilRol.class);

	private final String SQL_SELECT_COUNT = loadResourceToString("cl/bcs/plataforma/acc/sql/EliminarPerfilRol_Select_Count.sql");
	private final String SQL_DELETE_ROLJPERFIL = loadResourceToString("cl/bcs/plataforma/acc/sql/EliminarPerfilRol_Delete_RolPerfil.sql");

	public static class EntradaEliminarPerfilRol {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdRolPerfil;

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
	};
	
	public static class DelRolPerfil
	{
		public Long IdRolPerfil;
		
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

	public static class SalidaEliminarPerfilRol {
		public Long P_FLAGROLPERFIL;
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaEliminarPerfilRol() {
			this.P_FLAGROLPERFIL = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Contador {
		public Long Contador;
	}

	public static class FechaResult {
		public String FechaActual;
	}

	public SalidaEliminarPerfilRol logica(Connection conn, EntradaEliminarPerfilRol in) throws Exception {
		logger.info("INIT EliminarPerfilRol");
		SalidaEliminarPerfilRol salida = new SalidaEliminarPerfilRol();
		SQL sql = new SQL();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

		try {
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdRolPerfil", in.p_IdRolPerfil);

			logger.debug("Validar existencia de RolPerfil");
			List<Contador> countRolPerfil = sql.query(conn, SQL_SELECT_COUNT, params, Contador.class);
			Long v_countRolesPerfil = (countRolPerfil != null && countRolPerfil.size() > 0) ? countRolPerfil.get(0).Contador : 0L;

			if (v_countRolesPerfil < 1) {
				logger.warn("RolPerfil no existe");
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ROL_NO_TIENE_PERFIL", "El rol perfil no existe"));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}


			logger.debug("Agregar log de eliminacion");
			
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdTipoLog = "ELIMINACION";
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
			detLog.CampoRegistro = "Perfil";
			detLog.ValorAntiguo = "Rol";
			detLog.ValorNuevo = "";
			entradaAgregarLog.p_LogDetalle.add(detLog);

			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);

			logger.debug("DELETE RolPerfil");
			
			List<DelRolPerfil> paramsDelRolPerfil = new ArrayList<>();
			DelRolPerfil inRolPerfil = new DelRolPerfil();
			inRolPerfil.IdRolPerfil = in.p_IdRolPerfil;
			paramsDelRolPerfil.add(inRolPerfil);
			
			sql.batch(conn, SQL_DELETE_ROLJPERFIL, paramsDelRolPerfil, DelRolPerfil.class);

			salida.P_FLAGROLPERFIL = 1L;

		} catch (Exception ex) {
			logger.error("Error en EliminarPerfilRol: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_ELIMINAR_PERFIL_ROL", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END EliminarPerfilRol");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaEliminarPerfilRol in = gson.fromJson(jin.toString(), EntradaEliminarPerfilRol.class);
		SalidaEliminarPerfilRol out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"PLA\",\"p_IdRolPerfil\":196}";
			EntradaEliminarPerfilRol entrada = gson.fromJson(json, EntradaEliminarPerfilRol.class);
			EliminarPerfilRol demo = new EliminarPerfilRol();
			SalidaEliminarPerfilRol salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
