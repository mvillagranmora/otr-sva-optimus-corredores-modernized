package cl.bcs.replica;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Utils;

public interface IniciarReplica {
	
	public static class EntradaIniciarReplica {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaIniciarReplica {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public SalidaIniciarReplica() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Workstack {
		public Long IdStack;
		public String Key;
		public String Table;
		public String Action;
		public int Status;
		public Timestamp Stamp;
		public String Parameters;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public SalidaIniciarReplica logica (Connection conn, EntradaIniciarReplica in) throws Exception;
}
