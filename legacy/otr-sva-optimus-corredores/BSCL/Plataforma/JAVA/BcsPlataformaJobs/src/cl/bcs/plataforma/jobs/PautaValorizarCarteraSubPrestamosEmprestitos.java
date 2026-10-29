package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import cl.bcs.plataforma.pau.ValorizarCarteraSubPrestEmpres;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

public class PautaValorizarCarteraSubPrestamosEmprestitos implements BcsProcesoPauta {
	
	static final Logger logger = LogManager.getLogger(PautaValorizarCarteraSubPrestamosEmprestitos.class.getName());
	
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.info("INICIO::PautaValorizarCarteraSubPrestamosEmprestitos.transaction");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
			
	    	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
			ValorizarCarteraSubPrestEmpres.ValorizarCarteraSubPrestEmpresEntrada parametrosValorizar = new ValorizarCarteraSubPrestEmpres.ValorizarCarteraSubPrestEmpresEntrada();
			parametrosValorizar.p_FechaSistema = confFechaSistema;
			parametrosValorizar.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			parametrosValorizar.p_IdPais = empresa.IdPais;
			parametrosValorizar.p_IdUsuario = (Long)parameters.get("IdUsuario");
			parametrosValorizar.p_SimularOperacion = "N";
			parametrosValorizar.p_EsPauta = "S";
			
			ValorizarCarteraSubPrestEmpres valorizar = new ValorizarCarteraSubPrestEmpres();
			ValorizarCarteraSubPrestEmpres.ValorizarCarteraSubPrestEmpresSalida valorizarOut = new ValorizarCarteraSubPrestEmpres.ValorizarCarteraSubPrestEmpresSalida();
			valorizarOut = valorizar.logica(con, parametrosValorizar);
			salida.P_OUT_MENSAJE = valorizarOut.P_OUT_MENSAJE;
			if(salida.P_OUT_MENSAJE!=null && salida.P_OUT_MENSAJE.toLowerCase().contains("error")) {
				salida.P_OUT_ERRORESCONTADOR = 1L;
			}else {
				salida.P_OUT_ERRORESCONTADOR = valorizarOut.P_OUT_ERRORESCONTADOR;
			}
	
			logger.info("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			logger.info("Mensajes: " + salida.P_OUT_MENSAJE);

    	} catch (Exception e) {
    		logger.error("ERROR::PautaValorizarCarteraSubPrestamosEmprestitos.transaction");
    		logger.error(e);
    		throw e;
    	}
		logger.info("FIN::PautaValorizarCarteraSubPrestamosEmprestitos.transaction");
		return salida;
    }

    public static void main(String[] args) throws Throwable {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa2")){
			PautaValorizarCarteraSubPrestamosEmprestitos ipb = new PautaValorizarCarteraSubPrestamosEmprestitos();
			
		    Map<String, Object> params = new HashMap<String, Object>();
		    params.put("IdEmpresa", 1L);
		    params.put("IdUsuario", 0L);
			ipb.transaction(con,params );
		    con.commit();
		  }
		catch(Exception e) {
			e.printStackTrace();
		}
		  
	}
}