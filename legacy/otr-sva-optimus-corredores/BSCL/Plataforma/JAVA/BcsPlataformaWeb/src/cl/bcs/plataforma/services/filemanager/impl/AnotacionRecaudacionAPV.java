package cl.bcs.plataforma.services.filemanager.impl;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;

import cl.bcs.plataforma.services.filemanager.FileBlobManager;

public class AnotacionRecaudacionAPV implements FileBlobManager{
	private Connection connection = null;
	private String filename = null;
	
	@Override
	public String update(HttpServletRequest request, HttpServletResponse response) {
		try {
			String name = request.getParameter("name");
			Long idAnotacion = Long.parseLong(request.getParameter("id"));
			Part filePart = request.getPart("file");
							
			final String updateArchivoSql = "UPDATE \"BSCL.APV.BD::REC.Anotacion\" SET \"Archivo\" = ?, \"NombreArchivo\" = ? WHERE \"IdAnotacion\" = ?;";
			//final String updateArchivoSql = "UPDATE \""+Utils.getSchema()+"\".\"BSCL.SocioNegocio.BD::CLI.Anotacion\" SET \"Archivo\" = ?, \"NombreArchivo\" = ? WHERE \"IdAnotacion\" = 10000;";
			//final String updateArchivoSql = "UPDATE \""+Utils.getSchema()+"\".\"BSCL.APV.BD::REC.Anotacion\" SET \"Archivo\" = ?, \"NombreArchivo\" = ? ;";
			try(PreparedStatement pstmt = connection.prepareStatement(updateArchivoSql)){
				pstmt.setBinaryStream(1, filePart.getInputStream());
				pstmt.setString(2, name);					
				pstmt.setLong(3, idAnotacion);
				pstmt.execute();
				connection.commit();
				return "OK";
			} 
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}
		return "NOK2";
	}

	@Override
	public void download(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
		Long idAnotacion = Long.parseLong(request.getParameter("id"));
		//final String selectFile = "select \"NombreArchivo\", \"Archivo\" from \""+Utils.getSchema()+"\".\"BSCL.SocioNegocio.BD::CLI.Anotacion\" where \"IdAnotacion\" = ?";
		final String selectFile = "select \"NombreArchivo\", \"Archivo\" from \"BSCL.APV.BD::REC.Anotacion\" where \"IdAnotacion\" = ?";
		try(PreparedStatement pstmt = connection.prepareStatement(selectFile)){
			pstmt.setLong(1, idAnotacion);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()){
				try(InputStream is = rs.getBlob(2).getBinaryStream()){
					IOUtils.copy(is,response.getOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			response.setHeader("Content-disposition","inline; filename="+getFileName());
			response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");//HTTP 1.1
			response.setHeader("Pragma", "no-cache");//HTTP 1.0
			response.getOutputStream().flush();
		}
	}

	@Override
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	@Override
	public String getFileName() {
		return this.filename;
	}

	@Override
	public String upload(HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

}
