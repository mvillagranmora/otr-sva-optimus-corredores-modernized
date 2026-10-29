package cl.bcs.plataforma.services;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.PluginLoader;
import cl.bcs.utils.Utils;

@WebListener
@WebServlet("/CallBatch")
public class CallBatch  extends HttpServlet  implements ServletContextListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7511279591334465503L;

	@Resource(name = "jdbc/hanaDB")
	DataSource	dataSource;

	private final Log					log					= new Log("PLA.CallBatch");
	private ScheduledExecutorService	scheduler			= null;
	private boolean						stop				= false;
	private final static Stack<Job>		st					= new Stack<>();
	private String						tokenSeguridad		= null;

    public CallBatch() {
        super();
    }	
    
	private static class Job {
		public long			IdJob;
		public String		ClassName;
		public JsonObject	JsonInput;
		public String toString() {
			return "JOB="+IdJob+" CLASS="+ClassName+" JSON="+JsonInput.toString();
		}		
	}

	private void add(String pClass, JsonObject jin) {
		Job njob=new Job();
		njob.IdJob=System.currentTimeMillis();
		njob.ClassName=pClass;
		njob.JsonInput=jin;
		st.push(njob);
		log.write("Job agregado => "+njob.toString());
	}

	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String pTokenSession	= request.getHeader("X-SESSION-TOKEN");
			String   jarClass       = request.getParameter("t");		
			if (pTokenSession==null)  {
				response.setStatus(401);
				response.getOutputStream().print("401 - NOT AUTHORIZED");	
				response.getOutputStream().flush();					
				return;				
			}
			if (jarClass==null)  {
				response.setStatus(401);
				response.getOutputStream().print("401 - INVALID SERVICE");	
				response.getOutputStream().flush();					
				return;				
			}
			if (tokenSeguridad==null) {
				try (Connection conn=dataSource.getConnection()) {
					tokenSeguridad=Utils.getConfiguracion(conn,1L,"PLA.CallBatchToken");
				}catch(Exception er) {
					response.setStatus(503);
					response.getOutputStream().print("503 - SERVICE UNAVAILABLE");
					response.getOutputStream().flush();
					return;
				}
			}
			if (!tokenSeguridad.equals(pTokenSession)) {
				response.setStatus(401);
				response.getOutputStream().print("401 - NOT AUTHORIZED");	
				response.getOutputStream().flush();					
				return;					
			}
		    response.setContentType("application/json; charset=UTF-8");
		    response.setCharacterEncoding("UTF-8");	
			StringBuilder sb = new StringBuilder();
            String s;
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }         
            String bodyParms=sb.toString();            
            if (bodyParms==null||bodyParms.length()==0) bodyParms="{ }";            
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(bodyParms).getAsJsonObject();
	        add(jarClass,dataJsonIn);
			response.setStatus(200);
			response.getOutputStream().print("200 - OK+");	
			response.getOutputStream().flush();			        		    
		}catch (Exception er) {
			er.printStackTrace();
			response.setStatus(401);
			response.getOutputStream().print("401 - ERROR SERVICE");	
			response.getOutputStream().flush();		
		}
	}
	
	public void contextInitialized(ServletContextEvent event) {
		log.write("Iniciando Servidor CallBatch");
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				while (!stop) {
					if (stop) {
						log.write("CALLBATCH PILA="+st.size());
						while (st.size()>0) {
							Job ele=st.pop();											
							log.write("WARNING: No se va ejecutar => "+ele.toString());
						}					
						scheduler.shutdown();
					}else {		
						while (st.size()>0) {
							Job ele=st.pop();
							Connection conn=null;
							try {
								log.write("Procesando => "+ele.toString());
								long SrvTime=System.currentTimeMillis();
						        conn = dataSource.getConnection(); 
								try (Statement st=conn.createStatement();) {
									st.addBatch("SET TRANSACTION AUTOCOMMIT DDL OFF");
									st.addBatch("SET TRANSACTION LOCK WAIT TIMEOUT 60000");				
									st.executeBatch();
								}	
								String parts[]=ele.ClassName.split("@");			
								String pathBase=System.getProperty("user.home")+"/bin/";
				                
				                JavaCall jcall = null;
								try {
									jcall = (JavaCall)PluginLoader.getObject(pathBase,"PLUGIN_OPTIMUS.jar", parts[1]);
								}
								catch(NoSuchFileException e) {
									System.out.println("PLUGIN_OPTIMUS no encontrado, saltando");
								}
								if (jcall == null) {
									jcall = (JavaCall)PluginLoader.getObject(pathBase,parts[0], parts[1]);
								}
				                
							    JsonObject dataJsonCallOut=jcall.call(conn,ele.JsonInput);	
							    						    
							    String NameCountError="p_out_ErroresContador";
							    if (dataJsonCallOut.get("p_out_ErrorCounter".toUpperCase())!=null) NameCountError="p_out_ErrorCounter";	
							    if (dataJsonCallOut.get(NameCountError.toUpperCase())==null || dataJsonCallOut.get(NameCountError.toUpperCase()).getAsLong()>0) {
							    	conn.rollback();
							    	SrvTime=System.currentTimeMillis()-SrvTime;	
							    	log.write("Proceso terminado con rollback para Job="+ele.IdJob+" mseg="+SrvTime+" Salida="+dataJsonCallOut.toString());
							    }else {
							    	conn.commit();
							    	SrvTime=System.currentTimeMillis()-SrvTime;	
							    	log.write("Proceso terminado con commit para Job="+ele.IdJob+" mseg="+SrvTime+" Salida="+dataJsonCallOut.toString());						    	
							    }
							} catch (Exception e) {
								log.write("ERORR: No se va ejecutar => "+ele.toString(),e);
							}finally {
								if (conn!=null) {
									try {conn.rollback();}catch(Exception er) { }
									try {conn.close();}catch(Exception er) { }
								}
							}
						}
					}		
					try { Thread.sleep(1000);} catch (InterruptedException e) {  }
				}//end while				
			}			 
		 }, 0, 10, TimeUnit.SECONDS); 
	}

	public void contextDestroyed(ServletContextEvent event) {
		log.write("Terminando Servidor CallBatch");
		stop=true;
	}

}
