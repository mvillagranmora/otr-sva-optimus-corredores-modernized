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
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.pojo.SalidaEstandar;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarEliminarPersonaDireccionElectronica implements JavaCall{

	private static final Logger logger = LogManager.getLogger(ConfirmarEliminarPersonaDireccionElectronica.class);

	private final String SQL_SELECT_SOLICITUDES_CAMBIO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaDireccionElectronica_SelectSolicitudesCambio.sql");
	private final String SQL_UPDATE_BORRADO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaDireccionElectronica_UpdateBorrado.sql");
	private final String SQL_SELECT_DIRECCION_ELECTRONICA_LOG = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaDireccionElectronica_SelectDireccionElectronicaLog.sql");

	private static final String ID_TIPO_ENTIDAD = "DIRECCIONELECTRONICA-BORRAR";
	private static final String NOMBRE_PROCESO = "Confirmar Solicitud de Cambio";

	public static class EntradaConfirmarEliminarPersonaDireccionElectronica{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDireccionElectronica;
		public String p_IdEstadoAutorizacion;
	}

	public static class SalidaConfirmarEliminarPersonaDireccionElectronica extends SalidaEstandar{
		public SalidaConfirmarEliminarPersonaDireccionElectronica(){
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

	public static class DireccionElectronicaUpdate{
		public Long p_IdDireccionElectronica;
		public String p_Borrado;
	}

	public static class DireccionElectronicaLog{
		public Long IdDireccionElectronica;
		public Long IdPersona;
		public String Direccion;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarEliminarPersonaDireccionElectronica in = gson.fromJson(jin.toString(), EntradaConfirmarEliminarPersonaDireccionElectronica.class);
		SalidaConfirmarEliminarPersonaDireccionElectronica out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaConfirmarEliminarPersonaDireccionElectronica logica(Connection conn, EntradaConfirmarEliminarPersonaDireccionElectronica in) throws Exception{
		logger.info("INICIO::ConfirmarEliminarPersonaDireccionElectronica");
		SalidaConfirmarEliminarPersonaDireccionElectronica salida = new SalidaConfirmarEliminarPersonaDireccionElectronica();
		SQL sql = new SQL();

		String idEntidadOrigen = String.valueOf(in.p_IdDireccionElectronica);

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
			DireccionElectronicaUpdate update = new DireccionElectronicaUpdate();
			update.p_IdDireccionElectronica = in.p_IdDireccionElectronica;
			update.p_Borrado = solicitudBorrado.ValorNuevo;
			sql.batch(conn, SQL_UPDATE_BORRADO, Arrays.asList(update), DireccionElectronicaUpdate.class);
		}

		// El SP original confirma el cierre de la solicitud via
		// PLA.ModificarEstadoRegistroSolicitudCambioMasivo (no la version singular),
		// con p_SolicitudCambio vacio y p_EntidadOrigen poblado.
		ModificarEstadoRegistroSolicitudCambioMasivo modificarEstado = new ModificarEstadoRegistroSolicitudCambioMasivo();
		EntradaModificarEstado entradaModificar = new EntradaModificarEstado();
		entradaModificar.p_IdEmpresa = in.p_IdEmpresa;
		entradaModificar.p_IdUsuario = in.p_IdUsuario;
		entradaModificar.p_IdModulo = in.p_IdModulo;
		entradaModificar.p_IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
		EntidadOrigen entidadOrigen = new EntidadOrigen();
		entidadOrigen.IdEntidadOrigen = idEntidadOrigen;
		entidadOrigen.IdTipoEntidad = ID_TIPO_ENTIDAD;
		entradaModificar.p_EntidadOrigen.add(entidadOrigen);
		ModificarEstadoRegistroSolicitudCambioMasivo.SalidaModificarEstado salidaModificar = modificarEstado.logica(conn, entradaModificar);
		for(ModificarEstadoRegistroSolicitudCambioMasivo.ErrorDetalle err : salidaModificar.p_out_ErroresDetalle){
			salida.addError(err.Codigo, err.Descripcion);
		}

		try {
			registrarLog(conn, sql, in, solicitudesCambio);
		} catch (Exception exLog) {
			logger.error("Error registrando auditoria en ConfirmarEliminarPersonaDireccionElectronica: " + exLog.getMessage(), exLog);
		}

		logger.info("FIN::ConfirmarEliminarPersonaDireccionElectronica");
		return salida;
	}

	private void registrarLog(Connection conn, SQL sql, EntradaConfirmarEliminarPersonaDireccionElectronica in, List<SolicitudCambio> solicitudesCambio) throws Exception {
		logger.debug("INICIO::ConfirmarEliminarPersonaDireccionElectronica.registrarLog");

		HashMap<String, Object> params = new HashMap<>();
		params.put("IdDireccionElectronica", new ArrayList<>(Arrays.asList(in.p_IdDireccionElectronica)));
		List<DireccionElectronicaLog> resultado = sql.query(conn, SQL_SELECT_DIRECCION_ELECTRONICA_LOG, params, DireccionElectronicaLog.class);
		if(resultado.isEmpty()){
			logger.debug("FIN::ConfirmarEliminarPersonaDireccionElectronica.registrarLog - sin datos de direccion/persona");
			return;
		}
		DireccionElectronicaLog datos = resultado.get(0);

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
		log.IdRegistro = String.valueOf(in.p_IdDireccionElectronica);
		log.NombreProceso = NOMBRE_PROCESO;
		log.EstadoProceso = in.p_IdEstadoAutorizacion;
		entrada.p_Log.add(log);

		for(SolicitudCambio sol : solicitudesCambio){
			boolean esBorrado = "S".equals(sol.ValorNuevo);
			String valorAntiguo = esBorrado ? datos.Direccion : sol.ValorAntiguo;
			String valorNuevo = esBorrado ? "" : sol.ValorNuevo;
			entrada.agregarDetalleLogBasico(datos.IdPersona, 0L, sol.CampoModificado, valorAntiguo, valorNuevo, sol.IdTipoEntidad);
		}

		new AgregarLog().logica(conn, entrada);

		logger.debug("FIN::ConfirmarEliminarPersonaDireccionElectronica.registrarLog");
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdDireccionElectronica\":1,\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarEliminarPersonaDireccionElectronica in = gson.fromJson(json, EntradaConfirmarEliminarPersonaDireccionElectronica.class);

			ConfirmarEliminarPersonaDireccionElectronica demo = new ConfirmarEliminarPersonaDireccionElectronica();

			try {
				SalidaConfirmarEliminarPersonaDireccionElectronica out = demo.logica(con, in);
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
