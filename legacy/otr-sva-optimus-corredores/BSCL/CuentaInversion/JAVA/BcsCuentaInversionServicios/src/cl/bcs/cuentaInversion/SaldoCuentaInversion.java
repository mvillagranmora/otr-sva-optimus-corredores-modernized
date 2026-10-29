package cl.bcs.cuentaInversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.BarraProgreso;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class SaldoCuentaInversion implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(SaldoCuentaInversion.class);
	
	private final String SQL_SELECT_MOVIMIENTOS = loadResourceToString("cl/bcs/cuentaInversion/sql/SaldoCuentaInversion_SelectMovimientos.sql");
	private final String SQL_SELECT_SALDO_ANTERIOR_EFECTIVA = loadResourceToString("cl/bcs/cuentaInversion/sql/SaldoCuentaInversion_SelectSaldoAnteriorEfectiva.sql");
	private final String SQL_SELECT_SALDO_ANTERIOR_SISTEMA = loadResourceToString("cl/bcs/cuentaInversion/sql/SaldoCuentaInversion_SelectSaldoAnteriorSistema.sql");
	private final String SQL_SELECT_CUENTA_INVERSION = loadResourceToString("cl/bcs/cuentaInversion/sql/SaldoCuentaInversion_SelectCuentaInversion.sql");
	private final String SQL_SELECT_CUENTA_INVERSION_FULL = loadResourceToString("cl/bcs/cuentaInversion/sql/SaldoCuentaInversion_SelectCuentaInversionFull.sql");
	private final String SQL_INSERT_CUENTA_INVERSION_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/SaldoCuentaInversion_InsertCuentaInversionTablaPaso.sql");
	private final String SQL_DELETE_CUENTA_INVERSION_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/SaldoCuentaInversion_DeleteCuentaInversionTablaPaso.sql");
	
	private final String SQL_INSERT_PORTAFOLIOS = "INSERT INTO \"BSCL.CuentaInversion.BD::GBL.SaldoCuentaInversion_Proceso_Paso\"(\"IdPortafolio\", \"IdTrabajo\") VALUES(:IdPortafolio, :IdTrabajo);";
			
	public static class EntradaSaldoCuentaInversion{
		public Long p_IdEmpresa;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public String p_SoloSaldoNegativo;
		public String p_SoloSaldoDisponible;
		public String p_SoloSaldoDisponibleNegativo;
		public String p_IncluyeCargosPendientes;
		public String p_IncluyeAbonosPendientes;
		public BarraProgreso			p_BarraProgreso;
		public List<InCuentaInversionConsultar> p_CuentaInversionAConsultar;
		
		public EntradaSaldoCuentaInversion(){
			this.p_SoloSaldoNegativo = "N";
			this.p_SoloSaldoDisponible = "N";
			this.p_SoloSaldoDisponibleNegativo = "N";
			this.p_IncluyeCargosPendientes = "N";
			this.p_IncluyeAbonosPendientes = "N";
			this.p_CuentaInversionAConsultar = new ArrayList<InCuentaInversionConsultar>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InCuentaInversionConsultar{
		public Long IdEjecutivo;
		public Long IdPortafolio;
		public String IdMoneda;
		public InCuentaInversionConsultar() {
			
		}
		public InCuentaInversionConsultar(Long idPortafolio) {
			this.IdPortafolio = idPortafolio;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InPortafolioConsultar{
		public Long IdPortafolio;
		public Long IdTrabajo;
		
		public InPortafolioConsultar(Long IdPortafolio) {
			this.IdPortafolio = IdPortafolio;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CuentaInversion{
		public Long IdCuentaInversion;
		public String Codigo;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdMoneda;
		public String CuentaInversionPrincipal;
		public Long IdTrabajo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Movimientos{
		public Long IdCuentaInversion;
		public Date FechaSistema;
		public Timestamp FechaCertificacion;
		public Timestamp FechaIngresoRegistro;
		public Date FechaEfectivaMovimiento;
		public Long IdCargoAbonoMovimiento;
		public String IdTipoMovimiento;
		public String IdEstado;
		public BigDecimal Saldo;
		public BigDecimal SaldoPorFechaMovimiento;
		public BigDecimal Monto;
		public BigDecimal MontoCargoPendiente;
		public BigDecimal MontoAbonoPendiente;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaSaldoCuentaInversion{
		public List<SaldoInicialFinal> P_OUT_SALDOINICIALFINAL;
		public List<SaldoPorFechaEfectiva> P_OUT_SALDOPORFECHAEFECTIVA;
		public List<SaldoPorFechaMovimiento> P_OUT_SALDOPORFECHAMOVIMIENTO;
		
		public SalidaSaldoCuentaInversion(){
			this.P_OUT_SALDOINICIALFINAL = new ArrayList<SaldoInicialFinal>();
			this.P_OUT_SALDOPORFECHAEFECTIVA = new ArrayList<SaldoPorFechaEfectiva>();
			this.P_OUT_SALDOPORFECHAMOVIMIENTO = new ArrayList<SaldoPorFechaMovimiento>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SaldoInicialFinal{
		public Long IdCuentaInversion;
		public BigDecimal SaldoEfectivoInicial;
		public BigDecimal SaldoEfectivoFinal;
		public BigDecimal SaldoMovimientoInicial;
		public BigDecimal SaldoMovimientoFinal;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SaldoPorFechaEfectiva{
		public Long IdCuentaInversion;
	  	public String CodigoCuentaInversion;
	  	public Long IdCliente;
	  	public Long IdPortafolio;
	  	public String FechaSaldo;
	  	public String IdMoneda;
	  	public BigDecimal Saldo;
	  	public BigDecimal SaldoDisponible;
	  	public BigDecimal SaldoCargosPendientes;
	  	public BigDecimal SaldoAbonosPendientes;	  	
	  	public Long IdEjecutivo;
		public String NombreEjecutivo;
		public String CodigoEjecutivo;
		public String CodigoSucursal;
		public String NombreSucursal;
     	public String NombreCliente;
     	public String DocumentoIdentidad;
     	public String NombrePortafolio;
     	public String PropositoPortafolio;
     	public String CodigoPortafolio;
     	public String CodigoMoneda;
     	public String NombreMoneda;
     	public BigDecimal SaldoEnGarantia;
     	public BigDecimal SaldoAPlazoSimultanea;
     	public BigDecimal SaldoAPlazoVentaCorta;
     	public BigDecimal SaldoAPlazoCompromiso;
     	public BigDecimal SaldoRetiro;
     	public BigDecimal SaldoRetenido;
     	public String Producto;
     	public BigDecimal MontoTransSolicitudesAporte = BigDecimal.ZERO; //nuevo

     	public Long IdProductoAgrupador;
     	public BigDecimal MontoMoneyMarket;
     	public BigDecimal TotalLiquidez;
	  	public Long IdMatrizAgrupador;
	  	public String EsADC;
	  	public String Relacion;
	  	public String TipoCliente = "";
	  	public String FechaUltimoMovimiento;
	  	public BigDecimal SaldoPendienteLibDAP;
	  	
	  	public SaldoPorFechaEfectiva() {
	  		this.SaldoRetenido = BigDecimal.ZERO;
	  		this.FechaUltimoMovimiento = "0001-01-01";
	  		this.SaldoPendienteLibDAP = BigDecimal.ZERO;
	  	}
	  	
	  	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SaldoPorFechaMovimiento{
		public Long IdCuentaInversion;
	  	public String CodigoCuentaInversion;
	  	public Long IdCliente;
	  	public Long IdPortafolio;
	  	public String FechaSaldo;
	  	public String IdMoneda;
	  	public BigDecimal Saldo;
	  	public BigDecimal SaldoDisponible;                                              	 
	  	public BigDecimal SaldoCargosPendientes;
	  	public BigDecimal SaldoAbonosPendientes;
	  	public Long IdEjecutivo;
		public String NombreEjecutivo;
		public String CodigoEjecutivo;
		public String CodigoSucursal;
		public String NombreSucursal;
     	public String NombreCliente;
     	public String DocumentoIdentidad;
     	public String NombrePortafolio;
     	public String PropositoPortafolio;
     	public String CodigoPortafolio;
     	public String CodigoMoneda;
     	public String NombreMoneda;
     	public BigDecimal SaldoEnGarantia;
     	public BigDecimal SaldoAPlazoSimultanea;
     	public BigDecimal SaldoAPlazoVentaCorta;
     	public BigDecimal SaldoAPlazoCompromiso;
     	public BigDecimal SaldoRetiro;
     	public BigDecimal SaldoRetenido;
     	public String Producto;

     	public Long IdProductoAgrupador;     	
	  	public Long IdMatrizAgrupador;
	  	public String EsADC;
		public BigDecimal MontoTransSolicitudesAporte = BigDecimal.ZERO; //nuevo
		public BigDecimal SaldoPendienteLibDAP;
		
		public SaldoPorFechaMovimiento() {
	  		this.SaldoRetenido = BigDecimal.ZERO;
	  		this.SaldoPendienteLibDAP = BigDecimal.ZERO;
	  	}
	  	
	  	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SaldoInicial{
		public Long IdCuentaInversion;
 		public Date FechaAnterior;
 		public BigDecimal SaldoAnterior;
 		public BigDecimal SaldoCargoPendienteAnterior;
 		public BigDecimal SaldoAbonoPendienteAnterior;
 		
 		public SaldoInicial(){
 			this.FechaAnterior = Fecha.toDate("0001-01-01");
 			this.SaldoAnterior = BigDecimal.ZERO;
 			this.SaldoCargoPendienteAnterior = BigDecimal.ZERO;
 			this.SaldoAbonoPendienteAnterior = BigDecimal.ZERO;
 		}
 		
 		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SaldoFecha{
		public String Id;
		public Long IdCuentaInversion;
 	  	public String Codigo;
 	  	public Long IdCliente;
 	  	public Long IdPortafolio;
	  	public String IdMoneda;
	  	public String CuentaInversionPrincipal;
	  	public Date Fecha;
	  	public BigDecimal SaldoFechaEfectiva;
	  	public BigDecimal SaldoCargosPendientesFechaEfectiva;
	  	public BigDecimal SaldoAbonosPendientesFechaEfectiva;
	  	public BigDecimal SaldoFechaMovimiento;
	  	public BigDecimal SaldoCargosPendientesFechaMovimiento;
	  	public BigDecimal SaldoAbonosPendientesFechaMovimiento;
	  	
	  	public SaldoFecha(){
	  		this.SaldoFechaEfectiva = BigDecimal.ZERO;
	  		this.SaldoCargosPendientesFechaEfectiva = BigDecimal.ZERO;
	  		this.SaldoAbonosPendientesFechaEfectiva = BigDecimal.ZERO;
	  		this.SaldoFechaMovimiento = BigDecimal.ZERO;
	  		this.SaldoCargosPendientesFechaMovimiento = BigDecimal.ZERO;
	  		this.SaldoAbonosPendientesFechaMovimiento = BigDecimal.ZERO;
	  	} 
	  	 
	  	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaSaldoCuentaInversion jin = gson.fromJson(in.toString(), EntradaSaldoCuentaInversion.class);
		SalidaSaldoCuentaInversion out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	} 
	
	@Trace
	public SalidaSaldoCuentaInversion logica(Connection con, EntradaSaldoCuentaInversion entrada) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		logger.info("Inicio servicio SaldoCuentaInversion");
		
		boolean barraCreada = false;
		if (entrada.p_BarraProgreso == null) {
            // No habilitado para servicio principal
            
        } else {
            entrada.p_BarraProgreso.actualizar(1L, "PROCESANDO", "Inicio consulta cuenta inversion");
        }
		
		SQL sql=new SQL();
		SalidaSaldoCuentaInversion out = new SalidaSaldoCuentaInversion();
		
		try {
			Date fDesde = Fecha.toDate(entrada.p_FechaDesde);
			Date fHasta = Fecha.toDate(entrada.p_FechaHasta);
			
			Set<Long> portafoliosUnicos = new HashSet<>();
			Set<String> monedasUnicas = new HashSet<>();

			for(InCuentaInversionConsultar cuenta : entrada.p_CuentaInversionAConsultar){
				if(cuenta.IdMoneda != null) monedasUnicas.add(cuenta.IdMoneda);
				if(cuenta.IdPortafolio != null) portafoliosUnicos.add(cuenta.IdPortafolio);
			}
			//Insertar portafolios en tabla cuenta inversion temporal
			long IdTrabajo = new Double(Math.random() * 10000000000L).longValue();
			
			List<InPortafolioConsultar> li = portafoliosUnicos.stream().map(p -> {
				 InPortafolioConsultar port = new InPortafolioConsultar(p);
			        port.IdTrabajo = IdTrabajo;
			        return port;
			}).collect(Collectors.toList());
			
			logger.debug("Inicio SQL_INSERT_PORTAFOLIOS");
			sql.batch(con, SQL_INSERT_PORTAFOLIOS, li, InPortafolioConsultar.class);
			
			/*Carga Datos Iniciales*/
			HashMap<String, Object> params = new HashMap<String, Object>();
			Long filtroMoneda = monedasUnicas.isEmpty() ? 1L : 0L;
			params.put("IdMoneda", new ArrayList<>(monedasUnicas));
			params.put("FiltroMoneda", filtroMoneda);
			
			logger.debug("Inicio SQL_SELECT_CUENTA_INVERSION");
			startTime = System.nanoTime();
			List<CuentaInversion> cuentasInversion = null;
			if (portafoliosUnicos.isEmpty()) {
				cuentasInversion = sql.query(con, SQL_SELECT_CUENTA_INVERSION_FULL, params, CuentaInversion.class);	
			}
			else {
				params.put("IdTrabajo", IdTrabajo);
				cuentasInversion = sql.query(con, SQL_SELECT_CUENTA_INVERSION, params, CuentaInversion.class);
			}
			if(entrada.p_BarraProgreso != null) {
				entrada.p_BarraProgreso.actualizar(30L,"PROCESANDO","Agrupando datos cuenta inversion");
			}
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_CUENTA_INVERSION");
			
			portafoliosUnicos.clear();
			monedasUnicas.clear();
			
			Set<Long> cuentaInversionFiltro = new HashSet<>();
			List<CuentaInversion> cuentaInversionTablaPaso = new ArrayList<CuentaInversion>();
			List<SaldoFecha> saldosFechaHasta = new ArrayList<SaldoFecha>();
			for(CuentaInversion cuenta : cuentasInversion){
				if(cuentaInversionFiltro.add(cuenta.IdCuentaInversion)){
					cuentaInversionTablaPaso.add(cuenta);

					SaldoFecha saldoFecha = copyPojoSaldoFecha(cuenta);
					saldoFecha.Id = cuenta.IdCuentaInversion + "_" + Fecha.stringToSqlDate(fHasta);
					saldoFecha.Fecha = fHasta;
					saldosFechaHasta.add(saldoFecha);
				}
			}
			cuentaInversionFiltro.clear();
			HashMap<Long,Object> hSaldoFechaHasta = Utils.List2Hash(saldosFechaHasta, "IdCuentaInversion");
			if(entrada.p_BarraProgreso != null) {
				entrada.p_BarraProgreso.actualizar(45L,"PROCESANDO","Consultando movimientos cuenta inversion");
			}
			
			for (CuentaInversion cuenta : cuentaInversionTablaPaso) {
				cuenta.IdTrabajo = IdTrabajo;
			}
			
			//Se utiliza tabla de paso por la cantidad de ids CuentaInversion
			logger.debug("INICIO SQL_INSERT_CUENTA_INVERSION_TABLA_PASO");
			sql.batch(con, SQL_INSERT_CUENTA_INVERSION_TABLA_PASO, cuentaInversionTablaPaso, CuentaInversion.class);
			
			params.clear();
			params.put("FechaDesde",entrada.p_FechaDesde);
			params.put("FechaHasta",entrada.p_FechaHasta);
			params.put("IdTrabajo", IdTrabajo);

			logger.debug("Inicio SQL_SELECT_MOVIMIENTOS y Agrupar movimientos");
			startTime = System.nanoTime();
			Map<Long, List<Movimientos>> movimientosAgrupados = sql.query(con, SQL_SELECT_MOVIMIENTOS, params, Movimientos.class).stream()
					.collect(Collectors.groupingBy( mov -> mov.IdCuentaInversion));
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_MOVIMIENTOS y AgruparMovimientos");
			
			logger.debug("Inicio SQL_SELECT_SALDO_ANTERIOR_EFECTIVA");
			startTime = System.nanoTime();
			List<SaldoInicial> saldosAnteriorEfectivo = sql.query(con, SQL_SELECT_SALDO_ANTERIOR_EFECTIVA, params, SaldoInicial.class);
			HashMap<Long, Object> cuentaFechaSaldoInicialFechaEfectiva = Utils.List2Hash(saldosAnteriorEfectivo, "IdCuentaInversion");
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_SALDO_ANTERIOR_EFECTIVA");
			
			logger.debug("Inicio SQL_SELECT_SALDO_ANTERIOR_SISTEMA");
			startTime = System.nanoTime();
			List<SaldoInicial> saldosAnteriorMovimiento = sql.query(con, SQL_SELECT_SALDO_ANTERIOR_SISTEMA, params, SaldoInicial.class);
			HashMap<Long, Object> cuentaFechaSaldoInicialFechaMovimiento = Utils.List2Hash(saldosAnteriorMovimiento, "IdCuentaInversion");
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_SALDO_ANTERIOR_SISTEMA");
			
			/*Completar rango fecha*/
			List<SaldoFecha> saldosFecha = new ArrayList<SaldoFecha>();
			for(CuentaInversion cuenta : cuentasInversion){
				Date fDesdePeriodo = Fecha.toDate(entrada.p_FechaDesde);
				while(fDesdePeriodo.toString().compareTo(fHasta.toString()) < 1){
					SaldoFecha saldoFecha = copyPojoSaldoFecha(cuenta);
					saldoFecha.Id = cuenta.IdCuentaInversion + "_" + Fecha.stringToSqlDate(fDesdePeriodo);
					saldoFecha.Fecha = fDesdePeriodo;
					saldosFecha.add(saldoFecha);
					fDesdePeriodo = Fecha.addDays(fDesdePeriodo, 1);
				}
			}
			if(entrada.p_BarraProgreso != null) {
				entrada.p_BarraProgreso.actualizar(60L,"PROCESANDO","Obteniendo saldos");
			}
			/*
			 * Obtener Saldo Por Fecha
			 * 
			*/
			/*Obtener Saldo Efectivo y Movimiento*/
			logger.debug("Inicio Obtener Saldo Efectivo y Movimiento");
			List<Movimientos> movVacio = new ArrayList<>();
			
			for(SaldoFecha saldoFecha : saldosFecha){
				movimientosAgrupados.getOrDefault(saldoFecha.IdCuentaInversion, movVacio).forEach(mov -> {
					if(mov.IdCuentaInversion.equals(saldoFecha.IdCuentaInversion) && mov.FechaEfectivaMovimiento.compareTo(fDesde) != -1 && mov.FechaEfectivaMovimiento.compareTo(saldoFecha.Fecha) != 1){
						saldoFecha.SaldoFechaEfectiva = saldoFecha.SaldoFechaEfectiva.add(mov.Monto, MathContext.DECIMAL128);
						saldoFecha.SaldoCargosPendientesFechaEfectiva = saldoFecha.SaldoCargosPendientesFechaEfectiva.add(mov.MontoCargoPendiente, MathContext.DECIMAL128);
						saldoFecha.SaldoAbonosPendientesFechaEfectiva = saldoFecha.SaldoAbonosPendientesFechaEfectiva.add(mov.MontoAbonoPendiente, MathContext.DECIMAL128);
					}
					if(mov.IdCuentaInversion.equals(saldoFecha.IdCuentaInversion) && mov.FechaSistema.compareTo(fDesde) != -1 && mov.FechaSistema.compareTo(saldoFecha.Fecha) != 1){
						saldoFecha.SaldoFechaMovimiento = saldoFecha.SaldoFechaMovimiento.add(mov.Monto, MathContext.DECIMAL128);
						saldoFecha.SaldoCargosPendientesFechaMovimiento = saldoFecha.SaldoCargosPendientesFechaMovimiento.add(mov.MontoCargoPendiente, MathContext.DECIMAL128);
						saldoFecha.SaldoAbonosPendientesFechaMovimiento = saldoFecha.SaldoAbonosPendientesFechaMovimiento.add(mov.MontoAbonoPendiente, MathContext.DECIMAL128);
					}
				});
				
				
				SaldoInicial saldoEfectivo = (SaldoInicial) cuentaFechaSaldoInicialFechaEfectiva.get(saldoFecha.IdCuentaInversion);
				if(saldoEfectivo != null){
					saldoFecha.SaldoFechaEfectiva = saldoFecha.SaldoFechaEfectiva.add(saldoEfectivo.SaldoAnterior, MathContext.DECIMAL128);
					saldoFecha.SaldoCargosPendientesFechaEfectiva = saldoFecha.SaldoCargosPendientesFechaEfectiva.add(saldoEfectivo.SaldoCargoPendienteAnterior, MathContext.DECIMAL128);
					saldoFecha.SaldoAbonosPendientesFechaEfectiva = saldoFecha.SaldoAbonosPendientesFechaEfectiva.add(saldoEfectivo.SaldoAbonoPendienteAnterior, MathContext.DECIMAL128);
				}
				
				SaldoInicial saldoMovimiento = (SaldoInicial) cuentaFechaSaldoInicialFechaMovimiento.get(saldoFecha.IdCuentaInversion);
				if(saldoMovimiento != null){
					saldoFecha.SaldoFechaMovimiento = saldoFecha.SaldoFechaMovimiento.add(saldoMovimiento.SaldoAnterior, MathContext.DECIMAL128);
					saldoFecha.SaldoCargosPendientesFechaMovimiento = saldoFecha.SaldoCargosPendientesFechaMovimiento.add(saldoMovimiento.SaldoCargoPendienteAnterior, MathContext.DECIMAL128);
					saldoFecha.SaldoAbonosPendientesFechaMovimiento = saldoFecha.SaldoAbonosPendientesFechaMovimiento.add(saldoMovimiento.SaldoAbonoPendienteAnterior, MathContext.DECIMAL128);
				}
				
			}
			logger.debug("Fin Obtener Saldo Efectivo y Movimiento");
			if(entrada.p_BarraProgreso != null) {
				entrada.p_BarraProgreso.actualizar(63L,"PROCESANDO","Obteniendo saldos por fecha efectiva");
			}
			//Retorna informacion del saldo de la cuenta inversion 
			/*
			 * Por Fecha Efectiva
			 */
			
			for(SaldoFecha saldoFecha : saldosFecha){
				if((saldoFecha.SaldoFechaEfectiva.compareTo(BigDecimal.ZERO) == 1 && "S".equals(entrada.p_SoloSaldoDisponible) && "N".equals(entrada.p_SoloSaldoNegativo) && "N".equals(entrada.p_SoloSaldoDisponibleNegativo ))
					|| (saldoFecha.SaldoFechaEfectiva.compareTo(BigDecimal.ZERO) == -1 && "S".equals(entrada.p_SoloSaldoNegativo) && "N".equals(entrada.p_SoloSaldoDisponible)  && "N".equals(entrada.p_SoloSaldoDisponibleNegativo ))
					|| (saldoFecha.SaldoFechaEfectiva.compareTo(BigDecimal.ZERO) != 0 && "N".equals(entrada.p_SoloSaldoNegativo) && "N".equals(entrada.p_SoloSaldoDisponible)  && "S".equals(entrada.p_SoloSaldoDisponibleNegativo ))
					|| ("N".equals(entrada.p_SoloSaldoNegativo) && "N".equals(entrada.p_SoloSaldoDisponible) && "N".equals(entrada.p_SoloSaldoDisponibleNegativo ))){
					SaldoFecha saldoFechaHasta = (SaldoFecha) hSaldoFechaHasta.get(saldoFecha.IdCuentaInversion);
					SaldoPorFechaEfectiva saldoPorFechaEfectiva = new SaldoPorFechaEfectiva();
					saldoPorFechaEfectiva.IdCuentaInversion = saldoFecha.IdCuentaInversion;
					saldoPorFechaEfectiva.CodigoCuentaInversion = saldoFecha.Codigo;
					saldoPorFechaEfectiva.IdCliente = saldoFecha.IdCliente;
					saldoPorFechaEfectiva.IdPortafolio = saldoFecha.IdPortafolio;
					saldoPorFechaEfectiva.FechaSaldo = Fecha.stringToSqlDate(saldoFecha.Fecha);
					saldoPorFechaEfectiva.IdMoneda = saldoFecha.IdMoneda;
					saldoPorFechaEfectiva.SaldoDisponible = saldoFecha.SaldoFechaEfectiva;
					saldoPorFechaEfectiva.SaldoCargosPendientes = saldoFecha.SaldoCargosPendientesFechaEfectiva;
					saldoPorFechaEfectiva.SaldoAbonosPendientes = saldoFecha.SaldoAbonosPendientesFechaEfectiva;
					if("S".equals(entrada.p_IncluyeCargosPendientes)){
						if("S".equals(entrada.p_IncluyeAbonosPendientes)){
							saldoPorFechaEfectiva.Saldo = saldoFecha.SaldoFechaEfectiva.subtract(saldoFecha.SaldoCargosPendientesFechaEfectiva, MathContext.DECIMAL128).add(saldoFecha.SaldoAbonosPendientesFechaEfectiva, MathContext.DECIMAL128);
						}else{
							saldoPorFechaEfectiva.Saldo = saldoFecha.SaldoFechaEfectiva.subtract(saldoFecha.SaldoCargosPendientesFechaEfectiva, MathContext.DECIMAL128);
						}
					}else{
						if("S".equals(entrada.p_IncluyeAbonosPendientes)){
							saldoPorFechaEfectiva.Saldo = saldoFecha.SaldoFechaEfectiva.add(saldoFecha.SaldoAbonosPendientesFechaEfectiva, MathContext.DECIMAL128);
						}else{
							saldoPorFechaEfectiva.Saldo = saldoFecha.SaldoFechaEfectiva;
						}
					}
					/* TOTALES POR FECHA HASTA */
					if(saldoFechaHasta.Fecha.compareTo(saldoFecha.Fecha) == 0){
						saldoFechaHasta.SaldoFechaEfectiva = saldoFechaHasta.SaldoFechaEfectiva.add(saldoFecha.SaldoFechaEfectiva, MathContext.DECIMAL128);
						saldoFechaHasta.SaldoCargosPendientesFechaEfectiva = saldoFechaHasta.SaldoCargosPendientesFechaEfectiva.add(saldoFecha.SaldoCargosPendientesFechaEfectiva, MathContext.DECIMAL128);
						saldoFechaHasta.SaldoAbonosPendientesFechaEfectiva = saldoFechaHasta.SaldoAbonosPendientesFechaEfectiva.add(saldoFecha.SaldoAbonosPendientesFechaEfectiva, MathContext.DECIMAL128);
						hSaldoFechaHasta.put(saldoFecha.IdCuentaInversion,saldoFechaHasta);
					}
					out.P_OUT_SALDOPORFECHAEFECTIVA.add(saldoPorFechaEfectiva);
				}
			}
			if(entrada.p_BarraProgreso != null) {
				entrada.p_BarraProgreso.actualizar(70L,"PROCESANDO","Obteniendo saldos por fecha movimiento");
			}
			/*
			 * Por Fecha Movimiento
			 */
			
			for(SaldoFecha saldoFecha : saldosFecha){
				if((saldoFecha.SaldoFechaEfectiva.compareTo(BigDecimal.ZERO) == 1 && "S".equals(entrada.p_SoloSaldoDisponible) && "N".equals(entrada.p_SoloSaldoNegativo) && "N".equals(entrada.p_SoloSaldoDisponibleNegativo ))
						|| (saldoFecha.SaldoFechaEfectiva.compareTo(BigDecimal.ZERO) == -1 && "S".equals(entrada.p_SoloSaldoNegativo) && "N".equals(entrada.p_SoloSaldoDisponible)  && "N".equals(entrada.p_SoloSaldoDisponibleNegativo ))
						|| (saldoFecha.SaldoFechaEfectiva.compareTo(BigDecimal.ZERO) != 0 && "N".equals(entrada.p_SoloSaldoNegativo) && "N".equals(entrada.p_SoloSaldoDisponible)  && "S".equals(entrada.p_SoloSaldoDisponibleNegativo ))
						|| ("N".equals(entrada.p_SoloSaldoNegativo) && "N".equals(entrada.p_SoloSaldoDisponible) && "N".equals(entrada.p_SoloSaldoDisponibleNegativo ))){
					SaldoFecha saldoFechaHasta = (SaldoFecha) hSaldoFechaHasta.get(saldoFecha.IdCuentaInversion);
					SaldoPorFechaMovimiento saldoPorFechaMovimiento = new SaldoPorFechaMovimiento();
					saldoPorFechaMovimiento.IdCuentaInversion = saldoFecha.IdCuentaInversion;
					saldoPorFechaMovimiento.CodigoCuentaInversion = saldoFecha.Codigo;
					saldoPorFechaMovimiento.IdCliente = saldoFecha.IdCliente;
					saldoPorFechaMovimiento.IdPortafolio = saldoFecha.IdPortafolio;
					saldoPorFechaMovimiento.FechaSaldo = Fecha.stringToSqlDate(saldoFecha.Fecha);
					saldoPorFechaMovimiento.IdMoneda = saldoFecha.IdMoneda;
					saldoPorFechaMovimiento.SaldoDisponible = saldoFecha.SaldoFechaMovimiento;
					saldoPorFechaMovimiento.SaldoCargosPendientes = saldoFecha.SaldoCargosPendientesFechaMovimiento;
					saldoPorFechaMovimiento.SaldoAbonosPendientes = saldoFecha.SaldoAbonosPendientesFechaMovimiento;
					if("S".equals(entrada.p_IncluyeCargosPendientes)){
						if("S".equals(entrada.p_IncluyeAbonosPendientes)){
							saldoPorFechaMovimiento.Saldo = saldoFecha.SaldoFechaMovimiento.subtract(saldoFecha.SaldoCargosPendientesFechaMovimiento, MathContext.DECIMAL128).add(saldoFecha.SaldoAbonosPendientesFechaMovimiento, MathContext.DECIMAL128);
						}else{
							saldoPorFechaMovimiento.Saldo = saldoFecha.SaldoFechaMovimiento.subtract(saldoFecha.SaldoCargosPendientesFechaMovimiento, MathContext.DECIMAL128);
						}
					}else{
						if("S".equals(entrada.p_IncluyeAbonosPendientes)){
							saldoPorFechaMovimiento.Saldo = saldoFecha.SaldoFechaMovimiento.add(saldoFecha.SaldoAbonosPendientesFechaMovimiento, MathContext.DECIMAL128);
						}else{
							saldoPorFechaMovimiento.Saldo = saldoFecha.SaldoFechaMovimiento;
						}
					}
					/* TOTALES POR FECHA HASTA */
					if(saldoFechaHasta.Fecha.compareTo(saldoFecha.Fecha) == 0){
						saldoFechaHasta.SaldoFechaMovimiento = saldoFechaHasta.SaldoFechaMovimiento.add(saldoFecha.SaldoFechaMovimiento, MathContext.DECIMAL128);
						saldoFechaHasta.SaldoCargosPendientesFechaMovimiento = saldoFechaHasta.SaldoCargosPendientesFechaMovimiento.add(saldoFecha.SaldoCargosPendientesFechaMovimiento, MathContext.DECIMAL128);
						saldoFechaHasta.SaldoAbonosPendientesFechaMovimiento = saldoFechaHasta.SaldoAbonosPendientesFechaMovimiento.add(saldoFecha.SaldoAbonosPendientesFechaMovimiento, MathContext.DECIMAL128);
						hSaldoFechaHasta.put(saldoFecha.IdCuentaInversion,saldoFechaHasta);
					}
					out.P_OUT_SALDOPORFECHAMOVIMIENTO.add(saldoPorFechaMovimiento);
				}
			}
			if(entrada.p_BarraProgreso != null) {
				entrada.p_BarraProgreso.actualizar(90L,"PROCESANDO","Obteniendo saldo disponible");
			}
			/*
			 * Saldo Inicial
			 */
			for(CuentaInversion cuenta : cuentasInversion){
				SaldoFecha saldoFechaHasta = (SaldoFecha) hSaldoFechaHasta.get(cuenta.IdCuentaInversion);
				SaldoInicial saldoEfectivo = (SaldoInicial) cuentaFechaSaldoInicialFechaEfectiva.get(cuenta.IdCuentaInversion);
				SaldoInicial saldoMovimiento = (SaldoInicial) cuentaFechaSaldoInicialFechaMovimiento.get(cuenta.IdCuentaInversion);
				SaldoInicialFinal saldoInicialFinal = new SaldoInicialFinal();
				
				if (saldoEfectivo == null) {
					saldoEfectivo = new SaldoInicial();
				}
				if (saldoMovimiento == null) {
					saldoMovimiento = new SaldoInicial();
				}
				
				saldoInicialFinal.IdCuentaInversion = cuenta.IdCuentaInversion;
				if("S".equals(entrada.p_IncluyeCargosPendientes)){
					if("S".equals(entrada.p_IncluyeAbonosPendientes)){
						saldoInicialFinal.SaldoEfectivoInicial = saldoEfectivo.SaldoAnterior.subtract(saldoEfectivo.SaldoCargoPendienteAnterior, MathContext.DECIMAL128).add(saldoEfectivo.SaldoAbonoPendienteAnterior, MathContext.DECIMAL128);
						saldoInicialFinal.SaldoEfectivoFinal = saldoFechaHasta.SaldoFechaEfectiva.subtract(saldoFechaHasta.SaldoCargosPendientesFechaEfectiva, MathContext.DECIMAL128).add(saldoFechaHasta.SaldoAbonosPendientesFechaEfectiva);
						saldoInicialFinal.SaldoMovimientoInicial = saldoMovimiento.SaldoAnterior.subtract(saldoMovimiento.SaldoCargoPendienteAnterior, MathContext.DECIMAL128).add(saldoMovimiento.SaldoAbonoPendienteAnterior, MathContext.DECIMAL128);
						saldoInicialFinal.SaldoMovimientoFinal = saldoFechaHasta.SaldoFechaMovimiento.subtract(saldoFechaHasta.SaldoCargosPendientesFechaMovimiento, MathContext.DECIMAL128).add(saldoFechaHasta.SaldoAbonosPendientesFechaMovimiento);
					}else{
						saldoInicialFinal.SaldoEfectivoInicial = saldoEfectivo.SaldoAnterior.subtract(saldoEfectivo.SaldoCargoPendienteAnterior, MathContext.DECIMAL128);
						saldoInicialFinal.SaldoEfectivoFinal = saldoFechaHasta.SaldoFechaEfectiva.subtract(saldoFechaHasta.SaldoCargosPendientesFechaEfectiva, MathContext.DECIMAL128);
						saldoInicialFinal.SaldoMovimientoInicial = saldoMovimiento.SaldoAnterior.subtract(saldoMovimiento.SaldoCargoPendienteAnterior, MathContext.DECIMAL128);
						saldoInicialFinal.SaldoMovimientoFinal = saldoFechaHasta.SaldoFechaMovimiento.subtract(saldoFechaHasta.SaldoCargosPendientesFechaMovimiento, MathContext.DECIMAL128);
					}
				}else{
					if("S".equals(entrada.p_IncluyeAbonosPendientes)){
						saldoInicialFinal.SaldoEfectivoInicial = saldoEfectivo.SaldoAnterior.add(saldoEfectivo.SaldoAbonoPendienteAnterior, MathContext.DECIMAL128);
						saldoInicialFinal.SaldoEfectivoFinal = saldoFechaHasta.SaldoFechaEfectiva.add(saldoFechaHasta.SaldoAbonosPendientesFechaEfectiva);
						saldoInicialFinal.SaldoMovimientoInicial = saldoMovimiento.SaldoAnterior.add(saldoMovimiento.SaldoAbonoPendienteAnterior, MathContext.DECIMAL128);
						saldoInicialFinal.SaldoMovimientoFinal = saldoFechaHasta.SaldoFechaMovimiento.add(saldoFechaHasta.SaldoAbonosPendientesFechaMovimiento);
					}else{
						saldoInicialFinal.SaldoEfectivoInicial = saldoEfectivo.SaldoAnterior;
						saldoInicialFinal.SaldoEfectivoFinal = saldoFechaHasta.SaldoFechaEfectiva;
						saldoInicialFinal.SaldoMovimientoInicial = saldoMovimiento.SaldoAnterior;
						saldoInicialFinal.SaldoMovimientoFinal = saldoFechaHasta.SaldoFechaMovimiento;
					}
				}
				out.P_OUT_SALDOINICIALFINAL.add(saldoInicialFinal);
			}
			
			SQL.exec(con, SQL_DELETE_CUENTA_INVERSION_TABLA_PASO);
		}
		catch(Exception e){
		    logger.error("Error",e);
			throw e;
		}
		if(entrada.p_BarraProgreso != null) {
			entrada.p_BarraProgreso.actualizar(100L,"PROCESANDO","Fin consulta cuenta inversion");
		}
		logger.info("Termino del servicio SaldoCuentaInversion, tiempo:" + (System.currentTimeMillis() - init));
		
		return out;
	}
	
	private SaldoFecha copyPojoSaldoFecha(CuentaInversion cuenta) {
		SaldoFecha saldoFecha = new SaldoFecha();
		saldoFecha.IdCuentaInversion = cuenta.IdCuentaInversion;
		saldoFecha.Codigo = cuenta.Codigo;
		saldoFecha.IdCliente = cuenta.IdCliente;
		saldoFecha.IdPortafolio = cuenta.IdPortafolio;
		saldoFecha.IdMoneda = cuenta.IdMoneda;
		saldoFecha.CuentaInversionPrincipal = cuenta.CuentaInversionPrincipal;
		return saldoFecha;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String json = "{\"p_IdEmpresa\":1,\"p_FechaDesde\":\"2019-05-31\",\"p_FechaHasta\":\"2019-05-31\",\"p_SoloSaldoNegativo\":\"N\",\"p_SoloSaldoDisponible\":\"N\",\"p_IncluyeCargosPendientes\":\"N\",\"p_IncluyeAbonosPendientes\":\"N\",\"p_CuentaInversionAConsultar\":[{\"IdEjecutivo\":null,\"IdPortafolio\":1450068,\"IdMoneda\":null}]}";
			//String json2 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CTA\",\"p_UsaReglasNegocio\":\"S\",\"p_IncluyeOtrosSaldos\":\"S\",\"p_IdPais\":44,\"p_FechaDesde\":\"2018-10-19\",\"p_FechaHasta\":\"2018-10-19\",\"p_IncluyeProductoApv\":\"S\",\"p_IdCliente\":524891,\"p_IdPortafolio\":1450068,\"p_IdProductoAgrupador\":null,\"p_CuentaInversionAConsultar\":[{\"IdEjecutivo\":null,\"IdPortafolio\":1450068,\"IdMoneda\":null}]}"
			EntradaSaldoCuentaInversion Objin = gson.fromJson(json, EntradaSaldoCuentaInversion.class);
			SaldoCuentaInversion  demo=new SaldoCuentaInversion();				
			try {
				SalidaSaldoCuentaInversion ObjOut = demo.logica(con, Objin);
				//con.commit();
				System.out.println(ObjOut.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
    	}
		System.out.println("== END ==");
	}
}
