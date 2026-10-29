package cl.bcs.plataforma.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.utils.Fecha;

@WebServlet("/ProcesarIdTransaccion")
public class ProcesarIdTransaccion extends HttpServlet {
	private static final long serialVersionUID = 1L;
	final String INSERT_TRANSACCION_WS = "INSERT INTO \"BSCL.Plataforma.BD::PLA.TransaccionWebServices\"(\"IdTransaccionWS\",\"Servicio\",\"FechaInicio\") VALUES(?,?,?)";
	final String DELETE_TRANSACCION_WS = "DELETE FROM \"BSCL.Plataforma.BD::PLA.TransaccionWebServices\" WHERE \"IdTransaccionWS\" = ?";
	final int MAX_LENGTH_ID = 36;
	
	final String GENERIC_ERROR_CODE = "ERROR_DESCONOCIDO";
	final String GENERIC_ERROR_MESSAGE = "Error al procesar el ID de transacción";
	final int GENERIC_ERROR_HTTPCODE = 500;
	
	final String ID_LENGTH_ERROR_CODE = "LARGO_ID_SUPERA_LIMITE";
	final String ID_LENGTH_ERROR_MESSAGE = "El largo máximo del ID transacción es de 60 caracteres";
	final int ID_LENGTH_ERROR_HTTPCODE = 400;
	
	final String PARAMETERS_ERROR_CODE = "CANTIDAD_PARAMETROS_INCORRECTOS";
	final String PARAMETERS_ERROR_MESSAGE = "Cantidad de parámetros incorrectos";
	final int PARAMETERS_ERROR_HTTPCODE = 400;
	
	final String INSERT_ERROR_CODE = "TRANSACCION_BLOQUEADA_WS";
	final String INSERT_ERROR_MESSAGE = "ID transacción bloqueado";
	final int INSERT_ERROR_HTTPCODE = 409;
	
	final String INSERT_SUCESS_CODE = "TRANSACCION_INGRESADA_WS";
	final String INSERT_SUCESS_MESSAGE = "ID transacción ingresado";
	final int INSERT_SUCESS_HTTPCODE = 201;
	
	final String DELETE_SUCESS_CODE = "ID_TRANSACCION_ELIMINADO_WS";
	final String DELETE_SUCESS_MESSAGE = "ID transacción eliminado";
	final int DELETE_SUCESS_HTTPCODE = 200;
	
	final String INVALID_ACTION_CODE = "ACCION_INVALIDA";
	final String INVALID_ACTION_MESSAGE = "Acción no soportada";
	final int INVALID_ACTION_HTTPCODE = 400;

	@Resource(name = "jdbc/hanaDB")
	DataSource dataSource;
	
