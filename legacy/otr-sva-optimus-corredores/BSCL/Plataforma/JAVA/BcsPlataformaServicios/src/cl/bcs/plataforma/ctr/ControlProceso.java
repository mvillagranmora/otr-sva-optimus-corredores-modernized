package cl.bcs.plataforma.ctr;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Utils;

public interface ControlProceso {
	
	public class EntradaControlProceso{
		public Long				p_IdEmpresa;
		public Long				p_IdUsuario;
		public String			p_IdModulo;
		public String			p_Fecha;
		public String			p_FechaAnterior;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class SalidaControlProcesoDetalle {
		public Long 	IdEmpresa;
		public String 	CodigoControlProcesoTipo;
		public String   FechaSistema;
		public String 	FechaInicio;
		public Long 	CantidadTotal;
		public Long 	CantidadEjecutada;		
		public String 	IdEstado;
		public String 	Mensaje;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class SalidaControlProceso{
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long                 P_OUT_ERRORESCONTADOR;
		public List<SalidaControlProcesoDetalle>	P_OUT_RESULTADO;

		public SalidaControlProceso(){
			P_OUT_RESULTADO = new ArrayList<SalidaControlProcesoDetalle>();
		}
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public SalidaControlProceso ejecutar(Connection conn, EntradaControlProceso entrada);
	
}
