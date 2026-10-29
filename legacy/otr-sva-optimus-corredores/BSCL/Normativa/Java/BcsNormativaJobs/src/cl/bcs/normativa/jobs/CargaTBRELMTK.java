package cl.bcs.normativa.jobs;  
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;


public class CargaTBRELMTK extends BcsJob implements JavaCall {
	private final Log log = new Log("PLA.CargaTBRELMTK");
	
	private final String SQL_TBRELMTK_SELECT = loadResourceToString("cl/bcs/normativa/jobs/sql/CargaTBRELMTK_SelectTBRELMTK.sql");
	
	private final String SQL_TBRELMTK_DELETE = loadResourceToString("cl/bcs/normativa/jobs/sql/CargaTBRELMTK_DeleteTBRELMTK.sql");
	
	private final String SQL_TBRELMTK_INSERT = loadResourceToString("cl/bcs/normativa/jobs/sql/CargaTBRELMTK_InsertTBRELMTK.sql");

		
	public static class TBRELMTK {
		
		public	String		MERCADO ;
		public 	String		NEMO ;
		public	BigDecimal	COD_CORR ;
		public	String		ESTADO ;
		public	String		FEC_INI_MKT ;
		public	String		FEC_TERM_MKT ;
		
		
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}		
	}
	
	public static class Periodo {
		
		public	String		Periodo ;
		
		public Periodo(String Periodo)
		{
			this.Periodo = Periodo;
			
		}
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}		
	}
	
	
    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
    	log.write("INICIO JOB");
    	  long idEmpresa;
    	  
    	 try{
    		 idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
    	 }catch(Exception e){
    		 idEmpresa = 1L;
    	 }  
        String DBdriver,DBuser,DBpass,DBurl,DBprop, strMesProceso;
        SQL _sql=new SQL();
        
      
		final String periodoSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(con, idEmpresa), "yyyy");
		final String mesSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(con, idEmpresa), "MM");
		final String periodoAnterior = String.valueOf(Integer.parseInt(periodoSistema) - 1);
		

		try {
			strMesProceso=ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "NORMATIVA", "TRI_MESCARGA_TBRELMTK");
		}catch(Exception e){
			strMesProceso="01";
	    }  	
		
		if (mesSistema.equals(strMesProceso)) 
		{
			
			List<TBRELMTK> ListaTBRELMTK;		

	        //Obtiene propiedades a la BD RUEBAS  
					
	        try{
	        	Properties prop = new Properties();
	            //InputStream input = new FileInputStream("C:/BCS/Properties/" + "ruebas.properties");	        	
	        	InputStream input = new FileInputStream("ruebas.properties");
	            prop.load(input);
	            DBdriver=prop.getProperty("DBdriver");
	            DBuser=prop.getProperty("DBuser");
	            DBpass=prop.getProperty("DBpass");
	            DBurl=prop.getProperty("DBurl");
	            DBprop=prop.getProperty("DBprop");
	            input.close();
	            log.write("Configuracion cargada");
	        }catch(Exception e){
	        	log.write(e);
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
	            log.write("Conectado a RUEBAS");
	    	}catch(Exception e){
	        	log.write(e);
	        	throw new InterruptedException("ERROR NO SE PUEDE OBTENER CONEXION RUEBAS DB");
	        }
	        
	    	//Obtiene las TBRELMTK desde Ruebas
	    	//List<TBRELMTK> cal_trib_ruebas;
	    	HashMap<String, Object> params = new HashMap<>(2);
	    	params.put("periodoAnterior", periodoAnterior);
	    	
	        try (Connection conRU = dsTarget.getConnection()) {  
				log.write("Obteniendo TBRELMTK desde Ruebas");      	
	        	ListaTBRELMTK = _sql.query(conRU, SQL_TBRELMTK_SELECT,params, TBRELMTK.class);
	        }catch(Exception er) {
	        	log.write(er);
	        	throw new InterruptedException("ERROR NO SE PUEDEN OBTENER  TBRELMTK");        	
	        }
	        log.write(ListaTBRELMTK.size() + " leidos Periodo : " + periodoAnterior);


	        //Datos a Optimus
	        if (ListaTBRELMTK.size()>0) {
	        	
	        
	    		//Eliminamos lo que existe en tabla
	    		try{
	            	log.write("Se elimina la TBRELMTK de Optimus");
	            	//SQL.exec(con, SQL_TBRELMTK_DELETE);
	            	List<Periodo> strPeriodoAnterior = new ArrayList<Periodo>();
	            	strPeriodoAnterior.add(new Periodo(periodoAnterior));
	            	_sql.batch(con, SQL_TBRELMTK_DELETE, strPeriodoAnterior,Periodo.class); 
	    		}catch(Exception e){
	            	log.write(e);
	            	throw new InterruptedException("ERROR NO SE PUDO ELIMINAR TBRELMTK DE OPTIMUS");
	            }
	        	
	        	log.write("* Insertando en TBRELMTK Optimus -> registros =" + ListaTBRELMTK.size());
	        	ListaTBRELMTK.toArray();
	        	_sql.batch(con, SQL_TBRELMTK_INSERT, ListaTBRELMTK, TBRELMTK.class);  
	        	log.write("** Exito Insertando");
	        }else {
	        	log.write("No hay TBRELMTK que ingresar");
	        }
	        
			
		}else {
        	log.write("CARGA TBRELMTK, NO REALIZADA: mes del sistema debe ser: " + strMesProceso);
        }
		
		
        
        
        //Proceso Terminado correctamente
        return true;    	    	
    }

    @Override
    public void onError(Throwable t) {
    	log.write(t.getMessage());
        log.write("****** ERROR TAREA  *******");
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
            Connection con = PlataformaUtils.getDESAConnection();
            // ---------------------------------
            try {
            	CargaTBRELMTK demo=new CargaTBRELMTK();
            	demo.transaction(con);
            	con.commit();
                //con.rollback();
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