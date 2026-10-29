package cl.bcs.plataforma.pla;
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


public class GuardarConfiguracionGrillaUsuario implements JavaCall {
	
	private final String SQL_UPDATE_CONFIGURACION= loadResourceToString("cl/bcs/plataforma/pla/sql/GuardarConfiguracionGrillaUsuario_Update.sql");
	private final String SQL_INSERT_CONFIGURACION = loadResourceToString("cl/bcs/plataforma/pla/sql/GuardarConfiguracionGrillaUsuario_Insert.sql");
	private final String SQL_SELECT_CONFIGURACION = loadResourceToString("cl/bcs/plataforma/pla/sql/GuardarConfiguracionGrillaUsuario_Select.sql");
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		Parametros_Entrada in = gson.fromJson(jin.toString() ,Parametros_Entrada.class);
		Parametros_Salida out = logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_ERRORESDETALLE);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class Parametros_Entrada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<ObjConfiguracion> p_Configuracion;
	}
	
	public static class Parametros_Salida{
		public String P_OUT_RESULTADO;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_ERRORESDETALLE;		
		
		public Parametros_Salida(){
			P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjConfiguracion{
		public String Llave;
		public String Valor;
	}
	
	public static class EntidadConfiguracion{
		public Long IdConfiguracionUsuario;
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_Llave;	
		public String p_Valor;
	}
	
	public Parametros_Salida logica(Connection conn, Parametros_Entrada entrada) throws Exception {
		SQL sql = new SQL();
		Parametros_Salida salida = new Parametros_Salida();
		
		HashMap<String,Object> datosBusqueda = new HashMap<String,Object>();
		datosBusqueda.put("p_IdEmpresa", entrada.p_IdEmpresa);
		datosBusqueda.put("p_IdUsuario", entrada.p_IdUsuario);
		datosBusqueda.put("p_Llave", entrada.p_Configuracion.get(0).Llave);
	    
		List<EntidadConfiguracion> existe = sql.query(conn, SQL_SELECT_CONFIGURACION, datosBusqueda, EntidadConfiguracion.class);
	    
		EntidadConfiguracion datosActualizacion = new EntidadConfiguracion();
		datosActualizacion.p_IdEmpresa = entrada.p_IdEmpresa;
		datosActualizacion.p_IdUsuario = entrada.p_IdUsuario;
		datosActualizacion.p_Llave = entrada.p_Configuracion.get(0).Llave;
		datosActualizacion.p_Valor = entrada.p_Configuracion.get(0).Valor;
		List<EntidadConfiguracion> lstEntidadConfiguracion = new ArrayList<EntidadConfiguracion>();
		lstEntidadConfiguracion.add(datosActualizacion);
		
		if(existe.size() > 0){
			sql.batch(conn, SQL_UPDATE_CONFIGURACION, lstEntidadConfiguracion,EntidadConfiguracion.class);
		}else{ 
			sql.batch(conn, SQL_INSERT_CONFIGURACION, lstEntidadConfiguracion, EntidadConfiguracion.class);		
		}
	    
	    salida.P_OUT_ERRORESCONTADOR = 0L;
	    salida.P_OUT_ERRORESDETALLE.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_INFO", "Se guardo correctamente la configuración"));
		   
		return salida;
	}
	

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		    String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"PLA\",\"p_Configuracion\":[{Llave: \"1::21::GRID_grid-transacciones\",Valor:\"test\"}]}";
			
		    Parametros_Entrada ent = gson.fromJson(json, Parametros_Entrada.class);

		    GuardarConfiguracionGrillaUsuario demo = new GuardarConfiguracionGrillaUsuario();
			System.out.println("ENTRADA");
			System.out.println(ent.toString());
			System.out.println("SALIDA");
			try {
				Parametros_Salida ObjOut = demo.logica(con, ent);
				con.rollback();
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			// con.commit();

		}

		System.out.println("== END ==");
	}
	
}