package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import java.util.Arrays;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

/**
 * Wrapper para llamar al stored procedure COM.ConfirmarCrearPerfilCobrosDetalle
 */
public class ConfirmarCrearPerfilCobrosDetalle implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConfirmarCrearPerfilCobrosDetalle.class);
	private final String SQL_UPDATE_ESTADO = PlataformaUtils.loadResourceToString("cl/bcs/plataforma/com/sql/ConfirmarCrearPerfilCobrosDetalle_UpdateEstado.sql");	

	public static class EntradaConfirmarCrearPerfilCobrosDetalle {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDetallePerfilCobros;
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
	
	public static class DetallePerfilCobrosUpdateEstado {
		public String p_IdEstadoAutorizacion;
		public Long p_IdDetallePerfilCobros;
	}	

	public static class SalidaConfirmarCrearPerfilCobrosDetalle {
		public Long p_out_AdvertenciasContador;
		public List<AdvertenciaDetalle> p_out_AdvertenciasDetalle;
		public Long p_out_ErroresContador;
		public List<ErrorDetalle> p_out_ErroresDetalle;

		public SalidaConfirmarCrearPerfilCobrosDetalle() {
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
		EntradaConfirmarCrearPerfilCobrosDetalle in = gson.fromJson(jin.toString(), EntradaConfirmarCrearPerfilCobrosDetalle.class);
		SalidaConfirmarCrearPerfilCobrosDetalle out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaConfirmarCrearPerfilCobrosDetalle logica(Connection conn, EntradaConfirmarCrearPerfilCobrosDetalle in) throws Exception {
		logger.info("INICIO ConfirmarCrearPerfilCobrosDetalle (Wrapper)");
		SQL sql = new SQL();
		SalidaConfirmarCrearPerfilCobrosDetalle out = new SalidaConfirmarCrearPerfilCobrosDetalle();

		try {
			// Migrado de SP a Java: UPDATE directo del estado de autorizacion del detalle de perfil de cobro 	
			DetallePerfilCobrosUpdateEstado update = new DetallePerfilCobrosUpdateEstado(); 			
			update.p_IdEstadoAutorizacion = in.p_IdEstadoAutorizacion; 			
			update.p_IdDetallePerfilCobros = in.p_IdDetallePerfilCobros; 			
			List<DetallePerfilCobrosUpdateEstado> detallesUpdate = Arrays.asList(update); 			
			logger.debug("UPDATE COM.DetallePerfilCobros - IdDetallePerfilCobros: " + in.p_IdDetallePerfilCobros); 			
			sql.batch(conn, SQL_UPDATE_ESTADO, detallesUpdate, DetallePerfilCobrosUpdateEstado.class);

		} catch (Exception ex) {
			logger.error("Error en ConfirmarCrearPerfilCobrosDetalle: " + ex.getMessage(), ex);
			throw ex;
		}
		logger.info("FIN ConfirmarCrearPerfilCobrosDetalle (Wrapper)");
		return out;
	}	
}