package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.EntradaConsultarCustodiaPortafolioFondoMutuo;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.SalidaConsultarCustodiaPortafolioFondoMutuo;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia;
import cl.bcs.normativa.cartola.BcsCartola.Dato;
import cl.bcs.normativa.cartola.BcsCartola.EntradaCartola;
import cl.bcs.normativa.cartola.BcsCartola.SalidaCartola;
import cl.bcs.normativa.cartola.CarteraForward.SalidaCarteraForward;
import cl.bcs.normativa.cartola.CarteraRV.SalidaCarteraRV;
import cl.bcs.normativa.cartola.GenerarCartola.NominaPaso;
import cl.bcs.normativa.cartola.GenerarCartola.ServicioEjecutado;
import cl.bcs.normativa.cartola.CarteraIF.SalidaCarteraIF;
import cl.bcs.normativa.cartola.CarteraRF.SalidaCarteraRF;
import cl.bcs.normativa.cartola.ResumenInversionBase.Portafolio;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.EntradaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.SalidaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaEfectiva;

public class ResumenInversionBase implements JavaCall{
	
	private final static Logger logger = LogManager.getLogger(ResumenInversionBase.class);
	
	public final String[] GRUPOS = {"CAJA","RV","RF","IF","DV",/*"FWD",*/"FM","OPL"};
	
	//POR PORTAFOLIO
	private final String SQL_SELECT_MOVIMIENTOS_CUENTA_INVERSION_PORTAFOLIO = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectCuentaInversionPortafolio.sql");
	private final String SQL_SELECT_MOVIMIENTOS_GARANTIA_EN_DINERO_PORTAFOLIO = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectGarantiaEnDineroPortafolio.sql");
	private final String SQL_SELECT_MOVIMIENTOS_GARANTIA_EN_DINERO_HISTORICO_PORTAFOLIO = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectGarantiaEnDineroHistoricoPortafolio.sql");
	private final String SQL_SELECT_MOVIMIENTOS_OPERACIONES_POR_LIQUIDAR_PORTAFOLIO_TESORERIA = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectOperacionesPorLiquidarPortafolioTesoreria.sql");
	private final String SQL_SELECT_MOVIMIENTOS_OPERACIONES_POR_LIQUIDAR_PORTAFOLIO_CUENTA_INVERSION = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectOperacionesPorLiquidarPortafolioCuentaInversion.sql");
	private final String SQL_SELECT_MOVIMIENTOS_CUSTODIA_PORTAFOLIO = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectCustodiaPortafolio.sql");
	private final String SQL_SELECT_MOVIMIENTOS_CUSTODIA_HISTORICO_PORTAFOLIO = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectCustodiaHistoricoPortafolio.sql");
	private final String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectCliente.sql");
	private final String SQL_SELECT_DETALLE_MOVIMIENTOS_CUSTODIA_PORTAFOLIO = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectDetalleCustodiaPortafolio.sql");
	private final String SQL_SELECT_DETALLE_MOVIMIENTOS_CUSTODIA_HISTORICO_PORTAFOLIO = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectDetalleCustodiaHistoricoPortafolio.sql");
	
	//POR CLIENTES
	private final String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectPortafolio.sql");
	
	//FWD
	private final String SQL_SELECT_CARTERA_ACTUAL_FWD = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectCarteraActual.sql");
	private final String SQL_SELECT_CARTERA_HISTORICO_FWD = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectCarteraHistorico.sql");
	
	private final String SQL_SELECT_SORTEO_CONTRATOS = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectSorteoContratos.sql");

	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	private final String SQL_SELECT_CUENTA_INVERSION = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectCuentaInversion.sql");
	
	private final String SQL_SELECT_NOMINA_PASO = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectNominaPaso.sql");
	
	private final String SQL_SELECT_SERVICIOS_EJECUTADOS = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_SelectServicioEjecutado.sql");
	private final String SQL_INSERT_SERVICIOS_EJECUTADOS = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertServicioEjecutado.sql");
	private final String SQL_INSERT_RESUMEN_INVERSION_BASE_OUT = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_InsertOutServicioEjecutado.sql");
	private final String SQL_SELECT_RESUMEN_INVERSION_BASE_OUT = loadResourceToString("cl/bcs/normativa/cartola/sql/ResumenInversionBase_SelectOUTServicioEjecutado.sql");
	
	private final String SQL_DELETE_PRECIO_TASA_PASO = loadResourceToString("cl/bcs/normativa/cartola/sql/CarteraRV_DeletePrecioTasaTemporal.sql");
	
	private final String[] MONEDAS_CUENTA_INVERSION = {"USD", "EUR"};
	
	public static class EntradaResumenInversionBase{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
	    public Date p_FechaDesde;
	    public Date	p_FechaHasta;
	    public String p_IdTipoCanal ;
	    public List<Dato> p_Datos;
	    public String p_IdProductoAgrupador;
	    
	    public EntradaResumenInversionBase() {
	    	this.p_Datos = new ArrayList<Dato>();
	    	this.p_IdModulo = "NOR";
	    }
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Portafolio {
		public Long IdPortafolio;
		public Long IdTrabajo;
	}
	
	public static class ClientePaso {
		public Long IdCliente;
		public Long IdTrabajo;
	}
	
	public static class SalidaResumenInversionBase{
		public Long P_OUT_ERRORESCONTADOR;
		public List<OutResumenInversion> p_out_ResumenInversion;
		
