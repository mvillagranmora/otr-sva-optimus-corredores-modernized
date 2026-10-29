package cl.bcs.custodia.jobs;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.custodia.ConsultarEjercicioBloqueo;
import cl.bcs.custodia.ConsultarEjercicioBloqueo.EntradaConsultarEjercicioBloqueo;
import cl.bcs.custodia.ConsultarEjercicioBloqueo.Horario;
import cl.bcs.custodia.ConsultarEjercicioBloqueo.SalidaConsultarEjercicioBloqueo;
import cl.bcs.plataforma.evt.AgregarEvento;
import cl.bcs.plataforma.evt.AgregarEvento.ObjEVENTOS;
import cl.bcs.plataforma.evt.AgregarEvento.ObjIDDIRECCIONES;
import cl.bcs.plataforma.evt.AgregarEvento.ServiceEntrada;
import cl.bcs.plataforma.evt.NotificarAdministrador;
import cl.bcs.plataforma.pla.AgregarMensajeWebsocket;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
/**********************************
 * @version 	1.0
 * @author 		projo
 **********************************/
public class ValidarHorarioEjercicioBloqueoOpciones extends BcsJob {
	private static final Logger LOGGER = LogManager.getLogger(ValidarHorarioEjercicioBloqueoOpciones.class); 
	private static final String TIPO_PROGRAMA_INICIO 	= "AVISO_HORA_INICIO_EJER_BLOQUEO_OPCIONES";
	private static final String TIPO_PROGRAMA_TERMINO 	= "AVISO_HORA_TERMINO_EJER_BLOQUEO_OPCIONES";
	private final FileLoader fileLoader = new FileLoader();
	private final String SQL_CONSULTA_ANTERIOR = fileLoader.loadResourceToString("cl/bcs/custodia/jobs/sql/ValidarHorarioEjercicioBloqueoOpciones_SelectConsultaAnterior.sql");
	private final String SQL_CONSULTA_INFO_NOTIFICACION = fileLoader.loadResourceToString("cl/bcs/custodia/jobs/sql/ValidarHorarioEjercicioBloqueoOpciones_SelectInfoNotificacion.sql");
	
	public static class MensajeWebSocket{
		public Long IdEmpresa;
		public Date FechaRegistro;
		public String Evento;
		public String Data;
		public String EsProcesado;
		
