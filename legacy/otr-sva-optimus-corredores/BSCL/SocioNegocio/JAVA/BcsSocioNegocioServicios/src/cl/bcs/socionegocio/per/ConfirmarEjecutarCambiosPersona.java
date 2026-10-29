package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;

public interface ConfirmarEjecutarCambiosPersona {

	public static class EntradaConfirmarEjecutarCambiosPersona extends BcsPojo {
		public Long 							p_IdEmpresa;
		public Long 							p_IdUsuario;
		public String 							p_IdModulo;
		public String 							p_UsaReglasNegocio;
		public List<InEntidad> 					p_Entidad;
		public String 							p_IdEstadoAutorizacion;
		
		public EntradaConfirmarEjecutarCambiosPersona() {
			this.p_Entidad 		= new ArrayList<InEntidad>();
		}
	}
	
	public static class InEntidad extends BcsPojo {
		public Long			IdPersona;	
		public Long 		IdEntidad;	
		
		public InEntidad() {
			this.IdPersona = 0L;
			this.IdEntidad = 0L;
		}
		
		public InEntidad(Long idPersona, Long idEntidad) {
			this.IdPersona = idPersona;
			this.IdEntidad = idEntidad;
		}
	};
	
	public static class SalidaConfirmarEjecutarCambiosPersona extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;		
		
		public SalidaConfirmarEjecutarCambiosPersona(){
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();			
		}
	}
	
	public static class PersonaEntidadCambiar extends BcsPojo {
		public Long 	IdEntidad;
		public String 	IdEstadoAutorizacion;
	}
	
	public SalidaConfirmarEjecutarCambiosPersona logica(Connection con, EntradaConfirmarEjecutarCambiosPersona entrada) throws Exception;
	
}
