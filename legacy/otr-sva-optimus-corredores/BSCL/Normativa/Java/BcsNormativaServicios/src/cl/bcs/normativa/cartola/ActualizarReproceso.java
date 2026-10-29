package cl.bcs.normativa.cartola;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarReproceso implements JavaCall{
	
	private final Log log = new Log("NOR.ActualizarReproceso");
	
	private final String SQL_ACTUALIZAR_CARTOLA = loadResourceToString("cl/bcs/normativa/cartola/sql/ActualizarReproceso_ActualizarCartola.sql");
	
	public static class EntradaActualizarReproceso {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public List<Cartola> p_Cartolas;
		
		public EntradaActualizarReproceso(){
			this.p_Cartolas = new ArrayList<Cartola>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartola{
		public Long IdCartola;
		public String Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaActualizarReproceso {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaActualizarReproceso(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		EntradaActualizarReproceso in = gson.fromJson(jin.toString(), EntradaActualizarReproceso.class);
		SalidaActualizarReproceso out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaActualizarReproceso logica(Connection con, EntradaActualizarReproceso entrada) throws Exception {
		log.write("INICIO::ActualizarReproceso");
		
		SQL sql = new SQL();
		
		SalidaActualizarReproceso out = new SalidaActualizarReproceso();
		
		sql.batch(con, SQL_ACTUALIZAR_CARTOLA, entrada.p_Cartolas, Cartola.class);
		
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_INFORMACION, "ACTUALIZAR_REPROCESO_EXITOSO", new HashMap<String,String>() ,con,entrada.p_IdEmpresa));
		
		log.write("FIN::ActualizarReproceso");
		
		return out;
	}

}
