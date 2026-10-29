package cl.bcs.socionegocio.eje;

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
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Migrado de: EJE.CrearSupervisor
 * Agrega un supervisor de ejecutivos, validando que el empleado no exista ya como tal.
 */
public class CrearSupervisor implements JavaCall {

	private static final Logger logger = LogManager.getLogger(CrearSupervisor.class);

	private final String SQL_SELECT_COUNT = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/eje/sql/CrearSupervisor_Select_Count.sql");
	private final String SQL_INSERT_SUPERVISOR = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/eje/sql/CrearSupervisor_Insert_Supervisor.sql");

	public static class EntradaCrearSupervisor {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdEmpleadoSupervisor;
		public String p_IdEstado;

		public String toString() {
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

	public static class SalidaCrearSupervisor {
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaCrearSupervisor() {
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

	public static class InsertSupervisor {
		public Long IdEmpleadoSupervisor;
		public String FechaCreacion;
		public Long IdUsuarioCreacion;
		public String IdEstado;
	}

	public SalidaCrearSupervisor logica(Connection conn, EntradaCrearSupervisor in) throws Exception {
		logger.info("INIT CrearSupervisor");
		SalidaCrearSupervisor salida = new SalidaCrearSupervisor();
		SQL sql = new SQL();

		try {
			// Validar si el empleado ya existe como supervisor
			logger.debug("Validar si el empleado ya existe como supervisor");
			HashMap<String, Object> paramsExiste = new HashMap<>();
			paramsExiste.put("IdEmpleadoSupervisor", in.p_IdEmpleadoSupervisor);

			List<Contador> countList = sql.query(conn, SQL_SELECT_COUNT, paramsExiste, Contador.class);
			Long v_countSupervisor = (countList != null && !countList.isEmpty()) ? countList.get(0).Contador : 0L;

			if (v_countSupervisor > 0) {
				String mensajeExiste = Diccionario.getMensaje(conn, in.p_IdEmpresa, "SUPERVISOR_EXISTE", new HashMap<String, String>());
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("SUPERVISOR_EXISTE", mensajeExiste));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			// Obtener fecha/hora del sistema
			String v_Fecha = Utils.getFechaHoraSistema(conn, Utils.getSchema(), in.p_IdEmpresa);

			// Insertar supervisor
			logger.debug("INSERT supervisor");
			InsertSupervisor insertSupervisor = new InsertSupervisor();
			insertSupervisor.IdEmpleadoSupervisor = in.p_IdEmpleadoSupervisor;
			insertSupervisor.FechaCreacion = v_Fecha;
			insertSupervisor.IdUsuarioCreacion = in.p_IdUsuario;
			insertSupervisor.IdEstado = in.p_IdEstado;

			List<InsertSupervisor> insertList = new ArrayList<>();
			insertList.add(insertSupervisor);
			sql.batch(conn, SQL_INSERT_SUPERVISOR, insertList, InsertSupervisor.class);

			// Agregar log
			logger.debug("Agregar log de ingreso");
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = 1L;
			log.IdTipoLog = "INGRESO";
			log.IdModulo = "SOC";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = v_Fecha.substring(0, 10);
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdTipoRegistro = "SUPERVISOR";
			log.NombreProceso = "Crear Supervisor";
			log.IdRegistro = String.valueOf(in.p_IdEmpleadoSupervisor);
			entradaAgregarLog.p_Log.add(log);

			AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
			detLog.IdCabeceraLog = 1L;
			detLog.IdTrabajo = 0L;
			detLog.SubRegistro = "";
			detLog.CampoRegistro = "IdEmpleadoSupervisor";
			detLog.ValorAntiguo = "";
			detLog.ValorNuevo = String.valueOf(in.p_IdEmpleadoSupervisor);
			entradaAgregarLog.p_LogDetalle.add(detLog);

			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);

		} catch (Exception ex) {
			logger.error("Error en CrearSupervisor: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CREAR_SUPERVISOR", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END CrearSupervisor");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearSupervisor in = gson.fromJson(jin.toString(), EntradaCrearSupervisor.class);
		SalidaCrearSupervisor out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdEmpleadoSupervisor\":300,\"p_IdEstado\":\"ACT\"}";
			EntradaCrearSupervisor entrada = gson.fromJson(json, EntradaCrearSupervisor.class);
			CrearSupervisor demo = new CrearSupervisor();
			SalidaCrearSupervisor salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
