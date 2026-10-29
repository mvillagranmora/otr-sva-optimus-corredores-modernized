package cl.bcs.contabilidad;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.contabilidad.ConsultarOperacionesClienteRelacionado.ConceptoNormativo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class GenerarInformeClienteRelacionado implements JavaCall {
	private final String SQL_SELECT_LOGO_CORREDORA = "SELECT TOP 1 Imagen.\"Binario\" AS \"Contenido\" FROM \"BSCL.Plataforma.BD::PLA.Imagenes\" Imagen WHERE Imagen.\"NombreImagen\" = 'LOGO_CORREDOR' AND Imagen.\"IdEmpresa\" = :IdEmpresa ORDER BY Imagen.\"Version\" DESC;";  
	
	private final Log log = new Log("COM.ITZ.GenerarInformeClienteRelacionado");
	
	private static final Integer SIN_DECIMALES = 0;

    public static final Map<String, String> TIPO_COMPROBANTE_MAP;
    public static final Map<String, String> CLASIFICACION_MAP;
    public static final Map<String, String> TIPO_INSTRUMENTO_MAP;

    static {
        Map<String, String> map = new HashMap<>(17);
        map.put("FAC", "FA");
        map.put("FAE", "FD");
        map.put("FOC", "FO");
        map.put("FOE", "FT");
        map.put("FEE", "FX");
        map.put("FOX", "FX");
        map.put("NCR", "NC");
        map.put("NCE", "ND");
        map.put("NDR", "NB");
        map.put("NEO", "NT");
        map.put("NEE", "NT");

        map.put("COMFWD", "FW");
        map.put("VENFWD", "FW");
        map.put("FFM", "FM");
        map.put("COMPAC", "CP");
        map.put("VENPAC", "VP");
        map.put("COMTER", "CT");
        map.put("VENTER", "VT");

        TIPO_COMPROBANTE_MAP = Collections.unmodifiableMap(map);
    }

	static {
	    Map<String, String> clasificacion = new HashMap<>(4);
	    clasificacion.put("RF", "RF");
	    clasificacion.put("IF", "IF");
	    clasificacion.put("FM", "FM");
	    clasificacion.put("SPT", "DO");
	
	    Map<String, String> instrumento = new HashMap<>(11);
	    instrumento.put("OSA", "AC");
	    instrumento.put("SIM", "AC");
	    instrumento.put("ACN", "AC");
	    instrumento.put("ETN", "EN");
	    instrumento.put("ETX", "ET");
	    instrumento.put("MIL", "ST");
	    instrumento.put("ACX", "AO");
	    instrumento.put("CFX", "CO");
	    instrumento.put("CFM", "CM");
	    instrumento.put("FWD", "FW");
	    instrumento.put("CFI", "CF");
	    instrumento.put("SPT", "DO");
	
	    CLASIFICACION_MAP = Collections.unmodifiableMap(clasificacion);
	    TIPO_INSTRUMENTO_MAP = Collections.unmodifiableMap(instrumento);
	}
	
	public static class Imagen extends BcsPojo {
		public byte[] Contenido;

		public Imagen() {
			Contenido = new byte[0];
		}
	}
	
	public static class GenerarInformeClienteRelacionado_IN extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_FechaInicio;
		public String p_FechaFin;
		public String p_IdModulo;
	}
	
	public static class GenerarInformeClienteRelacionado_OUT extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public String RutaArchivo;
	}
	
	public static class Configuracion extends BcsPojo {
		public String			RutaBase;
		public String			RutaInforme;
		public String			RutaArchivo;
		public Configuracion () {}
	}


	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();
		GenerarInformeClienteRelacionado_IN in = gson.fromJson(jin.toString(), GenerarInformeClienteRelacionado_IN.class);
		GenerarInformeClienteRelacionado_OUT out = logica(conn, in, true);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}


	public GenerarInformeClienteRelacionado_OUT logica(Connection conn, GenerarInformeClienteRelacionado_IN in, boolean debug) throws Exception {
		GenerarInformeClienteRelacionado_OUT out = new GenerarInformeClienteRelacionado_OUT();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_MENSAJES = new ArrayList<>();

		log.write("Iniciado Generacion Archivo");

		try {
			log.write("Obtencion de variables");
			Long idEmpresa = in.p_IdEmpresa;
			Long idUsuario = in.p_IdUsuario;
			String idModulo = in.p_IdModulo;
			String fechaInicioStr = in.p_FechaInicio;
			String fechaFinStr = in.p_FechaFin;
			String nombre = "InformeOperacionesClientesRelacionados_" 
			        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss")) 
			        + ".xls";
			
			HashMap<String, Object> params = new HashMap<>(1);
			params.put("IdEmpresa", idEmpresa);
			
			Configuracion config = new Configuracion();

			config.RutaArchivo =  ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.RutaBaseArchivos");
			config.RutaInforme =  ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "CONTABILIDAD", "RutaInformeClienteRel");
			
			String rutaFisica = debug ? "C:\\__ARCHIVO_PRUEBA\\" : config.RutaArchivo + config.RutaInforme;
			String rutaFinal = debug ? "C:\\__ARCHIVO_PRUEBA\\" : config.RutaInforme;
			
			if (rutaFisica.equalsIgnoreCase("") || rutaFinal.equalsIgnoreCase(""))
			{
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_CONFIGURACION_RUTA_ARCHIVO",null,conn,in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES); 
				return out;
			}

			log.write("Validaciones");
	
			LocalDate fechaInicio = LocalDate.parse(fechaInicioStr);
			LocalDate fechaFin = LocalDate.parse(fechaFinStr);
	
			if (fechaFin.isBefore(fechaInicio)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_FECHAS_INVALIDAS",null,conn,in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES); 
				return out;
			}
		
			log.write("Comprobar la ruta");
			if (!new File(rutaFisica).exists()) {
				File theDir = new File(rutaFisica);
				theDir.mkdir();
			}
	
			File file = new File(rutaFisica.concat(nombre));
	
			log.write("Inicio Proceso");

			ConsultarOperacionesClienteRelacionado opcr = new ConsultarOperacionesClienteRelacionado();
			ConsultarOperacionesClienteRelacionado.ConsultaOperacionesClientesRelacionados_IN inOperaciones = new ConsultarOperacionesClienteRelacionado.ConsultaOperacionesClientesRelacionados_IN();
			inOperaciones.p_FechaInicio = fechaInicioStr;
			inOperaciones.p_FechaFin = fechaFinStr;
			inOperaciones.p_IdEmpresa = idEmpresa;
			inOperaciones.p_IdModulo = idModulo;
			inOperaciones.p_IdUsuario = idUsuario;

			ConsultarOperacionesClienteRelacionado.ConsultaOperacionesClientesRelacionados_OUT salida = opcr.logica(conn, inOperaciones);
			
			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,salida.P_OUT_MENSAJE,null,conn,in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES); 
				return out;
			}

			HSSFWorkbook workbook = new HSSFWorkbook();
			
			Imagen logo = new SQL().query(conn, SQL_SELECT_LOGO_CORREDORA, params, Imagen.class).stream().findFirst().orElse(new Imagen());

			HSSFSheet sheetConceptos = workbook.createSheet("Conceptos");
			escribirCabeceraReporte(sheetConceptos, workbook, fechaInicioStr, fechaFinStr, logo);
			escribirHojaConceptos(sheetConceptos, workbook, salida.conceptos, salida.totalizado);

			HSSFSheet sheetOperaciones = workbook.createSheet("Operaciones");
			escribirCabeceraReporteOperaciones(sheetOperaciones, workbook, fechaInicioStr, fechaFinStr, logo);
			escribirHojaOperaciones(sheetOperaciones, workbook, salida.operaciones);
			
			log.write("Proceso finalizado");
			
			out.RutaArchivo = rutaFinal.concat(nombre);
			log.write(out.RutaArchivo);

			try (FileOutputStream fos = new FileOutputStream(file)) {
				workbook.write(fos);
			}
			workbook.close();
		} catch (Exception ex) {
			log.write(ex);
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_GENERAR_INFORME",null,conn,in.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES); 
		}

		return out;
	}

	private void escribirCabeceraReporte(HSSFSheet sheet, HSSFWorkbook workbook, String fechaInicio, String fechaFin, Imagen imagen) throws ParseException {
		CellStyle estiloTexto = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 14);
		estiloTexto.setFont(font);
		estiloTexto.setWrapText(true);
		estiloTexto.setAlignment(CellStyle.ALIGN_CENTER);
		estiloTexto.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		sheet.addMergedRegion(new CellRangeAddress(0, 4, 0, 7));


		Row row = sheet.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue("Informe de Operaciones con Clientes Relacionados\nDESDE " + obtenerFechaFormateada(fechaInicio) + " HASTA " + obtenerFechaFormateada(fechaFin));
		cell.setCellStyle(estiloTexto);

		for (int col = 1; col <= 4; col++) {
			Cell c = row.createCell(col);
			c.setCellStyle(estiloTexto);
		}

		row.setHeightInPoints(40);
		

	    if (imagen != null && imagen.Contenido != null && imagen.Contenido.length > 0) {
	        int pictureIdx = workbook.addPicture(imagen.Contenido, Workbook.PICTURE_TYPE_PNG);
	        CreationHelper helper = workbook.getCreationHelper();
	        Drawing drawing = sheet.createDrawingPatriarch();

	        ClientAnchor anchor = helper.createClientAnchor();
	        anchor.setCol1(0);
	        anchor.setRow1(0);
	        anchor.setCol2(2);
	        anchor.setRow2(1);

	        Picture pict = drawing.createPicture(anchor, pictureIdx);
	        pict.resize(0.14, 2.0);
	    }
	}
	
	private void escribirCabeceraReporteOperaciones(HSSFSheet sheet, HSSFWorkbook workbook, String fechaInicio, String fechaFin, Imagen imagen) throws ParseException {
		CellStyle estiloTexto = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 14);
		estiloTexto.setFont(font);
		estiloTexto.setWrapText(true);
		estiloTexto.setAlignment(CellStyle.ALIGN_CENTER);
		estiloTexto.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		sheet.addMergedRegion(new CellRangeAddress(0, 4, 0, 12));


		Row row = sheet.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue("Informe de Operaciones con Clientes Relacionados\nDESDE " + obtenerFechaFormateada(fechaInicio) + " HASTA " + obtenerFechaFormateada(fechaFin));
		cell.setCellStyle(estiloTexto);

		for (int col = 1; col <= 6; col++) {
			Cell c = row.createCell(col);
			c.setCellStyle(estiloTexto);
		}

		row.setHeightInPoints(40);
		

	    if (imagen != null && imagen.Contenido != null && imagen.Contenido.length > 0) {
	        int pictureIdx = workbook.addPicture(imagen.Contenido, Workbook.PICTURE_TYPE_PNG);
	        CreationHelper helper = workbook.getCreationHelper();
	        Drawing drawing = sheet.createDrawingPatriarch();

	        ClientAnchor anchor = helper.createClientAnchor();
	        anchor.setCol1(0);
	        anchor.setRow1(0);
	        anchor.setCol2(1);
	        anchor.setRow2(1);

	        Picture pict = drawing.createPicture(anchor, pictureIdx);
	        pict.resize(1.0, 2.0);
	    }
	}

	private void escribirHojaConceptos(HSSFSheet sheet, HSSFWorkbook workbook,
			List<ConceptoNormativo> conceptos, ConceptoNormativo totalizado) {
		int filaIdx = 9;
		
		CellStyle headerStyle = workbook.createCellStyle();
		Font headerFont = workbook.createFont();
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerStyle.setFont(headerFont);
		headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
		headerStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		headerStyle.setBorderTop(CellStyle.BORDER_THIN);
		headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
		headerStyle.setBorderLeft(CellStyle.BORDER_THIN);
		headerStyle.setBorderRight(CellStyle.BORDER_THIN);
		headerStyle.setWrapText(true);

		CellStyle dataStyle = workbook.createCellStyle();
		dataStyle.setBorderTop(CellStyle.BORDER_THIN);
		dataStyle.setBorderBottom(CellStyle.BORDER_THIN);
		dataStyle.setBorderLeft(CellStyle.BORDER_THIN);
		dataStyle.setBorderRight(CellStyle.BORDER_THIN);

		CellStyle conceptoStyle = workbook.createCellStyle();
		Font conceptoFont = workbook.createFont();
		conceptoFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		conceptoStyle.setFont(conceptoFont);
		conceptoStyle.setBorderTop(CellStyle.BORDER_THIN);
		conceptoStyle.setBorderBottom(CellStyle.BORDER_THIN);
		conceptoStyle.setBorderLeft(CellStyle.BORDER_THIN);
		conceptoStyle.setBorderRight(CellStyle.BORDER_THIN);
		
		CellStyle formatoNumerico = this.obtenerEstiloNumerico(SIN_DECIMALES, workbook, false);
		CellStyle formatoNumericoVacio = this.obtenerEstiloNumerico(SIN_DECIMALES, workbook, true);

		Row header1 = sheet.createRow(filaIdx++);
		Cell hConcepto = header1.createCell(0);
		hConcepto.setCellValue("Concepto");
		hConcepto.setCellStyle(headerStyle);

		Cell hTotalTrans = header1.createCell(1);
		hTotalTrans.setCellValue("Total Transacción");
		hTotalTrans.setCellStyle(headerStyle);

		Cell hSaldo = header1.createCell(4);
		hSaldo.setCellValue("Saldo");
		hSaldo.setCellStyle(headerStyle);

		sheet.addMergedRegion(new CellRangeAddress(9, 10, 0, 0));
		sheet.addMergedRegion(new CellRangeAddress(9, 9, 1, 3));
		sheet.addMergedRegion(new CellRangeAddress(9, 9, 4, 5));

		Row header2 = sheet.createRow(filaIdx++);
		String[] subTotalTrans = { "Cantidad", "Monto \nM$", "Resultado \nM$" };
		String[] subSaldo = { "Activo \nM$", "Pasivo \nM$" };

		int colIndex = 1;
		for (String subCol : subTotalTrans) {
			Cell cell = header2.createCell(colIndex++);
			cell.setCellValue(subCol);
			cell.setCellStyle(headerStyle);
		}
		for (String subCol : subSaldo) {
			Cell cell = header2.createCell(colIndex++);
			cell.setCellValue(subCol);
			cell.setCellStyle(headerStyle);
		}

		if (conceptos != null) {
			for (ConsultarOperacionesClienteRelacionado.ConceptoNormativo c : conceptos) {
				Row row = sheet.createRow(filaIdx++);
				Cell cellConcepto = row.createCell(0);
				cellConcepto.setCellValue("   " + c.Concepto);
				cellConcepto.setCellStyle(conceptoStyle);
				
				for (int i = 1; i <= 5; i++) {
					row.createCell(i).setCellStyle(dataStyle);
				}
				
                formatearCeldaNumerica(row.getCell(1), c.totalTransaccion.Cantidad != null ? c.totalTransaccion.Cantidad.toString() : "0", SIN_DECIMALES, formatoNumerico);
                formatearCeldaNumerica(row.getCell(2), c.totalTransaccion.Monto != null ? c.totalTransaccion.Monto.toString() : "0", SIN_DECIMALES, formatoNumerico);
                formatearCeldaNumerica(row.getCell(3), c.totalTransaccion.Resultado.toString(), SIN_DECIMALES, formatoNumericoVacio);
                formatearCeldaNumerica(row.getCell(4), c.saldo.Activos != null ? c.saldo.Activos.toString() : "0", SIN_DECIMALES, formatoNumericoVacio);
                formatearCeldaNumerica(row.getCell(5), c.saldo.Pasivos != null ? c.saldo.Pasivos.toString() : "0", SIN_DECIMALES, formatoNumericoVacio);
			}
		}
		
		
        Row row = sheet.createRow(filaIdx++);
        Cell cellConcepto = row.createCell(0);
        cellConcepto.setCellValue(totalizado.Concepto);
        cellConcepto.setCellStyle(conceptoStyle);

        for (int i = 1; i <= 5; i++) {
            row.createCell(i).setCellStyle(conceptoStyle);
        }

        formatearCeldaNumerica(row.getCell(1), totalizado.totalTransaccion.Cantidad != null ? totalizado.totalTransaccion.Cantidad.toString() : "0", SIN_DECIMALES, formatoNumerico);
        formatearCeldaNumerica(row.getCell(2), totalizado.totalTransaccion.Monto != null ? totalizado.totalTransaccion.Monto.toString() : "0", SIN_DECIMALES, formatoNumerico);
    	formatearCeldaNumerica(row.getCell(3), totalizado.totalTransaccion.Resultado.toString(), SIN_DECIMALES, formatoNumericoVacio);
        formatearCeldaNumerica(row.getCell(4), totalizado.saldo.Activos != null ? totalizado.saldo.Activos.toString() : "0", SIN_DECIMALES, formatoNumericoVacio);
        formatearCeldaNumerica(row.getCell(5), totalizado.saldo.Pasivos != null ? totalizado.saldo.Pasivos.toString() : "0", SIN_DECIMALES, formatoNumericoVacio);

        CellRangeAddress totalRowRange = new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, 5);
        RegionUtil.setBorderTop(CellStyle.BORDER_MEDIUM, totalRowRange, sheet, workbook);
        RegionUtil.setBorderBottom(CellStyle.BORDER_MEDIUM, totalRowRange, sheet, workbook);
        RegionUtil.setBorderLeft(CellStyle.BORDER_MEDIUM, totalRowRange, sheet, workbook);
        RegionUtil.setBorderRight(CellStyle.BORDER_MEDIUM, totalRowRange, sheet, workbook);

		
		int lastDataRow = filaIdx - 1;

		CellRangeAddress fullTable = new CellRangeAddress(9, lastDataRow, 0, 5);
		RegionUtil.setBorderTop(CellStyle.BORDER_MEDIUM, fullTable, sheet, workbook);
		RegionUtil.setBorderBottom(CellStyle.BORDER_MEDIUM, fullTable, sheet, workbook);
		RegionUtil.setBorderLeft(CellStyle.BORDER_MEDIUM, fullTable, sheet, workbook);
		RegionUtil.setBorderRight(CellStyle.BORDER_MEDIUM, fullTable, sheet, workbook);

		CellRangeAddress bordeConcepto = new CellRangeAddress(9, lastDataRow, 0, 0);
		RegionUtil.setBorderRight(CellStyle.BORDER_MEDIUM, bordeConcepto, sheet, workbook);

		CellRangeAddress bordeTotalTrans = new CellRangeAddress(9, lastDataRow, 3, 3);
		RegionUtil.setBorderRight(CellStyle.BORDER_MEDIUM, bordeTotalTrans, sheet, workbook);

		for (int i = 0; i <= 5; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private void escribirHojaOperaciones(HSSFSheet sheet, HSSFWorkbook workbook,
			List<ConsultarOperacionesClienteRelacionado.OperacionesClienteRelacionadoBase> operaciones) {
		int filaIdx = 9;
		
		CellStyle estiloCabecera = workbook.createCellStyle();
		Font fontCabecera = workbook.createFont();
		fontCabecera.setBoldweight(Font.BOLDWEIGHT_NORMAL);
		estiloCabecera.setFont(fontCabecera);
		estiloCabecera.setAlignment(CellStyle.ALIGN_LEFT);
		estiloCabecera.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		estiloCabecera.setBorderTop(CellStyle.BORDER_THIN);
		estiloCabecera.setBorderBottom(CellStyle.BORDER_THIN);
		estiloCabecera.setBorderLeft(CellStyle.BORDER_THIN);
		estiloCabecera.setBorderRight(CellStyle.BORDER_THIN);
		estiloCabecera.setWrapText(true);

		CellStyle estiloGrupo = workbook.createCellStyle();
		estiloGrupo.cloneStyleFrom(estiloCabecera);
		estiloGrupo.setBorderTop(CellStyle.BORDER_MEDIUM);
		estiloGrupo.setBorderBottom(CellStyle.BORDER_MEDIUM);
		estiloGrupo.setBorderLeft(CellStyle.BORDER_MEDIUM);
		estiloGrupo.setBorderRight(CellStyle.BORDER_MEDIUM);
		
		CellStyle formatoSinDecimal = this.obtenerEstiloNumerico(SIN_DECIMALES, workbook, false);


		Row headerGrupo = sheet.createRow(filaIdx++);
		for (int i = 0; i <= 16; i++) {
			headerGrupo.createCell(i).setCellStyle(estiloGrupo);
		}
		headerGrupo.getCell(0).setCellValue("");
		headerGrupo.getCell(7).setCellValue("RV, RF, IIF y Sin Mercado");
		headerGrupo.getCell(9).setCellValue("Mercado Dólar");
		headerGrupo.getCell(11).setCellValue("Pactos");

		Row headerSub = sheet.createRow(filaIdx++);
		String[] subHeaders = { String.format("%-26s", "Rut"), String.format("%-28s", "Cliente"), String.format("%-12s", "Porta\nfolio")
				, String.format("%-10s", "Tipo \nComp"), String.format("%-12s", "Mercado"), String.format("%-10s", "Folio \nComp.")
				, String.format("%-10s", "Fecha"),"Monto Operado", "Comisión\n Cobrada", "Monto Venta", "Monto \nCompra", "Fecha \nInicio"
				, "Fecha \nTermino", "Monto Inicial", "Monto Final", "Perdida", "Ganancia" };
		int col = 0;
		for (String sub : subHeaders) {
			Cell cell = headerSub.createCell(col++);
			cell.setCellValue(sub);
			cell.setCellStyle(estiloCabecera);
		}

		sheet.addMergedRegion(new CellRangeAddress(9, 9, 0, 6));
		sheet.addMergedRegion(new CellRangeAddress(9, 9, 7, 8));
		sheet.addMergedRegion(new CellRangeAddress(9, 9, 9, 10));
		sheet.addMergedRegion(new CellRangeAddress(9, 9, 11, 16));

		if (operaciones != null) {
			for (ConsultarOperacionesClienteRelacionado.OperacionesClienteRelacionadoBase o : operaciones) {
				Row row = sheet.createRow(filaIdx++);
				
				for (int i = 0; i <= 16; i++) {
				    row.createCell(i);
				}
				
				String TipoComprobante = TIPO_COMPROBANTE_MAP.get(o.TipoDocumento) != null
				        ? TIPO_COMPROBANTE_MAP.get(o.TipoDocumento)
				        : TIPO_COMPROBANTE_MAP.getOrDefault(o.IdTipoOperacion, o.TipoDocumento);
				
				String TipoInstrumento = TIPO_INSTRUMENTO_MAP.get(o.IdTipoInstrumento) != null
				        ? TIPO_INSTRUMENTO_MAP.get(o.IdTipoInstrumento)
				        : CLASIFICACION_MAP.getOrDefault(o.IdClasificacionTipoInstrumento, "Otros Conceptos");

				row.getCell(0).setCellValue(o.DocumentoIdentidad);
				row.getCell(1).setCellValue(o.Nombre);
				row.getCell(2).setCellValue(o.Portafolio);
				row.getCell(3).setCellValue(TipoComprobante);
				row.getCell(4).setCellValue((o instanceof ConsultarOperacionesClienteRelacionado.PactoTermino) ? "Pactos" :  TipoInstrumento);
				row.getCell(5).setCellValue(o.FolioOperacion);
				setFechaString(workbook, row, 6, o.FechaOperacion);

				if (o instanceof ConsultarOperacionesClienteRelacionado.Comprobante) {
					ConsultarOperacionesClienteRelacionado.Comprobante c = (ConsultarOperacionesClienteRelacionado.Comprobante) o;
					formatearCeldaNumerica(row.getCell(7), c.MontoOperado.toString(), SIN_DECIMALES, formatoSinDecimal);
	                formatearCeldaNumerica(row.getCell(8), c.TotalComision.toString(), SIN_DECIMALES, formatoSinDecimal);
	            }
				if (o instanceof ConsultarOperacionesClienteRelacionado.OperacionDolar) {
					ConsultarOperacionesClienteRelacionado.OperacionDolar d = (ConsultarOperacionesClienteRelacionado.OperacionDolar) o;
					row.getCell(9).setCellValue(d.MontoVenta.doubleValue());
					row.getCell(10).setCellValue(d.MontoCompra.doubleValue());
				    formatearCeldaNumerica(row.getCell(9), d.MontoVenta.toString(), SIN_DECIMALES, formatoSinDecimal);
	                formatearCeldaNumerica(row.getCell(10), d.MontoCompra.toString(), SIN_DECIMALES, formatoSinDecimal);
				}
				if (o instanceof ConsultarOperacionesClienteRelacionado.PactoTermino) {
					ConsultarOperacionesClienteRelacionado.PactoTermino p = (ConsultarOperacionesClienteRelacionado.PactoTermino) o;

				    setFechaString(workbook, row, 11, p.FechaInicio);
				    setFechaString(workbook, row, 12, p.FechaTermino);
				    formatearCeldaNumerica(row.getCell(13), p.MontoInicial.toString(), SIN_DECIMALES, formatoSinDecimal);
	                formatearCeldaNumerica(row.getCell(14), p.MontoFinal.toString(), SIN_DECIMALES, formatoSinDecimal);
	                formatearCeldaNumerica(row.getCell(15), p.Perdida != null ? p.Perdida.toString() : "0", SIN_DECIMALES, formatoSinDecimal);
	                formatearCeldaNumerica(row.getCell(16), p.Ganancia != null ? p.Ganancia.toString() : "0", SIN_DECIMALES, formatoSinDecimal);
				}
			}
		}

		int lastDataRow = filaIdx - 1;

		CellRangeAddress fullTable = new CellRangeAddress(9, lastDataRow, 0, 16);
		RegionUtil.setBorderTop(CellStyle.BORDER_MEDIUM, fullTable, sheet, workbook);
		RegionUtil.setBorderBottom(CellStyle.BORDER_MEDIUM, fullTable, sheet, workbook);
		RegionUtil.setBorderLeft(CellStyle.BORDER_MEDIUM, fullTable, sheet, workbook);
		RegionUtil.setBorderRight(CellStyle.BORDER_MEDIUM, fullTable, sheet, workbook);

		CellRangeAddress bordeGrupo1 = new CellRangeAddress(9, lastDataRow, 6, 6);
		RegionUtil.setBorderRight(CellStyle.BORDER_MEDIUM, bordeGrupo1, sheet, workbook);

		CellRangeAddress bordeGrupo2 = new CellRangeAddress(9, lastDataRow, 8, 8);
		RegionUtil.setBorderRight(CellStyle.BORDER_MEDIUM, bordeGrupo2, sheet, workbook);

		CellRangeAddress bordeGrupo3 = new CellRangeAddress(9, lastDataRow, 10, 10);
		RegionUtil.setBorderRight(CellStyle.BORDER_MEDIUM, bordeGrupo3, sheet, workbook);

		for (int i = 0; i <= 16; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private CellStyle obtenerEstiloNumerico (int cantidadDecimales, HSSFWorkbook workbook, boolean formatoVacio) {
					
	    DataFormat dataFormat = workbook.createDataFormat();
	    CellStyle style = workbook.createCellStyle();
	    StringBuilder pattern = new StringBuilder("#,##0");

	    if (cantidadDecimales > 0) {
	        pattern.append(".");
	        for (int i = 0; i < cantidadDecimales; i++) {
	            pattern.append("0");
	        }
	    }

	    pattern.append(";" + (formatoVacio ? "" : "-"))
	        .append(pattern)
	        .append(";" + (formatoVacio ? "" : "-"));  

	    style.setDataFormat(dataFormat.getFormat(pattern.toString()));

	    return style;
	}
	
	private void setFechaString(HSSFWorkbook workbook, Row row, int colIndex, String fechaStr) {
	    Cell cell = row.getCell(colIndex);

	    try {
	        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
	        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
	        String fechaFormateada = outputFormat.format(inputFormat.parse(fechaStr));

	        cell.setCellValue(fechaFormateada);
	    } catch (Exception e) {
	        cell.setCellValue(fechaStr);
	    }
	}
	
	private void formatearCeldaNumerica(Cell celda, String valor, int cantidadDecimales, CellStyle estiloNumerico) {
	    BigDecimal numero = new BigDecimal(valor).setScale(cantidadDecimales, RoundingMode.HALF_UP);
        celda.setCellValue(numero.doubleValue());
        celda.setCellStyle(estiloNumerico);
	}
	
	private String obtenerFechaFormateada(String fechaStr) throws ParseException {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");
        return outputFormat.format(inputFormat.parse(fechaStr));
	}


	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa2", "C:\\Users\\lportales_ext\\OPTIMUS\\workspace\\optimus-corredores-dev-workspace\\OptimusDevJava\\application.properties")) {
			GenerarInformeClienteRelacionado demo = new GenerarInformeClienteRelacionado();
			GenerarInformeClienteRelacionado_IN in = new GenerarInformeClienteRelacionado_IN();
			in.p_IdEmpresa = 1L;
			in.p_IdUsuario = 97L;
			in.p_IdModulo = "CON";
			in.p_FechaInicio = "2025-08-20";
			in.p_FechaFin = "2025-08-20";
			
			JsonParser parser = new JsonParser();
			JsonObject dataJsonIn = parser.parse(new Gson().toJson(in)).getAsJsonObject();
			JsonObject salida = demo.call(con, dataJsonIn);
			System.out.print(salida.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA ==");
	}
}
