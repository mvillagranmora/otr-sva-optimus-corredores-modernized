package cl.bcs.plataforma.services;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.nuam.StoredProcedureException;
import cl.bcs.utils.Utils;
import cl.bcs.utils.ValidarNuevaClave;
import cl.bcs.utils.ValidarNuevaClave.EntradaValidarNuevaClave;
import cl.bcs.utils.ValidarNuevaClave.EstadoCriteriosNuevaClave;
import cl.bcs.utils.ValidarNuevaClave.SalidaValidarNuevaClave;

@WebServlet("/ActualizarClave")
public class ActualizarClave extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(ActualizarClave.class.getName());
	@Resource(name = "jdbc/hanaDB")
	DataSource dataSource;

	public ActualizarClave() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			performTask(request, response);
		} catch (SQLException | StoredProcedureException e) {
			e.printStackTrace();
		}
	}

	protected void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException, StoredProcedureException {
		StringBuilder sb = new StringBuilder();
		String s;
		JsonParser parser = new JsonParser();
		String fechaSistema = "";
		HashMap<String, String> diccionario = new HashMap<>();
		String contrasenaEncriptada = "";
		Long idEmpresa = 0L;
		try (Connection con = dataSource.getConnection();) {
			final String selectDiccionario = "SELECT \"CodDiccionario\", \"Descripcion\" FROM BSCL.\"BSCL.Plataforma.BD::PLA.Diccionario\" WHERE \"CodLenguaje\" = 'CL' AND \"CodDiccionario\" IN ('CONTRASENA_ACTUALIZADA', 'CONTRASENA_Y_CONFIRMACION_NO_SON_IGUALES', 'CONTRASENA_INVALIDA')";
			try (PreparedStatement pstmt = con.prepareStatement(selectDiccionario)) {
				ResultSet resultDiccionario = pstmt.executeQuery();
				while (resultDiccionario.next()) {
					diccionario.put(resultDiccionario.getString(1), resultDiccionario.getString(2));
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
				String Clave1 = (datosEntrada.get("Clave1") != null && !datosEntrada.get("Clave1").isJsonNull()) ? datosEntrada.get("Clave1").getAsString() : "";
				String Clave2 = (datosEntrada.get("Clave2") != null && !datosEntrada.get("Clave2").isJsonNull()) ? datosEntrada.get("Clave2").getAsString() : "";

				// validar clave
				if (!Clave1.isEmpty() && !Clave2.isEmpty()) {
					if (Clave1.equals(Clave2)) {
						final String selectIdEmpresaUsuario = "SELECT \"IdEmpresa\" FROM \"BSCL.Plataforma.BD::ACC.Usuario\" WHERE \"CodigoUsuario\" = ?";
						try (PreparedStatement pstmt = con.prepareStatement(selectIdEmpresaUsuario)) {
							pstmt.setString(1, p_CodigoUsuario.toUpperCase());
							ResultSet resultidEmpresa = pstmt.executeQuery();
							while (resultidEmpresa.next()) {
								idEmpresa = Long.parseLong(resultidEmpresa.getString(1));
							}
						} catch (SQLException ex) {
							logger.fatal("Error", ex);
							response.setStatus(500);
						}
						fechaSistema = Utils.getFechaSistema(con, idEmpresa);
						
						EntradaValidarNuevaClave entradaValidacion = new EntradaValidarNuevaClave(Clave1, idEmpresa);
						SalidaValidarNuevaClave salidaValidacion = null;
						Utils.Mensaje validacionContrasena = new Utils.Mensaje();
						boolean ClaveBuena = false;
						
						try {
							salidaValidacion = ValidarNuevaClave.logica(con, entradaValidacion);
							ClaveBuena = salidaValidacion.ClaveValida;
						} catch (Exception e) {
							logger.fatal("Error", e.getMessage());
						}
						
						if (salidaValidacion != null) {
							validacionContrasena.Estado = ClaveBuena ? "OK" : "NOK";
							
							if (!ClaveBuena) {
								ArrayList<String> listaStatus = new ArrayList<>();
								listaStatus.add("La contrase\u00f1a debe contener:");
								for(EstadoCriteriosNuevaClave estadoCriterio : salidaValidacion.P_OUT_ESTADOCRITERIOSNUEVACLAVE) {
									listaStatus.add("- " + estadoCriterio.criterio);
								};
								validacionContrasena.status = listaStatus;
							}
						} else {
							validacionContrasena.Estado = "NOK";
						}
								
						if (ClaveBuena) {
							contrasenaEncriptada = Utils.encryptPassword(Clave1);
							final String updateCambioDeContrasena = "UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET  \"Clave\" = ?, \"FechaIngreso\" = ?, \"IdEstado\" = 'ACT', \"DebeCambiarClave\" = 'N', \"UltimoCambioClave\" = ? WHERE \"CodigoUsuario\" = ?";
							try (PreparedStatement pstmt = con.prepareStatement(updateCambioDeContrasena)) {
								pstmt.setString(1, contrasenaEncriptada);
								pstmt.setString(2, fechaSistema);
								pstmt.setString(3, fechaSistema);
								pstmt.setString(4, p_CodigoUsuario);
								pstmt.execute();
								con.commit();

								respuesta(response, diccionario, "OK", "CONTRASENA_ACTUALIZADA", 200);
							} catch (SQLException ex) {
								logger.fatal("Error", ex);
								response.setStatus(500);
							}
						} else {// si la clave no cumple con los parametros
							logger.info("CONTRASENA_NO_CUMPLE_CON_REQUISITOS");
							JsonObject Salida = new JsonObject();
							Gson gson = Utils.getGsonBuilder();	
							Salida = parser.parse(gson.toJson(validacionContrasena)).getAsJsonObject();
							response.setStatus(200);
							response.getOutputStream().write(Salida.toString().getBytes("UTF-8"));
							response.getOutputStream().flush();
						}
					} else {// si la clave y la confirmacion son distintas
						respuesta(response, diccionario, "NOK", "CONTRASENA_Y_CONFIRMACION_NO_SON_IGUALES", 200);
					}
				} else {// si las claves vienen vacias
					respuesta(response, diccionario, "NOK", "CONTRASENA_INVALIDA", 200);
				}
			} else {// body params vacio
				respuesta(response, diccionario, "NOK", "CONTRASENA_INVALIDA", 200);
			}
		}
	}

	private void respuesta(HttpServletResponse response, HashMap<String, String> diccionario, String estado, String status, int statusCode) throws IOException {
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
