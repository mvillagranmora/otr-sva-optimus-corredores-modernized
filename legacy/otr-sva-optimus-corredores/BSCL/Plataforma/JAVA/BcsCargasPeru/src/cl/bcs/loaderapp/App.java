package cl.bcs.loaderapp;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.dbcp.BasicDataSource;

import cl.bcs.loaderapp.factorys.DbLoaderFactory;
import cl.bcs.loaderapp.factorys.FileLoaderFactory;
import cl.bcs.loaderapp.interfaces.DbLoader;
import cl.bcs.loaderapp.interfaces.FileLoader;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
public class App{
	private static Logger logger = Logger.getLogger("cl.kaiser.hana");
	private Log log = null;
	
	public static void main(String[] args) {
    	logger.setLevel(Level.SEVERE);
    	
    	App app = new App();
    	String csvFile = "";
        String entidad = "";
        String props = "C:\\Users\\crojas_EXT\\Desktop\\Optimus\\SVN_PRE\\BSCL\\Plataforma\\JAVA\\BcsCargasPeru\\src\\connection.properties";
        
//		csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA\\CLIENTE3.csv";
//		entidad = "CLIENTE";
//        csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA\\EMISOR.csv";
//        entidad = "EMISOR";
//        csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA\\PROVEEDOR.csv";
//        entidad = "PROVEEDOR";
//        csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180511\\DATA_MAESTRA\\BANCO.csv";
//        entidad = "BANCO";
//        csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA_MAESTRA\\BOLSA.csv";
//        entidad = "BOLSA";
//        csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA_MAESTRA\\BOVEDA.csv";
//        entidad = "BOVEDA";
//        csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA\\ORIGENFONDOSCLIENTE.csv";
//		entidad = "ORIGENFONDO";
//      csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA\\REQUISITOSCLIENTE.csv";
//		entidad = "REQUISITOSCLIENTE";
//        csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA\\INDICADORESCLIENTE.csv";
//		entidad = "INDICADORESCLIENTE";
//		csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA\\INDICADORESPERSONA.csv";
//		entidad = "INDICADORESPERSONA";
//		csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA\\INDICADORESPERSONA.csv";
//		entidad = "INDICADORESPERSONA";		
//		csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA\\PREFERENCIASPORTAFOLIO.csv";
//		entidad = "PREFERENCIASPORTAFOLIO";	
//		csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA\\CUPONES2.csv";
//		entidad = "CUPONES";	
				
//		csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\cargaGeneralPeru\\documentos.csv";
//		entidad = "DOCUMENTO";
//		csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\cargaGeneralPeru\\anotaciones.csv";
//		entidad = "ANOTACION";
		//csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180703\\DATA\\FATCACLIENTES.csv";
		//entidad = "FATCA";
		csvFile = "C:\\Users\\crojas_EXT\\Desktop\\PruebaCargaDatos\\PruebaPreferenciasPortafolio.csv";
		entidad = "PREFERENCIASPORTAFOLIO";	
//		csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\cargaGeneralPeru\\portafolio\\cuentacustodio.csv";
//		entidad = "CUENTACUSTODIO";        
//		csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\cargaGeneralPeru\\portafolio\\portafolio.csv";
//		entidad = "PORTAFOLIO";
	      
//		csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA\\INSTRUMENTO_MIN.csv";
//		entidad = "INSTRUMENTO";

//	    csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\cargaGeneralPeru\\posiciones\\subyacentes.csv";
//	    entidad = "SUBYACENTE";
		

//	    csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\cargaGeneralPeru\\posiciones\\movimiento_cartera.csv";
//	    entidad = "MOVIMIENTO_CARTERA";

//	    csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\cargaGeneralPeru\\posiciones\\opciones.csv";
//	    entidad = "OPCIONES";
		
//	    csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA\\POSICION2.csv";		
//	    entidad = "POSICION";
//	    csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA\\REPORTES2.csv";
//	    entidad = "REPORTE";        
//		csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\carga masiva peru\\20180625\\DATA\\GARANTIAS2.csv";
//	    entidad = "GARANTIA";		
		
//	    
//	    csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\cargaGeneralPeru\\facturacion\\poliza.csv";
//	    entidad = "POLIZA";
//	    csvFile = "C:\\Users\\chernandez\\On eDrive - Bolsa de Comercio de Santiago\\cargaGeneralPeru\\facturacion\\detallePoliza.csv";
//	    entidad = "DETALLEPOLIZA";
//	    csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\cargaGeneralPeru\\facturacion\\movimientoPoliza.csv";
//	    entidad = "MOVIMIENTOPOLIZA";
//	    csvFile = "C:\\Users\\chernandez\\OneDrive - Bolsa de Comercio de Santiago\\cargaGeneralPeru\\facturacion\\detalleMovimientoPoliza.csv";
//	    entidad = "DETALLEMOVIMIENTOPOLIZA";	    
	    
		if (args.length == 3) {
            csvFile = args[0];
            entidad = args[1];
            props = args[2];
        } else {
        	System.out.println("Error, debe ejecutar el programa de con los siguientes parametros: java -jar cargaGeneralPeru.jar RUTA_ARCHIVO ENTIDAD PROPERTIES");
            // terminate
           // System.exit(1);
        }

        try {
        	//Settings by file
            Properties prop = new Properties();
            InputStream input = new FileInputStream(props);
            prop.load(input);
            String dbURL  = prop.getProperty("URL");
            String dbprop = prop.getProperty("PROP");
            String dbdrv  = prop.getProperty("DRIVER");
            String dbqry  = prop.getProperty("QVAL");
            String user = prop.getProperty("USER");
            String passwd = prop.getProperty("PASSWD");
            
        	BasicDataSource jsJB = new BasicDataSource();
        	jsJB.setDriverClassName(dbdrv);
	        jsJB.setUsername(user);
	        jsJB.setPassword(passwd);
	        jsJB.setUrl(dbURL);
	        jsJB.setValidationQuery(dbqry);
	        jsJB.setConnectionProperties(dbprop);
	        jsJB.setDefaultAutoCommit(false);

            System.out.println("OK");
            EntradaCargaGeneral entrada = new EntradaCargaGeneral();
            entrada.p_IdEmpresa = new Long(1);
            entrada.p_IdUsuario = new Long(21);
            entrada.p_IdModulo = "PLA";
            entrada.p_CsvFile = csvFile;
            entrada.p_Entidad = entidad;
            
            System.out.println(app.logica(jsJB, entrada));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static class EntradaCargaGeneral{
    	public Long p_IdEmpresa;
    	public Long p_IdUsuario;
    	public String p_IdModulo;
    	
    	public String p_CsvFile;
    	public String p_Entidad;
    	public Long IdCarga;
    }
    
    public boolean logica(BasicDataSource jsJB, EntradaCargaGeneral entrada){

        System.out.println("RUTA: " + entrada.p_CsvFile);
        System.out.println("ENTIDAD: " + entrada.p_Entidad);
        
        log = new Log("PLA.CARGAGENERALPERU_"+entrada.p_Entidad);

        FileLoaderFactory fileFactory = new FileLoaderFactory(entrada.p_Entidad);
        FileLoader loader = fileFactory.getConcrete();
        List<Object> lines = loader.parse(entrada);
        if (lines != null && !lines.isEmpty()) {
            DbLoaderFactory dbFactory = new DbLoaderFactory(entrada.p_Entidad);
            DbLoader dbLoader = dbFactory.getConcrete();
            try(Connection conn = jsJB.getConnection()){
            	entrada.IdCarga = Utils.getSecuencia(conn, "BSCL.Plataforma.BD.Secuencias::PLA.ProcesoMigracion", (long) 1).get(0);
            	long init = System.currentTimeMillis();
            	List<String> resultado = dbLoader.loadIntoDb(jsJB, entrada, lines);
            	log.write("CARGA ENTIDAD: " + entrada.p_Entidad+", ID CARGA: " + entrada.IdCarga+", Tiempo(ms): "+ (System.currentTimeMillis()-init));
                for(String res : resultado){
                	log.write(res);
                }
                return true;	
            }catch (Exception e) {
				log.write("ERROR AL OBTENER CONEXION A LA BD", e);
			}
        } else {
            System.out.println("El archivo no existe, o esta vacio.");
        }
        return false;
    }

}
