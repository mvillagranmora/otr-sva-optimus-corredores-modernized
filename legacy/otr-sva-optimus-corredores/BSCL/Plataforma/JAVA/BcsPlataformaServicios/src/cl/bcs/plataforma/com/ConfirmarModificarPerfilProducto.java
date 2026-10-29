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
 
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio;

import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.EntradaModificarEstadoRegistroSolicitudCambio;

import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import cl.bcs.utils.JavaCall;

import cl.bcs.utils.SQL;

import cl.bcs.utils.Utils;
 
/**

* Wrapper migrado para COM.ConfirmarModificarPerfilProducto

* Aplica los cambios pendientes en PLA.SolicitudCambio sobre COM.PerfilProductos

* y cierra la solicitud vía la clase Java ModificarEstadoRegistroSolicitudCambio.

*/

public class ConfirmarModificarPerfilProducto implements JavaCall {
 
	private static final Logger logger = LogManager.getLogger(ConfirmarModificarPerfilProducto.class);
 
	private final String SQL_SELECT_SOLICITUD = PlataformaUtils.loadResourceToString(

		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProducto_Select_SolicitudCambio.sql");
 
	private final String SQL_UPDATE_DESCRIPCIONPERFILPRODUCTOS = PlataformaUtils.loadResourceToString(

		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProducto_Update_DescripcionPerfilProductos.sql");
 
	private final String SQL_UPDATE_IDCATEGORIAINVERSIONISTA = PlataformaUtils.loadResourceToString(

		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProducto_Update_IdCategoriaInversionista.sql");
 
	private final String SQL_UPDATE_ESPERFILPERSONALIZADO = PlataformaUtils.loadResourceToString(

		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProducto_Update_EsPerfilPersonalizado.sql");
 
	private final String SQL_UPDATE_TIPOENTIDADREFERENCIA = PlataformaUtils.loadResourceToString(

		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProducto_Update_TipoEntidadReferencia.sql");
 
	private final String SQL_UPDATE_IDREFERENCIA = PlataformaUtils.loadResourceToString(

		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProducto_Update_IdReferencia.sql");
 
	private final String SQL_UPDATE_IDESTADO = PlataformaUtils.loadResourceToString(

		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProducto_Update_IdEstado.sql");
 
	private final String SQL_UPDATE_PORDEFECTO = PlataformaUtils.loadResourceToString(

		"cl/bcs/plataforma/com/sql/ConfirmarModificarPerfilProducto_Update_PorDefecto.sql");
 
	public static class EntradaConfirmarModificarPerfilProducto {
 
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
 
	public static class SolicitudCambioRow {

		public String CampoModificado;

		public String ValorNuevo;

	}
 
	public static class UpdateCampo {

		public String p_ValorNuevo;

		public String p_IdPerfilProductos;

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
 
	public static class SalidaConfirmarModificarPerfilProducto {
 
		public Long p_out_AdvertenciasContador;

		public List<AdvertenciaDetalle> p_out_AdvertenciasDetalle;

		public Long p_out_ErroresContador;

		public List<ErrorDetalle> p_out_ErroresDetalle;
 
		public SalidaConfirmarModificarPerfilProducto() {

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
 
		EntradaConfirmarModificarPerfilProducto in = gson.fromJson(jin.toString(),

			EntradaConfirmarModificarPerfilProducto.class);
 
		SalidaConfirmarModificarPerfilProducto out = logica(conn, in);
 
		JsonParser parser = new JsonParser();

		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
 
		return dataout;

	}
 
	public SalidaConfirmarModificarPerfilProducto logica(Connection conn,

			EntradaConfirmarModificarPerfilProducto in) throws Exception {
 
		logger.info("INICIO ConfirmarModificarPerfilProducto");
 
		SQL sql = new SQL();
 
		SalidaConfirmarModificarPerfilProducto out = new SalidaConfirmarModificarPerfilProducto();
 
		try {
 
			String idEntidadOrigen = in.p_IdPerfilProductos;
 
			// 1. Traer la solicitud pendiente (CampoModificado / ValorNuevo) para este perfil

			HashMap<String, Object> parmsSelect = new HashMap<>();

			parmsSelect.put("p_IdEntidadOrigen", idEntidadOrigen);

			parmsSelect.put("p_IdTipoEntidad", "PERFILPRODUCTO");
 
			List<SolicitudCambioRow> solicitud = sql.query(conn, SQL_SELECT_SOLICITUD, parmsSelect,

				SolicitudCambioRow.class);
 
			if (solicitud == null || solicitud.isEmpty()) {
 
				out.p_out_ErroresContador = 1L;

				out.p_out_ErroresDetalle.add(new ErrorDetalle("SOLICITUD_NO_EXISTE",

					"No existe solicitud pendiente para IdPerfilProductos=" + idEntidadOrigen));
 
				logger.info("FIN ConfirmarModificarPerfilProducto (sin solicitud)");
 
				return out;

			}
 
			// 2. Aplicar UPDATE condicional por cada campo presente en la solicitud

			for (SolicitudCambioRow row : solicitud) {
 
				UpdateCampo update = new UpdateCampo();

				update.p_ValorNuevo = row.ValorNuevo;

				update.p_IdPerfilProductos = in.p_IdPerfilProductos;
 
				switch (row.CampoModificado) {
 
					case "DescripcionPerfilProductos":

						sql.batch(conn, SQL_UPDATE_DESCRIPCIONPERFILPRODUCTOS, Arrays.asList(update), UpdateCampo.class);

						break;
 
					case "IdCategoriaInversionista":

						sql.batch(conn, SQL_UPDATE_IDCATEGORIAINVERSIONISTA, Arrays.asList(update), UpdateCampo.class);

						break;
 
					case "EsPerfilPersonalizado":

						sql.batch(conn, SQL_UPDATE_ESPERFILPERSONALIZADO, Arrays.asList(update), UpdateCampo.class);

						break;
 
					case "TipoEntidadReferencia":

						sql.batch(conn, SQL_UPDATE_TIPOENTIDADREFERENCIA, Arrays.asList(update), UpdateCampo.class);

						break;
 
					case "IdReferencia":

						sql.batch(conn, SQL_UPDATE_IDREFERENCIA, Arrays.asList(update), UpdateCampo.class);

						break;
 
					case "IdEstado":

						sql.batch(conn, SQL_UPDATE_IDESTADO, Arrays.asList(update), UpdateCampo.class);

						break;
 
					case "PorDefecto":

						sql.batch(conn, SQL_UPDATE_PORDEFECTO, Arrays.asList(update), UpdateCampo.class);

						break;
 
					default:

						logger.warn("CampoModificado no reconocido: " + row.CampoModificado);

				}

			}
 
			// 3. Cerrar la solicitud (log + cambio de estado) vía clase Java migrada

			ModificarEstadoRegistroSolicitudCambio modificarEstado = new ModificarEstadoRegistroSolicitudCambio();
 
			EntradaModificarEstadoRegistroSolicitudCambio entradaModificar = new EntradaModificarEstadoRegistroSolicitudCambio();

			entradaModificar.p_IdEmpresa = in.p_IdEmpresa;

			entradaModificar.p_IdUsuario = in.p_IdUsuario;

			entradaModificar.p_IdModulo = in.p_IdModulo;

			entradaModificar.p_IdEntidadOrigen = idEntidadOrigen;

			entradaModificar.p_IdTipoEntidad = "PERFILPRODUCTO";

			entradaModificar.p_IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
 
			SalidaModificarEstadoRegistroSolicitudCambio salidaModificar = modificarEstado.logica(conn, entradaModificar);
 
			if (salidaModificar.P_OUT_ERRORESCONTADOR > 0) {
 
				out.p_out_ErroresContador += salidaModificar.P_OUT_ERRORESCONTADOR;
 
				salidaModificar.P_OUT_MENSAJES.forEach(msg ->

					out.p_out_ErroresDetalle.add(new ErrorDetalle(msg.codigo, msg.descripcion)));

			}
 
		} catch (Exception ex) {
 
			logger.error("Error en ConfirmarModificarPerfilProducto: " + ex.getMessage(), ex);

			throw ex;
 
		}
 
		logger.info("FIN ConfirmarModificarPerfilProducto");
 
		return out;

	}

}
 