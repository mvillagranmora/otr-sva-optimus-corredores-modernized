package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import cl.bcs.utils.Utils;

/**
 * ORACLE SHIM of cl.bcs.plataforma.pla.utils.PlataformaUtils: MensajeSalida (incl. getNumeroErrores, which compares tipo with == as in the legacy)
 * and the three detail POJOs are verbatim copies; connection factories are stubs.
 */
public class PlataformaUtils {

	// legacy cl/bcs/plataforma/pla/utils/PlataformaUtils.java:598-642 (verbatim)
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

	// legacy cl/bcs/plataforma/pla/utils/PlataformaUtils.java:652-659 (verbatim)
	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/pla/utils/PlataformaUtils.java:661-669 (verbatim)
	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/plataforma/pla/utils/PlataformaUtils.java:671-678 (verbatim)
	public static class InformacionDetalle {
		public String Codigo;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	/** Only used by the legacy main() methods (they open real HANA connections); never reached by the oracle. */
	public static Connection getDESAConnection() { throw new UnsupportedOperationException("oracle shim"); }

	public static Connection getPropertiesConnection(String a, String b) { throw new UnsupportedOperationException("oracle shim"); }

	public static Connection getBOLSAConnection() { throw new UnsupportedOperationException("oracle shim"); }

	public static Connection getPRESURAConnection() { throw new UnsupportedOperationException("oracle shim"); }

	public static Connection getMBIConnection() { throw new UnsupportedOperationException("oracle shim"); }

	public static Connection getQAConnection() { throw new UnsupportedOperationException("oracle shim"); }

	public static Connection getSURAConnection() { throw new UnsupportedOperationException("oracle shim"); }

	public static Connection getMBConnection() { throw new UnsupportedOperationException("oracle shim"); }

}
