package cl.bcs.mesa.MRF;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.mesa.FWD.ConfirmarRechazarAutorizacionOperacionForward.Evento;
import cl.bcs.mesa.MRF.AnularOperacionMesa.AnularOperacionMesa_IN;
import cl.bcs.mesa.MRF.AnularOperacionMesa.AnularOperacionMesa_OUT;
import cl.bcs.mesa.MRF.pojo.CacheMRF;
import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.utils.CacheServicio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearComprobante;
import cl.bcs.tesoreria.CrearComprobante.CrearComprobante_Entrada;
import cl.bcs.tesoreria.CrearComprobante.CrearComprobante_Salida;
import cl.bcs.tesoreria.CrearComprobante.IngresoEgresoGenerar_Entrada;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConfirmarRechazarAutorizacionOperacionMesa implements JavaCall, BcsAutorizaciones {
	
	static final Logger logger = LogManager.getLogger(ConfirmarRechazarAutorizacionOperacionMesa.class);
	
	//Consultas
	private final String SQL_SELECT_OPERACION_MESA = loadResourceToString("cl/bcs/mesa/MRF/sql/ConfirmarRechazarAutorizacionOperacionMesa_SelectOperacionMesa.sql");
	private final String SQL_UPDATE_ESTADO_OPERACION = loadResourceToString("cl/bcs/mesa/MRF/sql/ConfirmarRechazarAutorizacionOperacionMesa_UpdateEstadoOperacion.sql");
	private final String SQL_CREATE_LOCAL_TEMP_TABLE_EVENTO_OPERACION_MESA = loadResourceToString("cl/bcs/mesa/MRF/sql/ConfirmarRechazarAutorizacionOperacionMesa_CreateLocalTempTableEventoOperacionMesa.sql");
	private final String SQL_INSERT_TEMP_EVENTO_OPERACION_MESA = loadResourceToString("cl/bcs/mesa/MRF/sql/ConfirmarRechazarAutorizacionOperacionMesa_InsertTempEventoOperacionMesa.sql");
	private final String SQL_DROP_LOCAL_TEMP_TABLE_EVENTO_OPERACION_MESA = loadResourceToString("cl/bcs/mesa/MRF/sql/ConfirmarRechazarAutorizacionOperacionMesa_DropLocalTempTableEventoOperacionMesa.sql");
	private final String SQL_UPDATE_EVENTO = loadResourceToString("cl/bcs/mesa/MRF/sql/ConfirmarRechazarAutorizacionOperacionMesa_UpdateEvento.sql");
	private final String SQL_SELECT_EVENTO_OPERACION_MESA = loadResourceToString("cl/bcs/mesa/MRF/sql/ConfirmarRechazarAutorizacionOperacionMesa_SelectEventoOperacionMesa.sql");
	private final String SQL_SELECT_OBTENER_INGRESO_EGRESO = loadResourceToString("cl/bcs/mesa/MRF/sql/ConfirmarRechazarAutorizacionOperacionMesa_SelectIngresoEgresoOperacionMesa.sql");


	
	private ContextoProceso contexto = new ContextoProceso();
	
	private static class ContextoProceso{
		
		public Connection Connection;
		public EntradaAutorizacion Entrada;
		public String FechaSistemaStr;
		public Boolean EsAutorizacion;
		public List<Long> OperacionMesaIds;
		public Set<String> TipoEventoIds;
		
		public ContextoProceso() {
			Entrada = new EntradaAutorizacion();
			FechaSistemaStr = "";
			EsAutorizacion = false;
			OperacionMesaIds = new ArrayList<Long>();
			TipoEventoIds = Stream.of("LIMECP").collect(Collectors.toSet());
		}
		
	}
	
	public static class OperacionMesa extends BcsPojo{
		public Long IdOperacion;
		public String IdTipoOperacion;
		public java.sql.Timestamp FechaOperacion;
		public String Estado;
		public String EstadoConfirmacion;
		public Long IdEmpresa;
		
		public String EstadoPrevioAutorizacion;
		public String EstadoPosteriorAutorizacion;
	}	
	

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {

		logger.info("INICIO::ConfirmarRechazarAutorizacionOperacionMesa.autorizarEvento");
		long inicio = System.currentTimeMillis();

		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			contexto = obtenerContexto(conn, entrada, "AUT");
			List<OperacionMesa> operaciones = obtenerOperaciones();
			Map<Long,OperacionMesa> operacionesPorId =  obtenerOperacionMesaPorId(operaciones);
			
			Map<Long, List<Evento>> eventosPendientesPorIdOperacionMesa = obtenerEventosPendientesPorIdAutorizacionMesa(operacionesPorId); 
			
			List<OperacionMesa> operacionesConfirmar = new ArrayList<>(operaciones.size());
			List<OperacionMesa> operacionesPendientes = new ArrayList<>(operaciones.size());
			
			for(OperacionMesa operacion : operaciones) {
				List<Evento> eventosFaltantes = eventosPendientesPorIdOperacionMesa.getOrDefault(operacion.IdOperacion, Collections.emptyList());
				List<OperacionMesa> destino;
				if(eventosFaltantes.isEmpty()) {
					destino = operacionesConfirmar;
					operacion.EstadoPosteriorAutorizacion = "VIG";
				}else {
					destino = operacionesPendientes;
					operacion.EstadoPosteriorAutorizacion = "PDA";
				}
				destino.add(operacion);
			}
			
			modificarOperaciones(operacionesConfirmar);	
			generarComprobantesMesa(operacionesConfirmar);
			CacheServicio.invalidarPorTag(CacheMRF.TAG_POSICION);
			AgregarMensajeWebsocket.agregar(conn, entrada.p_IdEmpresa, "ACTMRF", "Refresco Operaciones Mesa RF/IF");
			agregarLog(operacionesConfirmar);
		} catch (Exception e) {
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("INICIO::ConfirmarRechazarAutorizacionOperacionMesa.autorizarEvento {}", fin-inicio);
		
		return salida;
	}		

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::ConfirmarRechazarAutorizacionOperacionMesa.rechazarEvento");
		long inicio = System.currentTimeMillis();
		
		SalidaAutorizacion salida = new SalidaAutorizacion();
		try {
			contexto = obtenerContexto(conn, entrada, "RZD");
			List<OperacionMesa> operaciones = obtenerOperaciones();
			Map<Long,OperacionMesa> operacionesPorId =  obtenerOperacionMesaPorId(operaciones);
			
			Map<Long, List<Evento>> eventosPendientesPorIdOperacionMesa = obtenerEventosPendientesPorIdAutorizacionMesa(operacionesPorId); 
			
			//modificarOperaciones(operaciones); // Cambiar por llamada a anularOperacionMesa
			modificarRestoEventosAsociados(eventosPendientesPorIdOperacionMesa);
			
			anularOperacionesMesa(operaciones);
			agregarLog(operaciones);
			
		} catch (Exception e) {
			e.printStackTrace();
			salida.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("INICIO::ConfirmarRechazarAutorizacionOperacionMesa.rechazarEvento {}", fin-inicio);
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	private ContextoProceso obtenerContexto(Connection conn, EntradaAutorizacion entrada, String accion) throws Exception {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionMesa.obtenerContexto");
		long inicio = System.currentTimeMillis();
		
		ContextoProceso contexto = new ContextoProceso();
		contexto.Connection = conn;
		contexto.Entrada = entrada;
		contexto.FechaSistemaStr = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		contexto.EsAutorizacion = "AUT".equals(accion);
		contexto.OperacionMesaIds = entrada.p_Lista.stream().map(u -> Long.valueOf(u.NumeroOperacionConcepto)).distinct().collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionMesa.obtenerContexto {}", fin-inicio);
		
		return contexto;
	}
	
	private List<OperacionMesa> obtenerOperaciones() throws SQLException{
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionMesa.obtenerOperaciones");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>(4);
		params.put("IdOperacion", contexto.OperacionMesaIds);
		params.put("IdEstado", "PDA");
		
		List<OperacionMesa> operaciones = new SQL().query(contexto.Connection, SQL_SELECT_OPERACION_MESA, params, OperacionMesa.class);
		for(OperacionMesa op: operaciones) {
			op.EstadoPosteriorAutorizacion = contexto.EsAutorizacion ? "VIG" : "ANU";
			op.EstadoConfirmacion = contexto.EsAutorizacion ? "CNF" : "RZD";
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionMesa.obtenerOperaciones {}", fin-inicio);
		
		return operaciones;
	}
	
	private Map<Long,OperacionMesa> obtenerOperacionMesaPorId(List<OperacionMesa> operaciones){
		logger.trace("INICIO::ConfirmarRechazarAutorizacionOperacionForward.obtenerOperacionMesaPorId");
		long inicio = System.currentTimeMillis();
		
		Map<Long,OperacionMesa> operacionPorId = operaciones.stream().collect(Collectors.toMap(u -> u.IdOperacion, u -> u));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConfirmarRechazarAutorizacionOperacionForward.obtenerOperacionMesaPorId {}", fin - inicio);
		
		return operacionPorId;
	}
	
	private Map<Long,List<Evento>> obtenerEventosPendientesPorIdAutorizacionMesa(Map<Long,OperacionMesa> operacionesPorId) throws SQLException{
		
		logger.trace("INICIO::ConfirmarRechazarAutorizacionOperacionForward.obtenerEventosPendientesPorIdAutorizacionMesa");
		long inicio = System.currentTimeMillis();
		
		final long idTrabajo = 1L;
		SQL.exec(contexto.Connection, SQL_CREATE_LOCAL_TEMP_TABLE_EVENTO_OPERACION_MESA);
		
		Set<String> clasificacionesIgnoradas = contexto.Entrada.p_Lista.stream().map(u -> u.IdTipoEvento + "-" + u.NumeroOperacionConcepto).collect(Collectors.toSet());
		List<Evento> eventos = contexto.OperacionMesaIds.stream()
				.map(operacionesPorId::get)
				.filter(Objects::nonNull)
				.flatMap(u -> contexto.TipoEventoIds.stream().map(e -> Pair.of(u, e)))
				.filter(parOperacionEvento -> clasificacionesIgnoradas.contains(parOperacionEvento.getRight() + "-" + parOperacionEvento.getLeft().IdOperacion) == false)
				.map(parOperacionEvento -> {
					OperacionMesa operacion = parOperacionEvento.getLeft();
					String idTipoEvento = parOperacionEvento.getRight();
					Evento evento = new Evento();
					evento.IdOperacion = operacion.IdOperacion;
					evento.NumeroOperacionConcepto = String.valueOf(operacion.IdOperacion);
					evento.IdTipoEvento = idTipoEvento;
					evento.IdEmpresa = operacion.IdEmpresa;
					evento.IdTipoOperacion = operacion.IdTipoOperacion;
					evento.Fecha = Fecha.toDate(Fecha.toStringDate(operacion.FechaOperacion));
					evento.IdTrabajo = idTrabajo;
					evento.IdEstadoPosteriorAutorizacion = contexto.EsAutorizacion ? "AUT" : "RZD";
					return evento;
				})
				.collect(Collectors.toList());
		
		SQL sql = new SQL();
		
		sql.batch(contexto.Connection, SQL_INSERT_TEMP_EVENTO_OPERACION_MESA, eventos, Evento.class);
		
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdTrabajo", idTrabajo);
		
		Map<Long, List<Evento>> eventosPorIdOperacion = sql.query(contexto.Connection,SQL_SELECT_EVENTO_OPERACION_MESA, params, Evento.class).stream()
				.collect(Collectors.groupingBy(u -> Long.valueOf(u.NumeroOperacionConcepto)));
		
		SQL.exec(contexto.Connection, SQL_DROP_LOCAL_TEMP_TABLE_EVENTO_OPERACION_MESA);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConfirmarRechazarAutorizacionOperacionForward.obtenerEventosPendientesPorIdAutorizacionMesa {}", fin - inicio);
		
		return eventosPorIdOperacion;
	}
	
	
	private AnularOperacionMesa_OUT anularOperacionesMesa(List<OperacionMesa> operaciones) throws Exception {
		logger.info("INICIO::ConfirmarRechazarAutorizacionOperacionMesa.anularOperacionesMesa");
		long inicio = System.currentTimeMillis();
		
		AnularOperacionMesa_IN entradaAnularOperacionMesa = new AnularOperacionMesa_IN();
		entradaAnularOperacionMesa.p_IdEmpresa = contexto.Entrada.p_IdEmpresa;
		entradaAnularOperacionMesa.p_IdUsuario = contexto.Entrada.p_IdUsuario;
		entradaAnularOperacionMesa.p_IdModulo = "MRF";//contexto.Entrada.p_IdModulo;
		entradaAnularOperacionMesa.p_UsaReglasNegocio = "S";
		entradaAnularOperacionMesa.p_Operaciones = operaciones.stream()
				.map(o -> {
					AnularOperacionMesa.Operacion operacion = new AnularOperacionMesa.Operacion();
					operacion.IdOperacion = o.IdOperacion;
					operacion.IdEstadoSiguiente = "ANU";
					operacion.EstadoConfirmacion = "RZD";
					return operacion;
				})
				.collect(Collectors.toList());
		
		AnularOperacionMesa_OUT salidaAnularOperacionMesa = new AnularOperacionMesa().logica(contexto.Connection, entradaAnularOperacionMesa);
		
		
		long fin = System.currentTimeMillis();
		logger.info("INICIO::ConfirmarRechazarAutorizacionOperacionMesa.anularOperacionesMesa {}", fin-inicio);
		
		return salidaAnularOperacionMesa;
	}
	

	private void modificarOperaciones(List<OperacionMesa> operaciones) throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionMesa.modificarOperaciones");
		long inicio = System.currentTimeMillis();
		
		new SQL().batch(contexto.Connection, SQL_UPDATE_ESTADO_OPERACION, operaciones, OperacionMesa.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionMesa.modificarOperaciones {}", fin - inicio);
	}
	
	private void modificarRestoEventosAsociados(Map<Long, List<Evento>> eventosPorIdOperacionForward) throws SQLException {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionMesa.modificarRestoEventosAsociados");
		long inicio = System.currentTimeMillis();
		
		List<Evento> eventos = eventosPorIdOperacionForward.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
		new SQL().batch(contexto.Connection, SQL_UPDATE_EVENTO, eventos, Evento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionMesa.modificarRestoEventosAsociados {}", fin - inicio);
	}
	

	private void generarComprobantesMesa(List<OperacionMesa> operaciones) throws Exception {
		
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionMesa.generarComprobante");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>(1);
		
		
		for(OperacionMesa op: operaciones) {
			params.put("IdOperacion", op.IdOperacion);
			List<IngresoEgresoGenerar_Entrada> ingresosEgresosGenerarComprobante = new SQL().query(contexto.Connection, SQL_SELECT_OBTENER_INGRESO_EGRESO, params , IngresoEgresoGenerar_Entrada.class);
			
			CrearComprobante_Entrada crearComprobanteEntrada = new CrearComprobante_Entrada();
			
			crearComprobanteEntrada.p_IdEmpresa = contexto.Entrada.p_IdEmpresa;  
			crearComprobanteEntrada.p_IdUsuario = contexto.Entrada.p_IdUsuario;
			crearComprobanteEntrada.p_IdModulo = "AUT";
			crearComprobanteEntrada.p_SimularOperacion =  "N";
			crearComprobanteEntrada.p_UsaReglasNegocio = "N";
			crearComprobanteEntrada.p_FechaMovimiento = contexto.FechaSistemaStr;				
			crearComprobanteEntrada.p_IdCaja = 0L;		
			crearComprobanteEntrada.p_GenerarComprobanteDesdeMovimientos  = "S";
			crearComprobanteEntrada.p_GenerarComprobanteDesdeComprobantes = "N" ;	
			crearComprobanteEntrada.p_EsIngresoAutomatico = "S";
			crearComprobanteEntrada.p_IngresoEgresoGenerar = ingresosEgresosGenerarComprobante; //Generar comprobantes desde movimientos
			
			CrearComprobante crearComprobante = new CrearComprobante();
			CrearComprobante_Salida crearComprobante_Salida = crearComprobante.logica(contexto.Connection, crearComprobanteEntrada);
			
			if (MensajeSalida.getNumeroErrores(crearComprobante_Salida.P_OUT_MENSAJES) > 0) 
			{
				logger.debug("GENERACION DE COMPROBANTES CON ERRORES: " + crearComprobante_Salida.P_OUT_MENSAJES);
			}
			else
			{
				logger.debug("GENERACION DE COMPROBANTES OK: " + crearComprobante_Salida.P_OUT_MENSAJES);
			}
			
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarRechazarAutorizacionOperacionMesa.generarComprobante {}", fin - inicio);
	}
	
	
	private void agregarLog(List<OperacionMesa> operaciones) throws Exception {
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionMesa.agregarLog");
		long inicio = System.currentTimeMillis();
		
		final long idTrabajo = 1L;
		
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdEmpresa = contexto.Entrada.p_IdEmpresa;
		entradaAgregarLog.p_IdUsuario = contexto.Entrada.p_IdUsuario;
		entradaAgregarLog.p_IdModulo = contexto.Entrada.p_IdModulo;
		entradaAgregarLog.p_UsaReglasNegocio = contexto.Entrada.p_UsaReglasNegocio;
		entradaAgregarLog.p_IdTrabajo = idTrabajo;
		entradaAgregarLog.p_IdEstadoAutorizacion = contexto.EsAutorizacion ? "VIG" : "ANU";
		
		List<AgregarLog.InAgregarLog.Log> logs = new ArrayList<>(operaciones.size());
		List<AgregarLog.InAgregarLog.DetalleLog> detalleLogs = new ArrayList<>(operaciones.size());
		
		for(OperacionMesa operacion: operaciones) {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdTrabajo = idTrabajo;
			log.IdTipoLog = "INFORMATIVO";
			log.IdModulo = contexto.Entrada.p_IdModulo;
			log.IdUsuario = contexto.Entrada.p_IdUsuario;
			log.IdEmpresa = contexto.Entrada.p_IdEmpresa;
			log.FechaSistemaLog = contexto.FechaSistemaStr;
			log.IdUsuarioAtendido = contexto.Entrada.p_IdUsuario;
			log.IdTipoRegistro = "MERF";
			log.NombreProceso = "Confirmacion Operacion Mesa RF/IF excede Limite Paridad y Tipo Cambio";
			//log.IdRegistro = "";
			log.EstadoProceso = contexto.EsAutorizacion ? "AUT" : "RZD";
			logs.add(log);
			
			AgregarLog.InAgregarLog.DetalleLog detalle = new AgregarLog.InAgregarLog.DetalleLog();
			detalle.IdTrabajo = idTrabajo;
			detalle.SubRegistro = "LimiteParidadYTipoCambio";
			detalle.CampoRegistro = "Estado";
			detalle.ValorAntiguo = operacion.EstadoPrevioAutorizacion;
			detalle.ValorNuevo = operacion.EstadoPosteriorAutorizacion;
			detalleLogs.add(detalle);
		}
		
		entradaAgregarLog.p_Log.addAll(logs);
		entradaAgregarLog.p_LogDetalle.addAll(detalleLogs);
		new AgregarLog().logica(contexto.Connection, entradaAgregarLog);
		
		long fin = System.currentTimeMillis();
		logger.debug("INICIO::ConfirmarRechazarAutorizacionOperacionMesa.agregarLog {}", fin-inicio);
	}

}
