package cl.bcs.cuentaInversion;

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
import java.util.Date;

/**
 * ORACLE FAKE of legacy cl.bcs.cuentaInversion.AnularCargoAbono (cl/bcs/cuentaInversion/AnularCargoAbono.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Used by AnularNominasTransferencias and QuitarDetalleNominasTransferencias.
 */
public class AnularCargoAbono implements JavaCall {

	// legacy cl/bcs/cuentaInversion/AnularCargoAbono.java:94-101 (verbatim)
	public static class CargoAbono_Entrada {
		public Long IdCargoAbonoMovimiento;
		public String GlosaAnulacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// legacy cl/bcs/cuentaInversion/AnularCargoAbono.java:118-137 (verbatim)
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
	};

	// legacy cl/bcs/cuentaInversion/AnularCargoAbono.java:139-150 (verbatim)
	public static class AnularCargoAbono_Salida {
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		
		public AnularCargoAbono_Salida (){
			P_OUT_MENSAJES 				= new ArrayList<>();
		};
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	};

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }


	/**
	 * FAKE of AnularCargoAbono.logica (legacy annuls investment-account movements): records its input and answers
	 * "colaboradores.anularCargoAbono": {"mensajes":[{id,tipo,codigo,descripcion}], "errores": n (default: number of ERROR messages), "lanza": "text"}.
	 */
	public AnularCargoAbono_Salida logica(Connection con, AnularCargoAbono_Entrada in) throws Exception {
		Hooks.registrar("AnularCargoAbono", in);
		JsonObject script = Hooks.objeto("anularCargoAbono");
		if (script.has("lanza")) throw new Exception(script.get("lanza").getAsString());
		AnularCargoAbono_Salida out = new AnularCargoAbono_Salida();
		if (script.has("mensajes")) {
			for (com.google.gson.JsonElement e : script.getAsJsonArray("mensajes")) out.P_OUT_MENSAJES.add(Hooks.mensaje(e.getAsJsonObject()));
		}
		out.P_OUT_ERRORESCONTADOR = script.has("errores") ? script.get("errores").getAsLong() : MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		return out;
	}

}
