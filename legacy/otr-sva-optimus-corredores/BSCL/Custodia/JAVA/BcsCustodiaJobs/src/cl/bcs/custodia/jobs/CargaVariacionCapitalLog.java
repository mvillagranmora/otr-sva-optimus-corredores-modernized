package cl.bcs.custodia.jobs;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargaVariacionCapitalLog extends BcsJob implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(CargaVariacionCapitalLog.class);
		
	private final String SQL_SELECT_OBTENER_ULTIMA_FECHA_INGRESADA_OPT 			= loadResourceToString("cl/bcs/custodia/jobs/sql/CargaVariacionCapitalLog_SelectObtenerUltimaFechaIngresadaOptimus.sql");
	private final String SQL_SELECT_SEBRA_VARACION_CAPITAL_LOG 					= loadResourceToString("cl/bcs/custodia/jobs/sql/CargaVariacionCapitalLog_SelectVariacionCapitalLog.sql");
	private final String SQL_INSERT_OPTIMUS_VARACION_CAPITAL_LOG_PROCESO_PASO	= loadResourceToString("cl/bcs/custodia/jobs/sql/CargaVariacionCapitalLog_InsertVariacionCapitalLogProcesoPaso.sql");
	private final String SQL_INSERT_OPTIMUS_VARACION_CAPITAL_LOG_NUEVOS			= loadResourceToString("cl/bcs/custodia/jobs/sql/CargaVariacionCapitalLog_InsertVariacionCapitalLogNuevos.sql");

	private boolean DEBUG = false;

	public static class VariacionCapitalLog extends BcsPojo {
		
		public Timestamp HoraAuditoria;
		public BigDecimal Secuencia;
		public String Mercado;
		public String Nemo;
		public String Tipo;
		public String Descripcion;
		public String NemoNuevo;
		public String NemoOpcion;
		public String Moneda;
		public Timestamp FechaPago;
		public Timestamp FechaInicioOpcion;
		public Timestamp FechaLimite;
		public Timestamp FechaMovimiento;
		public Timestamp FechaFinOpcion;
		public Timestamp FechaCorte;
		public BigDecimal AccionesConDerecho;
		public BigDecimal NumeroCupon;
		public BigDecimal NumeroDividendo;
		public BigDecimal ValorAccion;
		public BigDecimal FactorAntiguas;
		public BigDecimal FactorNuevas;
		public BigDecimal NumeroAccionesAntiguas;
		public BigDecimal NumeroAccionesNuevas;
		public BigDecimal PrecioAntes;
		public BigDecimal PrecioDespues;
		public BigDecimal ValorAntesDivision;
		public BigDecimal ValorDespuesDivision;
		public BigDecimal ValorLibroAjustado;
		public BigDecimal ValorSuscripcion;
		public String ActualizaNaa;
		public String TipoPrepago;
		public String TipoDividendo;
		public String Destino;
		public String IndicadorNoInscrito;
		public String IndicadorProceso;
		public String UsuarioModifica;
		public String TerminalModifica;
		public String TipoModificacion;
		public Long Indice;	
			
	}
	
	public static class UltimaCarga extends BcsPojo {
		public Timestamp UltimaHoraAuditoriaCargadaOpt;
	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		
		logger.info("INICIO::JOB::CargaVariacionCapitalLog");
        String DBdriver, DBuser, DBpass, DBurl, DBprop, anio;
        SQL _sql = new SQL();
        
		List<VariacionCapitalLog> listadoVariacionCapitalLogProcesoPaso =  new ArrayList<VariacionCapitalLog>();
		HashMap<String, Object> parametros 	= new HashMap<String, Object>();
		
        try {
        	
        	Properties prop = new Properties();
        	InputStream input;

    		if( DEBUG ) {
                input = new FileInputStream("C:\\BCS\\Properties\\ruebas.properties");
    		} else {
        		Long p_IdEmpresa = Long.parseLong(this.getParam("p_IdEmpresa"));
            	String RutaArchivosConfiguracion = Utils.getConfiguracion(con,  Utils.getSchema(), p_IdEmpresa, "PLA.RutaArchivosConfiguracion");
                input = new FileInputStream(RutaArchivosConfiguracion+"ruebas.properties");
    		}
    		
            prop.load(input);
            DBdriver=prop.getProperty("DBdriver");
            DBuser=prop.getProperty("DBuser");
            DBpass=prop.getProperty("DBpass");
            DBurl=prop.getProperty("DBurl");
            DBprop=prop.getProperty("DBprop");
            input.close();
            logger.info("Configuracion ruebas.properties cargada");
            
        } catch(Exception e) {
        	
        	logger.info(e);
        	throw new InterruptedException("ERROR NO SE PUEDE LEER ruebas.properties");
        	
        }  	
    	
    	BasicDataSource dsTarget = new BasicDataSource();    	 
    	try {
    		
            dsTarget.setDriverClassName(DBdriver);
            dsTarget.setUsername(DBuser);
            dsTarget.setPassword(DBpass);
            dsTarget.setUrl(DBurl);           
            dsTarget.setConnectionProperties(DBprop);
            dsTarget.setDefaultAutoCommit(true);
            logger.info("Conectado a RUEBAS");
            
    	} catch( Exception e ){
    		
    		logger.info(e);
        	throw new InterruptedException("ERROR NO SE PUEDE OBTENER CONEXION RUEBAS DB");
        	
        }
        
        try( Connection conRU = dsTarget.getConnection() ) {  
        	
			logger.info("Obteniendo ultima HoraAuditoria cargada en optimus");
			List<UltimaCarga> listUltimaHoraAuditoriaCargadaOpt  = _sql.query(con, SQL_SELECT_OBTENER_ULTIMA_FECHA_INGRESADA_OPT, new HashMap<String,Object>(0), UltimaCarga.class);
			if ( listUltimaHoraAuditoriaCargadaOpt.size() > 0 ) {
				logger.info("Ultima HoraAuditoria cargada en optimus encontrada");
				parametros.put("UltimaHoraAuditoriaCargadaOpt", listUltimaHoraAuditoriaCargadaOpt.get(0).UltimaHoraAuditoriaCargadaOpt);
			} else {
				logger.info("Ultima HoraAuditoria cargada no encontrada en optimus, se asigna por defecto");
				parametros.put("UltimaHoraAuditoriaCargadaOpt",new Timestamp(0L));
			}
			logger.info("Obteniendo logs de Variacion Capital en RUEBAS");
			listadoVariacionCapitalLogProcesoPaso = _sql.query(conRU, SQL_SELECT_SEBRA_VARACION_CAPITAL_LOG, parametros, VariacionCapitalLog.class);
        } catch( Exception er ) {
        	
        	logger.info(er);
        	throw new InterruptedException("ERROR NO FUE POSIBLE OBTENER LOS LOGS DE VARACION CAPITAL");
        	
        }
        

        if ( listadoVariacionCapitalLogProcesoPaso.size() > 0 ) {
        	
        	logger.info("Logs de Variacion Capital Encontrados, insertando en tabla de paso");
        	_sql.batch(con, SQL_INSERT_OPTIMUS_VARACION_CAPITAL_LOG_PROCESO_PASO, listadoVariacionCapitalLogProcesoPaso, VariacionCapitalLog.class);
        	logger.info("Se procede a insertar solo los nuevos registros en optimus");
        	_sql.batch(con, SQL_INSERT_OPTIMUS_VARACION_CAPITAL_LOG_NUEVOS, new ArrayList<VariacionCapitalLog>(0), VariacionCapitalLog.class);

        } else {
        	logger.info("No hay Logs de Variacion Capital nuevos que ingresar");
        }
        
		logger.info("FIN::JOB::CargaVariacionCapitalLog");
        return true;
        
	}

	@Override
    public void onError(Throwable t) {
    	logger.info(t.getMessage());
    	logger.info("****** ERROR TAREA  *******");
        t.printStackTrace();
    }

	@Override
	public void pause() {
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}	
	
    public static void main(String[] args) {
        System.out.println("== INICIO ==");       
        try {        	
            Connection con = PlataformaUtils.getDESAConnection();
            try {
            	CargaVariacionCapitalLog demo=new CargaVariacionCapitalLog();
            	demo.DEBUG = true;
            	demo.transaction(con);
                con.commit();
            } catch (Throwable er) {
                er.printStackTrace();
                con.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");       
    }
	
}
