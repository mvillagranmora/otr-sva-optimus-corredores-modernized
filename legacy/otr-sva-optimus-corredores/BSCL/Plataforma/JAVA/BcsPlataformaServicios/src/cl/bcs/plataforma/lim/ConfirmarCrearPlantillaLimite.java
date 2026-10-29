package cl.bcs.plataforma.lim;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
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

public class ConfirmarCrearPlantillaLimite implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ConfirmarCrearPlantillaLimite.class);

	private final String SQL_UPDATE_PLANTILLA = loadResourceToString("cl/bcs/plataforma/lim/sql/ConfirmarCrearPlantillaLimite_Update_Plantilla.sql");

	public static class EntradaConfirmarCrearPlantillaLimite {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdPlantillaLimite;
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

	public static class SalidaConfirmarCrearPlantillaLimite {
		public Long p_out_AdvertenciasContador;
		public List<AdvertenciaDetalle> p_out_AdvertenciasDetalle;
		public Long p_out_ErroresContador;
		public List<ErrorDetalle> p_out_ErroresDetalle;

		public SalidaConfirmarCrearPlantillaLimite() {
			this.p_out_AdvertenciasContador = 0L;
			this.p_out_AdvertenciasDetalle = new ArrayList<>();
			this.p_out_ErroresContador = 0L;
			this.p_out_ErroresDetalle = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class UpdatePlantilla {
		public String IdEstadoAutorizacion;
		public String IdPlantillaLimite;
		public Long IdEmpresa;
	}

	public SalidaConfirmarCrearPlantillaLimite logica(Connection conn, EntradaConfirmarCrearPlantillaLimite in) throws Exception {
		logger.info("INIT ConfirmarCrearPlantillaLimite");
		SQL sql = new SQL();
		SalidaConfirmarCrearPlantillaLimite salida = new SalidaConfirmarCrearPlantillaLimite();

		try {
			// Actualizar estado de autorización
			logger.debug("Actualizar estado de autorización");
			UpdatePlantilla updatePlantilla = new UpdatePlantilla();
			updatePlantilla.IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
			updatePlantilla.IdPlantillaLimite = in.p_IdPlantillaLimite;
			updatePlantilla.IdEmpresa = in.p_IdEmpresa;

			List<UpdatePlantilla> updateList = new ArrayList<>();
			updateList.add(updatePlantilla);
			sql.batch(conn, SQL_UPDATE_PLANTILLA, updateList, UpdatePlantilla.class);

		} catch (Exception ex) {
			logger.error("Error en ConfirmarCrearPlantillaLimite: " + ex.getMessage());
			salida.p_out_ErroresDetalle.add(new ErrorDetalle("ERROR_CONFIRMAR_CREAR_PLANTILLA_LIMITE", ex.getMessage()));
			salida.p_out_ErroresContador = (long) salida.p_out_ErroresDetalle.size();
		}

		logger.info("END ConfirmarCrearPlantillaLimite");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarCrearPlantillaLimite in = gson.fromJson(jin.toString(), EntradaConfirmarCrearPlantillaLimite.class);
		SalidaConfirmarCrearPlantillaLimite out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"PLA\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPlantillaLimite\":\"PLANTILLA001\",\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarCrearPlantillaLimite entrada = gson.fromJson(json, EntradaConfirmarCrearPlantillaLimite.class);
			ConfirmarCrearPlantillaLimite demo = new ConfirmarCrearPlantillaLimite();
			SalidaConfirmarCrearPlantillaLimite salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
