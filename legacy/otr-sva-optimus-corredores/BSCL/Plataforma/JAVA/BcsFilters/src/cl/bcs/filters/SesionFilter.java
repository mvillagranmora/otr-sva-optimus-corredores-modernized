package cl.bcs.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;



/**
 * Servlet Filter implementation class SesionFilter
 */
public class SesionFilter implements Filter {

    /**
     * Default constructor. 
     */
    public SesionFilter() {
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	private final List<String> noAuthURLs = Arrays.asList(
			"/PLA/ValidaClaveProvisoria",
			"/PLA/ActualizarClave",
			"/PLA/RecuperacionClave",
			"/PLA/CrearSesion",
			"/PLA/bootstrap",
			"/PLA/LoginCliente",
			"/PLA/SolicitudRecuperacionClave",
			"/PLA/ConfirmarCambioClave",
			"/PLA/CallBatch",
			"/PLA/WebStatic",
			"/PLA/RepoStatic",
			"/PORTAL/validate",
			"/PLA/CallWebsocket",
			"/PLA/AuditoriaWS",
			"/PLA/Configuraciones",
			"/PLA/Ping",
			"/PLA/SSOInit",
			"/PLA/SSOLogon",
			"/PLA/SSOLogonBofa",
			"/PLA/SSOLogonGoogle",
			"/PLA/SSOLogonLocalGoogle",
			"/PLA/SSOLogout");
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String URI=httpRequest.getRequestURI();		
		if (noAuthURLs.contains(URI) || URI.startsWith("/PLA/WebStatic")) {
			chain.doFilter(request, response);
		}else {			
			String spValidaToken = "SELECT COUNT(1) AS OK FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = ? and \"TokenValido\" = 'S' ";
			try (Connection conn = dataSource.getConnection();) {
				String pTokenSrv = httpRequest.getHeader("X-SESSION-TOKEN");  				
				if(pTokenSrv==null){ 
					httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
					return;				
				} else {				
					try (PreparedStatement callSQL = conn.prepareStatement(spValidaToken);) {
						callSQL.setString(1, pTokenSrv);
						try (ResultSet rs=callSQL.executeQuery()) {	
							int numtoken=0;
							if (rs.next()) numtoken = rs.getInt("OK");									
							if(numtoken==0){
								httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Session");
								return;
							}else {
								try {
									//Send request to child
									chain.doFilter(request, response);
								}catch(Exception erDo) {
									httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Error");
									erDo.printStackTrace();
									return;
								}
							}							
						}
						conn.commit();
					}catch(Exception er) {						
						try {							
							conn.rollback();
						}catch (Exception er2) {	}
						httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Session");
						er.printStackTrace();
						return;
					}
				}
				conn.close();
			} catch (SQLException e) {
				httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Error");
				e.printStackTrace();
				return;
			}	
		}
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

}
