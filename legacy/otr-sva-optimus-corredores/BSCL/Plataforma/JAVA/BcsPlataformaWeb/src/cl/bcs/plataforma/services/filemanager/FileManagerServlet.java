package cl.bcs.plataforma.services.filemanager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 * Servlet implementation class SubirArchivoServlet
 */
@WebServlet("/FileBlobManager")
@MultipartConfig
public class FileManagerServlet extends HttpServlet implements Servlet {
	private static final long serialVersionUID = 1L;
	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileManagerServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String concreteUploader = request.getParameter("entity");
		
		FileBlobManager manager = FileBlobManagerFactory.getManager(concreteUploader);
		if(manager!=null){
			try (Connection connection = dataSource.getConnection()){
				
				manager.setConnection(connection);
				String respuesta = manager.update(request, response);
				response.getWriter().append(respuesta);
				
			} catch (SQLException e) {
				response.getWriter().append("NOK");
			}
		}else{
			response.getWriter().append("NOK");
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String concreteDownloader = request.getParameter("entity");
		
		FileBlobManager manager = FileBlobManagerFactory.getManager(concreteDownloader);
		if(manager!=null){
			try (Connection connection = dataSource.getConnection()){
				
				manager.setConnection(connection);
				manager.download(request, response);
				
			} catch (Exception e) {
				e.printStackTrace();
				response.getWriter().append("NOK");
			}
		}else{
			response.getWriter().append("NOK");
		}
	}
}
