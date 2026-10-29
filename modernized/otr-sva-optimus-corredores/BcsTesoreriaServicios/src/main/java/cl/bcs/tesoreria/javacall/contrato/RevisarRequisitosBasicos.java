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
 * JSON contract (legacy field names, P_OUT_*) of the collaborator service {@code cl.bcs.plataforma.pla.RevisarRequisitosBasicos} as the Tesoreria services call it
 * through the {@code Colaboradores} port. The nested classes are the legacy Entrada/Salida classes; their no-argument constructors set the same
 * defaults, so decoding an empty answer with Gson gives the legacy empty Salida.
 */
public final class RevisarRequisitosBasicos {

	private RevisarRequisitosBasicos() {
	}


	// legacy cl/bcs/plataforma/pla/RevisarRequisitosBasicos.java:131-143 (verbatim)
	public static class RevisarRequisitosBasicosSalida {
		public List<MensajeSalida> p_out_Mensajes;
		
		public RevisarRequisitosBasicosSalida() {
			this.p_out_Mensajes = new ArrayList<MensajeSalida>();
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
}
