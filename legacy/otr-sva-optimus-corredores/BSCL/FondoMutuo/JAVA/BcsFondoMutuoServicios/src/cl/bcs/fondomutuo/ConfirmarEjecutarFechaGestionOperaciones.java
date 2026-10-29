package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.fondomutuo.ActualizarFechaGestionOperaciones.SolicitudesAporteRescate;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;

import cl.bcs.utils.BcsPojo;

public interface ConfirmarEjecutarFechaGestionOperaciones{

	public static class EntradaConfirmarEjecutarFechaGestionOperaciones extends BcsPojo {
		public Long 							p_IdEmpresa;
		public Long 							p_IdUsuario;
		public Long								p_IdEvento;
		public Long								p_IdUsuarioCambioEstado;
		public String 							p_IdModulo;
		public String 							p_UsaReglasNegocio;
		public List<InEntidad> 					p_Entidad;
		public String 							p_IdEstadoAutorizacion;
	
		public EntradaConfirmarEjecutarFechaGestionOperaciones() {
			this.p_Entidad 		= new ArrayList<InEntidad>();
		}
	}
	
	public static class InEntidad extends BcsPojo {
		public Long			IdSolicitudAporteRescate;	
		public Long  		IdEntidad;	
		
		public InEntidad() {
			this.IdSolicitudAporteRescate = 0L;
			this.IdEntidad = 0L;
		}
		
		public InEntidad(Long idSolicitudAporteRescate, Long idEntidad) {
			this.IdSolicitudAporteRescate = idSolicitudAporteRescate;
			this.IdEntidad = idEntidad;
		}
	};
	
	public static class SalidaConfirmarEjecutarFechaGestionOperaciones extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;		
		
		public SalidaConfirmarEjecutarFechaGestionOperaciones(){
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();			
		}
	}
	
	public static class SolicitudAporteRescateEntidadCambiar extends BcsPojo {
		public Long 	IdEntidad;
		public String 	IdEstadoAutorizacion;
	}
	
	public SalidaConfirmarEjecutarFechaGestionOperaciones logica(Connection con, EntradaConfirmarEjecutarFechaGestionOperaciones entrada ) throws Exception;
	
}
