package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.cuentaInversion.SaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.EntradaSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaMovimiento;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SalidaSaldoCuentaInversion;
import cl.bcs.normativa.cartola.MovimientosTitulos.MovimientoADL;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class MovimientosCaja implements BcsCartola{
	
	final static Logger logger = LogManager.getLogger(MovimientosCaja.class);
	
	private final String SQL_SELECT_MOVIMIENTOS_CAJA = loadResourceToString("cl/bcs/normativa/cartola/sql/MovimientosCaja_SelectMovimientosCaja.sql");
	private final String SQL_SELECT_MOVIMIENTOS_CAJA_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/MovimientosCaja_SelectMovimientosCajaLegacy.sql");
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	private final String SQL_SELECT_SALDO_INICIAL_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/MovimientosCaja_SelectSaldoInicialLegacy.sql");
	private final String SQL_SELECT_SALDO_GARANTIA_ACTUAL = loadResourceToString("cl/bcs/normativa/cartola/sql/MovimientosCaja_SelectSaldoGarantiaActual.sql");
	private final String SQL_SELECT_SALDO_GARANTIA_HISTORICA = loadResourceToString("cl/bcs/normativa/cartola/sql/MovimientosCaja_SelectSaldoGarantiaHistorica.sql");
	private final String SQL_SELECT_MONEDAS = loadResourceToString("cl/bcs/normativa/cartola/sql/MovimientosCaja_SelectMonedas.sql");
	private final String SQL_SELECT_EXCLUYE_DESDE_MOV_TRANSITO = loadResourceToString("cl/bcs/normativa/cartola/sql/MovimientosCaja_SelectExcluyeDesdeMovimientosEnTransito.sql");
	private final String SQL_SELECT_MOVIMIENTOS_ADL = loadResourceToString("cl/bcs/normativa/cartola/sql/MovimientosCaja_SelectMovimientosADL.sql");
	
	public static class SalidaMovimientosCaja{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long PosicionRegistro;
		public Long IdCargoAbonoMovimiento;
		public String NombreConcepto;
		public Long IdComprobante;
		public String TipoComprobante;
		public String Nemotecnico;
		public BigDecimal Cantidad;
		public String FechaIngreso;
		public String FechaOperacion;
		public String FechaLiquidacion;
		public String Moneda;
		public String TituloCuentaInversion;
		public BigDecimal Cargo;
		public BigDecimal Abono;
		public BigDecimal Saldo;
		public BigDecimal SaldoInicial;
		public BigDecimal SaldoFinal;
		public BigDecimal SaldoGarantia;
		public String PeriodoInicial;
		public String PeriodoFinal;
		public String IdConcepto;           
		public String NombreTipoMovimiento; // nuevo
		public String AfectaRentabilidad;   // nuevo
		public String TipoConcepto;         // nuevo
		public String IdTipoOperacion;       // nuevo
		
		public String AccionTipoOperacion;
		public String IdModulo;
		public SalidaMovimientosCaja() {
			this.SaldoInicial = BigDecimal.ZERO;
			this.SaldoFinal = BigDecimal.ZERO;
			this.SaldoGarantia = BigDecimal.ZERO;
			this.IdConcepto="CON";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Movimiento{
		public Long IdCliente;
		public Long IdPortafolio;
		public Long PosicionRegistro;
		public Long IdCargoAbonoMovimiento;
		public String NombreConcepto;
		public String TipoComprobante;
		public Long IdComprobante;
		public String Nemotecnico;
		public BigDecimal Cantidad;
		public String FechaIngreso;
		public String FechaOperacion;
		public String FechaLiquidacion;
		public String Moneda;
		public String TituloCuentaInversion;
		public BigDecimal Cargo;
		public BigDecimal Abono;
		public BigDecimal Saldo;
		public String IdConcepto;
		public String NombreTipoMovimiento; // nuevo
		public String AfectaRentabilidad;   // nuevo
		public String TipoConcepto;         // nuevo
		public String IdTipoOperacion;      //Nuevo
		public String AccionTipoOperacion;
		public String IdModulo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SaldoGarantia{
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal SaldoMonedaSistema;
		public BigDecimal SaldoMonedaAdicional;
		public BigDecimal SaldoMonedaInstrumento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SaldoInicialLegacy {
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal SaldoInicial;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SaldoCuentaInversionMovCaja{
		public BigDecimal SaldoInicial;
		public BigDecimal SaldoFinal;
		
		public SaldoCuentaInversionMovCaja() {
			this.SaldoInicial = BigDecimal.ZERO;
			this.SaldoFinal = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MonedaCuentaInversion{
		public String IdMoneda;
		public String Codigo;
		public String Nombre;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
		public Long IdTrabajo;
	}
	
	public static class ExclusionCartola extends BcsPojo{
		public Long IdCargoAbonoMovimiento;
		public String ExcluyeMovimientoEnCartola;
		
		public ExclusionCartola() {
			this.IdCargoAbonoMovimiento = 0L;
			this.ExcluyeMovimientoEnCartola = "N";
		}
	}
	
	public static class MovimientoADL{
		public Long IdSolicitudAporteRescate;
		public String Overnight;
		public String AdministracionLiquidez;
		public Long IdCargoAbonoMovimiento;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		logger.info("INICIO::MovimientosCaja");
		
		SQL sql = new SQL();
		
		SalidaCartola out = new SalidaCartola();

		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		Date fechaAnterior = Fecha.addDays(entrada.p_FechaDesde, -1);
		String fechaHastaStr = Fecha.stringToSqlDate(entrada.p_FechaHasta);
		int anio = Integer.valueOf(fechaHastaStr.split("-")[0]);
		int mes = Integer.valueOf(fechaHastaStr.split("-")[1]) - 1;
		Calendar calendario = new GregorianCalendar(anio, mes, 1);
		String fechaHastaUltimoDia = fechaHastaStr.split("-")[0] + "-" + fechaHastaStr.split("-")[1] + "-" + calendario.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		SaldoCuentaInversion saldoCuentaInversion = new SaldoCuentaInversion();
		EntradaSaldoCuentaInversion entradaSaldoCuentaInversion = new EntradaSaldoCuentaInversion();
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		List<MonedaCuentaInversion> monedasCuentaInversion = sql.query(con, SQL_SELECT_MONEDAS, params, MonedaCuentaInversion.class);
		
		List<String> idMonedas = monedasCuentaInversion.stream().map(moneda -> moneda.IdMoneda).collect(Collectors.toList());

		List<Long> portafolios = new ArrayList<Long>();
		List<Portafolio> portObjs = new ArrayList<>();
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();

		for(Dato dato : entrada.p_Datos){
			for(String moneda : idMonedas) {
				SaldoCuentaInversion.InCuentaInversionConsultar cuenta = new SaldoCuentaInversion.InCuentaInversionConsultar ();
				cuenta.IdPortafolio = dato.IdPortafolio;
				cuenta.IdMoneda = moneda;
				entradaSaldoCuentaInversion.p_CuentaInversionAConsultar.add(cuenta);
			}
			portafolios.add(dato.IdPortafolio);
			Portafolio p = new Portafolio();
			p.IdPortafolio = dato.IdPortafolio;
			p.IdTrabajo = idTrabajo;
			portObjs.add(p);
		}
		
		sql.batch(con,SQL_INSERT_PORTAFOLIOS_LEGACY,portObjs,Portafolio.class);
		
		params.clear();
		params.put("IdMoneda", idMonedas);
		params.put("FechaDesde", entrada.p_FechaDesde);
		params.put("FechaHasta", entrada.p_FechaHasta);
		List<Movimiento> movimientos = sql.query(con, SQL_SELECT_MOVIMIENTOS_CAJA, params, Movimiento.class);
		
		List<SalidaMovimientosCaja> movimientosLegacy = sql.query(con, SQL_SELECT_MOVIMIENTOS_CAJA_LEGACY, params, SalidaMovimientosCaja.class);
		
		Long ultimoindex = 0L;
		if (movimientosLegacy.size() > 0 && movimientosLegacy.get(movimientosLegacy.size()-1) != null) {
			SalidaMovimientosCaja ultimo = movimientosLegacy.get(movimientosLegacy.size()-1);
			ultimoindex = ultimo.PosicionRegistro;
		}
		
		//saldo legacy
		params.clear();
		params.put("IdMoneda", idMonedas);
		params.put("FechaDesde", entrada.p_FechaDesde);
		
		List<SaldoInicialLegacy> listSaldoInicialLegacy = sql.query(con, SQL_SELECT_SALDO_INICIAL_LEGACY, params, SaldoInicialLegacy.class);
		HashMap<String,SaldoInicialLegacy> hashSaldosLegacy = new HashMap<String,SaldoInicialLegacy>();
		for (SaldoInicialLegacy saldo: listSaldoInicialLegacy) {
			SaldoInicialLegacy saldo_aux = hashSaldosLegacy.get(saldo.IdPortafolio + "_" + saldo.IdMoneda);
			if(saldo_aux == null) saldo_aux = new SaldoInicialLegacy();
			saldo_aux.SaldoInicial = saldo.SaldoInicial;
			hashSaldosLegacy.put(saldo.IdPortafolio + "_" + saldo.IdMoneda, saldo_aux);
		}
		
		//garantias
		params.clear();
		params.put("FechaDesde", entrada.p_FechaDesde);
		params.put("FechaHasta", entrada.p_FechaHasta);
		List<SaldoGarantia> saldosGarantia = new ArrayList<SaldoGarantia>();
		if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
			saldosGarantia = sql.query(con, SQL_SELECT_SALDO_GARANTIA_ACTUAL, params, SaldoGarantia.class);
		}else {
			saldosGarantia = sql.query(con, SQL_SELECT_SALDO_GARANTIA_HISTORICA, params, SaldoGarantia.class);
		}
		
		HashMap<String,BigDecimal> hSaldoGarantias = new HashMap<String,BigDecimal>();
		for(SaldoGarantia saldoGarantia : saldosGarantia) {
			if(saldoGarantia.IdMoneda.equals(monedaSistema)) {
				hSaldoGarantias.put(saldoGarantia.IdPortafolio + "_" + monedaSistema, saldoGarantia.SaldoMonedaSistema);
			}else {
				hSaldoGarantias.put(saldoGarantia.IdPortafolio + "_" + saldoGarantia.IdMoneda, saldoGarantia.SaldoMonedaInstrumento);
			}
		}
		
		params.clear();
		List<ExclusionCartola> excluye = sql.query(con, SQL_SELECT_EXCLUYE_DESDE_MOV_TRANSITO, params, ExclusionCartola.class);
		Map<Long,ExclusionCartola> mapExcluye = excluye.parallelStream().collect(Collectors.toMap(excl->excl.IdCargoAbonoMovimiento, excl->excl));
		
		//SALDO INICIAL
		entradaSaldoCuentaInversion.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaSaldoCuentaInversion.p_FechaDesde = Fecha.stringToSqlDate(fechaAnterior);
		entradaSaldoCuentaInversion.p_FechaHasta = Fecha.stringToSqlDate(fechaAnterior);
		
		SalidaSaldoCuentaInversion salidaSaldoCuentaInversion = saldoCuentaInversion.logica(con, entradaSaldoCuentaInversion);
		HashMap<String,SaldoCuentaInversionMovCaja> saldosCuentaInversionMovCaja = new HashMap<String,SaldoCuentaInversionMovCaja>();
		
		for(SaldoPorFechaMovimiento saldo : salidaSaldoCuentaInversion.P_OUT_SALDOPORFECHAMOVIMIENTO) {
			SaldoCuentaInversionMovCaja saldoCuentaInversionMovCaja = saldosCuentaInversionMovCaja.get(saldo.IdPortafolio + "_" + saldo.IdMoneda);
			if(saldoCuentaInversionMovCaja == null) saldoCuentaInversionMovCaja = new SaldoCuentaInversionMovCaja();
			saldoCuentaInversionMovCaja.SaldoInicial = saldo.SaldoDisponible;
			saldosCuentaInversionMovCaja.put(saldo.IdPortafolio + "_" + saldo.IdMoneda, saldoCuentaInversionMovCaja);
		}
		
		entradaSaldoCuentaInversion.p_FechaDesde = Fecha.stringToSqlDate(entrada.p_FechaHasta);
		entradaSaldoCuentaInversion.p_FechaHasta = Fecha.stringToSqlDate(entrada.p_FechaHasta);
		
		salidaSaldoCuentaInversion = saldoCuentaInversion.logica(con, entradaSaldoCuentaInversion);
		
		//SALDO FINAL
		for(SaldoPorFechaMovimiento saldo : salidaSaldoCuentaInversion.P_OUT_SALDOPORFECHAMOVIMIENTO) {
			SaldoCuentaInversionMovCaja saldoCuentaInversionMovCaja = saldosCuentaInversionMovCaja.get(saldo.IdPortafolio + "_" + saldo.IdMoneda);
			if(saldoCuentaInversionMovCaja == null) saldoCuentaInversionMovCaja = new SaldoCuentaInversionMovCaja();
			saldoCuentaInversionMovCaja.SaldoFinal = saldo.SaldoDisponible;
			saldosCuentaInversionMovCaja.put(saldo.IdPortafolio + "_" + saldo.IdMoneda, saldoCuentaInversionMovCaja);
		}
		
		List<SalidaMovimientosCaja> salidas = new ArrayList<SalidaMovimientosCaja>();
		salidas.addAll(movimientosLegacy);
		for (SalidaMovimientosCaja mov : movimientosLegacy ) {
			SaldoInicialLegacy saldoInicialLegacy = hashSaldosLegacy.get(mov.IdPortafolio + "_" + mov.Moneda);
			if (saldoInicialLegacy != null) {
				mov.SaldoInicial = saldoInicialLegacy.SaldoInicial;	
			}
		}

		for(Movimiento mov : movimientos) {
			ExclusionCartola exclusion = mapExcluye.get(mov.IdCargoAbonoMovimiento);
			SaldoCuentaInversionMovCaja saldoCuentaInversionMovCaja = saldosCuentaInversionMovCaja.get(mov.IdPortafolio + "_" + mov.Moneda);
			SaldoInicialLegacy saldoInicialLegacy = hashSaldosLegacy.get(mov.IdPortafolio + "_" + mov.Moneda);
			BigDecimal saldoGarantia = hSaldoGarantias.get(mov.IdPortafolio + "_" + mov.Moneda);
			SalidaMovimientosCaja salida = new SalidaMovimientosCaja();
			
			salida.IdTipoOperacion = mov.IdTipoOperacion;
			salida.TipoConcepto = mov.TipoConcepto;
			salida.AfectaRentabilidad = mov.AfectaRentabilidad;
			salida.IdConcepto = mov.IdConcepto;
			salida.NombreTipoMovimiento = mov.NombreTipoMovimiento;
			salida.IdCliente = mov.IdCliente;
			salida.IdPortafolio = mov.IdPortafolio;
			salida.PosicionRegistro = mov.PosicionRegistro + ultimoindex;
			salida.IdCargoAbonoMovimiento = mov.IdCargoAbonoMovimiento;
			salida.NombreConcepto = mov.NombreConcepto.toUpperCase();
			salida.IdComprobante = mov.IdComprobante != null ? mov.IdComprobante : 0L;
			salida.TipoComprobante = mov.TipoComprobante;
			salida.Nemotecnico = mov.Nemotecnico;
			salida.Cantidad = mov.Cantidad;
			salida.FechaIngreso = mov.FechaIngreso;
			salida.FechaOperacion = mov.FechaOperacion;
			salida.FechaLiquidacion = mov.FechaLiquidacion;
			salida.Moneda = mov.Moneda;
			salida.TituloCuentaInversion = mov.TituloCuentaInversion;
			salida.Abono = MonedaFormato.formatear(con, mov.Abono,MonedaFormato.MONTO, mov.Moneda).valor;
			salida.Cargo = MonedaFormato.formatear(con, mov.Cargo,MonedaFormato.MONTO, mov.Moneda).valor;
			salida.Saldo = MonedaFormato.formatear(con, mov.Saldo,MonedaFormato.MONTO, mov.Moneda).valor;
			salida.PeriodoInicial = Fecha.stringToSqlDate(entrada.p_FechaDesde);
			salida.PeriodoFinal = fechaHastaUltimoDia;
			salida.AccionTipoOperacion = mov.AccionTipoOperacion;
			salida.IdModulo = mov.IdModulo;
			if(saldoCuentaInversionMovCaja != null) {
				salida.SaldoInicial = saldoCuentaInversionMovCaja.SaldoInicial;
				salida.SaldoFinal = saldoCuentaInversionMovCaja.SaldoFinal;
			}
			if (saldoInicialLegacy != null) {
				salida.SaldoInicial = saldoInicialLegacy.SaldoInicial;
			}
			if(saldoGarantia != null) {
				salida.SaldoGarantia = saldoGarantia;
			}
			
			if ((salida.Abono.compareTo(BigDecimal.ZERO) == 0) && salida.Cargo.compareTo(BigDecimal.ZERO) == 0 || (exclusion != null && "S".equals(exclusion.ExcluyeMovimientoEnCartola))) {
				System.out.println("me lo salto");
			}
			else {
				salidas.add(salida);
			}
		}
		
		Collections.sort(salidas,new Comparator<SalidaMovimientosCaja>() {
		    @Override
		    public int compare(SalidaMovimientosCaja mov1, SalidaMovimientosCaja mov2) {
		    	int mon = mov1.Moneda.compareTo(mov2.Moneda);
		    	if (mon != 0) return mon;
		    	
		    	return Long.compare(mov1.PosicionRegistro,mov2.PosicionRegistro);
		    	
		    }
		});
		
		List<HashMap<String,Object>> resultadoMovimientosCaja = Utils.List2ListHashGenericObject(salidas);

		String parametroADL = "N";
		
		try {
			params.clear();
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			parametroADL = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "NORMATIVA", "AgruparCartolaMovimientosADL");
		    
		} catch (Exception e) {
		    parametroADL = "N";
		}

		if("S".equals(parametroADL)) {
			params.clear();
			params.put("FechaDesde", entrada.p_FechaDesde);
			params.put("FechaHasta", entrada.p_FechaHasta);
			List<MovimientoADL>  MovimientosADL = sql.query(con, SQL_SELECT_MOVIMIENTOS_ADL, params, MovimientoADL.class);
			
			Set<Long> idsADL = MovimientosADL.stream().map(m -> m.IdCargoAbonoMovimiento).filter(Objects::nonNull).collect(Collectors.toSet());
						
			List<SalidaMovimientosCaja> movimientosNormales = new ArrayList<>();
			List<SalidaMovimientosCaja> movimientosParaAgrupar = new ArrayList<>();
			
			for (SalidaMovimientosCaja m : salidas) {
			    if (m.IdCargoAbonoMovimiento != null &&
			        idsADL.contains(m.IdCargoAbonoMovimiento)) {
			    	movimientosParaAgrupar.add(m);
			    } else {
			    	movimientosNormales.add(m);
			    }
			}
			
			Map<String, List<SalidaMovimientosCaja>> agrupados = movimientosParaAgrupar.stream().collect(Collectors.groupingBy(m -> m.NombreTipoMovimiento + "_" + m.Moneda + m.IdCliente + m.IdPortafolio));
			
			
			List<SalidaMovimientosCaja> agrupadosFinal = new ArrayList<>();
			String RescatesAgrupados = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "RESCATES_ADL_AGRUPADOS", new HashMap<String, String>(0));
			String AportesAgrupados = Diccionario.getMensaje(con, entrada.p_IdEmpresa, "APORTES_ADL_AGRUPADOS", new HashMap<String, String>(0));

			for (Map.Entry<String, List<SalidaMovimientosCaja>> entry : agrupados.entrySet()) {
				List<SalidaMovimientosCaja> grupo = entry.getValue();
				grupo.sort(Comparator.comparingLong(m -> m.PosicionRegistro != null ? m.PosicionRegistro : 0L));
			    SalidaMovimientosCaja base = grupo.get(0);
			    SalidaMovimientosCaja ultimo = grupo.get(grupo.size() - 1);

			    SalidaMovimientosCaja nuevo = new SalidaMovimientosCaja();

			    nuevo.NombreTipoMovimiento = base.NombreTipoMovimiento;
			    nuevo.Moneda = base.Moneda;
			    nuevo.TituloCuentaInversion = base.TituloCuentaInversion;
			    nuevo.TipoConcepto = base.TipoConcepto;
			    nuevo.IdModulo = base.IdModulo;
			    nuevo.PeriodoInicial = base.PeriodoInicial;
			    nuevo.PeriodoFinal = base.PeriodoFinal;
			    nuevo.AfectaRentabilidad = base.AfectaRentabilidad;
			    nuevo.IdTipoOperacion = base.IdTipoOperacion;
			    nuevo.AccionTipoOperacion = base.AccionTipoOperacion;
			    if (" RESCATE FONDO MUTUO".equalsIgnoreCase(base.NombreConcepto)) {
			        nuevo.NombreConcepto = RescatesAgrupados;
			    } 
			    else if (" APORTE OVERNIGHT".equalsIgnoreCase(base.NombreConcepto)) {
			        nuevo.NombreConcepto = AportesAgrupados;
			    } 
			    else {
			        nuevo.NombreConcepto = base.NombreConcepto;
			    }
			    nuevo.TipoComprobante = base.TipoComprobante;
			    nuevo.Nemotecnico = base.Nemotecnico;
			    nuevo.IdConcepto = base.IdConcepto;
			    nuevo.IdCliente = base.IdCliente;
			    nuevo.IdPortafolio = base.IdPortafolio;
			    nuevo.PosicionRegistro = ultimo.PosicionRegistro;
			    nuevo.IdCargoAbonoMovimiento = null;
			    nuevo.IdComprobante = null;
			    nuevo.FechaIngreso = "";
			    nuevo.FechaOperacion = "";
			    nuevo.FechaLiquidacion = "";
			    nuevo.Cargo = BigDecimal.ZERO;
			    nuevo.Abono = BigDecimal.ZERO;
			    nuevo.Saldo = BigDecimal.ZERO;
			    nuevo.SaldoInicial = BigDecimal.ZERO;
			    nuevo.SaldoFinal = BigDecimal.ZERO;
			    nuevo.SaldoGarantia = BigDecimal.ZERO;
			    nuevo.Cantidad = BigDecimal.ZERO;
			    nuevo.SaldoInicial = base.SaldoInicial;
			    nuevo.Saldo = ultimo.Saldo;
			    nuevo.SaldoFinal = ultimo.SaldoFinal;
			    nuevo.SaldoGarantia = ultimo.SaldoGarantia;

			    for (SalidaMovimientosCaja m : grupo) {
			        nuevo.Cargo = nuevo.Cargo.add(m.Cargo == null ? BigDecimal.ZERO : m.Cargo);
			        nuevo.Abono = nuevo.Abono.add(m.Abono == null ? BigDecimal.ZERO : m.Abono);
			        nuevo.Cantidad = (nuevo.Cantidad == null ? BigDecimal.ZERO : nuevo.Cantidad).add(m.Cantidad == null ? BigDecimal.ZERO : m.Cantidad);
			    }

			    agrupadosFinal.add(nuevo);

			}

			List<SalidaMovimientosCaja> salidasFinal = new ArrayList<>();

			salidasFinal.addAll(movimientosNormales);
			salidasFinal.addAll(agrupadosFinal);

			Collections.sort(salidasFinal, new Comparator<SalidaMovimientosCaja>() {
			    @Override
			    public int compare(SalidaMovimientosCaja mov1, SalidaMovimientosCaja mov2) {
			        int mon = mov1.Moneda.compareTo(mov2.Moneda);
			        if (mon != 0) return mon;

			        return Long.compare(mov1.PosicionRegistro, mov2.PosicionRegistro);
			    }
			});


			List<HashMap<String,Object>> resultadoFinal = Utils.List2ListHashGenericObject(salidasFinal);
			out.P_OUT_SALIDA.put("movimientosCaja", resultadoFinal);
		}else {
			out.P_OUT_SALIDA.put("movimientosCaja", resultadoMovimientosCaja);
		}

		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));
		logger.info("FIN::MovimientosCaja");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("master02","connections.properties")) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2026-03-01");
		Objin.p_FechaHasta = Fecha.toDate("2026-03-31");
		Dato dato = new Dato();
		dato.IdCliente = 2536L;
		dato.IdPortafolio = 7001L;
		Objin.p_Datos.add(dato);
		MovimientosCaja  demo= new MovimientosCaja();				
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
