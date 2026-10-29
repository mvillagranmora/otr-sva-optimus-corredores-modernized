package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cl.bcs.plataforma.pau.ValorizarCarteraOpciones;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;


public class PautaValorizarCarteraOpciones implements BcsProcesoPauta{
	
	static final Logger logger = LogManager.getLogger(PautaValorizarCarteraOpciones.class);

	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {		
		logger.info("INICIO::PautaValorizarCarteraOpciones.transaction");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");

			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
			ValorizarCarteraOpciones.ValorizarCarteraOpcionesEntrada parametroValorizacionOpciones = new ValorizarCarteraOpciones.ValorizarCarteraOpcionesEntrada();
			parametroValorizacionOpciones.p_FechaSistema = confFechaSistema;
			parametroValorizacionOpciones.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			parametroValorizacionOpciones.p_IdPais = empresa.IdPais;
			parametroValorizacionOpciones.p_IdUsuario = (Long)parameters.get("IdUsuario");
			parametroValorizacionOpciones.p_SimularOperacion = "N";
			
			ValorizarCarteraOpciones valorizacionOpciones = new ValorizarCarteraOpciones();
			ValorizarCarteraOpciones.ValorizarCarteraOpcionesSalida salidaOpciones = valorizacionOpciones.logica(con, parametroValorizacionOpciones);
			salida.P_OUT_MENSAJE = salidaOpciones.P_OUT_MENSAJE;
			salida.P_OUT_ERRORESCONTADOR = salidaOpciones.P_OUT_ERRORESCONTADOR;
			
			logger.debug(salidaOpciones.P_OUT_MENSAJE);
			logger.debug(salidaOpciones.P_OUT_ERRORESCONTADOR);
			
		}catch(Exception e) {
			logger.debug(e);
			logger.debug("***** Error PautaValorizarCarteraOpciones *****");
		}
		logger.info("FIN::PautaValorizarCarteraOpciones.transaction");
		return salida;
	}
	
	 public static void main(String[] args) throws Throwable {
			try (Connection con = PlataformaUtils.getDESAConnection()){
				PautaValorizarCarteraOpciones ipb = new PautaValorizarCarteraOpciones();
				
			    Map<String, Object> params = new HashMap<String, Object>();
			    params.put("IdEmpresa",1L);
			    params.put("IdUsuario",0L);
				ipb.transaction(con,params );
			    con.rollback();
			  }
			  
		}

}
