package cl.bcs.filters;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class ServiceFilter implements Filter {
	
	// Listar todos los servicios que se tiene acceso
	private final String SQL_SELECT_ACCESO_SERVICIO = "SELECT COUNT(1) AS OK \n"
													+ "FROM\n"
													+ "	\"BSCL.Plataforma.BD::ACC.PerfilAcceso\" PACC\n"
													+ "LEFT JOIN \"BSCL.Plataforma.BD::ACC.Acceso\" ACC ON PACC.\"IdAcceso\" = ACC.\"IdAcceso\"\n"
													+ "LEFT JOIN \"BSCL.Plataforma.BD::ACC.AccesoServicio\" ASV ON ACC.\"IdAcceso\"  = ASV.\"IdAcceso\" \n"
													+ "WHERE\n"
													+ "	\"IdPerfil\" IN (\n"
													+ "		SELECT\n"
													+ "			\"IdPerfil\"\n"
													+ "		FROM\n"
													+ "			(SELECT\n"
													+ "				DISTINCT per.\"IdPerfil\",\n"
													+ "				Usu.\"CodigoUsuario\" \n"
													+ "			FROM\n"
													+ "				\"BSCL.Plataforma.BD::ACC.Usuario\" Usu\n"
													+ "			LEFT JOIN \"BSCL.Plataforma.BD::ACC.RolUsuario\" RolUsu ON\n"
													+ "				RolUsu.\"IdUsuario\" = Usu.\"IdUsuario\"\n"
													+ "			LEFT JOIN \"BSCL.Plataforma.BD::ACC.Rol\" Rol ON\n"
													+ "					Rol.\"IdRol\" = RolUsu.\"IdRol\"\n"
													+ "			LEFT JOIN \"BSCL.Plataforma.BD::ACC.RolPerfil\" RolPer ON\n"
													+ "				RolPer.\"IdRol\" = Rol.\"IdRol\"\n"
													+ "			LEFT JOIN \"BSCL.Plataforma.BD::ACC.Perfil\" Per ON\n"
													+ "				Per.\"IdPerfil\" = RolPer.\"IdPerfil\"\n"
													+ "			AND Per.\"Nombre\" IS NOT NULL\n"
													+ "			WHERE per.\"IdPerfil\" IS NOT NULL) USUA\n"
													+ "		WHERE\n"
													+ "			USUA.\"CodigoUsuario\" = (SELECT\n"
													+ "										\"CodigoUsuario\"\n"
													+ "									FROM\n"
													+ "										\"BSCL.Plataforma.BD::ACC.Usuario\"\n"
													+ "									WHERE\n"
													+ "										\"IdUsuario\" = (\n"
													+ "											SELECT\n"
													+ "												\"IdUsuario\"\n"
													+ "											FROM\n"
													+ "												\"BSCL.Plataforma.BD::ACC.Sesion\" bpbas\n"
													+ "											WHERE\n"
													+ "												\"IdTokenSesion\" = ? )))\n"
													+ "AND ASV.\"IdServicio\"  = ? ";
    
	private final String SQL_SELECT_VALIDA_SERVICIO = "SELECT \"IdServicio\", \"ValidaAccesoServicio\"  FROM \"BSCL.Plataforma.BD::PLA.Servicio\" WHERE \"IdServicio\" = ?;";
	 
	private final String SQL_SELECT_CONFIGURACION = "SELECT \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"CodigoParametro\" = ?;";
	
	private final String SQL_SELECT_USUARIO = "SELECT \"CodigoUsuario\"  FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"IdUsuario\" IN (SELECT \"IdUsuario\"  FROM \"BSCL.Plataforma.BD::ACC.Sesion\" WHERE \"IdTokenSesion\" = ?);";
	/**
     * Default constructor. 
     */
    public ServiceFilter() {
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
	
	private final List<String> noAuthServices = Arrays.asList(
			"700729",
			"600017",
			"171721",
			"1381",
			"14",
			"1556",
			"384"
			);
	
	private final List<String> noAuthChanels = Arrays.asList(
			"WS",
			"CLI",
			"AGE");
	
	// CANAL
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String URI = httpRequest.getRequestURI();
		String t = getParameter(httpRequest, "t");
		String ch = getParameter(httpRequest, "ch");

		if (noAuthURLs.contains(URI) || URI.startsWith("/PLA/WebStatic") || noAuthServices.contains(t) || t == null || ch != null || noAuthChanels.contains(ch)) {
			chain.doFilter(request, response);
		}else {			
			
			try (Connection conn = dataSource.getConnection();) {
				String paramToken = httpRequest.getHeader("X-SESSION-TOKEN");
				
				if(paramToken==null){ 
					httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
					return;				
				} else {
					try {
						
						// Obtenes codigo usuario
						String queryUsuario = SQL_SELECT_USUARIO;
						Object paramTokenUsuario = paramToken;
						Map<String, Object> queryResultUsuario = executeQuery(conn, queryUsuario, paramTokenUsuario);
						
						String usuario = queryResultUsuario.get("CodigoUsuario").toString();
						
						// Obtiene tipo de validacion de servicio (TODOS o LISTA)
						String queryTipoValidaServicio = SQL_SELECT_CONFIGURACION;
						Object paramTipoValidaServicio = "PLA.TipoValidaServicio";
						Map<String, Object> queryResultTipoValidaServicio = executeQuery(conn, queryTipoValidaServicio, paramTipoValidaServicio);

						String validaTipoValidaServicio = (queryResultTipoValidaServicio.get("Valor") != null) ? queryResultTipoValidaServicio.get("Valor").toString() : null;
						
						// Nadie -> damos acceso a todos, sin validar
						if(validaTipoValidaServicio != null && validaTipoValidaServicio.equalsIgnoreCase("NADIE")) {
							chain.doFilter(request, response);
							return;
						}
						// Lista -> Validamos si se debe validar el usuario del request = a usuario a lista, continuamos la validacion
						if(validaTipoValidaServicio != null && validaTipoValidaServicio.equalsIgnoreCase("LISTA")) {
							Object paramListaValidaServicio = "PLA.ListaValidaServicio";
							Map<String, Object> queryResultListaValidaServicio = executeQuery(conn, queryTipoValidaServicio, paramListaValidaServicio);
							
							String[] usuariosArray = (queryResultListaValidaServicio.get("Valor") != null) ? queryResultListaValidaServicio.get("Valor").toString().replaceAll("\\s", "").split(",") : null;
							List<String> usuariosList = Arrays.asList(usuariosArray);
							
							if(!usuariosList.contains(usuario)) {
								// Si el usuario configurado en la lista no corresponde al usuario que solicita, le damos permiso
								chain.doFilter(request, response);
								return ;
							}
						
						}
						
						// Todos -> Continuamos con validacion normal
						// Valida si se debe validar servicio
						String queryValidaServicio = SQL_SELECT_VALIDA_SERVICIO;
						Object paramServicio = t;
						Map<String, Object> queryResultServicio = executeQuery(conn, queryValidaServicio, paramServicio);
						
						String validaAccesoServicio = (queryResultServicio.get("ValidaAccesoServicio") != null) ? queryResultServicio.get("ValidaAccesoServicio").toString() : null;
						
						if(validaAccesoServicio != null && validaAccesoServicio.equalsIgnoreCase("S")) {
							// Valida si se debe validar servicio
							String queryValidaAccesoServicio = SQL_SELECT_ACCESO_SERVICIO;
							Map<String, Object> queryResultAccesoServicio = executeQuery(conn, queryValidaAccesoServicio, paramToken, paramServicio);
							
							String validaServicio = (queryResultAccesoServicio.get("OK") != null) ? queryResultAccesoServicio.get("OK").toString() : null;
							
							if(validaServicio != null && Integer.parseInt(validaServicio) > 0) {
								chain.doFilter(request, response);
								return;
							}else {
								httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized Session");
							}

						}else {
							chain.doFilter(request, response);
						}
		
					}catch(Exception e) {
						e.printStackTrace();
						chain.doFilter(request, response);
					}
				}
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
				chain.doFilter(request, response);
			}
		}
	}

	// Permita ejecutar query mediante un preparedStatement pasandole N parametros de tipo Object
    public static Map<String, Object> executeQuery(Connection con, String sqlQuery, Object... params) {
		Map<String, Object> result = new HashMap<>();
		
		try (PreparedStatement preparedStatement = con.prepareStatement(sqlQuery)) {
		    for (int i = 0; i < params.length; i++) {
		        preparedStatement.setObject(i + 1, params[i]);
		    }
		
		    ResultSet resultSet = preparedStatement.executeQuery();
		
		    int columnCount = resultSet.getMetaData().getColumnCount();
		
		    while (resultSet.next()) {
		        for (int i = 1; i <= columnCount; i++) {
		            String columnName = resultSet.getMetaData().getColumnName(i);
		            Object columnValue = resultSet.getObject(i);
		
		            result.put(columnName, columnValue);
		        }
		        return result;
		    }
		
		} catch (SQLException e) {
		    e.printStackTrace();
		}
		
		return result;
    }
	
	public static String getCalledService(HttpServletRequest request) {

		String queryString = request.getQueryString();
		
        if (queryString != null) {
            String[] parametros = queryString.split("&");
            for (String parametro : parametros) {
                String[] partes = parametro.split("=");
                if (partes.length == 2 && partes[0].equals("t")) {
                    return partes[1];
                }
            }
        }

        return null;
	}
	
    public static String getParameter(HttpServletRequest request, String paramName) {
        String queryString = request.getQueryString();
        if (queryString != null) {
            String[] parametros = queryString.split("&");
            for (String parametro : parametros) {
                String[] partes = parametro.split("=");
                if (partes.length == 2 && partes[0].equals(paramName)) {
                    return partes[1];
                }
            }
        }
        return null;
    }
	    
	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

}
