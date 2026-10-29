package cl.bcs.becb.riesgo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import cl.bcs.becb.riesgo.DetalleRiesgoFinanciero;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * 
 * @author Carlos Herrera
 *
 */

public class DetalleRiesgoFinanciero implements InterfaceRiesgoFinanciero {

	static final Logger logger = LogManager.getLogger(DetalleRiesgoFinanciero.class);

	private String cosultarDataDetalleFinancieroSQL;
	private String cosultarDataDetalleFinancieroHisSQL;

	public static class DataDetalleRiesgo {
		public String Estado;
		public String Emisor;
		public String Familia;
		public String Nemo;
		public BigDecimal Cantidad;
		public BigDecimal Nominal;
		public BigDecimal TirTasa;
		public String TipoReajuste;
		public String FechaVencimiento;
		public BigDecimal ValorPresente;
		public BigDecimal Plazo;
		public BigDecimal Volatilidad;
		public BigDecimal Duracion;
		public BigDecimal Var;
		public Long SecCar;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public DetalleRiesgoFinanciero(String consultaSQL, String consultaHisSQL) {
		this.cosultarDataDetalleFinancieroSQL = consultaSQL;
		this.cosultarDataDetalleFinancieroHisSQL = consultaHisSQL;
	}

	@Override
	public boolean obtenerData(String codigo, String fechaInforme, Long cliente, BigDecimal valorUF, String ruta,
			String tabla, Connection con) throws SQLException, IOException {
		logger.info("==================== Inicio Detalle Riesgo ====================");
		SQL sql = new SQL();
		HashMap<String, Object> parametrosConsulta = new HashMap<>();
		parametrosConsulta.put("ValorUF", valorUF);
		parametrosConsulta.put("IdCliente", cliente);
		parametrosConsulta.put("FechaInforme", fechaInforme);
		String consultaFinal;
		if (tabla.equalsIgnoreCase("CUS")) {
			consultaFinal = cosultarDataDetalleFinancieroSQL;
		} else {
			consultaFinal = cosultarDataDetalleFinancieroHisSQL;
			
		}
		List<DataDetalleRiesgo> detalleRiesgo = sql.query(con, consultaFinal, parametrosConsulta,
				DataDetalleRiesgo.class);
		logger.info("==================== Fin Detalle Riesgo ====================");
		return detalleRiesgo(detalleRiesgo, ruta);
	}

