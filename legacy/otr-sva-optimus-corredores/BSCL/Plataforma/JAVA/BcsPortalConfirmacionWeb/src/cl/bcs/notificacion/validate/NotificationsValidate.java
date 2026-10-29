package cl.bcs.notificacion.validate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang.StringEscapeUtils;

import cl.bcs.notificacion.validate.sql.ConnectionDB;

@WebServlet("/validate")
public final class NotificationsValidate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	@Resource(name = "jdbc/hanaDB") DataSource dataSource;

	public Connection sapHanaConnection() throws SQLException {
		return DriverManager.getConnection("jdbc:sap://161.238.34.246:30215/BSCL", "VARIACODE", "Variacode123456");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			validate(request, response);
		} catch (Exception er) {
			er.printStackTrace();
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			validate(request, response);
		} catch (Exception er) {
			er.printStackTrace();
		}
	}

	private void validate(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {

		String p_token = request.getParameter("token");
		String p_rut = request.getParameter("rut");
		String p_accion = request.getParameter("accion");
		String p_observacion = request.getParameter("observacion");
		ConnectionDB conectors = new ConnectionDB();

		try (Connection con = dataSource.getConnection();) {
			long idEmpresa = conectors.validateToken(con, p_token);
			String url = conectors.getUrlHana(con, idEmpresa);
			
			try {
				if (p_token == null || url==null) {
					//si el token no es valido no obtiene la empresa y tampoco la empresa, error generico.
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad Request");
				} else {
					if (p_rut == null) {
						String html = readHtml(url, "/BSCL/Plataforma/UI/EVT/Confirmar/ConfirmarNotificacionIngreso.html");
						response.setStatus(200);
						html = html.replace("${token}", "<input type=\"hidden\" id=\"p_token\" value=\"" + p_token + "\" >");
						response.getOutputStream().print(html);
						response.getOutputStream().flush();
					} else {

						String template = conectors.validateTokenAndId(con, p_token, p_rut);
						if (template != null || !template.equals("-1")) {
							if (p_accion == null && p_observacion == null) {

								String token = "<input type=\"hidden\" ng-model =\"token_conf\" id=\"p_token\" value=\""+ p_token + "\" >";
								String rut = "<input type=\"hidden\" ng-model =\"rut_conf\" id=\"p_rut\" value=\""+ p_rut + "\" >";

								response.setStatus(200);
								response.getOutputStream()
										.print(StringEscapeUtils.unescapeJava(template).replace("&quot;", "\"")
												.replace("<div class=\"container\">", "").replace("<a", "<p")
												.replace("/a>", "/p>") + " ||" + "1" + "||" + "0" + "||" + rut
												+ "||" + token);
								response.getOutputStream().flush();
							} else {
								if (conectors.acceptOrReject(con, p_token, p_observacion, p_accion) != -1) {

									response.setStatus(200);
									response.getOutputStream().print("1");
									response.getOutputStream().flush();
								} else {
									response.setStatus(500);
									String html = readHtml(url, "-1");
									response.getOutputStream().print(html);
									response.getOutputStream().flush();
								}
							}

						} else {

							response.setStatus(500);
							String html = readHtml(url,"-1");
							response.getOutputStream().print(html);
							response.getOutputStream().flush();

						}

					}
				}
			} catch (Exception e) {
				String html = readHtml(url, "/Plataforma/UI/EVT/Confirmar/EventoNoDisponible.html");
				response.setStatus(500);
				response.getOutputStream().print(html);
				response.getOutputStream().flush();
			}
		}
	}

	private String readHtml(String server, String url_) {
		try {
			URL url = new URL(server + url_);

			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				inputLine = inputLine.replaceAll("::URL::", server);
				response.append(inputLine + "\n");
			}

			in.close();

			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
}
