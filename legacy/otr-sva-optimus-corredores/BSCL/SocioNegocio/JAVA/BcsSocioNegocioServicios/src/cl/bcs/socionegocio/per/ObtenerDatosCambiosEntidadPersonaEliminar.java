package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.InEntidad;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntidadPersonaEliminar;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.utils.SQL;

public class ObtenerDatosCambiosEntidadPersonaEliminar {
	
	static final Logger logger = LogManager.getLogger(ObtenerDatosCambiosEntidadPersonaEliminar.class);
	
	public List<EntidadPersonaEliminar> logica(Connection con, EntradaEjecutarCambiosPersona entrada, String selectEntidadPersonaEliminar) throws Exception {
		logger.debug("INICIO::ObtenerDatosCambiosEntidadPersonaEliminar");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		List<Long> idEntidades = entrada.p_RegistroCambio.stream()
														 .map(entidad -> entidad.IdEntidad)
														 .distinct()
														 .collect(Collectors.toList());
		params.put("IdEntidad", idEntidades);		
		List<EntidadPersonaEliminar> lstEntidad = sql.query(con, selectEntidadPersonaEliminar, params, EntidadPersonaEliminar.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ObtenerDatosCambiosEntidadPersonaEliminar", fin - inicio);
		return lstEntidad;
	}
	
	public List<InEntidad> obtenerListaPersonaEntidad(List<EntidadPersonaEliminar> lstEntidadPersonaEliminar) {
		logger.trace("INICIO::ObtenerDatosCambiosEntidadPersonaEliminar.obtenerListaPersonaEntidad");
		long inicio 	= System.currentTimeMillis();		
		List<InEntidad> listaPersonaEntidad = lstEntidadPersonaEliminar.stream()
												  .map(entidad -> new InEntidad(entidad.IdPersona,entidad.IdEntidadPersonaEliminar))
												  .distinct()
												  .collect(Collectors.toList());	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ObtenerDatosCambiosEntidadPersonaEliminar.obtenerListaPersonaEntidad", fin - inicio);
		return listaPersonaEntidad;
	}

}
