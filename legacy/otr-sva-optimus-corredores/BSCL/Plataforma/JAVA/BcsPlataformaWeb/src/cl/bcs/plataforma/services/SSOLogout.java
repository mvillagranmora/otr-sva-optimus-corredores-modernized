package cl.bcs.plataforma.services;

import java.sql.Connection;
import java.util.HashMap;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.utils.Utils;

/**
 * Servlet implementation class CrearSesion
 */
@WebServlet("/SSOLogout")
public class SSOLogout extends HttpServlet {
	
	private static final Logger logger = LogManager.getLogger(SSOLogout.class);
	
	private static final long serialVersionUID = 1L;
	
	
	
	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SSOLogout() {
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
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			performTask(request, response);
		}catch (Exception er) {
			er.printStackTrace();			
		}
	}

	/**
	 * @throws Exception 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    
    
    
	protected void performTask(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		try (Connection conn = dataSource.getConnection();){
			HashMap<String, HashMap<String, String>> parametrosConfiguracion = Utils.getConfiguraciones(conn,1);
			HashMap<String,String> configPla = parametrosConfiguracion.get("PLATAFORMA");
			
			String html = Utils.getWebStaticFile(conn,"",configPla.get("PLA.RutaArchivosWeb"),"BSCL.Plataforma.UI.views","autologout.html");
			response.setContentType("text/html; charset=UTF-8");
		    response.setCharacterEncoding("UTF-8");
		    response.setStatus(200);
		    response.getOutputStream().write(html.getBytes("UTF-8"));
			response.getOutputStream().flush();
			conn.commit();
			logger.info("Load AutoLogout Page");
			
		}
		
		
	}
	
	
	
}
