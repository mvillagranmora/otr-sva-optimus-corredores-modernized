package cl.bcs.plataforma.bat;

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
 * ORACLE FAKE of legacy cl.bcs.plataforma.bat.AgregarPrograma (cl/bcs/plataforma/bat/AgregarPrograma.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Used by CrearCargaMasivaPacRen (batch scheduling).
 */
public class AgregarPrograma implements JavaCall {

	// legacy cl/bcs/plataforma/bat/AgregarPrograma.java:99-117 (verbatim)
	public static class InAgregarPrograma {
		public Long 			p_IdEmpresa;
		public Long 			p_IdUsuario;
		public String			p_IdModulo;
		public String			p_IdTipoPrograma;
		public Long				p_Segundos;
		public String			p_FechaSistema;
		public List<Parametro>	p_Parametros;
		public String			p_Comentario;
		public static class Parametro{
			public String	NombreParametro;
			public String	Valor;
			public String	ValorBlob;
		}
		public InAgregarPrograma(){
			this.p_Parametros = new ArrayList<Parametro>();
			this.p_Comentario = "";
		}
	}

	// legacy cl/bcs/plataforma/bat/AgregarPrograma.java:119-124 (verbatim)
	public static class OutAgregarPrograma {
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_IDPROGRAMA;
		public Long P_OUT_IDGRUPOPARAMETRO;
		public String P_OUT_MENSAJE;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }


	/** FAKE of AgregarPrograma.logica (legacy schedules a batch program): records its input and answers {"idPrograma": n (default 9001)} from "colaboradores.agregarPrograma"; "lanza": "text". */
	public OutAgregarPrograma logica(Connection con, InAgregarPrograma in) throws Exception {
		Hooks.registrar("AgregarPrograma", in);
		JsonObject script = Hooks.objeto("agregarPrograma");
		if (script.has("lanza")) throw new Exception(script.get("lanza").getAsString());
		OutAgregarPrograma out = new OutAgregarPrograma();
		out.P_OUT_IDPROGRAMA = script.has("idPrograma") ? script.get("idPrograma").getAsLong() : 9001L;
		return out;
	}

}
