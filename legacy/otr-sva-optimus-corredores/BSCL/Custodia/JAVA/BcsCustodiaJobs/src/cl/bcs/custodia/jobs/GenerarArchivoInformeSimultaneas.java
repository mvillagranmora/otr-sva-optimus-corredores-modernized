package cl.bcs.custodia.jobs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

public class GenerarArchivoInformeSimultaneas extends BcsJob implements JavaCall {
	private final Log log = new Log("CUS.GenerarArchivoInformeMayorMenorValor");
	
	private static final Integer DECIMALES_TASAS = 6;
	private static final Integer DECIMALES_PRECIOS = 4;
	private static final Integer DECIMALES_CANTIDAD_UF = 4;
	private static final Integer DECIMALES_CANTIDAD_USD_EUR = 2;
	private static final Integer DECIMALES_CANTIDAD_CLP = 0;
	private static final Integer SIN_DECIMALES = 0;
	private static final Integer DECIMALES_PORCENTAJES = 2;

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
	
	public static class ObjetosTotalizados {
		public Integer Posicion = 0;
		public BigDecimal Total = BigDecimal.ZERO;
		
		public ObjetosTotalizados(Integer posicion) {
			this.Posicion = posicion;
		}
		
		public void SumarTotal(BigDecimal monto) {
			this.Total = this.Total.add(monto);
		}
	}
	
