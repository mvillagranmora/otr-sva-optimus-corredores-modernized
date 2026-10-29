
package cl.bcs.plataforma.services;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import cl.bcs.nuam.StoredProcedure;
import cl.bcs.nuam.StoredProcedureException;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.JavaCallExtra;
import cl.bcs.utils.JavaCallMetadata;
import cl.bcs.utils.JavaView;
import cl.bcs.utils.PluginLoader;
import cl.bcs.utils.ProcesaLlamadaServletCall;
import cl.bcs.utils.ProcesaLlamadaServletCall.EntradaProcesaLlamadaServletCall;
import cl.bcs.utils.ProcesaLlamadaServletCall.SalidaProcesaLlamadaServletCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.kaiser.odata.OdataImpSql;
import datadog.trace.api.DDTags;
import datadog.trace.api.Trace;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

/***
 * 
 * @author cstange
 *
 */
@WebServlet("/call")
public class CallServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger		= LogManager.getLogger(CallServlet.class.getName());
	private static final List<Integer> noTraceService = Arrays.asList(600017, 1381, 14);
	
	private static String muestraLog = "S";
	@Resource(name="jdbc/hanaDB") DataSource dataSource;

	@Resource(name="jdbc/auditDS") DataSource auditDataSource;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public CallServlet() {
        super();
        //servicios no monitorizados en datadog.
    }	
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    @Trace
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			performTask(request, response);
		}catch (Exception er) {
			er.printStackTrace();						
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    @Trace
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			performTask(request, response);
		}catch (Exception er) {
			er.printStackTrace();			
		}
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws SQLException 
	 * @throws IOException 
	 */
	
	public String SELECT_CONFIGURACION = "SELECT \"IdEmpresa\",\"CodigoParametro\",\"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"IdEmpresa\" = :IdEmpresa";
	public static class Configuracion {
		public String CodigoParametro;
		public String Valor;
	}
	
	@Trace
	private void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
		Connection serviceConn=null; 
		Connection auditConn=null; 
		int 	IdServicio=0;
		String 	pResulset="";
		String 	pTokenSession="";
		String 	schemaX=Utils.getSchema();
		long 	SesionIdEmpresa=0;
		long 	SesionIdUsuario=0;	
		String  SesionModulo="PLA";
		long   	SesionIdCliente=0;
		long    TotalTime=System.currentTimeMillis();
		long    SrvTime=0;	
		long    CallTime=0;		
		String  Canal = "*";		//COR,CLI,AGE,WS
		String 	SesionCodigoUsuario="";
		String 	SesionTipoUsuario="";
		String 	SrvPermiteUso="";
		String 	SrvNombre="";
		String 	SrvExtras="";
		String 	SrvTipo="";
		String 	SrvTempIn="";
		String 	SrvTempOUT="";	
		String 	SrvMensaje="";
		String NameCountError="p_out_ErroresContador";
		
		response.setContentType("application/json; charset=UTF-8");
	    response.setCharacterEncoding("UTF-8");
	    
	    //Instanciar opentracing
	    Tracer tracer = GlobalTracer.get();
        String txid = UUID.randomUUID().toString();
        ThreadContext.put("T", txid);
	    
	    try {										
			if (request.getParameter("t")==null) throw new Exception("bad request");			
			IdServicio 		= Integer.parseInt(request.getParameter("t"));
			pResulset 		= request.getParameter("r");		
			pTokenSession	= request.getHeader("X-SESSION-TOKEN");
			Canal			= request.getParameter("ch");
			//check token  
			if (pTokenSession==null) {
				response.setStatus(401);
				response.getOutputStream().print("401 - NOT AUTHORIZED");	
				response.getOutputStream().flush();					
				return;
			}
			//get body data
			StringBuilder sb = new StringBuilder();
            String s;
            
            //read request as UTF-8
            try(InputStream inputStream = request.getInputStream()){
	            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream , StandardCharsets.UTF_8));
	            while ((s = reader.readLine()) != null) {
	                sb.append(s);
	            }
            }
            String bodyParms=sb.toString();
            if (request.getMethod().equals("GET") && IdServicio != 600017) {
            	logger.debug("New request PARAMS=" + request.getQueryString());
            }
            else if (request.getMethod().equals("POST") && IdServicio != 600017) {
            	logger.debug("New request PARAMS=" + request.getQueryString() + " BODY="+bodyParms);
            }
            
            
            if (bodyParms==null||bodyParms.length()==0) bodyParms="{ }";
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(bodyParms).getAsJsonObject();
	        if (dataJsonIn.get("p_IdModulo")!=null && dataJsonIn.get("p_IdModulo").isJsonNull()==false)  
	        	SesionModulo=dataJsonIn.get("p_IdModulo").getAsString();
	        if (dataJsonIn.get("IdModulo")!=null && dataJsonIn.get("IdModulo").isJsonNull()==false)    
	        	SesionModulo=dataJsonIn.get("p_IdModulo").getAsString();
	        if (dataJsonIn.get("IdCliente")!=null && dataJsonIn.get("IdCliente").isJsonNull()==false)   
	        	SesionIdCliente=dataJsonIn.get("IdCliente").getAsLong();
	        if (dataJsonIn.get("p_IdCliente")!=null && dataJsonIn.get("p_IdCliente").isJsonNull()==false) 
	        	SesionIdCliente=dataJsonIn.get("p_IdCliente").getAsLong();
			///////////////////////////////////////
	        //get connection & check session
			///////////////////////////////////////
	        serviceConn = dataSource.getConnection();
	        auditConn = auditDataSource.getConnection();
	        boolean isPostgre = SQL.isPostgresConnection(serviceConn);
	        if (!isPostgre) {
				try (Statement st=serviceConn.createStatement();) {
					st.addBatch("SET TRANSACTION AUTOCOMMIT DDL OFF");
					st.addBatch("SET TRANSACTION LOCK WAIT TIMEOUT 60000");				
					st.executeBatch();
				}
	        }

			SrvTime=System.currentTimeMillis();
			EntradaProcesaLlamadaServletCall entradaProcesa = new EntradaProcesaLlamadaServletCall();
			entradaProcesa.p_Token = pTokenSession;
			entradaProcesa.p_IdServicio = IdServicio;
			entradaProcesa.p_IdTransaccion = txid;
			entradaProcesa.p_IdCanal = Canal;
			ProcesaLlamadaServletCall service = new ProcesaLlamadaServletCall();
			SalidaProcesaLlamadaServletCall salida = service.logica(serviceConn,auditConn,entradaProcesa);
			
			SrvPermiteUso = salida.p_out_PermiteUso;
			SrvMensaje = salida.p_out_Mensaje;
			
			if (SrvPermiteUso.equals("S")==false) {
				if(SrvMensaje.equals("SERVICIO INVALIDO")) {
					Utils.registrarAuditoriaServicios(auditConn, IdServicio,SrvNombre, SesionIdUsuario, txid, "FIN_ERROR", "404", null, null, null);
					serviceConn.rollback();
					response.setStatus(404);
					response.getOutputStream().print("404 - "+SrvMensaje);
					response.getOutputStream().flush();
				}else {
					Utils.registrarAuditoriaServicios(auditConn, IdServicio,SrvNombre, SesionIdUsuario, txid, "FIN_ERROR", "401", null, null, null);
					serviceConn.rollback();
					response.setStatus(401);
					response.getOutputStream().print("401 - "+SrvMensaje);
					response.getOutputStream().flush();
				}
				return;
			}
			
			SesionIdEmpresa = salida.p_out_IdEmpresa;
			SesionIdUsuario = salida.p_out_IdUsuario;
			SesionCodigoUsuario = salida.p_out_NombreUsuario;
			SesionTipoUsuario = salida.p_out_TipoUsuario;
			SrvNombre = salida.p_out_NombreServicio;
			SrvExtras = salida.p_out_Extras;
			SrvTipo = salida.p_out_TipoServicio;
			SrvTempIn = salida.p_out_ServicioTempIn;
			SrvTempOUT = salida.p_out_ServicioTempOut;
			
			SrvTime=System.currentTimeMillis()-SrvTime;
			SQL _sql = new SQL();
			HashMap<String,Object> params = new HashMap<>();
			params.put("IdEmpresa", SesionIdEmpresa);
			List<Configuracion> _configuracion = _sql.query(serviceConn, SELECT_CONFIGURACION, params, Configuracion.class);
			HashMap<String,Object> configuracion = Utils.List2Hash(_configuracion, "CodigoParametro");
			
			muestraLog = ((Configuracion)configuracion.get("PLA.MuestraLog")) != null ? ((Configuracion)configuracion.get("PLA.MuestraLog")).Valor : "S";
			
			ThreadContext.put("U", SesionCodigoUsuario + ":" + SesionTipoUsuario + ":" + SrvNombre);
			///////////////////////////////////////
			//add servlet input traces
			///////////////////////////////////////
			tracer.activeSpan().setBaggageItem(DDTags.RESOURCE_NAME, SrvNombre);
			tracer.activeSpan().setBaggageItem("http.service_number", String.valueOf(IdServicio));
			tracer.activeSpan().setBaggageItem("http.query_string", request.getRequestURL().append("?").append(request.getQueryString()).toString() );
			tracer.activeSpan().setBaggageItem("http.body", dataJsonIn.toString());
			
			///////////////////////////////////////
			//call procedure service
			///////////////////////////////////////
			if (SrvTipo.equalsIgnoreCase("p")) {
				//call procedure
				JsonObject dataJsonCallIn	= dataJsonIn;
				if (dataJsonCallIn.get("IdEmpresa")!=null)   	dataJsonCallIn.addProperty("IdEmpresa",SesionIdEmpresa); 
				if (dataJsonCallIn.get("p_IdEmpresa")!=null) 	dataJsonCallIn.addProperty("p_IdEmpresa",SesionIdEmpresa); 
				if (dataJsonCallIn.get("IdUsuario")!=null) 		dataJsonCallIn.addProperty("IdUsuario",SesionIdUsuario); 
				if (dataJsonCallIn.get("p_IdUsuario")!=null) 	dataJsonCallIn.addProperty("p_IdUsuario",SesionIdUsuario); 
				CallTime=System.currentTimeMillis();
				StoredProcedure kCallSrv2=new StoredProcedure(SrvNombre,SrvTempIn,SrvTempOUT);
				JsonObject dataJsonCallOut=kCallSrv2.call(serviceConn, dataJsonCallIn);
				CallTime=System.currentTimeMillis()-CallTime;
				if (dataJsonCallOut.get("p_out_ErrorCounter".toUpperCase())!=null) NameCountError="p_out_ErrorCounter";							
				if (dataJsonCallOut.get(NameCountError.toUpperCase())==null || dataJsonCallOut.get(NameCountError.toUpperCase()).getAsLong()>0) {
					TotalTime=System.currentTimeMillis()-TotalTime;
					Utils.registrarAuditoriaServicios(auditConn, IdServicio,SrvNombre, SesionIdUsuario, txid, "FIN_ERROR", null, null, null, null);
					serviceConn.rollback();
					response.setStatus(200);
					response.getOutputStream().write(dataJsonCallOut.toString().getBytes("UTF-8"));	
					response.getOutputStream().flush();
					logger.info("Total Time="+TotalTime);
					return;
				}								
				if (pResulset!=null) {
					TotalTime=System.currentTimeMillis()-TotalTime;
					Utils.registrarAuditoriaServicios(auditConn, IdServicio,SrvNombre, SesionIdUsuario, txid, "FIN", null, null, null, null);
					response.setStatus(200);					
					response.getOutputStream().write(dataJsonCallOut.get(pResulset.toUpperCase()).toString().getBytes("UTF-8"));	
					response.getOutputStream().flush();	
					serviceConn.commit();
					logger.info("Total Time="+TotalTime);							
					return;
				}
				TotalTime=System.currentTimeMillis()-TotalTime;
				Utils.registrarAuditoriaServicios(auditConn, IdServicio,SrvNombre, SesionIdUsuario, txid, "FIN", null, null, null, null);
				dataJsonCallOut.addProperty("TotalExecutionTime",TotalTime);
				dataJsonCallOut.addProperty("SessionExecutionTime",SrvTime);	
				dataJsonCallOut.addProperty("CallExecutionTime",CallTime);			
				response.setStatus(200);
				response.getOutputStream().write(dataJsonCallOut.toString().getBytes("UTF-8")); 
				response.getOutputStream().flush();	
				serviceConn.commit();
				logger.info("Total Time="+TotalTime);								
				return;
			} else if (SrvTipo.equalsIgnoreCase("q")) {
			    OdataImpSql odata=new OdataImpSql();
			    String 	urlCallback=request.getParameter("$callback");
			    String 	urlSelect=request.getParameter("$select");
			    String 	urlFilter=request.getParameter("$filter")!=null?request.getParameter("$filter"):"";
			    String 	urlSort=request.getParameter("$orderby");
			    String 	urlHandler=request.getParameter("$handler")!=null?request.getParameter("$handler"):"";
			    String  urlObject="\""+SrvNombre+"\"" + urlHandler;
			    long 	urlTop=request.getParameter("$top")!=null?Long.parseLong(request.getParameter("$top")):-1;
			    long 	urlSkip=request.getParameter("$skip")!=null?Long.parseLong(request.getParameter("$skip")):-1;
			    boolean urlInline=false;
			    if  (request.getParameter("$inlinecount")!=null) {
			    	if (request.getParameter("$inlinecount").equals("allpages")) urlInline=true;
			    }
				response.setStatus(200);
			    odata.QueryJsonObject(serviceConn,response.getOutputStream(),urlCallback, urlSelect, urlObject,urlFilter,urlSort,urlTop,urlSkip,urlInline);
			    TotalTime=System.currentTimeMillis()-TotalTime;
				Utils.registrarAuditoriaServicios(auditConn, IdServicio,SrvNombre, SesionIdUsuario, txid, "FIN", null, null, null, null);
			    logger.info("Total Time="+TotalTime);
			    serviceConn.commit();
				return;
			} 
			else if (SrvTipo.equalsIgnoreCase("j") && pResulset!=null) {
				
				JsonObject dataJsonCallIn	= dataJsonIn;
				if (dataJsonCallIn.get("IdEmpresa")!=null)   	dataJsonCallIn.addProperty("IdEmpresa",SesionIdEmpresa); 
				if (dataJsonCallIn.get("p_IdEmpresa")!=null) 	dataJsonCallIn.addProperty("p_IdEmpresa",SesionIdEmpresa); 
				if (dataJsonCallIn.get("IdUsuario")!=null) 		dataJsonCallIn.addProperty("IdUsuario",SesionIdUsuario); 
				if (dataJsonCallIn.get("p_IdUsuario")!=null) 	dataJsonCallIn.addProperty("p_IdUsuario",SesionIdUsuario); 
				CallTime=System.currentTimeMillis();	
				String parts[]=SrvNombre.split("@");			
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
						
			    JsonObject dataJsonCallOut=jcall.call(serviceConn,dataJsonCallIn);
			    
				if (dataJsonCallOut.get("p_out_ErrorCounter".toUpperCase())!=null) NameCountError="p_out_ErrorCounter";							
				if (dataJsonCallOut.get(NameCountError.toUpperCase())==null || dataJsonCallOut.get(NameCountError.toUpperCase()).getAsLong()>0) {
					serviceConn.rollback();
					
					CallTime=System.currentTimeMillis()-CallTime;
					TotalTime=System.currentTimeMillis()-TotalTime;
					Utils.registrarAuditoriaServicios(auditConn, IdServicio,SrvNombre, SesionIdUsuario, txid, "ERROR", null, null, null, null);
					dataJsonCallOut.addProperty("TotalExecutionTime",TotalTime);
					dataJsonCallOut.addProperty("SessionExecutionTime",SrvTime);	
					dataJsonCallOut.addProperty("CallExecutionTime",CallTime);	
					response.setStatus(200);
					response.getOutputStream().write(dataJsonCallOut.toString().getBytes("UTF-8"));	
					response.getOutputStream().flush();	
					
					return;
				}
				
				int skip = 0;
				int take = -1;
				try {
					skip = Integer.parseInt(request.getParameter("skip"));
					take = Integer.parseInt(request.getParameter("take"));
				}
				catch (Exception e) {
					
				}
				
				if (skip == 0 && take == -1) {
					logger.debug("Casting output to byte array");  
					byte[] t = dataJsonCallOut.toString().getBytes("UTF-8");
					logger.debug("Printing output");
					response.getOutputStream().write(t);	
					logger.debug("End Printing output");
				}
				else {
					JsonArray totalData = dataJsonCallOut.getAsJsonArray(pResulset);
					JsonArray data = new JsonArray();
					int total = totalData.size();
					
					if (take == -1) {
						take = total;
					}
					
					int start = skip > total ? total : skip;
					int end = skip + take > total ? total : skip + take;
					
					for (int i = start;i<end;i++) {
						data.add(totalData.get(i));
					}
					
					JsonObject obj = new JsonObject();
					obj.add("total", new JsonPrimitive(total));
					obj.add("data", data);
					response.getOutputStream().write(obj.toString().getBytes("UTF-8"));	
				}
				
				TotalTime=System.currentTimeMillis()-TotalTime;
				Utils.registrarAuditoriaServicios(auditConn, IdServicio,SrvNombre, SesionIdUsuario, txid, "FIN", null, null, null, null);
				response.setStatus(200);
				response.getOutputStream().flush();	
				serviceConn.commit();
				
				logger.info("Total Time="+TotalTime);									    
				return;
			}
			
			else if (SrvTipo.equalsIgnoreCase("j")) {
				
				CallTime=System.currentTimeMillis();	
				Object[] outObjects = callJavaService(dataJsonIn,SesionIdEmpresa,SesionIdUsuario,SrvNombre,serviceConn, null);
				
				JsonObject dataJsonCallOut = (JsonObject) outObjects[0];
				String[] metadata = (String[]) outObjects[1];
				
				if (dataJsonCallOut.get("p_out_ErrorCounter".toUpperCase())!=null) NameCountError="p_out_ErrorCounter";							
				if (dataJsonCallOut.get(NameCountError.toUpperCase())==null || dataJsonCallOut.get(NameCountError.toUpperCase()).getAsLong()>0) {
					serviceConn.rollback();
					
					CallTime=System.currentTimeMillis()-CallTime;
					TotalTime=System.currentTimeMillis()-TotalTime;
					Utils.registrarAuditoriaServicios(auditConn, IdServicio,SrvNombre, SesionIdUsuario, txid, "ERROR", metadata[0], null, null, null);
					dataJsonCallOut.addProperty("TotalExecutionTime",TotalTime);
					dataJsonCallOut.addProperty("SessionExecutionTime",SrvTime);	
					dataJsonCallOut.addProperty("CallExecutionTime",CallTime);	
					response.setStatus(200);
					response.getOutputStream().write(dataJsonCallOut.toString().getBytes("UTF-8"));	
					response.getOutputStream().flush();	
					
					return;
				}
				
				//for extras
				String[] extras = SrvExtras.split(",");
				for (String extra: extras) {
					if (extra.length() == 0) continue;
					Object[] outObjectsExtra = callJavaService(dataJsonIn,SesionIdEmpresa,SesionIdUsuario,extra,serviceConn,dataJsonCallOut);
					
					JsonObject dataJsonCallOutExtra = (JsonObject) outObjectsExtra[0];
					String[] metadataExtra = (String[]) outObjectsExtra[1];
					
					if (dataJsonCallOutExtra.get("p_out_ErrorCounter".toUpperCase())!=null) NameCountError="p_out_ErrorCounter";							
					if (dataJsonCallOutExtra.get(NameCountError.toUpperCase())==null || dataJsonCallOutExtra.get(NameCountError.toUpperCase()).getAsLong()>0) {
						serviceConn.rollback();
						
						CallTime=System.currentTimeMillis()-CallTime;
						TotalTime=System.currentTimeMillis()-TotalTime;
						Utils.registrarAuditoriaServicios(auditConn, IdServicio,SrvNombre, SesionIdUsuario, txid, "ERROR", metadataExtra[0], "EXTRA", null, null);
						dataJsonCallOutExtra.addProperty("TotalExecutionTime",TotalTime);
						dataJsonCallOutExtra.addProperty("SessionExecutionTime",SrvTime);	
						dataJsonCallOutExtra.addProperty("CallExecutionTime",CallTime);	
						response.setStatus(200);
						response.getOutputStream().write(dataJsonCallOutExtra.toString().getBytes("UTF-8"));	
						response.getOutputStream().flush();	
						
						return;
					}

				}
				
				CallTime=System.currentTimeMillis()-CallTime;
				TotalTime=System.currentTimeMillis()-TotalTime;
				Utils.registrarAuditoriaServicios(auditConn, IdServicio,SrvNombre, SesionIdUsuario, txid, "FIN", metadata[0], null, null, null);
				dataJsonCallOut.addProperty("TotalExecutionTime",TotalTime);
				dataJsonCallOut.addProperty("SessionExecutionTime",SrvTime);	
				dataJsonCallOut.addProperty("CallExecutionTime",CallTime);
				response.setStatus(200);
				response.getOutputStream().write(dataJsonCallOut.toString().getBytes("UTF-8")); 
				response.getOutputStream().flush();	
				serviceConn.commit();
				if (IdServicio != 600017) {
					logger.info("Total Time="+TotalTime);
				}
				return;
			} else if (SrvTipo.equalsIgnoreCase("o")) {
				String userpass="";
				String hanaServer=Utils.getConfiguracion(serviceConn, SesionIdEmpresa,"PLA.ServidorWebHana");	
				if (hanaServer.contains("@")) {
					userpass= hanaServer.substring(hanaServer.indexOf("//")+2, hanaServer.indexOf("@"));
					hanaServer=hanaServer.replace(userpass+"@", "");
				}
				String proxyURL=hanaServer+"/"+SrvNombre+"?$format=json";
				if  (request.getParameter("$inlinecount")!=null)  	proxyURL+="&$inlinecount="+URLEncoder.encode(request.getParameter("$inlinecount"),"UTF-8");
				if (request.getParameter("$select")!=null)  		proxyURL+="&$select="+URLEncoder.encode(request.getParameter("$select"),"UTF-8");
				if (request.getParameter("$filter")!=null)  		proxyURL+="&$filter="+URLEncoder.encode(request.getParameter("$filter"),"UTF-8");
				if (request.getParameter("$orderby")!=null) 		proxyURL+="&$orderby="+URLEncoder.encode(request.getParameter("$orderby"),"UTF-8");
				if (request.getParameter("$top")!=null) 			proxyURL+="&$top="+URLEncoder.encode(request.getParameter("$top"),"UTF-8");
				if (request.getParameter("$skip")!=null) 			proxyURL+="&$skip="+URLEncoder.encode(request.getParameter("$skip"),"UTF-8");						
				URL url = new URL(proxyURL);
				URLConnection urlc = url.openConnection();
				if (userpass.length()>0) urlc.setRequestProperty ("Authorization", "Basic " + new String(new Base64().encode(userpass.getBytes())));								
				BufferedInputStream in =  new BufferedInputStream(urlc.getInputStream());			     
		        byte[] buffer = new byte[4096];		        
		        int bytesRead;
		        while( ( bytesRead = in.read(buffer) ) != -1 ) {
		        	response.getOutputStream().write(buffer, 0, bytesRead);
		        	response.getOutputStream().flush();	
		        }		        
		        in.close();
				response.setStatus(200);	
				serviceConn.commit();
				return;				
			}else if(SrvTipo.equalsIgnoreCase("X")){
				JsonObject dataJsonCallIn	= dataJsonIn;
				if (dataJsonCallIn.get("IdEmpresa")!=null)   	dataJsonCallIn.addProperty("IdEmpresa",SesionIdEmpresa); 
				if (dataJsonCallIn.get("p_IdEmpresa")!=null) 	dataJsonCallIn.addProperty("p_IdEmpresa",SesionIdEmpresa); 
				if (dataJsonCallIn.get("IdUsuario")!=null) 		dataJsonCallIn.addProperty("IdUsuario",SesionIdUsuario); 
				if (dataJsonCallIn.get("p_IdUsuario")!=null) 	dataJsonCallIn.addProperty("p_IdUsuario",SesionIdUsuario); 
				CallTime=System.currentTimeMillis();	
				String parts[]=SrvNombre.split("@");			
				String pathBase=System.getProperty("user.home")+"/bin/";
				JavaView jcall = null;
				try {
					jcall = (JavaView) PluginLoader.getObject(pathBase,"PLUGIN_OPTIMUS.jar", parts[1]);
				}
				catch(NoSuchFileException e) {
					System.out.println("PLUGIN_OPTIMUS no encontrado, saltando");
				}
				if (jcall == null) {
					jcall = (JavaView) PluginLoader.getObject(pathBase,parts[0], parts[1]);
				}
				
				int skip = Integer.parseInt(Optional.ofNullable(request.getParameter("skip")).orElse("0"));
				int take = Integer.parseInt(Optional.ofNullable(request.getParameter("take")).orElse("1000000"));
				String sort = Optional.ofNullable(request.getParameter("sort")).orElse("");
				String filters = Optional.ofNullable(request.getParameter("filter")).orElse("");
			    JsonObject dataJsonCallOut=jcall.call(serviceConn,dataJsonCallIn, skip, take, sort, filters);
				
				if (dataJsonCallOut.get("p_out_ErrorCounter".toUpperCase())!=null) NameCountError="p_out_ErrorCounter";							
				if (dataJsonCallOut.get(NameCountError.toUpperCase())==null || dataJsonCallOut.get(NameCountError.toUpperCase()).getAsLong()>0) {
					serviceConn.rollback();
					
					CallTime=System.currentTimeMillis()-CallTime;
					TotalTime=System.currentTimeMillis()-TotalTime;
					Utils.registrarAuditoriaServicios(auditConn, IdServicio,SrvNombre, SesionIdUsuario, txid, "ERROR", null, null, null, null);

					dataJsonCallOut.addProperty("TotalExecutionTime",TotalTime);
					dataJsonCallOut.addProperty("SessionExecutionTime",SrvTime);	
					dataJsonCallOut.addProperty("CallExecutionTime",CallTime);	
					response.setStatus(200);
					response.getOutputStream().write(dataJsonCallOut.toString().getBytes("UTF-8"));	
					response.getOutputStream().flush();	
					
					return;
				}
				
				if (pResulset!=null) {
					JsonObject obj = new JsonObject();
					obj.add("data", dataJsonCallOut.getAsJsonArray(pResulset));
					obj.add("total", dataJsonCallOut.get("total"));
					response.getOutputStream().write(obj.toString().getBytes("UTF-8"));
										
					TotalTime=System.currentTimeMillis()-TotalTime;
					Utils.registrarAuditoriaServicios(auditConn, IdServicio,SrvNombre, SesionIdUsuario, txid, "FIN", null, null, null, null);

					response.setStatus(200);
					response.getOutputStream().flush();	
					serviceConn.commit();
					
					logger.info("Total Time="+TotalTime);									    
					return;
				}
				
				CallTime=System.currentTimeMillis()-CallTime;
				TotalTime=System.currentTimeMillis()-TotalTime;
				Utils.registrarAuditoriaServicios(auditConn, IdServicio,SrvNombre, SesionIdUsuario, txid, "FIN", null, null, null, null);
				dataJsonCallOut.addProperty("TotalExecutionTime",TotalTime);
				dataJsonCallOut.addProperty("SessionExecutionTime",SrvTime);	
				dataJsonCallOut.addProperty("CallExecutionTime",CallTime);			
				response.setStatus(200);
				response.getOutputStream().write(dataJsonCallOut.toString().getBytes("UTF-8")); 
				response.getOutputStream().flush();	
				serviceConn.commit();
				logger.info("Total Time="+TotalTime);
				return;
				
			}else {
				serviceConn.rollback();
				response.setStatus(401);
				response.getOutputStream().print("401 - Invalid Service");	
				response.getOutputStream().flush();	
				return;								
			}
		}catch (Exception e) {			
			logger.fatal("Error",e);
			response.setStatus(500);
			long 	CodigoError=-1;			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);	
			String 	PilaError=sw.toString();	
			PilaError=PilaError.replace("\r", "");
			try {
				//Clean cache
				if (SrvNombre!=null && SrvNombre.length()>0) {
					Utils.delDefinitionCache(SrvNombre);					
				}
				//Log Report
				if (serviceConn != null) {
					serviceConn.rollback();
					if (SesionIdEmpresa!=0 && SesionIdUsuario!=0) {
						try {
							CodigoError=Utils.addErrorLog(serviceConn, SesionIdEmpresa, SesionIdUsuario, SesionModulo, PilaError, SesionIdCliente, IdServicio);
							serviceConn.commit();
						}catch(Exception er) {}
					}
				}
			}finally {			
				JsonObject jlog=new JsonObject();
				if (muestraLog.equals("S")) {
					jlog.addProperty("CODIGO", CodigoError);
					jlog.addProperty("PILA", PilaError);
					jlog.addProperty("SP", IdServicio);
					
				}
				else {
					jlog.addProperty("CODIGO", CodigoError);
					jlog.addProperty("PILA", "INTERNAL ERROR #"+CodigoError);
					jlog.addProperty("SP", IdServicio);
				}				
				response.getOutputStream().write(jlog.toString().getBytes("UTF-8"));	
				response.getOutputStream().flush();
				Utils.registrarAuditoriaServicios(auditConn, IdServicio,SrvNombre, SesionIdUsuario, txid, "ERROR_FATAL", null, null, null, null);

			}
		}finally {
			
			ThreadContext.clearMap();
			
			if (serviceConn != null) {
				try {serviceConn.rollback(); }
				catch(Exception er) {}
				serviceConn.close();
			}
			
			if (auditConn != null) {
				try {auditConn.rollback(); }
				catch(Exception er) {}
				auditConn.close();
			}
			
			if(noTraceService.contains(IdServicio)) {
				//no guardar traces para servicio consulta notificaciones ni consulta estado modulos.
				tracer.activeSpan().setTag(DDTags.MANUAL_DROP, true);
			}
				

			tracer.activeSpan().finish();
		}				
	}
	
	Object[] callJavaService(JsonObject dataJsonIn, long sesionIdEmpresa, long sesionIdUsuario,String srvNombre, Connection conn, JsonObject dataJsonCallOutOriginal) throws Exception {
		JsonObject dataJsonCallIn	= dataJsonIn;
		String[] outMetadata = new String[4];
		if (dataJsonCallIn.get("IdEmpresa")!=null)   	dataJsonCallIn.addProperty("IdEmpresa",sesionIdEmpresa); 
		if (dataJsonCallIn.get("p_IdEmpresa")!=null) 	dataJsonCallIn.addProperty("p_IdEmpresa",sesionIdEmpresa); 
		if (dataJsonCallIn.get("IdUsuario")!=null) 		dataJsonCallIn.addProperty("IdUsuario",sesionIdUsuario); 
		if (dataJsonCallIn.get("p_IdUsuario")!=null) 	dataJsonCallIn.addProperty("p_IdUsuario",sesionIdUsuario); 
		
		String parts[]=srvNombre.split("@");			
		String pathBase=System.getProperty("user.home")+"/bin/";
		JavaCall jcall = null;
		JavaCallExtra jcallextra = null;
		Object call = null;
		JsonObject dataJsonCallOut = null;
		
		try {
			call = PluginLoader.getObject(pathBase,"PLUGIN_OPTIMUS.jar", parts[1]);
		}
		catch(NoSuchFileException e) {
			System.out.println("PLUGIN_OPTIMUS no encontrado, saltando");
		}
		
		if (call instanceof JavaCall) {
			jcall = (JavaCall)call;
			dataJsonCallOut=jcall.call(conn,dataJsonCallIn);
		}
		else if (call instanceof JavaCallExtra) {
			jcallextra = (JavaCallExtra)call;
			dataJsonCallOut=jcallextra.call(conn,dataJsonCallIn,dataJsonCallOutOriginal);
		}
		
		if (jcall instanceof JavaCallMetadata) {
			outMetadata = ((JavaCallMetadata) jcall).getMetadata();
		}
		return new Object[] {dataJsonCallOut,outMetadata};
	}
	
	
	
	
}
