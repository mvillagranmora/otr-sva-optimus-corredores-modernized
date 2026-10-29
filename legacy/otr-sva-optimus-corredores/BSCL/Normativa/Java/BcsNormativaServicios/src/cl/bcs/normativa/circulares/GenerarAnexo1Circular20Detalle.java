package cl.bcs.normativa.circulares;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.circulares.GenerarAnexo1Circular20.GenerarAnexo1Circular20_Entrada;
import cl.bcs.normativa.circulares.GenerarAnexo1Circular20.GenerarAnexo1Circular20_Salida;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;



public class GenerarAnexo1Circular20Detalle implements JavaCall{	
	private static FileLoader loader = new FileLoader();	
	private static final Logger LOGGER = LogManager.getLogger(GenerarAnexo1Circular20.class);
	private static String SQL_SELECT_SALDOACUMULADO_DETALLE = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexo1Circular20_Select_SaldoAcumuladoDetalle.sql");
	private static String SQL_SELECT_TRASPASO_DETALLE = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexoCircular20Detalle_SelectTraspasoDetalle.sql");
	private static String SQL_SELECT_SALDOCUSTODIA_DETALLE = loader.loadResourceToString("cl/bcs/normativa/circulares/sql/GenerarAnexoCircular20Detalle_Select_SaldoCustodiaDetalle.sql");
	
