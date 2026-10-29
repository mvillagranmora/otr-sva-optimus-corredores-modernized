package cl.bcs.custodia.jobs;

import java.sql.Connection;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.JavaCall;
import cl.bcs.socionegocio.bck.ReintentoPrestamosBlockChain;
import cl.bcs.socionegocio.bck.ReintentoPrestamosBlockChain.ReintentoPrestamosBlockChain_Entrada;

public class ReintentoPrestamosBlockChainJob extends BcsJob implements JavaCall{
	private final Log log = new Log("CUS.ReintentoPrestamoBlockChain"); 

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
			log.write("Inicio proceso reintento de envio de prestamos BlockChain");
			
			Long idEmpresa = 0L;
	        Long idUsuario = 0L;
	        
	        if (debug) {
	        	idEmpresa = 1L;
	        	idUsuario = 0L;
	        } else{
	        	 idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
	             idUsuario = Long.parseLong(this.getParam("IdUsuario"));
	        }
	        
	        ReintentoPrestamosBlockChain_Entrada in = new ReintentoPrestamosBlockChain_Entrada();
	        in.p_IdEmpresa = idEmpresa;
	        in.p_IdUsuario = idUsuario;
	        
	        ReintentoPrestamosBlockChain objReintentoPresBC = new ReintentoPrestamosBlockChain();
	        
	        objReintentoPresBC.logica(conn, in);
	        
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
            	ReintentoPrestamosBlockChainJob demo = new ReintentoPrestamosBlockChainJob();
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
