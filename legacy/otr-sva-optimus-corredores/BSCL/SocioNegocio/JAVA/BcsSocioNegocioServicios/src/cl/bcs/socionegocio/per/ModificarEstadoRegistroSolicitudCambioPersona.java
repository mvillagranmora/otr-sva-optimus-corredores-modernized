package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.EntradaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.EntradaConfirmarEjecutarCambiosPersona;

public class ModificarEstadoRegistroSolicitudCambioPersona {

	static final Logger logger = LogManager.getLogger(ModificarEstadoRegistroSolicitudCambioPersona.class);
	
	public SalidaModificarEstadoRegistroSolicitudCambio logica(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada, List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.debug("INICIO::ModificarEstadoRegistroSolicitudCambioPersona");
		long inicio = System.currentTimeMillis();
		
		EntradaModificarEstadoRegistroSolicitudCambio entradaModificarEstadoRegistroSolicitudCambio = new EntradaModificarEstadoRegistroSolicitudCambio();
		entradaModificarEstadoRegistroSolicitudCambio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdUsuario = entrada.p_IdUsuario;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdModulo = entrada.p_IdModulo;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdSolicitudCambio = solicitudesCambios.stream().findFirst().get().IdSolicitudCambio;
		entradaModificarEstadoRegistroSolicitudCambio.p_IdEstadoAutorizacion = entrada.p_IdEstadoAutorizacion;
		
		ModificarEstadoRegistroSolicitudCambio modificarEstadoRegistroSolicitudCambio = new ModificarEstadoRegistroSolicitudCambio();
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificarEstadoRegistroSolicitudCambio = modificarEstadoRegistroSolicitudCambio.logica(con, entradaModificarEstadoRegistroSolicitudCambio);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarEstadoRegistroSolicitudCambioPersona", fin - inicio);
		return salidaModificarEstadoRegistroSolicitudCambio;
    }
}
