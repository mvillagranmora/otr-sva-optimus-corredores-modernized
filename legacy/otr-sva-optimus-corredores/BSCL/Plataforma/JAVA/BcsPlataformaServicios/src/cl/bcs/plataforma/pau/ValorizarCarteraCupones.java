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
//import cl.bcs.custodia.utils.GenerarMovimientoCustodia.ObjMovimientosEnTransito;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.ParametrosAgenda;
import cl.bcs.plataforma.bat.AgregarPrograma.ParametrosPrograma;
import cl.bcs.plataforma.bat.AgregarPrograma.Secuencia;
import cl.bcs.plataforma.bat.AgregarPrograma.InAgregarPrograma.Parametro;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValorizarCarteraCupones implements JavaCall {	

	private final Log log = new Log("ValorizarCarteraCupones");	
	private final String SQL_SelectCartera = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_CarteraCupones.sql");
	
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
		public String WS_IdMonedaInstrumento;
		public String WS_IdMonedaEmision; 
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ValorizarCarteraCuponesEntrada {
		public Long 			p_IdEmpresa;
		public Long             p_IdUsuario;
		public String           p_FechaSistema;
		public Long             p_IdPais;
		public String           p_SimularOperacion;
		public Long				p_IdPortafolio=0L;
		public Long				p_IdCliente=0L;
	}
	
	public static class ValorizarCarteraCuponesSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		
		public ValorizarCarteraCuponesSalida(){
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
		ValorizarCarteraCuponesEntrada in=gson.fromJson(jin.toString() ,ValorizarCarteraCuponesEntrada.class);
		ValorizarCarteraCuponesSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	/**********************
	 * lógica del programa
	 **********************/
	public ValorizarCarteraCuponesSalida logica(Connection con, ValorizarCarteraCuponesEntrada entrada) throws Exception {
		ValorizarCarteraCuponesSalida out = new ValorizarCarteraCuponesSalida();
		out.P_OUT_ERRORESCONTADOR = new Long(0);
		out.P_OUT_MENSAJE = "";
		SQL sql=new SQL();

		List<ObjCarteras> carteras = new ArrayList<ObjCarteras>();
		HashMap<String,Object> paramsCartera = new HashMap<>();
		paramsCartera.put("IdEmpresa",1);
		paramsCartera.put("IdPortafolio", entrada.p_IdPortafolio);
		paramsCartera.put("IdCliente", entrada.p_IdCliente);
		carteras = sql.query(con, SQL_SelectCartera, paramsCartera, ObjCarteras.class);
		
		ValorizarCustodia.ValorizarCustodiaEntrada ValorizarCust = new ValorizarCustodia.ValorizarCustodiaEntrada();
		ValorizarCust.p_IdEmpresa 					= entrada.p_IdEmpresa;
		ValorizarCust.p_IdUsuario					= entrada.p_IdUsuario;
		ValorizarCust.p_IdModulo 					= "PLA";		
		ValorizarCust.p_IdPais 						= entrada.p_IdPais;
		ValorizarCust.p_OrigenProcesamiento 		= "VALORIZACION_CUPONES";
		Long IdValorizacion = 0L;
		
		for (int i = 0; i < carteras.size() ; i++ ){
			ObjCarteras carterasrf = carteras.get(i);
			ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarInstrumentos = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
			ValorizarInstrumentos.FechaOperacion = carterasrf.WS_FechaOperacion;
			ValorizarInstrumentos.IdClasificacionTipoInstrumento = carterasrf.WS_IdClasificacionTipoInstrumento;
			ValorizarInstrumentos.IdTipoInstrumento = carterasrf.WS_IdTipoInstrumento;
			ValorizarInstrumentos.IdInstrumento = carterasrf.WS_IdInstrumento;
			ValorizarInstrumentos.Nemotecnico = carterasrf.WS_Nemotecnico;
			ValorizarInstrumentos.IdTipoReajusteInstrumento = carterasrf.WS_IdTipoReajuste;
			ValorizarInstrumentos.Cantidad = carterasrf.WS_Cantidad;
			ValorizarInstrumentos.ValorRescate = carterasrf.WS_ValorRescate;
			ValorizarInstrumentos.PrecioCompra = carterasrf.WS_PrecioCompra;
			ValorizarInstrumentos.TasaCompra = carterasrf.WS_TasaCompra;
			ValorizarInstrumentos.PrecioSubyacente = carterasrf.WS_PrecioSubyacente;
			ValorizarInstrumentos.TasaSubyacente = carterasrf.WS_TasaSubyacente;
			ValorizarInstrumentos.PrecioGarantia = carterasrf.WS_PrecioGarantia;
			ValorizarInstrumentos.TasaGarantia = carterasrf.WS_TasaGarantia;
			ValorizarInstrumentos.TieneCorteDeCupon = carterasrf.WS_TieneCorteDeCupon;
			ValorizarInstrumentos.IdCliente = carterasrf.WS_IdCliente;
			ValorizarInstrumentos.IdPortafolio = carterasrf.WS_IdPortafolio;
			ValorizarInstrumentos.TablaOrigen = carterasrf.TablaOrigen;		
			ValorizarInstrumentos.IdCustodia = carterasrf.IdTablaOrigen;
			
			ValorizarInstrumentos.IdMonedaInstrumento = carterasrf.WS_IdMonedaInstrumento;
			ValorizarInstrumentos.EsRegistroNuevo = "N";
			ValorizarInstrumentos.FechaVencimientoOperacion = carterasrf.WS_FechaVencimiento;
			ValorizarInstrumentos.FechaVencimientoInstrumento = carterasrf.WS_FechaVencimiento;
			
			ValorizarInstrumentos.IdMonedaEmisionInstrumento= carterasrf.WS_IdMonedaEmision;
			
			
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
		 System.out.println("== COMIENZA ==");
       try (Connection con = PlataformaUtils.getDESAConnection()){
			
			ValorizarCarteraCuponesEntrada entrada = new ValorizarCarteraCuponesEntrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 0L;
			entrada.p_FechaSistema = "2018-07-25";
			entrada.p_IdPais = 44L;
			entrada.p_SimularOperacion = "N";
			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        ValorizarCarteraCupones demo=new ValorizarCarteraCupones();		
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
