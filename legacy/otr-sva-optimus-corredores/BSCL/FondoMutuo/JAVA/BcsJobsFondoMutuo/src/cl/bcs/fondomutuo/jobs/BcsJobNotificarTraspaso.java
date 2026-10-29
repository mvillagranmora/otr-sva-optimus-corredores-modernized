package cl.bcs.fondomutuo.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import cl.bcs.fondomutuo.NotificarTraspasoConfirmar;
import cl.bcs.fondomutuo.NotificarTraspasoConfirmar.EntradaNotificarTraspasoConfirmar;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;

import cl.bcs.utils.Log;



public class BcsJobNotificarTraspaso extends BcsJob{

    AtomicBoolean running = new AtomicBoolean(false);
    public String IdTipoPrograma = "NOTIFICARTRASPASOFFMM";
    private final Log log = new Log("FM.BcsJobNotificarTraspaso");
  
    public boolean transaction(Connection conn) throws InterruptedException, Throwable {
        log.write("*NOTIFICARTRASPASOFFMM* Inicio de Job");
       
        running.set(true);
       
        try{
            Long idEmpresa = 1L;
            Long idUsuario = 3L;
            String modulo = "FMM";
           

            log.write( "*NOTIFICARTRASPASOFFMM* Obtencion de parametros");

            idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
            idUsuario = Long.parseLong(this.getParam("IdUsuario"));
         
            log.write( "*NOTIFICARTRASPASOFFMM* idEmpresa: " + idEmpresa);
            log.write( "*NOTIFICARTRASPASOFFMM* idUsuario: " + idUsuario);
    	
			log.write( "*NOTIFICARTRASPASOFFMM* Inicio de llamada a servicio notificacion");
			
			NotificarTraspasoConfirmar p = new NotificarTraspasoConfirmar();
			
			EntradaNotificarTraspasoConfirmar in = new EntradaNotificarTraspasoConfirmar();
			in.p_IdEmpresa = idEmpresa;
			in.p_IdModulo = modulo;
			in.p_IdUsuario = idUsuario;
			
			
            p.logica(conn, in);
		    		  
        	       
        	log.write( "*NOTIFICARTRASPASOFFMM* fin notificacion de traspasos");
  
        } catch (Exception e) {
            log.write("Error",e);
            throw e;
        } finally {
        	 running.set(false);
        }

        running.set(false);
        return true;
    }

    @Override
    public void onError(Throwable t) {
        System.out.println(":ProcesadorTransacciones:  ****** ERROR  *******");
        t.printStackTrace();
    }

    @Override
    public void pause() {

    }

    public static void main(String[] args) throws SQLException {
    	Connection con = PlataformaUtils.getDESAConnection();
        BcsJobNotificarTraspaso p = new BcsJobNotificarTraspaso();
        try {
            p.transaction(con);
            con.commit();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


}
