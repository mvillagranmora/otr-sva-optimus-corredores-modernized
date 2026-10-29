package cl.bcs.socionegocio.per;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.pojo.SolicitudCambio;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntidadPersonaEliminar;

public class ObtenerEntidadesPersonaEliminar {
	
	static final Logger logger = LogManager.getLogger(ObtenerEntidadesPersonaEliminar.class);
	
	public List<EntidadPersonaEliminar> logica(List<SolicitudCambio> solicitudesCambios) throws Exception {
		logger.info("INICIO::ObtenerEntidadesPersonaEliminar");
		long inicio = System.currentTimeMillis();
		
		Map<Long, List<SolicitudCambio>> solicitudesPorIdEntidad = solicitudesCambios.parallelStream().collect(Collectors.groupingBy(solicitudCambio->solicitudCambio.IdEntidadOrigenLong));
		
		List<EntidadPersonaEliminar> datosAEliminar = new ArrayList<>();
		
		for(Entry<Long, List<SolicitudCambio>> persona : solicitudesPorIdEntidad.entrySet()) {
			EntidadPersonaEliminar datosEntidadPersonaEliminar = new EntidadPersonaEliminar();
			
			List<SolicitudCambio> cambiosDocumento = persona.getValue();
			datosEntidadPersonaEliminar.IdEntidadPersonaEliminar = persona.getKey();
			
			for(SolicitudCambio solicitudCambio : cambiosDocumento) {
				Field campo = datosEntidadPersonaEliminar.getClass().getField(solicitudCambio.CampoModificado);
				campo.set(datosEntidadPersonaEliminar, solicitudCambio.ValorNuevo);
			}
			datosAEliminar.add(datosEntidadPersonaEliminar);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ObtenerEntidadesPersonaEliminar", fin - inicio);
		return datosAEliminar;
	}
}
