package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cl.bcs.plataforma.pau.ValorizarCarteraFuturos;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;


public class PautaValorizarCarteraFuturos implements BcsProcesoPauta{
	
	static final Logger logger = LogManager.getLogger(PautaValorizarCarteraFuturos.class);

	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {		
		logger.info("INICIO::PautaValorizarCarteraFuturos.transaction");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");

			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
			ValorizarCarteraFuturos.ValorizarCarteraFuturosEntrada parametroValorizacionFuturos = new ValorizarCarteraFuturos.ValorizarCarteraFuturosEntrada();
			parametroValorizacionFuturos.p_FechaSistema = confFechaSistema;
			parametroValorizacionFuturos.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			parametroValorizacionFuturos.p_IdPais = empresa.IdPais;
			parametroValorizacionFuturos.p_IdUsuario = (Long)parameters.get("IdUsuario");
			parametroValorizacionFuturos.p_SimularOperacion = "N";
			
			ValorizarCarteraFuturos valorizacionFuturos = new ValorizarCarteraFuturos();
			ValorizarCarteraFuturos.ValorizarCarteraFuturosSalida salidaFuturos = valorizacionFuturos.logica(con, parametroValorizacionFuturos);
			salida.P_OUT_MENSAJE = salidaFuturos.P_OUT_MENSAJE;
			salida.P_OUT_ERRORESCONTADOR = salidaFuturos.P_OUT_ERRORESCONTADOR;
			
			logger.debug(salidaFuturos.P_OUT_MENSAJE);
			logger.debug(salidaFuturos.P_OUT_ERRORESCONTADOR);
			
		}catch(Exception e) {
			logger.debug(e);
			logger.debug("***** Error PautaValorizarCarteraFuturos *****");
		}
		logger.info("FIN::PautaValorizarCarteraFuturos.transaction");
		return salida;
	}
	
	 public static void main(String[] args) throws Throwable {
			try (Connection con = PlataformaUtils.getDESAConnection()){
				PautaValorizarCarteraFuturos ipb = new PautaValorizarCarteraFuturos();
				
			    Map<String, Object> params = new HashMap<String, Object>();
			    params.put("IdEmpresa",1L);
			    params.put("IdUsuario",0L);
				ipb.transaction(con,params );
			    con.rollback();
			  }
			  
		}

}
