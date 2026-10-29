package cl.bcs.ordenes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;


public class GenerarInformeEstadisticaUtils {
	
	static final Logger logger = LogManager.getLogger(GenerarInformeEstadisticaUtils.class);
	
	static CellStyle estiloTitulo;
	static CellStyle estiloEncabezado;
	static CellStyle estiloMoneda;
	static CellStyle estiloDetalle;
	static CellStyle estiloDetallePrincipal;
	static CellStyle estiloMontoTotal;
	
	public static enum EstiloCelda {
		TITULO,
		ENCABEZADO,
		MONEDA,
		MONTO_TOTAL,
		DETALLE,
		DETALLE_PRINCIPAL
	}
	
	public static void fijarEstilos(Workbook archivoExcel) {
		estiloTitulo 			= obtenerEstiloCelda(EstiloCelda.TITULO, archivoExcel);
		estiloEncabezado		= obtenerEstiloCelda(EstiloCelda.ENCABEZADO, archivoExcel);
		estiloMoneda 			= obtenerEstiloCelda(EstiloCelda.MONEDA, archivoExcel);
		estiloDetalle 			= obtenerEstiloCelda(EstiloCelda.DETALLE, archivoExcel);
		estiloDetallePrincipal 	= obtenerEstiloCelda(EstiloCelda.DETALLE_PRINCIPAL, archivoExcel);
		estiloMontoTotal		= obtenerEstiloCelda(EstiloCelda.MONTO_TOTAL, archivoExcel);
		
	}
	
	public static void fijarAnchoColumnas(Sheet hoja) {
		hoja.setColumnWidth(0, 7000);
		hoja.setColumnWidth(1, 3000);
		hoja.setColumnWidth(3, 5000);
		hoja.setColumnWidth(2, 4000);
		hoja.setColumnWidth(4, 3000);
		hoja.setColumnWidth(5, 3000);
	} 
	
	public static CellStyle obtenerEstiloCelda (EstiloCelda estilo,  Workbook archivoExcel) {
		
		CellStyle estiloCelda = archivoExcel.createCellStyle();
		Font fuenteCelda = archivoExcel.createFont();
		DataFormat df = archivoExcel.createDataFormat();
		switch (estilo) {
			case TITULO:
				// estilo de titulo de informe
				fuenteCelda.setBold(true);
    			estiloCelda.setFont(fuenteCelda);
    			estiloCelda.setAlignment(CellStyle.ALIGN_CENTER);
    			estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				break;
				
			case MONTO_TOTAL:
				// estilo de subtitulo de seccion
    			estiloCelda.setDataFormat(df.getFormat("_ $* #,##0_ ;_ $* -#,##0_ ;_ $* \"-\"??_ ;_ @_ "));
				fuenteCelda.setBold(true);
				estiloCelda.setFont(fuenteCelda);
				estiloCelda.setBorderTop(CellStyle.BORDER_THIN);
				estiloCelda.setBorderRight(CellStyle.BORDER_THIN);
				estiloCelda.setBorderBottom(CellStyle.BORDER_THIN);
				estiloCelda.setBorderLeft(CellStyle.BORDER_THIN);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);	
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				break;
				
			case MONEDA:
				// estilo de subtitulo de seccion
    			estiloCelda.setDataFormat(df.getFormat("_ $* #,##0_ ;_ $* -#,##0_ ;_ $* \"-\"??_ ;_ @_ "));
				estiloCelda.setFont(fuenteCelda);
				estiloCelda.setBorderTop(CellStyle.BORDER_THIN);
				estiloCelda.setBorderRight(CellStyle.BORDER_THIN);
				estiloCelda.setBorderBottom(CellStyle.BORDER_THIN);
				estiloCelda.setBorderLeft(CellStyle.BORDER_THIN);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);	
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());
				break;
				
			case ENCABEZADO: 
				fuenteCelda.setBold(true);
    			fuenteCelda.setColor(IndexedColors.WHITE.getIndex());
    			estiloCelda.setFont(fuenteCelda);
    			estiloCelda.setFillForegroundColor(IndexedColors.BLUE.getIndex());  
    			estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);  
    			estiloCelda.setAlignment(CellStyle.ALIGN_CENTER);
    			estiloCelda.setWrapText(true);	
    			estiloCelda.setBorderTop(CellStyle.BORDER_THIN);
				estiloCelda.setBorderRight(CellStyle.BORDER_THIN);
				estiloCelda.setBorderBottom(CellStyle.BORDER_THIN);
				estiloCelda.setBorderLeft(CellStyle.BORDER_THIN);
				break;
				
			case DETALLE:
				estiloCelda.setWrapText(true);
    			estiloCelda.setFont(fuenteCelda);
				estiloCelda.setAlignment(CellStyle.ALIGN_CENTER);
				estiloCelda.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloCelda.setBorderTop(CellStyle.BORDER_THIN);
				estiloCelda.setBorderRight(CellStyle.BORDER_THIN);
				estiloCelda.setBorderBottom(CellStyle.BORDER_THIN);
				estiloCelda.setBorderLeft(CellStyle.BORDER_THIN);
				estiloCelda.setFillForegroundColor(IndexedColors.WHITE.getIndex());		
				break;
				
			case DETALLE_PRINCIPAL:
				estiloCelda.setWrapText(true);
				fuenteCelda.setBold(true);
    			estiloCelda.setFont(fuenteCelda);
				estiloCelda.setAlignment(CellStyle.ALIGN_CENTER);
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
	
	

}
