package cl.bcs.tesoreria;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import cl.bcs.oracle.Hooks;
import cl.bcs.plataforma.ftp.FileManager.Cabecera;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

/**
 * ORACLE FAKE of legacy cl.bcs.tesoreria.CargaMasivaIngresoEgresoDAPFFMM (Tesoreria/.../CargaMasivaIngresoEgresoDAPFFMM.java:60-92, 814-830).
 * Entrada / Salida verbatim. getCabeceras records {tipoArchivoCarga, fechaEjecucion, idEstado} and answers as many default Cabecera
 * as "colaboradores.cabeceras.<idEstado>" says (default 0), "colaboradores.getCabeceras.lanza" scripts a failure.
 * logica records its input and answers "colaboradores.CargaMasivaIngresoEgresoDAPFFMM": {"salida": {...}} (absent: the default empty Salida),
 * "lanza" scripts a failure.
 */
public class CargaMasivaIngresoEgresoDAPFFMM {

	public static class EntradaCargaMasivaIngresoEgresoDAPFFMM {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_TipoArchivoCarga;
		public String p_EnviarArchivoACasilla;
		public String p_FechaCarga;
		public String p_IdModulo = "TES";
		public String p_SimularOperacion = "N";
		public String p_UsaReglasNegocio = "S";

		public EntradaCargaMasivaIngresoEgresoDAPFFMM() {
			this.p_IdEmpresa = 1L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "TES";
			this.p_SimularOperacion = "N";
			this.p_UsaReglasNegocio = "S";
		}
	}

	public static class SalidaCargaMasivaIngresoEgresoDAPFFMM {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_RUTA_ARCHIVO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_REGISTROSINGRESADOSCONTADOR;

		public SalidaCargaMasivaIngresoEgresoDAPFFMM() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_REGISTROSINGRESADOSCONTADOR = 0L;
			this.P_OUT_RUTA_ARCHIVO = "";
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}

	public SalidaCargaMasivaIngresoEgresoDAPFFMM logica(Connection con, EntradaCargaMasivaIngresoEgresoDAPFFMM entrada) throws Exception {
		Hooks.registrar("CargaMasivaIngresoEgresoDAPFFMM", entrada);
		Hooks.lanzaSiCorresponde("CargaMasivaIngresoEgresoDAPFFMM.lanza");
		if (!Hooks.objeto("CargaMasivaIngresoEgresoDAPFFMM").has("salida")) return new SalidaCargaMasivaIngresoEgresoDAPFFMM();
		return Utils.getGsonBuilder().fromJson(Hooks.objeto("CargaMasivaIngresoEgresoDAPFFMM").get("salida"), SalidaCargaMasivaIngresoEgresoDAPFFMM.class);
	}

	public List<Cabecera> getCabeceras(Connection con, String tipoArchivoCarga, String fechaEjecucion, String idEstado) throws Exception {
		com.google.gson.JsonObject o = new com.google.gson.JsonObject();
		o.addProperty("tipoArchivoCarga", tipoArchivoCarga);
		o.addProperty("fechaEjecucion", fechaEjecucion);
		o.addProperty("idEstado", idEstado);
		Hooks.registrarJson("CargaMasivaIngresoEgresoDAPFFMM.getCabeceras", o);
		Hooks.lanzaSiCorresponde("getCabeceras.lanza");
		List<Cabecera> l = new ArrayList<Cabecera>();
		long n = Hooks.objeto("cabeceras").has(idEstado) ? Hooks.objeto("cabeceras").get(idEstado).getAsLong() : 0L;
		for (long i = 0; i < n; i++) l.add(new Cabecera());
		return l;
	}
}
