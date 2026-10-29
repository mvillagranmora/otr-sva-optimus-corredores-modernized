package cl.bcs.normativa.circulares;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.util.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

import org.apache.poi.ss.usermodel.*;

import javax.sql.DataSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GenerarInformeMensualInversionistasRV13419 implements JavaCall {
	static final Logger logger = LogManager.getLogger(GenerarInformeMensualInversionistasRV13419.class);

	private final String SQL_SELECT_RAIZ_ARCHIVO = loadResourceToString(
			"cl/bcs/normativa/circulares/sql/GenerarInformeMensualInversionistasRV13419_SelectRaizArchivo.sql");

	public static class EntradaGenerarInformeMensualInversionistasRV13419 extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaInicio;
		public String p_FechaTermino;

		public transient DataSource p_DataSource;

	}

	public static class SalidaGenerarInformeMensualInversionistasRV13419 extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public String p_out_RutaCSV;

		public SalidaGenerarInformeMensualInversionistasRV13419() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}

	public static class RaizArchivo extends BcsPojo {
		public String RutaArchivo;
		public String RutaNOR;

		public RaizArchivo() {
			this.RutaNOR = "NOR/";
			this.RutaArchivo = "";
		}
	}

	public static class InversionistasRV13419 extends BcsPojo {
		public BigDecimal MontoComprobante;
		public String Accion;
		public String IdTipoInstrumento;
		public String EsExtranjero;
		public Timestamp FechaComprobante;
		public String IdTipoCliente;
		public Long FolioComprobante;
		public String Folio;
		public String RutCliente;
		public String Nacionalidad;
		public String EsCorredor;
		public String EsRelacionado;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}

	public SalidaGenerarInformeMensualInversionistasRV13419 logica(Connection conn,
			EntradaGenerarInformeMensualInversionistasRV13419 entrada) throws Exception {
		logger.info("INICIO::GenerarInformeMensualInversionistasRV13419");
		long inicio = System.currentTimeMillis();

		SalidaGenerarInformeMensualInversionistasRV13419 salida = new SalidaGenerarInformeMensualInversionistasRV13419();

		RaizArchivo raizArchivo = obtenerRaizArchivo(conn, entrada.p_IdEmpresa);
		if (StringUtils.isNullOrBlank(raizArchivo.RutaArchivo) || StringUtils.isNullOrBlank(raizArchivo.RutaNOR)) {
			salida.P_OUT_ERRORESCONTADOR++;
			salida.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR,
					"FALTA_CONFIGURACION_RUTA_ARCHIVO", new HashMap<>(), conn, entrada.p_IdEmpresa));
			return salida;
		}

		// final String rutaAbsolutaBase = "C:/BCS/pruebaExcel/";
		final String rutaAbsolutaBase = raizArchivo.RutaArchivo;

		File folder = new File(rutaAbsolutaBase);
		if (!folder.exists()) {
			folder.mkdirs();
		}

		List<ConsultarInversionistasRV13419.SalidaInvertsionistasRV> listaInversionistas = obtenerInversioniastasRV13419(
				conn, entrada);
		if (listaInversionistas.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_SIN_DATOS_INFORME_RV",
					new HashMap<>(), conn, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = Long.valueOf(salida.P_OUT_MENSAJES.size());
			return salida;
		}

		final String nombreArchivo = "Informe Inversionistas RV" + ".xlsx";
		final String rutaFinal = rutaAbsolutaBase + "/";

		try (Workbook archivoExcel = new XSSFWorkbook(); FileWriter fw = new FileWriter("mi ruta");) {

			// GENERACIÓN DE EXCEL
			Sheet hoja = archivoExcel.createSheet("BcsRptInfMensInvRV");

			String rutaFinalSalida = rutaFinal + nombreArchivo;
			FileOutputStream outputStream = new FileOutputStream(rutaFinalSalida);

			crearAnchoDeColumnaArchivo(hoja);
			/**/
			CellStyle estiloFecha = archivoExcel.createCellStyle();
			estiloFecha.setDataFormat(archivoExcel.createDataFormat().getFormat("mmm-yy"));
			Font fecha = archivoExcel.createFont();
			fecha.setFontName("Trebuchet MS");
			fecha.setFontHeightInPoints((short) 6);
			estiloFecha.setFont(fecha);
			estiloFecha.setAlignment(CellStyle.ALIGN_CENTER);
			estiloFecha.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			estiloFecha.setBorderTop(CellStyle.BORDER_MEDIUM);
			estiloFecha.setBorderLeft(CellStyle.BORDER_MEDIUM);
			estiloFecha.setBorderRight(CellStyle.BORDER_MEDIUM);
			estiloFecha.setBorderBottom(CellStyle.BORDER_MEDIUM);
			estiloFecha.setWrapText(true);

			CellStyle estiloTitulo = archivoExcel.createCellStyle();
			Font titulo = archivoExcel.createFont();
			titulo.setFontName("Trebuchet MS");
			titulo.setBold(true);
			titulo.setFontHeightInPoints((short) 16);
			estiloTitulo.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			estiloTitulo.setAlignment(CellStyle.ALIGN_LEFT);
			estiloTitulo.setFont(titulo);

			CellStyle estiloSubTitulo = archivoExcel.createCellStyle();
			Font subTitulo = archivoExcel.createFont();
			subTitulo.setBold(true);
			subTitulo.setFontName("Trebuchet MS");
			subTitulo.setFontHeightInPoints((short) 8);
			estiloSubTitulo.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			estiloSubTitulo.setAlignment(CellStyle.ALIGN_LEFT);
			estiloSubTitulo.setFont(subTitulo);

			CellStyle estiloEncabezado = archivoExcel.createCellStyle();
			Font encabezado = archivoExcel.createFont();
			encabezado.setFontName("Trebuchet MS");
			encabezado.setBold(true);
			encabezado.setFontHeightInPoints((short) 6);
			estiloEncabezado.setWrapText(true);
			estiloEncabezado.setFont(encabezado);
			estiloEncabezado.setAlignment(CellStyle.ALIGN_CENTER);
			estiloEncabezado.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			estiloEncabezado.setBorderTop(CellStyle.BORDER_MEDIUM);
			estiloEncabezado.setBorderLeft(CellStyle.BORDER_MEDIUM);
			estiloEncabezado.setBorderRight(CellStyle.BORDER_MEDIUM);
			estiloEncabezado.setBorderBottom(CellStyle.BORDER_MEDIUM);
			subTitulo.setFontHeightInPoints((short) 8);

			CellStyle estiloDatos = archivoExcel.createCellStyle();
			Font datos = archivoExcel.createFont();
			datos.setFontName("Trebuchet MS");
			datos.setFontHeightInPoints((short) 6);
			estiloDatos.setFont(datos);
			estiloDatos.setAlignment(CellStyle.ALIGN_CENTER);
			estiloDatos.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			estiloDatos.setBorderTop(CellStyle.BORDER_MEDIUM);
			estiloDatos.setBorderLeft(CellStyle.BORDER_MEDIUM);
			estiloDatos.setBorderRight(CellStyle.BORDER_MEDIUM);
			estiloDatos.setBorderBottom(CellStyle.BORDER_MEDIUM);
			estiloEncabezado.setWrapText(true);

			CellStyle estiloNumeros = archivoExcel.createCellStyle();
			Font numeros = archivoExcel.createFont();
			numeros.setFontName("Trebuchet MS");
			numeros.setFontHeightInPoints((short) 6);
			estiloNumeros.setFont(numeros);
			estiloNumeros.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			estiloNumeros.setWrapText(true);
			estiloNumeros.setBorderTop(CellStyle.BORDER_MEDIUM);
			estiloNumeros.setBorderLeft(CellStyle.BORDER_MEDIUM);
			estiloNumeros.setBorderRight(CellStyle.BORDER_MEDIUM);
			estiloNumeros.setBorderBottom(CellStyle.BORDER_MEDIUM);
			estiloNumeros.setDataFormat(archivoExcel.createDataFormat().getFormat("#,##0"));

			CellStyle borde = archivoExcel.createCellStyle();
			borde.setBorderBottom(CellStyle.BORDER_MEDIUM);

			//
			hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));
			hoja.addMergedRegion(new CellRangeAddress(2, 2, 0, 3));

			hoja.addMergedRegion(new CellRangeAddress(6, 6, 1, 2));
			hoja.addMergedRegion(new CellRangeAddress(6, 6, 3, 5));
			hoja.addMergedRegion(new CellRangeAddress(6, 6, 6, 8));
			hoja.addMergedRegion(new CellRangeAddress(6, 6, 10, 12));
			hoja.addMergedRegion(new CellRangeAddress(6, 6, 13, 15));
			hoja.addMergedRegion(new CellRangeAddress(6, 6, 17, 19));
			hoja.addMergedRegion(new CellRangeAddress(6, 6, 20, 22));
			hoja.addMergedRegion(new CellRangeAddress(6, 6, 24, 26));
			hoja.addMergedRegion(new CellRangeAddress(6, 6, 27, 29));
			hoja.addMergedRegion(new CellRangeAddress(6, 6, 31, 33));
			hoja.addMergedRegion(new CellRangeAddress(6, 6, 34, 36));
			/**/

			// INICIACION DE FILA Y COLUMNA//
			int filaCount = 1;
			int columnCount = 0;

			Row fila = hoja.createRow(filaCount);
			Cell celda = fila.createCell(columnCount);

			// TITULO//
			celda.setCellValue("Informe Mensual Inversionistas RV");
			celda.setCellStyle(estiloTitulo);

			celda = fila.createCell(columnCount = 1);

			// BORDE INFERIOR//
			columnCount = 0;
			fila = hoja.createRow(filaCount = 5);

			while (columnCount < 38) {
				celda = fila.createCell(columnCount);
				celda.setCellStyle(borde);
				columnCount++;
			}

			// ENCABEZADO//
			columnCount = 0;
			fila = hoja.createRow(++filaCount);

			fila = hoja.createRow(filaCount = 6);
			celda = fila.createCell(columnCount = 0);
			celda.setCellValue("Fecha");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 1);
			celda.setCellValue("Identificación corredor");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 3);
			celda.setCellValue("Montos Clientes Extranjeros No Mila");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 6);
			celda.setCellValue("Negocios Clientes Extranjeros No Mila");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 9);
			celda.setCellValue("Clientes Ext. No Mila");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 10);
			celda.setCellValue("Montos Clientes Extranjeros Mila");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 13);
			celda.setCellValue("Negocios Clientes Extranjeros Mila");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 16);
			celda.setCellValue("Clientes Extranjeros Mila");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 17);
			celda.setCellValue("Montos Clientes Institucionales Locales");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 20);
			celda.setCellValue("Negocios Clientes Institucionales Locales");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 23);
			celda.setCellValue("Clientes  Inst. Locales");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 24);
			celda.setCellValue("Montos Clientes No Institucionales Locales");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 27);
			celda.setCellValue("Negocios Clientes No Institucionales Locales");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 30);
			celda.setCellValue("Clientes  No Inst. Locales");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 31);
			celda.setCellValue("Montos Clientes Cuenta Propia y Relacionados");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 34);
			celda.setCellValue("Negocios Clientes  Cuenta Propia y Relacionados");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(columnCount = 37);
			celda.setCellValue("Clientes  Cta. Propia y Relacionados");
			celda.setCellStyle(estiloEncabezado);

			// Encabezado Detalle
			fila = hoja.createRow(filaCount = 7);
			columnCount = 0;
			celda = fila.createCell(columnCount);
			celda.setCellValue("Mes/Año");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Nombre");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Código");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Total");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Compras");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Ventas");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Total");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Compras");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Ventas");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Número");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Total");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Compras");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Ventas");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Total");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Compras");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Ventas");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Número");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Total");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Compras");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Ventas");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Total");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Compras");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Ventas");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Número");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Total");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Compras");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Ventas");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Total");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Compras");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Ventas");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Número");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Total");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Compras");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Ventas");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Total");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Compras");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Ventas");
			celda.setCellStyle(estiloEncabezado);

			celda = fila.createCell(++columnCount);
			celda.setCellValue("Número");
			celda.setCellStyle(estiloEncabezado);

			// DATOS//
			for (ConsultarInversionistasRV13419.SalidaInvertsionistasRV item : listaInversionistas) {
				columnCount = 0;
				fila = hoja.createRow(filaCount = 2);
				celda = fila.createCell(columnCount);
				celda.setCellValue("Periodo: " + item.Periodo + " (M$)");
				celda.setCellStyle(estiloSubTitulo);

				fila = hoja.createRow(filaCount = 8);
				columnCount = 0;
				celda = fila.createCell(columnCount);
				celda.setCellValue(item.Periodo);
				celda.setCellStyle(estiloFecha);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.NombreCorredor);
				celda.setCellStyle(estiloDatos);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.CodigoCorredor);
				celda.setCellStyle(estiloDatos);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesExtranjerosNoMilaMontoTotal.doubleValue());
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesExtranjerosNoMilaMontoCompras.doubleValue());
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesExtranjerosNoMilaMontoVentas.doubleValue());
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesExtranjerosNoMilaCantidadTotal);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesExtranjerosNoMilaCantidadCompras);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesExtranjerosNoMilaCantidadVentas);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesExtranjerosNoMilaCantidadClientes);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesExtranjerosMilaMontoTotal.doubleValue());
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesExtranjerosMilaMontoCompras.doubleValue());
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesExtranjerosMilaMontoVentas.doubleValue());
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesExtranjerosMilaCantidadTotal);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesExtranjerosMilaCantidadCompras);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesExtranjerosMilaCantidadVentas);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesExtranjerosMilaCantidadClientes);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesInstitucionalesNacionalesMontoTotal.doubleValue());
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesInstitucionalesNacionalesMontoCompras.doubleValue());
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesInstitucionalesNacionalesMontoVentas.doubleValue());
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesInstitucionalesNacionalesCantidadTotal);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesInstitucionalesNacionalesCantidadCompras);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesInstitucionalesNacionalesCantidadVentas);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesInstitucionalesNacionalesCantidadClientes);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesNoInstitucionalesNacionalesMontoTotal.doubleValue());
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesNoInstitucionalesNacionalesMontoCompras.doubleValue());
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesNoInstitucionalesNacionalesMontoVentas.doubleValue());
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesNoInstitucionalesNacionalesCantidadTotal);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesNoInstitucionalesNacionalesCantidadCompras);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesNoInstitucionalesNacionalesCantidadVentas);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.ClientesNoInstitucionalesNacionalesCantidadClientes);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.CuentaPropiaYRelacionadosMontoTotal.doubleValue());
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.CuentaPropiaYRelacionadosMontoCompras.doubleValue());
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.CuentaPropiaYRelacionadosMontoVentas.doubleValue());
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.CuentaPropiaYRelacionadosCantidadTotal);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.CuentaPropiaYRelacionadosCantidadCompras);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.CuentaPropiaYRelacionadosCantidadVentas);
				celda.setCellStyle(estiloNumeros);

				celda = fila.createCell(++columnCount);
				celda.setCellValue(item.CuentaPropiaYRelacionadosCantidadClientes);
				celda.setCellStyle(estiloNumeros);

			}

			archivoExcel.write(outputStream);
			archivoExcel.close();

		} catch (Exception e) {

		}

		salida.p_out_RutaCSV = raizArchivo.RutaNOR + nombreArchivo;

		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeMensualInversionistasRV13419 {}", fin - inicio);

		return salida;
	}

	public List<ConsultarInversionistasRV13419.SalidaInvertsionistasRV> obtenerInversioniastasRV13419(Connection conn,
			EntradaGenerarInformeMensualInversionistasRV13419 entrada) throws Exception {
		logger.info("INICIO::GenerarInformeMensualInversionistasRV13419.obtenerInversioniastasRV13419");
		long inicio = System.currentTimeMillis();

		/* LLAMAMOS LOGICA DEL CONSULTAR INVERSIONISTAS */
		ConsultarInversionistasRV13419 consulInversionistasRV13419 = new ConsultarInversionistasRV13419();
		ConsultarInversionistasRV13419.EntradaConsultarInversionistasRV13419 in = new ConsultarInversionistasRV13419.EntradaConsultarInversionistasRV13419();
		in.p_IdEmpresa = entrada.p_IdEmpresa;
		in.p_IdUsuario = entrada.p_IdUsuario;
		in.p_IdModulo = entrada.p_IdModulo;
		in.p_FechaInicio = entrada.p_FechaInicio;
		in.p_FechaTermino = entrada.p_FechaTermino;

		ConsultarInversionistasRV13419.SalidaConsultarInversionistasRV13419 salidaConsulInversionistasRV13419 = consulInversionistasRV13419
				.logica(conn, in);
		List<ConsultarInversionistasRV13419.SalidaInvertsionistasRV> listadoInversionistas = salidaConsulInversionistasRV13419.P_OUT_TABLA;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarInformeMensualInversionistasRV13419.obtenerInversioniastasRV13419", fin - inicio);
		return listadoInversionistas;

	}

	public void crearAnchoDeColumnaArchivo(Sheet hoja) {
		logger.info("INICIO::GenerarInformeMensualInversionistasRV13419.crearAnchoDeColumnaArchivo");
		long inicio = System.currentTimeMillis();

		int ancho6 = 1707;
		int ancho1271 = 3439;
		int ancho871 = 2431;

		hoja.setColumnWidth(0, ancho6);
		hoja.setColumnWidth(1, 6230);
		hoja.setColumnWidth(2, ancho6);
		hoja.setColumnWidth(3, ancho1271);
		hoja.setColumnWidth(4, ancho1271);
		hoja.setColumnWidth(5, ancho1271);
		hoja.setColumnWidth(6, ancho1271);
		hoja.setColumnWidth(7, ancho871);
		hoja.setColumnWidth(8, ancho871);
		hoja.setColumnWidth(9, 2074);
		hoja.setColumnWidth(10, ancho1271);
		hoja.setColumnWidth(11, ancho1271);
		hoja.setColumnWidth(12, ancho1271);
		hoja.setColumnWidth(13, ancho1271);
		hoja.setColumnWidth(14, ancho871);
		hoja.setColumnWidth(15, ancho871);
		hoja.setColumnWidth(16, 2955);
		hoja.setColumnWidth(17, ancho1271);
		hoja.setColumnWidth(18, ancho1271);
		hoja.setColumnWidth(19, ancho1271);
		hoja.setColumnWidth(20, ancho1271);
		hoja.setColumnWidth(21, ancho871);
		hoja.setColumnWidth(22, ancho871);
		hoja.setColumnWidth(23, ancho871);
		hoja.setColumnWidth(24, ancho1271);
		hoja.setColumnWidth(25, ancho1271);
		hoja.setColumnWidth(26, ancho1271);
		hoja.setColumnWidth(27, ancho1271);
		hoja.setColumnWidth(28, ancho871);
		hoja.setColumnWidth(29, ancho871);
		hoja.setColumnWidth(30, ancho871);
		hoja.setColumnWidth(31, ancho1271);
		hoja.setColumnWidth(32, ancho1271);
		hoja.setColumnWidth(33, ancho1271);
		hoja.setColumnWidth(34, ancho1271);
		hoja.setColumnWidth(35, ancho871);
		hoja.setColumnWidth(36, ancho871);
		hoja.setColumnWidth(37, 4154);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::GenerarInformeMensualInversionistasRV13419.crearAnchoDeColumnaArchivo", fin - inicio);
	}

	public RaizArchivo obtenerRaizArchivo(Connection conn, Long idEmpresa) throws SQLException {
		logger.debug("INICIO::GenerarInformeMensualInversionistasRV13419.obtenerRaizArchivo");
		long inicio = System.currentTimeMillis();

		RaizArchivo rutaResuelta = new RaizArchivo();

		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdEmpresa", idEmpresa);

		RaizArchivo raiz = new SQL().query(conn, SQL_SELECT_RAIZ_ARCHIVO, params, RaizArchivo.class).stream()
				.findFirst().orElse(new RaizArchivo());
		rutaResuelta.RutaArchivo = raiz.RutaArchivo + raiz.RutaNOR;

		long fin = System.currentTimeMillis();
		logger.debug("INICIO::GenerarInformeMensualInversionistasRV13419.obtenerRaizArchivo {}", fin - inicio);

		return rutaResuelta;
	}

	public static void main(String[] args) {
		System.out.println("== INICIO TEST =");

		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			EntradaGenerarInformeMensualInversionistasRV13419 entrada = new EntradaGenerarInformeMensualInversionistasRV13419();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 9L;
			entrada.p_IdModulo = "NOR";
			entrada.p_FechaInicio = "2022-10-18";
			entrada.p_FechaTermino = "2022-10-18";
			entrada.p_DataSource = PlataformaUtils.getBOLSADatasource();

			SalidaGenerarInformeMensualInversionistasRV13419 salida = new GenerarInformeMensualInversionistasRV13419()
					.logica(conn, entrada);
			if (salida.P_OUT_ERRORESCONTADOR > 0) {
				conn.rollback();
			}

			System.out.println("ENTRADA");
			System.out.println(entrada);
			System.out.println("SALIDA");
			System.out.println(salida);

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("== FIN TEST ==");
	}
}
