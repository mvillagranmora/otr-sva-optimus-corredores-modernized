package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.EntradaConfirmarEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.InEntidad;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.SalidaConfirmarEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;

public class ConfirmarCambiosPersonaEntidad {

	static final Logger logger = LogManager.getLogger(ConfirmarCambiosPersonaEntidad.class);
	
	public SalidaEjecutarCambiosPersona logica(Connection con, EntradaEjecutarCambiosPersona entrada, List<InEntidad> lstInEntidad, ConfirmarEjecutarCambiosPersona confirmarEjecutar) throws Exception {
		logger.debug("INICIO::confirmarCambiosPersonaEntidad");
		long inicio = System.currentTimeMillis();

		EntradaConfirmarEjecutarCambiosPersona entradaConfirmarEjecutarCambiosPersona = new EntradaConfirmarEjecutarCambiosPersona();		
		entradaConfirmarEjecutarCambiosPersona.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfirmarEjecutarCambiosPersona.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfirmarEjecutarCambiosPersona.p_IdModulo = entrada.p_IdModulo;
		entradaConfirmarEjecutarCambiosPersona.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConfirmarEjecutarCambiosPersona.p_IdEstadoAutorizacion = "ING";	
		entradaConfirmarEjecutarCambiosPersona.p_Entidad = lstInEntidad;

		SalidaConfirmarEjecutarCambiosPersona salidaConfirmarEjecutarCambiosPersona = confirmarEjecutar.logica(con, entradaConfirmarEjecutarCambiosPersona);

		SalidaEjecutarCambiosPersona out = new SalidaEjecutarCambiosPersona();
		out.P_OUT_ERRORESCONTADOR = salidaConfirmarEjecutarCambiosPersona.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaConfirmarEjecutarCambiosPersona.P_OUT_MENSAJES;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::confirmarCambiosPersonaEntidad", fin - inicio);
		return out;
	}

}
