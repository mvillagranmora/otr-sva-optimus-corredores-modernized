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
 * ORACLE FAKE of legacy cl.bcs.tesoreria.RequisitosGirarComprobanteIngreso (cl/bcs/tesoreria/RequisitosGirarComprobanteIngreso.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Only an unused import of QuitarDetalleNominasTransferencias needs this POJO.
 */
public class RequisitosGirarComprobanteIngreso {

	// legacy cl/bcs/tesoreria/RequisitosGirarComprobanteIngreso.java:107-118 (verbatim)
	public static class Validar {
		public int Prioridad;
		public Integer Cumple;
		public String CodigoError;
		public String CodigoDato;
		public String ValorDato;
		public long IdComprobante;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}


}
