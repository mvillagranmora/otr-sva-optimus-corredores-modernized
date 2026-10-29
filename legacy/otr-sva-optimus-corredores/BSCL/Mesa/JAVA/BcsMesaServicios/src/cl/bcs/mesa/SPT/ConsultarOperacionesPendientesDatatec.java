package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarOperacionesPendientesDatatec implements JavaCall {
	
	private final Log log = new Log("MES.ConsultarOperacionesPendientesDatatec");

	private final String SQL_SELECT_OPERACIONES_PENDIENTES_DATATEC = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarOperacionesDatatec_SelectOperacionesPendientes.sql");
	
	public static class EntradaConsultarOperacionesPendientesDatatec{
		public Long p_IdEmpresa;
		public String p_Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaConsultarOperacionesPendientesDatatec{
		public List<SalidaOperacion> P_OUT_TABLA;
		
		public SalidaConsultarOperacionesPendientesDatatec(){
			this.P_OUT_TABLA = new ArrayList<SalidaOperacion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaOperacion{
		public String IdDatatec;
		public Long NumeroRegistro;
    	public Timestamp FechaOperacion;
    	public String TipoOperacion;
    	public String GlosaProceso;
    	public String Estado;
    	public String NombreCliente;
		public String IdMonedaPrincipal;
		public String IdMonedaSecundaria;
		public BigDecimal MontoMonedaPrincipal;
		public BigDecimal TipoCambioCierre;
		public BigDecimal MontoMonedaSecundaria;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaConsultarOperacionesPendientesDatatec jin = gson.fromJson(in.toString(), EntradaConsultarOperacionesPendientesDatatec.class);
		SalidaConsultarOperacionesPendientesDatatec out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarOperacionesPendientesDatatec logica(Connection con, EntradaConsultarOperacionesPendientesDatatec entrada) throws Exception {
		log.write("INICIO::ConsultarOperacionesPendientesDatatec");
		SalidaConsultarOperacionesPendientesDatatec out = new SalidaConsultarOperacionesPendientesDatatec();
		SQL sql=new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("p_IdEmpresa", entrada.p_IdEmpresa);
		params.put("p_Fecha", entrada.p_Fecha);
		List<SalidaOperacion> operacionesPendientes = sql.query(con, SQL_SELECT_OPERACIONES_PENDIENTES_DATATEC, params, SalidaOperacion.class);
		out.P_OUT_TABLA = operacionesPendientes;
		
		log.write("FIN::ConsultarOperacionesPendientesDatatec");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String jsonRF = "{\"p_IdEmpresa\":1,\"p_Fecha\":\"2018-10-22\"}";
			EntradaConsultarOperacionesPendientesDatatec Objin = gson.fromJson(jsonRF, EntradaConsultarOperacionesPendientesDatatec.class);
			ConsultarOperacionesPendientesDatatec demo = new ConsultarOperacionesPendientesDatatec();
			try {
				SalidaConsultarOperacionesPendientesDatatec ObjOut = demo.logica(con, Objin);
				//con.rollback();
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
