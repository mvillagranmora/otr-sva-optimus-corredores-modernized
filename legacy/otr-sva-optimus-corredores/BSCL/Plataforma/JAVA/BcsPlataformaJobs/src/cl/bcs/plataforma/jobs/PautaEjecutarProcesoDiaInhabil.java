package cl.bcs.plataforma.jobs;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.plataforma.pau.EjecutarProcesosDiaInhabil;
import cl.bcs.plataforma.pau.EjecutarProcesosDiaInhabil.EjecutarProcesosSalida;
import cl.bcs.plataforma.pau.utils.PautaUtils;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaEjecutarProcesoDiaInhabil extends BcsJob  implements BcsProcesoPauta {
	private final Log log = new Log("PLA.PautaEjecutarProcesoDiaInhabil");

    @Override
    public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		log.write("***** Inicio PautaEjecutarProcesoDiaInhabil *****");
    	SalidaProcesoPauta salida = new SalidaProcesoPauta();
    	try {
    		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    		
    		// Traer Fecha Sistema
    		String  confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
    		
    		//Dia de ejecucion Pauta
    		Date fechaEnDate=format.parse(confFechaSistema);
    		Calendar calendario = Calendar.getInstance();
    		calendario.setTime(fechaEnDate);
    		
            //Ultimo dia del mes
            calendario.set(Calendar.DAY_OF_MONTH, calendario.getActualMaximum(Calendar.DAY_OF_MONTH));  
            String ultimoDia = format.format(calendario.getTime());
	    	
    		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
    		InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
            
	    	EjecutarProcesosDiaInhabil ejecutarProcesosDiaInhabil = new EjecutarProcesosDiaInhabil();
	    	EjecutarProcesosDiaInhabil.Entrada entrada = new EjecutarProcesosDiaInhabil.Entrada();
			
	    	entrada.ultimoDia = ultimoDia;
	    	entrada.IdEmpresa = (Long)parameters.get("IdEmpresa");
	    	entrada.IdPais = empresa.IdPais;
	    	entrada.IdUsuario = (Long)parameters.get("IdUsuario");
	    	entrada.IdModulo = (String)parameters.get("IdModulo");
	    	Long parIdPrograma= super.getJobId();
			
	    	EjecutarProcesosSalida out = ejecutarProcesosDiaInhabil.logica(con, entrada, false);
	    	
	    	String FechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.IdEmpresa);
	    	
			if (out.P_OUT_ERRORESCONTADOR == 0) {
				salida.P_OUT_MENSAJE = "Se han ejecutado correctamente los procesos del dia inhabil, ";
			} else {
				salida.P_OUT_MENSAJE = "Ha ocurrido un error durante los procesos del dia inhabil";
			}
			salida.P_OUT_ERRORESCONTADOR = out.P_OUT_ERRORESCONTADOR;
			
			PautaUtils.notificarUsuariosTI("CAMBIO_FECHA_SISTEMA", entrada.IdEmpresa, "N", "El proceso CambioFechaSistema se ha ejecutado correctamente "+FechaSistema, parIdPrograma, con, log);
	
			log.write("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			log.write("Salida: ");
			String[] mensajes = salida.P_OUT_MENSAJE.split("\n");
			for (String mensaje: mensajes) log.write(mensaje);
    	} catch (Exception e) {
    		log.write(e);
    		log.write("***** Error PautaEjecutarProcesoDiaInhabil *****");
    		throw e;
    	}
		log.write("***** Fin PautaEjecutarProcesoDiaInhabil *****");
		return salida;		 
    }

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}
    
}
