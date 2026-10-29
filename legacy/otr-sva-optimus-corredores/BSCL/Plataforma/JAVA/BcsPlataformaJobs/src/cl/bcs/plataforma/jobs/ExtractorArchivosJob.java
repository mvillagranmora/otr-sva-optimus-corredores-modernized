package cl.bcs.plataforma.jobs;
import java.io.InputStream;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sql.DataSource;

import org.sql2o.Sql2o;

import cl.bcs.plataforma.jobs.extractor.FileExtractor;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
/**
 *
 * @author Rulloa
 */
public class ExtractorArchivosJob extends BcsJob {
	AtomicBoolean running=new AtomicBoolean(false); 
	
	private final Log log = new Log("PLA.EXTRACTOR_ARCHIVOS");
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		go(super.getSql2o(),false);
		return true;		
	}
	
	public void go(Sql2o sql2ox, boolean isTestMode) throws InterruptedException, Throwable {
		
		String ftpHostname;
		String ftpUser;
		String ftpPassword;
		String schema;
		Long idEmpresa;
		Long idUsuario = 0L;
		String idModulo = "PLA";
		String idTipoTarea = "PROCESADOR_ARCHIVOS_RUEBAS";
		String folder = "";
		
		if (isTestMode) {
			idEmpresa = 1L;
			ftpHostname = System.getenv("FTP_TEST_HOST");
			ftpUser = System.getenv("FTP_TEST_USER");
			ftpPassword = System.getenv("FTP_TEST_PASSWORD");
			schema = "BSCL";
			//folder = "OPTIMUS_DES/SALIDA";
			//folder = "OPTIMUS_QA/SALIDA";
			folder = "OPTIMUS_UAT/SALIDA";
		} else {
			idEmpresa = this.getParamInteger("idEmpresa")*1L;
			ftpHostname = this.getParam("ftpHostname");
			ftpUser = this.getParam("ftpUser");
			ftpPassword = this.getParam("ftpPassword");
			schema = this.getParam("schema");
			folder = this.getParam("folder");
		}
		
		log.write("CONECTANDO A :" + ftpHostname + ":" + ftpUser + ":****");
		InputStream in = this.getClass().getResourceAsStream("/cl/bcs/plataforma/jobs/extractor/configuration.properties");
		Properties prop = new Properties();
		prop.load(in);
		FileExtractor test1 = new FileExtractor(ftpHostname,ftpUser,ftpPassword, prop,schema,sql2ox,idEmpresa,folder,log);
		boolean error = test1.loadFiles();
		if (!error) {
			try (org.sql2o.Connection conn = sql2ox.beginTransaction()) {
				String callSP = "CALL \"BSCL.Plataforma.BD.Servicios::BAT.AgregarProgramaSinParametros\"(p_IdEmpresa => ?,p_IdUsuario => ?,p_IdModulo => ?, p_IdTipoTarea => ?, p_out_IdPrograma => ?, p_out_AdvertenciasContador => ?,p_out_AdvertenciasDetalle => ?,p_out_ErroresContador => ?,p_out_ErroresDetalle => ?)";
				CallableStatement call = conn.getJdbcConnection().prepareCall(callSP);
				call.setLong(1, idEmpresa);
				call.setLong(2, idUsuario);
				call.setString(3, idModulo);
				call.setString(4, idTipoTarea);
				call.registerOutParameter(0, Types.INTEGER);
				call.registerOutParameter(1, Types.INTEGER);
				call.registerOutParameter(2, Types.INTEGER);
				call.execute();
				conn.commit();
			}
			
		}
		else {
			//no cargue nada
		}
		
	}
	
	
	@Override
    public void onError(Throwable t) {
		log.write(":Extractor de Archivos:  ****** ERROR  *******", t.getMessage());  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	
	private static final String DB_DRIVER = "com.sap.db.jdbc.Driver";
	
	private static final String DB_CONNECTION_DESA = "jdbc:sap://161.238.34.246:30215/?autocommit=false&currentschema=BSCL";
	private static final String DB_USER_DESA = "APPJAVA";
	private static final String DB_PASSWORD_DESA = "Bolsa123";
	
	
	private static final String DB_CONNECTION_QA = "jdbc:sap://161.238.34.246:30315/?autocommit=false&currentschema=BSCL";
	private static final String DB_USER_QA = "MDCLIENT";
	private static final String DB_PASSWORD_QA = "B0lsa1234";
	
	private static final String DB_CONNECTION_UAT = "jdbc:sap://161.238.37.7:30015/?autocommit=false&currentschema=BSCL";
	private static final String DB_USER_UAT = "MDCLIENT";
	private static final String DB_PASSWORD_UAT = "B0lsa123";
	
	private static final String DB_CONNECTION_MB = "jdbc:sap://161.238.37.7:30415/?autocommit=false&currentschema=BSCL";
	private static final String DB_USER_MB = "SOPORTE";
	private static final String DB_PASSWORD_MB = "Bolsa123";
	
	
	private static final String DB_CONNECTION_UAT_2 = "jdbc:sap://161.238.37.7:30115/?autocommit=false&currentschema=BSCL";
	private static final String DB_USER_UAT_2 = "SYSTEM";
	private static final String DB_PASSWORD_UAT_2 = "B0lsa1234";
	
	
	public static void main(String[] args){
	
		ExtractorArchivosJob p = new ExtractorArchivosJob();
		Sql2o sql2ox = new Sql2o(DB_CONNECTION_MB, DB_USER_MB, DB_PASSWORD_MB);
		try {
			p.go(sql2ox,true);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
    
}