	public String formatearFecha(String fecha) {
		return LocalDate.parse(fecha, DateTimeFormatter.ofPattern("yyyy-MM-dd")).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
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

	public boolean logica(Connection conn, boolean debug) throws InterruptedException, Throwable {
		running.set(true);

		log.write("Iniciado Generacion Archivo");

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

		log.write("Obtencion de variables");
		Long idEmpresa = debug ? 1L : Long.parseLong(this.getParam("IdEmpresa"));
		Long idUsuario = debug ? 97L : Long.parseLong(this.getParam("IdUsuario"));
		String idModulo = debug ? "CUS" : String.valueOf((this.getParam("IdModulo")));
		String tipoReporte = debug ? "INF_SIM" : String.valueOf(this.getParam("TipoReporte"));
		String fecha = debug ? "2025-09-01" : String.valueOf((this.getParam("Fecha")));
		String fechaValorizacion = debug ? "2025-09-02" : String.valueOf((this.getParam("FechaValorizacion")));
		String nombre = debug ? tipoReporte.concat(String.valueOf(Math.random())).concat(".xls") : String.valueOf((this.getParam("NombreArchivo")));
		String tipoValorizacion = debug ? "M" : String.valueOf((this.getParam("TipoValorizacion")));
		Long idCliente = debug ? 0L : Long.parseLong(this.getParam("IdCliente"));
		Long idPortafolio = debug ? 0L : Long.parseLong(this.getParam("IdPortafolio"));
		Long idSocioNegocio = debug ? 0L : Long.parseLong(this.getParam("IdSocioNegocio"));
		String idInforme = debug ? "CARTERA.SIMULTANEAMERC2025-09-012025-09-022025-09-01 12:56:40" : String.valueOf((this.getParam("IdInforme")));
		
		log.write("Validaciones");		
		
		//VALIDACION TIPO VALORIZACION		
		if (!tipoValorizacion.equals("M")) {
			System.out.println("ERROR AL GENERAR ARCHIVO, TIPO VALORIZACION NO PUEDE SER DISTINTA A MERCADO (M)");
			throw new Throwable();
		}
		
		//VALIDACION FECHA CONSULTA VS FECHAVALORIZACION
		LocalDate parsedFechaConsulta = LocalDate.parse(fecha);
		LocalDate parsedFechaValorizacion = LocalDate.parse(fechaValorizacion);
		
		if (parsedFechaValorizacion.isBefore(parsedFechaConsulta)) {
			System.out.println("ERROR AL GENERAR ARCHIVO, FECHA VALORIZACION NO PUEDE SER PREVIA A FECHA CONSULTA");
			throw new Throwable();
		}

		log.write("Asignacion Servicios");
		//LLAMA A SERVICIO CABECERA
		String callSPCabecera = "CALL \"BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPropiaMesaRFCuentaPropia\"(?,?,?,?,?,?,?,?,?,?)" ;
		//LLAMA A SERVICIO CUERPO
		String callSPTabla = "CALL \"BSCL.Custodia.CrystalReports.Servicios::ConsultarSimultaneasValorizadas\"(?,?,?,?)";

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
			HSSFSheet sheet = wb.createSheet("INFORME_SIMULTANEA");

			log.write("Ejecucion SP Cartera");
			boolean isResultSet = spTabla.execute();

			if (isResultSet) {
				ResultSet rs = spTabla.getResultSet();
				int contadorFila = 10;
				log.write("Armando tabla xls");
				this.escribirReporteSimultaneas(rs, sheet, contadorFila, sdf, styleFecha, wb);
				this.escribirParametrosFinancierosReporte(sheet, spCabecera);
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
	
	private void escribirReporteSimultaneas(ResultSet rs, HSSFSheet sheet, int contadorFila, SimpleDateFormat sdf,
			CellStyle styleFecha, HSSFWorkbook wb) throws SQLException, ParseException {	
		
		Map<Integer, CellStyle> estilosPorDecimal = new HashMap<>();
		Integer indiceFinal = 0;
		
		while (rs.next()) {
			int indice = 0;
			//CABECERA DE TABLA
			if (rs.isFirst()) {
				Row row = sheet.createRow(contadorFila++);
				row.createCell(indice++).setCellValue("Tipo Operacion");
				row.createCell(indice++).setCellValue("Corr. Contr.");
				row.createCell(indice++).setCellValue("Instrumento");
				row.createCell(indice++).setCellValue("Fecha Operacion");
				row.createCell(indice++).setCellValue("Fecha Inicio");
				row.createCell(indice++).setCellValue("Fecha Liqui");
				row.createCell(indice++).setCellValue("Fec Venc.");
				row.createCell(indice++).setCellValue("Folio Comprobante");
				row.createCell(indice++).setCellValue("Nro Dias");
				row.createCell(indice++).setCellValue("Condicion");
				row.createCell(indice++).setCellValue("Tasa");
				row.createCell(indice++).setCellValue("Cant. Acciones");
				row.createCell(indice++).setCellValue("Pre. Contado");
				row.createCell(indice++).setCellValue("Precio Actual");
				row.createCell(indice++).setCellValue("Monto Plazo");
				row.createCell(indice++).setCellValue("Precio Plazo");
				row.createCell(indice++).setCellValue("Valor Compra");
				row.createCell(indice++).setCellValue("Monto Actual");
				row.createCell(indice++).setCellValue("Interes Diario");
				row.createCell(indice++).setCellValue("Interes Acum.");
				row.createCell(indice++).setCellValue("Valor Presente");
				row.createCell(indice++).setCellValue("Cliente"); 
				row.createCell(indice++).setCellValue("Portafolio");
				row.createCell(indice++).setCellValue("Folio Cartera");
				row.createCell(indice++).setCellValue("Transaccion");
				row.createCell(indice++).setCellValue("Transaccion a Plazo");
				row.createCell(indice++).setCellValue("Tipo Instrumento");
				row.createCell(indice++).setCellValue("Agente");
				row.createCell(indice++).setCellValue("Bolsa");
				row.createCell(indice++).setCellValue("Flag Deterioro");
				row.createCell(indice++).setCellValue("Porcentaje Deterioro");
				row.createCell(indice++).setCellValue("Deterioro");
				row.createCell(indice++).setCellValue("Fase");
				
				indiceFinal += indice;
				indice = 0;
			}
			
			//CUERPO DE TABLA
			Row row = sheet.createRow(contadorFila++);
			
			//TIPO OPERACION
			row.createCell(indice++).setCellValue(rs.getString("IdTipoOperacion"));
			
			//CORREDOR CONTRAPARTE
			row.createCell(indice++).setCellValue(rs.getString("NombreCompleto"));
			
			//INSTRUMENTO
			row.createCell(indice++).setCellValue(rs.getString("NemoSVS"));
			
			//FECHA OPERACION
			row.createCell(indice++).setCellValue(rs.getString("FechaOperacion"));
			
			//FECHA INICIO
			row.createCell(indice++).setCellValue(rs.getString("FechaOperacion"));
			
			//FECHA LIQUIDACION
			row.createCell(indice++).setCellValue(rs.getString("FechaLiquidacion"));

			//FECHA VENCIMIENTO OPERACION
			row.createCell(indice++).setCellValue(rs.getString("FechaVencimientoOperacion"));

			//FOLIOCOMPROBANTE
			row.createCell(indice++).setCellValue(Long.parseLong(rs.getString("FolioComprobante")));
			
			//NUMERO DE DIAS
			row.createCell(indice++).setCellValue(rs.getString("NumeroDias"));
			
			//CONDICION LIQUIDACION
			row.createCell(indice++).setCellValue(String.format(rs.getString("Codigo")));
			
			//TASA TIR
			CellStyle formatoTasa =  this.obtenerEstiloNumerico(DECIMALES_TASAS, wb,estilosPorDecimal);
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("TasaCompra"), DECIMALES_TASAS, formatoTasa);
			
			//CANTIDAD ACCIONES
			Integer decimalesCantidad = this.obtenerDecimalesCantidad(rs.getString("IdMonedaInstrumento"));
			CellStyle formatoCantidad =  this.obtenerEstiloNumerico(decimalesCantidad, wb,estilosPorDecimal);
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("Cantidad"), decimalesCantidad, formatoCantidad);
			
			CellStyle formatoPrecio =  this.obtenerEstiloNumerico(DECIMALES_PRECIOS, wb,estilosPorDecimal);
			CellStyle formatoSinDecimal = this.obtenerEstiloNumerico(SIN_DECIMALES, wb,estilosPorDecimal);
			
			//PRECIO CONTADO
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("PrecioCompra"), DECIMALES_PRECIOS, formatoPrecio);
			
			//PRECIO ACTUAL
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("PrecioActual"), DECIMALES_PRECIOS, formatoPrecio);
			
			//MONTO PLAZO
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("MontoAPlazo"), SIN_DECIMALES, formatoSinDecimal);
			