		public SalidaResumenInversionBase() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.p_out_ResumenInversion = new ArrayList<OutResumenInversion>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClientePortafolio{
		public Long IdCliente;
		public Long IdPortafolio;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class OutResumenInversion{
		public Long IdCliente;
        public Long IdPortafolio;
        public String Grupo;
		public String Item;
		public String IdMonedaOperacion;
		public BigDecimal MontoMonedaOperacion;
		public String IdMonedaSistema;
		public BigDecimal MontoMonedaSistema;
		public BigDecimal TotalMonedaSistema;
		public String IdMonedaAdicional;
	    public BigDecimal MontoMonedaAdicional;
		public BigDecimal TotalMonedaAdicional;
		public String MonedaResumenInversion;
		public String IdTipoInstrumento;

		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	public static class OutResumenInversionPaso extends OutResumenInversion{
		public Long IdNomina;
		public Long IdTrabajo;
        
		public String toString() {
			return Utils.printJson(this, false);
		}
		
	}
	
	public static class CarteraResumenInversion{
		public Long IdInterno;
		public Long IdCliente;
		public Long IdPortafolio;
		public String Grupo;
		public String Item;
		public String IdMonedaOperacion;
		public String IdTipoInstrumento;
		public BigDecimal MontoMonedaOperacion;
		public String IdMonedaSistema;
		public BigDecimal MontoMonedaSistema;
		public String IdMonedaAdicional;
		public BigDecimal MontoMonedaAdicional;
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Totales{
		public BigDecimal TotalMonedaSistema;
		public BigDecimal TotalMonedaAdicional;
		
		public Totales() {
			this.TotalMonedaSistema = BigDecimal.ZERO;
			this.TotalMonedaAdicional = BigDecimal.ZERO;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CarteraDetalleInversion extends BcsPojo{
		public Long IdInterno;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public Long IdInstrumento;
		public BigDecimal TotalCantidad;
		public String IdMonedaOperacion;
		public String Nemotecnico;
		public BigDecimal MontoMonedaOperacion;
		public String Item;
		
		public CarteraDetalleInversion() {
			this.MontoMonedaOperacion = BigDecimal.ZERO;
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaResumenInversionBase jin = gson.fromJson(in.toString(), EntradaResumenInversionBase.class);
		SalidaResumenInversionBase out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaResumenInversionBase logica(Connection con, EntradaResumenInversionBase entrada) throws Exception{
		logger.info("INICIO::ResumenInversionBase");
		long inicioServicio = System.currentTimeMillis();
		SQL sql = new SQL();
		
		
		///////////////////////////////////////////////////////////////////////////////////////////
		//			VALIDA QUE EL PROCESO NO SE EJECUTE VARIAS VECES PARA LA MISMA CARTOLA		 //
		///////////////////////////////////////////////////////////////////////////////////////////
		Gson gson = new Gson();	
		NominaPaso nomina = new NominaPaso();
		Long idTrabajo = (long) (Math.random()*10000000000L);
		List<NominaPaso> nominas = new ArrayList();
		Boolean primeraEjecucion = false;
		Long idTrabajoEjecutado = 0L;
		try {
			nominas = sql.query(con, SQL_SELECT_NOMINA_PASO, new HashMap<>(), NominaPaso.class);
		} catch (Exception e) {
			logger.info("INICIO::ResumenInversionBase -ERROR SQL_SELECT_NOMINA_PASO "+e);
		}
		if(!nominas.isEmpty()) {
			nomina = nominas.get(0);
			if(nomina.IdNomina.compareTo(0L) != 0L) {
				JsonParser parser = new JsonParser();
				JsonObject jaison = parser.parse(gson.toJson(entrada)).getAsJsonObject();
				String jeisonString = jaison.toString();
				String clase = this.getClass().getSimpleName();
				HashMap<String,Object> paramsJson = new HashMap<String,Object>();
				List<ServicioEjecutado> serviciosEjecutados = new ArrayList();
				List<OutResumenInversionPaso> outGuardado = new ArrayList();
				List<OutResumenInversion> listOutExistente = new ArrayList();
				paramsJson.put("IdNomina", nomina.IdNomina);
				paramsJson.put("Recurso", clase);
				try {
					serviciosEjecutados = sql.query(con, SQL_SELECT_SERVICIOS_EJECUTADOS, paramsJson, ServicioEjecutado.class);
				} catch (Exception e) {
					logger.info("INICIO::ResumenInversionBase -ERROR SQL_SELECT_SERVICIO_EJECUTADO "+e);
				}
				
				if(serviciosEjecutados.isEmpty()) {
					List<ServicioEjecutado> portObj = new ArrayList<>();
					ServicioEjecutado se = new ServicioEjecutado();
					se.IdNomina = nomina.IdNomina;
					se.IdTrabajo = idTrabajo;
					se.Entrada = jeisonString;
					se.Recurso = clase;
					portObj.add(se);
					try {
						sql.batch(con, SQL_INSERT_SERVICIOS_EJECUTADOS, portObj, ServicioEjecutado.class);
					} catch (Exception e) {
						logger.info("INICIO::ResumenInversionBase -ERROR SQL_SELECT_SERVICIO_EJECUTADO "+e);
					}
					primeraEjecucion = true;
				}
				else
				{
					HashMap<String,Object> paramsOut = new HashMap<String,Object>();
					paramsOut.put("IdNomina", nomina.IdNomina);
					outGuardado = sql.query(con, SQL_SELECT_RESUMEN_INVERSION_BASE_OUT,paramsOut, OutResumenInversionPaso.class);
					
					for(ServicioEjecutado se : serviciosEjecutados) {
						if(se.Entrada.equals(jeisonString) && se.IdNomina.compareTo(nomina.IdNomina) == 0L && se.Recurso.equals(clase)) {
							idTrabajoEjecutado = se.IdTrabajo;
							primeraEjecucion = false;
							break;
						}
						else {
							primeraEjecucion = true;
						}
					}
					if(primeraEjecucion == false) {
						SalidaResumenInversionBase out = new SalidaResumenInversionBase();
						Long id = idTrabajoEjecutado;
						for(OutResumenInversionPaso p : outGuardado) {
							if(p.IdTrabajo.compareTo(id) == 0L) {
								OutResumenInversion outExistente = new OutResumenInversion();
								outExistente.IdCliente =  p.IdCliente;
								outExistente.IdPortafolio = p.IdPortafolio;
								outExistente.Grupo = p.Grupo;
								outExistente.Item = p.Item;
								outExistente.IdMonedaOperacion = p.IdMonedaOperacion;
								outExistente.MontoMonedaOperacion = p.MontoMonedaOperacion;
								outExistente.IdMonedaSistema = p.IdMonedaSistema;
								outExistente.MontoMonedaSistema = p.MontoMonedaSistema;
								outExistente.TotalMonedaSistema = p.TotalMonedaSistema;
								outExistente.IdMonedaAdicional = p.IdMonedaAdicional;
								outExistente.MontoMonedaAdicional = p.MontoMonedaAdicional;
								outExistente.TotalMonedaAdicional = p.TotalMonedaAdicional;
								outExistente.MonedaResumenInversion = p.MonedaResumenInversion;
								listOutExistente.add(outExistente);
							}
						}
						if(listOutExistente.size() > 0) {
							out.p_out_ResumenInversion = listOutExistente;
							return out;
						}
					}
				}
			}
			else {
				primeraEjecucion = false;
			}
		}
		
		SalidaResumenInversionBase out = new SalidaResumenInversionBase();
		String monedaAdicional = null;
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String localizacionSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.LocalizacionSistema");
		
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);
	    Long idPaisSistema = empresa.IdPais;
		
		//El Metodo arroja error cuando no encuentra parametro
		try{
			monedaAdicional = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaAdicional");
		}catch(Exception e){
			logger.error(e);
		}
		
		if(monedaAdicional == null || monedaAdicional.isEmpty()){
			monedaAdicional = "USD"; //Cuando no existe moneda es USD
		}
		
		List<Long> portafolios = new ArrayList<Long>();
		List<Long> clientes = new ArrayList<Long>();
		
		//Primero buscamos los Consolidados
		for(Dato dato : entrada.p_Datos){
			if(!clientes.contains(dato.IdCliente) && dato.IdPortafolio == null) clientes.add(dato.IdCliente);
		}
		
		//Segundo Los no consolidados y que no esten en los consolidados
		for(Dato dato : entrada.p_Datos){
			if(!clientes.contains(dato.IdCliente) && dato.IdPortafolio != null) portafolios.add(dato.IdPortafolio);
		}
		
		List<String> idMonedasCuentaInversion = new ArrayList<String>();
		
		idMonedasCuentaInversion.add(monedaSistema);
		
		for(String moneda : MONEDAS_CUENTA_INVERSION) {
			idMonedasCuentaInversion.add(moneda);
		}
		
		
		String fechaHastaFix = verificacionFecha(con, entrada.p_FechaHasta, entrada.p_IdEmpresa, idPaisSistema);
	
		java.sql.Date fechaHastaFixDateType = Fecha.toDate(fechaHastaFix);
		
		System.out.println(fechaHastaFixDateType);
		
		
		List<CarteraResumenInversion> carterasResumenInversion = new ArrayList<CarteraResumenInversion>();
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		
		List<Portafolio> portObj = new ArrayList<>();
		for (Long p : portafolios) {
			Portafolio por = new Portafolio();
			por.IdTrabajo = idTrabajo;
			por.IdPortafolio = p;
			portObj.add(por);
		}
		sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portObj, Portafolio.class);
		
		
		if(clientes.size() > 0) {
			params.put("IdCliente", clientes);
			List<ClientePortafolio> clientesPortafolio = sql.query(con, SQL_SELECT_PORTAFOLIO, params, ClientePortafolio.class);
				
			for(ClientePortafolio cliente : clientesPortafolio) {
				if(!portafolios.contains(cliente.IdPortafolio)) portafolios.add(cliente.IdPortafolio);
			}
		}
		
		//PARA SACAR EL ID CLIENTE PARA LOS QUE NO TIENEN DATOS EN LAS QUERYS DE MOV
		params.clear();

		
		List<ClientePortafolio> clientesPortafolio = sql.query(con, SQL_SELECT_CLIENTE, params, ClientePortafolio.class);
		Map<Long,Object> hClientesPortafolio =  clientesPortafolio.stream()
				.collect(Collectors.toMap(clientePortafolio->clientePortafolio.IdPortafolio, 
						clientePortafolio->clientePortafolio, 
						(primerPortafolio, segundoPortafolio)->primerPortafolio)
						);
		
		
		
		
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = entrada.p_IdEmpresa;
		Objin.p_IdModulo = entrada.p_IdModulo;
		Objin.p_IdUsuario = entrada.p_IdUsuario;
		Objin.p_FechaDesde = entrada.p_FechaDesde;
		Objin.p_FechaHasta = entrada.p_FechaHasta;
		Objin.p_IdTrabajo = idTrabajo;
		
		for(Dato datos : entrada.p_Datos){
			Dato dato = new Dato();
			dato.IdCliente 		= datos.IdCliente;		
			dato.IdPortafolio 	= datos.IdPortafolio;	
			Objin.p_Datos.add(dato);
		}
		
		CarteraRV  procesarCarteraRV = new CarteraRV();
		SalidaCartola ObjOut = procesarCarteraRV.procesar(con, Objin);
		
		
		if(portafolios.size() > 0) {
			
			params.clear();
			params.put("IdMoneda", idMonedasCuentaInversion);
			params.put("FechaHasta", fechaHastaFixDateType);
			params.put("IdProductoAgrupador", entrada.p_IdProductoAgrupador);
			params.put("IdTrabajo", idTrabajo);
			carterasResumenInversion.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_CUENTA_INVERSION_PORTAFOLIO, params, CarteraResumenInversion.class));
			List<SaldoPorFechaEfectiva> salidaPreliminar = new ArrayList<SaldoPorFechaEfectiva>();
			
			if (carterasResumenInversion.size() > 0) {
				String fechaHastaStr = entrada.p_FechaHasta.toString();
				
				List<SaldoPorFechaEfectiva>	saldos = obtenerSaldoMasivo(con, entrada,portafolios);
				salidaPreliminar.addAll(saldos);
				
				HashMap<String,SaldoPorFechaEfectiva> hashSaldos = new HashMap<>();
				for (SaldoPorFechaEfectiva saldo : salidaPreliminar) {
					hashSaldos.put(saldo.IdPortafolio + saldo.IdMoneda + saldo.FechaSaldo, saldo);
				}
				
				for (CarteraResumenInversion carteras : carterasResumenInversion) {
					SaldoPorFechaEfectiva saldo = hashSaldos.get(carteras.IdPortafolio + carteras.IdMonedaOperacion + fechaHastaStr);
					if (saldo != null) {
						carteras.MontoMonedaOperacion = saldo.SaldoDisponible;
					}
					/*for (SaldoPorFechaEfectiva salidaCta: salidaPreliminar ) {
						if (salidaCta.IdPortafolio.equals(carteras.IdPortafolio) && salidaCta.IdMoneda.equalsIgnoreCase(carteras.IdMonedaOperacion) && salidaCta.FechaSaldo.equals(entrada.p_FechaHasta.toString())) {
							carteras.MontoMonedaOperacion = salidaCta.SaldoDisponible;
						}
					}*/
				}
			}		
			
			params.clear();
			params.put("FechaHasta", fechaHastaFixDateType);
			params.put("IdProductoAgrupador", entrada.p_IdProductoAgrupador);
			params.put("IdTrabajo", idTrabajo);
			if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
				carterasResumenInversion.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_GARANTIA_EN_DINERO_PORTAFOLIO, params, CarteraResumenInversion.class));
			}else {
				carterasResumenInversion.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_GARANTIA_EN_DINERO_HISTORICO_PORTAFOLIO, params, CarteraResumenInversion.class));
			}
			
