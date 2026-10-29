package cl.bcs.tesoreria;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.tesoreria.LiquidarComprobante.ComprobanteLiquidar_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.LiquidarComprobante.LiquidarComprobante_Salida;
import cl.bcs.tesoreria.LiquidarComprobante.ResultadoProceso_Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;


public class LiquidarComprobanteTodos  implements JavaCall   // En SQL se llama LiquidarComprobantes
{

	// Log
	private static final Logger logger = LogManager.getLogger(LiquidarComprobante.class);
	
	// Queries	
	private final String SQL_SELECT_COMPROBANTES_A_LIQUIDAR_GRILLA 	= loadResourceToString("cl/bcs/tesoreria/sql/LiquidarComprobanteTodos_TodosComprobantesALiquidar.sql");


	List<String> listAutorizaciones = new ArrayList<String>();
	
    //-------------------------------------------------------------------------------------------
	// CLASES DE ENTRADA
	//-------------------------------------------------------------------------------------------
   
	public static class LiquidarComprobanteTodos_Entrada 
	{	
		public Long		p_IdEmpresa;  				 
		public Long		p_IdUsuario;
		public FiltrosGrilla		p_Filtros;
		public String 	p_FechaMovimiento; 
		public String 	p_IdModulo; 	 
		public String 	p_UsaReglasNegocio;	

	}	

	public static class FiltrosGrilla{
		public String	IdTipoMovimiento;  				 
		public Long		IdPersona;
		public Long		IdPortafolioTitularPago;
		public Long		IdCaja;  				 
		public String	IdMoneda;
		public String	IdEstado;
		public String	IdConcepto;
		public String	FechaLiquidacionDesde;  				 
		public String	FechaLiquidacionHasta;
		public String		IdTipoPersona;
		public String		IdMedioDePago;  				 
		public String	NombreBancoSocio;
		public String	NombreBancoCorredora;
		public String	AliasBanco = "-1";
		public String	NumeroCuentaBancariaCorredora = "-1";  				 
		public String	IdClasificacionTipoInstrumento;
		public String		IdTipoInstrumento;
		public String	FechaComprobanteOrigen ;  						 
		
	}
	

	
    //-------------------------------------------------------------------------------------------
	// CLASES DE SALIDA
	//-------------------------------------------------------------------------------------------
	
	public static class LiquidarComprobanteTodos_Salida 
	{	
		public List<MensajeSalida>				P_OUT_MENSAJES;
		public Long								P_OUT_ERRORESCONTADOR;
		public List<ResultadoProceso_Salida>	P_OUT_RESULTADOPROCESO;
		
		public LiquidarComprobanteTodos_Salida (){
			P_OUT_MENSAJES 				= new ArrayList<MensajeSalida>();
			P_OUT_RESULTADOPROCESO 		= new ArrayList<ResultadoProceso_Salida>();
		};
	    
		public String toString() {
			return Utils.printJson(this, false);
		}
	}



	
	//-------------------------------------------------------------------------------------------
	// LOGICA DEL SERVICIO
	//-------------------------------------------------------------------------------------------
	
