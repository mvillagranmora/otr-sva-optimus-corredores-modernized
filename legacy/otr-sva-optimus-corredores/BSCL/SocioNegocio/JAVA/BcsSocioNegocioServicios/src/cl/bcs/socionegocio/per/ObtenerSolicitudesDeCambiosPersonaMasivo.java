package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambioMasivo.EntradaConsultarRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ConsultarRegistroSolicitudCambioMasivo.SalidaConsultarRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.EntradaConfirmarEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.InEntidad;

public class ObtenerSolicitudesDeCambiosPersonaMasivo {
	
	static final Logger logger = LogManager.getLogger(ObtenerSolicitudesDeCambiosPersonaMasivo.class);
	
	public List<SolicitudCambio> logica(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada, String tipoEntidad) throws Exception {
		logger.info("INICIO::ObtenerSolicitudesDeCambiosPersona");
		long inicio = System.currentTimeMillis();

		EntradaConsultarRegistroSolicitudCambioMasivo entradaConsultarRegistroSolicitudCambioMasivo = new EntradaConsultarRegistroSolicitudCambioMasivo();
		
		for (InEntidad id : entrada.p_Entidad) {
			ConsultarRegistroSolicitudCambioMasivo.EntradaSolicitudCambio entradaSolicitudCambio = new ConsultarRegistroSolicitudCambioMasivo.EntradaSolicitudCambio();
			entradaSolicitudCambio.IdEntidadOrigen = id.IdEntidad.toString();
			entradaSolicitudCambio.IdTipoEntidad = tipoEntidad;
			entradaConsultarRegistroSolicitudCambioMasivo.p_SolicitudesCambio.add(entradaSolicitudCambio);
		}
		
		ConsultarRegistroSolicitudCambioMasivo consultarRegistroSolicitudCambioMasivo = new ConsultarRegistroSolicitudCambioMasivo();
		SalidaConsultarRegistroSolicitudCambioMasivo salidaConsultarRegistroSolicitudCambioMasivo = consultarRegistroSolicitudCambioMasivo.logica(con, entradaConsultarRegistroSolicitudCambioMasivo);

		long fin = System.currentTimeMillis();
		logger.info("FIN::ObtenerSolicitudesDeCambiosPersona", fin - inicio);
		
		return salidaConsultarRegistroSolicitudCambioMasivo.P_OUT_SOLICITUDES_CAMBIO;
	}

}
