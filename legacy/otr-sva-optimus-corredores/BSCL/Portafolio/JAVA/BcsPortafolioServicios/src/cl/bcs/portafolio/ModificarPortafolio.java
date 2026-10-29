package cl.bcs.portafolio;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.CrearEventoAutorizacion;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Servicio: ModificarPortafolio (Migración completa)
 * Propósito: Servicio que obtiene y evalúa información del portafolio a modificar,
 *            para ser creado como una solicitud de Cambio
 * Migrado de: BSCL.Portafolio.BD.Servicios::ModificarPortafolio
 * Autor: Equipo Migración
 * Fecha: Abril 2026
 */
public class ModificarPortafolio implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ModificarPortafolio.class);
	
	// Archivos SQL
	private final String SQL_COUNT_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/sql/ModificarPortafolio_Count_Portafolio.sql");
	private final String SQL_SELECT_CLIENTE_CODIGO = loadResourceToString("cl/bcs/portafolio/sql/ModificarPortafolio_Select_ClienteCodigo.sql");
	private final String SQL_COUNT_PLANTILLA_LIMITES_POR_DEFECTO = loadResourceToString("cl/bcs/portafolio/sql/ModificarPortafolio_Count_PlantillaLimitesPorDefecto.sql");
	private final String SQL_COUNT_CLIENTE_PLANTILLA_POR_DEFECTO = loadResourceToString("cl/bcs/portafolio/sql/ModificarPortafolio_Count_ClientePlantillaPorDefecto.sql");
	private final String SQL_SELECT_VALOR_ANTIGUO = loadResourceToString("cl/bcs/portafolio/sql/ModificarPortafolio_Select_ValorAntiguo.sql");
	private final String SQL_SELECT_GLOSA_CLIENTE = loadResourceToString("cl/bcs/portafolio/sql/ModificarPortafolio_Select_GlosaCliente.sql");
	private final String SQL_INSERT_LOG_GENERAR_CONTRATOS = loadResourceToString("cl/bcs/portafolio/sql/ModificarPortafolio_Insert_LogGenerarContratos.sql");
	private final String SQL_SELECT_ID_SOLICITUD_CAMBIO = loadResourceToString("cl/bcs/portafolio/sql/ModificarPortafolio_Select_IdSolicitudCambio.sql");
	
	/**
	 * Clase para entrada del servicio
	 */
	public static class Entrada extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public Long p_IdCliente;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdPortafolio;
		public String p_IdCanal;
		public List<RegistroCambio> p_RegistroCambio;
		
		public Entrada() {
			this.p_RegistroCambio = new ArrayList<>();
		}
	}
	
	/**
	 * Clase para registro de cambio
	 */
	public static class RegistroCambio {
		public String CampoModificado;
		public String ValorNuevo;
		
		public RegistroCambio() {
		}
		
		public RegistroCambio(String campoModificado, String valorNuevo) {
			this.CampoModificado = campoModificado;
			this.ValorNuevo = valorNuevo;
		}
	}
	
	/**
	 * Clase para salida del servicio
	 */
	public static class Salida extends BcsPojo {
		public Long P_OUT_REFID;
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;
		
		public Salida() {
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
	}
	
	/**
	 * Clase auxiliar para errores
	 */
	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;
	}
	
	/**
	 * Clase auxiliar para advertencias
	 */
	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}
	
	/**
	 * Clase para contador
	 */
	public static class Contador {
		public Long Contador;
	}
	
	/**
	 * Clase para ID de Solicitud Cambio
	 */
	public static class IdSolicitudCambioResult {
		public Long IdSolicitudCambio;
	}
	
	/**
	 * Clase para cliente y código portafolio
	 */
	public static class ClienteCodigo {
		public Long IdCliente;
		public String CodigoPortafolio;
	}
	
	/**
	 * Clase para valores antiguos
	 */
	public static class ValorAntiguo {
		public String TipoDato;
		public String NombreColumna;
		public String CampoModificado;
		public String ValorNuevo;
		public String ValorAntiguo;
	}
	
	/**
	 * Clase para glosa cliente
	 */
	public static class GlosaCliente {
		public String Glosa;
	}
	
	/**
	 * Clase para log generar contratos
	 */
	public static class LogGenerarContratos {
		public String CampoModificado;
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdUsuario;
		public String FechaHoraSistema;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		Entrada entrada = gson.fromJson(in.toString(), Entrada.class);
		Salida salida = logica(conn, entrada);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(salida)).getAsJsonObject();
		return dataout;
	}
	
	/**
	 * Lógica principal del servicio
	 */
	public Salida logica(Connection conn, Entrada entrada) throws Exception {
		logger.info("INICIO: ModificarPortafolio");
		Salida salida = new Salida();
		SQL sql = new SQL();
		
		try {
			// Variables
			String v_tipoEntidad = "PORTAFOLIO";
			String v_IdConcepto = "MODPOR";
			String v_IdEntidadOrigen = String.valueOf(entrada.p_IdPortafolio);
			Long v_IdEntidadBase;
			String v_CodigoPortafolio;
			String v_estadoAutorizacion = "PEN";
			Long v_secuencia_IdSolicitudCambio;
			
			// Obtener fecha sistema
			String v_fechaHoraSistema = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa.intValue());
			
			// 1. Validar existencia del portafolio
			logger.debug("Validar existencia del portafolio");
			HashMap<String, Object> paramsCount = new HashMap<>();
			paramsCount.put("IdPortafolio", entrada.p_IdPortafolio);
			
			List<Contador> countList = sql.query(conn, SQL_COUNT_PORTAFOLIO, paramsCount, Contador.class);
			Long v_countEntidad = (countList != null && !countList.isEmpty()) ? countList.get(0).Contador : 0L;
			
			if (v_countEntidad == 0) {
				salida.P_OUT_ERRORESCONTADOR = 1L;
				ErrorDetalle err = new ErrorDetalle();
				err.Codigo = "ENTIDAD_NO_EXISTE";
				err.Descripcion = "El portafolio no existe";
				salida.P_OUT_ERRORESDETALLE.add(err);
				return salida;
			}
			
			// Obtener IdCliente y CodigoPortafolio
			List<ClienteCodigo> clienteCodigos = sql.query(conn, SQL_SELECT_CLIENTE_CODIGO, paramsCount, ClienteCodigo.class);
			v_IdEntidadBase = clienteCodigos.get(0).IdCliente;
			v_CodigoPortafolio = clienteCodigos.get(0).CodigoPortafolio;
			
			// 2. Validar plantilla límites por defecto
			logger.debug("Validar plantilla límites por defecto");
			for (RegistroCambio cambio : entrada.p_RegistroCambio) {
				if ("IdPerfilLimites".equals(cambio.CampoModificado)) {
					HashMap<String, Object> paramsPlantilla = new HashMap<>();
					paramsPlantilla.put("ValorNuevo", cambio.ValorNuevo);
					
					List<Contador> countPlantilla = sql.query(conn, SQL_COUNT_PLANTILLA_LIMITES_POR_DEFECTO, 
						paramsPlantilla, Contador.class);
					Long esPlantillaPorDefecto = (countPlantilla != null && !countPlantilla.isEmpty()) 
						? countPlantilla.get(0).Contador : 0L;
					
					if (esPlantillaPorDefecto > 0) {
						// Verificar si el cliente ya tiene otra plantilla por defecto
						HashMap<String, Object> paramsCliente = new HashMap<>();
						paramsCliente.put("IdCliente", v_IdEntidadBase);
						paramsCliente.put("IdPortafolio", entrada.p_IdPortafolio);
						
						List<Contador> countClientePlantilla = sql.query(conn, SQL_COUNT_CLIENTE_PLANTILLA_POR_DEFECTO,
							paramsCliente, Contador.class);
						Long tieneOtraPlantilla = (countClientePlantilla != null && !countClientePlantilla.isEmpty())
							? countClientePlantilla.get(0).Contador : 0L;
						
						if (tieneOtraPlantilla > 0) {
							salida.P_OUT_ERRORESCONTADOR = 1L;
							ErrorDetalle err = new ErrorDetalle();
							err.Codigo = "CLIENTE_YA_TIENE_PLANTILLA_LIMITE_POR_DEFECTO";
							err.Descripcion = "El cliente ya tiene un portafolio con plantilla límite por defecto";
							salida.P_OUT_ERRORESDETALLE.add(err);
							return salida;
						}
					}
				}
			}
			
			// 3. Obtener secuencia para IdSolicitudCambio
			List<IdSolicitudCambioResult> secuencias = sql.query(conn, SQL_SELECT_ID_SOLICITUD_CAMBIO, 
				new HashMap<>(), IdSolicitudCambioResult.class);
			v_secuencia_IdSolicitudCambio = (secuencias != null && !secuencias.isEmpty()) 
				? secuencias.get(0).IdSolicitudCambio : null;
			
			if (v_secuencia_IdSolicitudCambio == null) {
				salida.P_OUT_ERRORESCONTADOR = 1L;
				ErrorDetalle err = new ErrorDetalle();
				err.Codigo = "ERROR_SECUENCIA";
				err.Descripcion = "No se pudo obtener la secuencia IdSolicitudCambio";
				salida.P_OUT_ERRORESDETALLE.add(err);
				return salida;
			}
			
			logger.debug("IdSolicitudCambio: " + v_secuencia_IdSolicitudCambio);
			
			// 4. Construir registros de solicitud de cambio
			logger.debug("Construir registros de solicitud de cambio");
			List<CrearRegistroSolicitudCambio.RegistroSolicitudCambio> registrosSolicitud = new ArrayList<>();
			
			for (RegistroCambio cambio : entrada.p_RegistroCambio) {
				// Obtener valor antiguo
				HashMap<String, Object> paramsValor = new HashMap<>();
				paramsValor.put("IdPortafolio", entrada.p_IdPortafolio);
				paramsValor.put("CampoModificado", cambio.CampoModificado);
				paramsValor.put("ValorNuevo", cambio.ValorNuevo);
				
				List<ValorAntiguo> valoresAntiguos = sql.query(conn, SQL_SELECT_VALOR_ANTIGUO, 
					paramsValor, ValorAntiguo.class);
				
				if (valoresAntiguos != null && !valoresAntiguos.isEmpty()) {
					ValorAntiguo valorAntiguo = valoresAntiguos.get(0);
					
					CrearRegistroSolicitudCambio.RegistroSolicitudCambio registro = 
						new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
					registro.IdSolicitudCambio = v_secuencia_IdSolicitudCambio;
					registro.IdTipoEntidad = v_tipoEntidad;
					registro.IdEntidadOrigen = v_IdEntidadOrigen;
					registro.CampoModificado = cambio.CampoModificado;
					registro.TipoDato = valorAntiguo.TipoDato;
					registro.ValorAntiguo = valorAntiguo.ValorAntiguo;
					registro.ValorNuevo = cambio.ValorNuevo;
					registro.IdEstadoAutorizacion = v_estadoAutorizacion;
					
					registrosSolicitud.add(registro);
				}
			}
			
			// 5. Crear registros de solicitud de cambio
			logger.debug("Crear registros de solicitud de cambio");
			CrearRegistroSolicitudCambio crearSolicitud = new CrearRegistroSolicitudCambio();
			CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio entradaCrear = 
				new CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio();
			entradaCrear.p_RegistroSolicitudCambio = registrosSolicitud;
			
			CrearRegistroSolicitudCambio.SalidaCrearRegistroSolicitudCambio salidaCrear = 
				crearSolicitud.logica(conn, entradaCrear);
			
			// 6. Verificar si requiere autorizacion
			logger.debug("Verificar si requiere autorizacion");
			String v_RequiereAutorizacion = Utils.getRequiereEventoAutorizacion(conn, entrada.p_IdEmpresa, v_IdConcepto);
			
			if ("S".equals(v_RequiereAutorizacion)) {
				// 7. Crear evento de autorizacion
				logger.debug("Crear evento de autorizacion");
				
				// Obtener glosa del cliente
				HashMap<String, Object> paramsGlosa = new HashMap<>();
				paramsGlosa.put("IdCliente", v_IdEntidadBase);
				paramsGlosa.put("CodigoPortafolio", v_CodigoPortafolio);
				
				List<GlosaCliente> glosas = sql.query(conn, SQL_SELECT_GLOSA_CLIENTE, paramsGlosa, GlosaCliente.class);
				String v_Glosa = (glosas != null && !glosas.isEmpty()) ? glosas.get(0).Glosa : "";
				
				CrearEventoAutorizacion crearEvento = new CrearEventoAutorizacion();
				CrearEventoAutorizacion.ServiceEntrada entradaEvento = 
					new CrearEventoAutorizacion.ServiceEntrada();
				
				entradaEvento.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaEvento.p_IdUsuario = entrada.p_IdUsuario;
				entradaEvento.p_IdModulo = entrada.p_IdModulo;
				entradaEvento.p_UsaReglasNegocio = "N";
				entradaEvento.p_IdTipoEvento = v_IdConcepto;
				entradaEvento.p_IdModuloSolicita = entrada.p_IdModulo;
				entradaEvento.p_IdUsuarioSolicita = entrada.p_IdUsuario;
				entradaEvento.p_Fecha = v_fechaHoraSistema.substring(0, 10);
				entradaEvento.p_IdConcepto = v_IdConcepto;
				entradaEvento.p_NumeroOperacionConcepto = v_IdEntidadOrigen;
				entradaEvento.p_FechaOperacionConcepto = v_fechaHoraSistema.substring(0, 10);
				entradaEvento.p_IdCliente = v_IdEntidadBase;
				entradaEvento.p_Glosa = v_Glosa;
				
				CrearEventoAutorizacion.ServiceSalida salidaEvento = 
					crearEvento.logica(conn, entradaEvento);
				
			// Propagar errores
			if (salidaEvento.p_out_ErroresDetalle != null) {
				for (CrearEventoAutorizacion.ObjERRORESDETALLE err : salidaEvento.p_out_ErroresDetalle) {
					ErrorDetalle error = new ErrorDetalle();
					error.Codigo = err.Codigo;
					error.Descripcion = err.Descripcion;
					salida.P_OUT_ERRORESDETALLE.add(error);
					salida.P_OUT_ERRORESCONTADOR++;
				}
			}
			
			// Propagar advertencias
			if (salidaEvento.p_out_AdvertenciasDetalle != null) {
				for (CrearEventoAutorizacion.ObjADVERTENCIASDETALLE adv : salidaEvento.p_out_AdvertenciasDetalle) {
					AdvertenciaDetalle advertencia = new AdvertenciaDetalle();
					advertencia.Codigo = adv.Codigo;
					advertencia.Descripcion = adv.Descripcion;
					advertencia.RequiereAutorizacion = adv.RequiereAutorizacion;
					salida.P_OUT_ADVERTENCIASDETALLE.add(advertencia);
					salida.P_OUT_ADVERTENCIASCONTADOR++;
				}
			}
			
		// Si hay errores, retornar ahora (sin agregar mensaje de autorizacion)
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			return salida;
		}
		
		// Agregar mensaje de requiere autorizacion (solo si no hay errores)
		AdvertenciaDetalle advAuth = new AdvertenciaDetalle();
		advAuth.Codigo = "REQUIERE_AUTORIZACION";
		advAuth.RequiereAutorizacion = "S";
		advAuth.Descripcion = "La modificacion requiere autorizacion";
		salida.P_OUT_ADVERTENCIASDETALLE.add(advAuth);
		salida.P_OUT_ADVERTENCIASCONTADOR++;
		
	} else {
		// 8. NO requiere autorizacion - confirmar modificacion directamente
		logger.debug("No requiere autorizacion - confirmar modificacion");
		
		// Insertar en LogGenerarContratos
		List<LogGenerarContratos> logsContratos = new ArrayList<>();
		for (RegistroCambio cambio : entrada.p_RegistroCambio) {
			LogGenerarContratos log = new LogGenerarContratos();
			log.CampoModificado = cambio.CampoModificado;
					log.IdEmpresa = entrada.p_IdEmpresa;
					log.IdCliente = v_IdEntidadBase;
					log.IdUsuario = entrada.p_IdUsuario;
					log.FechaHoraSistema = v_fechaHoraSistema;
					logsContratos.add(log);
				}
				
				if (!logsContratos.isEmpty()) {
					sql.batch(conn, SQL_INSERT_LOG_GENERAR_CONTRATOS, logsContratos, LogGenerarContratos.class);
				}
				
				// Llamar al servicio AutorizacionModificarPortafolioMasivo (confirmar modificacion)
				logger.debug("Llamar a AutorizacionModificarPortafolioMasivo.autorizarEvento");
				
				AutorizacionModificarPortafolioMasivo autorizacionServicio = new AutorizacionModificarPortafolioMasivo();
				BcsAutorizaciones.EntradaAutorizacion entradaAutorizacion = new BcsAutorizaciones.EntradaAutorizacion();
				
				entradaAutorizacion.p_IdEmpresa = entrada.p_IdEmpresa;
				entradaAutorizacion.p_IdUsuario = entrada.p_IdUsuario;
				entradaAutorizacion.p_IdModulo = entrada.p_IdModulo;
				
				// Crear evento simulado para autorizacion automatica
				Evento evento = new Evento();
				evento.IdEvento = 0L;
				evento.IdEmpresa = entrada.p_IdEmpresa;
				evento.IdConcepto = v_IdConcepto;
				evento.NumeroOperacionConcepto = v_IdEntidadOrigen;
				entradaAutorizacion.p_Lista.add(evento);
				
				BcsAutorizaciones.SalidaAutorizacion salidaAutorizacion = 
					autorizacionServicio.autorizarEvento(conn, entradaAutorizacion);
				
				// Propagar errores
				salida.P_OUT_ERRORESCONTADOR += salidaAutorizacion.P_OUT_ERRORESCONTADOR;
				
				// Convertir mensajes a errores/advertencias
				for (MensajeSalida msg : salidaAutorizacion.P_OUT_MENSAJES) {
					if (msg.tipo == MensajeSalida.MENSAJE_ERROR) {
						ErrorDetalle err = new ErrorDetalle();
						err.Codigo = msg.codigo;
						err.Descripcion = msg.descripcion;
						salida.P_OUT_ERRORESDETALLE.add(err);
					} else if (msg.tipo == MensajeSalida.MENSAJE_ADVERTENCIA) {
						AdvertenciaDetalle adv = new AdvertenciaDetalle();
						adv.Codigo = msg.codigo;
						adv.Descripcion = msg.descripcion;
						adv.RequiereAutorizacion = "N";
						salida.P_OUT_ADVERTENCIASDETALLE.add(adv);
						salida.P_OUT_ADVERTENCIASCONTADOR++;
					}
				}
				
				if (salida.P_OUT_ERRORESCONTADOR > 0) {
					return salida;
				}
			}
			
			// Establecer RefId
			salida.P_OUT_REFID = v_secuencia_IdSolicitudCambio;
			
		} catch (Exception ex) {
			logger.error("Error en ModificarPortafolio: " + ex.getMessage(), ex);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			ErrorDetalle err = new ErrorDetalle();
			err.Codigo = "ERROR_MODIFICAR_PORTAFOLIO";
			err.Descripcion = "Error al modificar portafolio: " + ex.getMessage();
			salida.P_OUT_ERRORESDETALLE.add(err);
		}
		
		logger.info("TERMINO: ModificarPortafolio");
		return salida;
	}
	
	/**
	 * Método main para pruebas
	 */
	public static void main(String[] args) throws SQLException {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			
			ModificarPortafolio servicio = new ModificarPortafolio();
			
			// JSON de entrada para pruebas
			String jsonEntrada = "{\r\n"
					+ "    \"p_IdEmpresa\": 1,\r\n"
					+ "    \"p_IdUsuario\": 76767,\r\n"
					+ "    \"p_IdModulo\": \"SOC\",\r\n"
					+ "    \"p_UsaReglasNegocio\": \"N\",\r\n"
					+ "    \"p_IdPortafolio\": 84725,\r\n"
					+ "    \"p_RegistroCambio\": [\r\n"
					+ "        {\r\n"
					+ "            \"CampoModificado\": \"Alias\",\r\n"
					+ "            \"ValorNuevo\": \"\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"CampoModificado\": \"Proposito\",\r\n"
					+ "            \"ValorNuevo\": \"PROPOSITOAPV\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"CampoModificado\": \"DescripcionPortafolio\",\r\n"
					+ "            \"ValorNuevo\": \"\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"CampoModificado\": \"IdDireccionEnvioCorrespondencia\",\r\n"
					+ "            \"ValorNuevo\": \"0\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"CampoModificado\": \"IdTelefonoContacto\",\r\n"
					+ "            \"ValorNuevo\": 1637268\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"CampoModificado\": \"IdEmpleadoEjecutivo\",\r\n"
					+ "            \"ValorNuevo\": 314\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"CampoModificado\": \"IdEmpleadoEjecutivoComercial\",\r\n"
					+ "            \"ValorNuevo\": \"0\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"CampoModificado\": \"NumeroFicha\",\r\n"
					+ "            \"ValorNuevo\": \"0\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"CampoModificado\": \"IdNombreFantasia\",\r\n"
					+ "            \"ValorNuevo\": \"0\"\r\n"
					+ "        },\r\n"
					+ "        {\r\n"
					+ "            \"CampoModificado\": \"AreaCliente\",\r\n"
					+ "            \"ValorNuevo\": \"\"\r\n"
					+ "        }\r\n"
					+ "    ]\r\n"
					+ "}";
			
			// Parsear JSON a objeto Entrada
			Gson gson = Utils.getGsonBuilder();
			JsonParser parser = new JsonParser();
			JsonObject jsonObject = parser.parse(jsonEntrada).getAsJsonObject();
			Entrada entrada = gson.fromJson(jsonObject, Entrada.class);
			
			System.out.println("Entrada parseada:");
			System.out.println("  IdPortafolio: " + entrada.p_IdPortafolio);
			System.out.println("  RegistrosCambio: " + entrada.p_RegistroCambio.size());
			
			Salida salida = servicio.logica(conn, entrada);
			
			System.out.println("\n=== RESULTADO ===");
		System.out.println("RefId: " + salida.P_OUT_REFID);
		System.out.println("Errores: " + salida.P_OUT_ERRORESCONTADOR);
		System.out.println("Advertencias: " + salida.P_OUT_ADVERTENCIASCONTADOR);
		
		// Mostrar errores
		if (!salida.P_OUT_ERRORESDETALLE.isEmpty()) {
			System.out.println("\n=== ERRORES ===");
			for (ErrorDetalle err : salida.P_OUT_ERRORESDETALLE) {
				System.out.println("  [ERROR] " + err.Codigo + ": " + err.Descripcion);
			}
		}
		
		// Mostrar advertencias/autorizaciones
		if (!salida.P_OUT_ADVERTENCIASDETALLE.isEmpty()) {
			System.out.println("\n=== ADVERTENCIAS/AUTORIZACIONES ===");
			for (AdvertenciaDetalle adv : salida.P_OUT_ADVERTENCIASDETALLE) {
				String tipo = "S".equals(adv.RequiereAutorizacion) ? "AUTORIZACION" : "ADVERTENCIA";
				System.out.println("  [" + tipo + "] " + adv.Codigo + ": " + adv.Descripcion);
			}
		}
			
			conn.rollback();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
