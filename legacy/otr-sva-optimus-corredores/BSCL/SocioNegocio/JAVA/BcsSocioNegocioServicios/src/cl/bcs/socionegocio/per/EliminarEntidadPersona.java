package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntidadPersonaEliminar;
import cl.bcs.utils.SQL;

public class EliminarEntidadPersona {

	static final Logger logger = LogManager.getLogger(EliminarEntidadPersona.class);
	private static FileLoader loader;
	private static String SQL_SELECT_TIENE_MINIMO;
	
	static {
		loader = new FileLoader();
		SQL_SELECT_TIENE_MINIMO = loader.loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarRegistroTablasPersona_SelectTieneCantidadMinima.sql");
	}
	

	public static class TieneCantidadMinima {
		public String Requerido;
		
		public TieneCantidadMinima() {
			this.Requerido = "N";
		}
	}
	
	public void logica(Connection con, List<EntidadPersonaEliminar> entidadPersonaEliminar, String deleteEntidadPersona) throws Exception {
		logger.debug("INICIO::EliminarEntidadPersona");
		long inicio = System.currentTimeMillis();
		
		if(entidadPersonaEliminar.size() > 0 ) {
			SQL sql = new SQL();
			sql.batch(con, deleteEntidadPersona, entidadPersonaEliminar, EntidadPersonaEliminar.class);			
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarEntidadPersona", fin - inicio);
	}
	

	public String tieneCantidadMinima(Connection con,  String tipoEnrolamiento, String tipoPersona,String codigoCampo, String idTipoCanal) throws Exception {
		logger.debug("INICIO::EliminarPersonaRegistroGrillasUtils.tieneCantidadMinima");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		String codigoFormulario = "GRILLA_" + tipoEnrolamiento + "_" +tipoPersona;
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("CodigoFormulario", codigoFormulario);
		params.put("CodigoCampo", codigoCampo);
		params.put("IdTipoCanal", idTipoCanal);
		 
		String tieneCantidadMinima = sql.query(con, SQL_SELECT_TIENE_MINIMO, params, TieneCantidadMinima.class).stream().findFirst().orElse(new TieneCantidadMinima()).Requerido;

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarPersonaRegistroGrillasUtils.tieneCantidadMinima", finServicio - inicio);
		return tieneCantidadMinima;
	}
	
	public boolean alMenosUnRegistro(Connection con, List<EntidadPersonaEliminar> lstEntidadPersonaEliminar, List<EntidadPersonaEliminar> registros, Long idPersona, String codigoCampo, String idTipoCanal) throws Exception {
		logger.debug("INICIO::EliminarEntidadPersona.alMenosUnRegistro");
		long inicio = System.currentTimeMillis();
		
		String TipoPersona = registros.stream().findFirst().get().TipoPersona.equals("N") ? "NATURAL" : "JURIDICA";
		String TipoEnrolamiento = registros.stream().findFirst().get().TipoEnrolamiento;
		
		Long cantidadRegistros = registros.stream().count();
		Long cantidadEliminar = lstEntidadPersonaEliminar.stream().count();
		
		String cantidadMinimaRegistros = tieneCantidadMinima(con, TipoEnrolamiento, TipoPersona, codigoCampo, idTipoCanal);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarEntidadPersona.alMenosUnRegistro", finServicio - inicio);
		return cantidadMinimaRegistros.equals("S") && cantidadRegistros.equals(cantidadEliminar) ? true : false;
	}

	public List<MensajeSalida> obtenerMensajesErrorEliminarEntidadesPredeterminadas(Connection con, Long idEmpresa, List<EntidadPersonaEliminar> entidadesPredeterminadas, String entidadPersona) throws Exception {
		logger.debug("INICIO::EliminarEntidadPersona.obtenerMensajesErrorEliminarEntidadesPredeterminadas");
		long inicio = System.currentTimeMillis();
		
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		for(EntidadPersonaEliminar entidad : entidadesPredeterminadas) {
			HashMap<String, String> parametrosMensajeError = new HashMap<>(2);
			parametrosMensajeError.put("EntidadPersona", entidadPersona);
			
			mensajes.add(new MensajeSalida(entidad.IdEntidadPersonaEliminar,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_ELIMINAR_ENTIDAD_PREDETERMINADA",parametrosMensajeError, con, idEmpresa));		
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarEntidadPersona.obtenerMensajesErrorEliminarEntidadesPredeterminadas", finServicio - inicio);
		return mensajes;
	}
}
