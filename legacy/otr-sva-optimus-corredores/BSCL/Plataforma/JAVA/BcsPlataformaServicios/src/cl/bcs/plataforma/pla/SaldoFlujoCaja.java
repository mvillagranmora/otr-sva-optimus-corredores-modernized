package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
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

import cl.bcs.cuentaInversion.SaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.EntradaSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaEfectiva;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SalidaSaldoCuentaInversion;
import cl.bcs.plataforma.pla.utils.BarraProgresoWrapper;
import cl.bcs.plataforma.pla.utils.BarraProgreso;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.tesoreria.SaldoPendienteDeLiquidar;
import cl.bcs.tesoreria.SaldoPendienteDeLiquidar.EntradaSaldoPendienteDeLiquidar;
import cl.bcs.tesoreria.SaldoPendienteDeLiquidar.SaldoPorFechaLiquidacion;
import cl.bcs.tesoreria.SaldoPendienteDeLiquidar.SalidaSaldoPendienteDeLiquidar;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;


public class SaldoFlujoCaja implements JavaCall {
	static final Logger logger = LogManager.getLogger(SaldoFlujoCaja.class);
	
	private final String SQL_SELECT_CONFIGURACIONES = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectConfiguraciones.sql");
	private final String SQL_SELECT_CLIENTE_PORTAFOLIO = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectClientePortafolio.sql");
	private final String SQL_SELECT_CUENTA_INVERSION = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectCuentaInversion.sql");
	private final String SQL_INSERT_PORTAFOLIOS_PROCESO_TABLA_PASO = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_InsertPortafoliosProcesoTablaPaso.sql");
	private final String SQL_DELETE_PORTAFOLIOS_PROCESO_TABLA_PASO = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_DeletePortafoliosProcesoTablaPaso.sql");

	private final String SQL_SELECT_ORDENES_EN_TRANSITO_COMPRAS = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectOrdenesEnTransito.sql");
	private final String SQL_SELECT_SALDO_ASIGNACIONES_EN_TRANSITO = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectAsignacionesEnTransito.sql");
	private final String SQL_SELECT_SALDO_MOVIMIENTOS_FACTURACION = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectMovimientoFacturacion.sql");
	
	
	
	
	//FFMM
	private final String SQL_SELECT_APORTES_EN_TRANSITO = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectAportesEnTransito.sql");
	private final String SQL_SELECT_APORTES_EN_TRANSITO_POR_REINVERSION = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectAportesEnTransitoPorReinversion.sql");
	private final String SQL_SELECT_APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectAportesEnTransitoInstruccionLiquidada.sql");
	private final String SQL_SELECT_RESCATES_EN_TRANSITO = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectRescateEnTransito.sql");
	
	
	private final String SQL_SELECT_INVERSION_EN_LIQUIDEZ = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectInversionEnLiquidez.sql");
	private final String SQL_SELECT_EVENTO_CAPITAL = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectEventoCapital.sql");
	
	private final String SQL_SELECT_SALDO_SIMULTANEAS_EMP_PRES = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectSimultaneasEmpPre.sql");
	
	private final String SQL_SELECT_MESAS = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectMesas.sql");
	private final String SQL_SELECT_LINEA_SOBREGIRO = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectLineaSobregiroPortafolio.sql");
	private final String SQL_SELECT_LINEA_SOBREGIRO_TESORERIA = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectLineaSobregiroTesoreriaPortafolio.sql");
	private final String SQL_SELECT_ENTREGAMOS_SPOT = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectEntregamosSpot.sql");
	private final String SQL_SELECT_RECIBIMOS_SPOT = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectRecibimosSpot.sql");

	private final String SQL_SELECT_SALDO_RETENIDO = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectSaldoRetenido.sql");
	private final String SQL_SELECT_SALDO_RETENIDO_CARGOS_POR_LIQUIDAR = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectSaldoRetenidoCargosPorLiquidar.sql");
	private final String SQL_SELECT_SALDO_RETENIDO_ABONOS_POR_LIQUIDAR = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectSaldoRetenidoAbonosPorLiquidar.sql");
	private final String SQL_SELECT_APORTES_EN_TRANSITO_PAC = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectAportesEnTransitoPAC.sql");
	private final String SQL_SELECT_CUSTODIA_MONEY_MARKET = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectCustodiaMoneyMarket.sql");
	private final String SQL_SELECT_RECIBIMOS_SPOT_PAC = loadResourceToString("cl/bcs/plataforma/pla/sql/SaldoFlujoCaja_SelectRecibimosSpotPAC.sql");
	/*
	 * 
	 * Clases utilitarias que representan objetos IN/OUT
	 */
	public static class ServiceEntrada {
		public Long								P_IDEMPRESA;
		public String							P_FECHADESDE;
		public String							P_FECHAHASTA;
		public String							P_USO;
		public String							P_DESPLEGARENUI;
		public String							P_AGRUPARPORORIGEN;
		public BarraProgreso					p_BarraProgreso;
		public List<ObjPORTAFOLIOACONSULTAR>	P_PORTAFOLIOACONSULTAR;

