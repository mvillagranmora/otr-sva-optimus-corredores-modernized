package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cl.bcs.plataforma.pau.InterfazLiquidezIntradia;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

public class PautaInterfazLiquidezIntradia implements BcsProcesoPauta {
	
	static final Logger logger = LogManager.getLogger(PautaInterfazLiquidezIntradia.class.getName());
	
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.info("INICIO::PautaInterfazLiquidezIntradia.transaction");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
			
			InterfazLiquidezIntradia.EntradaInterfazLiquidezIntradia entrada = new InterfazLiquidezIntradia.EntradaInterfazLiquidezIntradia();
			entrada.p_FechaProceso = confFechaSistema;
			entrada.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			entrada.p_IdUsuario = (Long)parameters.get("IdUsuario");
			
			InterfazLiquidezIntradia interfazLiquidezIntradia = new InterfazLiquidezIntradia();
			InterfazLiquidezIntradia.SalidaInterfazLiquidezIntradia out = interfazLiquidezIntradia.logica(con, entrada, false);

			salida.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJE = out.P_OUT_MENSAJES.toString();
			
			logger.info("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			logger.info("Mensajes: " + salida.P_OUT_MENSAJE);

    	} catch (Exception e) {
    		logger.error("ERROR::PautaInterfazLiquidezIntradia.transaction");
    		logger.error(e);
    		throw e;
    	}
		logger.info("FIN::PautaInterfazLiquidezIntradia.transaction");
		return salida;
    }

    public static void main(String[] args) throws Throwable {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2")){
			PautaInterfazLiquidezIntradia ipb = new PautaInterfazLiquidezIntradia();
			
		    Map<String, Object> params = new HashMap<String, Object>();
		    params.put("IdEmpresa", 1L);
		    params.put("IdUsuario", 0L);
			ipb.transaction(con,params );
		    con.commit();
		} catch(Exception e) {
			e.printStackTrace();
		}  
	}
}
