package cl.bcs.plataforma.pla;

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

import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarRegistroSolicitudCambioMasivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConsultarRegistroSolicitudCambioMasivo.class);
	
	private String SQL_INSERT_CONSULTA_SOLICITUD_CAMBIO = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarRegistroSolicitudCambioMasivo_InsertConsultaSolicitudCambio.sql");
	private String SQL_SELECT_SOLICITUD_CAMBIO = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarRegistroSolicitudCambioMasivo_SelectSolicitudCambio.sql");
	private String SQL_DELETE_CONSULTA_SOLICITUD_CAMBIO = loadResourceToString("cl/bcs/plataforma/pla/sql/ConsultarRegistroSolicitudCambioMasivo_DeleteConsultaSolicitudCambio.sql");
	
	public static class EntradaConsultarRegistroSolicitudCambioMasivo extends BcsPojo {
		public List<EntradaSolicitudCambio> p_SolicitudesCambio;
		
		public EntradaConsultarRegistroSolicitudCambioMasivo() {
			this.p_SolicitudesCambio = new ArrayList<>();
		}
	}
	
	public static class EntradaSolicitudCambio extends BcsPojo {
		public Long IdSolicitudCambio;
		public String IdEntidadOrigen;
		public String IdTipoEntidad; 
	}
	
	public static class SolicitudCambioConsulta extends EntradaSolicitudCambio {
		public Long IdTrabajo;
	}
	
	public static class SalidaConsultarRegistroSolicitudCambioMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
	    public List<SolicitudCambio> P_OUT_SOLICITUDES_CAMBIO;
	    
	    public SalidaConsultarRegistroSolicitudCambioMasivo(){
	    	this.P_OUT_ERRORESCONTADOR = 0L;
	    	this.P_OUT_MENSAJES = new ArrayList<>();
	    	this.P_OUT_SOLICITUDES_CAMBIO = new ArrayList<>();
	    }
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConsultarRegistroSolicitudCambioMasivo jin = gson.fromJson(in.toString(), EntradaConsultarRegistroSolicitudCambioMasivo.class);
		SalidaConsultarRegistroSolicitudCambioMasivo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	public SalidaConsultarRegistroSolicitudCambioMasivo logica(Connection con, EntradaConsultarRegistroSolicitudCambioMasivo entrada) throws Exception {
		logger.info("INICIO::ConsultarRegistroSolicitudCambioMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaConsultarRegistroSolicitudCambioMasivo out = new SalidaConsultarRegistroSolicitudCambioMasivo();
		Long idTrabajo = obtenerNumeroTrabajo();
		
		try {
			insertarTablaTemporal(con, entrada.p_SolicitudesCambio, idTrabajo);
			out.P_OUT_SOLICITUDES_CAMBIO = obtenerSolicitudesCambio(con, idTrabajo);
			out.P_OUT_MENSAJES = obtenerMensajesError(entrada.p_SolicitudesCambio, out.P_OUT_SOLICITUDES_CAMBIO);
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			
		}catch(Exception e) {
			logger.error("ERROR::ConsultarRegistroSolicitudCambioMasivo", e);
			throw e;
		}finally {
			eliminarTablaTemporal(con, idTrabajo);
		}
		
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConsultarRegistroSolicitudCambioMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private Long obtenerNumeroTrabajo() {
		logger.trace("INICIO::ConsultarRegistroSolicitudCambioMasivo.obtenerNumeroTrabajo");
		long inicio = System.currentTimeMillis();
		Long idTrabajo = Double.valueOf(Math.random()*10000000000L).longValue();
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarRegistroSolicitudCambioMasivo.obtenerNumeroTrabajo", fin - inicio);
		return idTrabajo;
	}
	
	private void insertarTablaTemporal(Connection con, List<EntradaSolicitudCambio> entradaSolicitudesCambio, Long idTrabajo) throws Exception {
		logger.debug("INICIO::ConsultarRegistroSolicitudCambioMasivo.insertarTablaTemporal");
		long inicio = System.currentTimeMillis();
		List<SolicitudCambioConsulta> solicitudesCambioConsulta = entradaSolicitudesCambio
				.parallelStream()
				.map(solicitud->{
					SolicitudCambioConsulta solicitudCambioConsulta = new SolicitudCambioConsulta();
					solicitudCambioConsulta.IdTrabajo = idTrabajo;
					solicitudCambioConsulta.IdSolicitudCambio = solicitud.IdSolicitudCambio;
					solicitudCambioConsulta.IdEntidadOrigen = solicitud.IdEntidadOrigen;
					solicitudCambioConsulta.IdTipoEntidad = solicitud.IdTipoEntidad;
					return solicitudCambioConsulta;
				})
				.collect(Collectors.toList());
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_CONSULTA_SOLICITUD_CAMBIO, solicitudesCambioConsulta, SolicitudCambioConsulta.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarRegistroSolicitudCambioMasivo.insertarTablaTemporal", fin - inicio);
	}
	
	private List<SolicitudCambio> obtenerSolicitudesCambio(Connection con, Long idTrabajo) throws Exception {
		logger.debug("INICIO::ConsultarRegistroSolicitudCambioMasivo.obtenerSolicitudesCambio");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdTrabajo", idTrabajo);
		
		SQL sql = new SQL();
		List<SolicitudCambio> solicitudesCambio = sql.query(con, SQL_SELECT_SOLICITUD_CAMBIO, parametros, SolicitudCambio.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarRegistroSolicitudCambioMasivo.obtenerSolicitudesCambio", fin - inicio);
		return solicitudesCambio;
	}
	
	private List<MensajeSalida> obtenerMensajesError(List<EntradaSolicitudCambio> solicitudesEntrada, List<SolicitudCambio> solicitudesCambio){
		logger.trace("INICIO::ConsultarRegistroSolicitudCambioMasivo.obtenerSolicitudesCambio");
		long inicio = System.currentTimeMillis();
		
		List<MensajeSalida> mensajes = new ArrayList<>();
		for(EntradaSolicitudCambio solicitudEntrada : solicitudesEntrada) {
			List<SolicitudCambio> solicitudesCambioEncontradas = solicitudesCambio
					.parallelStream()
					.filter(solicitud->solicitud.IdEntidadOrigenLong.equals(Long.valueOf(solicitudEntrada.IdEntidadOrigen)))
					.collect(Collectors.toList());
			if(solicitudesCambioEncontradas.size() == 0) {
				mensajes.add(new MensajeSalida(Long.valueOf(solicitudEntrada.IdEntidadOrigen),MensajeSalida.MENSAJE_ERROR,"UI_PROCESO_EJECUTADO_ERROR", "UI_ERROR_CONSULTA_SOLICITUD_CAMBIO"));
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarRegistroSolicitudCambioMasivo.obtenerSolicitudesCambio", fin - inicio);
		return mensajes;
	}
	
	private void eliminarTablaTemporal(Connection con, Long idTrabajo)  throws Exception {
		logger.debug("INICIO::ConsultarRegistroSolicitudCambioMasivo.insertarTablaTemporal");
		long inicio = System.currentTimeMillis();
		
		SolicitudCambioConsulta solicitudCambioConsulta = new SolicitudCambioConsulta();
		solicitudCambioConsulta.IdTrabajo = idTrabajo;
		List<SolicitudCambioConsulta> solicitudesCambioConsulta = Arrays.asList(solicitudCambioConsulta);
		
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_CONSULTA_SOLICITUD_CAMBIO, solicitudesCambioConsulta, SolicitudCambioConsulta.class);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarRegistroSolicitudCambioMasivo.insertarTablaTemporal", fin - inicio);
	}

}
