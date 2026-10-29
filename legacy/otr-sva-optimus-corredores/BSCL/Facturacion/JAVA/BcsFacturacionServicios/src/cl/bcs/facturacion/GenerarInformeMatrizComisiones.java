package cl.bcs.facturacion;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.JsonObject;

import cl.bcs.facturacion.GenerarInformesComprobanteComisionesMensuales.EntradaGenerarInformesComprobanteComisionesMensuales;
import cl.bcs.facturacion.pojo.ReporteMatrizComisiones;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class GenerarInformeMatrizComisiones implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(GenerarInformeMatrizComisiones.class);
	private final String SQL_SELECT_MATRIZ_COMISIONES = loadResourceToString("cl/bcs/facturacion/sql/GenerarInformeComprobanteComisionesMensuales_SelectMatrizComisiones.sql");

	private Integer columnaActual = 0;
	private Integer filaActual = 0;
	
	public void crearInformeMatrizComisiones(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada, Workbook archivoExcel, Sheet hoja) throws Exception{
		
		logger.info("INICIO::GenerarInformeMatrizComisiones.crearInformeComisionClienteDetallado");
		Long inicioServicio = System.currentTimeMillis();
		
		GenerarInformeComprobanteComisionesMensualesUtils.fijarEstilos(archivoExcel);
		GenerarInformeComprobanteComisionesMensualesUtils.fijarAnchoColumnasHojaExcelPorDefecto(hoja);
		archivoExcel.setSheetName(0, Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_INFORME_COMISIONES_MENSUALES").msg());
		
		GenerarInformeComprobanteComisionesMensualesUtils.agregarLogo(con, entrada.p_IdEmpresa, archivoExcel, hoja);

		Row fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(columnaActual);
		establecerCabeceraHoja(con, hoja, fila, entrada);
	
		List<ReporteMatrizComisiones> datosMatrizComisiones = obtenerDatosMatrizComisiones(con, entrada);
		
		if (datosMatrizComisiones.isEmpty()) {
			fila = hoja.createRow(5);
			GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, 
					GenerarInformeComprobanteComisionesMensualesUtils.estiloTituloEncabezado, "", 0);
			String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_NO_EXISTE_INFORMACION_BD").msg();
			GenerarInformeComprobanteComisionesMensualesUtils.crearFilaMsgPersonalizado(hoja, celda, fila, mensaje, 1, 5);
		}else {
			crearDetalleMatrizComisiones(con, entrada.p_IdEmpresa, datosMatrizComisiones, hoja);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeMatrizComisiones.crearInformeComisionClienteDetallado", finServicio - inicioServicio);
	}
	
	private void establecerCabeceraHoja(Connection con, Sheet hoja, Row fila, EntradaGenerarInformesComprobanteComisionesMensuales entrada) throws Exception{
		
		logger.trace("INICIO::GenerarInformeMatrizComisiones.establecerTitulo");
		Long inicioServicio = System.currentTimeMillis();
		
		columnaActual = 4;
		fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(columnaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloTituloEncabezado, Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_MZ_COMISIONES").msg(), columnaActual);
		fila = hoja.createRow(++filaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloCeldaSinBordes, "", 0);
		fila = hoja.createRow(++filaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerSeccionFiltros(con, hoja, celda, fila, entrada);
		
		Long finServicio = System.currentTimeMillis();
		logger.trace("FIN::GenerarInformeMatrizComisiones.establecerTitulo", finServicio - inicioServicio);
	}
	
	
	private List<ReporteMatrizComisiones> obtenerDatosMatrizComisiones(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada) throws Exception {
		
		logger.info("INICIO::GenerarInformeMatrizComisiones.obtenerDatosMatrizComisiones");
		Long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdCliente", entrada.p_IdCliente);
		params.put("IdPortafolio", entrada.p_IdPortafolio);
		params.put("IdSegmentoCliente", entrada.p_IdSegmentoCliente);
		params.put("IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento);
		
		List<ReporteMatrizComisiones> listaMatrizComisiones = sql.query(con, SQL_SELECT_MATRIZ_COMISIONES, params, ReporteMatrizComisiones.class);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeMatrizComisiones.obtenerDatosMatrizComisiones", finServicio - inicioServicio);
		
		return listaMatrizComisiones;
		
	}
	
	private void crearDetalleMatrizComisiones(Connection con, Long IdEmpresa, List<ReporteMatrizComisiones> datosMatrizComisiones, Sheet hoja) throws Exception {
		
		logger.trace("INICIO::GenerarInformeMatrizComisiones.crearDetalleMatrizComisiones");
		Long inicioServicio = System.currentTimeMillis();
		
		filaActual = 5;
		Row fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(0);
		
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloCeldaSinBordes, "", 0);
		fila = hoja.createRow(++filaActual);
		String[] encabezado = {"UI_RUT_CLIENTE", "UI_PORTAFOLIO", "UI_NOMBRE", "UI_FORMATO_CONTRATO_COBRO", "UI_MERCADO", "UI_MONEDA", 
							   "UI_CONCEPTO", "UI_CLASIFICACION_ABR", "UI_TIPO", "UI_TIPO_CONTRATO", "UI_HASTA_MONTO", "UI_VALOR_COMISION", "UI_PORCENTAJE_COMISION"};
		
		GenerarInformeComprobanteComisionesMensualesUtils.crearEncabezadoSeccion(con, IdEmpresa, encabezado, celda, fila);
		
		for (ReporteMatrizComisiones data : datosMatrizComisiones) {
			columnaActual = 0;
			
			fila = hoja.createRow(++filaActual);
			
			celda = fila.createCell(columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoCentro);
			celda.setCellValue(data.RutCliente);
			
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoDerecha);
			celda.setCellValue(data.CodigoPortafolio);
			
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoConSaltoLinea);
			celda.setCellValue(data.NombreCliente);

			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoConSaltoLinea);
			celda.setCellValue(data.FormatoCobro);

			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoCentro);
			celda.setCellValue(data.IdClasificacionTipoInstrumento);
			
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoCentro);
			celda.setCellValue(data.NombreMoneda);
			
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoConSaltoLinea);
			celda.setCellValue(data.NombreConcepto);
			
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoCentro);
			celda.setCellValue(data.CodigoCobroClasificacion);
			
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoCentro);
			celda.setCellValue(data.NombreCobro);
			
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoCentro);
			celda.setCellValue(data.TipoContrato);

			celda = fila.createCell(++columnaActual);
			if(data.MontoMaximo != null) {
				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
				celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);
				celda.setCellValue(data.MontoMaximo.doubleValue());
			}else {
				celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoCentro);
				celda.setCellValue("");
			}
			
			celda = fila.createCell(++columnaActual);
			if(data.Monto != null) {
				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
				celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);
				celda.setCellValue(data.Monto.doubleValue());
			}else {
				celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoCentro);
				celda.setCellValue("");
			}
			
			celda = fila.createCell(++columnaActual);
			if(data.Porcentaje != null) {
				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
				celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloPorcentaje);
				celda.setCellValue(data.Porcentaje.doubleValue());
			}else {
				celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoCentro);
				celda.setCellValue("");
			}
			
		}
	
		fila = hoja.createRow(++filaActual);
		++filaActual;
		
		Long finServicio = System.currentTimeMillis();
		logger.trace("FIN::GenerarInformeMatrizComisiones.crearDetalleMatrizComisiones", finServicio - inicioServicio);
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	

}
