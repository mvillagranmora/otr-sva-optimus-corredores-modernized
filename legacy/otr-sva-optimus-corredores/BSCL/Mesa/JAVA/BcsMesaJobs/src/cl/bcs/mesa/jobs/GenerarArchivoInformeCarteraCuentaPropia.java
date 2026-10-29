package cl.bcs.mesa.jobs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;

import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;

public class GenerarArchivoInformeCarteraCuentaPropia extends BcsJob implements JavaCall {
	private final Log log = new Log("MES.GenerarArchivoInformeCarteraPropia");
	
	private static final Integer DECIMALES_TASAS = 6;
	private static final Integer DECIMALES_PRECIOS = 2;
	private static final Integer DECIMALES_DURATION = 2;
	private static final Integer DECIMALES_CANTIDAD_UF = 4;
	private static final Integer DECIMALES_CANTIDAD_USD_EUR = 2;
	private static final Integer DECIMALES_CANTIDAD_CLP = 0;
	private static final Integer SIN_DECIMALES = 0;

	AtomicBoolean running = new AtomicBoolean(false);

	Function<String, String> coalesce = x -> null == x ? "0" : x;

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con, false);
	}

	@Override
	public void onError(Throwable t) {
		log.write(":GenerarArchivoInformeCarteraPropia:  ****** ERROR  *******");
		log.write(t);
		t.printStackTrace();
	}

	@Override
	public void pause() {
	}

	public boolean logica(Connection conn, boolean debug) throws InterruptedException, Throwable {
		running.set(true);

		log.write("Iniciado Generacion Archivo");

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

		log.write("Obtencion de variables");
		Long idEmpresa = debug ? 1L : Long.parseLong(this.getParam("IdEmpresa"));
		Long idUsuario = debug ? 97L : Long.parseLong(this.getParam("IdUsuario"));
		String idModulo = debug ? "MRF" : String.valueOf((this.getParam("IdModulo")));
		String tipoReporte = debug ? "CAR_PAC" : String.valueOf(this.getParam("TipoReporte"));
		String fecha = debug ? "2025-05-14" : String.valueOf((this.getParam("Fecha")));
		String fechaValorizacion = debug ? "2025-04-15" : String.valueOf((this.getParam("FechaValorizacion")));
		String nombre = debug ? tipoReporte.concat(String.valueOf(Math.random())).concat(".xls") : String.valueOf((this.getParam("NombreArchivo")));
		String tipoValorizacion = debug ? "M" : String.valueOf((this.getParam("TipoValorizacion")));
		Long idCliente = debug ? 0L : Long.parseLong(this.getParam("IdCliente"));
		Long idPortafolio = debug ? 0L : Long.parseLong(this.getParam("IdPortafolio"));
		Long idSocioNegocio = debug ? 1598L : Long.parseLong(this.getParam("IdSocioNegocio"));
		String idInforme = debug ? "CARTERA.PACTOMERC2025-05-142025-05-152025-05-14 16:51:59" : String.valueOf((this.getParam("IdInforme")));

		log.write("Asignacion Servicios");
		String callSPTabla = "";
		String callSPCabecera = "CALL \"BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPropiaMesaRFCuentaPropia\"(?,?,?,?,?,?,?,?,?,?)";
		if ("CAR_PRO".equals(tipoReporte)) {
			callSPTabla = "CALL \"BSCL.Mesa.CrystalReports.Servicios::MRF.ConsultaCarteraPropiaValorizada\"(?,?,?,?);";
		} else if ("CAR_PAC".equals(tipoReporte)) {
			callSPTabla = "CALL \"BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraDePactosValorizada_CuentaPropia\"(?,?,?,?)";
		}

		// Salida archivo
		String ruta = debug ? "C:\\__ARCHIVO_PRUEBA\\" : String.valueOf(this.getParam("RutaArchivo"));

		log.write("Comprobar la ruta");
		if (!new File(ruta).exists()) {
			File theDir = new File(ruta);
			theDir.mkdir();
		}

		log.write("Crear file salida");
		File file = new File(ruta.concat(nombre));

		log.write("Inicio Proceso");
		try (CallableStatement spTabla = conn.prepareCall(callSPTabla);
				CallableStatement spCabecera = conn.prepareCall(callSPCabecera);
				FileWriter fw = new FileWriter(file);
				CSVWriter cw = new CSVWriter(fw, ';');
				HSSFWorkbook wb = new HSSFWorkbook();
				FileOutputStream outputStream = new FileOutputStream(ruta.concat(nombre))) {

			// helper para estilos
			CreationHelper createHelper = wb.getCreationHelper();

			// estilo fecha
			CellStyle styleFecha = wb.createCellStyle();
			styleFecha.setDataFormat(createHelper.createDataFormat().getFormat("m/d/yy"));
			styleFecha.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
			styleFecha.setAlignment(CellStyle.ALIGN_CENTER);

			// parametros SP tabla
			spTabla.setString(1, idInforme);
			spTabla.setLong(2, idEmpresa);
			spTabla.setLong(3, idCliente);
			spTabla.setLong(4, idSocioNegocio);

			// parametros SP cabecera
			spCabecera.setLong(1, idEmpresa);
			spCabecera.setLong(2, idUsuario);
			spCabecera.setString(3, idModulo);
			spCabecera.setString(4, fecha);
			spCabecera.setString(5, fechaValorizacion);
			spCabecera.setString(6, tipoValorizacion);
			spCabecera.setLong(7, idCliente);
			spCabecera.setLong(8, idPortafolio);
			spCabecera.setLong(9, idSocioNegocio);
			spCabecera.setString(10, tipoReporte);

			log.write("Creacion wb salida");
			HSSFSheet sheet = wb.createSheet("CAR_PRO".equals(tipoReporte) ? "INFORME_CARTERA_PROPIA" : "INFORME_CARTERA_PACTOS");

			log.write("Ejecucion SP Cartera");
			boolean isResultSet = spTabla.execute();

			if (isResultSet) {
				ResultSet rs = spTabla.getResultSet();
				int contadorFila = 9;
				log.write("Armando tabla xls");
				if ("CAR_PRO".equals(tipoReporte)) {
					this.escribirReporteCarPro(rs, sheet, contadorFila, sdf, styleFecha, wb, tipoValorizacion);
				} else if ("CAR_PAC".equals(tipoReporte)) {
					this.escribirReporteCarPac(rs, sheet, contadorFila, sdf, styleFecha, wb);
				} else {
					log.write("El tipo de reporte no es valido");
				}

				this.escribirParametrosFinancierosReporte(sheet, spCabecera, tipoReporte, tipoValorizacion);

				log.write("Escribiendo xls");
				wb.write(outputStream);

				log.write("Cerrando recurso");
				rs.close();
			}
		} catch (Exception ex) {
			System.out.println("ERROR AL GENERAR ARCHIVO");
			ex.printStackTrace();
			throw new Throwable(ex);
		}

		running.set(false);
		return false;
	}
	
	private CellStyle obtenerEstiloNumerico (int cantidadDecimales, HSSFWorkbook workbook, Map<Integer, CellStyle> cache) {
		
		if (cache.containsKey(cantidadDecimales)) return cache.get(cantidadDecimales);
		
		DataFormat dataFormat = workbook.createDataFormat();
		CellStyle style = workbook.createCellStyle();
		StringBuilder pattern = new StringBuilder("#,##0");
		if (cantidadDecimales > 0 ) {
			pattern.append(".");
			for (int i = 0; i < cantidadDecimales; i++) {
	            pattern.append("0");
	        }
		}
		
		style.setDataFormat(dataFormat.getFormat(pattern.toString()));
		cache.put(cantidadDecimales, style);

	    return style;
	}
	
	private void formatearCeldaNumerica(Cell celda, String valor, int cantidadDecimales, CellStyle estiloNumerico) {
		celda.setCellValue(new BigDecimal(valor).setScale(cantidadDecimales, RoundingMode.HALF_UP).doubleValue());
		celda.setCellStyle(estiloNumerico);
	}
	
	private Integer obtenerDecimalesCantidad(String IdMoneda) {
		if (IdMoneda.equals("UF")) {
			return DECIMALES_CANTIDAD_UF;
		} else if (IdMoneda.equals("USD") || IdMoneda.equals("EUR")  ) {
			return DECIMALES_CANTIDAD_USD_EUR;
		} else {
			return DECIMALES_CANTIDAD_CLP;
		}
	}

	private void escribirReporteCarPro(ResultSet rs, HSSFSheet sheet, int contadorFila, SimpleDateFormat sdf,
			CellStyle styleFecha, HSSFWorkbook wb, String tipoValorizacion) throws SQLException, ParseException {
		
		Map<Integer, CellStyle> estilosPorDecimal = new HashMap<>();
		
		while (rs.next()) {
			int indice = 0;
			if (rs.isFirst()) {
				// escribir cabecera XLS
				Row row = sheet.createRow(contadorFila++);
				row.createCell(indice++).setCellValue("Tipo Cartera");
				row.createCell(indice++).setCellValue("Secuencia Cartera");
				row.createCell(indice++).setCellValue("Instrumento");
				row.createCell(indice++).setCellValue("Cat. IFRS");
				row.createCell(indice++).setCellValue("Emisor");
				row.createCell(indice++).setCellValue("Fun");
				row.createCell(indice++).setCellValue("Fecha Vct. Instrumento");
				row.createCell(indice++).setCellValue("Fecha Liquidacion");
				row.createCell(indice++).setCellValue("Dias Int/Vct");
				row.createCell(indice++).setCellValue("Fecha Compra");
				row.createCell(indice++).setCellValue("Duration");
				row.createCell(indice++).setCellValue("Cantidad");
				row.createCell(indice++).setCellValue("Monto Compra");
				row.createCell(indice++).setCellValue("Moneda Reajuste");
				row.createCell(indice++).setCellValue("Interes Diario");
				row.createCell(indice++).setCellValue("Reajuste");
				row.createCell(indice++).setCellValue("Precio Actual");
				row.createCell(indice++).setCellValue("Tasa");
				row.createCell(indice++).setCellValue("Valor Actual");
				row.createCell(indice++).setCellValue("Precio Proximo");
				String tipoValorizacionEscogida = tipoValorizacion.equals("M") ? "Tasa Mercado" : "Tasa Compra";
				row.createCell(indice++).setCellValue(tipoValorizacionEscogida);
				row.createCell(indice++).setCellValue("Valor Proximo");
				indice = 0;
			}

			// datos cuerpo XLS
			Row row = sheet.createRow(contadorFila++);
			String diferenciador = rs.getString("Diferenciador");
			String tipoCartera = "";
			if ("CG".equals(diferenciador)) {
				tipoCartera = "Cartera Garantia";
			} else if ("CD".equals(diferenciador)) {
				tipoCartera = "Cartera Disponible";
			} else if ("PD".equals(diferenciador)) {
				tipoCartera = "Cartera CRV Disponible";
			} else if ("PC".equals(diferenciador)) {
				tipoCartera = "Cartera Comprometida";
			} else if ("PP".equals(diferenciador)) {
				tipoCartera = "Cartera CRV Pacteada";
			}

			row.createCell(indice++).setCellValue(tipoCartera);
			row.createCell(indice++).setCellValue(new BigDecimal(rs.getString("FolioCartera")).longValue());
			row.createCell(indice++).setCellValue(rs.getString("Nemotecnico"));
			row.createCell(indice++).setCellValue(rs.getString("CodigoIFRS"));
			row.createCell(indice++).setCellValue(rs.getString("IdEmisor"));
			row.createCell(indice++).setCellValue(rs.getString("EsFungible"));

			Cell cell = row.createCell(indice++);
			String fechaCampo = rs.getString("FechaVencimientoInstrumento");
			if ("01-01-0001".equals(fechaCampo) || "0001-01-01".equals(fechaCampo)) {
				cell.setCellValue(fechaCampo);
			} else {
				cell.setCellValue(sdf.parse(fechaCampo));
			}
			cell.setCellStyle(styleFecha);

			cell = row.createCell(indice++);
			fechaCampo = rs.getString("FechaLiquidacion");
			if ("01-01-0001".equals(fechaCampo) || "0001-01-01".equals(fechaCampo)) {
				cell.setCellValue(fechaCampo);
			} else {
				cell.setCellValue(sdf.parse(fechaCampo));
			}
			cell.setCellStyle(styleFecha);

			row.createCell(indice++).setCellValue(new BigDecimal(rs.getString("PlazoEnDiasOperacion")).longValue());

			cell = row.createCell(indice++);
			fechaCampo = rs.getString("FechaOperacion");
			if ("01-01-0001".equals(fechaCampo) || "0001-01-01".equals(fechaCampo)) {
				cell.setCellValue(fechaCampo);
			} else {
				cell.setCellValue(sdf.parse(fechaCampo));
			}
			cell.setCellStyle(styleFecha);
			
			row.createCell(indice++).setCellValue(new BigDecimal(rs.getString("Duration")).doubleValue());
			
			cell = row.createCell(indice++);
			Integer decimalesCantidad = this.obtenerDecimalesCantidad(rs.getString("IdTipoReajuste"));			
			CellStyle formatoCantidad = this.obtenerEstiloNumerico(decimalesCantidad, wb,estilosPorDecimal);
			this.formatearCeldaNumerica(cell, rs.getString("Cantidad"), decimalesCantidad, formatoCantidad);
			
			cell = row.createCell(indice++);
			CellStyle formatoSinDecimal = this.obtenerEstiloNumerico(SIN_DECIMALES, wb,estilosPorDecimal);
			this.formatearCeldaNumerica(cell, rs.getString("MontoCompra"), SIN_DECIMALES, formatoSinDecimal);
			
			row.createCell(indice++).setCellValue(rs.getString("IdTipoReajuste"));
			
			cell = row.createCell(indice++);
			this.formatearCeldaNumerica(cell, rs.getString("Interes"), SIN_DECIMALES, formatoSinDecimal);
			
			cell = row.createCell(indice++);
			this.formatearCeldaNumerica(cell, rs.getString("Reajuste"), SIN_DECIMALES, formatoSinDecimal);
			
			cell = row.createCell(indice++);
			CellStyle formatoPrecio = this.obtenerEstiloNumerico(DECIMALES_PRECIOS, wb,estilosPorDecimal);
			if(!"IF".equals(rs.getString("IdClasificacionTipoInstrumento"))) {
				this.formatearCeldaNumerica(cell, rs.getString("PrecioActual"), DECIMALES_PRECIOS, formatoPrecio);
			}
			
			cell = row.createCell(indice++);
			CellStyle formatoTasa = this.obtenerEstiloNumerico(DECIMALES_TASAS, wb,estilosPorDecimal);
			this.formatearCeldaNumerica(cell, rs.getString("TasaCompra"), DECIMALES_TASAS, formatoTasa);
			
			cell = row.createCell(indice++);
			this.formatearCeldaNumerica(cell, rs.getString("MontoValorActual"), SIN_DECIMALES, formatoSinDecimal);
			
			cell = row.createCell(indice++);
			if(!"IF".equals(rs.getString("IdClasificacionTipoInstrumento"))) {
				this.formatearCeldaNumerica(cell, rs.getString("PrecioProximo"), DECIMALES_PRECIOS, formatoPrecio);
			}
			
			cell = row.createCell(indice++);
			this.formatearCeldaNumerica(cell, rs.getString("TasaProxima"), DECIMALES_TASAS, formatoTasa);
			
			cell = row.createCell(indice++);
			this.formatearCeldaNumerica(cell, rs.getString("MontoValorProximo"), SIN_DECIMALES, formatoSinDecimal);
			
			if (rs.isLast()) {
				log.write("Ancho Columnas");
				for (int i = 0; i < indice; i++) {
					sheet.autoSizeColumn(i);
				}
			}
		}

	}

	private void escribirReporteCarPac(ResultSet rs, HSSFSheet sheet, int contadorFila, SimpleDateFormat sdf,
			CellStyle styleFecha, HSSFWorkbook wb) throws SQLException, ParseException {
		
		Map<Integer, CellStyle> estilosPorDecimal = new HashMap<>();
		
		while (rs.next()) {
			int indice = 0;
			if (rs.isFirst()) {
				// escribir cabecera XLS
				Row row = sheet.createRow(contadorFila++);
				row.createCell(indice++).setCellValue("Operacion");
				row.createCell(indice++).setCellValue("Fecha");
				row.createCell(indice++).setCellValue("Rut Cliente");
				row.createCell(indice++).setCellValue("Portafolio Cliente");
				row.createCell(indice++).setCellValue("Nombre Cliente");
				row.createCell(indice++).setCellValue("Folio Operacion");
				row.createCell(indice++).setCellValue("Duration");
				row.createCell(indice++).setCellValue("Instrumento");
				row.createCell(indice++).setCellValue("Cat. IFRS");
				row.createCell(indice++).setCellValue("Fun");
				row.createCell(indice++).setCellValue("Emisor");
				row.createCell(indice++).setCellValue("Vencimiento Instrumento");
				row.createCell(indice++).setCellValue("Dia");
				row.createCell(indice++).setCellValue("Tasa");
				row.createCell(indice++).setCellValue("TR");
				row.createCell(indice++).setCellValue("Valor Nominal");
				row.createCell(indice++).setCellValue("Monto Operacion");
				row.createCell(indice++).setCellValue("Valor Pactado");
				row.createCell(indice++).setCellValue("Valor Anterior");
				row.createCell(indice++).setCellValue("Vencimiento Pacto");
				row.createCell(indice++).setCellValue("Interes Vencimiento");
				row.createCell(indice++).setCellValue("Reajuste");
				row.createCell(indice++).setCellValue("Interes Diario");
				row.createCell(indice++).setCellValue("Interes Acumulado");
				row.createCell(indice++).setCellValue("Valor Presente");
				indice = 0;
			}

			// cuerpo tabla excel
			Row row = sheet.createRow(contadorFila++);
			Cell cell = row.createCell(indice++);
			String diferenciador = rs.getString("Diferenciador");
			if ("PD".equals(diferenciador)) {
				cell.setCellValue("CRV");
			} else if ("PC".equals(diferenciador)) {
				cell.setCellValue("VRC");
			} else if ("PP".equals(diferenciador)) {
				cell.setCellValue("VRC-CRV");
			} else {
				cell.setCellValue(diferenciador);
			}

			cell = row.createCell(indice++);
			String fechaCampo = rs.getString("FechaOperacion");
			if ("01-01-0001".equals(fechaCampo) || "0001-01-01".equals(fechaCampo)) {
				cell.setCellValue(fechaCampo);
			} else {
				cell.setCellValue(sdf.parse(fechaCampo));
			}
			cell.setCellStyle(styleFecha);

			row.createCell(indice++).setCellValue(rs.getString("RutCliente"));
			
			String portafolioCliente = String.format("%s %s", rs.getString("PropositoPortafolio"), rs.getString("CodigoPortafolio"));
			row.createCell(indice++).setCellValue(portafolioCliente);

			row.createCell(indice++).setCellValue(rs.getString("NombreCliente"));
			row.createCell(indice++).setCellValue(new BigDecimal(rs.getString("FolioOperacion")).longValue());
			
			cell = row.createCell(indice++);
			CellStyle formatoDuration = this.obtenerEstiloNumerico(DECIMALES_DURATION, wb,estilosPorDecimal);
			this.formatearCeldaNumerica(cell, rs.getString("Duration"), DECIMALES_DURATION, formatoDuration);
			
			row.createCell(indice++).setCellValue(rs.getString("Instrumento"));
			row.createCell(indice++).setCellValue(rs.getString("CodigoIFRS"));
			row.createCell(indice++).setCellValue(rs.getString("EsFungible"));
			row.createCell(indice++).setCellValue(rs.getString("Emisor"));

			cell = row.createCell(indice++);
			fechaCampo = rs.getString("FechaVencimientoInstrumento");
			if ("01-01-0001".equals(fechaCampo) || "0001-01-01".equals(fechaCampo)) {
				cell.setCellValue(fechaCampo);
			} else {
				cell.setCellValue(sdf.parse(fechaCampo));
			}
			cell.setCellStyle(styleFecha);

			BigDecimal dia = new BigDecimal(rs.getString("Dia"));
			if (dia.compareTo(BigDecimal.ZERO) > 0) {
				row.createCell(indice++).setCellValue(dia.longValue());
			} else {
				row.createCell(indice++).setCellValue(0);
			}
			
			cell = row.createCell(indice++);
			CellStyle formatoTasa = this.obtenerEstiloNumerico(DECIMALES_TASAS, wb,estilosPorDecimal);
			this.formatearCeldaNumerica(cell, rs.getString("TasaActual"), DECIMALES_TASAS, formatoTasa);
			
			String IdTipoReajuste = rs.getString("IdTipoReajuste");
			row.createCell(indice++).setCellValue(IdTipoReajuste);		
			
			
			cell = row.createCell(indice++);
			Integer decimalesCantidad = this.obtenerDecimalesCantidad(IdTipoReajuste);
			CellStyle formatoCantidad =  this.obtenerEstiloNumerico(decimalesCantidad, wb,estilosPorDecimal);
			this.formatearCeldaNumerica(cell, rs.getString("Cantidad"), decimalesCantidad, formatoCantidad);
			
			cell = row.createCell(indice++);
			CellStyle formatoSinDecimales = this.obtenerEstiloNumerico(SIN_DECIMALES, wb, estilosPorDecimal);
			this.formatearCeldaNumerica(cell, rs.getString("MontoOperacion"), SIN_DECIMALES, formatoSinDecimales);
			
			cell = row.createCell(indice++);
			this.formatearCeldaNumerica(cell, rs.getString("MontoPactado"), SIN_DECIMALES, formatoSinDecimales);
			
			cell = row.createCell(indice++);
			this.formatearCeldaNumerica(cell, rs.getString("MontoAnterior"), SIN_DECIMALES, formatoSinDecimales);

			cell = row.createCell(indice++);
			fechaCampo = rs.getString("FechaVencimientoOperacion");
			if ("01-01-0001".equals(fechaCampo) || "0001-01-01".equals(fechaCampo)) {
				cell.setCellValue(fechaCampo);
			} else {
				cell.setCellValue(sdf.parse(fechaCampo));
			}
			cell.setCellStyle(styleFecha);
			
			cell = row.createCell(indice++);
			this.formatearCeldaNumerica(cell, rs.getString("InteresVencimiento"), SIN_DECIMALES, formatoSinDecimales);
			
			cell = row.createCell(indice++);
			this.formatearCeldaNumerica(cell, rs.getString("Reajuste"), SIN_DECIMALES, formatoSinDecimales);
			
			cell = row.createCell(indice++);
			this.formatearCeldaNumerica(cell, rs.getString("InteresDiario"), SIN_DECIMALES, formatoSinDecimales);
			
			cell = row.createCell(indice++);
			this.formatearCeldaNumerica(cell, rs.getString("InteresAcumulado"), SIN_DECIMALES, formatoSinDecimales);
			
			cell = row.createCell(indice++);
			this.formatearCeldaNumerica(cell, rs.getString("MontoPresente"), SIN_DECIMALES, formatoSinDecimales);
			
			if (rs.isLast()) {
				log.write("Ancho Columnas");
				for (int i = 0; i < indice; i++) {
					sheet.autoSizeColumn(i);
				}
			}

		}
		
	}

	private void escribirParametrosFinancierosReporte(HSSFSheet sheet, CallableStatement spCabecera, String tipoReporte,
			String tipoValorizacion) throws SQLException {
		log.write("Ejecucion SP CarteraPropiaMesaRF");
		boolean isResultSet = spCabecera.execute();

		if (isResultSet) {
			ResultSet rs = spCabecera.getResultSet();

			int celdaInicio = 0;

			if ("CAR_PRO".equals(tipoReporte)) {
				celdaInicio = 5;
			} else if ("CAR_PAC".equals(tipoReporte)) {
				celdaInicio = 2;
			}

			log.write("Escribiendo cabecero archivo xls");
			if (rs.next()) {
				// Titulo Libro
				Row row = sheet.createRow(0);
				Cell cell = row.createCell(celdaInicio);
				cell.setCellValue(String.format("CARTERA %s AL %s VALORIZADA A TASA %s",
						"CAR_PRO".equals(tipoReporte) ? "PROPIA" : "PACTOS", rs.getString("Fecha"),
						"M".equals(tipoValorizacion) ? "MERCADO" : "P".equals(tipoValorizacion) ? "PACTO" : "COMPRA"));

				// Nombre
				row = sheet.createRow(1);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("NOMBRE:");
				cell = row.createCell(celdaInicio + 1);
				cell.setCellValue(rs.getString("NombreCliente"));

				// Portafolio
				row = sheet.createRow(2);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("PORTAFOLIO:");
				cell = row.createCell(celdaInicio + 1);
				cell.setCellValue(rs.getString("CodigoPropositoPortafolio"));

				// Rut
				row = sheet.createRow(3);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("RUT:");
				cell = row.createCell(celdaInicio + 1);
				cell.setCellValue(rs.getString("DocumentoIdentidad"));

				// Parametros fecha
				row = sheet.createRow(4);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("PARAMETROS AL:");
				cell = row.createCell(celdaInicio + 2);
				cell.setCellValue(rs.getString("Fecha"));
				cell = row.createCell(celdaInicio + 4);
				cell.setCellValue(rs.getString("FechaProximo"));

				// uf
				row = sheet.createRow(5);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("U.F.");
				cell = row.createCell(celdaInicio + 1);
				BigDecimal valorUF = new BigDecimal(rs.getString("TipoCambioUf"));
				BigDecimal valorUFProximo = new BigDecimal(rs.getString("TipoCambioUfProximo"));
				cell.setCellValue(valorUF.doubleValue());
				cell = row.createCell(celdaInicio + 2);
				String fechaIndicador = "";
				if (valorUF.compareTo(BigDecimal.ZERO) > 0) {
					fechaIndicador = "(".concat(rs.getString("FechaUf")).concat(")");
				} else {
					fechaIndicador = "(Sin Informacion)";
				}
				cell.setCellValue(fechaIndicador);
				cell = row.createCell(celdaInicio + 3);
				cell.setCellValue(valorUFProximo.doubleValue());
				if (valorUFProximo.compareTo(BigDecimal.ZERO) > 0) {
					fechaIndicador = "(".concat(rs.getString("FechaUfProximo")).concat(")");
				} else {
					cell.setCellValue(valorUF.doubleValue());
				}
				cell = row.createCell(celdaInicio + 4);
				cell.setCellValue(fechaIndicador);

				// US
				row = sheet.createRow(6);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("US$");
				cell = row.createCell(celdaInicio + 1);
				BigDecimal valorDO = new BigDecimal(rs.getString("TipoCambioDolar"));
				BigDecimal valorDOProximo = new BigDecimal(rs.getString("TipoCambioDolarProximo"));
				cell.setCellValue(valorDO.doubleValue());
				cell = row.createCell(celdaInicio + 2);
				if (valorDO.compareTo(BigDecimal.ZERO) > 0) {
					fechaIndicador = "(".concat(rs.getString("FechaDolar")).concat(")");
				} else {
					fechaIndicador = "(Sin Informacion)";
				}
				cell.setCellValue(fechaIndicador);
				cell = row.createCell(celdaInicio + 3);
				cell.setCellValue(valorDOProximo.doubleValue());
				if (valorDOProximo.compareTo(BigDecimal.ZERO) > 0) {
					fechaIndicador = "(".concat(rs.getString("FechaDolarProximo")).concat(")");
				} else {
					fechaIndicador = "(Sin Informacion)";
				}
				cell = row.createCell(celdaInicio + 4);
				cell.setCellValue(fechaIndicador);

				// IVP
				row = sheet.createRow(7);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("I.V.P.");
				cell = row.createCell(celdaInicio + 1);
				BigDecimal valorIVP = new BigDecimal(rs.getString("TipoCambioIvp"));
				BigDecimal valorIVPProximo = new BigDecimal(rs.getString("TipoCambioIvpProximo"));
				cell.setCellValue(valorIVP.doubleValue());
				cell = row.createCell(celdaInicio + 2);
				if (valorIVP.compareTo(BigDecimal.ZERO) > 0) {
					fechaIndicador = "(".concat(rs.getString("FechaIvp")).concat(")");
				} else {
					fechaIndicador = "(Sin Informacion)";
				}
				cell.setCellValue(fechaIndicador);
				cell = row.createCell(celdaInicio + 3);
				cell.setCellValue(valorIVPProximo.doubleValue());
				if (valorIVPProximo.compareTo(BigDecimal.ZERO) > 0) {
					fechaIndicador = "(".concat(rs.getString("FechaIvpProximo")).concat(")");
				} else {
					fechaIndicador = "(Sin Informacion)";
				}
				cell = row.createCell(celdaInicio + 4);
				cell.setCellValue(fechaIndicador);
			}
		}
	}

	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA ==");
		try {
			Connection con = PlataformaUtils.getPropertiesConnection("master02", "c:/optimus-corredores/connections.properties");
			GenerarArchivoInformeCarteraCuentaPropia demo = new GenerarArchivoInformeCarteraCuentaPropia();
			demo.logica(con, true);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA ==");
	}

}