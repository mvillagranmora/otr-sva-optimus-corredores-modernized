package cl.bcs.plataforma.services;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.Utils;

/**
 * Servlet implementation class RecuperarClave
 */
@WebServlet("/ValidaClaveProvisoria")
public class ValidaClaveProvisoria extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(CallServlet.class.getName());
	@Resource(name = "jdbc/hanaDB")
	DataSource dataSource;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ValidaClaveProvisoria() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			performTask(request, response);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void performTask(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException, SQLException {
		StringBuilder sb = new StringBuilder();
		JsonParser parser = new JsonParser();
		HashMap<String, String> diccionario = new HashMap<>();
		String s;
		try (Connection conn = dataSource.getConnection()) {
			final String selectDiccionario = "SELECT \"CodDiccionario\", \"Descripcion\" FROM BSCL.\"BSCL.Plataforma.BD::PLA.Diccionario\" WHERE \"CodLenguaje\" = 'CL' AND \"CodDiccionario\" IN ('CLAVE_PROVISORIA_VALIDA', 'CLAVE_PROVISORIA_VENCIDA', 'CLAVE_PROVISORIA_INVALIDA', 'SUPERA_INTENTOS_MAXIMOS')";
			try (PreparedStatement pstmt = conn.prepareStatement(selectDiccionario)) {
				ResultSet DatosUsuario = pstmt.executeQuery();
				while (DatosUsuario.next()) {
					diccionario.put(DatosUsuario.getString(1), DatosUsuario.getString(2));
				}
			} catch (SQLException ex) {
				logger.fatal("Error", ex);
				response.setStatus(500);
			}
			while ((s = request.getReader().readLine()) != null) {
				sb.append(s);
			}
			String bodyParams = sb.toString();
			if (!bodyParams.isEmpty()) {
				JsonObject datosEntrada = parser.parse((bodyParams.isEmpty()) ? "{}" : bodyParams).getAsJsonObject();

				String p_CodigoUsuario = (datosEntrada.get("p_CodigoUsuario") != null && !datosEntrada.get("p_CodigoUsuario").isJsonNull()) ? datosEntrada.get("p_CodigoUsuario").getAsString().toUpperCase() : "";
				String p_ClaveProvisoria = (datosEntrada.get("p_ClaveProvisoria") != null && !datosEntrada.get("p_ClaveProvisoria").isJsonNull()) ? datosEntrada.get("p_ClaveProvisoria").getAsString() : "";

				String fechaSistema = "";
				Long idUsuario = null;
				Long idEmpresa = null;
				String claveProvisoria = "";
				String fechaHoraClaveProvisoria = "";
				int intentosConexionClaveProvisoria = 0;
				int cantidadIntentosMaximo = 0;
				// Trae datos clave provisoria
				final String selectClaveProvisoria = "SELECT TOP 1 USU.\"IdUsuario\",USU.\"IdEmpresa\",USU.\"ClaveProvisoria\", USU.\"FechaClaveProvisoria\", USU.\"IntentosConexionClaveProvisoria\", CON.\"Valor\" AS \"CantidadIntentosMaximo\"\r\n"
						+ "    		FROM \"BSCL.Plataforma.BD::ACC.Usuario\" AS USU\r\n"
						+ "    		LEFT JOIN \"BSCL.Plataforma.BD::PLA.Configuracion\" AS CON ON CON.\"CodigoParametro\" = 'PLA.ClaveUsuarioIntentos' AND CON.\"IdEmpresa\" = USU.\"IdEmpresa\"\r\n"
						+ "    		WHERE USU.\"CodigoUsuario\" = ?";

				try (PreparedStatement pstmt = conn.prepareStatement(selectClaveProvisoria)) {
					pstmt.setString(1, p_CodigoUsuario);
					ResultSet resultClaveProvisoria = pstmt.executeQuery();

					while (resultClaveProvisoria.next()) {
						idUsuario = resultClaveProvisoria.getLong(1);
						idEmpresa = resultClaveProvisoria.getLong(2);
						claveProvisoria = resultClaveProvisoria.getString(3);
						fechaHoraClaveProvisoria = resultClaveProvisoria.getString(4);
						intentosConexionClaveProvisoria = resultClaveProvisoria.getInt(5);
						cantidadIntentosMaximo = resultClaveProvisoria.getInt(6);
					}
					if (idUsuario != null && idEmpresa != null) {
						fechaSistema = Utils.getFechaSistema(conn, idEmpresa);
						SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
						Date date = dt.parse(fechaHoraClaveProvisoria);
						String fechaClaveProvisoria = dt.format(date).toString();

						// Encripta clave enviada para comparacion
						p_ClaveProvisoria = Utils.encryptPassword(p_ClaveProvisoria);

						if (p_ClaveProvisoria.equals(claveProvisoria)) {
							if (fechaClaveProvisoria.equalsIgnoreCase(fechaSistema)) {
								if (intentosConexionClaveProvisoria <= cantidadIntentosMaximo) {
									// clave es igual vuelve a 0
									final String updateClaveValida = "UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"ClaveProvisoria\" = '', \"FechaClaveProvisoria\" = '0001-01-01 00:00:00', \"IntentosConexionClaveProvisoria\" = 0\r\n"
											+ "WHERE \"CodigoUsuario\" = ?";
									try (PreparedStatement pstmt2 = conn.prepareStatement(updateClaveValida)) {
										pstmt2.setString(1, p_CodigoUsuario);
										pstmt2.execute();
										conn.commit();

										respuesta(response, diccionario, "OK", "CLAVE_PROVISORIA_VALIDA", 200);
									} catch (SQLException ex) {
										logger.fatal("Error", ex);
										response.setStatus(500);
									}
								} else {// intentos con clave supera maximo
									final String updateIntentosMaximos = "UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"ClaveProvisoria\" = '', \"FechaClaveProvisoria\" = '0001-01-01 00:00:00', \"IntentosConexionClaveProvisoria\" = 0\r\n"
											+ "WHERE \"CodigoUsuario\" = ?";
									try (PreparedStatement pstmt2 = conn.prepareStatement(updateIntentosMaximos)) {
										pstmt2.setString(1, p_CodigoUsuario);
										pstmt2.execute();
										conn.commit();

										respuesta(response, diccionario, "NOK", "SUPERA_INTENTOS_MAXIMOS", 200);
									} catch (SQLException ex) {
										logger.fatal("Error", ex);
										response.setStatus(500);
									}
								}
							} else {// fecha de clave distinta a fecha sistema
								final String updateIntentosMaximos = "UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"ClaveProvisoria\" = '', \"FechaClaveProvisoria\" = '0001-01-01 00:00:00', \"IntentosConexionClaveProvisoria\" = 0\r\n"
										+ "WHERE \"CodigoUsuario\" = ?";
								try (PreparedStatement pstmt2 = conn.prepareStatement(updateIntentosMaximos)) {
									pstmt2.setString(1, p_CodigoUsuario);
									pstmt2.execute();
									conn.commit();

									respuesta(response, diccionario, "NOK", "CLAVE_PROVISORIA_VENCIDA", 200);
								} catch (SQLException ex) {
									logger.fatal("Error", ex);
									response.setStatus(500);
								}
							}
						} else {// Clave provisoria distinta tiene que sumar 1 intento
							intentosConexionClaveProvisoria = intentosConexionClaveProvisoria + 1;
							final String updateIntentosMaximos = "UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET  \"IntentosConexionClaveProvisoria\" = ? WHERE \"CodigoUsuario\" = ?";
							try (PreparedStatement pstmt3 = conn.prepareStatement(updateIntentosMaximos)) {
								pstmt3.setInt(1, intentosConexionClaveProvisoria);
								pstmt3.setString(2, p_CodigoUsuario);
								pstmt3.execute();
								conn.commit();

								respuesta(response, diccionario, "NOK", "CLAVE_PROVISORIA_INVALIDA", 200);
							} catch (SQLException ex) {
								logger.fatal("Error", ex);
								response.setStatus(500);
							}
						}
					}
				} catch (Exception e) {// Conexion erronea
					logger.fatal("Error", e);
					response.setStatus(500);
				}
			} else {// body params vacio
				respuesta(response, diccionario, "NOK", "CLAVE_PROVISORIA_INVALIDA", 200);
			}
		}
	}

	private void respuesta(HttpServletResponse response, HashMap<String, String> diccionario, String estado,
			String status, int statusCode) throws IOException {
		for (Map.Entry<String, String> obj : diccionario.entrySet()) {
			if (status.equals(obj.getKey())) {
				logger.info(obj.getValue());
				JsonObject Salida = new JsonObject();
				Salida.addProperty("Estado", estado);
				Salida.addProperty("status", obj.getValue());
				response.setStatus(statusCode);
				response.getOutputStream().write(Salida.toString().getBytes("UTF-8"));
				response.getOutputStream().flush();
			}
		}
	}
}
