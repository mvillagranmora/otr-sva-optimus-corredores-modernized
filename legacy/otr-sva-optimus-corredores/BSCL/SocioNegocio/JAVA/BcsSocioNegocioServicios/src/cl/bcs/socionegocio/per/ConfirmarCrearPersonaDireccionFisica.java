package cl.bcs.socionegocio.per;

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
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Migrado de: PER.ConfirmarCrearPersonaDireccionFisica
 * Confirma el ingreso de una direccion fisica de una persona, actualizando su
 * IdEstadoAutorizacion y registrando el log de auditoria.
 * Invocado desde el modulo de autorizaciones o del propio negocio al no requerir autorizacion.
 * Nota: el SP original tiene comentada (deshabilitada) la llamada a
 * PLA.ModificarEstadoRegistroSolicitudCambio, por lo que no se replica aqui.
 */
public class ConfirmarCrearPersonaDireccionFisica implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ConfirmarCrearPersonaDireccionFisica.class);

	private final String SQL_UPDATE_DIRECCION_FISICA = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaDireccionFisica_Update.sql");
	private final String SQL_SELECT_DATOS_LOG = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaDireccionFisica_Select_DatosLog.sql");

	public static class EntradaConfirmarCrearPersonaDireccionFisica {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDireccionFisica;
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

	public static class SalidaConfirmarCrearPersonaDireccionFisica {
		public int P_OUT_ADVERTENCIASCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIASDETALLE;
		public int P_OUT_ERRORESCONTADOR;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		public SalidaConfirmarCrearPersonaDireccionFisica() {
			this.P_OUT_ADVERTENCIASCONTADOR = 0;
			this.P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0;
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class UpdateDireccionFisica {
		public Long IdDireccionFisica;
		public String IdEstadoAutorizacion;
	}

	public static class DatosLog {
		public Long IdPersona;
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public String TipoDocumentoIdentidad;
		public String Ciudad;
		public String Pais;
		public String Numero;
		public String Sector;
		public String CodigoPostal;
		public String Proposito;
		public String Comentario;
		public String Estado;
	}

	public SalidaConfirmarCrearPersonaDireccionFisica logica(Connection conn, EntradaConfirmarCrearPersonaDireccionFisica in) throws Exception {
		logger.info("INIT ConfirmarCrearPersonaDireccionFisica");
		SalidaConfirmarCrearPersonaDireccionFisica salida = new SalidaConfirmarCrearPersonaDireccionFisica();
		SQL sql = new SQL();

		try {
			String v_IdEntidadOrigen = String.valueOf(in.p_IdDireccionFisica);
			String v_TipoEntidad = "DIRECCIONFISICA";

			// Actualizar estado de autorizacion de la direccion fisica
			logger.debug("Actualizar estado de autorizacion de direccion fisica");
			UpdateDireccionFisica updateDireccion = new UpdateDireccionFisica();
			updateDireccion.IdDireccionFisica = in.p_IdDireccionFisica;
			updateDireccion.IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;

			List<UpdateDireccionFisica> updateList = new ArrayList<>();
			updateList.add(updateDireccion);
			sql.batch(conn, SQL_UPDATE_DIRECCION_FISICA, updateList, UpdateDireccionFisica.class);

			// Obtener datos de persona/direccion para el log
			logger.debug("Obtener datos para el log");
			HashMap<String, Object> paramsLog = new HashMap<>();
			paramsLog.put("IdDireccionFisica", in.p_IdDireccionFisica);

			List<DatosLog> datosLogList = sql.query(conn, SQL_SELECT_DATOS_LOG, paramsLog, DatosLog.class);
			DatosLog datosLog = (datosLogList != null && !datosLogList.isEmpty()) ? datosLogList.get(0) : new DatosLog();

			String v_FechaSistema = Utils.getFechaHoraSistema(conn, Utils.getSchema(), in.p_IdEmpresa);

			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = 1L;
			log.IdTipoLog = "INGRESO";
			log.IdModulo = "SOC";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = v_FechaSistema.substring(0, 10);
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdPersona = datosLog.IdPersona;
			log.NombreCliente = datosLog.NombreCompleto;
			log.IdDocumentoIdentidad = datosLog.DocumentoIdentidad;
			log.IdTipoDocumentoIdentidad = datosLog.TipoDocumentoIdentidad;
			log.IdTipoRegistro = v_TipoEntidad;
			log.NombreProceso = "Confirmar Ingreso Direccion Fisica";
			log.IdRegistro = v_IdEntidadOrigen;
			log.EstadoProceso = in.p_IdEstadoAutorizacion;
			entradaAgregarLog.p_Log.add(log);

			agregarDetalleLog(entradaAgregarLog, v_TipoEntidad, "Ciudad", datosLog.Ciudad);
			agregarDetalleLog(entradaAgregarLog, v_TipoEntidad, "Pais", datosLog.Pais);
			agregarDetalleLog(entradaAgregarLog, v_TipoEntidad, "Numero", datosLog.Numero);
			agregarDetalleLog(entradaAgregarLog, v_TipoEntidad, "Sector", datosLog.Sector);
			agregarDetalleLog(entradaAgregarLog, v_TipoEntidad, "CodigoPostal", datosLog.CodigoPostal);
			agregarDetalleLog(entradaAgregarLog, v_TipoEntidad, "Proposito", datosLog.Proposito);
			agregarDetalleLog(entradaAgregarLog, v_TipoEntidad, "Comentario", datosLog.Comentario);
			agregarDetalleLog(entradaAgregarLog, v_TipoEntidad, "Estado", datosLog.Estado);

			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);

		} catch (Exception ex) {
			logger.error("Error en ConfirmarCrearPersonaDireccionFisica: " + ex.getMessage());
			salida.P_OUT_ERRORESDETALLE.add(new ErrorDetalle("ERROR_CONFIRMAR_CREAR_PERSONA_DIRECCION_FISICA", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESDETALLE.size();
		}

		logger.info("END ConfirmarCrearPersonaDireccionFisica");
		return salida;
	}

	private void agregarDetalleLog(AgregarLog.InAgregarLog entradaAgregarLog, String subRegistro, String campoRegistro, String valorNuevo) {
		AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
		detLog.IdCabeceraLog = 1L;
		detLog.IdTrabajo = 0L;
		detLog.SubRegistro = subRegistro;
		detLog.CampoRegistro = campoRegistro;
		detLog.ValorAntiguo = "";
		detLog.ValorNuevo = valorNuevo != null ? valorNuevo : "";
		entradaAgregarLog.p_LogDetalle.add(detLog);
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarCrearPersonaDireccionFisica in = gson.fromJson(jin.toString(), EntradaConfirmarCrearPersonaDireccionFisica.class);
		SalidaConfirmarCrearPersonaDireccionFisica out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdDireccionFisica\":123456,\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarCrearPersonaDireccionFisica entrada = gson.fromJson(json, EntradaConfirmarCrearPersonaDireccionFisica.class);
			ConfirmarCrearPersonaDireccionFisica demo = new ConfirmarCrearPersonaDireccionFisica();
			SalidaConfirmarCrearPersonaDireccionFisica salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
