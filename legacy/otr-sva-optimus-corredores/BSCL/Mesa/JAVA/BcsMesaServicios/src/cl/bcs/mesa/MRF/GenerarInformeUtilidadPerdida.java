package cl.bcs.mesa.MRF;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInformeUtilidadPerdida  implements JavaCall{

private final String SQL_SELECT_INFORMACION_OPERACIONES = loadResourceToString("cl/bcs/mesa/MRF/sql/GenerarInformeUtilidadPerdida_ConsultarInformeOperacionesMRF.sql");
	
	private final static Logger logger = LogManager.getLogger(GenerarInformeUtilidadPerdida.class);


	
	public static class GenerarInformeUtilidadPerdida_Entrada{
		//Parametros Fijos
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String	p_Ruta;
		
		//Parametros de ventana		
		public String 	p_FechaDesde;
		public String 	p_FechaHasta;		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class GenerarInformeUtilidaPerdida_Salida{
		public List<String> 		P_OUT_ARCHIVOS;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long                 P_OUT_ERRORESCONTADOR;

		public String 				P_OUT_NOMBRE_ARCHIVO_LOG;
		public String 	 			P_OUT_RUTA_ARCHIVO_LOG;
		public String				P_OUT_NOMBRE_ARCHIVO;
		public String				P_OUT_RUTA_ARCHIVO; 
		public String 				P_OUT_COMENTARIO;
		
		public GenerarInformeUtilidaPerdida_Salida() {
			P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DetalleOperacion{
				
		//DATOS OPERACIONES
		public String MesOperacion;
		public Long NumeroOperacion;
		public String NombreCliente;
		public String RutCLiente;
		public String SecRutCLiente;
		public BigDecimal MontoInicial;
		public BigDecimal Tasa;
		public Long Plazo;
		public BigDecimal TotalInteres;
		public BigDecimal MontoFinal;
		public Date FechaInicio;
		public Date FechaFinal;
		public Date FechaLiquidacion;
		public BigDecimal CostoFondo;
		public BigDecimal CostoFondoCDB;
		public BigDecimal Utilidad;
		public String Moneda;
		public BigDecimal MontoMonedaOrigen;
		public BigDecimal UtilidadPerdida;
		public BigDecimal UtilidadFinal;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public GenerarInformeUtilidaPerdida_Salida logica(Connection conn, GenerarInformeUtilidadPerdida_Entrada entrada) throws Exception{
		logger.info("Inicio de servicio GenerarInformeEstadistica");
		String mensajeGeneracionArchivo = "ARCHIVO_GENERADO_EXITOSAMENTE";
		GenerarInformeUtilidaPerdida_Salida salida = new GenerarInformeUtilidaPerdida_Salida();
		try {
		SQL _sql = new SQL();
				
		HashMap<String,Object> params =  new HashMap<>();
		params.put("FechaDesde", entrada.p_FechaDesde);
		params.put("FechaHasta", entrada.p_FechaHasta);
		
		logger.debug("INICIO - Select Obtener informacion operaciones");
		List<DetalleOperacion> operaciones = _sql.query(conn, SQL_SELECT_INFORMACION_OPERACIONES,params, DetalleOperacion.class);
		logger.debug("FIN - Select Obtener informacion operaciones"); 
		
		
		// Configuracion para salida de archivo 
		String fechaSistema = Utils.getFechaHoraSistema(conn, "BSCL", entrada.p_IdEmpresa);
		Date fechaActual = Fecha.toDate(fechaSistema);
		Date pFecha = new SimpleDateFormat("yyyy-MM-dd").parse(fechaActual.toString());	
		SimpleDateFormat sdfyyyyMMdd = new SimpleDateFormat("yyyyMMdd"); 
		String fechayyyymmdd = sdfyyyyMMdd.format(pFecha);		
		
		String nombreArchivo = "InformeUtilidadPerdida";
				
		salida.P_OUT_NOMBRE_ARCHIVO = nombreArchivo + fechayyyymmdd + ".xls";
		salida.P_OUT_NOMBRE_ARCHIVO_LOG = nombreArchivo + fechayyyymmdd + "DET.log";		
		salida.P_OUT_RUTA_ARCHIVO = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO;
		salida.P_OUT_RUTA_ARCHIVO_LOG = entrada.p_Ruta + "/" + salida.P_OUT_NOMBRE_ARCHIVO_LOG;
		
		if (!generarArchivoInformeUtilidadPerdida(entrada.p_Ruta, salida.P_OUT_RUTA_ARCHIVO, operaciones,  Fecha.toDate(entrada.p_FechaDesde.substring(0,10)), Fecha.toDate(entrada.p_FechaHasta.substring(0,10)))) {
			mensajeGeneracionArchivo = "ERROR_ARCHIVO_GENERADO";
		}
		
		salida.P_OUT_COMENTARIO =  Diccionario.getMensaje(conn, entrada.p_IdEmpresa, mensajeGeneracionArchivo).msg();
		File archivoLog = new File(salida.P_OUT_RUTA_ARCHIVO_LOG);
		try(BufferedWriter bwAchivoLog = new BufferedWriter(new FileWriter(archivoLog))){
			bwAchivoLog.write(salida.P_OUT_COMENTARIO);
			bwAchivoLog.newLine();
		}
		
		logger.info("Fin de servicio ConsultarOperciones");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return salida;
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		GenerarInformeUtilidadPerdida_Entrada in = gson.fromJson(jin.toString(), GenerarInformeUtilidadPerdida_Entrada.class);
		GenerarInformeUtilidaPerdida_Salida out = logica(conn, in);
		out.P_OUT_ERRORESCONTADOR = 0L;
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;	
	}
	
	public String getNombreMes(String numMes) {
		String Mes="";
		if(numMes.equals("1")) {
			Mes="ENERO";
		}else if(numMes.equals("2")) {
			Mes="FEBRERO";
		}else if(numMes.equals("3")) {
			Mes="MARZO";
		}else if(numMes.equals("4")) {
			Mes="ABRIL";
		}else if(numMes.equals("5")) {
			Mes="MAYO";
		}else if(numMes.equals("6")) {
			Mes="JUNIO";
		}else if(numMes.equals("7")) {
			Mes="JULIO";
		}else if(numMes.equals("8")) {
			Mes="AGOSTO";
		}else if(numMes.equals("9")) {
			Mes="SEPTIEMBRE";
		}else if(numMes.equals("10")) {
			Mes="OCTUBRE";
		}else if(numMes.equals("11")) {
			Mes="NOVIEMBRE";
		}else if(numMes.equals("12")) {
			Mes="DICIEMBRE";
		}		
		return Mes;
	}
	
	
	public BigDecimal calculoUtilidadPerdida(DetalleOperacion ope,Date fechaDesde, Date fechaHasta) {
		BigDecimal out= BigDecimal.ZERO;	
		if(ope.UtilidadPerdida!= null) {
			out= ope.UtilidadPerdida;	
		}else {
			out = BigDecimal.ZERO;
		}

		return out;
	}
	
	public boolean generarArchivoInformeUtilidadPerdida(String rutaArchivo, String pOutNombreArchivo, List<DetalleOperacion> ordenes,  Date fechaDesde, Date fechaHasta) 
			throws FileNotFoundException, IOException, EncryptedDocumentException, InvalidFormatException {
		try {
					    
		    try(FileOutputStream outputStream = new FileOutputStream(pOutNombreArchivo)){
		    		
    			Workbook archivoExcel = new HSSFWorkbook();
    			Sheet hoja = archivoExcel.createSheet();
    			    			
    			/************************************/
    			/**			Formatos			   **/
    			/************************************/
    			hoja.setColumnWidth(0, 5000);
    			hoja.setColumnWidth(1, 4000);
    			hoja.setColumnWidth(2, 8000);
    			hoja.setColumnWidth(3, 4000);
    			hoja.setColumnWidth(4, 4000);
    			hoja.setColumnWidth(5, 6500);
    			hoja.setColumnWidth(6, 2000);
    			hoja.setColumnWidth(7, 3000);
    			hoja.setColumnWidth(8, 5000);
    			hoja.setColumnWidth(9, 6000);
    			hoja.setColumnWidth(10, 4500);
    			hoja.setColumnWidth(11, 4500);
    			hoja.setColumnWidth(12, 4500);
    			hoja.setColumnWidth(13, 4500);
    			hoja.setColumnWidth(14, 4500);
    			hoja.setColumnWidth(15, 200);
    			hoja.setColumnWidth(16, 3000);
    			hoja.setColumnWidth(17, 3000);
    			hoja.setColumnWidth(18, 6500);
    			hoja.setColumnWidth(19, 500);
    			hoja.setColumnWidth(20, 500);
    			hoja.setColumnWidth(21, 500);
    			hoja.setColumnWidth(22, 4500);
    			hoja.setColumnWidth(23, 4500);
    			
    			CellStyle estiloMoneda= archivoExcel.createCellStyle();
    			DataFormat df = archivoExcel.createDataFormat();
    			estiloMoneda.setDataFormat(df.getFormat("_ $* #,##0_ ;_ $* -#,##0_ ;_ $* \"-\"??_ ;_ @_ "));
    			
    			CellStyle estiloNegrita= archivoExcel.createCellStyle();
    			Font fuente = archivoExcel.createFont();
    			fuente.setBold(true);
    			estiloNegrita.setFont(fuente);
    			estiloNegrita.setAlignment(CellStyle.ALIGN_CENTER);
    			
    			CellStyle estiloEncabezado = archivoExcel.createCellStyle();
    			Font fuenteEncabezado = archivoExcel.createFont();
    			fuenteEncabezado.setBold(true);
    			fuenteEncabezado.setColor(IndexedColors.WHITE.getIndex());
    			estiloEncabezado.setFont(fuenteEncabezado);
    			estiloEncabezado.setFillForegroundColor(IndexedColors.BLUE.getIndex());  
    			estiloEncabezado.setFillPattern(CellStyle.SOLID_FOREGROUND);  
    			estiloEncabezado.setAlignment(CellStyle.ALIGN_CENTER);
    			estiloEncabezado.setWrapText(true);
    			
    			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    			
    			CellStyle estiloTipoOperacion = archivoExcel.createCellStyle();
    			estiloTipoOperacion.setWrapText(true);
    			
    			
    			
    			/************************************/
    			/**			Encabezado			   **/
    			/************************************/
    			int rowCount = 0;
    			Row fila = hoja.createRow(rowCount);
    			int columnCount = 0;

    			
    			Cell celda = fila.createCell(columnCount);
    			
    			celda.setCellValue("Fecha Desde:");
    			celda.setCellStyle(estiloNegrita);
    			
				celda = fila.createCell(1);
				celda.setCellValue(dateFormat.format(fechaDesde));
    			
    			rowCount = 1;
    			fila = hoja.createRow(rowCount);
    			celda = fila.createCell(columnCount);
    			celda.setCellValue("Fecha Hasta:");
    			celda.setCellStyle(estiloNegrita);
    			
				celda = fila.createCell(1);
				celda.setCellValue(dateFormat.format(fechaHasta));

    			    			
    			rowCount = 4;
    			fila = hoja.createRow(rowCount);
				columnCount = 0;
    			
				celda = fila.createCell(columnCount);
				celda.setCellValue("MES");
				celda.setCellStyle(estiloNegrita);
				
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("N OPERACION");
				celda.setCellStyle(estiloNegrita);
				
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("NOMBRE_CLIENTE");
				celda.setCellStyle(estiloNegrita);
				
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("RUT_CLIENTE");
				celda.setCellStyle(estiloNegrita);
				
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("SEC_RUT_CLIENTE");
				celda.setCellStyle(estiloNegrita);
				
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("MONTO_INICIAL");
				celda.setCellStyle(estiloNegrita);
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("TASA");
				celda.setCellStyle(estiloNegrita);
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("PLAZO");
				celda.setCellStyle(estiloNegrita);
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("TOTAL_INTERES");
				celda.setCellStyle(estiloNegrita);
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("MONTO_FINAL");
				celda.setCellStyle(estiloNegrita);
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("FECHA_INICO");
				celda.setCellStyle(estiloNegrita);
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("FECHA_FINAL");
				celda.setCellStyle(estiloNegrita);
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("FECHA_LIQUIDACION");
				celda.setCellStyle(estiloNegrita);
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("COSTO_FONDO");
				celda.setCellStyle(estiloNegrita);
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("COSTO_FONDO_CDB");
				celda.setCellStyle(estiloNegrita);
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("");
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("UTILIDAD");
				celda.setCellStyle(estiloNegrita);
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("MONEDA");
				celda.setCellStyle(estiloNegrita);
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("MONTO_MONEDA_ORIGEN");
				celda.setCellStyle(estiloNegrita);
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("");
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("");
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("");
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("UTILIDAD/PERDIDA");
				celda.setCellStyle(estiloNegrita);
				
				celda = fila.createCell(++columnCount);
				celda.setCellValue("UTILIDAD_FINAL");
				celda.setCellStyle(estiloNegrita);
				
				/************************************/
    			/**				DETALLE			   **/
    			/************************************/
				if (!ordenes.isEmpty()) {
					for(DetalleOperacion detalleOpercion : ordenes) {
						BigDecimal UtilidadPerdida= calculoUtilidadPerdida(detalleOpercion, fechaDesde, fechaHasta);
						BigDecimal UtilidadFinal = BigDecimal.ZERO;
						if(UtilidadPerdida.signum()== 1) {
							UtilidadFinal= UtilidadPerdida;
						}
						
						columnCount = 0;
						fila = hoja.createRow(++rowCount); //fila en donde se escribe
						
						celda = fila.createCell(columnCount);
	    				celda.setCellValue(getNombreMes(detalleOpercion.MesOperacion));
	    				celda.setCellStyle(estiloEncabezado);
	    				
	    				    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
	    				celda.setCellValue(detalleOpercion.NumeroOperacion);
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellValue(detalleOpercion.NombreCliente);
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellValue(detalleOpercion.RutCLiente);
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellValue(detalleOpercion.SecRutCLiente);
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
	    				celda.setCellValue(detalleOpercion.MontoInicial.doubleValue());
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
	    				celda.setCellValue(detalleOpercion.Tasa.doubleValue());
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellValue(detalleOpercion.Plazo);
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
	    				celda.setCellValue(detalleOpercion.TotalInteres.doubleValue());
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
	    				celda.setCellValue(detalleOpercion.MontoFinal.doubleValue());
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellValue(dateFormat.format(detalleOpercion.FechaInicio));
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellValue(dateFormat.format(detalleOpercion.FechaFinal));
	    				
	    				if(dateFormat.format(detalleOpercion.FechaLiquidacion).equals("01/01/0001")) {
		    				celda = fila.createCell(++columnCount);
		    				celda.setCellValue("");
	    				}else {
		    				celda = fila.createCell(++columnCount);
		    				celda.setCellValue(dateFormat.format(detalleOpercion.FechaLiquidacion));
	    				}
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
	    				celda.setCellValue(detalleOpercion.CostoFondo.doubleValue());
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
	    				celda.setCellValue(detalleOpercion.CostoFondoCDB.doubleValue());
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellValue("");
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
	    				celda.setCellValue(UtilidadPerdida.doubleValue());
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellValue(detalleOpercion.Moneda);
	    				
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
	    				celda.setCellValue(detalleOpercion.MontoMonedaOrigen.doubleValue());
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellValue("");
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellValue("");
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellValue("");
	    				
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
	    				celda.setCellValue(UtilidadPerdida.doubleValue());
	    				
	    				celda = fila.createCell(++columnCount);
	    				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
	    				celda.setCellValue(UtilidadFinal.doubleValue());
	    				
	    				
	    			}
				}
				
				archivoExcel.write(outputStream);			    			
				archivoExcel.close();
		    	}
		return true;    
		}catch(Exception e) {
			logger.error("Error en la creaion de Informe Utilidad/Perdida");
			logger.error(e.getMessage());
			return false;
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection())
		{
			System.out.println("============INICIO============");
			Gson gson = Utils.getGsonBuilder();
			
			String json = "{\"p_IdEmpresa\":1,\"p_Ruta\":\"C:\\\\BCS\\\\logs\",\"p_IdUsuario\":21,\"p_IdModulo\":\"MES\",\"p_FechaDesde\":\"2020-05-01T00:00:00.000Z\",\"p_FechaHasta\":\"2020-06-30T23:59:59.000Z\"}";
			GenerarInformeUtilidadPerdida_Entrada Objin = gson.fromJson(json, GenerarInformeUtilidadPerdida_Entrada.class);
			GenerarInformeUtilidadPerdida demo = new GenerarInformeUtilidadPerdida();
			try 
			{
				GenerarInformeUtilidaPerdida_Salida ObjOut = demo.logica(con, Objin);
				System.out.println(ObjOut.P_OUT_COMENTARIO);
				
				System.out.println("============FIN============");
			}catch(Exception er) {
				er.printStackTrace();
				logger.error(er.getMessage());
				con.rollback();
			}
		}	
    }
		
}


