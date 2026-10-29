package cl.bcs.plataforma.services;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang.StringEscapeUtils;

//import com.crystaldecisions.sdk.occa.report.lib.ReportSDKException;
import com.google.gson.Gson;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

/***
 * 
 * @author cstange
 *
 */
@WebServlet("/template")
public class TemplateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	private final Configuration configuration;

			
    /**
     * @see HttpServlet#HttpServlet()
     */
    public TemplateServlet() {
    	super();
    	configuration= new Configuration(new Version("2.3.23"));
    	configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    	configuration.setLogTemplateExceptions(true);
    }	
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */    
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
	 * @throws ReportSDKException 
	 */
	private void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
		try {
        	Gson gson=new Gson();            
	        //get parameters			        
	        String templateString=request.getParameter("template");
	        String contextString=request.getParameter("context");
	        System.out.println(StringEscapeUtils.unescapeHtml(templateString));
	        System.out.println(" ");
	        System.out.println(contextString);
	        @SuppressWarnings("unchecked")
			Map<String, String> model = gson.fromJson(contextString, HashMap.class);
	        //set template
	        try {
	        	 Template template;
	        	 final StringWriter out = new StringWriter();
				try {
					template = parseTemplate(StringEscapeUtils.unescapeJava(templateString).replace("!}", "}").replace("}", "!}"), configuration);
		            template.process(model, out);
				} catch (freemarker.core.InvalidReferenceException | IOException e) {
					e.printStackTrace();
					response.setStatus(500);
					return;
				}
				String output = "";
				if(StringEscapeUtils.unescapeHtml(out.toString()).startsWith("\"") && StringEscapeUtils.unescapeHtml(out.toString()).endsWith("\"")){
					
					  output = gson.toJson(out.toString().substring(1, out.toString().length()-1));
 
						
				}else{
					  output = gson.toJson(out.toString());	 
				}	 
				
 	    	    response.setContentType("application/html; charset=UTF-8");
	    	    response.setCharacterEncoding("UTF-8");				    	    
				response.setStatus(200);
				response.getOutputStream().print(StringEscapeUtils.unescapeJava(output));	 
				response.getOutputStream().flush();	
	        } catch (final TemplateException e) {
				e.printStackTrace();
				response.setStatus(500);
				return;
	        }
		}catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);
		}finally {	
		}	        			
	}	
	
    private Template parseTemplate(final String rawTemplate, final Configuration configuration) throws IOException {
        // this fixes list
        final String template = rawTemplate
                .replaceAll("&lt;#else&gt;", "<#else>")
                .replaceAll("&lt;#sep&gt;", "<#sep>")
                .replaceAll("&lt;#/sep&gt;", "</#sep>")
                .replaceAll("&lt;#list (.*?)&gt;", "<#list $1>")
                .replaceAll("&lt;#/list&gt;", "</#list>");
        return new Template("name", new StringReader(template), configuration);
    }
    
    
}
