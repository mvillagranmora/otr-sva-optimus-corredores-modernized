package cl.bcs.plataforma.aut;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.plataforma.aut.pojo.Evento;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Utils;

public interface BcsAutorizaciones {
	
	public final String EXITO_AUTORIZACION = "EXITO";
	public final String ERROR_AUTORIZACION = "ERROR";
	
	public static class SalidaAutorizacion {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		
		public List<ResultadoAutorizacion> P_OUT_RESULTADO;
		
		public SalidaAutorizacion() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_RESULTADO = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoAutorizacion{ 
		public Object evento;
		public String resultado; //EXITO_AUTORIZACION, ERROR_AUTORIZACION
	}
	
	public static class EntradaAutorizacion {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		
		public List<Evento> p_Lista;
		
		public EntradaAutorizacion() {
			this.p_Lista = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada);
	public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada);
		
}
