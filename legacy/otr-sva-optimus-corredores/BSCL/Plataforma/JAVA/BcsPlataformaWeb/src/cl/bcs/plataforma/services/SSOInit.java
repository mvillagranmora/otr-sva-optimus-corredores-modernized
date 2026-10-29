package cl.bcs.plataforma.services;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.services.SSOUtils.ConfiguracionSSO;
import cl.bcs.utils.SQL;

/**
 * Servlet implementation class CrearSesion
 */
@WebServlet("/SSOInit")
public class SSOInit extends HttpServlet {
	
	private static final Logger logger = LogManager.getLogger(SSOInit.class);
	
	private static final long serialVersionUID = 1L;
	
	
	
	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SSOInit() {
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
	 * @throws SQLException 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    
    
	protected void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
		
		String entity = request.getParameter("a");
		ConfiguracionSSO conf = null;
		SQL sql = new SQL();
		if (entity == null) {
			//buscamos el default
			try (Connection conn = dataSource.getConnection();){
				List<ConfiguracionSSO> confs = sql.query(conn, SSOUtils.queryConfPredeterminada, new HashMap<>(), SSOUtils.ConfiguracionSSO.class);
				if (confs.size() == 0) {
					throw new ServletException("SSO no configurado");
				}
				conf = confs.get(0);
			}			
		}
		else {
			try (Connection conn = dataSource.getConnection();){
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("IdEntidad", entity.toUpperCase());
				List<ConfiguracionSSO> confs = sql.query(conn, SSOUtils.queryConfiguracion, params , SSOUtils.ConfiguracionSSO.class);
				if (confs.size() == 0) {
					throw new ServletException("SSO no configurado");
				}
				conf = confs.get(0);
			}	
		}
		
		String client_id = "client_id=" + conf.ClientID;
		String redirectParams = "redirect_uri=" + URLEncoder.encode( conf.RedirectURL,"UTF-8");
		String scope = "scope=" + URLEncoder.encode("openid profile email phone address","UTF-8");
		String authURL = conf.URLAuth;
		String fullRedirect = authURL+"?"+ client_id + "&" + redirectParams + "&" + scope + "&response_type=code&state=fee7e77508933e8ba8c6008dc587b4c6934a94e9";
		logger.debug("Redirecting to:" + fullRedirect);
		response.sendRedirect(fullRedirect);    	
	}	
}
