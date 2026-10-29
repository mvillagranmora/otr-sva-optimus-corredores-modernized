package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class NotificarAnticipoOrden implements JavaCall {

	private final Log log = new Log("ORD.NotificarAnticipoOrden");

	private final String SQL_CALL = loadResourceToString("cl/bcs/ordenes/sql/NotificarAnticipoOrden_Call.sql");

	public static class NotificarAnticipoOrden_Entrada {
		public Long			p_IdEmpresa;
		public Long			p_IdUsuario;
		public String 		p_IdModulo;
		public List<Cartera> p_Cartera;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartera{
		public Long IdCartera;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}	
	}
	
	public static class NotificarAnticipoOrden_Salida {
		 public Long P_OUT_ERRORESCONTADOR;
	}
	
	public NotificarAnticipoOrden_Salida logica(Connection con, NotificarAnticipoOrden_Entrada in){
		NotificarAnticipoOrden_Salida out = new NotificarAnticipoOrden_Salida();
		
		try {
			SQL sql = new SQL();
			if(in.p_Cartera!=null && !in.p_Cartera.isEmpty()) {
				HashMap<String, Object> params = new HashMap<>();
				params.put("p_IdEmpresa", in.p_IdEmpresa);
				params.put("p_IdUsuario", in.p_IdUsuario);
				params.put("p_IdModulo", in.p_IdModulo);
				params.put("p_anticipos", in.p_Cartera);
				sql.call(con, SQL_CALL, params, new HashMap<>());
			}
			out.P_OUT_ERRORESCONTADOR = (long) 0;
		}catch(Exception e) {
			out.P_OUT_ERRORESCONTADOR = (long) 1;
		}
		return out;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = getGsonBuilder();				
		NotificarAnticipoOrden_Entrada in=gson.fromJson(jin.toString() ,NotificarAnticipoOrden_Entrada.class);
		NotificarAnticipoOrden_Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
		return parser.parse(gson.toJson(out)).getAsJsonObject();
	}	

	public static void main(String[] args){
		
		System.out.println("== TEST ==");
		Long idEmpresa = 1L;
		try (Connection con = PlataformaUtils.getQAPEConnection()) {
			NotificarAnticipoOrden sp = new NotificarAnticipoOrden();
			NotificarAnticipoOrden_Entrada in = new NotificarAnticipoOrden_Entrada();
			in.p_IdEmpresa = idEmpresa;
			in.p_IdModulo = "ORD";
			in.p_IdUsuario = Long.parseLong("21");
			in.p_Cartera = new ArrayList<>();
			Cartera cartera = new Cartera();
			cartera.IdCartera= Long.parseLong("24854");//rf
			in.p_Cartera.add(cartera);

			sp.logica(con, in);
			con.rollback();
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
	}


}