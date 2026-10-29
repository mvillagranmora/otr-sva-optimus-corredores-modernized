package cl.bcs.portafolio;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.SQL;

/**
 * Servicio: AutorizacionCrearPortafolioMasivo
 * Propósito: Autorizar o Rechazar Creación de Portafolios de forma masiva
 * Migrado de: ConfirmarCrearPortafolioMasivo.SQL + RechazaAutorizacionPortafolioMasivo.SQL
 * Autor: Equipo Migración
 * Fecha: Abril 2026
 */
public class AutorizacionCrearPortafolioMasivo implements BcsAutorizaciones {
	
	private static final Logger logger = LogManager.getLogger(AutorizacionCrearPortafolioMasivo.class);
	
	// Archivos SQL
	private final String SQL_COUNT_PORTAFOLIO_PENDIENTE = PlataformaUtils.loadResourceToString("cl/bcs/portafolio/sql/AutorizacionCrearPortafolioMasivo_Count_PortafolioPendiente.sql");
	private final String SQL_COUNT_CLIENTE_SIN_ENROLAMIENTO = PlataformaUtils.loadResourceToString("cl/bcs/portafolio/sql/AutorizacionCrearPortafolioMasivo_Count_ClienteSinEnrolamiento.sql");
	private final String SQL_SELECT_CLIENTE_MATRIZ_APV = PlataformaUtils.loadResourceToString("cl/bcs/portafolio/sql/AutorizacionCrearPortafolioMasivo_Select_ClienteMatrizAPV.sql");
	private final String SQL_SELECT_PRODUCTO_AGRUPADOR = PlataformaUtils.loadResourceToString("cl/bcs/portafolio/sql/AutorizacionCrearPortafolioMasivo_Select_ProductoAgrupador.sql");
	private final String SQL_SELECT_MATRIZ_AGRUPADOR = PlataformaUtils.loadResourceToString("cl/bcs/portafolio/sql/AutorizacionCrearPortafolioMasivo_Select_MatrizAgrupador.sql");
	private final String SQL_UPDATE_PORTAFOLIO_APV = PlataformaUtils.loadResourceToString("cl/bcs/portafolio/sql/AutorizacionCrearPortafolioMasivo_Update_PortafolioAPV.sql");
	private final String SQL_UPDATE_PORTAFOLIO = PlataformaUtils.loadResourceToString("cl/bcs/portafolio/sql/AutorizacionCrearPortafolioMasivo_Update_Portafolio.sql");
	private final String SQL_UPDATE_PORTAFOLIO_RECHAZAR = PlataformaUtils.loadResourceToString("cl/bcs/portafolio/sql/AutorizacionCrearPortafolioMasivo_Update_PortafolioRechazar.sql");
	private final String SQL_SELECT_SOLICITUD_CAMBIO = PlataformaUtils.loadResourceToString("cl/bcs/portafolio/sql/AutorizacionCrearPortafolioMasivo_Select_SolicitudCambio.sql");
	
	// Clases auxiliares
	public static class Contador {
		public Long Contador;
	}
	
	public static class ClienteMatriz {
		public Long IdCliente;
		public Long IdMatrizAgrupadorAPV;
	}
	
	public static class ProductoAgrupador {
		public int IdProductoAgrupador;
	}
	
	public static class MatrizAgrupador {
		public Long IdMatrizAgrupadorAPV;
	}
	
	public static class UpdatePortafolio {
		public Long IdPortafolio;
		public String IdEstadoAutorizacion;
	}
	
	public static class UpdatePortafolioAPV {
		public Long IdCliente;
		public Long IdMatrizAgrupadorAPV;
		public String IdEstadoAutorizacion;
	}
	
	public static class UpdatePortafolioRechazar {
		public Long IdPortafolio;
		public String IdEstadoAutorizacion;
		public String IdEstado;
	}
	
	public static class SolicitudCambioData {
		public String IdEntidadOrigen;
		public String IdTipoEntidad;
	}
	
	/**
	 * Autorizar Portafolios - Migrado de ConfirmarCrearPortafolioMasivo
	 * Actualiza IdEstadoAutorizacion a 'ING' o valor recibido
	 * Maneja lógica especial para portafolios APV
	 */
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO AutorizacionCrearPortafolioMasivo.autorizarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		SQL sql = new SQL();
		
