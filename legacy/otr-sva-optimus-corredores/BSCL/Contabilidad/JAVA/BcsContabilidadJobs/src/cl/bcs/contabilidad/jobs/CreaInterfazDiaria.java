package cl.bcs.contabilidad.jobs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import cl.bcs.contabilidad.CreaBroadCuentas;
import cl.bcs.contabilidad.CreaBroadMovimiento;
import cl.bcs.contabilidad.CreaBroadSaldo;
import cl.bcs.contabilidad.CreaCuentasBPC;
import cl.bcs.contabilidad.CreaDescriptivasBroad;
import cl.bcs.contabilidad.CreaMovimientoBPC;
import cl.bcs.contabilidad.SFTPConnector;
import cl.bcs.contabilidad.jobs.CreaInterfazDemanda.Contador;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CreaInterfazDiaria extends BcsJob implements JavaCall
{
	/*
	private static final String USERNAME = "interfaz-bpc";
    private static final String HOST = "161.238.34.90";
    private static final int PORT = 22;
    private static final String PASSWORD = "user123";
    */
    private final String SQL_CONSULTA = loadResourceToString("cl/bcs/contabilidad/sql/ConsultaContadorInterfazSIGA.sql");
	
    
	public static class  Contador {
		
		public Long Cont;		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		return null;
	}
	public void logica(Connection con, Properties properties) throws Exception {

		/*Properties properties = new Properties();
        InputStream input = new FileInputStream("Interfaz.properties");
        properties.load(input);
        */
		List<Contador> out = new ArrayList<Contador>();
    	Contador cont = null;
    	SQL _sql = new SQL();
    	Long contador = new Long(0);
        String path = ConfiguracionSistema.getParametroConfiguracionString(con, new Long(1), "PLATAFORMA", "PLA.RutaBaseArchivos"); ;
        cl.bcs.contabilidad.Log log = new cl.bcs.contabilidad.Log(path);
        try 
		{
			//String _fechaEjecucion   = "2017-05-23";
        	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        	Date currentdate = new Date();
        	String _fechaEjecucion = dateFormat.format(currentdate);     	
			//String _fechaEjecucion   = this.getParam("fechaEjecucion");
			String date_s = _fechaEjecucion; 
			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd"); 
			Date date = dt.parse(date_s); 
			Calendar calendar = Calendar.getInstance();
		    calendar.setTime(date); // Configuramos la fecha que se recibe
		    calendar.add(Calendar.DAY_OF_YEAR, -1);  // numero de días a añadir, o restar en caso de días<0
			SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
			_fechaEjecucion = dt1.format(calendar.getTime());
			
			
			HashMap<String, Object> parmsIN = new HashMap<String, Object>();
			parmsIN.put("FechaEjecucion", _fechaEjecucion);
			out = _sql.query(con, SQL_CONSULTA, parmsIN , Contador.class);
			out.toString();
			ListIterator<Contador> iter = out.listIterator(out.size());
			while (iter.hasPrevious())
			{
				cont = new Contador();
				cont = iter.previous();	
				contador = cont.Cont;
			}
			
			String rutaDescargaBroad = "";
			String rutaDescargaBPC = "";
			String nombreArchivo = "";
			String nombreArchivoCtr = "";
			log.EscribirLog("INF", "Estableciendo conexion a SAP");
			
			BasicDataSource dsb1 = PlataformaUtils.getDESAPEB1Datasource();
			//BasicDataSource dsb1 = PlataformaUtils.getDESAB1Datasource();
			Connection conSAP = dsb1.getConnection();
			
			rutaDescargaBroad = path + "Transmisiones/Salida/FI/" ;
			rutaDescargaBPC = path + "Transmisiones/Salida/BPC/" ;
			log.EscribirLog("INF", "Iniciando generacion de archivos");
			try
			{
				nombreArchivo = "B1_BROAD_CTAS.TXT";
				nombreArchivoCtr = "B1_CTRL_BROAD_CTAS.TXT";
				CreaBroadCuentas cbc = new CreaBroadCuentas();
				cbc.logica(conSAP, _fechaEjecucion, rutaDescargaBroad, nombreArchivo, nombreArchivoCtr, log);
			}
			catch (Exception e) 
			{
				log.EscribirLog("ERR", e.getMessage());
			}
			
			try
			{
				nombreArchivo = "B1_BROAD_MOV_CONT.TXT";
				nombreArchivoCtr = "B1_CTRL_BROAD_MOV_CONT.TXT";
				CreaBroadMovimiento cbm = new CreaBroadMovimiento();
				cbm.logica(conSAP,_fechaEjecucion, rutaDescargaBroad, nombreArchivo, nombreArchivoCtr, contador, log);
			}
			catch (Exception e) 
			{
				log.EscribirLog("ERR", e.getMessage());
			}
			
			
			try
			{
				nombreArchivo = "B1_BROAD_SALD_CONT.TXT";
				nombreArchivoCtr = "B1_CTRL_BROAD_SALD_CONT.TXT";
				CreaBroadSaldo cbs = new CreaBroadSaldo();
				cbs.logica(conSAP, _fechaEjecucion, rutaDescargaBroad, nombreArchivo, nombreArchivoCtr, contador, log);
			}
			catch (Exception e) 
			{
				log.EscribirLog("ERR", e.getMessage());
			}
			
			try
			{		
				CreaDescriptivasBroad des = new CreaDescriptivasBroad();
				nombreArchivo = "B1_DES_ENTIDAD.TXT";
				des.logicaEntidad(conSAP, rutaDescargaBroad, nombreArchivo, log);
				nombreArchivo = "B1_DES_TIPO_CTA.TXT";
				des.logicaTipoCuenta(conSAP, rutaDescargaBroad, nombreArchivo, log);
				nombreArchivo = "B1_DES_CC_PROD.TXT";
				des.logicaCentroCosto(conSAP, rutaDescargaBroad, nombreArchivo, log);
			}
			catch (Exception e) 
			{
				log.EscribirLog("ERR", e.getMessage());
			}
			
			
			try
			{
				nombreArchivo = "B1_CUENTAS_BPC.TXT";
				nombreArchivoCtr = "B1_CTRL_CUENTAS_BPC.TXT";
				CreaCuentasBPC cbbcp = new CreaCuentasBPC();
				cbbcp.logica(conSAP,_fechaEjecucion, rutaDescargaBPC, nombreArchivo, nombreArchivoCtr, log);
			}
			catch (Exception e) 
			{
				log.EscribirLog("ERR", e.getMessage());
			}
			
			try
			{
				nombreArchivo = "B1_BROAD_BPC.TXT";
				nombreArchivoCtr = "B1_CTRL_BROAD_BPC.TXT";
				CreaMovimientoBPC cbbcp = new CreaMovimientoBPC();
				cbbcp.logica(conSAP,_fechaEjecucion, rutaDescargaBPC, nombreArchivo, nombreArchivoCtr, log);
			}
			catch (Exception e) 
			{
				log.EscribirLog("ERR", e.getMessage());
			}
			
			
			/////////////////Establecer conexion FTP
			
			 try {
		            SFTPConnector sshConnector = new SFTPConnector();
		             
		            
		            sshConnector.connect(properties.getProperty("USERNAME"), properties.getProperty("PASSWORD"), properties.getProperty("HOST"), Integer.parseInt(properties.getProperty("PORT")));
		            sshConnector.removeDir("/home/interfaz-bpc/", path);
		            sshConnector.downloadDir("/home/interfaz-bpc/", path);		            
		            sshConnector.disconnect();
		        } catch (JSchException ex) {
		        	log.EscribirLog("ERR", ex.getMessage());
		            ex.printStackTrace();
		             
		            System.out.println(ex.getMessage());
		        } catch (IllegalAccessException ex) {
		        	log.EscribirLog("ERR", ex.getMessage());
		            ex.printStackTrace();
		             
		            System.out.println(ex.getMessage());
		        } catch (IOException ex) {
		        	log.EscribirLog("ERR", ex.getMessage());
		            ex.printStackTrace();
		             
		            System.out.println(ex.getMessage());
		        } catch (SftpException ex) {
		        	log.EscribirLog("ERR", ex.getMessage());
		            ex.printStackTrace();
		             
		            System.out.println(ex.getMessage());
		        }
			
		} 
		catch (Exception e) 
		{
			log.EscribirLog("ERR", e.getMessage());
		} 
		finally 
		{
			
		}
	
	}
	
	public static void main(String[] args) throws Exception 
	{
		Properties properties = new Properties();
        InputStream input = new FileInputStream("CreaInterfazSIGA.properties");
        properties.load(input);
        
		BasicDataSource dsb1 = PlataformaUtils.getDESADatasource();
		Connection con = dsb1.getConnection();
		CreaInterfazDiaria demo = new CreaInterfazDiaria();
		demo.logica(con, properties);
 	
		System.out.println("== END ==");
	}
	
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		
		try
		{
			Properties properties = new Properties();
	        InputStream input = new FileInputStream("CreaInterfazSIGA.properties");
	        properties.load(input);
	        
			logica(con, properties);
		}
		 catch (Exception e) 
		{
			return false;
		} 
		finally 
		{

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
}
