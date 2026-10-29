package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of legacy cl.bcs.plataforma.pla.utils.PlataformaUtils (BcsPlataformaServicios/.../PlataformaUtils.java).
 * The nested message classes below are copied VERBATIM from PlataformaUtils.java:598-718 (MensajeSalida with its
 * Diccionario-backed constructor, getNumeroErrores comparing tipo by reference, the Detalle classes and the
 * *AMensajes converters). The connection factories only exist for the main() methods of the legacy classes; they
 * are never executed by the oracle.
 */
public class PlataformaUtils {

    private static Connection noDisponible() throws SQLException {
        throw new SQLException("PlataformaUtils connection factories are not available in the oracle (legacy main() only)");
    }
    public static Connection getDESAConnection() throws SQLException { return noDisponible(); }
    public static Connection getQAConnection() throws SQLException { return noDisponible(); }
    public static Connection getMBConnection() throws SQLException { return noDisponible(); }
    public static Connection getMBIPREConnection() throws SQLException { return noDisponible(); }
    public static Connection getSURAConnection() throws SQLException { return noDisponible(); }
    public static Connection getPropertiesConnection(String ambiente) throws SQLException { return noDisponible(); }
    public static Connection getPropertiesConnection(String ambiente, String rutaProperties) throws SQLException { return noDisponible(); }

	public static class MensajeSalida {
		public Long id;
		public String tipo;	//ERROR,AUTORIZACION,ADVERTENCIA,INFORMACION
		public String codigo;
		public String descripcion;
		
		public static final String MENSAJE_OK = "OK";
		public static final String MENSAJE_ERROR = "ERROR";
		public static final String MENSAJE_ADVERTENCIA = "ADVERTENCIA";
		public static final String MENSAJE_AUTORIZACION = "AUTORIZACION";
		public static final String MENSAJE_INFORMACION = "INFORMACION";
		public static final Object MENSAJE_RECHAZO = "RECHAZO"; //TODO ELIMINAR DE REQUISITOS OPERATIVOS
		
		public MensajeSalida() {
			
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public MensajeSalida(Long id,String tipo,String codigo,String descripcion) {
			this.id = id;
			this.tipo = tipo;
			this.codigo = codigo;
			this.descripcion = descripcion;
		}
		
		public MensajeSalida(Long id,String tipo,String codigo,HashMap<String, String> valores,Connection con,Long idEmpresa) throws Exception {
			this.id = id;
			this.tipo = tipo;
			this.codigo = codigo;
			this.descripcion = Diccionario.getMensaje(con,idEmpresa, codigo, valores);
		}
		
		public static long getNumeroErrores(List<MensajeSalida> salida) {
			int c = 0;
			for (MensajeSalida s : salida) {
				if (s.tipo == MENSAJE_ERROR)
					c++;
			}
			return c;
		}
		
	}
	
	public static String concatenarMensajesSalida(List<MensajeSalida> mensajes) {
		String mensajeSalida = "";
		for (MensajeSalida mensaje : mensajes) {
			mensajeSalida += "\n" + mensaje.descripcion;
		}
		return mensajeSalida;
	}
	
	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InformacionDetalle {
		public String Codigo;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static List<MensajeSalida> erroresAMensajes (List<ErrorDetalle> errs) {
		List<MensajeSalida> salida = new ArrayList();
		for (ErrorDetalle err :errs) {
			MensajeSalida m = new MensajeSalida();
			m.codigo = err.Codigo;
			m.descripcion = err.Descripcion;
			m.tipo = "ERROR";
			salida.add(m);
		}
		return salida;
	}
	
	public static List<MensajeSalida> advertenciasAMensajes (List<AdvertenciaDetalle> advs) {
		List<MensajeSalida> salida = new ArrayList();
		for (AdvertenciaDetalle adv :advs) {
			MensajeSalida m = new MensajeSalida();
			m.codigo = adv.Codigo;
			m.descripcion = adv.Descripcion;
			if (adv.RequiereAutorizacion == "S")
				m.tipo = "AUTORIZACION";
			else
				m.tipo = "ADVERTENCIA";
			salida.add(m);
		}
		return salida;
	}
	
	public static List<MensajeSalida> informacionesAMensajes (List<InformacionDetalle> infos) {
		List<MensajeSalida> salida = new ArrayList();
		for (InformacionDetalle info :infos) {
			MensajeSalida m = new MensajeSalida();
			m.codigo = info.Codigo;
			m.descripcion = info.Descripcion;
			m.tipo = "INFORMACION";
			salida.add(m);
		}
		return salida;
	}

}
