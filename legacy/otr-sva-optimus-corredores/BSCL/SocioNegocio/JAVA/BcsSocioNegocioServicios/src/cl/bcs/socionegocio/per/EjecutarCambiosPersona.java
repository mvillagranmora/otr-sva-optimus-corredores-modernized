package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.BcsPojo;

public interface EjecutarCambiosPersona {
	
	public static class EntradaEjecutarCambiosPersona extends BcsPojo {
		public Long 				p_IdEmpresa;
		public Long 				p_IdUsuario;
		public Long					p_IdPersona;
		public String 				p_IdModulo;
		public String 				p_UsaReglasNegocio;
		public String 				p_IdTipoCanal;
		public String 				p_EsCrearCliente;
		public List<RegistroCambioMasivo> p_RegistroCambio;
		
		public EntradaEjecutarCambiosPersona() {
			this.p_RegistroCambio = new ArrayList<RegistroCambioMasivo>();
			this.p_IdTipoCanal = "";
			this.p_EsCrearCliente = "N";
		}
	}
	
	public static class SalidaEjecutarCambiosPersona extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;		
		public Long	 				P_OUT_REFID;
		
		public SalidaEjecutarCambiosPersona(){
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();			
		}
	}
	
	public static class EntidadPersonaEliminar extends BcsPojo {
		public Long 	IdEntidadPersonaEliminar;
		public String 	TipoPersona;
		public String 	TipoEnrolamiento;
		public Long 	IdPersona;
		public String 	NombreCompleto;
		public String 	DocumentoIdentidad;
		public String 	Borrado;
		public String	Predeterminado;
		
		public EntidadPersonaEliminar () {
			this.Predeterminado = "N";
		}
	}
	
	public SalidaEjecutarCambiosPersona logica(Connection con, EntradaEjecutarCambiosPersona entrada) throws Exception;
}