			//PRECIO PLAZO
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("PrecioAPlazo"), DECIMALES_PRECIOS, formatoPrecio);
			
			//MONTO INICIAL 
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("MontoCompra"), SIN_DECIMALES, formatoSinDecimal);
			
			//MONTO ACTUAL
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("MontoActual"), SIN_DECIMALES, formatoSinDecimal);	
			
			//INTERES DIARIO
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("MontoInteres"), SIN_DECIMALES, formatoSinDecimal);			
			
			//INTERES ACUMULADO
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("MontoInteresCorrido"), SIN_DECIMALES, formatoSinDecimal);
			
			//VALOR PRESENTE
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("MontoProximo"), SIN_DECIMALES, formatoSinDecimal);
			
			//CLIENTE
			row.createCell(indice++).setCellValue(rs.getString("NombreCliente"));
			
			//PORTAFOLIO
			row.createCell(indice++).setCellValue(Integer.parseInt(rs.getString("CodigoPortafolio")));

			//FOLIO CARTERA
			row.createCell(indice++).setCellValue(Long.parseLong(rs.getString("FolioCartera")));
			
			//TRANSACCION
			row.createCell(indice++).setCellValue(Long.parseLong(rs.getString("PaloNormal")));	
			
			//TRANSACCION A PLAZO
			row.createCell(indice++).setCellValue(Long.parseLong(rs.getString("PaloPlazo")));	
			
			//TIPO INSTRUMENTO
			row.createCell(indice++).setCellValue(rs.getString("IdTipoInstrumento"));
			
			//AGENTE
			row.createCell(indice++).setCellValue(rs.getString("NombreAgente"));			
			
			//BOLSA
			row.createCell(indice++).setCellValue(rs.getString("IdBolsa"));
			
			//FLAG DETERIORO
			row.createCell(indice++).setCellValue(rs.getString("FlagDeterioro"));
			
			//PORCENTAJE DETERIORO
			CellStyle formatoPorcentaje =  this.obtenerEstiloNumerico(DECIMALES_PORCENTAJES, wb,estilosPorDecimal);
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("PorcentajeDeterioro"), DECIMALES_PORCENTAJES, formatoPorcentaje);
			
			//DETERIORO
			row.createCell(indice++).setCellValue(new BigDecimal(rs.getString("Deterioro")).doubleValue());
			
			//FASE
			row.createCell(indice++).setCellValue(new BigDecimal(rs.getString("Fase")).doubleValue());
		}
		
		for (int i = 0; i <= indiceFinal; i++) {
			sheet.autoSizeColumn(i);
		}
	}

	private void escribirParametrosFinancierosReporte(HSSFSheet sheet, CallableStatement spCabecera) throws SQLException {
		log.write("Ejecucion SP CarteraPropia");
		boolean isResultSet = spCabecera.execute();

		if (isResultSet) {
			ResultSet rs = spCabecera.getResultSet();

			int celdaInicio = 5;
			
			log.write("Escribiendo cabecero archivo xls");
			if (rs.next()) {
				// Titulo Libro
				Row row = sheet.createRow(1);
				Cell cell = row.createCell(celdaInicio);
				cell.setCellValue("TITULO:");
				cell = row.createCell(celdaInicio + 1);
				cell.setCellValue(String.format("CARTERA SIMULTANEAS AL %s VALORIZADA EL %s", rs.getString("Fecha"), rs.getString("FechaProximo")));				
				
				// Nombre
				row = sheet.createRow(2);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("NOMBRE:");
				cell = row.createCell(celdaInicio + 1);
				cell.setCellValue(rs.getString("NombreCliente"));

				// Portafolio
				row = sheet.createRow(3);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("PORTAFOLIO:");
				cell = row.createCell(celdaInicio + 1);
				cell.setCellValue(rs.getString("CodigoPropositoPortafolio"));

				// Rut
				row = sheet.createRow(4);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("RUT:");
				cell = row.createCell(celdaInicio + 1);
				cell.setCellValue(rs.getString("DocumentoIdentidad"));

				// Parametros fecha
				row = sheet.createRow(5);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("PARAMETROS AL:");
				cell = row.createCell(celdaInicio + 2);
				cell.setCellValue(rs.getString("Fecha"));
				cell = row.createCell(celdaInicio + 4);
				cell.setCellValue(rs.getString("FechaProximo"));

				// uf
				row = sheet.createRow(6);
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
				row = sheet.createRow(7);
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
				row = sheet.createRow(8);
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
	
	public void escribirFechaEnCelda(Cell cell, ResultSet rs, CellStyle styleFecha, SimpleDateFormat sdf, String campo) throws SQLException, ParseException {
		String fecha = formatearFecha(rs.getString(campo));
		if ("01-01-0001".equals(fecha) || "0001-01-01".equals(fecha)) {
			cell.setCellValue(fecha);
		} else {
			cell.setCellValue(sdf.parse(fecha));
		}
		cell.setCellStyle(styleFecha);		
	}

	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA ==");
		try {
			Connection con = PlataformaUtils.getPropertiesConnection("master02", "c:/optimus-corredores/connections.properties");
			GenerarArchivoInformeSimultaneas demo = new GenerarArchivoInformeSimultaneas();
			demo.logica(con, true);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA ==");
	}

}