package cl.bcs.custodia.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.opencsv.CSVWriter;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.utils.ZipUtil;

public class GenerarResumenPosiciones extends BcsJob{
	private final Log log = new Log("CUS.GENERAR_RESUMEN_POSICIONES");
	private final String ARCHIVO_RF = "_Resumen_Tarjeta_Existencia_RF";
	private final String ARCHIVO_RV = "_Resumen_Tarjeta_Existencia_RV";
	
	private final FileLoader fileLoader = new FileLoader();
	private final String SQL_RESUMEN_TARJETA_EXISTENCIA_Call = fileLoader.loadResourceToString("cl/bcs/custodia/jobs/sql/GenerarResumenPosicion_Call.sql");
	private final String SQL_UPDATE_ID_PROGRAMA = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaSalida WHERE \"IdPrograma\" = :IdPrograma;";
	
	public static class ActualizarPrograma{
		public String 	RutaSalida;
		public Long 	IdPrograma;
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}
	
	public static class TarjetaExistenciaResumen{
		public String Nemotecnico;
		public String FechaProceso;
		public String IdMoneda;
		public BigDecimal SaldoCantidad;
		public BigDecimal SaldoMontoPrecioAdquisicion;
		public BigDecimal PrecioPromedioPonderado;
		public BigDecimal SaldoMontoAPrecioPromedioPonderado;
		public BigDecimal UtilidadPerdidaAcumulada;
		public BigDecimal PrecioAMercado;
		public BigDecimal SaldoMontoAPrecioMercado;
		public BigDecimal UtilidadPerdidaDevengada;
		public BigDecimal UtilidadPerdidaRealizada;
		public String IdClasificacionTipoInstrumento;
		@Override
		public String toString() {
			return String.format(
					"TarjetaExistenciaResumen [Nemotecnico=%s, FechaProceso=%s, IdMoneda=%s, SaldoCantidad=%s, SaldoMontoPrecioAdquisicion=%s, PrecioPromedioPonderado=%s, SaldoMontoAPrecioPromedioPonderado=%s, UtilidadPerdidaAcumulada=%s, PrecioAMercado=%s, SaldoMontoAPrecioMercado=%s, UtilidadPerdidaDevengada=%s, UtilidadPerdidaRealizada=%s, IdClasificacionTipoInstrumento=%s]",
					Nemotecnico, FechaProceso, IdMoneda, SaldoCantidad, SaldoMontoPrecioAdquisicion,
					PrecioPromedioPonderado, SaldoMontoAPrecioPromedioPonderado, UtilidadPerdidaAcumulada,
					PrecioAMercado, SaldoMontoAPrecioMercado, UtilidadPerdidaDevengada, UtilidadPerdidaRealizada,
					IdClasificacionTipoInstrumento);
		}
	}
	
