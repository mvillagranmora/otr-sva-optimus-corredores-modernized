package cl.bcs.plataforma.pla;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/***************************************
 * @version 	1.0
 * @author 		CSI
 *
**************************************/

public class ExtraerPreciosBECRuebasDB implements JavaCall {
	static final Logger logger = LogManager.getLogger(ExtraerPreciosBECRuebasDB.class);
	
	private final String SQL_PRECIOS_BEC = loadResourceToString("cl/bcs/plataforma/pla/sql/ExtraerPreciosBECRuebasDB_SelectPreciosBECRuebas.sql");
	
	private final String SQL_INSTRUMENTOS = loadResourceToString("cl/bcs/plataforma/pla/sql/ExtraerPreciosBECRuebasDB_SelectInstrumentos.sql");
	
	private final String SQL_PRECIO_INSERT = loadResourceToString("cl/bcs/plataforma/pla/sql/ExtraerPreciosBECRuebasDB_InsertPreciosBEC.sql");
	
	public static class InstrumentosBEC {
		public String		FECHA;
		public String		NEMO;
		public String		MEJOR_HORA;
		public BigDecimal	MEJOR_PRECIO;
		public String		PEOR_HORA;
		public BigDecimal	PEOR_PRECIO;
		//
		
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}		
	}
	
	public static class Instrumento {
		public Long IdInstrumento;
		public String Nemotecnico;
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}		
	
	public static class BolsaInstrumentoPrecio {
		public Long			IdInstrumento;
		public String		IdBolsa;
		public String		Nemotecnico;
		public Date			Fecha;
		public Timestamp	FechaActualizacionArchivo;
		public Timestamp	FechaActualizacionMD;
		public BigDecimal	PrecioMayor;
		public BigDecimal	PrecioMenor;
		public BigDecimal	PrecioApertura;
		public BigDecimal	PrecioCierre;
		public BigDecimal	PrecioMedio;
		public BigDecimal	UnidadesTransadas;
		public BigDecimal	MontoTransado;
		public BigDecimal	Variacion;
		public Time 		HoraPrecioMaximo;
		public Time			HoraPrecioMinimo;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}	
	public static class ExtraerPreciosBECRuebasDB_Salida {
		 public Long P_OUT_ERRORESCONTADOR;
		 public String P_OUT_MENSAJE;
	}
	
	public ExtraerPreciosBECRuebasDB_Salida logica(Connection con, String fechaSistema) throws Exception{
		logger.info("INICIO SERVICIO");
		ExtraerPreciosBECRuebasDB_Salida out = new ExtraerPreciosBECRuebasDB_Salida();
        String DBdriver,DBuser,DBpass,DBurl,DBprop;
        SQL _sql=new SQL();
        List<BolsaInstrumentoPrecio> ListaNuevosPrecios=new ArrayList<>();
        String rutaArchivosConfiguracion = Utils.getConfiguracion(con, Utils.getSchema(), 1L, "PLA.RutaArchivosConfiguracion");
        
        //Obtiene propiedades a la BD RUEBAS
        try{
        	Properties prop = new Properties();
            InputStream input = new FileInputStream(rutaArchivosConfiguracion + "ruebas.properties");
            prop.load(input);
            DBdriver=prop.getProperty("DBdriver");
            DBuser=prop.getProperty("DBuser");
            DBpass=prop.getProperty("DBpass");
            DBurl=prop.getProperty("DBurl");
            DBprop=prop.getProperty("DBprop");
            input.close();
            logger.info("Configuracion cargada");
        }catch(Exception e){
        	logger.info(e);
        	out.P_OUT_ERRORESCONTADOR++;
        	out.P_OUT_MENSAJE = "ERROR NO SE PUEDE LEER ruebas.properties (PRECIOS BEC)";
        	return out;
        	//throw new InterruptedException("ERROR NO SE PUEDE LEER ruebas.properties");
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
    		logger.info(e);
        	out.P_OUT_ERRORESCONTADOR++;
        	out.P_OUT_MENSAJE = "ERROR NO SE PUEDE OBTENER CONEXION RUEBAS DB (PRECIOS BEC)";
        	return out;
        	//throw new InterruptedException("ERROR NO SE PUEDE OBTENER CONEXION RUEBAS DB");
        }  	

        //Obtiene los Instrumentos de Optimus
    	logger.info("Obteniendo Instrumentos Optimus");
    	List<Instrumento> instrumentos = null;
    	 HashMap<String, Instrumento> hashinstrumentos = null;
        try {
        	instrumentos=_sql.query(con, SQL_INSTRUMENTOS,new HashMap<String,Object>(), Instrumento.class);
            hashinstrumentos = (HashMap) Utils.List2Hash(instrumentos, "Nemotecnico");
            logger.info("Instrumentos leidos:" + hashinstrumentos.size());
        }
        catch(Exception e) {
        	logger.info("Error al leer instrumentos optimus");
        	logger.info(e);
        	out.P_OUT_ERRORESCONTADOR++;
        	out.P_OUT_MENSAJE = "ERROR AL LEER INSTRUMENTOS OPTIMUS (PRECIOS BEC)";
        	return out;
        	
        }
        
        
    	//Obtiene los Precios BEC de Ruebas
        logger.info("Obteniendo Precios BEC Ruebas");
    	List<InstrumentosBEC> preciosBECRuebas;
    	HashMap<String,Object> params = new HashMap<>();
		params.put("Bolsa", "BEC");
		params.put("Fecha", fechaSistema);
        try (Connection conRU = dsTarget.getConnection()) {        	
        	preciosBECRuebas=_sql.query(conRU, SQL_PRECIOS_BEC,params, InstrumentosBEC.class);
        }catch(Exception er) {
        	logger.info(er);
        	out.P_OUT_ERRORESCONTADOR++;
        	out.P_OUT_MENSAJE = "ERROR NO SE PUEDE OBTENER PRECIOS BEC";
        	return out;
        	//throw new InterruptedException("ERROR NO SE PUEDE OBTENER PRECIOS BEC");        	
        }
        logger.info(preciosBECRuebas.size() + " leidos.");
        
        //Procesamiento
        logger.info("Procesando Instrumentos");
        for (InstrumentosBEC precioBEC:preciosBECRuebas) {
        	BolsaInstrumentoPrecio nPrecio=new BolsaInstrumentoPrecio(); 
        	String nemo=precioBEC.NEMO.trim().toUpperCase();
        	if (!hashinstrumentos.containsKey(nemo)) {
        		logger.info("Warning: Instrumento no cargado en optimus :"+nemo);
        		continue;
        	}
        	nPrecio.IdInstrumento=hashinstrumentos.get(nemo).IdInstrumento;        	
        	nPrecio.Nemotecnico=nemo;
        	nPrecio.IdBolsa="BEC";
        	nPrecio.Fecha=Fecha.toDate(precioBEC.FECHA.substring(0,10));
        	nPrecio.PrecioMayor=precioBEC.MEJOR_PRECIO;
        	nPrecio.PrecioMenor=precioBEC.PEOR_PRECIO;
        	nPrecio.PrecioApertura=BigDecimal.ZERO;
        	nPrecio.PrecioCierre=BigDecimal.ZERO;
        	nPrecio.PrecioMedio=BigDecimal.ZERO;
        	nPrecio.UnidadesTransadas=BigDecimal.ZERO;
        	nPrecio.MontoTransado=BigDecimal.ZERO;
        	nPrecio.Variacion=BigDecimal.ZERO;
        	nPrecio.HoraPrecioMaximo = getTime(precioBEC.MEJOR_HORA);
        	nPrecio.HoraPrecioMinimo = getTime(precioBEC.PEOR_HORA);
        	ListaNuevosPrecios.add(nPrecio);
        }
        
        ///////////////////////////////////////////
        //Envia Precios a BD
        ///////////////////////////////////////////
        if (ListaNuevosPrecios.size()>0) {
        	logger.info("Precios BEC encontrados n="+ListaNuevosPrecios.size());
        	try (Connection conRU = dsTarget.getConnection()) {        	
        		_sql.batch(con, SQL_PRECIO_INSERT, ListaNuevosPrecios, BolsaInstrumentoPrecio.class);  
            }catch(Exception er) {
            	logger.info(er);
            	out.P_OUT_ERRORESCONTADOR++;
            	out.P_OUT_MENSAJE = "ERROR AL INSERTAR DATOS" + er;
            	return out;
            	//throw new InterruptedException("ERROR NO SE PUEDE OBTENER PRECIOS BEC");        	
            }
        	      	
        }else {
        	logger.info("No hay precios BEC que ingresar");
        }
        out.P_OUT_ERRORESCONTADOR=0L;
    	out.P_OUT_MENSAJE = "";  
        //Proceso Terminado correctamente
      return out;   	    	
	}
    

   
    
    public Time getTime(String time) {
    	logger.info("Inicio - getTime()");
    	LocalTime timeparsed = LocalTime.parse(time,DateTimeFormatter.ofPattern("HHmmss"));
    	Time ruebasTime = Time.valueOf(timeparsed);
    	logger.info("Fin - getTime()");
    	return ruebasTime;
    }

	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}	
	
    public static void main(String[] args) {
        //System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getDESAConnection();
            // ---------------------------------
            try {
            	ExtraerPreciosBECRuebasDB demo=new ExtraerPreciosBECRuebasDB();
            	ExtraerPreciosBECRuebasDB_Salida out = demo.logica(con,"2022-04-21");
            	
            	System.out.println("SALIDA");
            	System.out.println(out.P_OUT_ERRORESCONTADOR);
            	System.out.println(out.P_OUT_MENSAJE);
                con.commit();
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