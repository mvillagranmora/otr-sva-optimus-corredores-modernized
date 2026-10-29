package cl.bcs.normativa.jobs;  
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

/***************************************
 * @version 	1.0
 * @author 		Ernesto Velasquez 
 *
**************************************/

public class CargaCalificacionesTributariasBD extends BcsJob implements JavaCall {
	private final Log log = new Log("PLA.CargaCalificacionesTributariasBD");
	
	private final String SQL_CAL_TRI_SELECT = loadResourceToString("cl/bcs/normativa/jobs/sql/CargaCalificacionesTributariasBD_Select.sql");
	
	private final String SQL_CAL_TRI_DELETE = loadResourceToString("cl/bcs/normativa/jobs/sql/CargaCalificacionesTributariasBD_Delete.sql");
	
	private final String SQL_CAL_TRI_INSERT = loadResourceToString("cl/bcs/normativa/jobs/sql/CargaCalificacionesTributariasBD_Insert.sql");
	
	public static class CalificacionesTributarias {
		public BigDecimal EJERCICIO;
		public String MERCADO;
		public String NEMO;
		public String FEC_PAGO;
		public BigDecimal SEC_EVE;
		public String DESCRIPCION;
		public BigDecimal FACTOR_06;
		public BigDecimal FACTOR_07;
		public BigDecimal FACTOR_08;
		public BigDecimal FACTOR_09;
		public BigDecimal FACTOR_10;
		public BigDecimal FACTOR_11;
		public BigDecimal FACTOR_12;
		public BigDecimal NUM_DIVDO;
		public String TIPO_SOC;
		public BigDecimal FACTOR_13;
		public BigDecimal FACTOR_14;
		public BigDecimal FACTOR_15;
		public BigDecimal FACTOR_16;
		public BigDecimal FACTOR_17;
		public BigDecimal FACTOR_18;
		public BigDecimal FACTOR_19;
		public BigDecimal FACTOR_20;
		public BigDecimal FACTOR_21;
		public BigDecimal FACTOR_22;
		public BigDecimal FACTOR_23;
		public BigDecimal FACTOR_24;
		public BigDecimal FACTORACT;
		public BigDecimal NUMCERTSOC;
		public BigDecimal FACTOR_25;
		public BigDecimal FACTOR_26;
		public BigDecimal FACTOR_27;
		public BigDecimal FACTOR_28;
		public BigDecimal FACTOR_29;
		public BigDecimal FACTOR_30;
		public BigDecimal FACTOR_31;
		public BigDecimal FACTOR_32;
		public BigDecimal FACTOR_33;
		public BigDecimal FACTOR_34;
		public BigDecimal FACTOR_35;
		public BigDecimal FACTOR_36;
		public BigDecimal FACTOR_37;
		public BigDecimal FACTOR_38;
		public String FechaUltimaMod;
		public String UsuarioUltimaMod;
		public String AcogidoISFUT;
		public String SEC_EVEAuxiliar;
		public String EJERCICIOAuxiliar;
		
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
    	
        String DBdriver,DBuser,DBpass,DBurl,DBprop,anio, strMesProceso;
        SQL _sql=new SQL();
        
		final String periodoSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(con, idEmpresa), "yyyy");
		final String mesSistema = Fecha.toFormat(FechaDB.ObtenerFechaHoraSistemaTs(con, idEmpresa), "MM");
		final String periodoAnterior = String.valueOf(Integer.parseInt(periodoSistema) - 1);
		
		
		
		try {
			strMesProceso=ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "NORMATIVA", "TRI_MESCARGA_TBSICAVC");
		}catch(Exception e){
			strMesProceso="04";
	    }  	
		
		int mesActual = Integer.parseInt(mesSistema);
		int mesHasta = Integer.parseInt(strMesProceso);
		
		
		if (mesActual <= mesHasta) 
		{
			
	
		List<CalificacionesTributarias> ListaCalificacionesTributarias;		

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
        
    	//Obtiene las Calificaciones Tributarias desde Ruebas
    	//List<CalificacionesTributarias> cal_trib_ruebas;
		    	HashMap<String, Object> params = new HashMap<>(2);
		    	params.put("periodoAnterior", periodoAnterior);
		    	
        try (Connection conRU = dsTarget.getConnection()) {  
			log.write("Obteniendo Calificaciones Tributarias desde Ruebas");      	
		        	ListaCalificacionesTributarias = _sql.query(conRU, SQL_CAL_TRI_SELECT,params, CalificacionesTributarias.class);
		        	
        }catch(Exception er) {
        	log.write(er);
        	throw new InterruptedException("ERROR NO SE PUEDEN OBTENER LAS CALIFICACIONES TRIBUTARIAS");        	
        }
		        log.write(ListaCalificacionesTributarias.size() + " leidos Periodo : " + periodoAnterior);
		        
				
				if (ListaCalificacionesTributarias.size()>0) {				        

		//Eliminamos lo que existe en tabla
		try{
							log.write("Se elimina la TBSICAVC de Optimus");
							List<Periodo> strPeriodoAnterior = new ArrayList<Periodo>();
			            	strPeriodoAnterior.add(new Periodo(periodoAnterior));
			            	_sql.batch(con, SQL_CAL_TRI_DELETE, strPeriodoAnterior,Periodo.class); 
		}catch(Exception e){
        	log.write(e);
        	throw new InterruptedException("ERROR NO SE PUDO ELIMINAR TBSICAVC");
        }
			        	
						log.write("* Insertando en TBSICAVC Optimus -> registros =" + ListaCalificacionesTributarias.size());
        	ListaCalificacionesTributarias.toArray();
			        	_sql.batch(con, SQL_CAL_TRI_INSERT, ListaCalificacionesTributarias, CalificacionesTributarias.class); 
			        	log.write("** Exito Insertando");
        }else {
        	log.write("No hay calificaciones tributarias que ingresar");
        }
        
	}else {
    	log.write("CARGA TBSICAVC, NO REALIZADA: mes del sistema :"+ mesSistema +" debe ser menor o igual a : " + strMesProceso  );
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
            	CargaCalificacionesTributariasBD demo=new CargaCalificacionesTributariasBD();
            	demo.transaction(con);
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