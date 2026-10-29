package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
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

import cl.bcs.normativa.cartola.ConsultarCartolaOnline.DatosCliente;
import cl.bcs.normativa.cartola.pojo.SaldoCuentaCliente;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class FlujoPatrimonialYRentabilidad_SaldoFinal implements JavaCall {

	private final String SQL_CREATE_TEMP_TABLE_CLIENTES_PORTAFOLIOS = "CREATE LOCAL TEMPORARY COLUMN TABLE #tmpTblClientesPortafolios(\"IdCliente\" BIGINT, \"IdPortafolio\" BIGINT);";
	private final String SQL_INSERT_TEMP_TABLE_CLIENTES_PORTAFOLIOS = "INSERT INTO #tmpTblClientesPortafolios VALUES(:IdCliente, :IdPortafolio);";
	private final String SQL_DROP_TEMP_TABLE_CLIENTES_PORTAFOLIOS = "DROP TABLE #tmpTblClientesPortafolios;";

	private final String SQL_SELECT_FWD = loadResourceToString(
			"cl/bcs/normativa/cartola/sql/FlujoPatrimonialYRentabilidad_CarteraFW.sql");
	private final String SQL_SELECT_PAC = loadResourceToString(
			"cl/bcs/normativa/cartola/sql/FlujoPatrimonialYRentabilidad_Pactos.sql");
	private final String SQL_SELECT_SIM = loadResourceToString(
			"cl/bcs/normativa/cartola/sql/FlujoPatrimonialYRentabilidad_Simultaneas.sql");
	private final String SQL_SELECT_EMPRESTITOS = loadResourceToString(
			"cl/bcs/normativa/cartola/sql/FlujoPatrimonialYRentabilidad_Emprestitos.sql");
	private final String SQL_SELECT_PRESTAMOS = loadResourceToString(
			"cl/bcs/normativa/cartola/sql/FlujoPatrimonialYRentabilidad_Prestamos.sql");

	BigDecimal sumaMTDSaldoFinal = BigDecimal.ZERO;
	BigDecimal sumaYTDSaldoFinal = BigDecimal.ZERO;
	BigDecimal sumaITDSaldoFinal = BigDecimal.ZERO;
	
	BigDecimal sumaMTDSaldoInicial = BigDecimal.ZERO;
	BigDecimal sumaYTDSaldoInicial = BigDecimal.ZERO;
	BigDecimal sumaITDSaldoInicial = BigDecimal.ZERO;
	
	BigDecimal sumaPatrimonioFechaConsulta = BigDecimal.ZERO;
	// -------------------------------------------------
	// CLASES DE ENTRADA
	// -------------------------------------------------
	public static class FlujoPatrimonialYRentabilidad_SaldoFinal_Entrada {
		public String p_FechaHasta;
		public Long p_IdEmpresa;
		public String p_fechaCreacionCuenta;
		public List<DatosCliente> p_Datos;
		public List<SaldoCuentaCliente> p_CRM;
		public List<Operaciones> movimientosCajaActivo;
		public List<Operaciones> movimientosCajaPasivo;
		public List<Operaciones> movimientosTitulosActivo;
		public List<Operaciones> movimientosTitulosPasivo;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class Operaciones {
		public Long 		IdCliente;
		public BigDecimal 	Monto;
		public Date 		FechaOperacion;

		public Operaciones() {
			this.IdCliente = 0L;
			this.Monto = BigDecimal.ZERO;
			this.FechaOperacion = new Date();
		}

		public Long getIdCliente() {
			return IdCliente;
		}

		public void setIdCliente(Long idCliente) {
			IdCliente = idCliente;
		}

		public BigDecimal getMonto() {
			return Monto;
		}

		public void setMonto(BigDecimal monto) {
			Monto = monto;
		}

		public Date getFechaOperacion() {
			return FechaOperacion;
		}

		public void setFechaOperacion(Date fechaOperacion) {
			FechaOperacion = fechaOperacion;
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	// -------------------------------------------------------------------------------------------
	// CLASES UTILITARIAS QUE REPRESENTAN OBJETOS OUT
	// -------------------------------------------------------------------------------------------

	public static class FlujoPatrimonialYRentabilidad_SaldoFinal_Salida {

		public List<TablaSalida> P_OUT_SALIDA;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR = 0L;

		public FlujoPatrimonialYRentabilidad_SaldoFinal_Salida() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_SALIDA = new ArrayList<TablaSalida>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class TablaSalida {
		public Long idCliente;
		public Long idEmpresa;
		
		public BigDecimal totalMTDSaldoFinal;
		public BigDecimal totalYTDSaldoFinal;
		public BigDecimal totalITDSaldoFinal;
		
		public BigDecimal totalMTDSaldoInicial;
		public BigDecimal totalYTDSaldoInicial;
		public BigDecimal totalITDSaldoInicial;
		public BigDecimal totalPatrimonioFechaConsulta;

		public TablaSalida() {
			this.idCliente = 0L;
			this.idEmpresa = 0L;
			this.totalMTDSaldoFinal 			= BigDecimal.ZERO;
			this.totalYTDSaldoFinal 			= BigDecimal.ZERO;
			this.totalITDSaldoFinal 			= BigDecimal.ZERO;
			this.totalMTDSaldoInicial			= BigDecimal.ZERO;
			this.totalYTDSaldoInicial			= BigDecimal.ZERO;
			this.totalITDSaldoInicial			= BigDecimal.ZERO;
			this.totalPatrimonioFechaConsulta   = BigDecimal.ZERO;
		}

		public BigDecimal getTotalMTDSaldoInicial() {
			return totalMTDSaldoInicial;
		}

		public void setTotalMTDSaldoInicial(BigDecimal totalMTDSaldoInicial) {
			this.totalMTDSaldoInicial = totalMTDSaldoInicial;
		}

		public BigDecimal getTotalYTDSaldoInicial() {
			return totalYTDSaldoInicial;
		}

		public void setTotalYTDSaldoInicial(BigDecimal totalYTDSaldoInicial) {
			this.totalYTDSaldoInicial = totalYTDSaldoInicial;
		}

		public BigDecimal getTotalPatrimonioFechaConsulta() {
			return totalPatrimonioFechaConsulta;
		}

		public void setTotalPatrimonioFechaConsulta(BigDecimal totalPatrimonioFechaConsulta) {
			this.totalPatrimonioFechaConsulta = totalPatrimonioFechaConsulta;
		}

		public Long getIdCliente() {
			return idCliente;
		}

		public void setIdCliente(Long idCliente) {
			this.idCliente = idCliente;
		}

		public BigDecimal getTotalMTDSaldoFinal() {
			return totalMTDSaldoFinal;
		}

		public void setTotalMTDSaldoFinal(BigDecimal totalMTDSaldoFinal) {
			this.totalMTDSaldoFinal = totalMTDSaldoFinal;
		}

		public BigDecimal getTotalYTDSaldoFinal() {
			return totalYTDSaldoFinal;
		}

		public void setTotalYTDSaldoFinal(BigDecimal totalYTDSaldoFinal) {
			this.totalYTDSaldoFinal = totalYTDSaldoFinal;
		}

		public BigDecimal getTotalITDSaldoFinal() {
			return totalITDSaldoFinal;
		}

		public void setTotalITDSaldoFinal(BigDecimal totalITDSaldoFinal) {
			this.totalITDSaldoFinal = totalITDSaldoFinal;
		}
		
		public BigDecimal getTotalITDSaldoInicial() {
			return totalITDSaldoInicial;
		}
		
		public void setTotalITDSaldoInicial(BigDecimal totalITDSaldoInicial) {
			this.totalITDSaldoInicial = totalITDSaldoInicial;
		}
		
		public Long getIdEmpresa() {
			return idEmpresa;
		}

		public void setIdEmpresa(Long idEmpresa) {
			this.idEmpresa = idEmpresa;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}

		public void add(TablaSalida dato) {
		}
		
	}

	// Utility function to get distinct IdCliente
	public <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	// -------------------------------------------------
	// LOGICA DEL SERVICIO
	// -------------------------------------------------
	@SuppressWarnings("unlikely-arg-type")
	public FlujoPatrimonialYRentabilidad_SaldoFinal_Salida logica(Connection conn,
			FlujoPatrimonialYRentabilidad_SaldoFinal_Entrada in) throws Exception {

		FlujoPatrimonialYRentabilidad_SaldoFinal_Salida out = new FlujoPatrimonialYRentabilidad_SaldoFinal_Salida();

		// Movimientos de Forward
		SQL sql = new SQL();
		SQL.exec(conn, SQL_CREATE_TEMP_TABLE_CLIENTES_PORTAFOLIOS);
		sql.batch(conn, SQL_INSERT_TEMP_TABLE_CLIENTES_PORTAFOLIOS, in.p_Datos, DatosCliente.class);
		List<Operaciones> operaciones = new ArrayList<Operaciones>();
		
		operaciones.addAll(sql.query(conn, SQL_SELECT_FWD, new HashMap<>(), Operaciones.class));
		
		// Movimientos de Pactos
		operaciones.addAll(sql.query(conn, SQL_SELECT_PAC, new HashMap<>(), Operaciones.class));

		// Movimientos de Simultaneas
		operaciones.addAll(sql.query(conn, SQL_SELECT_SIM, new HashMap<>(), Operaciones.class));

		// Movimientos de Premios (Emprestitos)
		operaciones.addAll(sql.query(conn, SQL_SELECT_EMPRESTITOS, new HashMap<>(), Operaciones.class));

		// Movimientos de Prima
		operaciones.addAll(sql.query(conn, SQL_SELECT_PRESTAMOS, new HashMap<>(), Operaciones.class));
		SQL.exec(conn, SQL_DROP_TEMP_TABLE_CLIENTES_PORTAFOLIOS); // Elimina la tabla temporal

		//Incorporacion Activos y Pasivos
		for (Operaciones operacion: in.movimientosCajaActivo) {
			Operaciones op = new Operaciones();
			op.IdCliente = operacion.IdCliente;
			op.FechaOperacion = operacion.FechaOperacion;
			op.Monto = operacion.Monto;
			operaciones.add(op);
		}
				
		for (Operaciones operacion: in.movimientosTitulosActivo) {
			Operaciones op = new Operaciones();
			op.IdCliente = operacion.IdCliente;
			op.FechaOperacion = operacion.FechaOperacion;
			op.Monto = operacion.Monto;
			operaciones.add(op);
		}
		
		for (Operaciones operacion: in.movimientosTitulosPasivo) {
			Operaciones op = new Operaciones();
			op.IdCliente = operacion.IdCliente;
			op.FechaOperacion = operacion.FechaOperacion;
			op.Monto = operacion.Monto.multiply(new BigDecimal(-1));
			operaciones.add(op);
		}
		
		
		for (Operaciones operacion: in.movimientosCajaPasivo) {
			Operaciones op = new Operaciones();
			op.IdCliente = operacion.IdCliente;
			op.FechaOperacion = operacion.FechaOperacion;
			op.Monto = operacion.Monto.multiply(new BigDecimal(-1));
			operaciones.add(op);
		}
		
		Collection<TablaSalida> saldosCuentaCliente = operaciones.stream().parallel().map(movimiento -> {
			
			LocalDate fechaConsultaCartola = LocalDate.parse(in.p_FechaHasta);
			String fechaStr = new SimpleDateFormat("yyyy-MM-dd").format(movimiento.FechaOperacion);
			LocalDate fechaOperacion = LocalDate.parse(fechaStr);
			
			
			LocalDate fechaMTDDate   = LocalDate.of(fechaConsultaCartola.getYear(), fechaConsultaCartola.getMonth(), 1); //Fecha Inicio mes consulta
			LocalDate fechaYTDDate   = LocalDate.of(fechaConsultaCartola.getYear(), 1, 1);						//fecha Inicio Anio consulta
			
			LocalDate periodoInicioMTD = fechaConsultaCartola.with( fechaConsultaCartola.minusMonths(1L).with( TemporalAdjusters.firstDayOfMonth()) ); //Inicio Mes anterior
	   		LocalDate periodoFinMTD    = fechaConsultaCartola.with( fechaConsultaCartola.minusMonths(1L).with( TemporalAdjusters.lastDayOfMonth()) ); //Fin Mes anterior
	   		
	   		LocalDate periodoInicioYTD = fechaConsultaCartola.with( fechaConsultaCartola.minusYears(1L).with( TemporalAdjusters.firstDayOfYear()) ); //Inicio Anio anterior
	   		LocalDate periodoFinYTD    = fechaConsultaCartola.with( fechaConsultaCartola.minusYears(1L).with( TemporalAdjusters.lastDayOfYear()) ); //Fin Anio anterior
	   		
	   		LocalDate periodoInicioITD = LocalDate.parse(in.p_fechaCreacionCuenta); //Inicio creacion cuenta
	   		LocalDate periodoFinITD = LocalDate.parse(in.p_fechaCreacionCuenta); //Fin creacion cuenta
	   		
	   		
	   		// Suma de Saldo Inicial
	   		if ((fechaOperacion.isAfter(periodoInicioMTD) || fechaOperacion.isEqual(periodoInicioMTD)) && (fechaOperacion.isBefore(periodoFinMTD)|| fechaOperacion.equals(periodoFinMTD))) {
				sumaMTDSaldoInicial = sumaMTDSaldoInicial.add(movimiento.Monto);
			}
	   		if ((fechaOperacion.isAfter(periodoInicioYTD) || fechaOperacion.isEqual(periodoInicioYTD)) && (fechaOperacion.isBefore(periodoFinYTD)|| fechaOperacion.equals(periodoFinYTD))) {
				sumaYTDSaldoInicial = sumaYTDSaldoInicial.add(movimiento.Monto);
			}
	   		if ((fechaOperacion.isAfter(periodoInicioITD) || fechaOperacion.isEqual(periodoInicioITD)) && (fechaOperacion.isBefore(periodoFinITD)|| fechaOperacion.equals(periodoFinITD))) {
				sumaITDSaldoInicial = sumaITDSaldoInicial.add(movimiento.Monto);
			}
	   		

	   		// Suma de Saldo Final			
			if (fechaOperacion.isAfter(fechaMTDDate) || fechaOperacion.isEqual(fechaMTDDate)) {
				sumaMTDSaldoFinal = sumaMTDSaldoFinal.add(movimiento.Monto);
			}
			if (fechaOperacion.isAfter(fechaYTDDate) || fechaOperacion.isEqual(fechaYTDDate)) {
				sumaYTDSaldoFinal = sumaYTDSaldoFinal.add(movimiento.Monto);
			}
			sumaITDSaldoFinal = sumaITDSaldoFinal.add(movimiento.Monto);
			
			// Suma Patrimonio Fecha Consulta
			if (fechaOperacion.equals(fechaConsultaCartola)) {
				sumaPatrimonioFechaConsulta = sumaPatrimonioFechaConsulta.add(movimiento.Monto);
			}
			
			TablaSalida salidatabla = new TablaSalida();
			salidatabla.idCliente = movimiento.IdCliente;
			salidatabla.totalMTDSaldoFinal = sumaMTDSaldoFinal;
			salidatabla.totalYTDSaldoFinal = sumaYTDSaldoFinal;
			salidatabla.totalITDSaldoFinal = sumaITDSaldoFinal;
			
			salidatabla.totalMTDSaldoInicial         = sumaMTDSaldoInicial;
			salidatabla.totalYTDSaldoInicial         = sumaYTDSaldoInicial;
			salidatabla.totalITDSaldoInicial		 = sumaITDSaldoInicial;
			salidatabla.totalPatrimonioFechaConsulta = sumaPatrimonioFechaConsulta;
			
			return salidatabla;

		}).collect(Collectors.toMap(TablaSalida::getIdCliente, Function.identity(), (left, rigth) -> {
			return left;
		})).values();

		List<TablaSalida> saldoscuentasclientes = saldosCuentaCliente.stream().collect(Collectors.toList());

		// Agrega el calculo del CRM a los Saldos finales acumulados por idCliente
		HashMap<String, Object> crmData;
		crmData = Utils.List2Hash(in.p_CRM, "idCliente");
		HashMap<String, Object> saldosData;
		saldosData = Utils.List2Hash(saldoscuentasclientes, "idCliente");

		TablaSalida dato = new TablaSalida();
		List<TablaSalida> datoSalida = new ArrayList<TablaSalida>();

		// Get distinct objects by key
		List<DatosCliente> listaClientes = in.p_Datos.stream().filter(distinctByKey(p -> p.getIdCliente()))
				.collect(Collectors.toList());
		
		// Recorre los distintos clientes
		for (DatosCliente cliente : listaClientes) {
			dato = new TablaSalida();
			BigDecimal crm_MTD_Final = BigDecimal.ZERO;
			BigDecimal crm_YTD_Final = BigDecimal.ZERO;
			BigDecimal crm_ITD_Final = BigDecimal.ZERO;
			BigDecimal crm_MTD_Inicial = BigDecimal.ZERO;
			BigDecimal crm_YTD_Inicial = BigDecimal.ZERO;
			BigDecimal crm_ITD_Inicial = BigDecimal.ZERO;

			dato.idCliente = cliente.IdCliente;

			// Obtiene los saldos acumulados de Forward, Pactos, Simultaneas, Emprestitos y Primas
			if (saldosData.get(dato.idCliente) != null) {
				TablaSalida objSaldos = (TablaSalida) saldosData.get(dato.idCliente);
				crm_MTD_Final = objSaldos.totalMTDSaldoFinal;
				crm_YTD_Final = objSaldos.totalYTDSaldoFinal;
				crm_ITD_Final = objSaldos.totalITDSaldoFinal;
				crm_MTD_Inicial = objSaldos.totalMTDSaldoInicial;
				crm_YTD_Inicial = objSaldos.totalYTDSaldoInicial;
				crm_ITD_Inicial = objSaldos.totalITDSaldoInicial;
			}

			// Calcula el total CRM (Activos - Pasivos) por idCliente para agregarlo a los saldos acumulados
			if (crmData.get(dato.idCliente) != null) {
				
				SaldoCuentaCliente ObjSaldosCRM = (SaldoCuentaCliente) crmData.get(dato.idCliente);

				crm_MTD_Final = (ObjSaldosCRM.totalVentasCRMTitulosMTD.add(ObjSaldosCRM.totalVentasCRMCajaMTD)).subtract((ObjSaldosCRM.totalComprasCRMTitulosMTD.add(ObjSaldosCRM.totalComprasCRMCajaMTD)));
				crm_YTD_Final = (ObjSaldosCRM.totalVentasCRMTitulosYTD.add(ObjSaldosCRM.totalVentasCRMCajaYTD)).subtract((ObjSaldosCRM.totalComprasCRMTitulosYTD.add(ObjSaldosCRM.totalComprasCRMCajaYTD)));
				crm_ITD_Final = (ObjSaldosCRM.totalVentasCRMTitulosITD.add(ObjSaldosCRM.totalVentasCRMCajaITD)).subtract((ObjSaldosCRM.totalComprasCRMTitulosITD.add(ObjSaldosCRM.totalComprasCRMCajaITD)));
				
			}
			dato.totalMTDSaldoFinal = crm_MTD_Final;
			dato.totalYTDSaldoFinal = crm_YTD_Final;
			dato.totalITDSaldoFinal = crm_ITD_Final;
			dato.totalMTDSaldoInicial = crm_MTD_Inicial;
			dato.totalYTDSaldoInicial = crm_YTD_Inicial;
			dato.totalITDSaldoInicial = crm_ITD_Inicial;
			dato.idEmpresa = in.p_IdEmpresa;
			datoSalida.add(dato);
		}

		out.P_OUT_SALIDA.addAll(datoSalida);
		return out;
	}
	
	// ------------------------------------------------------------------------------------------
	// OBJETO DE TRANSPORTE - CALL
	// -------------------------------------------------------------------------------------------
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		FlujoPatrimonialYRentabilidad_SaldoFinal_Entrada in = gson.fromJson(jin.toString(),
				FlujoPatrimonialYRentabilidad_SaldoFinal_Entrada.class);
		FlujoPatrimonialYRentabilidad_SaldoFinal_Salida out = logica(conn, in);

		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	// -------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// -------------------------------------------------
	public static void main(String[] args) {
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
					.serializeNulls().create();

			// String json =
			// "{\"fechaConsulta\":\"2019-07-01\",\"p_Clientes\":[{\"IdCliente\":100209},{\"IdCliente\":100064}]}";
			String json = "{\"p_FechaHasta\":\"2019-08-23\",\"p_Datos\":[{\"IdCliente\":249194,\"IdPortafolio\":1450263},{\"IdCliente\":249194,\"IdPortafolio\":153969}],\"p_CRM\":[{\"idCliente\":249194,\"saldoInicial\":null,\"totalComprasCajaMTD\":0,\"totalComprasCajaYTD\":0,\"totalComprasCajaITD\":0,\"totalIngresoTituloMTD\":0,\"totalIngresoTituloYTD\":0,\"totalIngresoTituloITD\":0,\"comprasCajaMTDRentabilidad\":0,\"comprasCajaYTDRentabilidad\":0,\"comprasCajaITDRentabilidad\":0,\"totalVentasCajaMTD\":0,\"totalVentasCajaYTD\":26752271.534883720930232558139534879,\"totalVentasCajaITD\":26752271.534883720930232558139534879,\"totalRetiroTituloMTD\":0,\"totalRetiroTituloYTD\":0,\"totalRetiroTituloITD\":0,\"ventasCajaMTDRentabilidad\":0,\"ventasCajaYTDRentabilidad\":1949458.34530419,\"ventasCajaITDRentabilidad\":989597.92274340,\"totalVentasCajaMTDPeriodoAnterior\":9321481,\"totalVentasCajaYTDPeriodoAnterior\":26752271.534883720930232558139534879,\"totalComprasCajaMTDPeriodoAnterior\":0,\"totalComprasCajaYTDPeriodoAnterior\":0},{\"idCliente\":524891,\"saldoInicial\":3608322.00000000,\"totalComprasCajaMTD\":0,\"totalComprasCajaYTD\":0,\"totalComprasCajaITD\":0,\"totalIngresoTituloMTD\":0,\"totalIngresoTituloYTD\":0,\"totalIngresoTituloITD\":0,\"comprasCajaMTDRentabilidad\":0,\"comprasCajaYTDRentabilidad\":0,\"comprasCajaITDRentabilidad\":0,\"totalVentasCajaMTD\":0,\"totalVentasCajaYTD\":2020000,\"totalVentasCajaITD\":2020000,\"totalRetiroTituloMTD\":0,\"totalRetiroTituloYTD\":0,\"totalRetiroTituloITD\":0,\"ventasCajaMTDRentabilidad\":0,\"ventasCajaYTDRentabilidad\":0,\"ventasCajaITDRentabilidad\":0,\"totalVentasCajaMTDPeriodoAnterior\":2020000,\"totalVentasCajaYTDPeriodoAnterior\":2020000,\"totalComprasCajaMTDPeriodoAnterior\":0,\"totalComprasCajaYTDPeriodoAnterior\":0},{\"idCliente\":249194,\"saldoInicial\":null,\"totalComprasCajaMTD\":0,\"totalComprasCajaYTD\":0,\"totalComprasCajaITD\":0,\"totalIngresoTituloMTD\":0,\"totalIngresoTituloYTD\":0,\"totalIngresoTituloITD\":0,\"comprasCajaMTDRentabilidad\":0,\"comprasCajaYTDRentabilidad\":0,\"comprasCajaITDRentabilidad\":0,\"totalVentasCajaMTD\":1000,\"totalVentasCajaYTD\":90046260,\"totalVentasCajaITD\":132598921,\"totalRetiroTituloMTD\":0,\"totalRetiroTituloYTD\":0,\"totalRetiroTituloITD\":0,\"ventasCajaMTDRentabilidad\":0,\"ventasCajaYTDRentabilidad\":9015025.04173623,\"ventasCajaITDRentabilidad\":8610852.71317829,\"totalVentasCajaMTDPeriodoAnterior\":46260,\"totalVentasCajaYTDPeriodoAnterior\":132598921,\"totalComprasCajaMTDPeriodoAnterior\":0,\"totalComprasCajaYTDPeriodoAnterior\":0},{\"idCliente\":1423323,\"saldoInicial\":null,\"totalComprasCajaMTD\":0,\"totalComprasCajaYTD\":0,\"totalComprasCajaITD\":0,\"totalIngresoTituloMTD\":0,\"totalIngresoTituloYTD\":0,\"totalIngresoTituloITD\":0,\"comprasCajaMTDRentabilidad\":0,\"comprasCajaYTDRentabilidad\":0,\"comprasCajaITDRentabilidad\":0,\"totalVentasCajaMTD\":463786379.992400000000000000000000101,\"totalVentasCajaYTD\":996221335.597500000000000000000000090,\"totalVentasCajaITD\":4294488039.580652000000000000000000090,\"totalRetiroTituloMTD\":0,\"totalRetiroTituloYTD\":0,\"totalRetiroTituloITD\":0,\"ventasCajaMTDRentabilidad\":0E-8,\"ventasCajaYTDRentabilidad\":0E-8,\"ventasCajaITDRentabilidad\":1243846913.95200000,\"totalVentasCajaMTDPeriodoAnterior\":463786379.992400000000000000000000101,\"totalVentasCajaYTDPeriodoAnterior\":4294488039.580652000000000000000000090,\"totalComprasCajaMTDPeriodoAnterior\":0,\"totalComprasCajaYTDPeriodoAnterior\":0}]}";

			FlujoPatrimonialYRentabilidad_SaldoFinal_Entrada Objin = gson.fromJson(json,
					FlujoPatrimonialYRentabilidad_SaldoFinal_Entrada.class);
			FlujoPatrimonialYRentabilidad_SaldoFinal demo = new FlujoPatrimonialYRentabilidad_SaldoFinal();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());

			try {
				FlujoPatrimonialYRentabilidad_SaldoFinal_Salida ObjOut = demo.logica(con, Objin);
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
