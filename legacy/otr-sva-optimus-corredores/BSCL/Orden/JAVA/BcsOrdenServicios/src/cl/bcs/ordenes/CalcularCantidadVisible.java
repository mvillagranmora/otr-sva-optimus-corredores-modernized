package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CalcularCantidadVisible implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(CalcularCantidadVisible.class);
		
	/***********************************************************************************************
	 *                                        CLASES
	 ***********************************************************************************************/
	public static class EntradaCalcularCantidadVisible{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_MontoBruto;
		public String p_MonedaInstrumento;
		public String p_Fecha;
		public Long p_VisibilidadParam;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCalcularCantidadVisible{
		public String p_out_Porcentaje;
		public ArrayList<String> p_out_arregloPorcentaje;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	/***********************************************************************************************
	 *                                        CALL
	 ***********************************************************************************************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = new Gson();	
		EntradaCalcularCantidadVisible in=gson.fromJson(jin.toString() ,EntradaCalcularCantidadVisible.class);
		SalidaCalcularCantidadVisible out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	/***********************************************************************************************
	 *                                       LOGICA
	 ***********************************************************************************************/
	@Trace
	public SalidaCalcularCantidadVisible logica(Connection con, EntradaCalcularCantidadVisible entrada) throws Exception {
		SalidaCalcularCantidadVisible salida = new SalidaCalcularCantidadVisible();
		logger.info("Inicio servicio CalcularCantidadVisible");
		BigDecimal montoPadron;
		try{
			montoPadron = ConfiguracionSistema.getParametroConfiguracionDecimal(con, entrada.p_IdEmpresa, "ORDENES", "MontoPadron");
			if(montoPadron == null || montoPadron.equals(BigDecimal.ZERO)) {
				montoPadron = BigDecimal.ONE;
			}
		}catch(Exception e){
			montoPadron = BigDecimal.ONE;
		}
		
		if (!entrada.p_MonedaInstrumento.equals("CLP")) {
			CalcularParidad paridad = new CalcularParidad();
			CalcularParidadEntrada paridadEntrada = new CalcularParidadEntrada();
			paridadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			paridadEntrada.p_IdTrabajo = 1156L;

			CalculoEntrada calculoEntrada = new CalculoEntrada();
			calculoEntrada.IdTrabajo = 1156L;
			calculoEntrada.IdInterno = 1L;
			calculoEntrada.Fecha = entrada.p_Fecha;
			calculoEntrada.IdMonedaOrigen = "CLP";
			calculoEntrada.IdMonedaDestino = entrada.p_MonedaInstrumento;
			calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntrada.MontoOrigen = montoPadron;// BigDecimal.ZERO;//en.Monto;
			calculoEntrada.IdUso = "ORDENES";
			paridadEntrada.p_CalculosEntrada.add(calculoEntrada);

			CalcularParidadSalida paridadSalida = paridad.logica(con, paridadEntrada);
			for (CalculoSalida montoParidad : paridadSalida.p_out_CalculosSalida) {
				MathContext decimales = new MathContext(8);
				montoPadron = montoParidad.MontoDestino != null ? montoParidad.MontoDestino.round(decimales) : BigDecimal.ZERO;
			}
		}
		
		BigDecimal montoBruto = new BigDecimal(entrada.p_MontoBruto.replace(".", ""));
	    BigDecimal resultadoDivision = montoPadron != BigDecimal.ZERO ? montoBruto.divide(montoPadron, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
		
		String porcentaje = "";
		ArrayList<String> arregloPorcentaje = new ArrayList<>();
		//respetamos lo que viene por parametros si no se usa resultado de division...
		if(entrada.p_VisibilidadParam != null && entrada.p_VisibilidadParam <= 5) {
			if(entrada.p_VisibilidadParam ==1 ) {
				porcentaje = "1";//100%
			}else if(entrada.p_VisibilidadParam ==2) {
				porcentaje = "2";//50%
				arregloPorcentaje.add("1");//100%
				arregloPorcentaje.add("2");//50%
			}else if(entrada.p_VisibilidadParam ==3) {
				porcentaje = "3";//25%
				arregloPorcentaje.add("1");//100%
				arregloPorcentaje.add("2");//50%
				arregloPorcentaje.add("3");//25%
			}else if(entrada.p_VisibilidadParam ==4) {
				porcentaje = "4";//20%
				arregloPorcentaje.add("1");//100%
				arregloPorcentaje.add("2");//50%
				arregloPorcentaje.add("3");//25%
				arregloPorcentaje.add("4");//20%
			}else if(entrada.p_VisibilidadParam ==5) {
				porcentaje = "5";//10%
				arregloPorcentaje.add("1");//100%
				arregloPorcentaje.add("2");//50%
				arregloPorcentaje.add("3");//25%
				arregloPorcentaje.add("4");//20%
				arregloPorcentaje.add("5");//10%
			}
			
		}else {
			if(resultadoDivision.doubleValue() >= 0 && resultadoDivision.doubleValue() < 2 ) {
				porcentaje = "1";//100%
			}else if(resultadoDivision.doubleValue() >= 2 && resultadoDivision.doubleValue() < 4) {
				porcentaje = "2";//50%
				arregloPorcentaje.add("1");//100%
				arregloPorcentaje.add("2");//50%
			}else if(resultadoDivision.doubleValue() >= 4 && resultadoDivision.doubleValue() < 5) {
				porcentaje = "3";//25%
				arregloPorcentaje.add("1");//100%
				arregloPorcentaje.add("2");//50%
				arregloPorcentaje.add("3");//25%
			}else if(resultadoDivision.doubleValue() >= 5 && resultadoDivision.doubleValue() < 10) {
				porcentaje = "4";//20%
				arregloPorcentaje.add("1");//100%
				arregloPorcentaje.add("2");//50%
				arregloPorcentaje.add("3");//25%
				arregloPorcentaje.add("4");//20%
			}else if(resultadoDivision.doubleValue() >= 10) {
				porcentaje = "5";//10%
				arregloPorcentaje.add("1");//100%
				arregloPorcentaje.add("2");//50%
				arregloPorcentaje.add("3");//25%
				arregloPorcentaje.add("4");//20%
				arregloPorcentaje.add("5");//10%
			}
				
		}
		
		salida.p_out_Porcentaje = porcentaje;
		salida.p_out_arregloPorcentaje = arregloPorcentaje;
		
		return salida;
	}
	/***********************************************************************************************
	 *                                       MAIN
	 ***********************************************************************************************/
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//String json = "{\"p_MontoBruto\":12000,\"p_MonedaInstrumento\":\"USD\",\"p_Fecha\":\"2020-01-10\",\"p_IdEmpresa\":1,\"p_IdModulo\":\"ORD\",\"p_IdUsuario\":21}";
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"ORD\",\"p_MontoBruto\":\"7998998\",\"p_MonedaInstrumento\":\"CLP\",\"p_Fecha\":\"2020-01-10\"}";
			EntradaCalcularCantidadVisible Objin = gson.fromJson(json, EntradaCalcularCantidadVisible.class);
			
			CalcularCantidadVisible demo=new CalcularCantidadVisible();		
			
 			
 			try {
 				SalidaCalcularCantidadVisible ObjOut=demo.logica(con, Objin);
 			
 				System.out.println(ObjOut);
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
	    	}	
		System.out.println("== END ==");
	}
}