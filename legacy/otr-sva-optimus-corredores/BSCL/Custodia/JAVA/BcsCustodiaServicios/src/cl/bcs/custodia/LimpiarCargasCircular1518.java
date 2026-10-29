package cl.bcs.custodia;

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
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

// Boton para SVARESGIN-7050
public class LimpiarCargasCircular1518 implements JavaCall {

	static final Logger logger = LogManager.getLogger(LimpiarCargasCircular1518.class);

	private final String SQL_DELETE_CUADRATURA_CIRCULAR1518 = loadResourceToString("cl/bcs/custodia/sql/LimpiarCargasCircular1518_DeleteCuadraturaCircular1518.sql");
	private final String SQL_UPDATE_CUADRATURA_CIRCULAR1518 = loadResourceToString("cl/bcs/custodia/sql/LimpiarCargasCircular1518_UpdateCuadraturaCircular1518.sql");

	public static class EntradaLimpiarCargasCircular1518 extends BcsPojo {
		public String p_FechaCuadratura;
	}

	public static class ParametroFecha extends BcsPojo {
		public String FechaCuadratura;
	}

	public static class SalidaLimpiarCargasCircular1518 extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long                P_OUT_ERRORESCONTADOR;

		public SalidaLimpiarCargasCircular1518() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
		}
	}


	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaLimpiarCargasCircular1518 in = gson.fromJson(jin.toString(), EntradaLimpiarCargasCircular1518.class);
		SalidaLimpiarCargasCircular1518 out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}


	public SalidaLimpiarCargasCircular1518 logica(Connection con, EntradaLimpiarCargasCircular1518 entrada) throws Exception {
		logger.info("INICIO::LimpiarCargasCircular1518");
		System.out.println("INICIO::LimpiarCargasCircular1518");
		long inicioServicio = System.currentTimeMillis();

		SalidaLimpiarCargasCircular1518 out = new SalidaLimpiarCargasCircular1518();

		limpiarCargaCuadratura(con, entrada.p_FechaCuadratura);

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::LimpiarCargasCircular1518", finServicio - inicioServicio);
		System.out.println("FIN::LimpiarCargasCircular1518");
		return out;
	}


	private void limpiarCargaCuadratura(Connection con, String fechaCuadratura) throws Exception {
		logger.info("INICIO: 1. 0 :LimpiarCargasCircular1518.limpiarCargaCuadratura fecha: "+fechaCuadratura);
		System.out.println("INICIO: 1. 0 :LimpiarCargasCircular1518.limpiarCargaCuadratura fecha: "+fechaCuadratura);
		long inicio = System.currentTimeMillis();

		ParametroFecha param = new ParametroFecha();
		param.FechaCuadratura = fechaCuadratura;
		List<ParametroFecha> cargasCircular1518 = new ArrayList<ParametroFecha>();
		cargasCircular1518.add(param);
		SQL sql = new SQL();
		sql.batch(con, SQL_DELETE_CUADRATURA_CIRCULAR1518, cargasCircular1518, ParametroFecha.class);
		sql.batch(con, SQL_UPDATE_CUADRATURA_CIRCULAR1518, cargasCircular1518, ParametroFecha.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::LimpiarCargasCircular1518.limpiarCargaCuadratura", fin - inicio);
		System.out.println("FIN::LimpiarCargasCircular1518.limpiarCargaCuadratura");
	}

	// Boton para SVARESGIN-7050
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {

			EntradaLimpiarCargasCircular1518 entrada = new EntradaLimpiarCargasCircular1518();
			entrada.p_FechaCuadratura = "2026-06-01";

			LimpiarCargasCircular1518 servicio = new LimpiarCargasCircular1518();

			try {
				SalidaLimpiarCargasCircular1518 salida = servicio.logica(con, entrada);
				System.out.println(salida.toString());
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			} finally {
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}

}
