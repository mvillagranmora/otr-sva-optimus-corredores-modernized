package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.Map;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.jobs.BcsProcesoPauta;
import cl.bcs.plataforma.jobs.BcsProcesoPauta.SalidaProcesoPauta;
import cl.bcs.plataforma.pla.AperturaMesasModulos;
import cl.bcs.plataforma.pla.AperturaMesasModulos.EntradaAperturaMesasModulos;
import cl.bcs.plataforma.pla.AperturaMesasModulos.SalidaAperturaMesasModulos;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
/**********************************
 * @version 	1.0
 * @author 		nfuentealba
 **********************************/

 public class JobAperturaMesasModulos extends BcsJob implements JavaCall{

    private final Log log = new Log("plataforma.pla.JobAperturaMesasModulos");
    

    
    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
        return logica (con);
	}
    
    public boolean logica(Connection con) throws InterruptedException, Throwable  {
        log.write("****** INICIO JobAperturaMesasModulos  *******");      
        SalidaProcesoPauta salida = new SalidaProcesoPauta();
        try{
        	EntradaAperturaMesasModulos entrada 		=	new EntradaAperturaMesasModulos();
        	SalidaAperturaMesasModulos  salidaservicio 	= 	new SalidaAperturaMesasModulos();
            AperturaMesasModulos 		servicio 		= 	new AperturaMesasModulos();
            
            entrada.p_IdEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
            entrada.p_IdUsuario = Long.parseLong(this.getParam("IdUsuario"));
            entrada.p_IdModulo	= "PLA";
		    
            salidaservicio = servicio.logica(con, entrada);
			
            
			salida.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;	
			salida.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR;
			
			salida.P_OUT_MENSAJE = "";
			salida.P_OUT_MENSAJES.stream().forEach(mensaje->{
				salida.P_OUT_MENSAJE += mensaje.descripcion + "\n";
			});
			
			
			log.write("ERRORES: ", salida.P_OUT_ERRORESCONTADOR);
			log.write("Salida: ");
		}catch(Exception e){
			log.write(e);
    		log.write("***** Error JobAperturaMesasModulos *****");
    		throw e;
		}
        log.write("***** Fin JobAperturaMesasModulos *****");
        if (salida.P_OUT_ERRORESCONTADOR>0){
        	return false;
        }
		return true;
    }

	public static void main(String[] args) {
        //System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getDESAConnection();
            // ---------------------------------
            try {
            	JobAperturaMesasModulos  demo = new JobAperturaMesasModulos ();
            	
            	demo.logica( con );
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
    
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	
}
