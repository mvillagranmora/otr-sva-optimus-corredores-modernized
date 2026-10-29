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

public class ValorizarCarteraSimultaneas implements JavaCall {	

	private final Log log = new Log("ValorizarCarteraSimultaneas");	
	private final String SQL_SelectCartera = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_CarteraSimultaneas.sql");
	
	
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
		public BigDecimal			PQ_Plazo; 
		public BigDecimal			PQ_PlazoResidual; 
		public Long					PQ_Base; 
		public Long					PQ_IdCliente; 
		public Long					PQ_IdPortafolio;
		public java.sql.Date		PQ_FechaLiquidacion;
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
	
	public static class ValorizarCarteraSIMEntrada {
		public Long 			p_IdEmpresa;
		public Long             p_IdUsuario;
		public String           p_FechaSistema;
		public Long             p_IdPais;
		public String           p_SimularOperacion;
		public Long				p_IdPortafolio=0L;
		public Long				p_IdCliente=0L;
		public String			p_EsPauta="N";
	}
	
	public static class ValorizarCarteraSIMSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		
		public ValorizarCarteraSIMSalida(){
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
		ValorizarCarteraSIMEntrada in=gson.fromJson(jin.toString() ,ValorizarCarteraSIMEntrada.class);
		ValorizarCarteraSIMSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	/**********************
	 * lógica del programa
	 **********************/
	public ValorizarCarteraSIMSalida logica(Connection con, ValorizarCarteraSIMEntrada entrada) throws Exception {
			
		ValorizarCarteraSIMSalida out = new ValorizarCarteraSIMSalida();
		out.P_OUT_ERRORESCONTADOR = new Long(0);
		out.P_OUT_MENSAJE = "";
		SQL sql=new SQL();

		List<ObjCarteras> carteras = new ArrayList<ObjCarteras>();
		HashMap<String,Object> paramsCartera = new HashMap<>();
		paramsCartera.put("IdEmpresa",entrada.p_IdEmpresa);
		paramsCartera.put("IdPortafolio", entrada.p_IdPortafolio);
		paramsCartera.put("FechaValorizacion", entrada.p_FechaSistema);
		paramsCartera.put("IdCliente", entrada.p_IdCliente);
		carteras = sql.query(con, SQL_SelectCartera, paramsCartera, ObjCarteras.class);
		
		ValorizarCustodia.ValorizarCustodiaEntrada ValorizarCust = new ValorizarCustodia.ValorizarCustodiaEntrada();
		ValorizarCust.p_IdEmpresa 					= entrada.p_IdEmpresa;
		ValorizarCust.p_IdUsuario					= entrada.p_IdUsuario;
		ValorizarCust.p_IdModulo 					= "PLA";			
		ValorizarCust.p_IdPais 						= entrada.p_IdPais;
		ValorizarCust.p_OrigenProcesamiento 		= "VALORIZACION_CARTERASIM";
		ValorizarCust.p_EsPauta						= entrada.p_EsPauta;
		
		
		for (int i = 0; i < carteras.size() ; i++ ){
			ObjCarteras carterassim = carteras.get(i);
			ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarInstrumentos = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
			ValorizarInstrumentos.FechaOperacion = carterassim.PQ_FechaOperacion;
			ValorizarInstrumentos.IdClasificacionTipoInstrumento = carterassim.PQ_IdClasificacionTipoInstrumento;
			ValorizarInstrumentos.IdTipoInstrumento = carterassim.PQ_IdTipoInstrumento;
			ValorizarInstrumentos.IdInstrumento = carterassim.PQ_IdInstrumento;
			ValorizarInstrumentos.Nemotecnico = carterassim.PQ_Nemotecnico;
			ValorizarInstrumentos.IdTipoReajusteInstrumento = carterassim.PQ_IdMoneda; //WS_IdTipoReajuste;
			ValorizarInstrumentos.Cantidad = carterassim.PQ_Cantidad;
			ValorizarInstrumentos.ValorRescate = null; //carterassim.WS_ValorRescate;
			ValorizarInstrumentos.PrecioCompra = carterassim.PQ_Precio; //carterassim.WS_PrecioCompra;
			ValorizarInstrumentos.TasaCompra = carterassim.PQ_Tasa;
			ValorizarInstrumentos.PrecioSubyacente = carterassim.WS_PrecioSubyacente;
			ValorizarInstrumentos.TasaSubyacente = carterassim.WS_TasaSubyacente;
			ValorizarInstrumentos.PrecioGarantia = carterassim.WS_PrecioGarantia;
			ValorizarInstrumentos.TasaGarantia = carterassim.WS_TasaGarantia;
			ValorizarInstrumentos.TieneCorteDeCupon = carterassim.WS_TieneCorteDeCupon;
			ValorizarInstrumentos.IdCliente = carterassim.PQ_IdCliente;
			ValorizarInstrumentos.IdPortafolio = carterassim.PQ_IdPortafolio;
			ValorizarInstrumentos.TablaOrigen = carterassim.TablaOrigen;		
			ValorizarInstrumentos.IdCustodia = carterassim.IdTablaOrigen;
			
			ValorizarInstrumentos.EsRegistroNuevo = "N";
			ValorizarInstrumentos.PlazoEnDiasOperacion = carterassim.PQ_Plazo.longValue();
			ValorizarInstrumentos.FechaVencimientoOperacion = carterassim.PQ_FechaVencimientoOperacion;
			ValorizarInstrumentos.FechaLiquidacion = carterassim.PQ_FechaLiquidacion;
			ValorizarInstrumentos.MontoCompraMonedaSistema = carterassim.WS_BK_INGMontoATasaCompraActual;
			ValorizarInstrumentos.MontoCompra = carterassim.WS_BK_INGMontoATasaCompraActual;
			ValorizarInstrumentos.IdMonedaInstrumento = carterassim.PQ_IdMoneda; //WS_IdTipoReajuste;
			ValorizarInstrumentos.OrigenProcesamiento = "VALORIZACION_CARTERASIM";
			
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
		
		return out;
	}
	
	public static void main(String[] args) {
		
			Connection con;
			try {
				con = PlataformaUtils.getMBConnection();
				ValorizarCarteraSIMEntrada entrada = new ValorizarCarteraSIMEntrada();
				entrada.p_IdEmpresa = 1L;
				entrada.p_IdUsuario = 377L;
				entrada.p_FechaSistema = "2019-02-06";
				entrada.p_IdPais = 44L;
				entrada.p_SimularOperacion = "N";
	 			JsonParser parser 		= new JsonParser();
		        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
		        ValorizarCarteraSimultaneas demo=new ValorizarCarteraSimultaneas();		
		    	System.out.println(demo.call(con, dataJsonIn).toString());	
		    	con.commit();
		    	con.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
        System.out.println("== END ==");
	}
}
