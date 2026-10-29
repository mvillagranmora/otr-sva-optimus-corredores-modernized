package cl.bcs.plataforma.jobs;
import java.sql.Connection;

import javax.sql.DataSource;

import cl.bcs.mesa.MES.CrearInformesCoberturaPatrimonial;
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
public class PautaCoberturaPatrimonio extends BcsJob {
	private final Log log = new Log("PLA.PAUTA_COBERTURA_PATRIMONIO"); 
	private final String tipoPrograma = "PAUTA_COBERTURA_PATRIMONIO";

    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
        log.write("  ****** INICIO  *******");
        Long notificacionIdEmpresa = 1L; 
        String notificacionMensaje="Ejecución del proceso:\n";
        String notificacionEsError="N";
        DataSource datasource = super.getDataSource();
        Long parIdPrograma= super.getJobId();
        try{	
        	//--------------------------------------
    		// OBTENCIÓN DE PARÁMETROS
    		//--------------------------------------
        	String mensajeLog = "";
        	Long parIdEmpresa=Long.parseLong(super.getParam("IdEmpresa"));
        	String codigoUsuario = super.getParam("codigoUsuario");
        	Long parIdUsuario=Long.parseLong(super.getParam("IdUsuario"));
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
    		mensajeLog = "Ejecutar Pauta Cobertura Patrimonio";
    		AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN","Ejecutar Pauta Cobertura Patrimonio");
			
    		CrearInformesCoberturaPatrimonial crearInformesCoberturaPatrimonial = new CrearInformesCoberturaPatrimonial();
    		CrearInformesCoberturaPatrimonial.Entrada entrada = new CrearInformesCoberturaPatrimonial.Entrada();
    		entrada.p_IdEmpresa = parIdEmpresa;
    		entrada.p_Datasource = datasource;
    		entrada.p_Ruta = Utils.getConfiguracion(con, Utils.getSchema(), entrada.p_IdEmpresa, "PLA.RutaBaseArchivos");
    		entrada.p_FechaHoraSistema = confFechaSistema;
    		entrada.p_FechaOperacion = confFechaSistema;
    		entrada.p_IdPais = Long.parseLong(super.getParam("IdPais"));
    		entrada.p_ValorizaAl = confFechaSistema;
    		entrada.p_IdUsuario = parIdUsuario;
    		
    		CrearInformesCoberturaPatrimonial.Salida salidaInforme = crearInformesCoberturaPatrimonial.logica(con, entrada);
    		
    		
    		if(salidaInforme.P_OUT_ERRORESCONTADOR>0){
    			mensajeLog = "Error al generar informe cobertura patrimonial";
    			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"ERR",mensajeLog);
    			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
    			notificacionEsError="S";
    			throw new InterruptedException(mensajeLog);
    		}
    		
    		mensajeLog="Pauta Cobertura Patrimonio ok";
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
    		
    		mensajeLog="Proceso Cobertura Patrimonio terminado";
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