package cl.bcs.socionegocio.cli;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambio.EntradaConsultarRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambio.SalidaConsultarRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.EntidadLogGenerarContrato;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.pojo.DeclaracionOrigenFondos;
import cl.bcs.socionegocio.cli.pojo.DetalleOrigenFondo;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarModificarDeclaracionOrigenFondosMasivo implements JavaCall, BcsAutorizaciones {
	
	static final Logger logger = LogManager.getLogger(ConfirmarModificarDeclaracionOrigenFondosMasivo.class);
	
	private String SQL_UPDATE_DECLARACION_ORIGEN_FONDOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConfirmarModificarDeclaracionOrigenFondosMasivo_UpdateDeclaracionOrigenFondos.sql");
	private String SQL_SELECT_DETALLE_DECLARACION_ORIGEN_FONDOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConfirmarModificarDeclaracionOrigenFondosMasivo_SelectDetalleDeclaracionOrigenFondos.sql");
	private String SQL_DELETE_DETALLE_DECLARACION_ORIGEN_FONDOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConfirmarModificarDeclaracionOrigenFondosMasivo_DeleteDetalleDeclaracionOrigenFondos.sql");
	private String SQL_INSERT_DETALLE_DECLARACION_ORIGEN_FONDOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConfirmarModificarDeclaracionOrigenFondosMasivo_InsertDetalleDeclaracionOrigenFondos.sql");
	private String SQL_INSERT_LOG_CONTRATOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConfirmarModificarDeclaracionOrigenFondosMasivo_InsertLogContratos.sql");

	private final String ID_TIPO_ENTIDAD        = "CLIENTEDOF";
	private final String ID_ESTADO_RECHAZADO    = "RZD";
	private final String USA_REGLAS_NEGOCIO     = "N";

	public static class EntradaConfirmarModificarDeclaracionOrigenFondosMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdEstadoAutorizacion;
		public List<InDeclaracionOrigenFondos> p_DeclaracionOrigenFondos;
		
		public EntradaConfirmarModificarDeclaracionOrigenFondosMasivo() {
			this.p_DeclaracionOrigenFondos = new ArrayList<>();
		}
	}
	
	public static class InDeclaracionOrigenFondos extends BcsPojo {
		public Long IdDeclaracionOrigenFondos;

		public InDeclaracionOrigenFondos(Long idDeclaracionOrigenFondos) {
			this.IdDeclaracionOrigenFondos = idDeclaracionOrigenFondos;
		}
	}

	public static class SalidaConfirmarModificarDeclaracionOrigenFondosMasivo extends BcsPojo {
		public Long					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;
		
		public SalidaConfirmarModificarDeclaracionOrigenFondosMasivo() {
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
	}



	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarModificarDeclaracionOrigenFondosMasivo jin = gson.fromJson(in.toString(), EntradaConfirmarModificarDeclaracionOrigenFondosMasivo.class);
		SalidaConfirmarModificarDeclaracionOrigenFondosMasivo out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaConfirmarModificarDeclaracionOrigenFondosMasivo logica(Connection con, EntradaConfirmarModificarDeclaracionOrigenFondosMasivo entrada) throws Exception {
		logger.info("INICIO::ConfirmarModificarDeclaracionOrigenFondosMasivo");
		long inicioServicio = System.currentTimeMillis();
		SalidaConfirmarModificarDeclaracionOrigenFondosMasivo salida = new SalidaConfirmarModificarDeclaracionOrigenFondosMasivo();
		
		
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios(con, entrada);	
		if (solicitudesCambios.isEmpty()){
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return salida;
		}

		// Punto 1: Validar existencia de la entidad
		if (!existeEntidad(con, entrada)) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ENTIDAD_NO_EXISTE", null, con, entrada.p_IdEmpresa));
			return salida;
		}

		// Punto 4: Insert en LogGenerarContratos antes de los UPDATEs
		insertarLogContratos(con, entrada, solicitudesCambios);

		List<DeclaracionOrigenFondos> declaracionOrigenFondos = obtenerEntidadesDeclaracionOrigenFondos(solicitudesCambios);
		List<DetalleOrigenFondo> todosLosDetalles = obtenerTodosLosDetalles(solicitudesCambios, entrada);

		if (declaracionOrigenFondos.isEmpty() && todosLosDetalles.isEmpty()) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "SOLICITUD_SIN_CAMBIOS", null, con, entrada.p_IdEmpresa));
			return salida;
		}

		modificarCampoDeclaracionOrigenFondos(con, declaracionOrigenFondos);	
		
		// DELETE + INSERT de detalles como lo hace el SP original
		if (!todosLosDetalles.isEmpty()) {
			reemplazarDetalles(con, entrada, todosLosDetalles);
		}

		// Punto 5: usar ModificarEstadoRegistroSolicitudCambioMasivo con todas las entidades
		SalidaModificarEstado salidaModificarEstado = modificarEstadoRegistroSolicitudCambio(con, entrada, solicitudesCambios);
		salida.P_OUT_ERRORESCONTADOR += salidaModificarEstado.p_out_ErroresContador;
		for (ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : salidaModificarEstado.p_out_ErroresDetalle) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, err.Codigo, err.Descripcion));
		}
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
			return salida;
		}

		agregarLog(con, entrada, solicitudesCambios);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarDeclaracionOrigenFondosMasivo", finServicio - inicioServicio);
		return salida;
	}

	/**
	 * Reemplaza todos los detalles de declaración: SELECT actual + MERGE cambios + DELETE + INSERT
	 * Lee los detalles actuales de la BD, los combina con los cambios de las solicitudes,
	 * y luego hace DELETE + INSERT de TODOS los valores para mantener los que no se modificaron
	 */
	private void reemplazarDetalles(Connection con, EntradaConfirmarModificarDeclaracionOrigenFondosMasivo entrada, List<DetalleOrigenFondo> cambiosDetalles) throws Exception {
		logger.debug("INICIO::ConfirmarModificarDeclaracionOrigenFondosMasivo.reemplazarDetalles");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		
		// Agrupar cambios por IdDeclaracionOrigenFondos
		Map<Long, List<DetalleOrigenFondo>> cambiosPorDOF = cambiosDetalles.stream()
				.collect(Collectors.groupingBy(d -> d.IdDeclaracionOrigenFondos));
		
		for (Entry<Long, List<DetalleOrigenFondo>> entry : cambiosPorDOF.entrySet()) {
			Long idDOF = entry.getKey();
			List<DetalleOrigenFondo> cambios = entry.getValue();
			
			// 1. Leer detalles actuales de la BD
			HashMap<String, Object> params = new HashMap<>();
			params.put("IdDeclaracionOrigenFondos", idDOF);
			List<DetalleOrigenFondo> detallesActuales = sql.query(con, SQL_SELECT_DETALLE_DECLARACION_ORIGEN_FONDOS, params, DetalleOrigenFondo.class);
			
			// 2. Hacer merge: actualizar los que vienen en cambios, mantener los demás
			Map<String, DetalleOrigenFondo> detallesFinales = new HashMap<>();
			
			// Primero agregar todos los actuales
			for (DetalleOrigenFondo actual : detallesActuales) {
				detallesFinales.put(actual.IdTipoOrigenFondo, actual);
			}
			
			// Luego actualizar los campos de los que vienen en cambios
			for (DetalleOrigenFondo cambio : cambios) {
				DetalleOrigenFondo detalleExistente = detallesFinales.get(cambio.IdTipoOrigenFondo);
				if (detalleExistente != null) {
					// Actualizar ambos campos (todos usan Valor, OTR además usa Otros)

					detalleExistente.Valor = cambio.Valor;
					detalleExistente.Otros = cambio.Otros;
				} else {
					// Si no existe, agregar el nuevo (ya viene con los campos correctos asignados)
					detallesFinales.put(cambio.IdTipoOrigenFondo, cambio);
				}
			}
			
			List<DetalleOrigenFondo> todosLosDetalles = new ArrayList<>(detallesFinales.values());
			
			// 3. DELETE todos los detalles actuales
			DetalleOrigenFondo paramDelete = new DetalleOrigenFondo();
			paramDelete.IdDeclaracionOrigenFondos = idDOF;
			List<DetalleOrigenFondo> paramsDelete = new ArrayList<>();
			paramsDelete.add(paramDelete);
			sql.batch(con, SQL_DELETE_DETALLE_DECLARACION_ORIGEN_FONDOS, paramsDelete, DetalleOrigenFondo.class);
			
			// 4. INSERT TODOS los detalles (actuales + modificados)
			for (DetalleOrigenFondo detalle : todosLosDetalles) {
				detalle.IdUsuarioCreador = entrada.p_IdUsuario;
			}
			sql.batch(con, SQL_INSERT_DETALLE_DECLARACION_ORIGEN_FONDOS, todosLosDetalles, DetalleOrigenFondo.class);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarDeclaracionOrigenFondosMasivo.reemplazarDetalles", fin - inicio);
	}

	private void modificarCampoDeclaracionOrigenFondos(Connection con, List<DeclaracionOrigenFondos> declaracionOrigenFondos) throws Exception {
		if (declaracionOrigenFondos.isEmpty()) return;
		logger.debug("INICIO::ConfirmarModificarDeclaracionOrigenFondosMasivo.modificarCampoDeclaracionOrigenFondos");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		
		sql.batch(con, SQL_UPDATE_DECLARACION_ORIGEN_FONDOS, declaracionOrigenFondos, DeclaracionOrigenFondos.class);			
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarDeclaracionOrigenFondosMasivo.modificarCampoDeclaracionOrigenFondos", fin - inicio);
	}

	private List<DeclaracionOrigenFondos> obtenerEntidadesDeclaracionOrigenFondos(List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.trace("INICIO::ConfirmarModificarDeclaracionOrigenFondosMasivo.obtenerEntidadesDeclaracionOrigenFondos");
		long inicio = System.currentTimeMillis();
		
		Map<Long, List<SolicitudCambio>> solicitudesPorIdEntidad = solicitudesCambios.parallelStream()
												.filter(solicitudCambio -> "IdTipoOperacionOrigenFondos".equals(solicitudCambio.CampoModificado)
														|| "FechaDeclaracion".equals(solicitudCambio.CampoModificado)
														|| "Comentario".equals(solicitudCambio.CampoModificado)
														|| "IdEstado".equals(solicitudCambio.CampoModificado)
														|| "OperacionInscripcion".equals(solicitudCambio.CampoModificado))
												.collect(Collectors.groupingBy(solicitudCambio->solicitudCambio.IdEntidadOrigenLong));
		
		List<DeclaracionOrigenFondos> datosAActualizar = new ArrayList<>();
		for(Entry<Long, List<SolicitudCambio>> cliente : solicitudesPorIdEntidad.entrySet()) {
			DeclaracionOrigenFondos declaracionOrigenFondos = new DeclaracionOrigenFondos();
			List<SolicitudCambio> cambiosCliente= cliente.getValue();
			declaracionOrigenFondos.IdDeclaracionOrigenFondos = cambiosCliente.parallelStream().findFirst().get().IdEntidadOrigenLong;
			for(SolicitudCambio solicitudCambio : cambiosCliente) {
				Field campo = declaracionOrigenFondos.getClass().getField(solicitudCambio.CampoModificado);
				if(campo.getType().isAssignableFrom(String.class)) {
					campo.set(declaracionOrigenFondos, solicitudCambio.ValorNuevo);
				}
				if(campo.getType().isAssignableFrom(Long.class)) {
					campo.set(declaracionOrigenFondos, Long.parseLong(solicitudCambio.ValorNuevo));
				}
				if(campo.getType().isAssignableFrom(java.sql.Timestamp.class)) {
					campo.set(declaracionOrigenFondos, Fecha.toDateTime(solicitudCambio.ValorNuevo));
				}
				if(campo.getType().isAssignableFrom(java.sql.Date.class)) {
					campo.set(declaracionOrigenFondos,  Fecha.toDate(solicitudCambio.ValorNuevo));
				}
			}
			datosAActualizar.add(declaracionOrigenFondos);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConfirmarModificarDeclaracionOrigenFondosMasivo.obtenerEntidadesDeclaracionOrigenFondos", fin - inicio);
		return datosAActualizar;
	}
	
/**
	 * Obtiene TODOS los detalles (regulares + OTR) que se deben insertar
	 * Igual que el SP que hace INSERT INTO #p_Detalle SELECT "CampoModificado", "ValorNuevo" 
	 * FROM :v_DatosSolicitudCambio WHERE "CampoModificado" IN (SELECT "IdTipoOrigenFondo" ...)
	 */
	private List<DetalleOrigenFondo> obtenerTodosLosDetalles(List<SolicitudCambio> solicitudesCambios, EntradaConfirmarModificarDeclaracionOrigenFondosMasivo entrada) throws Exception {
		logger.trace("INICIO::ConfirmarModificarDeclaracionOrigenFondosMasivo.obtenerTodosLosDetalles");
		long inicio = System.currentTimeMillis();
		
		// Filtra campos que NO son de cabecera (son detalles: IdTipoOrigenFondo incluyendo OTR)
		List<SolicitudCambio> solicitudesDetalle = solicitudesCambios.stream()
				.filter(sc -> !"IdTipoOperacionOrigenFondos".equals(sc.CampoModificado)
						&& !"FechaDeclaracion".equals(sc.CampoModificado)
						&& !"Comentario".equals(sc.CampoModificado)
						&& !"IdEstado".equals(sc.CampoModificado)
						&& !"OperacionInscripcion".equals(sc.CampoModificado))
				.collect(Collectors.toList());
		
		List<DetalleOrigenFondo> todosLosDetalles = new ArrayList<>();
		for(SolicitudCambio solicitudCambio : solicitudesDetalle) {
			DetalleOrigenFondo detalle = new DetalleOrigenFondo();
			detalle.IdDeclaracionOrigenFondos = solicitudCambio.IdEntidadOrigenLong;
			detalle.IdTipoOrigenFondo = solicitudCambio.CampoModificado;
			
			// Todos los tipos guardan en Valor (DECIMAL). OTR además guarda en Otros (NVARCHAR) por compatibilidad
			detalle.Valor = new BigDecimal(solicitudCambio.ValorNuevo);
			if ("OTR".equals(solicitudCambio.CampoModificado)) {
				detalle.Otros = solicitudCambio.ValorNuevo; // OTR duplica en columna texto
			} else {
				detalle.Otros = ""; // Otros tipos no usan columna Otros
			}
			
			todosLosDetalles.add(detalle);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConfirmarModificarDeclaracionOrigenFondosMasivo.obtenerTodosLosDetalles", fin - inicio);
		return todosLosDetalles;
	}

	private void agregarLog(Connection con, EntradaConfirmarModificarDeclaracionOrigenFondosMasivo entrada, List<SolicitudCambio> solicitudCambio) throws Exception {
		logger.debug("INICIO::ConfirmarModificarDeclaracionOrigenFondosMasivo.agregarLog");
		long inicio = System.currentTimeMillis();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		entradaAgregarLog.p_Log = solicitudCambio.parallelStream().map(datoGeneral->{
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = datoGeneral.IdEntidadOrigenLong;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdRegistro = datoGeneral.IdEntidadOrigenLong.toString();
			log.IdTipoRegistro = ID_TIPO_ENTIDAD;
			log.EstadoRegistro = entrada.p_IdEstadoAutorizacion;
			log.IdProceso = 0L;
			log.EstadoProceso = entrada.p_IdEstadoAutorizacion;
			log.NombreProceso = "Confirmar Solicitud Cambio Declaracion Origen Fondos Cliente";
			log.IdPersona = datoGeneral.IdEntidadOrigenLong;
			return log;
		}).collect(Collectors.toList());
		
		entradaAgregarLog.p_LogDetalle = solicitudCambio.parallelStream().map(solicitud->{
			AgregarLog.InAgregarLog.DetalleLog detalleLog = new AgregarLog.InAgregarLog.DetalleLog();			
			detalleLog.IdTrabajo = 0L; 
			detalleLog.IdCabeceraLog = solicitud.IdEntidadOrigenLong;			
			detalleLog.SubRegistro = solicitud.IdTipoEntidad;
			detalleLog.CampoRegistro = solicitud.CampoModificado;
			detalleLog.ValorAntiguo = solicitud.ValorAntiguo; 
			detalleLog.ValorNuevo = solicitud.ValorNuevo;	
			return detalleLog;
		}).collect(Collectors.toList());
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarDeclaracionOrigenFondosMasivo.agregarLog", fin - inicio);
	}

	private SalidaModificarEstado modificarEstadoRegistroSolicitudCambio(Connection con, EntradaConfirmarModificarDeclaracionOrigenFondosMasivo entrada, List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.debug("INICIO::ConfirmarModificarDeclaracionOrigenFondosMasivo.modificarEstadoRegistroSolicitudCambio");
		long inicio = System.currentTimeMillis();
		ModificarEstadoRegistroSolicitudCambioMasivo modificarEstado = new ModificarEstadoRegistroSolicitudCambioMasivo();
		EntradaModificarEstado entradaModificar = new EntradaModificarEstado();
		entradaModificar.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificar.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificar.p_IdModulo = entrada.p_IdModulo;
		entradaModificar.p_IdEstadoAutorizacion = entrada.p_IdEstadoAutorizacion;
		// Pasar todas las entidades origen (no solo la primera)
		solicitudesCambios.stream()
			.map(s -> s.IdEntidadOrigenLong)
			.distinct()
			.forEach(idEntidad -> {
				ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen eo = new ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen();
				eo.IdEntidadOrigen = String.valueOf(idEntidad);
				eo.IdTipoEntidad = ID_TIPO_ENTIDAD;
				entradaModificar.p_EntidadOrigen.add(eo);
			});
		SalidaModificarEstado salida = modificarEstado.logica(con, entradaModificar);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarDeclaracionOrigenFondosMasivo.modificarEstadoRegistroSolicitudCambio", fin - inicio);
		return salida;
	}

	private boolean existeEntidad(Connection con, EntradaConfirmarModificarDeclaracionOrigenFondosMasivo entrada) throws Exception {
		SQL sql = new SQL();
		java.util.HashMap<String, Object> params = new java.util.HashMap<>();
		params.put("IdDeclaracionOrigenFondos", entrada.p_DeclaracionOrigenFondos.stream()
				.map(d -> d.IdDeclaracionOrigenFondos).collect(Collectors.toList()));
		long count = sql.countQuery(con,
			"SELECT COUNT(\"IdDeclaracionOrigenFondos\") FROM \"BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos\" "
			+ "WHERE \"IdDeclaracionOrigenFondos\" IN (:IdDeclaracionOrigenFondos)", params);
		return count > 0;
	}

	private void insertarLogContratos(Connection con, EntradaConfirmarModificarDeclaracionOrigenFondosMasivo entrada, List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.debug("INICIO::ConfirmarModificarDeclaracionOrigenFondosMasivo.insertarLogContratos");
		List<SolicitudCambio> cambiosFiltrados = solicitudesCambios.stream()
				.filter(s -> (!s.ValorAntiguo.equals(s.ValorNuevo) && !"FechaDeclaracion".equals(s.CampoModificado))
						|| ("FechaDeclaracion".equals(s.CampoModificado)
							&& s.ValorAntiguo.length() >= 10 && s.ValorNuevo.length() >= 10
							&& !s.ValorAntiguo.substring(0, 10).equals(s.ValorNuevo.substring(0, 10))))
				.collect(Collectors.toList());
		if (cambiosFiltrados.isEmpty()) return;
		List<Long> idSecuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos", (long) cambiosFiltrados.size());
		List<EntidadLogGenerarContrato> params = new ArrayList<>();
		String fechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
		for (int i = 0; i < cambiosFiltrados.size(); i++) {
			SolicitudCambio s = cambiosFiltrados.get(i);
			EntidadLogGenerarContrato lc = new EntidadLogGenerarContrato();
			lc.Idlog = idSecuencias.get(i).longValue();
			lc.CodigoFormulario = "DECLARACION_ORIGEN_FONDOS_";
			lc.CampoBackend = s.CampoModificado;
			lc.IdEmpresa = entrada.p_IdEmpresa;
			lc.IdCliente = s.IdEntidadOrigenLong;
			lc.IdPersona = 0L;
			lc.IdUsuario = entrada.p_IdUsuario;
			lc.FechaSistema = fechaSistema;
			lc.IdPrograma = 0L;
			lc.Estado = "PEN";
			lc.Mensaje = "Cliente - ConfirmarModificarDeclaracionOrigenFondosMasivo.java";
			params.add(lc);
		}
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_LOG_CONTRATOS, params, EntidadLogGenerarContrato.class);
		logger.debug("FIN::ConfirmarModificarDeclaracionOrigenFondosMasivo.insertarLogContratos");
	}

	private List<SolicitudCambio> obtenerSolicitudesDeCambios(Connection con, EntradaConfirmarModificarDeclaracionOrigenFondosMasivo entrada) throws Exception {
	List<SolicitudCambio> todasLasSolicitudes = new ArrayList<>();
	ConsultarRegistroSolicitudCambio consultarSolicitudes = new ConsultarRegistroSolicitudCambio();
	
	// Iterar sobre TODAS las declaraciones de origen fondos
	for (InDeclaracionOrigenFondos declaracion : entrada.p_DeclaracionOrigenFondos) {
		EntradaConsultarRegistroSolicitudCambio entradaConsultarRegistroSolicitud = new EntradaConsultarRegistroSolicitudCambio();
		entradaConsultarRegistroSolicitud.p_IdEntidadOrigen = String.valueOf(declaracion.IdDeclaracionOrigenFondos);
		entradaConsultarRegistroSolicitud.p_IdTipoEntidad = ID_TIPO_ENTIDAD;
		SalidaConsultarRegistroSolicitudCambio salidaConsultarRegistroSolicitud = consultarSolicitudes.logica(con, entradaConsultarRegistroSolicitud);
		
		todasLasSolicitudes.addAll(salidaConsultarRegistroSolicitud.P_OUT_SOLICITUDES_CAMBIO);
	}

	return todasLasSolicitudes;
}

	@Override
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::ConfirmarModificarDeclaracionOrigenFondosMasivo.autorizarEvento");
		SalidaAutorizacion salidaAutoriza = new SalidaAutorizacion();
		EntradaConfirmarModificarDeclaracionOrigenFondosMasivo entradaConfirmar = new EntradaConfirmarModificarDeclaracionOrigenFondosMasivo();

		for (Evento evento : entrada.p_Lista) {
			entradaConfirmar.p_IdEmpresa            = evento.IdEmpresa;
			entradaConfirmar.p_IdModulo             = evento.IdModulo;
			entradaConfirmar.p_IdUsuario            = evento.IdUsuario;
			entradaConfirmar.p_UsaReglasNegocio     = USA_REGLAS_NEGOCIO;
			entradaConfirmar.p_IdEstadoAutorizacion = "AUT";
			entradaConfirmar.p_DeclaracionOrigenFondos.add(
					new InDeclaracionOrigenFondos(Long.parseLong(evento.NumeroOperacionConcepto)));
		}

		try {
			SalidaConfirmarModificarDeclaracionOrigenFondosMasivo salidaConfirmacion = logica(conn, entradaConfirmar);

			for (Evento evento : entrada.p_Lista) {
				if (salidaConfirmacion.P_OUT_ERRORESCONTADOR == 0) {
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, "OPERACION_AUTORIZADA").msg();
					salidaAutoriza.P_OUT_ERRORESCONTADOR = 0L;
					salidaAutoriza.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "OPERACION_AUTORIZADA", descripcion));
					ResultadoAutorizacion res = new ResultadoAutorizacion();
					res.evento    = evento.IdEvento;
					res.resultado = EXITO_AUTORIZACION;
					salidaAutoriza.P_OUT_RESULTADO.add(res);
				} else {
					salidaAutoriza.P_OUT_ERRORESCONTADOR = salidaConfirmacion.P_OUT_ERRORESCONTADOR;
					salidaAutoriza.P_OUT_MENSAJES        = salidaConfirmacion.P_OUT_MENSAJES;
				}
			}
		} catch (Exception e) {
			logger.error("Error en autorizarEvento ConfirmarModificarDeclaracionOrigenFondosMasivo", e);
			salidaAutoriza.P_OUT_ERRORESCONTADOR = 1L;
			salidaAutoriza.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AUTORIZACION", e.getMessage()));
		}
		logger.info("TERMINO::ConfirmarModificarDeclaracionOrigenFondosMasivo.autorizarEvento");
		return salidaAutoriza;
	}

	@Override
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
		logger.info("INICIO::ConfirmarModificarDeclaracionOrigenFondosMasivo.rechazarEvento");
		SalidaAutorizacion salidaAutorizacion = new SalidaAutorizacion();
		SQL sql = new SQL();

		try {
			// Recopilar todas las solicitudes por cada DOF del evento
			List<SolicitudCambio> solicitudesCambios = new ArrayList<>();
			ConsultarRegistroSolicitudCambio consultarSolicitudes = new ConsultarRegistroSolicitudCambio();
			for (Evento evento : entrada.p_Lista) {
				EntradaConsultarRegistroSolicitudCambio entradaConsultar = new EntradaConsultarRegistroSolicitudCambio();
				entradaConsultar.p_IdEntidadOrigen = evento.NumeroOperacionConcepto;
				entradaConsultar.p_IdTipoEntidad   = ID_TIPO_ENTIDAD;
				SalidaConsultarRegistroSolicitudCambio salidaConsultar = consultarSolicitudes.logica(conn, entradaConsultar);
				solicitudesCambios.addAll(salidaConsultar.P_OUT_SOLICITUDES_CAMBIO);
			}

			// Solo MODDOF requiere solicitud previa
			String idConcepto = entrada.p_Lista.stream().map(e -> e.IdConcepto).findFirst().orElse("MODDOF");
			if (solicitudesCambios.isEmpty() && "MODDOF".equals(idConcepto)) {
				salidaAutorizacion.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
						"SOLICITUD_NO_EXISTE", null));
				salidaAutorizacion.P_OUT_ERRORESCONTADOR = 1L;
				return salidaAutorizacion;
			}

			// MODDOF: actualizar estado registro solicitud a RZD
			if ("MODDOF".equals(idConcepto) && !solicitudesCambios.isEmpty()) {
				ModificarEstadoRegistroSolicitudCambioMasivo modificarEstado = new ModificarEstadoRegistroSolicitudCambioMasivo();
				EntradaModificarEstado entradaModificar = new EntradaModificarEstado();
				entradaModificar.p_IdEmpresa            = entrada.p_Lista.get(0).IdEmpresa;
				entradaModificar.p_IdUsuario            = entrada.p_Lista.get(0).IdUsuario;
				entradaModificar.p_IdModulo             = entrada.p_Lista.get(0).IdModulo;
				entradaModificar.p_IdEstadoAutorizacion = ID_ESTADO_RECHAZADO;
				solicitudesCambios.stream().map(s -> s.IdEntidadOrigenLong).distinct().forEach(idEntidad -> {
					ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen eo =
							new ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen();
					eo.IdEntidadOrigen = String.valueOf(idEntidad);
					eo.IdTipoEntidad   = ID_TIPO_ENTIDAD;
					entradaModificar.p_EntidadOrigen.add(eo);
				});
				SalidaModificarEstado salidaMod = modificarEstado.logica(conn, entradaModificar);
				if (salidaMod.p_out_ErroresContador > 0) {
					salidaAutorizacion.P_OUT_ERRORESCONTADOR = salidaMod.p_out_ErroresContador;
					for (ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : salidaMod.p_out_ErroresDetalle) {
						salidaAutorizacion.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, err.Codigo, err.Descripcion));
					}
					return salidaAutorizacion;
				}
			}

			// AgregarLog y retornar resultado por evento
			if (!solicitudesCambios.isEmpty()) {
				agregarLogRechazo(conn, entrada, solicitudesCambios);
			}

			HashMap<Long, String> hashSalida = new HashMap<>();
			for (Evento evento : entrada.p_Lista) {
				hashSalida.put(Long.parseLong(evento.NumeroOperacionConcepto), MensajeSalida.MENSAJE_OK);
			}
			for (Evento evento : entrada.p_Lista) {
				long id = Long.parseLong(evento.NumeroOperacionConcepto);
				if (hashSalida.containsKey(id)) {
					String descripcion = Diccionario.getMensaje(conn, entrada.p_IdEmpresa, "OPERACION_RECHAZADA").msg();
					salidaAutorizacion.P_OUT_ERRORESCONTADOR = 0L;
					salidaAutorizacion.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "OPERACION_RECHAZADA", descripcion));
					ResultadoAutorizacion res = new ResultadoAutorizacion();
					res.evento    = evento.IdEvento;
					res.resultado = EXITO_AUTORIZACION;
					salidaAutorizacion.P_OUT_RESULTADO.add(res);
				}
			}
		} catch (Exception e) {
			logger.error("Error en rechazarEvento ConfirmarModificarDeclaracionOrigenFondosMasivo", e);
			salidaAutorizacion.P_OUT_ERRORESCONTADOR = 1L;
			salidaAutorizacion.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_AUTORIZACION", e.getMessage()));
		}
		logger.info("TERMINO::ConfirmarModificarDeclaracionOrigenFondosMasivo.rechazarEvento");
		return salidaAutorizacion;
	}

	private void agregarLogRechazo(Connection con, EntradaAutorizacion entrada, List<SolicitudCambio> solicitudCambio) throws Exception {
		logger.debug("INICIO::ConfirmarModificarDeclaracionOrigenFondosMasivo.agregarLogRechazo");
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		Long idEmpresa = entrada.p_Lista.get(0).IdEmpresa;
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		entradaAgregarLog.p_Log = solicitudCambio.parallelStream().map(datoGeneral -> {
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog     = datoGeneral.IdEntidadOrigenLong;
			log.IdTipoLog         = "ANULACION";
			log.IdModulo          = entrada.p_Lista.get(0).IdModulo;
			log.IdUsuario         = entrada.p_Lista.get(0).IdUsuario;
			log.IdEmpresa         = idEmpresa;
			log.FechaSistemaLog   = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_Lista.get(0).IdUsuario;
			log.IdRegistro        = datoGeneral.IdEntidadOrigenLong.toString();
			log.IdCliente         = datoGeneral.IdEntidadOrigenLong;
			log.IdTipoRegistro    = ID_TIPO_ENTIDAD;
			log.EstadoRegistro    = ID_ESTADO_RECHAZADO;
			log.IdProceso         = 0L;
			log.EstadoProceso     = ID_ESTADO_RECHAZADO;
			log.NombreProceso     = "Rechazar Solicitud Cambio Declaracion Origen Fondos";
			return log;
		}).collect(Collectors.toList());

		entradaAgregarLog.p_LogDetalle = solicitudCambio.parallelStream().map(solicitud -> {
			AgregarLog.InAgregarLog.DetalleLog detalleLog = new AgregarLog.InAgregarLog.DetalleLog();
			detalleLog.IdTrabajo     = 0L;
			detalleLog.IdCabeceraLog = solicitud.IdEntidadOrigenLong;
			detalleLog.SubRegistro   = solicitud.IdTipoEntidad;
			detalleLog.CampoRegistro = solicitud.CampoModificado;
			detalleLog.ValorAntiguo  = solicitud.ValorAntiguo;
			detalleLog.ValorNuevo    = solicitud.ValorNuevo;
			return detalleLog;
		}).collect(Collectors.toList());

		new AgregarLog().logica(con, entradaAgregarLog);
		logger.debug("FIN::ConfirmarModificarDeclaracionOrigenFondosMasivo.agregarLogRechazo");
	}
}
