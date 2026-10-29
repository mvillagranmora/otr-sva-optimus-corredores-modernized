package cl.bcs.plataforma.services.filemanager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface FileBlobManager {
	public void setConnection(Connection connection);
	public String  getFileName();
	public String upload(HttpServletRequest request, HttpServletResponse response);
	public void download(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException;
	public String update(HttpServletRequest request, HttpServletResponse response);	
}
