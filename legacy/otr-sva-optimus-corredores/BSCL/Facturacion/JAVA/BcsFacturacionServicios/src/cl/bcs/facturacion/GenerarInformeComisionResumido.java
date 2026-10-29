package cl.bcs.facturacion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.JsonObject;

import cl.bcs.facturacion.GenerarInformesComprobanteComisionesMensuales.EntradaGenerarInformesComprobanteComisionesMensuales;
import cl.bcs.facturacion.pojo.ReporteResumido;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInformeComisionResumido implements JavaCall {
	
	private final static Logger logger = LogManager.getLogger(GenerarInformeComisionResumido.class);
	private final String SQL_SELECT_COMISION_CLIENTE_RESUMIDO 	= loadResourceToString("cl/bcs/facturacion/sql/GenerarInformeComprobanteComisionesMensuales_SelectComisionClienteResumido.sql");

	private Integer columnaActual = 0;
	private Integer filaActual = 0;

	private CellStyle estiloTextoCentroBold;
	private CellStyle estiloTextoDerechaBold;
	private CellStyle estiloTextoIzquierdaBold;
	private CellStyle estiloTextoConSaltoLineaBold;
	private CellStyle estiloNumeroBold;

	public void crearInformeComisionClienteResumido(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada, Workbook archivoExcel, Sheet hoja) throws Exception{
		
		logger.info("INICIO::GenerarInformeComisionResumido.crearInformeComisionClienteResumido");
		Long inicioServicio = System.currentTimeMillis();
		
		GenerarInformeComprobanteComisionesMensualesUtils.fijarEstilos(archivoExcel);
		crearEstilosBold(archivoExcel);
		GenerarInformeComprobanteComisionesMensualesUtils.fijarAnchoColumnasHojaExcelPorDefecto(hoja);
		archivoExcel.setSheetName(0, Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_GENERAR_INFORME_COBRO_COMISIONES_MENSUALES").msg());
	
		GenerarInformeComprobanteComisionesMensualesUtils.agregarLogo(con, entrada.p_IdEmpresa, archivoExcel, hoja);

		Row fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(columnaActual);
		establecerCabeceraHoja(con, hoja, fila, entrada);
		
		List<ReporteResumido> datosComisionResumido = obtenerDatosComisionClienteResumido(con, entrada);
		datosComisionResumido = formatearDatosComisionClienteResumido(con, entrada, datosComisionResumido);
		
		if (datosComisionResumido.isEmpty()) {
			fila = hoja.createRow(5);
			GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, 
					GenerarInformeComprobanteComisionesMensualesUtils.estiloTituloEncabezado, "", 0);
			String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_NO_EXISTE_INFORMACION_BD").msg();
			GenerarInformeComprobanteComisionesMensualesUtils.crearFilaMsgPersonalizado(hoja, celda, fila, mensaje, 1, 5);
		}else {
			crearDetalleComisionClienteResumido(con, entrada.p_IdEmpresa, datosComisionResumido, hoja);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeComisionResumido.crearInformeComisionClienteResumido", finServicio - inicioServicio);
		
	}
	
	private void establecerCabeceraHoja(Connection con, Sheet hoja, Row fila, EntradaGenerarInformesComprobanteComisionesMensuales entrada) throws Exception{
		
		logger.trace("INICIO::GenerarInformeMatrizComisiones.establecerCabeceraHoja");
		Long inicioServicio = System.currentTimeMillis();
		
		columnaActual = 4;
		fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(columnaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloTituloEncabezado, 
				Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_CC_RESUMIDO").msg(), columnaActual);
		fila = hoja.createRow(++filaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloCeldaSinBordes, "", columnaActual);
		fila = hoja.createRow(++filaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerSeccionFiltros(con, hoja, celda, fila, entrada);
		
		Long finServicio = System.currentTimeMillis();
		logger.trace("FIN::GenerarInformeMatrizComisiones.establecerCabeceraHoja", finServicio - inicioServicio);
	}
	
	private List<ReporteResumido> obtenerDatosComisionClienteResumido(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada) throws Exception {
		
		logger.info("INICIO::GenerarInformeComisionResumido.obtenerDatosComisionClienteResumido");
		Long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdCliente", entrada.p_IdCliente);
		params.put("IdPortafolio", entrada.p_IdPortafolio);
		params.put("FechaDesde", entrada.p_FechaDesde);
		params.put("FechaHasta", entrada.p_FechaHasta);
		params.put("IdSegmentoCliente", entrada.p_IdSegmentoCliente);
		params.put("IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento);
		
		List<ReporteResumido> listadoComisionResumido = sql.query(con, SQL_SELECT_COMISION_CLIENTE_RESUMIDO, params, ReporteResumido.class);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeComisionResumido.obtenerDatosComisionClienteResumido", finServicio - inicioServicio);
		
		return listadoComisionResumido;
		
	}
	
	private HashMap<String, Object> obtenerParidadPorMoneda(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada, List<ReporteResumido> datosComisionResumido) throws Exception {
		
		logger.info("INICIO::GenerarInformeComisionResumido.obtenerParidadPorMoneda");
		Long inicioServicio = System.currentTimeMillis();
		
		List<String> monedas = datosComisionResumido.stream().map(d -> d.IdMoneda).distinct().collect(Collectors.toList());
	
		CalcularParidadSalida calcularParidad  	= GenerarInformeComprobanteComisionesMensualesUtils.calcularParidad(con, entrada, monedas, "CLP");
		HashMap<String,Object> hashParidad 		= Utils.List2Hash(calcularParidad.p_out_CalculosSalida, "IdMonedaOrigen");
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeComisionResumido.obtenerParidadPorMoneda", finServicio - inicioServicio);
		
		return hashParidad;
		
	}
	
	private List<ReporteResumido> formatearDatosComisionClienteResumido(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada, List<ReporteResumido> datosComisionResumido) throws Exception {
		
		logger.info("INICIO::GenerarInformeComisionResumido.formatearDatosComisionClienteResumido");
		Long inicioServicio = System.currentTimeMillis();
		
		HashMap<String,Object> hashParidad = obtenerParidadPorMoneda(con, entrada, datosComisionResumido);
				
		for(ReporteResumido data: datosComisionResumido) {
			
			CalculoSalida montoParidad = (CalculoSalida) hashParidad.get(data.IdMoneda);
			
			data.MontoAcciones = data.MontoAcciones.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			data.ComisionAcciones = data.ComisionAcciones.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			data.MontoIIF = data.MontoIIF.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			data.ComisionIIF = data.ComisionIIF.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			data.MontoRF = data.MontoRF.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			data.ComisionRF = data.ComisionRF.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			data.MontoMonetario = data.MontoMonetario.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			data.ComisionMonetario = data.ComisionMonetario.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			
		}
		
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeComisionResumido.formatearDatosComisionClienteResumido", finServicio - inicioServicio);
		
		return datosComisionResumido;
		
	}
	
	private ReporteResumido calcularTotales(ReporteResumido dataOriginal, ReporteResumido dataEditada) {
		
		logger.info("INICIO::GenerarInformeComisionResumido.calcularTotales");
		Long inicioServicio = System.currentTimeMillis();
		
		if(dataOriginal.TipoComprobante.equals("NCE")) {
			dataEditada.MontoAcciones 		= dataEditada.MontoAcciones.subtract(dataOriginal.MontoAcciones);
			dataEditada.MontoIIF 			= dataEditada.MontoIIF.subtract(dataOriginal.MontoIIF);
			dataEditada.ComisionIIF 		= dataEditada.ComisionIIF.subtract(dataOriginal.ComisionIIF);
			dataEditada.MontoRF 			= dataEditada.MontoRF.subtract(dataOriginal.MontoRF);
			dataEditada.ComisionRF 			= dataEditada.ComisionRF.subtract(dataOriginal.ComisionRF);
		}else {
			dataEditada.MontoAcciones 		= dataEditada.MontoAcciones.add(dataOriginal.MontoAcciones);
			dataEditada.ComisionAcciones 	= dataEditada.ComisionAcciones.add(dataOriginal.ComisionAcciones);
			dataEditada.MontoIIF 			= dataEditada.MontoIIF.add(dataOriginal.MontoIIF);
			dataEditada.ComisionIIF 		= dataEditada.ComisionIIF.add(dataOriginal.ComisionIIF);
			dataEditada.MontoRF 			= dataEditada.MontoRF.add(dataOriginal.MontoRF);
			dataEditada.ComisionRF 			= dataEditada.ComisionRF.add(dataOriginal.ComisionRF);
			dataEditada.MontoMonetario   	= dataEditada.MontoMonetario.add(dataOriginal.MontoMonetario);
			dataEditada.ComisionMonetario   = dataEditada.ComisionMonetario.add(dataOriginal.ComisionMonetario);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeComisionResumido.calcularTotales", finServicio - inicioServicio);
		return dataEditada;
		
	}
	private void crearDetalleComisionClienteResumido(Connection con, Long IdEmpresa, List<ReporteResumido> listaComisionResumido, Sheet hoja) throws Exception {

		logger.trace("INICIO::GenerarInformeComisionResumido.crearDetalleComisionClienteResumido");
		Long inicioServicio = System.currentTimeMillis();
		filaActual = 5;
		Row fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(0);
		
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloCeldaSinBordes, "", 0);
		
		String[] encabezado = {"UI_RUT_CLIENTE", "UI_PORTAFOLIO", "UI_NOMBRE", "UI_TIPO_CONTRATO", "UI_MONTO_ACCIONES", "UI_COMISION_ACCIONES", 
							   "UI_MONTO_IIF", "UI_COMISION_IIF", "UI_MONTO_RF", "UI_COMISION_RF", "UI_MONTO_MONETARIOS", "UI_COMISION_MONETARIOS"};
		
		fila = hoja.createRow(++filaActual);
		
		GenerarInformeComprobanteComisionesMensualesUtils.crearEncabezadoSeccion(con, IdEmpresa, encabezado, celda, fila);
		
		BigDecimal montoTotalMontoAcciones 		= BigDecimal.ZERO;
		BigDecimal montoTotalComisionAcciones 	= BigDecimal.ZERO;
		BigDecimal montoTotalMontoIIF 			= BigDecimal.ZERO;
		BigDecimal montoTotalComisionIIF 		= BigDecimal.ZERO;
		BigDecimal montoTotalMontoRF 			= BigDecimal.ZERO;
		BigDecimal montoTotalComisionRF 		= BigDecimal.ZERO;
		BigDecimal montoTotalMontoMonetario		= BigDecimal.ZERO;
		BigDecimal montoTotalComisionMonetario	= BigDecimal.ZERO;
		Integer columnaTotalMontoAcciones		= ArrayUtils.indexOf(encabezado, "UI_MONTO_ACCIONES");
		Integer columnaTotalComisionAcciones 	= ArrayUtils.indexOf(encabezado, "UI_COMISION_ACCIONES");
		Integer columnaTotalMontoIIF 			= ArrayUtils.indexOf(encabezado, "UI_MONTO_IIF");
		Integer columnaTotalComisionIIF 		= ArrayUtils.indexOf(encabezado, "UI_COMISION_IIF");
		Integer columnaTotalMontoRF 			= ArrayUtils.indexOf(encabezado, "UI_MONTO_RF");
		Integer columnaTotalComisionRF 			= ArrayUtils.indexOf(encabezado, "UI_COMISION_RF");
		Integer columnaTotalMontoMonetario		= ArrayUtils.indexOf(encabezado, "UI_MONTO_MONETARIOS");
		Integer columnaTotalComisionMonetario	= ArrayUtils.indexOf(encabezado, "UI_COMISION_MONETARIOS");
		
		
		for (ReporteResumido dataComisionClienteResumido : listaComisionResumido) {
			columnaActual = 0;
			
			fila = hoja.createRow(++filaActual);
			

			boolean esFilaBold = Integer.valueOf(2).equals(dataComisionClienteResumido.OrdenFila) && "AGRUPADO".equals(dataComisionClienteResumido.TipoContrato);
			CellStyle estiloTextoCentroFila = esFilaBold ? estiloTextoCentroBold : GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoCentro;
			CellStyle estiloTextoDerechaFila = esFilaBold ? estiloTextoDerechaBold : GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoDerecha;
			CellStyle estiloTextoIzquierdaFila = esFilaBold ? estiloTextoIzquierdaBold : GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoIzquierda;
			CellStyle estiloTextoConSaltoLineaFila = esFilaBold ? estiloTextoConSaltoLineaBold : GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoConSaltoLinea;
			CellStyle estiloNumeroFila = esFilaBold ? estiloNumeroBold : GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero;
			
			boolean sumaTotales = Integer.valueOf(2).equals(dataComisionClienteResumido.OrdenFila);
			
			//columna 0 (A)
			celda = fila.createCell(columnaActual);
			celda.setCellStyle(estiloTextoCentroFila);
			celda.setCellValue(dataComisionClienteResumido.RutCliente);
			
			//columna 1 (B)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCentroFila);
			celda.setCellValue(dataComisionClienteResumido.CodigoPortafolio);
			
			//columna 2 (C)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoIzquierdaFila);
			celda.setCellValue(dataComisionClienteResumido.NombreCliente);

			//columna 3 (D)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(estiloTextoCentroFila);
			celda.setCellValue(dataComisionClienteResumido.TipoContrato);

			//columna 5 (F)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(dataComisionClienteResumido.MontoAcciones.doubleValue());
			celda.setCellStyle(estiloNumeroFila);
			montoTotalMontoAcciones	= sumaTotales ? montoTotalMontoAcciones.add(dataComisionClienteResumido.MontoAcciones) : montoTotalMontoAcciones;
			
			//columna 6 (G)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(dataComisionClienteResumido.ComisionAcciones.doubleValue());
			celda.setCellStyle(estiloNumeroFila);
			montoTotalComisionAcciones = montoTotalComisionAcciones.add(dataComisionClienteResumido.ComisionAcciones);
			
			//columna 6 (H)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(dataComisionClienteResumido.MontoIIF.doubleValue());
			celda.setCellStyle(estiloNumeroFila);
			montoTotalMontoIIF = sumaTotales ? montoTotalMontoIIF.add(dataComisionClienteResumido.MontoIIF) : montoTotalMontoIIF;
			
			//columna 7 (I)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(dataComisionClienteResumido.ComisionIIF.doubleValue());
			celda.setCellStyle(estiloNumeroFila);
			montoTotalComisionIIF = montoTotalComisionIIF.add(dataComisionClienteResumido.ComisionIIF);
			
			//columna 8 (J)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(dataComisionClienteResumido.MontoRF.doubleValue());
			celda.setCellStyle(estiloNumeroFila);
			montoTotalMontoRF = sumaTotales ? montoTotalMontoRF.add(dataComisionClienteResumido.MontoRF) : montoTotalMontoRF;
			
			//columna 9 (K)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(dataComisionClienteResumido.ComisionRF.doubleValue());
			celda.setCellStyle(estiloNumeroFila);
			montoTotalComisionRF = montoTotalComisionRF.add(dataComisionClienteResumido.ComisionRF);
			
			//columna 10 (L)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(dataComisionClienteResumido.MontoMonetario.doubleValue());
			celda.setCellStyle(estiloNumeroFila);
			montoTotalMontoMonetario = sumaTotales ? montoTotalMontoMonetario.add(dataComisionClienteResumido.MontoMonetario) : montoTotalMontoMonetario;
			
			//columna 11 (M)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(dataComisionClienteResumido.ComisionMonetario.doubleValue());
			celda.setCellStyle(estiloNumeroFila);
			montoTotalComisionMonetario = montoTotalComisionMonetario.add(dataComisionClienteResumido.ComisionMonetario);
			
			GenerarInformeComprobanteComisionesMensualesUtils.completarColumnasEnBlanco(fila, columnaActual);
		}
		
		fila = hoja.createRow(++filaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloCeldaSinBordes, "", columnaActual);
		fila = hoja.createRow(++filaActual);
	
		
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTotalColumna(hoja, celda, fila, montoTotalMontoAcciones, columnaTotalMontoAcciones, 0);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTotalColumna(hoja, celda, fila, montoTotalComisionAcciones, columnaTotalComisionAcciones, columnaTotalMontoAcciones + 1);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTotalColumna(hoja, celda, fila, montoTotalMontoIIF, columnaTotalMontoIIF, columnaTotalComisionAcciones + 1);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTotalColumna(hoja, celda, fila, montoTotalComisionIIF, columnaTotalComisionIIF, columnaTotalMontoIIF + 1);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTotalColumna(hoja, celda, fila, montoTotalMontoRF, columnaTotalMontoRF, columnaTotalComisionIIF + 1);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTotalColumna(hoja, celda, fila, montoTotalComisionRF, columnaTotalComisionRF, columnaTotalMontoRF + 1);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTotalColumna(hoja, celda, fila, montoTotalMontoMonetario, columnaTotalMontoMonetario, columnaTotalMontoMonetario + 1);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTotalColumna(hoja, celda, fila, montoTotalComisionMonetario, columnaTotalComisionMonetario, columnaTotalComisionMonetario + 1);
		
		fila = hoja.createRow(++filaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloCeldaSinBordes, "", columnaActual);
		String mensaje = Diccionario.getMensaje(con, IdEmpresa, "UI_MSG_FINAL_INFORME_CC").msg();
		GenerarInformeComprobanteComisionesMensualesUtils.crearFilaMsgPersonalizado(hoja,celda, fila, mensaje, 0, filaActual);
		
		Long finServicio = System.currentTimeMillis();
		logger.trace("FIN::GenerarInformeComisionResumido.crearDetalleComisionClienteResumido", finServicio - inicioServicio);
	}	

	private void crearEstilosBold(Workbook archivoExcel) {
	    Font fontBold = archivoExcel.createFont();
	    fontBold.setBoldweight(Font.BOLDWEIGHT_BOLD);

	    estiloTextoCentroBold = archivoExcel.createCellStyle();
	    estiloTextoCentroBold.cloneStyleFrom(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoCentro);
	    estiloTextoCentroBold.setFont(fontBold);

	    estiloTextoDerechaBold = archivoExcel.createCellStyle();
	    estiloTextoDerechaBold.cloneStyleFrom(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoDerecha);
	    estiloTextoDerechaBold.setFont(fontBold);
	    
	    estiloTextoIzquierdaBold = archivoExcel.createCellStyle();
	    estiloTextoIzquierdaBold.cloneStyleFrom(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoIzquierda);
	    estiloTextoIzquierdaBold.setFont(fontBold);

	    estiloTextoConSaltoLineaBold = archivoExcel.createCellStyle();
	    estiloTextoConSaltoLineaBold.cloneStyleFrom(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoConSaltoLinea);
	    estiloTextoConSaltoLineaBold.setFont(fontBold);

	    estiloNumeroBold = archivoExcel.createCellStyle();
	    estiloNumeroBold.cloneStyleFrom(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);
	    estiloNumeroBold.setFont(fontBold);
	}
	


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	

}
