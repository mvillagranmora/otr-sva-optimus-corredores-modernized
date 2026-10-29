package cl.bcs.socionegocio.jobs;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.jobs.BcsProcesoPauta;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.blo.DesbloqueoAutomaticoClientesPorTemporalidad;
import cl.bcs.utils.SQL;
/**********************************
 * @version 	2.0
 * @author 		gat-blac
 **********************************/
public class PautaDesbloqueoAutomaticoClientes implements BcsProcesoPauta {
	static final Logger logger = LogManager.getLogger(PautaDesbloqueoAutomaticoClientes.class.getName());	
	
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		logger.info("***** Inicio PautaDesbloqueoAutomaticoClientes *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		logger.info("DESBLOQUEO POR CONFIGURACION TEMPORALIDAD: ");
    		logger.info("Ejecutar Desbloqueo Automatico de Clientes");
    		DesbloqueoAutomaticoClientesPorTemporalidad dbpt = new DesbloqueoAutomaticoClientesPorTemporalidad();
    		DesbloqueoAutomaticoClientesPorTemporalidad.InDesbloqueoAutomaticoClientesPorTemporalidad indbpt = new DesbloqueoAutomaticoClientesPorTemporalidad.InDesbloqueoAutomaticoClientesPorTemporalidad();
    		indbpt.p_IdEmpresa=(Long)parameters.get("IdEmpresa");
    		indbpt.p_IdModulo="SOC";
    		indbpt.p_IdUsuario=(Long)parameters.get("IdUsuario");
    		indbpt.p_SimularOperacion="N";
    		DesbloqueoAutomaticoClientesPorTemporalidad.OutDesbloqueoAutomaticoClientesPorTemporalidad outdbpt = dbpt.logica(con, indbpt);
			salida.P_OUT_ERRORESCONTADOR = outdbpt.P_OUT_ERRORESCONTADOR;
	
			logger.info("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			logger.info("Mensajes: " + salida.P_OUT_MENSAJE);
    		
    		if(outdbpt.P_OUT_ERRORESCONTADOR>0){
    			salida.P_OUT_MENSAJE = "Error al ejecutar el proceso.";
    			return salida;
    		}else{
    			salida.P_OUT_MENSAJE = "Desbloqueo automatico por temporalidad ejecutado exitosamente. Se desbloquean "+outdbpt.P_OUT_LISTA_CLIENTES.size()+" clientes";
    		}
    		
		} catch (Exception e) {
    		logger.error(e);
    		logger.error("***** Error PautaDesbloqueoAutomaticoClientes *****");
    		throw e;
		}
    	logger.info("***** Fin PautaDesbloqueoAutomaticoClientes *****");
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
				PautaDesbloqueoAutomaticoClientes pDesAutoCli = new PautaDesbloqueoAutomaticoClientes();
				System.out.println(pDesAutoCli.transaction(con,params));
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