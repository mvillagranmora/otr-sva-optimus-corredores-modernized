package cl.bcs.plataforma.services;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;


import cl.bcs.utils.Utils;

/**
 * Servlet implementation class RepoStaticServlet
 */
@WebServlet("/RepoStatic")
public class RepoStaticServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RepoStaticServlet() {
        super();
    }
    
    /**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			performTask(request, response);
		}catch (Exception er) {
			er.printStackTrace();						
		}
	}
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			performTask(request, response);
		}catch (Exception er) {
			er.printStackTrace();			
		}
	}

	/**
	 * @throws SQLException 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
		String pfile=request.getParameter("file");
		//String x=request.getQueryString();
		//String y=request.getRequestURI();
		if (pfile==null) {
			response.setStatus(404);
			response.getOutputStream().print("404 - INVALID FILE");	
			response.getOutputStream().flush();								
			return;
		}		
		try (Connection con=dataSource.getConnection();)  {
			pfile=pfile.replace("\\", "/");
			String pkg=pfile.substring(0,pfile.lastIndexOf("/")).replace('/', '.');
			String fname=pfile.substring(pfile.lastIndexOf("/")+1);
			String obj=fname.substring(0,fname.lastIndexOf("."));
			String suffix=fname.substring(fname.lastIndexOf(".")+1);								
			byte[] bfile=Utils.getFileFromHana(con, pkg, obj, suffix);
			if (bfile==null) {
				response.setStatus(404);
				response.getOutputStream().print("404 - NOT FOUND FILE");	
				response.getOutputStream().flush();								
				return;								
			}
			response.setStatus(200);
			response.setHeader("content-type", getServletContext().getMimeType(pfile));
			if (!pfile.contains("BSCL/Plataforma/UI/libs")) {
				response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
				response.setHeader("Pragma", "no-cache");
				response.setDateHeader("Expires", 0);
			}
			response.getOutputStream().write(bfile);		
		}catch(Exception er) {
			response.setStatus(500);						
			return;
		}       
	}
	


}
