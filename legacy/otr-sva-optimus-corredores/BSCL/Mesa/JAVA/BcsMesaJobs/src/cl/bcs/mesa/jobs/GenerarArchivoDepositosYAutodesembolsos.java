package cl.bcs.mesa.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarArchivoDepositosYAutodesembolsos extends BcsJob{
	private final Log log = new Log("MES.GenerarArchivoDepositosYAutodesembolos");
	
	private final FileLoader fileLoader = new FileLoader();
	private final String SQL_DEPOSITOS_Y_AUTODESEMBOLSOS = fileLoader.loadResourceToString("cl/bcs/mesa/jobs/sql/CarterasDepositosYAutodesembolsos_Consulta.sql");
	private final String SQL_UPDATE_ID_PROGRAMA = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaSalida WHERE \"IdPrograma\" = :IdPrograma;";
	
	public static class ActualizarPrograma{
		public String 	RutaSalida;
		public Long 	IdPrograma;
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}
	
	public static class DepositosYAutodesembolsos{
		public String IdEmisor;
		public String FechaOperacion;
		public String FechaVencimiento;
		public String IdMoneda;
		public BigDecimal Tasa;
		public BigDecimal MontoInteres;
		public BigDecimal Monto;
		public String NumeroCuentaBancaria;
		public BigDecimal ValorPresente;
		public BigDecimal MontoInteresActual;
		public BigDecimal ValorPresenteT1;
		public BigDecimal MontoAPlazoMonedaInstrumento;
		public Long PlazoEnDiasOperacion;
		
				@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica (con,false);
	}
	
	public boolean logica(Connection conn,boolean debug) throws Exception {
		try{
			SQL sql = new SQL();
			/* Obtener Parametros del JOB*/
			Long IdEmpresa = null;
			Long IdUsuario = null;
			Long IdInstrumento = null;
			String IdTipoInstrumento = null;
			Long IdCliente = null; 
			String FechaDesde = null;
			String FechaHasta = null;
			
			if(!debug){
				IdEmpresa = this.getOptionalParam("IdEmpresa").map((i) -> new Long(i)).orElse( (long) 1);
				IdUsuario=this.getOptionalParam("IdUsuario").map((i) -> new Long(i)).orElse( (long) 3 );
				IdInstrumento = this.getOptionalParam("IdInstrumento").map((i) -> i.equals("") ? (Long)null : new Long(i)).orElse(null);
				IdTipoInstrumento = this.getOptionalParam("IdTipoInstrumento").map((i) -> i.equals("") ? null : new String(i)).orElse(null);
				IdCliente = this.getOptionalParam("IdCliente").map((i) -> i.equals("") ? (Long)null :new Long(i)).orElse(null); 
				FechaDesde = this.getOptionalParam("FechaDesde").map((i) -> i.equals("") ? null : new String(i)).orElse(null);
				FechaHasta = this.getOptionalParam("FechaHasta").map((i) -> i.equals("") ? null : new String(i)).orElse(null);
			}else {
				IdEmpresa = 1L;
				IdUsuario = 21L;
				IdInstrumento = 180001L;
				IdTipoInstrumento = "AUT";
				IdCliente = 175022L;
				FechaDesde = "2018-04-10";
				FechaHasta = "2018-04-10";
			}
			
			String tFechaHoraSistema	= ConfiguracionSistema.getParametroConfiguracionString(conn, IdEmpresa,"PLATAFORMA", "PLA.FechaSistema");

			System.out.println( String.format("Usuario %d de la empresa %d, ha solicitado generar archivo resumen posiciones", IdUsuario, IdEmpresa) );
			
			String rutaBase = Utils.getConfiguracion(conn, Utils.getSchema(), IdEmpresa, "PLA.RutaBaseArchivos");
			
			//Consultar el servicio de resumen de posiciones
			HashMap<String,Object>  paramsIn = new HashMap<String,Object>();
			paramsIn.put("IdInstrumento", IdInstrumento);
			//paramsIn.put("IdTipoInstrumento", IdInstrumento);
			paramsIn.put("IdCliente", IdCliente);
			paramsIn.put("FechaDesde", FechaDesde);
			paramsIn.put("FechaHasta", FechaHasta);
			paramsIn.put("IdTipoOperacion", IdTipoInstrumento == "AUT" ? "TAUTDI" : "TDEPLA");
			
			List<DepositosYAutodesembolsos>  datos  = sql.query(conn, SQL_DEPOSITOS_Y_AUTODESEMBOLSOS, paramsIn, DepositosYAutodesembolsos.class);			

			//Guardar archivos en servidor de aplicaciones
			String fechaServidorApps = FechaDB.ObtenerFechaHoraSistemaLocal(conn, IdEmpresa);
									
			String fechaArchivo = fechaServidorApps.replace("-", "").replace(" ", "").replace(":", "");
			
			String TipoArchivo = IdTipoInstrumento.equals("DEP") ? "DEPOSITOS" : "AUTODESEMBOLSOS";
			
			String NombreArchivo = fechaArchivo + TipoArchivo + ".xls";
			String RutaNombreArchivo = rutaBase + "mes/" + NombreArchivo;
			String RutaPrograma = "mes/" + NombreArchivo;		

			log.write(String.format("Cantidad de lineas %d con fecha: %s", datos.size(),fechaServidorApps) );
			
			//crear excel peru
			boolean archivo = generarArchivoDetalle(rutaBase, RutaNombreArchivo, datos, TipoArchivo, tFechaHoraSistema);
			
			log.write("Generarcion Archivos CSV completa");
			
			//Actualizar programa con la ruta del ZIP
			if(archivo){
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(RutaPrograma, this.getJobId()));
				int [] res = sql.batch(conn, SQL_UPDATE_ID_PROGRAMA, actualizar, ActualizarPrograma.class);
				log.write( String.format("Actualizar job: %d, con la ruta %s, resultado : %d", this.getJobId(), RutaPrograma, ((res!=null && res.length>0) ? res[0] : 0) ));
				return true;
			}else{
				return false;
			}
		}catch(Exception e){
			log.write(e);
		}
		return true;
	}
	
	private boolean generarArchivoDetalle(String rutaBase, String rutaNombreArchivo, List<DepositosYAutodesembolsos> detalles, String tipoArchivo, String fechaHoraSistema) throws Exception {
		try {
			//cambiar formato de decimales
			DecimalFormat df = new DecimalFormat("#####0.00");
		    DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
		    dfs.setDecimalSeparator('.');
		    df.setDecimalFormatSymbols(dfs);
		    		    
			String templateDetallePosciones = rutaBase + "template_depositos_y_autodesembolsos.xls";
			try(FileOutputStream outputStream = new FileOutputStream( rutaNombreArchivo );){
				try(FileInputStream fis = new FileInputStream(new File(templateDetallePosciones));){
			    	try(Workbook workbook = WorkbookFactory.create(fis);){
			    		Sheet sheet = workbook.getSheetAt(0);
			    		int rowCount = sheet.getLastRowNum();
			    		int rowNumero = --rowCount;
			    		Cell encabezado = sheet.getRow(0).getCell(0);
			    		
			    		if (tipoArchivo == "AUTODESEMBOLSOS") {
			    			encabezado.setCellValue("Empresa que otorga el prestamo o financiamiento");
			    		}
			    		
			    		for(DepositosYAutodesembolsos detalle : detalles){	
		    				BigDecimal devengoInteresDiario = BigDecimal.ZERO;
		    				BigDecimal valorPresente = BigDecimal.ZERO;
		    				BigDecimal valorPresenteT1 = BigDecimal.ZERO;


		    				Date fechaSistema = new SimpleDateFormat("yyyy-MM-dd").parse(fechaHoraSistema);//fechaHoraSistema
		    				Date fechaVen = new SimpleDateFormat("yyyy-MM-dd").parse(detalle.FechaVencimiento);		    				
							Date FechaValProximo = new Date();

		    				Calendar c = Calendar.getInstance(); 
							c.setTime(fechaSistema); 
							c.add(Calendar.DATE, 1);
							FechaValProximo = c.getTime();

		    				if(fechaSistema.before(fechaVen) || fechaSistema.equals(fechaVen)){
		    					valorPresente = detalle.ValorPresente;
		    					
		    					if(FechaValProximo.before(fechaVen) || FechaValProximo.equals(fechaVen))
		    						valorPresenteT1 = detalle.ValorPresenteT1;
		    					
		    					if(	valorPresenteT1.compareTo(BigDecimal.ZERO) != 0 && valorPresente.compareTo(BigDecimal.ZERO) != 0) 
		   							devengoInteresDiario = valorPresenteT1.subtract(valorPresente);
		    				}
		    				
		    				BigDecimal montoInteres = detalle.MontoAPlazoMonedaInstrumento.subtract(detalle.Monto);
							
		    				Row row = sheet.createRow(++rowNumero);
		    				int columnCount = 0;
		    				//IdEmisor
		    				Cell cell = row.createCell(columnCount);
		    				cell.setCellValue(detalle.IdEmisor);
		    				
		    				//FechaOperacion 
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(detalle.FechaOperacion);
		    				
		    				//FechaVencimiento
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(detalle.FechaVencimiento);
		    				
		    				//IdMoneda
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(detalle.IdMoneda);
		    				
		    				//Tasa
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(df.format(detalle.Tasa));
		    				
		    				//MontoInteres
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(df.format(montoInteres));
		    				
		    				//Monto
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(df.format(detalle.Monto));

		    				//Numero Cuenta Bancaria
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(detalle.NumeroCuentaBancaria);
		    				
		    				//Dias Totales
		    				
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(df.format(detalle.PlazoEnDiasOperacion));
		    				
		    				//Valor Final
										
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(df.format(detalle.MontoAPlazoMonedaInstrumento));
		    				
		    				//Valor Presente (t)
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(df.format(valorPresente));
		    				
		    				//Valor Presente  (t + 1)
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(df.format(valorPresenteT1));

		    				//Devengo Interes Diario
									
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(df.format(devengoInteresDiario));
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
		try (Connection con = PlataformaUtils.getQAPEConnection()) {
			con.setAutoCommit(false);
			SQL.exec(con, "set schema BSCL");
			SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
			// ---------------------------------
			try {
				GenerarArchivoDepositosYAutodesembolsos dya = new GenerarArchivoDepositosYAutodesembolsos();
				System.out.println(dya.logica(con,true));
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
