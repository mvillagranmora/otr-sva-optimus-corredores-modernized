package cl.bcs.tesoreria.javacall.contrato;

import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
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
 * JSON contract (legacy field names, P_OUT_*) of the collaborator service {@code cl.bcs.cuentaInversion.AnularCargoAbono} as the Tesoreria services call it
 * through the {@code Colaboradores} port. The nested classes are the legacy Entrada/Salida classes; their no-argument constructors set the same
 * defaults, so decoding an empty answer with Gson gives the legacy empty Salida.
 */
public final class AnularCargoAbono {

	private AnularCargoAbono() {
	}


	// legacy CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion/AnularCargoAbono.java:94-101 (verbatim)
	public static class CargoAbono_Entrada {
		public Long IdCargoAbonoMovimiento;
		public String GlosaAnulacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion/AnularCargoAbono.java:118-137 (verbatim)
	public static class AnularCargoAbono_Entrada{
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_simularOperacion;
		public String						p_UsaReglasNegocio;
		public String						p_EjecutaAutorizacion = "S";
		public String						p_OmiteRegistroSinAutorizar	= "S";
		public String						p_FechaMovimiento;
		public String						p_EsAnulacionManual	= "N";
		public List<CargoAbono_Entrada>		p_CargoAbonoAAnular;
				
		public AnularCargoAbono_Entrada (){			
			p_CargoAbonoAAnular	= new ArrayList<CargoAbono_Entrada>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}

	// legacy CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion/AnularCargoAbono.java:139-150 (verbatim)
	public static class AnularCargoAbono_Salida {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		
		public AnularCargoAbono_Salida (){
			P_OUT_MENSAJES 				= new ArrayList<>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}

	// legacy CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion/AnularCargoAbono.java:514-522 (verbatim)
	public static class ObjInformacionDetalle {
		public Long			IdCargoAbonoMovimiento; 
		public String		Codigo; 
		public String		Descripcion; 
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}

	// legacy CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion/AnularCargoAbono.java:523-532 (verbatim)
	public static class ObjAdvertenciasDetalle {
		public Long			NumeroCargoAbono; 
		public String		Codigo; 
		public String		RequiereAutorizacion; 
		public String		Descripcion; 
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}

	// legacy CuentaInversion/JAVA/BcsCuentaInversionServicios/src/cl/bcs/cuentaInversion/AnularCargoAbono.java:533-541 (verbatim)
	public static class ObjErroresDetalle {
		public Long			NumeroCargoAbono; 
		public String		Codigo; 
		public String		Descripcion; 
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
}