			params.clear();
			
			params.put("FechaHasta", fechaHastaFixDateType);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("IdProductoAgrupador", entrada.p_IdProductoAgrupador);
			params.put("IdTrabajo", idTrabajo);
			carterasResumenInversion.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_OPERACIONES_POR_LIQUIDAR_PORTAFOLIO_CUENTA_INVERSION, params, CarteraResumenInversion.class));
			///OPL
			if ( (entrada.p_IdModulo !=null) &&(entrada.p_IdTipoCanal !=null) && ("SOC".equalsIgnoreCase(entrada.p_IdModulo))&&("OPT".equalsIgnoreCase(entrada.p_IdTipoCanal))) {
				   carterasResumenInversion.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_OPERACIONES_POR_LIQUIDAR_PORTAFOLIO_TESORERIA, params, CarteraResumenInversion.class));
			   }		
			params.clear();
			params.put("FechaHasta", fechaHastaFixDateType);
			params.put("IdEmpresa", entrada.p_IdEmpresa);
			params.put("IdProductoAgrupador", entrada.p_IdProductoAgrupador);
			params.put("LocalizacionSistema", localizacionSistema);
			params.put("IdTrabajo", idTrabajo);			
			
			if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
				carterasResumenInversion.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_CUSTODIA_PORTAFOLIO, params, CarteraResumenInversion.class));
			}else {
				carterasResumenInversion.addAll(sql.query(con, SQL_SELECT_MOVIMIENTOS_CUSTODIA_HISTORICO_PORTAFOLIO, params, CarteraResumenInversion.class));
			}
			
			List<CarteraDetalleInversion> listaDetalleCarteraRV = this.obtenerDetalleCarteraRV(con, entrada, idTrabajo, fechaHastaFixDateType, fechaSistema, sql);
			
			if(!listaDetalleCarteraRV.isEmpty()) {
				this.actualizarResumenCarteraRV(con, entrada, idPaisSistema, listaDetalleCarteraRV, carterasResumenInversion);
			}
			
			SQL.exec(con, SQL_DELETE_PRECIO_TASA_PASO.replace(":IdTrabajo", idTrabajo+""));

			HashMap<String,Object> paramsFWD = new HashMap<String,Object>();
			paramsFWD.put("IdTrabajo", idTrabajo);
			List<SalidaCarteraForward> salidas = new ArrayList<SalidaCarteraForward>();
			
			if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
				salidas.addAll(sql.query(con, SQL_SELECT_CARTERA_ACTUAL_FWD, paramsFWD, SalidaCarteraForward.class));
			}else {
				paramsFWD.put("FechaHasta", fechaHastaFixDateType);
				salidas.addAll(sql.query(con, SQL_SELECT_CARTERA_HISTORICO_FWD, paramsFWD, SalidaCarteraForward.class));
			}
			
			SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));
			

			List<Long> idInternos = new ArrayList<Long>();
			CalcularParidad calcularParidad = new CalcularParidad();
			CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
			calcularParidadEntrada.p_IdTrabajo = 1L;
			calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
			for(SalidaCarteraForward salida : salidas) {
				if(!idInternos.contains(salida.folioOper.longValue())) {
					CalculoEntrada calculoEntradaResultado = new CalculoEntrada();
					calculoEntradaResultado.IdTrabajo = 0L;
					calculoEntradaResultado.IdInterno = salida.folioOper.longValue();
					calculoEntradaResultado.Fecha = entrada.p_FechaHasta.toString();
					if("ARBITR".equals(salida.Nemotecnico) && "DEB".equals(salida.IdTipoMoneda)) {
						calculoEntradaResultado.IdMonedaOrigen = salida.monedaEntrega;
						calculoEntradaResultado.IdMonedaDestino = salida.monedaRecibe;
					}else {
						calculoEntradaResultado.IdMonedaOrigen = salida.monedaRecibe;
						calculoEntradaResultado.IdMonedaDestino = salida.monedaEntrega;
					}
					calculoEntradaResultado.ValorCambioLocalModificado = BigDecimal.ZERO;
					calculoEntradaResultado.MontoOrigen = BigDecimal.ONE;
					calculoEntradaResultado.IdUso = "";
					calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaResultado);
					idInternos.add(salida.folioOper.longValue());
				}
				salida.valorRazonable = salida.valorRazonableActivo.subtract(salida.valorRazonablePasivo, MathContext.DECIMAL128).negate();
			}
			
			if(calcularParidadEntrada.p_CalculosEntrada.size() > 0) {
				CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
				HashMap<Long,Object> hCalcularParidadSalida = Utils.List2Hash(calcularParidadSalida.p_out_CalculosSalida, "IdInterno");
				calcularParidadEntrada.p_CalculosEntrada.clear();
				idInternos.clear();
				for(SalidaCarteraForward salida : salidas) {
					CalculoSalida calculoSalidaResultado = (CalculoSalida) hCalcularParidadSalida.get(salida.folioOper.longValue());
					if(calculoSalidaResultado != null && "OK".equals(calculoSalidaResultado.Resultado)) {
						salida.TipoCambioMercado = calculoSalidaResultado.MontoDestino;
						if("ARBITR".equals(salida.Nemotecnico) && "DEB".equals(salida.IdTipoMoneda)) {
							salida.montoPeso = "C".equals(salida.tipo) ? salida.monto.divide(salida.TipoCambioMercado.subtract(salida.precioCierre, MathContext.DECIMAL128), MathContext.DECIMAL128) : salida.monto.divide(salida.TipoCambioMercado.subtract(salida.precioCierre, MathContext.DECIMAL128), MathContext.DECIMAL128).negate();
						}else {
							salida.montoPeso = "C".equals(salida.tipo) ? salida.TipoCambioMercado.subtract(salida.precioCierre, MathContext.DECIMAL128).multiply(salida.monto, MathContext.DECIMAL128) : salida.TipoCambioMercado.subtract(salida.precioCierre, MathContext.DECIMAL128).multiply(salida.monto, MathContext.DECIMAL128).negate();
						}
						if(!idInternos.contains(salida.folioOper.longValue())) {
							CalculoEntrada calculoEntradaResultado = new CalculoEntrada();
							calculoEntradaResultado.IdTrabajo = 0L;
							calculoEntradaResultado.IdInterno = salida.folioOper.longValue();
							calculoEntradaResultado.Fecha = entrada.p_FechaHasta.toString();
							calculoEntradaResultado.IdMonedaOrigen = salida.monedaEntrega;
							calculoEntradaResultado.IdMonedaDestino = monedaSistema;
							calculoEntradaResultado.ValorCambioLocalModificado = BigDecimal.ZERO;
							calculoEntradaResultado.MontoOrigen = salida.montoPeso;
							calculoEntradaResultado.IdUso = "";
							calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaResultado);
							idInternos.add(salida.folioOper.longValue());
						}
						
					}
					
				}
			}			
			
				//PASAMOS A CLP EL RESULTADO NETO
				BigDecimal totalFWD = BigDecimal.ZERO;	
				HashMap<Long,BigDecimal> HashTotalFWD = new HashMap<Long,BigDecimal>();
				
				
				if(calcularParidadEntrada.p_CalculosEntrada.size() > 0) {
					CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
					HashMap<Long,Object> hCalcularParidadSalida = Utils.List2Hash(calcularParidadSalida.p_out_CalculosSalida, "IdInterno");
					for(SalidaCarteraForward salida : salidas) {
						CalculoSalida calculoSalidaResultado = (CalculoSalida) hCalcularParidadSalida.get(salida.folioOper.longValue());
						if(calculoSalidaResultado != null && "OK".equals(calculoSalidaResultado.Resultado)) {
							salida.montoPeso = calculoSalidaResultado.MontoDestino;
						}
						//		
						totalFWD = HashTotalFWD.get(salida.IdPortafolio);
						if(totalFWD == null) {
							totalFWD = salida.montoPeso;
						}else {
							totalFWD = totalFWD.add(salida.montoPeso);
						}
						HashTotalFWD.put(salida.IdPortafolio, totalFWD);
						
						
						
					}
				} 
				
				for(Dato dato : entrada.p_Datos){
					CarteraResumenInversion cartFWD = new CarteraResumenInversion();			
					cartFWD.IdCliente 		= dato.IdCliente;		
					cartFWD.IdPortafolio 	= dato.IdPortafolio;
					cartFWD.Grupo = "DV";
					cartFWD.Item = "FORWARDS";
					cartFWD.IdMonedaOperacion = "CLP";		
					
					cartFWD.MontoMonedaOperacion = HashTotalFWD.get(dato.IdPortafolio) == null? BigDecimal.ZERO : HashTotalFWD.get(dato.IdPortafolio);
					carterasResumenInversion.add(cartFWD);					
				}				
				
				
				
				
		}
		
		
		Long i = 1L;
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 0L;
		for(CarteraResumenInversion cartera : carterasResumenInversion) {
			cartera.IdInterno = i;
			cartera.IdMonedaSistema = monedaSistema;
			cartera.IdMonedaAdicional = monedaAdicional;
			CalculoEntrada calculoEntradaMonedaSistema = new CalculoEntrada();
			calculoEntradaMonedaSistema.IdInterno = cartera.IdInterno;
			calculoEntradaMonedaSistema.IdTrabajo = 0L;
			calculoEntradaMonedaSistema.Fecha = Fecha.stringToSqlDate(entrada.p_FechaHasta);
			calculoEntradaMonedaSistema.IdMonedaOrigen = cartera.IdMonedaOperacion;
			calculoEntradaMonedaSistema.IdMonedaDestino = monedaSistema;
			calculoEntradaMonedaSistema.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntradaMonedaSistema.MontoOrigen = cartera.MontoMonedaOperacion;
			calculoEntradaMonedaSistema.IdUso = "CARTOLA";
			CalculoEntrada calculoEntradaMonedaAdicional = new CalculoEntrada();
			calculoEntradaMonedaAdicional.IdInterno = cartera.IdInterno * -1;
			calculoEntradaMonedaAdicional.IdTrabajo = 0L;
			calculoEntradaMonedaAdicional.Fecha = Fecha.stringToSqlDate(entrada.p_FechaHasta);
			calculoEntradaMonedaAdicional.IdMonedaOrigen = cartera.IdMonedaOperacion;
			calculoEntradaMonedaAdicional.IdMonedaDestino = monedaAdicional;
			calculoEntradaMonedaAdicional.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntradaMonedaAdicional.MontoOrigen = cartera.MontoMonedaOperacion;
			calculoEntradaMonedaAdicional.IdUso = "CARTOLA";
			calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaMonedaSistema);
			calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaMonedaAdicional);
			i++;
		}
		
		CalcularParidadSalida calcularParidadSalida = calcularParidad.logica(con, calcularParidadEntrada);
		HashMap<Long,Object> hCalcularParidadSalida = Utils.List2Hash(calcularParidadSalida.p_out_CalculosSalida, "IdInterno");
		
		HashMap<String,Totales> totales = new HashMap<String,Totales>();
		for(CarteraResumenInversion cartera : carterasResumenInversion) {
			Long id = cartera.IdInterno * -1;
			CalculoSalida calculoSalidaMonedaSistema = (CalculoSalida) hCalcularParidadSalida.get(cartera.IdInterno);
			CalculoSalida calculoSalidaMonedaAdicional = (CalculoSalida) hCalcularParidadSalida.get(id);
			if("OK".equals(calculoSalidaMonedaSistema.Resultado)) cartera.MontoMonedaSistema = calculoSalidaMonedaSistema.MontoDestino;
			if("OK".equals(calculoSalidaMonedaAdicional.Resultado)) cartera.MontoMonedaAdicional = calculoSalidaMonedaAdicional.MontoDestino;
			
			Totales total = totales.get(cartera.Grupo + "_" + cartera.IdPortafolio);
			if(total == null) total = new Totales();
			if(cartera.MontoMonedaSistema != null) total.TotalMonedaSistema = total.TotalMonedaSistema.add(cartera.MontoMonedaSistema, MathContext.DECIMAL128);
			if(cartera.MontoMonedaAdicional != null) total.TotalMonedaAdicional = total.TotalMonedaAdicional.add(cartera.MontoMonedaAdicional, MathContext.DECIMAL128);
			totales.put(cartera.Grupo + "_" + cartera.IdPortafolio, total);
			
		}
		
		//CarteraRV
		
		if(!ObjOut.P_OUT_SALIDA.isEmpty() && ObjOut.P_OUT_SALIDA.containsKey("carteraRV")) {
			final ObjectMapper objMapper = new ObjectMapper();
			List<SalidaCarteraRV> pojo = objMapper.convertValue(ObjOut.P_OUT_SALIDA.get("carteraRV"), List.class);
			
			HashMap<String,HashMap> resultMontoMoneda = new HashMap<>();
			for(int k=0; k<pojo.size();k++) {
				SalidaCarteraRV cartera = objMapper.convertValue(pojo.get(k), SalidaCarteraRV.class);
				
				HashMap<String,Object> tempMontoMercadoAct = new HashMap<>();
				HashMap<String,Object> tempMontoMercadoPas = new HashMap<>();
				
				BigDecimal MontoMonedaSistemaAct = cartera.MontoMercadoMonedaSistemaActivo;
				BigDecimal MontoMonedaAdicionalAct = cartera.MontoMercadoMonedaAdicionalActivo;
				BigDecimal MontoMonedaSistemaPas = cartera.MontoMercadoMonedaSistemaPasivo;
				BigDecimal MontoMonedaAdicionalPas = cartera.MontoMercadoMonedaAdicionalPasivo;
				
				if(resultMontoMoneda.get(cartera.IdPortafolio.toString()+cartera.IdTipoInstrumento+cartera.IdMonedaOperacion+"ACT")!=null) {
					MontoMonedaSistemaAct = MontoMonedaSistemaAct.add(new BigDecimal(resultMontoMoneda.get(cartera.IdPortafolio.toString()+cartera.IdTipoInstrumento+cartera.IdMonedaOperacion+"ACT").get("MontoMercadoMonedaSistemaActivo").toString()));
					MontoMonedaAdicionalAct = MontoMonedaAdicionalAct.add(new BigDecimal(resultMontoMoneda.get(cartera.IdPortafolio.toString()+cartera.IdTipoInstrumento+cartera.IdMonedaOperacion+"ACT").get("MontoMercadoMonedaAdicionalActivo").toString()));
				}
				
				tempMontoMercadoAct.put("MontoMercadoMonedaSistemaActivo", MontoMonedaSistemaAct);
				tempMontoMercadoAct.put("MontoMercadoMonedaAdicionalActivo", MontoMonedaAdicionalAct);	
				
				if(resultMontoMoneda.get(cartera.IdPortafolio.toString()+cartera.IdTipoInstrumento+cartera.IdMonedaOperacion+"PAS")!=null) {
					MontoMonedaSistemaPas = MontoMonedaSistemaPas.add(new BigDecimal(resultMontoMoneda.get(cartera.IdPortafolio.toString()+cartera.IdTipoInstrumento+cartera.IdMonedaOperacion+"PAS").get("MontoMercadoMonedaSistemaPasivo").toString()));
					MontoMonedaAdicionalPas = MontoMonedaAdicionalPas.add(new BigDecimal(resultMontoMoneda.get(cartera.IdPortafolio.toString()+cartera.IdTipoInstrumento+cartera.IdMonedaOperacion+"PAS").get("MontoMercadoMonedaAdicionalPasivo").toString()));
				}
				
				tempMontoMercadoPas.put("MontoMercadoMonedaSistemaPasivo", MontoMonedaSistemaPas);
				tempMontoMercadoPas.put("MontoMercadoMonedaAdicionalPasivo", MontoMonedaAdicionalPas);
		
				//Llave Hash IdPortafolio-IdTipoInstrumneto-IdMonedaOperacion
				resultMontoMoneda.put(cartera.IdPortafolio.toString()+cartera.IdTipoInstrumento+cartera.IdMonedaOperacion+"ACT", tempMontoMercadoAct);
				resultMontoMoneda.put(cartera.IdPortafolio.toString()+cartera.IdTipoInstrumento+cartera.IdMonedaOperacion+"PAS", tempMontoMercadoPas);
			}
	
			for(CarteraResumenInversion cartera : carterasResumenInversion) {
				if(cartera.Grupo.equals("RV")) {
					totales.remove(cartera.Grupo + "_" + cartera.IdPortafolio);
				}
			}
			
			for(CarteraResumenInversion cartera : carterasResumenInversion) {
				if(cartera.Grupo.equals("RV")) {
					String clave = "" + cartera.IdPortafolio.toString() + cartera.IdTipoInstrumento + cartera.IdMonedaOperacion;
					if(cartera.IdMonedaOperacion.equals(cartera.IdMonedaSistema)) {
						if(resultMontoMoneda.get(clave+"ACT")!=null && resultMontoMoneda.get(clave+"PAS")!=null) {
							if(("ACCIONES NACIONALES").equals(cartera.Item)) {
								cartera.MontoMonedaSistema = new BigDecimal(resultMontoMoneda.get(clave+"ACT").get("MontoMercadoMonedaSistemaActivo").toString());
								cartera.MontoMonedaAdicional = new BigDecimal(resultMontoMoneda.get(clave+"ACT").get("MontoMercadoMonedaAdicionalActivo").toString());
								cartera.MontoMonedaOperacion = new BigDecimal(resultMontoMoneda.get(clave+"ACT").get("MontoMercadoMonedaSistemaActivo").toString());
							}else {
							cartera.MontoMonedaSistema = new BigDecimal(resultMontoMoneda.get(clave+"ACT").get("MontoMercadoMonedaSistemaActivo").toString()).subtract(new BigDecimal(resultMontoMoneda.get(clave+"PAS").get("MontoMercadoMonedaSistemaPasivo").toString()));
							cartera.MontoMonedaAdicional = new BigDecimal(resultMontoMoneda.get(clave+"ACT").get("MontoMercadoMonedaAdicionalActivo").toString()).subtract(new BigDecimal(resultMontoMoneda.get(clave+"PAS").get("MontoMercadoMonedaAdicionalPasivo").toString()));
							cartera.MontoMonedaOperacion = new BigDecimal(resultMontoMoneda.get(clave+"ACT").get("MontoMercadoMonedaSistemaActivo").toString()).subtract(new BigDecimal(resultMontoMoneda.get(clave+"PAS").get("MontoMercadoMonedaSistemaPasivo").toString()));
							}
						}
					}
					else if(cartera.IdMonedaOperacion.equals(cartera.IdMonedaAdicional)) {
						if(resultMontoMoneda.get(clave+"ACT")!=null && resultMontoMoneda.get(clave+"PAS")!=null) {
							cartera.MontoMonedaSistema = new BigDecimal(resultMontoMoneda.get(clave+"ACT").get("MontoMercadoMonedaSistemaActivo").toString()).subtract(new BigDecimal(resultMontoMoneda.get(clave+"PAS").get("MontoMercadoMonedaSistemaPasivo").toString()));
							cartera.MontoMonedaAdicional = new BigDecimal(resultMontoMoneda.get(clave+"ACT").get("MontoMercadoMonedaAdicionalActivo").toString()).subtract(new BigDecimal(resultMontoMoneda.get(clave+"PAS").get("MontoMercadoMonedaAdicionalPasivo").toString()));
							cartera.MontoMonedaOperacion = new BigDecimal(resultMontoMoneda.get(clave+"ACT").get("MontoMercadoMonedaAdicionalActivo").toString()).subtract(new BigDecimal(resultMontoMoneda.get(clave+"PAS").get("MontoMercadoMonedaAdicionalPasivo").toString()));
						}
					}				
					Totales total = totales.get(cartera.Grupo + "_" + cartera.IdPortafolio);
					if(total == null) total = new Totales();
					if(cartera.MontoMonedaSistema != null) total.TotalMonedaSistema = total.TotalMonedaSistema.add(cartera.MontoMonedaSistema, MathContext.DECIMAL128);
					if(cartera.MontoMonedaAdicional != null) total.TotalMonedaAdicional = total.TotalMonedaAdicional.add(cartera.MontoMonedaAdicional, MathContext.DECIMAL128);
					totales.put(cartera.Grupo + "_" + cartera.IdPortafolio, total);
				}
			}
		}
		
		//CarteraRV
		
		//Cartera FFMM
		long inicioServicioFFMM = System.currentTimeMillis();
		logger.info("INICIO::Cartera FFMM");
		List<SalidaCustodia> salidaPreliminar = new ArrayList<SalidaCustodia>();
		HashMap<String,HashMap> resultMontoMonedaFM = new HashMap<>();
		
		if (entrada.p_Datos.size() > 100) {
			List<ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia> custodias = obtenerCustodias(con, entrada,null);
			salidaPreliminar.addAll(custodias);
		}
		else {
			for(Dato dato : entrada.p_Datos){
				List<ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia> custodias = obtenerCustodias(con, entrada,dato);
				salidaPreliminar.addAll(custodias);
			}
		}
		
		for(int k=0; k<salidaPreliminar.size();k++) {
			BigDecimal MontoMonedaSistemaAct = salidaPreliminar.get(k).MontoMonedaSistema.add(salidaPreliminar.get(k).MontoGarantiaMonedaSistema, MathContext.DECIMAL128);;
			BigDecimal MontoMonedaAdicionalAct = salidaPreliminar.get(k).Monto.add(salidaPreliminar.get(k).MontoGarantia, MathContext.DECIMAL128);;
			HashMap<String,Object> tempMontoMercadoActFM = new HashMap<>();
			
			if(resultMontoMonedaFM.get(salidaPreliminar.get(k).IdPortafolio.toString()+salidaPreliminar.get(k).IdTipoInstrumento+salidaPreliminar.get(k).IdMoneda)!=null) {
				MontoMonedaSistemaAct = MontoMonedaSistemaAct.add(new BigDecimal(resultMontoMonedaFM.get(salidaPreliminar.get(k).IdPortafolio.toString()+salidaPreliminar.get(k).IdTipoInstrumento+salidaPreliminar.get(k).IdMoneda).get("MontoMercadoMonedaSistemaActivo").toString()));
				MontoMonedaAdicionalAct = MontoMonedaAdicionalAct.add(new BigDecimal(resultMontoMonedaFM.get(salidaPreliminar.get(k).IdPortafolio.toString()+salidaPreliminar.get(k).IdTipoInstrumento+salidaPreliminar.get(k).IdMoneda).get("MontoMercadoMonedaAdicionalActivo").toString()));
			}
			
			tempMontoMercadoActFM.put("MontoMercadoMonedaSistemaActivo", MontoMonedaSistemaAct);
			tempMontoMercadoActFM.put("MontoMercadoMonedaAdicionalActivo", MontoMonedaAdicionalAct);
			//Llave Hash IdPortafolio-IdTipoInstrumneto-IdMonedaOperacion
			resultMontoMonedaFM.put(salidaPreliminar.get(k).IdPortafolio.toString()+salidaPreliminar.get(k).IdTipoInstrumento+salidaPreliminar.get(k).IdMoneda, tempMontoMercadoActFM);
			
		}
		
			
		
		for(CarteraResumenInversion cartera : carterasResumenInversion) {
			if(cartera.Grupo.equals("FM")) {
				totales.remove(cartera.Grupo + "_" + cartera.IdPortafolio);
			}
		}
		
		for(CarteraResumenInversion cartera : carterasResumenInversion) {
			if(cartera.Grupo.equals("FM")) {
				String clave = "" + cartera.IdPortafolio.toString() + cartera.IdTipoInstrumento + cartera.IdMonedaOperacion;
				if(cartera.IdMonedaOperacion.equals(cartera.IdMonedaSistema)) {
					if(resultMontoMonedaFM.get(clave)!=null) {
						cartera.MontoMonedaSistema = new BigDecimal(resultMontoMonedaFM.get(clave).get("MontoMercadoMonedaSistemaActivo").toString());
						cartera.MontoMonedaAdicional = new BigDecimal(resultMontoMonedaFM.get(clave).get("MontoMercadoMonedaAdicionalActivo").toString());
						cartera.MontoMonedaOperacion = new BigDecimal(resultMontoMonedaFM.get(clave).get("MontoMercadoMonedaSistemaActivo").toString());
					}
				}
				else if(cartera.IdMonedaOperacion.equals(cartera.IdMonedaAdicional)) {
					if(resultMontoMonedaFM.get(clave)!=null) {
						cartera.MontoMonedaSistema = new BigDecimal(resultMontoMonedaFM.get(clave).get("MontoMercadoMonedaSistemaActivo").toString());
						cartera.MontoMonedaAdicional = new BigDecimal(resultMontoMonedaFM.get(clave).get("MontoMercadoMonedaAdicionalActivo").toString());
						cartera.MontoMonedaOperacion = new BigDecimal(resultMontoMonedaFM.get(clave).get("MontoMercadoMonedaAdicionalActivo").toString());
					}
				}				
				Totales total = totales.get(cartera.Grupo + "_" + cartera.IdPortafolio);
				if(total == null) total = new Totales();
				if(cartera.MontoMonedaSistema != null) total.TotalMonedaSistema = total.TotalMonedaSistema.add(cartera.MontoMonedaSistema, MathContext.DECIMAL128);
				if(cartera.MontoMonedaAdicional != null) total.TotalMonedaAdicional = total.TotalMonedaAdicional.add(cartera.MontoMonedaAdicional, MathContext.DECIMAL128);
				totales.put(cartera.Grupo + "_" + cartera.IdPortafolio, total);
				
			}
		}
		long finServicioFFMM = System.currentTimeMillis();
		logger.info("FIN::Cartera FFMM : " + (finServicioFFMM - inicioServicioFFMM));
		
		
		//Fin Cartera FFMM
		
		
		
		//Cartera RF
		
		CarteraRF procesarCarteraRF = new CarteraRF();
		SalidaCartola ObjOutRF= procesarCarteraRF.procesar(con, Objin);
		if(!ObjOutRF.P_OUT_SALIDA.isEmpty() && ObjOutRF.P_OUT_SALIDA.containsKey("carteraRF")) {
			HashMap<String,HashMap> resultMontoMoneda = this.AjustarMontos("carteraRF", ObjOutRF);
				
			for(CarteraResumenInversion cartera : carterasResumenInversion) {
				if(cartera.Grupo.equals("RF")) {
					totales.remove(cartera.Grupo + "_" + cartera.IdPortafolio);
				}
			}
			AjustarTotales("RF", resultMontoMoneda, carterasResumenInversion, totales);
				
		}
		//Fin Cartera RF
		
		
		
		//Cartera IF
		CarteraIF procesarCarteraIF = new CarteraIF();
		SalidaCartola ObjOutIF= procesarCarteraIF.procesar(con, Objin);
		if(!ObjOutIF.P_OUT_SALIDA.isEmpty() && ObjOutIF.P_OUT_SALIDA.containsKey("carteraIF")) {
			HashMap<String,HashMap> resultMontoMoneda = this.AjustarMontos("carteraIF", ObjOutIF);

			for(CarteraResumenInversion cartera : carterasResumenInversion) {
				if(cartera.Grupo.equals("IF")) {
					totales.remove(cartera.Grupo + "_" + cartera.IdPortafolio);
				}
			}
			AjustarTotales("IF", resultMontoMoneda, carterasResumenInversion, totales);
		}
		//Fin Cartera IF
		
		for(CarteraResumenInversion cartera : carterasResumenInversion) {
			if(cartera.MontoMonedaSistema != null && cartera.MontoMonedaAdicional != null) {
				OutResumenInversion outResumenInversion = new OutResumenInversion();
				outResumenInversion = Utils.copyPojo(cartera, outResumenInversion);
				Totales total = totales.get(cartera.Grupo + "_" + cartera.IdPortafolio);

				outResumenInversion.MonedaResumenInversion = cartera.IdMonedaOperacion;
				if(!"CAJA".equals(outResumenInversion.Grupo) && !"OPL".equals(outResumenInversion.Grupo)) {
					outResumenInversion.Item = WordUtils.capitalizeFully(outResumenInversion.Item, new char[]{' '});
				}

				if(total != null) {
					outResumenInversion.TotalMonedaSistema = total.TotalMonedaSistema;
					outResumenInversion.TotalMonedaAdicional = total.TotalMonedaAdicional;
				}
				out.p_out_ResumenInversion.add(outResumenInversion);
			}
		}
		
		for(Long portafolio : portafolios) {
			for(String grupo : GRUPOS) {
				Totales total = totales.get(grupo + "_" + portafolio);
				if(total == null) {
					OutResumenInversion outResumenInversion = new OutResumenInversion();
					outResumenInversion.IdCliente =  hClientesPortafolio.get(portafolio) != null ? ((ClientePortafolio) hClientesPortafolio.get(portafolio)).IdCliente : 0L;
					outResumenInversion.IdPortafolio = portafolio;
					outResumenInversion.Grupo = grupo;
					outResumenInversion.Item = "";
					outResumenInversion.IdMonedaOperacion = monedaSistema;
					outResumenInversion.MontoMonedaOperacion = BigDecimal.ZERO;
					outResumenInversion.IdMonedaSistema = monedaSistema;
					outResumenInversion.IdMonedaSistema = monedaSistema;
					outResumenInversion.MontoMonedaSistema = BigDecimal.ZERO;
					outResumenInversion.TotalMonedaSistema = BigDecimal.ZERO;
					outResumenInversion.IdMonedaAdicional = monedaAdicional;
					outResumenInversion.MontoMonedaAdicional = BigDecimal.ZERO;
					outResumenInversion.TotalMonedaAdicional = BigDecimal.ZERO;
					outResumenInversion.MonedaResumenInversion = "";
					out.p_out_ResumenInversion.add(outResumenInversion);
				}
			}
		}
		if(primeraEjecucion) {
			try {
				List<OutResumenInversionPaso> listOutPaso =  new ArrayList();
				for(OutResumenInversion salida : out.p_out_ResumenInversion) {
					OutResumenInversionPaso outPaso = new OutResumenInversionPaso();
					outPaso.IdNomina = nomina.IdNomina;
					outPaso.IdTrabajo = idTrabajo;
					outPaso.IdCliente =  salida.IdCliente;
					outPaso.IdPortafolio = salida.IdPortafolio;
					outPaso.Grupo = salida.Grupo;
					outPaso.Item = salida.Item;
					outPaso.IdMonedaOperacion = salida.IdMonedaOperacion;
					outPaso.MontoMonedaOperacion = salida.MontoMonedaOperacion;
					outPaso.IdMonedaSistema = salida.IdMonedaSistema;
					outPaso.MontoMonedaSistema = salida.MontoMonedaSistema;
					outPaso.TotalMonedaSistema = salida.TotalMonedaSistema;
					outPaso.IdMonedaAdicional = salida.IdMonedaAdicional;
					outPaso.MontoMonedaAdicional = salida.MontoMonedaAdicional;
					outPaso.TotalMonedaAdicional = salida.TotalMonedaAdicional;
					outPaso.MonedaResumenInversion = salida.MonedaResumenInversion;
					listOutPaso.add(outPaso);
				}
				sql.batch(con, SQL_INSERT_RESUMEN_INVERSION_BASE_OUT, listOutPaso, OutResumenInversionPaso.class);
			} catch (Exception e) {
				logger.info("INICIO::ResumenInversionBase -ERROR SQL_SELECT_OUT_SERVICIO_EJECUTADO "+e);
			}
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ResumenInversionBase", finServicio - inicioServicio);
		return out;
	}
	
	