		try {
			// Validar que hay eventos a procesar
			if (entrada.p_Lista == null || entrada.p_Lista.isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "", 
					"No se proporcionaron portafolios para autorizar"));
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			}
			
			logger.debug("Procesando " + entrada.p_Lista.size() + " portafolios para autorización");
			
			// Estado de autorización al aprobar es siempre 'ING' (Ingresado)
			String v_IdEstadoAutorizacion = "ING";
			
			// Construir lista de IdPortafolios
			List<Long> idsPortafolio = new ArrayList<>();
			for (Evento evento : entrada.p_Lista) {
				if (evento.NumeroOperacionConcepto != null && !evento.NumeroOperacionConcepto.isEmpty()) {
					try {
						idsPortafolio.add(Long.parseLong(evento.NumeroOperacionConcepto.trim()));
					} catch (NumberFormatException e) {
						logger.warn("NumeroOperacionConcepto no es un número válido: " + evento.NumeroOperacionConcepto);
					}
				}
			}
			
			if (idsPortafolio.isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "", 
					"No se encontraron IDs de portafolio válidos"));
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			}
			
			// 1. Validar existencia de portafolios pendientes
			logger.debug("Validar portafolios pendientes");
			HashMap<String, Object> paramsCount = new HashMap<>();
			paramsCount.put("IdPortafolios", idsPortafolio);
			
			List<Contador> countList = sql.query(conn, SQL_COUNT_PORTAFOLIO_PENDIENTE, paramsCount, Contador.class);
			Long countPendientes = (countList != null && !countList.isEmpty() && countList.get(0).Contador != null) 
				? countList.get(0).Contador : 0L;
			
			if (countPendientes == 0) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_EXISTE_PORTAFOLIO", 
					"No existen portafolios pendientes de autorización"));
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			}
			
			// 2. Validar enrolamiento de clientes
			logger.debug("Validar enrolamiento de clientes");
			List<Contador> countClienteList = sql.query(conn, SQL_COUNT_CLIENTE_SIN_ENROLAMIENTO, paramsCount, Contador.class);
			Long countSinEnrolamiento = (countClienteList != null && !countClienteList.isEmpty() && countClienteList.get(0).Contador != null) 
				? countClienteList.get(0).Contador : 0L;
			
			if (countSinEnrolamiento > 0) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "AUTORIZACION_ENROLAMIENTO_CLIENTE_PENDIENTE", 
					"Autorización de enrolamiento de Cliente Pendiente"));
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			}
			
			// 3. Verificar si hay portafolios APV
			logger.debug("Consultar portafolios APV");
			List<ClienteMatriz> clienteMatrizList = sql.query(conn, SQL_SELECT_CLIENTE_MATRIZ_APV, paramsCount, ClienteMatriz.class);
			
			if (clienteMatrizList != null && !clienteMatrizList.isEmpty()) {
				// Lógica para portafolios APV
				logger.debug("Procesando portafolios APV - Total: " + clienteMatrizList.size());
				
				// Obtener ProductoAgrupador
				HashMap<String, Object> paramsMatriz = new HashMap<>();
				List<Long> idsMatriz = new ArrayList<>();
				List<Long> idsCliente = new ArrayList<>();
				for (ClienteMatriz cm : clienteMatrizList) {
					idsMatriz.add(cm.IdMatrizAgrupadorAPV);
					idsCliente.add(cm.IdCliente);
				}
				paramsMatriz.put("IdsMatrizAgrupadorAPV", idsMatriz);
				
				List<ProductoAgrupador> productoList = sql.query(conn, SQL_SELECT_PRODUCTO_AGRUPADOR, paramsMatriz, ProductoAgrupador.class);
				
				if (productoList != null && !productoList.isEmpty()) {
					// Obtener todas las matrices con el mismo ProductoAgrupador
					HashMap<String, Object> paramsProducto = new HashMap<>();
					List<Long> idsProducto = new ArrayList<>();
					for (ProductoAgrupador pa : productoList) {
						idsProducto.add((long) pa.IdProductoAgrupador);
					}
					paramsProducto.put("IdsProductoAgrupador", idsProducto);
					
					List<MatrizAgrupador> matrizAgrupadorList = sql.query(conn, SQL_SELECT_MATRIZ_AGRUPADOR, paramsProducto, MatrizAgrupador.class);
					
					// UPDATE portafolios APV relacionados
					logger.debug("UPDATE portafolios APV");
					List<UpdatePortafolioAPV> updatesAPV = new ArrayList<>();
					for (Long idCliente : idsCliente) {
						for (MatrizAgrupador ma : matrizAgrupadorList) {
							UpdatePortafolioAPV update = new UpdatePortafolioAPV();
							update.IdCliente = idCliente;
							update.IdMatrizAgrupadorAPV = ma.IdMatrizAgrupadorAPV;
							update.IdEstadoAutorizacion = v_IdEstadoAutorizacion;
							updatesAPV.add(update);
						}
					}
					sql.batch(conn, SQL_UPDATE_PORTAFOLIO_APV, updatesAPV, UpdatePortafolioAPV.class);
				}
			} else {
				// Lógica para portafolios NO-APV
				logger.debug("UPDATE portafolios NO-APV");
				List<UpdatePortafolio> updatesNoAPV = new ArrayList<>();
				for (Long idPortafolio : idsPortafolio) {
					UpdatePortafolio update = new UpdatePortafolio();
					update.IdPortafolio = idPortafolio;
					update.IdEstadoAutorizacion = v_IdEstadoAutorizacion;
					updatesNoAPV.add(update);
				}
				sql.batch(conn, SQL_UPDATE_PORTAFOLIO, updatesNoAPV, UpdatePortafolio.class);
			}
			
			// Agregar resultados exitosos
			for (Evento evento : entrada.p_Lista) {
				ResultadoAutorizacion resultado = new ResultadoAutorizacion();
				resultado.evento = evento;
				resultado.resultado = EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception ex) {
			logger.error("Error en autorizarEvento: " + ex.getMessage(), ex);
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "", 
				"Error al autorizar portafolios: " + ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR++;
		}
		
		logger.info("FIN AutorizacionCrearPortafolioMasivo.autorizarEvento");
		return salida;
	}
	
	/**
	 * Rechazar Portafolios - Migrado de RechazaAutorizacionPortafolioMasivo
	 * Actualiza IdEstadoAutorizacion a 'RZD' y IdEstado a 'INA'
	 * Si es modificación (MODPOR), llama a ModificarEstadoRegistroSolicitudCambioMasivo
	 */
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO AutorizacionCrearPortafolioMasivo.rechazarEvento");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		SQL sql = new SQL();
		
		try {
			// Validar que hay eventos a procesar
			if (entrada.p_Lista == null || entrada.p_Lista.isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "", 
					"No se proporcionaron portafolios para rechazar"));
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			}
			
			logger.debug("Procesando " + entrada.p_Lista.size() + " portafolios para rechazo");
			
			// Construir lista de IdPortafolios
			List<Long> idsPortafolio = new ArrayList<>();
			for (Evento evento : entrada.p_Lista) {
				if (evento.NumeroOperacionConcepto != null && !evento.NumeroOperacionConcepto.isEmpty()) {
					try {
						idsPortafolio.add(Long.parseLong(evento.NumeroOperacionConcepto.trim()));
					} catch (NumberFormatException e) {
						logger.warn("NumeroOperacionConcepto no es un número válido: '" + evento.NumeroOperacionConcepto + "'");
					}
				}
			}
			
			if (idsPortafolio.isEmpty()) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "", 
					"No se encontraron IDs de portafolio válidos"));
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			}
			
			// 1. Validar existencia de portafolios pendientes
			logger.debug("Validar portafolios pendientes");
			HashMap<String, Object> paramsCount = new HashMap<>();
			paramsCount.put("IdPortafolios", idsPortafolio);
			
			List<Contador> countList = sql.query(conn, SQL_COUNT_PORTAFOLIO_PENDIENTE, paramsCount, Contador.class);
			Long countPendientes = (countList != null && !countList.isEmpty() && countList.get(0).Contador != null) 
				? countList.get(0).Contador : 0L;
			
			if (countPendientes == 0) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "NO_EXISTE_PORTAFOLIO", 
					"No existen portafolios pendientes de autorización"));
				salida.P_OUT_ERRORESCONTADOR = 1L;
				return salida;
			}
			
			// 2. Si es MODPOR, llamar a ModificarEstadoRegistroSolicitudCambioMasivo
			String v_IdConcepto = entrada.p_Lista.get(0).IdConcepto;
			if ("MODPOR".equals(v_IdConcepto)) {
				logger.debug("Procesando rechazo de modificación MODPOR");
				
				// Consultar datos de SolicitudCambio
				HashMap<String, Object> paramsSolicitud = new HashMap<>();
				List<String> idsPortafolioStr = new ArrayList<>();
				for (Long id : idsPortafolio) {
					idsPortafolioStr.add(String.valueOf(id));
				}
				paramsSolicitud.put("IdsPortafolio", idsPortafolioStr);
				
				List<SolicitudCambioData> solicitudesList = sql.query(conn, SQL_SELECT_SOLICITUD_CAMBIO, paramsSolicitud, SolicitudCambioData.class);
				
				if (solicitudesList != null && !solicitudesList.isEmpty()) {
					// Llamar a ModificarEstadoRegistroSolicitudCambioMasivo
					ModificarEstadoRegistroSolicitudCambioMasivo modificarWrapper = new ModificarEstadoRegistroSolicitudCambioMasivo();
					ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado modificarIn = 
						new ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado();
					
					modificarIn.p_IdEmpresa = entrada.p_IdEmpresa;
					modificarIn.p_IdUsuario = entrada.p_IdUsuario;
					modificarIn.p_IdModulo = entrada.p_IdModulo;
					modificarIn.p_IdEstadoAutorizacion = "RZD";
					
					// Agregar entidades origen
					for (SolicitudCambioData sc : solicitudesList) {
						ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen eo = 
							new ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen();
						eo.IdEntidadOrigen = sc.IdEntidadOrigen;
						eo.IdTipoEntidad = sc.IdTipoEntidad;
						modificarIn.p_EntidadOrigen.add(eo);
					}
					
					ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado modificarOut = 
						modificarWrapper.logica(conn, modificarIn);
					
					// Propagar errores y advertencias
					salida.P_OUT_ERRORESCONTADOR += modificarOut.p_out_ErroresContador;
					for (ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : modificarOut.p_out_ErroresDetalle) {
						salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, err.Codigo, err.Descripcion));
					}
					
					// Si hay errores, retornar
					if (salida.P_OUT_ERRORESCONTADOR > 0) {
						return salida;
					}
				}
			}
			
			// 3. UPDATE portafolios - Estado a RZD e INA
			logger.debug("UPDATE portafolios a RZD/INA");
			List<UpdatePortafolioRechazar> updatesRechazar = new ArrayList<>();
			for (Long idPortafolio : idsPortafolio) {
				UpdatePortafolioRechazar update = new UpdatePortafolioRechazar();
				update.IdPortafolio = idPortafolio;
				update.IdEstadoAutorizacion = "RZD";
				update.IdEstado = "INA";
				updatesRechazar.add(update);
			}
			sql.batch(conn, SQL_UPDATE_PORTAFOLIO_RECHAZAR, updatesRechazar, UpdatePortafolioRechazar.class);
			
			// Agregar resultados exitosos
			for (Evento evento : entrada.p_Lista) {
				ResultadoAutorizacion resultado = new ResultadoAutorizacion();
				resultado.evento = evento;
				resultado.resultado = EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception ex) {
			logger.error("Error en rechazarEvento: " + ex.getMessage(), ex);
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "", 
				"Error al rechazar portafolios: " + ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR++;
		}
		
		logger.info("FIN AutorizacionCrearPortafolioMasivo.rechazarEvento");
		return salida;
	}
	
	/**
	 * Método main para pruebas
	 */
	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			
			AutorizacionCrearPortafolioMasivo servicio = new AutorizacionCrearPortafolioMasivo();
			
			// TEST 1: AUTORIZAR PORTAFOLIOS
			System.out.println("\n--- TEST AUTORIZAR ---");
			EntradaAutorizacion entradaAutorizar = new EntradaAutorizacion();
			entradaAutorizar.p_IdEmpresa = 1L;
			entradaAutorizar.p_IdUsuario = 1L;
			entradaAutorizar.p_IdModulo = "POR";
			entradaAutorizar.p_UsaReglasNegocio = "N";
			
			// Crear eventos - REEMPLAZAR con IDs reales
			Evento evento1 = new Evento();
			evento1.NumeroOperacionConcepto = "1000"; // IdPortafolio como String
			evento1.IdEmpresa = 1L;
			evento1.IdConcepto = "ING"; // Estado de autorización a aplicar
			entradaAutorizar.p_Lista.add(evento1);
			
			SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entradaAutorizar);
			System.out.println(salidaAutorizar.toString());
			
			// TEST 2: RECHAZAR PORTAFOLIOS
			System.out.println("\n--- TEST RECHAZAR ---");
			EntradaAutorizacion entradaRechazar = new EntradaAutorizacion();
			entradaRechazar.p_IdEmpresa = 1L;
			entradaRechazar.p_IdUsuario = 1L;
			entradaRechazar.p_IdModulo = "POR";
			entradaRechazar.p_UsaReglasNegocio = "N";
			
			Evento evento2 = new Evento();
			evento2.NumeroOperacionConcepto = "1001"; // IdPortafolio como String
			evento2.IdEmpresa = 1L;
			evento2.IdConcepto = "MODPOR"; // Tipo de concepto
			entradaRechazar.p_Lista.add(evento2);
			
			SalidaAutorizacion salidaRechazar = servicio.rechazarEvento(conn, entradaRechazar);
			System.out.println(salidaRechazar.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
