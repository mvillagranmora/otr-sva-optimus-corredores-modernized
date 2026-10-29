package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.PerfilFondoMutuo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ObtenerPerfilFondoMutuo implements JavaCall{
	
	private final Log log = new Log("FM.ObtenerPerfilFondoMutuo");
	private final String SQL_PERFIL_FONDO_MUTUO = loadResourceToString("cl/bcs/fondomutuo/sql/Comunes_SelectPerfilFondoMutuo.sql");
	
	public static class EntradaObtenerPerfilFondoMutuo{
		public Long p_IdUsuario;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaObtenerPerfilFondoMutuo{
		
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public String PerfilFondoMutuo;
		
		public SalidaObtenerPerfilFondoMutuo(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaObtenerPerfilFondoMutuo jin = gson.fromJson(in.toString(), EntradaObtenerPerfilFondoMutuo.class);
		SalidaObtenerPerfilFondoMutuo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaObtenerPerfilFondoMutuo logica(Connection con, EntradaObtenerPerfilFondoMutuo entrada) throws Exception{
		log.write("INCIO ObtenerPerfilFondoMutuo");
		SalidaObtenerPerfilFondoMutuo out = new SalidaObtenerPerfilFondoMutuo();
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdUsuario", entrada.p_IdUsuario);
		List<PerfilFondoMutuo> perfilesFondoMutuo = sql.query(con, SQL_PERFIL_FONDO_MUTUO, params, PerfilFondoMutuo.class);
		if(perfilesFondoMutuo.size() > 0){
			out.PerfilFondoMutuo = perfilesFondoMutuo.get(0).Perfil;
		}
		log.write("FIN ObtenerPerfilFondoMutuo");
		return out;
	}
	

}
