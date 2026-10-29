package cl.bcs.plataforma.pla;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AgregarMensajeWebsocket implements JavaCall{	

	private final String SQL_Insert = loadResourceToString("cl/bcs/plataforma/pla/sql/AgregarMensajeWebSocket_Insert.sql");;
	
	// ENTRADA SERVICIO
	public static class EntradaServicio {
		public Long						p_IdEmpresa;
		public Long						p_IdUsuario;
		public String					p_IdModulo;
		//
		public List<MensajeWebSocket>	p_Mensajes;
		//
		public String toString() {
			return Utils.printJson(this, false);
		}
	}		
	public static class MensajeWebSocket {
		public Long		IdEmpresa;
		public String	Evento;
		public String	DATA;
	}
	//SALIDA SERVICIO
	public static class SalidaServicio {
		public Long P_OUT_ERRORESCONTADOR;
	}
	
	/**********************
	 * main pruebas
	 **********************/
	public static void main(String[] args){
        System.out.println("== START ==");
        try {
            Connection con = PlataformaUtils.getDESAConnection();
            // ---------------------------------
            try {
                JsonParser parser = new JsonParser();                
                JsonObject dataJsonIn = parser.parse("{\"IdEmpresa\":1,\"p_IdUsuario\":102,\"p_IdModulo\":'PLA',\"p_Mensajes\":[{\"IdEmpresa\":1,\"Evento\":'AVISO',\"DATA\":'HOLA MUNDO'}]} ").getAsJsonObject();
                AgregarMensajeWebsocket demo = new AgregarMensajeWebsocket();                
                System.out.println(demo.call(con, dataJsonIn).toString());
                con.commit();
            } catch (Exception er) {
                er.printStackTrace();
                con.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== END ==");    		
	}

	/**********************
	 * Transporte
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		EntradaServicio in=gson.fromJson(jin.toString() ,EntradaServicio.class);
		SalidaServicio out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	/**********************
	 * Logica
	 **********************/	
	public SalidaServicio logica(Connection con, EntradaServicio entrada) throws Exception {
		SalidaServicio salida = new SalidaServicio();
		salida.P_OUT_ERRORESCONTADOR = new Long(0);
		SQL sql=new SQL();		
		//Registra los datos en la BD
		if(entrada.p_Mensajes.size()>0){
			sql.batch(con,SQL_Insert, entrada.p_Mensajes, MensajeWebSocket.class);
		}		
		return salida;
	}
	
	public static void agregar(Connection con,Long	IdEmpresa,Long	IdUsuario,String Modulo,List<MensajeWebSocket> p_Mensajes) throws Exception {
		EntradaServicio entrada=new EntradaServicio();
		entrada.p_IdEmpresa=IdEmpresa;
		entrada.p_IdUsuario=IdUsuario;
		entrada.p_IdModulo=Modulo;
		entrada.p_Mensajes=p_Mensajes;
		AgregarMensajeWebsocket obj=new AgregarMensajeWebsocket();
		obj.logica(con,entrada);
	}
	
	public static void agregar(Connection con,Long	IdEmpresa, String	Evento,String	DATA) throws Exception {
		EntradaServicio entrada=new EntradaServicio();
		entrada.p_IdEmpresa=IdEmpresa;
		entrada.p_IdUsuario=0L;
		entrada.p_IdModulo="PLA";
		entrada.p_Mensajes=new ArrayList<>();
		
		MensajeWebSocket ws=new MensajeWebSocket();
		ws.IdEmpresa=IdEmpresa;
		ws.Evento=Evento;
		ws.DATA=DATA;
		entrada.p_Mensajes.add(ws);
		
		AgregarMensajeWebsocket obj=new AgregarMensajeWebsocket();
		obj.logica(con,entrada);		
	}	

}
