package cl.bcs.socionegocio.eje;

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

/**
 * Migrado de: EJE.EliminarDetalleCarteraEjecutivo
 * Elimina un detalle de cartera de ejecutivo, filtrando por
 * NombreCarteraEjecutivo + IdEmpresa + IdEmpleadoEjecutivo.
 */
public class EliminarDetalleCarteraEjecutivo implements JavaCall {

	private static final Logger logger = LogManager.getLogger(EliminarDetalleCarteraEjecutivo.class);

	private final String SQL_DELETE_DETALLE = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/eje/sql/EliminarDetalleCarteraEjecutivo_Delete.sql");

	public static class EntradaEliminarDetalleCarteraEjecutivo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_NombreCarteraEjecutivo;
		public Long p_IdEmpleadoEjecutivo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ErrorDetalle {
		public Long IdInterno;
		public String Codigo;
		public String Descripcion;

		public ErrorDetalle(Long idInterno, String codigo, String descripcion) {
			this.IdInterno = idInterno;
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}

	public static class AdvertenciaDetalle {
		public Long IdInterno;
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;

		public AdvertenciaDetalle(Long idInterno, String codigo, String requiereAutorizacion, String descripcion) {
			this.IdInterno = idInterno;
			this.Codigo = codigo;
			this.RequiereAutorizacion = requiereAutorizacion;
			this.Descripcion = descripcion;
		}
	}

	public static class SalidaEliminarDetalleCarteraEjecutivo {
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaEliminarDetalleCarteraEjecutivo() {
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DeleteDetalleCarteraEjecutivo {
		public String NombreCarteraEjecutivo;
		public Long IdEmpresa;
		public Long IdEmpleadoEjecutivo;
	}

	public SalidaEliminarDetalleCarteraEjecutivo logica(Connection conn, EntradaEliminarDetalleCarteraEjecutivo in) throws Exception {
		logger.info("INIT EliminarDetalleCarteraEjecutivo");
		SalidaEliminarDetalleCarteraEjecutivo salida = new SalidaEliminarDetalleCarteraEjecutivo();
		SQL sql = new SQL();

		try {
			DeleteDetalleCarteraEjecutivo deleteParams = new DeleteDetalleCarteraEjecutivo();
			deleteParams.NombreCarteraEjecutivo = in.p_NombreCarteraEjecutivo;
			deleteParams.IdEmpresa = in.p_IdEmpresa;
			deleteParams.IdEmpleadoEjecutivo = in.p_IdEmpleadoEjecutivo;

			List<DeleteDetalleCarteraEjecutivo> deleteList = new ArrayList<>();
			deleteList.add(deleteParams);

			logger.debug("DELETE detalle cartera ejecutivo");
			sql.batch(conn, SQL_DELETE_DETALLE, deleteList, DeleteDetalleCarteraEjecutivo.class);

		} catch (Exception ex) {
			logger.error("Error en EliminarDetalleCarteraEjecutivo: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(0L, "ERROR_ELIMINAR_DETALLE_CARTERA_EJECUTIVO", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END EliminarDetalleCarteraEjecutivo");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaEliminarDetalleCarteraEjecutivo in = gson.fromJson(jin.toString(), EntradaEliminarDetalleCarteraEjecutivo.class);
		SalidaEliminarDetalleCarteraEjecutivo out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"SOC\",\"p_NombreCarteraEjecutivo\":\"CARTERA_TEST\",\"p_IdEmpleadoEjecutivo\":300}";
			EntradaEliminarDetalleCarteraEjecutivo entrada = gson.fromJson(json, EntradaEliminarDetalleCarteraEjecutivo.class);
			EliminarDetalleCarteraEjecutivo demo = new EliminarDetalleCarteraEjecutivo();
			SalidaEliminarDetalleCarteraEjecutivo salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
