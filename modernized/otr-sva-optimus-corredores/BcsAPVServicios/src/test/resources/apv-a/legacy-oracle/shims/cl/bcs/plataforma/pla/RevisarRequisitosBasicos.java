package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import cl.bcs.utils.Utils;
import cl.bcs.utils.JavaCall;
import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import com.google.gson.JsonObject;

/**
 * ORACLE FAKE of cl.bcs.plataforma.pla.RevisarRequisitosBasicos (scripted messages).
 */
public class RevisarRequisitosBasicos implements JavaCall {

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


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }

	/**
	 * FAKE of RevisarRequisitosBasicos.logica (legacy checks empresa/usuario/modulo rows): records its input and returns the messages
	 * scripted in "colaboradores.requisitosBasicos" (list of {id,tipo,codigo,descripcion}); default none. 'tipo' is interned so that
	 * MensajeSalida.getNumeroErrores (reference comparison, legacy PlataformaUtils.java:636) behaves as with literals.
	 */
	public RevisarRequisitosBasicosSalida logica(Connection conn, Long p_IdEmpresa, Long p_IdUsuario, String p_IdModulo) throws Exception {
		java.util.Map<String, Object> in = new java.util.LinkedHashMap<>();
		in.put("p_IdEmpresa", p_IdEmpresa);
		in.put("p_IdUsuario", p_IdUsuario);
		in.put("p_IdModulo", p_IdModulo);
		Hooks.registrar("RevisarRequisitosBasicos", in);
		RevisarRequisitosBasicosSalida out = new RevisarRequisitosBasicosSalida();
		for (com.google.gson.JsonElement e : Hooks.lista("requisitosBasicos")) {
			com.google.gson.JsonObject o = e.getAsJsonObject();
			out.p_out_Mensajes.add(new MensajeSalida(o.get("id").getAsLong(), o.get("tipo").getAsString().intern(),
					o.get("codigo").getAsString(), o.get("descripcion").getAsString()));
		}
		return out;
	}

}
