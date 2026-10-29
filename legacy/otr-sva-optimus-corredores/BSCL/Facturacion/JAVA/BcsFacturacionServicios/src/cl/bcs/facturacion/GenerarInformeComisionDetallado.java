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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.JsonObject;

import cl.bcs.facturacion.GenerarInformesComprobanteComisionesMensuales.EntradaGenerarInformesComprobanteComisionesMensuales;
import cl.bcs.facturacion.pojo.ReporteDetallado;
import cl.bcs.facturacion.pojo.ResumenReporteDetallado;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarInformeComisionDetallado implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(GenerarInformeMatrizComisiones.class);
	
	private final String SQL_SELECT_COMISION_CLIENTE_DETALLADO			= loadResourceToString("cl/bcs/facturacion/sql/GenerarInformeComprobanteComisionesMensuales_SelectComisionClienteDetallado.sql");
	private final String SQL_SELECT_RESUMEN_COMISION_CLIENTE_DETALLADO 	= loadResourceToString("cl/bcs/facturacion/sql/GenerarInformeComprobanteComisionesMensuales_SelectResumenComisionClienteDetallado.sql");

	private Integer columnaActual = 0;
	private Integer filaActual = 0;
	
	public void crearInformeComisionClienteDetallado(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada, Workbook archivoExcel, Sheet hoja) throws Exception{

		logger.info("INICIO::GenerarInformeComisionDetallado.crearInformeComisionClienteDetallado");
		Long inicioServicio = System.currentTimeMillis();
		
		GenerarInformeComprobanteComisionesMensualesUtils.fijarEstilos(archivoExcel);
		GenerarInformeComprobanteComisionesMensualesUtils.fijarAnchoColumnasInformeComisionDetallado(hoja);
		archivoExcel.setSheetName(0, Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_GENERAR_INFORME_COBRO_COMISIONES_MENSUALES").msg());
		
		GenerarInformeComprobanteComisionesMensualesUtils.agregarLogo(con, entrada.p_IdEmpresa, archivoExcel, hoja);

		Row fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(columnaActual);
		establecerCabeceraHoja(con, hoja, fila, entrada);
		
		List<ReporteDetallado> datosComisionDetallado = obtenerDatosComisionClienteDetallado(con, entrada);
		datosComisionDetallado = formatearDatosComisionClienteDetallado(con, entrada, datosComisionDetallado);
		
		if (datosComisionDetallado.isEmpty()) {
			fila = hoja.createRow(5);
			GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, 
					GenerarInformeComprobanteComisionesMensualesUtils.estiloTituloEncabezado, "", 0);
			String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_NO_EXISTE_INFORMACION_BD").msg();
			GenerarInformeComprobanteComisionesMensualesUtils.crearFilaMsgPersonalizado(hoja, celda, fila, mensaje, 1, 5 );
		}else {
			crearDetalleComisionClienteDetallado(con, entrada, datosComisionDetallado, hoja);
		}	
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeComisionDetallado.crearInformeComisionClienteDetallado", finServicio - inicioServicio);
	}
	
	private void establecerCabeceraHoja(Connection con, Sheet hoja, Row fila, EntradaGenerarInformesComprobanteComisionesMensuales entrada) throws Exception{
		
		logger.trace("INICIO::GenerarInformeComisionDetallado.establecerCabeceraHoja");
		Long inicioServicio = System.currentTimeMillis();
		
		columnaActual = 4;
		fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(columnaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloTituloEncabezado, 
				Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_CC_DETALLADO").msg(), columnaActual);
		fila = hoja.createRow(++filaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloCeldaSinBordes, "", columnaActual);
		fila = hoja.createRow(++filaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerSeccionFiltros(con, hoja, celda, fila, entrada);
		
		Long finServicio = System.currentTimeMillis();
		logger.trace("FIN::GenerarInformeComisionDetallado.establecerCabeceraHoja", finServicio - inicioServicio);
	}
	
	
	
	private void crearDetalleComisionClienteDetallado(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada, List<ReporteDetallado> listaComisionDetallado, Sheet hoja) throws Exception {
		
		logger.info("INICIO::GenerarInformeComisionDetallado.crearDetalleComisionClienteDetallado");
		Long inicioServicio = System.currentTimeMillis();
		filaActual = 5;
		Row fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(0);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloCeldaSinBordes, "", 0);
		
		String[] encabezado = {"UI_FECHA", "UI_RUT_CLIENTE", "UI_PORTAFOLIO", "UI_TIPO", "UI_NUMERO", "UI_FOLIO_DTE", "UI_MERCADO", "UI_MONTO_COMPRA", "UI_MONTO_VENTA",
							   "UI_IVA", "UI_COMISION", "UI_DERECHO", "UI_TOTAL"};
		
		String agruparSeccion = "";
		Double montoTotalCompra = 0d;
		Double montoTotalVenta = 0d;
		int columnaTotalCompra = ArrayUtils.indexOf(encabezado, "UI_MONTO_COMPRA");
		int columnaTotalVenta = ArrayUtils.indexOf(encabezado, "UI_MONTO_VENTA");
		
		Map<String, List<ResumenReporteDetallado>> resumenPorClientePortafolio = obtenerResumenComisionClienteDetallado(con, entrada, listaComisionDetallado);
		List<ResumenReporteDetallado> resumen = new ArrayList<ResumenReporteDetallado>();
		
		Integer i = 0;
		for (ReporteDetallado data : listaComisionDetallado) {
			columnaActual = 0;
			
			if (!data.AgruparSeccion.equals(agruparSeccion)) {
				
				if (!agruparSeccion.equals("")) {
					fila = hoja.createRow(++filaActual);
					establecerTotalColumna(hoja, celda, fila, montoTotalCompra, columnaTotalCompra, 0);
					establecerTotalColumna(hoja, celda, fila, montoTotalVenta, columnaTotalVenta, columnaTotalCompra+1);
					montoTotalCompra = 0d;
					montoTotalVenta = 0d;
					resumen = resumenPorClientePortafolio.get(agruparSeccion);
					crearSeccionResumen(con, hoja, entrada, resumen);
				}
				
				columnaActual = 0;
				fila = hoja.createRow(++filaActual);
				GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloSubTituloSeccion, "Tipo Contrato: " + data.TipoContrato + " Formato Cobro: " + data.FormatoCobro, columnaActual);
				fila = hoja.createRow(++filaActual);
				GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloSubTituloSeccion, "Rut: " + data.RutCliente + " Nombre: " + data.NombreCliente, columnaActual);
				fila = hoja.createRow(++filaActual);
				GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloSubTituloSeccion, "Portafolio: " + data.CodigoPortafolioComision, columnaActual);
				fila = hoja.createRow(++filaActual);
				GenerarInformeComprobanteComisionesMensualesUtils.crearEncabezadoSeccion(con, entrada.p_IdEmpresa, encabezado, celda, fila);
				agruparSeccion = data.AgruparSeccion;
			}
			
			 
			fila = hoja.createRow(++filaActual);
			
			//columna 0 (A)
			celda = fila.createCell(columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoDerecha);
			celda.setCellValue(data.FechaComprobante);
			
			//columna 2 (C)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoConSaltoLinea);
			celda.setCellValue(data.RutCliente);
			
			//columna 1 (B)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoCentro);
			celda.setCellValue(data.CodigoPortafolio);
			
			//columna 1 (B)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoCentro);
			celda.setCellValue(data.TipoComprobante);
			
			//columna 2 (C)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoDerecha);
			celda.setCellValue(data.NumeroComprobante);
			
			//columna 2 (C)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoDerecha);
			celda.setCellValue(data.FolioDte);

			//columna 3 (D)
			celda = fila.createCell(++columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoCentro);
			celda.setCellValue(data.IdClasificacionTipoInstrumento);

			//columna 4 (E)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			Double montoCompra = (data.Accion.equals("COMPRA")) ? data.MontoLiquidacion.doubleValue() : 0d;
			celda.setCellValue(montoCompra);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);
			montoTotalCompra += data.TipoComprobante.equals("NCE") ? -montoCompra : montoCompra;
			
			//columna 5 (F)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			Double montoVenta = (data.Accion.equals("VENTA")) ? data.MontoLiquidacion.doubleValue() : 0d;
			celda.setCellValue(montoVenta);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);
			montoTotalVenta += data.TipoComprobante.equals("NCE") ? -montoVenta : montoVenta;
			
			//columna 6 (G)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(data.MontoIVA.doubleValue());
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);
			
			//columna 7 (H)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(data.MontoComision.doubleValue());
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);
			
			//columna 8 (I)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(data.MontoDerecho.doubleValue());
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);
			
			//columna 9 (J)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			Double totalDocumento = montoCompra + montoVenta +
					data.MontoIVA.doubleValue() + 
					data.MontoComision.doubleValue() +
					data.MontoDerecho.doubleValue();
			
			celda.setCellValue(totalDocumento);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);
			GenerarInformeComprobanteComisionesMensualesUtils.completarColumnasEnBlanco(fila, columnaActual);
			
			i++;
			if (i == listaComisionDetallado.size()) {
				resumen = resumenPorClientePortafolio.get(agruparSeccion);
				crearSeccionResumen(con, hoja, entrada, resumen);
			}
		}
		
		fila = hoja.createRow(++filaActual);
		fila = hoja.createRow(++filaActual);

		establecerTotalColumna(hoja, celda, fila, montoTotalCompra, columnaTotalCompra, 0);
		establecerTotalColumna(hoja, celda, fila, montoTotalVenta, columnaTotalVenta, columnaTotalCompra+1);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloCeldaSinBordes, "", 0);
		
		String mensaje = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_MSG_FINAL_INFORME_CC").msg();
		GenerarInformeComprobanteComisionesMensualesUtils.crearFilaMsgPersonalizado(hoja,celda, fila, mensaje, 0, filaActual);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeComisionDetallado.crearDetalleComisionClienteDetallado", finServicio - inicioServicio);
	}
	
	private List<ReporteDetallado> obtenerDatosComisionClienteDetallado(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada) throws Exception {
		
		logger.info("INICIO::GenerarInformeComisionDetallado.obtenerDatosComisionClienteDetallado");
		Long inicioServicio = System.currentTimeMillis();
	
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdCliente", entrada.p_IdCliente);
		params.put("IdPortafolio", entrada.p_IdPortafolio);
		params.put("FechaDesde", entrada.p_FechaDesde);
		params.put("FechaHasta", entrada.p_FechaHasta);
		params.put("IdSegmentoCliente", entrada.p_IdSegmentoCliente);
		params.put("IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento);
		
		List<ReporteDetallado> listaComisionDetallado = sql.query(con, SQL_SELECT_COMISION_CLIENTE_DETALLADO, params, ReporteDetallado.class);
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeComisionDetallado.obtenerDatosComisionClienteDetallado", finServicio - inicioServicio);
		return listaComisionDetallado;
		
	}
	
	private List<ReporteDetallado> formatearDatosComisionClienteDetallado(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada, List<ReporteDetallado> datosComisionDetallado) throws Exception {
		
		logger.trace("INICIO::GenerarInformeComisionDetallado.formatearDatosComisionClienteDetallado");
		Long inicioServicio = System.currentTimeMillis();
		
		List<ReporteDetallado> datosComisionDetalladoFormateados = new ArrayList<ReporteDetallado>();
		
		List<String> monedas = datosComisionDetallado.stream().map(d -> d.IdMoneda).distinct().collect(Collectors.toList());
		HashMap<String,Object> hashParidad 	= obtenerParidadPorMoneda(con, entrada, monedas);
		
		BigDecimal impuesto = GenerarInformeComprobanteComisionesMensualesUtils.obtenerPorcentajeImpuesto(con, entrada).divide(new BigDecimal(100), MathContext.DECIMAL128);
		
		for(ReporteDetallado data: datosComisionDetallado) {
			
			CalculoSalida montoParidad = (CalculoSalida) hashParidad.get(data.IdMoneda);
			
			data.MontoLiquidacion = data.MontoLiquidacion.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			data.MontoComision = data.MontoComision.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			data.MontoDerecho = data.MontoDerecho.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			data.MontoIVA = (data.MontoComision.add(data.MontoDerecho)).multiply(impuesto);
			datosComisionDetalladoFormateados.add(data);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.trace("FIN::GenerarInformeComisionDetallado.formatearDatosComisionClienteDetallado", finServicio - inicioServicio);
		return datosComisionDetalladoFormateados;
		
	}
	
	private HashMap<String, Object> obtenerParidadPorMoneda(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada, List<String> monedas) throws Exception {
		
		logger.info("INICIO::GenerarInformeComisionDetallado.obtenerParidadPorMoneda");
		Long inicioServicio = System.currentTimeMillis();
		
		CalcularParidadSalida calcularParidad  	= GenerarInformeComprobanteComisionesMensualesUtils.calcularParidad(con, entrada, monedas, "CLP");
		HashMap<String,Object> hashParidad 		= Utils.List2Hash(calcularParidad.p_out_CalculosSalida, "IdMonedaOrigen");
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeComisionDetallado.obtenerParidadPorMoneda", finServicio - inicioServicio);
		
		return hashParidad;	
	}
	
	private Map<String, List<ResumenReporteDetallado>> obtenerResumenComisionClienteDetallado(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada, List<ReporteDetallado> comisionClienteDetallado) throws Exception {
		
		logger.info("INICIO::GenerarInformeComisionDetallado.obtenerResumenComisionClienteDetallado");
		Long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		List<Long> idsCliente = comisionClienteDetallado
				.stream()
				.map(c -> c.IdCliente).distinct()
				.collect(Collectors.toList());

		List<String> codigosPortafolio = comisionClienteDetallado
		.stream()
		.map(c -> c.CodigoPortafolio).distinct()
		.collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(6);
		params.put("IdCliente", idsCliente);
		params.put("CodigoPortafolio", codigosPortafolio);
		params.put("FechaDesde", entrada.p_FechaDesde);
		params.put("FechaHasta", entrada.p_FechaHasta);
		params.put("IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento);
		
		List<ResumenReporteDetallado> resumenComisionClienteDetallado = sql.query(con, SQL_SELECT_RESUMEN_COMISION_CLIENTE_DETALLADO, params, ResumenReporteDetallado.class);
		
		Map<String, List<ResumenReporteDetallado>> data = resumenComisionClienteDetallado.stream()
																.collect(Collectors.groupingBy(detalle -> detalle.AgruparSeccion));
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeComisionDetallado.obtenerResumenComisionClienteDetallado", finServicio - inicioServicio);
		return data;
	}
	
	private void crearSeccionResumen(Connection con, Sheet hoja, EntradaGenerarInformesComprobanteComisionesMensuales entrada,  List<ResumenReporteDetallado> resumen) throws Exception{
		Row fila = hoja.createRow(++filaActual);
		Cell celda = fila.createCell(0);
		
		fila = hoja.createRow(filaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloCeldaSinBordes, "", 0);
		List<ResumenReporteDetallado> resumenFormateado = formatearResumen(con, entrada, resumen);
		fila = hoja.createRow(++filaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloSubTituloSeccion, 
				Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_CUADRO_RESUMEN").msg().toUpperCase(), 0);
		fila = hoja.createRow(++filaActual);
		
		crearResumenComisionClienteDetallado(con, entrada, resumenFormateado, hoja);
		fila = hoja.createRow(++filaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloCeldaSinBordes, "", 0);
		fila = hoja.createRow(++filaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloCeldaSinBordes, "", 0);
	}
	
	private List<ResumenReporteDetallado> formatearResumen(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada, List<ResumenReporteDetallado> datosResumen) throws Exception {
		
		logger.trace("INICIO::GenerarInformeComisionDetallado.formatearResumen");
		Long inicioServicio = System.currentTimeMillis();
		
		List<String> monedas = datosResumen.stream().map(d -> d.IdMoneda).distinct().collect(Collectors.toList());
		HashMap<String,Object> hashParidad 	= obtenerParidadPorMoneda(con, entrada, monedas);
		
		Map<String, ResumenReporteDetallado> resumenMap = new HashMap<String, ResumenReporteDetallado>();
		
		for(ResumenReporteDetallado data: datosResumen) {
			
			CalculoSalida montoParidad = (CalculoSalida) hashParidad.get(data.IdMoneda);
			
			data.TotalComision = data.TotalComision.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			data.TotalCompra = data.TotalCompra.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			data.TotalVenta = data.TotalVenta.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			data.TotalDerecho = data.TotalDerecho.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			data.TotalIVA = data.TotalIVA.multiply(montoParidad.MontoLocal, MathContext.DECIMAL128);
			
			if(resumenMap.containsKey(data.IdClasificacionTipoInstrumento)) {
				ResumenReporteDetallado datosFormateados = resumenMap.get(data.IdClasificacionTipoInstrumento);
				datosFormateados = resumenCalcularTotales(data, datosFormateados);
				
			}else {
				ResumenReporteDetallado datosFormateados 		= new ResumenReporteDetallado();
				datosFormateados.TotalCompra					= BigDecimal.ZERO;
				datosFormateados.TotalVenta						= BigDecimal.ZERO;
				datosFormateados.TotalIVA						= BigDecimal.ZERO;
				datosFormateados.TotalComision					= BigDecimal.ZERO;
				datosFormateados.TotalDerecho					= BigDecimal.ZERO;
				datosFormateados.NombreInstrumento				= data.NombreInstrumento;
				datosFormateados.TipoComprobante				= data.TipoComprobante;
				datosFormateados.AgruparSeccion 				= data.AgruparSeccion;
				datosFormateados.IdMoneda						= data.IdMoneda;
				datosFormateados.IdClasificacionTipoInstrumento = data.IdClasificacionTipoInstrumento;
				datosFormateados = resumenCalcularTotales(data, datosFormateados);
				resumenMap.put(data.IdClasificacionTipoInstrumento, datosFormateados);
			}
		}
		
		List<ResumenReporteDetallado> datosResumenFormateados = new ArrayList<ResumenReporteDetallado>(resumenMap.values());
		
		Long finServicio = System.currentTimeMillis();
		logger.trace("FIN::GenerarInformeComisionDetallado.formatearResumen", finServicio - inicioServicio);
		return datosResumenFormateados;
		
	}
	
	private ResumenReporteDetallado resumenCalcularTotales (ResumenReporteDetallado dataOriginal, ResumenReporteDetallado dataEditada) {
		
		logger.info("INICIO::GenerarInformeComisionDetallado.resumenCalcularTotales");
		Long inicioServicio = System.currentTimeMillis();
		
		if (dataOriginal.TipoComprobante.equals("NCE")) {
			dataEditada.TotalCompra 		= dataEditada.TotalCompra.subtract(dataOriginal.TotalCompra);
			dataEditada.TotalVenta 			= dataEditada.TotalVenta.subtract(dataOriginal.TotalVenta);
			dataEditada.TotalIVA 			= dataEditada.TotalIVA.subtract(dataOriginal.TotalIVA);
			dataEditada.TotalComision 		= dataEditada.TotalComision.subtract(dataOriginal.TotalComision);
			dataEditada.TotalDerecho 		= dataEditada.TotalDerecho.subtract(dataOriginal.TotalDerecho);
		}else {		
			dataEditada.TotalCompra 		= dataEditada.TotalCompra.add(dataOriginal.TotalCompra);
			dataEditada.TotalVenta 			= dataEditada.TotalVenta.add(dataOriginal.TotalVenta);
			dataEditada.TotalIVA 			= dataEditada.TotalIVA.add(dataOriginal.TotalIVA);
			dataEditada.TotalComision 		= dataEditada.TotalComision.add(dataOriginal.TotalComision);
			dataEditada.TotalDerecho 		= dataEditada.TotalDerecho.add(dataOriginal.TotalDerecho);
		}
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeComisionDetallado.resumenCalcularTotales", finServicio - inicioServicio);
		return dataEditada;
	}

	private void crearResumenComisionClienteDetallado(Connection con, EntradaGenerarInformesComprobanteComisionesMensuales entrada, List<ResumenReporteDetallado> resumenDetalle, Sheet hoja) throws Exception {
		
		logger.info("INICIO::GenerarInformeComisionDetallado.crearResumenComisionClienteDetallado");
		Long inicioServicio = System.currentTimeMillis();
		
		Row fila = hoja.createRow(filaActual);
		Cell celda = fila.createCell(0);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTituloEnFila(hoja, celda, fila, GenerarInformeComprobanteComisionesMensualesUtils.estiloCeldaSinBordes, Diccionario.getMensaje(con, entrada.p_IdEmpresa, "UI_CUADRO_RESUMEN").msg(), 4);
		
		String[] encabezado = {"UI_MERCADO", "UI_COMPRA", "UI_VENTA", "UI_COMPRAVENTA", "UI_IVA", "UI_COMISION", "UI_DERECHO","UI_TOTAL"};
		
		GenerarInformeComprobanteComisionesMensualesUtils.crearEncabezadoSeccion(con, entrada.p_IdEmpresa, encabezado, celda, fila);
		
		BigDecimal totalGeneralCompra 		= BigDecimal.ZERO;
		BigDecimal totalGeneralVenta		= BigDecimal.ZERO;
		BigDecimal totalGeneralCompraVenta 	= BigDecimal.ZERO;
		BigDecimal totalGeneralIVA			= BigDecimal.ZERO;
		BigDecimal totalGeneralComision 	= BigDecimal.ZERO;
		BigDecimal totalGeneralDerecho		= BigDecimal.ZERO;
		BigDecimal totalGeneralDocumento	= BigDecimal.ZERO;
		Integer columnaTotalCompra			= ArrayUtils.indexOf(encabezado, "UI_COMPRA");
		Integer columnaTotalVenta 			= ArrayUtils.indexOf(encabezado, "UI_VENTA");
		Integer columnaTotalCompraVenta		= ArrayUtils.indexOf(encabezado, "UI_COMPRAVENTA");
		Integer columnaTotalIVA 			= ArrayUtils.indexOf(encabezado, "UI_IVA");
		Integer columnaTotalComision		= ArrayUtils.indexOf(encabezado, "UI_COMISION");
		Integer columnaTotalDerecho			= ArrayUtils.indexOf(encabezado, "UI_DERECHO");
		Integer columnaTotalDocumento		= ArrayUtils.indexOf(encabezado, "UI_TOTAL");
		
		for( ResumenReporteDetallado data : resumenDetalle) {
			columnaActual = 0;
			fila = hoja.createRow(++filaActual);
			
			//columna 0 (A)
			celda = fila.createCell(columnaActual);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTextoCentro);
			celda.setCellValue(data.NombreInstrumento);
			
			//columna 1 (B)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(data.TotalCompra.doubleValue());
			totalGeneralCompra = totalGeneralCompra.add(data.TotalCompra, MathContext.DECIMAL128);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);
			
			//columna 2 (C)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(data.TotalVenta.doubleValue());
			totalGeneralVenta = totalGeneralVenta.add(data.TotalVenta, MathContext.DECIMAL128);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);
			
			//columna 3 (D)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			BigDecimal totalCompraVenta = data.TotalCompra.add(data.TotalVenta, MathContext.DECIMAL128);
			celda.setCellValue(totalCompraVenta.doubleValue());
			totalGeneralCompraVenta = totalGeneralCompra.add(totalGeneralVenta);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);

			//columna 4 (E)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(data.TotalIVA.doubleValue());
			totalGeneralIVA = totalGeneralIVA.add(data.TotalIVA, MathContext.DECIMAL128);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);

			//columna 5 (F)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(data.TotalComision.doubleValue());
			totalGeneralComision = totalGeneralComision.add(data.TotalComision, MathContext.DECIMAL128);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);
			
			//columna 6 (G)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(data.TotalDerecho.doubleValue());
			totalGeneralDerecho = totalGeneralDerecho.add(data.TotalDerecho, MathContext.DECIMAL128);
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);
				
			//columna 7 (H)
			celda = fila.createCell(++columnaActual);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			BigDecimal totalFila = totalCompraVenta.add(data.TotalIVA, MathContext.DECIMAL128)
									.add(data.TotalComision, MathContext.DECIMAL128)
									.add(data.TotalDerecho, MathContext.DECIMAL128);
			totalGeneralDocumento = totalGeneralDocumento.add(totalFila);
			celda.setCellValue(totalFila.doubleValue());
			celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloNumero);
			
			GenerarInformeComprobanteComisionesMensualesUtils.completarColumnasEnBlanco(fila, columnaActual);
		}
		
		fila = hoja.createRow(++filaActual);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTotalColumna(hoja, celda, fila, totalGeneralCompra, columnaTotalCompra, 0);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTotalColumna(hoja, celda, fila, totalGeneralVenta, columnaTotalVenta, columnaTotalCompra + 1);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTotalColumna(hoja, celda, fila, totalGeneralCompraVenta, columnaTotalCompraVenta, columnaTotalVenta + 1);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTotalColumna(hoja, celda, fila, totalGeneralIVA, columnaTotalIVA, columnaTotalCompraVenta + 1);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTotalColumna(hoja, celda, fila, totalGeneralComision, columnaTotalComision, columnaTotalIVA + 1);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTotalColumna(hoja, celda, fila, totalGeneralDerecho, columnaTotalDerecho, columnaTotalComision + 1);
		GenerarInformeComprobanteComisionesMensualesUtils.establecerTotalColumna(hoja, celda, fila, totalGeneralDocumento, columnaTotalDocumento, columnaTotalDerecho + 1);
		
		
		Long finServicio = System.currentTimeMillis();
		logger.info("FIN::GenerarInformeComisionDetallado.crearResumenComisionClienteDetallado", finServicio - inicioServicio);
	}
	
	private void establecerTotalColumna(Sheet hoja, Cell celda, Row fila, double valor, Integer columnaTotal, Integer inicioColumna) {

		for (Integer j=inicioColumna; j<GenerarInformeComprobanteComisionesMensualesUtils.CANTIDAD_COLUMNAS_EN_HOJA+1; j++) {
			celda = fila.createCell(j);
			if (j == columnaTotal) {
				celda.setCellType(Cell.CELL_TYPE_NUMERIC);
				celda.setCellValue(valor);
				celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloTotalCampo);
				
			} else if (j == columnaTotal-1 && inicioColumna == 0) {
				CellStyle totalStyle = GenerarInformeComprobanteComisionesMensualesUtils.estiloCeldaSinBordes;
				totalStyle.setAlignment(CellStyle.ALIGN_RIGHT);
				celda.setCellValue("Total:");
				celda.setCellStyle(totalStyle);
				
			} else {
				celda.setCellStyle(GenerarInformeComprobanteComisionesMensualesUtils.estiloCeldaSinBordes);
			}
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
}
