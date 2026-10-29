package cl.bcs.plataforma.jobs;
import java.sql.Connection;


import cl.bcs.plataforma.pau.AgregarControlPauta;
import cl.bcs.plataforma.pau.ConsultarPautaEjecutar;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pau.utils.PautaUtils;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaFacturacionMensual extends BcsJob {
	private final Log log = new Log("PLA.PAUTA_FACTURACION_MENSUAL"); 
	private final String tipoPrograma = "PAUTA_FACTURACION_MENSUAL";

    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
        log.write("  ****** INICIO  *******");
        Long notificacionIdEmpresa = 1L; 
        String notificacionMensaje="Ejecución del proceso:\n";
        String notificacionEsError="N";
        Long parIdPrograma= super.getJobId();
        try{	
        	//--------------------------------------
    		// OBTENCIÓN DE PARÁMETROS
    		//--------------------------------------
        	String mensajeLog = "";
        	Long parIdEmpresa=Long.parseLong(super.getParam("IdEmpresa"));
        	String codigoUsuario = super.getParam("codigoUsuario");
        	Long parIdUsuario=Long.parseLong(super.getParam("IdUsuario"));
        	Long parIdPais=Long.parseLong(super.getParam("IdPais"));
            if (codigoUsuario == null) codigoUsuario = "SISTEMA";
            notificacionIdEmpresa = parIdEmpresa;
            
    		String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,parIdEmpresa,"PLATAFORMA","PLA.FechaSistema");
    		String idTipoPrograma = tipoPrograma;
    		String ejecucion = "PROGRAMADA";
    		
    		AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN","Inicia el procesamiento");
    		//--------------------------------------
    		// BUSCA SI EXISTE PROCESO ANTERIOR Y POSTERIOR
    		//--------------------------------------
    		AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN","Verificando requisitos");
    		ConsultarPautaEjecutar pej = new ConsultarPautaEjecutar();
    		ConsultarPautaEjecutar.ConsultarPautaEjecutarEntrada pejin = new ConsultarPautaEjecutar.ConsultarPautaEjecutarEntrada();
    		pejin.p_IdEmpresa = parIdEmpresa;
    		pejin.p_IdProgramaActual = parIdPrograma;
    		ConsultarPautaEjecutar.ConsultarPautaEjecutarSalida pejout= pej.logica(con, pejin);
    		
    		//--------------------------------------
    		// VERIFICA EJECUCION PROCESO ANTERIOR
    		//--------------------------------------
    		PautaUtils.verificarProcesoAnterior(pejout, mensajeLog, con, log, parIdPrograma, idTipoPrograma, ejecucion, confFechaSistema, notificacionMensaje, notificacionEsError);
    		
    		//--------------------------------------
    		// EJECUTA PROCESO PRINCIPAL, INCLUIR LOGICA DE NEGOCIO DE CADA PROCESO
    		//--------------------------------------
    		mensajeLog = "Ejecutar Pauta Facturacion Mensual";
    		AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN","Ejecutar Pauta Facturacion Mensual");
			
    		//Job dummy, negocio aún no implementa lógica
    		
    		mensajeLog="Pauta Facturacion Mensual ok";
    		AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
    		notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
    		//--------------------------------------
    		// FIN EJECUTA PROCESO PRINCIPAL
    		//--------------------------------------
    		
    		//--------------------------------------
    		// SE REGISTRA EJECUCIÓN DE PROCESO EN LOGS DE PROCESOS
    		//--------------------------------------
    		PautaUtils.agregarLogProcesosPauta(tipoPrograma, codigoUsuario, confFechaSistema, parIdEmpresa, mensajeLog, con, idTipoPrograma, ejecucion, log, parIdPrograma, notificacionMensaje, notificacionEsError);
    		
    		//--------------------------------------
    		// VERIFICA PROCESO SIGUIENTE DE PAUTA
    		//--------------------------------------
    		PautaUtils.verificarProcesoPosterior(pejout, mensajeLog, con, log, parIdPrograma, idTipoPrograma, ejecucion, confFechaSistema, notificacionMensaje, notificacionEsError, codigoUsuario, parIdEmpresa, parIdUsuario);
    		
    		mensajeLog="Proceso terminado.";
			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"TER",mensajeLog);
			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
			return true;
        }
	    catch (Exception e) {
			log.write("****** ERROR *******");
			log.write(Utils.stackTraceToString(e));
			notificacionMensaje=notificacionMensaje+"\nExcepción: "+Utils.stackTraceToString(e);
			e.printStackTrace();
			notificacionEsError="S";
			throw e;
		} finally {
			try{
				PautaUtils.notificarUsuariosTI(notificacionEsError, notificacionIdEmpresa, notificacionEsError, notificacionMensaje, parIdPrograma, con, log);
			}
			catch(Exception e){
				log.write("Excepción controlada al notificar usuarios TI. Continúa ejecución");
			}
			log.write("****** FIN TAREA *******");
		}   
    }

    @Override
    public void onError(Throwable t) {
        log.write("****** ERROR  *******");
        log.write(t);
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
}