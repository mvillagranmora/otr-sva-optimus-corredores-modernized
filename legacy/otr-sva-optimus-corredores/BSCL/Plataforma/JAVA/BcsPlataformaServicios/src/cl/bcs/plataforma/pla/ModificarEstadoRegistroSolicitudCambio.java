package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarEstadoRegistroSolicitudCambio implements JavaCall{

	static final Logger logger = LogManager.getLogger(ModificarEstadoRegistroSolicitudCambio.class);
	
	private final String SQL_UPDATE_SOLICITUD_CAMBIO_POR_ID = loadResourceToString("cl/bcs/plataforma/pla/sql/ModificarEstadoRegistroSolicitudCambio_UpdateSolicitudCambioPorId.sql");
	private final String SQL_UPDATE_SOLICITUD_CAMBIO_POR_ENTIDAD = loadResourceToString("cl/bcs/plataforma/pla/sql/ModificarEstadoRegistroSolicitudCambio_UpdateSolicitudCambioPorEntidad.sql");
    private final String SQL_SELECT_SOLICITUD_CAMBIO_POR_ID = loadResourceToString("cl/bcs/plataforma/pla/sql/ModificarEstadoRegistroSolicitudCambio_SelectSolicitudCambioPorIdSolicitudCambio.sql");
	private final String SQL_SELECT_SOLICITUD_CAMBIO_POR_ENTIDAD = loadResourceToString("cl/bcs/plataforma/pla/sql/ModificarEstadoRegistroSolicitudCambio_SelectSolicitudCambioPorIdEntidadOrigen.sql");
	
	
	public static class RegistroSolicitudCambio extends BcsPojo{
		public Long IdSolicitudCambio;
		public String IdTipoEntidad;
		public String IdEntidadOrigen;
		public String CampoModificado;
		public String TipoDato;
		public String ValorAntiguo;
		public String ValorNuevo;
		public String IdEstadoAutorizacion;
	}
		


	public static class EntradaModificarEstadoRegistroSolicitudCambio extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdSolicitudCambio;
		public String p_IdEntidadOrigen;
		public String p_IdTipoEntidad;
		public String p_IdEstadoAutorizacion;
		
		public EntradaModificarEstadoRegistroSolicitudCambio() {
			this.p_IdSolicitudCambio = 0L;
			this.p_IdEntidadOrigen = "";
			this.p_IdTipoEntidad = "";
			this.p_IdEstadoAutorizacion = "";
		}
		
	}
	
	public static class SalidaModificarEstadoRegistroSolicitudCambio extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaModificarEstadoRegistroSolicitudCambio() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class SolicitudCambio extends BcsPojo{
		public Long IdSolicitudCambio;
		public String IdEntidadOrigen;
		public String IdTipoEntidad;
		public String IdEstadoAutorizacion;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarEstadoRegistroSolicitudCambio jin = gson.fromJson(in.toString(), EntradaModificarEstadoRegistroSolicitudCambio.class);
		SalidaModificarEstadoRegistroSolicitudCambio out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarEstadoRegistroSolicitudCambio logica(Connection con, EntradaModificarEstadoRegistroSolicitudCambio entrada) throws Exception {
		logger.info("INICIO::ModificarEstadoRegistroSolicitudCambio");
		long inicioServicio = System.currentTimeMillis();
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = new SalidaModificarEstadoRegistroSolicitudCambio();
		if(entrada.p_IdSolicitudCambio > 0) {
		    actualizarSolicitudCambioPorId(con, entrada.p_IdSolicitudCambio, entrada.p_IdEstadoAutorizacion);	
	        salidaModificarEstadoRegistroSolicitudCambio =comprobarUpdateModificacionEstadoPorId(con, entrada.p_IdSolicitudCambio, entrada.p_IdEstadoAutorizacion, entrada.p_IdEmpresa);
		}else {
		    actualizarSolicitudCambioPorEntidad(con, entrada.p_IdEntidadOrigen, entrada.p_IdTipoEntidad, entrada.p_IdEstadoAutorizacion);
            salidaModificarEstadoRegistroSolicitudCambio = comprobarUpdateModificacionEstadoPorEntidad(con, entrada.p_IdEntidadOrigen, entrada.p_IdTipoEntidad, entrada.p_IdEstadoAutorizacion, entrada.p_IdEmpresa);
	 	}
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarEstadoRegistroSolicitudCambio", finServicio - inicioServicio);
		return salidaModificarEstadoRegistroSolicitudCambio;
	}
	
	private void actualizarSolicitudCambioPorId(Connection con, Long idSolicitudCambio, String idEstadoAutorizacion) throws Exception {
		logger.debug("INICIO::ModificarEstadoRegistroSolicitudCambio.actualizarSolicitudCambioPorId");
		long inicio = System.currentTimeMillis();
		if(esIdSolicitudCambioValido(idSolicitudCambio)) {
			SolicitudCambio solicitudCambio = new SolicitudCambio();
			solicitudCambio.IdSolicitudCambio = idSolicitudCambio;
			solicitudCambio.IdEstadoAutorizacion = idEstadoAutorizacion;
			List<SolicitudCambio> solicitudesCambio = new ArrayList<SolicitudCambio>();
			solicitudesCambio.add(solicitudCambio);
			SQL sql = new SQL();
			sql.batch(con, SQL_UPDATE_SOLICITUD_CAMBIO_POR_ID, solicitudesCambio, SolicitudCambio.class);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarEstadoRegistroSolicitudCambio.actualizarSolicitudCambioPorId", fin - inicio);
	}
	
	private void actualizarSolicitudCambioPorEntidad(Connection con, String idEntidadOrigen, String idTipoEntidad, String idEstadoAutorizacion) throws Exception {
		logger.debug("INICIO::ModificarEstadoRegistroSolicitudCambio.actualizarSolicitudCambioPorEntidad");
		long inicio = System.currentTimeMillis();
		if(esEntidadValido(idEntidadOrigen, idTipoEntidad)) {
			SolicitudCambio solicitudCambio = new SolicitudCambio();
			solicitudCambio.IdEntidadOrigen = idEntidadOrigen;
			solicitudCambio.IdTipoEntidad = idTipoEntidad;
			solicitudCambio.IdEstadoAutorizacion = idEstadoAutorizacion;
			List<SolicitudCambio> solicitudesCambio = new ArrayList<SolicitudCambio>();
			solicitudesCambio.add(solicitudCambio);
			SQL sql = new SQL();
			sql.batch(con, SQL_UPDATE_SOLICITUD_CAMBIO_POR_ENTIDAD, solicitudesCambio, SolicitudCambio.class);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarEstadoRegistroSolicitudCambio.actualizarSolicitudCambioPorEntidad", fin - inicio);
	}
	
	private Boolean esIdSolicitudCambioValido(Long idSolicitudCambio) {
		logger.trace("INICIO::ModificarEstadoRegistroSolicitudCambio.esIdSolicitudCambioValido");
		Boolean esIdSolicitudCambioValido = idSolicitudCambio != null && !idSolicitudCambio.equals(0L);
		logger.trace("FIN::ModificarEstadoRegistroSolicitudCambio.esIdSolicitudCambioValido");
		return esIdSolicitudCambioValido;	
	}
	
	private Boolean esEntidadValido(String idEntidadOrigen, String idTipoEntidad) {
		logger.trace("INICIO::ModificarEstadoRegistroSolicitudCambio.esEntidadValido");
		Boolean esEntidadValido = idEntidadOrigen != null && !idEntidadOrigen.isEmpty() && idTipoEntidad != null && !idTipoEntidad.isEmpty();
		logger.trace("FIN::ModificarEstadoRegistroSolicitudCambio.esEntidadValido");
		return esEntidadValido;	
	}

	private SalidaModificarEstadoRegistroSolicitudCambio comprobarUpdateModificacionEstadoPorId(Connection con, Long idSolicitudCambio, String idEstadoAutorizacion, Long Empresa) throws Exception {
		logger.debug("INICIO::ModificarEstadoRegistroSolicitudCambio.comprobarUpdateModificacionEstadoPorId");
		long inicio = System.currentTimeMillis();
		SalidaModificarEstadoRegistroSolicitudCambio out = new SalidaModificarEstadoRegistroSolicitudCambio();
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();			
		params.put("IdSolicitudCambio", idSolicitudCambio);
		params.put("IdEstadoAutorizacion", idEstadoAutorizacion);
		List<RegistroSolicitudCambio> RegistroSolicitudCambio = sql.query(con, SQL_SELECT_SOLICITUD_CAMBIO_POR_ID, params, RegistroSolicitudCambio.class);
		
		if (RegistroSolicitudCambio.isEmpty() ) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR EN SOLICITUD DE CAMBIO ",null,con,Empresa));
			out.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarEstadoRegistroSolicitudCambio.comprobarUpdateModificacionEstadoPorId", fin - inicio);
		return out;
	}
	
	private SalidaModificarEstadoRegistroSolicitudCambio comprobarUpdateModificacionEstadoPorEntidad(Connection con, String idEntidadOrigen, String idTipoEntidad, String idEstadoAutorizacion, Long Empresa) throws Exception {
		logger.debug("INICIO::ModificarEstadoRegistroSolicitudCambio.comprobarUpdateModificacionEstadoPorEntidad");
		long inicio = System.currentTimeMillis();
		SalidaModificarEstadoRegistroSolicitudCambio out = new SalidaModificarEstadoRegistroSolicitudCambio();
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdEntidadOrigen", idEntidadOrigen);
		params.put("IdEstadoAutorizacion", idEstadoAutorizacion);
		params.put("IdTipoEntidad",idTipoEntidad);
		List<RegistroSolicitudCambio> RegistroSolicitudCambio = sql.query(con, SQL_SELECT_SOLICITUD_CAMBIO_POR_ENTIDAD, params, RegistroSolicitudCambio.class);
		
		if (RegistroSolicitudCambio.isEmpty() ) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR EN SOLICITUD DE CAMBIO ",null,con,Empresa));
			out.P_OUT_ERRORESCONTADOR = 1L;
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarEstadoRegistroSolicitudCambio.comprobarUpdateModificacionEstadoPorEntidad", fin - inicio);
		return out;
	}
	
}
