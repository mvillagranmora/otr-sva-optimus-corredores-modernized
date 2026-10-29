package cl.bcs.scheduler.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import cl.bcs.nuam.StoredProcedureException;
import cl.bcs.utils.Utils;

/**
 * Servlet implementation class
 */
@WebServlet("/admin")
public class WebAdmin extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    @Resource(name="jdbc/hanaDB") DataSource dataSource;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WebAdmin() {
        super();
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
	 * @throws KProcedureException 
	 * @throws ReportSDKException 
	 */
	private void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, SQLException, IOException, StoredProcedureException {		
		response.setContentType("application/plain");	
		Connection conn=dataSource.getConnection();
		if (request.getParameter("Action")!= null && request.getParameter("IdEmpresa")!=null) {
			int	   IdEmpresa=Integer.parseInt(request.getParameter("IdEmpresa"));
			String action=request.getParameter("Action");
			String Ambiente=Utils.getConfiguracion(conn, Utils.getSchema(), IdEmpresa, "PLA.Ambiente");
			if (Ambiente == null || !Ambiente.matches("[A-Za-z0-9_-]*")) {
				response.setStatus(400);
				response.getOutputStream().println("Error: invalid environment");
				conn.close();
				return;
			}
			String AppDir="/home/optimus"+Ambiente;
			response.getOutputStream().println("--------------------------------------------");			
			response.getOutputStream().println("USER   HOME   :"+System.getProperty("user.home"));
			response.getOutputStream().println("WORKING DIR   :"+System.getProperty("user.dir"));
			response.getOutputStream().println("ID EMPRESA    :"+IdEmpresa);
			response.getOutputStream().println("AMBIENTE      :"+Ambiente);
			response.getOutputStream().println("RUTA AMBIENTE :"+AppDir);
			response.getOutputStream().println("--------------------------------------------");
			if (action.equalsIgnoreCase("restart")) {
				String out="";
				out+="Deteniendo jobval01\n";
				out+=Utils.startProcess("sudo systemctl stop jobval01"+Ambiente,AppDir, 30*1000); //30 segs
				out+="Deteniendo jobagenda\n";
				out+=Utils.startProcess("sudo systemctl stop jobagenda"+Ambiente,AppDir,30*1000); //30 segs
				out+="Deteniendo jobbase\n";
				out+=Utils.startProcess("sudo systemctl stop jobbase"+Ambiente,AppDir,  30*1000); //30 segs
				out+="Deteniendo jobdaemon\n";
				out+=Utils.startProcess("sudo systemctl stop jobdaemon"+Ambiente,AppDir,  30*1000); //30 segs				
				try {Thread.currentThread();
				Thread.sleep(3000);}catch (Exception er) { }
				out+="--------------------------------------------\n";
				out+="Iniciando jobval01\n";
				out+=Utils.startProcess("sudo systemctl start jobval01"+Ambiente,AppDir, 30*1000); //30 segs
				out+="Iniciando jobagenda\n";
				out+=Utils.startProcess("sudo systemctl start jobagenda"+Ambiente,AppDir,30*1000); //30 segs
				out+="Iniciando jobbase\n";
				out+=Utils.startProcess("sudo systemctl start jobbase"+Ambiente,AppDir,  30*1000); //30 segs
				out+="Iniciando jobdaemon\n";
				out+=Utils.startProcess("sudo systemctl start jobdaemon"+Ambiente,AppDir,  30*1000); //30 segs					
				out+="--------------------------------------------\n";
				response.getOutputStream().println(out);
			} else if (action.equalsIgnoreCase("tomcatlog")) {
				String out="";
				out+="-------------- TOMCAT ---------------------\n";
				out+=Utils.startProcess("tail -n500 "+AppDir+"/logs/catalina.out",AppDir, 30*1000); //30 segs	
				response.getOutputStream().println(out);		
			} else if (action.equalsIgnoreCase("joblog")) {
				String out="";
				out+="-------------- JOBS   ---------------------\n";
				out+=Utils.startProcess("find "+AppDir+"/jobm/log* -mmin +500 -exec tail -n500 {} \\;",AppDir, 30*1000); //30 segs					
				out+="--------------------------------------------";
				response.getOutputStream().println(out);					
			}else {
				response.getOutputStream().println("Error:"+action);
			}
		}else {
			response.getOutputStream().println("Error: invalid parameters");
		}
		response.getOutputStream().flush();
	}
}
