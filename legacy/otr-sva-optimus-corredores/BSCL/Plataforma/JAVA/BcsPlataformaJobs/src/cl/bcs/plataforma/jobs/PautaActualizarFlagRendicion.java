package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class PautaActualizarFlagRendicion implements BcsProcesoPauta {
	static final Logger logger 	= LogManager.getLogger(PautaActualizarFlagRendicion.class.getName());	
	FileLoader loader 			= new FileLoader();


	private String SQL_UPDATE_FLAG = loader.loadResourceToString("cl/bcs/plataforma/jobs/sql/Pauta_ActualizarFlagRendicion.sql");

    public static class UpdateConfiguracion{
		public String CodigoParametro;
		public String FlagRendicionDefecto;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		logger.info("***** Inicio Reestablecer FlagRendicionPac *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		salida.P_OUT_ERRORESCONTADOR = 1L;
    	try {
    		SQL sql = new SQL();   
        	
        	UpdateConfiguracion updateConfig = new UpdateConfiguracion();
        	updateConfig.CodigoParametro 		= "PLA.FlagEjecucionProcesoRecPAC";
        	updateConfig.FlagRendicionDefecto 	= "N";
        	
    		List<UpdateConfiguracion> params = new ArrayList<>();
    		params.add(updateConfig);
        	
        	sql.batch(con, SQL_UPDATE_FLAG, params, UpdateConfiguracion.class);
			
		} catch (Exception e) {
			salida.P_OUT_ERRORESCONTADOR = 1L;
			salida.P_OUT_MENSAJE = "Error ejecutando el Proceso.";
			logger.info(e);
			logger.info("***** Error Reestablecer FlagRendicionPac *****");
    		throw e;	
		}
    	logger.info("***** Fin Reestablecer FlagRendicionPac *****");
		
		salida.P_OUT_ERRORESCONTADOR = 0L;
		salida.P_OUT_MENSAJE = "Proceso ejecutado correctamente.";		
		return salida;
	}
	
	
	public static void main(String[] args) {
		System.out.println("== START ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("Desa01", "connections.properties")) {
			con.setAutoCommit(false);
			SQL.exec(con, "set schema BSCL");
			SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
			// ---------------------------------
			try {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("IdEmpresa",1L);
				params.put("IdUsuario",1L);
				PautaActualizarFlagRendicion pFlagRendicionDef = new PautaActualizarFlagRendicion();
				System.out.println(pFlagRendicionDef.transaction(con,params));
				
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
