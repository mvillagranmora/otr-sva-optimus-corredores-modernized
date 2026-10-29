package cl.bcs.plataforma.services;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;

import com.crystaldecisions.sdk.occa.report.lib.ReportSDKException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import cl.bcs.nuam.StoredProcedure;
import cl.bcs.plataforma.report.api.ReportDocument;
import cl.bcs.utils.ListMapResultSet;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/***
 * 
 * @author cstange
 *
 */
@WebServlet("/report")
@MultipartConfig
// @WebServlet(name="TestServlet", urlPatterns={"/path", "/alt"})
public class CallReportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String tipoAmbiente = "";
	
	@Resource(name = "jdbc/hanaDB")
	DataSource dataSource;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public CallReportServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			performTask(request, response, "get");
		} catch (Exception er) {
			er.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			performTask(request, response, "post");
		} catch (Exception er) {
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
	 * @throws ReportSDKException
	 */
	@SuppressWarnings("unchecked")
	private void performTask(HttpServletRequest request, HttpServletResponse response, String requestType)
			throws ServletException, IOException, SQLException {
		final String serverURL = request.getRequestURL().toString().replace(request.getRequestURI(), "");
		String pTokenSession = "";
		long SesionIdEmpresa = 0;
		long SesionIdUsuario = 0;
		boolean debug = false;
		String debugServiceURL = "";
		/*
		long SesionIdCliente = 0;
		String SesionModulo = "PLA";
		String tipoCALL = "";
		String spCALL = "";
		long TotalTime = System.currentTimeMillis();
		long CallTime = 0;*/
		
	//	Enumeration<String> parameterNames = request.getParameterNames();
		
	//	List<String> parameterList = Collections.list(request.getParameterNames());
		/*
		String paramName = parameterNames.hasMoreElements() ? parameterNames.nextElement() : null;
		
		if( paramName == null || parameterNames.hasMoreElements()) {
			response.setStatus(404);
			response.getOutputStream().print("404 - NOT FOUND");
			response.getOutputStream().flush();
			return;
		}*/
		

		/*while (parameterNames.hasMoreElements()) {

			String paramName1 = parameterNames.nextElement();

			String[] paramValues = request.getParameterValues(paramName);
			for (int i = 0; i < paramValues.length; i++) {
				 String paramValue = paramValues[i];
			}

		}*/

		try {
			// get connection
			// conn = dataSource.getConnection();
			// conn.setAutoCommit(false);

			// check token
			pTokenSession = request.getHeader("X-SESSION-TOKEN");
			if (pTokenSession == null) {
				response.setStatus(401);
				response.getOutputStream().print("401 - NOT AUTHORIZED");
				response.getOutputStream().flush();
				return;
			}
			
			// get connection & session
			try (Connection conn = dataSource.getConnection();) {
				Statement st = conn.createStatement();
				boolean isPostgre = SQL.isPostgresConnection(conn);
				if (!isPostgre)
					st.executeUpdate("SET TRANSACTION AUTOCOMMIT DDL OFF");
			
				JsonObject dataJSOut = Utils.getSessionData(conn, pTokenSession);
				if (dataJSOut.get("p_out_ErroresContador".toUpperCase()) == null
						|| dataJSOut.get("p_out_ErroresContador".toUpperCase()).getAsLong() > 0) {
					conn.rollback();
					response.setStatus(401);
					response.getOutputStream().print("401 - NOT AUTHORIZED");
					response.getOutputStream().flush();
					return;
				}
				SesionIdEmpresa = dataJSOut.get("p_out_IdEmpresa".toUpperCase()).getAsLong();
				SesionIdUsuario = dataJSOut.get("p_out_IdUsuario".toUpperCase()).getAsLong();
			//	String SesionCodigoUsuario = dataJSOut.get("p_out_NombreUsuario".toUpperCase()).getAsString();
				
				if (tipoAmbiente.length() == 0)
					tipoAmbiente = Utils.getConfiguracion(conn, SesionIdEmpresa, "PLA.Ambiente");
			
			}
			if (request.getParameter("update") != null) {
				JsonObject jsonOut = new JsonObject();
				
				String reportId = request.getParameter("reportId");
				String reportName = request.getParameter("reportName");
				
		//		String ds = request.getParameter("datasource");
		//		String existFile = request.getParameter("file");
				
				int reportVersion = Integer.parseInt(request.getParameter("reportVersion"));
				long companyId = SesionIdEmpresa; //Integer.parseInt(request.getParameter("companyId"));
				
				Part reportFile = request.getPart("data");
				
				ReportDocument reportUpdate = new ReportDocument(null, dataSource);
				if (!reportUpdate.readDocument(reportId)) {
					response.setStatus(404);
					response.getOutputStream().print("404 - NOT FOUND");
					response.getOutputStream().flush();
					return;
				}
				
				reportUpdate.updateDocument(reportId, reportName, reportFile.getInputStream(), "", reportVersion, companyId);
				
				jsonOut.addProperty("result", "OK");				
				response.setStatus(200);
				response.setContentType("application/json");
				response.getOutputStream().write(jsonOut.toString().getBytes("UTF-8")); 
				response.getOutputStream().flush();
				return;
			}
			
			if (request.getParameter("upload") != null) {
				JsonObject jsonOut = new JsonObject();
				
				String reportName = request.getParameter("reportName");
				
				int reportVersion = Integer.parseInt(request.getParameter("reportVersion"));
				long companyId = SesionIdEmpresa;
				
				Part reportFile = request.getPart("data");
				ReportDocument reportUpload = new ReportDocument(null, dataSource);
				String reportId = reportUpload.uploadDocument(reportName, reportFile.getInputStream(), reportVersion, companyId);
				
				jsonOut.addProperty("result", "OK");
				jsonOut.addProperty("reportId", reportId);
				response.setStatus(200);
				response.setContentType("application/json");
				response.getOutputStream().write(jsonOut.toString().getBytes("UTF-8")); 
				response.getOutputStream().flush();
				/*
				response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
				IOUtils.copy(inStreamRpt, response.getOutputStream());
				response.getOutputStream().flush();
				*/
				return;
			}
			
			// get body data
			StringBuilder sb = new StringBuilder();
			String s;
			while ((s = request.getReader().readLine()) != null) {
				sb.append(s);
			}
			String bodyParms = sb.toString();
			if (bodyParms == null || bodyParms.length() == 0) {
				bodyParms = "{ }";
			}
			
			JsonParser parser = new JsonParser();
			JsonObject dataJsonIn = parser.parse(bodyParms).getAsJsonObject();
			
			debug = dataJsonIn.get("debug") != null ? dataJsonIn.get("debug").getAsBoolean() : false;
			if (debug) {
				debugServiceURL = dataJsonIn.get("_serviceURL") != null ? dataJsonIn.get("_serviceURL").getAsString() : null;  
			}
			
			String reportId = dataJsonIn.get("reportId") == null ? "" : dataJsonIn.get("reportId").getAsString();
			
			
			
			
			/*
			 * Get Report Template file (*.rpt)
			 */
			if (request.getParameter("rpt") != null) {
				ReportDocument reportRpt = new ReportDocument(null, dataSource);
				InputStream streamRpt = reportRpt.getDocument(reportId);
				
				if (streamRpt != null) {
					String fileName = reportRpt.getDocumentName() + "_" + reportRpt.getDocumentId() + ".rpt";
					response.setStatus(200);
					response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
					IOUtils.copy(streamRpt, response.getOutputStream());
					response.getOutputStream().flush();
					return;
				} else {
					// Report Template not found
					response.setStatus(404);
					response.getOutputStream().print("404 - NOT FOUND");
					response.getOutputStream().flush();
					return;
				}
			}
			
			if (request.getParameter("file") != null) {
				//return;
			}
			
			
			
			

			
			String exportFormat = dataJsonIn.get("exportFormat").getAsString().toLowerCase().trim();
			
			JsonObject configuration = dataJsonIn.get("configuration").getAsJsonObject();
			JsonObject mainReport = configuration.get("mainReport").getAsJsonObject();
			JsonArray subReports = configuration.get("subReports").getAsJsonArray();
			/*			
			if (tipoAmbiente.length() == 0)
				tipoAmbiente = Utils.getConfiguracion(conn, schema, SesionIdEmpresa, "Ambiente");*/
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			//parameters.put("connection", conn);
			parameters.put("dataJsonIn", dataJsonIn);
			parameters.put("schema", null);
			parameters.put("token", pTokenSession);
			parameters.put("companyId", SesionIdEmpresa);
			parameters.put("userId", SesionIdUsuario);
			parameters.put("server", serverURL);
			parameters.put("debugServiceURL", debugServiceURL);
			
			ReportDocument reportTest = new ReportDocument(null, dataSource);
			if (!reportTest.readDocument(reportId)) {
				response.setStatus(404);
				response.getOutputStream().print("404 - NOT FOUND");
				response.getOutputStream().flush();
				return;
			}
			
			// set main parameters
			if (mainReport.get("parameters") == null) {
				response.setStatus(404);
				response.getOutputStream().print("404 - NOT FOUND");
				response.getOutputStream().flush();
				return;
			}
			if (mainReport.get("parameters").isJsonArray()) {
				JsonArray mainReportParams = mainReport.get("parameters").getAsJsonArray();
				for ( int i = 0; i < mainReportParams.size(); i++ ) {
					JsonObject obj = mainReportParams.get(i).getAsJsonObject();
					JsonElement e = null;
					String type = (e = obj.get("type")) == null ? "" : e.getAsString();
					String name = (e = obj.get("name")) == null ? "" : e.getAsString();
					String data = (e = obj.get("data")) == null ? "" : e.getAsString();
					if (type.equals("") || name.equals("") || data.equals("")) {
						response.setStatus(404);
						response.getOutputStream().print("404 - NOT FOUND");
						response.getOutputStream().flush();
						return;
					}
					
					switch (type) {
					case "image":
						reportTest.setDocumentImage(data, name);
						break;
					case "pdf417":
						reportTest.setDocumentBarcodeImage(type, data, name);
						break;
					}
						
				}
				
			} else if (mainReport.get("parameters").isJsonObject()) {
				//TODO
				
			} else {
				response.setStatus(404);
				response.getOutputStream().print("404 - NOT FOUND");
				response.getOutputStream().flush();
				return;
			}

			// set main report
			if (mainReport.get("data") == null) {
				response.setStatus(404);
				response.getOutputStream().print("404 - NOT FOUND");
				response.getOutputStream().flush();
				return;
			}
			if (mainReport.get("data").isJsonObject()) {
				JsonObject data = mainReport.get("data").getAsJsonObject();
				String serviceId = data.get("serviceId").getAsString();
				JsonObject serviceData = data.get("serviceData").getAsJsonObject();
				String serviceResponse = data.get("serviceResponse") == null ? "" : data.get("serviceResponse").getAsString();
				
			//	for (Entry<String, JsonElement> entry : serviceData.entrySet())
			//		System.out.println(entry.getKey() + " : " + entry.getValue());
				
				parameters.put("serviceId", serviceId);
				parameters.put("serviceData", serviceData);
				parameters.put("serviceResponse", serviceResponse);
				
				ResultSet rs = getServiceData(parameters);
				if (rs == null) {
					response.setStatus(404);
					response.getOutputStream().print("404 - NOT FOUND");
					response.getOutputStream().flush();
					return;
				}
				reportTest.setDocumentResultSet(rs);
				
			} else if (mainReport.get("data").isJsonArray()) {
				JsonArray dataarr = mainReport.get("data").getAsJsonArray();
				List<Object> nList = new ArrayList<Object>();
				for (int j = 0; j < dataarr.size(); j++) {
					Type type = new TypeToken<Map<String, Object>>(){}.getType();
					Map<String, Object> map = (Map<String, Object>) new Gson().fromJson(dataarr.get(j).getAsJsonObject(), type);
					nList.add(map);
				}
				ListMapResultSet rs = new ListMapResultSet();
				if(nList.size()>0){
					rs.populate(nList);
					reportTest.setDocumentResultSet(rs);
				}
			} else { //TODO: Remove
				response.setStatus(404);
				response.getOutputStream().print("404 - NOT FOUND");
				response.getOutputStream().flush();
				return;/*
				String mainReportQuery = mainReport.get("data").getAsString();
				reportTest.setDocumentResultSet(mainReportQuery);*/
			}

			// set subs reports

			Map<String, String> subdocsQuery = new HashMap<String, String>();
			Map<String, ResultSet> subdocsResult = new HashMap<String, ResultSet>();
			for (int i = 0; i < subReports.size(); i++) {
				JsonObject subReport = subReports.get(i).getAsJsonObject();
				if (subReport.get("reportName") != null) {
					String subReportname = subReport.get("reportName").getAsString();
					if (subReport.get("data").isJsonObject()) {
						JsonObject data = subReport.get("data").getAsJsonObject();
						String serviceId = data.get("serviceId").getAsString();
						JsonObject serviceData = data.get("serviceData").getAsJsonObject();
						String serviceResponse = data.get("serviceResponse") == null ? "" : data.get("serviceResponse").getAsString();
						
						parameters.put("serviceId", serviceId);
						parameters.put("serviceData", serviceData);
						parameters.put("serviceResponse", serviceResponse);
						
						ResultSet rs = getServiceData(parameters);
						if (rs == null) {
							response.setStatus(404);
							response.getOutputStream().print("404 - NOT FOUND");
							response.getOutputStream().flush();
							return;
						}
						//reportTest.setDocumentResultSet(rs);
						subdocsResult.put(subReportname, rs);
						
					} else if (subReport.get("data").isJsonArray()) {
						JsonArray dataarr = subReport.get("data").getAsJsonArray();
						List<Object> nList = new ArrayList<Object>();
						for (int j = 0; j < dataarr.size(); j++) {
							Type type = new TypeToken<Map<String, Object>>() {}.getType();
							Map<String, Object> map = (Map<String, Object>) new Gson().fromJson(dataarr.get(j).getAsJsonObject(), type);
							nList.add(map);
						}
						ListMapResultSet rs = new ListMapResultSet();
						rs.populate(nList);
						
						subdocsResult.put(subReportname, rs);
					} else { //TODO: Remove
						response.setStatus(404);
						response.getOutputStream().print("404 - NOT FOUND");
						response.getOutputStream().flush();
						return;/*
						String subReportQuery = subReport.get("data").getAsString();
						subdocsQuery.put(subReportname, subReportQuery);*/
					}
				}
			}

			if (subdocsResult.size() > 0) {
				reportTest.setSubdocumentsResultSet(subdocsResult);
			} else if (subdocsQuery.size() > 0) {
				reportTest.setSubdocumentsResultSetFromQuery(subdocsQuery);
			}

			// set format
			InputStream inStreamRpt = null;
			String extension = ""; // TODO: API needs file extension
			switch (exportFormat) {
			case "pdf":
				inStreamRpt = reportTest.exportDocument(exportFormat);
				extension = "PDF";
				break;
			case "excel":
				inStreamRpt = reportTest.exportDocument(exportFormat);
				extension = "XLS";
				break;
			case "word":
				inStreamRpt = reportTest.exportDocument(exportFormat);
				extension = "DOC";
				break;
			case "rtf":
				inStreamRpt = reportTest.exportDocument(exportFormat);
				extension = "RTF";
				break;
			case "txt":
				inStreamRpt = reportTest.exportDocument(exportFormat);
				extension = "TXT";
				break;
			}
			if (inStreamRpt != null) {
				String fileName = reportTest.getDocumentName() + "_" + reportTest.getDocumentId() + "." + extension;
				response.setStatus(200);
				response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
				IOUtils.copy(inStreamRpt, response.getOutputStream());
				response.getOutputStream().flush();
			} else {
				// TODO: ERROR
				response.setStatus(500);
				response.getOutputStream().print("500 - INTERNAL ERROR");
				response.getOutputStream().flush();
			}			
			
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			response.setStatus(500);
			
			if (debug) {
				response.getOutputStream().print("500 - INTERNAL ERROR \n\n" + sw.toString());
				response.getOutputStream().flush();
			}
			else {
				response.getOutputStream().print("500 - INTERNAL ERROR");
				response.getOutputStream().flush();
			}
			
		}
		
	}
	
	/**
	 * 
	 * @param serviceId
	 * @return Result set
	 */
	@SuppressWarnings("unchecked")
	private ResultSet getServiceData(Map <String, Object> params) {
		String server; if((server = (String)params.get("server")) == null) return null;
		String token; if((token =(String)params.get("token")) == null) return null;
		JsonObject dataJsonIn; if ( (dataJsonIn =(JsonObject)params.get("dataJsonIn")) == null) return null;
		String serviceId; if ( (serviceId =(String)params.get("serviceId")) == null) return null;
		JsonObject serviceData; if ( (serviceData =(JsonObject)params.get("serviceData")) == null) return null;
		String serviceResponse = (String) params.get("serviceResponse");
		long companyId = (long) params.get("companyId"); if(params.get("companyId") == null) return null;
		long userId = (long) params.get("userId"); if(params.get("userId") == null) return null;
		
		String debugServiceURL = (String)params.get("debugServiceURL");
		
		String select = "";
		JsonElement el;
		Map <String, Integer> schemaTypes = null;
		JsonArray schemaArray = (el = serviceData.get("$schema")) == null || !el.isJsonArray() ? null : el.getAsJsonArray();
		if (schemaArray != null) {
			schemaArray = el.getAsJsonArray();
			schemaTypes = new HashMap<String, Integer>();
			
			int l;
			for (int i = 0; i < (l = schemaArray.size()); i++) {
				JsonObject obj = schemaArray.get(i).getAsJsonObject();
				select += ("\"" + obj.get("field").getAsString() + "\"");
				if (i < l - 1) {
					select += ",";
				}
				switch (obj.get("type").getAsString()) {
				case "number":
					schemaTypes.put(obj.get("field").getAsString(), Types.DECIMAL);
					break;
				case "date":
					schemaTypes.put(obj.get("field").getAsString(), Types.DATE);
					break;
				case "datetime":
					schemaTypes.put(obj.get("field").getAsString(), Types.TIMESTAMP);
					break;
				case "string":
					schemaTypes.put(obj.get("field").getAsString(), Types.VARCHAR);
					break;
				case "blob":
					schemaTypes.put(obj.get("field").getAsString(), Types.BLOB);
					break;
				default:
					schemaTypes.put(obj.get("field").getAsString(), Types.JAVA_OBJECT);
					break;
				}
			}
		}
		
		ListMapResultSet rs;
		//KProcedure procedure = null;
		//JsonObject procedureResult = null;
		JsonArray results = null;
		//Map <String, Integer> schemaTypes = null;
		try (Connection conn = dataSource.getConnection()){
			JsonObject dataSRVIn = new JsonObject();
			dataSRVIn.addProperty("p_IdEmpresa", companyId);
			dataSRVIn.addProperty("p_IdUsuario", userId);
			dataSRVIn.addProperty("p_Hash", serviceId);
			StoredProcedure kHashSrv = new StoredProcedure("BSCL.Plataforma.BD.Servicios::ACC.ValidaHashServicioUsuario");
			
			JsonObject dataSRVOut = kHashSrv.call(conn, dataSRVIn);

			/*if (dataSRVOut.get("p_out_PermiteUso".toUpperCase()) == null
					|| dataSRVOut.get("p_out_PermiteUso".toUpperCase()).getAsString().equals("S") == false) {
				conn.rollback();
				response.setStatus(500);
				response.getOutputStream().print("500 - Invalid Service");
				response.getOutputStream().flush();
				return;
			}*/
			
			String tipoCALL = dataSRVOut.get("p_out_Tipo".toUpperCase()).getAsString();
			String spCALL = dataSRVOut.get("p_out_Servicio".toUpperCase()).getAsString();

			if (tipoCALL.equalsIgnoreCase("p")) {
				// call procedure
				JsonObject dataJsonCallIn = dataJsonIn;
				if (dataJsonCallIn.get("IdEmpresa") != null)
					dataJsonCallIn.addProperty("IdEmpresa", companyId);
				if (dataJsonCallIn.get("p_IdEmpresa") != null)
					dataJsonCallIn.addProperty("p_IdEmpresa", companyId);
				if (dataJsonCallIn.get("IdUsuario") != null)
					dataJsonCallIn.addProperty("IdUsuario", userId);
				if (dataJsonCallIn.get("p_IdUsuario") != null)
					dataJsonCallIn.addProperty("p_IdUsuario", userId);
				
			//	CallTime = System.currentTimeMillis();
				
			//	procedure = new KProcedure("BSCL", "POC.ARQ.BD::TestCR");
				StoredProcedure procedure = new StoredProcedure(spCALL);
				JsonObject procedureResult = procedure.call(conn, serviceData);
				
				
			//	CallTime = System.currentTimeMillis() - CallTime;
				//System.out.println(procedureResult.toString());
				
				//intentamos sacar el primer elemento, ya sea si viene con nombre OUT o desde un select
				Set<Entry<String, JsonElement>> objs = procedureResult.entrySet();
				Entry<String, JsonElement> first = objs.iterator().next();
				//System.out.println("Objs:" + objs);
				if (!first.getKey().contains("ResultSet") || objs.size() != 1) {
					results = first.getValue().getAsJsonArray();
				}
				else {
					results = procedureResult.get("ResultSet_0").isJsonArray() ? procedureResult.get("ResultSet_0").getAsJsonArray() : null;
				}
				
				
				if(serviceResponse.equals("json") && results.size() == 1 ) {
					String dataBase64 = "";
					for (Entry<String, JsonElement> entry : results.get(0).getAsJsonObject().entrySet()) {
						dataBase64 = entry.getValue().getAsString();
						break;
					}
						//		System.out.println(entry.getKey() + " : " + entry.getValue());
						
				//	dataBase64 = results.get(0).getAsJsonObject().get("JsonData").getAsString();
					byte[] dataByte = Base64.getDecoder().decode(dataBase64);
					String jsonData = new String(dataByte);					
					JsonElement dataJsonElement = (new JsonParser()).parse(jsonData);
					results = dataJsonElement.getAsJsonArray();
				}
				
				conn.commit();
				// return;

			} 
			else if (tipoCALL.equalsIgnoreCase("q")) {
				String serviceURL = server + "/BSCL/Plataforma/GWJAVA/PLA/call?t=" + serviceId;
				if (debugServiceURL != null && !debugServiceURL.equals("")){
					serviceURL = debugServiceURL + "/call?t=" + serviceId;
				}
				//serviceURL = "http://localhost:8085/BSCL/Plataforma/GWJAVA/PLA/call?t=" + serviceId;
				
				//JsonElement e;
				String proxyURL = serviceURL;
				proxyURL += (el = serviceData.get("$select")) == null ? "" : "&$select=" + URLEncoder.encode(el.getAsString(),"UTF-8");
				/*
				if ( (e = serviceData.get("$schema")) == null || !e.isJsonArray() ){
					return null;
				}*/
				/*
				JsonArray schemaArray = el.getAsJsonArray();
				schemaTypes = new HashMap<String,Integer>();
				String select = "";
				int l;
				for (int i = 0; i < ( l = schemaArray.size()); i++) {
					JsonObject obj = schemaArray.get(i).getAsJsonObject();
					select += ("\"" + obj.get("field").getAsString() + "\"");
					if (i < l-1 ) {
						select += ",";
					}
					switch(obj.get("type").getAsString()) {
					case "number" :
						schemaTypes.put(obj.get("field").getAsString(), Types.DECIMAL);
						break;
					case "date" :
						schemaTypes.put(obj.get("field").getAsString(), Types.DATE);
						break;
					case "datetime" :
						schemaTypes.put(obj.get("field").getAsString(), Types.TIMESTAMP);
						break;
					case "string" :
						schemaTypes.put(obj.get("field").getAsString(), Types.VARCHAR);
						break;
					default :
						schemaTypes.put(obj.get("field").getAsString(), Types.JAVA_OBJECT);
						break;
					}
				}*/
				
				proxyURL += "&$select=" + URLEncoder.encode(select,"UTF-8");				
				proxyURL += (el = serviceData.get("$inlinecount")) == null ? "" : "&$inlinecount" + URLEncoder.encode(el.getAsString(),"UTF-8");
				proxyURL += (el = serviceData.get("$format")) == null ? "" : "&$format" + URLEncoder.encode(el.getAsString(),"UTF-8");
				proxyURL += (el = serviceData.get("$top")) == null ? "" : "&$top" + URLEncoder.encode(el.getAsString(),"UTF-8");
				proxyURL += (el = serviceData.get("$skip")) == null ? "" : "&$skip" + URLEncoder.encode(el.getAsString(),"UTF-8");
				proxyURL += (el = serviceData.get("$callback")) == null ? "" : "&$callback" + URLEncoder.encode(el.getAsString(),"UTF-8");
				proxyURL += (el = serviceData.get("$filter")) == null ? "" : "&$filter" + URLEncoder.encode(el.getAsString(),"UTF-8");
				proxyURL += (el = serviceData.get("$orderby")) == null ? "" : "&$orderby" + URLEncoder.encode(el.getAsString(),"UTF-8");
				proxyURL += (el = serviceData.get("$handler")) == null ? "" : "&$handler" + URLEncoder.encode(el.getAsString(),"UTF-8");
				
				URL url = new URL(proxyURL);
				URLConnection uc = url.openConnection();
				uc.setRequestProperty("X-SESSION-TOKEN", token);
				System.out.println("_> getServiceData proxyURL: " + proxyURL);
				BufferedInputStream in =  new BufferedInputStream(uc.getInputStream());			     
		        
				byte[] buffer = new byte[4096];		        
		        int bytesRead;
		        StringBuilder sb = new StringBuilder();
		        
		        while( ( bytesRead = in.read(buffer) ) != -1 ) {
		        	sb.append(new String(buffer,0,bytesRead));
		        }
		        String response = sb.toString();
		        if ( response == null || response.length() == 0) {
		        	response = "{}";
		        }
		        JsonParser parser = new JsonParser();
		        JsonObject jsonResponse = parser.parse(response).getAsJsonObject();
		        
		        JsonObject d = jsonResponse.get("d") == null ? null : jsonResponse.get("d").getAsJsonObject();
		        results = (d == null || d.get("results") == null) ? null : d.get("results").getAsJsonArray();
		        
				conn.commit();
			}
			
			rs = new ListMapResultSet();
			
			if (results != null) {
				Pattern pFixDate = Pattern.compile("(\\/Date\\()([0-9]*)(\\)\\/)");
				String val = null;
				Matcher m1 = null;
				List<Object> nList = new ArrayList<Object>();
				for (int j = 0; j < results.size(); j++) {
					Type type = new TypeToken<Map<String, Object>>() {}.getType();
					Map<String, Object> map = (Map<String, Object>) new Gson().fromJson(results.get(j).getAsJsonObject(), type);
					
				//	if (tipoCALL.equalsIgnoreCase("q")) {
					for (Map.Entry<String, Object> entry : map.entrySet()) {
						if (schemaTypes != null && schemaTypes.containsKey(entry.getKey())) {
							switch (schemaTypes.get(entry.getKey())) {
							case Types.DATE:
								val = entry.getValue().toString();
								m1 = pFixDate.matcher(val);
								if (m1.find()) {
									long l = Long.valueOf(m1.group(2)).longValue();
									entry.setValue((new java.sql.Date(l)).toString());
								}
								break;
							case Types.TIMESTAMP:
								val = entry.getValue().toString();
								m1 = pFixDate.matcher(val);
								if (m1.find()) {
									long l = Long.valueOf(m1.group(2)).longValue();
									entry.setValue((new java.sql.Timestamp(l)).toString());
								}
								break;
							case Types.BLOB:
								val = entry.getValue().toString();
								byte[] dataByte = Base64.getDecoder().decode(val);
								entry.setValue(dataByte);
								break;
							}
						}
					}
				//	}
					
					nList.add(map);
				}

				rs.populate(nList);
				return (ResultSet) rs;
			}
			
			conn.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

}
