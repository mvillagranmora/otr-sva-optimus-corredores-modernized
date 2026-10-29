package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import cl.bcs.plataforma.pau.EjecutarProcesosDiaInhabil;
import cl.bcs.plataforma.pau.ValorizarCuponFeriado;
import cl.bcs.plataforma.pau.ValorizarCuponFeriado.ValorizarCuponFeriadoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.Log;

public class PautaValorizarCuponFeriado implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaEjecutarValorizacionCuponFeriado");

    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaEjecutarValorizacionCuponFeriado *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    		
    		// Traer Fecha Sistema
    		String  confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
    		
    		//Dia de ejecucion Pauta
    		Date fechaEnDate=format.parse(confFechaSistema);
    		Calendar calendario = Calendar.getInstance();
    		calendario.setTime(fechaEnDate);
    		
    		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
    		InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
            
    		ValorizarCuponFeriado ejecutarValorizadorCuponFeriado = new ValorizarCuponFeriado();
	    	ValorizarCuponFeriado.ValorizarCuponFeriadoEntrada entrada = new ValorizarCuponFeriado.ValorizarCuponFeriadoEntrada();
			
	    	entrada.DiaHoy = confFechaSistema;
	    	entrada.IdEmpresa = (Long)parameters.get("IdEmpresa");
	    	entrada.IdPais = empresa.IdPais;
	    	entrada.IdUsuario = (Long)parameters.get("IdUsuario");
	    	entrada.IdModulo = (String)parameters.get("IdModulo");
			
	    	ValorizarCuponFeriadoSalida out = ejecutarValorizadorCuponFeriado.logica(con, entrada, false);
			if (out.P_OUT_ERRORESCONTADOR == 0) {
				salida.P_OUT_MENSAJE = "Se han valorizado correctamente las carteras RF con sus cupones";
			} else {
				salida.P_OUT_MENSAJE = "Ha ocurrido un error durante el proceso de valorizacion de carteras RF";
			}
			salida.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR;
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			log.write("Salida: ");
			String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			for (String mensaje: mensajes) log.write(mensaje);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaEjecutarValorizacionCuponFeriado *****");
    		throw e;
    	}
		log.write("***** Fin PautaEjecutarValorizacionCuponFeriado *****");
		return salida;		 
    }
}
