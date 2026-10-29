package cl.bcs.plataforma.aut;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RequiereEventoAutorizacion implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(RequiereEventoAutorizacion.class);
	private final String SQL_SELECT_REQUIERE_AUTORIZACION = loadResourceToString("cl/bcs/plataforma/aut/sql/RequiereEventoAutorizacion_SelectRequiereAutorizacion.sql");
	
	public static class EntradaRequiereEventoAutorizacion extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_IdConcepto;
	}
	
	public static class SalidaRequiereEventoAutorizacion extends BcsPojo{
		public String P_OUT_AUTORIZACION;
	}
	
	public static class RequiereAutorizacion extends BcsPojo{
		public String RequiereAutorizacion;

		public RequiereAutorizacion() {
			this.RequiereAutorizacion = "N";
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaRequiereEventoAutorizacion jin = gson.fromJson(in.toString(), EntradaRequiereEventoAutorizacion.class);
		SalidaRequiereEventoAutorizacion out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaRequiereEventoAutorizacion logica(Connection con, EntradaRequiereEventoAutorizacion entrada) throws Exception {
		logger.info("INICIO::RequiereEventoAutorizacion");
		long inicioServicio = System.currentTimeMillis();
		SalidaRequiereEventoAutorizacion out = new SalidaRequiereEventoAutorizacion();
		out.P_OUT_AUTORIZACION = obtenerRequiereAutorizacion(con, entrada.p_IdEmpresa, entrada.p_IdConcepto);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::RequiereEventoAutorizacion", finServicio - inicioServicio);
		return out;
	}
	
	private String obtenerRequiereAutorizacion(Connection con, Long idEmpresa, String idConcepto) throws Exception {
		logger.debug("INICIO::RequiereEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(2);
		parametros.put("IdEmpresa", idEmpresa);
		parametros.put("IdConcepto", idConcepto);
		SQL sql = new SQL();
		List<RequiereAutorizacion> requiereAutorizaciones = sql.query(con, SQL_SELECT_REQUIERE_AUTORIZACION, parametros, RequiereAutorizacion.class);
		RequiereAutorizacion requiereAutorizacion = requiereAutorizaciones.parallelStream().findFirst().orElse(new RequiereAutorizacion());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RequiereEventoAutorizacion", fin - inicio);
		return requiereAutorizacion.RequiereAutorizacion;
	}
	
}
