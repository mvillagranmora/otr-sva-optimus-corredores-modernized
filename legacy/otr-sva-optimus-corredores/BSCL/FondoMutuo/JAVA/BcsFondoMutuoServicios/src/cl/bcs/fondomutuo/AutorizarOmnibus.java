package cl.bcs.fondomutuo;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizarOmnibus implements JavaCall{
	private final Log log = new Log("FMM.AutorizarOmnibus");
	
	private final String SQL_UPDATE_OMNIBUS_DETALLE = loadResourceToString("cl/bcs/fondomutuo/sql/AutorizarOmnibus_UpdateOmnibusDetalle.sql");
	
	public static class EntradaAutorizarOmnibus{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdEstado;
		public List<Long> p_Omnibus;
		
		public EntradaAutorizarOmnibus(){
			this.p_Omnibus = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaAutorizarOmnibus{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_ERRORESDETALLE;
		
		public SalidaAutorizarOmnibus(){
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_ERRORESDETALLE = new ArrayList<MensajeSalida>(); 
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class UpdateOmnibusDetalle{
		public Long IdOmnibusDetalle;
		public String IdEstado;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaAutorizarOmnibus in=gson.fromJson(jin.toString() ,EntradaAutorizarOmnibus.class);
		SalidaAutorizarOmnibus out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaAutorizarOmnibus logica(Connection conn, EntradaAutorizarOmnibus in) throws Exception {
		log.write("INICIO");
		SalidaAutorizarOmnibus out = new SalidaAutorizarOmnibus();
		SQL sql = new SQL();
		List<UpdateOmnibusDetalle> updateOmnibusDetalle = new ArrayList<UpdateOmnibusDetalle>();
		for(Long om : in.p_Omnibus){
			UpdateOmnibusDetalle op = new UpdateOmnibusDetalle();
			op.IdOmnibusDetalle = om;
			op.IdEstado = in.p_IdEstado;
			updateOmnibusDetalle.add(op);
		}
		
		sql.batch(conn, SQL_UPDATE_OMNIBUS_DETALLE, updateOmnibusDetalle, UpdateOmnibusDetalle.class);
		
		log.write("FIN");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getSURAConnection()) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":35,\"p_IdModulo\":\"FMM\",\"p_IdEstado\":\"AUT\",\"p_Omnibus\":[19]}";
		EntradaAutorizarOmnibus Objin = gson.fromJson(jsonRF, EntradaAutorizarOmnibus.class);
		AutorizarOmnibus  demo=new AutorizarOmnibus();				
		try {
			SalidaAutorizarOmnibus ObjOut=demo.logica(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
	
}
