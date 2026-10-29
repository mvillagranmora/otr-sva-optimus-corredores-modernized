package cl.bcs.cuentaInversion.jobs;

import java.sql.Connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.cuentaInversion.GuardarPatrimonioCaja;
import cl.bcs.cuentaInversion.GuardarPatrimonioCaja.EntradaGuardarPatrimonioCaja;
import cl.bcs.cuentaInversion.GuardarPatrimonioCaja.SalidaGuardarPatrimonioCaja;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;

public class GuardarPatrimonioCajaJob extends BcsJob{
	
static final Logger logger = LogManager.getLogger(GuardarPatrimonioCajaJob.class);
	
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
    		GuardarPatrimonioCaja guardarPatrimonioCaja = new GuardarPatrimonioCaja();
    		EntradaGuardarPatrimonioCaja entradaGuardarPatrimonioCaja = new EntradaGuardarPatrimonioCaja();
    		entradaGuardarPatrimonioCaja.p_IdEmpresa = idEmpresa;
    		entradaGuardarPatrimonioCaja.p_IdUsuario = idUsuario;
    		entradaGuardarPatrimonioCaja.p_IdModulo = "PLA";
    		entradaGuardarPatrimonioCaja.p_Fecha = fecha;
    		SalidaGuardarPatrimonioCaja salidaGuardarPatrimonioCaja = guardarPatrimonioCaja.logica(con, entradaGuardarPatrimonioCaja);
    		if(salidaGuardarPatrimonioCaja.P_OUT_ERRORESCONTADOR > 0) {
    			return false;
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
					GuardarPatrimonioCajaJob apf = new GuardarPatrimonioCajaJob();
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
