package cl.bcs.socionegocio.per;

import java.lang.reflect.Field;
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
import cl.bcs.socionegocio.per.pojo.Fatca;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarModificarPersonaFatcaMasivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConfirmarModificarPersonaFatcaMasivo.class);
	
	private String SQL_UPDATE_FATCA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaFatcaMasivo_UpdateFatca.sql");
	private String SQL_SELECT_CLASIFICACIONFATCA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaFatcaMasivo_SelectClasificacionFatca.sql");
	
	private final String ID_TIPO_ENTIDAD 		= "FATCA";
	
	public static class EntradaConfirmarModificarPersonaFatcaMasivo extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<InPersonaFatca> p_PersonaFatca;
		public String p_IdEstadoAutorizacion;
		public EntradaConfirmarModificarPersonaFatcaMasivo() {
			this.p_PersonaFatca = new ArrayList<InPersonaFatca>();
		}
	}

	public static class InPersonaFatca extends BcsPojo{
		public Long IdPersona;
		
		public InPersonaFatca(Long idPersona) {
			IdPersona = idPersona;
		}
		
	}
	
	public static class FatcaBD{
		public String IdClasificacionFatca;
		public String Nombre;
	}
	
	public static class SalidaConfirmarModificarPersonaFatcaMasivo extends BcsPojo{
		public Long					P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;
		
		public SalidaConfirmarModificarPersonaFatcaMasivo() {
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();
		}
	}

	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarModificarPersonaFatcaMasivo jin = gson.fromJson(in.toString(), EntradaConfirmarModificarPersonaFatcaMasivo.class);
		SalidaConfirmarModificarPersonaFatcaMasivo out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConfirmarModificarPersonaFatcaMasivo logica(Connection con, EntradaConfirmarModificarPersonaFatcaMasivo entrada) throws Exception {
		logger.info("INICIO::ConfirmarModificarPersonaFatcaMasivo");
		long inicioServicio = System.currentTimeMillis();
		SalidaConfirmarModificarPersonaFatcaMasivo salida = new SalidaConfirmarModificarPersonaFatcaMasivo();		
		
		if (entrada.p_PersonaFatca.isEmpty()){		
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_PERSONA",null,con,entrada.p_IdEmpresa));
			return salida;
		}
		
		List<SolicitudCambio> solicitudesCambios = obtenerSolicitudesDeCambios(con, entrada);	
		if (solicitudesCambios.isEmpty()){
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return salida;
		}

		List<Fatca> fatca = obtenerEntidadesFatca(solicitudesCambios);
		
		modificarCampoFatca(con, fatca);		

		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambio(con, entrada, solicitudesCambios);
		salida.P_OUT_ERRORESCONTADOR += salidaModificarEstadoRegistroSolicitudCambio.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES.addAll(salidaModificarEstadoRegistroSolicitudCambio.P_OUT_MENSAJES);
		
		agregarLog(con, entrada, solicitudesCambios);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarPersonaFatcaMasivo", finServicio - inicioServicio);
		return salida;
	}
	
	private List<SolicitudCambio> obtenerSolicitudesDeCambios(Connection con, EntradaConfirmarModificarPersonaFatcaMasivo entrada) throws Exception {
		Long idPersona = entrada.p_PersonaFatca.stream().findFirst().get().IdPersona;

		ConsultarRegistroSolicitudCambio consultarSolicitudes = new ConsultarRegistroSolicitudCambio();
		EntradaConsultarRegistroSolicitudCambio entradaConsultarRegistroSolicitud = new EntradaConsultarRegistroSolicitudCambio();
		entradaConsultarRegistroSolicitud.p_IdEntidadOrigen = idPersona.toString();
		entradaConsultarRegistroSolicitud.p_IdTipoEntidad = ID_TIPO_ENTIDAD;
		SalidaConsultarRegistroSolicitudCambio salidaConsultarRegistroSolicitud = consultarSolicitudes.logica(con, entradaConsultarRegistroSolicitud);

		return salidaConsultarRegistroSolicitud.P_OUT_SOLICITUDES_CAMBIO;
	}
	
	private SalidaModificarEstadoRegistroSolicitudCambio modificarEstadoRegistroSolicitudCambio(Connection con, EntradaConfirmarModificarPersonaFatcaMasivo entrada, List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaFatcaMasivo.modificarEstadoRegistroSolicitudCambio");
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
		logger.debug("FIN::ConfirmarModificarPersonaFatcaMasivo.modificarEstadoRegistroSolicitudCambio", fin - inicio);
		return salidaModificarEstadoRegistroSolicitudCambio;
    }
	
	private List<Fatca> obtenerEntidadesFatca(List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.trace("INICIO::ConfirmarModificarPersonaFatcaMasivo.obtenerEntidadesFatca");
		long inicio = System.currentTimeMillis();
		
		Map<Long, List<SolicitudCambio>> solicitudesPorIdEntidad = solicitudesCambios.parallelStream().collect(Collectors.groupingBy(solicitudCambio->solicitudCambio.IdEntidadOrigenLong));
		
		List<Fatca> datosAActualizar = new ArrayList<>();
		Fatca fatca = new Fatca();
		for(Entry<Long, List<SolicitudCambio>> persona : solicitudesPorIdEntidad.entrySet()) {
			List<SolicitudCambio> cambiosPersona = persona.getValue();
			fatca.IdPersona = cambiosPersona.parallelStream().findFirst().get().IdEntidadOrigenLong;
			for(SolicitudCambio solicitudCambio : cambiosPersona) {
				Field campo = fatca.getClass().getField(solicitudCambio.CampoModificado);
				if(campo.getType().isAssignableFrom(String.class)) {
					campo.set(fatca, solicitudCambio.ValorNuevo);
				}
				if(campo.getType().isAssignableFrom(Long.class)) {
					campo.set(fatca, Long.parseLong(solicitudCambio.ValorNuevo));
				}
				if(campo.getType().isAssignableFrom(java.sql.Timestamp.class)) {
					campo.set(fatca, Fecha.toDateTime(solicitudCambio.ValorNuevo));
				}
				if(campo.getType().isAssignableFrom(java.sql.Date.class)) {
					campo.set(fatca,  Fecha.toDate(solicitudCambio.ValorNuevo));
				}
			}
			datosAActualizar.add(fatca);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConfirmarModificarPersonaFatcaMasivo.obtenerEntidadesFatca", fin - inicio);
		return datosAActualizar;
	}
	
	private void modificarCampoFatca(Connection con, List<Fatca> fatca) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaFatcaMasivo.modificarFatca");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		
		sql.batch(con, SQL_UPDATE_FATCA, fatca, Fatca.class);			
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaFatcaMasivo.modificarFatca", fin - inicio);
	}
	
	private HashMap<String, Object> obtenerClasificacionFatca(Connection con, SQL sql, List<String> idsTipoFatca){
		logger.debug("INICIO::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerClasificacionFatca");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> hashLista = new HashMap<String, Object>();
		if(!(idsTipoFatca.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdClasificacionFatca", idsTipoFatca);
			try {
				List<FatcaBD> listado = sql.query(con, SQL_SELECT_CLASIFICACIONFATCA, params, FatcaBD.class);
				if(!(listado.isEmpty())) {
					hashLista = Utils.List2Hash(listado, "IdClasificacionFatca");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaDatosGeneralesMasivo.obtenerClasificacionFatca", fin - inicio);
		
		return hashLista;
	}
	
	
	private void agregarLog(Connection con, EntradaConfirmarModificarPersonaFatcaMasivo entrada, List<SolicitudCambio> solicitudCambio) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaFatcaMasivo.agregarLog");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		AgregarLog.OutAgregarLog salida = new AgregarLog.OutAgregarLog();
		
		AgregarLog agregarLog = new AgregarLog();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaAgregarLog.p_IdUsuario = entrada.p_IdUsuario;
		entradaAgregarLog.p_IdModulo = entrada.p_IdModulo;
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		List<String> idClasificacionFatca = new ArrayList<String>();

		for(SolicitudCambio solicitud : solicitudCambio) {
			if(solicitud.CampoModificado!=null) {
				if(solicitud.CampoModificado.compareToIgnoreCase("IdClasificacionFatca") == 0){
					if(solicitud.ValorAntiguo!=null && !(idClasificacionFatca.contains(solicitud.ValorAntiguo))) idClasificacionFatca.add(solicitud.ValorAntiguo);
					if(solicitud.ValorNuevo!=null && !(idClasificacionFatca.contains(solicitud.ValorNuevo))) idClasificacionFatca.add(solicitud.ValorNuevo);
				}
			}
		}
		
		HashMap hClasificacionFatca = new HashMap<String, Object>();
		try {
			hClasificacionFatca = obtenerClasificacionFatca(con, sql, idClasificacionFatca);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		List<Long> IdCabecera = new ArrayList<Long>();
		for(SolicitudCambio solicitud : solicitudCambio) {
			//cabecera LOG
			if(solicitud.IdEntidadOrigenLong!=null && !(solicitud.IdEntidadOrigenLong.compareTo(0L)==0) && !(IdCabecera.contains(solicitud.IdEntidadOrigenLong))){
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				log.IdCabeceraLog = solicitud.IdEntidadOrigenLong;
				log.IdTipoLog = "MODIFICACION";
				log.IdModulo = entrada.p_IdModulo;
				log.IdUsuario = entrada.p_IdUsuario;
				log.IdEmpresa = entrada.p_IdEmpresa;
				log.FechaSistemaLog = fechaSistema;
				log.IdUsuarioAtendido = entrada.p_IdUsuario;
				log.IdRegistro = solicitud.IdEntidadOrigenLong.toString();
				log.IdTipoRegistro = ID_TIPO_ENTIDAD;
				log.EstadoRegistro = entrada.p_IdEstadoAutorizacion;
				log.IdProceso = 0L;
				log.EstadoProceso = entrada.p_IdEstadoAutorizacion;
				log.NombreProceso = "Confirmar Solicitud Cambio Fatca Persona";
				log.IdPersona = solicitud.IdEntidadOrigenLong;
				IdCabecera.add(solicitud.IdEntidadOrigenLong);
				entradaAgregarLog.p_Log.add(log);
			}
			//detalle LOG
			if(solicitud.CampoModificado!=null) {
				AgregarLog.InAgregarLog.DetalleLog detalleLog = new AgregarLog.InAgregarLog.DetalleLog();
				detalleLog.IdTrabajo = 0L; 
				detalleLog.IdCabeceraLog = solicitud.IdEntidadOrigenLong;			
				detalleLog.SubRegistro = solicitud.IdTipoEntidad;
				detalleLog.CampoRegistro = solicitud.CampoModificado;
				detalleLog.ValorAntiguo = "";
				if(solicitud.ValorAntiguo!=null) {
					if(solicitud.CampoModificado.compareToIgnoreCase("IdClasificacionFatca") == 0) {
						FatcaBD fatcabd = (FatcaBD) hClasificacionFatca.get(solicitud.ValorAntiguo);
						if(fatcabd!=null) {
							detalleLog.ValorAntiguo = (fatcabd.Nombre!=null)? fatcabd.Nombre: "";
						}
					}else{
						detalleLog.ValorAntiguo = solicitud.ValorAntiguo;
					}
				}
				detalleLog.ValorNuevo = "";
				if(solicitud.ValorNuevo!=null) {
					if(solicitud.CampoModificado.compareToIgnoreCase("IdClasificacionFatca") == 0){
						FatcaBD fatcabd = (FatcaBD) hClasificacionFatca.get(solicitud.ValorNuevo);
						if(fatcabd!=null) {
							detalleLog.ValorNuevo = (fatcabd.Nombre!=null)? fatcabd.Nombre: "";
						}
					}
					else{
						detalleLog.ValorNuevo = solicitud.ValorNuevo;
					}
				}
				entradaAgregarLog.p_LogDetalle.add(detalleLog);
			}
		}

		salida = agregarLog.logica(con, entradaAgregarLog);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaFatcaMasivo.agregarLog", fin - inicio);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{p_IdEmpresa:1,p_IdUsuario:21,p_IdModulo:\"SOC\",p_UsaReglasNegocio: \"N\",p_PersonaFatca: [{IdPersona:1369675}],p_IdEstadoAutorizacion: \"AUT\"}";
			EntradaConfirmarModificarPersonaFatcaMasivo Objin = gson.fromJson(json, EntradaConfirmarModificarPersonaFatcaMasivo.class);
			
			ConfirmarModificarPersonaFatcaMasivo demo = new ConfirmarModificarPersonaFatcaMasivo();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaConfirmarModificarPersonaFatcaMasivo ObjOut = demo.logica(con, Objin);
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
