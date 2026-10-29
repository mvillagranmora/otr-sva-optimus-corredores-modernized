package cl.bcs.socionegocio.cli;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import cl.bcs.utils.Fecha;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.cartola.ResumenInversionWrapper.EntradaResumenInversionWrapper;

import cl.bcs.normativa.cartola.ResumenInversionWrapper;
import cl.bcs.normativa.cartola.BcsCartola.Dato;
import cl.bcs.normativa.cartola.BcsCartola.EntradaCartola;
import cl.bcs.normativa.cartola.BcsCartola.SalidaCartola;
import cl.bcs.normativa.cartola.CarteraFwd;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

import cl.bcs.plataforma.pla.ConsultarFlujoCaja;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.EntradaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.PortafolioAConsultar;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.SalidaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;

import cl.bcs.ordenes.ConsultarOrdenes;
import cl.bcs.ordenes.ConsultarOrdenes.ConsultarOrdenes_Entrada;
import cl.bcs.ordenes.ConsultarOrdenes.ConsultarOrdenes_Salida;
import cl.bcs.ordenes.ConsultarOrdenes.Orden_Salida;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.EntradaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.SalidaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaEfectiva;
import cl.bcs.custodia.ConsultarCarteras;
import cl.bcs.custodia.ConsultarCarteras.ConsultarCarteraIn;
import cl.bcs.custodia.ConsultarCarteras.ConsultarCarterasOut;

import cl.bcs.custodia.ConsultarCustodiaPortafolio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Entrada_ConsultaCustodiaPortafolio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Salida_ConsultaCustodiaPortafolio;
import cl.bcs.custodia.ConsultarCustodiaPortafolio.Tabla_Salida;

public class ResumenPortafolioCliente implements JavaCall{
	
	private final Log log = new Log("ResumenPortafolioCliente");
	private final String SQL_CONSULTA_INFORMACION_EJECUTIVO = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConsultarInformacionEjecutivo.sql");
	private final String SQL_CONSULTA_USO_FLUJO_CAJA = loadResourceToString("cl/bcs/socionegocio/cli/sql/ConsultarUsoFlujoCaja.sql");

