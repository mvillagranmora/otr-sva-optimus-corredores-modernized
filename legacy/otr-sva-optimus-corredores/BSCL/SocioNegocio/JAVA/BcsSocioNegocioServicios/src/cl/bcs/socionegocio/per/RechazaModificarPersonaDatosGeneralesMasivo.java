package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambio.EntradaConsultarRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambio.SalidaConsultarRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.EntradaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.RechazaModificarClienteDatosGeneralesMasivo.EntradaRechazaModificarClienteDatosGeneralesMasivo;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RechazaModificarPersonaDatosGeneralesMasivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(RechazaModificarPersonaDatosGeneralesMasivo.class);
	
	private String SQL_SELECT_SOLICITUDES_CAMBIO = loadResourceToString("cl/bcs/socionegocio/per/sql/RechazaAutorizacionModificarPersonaDatosGeneralesMasivo_SelectSolicitudesCambio.sql");
	private String SQL_UPDATE_ESTADO_SOLICITUD_CAMBIO = loadResourceToString("cl/bcs/socionegocio/per/sql/RechazaAutorizacionModificarPersonaDatosGeneralesMasivo_UpdateSolicitudesCambio.sql");

	private final String ID_TIPO_ENTIDAD = "PERSONA";
	
	public static class EntradaRechazaModificarPersonaDatosGeneralesMasivo extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<InPersonaDatosGeneralesPorRechazar> p_Persona;
		public String p_IdEstadoAutorizacion;
		public EntradaRechazaModificarPersonaDatosGeneralesMasivo() {
			this.p_Persona = new ArrayList<>();
		}
		
	}

	public static class InPersonaDatosGeneralesPorRechazar extends BcsPojo{
		public Long 	IdPersona;
		
		public InPersonaDatosGeneralesPorRechazar(Long idPersona) {
			IdPersona = idPersona;
		}
	}
	
	public static class SalidaRechazaModificarPersonaDatosGeneralesMasivo extends BcsPojo{
		public Long					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;
		
		public SalidaRechazaModificarPersonaDatosGeneralesMasivo() {
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
	}
	
//	public  static class SolicitudCambio extends BcsPojo
//	{
//	   public Long 		IdSolicitudCambio;
//	   public String 	CampoModificado;
//	   public String 	ValorAntiguo;
//	   public String 	ValorNuevo;
//	   public String 	IdEntidadOrigen;
//	   public Long 		IdPersona;	   
//	   public String 	IdTipoEntidad;    // PERSONA	   
//	   public String 	IdEstadoAutorizacion = "";
//	}  
	
	public static class PersonaDatosGeneralesRechazar extends BcsPojo{
		public Long 	IdPersona;
		public String 	IdEstadoAutorizacion;
	}
	
	public static class PersonaFatcaPaisResidenciaActualizar extends BcsPojo{
		public Long 	IdPersona;
		public String 	IdPaisResidenciaAntiguo;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaRechazaModificarPersonaDatosGeneralesMasivo jin = gson.fromJson(in.toString(), EntradaRechazaModificarPersonaDatosGeneralesMasivo.class);
		SalidaRechazaModificarPersonaDatosGeneralesMasivo out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public SalidaRechazaModificarPersonaDatosGeneralesMasivo logica(Connection con, EntradaRechazaModificarPersonaDatosGeneralesMasivo entrada) throws Exception {
		logger.info("INICIO::RechazaModificarPersonaDatosGeneralesMasivo");
		long inicioServicio = System.currentTimeMillis();
		SalidaRechazaModificarPersonaDatosGeneralesMasivo out = new SalidaRechazaModificarPersonaDatosGeneralesMasivo();
		
		if (entrada.p_Persona.isEmpty()){		
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_PERSONA",null,con,entrada.p_IdEmpresa));
			return out;
		}
		/*
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdEntidadOrigen", entrada.p_Persona.stream().findFirst().get().IdPersona);
		params.put("IdTipoEntidad", ID_TIPO_ENTIDAD);
		
		SQL sql = new SQL();
		List<SolicitudCambio> solicitudesCambios = sql.query(con, SQL_SELECT_SOLICITUDES_CAMBIO, params, SolicitudCambio.class);*/
		
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios(con, entrada);
		if (solicitudesCambios.isEmpty()){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambio(con, entrada, solicitudesCambios);
		out.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambio.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaModificarEstadoRegistroSolicitudCambio.P_OUT_MENSAJES);
		
		/*List<SolicitudCambio> paramsUpdateEstadoSolicitud = new ArrayList<>();
		SolicitudCambio objUpdateEstadoSolicitud = new SolicitudCambio();
		objUpdateEstadoSolicitud.IdEstadoAutorizacion = entrada.p_IdEstadoAutorizacion;
		objUpdateEstadoSolicitud.IdSolicitudCambio = solicitudesCambios.stream().findFirst().get().IdSolicitudCambio;
		paramsUpdateEstadoSolicitud.add(objUpdateEstadoSolicitud);
		sql.batch(con, SQL_UPDATE_ESTADO_SOLICITUD_CAMBIO, paramsUpdateEstadoSolicitud, SolicitudCambio.class);*/
		
		agregarLog(con, entrada, solicitudesCambios);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::RechazaModificarPersonaDatosGeneralesMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<SolicitudCambio> obtenerSolicitudesDeCambios(Connection con, EntradaRechazaModificarPersonaDatosGeneralesMasivo entrada) throws Exception {
    	logger.debug("INICIO::RechazaModificarPersonaDatosGeneralesMasivo.obtenerSolicitudesDeCambios");
    	long inicio = System.currentTimeMillis();
		Long idPersona = entrada.p_Persona.stream().findFirst().get().IdPersona;
		
		ConsultarRegistroSolicitudCambio consultarSolicitudes = new ConsultarRegistroSolicitudCambio();
		EntradaConsultarRegistroSolicitudCambio entradaConsultarRegistroSolicitud = new EntradaConsultarRegistroSolicitudCambio();
		entradaConsultarRegistroSolicitud.p_IdEntidadOrigen = idPersona.toString();
		entradaConsultarRegistroSolicitud.p_IdTipoEntidad = ID_TIPO_ENTIDAD;
		SalidaConsultarRegistroSolicitudCambio salidaConsultarRegistroSolicitud = consultarSolicitudes.logica(con, entradaConsultarRegistroSolicitud);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazaModificarPersonaDatosGeneralesMasivo.obtenerSolicitudesDeCambios", fin - inicio);
		return salidaConsultarRegistroSolicitud.P_OUT_SOLICITUDES_CAMBIO;
    }
	
	 private SalidaModificarEstadoRegistroSolicitudCambio modificarEstadoRegistroSolicitudCambio(Connection con, EntradaRechazaModificarPersonaDatosGeneralesMasivo entrada, List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.debug("INICIO::RechazaModificarPersonaDatosGeneralesMasivo.modificarEstadoRegistroSolicitudCambio");
		long inicio = System.currentTimeMillis();
		
		ModificarEstadoRegistroSolicitudCambio modificarEstadoRegistroSolicitudCambio = new ModificarEstadoRegistroSolicitudCambio();
		EntradaModificarEstadoRegistroSolicitudCambio entradaModificarEstadoRegistroSolicitudCambio = new EntradaModificarEstadoRegistroSolicitudCambio();
		entradaModificarEstadoRegistroSolicitudCambio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdModulo = entrada.p_IdModulo;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdSolicitudCambio = solicitudesCambios.stream().findFirst().get().IdSolicitudCambio;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdEstadoAutorizacion = entrada.p_IdEstadoAutorizacion;
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambio.logica(con, entradaModificarEstadoRegistroSolicitudCambio);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazaModificarPersonaDatosGeneralesMasivo.modificarEstadoRegistroSolicitudCambio", fin - inicio);
		return salidaModificarEstadoRegistroSolicitudCambio;
    }
	 
	private void agregarLog(Connection con, EntradaRechazaModificarPersonaDatosGeneralesMasivo entrada, List<SolicitudCambio> solicitudCambio) throws Exception {
		logger.debug("INICIO::RechazaModificarPersonaDatosGeneralesMasivo.agregarLog");
		long inicio = System.currentTimeMillis();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		entradaAgregarLog.p_Log = solicitudCambio.parallelStream().map(datoGeneral->{
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = datoGeneral.IdEntidadOrigenLong;
			log.IdTipoLog = "ANULACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdPersona = datoGeneral.IdEntidadOrigenLong;
			log.IdRegistro = datoGeneral.IdEntidadOrigenLong.toString();
			log.IdTipoRegistro = ID_TIPO_ENTIDAD;
			log.EstadoRegistro = entrada.p_IdEstadoAutorizacion;
			log.IdProceso = 0L;
			log.EstadoProceso = entrada.p_IdEstadoAutorizacion;
			log.NombreProceso = "Rechazar Solicitud Cambio Datos Generales";
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
		logger.debug("FIN::RechazaModificarPersonaDatosGeneralesMasivo.agregarLog", fin - inicio);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{}";
			EntradaRechazaModificarPersonaDatosGeneralesMasivo Objin = gson.fromJson(json, EntradaRechazaModificarPersonaDatosGeneralesMasivo.class);
			
			RechazaModificarPersonaDatosGeneralesMasivo demo = new RechazaModificarPersonaDatosGeneralesMasivo();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaRechazaModificarPersonaDatosGeneralesMasivo ObjOut = demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}	
