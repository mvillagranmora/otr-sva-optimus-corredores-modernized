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

public class ValorizarCarteraSubPrestEmpres implements JavaCall {	

	private final Log log = new Log("ValorizarCarteraSubPrestEmpres");	
	private final String SQL_SelectCartera = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_CarteraSubPrestEmpres.sql");
	
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
		public String 				PQ_IdMonedaInstrumento;
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
		public String 				IdClasificacionTipoInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ValorizarCarteraSubPrestEmpresEntrada {
		public Long 			p_IdEmpresa;
		public Long             p_IdUsuario;
		public String           p_FechaSistema;
		public Long             p_IdPais;
		public String           p_SimularOperacion;
		public Long				p_IdPortafolio = 0L;
		public Long				p_IdCliente = 0L;
		public Long				p_FolioCartera = 0L;
		public String			p_EsPauta = "N";
	}
	
	public static class ValorizarCarteraSubPrestEmpresSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		
		public ValorizarCarteraSubPrestEmpresSalida(){
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
		ValorizarCarteraSubPrestEmpresEntrada in=gson.fromJson(jin.toString() ,ValorizarCarteraSubPrestEmpresEntrada.class);
		ValorizarCarteraSubPrestEmpresSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	/**********************
	 * lógica del programa
	 **********************/
	public ValorizarCarteraSubPrestEmpresSalida logica(Connection con, ValorizarCarteraSubPrestEmpresEntrada entrada) throws Exception {
		ValorizarCarteraSubPrestEmpresSalida out = new ValorizarCarteraSubPrestEmpresSalida();
		out.P_OUT_ERRORESCONTADOR = 0L;
		out.P_OUT_MENSAJE = "";
		SQL sql=new SQL();

		List<ObjCarteras> carteras = new ArrayList<ObjCarteras>();
		HashMap<String,Object> paramsCartera = new HashMap<>();
		paramsCartera.put("IdEmpresa",entrada.p_IdEmpresa);
		paramsCartera.put("IdPortafolio", entrada.p_IdPortafolio);
		paramsCartera.put("IdCliente", entrada.p_IdCliente);
		paramsCartera.put("FolioCarteraRelacionada", entrada.p_FolioCartera);
		carteras = sql.query(con, SQL_SelectCartera, paramsCartera, ObjCarteras.class);
		
		ValorizarCustodia.ValorizarCustodiaEntrada ValorizarCust = new ValorizarCustodia.ValorizarCustodiaEntrada();
		ValorizarCust.p_IdEmpresa 					= entrada.p_IdEmpresa;
		ValorizarCust.p_IdUsuario					= entrada.p_IdUsuario;
		ValorizarCust.p_IdModulo 					= "PLA"; 			
		ValorizarCust.p_IdPais 						= entrada.p_IdPais;
		ValorizarCust.p_OrigenProcesamiento 		= "VALORIZACION_CARTERAS_SUBYACENTE_PRESTAMOS_EMPRESTITOS";
		Long IdValorizacion = 0L;
		ValorizarCust.p_EsPauta						= entrada.p_EsPauta;
		
		for (int i = 0; i < carteras.size() ; i++ ){
			ObjCarteras carterassubpremp = carteras.get(i);
			String clasificacionInstrumento =  carteras.get(i).IdClasificacionTipoInstrumento;
			ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarInstrumentos = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
			ValorizarInstrumentos.FechaOperacion = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_FechaOperacion  : carterassubpremp.PQ_FechaOperacion;
			ValorizarInstrumentos.IdClasificacionTipoInstrumento = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_IdClasificacionTipoInstrumento : carterassubpremp.PQ_IdClasificacionTipoInstrumento;
			ValorizarInstrumentos.IdTipoInstrumento = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_IdTipoInstrumento : carterassubpremp.PQ_IdTipoInstrumento;
			ValorizarInstrumentos.IdInstrumento = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_IdInstrumento : carterassubpremp.PQ_IdInstrumento;
			ValorizarInstrumentos.Nemotecnico = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_Nemotecnico : carterassubpremp.PQ_Nemotecnico;
			ValorizarInstrumentos.FechaOperacion = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_FechaOperacion : carterassubpremp.PQ_FechaOperacion;
			ValorizarInstrumentos.IdTipoReajusteInstrumento = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_IdTipoReajuste : null;
			ValorizarInstrumentos.Cantidad = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_Cantidad : carterassubpremp.PQ_Cantidad;
			ValorizarInstrumentos.ValorRescate = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_ValorRescate : null;
			ValorizarInstrumentos.PrecioCompra = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_PrecioCompra : carterassubpremp.PQ_Precio;
			ValorizarInstrumentos.TasaCompra = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_TasaCompra  : carterassubpremp.PQ_Tasa ;
			ValorizarInstrumentos.PrecioSubyacente = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_PrecioSubyacente : null;
			ValorizarInstrumentos.TasaSubyacente = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_TasaSubyacente : null;
			ValorizarInstrumentos.PrecioGarantia = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_PrecioGarantia : null;
			ValorizarInstrumentos.TasaGarantia = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_TasaGarantia : null;
			ValorizarInstrumentos.TieneCorteDeCupon = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_TieneCorteDeCupon : null;
			ValorizarInstrumentos.IdCliente = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_IdCliente : carterassubpremp.PQ_IdCliente;
			ValorizarInstrumentos.IdPortafolio = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_IdPortafolio : carterassubpremp.PQ_IdPortafolio;
			ValorizarInstrumentos.TablaOrigen = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.TablaOrigen : carterassubpremp.TablaOrigen;		
			ValorizarInstrumentos.IdCustodia = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.IdTablaOrigen : carterassubpremp.IdTablaOrigen;
			ValorizarInstrumentos.IdMonedaInstrumento = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.PQ_IdMoneda : carterassubpremp.PQ_IdMoneda;
			ValorizarInstrumentos.FechaVencimientoInstrumento = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_FechaVencimiento : null;
			ValorizarInstrumentos.IdMonedaInstrumento = "RF".equalsIgnoreCase(clasificacionInstrumento) ? carterassubpremp.WS_IdMonedaInstrumento : carterassubpremp.PQ_IdMoneda;
			ValorizarInstrumentos.EsRegistroNuevo = "N";
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
		
		ValorizarCustodia ValorizacionCustodia = new ValorizarCustodia();
        ValorizarCustodia.ValorizarCustodiaSalida salida = ValorizacionCustodia.logica(con, ValorizarCust);
        System.out.println(salida);
		
		return out;
	}
	
	public static void main(String[] args) {
		try {
			Connection con = PlataformaUtils.getPropertiesConnection("desa2");
			ValorizarCarteraSubPrestEmpresEntrada entrada = new ValorizarCarteraSubPrestEmpresEntrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 6096L;
			entrada.p_FechaSistema = "2024-02-02";
			entrada.p_IdPais = 44L;
			entrada.p_SimularOperacion = "N";
			entrada.p_FolioCartera = 541370L;
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        ValorizarCarteraSubPrestEmpres demo=new ValorizarCarteraSubPrestEmpres();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.commit();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
}
