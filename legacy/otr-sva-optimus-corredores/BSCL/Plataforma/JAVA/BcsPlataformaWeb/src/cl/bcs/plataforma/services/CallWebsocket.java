package cl.bcs.plataforma.services;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.JsonObject;

import cl.bcs.nuam.StoredProcedure;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import datadog.trace.api.DDTags;
import datadog.trace.api.Trace;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

@WebListener
@ServerEndpoint("/CallWebsocket")
public class CallWebsocket   implements ServletContextListener {

	@Resource(name = "jdbc/hanaDB")
	DataSource	dataSource;

	private final Log	log = new Log("PLA.CallWebsocket");
	
	private static HashMap<String,WSession> allSessions		= new HashMap<>() ; 	 
	private ScheduledExecutorService		scheduler		= null;
	private boolean							stop			= false;
	
	public static class WSession {
		public Long	   timestamp;
		public Session session;
		public String  token;
		public Long	   IdEmpresa;
		public String  Usuario;
		public Long    IdUsuario;
		public boolean Autentificado;
	}

	public static class MensajeWebsocket {
		public Long 		IdEmpresa;
		public Timestamp	FechaRegistro;
		public String 		Evento;
		public String		DATA;
		public String		EsProcesado;		
	}
	
    public CallWebsocket() {
        super();
    }	
        
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {      	
    	log.write("Nueva conexion id="+session.getId());    
    	WSession obj=new WSession();
    	obj.session=session;
    	obj.timestamp=System.currentTimeMillis();
    	obj.Autentificado=false;
    	allSessions.put(session.getId(), obj);
    }
    
    @Trace
    @OnMessage
    public void onMessage(Session session, String message) {
    	//drop trace
	    Tracer tracer = GlobalTracer.get();
	    tracer.activeSpan().setTag(DDTags.MANUAL_DROP, true);
	    
    	log.write("Mensaje recibido id="+session.getId()+" Msg="+message);
    	String respuesta="";
    	if (message.startsWith("X-SESSION-TOKEN:")) {    		
    		Connection conn=null;
			try {				
		        conn = dataSource.getConnection();
		        boolean isPostgre = SQL.isPostgresConnection(conn);
		        if (!isPostgre) {
					try (Statement st=conn.createStatement();) {
						st.addBatch("SET TRANSACTION AUTOCOMMIT DDL OFF");
						st.addBatch("SET TRANSACTION LOCK WAIT TIMEOUT 60000");				
						st.executeBatch();
					}
		        }
	    		WSession obj=allSessions.get(session.getId());
	    		obj.token=message.split(":")[1];
	    		
	    		//Valida Sesion del Cliente	    		
	    		int     servicioObtenerFechas=14;
				JsonObject dataSRVIn=new JsonObject();
				dataSRVIn.addProperty("p_Token",obj.token);
				dataSRVIn.addProperty("p_IdServicio",servicioObtenerFechas);
				StoredProcedure kHashSrv=new StoredProcedure("BSCL.Plataforma.BD.Servicios::ACC.ProcesaLlamadaServletCall");
				JsonObject dataSRVOut=kHashSrv.call(conn, dataSRVIn);
				String SrvPermiteUso=dataSRVOut.get("p_out_PermiteUso".toUpperCase()).getAsString();
				String SrvMensaje=dataSRVOut.get("p_out_Mensaje".toUpperCase()).getAsString();
				Long SesionIdEmpresa=dataSRVOut.get("p_out_IdEmpresa".toUpperCase()).getAsLong();
				Long SesionIdUsuario=dataSRVOut.get("p_out_IdUsuario".toUpperCase()).getAsLong();
				String SesionCodigoUsuario=dataSRVOut.get("p_out_NombreUsuario".toUpperCase()).getAsString();				
				if (SrvPermiteUso.equals("S")==false) {
					conn.rollback();
					respuesta="";
					obj.Autentificado=false;
					log.write("Token Sesion Invalido id="+session.getId()+" Msg="+SrvMensaje);
				}else {
					conn.commit();
					obj.IdEmpresa=SesionIdEmpresa;
					obj.Usuario=SesionCodigoUsuario;
					obj.IdUsuario=SesionIdUsuario;
					obj.Autentificado=true;
					respuesta="OK+";   
					log.write("Token Sesion +OK id="+session.getId()+" Usuario="+SesionCodigoUsuario+" IdUsuario="+SesionIdUsuario);					
				}	    		
			} catch (Exception e) {
		    	try { session.close();} catch (IOException e2) {}
			}finally {
				if (conn!=null) {
					try {conn.rollback();}catch(Exception er) { }
					try {conn.close();}catch(Exception er) { }
				}
			}			    		 		
    	}
    	else {
    		//Mensaje no reconocido
    	}
    	//Envia Respuesta
    	try {
    		if (respuesta.length()>0) {
    			session.getBasicRemote().sendText(respuesta);
    		}
		} catch (IOException e) {
			log.write("Error respuesta id="+session.getId()+" Msg="+message,e);
		}
    	tracer.activeSpan().finish();
    }    
    