		public MensajeWebSocket() {
			super();
		}			
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class InfoNotificacion{
		public Long IdPersona;//Ejecutivo asociado
		public String Producto;
		public String Supervisor;
		public String Agente;
		public String NombreCorredora;
		public Long IdDireccionElectronica;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	@Override
    public boolean transaction(Connection con) throws InterruptedException, Throwable {
    	LOGGER.info("****** INICIO  HORARIO_EJERCICIO_BLOQUEO_OPCIONES *******");
    	Long parIdEmpresa = 1L;
        String notificacionMensaje="Ejecucion del proceso:\n";
        String notificacionEsError="N";
        HashMap<String, String> hash = new HashMap<String, String>();
        List<MensajeWebSocket> resConsulta = new ArrayList<ValidarHorarioEjercicioBloqueoOpciones.MensajeWebSocket>();
        List<InfoNotificacion> resNotif = new ArrayList<ValidarHorarioEjercicioBloqueoOpciones.InfoNotificacion>();
        List<String> tipoPrograma = new ArrayList<String>();
        ServiceEntrada notifIn = new ServiceEntrada();
        SQL _sql = new SQL();
        try{	
        	//--------------------------------------
    		// OBTENCION DE PARAMETROS
    		//--------------------------------------
        	ConsultarEjercicioBloqueo cons = new ConsultarEjercicioBloqueo();
            //String parIdGrupo = super.getParam("IdGrupo");
            parIdEmpresa = Long.parseLong(super.getParam("IdEmpresa"));//1L;
            Long parIdUsuario = Long.parseLong(super.getParam("IdUsuario"));//9L;
            String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, parIdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
            String logo = ConfiguracionSistema.getParametroConfiguracionString(con, parIdEmpresa, "PLATAFORMA", "PLA.UrlLogoCorredoraPublic");
            String horaIniStr = ConfiguracionSistema.getParametroConfiguracionString(con, parIdEmpresa, "PLATAFORMA", "DER.HorarioEjercicioBloqueo.Inicio");
            String horaTerStr = ConfiguracionSistema.getParametroConfiguracionString(con, parIdEmpresa, "PLATAFORMA", "DER.HorarioEjercicioBloqueo.Fin");
            HashMap<String, Object> params = new HashMap<String, Object>();
            String fechaLocal = FechaDB.ObtenerFechaHoraSistemaLocal(con, parIdEmpresa);
            Timestamp fechaLocalTS = Fecha.toDateTime(fechaLocal);
            
            Timestamp fechaLocalIni = Fecha.toDateTime(fechaLocal.substring(0, 10)+" "+horaIniStr);
            Timestamp fechaLocalTer = Fecha.toDateTime(fechaLocal.substring(0, 10)+" "+horaTerStr);
            
//            fechaLocalTS.getTime();
            if (fechaLocalTS.after(fechaLocalIni) && fechaLocalTS.before(fechaLocalTer)) {
	            //------------------------------------------
	            // DATOS NOTIFICACION
	            //------------------------------------------
	            params.put("IdEmpresa", parIdEmpresa);
	            resNotif = _sql.query(con, SQL_CONSULTA_INFO_NOTIFICACION, params, InfoNotificacion.class);
	            
	            //--------------------------------------
	    		// VERIFICA CONSULTA ANTERIOR
	    		//--------------------------------------
	            tipoPrograma.add(TIPO_PROGRAMA_INICIO);
	            tipoPrograma.add(TIPO_PROGRAMA_TERMINO);
	            params = new HashMap<String, Object>();
	            params.put("IdEmpresa", parIdEmpresa.toString());
	            params.put("Fecha", confFechaSistema);
	            params.put("CodigoEvento", tipoPrograma);
	            resConsulta = _sql.query(con, SQL_CONSULTA_ANTERIOR, params, MensajeWebSocket.class);
	            
	    		//--------------------------------------
	    		// EJECUTA PROCESO PRINCIPAL
	    		//--------------------------------------
	            LOGGER.info("[ValidarHorarioEjercicioBloqueoOpciones--] Inicio llamada ConsultarEjercicioBloqueo");
	    		//Consulta horario para Envio Ejercicio/bloqueo de opciones
	    		EntradaConsultarEjercicioBloqueo inCons = new EntradaConsultarEjercicioBloqueo();
	    		inCons.p_IdEmpresa 					= parIdEmpresa;
	    		inCons.p_IdUsuario 					= parIdUsuario;
	    		inCons.p_IdModulo 					= "CUS";
	    		
	    		SalidaConsultarEjercicioBloqueo consSal = cons.logica(con, inCons);
	    		Horario horario = consSal.P_OUT_HORARIO;
	    		LOGGER.info("[ValidarHorarioEjercicioBloqueoOpciones--] Fin  llamada ConsultarEjercicioBloqueo");
	    		
	            //--------------------------------------
	    		// VERIFICA PROCESO SIGUIENTE DE PAUTA
	    		//--------------------------------------
	    		LOGGER.info("[ValidarHorarioEjercicioBloqueoOpciones--] Inicio llamada AgregarMensajeWebsocket");            
	            if(resConsulta.size() < 1 && horario.Estado.equalsIgnoreCase("SI")) {
	            	String dic = Diccionario.getMensaje(con, parIdEmpresa, "UI_HORARIO_INICIO_SORT_OPC", hash );
	            	AgregarMensajeWebsocket.agregar(con, parIdEmpresa, TIPO_PROGRAMA_INICIO, dic);
	            	notifIn = notificarHorario(confFechaSistema, resNotif, dic, parIdUsuario, parIdEmpresa, logo);
	            }
	            for (MensajeWebSocket res: resConsulta) { //ya se ingreso una notificacion de inicio de ejercicio
	            	if (res.Evento.equalsIgnoreCase(TIPO_PROGRAMA_INICIO) && horario.Estado.equalsIgnoreCase("NO") && resConsulta.size() <= 1) {
	            		String dic = Diccionario.getMensaje(con, parIdEmpresa, "UI_HORARIO_FIN_SORT_OPC", hash );
	            		AgregarMensajeWebsocket.agregar(con, parIdEmpresa, TIPO_PROGRAMA_TERMINO, dic);
	            		notifIn = notificarHorario(confFechaSistema, resNotif, dic, parIdUsuario, parIdEmpresa, logo);
	            	}
	            }
	            LOGGER.info("[ValidarHorarioEjercicioBloqueoOpciones--] Fin llamada AgregarMensajeWebsocket");
	            
	            if (notifIn.P_EVENTOS.size() > 0) {
					AgregarEvento notif = new AgregarEvento();
					LOGGER.info("[ValidarHorarioEjercicioBloqueoOpciones--] Inicio llamada AgregarEvento Notificacion");
					//startTime = System.nanoTime();
					AgregarEvento.ServiceSalida notifOut = notif.logica(con, notifIn);
					//log.debug("[ValidarHorarioEjercicioBloqueoOpciones--] (" + (System.nanoTime() - startTime) / 1000000 + ") Fin  llamada AgregarEvento Notificacion");
					LOGGER.info("[ValidarHorarioEjercicioBloqueoOpciones--] Fin  llamada AgregarEvento Notificacion");
	
				}
            }
            else {
            	LOGGER.info("[ValidarHorarioEjercicioBloqueoOpciones--] Fuera de Horario");
            }
			
			return true;
        }
	    catch (Exception e) {
	    	LOGGER.error("****** ERROR *******");
			LOGGER.error(Utils.stackTraceToString(e));
			notificacionMensaje=notificacionMensaje+"\nExcepcion: "+Utils.stackTraceToString(e);
			e.printStackTrace();
			notificacionEsError="S";
			throw e;
		} finally {
			try{//Notificar a usuarios TI:
				if (notificacionEsError.equalsIgnoreCase("S")) {
					NotificarAdministrador notiadmin = new NotificarAdministrador();
					NotificarAdministrador.NotificarAdministradorEntrada innotiadmin = new NotificarAdministrador.NotificarAdministradorEntrada();
					innotiadmin.NombreProceso=tipoPrograma.toString();
					innotiadmin.IdEmpresa=parIdEmpresa;
					innotiadmin.EsError=notificacionEsError;
					notificacionMensaje=notificacionMensaje+"\n\n"+"Id Programa: "+super.getJobId();
					innotiadmin.Mensaje=notificacionMensaje.replace("\n", "<br>-").replace("<br>-<br>", "<br>");
					LOGGER.info("Notificacion enviada:",notiadmin.logica(con, innotiadmin));	
				}
			}
			catch(Exception e){
				LOGGER.info("Excepcion controlada al notificar. Continua ejecucion");
			}
			LOGGER.info("****** FIN TAREA *******");
		}   
    }

    @Override
    public void onError(Throwable t) {
    	LOGGER.error("  ****** ERROR  *******");
    	LOGGER.error(t);
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	public static ServiceEntrada notificarHorario (String fecha, List<InfoNotificacion> movs, String estado, Long idUsuario, Long idEmpresa, String logo) {
		ServiceEntrada notifIn = new ServiceEntrada();
		List<ObjEVENTOS> eventosNotificacion = new ArrayList<>();
		List<ObjIDDIRECCIONES> direccionesNotificacion = new ArrayList<>();
		
		for (InfoNotificacion m: movs) {
			ObjEVENTOS evento = new ObjEVENTOS();
			evento.CodigoTipoEvento = "NOTIF_HORA_EJERCICIO_BLOQUEO_OPCIONES";
			evento.FechaCreacionSistema = fecha;
			evento.IdPersonaDestinatario = m.IdPersona;
			evento.IdPersonaRemitente = m.IdPersona;
			evento.IdPortafolio = 0L;
			evento.IdSecuencia = m.IdPersona;
			eventosNotificacion.add(evento);
	
			// Valores generales
			notifIn.addValor(m.IdPersona, "Producto", m.Producto)
					.addValor(m.IdPersona, "Supervisor", m.Supervisor)
					.addValor(m.IdPersona, "Agente", m.Agente)
					.addValor(m.IdPersona, "Fecha", Fecha.toFormat(Fecha.toDate(fecha), "dd-MM-yyyy"))
					.addValor(m.IdPersona, "Estado", estado)
					.addValor(m.IdPersona, "Corredora", m.NombreCorredora)
					.addValor(m.IdPersona, "LOGO_PUBLICO", logo);;
	
			ObjIDDIRECCIONES dir1 = new ObjIDDIRECCIONES();
			dir1.IdDireccion = m.IdDireccionElectronica;
			dir1.IdSecuencia = m.IdPersona;
			dir1.TipoDestinatario = "TO";
	
			direccionesNotificacion.add(dir1);
		}
		notifIn.P_IDEMPRESA	= idEmpresa;
		notifIn.P_IDUSUARIO = idUsuario;
		notifIn.P_IDMODULO = "CUS";
		notifIn.P_EVENTOS = eventosNotificacion;
		notifIn.P_IDDIRECCIONES = direccionesNotificacion;

		return notifIn;
	}
	//solo para test
	public static void main(String[] args) throws Throwable {
		// Example
		System.out.println("== START ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			//con.setAutoCommit(true);
			SQL.exec(con, "set schema BSCL");
			//SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
			// ---------------------------------
			try {
				ValidarHorarioEjercicioBloqueoOpciones apf = new ValidarHorarioEjercicioBloqueoOpciones();
				System.out.println(apf.transaction(con));
				con.commit();
			} catch (Exception er) {
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
	
}