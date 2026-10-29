package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Utils;

public interface BcsProcesoPauta {

	public abstract SalidaProcesoPauta transaction(Connection con,Map<String, Object> parameters) throws Throwable;

	public static class SalidaProcesoPauta {
		public List<MensajeSalida>	P_OUT_MENSAJES;	//DETALLES
		public String				P_OUT_MENSAJE;	//RESUMEN
		public Long					P_OUT_ERRORESCONTADOR = 0L;

		public SalidaProcesoPauta() {
			P_OUT_MENSAJES = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public String toString(boolean pretty) {
			return Utils.printJson(this, pretty);
		}
	}
	
	public static class EntradaProcesoPauta {
		public Long				p_IdEmpresa;
		public Long				p_IdUsuario;
		public String			p_IdModulo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

}
