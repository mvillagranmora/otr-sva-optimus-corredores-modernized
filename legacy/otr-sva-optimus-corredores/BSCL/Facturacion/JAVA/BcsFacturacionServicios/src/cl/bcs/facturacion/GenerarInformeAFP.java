package cl.bcs.facturacion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.afp.FactoryAFP;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * 
 * @author Carlos Herrera
 *
 */

public class GenerarInformeAFP implements JavaCall {
	static final Logger logger = LogManager.getLogger(GenerarInformeAFP.class);
	
	private final String SQL_CONSULTAR_EXTENCION = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformeAFP_ExtencionAFP.sql");

	public static class EntradaGenerarInformeAFP {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Fecha;
		public String p_Ruta;
		public Boolean p_Interfaz = true;
		public Boolean p_EsTodos = false;
		public Long IdAFP;
		public String FechaAFP;
		public String NombreAFP;
		public String ParametroJPM;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaGenerarInformeAFP {
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
	
	public static class ExtencionAFP{
		public String ExtensionInterfazAFP;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {

		logger.info("=============== Inicio Llamada GenerarInformeAFP ===============");
		Gson gson = Utils.getGsonBuilder();
		EntradaGenerarInformeAFP in = gson.fromJson(jin.toString(), EntradaGenerarInformeAFP.class);
		SalidaGenerarInformeAFP out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		logger.info("=============== Fin Llamada GenerarInformeAFP ===============");
		return dataout;
		
	}

	public SalidaGenerarInformeAFP logica(Connection con, EntradaGenerarInformeAFP entrada) throws Exception {
		logger.info("=============== Inicio GenerarInformeAFP ===============");
		SalidaGenerarInformeAFP out = new SalidaGenerarInformeAFP();
		SimpleDateFormat formatoFechaArchivo = new SimpleDateFormat("yyyyMMdd");
		Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(entrada.p_Fecha);
		String fechayyyymmdd = formatoFechaArchivo.format(fecha);

		try {

			SQL sql = new SQL();
			HashMap<String, Object> parametrosConsulta = new HashMap<>();
			parametrosConsulta.put("IdAFP", entrada.IdAFP);
			List<ExtencionAFP> extencionAFP = sql.query(con, SQL_CONSULTAR_EXTENCION,parametrosConsulta, ExtencionAFP.class);
			if(extencionAFP.get(0).ExtensionInterfazAFP == null) {
				logger.debug("Extencion de archivo no configurada");
				out.p_out_Comentario = "Extencion de archivo no configurada";
				out.p_out_ErroresContador = 1L;
				return out;
			}

			FactoryAFP factoryAFP = new FactoryAFP();
			if(factoryAFP.getDAO(entrada.NombreAFP) == null) {
				logger.debug("Interfaz no configurada");
				out.p_out_Comentario = "Interfaz no configurada";
				out.p_out_ErroresContador = 1L;
				return out;
			}
			
			String archivoTXT = factoryAFP.getDAO(entrada.NombreAFP).obtenerData(entrada.IdAFP, entrada.FechaAFP,entrada.ParametroJPM, con);
			String extencion = "."+extencionAFP.get(0).ExtensionInterfazAFP.toString();
			logger.debug("Extencion archivo: \"" + extencion + "\"");
			
			out.p_out_NombreArchivo = fechayyyymmdd + "_ARCHIVO_AFP_" + entrada.NombreAFP + extencion;
			out.p_out_RutaArchivo = entrada.p_Ruta + "/" + out.p_out_NombreArchivo;
			out.p_out_NombreArchivoLog = fechayyyymmdd + "_ARCHIVO_AFP_" + entrada.NombreAFP + ".log";

			File fileAFP = new File(out.p_out_RutaArchivo);
			try (BufferedWriter archivoAFP = new BufferedWriter(new FileWriter(fileAFP));) {
				archivoAFP.write(archivoTXT);
				archivoAFP.newLine();
			}
			
			out.p_out_Comentario = "Archivo generado exitosamente";
			out.p_out_ErroresContador = 0L;

		} catch (Exception e) {
			logger.debug(e);
			out.p_out_Comentario = "Archivo no pudo ser Generado";
			out.p_out_ErroresContador = 1L;
		}
		logger.info("=============== Fin GenerarInformeAFP ===============");
		return out;
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {

			EntradaGenerarInformeAFP entrada = new EntradaGenerarInformeAFP();
			entrada.p_IdEmpresa = new Long(1);
			entrada.p_Fecha = "2019-05-22";
			entrada.p_Ruta = "C:\\BCS\\logs";
			entrada.IdAFP = Long.parseLong("3");
			entrada.NombreAFP = "HABITAT";
			entrada.FechaAFP = "2016-04-26";
			entrada.ParametroJPM = "S";
			JsonParser parser = new JsonParser();
			JsonObject dataJsonIn = parser.parse(Utils.getGsonBuilder().toJson(entrada)).getAsJsonObject();
			GenerarInformeAFP demo = new GenerarInformeAFP();
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