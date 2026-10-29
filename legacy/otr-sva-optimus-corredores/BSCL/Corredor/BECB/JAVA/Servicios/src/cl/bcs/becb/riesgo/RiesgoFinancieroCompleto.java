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

import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * 
 * @author Carlos Herrera
 *
 */

public class RiesgoFinancieroCompleto implements InterfaceRiesgoFinanciero {
	static final Logger logger = LogManager.getLogger(RiesgoFinancieroCompleto.class);

	private String cosultarDataRiesgoFinancieroSQL;
	private String cosultarDataRiesgoFinancieroHisSQL;
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

	public RiesgoFinancieroCompleto(String consultaRiesgo, String consultaRiesgoHis, String consultaDetalle,
			String consultaDetalleHis) {
		this.cosultarDataRiesgoFinancieroSQL = consultaRiesgo;
		this.cosultarDataRiesgoFinancieroHisSQL = consultaRiesgoHis;
		this.cosultarDataDetalleFinancieroSQL = consultaDetalle;
		this.cosultarDataDetalleFinancieroHisSQL = consultaDetalleHis;
	}

	@Override
	public boolean obtenerData(String codigo, String fechaInforme, Long cliente, BigDecimal valorUF, String ruta,
			String tabla, Connection con) throws SQLException, FileNotFoundException, IOException {
		logger.info("==================== Inicio Riesgo Financiero Completo ====================");
		SQL sql = new SQL();
		HashMap<String, Object> parametrosConsulta = new HashMap<>();
		parametrosConsulta.put("ValorUF", valorUF);
		parametrosConsulta.put("IdCliente", cliente);
		parametrosConsulta.put("FechaInforme", fechaInforme);

		String consultaFinal;
		String consultaFinalDetalle;
		if (tabla.equalsIgnoreCase("CUS")) {
			consultaFinal = cosultarDataRiesgoFinancieroSQL;
			consultaFinalDetalle = cosultarDataDetalleFinancieroSQL;
		} else {
			consultaFinal = cosultarDataRiesgoFinancieroHisSQL;
			consultaFinalDetalle = cosultarDataDetalleFinancieroHisSQL;
			
		}
		List<DataRiesgoFinanciero> riesgoFinanciero = sql.query(con, consultaFinal, parametrosConsulta,
				DataRiesgoFinanciero.class);
		List<DataDetalleRiesgo> detalleRiesgo = sql.query(con, consultaFinalDetalle, parametrosConsulta,
				DataDetalleRiesgo.class);
		logger.info("==================== Fin Riesgo Financiero Completo ====================");
		return informeRiesgo(riesgoFinanciero, detalleRiesgo, ruta);
	}