	public static class ResumenPosicionesRV{
		public String Nemotecnico;
		public String IdMoneda;
		public String IdClasificacionTipoInstrumento;
		public String FechaProceso;
		public BigDecimal CantidadCompra;
		public BigDecimal MontoCompra;
		public BigDecimal CantidadVenta;
		public BigDecimal MontoVenta;
		public BigDecimal SaldoMonto;
		public BigDecimal SaldoCantidad;
		@Override
		public String toString() {
			return String.format(
					"ResumenPosicionesRV [Nemotecnico=%s, IdMoneda=%s, IdClasificacionTipoInstrumento=%s, FechaProceso=%s, CantidadCompra=%s, MontoCompra=%s, CantidadVenta=%s, MontoVenta=%s, SaldoMonto=%s, SaldoCantidad=%s]",
					Nemotecnico, IdMoneda, IdClasificacionTipoInstrumento, FechaProceso, CantidadCompra, MontoCompra,
					CantidadVenta, MontoVenta, SaldoMonto, SaldoCantidad);
		}
	}
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	public boolean logica(Connection conn,boolean debug) throws InterruptedException, Throwable {
		try{
			SQL sql = new SQL();
			
			/* Obtener Parametros del JOB*/
			Long idEmpresa = this.getOptionalParam("IdEmpresa").map((i) -> new Long(i)).orElse( (long) 1);
			Long idUsuario = this.getOptionalParam("IdUsuario").map((i) -> new Long(i)).orElse( (long) 3 );
			String idModulo = this.getOptionalParam("IdModulo").orElse("CUS");
			
			Long idInstrumento;
			String idConcepto;
			String fechaConsultaDesde;
			String fechaConsultaHasta;
			System.out.println( String.format("Usuario %d de la empresa %d, ha solicitado generar archivo resumen posiciones", idUsuario, idEmpresa) );
			String rutaBase;
			String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			
			
			if(!debug) {
				idEmpresa = this.getOptionalParam("IdEmpresa").map((i) -> new Long(i)).orElse( (long) 1);
				idUsuario = this.getOptionalParam("IdUsuario").map((i) -> new Long(i)).orElse( (long) 3 );
				idInstrumento = this.getOptionalParam("IdInstrumento").map((i) -> new Long(i)).orElse(null);
				idConcepto = this.getOptionalParam("IdConcepto").orElse(null);
				fechaConsultaDesde = this.getOptionalParam("FechaConsultaDesde").orElse("2016-01-01");
				fechaConsultaHasta = this.getOptionalParam("FechaConsultaHasta").orElse("2999-01-01");	
				rutaBase = Utils.getConfiguracion(conn, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
			}else {
				idEmpresa = (long) 1;
				idUsuario = (long)21;
				idInstrumento = null;
				idConcepto = null;
				fechaConsultaDesde = "2018-11-01";
				fechaConsultaHasta = "2018-11-01";
				rutaBase = "C:/mnt/optimuspeqa/empresa1/";
			}
			
			
			//Consultar el servicio de resumen de posiciones
			HashMap<String,Object>  paramsIn = new HashMap<String,Object>();
			paramsIn.put("p_IdEmpresa", idEmpresa);
			paramsIn.put("p_IdUsuario", idUsuario);
			paramsIn.put("p_IdModulo", idModulo);
			paramsIn.put("p_IdInstrumento", idInstrumento);
			paramsIn.put("p_IdConcepto", idConcepto);
			paramsIn.put("p_FechaConsultaDesde", fechaConsultaDesde);
			paramsIn.put("p_FechaConsultaHasta", fechaConsultaHasta);
			HashMap<String,Class>  paramsOut = new HashMap<String,Class>();
			paramsOut.put("p_out_TarjetaExistenciaResumen", TarjetaExistenciaResumen.class);
			paramsOut.put("p_out_ResumenPosicionesRV", ResumenPosicionesRV.class);
			
			HashMap<String,Object> resultSP = sql.call(conn, SQL_RESUMEN_TARJETA_EXISTENCIA_Call, paramsIn, paramsOut);
			List<TarjetaExistenciaResumen> resumenes = (List<TarjetaExistenciaResumen>) resultSP.get("p_out_TarjetaExistenciaResumen");
			List<ResumenPosicionesRV> resumenesRV = (List<ResumenPosicionesRV>) resultSP.get("p_out_ResumenPosicionesRV");
						
			//Guardar archivos en servidor de aplicaciones
			String fechaServidorApps = Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(conn, idEmpresa),"yyyyMMddHHmmssSSS");
			String NombreArchivoRF = fechaServidorApps + ARCHIVO_RF + ".csv";
			String RutaNombreArchivoRF = rutaBase + "cus/" + NombreArchivoRF;
			String NombreArchivoRV = fechaServidorApps + ARCHIVO_RV + ".xls";
			String RutaNombreArchivoRV = rutaBase + "cus/" + NombreArchivoRV;
			
			//cambiar formato de decimales
			DecimalFormat df = new DecimalFormat("#####0.00");
		    DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
		    dfs.setDecimalSeparator('.');
		    df.setDecimalFormatSymbols(dfs);
			
			log.write(String.format("Cantidad de lineas %d con fecha: %s", resumenes.size(),fechaServidorApps) );
			try(FileOutputStream osRF = new FileOutputStream(RutaNombreArchivoRF);){
				//inicializar archivos CSV como UTF-8
				osRF.write(0xef); osRF.write(0xbb); osRF.write(0xbf);
				log.write("Generar Archivos CSV RF");
				try(CSVWriter RfEscribeCSV = new CSVWriter(new OutputStreamWriter(osRF,"UTF-8"),';');){
					
					String [] FilaResumenRF= new String[]{
							"Valor",
							"FechaEmision",
							"Nominal T",
							"Precio",
							"CARTERA",
							"Precio Credicorp",
							"Interes Devengados No Percibidos",
							"Fluctuacion"
							};
					RfEscribeCSV.writeNext(FilaResumenRF);		    
				    
					//guardar registros en archivos CSV
					for(TarjetaExistenciaResumen resumen : resumenes){
						if(resumen.IdClasificacionTipoInstrumento.equals("RF")) {
							//resumen.PrecioPromedioPonderado = resumen.PrecioPromedioPonderado.divide( new BigDecimal(100) );//pasar a porcentaje
							FilaResumenRF = new String[]{
									resumen.Nemotecnico,//"Valor",
									resumen.FechaProceso,//"FechaEmision",
									df.format(resumen.SaldoCantidad),//"Nominal T",
									df.format(resumen.PrecioPromedioPonderado),//"Precio",
									df.format(resumen.SaldoCantidad.multiply(resumen.PrecioPromedioPonderado)),//"CARTERA",
									df.format(resumen.PrecioAMercado),//"Precio Credicorp",
									"0.0",//"Interes Devengados No Percibidos",
									df.format(resumen.SaldoCantidad.multiply( resumen.PrecioAMercado.subtract(resumen.PrecioPromedioPonderado )))//"Fluctuacion"
							};
							RfEscribeCSV.writeNext(FilaResumenRF);
							
						}
					}
				}
			}
			
			//crear excel peru
			String templatePoscionesRv = rutaBase + "template_resumen_posiciones_rv.xls";
			try(FileOutputStream outputStream = new FileOutputStream( RutaNombreArchivoRV );){
				try(FileInputStream fis = new FileInputStream(new File(templatePoscionesRv));){
			    	try(Workbook workbook = WorkbookFactory.create(fis);){
			    		Sheet sheet = workbook.getSheetAt(0);
			    		int rowCount = sheet.getLastRowNum();
			    		for(ResumenPosicionesRV resumen : resumenesRV){
			    			if(resumen.IdClasificacionTipoInstrumento.equals("RV")) {
			    				Row row = sheet.createRow(++rowCount);
			    				int columnCount = 0;
			    				//nemo (ok)
			    				Cell cell = row.createCell(columnCount);
			    				cell.setCellValue(resumen.Nemotecnico);
			    				//moneda (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(resumen.IdMoneda);
			    				//cantidad compra (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format(resumen.CantidadCompra));
			    				//soles (monto compra) (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format(resumen.MontoCompra));
			    				//CU (cu compra) (ok)
			    				cell = row.createCell(++columnCount);
			    				BigDecimal cuCompra = BigDecimal.valueOf(0);
			    				if(resumen.MontoCompra.longValue() > 0 && resumen.CantidadCompra.longValue()>0) {
			    					cuCompra = resumen.MontoCompra.divide(resumen.CantidadCompra, 2, RoundingMode.HALF_EVEN);
			    					cell.setCellValue(df.format( cuCompra ));
			    				}else {
			    					cell.setCellValue("-");
			    				}
			    				//cantidad ventas (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format(resumen.CantidadVenta));
			    				//soles (monto venta) (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format(resumen.MontoVenta));
			    				//CU (cu venta) (ok)
			    				cell = row.createCell(++columnCount);
			    				BigDecimal cuVenta = BigDecimal.valueOf(0);
			    				if(resumen.MontoVenta.longValue() > 0 && resumen.CantidadVenta.longValue()>0) {
			    					cuVenta = resumen.MontoVenta.divide(resumen.CantidadVenta, 2, RoundingMode.HALF_EVEN);
			    					cell.setCellValue(df.format( cuVenta ));
			    				}else {
			    					cell.setCellValue("-");
			    				}
			    				
			    				//calcular saldo inicial
			    				BigDecimal cantidadInicial = resumen.SaldoCantidad.subtract(resumen.CantidadCompra).add(resumen.CantidadVenta);
			    				BigDecimal montoInicial = resumen.SaldoMonto.subtract(resumen.MontoCompra).add(resumen.MontoVenta);
			    				 
			    				//Costo de ventas
			    				BigDecimal cantidadCostoVenta = resumen.CantidadVenta;
			    				BigDecimal cuCostoVenta = BigDecimal.valueOf(0);
			    				if(resumen.MontoCompra.add(montoInicial).longValue() > 0 && resumen.CantidadCompra.add(cantidadInicial).longValue()>0) {
			    					cuCostoVenta = resumen.MontoCompra.add(montoInicial).divide(resumen.CantidadCompra.add(cantidadInicial), 2, RoundingMode.HALF_EVEN);
			    				}
			    				BigDecimal montoCostoVenta = cantidadCostoVenta.multiply( cuCostoVenta );
			    				//cantidad costo de venta (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format( cantidadCostoVenta ));
			    				//soles (monto costo de venta) (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format( montoCostoVenta ));
			    				//cu (cu costo de venta)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format( cuCostoVenta ));
			    				
			    				//Saldo Final
			    				BigDecimal cantidadSaldoFinal = cantidadCostoVenta.add(resumen.CantidadCompra.add(cantidadInicial));
			    				BigDecimal montoSaldoFinal = montoCostoVenta.add(resumen.MontoCompra.add(montoInicial));
			    				BigDecimal cuSaldoFinal = BigDecimal.valueOf(0);
			    				if(montoSaldoFinal.longValue()>0 && cantidadSaldoFinal.longValue() > 0) {
			    					cuSaldoFinal = montoSaldoFinal.divide(cantidadSaldoFinal, 2, RoundingMode.HALF_EVEN);
			    				}
			    				BigDecimal interesesPercibidos = resumen.MontoVenta.subtract(montoCostoVenta);
			    				//cantidad saldo final
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue( df.format( cantidadSaldoFinal ) );
			    				//soles (monto saldo final)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue( df.format( montoSaldoFinal ));
			    				//cu (cu saldo final)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue( df.format( cuSaldoFinal ));
			    				//intereses percibidos
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format( interesesPercibidos ));
			    			}
			    		}
			    		
			    		workbook.write(outputStream);
		    		}
		    	}
		    }
			
			log.write("Generarcion Archivos CSV completa");
			//Comprimir ambos archivos en un ZIP
			List<String> archivos = Arrays.asList(RutaNombreArchivoRV,RutaNombreArchivoRF);
			String NombreArchivoZip;
			if(("PE").equals(localizacionSistema)){
				NombreArchivoZip = "_ResumenKardex_RV_RF.zip";
			}else {
				NombreArchivoZip = "_ResumenTarjetaExistencia_RV_RF.zip";
			}
			final String nombreZip = "cus/" + fechaServidorApps + NombreArchivoZip;
			final String rutaZip = rutaBase + nombreZip;
			boolean resultadoZIP = ZipUtil.generateMultipleZip(archivos, rutaZip);
			log.write(String.format("Comprimir archivos en %s, con resultado: %s", rutaZip, String.valueOf(resultadoZIP)));
			
			//eliminar archivos rv/rf
			for(String archivo : archivos) {
				FileUtils.forceDelete(new File(archivo));
				log.write("Eliminar archivo: "+archivo);
			}
			
			//Actualizar programa con la ruta del ZIP
			if(resultadoZIP){
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(nombreZip, this.getJobId()));
				int [] res = sql.batch(conn, SQL_UPDATE_ID_PROGRAMA, actualizar, ActualizarPrograma.class);
				log.write( String.format("Actualizar job: %d, con la ruta %s, resultado : %d", this.getJobId(), rutaZip, ((res!=null && res.length>0) ? res[0] : 0) ));
				return true;
			}else{
				return false;
			}
		}catch(Exception e){
			log.write(e);
		}
		return true;
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}
	
	//DEBUG
	public static void main(String[] args) {
		// Example
		System.out.println("== START ==");
		try (Connection con = PlataformaUtils.getQAPEConnection()) {
			con.setAutoCommit(false);
			SQL.exec(con, "set schema BSCL");
			SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
			// ---------------------------------
			try {
				GenerarResumenPosiciones apf = new GenerarResumenPosiciones();
				
				System.out.println(apf.logica(con,true));
				con.rollback();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	
		System.out.println("== END ==");
	}

}