	public LiquidarComprobanteTodos_Salida logica(Connection con, LiquidarComprobanteTodos_Entrada in) throws Exception {
				
		logger.info("== INICIO LIQUIDAR COMPROBANTE ==");
		LiquidarComprobanteTodos_Salida out = new LiquidarComprobanteTodos_Salida();
		SQL sql = new SQL();
		HashMap<String,Object> param = new HashMap<>();
		param.put("IdTipoMovimiento",in.p_Filtros.IdTipoMovimiento);  				 
		param.put("IdPersona",in.p_Filtros.IdPersona);
		param.put("IdPortafolioTitularPago",in.p_Filtros.IdPortafolioTitularPago);
		param.put("IdCaja",in.p_Filtros.IdCaja);  				 
		param.put("IdMoneda",in.p_Filtros.IdMoneda);
		param.put("IdEstado",in.p_Filtros.IdEstado);
		param.put("IdConcepto",in.p_Filtros.IdConcepto);
		param.put("FechaLiquidacionDesde",in.p_Filtros.FechaLiquidacionDesde);  				 
		param.put("FechaLiquidacionHasta",in.p_Filtros.FechaLiquidacionHasta);
		param.put("IdTipoPersona",in.p_Filtros.IdTipoPersona);
		param.put("IdMedioDePago",in.p_Filtros.IdMedioDePago);  				 
		param.put("NombreBancoSocio",in.p_Filtros.NombreBancoSocio);
		param.put("NombreBancoCorredora",in.p_Filtros.NombreBancoCorredora);
		param.put("AliasBanco",in.p_Filtros.AliasBanco);
		param.put("NumeroCuentaBancariaCorredora",in.p_Filtros.NumeroCuentaBancariaCorredora);  				 
		param.put("IdClasificacionTipoInstrumento",in.p_Filtros.IdClasificacionTipoInstrumento);
		param.put("IdTipoInstrumento",in.p_Filtros.IdTipoInstrumento);
		param.put("FechaComprobanteOrigen",in.p_Filtros.FechaComprobanteOrigen); 	
 
		List<ComprobanteLiquidar_Entrada> ComprobantesLiquidar_Entrada = sql.query(con, SQL_SELECT_COMPROBANTES_A_LIQUIDAR_GRILLA, param, ComprobanteLiquidar_Entrada.class);
		
		if(ComprobantesLiquidar_Entrada.size()==0) {
			String msg= "No Existen Comprobantes a Liquidar o Ya se encuentran Liquidados";
			out.P_OUT_ERRORESCONTADOR=0L;
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ADVERTENCIA,"SIN_COMPROBANTE_TESORERIA_A_LIQUIDAR_O_YA_LIQUIDADOS",msg));
			return out;
		};
		
		
		LiquidarComprobante liquidarComprobantes= new LiquidarComprobante();
		LiquidarComprobante_Entrada LiquidarComprobante_Entrada= new LiquidarComprobante_Entrada();
		LiquidarComprobante_Entrada.p_IdEmpresa=in.p_IdEmpresa;  				 
		LiquidarComprobante_Entrada.p_IdUsuario=in.p_IdUsuario; 				   
		LiquidarComprobante_Entrada.p_IdModulo=in.p_IdModulo; 				
		LiquidarComprobante_Entrada.p_SimularOperacion = "N"; 	 
		LiquidarComprobante_Entrada.p_UsaReglasNegocio="S";			
		LiquidarComprobante_Entrada.p_FechaMovimiento=in.p_FechaMovimiento; 	
		LiquidarComprobante_Entrada.p_ComprobanteLiquidar= ComprobantesLiquidar_Entrada;  
		LiquidarComprobante_Entrada.p_FormaDePagoLiquidar = new ArrayList<>();
		LiquidarComprobante_Salida LiquidarComprobante_Salida = liquidarComprobantes.logica(con, LiquidarComprobante_Entrada);
		
		if ( LiquidarComprobante_Salida.P_OUT_ERRORESCONTADOR != null && LiquidarComprobante_Salida.P_OUT_ERRORESCONTADOR>0) {
			con.rollback();
		}
		if (LiquidarComprobante_Salida.P_OUT_ERRORESCONTADOR == null) {
			con.rollback();
		}
		out.P_OUT_ERRORESCONTADOR= LiquidarComprobante_Salida.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = LiquidarComprobante_Salida.P_OUT_MENSAJES;
		out.P_OUT_RESULTADOPROCESO = LiquidarComprobante_Salida.P_OUT_RESULTADOPROCESO;
		
		return out;
		


	}
	
	
	//-------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE
	//-------------------------------------------------------------------------------------------	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		
		LiquidarComprobanteTodos_Entrada in = gson.fromJson(jin.toString(), LiquidarComprobanteTodos_Entrada.class);
		LiquidarComprobanteTodos_Salida out = logica(con, in);
		//LiquidarComprobanteTodos_Salida out = new LiquidarComprobanteTodos_Salida();
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		//out.P_OUT_MENSAJES=(List<MensajeSalida>) in;

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	//-------------------------------------------------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------------------------------------------------------
	
	public static void main(String[] args) throws Exception 
	{
		System.out.println("== INICIO PRUEBA LOCAL ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties")) 
		{
			Gson gson = Utils.getGsonBuilder();


			String json= "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"TES\",\"p_UsaReglasNegocio\":\"S\",\"p_FechaMovimiento\":\"2025-11-27\",\"p_Filtros\":{\"IdTipoMovimiento\":\"MOVING\",\"IdPersona\":-1,\"IdPortafolioTitularPago\":-1,\"IdCaja\":-1,\"IdMoneda\":-1,\"IdEstado\":\"ING\",\"IdConcepto\":-1,\"FechaLiquidacionDesde\":\"2025-11-28T06:00:00.000Z\",\"FechaLiquidacionHasta\":\"2025-11-29T02:59:59.000Z\",\"IdTipoPersona\":-1,\"IdMedioDePago\":-1,\"NombreBancoSocio\":-1,\"NombreBancoCorredora\":-1,\"IdClasificacionTipoInstrumento\":-1,\"IdTipoInstrumento\":-1},\"p_FormaDePagoLiquidar\":[]}";
			
			LiquidarComprobanteTodos_Entrada Objin = gson.fromJson(json, LiquidarComprobanteTodos_Entrada.class);
			LiquidarComprobanteTodos demo = new LiquidarComprobanteTodos();
			
 			System.out.println("ENTRADA:");
 			System.out.println(Objin.toString());
 			
 			try 
 			{
 				LiquidarComprobanteTodos_Salida ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				con.rollback();
 				//con.commit();
 			}
 			catch(Exception er) 
 			{
				er.printStackTrace();	
				//con.commit();
			}
 			
 			
 			con.close();
 			
	    	}
		 	
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
	
	
}
