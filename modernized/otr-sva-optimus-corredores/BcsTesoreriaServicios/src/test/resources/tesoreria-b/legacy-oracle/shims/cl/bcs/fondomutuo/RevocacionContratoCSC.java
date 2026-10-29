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
 * ORACLE FAKE of legacy cl.bcs.fondomutuo.RevocacionContratoCSC (cl/bcs/fondomutuo/RevocacionContratoCSC.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Used by RevocacionMandatoPAC.
 */
public class RevocacionContratoCSC implements JavaCall {

	// legacy cl/bcs/fondomutuo/RevocacionContratoCSC.java:38-47 (verbatim)
	public static class InContrato{
		public Long 	IdContrato;
		public String 	Estado="";
		public String   FolioCSC;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};

	// legacy cl/bcs/fondomutuo/RevocacionContratoCSC.java:89-103 (verbatim)
	public static class EntradaRevocacionContrato {		
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	List<InContrato>			p_Contrato;	
		public  String						p_DesdePac;
		
		public EntradaRevocacionContrato(){
			p_Contrato 					= new ArrayList<InContrato>();
			p_DesdePac					= "N";
		}	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	// legacy cl/bcs/fondomutuo/RevocacionContratoCSC.java:122-124 (verbatim)
	public static class ResultadoRevocacionCSC{
		public Long IdFolioCSC;
	}

	// legacy cl/bcs/fondomutuo/RevocacionContratoCSC.java:106-120 (verbatim)
	public static class SalidaRevocacionContrato{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoRevocacionCSC> P_OUT_RESULTADO;
			
		public SalidaRevocacionContrato(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoRevocacionCSC>();
		}
			
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }


	/**
	 * FAKE of RevocacionContratoCSC.logica (legacy writes the contract tables): records its input and answers the script
	 * "colaboradores.revocacionContratoCSC": {"mensajes":[{id,tipo,codigo,descripcion}], "errores": n, "lanza": "text"}.
	 */
	public SalidaRevocacionContrato logica(Connection con, EntradaRevocacionContrato in) throws Exception {
		Hooks.registrar("RevocacionContratoCSC", in);
		JsonObject script = Hooks.objeto("revocacionContratoCSC");
		if (script.has("lanza")) throw new Exception(script.get("lanza").getAsString());
		SalidaRevocacionContrato out = new SalidaRevocacionContrato();
		if (script.has("mensajes")) {
			for (com.google.gson.JsonElement e : script.getAsJsonArray("mensajes")) out.P_OUT_MENSAJES.add(Hooks.mensaje(e.getAsJsonObject()));
		}
		out.P_OUT_ERRORESCONTADOR = script.has("errores") ? script.get("errores").getAsLong() : 0L;
		return out;
	}

}
