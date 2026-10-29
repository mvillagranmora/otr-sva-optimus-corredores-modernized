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

public class ConfirmarEliminarPersonaDocumentoIdentidad implements JavaCall{

	private static final Logger logger = LogManager.getLogger(ConfirmarEliminarPersonaDocumentoIdentidad.class);

	private final String SQL_SELECT_SOLICITUDES_CAMBIO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaDocumentoIdentidad_SelectSolicitudesCambio.sql");
	private final String SQL_UPDATE_BORRADO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaDocumentoIdentidad_UpdateBorrado.sql");
	private final String SQL_SELECT_PERSONA_DOCUMENTO_IDENTIDAD = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaDocumentoIdentidad_SelectPersonaDocumentoIdentidad.sql");

	private static final String ID_TIPO_ENTIDAD = "DOCUMENTOIDENTIDAD-BORRAR";
	private static final String NOMBRE_PROCESO = "Confirmar Eliminacion";

	public static class EntradaConfirmarEliminarPersonaDocumentoIdentidad{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDocumentoIdentidad;
		public String p_IdEstadoAutorizacion;
	}

	public static class SalidaConfirmarEliminarPersonaDocumentoIdentidad extends SalidaEstandar{
		public SalidaConfirmarEliminarPersonaDocumentoIdentidad(){
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
		public String p_Borrado;
	}

	public static class PersonaDocumentoIdentidad{
		public Long IdDocumentoIdentidad;
		public Long IdPersona;
		public String Borrado;
		public String DocumentoIdentidad;
		public String NombreCompleto;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarEliminarPersonaDocumentoIdentidad in = gson.fromJson(jin.toString(), EntradaConfirmarEliminarPersonaDocumentoIdentidad.class);
		SalidaConfirmarEliminarPersonaDocumentoIdentidad out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaConfirmarEliminarPersonaDocumentoIdentidad logica(Connection conn, EntradaConfirmarEliminarPersonaDocumentoIdentidad in) throws Exception{
		logger.info("INICIO::ConfirmarEliminarPersonaDocumentoIdentidad");
		SalidaConfirmarEliminarPersonaDocumentoIdentidad salida = new SalidaConfirmarEliminarPersonaDocumentoIdentidad();
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

		SolicitudCambio solicitudBorrado = solicitudesCambio.stream()
				.filter(s -> "Borrado".equals(s.CampoModificado))
				.findFirst().orElse(null);

		if(solicitudBorrado != null){
			DocumentoIdentidadUpdate update = new DocumentoIdentidadUpdate();
			update.p_IdDocumentoIdentidad = in.p_IdDocumentoIdentidad;
			update.p_Borrado = solicitudBorrado.ValorNuevo;
			sql.batch(conn, SQL_UPDATE_BORRADO, Arrays.asList(update), DocumentoIdentidadUpdate.class);
		}

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

		// Aislado en su propio try/catch: un fallo de auditoria no debe reportar como fallida
		// una confirmacion que ya actualizo el documento y cerro la solicitud correctamente.
		try {
			registrarLog(conn, sql, in, solicitudesCambio);
		} catch (Exception exLog) {
			logger.error("Error registrando auditoria en ConfirmarEliminarPersonaDocumentoIdentidad: " + exLog.getMessage(), exLog);
		}

		logger.info("FIN::ConfirmarEliminarPersonaDocumentoIdentidad");
		return salida;
	}

	private void registrarLog(Connection conn, SQL sql, EntradaConfirmarEliminarPersonaDocumentoIdentidad in, List<SolicitudCambio> solicitudesCambio) throws Exception {
		logger.debug("INICIO::ConfirmarEliminarPersonaDocumentoIdentidad.registrarLog");

		HashMap<String, Object> params = new HashMap<>();
		params.put("IdDocumentoIdentidad", new ArrayList<>(Arrays.asList(in.p_IdDocumentoIdentidad)));
		List<PersonaDocumentoIdentidad> resultado = sql.query(conn, SQL_SELECT_PERSONA_DOCUMENTO_IDENTIDAD, params, PersonaDocumentoIdentidad.class);
		if(resultado.isEmpty()){
			logger.debug("FIN::ConfirmarEliminarPersonaDocumentoIdentidad.registrarLog - sin datos de documento/persona");
			return;
		}
		PersonaDocumentoIdentidad datos = resultado.get(0);

		String fechaSistema = Utils.getFechaSistema(conn, Utils.getSchema(), in.p_IdEmpresa);

		InAgregarLog entrada = new InAgregarLog();

		InAgregarLog.Log log = new InAgregarLog.Log();
		log.IdCabeceraLog = datos.IdPersona;
		log.IdTipoLog = "ELIMINACION";
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

		logger.debug("FIN::ConfirmarEliminarPersonaDocumentoIdentidad.registrarLog");
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdDocumentoIdentidad\":1720824,\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarEliminarPersonaDocumentoIdentidad in = gson.fromJson(json, EntradaConfirmarEliminarPersonaDocumentoIdentidad.class);

			ConfirmarEliminarPersonaDocumentoIdentidad demo = new ConfirmarEliminarPersonaDocumentoIdentidad();

			try {
				SalidaConfirmarEliminarPersonaDocumentoIdentidad out = demo.logica(con, in);
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