		public ServiceEntrada() {
			this.P_PORTAFOLIOACONSULTAR = new ArrayList<ObjPORTAFOLIOACONSULTAR>();
			this.P_DESPLEGARENUI = "N";
			this.P_AGRUPARPORORIGEN = "N";
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ServiceSalida {
		public List<ObjFLUJOCAJA>			P_OUT_FLUJOCAJA;
		public List<ObjFLUJOCAJADETALLADO>	P_OUT_FLUJOCAJADETALLADO;
		public Long						P_OUT_ERRORESCONTADOR;
		public List<ObjERRORESDETALLE>		P_OUT_ERRORESDETALLE;

		public ServiceSalida() {
			this.P_OUT_FLUJOCAJA = new ArrayList<>();
			this.P_OUT_FLUJOCAJADETALLADO = new ArrayList<>();
			this.P_OUT_ERRORESDETALLE = new ArrayList<>();
			this.P_OUT_ERRORESCONTADOR = 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	/*
	 * 
	 * Clases utilitarias que representan objetos de procesamiento
	 * 
	 */
	public static class ObjPORTAFOLIOACONSULTAR {
		public Long		IdPortafolio;
		public String	IdMoneda;
		
		public ObjPORTAFOLIOACONSULTAR(Long idPortafolio, String idMoneda) {
			this.IdPortafolio = idPortafolio;
			this.IdMoneda = idMoneda;
		}
		
		public ObjPORTAFOLIOACONSULTAR() {
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}

		@Override
		public boolean equals(Object o2) {
			ObjPORTAFOLIOACONSULTAR oo = (ObjPORTAFOLIOACONSULTAR)o2;
			return o2 == null? this == null : this.IdPortafolio == oo.IdPortafolio && this.IdMoneda == oo.IdMoneda;
		}
	}

	public static class ObjFLUJOCAJA {
		public String		NombreUso;
		public String		FechaSaldo;
		public Long			IdPortafolio;
		public String		IdMoneda;
		public BigDecimal	Saldo;
		
		public ObjFLUJOCAJA(){
			this.Saldo = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ObjFLUJOCAJADETALLADO {
		public String		FechaSaldo;
		public Long			IdPortafolio;
		public String		IdMoneda;
		public Long			IdUsoFlujoCaja;
		public String		NombreUso;
		public Long			IdConfiguracionFlujoCaja;
		public String		ItemCalculo;
		public String		FuncionOperador;
		public BigDecimal	Monto;
		
		public ObjFLUJOCAJADETALLADO(){
			this.IdConfiguracionFlujoCaja = 0L;
			this.Monto = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class ObjERRORESDETALLE {
		public String	Codigo;
		public String	Descripcion;
	}
	
	public static class ConfiguracionFlujoCaja {
		public Long IdUsoFlujoCaja;
		public String NombreUso;
		public String ItemCalculo;
		public String ItemOrigen;
		public String FuncionOperador;
		public String DesplegarEnUI;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class PortafolioProceso {
		public String Id;
		public Long IdPersona;
		public Long IdPortafolio;
		public String IdMoneda;
		
		public PortafolioProceso() {
			
		}
		
		public PortafolioProceso(long idPersona, long idPortafolio, String idMoneda) {
			this.IdPersona = idPersona;
			this.IdPortafolio = idPortafolio;
			this.IdMoneda = idMoneda;
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class RangoFecha{
		public String Id;
		public Long IdPersona;
		public Long IdPortafolio;
		public String IdMoneda;
		public Long IdUsoFlujoCaja;
		public String NombreUso;
		public String Item;
		public String ItemCalculo;
		public String ItemOrigen;
		public Long FuncionOperador;
		public String FuncionOperadorString;
		public String DesplegarEnUI;
		public String UsoSolicitado;
		public Date Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosFlujoCaja{
		public String Id;
		public Date FechaLiquidacion;
		public Long IdPortafolio;
		public String IdMoneda;
		public BigDecimal Monto;
		public BigDecimal MontoCompra;
		public BigDecimal MontoVenta;
		public String ItemCalculo;
		
		public String TipoCalculoFlujoCaja;  //Tipo de Calculo de flujo Caja por Fecha liquidacion o fecha de adquisicion (solo para compras)
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	/*
	 * Objeto de transporte
	 */
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = getGsonBuilder();
		ServiceEntrada in = gson.fromJson(jin.toString(), ServiceEntrada.class);
		ServiceSalida out = logica(con, in);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	

	/*
	 * Logica Servicio
	 * 
	 */
	@Trace
	public ServiceSalida logica(Connection con, ServiceEntrada in) throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		logger.info("Inicio servicio SaldoFlujoCaja");
		
		boolean barraCreada = false;
		if (in.p_BarraProgreso == null) {
            // No habilitado para servicio principal
            
        } else {
            in.p_BarraProgreso.actualizar(0L, "PROCESANDO", "Consulta saldo caja");
        }
		
		SQL sql = new SQL();
		ServiceSalida out = new ServiceSalida();
		
		try {
			Date fDesde = Fecha.toDate(in.P_FECHADESDE);
			Date fHasta = Fecha.toDate(in.P_FECHAHASTA);
			
			Set<Long> idPortafolios = new HashSet<Long>();
			List<String> idMonedas = new ArrayList<String>();
			
			HashMap<Long, Boolean> idsPortafolioFiltro = new HashMap<Long, Boolean>();
			HashMap<Long, Boolean> idsPersonaFiltro = new HashMap<Long, Boolean>();
			HashMap<String, Boolean> idsMonedaFiltro = new HashMap<String, Boolean>();
			
			
			//Obtiene parametro que indica si Flujo Caja se debe calcular por Fecha Liquidacion o Fecha Adquisicion (solo para compras)
			String tipoCalculoFlujoCaja = "";
			try
			{
				tipoCalculoFlujoCaja = ConfiguracionSistema.getParametroConfiguracionString(con, in.P_IDEMPRESA, "PLATAFORMA", "PLA.TipoCalculoFlujoCaja");
			}
			catch (Exception e)
			{
				tipoCalculoFlujoCaja = "LIQUIDACION";
			}
			
			String tipoCalculoFlujoCajaSpot = "";
			try
			{
				tipoCalculoFlujoCajaSpot = ConfiguracionSistema.getParametroConfiguracionString(con, in.P_IDEMPRESA, "PLATAFORMA", "PLA.TipoCalculoFlujoCajaSpot");
				tipoCalculoFlujoCajaSpot = tipoCalculoFlujoCajaSpot.toUpperCase();
			}
			catch (Exception e)
			{
				tipoCalculoFlujoCajaSpot = "LIQUIDACION";
			}
			
			
			for(ObjPORTAFOLIOACONSULTAR port : in.P_PORTAFOLIOACONSULTAR){
				if (!idsMonedaFiltro.containsKey(port.IdMoneda)) {
					idsMonedaFiltro.put(port.IdMoneda, true);
					idMonedas.add(port.IdMoneda);
				}
			}
			List<ObjPORTAFOLIOACONSULTAR> portafoliosABuscar = in.P_PORTAFOLIOACONSULTAR.parallelStream().filter(por -> idPortafolios.add(por.IdPortafolio)).collect(Collectors.toList());
			
			logger.debug("Inicio SQL_SELECT_CUENTA_INVERSION / SQL_SELECT_CLIENTE_PORTAFOLIO");
			startTime = System.nanoTime();
			List<PortafolioProceso> portafoliosBusqueda = new ArrayList<PortafolioProceso>();
			
			HashMap<String, Object> params = new HashMap<String, Object>();
			sql.batch(con, SQL_INSERT_PORTAFOLIOS_PROCESO_TABLA_PASO, portafoliosABuscar, ObjPORTAFOLIOACONSULTAR.class);
			if("CUENTA_INVERSION".equals(in.P_USO)){
				params.put("IdMoneda", idMonedas);
				logger.debug("query cuenta inversion");
				logger.debug("INICIO::SQL_SELECT_CUENTA_INVERSION");
				portafoliosBusqueda = sql.query(con, SQL_SELECT_CUENTA_INVERSION, params, PortafolioProceso.class);
				logger.debug("FIN::SQL_SELECT_CUENTA_INVERSION");
			}
			else{
				logger.debug("query cliente portafolio");
				logger.debug("INICIO::SQL_SELECT_CLIENTE_PORTAFOLIO");
				//se cambia a preparedstatement por rendimiento, con reflexion se cae.
				try(PreparedStatement ps = con.prepareStatement(SQL_SELECT_CLIENTE_PORTAFOLIO)){
					try(ResultSet rs = ps.executeQuery()){
						while(rs.next()){
							portafoliosBusqueda.add(new PortafolioProceso(rs.getLong("IdPersona"), rs.getLong("IdPortafolio"), rs.getString("IdMoneda")));
						}
					}
				}
				logger.debug("FIN::SQL_SELECT_CLIENTE_PORTAFOLIO");
			}
			SQL.exec(con, SQL_DELETE_PORTAFOLIOS_PROCESO_TABLA_PASO);
			logger.debug("(" + (System.nanoTime() - startTime)/1000000 + ") Fin SQL_SELECT_CUENTA_INVERSION / SQL_SELECT_CLIENTE_PORTAFOLIO, COUNT: "+ portafoliosBusqueda.size());
			
			HashMap<String, PortafolioProceso> portafoliosBusquedaHash = new HashMap<String, PortafolioProceso>();
			for (PortafolioProceso portafolio : portafoliosBusqueda) {
				portafolio.Id = portafolio.IdPortafolio+"_"+portafolio.IdMoneda;
				
				if (!portafoliosBusquedaHash.containsKey(portafolio.Id)) {
					portafoliosBusquedaHash.put(portafolio.Id, portafolio);
				}
			}
			
			List<PortafolioProceso> portafoliosProceso = new ArrayList<PortafolioProceso>();
			idsPortafolioFiltro.clear();
			idsMonedaFiltro.clear();
			idPortafolios.clear();
			idMonedas.clear();
			portafoliosBusqueda.clear();
			HashMap<String, Boolean> idsPortafolioMonedaFiltro = new HashMap<String, Boolean>();
			//Buscamos los portafolios a consultar
			for(ObjPORTAFOLIOACONSULTAR port : in.P_PORTAFOLIOACONSULTAR){
				PortafolioProceso pp = portafoliosBusquedaHash.get(port.IdPortafolio+"_"+port.IdMoneda);
				if (pp != null) {
					if (!idsPortafolioFiltro.containsKey(pp.IdPortafolio) || !idsMonedaFiltro.containsKey(pp.IdMoneda) || !idsPersonaFiltro.containsKey(pp.IdPersona) || !idsPortafolioMonedaFiltro.containsKey(pp.Id)) {
						portafoliosProceso.add(pp);
					}
					if (!idsPortafolioFiltro.containsKey(pp.IdPortafolio)) {
						idsPortafolioFiltro.put(pp.IdPortafolio, true);
						idPortafolios.add(pp.IdPortafolio);
					}
					if (!idsMonedaFiltro.containsKey(pp.IdMoneda)) {
						idsMonedaFiltro.put(pp.IdMoneda, true);
						idMonedas.add(pp.IdMoneda);
					}
					if (!idsPersonaFiltro.containsKey(pp.IdPersona)) {
						idsPersonaFiltro.put(pp.IdPersona, true);
					}
					if (!idsPortafolioMonedaFiltro.containsKey(pp.Id)) {
						idsPortafolioMonedaFiltro.put(port.IdPortafolio+"_"+port.IdMoneda, true);
					}
				}
			}
			
			params.clear();
			params.put("IdEmpresa", in.P_IDEMPRESA);
			params.put("Uso", in.P_USO);
			logger.debug("INICIO::SQL_SELECT_CONFIGURACIONES");
			List<ConfiguracionFlujoCaja> configuracionFlujoCaja = sql.query(con, SQL_SELECT_CONFIGURACIONES, params, ConfiguracionFlujoCaja.class);
			logger.debug("FIN::SQL_SELECT_CONFIGURACIONES");
			
			/*Completar rango fecha*/
			List<ConfiguracionFlujoCaja> configuracionFlujoCajaFilter = configuracionFlujoCaja.stream()
					.filter(c -> (in.P_USO == null || in.P_USO.equals(c.NombreUso)) && (!"S".equals(in.P_DESPLEGARENUI) || ("S".equals(in.P_DESPLEGARENUI) && in.P_DESPLEGARENUI.equals(c.DesplegarEnUI))))
					.collect(Collectors.toList());
			
			List<RangoFecha> saldosFecha = new ArrayList<RangoFecha>();
			EntradaSaldoCuentaInversion entradaSaldoCuentaInversion = new EntradaSaldoCuentaInversion();
			EntradaSaldoPendienteDeLiquidar entradaSaldoPendienteDeLiquidar = new EntradaSaldoPendienteDeLiquidar();
			
			for(PortafolioProceso pp : portafoliosProceso){
				for(ConfiguracionFlujoCaja conf : configuracionFlujoCajaFilter){
					Date fDesdePeriodo = Fecha.toDate(in.P_FECHADESDE);
					while(fDesdePeriodo.compareTo(fHasta) != 1){
						RangoFecha saldoFecha = new RangoFecha();
						//saldoFecha = Utils.copyPojo(pp, saldoFecha);
						saldoFecha = getPojoSaldoFecha(pp, conf, saldoFecha);
						saldoFecha.Item = "N".equals(in.P_AGRUPARPORORIGEN) ? conf.ItemCalculo : conf.ItemOrigen;
						saldoFecha.FuncionOperador = "-".equals(conf.FuncionOperador) ? -1L : 1L;
						saldoFecha.Fecha = fDesdePeriodo;
						saldosFecha.add(saldoFecha);
						fDesdePeriodo = Fecha.addDays(fDesdePeriodo, 1);
					}
				}
				
				//Saldo Cuenta Inversion
				SaldoCuentaInversion.InCuentaInversionConsultar inSaldoCuentaInversion = new SaldoCuentaInversion.InCuentaInversionConsultar();
				inSaldoCuentaInversion.IdMoneda = pp.IdMoneda;
				inSaldoCuentaInversion.IdPortafolio = pp.IdPortafolio;
				entradaSaldoCuentaInversion.p_CuentaInversionAConsultar.add(inSaldoCuentaInversion);
				
				//Saldo Tesoreria sin liquidar
				SaldoPendienteDeLiquidar.SocioConsulta inSocioConsulta = new SaldoPendienteDeLiquidar.SocioConsulta();
				inSocioConsulta.IdMoneda = pp.IdMoneda;
				inSocioConsulta.IdPortafolio = pp.IdPortafolio;
				inSocioConsulta.IdPersona = pp.IdPersona;
				entradaSaldoPendienteDeLiquidar.p_SociosAConsultar.add(inSocioConsulta);
			}
			
			//Saldo Cuenta Inversion
			SaldoCuentaInversion saldoCuentaInversion = new SaldoCuentaInversion();
			entradaSaldoCuentaInversion.p_IdEmpresa = in.P_IDEMPRESA;
			entradaSaldoCuentaInversion.p_FechaDesde = in.P_FECHADESDE;
			entradaSaldoCuentaInversion.p_FechaHasta = in.P_FECHAHASTA;
			entradaSaldoCuentaInversion.p_SoloSaldoNegativo = "N";
			entradaSaldoCuentaInversion.p_IncluyeCargosPendientes = "S";
			entradaSaldoCuentaInversion.p_IncluyeAbonosPendientes = "N";
			
			//Saldo Tesoreria sin liquidar
			SaldoPendienteDeLiquidar saldoPendienteDeLiquidar = new SaldoPendienteDeLiquidar();
			entradaSaldoPendienteDeLiquidar.p_IdEmpresa = in.P_IDEMPRESA;
			entradaSaldoPendienteDeLiquidar.p_FechaDesde = in.P_FECHADESDE;
			entradaSaldoPendienteDeLiquidar.p_FechaHasta = in.P_FECHAHASTA;
			
			//logger.debug("entradaSaldoCuentaInversion : " + entradaSaldoCuentaInversion);
			//logger.debug("entradaSaldoPendienteDeLiquidar : " + entradaSaldoPendienteDeLiquidar);
			{
				if(in.p_BarraProgreso != null) {
					in.p_BarraProgreso.actualizar(5L,"PROCESANDO","Preparando consulta de saldo");
					BarraProgreso subBarraSaldoCuentaInversion = in.p_BarraProgreso.subrango(5, 82);
					entradaSaldoCuentaInversion.p_BarraProgreso = subBarraSaldoCuentaInversion;
				}
			}
			
			SalidaSaldoCuentaInversion salidaSaldoCuentaInversion = saldoCuentaInversion.logica(con, entradaSaldoCuentaInversion);
			SalidaSaldoPendienteDeLiquidar salidaSaldoPendienteDeLiquidar = saldoPendienteDeLiquidar.logica(con, entradaSaldoPendienteDeLiquidar);
			
			HashMap<String, SaldoPorFechaEfectiva> salidaSaldoCuentaInversionHash = new HashMap<String, SaldoPorFechaEfectiva>();
			for(SaldoPorFechaEfectiva saldo : salidaSaldoCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA){
				if (!salidaSaldoCuentaInversionHash.containsKey(saldo.IdPortafolio+"_"+saldo.IdMoneda+"_"+saldo.FechaSaldo)) {
					salidaSaldoCuentaInversionHash.put(saldo.IdPortafolio+"_"+saldo.IdMoneda+"_"+saldo.FechaSaldo, saldo);
				}
			}
			
			
			HashMap<String, SaldoPorFechaLiquidacion> salidaSaldoPendienteDeLiquidarHash = new HashMap<String, SaldoPorFechaLiquidacion>();
			for(SaldoPorFechaLiquidacion saldo : salidaSaldoPendienteDeLiquidar.P_OUT_SALDOPORFECHALIQUIDACION){
				if (!salidaSaldoPendienteDeLiquidarHash.containsKey(saldo.IdPortafolio+"_"+saldo.IdMoneda+"_"+saldo.FechaSaldo)) {
					salidaSaldoPendienteDeLiquidarHash.put(saldo.IdPortafolio+"_"+saldo.IdMoneda+"_"+saldo.FechaSaldo, saldo);
				}
			}
			
			//Delete a tabla de paso para para limpiarla
			SQL.exec(con, SQL_DELETE_PORTAFOLIOS_PROCESO_TABLA_PASO);

			//Insert a tabla de paso para usar en las querys siguientes
			sql.batch(con, SQL_INSERT_PORTAFOLIOS_PROCESO_TABLA_PASO, portafoliosProceso, PortafolioProceso.class);
			
			HashMap<String, Boolean> queryFiltro = new HashMap<String, Boolean>();
			List<DatosFlujoCaja> datosFlujoCaja = new ArrayList<DatosFlujoCaja>();
			params.clear();
			/*params.put("IdPortafolio", idPortafolios);
			params.put("IdMoneda", idMonedas);*/
			params.put("FechaDesde", in.P_FECHADESDE);
			params.put("FechaHasta", in.P_FECHAHASTA);
			params.put("IdEmpresa",in.P_IDEMPRESA);
			
			
			params.put("TipoCalculoFlujoCaja",tipoCalculoFlujoCaja);  //Indica si el calculo es por fecha de liquidacion o Adquisicion (sola para compras)
			  
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(50L,"PROCESANDO","Agrupando consulta de saldo");
			}
			

			
			for(ConfiguracionFlujoCaja conf : configuracionFlujoCaja){
				String tipoConsulta = tipoQuery(conf.ItemCalculo);
				String query = "";
				if (tipoConsulta != null && (query = obtenerConsulta(tipoConsulta)) != null && !queryFiltro.containsKey(tipoConsulta)) {
					queryFiltro.put(tipoConsulta, true);
					if ((tipoConsulta.equals("ENTREGAMOS_OPERACION_SPOT") || tipoConsulta.equals("RECIBIMOS_OPERACION_SPOT")|| tipoConsulta.equals("FACTURAS"))) {
						params.put("TipoCalculoFlujoCajaSpot",tipoCalculoFlujoCajaSpot);						
					}
					logger.debug("Inicio sql.query : tipoConsulta = " + tipoConsulta);
					long startTime1 = System.nanoTime();
					datosFlujoCaja.addAll(sql.query(con, query, params, DatosFlujoCaja.class));					
					logger.debug("(" + (System.nanoTime() - startTime1)/1000000 + ") Fin sql.query : " + tipoConsulta);
				if ((tipoConsulta.equals("ENTREGAMOS_OPERACION_SPOT") || tipoConsulta.equals("RECIBIMOS_OPERACION_SPOT")|| tipoConsulta.equals("FACTURAS"))) {
						params.remove("TipoCalculoFlujoCajaSpot");						
					}
				}
			}
						
			
			HashMap<String, ObjFLUJOCAJADETALLADO> flujosDetallado = new HashMap<String, ObjFLUJOCAJADETALLADO>();
			HashMap<String, ObjFLUJOCAJA> flujosTotalizado = new HashMap<String, ObjFLUJOCAJA>();
		
			for(RangoFecha saldoFecha : saldosFecha){
				String fechaSaldo = Fecha.toStringDate(saldoFecha.Fecha);
				String keyDetalle = saldoFecha.IdPortafolio.toString() + '_' + saldoFecha.IdMoneda + '_' + fechaSaldo + '_' + saldoFecha.Item + "_" + saldoFecha.ItemCalculo + "_" + saldoFecha.IdUsoFlujoCaja;
				String keyCaja = saldoFecha.IdPortafolio.toString() + '_' + saldoFecha.IdMoneda + '_' + fechaSaldo + "_" + saldoFecha.NombreUso;
				ObjFLUJOCAJADETALLADO flujoDetallado = flujosDetallado.get(keyDetalle);
				ObjFLUJOCAJA flujoCaja = flujosTotalizado.get(keyCaja);
				BigDecimal operador = new BigDecimal(saldoFecha.FuncionOperador);
				if(flujoDetallado == null){
					flujoDetallado = new ObjFLUJOCAJADETALLADO();
					//flujoDetallado = Utils.copyPojo(saldoFecha, flujoDetallado);
					flujoDetallado = getPojoFlujoDetallado(saldoFecha, flujoDetallado);
					flujoDetallado.FuncionOperador = saldoFecha.FuncionOperadorString;
					flujoDetallado.FechaSaldo = Fecha.toStringDate(saldoFecha.Fecha);
				}
				
				if(flujoCaja == null){
					flujoCaja = new ObjFLUJOCAJA();
					//flujoCaja = Utils.copyPojo(saldoFecha, flujoCaja);
					flujoCaja = getPojoFlujoCaja(saldoFecha, flujoCaja);
					flujoCaja.FechaSaldo = Fecha.toStringDate(saldoFecha.Fecha);
				}
				
				//CUENTA INVERSION
				if("SALDO_CUENTA_INVERSION".equals(saldoFecha.ItemCalculo) || "CARGOS_CUENTA_INVERSION".equals(saldoFecha.ItemCalculo)){
					/*for(SaldoPorFechaEfectiva saldo : salidaSaldoCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA){
						if(saldo.IdPortafolio.equals(saldoFecha.IdPortafolio) && saldo.IdMoneda.equals(saldoFecha.IdMoneda) && saldo.FechaSaldo.equals(fechaSaldo)){
							BigDecimal monto = "SALDO_CUENTA_INVERSION".equals(saldoFecha.ItemCalculo) ? operador.multiply(saldo.SaldoDisponible, MathContext.DECIMAL128) : operador.multiply(saldo.SaldoCargosPendientes, MathContext.DECIMAL128); 
							flujoDetallado.Monto = flujoDetallado.Monto.add(monto,MathContext.DECIMAL128);
							flujoCaja.Saldo = flujoCaja.Saldo.add(monto,MathContext.DECIMAL128);
							break;
						}
					}*/
					SaldoPorFechaEfectiva saldo = salidaSaldoCuentaInversionHash.get(saldoFecha.IdPortafolio+"_"+saldoFecha.IdMoneda+"_"+fechaSaldo);
					if (saldo != null && saldo.SaldoDisponible != null && saldo.SaldoCargosPendientes != null) {
						BigDecimal monto = "SALDO_CUENTA_INVERSION".equals(saldoFecha.ItemCalculo) ? operador.multiply(saldo.SaldoDisponible, MathContext.DECIMAL128) : operador.multiply(saldo.SaldoCargosPendientes, MathContext.DECIMAL128); 
						flujoDetallado.Monto = flujoDetallado.Monto.add(monto,MathContext.DECIMAL128);
						flujoCaja.Saldo = flujoCaja.Saldo.add(monto,MathContext.DECIMAL128);
					}
				}
				//TESORERIA
				else if("INGRESOS_TESORERIA".equals(saldoFecha.ItemCalculo) || "EGRESOS_TESORERIA".equals(saldoFecha.ItemCalculo)){
					/*for(SaldoPorFechaLiquidacion saldo : salidaSaldoPendienteDeLiquidar.P_OUT_SALDOPORFECHALIQUIDACION){
						if(saldo.IdPortafolio.equals(saldoFecha.IdPortafolio) && saldo.IdMoneda.equals(saldoFecha.IdMoneda) && saldo.FechaSaldo.equals(fechaSaldo)){
							BigDecimal monto = "INGRESOS_TESORERIA".equals(saldoFecha.ItemCalculo) ? operador.multiply(saldo.SaldoMovIng, MathContext.DECIMAL128) : operador.multiply(saldo.SaldoMovEgr, MathContext.DECIMAL128);
							flujoDetallado.Monto = flujoDetallado.Monto.add(monto,MathContext.DECIMAL128);
							flujoCaja.Saldo = flujoCaja.Saldo.add(monto,MathContext.DECIMAL128);
							break;
						}
					}*/
					SaldoPorFechaLiquidacion saldo = salidaSaldoPendienteDeLiquidarHash.get(saldoFecha.IdPortafolio+"_"+saldoFecha.IdMoneda+"_"+fechaSaldo);
					if (saldo != null && saldo.SaldoMovIng != null && saldo.SaldoMovEgr != null) {
						BigDecimal monto = "INGRESOS_TESORERIA".equals(saldoFecha.ItemCalculo) ? operador.multiply(saldo.SaldoMovIng, MathContext.DECIMAL128) : operador.multiply(saldo.SaldoMovEgr, MathContext.DECIMAL128);
						flujoDetallado.Monto = flujoDetallado.Monto.add(monto,MathContext.DECIMAL128);
						flujoCaja.Saldo = flujoCaja.Saldo.add(monto,MathContext.DECIMAL128);
					}
				}
				else{
					//OPERACIONES TRANSITO Y LINEA SOBRE GIRO
					String tipoMonto = obtenerTipoMonto(saldoFecha.ItemCalculo);
					if(tipoMonto != null){
						for(DatosFlujoCaja dato : datosFlujoCaja){
							if(dato.ItemCalculo != null && dato.ItemCalculo.equals(saldoFecha.ItemCalculo) && dato.Id.equals(saldoFecha.Id) && dato.FechaLiquidacion.compareTo(fDesde) != -1 && (dato.FechaLiquidacion.compareTo(saldoFecha.Fecha) != 1 || "CARGOS_RETENIDOS_POR_LIQUIDAR".equalsIgnoreCase(dato.ItemCalculo))){
								BigDecimal monto = BigDecimal.ZERO;
								if("MontoCompra".equals(tipoMonto)){
									monto = dato.MontoCompra;
								}
								if("MontoVenta".equals(tipoMonto)){
									monto = dato.MontoVenta;
								}
								if("Monto".equals(tipoMonto)){
									monto = dato.Monto;
								}
								if(dato.FechaLiquidacion.compareTo(saldoFecha.Fecha)<=0 && "CARGOS_RETENIDOS_POR_LIQUIDAR".equalsIgnoreCase(dato.ItemCalculo) ) {
									monto = BigDecimal.ZERO;
								}
								monto = operador.multiply(monto, MathContext.DECIMAL128);
								flujoDetallado.Monto = flujoDetallado.Monto.add(monto,MathContext.DECIMAL128);
								flujoCaja.Saldo = flujoCaja.Saldo.add(monto,MathContext.DECIMAL128);
							}
						}
					}
				}
				
				flujosDetallado.put(keyDetalle, flujoDetallado);
				flujosTotalizado.put(keyCaja, flujoCaja);
			}
			if(in.p_BarraProgreso != null) {
				in.p_BarraProgreso.actualizar(100L,"PROCESANDO","Finalizando consulta de saldo");
			}
			for(Map.Entry<String, ObjFLUJOCAJADETALLADO> obj : flujosDetallado.entrySet()){
				out.P_OUT_FLUJOCAJADETALLADO.add(obj.getValue());
			}
			
			for(Map.Entry<String, ObjFLUJOCAJA> obj : flujosTotalizado.entrySet()){
				out.P_OUT_FLUJOCAJA.add(obj.getValue());
			}
			
			out.P_OUT_FLUJOCAJADETALLADO.sort(new Comparator<ObjFLUJOCAJADETALLADO>() {
			    @Override
			    public int compare(ObjFLUJOCAJADETALLADO mov1, ObjFLUJOCAJADETALLADO mov2) {
			        if(mov1.IdUsoFlujoCaja.equals(mov2.IdUsoFlujoCaja)){
			            return 0;
			        }
			        return mov1.IdUsoFlujoCaja.compareTo(mov2.IdUsoFlujoCaja);
			     }
			});

		} catch(Exception e){
		    logger.error("Error",e);
			throw e;
		}
		
		logger.info("Termino del servicio SaldoFlujoCaja, tiempo:" + (System.currentTimeMillis() - init));
		return out;
	}

	
	
	public RangoFecha getPojoSaldoFecha(PortafolioProceso pp, ConfiguracionFlujoCaja conf, RangoFecha rf){
		rf.Id = pp.Id;
		rf.IdPersona = pp.IdPersona; 
		rf.IdPortafolio = pp.IdPortafolio;
		rf.IdMoneda = pp.IdMoneda;
		rf.IdUsoFlujoCaja = conf.IdUsoFlujoCaja;
		rf.NombreUso = conf.NombreUso;
		//rf.Item = conf.Item;
		rf.ItemCalculo = conf.ItemCalculo;
		rf.ItemOrigen = conf.ItemOrigen;
		//rf.FuncionOperador = conf.FuncionOperador;
		rf.FuncionOperadorString = conf.FuncionOperador;
		rf.DesplegarEnUI = conf.DesplegarEnUI;
		rf.UsoSolicitado = conf.NombreUso;
		//rf.Fecha = conf.Fecha;
		return rf;
	}
	
	public ObjFLUJOCAJA getPojoFlujoCaja(RangoFecha rf, ObjFLUJOCAJA fc){
		fc.NombreUso = rf.NombreUso;
		//fc.FechaSaldo = rf.FechaSaldo;
		fc.IdPortafolio = rf.IdPortafolio;
		fc.IdMoneda = rf.IdMoneda;
		//fc.Saldo = rf.Saldo;
		return fc;
	}
	
	public ObjFLUJOCAJADETALLADO getPojoFlujoDetallado(RangoFecha rf, ObjFLUJOCAJADETALLADO fd){
		//fd.FechaSaldo = rf.FechaSaldo;
		fd.IdPortafolio = rf.IdPortafolio;
		fd.IdMoneda = rf.IdMoneda;
		fd.IdUsoFlujoCaja = rf.IdUsoFlujoCaja;
		fd.NombreUso = rf.NombreUso;
		//fd.IdConfiguracionFlujoCaja = rf.IdConfiguracionFlujoCaja;
		fd.ItemCalculo = rf.ItemCalculo;
		//fd.FuncionOperador = rf.FuncionOperador;
		//fd.Monto = rf.Monto;
		return fd;
	}
	
	public String obtenerTipoMonto(String item){
		switch(item){
			case "ORDENES_EN_TRANSITO_COMPRAS":
			case "ASIGNACIONES_COMPRAS":
			case "MOVIMIENTOS_FACTURAR_COMPRAS":
			case "APORTES_EN_TRANSITO":
			case "APORTES_EN_TRANSITO_PAC":
			case "APORTES_EN_TRANSITO_POR_REINVERSION":
				
			case "SIMULTANEA_COMPRAS":
			case "COMPRA_PACTO":
			case "COMPRA_TERMINO":
			case "COMPRA_FORWARD":
			case "VENTA_PACTO":
			case "VENTA_TERMINO":
			case "VENTA_FORWARD":
				return "MontoCompra";
			case "ORDENES_EN_TRANSITO_VENTAS":
			case "ASIGNACIONES_VENTAS":
			case "MOVIMIENTOS_FACTURAR_VENTAS":
			case "RESCATES_EN_TRANSITO":
			case "SIMULTANEA_VENTAS":
				return "MontoVenta";
			case "DIVIDENDOS_EN_DINERO":
			case "CORTE_CUPON":
			case "SORTEO_DE_LETRAS":
			case "EMPRESTITOS":
			case "PRESTAMOS":
			case "LINEA_SOBREGIRO":
			case "LINEA_SOBREGIRO_TESORERIA":
			case "ENTREGAMOS_OPERACION_SPOT":
			case "RECIBIMOS_OPERACION_SPOT_PAC":
			case "RECIBIMOS_OPERACION_SPOT":
			case "SALDO_RETENIDO":
			case "ABONOS_RETENIDOS_POR_LIQUIDAR":
			case "CARGOS_RETENIDOS_POR_LIQUIDAR":
			case "CUSTODIA_FFMM_MONEY_MARKET":
			case "MOVIMIENTOS_FACTURAR_COMISION_SALIDA_FFM":
				return "Monto";
			default: 
				return null;
		}
	}
	
	private String tipoQuery(String item){
		switch (item){
			case "ORDENES_EN_TRANSITO_COMPRAS":
			case "ORDENES_EN_TRANSITO_VENTAS":
				return "ORDENES";
			case "ASIGNACIONES_COMPRAS":
			case "ASIGNACIONES_VENTAS":
				return "ASIGNACIONES";
			case "MOVIMIENTOS_FACTURAR_COMPRAS":
			case "MOVIMIENTOS_FACTURAR_VENTAS":
			case "MOVIMIENTOS_FACTURAR_COMISION_SALIDA_FFM":
				return "FACTURAS";
			case "APORTES_EN_TRANSITO":
				return "APORTES_EN_TRANSITO";
			case "APORTES_EN_TRANSITO_PAC":
				return "APORTES_EN_TRANSITO_PAC";

			case "APORTES_EN_TRANSITO_POR_REINVERSION":
				return "APORTES_EN_TRANSITO_POR_REINVERSION";
			
			case "APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA":
				return "APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA";
			case "RESCATES_EN_TRANSITO":
				return "SELECT_RESCATES_EN_TRANSITO";
			case "INVERSION_EN_LIQUIDEZ":
				return "INVERSION_EN_LIQUIDEZ";
			case "DIVIDENDOS_EN_DINERO":
			case "CORTE_CUPON":
			case "SORTEO_DE_LETRAS":
				return "EVENTO_CAPITAL";
			case "SIMULTANEA_COMPRAS":
			case "EMPRESTITOS":
			case "PRESTAMOS":
				return "SIMULTANEAS_EMP_PRES";
			case "COMPRA_PACTO":
			case "COMPRA_TERMINO":
			case "COMPRA_FORWARD":
			case "VENTA_PACTO":
			case "VENTA_TERMINO":
			case "VENTA_FORWARD":
				return "MESAS";
			case "LINEA_SOBREGIRO":
				return "SOBREGIRO";
			case "LINEA_SOBREGIRO_TESORERIA":
				return "SOBREGIRO_TESORERIA";
			case "ENTREGAMOS_OPERACION_SPOT":
				return "ENTREGAMOS_OPERACION_SPOT";
			case "RECIBIMOS_OPERACION_SPOT_PAC":
				return "RECIBIMOS_OPERACION_SPOT_PAC";
			case "RECIBIMOS_OPERACION_SPOT":
				return "RECIBIMOS_OPERACION_SPOT";
			case "SALDO_RETENIDO":
				return "SALDO_RETENIDO";
			case "ABONOS_RETENIDOS_POR_LIQUIDAR":
				return "ABONOS_RETENIDOS_POR_LIQUIDAR";
			case "CARGOS_RETENIDOS_POR_LIQUIDAR":
				return "CARGOS_RETENIDOS_POR_LIQUIDAR";
			case "CUSTODIA_FFMM_MONEY_MARKET":
				return "CUSTODIA_FFMM_MONEY_MARKET";
			default : 
				return null;
		}
	}
	
	private String obtenerConsulta(String tipo){
		switch (tipo){
			case "ORDENES":
				return SQL_SELECT_ORDENES_EN_TRANSITO_COMPRAS;
			case "ASIGNACIONES":
				return SQL_SELECT_SALDO_ASIGNACIONES_EN_TRANSITO;
			case "FACTURAS":
				return SQL_SELECT_SALDO_MOVIMIENTOS_FACTURACION;
			case "APORTES_EN_TRANSITO":
				return SQL_SELECT_APORTES_EN_TRANSITO;
			case "APORTES_EN_TRANSITO_PAC":
				return SQL_SELECT_APORTES_EN_TRANSITO_PAC;

			case "APORTES_EN_TRANSITO_POR_REINVERSION":
				return SQL_SELECT_APORTES_EN_TRANSITO_POR_REINVERSION;	
			
				
			
			case "APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA":
				return SQL_SELECT_APORTES_EN_TRANSITO_INSTRUCCION_LIQUIDADA;
			case "RESCATES_EN_TRANSITO":
				return SQL_SELECT_RESCATES_EN_TRANSITO;
			case "INVERSION_EN_LIQUIDEZ":
				return SQL_SELECT_INVERSION_EN_LIQUIDEZ;
			case "EVENTO_CAPITAL":
				return SQL_SELECT_EVENTO_CAPITAL;
			case "SIMULTANEAS_EMP_PRES":
				return SQL_SELECT_SALDO_SIMULTANEAS_EMP_PRES;
			case "MESAS":
				return SQL_SELECT_MESAS;
			case "SOBREGIRO":
				return SQL_SELECT_LINEA_SOBREGIRO;
			case "SOBREGIRO_TESORERIA":
				return SQL_SELECT_LINEA_SOBREGIRO_TESORERIA;
			case "ENTREGAMOS_OPERACION_SPOT":
				return SQL_SELECT_ENTREGAMOS_SPOT;
			case "RECIBIMOS_OPERACION_SPOT_PAC":
				return SQL_SELECT_RECIBIMOS_SPOT_PAC;	
			case "RECIBIMOS_OPERACION_SPOT":
				return SQL_SELECT_RECIBIMOS_SPOT;
			case "SALDO_RETENIDO":
				return SQL_SELECT_SALDO_RETENIDO;
			case "ABONOS_RETENIDOS_POR_LIQUIDAR":
				return SQL_SELECT_SALDO_RETENIDO_ABONOS_POR_LIQUIDAR;
			case "CARGOS_RETENIDOS_POR_LIQUIDAR":
				return SQL_SELECT_SALDO_RETENIDO_CARGOS_POR_LIQUIDAR;
			case "CUSTODIA_FFMM_MONEY_MARKET":
				return SQL_SELECT_CUSTODIA_MONEY_MARKET;
			default : 
				return null;
		}
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("QA01", "connections.properties")) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
		String jsonDE = "{\"P_IDEMPRESA\":1,\"P_FECHADESDE\":\"2024-07-25\",\"P_FECHAHASTA\":\"2024-07-25\",\"P_USO\":\"CUENTA_INVERSION\",\"P_DESPLEGARENUI\":\"N\",\"P_AGRUPARPORORIGEN\":\"N\",\"P_PORTAFOLIOACONSULTAR\":[{\"IdPortafolio\":319944,\"IdMoneda\":\"CLP\"},{\"IdPortafolio\":319944,\"IdMoneda\":\"USD\"}]}";
		//String jsonMB = "{\"P_IDEMPRESA\":1,\"P_FECHADESDE\":\"2019-03-27\",\"P_FECHAHASTA\":\"2019-03-27\",\"P_USO\":\"ORDENES_DE_COMPRA\",\"P_DESPLEGARENUI\":null,\"P_AGRUPARPORORIGEN\":null,\"P_PORTAFOLIOACONSULTAR\":[{\"IdPortafolio\":19110,\"IdMoneda\":\"CLP\"},{\"IdPortafolio\":21809,\"IdMoneda\":\"CLP\"},{\"IdPortafolio\":10841,\"IdMoneda\":\"CLP\"}]}";
		ServiceEntrada Objin = gson.fromJson(jsonDE, ServiceEntrada.class);
		SaldoFlujoCaja  demo= new SaldoFlujoCaja();				
		try {
			ServiceSalida ObjOut=demo.logica(con, Objin);
			con.rollback();
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