	public static class ObjTablaSalida {
		public Integer IdPlanTipoInversion;
		public String CodigoPortafolio;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		public BigDecimal SaldoAcumulado;
		public BigDecimal SaldoAcumuladoCuentaInversion;
		public BigDecimal SaldoAcumuladoCustodia;
		public String Sexo;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjTablaTraspasoSalida {
		public Integer IdPlanTipoInversion;
		public String Codigo;
		public Long IdPortafolio;
		public String DocumentoIdentidad;
		public String TipoMovimiento;
		public String NombreEntidadRecaudadora;
		public String DocumentoIdentidadRecaudadora;
		public Long IdSolicitudAporteRescate;
		public String IdSerieFondo;
		public String FechaCertificacion;
		public String FechaLiquidacion;
		public String FechaValorCuota;
		public String FechaAceptacion;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	public static class ObjTablaSaldoCustodiaSalida {
		public Integer IdPlanTipoInversion;
		public String CodigoPortafolio;
		public Long IdCliente;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		public BigDecimal SaldoCustodia;
		public String Nemotecnico;
		
		public String toString() {
			return Utils.printJson(this, false);
		};
	}
	
	
	public static class GenerarAnexo1Circular20Detalle_Entrada {
			public Long			p_IdEmpresa;
			public Long			p_IdUsuario;
			public String 		p_IdModulo;
			public String		p_FechaSistema;
			public String		p_FechaInicio;
			public String		p_FechaFin;
			public String		p_Ruta;
			public Long			p_SecuenciaSolicitud;
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		
	
	public static class GenerarAnexo1Circular20Detalle_Salida {
		public String P_OUT_NOMBRE_ARCHIVO;
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		
	}
	
	public static class ParametroSaldoAcumuladoDetalle {

		public Long SecuenciaSolicitud;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
	}

	
	@Trace
	
	public GenerarAnexo1Circular20Detalle_Salida logica(Connection con, GenerarAnexo1Circular20Detalle_Entrada in) throws Exception {
		LOGGER.info("Inicio GenerarAnexo1Circular20Detalle");
		LOGGER.info(in.toString());
				
		GenerarAnexo1Circular20Detalle_Salida out = new GenerarAnexo1Circular20Detalle_Salida();
		SQL sql = new SQL();
		out.P_OUT_ERRORESCONTADOR = 0L;
		//Recupera valores de empresa
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdEmpresa", in.p_IdEmpresa);
	
		try {		
			
			final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	        final java.util.Date da = df.parse(in.p_FechaInicio.toString());
	        final Calendar cal = Calendar.getInstance();
	        cal.setTime(da);
	        
	        Integer year = cal.get(Calendar.YEAR);
	        Integer month = cal.get(Calendar.MONTH)+1;
	        
	    
			GenerarAnexo1Circular20 sp = new GenerarAnexo1Circular20();
			GenerarAnexo1Circular20_Entrada entradaCircular = new GenerarAnexo1Circular20_Entrada();
			
			entradaCircular.p_IdEmpresa = in.p_IdEmpresa;
			entradaCircular.p_IdModulo = in.p_IdModulo;
			entradaCircular.p_IdUsuario = in.p_IdUsuario;
			entradaCircular.p_FechaSistema = in.p_FechaSistema;
			entradaCircular.p_FechaInicio = in.p_FechaInicio;
			entradaCircular.p_FechaFin = in.p_FechaFin;
			entradaCircular.p_Ruta = in.p_Ruta;
			entradaCircular.p_SecuenciaSolicitud= in.p_SecuenciaSolicitud;
			
			GenerarAnexo1Circular20_Salida SalidaCircular = new GenerarAnexo1Circular20_Salida();
			SalidaCircular = sp.logica(con, entradaCircular);
			
			if(SalidaCircular.P_OUT_ERRORESCONTADOR == 0L) {
	        HashMap<String,Object> paramsdetalle = new HashMap<>();
	        paramsdetalle.put("SecuenciaSolicitud", in.p_SecuenciaSolicitud);
			
			 List<ObjTablaSalida> lstSaldosAcumuladosDetalle = new ArrayList<ObjTablaSalida>();
			 lstSaldosAcumuladosDetalle = sql.query(con, SQL_SELECT_SALDOACUMULADO_DETALLE, paramsdetalle, ObjTablaSalida.class);
			 
			 System.out.println("SQL_SELECT_SALDOACUMULADO_DETALLE");
			 
			  out.P_OUT_NOMBRE_ARCHIVO =("Circular20Anexo1Detalle".concat(year.toString()).concat(month.toString().concat(".xls")));
			  
			  try (FileOutputStream outputStream = new FileOutputStream(in.p_Ruta.concat(out.P_OUT_NOMBRE_ARCHIVO))) {
					Workbook archivoExcel = new HSSFWorkbook();
					Sheet hoja = archivoExcel.createSheet();
					archivoExcel.setSheetName(0, "Detalle Saldos Acumulados");
					
					/************************************/
					/** Formatos **/
					/************************************/

					hoja.setColumnWidth(0, 7000);
					hoja.setColumnWidth(1, 7000);
					hoja.setColumnWidth(2, 7000);
					hoja.setColumnWidth(3, 7000);
					hoja.setColumnWidth(4, 7000);
					hoja.setColumnWidth(5, 7000);
					hoja.setColumnWidth(6, 7000);
					hoja.setColumnWidth(7, 7000);
				

					CellStyle estiloNegrita = archivoExcel.createCellStyle();
					Font fuente = archivoExcel.createFont();
					fuente.setBold(true);
					estiloNegrita.setFont(fuente);
					estiloNegrita.setAlignment(CellStyle.ALIGN_CENTER);

					CellStyle estiloEncabezado = archivoExcel.createCellStyle();
					Font fuenteEncabezado = archivoExcel.createFont();
					fuenteEncabezado.setBold(true);
					estiloEncabezado.setFont(fuenteEncabezado);
					//estiloEncabezado.setFillForegroundColor(IndexedColors.BLUE.getIndex());
					//estiloEncabezado.setFillPattern(CellStyle.SOLID_FOREGROUND);
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
					celda.setCellValue("IdPlanTipoInversion");
					celda.setCellStyle(estiloEncabezado);
					 celda = fila.createCell(++columnCount);
					celda.setCellValue("Codigo Portafolio");
					celda.setCellStyle(estiloEncabezado);
					 celda = fila.createCell(++columnCount);
					celda.setCellValue("Documento Identidad");
					celda.setCellStyle(estiloEncabezado);
					 celda = fila.createCell(++columnCount);
					celda.setCellValue("IdPortafolio");
					celda.setCellStyle(estiloEncabezado);
					 celda = fila.createCell(++columnCount);
					celda.setCellValue("Sexo");
					celda.setCellStyle(estiloEncabezado);
					 celda = fila.createCell(++columnCount);
					celda.setCellValue("Saldo Acumulado");
					celda.setCellStyle(estiloEncabezado);
					 celda = fila.createCell(++columnCount);
					celda.setCellValue("Saldo Acumulado Cuenta Inversion");
					celda.setCellStyle(estiloEncabezado);
					 celda = fila.createCell(++columnCount);
					celda.setCellValue("Saldo Acumulado Custodia");
					celda.setCellStyle(estiloEncabezado);
					
					/************************************/
					/** DETALLE Saldos Acumulados **/
					/************************************/
					 System.out.println("DETALLE Saldos Acumulados");
					 
					for (ObjTablaSalida f : lstSaldosAcumuladosDetalle) {
						columnCount = 0;
						fila = hoja.createRow(++rowCount); // fila en donde se escribe

						celda = fila.createCell(columnCount);
						celda.setCellValue(f.IdPlanTipoInversion);
						celda = fila.createCell(++columnCount);
						celda.setCellValue(f.CodigoPortafolio);
						celda = fila.createCell(++columnCount);
						celda.setCellValue(f.DocumentoIdentidad);
						celda = fila.createCell(++columnCount);
						celda.setCellValue(f.IdPortafolio);
						celda = fila.createCell(++columnCount);
						celda.setCellValue(f.Sexo);
						celda = fila.createCell(++columnCount);
						celda.setCellValue(f.SaldoAcumulado.doubleValue());
						celda = fila.createCell(++columnCount);
						celda.setCellValue(f.SaldoAcumuladoCuentaInversion.doubleValue());
						celda = fila.createCell(++columnCount);
						celda.setCellValue(f.SaldoAcumuladoCustodia.doubleValue());
					}
					
					/***
					 * Hoja 2 Detalle Traspaso
					 * 
					 * ****/
					
					 List<ObjTablaTraspasoSalida> lstTraspasoDetalle = new ArrayList<ObjTablaTraspasoSalida>();
					 lstTraspasoDetalle = sql.query(con, SQL_SELECT_TRASPASO_DETALLE, paramsdetalle, ObjTablaTraspasoSalida.class);
					 
					 System.out.println("SQL_SELECT_TRASPASO_DETALLE");
					 
					
					Sheet hoja2 = archivoExcel.createSheet();
					archivoExcel.setSheetName(1, "Detalle Traspasos");
					hoja2.setColumnWidth(0, 7000);
					hoja2.setColumnWidth(1, 7000);
					hoja2.setColumnWidth(2, 7000);
					hoja2.setColumnWidth(3, 7000);
					hoja2.setColumnWidth(4, 7000);
					hoja2.setColumnWidth(5, 7000);
					hoja2.setColumnWidth(6, 7000);
					hoja2.setColumnWidth(7, 7000);
					hoja2.setColumnWidth(8, 7000);
					hoja2.setColumnWidth(9, 7000);
					hoja2.setColumnWidth(10, 7000);
					hoja2.setColumnWidth(11, 7000);
					hoja2.setColumnWidth(12, 7000);
					
					/************************************/
					/** Encabezado2 **/
					/************************************/
					int rowCount2 = 0;
					Row fila2 = hoja2.createRow(rowCount2);
					int columnCount2 = 0;

					Cell celda2 = fila2.createCell(columnCount2);
					celda2.setCellValue("IdPlanTipoInversion");
					celda2.setCellStyle(estiloEncabezado);
					celda2 = fila2.createCell(++columnCount2);
					celda2.setCellValue("Codigo");
					celda2.setCellStyle(estiloEncabezado);
					celda2 = fila2.createCell(++columnCount2);
					celda2.setCellValue("IdPortafolio");
					celda2.setCellStyle(estiloEncabezado);
					celda2 = fila2.createCell(++columnCount2);
					celda2.setCellValue("Documento Identidad");
					celda2.setCellStyle(estiloEncabezado);
					celda2 = fila2.createCell(++columnCount2);
					celda2.setCellValue("Tipo Movimiento");
					celda2.setCellStyle(estiloEncabezado);
					celda2 = fila2.createCell(++columnCount2);
					celda2.setCellValue("Nombre Entidad Recaudadora");
					celda2.setCellStyle(estiloEncabezado);
					celda2 = fila2.createCell(++columnCount2);
					celda2.setCellValue("Documento Identidad Recaudadora");
					celda2.setCellStyle(estiloEncabezado);
					celda2 = fila2.createCell(++columnCount2);
					celda2.setCellValue("IdSolicitudAporteRescate");
					celda2.setCellStyle(estiloEncabezado);
					celda2 = fila2.createCell(++columnCount2);
					celda2.setCellValue("IdSerieFondo");
					celda2.setCellStyle(estiloEncabezado);
					celda2 = fila2.createCell(++columnCount2);
					celda2.setCellValue("Fecha Certificacion");
					celda2.setCellStyle(estiloEncabezado);
					celda2 = fila2.createCell(++columnCount2);
					celda2.setCellValue("Fecha Liquidacion");
					celda2.setCellStyle(estiloEncabezado);
					celda2 = fila2.createCell(++columnCount2);
					celda2.setCellValue("Fecha Valor Cuota");
					celda2.setCellStyle(estiloEncabezado);
					celda2 = fila2.createCell(++columnCount2);
					celda2.setCellValue("Fecha Aceptacion");
					celda2.setCellStyle(estiloEncabezado);
					
					for (ObjTablaTraspasoSalida f2 : lstTraspasoDetalle) {
						columnCount2 = 0;
						fila2 = hoja2.createRow(++rowCount2); // fila en donde se escribe

						celda2 = fila2.createCell(columnCount2);
						celda2.setCellValue(f2.IdPlanTipoInversion);
						celda2 = fila2.createCell(++columnCount2);
						celda2.setCellValue(f2.Codigo);
						celda2 = fila2.createCell(++columnCount2);
						celda2.setCellValue(f2.IdPortafolio);
						celda2 = fila2.createCell(++columnCount2);
						celda2.setCellValue(f2.DocumentoIdentidad);
						celda2 = fila2.createCell(++columnCount2);
						celda2.setCellValue(f2.TipoMovimiento);
						celda2 = fila2.createCell(++columnCount2);
						celda2.setCellValue(f2.NombreEntidadRecaudadora);
						celda2 = fila2.createCell(++columnCount2);
						celda2.setCellValue(f2.DocumentoIdentidadRecaudadora);
						celda2 = fila2.createCell(++columnCount2);
						celda2.setCellValue(f2.IdSolicitudAporteRescate);
						celda2 = fila2.createCell(++columnCount2);
						celda2.setCellValue(f2.IdSerieFondo);
						celda2 = fila2.createCell(++columnCount2);
						celda2.setCellValue(f2.FechaCertificacion);
						celda2 = fila2.createCell(++columnCount2);
						celda2.setCellValue(f2.FechaLiquidacion);
						celda2 = fila2.createCell(++columnCount2);
						celda2.setCellValue(f2.FechaValorCuota);
						celda2 = fila2.createCell(++columnCount2);
						celda2.setCellValue(f2.FechaAceptacion);
					}
					
					
					/***
					 * Hoja 3 Detalle Saldo Custodia
					 * 
					 * ****/
					
					 List<ObjTablaSaldoCustodiaSalida> lstSaldoCustodiaDetalle = new ArrayList<ObjTablaSaldoCustodiaSalida>();
					 lstSaldoCustodiaDetalle = sql.query(con, SQL_SELECT_SALDOCUSTODIA_DETALLE, paramsdetalle, ObjTablaSaldoCustodiaSalida.class);
					 
					 System.out.println("CANTIDAD REGISTROS > " + lstSaldoCustodiaDetalle.size());
					 
					 System.out.println("SQL_SELECT_SALDOCUSTODIA_DETALLE");
					 
						Sheet hoja3 = archivoExcel.createSheet();
						archivoExcel.setSheetName(2, "Detalle Saldo Custodia");
						hoja3.setColumnWidth(0, 7000);
						hoja3.setColumnWidth(1, 7000);
						hoja3.setColumnWidth(2, 7000);
						hoja3.setColumnWidth(3, 7000);
						hoja3.setColumnWidth(4, 7000);
						hoja3.setColumnWidth(5, 7000);
						hoja3.setColumnWidth(6, 7000);
						
						/************************************/
						/** Encabezado 3 **/
						/************************************/
						int rowCount3 = 0;
						Row fila3 = hoja3.createRow(rowCount3);
						int columnCount3 = 0;
						
						Cell celda3 = fila3.createCell(columnCount3);
						celda3.setCellValue("IdPlanTipoInversion");
						celda3.setCellStyle(estiloEncabezado);
						celda3 = fila3.createCell(++columnCount3);
						celda3.setCellValue("Codigo Portafolio");
						celda3.setCellStyle(estiloEncabezado);
						celda3 = fila3.createCell(++columnCount3);
						celda3.setCellValue("IdCliente");
						celda3.setCellStyle(estiloEncabezado);
						celda3 = fila3.createCell(++columnCount3);
						celda3.setCellValue("Documento Identidad");
						celda3.setCellStyle(estiloEncabezado);
						celda3 = fila3.createCell(++columnCount3);
						celda3.setCellValue("IdPortafolio");
						celda3.setCellStyle(estiloEncabezado);
						celda3 = fila3.createCell(++columnCount3);
						celda3.setCellValue("Saldo Custodia");
						celda3.setCellStyle(estiloEncabezado);
						celda3 = fila3.createCell(++columnCount3);
						celda3.setCellValue("Nemotecnico");
						celda3.setCellStyle(estiloEncabezado);
						
			
						int rowCount4 = 0;
						Sheet hoja4 = archivoExcel.createSheet();
						Row fila4 = hoja4.createRow(rowCount4);
						int columnCount4 = 0;
						
						archivoExcel.setSheetName(3, "Detalle Saldo Custodia Continuacion");
						hoja4.setColumnWidth(0, 7000);
						hoja4.setColumnWidth(1, 7000);
						hoja4.setColumnWidth(2, 7000);
						hoja4.setColumnWidth(3, 7000);
						hoja4.setColumnWidth(4, 7000);
						hoja4.setColumnWidth(5, 7000);
						hoja4.setColumnWidth(6, 7000);
						
						/************************************/
						/** Encabezado 3 **/
						/************************************/
						

						Cell celda4 = fila4.createCell(columnCount4);
						celda4.setCellValue("IdPlanTipoInversion");
						celda4.setCellStyle(estiloEncabezado);
						celda4 = fila4.createCell(++columnCount4);
						celda4.setCellValue("Codigo Portafolio");
						celda4.setCellStyle(estiloEncabezado);
						celda4 = fila4.createCell(++columnCount4);
						celda4.setCellValue("IdCliente");
						celda4.setCellStyle(estiloEncabezado);
						celda4 = fila4.createCell(++columnCount4);
						celda4.setCellValue("Documento Identidad");
						celda4.setCellStyle(estiloEncabezado);
						celda4 = fila4.createCell(++columnCount4);
						celda4.setCellValue("IdPortafolio");
						celda4.setCellStyle(estiloEncabezado);
						celda4 = fila4.createCell(++columnCount4);
						celda4.setCellValue("Saldo Custodia");
						celda4.setCellStyle(estiloEncabezado);
						celda4 = fila4.createCell(++columnCount4);
						celda4.setCellValue("Nemotecnico");
						celda4.setCellStyle(estiloEncabezado);
						
						
						int f3 = 0;
						for( f3 = lstSaldoCustodiaDetalle.size()-1; f3 >= 0; f3 --) {
							
							if(f3<=65534) {
							columnCount3 = 0;
							fila3 = hoja3.createRow(++rowCount3); // fila en donde se escribe

							celda3 = fila3.createCell(columnCount3);
							celda3.setCellValue(lstSaldoCustodiaDetalle.get(f3).IdPlanTipoInversion);
							celda3 = fila3.createCell(++columnCount3);
							celda3.setCellValue(lstSaldoCustodiaDetalle.get(f3).CodigoPortafolio);
							celda3 = fila3.createCell(++columnCount3);
							celda3.setCellValue(lstSaldoCustodiaDetalle.get(f3).IdCliente);
							celda3 = fila3.createCell(++columnCount3);
							celda3.setCellValue(lstSaldoCustodiaDetalle.get(f3).DocumentoIdentidad);
							celda3 = fila3.createCell(++columnCount3);
							celda3.setCellValue(lstSaldoCustodiaDetalle.get(f3).IdPortafolio);
							celda3 = fila3.createCell(++columnCount3);
							celda3.setCellValue(lstSaldoCustodiaDetalle.get(f3).SaldoCustodia.doubleValue());
							celda3 = fila3.createCell(++columnCount3);
							celda3.setCellValue(lstSaldoCustodiaDetalle.get(f3).Nemotecnico);
							
							}
							if(f3 >65534) {
								
							columnCount4 = 0;
							fila4 = hoja4.createRow(++rowCount4); // fila en donde se escribe

							celda4 = fila4.createCell(columnCount4);
							celda4.setCellValue(lstSaldoCustodiaDetalle.get(f3).IdPlanTipoInversion);
							celda4 = fila4.createCell(++columnCount4);
							celda4.setCellValue(lstSaldoCustodiaDetalle.get(f3).CodigoPortafolio);
							celda4 = fila4.createCell(++columnCount4);
							celda4.setCellValue(lstSaldoCustodiaDetalle.get(f3).IdCliente);
							celda4 = fila4.createCell(++columnCount4);
							celda4.setCellValue(lstSaldoCustodiaDetalle.get(f3).DocumentoIdentidad);
							celda4 = fila4.createCell(++columnCount4);
							celda4.setCellValue(lstSaldoCustodiaDetalle.get(f3).IdPortafolio);
							celda4 = fila4.createCell(++columnCount4);
							celda4.setCellValue(lstSaldoCustodiaDetalle.get(f3).SaldoCustodia.doubleValue());
							celda4 = fila4.createCell(++columnCount4);
							celda4.setCellValue(lstSaldoCustodiaDetalle.get(f3).Nemotecnico);
							
							}
						}
					
						
						
					archivoExcel.write(outputStream);
					
					
			  }

	
			 
			 LOGGER.info("Select  GenerarAnexo1Circular20Detalle Traspaso Detalle");
			
			 	
			}
		
		}
		finally {
		   
		}
				
		return out;
	};

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		GenerarAnexo1Circular20Detalle_Entrada jin=gson.fromJson(in.toString() ,GenerarAnexo1Circular20Detalle_Entrada.class);
		GenerarAnexo1Circular20Detalle_Salida out=logica(conn,jin);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {	

		Connection con = PlataformaUtils.getBOLSAConnection();
		
		try {
				GenerarAnexo1Circular20Detalle_Entrada entrada = new GenerarAnexo1Circular20Detalle_Entrada();
				entrada.p_IdEmpresa = 1L;
				entrada.p_IdModulo = "NOR";
				entrada.p_IdUsuario = 1L;
				entrada.p_FechaSistema = "2021-01-25";
				entrada.p_FechaInicio = "2021-01-01";
				entrada.p_FechaFin = "2021-01-25";
				entrada.p_Ruta = "C:/mnt/optimus/des/empresa1/nor/";
				entrada.p_SecuenciaSolicitud= 1103L;
				
				GenerarAnexo1Circular20Detalle_Salida Salida = new GenerarAnexo1Circular20Detalle_Salida();
				GenerarAnexo1Circular20Detalle sp = new GenerarAnexo1Circular20Detalle();
				Salida = sp.logica(con,entrada);
				System.out.println(Salida);
				con.rollback();
			}catch(Exception e){
				con.rollback();
				e.printStackTrace();
			}finally{
				con.close();
			}	
		System.out.println("== END ==");
	};
};
