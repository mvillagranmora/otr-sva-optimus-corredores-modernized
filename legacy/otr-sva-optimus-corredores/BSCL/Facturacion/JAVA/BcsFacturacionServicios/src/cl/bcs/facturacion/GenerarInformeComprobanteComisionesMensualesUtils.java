package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import cl.bcs.contabilidad.GenerarInformeClienteRelacionado.Imagen;
import cl.bcs.facturacion.GenerarInformesComprobanteComisionesMensuales.EntradaGenerarInformesComprobanteComisionesMensuales;
import cl.bcs.facturacion.pojo.Cobro;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInformeComprobanteComisionesMensualesUtils {
	
	private final static Logger logger = LogManager.getLogger(GenerarInformeComprobanteComisionesMensualesUtils.class);
	
	private static FileLoader loader;
	private static String SQL_SELECT_PORCENTAJE_IMPUESTO;
	
	static {
		loader = new FileLoader();
		SQL_SELECT_PORCENTAJE_IMPUESTO = loader.loadResourceToString("cl/bcs/facturacion/sql/GenerarInformeComprobanteComisionesMensuales_SelectPorcentajeImpuesto.sql");
	}
	
	private static String SQL_SELECT_LOGO_CORREDORA = "SELECT TOP 1 Imagen.\"Binario\" AS \"Contenido\" FROM \"BSCL.Plataforma.BD::PLA.Imagenes\" Imagen WHERE Imagen.\"NombreImagen\" = 'LOGO_CORREDOR' AND Imagen.\"IdEmpresa\" = :IdEmpresa ORDER BY Imagen.\"Version\" DESC;";  

	
	public static final Integer CANTIDAD_COLUMNAS_EN_HOJA = 13;
	
	static CellStyle estiloCeldaSinBordes;
	static CellStyle estiloPorcentaje;
	static CellStyle estiloTituloEncabezado;
	static CellStyle estiloSubTituloSeccion;
	static CellStyle estiloTextoCentro;
	static CellStyle estiloTextoConSaltoLinea;
	static CellStyle estiloTextoDerecha;
	static CellStyle estiloTextoIzquierda;
	static CellStyle estiloTotalCampo;
	static CellStyle estiloNumero;
	
	public static enum EstiloCelda {
		TEXTO_DERECHA,
		TEXTO_IZQUIERDA,
		TEXTO_CENTRO,
		TEXTO_SALTO_LINEA,
		SUBTITULO,
		TOTALES,
		TITULO,
		NUMERO,
		CELDA_SIN_BORDES,
		PORCENTAJE
	}
	
	public static class Imagen extends BcsPojo {
		public byte[] Contenido;

		public Imagen() {
			Contenido = new byte[0];
		}
	}
	
	public static void fijarAnchoColumnasHojaExcelPorDefecto(Sheet hoja) {
		hoja.setColumnWidth(0, 4300);
		hoja.setColumnWidth(1, 4000);
		hoja.setColumnWidth(2, 12000);;
		hoja.setColumnWidth(3, 6000);
		hoja.setColumnWidth(4, 4200);
		hoja.setColumnWidth(5, 6000);
		hoja.setColumnWidth(6, 8000);
		hoja.setColumnWidth(7, 4500);
		hoja.setColumnWidth(8, 4500);
		hoja.setColumnWidth(9, 4500);
		hoja.setColumnWidth(10, 4500);
		hoja.setColumnWidth(11, 4500);
		hoja.setColumnWidth(12, 4500);
	}
	
	public static void fijarAnchoColumnasInformeComisionDetallado(Sheet hoja) {
		hoja.setColumnWidth(0, 4300);
		hoja.setColumnWidth(1, 4000);
		hoja.setColumnWidth(2, 4000);;
		hoja.setColumnWidth(3, 5000);
		hoja.setColumnWidth(4, 4200);
		hoja.setColumnWidth(5, 4500);
		hoja.setColumnWidth(6, 4500);
		hoja.setColumnWidth(7, 4500);
		hoja.setColumnWidth(8, 4500);
		hoja.setColumnWidth(9, 4500);
		hoja.setColumnWidth(10, 4500);
		hoja.setColumnWidth(11, 4500);
		hoja.setColumnWidth(12, 4500);
	}
	
	public static void fijarEstilos(Workbook archivoExcel) {
		estiloTituloEncabezado 		= obtenerEstiloCelda(EstiloCelda.TITULO, archivoExcel);
		estiloSubTituloSeccion 		= obtenerEstiloCelda(EstiloCelda.SUBTITULO, archivoExcel);
		estiloTotalCampo 			= obtenerEstiloCelda(EstiloCelda.TOTALES, archivoExcel);
		estiloTextoCentro 			= obtenerEstiloCelda(EstiloCelda.TEXTO_CENTRO, archivoExcel);
		estiloTextoConSaltoLinea 	= obtenerEstiloCelda(EstiloCelda.TEXTO_SALTO_LINEA, archivoExcel);
		estiloTextoDerecha 			= obtenerEstiloCelda(EstiloCelda.TEXTO_DERECHA, archivoExcel);
		estiloTextoIzquierda		= obtenerEstiloCelda(EstiloCelda.TEXTO_IZQUIERDA, archivoExcel);
		estiloNumero 				= obtenerEstiloCelda(EstiloCelda.NUMERO, archivoExcel);
		estiloCeldaSinBordes 		= obtenerEstiloCelda(EstiloCelda.CELDA_SIN_BORDES, archivoExcel);
		estiloPorcentaje 			= obtenerEstiloCelda(EstiloCelda.PORCENTAJE, archivoExcel);
	}
	
	public static void establecerTituloEnFila(Sheet hoja, Cell celda, Row fila, CellStyle estiloTitulo, String valor, Integer columnaTitulo) {
		
		for (Integer j=0; j<CANTIDAD_COLUMNAS_EN_HOJA; j++) {
			celda = fila.createCell(j);
			if (columnaTitulo == j) {
				celda.setCellValue(valor);
				celda.setCellStyle(estiloTitulo);
			} else {
				celda.setCellStyle(estiloCeldaSinBordes);
			}
		}
	}
	
	
	public static void establecerTotalColumna(Sheet hoja, Cell celda, Row fila, BigDecimal valor, Integer columnaTotal, Integer inicioColumna) {
		// inicioColumna: Ayuda a identificar la primera columna de Total para establecer la etiqueta Total: a su izquierda
		
		for (Integer j=inicioColumna; j<CANTIDAD_COLUMNAS_EN_HOJA; j++) {
			celda = fila.createCell(j);
			if (j == columnaTotal) {
				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
				celda.setCellValue(valor.doubleValue());
				celda.setCellStyle(estiloTotalCampo);
				
			} else if (j == columnaTotal-1 && inicioColumna == 0) {
				celda.setCellValue("TOTAL GENERAL");
				celda.setCellStyle(estiloTextoCentro);
				
			} else {
				celda.setCellStyle(estiloCeldaSinBordes);
			}
		}
	}
	
	
	public static void crearEncabezadoSeccion(Connection con, Long IdEmpresa, String[] encabezado, Cell celda, Row fila) throws Exception {
		
		int columnaCelda = 0;
		for (int i=0; i<CANTIDAD_COLUMNAS_EN_HOJA; i++) {
			celda = fila.createCell(columnaCelda);
			
			if (i<encabezado.length && !encabezado[i].equals("")) {
				celda.setCellStyle(estiloTextoCentro);
				celda.setCellValue(Diccionario.getMensaje(con, IdEmpresa, encabezado[i]).msg());
			} else {
				celda.setCellStyle(estiloCeldaSinBordes);
			}
			++columnaCelda;
		}	
	}
	
	
	public static void completarColumnasEnBlanco(Row fila, Integer columnaActual) {
		for (Integer i=columnaActual; i<CANTIDAD_COLUMNAS_EN_HOJA - 1;i++) {
			Cell celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloCeldaSinBordes);
			celda.setCellValue("");
		}
	}
	
	public static void crearFilaMsgPersonalizado(Sheet hoja, Cell celda, Row fila, String mensaje, Integer columnaTitulo, Integer filaActual) throws Exception {
		fila = hoja.createRow(++filaActual);
		for (Integer j=0; j<CANTIDAD_COLUMNAS_EN_HOJA; j++) {
			celda = fila.createCell(j);
			if (columnaTitulo == j) {
				celda.setCellValue(mensaje);
				celda.setCellStyle(estiloSubTituloSeccion);
			} else {
				celda.setCellStyle(estiloCeldaSinBordes);
			}
		}
	}
	
	public static CellStyle obtenerEstiloCelda (EstiloCelda estilo,  Workbook archivoExcel) {
		
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
				
			case TOTALES:
				// estilo de totales y subtotales de montos
				estiloCelda.setBorderTop(CellStyle.BORDER_DOUBLE);
				df = archivoExcel.createDataFormat();
				estiloCelda.setDataFormat(df.getFormat("#,##0"));
				estiloCelda.setBorderTop(CellStyle.BORDER_THIN);
				estiloCelda.setBorderRight(CellStyle.BORDER_THIN);
				estiloCelda.setBorderBottom(CellStyle.BORDER_THIN);
				estiloCelda.setBorderLeft(CellStyle.BORDER_THIN);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				break;
			case TEXTO_DERECHA: 
				estiloCelda.setFont(fuenteCelda);
				estiloCelda.setAlignment(CellStyle.ALIGN_RIGHT);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setBorderTop(CellStyle.BORDER_THIN);
				estiloCelda.setBorderRight(CellStyle.BORDER_THIN);
				estiloCelda.setBorderBottom(CellStyle.BORDER_THIN);
				estiloCelda.setBorderLeft(CellStyle.BORDER_THIN);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());		
				break;
			case TEXTO_IZQUIERDA: 
				estiloCelda.setFont(fuenteCelda);
				estiloCelda.setAlignment(CellStyle.ALIGN_LEFT);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setBorderTop(CellStyle.BORDER_THIN);
				estiloCelda.setBorderRight(CellStyle.BORDER_THIN);
				estiloCelda.setBorderBottom(CellStyle.BORDER_THIN);
				estiloCelda.setBorderLeft(CellStyle.BORDER_THIN);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());		
				break;
			case TEXTO_CENTRO:
				fuenteCelda.setBold(false);
				estiloCelda.setFont(fuenteCelda);
				estiloCelda.setAlignment(CellStyle.ALIGN_CENTER);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setBorderTop(CellStyle.BORDER_THIN);
				estiloCelda.setBorderRight(CellStyle.BORDER_THIN);
				estiloCelda.setBorderBottom(CellStyle.BORDER_THIN);
				estiloCelda.setBorderLeft(CellStyle.BORDER_THIN);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());		
				break;
				
			case TEXTO_SALTO_LINEA:
				estiloCelda.setFont(fuenteCelda);
				estiloCelda.setWrapText(true);
				estiloCelda.setAlignment(CellStyle.ALIGN_CENTER);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setBorderTop(CellStyle.BORDER_THIN);
				estiloCelda.setBorderRight(CellStyle.BORDER_THIN);
				estiloCelda.setBorderBottom(CellStyle.BORDER_THIN);
				estiloCelda.setBorderLeft(CellStyle.BORDER_THIN);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());		
				break;
				
			case NUMERO:
				// estilo de celdas numericas
				df = archivoExcel.createDataFormat();
				estiloCelda.setDataFormat(df.getFormat("#,##0"));
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setBorderTop(CellStyle.BORDER_THIN);
				estiloCelda.setBorderRight(CellStyle.BORDER_THIN);
				estiloCelda.setBorderBottom(CellStyle.BORDER_THIN);
				estiloCelda.setBorderLeft(CellStyle.BORDER_THIN);
				estiloCelda.setAlignment(CellStyle.ALIGN_RIGHT);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				break;
				
			case CELDA_SIN_BORDES:
				// estilo de fondo pra las celdas en blanco
				estiloCelda.setFont(fuenteCelda);
				estiloCelda.setAlignment(CellStyle.ALIGN_LEFT);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				break;
				
			case PORCENTAJE:
				// estilo de fondo pra las celdas en blanco
				df = archivoExcel.createDataFormat();
				estiloCelda.setDataFormat(df.getFormat("#,##0.##########"));
				estiloCelda.setAlignment(CellStyle.ALIGN_RIGHT);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setBorderTop(CellStyle.BORDER_THIN);
				estiloCelda.setBorderRight(CellStyle.BORDER_THIN);
				estiloCelda.setBorderBottom(CellStyle.BORDER_THIN);
				estiloCelda.setBorderLeft(CellStyle.BORDER_THIN);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				break;
		}

		return estiloCelda;
	}
	
	public static void establecerSeccionFiltros(Connection con, Sheet hoja, Cell celda, Row fila, EntradaGenerarInformesComprobanteComisionesMensuales entrada) throws Exception {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		String filtro1 	= Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_FECHA_DESDE").msg();
		Date fechaDesde = dateFormat.parse(entrada.p_FechaDesde);
		String valor1 	= new SimpleDateFormat("dd-MM-yyyy").format(fechaDesde);
		String filtro2 	= Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_FECHA_HASTA").msg();
		Date fechaHasta = dateFormat.parse(entrada.p_FechaHasta);
		String valor2 	= new SimpleDateFormat("dd-MM-yyyy").format(fechaHasta);
		
		Integer filaActual2 = fila.getRowNum();
		fila = hoja.createRow(filaActual2);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerFilaFiltros(hoja,celda,fila, filtro1, valor1, filtro2, valor2);
		fila = hoja.createRow(++filaActual2);
		filtro1 = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_RUT_CLIENTE").msg();
		valor1 	= entrada.p_RutCliente;
		filtro2 = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_PORTAFOLIO").msg();
		valor2	= entrada.p_CodigoPortafolio;
		GenerarInformeComprobanteComisionesMensualesUtils.establecerFilaFiltros(hoja,celda,fila, filtro1, valor1, filtro2, valor2);
		fila 	= hoja.createRow(++filaActual2);
		filtro1 = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_MERCADO").msg();
		valor1 	= entrada.p_NombreTipoInstrumento;
		filtro2 = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_GRUPO_CLIENTE").msg();
		valor2 	= entrada.p_DescripcionSegmento;
		GenerarInformeComprobanteComisionesMensualesUtils.establecerFilaFiltros(hoja,celda,fila, filtro1, valor1, filtro2, valor2);
	
	}	
	
	public static void establecerFilaFiltros (Sheet hoja, Cell celda, Row fila, String filtro1, String valor1, String filtro2, String valor2) {
		
		for (Integer j=0; j<CANTIDAD_COLUMNAS_EN_HOJA; j++) {
			celda = fila.createCell(j);
			celda.setCellStyle(estiloCeldaSinBordes);
			if (j == 3) {
				celda.setCellValue(filtro1);
				celda.setCellStyle(estiloSubTituloSeccion);
			} 
			
			if( j == 4) {
				celda.setCellValue(valor1);
			}
			
			if( j == 5) {
				celda.setCellValue(filtro2);
				celda.setCellStyle(estiloSubTituloSeccion);
			}
			
			if( j == 6) {
				celda.setCellValue(valor2);
			}
			
		}
	}
	
	public static CalcularParidadSalida calcularParidad(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada, List<String> listaMonedas, String MonedaDestino) throws Exception {
		
		logger.info("INICIO::GenerarInformeComprobanteComisionesMensualesUtils.calcularParidad");
		Long inicioServicio = System.currentTimeMillis();
		
		String fechaSistema 					= Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.FechaSistema");;
		CalcularParidad paridad 				= new CalcularParidad();
		CalcularParidadEntrada paridadEntrada 	= new CalcularParidadEntrada();
		paridadEntrada.p_IdEmpresa				= entrada.p_IdEmpresa;
		paridadEntrada.p_IdTrabajo				= 1L;
		paridadEntrada.p_CalculosEntrada		= listaMonedas.parallelStream().map(moneda->{
			CalculoEntrada calculoEntrada 	= new CalculoEntrada();
			calculoEntrada.IdInterno		= 1L;
			calculoEntrada.Fecha    		= fechaSistema;
			calculoEntrada.IdMonedaOrigen 	= moneda;
			calculoEntrada.IdMonedaDestino 	= MonedaDestino;
			calculoEntrada.MontoOrigen 		= BigDecimal.ONE;
			calculoEntrada.IdUso			= "";
			return calculoEntrada;			
		}).collect(Collectors.toList());
		
		CalcularParidadSalida salidaParidad = paridad.logica(con, paridadEntrada);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeComprobanteComisionesMensualesUtils.calcularParidad", finServicio - inicioServicio);
		return salidaParidad;
	}
	
	public static BigDecimal obtenerPorcentajeImpuesto(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada) throws Exception {
		
		logger.info("INICIO::GenerarInformeComprobanteComisionesMensualesUtils.obtenerPorcentajeImpuesto");
		Long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		String cobro = "IVA";
		HashMap<String,Object> params = new HashMap<>();
        params.put("IdCobro", cobro);
        params.put("IdEmpresa", entrada.p_IdEmpresa);
        List<Cobro> impuesto = sql.query(con, SQL_SELECT_PORCENTAJE_IMPUESTO, params, Cobro.class);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeComprobanteComisionesMensualesUtils.obtenerPorcentajeImpuesto", finServicio - inicioServicio);
		return impuesto.get(0).ValorCobro;
	}
	
	public static void agregarLogo(Connection con, Long idEmpresa, Workbook archivoExcel, Sheet hoja) {
	    try {
	        HashMap<String, Object> params = new HashMap<>(1);
	        params.put("IdEmpresa", idEmpresa);

	        Imagen logo = new SQL().query(con, SQL_SELECT_LOGO_CORREDORA, params, Imagen.class).stream().findFirst().orElse(new Imagen());

	        if (logo.Contenido == null || logo.Contenido.length == 0) {
	            return;
	        }

	        byte[] bytes = logo.Contenido;

	        int tipo;
	        if (bytes.length >= 3 &&
	            bytes[0] == (byte) 0xFF &&
	            bytes[1] == (byte) 0xD8 &&
	            bytes[2] == (byte) 0xFF) {
	            tipo = Workbook.PICTURE_TYPE_JPEG;
	        } else {
	            tipo = Workbook.PICTURE_TYPE_PNG;
	        }

	        int pictureIdx = archivoExcel.addPicture(bytes, tipo);

	        CreationHelper helper = archivoExcel.getCreationHelper();
	        Drawing drawing = hoja.createDrawingPatriarch();
	        ClientAnchor anchor = helper.createClientAnchor();

	        anchor.setCol1(0);
	        anchor.setRow1(0);

	        Picture pict = drawing.createPicture(anchor, pictureIdx);
	        pict.resize(2.5, 6);
	    } catch (Exception e) {
	        logger.error("Error al agregar logo en Excel", e);
	    }
	}

}
