package cl.bcs.socionegocio.por;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.OpenEJBEncryption;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.por.pojo.AnotacionClientePortafolio;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Crypto;
import cl.bcs.utils.CryptoUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarAnotacionesClientePortafolio implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ConsultarAnotacionesClientePortafolio.class);
	
	private final String SQL_SELECT_ANOTACIONES_SEBRA = loadResourceToString("cl/bcs/socionegocio/por/sql/ConsultarAnotacionesClientePortafolio_SelectAnotacionesSebra.sql");
	private final String SQL_SELECT_ANOTACIONES_OPTIMUS = loadResourceToString("cl/bcs/socionegocio/por/sql/ConsultarAnotacionesClientePortafolio_SelectAnotacionesOptimus.sql");
	private final String SQL_SELECT_ATRIBUTO_ANOTACIONES = loadResourceToString("cl/bcs/socionegocio/por/sql/ConsultarAnotacionesClientePortafolio_SelectAtributosAnotacionesOptimus.sql");
	private final String SQL_SELECT_DATA_CLIENTE_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/por/sql/ConsultarAnotacionesClientePortafolio_SelectDataClientePortafolio.sql");
	
	public static class EntradaConsultarAnotacionesClientePortafolio extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<ClienteCodigoPortafolio> p_ClienteCodigoPortafolio;
		
		public EntradaConsultarAnotacionesClientePortafolio() {
			this.p_IdEmpresa = 0L;
			this.p_IdUsuario = 0L;
			this.p_IdModulo = "SOC";
			this.p_UsaReglasNegocio = "N";
			this.p_ClienteCodigoPortafolio = new ArrayList<ClienteCodigoPortafolio>();
			
		}
	}
	
	public static class ClienteCodigoPortafolio extends BcsPojo {
		public Long IdCliente;
		public String DocumentoIdentidad;
		public Long IdPortafolio;
		public String CodigoPortafolio;
		
		public ClienteCodigoPortafolio () {
			this.IdCliente = 0L;
			this.DocumentoIdentidad = "";
			this.IdPortafolio = 0L;
			this.CodigoPortafolio = "";
		}
		
		public ClienteCodigoPortafolio (Long idPortafolio) {
			this.IdPortafolio = idPortafolio;
			this.IdCliente = 0L;
			this.DocumentoIdentidad = "";
			this.CodigoPortafolio = "";
		}
	}
	
	public static class SalidaConsultarAnotacionesClientePortafolio extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<AnotacionClientePortafolio> P_OUT_RESULTADO;
		
		public SalidaConsultarAnotacionesClientePortafolio() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<AnotacionClientePortafolio>();
		}
	}
	
	public static class AtributoAnotacion extends BcsPojo{
		public String IdAtributoAnotacion;
		public String TipoAtributoAnotacion;
		public String NombreAtributoAnotacion;
		
		public AtributoAnotacion () {
			this.IdAtributoAnotacion = "";
			this.TipoAtributoAnotacion = "";
			this.NombreAtributoAnotacion = "";
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarAnotacionesClientePortafolio jin=gson.fromJson(in.toString() ,EntradaConsultarAnotacionesClientePortafolio.class);
		SalidaConsultarAnotacionesClientePortafolio out=logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarAnotacionesClientePortafolio logica(Connection con, EntradaConsultarAnotacionesClientePortafolio entrada) throws Exception {
		logger.info("INICIO::ConsultarAnotacionesClientePortafolio");
		long inicio = System.currentTimeMillis();
		
		SalidaConsultarAnotacionesClientePortafolio salida = new SalidaConsultarAnotacionesClientePortafolio();
		
		boolean debug = false;
		
		if(entrada.p_ClienteCodigoPortafolio.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_ENTRADA_SIN_CLIENTE_PORTAFOLIO", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
        
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>(1);
        params.put("IdPortafolio", entrada.p_ClienteCodigoPortafolio.get(0).IdPortafolio);
        
        ClienteCodigoPortafolio dataClienteCodigoPortafolio = sql.query(con, SQL_SELECT_DATA_CLIENTE_PORTAFOLIO, params, ClienteCodigoPortafolio.class).stream().findFirst().orElse(new ClienteCodigoPortafolio());
		
        if(dataClienteCodigoPortafolio.IdCliente == 0L || dataClienteCodigoPortafolio.IdPortafolio == 0L) {
        	salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_CLIENTE_PORTAFOLIO_INVALIDO", new HashMap<String,String>(0), con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
        }
        
        List<AnotacionClientePortafolio> anotacionesSebra = consultarAnotacionesSebra(con, dataClienteCodigoPortafolio, entrada.p_IdEmpresa, debug);
        salida.P_OUT_RESULTADO.addAll(anotacionesSebra);
        
        List<AnotacionClientePortafolio> anotacionesOptimus = consultarAnotacionesOptimus(con, dataClienteCodigoPortafolio);
        salida.P_OUT_RESULTADO.addAll(anotacionesOptimus);
        
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarAnotacionesClientePortafolio", fin - inicio);
		return salida;
	}

	private List<AnotacionClientePortafolio> consultarAnotacionesSebra(Connection con, ClienteCodigoPortafolio clienteCodigoPortafolio, Long idEmpresa, boolean debug) {
		logger.info("INICIO::ConsultarAnotacionesClientePortafolio.consultarAnotacionesSebra");
		long inicio = System.currentTimeMillis();
		
		List<AnotacionClientePortafolio> anotacionesSebra = new ArrayList<>();

		String DBdriverSebra= "";
		String DBuserSebra 	= "";
		String DBpassSebra 	= "";
		String DBurlSebra 	= "";
		String DBpropSebra	= "";
		String DBkeySebra	= "";
		
		Properties prop = new Properties();
		InputStream input;
		SQL sql = new SQL();
		
        try {
        	if( debug ) {
                input = new FileInputStream("C:\\GIT\\OPTIMUS\\workspace\\optimus-corredores-dev-workspace\\OptimusDevJava\\optimus.properties");
            } else {
            	String rutaArchivoProperties = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.RutaArchivosConfiguracion");        	
            	input = new FileInputStream(rutaArchivoProperties+"optimus.properties");
            	logger.info("Configuracion optimus.properties cargada");
            }
        	
        	prop.load(input);
        	DBdriverSebra=prop.getProperty("DBdriverSebra");
            DBuserSebra=prop.getProperty("DBuserSebra");
            DBpassSebra=prop.getProperty("DBpassSebra");
            DBurlSebra=prop.getProperty("DBurlSebra");
            DBpropSebra=prop.getProperty("DBpropSebra");
            DBkeySebra = prop.getProperty("DBkeySebra");
            input.close();
            logger.info("PROPIEDADES CONEXION SEBRA :::::::::::::");
            logger.info("DBdriverSebra :: "+ DBdriverSebra);
            logger.info("DBuserSebra :: "+DBuserSebra + "( "+OpenEJBEncryption.decrypt(DBuserSebra, DBkeySebra)+" )");
            logger.info("DBpassSebra :: "+DBpassSebra+ "( "+OpenEJBEncryption.decrypt(DBpassSebra, DBkeySebra)+" )");
            logger.info("DBurlSebra :: "+DBurlSebra);
            logger.info("DBpropSebra :: "+DBpropSebra);
            logger.info("DBkeySebra :: "+DBkeySebra);
        } catch(Exception ex) {
        	logger.info("ERROR NO SE PUEDE LEER el archivo optimus.properties", ex);
            logger.info(ex.toString());
        }
        
        BasicDataSource dsTarget = new BasicDataSource();            
        try {                                                
	        dsTarget.setDriverClassName(DBdriverSebra);
	        dsTarget.setUsername(OpenEJBEncryption.decrypt(DBuserSebra, DBkeySebra));
	        dsTarget.setPassword(OpenEJBEncryption.decrypt(DBpassSebra, DBkeySebra));
	        dsTarget.setUrl(DBurlSebra);          
	        dsTarget.setConnectionProperties(DBpropSebra);
	        dsTarget.setDefaultAutoCommit(true);
	        logger.info("Conectado a Sebra");
        } catch( Exception e ){
        	logger.info("ERROR NO SE PUEDE OBTENER CONEXION SEBRA DB", e);
        	logger.info(e.toString());                                            
        }
        
        try( Connection conRU = dsTarget.getConnection() ) {
        	
            logger.info("Consulta de data generada en sebra");
            HashMap<String,Object> paramsSebra = new HashMap<String,Object>(3);
            paramsSebra.put("DocumentoIdentidad", clienteCodigoPortafolio.DocumentoIdentidad);
            paramsSebra.put("CodigoPortafolio", clienteCodigoPortafolio.CodigoPortafolio);
            paramsSebra.put("IdPortafolio", clienteCodigoPortafolio.IdPortafolio);
            
            List<AnotacionClientePortafolio> anotaciones = sql.query(conRU, SQL_SELECT_ANOTACIONES_SEBRA, paramsSebra, AnotacionClientePortafolio.class);
            
            Map<String, AtributoAnotacion> atributosAnotaciones = sql.query(con,SQL_SELECT_ATRIBUTO_ANOTACIONES, new HashMap<>(0),AtributoAnotacion.class).stream().collect(Collectors.toMap(atr -> atr.TipoAtributoAnotacion +"_"+atr.IdAtributoAnotacion, atr -> atr));
            anotacionesSebra = repasoAnotacionesSebra(anotaciones, atributosAnotaciones);
            logger.info("Se agregaron las anotaciones de Sebra.");
		} catch( Exception er ) {
			logger.info("ERROR NO SE PUEDE CONECTAR BD EN SEBRA", er);
			logger.info(er.toString());                                                    
		}
        
        long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarAnotacionesClientePortafolio.consultarAnotacionesSebra", fin - inicio);
		return anotacionesSebra;
	}

	private List<AnotacionClientePortafolio> repasoAnotacionesSebra(List<AnotacionClientePortafolio> anotacionesSebra, Map<String, AtributoAnotacion> atributosAnotaciones) {
		logger.info("INICIO::ConsultarAnotacionesClientePortafolio.repasoAnotacionesSebra");
		long inicio = System.currentTimeMillis();
		
		for(AnotacionClientePortafolio anota : anotacionesSebra) {
			AtributoAnotacion atributoMotivo = atributosAnotaciones.get("MOTIVO_" + anota.IdMotivo);
			AtributoAnotacion atributoRelevancia = atributosAnotaciones.get("RELEVANCIA_" + anota.IdRelevancia);
			AtributoAnotacion atributoAmbito = atributosAnotaciones.get("AMBITO_" + anota.IdAmbito);
			
			anota.NombreMotivo = atributoMotivo.NombreAtributoAnotacion;
			anota.NombreRelevancia = atributoRelevancia.NombreAtributoAnotacion;
			anota.NombreAmbito = atributoAmbito.NombreAtributoAnotacion;
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarAnotacionesClientePortafolio.repasoAnotacionesSebra", fin - inicio);
		return anotacionesSebra;
	}

	private List<AnotacionClientePortafolio> consultarAnotacionesOptimus(Connection con, ClienteCodigoPortafolio clienteCodigoPortafolio) throws Exception {
		logger.info("INICIO::ConsultarAnotacionesClientePortafolio.consultarAnotacionesOptimus");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
        HashMap<String,Object> paramsOptimus = new HashMap<String,Object>(1);
        paramsOptimus.put("IdPortafolio", clienteCodigoPortafolio.IdPortafolio);
        
		List<AnotacionClientePortafolio> anotacionesOptimus = sql.query(con, SQL_SELECT_ANOTACIONES_OPTIMUS, paramsOptimus, AnotacionClientePortafolio.class);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarAnotacionesClientePortafolio.consultarAnotacionesOptimus", fin - inicio);
		return anotacionesOptimus;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02")) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
																				//4	
//		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_ClienteCodigoPortafolio\":[{ \"DocumentoIdentidad\": \"10111045-1\",\"CodigoPortafolio\": \"0\"}]}";
		String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_ClienteCodigoPortafolio\":[{ \"IdPortafolio\": 6228}]}";
		//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_ClienteCodigoPortafolio\":[]}";
		
		EntradaConsultarAnotacionesClientePortafolio Objin = gson.fromJson(json, EntradaConsultarAnotacionesClientePortafolio.class);																
		ConsultarAnotacionesClientePortafolio  demo=new ConsultarAnotacionesClientePortafolio();				
		try {
			SalidaConsultarAnotacionesClientePortafolio ObjOut=demo.logica(con, Objin);
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
		}
		System.out.println("== END ==");
	}
}
