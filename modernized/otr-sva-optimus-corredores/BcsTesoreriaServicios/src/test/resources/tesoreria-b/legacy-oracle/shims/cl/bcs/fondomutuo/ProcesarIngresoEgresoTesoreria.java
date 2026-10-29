package cl.bcs.fondomutuo;

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
 * ORACLE FAKE of legacy cl.bcs.fondomutuo.ProcesarIngresoEgresoTesoreria (cl/bcs/fondomutuo/ProcesarIngresoEgresoTesoreria.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Only the Empleado row POJO is needed by ProcesarDistribucion.
 */
public class ProcesarIngresoEgresoTesoreria {

	// legacy cl/bcs/fondomutuo/ProcesarIngresoEgresoTesoreria.java:88-94 (verbatim)
	public static class Empleado{
		public Long		IdEmpleado;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	


}
