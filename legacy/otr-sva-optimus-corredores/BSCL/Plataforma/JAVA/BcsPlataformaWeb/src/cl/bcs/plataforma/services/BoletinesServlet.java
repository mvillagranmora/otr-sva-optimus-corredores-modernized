package cl.bcs.plataforma.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.utils.Utils;

/**
 * Servlet implementation class DatosContratos
 */
@WebServlet("/Boletines")
@MultipartConfig
public class BoletinesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	
	private static final Logger logger = LogManager.getLogger(ContratosServlet.class);
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BoletinesServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pTokenSession = request.getHeader("X-SESSION-TOKEN");
		
		String filename = null;
		//String opcion = request.getParameter("OP");
		String code = request.getParameter("filename");
		//String estatico = request.getParameter("estatico");
		String type = "";
		String extension = "";
		
		logger.info("INICIO: doGet");
		
		if(code!=null){
			System.out.println("get: " + code);
			
			try (Connection dbConnection = dataSource.getConnection()){
				//Get Session Data
				JsonObject dataJSOut = Utils.getSessionData(dbConnection, pTokenSession);
				if (dataJSOut.get("p_out_ErroresContador".toUpperCase()) == null
						|| dataJSOut.get("p_out_ErroresContador".toUpperCase()).getAsLong() > 0) {
					dbConnection.rollback();
					response.setStatus(401);
					response.getOutputStream().print("401 - NOT AUTHORIZED");
					response.getOutputStream().flush();
					return;
				}
				long SesionIdEmpresa = dataJSOut.get("p_out_IdEmpresa".toUpperCase()).getAsLong();
					
				//Obtener archivo Boletin (pdf)
				final String selectSQL = "select top 1 \"CodigoArchivo\", \"Binario\", \"TipoBinario\", \"TipoPresentacion\", \"Descripcion\" from \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarBoletines\" where \"CodigoArchivo\"=? AND \"IdEmpresa\"=?";
				try(PreparedStatement pstmt = dbConnection.prepareStatement(selectSQL)){
					pstmt.setString(1, code);
					pstmt.setLong(2, SesionIdEmpresa);
					
					ResultSet rs = pstmt.executeQuery();
					
					while(rs.next()){
						
						filename = rs.getString(5);
						System.out.println(filename);
						try(InputStream is = rs.getBlob(2).getBinaryStream()){
							IOUtils.copy(is,response.getOutputStream());
						}
						extension = rs.getString(3);
						type = rs.getString(4);
					}
					filename = filename+"."+extension;
				}
				
				response.setHeader("Content-disposition","inline; filename="+filename);
				response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");//HTTP 1.1
				response.setHeader("Pragma", "no-cache");//HTTP 1.0
				response.setContentType(type);
				System.out.println(type);
				response.getOutputStream().flush();
				
			} catch (Exception e) {
				//e.printStackTrace();
				System.out.println("DOWNLOAD ERROR: "+e.getMessage());
				response.setStatus(500);
				response.getOutputStream().print("500 - BAD REQUEST");	
				response.getOutputStream().flush();
				return;
			}
			
		}else{
			response.setStatus(401);
			response.getOutputStream().print("500 - BAD REQUEST");	
			response.getOutputStream().flush();	
			return;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		logger.info("INICIO: doPost");
		
		String pTokenSession = request.getHeader("X-SESSION-TOKEN");
		
		String opcion = request.getParameter("OP");
		
		
		logger.info("OPCION: " + opcion);
		//POST: Ingresar archivo o datos nuevos; PUT:actualizar archivo o datos
		if(!opcion.equals("DELETE")){
			
			String code = request.getParameter("code");
			String desc = request.getParameter("desc");
			String name = request.getParameter("name");
			String type = request.getParameter("type");
			String ext = request.getParameter("ext"); //***
			String idEstado = request.getParameter("estado");
			
			Part filePart = request.getPart("data");
			
			logger.info("DATOS RECIBIDOS: "  + code + ", " + desc + ", " + name + ", " + type + ", " + ext + ", " + idEstado);
			
			try (Connection dbConnection = dataSource.getConnection()){
				//Get Session Data
				JsonObject dataJSOut = Utils.getSessionData(dbConnection, pTokenSession);
				if (dataJSOut.get("p_out_ErroresContador".toUpperCase()) == null
						|| dataJSOut.get("p_out_ErroresContador".toUpperCase()).getAsLong() > 0) {
					dbConnection.rollback();
					response.setStatus(401);
					response.getOutputStream().print("401 - NOT AUTHORIZED");
					response.getOutputStream().flush();
					return;
				}
				long SesionIdEmpresa = dataJSOut.get("p_out_IdEmpresa".toUpperCase()).getAsLong();
				long SesionIdUsuario = dataJSOut.get("p_out_IdUsuario".toUpperCase()).getAsLong();
				
				String respuesta="";
				if(opcion.equals("CONTRACT")){
					//Actualizacion Archivo Contrato
				
					UUID idTemplate = null;
					
					//no es estatico, almacenar archivo en template
					idTemplate = UUID.randomUUID();
					
					logger.info("MOMENTO PREVIO AL INSERT a Template");
					
					String insert = "INSERT INTO \"BSCL.Report::Template\" ( "
							+ "\"Id\", \"ReportName\", \"MasterReportId\", \"ReportFile\", "
							+ "\"ReportVersion\") VALUES ( ?, ?, ?, ?, ?)";
					System.out.println(insert);
					logger.info("INSERT: " + insert);
					try(PreparedStatement pstmt = dbConnection.prepareStatement(insert)){	
						pstmt.setString(1, idTemplate.toString());
						pstmt.setString(2, idTemplate.toString());
						pstmt.setString(3, null);
						pstmt.setBlob(4, filePart.getInputStream());
						pstmt.setInt(5, 1);
						pstmt.executeUpdate();
						dbConnection.commit();
					}catch(SQLException ex){
						ex.printStackTrace();
						dbConnection.rollback();
						throw new Exception(ex);
					}
				
					Long IdBoletin = -1L;
					final String IdBoletinQuery = "SELECT \"BSCL.Plataforma.BD.Secuencias::PLA.IdBoletin\".NEXTVAL FROM DUMMY";
					logger.info("SELECT ID: " + IdBoletinQuery);
					try(PreparedStatement pstmt = dbConnection.prepareStatement(IdBoletinQuery)){
						ResultSet rs = pstmt.executeQuery();
						
						while(rs.next()){
							IdBoletin = rs.getLong(1);
						}
						
					}catch(SQLException ex){
						System.out.println("UPLOAD BOLETIN:" + ex.getMessage());
						dbConnection.rollback();
						logger.error("CATCH INSERT: " + ex + IdBoletinQuery);
						respuesta = "NOK";
					}
					
					logger.info("MOMENTO PREVIO AL INSERT a ArchivosBoletin");
					
					final String insertSql = "INSERT INTO \"BSCL.Plataforma.BD::PLA.Boletines\"(\"IdEmpresa\", \"CodigoArchivo\", \"Descripcion\", \"IdUsuario\", \"FechaModificacion\", \"Binario\", \"TipoBinario\", \"TipoPresentacion\", \"IdTemplate\", \"IdEstado\") VALUES(?,?,?,?,CURRENT_UTCTIMESTAMP,?,?,?,?,?)";
					System.out.println(insertSql);
					logger.info("INSERT: " + insertSql);
					try(PreparedStatement pstmt = dbConnection.prepareStatement(insertSql)){
						pstmt.setLong(1, SesionIdEmpresa);
						pstmt.setLong(2, IdBoletin);
						pstmt.setString(3, desc);
						pstmt.setLong(4, SesionIdUsuario);
						pstmt.setBinaryStream(5, filePart.getInputStream());
						pstmt.setString(6, ext);
						pstmt.setString(7, type);
						pstmt.setString(8, (idTemplate!=null)?idTemplate.toString(): "");
						pstmt.setString(9, idEstado);
						pstmt.execute();
						dbConnection.commit();
						respuesta = "OK";
					}catch(SQLException ex){
						System.out.println("UPLOAD BOLETIN:" + ex.getMessage());
						dbConnection.rollback();
						logger.error("CATCH INSERT: " + ex + insertSql);
						respuesta = "NOK";
					}
				}
				
				try(PrintWriter out = response.getWriter()){
					response.setContentType("application/text");
					out.print(respuesta);
					out.flush();
				}
				
			} catch (Exception e) {
				response.setStatus(401);
				response.getOutputStream().print("401 - BAD REQUEST");	
				response.getOutputStream().flush();
				return;
			}
			
		}else{
			//workarround puesto que no esta soportado el method PUT con multipart-form
			//http://programmers.stackexchange.com/questions/114156/why-are-there-are-no-put-and-delete-methods-on-html-forms
			deleteBoletin(request, response);
		}
	}
	
	protected void deleteBoletin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pTokenSession = request.getHeader("X-SESSION-TOKEN");
		
		logger.info("INICIO execPut");
		
		//parametros de entrada genericos
		String code = request.getParameter("code");		
		
		logger.info("DATOS RECIBIDOS: " + code);
		
		try (Connection dbConnection = dataSource.getConnection()){
			//Get Session Data
			JsonObject dataJSOut = Utils.getSessionData(dbConnection, pTokenSession);
			if (dataJSOut.get("p_out_ErroresContador".toUpperCase()) == null
					|| dataJSOut.get("p_out_ErroresContador".toUpperCase()).getAsLong() > 0) {
				dbConnection.rollback();
				response.setStatus(401);
				response.getOutputStream().print("401 - NOT AUTHORIZED");
				response.getOutputStream().flush();
				return;
			}
			long SesionIdEmpresa = dataJSOut.get("p_out_IdEmpresa".toUpperCase()).getAsLong();
			
			String respuesta="";
			
			if(code!=null){
				logger.info("DELETE Boletin code not null");
								
				final String deleteSql = "DELETE FROM \"BSCL.Plataforma.BD::PLA.Boletines\" WHERE \"CodigoArchivo\" = ? AND  \"IdEmpresa\" = ?";
				
				System.out.println(deleteSql);
				logger.info("DELETE Boletin: " + deleteSql);
				try(PreparedStatement pstmt = dbConnection.prepareStatement(deleteSql)){
					
					pstmt.setLong(1, Long.valueOf(code));
					pstmt.setLong(2, SesionIdEmpresa);

					pstmt.execute();
					dbConnection.commit();
				}catch(SQLException ex){
					dbConnection.rollback();
					logger.error(ex);
					throw new Exception(ex);
				}
			}

			respuesta = "OK";				
			response.setContentType("application/text");
			
			try(PrintWriter out = response.getWriter()){
				out.print(respuesta);
				out.flush();
			}
			
		} catch (Exception e) {
			response.setStatus(401);
			response.getOutputStream().print("401 - BAD REQUEST");	
			response.getOutputStream().flush();
			return;
		}
	}
	
	
}
