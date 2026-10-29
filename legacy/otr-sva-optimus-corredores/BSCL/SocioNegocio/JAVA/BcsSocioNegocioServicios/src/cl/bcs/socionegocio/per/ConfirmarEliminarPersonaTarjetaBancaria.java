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

public class ConfirmarEliminarPersonaTarjetaBancaria implements JavaCall{

	private static final Logger logger = LogManager.getLogger(ConfirmarEliminarPersonaTarjetaBancaria.class);

	private final String SQL_SELECT_SOLICITUDES_CAMBIO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaTarjetaBancaria_SelectSolicitudesCambio.sql");
	private final String SQL_UPDATE_BORRADO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaTarjetaBancaria_UpdateBorrado.sql");
	private final String SQL_SELECT_TARJETA_BANCARIA_LOG = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaTarjetaBancaria_SelectTarjetaBancariaLog.sql");

	private static final String ID_TIPO_ENTIDAD = "TARJETABANCARIA-BORRAR";
	private static final String NOMBRE_PROCESO = "Confirmar Eliminacion";

	public static class EntradaConfirmarEliminarPersonaTarjetaBancaria{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdTarjetaBancaria;
		public String p_IdEstadoAutorizacion;
	}

	public static class SalidaConfirmarEliminarPersonaTarjetaBancaria extends SalidaEstandar{
		public SalidaConfirmarEliminarPersonaTarjetaBancaria(){
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
		public String p_Borrado;
	}

	public static class TarjetaBancariaLog{
		public Long IdTarjetaBancaria;
		public Long IdPersona;
		public String NumeroTarjetaBancaria;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarEliminarPersonaTarjetaBancaria in = gson.fromJson(jin.toString(), EntradaConfirmarEliminarPersonaTarjetaBancaria.class);
		SalidaConfirmarEliminarPersonaTarjetaBancaria out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaConfirmarEliminarPersonaTarjetaBancaria logica(Connection conn, EntradaConfirmarEliminarPersonaTarjetaBancaria in) throws Exception{
		logger.info("INICIO::ConfirmarEliminarPersonaTarjetaBancaria");
		SalidaConfirmarEliminarPersonaTarjetaBancaria salida = new SalidaConfirmarEliminarPersonaTarjetaBancaria();
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

		SolicitudCambio solicitudBorrado = solicitudesCambio.stream()
				.filter(s -> "Borrado".equals(s.CampoModificado))
				.findFirst().orElse(null);

		if(solicitudBorrado != null){
			TarjetaBancariaUpdate update = new TarjetaBancariaUpdate();
			update.p_IdTarjetaBancaria = in.p_IdTarjetaBancaria;
			update.p_Borrado = solicitudBorrado.ValorNuevo;
			sql.batch(conn, SQL_UPDATE_BORRADO, Arrays.asList(update), TarjetaBancariaUpdate.class);
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

		try {
			registrarLog(conn, sql, in, solicitudesCambio);
		} catch (Exception exLog) {
			logger.error("Error registrando auditoria en ConfirmarEliminarPersonaTarjetaBancaria: " + exLog.getMessage(), exLog);
		}

		logger.info("FIN::ConfirmarEliminarPersonaTarjetaBancaria");
		return salida;
	}

	private void registrarLog(Connection conn, SQL sql, EntradaConfirmarEliminarPersonaTarjetaBancaria in, List<SolicitudCambio> solicitudesCambio) throws Exception {
		logger.debug("INICIO::ConfirmarEliminarPersonaTarjetaBancaria.registrarLog");

		HashMap<String, Object> params = new HashMap<>();
		params.put("IdTarjetaBancaria", new ArrayList<>(Arrays.asList(in.p_IdTarjetaBancaria)));
		List<TarjetaBancariaLog> resultado = sql.query(conn, SQL_SELECT_TARJETA_BANCARIA_LOG, params, TarjetaBancariaLog.class);
		if(resultado.isEmpty()){
			logger.debug("FIN::ConfirmarEliminarPersonaTarjetaBancaria.registrarLog - sin datos de tarjeta/persona");
			return;
		}
		TarjetaBancariaLog datos = resultado.get(0);

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
		log.IdRegistro = String.valueOf(in.p_IdTarjetaBancaria);
		log.NombreProceso = NOMBRE_PROCESO;
		log.EstadoProceso = in.p_IdEstadoAutorizacion;
		entrada.p_Log.add(log);

		for(SolicitudCambio sol : solicitudesCambio){
			boolean esBorrado = "S".equals(sol.ValorNuevo);
			String valorAntiguo = esBorrado ? "Numero tarjeta Bancaria " + datos.NumeroTarjetaBancaria : sol.ValorAntiguo;
			String valorNuevo = esBorrado ? "" : sol.ValorNuevo;
			entrada.agregarDetalleLogBasico(datos.IdPersona, 0L, sol.CampoModificado, valorAntiguo, valorNuevo, sol.IdTipoEntidad);
		}

		new AgregarLog().logica(conn, entrada);

		logger.debug("FIN::ConfirmarEliminarPersonaTarjetaBancaria.registrarLog");
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdTarjetaBancaria\":10056,\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarEliminarPersonaTarjetaBancaria in = gson.fromJson(json, EntradaConfirmarEliminarPersonaTarjetaBancaria.class);

			ConfirmarEliminarPersonaTarjetaBancaria demo = new ConfirmarEliminarPersonaTarjetaBancaria();

			try {
				SalidaConfirmarEliminarPersonaTarjetaBancaria out = demo.logica(con, in);
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
