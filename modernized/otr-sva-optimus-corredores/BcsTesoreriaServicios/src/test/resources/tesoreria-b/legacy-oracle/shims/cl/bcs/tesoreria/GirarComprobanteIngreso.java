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
 * ORACLE FAKE of legacy cl.bcs.tesoreria.GirarComprobanteIngreso (cl/bcs/tesoreria/GirarComprobanteIngreso.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Only an unused import of QuitarDetalleNominasTransferencias needs this POJO.
 */
public class GirarComprobanteIngreso {

	// legacy cl/bcs/tesoreria/GirarComprobanteIngreso.java:117-125 (verbatim)
	public static class Comprobantes{

		public Long IdComprobante;
		public String IdEstado;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


}