	public ProcesarIdTransaccion() {
		super();
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			performTask(request, response);
		} catch (Exception e) {
			e.printStackTrace();			
		}
	}
	
	protected void performTask(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			StringBuilder sb = new StringBuilder();
	        String s;
			InputStream inputStream = request.getInputStream();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream , StandardCharsets.UTF_8));
	        
	        while ((s = reader.readLine()) != null) {
	            sb.append(s);
	        }
	        
	        String bodyParms = sb.toString();
			String[] params = bodyParms.split("@@@");
			
			if (params.length != 4) {
				errorMessage(response, PARAMETERS_ERROR_CODE, PARAMETERS_ERROR_MESSAGE, PARAMETERS_ERROR_HTTPCODE);
				return;
			}
			
			String action = params[0];
			
			switch (action) {
			    case "INSERT":
			        insert(params, response);
			        return;
			    case "DELETE":
			        delete(params, response);
			        return;
			    default:
			        errorMessage(response, INVALID_ACTION_CODE, INVALID_ACTION_MESSAGE, INVALID_ACTION_HTTPCODE);
			        return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage(response, GENERIC_ERROR_CODE, GENERIC_ERROR_MESSAGE, GENERIC_ERROR_HTTPCODE);
			return;
		}
	}
	
	private void errorMessage(HttpServletResponse response, String code, String message, int httpCode) throws Exception {
    	JsonObject errorOut = new JsonObject();
    	errorOut.addProperty("P_OUT_ERRORESCONTADOR", 1);
    	JsonArray errorDetalleOut = new JsonArray();
    	
    	JsonObject obj = new JsonObject();
    	
    	obj.addProperty("IdInterno", 0L);
    	obj.addProperty("TipoMensaje", "ERROR");
    	obj.addProperty("Codigo", code);
		obj.addProperty("Mensaje", message);
    	errorDetalleOut.add(obj);
    	errorOut.add("P_OUT_MENSAJES", errorDetalleOut);
    	
    	response.setStatus(httpCode);
		response.getOutputStream().write(errorOut.toString().getBytes("UTF-8")); 
		response.getOutputStream().flush();
	}
	
	private void successMessage(HttpServletResponse response, String code, String message, int httpCode) throws Exception {
    	JsonObject successOut = new JsonObject();
    	successOut.addProperty("P_OUT_ERRORESCONTADOR", 0);
    	JsonArray successDetalleOut = new JsonArray();
    	
    	JsonObject obj = new JsonObject();
    	
    	obj.addProperty("IdInterno", 0L);
    	obj.addProperty("TipoMensaje", "AVISO");
    	obj.addProperty("Codigo", code);
		obj.addProperty("Mensaje", message);
		successDetalleOut.add(obj);
    	successOut.add("P_OUT_MENSAJES", successDetalleOut);
    	
    	response.setStatus(httpCode);
		response.getOutputStream().write(successOut.toString().getBytes("UTF-8")); 
		response.getOutputStream().flush();
	}
	
	private void insert(String[] params, HttpServletResponse response) throws Exception {
		String idTransaccionWS = params[1];
		
		if(idTransaccionWS.length() > MAX_LENGTH_ID) {
			errorMessage(response, ID_LENGTH_ERROR_CODE, ID_LENGTH_ERROR_MESSAGE, ID_LENGTH_ERROR_HTTPCODE);
			return;
		}
			
		String servicio = params[2];
		Timestamp fechaInicio = Fecha.toTimeStamp(params[3]);
				
		try (Connection conn = dataSource.getConnection()) {
			PreparedStatement p = conn.prepareStatement(INSERT_TRANSACCION_WS);
			
			p.setString(1,idTransaccionWS);
			p.setString(2, servicio);
			p.setTimestamp(3, fechaInicio);
			
			p.executeUpdate();
			conn.commit();
			
			successMessage(response, INSERT_SUCESS_CODE, INSERT_SUCESS_MESSAGE, INSERT_SUCESS_HTTPCODE);
			return;
		} catch (SQLException err) {
			if (err.getErrorCode() == 301) {
				errorMessage(response, INSERT_ERROR_CODE, INSERT_ERROR_MESSAGE, INSERT_ERROR_HTTPCODE);
			} else {
				err.printStackTrace();
				errorMessage(response, GENERIC_ERROR_CODE, GENERIC_ERROR_MESSAGE, GENERIC_ERROR_HTTPCODE);
			}
			return;
		}
	}
	
	private void delete(String[] params, HttpServletResponse response) throws Exception {
		String idTransaccionWS = params[1];
				
		try (Connection conn = dataSource.getConnection()) {
			PreparedStatement p = conn.prepareStatement(DELETE_TRANSACCION_WS);
			
			p.setString(1,idTransaccionWS);
			
			p.executeUpdate();
			conn.commit();
			
			successMessage(response, DELETE_SUCESS_CODE, DELETE_SUCESS_MESSAGE, DELETE_SUCESS_HTTPCODE);
			return;
		} catch (SQLException err) {
			err.printStackTrace();
			errorMessage(response, GENERIC_ERROR_CODE, GENERIC_ERROR_MESSAGE, GENERIC_ERROR_HTTPCODE);
			return;
		}
	}
}
