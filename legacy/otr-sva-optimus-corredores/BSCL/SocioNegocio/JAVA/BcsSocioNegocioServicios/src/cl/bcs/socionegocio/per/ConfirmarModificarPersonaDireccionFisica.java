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

public class ConfirmarModificarPersonaDireccionFisica implements JavaCall{

	private static final Logger logger = LogManager.getLogger(ConfirmarModificarPersonaDireccionFisica.class);

	private final String SQL_SELECT_SOLICITUDES_CAMBIO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisica_SelectSolicitudesCambio.sql");
	private final String SQL_SELECT_ID_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisica_SelectIdPersona.sql");
	private final String SQL_SELECT_DETALLE_LOG = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisica_SelectDetalleLog.sql");
	private final String SQL_UPDATE_CALLE = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisica_UpdateCalle.sql");
	private final String SQL_UPDATE_NUMERO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisica_UpdateNumero.sql");
	private final String SQL_UPDATE_DEPARTAMENTO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisica_UpdateDepartamento.sql");
	private final String SQL_UPDATE_SECTOR = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisica_UpdateSector.sql");
	private final String SQL_UPDATE_CODIGO_POSTAL = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisica_UpdateCodigoPostal.sql");
	private final String SQL_UPDATE_ID_CIUDAD = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisica_UpdateIdCiudad.sql");
	private final String SQL_UPDATE_ID_DIVISION_PAIS = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisica_UpdateIdDivisionPais.sql");
	private final String SQL_UPDATE_CODIGO_PROPOSITO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisica_UpdateCodigoProposito.sql");
	private final String SQL_UPDATE_COMENTARIO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisica_UpdateComentario.sql");
	private final String SQL_UPDATE_ID_ESTADO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisica_UpdateIdEstado.sql");
	private final String SQL_UPDATE_CODIGO_COMPUESTO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaDireccionFisica_UpdateCodigoCompuesto.sql");

	private static final String ID_TIPO_ENTIDAD = "DIRECCIONFISICA";
	private static final String NOMBRE_PROCESO = "Confirmar Solicitud de Cambio";

	public static class EntradaConfirmarModificarPersonaDireccionFisica{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDireccionFisica;
		public String p_IdEstadoAutorizacion;
	}

