package cl.bcs.custodia.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
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

public class GenerarDetallePosiciones extends BcsJob{
	private final Log log = new Log("CUS.GENERAR_DETALLE_POSICIONES");
	private final String ARCHIVO_RF = "_Detalle_Tarjeta_Existencia_RF";
	private final String ARCHIVO_RV = "_Detalle_Tarjeta_Existencia_RV";
	private final String ARCHIVOPERU_RF = "_Detalle_Kardex_RF";
	private final String ARCHIVOPERU_RV = "_Detalle_Kardex_RV";
	
	private final FileLoader fileLoader = new FileLoader();
	private final String SQL_DETALLE_TARJETA_EXISTENCIA_QUERY = fileLoader.loadResourceToString("cl/bcs/custodia/jobs/sql/GenerarDetallePosicion_Consulta.sql");
	private final String SQL_UPDATE_ID_PROGRAMA = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaSalida WHERE \"IdPrograma\" = :IdPrograma;";
	
	public static class ActualizarPrograma{
		public String 	RutaSalida;
		public Long 	IdPrograma;
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}
	
	public static class TarjetaExistenciaDetalle{
		public Date FechaProceso;
		public String TipoUI;
		public String IdClasificacionTipoInstrumento;
		public String Nemotecnico;
		//
		public Long NumeroPoliza;
		public String IdMoneda;
		public String OrigenInstrumento;
		public String PresenciaBursatil;
		public String TipoRenta;
		public String PactoAndino;
		public String IdBolsa;
		//
		public BigDecimal CantidadIngreso;
		public BigDecimal CantidadEgreso;
		public BigDecimal SaldoCantidad;
		public BigDecimal MontoIngreso;
		public BigDecimal MontoEgreso;
		public BigDecimal SaldoMontoPrecioAdquisicion;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	public boolean logica(Connection conn, boolean debug) throws Exception {
		try{
			SQL sql = new SQL();
			/* Obtener Parametros del JOB*/
			Long idEmpresa;
			Long idUsuario = this.getOptionalParam("IdUsuario").map((i) -> new Long(i)).orElse( (long) 3 );
			Long idInstrumento;
			String idConcepto;
			String fechaConsultaDesde;
			String fechaConsultaHasta;
			String rutaBase;
			
			if(!debug) {
				idEmpresa = this.getOptionalParam("IdEmpresa").map((i) -> new Long(i)).orElse( (long) 1);
				idInstrumento = this.getOptionalParam("IdInstrumento").map((i) -> new Long(i)).orElse(null);
				idConcepto = this.getOptionalParam("IdConcepto").orElse(null);
				fechaConsultaDesde = this.getOptionalParam("FechaConsultaDesde").orElse("2016-01-01");
				fechaConsultaHasta = this.getOptionalParam("FechaConsultaHasta").orElse("2999-01-01");	
				rutaBase = Utils.getConfiguracion(conn, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
			}else {
				idEmpresa = (long) 1;
				idInstrumento = null;
				idConcepto = null;
				fechaConsultaDesde = "2018-04-10";
				fechaConsultaHasta = "2018-04-10";
				rutaBase = "C:/mnt/optimuspeqa/empresa1/";
			}
			
			System.out.println( String.format("Usuario %d de la empresa %d, ha solicitado generar archivo resumen posiciones", idUsuario, idEmpresa) );
			
			//Consultar el servicio de resumen de posiciones
			HashMap<String,Object>  paramsIn = new HashMap<String,Object>();
			paramsIn.put("p_IdEmpresa", idEmpresa);
			paramsIn.put("p_IdInstrumento", idInstrumento);
			paramsIn.put("p_IdConcepto", idConcepto);
			paramsIn.put("p_FechaConsultaDesde", fechaConsultaDesde);
			paramsIn.put("p_FechaConsultaHasta", fechaConsultaHasta);

			List<TarjetaExistenciaDetalle>  detalles  = sql.query(conn, SQL_DETALLE_TARJETA_EXISTENCIA_QUERY, paramsIn, TarjetaExistenciaDetalle.class);
			
			String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			
			//Guardar archivos en servidor de aplicaciones
			String fechaServidorApps = Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(conn, idEmpresa),"yyyyMMddHHmmssSSS");
			
			/*INICIO EDN - OPTIMUSPE-1371 y OPTIMUSPE-1370 */
			String NombreArchivoRF = "";
			String NombreArchivoRV = "";
			if(("PE").equals(localizacionSistema)){
				NombreArchivoRF = fechaServidorApps + ARCHIVOPERU_RF + ".xls";
				NombreArchivoRV = fechaServidorApps + ARCHIVOPERU_RV + ".xls";
			}else {
				NombreArchivoRF = fechaServidorApps + ARCHIVO_RF + ".xls";
				NombreArchivoRV = fechaServidorApps + ARCHIVO_RV + ".xls";
			}
			/*FIN EDN - OPTIMUSPE-1371 y OPTIMUSPE-1370 */
			String RutaNombreArchivoRF = rutaBase + "cus/" + NombreArchivoRF;
			String RutaNombreArchivoRV = rutaBase + "cus/" + NombreArchivoRV;
						
			log.write(String.format("Cantidad de lineas %d con fecha: %s", detalles.size(),fechaServidorApps) );
			
			//crear excel peru
			boolean archivoRV = generarArchivoDetalle(rutaBase, RutaNombreArchivoRV, "RV", detalles);
			boolean archivoRF = generarArchivoDetalle(rutaBase, RutaNombreArchivoRF, "RF", detalles);
			
			log.write("Generarcion Archivos CSV completa");
			//Comprimir ambos archivos en un ZIP
			List<String> archivos = Arrays.asList(RutaNombreArchivoRV,RutaNombreArchivoRF);
			/*INICIO EDN - OPTIMUSPE-1371 y OPTIMUSPE-1370 */
			final String nombreZip = ("PE").equals(localizacionSistema) ? "cus/"+fechaServidorApps+"_DetalleKardex_RV_RF.zip" : "cus/"+fechaServidorApps+"_DetalleTarjetaExistencia_RV_RF.zip";
			/*FIN EDN - OPTIMUSPE-1371 y OPTIMUSPE-1370 */
			
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
			throw e;
		}
	}
	
