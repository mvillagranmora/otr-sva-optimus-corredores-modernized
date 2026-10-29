package cl.bcs.plataforma.services;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.utils.Utils;
import cl.bcs.utils.AgregarNotificacion;

/**
 * Servlet implementation class ServletRecuperacionClave
 */
@WebServlet("/RecuperacionClave")
public class RecuperacionClave extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(CallServlet.class.getName());
	@Resource(name = "jdbc/hanaDB")
	DataSource dataSource;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RecuperacionClave() {
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

	protected void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
		StringBuilder sb = new StringBuilder();
		JsonParser parser = new JsonParser();
		String s;
		while ((s = request.getReader().readLine()) != null) {
			sb.append(s);
		}
		String bodyParams = sb.toString();
		if (!bodyParams.isEmpty()) {
			JsonObject datosEntrada = new JsonObject();
			
			try {
				datosEntrada = parser.parse( (bodyParams.isEmpty()) ? "{}" : bodyParams ).getAsJsonObject();
			
				String p_CodigoUsuario = (datosEntrada.get("p_CodigoUsuario") != null && !datosEntrada.get("p_CodigoUsuario").isJsonNull()) ? datosEntrada.get("p_CodigoUsuario").getAsString().toUpperCase() : "";
				String p_CanalSistema = (datosEntrada.get("p_CanalSistema") != null && !datosEntrada.get("p_CanalSistema").isJsonNull()) ? datosEntrada.get("p_CanalSistema").getAsString() : "";
	
				try (Connection conn = dataSource.getConnection()) {
					//DATOS USUARIO
					String fechaSistema = "";
					int largoToken = 0;
					Long idPersona = null;
					Long idDireccionElectronica = null;
					String nombres = null;
					String corredora = null;
					Long idEmpresa = null;
					Long idUsuario = null;
					String tipoEntidad = null;
					Long idSecuencia = 0L;
					Long idPortafolio = 0L;
	
					final String selectDatosUsuario = "SELECT TOP 1 CLI.\"IdPersona\" AS IdPersona, PER.\"IdDireccionElectronicaPrincipal\", PER.\"NombreCompleto\", DTOS.\"RazonSocial\" AS corredora, CLI.\"IdEmpresa\", USU.\"IdUsuario\", USU.\"TipoEntidad\" from \"BSCL.Plataforma.BD::ACC.Usuario\" USU "
							+ "LEFT JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" CLI ON (USU.\"TipoEntidad\" = 'CLI' AND USU.\"IdEntidad\" = CLI.\"IdCliente\") OR (USU.\"TipoEntidad\" = 'EMP' AND USU.\"IdEntidad\" = CLI.\"IdEmpleado\") OR (USU.\"TipoEntidad\" = 'PER' AND USU.\"IdEntidad\" = CLI.\"IdPersona\") "
							+ "LEFT JOIN \"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona\" PER ON PER.\"IdPersona\" = CLI.\"IdPersona\" "
							+ "LEFT JOIN \"BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales\" DTOS ON DTOS.\"IdEmpresa\" = CLI.\"IdEmpresa\" WHERE USU.\"CodigoUsuario\" = ?";
	
					try (PreparedStatement pstmt = conn.prepareStatement(selectDatosUsuario)) {
						pstmt.setString(1, p_CodigoUsuario.toUpperCase());
						ResultSet DatosUsuario = pstmt.executeQuery();
	
						while (DatosUsuario.next()) {
							idPersona = Long.parseLong(DatosUsuario.getString(1));
							idDireccionElectronica = Long.parseLong(DatosUsuario.getString(2));
							nombres = DatosUsuario.getString(3);
							corredora = DatosUsuario.getString(4);
							idEmpresa = Long.parseLong(DatosUsuario.getString(5));
							idUsuario = Long.parseLong(DatosUsuario.getString(6));
							tipoEntidad = DatosUsuario.getString(7);
						}
						if(idUsuario != null || idEmpresa != null)
							fechaSistema = Utils.getFechaSistema(conn, idEmpresa);
					} catch (SQLException ex) {
						logger.fatal("Error",ex);
						defaultError(request,response);
					}
					// SI EL USUARIARIO PERTENECE A ALGUN CANAL
					if (tipoEntidad != null && (p_CanalSistema.equals("CLIENTE") && (tipoEntidad.equals("CLI") || tipoEntidad.equals("PER"))) || (p_CanalSistema.equals("OPTIMUS") && tipoEntidad.equals("EMP")) || (p_CanalSistema.equals("AGENTE") && tipoEntidad.equals("EMP"))) {
	
						// CREAR EVENTO ENVIA MAIL CON CLAVE PROVISORIA
						try {
							AgregarNotificacion.EntradaAgregarNotificacion notifIn = new AgregarNotificacion.EntradaAgregarNotificacion();
							notifIn.P_IDEMPRESA = idEmpresa;
							notifIn.P_IDUSUARIO = idUsuario;
							notifIn.P_IDMODULO = "PLA";
							notifIn.P_EVENTOS = new ArrayList<>();
							notifIn.P_VALORES = new ArrayList<>();
							notifIn.P_ARCHIVOSADJUNTOS = new ArrayList<>();
							notifIn.P_IDDIRECCIONES = new ArrayList<>();
	
							// Agregar Evento
							AgregarNotificacion.Evento evtentoNotificacion = new AgregarNotificacion.Evento();
							evtentoNotificacion.CodigoTipoEvento = "SOLICITUD_CAMBIO_CLAVE";
							evtentoNotificacion.IdSecuencia = idSecuencia;
							evtentoNotificacion.FechaCreacionSistema = fechaSistema;
							evtentoNotificacion.IdPersonaRemitente = Long.parseLong("0");
							evtentoNotificacion.IdPersonaDestinatario = idPersona;
							evtentoNotificacion.IdPortafolio = idPortafolio;
							notifIn.P_EVENTOS.add(evtentoNotificacion);
	
							// Agregar notificacion al cliente
							AgregarNotificacion.DireccionEvento notificaciondireccionEvento = new AgregarNotificacion.DireccionEvento();
							notificaciondireccionEvento = new AgregarNotificacion.DireccionEvento();
							notificaciondireccionEvento.IdSecuencia = idSecuencia;
							notificaciondireccionEvento.TipoDestinatario = "TO";
							notificaciondireccionEvento.IdDireccion = new Long(idDireccionElectronica);
							notifIn.P_IDDIRECCIONES.add(notificaciondireccionEvento);
							
							final String selectLargoToken = "SELECT \"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" WHERE \"CodigoParametro\" = 'PLA.LargoMaximoToken' AND \"IdEmpresa\" = ?";
							try (PreparedStatement pstmt = conn.prepareStatement(selectLargoToken)) {
								pstmt.setLong(1, idEmpresa);
								ResultSet reslargotoken = pstmt.executeQuery();
	
								while (reslargotoken.next()) {
									largoToken = reslargotoken.getInt(1);
								}
							} catch (SQLException ex) {
								logger.fatal("Error",ex);
								defaultError(request,response);
							}
							String claveProvisoria = Utils.generateToken(largoToken);
	
							// encripta clave provisoria
							String claveProvisoriaNueva = Utils.encryptPassword(claveProvisoria);
							
							String logoCorredorRutaPublica = "";
	
							final String selectImagenCorredora = "SELECT \"RutaPublica\" FROM \"BSCL.Plataforma.BD::PLA.Imagenes\" WHERE \"NombreImagen\" = ? AND \"IdEmpresa\" = ?;";
							try (PreparedStatement pstmt = conn.prepareStatement(selectImagenCorredora)) {
								pstmt.setString(1, "LOGO_CORREDOR");
								pstmt.setLong(2, idEmpresa);
								ResultSet resRutaPublica = pstmt.executeQuery();
	
								while (resRutaPublica.next()) {
									logoCorredorRutaPublica = resRutaPublica.getString(1);
								}
							} catch (SQLException ex) {
								logger.fatal("Error",ex);
								defaultError(request,response);
							}
							
							notifIn.addValor(idSecuencia, "Nombre", nombres).addValor(idSecuencia, "Corredora", corredora).addValor(idSecuencia, "ClaveProvisoria", claveProvisoria)
								.addValor(idSecuencia, "LogoCorredora", logoCorredorRutaPublica);
	
							if (notifIn.P_EVENTOS.size() > 0) {
								AgregarNotificacion notif = new AgregarNotificacion();
								notif.logica(conn, notifIn);
	
								final String insertClaveProvisoria = "UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"ClaveProvisoria\" = ?, \"FechaClaveProvisoria\" = ?, \"IntentosConexionClaveProvisoria\" = 0 WHERE \"CodigoUsuario\" = ?";
								try (PreparedStatement pstmt = conn.prepareStatement(insertClaveProvisoria)) {
									pstmt.setString(1, claveProvisoriaNueva);
									pstmt.setString(2, fechaSistema);
									pstmt.setString(3, p_CodigoUsuario);
									pstmt.execute();
									conn.commit();
									
									respuesta(response, 200);
	
								} catch (SQLException ex) {
									logger.fatal("Error",ex);
									defaultError(request,response);
								}
							}
						} catch (Exception e) {
							logger.fatal("Error",e);
							defaultError(request,response);
						}
					} else {// Si el usiario no pertenece al canal
						logger.info("USUARIO NO PERTENECE A CANAL");
						defaultError(request,response);
					}
				} catch (Exception e1) {
					logger.fatal("Error",e1);
					defaultError(request,response);
				}
			} catch(Exception err) {
				 logger.fatal("Error al parsear JSON: ", err);
				 defaultError(request,response);
			}
		} else {
			logger.info("USUARIO NO INGRESADO");
			defaultError(request,response);
		}
	}
	
	private void respuesta(HttpServletResponse response, int statusCode) throws IOException{
		logger.info("CLAVE ENVIADA");
		JsonObject Salida = new JsonObject();
		Salida.addProperty("Estado", "OK");
		Salida.addProperty("status", "CLAVE ENVIADA");
		response.setStatus(statusCode);
		response.getOutputStream().write(Salida.toString().getBytes("UTF-8"));
		response.getOutputStream().flush();
	}
	
	private void defaultError(HttpServletRequest request, HttpServletResponse response) throws IOException{
    	JsonObject errorOut = new JsonObject();
    	errorOut.addProperty("p_out_ErroresContador".toUpperCase(), 1);
    	JsonArray errorDetalleOut = new JsonArray();
    	
    	JsonObject obj = new JsonObject();
		obj.addProperty("CODIGO", "ERROR_RECUPERAR_CLAVE");
		obj.addProperty("Descripcion", "Error al Recuperar Clave.");
    	errorDetalleOut.add(obj);
    	errorOut.add("p_out_ErroresDetalle".toUpperCase(), errorDetalleOut);
    	
    	response.setStatus(401);
		response.getOutputStream().write(errorOut.toString().getBytes("UTF-8")); 
		response.getOutputStream().flush();
	}
}
