package cl.bcs.plataforma.pau;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarLogValorizadorPauta implements JavaCall {

	private final String SQL_SelectRutaLogValorizadorPauta = loadResourceToString("cl/bcs/plataforma/pau/sql/ConsultarLogValorizadorPauta_SEL_RutaLog.sql");

	public static class ConsultarLogValorizadorPautaEntrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_IdEjecucion;
		public String p_CodigoProceso;
	}

	public static class ConsultarLogValorizadorPautaSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		public String P_OUT_RUTA_LOG;

		public ConsultarLogValorizadorPautaSalida() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJE = "";
			this.P_OUT_RUTA_LOG = "";
		}
	}

	public static class ObjRutaLogValorizadorPauta {
		public String RutaLog;
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		
		Gson gson = Utils.getGsonBuilder();
		ConsultarLogValorizadorPautaEntrada in = gson.fromJson(jin.toString(), ConsultarLogValorizadorPautaEntrada.class);
		ConsultarLogValorizadorPautaSalida out = logica(con, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public ConsultarLogValorizadorPautaSalida logica(Connection con, ConsultarLogValorizadorPautaEntrada in) throws Exception {

		ConsultarLogValorizadorPautaSalida out = new ConsultarLogValorizadorPautaSalida();
		SQL sql = new SQL();

		if (in == null) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "Entrada no informada.";
			return out;
		}

		if (in.p_IdEjecucion == null || in.p_IdEjecucion.equals(0L)) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "IdEjecucion no informado.";
			return out;
		}

		if (in.p_CodigoProceso == null || in.p_CodigoProceso.trim().isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "CodigoProceso no informado.";
			return out;
		}

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdEjecucion", in.p_IdEjecucion);
		params.put("CodigoProceso", in.p_CodigoProceso);

		List<ObjRutaLogValorizadorPauta> rutas = sql.query(con,	SQL_SelectRutaLogValorizadorPauta, params, ObjRutaLogValorizadorPauta.class);

		if (rutas == null || rutas.isEmpty() || rutas.get(0).RutaLog == null || rutas.get(0).RutaLog.trim().isEmpty()) {
			out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "No existe log valorizador para la ejecucion seleccionada.";
			return out;
		}

		out.P_OUT_RUTA_LOG = rutas.get(0).RutaLog;

		return out;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO ConsultarLogValorizadorPauta ==");

		try (Connection con = PlataformaUtils.getPropertiesConnection("master02", "connections.properties")){

			ConsultarLogValorizadorPautaEntrada entrada = new ConsultarLogValorizadorPautaEntrada();

			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 0L;
			entrada.p_IdModulo = "PLA";

			// Cambiar por un IdEjecucion real de la tabla LogValorizadorPauta
			entrada.p_IdEjecucion = 0L;
			
			// Proceso del log valorizador
			entrada.p_CodigoProceso = "VALORIZAR_CARTERA_RF";

			ConsultarLogValorizadorPauta servicio = new ConsultarLogValorizadorPauta();
			ConsultarLogValorizadorPautaSalida salida =	servicio.logica(con, entrada);

			System.out.println("Salida:");
			System.out.println(Utils.printJson(salida, false));

			con.rollback();

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("== FIN ConsultarLogValorizadorPauta ==");
	}
}