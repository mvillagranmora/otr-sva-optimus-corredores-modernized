package cl.bcs.plataforma.ccl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.utils.BcsPojo;

public interface BcsConciliacion {
	
	public abstract SalidaConciliacion conciliar(Connection conn) throws SQLException;
	
	public static class SalidaConciliacion extends BcsPojo {
		public String nombreConciliacion = "";
		public String estadoConciliacion = "";
		public List<DetalleConciliacion> detalleConciliacion = new ArrayList<>();
	}
	
	public static class DetalleConciliacion extends BcsPojo {
		public String nombreDetalle = "";
		public String estadoDetalle = "";
		public String mensajeDetalle = "";
		public List<Object> detalleMovimientos = new ArrayList<>();
	}
	
	
}