package cl.bcs.APV;

import java.math.BigDecimal;
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
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarBonificacion implements JavaCall{	
	
	private final String SQL_UPDATE_BONIFICACION = loadResourceToString("cl/bcs/APV/sql/ModificarBonificacion.sql");
	
	public static class Bonificacion {
		public Long			IdBonificacion;
		public BigDecimal	MontoABonificar;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};

	public static class ModificarBonificacion_Entrada {
	    public Long					p_IdEmpresa;
	    public Long					p_IdUsuario;
	    public String				p_IdModulo;
	    public List<Bonificacion>	p_Parametros;
	    
	    public ModificarBonificacion_Entrada(){
	    	p_Parametros = new ArrayList<Bonificacion>();
		}
	   
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ModificarBonificacion_Salida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ModificarBonificacion_Salida(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}		
	};
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ModificarBonificacion_Entrada jin = gson.fromJson(in.toString() ,ModificarBonificacion_Entrada.class);
		ModificarBonificacion_Salida out = logica(conn,jin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public ModificarBonificacion_Salida logica(Connection con, ModificarBonificacion_Entrada in) throws Exception {
		ModificarBonificacion_Salida salida = new ModificarBonificacion_Salida();
		SQL sql = new SQL();
		MensajeSalida	msg	= new MensajeSalida();
		
		for(Bonificacion fila : in.p_Parametros){
			
			Bonificacion Bon = new Bonificacion();

		  	Bon.IdBonificacion 	= fila.IdBonificacion;
		  	Bon.MontoABonificar	= fila.MontoABonificar;
		  			
			List<Bonificacion> ParamsBon = new ArrayList<Bonificacion>();
			
			ParamsBon.add(Bon);

			int[] res = sql.batch(con, SQL_UPDATE_BONIFICACION ,ParamsBon, Bonificacion.class);

			if(res!=null && res.length>0 && res[0]==1){
				msg.id = 1L;
				msg.tipo = "APV";
				msg.codigo = "OK";
				msg.descripcion = "Bonificación Modificada";
				salida.P_OUT_MENSAJES.add(msg);
				salida.P_OUT_ERRORESCONTADOR = 0L;	
			}else{
				msg.id = 1L;
				msg.tipo = "APV";
				msg.codigo = "ERROR";
				msg.descripcion = "Error al Modificar Bonificación";
				salida.P_OUT_MENSAJES.add(msg);
				salida.P_OUT_ERRORESCONTADOR = 1L;
			}
		}
		
		return salida;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			try {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":2,\"p_IdModulo\":\"APV\",\"p_RelacionLaboral\":[]}";
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":17,\"p_IdModulo\":\"APV\",\"p_Parametros\":[{\"IdBonificacion\":210,\"MontoABonificar\":100},{\"IdBonificacion\":208,\"MontoABonificar\":200}]}";
				
				ModificarBonificacion_Entrada Objin = gson.fromJson(json, ModificarBonificacion_Entrada.class);
				
				ModificarBonificacion demo = new ModificarBonificacion();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			
	 			try {
	 				ModificarBonificacion_Salida ObjOut = demo.logica(con, Objin);
	 				con.commit();
	 				System.out.println(ObjOut.toString());
	 			}
	 			catch(Exception er) {
					//er.printStackTrace();	
					con.rollback();
				}
	 			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
}