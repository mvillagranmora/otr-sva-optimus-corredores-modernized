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
import cl.bcs.fondomutuo.pojo.ResultadoSolicitud;

/**
 * ORACLE FAKE of legacy cl.bcs.fondomutuo.AnulacionContratoCSC (cl/bcs/fondomutuo/AnulacionContratoCSC.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Used by AnulacionMandatoPAC.
 */
public class AnulacionContratoCSC implements JavaCall {

	// legacy cl/bcs/fondomutuo/AnulacionContratoCSC.java:35-44 (verbatim)
	public static class InContrato{
		public Long 	IdContrato;
		public String 	Estado="";
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	};

	// legacy cl/bcs/fondomutuo/AnulacionContratoCSC.java:67-84 (verbatim)
	public static class EntradaAnulacionContrato {
		
		public 	Long						p_IdEmpresa;
		public	Long						p_IdUsuario;
		public 	String						p_IdModulo;
		public	List<InContrato>			p_Contrato;
		public  String                      p_DesdePac;
		
		
		public EntradaAnulacionContrato(){
			p_Contrato 					= new ArrayList<InContrato>();
			p_DesdePac                  = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}	

	// legacy cl/bcs/fondomutuo/AnulacionContratoCSC.java:87-101 (verbatim)
		public static class SalidaAnulacionContrato{
			public Long P_OUT_ERRORESCONTADOR;
			public List <MensajeSalida> P_OUT_MENSAJES;
			public List <ResultadoSolicitud> P_OUT_RESULTADO;
			
			public SalidaAnulacionContrato(){
				this.P_OUT_ERRORESCONTADOR=0L;
				this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
				this.P_OUT_RESULTADO = new ArrayList<ResultadoSolicitud>();
			}
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }


	/**
	 * FAKE of AnulacionContratoCSC.logica (legacy writes the contract tables): records its input and answers the script
	 * "colaboradores.anulacionContratoCSC": {"mensajes":[{id,tipo,codigo,descripcion}], "errores": n, "lanza": "text"}.
	 */
	public SalidaAnulacionContrato logica(Connection con, EntradaAnulacionContrato in) throws Exception {
		Hooks.registrar("AnulacionContratoCSC", in);
		JsonObject script = Hooks.objeto("anulacionContratoCSC");
		if (script.has("lanza")) throw new Exception(script.get("lanza").getAsString());
		SalidaAnulacionContrato out = new SalidaAnulacionContrato();
		if (script.has("mensajes")) {
			for (com.google.gson.JsonElement e : script.getAsJsonArray("mensajes")) out.P_OUT_MENSAJES.add(Hooks.mensaje(e.getAsJsonObject()));
		}
		out.P_OUT_ERRORESCONTADOR = script.has("errores") ? script.get("errores").getAsLong() : 0L;
		return out;
	}

}
