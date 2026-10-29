package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.EntradaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.SalidaConsultarSaldoCuentaInversion;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.InCuentaInversionConsultar;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoInicialFinal;
import cl.bcs.cuentaInversion.SaldoCuentaInversion.SaldoPorFechaEfectiva;
import cl.bcs.normativa.cartola.CalcularRentabilidad.CalcularRentabilidad_Entrada;
import cl.bcs.normativa.cartola.CalcularRentabilidad.CalcularRentabilidad_Salida;
import cl.bcs.normativa.cartola.CalcularRentabilidad.Rentabilidad;
import cl.bcs.normativa.cartola.ConsultarCartolaOnline.DatosCliente;
import cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad_ProcesaMovimientosCaja.ProcesaMovimientosCaja_Entrada;
import cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad_ProcesaMovimientosCaja.ProcesaMovimientosCaja_Salida;
import cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad_ProcesaMovimientosTitulos.ProcesaMovimientosTitulo_Entrada;
import cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad_ProcesaMovimientosTitulos.ProcesaMovimientosTitulo_Salida;
import cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad_SaldoFinal.FlujoPatrimonialYRentabilidad_SaldoFinal_Entrada;
import cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad_SaldoFinal.FlujoPatrimonialYRentabilidad_SaldoFinal_Salida;
import cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad_SaldoFinal.Operaciones;
import cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad_SaldoFinal.TablaSalida;
import cl.bcs.normativa.cartola.pojo.SaldoCuentaCliente;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class FlujoPatrimonialYRentabilidad implements JavaCall {

	// -------------------------------------------------------------------------------------------
	// ESTA CLASE ES LLAMADA DESDE EL SERVICIO ConsultarCartolaOnline.java Y DESDE UN BATCH
	// PARA INSERTAR DATOS EN LA TABLA BSCL.Normativa.BD::CAR.CartolaHistorica
	// SU SALIDA DEPENDE DEL PARAMETRO DE ENTRADA esPauta (Por defecto = false)
	//
	// esPauta = true	-> 	ENVIA LA SALIDA PARA EL BATCH
	// esPauta = false	-> 	ENVIA LA SALIDA PARA EL SERVICIO ConsultarCartolaOnline.java 
	// -------------------------------------------------------------------------------------------
	
	private final String SQL_CREATE_TEMP_TABLE_CLIENTES_PORTAFOLIOS = "CREATE LOCAL TEMPORARY COLUMN TABLE #tmpTblClientesPortafolios(\"IdCliente\" BIGINT, \"IdPortafolio\" BIGINT);";
	private final String SQL_INSERT_TEMP_TABLE_CLIENTES_PORTAFOLIOS = "INSERT INTO #tmpTblClientesPortafolios VALUES(:IdCliente, :IdPortafolio);";
	private final String SQL_DROP_TEMP_TABLE_CLIENTES_PORTAFOLIOS = "DROP TABLE #tmpTblClientesPortafolios;";

	private final String SQL_SELECT_MOV_CTA = loadResourceToString("cl/bcs/normativa/cartola/sql/FlujoPatromonialYRentabilidad_PrimerMovimiento.sql");

	private final String SQL_SELECT_MONEDAS_CTAINVERSION = loadResourceToString("cl/bcs/normativa/cartola/sql/FlujoPatromonialYRentabilidad_SelectMonedas.sql");
	// -------------------------------------------------------------------------------------------
	// CLASE DE ENTRADA
	// -------------------------------------------------------------------------------------------
	public static class FlujoPatrimonialYRentabilidad_Entrada {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_FechaDesde;
		public String p_FechaHasta;
		public List<DatosCliente> p_Datos;
		public Boolean esPauta = false;
		public List<HashMap<String, Object>> p_MovimientosTitulos;
		public List<HashMap<String, Object>> p_MovimientosCaja;
		public List<Operaciones> movimientosCajaActivo;
		public List<Operaciones> movimientosCajaPasivo;
		public List<Operaciones> movimientosTitulosActivo;
		public List<Operaciones> movimientosTitulosPasivo;
		public BigDecimal totalPatrimonio;

		public FlujoPatrimonialYRentabilidad_Entrada() {
			this.p_Datos = new ArrayList<>();
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------------------------------------------------
	// CLASE DE SALIDA DE PAUTA, SI EL PARAMETRO DE ENTRADA esPauta = true
	// -------------------------------------------------------------------------------------------
	public static class SalidaPauta {
		public Long IdCliente;
		public BigDecimal SaldoInicialMTD = BigDecimal.ZERO;
		public BigDecimal SaldoInicialYTD = BigDecimal.ZERO;
		public BigDecimal SaldoInicialITD = BigDecimal.ZERO;
		
		public BigDecimal AportesMTD = BigDecimal.ZERO;
		public BigDecimal AportesYTD = BigDecimal.ZERO;
		public BigDecimal AportesITD = BigDecimal.ZERO;
		
		public BigDecimal RescatesMTD = BigDecimal.ZERO;
		public BigDecimal RescatesYTD = BigDecimal.ZERO;
		public BigDecimal RescatesITD = BigDecimal.ZERO;

		public BigDecimal UtilidadMTD = BigDecimal.ZERO;
		public BigDecimal UtilidadYTD = BigDecimal.ZERO;
		public BigDecimal UtilidadITD = BigDecimal.ZERO;
		
		public BigDecimal SaldoFinalMTD = BigDecimal.ZERO;
		public BigDecimal SaldoFinalYTD = BigDecimal.ZERO;
		public BigDecimal SaldoFinalITD = BigDecimal.ZERO;	
		
		public BigDecimal RentabilidadMTD = BigDecimal.ZERO;
		public BigDecimal RentabilidadYTD = BigDecimal.ZERO;
		public BigDecimal RentabilidadITD = BigDecimal.ZERO;			

		public Long getIdCliente() {
			return IdCliente;
		}
		public void setIdCliente(Long idCliente) {
			IdCliente = idCliente;
		}
		public BigDecimal getSaldoInicialMTD() {
			return SaldoInicialMTD;
		}
		public void setSaldoInicialMTD(BigDecimal saldoInicialMTD) {
			SaldoInicialMTD = saldoInicialMTD;
		}
		public BigDecimal getSaldoInicialYTD() {
			return SaldoInicialYTD;
		}
		public void setSaldoInicialYTD(BigDecimal saldoInicialYTD) {
			SaldoInicialYTD = saldoInicialYTD;
		}
		public BigDecimal getSaldoInicialITD() {
			return SaldoInicialITD;
		}
		public void setSaldoInicialITD(BigDecimal saldoInicialITD) {
			SaldoInicialITD = saldoInicialITD;
		}
		public BigDecimal getAportesMTD() {
			return AportesMTD;
		}
		public void setAportesMTD(BigDecimal aportesMTD) {
			AportesMTD = aportesMTD;
		}
		public BigDecimal getAportesYTD() {
			return AportesYTD;
		}
		public void setAportesYTD(BigDecimal aportesYTD) {
			AportesYTD = aportesYTD;
		}
		public BigDecimal getAportesITD() {
			return AportesITD;
		}
		public void setAportesITD(BigDecimal aportesITD) {
			AportesITD = aportesITD;
		}
		public BigDecimal getRescatesMTD() {
			return RescatesMTD;
		}
		public void setRescatesMTD(BigDecimal rescatesMTD) {
			RescatesMTD = rescatesMTD;
		}
		public BigDecimal getRescatesYTD() {
			return RescatesYTD;
		}
		public void setRescatesYTD(BigDecimal rescatesYTD) {
			RescatesYTD = rescatesYTD;
		}
		public BigDecimal getRescatesITD() {
			return RescatesITD;
		}
		public void setRescatesITD(BigDecimal rescatesITD) {
			RescatesITD = rescatesITD;
		}
		public BigDecimal getUtilidadMTD() {
			return UtilidadMTD;
		}
		public void setUtilidadMTD(BigDecimal utilidadMTD) {
			UtilidadMTD = utilidadMTD;
		}
		public BigDecimal getUtilidadYTD() {
			return UtilidadYTD;
		}
		public void setUtilidadYTD(BigDecimal utilidadYTD) {
			UtilidadYTD = utilidadYTD;
		}
		public BigDecimal getUtilidadITD() {
			return UtilidadITD;
		}
		public void setUtilidadITD(BigDecimal utilidadITD) {
			UtilidadITD = utilidadITD;
		}
		public BigDecimal getSaldoFinalMTD() {
			return SaldoFinalMTD;
		}
		public void setSaldoFinalMTD(BigDecimal saldoFinalMTD) {
			SaldoFinalMTD = saldoFinalMTD;
		}
		public BigDecimal getSaldoFinalYTD() {
			return SaldoFinalYTD;
		}
		public void setSaldoFinalYTD(BigDecimal saldoFinalYTD) {
			SaldoFinalYTD = saldoFinalYTD;
		}
		public BigDecimal getSaldoFinalITD() {
			return SaldoFinalITD;
		}
		public void setSaldoFinalITD(BigDecimal saldoFinalITD) {
			SaldoFinalITD = saldoFinalITD;
		}
		public BigDecimal getRentabilidadMTD() {
			return RentabilidadMTD;
		}
		public void setRentabilidadMTD(BigDecimal rentabilidadMTD) {
			RentabilidadMTD = rentabilidadMTD;
		}
		public BigDecimal getRentabilidadYTD() {
			return RentabilidadYTD;
		}
		public void setRentabilidadYTD(BigDecimal rentabilidadYTD) {
			RentabilidadYTD = rentabilidadYTD;
		}
		public BigDecimal getRentabilidadITD() {
			return RentabilidadITD;
		}
		public void setRentabilidadITD(BigDecimal rentabilidadITD) {
			RentabilidadITD = rentabilidadITD;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------------------------------------------------
	// CLASE DE PRIMER MOVIMIENTO DE PORTAFOLIO
	// -------------------------------------------------------------------------------------------
	public static class FechaPrimerMovimientoPortafolio {
		public Long IdCliente;
		public Date FechaEfectivaMovimiento;
		public Long IdPortafolio;
		public Date FechaCreacion;

		public Long getIdCliente() {
			return IdCliente;
		}
		public void setIdCliente(Long idCliente) {
			IdCliente = idCliente;
		}
		public Date getFechaEfectivaMovimiento() {
			return FechaEfectivaMovimiento;
		}
		public void setFechaEfectivaMovimiento(Date fechaEfectivaMovimiento) {
			FechaEfectivaMovimiento = fechaEfectivaMovimiento;
		}
		public Long getIdPortafolio() {
			return IdPortafolio;
		}
		public void setIdPortafolio(Long idPortafolio) {
			IdPortafolio = idPortafolio;
		}
		public Date getFechaCreacion() {
			return FechaCreacion;
		}
		public void setFechaCreacion(Date fechaCreacion) {
			FechaCreacion = fechaCreacion;
		}
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MonedaCtaInversion{
		public String IdMoneda;
		public Long IdCliente;
		public BigDecimal CuentaInversionMTD = BigDecimal.ZERO;
		public BigDecimal CuentaInversionYTD = BigDecimal.ZERO;
		public BigDecimal CuentaInversionITD = BigDecimal.ZERO;
		public BigDecimal CuentaInversionFinal = BigDecimal.ZERO;
				
		public String toString() {
			return Utils.printJson(this, false);
		}
	} 

	// -------------------------------------------------------------------------------------------
	// FUNCION PARA OBTENER DISTINTC EN UN STREAM
	// -------------------------------------------------------------------------------------------	
	public <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	// -------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT
	// -------------------------------------------------------------------------------------------
	public static class FlujoPatrimonialYRentabilidad_Salida {
		public Long P_OUT_ERRORESCONTADOR = 0L;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public HashMap<String,List<HashMap<String, Object>>> P_OUT_SALIDA = new HashMap<>();
		
		public FlujoPatrimonialYRentabilidad_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------
	@SuppressWarnings("unlikely-arg-type")
	public FlujoPatrimonialYRentabilidad_Salida logica(Connection con, FlujoPatrimonialYRentabilidad_Entrada in)
			throws Exception {

		FlujoPatrimonialYRentabilidad_Salida out = new FlujoPatrimonialYRentabilidad_Salida();

		HashMap<Long, FechaPrimerMovimientoPortafolio> fechasIncialesOperaciones = getFechasInicialesOperacionPortafolio(in.p_Datos, con);
		
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		// PRIMERA FECHA DE MOVIMIENTO DE getFechasInicialesOperacionPortafolio
		FechaPrimerMovimientoPortafolio fechaPrimerMovimiento = Collections.min(fechasIncialesOperaciones.values(), Comparator.comparing(m -> m.FechaCreacion));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String fechaCreacionPortafolio = sdf.format(fechaPrimerMovimiento.getFechaCreacion());

		// MOVIMIENTOS DE TITULOS
		FlujoPatrimonialYRentabilidad_ProcesaMovimientosTitulos movimientosTitulos = new FlujoPatrimonialYRentabilidad_ProcesaMovimientosTitulos();
		ProcesaMovimientosTitulo_Entrada entradaMovimientosTitulos = new ProcesaMovimientosTitulo_Entrada();
		ProcesaMovimientosTitulo_Salida salidaMovimientosTitulos = new ProcesaMovimientosTitulo_Salida();
		entradaMovimientosTitulos.p_FechaHasta = in.p_FechaHasta;
		entradaMovimientosTitulos.MovimientosTitulo = in.p_MovimientosTitulos;
		entradaMovimientosTitulos.FechasInicialesPortaFolioMovimientos = fechasIncialesOperaciones;
		entradaMovimientosTitulos.p_Datos = in.p_Datos;
		entradaMovimientosTitulos.p_IdEmpresa = in.p_IdEmpresa;
		salidaMovimientosTitulos = movimientosTitulos.logica(con, entradaMovimientosTitulos);

		// MOVIMIENTOS DE CAJA
		FlujoPatrimonialYRentabilidad_ProcesaMovimientosCaja procesaMovimientosCaja = new FlujoPatrimonialYRentabilidad_ProcesaMovimientosCaja();
		ProcesaMovimientosCaja_Entrada entradaMovimientosCaja = new ProcesaMovimientosCaja_Entrada();
		ProcesaMovimientosCaja_Salida salidaMovimientosCaja = new ProcesaMovimientosCaja_Salida();
		entradaMovimientosCaja.p_IdEmpresa = in.p_IdEmpresa;
		entradaMovimientosCaja.p_FechaHasta = in.p_FechaHasta;
		entradaMovimientosCaja.MovimientosCaja = in.p_MovimientosCaja;
		entradaMovimientosCaja.p_Datos = in.p_Datos;
		entradaMovimientosCaja.FechasInicialesPortaFolioMovimientos = fechasIncialesOperaciones;
		salidaMovimientosCaja = procesaMovimientosCaja.logica(con, entradaMovimientosCaja);
		//System.out.println(salidaMovimientosCaja);

		// SALDO INICIAL Y FINAL
		FlujoPatrimonialYRentabilidad_SaldoFinal procesaSaldoFinal = new FlujoPatrimonialYRentabilidad_SaldoFinal();
		FlujoPatrimonialYRentabilidad_SaldoFinal_Entrada entradaSaldoFinal = new FlujoPatrimonialYRentabilidad_SaldoFinal_Entrada();
		FlujoPatrimonialYRentabilidad_SaldoFinal_Salida salidaSaldoFinal = new FlujoPatrimonialYRentabilidad_SaldoFinal_Salida();
		entradaSaldoFinal.p_FechaHasta = in.p_FechaHasta;
		entradaSaldoFinal.p_Datos = in.p_Datos;
		entradaSaldoFinal.p_CRM = salidaMovimientosCaja.P_OUT_SALIDA;
		entradaSaldoFinal.p_IdEmpresa = in.p_IdEmpresa;
		entradaSaldoFinal.p_fechaCreacionCuenta = fechaCreacionPortafolio;
		entradaSaldoFinal.movimientosCajaActivo = in.movimientosCajaActivo;
		entradaSaldoFinal.movimientosCajaPasivo = in.movimientosCajaPasivo;
		entradaSaldoFinal.movimientosTitulosActivo = in.movimientosTitulosActivo;
		entradaSaldoFinal.movimientosTitulosPasivo = in.movimientosTitulosPasivo;
		
		salidaSaldoFinal = procesaSaldoFinal.logica(con, entradaSaldoFinal);
		//System.out.println(salidaSaldoFinal);
		
		// OBTIENE SALDOS CUENTAS INVERSION
		ConsultarSaldoCuentaInversion servicioConsultarSaldoCuenta = new ConsultarSaldoCuentaInversion();
		EntradaConsultarSaldoCuentaInversion entradaConsultarSaldoCuenta = new EntradaConsultarSaldoCuentaInversion();
		
		entradaConsultarSaldoCuenta.p_IdEmpresa 			= in.p_IdEmpresa;
		entradaConsultarSaldoCuenta.p_IdUsuario         	= in.p_IdUsuario;
		entradaConsultarSaldoCuenta.p_IdModulo				= in.p_IdModulo;
		entradaConsultarSaldoCuenta.p_UsaReglasNegocio		= "S";
		entradaConsultarSaldoCuenta.p_IncluyeOtrosSaldos	= "S";		
		entradaConsultarSaldoCuenta.p_FechaHasta			= in.p_FechaHasta;
		entradaConsultarSaldoCuenta.p_IncluyeProductoApv    = "N";		
		entradaConsultarSaldoCuenta.p_IdProductoAgrupador	= null;
		entradaConsultarSaldoCuenta.p_FechaDesde			= fechaCreacionPortafolio;
		entradaConsultarSaldoCuenta.p_IdCliente				= in.p_Datos.get(0).IdCliente;
		entradaConsultarSaldoCuenta.p_IdPortafolio			= in.p_Datos.size() > 1 ? null : in.p_Datos.get(0).IdPortafolio;
		
		List<InCuentaInversionConsultar> lstCuentaInversionConsultar = new ArrayList<InCuentaInversionConsultar>();
		
		InCuentaInversionConsultar objCuentaInversionConsultar = new InCuentaInversionConsultar();
		objCuentaInversionConsultar.IdEjecutivo  = null;
		objCuentaInversionConsultar.IdMoneda 	 = null;
		objCuentaInversionConsultar.IdPortafolio = in.p_Datos.size() > 1 ? null : in.p_Datos.get(0).IdPortafolio;
		lstCuentaInversionConsultar.add(objCuentaInversionConsultar);
		
   		entradaConsultarSaldoCuenta.p_CuentaInversionAConsultar = lstCuentaInversionConsultar;
		
		SalidaConsultarSaldoCuentaInversion salidaConsultarSaldoCuenta = servicioConsultarSaldoCuenta.logica(con, entradaConsultarSaldoCuenta);
		
		List<Long> idsCuentaInversion = new ArrayList<>();
		for(SaldoInicialFinal saldo : salidaConsultarSaldoCuenta.P_OUT_SALDOINICIALFINAL) {
			idsCuentaInversion.add(saldo.IdCuentaInversion);
		}
		
		SQL sql = new SQL();
		HashMap<String,Object> params = new HashMap<>();
		params.put("idsCuentaInversion",idsCuentaInversion);
		List<MonedaCtaInversion> monedasCuentaInversion = sql.query(con,SQL_SELECT_MONEDAS_CTAINVERSION,params,MonedaCtaInversion.class);
		
		LocalDate fechaConsultaCartola = LocalDate.parse(in.p_FechaHasta);
		LocalDate fechaMTDDate   = LocalDate.of(fechaConsultaCartola.getYear(), fechaConsultaCartola.getMonth(), 1);
		LocalDate fechaYTDDate   = LocalDate.of(fechaConsultaCartola.getYear(), 1, 1);
		LocalDate fechaITDDate = LocalDate.parse(fechaCreacionPortafolio);
   		
		for(MonedaCtaInversion monedaCtaInv : monedasCuentaInversion) {
			for(SaldoPorFechaEfectiva saldo : salidaConsultarSaldoCuenta.P_OUT_SALDOPORFECHAEFECTIVA) {
	   			if(saldo.FechaSaldo.equals(fechaMTDDate.toString()) && monedaCtaInv.IdMoneda.equalsIgnoreCase(saldo.CodigoMoneda)) {
	   				monedaCtaInv.CuentaInversionMTD = monedaCtaInv.CuentaInversionMTD.add(saldo.Saldo);
	   			}else if(saldo.FechaSaldo.equals(fechaYTDDate.toString()) && monedaCtaInv.IdMoneda.equalsIgnoreCase(saldo.CodigoMoneda)) {
	   				monedaCtaInv.CuentaInversionYTD = monedaCtaInv.CuentaInversionYTD.add(saldo.Saldo);
	   			}else if(saldo.FechaSaldo.equals(fechaITDDate.toString()) && monedaCtaInv.IdMoneda.equalsIgnoreCase(saldo.CodigoMoneda)) {
	   				monedaCtaInv.CuentaInversionITD = monedaCtaInv.CuentaInversionITD.add(saldo.Saldo);
	   			}else if(saldo.FechaSaldo.equals(in.p_FechaHasta) && monedaCtaInv.IdMoneda.equalsIgnoreCase(saldo.CodigoMoneda)) {
	   				monedaCtaInv.CuentaInversionFinal = monedaCtaInv.CuentaInversionFinal.add(saldo.Saldo);
	   			}
	   		}
		}
		
		// CALCULO PARIDAD TIPO CAMBIO
		CalcularParidad servicioCalculaParidad = new CalcularParidad();
		CalcularParidad.CalcularParidadEntrada ParidadEntrada = new CalcularParidad.CalcularParidadEntrada();
		List<CalcularParidad.CalculoEntrada> listaParidadEntrada = new ArrayList<CalcularParidad.CalculoEntrada>();
		
		ParidadEntrada.p_IdEmpresa = in.p_IdEmpresa;
		ParidadEntrada.p_IdTrabajo = 0L;
		
		for(MonedaCtaInversion monedaCtaInv : monedasCuentaInversion) {
			if(!monedaSistema.equalsIgnoreCase(monedaCtaInv.IdMoneda)) {
				CalculoEntrada entradaCalculoParidad = new CalculoEntrada();
				entradaCalculoParidad.IdTrabajo = 0L;
				entradaCalculoParidad.IdInterno = 0L;
				entradaCalculoParidad.Fecha = in.p_FechaHasta;
				entradaCalculoParidad.IdMonedaOrigen = monedaSistema;
				entradaCalculoParidad.IdMonedaDestino = monedaCtaInv.IdMoneda; // MONDEA A TRANSFORMAR
				entradaCalculoParidad.ValorCambioLocalModificado = BigDecimal.ZERO;
				entradaCalculoParidad.IdUso = "";
				entradaCalculoParidad.MontoOrigen = monedaCtaInv.CuentaInversionITD;

				listaParidadEntrada.add(entradaCalculoParidad);
				
				entradaCalculoParidad.MontoOrigen = monedaCtaInv.CuentaInversionYTD;
				listaParidadEntrada.add(entradaCalculoParidad);
				
				entradaCalculoParidad.MontoOrigen = monedaCtaInv.CuentaInversionMTD;
				listaParidadEntrada.add(entradaCalculoParidad);
				
				entradaCalculoParidad.MontoOrigen = monedaCtaInv.CuentaInversionFinal;
				listaParidadEntrada.add(entradaCalculoParidad);
			}
			
		}
		
		ParidadEntrada.p_CalculosEntrada = listaParidadEntrada;
		CalcularParidadSalida paridad = servicioCalculaParidad.logica(con, ParidadEntrada);
		
		HashMap<String,CalculoSalida> hashParidad = new HashMap<>();
		for(CalculoSalida p: paridad.p_out_CalculosSalida) {
			hashParidad.put(p.IdMonedaDestino + "_" + p.MontoOrigen,p);
		}
		
		BigDecimal CtaInvTotalITD = BigDecimal.ZERO;
		BigDecimal CtaInvTotalYTD = BigDecimal.ZERO;
		BigDecimal CtaInvTotalMTD = BigDecimal.ZERO;
		BigDecimal CtaInvTotalFinal = BigDecimal.ZERO;
		
		for(MonedaCtaInversion monedaCtaInv : monedasCuentaInversion) {
			if(!monedaSistema.equalsIgnoreCase(monedaCtaInv.IdMoneda)) {
				CalculoSalida calculoParidadITD = hashParidad.get(monedaCtaInv.IdMoneda + "_" + monedaCtaInv.CuentaInversionITD);
				CalculoSalida calculoParidadYTD = hashParidad.get(monedaCtaInv.IdMoneda + "_" + monedaCtaInv.CuentaInversionYTD); 
				CalculoSalida calculoParidadMTD = hashParidad.get(monedaCtaInv.IdMoneda + "_" + monedaCtaInv.CuentaInversionMTD);
				CalculoSalida calculoParidadFinal = hashParidad.get(monedaCtaInv.IdMoneda + "_" + monedaCtaInv.CuentaInversionFinal);
				
				monedaCtaInv.CuentaInversionITD = calculoParidadITD != null ? monedaCtaInv.CuentaInversionITD.multiply(calculoParidadITD.FactorOrigenDestino).setScale(2,RoundingMode.HALF_UP) : monedaCtaInv.CuentaInversionITD;
				monedaCtaInv.CuentaInversionYTD = calculoParidadYTD != null ? monedaCtaInv.CuentaInversionYTD.multiply(calculoParidadYTD.FactorOrigenDestino).setScale(2,RoundingMode.HALF_UP) : monedaCtaInv.CuentaInversionYTD;
				monedaCtaInv.CuentaInversionMTD = calculoParidadMTD != null ? monedaCtaInv.CuentaInversionMTD.multiply(calculoParidadMTD.FactorOrigenDestino).setScale(2,RoundingMode.HALF_UP) : monedaCtaInv.CuentaInversionMTD;
				monedaCtaInv.CuentaInversionFinal = calculoParidadFinal != null ? monedaCtaInv.CuentaInversionFinal.multiply(calculoParidadFinal.FactorOrigenDestino).setScale(2,RoundingMode.HALF_UP) : monedaCtaInv.CuentaInversionFinal;
			}
			
			CtaInvTotalITD = CtaInvTotalITD.add(monedaCtaInv.CuentaInversionITD);
			CtaInvTotalYTD = CtaInvTotalYTD.add(monedaCtaInv.CuentaInversionYTD);
			CtaInvTotalMTD = CtaInvTotalMTD.add(monedaCtaInv.CuentaInversionMTD);
			CtaInvTotalFinal = CtaInvTotalFinal.add(monedaCtaInv.CuentaInversionFinal);
		}
		
		/*for(TablaSalida saldoFinal : salidaSaldoFinal.P_OUT_SALIDA) {
			for(MonedaCtaInversion monedaCtaInv : monedasCuentaInversion) {
				saldoFinal.totalITDSaldoFinal = saldoFinal.totalITDSaldoFinal.add(monedaCtaInv.CuentaInversionFinal);
				saldoFinal.totalITDSaldoInicial = saldoFinal.totalITDSaldoInicial.add(monedaCtaInv.CuentaInversionITD);
				saldoFinal.totalMTDSaldoFinal = saldoFinal.totalMTDSaldoFinal.add(monedaCtaInv.CuentaInversionFinal);
				saldoFinal.totalMTDSaldoInicial = saldoFinal.totalMTDSaldoInicial.add(monedaCtaInv.CuentaInversionMTD);
				saldoFinal.totalYTDSaldoFinal = saldoFinal.totalYTDSaldoFinal.add(monedaCtaInv.CuentaInversionFinal);
				saldoFinal.totalYTDSaldoInicial = saldoFinal.totalYTDSaldoInicial.add(monedaCtaInv.CuentaInversionYTD);
				saldoFinal.totalPatrimonioFechaConsulta = saldoFinal.totalPatrimonioFechaConsulta.add(monedaCtaInv.CuentaInversionFinal);
			}
		}*/
		
		// RENTABILIDAD
		CalcularRentabilidad rentabilidad = new CalcularRentabilidad();
		CalcularRentabilidad_Entrada entradaRentabilidad = new CalcularRentabilidad_Entrada(); 
		CalcularRentabilidad_Salida salidaRentabilidad = new CalcularRentabilidad_Salida();
		entradaRentabilidad.FechaConsulta = in.p_FechaHasta;
		entradaRentabilidad.MovimientosCaja = salidaMovimientosCaja.P_OUT_SALIDA;
		entradaRentabilidad.MovimientosTitulo = salidaMovimientosTitulos.P_OUT_SALIDA;
		entradaRentabilidad.SaldoFinal = salidaSaldoFinal.P_OUT_SALIDA;
		entradaRentabilidad.esPauta = in.esPauta;
		entradaRentabilidad.p_IdEmpresa = in.p_IdEmpresa; 
		entradaRentabilidad.ListaClientes = in.p_Datos;
		//System.out.println(entradaRentabilidad);
		salidaRentabilidad = rentabilidad.logica(con, entradaRentabilidad);
		
		// SALIDA DE MOVIMIENTOS DE TITULOS, CAJAS, SALDO FINAL Y RENTABILIDAD
		HashMap<String, Object> salidaTitulosData;
		salidaTitulosData = Utils.List2Hash(salidaMovimientosTitulos.P_OUT_SALIDA, "idCliente");
		HashMap<String, Object> salidaCajaData;
		salidaCajaData = Utils.List2Hash(salidaMovimientosCaja.P_OUT_SALIDA, "idCliente");
		HashMap<String, Object> salidaSaldoFinalData;
		salidaSaldoFinalData = Utils.List2Hash(salidaSaldoFinal.P_OUT_SALIDA, "idCliente");
		HashMap<String, Object> salidaRentabilidadData;
		salidaRentabilidadData = Utils.List2Hash(salidaRentabilidad.P_OUT_SALIDA, "IdCliente");
		
		SalidaPauta dato = new SalidaPauta();
		List<SalidaPauta> datoSalida = new ArrayList<SalidaPauta>();
		// Get distinct objects by key
		List<DatosCliente> listaClientes = in.p_Datos.stream().filter(distinctByKey(p -> p.getIdCliente())).collect(Collectors.toList());
		
		// Recorre los distintos clientes
		for (DatosCliente cliente : listaClientes) {
			// Inicializacion de variables
			dato = new SalidaPauta();
			// APORTES
			BigDecimal ingresosTituloMTD = BigDecimal.ZERO;
			BigDecimal ingresosTituloYTD = BigDecimal.ZERO;
			BigDecimal ingresosTituloITD = BigDecimal.ZERO;
			BigDecimal abonosCajaMTD = BigDecimal.ZERO;
			BigDecimal abonosCajaYTD = BigDecimal.ZERO;
			BigDecimal abonosCajaITD = BigDecimal.ZERO;
			//RESCATES
			BigDecimal retirosTituloMTD = BigDecimal.ZERO;
			BigDecimal retirosTituloYTD = BigDecimal.ZERO;
			BigDecimal retirosTituloITD = BigDecimal.ZERO;
			BigDecimal cargosCajaMTD = BigDecimal.ZERO;
			BigDecimal cargosCajaYTD = BigDecimal.ZERO;
			BigDecimal cargosCajaITD = BigDecimal.ZERO;
			//SALDO INICIAL
			BigDecimal saldoInicialMTD = BigDecimal.ZERO;
			BigDecimal saldoInicialYTD = BigDecimal.ZERO;
			BigDecimal saldoInicialITD = BigDecimal.ZERO;
			//SALDO FINAL
			BigDecimal saldoFinalMTD = BigDecimal.ZERO;
			BigDecimal saldoFinalYTD = BigDecimal.ZERO;
			BigDecimal saldoFinalITD = BigDecimal.ZERO;	
			//RENTABILIDAD
			BigDecimal rentabilidadMTD = BigDecimal.ZERO;
			BigDecimal rentabilidadYTD = BigDecimal.ZERO;
			BigDecimal rentabilidadITD = BigDecimal.ZERO;
			
			dato.IdCliente = cliente.IdCliente;

			// Total Titulo por IdCliente
			if (salidaTitulosData.get(dato.IdCliente) != null) {
				SaldoCuentaCliente objTitulos = (SaldoCuentaCliente) salidaTitulosData.get(dato.IdCliente);
				ingresosTituloMTD = objTitulos.totalIngresoTituloMTD;
				ingresosTituloYTD = objTitulos.totalIngresoTituloYTD;
				ingresosTituloITD = objTitulos.totalIngresoTituloITD;
				retirosTituloMTD = objTitulos.totalRetiroTituloMTD;
				retirosTituloYTD = objTitulos.totalRetiroTituloYTD;
				retirosTituloITD = objTitulos.totalRetiroTituloITD;
			}

			// Total Caja por IdCliente
			if (salidaCajaData.get(dato.IdCliente) != null) {
				SaldoCuentaCliente objCajas = (SaldoCuentaCliente) salidaCajaData.get(dato.IdCliente);
				abonosCajaMTD = objCajas.totalAbonosCajaMTD;
				abonosCajaYTD = objCajas.totalAbonosCajaYTD;
				abonosCajaITD = objCajas.totalAbonosCajaITD;
				cargosCajaMTD = objCajas.totalCargosCajaMTD;
				cargosCajaYTD = objCajas.totalCargosCajaYTD;
				cargosCajaITD = objCajas.totalCargosCajaITD;
			}
			// Total Saldo Incial y Final por IdCliente
			if (salidaSaldoFinalData.get(dato.IdCliente) != null) {
				TablaSalida objSaldoFinal = (TablaSalida) salidaSaldoFinalData.get(dato.IdCliente);
				saldoFinalMTD = objSaldoFinal.totalMTDSaldoFinal;
				saldoFinalYTD = objSaldoFinal.totalYTDSaldoFinal;
				saldoFinalITD = objSaldoFinal.totalITDSaldoFinal;
				saldoInicialMTD = objSaldoFinal.totalMTDSaldoInicial;
				saldoInicialYTD = objSaldoFinal.totalYTDSaldoInicial;
				saldoInicialITD = objSaldoFinal.totalITDSaldoInicial;
			}	
			// Total de la Rentabilidad por IdCliente
			if (salidaRentabilidadData.get(dato.IdCliente) != null) {
				Rentabilidad objRentabilidad = (Rentabilidad) salidaRentabilidadData.get(dato.IdCliente);
				rentabilidadMTD = objRentabilidad.RentabilidadMTD;
				rentabilidadYTD = objRentabilidad.RentabilidadYTD;
				rentabilidadITD = objRentabilidad.RentabilidadITD;
			}
			dato.SaldoInicialMTD = saldoInicialMTD.add(CtaInvTotalMTD).setScale(2,BigDecimal.ROUND_UP);
			dato.SaldoInicialYTD = saldoInicialYTD.add(CtaInvTotalYTD).setScale(2,BigDecimal.ROUND_UP);
			dato.SaldoInicialITD = saldoInicialITD.add(CtaInvTotalITD).setScale(2,BigDecimal.ROUND_UP);
			
			dato.AportesMTD = ingresosTituloMTD.add(abonosCajaMTD).setScale(2,BigDecimal.ROUND_UP);
			dato.AportesYTD = ingresosTituloYTD.add(abonosCajaYTD).setScale(2,BigDecimal.ROUND_UP);
			dato.AportesITD = ingresosTituloITD.add(abonosCajaITD).setScale(2,BigDecimal.ROUND_UP);
			
			dato.RescatesMTD = retirosTituloMTD.add(cargosCajaMTD.setScale(2,BigDecimal.ROUND_UP));
			dato.RescatesYTD = retirosTituloYTD.add(cargosCajaYTD).setScale(2,BigDecimal.ROUND_UP);
			dato.RescatesITD = retirosTituloITD.add(cargosCajaITD).setScale(2,BigDecimal.ROUND_UP);
			
			//Por definicion de cliente en todas las columnas el saldo final corresponde al patrimonio actual (mismo patrimonio en composicion de la cartera)
			saldoFinalMTD = in.totalPatrimonio;
			saldoFinalYTD = in.totalPatrimonio;
			saldoFinalITD = in.totalPatrimonio;
						
			// Utilidad = Saldo Final - Saldo Inicial - Abonos e Ingreso de titulos + Cargos y retiros de titulos
			dato.UtilidadMTD = (((saldoFinalMTD.add(CtaInvTotalFinal)).subtract(dato.SaldoInicialMTD)).subtract(dato.AportesMTD)).add(dato.RescatesMTD).setScale(2,BigDecimal.ROUND_UP);
			dato.UtilidadYTD = (((saldoFinalYTD.add(CtaInvTotalFinal)).subtract(dato.SaldoInicialYTD)).subtract(dato.AportesYTD)).add(dato.RescatesYTD).setScale(2,BigDecimal.ROUND_UP);
			dato.UtilidadITD = (((saldoFinalITD.add(CtaInvTotalFinal)).subtract(dato.SaldoInicialITD)).subtract(dato.AportesITD)).add(dato.RescatesITD).setScale(2,BigDecimal.ROUND_UP);				

			
			dato.SaldoFinalMTD = saldoFinalMTD.add(CtaInvTotalFinal);
			dato.SaldoFinalYTD = saldoFinalYTD.add(CtaInvTotalFinal);
			dato.SaldoFinalITD = saldoFinalITD.add(CtaInvTotalFinal);

			//Rentabilidad
			if(dato.SaldoInicialMTD.compareTo(BigDecimal.ZERO)==0) {
				dato.RentabilidadMTD  = BigDecimal.ZERO;
			}else {
				dato.RentabilidadMTD = dato.UtilidadMTD.divide(dato.SaldoInicialMTD,2,RoundingMode.HALF_UP);// rentabilidadMTD;
			}
			
			if(dato.SaldoInicialYTD.compareTo(BigDecimal.ZERO)==0) {
				dato.RentabilidadYTD  = BigDecimal.ZERO;
			}else {
				dato.RentabilidadYTD = dato.UtilidadYTD.divide(dato.SaldoInicialYTD,2,RoundingMode.HALF_UP);//rentabilidadYTD;
			}
			
			if(dato.SaldoInicialITD.compareTo(BigDecimal.ZERO)==0) {
				dato.RentabilidadITD  = BigDecimal.ZERO;
			}else {
				dato.RentabilidadITD = dato.UtilidadITD.divide(dato.SaldoInicialITD,2,RoundingMode.HALF_UP);//rentabilidadITD;
			}
			
			datoSalida.add(dato);
		}
		
		// Prepara la salida para el Batch
		if (in.esPauta) {
			HashMap<String, Object> salidaPauta;
			salidaPauta = Utils.List2Hash(datoSalida, "IdCliente");
			List<HashMap<String, Object>> listaPauta = new ArrayList<HashMap<String,Object>>();
			listaPauta.add(salidaPauta);
			out.P_OUT_SALIDA.put("FlujoPatrimonial", listaPauta);
		} 
		// Salida para la cartola online
		else {
			HashMap<String, Object> saldoInicial = new HashMap<>();
			HashMap<String, Object> aportes = new HashMap<>();
			HashMap<String, Object> rescates = new HashMap<>();
			HashMap<String, Object> utilidad = new HashMap<>();
			HashMap<String, Object> saldoFinal = new HashMap<>();
			HashMap<String, Object> totalPatrimonio = new HashMap<>();
			
			// TOTAL SALDO INICIAL
			BigDecimal saldoInicialMTD = datoSalida.stream().map(SalidaPauta::getSaldoInicialMTD).reduce(BigDecimal::add).get();
			BigDecimal saldoInicialYTD = datoSalida.stream().map(SalidaPauta::getSaldoInicialYTD).reduce(BigDecimal::add).get();
			BigDecimal saldoInicialITD = datoSalida.stream().map(SalidaPauta::getSaldoInicialITD).reduce(BigDecimal::add).get();
			
			// TOTAL ABONOS			
			BigDecimal totalAbonosMTD = datoSalida.stream().map(SalidaPauta::getAportesMTD).reduce(BigDecimal::add).get();
			BigDecimal totalAbonosYTD = datoSalida.stream().map(SalidaPauta::getAportesYTD).reduce(BigDecimal::add).get();
			BigDecimal totalAbonosITD = datoSalida.stream().map(SalidaPauta::getAportesITD).reduce(BigDecimal::add).get();
			// TOTAL CARGOS			
			BigDecimal totalCargosMTD = datoSalida.stream().map(SalidaPauta::getRescatesMTD).reduce(BigDecimal::add).get();
			BigDecimal totalCargosYTD = datoSalida.stream().map(SalidaPauta::getRescatesYTD).reduce(BigDecimal::add).get();
			BigDecimal totalCargosITD = datoSalida.stream().map(SalidaPauta::getRescatesITD).reduce(BigDecimal::add).get();			
			// TOTAL UTILIDAD/PERDIDA
			BigDecimal utilidadPerdidaMTD = datoSalida.stream().map(SalidaPauta::getUtilidadMTD).reduce(BigDecimal::add).get();
			BigDecimal utilidadPerdidaYTD = datoSalida.stream().map(SalidaPauta::getUtilidadYTD).reduce(BigDecimal::add).get();
			BigDecimal utilidadPerdidaITD = datoSalida.stream().map(SalidaPauta::getUtilidadITD).reduce(BigDecimal::add).get();	
			// TOTAL SALDO FINAL			
			BigDecimal saldoFinalMTD = datoSalida.stream().map(SalidaPauta::getSaldoFinalMTD).reduce(BigDecimal::add).get();
			BigDecimal saldoFinalYTD = datoSalida.stream().map(SalidaPauta::getSaldoFinalYTD).reduce(BigDecimal::add).get();
			BigDecimal saldoFinalITD = datoSalida.stream().map(SalidaPauta::getSaldoFinalITD).reduce(BigDecimal::add).get();
			// TOTAL RENTABILIDAD
			BigDecimal rentabilidadMTD = datoSalida.stream().map(SalidaPauta::getRentabilidadMTD).reduce(BigDecimal::add).get();
			
			BigDecimal rentabilidadYTD = datoSalida.stream().map(SalidaPauta::getRentabilidadYTD).reduce(BigDecimal::add).get();
			BigDecimal rentabilidadITD = datoSalida.stream().map(SalidaPauta::getRentabilidadITD).reduce(BigDecimal::add).get();			
			
			
			// TITULOS (REFERENCIADOS DEL DICCIONARIO)
			String tituloSaldoInicial = Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_SALDO_INICIAL").msg();
			String tituloAbonoIngreso = Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_ABONO_INGRESO_TITULOS").msg();
			String tituloCargoRetiro = Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_CARGO_RETIRO_TITULOS").msg();
			String tituloUtilidadPerdida = Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_UTILIDAD/PERDIDA").msg();
			String tituloSaldoFinal = Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_SALDO_FINAL").msg();
			String tituloRentabilidad = Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_RENTABILIDAD_PORCENTAJE").msg();

			List<HashMap<String, Object>> salidaHash = new ArrayList<HashMap<String, Object>>();
			saldoInicial.put("Titulo", tituloSaldoInicial); 
			saldoInicial.put("MTD", saldoInicialMTD); 
			saldoInicial.put("YTD", saldoInicialYTD);
			saldoInicial.put("ITD", saldoInicialITD); 
			saldoInicial.put("Fecha", fechaCreacionPortafolio); 
			salidaHash.add(saldoInicial);
			
			aportes.put("Titulo", tituloAbonoIngreso); 
			aportes.put("MTD", totalAbonosMTD);
			aportes.put("YTD", totalAbonosYTD);
			aportes.put("ITD", totalAbonosITD);
			aportes.put("Fecha", fechaCreacionPortafolio);
			salidaHash.add(aportes);
			  
			rescates.put("Titulo", tituloCargoRetiro); 
			rescates.put("MTD", totalCargosMTD);
			rescates.put("YTD", totalCargosYTD); 
			rescates.put("ITD", totalCargosITD);
			rescates.put("Fecha", fechaCreacionPortafolio);		
			salidaHash.add(rescates);
			 
			utilidad.put("Titulo", tituloUtilidadPerdida); 
			utilidad.put("MTD", utilidadPerdidaMTD); 
			utilidad.put("YTD", utilidadPerdidaYTD);
			utilidad.put("ITD", utilidadPerdidaITD); 
			utilidad.put("Fecha", fechaCreacionPortafolio); 
			salidaHash.add(utilidad);
			 
			saldoFinal.put("Titulo", tituloSaldoFinal); 
			saldoFinal.put("MTD", saldoFinalMTD); 
			saldoFinal.put("YTD", saldoFinalYTD);
			saldoFinal.put("ITD", saldoFinalITD);
			saldoFinal.put("Fecha", fechaCreacionPortafolio); 
			salidaHash.add(saldoFinal);
			 
			totalPatrimonio.put("Titulo", tituloRentabilidad); 
			totalPatrimonio.put("MTD", rentabilidadMTD); 
			totalPatrimonio.put("YTD", rentabilidadYTD);
			totalPatrimonio.put("ITD", rentabilidadITD);
			totalPatrimonio.put("Fecha", fechaCreacionPortafolio); 
			salidaHash.add(totalPatrimonio);
			out.P_OUT_SALIDA.put("FlujoPatrimonial", salidaHash);
		}
		return out;
	}

	public HashMap<Long, FechaPrimerMovimientoPortafolio> getFechasInicialesOperacionPortafolio(
			List<DatosCliente> p_Datos, Connection con) throws SQLException {

		SQL sql = new SQL();
		SQL.exec(con, SQL_CREATE_TEMP_TABLE_CLIENTES_PORTAFOLIOS);
		sql.batch(con, SQL_INSERT_TEMP_TABLE_CLIENTES_PORTAFOLIOS, p_Datos, DatosCliente.class);

		List<FechaPrimerMovimientoPortafolio> fechasInicialesPortaFolioMovimientos = sql.query(con, SQL_SELECT_MOV_CTA,
				new HashMap<>(), FechaPrimerMovimientoPortafolio.class);
		SQL.exec(con, SQL_DROP_TEMP_TABLE_CLIENTES_PORTAFOLIOS); // Elimina la tabla temporal

		HashMap<Long, FechaPrimerMovimientoPortafolio> fechaInicioPortafolio = new HashMap<>();
		for (FechaPrimerMovimientoPortafolio fechaprimermovimiento : fechasInicialesPortaFolioMovimientos) {
			fechaInicioPortafolio.put(fechaprimermovimiento.IdPortafolio, fechaprimermovimiento);
		}

		return fechaInicioPortafolio;
	}

	// -------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	// -------------------------------------------------------------------------------------------

	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		FlujoPatrimonialYRentabilidad_Entrada in = gson.fromJson(jin.toString(),
				FlujoPatrimonialYRentabilidad_Entrada.class);
		FlujoPatrimonialYRentabilidad_Salida out = logica(conn, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// -------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------
	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getBOLSAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();

			String json = "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"NOR\",\"p_IdUsuario\":5,\"p_FechaDesde\":\"2019-07-01\",\"p_FechaHasta\":\"2019-07-11\",\"p_Datos\":[{\"IdCliente\":5375,\"IdPortafolio\":5843}]}";
			// String json =
			// "{\"p_IdEmpresa\":1,\"p_IdModulo\":\"NOR\",\"p_IdUsuario\":53,\"p_FechaDesde\":\"2019-01-01\",\"p_FechaHasta\":\"2019-07-02\",\"p_Datos\":[{\"IdCliente\":524891,\"IdPortafolio\":1451609},{\"IdCliente\":524891,\"IdPortafolio\":1451587},{\"IdCliente\":524891,\"IdPortafolio\":1418745},{\"IdCliente\":524891,\"IdPortafolio\":1451614},{\"IdCliente\":524891,\"IdPortafolio\":1451604},{\"IdCliente\":524891,\"IdPortafolio\":1451583},{\"IdCliente\":524891,\"IdPortafolio\":1451602},{\"IdCliente\":524891,\"IdPortafolio\":1451342},{\"IdCliente\":524891,\"IdPortafolio\":1451601},{\"IdCliente\":524891,\"IdPortafolio\":1451341},{\"IdCliente\":524891,\"IdPortafolio\":1450071},{\"IdCliente\":524891,\"IdPortafolio\":1443821},{\"IdCliente\":524891,\"IdPortafolio\":1451600},{\"IdCliente\":524891,\"IdPortafolio\":1450070},{\"IdCliente\":524891,\"IdPortafolio\":1451180},{\"IdCliente\":524891,\"IdPortafolio\":1451253},{\"IdCliente\":524891,\"IdPortafolio\":1451287},{\"IdCliente\":524891,\"IdPortafolio\":1451254},{\"IdCliente\":524891,\"IdPortafolio\":512348},{\"IdCliente\":524891,\"IdPortafolio\":1451618},{\"IdCliente\":524891,\"IdPortafolio\":1450055},{\"IdCliente\":524891,\"IdPortafolio\":1418752},{\"IdCliente\":524891,\"IdPortafolio\":1451616},{\"IdCliente\":524891,\"IdPortafolio\":1451199},{\"IdCliente\":524891,\"IdPortafolio\":1450037},{\"IdCliente\":524891,\"IdPortafolio\":1451179},{\"IdCliente\":524891,\"IdPortafolio\":1451615},{\"IdCliente\":524891,\"IdPortafolio\":1451704},{\"IdCliente\":524891,\"IdPortafolio\":1451705},{\"IdCliente\":524891,\"IdPortafolio\":1451580},{\"IdCliente\":524891,\"IdPortafolio\":1450068},{\"IdCliente\":524891,\"IdPortafolio\":512349},{\"IdCliente\":524891,\"IdPortafolio\":1451582},{\"IdCliente\":524891,\"IdPortafolio\":1451532},{\"IdCliente\":524891,\"IdPortafolio\":1451531},{\"IdCliente\":524891,\"IdPortafolio\":512341}]}";

			FlujoPatrimonialYRentabilidad_Entrada Objin = gson.fromJson(json,
					FlujoPatrimonialYRentabilidad_Entrada.class);

			FlujoPatrimonialYRentabilidad demo = new FlujoPatrimonialYRentabilidad();
			System.out.println("ENTRADA");
			// System.out.println(Objin.toString());

			try {
				FlujoPatrimonialYRentabilidad_Salida ObjOut = demo.logica(con, Objin);
				con.rollback();
				System.out.println("SALIDA");
				System.out.println(ObjOut.toString());
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			con.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== FIN PRUEBA LOCAL ==");
	}

}
