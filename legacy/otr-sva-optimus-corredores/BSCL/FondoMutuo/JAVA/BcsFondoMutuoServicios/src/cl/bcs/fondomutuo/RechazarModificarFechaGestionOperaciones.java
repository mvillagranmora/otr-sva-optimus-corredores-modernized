package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambioMasivo.EntradaConsultarRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambioMasivo.SalidaConsultarRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class RechazarModificarFechaGestionOperaciones implements JavaCall{


	static final Logger logger = LogManager.getLogger(RechazarModificarFechaGestionOperaciones.class);

	private final String ID_TIPO_ENTIDAD = "SOLICITUD-APORTE-RESCATE";
	
	public static class EntradaRechazaFechaGestionOperacionesMasivo extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<InFechaGestionOperacionesPorRechazar> p_SolicitudAporteRescate;
		public String p_IdEstadoAutorizacion;
		public EntradaRechazaFechaGestionOperacionesMasivo() {
			this.p_SolicitudAporteRescate = new ArrayList<>();
		}
		
	}

	public static class InFechaGestionOperacionesPorRechazar extends BcsPojo{
		public Long 	IdSolicitudAporteRescate;
		
		public InFechaGestionOperacionesPorRechazar(Long idSolicitudAporteRescate) {
			IdSolicitudAporteRescate = idSolicitudAporteRescate;
		}
	}
	
	public static class SalidaRechazaModificarFechaGestionOperacionesMasivo extends BcsPojo{
		public Long					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;
		
		public SalidaRechazaModificarFechaGestionOperacionesMasivo() {
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
	}
	
	public static class SolicitudAporteRescateRechazar extends BcsPojo{
		public Long 	IdSolicitudAporteRescate;
		public String 	IdEstadoAutorizacion;
	}


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaRechazaFechaGestionOperacionesMasivo jin = gson.fromJson(in.toString(), EntradaRechazaFechaGestionOperacionesMasivo.class);
		SalidaRechazaModificarFechaGestionOperacionesMasivo out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public SalidaRechazaModificarFechaGestionOperacionesMasivo logica(Connection con, EntradaRechazaFechaGestionOperacionesMasivo entrada) throws Exception {
		logger.info("INICIO::RechazaModificarFechaGestionOperacionesMasivo");
		long inicioServicio = System.currentTimeMillis();
		SalidaRechazaModificarFechaGestionOperacionesMasivo out = new SalidaRechazaModificarFechaGestionOperacionesMasivo();
		
		if (entrada.p_SolicitudAporteRescate.isEmpty()){		
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_SOLICITUD_APORTE_RESCATE",null,con,entrada.p_IdEmpresa));
			return out;
		}
	
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios(con, entrada);
		if (solicitudesCambios.isEmpty()){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		out = modificarEstadoRegistroSolicitudCambio(con, entrada, solicitudesCambios);
		
		agregarLog(con, entrada, solicitudesCambios);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::RechazaModificarFechaGestionOperacionesMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<SolicitudCambio> obtenerSolicitudesDeCambios(Connection con, EntradaRechazaFechaGestionOperacionesMasivo entrada) throws Exception {
    	logger.debug("INICIO::RechazaModificarFechaGestionOperacionesMasivo.obtenerSolicitudesDeCambios");
    	long inicio = System.currentTimeMillis();
    	
    	EntradaConsultarRegistroSolicitudCambioMasivo entradaConsultarRegistroSolicitudCambioMasivo = new EntradaConsultarRegistroSolicitudCambioMasivo();
		
		for (InFechaGestionOperacionesPorRechazar operacion : entrada.p_SolicitudAporteRescate) {
			ConsultarRegistroSolicitudCambioMasivo.EntradaSolicitudCambio entradaSolicitudCambio = new ConsultarRegistroSolicitudCambioMasivo.EntradaSolicitudCambio();
			entradaSolicitudCambio.IdEntidadOrigen = operacion.IdSolicitudAporteRescate.toString();
			entradaSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
			entradaConsultarRegistroSolicitudCambioMasivo.p_SolicitudesCambio.add(entradaSolicitudCambio);
		}
		
		ConsultarRegistroSolicitudCambioMasivo consultarRegistroSolicitudCambioMasivo = new ConsultarRegistroSolicitudCambioMasivo();
		SalidaConsultarRegistroSolicitudCambioMasivo salidaConsultarRegistroSolicitudCambioMasivo = consultarRegistroSolicitudCambioMasivo.logica(con, entradaConsultarRegistroSolicitudCambioMasivo);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazaModificarFechaGestionOperacionesMasivo.obtenerSolicitudesDeCambios", fin - inicio);
		return salidaConsultarRegistroSolicitudCambioMasivo.P_OUT_SOLICITUDES_CAMBIO;
    }
	
	 private SalidaRechazaModificarFechaGestionOperacionesMasivo modificarEstadoRegistroSolicitudCambio(Connection con, EntradaRechazaFechaGestionOperacionesMasivo entrada, List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.debug("INICIO::RechazaModificarFechaGestionOperacionesMasivo.modificarEstadoRegistroSolicitudCambio");
		long inicio = System.currentTimeMillis();
		
		SalidaRechazaModificarFechaGestionOperacionesMasivo out = new SalidaRechazaModificarFechaGestionOperacionesMasivo();
		ModificarEstadoRegistroSolicitudCambioMasivo modificarEstadoRegistroSolicitudCambioMasivo = new ModificarEstadoRegistroSolicitudCambioMasivo();
		ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado entradaModificarEstadoRegistroSolicitudCambioMasivo = new ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado();
		entradaModificarEstadoRegistroSolicitudCambioMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarEstadoRegistroSolicitudCambioMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarEstadoRegistroSolicitudCambioMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaModificarEstadoRegistroSolicitudCambioMasivo.p_IdEstadoAutorizacion = entrada.p_IdEstadoAutorizacion;
		
		for (SolicitudCambio sc : solicitudesCambios) {
			ModificarEstadoRegistroSolicitudCambioMasivo.SolicitudCambio solicitud = new ModificarEstadoRegistroSolicitudCambioMasivo.SolicitudCambio();
			solicitud.IdSolicitudCambio = sc.IdSolicitudCambio;
			entradaModificarEstadoRegistroSolicitudCambioMasivo.p_SolicitudCambio.add(solicitud);
		}
		
		ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado salidaModificarEstadoRegistroSolicitudCambioMasivo = modificarEstadoRegistroSolicitudCambioMasivo.logica(con, entradaModificarEstadoRegistroSolicitudCambioMasivo);
		out.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambioMasivo.p_out_ErroresContador;
		
		for (ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : salidaModificarEstadoRegistroSolicitudCambioMasivo.p_out_ErroresDetalle) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, err.Codigo, err.Descripcion));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazaModificarFechaGestionOperacionesMasivo.modificarEstadoRegistroSolicitudCambio", fin - inicio);
		return out;
    }
	 
	private void agregarLog(Connection con, EntradaRechazaFechaGestionOperacionesMasivo entrada, List<SolicitudCambio> solicitudCambio) throws Exception {
		logger.debug("INICIO::RechazaModificarFechaGestionOperacionesMasivo.agregarLog");
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
			log.NombreProceso = "Rechazar Solicitud Cambio de Fecha Gestion Operaciones";
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
		logger.debug("FIN::RechazaModificarFechaGestionOperacionesMasivo.agregarLog", fin - inicio);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{}";
			EntradaRechazaFechaGestionOperacionesMasivo Objin = gson.fromJson(json, EntradaRechazaFechaGestionOperacionesMasivo.class);
			
			RechazarModificarFechaGestionOperaciones demo = new RechazarModificarFechaGestionOperaciones();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaRechazaModificarFechaGestionOperacionesMasivo ObjOut = demo.logica(con, Objin);
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

