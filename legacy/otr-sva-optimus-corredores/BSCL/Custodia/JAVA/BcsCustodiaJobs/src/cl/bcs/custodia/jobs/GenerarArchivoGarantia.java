package cl.bcs.custodia.jobs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
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

public class GenerarArchivoGarantia extends BcsJob{
	private final Log log = new Log("CUS.GenerarArchivoGarantia");
	private final String ARCHIVO = "_Archivo_Garantia";
	private final int LARGO_ARCHIVO = 88;
	
	private final FileLoader fileLoader = new FileLoader();
	private final String SQL_GARANTIA_QUERY = fileLoader.loadResourceToString("cl/bcs/custodia/jobs/sql/GenerarDetalleGarantia_Consulta.sql");
	private final String SQL_UPDATE_ID_PROGRAMA = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaSalida WHERE \"IdPrograma\" = :IdPrograma;";
	
	public static class ActualizarPrograma{
		public String 	RutaSalida;
		public Long 	IdPrograma;
		public ActualizarPrograma(String RutaSalida,Long IdPrograma){
			this.RutaSalida = RutaSalida;
			this.IdPrograma = IdPrograma;
		}
	}
	
	public static class GarantiaBCP{
		public String DocumentoIdentidad;
		public String Nemotecnico;
		public String RazonSocial;
		public BigDecimal Monto;
		public Long IdMoneda;
		public String FechaUltimoMovimiento;
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
			Long idEmpresa = this.getOptionalParam("IdEmpresa").map((i) -> new Long(i)).orElse( (long) 1);
			Long idUsuario=this.getOptionalParam("IdUsuario").map((i) -> new Long(i)).orElse( (long) 3 );
			
			Long IdCliente = null;
			Long IdPortafolio = null;
			String IdTipoInstrumentoGarantizado = null; 
			Long IdContraparteGarantia = null;
			String IdOrigenGarantia = null;
			String IdEstado = null;
			String IdMonedaInstrumento = null; 
			Long IdInstrumento = null;
			Long FolioCarteraGarantizada = null;
			String FechaLiquidacion = null;
			System.out.println( String.format("Usuario %d de la empresa %d, ha solicitado generar archivo resumen posiciones", idUsuario, idEmpresa) );			
			String rutaBase = Utils.getConfiguracion(conn, Utils.getSchema(), idEmpresa, "PLA.RutaBaseArchivos");
			
			if(!debug){
				IdCliente = this.getOptionalParam("IdCliente").map((i) -> i.equals("") ? (Long)null : new Long(i)).orElse(null);
				IdPortafolio = this.getOptionalParam("IdPortafolio").map((i) -> i.equals("") ? (Long)null : new Long(i)).orElse(null);
				IdTipoInstrumentoGarantizado = this.getOptionalParam("IdTipoInstrumentoGarantizado").map((i) -> i.equals("") ? null :new String(i)).orElse(null); 
				IdContraparteGarantia = this.getOptionalParam("IdContraparteGarantia").map((i) -> i.equals("") ? (Long)null : new Long(i)).orElse(null);
				IdOrigenGarantia = this.getOptionalParam("IdOrigenGarantia").map((i) -> i.equals("") ? null : new String(i)).orElse(null);
				IdEstado = this.getOptionalParam("IdEstado").map((i) -> i.equals("") ? null : new String(i)).orElse(null);
				IdMonedaInstrumento = this.getOptionalParam("IdMonedaInstrumento").map((i) -> i.equals("") ? null : new String(i)).orElse(null); 
				IdInstrumento = this.getOptionalParam("IdInstrumento").map((i) -> i.equals("") ? (Long)null : new Long(i)).orElse(null);
				FolioCarteraGarantizada = this.getOptionalParam("FolioCarteraGarantizada").map((i) -> i.equals("") ? (Long)null : new Long(i)).orElse(null);
				FechaLiquidacion = this.getOptionalParam("FechaLiquidacion").map((i) -> i.equals("") ? null : new String(i)).orElse(null);
			}else {
				String valorblanco = "";
				IdCliente = valorblanco.equals("")  ? null : IdCliente;
				IdPortafolio = valorblanco.equals("")  ? null : IdPortafolio;
				IdTipoInstrumentoGarantizado = valorblanco.equals("")  ? null : IdTipoInstrumentoGarantizado; 
				IdContraparteGarantia = valorblanco.equals("")  ? null : IdContraparteGarantia;
				IdOrigenGarantia = valorblanco.equals("")  ? null : IdOrigenGarantia;
				IdEstado = valorblanco.equals("")  ? null : IdEstado;
				IdMonedaInstrumento = valorblanco.equals("")  ? null : IdMonedaInstrumento; 
				IdInstrumento = valorblanco.equals("")  ? null : IdInstrumento;
				FolioCarteraGarantizada = valorblanco.equals("")  ? null : FolioCarteraGarantizada;
				FechaLiquidacion = "2017-07-26";
				rutaBase = "C:/mnt/optimuspeqa/empresa1/";
			}
			
