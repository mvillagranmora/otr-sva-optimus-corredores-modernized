package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.normativa.cartola.BcsCartola.Dato;
import cl.bcs.normativa.cartola.ResumenInversionBase.Portafolio;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ItemDerivadosFWD implements BcsCartola{
	
	public final Log log = new Log("NOR.ItemDerivadosForward");
	
	private final String SQL_SELECT_CARTERA_ACTUAL = loadResourceToString("cl/bcs/normativa/cartola/sql/ItemDerivadosFWD_SelectCarteraActual.sql");
	private final String SQL_SELECT_CARTERA_HISTORICO = loadResourceToString("cl/bcs/normativa/cartola/sql/ItemDerivadosFWD_SelectCarteraHistorico.sql");
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	
	public static class SalidaItemDerivadosForward{
		public Long Id;
		public Long IdCliente;
        public Long IdPortafolio;
        public BigDecimal total;
        public String item;
        public BigDecimal monto;
        public String grupo;
        public BigDecimal compraUSD;
        public BigDecimal ventaUSD;
        public BigDecimal compraEUR;
        public BigDecimal ventaEUR;
        public BigDecimal compraUF;
        public BigDecimal ventaUF;
        public BigDecimal compraValUSD;
        public BigDecimal ventaValUSD;
        public BigDecimal compraValEUR;
        public BigDecimal ventaValEUR;
        public BigDecimal compraValUF;
        public BigDecimal ventaValUF;
        public String Operacion;
        public String moneda;
        public BigDecimal montoVenta;
        public BigDecimal montoCompra;
        public BigDecimal neto;
        public BigDecimal saldo;
        public String monedaEntrega;
        
        public SalidaItemDerivadosForward() {
        	this.montoCompra = BigDecimal.ZERO;
        	this.montoVenta = BigDecimal.ZERO;
        	this.saldo = BigDecimal.ZERO;
        	this.neto = BigDecimal.ZERO;
        }
        
        public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Cartera{
		public Long IdCliente;
		public Long IdPortafolio;
		public String Tipo;
		public String Operacion;
		public String Nemotecnico;
		public String Moneda;
		public BigDecimal MontoVenta;
		public BigDecimal MontoCompra;
		public BigDecimal TipoCambioMercado;
		public BigDecimal TipoCambioCierre;
		public Long Folio;
		public String MonedaRecibe;
		public String MonedaEntrega;
		public String IdTipoMoneda;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		log.write("INICIO::ItemDerivadosFWD");
		SQL sql = new SQL();
		SalidaCartola out = new SalidaCartola();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		

		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		List<Portafolio> portObj = new ArrayList<>();
		List<Long> portafolios = new ArrayList<Long>();
		for(Dato dato : entrada.p_Datos){
			portafolios.add(dato.IdPortafolio);
			
			Portafolio por = new Portafolio();
			por.IdTrabajo = idTrabajo;
			por.IdPortafolio = dato.IdPortafolio;
			portObj.add(por);
			
		}
		
		sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portObj, Portafolio.class);
		
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		
		List<Cartera> carteras = new ArrayList<Cartera>();
		
		if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
			carteras.addAll(sql.query(con, SQL_SELECT_CARTERA_ACTUAL, params, Cartera.class));
		}else {
			params.put("FechaHasta", entrada.p_FechaHasta);
			carteras.addAll(sql.query(con, SQL_SELECT_CARTERA_HISTORICO, params, Cartera.class));
		}
		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));

		HashMap<String,SalidaItemDerivadosForward> hSalidas = new HashMap<String,SalidaItemDerivadosForward>();
		List<Long> idInternos = new ArrayList<Long>();
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdTrabajo = 1L;
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		Long i= 1L;
		for(Cartera cartera : carteras) {
			SalidaItemDerivadosForward salida = hSalidas.get(cartera.IdPortafolio + "_" + cartera.Nemotecnico + "_" + cartera.Moneda);
			if(salida == null) {
				salida = new SalidaItemDerivadosForward();
				salida.IdCliente = cartera.IdCliente;
				salida.IdPortafolio = cartera.IdPortafolio;
				salida.Operacion = cartera.Operacion;
				salida.moneda = cartera.Moneda;
				salida.monedaEntrega = cartera.MonedaEntrega;
				salida.Id = i;
				i++;
			}
			salida.montoCompra = salida.montoCompra.add(cartera.MontoCompra, MathContext.DECIMAL128);
			salida.montoVenta = salida.montoVenta.add(cartera.MontoVenta, MathContext.DECIMAL128);
			salida.neto =  salida.montoCompra.subtract(salida.montoVenta, MathContext.DECIMAL128);
			if (!idInternos.contains(cartera.Folio.longValue())){
				CalculoEntrada calculoEntradaResultado = new CalculoEntrada();
				calculoEntradaResultado.IdTrabajo = 0L;
				calculoEntradaResultado.IdInterno = cartera.Folio;
				calculoEntradaResultado.Fecha = fechaSistema;
				if("ARBITR".equals(cartera.Nemotecnico) && "DEB".equals(cartera.IdTipoMoneda)) {
					calculoEntradaResultado.IdMonedaOrigen = cartera.MonedaEntrega;
					calculoEntradaResultado.IdMonedaDestino = cartera.MonedaRecibe;
				}else {
					calculoEntradaResultado.IdMonedaOrigen = cartera.MonedaRecibe;
					calculoEntradaResultado.IdMonedaDestino = cartera.MonedaEntrega;
				}
				calculoEntradaResultado.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntradaResultado.MontoOrigen = BigDecimal.ONE;
				calculoEntradaResultado.IdUso = "";
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaResultado);
				idInternos.add(cartera.Folio);
			}
			hSalidas.put(cartera.IdPortafolio + "_" + cartera.Nemotecnico + "_" + cartera.Moneda, salida);
		}
		CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
		HashMap<Long,Object> hCalcularParidadSalida = Utils.List2Hash(calcularParidadSalida.p_out_CalculosSalida, "IdInterno");
		
		for(Cartera cartera : carteras) {
			SalidaItemDerivadosForward salida = hSalidas.get(cartera.IdPortafolio + "_" + cartera.Nemotecnico + "_" + cartera.Moneda);
			CalculoSalida calculoSalidaResultado = (CalculoSalida) hCalcularParidadSalida.get(cartera.Folio);
			if(calculoSalidaResultado != null && "OK".equals(calculoSalidaResultado.Resultado)){
				BigDecimal resultadoNeto = BigDecimal.ZERO;
				if("ARBITR".equals(cartera.Nemotecnico) && "DEB".equals(cartera.IdTipoMoneda)) {
					resultadoNeto = "C".equals(cartera.Tipo) ? cartera.MontoCompra.divide(calculoSalidaResultado.MontoDestino.subtract(cartera.TipoCambioCierre, MathContext.DECIMAL128), MathContext.DECIMAL128) : cartera.MontoVenta.divide(calculoSalidaResultado.MontoDestino.subtract(cartera.TipoCambioCierre, MathContext.DECIMAL128), MathContext.DECIMAL128).negate();
				}else {
					resultadoNeto = "C".equals(cartera.Tipo) ? calculoSalidaResultado.MontoDestino.subtract(cartera.TipoCambioCierre, MathContext.DECIMAL128).multiply(cartera.MontoCompra, MathContext.DECIMAL128) : calculoSalidaResultado.MontoDestino.subtract(cartera.TipoCambioCierre, MathContext.DECIMAL128).multiply(cartera.MontoVenta, MathContext.DECIMAL128).negate();
				}
				salida.saldo = salida.saldo.add(resultadoNeto);
			}
			hSalidas.put(cartera.IdPortafolio + "_" + cartera.Nemotecnico + "_" + cartera.Moneda, salida);
		}
		
		List<SalidaItemDerivadosForward> salidas = new ArrayList<SalidaItemDerivadosForward>();
		
		//Pasar todo a moneda sistema
		calcularParidadEntrada.p_CalculosEntrada.clear();
		for(Map.Entry<String, SalidaItemDerivadosForward> obj : hSalidas.entrySet()) {
			SalidaItemDerivadosForward salida = obj.getValue();
			CalculoEntrada calculoEntradaResultado = new CalculoEntrada();
			calculoEntradaResultado.IdTrabajo = 0L;
			calculoEntradaResultado.IdInterno = salida.Id;
			calculoEntradaResultado.Fecha = fechaSistema;
			calculoEntradaResultado.IdMonedaOrigen = salida.monedaEntrega;
			calculoEntradaResultado.IdMonedaDestino = monedaSistema;
			calculoEntradaResultado.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntradaResultado.MontoOrigen = salida.saldo;
			calculoEntradaResultado.IdUso = "";
			calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaResultado);
			salidas.add(salida);
		}
		calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
		hCalcularParidadSalida = Utils.List2Hash(calcularParidadSalida.p_out_CalculosSalida, "IdInterno");
		
		for(SalidaItemDerivadosForward salida : salidas) {
			CalculoSalida calculoSalidaResultado = (CalculoSalida) hCalcularParidadSalida.get(salida.Id);
			if(calculoSalidaResultado != null && "OK".equals(calculoSalidaResultado.Resultado)){
				salida.saldo = calculoSalidaResultado.MontoDestino;
			}
		}
		
		List<HashMap<String,Object>> resultadoItemDerivadosForward = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("itemDerivadosFWD", resultadoItemDerivadosForward);
		
		log.write("FIN::ItemDerivadosFWD");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getMBConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2018-12-01");
		Objin.p_FechaHasta = Fecha.toDate("2018-12-26");
		Dato dato = new Dato();
		dato.IdCliente = 18145L;
		dato.IdPortafolio = 24144L;
		Objin.p_Datos.add(dato);
		ItemDerivadosFWD demo= new ItemDerivadosFWD();				
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
