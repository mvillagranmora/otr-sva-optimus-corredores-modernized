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

public class ConfirmarModificarPersonaTarjetaBancaria implements JavaCall{

	private static final Logger logger = LogManager.getLogger(ConfirmarModificarPersonaTarjetaBancaria.class);

	private final String SQL_SELECT_SOLICITUDES_CAMBIO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTarjetaBancaria_SelectSolicitudesCambio.sql");
	private final String SQL_SELECT_ID_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTarjetaBancaria_SelectIdPersona.sql");
	private final String SQL_SELECT_DETALLE_LOG = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTarjetaBancaria_SelectDetalleLog.sql");
	private final String SQL_UPDATE_ID_BANCO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTarjetaBancaria_UpdateIdBanco.sql");
	private final String SQL_UPDATE_ID_TIPO_TARJETA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTarjetaBancaria_UpdateIdTipoTarjetaBancaria.sql");
	private final String SQL_UPDATE_NUMERO_TARJETA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTarjetaBancaria_UpdateNumeroTarjetaBancaria.sql");
	private final String SQL_UPDATE_FECHA_VENCIMIENTO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTarjetaBancaria_UpdateFechaVencimiento.sql");
	private final String SQL_UPDATE_CODIGO_VERIFICACION = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTarjetaBancaria_UpdateCodigoVerificacion.sql");
	private final String SQL_UPDATE_ID_ESTADO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaTarjetaBancaria_UpdateIdEstado.sql");

	private static final String ID_TIPO_ENTIDAD = "TARJETABANCARIA";
	private static final String NOMBRE_PROCESO = "Confirmar Solicitud de Cambio";

	public static class EntradaConfirmarModificarPersonaTarjetaBancaria{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdTarjetaBancaria;
		public String p_IdEstadoAutorizacion;
	}

