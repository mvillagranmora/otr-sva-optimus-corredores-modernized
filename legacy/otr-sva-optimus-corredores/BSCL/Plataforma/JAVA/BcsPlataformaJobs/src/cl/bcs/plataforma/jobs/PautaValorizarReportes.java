package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;

import cl.bcs.plataforma.pau.ValorizarCarteraReportes;
import cl.bcs.plataforma.pau.ValorizarCarteraReportes.ValorizarCarteraReporteEntrada;
import cl.bcs.plataforma.pau.ValorizarCarteraReportes.ValorizarCarteraReporteSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Log;

public class PautaValorizarReportes implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaValorizarReportes");

	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		log.write("***** Inicio PautaValorizarReportes *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
	    	String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
	    	
	    	ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
			ValorizarCarteraReporteEntrada valorizarReportesParametros = new ValorizarCarteraReporteEntrada();
			
			valorizarReportesParametros.p_FechaSistema = confFechaSistema;
			valorizarReportesParametros.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
			valorizarReportesParametros.p_IdPais = empresa.IdPais;
			valorizarReportesParametros.p_IdUsuario =  (Long)parameters.get("IdUsuario");
			valorizarReportesParametros.p_SimularOperacion = "N";
			
			ValorizarCarteraReportes valorizarCarteraReportes = new ValorizarCarteraReportes();
			ValorizarCarteraReporteSalida valorizarCarteraReportesOut = new ValorizarCarteraReporteSalida();
			
			valorizarCarteraReportesOut = valorizarCarteraReportes.logica(con, valorizarReportesParametros);
			salida.P_OUT_MENSAJE = valorizarCarteraReportesOut.P_OUT_MENSAJE;
			salida.P_OUT_ERRORESCONTADOR = valorizarCarteraReportesOut.P_OUT_ERRORESCONTADOR;
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			//log.write("Salida: ");
			//String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			//for (String mensaje: mensajes) log.write(mensaje);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaValorizarReportes *****");
    		throw e;
    	}
		log.write("***** Fin PautaValorizarReportes *****");
		return salida;
	}

}
