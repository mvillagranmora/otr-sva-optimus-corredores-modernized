package cl.bcs.socionegocio.jobs;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.jobs.BcsProcesoPauta;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.blo.DesbloqueoAutomaticoPortafolioPorTemporalidad;
import cl.bcs.socionegocio.blo.BloqueoAutomaticoPortafolioPorTemporalidad;
import cl.bcs.utils.SQL;
/**********************************
 * @version 	2.0
 * @author 		gat-blac
 **********************************/
public class PautaBloqueoDesbloqueoAutomaticoPortafolio implements BcsProcesoPauta {
	static final Logger logger = LogManager.getLogger(PautaBloqueoDesbloqueoAutomaticoPortafolio.class.getName());	
	
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		logger.info("***** Inicio PautaBloqueoDesbloqueoAutomaticoPortafolio *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		
    		boolean error = false;
    		
    		logger.info("DESBLOQUEO POR CONFIGURACION TEMPORALIDAD: ");
    		logger.info("Ejecutar Desbloqueo Automatico de Portafolio");
    		
    		DesbloqueoAutomaticoPortafolioPorTemporalidad dbpt = new DesbloqueoAutomaticoPortafolioPorTemporalidad();
    		DesbloqueoAutomaticoPortafolioPorTemporalidad.InDesbloqueoAutomaticoPortafoliosPorTemporalidad indbpt = new DesbloqueoAutomaticoPortafolioPorTemporalidad.InDesbloqueoAutomaticoPortafoliosPorTemporalidad();
    		
    		indbpt.p_IdEmpresa=(Long)parameters.get("IdEmpresa");
    		indbpt.p_IdModulo="SOC";
    		indbpt.p_IdUsuario=(Long)parameters.get("IdUsuario");
    		indbpt.p_SimularOperacion="N";
    		
    		DesbloqueoAutomaticoPortafolioPorTemporalidad.OutDesbloqueoAutomaticoPortafoliosPorTemporalidad outdbpt = dbpt.logica(con, indbpt);
			   		
    		if (outdbpt.P_OUT_ERRORESCONTADOR > 0) {
				error = true;
				logger.error("Error en desbloqueo automático.");
			} else {
				logger.info("Desbloqueo automático ejecutado correctamente.");
				logger.info("Portafolios desbloqueados: " + outdbpt.P_OUT_LISTA_PORTAFOLIO.size());
			}
    		
    		logger.info("BLOQUEO POR CONFIGURACION TEMPORALIDAD: ");
    		logger.info("Ejecutar Bloqueo Automatico de Portafolio");

    		BloqueoAutomaticoPortafolioPorTemporalidad bappt = new BloqueoAutomaticoPortafolioPorTemporalidad();
    		BloqueoAutomaticoPortafolioPorTemporalidad.InBloqueoAutomaticoPortafoliosPorTemporalidad inbappt = new BloqueoAutomaticoPortafolioPorTemporalidad.InBloqueoAutomaticoPortafoliosPorTemporalidad();
    		        
    		inbappt.p_IdEmpresa = (Long) parameters.get("IdEmpresa");
    		inbappt.p_IdModulo = "SOC";
    		inbappt.p_IdUsuario = (Long) parameters.get("IdUsuario");
    		inbappt.p_SimularOperacion = "N";

    		BloqueoAutomaticoPortafolioPorTemporalidad.OutBloqueoAutomaticoPortafoliosPorTemporalidad outbappt = bappt.logica(con, inbappt);
    		        
    		// Manejo de errores
    		if (outbappt.P_OUT_ERRORESCONTADOR > 0) {
    			error = true;
    			logger.error("Error en bloqueo automático.");	 
    		} else {
    		    logger.info("Bloqueo automático ejecutado correctamente.");
    		    logger.info("Portafolios bloqueados: " + outbappt.P_OUT_LISTA_PORTAFOLIO.size());
    		}

    		salida.P_OUT_ERRORESCONTADOR = outbappt.P_OUT_ERRORESCONTADOR + outdbpt.P_OUT_ERRORESCONTADOR;
		
    		if (error) {
				salida.P_OUT_MENSAJE = "Proceso ejecutado con errores.";
			} else {
				salida.P_OUT_MENSAJE = "Proceso OK. "
						+ "Bloqueados: " + outbappt.P_OUT_LISTA_PORTAFOLIO.size()
						+ ", Desbloqueados: " + outdbpt.P_OUT_LISTA_PORTAFOLIO.size();
			}
    		
		} catch (Exception e) {
    		logger.error(e);
    		logger.error("***** Error PautaBloqueoDesbloqueoAutomaticoPortafolio *****");
    		throw e;
		}
    	logger.info("***** Fin PautaBloqueoDesbloqueoAutomaticoPortafolio *****");
		return salida;
	}
	public static void main(String[] args) {
		System.out.println("== START ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			con.setAutoCommit(false);
			SQL.exec(con, "set schema BSCL");
			SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
			// ---------------------------------
			try {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("IdEmpresa",1L);
				params.put("IdUsuario",1L);
				PautaBloqueoDesbloqueoAutomaticoPortafolio proceso = new PautaBloqueoDesbloqueoAutomaticoPortafolio();
				System.out.println(proceso.transaction(con,params));
				con.rollback();
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