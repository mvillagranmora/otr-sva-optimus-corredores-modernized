package cl.bcs.custodia;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class GuardarCuadraturaCustodia implements JavaCall{
	
	private final String SQL_UPDATE_CUADRATURA = loadResourceToString("cl/bcs/custodia/sql/CuadraturaCustodia_UpdateCuadraturaCustodia.sql");
	
	public static class GuardarCuadraturaCustodia_Salida{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida>	P_OUT_MENSAJES;
		
		public GuardarCuadraturaCustodia_Salida(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class GuardarDatos_Entrada{
		public Long p_IdEmpresa;
		public List<ExplicacionDescuadratura> p_ExplicacionDescuadratura;
		
		public GuardarDatos_Entrada(){
			this.p_ExplicacionDescuadratura = new ArrayList<ExplicacionDescuadratura>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ExplicacionDescuadratura{
		public Long IdCuadraturaCustodia;
		public String ExplicacionDescuadratura;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public GuardarCuadraturaCustodia_Salida logica(Connection con, GuardarDatos_Entrada entrada) throws Exception {
		
		SQL sql = new SQL();
		GuardarCuadraturaCustodia_Salida out = new GuardarCuadraturaCustodia_Salida();	
		
		if(!entrada.p_ExplicacionDescuadratura.isEmpty()){
			sql.batch(con, SQL_UPDATE_CUADRATURA, entrada.p_ExplicacionDescuadratura, ExplicacionDescuadratura.class);	
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_OPERACION_ACTUALIZAR_EXITO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
			

		return out;		
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		GuardarDatos_Entrada in=gson.fromJson(jin.toString() ,GuardarDatos_Entrada.class);
		GuardarCuadraturaCustodia_Salida out= logica(conn,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAPEConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdCuadraturaCustodia\":689,\"p_ExplicacionDescuadratura\":\"20\",\"p_IdEmpresa\":1}";
			GuardarDatos_Entrada Objin = gson.fromJson(json, GuardarDatos_Entrada.class);
			
			GuardarCuadraturaCustodia demo=new GuardarCuadraturaCustodia();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				GuardarCuadraturaCustodia_Salida ObjOut= demo.logica(con, Objin);
 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
	    	}
		System.out.println("== END ==");
	}
}