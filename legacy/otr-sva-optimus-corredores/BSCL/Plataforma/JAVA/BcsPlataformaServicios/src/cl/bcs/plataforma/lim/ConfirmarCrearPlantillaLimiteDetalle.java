package cl.bcs.plataforma.lim;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarCrearPlantillaLimiteDetalle implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConfirmarCrearPlantillaLimiteDetalle.class);

	private final String SQL_UPDATE_DETALLE = loadResourceToString("cl/bcs/plataforma/lim/sql/ConfirmarCrearPlantillaLimiteDetalle_Update_Detalle.sql");

	public static class EntradaConfirmarCrearPlantillaLimiteDetalle {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdLimite;
		public String p_IdEstadoAutorizacion;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

		public AdvertenciaDetalle(String codigo, String requiereAutorizacion, String descripcion) {
			this.Codigo = codigo;
			this.RequiereAutorizacion = requiereAutorizacion;
			this.Descripcion = descripcion;
		}
	}

	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;

		public ErrorDetalle(String codigo, String descripcion) {
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}

	public static class SalidaConfirmarCrearPlantillaLimiteDetalle {
		public Long p_out_AdvertenciasContador;
		public List<AdvertenciaDetalle> p_out_AdvertenciasDetalle;
		public Long p_out_ErroresContador;
		public List<ErrorDetalle> p_out_ErroresDetalle;

		public SalidaConfirmarCrearPlantillaLimiteDetalle() {
			this.p_out_AdvertenciasContador = 0L;
			this.p_out_AdvertenciasDetalle = new ArrayList<>();
			this.p_out_ErroresContador = 0L;
			this.p_out_ErroresDetalle = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class UpdateDetalle {
		public String IdEstadoAutorizacion;
		public Long IdLimite;
	}

	public SalidaConfirmarCrearPlantillaLimiteDetalle logica(Connection conn, EntradaConfirmarCrearPlantillaLimiteDetalle in) throws Exception {
		logger.info("INIT ConfirmarCrearPlantillaLimiteDetalle");
		SQL sql = new SQL();
		SalidaConfirmarCrearPlantillaLimiteDetalle salida = new SalidaConfirmarCrearPlantillaLimiteDetalle();

		try {
			// Actualizar estado de autorización
			logger.debug("Actualizar estado de autorización");
			UpdateDetalle updateDetalle = new UpdateDetalle();
			updateDetalle.IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
			updateDetalle.IdLimite = in.p_IdLimite;

			List<UpdateDetalle> updateList = new ArrayList<>();
			updateList.add(updateDetalle);
			sql.batch(conn, SQL_UPDATE_DETALLE, updateList, UpdateDetalle.class);

		} catch (Exception ex) {
			logger.error("Error en ConfirmarCrearPlantillaLimiteDetalle: " + ex.getMessage());
			salida.p_out_ErroresDetalle.add(new ErrorDetalle("ERROR_CONFIRMAR_CREAR_PLANTILLA_LIMITE_DETALLE", ex.getMessage()));
			salida.p_out_ErroresContador = (long) salida.p_out_ErroresDetalle.size();
		}

		logger.info("END ConfirmarCrearPlantillaLimiteDetalle");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarCrearPlantillaLimiteDetalle in = gson.fromJson(jin.toString(), EntradaConfirmarCrearPlantillaLimiteDetalle.class);
		SalidaConfirmarCrearPlantillaLimiteDetalle out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdLimite\":1,\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarCrearPlantillaLimiteDetalle entrada = gson.fromJson(json, EntradaConfirmarCrearPlantillaLimiteDetalle.class);
			ConfirmarCrearPlantillaLimiteDetalle demo = new ConfirmarCrearPlantillaLimiteDetalle();
			SalidaConfirmarCrearPlantillaLimiteDetalle salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
