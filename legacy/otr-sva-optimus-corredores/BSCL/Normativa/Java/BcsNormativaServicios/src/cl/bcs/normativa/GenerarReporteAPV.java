package cl.bcs.normativa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.tesoreria.CrearIngresoEgreso.InformacionCorredora;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarReporteAPV implements JavaCall {

	private final static Logger logger = LogManager.getLogger(GenerarReporteAPV.class);

	private static final Integer LARGO_LINEA = 116;

	private final String SQL_SELECT_REPORTE_APV = loadResourceToString("cl/bcs/normativa/sql/GenerarReporteAPV_SelectReporteAPV.sql");
	private final String SQL_SELECT_CORREDORA = loadResourceToString("cl/bcs/tesoreria/sql/CrearIngresoEgreso_SelectCorredora.sql");

	public static class EntradaGenerarReporteAPV {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Ruta;
		public String p_FechaCierre;
		public String p_FormatoArchivo;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaGenerarReporteAPV {
		public String P_OUT_NOMBRE_ARCHIVO_LOG;
		public String P_OUT_RUTA_ARCHIVO_LOG;
		public String P_OUT_NOMBRE_ARCHIVO;
		public String P_OUT_RUTA_ARCHIVO;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;

		public SalidaGenerarReporteAPV() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ConsultarReporteAPV {
		public Long IdReporteAPV;
		public String DocumentoIdentidad;
		public String NombreCliente;
		public String CodigoRegimenTributario;
		public String NombreRegimenTributario;
		public String TipoProducto;
		public Long IdPlanTipoInversion;
		public String NombrePlanTipoInversion;
		public Date FechaCierre;
		public BigDecimal SaldoAcumulado;
		public BigDecimal SaldoRegimenTributarioA;
		public BigDecimal SaldoRegimenTributarioB;
		public BigDecimal SaldoBonificacionEstatal;
		public BigDecimal SaldoAPVCEmpleador;
		public BigDecimal SaldoAPVCTrabajadorA;
		public BigDecimal SaldoAPVCTrabajadorB;
		public BigDecimal SaldoAPVCBonificacionEstatal;
	}

	public static class LogEjecucion {
		public Long IdInterfaz;
		public Long NumeroRegistros;
		public Long NumeroRegistrosCorrectos;
		public Long NumeroRegistrosIncorrectos;
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaGenerarReporteAPV in = gson.fromJson(jin.toString(), EntradaGenerarReporteAPV.class);
		SalidaGenerarReporteAPV out = logica(con, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	}

	public SalidaGenerarReporteAPV logica(Connection con, EntradaGenerarReporteAPV entrada) throws Exception {
		logger.info("INICIO::GenerarReporteAPV.logica");

	    SalidaGenerarReporteAPV salida = new SalidaGenerarReporteAPV();
	
	    try {
	        long startTime = System.currentTimeMillis();
	        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
	        String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
	
	        SQL sql = new SQL();
	
			HashMap<String, Object> params = new HashMap<>();
			params.put("FechaCierre", entrada.p_FechaCierre);
	
			logger.debug("INICIO::SQL_SELECT_REPORTE_APV");
			List<ConsultarReporteAPV> reportes = sql.query(con, SQL_SELECT_REPORTE_APV, params, ConsultarReporteAPV.class);
			logger.debug("FIN::SQL_SELECT_REPORTE_APV");
	
			logger.debug("INICIO::SQL_SELECT_DATOS_CORREDOR");
			List<InformacionCorredora> informacionCorredora = sql.query(con, SQL_SELECT_CORREDORA, new HashMap<String, Object>(), InformacionCorredora.class);
			logger.debug("FIN::SQL_SELECT_DATOS_CORREDOR");
	
			String documento = informacionCorredora.get(0).DocumentoIdentidad;
			String rut = documento.split("-")[0];
			String dv = documento.split("-")[1].toUpperCase();
	
			String nombreArchivo = "bolsa_apvdc" + entrada.p_FechaCierre.replace("-", "") + "." + rut + dv;
	
			salida.P_OUT_NOMBRE_ARCHIVO = "nor/" + nombreArchivo;
			salida.P_OUT_RUTA_ARCHIVO = "/" + salida.P_OUT_NOMBRE_ARCHIVO;
			salida.P_OUT_NOMBRE_ARCHIVO_LOG = nombreArchivo + ".log";
			salida.P_OUT_RUTA_ARCHIVO_LOG = salida.P_OUT_NOMBRE_ARCHIVO_LOG;
	
			String rutaCompletaArchivo = entrada.p_Ruta + salida.P_OUT_RUTA_ARCHIVO;
			String rutaCompletaArchivoLog = entrada.p_Ruta + salida.P_OUT_RUTA_ARCHIVO_LOG;
	
	        generarArchivo(con, idMonedaSistema, rutaCompletaArchivo, rutaCompletaArchivoLog, reportes, entrada.p_FormatoArchivo, startTime);
	
	        agregarLog(con, entrada, "ARCHIVO_GENERADO_EXITOSAMENTE", fechaSistema, idMonedaSistema);
	
	        salida.P_OUT_MENSAJES.add(new MensajeSalida( 0L, MensajeSalida.MENSAJE_OK, "UI_REPORTE_APV_GENERADO_EXITOSAMENTE", new HashMap<>(), con, entrada.p_IdEmpresa));
	        salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
	
	        logger.info("FIN::GenerarReporteAPV.logica");
	        return salida;
	    } catch (Exception e) {
	        logger.error("ERROR::GenerarReporteAPV.logica", e);
	        salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_GENERAR_REPORTE_APV", e.getMessage()));
	        salida.P_OUT_ERRORESCONTADOR = 1L;
	        throw e;
	    }
	}


	public boolean generarArchivo(Connection con, String idMonedaSistema, String rutaArchivo, String rutaArchivoLog, List<ConsultarReporteAPV> reportes, String formatoArchivo, long startTime) throws Exception {
		logger.trace("INICIO::GenerarReporteAPV.generarArchivo");

		int numeroRegistros = 0;
		File archivo = new File(rutaArchivo);
		File archivoLog = new File(rutaArchivoLog);
		
		SimpleDateFormat fechaFormato = new SimpleDateFormat("yyyyMMdd");

		try (BufferedWriter BwArchivo = new BufferedWriter(new FileWriter(archivo));
				BufferedWriter BwArchivoLog = new BufferedWriter(new FileWriter(archivoLog))) {
			if (reportes.size() > 0) {
				for (ConsultarReporteAPV reporte : reportes) {
					String reporteDocumento = reporte.DocumentoIdentidad;
					String reporteNumero = reporteDocumento.split("-")[0];
					String reporteDigitoVerificador = reporteDocumento.split("-")[1].toUpperCase();
	
					String linea = "";
	
					linea += formatNum(new BigDecimal(reporteNumero), 8);
					linea += formatString(reporteDigitoVerificador, 1);
					linea += formatNum(new BigDecimal(reporte.CodigoRegimenTributario), 1);
					linea += formatNum(new BigDecimal(reporte.TipoProducto), 1);
					linea += formatNum(new BigDecimal(reporte.NombrePlanTipoInversion), 11);
					linea += formatNum(MonedaFormato.formatear(con, reporte.SaldoAcumulado, MonedaFormato.MONTO, idMonedaSistema).valor, 11);
					linea += formatNum(MonedaFormato.formatear(con, reporte.SaldoRegimenTributarioA, MonedaFormato.MONTO, idMonedaSistema).valor, 11);
					linea += formatNum(MonedaFormato.formatear(con, reporte.SaldoRegimenTributarioB, MonedaFormato.MONTO, idMonedaSistema).valor, 11);
					linea += formatNum(MonedaFormato.formatear(con, reporte.SaldoBonificacionEstatal, MonedaFormato.MONTO, idMonedaSistema).valor, 10);
					linea += formatNum(MonedaFormato.formatear(con, reporte.SaldoAPVCEmpleador, MonedaFormato.MONTO, idMonedaSistema).valor, 11);
					linea += formatNum(MonedaFormato.formatear(con, reporte.SaldoAPVCTrabajadorA, MonedaFormato.MONTO, idMonedaSistema).valor, 11);
					linea += formatNum(MonedaFormato.formatear(con, reporte.SaldoAPVCTrabajadorB, MonedaFormato.MONTO, idMonedaSistema).valor, 11);
					linea += formatNum(MonedaFormato.formatear(con, reporte.SaldoAPVCBonificacionEstatal, MonedaFormato.MONTO, idMonedaSistema).valor, 10);
					linea += fechaFormato.format(reporte.FechaCierre);
					if (linea.length() > LARGO_LINEA) {
						throw new Error("Largo no válido");
					}
	
					numeroRegistros++;
					BwArchivo.write(linea);
					BwArchivo.newLine();
				}
			}
	
			BwArchivo.close();
	
			SimpleDateFormat fm = new SimpleDateFormat("ddMMyyyyHHmm");
			String fechaInicio = "Fecha Hora Inicio:" + fm.format(new Date(startTime));
			String fechaTermino = "Fecha Hora Termino:" + fm.format(new Date());
			String tiempoEjecucion = "Tiempo de ejecución:" + (System.currentTimeMillis() - startTime);
	
			BwArchivoLog.write(fechaInicio);
			BwArchivoLog.newLine();
			BwArchivoLog.write(fechaTermino);
			BwArchivoLog.newLine();
			BwArchivoLog.write(tiempoEjecucion);
			BwArchivoLog.newLine();
			BwArchivoLog.write(String.valueOf(numeroRegistros));
			BwArchivoLog.newLine();
			BwArchivoLog.write("Ruta Archivo Salida:" + rutaArchivo);
	
			BwArchivoLog.close();
	
			logger.info("FIN::GenerarReporteAPV.logica");
			return true;
		} catch (Exception e) {
			logger.error("ERROR::GenerarReporteAPV.logica", e);
			throw e;
		}
	}

	private String formatString(String valor, int largo) {
		if (valor == null)
			valor = "";

		valor = valor.toUpperCase().replace("Á", "A").replace("É", "E").replace("Í", "I").replace("Ó", "O")
				.replace("Ú", "U").replace("Ñ", "N");

		valor = valor.replaceAll("[^A-Z0-9 .,'-]", "");

		if (valor.length() > largo) {
			valor = valor.substring(0, largo);
		}

		return String.format("%-" + largo + "s", valor);
	}

	private String formatNum(BigDecimal valor, int largo) {
		if (valor == null)
			valor = BigDecimal.ZERO;

		String limpio = valor.setScale(0, BigDecimal.ROUND_DOWN).toPlainString();

		return String.format("%" + largo + "s", limpio).replace(' ', '0');
	}
	
	private void agregarLog(Connection con, EntradaGenerarReporteAPV entrada, String mensajeGeneracionArchivo, String fechaSistema, String idMonedaSistema) throws Exception {
		logger.info("INICIO::GenerarReporteAPV.agregarLog");
		try {
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			
			entradaAgregarLog.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaAgregarLog.p_IdModulo = "PLA";
			entradaAgregarLog.p_IdUsuario = entrada.p_IdUsuario;
			entradaAgregarLog.p_UsaReglasNegocio = "N";
			
			Long IdCabeceraLog = entrada.p_IdUsuario;

			log.IdCabeceraLog = IdCabeceraLog;
			log.IdTipoLog = "INFORMATIVO";
			log.IdModulo = "NOR";
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = 0L;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "GENERAR_REPORTE_APV";
			log.EstadoRegistro = "";
			log.IdTipoOperacion = "";
			log.Cantidad = BigDecimal.ZERO;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = BigDecimal.ZERO;
			log.IdMonedaOrigen = idMonedaSistema;
			log.IdProceso = 0L;
			log.EstadoProceso = mensajeGeneracionArchivo != "" ? "PRO" : "ERR";
			log.NombreProceso = "Generar Reporte APV a Fecha Cierre: " + entrada.p_FechaCierre;
			
			entradaAgregarLog.p_Log.add(log);

			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(con, entradaAgregarLog);
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("FIN::GenerarReporteAPV.agregarLog");
	}

	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa01", "connections.properties")) {
			System.out.println("============INICIO============");
			Gson gson = Utils.getGsonBuilder();

			String json = "{\n"
		       + "  \"p_IdEmpresa\": 1,\n"
		       + "  \"p_IdUsuario\": 9784,\n"
		       + "  \"p_IdModulo\": \"NOR\",\n"
		       + "  \"p_Ruta\": \"C:\\\\ARCHIVO_PRUEBA\\\\\",\n"
		       + "  \"p_FechaCierre\": \"2026-02-27\",\n"
		       + "  \"p_FormatoArchivo\": \"TXT\"\n"
		       + "}";

			EntradaGenerarReporteAPV Objin = gson.fromJson(json, EntradaGenerarReporteAPV.class);
			GenerarReporteAPV demo = new GenerarReporteAPV();
			try {
				SalidaGenerarReporteAPV ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.P_OUT_MENSAJES);

				con.rollback();

				System.out.println("============FIN============");
			} catch (Exception er) {
				er.printStackTrace();
				logger.error(er.getMessage());
				con.rollback();
			}
		}
	}
}
