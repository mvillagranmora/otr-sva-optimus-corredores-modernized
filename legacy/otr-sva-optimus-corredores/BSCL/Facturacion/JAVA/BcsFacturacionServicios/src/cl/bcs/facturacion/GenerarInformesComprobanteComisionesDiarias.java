package cl.bcs.facturacion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.facturacion.pojo.ReporteMatrizComisiones;
import cl.bcs.nuam.StoredProcedureException;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInformesComprobanteComisionesDiarias implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(GenerarInformesComprobanteComisionesMensuales.class);

	public static class EntradaGenerarInformesComprobanteComisionesDiarias extends BcsPojo{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_Ruta;
		public Long 	p_IdCliente;
		public Long	 	p_IdPortafolio;
		public String 	p_Fecha;
	}
	
	public static class SalidaGenerarInformesComprobanteComisionesDiarias extends BcsPojo{
		public String 		p_out_NombreArchivoLog;
		public String 		p_out_RutaArchivoLog;
		public String 		p_out_NombreArchivo;
		public String 		p_out_RutaArchivo;
		public Long 		p_out_ErroresContador;
		public String 		p_out_Comentario;
	}
			
	public SalidaGenerarInformesComprobanteComisionesDiarias logica(Connection con, EntradaGenerarInformesComprobanteComisionesDiarias entrada) throws Exception {
		
		logger.info("INICIO::GenerarInformesCobroComisionesDiariasRV");
		Long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		SalidaGenerarInformesComprobanteComisionesDiarias out = new SalidaGenerarInformesComprobanteComisionesDiarias();
		String fechaSistema = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.FechaSistema");
		
		if (entrada.p_Fecha == null || entrada.p_Fecha.equals("")) {
			entrada.p_Fecha = fechaSistema;
		}
		LocalTime horaActual = LocalTime.now();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hhmmss");
		Date fechaInforme 			= dateFormat.parse(entrada.p_Fecha);
		String nombreArchivo 		= "InformeCobroComisionesDiarias_" + new SimpleDateFormat("ddMMyyyy").format(fechaInforme) + "_" + formatter.format(horaActual);
		
		out.p_out_ErroresContador = 0L;
		out.p_out_NombreArchivo = nombreArchivo + ".xls";
		out.p_out_NombreArchivoLog = nombreArchivo + ".log";
		out.p_out_RutaArchivo = entrada.p_Ruta  + out.p_out_NombreArchivo;
		out.p_out_RutaArchivoLog = entrada.p_Ruta + out.p_out_NombreArchivoLog;

		out.p_out_Comentario =  Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ARCHIVO_GENERADO_EXITOSAMENTE").msg();
		
		entrada.p_Ruta = out.p_out_RutaArchivo;
		
		try (FileOutputStream outputStream = new FileOutputStream(out.p_out_RutaArchivo)) {
			Workbook archivoExcel = new HSSFWorkbook();
			
			GenerarInformeMatrizComisionesDiarias generarInformeMatrizComisionesDiariasRV = new GenerarInformeMatrizComisionesDiarias();
			generarInformeMatrizComisionesDiariasRV.crearInformeMatrizComisiones(con, entrada, archivoExcel);	
			
			archivoExcel.write(outputStream);
			logger.debug(archivoExcel);
			
		} catch (Exception e) {
			out.p_out_ErroresContador++;
			out.p_out_Comentario = e.getMessage();
		}
		
		File fileRF = new File(out.p_out_RutaArchivoLog);
		try (BufferedWriter bwAchivoLog = new BufferedWriter(new FileWriter(fileRF));) {
			bwAchivoLog.write(out.p_out_Comentario);
			bwAchivoLog.newLine();
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformesCobroComisionesDiariasRV", finServicio - inicioServicio);
		
		return out;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {

		logger.info("=============== Inicio Llamada GenerarInformesCobroComisionesDiariasRV ===============");
		Gson gson = Utils.getGsonBuilder();
		EntradaGenerarInformesComprobanteComisionesDiarias in = gson.fromJson(jin.toString(), EntradaGenerarInformesComprobanteComisionesDiarias.class);
		SalidaGenerarInformesComprobanteComisionesDiarias out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		logger.info("=============== Fin Llamada GenerarInformesCobroComisionesDiariasRV ===============");
		
		return dataout;
	}
	
	public static void main(String[] args) throws StoredProcedureException {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {

			EntradaGenerarInformesComprobanteComisionesDiarias entrada = new EntradaGenerarInformesComprobanteComisionesDiarias();
			entrada.p_IdEmpresa = 1L;
			entrada.p_Fecha = "2026-08-24";
			entrada.p_IdCliente = 6953L;
			entrada.p_IdPortafolio = 2101L;

			entrada.p_Ruta = System.getProperty("java.io.tmpdir");
			//entrada.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaBaseArchivos");;

			JsonParser parser = new JsonParser();
			JsonObject dataJsonIn = parser.parse(Utils.getGsonBuilder().toJson(entrada)).getAsJsonObject();
			GenerarInformesComprobanteComisionesDiarias demo = new GenerarInformesComprobanteComisionesDiarias();
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
