package cl.bcs.APV;

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
 * ORACLE FAKE of legacy cl.bcs.APV.CambiarEstadoContratosSAAPV (cl/bcs/APV/CambiarEstadoContratosSAAPV.java).
 * The nested POJOs are VERBATIM copies of the legacy source (line ranges in the comment of each one); logica()/call() are fakes
 * that record their input in Hooks and answer what case.json "colaboradores" scripts. Used by AnulacionMandatoPAC and RevocacionMandatoPAC.
 */
public class CambiarEstadoContratosSAAPV implements JavaCall {

	// legacy cl/bcs/APV/CambiarEstadoContratosSAAPV.java:54-75 (verbatim)
	public static class ContratosSAAPV {
		public Long					IdPersona;
		public Long					IdCliente;
		public Long					IdEmpleador;
		public String				IdFolioContratoSAAPV;
		public String				FechaRevocacion; 
		public String				IdEstadoEnvioNotificacion;
		public String				IdEstadoSolicitud;
		public String				IdEstado;
		
	    public Long 				IdGlosa;
	    public String 				DescripcionGlosa;
	    
	    public ContratosSAAPV() {
	    	IdGlosa = 0L;
	    	DescripcionGlosa = "";
	    }
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	// legacy cl/bcs/APV/CambiarEstadoContratosSAAPV.java:107-123 (verbatim)
	public static class ContratosSAAPV_Entrada {
	    public Long				p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_IdModulo;
	    public String			p_Opcion; // REV = Revocacion, CER = Cerrar, COM = Completar 
	    public List<ContratosSAAPV>	p_ContratosSAAPV;
	    public String			p_DesdePac;
	    
	    public ContratosSAAPV_Entrada(){
	    	p_ContratosSAAPV = new ArrayList<ContratosSAAPV>();
	    	p_DesdePac		 = "N";
		}
	   
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	// legacy cl/bcs/APV/CambiarEstadoContratosSAAPV.java:125-139 (verbatim)
	public static class ContratosSAAPV_Salida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					P_OUT_ADVERTENCIASCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ContratosSAAPV_Salida(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_ADVERTENCIASCONTADOR = 0L;
		}		
	};

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception { throw new UnsupportedOperationException("oracle fake"); }


	/**
	 * FAKE of CambiarEstadoContratosSAAPV.logica (legacy writes the contract tables): records its input and answers the script
	 * "colaboradores.cambiarEstadoContratosSAAPV": {"mensajes":[{id,tipo,codigo,descripcion}], "errores": n, "lanza": "text"}.
	 */
	public ContratosSAAPV_Salida logica(Connection con, ContratosSAAPV_Entrada in) throws Exception {
		Hooks.registrar("CambiarEstadoContratosSAAPV", in);
		JsonObject script = Hooks.objeto("cambiarEstadoContratosSAAPV");
		if (script.has("lanza")) throw new Exception(script.get("lanza").getAsString());
		ContratosSAAPV_Salida out = new ContratosSAAPV_Salida();
		if (script.has("mensajes")) {
			for (com.google.gson.JsonElement e : script.getAsJsonArray("mensajes")) out.P_OUT_MENSAJES.add(Hooks.mensaje(e.getAsJsonObject()));
		}
		out.P_OUT_ERRORESCONTADOR = script.has("errores") ? script.get("errores").getAsLong() : 0L;
		return out;
	}

}