			//Consultar el servicio de resumen de posiciones
			HashMap<String,Object>  paramsIn = new HashMap<String,Object>();
			paramsIn.put("IdCliente", IdCliente);
			paramsIn.put("IdPortafolio", IdPortafolio);
			paramsIn.put("IdTipoInstrumentoGarantizado", IdTipoInstrumentoGarantizado);
			paramsIn.put("IdContraparteGarantia", IdContraparteGarantia);
			paramsIn.put("IdOrigenGarantia", IdOrigenGarantia);
			paramsIn.put("IdEstado", IdEstado);
			paramsIn.put("IdMonedaInstrumento", IdMonedaInstrumento);
			paramsIn.put("IdInstrumento", IdInstrumento);
			paramsIn.put("FolioCarteraGarantizada", FolioCarteraGarantizada);
			paramsIn.put("FechaLiquidacion", FechaLiquidacion);
			
			List<GarantiaBCP>  gtiaDetalle  = sql.query(conn, SQL_GARANTIA_QUERY, paramsIn, GarantiaBCP.class);
			
			String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
			
			//Guardar archivos en servidor de aplicaciones
			String fechaServidorApps = Fecha.toFormat(FechaDB.ObtenerFechaHoraTs(conn, idEmpresa),"yyyyMMddHHmmssSSS");
			
