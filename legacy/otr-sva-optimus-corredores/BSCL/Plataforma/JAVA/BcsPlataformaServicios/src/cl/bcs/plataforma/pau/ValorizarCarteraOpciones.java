package cl.bcs.plataforma.pau;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.ValorizarCustodia;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValorizarCarteraOpciones implements JavaCall {	

	static final Logger logger = LogManager.getLogger(ValorizarCarteraOpciones.class.getName());
	private final String SQL_SelectCartera = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_CarteraOpciones.sql");
	
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
	
	public static class ValorizarCarteraOpcionesEntrada {
		public Long 			p_IdEmpresa;
		public Long             p_IdUsuario;
		public String           p_FechaSistema;
		public Long             p_IdPais;
		public String           p_SimularOperacion;
		public Long				p_IdPortafolio = 0L;
		public Long				p_IdCliente = 0L;
	}
	
	public static class ValorizarCarteraOpcionesSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		
		public ValorizarCarteraOpcionesSalida(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJE = "";
		}
	}	
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = Utils.getGsonBuilder();		
		ValorizarCarteraOpcionesEntrada in=gson.fromJson(jin.toString() ,ValorizarCarteraOpcionesEntrada.class);
		ValorizarCarteraOpcionesSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	/**********************
	 * lógica del programa
	 **********************/
	public ValorizarCarteraOpcionesSalida logica(Connection con, ValorizarCarteraOpcionesEntrada entrada) throws Exception {
		logger.info("Inicio");
		ValorizarCarteraOpcionesSalida out = new ValorizarCarteraOpcionesSalida();
		out.P_OUT_ERRORESCONTADOR = new Long(0);
		out.P_OUT_MENSAJE = "";
		SQL sql=new SQL();

		List<ObjCarteras> carteras = new ArrayList<ObjCarteras>();
		HashMap<String,Object> paramsCartera = new HashMap<>();
		paramsCartera.put("IdEmpresa",entrada.p_IdEmpresa);
		paramsCartera.put("IdClasificacionTipoInstrumento","DV");	
		paramsCartera.put("IdPortafolio", entrada.p_IdPortafolio);
		paramsCartera.put("IdCliente", entrada.p_IdCliente);
		
		carteras = sql.query(con, SQL_SelectCartera, paramsCartera, ObjCarteras.class);
		
		ValorizarCustodia.ValorizarCustodiaEntrada ValorizarCust = new ValorizarCustodia.ValorizarCustodiaEntrada();
		ValorizarCust.p_IdEmpresa 					= entrada.p_IdEmpresa;
		ValorizarCust.p_IdUsuario					= entrada.p_IdUsuario;
		ValorizarCust.p_IdModulo 					= "PLA";			
		ValorizarCust.p_IdPais 						= entrada.p_IdPais;
		ValorizarCust.p_OrigenProcesamiento 		= "VALORIZACION_CARTERAOPCIONES";
		
		for (int i = 0; i < carteras.size() ; i++ ){
			ObjCarteras carterasopc = carteras.get(i);
			ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarInstrumentos = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
			ValorizarInstrumentos.FechaOperacion = carterasopc.PQ_FechaOperacion;
			ValorizarInstrumentos.IdClasificacionTipoInstrumento = carterasopc.PQ_IdClasificacionTipoInstrumento;
			ValorizarInstrumentos.IdTipoInstrumento = carterasopc.PQ_IdTipoInstrumento;
			ValorizarInstrumentos.IdInstrumento = carterasopc.PQ_IdInstrumento;
			ValorizarInstrumentos.Nemotecnico = carterasopc.PQ_Nemotecnico;
			ValorizarInstrumentos.Cantidad = carterasopc.PQ_Cantidad;
			ValorizarInstrumentos.ValorRescate = BigDecimal.ZERO;
			ValorizarInstrumentos.PrecioCompra = carterasopc.PQ_Precio;
			ValorizarInstrumentos.TasaCompra = BigDecimal.ZERO;
			ValorizarInstrumentos.IdCliente = carterasopc.PQ_IdCliente;
			ValorizarInstrumentos.IdPortafolio = carterasopc.PQ_IdPortafolio;
			ValorizarInstrumentos.TablaOrigen = carterasopc.TablaOrigen;		
			ValorizarInstrumentos.IdCustodia = carterasopc.IdTablaOrigen;
			ValorizarInstrumentos.EsRegistroNuevo = "S";
			ValorizarInstrumentos.IdMonedaInstrumento = carterasopc.PQ_IdMoneda;
			ValorizarCust.p_InstrumentosAValorizar.add(ValorizarInstrumentos);
			
		}
		
		ValorizarCustodia ValorizacionCustodia = new ValorizarCustodia();
        ValorizarCustodia.ValorizarCustodiaSalida salida = ValorizacionCustodia.logica(con, ValorizarCust);
        out.P_OUT_MENSAJE += PlataformaUtils.concatenarMensajesSalida(salida.P_OUT_MENSAJES);
       return out;
	}
	
	public static void main(String[] args) {
		try {
			Connection con = PlataformaUtils.getDESAConnection();
			ValorizarCarteraOpcionesEntrada entrada = new ValorizarCarteraOpcionesEntrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 0L;
			entrada.p_FechaSistema = "2020-01-20";
			entrada.p_IdPais = 44L;
			entrada.p_SimularOperacion = "N";
			entrada.p_IdCliente = 123569L;
			entrada.p_IdPortafolio = 27499L;
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        ValorizarCarteraOpciones demo=new ValorizarCarteraOpciones();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.commit();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
}
