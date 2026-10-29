package cl.bcs.custodia.jobs;

import java.sql.Connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import cl.bcs.custodia.GuardarPatrimonioRetroactivo;
import cl.bcs.custodia.GuardarPatrimonioRetroactivo.EntradaGuardarPatrimonioRetroactivo;
import cl.bcs.custodia.GuardarPatrimonioRetroactivo.SalidaGuardarPatrimonioRetroactivo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;

public class GuardarPatrimonioConsolidadoJob extends BcsJob{
	
	static final Logger logger = LogManager.getLogger(GuardarPatrimonioConsolidadoJob.class);
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	
	public boolean logica(Connection con,boolean debug) throws InterruptedException, Throwable {
		logger.info("***** Inicio GuardarPatrimonioConsolidadoJob *****");
    	try {
    		Long idEmpresa = 0L;
    		Long idUsuario = 0L;
    		String fechaInicio = null;
    		String fechaTermino = null;
    		if(debug) {
    			idEmpresa = 1L;
                idUsuario = 1L;
                fechaInicio = "2019-07-31";
                fechaTermino = "2019-07-31";
    		}else {
    			idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
                idUsuario = Long.parseLong(this.getParam("IdUsuario"));
                fechaInicio = this.getParam("FechaInicio");
                fechaTermino = this.getParam("FechaTermino");
    		}
    		
            if(fechaInicio == null || fechaInicio.isEmpty() || fechaTermino == null || fechaTermino.isEmpty()) {
            	fechaInicio = ConfiguracionSistema.getParametroConfiguracionString(con,idEmpresa,"PLATAFORMA","PLA.FechaSistema");
            	fechaTermino = fechaInicio;
            	
            }
    		
    		/// RETROACTIVO
    		
    		GuardarPatrimonioRetroactivo guardarPatrimonioRetroactivo = new GuardarPatrimonioRetroactivo();
    		EntradaGuardarPatrimonioRetroactivo entradaGuardarPatrimonioRetroactivo = new EntradaGuardarPatrimonioRetroactivo();
    		
    		entradaGuardarPatrimonioRetroactivo.p_IdEmpresa = idEmpresa;
    		entradaGuardarPatrimonioRetroactivo.p_IdUsuario = idUsuario;
    		entradaGuardarPatrimonioRetroactivo.p_IdModulo = "PLA";
    		entradaGuardarPatrimonioRetroactivo.p_FechaInicio = fechaInicio;
    		entradaGuardarPatrimonioRetroactivo.p_FechaTermino = fechaTermino;
    		
    		SalidaGuardarPatrimonioRetroactivo salidaGuardarPatrimonioRetroactivo = guardarPatrimonioRetroactivo.logica(con, entradaGuardarPatrimonioRetroactivo);
    		
    		
    		
    		if(salidaGuardarPatrimonioRetroactivo.P_OUT_ERRORESCONTADOR > 0) {
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
					GuardarPatrimonioConsolidadoJob apf = new GuardarPatrimonioConsolidadoJob();
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
