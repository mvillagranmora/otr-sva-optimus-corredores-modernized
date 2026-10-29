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

import cl.bcs.normativa.jobs.CargaTBRELMTK.Periodo;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores.FechasIn;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;


public class CargaTBPRAJHI extends BcsJob implements JavaCall {
	private final Log log = new Log("PLA.CargaTBPRAJHI");
	
	private final String SQL_TBPRAJHI_DESTINO_SELECT = loadResourceToString("cl/bcs/normativa/jobs/sql/CargaTBPRAJHI_SelectTBPRAJHI_Destino.sql");
	
	private final String SQL_TBPRAJHI_SELECT = loadResourceToString("cl/bcs/normativa/jobs/sql/CargaTBPRAJHI_SelectTBPRAJHI.sql");
	
	private final String SQL_TBPRAJHI_DELETE = loadResourceToString("cl/bcs/normativa/jobs/sql/CargaTBPRAJHI_DeleteTBPRAJHI.sql");
	
	private final String SQL_TBPRAJHI_INSERT = loadResourceToString("cl/bcs/normativa/jobs/sql/CargaTBPRAJHI_InsertTBPRAJHI.sql");
	
	public static class TBPRAJHI {
		
		public	String		FECHA;
		public 	String		MERCADO;
		public 	String		BOLSA;
		public 	String		NEMO;
		public	BigDecimal	MONTO;
		public	BigDecimal	CANTIDAD;
		public	BigDecimal	PRESEN_AJU;
		public	BigDecimal	PRECIO_MAY_FM;
		public	BigDecimal	PRECIO_MEN_FM;
		public	BigDecimal	MONTO_FM;
		public	BigDecimal	CANTIDAD_FM;
		public	BigDecimal	PRESEN_AJU_120_D;
		public	String		IND_MARKET ;
		
			
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}		
	}
	public static class Destino {
		
		
		public	long Contador;
		
			
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
	
	
	public String obtenerDiaHabilAnterior(Connection con, Long p_IdEmpresa,String p_FechaConsulta) throws Exception {
		log.write("INICIO :: CargaTBPRAJHI.obtenerDiaHabilAnterior");
		
		String diaHabilAnterior = "0001-01-01";
		ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores entradaObtenerDiasHabilesAnteriores = new ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores();
        entradaObtenerDiasHabilesAnteriores.idPais = ConfiguracionSistema.getParametroConfiguracionLong(con, p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		entradaObtenerDiasHabilesAnteriores.diaHabil = "S";
		entradaObtenerDiasHabilesAnteriores.soloCaeDiaHabil = "S";
		FechasIn fecha = new FechasIn(p_FechaConsulta, 1L);
		entradaObtenerDiasHabilesAnteriores.fechas.add(fecha);
		ObtenerDiasHabilesAnteriores obtenerDiasHabilesAnteriores = new ObtenerDiasHabilesAnteriores();
		OutObtenerDiasHabilesAnteriores salidaObtenerDiaHabilAnterior = obtenerDiasHabilesAnteriores.logica(con, entradaObtenerDiasHabilesAnteriores);
		
		diaHabilAnterior = salidaObtenerDiaHabilAnterior.fechas.stream().filter(u -> u.numeroDiasInicial == 1L && p_FechaConsulta.equals(u.fechaInicial)).map(u -> u.fechaCalculada).findFirst().orElse(p_FechaConsulta);
		
		log.write("FIN :: CargaTBPRAJHI.obtenerDiaHabilAnterior");
		return diaHabilAnterior;
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
        long lngContador;
        SQL _sql=new SQL();
        
      
    	final String periodoSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(con, idEmpresa), "yyyy");
		final String mesSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(con, idEmpresa), "MM");
		final String periodoAnterior = String.valueOf(Integer.parseInt(periodoSistema) - 1);

		try {
			strMesProceso=ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "NORMATIVA", "TRI_MESCARGA_TBPRAJHI");
		}catch(Exception e){
			strMesProceso="01";
	    }

		
		if (mesSistema.equals(strMesProceso)) 
		{		
			//verifica total en destino
			HashMap<String, Object> param = new HashMap<>(2);
	    	param.put("periodoAnterior", periodoAnterior);
	    	List<Destino> ListaDESTINO;
	        try  {  
				log.write("Obteniendo TBPRAJHI en Optimus");      	
	        	ListaDESTINO = _sql.query(con, SQL_TBPRAJHI_DESTINO_SELECT,param, Destino.class);
	        	
	        	if(!ListaDESTINO.isEmpty()) {
	        		lngContador = ListaDESTINO.get(0).Contador;
	    		}else {lngContador=0L;}
	        }catch(Exception er) {
	        	log.write(er);
	        	throw new InterruptedException("ERROR NO SE PUEDEN OBTENER TBPRAJHI en Optimus");        	
	        }
	        log.write(lngContador + ", registros previos en Optimus para el periodo : " + periodoAnterior);
			
	        if (lngContador == 0L) {
			
			
					List<TBPRAJHI> ListaTBPRAJHI;		
		
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
			        
			    	//Obtiene las TBPRAJHI desde Ruebas
			    	//List<TBPRAJHI> cal_trib_ruebas;
			    	HashMap<String, Object> params = new HashMap<>(2);
			    	params.put("periodoAnterior", periodoAnterior);
			    	
			        try (Connection conRU = dsTarget.getConnection()) {  
						log.write("Obteniendo TBPRAJHI desde Ruebas");      	
			        	ListaTBPRAJHI = _sql.query(conRU, SQL_TBPRAJHI_SELECT,params, TBPRAJHI.class);
			        }catch(Exception er) {
			        	log.write(er);
			        	throw new InterruptedException("ERROR NO SE PUEDEN OBTENER TBPRAJHI");        	
			        }
			        log.write(ListaTBPRAJHI.size() + " leidos Periodo : " + periodoAnterior);
		
		
			        //Datos a Optimus
			        if (ListaTBPRAJHI.size()>0) {
			        	
			        
			    		//Eliminamos lo que existe en tabla
			    		try{
			            	log.write("Se elimina la TBPRAJHI de Optimus");
			            	//SQL.exec(con, SQL_TBPRAJHI_DELETE);
			            	List<Periodo> strPeriodoAnterior = new ArrayList<Periodo>();
			            	strPeriodoAnterior.add(new Periodo(periodoAnterior));
			            	_sql.batch(con, SQL_TBPRAJHI_DELETE, strPeriodoAnterior,Periodo.class); 
			    		}catch(Exception e){
			            	log.write(e);
			            	throw new InterruptedException("ERROR NO SE PUDO ELIMINAR TBPRAJHI DE OPTIMUS");
			            }
			        	
			        	
			        	log.write("* Insertando en TBPRAJHI Optimus -> registros =" + ListaTBPRAJHI.size());
			        	ListaTBPRAJHI.toArray();
			        	_sql.batch(con, SQL_TBPRAJHI_INSERT, ListaTBPRAJHI, TBPRAJHI.class); 
			        	log.write("** Exito Insertando");
			        }else {
			        	log.write("No hay TBPRAJHI que ingresar");
			        }
	        }else {
	        	log.write("CARGA TBPRAJHI, NO REALIZADA: datos ya existen en el Destino Optimus ");
	        }	
	        
	        
	        
		}else {
        	log.write("CARGA TBPRAJHI, NO REALIZADA: mes del sistema debe ser: " + strMesProceso);
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
            	CargaTBPRAJHI demo=new CargaTBPRAJHI();
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