package cl.bcs.plataforma.jobs;
import java.sql.Connection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.OutAgregarPrograma;
import cl.bcs.plataforma.cre.CambiarEstadoModulo;
import cl.bcs.plataforma.evt.NotificarAdministrador;
import cl.bcs.plataforma.pau.AdelantarPauta;
import cl.bcs.plataforma.pau.AdelantarPauta.AdelantarPautaEntrada.Parametro;
import cl.bcs.plataforma.pau.utils.PautaUtils;
import cl.bcs.plataforma.pau.AgregarControlPauta;
import cl.bcs.plataforma.pau.ConsultarPautaEjecutar;
import cl.bcs.plataforma.pla.CambiarFechaSistema;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pau.EntregarSistemaManual;
import cl.bcs.plataforma.pau.ActualizarEstadoSistema;
import cl.bcs.plataforma.pau.AgregarControlProceso;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
/**********************************
 * @version 	1.0
 * @author 		gat-blac
 **********************************/
public class PautaEntregarSistema extends BcsJob {
	private final Log log = new Log("PAUTA_ENTREGAR_SISTEMA"); 
	private final String tipoPrograma = "ENTREGAR_SISTEMA";

    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
        log.write("  ****** INICIO  *******");
        Long notificacionIdEmpresa = 1L; 
        String notificacionMensaje="EjecuciÃ³n del proceso:\n";
        String notificacionEsError="N";
        Long parIdPrograma= super.getJobId();
        try{	
        	//--------------------------------------
    		// OBTENCIÃ“N DE PARÃ�METROS
    		//--------------------------------------
        	ConfiguracionSistema.limpiarParametros();
        	String mensajeLog = "";
        	Long parIdEmpresa=Long.parseLong(super.getParam("IdEmpresa"));
        	notificacionIdEmpresa = parIdEmpresa;
        	Long parIdUsuario=Long.parseLong(super.getParam("IdUsuario"));
        	String codigoUsuario = super.getParam("codigoUsuario");
            if (codigoUsuario == null) codigoUsuario = "SISTEMA";
           
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
    		pejin.p_IdEmpresa = Long.parseLong(super.getParam("IdEmpresa"));
    		pejin.p_IdProgramaActual = parIdPrograma;
    		ConsultarPautaEjecutar.ConsultarPautaEjecutarSalida pejout= pej.logica(con, pejin);
    		
    		//--------------------------------------
    		// VERIFICA EJECUCION PROCESO ANTERIOR
    		//--------------------------------------
    		PautaUtils.verificarProcesoAnterior(pejout, mensajeLog, con, log, parIdPrograma, idTipoPrograma, ejecucion, confFechaSistema, notificacionMensaje, notificacionEsError);
    		
    		//--------------------------------------
    		// EJECUTA PROCESO PRINCIPAL, INCLUIR LOGICA DE NEGOCIO DE CADA PROCESO
    		//--------------------------------------
    		mensajeLog = "Entregar Sistema";
    		EntregarSistemaManual entregarSistema = new EntregarSistemaManual();
    		
    		ActualizarEstadoSistema actualizarEstadoSistema = new ActualizarEstadoSistema();
    		ActualizarEstadoSistema.EstadoSistemaEntrada estadoSistemaEntrada = new ActualizarEstadoSistema.EstadoSistemaEntrada();
    		estadoSistemaEntrada.IdEmpresa = parIdEmpresa;
    		estadoSistemaEntrada.IdEstado = "ENT";
    		estadoSistemaEntrada.CodigoUsuario = codigoUsuario;
    		ActualizarEstadoSistema.EstadoSistemaSalida respuestaActualizarSistema = actualizarEstadoSistema.logica(con, estadoSistemaEntrada);
    		if (respuestaActualizarSistema.P_OUT_ERRORESCONTADOR > 0){
    			mensajeLog = "Error al actualizar estado del sistema: "+(respuestaActualizarSistema.P_OUT_MENSAJE);
    			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"ERR",mensajeLog);
    			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
    			notificacionEsError="S";
    			throw new InterruptedException(mensajeLog);
    		} else {
    			mensajeLog = "Sistema Entregado, se adelantarÃ¡ el siguiente proceso";
    			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
    		}
    		
    		AgregarMensajeWebsocket agregarMensajeWebsocket = new AgregarMensajeWebsocket();
    		String mensajeWebSocket = "Sistema entregado por usuario " + codigoUsuario;
    		JsonParser parser = new JsonParser();                
            JsonObject dataJsonIn = parser.parse("{\"IdEmpresa\":\"" + parIdEmpresa +"\",\"p_IdUsuario\":\""+parIdUsuario+"\",\"p_IdModulo\":'PLA',\"p_Mensajes\":[{\"IdEmpresa\":\""+parIdEmpresa+"\",\"Evento\":'AVISO',\"DATA\":\""+mensajeWebSocket+"\"}]} ").getAsJsonObject();
    		System.out.println(dataJsonIn);
            try{
    			agregarMensajeWebsocket.call(con, dataJsonIn);
    			mensajeLog = "Notificacion de entrega de sistema enviada OK";
    			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
    		} catch (Exception ex){
    			mensajeLog = "Error al enviar notificacion de entrega de sistema al usuario: "+(respuestaActualizarSistema.P_OUT_MENSAJE);
    			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"ERR",mensajeLog);
    			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
    			notificacionEsError="S";
    			throw new InterruptedException(mensajeLog);
    		}
            //--------------------------------------
    		// FIN EJECUTA PROCESO PRINCIPAL
    		//--------------------------------------
    		
    		//--------------------------------------
    		// SE REGISTRA EJECUCIÃ“N DE PROCESO EN LOGS DE PROCESOS
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
			notificacionMensaje=notificacionMensaje+"\nExcepciÃ³n: "+Utils.stackTraceToString(e);
			e.printStackTrace();
			notificacionEsError="S";
			throw e;
		} finally {
			try{
				PautaUtils.notificarUsuariosTI(notificacionEsError, notificacionIdEmpresa, notificacionEsError, notificacionMensaje, parIdPrograma, con, log);	
			}
			catch(Exception e){
				log.write("ExcepciÃ³n controlada al notificar. ContinÃºa ejecuciÃ³n");
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