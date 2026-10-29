package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarCostoUFCarga implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CertificarSolicitudAporteRescate.class);
	
	private final String SQL_UPDATE_RESCATES = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUFCarga_UpdateAportes.sql");
	private final String SQL_UPDATE_APORTES = loadResourceToString("cl/bcs/fondomutuo/sql/ActualizarCostoUFCarga_UpdateRescates.sql");
	
	public static class EntradaActualizarCostoUFCarga{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaActualizarCostoUFCarga{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaActualizarCostoUFCarga() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaActualizarCostoUFCarga jin = gson.fromJson(in.toString(), EntradaActualizarCostoUFCarga.class);
		SalidaActualizarCostoUFCarga out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaActualizarCostoUFCarga logica (Connection con, EntradaActualizarCostoUFCarga entrada) throws Exception {
		logger.info("INICIO::CalcularCostoUFAportes");
		SalidaActualizarCostoUFCarga out = new SalidaActualizarCostoUFCarga();
		
		SQL.exec(con, SQL_UPDATE_APORTES);
		SQL.exec(con, SQL_UPDATE_RESCATES);
		
		logger.info("FIN::CalcularCostoUFAportes");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			ActualizarCostoUFCarga demo = new ActualizarCostoUFCarga();
			EntradaActualizarCostoUFCarga entrada = new EntradaActualizarCostoUFCarga();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 21L;
			entrada.p_IdModulo = "FMM";
			System.out.println(demo.logica(con, entrada));
		}catch(Exception er) {
			er.printStackTrace();
		}
	}
}
