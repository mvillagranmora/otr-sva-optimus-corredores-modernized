package cl.bcs.plataforma.services;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonObject;

import cl.bcs.utils.Utils;

/**
 * Servlet implementation class ImagenesServlet
 */
@WebServlet("/ImageManager")
@MultipartConfig
public class ImageManagerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ImageManagerServlet() {
        super();
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		//INIT
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pTokenSession = request.getHeader("X-SESSION-TOKEN");
		
		String file = request.getParameter("file");//IdImagen		
		if(file!=null){
			String filename="";
			String extension="";
			String type="";
			try(Connection dbConnection = dataSource.getConnection()) {
				//Validate Session token
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
				
				//Get Image by IdEmpresa and IdImagen
				final String selectSQL = "SELECT TOP 1 \"NombreImagen\", \"Binario\", \"Tipo\", \"Extension\" FROM \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen\" WHERE \"IdImagen\"=? and \"IdEmpresa\"=?";
				try(PreparedStatement pstmt = dbConnection.prepareStatement(selectSQL)){
					pstmt.setString(1, file);
					pstmt.setLong(2, SesionIdEmpresa);
					try(ResultSet rs = pstmt.executeQuery()){
						while(rs.next()){
							try(InputStream is = rs.getBlob(2).getBinaryStream()){
								filename = rs.getString(1);
								IOUtils.copy(is,response.getOutputStream());
								extension = rs.getString(3);
								type = rs.getString(4);
								filename = filename+"."+extension;
								System.out.println("DOWNLOAD: "+filename);
							}
						}
					}
					response.setHeader("Content-disposition","inline; filename="+filename);
					response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");//HTTP 1.1
					response.setHeader("Pragma", "no-cache");//HTTP 1.0
					response.setContentType(type);
					response.getOutputStream().flush();
					
				}				
			} catch (Exception e) {
				System.out.println("DOWNLOAD ERROR: "+e.getMessage());
				response.setStatus(500);
				response.getOutputStream().print("500 - INTERNAL ERROR");	
				response.getOutputStream().flush();
				return;
			}
		}else{
			response.setStatus(400);
			response.getOutputStream().print("400 - Bad Request");	
			response.getOutputStream().flush();	
			return;
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String pTokenSession = request.getHeader("X-SESSION-TOKEN");
		
		String idImage = request.getParameter("id");
		String name = request.getParameter("name");
		String version = request.getParameter("version");
		String type = request.getParameter("type");
		String extension = request.getParameter("ext");
		String existFile = request.getParameter("file");
		Part filePart = request.getPart("data");
		String rutaPublica = request.getParameter("rutaPublica");
		
		try (Connection dbConnection = dataSource.getConnection()){
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
			JsonObject jsonOut = new JsonObject();
			
			if(idImage==null){
				final String insertSql = "INSERT INTO \"BSCL.Plataforma.BD::PLA.Imagenes\"(\"IdEmpresa\", \"IdImagen\", \"NombreImagen\", \"Tipo\", \"Extension\", \"Version\", \"Binario\", \"RutaPublica\") VALUES(?,CAST (SYSUUID AS VARCHAR),?,?,?,?,?,?)";
				System.out.println(insertSql);

				try (PreparedStatement pstmt = dbConnection.prepareStatement(insertSql)){
					pstmt.setLong(1, SesionIdEmpresa);
					pstmt.setString(2, name);
					pstmt.setString(3, type);
					pstmt.setString(4, extension);
					pstmt.setString(5, version);
					pstmt.setBinaryStream(6, filePart.getInputStream());
					pstmt.setString(7, rutaPublica);
					
					pstmt.execute();
					dbConnection.commit();
				}catch(SQLException ex){
					dbConnection.rollback();
					throw new Exception(ex);
				}
			}else{
				String updateSql;
				if(existFile!=null && existFile.equals("S")){
					//actualizar binario
					updateSql = "UPDATE \"BSCL.Plataforma.BD::PLA.Imagenes\" SET \"NombreImagen\" =?, \"Tipo\"=?, \"Extension\"=?, \"Version\"=?, \"Binario\"=?, \"RutaPublica\"=? WHERE \"IdImagen\"=? and \"IdEmpresa\"=?";
				}else{
					updateSql = "UPDATE \"BSCL.Plataforma.BD::PLA.Imagenes\" SET \"NombreImagen\" =?, \"Tipo\"=?, \"Extension\"=?, \"Version\"=?, \"RutaPublica\"=? WHERE \"IdImagen\"=? and \"IdEmpresa\"=?";
				}
				
				System.out.println(updateSql);

				try (PreparedStatement pstmt = dbConnection.prepareStatement(updateSql)){
					pstmt.setString(1, name);
					pstmt.setString(2, type);
					pstmt.setString(3, extension);
					pstmt.setString(4, version);
					if(existFile!=null && existFile.equals("S")){
						pstmt.setBinaryStream(5, filePart.getInputStream());
						pstmt.setString(6, rutaPublica);
						pstmt.setString(7, idImage);
						pstmt.setLong(8, SesionIdEmpresa);
						
					}else{
						pstmt.setString(5, rutaPublica);
						pstmt.setString(6, idImage);
						pstmt.setLong(7, SesionIdEmpresa);
					}
					
					pstmt.execute();
					dbConnection.commit();
				}catch(SQLException ex){
					dbConnection.rollback();
					throw new Exception(ex);
				}
			}
			
			jsonOut.addProperty("result", "OK");				
			response.setStatus(200);
			response.setContentType("application/json");
			response.getOutputStream().write(jsonOut.toString().getBytes("UTF-8")); 
			response.getOutputStream().flush();	
		}catch(Exception e){
			System.out.println("UPLOAD ERROR: "+e.getMessage());
			response.setStatus(500);
			response.getOutputStream().print("500 - INTERNAL ERROR");	
			response.getOutputStream().flush();	
			return;
		}
			
	}
	
}
