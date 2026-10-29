package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;

import cl.bcs.custodia.GenerarCuadraturaCustodiaCircular1518;
import cl.bcs.custodia.GenerarCuadraturaCustodiaCircular1518.EntradaGenerarCuadraturaCustodiaCircular1518;
import cl.bcs.custodia.GenerarCuadraturaCustodiaCircular1518.SalidaGenerarCuadraturaCustodiaCircular1518;
import cl.bcs.plataforma.jobs.BcsProcesoPauta.SalidaProcesoPauta;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Log;

public class PautaGenerarCuadraturaCustodiaCircular1518 implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaGenerarCuadraturaCustodiaCircular1518");

	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		log.write("***** Inicio PautaGenerarCuadraturaCustodiaCircular1518 *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		try {
			
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
			
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			
			EntradaGenerarCuadraturaCustodiaCircular1518 entradaGenerarCuadraturaCustodiaCircular1518 = new EntradaGenerarCuadraturaCustodiaCircular1518();
			entradaGenerarCuadraturaCustodiaCircular1518.p_IdEmpresa =(Long)parameters.get("IdEmpresa");;
			entradaGenerarCuadraturaCustodiaCircular1518.p_IdUsuario =(Long)parameters.get("IdUsuario");;
			entradaGenerarCuadraturaCustodiaCircular1518.p_IdModulo = "CUS";
			entradaGenerarCuadraturaCustodiaCircular1518.p_FechaCuadratura = (String)parameters.get("FechaSistema");
			
			GenerarCuadraturaCustodiaCircular1518 GenerarCuadraturaCustodiaCircular1518 = new GenerarCuadraturaCustodiaCircular1518();
			SalidaGenerarCuadraturaCustodiaCircular1518 salidaProcesarCargaMasiva = GenerarCuadraturaCustodiaCircular1518.logica(con, entradaGenerarCuadraturaCustodiaCircular1518);
			
			salida.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(salidaProcesarCargaMasiva.P_OUT_MENSAJES);
	    	salida.P_OUT_ERRORESCONTADOR = salidaProcesarCargaMasiva.P_OUT_ERRORESCONTADOR;
			
		}catch(Exception e) {
			log.write(e);
    		log.write("***** Error PautaGenearCuadraturaCustodiaCircular1518 *****");
    		throw e;
		}
		log.write("***** Fin PautaGenerarCuadraturaCustodiaCircular1518 *****");
		return salida;
	} 
}
