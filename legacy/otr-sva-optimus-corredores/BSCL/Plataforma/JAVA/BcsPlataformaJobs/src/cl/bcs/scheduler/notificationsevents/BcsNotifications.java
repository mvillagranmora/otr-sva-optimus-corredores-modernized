package cl.bcs.scheduler.notificationsevents;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.evt.AgregarEmailNotificaciones;
import cl.bcs.plataforma.evt.AgregarEmailNotificaciones.ServiceEntrada;
import cl.bcs.plataforma.evt.AgregarEmailNotificaciones.ServiceSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.scheduler.notifications.pojo.KeyValue;
import cl.bcs.scheduler.notifications.pojo.ParametrosDefault;
import cl.bcs.scheduler.notifications.sql.Connectors;
import cl.bcs.scheduler.notifications.util.EvalTemplate;
import cl.bcs.scheduler.notifications.util.Validator;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class BcsNotifications extends BcsJob implements JavaCall{

    private Connectors connectors;
    private ParametrosDefault paramDefault;
    private final static String SQL_COUNT_NOTIFICADORES_CORRIENDO = "select \"IdPrograma\" FROM \"BSCL.Plataforma.BD::BAT.Programa\" where \"IdTipoPrograma\" = 'NOTIFY' and ( \"idEstado\"='ASI') and \"IdPrograma\" != :IdPrograma";
    
    private final Logger log = LogManager.getLogger(BcsNotifications.class);
    public boolean DEBUG = false;

    public BcsNotifications() {
        this.paramDefault = new ParametrosDefault();
        this.paramDefault.setIdEmpresa( Integer.parseInt(this.getOptionalParam("IdEmpresa").orElse("1")));
        this.paramDefault.setIdUsuario(0);
        this.paramDefault.setModulo("PLA");
        this.connectors = new Connectors();

    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();

    }
    
    private void vencerNotificaciones(Connection conn, ParametrosDefault params) {
    	//TODO desarmar esa funcion para consultar primero si hay notificaciones que vencer.
    	// vencer las notificaciones con fecha de vencimiento
        connectors.notifyOutTime(conn, this.paramDefault);
    }
    
    private List<Long> obtenerEventosAProcesar(Connection conn){
    	
    	
    	return null;
    }

    @Override
    public boolean transaction(Connection conn) throws InterruptedException, Throwable {
    	SQL sql = new SQL();
    	log.info("INICIO");
    	HashMap<String, Object> params = new HashMap<>();
    	params.put("IdPrograma", super.getJobId());
    	
        long inExecution = sql.countQuery(conn, SQL_COUNT_NOTIFICADORES_CORRIENDO, params);
        // POSIBLE CONDICION DE CARRERAj
        if (inExecution == 0 || DEBUG) {
            try {                
                this.vencerNotificaciones(conn, this.paramDefault);
                // obtener notificaciones agrupadas y no
                List<Long> notificaciones = groupNotifications(conn, connectors, this.paramDefault);

                if (notificaciones!= null && notificaciones.size() > 0) {
                    log.debug("Agregar :"+notificaciones.size());
                    AgregarEmailNotificaciones agregarEmailNotificaciones = new AgregarEmailNotificaciones();
                    ServiceEntrada in = new ServiceEntrada();
                    in.notificaciones = notificaciones;
                    in.idEmpresa = 1;
                    // generar correos electronicos a partir de las notificaciones
                    ServiceSalida salidaNotificacion = agregarEmailNotificaciones.logica(conn, in);
                    log.debug("Notificaciones agregadas a email: "+ ((salidaNotificacion.P_OUT_IDEVENTO!=null) ? salidaNotificacion.P_OUT_IDEVENTO.size() : 0 ));
                }
                log.info("TERMINO OK");
                return true;
            } catch (Exception e) {
                log.error(e);
            }
        }
        log.info("TERMINO NOK");
        return false;
    }

    @Override
    public void pause() {

    }

    /**
     * Metodo encargado de leer las notificaciones que no se hayan enviados o se
     * deban reenviar y se deban enviar segun su fecha de despacho
     */

    public List<Long> groupNotifications(Connection connection, Connectors connectors, ParametrosDefault paramDefault) {
        EvalTemplate evualate = new EvalTemplate();
        try {
            //buscar los eventos e ingresar notificaciones
            Map<String, List<Long>> idEventosAgrupados = connectors.groupNotifications(connection, paramDefault);
            //obtener notifiaciones a reprocesar.
            Set<Long> enviarNotificaciones = connectors.obtenerIdsNotificacionesReenviar(connection);
            
            if(idEventosAgrupados.get("IdNotificaciones").size()>0){
            	
                // var para incluir los tags de confirmacion en el correo
                String confirmationMacro = connectors.getConfirmationMacro(connection);
                // url para confirmar la notificacion
                String urlConfirmation = connectors.getUrl(connection, this.paramDefault);
                
                final List<Long> eventosNoAgrupados = idEventosAgrupados.get("IdEventoNoAgrupados");
                final List<Long> configuracionEventosNoAgrupados = idEventosAgrupados.get("IdConfiguracionEventoNoAgrupados");
                final List<Long> eventosAgrupados = idEventosAgrupados.get("IdEventoAgrupados");
                final List<Long> configuracionEventosAgrupados = idEventosAgrupados.get("IdConfiguracionEventoAgrupados");
                
                log.debug("Eventos agrupados: " + (eventosAgrupados!=null ? eventosAgrupados.size() : 0));
                log.debug("Eventos no agrupados: " + (eventosNoAgrupados!=null ? eventosNoAgrupados.size() : 0));              
                
                final Map<String, Long> idPersonasConEvento = connectors.getIdPersona(connection, paramDefault);

                Map<Long, String> getCodeTypeEvent = connectors.getCodeEventType(connection, paramDefault);
                Map<Long, String> templatesCodeEventType = connectors.getTemplate(connection, paramDefault);
                Map<Long, String> templatesRememberCodeEventType = connectors.getTemplateRecordatory(connection, paramDefault);
                Map<String, Object> keyValueMap = connectors.getKeyAndValue(connection, paramDefault);
                Map<String, String> tagPredefinidos = connectors.getTagPredefinidos(connection, paramDefault);
                Map<Integer, String> tokens = connectors.getTokens(connection, paramDefault);
    
                List<KeyValue> grouped = (List<KeyValue>) keyValueMap.get("Agrupa");
                List<KeyValue> notgrouped = (List<KeyValue>) keyValueMap.get("NoAgrupa");
    
                // buscar una forma de traerse solo el template que corresponde al
                // ID del evento, ver por que se usa en el agruopado
                JSONObject notgroupedjson = evualate.NoAgrupa(notgrouped, tagPredefinidos, 0);
    
                String template = "";
                String templateRecordatorio = "";
                String token = "";
    
                String sql = "UPDATE \"" + Utils.getSchema() + "\".\"BSCL.Plataforma.BD::EVT.Evento\" SET \"MensajeBody\" = ?, \"MensajeRecordatorio\" = ? WHERE \"IdEvento\" = ? ";
    
                try (PreparedStatement pst = connection.prepareStatement(sql)) {
    
                    List<Long> configuracionesEventoAgrupadoTodos = new ArrayList<Long>(configuracionEventosAgrupados);
    
                    for (Long configurationId : Validator.duplicateRemoveInteger(configuracionEventosAgrupados)) {
                        System.out.println(getCodeTypeEvent.get(configurationId));
                        JSONObject groupedjson = evualate.Agrupa(grouped, tagPredefinidos, getCodeTypeEvent.get(configurationId));
                        JSONObject tagGenerales = new JSONObject(groupedjson.getJSONObject("data").get("TagGenerales").toString());
    
                        template = templatesCodeEventType.get(configurationId);
                        token = tokens.get(configurationId);
                        templateRecordatorio = templatesRememberCodeEventType.get(configurationId);
                        try{
                        	JSONArray obj = groupedjson.getJSONObject("data").getJSONArray(getCodeTypeEvent.get(configurationId));
                        
	                        for (int i = 0; i < configuracionesEventoAgrupadoTodos.size(); i++) {
	    
	                            if (configuracionesEventoAgrupadoTodos.get(i) == configurationId && obj!=null) {
	                                Long eventId = eventosAgrupados.get(i);
	                                // id del evento a actualizar
	                                Long idPersona = idPersonasConEvento.get(String.valueOf(eventId));
	                                // Quitar informacion que no pertenezca al mismo
	                                // destinatario.
	                                JSONArray objTodosDestinatario = new JSONArray(obj.toString());
	                                JSONArray objSoloDestinatario = new JSONArray();
	                                for (int j = 0; j < objTodosDestinatario.length(); j++) {
	                                    JSONObject json = objTodosDestinatario.getJSONObject(j);
	                                    if (json.getInt("IdPersonaDestinatario") == idPersona) {
	                                        objSoloDestinatario.put(json);
	                                    }
	                                }
	    
	                                JSONObject reqObj = new JSONObject();
	                                reqObj.put(getCodeTypeEvent.get(configurationId), objSoloDestinatario);
	                                // log.write(objSoloDestinatario);
	                                reqObj.put("TagGenerales", tagGenerales);
	                                JSONObject Objfather = new JSONObject();
	                                Objfather.put("data", reqObj);
	                                String temp = StringEscapeUtils.unescapeHtml(confirmationMacro.concat(template.replace("<!--", "").replace("-->", "")));
	                                String context = EvalTemplate.eval(Objfather.toString(), temp);
	                                pst.setString(1, context.trim().equals("-1") ? null : StringEscapeUtils.escapeHtml(context.replace("::URL::", "/BSCL/Plataforma/GWJAVA/PORTAL/validate?token=" + token)));
	                                pst.setString(2, templateRecordatorio);
	                                pst.setLong(3, eventId);
	                                pst.addBatch();
	                            }
	                        }
                        }catch(JSONException ex){
                        	ex.printStackTrace();
                        }
                    }
    
                    int[] x = pst.executeBatch();
                    if (x.length > 0)
                        connection.commit();
                }
                try (PreparedStatement pst = connection.prepareStatement(sql)) {
    
                    for (int i = 0; i < configuracionEventosNoAgrupados.size(); i++) {
    
                        template = templatesCodeEventType.get(configuracionEventosNoAgrupados.get(i));
                        templateRecordatorio = templatesRememberCodeEventType.get(configuracionEventosNoAgrupados.get(i));
                        token = tokens.get(configuracionEventosNoAgrupados.get(i));
                        Long eventId = eventosNoAgrupados.get(i); // id del evento a
                        // actualizar
                        JSONObject dataEventoJson = evualate.NoAgrupa(notgrouped, tagPredefinidos, eventId);
                        
                        String urlBase = ConfiguracionSistema.getParametroConfiguracionString(connection, (long) paramDefault.getIdEmpresa(), "PLATAFORMA", "PLA.UrlHana");
                        String idEventoCodificado = Utils.codificarEvento(eventId+"");
                        
                        String bug = "<img id=\"bcs-tracker\" data-corredor=\"\" data-id-evento=\"" + idEventoCodificado + "\" src=\"" + urlBase + "/BSCL/Plataforma/GWSERVICES/mail?r=" + idEventoCodificado + "\" style=\"display:none!important\" height=\"1\" width=\"1\"></div>";
                        confirmationMacro = confirmationMacro.replace("::URL::", urlConfirmation + "?token=" + token);
                        String temp = StringEscapeUtils.unescapeHtml(confirmationMacro.concat(template.replace("<!--", "").replace("-->", "")));
                        String context = EvalTemplate.eval(dataEventoJson.toString(), temp);
                        context+=bug;
                        pst.setString(1, context.trim().equals("-1") ? null : StringEscapeUtils.escapeHtml(context.replace("::URL::", "/BSCL/Plataforma/GWJAVA/PORTAL/validate?token=" + token)));
                        pst.setString(2, templateRecordatorio);
                        pst.setLong(3, eventId);
                        pst.addBatch();
    
                    }
                    int[] x = pst.executeBatch();
                    if (x.length > 0)
                        connection.commit();
                }
    
                List<Long> outEvt = new ArrayList<>();
                outEvt.addAll(Validator.duplicateRemoveInteger(eventosNoAgrupados));
                outEvt.addAll(Validator.duplicateRemoveInteger(eventosAgrupados));
                // actualizar los eventos como procesados
                connectors.updateEvent(connection, outEvt, paramDefault);
                //agregar notificaciones nuevas al set de notificaciones por reenviar.
                idEventosAgrupados.get("IdNotificaciones").forEach(not -> enviarNotificaciones.add(not));                
            }
            
            return Lists.newArrayList(enviarNotificaciones);
        } catch (Exception e) {
            log.error(e);
            return null;
        }

    }
    
  //solo para test
	public static void main(String[] args) {
		// Example
		System.out.println("== START ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa")) {
			con.setAutoCommit(false);
			SQL.exec(con, "set schema BSCL");
			SQL.exec(con, "SET TRANSACTION AUTOCOMMIT DDL OFF");
			// ---------------------------------
			try {
				BcsNotifications apf = new BcsNotifications();
				apf.DEBUG = true;
				Long idEvento = 223547L;
				SQL.exec(con, "UPDATE \"BSCL.Plataforma.BD::EVT.Evento\" SET \"Procesado\" = 'N' WHERE \"IdEvento\" = "+idEvento+";");
				System.out.println(apf.transaction(con));
				con.rollback();
				
			} catch (Exception er) {
				er.printStackTrace();
				con.commit();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	
		System.out.println("== END ==");
	}

	/**
	 * UNINPLEMENTED
	 */
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}

}