	public static class SalidaConfirmarModificarPersonaDireccionFisica extends SalidaEstandar{
		public SalidaConfirmarModificarPersonaDireccionFisica(){
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

	public static class DireccionFisicaUpdate{
		public Long p_IdDireccionFisica;
		public String p_ValorNuevo;
	}

	public static class DireccionFisicaPersona{
		public Long IdDireccionFisica;
		public Long IdPersona;
	}

	public static class DetalleLog{
		public String SubRegistro;
		public String CampoRegistro;
		public String ValorAntiguo;
		public String ValorNuevo;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarModificarPersonaDireccionFisica in = gson.fromJson(jin.toString(), EntradaConfirmarModificarPersonaDireccionFisica.class);
		SalidaConfirmarModificarPersonaDireccionFisica out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaConfirmarModificarPersonaDireccionFisica logica(Connection conn, EntradaConfirmarModificarPersonaDireccionFisica in) throws Exception{
		logger.info("INICIO::ConfirmarModificarPersonaDireccionFisica");
		SalidaConfirmarModificarPersonaDireccionFisica salida = new SalidaConfirmarModificarPersonaDireccionFisica();
		SQL sql = new SQL();

		String idEntidadOrigen = String.valueOf(in.p_IdDireccionFisica);

		HashMap<String, Object> paramsSolicitud = new HashMap<>();
		paramsSolicitud.put("p_IdEntidadOrigen", idEntidadOrigen);
		paramsSolicitud.put("p_IdTipoEntidad", ID_TIPO_ENTIDAD);
		List<SolicitudCambio> solicitudesCambio = sql.query(conn, SQL_SELECT_SOLICITUDES_CAMBIO, paramsSolicitud, SolicitudCambio.class);

		if(solicitudesCambio.isEmpty()){
			salida.addError("SOLICITUD_NO_EXISTE", "SOLICITUD_NO_EXISTE");
			return salida;
		}

		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "Calle", SQL_UPDATE_CALLE, in.p_IdDireccionFisica);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "Numero", SQL_UPDATE_NUMERO, in.p_IdDireccionFisica);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "Departamento", SQL_UPDATE_DEPARTAMENTO, in.p_IdDireccionFisica);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "Sector", SQL_UPDATE_SECTOR, in.p_IdDireccionFisica);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "CodigoPostal", SQL_UPDATE_CODIGO_POSTAL, in.p_IdDireccionFisica);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "IdCiudad", SQL_UPDATE_ID_CIUDAD, in.p_IdDireccionFisica);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "IdDivisionPais", SQL_UPDATE_ID_DIVISION_PAIS, in.p_IdDireccionFisica);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "CodigoProposito", SQL_UPDATE_CODIGO_PROPOSITO, in.p_IdDireccionFisica);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "Comentario", SQL_UPDATE_COMENTARIO, in.p_IdDireccionFisica);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "IdEstado", SQL_UPDATE_ID_ESTADO, in.p_IdDireccionFisica);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "CodigoCompuesto", SQL_UPDATE_CODIGO_COMPUESTO, in.p_IdDireccionFisica);

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
			logger.error("Error registrando auditoria en ConfirmarModificarPersonaDireccionFisica: " + exLog.getMessage(), exLog);
		}

		logger.info("FIN::ConfirmarModificarPersonaDireccionFisica");
		return salida;
	}

	private boolean aplicarCambioSiExiste(Connection conn, SQL sql, List<SolicitudCambio> solicitudesCambio, String campoModificado, String sqlUpdate, Long idDireccionFisica) throws Exception {
		SolicitudCambio solicitud = solicitudesCambio.stream()
				.filter(s -> campoModificado.equals(s.CampoModificado))
				.findFirst().orElse(null);
		if(solicitud == null){
			return false;
		}
		DireccionFisicaUpdate update = new DireccionFisicaUpdate();
		update.p_IdDireccionFisica = idDireccionFisica;
		update.p_ValorNuevo = solicitud.ValorNuevo;
		sql.batch(conn, sqlUpdate, Arrays.asList(update), DireccionFisicaUpdate.class);
		return true;
	}

	private void registrarLog(Connection conn, SQL sql, EntradaConfirmarModificarPersonaDireccionFisica in, List<SolicitudCambio> solicitudesCambio) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaDireccionFisica.registrarLog");

		HashMap<String, Object> paramsPersona = new HashMap<>();
		paramsPersona.put("IdDireccionFisica", new ArrayList<>(Arrays.asList(in.p_IdDireccionFisica)));
		List<DireccionFisicaPersona> resultado = sql.query(conn, SQL_SELECT_ID_PERSONA, paramsPersona, DireccionFisicaPersona.class);
		if(resultado.isEmpty()){
			logger.debug("FIN::ConfirmarModificarPersonaDireccionFisica.registrarLog - sin datos de persona");
			return;
		}
		DireccionFisicaPersona datos = resultado.get(0);

		String idEntidadOrigen = String.valueOf(in.p_IdDireccionFisica);
		// Se filtra por IdSolicitudCambio (capturado antes de cerrar la solicitud), no por IdEstadoAutorizacion='PEN':
		// para cuando se ejecuta este metodo, ModificarEstadoRegistroSolicitudCambio ya cambio el estado de la
		// solicitud, por lo que un filtro por 'PEN' nunca encontraria las filas (quedaria el log siempre vacio).
		List<Long> idsSolicitudCambio = solicitudesCambio.stream().map(s -> s.IdSolicitudCambio).collect(java.util.stream.Collectors.toList());
		HashMap<String, Object> paramsDetalle = new HashMap<>();
		paramsDetalle.put("p_IdSolicitudCambio", idsSolicitudCambio);
		List<DetalleLog> detalles = sql.query(conn, SQL_SELECT_DETALLE_LOG, paramsDetalle, DetalleLog.class);
		if(detalles.isEmpty()){
			logger.debug("FIN::ConfirmarModificarPersonaDireccionFisica.registrarLog - sin detalle de cambios");
			return;
		}

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
		log.IdRegistro = idEntidadOrigen;
		log.NombreProceso = NOMBRE_PROCESO;
		log.EstadoProceso = in.p_IdEstadoAutorizacion;
		entrada.p_Log.add(log);

		for(DetalleLog detalle : detalles){
			entrada.agregarDetalleLogBasico(datos.IdPersona, 0L, detalle.CampoRegistro, detalle.ValorAntiguo, detalle.ValorNuevo, detalle.SubRegistro);
		}

		new AgregarLog().logica(conn, entrada);

		logger.debug("FIN::ConfirmarModificarPersonaDireccionFisica.registrarLog");
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdDireccionFisica\":2469693,\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarModificarPersonaDireccionFisica in = gson.fromJson(json, EntradaConfirmarModificarPersonaDireccionFisica.class);

			ConfirmarModificarPersonaDireccionFisica demo = new ConfirmarModificarPersonaDireccionFisica();

			try {
				SalidaConfirmarModificarPersonaDireccionFisica out = demo.logica(con, in);
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
