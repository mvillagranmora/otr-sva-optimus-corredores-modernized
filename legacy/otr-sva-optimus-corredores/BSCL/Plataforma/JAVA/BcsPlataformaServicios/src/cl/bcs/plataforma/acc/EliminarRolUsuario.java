package cl.bcs.plataforma.acc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.acc.AgregarRolUsuario.EntradaAgregarRolUsuario;
import cl.bcs.plataforma.acc.AgregarRolUsuario.SalidaAgregarRolUsuario;
import cl.bcs.plataforma.aut.CrearEventoAutorizacion;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarRolUsuario implements JavaCall {
	private static final Logger logger = LogManager.getLogger(EliminarRolUsuario.class);

	private final String SQL_COUNT_ROL = loadResourceToString("cl/bcs/plataforma/acc/sql/EliminarRolUsuario_Count_Rol.sql");
	private final String SQL_COUNT_ROL_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/EliminarRolUsuario_Count_RolUsuario.sql");
	private final String SQL_SELECT_REQUIERE_AUTORIZACION = loadResourceToString("cl/bcs/plataforma/acc/sql/EliminarRolUsuario_Select_RequiereAutorizacion.sql");
	private final String SQL_SELECT_ID_ROL_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/EliminarRolUsuario_Select_IdRolUsuario.sql");
	private final String SQL_DELETE_ROL_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/EliminarRolUsuario_Delete.sql");
	private final String SQL_SELECT_DETALLE_LOG = loadResourceToString("cl/bcs/plataforma/acc/sql/EliminarRolUsuario_Select_DetalleLog.sql");

	public static class EntradaEliminarRolUsuario {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdUsuarioRol;
		public Long p_IdRol;
		public Long p_IdEmpresaRol;
		public String p_CodigoUsuario;
		public String p_FechaSistema;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaEliminarRolUsuario {
		public Long P_OUT_IDROLUSUARIO;
		public Integer P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public Integer P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaEliminarRolUsuario() {
			this.P_OUT_IDROLUSUARIO = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		@Override
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

	public static class Contador {
		public Long Contador;
	}

	public static class RequiereAutorizacion {
		public String RequiereAutorizacion;
	}

	public static class IdRolUsuario {
		public Long IdRolUsuario;
	}

	public static class DeleteRolUsuarioParams {
		public Long IdRolUsuario;
	}

	public static class DetalleLogReg {
		public String SubRegistro;
		public String CampoRegistro;
		public String ValorAntiguo;
		public String ValorNuevo;
	}

	public SalidaEliminarRolUsuario logica(Connection conn, EntradaEliminarRolUsuario entrada) throws Exception {
		logger.info("INIT EliminarRolUsuario");
		SalidaEliminarRolUsuario salida = new SalidaEliminarRolUsuario();
		SQL sql = new SQL();

		// Validación 1: Verificar que el rol pertenece a la empresa
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdRol", entrada.p_IdRol);

		logger.debug("Validando que el rol pertenece a la empresa");
		List<Contador> contadores = sql.query(conn, SQL_COUNT_ROL, params, Contador.class);

		if (contadores.isEmpty() || contadores.get(0).Contador == 0) {
			logger.error("ROL_NO_PERTENECE_EMPRESA");
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ROL_NO_PERTENECE_EMPRESA", ""));
			salida.P_OUT_ERRORESCONTADOR++;
		}
		
		// Validación 2: Verificar que el usuario tiene el rol
		params.clear();
		params.put("IdRol", entrada.p_IdRol);
		params.put("IdUsuario", entrada.p_IdUsuarioRol);

		logger.debug("Validando que el usuario tiene el rol");
		contadores = sql.query(conn, SQL_COUNT_ROL_USUARIO, params, Contador.class);

		if (contadores.isEmpty() || contadores.get(0).Contador == 0) {
			logger.error("USUARIO_NO_TIENE_ROL");
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("USUARIO_NO_TIENE_ROL", ""));
			salida.P_OUT_ERRORESCONTADOR++;
		}

		// Si hay errores, retornar
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			return salida;
		}

		// Consultar si requiere autorización
		params.clear();
		params.put("IdEmpresa", entrada.p_IdEmpresa);

		logger.debug("Consultando si requiere autorización");
		List<RequiereAutorizacion> reqAutList = sql.query(conn, SQL_SELECT_REQUIERE_AUTORIZACION, params,
				RequiereAutorizacion.class);

		String requiereAutorizacion = "N";
		if (!reqAutList.isEmpty()) {
			requiereAutorizacion = reqAutList.get(0).RequiereAutorizacion;
		}

		// Obtener IdRolUsuario
		params.clear();
		params.put("IdRol", entrada.p_IdRol);
		params.put("IdUsuario", entrada.p_IdUsuarioRol);

		logger.debug("Obteniendo IdRolUsuario");
		List<IdRolUsuario> idRolUsuarioList = sql.query(conn, SQL_SELECT_ID_ROL_USUARIO, params, IdRolUsuario.class);

		if (!idRolUsuarioList.isEmpty()) {
			salida.P_OUT_IDROLUSUARIO = idRolUsuarioList.get(0).IdRolUsuario;
		}

		// Si requiere autorización, crear evento de autorización
		if (requiereAutorizacion.equals("S")) {
			logger.debug("Requiere autorización, creando evento de autorización");

			// Crear entrada para CrearEventoAutorizacion
			CrearEventoAutorizacion servicioAuth = new CrearEventoAutorizacion();
			JsonObject jsonAuth = new JsonObject();
			jsonAuth.addProperty("p_IdEmpresa", entrada.p_IdEmpresa);
			jsonAuth.addProperty("p_IdUsuario", entrada.p_IdUsuario);
			jsonAuth.addProperty("p_IdModulo", entrada.p_IdModulo);
			jsonAuth.addProperty("p_UsaReglasNegocio", "N");
			jsonAuth.addProperty("p_IdTipoEvento", "DROLUS");
			jsonAuth.addProperty("p_IdModuloSolicita", entrada.p_IdModulo);
			jsonAuth.addProperty("p_IdUsuarioSolicita", entrada.p_IdUsuario);
			jsonAuth.addProperty("p_Fecha", entrada.p_FechaSistema);
			jsonAuth.addProperty("p_IdConcepto", "DROLUS");
			jsonAuth.addProperty("p_NumeroOperacionConcepto", salida.P_OUT_IDROLUSUARIO.toString());
			jsonAuth.addProperty("p_IdCliente", salida.P_OUT_IDROLUSUARIO);
			jsonAuth.addProperty("p_FechaOperacionConcepto", entrada.p_FechaSistema);
			jsonAuth.addProperty("p_Glosa", entrada.p_CodigoUsuario);

			JsonObject respAuth = servicioAuth.call(conn, jsonAuth);

			// Verificar si hubo errores en la autorización
			if (respAuth.has("p_out_ErroresContador") && respAuth.get("p_out_ErroresContador").getAsInt() > 0) {
				// Copiar errores de autorización
				if (respAuth.has("p_out_ErroresDetalle") && respAuth.get("p_out_ErroresDetalle").isJsonArray()) {
					respAuth.getAsJsonArray("p_out_ErroresDetalle").forEach(elem -> {
						JsonObject errorObj = elem.getAsJsonObject();
						salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(
								errorObj.get("Codigo").getAsString(),
								errorObj.get("Descripcion").getAsString()));
						salida.P_OUT_ERRORESCONTADOR++;
					});
				}
				return salida;
			}

			// Copiar advertencias de autorización
			if (respAuth.has("p_out_AdvertenciasDetalle") && respAuth.get("p_out_AdvertenciasDetalle").isJsonArray()) {
				respAuth.getAsJsonArray("p_out_AdvertenciasDetalle").forEach(elem -> {
					JsonObject warnObj = elem.getAsJsonObject();
					salida.P_OUT_ADVERTENCIASDETALLE.add(new AdvertenciaDetalle(
							warnObj.get("Codigo").getAsString(),
							warnObj.has("RequiereAutorizacion") ? warnObj.get("RequiereAutorizacion").getAsString()
									: "",
							warnObj.get("Descripcion").getAsString()));
					salida.P_OUT_ADVERTENCIASCONTADOR++;
				});
			}

			// Agregar mensaje de requiere autorización
			salida.P_OUT_ADVERTENCIASDETALLE.add(
					new AdvertenciaDetalle("REQUIERE_AUTORIZACION", "S", ""));
			salida.P_OUT_ADVERTENCIASCONTADOR++;

		} else {
			// No requiere autorización, eliminar el rol
			logger.debug("No requiere autorización, eliminando el rol");

			// 1. Primero eliminamos la asignación del rol al usuario
			DeleteRolUsuarioParams deleteParams = new DeleteRolUsuarioParams();
			deleteParams.IdRolUsuario = salida.P_OUT_IDROLUSUARIO;
			sql.batch(conn, SQL_DELETE_ROL_USUARIO, Arrays.asList(deleteParams), DeleteRolUsuarioParams.class);

			// 2. Consultamos el detalle del log (usa IdRol para consultar Rol y Perfiles, no el RolUsuario eliminado)
			params.clear();
			params.put("IdRol", entrada.p_IdRol);
			logger.debug("Consultando detalle log");
			List<DetalleLogReg> detalleLog = sql.query(conn, SQL_SELECT_DETALLE_LOG, params, DetalleLogReg.class);

			// 3. Registramos el log de la eliminación
			AgregarLog servicioLog = new AgregarLog();
			InAgregarLog entradaAgregarLog = new InAgregarLog();
			AtomicLong contador = new AtomicLong();
			entradaAgregarLog.agregarLogBasico(entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_IdUsuarioRol, contador.get(), "ELIMINACION", "ROL", entrada.p_FechaSistema, "EliminarRolUsuario.java");
			
			for (DetalleLogReg detalle : detalleLog) {
				entradaAgregarLog.agregarDetalleLogBasico(entrada.p_IdUsuarioRol, contador.getAndIncrement(), detalle.CampoRegistro, detalle.ValorAntiguo, detalle.ValorNuevo, detalle.SubRegistro);
			}

			servicioLog.logica(conn, entradaAgregarLog);
		}

		logger.info("END EliminarRolUsuario");
		return salida;
	}


	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaEliminarRolUsuario in = gson.fromJson(jin.toString(), EntradaEliminarRolUsuario.class);
		SalidaEliminarRolUsuario out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		Connection con = PlataformaUtils.getPropertiesConnection("desa01","connections.properties");
		
		String json = "{\r\n"
				+ "    \"p_IdEmpresa\": 1,\r\n"
				+ "    \"p_IdUsuario\": 1,\r\n"
				+ "    \"p_IdModulo\": \"PLA\",\r\n"
				+ "    \"p_IdUsuarioRol\": 85,\r\n"
				+ "    \"p_IdRol\": 4,\r\n"
				+ "    \"p_IdEmpresaRol\": 1,\r\n"
				+ "    \"p_CodigoUsuario\": \"9704277-2\",\r\n"
				+ "    \"p_FechaSistema\": \"2026-02-11\"\r\n"
				+ "}";

		Gson gson = new Gson();	
		EntradaEliminarRolUsuario entrada = gson.fromJson(json, EntradaEliminarRolUsuario.class);

		EliminarRolUsuario servicio = new EliminarRolUsuario();
		System.out.println("ENTRADA");
		System.out.println(entrada);
		
		SalidaEliminarRolUsuario salida = servicio.logica(con, entrada);
		System.out.println(salida);
	}
}
