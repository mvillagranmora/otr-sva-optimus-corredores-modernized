package cl.bcs.ordenes.jobs;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ControlMercadoCargaModificacionRuebas extends BcsJob{
	
	private static final Logger logger = LogManager.getLogger(ControlMercadoCargaModificacionRuebas.class);
	private boolean DEBUG = false;
	
	private final String SQL_SELECT_MODIFICACIONES_CARGADAS = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoCargaModificacionRuebas_SelectModificacionesCargadas.sql");
	private final String SQL_SELECT_MODIFICACIONES_RUEBAS = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoCargaModificacionRuebas_SelectModificaciones.sql");
	private final String SQL_INSERT_MODIFICACIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoCargaModificacionRuebas_InsertModificaciones.sql");
	private final String SQL_SELECT_CORREDORA = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoCargaModificacionRuebas_SelectCorredora.sql");
	
	private final String SQL_CREATE_TEMP_MODIFICACIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoCargaModificacionRuebas_CreateTempModificaciones.sql");
	private final String SQL_INSERT_TEMP_MODIFICACIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoCargaModificacionRuebas_InsertTempModificaciones.sql");
	private final String SQL_DELETE_TEMP_MODIFICACIONES = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoCargaModificacionRuebas_DeleteTempModificaciones.sql");
	
	private final String SQL_SELECT_MODIFICACIONES_FILTRADAS = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoCargaModificacionRuebas_SelectModificacionesFiltradas.sql");
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica (con,false);
	}

	public static class RegistroModificacion {
		public BigDecimal FolioMod;
		public String FechaMod;
		public String Fecha;
		public BigDecimal CodComNuevo;
		public BigDecimal CodVenNuevo;
	}
	
	public static class Corredora {
		public String CodigoCorredora;
	}
	
	public boolean logica(Connection con,boolean debug) throws Exception {
		logger.info("INICIO::JOB::ConsultarModificacionesPalosRuebas");
		Long idEmpresa = 0L;
        if (debug) {
        	idEmpresa = 1L;
        } else{
        	 idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
        }
        
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String DBdriver, DBuser, DBpass, DBurl, DBprop;
		SQL sql = new SQL();
		HashMap<String, Object> parametros 	= new HashMap<String, Object>();
		parametros.put("IdEmpresa", idEmpresa);
		List<Corredora> codigoCorredora = sql.query(con, SQL_SELECT_CORREDORA, parametros, Corredora.class);
		if (codigoCorredora.size() == 0) {
			throw new Exception("ERROR Código de corredora no encontrado");
		}
		parametros.clear();
		try {	
        	Properties prop = new Properties();
        	InputStream input;
    		if( DEBUG ) {
                input = new FileInputStream("C:\\BCS\\Properties\\ruebas.properties");
    		} else {
            	String RutaArchivosConfiguracion = Utils.getConfiguracion(con,  Utils.getSchema(), idEmpresa, "PLA.RutaArchivosConfiguracion");
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
            dsTarget.setValidationQuery("SELECT 1");
            dsTarget.setTestOnBorrow(true);
            logger.info("Conectado a RUEBAS");
    	} catch( Exception e ){
    		logger.info(e);
        	throw new InterruptedException("ERROR NO SE PUEDE OBTENER CONEXION RUEBAS DB");
        }
    	List<RegistroModificacion> listaModificacionesRuebas = new ArrayList<RegistroModificacion>();
    	try( Connection conRU = dsTarget.getConnection()) {
    		parametros.put("FechaSistema", fechaSistema);
    		parametros.put("CodigoCorredora", codigoCorredora.get(0).CodigoCorredora);
    		listaModificacionesRuebas = sql.query(conRU, SQL_SELECT_MODIFICACIONES_RUEBAS, parametros, RegistroModificacion.class);
    		parametros.clear();
    	} catch( Exception er ) {
        	logger.info(er);
        	throw new InterruptedException("ERROR NO FUE POSIBLE OBTENER LOS REGISTROS DE LAS MODIFICACIONES");
        }
        
    	PreparedStatement pstmt = null;
        pstmt = con.prepareStatement(SQL_CREATE_TEMP_MODIFICACIONES);
        pstmt.execute();
        pstmt.close();
        sql.batch(con, SQL_INSERT_TEMP_MODIFICACIONES, listaModificacionesRuebas, RegistroModificacion.class);
    	
    	List<RegistroModificacion> modificacionesFiltradas = new ArrayList<RegistroModificacion>();
    	modificacionesFiltradas = sql.query(con, SQL_SELECT_MODIFICACIONES_FILTRADAS, parametros, RegistroModificacion.class);

    	pstmt = con.prepareStatement(SQL_DELETE_TEMP_MODIFICACIONES);
        pstmt.execute();
        pstmt.close();

        parametros.put("FechaSistema", fechaSistema);
    	List<RegistroModificacion> listaModificacionesCargadas = new ArrayList<RegistroModificacion>();
    	listaModificacionesCargadas = sql.query(con, SQL_SELECT_MODIFICACIONES_CARGADAS, parametros, RegistroModificacion.class);
    	parametros.clear();
    	List<RegistroModificacion> modificacionesNoCargadas = obtenerModificacionesNoCargadas(listaModificacionesCargadas, modificacionesFiltradas);
    	if (listaModificacionesRuebas.size() > 0) {
			sql.batch(con, SQL_INSERT_MODIFICACIONES, modificacionesNoCargadas, RegistroModificacion.class);
		}    	
		logger.info("FIN::JOB::ConsultarModificacionesPalosRuebas");
		return true;
	}
	
	private List<RegistroModificacion> obtenerModificacionesNoCargadas(List<RegistroModificacion> modificacionesCargadas, List<RegistroModificacion> modificacionesEnRuebas){
		List<RegistroModificacion> modificacionesNoCargadas = new ArrayList<RegistroModificacion>();
		HashMap<String,RegistroModificacion> hashModificacionesRuebas = new HashMap<String,RegistroModificacion>();
		Set<String> setModificacionesRuebas = new HashSet<String>();
		for (RegistroModificacion mod : modificacionesEnRuebas) {
			String llave = mod.FolioMod+mod.FechaMod;
			hashModificacionesRuebas.put(llave, mod);
			setModificacionesRuebas.add(llave);
		}
		Set<String> setModificacionesCargadas = new HashSet<String>();
		for (RegistroModificacion mod : modificacionesCargadas) {
			String llave = mod.FolioMod+mod.FechaMod;
			setModificacionesCargadas.add(llave);
		}
		Set<String> setModificacionesNoCargadas = new HashSet<String>(setModificacionesRuebas);
		setModificacionesNoCargadas.removeAll(setModificacionesCargadas);
		for (String llave : setModificacionesNoCargadas) {
			RegistroModificacion mod = hashModificacionesRuebas.get(llave);
			modificacionesNoCargadas.add(mod);
		}
		return modificacionesNoCargadas;
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args) {
        try {
            Connection con = PlataformaUtils.getPropertiesConnection("master02","connections.properties");
            ControlMercadoCargaModificacionRuebas demo = new ControlMercadoCargaModificacionRuebas();
            try {
            	demo.DEBUG = true;
            	demo.logica(con,true);
            	con.commit();
//                con.rollback();
            } catch (Throwable er) {
                er.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }    
    }
}