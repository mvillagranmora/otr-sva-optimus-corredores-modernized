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

public class AsignarPerfilRol implements JavaCall {
	private static final Logger logger = LogManager.getLogger(AsignarPerfilRol.class);

	private final String SQL_SELECT_COUNT = loadResourceToString("cl/bcs/plataforma/acc/sql/AsignarPerfilRol_Select_Count.sql");
	private final String SQL_INSERT_ROLJPERFIL = loadResourceToString("cl/bcs/plataforma/acc/sql/AsignarPerfilRol_Insert_RolPerfil.sql");

	public static class EntradaAsignarPerfilRol {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdPerfil;
		public Long p_IdRol;
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
	};
	
	public static class InsertRolPerfil
	{
		public Long IdRolPerfil;
		public Long IdRol;
		public Long IdPerfil;
		public String IdEstado;
		
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

	public static class SalidaAsignarPerfilRol {
		public Long P_IDROLFPERFIL;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaAsignarPerfilRol() {
			this.P_IDROLFPERFIL = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
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

	public SalidaAsignarPerfilRol logica(Connection conn, EntradaAsignarPerfilRol in) throws Exception {
		logger.info("INIT AsignarPerfilRol");
		SalidaAsignarPerfilRol salida = new SalidaAsignarPerfilRol();
		SQL sql = new SQL();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

		try {
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdRol", in.p_IdRol);
			params.put("IdPerfil", in.p_IdPerfil);

			logger.debug("Validar si perfil ya tiene rol");
			List<Contador> countRolPerfil = sql.query(conn, SQL_SELECT_COUNT, params, Contador.class);
			Long v_countRolesPerfil = (countRolPerfil != null && countRolPerfil.size() > 0) ? countRolPerfil.get(0).Contador : 0L;

			if (v_countRolesPerfil > 0) {
				logger.warn("Rol ya tiene perfil");
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ROL_YA_TIENE_PERFIL", "El rol ya tiene asignado este perfil"));
				salida.P_OUT_ERRORESCONTADOR = (long) salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			logger.debug("Generar ID RolPerfil");
			List<Long> v_IdRolPerfil = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::RolPerfil", 1L);
			salida.P_IDROLFPERFIL = v_IdRolPerfil.get(0);

			logger.debug("INSERT RolPerfil");
			
			List<InsertRolPerfil> paramsInsertRolPerfil = new ArrayList<>();
			InsertRolPerfil inRolPerfil = new InsertRolPerfil();
			inRolPerfil.IdRolPerfil = v_IdRolPerfil.get(0);
			inRolPerfil.IdRol = in.p_IdRol;
			inRolPerfil.IdPerfil = in.p_IdPerfil;
			inRolPerfil.IdEstado = in.p_IdEstado;
			paramsInsertRolPerfil.add(inRolPerfil);
			
			sql.batch(conn, SQL_INSERT_ROLJPERFIL, paramsInsertRolPerfil, InsertRolPerfil.class);
			

			logger.debug("Agregar log de ingreso");
			
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = 159L;
			log.IdTipoLog = "INGRESO";
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
			detLog.ValorAntiguo = "";
			detLog.ValorNuevo = in.p_IdEstado;
			entradaAgregarLog.p_LogDetalle.add(detLog);

			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);

		} catch (Exception ex) {
			logger.error("Error en AsignarPerfilRol: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_ASIGNAR_PERFIL_ROL", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = (long) salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END AsignarPerfilRol");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaAsignarPerfilRol in = gson.fromJson(jin.toString(), EntradaAsignarPerfilRol.class);
		SalidaAsignarPerfilRol out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"PLA\",\"p_IdPerfil\":16,\"p_IdRol\":46,\"p_IdEstado\":\"ACT\"}";
			EntradaAsignarPerfilRol entrada = gson.fromJson(json, EntradaAsignarPerfilRol.class);
			AsignarPerfilRol demo = new AsignarPerfilRol();
			SalidaAsignarPerfilRol salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
