package cl.bcs.plataforma.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.io.FilenameUtils;

import com.google.gson.JsonObject;

import cl.bcs.utils.JobFileManager;
import cl.bcs.utils.Utils;
import cl.bcs.utils.ZipUtil;
import cl.bcs.utils.JobFileManager.EntradaJobFileManager;

@WebServlet("/download")
public class FileDownload extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileDownload() {
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

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			performTask(request, response);
		}catch (Exception er) {
			er.printStackTrace();			
		}
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws SQLException 
	 * @throws IOException
	 */
	private void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {	
		Connection conn=null; 
		long 	SesionIdEmpresa=0;
		long 	SesionIdUsuario=0;	
		String 	pTokenSession= request.getHeader("X-SESSION-TOKEN");
		if (pTokenSession==null) {
			response.setStatus(401);
			response.getOutputStream().print("401 - NOT AUTHORIZED");	
			response.getOutputStream().flush();					
			return;
		}				
		try {
	        conn = dataSource.getConnection();
			///////////////////////////////////////
	        //get connection & session
			///////////////////////////////////////		        
			try (Statement st=conn.createStatement();) {
				st.executeUpdate("SET TRANSACTION AUTOCOMMIT DDL OFF");
			}
			JsonObject dataJSOut=Utils.getSessionData(conn,pTokenSession);
			if (dataJSOut.get("p_out_ErroresContador".toUpperCase())==null || dataJSOut.get("p_out_ErroresContador".toUpperCase()).getAsLong()>0) {
				conn.rollback();
				response.setStatus(401);
				response.getOutputStream().print("401 - NOT AUTHORIZED");	
				response.getOutputStream().flush();	
				return;
			}
			SesionIdEmpresa=dataJSOut.get("p_out_IdEmpresa".toUpperCase()).getAsLong();
			SesionIdUsuario=dataJSOut.get("p_out_IdUsuario".toUpperCase()).getAsLong();
			///////////////////////////////////////
	        //File Download
			///////////////////////////////////////	
			String rutaBase=Utils.getConfiguracion(conn,SesionIdEmpresa, "PLA.RutaBaseArchivos");
			
			String sfile=request.getParameter("file");			
			if (sfile==null) {
				response.setStatus(404);
				response.getOutputStream().print("404 - INVALID FILE");	
				response.getOutputStream().flush();								
				return;
			}
			String rutaCompleta = rutaBase + sfile;
				String normalizedBase = rutaBase.endsWith(File.separator) ? rutaBase : rutaBase + File.separator;
			
			//Generar zip
			String zipped = request.getParameter("zipped");
            String nuevaRuta = obtenerRutaZip(zipped, rutaCompleta);
            
			File file = new File(nuevaRuta);
			if(file.exists()==false || file.isDirectory() || !file.getCanonicalPath().startsWith(normalizedBase)) {
				try {
					JobFileManager jobfile = new JobFileManager();
					EntradaJobFileManager entrada = new EntradaJobFileManager();
					entrada.p_IdEmpresa = SesionIdEmpresa;
					entrada.p_IdUsuario = SesionIdUsuario;
					entrada.p_Directorio = rutaBase;
					entrada.p_Archivo = sfile;
					entrada.p_Destino = "Secundario";
					
					//Primer intento para ir a buscar el archivo al nodo secundario
					nuevaRuta = obtenerRutaZip(zipped, rutaCompleta);
					jobfile.logica(conn, entrada);
					file = new File(nuevaRuta);
					
					if(file.exists()==false || file.isDirectory() || !file.getCanonicalPath().startsWith(normalizedBase)) {
						entrada.p_Destino = "Primario";
						
						//Segundo intento para ir a buscar el archivo al nodo primario, en caso de que sea el secundario el activo
						nuevaRuta = obtenerRutaZip(zipped, rutaCompleta);
						jobfile.logica(conn, entrada);
						file = new File(nuevaRuta);
					}
				}
				catch(Exception e) {
					response.setStatus(404);
					response.getOutputStream().print("404 - File Not Found");
					response.getOutputStream().flush();					
					return;
				}
				
				if(file.exists()==false || file.isDirectory() || !file.getCanonicalPath().startsWith(normalizedBase)) {
					response.setStatus(404);
					response.getOutputStream().print("404 - File Not Found");
					response.getOutputStream().flush();					
					return;
				}
			}
			
			response.setStatus(200);	
			OutputStream out = response.getOutputStream();
			FileInputStream in = new FileInputStream(file);
			byte[] buffer = new byte[4096];
			int length;
			while ((length = in.read(buffer)) > 0){
			    out.write(buffer, 0, length);
			}
			in.close();
			out.flush();			
			conn.commit();		
		}catch (Exception e) {
			e.printStackTrace();
			response.setStatus(500);			
		}finally  {
			if (conn != null) {
				try {conn.rollback(); }catch(Exception er) {}
				conn.close();
			}				
		}
	}
	
	// Entrega la ruta para archivo zip generado si zipped es S, de lo contrario devuelve la misma ruta
	private String obtenerRutaZip(String zipped, String ruta) throws IOException {
		String nuevaRuta = ruta;
		if (zipped != null && zipped.equalsIgnoreCase("S")) {
            final String zipPath = FilenameUtils.removeExtension(ruta) + ".zip";
            ZipUtil.generateZip(ruta, zipPath);
            nuevaRuta = zipPath;	                   
         }
		return nuevaRuta;
	}
	
}
