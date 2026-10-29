package cl.bcs.socionegocio.per;

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

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.EntradaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambio.SalidaModificarEstadoRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarModificarPersonaDocumentosIdentidad implements JavaCall{

	private static final Logger logger = LogManager.getLogger(ConfirmarModificarPersonaDocumentosIdentidad.class);

	private final String SQL_SELECT_SOLICITUDES_CAMBIO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDocumentosIdentidad_SelectSolicitudesCambio.sql");
	private final String SQL_SELECT_ID_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDocumentosIdentidad_SelectIdPersona.sql");
	private final String SQL_UPDATE_ID_TIPO_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDocumentosIdentidad_UpdateIdTipoDocumentoIdentidad.sql");
	private final String SQL_UPDATE_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDocumentosIdentidad_UpdateDocumentoIdentidad.sql");
	private final String SQL_UPDATE_NUMERO_SERIE = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDocumentosIdentidad_UpdateNumeroSerie.sql");
	private final String SQL_UPDATE_FECHA_EMISION = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDocumentosIdentidad_UpdateFechaEmision.sql");
	private final String SQL_UPDATE_FECHA_VENCIMIENTO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDocumentosIdentidad_UpdateFechaVencimiento.sql");
	private final String SQL_UPDATE_ID_ESTADO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDocumentosIdentidad_UpdateIdEstado.sql");
	private final String SQL_CALL_DOCUMENTOS_POR_VENCER = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDocumentosIdentidad_CallDocumentosPorVencer.sql");

	private static final String ID_TIPO_ENTIDAD = "DOCUMENTOIDENTIDAD";
	private static final String NOMBRE_PROCESO = "Confirmar Solicitud de Cambio";

	public static class EntradaConfirmarModificarPersonaDocumentosIdentidad{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDocumentoIdentidad;
		public String p_IdEstadoAutorizacion;
	}

	public static class SalidaConfirmarModificarPersonaDocumentosIdentidad extends SalidaEstandar{
		public SalidaConfirmarModificarPersonaDocumentosIdentidad(){
			super();
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
	}

	public static class SolicitudCambio{
		public Long IdSolicitudCambio;
		public String IdEntidadOrigen;
		public String IdTipoEntidad;
		public String IdEstadoAutorizacion;
		public String CampoModificado;
		public String ValorAntiguo;
		public String ValorNuevo;
	}

	public static class DocumentoIdentidadUpdate{
		public Long p_IdDocumentoIdentidad;
		public String p_ValorNuevo;
	}

	public static class DocumentoIdentidadPersona{
		public Long IdDocumentoIdentidad;
		public Long IdPersona;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarModificarPersonaDocumentosIdentidad in = gson.fromJson(jin.toString(), EntradaConfirmarModificarPersonaDocumentosIdentidad.class);
		SalidaConfirmarModificarPersonaDocumentosIdentidad out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaConfirmarModificarPersonaDocumentosIdentidad logica(Connection conn, EntradaConfirmarModificarPersonaDocumentosIdentidad in) throws Exception{
		logger.info("INICIO::ConfirmarModificarPersonaDocumentosIdentidad");
		SalidaConfirmarModificarPersonaDocumentosIdentidad salida = new SalidaConfirmarModificarPersonaDocumentosIdentidad();
		SQL sql = new SQL();

		String idEntidadOrigen = String.valueOf(in.p_IdDocumentoIdentidad);

		HashMap<String, Object> paramsSolicitud = new HashMap<>();
		paramsSolicitud.put("p_IdEntidadOrigen", idEntidadOrigen);
		paramsSolicitud.put("p_IdTipoEntidad", ID_TIPO_ENTIDAD);
		List<SolicitudCambio> solicitudesCambio = sql.query(conn, SQL_SELECT_SOLICITUDES_CAMBIO, paramsSolicitud, SolicitudCambio.class);

		if(solicitudesCambio.isEmpty()){
			salida.addError("SOLICITUD_NO_EXISTE", "SOLICITUD_NO_EXISTE");
			return salida;
		}

		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "IdTipoDocumentoIdentidad", SQL_UPDATE_ID_TIPO_DOCUMENTO_IDENTIDAD, in.p_IdDocumentoIdentidad);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "DocumentoIdentidad", SQL_UPDATE_DOCUMENTO_IDENTIDAD, in.p_IdDocumentoIdentidad);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "NumeroSerie", SQL_UPDATE_NUMERO_SERIE, in.p_IdDocumentoIdentidad);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "FechaEmision", SQL_UPDATE_FECHA_EMISION, in.p_IdDocumentoIdentidad);

		boolean cambioFechaVencimiento = aplicarCambioSiExiste(conn, sql, solicitudesCambio, "FechaVencimiento", SQL_UPDATE_FECHA_VENCIMIENTO, in.p_IdDocumentoIdentidad);
		if(cambioFechaVencimiento){
			// El SP original reutiliza los mismos OUT del propio Confirm para esta llamada (por lo que su resultado
			// termina siendo sobrescrito por el ModificarEstadoRegistroSolicitudCambio de mas abajo de todas formas).
			// Se aisla en try/catch: es un efecto colateral (notificacion de documentos por vencer), no debe
			// interrumpir la confirmacion si falla.
			try {
				HashMap<String, Object> paramsIn = new HashMap<>();
				paramsIn.put("p_IdEmpresa", 1L);
				paramsIn.put("p_IdUsuario", 0L);
				paramsIn.put("p_IdModulo", "PLA");
				HashMap<String, Class> paramsOut = new HashMap<>();
				paramsOut.put("p_out_AdvertenciasContador", Long.class);
				paramsOut.put("p_out_AdvertenciasDetalle", AdvertenciaDetalleGenerica.class);
				paramsOut.put("p_out_ErroresContador", Long.class);
				paramsOut.put("p_out_ErroresDetalle", ErrorDetalleGenerico.class);
				sql.call(conn, SQL_CALL_DOCUMENTOS_POR_VENCER, paramsIn, paramsOut);
			} catch (Exception exDocPorVencer) {
				logger.error("Error llamando DOC.DocumentosPorVencer en ConfirmarModificarPersonaDocumentosIdentidad: " + exDocPorVencer.getMessage(), exDocPorVencer);
			}
		}

		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "IdEstado", SQL_UPDATE_ID_ESTADO, in.p_IdDocumentoIdentidad);

		ModificarEstadoRegistroSolicitudCambio modificarEstado = new ModificarEstadoRegistroSolicitudCambio();
		EntradaModificarEstadoRegistroSolicitudCambio entradaModificar = new EntradaModificarEstadoRegistroSolicitudCambio();
		entradaModificar.p_IdEmpresa = in.p_IdEmpresa;
		entradaModificar.p_IdUsuario = in.p_IdUsuario;
		entradaModificar.p_IdModulo = in.p_IdModulo;
		entradaModificar.p_IdEntidadOrigen = idEntidadOrigen;
		entradaModificar.p_IdTipoEntidad = ID_TIPO_ENTIDAD;
		entradaModificar.p_IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
		SalidaModificarEstadoRegistroSolicitudCambio salidaModificar = modificarEstado.logica(conn, entradaModificar);
		for(MensajeSalida msg : salidaModificar.P_OUT_MENSAJES){
			if(MensajeSalida.MENSAJE_ERROR.equals(msg.tipo)){
				salida.addError(msg.codigo, msg.descripcion);
			}
		}

		try {
			registrarLog(conn, sql, in, solicitudesCambio);
		} catch (Exception exLog) {
			logger.error("Error registrando auditoria en ConfirmarModificarPersonaDocumentosIdentidad: " + exLog.getMessage(), exLog);
		}

		logger.info("FIN::ConfirmarModificarPersonaDocumentosIdentidad");
		return salida;
	}

	private boolean aplicarCambioSiExiste(Connection conn, SQL sql, List<SolicitudCambio> solicitudesCambio, String campoModificado, String sqlUpdate, Long idDocumentoIdentidad) throws Exception {
		SolicitudCambio solicitud = solicitudesCambio.stream()
				.filter(s -> campoModificado.equals(s.CampoModificado))
				.findFirst().orElse(null);
		if(solicitud == null){
			return false;
		}
		DocumentoIdentidadUpdate update = new DocumentoIdentidadUpdate();
		update.p_IdDocumentoIdentidad = idDocumentoIdentidad;
		update.p_ValorNuevo = solicitud.ValorNuevo;
		sql.batch(conn, sqlUpdate, Arrays.asList(update), DocumentoIdentidadUpdate.class);
		return true;
	}

	private void registrarLog(Connection conn, SQL sql, EntradaConfirmarModificarPersonaDocumentosIdentidad in, List<SolicitudCambio> solicitudesCambio) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaDocumentosIdentidad.registrarLog");

		HashMap<String, Object> params = new HashMap<>();
		params.put("IdDocumentoIdentidad", new ArrayList<>(Arrays.asList(in.p_IdDocumentoIdentidad)));
		List<DocumentoIdentidadPersona> resultado = sql.query(conn, SQL_SELECT_ID_PERSONA, params, DocumentoIdentidadPersona.class);
		if(resultado.isEmpty()){
			logger.debug("FIN::ConfirmarModificarPersonaDocumentosIdentidad.registrarLog - sin datos de persona");
			return;
		}
		DocumentoIdentidadPersona datos = resultado.get(0);

		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);

		InAgregarLog entrada = new InAgregarLog();

		InAgregarLog.Log log = new InAgregarLog.Log();
		log.IdCabeceraLog = datos.IdPersona;
		log.IdTipoLog = "MODIFICACION";
		log.IdModulo = "SOC";
		log.IdTipoRegistro = ID_TIPO_ENTIDAD;
		log.IdUsuario = in.p_IdUsuario;
		log.IdEmpresa = in.p_IdEmpresa;
		log.FechaSistemaLog = fechaSistema;
		log.IdUsuarioAtendido = in.p_IdUsuario;
		log.IdPersona = datos.IdPersona;
		log.IdRegistro = String.valueOf(in.p_IdDocumentoIdentidad);
		log.NombreProceso = NOMBRE_PROCESO;
		log.EstadoProceso = in.p_IdEstadoAutorizacion;
		entrada.p_Log.add(log);

		for(SolicitudCambio sol : solicitudesCambio){
			entrada.agregarDetalleLogBasico(datos.IdPersona, 0L, sol.CampoModificado, sol.ValorAntiguo, sol.ValorNuevo, sol.IdTipoEntidad);
		}

		new AgregarLog().logica(conn, entrada);

		logger.debug("FIN::ConfirmarModificarPersonaDocumentosIdentidad.registrarLog");
	}

	public static class AdvertenciaDetalleGenerica {
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
	}

	public static class ErrorDetalleGenerico {
		public String Codigo;
		public String Descripcion;
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdDocumentoIdentidad\":1790725,\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarModificarPersonaDocumentosIdentidad in = gson.fromJson(json, EntradaConfirmarModificarPersonaDocumentosIdentidad.class);

			ConfirmarModificarPersonaDocumentosIdentidad demo = new ConfirmarModificarPersonaDocumentosIdentidad();

			try {
				SalidaConfirmarModificarPersonaDocumentosIdentidad out = demo.logica(con, in);
				con.rollback();
				System.out.println(Utils.printJson(out, false));
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
