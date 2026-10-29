package cl.bcs.plataforma.jobs;
/*package cl.bcs.custodia;

/********************************
 * 
 * Objetivo : Comparar los dias feriados cargados en ruebas y actualizar los de optimus de forma automatizada.
 * Aiutor   : - BCS
 * Fecha    : 19/05/2025
 ********************************/


import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class CargarDiasFeriados implements JavaCall {

    static final Logger logger = LogManager.getLogger(CargarDiasFeriados.class.getName());
    final String SQL_SELECT_FERIADOS_RUEBAS = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarDiasFeriados_SelectFeriadosRuebas.sql");
    final String SQL_CREATE_TEMP_TABLE = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarDiasFeriados_CreateTablaTemporal.sql");
    final String SQL_INSERT_FERIADOS_RUEBAS_TABLA_TEMPORAL = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarDiasFeriados_InsertFeriadosRuebas.sql");
    final String SQL_INSERT_FERIADOS_RUEBAS_TABLA_OPTIMUS = loadResourceToString("cl/bcs/plataforma/jobs/sql/CargarDiasFeriados_InsertFeriadosRuebasOptimus.sql");

    public static class EntradaCargarDiasFeriados extends BcsPojo{
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
    }

    public static class SalidaCargarDiasFeriados extends BcsPojo{
        public List<MensajeSalida> P_OUT_MENSAJES;
        public Long P_OUT_ERRORESCONTADOR;   
        public SalidaCargarDiasFeriados() {
        	this.P_OUT_MENSAJES = new ArrayList<>();
        	this.P_OUT_ERRORESCONTADOR = 0L;
        }
    }

    public static class Feriado extends BcsPojo{
        public String tipoFecha;
        public String fecha;
        public BigDecimal numRemate;
    }

    public static class RegistroAInsertar extends BcsPojo{
        public String tipoFecha;
        public String fecha;
        public BigDecimal numRemate;     
    }
    
   ///Prueba local///
    @Override
    public JsonObject call(Connection con, JsonObject jin) throws Exception {
    	Gson gson = new Gson();
    	EntradaCargarDiasFeriados in = gson.fromJson(jin.toString(), EntradaCargarDiasFeriados.class);
    	SalidaCargarDiasFeriados out = logica(con, in);
    	out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
    	JsonParser parser = new JsonParser();
    	JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
    	return dataout;
    }   
    //-------------------------------------------
  	// LOGICA
  	//-------------------------------------------
    public SalidaCargarDiasFeriados logica(Connection con, EntradaCargarDiasFeriados EntradaCargarDiasFeriados) throws Exception {
        logger.info("INICIO :: CargarDiasFeriados");
        SalidaCargarDiasFeriados out = new SalidaCargarDiasFeriados();
        logger.info("EntradaCargarDiasFeriados: " + EntradaCargarDiasFeriados.toString());
        
        SQL query = new SQL();
        logger.info("INICIO :: Crear tabla Temporal");
        try (java.sql.Statement stmt = con.createStatement()){
        	query.exec(con, SQL_CREATE_TEMP_TABLE);
        	logger.info("Tabla temporal #p_TempFeriados creada.");
        	}catch (SQLException e){
        			logger.error("Error al crear la tabla temporal", e);
        			throw e;
        		}
        List<RegistroAInsertar> feriados = obtenerFeriadoRuebas(con, EntradaCargarDiasFeriados);
        logger.debug("Feriados obtenidos: " + feriados.size());    

        if(feriados.isEmpty()){
        	out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L,PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "ERROR_NO_SE_ENCONTRARON_FERIADOS_EN_RUEBAS",new HashMap<>(),con,1L));
        	out.P_OUT_ERRORESCONTADOR++;
        	return out;
        		}
      
        Boolean insertarFeriados = insertarFeriados(con, feriados,out);
        if(insertarFeriados) {
        	insertarFeriadosOptimus(con);
        	return out;
        	}    
        logger.info("SalidaCargarDiasFeriados: " + out.toString());
        logger.info("TERMINO :: CargarDiasFeriados");
        return out;
    }

    public boolean insertarFeriados(Connection con, List<RegistroAInsertar> feriados, SalidaCargarDiasFeriados out) throws Exception {
        logger.debug("INICIO::CargarDiasFeriados.insertarFeriadosEnTemporal");
        long inicio = System.currentTimeMillis();
        boolean operacionTotalmenteExitosa = true; 
        try (PreparedStatement pstmt = con.prepareStatement(SQL_INSERT_FERIADOS_RUEBAS_TABLA_TEMPORAL)) {
        	for (RegistroAInsertar feriado : feriados) {
        		if (feriado.tipoFecha == null || feriado.fecha == null || feriado.numRemate == null) {
        			logger.warn("Registro con datos nulos omitido para inserción en tabla #p_TempFeriados {}", feriado);
        			out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA, "ADVERTENCIA_REGISTRO_NULO_SALTADO", new HashMap<>(), con, 0L));                    
        			continue; 
        		}
                pstmt.setString(1, feriado.tipoFecha);
                pstmt.setString(2, feriado.fecha);
                pstmt.setBigDecimal(3, feriado.numRemate);
                pstmt.addBatch();
            	}
        	logger.info("Feriados a insertar en #p_TempFeriados: " + feriados.size());
            int[] resultadoDelbash = pstmt.executeBatch(); 
            int registrosInsertadosExitosamente = 0;
            for(int resultado : resultadoDelbash) {
            	if (resultado >= 0 || resultado == Statement.SUCCESS_NO_INFO) {registrosInsertadosExitosamente++;
                	} else if (resultado == Statement.EXECUTE_FAILED) {operacionTotalmenteExitosa = false;
                    	logger.error("Una operación dentro del bash de inserción en tabla #p_TempFeriados. Resultado: {}", resultado);
                    	out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "ERROR_OPERACION_BATCH_FALLIDA", new HashMap<>(), con, 1L));
                    	out.P_OUT_ERRORESCONTADOR++;
                		}
            		}
            logger.info("Feriados insertados en #p_TempFeriados:" + registrosInsertadosExitosamente);
        	}catch (SQLException e) {operacionTotalmenteExitosa = false;
            	logger.error("Error SQL general al insertar en la tabla temporal (batch): " + e.getMessage(), e);
            	out.P_OUT_MENSAJES.add(new PlataformaUtils.MensajeSalida(0L, PlataformaUtils.MensajeSalida.MENSAJE_ERROR, "ERROR_SQL_INSERT_TEMPORAL", new HashMap<>(), con, 1L));
            	out.P_OUT_ERRORESCONTADOR++;
        		}
        long fin = System.currentTimeMillis();
        logger.debug("FIN::CargarDiasFeriados.insertarFeriados tiempo:" + (fin - inicio));
        return operacionTotalmenteExitosa;
    }

    public void insertarFeriadosOptimus(Connection con) throws SQLException {
        logger.info("Iniciando inserción en Optimus");
        try (PreparedStatement pstmt = con.prepareStatement(SQL_INSERT_FERIADOS_RUEBAS_TABLA_OPTIMUS)) {
        	int affectedRows = pstmt.executeUpdate();
        	logger.info("Feriados insertados la tabla REG:DiaFeriado de Optimus: " + affectedRows);
        }catch (SQLException e) {
        	logger.error("Error al insertar en Optimus", e);
            throw e; 
        }
    }
    //-------------------------------------------
    // Conexion a Ruebas
    //-------------------------------------------
    private List<RegistroAInsertar> obtenerFeriadoRuebas(Connection con, EntradaCargarDiasFeriados EntradaCargarDiasFeriados) {
    	logger.debug("INICIO::CargarDiasFeriados.obtenerFeriadoRuebas");
    	long inicio = System.currentTimeMillis();
    	HashMap<String, Object> params = new HashMap<>();
        SQL _sql = new SQL();
        List<RegistroAInsertar> feriadosRuebas = new ArrayList<>();
         
        try {
     		String DBdriver, DBuser, DBpass, DBurl, DBprop;
     		String RutaArchivosConfiguracion = Utils.getConfiguracion(con, Utils.getSchema(), 1L,"PLA.RutaArchivosConfiguracion");
     		Properties prop = new Properties();
     		InputStream input = new FileInputStream(RutaArchivosConfiguracion + "ruebas.properties");
     		prop.load(input);
     		DBdriver = prop.getProperty("DBdriver");
     		DBuser = prop.getProperty("DBuser");
     		DBpass = prop.getProperty("DBpass");
     		DBurl = prop.getProperty("DBurl");
     		DBprop = prop.getProperty("DBprop");
     		input.close();
     		logger.info("Configuracion cargada: " + DBurl.toString());
    	
        	///Esto es solo para debug Local///
           /* String DBdriver = "net.sourceforge.jtds.jdbc.Driver";
            String DBuser = "consorcio";
            String DBpass = "consorcio";
            String DBurl = "jdbc:jtds:sqlserver://161.238.92.5:1433/RUEBAS";
            String DBprop = "SocketTimeout=120;LoginTimeout=30;";*/
     		///////////////////////////////////////
            BasicDataSource dsTarget = new BasicDataSource();
            dsTarget.setDriverClassName(DBdriver);
            dsTarget.setUsername(DBuser);
            dsTarget.setPassword(DBpass);
            dsTarget.setUrl(DBurl);
            dsTarget.setConnectionProperties(DBprop);
            dsTarget.setDefaultAutoCommit(true);
            logger.info("Conectado a RUEBAS");

            try (Connection conRU = dsTarget.getConnection()) {
                String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, EntradaCargarDiasFeriados.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
                params.put("p_fechaSistema", fechaSistema);
                feriadosRuebas = _sql.query(conRU, SQL_SELECT_FERIADOS_RUEBAS, params, RegistroAInsertar.class);
                
            } catch (Exception e) {
                logger.error("ERROR al obtener Feriados de Ruebas", e);
            }
        } catch (Exception e) {
            logger.error("ERROR al conectar a Ruebas", e);
        }
        
        long fin = System.currentTimeMillis();
        logger.debug("FIN::CargarDiasFeriados.obtenerFeriadoRuebas tiempo: " + (fin - inicio));
        return feriadosRuebas;
    }

    //-------------------------------------------
    // METODO PARA PRUEBAS LOCALES
    //-------------------------------------------
    public static void main(String[] args) {
        try {
            System.out.println("== INICIO ==");
            Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties");
            EntradaCargarDiasFeriados EntradaCargarDiasFeriados = new EntradaCargarDiasFeriados();
            EntradaCargarDiasFeriados.p_IdEmpresa = 1L;
            EntradaCargarDiasFeriados.p_IdUsuario = 0L;
            EntradaCargarDiasFeriados.p_IdModulo = "PLA";          
            System.out.println(EntradaCargarDiasFeriados.toString());

            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonObject dataJsonIn = parser.parse(gson.toJson(EntradaCargarDiasFeriados)).getAsJsonObject();
            CargarDiasFeriados demo = new CargarDiasFeriados();
            SalidaCargarDiasFeriados resp = gson.fromJson(demo.call(con, dataJsonIn), SalidaCargarDiasFeriados.class);
            System.out.println(resp);            
            con.commit();
           /* con.rollback();*/
        } catch (Exception e) {
            e.printStackTrace();
        }   
        System.out.println("== END ==");
    }
}
