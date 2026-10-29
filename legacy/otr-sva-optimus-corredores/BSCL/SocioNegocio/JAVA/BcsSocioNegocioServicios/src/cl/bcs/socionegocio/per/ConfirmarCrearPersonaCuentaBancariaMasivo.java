package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Migrado de: PER.ConfirmarCrearPersonaCuentaBancaria (variante masiva, usada por CrearPersonaCuentaBancaria).
 * Confirma el ingreso de una o mas cuentas bancarias, actualizando su IdEstadoAutorizacion
 * (y el de sus PreferenciaPagoPortafolio asociadas) y registrando el log de auditoria detallado
 * por cuenta (persona, banco, tipo de cuenta, moneda, P.A.C.), igual que el SP singular original.
 * Salida en formato Long/MensajeSalida para mantener el mismo contrato que consume
 * CrearPersonaCuentaBancaria.SalidaCrearPersonaCuentaBancaria.
 */
public class ConfirmarCrearPersonaCuentaBancariaMasivo implements JavaCall {

	private static final Logger logger = LogManager.getLogger(ConfirmarCrearPersonaCuentaBancariaMasivo.class);

	private final String SQL_UPDATE_CUENTA_BANCARIA = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaCuentaBancariaMasivo_UpdateCuentaBancaria.sql");
	private final String SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaCuentaBancariaMasivo_UpdatePreferenciaPagoPortafolio.sql");
	private final String SQL_SELECT_DATOS_LOG = PlataformaUtils.loadResourceToString(
		"cl/bcs/socionegocio/per/sql/ConfirmarCrearPersonaCuentaBancariaMasivo_Select_DatosLog.sql");

	public static class EntradaConfirmarCrearPersonaCuentaBancariaMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<CuentaBancaria> p_CuentaBancaria;
		public String p_IdEstadoAutorizacion;

