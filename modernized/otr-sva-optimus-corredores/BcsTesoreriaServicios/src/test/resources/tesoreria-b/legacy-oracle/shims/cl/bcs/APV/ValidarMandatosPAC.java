package cl.bcs.APV;

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
import java.sql.Date;

/**
 * ORACLE FAKE of legacy cl.bcs.APV.ValidarMandatosPAC (cl/bcs/APV/ValidarMandatosPAC.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Only the LogValidarMandatosPAC row POJO is needed by ProcesarDistribucion.
 */
public class ValidarMandatosPAC {

	// legacy cl/bcs/APV/ValidarMandatosPAC.java:53-63 (verbatim)
	public static class LogValidarMandatosPAC
	{
		public Long			IdMandatoPAC;
		public String 		IdentServicio;
		public Date			FechaProceso;
		public Date			FechaSistema;
		public String		EstadoProcesamiento;
		public String		PreRendicion;
		public String 		Usuario;
		public String 		Glosa;
	}


}
