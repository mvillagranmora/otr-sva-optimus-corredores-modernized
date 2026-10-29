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

import com.google.gson.JsonObject;

import cl.bcs.ordenes.CrearTransacciones;
import cl.bcs.ordenes.CrearTransacciones.CrearTransacciones_Entrada;
import cl.bcs.ordenes.CrearTransacciones.CrearTransacciones_Salida;
import cl.bcs.ordenes.CrearTransaccionesBolsaElectronica;
import cl.bcs.ordenes.CrearTransaccionesBolsaElectronica.CrearTransaccionesBolsaElectronica_Entrada;
import cl.bcs.ordenes.CrearTransaccionesBolsaElectronica.CrearTransaccionesBolsaElectronica_Salida;
import cl.bcs.ordenes.pojo.ConstantesOrdenes;
import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.ordenes.pojo.TransaccionIFBEC;
import cl.bcs.ordenes.pojo.TransaccionRFBEC;
import cl.bcs.ordenes.pojo.TransaccionRVBEC;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargarTransaccionesBolsaElectronica extends BcsJob implements JavaCall {
	private final Logger logger = LogManager.getLogger(CargarTransaccionesBolsaElectronica.class);
	private final ConstantesOrdenes _c = new ConstantesOrdenes();
	private final String SQL_SELECT_TRANSACCIONES_RV = "SELECT MERCADO, NEMO, HORA, FOLIO, CANTIDAD, CANTIDAD_AJU_VC, CAN_CPC, CAN_CPV, COD_CORR_COM, COD_CORR_VEN, COD_OPE_COM, COD_OPE_VEN, OPE_INT_COM, OPE_INT_VEN, COND_COD_001, COND_COD_002, COND_COD_003, COND_COD_004, COND_COD_005, COND_COD_006, COND_COD_007, COND_COD_008, COND_COD_009, COND_COD_010, COND_COD_011, COND_COD_012, COND_COD_013, COND_COD_014, COND_COD_015, COND_DIS, EMISOR_NO_INS, FECHA, FEC_LIQ, FEC_DIG, IND_FIJ_CIE, MONTO, PRECIO, PRECIO_AJU_VC, REDONDEL, RUEDA, IND_DCV, FOLIO_SIMUL, TASA, CLIENTE_COM, CLIENTE_VEN, FCREOF_COM, FCREOF_VEN, CAN_COM, PRECIO_COM, BOLSA_COM, CAN_VEN, PRECIO_VEN, BOLSA_VEN FROM TBRVTRBE WHERE COND_COD_002 != 'S' AND FECHA = :fechaBusqueda AND (COD_CORR_COM = :corredor OR COD_CORR_VEN = :corredor)";
	private final String SQL_SELECT_TRANSACCIONES_SIM = "SELECT MERCADO, NEMO, HORA, FOLIO, CANTIDAD, CANTIDAD_AJU_VC, CAN_CPC, CAN_CPV, COD_CORR_COM, COD_CORR_VEN, COD_OPE_COM, COD_OPE_VEN, OPE_INT_COM, OPE_INT_VEN, COND_COD_001, COND_COD_002, COND_COD_003, COND_COD_004, COND_COD_005, COND_COD_006, COND_COD_007, COND_COD_008, COND_COD_009, COND_COD_010, COND_COD_011, COND_COD_012, COND_COD_013, COND_COD_014, COND_COD_015, COND_DIS, EMISOR_NO_INS, FECHA, FEC_LIQ, FEC_DIG, IND_FIJ_CIE, MONTO, PRECIO, PRECIO_AJU_VC, REDONDEL, RUEDA, IND_DCV, FOLIO_SIMUL, TASA, CLIENTE_COM, CLIENTE_VEN, FCREOF_COM, FCREOF_VEN, CAN_COM, PRECIO_COM, BOLSA_COM, CAN_VEN, PRECIO_VEN, BOLSA_VEN FROM TBRVTRBE WHERE COND_COD_002 = 'S' AND COND_COD_004 != 'T' AND FECHA = :fechaBusqueda AND (COD_CORR_COM = :corredor OR COD_CORR_VEN = :corredor)";
	private final String SQL_SELECT_TRANSACCIONES_RF = "SELECT HORA, FOLIO, CANTIDAD, CAN_CPC, CAN_CPV, COD_CORR_COM, COD_CORR_VEN, COD_OPE_COM, COD_OPE_VEN, OPE_INT_COM, OPE_INT_VEN, COND_COD_001, COND_COD_002, COND_COD_003, COND_COD_004, COND_COD_005, COND_COD_006, COND_COD_007, COND_COD_008, COND_COD_009, COND_COD_010, COND_DIS, EMISOR_NO_INS, FEC_DIG, FECHA, IND_BOLSON, MONTO, NEMO, OPE_ASO_COMP, PLAZO, PLAZO_COR_PRT, PRECIO, REMATE, TIR, TIPO_CALCULO, OFEREF, EMI_PROV, MONEDA, DURATION FROM TBRFTRBE WHERE FECHA = :fechaBusqueda AND (COD_CORR_COM = :corredor OR COD_CORR_VEN = :corredor)";
	private final String SQL_SELECT_TRANSACCIONES_IF = "SELECT FOLIO, HORA, CAN_CPC, CAN_CPV, COD_CORR_COM, COD_CORR_VEN, COD_OPE_COM, COD_OPE_VEN, OPE_INT_COM, OPE_INT_VEN, COND_COD_001, COND_COD_002, COND_COD_003, COND_COD_004, COND_COD_005, COND_COD_006, COND_COD_007, COND_COD_008, COND_COD_009, COND_COD_010, COND_DIS, EMISOR, EMISOR_NO_INS, FAMILIA, FECHA, FEC_DIG, FEC_EMI, FEC_VCTO, LAMINA, LUG_EMI, MADUREZ, MONTO, NEMO, OPE_ASO_COMP, PLAZO, REMATE, TASA, TIPO_PLAZ, TIPO_REAJ, VAL_RESC, OFEREF, CUPVAR FROM TBIFTRBE WHERE FECHA = :fechaBusqueda AND (COD_CORR_COM = :corredor OR COD_CORR_VEN = :corredor)";

	private final String SQL_INSERT_CONTROL_MERCADO = loadResourceToString("cl/bcs/ordenes/jobs/sql/CargarTransaccionesBolsaElectronica_InsertControlMercado.sql");

	private final String SQL_SELECT_CONTROL_MERCADO = loadResourceToString("cl/bcs/ordenes/jobs/sql/CargarTransaccionesBolsaElectronica_SelectControlMercado.sql");
	
	
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
        
        ConsultarEmpresa asdf = new ConsultarEmpresa();
        InformacionEmpresa infoEmpresa = asdf.logica(con,idEmpresa);
        String codigo = infoEmpresa.Codigo;
        
        String fechaBusqueda = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        logger.debug("Cargando transacciones del corredor " + codigo + " para el dia " + fechaBusqueda);
        
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
            logger.debug("Conectado a RUEBAS");
    	}catch(Exception e){
        	logger.error(e);
        	throw new InterruptedException("ERROR NO SE PUEDE OBTENER CONEXION RUEBAS DB");
        }  	
    	
    	HashMap<String,Object> params1 = new HashMap<>();
    	params1.put("fechaBusqueda",fechaBusqueda);
    	params1.put("corredor", codigo);
        List<TransaccionIFBEC> txsIF = _sql.query(dsTarget.getConnection(), SQL_SELECT_TRANSACCIONES_IF, params1,TransaccionIFBEC.class);   	
    	List<TransaccionRFBEC> txsRF = _sql.query(dsTarget.getConnection(), SQL_SELECT_TRANSACCIONES_RF, params1,TransaccionRFBEC.class);
    	List<TransaccionRVBEC> txsRV = _sql.query(dsTarget.getConnection(), SQL_SELECT_TRANSACCIONES_RV, params1,TransaccionRVBEC.class);
    	List<TransaccionRVBEC> txsSIM = _sql.query(dsTarget.getConnection(), SQL_SELECT_TRANSACCIONES_SIM, params1,TransaccionRVBEC.class);
    	
    	//armar llaves
    	List<String> llaves = new ArrayList<>();
    	for (TransaccionIFBEC tx : txsIF) {
    		llaves.add(_c.TIPO_MENSAJE_BEC_PALO_IF+tx.HORA+tx.FOLIO);
    	}
    	for (TransaccionRFBEC tx : txsRF) {
    		llaves.add(_c.TIPO_MENSAJE_BEC_PALO_RF+tx.HORA+tx.FOLIO);
    	}
    	for (TransaccionRVBEC tx : txsRV) {
    		llaves.add(_c.TIPO_MENSAJE_BEC_PALO_RV+tx.HORA+tx.FOLIO);
    	}
    	for (TransaccionRVBEC tx : txsSIM) {
    		llaves.add(_c.TIPO_MENSAJE_BEC_PALO_SIM+tx.HORA+tx.FOLIO);
    	}
    	
    	HashMap<String,Object> params2 = new HashMap<>();
    	params2.put("llaves", llaves);
    	List<ControlMercado> _registrosRepetidos = _sql.query(con, SQL_SELECT_CONTROL_MERCADO, params2, ControlMercado.class);
    	List<String> registrosRepetidos = new ArrayList<>();
    	for (ControlMercado cm : _registrosRepetidos) {
    		registrosRepetidos.add(cm.TipoMensaje+cm.IdMensaje1+cm.IdMensaje2);
    	}
    	
    	//INSERTAR EN CONTROL MERCADO
    	List<ControlMercado> registrosNuevos = new ArrayList<>();
    	for (TransaccionIFBEC tx : txsIF) {
    		
    		if (registrosRepetidos.contains(_c.TIPO_MENSAJE_BEC_PALO_IF+tx.HORA+tx.FOLIO)) {
    			continue;
    		}
    		ControlMercado cm = new ControlMercado();
    		cm.TipoMensaje = _c.TIPO_MENSAJE_BEC_PALO_IF;
    		cm.IdMensaje1 = tx.HORA;
    		cm.IdMensaje2 = tx.FOLIO.toString();
    		registrosNuevos.add(cm);
    		
    	}
    	for (TransaccionRFBEC tx : txsRF) {
    		if (registrosRepetidos.contains(_c.TIPO_MENSAJE_BEC_PALO_RF+tx.HORA+tx.FOLIO)) {
    			continue;
    		}
    		ControlMercado cm = new ControlMercado();
    		cm.TipoMensaje = _c.TIPO_MENSAJE_BEC_PALO_RF;
    		cm.IdMensaje1 = tx.HORA;
    		cm.IdMensaje2 = tx.FOLIO.toString();
    		registrosNuevos.add(cm);
    	}
    	for (TransaccionRVBEC tx : txsRV) {
    		if (registrosRepetidos.contains(_c.TIPO_MENSAJE_BEC_PALO_RV+tx.HORA+tx.FOLIO)) {
    			continue;
    		}
    		ControlMercado cm = new ControlMercado();
    		cm.TipoMensaje = _c.TIPO_MENSAJE_BEC_PALO_RV;
    		cm.IdMensaje1 = tx.HORA;
    		cm.IdMensaje2 = tx.FOLIO.toString();
    		registrosNuevos.add(cm);
    	}
    	for (TransaccionRVBEC tx : txsSIM) {
    		if (registrosRepetidos.contains(_c.TIPO_MENSAJE_BEC_PALO_SIM+tx.HORA+tx.FOLIO)) {
    			continue;
    		}
    		ControlMercado cm = new ControlMercado();
    		cm.TipoMensaje = _c.TIPO_MENSAJE_BEC_PALO_SIM;
    		cm.IdMensaje1 = tx.HORA;
    		cm.IdMensaje2 = tx.FOLIO.toString();
    		registrosNuevos.add(cm);
    	}
    	
    	if (registrosNuevos.size() > 0) {
    		logger.debug("Insertando " + registrosNuevos.size() + " registros.");
    		for (ControlMercado cm : registrosNuevos) {
    			logger.debug(cm.toString());
    		}
    		_sql.batch(con, SQL_INSERT_CONTROL_MERCADO, registrosNuevos, ControlMercado.class);
        	con.commit();
    	}
    	else {
    		logger.debug("No se encontraron nuevos registros.");
    	}
    	
    	logger.info("FIN");
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
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}	
	
    public static void main(String[] args) {
        //System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getMASTER01Connection();
            // ---------------------------------
            try {
            	CargarTransaccionesBolsaElectronica demo=new CargarTransaccionesBolsaElectronica();
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