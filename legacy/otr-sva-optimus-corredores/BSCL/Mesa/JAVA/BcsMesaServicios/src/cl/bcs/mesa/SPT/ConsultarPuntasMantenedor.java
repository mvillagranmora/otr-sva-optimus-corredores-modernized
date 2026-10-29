package cl.bcs.mesa.SPT;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarPuntasMantenedor implements JavaCall {
	private final String SQL_SELECT_PUNTAS = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarPuntasMantenedor.sql");
	
	public static class PuntasEntrada {
		public String IdMoneda;
		public Long IdEmpresa;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PuntasSalida {
		public List<Puntas>	P_OUT_PUNTAS;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Puntas {
		public Long IdPuntas;
		public String IdMonedaPunta;  	
		public String ExistePunta;						
		public Long IdEmpresa;
		public Timestamp Fecha;
		public String IdUsuario;
		public BigDecimal AperturaPrecio;
		public BigDecimal PuntaCompra;
		public BigDecimal PuntaVenta;
		public String CheckPrecioPorTramo;
		public String Estado;
		public Integer PrecioDecimales;
		public Integer MontoDecimales;
		public Integer TipoCambioDecimales;
		public Integer ParidadDecimales;
		public Integer PuntosDecimales;
		public String Editable;
		public String SistemaModificador;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		PuntasEntrada in = gson.fromJson(jin.toString(), PuntasEntrada.class);
		PuntasSalida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public PuntasSalida logica(Connection conn, PuntasEntrada in) {
		try {
			SQL _sql = new SQL();
			PuntasSalida out = new PuntasSalida();
			String puntasDatatec = ConfiguracionSistema.getParametroConfiguracionString(conn,in.IdEmpresa, "PLATAFORMA", "PLA.HabilitarPuntasDatatec");

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdMoneda", in.IdMoneda);
			params.put("IdEmpresa", in.IdEmpresa);
			
			List<Puntas> selectPuntas = _sql.query(conn, SQL_SELECT_PUNTAS, params, Puntas.class);
			selectPuntas.get(0).Editable = "S".equalsIgnoreCase(puntasDatatec) && "USD".equalsIgnoreCase(in.IdMoneda) ? "N" : "S";
			
			out.P_OUT_PUNTAS = selectPuntas;
			return out;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("dd-MM-yyyy HH:mm:ss").serializeNulls().create();
			String jsonDesa = "{\"IdMoneda\":\"USD\",\"IdEmpresa\":1}";
			PuntasEntrada Objin = gson.fromJson(jsonDesa, PuntasEntrada.class);
			
			ConsultarPuntasMantenedor demo = new ConsultarPuntasMantenedor();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			try {
 				PuntasSalida ObjOut = demo.logica(con, Objin);
 				con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				con.rollback();
			}
			finally {
				con.close();
			}
	    }
		System.out.println("== END ==");
	}
}
