package cl.bcs.normativa;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CargarDatosReporteAPV implements JavaCall {
	static final Logger logger = LogManager.getLogger(CargarDatosReporteAPV.class);
	
	private final String SQL_SELECT_DATOS_CUSTODIA = loadResourceToString("cl/bcs/normativa/sql/CargarDatosReporteAPV_SelectDatosOrigenReporteAPV.sql");
	private final String SQL_SELECT_DATOS_CUSTODIA_HIS = loadResourceToString("cl/bcs/normativa/sql/CargarDatosReporteAPV_SelectDatosOrigenReporteAPVHis.sql");
	private final String SQL_SELECT_SALDO_CIN_PORTAFOLIO = loadResourceToString("cl/bcs/normativa/sql/CargarDatosReporteAPV_SelectSaldoCuentaInversionPortafolio.sql");
	private final String SQL_INSERT_DATOS_REPORTE_APV = loadResourceToString("cl/bcs/normativa/sql/CargarDatosReporteAPV_InsertDatosReporteAPV.sql");
	private final String SQL_DELETE_DATOS_REPORTE_APV = loadResourceToString("cl/bcs/normativa/sql/CargarDatosReporteAPV_DeleteDatosReporteAPV.sql");
	
	public static class EntradaCargarDatosReporteAPV extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaCierre;
	}
	
	public static class SalidaCargarDatosReporteAPV extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;;
		public List<MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaCargarDatosReporteAPV() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class ReporteBase extends BcsPojo{
		public Long IdInterno;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String DocumentoIdentidad;
		public String NombreCliente;
		public String GrupoRegimenTributario;
		public Long IdRegimenTributario;
		public String CodigoRegimenTributario;
		public Integer IdPlanTipoInversion;
		public String NombrePlanTipoInversion;
		public String IdMonedaInstrReajuste;
		public BigDecimal MontoMercado; 
		public String IdClasificacionTipoInstrumento;
		public String IdMonedaEmision;
		public String IdTipoInstrumento;
		public String Nemotecnico;
		public BigDecimal CantidadCustodia;
		public BigDecimal SaldoTotal;
		
		public ReporteBase() {
			this.IdInstrumento = 0L;
			this.IdMonedaInstrReajuste = "";
			this.MontoMercado = BigDecimal.ZERO;
			this.IdClasificacionTipoInstrumento = "";
			this.IdMonedaEmision = "";
			this.IdTipoInstrumento = "";
			this.Nemotecnico = "";
			this.CantidadCustodia = BigDecimal.ZERO;
			this.SaldoTotal = BigDecimal.ZERO;
		}
		
	}
	
	public static class DatoReporteAPV extends BcsPojo{
		public Long IdReporteAPV;
		public Long IdCliente;
		public String DocumentoIdentidad;
		public String NombreCliente;
		public String GrupoRegimenTributario;
		public String NombreRegimenTributario;
		public String TipoProducto;
		public Integer IdPlanTipoInversion;
		public String NombrePlanTipoInversion;
		public Date FechaCierre;
		public BigDecimal SaldoAcumulado;
		public BigDecimal SaldoRegimenTributarioA;
		public BigDecimal SaldoRegimenTributarioB;
		public BigDecimal SaldoBonificacionEstatal;
		public BigDecimal SaldoAPVCEmpleador;
		public BigDecimal SaldoAPVCTrabajadorA;
		public BigDecimal SaldoAPVCTrabajadorB;
		public BigDecimal SaldoAPVCBonificacionEstatal;
		public Long IdUsuarioCarga;		
		public Timestamp FechaCarga;
		public Long IdUsuarioModificacion;		
		public Timestamp FechaModificacion;
		
		public DatoReporteAPV() {
			this.SaldoAcumulado = BigDecimal.ZERO;
			this.SaldoRegimenTributarioA = BigDecimal.ZERO;
			this.SaldoRegimenTributarioB = BigDecimal.ZERO;
			this.SaldoBonificacionEstatal = BigDecimal.ZERO;
			this.SaldoAPVCEmpleador = BigDecimal.ZERO;
			this.SaldoAPVCTrabajadorA = BigDecimal.ZERO;
			this.SaldoAPVCTrabajadorB = BigDecimal.ZERO;
			this.SaldoAPVCBonificacionEstatal = BigDecimal.ZERO;
		}
	}
	
	public static class PortafolioProceso_Insertar extends BcsPojo{
		public Long IdTrabajo;
		public Long IdPortafolio;
	}
	
	public static class PortafolioProceso_Eliminar {
		public Long IdTrabajo;
		
		public PortafolioProceso_Eliminar(){
			IdTrabajo = 0L;
		}
	}
	
	public static class SaldoCuentaInversionPortafolio extends BcsPojo{
	    public Long IdPortafolio;
	    public String DocumentoIdentidad;
	    public String NombreCliente;
	    public String GrupoRegimenTributario;
	    public Long IdRegimenTributario;
	    public String CodigoRegimenTributario;
	    public Integer IdPlanTipoInversion;
	    public String NombrePlanTipoInversion;
	    public BigDecimal SaldoTotal;
	    
	    public SaldoCuentaInversionPortafolio() {
	    	this.SaldoTotal = BigDecimal.ZERO;
	    }
	}
	
	public Long IdPortafolio;
	public String DocumentoIdentidad;
	public String NombreCliente;
	public String GrupoRegimenTributario;
	public Long IdRegimenTributario;
	public String CodigoRegimenTributario;
	public Integer IdPlanTipoInversion;
	public String NombrePlanTipoInversion;
	public String IdMonedaInstrReajuste;
	public BigDecimal MontoMercado; 
	public String IdClasificacionTipoInstrumento;
	public String IdMonedaEmision;
	public String IdTipoInstrumento;
	public String Nemotecnico;
	public BigDecimal CantidadCustodia;
	
	static class KeyClientePlan {
	    private final Long IdCliente;
	    private final String CodigoRegimen;
	    private final Integer IdPlan;

	    KeyClientePlan(Long idCliente, String codigoRegimen, Integer idPlan) {
	        this.IdCliente = idCliente;
	        this.CodigoRegimen = codigoRegimen;
	        this.IdPlan = idPlan;
	    }

	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;

	        KeyClientePlan that = (KeyClientePlan) o;

	        return java.util.Objects.equals(IdCliente, that.IdCliente) &&
	               java.util.Objects.equals(CodigoRegimen, that.CodigoRegimen) &&
	               java.util.Objects.equals(IdPlan, that.IdPlan);
	    }

	    @Override
	    public int hashCode() {
	        return java.util.Objects.hash(IdCliente, CodigoRegimen, IdPlan);
	    }
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCargarDatosReporteAPV jin = gson.fromJson(in.toString(), EntradaCargarDatosReporteAPV.class);
		SalidaCargarDatosReporteAPV salida = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject datasalida = parser.parse(gson.toJson(salida)).getAsJsonObject();
		return datasalida;
	}
	
	public SalidaCargarDatosReporteAPV logica(Connection con, EntradaCargarDatosReporteAPV entrada) throws Exception{
		logger.info("INICIO::CargarDatosReporteAPV.logica");
		
		SQL sql=new SQL();
		
		SalidaCargarDatosReporteAPV salida = new SalidaCargarDatosReporteAPV();
		String idMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		Long time = System.currentTimeMillis();
		Timestamp fechaHora = new Timestamp(time);
		
		eliminarDatosReporteAPVFechaCierre(con, entrada, sql);
		
		List<ReporteBase> datosCustodia = obtenerDatosCustodiaAPV(con, entrada, fechaSistema);
		
		HashMap<Long, Object> hashSaldosCuentaInversionPortafolio = obtenerSaldosCuentaInversionPortafolio(con, entrada);

		List<ReporteBase> datosCustodiaProcesados = convertirMontosCustodia(con, datosCustodia, idMonedaSistema, entrada);	
		
		List<DatoReporteAPV> datosReporteAPVInsertar = agruparDatosReporteAPV(entrada, fechaSistema, fechaHora, datosCustodiaProcesados, hashSaldosCuentaInversionPortafolio);
		
		if(datosReporteAPVInsertar.isEmpty()) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "UI_SIN_DATOS_REPORTE_APV", null, con, entrada.p_IdEmpresa));				
			agregarLog(con, entrada, fechaSistema);
		} else {
			insertarDatosReporteAPV(con, entrada, salida, fechaSistema, datosReporteAPVInsertar);
		}

		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		logger.info("FIN::CargarDatosReporteAPV.logica");
		return salida;
	}

	private HashMap<Long, Object> obtenerSaldosCuentaInversionPortafolio(Connection con, EntradaCargarDatosReporteAPV entrada) throws SQLException, Exception {
		logger.info("INICIO::CargarDatosReporteAPV.obtenerSaldosCuentaInversionPortafolio");
		
		SQL sql=new SQL();
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdEmpresa",entrada.p_IdEmpresa);
		params.put("FechaCierre", entrada.p_FechaCierre);
		
		List<ReporteBase> saldosCuentaInversionPortafolio = sql.query(con, SQL_SELECT_SALDO_CIN_PORTAFOLIO, params, ReporteBase.class);
		HashMap<Long, Object> hashSaldosCuentaInversionPortafolio = Utils.List2Hash(saldosCuentaInversionPortafolio,"IdPortafolio");
		
		logger.info("FIN::CargarDatosReporteAPV.obtenerSaldosCuentaInversionPortafolio");
		return hashSaldosCuentaInversionPortafolio;
	}

	private void insertarDatosReporteAPV(Connection con, EntradaCargarDatosReporteAPV entrada, SalidaCargarDatosReporteAPV salida, String fechaSistema, List<DatoReporteAPV> datosReporteAPVInsertar) throws SQLException, Exception {
		logger.info("INICIO::CargarDatosReporteAPV.insertarDatosReporteAPV");
		
		SQL sql = new SQL();
		
		List<Long> secuenciasReporteAPV = Utils.getSecuencia(con, "BSCL.Normativa.BD.Secuencias::NOR.ReporteAPV", Long.valueOf(datosReporteAPVInsertar.size()));
		
		for (int i = 0; i < datosReporteAPVInsertar.size(); i++) {
			datosReporteAPVInsertar.get(i).IdReporteAPV = secuenciasReporteAPV.get(i);
		}
		
		sql.batch(con, SQL_INSERT_DATOS_REPORTE_APV, datosReporteAPVInsertar, DatoReporteAPV.class);	
		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"UI_DATOS_REPORTE_APV_CARGADOS", null, con,entrada.p_IdEmpresa));

		agregarLog(con, entrada, fechaSistema);
		
		logger.info("FIN::CargarDatosReporteAPV.insertarDatosReporteAPV");
	}

	private List<DatoReporteAPV> agruparDatosReporteAPV(EntradaCargarDatosReporteAPV entrada, String fechaSistema, Timestamp fechaHora, List<ReporteBase> datosCustodiaProcesados, HashMap<Long, Object> hashSaldosCuentaInversionPortafolio) {
		logger.info("INICIO::CargarDatosReporteAPV.agruparDatosReporteAPV");
		
		//los saldos que calcen con custodia
		List<DatoReporteAPV> datosReporteAPVInsertar = datosCustodiaProcesados.stream()
				.collect(Collectors.groupingBy(p -> new KeyClientePlan(p.IdCliente, p.GrupoRegimenTributario, p.IdPlanTipoInversion)))
				.values().stream().map(grupo -> {
					ReporteBase custodiaBase = grupo.iterator().next();
					DatoReporteAPV datoReporteAPV = crearDatoReporteBase(custodiaBase, entrada, fechaSistema, fechaHora);
					grupo.forEach(item -> acumularSaldosCustodiaCuentaInversion(datoReporteAPV, item, hashSaldosCuentaInversionPortafolio));
					return datoReporteAPV;
				}).collect(Collectors.toList());
		
		
		//los saldos que quedan sin calce con custodia
		if(!hashSaldosCuentaInversionPortafolio.isEmpty()) {
			hashSaldosCuentaInversionPortafolio.values().stream().map(obj -> (ReporteBase) obj)
					.collect(Collectors.groupingBy(s -> new KeyClientePlan(s.IdCliente, s.GrupoRegimenTributario, s.IdPlanTipoInversion)))
					.values().stream().map(grupo -> {
						ReporteBase saldo = grupo.iterator().next();
						
						Optional<DatoReporteAPV> datoReporteAPVExistente = datosReporteAPVInsertar.stream()
								.filter(d -> d.IdCliente.equals(saldo.IdCliente)
										&& d.GrupoRegimenTributario.equals(saldo.GrupoRegimenTributario)
										&& d.IdPlanTipoInversion.equals(saldo.IdPlanTipoInversion)).findFirst();

						DatoReporteAPV dato = datoReporteAPVExistente.orElseGet(() -> {
							DatoReporteAPV nuevoDatoReporteAPV = crearDatoReporteBase(saldo, entrada, fechaSistema, fechaHora);
							datosReporteAPVInsertar.add(nuevoDatoReporteAPV);
							return nuevoDatoReporteAPV;
						});

						grupo.forEach(item -> acumularSaldosCuentaInversion(dato, item));

						return dato;
					}).collect(Collectors.toList());
		}
		
		logger.info("FIN::CargarDatosReporteAPV.agruparDatosReporteAPV");
		return datosReporteAPVInsertar;
	}

	private List<ReporteBase> obtenerDatosCustodiaAPV(Connection con, EntradaCargarDatosReporteAPV entrada, String fechaSistema) throws SQLException {
		logger.info("INICIO::CargarDatosReporteAPV.obtenerDatosCustodia");
		
		SQL sql = new SQL();
		List<ReporteBase> datosCustodia = new ArrayList<ReporteBase>();
		
		HashMap<String,Object> params = new HashMap<String,Object>(2);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("FechaCierre", entrada.p_FechaCierre);
		
		if(Fecha.toDate(entrada.p_FechaCierre).equals(Fecha.toDate(fechaSistema)) || Fecha.toDate(entrada.p_FechaCierre).after(Fecha.toDate(fechaSistema))) {
			datosCustodia = sql.query(con, SQL_SELECT_DATOS_CUSTODIA, params, ReporteBase.class);
		}else {
			datosCustodia = sql.query(con, SQL_SELECT_DATOS_CUSTODIA_HIS, params, ReporteBase.class);
		}
		
		logger.info("FIN::CargarDatosReporteAPV.obtenerDatosCustodia");
		return datosCustodia;
	}

	private void eliminarDatosReporteAPVFechaCierre(Connection con, EntradaCargarDatosReporteAPV entrada, SQL sql) throws SQLException {
		logger.info("INICIO::CargarDatosReporteAPV.eliminarDatosReporteAPVFechaCierre");
		
		List<DatoReporteAPV> datosReporteApvEliminar = new ArrayList<DatoReporteAPV>();
		DatoReporteAPV datoReporteApv = new DatoReporteAPV();
		datoReporteApv.FechaCierre = Fecha.toDate(entrada.p_FechaCierre);
		datosReporteApvEliminar.add(datoReporteApv);
		sql.batch(con, SQL_DELETE_DATOS_REPORTE_APV, datosReporteApvEliminar, DatoReporteAPV.class);
		
		logger.info("FIN::CargarDatosReporteAPV.eliminarDatosReporteAPVFechaCierre");
	}


	private DatoReporteAPV crearDatoReporteBase(ReporteBase reporteBase, EntradaCargarDatosReporteAPV entrada, String fechaSistema, Timestamp fechaHora) {
	    DatoReporteAPV datoReporteAPV = new DatoReporteAPV();

	    String nombreRegimenTributario = "";
	    switch (reporteBase.GrupoRegimenTributario) {
        case "APV": 
        	nombreRegimenTributario = "APV depósitos voluntarios";
            break;
        case "DC":
        	nombreRegimenTributario = "APV depósitos convenidos";
            break;
	    }
	    
	    datoReporteAPV.IdCliente = reporteBase.IdCliente;
	    datoReporteAPV.DocumentoIdentidad = reporteBase.DocumentoIdentidad;
	    datoReporteAPV.NombreCliente = reporteBase.NombreCliente;
	    datoReporteAPV.GrupoRegimenTributario = reporteBase.GrupoRegimenTributario;
	    datoReporteAPV.NombreRegimenTributario = nombreRegimenTributario;
	    datoReporteAPV.TipoProducto = "Contrato de administración de cartera APV";
	    datoReporteAPV.IdPlanTipoInversion = reporteBase.IdPlanTipoInversion;
	    datoReporteAPV.NombrePlanTipoInversion = reporteBase.NombrePlanTipoInversion;
	    datoReporteAPV.FechaCierre = Fecha.toDate(entrada.p_FechaCierre);
	    datoReporteAPV.IdUsuarioCarga = entrada.p_IdUsuario;
	    datoReporteAPV.FechaCarga = fechaHora;

	    return datoReporteAPV;
	}
	
	private void acumularSaldosCustodiaCuentaInversion(DatoReporteAPV datoReporteAPV, ReporteBase reporteBase, HashMap<Long, Object> hashSaldosCuentaInversionPortafolio) {
		BigDecimal monto = reporteBase.MontoMercado != null ? reporteBase.MontoMercado : BigDecimal.ZERO;

		// Suma saldos custodia
		sumarSaldoPorRegimen(datoReporteAPV, reporteBase.CodigoRegimenTributario, monto);

		// Suma saldo de cuenta inversion a custodia solo una vez por portafolio
		ReporteBase saldoCuentaInversionPortafolio = (ReporteBase) hashSaldosCuentaInversionPortafolio.get(reporteBase.IdPortafolio);
		
		if (saldoCuentaInversionPortafolio!= null) {
			BigDecimal saldo = saldoCuentaInversionPortafolio.SaldoTotal != null ? saldoCuentaInversionPortafolio.SaldoTotal: BigDecimal.ZERO;
			
			if(saldo.compareTo(BigDecimal.ZERO) > 0) {
				sumarSaldoPorRegimen(datoReporteAPV, reporteBase.CodigoRegimenTributario, saldo);
			}
			
			hashSaldosCuentaInversionPortafolio.remove(reporteBase.IdPortafolio);
		}
	}
	
	private void acumularSaldosCuentaInversion(DatoReporteAPV datoReporteAPV, ReporteBase reporteBase) {
		BigDecimal monto = reporteBase.SaldoTotal != null ? reporteBase.SaldoTotal : BigDecimal.ZERO;
		sumarSaldoPorRegimen(datoReporteAPV, reporteBase.CodigoRegimenTributario, monto);
	}
	
	private void sumarSaldoPorRegimen(DatoReporteAPV datoReporteAPV, String regimen, BigDecimal monto) {
		datoReporteAPV.SaldoAcumulado = datoReporteAPV.SaldoAcumulado.add(monto);

	    switch (regimen) {
	        case "APV-A":
	        	datoReporteAPV.SaldoRegimenTributarioA = datoReporteAPV.SaldoRegimenTributarioA.add(monto);
	            break;
	        case "APV-B":
	        case "APV-AN":
	        case "DC-R":
	        case "DC-AL":
	        case "DC-DL":
	        	datoReporteAPV.SaldoRegimenTributarioB = datoReporteAPV.SaldoRegimenTributarioB.add(monto);
	            break;
	        case "BF":
	        	datoReporteAPV.SaldoBonificacionEstatal = datoReporteAPV.SaldoBonificacionEstatal.add(monto);
	            break;
	    }
	}
	
	private List<ReporteBase> convertirMontosCustodia(Connection con, List<ReporteBase> datosCustodia, String idMonedaSistema, EntradaCargarDatosReporteAPV entrada) throws Exception{
		logger.info("INICIO::CargarDatosReporteAPV.convertirMontosCustodia");
		
		HashMap<String, Object> hashSalidaPrecioTasa = obtenerCalculoPrecioTasa(con, datosCustodia, entrada);

		for (ReporteBase detalle : datosCustodia) {
			if(detalle.IdClasificacionTipoInstrumento.equals("RV") || detalle.IdClasificacionTipoInstrumento.equals("FM")) {
				EntregarPreciosTasas_Salida_PreciosTasas precioTasa = (EntregarPreciosTasas_Salida_PreciosTasas) hashSalidaPrecioTasa.get(detalle.Nemotecnico+detalle.IdTipoInstrumento);
				if(precioTasa != null && precioTasa.Precio != null) {
					detalle.MontoMercado = detalle.CantidadCustodia.multiply(precioTasa.Precio, MathContext.DECIMAL128);
				}
			}	
		}
		
		HashMap<Long, Object> hashSalidaParidadMonto = obtenerCalculoParidad(con, datosCustodia, idMonedaSistema, entrada);
		
		for (ReporteBase detalle : datosCustodia) {
			CalculoSalida calculoSalidaMonto = (CalculoSalida) hashSalidaParidadMonto.get(detalle.IdInterno);
			BigDecimal montoDestino = calculoSalidaMonto != null ? calculoSalidaMonto.MontoDestino : detalle.MontoMercado;
			detalle.MontoMercado = MonedaFormato.formatear(con, montoDestino, MonedaFormato.MONTO, idMonedaSistema).valor;
		}
		
		logger.info("FIN::CargarDatosReporteAPV.convertirMontosCustodia");
		return datosCustodia;
	}

	private HashMap<String, Object> obtenerCalculoPrecioTasa(Connection con, List<ReporteBase> datosCustodia,EntradaCargarDatosReporteAPV entrada) throws Exception, SQLException {
		logger.info("INICIO::CargarDatosReporteAPV.obtenerCalculoPrecioTasa");
		
		EntregarPreciosTasas precioTasa = new EntregarPreciosTasas();
		EntregarPreciosTasas.EntregarPreciosTasas_Entrada  paramsPrecioTasa = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada();
		paramsPrecioTasa.p_IdEmpresa = entrada.p_IdEmpresa;
		paramsPrecioTasa.p_IdModulo = entrada.p_IdModulo;
		paramsPrecioTasa.p_IdPais = ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		paramsPrecioTasa.p_IdUsuario = entrada.p_IdUsuario;

		List<ReporteBase> instrumentosRVFM = datosCustodia.parallelStream()
				.filter(dato -> dato.IdClasificacionTipoInstrumento.equals("RV") || dato.IdClasificacionTipoInstrumento.equals("RF") || dato.IdClasificacionTipoInstrumento.equals("FM")).distinct().collect(Collectors.toList());
		
		List<Long> secuencias = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::TAS.IdRegistroEntregarPreciosTasas",new Long(instrumentosRVFM.size()));
		AtomicInteger i = new AtomicInteger(0);
		paramsPrecioTasa.p_DatosInstrumentos = datosCustodia.parallelStream()
				.filter(dato -> dato.IdClasificacionTipoInstrumento.equals("RV") || dato.IdClasificacionTipoInstrumento.equals("RF") ||dato.IdClasificacionTipoInstrumento.equals("FM"))
				.map(dato->{
					EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos paramsRegIn = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos();
					paramsRegIn.FechaConsulta=Fecha.toDate(entrada.p_FechaCierre);
					paramsRegIn.IdClasificacionTipoInstrumento = dato.IdClasificacionTipoInstrumento;
					paramsRegIn.IdInstrumento = dato.IdInstrumento;
					paramsRegIn.IdMoneda = dato.IdMonedaEmision;
					paramsRegIn.IdRegistro = secuencias.get(i.getAndIncrement());
					paramsRegIn.IdTipoCriterioTasa = "0";
					paramsRegIn.IdTipoInstrumento = dato.IdTipoInstrumento;
					paramsRegIn.IdUso = "CARTERA";
					paramsRegIn.Nemotecnico = dato.Nemotecnico;
					paramsRegIn.PlazoResidual = 0L;
					paramsRegIn.TipoCartera = "0";
					return paramsRegIn;
				})
				.collect(Collectors.toList());
		
		HashMap<String, Object> hashSalidaPrecioTasa = new HashMap<String,Object>();
		EntregarPreciosTasas.EntregarPreciosTasas_Salida salidaPrecioTasa = precioTasa.logica(con, paramsPrecioTasa);
		
		for(EntregarPreciosTasas_Salida_PreciosTasas dato : salidaPrecioTasa.P_OUT_PRECIOSTASAS){
			hashSalidaPrecioTasa.putIfAbsent(dato.Nemotecnico+dato.IdTipoInstrumento, dato);
		}
		
		logger.info("FIN::CargarDatosReporteAPV.obtenerCalculoPrecioTasa");
		return hashSalidaPrecioTasa;
	}
	
	private HashMap<Long, Object> obtenerCalculoParidad (Connection con, List<ReporteBase> datosCustodia, String idMonedaSistema, EntradaCargarDatosReporteAPV entrada) throws Exception{
		logger.info("INICIO::CargarDatosReporteAPV.obtenerCalculoParidad");
		
		IntStream.range(0, datosCustodia.size()).forEach(ind -> datosCustodia.get(ind).IdInterno = ind + 1L);
		
		List<CalculoEntrada> entradaParidadMonto = datosCustodia.stream()
				.filter(detalle -> !idMonedaSistema.equalsIgnoreCase(detalle.IdMonedaInstrReajuste))
				.map(detalle -> {
					CalculoEntrada calculoEntradaMonto = new CalculoEntrada();
					calculoEntradaMonto.IdTrabajo			= 1L;
					calculoEntradaMonto.Fecha				= entrada.p_FechaCierre;
					calculoEntradaMonto.IdInterno			= detalle.IdInterno;
					calculoEntradaMonto.IdMonedaOrigen	= detalle.IdMonedaInstrReajuste;
					calculoEntradaMonto.IdMonedaDestino	= idMonedaSistema;
					calculoEntradaMonto.MontoOrigen		= detalle.MontoMercado;
					return calculoEntradaMonto;
				})
				.distinct()
				.collect(Collectors.toList());
		
		List<CalculoSalida> paridadesConvertidas = new ArrayList<>();
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada entradaCalcularParidad = new CalcularParidadEntrada();
		entradaCalcularParidad.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaCalcularParidad.p_IdTrabajo = 0L;
		entradaCalcularParidad.p_CalculosEntrada = entradaParidadMonto;
		CalcularParidadSalida salidaCalculoParidad = calcularParidad.logica(con, entradaCalcularParidad);
		
		if( salidaCalculoParidad.P_OUT_ERRORESCONTADOR.equals(0L) && !salidaCalculoParidad.p_out_CalculosSalida.isEmpty()) {
			paridadesConvertidas = salidaCalculoParidad.p_out_CalculosSalida;
		}
		
		HashMap<Long, Object> hashParidadMonto = Utils.List2Hash(paridadesConvertidas,"IdInterno");
		
		logger.info("FIN::CargarDatosReporteAPV.obtenerCalculoParidad");
		return hashParidadMonto;
	}
	
	public void agregarLog (Connection conn, EntradaCargarDatosReporteAPV entrada, String fechaSistema) {		
		logger.info("INICIO::CargarDatosReporteAPV.agregarLog");
		try {
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();			
			
			entradaAgregarLog.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaAgregarLog.p_IdModulo = "PLA";
			entradaAgregarLog.p_IdUsuario = entrada.p_IdUsuario;
			entradaAgregarLog.p_UsaReglasNegocio = "N";
										
			log.IdCabeceraLog = 1L;
			log.IdTipoLog = "INGRESO";
			log.IdModulo = "NOR";
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdCliente = 0L;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "CARGAR_REPORTE_APV";
			log.EstadoRegistro = "";
			log.IdTipoOperacion = "";
			log.Folio = 0L;
			log.Cantidad = BigDecimal.ZERO;
			log.PrecioTasa = BigDecimal.ZERO;
			log.Monto = BigDecimal.ZERO;
			log.IdMonedaOrigen = "";
			log.IdMonedaLiquidacion = "";
			log.TipoCambio = "";
			log.Nemo = "";
			log.IdProceso = 0L;
			log.EstadoProceso = "PRO";
			log.IdPersona = 0L;
			log.NombreProceso = "Carga Datos Reporte APV a Fecha Cierre: " + entrada.p_FechaCierre;
		
			entradaAgregarLog.p_Log.add(log);
			
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("FIN::CargarDatosReporteAPV.agregarLog");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");		                  
		try (Connection con = PlataformaUtils.getPropertiesConnection("master", "connections.properties")) {
			Gson gson = Utils.getGsonBuilder();

			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":82260,\"p_IdModulo\":\"NOR\",\"p_FechaCierre\":\"2026-05-13\"}";
			EntradaCargarDatosReporteAPV Objin = gson.fromJson(json, EntradaCargarDatosReporteAPV.class);
			CargarDatosReporteAPV demo = new CargarDatosReporteAPV();
			try {
				SalidaCargarDatosReporteAPV Objsalida = demo.logica(con, Objin);
				con.rollback();
				System.out.println(Objsalida.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		}
		System.out.println("== END ==");
	}
}
