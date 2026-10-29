package cl.bcs.custodia.jobs;

import java.sql.Connection;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.JavaCall;
import cl.bcs.socionegocio.bck.ActualizaEstadosBlockChain;
import cl.bcs.socionegocio.bck.ActualizaEstadosBlockChain.ActualizaEstadosBlockChain_Entrada;

public class ActualizaEstadosBlockChainJob extends BcsJob implements JavaCall{
	private final Log log = new Log("CUS.ConsultaEstadosBlockChain"); 

	@Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
        return logica (con,false);        
	}
    @Override
    public void onError(Throwable t) {
    	log.write("  ****** ERROR  *******");
    	log.write(t);
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	public boolean logica(Connection conn,boolean debug) {
		try {
			log.write("Inicio proceso Consulta Estado BlockChain");
			
			Long idEmpresa = 0L;
	
	        
	        if (debug) {
	        	idEmpresa = 1L;
	        } else{
	        	 idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));       
	        }
	        
	        ActualizaEstadosBlockChain_Entrada in = new ActualizaEstadosBlockChain_Entrada();
	        in.p_IdEmpresa = idEmpresa;
	        in.p_IdCliente = 1L;
	        
	        ActualizaEstadosBlockChain objActualizarEstadoBC = new ActualizaEstadosBlockChain();
	        
	        objActualizarEstadoBC.logica(conn, in);
	        
		}
		catch (Exception e) {
			log.write("Error al iniciar proceso de Reintento envio de prestamos BlockChain");
			log.write(e);
		}
		
		return true;
	}
	public static void main(String[] args) {
        //System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getDESAConnection();
            // ---------------------------------
            try {
            	ActualizaEstadosBlockChainJob demo = new ActualizaEstadosBlockChainJob();
            	demo.logica(con,true);
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