			//String NombreArchivo = fechaServidorApps + ARCHIVO + ".xls";
			//String RutaNombreArchivo = rutaBase + "cus/" + NombreArchivo;
			//String RutaPrograma = "cus/" + NombreArchivo;
			/*
			//crear excel peru
			boolean archivo = generarArchivoDetalle(rutaBase, RutaNombreArchivo, gtiaDetalle);
			log.write("Generarcion Archivos CSV completa");
			//Actualizar programa con la ruta del ZIP
			if(archivo){
				List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
				actualizar.add(new ActualizarPrograma(RutaPrograma, this.getJobId()));
				int [] res = sql.batch(conn, SQL_UPDATE_ID_PROGRAMA, actualizar, ActualizarPrograma.class);
				log.write( String.format("Actualizar job: %d, con la ruta %s, resultado : %d", this.getJobId(), RutaNombreArchivo, ((res!=null && res.length>0) ? res[0] : 0) ));
				return true;
			}else{
				return false;
			}
			*/
			log.write(String.format("Cantidad de lineas %d con fecha: %s", gtiaDetalle.size(),fechaServidorApps) );
			String NombreArchivo = fechaServidorApps + ARCHIVO + ".txt";
			String Archivotxt = rutaBase + "cus/" + NombreArchivo;
			String RutaPrograma = "cus/" + NombreArchivo;
			File archivo = new File(Archivotxt);
			BufferedWriter bwArchivo;
			bwArchivo = new BufferedWriter(new FileWriter(archivo));
			//crear txt peru
			try{
				log.write("Inicio generacion archivo TXT ");
				String detalle = "";
				int contadorRegistros = 0;
				int contadorRegistrosErr = 0;
				String constante = "P";
				String ret;
				for(GarantiaBCP gtia : gtiaDetalle) {
	    			ret = "";
					ret += String.format("%-10s", 	(gtia.DocumentoIdentidad.length() > 10)? gtia.DocumentoIdentidad.substring(0, 10): gtia.DocumentoIdentidad);
					ret += String.format("%-12s", 	(gtia.Nemotecnico.length() > 12)? gtia.Nemotecnico.substring(0, 12): gtia.Nemotecnico);
					ret += String.format("%-40s", 	(gtia.RazonSocial.length() > 40)? gtia.RazonSocial.substring(0, 40): gtia.RazonSocial);
					ret += String.format("%015d", 	gtia.Monto.intValue());
					ret += String.format("%02d", 	gtia.IdMoneda.intValue());
					ret += String.format("%-8s", 	(gtia.FechaUltimoMovimiento.toString().length() > 8)? gtia.FechaUltimoMovimiento.toString().substring(0, 8): gtia.FechaUltimoMovimiento.toString());
					ret += String.format("%-1s",	constante);
					
					if(ret.length() != LARGO_ARCHIVO){
						log.write(String.format("Registro con Error de tama�o del registro, se esperaba:" + LARGO_ARCHIVO + ",se obtuvo:" + ret.length() + ",registro:" + ret));
						contadorRegistrosErr++;
					}else{
						bwArchivo.write(ret);
						bwArchivo.newLine();
						contadorRegistros++;
					}
				}
				log.write("Generarcion Archivos TXT completa.");
				if(contadorRegistros > 0) {
					List<ActualizarPrograma> actualizar = new ArrayList<ActualizarPrograma>();
					actualizar.add(new ActualizarPrograma(RutaPrograma, this.getJobId()));
					int [] res = sql.batch(conn, SQL_UPDATE_ID_PROGRAMA, actualizar, ActualizarPrograma.class);
					log.write( String.format("Actualizar job: %d, con la ruta %s, resultado : %d", this.getJobId(), Archivotxt, ((res != null && res.length > 0) ? res[0] : 0) ));
					conn.commit();
					return true;
				}else {
					return false;
				}
				
			}catch(Exception e){
				log.write(e);
				conn.rollback();
				throw e;
			}finally{
				 if(bwArchivo !=null){
					 bwArchivo.close();
				 }
			}
		}catch(Exception e){
			log.write(e);			
			throw e;
		}
	}
	
	private boolean generarArchivoDetalle(String rutaBase, String rutaNombreArchivo, List<GarantiaBCP> detalles) {
		try {
			//cambiar formato de decimales
			DecimalFormat df = new DecimalFormat("#####0.00");
		    DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
		    dfs.setDecimalSeparator('.');
		    df.setDecimalFormatSymbols(dfs);
		    
		    DateFormat dateFormar = new SimpleDateFormat("dd-MM-yyyy");
		    
			String templateDetallePosciones = rutaBase + "template_garantia_bcp.xls";
			try(FileOutputStream outputStream = new FileOutputStream( rutaNombreArchivo );){
				try(FileInputStream fis = new FileInputStream(new File(templateDetallePosciones));){
			    	try(Workbook workbook = WorkbookFactory.create(fis);){
			    		Sheet sheet = workbook.getSheetAt(0);
			    		int rowCount = sheet.getLastRowNum();
			    		String ultimoNemo = "";
			    		for(GarantiaBCP detalle : detalles){	
			    			/*
			    			public String DocumentoIdentidad;
			    			public String Nemotecnico;
			    			public String RazonSocial;
			    			public BigDecimal Monto;
			    			public String IdMoneda;
			    			public Date FechaUltimoMovimiento;
			    			
		    				if(!ultimoNemo.equals(detalle.Nemotecnico)) {
		    					//nemo nuevo, saltar una linea extra
		    					if(!ultimoNemo.equals("")) {
		    						//si no es la primera linea
		    						sheet.createRow(++rowCount);
		    					}
		    					ultimoNemo = detalle.Nemotecnico;
		    				}
		    				*/
		    				Row row = sheet.createRow(++rowCount);
		    				int columnCount = 0;
		    				//DocumentoIdentidad
		    				Cell cell = row.createCell(columnCount);
		    				cell.setCellValue(detalle.DocumentoIdentidad);
		    				
		    				//NemoTecnico 
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(detalle.Nemotecnico);
		    				
		    				//RazonSocial
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(detalle.RazonSocial);
		    				
		    				//Monto
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(df.format(detalle.Monto));
		    				
		    				//Moneda
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(detalle.IdMoneda);
		    				
		    				//Fecha Ultimo Movimiento
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue(dateFormar.format(detalle.FechaUltimoMovimiento));
		    				
		    				//Constante "p"
		    				cell = row.createCell(++columnCount);
		    				cell.setCellValue("P");

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
		try (Connection con = PlataformaUtils.getDESAPEConnection()) {
			con.setAutoCommit(false);
			SQL.exec(con, "set schema BSCL");
			SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
			// ---------------------------------
			try {
				GenerarArchivoGarantia apf = new GenerarArchivoGarantia();
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
