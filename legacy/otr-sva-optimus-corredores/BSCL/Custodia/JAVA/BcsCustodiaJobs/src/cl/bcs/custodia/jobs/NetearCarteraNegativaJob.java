package cl.bcs.custodia.jobs;

import java.sql.Connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import cl.bcs.custodia.NetearCustodiaNegativa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Utils;

public class NetearCarteraNegativaJob extends BcsJob {
	
	private static final Logger logger = LogManager.getLogger(NetearCarteraNegativaJob.class);
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,false);
	}
	
	public boolean logica(Connection con, boolean debug) throws InterruptedException, Throwable {
		logger.info("INICIO::NetearCarteraNegativaJob");
		
		NetearCustodiaNegativa.EntradaNetearCustodiaNegativa parametroNetearCustodiaNegativa = debug ? obtenerParametrosNetearCarteraNegativaJobDebug() : obtenerParametrosNetearCarteraNegativaJob();
		
		NetearCustodiaNegativa.SalidaNetearCustodiaNegativa salidaNetearCustodiaNegativa = netearCarteras(con, parametroNetearCustodiaNegativa);
		
		if(salidaNetearCustodiaNegativa.P_OUT_ERRORESCONTADOR > 0) {
			throw new Exception(salidaNetearCustodiaNegativa.P_OUT_MENSAJES.get(0).codigo);
		}
		
		logger.info("FIN::NetearCarteraNegativaJob");
		return true;
	}
	
	private NetearCustodiaNegativa.EntradaNetearCustodiaNegativa obtenerParametrosNetearCarteraNegativaJobDebug() {
		logger.trace("INICIO::NetearCarteraNegativaJob.obtenerParametrosNetearCarteraNegativaJobDebug");
		Gson gson = Utils.getGsonBuilder();
		NetearCustodiaNegativa.EntradaNetearCustodiaNegativa parametros = gson.fromJson("{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"FAC\",\"p_UsaReglasNegocio\":\"S\",\"p_SimularOperacion\":\"N\",\"p_PortafoliosInstrumento\":[{\"IdPortafolio\":308818,\"IdInstrumento\":2223}]}", NetearCustodiaNegativa.EntradaNetearCustodiaNegativa.class);
		logger.trace("FIN::NetearCarteraNegativaJob.obtenerParametrosNetearCarteraNegativaJobDebug");
		return parametros;
	}
	
	private NetearCustodiaNegativa.EntradaNetearCustodiaNegativa obtenerParametrosNetearCarteraNegativaJob() {
		logger.trace("INICIO::NetearCarteraNegativaJob.obtenerParametrosNetearCarteraNegativaJob");
		Gson gson = Utils.getGsonBuilder();
		NetearCustodiaNegativa.EntradaNetearCustodiaNegativa parametros = gson.fromJson(this.getParam("Entrada"), NetearCustodiaNegativa.EntradaNetearCustodiaNegativa.class);
		logger.trace("FIN::NetearCarteraNegativaJob.obtenerParametrosNetearCarteraNegativaJob");
		return parametros;
	}
	
	private NetearCustodiaNegativa.SalidaNetearCustodiaNegativa netearCarteras(Connection con, NetearCustodiaNegativa.EntradaNetearCustodiaNegativa parametroNetearCustodiaNegativa) throws Exception {
		logger.trace("INICIO::NetearCarteraNegativaJob.netearCarteras");
		NetearCustodiaNegativa netearCustodiaNegativa = new NetearCustodiaNegativa();
		NetearCustodiaNegativa.SalidaNetearCustodiaNegativa salidaNetearCustodiaNegativa = netearCustodiaNegativa.logica(con, parametroNetearCustodiaNegativa);
		logger.trace("FIN::NetearCarteraNegativaJob.netearCarteras");
		return salidaNetearCustodiaNegativa;
	}
	
	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String [] args) throws InterruptedException, Throwable {
		Connection c =  PlataformaUtils.getPropertiesConnection("desa","connections.properties");
		NetearCarteraNegativaJob a = new NetearCarteraNegativaJob();
		a.logica(c, true);
		c.rollback();
		
	}
	
}
