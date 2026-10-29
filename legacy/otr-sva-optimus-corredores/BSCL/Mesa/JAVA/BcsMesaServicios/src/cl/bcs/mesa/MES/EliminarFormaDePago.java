package cl.bcs.mesa.MES;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarFormaDePago  implements JavaCall {
	
	private final String SQL_DELETE_COMPROBANTE_PAGO_OPERACION = loadResourceToString("cl/bcs/mesa/MES/sql/EliminarFormaDePago_DeleteComprobantePagoOperacion.sql");

	
	public class EliminarFormaDePagoIN{
		
		public Long p_IdComprobantePagoOperacion;
		public String p_NombreFormaDePago;
		public Long p_IdEmpresa;
		public Long p_IdUsuario;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class EliminarFormaDePagoObjQuery{
		public Long IdComprobantePagoOperacion;
		
	}
	public class EliminarFormaDePagoObjOut{
		public Long IdComprobantePagoOperacion;
		public String NombreFormaDePago;
		public Long IdEmpresa;
		public Long IdUsuario;
		
	}
	
	
	public class EliminarFormaDePagoOut{
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<EliminarFormaDePagoObjOut>	P_OUT_COMPRABANTEPAGO;
		
		public EliminarFormaDePagoOut() {
			this.P_OUT_COMPRABANTEPAGO 		= new ArrayList<EliminarFormaDePagoObjOut>();
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();

		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public EliminarFormaDePagoOut logica (Connection conn, EliminarFormaDePagoIN in) throws Exception {
		SQL _sql = new SQL();
		Long Id=0L;
		List<EliminarFormaDePagoObjQuery> v_pagoEliminar= new ArrayList<>();
		List<EliminarFormaDePagoObjOut> eliminarFPOut = new ArrayList<>();
		
		EliminarFormaDePagoObjOut  formaPago = new EliminarFormaDePagoObjOut();
		EliminarFormaDePagoObjQuery  formaPagoQ = new EliminarFormaDePagoObjQuery();
		EliminarFormaDePagoOut out = new EliminarFormaDePagoOut();
		
		formaPagoQ.IdComprobantePagoOperacion = in.p_IdComprobantePagoOperacion;
		v_pagoEliminar.add(formaPagoQ);
		
		_sql.batch(conn, SQL_DELETE_COMPROBANTE_PAGO_OPERACION, v_pagoEliminar, EliminarFormaDePagoObjQuery.class);
		
		formaPago.IdComprobantePagoOperacion=in.p_IdComprobantePagoOperacion;
		formaPago.NombreFormaDePago= in.p_NombreFormaDePago;
		eliminarFPOut.add(formaPago);
		
		HashMap<String,String> mapMsg = new HashMap<>();
		
		for(EliminarFormaDePagoObjOut formPag: eliminarFPOut) {
			mapMsg.put("NombreFormaDePago", formPag.NombreFormaDePago);
			out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"COMPROBANTE_PAGO_ELIMINADO",mapMsg,conn,in.p_IdEmpresa));
			out.P_OUT_COMPRABANTEPAGO.add(formPag);
			Id++;
			
		}
		return out;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EliminarFormaDePagoIN jin = gson.fromJson(in.toString(), EliminarFormaDePagoIN.class);
		EliminarFormaDePagoOut out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args)  throws Exception,SQLException {
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getDESAConnection();
			//datos entrada
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdComprobantePagoOperacion\":18276,\"p_NombreFormaDePago\":\"CHEQUE\"}";
			EliminarFormaDePagoIN objIn = gson.fromJson(json,EliminarFormaDePagoIN.class);
			//mientras pruebo hare el rollback
			EliminarFormaDePago demo = new EliminarFormaDePago();
			
			EliminarFormaDePagoOut ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			
			conn.rollback();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
		System.out.println("== END ==");
	};
}
