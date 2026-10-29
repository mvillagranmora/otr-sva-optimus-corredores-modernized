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

public class ValorizarCarteraPactos implements JavaCall {	

	private final Log log = new Log("ValorizarCarteraPactos");	
	private final String SQL_SelectCartera = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_CarteraPactos.sql");
	private final String SQL_SelectSubyacente = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_CarteraPactos_Subyacente.sql");
	
	public static class ObjCarteras {		
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
		public Long 				WS_PlazoEnDias;
		public String 				WS_IdTipoReajusteCabecera;		
		public BigDecimal           WS_MontoInicialSubyacente;
		public BigDecimal			WS_TasaCompraCarteraOrigen;
		public BigDecimal			WS_TasaCompraCarteraPacto;
		
		public	ObjCarteras() {
			this.WS_IdTipoReajusteCabecera= "";
			this.WS_MontoInicialSubyacente= BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ValorizarCarteraPactosEntrada {
		public Long 			p_IdEmpresa;
		public Long             p_IdUsuario;
		public String           p_FechaSistema;
		public Long             p_IdPais;
		public String           p_SimularOperacion;
		public Long				p_IdPortafolio=0L;
		public Long				p_IdCliente=0L;
		public String			p_EsPauta="N";
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ValorizarCarteraPactosSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ValorizarCarteraPactosSalida(){
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
		ValorizarCarteraPactosEntrada in=gson.fromJson(jin.toString() ,ValorizarCarteraPactosEntrada.class);
		ValorizarCarteraPactosSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	/**********************
	 * lógica del programa
	 **********************/
	public ValorizarCarteraPactosSalida logica(Connection con, ValorizarCarteraPactosEntrada entrada) throws Exception {
		ValorizarCarteraPactosSalida out = new ValorizarCarteraPactosSalida();
		out.P_OUT_ERRORESCONTADOR = new Long(0);
		out.P_OUT_MENSAJE = "";
		SQL sql=new SQL();

		List<ObjCarteras> carteras = new ArrayList<ObjCarteras>();
		List<ObjCarteras> subyacentes = new ArrayList<ObjCarteras>();
		HashMap<String,Object> paramsCartera = new HashMap<>();
		paramsCartera.put("IdEmpresa",1);
		paramsCartera.put("IdPortafolio", entrada.p_IdPortafolio);
		paramsCartera.put("IdCliente", entrada.p_IdCliente);
		paramsCartera.put("fechaSistema", entrada.p_FechaSistema);
		
		carteras = sql.query(con, SQL_SelectCartera, paramsCartera, ObjCarteras.class);
		subyacentes = sql.query(con, SQL_SelectSubyacente, paramsCartera, ObjCarteras.class);
		
		carteras.addAll(subyacentes);
		
		ValorizarCustodia.ValorizarCustodiaEntrada ValorizarCust = new ValorizarCustodia.ValorizarCustodiaEntrada();
		ValorizarCust.p_IdEmpresa 					= entrada.p_IdEmpresa;
		ValorizarCust.p_IdUsuario					= entrada.p_IdUsuario;
		ValorizarCust.p_IdModulo 					= "PLA";		
		ValorizarCust.p_IdPais 						= entrada.p_IdPais;
		ValorizarCust.p_OrigenProcesamiento 		= "VALORIZACION_CARTERAPACTOS";
		ValorizarCust.p_EsPauta						= entrada.p_EsPauta;
		
		for (int i = 0; i < carteras.size() ; i++ ){
			ObjCarteras carteraspac = carteras.get(i);
			ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarInstrumentos = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
			ValorizarInstrumentos.FechaOperacion = carteraspac.WS_FechaOperacion;
			ValorizarInstrumentos.IdClasificacionTipoInstrumento = carteraspac.WS_IdClasificacionTipoInstrumento;
			ValorizarInstrumentos.IdTipoInstrumento = carteraspac.WS_IdTipoInstrumento;
			ValorizarInstrumentos.FechaVencimientoOperacion = carteraspac.WS_FechaVencimiento;
			ValorizarInstrumentos.FechaVencimientoInstrumento = carteraspac.WS_FechaVencimiento;
			ValorizarInstrumentos.IdInstrumento = carteraspac.WS_IdInstrumento;
			ValorizarInstrumentos.Nemotecnico = carteraspac.WS_Nemotecnico;
			
			ValorizarInstrumentos.IdMonedaInstrumento = carteraspac.WS_IdMonedaInstrumento;
			ValorizarInstrumentos.EsRegistroNuevo = "N";
			
			ValorizarInstrumentos.IdMonedaEmisionInstrumento= carteraspac.WS_IdMonedaEmision;
			
			ValorizarInstrumentos.Nemotecnico = carteraspac.WS_Nemotecnico;
			ValorizarInstrumentos.IdTipoReajusteInstrumento = carteraspac.WS_IdTipoReajuste;
			ValorizarInstrumentos.Cantidad = carteraspac.WS_Cantidad;
			ValorizarInstrumentos.ValorRescate = carteraspac.WS_MontoCompra;//INGMontoCompra
			ValorizarInstrumentos.PrecioCompra = carteraspac.WS_PrecioCompra;
			ValorizarInstrumentos.TasaCompra = carteraspac.WS_TasaCompra;
			ValorizarInstrumentos.PrecioSubyacente = carteraspac.WS_PrecioSubyacente;
			ValorizarInstrumentos.TasaSubyacente = carteraspac.WS_TasaSubyacente;
			ValorizarInstrumentos.PrecioGarantia = carteraspac.WS_PrecioGarantia;
			ValorizarInstrumentos.TasaGarantia = carteraspac.WS_TasaGarantia;
			ValorizarInstrumentos.TieneCorteDeCupon = carteraspac.WS_TieneCorteDeCupon;
			ValorizarInstrumentos.IdCliente = carteraspac.WS_IdCliente;
			ValorizarInstrumentos.IdPortafolio = carteraspac.WS_IdPortafolio;
			ValorizarInstrumentos.TablaOrigen = carteraspac.TablaOrigen;		
			ValorizarInstrumentos.IdCustodia = carteraspac.IdTablaOrigen;
			ValorizarInstrumentos.IdTipoOperacion = carteraspac.WS_IdTipoOperacion;
			ValorizarInstrumentos.MontoCompra ="SUBYACENTE".equals(carteraspac.TablaOrigen) ? carteraspac.WS_MontoInicialSubyacente:null;//siempre se valoriza con valor rescate/cantidad + precio/tasa
			ValorizarInstrumentos.MontoCompraMonedaSistema = "SUBYACENTE".equals(carteraspac.TablaOrigen) ? carteraspac.WS_BK_INGMontoATasaCompraActual:null;//siempre se valoriza con valor rescate/cantidad + precio/tasa
			ValorizarInstrumentos.IdTipoReajusteCabecera = carteraspac.WS_IdTipoReajusteCabecera;
				
			
			if(carteraspac.WS_IdEmisor.equals(""))  carteraspac.WS_IdEmisor = "OPTIMUS";
			ValorizarInstrumentos.Emisor = carteraspac.WS_IdEmisor;
			ValorizarInstrumentos.PlazoEnDiasOperacion = carteraspac.WS_PlazoEnDias;
			
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
		//Settings by file
		try {
			Connection con = PlataformaUtils.getPropertiesConnection("desa2","connections.properties");
			ValorizarCarteraPactosEntrada entrada = new ValorizarCarteraPactosEntrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 97L;
			entrada.p_FechaSistema = "2024-11-15";
			entrada.p_IdPais = 44L;
			entrada.p_SimularOperacion = "N";
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        ValorizarCarteraPactos demo = new ValorizarCarteraPactos();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.rollback();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
}
