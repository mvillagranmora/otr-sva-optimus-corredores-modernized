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
 * Migrado de: EJE.EliminarCarteraEjecutivo
 * Elimina una cartera de ejecutivo y sus relaciones (operadores y detalle),
 * filtrando por NombreCarteraEjecutivo + IdEmpresa.
 */
public class EliminarCarteraEjecutivo implements JavaCall {

	private static final Logger logger = LogManager.getLogger(EliminarCarteraEjecutivo.class);

	private final String SQL_DELETE_OPERADOR_CARTERA_EJECUTIVO = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/eje/sql/EliminarCarteraEjecutivo_Delete_Operador.sql");
	private final String SQL_DELETE_DETALLE_CARTERA_EJECUTIVO = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/eje/sql/EliminarCarteraEjecutivo_Delete_Detalle.sql");
	private final String SQL_DELETE_CARTERA_EJECUTIVO = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/eje/sql/EliminarCarteraEjecutivo_Delete_Cartera.sql");

	public static class EntradaEliminarCarteraEjecutivo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_NombreCarteraEjecutivo;

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

	public static class SalidaEliminarCarteraEjecutivo {
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaEliminarCarteraEjecutivo() {
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DeleteCarteraEjecutivo {
		public String NombreCarteraEjecutivo;
		public Long IdEmpresa;
	}

	public SalidaEliminarCarteraEjecutivo logica(Connection conn, EntradaEliminarCarteraEjecutivo in) throws Exception {
		logger.info("INIT EliminarCarteraEjecutivo");
		SalidaEliminarCarteraEjecutivo salida = new SalidaEliminarCarteraEjecutivo();
		SQL sql = new SQL();

		try {
			DeleteCarteraEjecutivo deleteParams = new DeleteCarteraEjecutivo();
			deleteParams.NombreCarteraEjecutivo = in.p_NombreCarteraEjecutivo;
			deleteParams.IdEmpresa = in.p_IdEmpresa;

			List<DeleteCarteraEjecutivo> deleteList = new ArrayList<>();
			deleteList.add(deleteParams);

			// Orden fiel al SP original: primero las tablas dependientes, al final la cabecera
			logger.debug("DELETE operador cartera ejecutivo");
			sql.batch(conn, SQL_DELETE_OPERADOR_CARTERA_EJECUTIVO, deleteList, DeleteCarteraEjecutivo.class);

			logger.debug("DELETE detalle cartera ejecutivo");
			sql.batch(conn, SQL_DELETE_DETALLE_CARTERA_EJECUTIVO, deleteList, DeleteCarteraEjecutivo.class);

			logger.debug("DELETE cartera ejecutivo");
			sql.batch(conn, SQL_DELETE_CARTERA_EJECUTIVO, deleteList, DeleteCarteraEjecutivo.class);

		} catch (Exception ex) {
			logger.error("Error en EliminarCarteraEjecutivo: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(0L, "ERROR_ELIMINAR_CARTERA_EJECUTIVO", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END EliminarCarteraEjecutivo");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaEliminarCarteraEjecutivo in = gson.fromJson(jin.toString(), EntradaEliminarCarteraEjecutivo.class);
		SalidaEliminarCarteraEjecutivo out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"SOC\",\"p_NombreCarteraEjecutivo\":\"CARTERA_TEST\"}";
			EntradaEliminarCarteraEjecutivo entrada = gson.fromJson(json, EntradaEliminarCarteraEjecutivo.class);
			EliminarCarteraEjecutivo demo = new EliminarCarteraEjecutivo();
			SalidaEliminarCarteraEjecutivo salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
