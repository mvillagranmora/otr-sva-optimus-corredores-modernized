package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;



public class CalcularCoberturaOrdenes implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(CalcularCoberturaOrdenes.class);
	
	private final String SQL_SELECT_CONSULTAR_TIPO_PRESTAMO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarTipoPrestamo_Select.sql");
	private final String SQL_SELECT_CONSULTAR_BOLSA_TIPO_INSTRUMENTO = loadResourceToString("cl/bcs/ordenes/sql/ConsultarBolsaTipoInstrumento_Select.sql");
	private final String SQL_CALL_CALCULAR_COBERTURAS= loadResourceToString("cl/bcs/ordenes/sql/CalcularCoberturas_Call.sql");
	private final String SQL_COBERTURA_ORDENES_TABLA= loadResourceToString("cl/bcs/ordenes/sql/CoberturasOrdenes_TablaTemporal.sql");
	private final String SQL_INSERT_COBERTURA= loadResourceToString("cl/bcs/ordenes/sql/Cobertura_Insert.sql");
	
	
	/***********************************************************************************************
	 *                                        CLASES
	 ***********************************************************************************************/
	public static class EntradaCoberturasOrdenes{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		                                   
		public Long p_IdPortafolio;
		public String p_IdBolsa;
		public Long p_IdInstrumento;
		public String p_IdTipoInstrumento;
		public String p_EsVentaCorta;
		public String p_IdMoneda = "";
		public String p_IdMonedaOrigen = "";
		public List<InCoberturaSimulacion> p_Simulacion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCoberturasOrdenes{
		public List<OutCoberturaSimulacion>p_out_TableCoberturaSimulacion;
		public List<OutCoberturaPortafolio>p_out_TableCoberturaPortafolio;
		
		public List<OutAdvertenciasDetalle> p_out_AdvertenciasDetalle;
		public List<OutErroresDetalle> p_out_ErroresDetalle;
		
		public Long p_out_AdvertenciasContador;
		public Long p_out_ErroresContador;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class InCoberturaSimulacion{
		public Long	IdReferencial;
		public Long	IdInstrumento;
		public BigDecimal	Cantidad;
		public BigDecimal	PrecioMercado;
		public BigDecimal	Monto;
		public BigDecimal 	MontoCoberturaSimular;
		
		//public String MonedaOrigen = "";// si no viene, se va a buscar al instrumento.
		public String MonedaDestino = "";// si no viene, usa la del instrumento.
		public InCoberturaSimulacion(){
			this.MontoCoberturaSimular =BigDecimal.ZERO;;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class InCobertura{
		public Long IdReferencial;
		public Long IdInstrumento;
		public BigDecimal Cantidad;
		public BigDecimal PrecioMercado;
		public BigDecimal Monto;
		public BigDecimal MontoCoberturaSimular;
		
		public InCobertura(){
			this.MontoCoberturaSimular=BigDecimal.ZERO;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class OutCoberturaSimulacion{
		public Long	IdReferencial; 					
		public Long	IdInstrumento; 					
		public BigDecimal	Cantidad;  						
		public BigDecimal	PrecioMercado;						
		public BigDecimal	Monto;								
		public BigDecimal	PorcentajeCoberturaEntidad; 		
		public BigDecimal	MontoCoberturaEntidad; 			
		public BigDecimal	PorcentajeCoberturaCorredora; 		
		public BigDecimal	MontoCoberturaCorredora;

		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class OutCoberturaPortafolio{
		public BigDecimal MontoCoberturaEntidad;
		public BigDecimal MontoCoberturaCorredora;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	public static class OutErroresDetalle{
		public String Codigo;
		public String Descripcion;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class OutAdvertenciasDetalle{
		public String Codigo;
		public String RequiereAutorizacion;
		public String Descripcion;
		
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
	public static class EntidadDestinoGarantia{
		public Long IdEntidadDestinoGarantia;
		public Long CountEntidadDestinoGarantia;
		
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
		EntradaCoberturasOrdenes in=gson.fromJson(jin.toString() ,EntradaCoberturasOrdenes.class);
		SalidaCoberturasOrdenes out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		
		return dataout;
	}
	/***********************************************************************************************
	 *                                       LOGICA
	 ***********************************************************************************************/
	@Trace
	@SuppressWarnings({ "unchecked", "unlikely-arg-type", "rawtypes" })
	public SalidaCoberturasOrdenes logica(Connection con, EntradaCoberturasOrdenes entrada) throws Exception {
		Long init = System.currentTimeMillis();
		SalidaCoberturasOrdenes Salida = new SalidaCoberturasOrdenes();
		SQL sql=new SQL();
		logger.info("Inicio servicio CalcularCoberturaOrdenes");
		
		String v_IdTipoInstrumentoExigeGarantia;
		Long v_IdInstrumento;
		
		Long v_countEntidadDestinoGarantia;
		Long v_IdEntidadDestinoGarantia;
		
		try{
			if(entrada.p_EsVentaCorta.equals("S")){
				v_IdTipoInstrumentoExigeGarantia = "PRT";
				List<Instrumento> RespTipoPrestamo = sql.query(con,SQL_SELECT_CONSULTAR_TIPO_PRESTAMO, new HashMap<>() ,Instrumento.class);
				v_IdInstrumento = RespTipoPrestamo.get(0).IdInstrumentoPrestamo;
			}else{
				v_IdTipoInstrumentoExigeGarantia = entrada.p_IdTipoInstrumento;
				v_IdInstrumento =entrada.p_IdInstrumento;
			}
			
			//Hash llama tabla Bolsa Tipo Instrumento
			HashMap<String,Object> paramsObj = new HashMap<>();
			paramsObj.put("IdTipoInstrumento", v_IdTipoInstrumentoExigeGarantia);
			paramsObj.put("IdBolsa", (entrada.p_IdBolsa.equals("0") ? "BCS" : entrada.p_IdBolsa));
			
			List<EntidadDestinoGarantia> contBTI = sql.query(con, SQL_SELECT_CONSULTAR_BOLSA_TIPO_INSTRUMENTO, paramsObj, EntidadDestinoGarantia.class);		
			v_countEntidadDestinoGarantia = (long) contBTI.size();
			v_IdEntidadDestinoGarantia = contBTI.get(0).IdEntidadDestinoGarantia;
			
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
			
			if (entrada.p_IdMoneda.equals("")){
				entrada.p_IdMoneda = idMonedaSistema;
			}
			if (entrada.p_IdMonedaOrigen.equals("")) {
				entrada.p_IdMonedaOrigen = idMonedaSistema;
			}
			
			//objeto para tabla entrada SP 
			//List<InTableExigibilidadSimulacionSP> tabIn = new ArrayList<InTableExigibilidadSimulacionSP>();
			List<InCobertura> inSimCob = new ArrayList<>();
			for(InCoberturaSimulacion cob: entrada.p_Simulacion){
				InCobertura in = new InCobertura();
				//Utils.copyPojo(cob, in);
				in.IdReferencial = cob.IdReferencial;
				in.IdInstrumento = cob.IdInstrumento;
				in.Cantidad = cob.Cantidad;
				in.PrecioMercado = cob.PrecioMercado;
				in.Monto = cob.Monto;
				in.MontoCoberturaSimular = cob.MontoCoberturaSimular;
				
				//in.Monto =((CalculoSalida)paridadOutHash.get(cob.IdReferencial)).MontoDestino;//cob.Monto.multiply(((CalculoSalida)paridadOutHash.get(cob.IdReferencial)).FactorOrigenDestino);
				inSimCob.add(in);
			}
			
			//insert tabla temporal
			SQL.exec(con,SQL_COBERTURA_ORDENES_TABLA);
			sql.batch(con,SQL_INSERT_COBERTURA,inSimCob, InCobertura.class);
			
			//Call Calcula Cobertura
			if(v_countEntidadDestinoGarantia > 0){			
				
				//Hash parametros de entrada Call 
				HashMap<String,Object>  paramsIn = new HashMap<String,Object>();
				paramsIn.put("p_IdEmpresa",entrada.p_IdEmpresa);
				paramsIn.put("p_IdUsuario",entrada.p_IdUsuario);
				paramsIn.put("p_IdModulo",entrada.p_IdModulo);
				paramsIn.put("p_UsaReglasNegocio",entrada.p_UsaReglasNegocio);
				paramsIn.put("p_IdPortafolio",entrada.p_IdPortafolio);
				paramsIn.put("p_IdEntidadDestinoGarantia",v_IdEntidadDestinoGarantia);
				paramsIn.put("p_IdTipoInstrumentoExigeGarantia",v_IdTipoInstrumentoExigeGarantia);
				paramsIn.put("p_IdInstrumentoExigeGarantia",v_IdInstrumento);
				//paramsIn.put("p_Simulacion",entrada.p_Simulacion);
				
				//Hash parametros de Salida Call 
				HashMap<String,Class>  paramsOut = new HashMap<String,Class>();
				paramsOut.put("p_out_TableCoberturaSimulacion",	OutCoberturaSimulacion.class);
				paramsOut.put("p_out_TableCoberturaPortafolio",	OutCoberturaPortafolio.class);
				paramsOut.put("p_out_AdvertenciasContador",Long.class);
				paramsOut.put("p_out_AdvertenciasDetalle",OutAdvertenciasDetalle.class);
				paramsOut.put("p_out_ErroresContador",Long.class);
				paramsOut.put("p_out_ErroresDetalle",OutErroresDetalle.class);
				
				//hash Call 
				HashMap<String,Object> respuesta=sql.call(con, SQL_CALL_CALCULAR_COBERTURAS, paramsIn, paramsOut);
				
				Salida.p_out_TableCoberturaSimulacion = (List<OutCoberturaSimulacion>) respuesta.get(("p_out_TableCoberturaSimulacion"));
				Salida.p_out_TableCoberturaPortafolio = (List<OutCoberturaPortafolio>) respuesta.get(("p_out_TableCoberturaPortafolio"));
				
				// calculo paridad para cobertura en otra moneda de liquidacion ditinta a la de sistema
				CalcularParidad paridadPor = new CalcularParidad();
				CalcularParidadEntrada parInPor = new CalcularParidadEntrada();
				parInPor.p_IdEmpresa = entrada.p_IdEmpresa;
				parInPor.p_IdTrabajo = 1156L;
				for (OutCoberturaPortafolio en: Salida.p_out_TableCoberturaPortafolio){
					CalculoEntrada in =  new CalculoEntrada();
					in.IdTrabajo = 1156L;
					in.IdInterno = 1L;
					in.Fecha	= fechaSistema;
					in.IdMonedaOrigen = entrada.p_IdMonedaOrigen;
					in.IdMonedaDestino = entrada.p_IdMoneda;
					in.ValorCambioLocalModificado = BigDecimal.ZERO;
					in.MontoOrigen = en.MontoCoberturaCorredora;//BigDecimal.ZERO;//en.Monto;
					in.IdUso = "ORDENES";
					parInPor.p_CalculosEntrada.add(in);
					in =  new CalculoEntrada();
					in.IdTrabajo = 1156L;
					in.IdInterno = 2L;
					in.Fecha	= fechaSistema;
					in.IdMonedaOrigen = entrada.p_IdMonedaOrigen;
					in.IdMonedaDestino = entrada.p_IdMoneda;
					in.ValorCambioLocalModificado = BigDecimal.ZERO;
					in.MontoOrigen = en.MontoCoberturaEntidad;//BigDecimal.ZERO;//en.Monto;
					in.IdUso = "ORDENES";
					parInPor.p_CalculosEntrada.add(in);
				}
								
				CalcularParidadSalida paridadPorOut = paridadPor.logica(con, parInPor);
				HashMap<String, Object> paridadPorOutHash = Utils.List2Hash(paridadPorOut.p_out_CalculosSalida, "IdInterno");
				for(OutCoberturaPortafolio en: Salida.p_out_TableCoberturaPortafolio){
					en.MontoCoberturaCorredora 	=((CalculoSalida)paridadPorOutHash.get(1L)).MontoDestino;
					en.MontoCoberturaEntidad 	=((CalculoSalida)paridadPorOutHash.get(2L)).MontoDestino; 
				}
				
				Salida.p_out_AdvertenciasDetalle = (List<OutAdvertenciasDetalle>) respuesta.get(("p_out_AdvertenciasDetalle"));
				Salida.p_out_ErroresDetalle = (List<OutErroresDetalle>) respuesta.get(("p_out_ErroresDetalle"));
				
				Salida.p_out_AdvertenciasContador=(long) Salida.p_out_AdvertenciasDetalle.size();
				Salida.p_out_ErroresContador=(long) Salida.p_out_ErroresDetalle.size();			
				
			}
		}catch(Exception e){
			logger.error("Error::",e);
			throw e;
		}finally{
			try {
				SQL.exec(con, "DROP TABLE #p_SimulacionCobertura");
			} catch (Exception e) {
				logger.error("Error al dropear tabla::",e);
			}
		}
		logger.info("Termino del servicio CalcularCoberturaOrdenes, tiempo:" + (System.currentTimeMillis() - init));

		return Salida;
	}
	/***********************************************************************************************
	 *                                       MAIN
	 ***********************************************************************************************/
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAPEConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_EsVentaCorta\":\"N\",\"p_UsaReglasNegocio\":\"S\",\"p_IdBolsa\":\"BVL\",\"p_IdEmpresa\":1,\"p_IdInstrumento\":182549,\"p_IdModulo\":\"ORD\",\"p_IdPortafolio\":162283,\"p_IdTipoInstrumento\":\"RRV\",\"p_IdUsuario\":21,\"p_IdMoneda\":\"PEN\",\"p_Simulacion\":[{\"IdPortafolio\":162283,\"IdBolsa\":\"BVL\",\"IdInstrumentoExige\":182549,\"EsVentaCorta\":false,\"IdTipoInstrumentoExige\":\"RRV\",\"IdInstrumento\":20510,\"Cantidad\":0,\"Precio\":0,\"Monto\":150,\"MontoSimular\":null,\"MonedaDestino\":\"PEN\"}]}";
			
			EntradaCoberturasOrdenes Objin = gson.fromJson(json, EntradaCoberturasOrdenes.class);
			
			CalcularCoberturaOrdenes demo=new CalcularCoberturaOrdenes();		
			
 			
 			try {
 				SalidaCoberturasOrdenes ObjOut=demo.logica(con, Objin);
 			
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