	private boolean detalleRiesgo(List<DataDetalleRiesgo> detalleRiesgo, String ruta) throws IOException {
		String vacio = "No existe data";
		if (detalleRiesgo.isEmpty()) {
			logger.debug("========================");
			logger.debug(vacio);
			logger.debug("========================");
			return false;
		} else {
			try (FileOutputStream outputStream = new FileOutputStream(ruta)) {
				Workbook archivoExcel = new HSSFWorkbook();
				Sheet hoja = archivoExcel.createSheet();
				archivoExcel.setSheetName(0, "Detalle Riesgo Financiero");

				/************************************/
				/** Formatos **/
				/************************************/

				hoja.setColumnWidth(0, 4000);
				hoja.setColumnWidth(1, 4000);
				hoja.setColumnWidth(2, 4000);
				hoja.setColumnWidth(3, 4000);
				hoja.setColumnWidth(4, 4000);
				hoja.setColumnWidth(5, 4000);
				hoja.setColumnWidth(6, 4000);
				hoja.setColumnWidth(7, 4000);
				hoja.setColumnWidth(8, 4000);
				hoja.setColumnWidth(9, 4000);
				hoja.setColumnWidth(10, 4000);
				hoja.setColumnWidth(11, 4000);
				hoja.setColumnWidth(12, 4000);
				hoja.setColumnWidth(13, 4000);
				hoja.setColumnWidth(14, 4000);

				CellStyle estiloMoneda = archivoExcel.createCellStyle();
				DataFormat df = archivoExcel.createDataFormat();
				estiloMoneda.setDataFormat(df.getFormat("_ $* #,##0_ ;_ $* -#,##0_ ;_ $* \"-\"??_ ;_ @_ "));

				CellStyle estiloNumero = archivoExcel.createCellStyle();
				estiloNumero.setDataFormat(df.getFormat("#,##0"));

				CellStyle estiloNegrita = archivoExcel.createCellStyle();
				Font fuente = archivoExcel.createFont();
				fuente.setBold(true);
				estiloNegrita.setFont(fuente);
				estiloNegrita.setAlignment(CellStyle.ALIGN_CENTER);

				CellStyle estiloEncabezado = archivoExcel.createCellStyle();
				Font fuenteEncabezado = archivoExcel.createFont();
				fuenteEncabezado.setBold(true);
				estiloEncabezado.setFont(fuenteEncabezado);
				estiloEncabezado.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
				estiloEncabezado.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloEncabezado.setAlignment(CellStyle.ALIGN_CENTER);
				estiloEncabezado.setWrapText(true);

				CellStyle estiloTipoOperacion = archivoExcel.createCellStyle();
				estiloTipoOperacion.setWrapText(true);

				/************************************/
				/** Encabezado **/
				/************************************/
				int rowCount = 0;
				Row fila = hoja.createRow(rowCount);
				int columnCount = 0;

				Cell celda = fila.createCell(columnCount);
				celda.setCellValue("Estado");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Emisor");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Familia");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Nemo");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Cantidad");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Nominal");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Tir/Tasa");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Tipo Reajuste");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Fecha Vcto");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Valor Presente");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Plazo");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Volatilidad");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Duracion");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Var");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Sec-Car");
				celda.setCellStyle(estiloEncabezado);

				/************************************/
				/** DETALLE DIFI **/
				/************************************/

				for (DataDetalleRiesgo dataDetalleRiesgo : detalleRiesgo) {
					columnCount = 0;
					fila = hoja.createRow(++rowCount); // fila en donde se escribe

					celda = fila.createCell(columnCount);
					celda.setCellValue(dataDetalleRiesgo.Estado);

					celda = fila.createCell(++columnCount);
					celda.setCellValue(dataDetalleRiesgo.Emisor);

					celda = fila.createCell(++columnCount);
					celda.setCellValue(dataDetalleRiesgo.Familia);

					celda = fila.createCell(++columnCount);
					celda.setCellValue(dataDetalleRiesgo.Nemo);

					celda = fila.createCell(++columnCount);
					celda.setCellType(Cell.CELL_TYPE_NUMERIC);
					celda.setCellValue(dataDetalleRiesgo.Cantidad.doubleValue());
					celda.setCellStyle(estiloNumero);

					celda = fila.createCell(++columnCount);
					celda.setCellType(Cell.CELL_TYPE_NUMERIC);
					celda.setCellValue(dataDetalleRiesgo.Nominal.doubleValue());
					celda.setCellStyle(estiloNumero);

					celda = fila.createCell(++columnCount);
					celda.setCellType(Cell.CELL_TYPE_NUMERIC);
					celda.setCellValue(dataDetalleRiesgo.TirTasa.doubleValue());
					celda.setCellStyle(estiloNumero);

					celda = fila.createCell(++columnCount);
					celda.setCellValue(dataDetalleRiesgo.TipoReajuste);

					celda = fila.createCell(++columnCount);
					celda.setCellValue(dataDetalleRiesgo.FechaVencimiento);

					celda = fila.createCell(++columnCount);
					celda.setCellType(Cell.CELL_TYPE_NUMERIC);
					celda.setCellValue(dataDetalleRiesgo.ValorPresente.doubleValue());
					celda.setCellStyle(estiloNumero);

					celda = fila.createCell(++columnCount);
					celda.setCellType(Cell.CELL_TYPE_NUMERIC);
					celda.setCellValue(dataDetalleRiesgo.Plazo.doubleValue());
					celda.setCellStyle(estiloNumero);

					celda = fila.createCell(++columnCount);
					celda.setCellType(Cell.CELL_TYPE_NUMERIC);
					celda.setCellValue(dataDetalleRiesgo.Volatilidad.doubleValue());

					celda = fila.createCell(++columnCount);
					celda.setCellType(Cell.CELL_TYPE_NUMERIC);
					celda.setCellValue(dataDetalleRiesgo.Duracion.doubleValue());
					celda.setCellStyle(estiloNumero);

					celda = fila.createCell(++columnCount);
					celda.setCellType(Cell.CELL_TYPE_NUMERIC);
					celda.setCellValue(dataDetalleRiesgo.Var.doubleValue());
					celda.setCellStyle(estiloNumero);

					celda = fila.createCell(++columnCount);
					celda.setCellType(Cell.CELL_TYPE_NUMERIC);
					celda.setCellValue(dataDetalleRiesgo.SecCar.doubleValue());
					celda.setCellStyle(estiloNumero);
				}

				archivoExcel.write(outputStream);
				logger.debug(archivoExcel);
				return true;
			}
		}
	}
}
