package cl.bcs.custodia;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.replica.ReplicaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class RespaldarCustodia implements JavaCall {	
	
	private static final Logger logger = LogManager.getLogger(RespaldarCustodia.class);

	public static class Entrada {
		public String 		p_FechaRespaldo;
		public String 		p_RespaldaCarteras;
		public String 		p_RespaldaGarantias;
		public String 		p_IdClasificacionTipoInstrumento;
		public String 		p_TipoInstrumento;
		
		public Long 		p_IdClient=0L;
		public Long 		p_IdPortafolio=0L;
		public Long 		p_IdInstrumento=0L;
		public Long 		p_IdCarga=0L;		
		
	}
	
	public static class Salida {
		public Long 		P_OUT_ERRORESCONTADOR;
		public Long 		P_OUT_COUNTCARTERAS;
		public Long 		P_OUT_COUNTSUBYACENTES;
		public Long 		P_OUT_COUNTRESERVAS;
		public Long 		P_OUT_COUNTGARANTIAS;	
		public Long 		P_OUT_COUNTMOVTRANSITO;
		public Long 		P_OUT_COUNTNEGATIVAS;
		
		public Salida() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_COUNTCARTERAS = 0L;
			P_OUT_COUNTSUBYACENTES = 0L;
			P_OUT_COUNTRESERVAS = 0L;
			P_OUT_COUNTGARANTIAS = 0L;
			P_OUT_COUNTMOVTRANSITO = 0L;
			P_OUT_COUNTNEGATIVAS = 0L;
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class LogEjecucion {
		public Long		TimeMillis;
		public String 	Mensaje;
		public String	EsError;
		public String 	EsInterno;
	}


	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {	
		logger.info("*** Inicio de la ejecucion ***");
		Gson gson = new Gson();		
		Entrada in=gson.fromJson(jin.toString() ,Entrada.class);
		Salida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
        logger.info("*** Fin de la ejecucion ***");
		return dataout;
	}

	
	/**********************
	 * lógica del programa
	 **********************/
	public Salida logica(Connection con, Entrada entrada) throws Exception {
		SQL sql=new SQL();
		Salida out=new Salida();
		
		String SQL_CALL_Respaldo = "call \"BSCL.Custodia.BD.Servicios::HIS.RespaldarCustodia\"("
				+ "p_FechaRespaldo=>:p_FechaRespaldo,"
				+ "p_IdClasificacionTipoInstrumento=>:p_IdClasificacionTipoInstrumento,"
				+ "p_IdClient=>:p_IdClient,"
				+ "p_IdPortafolio=>:p_IdPortafolio,"
				+ "p_IdInstrumento=>:p_IdInstrumento,"
				+ "p_TipoInstrumento=>:p_TipoInstrumento,"
				+ "p_IdCarga=>:p_IdCarga,"
				+ "p_out_countCarteras=>@p_out_countCarteras,"
				+ "p_out_countSubyacentes=>@p_out_countSubyacentes,"
				+ "p_out_countReservas=>@p_out_countReservas,"
				+ "p_out_countGarantias=>@p_out_countGarantias,"
				+ "p_out_countNegativas=>@p_out_countNegativas,"
				+ "p_out_countMovTransito=>@p_out_countMovTransito)";
		
		
		
		try {		
			logger.info("Inicia ejecucion de respaldo de custodia: " + entrada);
//			boolean debug = true;			
			out.P_OUT_COUNTCARTERAS=new Long(0);
			out.P_OUT_COUNTSUBYACENTES=new Long(0);
			out.P_OUT_COUNTRESERVAS=new Long(0);
			out.P_OUT_COUNTGARANTIAS=new Long(0);
			out.P_OUT_COUNTNEGATIVAS=new Long(0);
			
			//Escalares de entrada:
			HashMap<String,Object> parmsIn =new HashMap<String,Object>();
			parmsIn.put("p_FechaRespaldo", entrada.p_FechaRespaldo);
			parmsIn.put("p_RespaldaCarteras", entrada.p_RespaldaCarteras);
			parmsIn.put("p_RespaldaGarantias", entrada.p_RespaldaGarantias);
			parmsIn.put("p_IdClasificacionTipoInstrumento", entrada.p_IdClasificacionTipoInstrumento);
			
			parmsIn.put("p_IdClient", entrada.p_IdClient);
			parmsIn.put("p_IdPortafolio", entrada.p_IdPortafolio);
			parmsIn.put("p_IdInstrumento", entrada.p_IdInstrumento);
			parmsIn.put("p_TipoInstrumento", entrada.p_TipoInstrumento);
			parmsIn.put("p_IdCarga", entrada.p_IdCarga);
			
			HashMap<String,Class> parmsOut =new HashMap<String,Class>();
			parmsOut.put("p_out_countCarteras", Long.class);
			parmsOut.put("p_out_countSubyacentes", Long.class);
			parmsOut.put("p_out_countReservas", Long.class);
			parmsOut.put("p_out_countGarantias", Long.class);
			parmsOut.put("p_out_countMovTransito", Long.class);
			parmsOut.put("p_out_countNegativas", Long.class);
			
			
			//Realiza el call
			HashMap<String,Object> respuestaCCA = sql.call(con, SQL_CALL_Respaldo, parmsIn, parmsOut);
			
			//################################
			//		PROCESAR LA SALIDA
			//################################
			
			out.P_OUT_COUNTCARTERAS=(Long)respuestaCCA.get("p_out_countCarteras");
			out.P_OUT_COUNTSUBYACENTES=(Long)respuestaCCA.get("p_out_countSubyacentes");
			out.P_OUT_COUNTRESERVAS=(Long)respuestaCCA.get("p_out_countReservas");
			out.P_OUT_COUNTGARANTIAS=(Long)respuestaCCA.get("p_out_countGarantias");
			out.P_OUT_COUNTMOVTRANSITO=(Long)respuestaCCA.get("p_out_countMovTransito");
			out.P_OUT_COUNTNEGATIVAS =(Long)respuestaCCA.get("p_out_countNegativas");
			
			ReplicaUtils r = new ReplicaUtils();
			r.guardarEnvioPostPauta(con, "Custodia_CarteraFMHis", entrada.p_FechaRespaldo, entrada.p_FechaRespaldo);
			r.guardarEnvioPostPauta(con, "Custodia_CarteraRVHis", entrada.p_FechaRespaldo, entrada.p_FechaRespaldo);
			r.guardarEnvioPostPauta(con, "Custodia_CarteraRFIFHis", entrada.p_FechaRespaldo, entrada.p_FechaRespaldo);
			r.guardarEnvioPostPauta(con, "Custodia_GarantiaHis", entrada.p_FechaRespaldo, entrada.p_FechaRespaldo);
			r.guardarEnvioPostPauta(con, "Custodia_CarteraOtrosProductosHis", entrada.p_FechaRespaldo, entrada.p_FechaRespaldo);
			
			
			logger.info("Fin de la ejecucion respaldo de custodia");			
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return out;
	}
	
	/**********************
	 * metodos del programa
	 **********************/
	
	public void logEjecucion(List<LogEjecucion> logej,String mensaje,String esError,String esInterno){
		LogEjecucion aux = new LogEjecucion();
		aux.TimeMillis = System.currentTimeMillis();
		aux.Mensaje = mensaje;
		aux.EsError = esError;
		aux.EsInterno = esInterno;
		logej.add(aux);
	}

	public static void main(String[] args) {
        //prueba de la clase
		try {
			
			//Connection con = PlataformaUtils.getDESAConnection();
			Connection con = PlataformaUtils.getDESAConnection();
			Entrada entrada = new Entrada();
			entrada.p_FechaRespaldo="2023-08-22";
			Gson gson = Utils.getGsonBuilder();
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(gson.toJson(entrada)).getAsJsonObject();
	        RespaldarCustodia demo=new RespaldarCustodia();			
	        Salida resp=gson.fromJson(demo.call(con, dataJsonIn),Salida.class);
	    	System.out.println(resp);	

	    	con.commit();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}

}
