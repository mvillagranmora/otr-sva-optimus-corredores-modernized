package cl.bcs.socionegocio.per;

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
import cl.bcs.socionegocio.per.ConfirmarCrearPersonaTarjetaBancaria.EntradaConfirmarCrearPersonaTarjetaBancaria;
import cl.bcs.socionegocio.per.ConfirmarCrearPersonaTarjetaBancaria.SalidaConfirmarCrearPersonaTarjetaBancaria;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearPersonaTarjetaBancaria implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearPersonaTarjetaBancaria.class);
	
	private final String SQL_INSERT_TARJETA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaTarjetaBancaria_InsertTarjeraBancaria.sql");
	private final String SQL_SELECT_DATOS_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaTarjetaBancaria_SelectDatosPersona.sql");
	private final String SQL_INSERT_LOG_GENERAR_CONTRATOS = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaTarjetaBancaria_InsertLogGenerarContratos.sql");
	
	private final String ID_CONCEPTO = "NUETJB";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	
	public static class EntradaCrearPersonaTarjetaBancaria extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdBanco;
		public String p_IdTipoTarjetaBancaria;
		public String p_NumeroTarjetaBancaria;
		public Long p_IdPersona;
		public String p_FechaVencimiento;
		public Long p_CodigoVerificacion;
		public String p_IdEstado;
	}
	
	public static class SalidaCrearPersonaTarjetaBancaria extends BcsPojo{
		public Long P_OUT_REFID;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCrearPersonaTarjetaBancaria() {
			this.P_OUT_REFID = 0L;
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class TarjetaBancaria extends BcsPojo{
		public Long IdTarjetaBancaria;
		public Long IdBanco;
		public String IdTipoTarjetaBancaria;
		public String NumeroTarjetaBancaria;
		public Long IdPersona;
		public java.sql.Date FechaVencimiento;
		public Long CodigoVerificacion;
		public java.sql.Timestamp FechaIngreso;
		public String IdEstado;
		public String IdEstadoAutorizacion;
		public Long IdUsuarioCreador;
	}
	
	public static class Persona extends BcsPojo{
		public String DocumentoIdentidad;
		public String NombreCompleto;
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

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearPersonaTarjetaBancaria jin = gson.fromJson(in.toString(), EntradaCrearPersonaTarjetaBancaria.class);
		SalidaCrearPersonaTarjetaBancaria out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearPersonaTarjetaBancaria logica(Connection con, EntradaCrearPersonaTarjetaBancaria entrada) throws Exception {
		logger.info("INICIO::CrearPersonaTarjetaBancaria");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearPersonaTarjetaBancaria out = new SalidaCrearPersonaTarjetaBancaria();
		
		Long idTarjetaBancaria = insertarTarjetaBancaria(con, entrada);
		
		String requiereAutorizacion = obtenerRequiereAutorizacion(con, entrada.p_IdEmpresa);
		
		// Replica la condición del SP: v_RequiereAutorizacion ='S' and p_UsaReglasNegocio != 'N'
		boolean solicitaAutorizacion = "S".equals(requiereAutorizacion) && !"N".equals(entrada.p_UsaReglasNegocio);
		
		if(solicitaAutorizacion) {
			out = crearEventoAutorizacion(con, entrada, idTarjetaBancaria);
		} else {
			out = confirmarPersonaTarjetaBancaria(con, entrada, idTarjetaBancaria);
		}
		
		out.P_OUT_REFID = idTarjetaBancaria;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearPersonaTarjetaBancaria", finServicio - inicioServicio);
		return out;
	}
	
	private Long insertarTarjetaBancaria(Connection con, EntradaCrearPersonaTarjetaBancaria entrada) throws Exception {
		logger.debug("INICIO::CrearPersonaTarjetaBancaria.insertarTarjetaBancaria");
		long inicio = System.currentTimeMillis();
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
		
		List<Long> secuenciasTarjetasBancarias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::TarjetaBancaria", 1L);
		Long idTarjetaBancaria = secuenciasTarjetasBancarias.get(0);
		
		TarjetaBancaria tarjetaBancaria = new TarjetaBancaria();
		tarjetaBancaria.IdTarjetaBancaria = idTarjetaBancaria;
		tarjetaBancaria.IdBanco = entrada.p_IdBanco;
		tarjetaBancaria.IdTipoTarjetaBancaria = entrada.p_IdTipoTarjetaBancaria;
		tarjetaBancaria.NumeroTarjetaBancaria = entrada.p_NumeroTarjetaBancaria;
		tarjetaBancaria.IdPersona = entrada.p_IdPersona;
		// Extraer solo la fecha si viene con timestamp (2026-05-06T04:00:00.000Z -> 2026-05-06)
		String fechaVencimiento = entrada.p_FechaVencimiento;
		if (fechaVencimiento != null && fechaVencimiento.length() > 10) {
			fechaVencimiento = fechaVencimiento.substring(0, 10);
		}
		tarjetaBancaria.FechaVencimiento = Fecha.toDate(fechaVencimiento);
		tarjetaBancaria.CodigoVerificacion = entrada.p_CodigoVerificacion;
		tarjetaBancaria.FechaIngreso = Fecha.toDateTime(fechaHoraSistema);
		tarjetaBancaria.IdEstado = entrada.p_IdEstado;
		tarjetaBancaria.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
		tarjetaBancaria.IdUsuarioCreador = entrada.p_IdUsuario;
		
		List<TarjetaBancaria> tarjetasBancarias = Arrays.asList(tarjetaBancaria);
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_TARJETA_BANCARIA, tarjetasBancarias, TarjetaBancaria.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaTarjetaBancaria.insertarTarjetaBancaria", fin - inicio);
		return idTarjetaBancaria;
	}
	
	private String obtenerRequiereAutorizacion(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::CrearPersonaTarjetaBancaria.obtenerRequiereAutorizacion");
		long inicio = System.currentTimeMillis();
		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, idEmpresa, ID_CONCEPTO);
		if(requiereAutorizacion == null) requiereAutorizacion = "N";
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaTarjetaBancaria.obtenerRequiereAutorizacion", fin - inicio);
		return requiereAutorizacion;
	}
	
	private SalidaCrearPersonaTarjetaBancaria crearEventoAutorizacion(Connection con, EntradaCrearPersonaTarjetaBancaria entrada, Long idTarjetaBancaria) throws Exception {
		logger.debug("INICIO::CrearPersonaTarjetaBancaria.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		Persona datosPersona = obtenerDatosPersona(con, entrada.p_IdPersona);
		SalidaCrearPersonaTarjetaBancaria salidaCrearPersonaTarjetaBancaria = new SalidaCrearPersonaTarjetaBancaria();
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
		evento.NumeroOperacionConcepto = String.valueOf(idTarjetaBancaria);
		evento.FechaOperacionConcepto = fechaSistema;
		evento.IdCliente = entrada.p_IdPersona;
		evento.Glosa = datosPersona.DocumentoIdentidad + "/" + datosPersona.NombreCompleto + " / NroTarjeta: " + entrada.p_NumeroTarjetaBancaria;
		
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = Arrays.asList(evento);
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaCrearPersonaTarjetaBancaria.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaCrearPersonaTarjetaBancaria.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaCrearPersonaTarjetaBancaria.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaCrearPersonaTarjetaBancaria.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaCrearPersonaTarjetaBancaria.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaTarjetaBancaria.crearEventoAutorizacion", fin - inicio);
		return salidaCrearPersonaTarjetaBancaria;	
	}
	
	private Persona obtenerDatosPersona(Connection con, Long idPersona) throws Exception {
		logger.debug("INICIO::CrearPersonaTarjetaBancaria.obtenerDatosPersona");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdPersona", idPersona);
		SQL sql = new SQL();
		List<Persona> personas = sql.query(con, SQL_SELECT_DATOS_PERSONA, parametros, Persona.class);
		Persona persona = personas.parallelStream().findFirst().orElse(new Persona());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaTarjetaBancaria.obtenerDatosPersona", fin - inicio);
		return persona;
	}
	
	 private SalidaCrearPersonaTarjetaBancaria confirmarPersonaTarjetaBancaria(Connection con, EntradaCrearPersonaTarjetaBancaria entrada, Long idTarjetaBancaria) throws Exception {
		logger.debug("INICIO::CrearPersonaTarjetaBancaria.confirmarPersonaTarjetaBancaria");
		long inicio = System.currentTimeMillis();
		EntradaConfirmarCrearPersonaTarjetaBancaria entradaConfirmarCrearPersonaTarjetaBancaria = new EntradaConfirmarCrearPersonaTarjetaBancaria();
		entradaConfirmarCrearPersonaTarjetaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfirmarCrearPersonaTarjetaBancaria.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfirmarCrearPersonaTarjetaBancaria.p_IdModulo = entrada.p_IdModulo;
		entradaConfirmarCrearPersonaTarjetaBancaria.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConfirmarCrearPersonaTarjetaBancaria.p_IdTarjetaBancaria = idTarjetaBancaria;
		entradaConfirmarCrearPersonaTarjetaBancaria.p_IdEstadoAutorizacion = "ING";
		
		ConfirmarCrearPersonaTarjetaBancaria confirmarCrearPersonaTarjetaBancaria = new ConfirmarCrearPersonaTarjetaBancaria();
		SalidaConfirmarCrearPersonaTarjetaBancaria salidaConfirmarCrearPersonaTarjetaBancaria = confirmarCrearPersonaTarjetaBancaria.logica(con, entradaConfirmarCrearPersonaTarjetaBancaria);
		
		// Insertar en LogGenerarContratos (replica funcionalidad del SP líneas 249-252)
		insertarLogGenerarContratos(con, entrada, idTarjetaBancaria);
		
		SalidaCrearPersonaTarjetaBancaria out = new SalidaCrearPersonaTarjetaBancaria();
		out.P_OUT_ERRORESCONTADOR = salidaConfirmarCrearPersonaTarjetaBancaria.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaConfirmarCrearPersonaTarjetaBancaria.P_OUT_MENSAJES;
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaTarjetaBancaria.confirmarPersonaTarjetaBancaria", fin - inicio);
		return out;
	 }
	 
	 private void insertarLogGenerarContratos(Connection con, EntradaCrearPersonaTarjetaBancaria entrada, Long idTarjetaBancaria) throws Exception {
		logger.debug("INICIO::CrearPersonaTarjetaBancaria.insertarLogGenerarContratos");
		long inicio = System.currentTimeMillis();
		
		String fechaSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
		String fechaSistemaStr = fechaSistema.substring(0, 10);
		
		List<Long> secuencia = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos", 1L);
		
		LogGenerarContratos log = new LogGenerarContratos();
		log.Idlog = secuencia.get(0);
		log.CodigoFormulario = "TARJETA_BANCARIA_";
		log.CampoBackend = "Creado";
		log.IdEmpresa = entrada.p_IdEmpresa;
		log.IdCliente = 0L;
		log.IdPersona = entrada.p_IdPersona;
		log.IdUsuario = entrada.p_IdUsuario;
		log.FechaSistema = fechaSistemaStr;
		log.IdPrograma = 0L;
		log.Estado = "PEN";
		log.Mensaje = "Persona - CrearPersonaTarjetaBancaria.sql";
		
		List<LogGenerarContratos> logs = Arrays.asList(log);
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_LOG_GENERAR_CONTRATOS, logs, LogGenerarContratos.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaTarjetaBancaria.insertarLogGenerarContratos", fin - inicio);
	 }
}
