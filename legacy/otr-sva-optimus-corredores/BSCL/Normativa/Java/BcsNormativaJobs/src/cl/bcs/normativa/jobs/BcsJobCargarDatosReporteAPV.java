package cl.bcs.normativa.jobs;

import java.sql.Connection;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import cl.bcs.normativa.CargarDatosReporteAPV;
import cl.bcs.normativa.CargarDatosReporteAPV.EntradaCargarDatosReporteAPV;
import cl.bcs.normativa.CargarDatosReporteAPV.SalidaCargarDatosReporteAPV;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;

public class BcsJobCargarDatosReporteAPV extends BcsJob {
	private static final Logger logger = LogManager.getLogger(BcsJobCargarDatosReporteAPV.class);
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con, false);
	}
	
	public boolean logica(Connection con, boolean debug) throws InterruptedException, Throwable {
		logger.info("INICIO::BcsJobCargarDatosReporteAPV.logica");
		try {
			
			Long idEmpresa = 0L;
    		Long idUsuario = 0L;
    		String fechaCierre = null;
    		if(debug) {
    			idEmpresa = 1L;
                idUsuario = 1L;
                fechaCierre = "2026-04-24";
    		}else {
    			idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
                idUsuario = Long.parseLong(this.getParam("IdUsuario"));
                fechaCierre = this.getParam("FechaCierre");
    		}
    		
    		CargarDatosReporteAPV cargarDatosReporteAPV = new  CargarDatosReporteAPV();
    		EntradaCargarDatosReporteAPV entrada= new EntradaCargarDatosReporteAPV();
    		
    		entrada.p_IdEmpresa = idEmpresa;
    		entrada.p_IdUsuario = idUsuario;
    		entrada.p_IdModulo = "NOR";
    		entrada.p_FechaCierre = fechaCierre;
    		
    		SalidaCargarDatosReporteAPV salida = cargarDatosReporteAPV.logica(con, entrada);
    		
			if (salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
				con.commit();
				logger.info("FIN::BcsJobCargarDatosReporteAPV.logica");
				return true;
			} else {
				logger.info("FIN::BcsJobCargarDatosReporteAPV.logica");
				return false;
			}

		}catch(Exception err) {
			logger.error("***** ERROR BcsJobCargarDatosReporteAPV *****", err);
    		throw err;
		}
	}
	
	
	@Override
    public void onError(Throwable t) {
		// TODO Auto-generated method stub
    }

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}

	public static void main(String[] args) {
		try (Connection con = PlataformaUtils.getPropertiesConnection("qa","connections.properties")) {
			// ---------------------------------
			try {
				BcsJobCargarDatosReporteAPV demo = new BcsJobCargarDatosReporteAPV();
				System.out.println(demo.logica(con,true));
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
