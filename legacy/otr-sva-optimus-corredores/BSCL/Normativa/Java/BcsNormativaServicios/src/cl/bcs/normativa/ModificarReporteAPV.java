package cl.bcs.normativa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.enr.pojo.SalidaAdministrarBorrador;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarReporteAPV implements JavaCall {

	static final Logger logger = LogManager.getLogger(ModificarReporteAPV.class);

	private static final String MONEDA_FORMATO = "CLP";

	public String SQL_SELECT_REPORTE_APV = loadResourceToString("cl/bcs/normativa/sql/ModificarReporteAPV_SelectReporteAPV.sql");
	public String SQL_UPDATE_REPORTE_APV = loadResourceToString("cl/bcs/normativa/sql/ModificarReporteAPV_UpdateReporteAPV.sql");

	public static class EntradaModificarReporteAPV {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<InReporteAPV> p_ReporteAPV;
	}

	public static class InReporteAPV extends BcsPojo {
		public Long IdReporteAPV;
		public String DocumentoIdentidad;
		public Long IdDocumentoIdentidad;
		public Long IdCliente;
		public String TipoDocumentoIdentidad;
		public String NombreCliente;
		public String CodigoRegimenTributario;
		public String NombreRegimenTributario;
		public String TipoProducto;
		public Long IdPlanTipoInversion;
		public String NombrePlanTipoInversion;
		public Timestamp FechaCierre;
		public BigDecimal SaldoAcumulado;
		public BigDecimal SaldoRegimenTributarioA;
		public BigDecimal SaldoRegimenTributarioB;
		public BigDecimal SaldoBonificacionEstatal;
		public BigDecimal SaldoAPVCEmpleador;
		public BigDecimal SaldoAPVCTrabajadorA;
		public BigDecimal SaldoAPVCTrabajadorB;
		public BigDecimal SaldoAPVCBonificacionEstatal;
		public Long IdUsuarioModificacion;
		public Date FechaModificacion;
	}

	public static class ReporteAPV {
		public Long IdReporteAPV;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaModificarReporteAPV jin = gson.fromJson(in.toString(), EntradaModificarReporteAPV.class);
		SalidaAdministrarBorrador salida = logica(conn, jin);

		JsonParser parser = new JsonParser();
		JsonObject datasalida = parser.parse(gson.toJson(salida)).getAsJsonObject();
		return datasalida;
	}

	public SalidaAdministrarBorrador logica(Connection con, EntradaModificarReporteAPV entrada) throws Exception {
		logger.info("INICIO::ModificarReporteAPV");
		long inicio = System.currentTimeMillis();
		SalidaAdministrarBorrador salida = new SalidaAdministrarBorrador();

		RevisarRequisitosBasicos basicos = new RevisarRequisitosBasicos();
		RevisarRequisitosBasicosSalida salidaBasicos = basicos.logica(con, entrada.p_IdEmpresa, entrada.p_IdUsuario,
				entrada.p_IdModulo);

		if (MensajeSalida.getNumeroErrores(salidaBasicos.p_out_Mensajes) > 0) {
			salida.P_OUT_MENSAJES.addAll(salidaBasicos.p_out_Mensajes);
		}
		;

		Map<Long, InReporteAPV> mapaValoresAntiguos = obtenerValoresAntiguos(con, entrada);
		actualizarReportesAPV(con, entrada);
		agregarLogs(con, entrada, mapaValoresAntiguos);

		for (InReporteAPV reporteAPV : entrada.p_ReporteAPV) {
			HashMap<String, String> params = new HashMap<>();
			params.put("IdReporteAPV", String.valueOf(reporteAPV.IdReporteAPV));
			salida.P_OUT_MENSAJES.add(new MensajeSalida(reporteAPV.IdReporteAPV, MensajeSalida.MENSAJE_OK,
					"REPORTE_APV_MODIFICADO", params, con, entrada.p_IdEmpresa));
		}

		long fin = System.currentTimeMillis();
		logger.info("FIN::ModificarReporteAPV {}", (fin - inicio));

		return salida;
	}

	public Map<Long, InReporteAPV> obtenerValoresAntiguos(Connection con, EntradaModificarReporteAPV entrada)
			throws Exception {
		SQL sql = new SQL();
		List<Long> idsReporteAPV = entrada.p_ReporteAPV.stream().map(r -> r.IdReporteAPV).collect(Collectors.toList());

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdReporteAPV", idsReporteAPV);

		logger.debug("INICIO::SQL_SELECT_REPORTE_APV");
		List<InReporteAPV> lista = sql.query(con, SQL_SELECT_REPORTE_APV, params, InReporteAPV.class);
		logger.debug("FIN::SQL_SELECT_REPORTE_APV");

		Map<Long, InReporteAPV> mapaBD = lista.stream().collect(Collectors.toMap(r -> r.IdReporteAPV, r -> r));
		return mapaBD;
	}

	private void agregarLogs(Connection con, EntradaModificarReporteAPV entrada,
			Map<Long, InReporteAPV> mapaValoresAntiguos) throws Exception {
		logger.debug("INICIO::ModificarReporteAPV.AgregarLog");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,
				"PLATAFORMA", "PLA.FechaSistema");

		try {
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();

			for (InReporteAPV reporteNuevo : entrada.p_ReporteAPV) {
				InReporteAPV reporteAntiguo = mapaValoresAntiguos.get(reporteNuevo.IdReporteAPV);

				if (reporteAntiguo != null) {
					List<AgregarLog.InAgregarLog.DetalleLog> detallesTemporales = new ArrayList<>();

					if (reporteNuevo.SaldoAcumulado != null && reporteAntiguo.SaldoAcumulado != null
							&& reporteNuevo.SaldoAcumulado.compareTo(reporteAntiguo.SaldoAcumulado.setScale(0, RoundingMode.HALF_UP)) != 0) {
						AgregarLog.InAgregarLog.DetalleLog det = new AgregarLog.InAgregarLog.DetalleLog();
						det.IdCabeceraLog = reporteNuevo.IdReporteAPV;
						det.SubRegistro = reporteNuevo.CodigoRegimenTributario + "/"+ reporteNuevo.NombrePlanTipoInversion;
						det.CampoRegistro = "SaldoAcumulado";
						det.ValorAntiguo = MonedaFormato.formatear(con, reporteAntiguo.SaldoAcumulado, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						det.ValorNuevo = MonedaFormato.formatear(con, reporteNuevo.SaldoAcumulado, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						detallesTemporales.add(det);
					}

					if (reporteNuevo.SaldoRegimenTributarioA != null && reporteAntiguo.SaldoRegimenTributarioA != null
							&& reporteNuevo.SaldoRegimenTributarioA.compareTo(reporteAntiguo.SaldoRegimenTributarioA.setScale(0, RoundingMode.HALF_UP)) != 0) {
						AgregarLog.InAgregarLog.DetalleLog det = new AgregarLog.InAgregarLog.DetalleLog();
						det.IdCabeceraLog = reporteNuevo.IdReporteAPV;
						det.SubRegistro = reporteNuevo.CodigoRegimenTributario + "/"+ reporteNuevo.NombrePlanTipoInversion;
						det.CampoRegistro = "SaldoRegimenTributarioA";
						det.ValorAntiguo = MonedaFormato.formatear(con, reporteAntiguo.SaldoRegimenTributarioA, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						det.ValorNuevo = MonedaFormato.formatear(con, reporteNuevo.SaldoRegimenTributarioA, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						detallesTemporales.add(det);
					}

					if (reporteNuevo.SaldoRegimenTributarioB != null && reporteAntiguo.SaldoRegimenTributarioB != null
							&& reporteNuevo.SaldoRegimenTributarioB.compareTo(reporteAntiguo.SaldoRegimenTributarioB.setScale(0, RoundingMode.HALF_UP)) != 0) {
						AgregarLog.InAgregarLog.DetalleLog det = new AgregarLog.InAgregarLog.DetalleLog();
						det.IdCabeceraLog = reporteNuevo.IdReporteAPV;
						det.SubRegistro = reporteNuevo.CodigoRegimenTributario + "/"+ reporteNuevo.NombrePlanTipoInversion;
						det.CampoRegistro = "SaldoRegimenTributarioB";
						det.ValorAntiguo = MonedaFormato.formatear(con, reporteAntiguo.SaldoRegimenTributarioB, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						det.ValorNuevo = MonedaFormato.formatear(con, reporteNuevo.SaldoRegimenTributarioB, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						detallesTemporales.add(det);
					}

					if (reporteNuevo.SaldoBonificacionEstatal != null && reporteAntiguo.SaldoBonificacionEstatal != null
							&& reporteNuevo.SaldoBonificacionEstatal.compareTo(reporteAntiguo.SaldoBonificacionEstatal.setScale(0, RoundingMode.HALF_UP)) != 0) {
						AgregarLog.InAgregarLog.DetalleLog det = new AgregarLog.InAgregarLog.DetalleLog();
						det.IdCabeceraLog = reporteNuevo.IdReporteAPV;
						det.SubRegistro = reporteNuevo.CodigoRegimenTributario + "/"+ reporteNuevo.NombrePlanTipoInversion;
						det.CampoRegistro = "SaldoBonificacionEstatal";
						det.ValorAntiguo = MonedaFormato.formatear(con, reporteAntiguo.SaldoBonificacionEstatal, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						det.ValorNuevo = MonedaFormato.formatear(con, reporteNuevo.SaldoBonificacionEstatal, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						detallesTemporales.add(det);
					}

					if (reporteNuevo.SaldoAPVCEmpleador != null && reporteAntiguo.SaldoAPVCEmpleador != null
							&& reporteNuevo.SaldoAPVCEmpleador.compareTo(reporteAntiguo.SaldoAPVCEmpleador.setScale(0, RoundingMode.HALF_UP)) != 0) {
						AgregarLog.InAgregarLog.DetalleLog det = new AgregarLog.InAgregarLog.DetalleLog();
						det.IdCabeceraLog = reporteNuevo.IdReporteAPV;
						det.SubRegistro = reporteNuevo.CodigoRegimenTributario + "/"+ reporteNuevo.NombrePlanTipoInversion;
						det.CampoRegistro = "SaldoAPVCEmpleador";
						det.ValorAntiguo = MonedaFormato.formatear(con, reporteAntiguo.SaldoAPVCEmpleador, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						det.ValorNuevo = MonedaFormato.formatear(con, reporteNuevo.SaldoAPVCEmpleador, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						detallesTemporales.add(det);
					}

					if (reporteNuevo.SaldoAPVCTrabajadorA != null && reporteAntiguo.SaldoAPVCTrabajadorA != null
							&& reporteNuevo.SaldoAPVCTrabajadorA.compareTo(reporteAntiguo.SaldoAPVCTrabajadorA.setScale(0, RoundingMode.HALF_UP)) != 0) {
						AgregarLog.InAgregarLog.DetalleLog det = new AgregarLog.InAgregarLog.DetalleLog();
						det.IdCabeceraLog = reporteNuevo.IdReporteAPV;
						det.SubRegistro = reporteNuevo.CodigoRegimenTributario + "/"+ reporteNuevo.NombrePlanTipoInversion;
						det.CampoRegistro = "SaldoAPVCTrabajadorA";
						det.ValorAntiguo = MonedaFormato.formatear(con, reporteAntiguo.SaldoAPVCTrabajadorA, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						det.ValorNuevo = MonedaFormato.formatear(con, reporteNuevo.SaldoAPVCTrabajadorA, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						detallesTemporales.add(det);
					}

					if (reporteNuevo.SaldoAPVCTrabajadorB != null && reporteAntiguo.SaldoAPVCTrabajadorB != null
							&& reporteNuevo.SaldoAPVCTrabajadorB.compareTo(reporteAntiguo.SaldoAPVCTrabajadorB.setScale(0, RoundingMode.HALF_UP)) != 0) {
						AgregarLog.InAgregarLog.DetalleLog det = new AgregarLog.InAgregarLog.DetalleLog();
						det.IdCabeceraLog = reporteNuevo.IdReporteAPV;
						det.SubRegistro = reporteNuevo.CodigoRegimenTributario + "/"+ reporteNuevo.NombrePlanTipoInversion;
						det.CampoRegistro = "SaldoAPVCTrabajadorB";
						det.ValorAntiguo = MonedaFormato.formatear(con, reporteAntiguo.SaldoAPVCTrabajadorB, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						det.ValorNuevo = MonedaFormato.formatear(con, reporteNuevo.SaldoAPVCTrabajadorB, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						detallesTemporales.add(det);
					}

					if (reporteNuevo.SaldoAPVCBonificacionEstatal != null
							&& reporteAntiguo.SaldoAPVCBonificacionEstatal != null
							&& reporteNuevo.SaldoAPVCBonificacionEstatal.compareTo(reporteAntiguo.SaldoAPVCBonificacionEstatal.setScale(0,RoundingMode.HALF_UP)) != 0) {
						AgregarLog.InAgregarLog.DetalleLog det = new AgregarLog.InAgregarLog.DetalleLog();
						det.IdCabeceraLog = reporteNuevo.IdReporteAPV;
						det.SubRegistro = reporteNuevo.CodigoRegimenTributario + "/"+ reporteNuevo.NombrePlanTipoInversion;
						det.CampoRegistro = "SaldoAPVCBonificacionEstatal";
						det.ValorAntiguo = MonedaFormato.formatear(con, reporteAntiguo.SaldoAPVCBonificacionEstatal, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						det.ValorNuevo = MonedaFormato.formatear(con, reporteNuevo.SaldoAPVCBonificacionEstatal, MonedaFormato.MONTO, MONEDA_FORMATO, "", "").valorConFormato;
						detallesTemporales.add(det);
					}

					if (!detallesTemporales.isEmpty()) {
						AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
						log.IdCabeceraLog = reporteNuevo.IdReporteAPV;
						log.IdTipoLog = "MODIFICACION";
						log.IdModulo = entrada.p_IdModulo;
						log.IdUsuario = entrada.p_IdUsuario;
						log.IdEmpresa = entrada.p_IdEmpresa;
						log.FechaSistemaLog = fechaSistema;
						log.IdUsuarioAtendido = entrada.p_IdUsuario;
						log.NombreCliente = reporteAntiguo.NombreCliente;
						log.IdCliente = reporteAntiguo.IdCliente;
						log.IdDocumentoIdentidad = reporteAntiguo.IdDocumentoIdentidad.toString();
						log.IdTipoDocumentoIdentidad = reporteAntiguo.TipoDocumentoIdentidad.toString();
						log.IdTipoRegistro = "MODIFICACION_REPORTE_APV";
						log.NombreProceso = "Modificación de Reporte APV " + reporteAntiguo.IdReporteAPV;
						log.Cantidad = BigDecimal.ZERO;
						log.PrecioTasa = BigDecimal.ZERO;
						log.Monto = BigDecimal.ZERO;
						log.IdRegistro = reporteNuevo.IdReporteAPV.toString();

						entradaAgregarLog.p_Log.add(log);
						entradaAgregarLog.p_LogDetalle.addAll(detallesTemporales);
					}
				}
			}

			if (!entradaAgregarLog.p_Log.isEmpty()) {
				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(con, entradaAgregarLog);
			}

			long fin = System.currentTimeMillis();
			logger.debug("FIN::ModificarReporteAPV.AgregarLog {}", (fin - inicio));
		} catch (Exception e) {
			logger.error("ERROR::ModificarReporteAPV.AgregarLog", e);
		}
	}

	private void actualizarReportesAPV(Connection con, EntradaModificarReporteAPV entrada) throws Exception {
		SQL sql = new SQL();
		logger.debug("INICIO::SQL_UPDATE_REPORTE_APV");
		List<InReporteAPV> entrada_ReportesAPV = entrada.p_ReporteAPV;

		sql.batch(con, SQL_UPDATE_REPORTE_APV, entrada_ReportesAPV, InReporteAPV.class);
		logger.debug("FIN::SQL_UPDATE_REPORTE_APV");
	}

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues()
						.setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

				String json = "{\r\n" + "    \"p_IdEmpresa\": 1,\r\n" + "    \"p_IdUsuario\": 76522,\r\n"
						+ "    \"p_IdModulo\": \"NOR\",\r\n" + "    \"p_ReporteAPV\": [\r\n" + "        {\r\n"
						+ "            \"IdReporteAPV\": 184060,\r\n"
						+ "            \"DocumentoIdentidad\": \"13767078-K\",\r\n"
						+ "            \"NombreCliente\": \"PAULINA ALEJANDRA SAAVEDRA GONZALEZ\",\r\n"
						+ "            \"CodigoRegimenTributario\": \"BF\",\r\n"
						+ "            \"NombreRegimenTributario\": \"APV depósitos voluntarios\",\r\n"
						+ "            \"TipoProducto\": \"Contrato de administración de cartera APV\",\r\n"
						+ "            \"IdPlanTipoInversion\": 2,\r\n"
						+ "            \"NombrePlanTipoInversion\": \"I. ACTIVO\",\r\n"
						+ "            \"FechaCierre\": \"2026-05-06T04:00:00.000Z\",\r\n"
						+ "            \"SaldoRegimenTributarioA\": 5289058,\r\n"
						+ "            \"SaldoRegimenTributarioB\": 22,\r\n"
						+ "            \"SaldoBonificacionEstatal\": 476693,\r\n"
						+ "            \"SaldoAPVCEmpleador\": 0,\r\n" + "            \"SaldoAPVCTrabajadorA\": 0,\r\n"
						+ "            \"SaldoAPVCTrabajadorB\": 0,\r\n"
						+ "            \"SaldoAPVCBonificacionEstatal\": 0,\r\n"
						+ "            \"SaldoAcumulado\": 5765773,\r\n"
						+ "            \"IdUsuarioModificacion\": 76522,\r\n"
						+ "            \"FechaModificacion\": \"2026-05-13\"\r\n" + "        }\r\n" + "    ]\r\n" + "}";

				EntradaModificarReporteAPV objIn = gson.fromJson(json, EntradaModificarReporteAPV.class);
				ModificarReporteAPV servicio = new ModificarReporteAPV();
				SalidaAdministrarBorrador objOut = servicio.logica(con, objIn);

				con.rollback();

				System.out.println(gson.toJson(objOut));
			} catch (Exception er) {
				er.printStackTrace();
				if (con != null)
					con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
