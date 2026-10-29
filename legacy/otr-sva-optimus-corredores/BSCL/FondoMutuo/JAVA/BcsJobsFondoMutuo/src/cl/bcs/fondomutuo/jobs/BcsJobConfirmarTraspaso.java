package cl.bcs.fondomutuo.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.ConfirmarTraspaso;
import cl.bcs.fondomutuo.ConfirmarTraspaso.EntradaConfirmarTraspaso;
import cl.bcs.fondomutuo.ConfirmarTraspaso.SalidaConfirmarTraspaso;
import cl.bcs.fondomutuo.pojo.Traspaso.RegistroTraspaso;
import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.scheduler.BcsJob;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;


public class BcsJobConfirmarTraspaso extends BcsJob implements JavaCall{

    AtomicBoolean running = new AtomicBoolean(false);
    public String IdTipoPrograma = "CONFIRMARTRASPASOFFMM";
    private final Log log = new Log("FM.BcsJobConfirmarTraspaso");
    private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
    
    private final String SQL_SELECT_TRASPASO = loadResourceToString("cl/bcs/fondomutuo/jobs/sql/ConfirmarTraspaso_SelectTraspaso.sql");
   
    
    public boolean transaction(Connection conn) throws InterruptedException, Throwable {
        log.write("*CONFIRMARTRASPASOFFMM* Inicio de Job");
        SQL sql=new SQL();
        running.set(true);
       
        try{
            Long idEmpresa = 1L;
            Long idUsuario = 3L;
            String modulo = "FMM";
           

            log.write( "*CONFIRMARTRASPASOFFMM* Obtencion de parametros");

            idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
            idUsuario = Long.parseLong(this.getParam("IdUsuario"));
            //trans = this.getParam("Transacciones");

            log.write( "*CONFIRMARTRASPASOFFMM* idEmpresa: " + idEmpresa);
            log.write( "*CONFIRMARTRASPASOFFMM* idUsuario: " + idUsuario);
            String traspasoConfirmar="";
            long parDiasTranscurridos=0L;
            
            try{traspasoConfirmar = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "FONDOMUTUO", "TRASPASO_PAUTA_CONFIRMAR");
            } catch (Exception e) {if (traspasoConfirmar==""){traspasoConfirmar=_c.NO;};};
            
            try{parDiasTranscurridos = Long.parseLong(ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "FONDOMUTUO", "TRASPASO_DIAS_CONFIRMAR"));
            } catch (Exception e) {if (parDiasTranscurridos==0L){parDiasTranscurridos=0L;};};
        
            
            log.write( "*CONFIRMARTRASPASOFFMM* Parametro : TRASPASO_PAUTA_CONFIRMAR = " + traspasoConfirmar);
           
        	log.write( "*CONFIRMARTRASPASOFFMM* Inicio de seleccion de traspasos");
        	if(_c.SI.equals(traspasoConfirmar))
        	{
        		
        	   	
		        	HashMap<String,Object> params = new HashMap<String,Object>();
		    		params.put("IdEmpresa", idEmpresa);
		    		params.put("DiasTranscurridos", parDiasTranscurridos);
		    		
		    		List<RegistroTraspaso> traspasos = sql.query(conn, SQL_SELECT_TRASPASO, params, RegistroTraspaso.class);
		         
		
		    		if(traspasos.size() != 0){
		    			log.write( "*CONFIRMARTRASPASOFFMM* Inicio de llamada a servicio Confirmar");
		    			ConfirmarTraspaso ProcesarTraspasos = new ConfirmarTraspaso();
		   	          	EntradaConfirmarTraspaso entradaConfirmarTraspaso = new EntradaConfirmarTraspaso();
		   	          
		    			for(RegistroTraspaso in : traspasos){
		    				
		    				ConfirmarTraspaso.InTraspaso   traspasoObj = new ConfirmarTraspaso.InTraspaso();
		                	                	
		    				traspasoObj.IdTraspaso =  in.IdTraspaso;
		                	entradaConfirmarTraspaso.p_Traspaso.add(traspasoObj);
		    				
		    			}
		    			
		            	entradaConfirmarTraspaso.p_IdEmpresa = idEmpresa;
		            	entradaConfirmarTraspaso.p_IdUsuario = idUsuario;
		            	entradaConfirmarTraspaso.p_IdModulo = modulo;
		    			 
		            	SalidaConfirmarTraspaso salida = ProcesarTraspasos.logica(conn, entradaConfirmarTraspaso);
		            	log.write( "*CONFIRMARTRASPASOFFMM* Fin ejecucion de servicio");
		            	
		            	if(salida.P_OUT_ERRORESCONTADOR > 0){
		            		String error = "";
							for(MensajeSalida s : salida.P_OUT_MENSAJES){
								if(MensajeSalida.MENSAJE_ERROR.equals(s.tipo)){
									error = s.descripcion;
								}
							}
							if(!error.isEmpty()){
								throw new Exception(error);
							}else{
								throw new Exception("Error al procesar traspaso");
							}
		            	}
		   			
		    		};            
        	};        
        	log.write( "*CONFIRMARTRASPASOFFMM* fin procesamiento de traspasos");
  
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
        BcsJobConfirmarTraspaso p = new BcsJobConfirmarTraspaso();
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

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
