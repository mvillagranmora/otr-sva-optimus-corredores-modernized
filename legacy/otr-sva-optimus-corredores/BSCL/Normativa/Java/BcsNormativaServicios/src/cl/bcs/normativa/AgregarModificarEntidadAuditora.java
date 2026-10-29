package cl.bcs.normativa;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.MRF.CrearOperacionMesa.DatosOperacion;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AgregarModificarEntidadAuditora implements JavaCall {
	

	public static class ObjDetalleParidad {
		   
	
	    
	    public String toString() {
			return Utils.printJson(this, true);
		}
	}
	
	public static class AgregarModificarEntidadAuditora_Entrada {
		public List<EntidadAuditora> EntidadAuditora_in;
		public String Accion;
	}
		
	
	public static class EntidadAuditora {
		
		public Long IdEntidadAuditora;
		public String EmpresaAuditora;
		public String AuditorEncargado;
		public String Casilla;
		public String TipoCasilla;
		public String Estado;
		
	}
	
	public static class  AgregarModificarEntidadAuditora_Salida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
	}
	
	private static FileLoader loader = new FileLoader();
	
	private static String SQL_UPDATE_ENTIDAD_AUDITORA = loader.loadResourceToString("cl/bcs/normativa/sql/AgregarModificarEntidadAuditora_UpdateEntidadAuditora.sql");
	private static String SQL_INSERT_ENTIDAD_AUDITORA = loader.loadResourceToString("cl/bcs/normativa/sql/AgregarModificarEntidadAuditora_InsertEntidadAuditora.sql");
	

	
	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		AgregarModificarEntidadAuditora_Entrada in=gson.fromJson(jin.toString() ,AgregarModificarEntidadAuditora_Entrada.class);
		AgregarModificarEntidadAuditora_Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public  AgregarModificarEntidadAuditora_Salida logica(Connection con, AgregarModificarEntidadAuditora_Entrada entrada) throws Exception {
		AgregarModificarEntidadAuditora_Salida out = new  AgregarModificarEntidadAuditora_Salida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_MENSAJE = "";
		
		
        try {
	        SQL sql = new SQL();
	        if("CREAR".equals(entrada.Accion)) {
	        	
	        	sql.batch(con, SQL_INSERT_ENTIDAD_AUDITORA, entrada.EntidadAuditora_in, EntidadAuditora.class);
	        	out.P_OUT_MENSAJE = "OK";
	        }
	        if("MODIFICAR".equals(entrada.Accion)) {
	        	
	        	sql.batch(con, SQL_UPDATE_ENTIDAD_AUDITORA, entrada.EntidadAuditora_in, EntidadAuditora.class);
	        	out.P_OUT_MENSAJE = "OK";
	        }
	    
	    
        } catch (SQLException e) {
        	out.P_OUT_ERRORESCONTADOR = 1L;
			out.P_OUT_MENSAJE = "";
			e.printStackTrace();
			return out;
		}
        con.commit();
		return out;
		 
	}
	
	public static void main (String [] args) throws Exception{
		
        //prueba de la clase
  		try {
  			Connection con = PlataformaUtils.getDESAConnection();
  			//Connection con = PlataformaUtils.getQAConnection();
  			try{
  				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
  				 		
  				
  				String json = "{\"Accion\": \"MODIFICAR\", \"EntidadAuditora_in\": [{\"IdEntidadAuditora\" : 3,\r\n"
  						+ "                \"Nombre\" : \"Auditora los palitos\",\r\n"
  						+ "                \"TipoCasilla\" : \"ELECTRONICA\",\r\n"
  						+ "                \"Casilla\" :\"PALITOS@PRUEBAS.COM\",\r\n"
  						+ "                \"Estado\" : \"INA\"}]}";
  				AgregarModificarEntidadAuditora_Entrada Objin_json = gson.fromJson(json, AgregarModificarEntidadAuditora_Entrada.class);
  				
  				AgregarModificarEntidadAuditora demo = new AgregarModificarEntidadAuditora();	
  				AgregarModificarEntidadAuditora_Salida ObjOut = demo.logica(con, Objin_json);  				
  				System.out.println(ObjOut.toString());
  				//con.rollback();
 				
  				con.commit();
  				
  			}catch(Exception e){
  				con.rollback();
  				e.printStackTrace();
  			}finally{
  				con.close();
  			}
  		}catch (Exception e) {
  			e.printStackTrace();
  		}
  		
        System.out.println("== END ==");
	}
}
