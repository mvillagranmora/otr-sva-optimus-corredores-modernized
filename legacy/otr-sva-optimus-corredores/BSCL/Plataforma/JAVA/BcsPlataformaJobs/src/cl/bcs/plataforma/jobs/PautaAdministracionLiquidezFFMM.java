package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.util.Map;

import cl.bcs.fondomutuo.AdministracionLiquidez;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Log;

public class PautaAdministracionLiquidezFFMM implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaAdministracionLiquidezFFMM");
	
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		log.write("***** Inicio PautaAdministracionLiquidezFFMM *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		AdministracionLiquidez.EntradaAdministracionLiquidez parametros = new AdministracionLiquidez.EntradaAdministracionLiquidez();
    		parametros.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
    		parametros.p_IdUsuario = (Long)parameters.get("IdUsuario");
    		parametros.p_IdModulo = "FMM";
    		parametros.p_SimularOperacion = "N";
    		parametros.p_UsaReglasNegocio = "S";
    		
    		AdministracionLiquidez administracionLiquidez = new AdministracionLiquidez();
    		AdministracionLiquidez.SalidaAdministracionLiquidez salidaAdministracionLiquidezFFMM = administracionLiquidez.logica(con, parametros);

    		salida.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(salidaAdministracionLiquidezFFMM.P_OUT_MENSAJES);
    		salida.P_OUT_ERRORESCONTADOR = salidaAdministracionLiquidezFFMM.P_OUT_ERRORESCONTADOR;

    		log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			//log.write("Salida: ");
			//String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			//for (String mensaje: mensajes) log.write(mensaje);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaAdministracionLiquidezFFMM *****");
    		throw e;
    	}
    	log.write("***** Fin PautaAdministracionLiquidezFFMM *****");
		return salida;
	}

}