	private boolean informeRiesgo(List<DataRiesgoFinanciero> riesgoFinanciero, List<DataDetalleRiesgo> detalleRiesgo,
			String ruta) throws FileNotFoundException, IOException {
		String vacio = "No existe data";
		if (riesgoFinanciero.isEmpty() && detalleRiesgo.isEmpty()) {
			logger.debug("========================");
			logger.debug(vacio);
			logger.debug("========================");
			return false;
		} else {
			try (FileOutputStream outputStream = new FileOutputStream(ruta)) {
				Workbook archivoExcel = new HSSFWorkbook();
				Sheet hojaRifi = archivoExcel.createSheet();
				Sheet hojaDifi = archivoExcel.createSheet();
				archivoExcel.setSheetName(0, "Informe Riesgo Financiero");
				archivoExcel.setSheetName(1, "Detalle Riesgo Financiero");

				/************************************/
				/** Formatos **/
				/************************************/
				hojaRifi.setColumnWidth(0, 6000);
				hojaRifi.setColumnWidth(1, 3000);
				hojaRifi.setColumnWidth(2, 3000);
				hojaRifi.setColumnWidth(3, 3000);
				hojaRifi.setColumnWidth(4, 6000);
				hojaRifi.setColumnWidth(5, 3000);

				hojaDifi.setColumnWidth(0, 4000);
				hojaDifi.setColumnWidth(1, 4000);
				hojaDifi.setColumnWidth(2, 4000);
				hojaDifi.setColumnWidth(3, 4000);
				hojaDifi.setColumnWidth(4, 4000);
				hojaDifi.setColumnWidth(5, 4000);
				hojaDifi.setColumnWidth(6, 4000);
				hojaDifi.setColumnWidth(7, 4000);
				hojaDifi.setColumnWidth(8, 4000);
				hojaDifi.setColumnWidth(9, 4000);
				hojaDifi.setColumnWidth(10, 4000);
				hojaDifi.setColumnWidth(11, 4000);
				hojaDifi.setColumnWidth(12, 4000);
				hojaDifi.setColumnWidth(13, 4000);
				hojaDifi.setColumnWidth(14, 4000);

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
				estiloEncabezado.setFont(fuenteEncabezado);
				estiloEncabezado.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
				estiloEncabezado.setFillPattern(CellStyle.SOLID_FOREGROUND);
				estiloEncabezado.setAlignment(CellStyle.ALIGN_CENTER);
				estiloEncabezado.setWrapText(true);


				CellStyle estiloTipoOperacion = archivoExcel.createCellStyle();
				estiloTipoOperacion.setWrapText(true);

				/************************************/
				/** Encabezado Rifi **/
				/************************************/
				int rowCount = 0;
				Row filaRifi = hojaRifi.createRow(rowCount);
				int columnCount = 0;

				Cell celdaRifi = filaRifi.createCell(columnCount);
				celdaRifi.setCellValue("Instrumento Emisor");
				celdaRifi.setCellStyle(estiloEncabezado);

				celdaRifi = filaRifi.createCell(++columnCount);
				celdaRifi.setCellValue("Rango");
				celdaRifi.setCellStyle(estiloEncabezado);

				celdaRifi = filaRifi.createCell(++columnCount);
				celdaRifi.setCellValue("Volatilidad");
				celdaRifi.setCellStyle(estiloEncabezado);

				celdaRifi = filaRifi.createCell(++columnCount);
				celdaRifi.setCellValue("Duracion");
				celdaRifi.setCellStyle(estiloEncabezado);

				celdaRifi = filaRifi.createCell(++columnCount);
				celdaRifi.setCellValue("Valor Presente (UF)");
				celdaRifi.setCellStyle(estiloEncabezado);

				celdaRifi = filaRifi.createCell(++columnCount);
				celdaRifi.setCellValue("Var");
				celdaRifi.setCellStyle(estiloEncabezado);

				/************************************/
				/** DETALLE RIFI **/
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
					filaRifi = hojaRifi.createRow(++rowCount); // fila en donde se escribe

					celdaRifi = filaRifi.createCell(columnCount);
					celdaRifi.setCellValue(dataRiesgoFinanciero.InstrumentoEmisor);

					celdaRifi = filaRifi.createCell(++columnCount);
					celdaRifi.setCellType(Cell.CELL_TYPE_NUMERIC);
					if (dataRiesgoFinanciero.Rango.doubleValue() == 0) {
						celdaRifi.setCellValue("");
					} else {
						celdaRifi.setCellValue(dataRiesgoFinanciero.Rango.doubleValue());
					}
					celdaRifi.setCellStyle(estiloNumero);

					celdaRifi = filaRifi.createCell(++columnCount);
					celdaRifi.setCellType(Cell.CELL_TYPE_NUMERIC);
					celdaRifi.setCellValue(dataRiesgoFinanciero.Volatilidad.doubleValue());
					celdaRifi.setCellStyle(estiloNumero);

					celdaRifi = filaRifi.createCell(++columnCount);
					celdaRifi.setCellType(Cell.CELL_TYPE_NUMERIC);
					celdaRifi.setCellValue(dataRiesgoFinanciero.Duracion.doubleValue());
					celdaRifi.setCellStyle(estiloNumero);

					celdaRifi = filaRifi.createCell(++columnCount);
					celdaRifi.setCellType(Cell.CELL_TYPE_NUMERIC);
					celdaRifi.setCellValue(dataRiesgoFinanciero.ValorPresente.doubleValue());
					celdaRifi.setCellStyle(estiloNumero);

					celdaRifi = filaRifi.createCell(++columnCount);
					celdaRifi.setCellType(Cell.CELL_TYPE_NUMERIC);
					celdaRifi.setCellValue(dataRiesgoFinanciero.Var.doubleValue());
					celdaRifi.setCellStyle(estiloNumero);
				}

				/************************************/
				/** Encabezado Difi **/
				/************************************/
				
				rowCount = 0;
				Row filaDifi = hojaDifi.createRow(rowCount);
				columnCount = 0;

				Cell celdaDifi = filaDifi.createCell(columnCount);
				celdaDifi.setCellValue("Estado");
				celdaDifi.setCellStyle(estiloEncabezado);

				celdaDifi = filaDifi.createCell(++columnCount);
				celdaDifi.setCellValue("Emisor");
				celdaDifi.setCellStyle(estiloEncabezado);

				celdaDifi = filaDifi.createCell(++columnCount);
				celdaDifi.setCellValue("Familia");
				celdaDifi.setCellStyle(estiloEncabezado);

				celdaDifi = filaDifi.createCell(++columnCount);
				celdaDifi.setCellValue("Nemo");
				celdaDifi.setCellStyle(estiloEncabezado);

				celdaDifi = filaDifi.createCell(++columnCount);
				celdaDifi.setCellValue("Cantidad");
				celdaDifi.setCellStyle(estiloEncabezado);

				celdaDifi = filaDifi.createCell(++columnCount);
				celdaDifi.setCellValue("Nominal");
				celdaDifi.setCellStyle(estiloEncabezado);

				celdaDifi = filaDifi.createCell(++columnCount);
				celdaDifi.setCellValue("Tir/Tasa");
				celdaDifi.setCellStyle(estiloEncabezado);

				celdaDifi = filaDifi.createCell(++columnCount);
				celdaDifi.setCellValue("Tipo Reajuste");
				celdaDifi.setCellStyle(estiloEncabezado);

				celdaDifi = filaDifi.createCell(++columnCount);
				celdaDifi.setCellValue("Fecha Vcto");
				celdaDifi.setCellStyle(estiloEncabezado);

				celdaDifi = filaDifi.createCell(++columnCount);
				celdaDifi.setCellValue("Valor Presente");
				celdaDifi.setCellStyle(estiloEncabezado);

				celdaDifi = filaDifi.createCell(++columnCount);
				celdaDifi.setCellValue("Plazo");
				celdaDifi.setCellStyle(estiloEncabezado);

				celdaDifi = filaDifi.createCell(++columnCount);
				celdaDifi.setCellValue("Volatilidad");
				celdaDifi.setCellStyle(estiloEncabezado);

				celdaDifi = filaDifi.createCell(++columnCount);
				celdaDifi.setCellValue("Duracion");
				celdaDifi.setCellStyle(estiloEncabezado);

				celdaDifi = filaDifi.createCell(++columnCount);
				celdaDifi.setCellValue("Var");
				celdaDifi.setCellStyle(estiloEncabezado);

				celdaDifi = filaDifi.createCell(++columnCount);
				celdaDifi.setCellValue("Sec-Car");
				celdaDifi.setCellStyle(estiloEncabezado);

				/************************************/
				/** DETALLE DIFI **/
				/************************************/

				for (DataDetalleRiesgo dataDetalleRiesgo : detalleRiesgo) {
					columnCount = 0;
					filaDifi = hojaDifi.createRow(++rowCount); // fila en donde se escribe

					celdaDifi = filaDifi.createCell(columnCount);
					celdaDifi.setCellValue(dataDetalleRiesgo.Estado);

					celdaDifi = filaDifi.createCell(++columnCount);
					celdaDifi.setCellValue(dataDetalleRiesgo.Emisor);

					celdaDifi = filaDifi.createCell(++columnCount);
					celdaDifi.setCellValue(dataDetalleRiesgo.Familia);

					celdaDifi = filaDifi.createCell(++columnCount);
					celdaDifi.setCellValue(dataDetalleRiesgo.Nemo);

					celdaDifi = filaDifi.createCell(++columnCount);
					celdaDifi.setCellType(Cell.CELL_TYPE_NUMERIC);
					celdaDifi.setCellValue(dataDetalleRiesgo.Cantidad.doubleValue());
					celdaDifi.setCellStyle(estiloNumero);

					celdaDifi = filaDifi.createCell(++columnCount);
					celdaDifi.setCellType(Cell.CELL_TYPE_NUMERIC);
					celdaDifi.setCellValue(dataDetalleRiesgo.Nominal.doubleValue());
					celdaDifi.setCellStyle(estiloNumero);

					celdaDifi = filaDifi.createCell(++columnCount);
					celdaDifi.setCellType(Cell.CELL_TYPE_NUMERIC);
					celdaDifi.setCellValue(dataDetalleRiesgo.TirTasa.doubleValue());
					celdaDifi.setCellStyle(estiloNumero);

					celdaDifi = filaDifi.createCell(++columnCount);
					celdaDifi.setCellValue(dataDetalleRiesgo.TipoReajuste);

					celdaDifi = filaDifi.createCell(++columnCount);
					celdaDifi.setCellValue(dataDetalleRiesgo.FechaVencimiento);

					celdaDifi = filaDifi.createCell(++columnCount);
					celdaDifi.setCellType(Cell.CELL_TYPE_NUMERIC);
					celdaDifi.setCellValue(dataDetalleRiesgo.ValorPresente.doubleValue());
					celdaDifi.setCellStyle(estiloNumero);

					celdaDifi = filaDifi.createCell(++columnCount);
					celdaDifi.setCellType(Cell.CELL_TYPE_NUMERIC);
					celdaDifi.setCellValue(dataDetalleRiesgo.Plazo.doubleValue());
					celdaDifi.setCellStyle(estiloNumero);

					celdaDifi = filaDifi.createCell(++columnCount);
					celdaDifi.setCellType(Cell.CELL_TYPE_NUMERIC);
					celdaDifi.setCellValue(dataDetalleRiesgo.Volatilidad.doubleValue());

					celdaDifi = filaDifi.createCell(++columnCount);
					celdaDifi.setCellType(Cell.CELL_TYPE_NUMERIC);
					celdaDifi.setCellValue(dataDetalleRiesgo.Duracion.doubleValue());
					celdaDifi.setCellStyle(estiloNumero);

					celdaDifi = filaDifi.createCell(++columnCount);
					celdaDifi.setCellType(Cell.CELL_TYPE_NUMERIC);
					celdaDifi.setCellValue(dataDetalleRiesgo.Var.doubleValue());
					celdaDifi.setCellStyle(estiloNumero);

					celdaDifi = filaDifi.createCell(++columnCount);
					celdaDifi.setCellType(Cell.CELL_TYPE_NUMERIC);
					celdaDifi.setCellValue(dataDetalleRiesgo.SecCar.doubleValue());
					celdaDifi.setCellStyle(estiloNumero);
				}

				archivoExcel.write(outputStream);
				logger.debug(archivoExcel);
				return true;
			}
		}
	}
}
