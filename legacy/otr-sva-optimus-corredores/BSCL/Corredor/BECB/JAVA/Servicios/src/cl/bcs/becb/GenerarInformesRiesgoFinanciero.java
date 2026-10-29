package cl.bcs.becb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
//import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.becb.riesgo.FactoryRiesgoFinanciero;
//import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
//import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * 
 * @author Carlos Herrera
 *
 */

public class GenerarInformesRiesgoFinanciero implements JavaCall {

	private final static Logger logger = LogManager.getLogger(GenerarInformesRiesgoFinanciero.class);

	private final String SQL_GET_ID_CLIENTE = loadResourceToString(
			"cl/bcs/becb/sql/FactoryRiesgoFinanciero_GetIdCliente.sql");
	private final String SQL_GET_VALOR_UF = loadResourceToString(
			"cl/bcs/becb/sql/FactoryRiesgoFinanciero_GetValorUF.sql");

	public static class SalidaIdCliente {
		public Long IdCliente;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaValorUF {
		public BigDecimal Valor;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class EntradaGenerarInforme {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Fecha;
		public String p_Ruta;
		public Boolean p_Interfaz = true;
		public Boolean p_EsTodos = false;
		public String codigoInforme;
		public String fechaInforme;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaGenerarInforme {
		public String p_out_NombreArchivoLog;
		public String p_out_RutaArchivoLog;
		public String p_out_NombreArchivo;
		public String p_out_RutaArchivo;
		public Long p_out_ErroresContador;
		public String p_out_Comentario;
		public List<String> p_out_ArchivoSecundario;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {

		logger.info("=============== Inicio Llamada GenerarInformesRiesgoFinanciero ===============");
		Gson gson = Utils.getGsonBuilder();
		EntradaGenerarInforme in = gson.fromJson(jin.toString(), EntradaGenerarInforme.class);
		SalidaGenerarInforme out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		logger.info("=============== Fin Llamada GenerarInformesRiesgoFinanciero ===============");
		return dataout;

	}

	public SalidaGenerarInforme logica(Connection con, EntradaGenerarInforme entrada) throws Exception {
		logger.info("=============== Inicio GenerarInformesRiesgoFinanciero ===============");
		SalidaGenerarInforme out = new SalidaGenerarInforme();
		String tabla;
		SimpleDateFormat formatoFechaArchivo = new SimpleDateFormat("yyyyMMdd");
		Date fechaSistema = new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_Fecha);
		Date fechaConsulta = new SimpleDateFormat("yyyy-MM-dd").parse(entrada.fechaInforme);
		if (fechaSistema.compareTo(fechaConsulta) == 0) {
			tabla = "CUS";
		} else {
			tabla = "HIS";
		}

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("_HHmmss");
		LocalDateTime now = LocalDateTime.now();
		String time = dtf.format(now);
		String mensajeGeneracionArchivo = "Archivo generado exitosamente";

		try {

			SQL sql = new SQL();
			HashMap<String, Object> vacio = new HashMap<>();
			List<SalidaIdCliente> cliente = sql.query(con, SQL_GET_ID_CLIENTE, vacio, SalidaIdCliente.class);
			HashMap<String, Object> consultarUF = new HashMap<>();
			consultarUF.put("FechaSistema", entrada.p_Fecha);
			List<SalidaValorUF> valorUF = sql.query(con, SQL_GET_VALOR_UF, consultarUF, SalidaValorUF.class);

			String fechayyyymmdd = formatoFechaArchivo.format(fechaSistema) + time;

			FactoryRiesgoFinanciero factoryRF = new FactoryRiesgoFinanciero();
			if (factoryRF.getDAO(entrada.codigoInforme) == null) {
				logger.debug("Interfaz no configurada");
				out.p_out_Comentario = "Interfaz no configurada";
				out.p_out_ErroresContador = 1L;
				return out;
			}

			String nombreArchivo = "Informe_" + entrada.codigoInforme + "_" + tabla + "_";

			out.p_out_NombreArchivo = nombreArchivo + fechayyyymmdd + ".xls";
			out.p_out_NombreArchivoLog = nombreArchivo + fechayyyymmdd + ".log";
			out.p_out_RutaArchivo = entrada.p_Ruta + "/" + out.p_out_NombreArchivo;
			out.p_out_RutaArchivoLog = entrada.p_Ruta + "/" + out.p_out_NombreArchivoLog;
			out.p_out_Comentario = mensajeGeneracionArchivo;
			out.p_out_ErroresContador = 0L;

			if (!factoryRF.getDAO(entrada.codigoInforme).obtenerData(entrada.codigoInforme, entrada.fechaInforme,
					cliente.get(0).IdCliente, valorUF.get(0).Valor, out.p_out_RutaArchivo, tabla, con)) {
				logger.debug("Archivo no tiene data");
				out.p_out_Comentario = "Archivo no tiene data";
				out.p_out_ErroresContador = 1L;
				return out;
			}

			File fileRF = new File(out.p_out_RutaArchivoLog);
			try (BufferedWriter bwAchivoLog = new BufferedWriter(new FileWriter(fileRF));) {
				bwAchivoLog.write(out.p_out_Comentario);
				bwAchivoLog.newLine();
			}

		} catch (Exception e) {
			logger.debug(e);
			out.p_out_Comentario = "Archivo no pudo ser Generado";
			out.p_out_ErroresContador = 1L;
		}
		logger.info("=============== Fin GenerarInformesRiesgoFinanciero ===============");
		return out;

	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getUAT2Connection()) {

			EntradaGenerarInforme entrada = new EntradaGenerarInforme();
			entrada.p_IdEmpresa = new Long(1);
			entrada.p_Fecha = "2020-06-03";
			entrada.p_Ruta = "C:\\BCS";
			entrada.codigoInforme = "DIFI";//"ALL";
			entrada.fechaInforme = "2020-06-03";
			JsonParser parser = new JsonParser();
			JsonObject dataJsonIn = parser.parse(Utils.getGsonBuilder().toJson(entrada)).getAsJsonObject();
			GenerarInformesRiesgoFinanciero demo = new GenerarInformesRiesgoFinanciero();
			try {
				System.out.println(demo.call(con, dataJsonIn).toString());
				con.commit();
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
