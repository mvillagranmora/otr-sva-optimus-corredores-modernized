package cl.bcs.plataforma.jobs;


import java.sql.Connection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonObject;
import cl.bcs.plataforma.jobs.CargarDiasFeriados.EntradaCargarDiasFeriados;
import cl.bcs.plataforma.pla.CargarDiasInhabiles;
import cl.bcs.plataforma.pla.CargarDiasInhabiles.EntradaCargarDiasInhabiles;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;



public class CargarDiasFeriadosJob extends BcsJob implements JavaCall{
	static final Logger logger = LogManager.getLogger(CargarDiasFeriados.class.getName());
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		
		logger.info("INICIO Del Job");
		CargarDiasFeriados cargardiasferiados= new CargarDiasFeriados();
		EntradaCargarDiasFeriados EntradaCargarDiasFeriados = new EntradaCargarDiasFeriados();
        EntradaCargarDiasFeriados.p_IdEmpresa = 1L;
        EntradaCargarDiasFeriados.p_IdUsuario = 0L;
        EntradaCargarDiasFeriados.p_IdModulo = "PLA";
        
        CargarDiasInhabiles cargarDiasInhabiles = new CargarDiasInhabiles();
        EntradaCargarDiasInhabiles entradaCargarDiasInhabiles = new EntradaCargarDiasInhabiles();
		entradaCargarDiasInhabiles.p_IdEmpresa = 1L;
		entradaCargarDiasInhabiles.p_IdUsuario = 1L;
		entradaCargarDiasInhabiles.p_IdModulo = "PLA";
        
        try {
        	logger.info("*Prepando llamado CargarDiasFeriados*");
        	cargardiasferiados.logica(con,EntradaCargarDiasFeriados);
        	}
        catch(Exception e){
        	logger.error("*Error al Hacer el llamado a CargarDiasFeriados*");
        	}
        
        try {
        	logger.info("*Prepando llamado CargarDiasInhabiles*");
        	cargarDiasInhabiles.logica(con, entradaCargarDiasInhabiles);
        } catch (Exception e) {
        	logger.error("*Error al Hacer el llamado a CargarDiasInhabiles*");
        }
        
		return true;
		}
	
	@Override
	    public void onError(Throwable t) {
	    	logger.info(t.getMessage());
	        logger.error("****** ERROR TAREA  *******",t);
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
			Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties");							
			try{
				CargarDiasFeriadosJob demo=new CargarDiasFeriadosJob();
        		demo.transaction(con);
        		con.commit();
			}catch (Throwable er) {
        		er.printStackTrace();
        		con.rollback();
        	}
			}catch (Exception e) {
				e.printStackTrace();
			}
		System.out.println("== END ==");       
		}

	}