	private boolean generarArchivoDetalle(String rutaBase, String rutaNombreArchivo, String clasificacionTipoInstrumento, List<TarjetaExistenciaDetalle> detalles) {
		try {
			//cambiar formato de decimales
			DecimalFormat df = new DecimalFormat("#####0.00");
		    DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
		    dfs.setDecimalSeparator('.');
		    df.setDecimalFormatSymbols(dfs);
		    
		    DateFormat dateFormar = new SimpleDateFormat("dd-MM-yyyy");
		    
			String templateDetallePosciones = rutaBase + "template_detalle_posiciones.xls";
			try(FileOutputStream outputStream = new FileOutputStream(rutaNombreArchivo)){
				try(FileInputStream fis = new FileInputStream(new File(templateDetallePosciones))){
			    	try(Workbook workbook = WorkbookFactory.create(fis);){
			    		Sheet sheet = workbook.getSheetAt(0);
			    		int rowCount = sheet.getLastRowNum();
			    		String ultimoNemo = "";
			    		for(TarjetaExistenciaDetalle detalle : detalles){
			    			if(detalle.IdClasificacionTipoInstrumento.equals(clasificacionTipoInstrumento)) {
			    				
			    				if(detalle.Nemotecnico == null){
			    					detalle.Nemotecnico = "";
			    				}
			    				
		    					if(!ultimoNemo.equals(detalle.Nemotecnico)) {
			    					//nemo nuevo, saltar una linea extra
			    					if(!ultimoNemo.equals("")) {
			    						//si no es la primera linea
			    						sheet.createRow(++rowCount);
			    					}
			    					ultimoNemo = detalle.Nemotecnico;
			    				}
			    				
			    				Row row = sheet.createRow(++rowCount);
			    				int columnCount = 0;
			    				//nemo (ok)
			    				Cell cell = row.createCell(columnCount);
			    				cell.setCellValue(detalle.Nemotecnico);

			    				
			    				//n° poliza 
			    				cell = row.createCell(++columnCount);
			    				if(detalle.NumeroPoliza == null){
			    					cell.setCellValue("0");
			    				}else{
			    					cell.setCellValue(detalle.NumeroPoliza);
			    				}
			    				
			    				//Tipo (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(detalle.TipoUI);
			    				//fecha (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(dateFormar.format(detalle.FechaProceso));

			    				//Saldo Inicial
			    				BigDecimal cantidadInicial = detalle.SaldoCantidad.add(detalle.CantidadEgreso).subtract(detalle.CantidadIngreso);
			    				BigDecimal montoInicial = detalle.SaldoMontoPrecioAdquisicion;
			    				BigDecimal cuSaldoInicial = BigDecimal.valueOf(0);
			    				//cantidad inicial (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format(cantidadInicial));
			    				//soles (monto compra) (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format(montoInicial));
			    				//CU (cu compra) (ok)
			    				cell = row.createCell(++columnCount);
			    				if(montoInicial.longValue() > 0 && cantidadInicial.longValue()>0) {
			    					cuSaldoInicial = montoInicial.divide(cantidadInicial, 2, RoundingMode.HALF_EVEN);
			    					cell.setCellValue(df.format( cuSaldoInicial ));
			    				}else {
			    					cell.setCellValue("-");
			    				}
			    				
			    				//compras
			    				BigDecimal cantidadCompra = detalle.CantidadIngreso;
			    				BigDecimal montoCompra = detalle.MontoIngreso;
			    				BigDecimal cuCompra = BigDecimal.valueOf(0);
			    				//cantidad inicial (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format(cantidadCompra));
			    				//soles (monto compra) (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format(montoCompra));
			    				//CU (cu compra) (ok)
			    				cell = row.createCell(++columnCount);
			    				if(cantidadCompra.longValue() > 0 && montoCompra.longValue()>0) {
			    					cuCompra = montoCompra.divide(cantidadCompra, 2, RoundingMode.HALF_EVEN);
			    					cell.setCellValue(df.format( cuCompra ));
			    				}else {
			    					cell.setCellValue("-");
			    				}
			    				
			    				//ventas
			    				BigDecimal cantidadVenta = detalle.CantidadEgreso;
			    				BigDecimal montoVenta = detalle.MontoEgreso;
			    				BigDecimal cuVenta = BigDecimal.valueOf(0);
			    				//Bolsa
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(detalle.IdBolsa);
			    				//cantidad venta (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format( cantidadVenta ));
			    				//monto de venta (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format( montoVenta ));
			    				//cu de venta
			    				cell = row.createCell(++columnCount);
			    				if(cantidadVenta.longValue() > 0 && montoVenta.longValue()>0) {
			    					cuVenta = montoVenta.divide(cantidadVenta, 2, RoundingMode.HALF_EVEN);
			    					cell.setCellValue(df.format( cuVenta ));
			    				}else {
			    					cell.setCellValue("-");
			    				}
			    				
			    				//costo de ventas
			    				BigDecimal cantidadCostoVenta = cantidadVenta;
			    				BigDecimal cuCostoVenta = BigDecimal.valueOf(0);
			    				if(detalle.MontoIngreso.add(montoInicial).longValue() > 0 && detalle.CantidadIngreso.add(cantidadInicial).longValue()>0) {
			    					cuCostoVenta = detalle.MontoIngreso.add(montoInicial).divide(detalle.CantidadIngreso.add(cantidadInicial), 2, RoundingMode.HALF_EVEN);
			    				}
			    				BigDecimal montoCostoVenta = cantidadCostoVenta.multiply( cuCostoVenta );
			    				//cantidad costo de venta (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format( cantidadCostoVenta ));
			    				//monto costo de venta (ok)
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format( montoCostoVenta ));
			    				//cu costo de venta
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(df.format( cuCostoVenta ));
			    				   				
			    				//Saldo Final
			    				BigDecimal cantidadSaldoFinal = cantidadCostoVenta.add(detalle.CantidadIngreso.add(cantidadInicial));
			    				BigDecimal montoSaldoFinal = montoCostoVenta.add(detalle.MontoIngreso.add(montoInicial));
			    				BigDecimal cuSaldoFinal = BigDecimal.valueOf(0);
			    				if(montoSaldoFinal.longValue()>0 && cantidadSaldoFinal.longValue() > 0) {
			    					cuSaldoFinal = montoSaldoFinal.divide(cantidadSaldoFinal, 2, RoundingMode.HALF_EVEN);
			    				}
			    				BigDecimal interesesPercibidos = detalle.MontoEgreso.subtract(montoCostoVenta);
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
			    				
			    				/*INICIO EDN - OPTIMUSPE-1371 y OPTIMUSPE-1370 */
			    				//Moneda
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(detalle.IdMoneda);
			    				
			    				//Origen del Instrumento
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(detalle.OrigenInstrumento);
			    				
			    				//Presencia Bursatil
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(detalle.PresenciaBursatil);
			    				
			    				//Tipo de Renta
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(detalle.TipoRenta);
			    				
			    				//Pacto Andino
			    				cell = row.createCell(++columnCount);
			    				cell.setCellValue(detalle.PactoAndino);
			    				/*FIN EDN  */
			    				
			    			}
			    		}
			    		workbook.write(outputStream);
		    		}
		    	}
		    }
			return true;
		}catch(Exception e) {
			log.write(e);
			return false;
		}
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
		try (Connection con = PlataformaUtils.getMBPEConnection()) {
			con.setAutoCommit(false);
			SQL.exec(con, "set schema BSCL");
			SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
			// ---------------------------------
			try {
				GenerarDetallePosiciones apf = new GenerarDetallePosiciones();
				
				System.out.println(apf.logica(con,false));
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
