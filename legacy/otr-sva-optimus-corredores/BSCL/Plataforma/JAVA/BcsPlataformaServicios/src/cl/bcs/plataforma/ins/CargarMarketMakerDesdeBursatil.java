package cl.bcs.plataforma.ins;
/********************************
 *
 * Obtiene desde la Ruabas informacion de Market Market(para presencia bursatil) y los carga en optimus
 * Este proceso debe estar inscrito en la pauta diaria
 ********************************/

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;




public class CargarMarketMakerDesdeBursatil implements JavaCall {	
	
	static final Logger logger = LogManager.getLogger(CargarMarketMakerDesdeBursatil.class.getName());
	                                                                     
	private final String SQL_SELECT_MARKETMARKET_BURSATIL 		= loadResourceToString("cl/bcs/plataforma/ins/sql/CargarMarketMakerDesdeBursatil_SelectMarketMarketBursatil.sql");
	private final String SQL_INSERT_MARKETMARKET_TABLA_PASO 	= loadResourceToString("cl/bcs/plataforma/ins/sql/CargarMarketMakerDesdeBursatil_InsertMarketMarketTablaPaso.sql");
	private final String SQL_DELETE_MARKETMARKET 		        = loadResourceToString("cl/bcs/plataforma/ins/sql/CargarMarketMakerDesdeBursatil_DeleteMarketMarket.sql");
	private final String SQL_INSERT_MARKETMARKET 		        = loadResourceToString("cl/bcs/plataforma/ins/sql/CargarMarketMakerDesdeBursatil_InsertMarketMarket.sql");

	
	public static class Entrada {
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_FechaSistema;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Salida {
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR;
		
		public Salida(){
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	
	public static class TBRELMTK {
		
		public String 		MERCADO;
		public String 		NEMO;
		public BigDecimal	COD_CORR;
		public String 		ESTADO;
		public String 		FEC_INI_MKT;
		public String 		FEC_TERM_MKT;
		
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class MarketMarket {
		
		public Long 		IdInstrumento;
		public String 		IdEstado;
		public Long 		IdCorredora;
		public String 		FechaInicio;
		public String 		FechaFin;
		
		public String toString()
		{
			return Utils.printJson(this, false);
		}
		
	}
	



	//-------------------------------------------
	// CALL
	//-------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {	
		Gson gson = Utils.getGsonBuilder();	
		
		Entrada in=gson.fromJson(jin.toString() ,Entrada.class);
		Salida out=logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	}

	
	//-------------------------------------------
	// LOGICA
	//-------------------------------------------
	public Salida logica(Connection con, Entrada entrada) throws Exception {
		
		logger.info("INICIO :: CargarMarketMakerDesdeBursatil");
		
		logger.info("ENTRADA > " + entrada.toString());
		
		SQL _sql = new SQL();
		Salida out = new Salida();
		String DBdriver, DBuser, DBpass, DBurl, DBprop;
		HashMap<String,Object> params = new HashMap<String,Object>();
		

		// ---------------------------------------------------------------
		// OBTIENE PROPIEDADES DE CONEXION A LA BD RUEBAS
		
        /*****
    	// Para pruebas locales, apuntando a Ruebas QA Sebra(para esto se se debe tener referenciada la libreria jtds en el proyecto)
    	DBdriver	= "net.sourceforge.jtds.jdbc.Driver";
		DBuser		= "consorcio";
		DBpass		= "consorcio";
		DBurl		= "jdbc:jtds:sqlserver://161.238.90.24:1433/RUEBAS";  //QA SEBRA CB
		DBprop		= "SocketTimeout=120;LoginTimeout=30;";
		******/
		
        try{
        	logger.info("Obtiene propiedades de Conexion a Ruebas ");
        	
        	String RutaArchivosConfiguracion = Utils.getConfiguracion(con,  Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaArchivosConfiguracion");
        	Properties prop = new Properties();
            InputStream input = new FileInputStream(RutaArchivosConfiguracion+"ruebas.properties");   // /home/optimus/ruebas.properties
            prop.load(input);
            
            DBdriver	= prop.getProperty("DBdriver");
            DBuser		= prop.getProperty("DBuser");
            DBpass		= prop.getProperty("DBpass");
            DBurl		= prop.getProperty("DBurl");
            DBprop		= prop.getProperty("DBprop");
        	
            input.close();
            
            logger.info("Configuracion a Ruebas cargada OK >  " + DBurl.toString());
            
        }catch(Exception e){
        
        	logger.info("ERROR NO SE PUEDE LEER ruebas.properties");
        	logger.info(e);
        	throw new InterruptedException("ERROR NO SE PUEDE LEER ruebas.properties");
        }  	
        

		

        
		// ------------------------------------------------------       
		// CONEXION A LA BD RUEBAS
        
        //Obtiene DS conexion a la BD RUEBAS
    	BasicDataSource dsTarget = new BasicDataSource();    	 
    	try {
            dsTarget.setDriverClassName(DBdriver);
            dsTarget.setUsername(DBuser);
            dsTarget.setPassword(DBpass);
            dsTarget.setUrl(DBurl);
            dsTarget.setConnectionProperties(DBprop);
            dsTarget.setDefaultAutoCommit(true);
           
            logger.info("Conectado a RUEBAS OK");
    	}catch(Exception e){
    		logger.info("ERROR NO SE PUEDE OBTENER CONEXION RUEBAS DB");
    		logger.info(e);
        	
        	throw new InterruptedException("ERROR NO SE PUEDE OBTENER CONEXION RUEBAS DB");
        	
        }  	
	
    	
		// ------------------------------------------------------       
		// OBTENCION DE DATOS MARKET MARKET DESDE RUEBAS
    	
    	logger.info("Inicia obtencion de datos MarketMarket desde Ruebas");
    	
    	params.clear();
		params.put("Fecha", entrada.p_FechaSistema);
		
    	
    	List<TBRELMTK> marketMarketRuebas;
    	
        try (Connection conRU = dsTarget.getConnection()) 
        {         	
        	marketMarketRuebas = _sql.query(conRU, SQL_SELECT_MARKETMARKET_BURSATIL, params, TBRELMTK.class);
        }
        catch(Exception er) {
        	     	
        	logger.info("ERROR NO SE PUEDEN OBTENER LOS DATOS MARKETMARKET DESDE RUEBAS");
        	logger.info(er);     	
        	throw new InterruptedException("ERROR NO SE PUEDEN OBTENER LOS DATOS MARKETMARKET DESDE RUEBAS");       	
        }
        
        logger.info(" Registros MarketMarket obtenidos desde Ruebas >  " + marketMarketRuebas.size() );
        
        
        
        
		// ------------------------------------------------------       
		// INSERTAR LOS DATOS DE LA RUEBAS EN TABLA GLOBAL		
		
		logger.info("Se insertan en tabla Global los MarketMarket obtenidos desde Ruebas");
		
		_sql.batch(con, SQL_INSERT_MARKETMARKET_TABLA_PASO, marketMarketRuebas, TBRELMTK.class);
		

		
		
		// ------------------------------------------------------       
		// ELIMINA E INSERTAR MARKET MARKET EN OPTIMUS
        
		List<MarketMarket> lstMarketMarket = new ArrayList<MarketMarket>();
		
		
		logger.info("Se borra en optimus los MarketMarket a reemplazar");        
        _sql.batch(con, SQL_DELETE_MARKETMARKET, lstMarketMarket, MarketMarket.class);
		
		logger.info("Se insertan en optimus los nuvos MarketMarket obtenidos desde Ruebas");        
        _sql.batch(con, SQL_INSERT_MARKETMARKET, lstMarketMarket, MarketMarket.class);
        
      
		logger.info("TERMINO :: CargarMarketMakerDesdeBursatil");
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		return out;
	}
	
	
	

	
	//-------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------
	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");
		try {
			
			Connection con = PlataformaUtils.getDESAConnection();
			Entrada entrada = new Entrada();
			
			entrada.p_IdEmpresa 	= 1L;
			entrada.p_IdUsuario 	= 1020L;
			entrada.p_IdModulo 		= "CUS";
			entrada.p_FechaSistema 	= "2020-07-27";
			
			System.out.println(entrada.toString());

			Gson gson = new Gson();
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(gson.toJson(entrada)).getAsJsonObject();
	        CargarMarketMakerDesdeBursatil demo = new CargarMarketMakerDesdeBursatil();			
	        Salida resp=gson.fromJson(demo.call(con, dataJsonIn),Salida.class);
	    	System.out.println(resp);	

	    	con.commit();
	    	//con.rollback();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
