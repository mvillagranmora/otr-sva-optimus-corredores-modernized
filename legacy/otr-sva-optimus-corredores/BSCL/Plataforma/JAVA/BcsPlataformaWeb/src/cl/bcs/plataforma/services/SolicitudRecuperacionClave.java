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
import cl.bcs.utils.Utils;

/**
 * Servlet implementation class RecuperarClave
 */
@WebServlet("/SolicitudRecuperacionClave")
public class SolicitudRecuperacionClave extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Resource(name="jdbc/hanaDB") DataSource dataSource;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SolicitudRecuperacionClave() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			performTask(request, response);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			performTask(request, response);
		} catch (SQLException e) {
			e.printStackTrace();
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
        try (Connection conn = dataSource.getConnection();){
    		        		
        	StoredProcedure kCallSrv=new StoredProcedure("BSCL.Plataforma.BD.Servicios::ACC.RecuperarClaveUsuario");
    		JsonObject dataJsonCallIn = new JsonObject();
    		dataJsonCallIn.addProperty("p_IdEmpresa", Long.parseLong(IdEmpresa));
    		dataJsonCallIn.addProperty("p_Username", NombreUsuario.toUpperCase());
    		dataJsonCallIn.addProperty("p_Token", Utils.generateToken(5));
    		
    		JsonObject dataJsonCallOut=kCallSrv.call(conn, dataJsonCallIn);

    		if(dataJsonCallOut.get("P_OUT_ERRORESCONTADOR") != null && !dataJsonCallOut.get("P_OUT_ERRORESCONTADOR").isJsonNull() && dataJsonCallOut.get("P_OUT_ERRORESCONTADOR").getAsString().equals("0")){
    			JsonObject jsonOut = new JsonObject();
    			jsonOut.addProperty("statusCode", "0");
    			jsonOut.addProperty("statusMessage", "OK");
    			jsonOut.addProperty("recoveryTo", Utils.obfuscateEmail(dataJsonCallOut.get("P_OUT_MAIL").getAsString()));
    			
    			response.setStatus(200);
    			response.getOutputStream().write(jsonOut.toString().getBytes("UTF-8")); 
    			response.getOutputStream().flush();	
    			conn.commit();
    			return;
    		}else{
    			//no es un usuario valido o es cliente intentando acceder a backoffice
    			defaultError(request, response, Integer.parseInt(dataJsonCallOut.get("P_OUT_ERRORESCONTADOR").toString()));
    			return;
    		}
    		
		} catch (Exception e) {
			System.out.println("Error al solicitar recuperacion de password para usuario:" + NombreUsuario);
			System.out.println(e.getMessage());
			defaultError(request, response, 3);
		}
	}
	
	private void defaultError(HttpServletRequest request, HttpServletResponse response, int statusCode) throws IOException{
    	JsonObject errorOut = new JsonObject();
    	errorOut.addProperty("statusCode", statusCode);
    	errorOut.addProperty("statusMessage", "ERROR_RECUPERAR_CLAVE");
    	
    	response.setStatus(200);
		response.getOutputStream().write(errorOut.toString().getBytes("UTF-8")); 
		response.getOutputStream().flush();
	}
	
}
