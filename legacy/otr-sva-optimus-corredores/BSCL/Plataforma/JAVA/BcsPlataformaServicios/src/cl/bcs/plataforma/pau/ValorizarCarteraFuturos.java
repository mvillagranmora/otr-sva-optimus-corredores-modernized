package cl.bcs.plataforma.pau;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.custodia.utils.ValorizarCustodia;
import cl.bcs.plataforma.pau.ValorizarCarteraRV.ObjCarteras;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValorizarCarteraFuturos implements JavaCall {
	static final Logger logger = LogManager.getLogger(ValorizarCarteraFuturos.class);
	private final String SQL_SelectCarteraFuturoPQ = loadResourceToString( "cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_CarteraFuturosPQ.sql");
	private final String SQL_SelectCarteraFuturoWS = loadResourceToString( "cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_CarteraFuturosWS.sql");
	
	public static class ValorizarCarteraFuturosEntrada {
		public Long 			p_IdEmpresa;
		public Long             p_IdUsuario;
		public String           p_FechaSistema;
		public Long             p_IdPais;
		public String           p_SimularOperacion;
		public Long				p_IdPortafolio;
		public Long				p_IdCliente;
		
		public ValorizarCarteraFuturosEntrada () {
			this.p_IdPortafolio = 0L;
			this.p_IdCliente =0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ObjCarterasWS {		
		public Long					IdProcesamientoValorizaCustodiaMasiva; 
		public Long					IdTablaOrigen; 
		public String				TablaOrigen; 
		public String				EstadoProcesamiento; 
		public java.sql.Date		WS_FechaOperacion; 
		public String				WS_ValorizaACompra; 
		public String				WS_ValorizaPor; 
		public String				WS_IdClasificacionTipoInstrumento; 
		public String				WS_IdTipoInstrumento; 
		public Long					WS_IdInstrumento; 
		public String				WS_Nemotecnico; 
		public String				WS_IdEmisor; 
		public java.sql.Date		WS_FechaEmision; 
		public java.sql.Date		WS_FechaVencimiento; 
		public String				WS_IdTipoReajuste; 
		public BigDecimal			WS_Cantidad; 
		public BigDecimal			WS_ValorRescate; 
		public BigDecimal			WS_PrecioCompra; 
		public BigDecimal			WS_TasaCompra; 
		public BigDecimal			WS_PrecioSubyacente; 
		public BigDecimal			WS_TasaSubyacente; 
		public BigDecimal			WS_PrecioGarantia; 
		public BigDecimal			WS_TasaGarantia; 
		public BigDecimal           WS_TasaTIF; 
		public String				WS_UsoPrecio; 
		public String				WS_TipoCartera; 
		public String				WS_IdTipoCriterioTasa; 
		public String				WS_TipoValorizacion; 
		public String				WS_TieneCorteDeCupon; 
		public Long					WS_IdCliente; 
		public Long					WS_IdPortafolio; 
		public BigDecimal			WS_BK_INGMontoCompraActualMonedaInstrumento; 
		public BigDecimal			WS_BK_INGMontoATasaCompraActual; 
		public BigDecimal			WS_BK_INGMontoMercadoActualMonedaInstrumento; 
		public BigDecimal			WS_BK_INGMontoATasaMercadoActual;
		public String 				WS_IdMonedaInstrumento;
		public String 				WS_IdMonedaEmision;
		public String				WS_IdTipoOperacion;
		public BigDecimal			WS_MontoCompra;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ObjCarterasPQ {		
		public Long					IdProcesamientoValorizaCustodiaMasiva; 
		public Long					IdTablaOrigen; 
		public String				TablaOrigen; 
		public String				EstadoProcesamiento; 
		public java.sql.Date		PQ_FechaOperacion;
		public String				PQ_ValorizaACompra; 
		public String				PQ_IdClasificacionTipoInstrumento; 
		public String				PQ_IdTipoInstrumento; 
		public Long					PQ_IdInstrumento; 
		public String				PQ_Nemotecnico; 
		public String				PQ_IdMoneda; 
		public BigDecimal  			PQ_Cantidad; 
		public BigDecimal			PQ_Precio; 
		public Long					PQ_Tamano; 
		public String				PQ_UsoPrecio; 
		public String				PQ_TipoCartera; 
		public String				PQ_IdTipoCriterioTasa; 
		public java.sql.Date		PQ_FechaVencimientoOperacion; 
		public BigDecimal           PQ_Tasa; 
		public Long					PQ_Plazo; 
		public Long 				PQ_PlazoResidual; 
		public Long					PQ_Base; 
		public Long					PQ_IdCliente; 
		public Long					PQ_IdPortafolio; 
		public java.sql.Date		WS_FechaOperacion; 
		public String				WS_ValorizaACompra; 
		public String				WS_ValorizaPor; 
		public String				WS_IdClasificacionTipoInstrumento; 
		public String				WS_IdTipoInstrumento; 
		public Long					WS_IdInstrumento; 
		public String				WS_Nemotecnico; 
		public String				WS_IdEmisor; 
		public java.sql.Date		WS_FechaEmision; 
		public java.sql.Date		WS_FechaVencimiento; 
		public String				WS_IdTipoReajuste; 
		public BigDecimal			WS_Cantidad; 
		public BigDecimal			WS_ValorRescate; 
		public BigDecimal			WS_PrecioCompra; 
		public BigDecimal			WS_TasaCompra; 
		public BigDecimal			WS_PrecioSubyacente; 
		public BigDecimal			WS_TasaSubyacente; 
		public BigDecimal			WS_PrecioGarantia; 
		public BigDecimal			WS_TasaGarantia; 
		public BigDecimal           WS_TasaTIF; 
		public String				WS_UsoPrecio; 
		public String				WS_TipoCartera; 
		public String				WS_IdTipoCriterioTasa; 
		public String				WS_TipoValorizacion; 
		public String				WS_TieneCorteDeCupon; 
		public Long					WS_IdCliente; 
		public Long					WS_IdPortafolio; 
		public BigDecimal			WS_BK_INGMontoCompraActualMonedaInstrumento; 
		public BigDecimal			WS_BK_INGMontoATasaCompraActual; 
		public BigDecimal			WS_BK_INGMontoMercadoActualMonedaInstrumento; 
		public BigDecimal			WS_BK_INGMontoATasaMercadoActual; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ValorizarCarteraFuturosSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE; 
		
		public ValorizarCarteraFuturosSalida(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJE = "";
		}
	}	

	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		ValorizarCarteraFuturosEntrada in=gson.fromJson(jin.toString() ,ValorizarCarteraFuturosEntrada.class);
		ValorizarCarteraFuturosSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public ValorizarCarteraFuturosSalida logica (Connection con ,ValorizarCarteraFuturosEntrada entrada) throws Exception {
		logger.info("INICIO::ValorizarCarteraFuturos.logica");
		ValorizarCarteraFuturosSalida out = new ValorizarCarteraFuturosSalida();
		ValorizarCustodia ValorizacionCustodia = new ValorizarCustodia();
		
		HashMap<String,Object> paramsCartera = new HashMap<>();
		paramsCartera.put("IdEmpresa",entrada.p_IdEmpresa);
		paramsCartera.put("IdPortafolio", entrada.p_IdPortafolio);
		paramsCartera.put("IdCliente", entrada.p_IdCliente);
		
		List<ObjCarterasPQ> listaPQ = obtenerCarterasPQ(con,paramsCartera );
		List<ObjCarterasWS> listaWS = obtenerCarterasWS(con,paramsCartera);
		
		ValorizarCustodia.ValorizarCustodiaEntrada ValorizarCust = new ValorizarCustodia.ValorizarCustodiaEntrada();
		ValorizarCust.p_IdEmpresa 					= entrada.p_IdEmpresa;
		ValorizarCust.p_IdUsuario					= entrada.p_IdUsuario;
		ValorizarCust.p_IdModulo 					= "PLA";			
		ValorizarCust.p_IdPais 						= entrada.p_IdPais;
		ValorizarCust.p_OrigenProcesamiento 		= "VALORIZACION_CARTERAFU";
		
		/*if (listaWS.isEmpty()) {
			ValorizarCust.p_InstrumentosAValorizar.clear();
			ValorizarCust.p_InstrumentosAValorizar = entradaCarterasWS(listaWS);
			
			ValorizarCustodia.ValorizarCustodiaSalida salidaWS = ValorizacionCustodia.logica(con, ValorizarCust);
			out.P_OUT_MENSAJE += PlataformaUtils.concatenarMensajesSalida(salidaWS.P_OUT_MENSAJES);
		}*/
		if(!listaPQ.isEmpty()) {
			ValorizarCust.p_InstrumentosAValorizar.clear();
			ValorizarCust.p_InstrumentosAValorizar = entradaCarterasPQ(listaPQ);
			
			ValorizarCustodia.ValorizarCustodiaSalida salidaPQ = ValorizacionCustodia.logica(con, ValorizarCust);
			out.P_OUT_MENSAJE += PlataformaUtils.concatenarMensajesSalida(salidaPQ.P_OUT_MENSAJES);
		}
		
		logger.info("FIN::ValorizarCarteraFuturos.logica");
		return out;
	}
	private List<ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada> entradaCarterasWS(List<ObjCarterasWS> listaWS){
		
		List<ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada> listaSalida =listaWS.parallelStream().map(cartera->{
			ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarCartera = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
						
			ValorizarCartera.FechaOperacion 				= cartera.WS_FechaOperacion;
			ValorizarCartera.IdClasificacionTipoInstrumento = cartera.WS_IdClasificacionTipoInstrumento;
			ValorizarCartera.IdTipoInstrumento 				= cartera.WS_IdTipoInstrumento;
			ValorizarCartera.FechaVencimientoOperacion 		= cartera.WS_FechaVencimiento;
			ValorizarCartera.FechaVencimientoInstrumento 	= cartera.WS_FechaVencimiento;
			ValorizarCartera.IdInstrumento 					= cartera.WS_IdInstrumento;
			ValorizarCartera.Nemotecnico 					= cartera.WS_Nemotecnico;
			ValorizarCartera.IdMonedaInstrumento 			= cartera.WS_IdMonedaInstrumento;
			ValorizarCartera.EsRegistroNuevo 				= "N";
			ValorizarCartera.IdMonedaEmisionInstrumento		= cartera.WS_IdMonedaEmision;
			ValorizarCartera.Nemotecnico 					= cartera.WS_Nemotecnico;
			ValorizarCartera.IdTipoReajusteInstrumento 		= cartera.WS_IdTipoReajuste;
			ValorizarCartera.Cantidad 						= cartera.WS_Cantidad;
			ValorizarCartera.ValorRescate 					= cartera.WS_ValorRescate;
			ValorizarCartera.PrecioCompra 					= cartera.WS_PrecioCompra;
			ValorizarCartera.TasaCompra 					= cartera.WS_TasaCompra;
			ValorizarCartera.PrecioSubyacente				= cartera.WS_PrecioSubyacente;
			ValorizarCartera.TasaSubyacente 				= cartera.WS_TasaSubyacente;
			ValorizarCartera.PrecioGarantia 				= cartera.WS_PrecioGarantia;
			ValorizarCartera.TasaGarantia 					= cartera.WS_TasaGarantia;
			ValorizarCartera.TieneCorteDeCupon 				= cartera.WS_TieneCorteDeCupon;
			ValorizarCartera.IdCliente 						= cartera.WS_IdCliente;
			ValorizarCartera.IdPortafolio 					= cartera.WS_IdPortafolio;
			ValorizarCartera.TablaOrigen 					= cartera.TablaOrigen;		
			ValorizarCartera.IdCustodia 					= cartera.IdTablaOrigen;
			ValorizarCartera.IdTipoOperacion 				= cartera.WS_IdTipoOperacion;
			ValorizarCartera.MontoCompra 					= cartera.WS_MontoCompra;
			
			return ValorizarCartera;
			
		}).collect(Collectors.toList());
		return listaSalida;
	}
	
	private List<ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada> entradaCarterasPQ(List<ObjCarterasPQ> listaPQ){
		
		List<ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada> listaSalida =listaPQ.parallelStream().map(cartera->{
			ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarCartera = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
			
			ValorizarCartera.FechaOperacion 				= cartera.PQ_FechaOperacion;
			ValorizarCartera.IdClasificacionTipoInstrumento = cartera.PQ_IdClasificacionTipoInstrumento;
			ValorizarCartera.IdTipoInstrumento 				= cartera.PQ_IdTipoInstrumento;
			ValorizarCartera.IdInstrumento 					= cartera.PQ_IdInstrumento;
			ValorizarCartera.Nemotecnico 					= cartera.PQ_Nemotecnico;
			ValorizarCartera.Cantidad 						= cartera.PQ_Cantidad;
			ValorizarCartera.ValorRescate 					= BigDecimal.ZERO;
			ValorizarCartera.PrecioCompra 					= cartera.PQ_Precio;
			ValorizarCartera.TasaCompra 					= BigDecimal.ZERO;
			ValorizarCartera.IdCliente 						= cartera.PQ_IdCliente;
			ValorizarCartera.IdPortafolio 					= cartera.PQ_IdPortafolio;
			ValorizarCartera.TablaOrigen 					= cartera.TablaOrigen;		
			ValorizarCartera.IdCustodia 					= cartera.IdTablaOrigen;
			ValorizarCartera.EsRegistroNuevo 				= "S";
			ValorizarCartera.IdMonedaInstrumento 			= cartera.PQ_IdMoneda;
			
			return ValorizarCartera;
			
		}).collect(Collectors.toList());
		return listaSalida;
	}
	
	private List<ObjCarterasPQ> obtenerCarterasPQ(Connection con,HashMap<String,Object> paramsCartera ) throws SQLException{
		logger.info("INICIO::ValorizarCarteraFuturos.obtenerCarterasPQ");
		SQL sql=new SQL();
		List<ObjCarterasPQ> listaCarterasPQ = sql.query(con, SQL_SelectCarteraFuturoPQ, paramsCartera, ObjCarterasPQ.class);
		logger.info("FIN::ValorizarCarteraFuturos.obtenerCarterasPQ");
		return listaCarterasPQ;
	}
	
	private List<ObjCarterasWS> obtenerCarterasWS(Connection con,HashMap<String,Object> paramsCartera ) throws SQLException{
		logger.info("INICIO::ValorizarCarteraFuturos.obtenerCarterasWS");
		SQL sql=new SQL();
		List<ObjCarterasWS> listaCarterasWS = sql.query(con, SQL_SelectCarteraFuturoWS, paramsCartera, ObjCarterasWS.class);
		logger.info("FIN::ValorizarCarteraFuturos.obtenerCarterasWS");
		return listaCarterasWS;
	}
	
	public static void main(String[] args) {
		try {
			Connection con = PlataformaUtils.getDESAConnection();
			ValorizarCarteraFuturosEntrada entrada = new ValorizarCarteraFuturosEntrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 0L;
			entrada.p_FechaSistema = "2020-03-19";
			entrada.p_IdPais = 44L;
			entrada.p_SimularOperacion = "N";
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        ValorizarCarteraFuturos demo=new ValorizarCarteraFuturos();
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.commit();
//	    	con.rollback();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}

}
