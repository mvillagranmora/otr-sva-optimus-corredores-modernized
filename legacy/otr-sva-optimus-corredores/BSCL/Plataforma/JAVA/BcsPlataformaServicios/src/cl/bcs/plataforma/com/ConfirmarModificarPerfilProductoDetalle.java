package cl.bcs.plataforma.com;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.EntradaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
 
/**

* Wrapper migrado para COM.ConfirmarModificarPerfilProductoDetalle
* Aplica los cambios pendientes en PLA.SolicitudCambio sobre COM.DetallePerfilProductos
* y cierra la solicitud vía la clase Java ModificarEstadoRegistroSolicitudCambio.
*/

public class ConfirmarModificarPerfilProductoDetalle implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ConfirmarModificarPerfilProductoDetalle.class);
 
	private final String SQL_SELECT_SOLICITUD = PlataformaUtils.loadResourceToString(
		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProductoDetalle_Select_SolicitudCambio.sql");

	private final String SQL_UPDATE_IDTIPOPRODUCTO = PlataformaUtils.loadResourceToString(
		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProductoDetalle_Update_IdTipoProducto.sql");

	private final String SQL_UPDATE_IDTIPOOPERACION = PlataformaUtils.loadResourceToString(
		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProductoDetalle_Update_IdTipoOperacion.sql");

	private final String SQL_UPDATE_IDCANALINGRESO = PlataformaUtils.loadResourceToString(
		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProductoDetalle_Update_IdCanalIngreso.sql");

	private final String SQL_UPDATE_IDMONEDA = PlataformaUtils.loadResourceToString(
		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProductoDetalle_Update_IdMoneda.sql");

	private final String SQL_UPDATE_IDGRUPOACTIVOS = PlataformaUtils.loadResourceToString(
		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProductoDetalle_Update_IdGrupoActivos.sql");

	private final String SQL_UPDATE_IDESTADO = PlataformaUtils.loadResourceToString(
		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProductoDetalle_Update_IdEstado.sql");

	private final String SQL_UPDATE_PERMITERUTEO = PlataformaUtils.loadResourceToString(
		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProductoDetalle_Update_PermiteRuteo.sql");
 
	public static class EntradaConfirmarModificarPerfilProductoDetalle {

		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDetallePerfilProductos;
		public String p_IdEstadoAutorizacion;
 
		public String toString() {

			return Utils.printJson(this, false);
		}
	} 
	public static class SolicitudCambioRow {

		public String CampoModificado;
		public String ValorNuevo;
	} 
	public static class UpdateCampo {

		public String p_ValorNuevo;
		public Long p_IdDetallePerfilProductos;
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
	public static class AdvertenciaDetalle {

		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	} 
	public static class SalidaConfirmarModificarPerfilProductoDetalle {

		public Long p_out_AdvertenciasContador;
		public List<AdvertenciaDetalle> p_out_AdvertenciasDetalle;
		public Long p_out_ErroresContador;
		public List<ErrorDetalle> p_out_ErroresDetalle;
 
		public SalidaConfirmarModificarPerfilProductoDetalle() {

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

		EntradaConfirmarModificarPerfilProductoDetalle in = gson.fromJson(jin.toString(),
		EntradaConfirmarModificarPerfilProductoDetalle.class);

		SalidaConfirmarModificarPerfilProductoDetalle out = logica(conn, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	} 
	public SalidaConfirmarModificarPerfilProductoDetalle logica(Connection conn,

			EntradaConfirmarModificarPerfilProductoDetalle in) throws Exception {

		logger.info("INICIO ConfirmarModificarPerfilProductoDetalle");

		SQL sql = new SQL();

		SalidaConfirmarModificarPerfilProductoDetalle out = new SalidaConfirmarModificarPerfilProductoDetalle();
 
		try {

			String idEntidadOrigen = String.valueOf(in.p_IdDetallePerfilProductos);
 
			// 1. Traer la solicitud pendiente (CampoModificado / ValorNuevo) para este detalle

			HashMap<String, Object> parmsSelect = new HashMap<>();

			parmsSelect.put("p_IdEntidadOrigen", idEntidadOrigen);
			parmsSelect.put("p_IdTipoEntidad", "PERFILPRODUCTODETALLE");
 
			List<SolicitudCambioRow> solicitud = sql.query(conn, SQL_SELECT_SOLICITUD, parmsSelect,

				SolicitudCambioRow.class);
 
			if (solicitud == null || solicitud.isEmpty()) {

				out.p_out_ErroresContador = 1L;
				out.p_out_ErroresDetalle.add(new ErrorDetalle("SOLICITUD_NO_EXISTE",

					"No existe solicitud pendiente para IdDetallePerfilProductos=" + idEntidadOrigen));

				logger.info("FIN ConfirmarModificarPerfilProductoDetalle (sin solicitud)");

				return out;
			} 
			// 2. Aplicar UPDATE condicional por cada campo presente en la solicitud

			for (SolicitudCambioRow row : solicitud) {

				UpdateCampo update = new UpdateCampo();
				update.p_ValorNuevo = row.ValorNuevo;
				update.p_IdDetallePerfilProductos = in.p_IdDetallePerfilProductos;
 
				switch (row.CampoModificado) {

					case "IdTipoProducto":
						sql.batch(conn, SQL_UPDATE_IDTIPOPRODUCTO, Arrays.asList(update), UpdateCampo.class);
						break;

					case "IdTipoOperacion":
						sql.batch(conn, SQL_UPDATE_IDTIPOOPERACION, Arrays.asList(update), UpdateCampo.class);
						break;

					case "IdCanalIngreso":
						sql.batch(conn, SQL_UPDATE_IDCANALINGRESO, Arrays.asList(update), UpdateCampo.class);
						break;

					case "IdMoneda":
						sql.batch(conn, SQL_UPDATE_IDMONEDA, Arrays.asList(update), UpdateCampo.class);
						break;

					case "IdGrupoActivos":
						sql.batch(conn, SQL_UPDATE_IDGRUPOACTIVOS, Arrays.asList(update), UpdateCampo.class);
						break;

					case "IdEstado":
						sql.batch(conn, SQL_UPDATE_IDESTADO, Arrays.asList(update), UpdateCampo.class);
						break;

					case "PermiteRuteo":
						sql.batch(conn, SQL_UPDATE_PERMITERUTEO, Arrays.asList(update), UpdateCampo.class);
						break;

					default:
						logger.warn("CampoModificado no reconocido: " + row.CampoModificado);
				}
			} 
			// 3. Cerrar la solicitud (cambio de estado) vía clase Java migrada

			ModificarEstadoRegistroSolicitudCambio modificarEstado = new ModificarEstadoRegistroSolicitudCambio();
			EntradaModificarEstadoRegistroSolicitudCambio entradaModificar = new EntradaModificarEstadoRegistroSolicitudCambio();

			entradaModificar.p_IdEmpresa = in.p_IdEmpresa;
			entradaModificar.p_IdUsuario = in.p_IdUsuario;
			entradaModificar.p_IdModulo = in.p_IdModulo;
			entradaModificar.p_IdEntidadOrigen = idEntidadOrigen;
			entradaModificar.p_IdTipoEntidad = "PERFILPRODUCTODETALLE";
			entradaModificar.p_IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
 
			SalidaModificarEstadoRegistroSolicitudCambio salidaModificar = modificarEstado.logica(conn, entradaModificar);
 
			if (salidaModificar.P_OUT_ERRORESCONTADOR != null && salidaModificar.P_OUT_ERRORESCONTADOR > 0) {

				out.p_out_ErroresContador = salidaModificar.P_OUT_ERRORESCONTADOR;
				out.p_out_ErroresDetalle.add(new ErrorDetalle("ERROR_CIERRE_SOLICITUD",

					"Error al modificar el estado de la solicitud de cambio para IdDetallePerfilProductos=" + idEntidadOrigen));

			} 
		} catch (Exception ex) {

			logger.error("Error en ConfirmarModificarPerfilProductoDetalle: " + ex.getMessage(), ex);

			throw ex;
		}
 
		logger.info("FIN ConfirmarModificarPerfilProductoDetalle");

		return out;
	}
} 