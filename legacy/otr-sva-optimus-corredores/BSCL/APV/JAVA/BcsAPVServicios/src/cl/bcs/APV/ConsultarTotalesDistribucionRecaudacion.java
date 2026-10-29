 package cl.bcs.APV;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.sql.Date;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import java.math.BigDecimal;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarTotalesDistribucionRecaudacion implements JavaCall{	
	
	private final Log log = new Log("TotalesDistribucionRecaudacion");
	
	private  String SQL_CONSULTA_TOTALES = loadResourceToString("cl/bcs/APV/sql/ConsultarTotalesDistribucionRecaudacion.sql");
	
	/********************
	 * Clases IN/OUT	*
	 ********************/
	//  SERVICIO ENTRADA
	public static class Parametros_Entrada {
		
		public 	String	p_FechaDesde;
		public	String	p_FechaHasta;	

	}
	
	
	public static class Totales_DR {
		public String		NombreEstado;
		public BigDecimal	RegistrosRecaudado;
		public BigDecimal	DisTribuidoTotal;
		public BigDecimal	RecTotal;
		public BigDecimal	Diferencia;
		public String		Grilla;
		
	};
	
	public static class Totales_DR_Salida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Totales_DR>		p_Totales;
		public List<Totales_DR>		p_Resumen;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public Totales_DR_Salida(){
			p_Totales = new ArrayList<Totales_DR>();
		
		}		
	};
	
	
	public Totales_DR_Salida logica(Connection con, Parametros_Entrada in) throws Exception
	{
		Totales_DR_Salida out = new Totales_DR_Salida();
		SQL sql = new SQL();
		HashMap<String,Object> param = new HashMap<>();
		param.put("p_FechaDesde", in.p_FechaDesde);
		param.put("p_FechaHasta", in.p_FechaHasta);
		param.put("Grilla", 'T');
		out.p_Totales = sql.query(con, SQL_CONSULTA_TOTALES, param, Totales_DR.class);
		
		param = new HashMap<>();
		param.put("p_FechaDesde", in.p_FechaDesde);
		param.put("p_FechaHasta", in.p_FechaHasta);
		param.put("Grilla", 'R');
		out.p_Resumen = sql.query(con, SQL_CONSULTA_TOTALES, param, Totales_DR.class);
		/*
		for(Totales_DR row : resultQuery){
			Totales_DR reg;
			if (row.Grilla == String.valueOf('R'))
			{
				reg = new Totales_DR();
				
			}
				
		}
		*/
		return out;
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		Parametros_Entrada Objin = gson.fromJson(in.toString(),Parametros_Entrada.class);
		Totales_DR_Salida Objout = logica(conn,Objin);		
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(Objout)).getAsJsonObject();
        
        
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			ConsultarTotalesDistribucionRecaudacion CTDR = new ConsultarTotalesDistribucionRecaudacion();
			try {
				Parametros_Entrada Objin = new Parametros_Entrada();
 				Objin.p_FechaDesde = "2017-11-15";
 				Objin.p_FechaHasta = "2017-11-01";
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			try { 

	 				Totales_DR_Salida ObjOut = CTDR.logica(con, Objin);
	 				con.commit();
	 				System.out.println(ObjOut.toString());
	 			} 
	 			catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
 
}