package cl.bcs.facturacion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class GenerarInformesComprobanteComisionesMensuales implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(GenerarInformesComprobanteComisionesMensuales.class);

	public static class EntradaGenerarInformesComprobanteComisionesMensuales extends BcsPojo{
		public Long 	p_IdEmpresa;
		public Long 	p_IdUsuario;
		public String 	p_IdModulo;
		public String 	p_FechaSistema;
		public String 	p_Ruta;
		public Long 	p_IdCliente;
		public String 	p_RutCliente;
		public Long	 	p_IdPortafolio;
		public String	p_CodigoPortafolio;
		public String 	p_TipoInforme;
		public String 	p_IdClasificacionTipoInstrumento;  
		public String 	p_NombreTipoInstrumento;
		public Long 	p_IdSegmentoCliente;
		public String 	p_DescripcionSegmento;
		public String 	p_FechaDesde;
		public String 	p_FechaHasta;
	}
	
	public static class SalidaGenerarInformesComprobanteComisionesMensuales extends BcsPojo{
		public String 		p_out_NombreArchivoLog;
		public String 		p_out_RutaArchivoLog;
		public String 		p_out_NombreArchivo;
		public String 		p_out_RutaArchivo;
		public Long 		p_out_ErroresContador;
		public String 		p_out_Comentario;
		public List<String> p_out_ArchivoSecundario;

	}
			
	public SalidaGenerarInformesComprobanteComisionesMensuales logica(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada) throws Exception {
		
		logger.info("INICIO::GenerarInformesComprobanteComisionesMensuales");
		Long inicioServicio = System.currentTimeMillis();
		
		SalidaGenerarInformesComprobanteComisionesMensuales out = new SalidaGenerarInformesComprobanteComisionesMensuales();
		String fechaSistema = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.FechaSistema");
		
		establecerFechaPorDefecto(entrada, fechaSistema);
	
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date fechaInforme 			= dateFormat.parse(fechaSistema);
		String tipoInforme 			=  (Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_" + entrada.p_TipoInforme).msg()).replace(" ", "");
		String nombreArchivo 		= "InformeCobroComisionesMensuales_" + tipoInforme + "_" + new SimpleDateFormat("ddMMyyyy").format(fechaInforme);
		
		out.p_out_ErroresContador = 0L;
		out.p_out_NombreArchivo = nombreArchivo + ".xls";
		out.p_out_NombreArchivoLog = nombreArchivo + ".log";
		out.p_out_RutaArchivo = entrada.p_Ruta + "/" + out.p_out_NombreArchivo;
		out.p_out_RutaArchivoLog = entrada.p_Ruta + "/" + out.p_out_NombreArchivoLog;
		out.p_out_Comentario =  Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ARCHIVO_GENERADO_EXITOSAMENTE").msg();
		
		try (FileOutputStream outputStream = new FileOutputStream(out.p_out_RutaArchivo)) {
			Workbook archivoExcel = new HSSFWorkbook();
			Sheet hoja = archivoExcel.createSheet();
			
			switch(entrada.p_TipoInforme) {
				case "MZCOM": 
					GenerarInformeMatrizComisiones informeMatrizComisiones = new GenerarInformeMatrizComisiones();
					informeMatrizComisiones.crearInformeMatrizComisiones(con, entrada, archivoExcel, hoja);
					break;
				case "CCRES": 
					GenerarInformeComisionResumido informeComisionResumido = new GenerarInformeComisionResumido();
					informeComisionResumido.crearInformeComisionClienteResumido(con, entrada, archivoExcel, hoja);
					break;
				case "CCDET": 
					GenerarInformeComisionDetallado informeComisionDetallado = new GenerarInformeComisionDetallado();
					informeComisionDetallado.crearInformeComisionClienteDetallado(con, entrada, archivoExcel, hoja);
					break;
			}
			
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
		logger.info("FIN::GenerarInformesCobroComisionesMensuales", finServicio - inicioServicio);
		
		return out;
	}
	
	
	private void establecerFechaPorDefecto( EntradaGenerarInformesComprobanteComisionesMensuales entrada, String fechaSistema) {
		
		if (entrada.p_FechaHasta == null || entrada.p_FechaHasta.equals("")) {
			entrada.p_FechaHasta = fechaSistema;
		}
		
		if (entrada.p_FechaDesde == null || entrada.p_FechaDesde.equals("")) {
			entrada.p_FechaDesde = fechaSistema;
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {

		logger.info("=============== Inicio Llamada GenerarInformesCobroComisionesMensuales ===============");
		Gson gson = Utils.getGsonBuilder();
		EntradaGenerarInformesComprobanteComisionesMensuales in = gson.fromJson(jin.toString(), EntradaGenerarInformesComprobanteComisionesMensuales.class);
		SalidaGenerarInformesComprobanteComisionesMensuales out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		logger.info("=============== Fin Llamada GenerarInformesCobroComisionesMensuales ===============");
		
		return dataout;
	}
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {

			EntradaGenerarInformesComprobanteComisionesMensuales entrada = new EntradaGenerarInformesComprobanteComisionesMensuales();
			entrada.p_IdEmpresa = new Long(1);
			entrada.p_FechaSistema = "2020-06-05";
			entrada.p_FechaDesde = "2020-08-21";
			entrada.p_FechaHasta = "2021-10-21";
			entrada.p_IdClasificacionTipoInstrumento = "";
			entrada.p_IdCliente = new Long(0);
			entrada.p_IdPortafolio = new Long(0);
			entrada.p_IdSegmentoCliente = new Long(0);
			entrada.p_TipoInforme = "CCRES";
			/*
			{ Codigo: "CCDET", Nombre: $translate.instant('UI_CC_DETALLADO') },
						{ Codigo: "CCRES", Nombre: $translate.instant('UI_CC_RESUMIDO') },
						{ Codigo: "MZCOM", Nombre: $translate.instant('UI_MZ_COMISIONES') },
					  ]
			*/
			
			entrada.p_Ruta = "C:\\BSC";
			//entrada.p_FechaInforme = "2020-06-05";//"2018-12-01"; //"2020-08-20";//"2018-12-01"; // "2020-08-20"; // "2019-06-18"
			JsonParser parser = new JsonParser();
			JsonObject dataJsonIn = parser.parse(Utils.getGsonBuilder().toJson(entrada)).getAsJsonObject();
			GenerarInformesComprobanteComisionesMensuales demo = new GenerarInformesComprobanteComisionesMensuales();
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
