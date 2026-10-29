package cl.bcs.becb.riesgo;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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

import cl.bcs.becb.riesgo.InformeRiesgoFinanciero;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * 
 * @author Carlos Herrera
 *
 */

public class InformeRiesgoFinanciero implements InterfaceRiesgoFinanciero {
	static final Logger logger = LogManager.getLogger(InformeRiesgoFinanciero.class);

	private String cosultarDataRiesgoFinancieroSQL;
	private String cosultarDataRiesgoFinancieroHisSQL;

	public static class DataRiesgoFinanciero {
		public String InstrumentoEmisor;
		public BigDecimal Rango;
		public BigDecimal Volatilidad;
		public BigDecimal Duracion;
		public BigDecimal ValorPresente;
		public BigDecimal Var;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class DataRiesgoFinancieroAux {
		public String InstrumentoEmisor;
		public BigDecimal Rango;
		public BigDecimal Volatilidad;
		public BigDecimal Duracion;
		public BigDecimal ValorPresente;
		public BigDecimal Var;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public InformeRiesgoFinanciero(String consultaSQL, String consultaHisSQL) {
		this.cosultarDataRiesgoFinancieroSQL = consultaSQL;
		this.cosultarDataRiesgoFinancieroHisSQL = consultaHisSQL;
	}

	@Override
	public boolean obtenerData(String codigo, String fechaInforme, Long cliente, BigDecimal valorUF, String ruta,
			String tabla, Connection con) throws SQLException, FileNotFoundException, IOException {
		logger.info("==================== Inicio Riesgo Financiero ====================");
		SQL sql = new SQL();
		HashMap<String, Object> parametrosConsulta = new HashMap<>();
//		parametrosConsulta.put("FechaSistema", fechaSistema);
		parametrosConsulta.put("ValorUF", valorUF);
		parametrosConsulta.put("IdCliente", cliente);
		parametrosConsulta.put("FechaInforme", fechaInforme);
		String consultaFinal;
		if (tabla.equalsIgnoreCase("CUS")) {
			consultaFinal = cosultarDataRiesgoFinancieroSQL;
		} else {
			consultaFinal = cosultarDataRiesgoFinancieroHisSQL;
			//parametrosConsulta.put("FechaInforme", fechaInforme);
		}
		List<DataRiesgoFinanciero> riesgoFinanciero = sql.query(con, consultaFinal, parametrosConsulta,
				DataRiesgoFinanciero.class);
		logger.info("==================== Fin Riesgo Financiero ====================");
		return informeRiesgo(riesgoFinanciero, ruta);
	}

	private boolean informeRiesgo(List<DataRiesgoFinanciero> riesgoFinanciero, String ruta)
			throws FileNotFoundException, IOException {
		String vacio = "No existe data";
		if (riesgoFinanciero.isEmpty()) {
			logger.debug("========================");
			logger.debug(vacio);
			logger.debug("========================");
			return false;
		} else {
			try (FileOutputStream outputStream = new FileOutputStream(ruta)) {
				Workbook archivoExcel = new HSSFWorkbook();
				Sheet hoja = archivoExcel.createSheet();
				archivoExcel.setSheetName(0, "Informe Riesgo Financiero");

				/************************************/
				/** Formatos **/
				/************************************/
				hoja.setColumnWidth(0, 6000);
				hoja.setColumnWidth(1, 3000);
				hoja.setColumnWidth(2, 3000);
				hoja.setColumnWidth(3, 3000);
				hoja.setColumnWidth(4, 6000);
				hoja.setColumnWidth(5, 3000);

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
//				fuenteEncabezado.setColor(IndexedColors.WHITE.getIndex());
				estiloEncabezado.setFont(fuenteEncabezado);
				estiloEncabezado.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
				estiloEncabezado.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloEncabezado.setAlignment(CellStyle.ALIGN_CENTER);
				estiloEncabezado.setWrapText(true);

//				DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

				CellStyle estiloTipoOperacion = archivoExcel.createCellStyle();
				estiloTipoOperacion.setWrapText(true);

				/************************************/
				/** Encabezado **/
				/************************************/
				int rowCount = 0;
				Row fila = hoja.createRow(rowCount);
				int columnCount = 0;

				Cell celda = fila.createCell(columnCount);
				celda.setCellValue("Instrumento Emisor");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Rango");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Volatilidad");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Duracion");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Valor Presente (UF)");
				celda.setCellStyle(estiloEncabezado);

				celda = fila.createCell(++columnCount);
				celda.setCellValue("Var");
				celda.setCellStyle(estiloEncabezado);

				/************************************/
				/** DETALLE **/
				/************************************/
				List<DataRiesgoFinancieroAux> riesgoFinancieroAux = new ArrayList<DataRiesgoFinancieroAux>();
				riesgoFinancieroAux.clear();
				DataRiesgoFinancieroAux dataAuxRiesgoFinanciero = new DataRiesgoFinancieroAux();

				dataAuxRiesgoFinanciero.InstrumentoEmisor = riesgoFinanciero.get(0).InstrumentoEmisor;
				dataAuxRiesgoFinanciero.Rango = riesgoFinanciero.get(0).Rango;
				dataAuxRiesgoFinanciero.Volatilidad = BigDecimal.ZERO;;
				dataAuxRiesgoFinanciero.Duracion = riesgoFinanciero.get(0).Duracion;
				dataAuxRiesgoFinanciero.ValorPresente = BigDecimal.ZERO;
				dataAuxRiesgoFinanciero.Var = BigDecimal.ZERO;;
				logger.debug(riesgoFinancieroAux.size());

				for (DataRiesgoFinanciero dataRiesgoFinanciero : riesgoFinanciero) {
					if (dataRiesgoFinanciero.Rango.compareTo(dataAuxRiesgoFinanciero.Rango) == 0
							&& dataRiesgoFinanciero.InstrumentoEmisor
									.equalsIgnoreCase(dataAuxRiesgoFinanciero.InstrumentoEmisor)) {
						if (dataAuxRiesgoFinanciero.Duracion.compareTo(dataRiesgoFinanciero.Duracion) < 0) {
							dataAuxRiesgoFinanciero.Duracion = dataRiesgoFinanciero.Duracion;
						}
						dataAuxRiesgoFinanciero.ValorPresente = dataAuxRiesgoFinanciero.ValorPresente
								.add(dataRiesgoFinanciero.ValorPresente);

					} else {
						riesgoFinancieroAux.add(dataAuxRiesgoFinanciero);
						dataAuxRiesgoFinanciero = new DataRiesgoFinancieroAux();
						dataAuxRiesgoFinanciero.InstrumentoEmisor = dataRiesgoFinanciero.InstrumentoEmisor;
						dataAuxRiesgoFinanciero.Rango = dataRiesgoFinanciero.Rango;
						dataAuxRiesgoFinanciero.Volatilidad = dataRiesgoFinanciero.Volatilidad;
						dataAuxRiesgoFinanciero.Duracion = dataRiesgoFinanciero.Duracion;
						dataAuxRiesgoFinanciero.ValorPresente = dataRiesgoFinanciero.ValorPresente;
						dataAuxRiesgoFinanciero.Var = dataRiesgoFinanciero.Var;
					}
				}
				riesgoFinancieroAux.add(dataAuxRiesgoFinanciero);

				for (DataRiesgoFinancieroAux dataRiesgoFinanciero : riesgoFinancieroAux) {
					columnCount = 0;
					fila = hoja.createRow(++rowCount); // fila en donde se escribe

					celda = fila.createCell(columnCount);
					celda.setCellValue(dataRiesgoFinanciero.InstrumentoEmisor);

					celda = fila.createCell(++columnCount);
					celda.setCellType(Cell.CELL_TYPE_NUMERIC);
					if (dataRiesgoFinanciero.Rango.doubleValue() == 0) {
						celda.setCellValue("");
					} else {
						celda.setCellValue(dataRiesgoFinanciero.Rango.doubleValue());
					}
					celda.setCellStyle(estiloNumero);

					celda = fila.createCell(++columnCount);
					celda.setCellType(Cell.CELL_TYPE_NUMERIC);
					celda.setCellValue(dataRiesgoFinanciero.Volatilidad.doubleValue());
					celda.setCellStyle(estiloNumero);

					celda = fila.createCell(++columnCount);
					celda.setCellType(Cell.CELL_TYPE_NUMERIC);
					celda.setCellValue(dataRiesgoFinanciero.Duracion.doubleValue());
					celda.setCellStyle(estiloNumero);

					celda = fila.createCell(++columnCount);
					celda.setCellType(Cell.CELL_TYPE_NUMERIC);
					celda.setCellValue(dataRiesgoFinanciero.ValorPresente.doubleValue());
					celda.setCellStyle(estiloNumero);

					celda = fila.createCell(++columnCount);
					celda.setCellType(Cell.CELL_TYPE_NUMERIC);
					celda.setCellValue(dataRiesgoFinanciero.Var.doubleValue());
					celda.setCellStyle(estiloNumero);
				}
				
				archivoExcel.write(outputStream);
				logger.debug(archivoExcel);
				return true;
			}
		}
	}
}
