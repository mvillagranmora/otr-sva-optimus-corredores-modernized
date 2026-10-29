package cl.bcs.socionegocio.cnt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Migrado de: CNT.ConfirmarModificarEstadoContraparte
 * Confirma la modificacion del estado de una Contraparte: aplica en CNT.Contraparte
 * los valores pendientes en la SolicitudCambio (CONTRAPARTE-ESTADO), cierra esa solicitud
 * y registra el log de auditoria.
 * Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion.
 */
public class ConfirmarModificarEstadoContraparte implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ConfirmarModificarEstadoContraparte.class);

	private final String SQL_SELECT_SOLICITUD_CAMBIO = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/cnt/sql/ConfirmarModificarEstadoContraparte_Select_SolicitudCambio.sql");
	private final String SQL_UPDATE_CONTRAPARTE = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/cnt/sql/ConfirmarModificarEstadoContraparte_Update_Contraparte.sql");
	private final String SQL_SELECT_DOCUMENTO_IDENTIDAD = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/cnt/sql/ConfirmarModificarEstadoContraparte_Select_DocumentoIdentidad.sql");

	public static class EntradaConfirmarModificarEstadoContraparte {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdContraparte;
		public String p_IdEstadoAutorizacion;

		public String toString() {
			return Utils.printJson(this, false);
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

	public static class SalidaConfirmarModificarEstadoContraparte {
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaConfirmarModificarEstadoContraparte() {
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SolicitudCambio {
		public Long IdSolicitudCambio;
		public String IdEntidadOrigen;
		public String IdTipoEntidad;
		public String CampoModificado;
		public String TipoDato;
		public String ValorAntiguo;
		public String ValorNuevo;
		public String IdEstadoAutorizacion;
	}

	public static class UpdateContraparte {
		public Long IdContraparte;
	}

	public static class DocumentoContraparte {
		public String TipoDocumentoIdentidad;
		public String DocumentoIdentidad;
	}

	public SalidaConfirmarModificarEstadoContraparte logica(Connection conn, EntradaConfirmarModificarEstadoContraparte in) throws Exception {
		logger.info("INIT ConfirmarModificarEstadoContraparte");
		SalidaConfirmarModificarEstadoContraparte salida = new SalidaConfirmarModificarEstadoContraparte();
		SQL sql = new SQL();

		try {
			String v_IdEntidadOrigen = String.valueOf(in.p_IdContraparte);
			String v_TipoEntidad = "CONTRAPARTE-ESTADO";

			// Validar existencia de solicitud de cambio pendiente y obtener su detalle
			logger.debug("Consultar solicitud de cambio pendiente");
			HashMap<String, Object> paramsSolicitud = new HashMap<>();
			paramsSolicitud.put("IdEntidadOrigen", v_IdEntidadOrigen);
			paramsSolicitud.put("IdTipoEntidad", v_TipoEntidad);

			List<SolicitudCambio> solicitudesCambio = sql.query(conn, SQL_SELECT_SOLICITUD_CAMBIO, paramsSolicitud, SolicitudCambio.class);

			if (solicitudesCambio == null || solicitudesCambio.isEmpty()) {
				String mensajeSolicitudNoExiste = Diccionario.getMensaje(conn, in.p_IdEmpresa, "SOLICITUD_NO_EXISTE", new HashMap<String, String>());
				salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("SOLICITUD_NO_EXISTE", mensajeSolicitudNoExiste));
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
				return salida;
			}

			// Actualizar en Contraparte los campos que quedaron pendientes en la solicitud de cambio
			logger.debug("Actualizar estado de contraparte");
			UpdateContraparte updateContraparte = new UpdateContraparte();
			updateContraparte.IdContraparte = in.p_IdContraparte;

			List<UpdateContraparte> updatesContraparte = new ArrayList<>();
			updatesContraparte.add(updateContraparte);
			sql.batch(conn, SQL_UPDATE_CONTRAPARTE, updatesContraparte, UpdateContraparte.class);

			// Confirmar (cerrar) el registro de la solicitud de cambio
			logger.debug("Confirmar registro de solicitud de cambio");
			ModificarEstadoRegistroSolicitudCambio.EntradaModificarEstadoRegistroSolicitudCambio entradaModEstado =
				new ModificarEstadoRegistroSolicitudCambio.EntradaModificarEstadoRegistroSolicitudCambio();
			entradaModEstado.p_IdEmpresa = in.p_IdEmpresa;
			entradaModEstado.p_IdUsuario = in.p_IdUsuario;
			entradaModEstado.p_IdModulo = in.p_IdModulo;
			entradaModEstado.p_IdEntidadOrigen = v_IdEntidadOrigen;
			entradaModEstado.p_IdTipoEntidad = v_TipoEntidad;
			entradaModEstado.p_IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;

			ModificarEstadoRegistroSolicitudCambio modEstadoWrapper = new ModificarEstadoRegistroSolicitudCambio();
			ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio modEstadoOut =
				modEstadoWrapper.logica(conn, entradaModEstado);

			// El SP original propaga estos errores sin detener el flujo (el log se agrega igual)
			if (modEstadoOut.P_OUT_ERRORESCONTADOR != null && modEstadoOut.P_OUT_ERRORESCONTADOR > 0) {
				for (MensajeSalida msg : modEstadoOut.P_OUT_MENSAJES) {
					salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle(msg.codigo, msg.descripcion));
				}
				salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
			}

			// Log de auditoria
			logger.debug("Agregar log de auditoria");
			HashMap<String, Object> paramsDocumento = new HashMap<>();
			paramsDocumento.put("IdContraparte", in.p_IdContraparte);

			List<DocumentoContraparte> documentos = sql.query(conn, SQL_SELECT_DOCUMENTO_IDENTIDAD, paramsDocumento, DocumentoContraparte.class);
			String v_IdTipoDocumentoIdentidad = (documentos != null && !documentos.isEmpty()) ? documentos.get(0).TipoDocumentoIdentidad : "";
			String v_IdDocumentoIdentidad = (documentos != null && !documentos.isEmpty()) ? documentos.get(0).DocumentoIdentidad : "";

			String v_FechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);

			AgregarLog.InAgregarLog entradaLog = new AgregarLog.InAgregarLog();

			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = 1L;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = "SOC";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = v_FechaSistema;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdTipoRegistro = v_TipoEntidad;
			log.NombreProceso = "Confirmar Cambio de Estado";
			log.IdRegistro = v_IdEntidadOrigen;
			log.EstadoProceso = in.p_IdEstadoAutorizacion;
			log.EstadoRegistro = in.p_IdEstadoAutorizacion;
			log.IdTipoDocumentoIdentidad = v_IdTipoDocumentoIdentidad;
			log.IdDocumentoIdentidad = v_IdDocumentoIdentidad;
			entradaLog.p_Log.add(log);

			for (SolicitudCambio item : solicitudesCambio) {
				AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
				detLog.IdCabeceraLog = 1L;
				detLog.IdTrabajo = 0L;
				detLog.SubRegistro = item.IdTipoEntidad;
				detLog.CampoRegistro = item.CampoModificado;
				detLog.ValorAntiguo = item.ValorAntiguo;
				detLog.ValorNuevo = item.ValorNuevo;
				entradaLog.p_LogDetalle.add(detLog);
			}

			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaLog);

		} catch (Exception ex) {
			logger.error("Error en ConfirmarModificarEstadoContraparte: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CONFIRMAR_MODIFICAR_ESTADO_CONTRAPARTE", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END ConfirmarModificarEstadoContraparte");
		return salida;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarModificarEstadoContraparte in = gson.fromJson(jin.toString(), EntradaConfirmarModificarEstadoContraparte.class);
		SalidaConfirmarModificarEstadoContraparte out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdContraparte\":123456,\"p_IdEstadoAutorizacion\":\"AUT\"}";
			EntradaConfirmarModificarEstadoContraparte entrada = gson.fromJson(json, EntradaConfirmarModificarEstadoContraparte.class);
			ConfirmarModificarEstadoContraparte demo = new ConfirmarModificarEstadoContraparte();
			SalidaConfirmarModificarEstadoContraparte salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