	public static class EntradaResumenPortafolioCliente{
		public Long 	p_IdEmpresa;
		public Long 	p_IdCliente;
		public Long 	p_IdUsuario;
//		public String 	p_IdModulo;
		public Long 	p_IdPortafolio;
		public String 	p_IdMonedaFlujoCaja;
		public String 	p_FechaFlujoCaja;
		public String 	p_FechaDesde;
		public String 	p_FechaHasta;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class SalidaResumenPortafolioCliente{
		public String P_OUT_FECHACONSULTA="";
		public List<SalidaInformacionEjecutivo> P_OUT_INFORMACION_EJECUTIVO;
		public List<SalidaComposicionActivos> P_OUT_COMPOSICION_ACTIVOS;
		public List<SalidaCustodiaChart> P_OUT_CUSTODIACHART;
		public List<ObjFLUJOCAJA> P_OUT_FLUJOCAJA;
		public List<SalidaUsoFlujoCaja> P_OUT_USOS;
		public List<SalidaOrdenes> P_OUT_ORDENES;
		public List <Tabla_Salida> P_OUT_GRIDCUSTODIA;
		public List <cl.bcs.custodia.ConsultarCarteras.Tabla_Salida> P_OUT_GRIDCARTERA;
		//AQUI AGREGO TODAS LAS SALIDAS. EJECUTIVO, GRAF1,GRAFN,TABLA1,TABLAN
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public String P_OUT_MENSAJE = "";
		
		public SalidaResumenPortafolioCliente(){
			this.P_OUT_INFORMACION_EJECUTIVO = new ArrayList<SalidaInformacionEjecutivo>();
			this.P_OUT_COMPOSICION_ACTIVOS = new ArrayList<SalidaComposicionActivos>();
			this.P_OUT_CUSTODIACHART = new ArrayList<SalidaCustodiaChart>();
			this.P_OUT_FLUJOCAJA = new ArrayList<ObjFLUJOCAJA>();
			this.P_OUT_ORDENES = new ArrayList<SalidaOrdenes>();
			this.P_OUT_GRIDCUSTODIA = new ArrayList<Tabla_Salida>();
			this.P_OUT_GRIDCARTERA = new ArrayList<cl.bcs.custodia.ConsultarCarteras.Tabla_Salida>();
			this.P_OUT_USOS = new ArrayList<SalidaUsoFlujoCaja>();

		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class SalidaInformacionEjecutivo{
		
		public String NombreEjecutivo;
		public String TelefonoEjecutivo;
		public String EmailEjecutivo;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class SalidaUsoFlujoCaja{
		public String Nombre;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class SalidaComposicionActivos{
		
		public String nombreGrupo;
		public String clasificacionGrupo;
		public BigDecimal porcentaje;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class SalidaCustodiaChart{
	    public String IdClasificacionTipoInstrumento;
	    public String Nemotecnico;
	    public BigDecimal Monto;
	    public BigDecimal Cantidad;
	    public BigDecimal Precio;
	    public BigDecimal Rentabilidad;
	    public String Moneda;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class SalidaOrdenes{
		public Long IdOrden;
	    public Timestamp FechaOperacionOrden;
	    public String IdClasificacionTipoInstrumento;
	    public String AccionTipoOperacion;
	    public String InstrumentoUI;
	    public String IdMonedaOrden;
	    public String EstadoOrden;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}


	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();		
		EntradaResumenPortafolioCliente jin = gson.fromJson(in.toString(), EntradaResumenPortafolioCliente.class);
		SalidaResumenPortafolioCliente out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaResumenPortafolioCliente logica(Connection con, EntradaResumenPortafolioCliente entrada) throws Exception {
		log.write("INICIO::ResumenPortafolioCliente");
		SalidaResumenPortafolioCliente out = new SalidaResumenPortafolioCliente();
		
		SQL sql=new SQL();
		
		try{
			// FECHA CONSULTA Y DATA EJECUTIVO. 
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
			DateTimeFormatter in = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			DateTimeFormatter salida = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			LocalDate fecha = LocalDate.parse(fechaSistema, in);
			String fechaSistemaFormatted = fecha.format(salida);
	        String horaReal = FechaDB.ObtenerHoraSistemaLocal(con, entrada.p_IdEmpresa);
			out.P_OUT_FECHACONSULTA	= fechaSistemaFormatted+" "+horaReal;
			HashMap<String,Object> params =new HashMap<String,Object>();
			params.put("p_IdPortafolio", entrada.p_IdPortafolio);
			List<SalidaInformacionEjecutivo> SalidaInformacionEjecutivo = sql.query(con, SQL_CONSULTA_INFORMACION_EJECUTIVO, params, SalidaInformacionEjecutivo.class);
			out.P_OUT_INFORMACION_EJECUTIVO = SalidaInformacionEjecutivo;
			
			// COMPOSICION ACTIVOS
			EntradaResumenInversionWrapper EntradaComposicionActivos = new EntradaResumenInversionWrapper();
			EntradaComposicionActivos.p_IdEmpresa = entrada.p_IdEmpresa;
			EntradaComposicionActivos.p_FechaDesde = fechaSistema;
			EntradaComposicionActivos.p_FechaHasta = fechaSistema;
			EntradaComposicionActivos.p_IdProductoAgrupador = null;
	        Dato dato = new Dato();
	        dato.IdCliente    = entrada.p_IdCliente;
	        dato.IdPortafolio = entrada.p_IdPortafolio;
	        EntradaComposicionActivos.p_Datos.add(dato);
	        out.P_OUT_COMPOSICION_ACTIVOS = ComposicionActivos(con, EntradaComposicionActivos);
	        
	        // CUSTODIA CHART
	        // Consultar Custodia
			Entrada_ConsultaCustodiaPortafolio entradaCustodia = new Entrada_ConsultaCustodiaPortafolio();
			entradaCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCustodia.p_IdModulo = "SOC";
			entradaCustodia.p_IdUsuario = entrada.p_IdUsuario;
			entradaCustodia.p_AgrupaPorBoveda = "S";
			entradaCustodia.p_AgrupaPorRelacion = "S";
			entradaCustodia.p_FechaConsulta = fechaSistema;
			entradaCustodia.p_IdBoveda = "";
			entradaCustodia.p_IdClasificacionTipoInstrumento = "";
			entradaCustodia.p_IdCliente = entrada.p_IdCliente;
			entradaCustodia.p_IdInstrumento = 0L;
			entradaCustodia.p_IdPortafolio = entrada.p_IdPortafolio;
			entradaCustodia.p_IdProductoAgrupador = null;
			entradaCustodia.p_IdTipoInstrumento = "";
			entradaCustodia.p_SaldosNegativos = "N";
			entradaCustodia.p_SaldosNegativosPresuntos = "N";
			entradaCustodia.p_TipoFechaConsulta = "ADQ";
			entradaCustodia.p_SolicitaDividendo = "S";
			
			ConsultarCustodiaPortafolio consultarCustodiaPortafolio = new ConsultarCustodiaPortafolio();
			Salida_ConsultaCustodiaPortafolio salidaCustodiaPortafolio = consultarCustodiaPortafolio.logica(con, entradaCustodia);
			// Consultar FWD
			EntradaCartola entradaFWD = new EntradaCartola();
			entradaFWD.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaFWD.p_IdModulo = "SOC";
			entradaFWD.p_IdUsuario = entrada.p_IdUsuario;
			entradaFWD.p_FechaDesde = Fecha.toDate(fechaSistema);
			entradaFWD.p_FechaHasta = Fecha.toDate(fechaSistema);
			Dato datoFwd = new Dato();
			datoFwd.IdCliente = entrada.p_IdCliente;
			datoFwd.IdPortafolio = entrada.p_IdPortafolio;
			entradaFWD.p_Datos.add(datoFwd);
			CarteraFwd consultaFWD= new CarteraFwd();
			SalidaCartola ObjOut=consultaFWD.procesar(con, entradaFWD);
			//Procesar Custodia y anexar a SalidaCustodiaChart
			List<SalidaCustodiaChart> SalidaCustodiaChart = new ArrayList<>();
			if (salidaCustodiaPortafolio != null && salidaCustodiaPortafolio.P_OUT_TBL != null) {
			    // Agrupar por IdClasificacionTipoInstrumento
			    Map<String, List<Tabla_Salida>> agrupado = new LinkedHashMap<>();
			    for (Tabla_Salida CustodiaPortafolio : salidaCustodiaPortafolio.P_OUT_TBL) {
			        String clasificacion = CustodiaPortafolio.IdClasificacionTipoInstrumento;
			        agrupado.computeIfAbsent(clasificacion, k -> new ArrayList<>()).add(CustodiaPortafolio);
			    }
			    // Por cada clasificacion, ordenar por MontoMercadoCustodia (Monto moneda Local) desc y tomar top 10
			    for (Map.Entry<String, List<Tabla_Salida>> entry : agrupado.entrySet()) {
			    	List<Tabla_Salida> lista = entry.getValue();
			    	lista.sort((a, b) -> {
			    	    BigDecimal montA = a.MontoMercadoCustodia != null ? a.MontoMercadoCustodia : BigDecimal.valueOf(Long.MIN_VALUE);
			    	    BigDecimal montB = b.MontoMercadoCustodia != null ? b.MontoMercadoCustodia : BigDecimal.valueOf(Long.MIN_VALUE);
			    	    return montB.compareTo(montA);
			    	});
			    	List<Tabla_Salida> top10 = lista.subList(0, Math.min(10, lista.size()));

			        for (Tabla_Salida CustodiaPortafolio : top10) {
			            SalidaCustodiaChart CustPort = new SalidaCustodiaChart();
			            CustPort.IdClasificacionTipoInstrumento = CustodiaPortafolio.IdClasificacionTipoInstrumento;
			            CustPort.Nemotecnico                    = CustodiaPortafolio.Nemotecnico;
			            CustPort.Monto                          = CustodiaPortafolio.ValorMercadoCustodia;
			            CustPort.Cantidad                       = CustodiaPortafolio.CantidadCustodia;
			            CustPort.Precio                         = CustodiaPortafolio.PrecioMercadoCustodia;
			            CustPort.Rentabilidad                   = CustodiaPortafolio.Rentabilidad;
			            CustPort.Moneda                         = CustodiaPortafolio.IdMonedaPrincipal;
			            SalidaCustodiaChart.add(CustPort);
			        }
			    }
			}
			// Procesar FWD y anexar a SalidaCustodiaChart
			if (ObjOut != null && ObjOut.P_OUT_SALIDA != null) {
			    Object carteraFwdObj = ObjOut.P_OUT_SALIDA.get("carteraFwd");

			    if (carteraFwdObj instanceof List) {
			        List<HashMap<String, Object>> listaFwd = (List<HashMap<String, Object>>) carteraFwdObj;

			        // Agrupar por Nemotecnico + moneda
			        Map<String, List<HashMap<String, Object>>> agrupadoFwd = new LinkedHashMap<>();
			        for (HashMap<String, Object> fwd : listaFwd) {
			            String key = fwd.get("Nemotecnico") + "|" + fwd.get("moneda");
			            agrupadoFwd.computeIfAbsent(key, k -> new ArrayList<>()).add(fwd);
			        }

			        // Por cada grupo, generar el pie de resumen
			        for (Map.Entry<String, List<HashMap<String, Object>>> entry : agrupadoFwd.entrySet()) {
			            List<HashMap<String, Object>> grupoFwd = entry.getValue();

			            BigDecimal montoTotal    = BigDecimal.ZERO;
			            BigDecimal resultadoNeto = BigDecimal.ZERO;
			            String moneda            = null;
			            String nemotecnico       = null;

			            for (HashMap<String, Object> fwd : grupoFwd) {
			                moneda      = (String) fwd.get("moneda");
			                nemotecnico = (String) fwd.get("Nemotecnico");
			                String tipo = (String) fwd.get("tipo");

			                BigDecimal montoRecibe = fwd.get("montoRecibe") != null 
			                    ? new BigDecimal(fwd.get("montoRecibe").toString()) 
			                    : BigDecimal.ZERO;

			                BigDecimal resultado = fwd.get("resultado") != null 
			                    ? new BigDecimal(fwd.get("resultado").toString()) 
			                    : BigDecimal.ZERO;

			                if ("C".equalsIgnoreCase(tipo)) {
			                    montoTotal = montoTotal.add(montoRecibe);
			                } else if ("V".equalsIgnoreCase(tipo)) {
			                    montoTotal = montoTotal.subtract(montoRecibe);
			                }

			                resultadoNeto = resultadoNeto.add(resultado);
			            }

			            SalidaCustodiaChart custFwd = new SalidaCustodiaChart();
			            custFwd.IdClasificacionTipoInstrumento = "DV";
			            custFwd.Nemotecnico                    = nemotecnico;
			            custFwd.Monto                          = resultadoNeto;
			            custFwd.Moneda                         = moneda;
			            custFwd.Rentabilidad                   = BigDecimal.ZERO;
			            custFwd.Cantidad                       = montoTotal;
			            custFwd.Precio                         = null;
			            SalidaCustodiaChart.add(custFwd);
			        }
			    }
			}
			// Consultar saldo disponible
			ConsultarSaldoCuentaInversion consultarSaldoCuentaInversion = new ConsultarSaldoCuentaInversion();
			EntradaConsultarSaldoCuentaInversion entradaConsultarSaldoCuentaInversion = new EntradaConsultarSaldoCuentaInversion();
			entradaConsultarSaldoCuentaInversion.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsultarSaldoCuentaInversion.p_IdUsuario = entrada.p_IdUsuario;
			entradaConsultarSaldoCuentaInversion.p_IdModulo = "SOC";
			entradaConsultarSaldoCuentaInversion.p_UsaReglasNegocio = "S";
			entradaConsultarSaldoCuentaInversion.p_FechaDesde = fechaSistema;
			entradaConsultarSaldoCuentaInversion.p_FechaHasta = fechaSistema;
			entradaConsultarSaldoCuentaInversion.p_IncluyeOtrosSaldos = "S";
			entradaConsultarSaldoCuentaInversion.p_UsaReglasNegocio = "S";
			InCuentaInversionConsultar inCuentaInversionConsultar = new InCuentaInversionConsultar();
			inCuentaInversionConsultar.IdPortafolio = entrada.p_IdPortafolio;
			inCuentaInversionConsultar.IdEjecutivo = null;
			inCuentaInversionConsultar.IdMoneda = null;
			
			entradaConsultarSaldoCuentaInversion.p_CuentaInversionAConsultar.add(inCuentaInversionConsultar);
			
			SalidaConsultarSaldoCuentaInversion salidaConsultarSaldoCuentaInversion = consultarSaldoCuentaInversion.logica(con, entradaConsultarSaldoCuentaInversion);
			if (out.P_OUT_CUSTODIACHART == null) {
			    out.P_OUT_CUSTODIACHART = new ArrayList<>();
			}

			if (salidaConsultarSaldoCuentaInversion != null
			        && salidaConsultarSaldoCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA != null) {

			    for (SaldoPorFechaEfectiva saldo : salidaConsultarSaldoCuentaInversion.P_OUT_SALDOPORFECHAEFECTIVA) {

			        if (saldo == null
			                || saldo.SaldoDisponible == null
			                || saldo.SaldoDisponible.compareTo(BigDecimal.ZERO) <= 0) {
			            continue;
			        }

			        SalidaCustodiaChart itemCaja = new SalidaCustodiaChart();

			        itemCaja.IdClasificacionTipoInstrumento = "CAJA";
			        itemCaja.Nemotecnico = saldo.CodigoCuentaInversion;
			        itemCaja.Monto = saldo.SaldoDisponible;
			        itemCaja.Moneda = saldo.CodigoMoneda;

			        itemCaja.Rentabilidad = BigDecimal.ZERO;
			        itemCaja.Cantidad = BigDecimal.ZERO;;
			        itemCaja.Precio = BigDecimal.ZERO;;

			        SalidaCustodiaChart.add(itemCaja);
			    }
			}
			out.P_OUT_CUSTODIACHART = SalidaCustodiaChart;
	        
	        // FLUJO CAJA

			//List<String> usosUI = new ArrayList<String>();
			HashMap<String,Object> paramsUso =new HashMap<String,Object>();
			List<SalidaUsoFlujoCaja> usosUI = sql.query(con, SQL_CONSULTA_USO_FLUJO_CAJA, paramsUso, SalidaUsoFlujoCaja.class);
			Set<String> nombresUsosUI = usosUI.stream()
				    .map(u -> u.Nombre)
				    .collect(Collectors.toSet());
	        List<PortafolioAConsultar> portafoliosConsultar = new ArrayList<>();
	        PortafolioAConsultar portafolio = new PortafolioAConsultar();
	        portafolio.IdMoneda = entrada.p_IdMonedaFlujoCaja;
	        portafolio.IdPortafolio = entrada.p_IdPortafolio;
	        portafoliosConsultar.add(portafolio);
	        ConsultarFlujoCaja llamadoFlujoCaja = new ConsultarFlujoCaja();
			EntradaConsultarFlujoCaja entradaFlujoCaja = new EntradaConsultarFlujoCaja();
			entradaFlujoCaja.p_DesplegarEnUI = "S";
			entradaFlujoCaja.p_UsaReglasNegocio = "S";
			entradaFlujoCaja.p_AgruparPorOrigen = "N";
			entradaFlujoCaja.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaFlujoCaja.p_IdUsuario = entrada.p_IdUsuario;
			entradaFlujoCaja.p_IdModulo = "SOC";
			entradaFlujoCaja.p_FechaDesde = entrada.p_FechaFlujoCaja;
			entradaFlujoCaja.p_FechaHasta = entrada.p_FechaFlujoCaja;
			entradaFlujoCaja.p_PortafolioAConsultar = portafoliosConsultar;
			SalidaConsultarFlujoCaja salidaFlujoCaja = llamadoFlujoCaja.logica(con, entradaFlujoCaja);
			List<ObjFLUJOCAJA> flujoCaja = salidaFlujoCaja.P_OUT_FLUJOCAJA;
			flujoCaja = flujoCaja.stream()
				    .filter(f -> nombresUsosUI.contains(f.NombreUso))
				    .collect(Collectors.toList());
			out.P_OUT_FLUJOCAJA = flujoCaja;
			out.P_OUT_USOS = usosUI;
	        
			// CONSULTA ORDENES
			ConsultarOrdenes llamadoConsultarOrdenes = new ConsultarOrdenes();
			ConsultarOrdenes_Entrada entradaConsultarOrdenes = new ConsultarOrdenes_Entrada();
			entradaConsultarOrdenes.p_CondicionLiquidacion = "0";
			entradaConsultarOrdenes.p_EsVentaCorta = "N";
			entradaConsultarOrdenes.p_FechaOperacionOrdenI = entrada.p_FechaDesde;
			entradaConsultarOrdenes.p_FechaOperacionOrdenF = entrada.p_FechaHasta;
			entradaConsultarOrdenes.p_IdCliente = 0L;
			entradaConsultarOrdenes.p_IdEmpleadoConsulta = 0L;
			entradaConsultarOrdenes.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsultarOrdenes.p_IdModulo = "SOC";
			entradaConsultarOrdenes.p_IdPortafolio = entrada.p_IdPortafolio;
			entradaConsultarOrdenes.p_IdProducto = 0L;
			entradaConsultarOrdenes.p_IdTipoCanalIngreso = "0";
			entradaConsultarOrdenes.p_IdUsuario = entrada.p_IdUsuario;
			entradaConsultarOrdenes.p_IncluyeOrdenesVigentes = "N";
			entradaConsultarOrdenes.p_Instrumento = 0L;
			entradaConsultarOrdenes.p_LibroOrdenes = 0L;
			entradaConsultarOrdenes.p_Mercado = "0";
			entradaConsultarOrdenes.p_Moneda = "0";
			entradaConsultarOrdenes.p_Operacion = "0";
			entradaConsultarOrdenes.p_OrdenesPorAsignar = false;
			entradaConsultarOrdenes.p_TipoInstrumento ="0";
			ConsultarOrdenes_Salida salidaOrdenes = llamadoConsultarOrdenes.logica(con, entradaConsultarOrdenes);
			List<SalidaOrdenes> salidaOrdenesFiltrada = new ArrayList<>();
			if(salidaOrdenes != null && salidaOrdenes.P_OUT_ORDENES != null) {
				for (Orden_Salida orden : salidaOrdenes.P_OUT_ORDENES) {
					SalidaOrdenes ordenOut = new SalidaOrdenes();
					ordenOut.IdOrden = orden.IdOrden;
					ordenOut.FechaOperacionOrden = orden.FechaOperacionOrden;
					ordenOut.IdClasificacionTipoInstrumento = orden.IdClasificacionTipoInstrumento;
					ordenOut.AccionTipoOperacion = orden.AccionTipoOperacion;
					ordenOut.InstrumentoUI = orden.InstrumentoUI;
					ordenOut.IdMonedaOrden = orden.IdMonedaOrden;
					ordenOut.EstadoOrden = orden.EstadoOrden;
					salidaOrdenesFiltrada.add(ordenOut);
				}
			}
			out.P_OUT_ORDENES = salidaOrdenesFiltrada;
		  
		    // REUTILIZA CONSULTA CUSTODIA
			out.P_OUT_GRIDCUSTODIA = (salidaCustodiaPortafolio.P_OUT_TBL != null && !salidaCustodiaPortafolio.P_OUT_TBL.isEmpty()) 
				    ? salidaCustodiaPortafolio.P_OUT_TBL 
				    : new ArrayList<Tabla_Salida>();
				    
		   // CONSULTA CARTERAS VIGENTES PORTAFOLIO
		    ConsultarCarteraIn entradaConsultarCarteras = new ConsultarCarteraIn();
			entradaConsultarCarteras.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConsultarCarteras.p_IdModulo = "SOC";
			entradaConsultarCarteras.p_IdUsuario = entrada.p_IdUsuario;
			entradaConsultarCarteras.p_tipoFecha = "ADQ";
			entradaConsultarCarteras.p_IdInstrumento = 0L;
			entradaConsultarCarteras.p_FolioCartera = 0L;
			entradaConsultarCarteras.p_FechaConsulta = fechaSistema;
			entradaConsultarCarteras.p_IdClasificacionTipoInstrumento = "";
			entradaConsultarCarteras.p_IdCliente = entrada.p_IdCliente;
			entradaConsultarCarteras.p_IdEstado = "VIG";
			entradaConsultarCarteras.p_IdMoneda = "";
			entradaConsultarCarteras.p_IdPortafolio = entrada.p_IdPortafolio;
			entradaConsultarCarteras.p_IdTipoInstrumento = "";
					
			ConsultarCarteras consultarCarteras = new ConsultarCarteras();
			ConsultarCarterasOut salidaConsultarCarteras = consultarCarteras.logica(con, entradaConsultarCarteras);
			out.P_OUT_GRIDCARTERA = (salidaConsultarCarteras.P_OUT_TABLA != null && !salidaConsultarCarteras.P_OUT_TABLA.isEmpty()) 
				    ? salidaConsultarCarteras.P_OUT_TABLA 
				    : new ArrayList<cl.bcs.custodia.ConsultarCarteras.Tabla_Salida>();
		
		}catch(Exception e){
			log.write("ERROR",e);
			throw e;
		}finally{}
		log.write("FIN.ResumenPortafolioCliente");
		return out;
	}
	private static BigDecimal toBigDecimal(Object value) {
	    if (value == null) return null;

	    if (value instanceof BigDecimal) return (BigDecimal) value;
	    if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());

	    try {
	        return new BigDecimal(value.toString().trim());
	    } catch (Exception e) {
	        return null;
	    }
	}
	private static BigDecimal obtenerPorcentajeDelTotalPorGrupo(BigDecimal montoGrupo, BigDecimal sumaTotal) {
	    if (montoGrupo == null || sumaTotal == null || sumaTotal.compareTo(BigDecimal.ZERO) == 0) {
	        return BigDecimal.ZERO;
	    }

	    return montoGrupo
	            .multiply(BigDecimal.valueOf(100))
	            .divide(sumaTotal, 8, RoundingMode.HALF_UP);
	}
	private List<SalidaComposicionActivos> ComposicionActivos(Connection con, EntradaResumenInversionWrapper Objin) throws Exception {
		long inicio = System.currentTimeMillis();
		ResumenInversionWrapper ResumenComposicionActivos = new ResumenInversionWrapper();
		SalidaCartola objOut=ResumenComposicionActivos.logica(con, Objin);
		List<SalidaComposicionActivos> grupos = new ArrayList<>();
		if (objOut == null || objOut.P_OUT_SALIDA == null) {
	        return grupos;
	    }

	    Object bloqueObj = objOut.P_OUT_SALIDA.get("resumenInversion");

	    if (!(bloqueObj instanceof List)) {
	        return grupos;
	    }

	    List<Map<String, Object>> registros = (List<Map<String, Object>>) bloqueObj;
	    Set<String> gruposVistos = new HashSet<>();

	    // 1) filtrar en sitio
	    registros.removeIf(registro -> {
	        String nombreGrupo = registro.get("DescripcionGrupo") != null
	                ? registro.get("DescripcionGrupo").toString().trim()
	                : null;

	        BigDecimal total = toBigDecimal(registro.get("TotalGrupoMonedaSistema"));

	        if (nombreGrupo == null || nombreGrupo.isEmpty()) {
	            return true;
	        }

	        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
	            return true;
	        }

	        return !gruposVistos.add(nombreGrupo); // true si esta repetido
	    });

	    // 2) suma total de los grupos que quedaron
	    BigDecimal sumaTotal = BigDecimal.ZERO;
	    for (Map<String, Object> registro : registros) {
	        BigDecimal total = toBigDecimal(registro.get("TotalGrupoMonedaSistema"));
	        if (total != null) {
	            sumaTotal = sumaTotal.add(total);
	        }
	    }

	    // 3) construir salida con porcentaje
	    for (Map<String, Object> registro : registros) {
	        String nombreGrupo = registro.get("DescripcionGrupo") != null
	                ? registro.get("DescripcionGrupo").toString().trim()
	                : "";
	        String clasificacionGrupo = registro.get("Grupo") != null
	                ? registro.get("Grupo").toString().trim()
	                : "";

	        BigDecimal montoGrupo = toBigDecimal(registro.get("TotalGrupoMonedaSistema"));

	        SalidaComposicionActivos grupo = new SalidaComposicionActivos();
	        grupo.nombreGrupo = nombreGrupo;
	        grupo.clasificacionGrupo = clasificacionGrupo;
	        grupo.porcentaje = obtenerPorcentajeDelTotalPorGrupo(montoGrupo, sumaTotal);

	        grupos.add(grupo);
	    }
		
		
		return grupos;
	}

	public static void main(String[] args) throws Exception{
		System.out.println("== DESA ==");
		Connection con = PlataformaUtils.getDESAConnection();
		try {
			EntradaResumenPortafolioCliente entrada = new EntradaResumenPortafolioCliente();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdCliente = 4846L;
//			entrada.p_IdModulo = "SOC";
			entrada.p_IdUsuario = 9573L;
			entrada.p_IdPortafolio = 2225L;
			entrada.p_FechaFlujoCaja = "2026-05-18";
			entrada.p_IdMonedaFlujoCaja = "CLP";
			entrada.p_FechaDesde = "2026-05-18T04:00:00.000Z";
			entrada.p_FechaHasta = "2026-05-18T03:59:59.000Z";
			

			SalidaResumenPortafolioCliente Salida = new SalidaResumenPortafolioCliente();
			ResumenPortafolioCliente sp = new ResumenPortafolioCliente();
			Salida = sp.logica(con,entrada);
			System.out.println(Salida);
			con.commit();
		}catch(Exception e){
			con.rollback();
			e.printStackTrace();
		}finally{
			con.close();
		}
		System.out.println("== END ==");
	}
}
