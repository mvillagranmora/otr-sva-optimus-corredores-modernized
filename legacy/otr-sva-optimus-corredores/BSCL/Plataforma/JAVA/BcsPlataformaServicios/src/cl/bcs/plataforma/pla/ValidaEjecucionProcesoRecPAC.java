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

public class ValidaEjecucionProcesoRecPAC implements JavaCall {
	
	private final String SQL_SELECT_FlagRECPAC = loadResourceToString("cl/bcs/plataforma/pla/sql/ValidaEjecucionProcesoRecPAC_SelectRECPAC.sql");
	private final String SQL_UPDATE_FLAG_FECHA = loadResourceToString("cl/bcs/plataforma/pla/sql/ValidaEjecucionProcesoRecPAC_UpdateFlag.sql");
	
	
	//Entrada
	public static class ValidaEjecucionProcesoRecPACEntrada{
		
		public 	Long p_IdEmpresa;
		public String p_ValorNuevo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	//Salida
	public static class ValidaEjecucionProcesoRecPACSalida{
		
		public List<MensajeSalida>			P_OUT_MENSAJES;
		public Long							P_OUT_ERRORESCONTADOR;
		
		public ValidaEjecucionProcesoRecPACSalida() {
			this.P_OUT_MENSAJES 		  = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}	
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public static class ValorFlag{
		
		public String Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		ValidaEjecucionProcesoRecPACEntrada in = gson.fromJson(jin.toString(), ValidaEjecucionProcesoRecPACEntrada.class);
		ValidaEjecucionProcesoRecPACSalida out = logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
		
	}
	
	public ValidaEjecucionProcesoRecPACSalida logica(Connection conn, ValidaEjecucionProcesoRecPACEntrada  in) throws Exception {
		SQL sql = new SQL();
		ValidaEjecucionProcesoRecPACSalida out = new ValidaEjecucionProcesoRecPACSalida();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		HashMap<String,Object> ValorNuevo = new HashMap<String,Object>();
		//update flag 
		List<ValorFlag> ActualizaValor = new ArrayList<ValorFlag>();
		ValorFlag val = new ValorFlag();
		val.Valor 	= in.p_ValorNuevo;
		ActualizaValor.add(val);
		
		if("N".equalsIgnoreCase(in.p_ValorNuevo)) {
			
			sql.batch(conn, SQL_UPDATE_FLAG_FECHA, ActualizaValor, ValorFlag.class);
		
		}else {
		
			List<ValorFlag> valorProceso = sql.query(conn,SQL_SELECT_FlagRECPAC,params, ValorFlag.class); 
			
			if ("S".equalsIgnoreCase(valorProceso.get(0).Valor)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"INTENTE_MAS_TARDE_PROCESO_PAC_REC_EJECUTANDOSE",null,conn,in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR++;
				return out;
			}else {
				
				sql.batch(conn, SQL_UPDATE_FLAG_FECHA, ActualizaValor, ValorFlag.class);
			}
		}
		
		MensajeSalida msg= new MensajeSalida();
		msg.id = 1L;
		msg.tipo = MensajeSalida.MENSAJE_OK;
		msg.codigo = "OK";
		msg.descripcion = "Flag Actualizado";
		out.P_OUT_MENSAJES.add(msg);
	
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_ValorNuevo\":\"S\"}"; 
			ValidaEjecucionProcesoRecPACEntrada Objin = gson.fromJson(json,ValidaEjecucionProcesoRecPACEntrada.class);
			
			ValidaEjecucionProcesoRecPAC demo = new ValidaEjecucionProcesoRecPAC();
			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ValidaEjecucionProcesoRecPACSalida ObjOut = demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 				
 			}catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			con.commit();
	    	}
		System.out.println("== END ==");
	}
}