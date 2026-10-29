package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
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


public class ResumenOperacionesDiarias implements JavaCall {
	
	private final String SQL_SELECT_ORDENES = loadResourceToString("cl/bcs/ordenes/sql/ConsultarOperacionesOrdenes_Select.sql");
	private final String SQL_SELECT_FFMM = loadResourceToString("cl/bcs/ordenes/sql/ConsultarOperacionesFFMM_Select.sql");
	private final String SQL_SELECT_MESARF = loadResourceToString("cl/bcs/ordenes/sql/ConsultarOperacionesMesaRF_Select.sql");
	private final String SQL_SELECT_SPOT = loadResourceToString("cl/bcs/ordenes/sql/ConsultarOperacionesSpot_Select.sql");
	private final String SQL_SELECT_FORWARD = loadResourceToString("cl/bcs/ordenes/sql/ConsultarOperacionesForward_Select.sql");
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = new Gson();		
		ResumenOperaciones_Entrada in = gson.fromJson(jin.toString() ,ResumenOperaciones_Entrada.class);
		ResumenOperaciones_Salida out = logica(conn,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_ERRORESDETALLE);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class ResumenOperaciones_Entrada{
		public Long p_IdCliente;
		public String p_FechaOperacion;
	}
	
	public static class ResumenOperaciones_Salida{
		public List<ObjConsultarOperacionesDiarias> ListaOperaciones;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_ERRORESDETALLE;		
		
		public ResumenOperaciones_Salida() {
			ListaOperaciones = new ArrayList<>();
		    P_OUT_ERRORESDETALLE = new ArrayList<>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjConsultarOperacionesDiarias{
		public String FechaOperacion;
		public String Rut;
		public Long Cliente;
		public String NombreCliente;
		public String Portafolio;
		public String Mercado;
		public String TipoInstrumento;
		public BigDecimal Monto;
		public BigDecimal Cantidad; //o ValorRescate
		public String IdMoneda;
		public BigDecimal Precio;
		public BigDecimal Tasa;
		public BigDecimal Paridad;		
		public BigDecimal PrecioPromedio;
		public BigDecimal ParidadPromedio;
		public BigDecimal TasaPromedio;
		public Date FechaVencimiento;
		public Date FechaLiquidacion;
	}

	
	
	
	public ResumenOperaciones_Salida logica(Connection conn, ResumenOperaciones_Entrada entrada) throws Exception {
		SQL sql = new SQL();
		
		ResumenOperaciones_Salida salida = new ResumenOperaciones_Salida();
		
		HashMap<String,Object> consultaRODOrdenes = new HashMap<String,Object>();
		consultaRODOrdenes.put("p_IdCliente", entrada.p_IdCliente);
		consultaRODOrdenes.put("p_FechaOperacion", entrada.p_FechaOperacion);
		
		HashMap<String,Object> consultaRODFFMM = new HashMap<String,Object>();
		consultaRODFFMM.put("p_IdCliente", entrada.p_IdCliente);
		consultaRODFFMM.put("p_FechaOperacion", entrada.p_FechaOperacion);
	
		HashMap<String,Object> consultaRODMRF= new HashMap<String,Object>();
		consultaRODMRF.put("p_IdCliente", entrada.p_IdCliente);
		consultaRODMRF.put("p_FechaOperacion", entrada.p_FechaOperacion);
		
		HashMap<String,Object> consultaRODSPOT = new HashMap<String,Object>();
		consultaRODSPOT.put("p_IdCliente", entrada.p_IdCliente);
		consultaRODSPOT.put("p_FechaOperacion",entrada.p_FechaOperacion);
		
		HashMap<String,Object> consultaRODFORWARD = new HashMap<String,Object>();
		consultaRODFORWARD.put("p_IdCliente", entrada.p_IdCliente);
		consultaRODFORWARD.put("p_FechaOperacion", entrada.p_FechaOperacion);
	    
	    List<ObjConsultarOperacionesDiarias> listaRODOrdenes = sql.query(conn, SQL_SELECT_ORDENES, consultaRODOrdenes, ObjConsultarOperacionesDiarias.class);
	    List<ObjConsultarOperacionesDiarias> listaRODFFMM = sql.query(conn, SQL_SELECT_FFMM, consultaRODFFMM, ObjConsultarOperacionesDiarias.class);
	    List<ObjConsultarOperacionesDiarias> litaRODMRF = sql.query(conn, SQL_SELECT_MESARF, consultaRODMRF, ObjConsultarOperacionesDiarias.class);
	    List<ObjConsultarOperacionesDiarias> listaRODSPOT = sql.query(conn, SQL_SELECT_SPOT, consultaRODSPOT, ObjConsultarOperacionesDiarias.class);
	    List<ObjConsultarOperacionesDiarias> listaRODFORWARD = sql.query(conn, SQL_SELECT_FORWARD, consultaRODFORWARD, ObjConsultarOperacionesDiarias.class);
	    
	    List<ObjConsultarOperacionesDiarias> listaTotal = new ArrayList<ObjConsultarOperacionesDiarias>();
	    listaTotal.addAll(listaRODOrdenes);
	    listaTotal.addAll(listaRODFFMM);
	    listaTotal.addAll(litaRODMRF);
	    listaTotal.addAll(listaRODSPOT);
	    listaTotal.addAll(listaRODFORWARD);
	    
	    for(ObjConsultarOperacionesDiarias oper : listaTotal){
		    salida.ListaOperaciones.add(oper);
	    }

	    salida.P_OUT_ERRORESCONTADOR=0L;
		   
		return salida;
	}
	

	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		    String json = "{\"p_IdCliente\":395174,\"p_FechaOperacion\":'2017-08-07'}";
			
		  	ResumenOperaciones_Entrada ent = gson.fromJson(json, ResumenOperaciones_Entrada.class);

			ResumenOperacionesDiarias demo = new ResumenOperacionesDiarias();
			System.out.println("ENTRADA");
			System.out.println(ent.toString());
			System.out.println("SALIDA");
			try {
				ResumenOperaciones_Salida ObjOut = demo.logica(con, ent);
				con.rollback();
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			// con.commit();

		}

		System.out.println("== END ==");
	}
	
}