package cl.bcs.ordenes;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesEntrada;
import cl.bcs.ordenes.CrearOrdenes.CrearOrdenesSalida;
import cl.bcs.ordenes.pojo.FormaDePagoOrden;
import cl.bcs.ordenes.pojo.Orden;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarFormaDePagoUltimaOrden implements JavaCall {

	private final Log log = new Log("ORD.ConsultarFormaDePagoUltimaOrden");

	private final String SQL_SELECT_ULTIMA_ORDEN = loadResourceToString("cl/bcs/ordenes/sql/ConsultarFormaDePagoUltimaOrden_SelectUltimaOrden.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarFormaDePagoUltimaOrden_SelectFormaDePago.sql");
	private final String SQL_SELECT_FORMA_DE_PAGO_POR_DEFECTO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarFormaDePagoUltimaOrden_SelectFormaDePagoPorDefecto.sql");
	
	public static class ConsultarFormaDePagoUltimaOrden_Entrada {

		public Long p_IdPortafolio;
		public String p_IdTipoOperacion;
		public String p_IdMonedaLiquidacionOrden;

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ConsultarFormaDePagoUltimaOrden_Salida {
		
		public List<FormaDePagoOrden> p_out_FormaDePagoOrden;
		public Long	 P_OUT_ERRORESCONTADOR = 0L;

		public ConsultarFormaDePagoUltimaOrden_Salida() {
			p_out_FormaDePagoOrden = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ConsultarFormaDePagoUltimaOrden_Entrada in = gson.fromJson(jin.toString(), ConsultarFormaDePagoUltimaOrden_Entrada.class);
		ConsultarFormaDePagoUltimaOrden_Salida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public ConsultarFormaDePagoUltimaOrden_Salida logica(Connection con, ConsultarFormaDePagoUltimaOrden_Entrada in) throws Exception {
		ConsultarFormaDePagoUltimaOrden_Salida out = new ConsultarFormaDePagoUltimaOrden_Salida();
		SQL _sql = new SQL();
		
		HashMap<String,Object> params1 = new HashMap<String,Object>();
		params1.put("IdPortafolio", in.p_IdPortafolio);
		params1.put("IdTipoOperacion", in.p_IdTipoOperacion);
		params1.put("IdMonedaLiquidacionOrden", in.p_IdMonedaLiquidacionOrden);
		
		List<Orden> ultimaOrden = _sql.query(con,SQL_SELECT_ULTIMA_ORDEN, params1, Orden.class);
		
		List<FormaDePagoOrden> formaDePagoUltimaOrden = new ArrayList<FormaDePagoOrden>();
		if (ultimaOrden.size() > 0) {
			HashMap<String,Object> params2 = new HashMap<String,Object>();
			params2.put("IdOrden", ultimaOrden.get(0).IdOrden);
			formaDePagoUltimaOrden = _sql.query(con,SQL_SELECT_FORMA_DE_PAGO_POR_DEFECTO, params1, FormaDePagoOrden.class);
		} 
		
		if (formaDePagoUltimaOrden.size() > 0) {
			out.p_out_FormaDePagoOrden.add(formaDePagoUltimaOrden.get(0));
		}
				
		log.write("TERMINO");
		
		return out;

	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdPortafolio\":343710,\"p_IdTipoOperacion\":\"COMACN\",\"p_IdMonedaLiquidacionOrden\":\"CLP\"}";
			ConsultarFormaDePagoUltimaOrden_Entrada Objin = gson.fromJson(json, ConsultarFormaDePagoUltimaOrden_Entrada.class);
			
			ConsultarFormaDePagoUltimaOrden demo=new ConsultarFormaDePagoUltimaOrden();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ConsultarFormaDePagoUltimaOrden_Salida ObjOut=demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
}
