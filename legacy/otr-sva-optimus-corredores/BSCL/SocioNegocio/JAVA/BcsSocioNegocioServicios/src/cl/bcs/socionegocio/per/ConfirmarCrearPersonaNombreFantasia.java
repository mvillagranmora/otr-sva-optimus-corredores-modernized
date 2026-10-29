package cl.bcs.socionegocio.per;

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

public class ConfirmarCrearPersonaNombreFantasia implements JavaCall {

	static final Logger logger = LogManager.getLogger(ConfirmarCrearPersonaNombreFantasia.class);
	
	private final String SQL_UPDATE_NOMBRE_FANTASIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaNombreFantasia_InsertNombreFantasia.sql");
	
	public static class EntradaConfirmarCrearPersonaNombreFantasia extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdNombreFantasia;
		public String p_IdEstadoAutorizacion;
	}
	
	public static class SalidaConfirmarCrearPersonaNombreFantasia extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaConfirmarCrearPersonaNombreFantasia() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class NombreFantasia extends BcsPojo{
		public Long IdNombreFantasia;
		public String IdEstadoAutorizacion;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarCrearPersonaNombreFantasia jin = gson.fromJson(in.toString(), EntradaConfirmarCrearPersonaNombreFantasia.class);
		SalidaConfirmarCrearPersonaNombreFantasia out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConfirmarCrearPersonaNombreFantasia logica(Connection con, EntradaConfirmarCrearPersonaNombreFantasia entrada) throws Exception {
		logger.info("INICIO::ConfirmarCrearPersonaNombreFantasia");
		long inicioServicio = System.currentTimeMillis();
		SalidaConfirmarCrearPersonaNombreFantasia out = new SalidaConfirmarCrearPersonaNombreFantasia();
		actualizarNombreFantasia(con, entrada.p_IdNombreFantasia, entrada.p_IdEstadoAutorizacion);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarCrearPersonaNombreFantasia", finServicio - inicioServicio);
		return out;
	}
	
	private void actualizarNombreFantasia(Connection con, Long idNombreFantasia, String idEstadoAutorizacion) throws Exception {
		logger.debug("INICIO::ConfirmarCrearPersonaNombreFantasia.actualizarTarjetaBancaria");
		long inicio = System.currentTimeMillis();
		NombreFantasia nombreFantasia = new NombreFantasia();
		nombreFantasia.IdNombreFantasia = idNombreFantasia;
		nombreFantasia.IdEstadoAutorizacion = idEstadoAutorizacion;
		
		List<NombreFantasia> nombresFantasia = Arrays.asList(nombreFantasia);
		
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_NOMBRE_FANTASIA, nombresFantasia, NombreFantasia.class);
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConfirmarCrearPersonaNombreFantasia.actualizarTarjetaBancaria", fin - inicio);
	}
}
