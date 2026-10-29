package cl.bcs.socionegocio.cli;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.ConfirmarCrearDeclaracionOrigenFondos.EntradaConfirmarCrearDeclaracionOrigenFondos;
import cl.bcs.socionegocio.cli.ConfirmarCrearDeclaracionOrigenFondos.SalidaConfirmarCrearDeclaracionOrigenFondos;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearDeclaracionOrigenFondos implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearDeclaracionOrigenFondos.class);
	
	private final String SQL_SELECT_VALIDAR_DECLARACION_ORIGEN_FONDOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearDeclaracionOrigenFondos_SelectValidacionDeclaracionOrigenFondos.sql");
	private final String SQL_INSERT_DECLARACION_ORIGEN_FONDOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearDeclaracionOrigenFondos_InsertDeclaracionOrigenFondos.sql");
	private final String SQL_INSERT_DETALLE_DECLARACION_ORIGEN_FONDOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearDeclaracionOrigenFondos_InsertDetalleDeclaracionOrigenFondos.sql");
	private final String SQL_SELECT_DATOS_CLIENTE = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearDeclaracionOrigenFondos_SelectDatosCliente.sql");
	private final String SQL_INSERT_LOG_GENERAR_CONTRATOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/CrearDeclaracionOrigenFondos_InsertLogGenerarContratos.sql");
	
	private final String ID_CONCEPTO = "NUEDOF";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	
	public static class EntradaCrearDeclaracionOrigenFondos extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdCliente;
		public String p_IdTipoOperacionOrigenFondos;
		public String p_FechaDeclaracion;
		public String p_Comentario;
		public String p_IdEstado;
		public String p_OperacionInscripcion;
		public List<Detalle> p_Detalle;
		
		public EntradaCrearDeclaracionOrigenFondos() {
			this.p_Detalle = new ArrayList<Detalle>();
		}
	}
	
	public static class Detalle extends BcsPojo{
		public String IdTipoOrigenFondo;
		public BigDecimal Valor;
		public String Otros;
	}
	
	public static class DeclaracionOrigenFondos extends BcsPojo{
		public Long IdDeclaracionOrigenFondos;
		public Long IdCliente;
		public String IdTipoOperacionOrigenFondos;
		public java.sql.Timestamp FechaDeclaracion;
		public String Comentario;
		public Long IdUsuarioCreador;
		public java.sql.Timestamp FechaIngreso;
		public Long IdEmpresa;
		public String IdEstado;
		public String IdEstadoAutorizacion;
		public String OperacionInscripcion;
	}
	
	public static class DetalleDeclaracionOrigenFondos extends BcsPojo{
		public String IdTipoOrigenFondo;
		public Long IdDeclaracionOrigenFondos;
		public BigDecimal Valor;
		public Long IdUsuarioCreador;
		public java.sql.Timestamp FechaIngreso;
		public String IdEstado;
		public String Otros;
	}
	
	public static class Validacion extends BcsPojo{
		public Boolean EsError;
		public MensajeSalida MensajeError;
		
		public Validacion() {
			this.EsError = false;
		}
	}
	
	public static class Cliente extends BcsPojo{
		public String NombreCompleto;
		public String DocumentoIdentidad;
	}
	
	public static class LogGenerarContratos extends BcsPojo{
		public Long Idlog;
		public String CodigoFormulario;
		public String CampoBackend;
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdPersona;
		public Long IdUsuario;
		public String FechaSistema;
		public Long IdPrograma;
		public String Estado;
		public String Mensaje;
	}
	
	public static class SalidaCrearDeclaracionOrigenFondos extends BcsPojo{
		public Long P_OUT_REFID;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCrearDeclaracionOrigenFondos() {
			this.P_OUT_REFID = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearDeclaracionOrigenFondos jin = gson.fromJson(in.toString(), EntradaCrearDeclaracionOrigenFondos.class);
		SalidaCrearDeclaracionOrigenFondos out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearDeclaracionOrigenFondos logica(Connection con, EntradaCrearDeclaracionOrigenFondos entrada) throws Exception {
		logger.info("INICIO::CrearDeclaracionOrigenFondos");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearDeclaracionOrigenFondos out = new SalidaCrearDeclaracionOrigenFondos();
		
		Validacion validarDeclaracionOrigenFondos = validarDeclaracionOrigenFondos(con, entrada);
		out = agregarValidacionSalida(out, validarDeclaracionOrigenFondos);
		if(out.P_OUT_ERRORESCONTADOR > 0) return out;
		
		Long idDeclaracionOrigenFondos = insertarDeclaracionOrigenFondos(con, entrada);
		insertDetalleDeclaracionOrigenFondos(con, entrada, idDeclaracionOrigenFondos);
		String requiereAutorizacion = obtenerRequiereAutorizacion(con, entrada.p_IdEmpresa);
		
		if("S".equals(requiereAutorizacion)) {
			out = crearEventoAutorizacion(con, entrada, idDeclaracionOrigenFondos);
		} else {
			out = confirmarCrearDeclaracionOrigenFondos(con, entrada, idDeclaracionOrigenFondos);
		}
		out.P_OUT_REFID = idDeclaracionOrigenFondos;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearDeclaracionOrigenFondos", finServicio - inicioServicio);
		return out;
	}
	
	private Validacion validarDeclaracionOrigenFondos(Connection con, EntradaCrearDeclaracionOrigenFondos entrada) throws Exception {
		logger.debug("INICIO::CrearDeclaracionOrigenFondos.validarDeclaracionOrigenFondos");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(3);
		parametros.put("IdCliente", entrada.p_IdCliente);
		parametros.put("IdTipoOperacionOrigenFondos", entrada.p_IdTipoOperacionOrigenFondos);
		parametros.put("FechaDeclaracion", entrada.p_FechaDeclaracion);
		SQL sql = new SQL();
		Long cantidadDeclaracionesDeFondos = sql.countQuery(con, SQL_SELECT_VALIDAR_DECLARACION_ORIGEN_FONDOS, parametros);
		Validacion validacion = new Validacion();
		if(cantidadDeclaracionesDeFondos > 0) {
			validacion.EsError = true;
			validacion.MensajeError = new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"REGISTRO_EXISTE",new HashMap<String,String>(),con,entrada.p_IdEmpresa);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDeclaracionOrigenFondos.validarDeclaracionOrigenFondos", fin - inicio);
		return validacion;
	}
	
	private SalidaCrearDeclaracionOrigenFondos agregarValidacionSalida(SalidaCrearDeclaracionOrigenFondos salida, Validacion validacion) {
		logger.trace("INICIO::RevisaDatosPortafolio.agregarValidacionSalida");
		if(validacion.EsError) {
			salida.P_OUT_ERRORESCONTADOR += 1L;
			salida.P_OUT_MENSAJES.add(validacion.MensajeError);
		}
		logger.trace("FIN::RevisaDatosPortafolio.agregarValidacionSalida");
		return salida;
	}
	
	private Long insertarDeclaracionOrigenFondos(Connection con, EntradaCrearDeclaracionOrigenFondos entrada) throws Exception {
		logger.debug("INICIO::CrearDeclaracionOrigenFondos.insertarDeclaracionOrigenFondos");
		long inicio = System.currentTimeMillis();
		
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
		
		List<Long> secuenciasDeclaracionOrigenFondos = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::DeclaracionOrigenFondos", 1L);
		Long idDeclaracionOrigenFondos = secuenciasDeclaracionOrigenFondos.get(0);
		
		DeclaracionOrigenFondos declaracionOrigenFondos = new DeclaracionOrigenFondos();
		declaracionOrigenFondos.IdDeclaracionOrigenFondos = idDeclaracionOrigenFondos;
		declaracionOrigenFondos.IdCliente = entrada.p_IdCliente;
		declaracionOrigenFondos.IdTipoOperacionOrigenFondos = entrada.p_IdTipoOperacionOrigenFondos;
		declaracionOrigenFondos.FechaDeclaracion = Fecha.toDateTime(entrada.p_FechaDeclaracion);
		declaracionOrigenFondos.Comentario = entrada.p_Comentario;
		declaracionOrigenFondos.IdUsuarioCreador = entrada.p_IdUsuario;
		declaracionOrigenFondos.FechaIngreso = Fecha.toDateTime(fechaHoraSistema);
		declaracionOrigenFondos.IdEmpresa = entrada.p_IdEmpresa;
		declaracionOrigenFondos.IdEstado = entrada.p_IdEstado;
		declaracionOrigenFondos.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
		declaracionOrigenFondos.OperacionInscripcion = entrada.p_OperacionInscripcion;
		
		List<DeclaracionOrigenFondos> declaracionesOrigenFondos = Arrays.asList(declaracionOrigenFondos);
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_DECLARACION_ORIGEN_FONDOS, declaracionesOrigenFondos, DeclaracionOrigenFondos.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDeclaracionOrigenFondos.insertarDeclaracionOrigenFondos", fin - inicio);
		return idDeclaracionOrigenFondos;
	}
	
	private void insertDetalleDeclaracionOrigenFondos(Connection con, EntradaCrearDeclaracionOrigenFondos entrada, Long idDeclaracionOrigenFondos) throws Exception {
		logger.debug("INICIO::CrearDeclaracionOrigenFondos.insertDetalleDeclaracionOrigenFondos");
		long inicio = System.currentTimeMillis();
		
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
		
		List<DetalleDeclaracionOrigenFondos> detallesDeclaracionOrigenFondos = entrada.p_Detalle.parallelStream().map(detalle->{
			DetalleDeclaracionOrigenFondos detalleDeclaracionOrigenFondos = new DetalleDeclaracionOrigenFondos();
			detalleDeclaracionOrigenFondos.IdTipoOrigenFondo = detalle.IdTipoOrigenFondo;
			detalleDeclaracionOrigenFondos.IdDeclaracionOrigenFondos = idDeclaracionOrigenFondos;
			detalleDeclaracionOrigenFondos.Valor = detalle.Valor;
			detalleDeclaracionOrigenFondos.IdUsuarioCreador = entrada.p_IdUsuario;
			detalleDeclaracionOrigenFondos.FechaIngreso = Fecha.toDateTime(fechaHoraSistema);
			detalleDeclaracionOrigenFondos.IdEstado = "ACT";
			detalleDeclaracionOrigenFondos.Otros = detalle.Otros;
			return detalleDeclaracionOrigenFondos;
		}).collect(Collectors.toList());
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_DETALLE_DECLARACION_ORIGEN_FONDOS, detallesDeclaracionOrigenFondos, DetalleDeclaracionOrigenFondos.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDeclaracionOrigenFondos.insertDetalleDeclaracionOrigenFondos", fin - inicio);
	}
	
	private String obtenerRequiereAutorizacion(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::CrearDeclaracionOrigenFondos.obtenerRequiereAutorizacion");
		long inicio = System.currentTimeMillis();
		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, idEmpresa, ID_CONCEPTO);
		if(requiereAutorizacion == null) requiereAutorizacion = "N";
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDeclaracionOrigenFondos.obtenerRequiereAutorizacion", fin - inicio);
		return requiereAutorizacion;
	}
	
	private SalidaCrearDeclaracionOrigenFondos crearEventoAutorizacion(Connection con, EntradaCrearDeclaracionOrigenFondos entrada, Long idDeclaracionOrigenFondos) throws Exception {
		logger.debug("INICIO::CrearDeclaracionOrigenFondos.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		Cliente datosCliente = obtenerDatosCliente(con, entrada.p_IdCliente);
		SalidaCrearDeclaracionOrigenFondos salidaCrearDeclaracionOrigenFondos = new SalidaCrearDeclaracionOrigenFondos();
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
		evento.IdTipoEvento = ID_CONCEPTO;
		evento.IdModuloSolicita = entrada.p_IdModulo;
		evento.IdUsuarioSolicita = entrada.p_IdUsuario;
		evento.Fecha = fechaSistema;
		evento.IdConcepto = ID_CONCEPTO;
		evento.NumeroOperacionConcepto = String.valueOf(idDeclaracionOrigenFondos);
		evento.FechaOperacionConcepto = fechaSistema;
		evento.IdCliente = entrada.p_IdCliente;
		evento.Glosa = datosCliente.DocumentoIdentidad + "/" + datosCliente.NombreCompleto;
		
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = Arrays.asList(evento);
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaCrearDeclaracionOrigenFondos.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaCrearDeclaracionOrigenFondos.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaCrearDeclaracionOrigenFondos.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaCrearDeclaracionOrigenFondos.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaCrearDeclaracionOrigenFondos.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDeclaracionOrigenFondos.crearEventoAutorizacion", fin - inicio);
		return salidaCrearDeclaracionOrigenFondos;	
	}
	
	private Cliente obtenerDatosCliente(Connection con, Long idCliente) throws Exception {
		logger.debug("INICIO::CrearDeclaracionOrigenFondos.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdCliente", idCliente);
		SQL sql = new SQL();
		List<Cliente> clientes = sql.query(con, SQL_SELECT_DATOS_CLIENTE, parametros, Cliente.class);
		Cliente cliente = clientes.parallelStream().findFirst().orElse(new Cliente());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDeclaracionOrigenFondos.crearEventoAutorizacion", fin - inicio);
		return cliente;
	}
	
	private SalidaCrearDeclaracionOrigenFondos confirmarCrearDeclaracionOrigenFondos(Connection con, EntradaCrearDeclaracionOrigenFondos entrada, Long idDeclaracionOrigenFondos) throws Exception {
		logger.debug("INICIO::CrearDeclaracionOrigenFondos.confirmarCrearDeclaracionOrigenFondos");
		long inicio = System.currentTimeMillis();
		
		// Insertar en LogGenerarContratos ANTES de confirmar (igual que SQL)
		insertarLogGenerarContratos(con, entrada, idDeclaracionOrigenFondos);
		
		EntradaConfirmarCrearDeclaracionOrigenFondos entradaConfirmarCrearDeclaracionOrigenFondos = new EntradaConfirmarCrearDeclaracionOrigenFondos();
		entradaConfirmarCrearDeclaracionOrigenFondos.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfirmarCrearDeclaracionOrigenFondos.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfirmarCrearDeclaracionOrigenFondos.p_IdModulo = entrada.p_IdModulo;
		entradaConfirmarCrearDeclaracionOrigenFondos.p_UsaReglasNegocio = "N";
		entradaConfirmarCrearDeclaracionOrigenFondos.p_IdDeclaracionOrigenFondos = idDeclaracionOrigenFondos;
		entradaConfirmarCrearDeclaracionOrigenFondos.p_IdEstadoAutorizacion = "ING";
		
		ConfirmarCrearDeclaracionOrigenFondos confirmarCrearDeclaracionOrigenFondos = new ConfirmarCrearDeclaracionOrigenFondos();
		SalidaConfirmarCrearDeclaracionOrigenFondos salidaConfirmarCrearDeclaracionOrigenFondos = confirmarCrearDeclaracionOrigenFondos.logica(con, entradaConfirmarCrearDeclaracionOrigenFondos);
		
		SalidaCrearDeclaracionOrigenFondos salidaCrearDeclaracionOrigenFondos = new SalidaCrearDeclaracionOrigenFondos();
		salidaCrearDeclaracionOrigenFondos.P_OUT_ERRORESCONTADOR = salidaConfirmarCrearDeclaracionOrigenFondos.P_OUT_ERRORESCONTADOR;
		salidaCrearDeclaracionOrigenFondos.P_OUT_MENSAJES = salidaConfirmarCrearDeclaracionOrigenFondos.P_OUT_MENSAJES;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDeclaracionOrigenFondos.confirmarCrearDeclaracionOrigenFondos", fin - inicio);
		return salidaCrearDeclaracionOrigenFondos;
	}
	
	private void insertarLogGenerarContratos(Connection con, EntradaCrearDeclaracionOrigenFondos entrada, Long idDeclaracionOrigenFondos) throws Exception {
		logger.debug("INICIO::CrearDeclaracionOrigenFondos.insertarLogGenerarContratos");
		long inicio = System.currentTimeMillis();
		
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
		String fechaSistema = fechaHoraSistema.substring(0, 10);
		
		List<Long> secuenciasLog = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos", 1L);
		Long idLog = secuenciasLog.get(0);
		
		LogGenerarContratos log = new LogGenerarContratos();
		log.Idlog = idLog;
		log.CodigoFormulario = "DECLARACION_ORIGEN_FONDOS_";
		log.CampoBackend = "Creado";
		log.IdEmpresa = entrada.p_IdEmpresa;
		log.IdCliente = entrada.p_IdCliente;
		log.IdPersona = 0L;
		log.IdUsuario = entrada.p_IdUsuario;
		log.FechaSistema = fechaSistema;
		log.IdPrograma = 0L;
		log.Estado = "PEN";
		log.Mensaje = "Cliente - CrearDeclaracionOrigenFondos.sql";
		
		List<LogGenerarContratos> logs = Arrays.asList(log);
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_LOG_GENERAR_CONTRATOS, logs, LogGenerarContratos.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearDeclaracionOrigenFondos.insertarLogGenerarContratos", fin - inicio);
	}

}
