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

public class ConfirmarEliminarPersonaDireccionFisica implements JavaCall{

	private static final Logger logger = LogManager.getLogger(ConfirmarEliminarPersonaDireccionFisica.class);

	private final String SQL_SELECT_SOLICITUDES_CAMBIO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaDireccionFisica_SelectSolicitudesCambio.sql");
	private final String SQL_UPDATE_BORRADO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaDireccionFisica_UpdateBorrado.sql");
	private final String SQL_SELECT_DIRECCION_FISICA_LOG = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarEliminarPersonaDireccionFisica_SelectDireccionFisicaLog.sql");

	private static final String ID_TIPO_ENTIDAD = "DIRECCIONFISICA-BORRAR";
	private static final String NOMBRE_PROCESO = "Confirmar Solicitud de Cambio";

	public static class EntradaConfirmarEliminarPersonaDireccionFisica{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDireccionFisica;
		public String p_IdEstadoAutorizacion;
	}

	public static class SalidaConfirmarEliminarPersonaDireccionFisica extends SalidaEstandar{
		public SalidaConfirmarEliminarPersonaDireccionFisica(){
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
		public String p_Borrado;
	}

	public static class DireccionFisicaLog{
		public Long IdDireccionFisica;
		public Long IdPersona;
		public String Calle;
		public String Numero;
		public String NombreCiudad;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarEliminarPersonaDireccionFisica in = gson.fromJson(jin.toString(), EntradaConfirmarEliminarPersonaDireccionFisica.class);
		SalidaConfirmarEliminarPersonaDireccionFisica out = logica(conn, in);
		JsonParser parser = new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}

	public SalidaConfirmarEliminarPersonaDireccionFisica logica(Connection conn, EntradaConfirmarEliminarPersonaDireccionFisica in) throws Exception{
		logger.info("INICIO::ConfirmarEliminarPersonaDireccionFisica");
		SalidaConfirmarEliminarPersonaDireccionFisica salida = new SalidaConfirmarEliminarPersonaDireccionFisica();
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

		SolicitudCambio solicitudBorrado = solicitudesCambio.stream()
				.filter(s -> "Borrado".equals(s.CampoModificado))
				.findFirst().orElse(null);

		if(solicitudBorrado != null){
			DireccionFisicaUpdate update = new DireccionFisicaUpdate();
			update.p_IdDireccionFisica = in.p_IdDireccionFisica;
			update.p_Borrado = solicitudBorrado.ValorNuevo;
			sql.batch(conn, SQL_UPDATE_BORRADO, Arrays.asList(update), DireccionFisicaUpdate.class);
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
			logger.error("Error registrando auditoria en ConfirmarEliminarPersonaDireccionFisica: " + exLog.getMessage(), exLog);
		}

		logger.info("FIN::ConfirmarEliminarPersonaDireccionFisica");
		return salida;
	}

	private void registrarLog(Connection conn, SQL sql, EntradaConfirmarEliminarPersonaDireccionFisica in, List<SolicitudCambio> solicitudesCambio) throws Exception {
		logger.debug("INICIO::ConfirmarEliminarPersonaDireccionFisica.registrarLog");

		HashMap<String, Object> params = new HashMap<>();
		params.put("IdDireccionFisica", new ArrayList<>(Arrays.asList(in.p_IdDireccionFisica)));
		List<DireccionFisicaLog> resultado = sql.query(conn, SQL_SELECT_DIRECCION_FISICA_LOG, params, DireccionFisicaLog.class);
		if(resultado.isEmpty()){
			logger.debug("FIN::ConfirmarEliminarPersonaDireccionFisica.registrarLog - sin datos de direccion/persona");
			return;
		}
		DireccionFisicaLog datos = resultado.get(0);

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
		log.IdRegistro = String.valueOf(in.p_IdDireccionFisica);
		log.NombreProceso = NOMBRE_PROCESO;
		log.EstadoProceso = in.p_IdEstadoAutorizacion;
		entrada.p_Log.add(log);

		String direccionCompleta = (datos.Calle + " " + datos.Numero + " " + datos.NombreCiudad).trim();

		for(SolicitudCambio sol : solicitudesCambio){
			boolean esBorrado = "S".equals(sol.ValorNuevo);
			String valorAntiguo = esBorrado ? direccionCompleta : sol.ValorAntiguo;
			String valorNuevo = esBorrado ? "" : sol.ValorNuevo;
			entrada.agregarDetalleLogBasico(datos.IdPersona, 0L, sol.CampoModificado, valorAntiguo, valorNuevo, sol.IdTipoEntidad);
		}

		new AgregarLog().logica(conn, entrada);

		logger.debug("FIN::ConfirmarEliminarPersonaDireccionFisica.registrarLog");
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdDireccionFisica\":2469693,\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarEliminarPersonaDireccionFisica in = gson.fromJson(json, EntradaConfirmarEliminarPersonaDireccionFisica.class);

			ConfirmarEliminarPersonaDireccionFisica demo = new ConfirmarEliminarPersonaDireccionFisica();

			try {
				SalidaConfirmarEliminarPersonaDireccionFisica out = demo.logica(con, in);
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
