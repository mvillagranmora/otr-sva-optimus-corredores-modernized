package cl.bcs.plataforma.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;


/**
 * Servlet implementation class WebStatic
 */
@WebServlet("/WebStatic/*")
//@WebServlet(name="WebStatic", urlPatterns={"*.css"})
public class WebStaticServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WebStaticServlet() {
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
		if (pfile == null) {
			pfile = request.getPathInfo().replace("/file/", "");
		}
		Path pathBase= Paths.get(System.getProperty("user.home")+"/web/");
		//pathBase= Paths.get("C:\\Nuam\\Repos\\otr-sva-optimus-corredores\\");
		//System.out.println("me vine por aqui:" + pfile);
		if (pfile==null||pathBase==null||pathBase.toString().length()<5) {
			response.setStatus(404);
			response.getOutputStream().print("404 - INVALID FILE");	
			response.getOutputStream().flush();								
			return;
		}		
		try {
			Path pathFile = Paths.get(pfile);
			Path finalPath = pathBase.resolve(pathFile).normalize().toAbsolutePath();
			String sfile = finalPath.toString();
			File file=new File(sfile);
			if (!finalPath.startsWith(pathBase) || file.exists()==false || file.isFile()==false) {
				response.setStatus(404);
				response.getOutputStream().print("404 - NOT FOUND FILE");	
				response.getOutputStream().flush();								
				return;								
			}
			response.setStatus(200);
			response.setHeader("content-type", getServletContext().getMimeType(sfile));
			if (pfile.contains("BSCL/Plataforma/UI/libs")) {
				response.setHeader("Cache-Control", "max-age=350000");				
			}else {
				response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
				response.setHeader("Pragma", "no-cache");
				response.setDateHeader("Expires", 0);				
			}
			FileUtils.copyFile(file, response.getOutputStream());			
		}catch(Exception er) {
			response.setStatus(500);						
			return;
		}       
	}
	


}
