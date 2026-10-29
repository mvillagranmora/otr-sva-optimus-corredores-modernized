package cl.bcs.custodia.jobs;
        
import java.sql.Connection;

import com.google.gson.JsonObject;

import cl.bcs.custodia.RetiraCustodiaDeMovimientoNominal;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.JavaCall;

public class JobRetiraCustodiaDeMovimientoNominal extends BcsJob implements JavaCall{
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
			log.write("Inicio proceso Retira Custodia de Movimiento Nominal");
			
			Long idEmpresa = 0L;
	        if (debug) {
	        	idEmpresa = 1L;
	        } else{
	        	 idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));       
	        }

			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn,(Long)idEmpresa,"PLATAFORMA","PLA.FechaSistema");
	        
			RetiraCustodiaDeMovimientoNominal.Entrada in = new RetiraCustodiaDeMovimientoNominal.Entrada();
	        in.p_IdEmpresa = idEmpresa;
	        in.p_Fecha = confFechaSistema;
	        
	        RetiraCustodiaDeMovimientoNominal objRetiraCustodia = new RetiraCustodiaDeMovimientoNominal();
	        
	        objRetiraCustodia.logica(conn, in);
	        
		}
		catch (Exception e) {
			log.write("Error al iniciar proceso de Reintento Retiro Custodia de Movimiento Nominal");
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
            	JobRetiraCustodiaDeMovimientoNominal demo = new JobRetiraCustodiaDeMovimientoNominal();
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