private String verificacionFecha(Connection con, Date fechaHasta, Long idEmpresa, Long idPaisSistema) throws Exception {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		String fechaHastaString = dateFormat.format(fechaHasta);  

	    
	    boolean encontrado = true;
	    int cont = 0;
	    
		 
		String fechaReturn = null;
		
		Calendar cal = Calendar.getInstance();
	    String fechaOut = "";
	    
	    while(encontrado != false ) {
	    	
	    		cal.setTime(Fecha.toDate(fechaHastaString));
	    		cal.add(Calendar.DATE, -cont);//cont = 1
	    		String dateComp =  dateFormat.format(cal.getTime());
	    		
	    		cal.setTime(Fecha.toDate(fechaHastaString));
	    		cont++;//aumentamos para dar con fecha de comparacion
	    		cal.add(Calendar.DATE, -cont);//cont = 2
	    		
	    		java.util.Date fechaParaDiaSiguiente = cal.getTime();
	    		fechaReturn = null;//limpiamos obj
	    		fechaReturn = dateFormat.format(fechaParaDiaSiguiente);//set fecha
	    		
	    		OutObtenerDiaSiguiente salDiaSiguiente = ObtenerDiaAnteriorHabil(con, fechaReturn, idPaisSistema);
				String siguienteDiaHabil = salDiaSiguiente.p_out_fechas.get(0).fechaCalculada.toString();
				
				if(siguienteDiaHabil.equalsIgnoreCase(dateComp)) {
					//dia siguiente = dia comparativo -> dia habil
					fechaOut = siguienteDiaHabil;
					encontrado = false;
				}
	    }
	  
		
		return fechaOut;
	}

	private OutObtenerDiaSiguiente ObtenerDiaAnteriorHabil(Connection con, String fechaReturn, Long idPaisSistema)
			throws Exception {
		ObtenerDiaSiguiente diaHabilSiguiente = new ObtenerDiaSiguiente();
		InObtenerDiaSiguiente diaHabilSiguienteIn = new InObtenerDiaSiguiente();
		diaHabilSiguienteIn.p_idPais = idPaisSistema;
		diaHabilSiguienteIn.p_diaHabil = "S";
		FechasIn fecha = new FechasIn();
		fecha.fechaInicial = fechaReturn;
		fecha.numeroDias = 1L;
		fecha.pais = idPaisSistema;
		diaHabilSiguienteIn.p_fechas.add(fecha);
		OutObtenerDiaSiguiente salDiaSiguiente =  diaHabilSiguiente.logica(con, diaHabilSiguienteIn);
		return salDiaSiguiente;
	}
	
	
	
	private List<ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia> obtenerCustodias(Connection con, EntradaResumenInversionBase entrada,Dato dato) throws Exception{
		logger.info("INICIO::resultadoCarteraFM.obtenerCustodias");
		long inicio = System.currentTimeMillis();
		ConsultarCustodiaPortafolioFondoMutuo consultarCustodiaPortafolioFondoMutuo = new ConsultarCustodiaPortafolioFondoMutuo();
		EntradaConsultarCustodiaPortafolioFondoMutuo entradaConsultarCustodiaPortafolioFondoMutuo = new EntradaConsultarCustodiaPortafolioFondoMutuo();
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdModulo = "FMM";
		entradaConsultarCustodiaPortafolioFondoMutuo.p_FechaConsulta =  Fecha.stringToSqlDate(entrada.p_FechaHasta);
		entradaConsultarCustodiaPortafolioFondoMutuo.p_IdProductoAgrupador = null;
		if (dato == null) {
			entradaConsultarCustodiaPortafolioFondoMutuo.p_IdCliente = null;
			entradaConsultarCustodiaPortafolioFondoMutuo.p_IdPortafolio = null;
		}
		else {
			entradaConsultarCustodiaPortafolioFondoMutuo.p_IdCliente = dato.IdCliente;
			entradaConsultarCustodiaPortafolioFondoMutuo.p_IdPortafolio = dato.IdPortafolio;
		}
		SalidaConsultarCustodiaPortafolioFondoMutuo salidaConsultarCustodiaPortafolioFondoMutuo = consultarCustodiaPortafolioFondoMutuo.logica(con, entradaConsultarCustodiaPortafolioFondoMutuo);
		
		
		List<ConsultarCustodiaPortafolioFondoMutuo.SalidaCustodia> custodia = salidaConsultarCustodiaPortafolioFondoMutuo.P_OUT_TBL;
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::resultadoCarteraFM.obtenerCustodias", fin - inicio);
		return custodia;
	}
	
	private List<SaldoPorFechaEfectiva> obtenerSaldoMasivo (Connection con, EntradaResumenInversionBase entrada,List<Long> portafolios) throws Exception{
		logger.info("INICIO::resultado.obtenerSaldoMasivo");
		long inicio = System.currentTimeMillis();
		
		
		ConsultarSaldoCuentaInversion servicio = new ConsultarSaldoCuentaInversion();
		EntradaConsultarSaldoCuentaInversion entradaConsultarSaldoCuentaInversion = new EntradaConsultarSaldoCuentaInversion();
		SalidaConsultarSaldoCuentaInversion salidaCtaInv = new SalidaConsultarSaldoCuentaInversion();	
		entradaConsultarSaldoCuentaInversion.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarSaldoCuentaInversion.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarSaldoCuentaInversion.p_IdModulo = entrada.p_IdModulo;
		entradaConsultarSaldoCuentaInversion.p_UsaReglasNegocio = "S";
		entradaConsultarSaldoCuentaInversion.p_IncluyeOtrosSaldos = "S";			
		entradaConsultarSaldoCuentaInversion.p_FechaDesde = entrada.p_FechaDesde.toString();
		entradaConsultarSaldoCuentaInversion.p_FechaHasta = entrada.p_FechaHasta.toString();
		entradaConsultarSaldoCuentaInversion.p_CuentaInversionAConsultar = new ArrayList<>();
		
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		List<Portafolio> portObj = new ArrayList<>();
		
		for (Long p : portafolios) {
			Portafolio por = new Portafolio();
			por.IdTrabajo = idTrabajo;
			por.IdPortafolio = p;
			portObj.add(por);
		}
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portObj, Portafolio.class);
		
		List<InCuentaInversionConsultar> ctas = sql.query(con, SQL_SELECT_CUENTA_INVERSION, new HashMap<>() , InCuentaInversionConsultar.class);
		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));
		entradaConsultarSaldoCuentaInversion.p_CuentaInversionAConsultar = ctas;
		
		salidaCtaInv = servicio.logica(con, entradaConsultarSaldoCuentaInversion);	
		List<SaldoPorFechaEfectiva> saldo = salidaCtaInv.P_OUT_SALDOPORFECHAEFECTIVA;
		long fin = System.currentTimeMillis();
		logger.info("FIN::resultado.obtenerSaldoMasivo", fin - inicio);
		return saldo;
	}
	
	private String armarClave(String llave, String Tipo) throws Exception {
		String clave = llave;
		if(Tipo.equals("TOTAL")) {
			clave = llave + Tipo;
		}
		return clave;
	}
	
	private HashMap AjustarMontos(String TipoCartera, SalidaCartola ObjOut) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		
		List<SalidaCarteraIF> salidaIF;
		List<SalidaCarteraRF> salidaRF;
		List arregloCarteras = null;
		
		if(TipoCartera.equals("carteraIF")) {
			salidaIF = objMapper.convertValue(ObjOut.P_OUT_SALIDA.get(TipoCartera), List.class);
			arregloCarteras = salidaIF;
		}else if(TipoCartera.equals("carteraRF")) {
			salidaRF = objMapper.convertValue(ObjOut.P_OUT_SALIDA.get(TipoCartera), List.class);
			arregloCarteras = salidaRF;
		}
		
		HashMap<String,HashMap> resultMontoMoneda = new HashMap<>();
		if(!(arregloCarteras==null)) {
			for(Object carteraIndividual : arregloCarteras.toArray()) {
				BigDecimal MontoMercadoMonedaSistema = null;
				BigDecimal MontoMercadoMonedaAdicional = null;
				String 	   Moneda;
				String key = null;
				
				if (TipoCartera.equals("carteraIF")) {
					SalidaCarteraIF carteraIF = objMapper.convertValue(carteraIndividual, SalidaCarteraIF.class);
					MontoMercadoMonedaSistema = carteraIF.MontoMercadoMonedaSistema;//ValorActual
					MontoMercadoMonedaAdicional = carteraIF.MontoMercadoMonedaAdicional;//ValorActualAdicional
					Moneda = carteraIF.IdMonedaInstrumento;
					key = this.armarClave(carteraIF.IdPortafolio.toString()+carteraIF.IdTipoInstrumento+Moneda,"TOTAL");
				}
				else if(TipoCartera.equals("carteraRF")) {
					SalidaCarteraRF carteraRF = objMapper.convertValue(carteraIndividual, SalidaCarteraRF.class);
					MontoMercadoMonedaSistema = carteraRF.MontoMercadoMonedaSistema;//ValorActual
					MontoMercadoMonedaAdicional = carteraRF.MontoMercadoMonedaAdicional;//ValorActualAdicional
					Moneda = carteraRF.IdMonedaInstrumento;
					key = this.armarClave(carteraRF.IdPortafolio.toString()+carteraRF.IdTipoInstrumento+Moneda,"TOTAL");
				}
				
				HashMap<String,Object> tempMontoMercadoAct = new HashMap<>();
				if(resultMontoMoneda.get(key)!=null) {
						BigDecimal HashMontoMercadoMonedaSistema = (BigDecimal) resultMontoMoneda.get(key).get("MontoMercadoMonedaSistema");
						MontoMercadoMonedaSistema = MontoMercadoMonedaSistema.add(HashMontoMercadoMonedaSistema);
						MontoMercadoMonedaAdicional = MontoMercadoMonedaAdicional.add(new BigDecimal(resultMontoMoneda.get(key).get("MontoMercadoMonedaAdicional").toString()));
				}
					
				tempMontoMercadoAct.put("MontoMercadoMonedaSistema", MontoMercadoMonedaSistema);
				tempMontoMercadoAct.put("MontoMercadoMonedaAdicional", MontoMercadoMonedaAdicional);	
					
				resultMontoMoneda.put(key, tempMontoMercadoAct);
			}
		}
		
		return resultMontoMoneda;
	}
	
	private void AjustarTotales(String TipoMercado, HashMap<String,HashMap> resultMontoMoneda, List<CarteraResumenInversion> carterasResumenInversion, HashMap<String,Totales> totales) throws Exception {
		for(CarteraResumenInversion cartera : carterasResumenInversion) {
			if(cartera.Grupo.equals(TipoMercado)) {
				String clave = this.armarClave(cartera.IdPortafolio.toString()+cartera.IdTipoInstrumento+cartera.IdMonedaOperacion,"TOTAL");
				if(cartera.IdMonedaOperacion.equals(cartera.IdMonedaSistema)) {
					if(resultMontoMoneda.get(clave)!=null) {
						cartera.MontoMonedaSistema = new BigDecimal(resultMontoMoneda.get(clave).get("MontoMercadoMonedaSistema").toString());
						cartera.MontoMonedaAdicional = new BigDecimal(resultMontoMoneda.get(clave).get("MontoMercadoMonedaAdicional").toString());
						cartera.MontoMonedaOperacion = new BigDecimal(resultMontoMoneda.get(clave).get("MontoMercadoMonedaSistema").toString());
					}
				}
				else if(cartera.IdMonedaOperacion.equals(cartera.IdMonedaAdicional)) {
					if(resultMontoMoneda.get(clave)!=null) {
						cartera.MontoMonedaSistema = new BigDecimal(resultMontoMoneda.get(clave).get("MontoMercadoMonedaSistema").toString());
						cartera.MontoMonedaAdicional = new BigDecimal(resultMontoMoneda.get(clave).get("MontoMercadoMonedaAdicional").toString());
						cartera.MontoMonedaOperacion = new BigDecimal(resultMontoMoneda.get(clave).get("MontoMercadoMonedaAdicional").toString());
					}
				}				
				Totales total = totales.get(cartera.Grupo + "_" + cartera.IdPortafolio);
				if(total == null) total = new Totales();
				if(cartera.MontoMonedaSistema != null) total.TotalMonedaSistema = total.TotalMonedaSistema.add(cartera.MontoMonedaSistema, MathContext.DECIMAL128);
				if(cartera.MontoMonedaAdicional != null) total.TotalMonedaAdicional = total.TotalMonedaAdicional.add(cartera.MontoMonedaAdicional, MathContext.DECIMAL128);
				totales.put(cartera.Grupo + "_" + cartera.IdPortafolio, total);
					
			}
		}
		
		return;	
	}
	
	private void actualizarResumenCarteraRV (Connection con, EntradaResumenInversionBase entrada, Long idPaisSistema, List<CarteraDetalleInversion> carterasRVDetalleInversion, List<CarteraResumenInversion> carterasResumenInversion) throws Exception{
		logger.trace("INICIO::ResumenInversionBase.actualizarResumenCarteraRV");
		long inicio = System.currentTimeMillis();
		
		EntregarPreciosTasas_Entrada entregarPreciosTasasEntrada = new EntregarPreciosTasas_Entrada();
		
		entregarPreciosTasasEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		entregarPreciosTasasEntrada.p_IdModulo = entrada.p_IdModulo;
		entregarPreciosTasasEntrada.p_IdPais = idPaisSistema ;		
		entregarPreciosTasasEntrada.p_IdUsuario = entrada.p_IdUsuario;
		
		Long id = 1L;
		
		for(CarteraDetalleInversion cartera :carterasRVDetalleInversion) {
			EntregarPreciosTasas_Entrada_DatosInstrumentos entradaDatosInstrumentos = new EntregarPreciosTasas_Entrada_DatosInstrumentos();
			cartera.IdInterno = id;
			entradaDatosInstrumentos.IdRegistro = id;
			entradaDatosInstrumentos.TipoCartera = "0";
			entradaDatosInstrumentos.IdUso = "CARTERA";
			entradaDatosInstrumentos.IdTipoCriterioTasa = "0";
			entradaDatosInstrumentos.IdClasificacionTipoInstrumento = cartera.IdClasificacionTipoInstrumento;
			entradaDatosInstrumentos.IdTipoInstrumento =cartera.IdTipoInstrumento;
			entradaDatosInstrumentos.IdInstrumento = cartera.IdInstrumento;
			entradaDatosInstrumentos.Nemotecnico = cartera.Nemotecnico;
			entradaDatosInstrumentos.IdMoneda = cartera.IdMonedaOperacion;
			entradaDatosInstrumentos.FechaConsulta = entrada.p_FechaHasta;  
			entradaDatosInstrumentos.PlazoResidual = 0L;
			entregarPreciosTasasEntrada.p_DatosInstrumentos.add(entradaDatosInstrumentos);
			id++;
		}
		
		if (entregarPreciosTasasEntrada.p_DatosInstrumentos.size() > 0) {
			EntregarPreciosTasas entregarPreciosTasasService = new EntregarPreciosTasas();
			EntregarPreciosTasas_Salida entregarPreciosTasasSalida = entregarPreciosTasasService.logica(con, entregarPreciosTasasEntrada);
			if(entregarPreciosTasasSalida != null && entregarPreciosTasasSalida.P_OUT_ERRORESCONTADOR.compareTo(0L)== 0) {
				HashMap<String, Object> precios = Utils.List2Hash(entregarPreciosTasasSalida.P_OUT_PRECIOSTASAS,"IdRegistro");
				
				List<String> listaClavesResumenInversion = new ArrayList<String>();
				List<CarteraResumenInversion> carterasRVResumenInversion = new ArrayList<CarteraResumenInversion>();
				
				for(CarteraDetalleInversion cartera :carterasRVDetalleInversion) {
					EntregarPreciosTasas_Salida_PreciosTasas registroPrecio = (EntregarPreciosTasas_Salida_PreciosTasas) precios.get(cartera.IdInterno);
					if(registroPrecio != null) {
						BigDecimal precio = registroPrecio.Precio != null ? registroPrecio.Precio : BigDecimal.ZERO;
						cartera.MontoMonedaOperacion = precio.multiply(cartera.TotalCantidad, MathContext.DECIMAL128);
					}
					
					String clave = String.join("_",cartera.IdCliente.toString(), cartera.IdPortafolio.toString(), cartera.IdClasificacionTipoInstrumento, cartera.Item, cartera.IdTipoInstrumento, cartera.IdMonedaOperacion);
					if(!listaClavesResumenInversion.contains(clave)) {
						listaClavesResumenInversion.add(clave);
						CarteraResumenInversion carteraResumen = new CarteraResumenInversion();
						carteraResumen.Grupo = cartera.IdClasificacionTipoInstrumento;
						carteraResumen.IdCliente = cartera.IdCliente;
						carteraResumen.IdPortafolio = cartera.IdPortafolio;
						carteraResumen.IdMonedaOperacion = cartera.IdMonedaOperacion;
						carteraResumen.IdTipoInstrumento = cartera.IdTipoInstrumento;
						carteraResumen.MontoMonedaOperacion = BigDecimal.ZERO;
						carteraResumen.Item = cartera.Item;
						carterasRVResumenInversion.add(carteraResumen);
					}
				}
				
				if(!carterasRVResumenInversion.isEmpty()) {
					
					for(CarteraResumenInversion carteraRV: carterasRVResumenInversion) {
						BigDecimal monto = carterasRVDetalleInversion.stream()
							.filter(cartera -> cartera.IdCliente.compareTo(carteraRV.IdCliente) == 0 && cartera.IdPortafolio.compareTo(carteraRV.IdPortafolio) == 0 &&
								cartera.IdClasificacionTipoInstrumento.equalsIgnoreCase(carteraRV.Grupo) && cartera.IdTipoInstrumento.equalsIgnoreCase(carteraRV.IdTipoInstrumento) &&
								cartera.IdMonedaOperacion.equalsIgnoreCase(carteraRV.IdMonedaOperacion) && cartera.Item.equalsIgnoreCase(carteraRV.Item)
									)
							.map(cartera -> cartera.MontoMonedaOperacion)
							.reduce(BigDecimal.ZERO, BigDecimal::add);
						
						carteraRV.MontoMonedaOperacion = monto;
					}
					
					Map<String, CarteraResumenInversion> hashResumenInversion =  carterasResumenInversion.stream()
							.collect(Collectors.toMap(cartera-> String.join("_",cartera.IdCliente.toString(), cartera.IdPortafolio.toString(), cartera.Grupo, cartera.Item, cartera.IdTipoInstrumento, cartera.IdMonedaOperacion), cartera -> cartera, (a,b) -> a)
							);
					
					for(CarteraResumenInversion carteraRV: carterasRVResumenInversion) {
						String clave = String.join("_",carteraRV.IdCliente.toString(), carteraRV.IdPortafolio.toString(), carteraRV.Grupo, carteraRV.Item, carteraRV.IdTipoInstrumento, carteraRV.IdMonedaOperacion);
						CarteraResumenInversion carteraResumen = (CarteraResumenInversion) hashResumenInversion.get(clave);
						if(carteraResumen != null) {
							carteraResumen.MontoMonedaOperacion = carteraResumen.MontoMonedaOperacion.add(carteraRV.MontoMonedaOperacion, MathContext.DECIMAL128);
						}else {
							carterasResumenInversion.add(carteraRV);
						}
					}
				}
			}	
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ResumenInversionBase.actualizarResumenCarteraRV tiempo: " + (fin - inicio));
	}
	
	private List<CarteraDetalleInversion> obtenerDetalleCarteraRV (Connection con, EntradaResumenInversionBase entrada, Long idTrabajo, Date fechaHastaFixDateType , String fechaSistema, SQL sql) throws SQLException{
		logger.debug("INICIO::ResumenInversionBase.obtenerDetalleCarteraRV");
		long inicio = System.currentTimeMillis();
		
		HashMap<String,Object> params = new HashMap<String,Object>(4);
		
		params.put("FechaHasta", fechaHastaFixDateType);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdProductoAgrupador", entrada.p_IdProductoAgrupador);
		params.put("IdTrabajo", idTrabajo);
		
		List<CarteraDetalleInversion> carterasRVDetalleInversion = new ArrayList<CarteraDetalleInversion>();
					
	    if(fechaSistema.equals(Fecha.stringToSqlDate(entrada.p_FechaHasta))){
	    	carterasRVDetalleInversion = sql.query(con,SQL_SELECT_DETALLE_MOVIMIENTOS_CUSTODIA_PORTAFOLIO, params, CarteraDetalleInversion.class);
	    }else {
	    	carterasRVDetalleInversion = sql.query(con,SQL_SELECT_DETALLE_MOVIMIENTOS_CUSTODIA_HISTORICO_PORTAFOLIO, params, CarteraDetalleInversion.class);
	    }
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ResumenInversionBase.obtenerDetalleCarteraRV tiempo: " + (fin - inicio));
		return carterasRVDetalleInversion;
	}
		
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getMBIPREConnection()) {
		EntradaResumenInversionBase Objin = new EntradaResumenInversionBase();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 288L;
		Objin.p_FechaDesde = Fecha.toDate("2022-01-01");
		Objin.p_FechaHasta = Fecha.toDate("2022-01-31");
		Dato dato = new Dato();
		dato.IdCliente =2956L;
		dato.IdPortafolio =6104L;

		Objin.p_Datos.add(dato);
		ResumenInversionBase  demo= new ResumenInversionBase();				
		try {
			SalidaResumenInversionBase ObjOut=demo.logica(con, Objin);
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
