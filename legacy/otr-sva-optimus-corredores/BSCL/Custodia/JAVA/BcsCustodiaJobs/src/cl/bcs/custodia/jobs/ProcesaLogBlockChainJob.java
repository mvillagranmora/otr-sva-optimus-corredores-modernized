package cl.bcs.custodia.jobs;
import java.sql.Connection;
import com.google.gson.JsonObject;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.JavaCall;
import cl.bcs.socionegocio.bck.ProcesaLogBlockChain;
import cl.bcs.socionegocio.bck.ProcesaLogBlockChain.ProcesaLogBlockChain_Entrada;
/*JOB:
 * 
 * ANULA
 * ANTICIPA
 * 
 * DESDE BLOCKCHAIN A OPTIMUS
 * 
 * */
public class ProcesaLogBlockChainJob extends BcsJob {
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
	public boolean logica(Connection conn,boolean debug) {
		try {
			log.write("Inicio proceso Log BlockChain");
			
			Long idEmpresa = Long.parseLong(this.getOptionalParam("IdEmpresa").orElse("1"));
			Long idUsuario = Long.parseLong(this.getOptionalParam("IdUsuario").orElse("1"));
		 
	        ProcesaLogBlockChain_Entrada in = new ProcesaLogBlockChain_Entrada();
	        in.p_IdEmpresa = idEmpresa;
	        in.p_IdUsuario = idUsuario;
	        
	        ProcesaLogBlockChain objProcesaLogBC = new ProcesaLogBlockChain();
	        
	        objProcesaLogBC.logica(conn, in);
	        
		}
		catch (Exception e) {
			log.write("Error al iniciar proceso de procesa log BlockChain");
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
            	ProcesaLogBlockChainJob demo = new ProcesaLogBlockChainJob();
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
