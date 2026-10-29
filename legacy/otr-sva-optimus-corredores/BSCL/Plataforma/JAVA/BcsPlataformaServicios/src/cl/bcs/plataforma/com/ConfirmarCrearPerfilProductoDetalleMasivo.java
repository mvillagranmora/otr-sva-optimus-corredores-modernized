package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Migrado de: COM.ConfirmarCrearPerfilProductoDetalleMasivo
 * Actualiza el IdEstadoAutorizacion de un conjunto de DetallePerfilProductos.
 */
public class ConfirmarCrearPerfilProductoDetalleMasivo implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ConfirmarCrearPerfilProductoDetalleMasivo.class);

	private final String SQL_UPDATE_DETALLE_PERFIL_PRODUCTOS = PlataformaUtils.loadResourceToString(
		"cl/bcs/plataforma/com/sql/ConfirmarCrearPerfilProductoDetalleMasivo_Update_DetallePerfilProductos.sql");

	public static class Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<DetallePerfilProductoItem> p_DetallePerfilProductos;
		public String p_IdEstadoAutorizacion;

		public Entrada() {
			this.p_DetallePerfilProductos = new ArrayList<>();
		}
	}

	public static class DetallePerfilProductoItem {
		public Long IdDetallePerfilProductos;
	}

	public static class UpdateDetallePerfilProducto {
		public Long IdDetallePerfilProductos;
		public String IdEstadoAutorizacion;
	}

	public static class AdvertenciaDetalle {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}

	public static class ErrorDetalle {
		public String Codigo;
		public String Descripcion;

		public ErrorDetalle() {
		}

		public ErrorDetalle(String codigo, String descripcion) {
			this.Codigo = codigo;
			this.Descripcion = descripcion;
		}
	}

	public static class Salida {
		public Long P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public Long P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public Salida() {
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
	}

	public Salida logica(Connection conn, Entrada in) throws Exception {
		logger.info("INICIO::ConfirmarCrearPerfilProductoDetalleMasivo");
		Salida salida = new Salida();
		SQL sql = new SQL(); // única instancia reutilizable

		try {
			List<UpdateDetallePerfilProducto> updates = new ArrayList<>();
			for (DetallePerfilProductoItem item : in.p_DetallePerfilProductos) {
				UpdateDetallePerfilProducto update = new UpdateDetallePerfilProducto();
				update.IdDetallePerfilProductos = item.IdDetallePerfilProductos;
				update.IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
				updates.add(update);
			}

			if (!updates.isEmpty()) {
				sql.batch(conn, SQL_UPDATE_DETALLE_PERFIL_PRODUCTOS, updates, UpdateDetallePerfilProducto.class);
			}
		} catch (Exception e) {
			logger.error("Error en ConfirmarCrearPerfilProductoDetalleMasivo: " + e.getMessage(), e);
			salida.P_OUT_ERRORESCONTADOR = 1L;
			String descripcion = obtenerMensajeDiccionario(conn, in.p_IdEmpresa, "ERROR_CONFIRMAR_PERFIL_PRODUCTO_DETALLE", e.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CONFIRMAR_PERFIL_PRODUCTO_DETALLE", descripcion));
		}

		logger.info("FIN::ConfirmarCrearPerfilProductoDetalleMasivo");
		return salida;
	}

	private String obtenerMensajeDiccionario(Connection conn, Long idEmpresa, String codigo, String fallback) {
		try {
			return Diccionario.getMensaje(conn, idEmpresa, codigo).msg();
		} catch (Exception ex) {
			logger.warn("No se pudo resolver mensaje diccionario {}: {}", codigo, ex.getMessage());
			return fallback;
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		Entrada in = gson.fromJson(jin.toString(), Entrada.class);
		Salida out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO TEST ==");

		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				Gson gson = Utils.getGsonBuilder();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"COM\",\"p_UsaReglasNegocio\":\"N\",\"p_DetallePerfilProductos\":[{\"IdDetallePerfilProductos\":1001},{\"IdDetallePerfilProductos\":1002}],\"p_IdEstadoAutorizacion\":\"AUT\"}";
				Entrada entrada = gson.fromJson(json, Entrada.class);

				ConfirmarCrearPerfilProductoDetalleMasivo servicio = new ConfirmarCrearPerfilProductoDetalleMasivo();

				System.out.println("ENTRADA");
				System.out.println(gson.toJson(entrada));
				System.out.println("SALIDA");

				Salida salida = servicio.logica(conn, entrada);
				System.out.println(gson.toJson(salida));
				System.out.println("Errores: " + salida.P_OUT_ERRORESCONTADOR);

				conn.rollback();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				conn.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("== FIN TEST ==");
	}
}
