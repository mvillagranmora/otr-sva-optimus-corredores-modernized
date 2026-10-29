package cl.bcs.custodia.jobs;
import java.sql.Connection;

import cl.bcs.plataforma.bat.AgregarPrograma;
import cl.bcs.plataforma.evt.NotificarAdministrador;
import cl.bcs.plataforma.pau.AdelantarPauta;
import cl.bcs.plataforma.pau.AgregarControlPauta;
import cl.bcs.plataforma.pau.ConsultarPautaEjecutar;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.val.TipificarInstrumentos;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;
/**********************************
 * @version 	1.0
 * @author 		jgarces
 **********************************/
public class PautaTipificarInstrumentos extends BcsJob {
	private final Log log = new Log("PAUTA_TIPIFICAR_INSTRUMENTOS"); 
	private final String tipoPrograma = "TIPIFICAR_INSTRUMENTOS";
    @Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
        log.write("****** INICIO  *******");
        Long notificacionIdEmpresa = 1L; 
        String notificacionMensaje="Ejecución del proceso:\n";
        String notificacionEsError="N";
        try{	
        	//--------------------------------------
    		// OBTENCIÓN DE PARÁMETROS
    		//--------------------------------------
        	String mensajeLog = "";
        	String parIdGrupo=super.getParam("IdGrupo"); 
        	Long parIdEmpresa=Long.parseLong(super.getParam("IdEmpresa"));
        	Long parIdUsuario=Long.parseLong(super.getParam("IdUsuario"));
        	Long parIdPais=Long.parseLong(super.getParam("IdPais"));
        	String parSimularOperacion=super.getParam("SimularOperacion");
        	Long parIdPersonaNotificar=(super.getParam("IdPersonaNotificar")=="")?0L:Long.parseLong(super.getParam("IdPersonaNotificar"));
        	String parEmailNotificar=super.getParam("EmailNotificar");
        	Long parIdPrograma= super.getJobId();
        	Long parMaximoProcesar=Long.parseLong(super.getParam("MaximoProcesar"));
        	String parForzarProcesarTodos=super.getParam("ForzarProcesarTodos");
    		String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,parIdEmpresa,"PLATAFORMA","PLA.FechaSistema");
    		String idTipoPrograma = tipoPrograma;
    		String ejecucion = "PROGRAMADA";
    		AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN","Inicia el procesamiento");
    		//--------------------------------------
    		// VERIFICA EJECUCION PROCESO ANTERIOR
    		//--------------------------------------
    		AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN","Verificando requisitos");
    		ConsultarPautaEjecutar pej = new ConsultarPautaEjecutar();
    		ConsultarPautaEjecutar.ConsultarPautaEjecutarEntrada pejin = new ConsultarPautaEjecutar.ConsultarPautaEjecutarEntrada();
    		pejin.p_IdEmpresa = Long.parseLong(super.getParam("IdEmpresa"));
    		pejin.p_IdProgramaActual = parIdPrograma;
    		ConsultarPautaEjecutar.ConsultarPautaEjecutarSalida pejout= pej.logica(con, pejin);
    		if(pejout.P_OUT_ERRORESCONTADOR>0){
    			mensajeLog = "Error al ejecutar: ConsultarPautaEjecutar";
    			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"ERR",mensajeLog);
    			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
    			notificacionEsError="S";
    			throw new InterruptedException(mensajeLog);
    		}
    		if(pejout.P_OUT_PAUTAANTERIOR!=null && !pejout.P_OUT_PAUTAANTERIOR.IdEstadoPrograma.equals("TER")){
    			mensajeLog = "No se ha ejecutado o no ha finalizado proceso de pauta anterior: "+pejout.P_OUT_IDTIPOPROGRAMAANTERIOR;
    			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
    			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
    			notificacionEsError="S";
    			//throw new InterruptedException(mensajeLog);
    			return false;
    		}
    		//--------------------------------------
    		// EJECUTA PROCESO PRINCIPAL
    		//--------------------------------------
    		AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN","Ejecutar Tipificar Instrumentos");
    		TipificarInstrumentos tipins = new TipificarInstrumentos();
    		TipificarInstrumentos.InTipificarInstrumentos intipins = new TipificarInstrumentos.InTipificarInstrumentos();
    		intipins.idEmpresa = parIdEmpresa; 			
    		intipins.idUsuario = parIdUsuario;
    		intipins.idModulo = "PLA";
    		intipins.idPais = parIdPais;
    		intipins.fechaTipificar = confFechaSistema; //java.sql.Date
    		intipins.cantidadTipificar = parMaximoProcesar;
    		TipificarInstrumentos.OutTipificarInstrumentos outtipins = tipins.logica(con, intipins);
    		if(outtipins.erroresContador>0){
    			mensajeLog = "Error al tipificar";
    			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"ERR",mensajeLog);
    			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
    			notificacionEsError="S";
    			throw new InterruptedException(mensajeLog);
    		}
    		mensajeLog="Instrumentos procesados: "+outtipins.instrumentos.size();
    		AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
    		notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
    		//--------------------------------------
    		// VERIFICA PROCESO SIGUIENTE DE PAUTA
    		//--------------------------------------
    		AgregarControlPauta.logPauta(con,log,parIdPrograma, idTipoPrograma, ejecucion, confFechaSistema, "PEN", "Verificando proceso siguiente en Pauta"); 
    		if(pejout.P_OUT_PAUTASIGUIENTE!=null && pejout.P_OUT_PAUTASIGUIENTE.IdPrograma>0){
    			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN","Existe programa para adelantar ejecución: IdProgama="+pejout.P_OUT_PAUTASIGUIENTE.IdPrograma
    					+", Tipo Programa="+pejout.P_OUT_PAUTASIGUIENTE.IdTipoPrograma);
    			if(!pejout.P_OUT_PAUTASIGUIENTE.IdEstadoPrograma.equals("PEN")){
    				AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN","ADVERTENCIA!: Pauta a adelantar no se encuentra pendiente.");
    			}
    			else{//Adelantar ejecución pauta:
    				AdelantarPauta adp = new AdelantarPauta();
    				AdelantarPauta.AdelantarPautaEntrada adpin = new AdelantarPauta.AdelantarPautaEntrada();
    				adpin.p_IdEmpresa = parIdEmpresa;
    				adpin.p_IdUsuario = parIdUsuario;
    				adpin.p_IdModulo = "PLA";
    				adpin.p_IdPrograma = pejout.P_OUT_PAUTASIGUIENTE.IdPrograma;
    				adpin.p_Segundos = new Long(10);
    				AdelantarPauta.AdelantarPautaSalida adpout = adp.logica(con, adpin);
    				if(adpout.P_OUT_ERRORESCONTADOR>0){
    					mensajeLog="ADVERTENCIA!: Error al adelantar pauta: "+pejout.P_OUT_IDTIPOPROGRAMASIGUIENTE;
    					AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
    					notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
    				}
    				else{
    					mensajeLog="Pauta adelantada con éxito: "+pejout.P_OUT_IDTIPOPROGRAMASIGUIENTE;
    					AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
    					notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
    				}
    			}
    		}else if(pejout.P_OUT_IDTIPOPROGRAMASIGUIENTE!=null) {
    			mensajeLog="No hay programa para adelantar ejecución";
    			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
    			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
    			//al ser proceso posterior a cambio fecha, se resta 1 día para visualización en pauta.
        		ObtenerDiaAnterior dant = new ObtenerDiaAnterior();
        		ObtenerDiaAnterior.InObtenerDiaAnterior dantin = new ObtenerDiaAnterior.InObtenerDiaAnterior();
        		dantin.idPais = 44L;
        		dantin.diaHabil = "S";
        		ObtenerDiaAnterior.InObtenerDiaAnterior.FechasIn itemFec = new ObtenerDiaAnterior.InObtenerDiaAnterior.FechasIn();
        		itemFec.fechaInicial = confFechaSistema;
        		itemFec.numeroDias = 1L;
        		dantin.fechas.add(itemFec);
        		ObtenerDiaAnterior.OutObtenerDiaAnterior dantout = dant.logica(con, dantin);
        		String fecSistemaParaPauta=confFechaSistema;
        		if(dantout.erroresContador>0 || dantout.fechas.size()==0){
        			log.write("Error al obtener fecha anterior");
        		}else fecSistemaParaPauta = dantout.fechas.get(0).fechaCalculada;
    			//crear nuevo programa
    			mensajeLog="Creando nuevo programa";
    			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
    			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
    		   	AgregarPrograma agrprg = new AgregarPrograma();
    		   	AgregarPrograma.InAgregarPrograma inagrprg = new AgregarPrograma.InAgregarPrograma();
    		   	inagrprg.p_IdEmpresa = parIdEmpresa;
    		   	inagrprg.p_IdUsuario = parIdUsuario;
    		   	inagrprg.p_IdModulo = "PLA";
    		   	inagrprg.p_FechaSistema = fecSistemaParaPauta;
    		   	inagrprg.p_IdTipoPrograma = pejout.P_OUT_IDTIPOPROGRAMASIGUIENTE;
    		   	inagrprg.p_Segundos = 30L; //delay de 30 segundos.		
    		   	AgregarPrograma.OutAgregarPrograma outagrprg = agrprg.logica(con, inagrprg);
    		   	if(outagrprg.P_OUT_ERRORESCONTADOR>0){
    		   		mensajeLog="ADVERTENCIA!: Error al adelantar pauta: "+pejout.P_OUT_IDTIPOPROGRAMASIGUIENTE;
					AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
					notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
    		   	}else{
    		   		mensajeLog="Pauta adelantada con éxito: "+pejout.P_OUT_IDTIPOPROGRAMASIGUIENTE;
					AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
					notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
    		   	}
    		}else {
    			mensajeLog="No hay tipo programa configurado para adelantar";
    			AgregarControlPauta.logPauta(con,log,parIdPrograma,idTipoPrograma,ejecucion,confFechaSistema,"PEN",mensajeLog);
    			notificacionMensaje=notificacionMensaje+"\n"+mensajeLog;
    		}
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
			try{//Notificar a usuarios TI:
				NotificarAdministrador notiadmin = new NotificarAdministrador();
				NotificarAdministrador.NotificarAdministradorEntrada innotiadmin = new NotificarAdministrador.NotificarAdministradorEntrada();
				innotiadmin.NombreProceso=tipoPrograma;
				innotiadmin.IdEmpresa=notificacionIdEmpresa;
				innotiadmin.EsError=notificacionEsError;
				notificacionMensaje=notificacionMensaje+"\n\n"+"Id Programa: "+super.getJobId();
				innotiadmin.Mensaje=notificacionMensaje.replace("\n", "<br>-");
				log.writeAsJson("Notificación enviada:",notiadmin.logica(con, innotiadmin));	
			}
			catch(Exception e){
				log.write("Excepción controlada al notificar. Continúa ejecución");
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