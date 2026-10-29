package cl.bcs.plataforma.pau;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.ValorizarCustodia;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValorizarCarteraRFIF implements JavaCall {	

	private final Log log = new Log("ValorizarCarteraRFIF");	
	private final String SQL_SelectCartera = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_Cartera.sql");
	private final String SQL_SelectCarteraNegativa = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_CarteraNegativa.sql");
	
	public static class ObjCarteras {		
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
		
		public String 				WS_IdMonedaInstrumento;
		public BigDecimal			WS_BK_INGMontoCompraActualMonedaInstrumento; 
		public BigDecimal			WS_BK_INGMontoATasaCompraActual; 
		public BigDecimal			WS_BK_INGMontoMercadoActualMonedaInstrumento; 
		public BigDecimal			WS_BK_INGMontoATasaMercadoActual; 
		
		//Agregados
		public Long					WS_PlazoEnDias;
		public String				WS_IdTipoReajuste; 
		public BigDecimal			WS_MontoCompra;
		public BigDecimal 			WS_MontoCompraMonedaSistema;                                                      
		public BigDecimal 			WS_MontoMercadoMonedaSistema;                                                     
		public BigDecimal			WS_MontoCompraSubyacente;                                                         
		public BigDecimal			WS_MontoCompraGarantia;
		public String 				WS_IdMonedaEmision;
	//	public String 				WS_GenerarNuevoFolioCustodia;      
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ValorizarCarteraRFIFEntrada {
		public Long 			p_IdEmpresa;
		public Long             p_IdUsuario;
		public String           p_FechaSistema;
		public Long             p_IdPais;
		public String           p_SimularOperacion;
		public Long				p_IdPortafolio=0L;
		public Long				p_IdCliente=0L;
	}
	
	public static class ValorizarCarteraRFIFSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		
		public ValorizarCarteraRFIFSalida(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJE = "";
		}
	}	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ValorizarCarteraRFIFEntrada in=gson.fromJson(jin.toString() ,ValorizarCarteraRFIFEntrada.class);
		ValorizarCarteraRFIFSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	/**********************
	 * lógica del programa
	 **********************/
	public ValorizarCarteraRFIFSalida logica(Connection con, ValorizarCarteraRFIFEntrada entrada) throws Exception {
		log.write("INICIO::ValorizarCarteraRFIF");
		ValorizarCarteraRFIFSalida out = new ValorizarCarteraRFIFSalida();
		out.P_OUT_ERRORESCONTADOR = new Long(0);
		out.P_OUT_MENSAJE = "";
		SQL sql=new SQL();
		List<String> clasificacion = new ArrayList<String>();
		try{
			
			List<ObjCarteras> carteras = new ArrayList<ObjCarteras>();
			List<ObjCarteras> carterasNegativas = new ArrayList<ObjCarteras>();
			clasificacion.add("RF");
			clasificacion.add("IF");
			HashMap<String,Object> paramsCartera = new HashMap<>();
			paramsCartera.put("IdEmpresa",entrada.p_IdEmpresa);
			paramsCartera.put("IdClasificacionTipoInstrumento",clasificacion);
			paramsCartera.put("IdPortafolio", entrada.p_IdPortafolio);
			paramsCartera.put("IdCliente", entrada.p_IdCliente);
			carteras = sql.query(con, SQL_SelectCartera, paramsCartera, ObjCarteras.class);
			carterasNegativas = sql.query(con, SQL_SelectCarteraNegativa, paramsCartera, ObjCarteras.class);
			carteras.addAll(carterasNegativas);
			log.write(carteras.size() + " carteras a valorizar");
			
			ValorizarCustodia.ValorizarCustodiaEntrada ValorizarCust = new ValorizarCustodia.ValorizarCustodiaEntrada();
			ValorizarCust.p_IdEmpresa 					= entrada.p_IdEmpresa;
			ValorizarCust.p_IdUsuario					= entrada.p_IdUsuario;
			ValorizarCust.p_IdModulo 					= "PLA";			
			ValorizarCust.p_IdPais 						= entrada.p_IdPais;
			ValorizarCust.p_OrigenProcesamiento 		= "VALORIZACION_CARTERARFIF";
			
			for (int i = 0; i < carteras.size() ; i++ ){
				ObjCarteras carterasrfif = carteras.get(i);
				ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarInstrumentos = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
				
				ValorizarInstrumentos.FechaOperacion = carterasrfif.WS_FechaOperacion;
				ValorizarInstrumentos.IdClasificacionTipoInstrumento = carterasrfif.WS_IdClasificacionTipoInstrumento;
				ValorizarInstrumentos.IdTipoInstrumento = carterasrfif.WS_IdTipoInstrumento;
				ValorizarInstrumentos.IdInstrumento = carterasrfif.WS_IdInstrumento;
				ValorizarInstrumentos.Nemotecnico = carterasrfif.WS_Nemotecnico;
				ValorizarInstrumentos.IdTipoReajusteInstrumento = carterasrfif.WS_IdTipoReajuste;
				ValorizarInstrumentos.IdMonedaEmisionInstrumento = carterasrfif.WS_IdMonedaEmision;
				ValorizarInstrumentos.Cantidad = carterasrfif.WS_Cantidad;
				ValorizarInstrumentos.ValorRescate = carterasrfif.WS_ValorRescate;
				ValorizarInstrumentos.PrecioCompra = carterasrfif.WS_PrecioCompra;
				ValorizarInstrumentos.TasaCompra = carterasrfif.WS_TasaCompra;
				ValorizarInstrumentos.PrecioSubyacente = carterasrfif.WS_PrecioSubyacente;
				ValorizarInstrumentos.TasaSubyacente = carterasrfif.WS_TasaSubyacente;
				ValorizarInstrumentos.PrecioGarantia = carterasrfif.WS_PrecioGarantia;
				ValorizarInstrumentos.TasaGarantia = carterasrfif.WS_TasaGarantia;
				ValorizarInstrumentos.TieneCorteDeCupon = carterasrfif.WS_TieneCorteDeCupon;
				ValorizarInstrumentos.IdCliente = carterasrfif.WS_IdCliente;
				ValorizarInstrumentos.IdPortafolio = carterasrfif.WS_IdPortafolio;
				ValorizarInstrumentos.TablaOrigen = carterasrfif.TablaOrigen;		
				ValorizarInstrumentos.IdCustodia = carterasrfif.IdTablaOrigen;
				
				
				// Campos agregados nuevos
				ValorizarInstrumentos.TablaOrigen=carterasrfif.TablaOrigen;
				ValorizarInstrumentos.FechaVencimientoInstrumento= carterasrfif.WS_FechaVencimiento;
				ValorizarInstrumentos.IdTipoReajusteInstrumento= carterasrfif.WS_IdTipoReajuste;
				ValorizarInstrumentos.IdMonedaInstrumento =carterasrfif.WS_IdMonedaInstrumento;
				ValorizarInstrumentos.EsRegistroNuevo = "N";
				ValorizarInstrumentos.MontoCompra=carterasrfif.WS_MontoCompra;						
				ValorizarInstrumentos.MontoCompraMonedaSistema=carterasrfif.WS_BK_INGMontoATasaCompraActual;			//preguntas que montos son NULLL 
				ValorizarInstrumentos.MontoMercadoMonedaSistema=carterasrfif.WS_BK_INGMontoATasaMercadoActual;			
				//ValorizarInstrumentos.MontoCompraSubyacente=new BigDecimal(100.2);				
				//ValorizarInstrumentos.MontoCompraGarantia=new BigDecimal(100.2);
				//ValorizarInstrumentos.GenerarNuevoFolioCustodia="S";  // se lee de la configuracion 
				//ValorizarInstrumentos.FechaVencimientoOperacion=""; 
				ValorizarInstrumentos.PlazoEnDiasOperacion=carterasrfif.WS_PlazoEnDias;
				ValorizarInstrumentos.IdMonedaEmisionInstrumento= carterasrfif.WS_IdMonedaEmision;
				
				
				ValorizarCust.p_InstrumentosAValorizar.add(ValorizarInstrumentos);
				//AL FINAL O CADA 5000
				if ((i!= 0 && i%5000 == 0) || i == carteras.size() - 1) {
					log.write("Valorizando hasta " + i);
					ValorizarCustodia ValorizacionCustodia = new ValorizarCustodia();
			        ValorizarCustodia.ValorizarCustodiaSalida salida = ValorizacionCustodia.logica(con, ValorizarCust);
			        out.P_OUT_MENSAJE += PlataformaUtils.concatenarMensajesSalida(salida.P_OUT_MENSAJES);
			        ValorizarCust.p_InstrumentosAValorizar.clear();
				}
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			log.write("ERROR::",e);
			throw e;
		}
		log.write("FIN::ValorizarCarteraRFIF");
		return out;
	}
	
	public static void main(String[] args) {
		 System.out.println("== COMIENZA ==");
        try (Connection con = PlataformaUtils.getMBConnection()){
			
			ValorizarCarteraRFIFEntrada entrada = new ValorizarCarteraRFIFEntrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 6096L;
			entrada.p_FechaSistema = "2018-07-25";
			entrada.p_IdPais = 44L;
			entrada.p_SimularOperacion = "N";
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        ValorizarCarteraRFIF demo=new ValorizarCarteraRFIF();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.commit();
	    	//con.rollback();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
}
