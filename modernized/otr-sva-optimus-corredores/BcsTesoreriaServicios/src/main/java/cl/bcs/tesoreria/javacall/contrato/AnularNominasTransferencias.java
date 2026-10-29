package cl.bcs.tesoreria.javacall.contrato;

import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.javacall.comun.SalidaServicios;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON contract (legacy field names, P_OUT_*) of the collaborator service {@code cl.bcs.tesoreria.nor.AnularNominasTransferencias} as the Tesoreria services call it
 * through the {@code Colaboradores} port. The nested classes are the legacy Entrada/Salida classes; their no-argument constructors set the same
 * defaults, so decoding an empty answer with Gson gives the legacy empty Salida.
 */
public final class AnularNominasTransferencias {

	private AnularNominasTransferencias() {
	}


	// legacy Tesoreria/JAVA/BcsTesoreriaServicios/src/cl/bcs/tesoreria/nor/AnularNominasTransferencias.java:58-66 (verbatim)
	public static class RegistrosAnularNominasTransferencias extends BcsPojo {
	    public Long Id;
	    public String Entidad;

	    public RegistrosAnularNominasTransferencias(Long Id, String Entidad) {
	        this.Id = Id;
	        this.Entidad = Entidad;
	    }
	}

	// legacy Tesoreria/JAVA/BcsTesoreriaServicios/src/cl/bcs/tesoreria/nor/AnularNominasTransferencias.java:69-81 (verbatim)
	public static class EntradaAnularNominasTransferencias{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_FechaMovimiento;
		public List<RegistrosAnularNominasTransferencias> p_RegistrosAAnular;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}

	// legacy Tesoreria/JAVA/BcsTesoreriaServicios/src/cl/bcs/tesoreria/nor/AnularNominasTransferencias.java:83-88 (verbatim)
	public static class ResultadoAnularNominasTransferencias{
		public String Campo;
		public Long Id;
		public String Servicio;
		public String Resultado;
	}

	// legacy Tesoreria/JAVA/BcsTesoreriaServicios/src/cl/bcs/tesoreria/nor/AnularNominasTransferencias.java:90-100 (verbatim)
	public static class SalidaAnularNominasTransferencias extends SalidaServicios{
		public List<ResultadoAnularNominasTransferencias> p_out_ResultadoProceso;
		public SalidaAnularNominasTransferencias() {
			super();
			this.p_out_ResultadoProceso = new ArrayList<>();
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
}
