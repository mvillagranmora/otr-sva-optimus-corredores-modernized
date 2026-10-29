package cl.bcs.socionegocio.cli;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarCrearDeclaracionOrigenFondos implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConfirmarCrearDeclaracionOrigenFondos.class);
	
	private final String SQL_UPDATE_DECLARACION_ORIGEN_FONDOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConfirmarCrearDeclaracionOrigenFondos_UpdateDeclaracionOrigenFondos.sql");
	
	public static class EntradaConfirmarCrearDeclaracionOrigenFondos extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDeclaracionOrigenFondos;
		public String p_IdEstadoAutorizacion;
	}
	
	public static class SalidaConfirmarCrearDeclaracionOrigenFondos extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaConfirmarCrearDeclaracionOrigenFondos() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class DeclaracionOrigenFondos extends BcsPojo{
		public Long IdDeclaracionOrigenFondos;
		public String IdEstadoAutorizacion;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarCrearDeclaracionOrigenFondos jin = gson.fromJson(in.toString(), EntradaConfirmarCrearDeclaracionOrigenFondos.class);
		SalidaConfirmarCrearDeclaracionOrigenFondos out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
        return dataout;
	}
	
	public SalidaConfirmarCrearDeclaracionOrigenFondos logica(Connection con, EntradaConfirmarCrearDeclaracionOrigenFondos entrada) throws Exception {
		logger.debug("INICIO::ConfirmarCrearDeclaracionOrigenFondos");
		long inicioServicio = System.currentTimeMillis();
		SalidaConfirmarCrearDeclaracionOrigenFondos out = new SalidaConfirmarCrearDeclaracionOrigenFondos();
		actualizarDeclaracionOrigenFondos(con, entrada.p_IdDeclaracionOrigenFondos, entrada.p_IdEstadoAutorizacion);
		long finServicio = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarCrearDeclaracionOrigenFondos", finServicio - inicioServicio);
		return out;
	}
	
	private void actualizarDeclaracionOrigenFondos(Connection con, Long idDeclaracionOrigenFondos, String idEstadoAutorizacion) throws Exception {
		logger.debug("INICIO::ConfirmarCrearDeclaracionOrigenFondos.actualizarDeclaracionOrigenFondos");
		long inicio = System.currentTimeMillis();
		DeclaracionOrigenFondos declaracionOrigenFondos = new DeclaracionOrigenFondos();
		declaracionOrigenFondos.IdDeclaracionOrigenFondos = idDeclaracionOrigenFondos;
		declaracionOrigenFondos.IdEstadoAutorizacion = idEstadoAutorizacion;
		List<DeclaracionOrigenFondos> declaracionesFondos = Arrays.asList(declaracionOrigenFondos);
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_DECLARACION_ORIGEN_FONDOS, declaracionesFondos, DeclaracionOrigenFondos.class);
		long fin = System.currentTimeMillis();
		logger.debug("INICIO::ConfirmarCrearDeclaracionOrigenFondos.actualizarDeclaracionOrigenFondos", fin - inicio);
	}
	

}
