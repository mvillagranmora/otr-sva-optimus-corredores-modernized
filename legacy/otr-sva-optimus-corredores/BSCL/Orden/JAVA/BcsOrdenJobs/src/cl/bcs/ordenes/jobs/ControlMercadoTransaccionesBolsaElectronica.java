package cl.bcs.ordenes.jobs;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.ordenes.CrearTransaccionesBolsaElectronica;
import cl.bcs.ordenes.CrearTransaccionesBolsaElectronica.CrearTransaccionesBolsaElectronica_Entrada;
import cl.bcs.ordenes.CrearTransaccionesBolsaElectronica.CrearTransaccionesBolsaElectronica_Salida;
import cl.bcs.ordenes.CrearTransaccionesBolsaElectronica.Resultado_Salida;
import cl.bcs.ordenes.CrearTransaccionesBolsaElectronica.TransaccionIFBEC_Entrada;
import cl.bcs.ordenes.CrearTransaccionesBolsaElectronica.TransaccionRFBEC_Entrada;
import cl.bcs.ordenes.CrearTransaccionesBolsaElectronica.TransaccionRVBEC_Entrada;
import cl.bcs.ordenes.jobs.ControlMercadoMarketDataPalosIF.Usuario;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.ordenes.pojo.TransaccionIFBEC;
import cl.bcs.ordenes.pojo.TransaccionRFBEC;
import cl.bcs.ordenes.pojo.TransaccionRVBEC;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ControlMercadoTransaccionesBolsaElectronica extends BcsJob {
	private final Logger logger = LogManager.getLogger(ControlMercadoTransaccionesBolsaElectronica.class);
	
	private final String SQL_SELECT_TRANSACCIONES_RV = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoTransaccionesBolsaElectronica_SelectTransaccionesRV.sql");
	private final String SQL_SELECT_TRANSACCIONES_RF = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoTransaccionesBolsaElectronica_SelectTransaccionesRF.sql");
	private final String SQL_SELECT_TRANSACCIONES_IF = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoTransaccionesBolsaElectronica_SelectTransaccionesIF.sql");
	
	private final String SQL_OBTENER_MENSAJES_SIN_PROCESAR = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/ControlMercadoTransaccionesBolsaElectronica_ObtenerMensajesSinProcesar.sql");
	private final String SQL_ACTUALIZA_CONTROL_MERCADO = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_ActualizaControlMercado.sql");
	private final String SQL_SELECT_USUARIO_SISTEMA = PlataformaUtils.loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_SelectUsuarioSistema.sql");
	
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	
	public static class Usuario {
		public Long IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
    public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
    	logger.info("INICIO JOB");
        String DBdriver,DBuser,DBpass,DBurl,DBprop;
        SQL _sql=new SQL();
        
        Long idEmpresa = 0L;
        
        if (debug) {
        	idEmpresa = 1L;
        } else{
        	 idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
        }
        
        
        HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", idEmpresa);
		List<Usuario> usuarioSistema = _sql.query(con, SQL_SELECT_USUARIO_SISTEMA, params, Usuario.class);        
		Long idUsuario = usuarioSistema.get(0).IdUsuario;
        
        //Obtiene propiedades a la BD RUEBAS
        logger.debug("Buscando archivo de configuracion en " + new File(".").getCanonicalPath());
        try{
        	Properties prop = new Properties();
            InputStream input = new FileInputStream("ruebas.properties");
            prop.load(input);
            DBdriver=prop.getProperty("DBdriver");
            DBuser=prop.getProperty("DBuser");
            DBpass=prop.getProperty("DBpass");
            DBurl=prop.getProperty("DBurl");
            DBprop=prop.getProperty("DBprop");
            input.close();
            logger.debug("Configuracion cargada");
        }catch(Exception e){
        	logger.error(e);
        	throw new InterruptedException("ERROR NO SE PUEDE LEER ruebas.properties");
        }  	
    	
        //Obtiene DS conexion a la BD RUEBAS
    	BasicDataSource dsTarget = new BasicDataSource();    	 
    	try {
            dsTarget.setDriverClassName(DBdriver);
            dsTarget.setUsername(DBuser);
            dsTarget.setPassword(DBpass);
            dsTarget.setUrl(DBurl);           
            dsTarget.setConnectionProperties(DBprop);
            dsTarget.setDefaultAutoCommit(true);
            logger.info("Conectado a RUEBAS");
    	}catch(Exception e){
        	logger.error(e);
        	throw new InterruptedException("ERROR NO SE PUEDE OBTENER CONEXION RUEBAS DB");
        }  	
    	
    	
    	//OBTENER MENSAJES SIN PROCESAR
    	HashMap<String,Object> params1 = new HashMap<String,Object>();
    	List<String> tiposMensajeBolsaElectronica = new ArrayList<String>();
    	tiposMensajeBolsaElectronica.add(_c.TIPO_MENSAJE_BEC_PALO_IF);
    	tiposMensajeBolsaElectronica.add(_c.TIPO_MENSAJE_BEC_PALO_RF);
    	tiposMensajeBolsaElectronica.add(_c.TIPO_MENSAJE_BEC_PALO_RV);
    	params1.put("tiposMensajeBolsaElectronica", tiposMensajeBolsaElectronica);
    	
    	List<ControlMercado> mensajesSinProcesar = _sql.query(con, SQL_OBTENER_MENSAJES_SIN_PROCESAR, params1, ControlMercado.class); 
    	HashMap<ControlMercado,Long> hashIdsMensajes = new HashMap<ControlMercado,Long>();
    	long i = 0;
    	for (ControlMercado cm : mensajesSinProcesar) {
    		hashIdsMensajes.put(cm,new Long(i++));
    	}
    	
    	//LLAVE SIEMPRE ES HORA+FOLIO
    	List<String> llavesIF = new ArrayList<String>();
    	List<String> llavesRF = new ArrayList<String>();
    	List<String> llavesRV = new ArrayList<String>();
    	
    	for (ControlMercado cm : mensajesSinProcesar) {
    		
    		if (cm.TipoMensaje.equalsIgnoreCase(_c.TIPO_MENSAJE_BEC_PALO_IF)) {
    			llavesIF.add(cm.IdMensaje1 + cm.IdMensaje2);
    		}
    		else if (cm.TipoMensaje.equalsIgnoreCase(_c.TIPO_MENSAJE_BEC_PALO_RF)) {
    			llavesRF.add(cm.IdMensaje1 + cm.IdMensaje2);
    		}
    		else if (cm.TipoMensaje.equalsIgnoreCase(_c.TIPO_MENSAJE_BEC_PALO_RV)) {
    			llavesRV.add(cm.IdMensaje1 + cm.IdMensaje2);
    		}
    		else if (cm.TipoMensaje.equalsIgnoreCase(_c.TIPO_MENSAJE_BEC_PALO_RV)) {
    			llavesRV.add(cm.IdMensaje1 + cm.IdMensaje2);
    		}
    		else {
    			logger.debug("Registro no se puede procesar : " + cm.toString());
    		}
    	}
    	
    	//MAPA DE MENSAJES
    	HashMap<ControlMercado,Object> hashMensajes = new HashMap<ControlMercado,Object>();
    	
    	//BUSCAR INFO DE PALOS RV
    	HashMap<String,Object> paramsRV = new HashMap<>();
    	paramsRV.put("llaves", llavesRV);
    	List<TransaccionRVBEC> _transaccionesRVBEC = _sql.query(dsTarget.getConnection(), SQL_SELECT_TRANSACCIONES_RV, paramsRV, TransaccionRVBEC.class);
    	
    	//BUSCAR INFO DE PALOS RF
    	HashMap<String,Object> paramsRF = new HashMap<>();
    	paramsRF.put("llaves", llavesRF);
    	List<TransaccionRFBEC> _transaccionesRFBEC = _sql.query(dsTarget.getConnection(), SQL_SELECT_TRANSACCIONES_RF, paramsRF, TransaccionRFBEC.class);
    	
    	//BUSCAR INFO DE PALOS IF
    	HashMap<String,Object> paramsIF = new HashMap<>();
    	paramsIF.put("llaves", llavesIF);
    	List<TransaccionIFBEC> _transaccionesIFBEC = _sql.query(dsTarget.getConnection(), SQL_SELECT_TRANSACCIONES_IF, paramsIF, TransaccionIFBEC.class);
    	
    	//ARMAR RELACION MENSAJE-PALO
    	for (ControlMercado cm : mensajesSinProcesar) {
    		for (TransaccionRVBEC tx : _transaccionesRVBEC) {
    			if (cm.TipoMensaje.equalsIgnoreCase(_c.TIPO_MENSAJE_BEC_PALO_RV) && cm.IdMensaje1.equalsIgnoreCase(tx.HORA) && cm.IdMensaje2.equalsIgnoreCase(tx.FOLIO.toString())) {
    				hashMensajes.put(cm, tx);
    				break;
    			}
    		}
    		
    		for (TransaccionRFBEC tx : _transaccionesRFBEC) {
    			if (cm.TipoMensaje.equalsIgnoreCase(_c.TIPO_MENSAJE_BEC_PALO_RF) && cm.IdMensaje1.equalsIgnoreCase(tx.HORA) && cm.IdMensaje2.equalsIgnoreCase(tx.FOLIO.toString())) {
    				hashMensajes.put(cm, tx);
    				break;
    			}
    		}
    		
    		for (TransaccionIFBEC tx : _transaccionesIFBEC) {
    			if (cm.TipoMensaje.equalsIgnoreCase(_c.TIPO_MENSAJE_BEC_PALO_IF) && cm.IdMensaje1.equalsIgnoreCase(tx.HORA) && cm.IdMensaje2.equalsIgnoreCase(tx.FOLIO.toString())) {
    				hashMensajes.put(cm, tx);
    				break;
    			}
    		}
    		if (!hashMensajes.containsKey(cm)) {
    			//nunca deberia entrar aqui
    			logger.debug("error al asignar transaccion a registro de control mercado");
    		}
		}
	
    	//GENERAR ENTRADA
    	CrearTransaccionesBolsaElectronica_Entrada entradaCrearTransacciones = new CrearTransaccionesBolsaElectronica_Entrada();
    	entradaCrearTransacciones.p_IdEmpresa = idEmpresa;
    	entradaCrearTransacciones.p_IdUsuario = idUsuario;
    	
    	for (ControlMercado cm : mensajesSinProcesar) {
    		
    		if (cm.TipoMensaje.equalsIgnoreCase(_c.TIPO_MENSAJE_BEC_PALO_IF)) {
    			TransaccionIFBEC tx = (TransaccionIFBEC) hashMensajes.get(cm);
    			TransaccionIFBEC_Entrada tx2 = new TransaccionIFBEC_Entrada();
    			Utils.copyPojo(tx, tx2);
    			tx2.IdInterno = hashIdsMensajes.get(cm);
    			entradaCrearTransacciones.transaccionesIF.add(tx2);
    		}
    		
    		if (cm.TipoMensaje.equalsIgnoreCase(_c.TIPO_MENSAJE_BEC_PALO_RF)) {
    			TransaccionRFBEC tx = (TransaccionRFBEC) hashMensajes.get(cm);
    			TransaccionRFBEC_Entrada tx2 = new TransaccionRFBEC_Entrada();
    			Utils.copyPojo(tx, tx2);
    			tx2.IdInterno = hashIdsMensajes.get(cm);
    			entradaCrearTransacciones.transaccionesRF.add(tx2);
    		}
    		
    		if (cm.TipoMensaje.equalsIgnoreCase(_c.TIPO_MENSAJE_BEC_PALO_RV)) {
    			TransaccionRVBEC tx = (TransaccionRVBEC) hashMensajes.get(cm);
    			TransaccionRVBEC_Entrada tx2 = new TransaccionRVBEC_Entrada();
    			Utils.copyPojo(tx, tx2);
    			tx2.IdInterno = hashIdsMensajes.get(cm);
    			entradaCrearTransacciones.transaccionesRV.add(tx2);
    		}
    		
    	}
    	
    	CrearTransaccionesBolsaElectronica serviceCrearTransaccionesBolsaElectronica = new CrearTransaccionesBolsaElectronica();
		CrearTransaccionesBolsaElectronica_Salida salida = serviceCrearTransaccionesBolsaElectronica.logica(con, entradaCrearTransacciones);
		
		List<ControlMercado> updateControlMercado = new ArrayList<>();
		for (ControlMercado cm : mensajesSinProcesar) {
			
			cm.Log = "";
			cm.Procesado = "N";
			
			for (Resultado_Salida resp : salida.P_OUT_RESULTADO) {
	    		if (hashIdsMensajes.get(cm).equals(resp.IdInterno)) {
	    			cm.Procesado = "S";
	    			cm.Log += "TRANSACCION CREADA:" + resp.IdTransaccion+"\n";
	    		}
	    	}
			if (!cm.Procesado.equalsIgnoreCase("S")) {
				cm.Procesado = "E";
			}
			
			for (MensajeSalida msj : salida.P_OUT_MENSAJES) {
				if (hashIdsMensajes.get(cm).equals(msj.id)) {
	    			cm.Log+=msj.descripcion;
	    		}
			}
			
			if (cm.Procesado.equalsIgnoreCase("S") || cm.Procesado.equalsIgnoreCase("E")) {
				updateControlMercado.add(cm);
			}
		}
    	
    	//ACTUALIZAR CONTROL MERCADO
		if (updateControlMercado.size() > 0) {
			_sql.batch(con, SQL_ACTUALIZA_CONTROL_MERCADO, updateControlMercado, ControlMercado.class);
			con.commit();
		}
		
    	return true;
    }

    @Override
    public void onError(Throwable t) {
    	logger.error(t.getMessage());
        logger.error("****** ERROR TAREA  *******");
        t.printStackTrace();
    }

	@Override
	public void pause() {
	}
	
	public static void main(String[] args) {
        //System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getMASTER01Connection();
            // ---------------------------------
            try {
            	ControlMercadoTransaccionesBolsaElectronica demo=new ControlMercadoTransaccionesBolsaElectronica();
            	demo.logica(con,true);
                con.rollback();
            } catch (Throwable er) {
                er.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");       
    }
        

    	
	
}