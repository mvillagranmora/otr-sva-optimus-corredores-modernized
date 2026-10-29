package cl.bcs.socionegocio.soc;

import java.util.ArrayList;
import java.util.List;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;


public interface EjecutarCambiosSocioNegocio {
	
	public static class EntradaModificarSocioNegocio<T> {
		
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdPersona;
		public Long p_IdCliente;
		public String p_IdTipoPersona;
		public List<T> p_datos;
		
		public EntradaModificarSocioNegocio() {
			this.p_IdModulo 		= "SOC";
			this.p_UsaReglasNegocio = "N";
			this.p_datos 			= new ArrayList<>();
		}
	}
	
	public static class SalidaModificarSocioNegocio {
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		
		public SalidaModificarSocioNegocio() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;	
		}
	}
	
}
