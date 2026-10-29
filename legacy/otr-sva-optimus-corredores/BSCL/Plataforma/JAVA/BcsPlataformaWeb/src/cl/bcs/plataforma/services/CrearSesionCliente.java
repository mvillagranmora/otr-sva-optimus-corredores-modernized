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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.nuam.StoredProcedure;

/**
 * Servlet implementation class CrearSesionCliente
 */
@WebServlet("/LoginCliente")
public class CrearSesionCliente extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name="jdbc/hanaDB") DataSource dataSource;

    /**
     * Default constructor. 
     */
    public CrearSesionCliente() {
        // TODO Auto-generated constructor stub
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
	 * @throws SQLException 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
		try {
			performTask(request, response);
		}catch (Exception er) {
			er.printStackTrace();			
		}
	}

	
	protected void performTask(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException {
		
				
		StringBuilder sb = new StringBuilder();
        String s;
		while ((s = request.getReader().readLine()) != null) {
            sb.append(s);
        } 
		String bodyParams=sb.toString();
		JsonParser parser 		= new JsonParser();
        JsonObject dataJsonIn	= parser.parse(bodyParams).getAsJsonObject();
        
        String IdEmpresa = (dataJsonIn.get("p_IdEmpresa")!= null && !dataJsonIn.get("p_IdEmpresa").isJsonNull())? dataJsonIn.get("p_IdEmpresa").getAsString() : "";
        String NombreUsuario = (dataJsonIn.get("p_Username") !=null &&!dataJsonIn.get("p_Username").isJsonNull())? dataJsonIn.get("p_Username").getAsString() : "";
        String ClaveUsuario = (dataJsonIn.get("p_Password") != null && !dataJsonIn.get("p_Password").isJsonNull())? dataJsonIn.get("p_Password").getAsString() : "";
        try (Connection conn = dataSource.getConnection();){
    		        		
    		StoredProcedure kCallSrv=new StoredProcedure("BSCL.CanalCliente.BD.Servicios::CrearSesionUsuarioCliente");
    		
    		JsonObject dataJsonCallIn = new JsonObject();
    		dataJsonCallIn.addProperty("p_IdEmpresa", Long.parseLong(IdEmpresa));
    		dataJsonCallIn.addProperty("p_Username", NombreUsuario.toUpperCase());
    		dataJsonCallIn.addProperty("p_Password", ClaveUsuario);
    		
    		JsonObject dataJsonCallOut=kCallSrv.call(conn, dataJsonCallIn);

    		if(dataJsonCallOut.get("P_OUT_STATUSCODE") != null && !dataJsonCallOut.get("P_OUT_STATUSCODE").isJsonNull() && dataJsonCallOut.get("P_OUT_STATUSCODE").getAsString().equals("0")){
    			response.setStatus(200);
    			response.getOutputStream().write(dataJsonCallOut.toString().getBytes("UTF-8")); 
    			response.getOutputStream().flush();	
    			conn.commit();
    			return;
    		}else{
    			//no es un usuario valido o es cliente intentando acceder a backoffice
    			defaultError(request, response, Integer.parseInt(dataJsonCallOut.get("P_OUT_STATUSCODE").toString()));
    			return;
    		}
    		
		} catch (Exception e) {
			System.out.println("Error al iniciar sesion usuario:" + NombreUsuario +" - CANAL_CLIENTE");
			System.out.println(e.getMessage());
			defaultError(request, response, 3);
		}
        
	}
	
	private void defaultError(HttpServletRequest request, HttpServletResponse response, int statusCode) throws IOException{
    	JsonObject errorOut = new JsonObject();
    	errorOut.addProperty("statusCode", statusCode);
    	
    	response.setStatus(200);
		response.getOutputStream().write(errorOut.toString().getBytes("UTF-8")); 
		response.getOutputStream().flush();
	}

}
