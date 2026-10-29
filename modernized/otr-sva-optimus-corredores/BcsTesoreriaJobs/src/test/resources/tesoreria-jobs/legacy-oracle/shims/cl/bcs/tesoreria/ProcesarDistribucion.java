package cl.bcs.tesoreria;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;

import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Utils;

/**
 * ORACLE FAKE of legacy cl.bcs.tesoreria.ProcesarDistribucion (Tesoreria/.../ProcesarDistribucion.java:250-290). Only the two
 * POJOs the job touches; the row lists are kept as raw JSON (the job only forwards them). logica() records its input in Hooks and
 * answers case.json "colaboradores.ProcesarDistribucion": {"salida": {...}, "lanza": "lanza:Exception:msg"}.
 */
public class ProcesarDistribucion {

	public static class EntradaRecaudacionTraspaso {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_simular;
		public List<JsonElement> p_Recaudacion;

		public EntradaRecaudacionTraspaso() {
			p_Recaudacion = new ArrayList<JsonElement>();
		}
	}

	public static class SalidaRecaudacionTraspaso {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<JsonElement> P_OUT_SOLICITUDES_PROCESADAS;
		public List<JsonElement> P_OUT_SOLICITUDES_NO_PROCESADAS;
		public List<JsonElement> P_OUT_SOLICITUDES_A_PROCESAR;
		public List<JsonElement> P_OUT_RESULTADO;

		public SalidaRecaudacionTraspaso() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<JsonElement>();
			this.P_OUT_SOLICITUDES_PROCESADAS = new ArrayList<JsonElement>();
			this.P_OUT_SOLICITUDES_NO_PROCESADAS = new ArrayList<JsonElement>();
			this.P_OUT_SOLICITUDES_A_PROCESAR = new ArrayList<JsonElement>();
		}
	}

	public SalidaRecaudacionTraspaso logica(Connection con, EntradaRecaudacionTraspaso entrada) throws Exception {
		Hooks.registrar("ProcesarDistribucion", entrada);
		Hooks.objeto("ProcesarDistribucion");
		Hooks.lanzaSiCorresponde("ProcesarDistribucion.lanza");
		if (!Hooks.objeto("ProcesarDistribucion").has("salida")) return new SalidaRecaudacionTraspaso();
		return Utils.getGsonBuilder().fromJson(Hooks.objeto("ProcesarDistribucion").get("salida"), SalidaRecaudacionTraspaso.class);
	}
}
