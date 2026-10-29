package cl.bcs.normativa.jobs;
  


import java.sql.Connection;


import java.util.concurrent.atomic.AtomicBoolean;


import com.google.gson.JsonObject;


import cl.bcs.fondomutuo.ActualizarCostoUF_MLT;

import cl.bcs.fondomutuo.ActualizarCostoUF_MLT.EntradaActualizarCostoUF;
import cl.bcs.fondomutuo.ActualizarCostoUF_MLT.SalidaActualizarCostoUF;



import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import javax.sql.DataSource;

import cl.bcs.scheduler.BcsJob;

import cl.bcs.utils.JavaCall;

import cl.bcs.utils.SQL;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BcsJobProcesarCostosComplemento extends BcsJob implements JavaCall{
	

	


	AtomicBoolean running=new AtomicBoolean(false); 
	
	private final Logger log = LogManager.getLogger(BcsJobProcesarCostosComplemento.class);
	
	SQL sql=new SQL();
	

	public String 	FechaMinima;
	
	
	public boolean transaction(Connection dbcon) throws InterruptedException, Throwable {
		log.info("INICIO ProcesarCostosUF Complemento ");
		
		running.set(true);
        DataSource datasource = super.getDataSource();
    	
		
        Connection dbConnection = null;
	
		try {
			dbConnection = datasource.getConnection();
			final long idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
			final long IdUsuario=Long.parseLong(this.getParam("IdUsuario"));
			final long IdPortafolio=Long.parseLong(this.getParam("IdPortafolio"));
			String FechaDesde = this.getParam("FechaDesde");
			String FechaHasta = this.getParam("FechaHasta");
			
			
			//---- para debug ------
			//dbConnection=dbcon;
			//final long idEmpresa =1L;	
			//final long IdUsuario =1L;
			//---- para debug ------
			
		

        	try{
    			
        		ActualizarCostoUF_MLT actualiza = new ActualizarCostoUF_MLT();
				EntradaActualizarCostoUF entradaActualizarCostoUF = new EntradaActualizarCostoUF();
				entradaActualizarCostoUF.p_IdEmpresa = idEmpresa;
				entradaActualizarCostoUF.p_IdUsuario = IdUsuario;
				entradaActualizarCostoUF.p_IdModulo = "NOR";
				

				log.info("====== INICIO DE PROCESO ===========");
				log.debug("NUMERO DE REGISTROS A PROCESAR:" + IdPortafolio);
				
					
				entradaActualizarCostoUF.p_FechaDesde = FechaDesde;
				entradaActualizarCostoUF.p_FechaHasta = FechaHasta;
				entradaActualizarCostoUF.p_Portafolio = IdPortafolio;
					
				SalidaActualizarCostoUF salida = actualiza.logica(dbConnection, entradaActualizarCostoUF);
				log.debug(salida);
			
					
									
				log.info("====== TERMINO DE PROCESO ===========");
				dbConnection.commit();
        	
        		
        	}catch(Exception ex){
        		dbConnection.rollback();
        		ex.printStackTrace(); 
        	}
        	
        	
		} catch (Exception e) {
			dbConnection.rollback();
    		e.printStackTrace(); 
		}
	
		return true;
	}


	
	
	@Override
    public void onError(Throwable t) {
        System.out.println(":BcsJobProcesarServ:  ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		Connection con = PlataformaUtils.getDESAConnection();
		BcsJobProcesarCostos a = new BcsJobProcesarCostos();
		try {
			boolean  b= a.transaction(con);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
