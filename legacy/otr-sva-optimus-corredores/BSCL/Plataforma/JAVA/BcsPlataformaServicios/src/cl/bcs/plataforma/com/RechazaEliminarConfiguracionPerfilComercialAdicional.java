package cl.bcs.plataforma.com;

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
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class RechazaEliminarConfiguracionPerfilComercialAdicional implements JavaCall {

    static final Logger logger = LogManager.getLogger(RechazaEliminarConfiguracionPerfilComercialAdicional.class);
    
    private final String ID_TIPO_ENTIDAD = "CLIENTE";

    public static class EntradaRechazaEliminarConfiguracionPerfilComercialAdicional extends BcsPojo {
    	
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<InPerfilComercialPorRechazar> p_IdsPerfilComercialAdicional;
		public String p_IdEstadoAutorizacion;

		public EntradaRechazaEliminarConfiguracionPerfilComercialAdicional() {
		    this.p_IdsPerfilComercialAdicional = new ArrayList<>();
		}

    }

    public static class InPerfilComercialPorRechazar extends BcsPojo {
		
    	public Long IdPerfilComercialAdicional;
		public InPerfilComercialPorRechazar(Long idPerfilComercialAdicional) {
			IdPerfilComercialAdicional = idPerfilComercialAdicional;
		}
    }

    public static class SalidaRechazaEliminarConfiguracionPerfilComercialAdicional extends BcsPojo {
		
    	public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<SolicitudCambio> P_OUT_SOLICITUDES_CAMBIOS;
	
		public SalidaRechazaEliminarConfiguracionPerfilComercialAdicional() {
		    this.P_OUT_ERRORESCONTADOR = 0L;
		    this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		    this.P_OUT_SOLICITUDES_CAMBIOS = new ArrayList<SolicitudCambio>();
		}
		
    }

    @Override
    public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaRechazaEliminarConfiguracionPerfilComercialAdicional jin = gson.fromJson(in.toString(), EntradaRechazaEliminarConfiguracionPerfilComercialAdicional.class);
		SalidaRechazaEliminarConfiguracionPerfilComercialAdicional out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
    };

    public SalidaRechazaEliminarConfiguracionPerfilComercialAdicional logica(Connection con, EntradaRechazaEliminarConfiguracionPerfilComercialAdicional entrada) throws Exception {
	
    	logger.info("INICIO::RechazaEliminarConfiguracionPerfilComercialAdicional");
		long inicioServicio = System.currentTimeMillis();
		SalidaRechazaEliminarConfiguracionPerfilComercialAdicional out = new SalidaRechazaEliminarConfiguracionPerfilComercialAdicional();
	
		if (entrada.p_IdsPerfilComercialAdicional.isEmpty()) {
		    out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_ESPECIFICAR_PERFIL_COMERCIAL_ADICIONAL", null, con, entrada.p_IdEmpresa));
		    return out;
		}
		
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios(con, entrada);
		if (solicitudesCambios.isEmpty()) {
		    out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "SOLICITUD_NO_EXISTE", null, con, entrada.p_IdEmpresa));
		    return out;
		}

		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambio(con, entrada, solicitudesCambios);
		out.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambio.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaModificarEstadoRegistroSolicitudCambio.P_OUT_MENSAJES);
	
		agregarLog(con, entrada, solicitudesCambios);
	
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::RechazaEliminarConfiguracionPerfilComercialAdicional", finServicio - inicioServicio);
		return out;
		
    }


    private SalidaModificarEstadoRegistroSolicitudCambio modificarEstadoRegistroSolicitudCambio(Connection con, EntradaRechazaEliminarConfiguracionPerfilComercialAdicional entrada, List<SolicitudCambio> solicitudesCambios) throws Exception {
		
    	logger.debug("INICIO::RechazaEliminarConfiguracionPerfilComercialAdicional.modificarEstadoRegistroSolicitudCambio");
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
		logger.debug("FIN::RechazaEliminarConfiguracionPerfilComercialAdicional.modificarEstadoRegistroSolicitudCambio", fin - inicio);
		return salidaModificarEstadoRegistroSolicitudCambio;
		
    }

    private List<SolicitudCambio> obtenerSolicitudesDeCambios(Connection con, EntradaRechazaEliminarConfiguracionPerfilComercialAdicional entrada) throws Exception {
    	
    	logger.debug("INICIO::RechazaEliminarConfiguracionPerfilComercialAdicional.obtenerSolicitudesDeCambios");
    	long inicio = System.currentTimeMillis();
		Long idCliente = entrada.p_IdsPerfilComercialAdicional.stream().findFirst().get().IdPerfilComercialAdicional;
		
		ConsultarRegistroSolicitudCambio consultarSolicitudes = new ConsultarRegistroSolicitudCambio();
		EntradaConsultarRegistroSolicitudCambio entradaConsultarRegistroSolicitud = new EntradaConsultarRegistroSolicitudCambio();
		entradaConsultarRegistroSolicitud.p_IdEntidadOrigen = idCliente.toString();
		entradaConsultarRegistroSolicitud.p_IdTipoEntidad = ID_TIPO_ENTIDAD;
		SalidaConsultarRegistroSolicitudCambio salidaConsultarRegistroSolicitud = consultarSolicitudes.logica(con, entradaConsultarRegistroSolicitud);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RechazaEliminarConfiguracionPerfilComercialAdicional.obtenerSolicitudesDeCambios", fin - inicio);
		return salidaConsultarRegistroSolicitud.P_OUT_SOLICITUDES_CAMBIO;
		
    }

    private void agregarLog(Connection con, EntradaRechazaEliminarConfiguracionPerfilComercialAdicional entrada, List<SolicitudCambio> solicitudCambio) throws Exception {
		
    	logger.debug("INICIO::RechazaEliminarConfiguracionPerfilComercialAdicional.agregarLog");
		long inicio = System.currentTimeMillis();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	
		entradaAgregarLog.p_Log = solicitudCambio.parallelStream().map(datoGeneral -> {
		    AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		    log.IdCabeceraLog = datoGeneral.IdEntidadOrigenLong;
		    log.IdTipoLog = "ANULACION";
		    log.IdModulo = entrada.p_IdModulo;
		    log.IdUsuario = entrada.p_IdUsuario;
		    log.IdEmpresa = entrada.p_IdEmpresa;
		    log.FechaSistemaLog = fechaSistema;
		    log.IdUsuarioAtendido = entrada.p_IdUsuario;
		    log.IdRegistro = datoGeneral.IdEntidadOrigenLong.toString();
		    log.IdCliente = datoGeneral.IdEntidadOrigenLong;
		    log.IdTipoRegistro = ID_TIPO_ENTIDAD;
		    log.EstadoRegistro = entrada.p_IdEstadoAutorizacion;
		    log.IdProceso = 0L;
		    log.EstadoProceso = entrada.p_IdEstadoAutorizacion;
		    log.NombreProceso = "Rechazar Solicitud Eliminar Configuracion de Matriz Enrolamiento";
		    return log;
		}).collect(Collectors.toList());
	
		entradaAgregarLog.p_LogDetalle = solicitudCambio.parallelStream().map(solicitud -> {
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
		logger.debug("FIN::RechazaEliminarConfiguracionPerfilComercialAdicional.agregarLog", fin - inicio);
		
    }

    public static void main(String[] args) throws Exception {
	System.out.println("== TEST ==");

	try (Connection con = PlataformaUtils.getDESAConnection()) {
	    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
	    String json = "{}";
	    EntradaRechazaEliminarConfiguracionPerfilComercialAdicional Objin = gson.fromJson(json, EntradaRechazaEliminarConfiguracionPerfilComercialAdicional.class);

	    RechazaEliminarConfiguracionPerfilComercialAdicional demo = new RechazaEliminarConfiguracionPerfilComercialAdicional();
	    System.out.println("ENTRADA");
	    System.out.println(Objin.toString());
	    System.out.println("SALIDA");

	    try {
		SalidaRechazaEliminarConfiguracionPerfilComercialAdicional ObjOut = demo.logica(con, Objin);
		con.rollback();
		System.out.println(ObjOut.toString());
	    } catch (Exception er) {
		er.printStackTrace();
	    } finally {
		con.rollback();
		con.close();
	    }
	}
	System.out.println("== END ==");
    }
}
