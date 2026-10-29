package cl.bcs.plataforma.services;

import java.sql.Connection;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Servlet implementation class CrearSesion
 */
@WebServlet("/SSOLogonLocalGoogle")
public class SSOLogonLocalGoogle extends HttpServlet {
	
	private static final Logger logger = LogManager.getLogger(SSOLogonLocalGoogle.class);
	
	private static final long serialVersionUID = 1L;
	
	
	
	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SSOLogonLocalGoogle() {
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
			String html = SSOUtils.procesar(conn,request,"LOCALGOOGLE");
			response.setContentType("text/html; charset=UTF-8");
		    response.setCharacterEncoding("UTF-8");
		    response.setStatus(200);
		    response.getOutputStream().write(html.getBytes("UTF-8"));
			response.getOutputStream().flush();
			conn.commit();
			logger.info("Load AutoLogin Page");
			
		}
		
		
	}
	
	
	
}
