package cl.bcs.custodia.jobs;

import java.sql.Connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.custodia.GuardarPatrimonioCustodia;
import cl.bcs.custodia.GuardarPatrimonioCustodia.EntradaGuardarPatrimonioCustodia;
import cl.bcs.custodia.GuardarPatrimonioCustodia.SalidaGuardarPatrimonioCustodia;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;

public class GuardarPatrimonioCustodiaJob extends BcsJob{
static final Logger logger = LogManager.getLogger(GuardarPatrimonioCustodiaJob.class);
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	
	public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
		logger.info("***** Inicio GuardarPatrimonioConsolidadoJob *****");
    	try {
    		Long idEmpresa = 0L;
    		Long idUsuario = 0L;
    		String fecha = null;
    		if(debug) {
    			idEmpresa = 1L;
                idUsuario = 1L;
                fecha = "2019-07-31";
    		}else {
    			idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
                idUsuario = Long.parseLong(this.getParam("IdUsuario"));
                fecha = this.getParam("Fecha");
    		}
    		
            if(fecha == null || fecha.isEmpty()) {
            	fecha = ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa,"PLATAFORMA","PLA.FechaSistema");
            }
            
            String[] fechas = null;
            if (fecha.length() > 10) {
            	fechas = fecha.split(";");
            }
            else {
            	fechas = new String[1];
            	fechas[0] = fecha;
            }
            for (String fechaProceso : fechas) {
            	GuardarPatrimonioCustodia guardarPatrimonioCustodia = new GuardarPatrimonioCustodia();
        		EntradaGuardarPatrimonioCustodia entradaGuardarPatrimonioCustodia = new EntradaGuardarPatrimonioCustodia();
        		entradaGuardarPatrimonioCustodia.p_IdEmpresa = idEmpresa;
        		entradaGuardarPatrimonioCustodia.p_IdUsuario = idUsuario;
        		entradaGuardarPatrimonioCustodia.p_IdModulo = "PLA";
        		entradaGuardarPatrimonioCustodia.p_Fecha = fechaProceso;
        		SalidaGuardarPatrimonioCustodia salidaGuardarPatrimonioCustodia = guardarPatrimonioCustodia.logica(con, entradaGuardarPatrimonioCustodia);
        		if(salidaGuardarPatrimonioCustodia.P_OUT_ERRORESCONTADOR > 0) {
        			return false;
        		}
        		else {
        			if (fechas.length > 1) {
        				con.commit();        				
        			}
        		}
            }
            
    	} catch (Exception e) {
    		logger.error("***** Error GuardarPatrimonioConsolidadoJob *****",e);
    		throw e;
    	}
    	logger.info("***** Fin GuardarPatrimonioConsolidadoJob *****");
		return true;
	}
	
	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}
	
	//DEBUG
		public static void main(String[] args) {
			// Example
			System.out.println("== START ==");
			try (Connection con = PlataformaUtils.getDESAConnection()) {
				// ---------------------------------
				try {
					GuardarPatrimonioCustodiaJob apf = new GuardarPatrimonioCustodiaJob();
					System.out.println(apf.logica(con,true));
					con.commit();
				} catch (Exception er) {
					er.printStackTrace();
					con.rollback();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("== END ==");
		}
}
