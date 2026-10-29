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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;

import com.google.gson.JsonObject;
import com.opencsv.CSVWriter;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;

public class GenerarArchivoInformeMayorMenorValor extends BcsJob implements JavaCall {
	private final Log log = new Log("CUS.GenerarArchivoInformeMayorMenorValor");
	
	private static final Integer DECIMALES_TASAS = 6;
	private static final Integer DECIMALES_PRECIOS = 2;
	private static final Integer DECIMALES_CANTIDAD_UF = 4;
	private static final Integer DECIMALES_CANTIDAD_USD_EUR = 2;
	private static final Integer DECIMALES_CANTIDAD_CLP = 0;
	private static final Integer SIN_DECIMALES = 0;
	private static final Integer DECIMALES_PORCENTAJES = 2;
	private static final Integer DECIMALES_CONVEXIDAD_DURATION = 2;

	private static final List<String> DIFERENCIADORES_PACTOS = Arrays.asList("PP", "PC", "PD");

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
		
		public void ReiniciarTotal() {
			this.Total = BigDecimal.ZERO;
		}
	}
	
	public String formatearFecha(String fecha) {
		return LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd-MM-yyyy")).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
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
		String tipoReporte = debug ? "MAME" : String.valueOf(this.getParam("TipoReporte"));
		String fecha = debug ? "2025-08-27" : String.valueOf((this.getParam("Fecha")));
		String fechaValorizacion = debug ? "2025-08-28" : String.valueOf((this.getParam("FechaValorizacion")));
		String nombre = debug ? tipoReporte.concat(String.valueOf(Math.random())).concat(".xls") : String.valueOf((this.getParam("NombreArchivo")));
		String tipoValorizacion = debug ? "M" : String.valueOf((this.getParam("TipoValorizacion")));
		Long idCliente = debug ? 0L : Long.parseLong(this.getParam("IdCliente"));
		Long idPortafolio = debug ? 0L : Long.parseLong(this.getParam("IdPortafolio"));
		Long idSocioNegocio = debug ? 0L : Long.parseLong(this.getParam("IdSocioNegocio"));
		String idInforme = debug ? "MAYOR.MENOR.VALOR.RV.MERC2025-08-292025-08-292025-07-25 16:52:22" : String.valueOf((this.getParam("IdInforme")));
		String IdTipoValorizacion = debug ? "RV" : String.valueOf(this.getParam("IdTipoValorizacion"));
		
		log.write("Validaciones");		
		//VALIDACION FECHA CONSULTA VS FECHAVALORIZACION
		if (!tipoValorizacion.equals("M")) {
			System.out.println("ERROR AL GENERAR ARCHIVO, TIPO VALORIZACION NO PUEDE SER DISTINTA A MERCADO (M)");
			throw new Throwable();
		}
		
		//VALIDACION TIPO VALORIZACION
		LocalDate parsedFechaConsulta = LocalDate.parse(fecha);
		LocalDate parsedFechaValorizacion = LocalDate.parse(fechaValorizacion);
		
		if (parsedFechaValorizacion.isBefore(parsedFechaConsulta)) {
			System.out.println("ERROR AL GENERAR ARCHIVO, FECHA VALORIZACION NO PUEDE SER PREVIA A FECHA CONSULTA");
			throw new Throwable();
		}

		log.write("Asignacion Servicios");
		//LLAMA A SERVICIO CABECERA
		String callSPCabecera = "CALL \"BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPropiaMesaRFCuentaPropia\"(?,?,?,?,?,?,?,?,?,?,?)";
		//LLAMA A SERVICIO CUERPO
		String callSPTabla = "CALL \"BSCL.Custodia.CrystalReports.Servicios::InformeMayorMenorValor\"(?,?)";

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
			
			// estilos totalizados
			CellStyle styleTotal = wb.createCellStyle();
			styleTotal.setFillBackgroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			styleTotal.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			styleTotal.setFillPattern(CellStyle.SOLID_FOREGROUND);
			DataFormat format = wb.createDataFormat();
			styleTotal.setDataFormat(format.getFormat("#,##0"));

			// parametros SP tabla
			spTabla.setString(1, idInforme);
			spTabla.setLong(2, idEmpresa);

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
			spCabecera.setString(11, IdTipoValorizacion);

			log.write("Creacion wb salida");
			HSSFSheet sheet = wb.createSheet("INFORME_MAYOR_MENOR_VALOR");

			log.write("Ejecucion SP Cartera");
			boolean isResultSet = spTabla.execute();

			if (isResultSet) {
				ResultSet rs = spTabla.getResultSet();
				int contadorFila = 11;
				log.write("Armando tabla xls");
				this.escribirReporteMayorMenorValor(rs, sheet, contadorFila, sdf, styleFecha, styleTotal,wb, IdTipoValorizacion);
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
	
	private void escribirReporteMayorMenorValor(ResultSet rs, HSSFSheet sheet, int contadorFila, SimpleDateFormat sdf,
			CellStyle styleFecha, CellStyle totalStyle, HSSFWorkbook wb, String IdTipoValorizacion) throws SQLException, ParseException {		
		
		//DIFERENCIADOR DE AGRUPACION
		String diferenciador = "";
		Boolean esPrimerGrupo = true;		
		HashMap<Integer, ObjetosTotalizados> totalizados = new HashMap<>();
		
		Map<Integer, CellStyle> estilosPorDecimal = new HashMap<>();
		
		Integer indiceFinal = 0;
		
		while (rs.next()) {
			int indice = 0;
			if (rs.isFirst()) {
				// CABECERA DE TABLA
				Row row = sheet.createRow(contadorFila++);
				row.createCell(indice++).setCellValue("");
				row.createCell(indice++).setCellValue("Tipo Cartera");
				row.createCell(indice++).setCellValue("Folio Cartera");
				row.createCell(indice++).setCellValue("Rut Cliente");
				row.createCell(indice++).setCellValue("Portafolio Cliente");
				row.createCell(indice++).setCellValue("Nombre Cliente");
				row.createCell(indice++).setCellValue("Tipo Instrumento");
				row.createCell(indice++).setCellValue("Nemotecnico");
				row.createCell(indice++).setCellValue("Clasificacion");
				row.createCell(indice++).setCellValue("Tipo Reajuste");
				row.createCell(indice++).setCellValue("Emisor");
				
				if("RF".equals(IdTipoValorizacion)) {
					row.createCell(indice++).setCellValue("Familia");
				}
				
				totalizados.put(indice , new ObjetosTotalizados(indice));
				row.createCell(indice++).setCellValue("Unidades");
				
				row.createCell(indice++).setCellValue("Tasa Compra");
				row.createCell(indice++).setCellValue("Precio Compra");
				row.createCell(indice++).setCellValue("Valor Compra");
				row.createCell(indice++).setCellValue("Valor Presente");
				row.createCell(indice++).setCellValue("Fecha Vcto");
				row.createCell(indice++).setCellValue("Mayor a siete dias"); 
				row.createCell(indice++).setCellValue("Plazo al Vencimiento");
				row.createCell(indice++).setCellValue("Tasa Mercado");
				row.createCell(indice++).setCellValue("Precio Mercado");
				
				if("RF".equals(IdTipoValorizacion)) {
					row.createCell(indice++).setCellValue("Duration");
				}
				
				row.createCell(indice++).setCellValue("Valor Mercado");
				
				totalizados.put(indice, new ObjetosTotalizados(indice));
				row.createCell(indice++).setCellValue("Monto Mercado");
				
				totalizados.put(indice, new ObjetosTotalizados(indice));
				row.createCell(indice++).setCellValue("Menor Valor");
				
				totalizados.put(indice, new ObjetosTotalizados(indice));
				row.createCell(indice++).setCellValue("Mayor Valor");				
				
				row.createCell(indice++).setCellValue("Numero Operacion");
				row.createCell(indice++).setCellValue("Fecha Inversion");
				row.createCell(indice++).setCellValue("Fecha Liquidacion");
				row.createCell(indice++).setCellValue("Fecha Emision");
				
				if("RF".equals(IdTipoValorizacion)) {
					row.createCell(indice++).setCellValue("Convexidad");
				}
				
				row.createCell(indice++).setCellValue("Codigo IFRS");
				row.createCell(indice++).setCellValue("Boveda");
				row.createCell(indice++).setCellValue("Moneda Instrumento");				
				row.createCell(indice++).setCellValue("Codigo Portafolio");
				
				if("RF".equals(IdTipoValorizacion)) {
					row.createCell(indice++).setCellValue("Flag Deterioro");
					row.createCell(indice++).setCellValue("Porcentaje Deterioro");
					row.createCell(indice++).setCellValue("Deterioro");
					row.createCell(indice++).setCellValue("Fase");
				}
				
				indiceFinal = indice;
				indice = 0;
			}
			
			//GENERACION CUERPO EXCEL
			Row row = sheet.createRow(contadorFila++);
			Cell cell = row.createCell(indice++);
			String diferenciadorOperacion = rs.getString("Diferenciador");
			
			if (!diferenciadorOperacion.equals(diferenciador)) {				

				//SE AGREGAN TOTALES POR TIPO CARTERA
				if (!esPrimerGrupo) {
					row.getCell(0).setCellStyle(totalStyle);
					row.setRowStyle(totalStyle);
					//LOGICA PARA AGREGAR LOS TOTALES
					for (Integer index : totalizados.keySet()) {
						ObjetosTotalizados totalCelda = totalizados.get(index);						
						row.createCell(totalCelda.Posicion).setCellValue(totalCelda.Total.doubleValue());
						row.getCell(totalCelda.Posicion).setCellStyle(totalStyle);
						totalCelda.ReiniciarTotal();
					}
					row = sheet.createRow(contadorFila++);	
					indice = 0;
					cell = row.createCell(indice++);					
				}
				
				if ("PD".equals(diferenciadorOperacion)) {
					cell.setCellValue("Tipo Cartera: CRV");
				} else if ("PC".equals(diferenciadorOperacion)) {
					cell.setCellValue("Tipo Cartera: VRC");
				} else if ("PP".equals(diferenciadorOperacion)) {
					cell.setCellValue("Tipo Cartera: VRC-CRV");
				} else if ("CG".equals(diferenciadorOperacion)) {
					cell.setCellValue("Tipo Cartera: Cartera Garantia");
				} else if ("CD".equals(diferenciadorOperacion)) {
					cell.setCellValue("TipoCartera: Cartera Libre");
				} else if ("EM".equals(diferenciadorOperacion)) {
					cell.setCellValue("Emprestito");
				} else if ("PR".equals(diferenciadorOperacion)) {
					cell.setCellValue("Prestamos");
				} else if ("SIM".equals(diferenciadorOperacion)) {
					cell.setCellValue("Tipo Cartera: Simultanea");
				}
				else {
					cell.setCellValue(diferenciadorOperacion);
				}
				cell.setCellStyle(totalStyle);
				row.setRowStyle(totalStyle);
				
				diferenciador = rs.getString("Diferenciador");
				row = sheet.createRow(contadorFila++);
				esPrimerGrupo = false;
			}
			
			indice = 1;				
			
			//TIPO CARTERA
			if ("PD".equals(diferenciadorOperacion)) {
				row.createCell(indice++).setCellValue("CRV");
			} else if ("PC".equals(diferenciadorOperacion)) {
				row.createCell(indice++).setCellValue("VRC");
			} else if ("PP".equals(diferenciadorOperacion)) {
				row.createCell(indice++).setCellValue("VRC-CRV");
			} else if ("CG".equals(diferenciadorOperacion)) {
				row.createCell(indice++).setCellValue("GARANTIA");
			} else if ("CD".equals(diferenciadorOperacion)) {
				row.createCell(indice++).setCellValue("LIBRE");
			} else if ("EM".equals(diferenciadorOperacion)) {
				row.createCell(indice++).setCellValue("EMPRESTITO");
			} else if ("PR".equals(diferenciadorOperacion)) {
				row.createCell(indice++).setCellValue("PRESTAMOS");
			} else if ("SIM".equals(diferenciadorOperacion)) {
				row.createCell(indice++).setCellValue("SIMULTANEA");
			}
			else {
				row.createCell(indice++).setCellValue(diferenciadorOperacion);
			}
			
			//FOLIO CARTERA
			row.createCell(indice++).setCellValue(Long.parseLong(rs.getString("FolioCartera")));

			//RUT CLIENTE
			row.createCell(indice++).setCellValue(rs.getString("DocumentoIdentidad"));
			
			//PORTAFOLIO CLIENTE
			row.createCell(indice++).setCellValue(Long.parseLong(rs.getString("CodigoPortafolio")));
			
			//NOMBRE CLIENTE
			row.createCell(indice++).setCellValue(rs.getString("NombreCliente"));
			
			//TIPO INSTRUMENTO			
			row.createCell(indice++).setCellValue(rs.getString("IdTipoInstrumento"));
			
			//NEMOTECNICO			
			row.createCell(indice++).setCellValue(rs.getString("NemoSVS"));
			
			//CLASIFICACION TIPOINSTRUMENTO
			row.createCell(indice++).setCellValue(rs.getString("IdClasificacionTipoInstrumento"));
			
			//TIPO REAJUSTE
			row.createCell(indice++).setCellValue(rs.getString("IdTipoReajuste"));
			
			//EMISOR
			row.createCell(indice++).setCellValue(rs.getString("Emisor"));
			
			//FAMILIA
			if("RF".equals(IdTipoValorizacion)) {
				row.createCell(indice++).setCellValue(rs.getString("Familia"));
			}
			
			//CANTIDAD
			Integer decimalesCantidad = this.obtenerDecimalesCantidad(rs.getString("IdTipoReajuste"));
			CellStyle formatoCantidad =  this.obtenerEstiloNumerico(decimalesCantidad, wb,estilosPorDecimal);
			BigDecimal cantidadOperacion = new BigDecimal(rs.getString("Cantidad"));
			
			if (rs.getString("IdClasificacionTipoInstrumento").equals("IF")) {
				cantidadOperacion = new BigDecimal(rs.getString("ValorRescate"));
			}
			
			totalizados.get(indice).SumarTotal(cantidadOperacion);
			this.formatearCeldaNumerica(row.createCell(indice++), cantidadOperacion.toString(), DECIMALES_TASAS, formatoCantidad);
			
			//TASA COMPRA
			CellStyle formatoTasa =  this.obtenerEstiloNumerico(DECIMALES_TASAS, wb,estilosPorDecimal);
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("TasaCompra"), DECIMALES_TASAS, formatoTasa);
			
			//PRECIO COMPRA
			CellStyle formatoPrecio = this.obtenerEstiloNumerico(DECIMALES_PRECIOS, wb,estilosPorDecimal);
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("PrecioCompra"), DECIMALES_PRECIOS, formatoPrecio);
			
			//VALOR COMPRA
			CellStyle formatoSinDecimal = this.obtenerEstiloNumerico(SIN_DECIMALES, wb,estilosPorDecimal);
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("MontoCompra"), SIN_DECIMALES, formatoSinDecimal);
			
			//VALOR PRESENTE			
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("MontoActual"), SIN_DECIMALES, formatoSinDecimal);
			
			//FECHA VCTO
			cell = row.createCell(indice++);
			if ("EM".equals(diferenciadorOperacion) || "PR".equals(diferenciadorOperacion) || "SIM".equals(diferenciadorOperacion)) {
				this.escribirFechaEnCelda(cell, rs, styleFecha, sdf, "FechaVencimientoOperacion");
			} else {
				this.escribirFechaEnCelda(cell, rs, styleFecha, sdf, "FechaVencimientoInstrumento");
			}
			
			
			//MAYOR O IGUAL A SIETE DIAS			
			row.createCell(indice++).setCellValue(rs.getString("MayorIgualSieteDias"));

			//PLAZO AL VENCIMIENTO
			row.createCell(indice++).setCellValue(Integer.parseInt(rs.getString("Dias")));
			
			//TASA MERCADO
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("TasaProximo"), DECIMALES_TASAS, formatoTasa);
			
			//PRECIO MERCADO
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("PrecioProximo"), DECIMALES_PRECIOS, formatoPrecio);
			
			//DURATION
			CellStyle formatoConvexidadDuration = this.obtenerEstiloNumerico(DECIMALES_CONVEXIDAD_DURATION, wb,estilosPorDecimal);
			if("RF".equals(IdTipoValorizacion)) {
				this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("Duration"), DECIMALES_CONVEXIDAD_DURATION, formatoConvexidadDuration);
			}
			
			//VALOR MERCADO
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("MontoProximoMonedaInstrumento"), SIN_DECIMALES, formatoSinDecimal);
			
			//MONTO MERCADO
			BigDecimal montoMercadoActual = new BigDecimal(rs.getString("MontoProximo"));
			totalizados.get(indice).SumarTotal(montoMercadoActual);
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("MontoProximo"), SIN_DECIMALES, formatoSinDecimal);
			
			//MAYORES MENORES VALORES
			BigDecimal menorMontoMercado = new BigDecimal(rs.getString("MenorValor")).abs();	
			totalizados.get(indice).SumarTotal(menorMontoMercado);
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("MenorValor"), SIN_DECIMALES, formatoSinDecimal);
			
			BigDecimal mayorMontoMercado = new BigDecimal(rs.getString("MayorValor")).abs();
			totalizados.get(indice).SumarTotal(mayorMontoMercado);
			this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("MayorValor"), SIN_DECIMALES, formatoSinDecimal);
			
			//NUMERO OPERACION
			if(DIFERENCIADORES_PACTOS.contains(diferenciadorOperacion)) {
				row.createCell(indice++).setCellValue(Long.parseLong(rs.getString("FolioOperacion")));
			} else {
				row.createCell(indice++).setCellValue(Long.parseLong(rs.getString("IdOperacion")));
			}
			
			//FECHA INVERSION
			cell = row.createCell(indice++);
			this.escribirFechaEnCelda(cell, rs, styleFecha, sdf, "FechaOperacion");
			
			//FECHA LIQUIDACION
			cell = row.createCell(indice++);
			this.escribirFechaEnCelda(cell, rs, styleFecha, sdf, "FechaLiquidacion");	
			
			//FECHA EMISION
			cell = row.createCell(indice++);
			this.escribirFechaEnCelda(cell, rs, styleFecha, sdf, "FechaEmisionInstrumento");
			
			//CONVEXIDAD
			if("RF".equals(IdTipoValorizacion)) {
				this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("Convexidad"), DECIMALES_CONVEXIDAD_DURATION, formatoConvexidadDuration);
			}
			
			//CODIGO IFRS
			row.createCell(indice++).setCellValue(rs.getString("CodigoIFRS"));
			
			//BOVEDA
			row.createCell(indice++).setCellValue(rs.getString("IdBoveda"));
			
			//MONEDA INSTRUMENTO
			row.createCell(indice++).setCellValue(rs.getString("IdMonedaInstrumento"));
			
			//CODIGO PORTAFOLIO
			row.createCell(indice++).setCellValue(Integer.parseInt(rs.getString("CodigoPortafolio")));
			
			if("RF".equals(IdTipoValorizacion)) {
				//FLAGS
				row.createCell(indice++).setCellValue(rs.getString("FlagDeterioro"));
				
				//PORCENTAJE DETERIORO
				CellStyle formatoPorcentaje =  this.obtenerEstiloNumerico(DECIMALES_PORCENTAJES, wb,estilosPorDecimal);
				this.formatearCeldaNumerica(row.createCell(indice++), rs.getString("PorcentajeDeterioro"), DECIMALES_PORCENTAJES, formatoPorcentaje);
				
				//DETERIORO
				row.createCell(indice++).setCellValue(new BigDecimal(rs.getString("Deterioro")).doubleValue());
				
				//FASE
				row.createCell(indice++).setCellValue(Integer.parseInt(rs.getString("Fases")));
			}
			
			if (rs.isLast()) {				
				indiceFinal = indice;
				row = sheet.createRow(contadorFila++);	
				row.setRowStyle(totalStyle);
				//LOGICA PARA AGREGAR LOS TOTALES EN GRUPO FINAL 
				for (Integer index : totalizados.keySet()) {
					ObjetosTotalizados totalCelda = totalizados.get(index);
					row.createCell(totalCelda.Posicion).setCellValue(totalCelda.Total.doubleValue());
					row.getCell(totalCelda.Posicion).setCellStyle(totalStyle);
				}
			}
		}

		log.write("Ancho Columnas");
		
		for (int i = 0; i <= indiceFinal; i++) {
			if (i == 0) {
				sheet.setColumnWidth(i, 10 * 256);
				continue;
			}else if (i == 5) {
				sheet.setColumnWidth(i, 20 * 256);
				continue;
			}
			sheet.autoSizeColumn(i);
		}
	}

	private void escribirParametrosFinancierosReporte(HSSFSheet sheet, CallableStatement spCabecera) throws SQLException {
		log.write("Ejecucion SP CarteraPropiaMesaRF");
		boolean isResultSet = spCabecera.execute();

		if (isResultSet) {
			ResultSet rs = spCabecera.getResultSet();

			int celdaInicio = 5;
			int celdaEncabezados = 1;
			
			log.write("Escribiendo cabecero archivo xls");
			if (rs.next()) {
				// Titulo Libro
				Row row = sheet.createRow(0);
				Cell cell = row.createCell(celdaInicio);
				cell.setCellValue("TITULO:");
				cell = row.createCell(celdaInicio + 1);
				String tipoInforme = "RF".equals(rs.getString("IdTipoValorizacion")) ? "RF/IF" : "RV";
				cell.setCellValue(String.format("INFORME MAYOR Y MENOR VALOR %s", tipoInforme));
				
				
				// Nombre
				row = sheet.createRow(celdaEncabezados++);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("NOMBRE:");
				cell = row.createCell(celdaInicio + 1);
				cell.setCellValue(rs.getString("NombreCliente"));
				
				// Rut
				row = sheet.createRow(celdaEncabezados++);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("RUT:");
				cell = row.createCell(celdaInicio + 1);
				cell.setCellValue(rs.getString("DocumentoIdentidad"));

				// Portafolio
				row = sheet.createRow(celdaEncabezados++);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("PORTAFOLIO:");
				cell = row.createCell(celdaInicio + 1);
				cell.setCellValue(rs.getString("CodigoPropositoPortafolio"));

				//Valorizado al
				row = sheet.createRow(celdaEncabezados++);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("VALORIZADO AL:");
				cell = row.createCell(celdaInicio + 1);
				cell.setCellValue(rs.getString("FechaProximo"));
				
				//Tipo Val
				row = sheet.createRow(celdaEncabezados++);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("TIPO VALORIZACION:");
				cell = row.createCell(celdaInicio + 1);
				String tipoValorizacion = rs.getString("TipoValorizacion");
				cell.setCellValue("C".equals(tipoValorizacion) ? "TASA COMPRA" : "M".equals(tipoValorizacion) ? "TASA MERCADO" : "TASA PACTO");
				
				// Parametros fecha
				row = sheet.createRow(celdaEncabezados++);
				cell = row.createCell(celdaInicio);
				cell.setCellValue("PARAMETROS AL:");
				cell = row.createCell(celdaInicio + 2);
				cell.setCellValue(rs.getString("Fecha"));
				cell = row.createCell(celdaInicio + 4);
				cell.setCellValue(rs.getString("FechaProximo"));

				// uf
				row = sheet.createRow(celdaEncabezados++);
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
				row = sheet.createRow(celdaEncabezados++);
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
				row = sheet.createRow(celdaEncabezados++);
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
			GenerarArchivoInformeMayorMenorValor demo = new GenerarArchivoInformeMayorMenorValor();
			demo.logica(con, true);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA ==");
	}

}