    @OnError
    public void error(Session session, Throwable t) {
    }
    
    @OnClose
    public void onClose(Session session, CloseReason reason) {       	
    	log.write("Conexion cerrada id="+session.getId()+" Msg="+reason.getReasonPhrase());
    	if (allSessions.containsKey(session.getId())) allSessions.remove(session.getId());
    }
    
	public void contextInitialized(ServletContextEvent event) {
		log.write("Iniciando Servidor CallWebsocket");
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Trace
			@Override
			public void run() {
				while (!stop) {								
					if (stop) {
						log.write("Terminando Servidor CallWebsocket...");			
						scheduler.shutdown();
					}else if (allSessions!=null && allSessions.size()>0) {
						//drop trace
					    Tracer tracer = GlobalTracer.get();
					    tracer.activeSpan().setTag(DDTags.MANUAL_DROP, true);
						Connection conn=null;
						try {
					        conn = dataSource.getConnection(); 
					        boolean isPostgre = SQL.isPostgresConnection(conn);
					        if (!isPostgre) {
								try (Statement st=conn.createStatement();) {
									st.addBatch("SET TRANSACTION AUTOCOMMIT DDL OFF");
									st.addBatch("SET TRANSACTION LOCK WAIT TIMEOUT 60000");				
									st.executeBatch();
								}
					        }
							///////////////////////////////////
							//Buscar eventos para enviar a suscriptores
							SQL sql=new SQL();
							List<MensajeWebsocket> mensajes=sql.query(conn, 
									"SELECT  \"IdEmpresa\",\"FechaRegistro\",\"Evento\",\"DATA\" "
									+ " FROM \"BSCL.Plataforma.BD::PLA.MensajeWebsocket\" "
									+ " WHERE \"EsProcesado\" = 'N' AND \"FechaRegistro\" > ADD_SECONDS(CURRENT_UTCTIMESTAMP,-3600) ",  
									new HashMap<>(), 
									MensajeWebsocket.class);	
							///////////////////////////////////
							//Procesa Mensajes para enviar
							Timestamp FechaRegistroMax=null;
							for (MensajeWebsocket msg:mensajes) {
								//Ultima fecha utilziada para actualziar
								if (FechaRegistroMax==null || 
										msg.FechaRegistro.after(FechaRegistroMax)) {
									FechaRegistroMax=msg.FechaRegistro;
								}
								//Enviar a los suscriptores mensaje
								for(WSession vsesion:allSessions.values()) {
									Session session=vsesion.session;								
									try {							
										if (vsesion.Autentificado==false) continue;
										if (vsesion.IdEmpresa.longValue()==msg.IdEmpresa.longValue()) {
											JsonObject obj=new JsonObject();
											obj.addProperty("Evento", msg.Evento);
											obj.addProperty("DATA", msg.DATA);											
											session.getBasicRemote().sendText(obj.toString());											
										}										
									}catch (Exception er) {
										log.write("No se puede enviar mensaje id="+session.getId()+" Error:"+er.getMessage());
									}
								}								
							}	
							///////////////////////////////////
							//Actualiza los eventos como procesados
							if (FechaRegistroMax!=null) {
								List<MensajeWebsocket> mensajesOUT=new ArrayList<>();
								MensajeWebsocket obj=new MensajeWebsocket();
								obj.FechaRegistro=FechaRegistroMax;
								mensajesOUT.add(obj);
								sql.batch(conn,
										"UPDATE \"BSCL.Plataforma.BD::PLA.MensajeWebsocket\" "
										+ " SET \"EsProcesado\" = 'S' "
										+ " WHERE \"EsProcesado\" = 'N' AND  \"FechaRegistro\" <= :FechaRegistro ",
										mensajesOUT, 
										MensajeWebsocket.class);
								conn.commit();
							}
						} catch (Exception e) {
							log.write("ERORR: al procesar. Reintento en 5 segs.",e);
							try { Thread.sleep(5000);} catch (InterruptedException e2) {  }
						}finally {
							if (conn!=null) {
								try {conn.rollback();}catch(Exception er) { }
								try {conn.close();}catch(Exception er) { }
							}
						}
						tracer.activeSpan().finish();
					}
					try { Thread.sleep(1000);} catch (InterruptedException e) {  }
				}//end while				
			}			 
		 }, 0, 10, TimeUnit.SECONDS); 
	}

	public void contextDestroyed(ServletContextEvent event) {
		log.write("Terminando Servidor CallWebsocket");
		stop=true;
	}

}
