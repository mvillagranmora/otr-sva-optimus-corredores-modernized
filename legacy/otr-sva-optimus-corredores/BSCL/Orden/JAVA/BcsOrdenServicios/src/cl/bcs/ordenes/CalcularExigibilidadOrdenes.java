package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.gta.CalcularExigibilidad;
import cl.bcs.plataforma.gta.CalcularExigibilidad.CalcularExigibilidadEntrada;
import cl.bcs.plataforma.gta.CalcularExigibilidad.CalcularExigibilidadSalida;
import cl.bcs.plataforma.gta.CalcularExigibilidad.ObjExigibilidadEntrada;
import cl.bcs.plataforma.gta.CalcularExigibilidad.ObjExigibilidadPortafolio;
import cl.bcs.plataforma.gta.CalcularExigibilidad.ObjExigibilidadSimulacion;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CalcularExigibilidadOrdenes implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(CalcularExigibilidadOrdenes.class);
	
	private final String SQL_SELECT_CONSULTAR_TIPO_PRESTAMO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarTipoPrestamo_Select.sql");

	/***********************************************************************************************
	 *                                        CLASES
	 ***********************************************************************************************/
	
	public static class EntradaExigibilidadOrdenes{
		 public Long p_IdEmpresa;
		 public Long p_IdUsuario;	                       
		 public String p_IdModulo;                          
		 public String p_simularOperacion;					
		 public String p_UsaReglasNegocio;
		 
		 public List<ObjExigibilidadEntrada> p_TableExigibilidadSimulacion;
		 						
		 public String p_EsVentaCorta;						
		 public String p_IdTipoInstrumento; 					
		 public Long p_IdPortafolio; 						
		 public String p_IdBolsa; 							
		 public Long p_IdInstrumento;
		 
		 public String p_IdMoneda = "";
		 
		 public EntradaExigibilidadOrdenes () {
			this.p_TableExigibilidadSimulacion= new ArrayList<>(); 
		 }
		 
		 public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaExigibilidadOrdenes{
		
		public List<ObjExigibilidadSimulacion> p_out_TableExigibilidadSimulacion;
		public List<ObjExigibilidadPortafolio> p_out_TableExigibilidadPortafolio;
		public List<MensajeSalida> p_out_mensajes;
		public Long p_out_ErroresContador;
		
		
		public SalidaExigibilidadOrdenes() {
			this.p_out_TableExigibilidadSimulacion = new ArrayList<>();
			this.p_out_TableExigibilidadPortafolio = new ArrayList<>();
			this.p_out_mensajes = new ArrayList<>();
			this.p_out_ErroresContador=0L;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	public static class Instrumento{
		public Long IdInstrumentoPrestamo;
		
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
		EntradaExigibilidadOrdenes in=gson.fromJson(jin.toString() ,EntradaExigibilidadOrdenes.class);
		SalidaExigibilidadOrdenes out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}	
	
	/***********************************************************************************************
	 *                                       LOGICA
	 ***********************************************************************************************/
	@Trace
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SalidaExigibilidadOrdenes logica(Connection con, EntradaExigibilidadOrdenes entrada) throws Exception {
		Long init = System.currentTimeMillis();
		SalidaExigibilidadOrdenes Salida = new SalidaExigibilidadOrdenes();
		SQL sql=new SQL();		
		logger.info("Inicio servicio CalcularExigibilidadOrdenes");
		
		try {			
			String v_IdTipoInstrumento;
			Long v_IdInstrumento ;
			
			if(entrada.p_EsVentaCorta.equals("S")){
				v_IdTipoInstrumento = "PRT";
				List<Instrumento> RespTipoPrestamo = sql.query(con,SQL_SELECT_CONSULTAR_TIPO_PRESTAMO, new HashMap<>() ,Instrumento.class);
				v_IdInstrumento = RespTipoPrestamo.get(0).IdInstrumentoPrestamo;			
			}else{
				v_IdTipoInstrumento = entrada.p_IdTipoInstrumento;
				v_IdInstrumento =entrada.p_IdInstrumento;
			}			
			//String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			
			
			if (entrada.p_IdMoneda.equals("")){
				entrada.p_IdMoneda = idMonedaSistema;
			}
			
			
			CalcularExigibilidad calculoExigibilidad = new CalcularExigibilidad();
			CalcularExigibilidadEntrada inCalculoExigibilidad = new CalcularExigibilidadEntrada();
			
			inCalculoExigibilidad.p_IdEmpresa= entrada.p_IdEmpresa;
			inCalculoExigibilidad.p_IdModulo = entrada.p_IdModulo;
			inCalculoExigibilidad.p_IdUsuario= entrada.p_IdUsuario;
			inCalculoExigibilidad.p_UsaReglasNegocio =entrada.p_UsaReglasNegocio;
			inCalculoExigibilidad.p_simularOperacion =entrada.p_simularOperacion;
			inCalculoExigibilidad.p_IdPortafolio	 = entrada.p_IdPortafolio;
			inCalculoExigibilidad.p_IdInstrumento	 = entrada.p_IdInstrumento;
			inCalculoExigibilidad.p_IdTipoInstrumento= entrada.p_IdTipoInstrumento;
			inCalculoExigibilidad.p_IdBolsa			 = entrada.p_IdBolsa;
			inCalculoExigibilidad.p_IdMoneda		 = entrada.p_IdMoneda;
			inCalculoExigibilidad.p_TableExigibilidadSimulacion=entrada.p_TableExigibilidadSimulacion;
			
			CalcularExigibilidadSalida outCalculoExigibilidad = calculoExigibilidad.logica(con, inCalculoExigibilidad);
			
			Salida.p_out_ErroresContador= outCalculoExigibilidad.P_OUT_ERRORESCONTADOR;
			Salida.p_out_mensajes = outCalculoExigibilidad.P_OUT_MENSAJES;
			Salida.p_out_TableExigibilidadPortafolio = outCalculoExigibilidad.P_OUT_TABLEEXIGIBILIDADPORTAFOLIO;
			Salida.p_out_TableExigibilidadSimulacion = outCalculoExigibilidad.P_OUT_TABLEEXIGIBILIDADSIMULACION;
			
		} catch(Exception e){
			logger.error("Error::",e);
			throw e;
		}
		logger.info("Termino del servicio CalcularExigibilidadOrdenes, tiempo:" + (System.currentTimeMillis() - init));
		
		return Salida;
	}
	
	/***********************************************************************************************
	 *                                       MAIN
	 ***********************************************************************************************/
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"ORD\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_TableExigibilidadSimulacion\":[{\"IdInterno\":0,\"IdInstrumento\":502,\"Cantidad\":0,\"Precio\":0,\"Monto\":49418.119999999995,\"IdBolsa\":\"BCS\"}],\"p_EsVentaCorta\":\"S\",\"p_IdTipoInstrumento\":\"ACN\",\"p_IdPortafolio\":1451920,\"p_IdBolsa\":\"BCS\",\"p_IdInstrumento\":502,\"p_IdMoneda\":\"CLP\"}";
			EntradaExigibilidadOrdenes Objin = gson.fromJson(json, EntradaExigibilidadOrdenes.class);
			
			CalcularExigibilidadOrdenes demo=new CalcularExigibilidadOrdenes();				
 			
 			try {
 				SalidaExigibilidadOrdenes ObjOut=demo.logica(con, Objin);
 			
 				System.out.println(ObjOut);
 			}
 			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
 			//con.commit();
 			
	    	}
		 	
		System.out.println("== END ==");
	}
	

}
