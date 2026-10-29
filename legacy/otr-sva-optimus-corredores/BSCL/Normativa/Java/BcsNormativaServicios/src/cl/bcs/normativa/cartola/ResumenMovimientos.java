package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import cl.bcs.cuentaInversion.SaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.EntradaSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SalidaSaldoCuentaInversion;
import cl.bcs.normativa.cartola.MovimientosCaja.SaldoInicialLegacy;
import cl.bcs.normativa.cartola.ResumenInversionBase.Portafolio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ResumenMovimientos implements BcsCartola{
	public final Log log = new Log("NOR.ResumenMovimientos");
	
	private final String[] TIPO_MOVIMIENTO = {"Operaciones", "Dividendos / Cupones", "Movimiento Tesorería", "Movimiento Cuenta Inversión", "Admin. Liquidez"};
	
	public final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenMovimientos_SelectMovimientos.sql");
	public final String SQL_SELECT_MOVIMIENTOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenMovimientos_SelectMovimientosLegacy.sql");
	public final String SQL_SELECT_SALDO_INICIAL_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenMovimientos_SelectSaldoInicialLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_SELECT_MONEDAS_PRINCIPALES = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenMovimientos_SelectMonedasPrincipales.sql");

	
	public static class SaldoInicialLegacy {
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal SaldoInicial;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaResumenMovimientos{
		public String TipoMovimiento;
		public Long IdCliente;
	   	public Long IdPortafolio;
	   	public String Moneda;
	   	public BigDecimal Cargo;
	   	public BigDecimal Abono;
	   	public BigDecimal SaldoInicial;
	   	public BigDecimal SaldoFinal;
	   	
	   	public SalidaResumenMovimientos(){
	   		this.Cargo = BigDecimal.ZERO;
	   		this.Abono = BigDecimal.ZERO;
	   	}
	   	
	   	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MovimientoCuentaInversion{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdCargoAbonoMovimiento;
		public String IdConcepto;
		public String NombreConcepto;
		public String Origen;
		public String IdModulo;
		public String IdTipoOperacion;
		public String NombreTipoOperacion;
		public String IdMoneda;
		public BigDecimal Cargo;
		public BigDecimal Abono;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Movimiento{
		public Long IdCliente;
		public Long IdPortafolio;
		public String TipoMovimiento;
		public String Moneda;
		public BigDecimal Cargo;
		public BigDecimal Abono;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CargoAbono{
		public BigDecimal Abono;
		public BigDecimal Cargo;
		
		public CargoAbono(){
			this.Abono = BigDecimal.ZERO;
			this.Cargo = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class TipoMovimiento{
		public String TipoMovimiento;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Monedas extends BcsPojo{
		public String IdMoneda;
	}
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		log.write("INICIO::ResumenMovimientos");
		SQL sql = new SQL();
		SalidaCartola out = new SalidaCartola();
				
		Date fechaAnterior = Fecha.addDays(entrada.p_FechaDesde, -1);
		
		SaldoCuentaInversion saldoCuentaInversion = new SaldoCuentaInversion();
		EntradaSaldoCuentaInversion entradaSaldoCuentaInversion = new EntradaSaldoCuentaInversion();
		
		List<String> monedasPrincipales = obtenerMonedasPrincipales(con, entrada.p_IdEmpresa);
		
		List<Long> portafolios = new ArrayList<Long>();
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		List<Portafolio> portObj = new ArrayList<>();
		
		for(Dato dato : entrada.p_Datos){
			if(!portafolios.contains(dato.IdPortafolio)){
				SaldoCuentaInversion.InCuentaInversionConsultar cuenta = new SaldoCuentaInversion.InCuentaInversionConsultar ();
				cuenta.IdPortafolio = dato.IdPortafolio;
				entradaSaldoCuentaInversion.p_CuentaInversionAConsultar.add(cuenta);
				portafolios.add(dato.IdPortafolio);
				
				Portafolio por = new Portafolio();
				por.IdTrabajo = idTrabajo;
				por.IdPortafolio = dato.IdPortafolio;
				portObj.add(por);
			}
		}
		sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portObj, Portafolio.class);

		HashMap<String,SaldoInicialLegacy> hashSaldosLegacy = new HashMap<String,SaldoInicialLegacy>();
		//saldos legacy
		{
			HashMap<String,Object> params = new HashMap<String,Object>();
			params.clear();
			params.put("IdMoneda", new ArrayList<>(Arrays. asList("CLP","USD","EUR")));
			params.put("FechaDesde", entrada.p_FechaDesde);
			
			List<SaldoInicialLegacy> listSaldoInicialLegacy = sql.query(con, SQL_SELECT_SALDO_INICIAL_LEGACY, params, SaldoInicialLegacy.class);
			for (SaldoInicialLegacy saldo: listSaldoInicialLegacy) {
				SaldoInicialLegacy saldo_aux = hashSaldosLegacy.get(saldo.IdPortafolio + "_" + saldo.IdMoneda);
				if(saldo_aux == null) saldo_aux = new SaldoInicialLegacy();
				saldo_aux.SaldoInicial = saldo.SaldoInicial;
				hashSaldosLegacy.put(saldo.IdPortafolio + "_" + saldo.IdMoneda, saldo_aux);
			}
		}
		
		entradaSaldoCuentaInversion.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaSaldoCuentaInversion.p_FechaDesde = Fecha.stringToSqlDate(fechaAnterior);
		entradaSaldoCuentaInversion.p_FechaHasta = Fecha.stringToSqlDate(fechaAnterior);
		
		SalidaSaldoCuentaInversion salidaSaldoCuentaInversion = saldoCuentaInversion.logica(con, entradaSaldoCuentaInversion);
		List<SalidaResumenMovimientos> salidaTemp = new ArrayList<SalidaResumenMovimientos>();
		
		for(SaldoCuentaInversion.SaldoPorFechaMovimiento saldo : salidaSaldoCuentaInversion.P_OUT_SALDOPORFECHAMOVIMIENTO){
			for(String tipo : TIPO_MOVIMIENTO){
				SalidaResumenMovimientos salidaResumen = new SalidaResumenMovimientos();
				salidaResumen.TipoMovimiento = tipo;
				salidaResumen.IdCliente = saldo.IdCliente;
				salidaResumen.IdPortafolio = saldo.IdPortafolio;
				salidaResumen.Moneda = saldo.IdMoneda;
				salidaResumen.SaldoInicial = saldo.SaldoDisponible;
				if (hashSaldosLegacy.get(salidaResumen.IdPortafolio + "_" + salidaResumen.Moneda) != null) {
					salidaResumen.SaldoInicial = hashSaldosLegacy.get(salidaResumen.IdPortafolio + "_" + salidaResumen.Moneda).SaldoInicial;
				}
				salidaTemp.add(salidaResumen);
			}
		}
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("FechaDesde", entrada.p_FechaDesde);
		params.put("FechaHasta", entrada.p_FechaHasta);
		params.put("IdProductoAgrupador", null);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		
		List<MovimientoCuentaInversion> movimientosCuentaInversion = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, MovimientoCuentaInversion.class);
		params.remove("IdProductoAgrupador");
		List<MovimientoCuentaInversion> movimientosCuentaInversionLegacy = sql.query(con, SQL_SELECT_MOVIMIENTOS_LEGACY, params, MovimientoCuentaInversion.class);
		movimientosCuentaInversion.addAll(movimientosCuentaInversionLegacy);
		List<Movimiento> movimientos = new ArrayList<Movimiento>();
		
		for(MovimientoCuentaInversion mov : movimientosCuentaInversion){
			Movimiento movimiento = new Movimiento();
			movimiento.IdCliente = mov.IdCliente;
			movimiento.IdPortafolio = mov.IdPortafolio;
			
			if("CTA".equals(mov.IdModulo)){
				movimiento.TipoMovimiento = "Movimiento Cuenta Inversión";
			}
			else if("TES".equals(mov.IdModulo)){
				movimiento.TipoMovimiento = "Movimiento Tesorería";
			}
			else if("CUS".equals(mov.IdModulo) && mov.NombreTipoOperacion != null && (mov.NombreTipoOperacion.matches("^DIVIDENDO") ||  mov.NombreTipoOperacion.matches("CUPON"))){
				movimiento.TipoMovimiento = "Dividendos / Cupones";
			}
			else {
				movimiento.TipoMovimiento = "Operaciones";
			}
			
			movimiento.Moneda = mov.IdMoneda;
			movimiento.Cargo = mov.Cargo;
			movimiento.Abono = mov.Abono;
			
			if(movimiento.TipoMovimiento != null){
				movimientos.add(movimiento);
			}
			else {
				
			}
		}
		
		HashMap<String,CargoAbono> clientesCargoAbono = new HashMap<String,CargoAbono>();
		List<SalidaResumenMovimientos> salidas = new ArrayList<SalidaResumenMovimientos>();
		//SE BUSCAN POR TIPO MOVIMIENTO
		for(SalidaResumenMovimientos saldoTemp : salidaTemp){
			CargoAbono cargoAbono = clientesCargoAbono.get(saldoTemp.Moneda + "_" + saldoTemp.IdCliente + "_" + saldoTemp.IdPortafolio) != null ? clientesCargoAbono.get(saldoTemp.Moneda + "_" + saldoTemp.IdCliente + "_" + saldoTemp.IdPortafolio) : new CargoAbono();
			for(Movimiento mov : movimientos){
				if(saldoTemp.IdCliente.equals(mov.IdCliente) && saldoTemp.IdPortafolio.equals(mov.IdPortafolio) && saldoTemp.TipoMovimiento.equals(mov.TipoMovimiento) && saldoTemp.Moneda.equals(mov.Moneda)){
					if(mov.Abono != null) saldoTemp.Abono = saldoTemp.Abono.add(mov.Abono, MathContext.DECIMAL128);
					if(mov.Cargo != null) saldoTemp.Cargo = saldoTemp.Cargo.add(mov.Cargo, MathContext.DECIMAL128);
				}
			}
			//SUMAMOS AL GLOBAL POR CLIENTE
			cargoAbono.Abono = cargoAbono.Abono.add(saldoTemp.Abono, MathContext.DECIMAL128);
			cargoAbono.Cargo = cargoAbono.Cargo.add(saldoTemp.Cargo, MathContext.DECIMAL128);
			clientesCargoAbono.put(saldoTemp.Moneda + "_" + saldoTemp.IdCliente + "_" + saldoTemp.IdPortafolio,cargoAbono);
		}
		
		//SE CALCULA SALDO FINAL
		HashMap<String,Boolean> existeCliente = new HashMap<String,Boolean>();
		for(SalidaResumenMovimientos saldoTemp : salidaTemp){
			CargoAbono cargoAbono = clientesCargoAbono.get(saldoTemp.Moneda + "_" + saldoTemp.IdCliente + "_" + saldoTemp.IdPortafolio) != null ? clientesCargoAbono.get(saldoTemp.Moneda + "_" + saldoTemp.IdCliente + "_" + saldoTemp.IdPortafolio) : new CargoAbono();
			
			BigDecimal diferencia = cargoAbono.Abono.subtract(cargoAbono.Cargo, MathContext.DECIMAL128);
			saldoTemp.SaldoFinal = saldoTemp.SaldoInicial.add(diferencia, MathContext.DECIMAL128);
			
			if(BigDecimal.ZERO.compareTo(saldoTemp.Abono) != 0 || BigDecimal.ZERO.compareTo(saldoTemp.Cargo) != 0){
				salidas.add(saldoTemp);
			}
			
			Boolean existe = existeCliente.get(saldoTemp.Moneda + "_" + saldoTemp.IdCliente + "_" + saldoTemp.IdPortafolio);
			
			if(BigDecimal.ZERO.compareTo(cargoAbono.Abono) == 0 && BigDecimal.ZERO.compareTo(cargoAbono.Cargo) == 0 && (existe == null || !existe)){
				if(monedasPrincipales != null && monedasPrincipales.contains(saldoTemp.Moneda)) {
					SalidaResumenMovimientos salidaResumen = new SalidaResumenMovimientos();
					salidaResumen.TipoMovimiento = "Sin Movimientos";
					salidaResumen.IdCliente = saldoTemp.IdCliente;
					salidaResumen.IdPortafolio = saldoTemp.IdPortafolio;
					salidaResumen.Moneda = saldoTemp.Moneda;
					salidaResumen.SaldoInicial = saldoTemp.SaldoInicial;
					salidaResumen.SaldoFinal = saldoTemp.SaldoInicial;
					saldoTemp.SaldoFinal = BigDecimal.ZERO;
					existeCliente.put(saldoTemp.Moneda + "_" + saldoTemp.IdCliente + "_" + saldoTemp.IdPortafolio, true);
					
					salidas.add(salidaResumen);
				}
			}
		}
		
		List<HashMap<String,Object>> resumenMovimientos = Utils.List2ListHashGenericObject(salidas);
		
		out.P_OUT_SALIDA.put("resumenMovimientos", resumenMovimientos);
		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));

		log.write("FIN::ResumenMovimientos");
		return out;
	}
	
	private List<String> obtenerMonedasPrincipales(Connection con, Long idEmpresa) throws SQLException{
		log.write("INICIO::ResumenMovimientos.obtenerMonedasPrincipales");
		long inicio = System.currentTimeMillis();
		
		List<String> monedas = new ArrayList<String>();
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>(1);
		params.put("IdEmpresa", idEmpresa);
		List<Monedas> monedasPrincipales = sql.query(con, SQL_SELECT_MONEDAS_PRINCIPALES, params, Monedas.class);
		
		if(!monedasPrincipales.isEmpty()) {
			monedas = monedasPrincipales.stream().map(m -> m.IdMoneda).collect(Collectors.toList());
		}
		
		long fin = System.currentTimeMillis();
		log.write("FIN::ResumenMovimientos.obtenerMonedasPrincipales: ", fin - inicio);
		return monedas;
		
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2018-08-01");
		Objin.p_FechaHasta = Fecha.toDate("2018-08-03");
		Dato dato = new Dato();
		dato.IdCliente = 524891L;
		dato.IdPortafolio = 512341L;
		Objin.p_Datos.add(dato);
		ResumenMovimientos  demo= new ResumenMovimientos();				
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
