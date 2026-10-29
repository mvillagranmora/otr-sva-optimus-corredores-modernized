package cl.bcs.portafolio;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.eje.NotificarCambioEjecutivoCliente;
import cl.bcs.APV.ReplicaPortafolioAPV;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Servicio: AutorizacionModificarPortafolioMasivo
 * Propósito: Autorizar o Rechazar Modificación de Portafolios de forma masiva
 * Migrado de: ConfirmarModificarPortafolioMasivo.SQL + RechazaAutorizacionPortafolioMasivo.SQL
 * Autor: Equipo Migración
 * Fecha: Abril 2026
 */
public class AutorizacionModificarPortafolioMasivo implements BcsAutorizaciones {
	private Logger logger = LogManager.getLogger(AutorizacionModificarPortafolioMasivo.class);
	
	// Archivos SQL
	private final String SQL_COUNT_SOLICITUD = PlataformaUtils.loadResourceToString("cl/bcs/portafolio/sql/AutorizacionModificarPortafolioMasivo_Count_Solicitud.sql");
	private final String SQL_SELECT_DATOS_SOLICITUD = PlataformaUtils.loadResourceToString("cl/bcs/portafolio/sql/AutorizacionModificarPortafolioMasivo_Select_DatosSolicitud.sql");
	private final String SQL_COUNT_PORTAFOLIO_APV = PlataformaUtils.loadResourceToString("cl/bcs/portafolio/sql/AutorizacionModificarPortafolioMasivo_Count_PortafolioAPV.sql");
	private final String SQL_UPDATE_PERFIL_COMERCIAL_BASE = PlataformaUtils.loadResourceToString("cl/bcs/portafolio/sql/AutorizacionModificarPortafolioMasivo_Update_PerfilComercialBase.sql");
	
	/**
	 * Clase auxiliar para mapear portafolios
	 */
	public static class PortafolioEntrada {
		public Long IdPortafolio;
		
		public PortafolioEntrada(Long idPortafolio) {
			this.IdPortafolio = idPortafolio;
		}
	}
	
	/**
	 * Clase para contador
	 */
	public static class Contador {
		public Long Contador;
	}
	
	/**
	 * Clase para datos de solicitud de cambio
	 */
	public static class DatosSolicitud {
		public Long IdSolicitudCambio;
		public String IdEntidadOrigen;
		public String IdTipoEntidad;
		public String CampoModificado;
		public String ValorAntiguo;
		public String ValorNuevo;
		public Long IdPortafolio;
	}
	
	/**
	 * Clase para portafolios APV
	 */
	public static class PortafolioAPV {
		public Long IdPortafolio;
	}
	
	/**
	 * Clase para agrupar los cambios por portafolio en batch update
	 */
	public static class UpdatePortafolio {
		public Long IdPortafolio;
		public String CodigoPortafolio;
		public Long IdNombreFantasia;
		public Long IdPersonaBrokerExtranjero;
		public Long IdEmpleadoEjecutivo;
		public String NumeroFicha;
		public String Alias;
		public String DescripcionPortafolio;
		public String Proposito;
		public String IdPerfilComercialBaseOrigen;
		public String IdPerfilProductos;
		public String IdPerfilProductosExtra;
		public String IdPerfilCobros;
		public String IdPerfilCobrosExtra;
		public String IdPerfilCobrosPeru;
		public String IdPerfilCobrosPeruExtra;
		public String IdPerfilLimites;
		public String IdPerfilLimitesExtra;
		public String IdPerfilCondicionesOperativas;
		public String IdPerfilCondicionesOperativasExtra;
		public Long IdDireccionEnvioCorrespondencia;
		public Long IdTelefonoContacto;
		public String AreaCliente;
		public Timestamp FechaModificacion;
		public Long IdEmpleadoEjecutivoComercial;
	}
	
