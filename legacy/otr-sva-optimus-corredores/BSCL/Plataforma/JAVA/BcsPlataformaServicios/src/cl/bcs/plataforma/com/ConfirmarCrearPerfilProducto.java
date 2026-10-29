package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Wrapper para llamar al stored procedure COM.ConfirmarCrearPerfilProducto
 * Permite migración futura sin modificar código que lo usa
 */
public class ConfirmarCrearPerfilProducto implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConfirmarCrearPerfilProducto.class);

	public static class EntradaConfirmarCrearPerfilProducto {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdPerfilProductos;
		public String p_IdEstadoAutorizacion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}

	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;
	}

	public static class SalidaConfirmarCrearPerfilProducto {
		public Long p_out_AdvertenciasContador;
		public List<AdvertenciaDetalle> p_out_AdvertenciasDetalle;
		public Long p_out_ErroresContador;
		public List<ErrorDetalle> p_out_ErroresDetalle;

		public SalidaConfirmarCrearPerfilProducto() {
			this.p_out_AdvertenciasContador = 0L;
			this.p_out_AdvertenciasDetalle = new ArrayList<>();
			this.p_out_ErroresContador = 0L;
			this.p_out_ErroresDetalle = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarCrearPerfilProducto in = gson.fromJson(jin.toString(), EntradaConfirmarCrearPerfilProducto.class);
		SalidaConfirmarCrearPerfilProducto out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaConfirmarCrearPerfilProducto logica(Connection conn, EntradaConfirmarCrearPerfilProducto in) throws Exception {
		logger.info("INICIO ConfirmarCrearPerfilProducto (Wrapper)");
		SQL sql = new SQL();
		SalidaConfirmarCrearPerfilProducto out = new SalidaConfirmarCrearPerfilProducto();

		try {
			// Preparar parámetros de entrada
			HashMap<String, Object> parmsIN = new HashMap<>();
			parmsIN.put("p_IdEmpresa", in.p_IdEmpresa);
			parmsIN.put("p_IdUsuario", in.p_IdUsuario);
			parmsIN.put("p_IdModulo", in.p_IdModulo);
			parmsIN.put("p_UsaReglasNegocio", in.p_UsaReglasNegocio);
			parmsIN.put("p_IdPerfilProductos", in.p_IdPerfilProductos);
			parmsIN.put("p_IdEstadoAutorizacion", in.p_IdEstadoAutorizacion);

			// Preparar tipos de salida
			HashMap<String, Class> parmsOUT = new HashMap<>();
			parmsOUT.put("p_out_AdvertenciasContador", Long.class);
			parmsOUT.put("p_out_AdvertenciasDetalle", AdvertenciaDetalle.class);
			parmsOUT.put("p_out_ErroresContador", Long.class);
			parmsOUT.put("p_out_ErroresDetalle", ErrorDetalle.class);

			// Ejecutar SP
			logger.debug("CALL COM.ConfirmarCrearPerfilProducto SP");
			String queryCall = "CALL \"BSCL.Plataforma.BD.Servicios::COM.ConfirmarCrearPerfilProducto\" ("
					+ ":p_IdEmpresa,"
					+ ":p_IdUsuario,"
					+ ":p_IdModulo,"
					+ ":p_UsaReglasNegocio,"
					+ ":p_IdPerfilProductos,"
					+ ":p_IdEstadoAutorizacion,"
					+ "@p_out_AdvertenciasContador,"
					+ "$p_out_AdvertenciasDetalle,"
					+ "@p_out_ErroresContador,"
					+ "$p_out_ErroresDetalle)";

			HashMap<String, Object> resultados = sql.call(conn, queryCall, parmsIN, parmsOUT);

			// Mapear resultados
			out.p_out_AdvertenciasContador = (Long) resultados.get("p_out_AdvertenciasContador");
			out.p_out_AdvertenciasDetalle = (List<AdvertenciaDetalle>) resultados.get("p_out_AdvertenciasDetalle");
			out.p_out_ErroresContador = (Long) resultados.get("p_out_ErroresContador");
			out.p_out_ErroresDetalle = (List<ErrorDetalle>) resultados.get("p_out_ErroresDetalle");

			logger.debug("FIN CALL COM.ConfirmarCrearPerfilProducto SP");

		} catch (Exception ex) {
			logger.error("Error en ConfirmarCrearPerfilProducto wrapper: " + ex.getMessage(), ex);
			throw ex;
		}

		logger.info("FIN ConfirmarCrearPerfilProducto (Wrapper)");
		return out;
	}
}
