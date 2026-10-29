package cl.bcs.plataforma.pau;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.ValorizarCustodia;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
//import cl.bcs.custodia.utils.GenerarMovimientoCustodia.ObjMovimientosEnTransito;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValorizarCarteraPrestEmpres implements JavaCall {	

	private final Log log = new Log("ValorizarCarteraPrestEmpres");	
	private final String SQL_SelectCartera = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_CarteraPrestEmpres.sql");
	private final String SQL_SelectPrecios = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_PreciosInstrumentos.sql");
	private final String SQL_UpdateCartera = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_UpdatePrecioCartera.sql");
	private final String SQL_UpdatePrimaPremio = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_UpdatePrimaPremioCartera.sql");
	private final String SQL_RespaldaPrecioCompra = loadResourceToString("cl/bcs/plataforma/pau/sql/PautaNocturna_SEL_RespaldarPrecioCompraInicial.sql");
	
	public static class ObjCarteras {	
		public Long					IdProcesamientoValorizaCustodiaMasiva; 
		public Long					IdTablaOrigen;
		public Long					FolioCartera;
		public Long					FolioCarteraRelacionada;
		public Long 				IdInstrumentoSubyacente;
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
		public Long					PQ_PlazoTotal;
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
		
		public BigDecimal			INGPrecioCompra;
		public BigDecimal			PrecioCompra;
		public BigDecimal			PrecioCompraInicial;
		
		public BigDecimal			INGMontoPrima;
		public BigDecimal			INGMontoPremio;
		public BigDecimal			INGMontoATasaCompraActual; //Monto
		public BigDecimal			INGMontoAPlazo; //Monto a plazo
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ValorizarCarteraPrestEmpresEntrada {
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
	
	public static class ValorizarCarteraPrestEmpresSalida {
		public Long P_OUT_ERRORESCONTADOR;
		public String P_OUT_MENSAJE;
		
		public ValorizarCarteraPrestEmpresSalida(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJE = "";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PrecioInstrumento {
		public Long IdInstrumento;
		public BigDecimal PrecioMedio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		ValorizarCarteraPrestEmpresEntrada in=gson.fromJson(jin.toString() ,ValorizarCarteraPrestEmpresEntrada.class);
		ValorizarCarteraPrestEmpresSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	/**********************
	 * lógica del programa
	 **********************/
	public ValorizarCarteraPrestEmpresSalida logica(Connection con, ValorizarCarteraPrestEmpresEntrada entrada) throws Exception {
		ValorizarCarteraPrestEmpresSalida out = new ValorizarCarteraPrestEmpresSalida();
		out.P_OUT_ERRORESCONTADOR = new Long(0);
		out.P_OUT_MENSAJE = "";
		
		Long idPaisEmpresa = obtenerIdPais(con, entrada.p_IdEmpresa);
		String fechaAnterior = obtenerFechaAnterior(con, entrada.p_FechaSistema, idPaisEmpresa);
		List<ObjCarteras> carteras = obtenerCarterasPrestamosEmprestitos(con, entrada);
		List<ObjCarteras> prestamos = obtenerPrestamosDiaCero(con, fechaAnterior, carteras);
		Map<Long,ObjCarteras> mapPrestamos = listCarterasToMap(prestamos);
		List<ObjCarteras> emprestitos = obtenerEmprestitosDiaCero(con, carteras, mapPrestamos);
		List<ObjCarteras> carterasActualizar = prestamos;
		carterasActualizar.addAll(emprestitos);
		Map<Long,PrecioInstrumento> precios = obtenerPrecios(con,prestamos,fechaAnterior);
		respaldarPrecioCompra(con, carterasActualizar, carteras);
		actualizarPrecioCompra(con, carterasActualizar, precios, entrada, carteras);
		
		carteras = obtenerCarterasPrestamosEmprestitos(con, entrada);
		
		ValorizarCustodia.ValorizarCustodiaEntrada ValorizarCust = new ValorizarCustodia.ValorizarCustodiaEntrada();
		ValorizarCust.p_IdEmpresa 					= entrada.p_IdEmpresa;
		ValorizarCust.p_IdUsuario					= entrada.p_IdUsuario;
		ValorizarCust.p_IdModulo 					= "PLA";		
		ValorizarCust.p_IdPais 						= entrada.p_IdPais;
		ValorizarCust.p_OrigenProcesamiento 		= "VALORIZACION_CARTERAPRESTEMPREST";
		ValorizarCust.p_EsPauta						= entrada.p_EsPauta;
		
		for (int i = 0; i < carteras.size() ; i++ ){
			ObjCarteras carteraspremp = carteras.get(i);
			ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada ValorizarInstrumentos = new ValorizarCustodia.ValorizarCustodiaEntrada.ObjDatoEntrada();
			ValorizarInstrumentos.FechaOperacion = carteraspremp.PQ_FechaOperacion;
			ValorizarInstrumentos.IdClasificacionTipoInstrumento = carteraspremp.PQ_IdClasificacionTipoInstrumento;
			ValorizarInstrumentos.IdTipoInstrumento = !"RV".equalsIgnoreCase(carteraspremp.PQ_IdClasificacionTipoInstrumento) ? carteraspremp.WS_IdTipoInstrumento : carteraspremp.PQ_IdTipoInstrumento;
			ValorizarInstrumentos.IdInstrumento = carteraspremp.PQ_IdInstrumento;
			ValorizarInstrumentos.Nemotecnico = carteraspremp.PQ_Nemotecnico;
			ValorizarInstrumentos.IdTipoReajusteInstrumento = carteraspremp.PQ_IdMoneda; //WS_IdTipoReajuste;
			ValorizarInstrumentos.Cantidad = carteraspremp.PQ_Cantidad;
			ValorizarInstrumentos.ValorRescate = null; //carterassim.WS_ValorRescate;
			ValorizarInstrumentos.PrecioCompra = carteraspremp.PQ_Precio; //carterassim.WS_PrecioCompra;
			ValorizarInstrumentos.TasaCompra = carteraspremp.WS_TasaCompra;
			ValorizarInstrumentos.PrecioSubyacente = carteraspremp.WS_PrecioSubyacente;
			ValorizarInstrumentos.TasaSubyacente = carteraspremp.WS_TasaSubyacente;
			ValorizarInstrumentos.PrecioGarantia = carteraspremp.WS_PrecioGarantia;
			ValorizarInstrumentos.TasaGarantia = carteraspremp.WS_TasaGarantia;
			ValorizarInstrumentos.TieneCorteDeCupon = carteraspremp.WS_TieneCorteDeCupon;
			ValorizarInstrumentos.IdCliente = carteraspremp.PQ_IdCliente;
			ValorizarInstrumentos.IdPortafolio = carteraspremp.PQ_IdPortafolio;
			ValorizarInstrumentos.TablaOrigen = carteraspremp.TablaOrigen;		
			ValorizarInstrumentos.IdCustodia = carteraspremp.IdTablaOrigen;
			
			ValorizarInstrumentos.EsRegistroNuevo = "N";
			ValorizarInstrumentos.FechaVencimientoInstrumento = carteraspremp.WS_FechaVencimiento;
			ValorizarInstrumentos.PlazoEnDiasOperacion = carteraspremp.PQ_Plazo.longValue();
			ValorizarInstrumentos.FechaVencimientoOperacion = carteraspremp.PQ_FechaVencimientoOperacion;
			ValorizarInstrumentos.MontoCompraMonedaSistema = carteraspremp.WS_BK_INGMontoATasaCompraActual;
			ValorizarInstrumentos.MontoCompra = carteraspremp.WS_BK_INGMontoCompraActualMonedaInstrumento;
			ValorizarInstrumentos.IdMonedaInstrumento = carteraspremp.PQ_IdMoneda; //WS_IdTipoReajuste;
			ValorizarInstrumentos.OrigenProcesamiento = "VALORIZACION_CARTERAPRESTEMPREST";			
			
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
	
	private Long obtenerIdPais(Connection con, Long idEmpresa) throws Exception {
		log.write("INICIO::ValorizarCarteraPrestEmpres.obtenerIdPais");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, idEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		log.write("FIN::ValorizarCarteraPrestEmpres.obtenerIdPais");
		return idPaisEmpresa;
	}
	
	private String obtenerFechaAnterior(Connection con, String fechaSistema, Long idPais) throws Exception {
		log.write("INICIO::ValorizarCarteraPrestEmpres.obtenerFechaAnterior");
		String fecha = "0001-01-01";
		InObtenerDiaAnterior inObtenerDiaAnterior = new InObtenerDiaAnterior();
		inObtenerDiaAnterior.idPais = idPais;
		inObtenerDiaAnterior.diaHabil = "S";
		inObtenerDiaAnterior.soloCaeDiaHabil = "S";
		
		InObtenerDiaAnterior.FechasIn fechaIn = new InObtenerDiaAnterior.FechasIn();
		fechaIn.numeroDias = 1L;
		fechaIn.fechaInicial = fechaSistema;
		inObtenerDiaAnterior.fechas.add(fechaIn);
		
		ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();
		
		OutObtenerDiaAnterior outObtenerDiaAnterior = obtenerDiaAnterior.logica(con, inObtenerDiaAnterior);
		if(outObtenerDiaAnterior.fechas.size() > 0) {
			fecha = outObtenerDiaAnterior.fechas.get(0).fechaCalculada;
		}
		log.write("FIN::ValorizarCarteraPrestEmpres.obtenerFechaAnterior");
		return fecha;
		
	}
	
	private List<ObjCarteras> obtenerCarterasPrestamosEmprestitos(Connection con, ValorizarCarteraPrestEmpresEntrada entrada) throws Exception{
		log.write("INICIO::ValorizarCarteraPrestEmpres.obtenerCarterasPrestamosEmprestitos");
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("IdEmpresa",entrada.p_IdEmpresa);
		params.put("IdPortafolio", entrada.p_IdPortafolio);
		params.put("FechaValorizacion", entrada.p_FechaSistema);
		params.put("IdCliente", entrada.p_IdCliente);
		List<ObjCarteras> carteras = sql.query(con, SQL_SelectCartera, params, ObjCarteras.class);
		log.write("FIN::ValorizarCarteraPrestEmpres.obtenerCarterasPrestamosEmprestitos");
		return carteras;
	}
	
	private List<ObjCarteras> obtenerPrestamosDiaCero(Connection con, String fechaAnterior, List<ObjCarteras> carteras){
		log.write("INICIO::ValorizarCarteraPrestEmpres.obtenerPrestamosDiaCero");
		List<ObjCarteras> prestamosDiaCero = carteras.parallelStream().filter(cartera->"PRT".equals(cartera.PQ_IdTipoInstrumento) && fechaAnterior.equals(Fecha.stringToSqlDate(cartera.PQ_FechaOperacion))).collect(Collectors.toList());
		log.write("FIN::ValorizarCarteraPrestEmpres.obtenerPrestamosDiaCero");
		return prestamosDiaCero;
	}
	
	private Map<Long,PrecioInstrumento> obtenerPrecios(Connection con, List<ObjCarteras> carteras, String fecha) throws Exception{
		log.write("INICIO::ValorizarCarteraPrestEmpres.obtenerPrecios");
		SQL sql=new SQL();
		List<Long> idInstrumentos = carteras.parallelStream().map(cartera->cartera.IdInstrumentoSubyacente).distinct().collect(Collectors.toList());
		HashMap<String,Object> params = new HashMap<>();
		params.put("Fecha", fecha);
		params.put("IdInstrumento", idInstrumentos);
		Map<Long,PrecioInstrumento> precios = sql.query(con, SQL_SelectPrecios, params, PrecioInstrumento.class).parallelStream().collect(Collectors.toMap(precio->precio.IdInstrumento, precio->precio));
		log.write("FIN::ValorizarCarteraPrestEmpres.obtenerPrecios");
		return precios;
	}
	
	private Map<Long,ObjCarteras> listCarterasToMap(List<ObjCarteras> carteras){
		log.write("INICIO::ValorizarCarteraPrestEmpres.listCarterasToMap");
		Map<Long,ObjCarteras> mapCarteras = carteras.parallelStream().collect(Collectors.toMap(cartera->cartera.FolioCartera, cartera->cartera));
		log.write("FIN::ValorizarCarteraPrestEmpres.listCarterasToMap");
		return mapCarteras;
	}
	
	private List<ObjCarteras> obtenerEmprestitosDiaCero(Connection con, List<ObjCarteras> carteras, Map<Long,ObjCarteras> mapPrestamos){
		log.write("INICIO::ValorizarCarteraPrestEmpres.obtenerEmprestitosDiaCero");
		List<ObjCarteras> emprestitos = carteras.parallelStream().filter(cartera->"EMP".equals(cartera.PQ_IdTipoInstrumento) && mapPrestamos.get(cartera.FolioCarteraRelacionada) != null).collect(Collectors.toList());
		log.write("FIN::ValorizarCarteraPrestEmpres.obtenerEmprestitosDiaCero");
		return emprestitos;
	}
		
	private void actualizarPrecioCompra(Connection con, List<ObjCarteras> carterasActualizar, Map<Long,PrecioInstrumento> precios, ValorizarCarteraPrestEmpresEntrada entrada, List<ObjCarteras> carteras) throws Exception {
		log.write("INICIO::ValorizarCarteraPrestEmpres.actualizarPrecioCompra");
		List<ObjCarteras> carterasFinales = carterasActualizar.parallelStream()
				.filter(cartera->precios.get(cartera.IdInstrumentoSubyacente) != null && precios.get(cartera.IdInstrumentoSubyacente).PrecioMedio.compareTo(BigDecimal.ZERO) != 0)
				.map(cartera->{					
					PrecioInstrumento precio = precios.get(cartera.IdInstrumentoSubyacente);
					cartera.INGPrecioCompra = precio.PrecioMedio;
					cartera.PrecioCompra = precio.PrecioMedio;
					return cartera;
				}).collect(Collectors.toList());
		SQL sql=new SQL();
		sql.batch(con, SQL_UpdateCartera, carterasFinales, ObjCarteras.class);
		
		actualizarPrimaPremio(con, carterasActualizar, entrada);
		
		log.write("FIN::ValorizarCarteraPrestEmpres.actualizarPrecioCompra");
	}
	
	private void actualizarPrimaPremio(Connection con, List<ObjCarteras> carterasActualizar, ValorizarCarteraPrestEmpresEntrada entrada) throws Exception {
		log.write("INICIO::ValorizarCarteraPrestEmpres.actualizarPrimaPremio");
		SQL sql=new SQL();
		
		// FILTRAMOS LAS CARTERAS QUE NO CUMPLEN CON LOS DATOS PARA RE-VALORIZAR
		carterasActualizar.removeIf(cartera -> 
		    cartera.PQ_Cantidad.compareTo(BigDecimal.ZERO) <= 0 ||
		    cartera.INGPrecioCompra.compareTo(BigDecimal.ZERO) <= 0 ||
		    cartera.PQ_Tasa.compareTo(BigDecimal.ZERO) <= 0 ||
		    cartera.PQ_PlazoTotal <= 0
		);
		
		carterasActualizar.forEach(cartera->{
			if(cartera.PQ_Cantidad.compareTo(BigDecimal.ZERO) > 0 && cartera.INGPrecioCompra.compareTo(BigDecimal.ZERO) > 0 && cartera.PQ_Tasa.compareTo(BigDecimal.ZERO) > 0 && cartera.PQ_PlazoTotal > 0) {
				
				//Monto:
				cartera.INGMontoATasaCompraActual = cartera.INGPrecioCompra.multiply(cartera.PQ_Cantidad).setScale(4, RoundingMode.HALF_UP);
				
				//Monto a Plazo:
				BigDecimal monto = cartera.INGMontoATasaCompraActual;
				// si IdMoneda == 'CLP' ? 30 : 360
				BigDecimal base = cartera.PQ_IdMoneda.equals("CLP") ? new BigDecimal("30") : new BigDecimal("360");
				BigDecimal uno = new BigDecimal("1");
				BigDecimal tasaNew = cartera.PQ_Tasa;
				BigDecimal tasadiv = tasaNew.divide(new BigDecimal("100"),3, BigDecimal.ROUND_HALF_UP);
				BigDecimal plazodiv = new BigDecimal(cartera.PQ_PlazoTotal).divide(base,10, BigDecimal.ROUND_HALF_UP);
				
				//Number(monto * ( 1 + (tasa/100) * (plazo /30) ))
				cartera.INGMontoAPlazo = monto.multiply(( uno.add(tasadiv.multiply( plazodiv )))).setScale(4, RoundingMode.HALF_UP);
				
				//Prima o Premio
				if(cartera.PQ_IdTipoInstrumento.equals("PRT")) {
					cartera.INGMontoPrima = cartera.INGMontoAPlazo.subtract(cartera.INGMontoATasaCompraActual).setScale(4, RoundingMode.HALF_UP);
				} else {
					cartera.INGMontoPremio = cartera.INGMontoAPlazo.subtract(cartera.INGMontoATasaCompraActual).setScale(4, RoundingMode.HALF_UP);
				}
			}
		});	
		
		sql.batch(con, SQL_UpdatePrimaPremio, carterasActualizar, ObjCarteras.class);
		
		log.write("FIN::ValorizarCarteraPrestEmpres.actualizarPrimaPremio");
	}
	
	private void respaldarPrecioCompra(Connection con, List<ObjCarteras> carterasActualizar, List<ObjCarteras> carterasVigentes) throws Exception {
		log.write("INICIO::ValorizarCarteraPrestEmpres.actualizarPrecioCompra");		
		List<ObjCarteras> carteras = carterasVigentes;
		carterasActualizar.forEach(cartera->{
			if(cartera.PQ_IdTipoInstrumento.equalsIgnoreCase("EMP")) {
				Map<Long,ObjCarteras> mapCarteras = carteras.parallelStream().collect(Collectors.toMap(cart->cart.FolioCartera, cart->cart));
				ObjCarteras carteraRelacionada = mapCarteras.get(cartera.FolioCarteraRelacionada);
				cartera.PrecioCompraInicial = (cartera.PrecioCompraInicial.compareTo(BigDecimal.ZERO) == 0) ? carteraRelacionada.PrecioCompraInicial : cartera.PrecioCompraInicial;
			} else {
				cartera.PrecioCompraInicial = (cartera.PrecioCompraInicial.compareTo(BigDecimal.ZERO) == 0) ? cartera.INGPrecioCompra : cartera.PrecioCompraInicial;
			}
		});		
		SQL sql=new SQL();
		sql.batch(con, SQL_RespaldaPrecioCompra, carterasActualizar, ObjCarteras.class);	
		log.write("FIN::ValorizarCarteraPrestEmpres.actualizarPrecioCompra");
	}
	
	public static void main(String[] args) {
		//Settings by file
		try {
			Connection con = PlataformaUtils.getPropertiesConnection("qa2");
			ValorizarCarteraPrestEmpresEntrada entrada = new ValorizarCarteraPrestEmpresEntrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 97L;
			entrada.p_FechaSistema = "2024-08-20";
			entrada.p_IdPais = 44L;
			entrada.p_SimularOperacion = "N";
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(new Gson().toJson(entrada)).getAsJsonObject();
	        ValorizarCarteraPrestEmpres demo=new ValorizarCarteraPrestEmpres();		
	    	System.out.println(demo.call(con, dataJsonIn).toString());	
	    	con.commit();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}
}
