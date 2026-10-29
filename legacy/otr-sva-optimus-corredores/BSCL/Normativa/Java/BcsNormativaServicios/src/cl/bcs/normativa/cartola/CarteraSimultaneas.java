package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cl.bcs.normativa.cartola.BcsCartola.Dato;
import cl.bcs.normativa.cartola.CarteraRV.Movimiento;
import cl.bcs.normativa.cartola.ResumenInversionBase.Portafolio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CarteraSimultaneas implements BcsCartola{
	
	public final Log log = new Log("NOR.CarteraSimultaneas");
	
	private final String SQL_SELECT_SIMULTANEA = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraSimultaneas_SelectMovimiento.sql");
	private final String SQL_SELECT_SIMULTANEA_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraSimultaneas_SelectMovimientoHistorico.sql");
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");

	public static class Simultanea{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdCartera;
		public Long FolioCartera;
		public String IdTipoOperacion;
		public String NombreOperacion;
		public String AccionAlContado;
		public String AccionAPlazo;
		public String TipoPosicion;
		public Long FolioComprobante;
		public Long IdSubyacente;
		public String FechaOperacion;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String RazonSocial;
		public BigDecimal Cantidad;
		public BigDecimal PrecioContado;
		public BigDecimal Tasa;
		public BigDecimal MontoATasaCompraActual;
		public BigDecimal MontoCompraActualMonedaInstrumento;
		public BigDecimal MontoCompraActualMonedaAdicional;
		public BigDecimal PrecioMercado;
		public BigDecimal TasaMercado;
		public BigDecimal MontoMercadoMonedaSistema;
		public BigDecimal MontoMercadoMonedaInstrumento;
		public BigDecimal MontoMercadoMonedaAdicional;
		public String FechaVencimientoOperacion;
		public Long PlazoEnDiasOperacion;
		public BigDecimal PrecioAPlazo;
		public BigDecimal MontoAPlazoMonedaSistema;
		public BigDecimal MontoAPlazoMonedaInstrumento;
		public BigDecimal MontoAPlazoMonedaAdicional;
		public BigDecimal MontoCompra;
		public BigDecimal MontoCompraMonedaAdicional;
		public String IdMonedaInstrumento;
		public Date FechaOperacionSimultanea;
		
		public Long IdRegistro = 0L;
		
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCarteraSimultaneas{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdCartera;
		public Long FolioCartera;
		public String IdTipoOperacion;
		public String NombreOperacion;
		public String AccionAlContado;
		public String AccionAPlazo;
		public String TipoPosicion;
		public Long FolioComprobante;
		public Long IdSubyacente;
		public String FechaOperacion;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String RazonSocial;
		public BigDecimal Cantidad;
		public BigDecimal PrecioContado;
		public BigDecimal Tasa;
		public BigDecimal MontoATasaCompraActual;
		public BigDecimal MontoCompraActualMonedaInstrumento;
		public BigDecimal MontoCompraActualMonedaAdicional;
		public BigDecimal PrecioMercado;
		public BigDecimal TasaMercado;
		public BigDecimal MontoMercadoMonedaSistema;
		public BigDecimal MontoMercadoMonedaInstrumento;
		public BigDecimal MontoMercadoMonedaAdicional;
		public String FechaVencimientoOperacion;
		public Long PlazoEnDiasOperacion;
		public BigDecimal PrecioAPlazo;
		public BigDecimal MontoAPlazoMonedaSistema;
		public BigDecimal MontoAPlazoMonedaInstrumento;
		public BigDecimal MontoAPlazoMonedaAdicional;
		public String Moneda;
		public BigDecimal MontoAContado;
		public BigDecimal MontoMercado;
		public BigDecimal MontoAPlazo;
		public BigDecimal Rentabilidad;
		public Long CantidadCompraTotalAPlazo;
		public BigDecimal MontoCompraTotalAPlazo;
		public Long CantidadVentaTotalAPlazo;
		public BigDecimal MontoVentaTotalAPlazo;
		public BigDecimal Amortizado;
		public BigDecimal AmortizadoTotCompra;
		public BigDecimal AmortizadoTotVenta;
		public BigDecimal montoMercCompra;
		public BigDecimal montoMercVenta;
		public BigDecimal resultado;
		public BigDecimal resultadoCompra;
		public BigDecimal resultadoVenta;
		
		public String TIPO_OPERAC = "C";
		
		public SalidaCarteraSimultaneas(){
			this.CantidadCompraTotalAPlazo = 0L;
			this.MontoCompraTotalAPlazo = BigDecimal.ZERO;
			this.CantidadVentaTotalAPlazo = 0L;
			this.MontoVentaTotalAPlazo = BigDecimal.ZERO;
			this.Amortizado = BigDecimal.ZERO;
			this.AmortizadoTotCompra = BigDecimal.ZERO;
			this.AmortizadoTotVenta = BigDecimal.ZERO;
			this.montoMercCompra = BigDecimal.ZERO;
			this.montoMercVenta = BigDecimal.ZERO;
			this.resultadoCompra = BigDecimal.ZERO;
			this.resultadoVenta = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SimultaneaTotalizada{
		public Long CantidadCompraTotalAPlazo;
		public BigDecimal MontoCompraTotalAPlazo;
		public Long CantidadVentaTotalAPlazo;
		public BigDecimal MontoVentaTotalAPlazo;
		public BigDecimal AmortizadoTotCompra;
		public BigDecimal AmortizadoTotVenta;
		public BigDecimal montoMercCompra;
		public BigDecimal montoMercVenta;
		public BigDecimal resultadoCompra;
		public BigDecimal resultadoVenta;
		
		public SimultaneaTotalizada(){
			this.CantidadCompraTotalAPlazo = 0L;
			this.MontoCompraTotalAPlazo = BigDecimal.ZERO;
			this.CantidadVentaTotalAPlazo = 0L;
			this.MontoVentaTotalAPlazo = BigDecimal.ZERO;
			this.AmortizadoTotCompra = BigDecimal.ZERO;
			this.AmortizadoTotVenta = BigDecimal.ZERO;
			this.montoMercCompra = BigDecimal.ZERO;
			this.montoMercVenta = BigDecimal.ZERO;
			this.resultadoCompra = BigDecimal.ZERO;
			this.resultadoVenta = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		log.write("INICIO::CarteraSimultaneas");
		
		SQL sql = new SQL();
		SalidaCartola out = new SalidaCartola();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String monedaAdicional = null;
		
		//El Metodo arroja error cuando no encuentra parametro
		try{
			monedaAdicional = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaAdicional");
		}catch(Exception e){
			log.write(e);
		}
		
		if(monedaAdicional == null || monedaAdicional.isEmpty()){
			monedaAdicional = "USD"; //Cuando no existe moneda es USD
		}
		
		List<Long> portafolios = new ArrayList<Long>();
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		List<Portafolio> portObj = new ArrayList<>();
		for(Dato dato : entrada.p_Datos){
			portafolios.add(dato.IdPortafolio);
			Portafolio por = new Portafolio();
			por.IdTrabajo = idTrabajo;
			por.IdPortafolio = dato.IdPortafolio;
			portObj.add(por);
		}
		sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portObj, Portafolio.class);

		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		
		List<Simultanea> simultaneas = new ArrayList<Simultanea>();
		
		if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
			simultaneas = sql.query(con, SQL_SELECT_SIMULTANEA, params, Simultanea.class);
		}else{
			params.put("FechaHasta", entrada.p_FechaHasta);
			simultaneas = sql.query(con, SQL_SELECT_SIMULTANEA_HISTORICO, params, Simultanea.class);
		}
		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));

		List<SalidaCarteraSimultaneas> salidas = new ArrayList<SalidaCarteraSimultaneas>();
		HashMap<Long,SimultaneaTotalizada> hTotalSimultaneasPorPortafolio = new HashMap<Long,SimultaneaTotalizada>();
		
		
		//OBTIENE VECTOR PRECIOS
		EntregarPreciosTasas_Entrada entregarPreciosTasasEntrada = new EntregarPreciosTasas_Entrada();
		
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		entregarPreciosTasasEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		entregarPreciosTasasEntrada.p_IdModulo = entrada.p_IdModulo;
		entregarPreciosTasasEntrada.p_IdPais = idPaisEmpresa;		
		entregarPreciosTasasEntrada.p_IdUsuario = entrada.p_IdUsuario;
		
		Long i = 1L;
		for (Simultanea simultanea : simultaneas) {
			EntregarPreciosTasas_Entrada_DatosInstrumentos entradaDatosInstrumentos = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
			simultanea.IdRegistro = i;
			entradaDatosInstrumentos.IdRegistro = simultanea.IdRegistro;
			entradaDatosInstrumentos.TipoCartera = "0";
			entradaDatosInstrumentos.IdUso = "CARTERA";
			entradaDatosInstrumentos.IdTipoCriterioTasa = "0";
			entradaDatosInstrumentos.IdClasificacionTipoInstrumento = "RV";
			entradaDatosInstrumentos.IdTipoInstrumento = simultanea.IdTipoInstrumento==null || simultanea.IdTipoInstrumento=="" ? "0" : simultanea.IdTipoInstrumento;
			entradaDatosInstrumentos.IdInstrumento = simultanea.IdInstrumento;
			entradaDatosInstrumentos.Nemotecnico = simultanea.Nemotecnico;
			entradaDatosInstrumentos.IdMoneda = simultanea.IdMonedaInstrumento;
			entradaDatosInstrumentos.FechaConsulta = entrada.p_FechaHasta;  
			entradaDatosInstrumentos.PlazoResidual = 0L;
			entregarPreciosTasasEntrada.p_DatosInstrumentos.add(entradaDatosInstrumentos);
			i++;
		}
		
		HashMap<String, Object> precios = new HashMap<String, Object>();
		if (entregarPreciosTasasEntrada.p_DatosInstrumentos.size() > 0) {
			EntregarPreciosTasas entregarPreciosTasasService = new EntregarPreciosTasas();
			EntregarPreciosTasas_Salida entregarPreciosTasasSalida = entregarPreciosTasasService.logica(con, entregarPreciosTasasEntrada);
			precios = Utils.List2Hash(entregarPreciosTasasSalida.P_OUT_PRECIOSTASAS,"IdRegistro");
		}
		//OBTIENE VECTOR PRECIOS
		
		for(Simultanea simultanea : simultaneas){
			SimultaneaTotalizada totalizado =  hTotalSimultaneasPorPortafolio.get(simultanea.IdPortafolio);
			if(totalizado == null){
				totalizado = new SimultaneaTotalizada();
			}
			SalidaCarteraSimultaneas salidaCarteraSimultaneas = new SalidaCarteraSimultaneas();
			salidaCarteraSimultaneas = Utils.copyPojo(simultanea, salidaCarteraSimultaneas);
			
			//Calcula monto mercado
			EntregarPreciosTasas_Salida_PreciosTasas registroPrecio = (EntregarPreciosTasas_Salida_PreciosTasas) precios.get(simultanea.IdRegistro);
			if (registroPrecio != null && registroPrecio.Precio != null /*&& registroPrecio.Precio.doubleValue() > 0*/) {
				simultanea.MontoMercadoMonedaSistema = simultanea.Cantidad.multiply(registroPrecio.Precio);
			}
			
			long plazo =  (entrada.p_FechaHasta).getTime() - simultanea.FechaOperacionSimultanea.getTime();
			long dias = plazo / (1000 * 60 * 60 * 24);
			
			if(monedaSistema.equals(simultanea.IdMonedaInstrumento)){
				salidaCarteraSimultaneas.Moneda = monedaSistema;
				salidaCarteraSimultaneas.MontoAContado = MonedaFormato.formatear(con, simultanea.PrecioContado.multiply(simultanea.Cantidad, MathContext.DECIMAL128), MonedaFormato.MONTO, simultanea.IdMonedaInstrumento).valor;
				salidaCarteraSimultaneas.MontoMercado = simultanea.MontoMercadoMonedaSistema;
				salidaCarteraSimultaneas.MontoAPlazo = simultanea.MontoAPlazoMonedaSistema;
				salidaCarteraSimultaneas.Amortizado = salidaCarteraSimultaneas.MontoAContado.multiply((BigDecimal.ONE).add((simultanea.Tasa.divide(new BigDecimal(100))).multiply(new BigDecimal(dias)).divide(new BigDecimal(30), MathContext.DECIMAL128)));
						
				
			}else{
				salidaCarteraSimultaneas.Moneda = monedaAdicional;
				salidaCarteraSimultaneas.MontoAContado = simultanea.MontoCompraMonedaAdicional;
				salidaCarteraSimultaneas.MontoMercado = simultanea.MontoMercadoMonedaAdicional;
				salidaCarteraSimultaneas.MontoAPlazo = simultanea.MontoAPlazoMonedaAdicional;
				salidaCarteraSimultaneas.Amortizado = simultanea.MontoCompraMonedaAdicional.multiply((BigDecimal.ONE).add((simultanea.Tasa.divide(new BigDecimal(100))).multiply(new BigDecimal(dias)).divide(new BigDecimal(30), MathContext.DECIMAL128)));
			}
			
			if(simultanea.MontoMercadoMonedaSistema.compareTo(BigDecimal.ZERO) != 0){
				salidaCarteraSimultaneas.Rentabilidad = ((salidaCarteraSimultaneas.MontoMercado.divide(salidaCarteraSimultaneas.Amortizado, MathContext.DECIMAL128)).subtract(new BigDecimal(1), MathContext.DECIMAL128)).multiply(new BigDecimal(100), MathContext.DECIMAL128);
			}
			
			salidaCarteraSimultaneas.resultado = salidaCarteraSimultaneas.MontoMercado.subtract(salidaCarteraSimultaneas.Amortizado, MathContext.DECIMAL128);
			
			if("COMPRA".equals(simultanea.AccionAPlazo)){
				totalizado.CantidadCompraTotalAPlazo++;
				totalizado.MontoCompraTotalAPlazo = totalizado.MontoCompraTotalAPlazo.add(salidaCarteraSimultaneas.MontoAPlazo, MathContext.DECIMAL128);
				totalizado.AmortizadoTotCompra = totalizado.AmortizadoTotCompra.add(salidaCarteraSimultaneas.Amortizado, MathContext.DECIMAL128);
				totalizado.montoMercCompra = totalizado.montoMercCompra.add(salidaCarteraSimultaneas.MontoMercado, MathContext.DECIMAL128);
				totalizado.resultadoCompra = totalizado.resultadoCompra.add(salidaCarteraSimultaneas.resultado, MathContext.DECIMAL128);
			}
			if("VENTA".equals(simultanea.AccionAPlazo)){
				totalizado.CantidadVentaTotalAPlazo++;
				totalizado.MontoVentaTotalAPlazo = totalizado.MontoVentaTotalAPlazo.add(salidaCarteraSimultaneas.MontoAPlazo, MathContext.DECIMAL128);
				totalizado.AmortizadoTotVenta =totalizado.AmortizadoTotVenta =totalizado.AmortizadoTotVenta.add(salidaCarteraSimultaneas.Amortizado, MathContext.DECIMAL128);
				totalizado.montoMercVenta = totalizado.montoMercVenta.add(salidaCarteraSimultaneas.MontoMercado, MathContext.DECIMAL128);
				totalizado.resultadoVenta = totalizado.resultadoVenta.add(salidaCarteraSimultaneas.resultado, MathContext.DECIMAL128);
			}
			hTotalSimultaneasPorPortafolio.put(simultanea.IdPortafolio, totalizado);
			salidas.add(salidaCarteraSimultaneas);
		}
		
		for(SalidaCarteraSimultaneas salidaCarteraSimultaneas : salidas){
			SimultaneaTotalizada totalizado =  hTotalSimultaneasPorPortafolio.get(salidaCarteraSimultaneas.IdPortafolio);
			if(totalizado != null){
				salidaCarteraSimultaneas.CantidadCompraTotalAPlazo = totalizado.CantidadCompraTotalAPlazo;
				salidaCarteraSimultaneas.MontoCompraTotalAPlazo = totalizado.MontoCompraTotalAPlazo;
				salidaCarteraSimultaneas.CantidadVentaTotalAPlazo = totalizado.CantidadVentaTotalAPlazo;
				salidaCarteraSimultaneas.MontoVentaTotalAPlazo = totalizado.MontoVentaTotalAPlazo;
				salidaCarteraSimultaneas.AmortizadoTotVenta = totalizado.AmortizadoTotVenta;
				salidaCarteraSimultaneas.AmortizadoTotCompra = totalizado.AmortizadoTotCompra;
				salidaCarteraSimultaneas.montoMercCompra = totalizado.montoMercCompra;
				salidaCarteraSimultaneas.montoMercVenta = totalizado.montoMercVenta;
				salidaCarteraSimultaneas.resultadoCompra = totalizado.resultadoCompra;
				salidaCarteraSimultaneas.resultadoVenta = totalizado.resultadoVenta;
			}
			
		}
		
		List<HashMap<String,Object>> resultadoCarteraSimultaneas = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("carteraSimultaneas", resultadoCarteraSimultaneas);
		
		log.write("FIN::CarteraSimultaneas");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getQAConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2018-09-01");
		Objin.p_FechaHasta = Fecha.toDate("2018-09-13");
		Dato dato = new Dato();
		dato.IdCliente = 100350L;
		dato.IdPortafolio = 100586L;
		Objin.p_Datos.add(dato);
		CarteraSimultaneas  demo= new CarteraSimultaneas();				
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
	
}
