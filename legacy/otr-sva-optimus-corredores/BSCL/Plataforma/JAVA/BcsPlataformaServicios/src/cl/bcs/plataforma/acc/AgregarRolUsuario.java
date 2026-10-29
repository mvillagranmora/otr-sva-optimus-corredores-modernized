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

import cl.bcs.plataforma.aut.CrearEventoAutorizacion;
import cl.bcs.plataforma.aut.CrearEventoAutorizacion.ServiceEntrada;
import cl.bcs.plataforma.aut.CrearEventoAutorizacion.ServiceSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AgregarRolUsuario implements JavaCall {
	private static final Logger logger = LogManager.getLogger(AgregarRolUsuario.class);

	private final String SQL_COUNT_ROL = loadResourceToString("cl/bcs/plataforma/acc/sql/AgregarRolUsuario_Count_Rol.sql");
	private final String SQL_COUNT_ROL_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/AgregarRolUsuario_Count_RolUsuario.sql");
	private final String SQL_SELECT_REQUIERE_AUTORIZACION = loadResourceToString("cl/bcs/plataforma/acc/sql/AgregarRolUsuario_Select_RequiereAutorizacion.sql");
	private final String SQL_SELECT_NOMBRE_ROL = loadResourceToString("cl/bcs/plataforma/acc/sql/AgregarRolUsuario_Select_NombreRol.sql");
	private final String SQL_INSERT_ROL_USUARIO = loadResourceToString("cl/bcs/plataforma/acc/sql/AgregarRolUsuario_Insert.sql");
	private final String SQL_SELECT_DETALLE_LOG = loadResourceToString("cl/bcs/plataforma/acc/sql/AgregarRolUsuario_Select_DetalleLog.sql");

	public static class EntradaAgregarRolUsuario {
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

	public static class SalidaAgregarRolUsuario {
		public Long P_OUT_IDROLUSUARIO;
		public Integer P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public Integer P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaAgregarRolUsuario() {
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

	public static class NombreRol {
		public String Nombre;
	}

	public static class DetalleLogReg {
		public String SubRegistro;
		public String CampoRegistro;
		public String ValorAntiguo;
		public String ValorNuevo;
	}
	
	public static class InsertRol{
		public Long IdRolUsuario;
		public Long IdRol;
		public Long IdUsuario;
		public String EsAutorizado;
	}

	public SalidaAgregarRolUsuario logica(Connection conn, EntradaAgregarRolUsuario entrada) throws Exception {
		logger.info("INIT AgregarRolUsuario");
		SalidaAgregarRolUsuario salida = new SalidaAgregarRolUsuario();
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

	// Validación 2: Verificar que el usuario no tiene el mismo rol
	params.clear();
	params.put("IdRol", entrada.p_IdRol);
	params.put("IdUsuario", entrada.p_IdUsuarioRol);
	
	logger.debug("Validando que el usuario no tiene el mismo rol");
	contadores = sql.query(conn, SQL_COUNT_ROL_USUARIO, params, Contador.class);
	
	if (!contadores.isEmpty() && contadores.get(0).Contador > 0) {
		logger.error("USUARIO_YA_TIENE_ROL");
		salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("USUARIO_YA_TIENE_ROL", ""));
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

	String esEnrolado = requiereAutorizacion.equals("S") ? "P" : "S";

	// Obtener ID de secuencia e insertar
	try {
		List<Long> nuevoIdRolUsuario = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::RolUsuario", 1L);
		salida.P_OUT_IDROLUSUARIO = nuevoIdRolUsuario.get(0);

		// Preparar parámetros para insert
		InsertRol insertRol = new InsertRol();
		insertRol.IdRolUsuario = nuevoIdRolUsuario.get(0);
		insertRol.IdRol = entrada.p_IdRol;
		insertRol.IdUsuario = entrada.p_IdUsuarioRol;
		insertRol.EsAutorizado =  esEnrolado;

		logger.debug("Insertando RolUsuario");
		sql.batch(conn, SQL_INSERT_ROL_USUARIO, Arrays.asList(insertRol), InsertRol.class);

		// Obtener nombre del rol
		params.clear();
		params.put("IdRol", entrada.p_IdRol);
		logger.debug("Consultando nombre del rol");
		List<NombreRol> nombresRol = sql.query(conn, SQL_SELECT_NOMBRE_ROL, params, NombreRol.class);
		String nombreRol = (!nombresRol.isEmpty()) ? nombresRol.get(0).Nombre : "";

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
			jsonAuth.addProperty("p_IdTipoEvento", "AROLUS");
			jsonAuth.addProperty("p_IdModuloSolicita", entrada.p_IdModulo);
			jsonAuth.addProperty("p_IdUsuarioSolicita", entrada.p_IdUsuario);
			jsonAuth.addProperty("p_Fecha", entrada.p_FechaSistema);
			jsonAuth.addProperty("p_IdConcepto", "AROLUS");
			jsonAuth.addProperty("p_NumeroOperacionConcepto", nuevoIdRolUsuario.get(0).toString());
			jsonAuth.addProperty("p_IdCliente", nuevoIdRolUsuario.get(0));
			jsonAuth.addProperty("p_FechaOperacionConcepto", entrada.p_FechaSistema);
			jsonAuth.addProperty("p_Glosa", entrada.p_CodigoUsuario + " // " + nombreRol);
							
			JsonObject respAuth = servicioAuth.call(conn, jsonAuth);
			
			// Verificar si hubo errores en la autorización
			if (respAuth.has("p_out_ErrorCounter") && respAuth.get("p_out_ErrorCounter").getAsInt() > 0) {
				// Copiar errores de autorización
				if (respAuth.has("p_out_ErrorDetail")) {
					JsonObject errorDetail = respAuth.getAsJsonObject("p_out_ErrorDetail");
					errorDetail.entrySet().forEach(entry -> {
						salida.P_OUT_ERRORESDETALLE.add(
							new ErrorDetalle(entry.getKey(), entry.getValue().getAsString())
						);
						salida.P_OUT_ERRORESCONTADOR++;
					});
				}
				return salida;
			}

			// Copiar advertencias de autorización
			if (respAuth.has("p_out_WarnDetail")) {
				JsonObject warnDetail = respAuth.getAsJsonObject("p_out_WarnDetail");
				warnDetail.entrySet().forEach(entry -> {
					salida.P_OUT_ADVERTENCIASDETALLE.add(
						new AdvertenciaDetalle(entry.getKey(), "S", entry.getValue().getAsString())
					);
					salida.P_OUT_ADVERTENCIASCONTADOR++;
				});
			}

			// Agregar mensaje de requiere autorización
			salida.P_OUT_ADVERTENCIASDETALLE.add(
				new AdvertenciaDetalle("REQUIERE_AUTORIZACION", "S", "")
			);
			salida.P_OUT_ADVERTENCIASCONTADOR++;

		} else {
			// No requiere autorización, agregar log
			logger.debug("No requiere autorización, agregando log");
			
			params.clear();
			params.put("IdRol", entrada.p_IdRol);
			logger.debug("Consultando detalle log");
			List<DetalleLogReg> detalleLog = sql.query(conn, SQL_SELECT_DETALLE_LOG, params, DetalleLogReg.class);

			// Llamar a AgregarLog
			AgregarLog servicioLog = new AgregarLog();
			//Construir entradas log
			InAgregarLog entradaAgregarLog = new InAgregarLog();
			AtomicLong contador = new AtomicLong();
			entradaAgregarLog.agregarLogBasico(entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entrada.p_IdUsuario, contador.get(), "INFORMATIVO", "AGREGAR ROL USUARIO", entrada.p_FechaSistema, "AgregarRolUsuario.java");
			entradaAgregarLog.agregarDetalleLogBasico(entrada.p_IdUsuario, contador.getAndIncrement(), detalleLog.get(0).CampoRegistro, "", detalleLog.get(0).ValorNuevo);

			servicioLog.logica(conn, entradaAgregarLog);
		}

	} catch (Exception ex) {
		logger.error("Error al agregar rol usuario", ex);
		salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_AGREGAR_ROL", ex.getMessage()));
		salida.P_OUT_ERRORESCONTADOR++;
	}

	return salida;
}
	

	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaAgregarRolUsuario in = gson.fromJson(jin.toString(), EntradaAgregarRolUsuario.class);
		SalidaAgregarRolUsuario out = logica(con, in);
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
				+ "    \"p_IdUsuarioRol\": 32,\r\n"
				+ "    \"p_IdRol\": 4,\r\n"
				+ "    \"p_IdEmpresaRol\": 1,\r\n"
				+ "    \"p_CodigoUsuario\": \"10610931-1\",\r\n"
				+ "    \"p_FechaSistema\": \"2026-02-11\"\r\n"
				+ "}";

		Gson gson = new Gson();	
		EntradaAgregarRolUsuario entrada = gson.fromJson(json, EntradaAgregarRolUsuario.class);

		AgregarRolUsuario servicio = new AgregarRolUsuario();
		System.out.println("ENTRADA");
		System.out.println(entrada);
		
		SalidaAgregarRolUsuario salida = servicio.logica(con, entrada);
		System.out.println(salida);
	}
}