	/**
	 * Clase auxiliar para errores retornados por el SP
	 */
	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;
	}
	
	/**
	 * Clase auxiliar para advertencias retornadas por el SP
	 */
	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}

	/**
	 * Autorizar modificación de portafolios
	 * Migrado de: ConfirmarModificarPortafolioMasivo
	 */
	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO:AUTORIZACION MODIFICAR PORTAFOLIO");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		SQL sql = new SQL();
		
		try {
			// Extraer lista de IDs de portafolios
			List<Long> idsPortafolio = entrada.p_Lista.stream()
				.map(evt -> Long.parseLong(evt.NumeroOperacionConcepto))
				.collect(Collectors.toList());
			
			String v_TipoEntidad = "PORTAFOLIO";
			String v_IdEstadoAutorizacion = "ING"; // Estado al autorizar
			
			// 1. Validar existencia de solicitudes
			logger.debug("Validar existencia de solicitudes");
			HashMap<String, Object> paramsCount = new HashMap<>();
			paramsCount.put("IdPortafolios", idsPortafolio);
			paramsCount.put("IdTipoEntidad", v_TipoEntidad);
			paramsCount.put("IdEstadoAutorizacion", "PEN");
			
			List<Contador> countList = sql.query(conn, SQL_COUNT_SOLICITUD, paramsCount, Contador.class);
			Long v_countSolicitud = (countList != null && !countList.isEmpty() && countList.get(0).Contador != null) 
				? countList.get(0).Contador : 0L;
			
			if (v_countSolicitud == 0) {
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
					"SOLICITUD_NO_EXISTE", "No existen solicitudes de cambio pendientes"));
				return salida;
			}
			
			// 2. Obtener datos de solicitudes de cambio
			logger.debug("Obtener datos de solicitudes");
			List<DatosSolicitud> v_DatosSolicitudCambio = sql.query(conn, SQL_SELECT_DATOS_SOLICITUD, 
				paramsCount, DatosSolicitud.class);
			
			Long v_CountAtributoModificado = (long) v_DatosSolicitudCambio.size();
			
			if (v_CountAtributoModificado > 0) {
				// Identificar campos únicos a modificar
				java.util.Set<String> camposAModificar = v_DatosSolicitudCambio.stream()
					.map(ds -> ds.CampoModificado)
					.collect(Collectors.toSet());
				
				logger.debug("Campos a modificar: " + camposAModificar);
				
			// Separar IdPerfilComercialBaseOrigen para procesamiento especial (requiere JOIN)
			boolean tienePerfilComercialBase = camposAModificar.contains("IdPerfilComercialBaseOrigen");
			java.util.Set<String> camposBatch = new java.util.HashSet<>(camposAModificar);
			if (tienePerfilComercialBase) {
				camposBatch.remove("IdPerfilComercialBaseOrigen");
			}
			
			// Procesar campos normales con batch update
			if (!camposBatch.isEmpty()) {
				// Agrupar solicitudes por IdPortafolio
				Map<Long, List<DatosSolicitud>> solicitudesPorPortafolio = v_DatosSolicitudCambio.stream()
					.filter(ds -> !ds.CampoModificado.equals("IdPerfilComercialBaseOrigen"))
					.collect(Collectors.groupingBy(ds -> ds.IdPortafolio));
				
				// Crear lista de updates
				List<UpdatePortafolio> updatesPortafolio = new ArrayList<>();
				Timestamp ahora = new Timestamp(System.currentTimeMillis());
				
				for (Map.Entry<Long, List<DatosSolicitud>> entry : solicitudesPorPortafolio.entrySet()) {
					UpdatePortafolio updatePortafolio = new UpdatePortafolio();
					updatePortafolio.IdPortafolio = entry.getKey();
					updatePortafolio.FechaModificacion = ahora;
					
					// Asignar valores nuevos según campo modificado
					for (DatosSolicitud ds : entry.getValue()) {
						asignarValorNuevo(updatePortafolio, ds.CampoModificado, ds.ValorNuevo);
					}
					
					updatesPortafolio.add(updatePortafolio);
				}
				
				// Construir y ejecutar UPDATE batch dinámico
				String updateSQL = buildUpdatePortafolioSQL(camposBatch);
				logger.debug("Ejecutando UPDATE batch para " + updatesPortafolio.size() + " portafolios con " + camposBatch.size() + " campos");
				sql.batch(conn, updateSQL, updatesPortafolio, UpdatePortafolio.class);
			}
			
			// Procesar IdPerfilComercialBaseOrigen con JOIN especial
			if (tienePerfilComercialBase) {
				logger.debug("Procesando IdPerfilComercialBaseOrigen con JOIN a PerfilComercialBase");
				
				List<UpdatePerfilComercialBase> updates = new ArrayList<>();
				Timestamp ahora = new Timestamp(System.currentTimeMillis());
				
				for (DatosSolicitud ds : v_DatosSolicitudCambio) {
					if ("IdPerfilComercialBaseOrigen".equals(ds.CampoModificado)) {
						UpdatePerfilComercialBase update = new UpdatePerfilComercialBase();
						update.IdPortafolio = ds.IdPortafolio;
						update.ValorNuevo = ds.ValorNuevo;
						update.FechaModificacion = ahora;
						updates.add(update);
					}
				}
				
				sql.batch(conn, SQL_UPDATE_PERFIL_COMERCIAL_BASE, updates, UpdatePerfilComercialBase.class);
				logger.debug("Actualizado IdPerfilComercialBaseOrigen para " + updates.size() + " portafolios");
			}
			
			// Manejar lógica especial para cambio de ejecutivo
			if (camposAModificar.contains("IdEmpleadoEjecutivo")) {
				logger.debug("Procesando notificación de cambio de ejecutivo");
				notificarCambioEjecutivo(conn, entrada, v_DatosSolicitudCambio);
			}
		}
		
		// 3. Modificar estado de solicitudes de cambio
			logger.debug("Modificar estado de solicitudes");
			ModificarEstadoRegistroSolicitudCambioMasivo modificarWrapper = new ModificarEstadoRegistroSolicitudCambioMasivo();
			ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado modificarIn = 
				new ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado();
			
			modificarIn.p_IdEmpresa = entrada.p_IdEmpresa;
			modificarIn.p_IdUsuario = entrada.p_IdUsuario;
			modificarIn.p_IdModulo = entrada.p_IdModulo;
			modificarIn.p_IdEstadoAutorizacion = v_IdEstadoAutorizacion;
			
			// Agregar entidades origen unicas
			modificarIn.p_EntidadOrigen = new ArrayList<>();
			HashMap<String, ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen> mapEntidades = new HashMap<>();
			for (DatosSolicitud ds : v_DatosSolicitudCambio) {
				String key = ds.IdEntidadOrigen + "_" + ds.IdTipoEntidad;
				if (!mapEntidades.containsKey(key)) {
					ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen eo = 
						new ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen();
					eo.IdEntidadOrigen = ds.IdEntidadOrigen;
					eo.IdTipoEntidad = ds.IdTipoEntidad;
					mapEntidades.put(key, eo);
				}
			}
			modificarIn.p_EntidadOrigen.addAll(mapEntidades.values());
			
			ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado modificarOut = 
				modificarWrapper.logica(conn, modificarIn);
			
			// Propagar errores
			if (modificarOut.p_out_ErroresContador > 0) {
				salida.P_OUT_ERRORESCONTADOR = (long) modificarOut.p_out_ErroresContador;
				for (ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : modificarOut.p_out_ErroresDetalle) {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, 
						err.Codigo, err.Descripcion));
				}
				return salida;
			}
			
			// 4. Verificar portafolios APV y replicar
			logger.debug("Verificar portafolios APV");
			HashMap<String, Object> paramsAPV = new HashMap<>();
			paramsAPV.put("IdPortafolios", idsPortafolio);
			List<PortafolioAPV> portafoliosAPV = sql.query(conn, SQL_COUNT_PORTAFOLIO_APV, paramsAPV, PortafolioAPV.class);
			
			if (portafoliosAPV != null && !portafoliosAPV.isEmpty()) {
				logger.debug("Se encontraron " + portafoliosAPV.size() + " portafolios APV para replicar");
				
				// Llamar al servicio ReplicaPortafolioAPV para cada portafolio APV
				ReplicaPortafolioAPV servicioAPV = new ReplicaPortafolioAPV();
				for (PortafolioAPV porAPV : portafoliosAPV) {
					try {
						ReplicaPortafolioAPV.EntradaReplicaPortafolioAPV entradaAPV = new ReplicaPortafolioAPV.EntradaReplicaPortafolioAPV();
						entradaAPV.p_IdEmpresa = entrada.p_IdEmpresa;
						entradaAPV.p_IdUsuario = entrada.p_IdUsuario;
						entradaAPV.p_IdModulo = entrada.p_IdModulo;
						entradaAPV.p_UsaReglasNegocio = "N";
						entradaAPV.p_Portafolio = porAPV.IdPortafolio;
						entradaAPV.p_IdEntidad = 0L;
						entradaAPV.p_TipoEntidad = "PORTAFOLIO";
						entradaAPV.p_TipoOperacion = "U";
						
						ReplicaPortafolioAPV.SalidaReplicaPortafolioAPV salidaAPV = servicioAPV.logica(conn, entradaAPV);
						
						if (salidaAPV.P_OUT_ERRORESCONTADOR > 0) {
						String errores = "";
						if (salidaAPV.P_OUT_MENSAJES != null && !salidaAPV.P_OUT_MENSAJES.isEmpty()) {
							errores = salidaAPV.P_OUT_MENSAJES.stream()
								.map((MensajeSalida msg) -> msg.codigo + ": " + msg.descripcion)
								.collect(java.util.stream.Collectors.joining(", "));
						}
							logger.error("Error al replicar portafolio APV " + porAPV.IdPortafolio + ": " + errores);
						} else {
							logger.debug("Portafolio APV replicado: " + porAPV.IdPortafolio);
						}
					} catch (Exception ex) {
						logger.error("Error al replicar portafolio APV " + porAPV.IdPortafolio + ": " + ex.getMessage(), ex);
						// Continuar con los demás portafolios
					}
				}
			}
			
			// 5. Agregar log de auditoría
			logger.debug("Agregar log de auditoría");
			agregarLog(conn, entrada, v_DatosSolicitudCambio, v_IdEstadoAutorizacion);
			
			// 6. Éxito - crear resultados por cada evento
			salida.P_OUT_ERRORESCONTADOR = 0L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION,
				"OPERACION_AUTORIZADA", "Modificación de portafolio realizada con éxito"));
			
			for (Evento evento : entrada.p_Lista) {
				ResultadoAutorizacion resultado = new ResultadoAutorizacion();
				resultado.evento = evento.IdEvento;
				resultado.resultado = EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception ex) {
			logger.error("Error en autorizarEvento: " + ex.getMessage(), ex);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
				"ERROR_AUTORIZACION", "Error al realizar modificación: " + ex.getMessage()));
		}
		
		logger.info("TERMINO:AUTORIZACION MODIFICAR PORTAFOLIO");
		return salida;
	}
	
	/**
	 * Asigna el valor nuevo al campo modificado del objeto UpdatePortafolio
	 */
	private void asignarValorNuevo(UpdatePortafolio updatePortafolio, String campoModificado, String valorNuevo) {
		if (valorNuevo == null) {
			return;
		}
		
		try {
			switch (campoModificado) {
				case "CodigoPortafolio":
					updatePortafolio.CodigoPortafolio = valorNuevo;
					break;
				case "IdNombreFantasia":
					updatePortafolio.IdNombreFantasia = valorNuevo.isEmpty() ? null : Long.parseLong(valorNuevo);
					break;
				case "IdPersonaBrokerExtranjero":
					updatePortafolio.IdPersonaBrokerExtranjero = valorNuevo.isEmpty() ? null : Long.parseLong(valorNuevo);
					break;
				case "IdEmpleadoEjecutivo":
					updatePortafolio.IdEmpleadoEjecutivo = valorNuevo.isEmpty() ? null : Long.parseLong(valorNuevo);
					break;
				case "NumeroFicha":
					updatePortafolio.NumeroFicha =valorNuevo;
					break;
				case "Alias":
					updatePortafolio.Alias = valorNuevo;
					break;
				case "DescripcionPortafolio":
					updatePortafolio.DescripcionPortafolio = valorNuevo;
					break;
				case "Proposito":
					updatePortafolio.Proposito = valorNuevo;
					break;
				case "IdPerfilComercialBaseOrigen":
				// Este campo se procesa por separado con JOIN, no debe llegar aquí
				logger.warn("IdPerfilComercialBaseOrigen debería procesarse por separado");
					break;
				case "IdPerfilProductos":
				updatePortafolio.IdPerfilProductos = valorNuevo;
					break;
				case "IdPerfilProductosExtra":
					updatePortafolio.IdPerfilProductosExtra = valorNuevo;
					break;
				case "IdPerfilCobros":
					updatePortafolio.IdPerfilCobros = valorNuevo;
					break;
				case "IdPerfilCobrosExtra":
					updatePortafolio.IdPerfilCobrosExtra = valorNuevo;
					break;
				case "IdPerfilCobrosPeru":
					updatePortafolio.IdPerfilCobrosPeru = valorNuevo;
					break;
				case "IdPerfilCobrosPeruExtra":
					updatePortafolio.IdPerfilCobrosPeruExtra = valorNuevo;
					break;
				case "IdPerfilLimites":
					updatePortafolio.IdPerfilLimites = valorNuevo;
					break;
				case "IdPerfilLimitesExtra":
					updatePortafolio.IdPerfilLimitesExtra = valorNuevo;
					break;
				case "IdPerfilCondicionesOperativas":
					updatePortafolio.IdPerfilCondicionesOperativas = valorNuevo;
					break;
				case "IdPerfilCondicionesOperativasExtra":
					updatePortafolio.IdPerfilCondicionesOperativasExtra = valorNuevo;
					break;
				case "IdDireccionEnvioCorrespondencia":
					updatePortafolio.IdDireccionEnvioCorrespondencia = valorNuevo.isEmpty() ? null : Long.parseLong(valorNuevo);
					break;
				case "IdTelefonoContacto":
					updatePortafolio.IdTelefonoContacto = valorNuevo.isEmpty() ? null : Long.parseLong(valorNuevo);
					break;
				case "AreaCliente":
					updatePortafolio.AreaCliente = valorNuevo;
					break;
				case "IdEmpleadoEjecutivoComercial":
				    updatePortafolio.IdEmpleadoEjecutivoComercial = valorNuevo.isEmpty() ? null : Long.parseLong(valorNuevo);
				    break;
				default:
					logger.warn("Campo modificado no reconocido: " + campoModificado);
					break;
					
			}
		} catch (NumberFormatException e) {
			logger.error("Error al convertir valor para campo " + campoModificado + ": " + valorNuevo, e);
		}
	}
	
	/**
	 * Construye dinámicamente el SQL para actualizar solo los campos necesarios
	 */
	private String buildUpdatePortafolioSQL(java.util.Set<String> camposAModificar) {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE \"BSCL.Portafolio.BD::POR.Portafolio\" SET ");
		
		List<String> setClauses = new ArrayList<>();
		
		// Agregar cada campo a modificar
		for (String campo : camposAModificar) {
			String setClause = buildSetClause(campo);
			if (setClause != null) {
				setClauses.add(setClause);
			}
		}
		
		// Unir todas las cláusulas SET
		sb.append(String.join(", ", setClauses));
		
		// Siempre actualizar FechaModificacion
		if (!setClauses.isEmpty()) {
			sb.append(", ");
		}
		sb.append("\"FechaModificacion\" = :FechaModificacion ");
		sb.append("WHERE \"IdPortafolio\" = :IdPortafolio");
		
		return sb.toString();
	}
	
	/**
	 * Construye la cláusula SET para un campo específico
	 */
	private String buildSetClause(String campo) {
		// Validar que el campo sea reconocido
		if (!esCampoValido(campo)) {
			logger.warn("Campo no reconocido en buildSetClause: " + campo);
			return null;
		}
		
		// Todos los campos siguen el mismo patrón
		return String.format("\"%s\" = CASE WHEN :%s IS NOT NULL THEN :%s ELSE \"%s\" END", 
			campo, campo, campo, campo);
	}
	
	/**
	 * Valida si el campo es uno de los campos modificables del portafolio
	 */
	private boolean esCampoValido(String campo) {
		switch (campo) {
			case "CodigoPortafolio":
			case "IdNombreFantasia":
			case "IdPersonaBrokerExtranjero":
			case "IdEmpleadoEjecutivo":
			case "NumeroFicha":
			case "Alias":
			case "DescripcionPortafolio":
			case "Proposito":
			case "IdPerfilComercialBaseOrigen":
			case "IdPerfilProductos":
			case "IdPerfilProductosExtra":
			case "IdPerfilCobros":
			case "IdPerfilCobrosExtra":
			case "IdPerfilCobrosPeru":
			case "IdPerfilCobrosPeruExtra":
			case "IdPerfilLimites":
			case "IdPerfilLimitesExtra":
			case "IdPerfilCondicionesOperativas":
			case "IdPerfilCondicionesOperativasExtra":
			case "IdDireccionEnvioCorrespondencia":
			case "IdTelefonoContacto":
			case "AreaCliente":
			case "IdEmpleadoEjecutivoComercial":
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * Construye dinámicamente el SQL UPDATE para un campo específico
	 */
	private String buildUpdateSQL(String campoModificado, Timestamp fechaModificacion) {
		// Campos que requieren CASE WHEN para valores vacíos
		boolean requiresCaseWhen = "IdDireccionEnvioCorrespondencia".equals(campoModificado) ||
								   "IdTelefonoContacto".equals(campoModificado) ||
								   "AreaCliente".equals(campoModificado);
		
		String valorExpression = requiresCaseWhen 
			? "CASE WHEN :ValorNuevo = '' THEN 0 ELSE :ValorNuevo END"
			: ":ValorNuevo";
		
		// Caso especial para IdPerfilComercialBaseOrigen (actualiza también IdPerfilProductos)
		if ("IdPerfilComercialBaseOrigen".equals(campoModificado)) {
			return "UPDATE \"BSCL.Portafolio.BD::POR.Portafolio\" DOC " +
				   "SET DOC.\"IdPerfilComercialBaseOrigen\" = :ValorNuevo, " +
				   "DOC.\"IdPerfilProductos\" = PCOM.\"IdPerfilProductos\", " +
				   "DOC.\"FechaModificacion\" = :FechaModificacion " +
				   "FROM \"BSCL.Plataforma.BD::COM.PerfilComercialBase\" PCOM " +
				   "WHERE PCOM.\"IdPerfilComercialBase\" = :ValorNuevo " +
				   "AND DOC.\"IdPortafolio\" = :IdPortafolio";
		}
		
		return "UPDATE \"BSCL.Portafolio.BD::POR.Portafolio\" " +
			   "SET \"" + campoModificado + "\" = " + valorExpression + ", " +
			   "\"FechaModificacion\" = :FechaModificacion " +
			   "WHERE \"IdPortafolio\" = :IdPortafolio";
	}
	
	/**
	 * Notifica cambio de ejecutivo para los portafolios afectados
	 * Migrado de lógica especial en ConfirmarModificarPortafolioMasivo (líneas 234-247)
	 */
	private void notificarCambioEjecutivo(Connection conn, EntradaAutorizacion entrada, 
										   List<DatosSolicitud> datosSolicitud) throws Exception {
		
		// Filtrar solo las solicitudes de cambio de ejecutivo
		List<DatosSolicitud> cambiosEjecutivo = datosSolicitud.stream()
			.filter(ds -> "IdEmpleadoEjecutivo".equals(ds.CampoModificado))
			.collect(Collectors.toList());
		
		if (cambiosEjecutivo.isEmpty()) {
			return;
		}
		
		SQL sql = new SQL();
		
		// Por cada cambio de ejecutivo, obtener el IdCliente del portafolio y notificar
		for (DatosSolicitud ds : cambiosEjecutivo) {
			Long idPortafolio = Long.parseLong(ds.IdEntidadOrigen);
			Long idNuevoEjecutivo = ds.ValorNuevo != null && !ds.ValorNuevo.isEmpty() 
				? Long.parseLong(ds.ValorNuevo) : 0L;
			
			// Consultar IdCliente del portafolio
			String queryCliente = "SELECT \"IdCliente\" FROM \"BSCL.Portafolio.BD::POR.Portafolio\" " +
								 "WHERE \"IdPortafolio\" = :IdPortafolio";
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdPortafolio", idPortafolio);
			
			List<ClientePortafolio> clientes = sql.query(conn, queryCliente, params, ClientePortafolio.class);
			
			if (clientes != null && !clientes.isEmpty()) {
				Long idCliente = clientes.get(0).IdCliente;
				
				// Llamar al servicio NotificarCambioEjecutivoCliente
				NotificarCambioEjecutivoCliente wrapper = new NotificarCambioEjecutivoCliente();
				NotificarCambioEjecutivoCliente.Entrada entradaNot = 
					new NotificarCambioEjecutivoCliente.Entrada();
				
				entradaNot.IdSolicitud = ds.IdSolicitudCambio;
				entradaNot.IdCliente = idCliente;
				entradaNot.IdPersonaNuevoEjecutivo = idNuevoEjecutivo;
				
				wrapper.ejecutar(conn, entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo, entradaNot);
				
				logger.info("Notificación de cambio de ejecutivo enviada - IdSolicitud: " + ds.IdSolicitudCambio + 
						   ", IdCliente: " + idCliente + ", IdNuevoEjecutivo: " + idNuevoEjecutivo);
			}
		}
	}
	
	/**
	 * Clase auxiliar para mapear IdCliente del portafolio
	 */
	public static class ClientePortafolio {
		public Long IdCliente;
	}
	
	/**
	 * Clase para UPDATE de IdPerfilComercialBaseOrigen con JOIN
	 */
	public static class UpdatePerfilComercialBase {
		public Long IdPortafolio;
		public String ValorNuevo;
		public Timestamp FechaModificacion;
	}
	
	/**
	 * Agregar log de auditoría
	 */
	private void agregarLog(Connection conn, EntradaAutorizacion entrada, 
							List<DatosSolicitud> datosSolicitud, String idEstadoAutorizacion) throws Exception {
		
		String fechaHoraSistema = Utils.getFechaSistema(conn, Utils.getSchema(), entrada.p_IdEmpresa.intValue());
		
		AgregarLog.InAgregarLog logIn = new AgregarLog.InAgregarLog();
		
		// Crear Log (cabecera)
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		log.IdCabeceraLog = 1L;
		log.IdTipoLog = "MODIFICACION";
		log.IdModulo = "POR";
		log.IdUsuario = entrada.p_IdUsuario;
		log.IdEmpresa = entrada.p_IdEmpresa;
		log.FechaSistemaLog = fechaHoraSistema;
		log.IdUsuarioAtendido = entrada.p_IdUsuario;
		log.IdTipoRegistro = "PORTAFOLIO";
		logIn.p_Log.add(log);
		
		// Crear DetalleLog (detalle de cambios)
		for (DatosSolicitud ds : datosSolicitud) {
			AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
			detLog.IdCabeceraLog = 1L;
			detLog.IdTrabajo = 0L;
			detLog.SubRegistro = ds.IdTipoEntidad;
			detLog.CampoRegistro = ds.CampoModificado;
			detLog.ValorAntiguo = ds.ValorAntiguo != null ? ds.ValorAntiguo : "";
			detLog.ValorNuevo = ds.ValorNuevo != null ? ds.ValorNuevo : "";
			logIn.p_LogDetalle.add(detLog);
		}
		
		// Ejecutar servicio de log
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(conn, logIn);
	}
	
	/**
	 * Rechazar modificación de portafolios
	 * Migrado de: RechazaAutorizacionPortafolioMasivo
	 */
	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO:RECHAZO MODIFICAR PORTAFOLIO");
		SalidaAutorizacion salida = new SalidaAutorizacion();
		SQL sql = new SQL();
		
		try {
			// Extraer lista de IDs de portafolios
			List<Long> idsPortafolio = entrada.p_Lista.stream()
				.map(evt -> Long.parseLong(evt.NumeroOperacionConcepto))
				.collect(Collectors.toList());
			
			String v_TipoEntidad = "PORTAFOLIO";
			String v_IdEstadoAutorizacion = "RZD"; // Estado al rechazar
			
			// 1. Validar existencia de solicitudes de cambio pendientes
			logger.debug("Validar existencia de solicitudes de cambio pendientes");
			HashMap<String, Object> paramsCount = new HashMap<>();
			paramsCount.put("IdPortafolios", idsPortafolio);
			paramsCount.put("IdTipoEntidad", v_TipoEntidad);
			paramsCount.put("IdEstadoAutorizacion", "PEN");
			
			List<Contador> countList = sql.query(conn, SQL_COUNT_SOLICITUD, paramsCount, Contador.class);
			Long v_countSolicitud = (countList != null && !countList.isEmpty() && countList.get(0).Contador != null) 
				? countList.get(0).Contador : 0L;
			
			if (v_countSolicitud == 0) {
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
					"SOLICITUD_NO_EXISTE", "No existen solicitudes de cambio pendientes"));
				return salida;
			}
			
			// 2. Obtener datos de solicitudes de cambio
			logger.debug("Obtener datos de solicitudes de cambio");
			List<DatosSolicitud> v_DatosSolicitudCambio = sql.query(conn, SQL_SELECT_DATOS_SOLICITUD, 
				paramsCount, DatosSolicitud.class);
			
			if (v_DatosSolicitudCambio == null || v_DatosSolicitudCambio.isEmpty()) {
				salida.P_OUT_ERRORESCONTADOR = 1L;
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
					"SOLICITUD_NO_EXISTE", "No se encontraron datos de solicitudes de cambio"));
				return salida;
			}
			
			// 3. Modificar estado de solicitudes de cambio a RZD (Rechazado)
			logger.debug("Modificar estado de solicitudes a RZD");
			ModificarEstadoRegistroSolicitudCambioMasivo modificarWrapper = new ModificarEstadoRegistroSolicitudCambioMasivo();
			ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado modificarIn = 
				new ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado();
			
			modificarIn.p_IdEmpresa = entrada.p_IdEmpresa;
			modificarIn.p_IdUsuario = entrada.p_IdUsuario;
			modificarIn.p_IdModulo = entrada.p_IdModulo;
			modificarIn.p_IdEstadoAutorizacion = v_IdEstadoAutorizacion;
			
			// Agregar entidades origen unicas
			modificarIn.p_EntidadOrigen = new ArrayList<>();
			HashMap<String, ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen> mapEntidades = new HashMap<>();
			for (DatosSolicitud ds : v_DatosSolicitudCambio) {
				String key = ds.IdEntidadOrigen + "_" + ds.IdTipoEntidad;
				if (!mapEntidades.containsKey(key)) {
					ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen eo = 
						new ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen();
					eo.IdEntidadOrigen = ds.IdEntidadOrigen;
					eo.IdTipoEntidad = ds.IdTipoEntidad;
					mapEntidades.put(key, eo);
				}
			}
			modificarIn.p_EntidadOrigen.addAll(mapEntidades.values());
			
			ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado modificarOut = 
				modificarWrapper.logica(conn, modificarIn);
			
			// Propagar errores
			if (modificarOut.p_out_ErroresContador > 0) {
				salida.P_OUT_ERRORESCONTADOR = (long) modificarOut.p_out_ErroresContador;
				for (ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : modificarOut.p_out_ErroresDetalle) {
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, 
						err.Codigo, err.Descripcion));
				}
				return salida;
			}
			
			// 4. Éxito - crear resultados por cada evento
			salida.P_OUT_ERRORESCONTADOR = 0L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION,
				"OPERACION_RECHAZADA", "Modificación de portafolio rechazada con éxito"));
			
			for (Evento evento : entrada.p_Lista) {
				ResultadoAutorizacion resultado = new ResultadoAutorizacion();
				resultado.evento = evento.IdEvento;
				resultado.resultado = EXITO_AUTORIZACION;
				salida.P_OUT_RESULTADO.add(resultado);
			}
			
		} catch (Exception ex) {
			logger.error("Error en rechazarEvento: " + ex.getMessage(), ex);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
				"ERROR_RECHAZO", "Error al rechazar modificación: " + ex.getMessage()));
		}
		
		logger.info("TERMINO:RECHAZO MODIFICAR PORTAFOLIO");
		return salida;
	}
	
	/**
	 * Método main para pruebas
	 */
	public static void main(String[] args) throws SQLException {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			
			AutorizacionModificarPortafolioMasivo servicio = new AutorizacionModificarPortafolioMasivo();
			EntradaAutorizacion entrada = new EntradaAutorizacion();
			
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdModulo = "POR";
			entrada.p_IdUsuario = 1L;
			
			// Crear evento de prueba
			Evento evento = new Evento();
			evento.NumeroOperacionConcepto = "1000"; // IdPortafolio a autorizar/rechazar
			evento.IdEmpresa = 1L;
			evento.IdConcepto = "MODPOR";
			evento.IdEvento = 1L;
			entrada.p_Lista.add(evento);
			
			// Probar AUTORIZAR
			System.out.println("\n=== AUTORIZAR MODIFICACIÓN ===");
			SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entrada);
			System.out.println("Errores: " + salidaAutorizar.P_OUT_ERRORESCONTADOR);
			System.out.println("Mensajes: " + salidaAutorizar.P_OUT_MENSAJES.size());
			System.out.println("Resultados: " + salidaAutorizar.P_OUT_RESULTADO.size());
			conn.rollback();
			
			// Probar RECHAZAR
			System.out.println("\n=== RECHAZAR MODIFICACIÓN ===");
			SalidaAutorizacion salidaRechazar = servicio.rechazarEvento(conn, entrada);
			System.out.println("Errores: " + salidaRechazar.P_OUT_ERRORESCONTADOR);
			System.out.println("Mensajes: " + salidaRechazar.P_OUT_MENSAJES.size());
			System.out.println("Resultados: " + salidaRechazar.P_OUT_RESULTADO.size());
			conn.rollback();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