		public EntradaConfirmarCrearPersonaCuentaBancariaMasivo() {
			this.p_CuentaBancaria = new ArrayList<CuentaBancaria>();
		}
	}

	public static class SalidaConfirmarCrearPersonaCuentaBancariaMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;

		public SalidaConfirmarCrearPersonaCuentaBancariaMasivo() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}

	public static class CuentaBancaria extends BcsPojo {
		public Long IdCuentaBancaria;
	}

	public static class UpdateCuentaBancaria extends BcsPojo {
		public Long IdCuentaBancaria;
		public String IdEstadoAutorizacion;
	}

	public static class DatosLog extends BcsPojo {
		public Long IdCuentaBancaria;
		public Long IdPersona;
		public String NombreCompleto;
		public String DocumentoIdentidad;
		public String TipoDocumentoIdentidad;
		public String NumeroCuentaBancaria;
		public String NombreBanco;
		public String NombreMoneda;
		public String NombreTipoCuentaBancaria;
		public String EsPac;
	}

	public SalidaConfirmarCrearPersonaCuentaBancariaMasivo logica(Connection conn, EntradaConfirmarCrearPersonaCuentaBancariaMasivo in) throws Exception {
		logger.info("INIT ConfirmarCrearPersonaCuentaBancariaMasivo");
		SalidaConfirmarCrearPersonaCuentaBancariaMasivo salida = new SalidaConfirmarCrearPersonaCuentaBancariaMasivo();
		SQL sql = new SQL();

		try {
			logger.debug("Actualizar estado de autorizacion de cuentas bancarias");
			List<UpdateCuentaBancaria> updateList = in.p_CuentaBancaria.stream().map(cuentaBancaria -> {
				UpdateCuentaBancaria updateCuenta = new UpdateCuentaBancaria();
				updateCuenta.IdCuentaBancaria = cuentaBancaria.IdCuentaBancaria;
				updateCuenta.IdEstadoAutorizacion = in.p_IdEstadoAutorizacion;
				return updateCuenta;
			}).collect(Collectors.toList());

			sql.batch(conn, SQL_UPDATE_CUENTA_BANCARIA, updateList, UpdateCuentaBancaria.class);
			sql.batch(conn, SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO, updateList, UpdateCuentaBancaria.class);

			agregarLogDetallado(conn, sql, in);

		} catch (Exception ex) {
			logger.error("Error en ConfirmarCrearPersonaCuentaBancariaMasivo: " + ex.getMessage());
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_CONFIRMAR_CREAR_PERSONA_CUENTA_BANCARIA_MASIVO", ex.getMessage()));
			salida.P_OUT_ERRORESCONTADOR = Long.valueOf(salida.P_OUT_MENSAJES.size());
		}

		logger.info("END ConfirmarCrearPersonaCuentaBancariaMasivo");
		return salida;
	}

	private void agregarLogDetallado(Connection conn, SQL sql, EntradaConfirmarCrearPersonaCuentaBancariaMasivo in) throws Exception {
		logger.debug("INICIO::ConfirmarCrearPersonaCuentaBancariaMasivo.agregarLogDetallado");

		if (in.p_CuentaBancaria.isEmpty()) {
			return;
		}

		List<Long> idsCuentaBancaria = in.p_CuentaBancaria.stream().map(cuentaBancaria -> cuentaBancaria.IdCuentaBancaria).collect(Collectors.toList());
		HashMap<String, Object> paramsLog = new HashMap<>();
		paramsLog.put("IdCuentaBancaria", idsCuentaBancaria);
		List<DatosLog> datosLogs = sql.query(conn, SQL_SELECT_DATOS_LOG, paramsLog, DatosLog.class);
		Map<Long, DatosLog> datosLogPorCuenta = datosLogs.stream().collect(Collectors.toMap(datosLog -> datosLog.IdCuentaBancaria, datosLog -> datosLog));

		String fechaSistema = Utils.getFechaHoraSistema(conn, Utils.getSchema(), in.p_IdEmpresa);
		String fechaSistemaLog = fechaSistema.substring(0, 10);

		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

		for (CuentaBancaria cuentaBancaria : in.p_CuentaBancaria) {
			DatosLog datosLog = datosLogPorCuenta.get(cuentaBancaria.IdCuentaBancaria);
			if (datosLog == null) {
				datosLog = new DatosLog();
			}

			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = cuentaBancaria.IdCuentaBancaria;
			log.IdTipoLog = "INGRESO";
			log.IdModulo = "SOC";
			log.IdUsuario = in.p_IdUsuario;
			log.IdEmpresa = in.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistemaLog;
			log.IdUsuarioAtendido = in.p_IdUsuario;
			log.IdPersona = datosLog.IdPersona;
			log.NombreCliente = datosLog.NombreCompleto;
			log.IdDocumentoIdentidad = datosLog.DocumentoIdentidad;
			log.IdTipoDocumentoIdentidad = datosLog.TipoDocumentoIdentidad;
			log.IdTipoRegistro = "CUENTABANCARIA";
			log.NombreProceso = "Confirmar Ingreso cuenta Bancaria";
			log.IdRegistro = String.valueOf(cuentaBancaria.IdCuentaBancaria);
			log.EstadoProceso = in.p_IdEstadoAutorizacion;
			entradaAgregarLog.p_Log.add(log);

			agregarDetalleLog(entradaAgregarLog, cuentaBancaria.IdCuentaBancaria, "NumeroCuentaBancaria", datosLog.NumeroCuentaBancaria);
			agregarDetalleLog(entradaAgregarLog, cuentaBancaria.IdCuentaBancaria, "Banco", datosLog.NombreBanco);
			agregarDetalleLog(entradaAgregarLog, cuentaBancaria.IdCuentaBancaria, "TipoCuentaBancaria", datosLog.NombreTipoCuentaBancaria);
			agregarDetalleLog(entradaAgregarLog, cuentaBancaria.IdCuentaBancaria, "Moneda", datosLog.NombreMoneda);
			agregarDetalleLog(entradaAgregarLog, cuentaBancaria.IdCuentaBancaria, "P.A.C", datosLog.EsPac);
		}

		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(conn, entradaAgregarLog);

		logger.debug("FIN::ConfirmarCrearPersonaCuentaBancariaMasivo.agregarLogDetallado");
	}

	private void agregarDetalleLog(AgregarLog.InAgregarLog entradaAgregarLog, Long idCabeceraLog, String campoRegistro, String valorNuevo) {
		AgregarLog.InAgregarLog.DetalleLog detLog = new AgregarLog.InAgregarLog.DetalleLog();
		detLog.IdCabeceraLog = idCabeceraLog;
		detLog.IdTrabajo = 0L;
		detLog.SubRegistro = "CUENTABANCARIA";
		detLog.CampoRegistro = campoRegistro;
		detLog.ValorAntiguo = "";
		detLog.ValorNuevo = valorNuevo != null ? valorNuevo : "";
		entradaAgregarLog.p_LogDetalle.add(detLog);
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaConfirmarCrearPersonaCuentaBancariaMasivo in = gson.fromJson(jin.toString(), EntradaConfirmarCrearPersonaCuentaBancariaMasivo.class);
		SalidaConfirmarCrearPersonaCuentaBancariaMasivo out = logica(conn, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76770,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_CuentaBancaria\":[{\"IdCuentaBancaria\":123456}],\"p_IdEstadoAutorizacion\":\"ING\"}";
			EntradaConfirmarCrearPersonaCuentaBancariaMasivo entrada = gson.fromJson(json, EntradaConfirmarCrearPersonaCuentaBancariaMasivo.class);
			ConfirmarCrearPersonaCuentaBancariaMasivo demo = new ConfirmarCrearPersonaCuentaBancariaMasivo();
			SalidaConfirmarCrearPersonaCuentaBancariaMasivo salida = demo.logica(conn, entrada);
			System.out.println(salida.toString());
			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
