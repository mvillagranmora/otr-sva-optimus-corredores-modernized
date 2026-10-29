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

public class ConfirmarCrearPersonaTarjetaBancaria implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConfirmarCrearPersonaTarjetaBancaria.class);
	
	private final String SQL_UPDATE_TARJETA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaTarjetaBancaria_UpdateTarjeraBancaria.sql");
	
	public static class EntradaConfirmarCrearPersonaTarjetaBancaria extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdTarjetaBancaria;
		public String p_IdEstadoAutorizacion;
	}
	
	public static class SalidaConfirmarCrearPersonaTarjetaBancaria extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaConfirmarCrearPersonaTarjetaBancaria() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class TarjetaBancaria extends BcsPojo{
		public Long IdTarjetaBancaria;
		public String IdEstadoAutorizacion;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarCrearPersonaTarjetaBancaria jin = gson.fromJson(in.toString(), EntradaConfirmarCrearPersonaTarjetaBancaria.class);
		SalidaConfirmarCrearPersonaTarjetaBancaria out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConfirmarCrearPersonaTarjetaBancaria logica(Connection con, EntradaConfirmarCrearPersonaTarjetaBancaria entrada) throws Exception {
		logger.info("INICIO::ConfirmarCrearPersonaTarjetaBancaria");
		long inicioServicio = System.currentTimeMillis();
		SalidaConfirmarCrearPersonaTarjetaBancaria out = new SalidaConfirmarCrearPersonaTarjetaBancaria();
		actualizarTarjetaBancaria(con, entrada.p_IdTarjetaBancaria, entrada.p_IdEstadoAutorizacion);
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarCrearPersonaTarjetaBancaria", finServicio - inicioServicio);
		return out;
	}
	
	private void actualizarTarjetaBancaria(Connection con, Long idTarjetaBancaria, String idEstadoAutorizacion) throws Exception {
		logger.debug("INICIO::ConfirmarCrearPersonaTarjetaBancaria.actualizarTarjetaBancaria");
		long inicio = System.currentTimeMillis();
		TarjetaBancaria tarjetaBancaria = new TarjetaBancaria();
		tarjetaBancaria.IdTarjetaBancaria = idTarjetaBancaria;
		tarjetaBancaria.IdEstadoAutorizacion = idEstadoAutorizacion;
		
		List<TarjetaBancaria> tarjetasBancarias = Arrays.asList(tarjetaBancaria);
		SQL sql = new SQL();
		sql.batch(con, SQL_UPDATE_TARJETA_BANCARIA, tarjetasBancarias, TarjetaBancaria.class);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConfirmarCrearPersonaTarjetaBancaria.actualizarTarjetaBancaria", fin - inicio);
	}

}