	public static class SalidaConfirmarModificarPersonaTarjetaBancaria extends SalidaEstandar{
		public SalidaConfirmarModificarPersonaTarjetaBancaria(){
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

	public static class TarjetaBancariaUpdate{
		public Long p_IdTarjetaBancaria;
		public String p_ValorNuevo;
	}

	public static class TarjetaBancariaPersona{
		public Long IdTarjetaBancaria;
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
		EntradaConfirmarModificarPersonaTarjetaBancaria in = gson.fromJson(jin.toString(), EntradaConfirmarModificarPersonaTarjetaBancaria.class);
		SalidaConfirmarModificarPersonaTarjetaBancaria out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaConfirmarModificarPersonaTarjetaBancaria logica(Connection conn, EntradaConfirmarModificarPersonaTarjetaBancaria in) throws Exception{
		logger.info("INICIO::ConfirmarModificarPersonaTarjetaBancaria");
		SalidaConfirmarModificarPersonaTarjetaBancaria salida = new SalidaConfirmarModificarPersonaTarjetaBancaria();
		SQL sql = new SQL();

		String idEntidadOrigen = String.valueOf(in.p_IdTarjetaBancaria);

		HashMap<String, Object> paramsSolicitud = new HashMap<>();
		paramsSolicitud.put("p_IdEntidadOrigen", idEntidadOrigen);
		paramsSolicitud.put("p_IdTipoEntidad", ID_TIPO_ENTIDAD);
		List<SolicitudCambio> solicitudesCambio = sql.query(conn, SQL_SELECT_SOLICITUDES_CAMBIO, paramsSolicitud, SolicitudCambio.class);

		if(solicitudesCambio.isEmpty()){
			salida.addError("SOLICITUD_NO_EXISTE", "SOLICITUD_NO_EXISTE");
			return salida;
		}

		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "IdBanco", SQL_UPDATE_ID_BANCO, in.p_IdTarjetaBancaria);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "IdTipoTarjetaBancaria", SQL_UPDATE_ID_TIPO_TARJETA_BANCARIA, in.p_IdTarjetaBancaria);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "NumeroTarjetaBancaria", SQL_UPDATE_NUMERO_TARJETA_BANCARIA, in.p_IdTarjetaBancaria);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "FechaVencimiento", SQL_UPDATE_FECHA_VENCIMIENTO, in.p_IdTarjetaBancaria);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "CodigoVerificacion", SQL_UPDATE_CODIGO_VERIFICACION, in.p_IdTarjetaBancaria);
		aplicarCambioSiExiste(conn, sql, solicitudesCambio, "IdEstado", SQL_UPDATE_ID_ESTADO, in.p_IdTarjetaBancaria);

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
			logger.error("Error registrando auditoria en ConfirmarModificarPersonaTarjetaBancaria: " + exLog.getMessage(), exLog);
		}

		logger.info("FIN::ConfirmarModificarPersonaTarjetaBancaria");
		return salida;
	}

	private boolean aplicarCambioSiExiste(Connection conn, SQL sql, List<SolicitudCambio> solicitudesCambio, String campoModificado, String sqlUpdate, Long idTarjetaBancaria) throws Exception {
		SolicitudCambio solicitud = solicitudesCambio.stream()
				.filter(s -> campoModificado.equals(s.CampoModificado))
				.findFirst().orElse(null);
		if(solicitud == null){
			return false;
		}
		TarjetaBancariaUpdate update = new TarjetaBancariaUpdate();
		update.p_IdTarjetaBancaria = idTarjetaBancaria;
		update.p_ValorNuevo = solicitud.ValorNuevo;
		sql.batch(conn, sqlUpdate, Arrays.asList(update), TarjetaBancariaUpdate.class);
		return true;
	}

	private void registrarLog(Connection conn, SQL sql, EntradaConfirmarModificarPersonaTarjetaBancaria in, List<SolicitudCambio> solicitudesCambio) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaTarjetaBancaria.registrarLog");

		HashMap<String, Object> paramsPersona = new HashMap<>();
		paramsPersona.put("IdTarjetaBancaria", new ArrayList<>(Arrays.asList(in.p_IdTarjetaBancaria)));
		List<TarjetaBancariaPersona> resultado = sql.query(conn, SQL_SELECT_ID_PERSONA, paramsPersona, TarjetaBancariaPersona.class);
		if(resultado.isEmpty()){
			logger.debug("FIN::ConfirmarModificarPersonaTarjetaBancaria.registrarLog - sin datos de persona");
			return;
		}
		TarjetaBancariaPersona datos = resultado.get(0);

		String idEntidadOrigen = String.valueOf(in.p_IdTarjetaBancaria);
		// Se filtra por IdSolicitudCambio (capturado antes de cerrar la solicitud), no por IdEstadoAutorizacion='PEN':
		// para cuando se ejecuta este metodo, ModificarEstadoRegistroSolicitudCambio ya cambio el estado de la
		// solicitud, por lo que un filtro por 'PEN' nunca encontraria las filas (quedaria el log siempre vacio).
		List<Long> idsSolicitudCambio = solicitudesCambio.stream().map(s -> s.IdSolicitudCambio).collect(java.util.stream.Collectors.toList());
		HashMap<String, Object> paramsDetalle = new HashMap<>();
		paramsDetalle.put("p_IdSolicitudCambio", idsSolicitudCambio);
		List<DetalleLog> detalles = sql.query(conn, SQL_SELECT_DETALLE_LOG, paramsDetalle, DetalleLog.class);
		if(detalles.isEmpty()){
			logger.debug("FIN::ConfirmarModificarPersonaTarjetaBancaria.registrarLog - sin detalle de cambios");
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

		logger.debug("FIN::ConfirmarModificarPersonaTarjetaBancaria.registrarLog");
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdTarjetaBancaria\":10056,\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarModificarPersonaTarjetaBancaria in = gson.fromJson(json, EntradaConfirmarModificarPersonaTarjetaBancaria.class);

			ConfirmarModificarPersonaTarjetaBancaria demo = new ConfirmarModificarPersonaTarjetaBancaria();

			try {
				SalidaConfirmarModificarPersonaTarjetaBancaria out = demo.logica(con, in);
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
