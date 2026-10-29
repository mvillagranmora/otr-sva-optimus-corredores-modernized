package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearOrdenes.ObjOrdenesEntrada;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ObtenerTipoOperacionInstrumento  implements JavaCall{
	
	private final String SQL_SELECT_DETALLE_TIPOOPERACIONINSTRUMENTO = loadResourceToString("cl/bcs/ordenes/sql/ObtenerTipoOperacionInstrumento_SelectTipoOperacionInstrumento.sql");
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ObtenerTipoOperacionInstrumentoEntrada in = gson.fromJson(jin.toString(), ObtenerTipoOperacionInstrumentoEntrada.class);
		ObtenerTipoOperacionInstrumentoSalida out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	
	public static class DetalleTipoOperacionInstrumento {
		
		List<TipoOperacionInstrumento> lstTipoOperacionIntrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class TipoOperacionInstrumento {
		public String IdTipoInstrumento;
		public String IdTipoOperacion;
		public String AccionOrden;
		public String IdTipoProducto;
		public String EsVentaCorta;
		public String Nemotecnico;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public class ObtenerTipoOperacionInstrumentoEntrada {

		public List<TipoOperacionInstrumentoEntrada> p_lstTipoOperacionIntrumento;
		
		public ObtenerTipoOperacionInstrumentoEntrada() {
			this.p_lstTipoOperacionIntrumento = new ArrayList<TipoOperacionInstrumentoEntrada>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public class TipoOperacionInstrumentoEntrada {
		
		public String	p_AccionOrden;
		public String 	p_Nemotecnico;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObtenerTipoOperacionInstrumentoSalida {

		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;

		public List<DetalleTipoOperacionInstrumento>		P_OUT_DETALLETIPOOPERACIONINSTRUMENTO;
		
		public ObtenerTipoOperacionInstrumentoSalida() {
			this.P_OUT_DETALLETIPOOPERACIONINSTRUMENTO = new ArrayList<DetalleTipoOperacionInstrumento>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public ObtenerTipoOperacionInstrumentoSalida logica(Connection conn, ObtenerTipoOperacionInstrumentoEntrada entrada) throws SQLException {
		ObtenerTipoOperacionInstrumentoSalida out = new ObtenerTipoOperacionInstrumentoSalida();
		SQL sql = new SQL();
		
		
		for (TipoOperacionInstrumentoEntrada item : entrada.p_lstTipoOperacionIntrumento) {
			
			DetalleTipoOperacionInstrumento detalleOperacionInstrumento = new DetalleTipoOperacionInstrumento();
			if (item.p_AccionOrden != null && !item.p_AccionOrden.isEmpty()) {
				
				if(item.p_AccionOrden.equalsIgnoreCase("C")) {
					item.p_AccionOrden = "COMPRA";
				}else if(item.p_AccionOrden.equalsIgnoreCase("V")) {
					item.p_AccionOrden = "VENTA";
				}
				
				HashMap<String,Object> params = new HashMap<>();
				params.put("TipoOperacion", item.p_AccionOrden);
				params.put("Nemotecnico", item.p_Nemotecnico);
				
				List<TipoOperacionInstrumento> detalleTipoOperacionInstrumento = sql.query(conn, SQL_SELECT_DETALLE_TIPOOPERACIONINSTRUMENTO, params, TipoOperacionInstrumento.class);
				
				if (detalleTipoOperacionInstrumento.size() == 0) {
					out.P_OUT_MENSAJES.add(new MensajeSalida(1L,MensajeSalida.MENSAJE_ERROR,"TIPO_OPERACION_NO_COINCIDE",null));
					return out;
				}else {
					out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"CONSULTA_OK",null));
					detalleOperacionInstrumento.lstTipoOperacionIntrumento = detalleTipoOperacionInstrumento;
					out.P_OUT_DETALLETIPOOPERACIONINSTRUMENTO.add(detalleOperacionInstrumento);
				}
			}
		}
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new Gson();

			String jsonC = "{\"p_lstTipoOperacionIntrumento\":[{\"p_AccionOrden\":\"C\",\"p_Nemotecnico\":\"COPEC\"}, {\"p_AccionOrden\":\"V\",\"p_Nemotecnico\":\"COPEC\"} ]}";
			
			ObtenerTipoOperacionInstrumentoEntrada Objin = gson.fromJson(jsonC, ObtenerTipoOperacionInstrumentoEntrada.class);

			ObtenerTipoOperacionInstrumento demo=new ObtenerTipoOperacionInstrumento();				
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				ObtenerTipoOperacionInstrumentoSalida ObjOut=demo.logica(con, Objin);
 				con.rollback();
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
