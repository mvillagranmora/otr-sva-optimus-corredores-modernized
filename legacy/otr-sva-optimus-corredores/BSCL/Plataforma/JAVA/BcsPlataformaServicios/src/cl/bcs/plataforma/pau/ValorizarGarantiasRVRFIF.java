package cl.bcs.plataforma.pau;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.ValorizarCustodia;
import cl.bcs.plataforma.pau.ValorizarCarteraCupones.ObjCarteras;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValorizarGarantiasRVRFIF implements JavaCall {	

	private final Log log = new Log("ValorizarGarantiasRVRFIF");	
	private final String SQL_SelectCartera = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_Garantia.sql");
	
	public static class ObjCarteras {		
		public Long					IdProcesamientoValorizaCustodiaMasiva; 
		public Long					IdTablaOrigen; 
		public String				TablaOrigen; 
		public String				EstadoProcesamiento; 
		public String				IdClasificacionTipoInstrumento;
		public java.sql.Date		PQ_FechaOperacion;
		public String				PQ_ValorizaACompra; 
		public String				PQ_IdClasificacionTipoInstrumento; 
		public String				PQ_IdTipoInstrumento; 
		public Long					PQ_IdInstrumento; 
		public String				PQ_Nemotecnico; 
		public String				PQ_IdMoneda;
		public String				PQ_IdMonedaEmision;
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
		public String				WS_IdMoneda;
		public String				WS_IdMonedaEmision;
		public BigDecimal			WS_BK_INGMontoCompraActualMonedaInstrumento; 
		public BigDecimal			WS_BK_INGMontoATasaCompraActual; 
		public BigDecimal			WS_BK_INGMontoMercadoActualMonedaInstrumento; 
		public BigDecimal			WS_BK_INGMontoATasaMercadoActual; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ValorizarCarteraRVRFIFEntrada {
		public Long 			p_IdEmpresa;
		public Long             p_IdUsuario;
		public String           p_FechaSistema;
		public Long             p_IdPais;
		public String           p_SimularOperacion;
		public Long				p_IdPortafolio=0L;
		public Long				p_IdCliente=0L;
		public String			p_EsPauta="N";
		public Long				p_IdLogValorizadorPauta=0L;
	}
	
	public static class ValorizarCarteraRVRFIFSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		
		public ValorizarCarteraRVRFIFSalida(){
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
		ValorizarCarteraRVRFIFEntrada in=gson.fromJson(jin.toString() ,ValorizarCarteraRVRFIFEntrada.class);
		ValorizarCarteraRVRFIFSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	/**********************
	 * lógica del programa
	 **********************/
	public ValorizarCarteraRVRFIFSalida logica(Connection con, ValorizarCarteraRVRFIFEntrada entrada) throws Exception {
		ValorizarCarteraRVRFIFSalida out = new ValorizarCarteraRVRFIFSalida();
		out.P_OUT_ERRORESCONTADOR = new Long(0);
		out.P_OUT_MENSAJE = "";
		SQL sql=new SQL();

		List<ObjCarteras> carteras = new ArrayList<ObjCarteras>();
		HashMap<String,Object> paramsCartera = new HashMap<>();
		paramsCartera.put("IdEmpresa",entrada.p_IdEmpresa);
		paramsCartera.put("IdPortafolio", entrada.p_IdPortafolio);
		paramsCartera.put("IdCliente", entrada.p_IdCliente);
		carteras = sql.query(con, SQL_SelectCartera, paramsCartera, ObjCarteras.class);
		
		ValorizarCustodia.ValorizarCustodiaEntrada ValorizarCust = new ValorizarCustodia.ValorizarCustodiaEntrada();
		ValorizarCust.p_IdEmpresa 					= entrada.p_IdEmpresa;
		ValorizarCust.p_IdUsuario					= entrada.p_IdUsuario;
		ValorizarCust.p_IdModulo 					= "PLA";			
		ValorizarCust.p_IdPais 						= entrada.p_IdPais;
		ValorizarCust.p_OrigenProcesamiento 		= "VALORIZACION_CARTERARV";
		Long IdValorizacion = 0L;
		ValorizarCust.p_EsPauta						= entrada.p_EsPauta;
		ValorizarCust.p_IdLogValorizadorPauta		= entrada.p_IdLogValorizadorPauta;
		
		for (int i = 0; i < carteras.size() ; i++ ){
			ObjCarteras carterasrvrfif = carteras.get(i);
			ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarInstrumentos = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
			if("RV".equals(carterasrvrfif.IdClasificacionTipoInstrumento) || "FM".equals(carterasrvrfif.IdClasificacionTipoInstrumento)){
				ValorizarInstrumentos.FechaOperacion = carterasrvrfif.PQ_FechaOperacion;
				ValorizarInstrumentos.IdClasificacionTipoInstrumento = carterasrvrfif.PQ_IdClasificacionTipoInstrumento;
				ValorizarInstrumentos.IdTipoInstrumento = carterasrvrfif.PQ_IdTipoInstrumento;
				ValorizarInstrumentos.IdInstrumento = carterasrvrfif.PQ_IdInstrumento;
				ValorizarInstrumentos.Nemotecnico = carterasrvrfif.PQ_Nemotecnico;
				ValorizarInstrumentos.Cantidad = carterasrvrfif.PQ_Cantidad;
				ValorizarInstrumentos.ValorRescate = BigDecimal.ZERO;
				ValorizarInstrumentos.PrecioCompra = BigDecimal.ZERO;
				ValorizarInstrumentos.PrecioGarantia = carterasrvrfif.PQ_Precio;
				ValorizarInstrumentos.TasaGarantia = BigDecimal.ZERO;
				ValorizarInstrumentos.IdCliente = carterasrvrfif.PQ_IdCliente;
				ValorizarInstrumentos.IdPortafolio = carterasrvrfif.PQ_IdPortafolio;
				ValorizarInstrumentos.FechaVencimientoOperacion = carterasrvrfif.PQ_FechaVencimientoOperacion;
				ValorizarInstrumentos.TablaOrigen = carterasrvrfif.TablaOrigen;		
				ValorizarInstrumentos.IdCustodia = carterasrvrfif.IdTablaOrigen;
				ValorizarInstrumentos.OrigenProcesamiento = "VALORIZACION_CARTERARV";
				ValorizarInstrumentos.EsRegistroNuevo = "N";
				ValorizarInstrumentos.IdMonedaInstrumento = carterasrvrfif.PQ_IdMoneda;
				ValorizarInstrumentos.IdMonedaEmisionInstrumento = carterasrvrfif.PQ_IdMonedaEmision;
				ValorizarCust.p_InstrumentosAValorizar.add(ValorizarInstrumentos);
			}else{
				ValorizarInstrumentos.FechaOperacion = carterasrvrfif.WS_FechaOperacion;
				ValorizarInstrumentos.IdClasificacionTipoInstrumento = carterasrvrfif.WS_IdClasificacionTipoInstrumento;
				ValorizarInstrumentos.IdTipoInstrumento = carterasrvrfif.WS_IdTipoInstrumento;
				ValorizarInstrumentos.IdInstrumento = carterasrvrfif.WS_IdInstrumento;
				ValorizarInstrumentos.Nemotecnico = carterasrvrfif.WS_Nemotecnico;
				ValorizarInstrumentos.IdTipoReajusteInstrumento = carterasrvrfif.WS_IdTipoReajuste;
				ValorizarInstrumentos.Cantidad = carterasrvrfif.WS_Cantidad;
				ValorizarInstrumentos.ValorRescate = carterasrvrfif.WS_ValorRescate;
				ValorizarInstrumentos.PrecioCompra = carterasrvrfif.WS_PrecioCompra;
				ValorizarInstrumentos.TasaCompra = carterasrvrfif.WS_TasaCompra;
				ValorizarInstrumentos.PrecioSubyacente = carterasrvrfif.WS_PrecioSubyacente;
				ValorizarInstrumentos.TasaSubyacente = carterasrvrfif.WS_TasaSubyacente;
				ValorizarInstrumentos.PrecioGarantia = carterasrvrfif.WS_PrecioGarantia;
				ValorizarInstrumentos.TasaGarantia = carterasrvrfif.WS_TasaGarantia;
				ValorizarInstrumentos.TieneCorteDeCupon = carterasrvrfif.WS_TieneCorteDeCupon;
				ValorizarInstrumentos.IdCliente = carterasrvrfif.WS_IdCliente;
				ValorizarInstrumentos.IdPortafolio = carterasrvrfif.WS_IdPortafolio;
				ValorizarInstrumentos.Emisor = carterasrvrfif.WS_IdEmisor;
				ValorizarInstrumentos.TablaOrigen = carterasrvrfif.TablaOrigen;		
				ValorizarInstrumentos.IdCustodia = carterasrvrfif.IdTablaOrigen;
				ValorizarInstrumentos.FechaVencimientoInstrumento = carterasrvrfif.WS_FechaVencimiento;
				ValorizarInstrumentos.EsRegistroNuevo = "N";
				ValorizarInstrumentos.OrigenProcesamiento = "VALORIZACION_CARTERARF";
				ValorizarInstrumentos.IdMonedaInstrumento = carterasrvrfif.WS_IdMoneda;
				ValorizarInstrumentos.IdMonedaEmisionInstrumento = carterasrvrfif.WS_IdMonedaEmision;
				ValorizarCust.p_InstrumentosAValorizar.add(ValorizarInstrumentos);
			}
			
			//AL FINAL O CADA 5000
			if ((i!= 0 && i%5000 == 0) || i == carteras.size() - 1) {
				log.write("Valorizando hasta " + i);
				ValorizarCustodia ValorizacionCustodia = new ValorizarCustodia();
		        ValorizarCustodia.ValorizarCustodiaSalida salida = ValorizacionCustodia.logica(con, ValorizarCust);
		        out.P_OUT_MENSAJE += PlataformaUtils.concatenarMensajesSalida(salida.P_OUT_MENSAJES);
		        if("S".equalsIgnoreCase(entrada.p_EsPauta) && !Long.valueOf(0L).equals(entrada.p_IdLogValorizadorPauta)	&& !Long.valueOf(0L).equals(salida.P_OUT_ERRORESCONTADOR)) {
		        	out.P_OUT_ERRORESCONTADOR = 1L;
		        }
		        ValorizarCust.p_InstrumentosAValorizar.clear();
			}
		}
		
		return out;
	}
	
	public static void main(String[] args) {
		//Settings by file
		try {
			Connection con = PlataformaUtils.getUATConnection();
			ValorizarCarteraRVRFIFEntrada entrada = new ValorizarCarteraRVRFIFEntrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 1L;
			entrada.p_FechaSistema = "2018-08-07";
			entrada.p_IdPais = 44L;
			entrada.p_SimularOperacion = "N";
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        ValorizarGarantiasRVRFIF demo=new ValorizarGarantiasRVRFIF();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.commit();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
}
