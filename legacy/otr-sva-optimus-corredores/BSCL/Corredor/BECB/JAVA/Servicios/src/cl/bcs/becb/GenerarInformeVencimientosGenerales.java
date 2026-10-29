package cl.bcs.becb;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataFormatter;
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
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class GenerarInformeVencimientosGenerales implements JavaCall {
	
	private final static Logger logger = LogManager.getLogger(GenerarInformeVencimientosGenerales.class);
	
	private final String SQL_SELECT_VENCIMIENTOS_PACTOS_SIMULTANEAS = loadResourceToString("cl/bcs/becb/sql/GenerarInformeVencimientosGenerales_ConsultarPactosSimultaneas.sql");
	private final String SQL_SELECT_VENCIMIENTOS_PACTOS_SIMULTANEAS_HIST = loadResourceToString("cl/bcs/becb/sql/GenerarInformeVencimientosGenerales_ConsultarPactosSimultaneasHistorica.sql");
	private final String SQL_SELECT_VENCIMIENTOS_SPOTS = loadResourceToString("cl/bcs/becb/sql/GenerarInformeVencimientosGenerales_ConsultarSpots.sql");
	private final String SQL_SELECT_VENCIMIENTOS_CUENTA_PROPIA_TERCEROS = loadResourceToString("cl/bcs/becb/sql/GenerarInformeVencimientosGenerales_ConsultarCuentaPropiaTerceros.sql");
	private final String SQL_SELECT_VENCIMIENTOS_CUENTA_PROPIA_TERCEROS_HIST = loadResourceToString("cl/bcs/becb/sql/GenerarInformeVencimientosGenerales_ConsultarCuentaPropiaTercerosHistorica.sql");
	private final String SQL_SELECT_VENCIMIENTOS_OPERACIONES_TERMINO = loadResourceToString("cl/bcs/becb/sql/GenerarInformeVencimientosGenerales_ConsultarVencimientoOperacionesTermino.sql");
	private final String SQL_SELECT_VENCIMIENTOS_OPERACIONES_TERMINO_HIST = loadResourceToString("cl/bcs/becb/sql/GenerarInformeVencimientosGenerales_ConsultarVencimientoOperacionesTerminoHistorica.sql");
	
	public static class EntradaGenerarInformeVencimientosGenerales {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaSistema;
		public String p_Ruta;
		public Boolean p_Interfaz = true;
		public String p_FechaInforme;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaGenerarInformeVencimientosGenerales {
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
	
	public static class DataPactosSimultaneas {
		public Long 		IdOperacion;
		public String		NombreCliente;
		public String		RutCliente;
		public String		FechaInicio;
		public String		FechaVencimiento;
		public String		CodigoPortafolio;
		public BigDecimal 	MontoPagarRecibir;
		public String		IdMonedaOrigen;
		public String		IdMonedaPago;
		public String		IdTipoInstrumento;
		public String		EsAdministradorCartera;
		public String		AgruparSeccion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DataSpot {
		public String 		FechaOperacion;
		public String		NombreCliente;
		public String		TipoOperacion;
		public String		IdMonedaPrincipal;
		public BigDecimal	MontoPrimario;
		public BigDecimal	Precio;
		public String		IdMonedaSecundaria;
		public BigDecimal 	MontoSecundario;
		public String		FormaPagoEntrega;
		public String		FormaPagoRecibe;
		public String		AgruparSeccion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DataCuentaPropiaTerceros {
		public String		NombreCliente;
		public String		RutCliente;
		public String		CodigoPortafolio;		
		public String 		Nemotecnico;
		public String		FechaCompra;
		public String		FechaVencimiento;
		public String		Emisor;
		public String		Reajustabilidad;
		public BigDecimal	Precio;
		public BigDecimal	ValorRescate;
		public BigDecimal 	MontoPagar;
		public String		TipoCustodia;
		public String		IdMonedaPago;
		public String		EsCuentaPropia;
		public String		EsAdministradorCartera;
		public String		AgruparSeccion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DataOperacionesTermino {
		public String 		FechaCompra;
		public String 		FechaVencimiento;
		public String		Nemotecnico;
		public String		IdEmisor;
		public String		IdReajuste;
		public BigDecimal	Precio;
		public BigDecimal	Monto;
		public String 		IdMonedaPago;
		public String		IdBoveda;
		public String		EsAdministracionCartera;
		public String		AgruparSeccion;
		public String       DocumentoIdentidad;
		public String       CodigoPortafolio;
		public String       NombreCompleto;
		public BigDecimal   ValorRescate;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// propiedades para configuracion de hoja excel
	private final Integer CANTIDAD_COLUMNAS_EN_HOJA = 13;
	private Integer columnaActual = 0;
	private Integer filaActual = 0;
	
	CellStyle estiloTituloEncabezado;
	CellStyle estiloSubTituloSeccion;
	CellStyle estiloTituloCampo;
	CellStyle estiloTotalCampo;
	CellStyle estiloTextoIzq;
	CellStyle estiloTextoCen;
	CellStyle estiloNumero;
	CellStyle estiloFondo;
	
	private enum EstiloCelda {
		TEXTO_IZQUIERDA,
		TEXTO_CENTRO,
		ENCABEZADO,
		SUBTITULO,
		TOTALES,
		TITULO,
		NUMERO,
		FONDO
	}	
	
	//configuracion exportar pdf
	public static String TituloPrincipal; 
	public static  List<String> TitulosSecundario= new ArrayList<>();;

	public class CellPdf {
		public  List<String> lines;
		public PDFont font;
		public  BigDecimal fontSize;
		public Integer aligneacion;
	}
	
	
	public class PdfColumnConfig {
		public BigDecimal width;
		public Integer align;
		public BigDecimal fontSize;
		public DecimalFormat numberFormat;
	    public PdfColumnConfig(BigDecimal width, int align, BigDecimal fontSize) {
	        this.width = width;
	        this.align = align;
	        this.fontSize = fontSize;
	        this.numberFormat = null; 
	    }
	    public PdfColumnConfig(BigDecimal width, int align, BigDecimal fontSize, DecimalFormat numberFormat) {
	        this.width = width;
	        this.align = align;
	        this.fontSize = fontSize;
	        this.numberFormat = numberFormat;
	    }
	    
	}
	static final int ALIGN_LEFT = 0;
	static final int ALIGN_CENTER = 1;
	static final int ALIGN_RIGHT = 2;
	
	public SalidaGenerarInformeVencimientosGenerales logica(Connection con, EntradaGenerarInformeVencimientosGenerales entrada) throws Exception {
		SalidaGenerarInformeVencimientosGenerales out = new SalidaGenerarInformeVencimientosGenerales();
		String fechaSistema = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.FechaSistema");
		// validacion fecha de informe
		if (entrada.p_FechaInforme==null || entrada.p_FechaInforme.equals("")) {
			entrada.p_FechaInforme = fechaSistema;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date fechaInforme = dateFormat.parse(entrada.p_FechaInforme);
		String nombreArchivo = "InformeVencimientosGenerales_" + new SimpleDateFormat("ddMMyyyy").format(fechaInforme);
		TitulosSecundario.clear();
		
		/***************************************/
		/** Definicion de nombre de archivos  **/
		/***************************************/
		out.p_out_ErroresContador = 0L;
		out.p_out_NombreArchivo = nombreArchivo + ".xls";
		out.p_out_NombreArchivoLog = nombreArchivo + ".log";
		out.p_out_RutaArchivo = entrada.p_Ruta + "/" + out.p_out_NombreArchivo;
		out.p_out_RutaArchivoLog = entrada.p_Ruta + "/" + out.p_out_NombreArchivoLog;
		out.p_out_Comentario =  Diccionario.getMensaje(con, entrada.p_IdEmpresa, "ARCHIVO_GENERADO_EXITOSAMENTE").msg();

		try (FileOutputStream outputStream = new FileOutputStream(out.p_out_RutaArchivo)) {
			Workbook archivoExcel = new HSSFWorkbook();
			Sheet hoja = archivoExcel.createSheet();
			
			/************************************/
			/** Estilos y Formatos 			   **/
			/************************************/
			fijarEstilos(archivoExcel);
			fijarAnchoColumnasHojaExcel(hoja);	// establece el ancho de las columnas 0-8 (A-I) en la hoja de Excel
			archivoExcel.setSheetName(0, Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_GENERAR_INFORME_VENCIMIENTOS_GENERALES").msg());
			
			/************************************/
			/** Titulo de informe 			   **/
			/************************************/
			columnaActual = 4;		// Columna E en la hoja Excel
			Row fila = hoja.createRow(filaActual);
			Cell celda = fila.createCell(columnaActual);
			establecerTituloEnFila(hoja, celda, fila, estiloTituloEncabezado, Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_INFORME_VENCIMIENTOS_FECHA").add("fecha", new SimpleDateFormat("dd/MM/yyyy").format(fechaInforme)).msg(), columnaActual);
			TituloPrincipal= Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_INFORME_VENCIMIENTOS_FECHA").add("fecha", new SimpleDateFormat("dd/MM/yyyy").format(fechaInforme)).msg();
				
			/************************************/
			/** Captura de datos 			   **/
			/************************************/
			
			Date fechaSistemaDate = dateFormat.parse(entrada.p_FechaSistema);
			boolean esHistorica = fechaInforme.compareTo(fechaSistemaDate)<0;
			
			// consulta de vencimiento de pactos y simultaneas 
			SQL sql = new SQL();
			HashMap<String, Object> params = new HashMap<>();
			params.put("p_IdEmpresa", entrada.p_IdEmpresa);
			params.put("p_FechaVencimiento", entrada.p_FechaInforme);
			List<DataPactosSimultaneas> listaPactosSimultaneas = sql.query(con, esHistorica ? SQL_SELECT_VENCIMIENTOS_PACTOS_SIMULTANEAS_HIST: SQL_SELECT_VENCIMIENTOS_PACTOS_SIMULTANEAS, params, DataPactosSimultaneas.class);
			
			// consulta de spots
			params.clear();
			params.put("p_IdEmpresa", entrada.p_IdEmpresa);
			params.put("p_FechaOperacion", entrada.p_FechaInforme);
			List<DataSpot> listaSpots = sql.query(con, SQL_SELECT_VENCIMIENTOS_SPOTS, params, DataSpot.class);
			
			// consulta de vencimiento de cuenta propia
			params.clear();
			params.put("p_IdEmpresa", entrada.p_IdEmpresa);
			params.put("p_FechaVencimiento", entrada.p_FechaInforme);
			List<DataCuentaPropiaTerceros> listaCuentaPropiaTerceros = sql.query(con, esHistorica ? SQL_SELECT_VENCIMIENTOS_CUENTA_PROPIA_TERCEROS_HIST : SQL_SELECT_VENCIMIENTOS_CUENTA_PROPIA_TERCEROS, params, DataCuentaPropiaTerceros.class);

			// consulta de vencimiento de cuenta terceros
			params.clear();
			params.put("p_IdEmpresa", entrada.p_IdEmpresa);
			params.put("p_FechaVencimiento", entrada.p_FechaInforme);
			if (esHistorica) params.put("p_FechaConsulta", entrada.p_FechaInforme);
			List<DataOperacionesTermino> listaOperacionesTermino = new ArrayList<DataOperacionesTermino>();
			listaOperacionesTermino = sql.query(con, esHistorica ? SQL_SELECT_VENCIMIENTOS_OPERACIONES_TERMINO_HIST: SQL_SELECT_VENCIMIENTOS_OPERACIONES_TERMINO, params, DataOperacionesTermino.class);

			
			/************************************/
			/** Exportacion de datos a Excel   **/
			/************************************/
			// Vencimientos de Pactos y Simultaneas
			if (!listaPactosSimultaneas.isEmpty()) {
				crearSeccionPactosSimultaneas(con, entrada.p_IdEmpresa, listaPactosSimultaneas, hoja);
			}		
			
			// Vencimientos de Spots
			if (!listaSpots.isEmpty()) {
				crearSeccionSpot(con, entrada.p_IdEmpresa, listaSpots, hoja);
			}
			
			// Vencimiento de Instrumentos - Cuenta Propia
			List<DataCuentaPropiaTerceros> listaCuentaPropia = listaCuentaPropiaTerceros.stream().filter(e-> e.EsCuentaPropia.equalsIgnoreCase("S")).collect(Collectors.toList());
			if (!listaCuentaPropia.isEmpty()) {
				crearSeccionCuentaPropia(con, entrada.p_IdEmpresa, listaCuentaPropia, hoja);
			}
			
			// Vencimiento de Instrumentos - Cuenta Terceros
			List<DataOperacionesTermino> listaOperacionTerminoAdmCartera = listaOperacionesTermino.stream().filter(e-> e.EsAdministracionCartera.equalsIgnoreCase("S")).collect(Collectors.toList());
			if (!listaOperacionTerminoAdmCartera.isEmpty()) {
				crearSeccionOperacionesTerminoAdmCartera(con, entrada.p_IdEmpresa, listaOperacionTerminoAdmCartera, hoja);
			}
			
			List<DataOperacionesTermino> listaOperacionTerminoSinAdmCartera = listaOperacionesTermino.stream().filter(e-> !e.EsAdministracionCartera.equalsIgnoreCase("S")).collect(Collectors.toList());
			if (!listaOperacionTerminoSinAdmCartera.isEmpty()) {
				crearSeccionOperacionesTerminoSinAdmCartera(con, entrada.p_IdEmpresa, listaOperacionTerminoSinAdmCartera, hoja);
			}
			
			archivoExcel.write(outputStream);
			//------- Exporta a pdf ------
			generaExpotacioPdf( archivoExcel,out.p_out_RutaArchivo, out );

			logger.debug(archivoExcel);
			
		} catch (Exception e) {
			out.p_out_ErroresContador++;
			out.p_out_Comentario = e.getMessage();
		}
		
		// crear archivo log
		File fileRF = new File(out.p_out_RutaArchivoLog);
		try (BufferedWriter bwAchivoLog = new BufferedWriter(new FileWriter(fileRF));) {
			bwAchivoLog.write(out.p_out_Comentario);
			bwAchivoLog.newLine();
		}
		
		return out;
	}
	
	private void crearSeccionPactosSimultaneas(Connection con, Long IdEmpresa, List<DataPactosSimultaneas> pactosSimultaneas, Sheet hoja) throws Exception {

		Row fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(0);
		
		// obtener moneda local del sistema para identificar la moneda nacional
		String idMonedaSistema = Utils.getConfiguracion(con, Utils.getSchema(), IdEmpresa, "PLA.MonedaSistema");

		// encabezado de seccion
		String[] encabezado = {"UI_NO_OPERACION", "UI_CLIENTE", "UI_DOCUMENTO_IDENTIDAD", "UI_FECHA_INICIO", "UI_FECHA_VENCIMIENTO", 
							   "UI_CODIGO_PORTAFOLIO", "UI_MONTO_PAGAR_RECIBIR", "UI_ID_MONEDA_ORIGEN", "UI_MONEDA_PAGO"};
		
		/************************************/
		/** DETALLE DE SECCION			   **/
		/************************************/
		--filaActual;
		Double montoTotal = 0d;
		String agruparSeccion = "";
		int columnaTotal = ArrayUtils.indexOf(encabezado, "UI_MONTO_PAGAR_RECIBIR");
		
		for (DataPactosSimultaneas dataPactosSimultaneas : pactosSimultaneas) {
			columnaActual = 0;
			
			// establecer header de seccion
			// agrupar por Tipo de isntrumento (PAC/SIM), tipo de moneda (Nacional/Extranjera) y administrador de cartera (Si/No)
			if (!dataPactosSimultaneas.AgruparSeccion.equals(agruparSeccion)) {
				
				/*************************************/
				/** Encabezado de seccion por grupo **/
				/*************************************/
				if (!agruparSeccion.equals("")) {
					// establece el total del monto a pagar o recibir
					fila = hoja.createRow(++filaActual);
					establecerTotalColumna(hoja, celda, fila, montoTotal, columnaTotal, 0);
					montoTotal = 0d;
				}

				// subtitulos y encabezado de la siguiente seccion
				fila = hoja.createRow(++filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloFondo, "", 0);	// agregar linea en blanco
				fila = hoja.createRow(filaActual);
				
				if (dataPactosSimultaneas.IdTipoInstrumento.equalsIgnoreCase("PAC")) {
					boolean esMonedaNacional = dataPactosSimultaneas.IdMonedaOrigen.equalsIgnoreCase(idMonedaSistema);					
					
					establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, esMonedaNacional ? "UI_VENCIMIENTO_PACTOS_MONEDA_NACIONAL" : "UI_VENCIMIENTO_PACTOS_MONEDA_EXTRANJERA").msg(), columnaActual);
					fila = hoja.createRow(filaActual);
					establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, esMonedaNacional ? "UI_MONEDA_NACIONAL" : "UI_MONEDA_EXTRANJERA").msg(), columnaActual);			
					fila = hoja.createRow(filaActual);
					establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, dataPactosSimultaneas.EsAdministradorCartera.equalsIgnoreCase("S") ? "UI_CLIENTES_CON_ADMINISTRACION_CARTERA" : "UI_CLIENTES_SIN_ADMINISTRACION_CARTERA").msg(), columnaActual);
					fila = hoja.createRow(filaActual);
				} else {
					establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, "UI_VENCIMIENTO_SIMULTANEAS").msg(), columnaActual);
					fila = hoja.createRow(filaActual);
				}
				crearEncabezadoSeccion(con, IdEmpresa, encabezado, celda, fila);
				agruparSeccion = dataPactosSimultaneas.AgruparSeccion;
			}					
			
			fila = hoja.createRow(++filaActual);
			
			//columna 0 (A)
			celda = fila.createCell(columnaActual);
			celda.setCellStyle(estiloFondo);
			celda.setCellValue(dataPactosSimultaneas.IdOperacion);
			
			//columna 1 (B)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataPactosSimultaneas.NombreCliente);

			//columna 2 (C)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloFondo);
			celda.setCellValue(dataPactosSimultaneas.RutCliente);

			//columna 3 (D)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);//estiloFondo);
			celda.setCellValue(dataPactosSimultaneas.FechaInicio);
			
			//columna 4 (E)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);//estiloFondo);
			celda.setCellValue(dataPactosSimultaneas.FechaVencimiento);
			
			//columna 5 (F)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloFondo);
			celda.setCellValue(dataPactosSimultaneas.CodigoPortafolio);
			
			//columna 6 (G)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(dataPactosSimultaneas.MontoPagarRecibir.doubleValue());
			celda.setCellStyle(estiloNumero);
			montoTotal += dataPactosSimultaneas.MontoPagarRecibir.doubleValue();

			//columna 7 (H)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataPactosSimultaneas.IdMonedaOrigen);

			//columna 8 (I)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataPactosSimultaneas.IdMonedaPago);
			
			// Rellena columnas siguientes en blanco basado en la cantidad de columnas
			// establecidas en la hoja de Excel para establecer el margen derecho del informe
			completarColumnasEnBlanco(fila);
		}
		
		// total monto total a pagar o recibir del ultimo grupo
		fila = hoja.createRow(++filaActual);
		establecerTotalColumna(hoja, celda, fila, montoTotal, columnaTotal, 0);
		++filaActual;
	}
	
	private void crearSeccionSpot(Connection con, Long IdEmpresa, List<DataSpot> spots, Sheet hoja) throws Exception {
		
		Row fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(0);
		
		// encabezado de seccion
		String[] encabezado = {"UI_FECHA_OPERACION", "UI_CLIENTE", "UI_TIPO_OPERACION", "UI_MONTO_PPAL", "UI_PRECIO", 
				   			   "UI_MONTO_SECUNDARIO", "UI_FORMA_PAGO_ENTREGA", "UI_FORMA_PAGO_RECIBE", ""};
		
		/************************************/
		/** DETALLE DE SECCION			   **/
		/************************************/
		--filaActual;
		String agruparSeccion = "";
		Double montoTotalPrincipal = 0d;
		Double montoTotalSecundario = 0d;
		int columnaTotalPrincipal = ArrayUtils.indexOf(encabezado, "UI_MONTO_PPAL");
		int columnaTotalSecundaria = ArrayUtils.indexOf(encabezado, "UI_MONTO_SECUNDARIO");
		String monedaPrincipal = Diccionario.getMensaje(con, IdEmpresa, "UI_MONEDA_PRINCIPAL").msg();
		String monedaSecundaria = Diccionario.getMensaje(con, IdEmpresa, "UI_MONEDA_SECUNDARIA").msg();
		
		for (DataSpot dataSpots : spots) {
			columnaActual = 0;
			
			// establecer header de seccion
			// agrupar por Cliente
			if (!dataSpots.AgruparSeccion.equals(agruparSeccion)) {
				
				/*************************************/
				/** Encabezado de seccion por grupo **/
				/*************************************/
				if (!agruparSeccion.equals("")) {
					// establece el total del monto principal y secundario de la seccion
					fila = hoja.createRow(++filaActual);
					establecerTotalColumna(hoja, celda, fila, montoTotalPrincipal, columnaTotalPrincipal, 0);
					establecerTotalColumna(hoja, celda, fila, montoTotalSecundario, columnaTotalSecundaria, columnaTotalPrincipal+1);
					montoTotalPrincipal = 0d;
					montoTotalSecundario = 0d;
				}
				
				// subtitulos y encabezado de la siguiente seccion
				fila = hoja.createRow(++filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloFondo, "", 0);	// agregar linea en blanco
				fila = hoja.createRow(filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, "Spot", columnaActual);
				fila = hoja.createRow(filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, monedaPrincipal + ": " +dataSpots.IdMonedaPrincipal, columnaActual);
				fila = hoja.createRow(filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, monedaSecundaria + ": " +dataSpots.IdMonedaSecundaria, columnaActual);
				fila = hoja.createRow(filaActual);				
				crearEncabezadoSeccion(con, IdEmpresa, encabezado, celda, fila);
				agruparSeccion = dataSpots.AgruparSeccion;
			} 
			
			fila = hoja.createRow(++filaActual);
			
			//columna 0 (A)
			celda = fila.createCell(columnaActual);
			celda.setCellStyle(estiloTextoCen);//estiloFondo);
			celda.setCellValue(dataSpots.FechaOperacion);
			
			//columna 1 (B)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataSpots.NombreCliente);
			
			//columna 2 (C)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataSpots.TipoOperacion);

			//columna 3 (D)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellStyle(estiloNumero);
			celda.setCellValue(dataSpots.MontoPrimario.doubleValue());
			montoTotalPrincipal += dataSpots.MontoPrimario.doubleValue();
			
			//columna 4 (E)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellStyle(estiloNumero);
			celda.setCellValue(dataSpots.Precio.doubleValue());
			
			//columna 5 (F)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellStyle(estiloNumero);			
			celda.setCellValue(dataSpots.MontoSecundario.doubleValue());
			montoTotalSecundario += dataSpots.MontoSecundario.doubleValue();
			
			//columna 6 (G)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataSpots.FormaPagoEntrega);

			//columna 7 (H)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataSpots.FormaPagoRecibe);
			
			//columna 8 (I)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloFondo);
			celda.setCellValue("");
			
			// Rellena columnas siguientes en blanco basado en la cantidad de columnas
			// establecidas en la hoja de Excel para establecer el margen derecho del informe
			completarColumnasEnBlanco(fila);
		}
		
		// total monto principal y secundario del ultimo grupo
		fila = hoja.createRow(++filaActual);
		establecerTotalColumna(hoja, celda, fila, montoTotalPrincipal, columnaTotalPrincipal, 0);
		establecerTotalColumna(hoja, celda, fila, montoTotalSecundario, columnaTotalSecundaria, columnaTotalPrincipal+1);
		++filaActual;
	}
	
	private void crearSeccionCuentaPropia(Connection con, Long IdEmpresa, List<DataCuentaPropiaTerceros> cuentaPropiaTerceros, Sheet hoja) throws Exception {
		
		Row fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(0);
		
		// encabezado de seccion
		//String[] encabezado = { "UI_CLIENTE", "UI_RUT", "UI_PORTAFOLIO", "UI_NEMOTENICO", "UI_FECHA_COMPRA", "UI_FECHA_VENCIMIENTO", "UI_EMISOR", "UI_REAJUSTABILIDAD", 
		//					   "UI_VALOR_RESCATE", "UI_MONTO_PAGAR", "UI_MONEDA_PAGO", "UI_TIPO_CUSTODIA"};
		String[] encabezado = {"UI_NEMOTENICO", "UI_CLIENTE", "UI_DOCUMENTO_IDENTIDAD", "UI_CODIGO_PORTAFOLIO", "UI_FECHA_COMPRA", "UI_FECHA_VENCIMIENTO", "UI_EMISOR", "UI_REAJUSTABILIDAD", 
				   "UI_VALOR_RESCATE", "UI_MONTO_PAGAR", "UI_MONEDA_PAGO", "UI_TIPO_CUSTODIA"};
		
		
		/************************************/
		/** DETALLE DE SECCION			   **/
		/************************************/
		--filaActual;
		String agruparSeccion = "";
		Double montoTotalMontoPagar = 0d;
		Double montoTotalValorRescate = 0d;
		int columnaTotalMontoPagar = ArrayUtils.indexOf(encabezado, "UI_MONTO_PAGAR");
		int columnaTotalValorRescate = ArrayUtils.indexOf(encabezado, "UI_VALOR_RESCATE");
		
		for (DataCuentaPropiaTerceros dataCuentaPropiaTerceros : cuentaPropiaTerceros) {
			columnaActual = 0;
			
			// establecer header de seccion
			// agrupar por Tipo de Cuenta (Propia/Tercero) y administrador de cartera (Si/No)
			if (!dataCuentaPropiaTerceros.AgruparSeccion.equals(agruparSeccion)) {
				
				/*************************************/
				/** Encabezado de seccion por grupo **/
				/*************************************/
				if (!agruparSeccion.equals("")) {
					// establece el total del monto a pagar de la seccion
					fila = hoja.createRow(++filaActual);
					establecerTotalColumna(hoja, celda, fila, montoTotalValorRescate, columnaTotalValorRescate, 0);
					establecerTotalColumna(hoja, celda, fila, montoTotalMontoPagar, columnaTotalMontoPagar, columnaTotalValorRescate+1);				
					montoTotalMontoPagar = 0d;
					montoTotalValorRescate = 0d;
				}
				
				// subtitulos y encabezado de la siguiente seccion
				fila = hoja.createRow(++filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloFondo, "", 0);	// agregar linea en blanco
				fila = hoja.createRow(filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, "UI_VENCIMIENTO_INSTRUMENTOS").msg(), columnaActual);
				fila = hoja.createRow(filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, dataCuentaPropiaTerceros.EsCuentaPropia.equalsIgnoreCase("S") ? "UI_CUENTA_PROPIA" : "UI_CUENTA_TERCEROS").msg(), columnaActual);			
				fila = hoja.createRow(filaActual);
				
				crearEncabezadoSeccion(con, IdEmpresa, encabezado, celda, fila);
				agruparSeccion = dataCuentaPropiaTerceros.AgruparSeccion;
			}
			
			fila = hoja.createRow(++filaActual);
			
			celda = fila.createCell(columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataCuentaPropiaTerceros.Nemotecnico);
			
			//columna 0 (A)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataCuentaPropiaTerceros.NombreCliente);

			//columna 1 (B)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataCuentaPropiaTerceros.RutCliente);

			//columna 2 (C)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloFondo);//estiloTextoIzq);
			celda.setCellValue(dataCuentaPropiaTerceros.CodigoPortafolio);			

			//columna 3 (D)
			//celda = fila.createCell(++columnaActual);
			//celda.setCellStyle(estiloTextoIzq);
			//celda.setCellValue(dataCuentaPropiaTerceros.Nemotecnico);
			
			//columna 4 (E)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataCuentaPropiaTerceros.FechaCompra);
			
			//columna 5 (F)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataCuentaPropiaTerceros.FechaVencimiento);

			//columna 6 (G)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataCuentaPropiaTerceros.Emisor);
			
			//columna 7 (H)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataCuentaPropiaTerceros.Reajustabilidad);
			
			//columna 8 (I)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellStyle(estiloNumero);
			celda.setCellValue(dataCuentaPropiaTerceros.ValorRescate.doubleValue());
			montoTotalValorRescate += dataCuentaPropiaTerceros.ValorRescate.doubleValue();
			
			//columna 9 (J)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellStyle(estiloNumero);
			celda.setCellValue(dataCuentaPropiaTerceros.MontoPagar.doubleValue());
			montoTotalMontoPagar += dataCuentaPropiaTerceros.MontoPagar.doubleValue();
			
			//columna 10 (K)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataCuentaPropiaTerceros.IdMonedaPago);
						
			//columna 11 (L)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataCuentaPropiaTerceros.TipoCustodia);
			
			//columna 12 (M)		-- En blanco para establecer el margen derecho del informe
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloFondo);
			celda.setCellValue("");
			
			// Rellena columnas siguientes en blanco basado en la cantidad de columnas
			// establecidas en la hoja de Excel para establecer el margen derecho del informe
			completarColumnasEnBlanco(fila);			
		}

		// establece el total del monto a pagar y valor rescate del ultimo grupo
		fila = hoja.createRow(++filaActual);
		establecerTotalColumna(hoja, celda, fila, montoTotalValorRescate, columnaTotalValorRescate, 0);
		establecerTotalColumna(hoja, celda, fila, montoTotalMontoPagar, columnaTotalMontoPagar, columnaTotalValorRescate+1);
		++filaActual;
	}
	
	private void crearSeccionCuentaTerceros(Connection con, Long IdEmpresa, List<DataCuentaPropiaTerceros> cuentaPropiaTerceros, Sheet hoja) throws Exception {
		
		Row fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(0);
		
		// encabezado de seccion
		String[] encabezado = {"UI_NEMOTENICO", "UI_FECHA_COMPRA", "UI_FECHA_VENCIMIENTO", "UI_EMISOR", "UI_REAJUSTABILIDAD", 
							   "UI_PRECIO", "UI_MONTO_PAGAR", "UI_MONEDA_PAGO", "UI_TIPO_CUSTODIA"};
		
		/************************************/
		/** DETALLE DE SECCION			   **/
		/************************************/
		--filaActual;
		Double montoTotal = 0d;
		String agruparSeccion = "";
		int columnaTotal = ArrayUtils.indexOf(encabezado, "UI_MONTO_PAGAR");
		
		for (DataCuentaPropiaTerceros dataCuentaPropiaTerceros : cuentaPropiaTerceros) {
			columnaActual = 0;
			
			// establecer header de seccion
			// agrupar por Tipo de Cuenta (Propia/Tercero) y administrador de cartera (Si/No)
			if (!dataCuentaPropiaTerceros.AgruparSeccion.equals(agruparSeccion)) {
				
				/*************************************/
				/** Encabezado de seccion por grupo **/
				/*************************************/
				if (!agruparSeccion.equals("")) {
					// establece el total del monto a pagar de la seccion
					fila = hoja.createRow(++filaActual);
					establecerTotalColumna(hoja, celda, fila, montoTotal, columnaTotal, 0);
					montoTotal = 0d;
				}
				
				// subtitulos y encabezado de la siguiente seccion
				fila = hoja.createRow(++filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloFondo, "", 0);	// agregar linea en blanco
				fila = hoja.createRow(filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, "UI_VENCIMIENTO_INSTRUMENTOS").msg(), columnaActual);
				fila = hoja.createRow(filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, dataCuentaPropiaTerceros.EsCuentaPropia.equalsIgnoreCase("S") ? "UI_CUENTA_PROPIA" : "UI_CUENTA_TERCEROS").msg(), columnaActual);			
				fila = hoja.createRow(filaActual);
				
				// define subtitulo si es adminsitrador de cartera para cuentas a terceros
				establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, dataCuentaPropiaTerceros.EsAdministradorCartera.equalsIgnoreCase("S") ? "UI_CLIENTES_CON_ADMINISTRACION_CARTERA" : "UI_CLIENTES_SIN_ADMINISTRACION_CARTERA").msg(), columnaActual);
				fila = hoja.createRow(filaActual);
				crearEncabezadoSeccion(con, IdEmpresa, encabezado, celda, fila);
				agruparSeccion = dataCuentaPropiaTerceros.AgruparSeccion;
			}
			
			fila = hoja.createRow(++filaActual);

			//columna 0 (A)
			celda = fila.createCell(columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataCuentaPropiaTerceros.Nemotecnico);
			
			//columna 1 (B)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataCuentaPropiaTerceros.FechaCompra);
			
			//columna 2 (C)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataCuentaPropiaTerceros.FechaVencimiento);

			//columna 3 (D)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataCuentaPropiaTerceros.Emisor);
			
			//columna 4 (E)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataCuentaPropiaTerceros.Reajustabilidad);
			
			//columna 5 (F)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellStyle(estiloNumero);
			celda.setCellValue(dataCuentaPropiaTerceros.Precio.doubleValue());
			
			//columna 6 (G)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellStyle(estiloNumero);
			celda.setCellValue(dataCuentaPropiaTerceros.MontoPagar.doubleValue());
			montoTotal += dataCuentaPropiaTerceros.MontoPagar.doubleValue();
			
			//columna 7 (H)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataCuentaPropiaTerceros.IdMonedaPago);
			
			//columna 8 (I)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataCuentaPropiaTerceros.TipoCustodia);			
			
			//columna 9 (J)		-- En blanco para establecer el margen derecho del informe
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloFondo);
			celda.setCellValue("");
			
			// Rellena columnas siguientes en blanco basado en la cantidad de columnas
			// establecidas en la hoja de Excel para establecer el margen derecho del informe
			completarColumnasEnBlanco(fila);			
		}
		
		// total monto total a pagar o recibir del ultimo grupo
		fila = hoja.createRow(++filaActual);
		establecerTotalColumna(hoja, celda, fila, montoTotal, columnaTotal, 0);
		
		++filaActual;
		fila = hoja.createRow(filaActual);
		// agregar linea en blanco para crear margen inferior en la hoja de Excel (aplica solo en ultima secion)
		establecerTituloEnFila(hoja, celda, fila, estiloFondo, "", 0);	
	}
	
    private void crearSeccionOperacionesTerminoAdmCartera(Connection con, Long IdEmpresa, List<DataOperacionesTermino> operacionesTermino, Sheet hoja) throws Exception {
		
		Row fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(0);
		
		// encabezado de seccion
		String[] encabezado = {"UI_DOCUMENTO_IDENTIDAD", "UI_NOMBRE", "UI_CODIGO_PORTAFOLIO", "UI_NEMOTENICO", "UI_FECHA_COMPRA", "UI_FECHA_VENCIMIENTO", "UI_EMISOR", "UI_REAJUSTABILIDAD", 
							   "UI_PRECIO", "UI_MONTO_PAGAR", "UI_VALOR_RESCATE", "UI_MONEDA_PAGO", "UI_TIPO_CUSTODIA"};
		
		/************************************/
		/** DETALLE DE SECCION			   **/
		/************************************/
		--filaActual;
		Double montoTotal = 0d;
		String agruparSeccion = "";
		int columnaTotal = ArrayUtils.indexOf(encabezado, "UI_MONTO_PAGAR");
		
		for (DataOperacionesTermino dataOperacionesTermino : operacionesTermino) {
			columnaActual = 0;
			
			// establecer header de seccion
			// agrupar por Tipo de Cuenta (Propia/Tercero) y administrador de cartera (Si/No)
			if (!dataOperacionesTermino.AgruparSeccion.equals(agruparSeccion)) {
				
				/*************************************/
				/** Encabezado de seccion por grupo **/
				/*************************************/
				if (!agruparSeccion.equals("")) {
					// establece el total del monto a pagar de la seccion
					fila = hoja.createRow(++filaActual);
					establecerTotalColumna(hoja, celda, fila, montoTotal, columnaTotal, 0);
					montoTotal = 0d;
				}
				
				// subtitulos y encabezado de la siguiente seccion
				fila = hoja.createRow(++filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloFondo, "", 0);	// agregar linea en blanco
				fila = hoja.createRow(filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, "UI_VENCIMIENTO_INSTRUMENTOS").msg(), columnaActual);
				fila = hoja.createRow(filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, "UI_CUENTA_TERCEROS").msg(), columnaActual);
				fila = hoja.createRow(filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, dataOperacionesTermino.EsAdministracionCartera.equalsIgnoreCase("S") ? "UI_ADMINISTRACION_CARTERA" : "UI_SIN_ADMINISTRACION_CARTERA").msg(), columnaActual);			
				fila = hoja.createRow(filaActual);
				
				// define subtitulo si es adminsitrador de cartera para cuentas a terceros
				//establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, dataOperacionesTermino.EsAdministradorCartera.equalsIgnoreCase("S") ? "UI_CLIENTES_CON_ADMINISTRACION_CARTERA" : "UI_CLIENTES_SIN_ADMINISTRACION_CARTERA").msg(), columnaActual);
				//fila = hoja.createRow(filaActual);
				crearEncabezadoSeccion(con, IdEmpresa, encabezado, celda, fila);
				agruparSeccion = dataOperacionesTermino.AgruparSeccion;
			}
			
			fila = hoja.createRow(++filaActual);
			
			celda = fila.createCell(columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataOperacionesTermino.DocumentoIdentidad);
			
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataOperacionesTermino.NombreCompleto);
			
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloFondo);//estiloTextoIzq);
			celda.setCellValue(dataOperacionesTermino.CodigoPortafolio);
			//columna 0 (A)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataOperacionesTermino.Nemotecnico);
			
			//columna 1 (B)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataOperacionesTermino.FechaCompra);
			
			//columna 2 (C)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataOperacionesTermino.FechaVencimiento);

			//columna 3 (D)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataOperacionesTermino.IdEmisor);
			
			//columna 4 (E)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);//estiloTextoIzq);
			celda.setCellValue(dataOperacionesTermino.IdReajuste);
			
			//columna 5 (F)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellStyle(estiloNumero);
			celda.setCellValue(dataOperacionesTermino.Precio.doubleValue());
			
			//columna 6 (G)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellStyle(estiloNumero);
			celda.setCellValue(dataOperacionesTermino.Monto.doubleValue());
			montoTotal += dataOperacionesTermino.Monto.doubleValue();
			
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellStyle(estiloNumero);
			celda.setCellValue(dataOperacionesTermino.ValorRescate.doubleValue());
			
			//columna 7 (H)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataOperacionesTermino.IdMonedaPago);
			
			//columna 8 (I)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataOperacionesTermino.IdBoveda);			
			
			//columna 9 (J)		-- En blanco para establecer el margen derecho del informe
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloFondo);
			celda.setCellValue("");
		}
		
		// total monto total a pagar o recibir del ultimo grupo
		fila = hoja.createRow(++filaActual);
		establecerTotalColumna(hoja, celda, fila, montoTotal, columnaTotal, 0);
		
		++filaActual;
		fila = hoja.createRow(filaActual);
		// agregar linea en blanco para crear margen inferior en la hoja de Excel (aplica solo en ultima secion)
		establecerTituloEnFila(hoja, celda, fila, estiloFondo, "", 0);	
	}	

    private void crearSeccionOperacionesTerminoSinAdmCartera(Connection con, Long IdEmpresa, List<DataOperacionesTermino> operacionesTermino, Sheet hoja) throws Exception {
		
		Row fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(0);
		
		// encabezado de seccion
		String[] encabezado = {"UI_DOCUMENTO_IDENTIDAD", "UI_NOMBRE", "UI_CODIGO_PORTAFOLIO", "UI_NEMOTENICO", "UI_FECHA_COMPRA", "UI_FECHA_VENCIMIENTO", "UI_EMISOR", "UI_REAJUSTABILIDAD", 
				   "UI_PRECIO", "UI_MONTO_PAGAR", "UI_VALOR_RESCATE", "UI_MONEDA_PAGO", "UI_TIPO_CUSTODIA"};
		
		/************************************/
		/** DETALLE DE SECCION			   **/
		/************************************/
		--filaActual;
		Double montoTotal = 0d;
		String agruparSeccion = "";
		int columnaTotal = ArrayUtils.indexOf(encabezado, "UI_MONTO_PAGAR");
		
		for (DataOperacionesTermino dataOperacionesTermino : operacionesTermino) {
			columnaActual = 0;
			
			// establecer header de seccion
			// agrupar por Tipo de Cuenta (Propia/Tercero) y administrador de cartera (Si/No)
			if (!dataOperacionesTermino.AgruparSeccion.equals(agruparSeccion)) {
				
				/*************************************/
				/** Encabezado de seccion por grupo **/
				/*************************************/
				if (!agruparSeccion.equals("")) {
					// establece el total del monto a pagar de la seccion
					fila = hoja.createRow(++filaActual);
					establecerTotalColumna(hoja, celda, fila, montoTotal, columnaTotal, 0);
					montoTotal = 0d;
				}
				
				// subtitulos y encabezado de la siguiente seccion
				fila = hoja.createRow(++filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloFondo, "", 0);	// agregar linea en blanco
				fila = hoja.createRow(filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, "UI_VENCIMIENTO_INSTRUMENTOS").msg(), columnaActual);
				fila = hoja.createRow(filaActual);
				establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, "UI_CUENTA_TERCEROS").msg(), columnaActual);
				fila = hoja.createRow(filaActual);
				
				establecerTituloEnFila(hoja, celda, fila, estiloSubTituloSeccion, Diccionario.getMensaje(con, IdEmpresa, dataOperacionesTermino.EsAdministracionCartera.equalsIgnoreCase("S") ? "UI_ADMINISTRACION_CARTERA" : "UI_SIN_ADMINISTRACION_CARTERA").msg(), columnaActual);			
				fila = hoja.createRow(filaActual);
				
				crearEncabezadoSeccion(con, IdEmpresa, encabezado, celda, fila);
				agruparSeccion = dataOperacionesTermino.AgruparSeccion;
			}
			
			fila = hoja.createRow(++filaActual);

			celda = fila.createCell(columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataOperacionesTermino.DocumentoIdentidad);
			
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataOperacionesTermino.NombreCompleto);
			
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataOperacionesTermino.CodigoPortafolio);
			
			//columna 0 (A)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataOperacionesTermino.Nemotecnico);
			
			//columna 1 (B)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataOperacionesTermino.FechaCompra);
			
			//columna 2 (C)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataOperacionesTermino.FechaVencimiento);

			//columna 3 (D)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataOperacionesTermino.IdEmisor);
			
			//columna 4 (E)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzq);
			celda.setCellValue(dataOperacionesTermino.IdReajuste);
			
			//columna 5 (F)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellStyle(estiloNumero);
			celda.setCellValue(dataOperacionesTermino.Precio.doubleValue());
			
			//columna 6 (G)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellStyle(estiloNumero);
			celda.setCellValue(dataOperacionesTermino.Monto.doubleValue());
			montoTotal += dataOperacionesTermino.Monto.doubleValue();
			
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellStyle(estiloNumero);
			celda.setCellValue(dataOperacionesTermino.ValorRescate.doubleValue());
			
			//columna 7 (H)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataOperacionesTermino.IdMonedaPago);
			
			//columna 8 (I)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCen);
			celda.setCellValue(dataOperacionesTermino.IdBoveda);			
			
			//columna 9 (J)		-- En blanco para establecer el margen derecho del informe
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloFondo);
			celda.setCellValue("");
		}
		
		// total monto total a pagar o recibir del ultimo grupo
		fila = hoja.createRow(++filaActual);
		establecerTotalColumna(hoja, celda, fila, montoTotal, columnaTotal, 0);
		
		++filaActual;
		fila = hoja.createRow(filaActual);
		// agregar linea en blanco para crear margen inferior en la hoja de Excel (aplica solo en ultima secion)
		establecerTituloEnFila(hoja, celda, fila, estiloFondo, "", 0);	
	}	

	private void fijarAnchoColumnasHojaExcel(Sheet hoja) {
		hoja.setColumnWidth(0, 4300);//4000);
		hoja.setColumnWidth(1, 12000);
		hoja.setColumnWidth(2, 4500);//4200);
		hoja.setColumnWidth(3, 4000);
		hoja.setColumnWidth(4, 4200);
		hoja.setColumnWidth(5, 4500);
		hoja.setColumnWidth(6, 6000);
		hoja.setColumnWidth(7, 4500);
		hoja.setColumnWidth(8, 4500);
		hoja.setColumnWidth(9, 4500);
		hoja.setColumnWidth(10, 4500);
		hoja.setColumnWidth(11, 4500);
	}
	
	private void fijarEstilos(Workbook archivoExcel) {
		estiloTituloEncabezado = obtenerEstiloCelda(EstiloCelda.TITULO, archivoExcel);
		estiloSubTituloSeccion = obtenerEstiloCelda(EstiloCelda.SUBTITULO, archivoExcel);
		estiloTituloCampo = obtenerEstiloCelda(EstiloCelda.ENCABEZADO, archivoExcel);
		estiloTotalCampo = obtenerEstiloCelda(EstiloCelda.TOTALES, archivoExcel);
		estiloTextoIzq = obtenerEstiloCelda(EstiloCelda.TEXTO_IZQUIERDA, archivoExcel);	
		estiloTextoCen = obtenerEstiloCelda(EstiloCelda.TEXTO_CENTRO, archivoExcel);
		estiloNumero = obtenerEstiloCelda(EstiloCelda.NUMERO, archivoExcel);
		estiloFondo = obtenerEstiloCelda(EstiloCelda.FONDO, archivoExcel);
	}
	
	private void establecerTituloEnFila(Sheet hoja, Cell celda, Row fila, CellStyle estiloTitulo, String valor, Integer columnaTitulo) {
		// titulos y subtitulos 
		// rellena en fondo blanco el resto de las celdas de la fila
		for (Integer j=0; j<CANTIDAD_COLUMNAS_EN_HOJA+1; j++) {
			celda = fila.createCell(j);
			if (columnaTitulo == j) {
				celda.setCellValue(valor);
				celda.setCellStyle(estiloTitulo);
					if ( valor.length()>0) {
						TitulosSecundario.add(valor);
					}
				} else {
				celda.setCellStyle(estiloFondo);
			}
		}
		++filaActual;
	}
	
	private void establecerTotalColumna(Sheet hoja, Cell celda, Row fila, double valor, Integer columnaTotal, Integer inicioColumna) {
		// total de la columna 
		// rellena en fondo blanco el resto de las celdas de la fila
		// inicioColumna: Ayuda a identificar la primera columna de Total para establecer la etiqueta Total: a su izquierda
		
		for (Integer j=inicioColumna; j<CANTIDAD_COLUMNAS_EN_HOJA+1; j++) {
			celda = fila.createCell(j);
			if (j == columnaTotal) {
				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
				celda.setCellValue(valor);
				celda.setCellStyle(estiloTotalCampo);
				
			} else if (j == columnaTotal-1 && inicioColumna == 0) {
				CellStyle totalStyle = estiloFondo;
				totalStyle.setAlignment(CellStyle.ALIGN_RIGHT);
				celda.setCellValue("Total:");
				celda.setCellStyle(totalStyle);
				
			} else {
				celda.setCellStyle(estiloFondo);
			}
		}
	}	
	
	private void crearEncabezadoSeccion(Connection con, Long IdEmpresa, String[] encabezado, Cell celda, Row fila) throws Exception {
		// titulos de encabezado de seccion
		int columnaCelda = 0;
		for (int i=0; i<CANTIDAD_COLUMNAS_EN_HOJA; i++) {
			celda = fila.createCell(columnaCelda);
			
			if (i<encabezado.length && !encabezado[i].equals("")) {
				celda.setCellStyle(estiloTituloCampo);
				celda.setCellValue(Diccionario.getMensaje(con, IdEmpresa, encabezado[i]).msg());
			} else {
				celda.setCellStyle(estiloFondo);
			}
			++columnaCelda;
		}	
	}
	
	private void completarColumnasEnBlanco(Row fila) {
		// Rellena columnas en blanco cuando la secion tiene menos
		// columnas de las establecidas en la hoja de Excel (CANTIDAD_COLUMNAS_EN_HOJA)
		
		for (int i=columnaActual; i<CANTIDAD_COLUMNAS_EN_HOJA;i++) {
			Cell celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloFondo);
			celda.setCellValue("");
		}
	}
	
	private CellStyle obtenerEstiloCelda (EstiloCelda estilo,  Workbook archivoExcel) {
		
		CellStyle estiloCelda = archivoExcel.createCellStyle();
		Font fuenteCelda = archivoExcel.createFont();
		DataFormat df = archivoExcel.createDataFormat();
		switch (estilo) {
			case TITULO:
				// estilo de titulo de informe
				fuenteCelda.setBold(true);
				fuenteCelda.setFontHeightInPoints((short) 16);
				estiloCelda.setFont(fuenteCelda);
				estiloCelda.setAlignment(CellStyle.ALIGN_CENTER_SELECTION);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				break;
				
			case SUBTITULO:
				// estilo de subtitulo de seccion
				fuenteCelda.setBold(true);
				estiloCelda.setFont(fuenteCelda);
				estiloCelda.setAlignment(CellStyle.ALIGN_LEFT);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());				
				break;
				
			case ENCABEZADO:
				// estilo de encabezados de seccion
				estiloCelda.setAlignment(CellStyle.ALIGN_CENTER);
				estiloCelda.setBorderBottom(CellStyle.BORDER_THIN);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				break;
				
			case TOTALES:
				// estilo de totales y subtotales de montos
				estiloCelda.setBorderTop(CellStyle.BORDER_DOUBLE);
				df = archivoExcel.createDataFormat();
				estiloCelda.setDataFormat(df.getFormat("#,##0.00"));
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				break;
				
			case TEXTO_IZQUIERDA:
				// estilo de texto en cuero de la seccion
				fuenteCelda.setBold(false);
				estiloCelda.setFont(fuenteCelda);
				estiloCelda.setAlignment(CellStyle.ALIGN_LEFT);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());		
				break;
				
			case TEXTO_CENTRO:
				// estilo de texto en cuero de la seccion
				fuenteCelda.setBold(false);
				estiloCelda.setFont(fuenteCelda);
				estiloCelda.setAlignment(CellStyle.ALIGN_CENTER);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());		
				break;
				
			case NUMERO:
				// estilo de celdas numericas
				df = archivoExcel.createDataFormat();
				estiloCelda.setDataFormat(df.getFormat("#,##0.00"));
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				break;

			case FONDO:
				// estilo de fondo pra las celdas en blanco
				estiloCelda.setFont(fuenteCelda);
				estiloCelda.setAlignment(CellStyle.ALIGN_LEFT);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);				
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				break;
		}

		return estiloCelda;
	}
	
	private void generaExpotacioPdf( Workbook archivoExcel,  String rutaDestino, SalidaGenerarInformeVencimientosGenerales salidaErrores) {
		// formato numeros
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator(',');
		symbols.setGroupingSeparator('.');
		DecimalFormat formatoPesos = new DecimalFormat("#,##0.00", symbols);

	    try {
	        // ===== PASAR EXCEL A MEMORIA =====
	        ByteArrayOutputStream excelOut = new ByteArrayOutputStream();
	        archivoExcel.write(excelOut);
	        archivoExcel.close();
	        Boolean formatoCelda= true;

	        byte[] excelBytes = excelOut.toByteArray();
	        ByteArrayInputStream excelIn = new ByteArrayInputStream(excelBytes);

	        Workbook wb2 = new HSSFWorkbook(excelIn);
	        Sheet sheet2 = wb2.getSheetAt(0);

	        // ===== CONFIGURACION COLUMNAS =====
	        List<PdfColumnConfig> columnas = new ArrayList<>();
	        columnas.add(new PdfColumnConfig(new BigDecimal("60"),  ALIGN_CENTER,   new BigDecimal("8")));
	        columnas.add(new PdfColumnConfig(new BigDecimal("250"), ALIGN_LEFT,   new BigDecimal("8")));
	        columnas.add(new PdfColumnConfig(new BigDecimal("70"), ALIGN_LEFT, new BigDecimal("8")));
	        columnas.add(new PdfColumnConfig(new BigDecimal("80"), ALIGN_RIGHT,  new BigDecimal("8"), formatoPesos));
	        columnas.add(new PdfColumnConfig(new BigDecimal("80"), ALIGN_CENTER,  new BigDecimal("8"), formatoPesos));
	        columnas.add(new PdfColumnConfig(new BigDecimal("80"), ALIGN_RIGHT,  new BigDecimal("8"), formatoPesos));
	        columnas.add(new PdfColumnConfig(new BigDecimal("140"), ALIGN_RIGHT,   new BigDecimal("8"), formatoPesos));
	        columnas.add(new PdfColumnConfig(new BigDecimal("100"), ALIGN_CENTER, new BigDecimal("8"), formatoPesos));
	        columnas.add(new PdfColumnConfig(new BigDecimal("100"), ALIGN_CENTER, new BigDecimal("8"), formatoPesos));
	        columnas.add(new PdfColumnConfig(new BigDecimal("60"), ALIGN_RIGHT, new BigDecimal("8"), formatoPesos));
	        columnas.add(new PdfColumnConfig(new BigDecimal("60"), ALIGN_CENTER, new BigDecimal("8"), formatoPesos));
	        columnas.add(new PdfColumnConfig(new BigDecimal("60"), ALIGN_CENTER, new BigDecimal("8"), formatoPesos));

	        // ===== CREAR PDF =====
	        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
	        PDDocument document = new PDDocument();
	        PDRectangle landscape = new PDRectangle(PDRectangle.TABLOID.getHeight(), PDRectangle.TABLOID.getWidth());
	        PDPage page = new PDPage(landscape);
	        document.addPage(page);
	        PDPageContentStream content = new PDPageContentStream(document, page);

	        BigDecimal pageHeight = new BigDecimal(landscape.getHeight());
	        BigDecimal margin = new BigDecimal("30");
	        BigDecimal y = pageHeight.subtract(new BigDecimal("40"));
	        DataFormatter formatter = new DataFormatter();	        
	        
	        // ===== ANCHO TOTAL TABLA =====
	        BigDecimal anchoTotalTabla = BigDecimal.ZERO;
	        for (PdfColumnConfig c : columnas) {
	            anchoTotalTabla = anchoTotalTabla.add(c.width);
	        }
	        
	        // ===== RECORRER FILAS =====
	        Integer contadorLineas =0;	        
	        for (Row row : sheet2) {

	            BigDecimal maxRowHeight = new BigDecimal("8");	 
	            List<CellPdf> rowCells = new ArrayList<>();
	            int totalCols = Math.min(row.getLastCellNum(), columnas.size());	            
	            boolean lineaHorizontal= true;
	            boolean esFilaTotal = false;

	            // ===== PREPROCESAR FILA =====
	            for (int c = 0; c < totalCols; c++) {
	                PdfColumnConfig col = columnas.get(c);
	                Cell cell = row.getCell(c);
                    String text = "";                 
                    
                    if (cell != null) {
                        int cellType = cell.getCellType();
                        if (col.numberFormat != null &&
                           (cellType == Cell.CELL_TYPE_NUMERIC || cellType == Cell.CELL_TYPE_FORMULA)) {
                            try {
                                double val = cell.getNumericCellValue();
                                text = col.numberFormat.format(val);
                            } catch (Exception ex) {
                                text = formatter.formatCellValue(cell);
                            }

                        } else {
                            text = formatter.formatCellValue(cell);
                        }

                        if (text != null && text.toUpperCase().contains("TOTAL")) {
                            esFilaTotal = true;
                        }
                    }

	                PDFont cellFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
	                BigDecimal cellFontSize = col.fontSize;
	                int align = col.align;
	                
	                if (cell != null && TituloPrincipal.equalsIgnoreCase(text)) {
	                    cellFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
	                    cellFontSize = new BigDecimal("16");
	                    align = ALIGN_RIGHT;
	                    formatoCelda =false;   
	                    lineaHorizontal=false;
	                    contadorLineas =10;                  
	                }else{                	
	                	if ( TitulosSecundario.contains(text)) {
	                		cellFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
		                    cellFontSize = new BigDecimal("10");
		                    align = ALIGN_LEFT;
		                    formatoCelda =false; 
		                    lineaHorizontal=false;
		                    contadorLineas =0;	           
	                	}else {
	                		if (contadorLineas == 1) {
	                			formatoCelda =false;
	                		}else {
	                			formatoCelda = true; 
	                		}	 	                  		 
	                	}    
	                };
	                
	                CellPdf cellPdf = new CellPdf();
	                cellPdf.font = cellFont;
	                cellPdf.fontSize = cellFontSize;
	                cellPdf.aligneacion = align;
                    
	                if (formatoCelda) {
                    	cellPdf.lines = pdfWrapText(text,cellFont, cellFontSize.floatValue(), col.width.subtract(new BigDecimal("4")).floatValue());
                     }else {
                    	    List<String> one = new ArrayList<>();
                    	    one.add(text);
                    	    cellPdf.lines = one; 
                     }
	                rowCells.add(cellPdf);
	                BigDecimal neededHeight = new BigDecimal(cellPdf.lines.size()).multiply(cellFontSize.add(new BigDecimal("4")));
	               
	                if (neededHeight.compareTo(maxRowHeight) > 0) {
	                    maxRowHeight = neededHeight;
	                }
	            }

	            // ===== SALTO DE PAGINA =====
	            if (y.subtract(maxRowHeight).compareTo(new BigDecimal("40")) < 0) {
	                content.close();
	                page = new PDPage(landscape);
	                document.addPage(page);
	                content = new PDPageContentStream(document, page);
	                y = pageHeight.subtract(new BigDecimal("40"));
	            }
	            // ===== LINEA SOBRE TOTAL =====
	            if (esFilaTotal) {
	                float yLinea = y.floatValue();
	                content.setLineWidth(1f);
	                content.moveTo(margin.floatValue(), yLinea + 1);
	                content.lineTo(margin.add(anchoTotalTabla).floatValue(), yLinea +1);
	                content.stroke();
	                content.moveTo(margin.floatValue(), yLinea -2);
	                content.lineTo(margin.add(anchoTotalTabla).floatValue(), yLinea -2);
	                content.stroke();
	                y = y.subtract(new BigDecimal("5"));
	            }
	            // ===== DIBUJAR FILA =====
	            BigDecimal cellX = margin;
	            for (int c = 0; c < rowCells.size(); c++) {
	                PdfColumnConfig col = columnas.get(c);
	                CellPdf cellPdf = rowCells.get(c);
	                BigDecimal textY = y.subtract(cellPdf.fontSize);
	                for (String line : cellPdf.lines) {
	                    float textWidth = cellPdf.font.getStringWidth(line) / 1000f * cellPdf.fontSize.floatValue();
	                    float startX;
	                    if (cellPdf.aligneacion == ALIGN_RIGHT) {
	                        startX = cellX.add(col.width).floatValue() - textWidth - 2;
	                    } else if (cellPdf.aligneacion == ALIGN_CENTER) {
	                        startX = cellX.add(col.width).floatValue() - (col.width.floatValue() / 2) - (textWidth / 2);
	                    } else {
	                        startX = cellX.floatValue() + 2;
	                    }
	                    content.beginText();
	                    content.setFont(cellPdf.font, cellPdf.fontSize.floatValue());
	                    content.newLineAtOffset(startX, textY.floatValue());
	                    content.showText(line);
	                    content.endText();
	                    textY = textY.subtract(cellPdf.fontSize.add(new BigDecimal("4")));
	                }
	                cellX = cellX.add(col.width);
	            }
	            y = y.subtract(maxRowHeight);            
	            ++contadorLineas;
	            // ===== LINEA BAJO ENCABEZADO =====
         		 if (lineaHorizontal &&contadorLineas ==2) {
          			 float yLinea = y.floatValue();
          			 content.setLineWidth(1f);
          			 content.moveTo(margin.floatValue(), yLinea);
          			 content.lineTo(margin.add(anchoTotalTabla).floatValue(), yLinea);
          			 content.stroke();
          			 lineaHorizontal= false;
          		 } 
	        }

	        content.close();
	        document.save(pdfOut);
	        document.close();

	        byte[] pdfBytes = pdfOut.toByteArray();
	        // Descomentar para hacer debug
	        /*
	        FileOutputStream fos = new FileOutputStream(rutaDestino.replace(".xls", ".pdf"));
	        fos.write(pdfBytes);
	        fos.close();
            */
	        guardarZip(excelBytes, pdfBytes, rutaDestino,salidaErrores.p_out_NombreArchivo );
	        
	    } catch (Exception e) {
	        salidaErrores.p_out_Comentario = e.getMessage();
	        e.printStackTrace();
	    }
	}

	private List<String> pdfWrapText(String text, PDFont font, float fontSize, float maxWidth)  {
		List<String> lines = new ArrayList<>();
		try {
		
	    
	    if (text == null || text.isEmpty()) return lines;
	    String[] words = text.split(" ");
	    StringBuilder line = new StringBuilder();
	    
	    for (String word : words) {
	        String testLine = line.length() == 0 ? word : line + " " + word;
	        float size = font.getStringWidth(testLine) / 1000 * fontSize;
	        if (size > maxWidth) {
	            lines.add(line.toString());
	            line = new StringBuilder(word);
	        } else {
	            line = new StringBuilder(testLine);
	        }
	    }

	    if (line.length() > 0) {
	        lines.add(line.toString());
	    }
	   }catch (Exception e) {
	        e.printStackTrace();
	}

	    return lines;
	}

    private void guardarZip(
    		byte[] excelBytes,
    		byte[] pdfBytes,
    		String rutaZip,
    		String NombreArchivoXls) throws IOException {
        logger.debug("INICIO::GenerarInformeVencimientosGenerales.guardarZip");
        long startTime = System.currentTimeMillis();

        ByteArrayOutputStream zipOut = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipOut)) {

            ZipEntry excelEntry = new ZipEntry(NombreArchivoXls);
            zos.putNextEntry(excelEntry);
            zos.write(excelBytes);
            zos.closeEntry();

            ZipEntry pdfEntry = new ZipEntry(NombreArchivoXls.replace(".xls", ".pdf"));
            zos.putNextEntry(pdfEntry);
            zos.write(pdfBytes);
            zos.closeEntry();
        }
        Files.write(Paths.get(rutaZip.replace(".xls", ".zip")), zipOut.toByteArray());

        long endTime = System.currentTimeMillis();
        logger.debug("FIN::GenerarInformeVencimientosGenerales.guardarZip");
    }

	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {

		logger.info("=============== Inicio Llamada GenerarInformeVencimientosGenerales ===============");
		Gson gson = Utils.getGsonBuilder();
		EntradaGenerarInformeVencimientosGenerales in = gson.fromJson(jin.toString(), EntradaGenerarInformeVencimientosGenerales.class);
		SalidaGenerarInformeVencimientosGenerales out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		logger.info("=============== Fin Llamada GenerarInformeVencimientosGenerales ===============");
		
		return dataout;
	}
	
	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {

			EntradaGenerarInformeVencimientosGenerales entrada = new EntradaGenerarInformeVencimientosGenerales();
			entrada.p_IdEmpresa = new Long(1);
			entrada.p_FechaSistema = "2020-06-05";
			entrada.p_Ruta = "C:\\BCS";
			entrada.p_FechaInforme = "2018-12-01";//"2018-12-01"; //"2020-08-20";//"2018-12-01"; // "2020-08-20"; // "2019-06-18"
			JsonParser parser = new JsonParser();
			JsonObject dataJsonIn = parser.parse(Utils.getGsonBuilder().toJson(entrada)).getAsJsonObject();
			GenerarInformeVencimientosGenerales demo = new GenerarInformeVencimientosGenerales();
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
