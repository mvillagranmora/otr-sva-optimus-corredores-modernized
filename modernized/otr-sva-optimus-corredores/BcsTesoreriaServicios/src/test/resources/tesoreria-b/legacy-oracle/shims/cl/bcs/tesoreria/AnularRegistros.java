package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import cl.bcs.utils.Utils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.BcsPojo;
import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import com.google.gson.JsonObject;

/**
 * ORACLE FAKE of legacy cl.bcs.tesoreria.AnularRegistros (cl/bcs/tesoreria/AnularRegistros.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Only an unused import of AnularNominasTransferencias needs this POJO.
 */
public class AnularRegistros {

	// legacy cl/bcs/tesoreria/AnularRegistros.java:103-115 (verbatim)
	public static class Registro_Entrada {
		public Long Id;
		public String Entidad;
		public Long getId() {
			return Id;
		}
		public String getEntidad() {
			return Entidad;
		}		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}

	// legacy cl/bcs/tesoreria/AnularRegistros.java:117-134 (verbatim)
	public static class AnularRegistros_Entrada {
		public Long							p_IdEmpresa;
		public Long							p_IdUsuario;
		public String						p_IdModulo;
		public String						p_simularOperacion = "N";
		public String						p_UsaReglasNegocio;
		public String						p_EjecutaAutorizacion = "S";
		public String						p_FechaMovimiento;
		public List<Registro_Entrada>		p_RegistrosAProcesar;
				
		public AnularRegistros_Entrada (){			
			p_RegistrosAProcesar	= new ArrayList<Registro_Entrada>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};


}
