package cl.bcs.cuentaInversion;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.icu.util.Calendar;

import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.CantidadCustodia;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.CuentaInversion_Proceso;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.InstrumentosMoneyMarket;
import cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion.OtrosSaldos;
import cl.bcs.fondomutuo.ConsultarCustodiaPortafolioFondoMutuo.ValorCuota;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class GenerarArchivoClientesPorPagarYCobrar implements JavaCall {

    static final Logger logger = LogManager.getLogger(GenerarArchivoClientesPorPagarYCobrar.class);

    private final String SQL_SELECT_CONFIGURACION = loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarArchivoClientesPorPagarYCobrar_SelectConfiguracion.sql");
    private final String SQL_SELECT_ULTIMOS_SALDOS = loadResourceToString("cl/bcs/cuentaInversion/sql/GenerarArchivoClientesPorPagarYCobrar_SelectUltimosSaldos.sql");
    // Consultas desde ConsultarSaldoCuentaInversion
    private final String SQL_SELECT_INSTRUMENTOS_MONEY_MARKET = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Instrumentos_Money_Market.sql");
    private final String SQL_INSERT_CUENTA_INVERSION_TABLA_PASO = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Insert_Cuenta_Inversion_Tabla_Paso.sql");
    private final String SQL_INSERT_PORTAFOLIO_MONEDA = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Insert_Portafolio_Moneda.sql");
    private final String SQL_SELECT_SALDO_A_PLAZO_SIMULTANEAS = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Saldo_A_Plazo_Simultaneas.sql");
    private final String SQL_SELECT_SALDO_EN_GARANTIA = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Saldo_En_Garantia.sql");
    private final String SQL_SELECT_SALDOS_RETENIDOS = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Saldos_Retenidos.sql");
    private final String SQL_CLEAN_DELETE = "DELETE FROM \"BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Delete\" WHERE \"IdTrabajo\" = :IdTrabajo;";
    private final String SQL_SELECT_VALOR_CUOTA = loadResourceToString("cl/bcs/fondomutuo/sql/ConsultarCustodiaPortafolioFondoMutuo_SelectValorCuota.sql");
    private final String SQL_SELECT_CANTIDAD_CUSTODIA = loadResourceToString("cl/bcs/cuentaInversion/sql/ConsultarSaldoCuentaInversion_Select_Cantidad_Custodia.sql");

	// ----------------------------------------------------
	// CLASES DE ENTRADA SALIDA
	// ----------------------------------------------------
	public static class EntradaGenerarArchivoClientesPorPagarYCobrar extends BcsPojo {
		public Long p_IdEmpresa;// OBLIGATORIO
		public Long p_IdUsuario;// OBLIGATORIO
		public String p_IdModulo;// OBLIGATORIO
		public String FechaSistema;
		public String EsBusqueda;
		public String IdMoneda;
		public String TipoSaldo;// POS;NEG;TOD 
	}

    public static class SalidaGenerarArchivoClientePorPagarYCobrar extends BcsPojo {

	public List<MensajeSalida> P_OUT_MENSAJES;
	public Long P_OUT_ERRORESCONTADOR;
	public String P_OUT_RUTAARCHIVO;

	public List<ClientesPagarCobrar> P_OUT_RESULTADO;

	public SalidaGenerarArchivoClientePorPagarYCobrar() {
	    this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
	    this.P_OUT_ERRORESCONTADOR = 0L;
	    this.P_OUT_RUTAARCHIVO = "";
	}
    }

    // ----------------------------------------------------
    // CLASES DE PROCESO
    // ----------------------------------------------------

    public static class Configuracion extends BcsPojo {
	public String RutaBase;
	public String RutaCta;
	public String RutaArchivo;

    }

	public static class ClientesPagarCobrar extends BcsPojo {
		public Long IdCuentaInversion;
		public Long IdPersona;
		public Long IdCliente;
		public String DocumentoIdentidad;
		public String NombreCliente;
		public Long IdPortafolio;
		public String CodigoProposito;
		public String Relacion;
		public Date FechaIngresoRegistro;
		public Date FechaCertificacion;
		public Date FechaEfectivaMovimiento;
		public String IdMoneda;
		public BigDecimal Saldo;
		public String NombreAgenteCliente;
		public String DocumentoIdentidadAgente;
		public Long IdEjecutivo;
		public String NombreEjecutivoMovimiento;
		public String DocumentoIdentidadEjecutivo;
		public BigDecimal SaldoRetenido;
		public BigDecimal MoneyMarket;
		public BigDecimal Garantias;
		public BigDecimal Simultanea;
		public BigDecimal Pacto;

	public ClientesPagarCobrar() {
	    this.IdCuentaInversion = 0L;
	    this.IdPersona = 0L;
	    this.IdCliente = 0L;
	    this.DocumentoIdentidad = "";
	    this.IdPortafolio = 0L;
	    this.CodigoProposito = "";
	    this.Relacion = "";
	    this.FechaIngresoRegistro = Fecha.toDateTime("0001-01-01 01:01:01");
	    this.IdMoneda = "";
	    this.NombreAgenteCliente = "";
	    this.DocumentoIdentidadAgente = "";
	    this.IdEjecutivo = 0L;
	    this.NombreEjecutivoMovimiento = "";
	    this.DocumentoIdentidadEjecutivo = "";
	}
    }

    private static class TotalesPorRango extends BcsPojo {

	public BigDecimal rango1a7;
	public BigDecimal rango8y9;
	public BigDecimal rango10a30;
	public BigDecimal rango31oMas;

	public TotalesPorRango() {
	    rango1a7 = BigDecimal.ZERO;
	    rango8y9 = BigDecimal.ZERO;
	    rango10a30 = BigDecimal.ZERO;
	    rango31oMas = BigDecimal.ZERO;
	}

    }

    // ----------------------------------------------------
    // CALL
    // ----------------------------------------------------

    @Override
    public JsonObject call(Connection con, JsonObject jin) throws Exception {
	Gson gson = new Gson();

		EntradaGenerarArchivoClientesPorPagarYCobrar in = gson.fromJson(jin.toString(),
				EntradaGenerarArchivoClientesPorPagarYCobrar.class);
		// entramos a logica
		SalidaGenerarArchivoClientePorPagarYCobrar out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();

	return dataout;
    }
    // -----------------------------------------------------
    // LOGICA
    // -----------------------------------------------------

	public SalidaGenerarArchivoClientePorPagarYCobrar logica(Connection con,
			EntradaGenerarArchivoClientesPorPagarYCobrar in) throws Exception {
		logger.info("INICIO::GenerarArchivoClientePorPagarYCobrar");
		long inicio = System.currentTimeMillis();

		SalidaGenerarArchivoClientePorPagarYCobrar out = new SalidaGenerarArchivoClientePorPagarYCobrar();
		SQL sql = new SQL();
		HashMap<String, Object> paramsDatosClientes = new HashMap<String, Object>();
		paramsDatosClientes.put("IdEmpresa", in.p_IdEmpresa);
		paramsDatosClientes.put("IdMoneda", in.IdMoneda);
		paramsDatosClientes.put("TipoSaldo", in.TipoSaldo);

	List<ClientesPagarCobrar> datosClientes = new ArrayList<ClientesPagarCobrar>();
	// datosClientes = dumyData();
	datosClientes = sql.query(con, SQL_SELECT_ULTIMOS_SALDOS, paramsDatosClientes, ClientesPagarCobrar.class);

	// llenar tabla paso
	Long idTrabajo = (long) (Math.random() * 10000000000L);
	List<CuentaInversion_Proceso> datosProceso = new ArrayList<>();
	datosProceso = datosClientes.parallelStream().map(datoCliente -> {
	    CuentaInversion_Proceso datoProceso = new CuentaInversion_Proceso();
	    datoProceso.IdMoneda = datoCliente.IdMoneda;
	    datoProceso.IdPortafolio = datoCliente.IdPortafolio;
	    datoProceso.IdTrabajo = idTrabajo;
	    return datoProceso;
	}).collect(Collectors.toList());

	sql.batch(con, SQL_INSERT_CUENTA_INVERSION_TABLA_PASO, datosProceso, CuentaInversion_Proceso.class);
	sql.batch(con, SQL_INSERT_PORTAFOLIO_MONEDA.replace(":IdTrabajo", idTrabajo + ""), new ArrayList<>(), Object.class);
	SQL.exec(con, "DELETE FROM \"BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso\" WHERE \"IdMoneda\" IS NULL ");


	HashMap<String, Object> paramsOtrosSaldos = new HashMap<String, Object>();
	paramsOtrosSaldos.put("p_IdEmpresa", in.p_IdEmpresa);
	paramsOtrosSaldos.put("p_FechaDesde", in.FechaSistema);
	paramsOtrosSaldos.put("IdTrabajo", idTrabajo);


	List<OtrosSaldos> saldosGarantias = sql.query(con, SQL_SELECT_SALDO_EN_GARANTIA, paramsOtrosSaldos, OtrosSaldos.class);
	Map<Object, BigDecimal> garantiasPorCuenta = saldosGarantias.stream().collect(Collectors.toMap(saldo -> saldo.IdPortafolio + "-" + saldo.IdMoneda, saldo -> saldo.Monto));

	List<OtrosSaldos> saldosRetenidos = sql.query(con, SQL_SELECT_SALDOS_RETENIDOS, paramsOtrosSaldos, OtrosSaldos.class);
	Map<Object, BigDecimal> retenidosPorCuenta = saldosRetenidos.stream().collect(Collectors.toMap(saldo -> saldo.IdPortafolio + "-" + saldo.IdMoneda, saldo -> saldo.Monto));

	List<OtrosSaldos> saldosSimultaneas = sql.query(con, SQL_SELECT_SALDO_A_PLAZO_SIMULTANEAS, paramsOtrosSaldos, OtrosSaldos.class);
	Map<Object, BigDecimal> simultaneasPorCuenta = saldosSimultaneas.stream().collect(Collectors.toMap(saldo -> saldo.IdPortafolio + "-" + saldo.IdMoneda, saldo -> saldo.Monto));

	//  MoneyMarket

	// ----------------------------------------------------------------------------------------
	// CALCULO DE MONEY MARKET
	HashMap<String, Object> parmsInstrMoney = new HashMap<>();
	List<ValorCuota> valoresCuota = new ArrayList<ValorCuota>();
	HashMap<String, Object> params = new HashMap<String, Object>();
	List<CantidadCustodia> cantidad = new ArrayList<CantidadCustodia>();

	// Obtiene los instrumentos marcados como MoneyMarket
	List<InstrumentosMoneyMarket> instrumentoMoney = sql.query(con, SQL_SELECT_INSTRUMENTOS_MONEY_MARKET, parmsInstrMoney, InstrumentosMoneyMarket.class);
	List<String> nemotecnicos = instrumentoMoney.stream().filter(s -> s.IdInstrumento != null).map(s -> s.Nemotecnico).collect(Collectors.toList());

	List<Long> IdPortafolio = datosProceso.parallelStream().map(dato -> dato.IdPortafolio).collect(Collectors.toList());
	HashMap<String, Object> valoresCuot = null;
	Map<Long, List<CantidadCustodia>> cantidadesAgrupadas = new HashMap<Long, List<CantidadCustodia>>();

	// No calcular cuando el proceso sea masivo, sino se va a la B
	if (nemotecnicos.size() > 0 && IdPortafolio.size() < 1000) {

	    // Obtiene valor cuota de nemotecnicos marcados para MoneyMarket
	    params.put("IdEmpresa", in.p_IdEmpresa);
	    params.put("FechaConsulta", in.FechaSistema);
	    params.put("IdSerieFondo", nemotecnicos);
	    valoresCuota = sql.query(con, SQL_SELECT_VALOR_CUOTA, params, ValorCuota.class);

	    // Obtiene el total custodia por portafolio, monedainstrumento y nemotecnico
	    params.clear();

	    params.put("Nemotecnico", nemotecnicos);
	    cantidad = sql.query(con, SQL_SELECT_CANTIDAD_CUSTODIA, params, CantidadCustodia.class);

	    valoresCuot = Utils.List2Hash(valoresCuota, "IdSerieFondo");
	    cantidadesAgrupadas = cantidad.stream().collect(Collectors.groupingBy(cant -> cant.IdPortafolio));
	}
	CalcularParidad servicioCalculaParidad = new CalcularParidad();
	CalcularParidad.CalcularParidadEntrada entradaCalcularParidad = new CalcularParidad.CalcularParidadEntrada();
	entradaCalcularParidad.p_IdEmpresa=in.p_IdEmpresa;
	entradaCalcularParidad.p_IdTrabajo=idTrabajo;
	List<CalcularParidad.CalculoEntrada> listaParidadEntrada = new ArrayList<CalcularParidad.CalculoEntrada>();
	HashMap<Long, BigDecimal> paridadMontoMoneyMarket = new HashMap<Long, BigDecimal>();
	String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");

		for (ClientesPagarCobrar dataCliente : datosClientes) {
			BigDecimal montoMoneyMarket = BigDecimal.ZERO;
			// Calcula MoneyMarket General
			List<CantidadCustodia> cantidades = cantidadesAgrupadas.get(dataCliente.IdPortafolio);
			if (cantidades != null) {
				for (CantidadCustodia reg : cantidades) {
					ValorCuota val = (ValorCuota) valoresCuot.get(reg.Nemotecnico);
					if (val != null) {
						montoMoneyMarket = montoMoneyMarket.add(valoresCuot == null ? BigDecimal.ZERO
								: reg.CantidadCustodia.multiply(val.ValorCuota, MathContext.DECIMAL128));
					}
				}
			}
			dataCliente.MoneyMarket = montoMoneyMarket;

	    // Si la moneda es distinta a la moneda sistema (CLP) se hace convergencia del
	    // MoneyMarket Calculado anteriormente a la moneda que corresponde
	    if (!dataCliente.IdMoneda.equalsIgnoreCase(monedaSistema)) {
		CalculoEntrada calculoEntrada = new CalculoEntrada();
		calculoEntrada.IdTrabajo = idTrabajo;
		calculoEntrada.IdInterno = dataCliente.IdCuentaInversion;

		calculoEntrada.Fecha = in.FechaSistema;

		calculoEntrada.IdMonedaOrigen = monedaSistema;
		calculoEntrada.IdMonedaDestino = dataCliente.IdMoneda; // MONDEA A TRANSFORMAR
		calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
		calculoEntrada.IdUso = "";
		calculoEntrada.MontoOrigen = dataCliente.MoneyMarket;

		listaParidadEntrada.add(calculoEntrada);
	    }
	}
	
	// con el listado completo se van a buscar todas juntas
	if (!listaParidadEntrada.isEmpty()) {
	    entradaCalcularParidad.p_CalculosEntrada = listaParidadEntrada;
	    CalcularParidadSalida paridadCargo = servicioCalculaParidad.logica(con, entradaCalcularParidad);

	    for (CalculoSalida paridadTotal : paridadCargo.p_out_CalculosSalida) {
		paridadMontoMoneyMarket.put(paridadTotal.IdInterno, paridadTotal.MontoDestino);
	    }
	}
	//
	datosClientes.forEach(dataCliente -> {
	    BigDecimal montoMoneyMarket = paridadMontoMoneyMarket.get(dataCliente.IdCuentaInversion);

	    dataCliente.MoneyMarket = montoMoneyMarket != null ? montoMoneyMarket : dataCliente.MoneyMarket;
	});

	// TODO Pacto

	// se borra lo usado
	SQL.exec(con, SQL_CLEAN_DELETE.replace(":IdTrabajo", idTrabajo + ""));

	// setear montos
	datosClientes.parallelStream().forEach(cuentaInv -> {
	    cuentaInv.Garantias = garantiasPorCuenta.getOrDefault(cuentaInv.IdPortafolio + "-" + cuentaInv.IdMoneda, BigDecimal.ZERO);
	    cuentaInv.SaldoRetenido = retenidosPorCuenta.getOrDefault(cuentaInv.IdPortafolio + "-" + cuentaInv.IdMoneda, BigDecimal.ZERO);
	    cuentaInv.Simultanea = simultaneasPorCuenta.getOrDefault(cuentaInv.IdPortafolio + "-" + cuentaInv.IdMoneda, BigDecimal.ZERO);
	});

		datosClientes = datosClientes.stream().filter(cuentaInv -> { // limpiando cosas con todos los saldos en cero
			boolean saldosNoEsCero = cuentaInv.Saldo.compareTo(BigDecimal.ZERO) != 0;
			boolean retenidoNoEsCero = cuentaInv.SaldoRetenido.compareTo(BigDecimal.ZERO) != 0;
			boolean garantiaNoEsCero = cuentaInv.Garantias.compareTo(BigDecimal.ZERO) != 0;
			boolean simultaneaNoEsCero = cuentaInv.Simultanea.compareTo(BigDecimal.ZERO) != 0;
			boolean moneyMarketNoEsCero = cuentaInv.MoneyMarket.compareTo(BigDecimal.ZERO) != 0;
			boolean pactoNoEsCero = cuentaInv.Pacto.compareTo(BigDecimal.ZERO) != 0;
			return saldosNoEsCero || retenidoNoEsCero || garantiaNoEsCero || simultaneaNoEsCero || moneyMarketNoEsCero || pactoNoEsCero;
		}).filter(cuentaInv -> {
			// limpiando cuentas inversion en dia 0
			Date fechaUnDiaAntes = Fecha.toDate(in.FechaSistema);
			Calendar c = Calendar.getInstance();
			c.setTime(fechaUnDiaAntes);
			c.add(Calendar.DATE, 0);
			fechaUnDiaAntes = c.getTime();
			return fechaUnDiaAntes.after(cuentaInv.FechaEfectivaMovimiento);
		}).collect(Collectors.toList());

	long fin = System.currentTimeMillis();
	logger.info("FIN::GenerarArchivoClientePorPagarYCobrar", fin-inicio);
	
	if (in.EsBusqueda.equals("S")) {
	    out.P_OUT_RESULTADO = datosClientes;
	    return out;

	} else {
	    out.P_OUT_RESULTADO = null;

	    String rutaArchivo = "";
	    String nombreArchivo = "";
	    String rutaCTA = "";
	    String rutaCompleta = ""; // ruta + nombre archivo - se usa para el servidor

	    // buscamos configuracion

	    if (datosClientes.isEmpty() || datosClientes == null) {
		out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_DATA_REPORTE", null, con, in.p_IdEmpresa));
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		return out;
	    }

	    params.clear();
	    params.put("IdEmpresa", in.p_IdEmpresa);
	    List<Configuracion> lstConfiguracion = sql.query(con, SQL_SELECT_CONFIGURACION, params, Configuracion.class);

	    if (!lstConfiguracion.isEmpty()) {
		for (Configuracion conf : lstConfiguracion) {
		    rutaArchivo = conf.RutaArchivo != null ? conf.RutaArchivo : "";
		    rutaCTA = conf.RutaCta != null ? conf.RutaCta : "";
		}
	    }

			if (rutaArchivo.equalsIgnoreCase("") || rutaCTA.equalsIgnoreCase("")) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR,
						"FALTA_CONFIGURACION_RUTA_ARCHIVO", null, con, in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				return out;
			}
			
			/*** TODO RUTA ARCHIVO ****/
			nombreArchivo = generarNombreArchivo("ClientesPorPagarYCobrar", "xls");
			//rutaArchivo = "C:\\Users\\nrojas_ext\\Documents\\"; //SOLO PARA local
			rutaCompleta = rutaArchivo + nombreArchivo;
			rutaCTA = rutaCTA + nombreArchivo; 
			out.P_OUT_RUTAARCHIVO = rutaCTA; 
			
			// creamos excel
			if (!generarArchivo(con, in, rutaCompleta, datosClientes)) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_GENERAR_ARCHIVO", null,con, in.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
				return out;
			}
			out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
			return out;
		}
	}

    private Map<String, BigDecimal> calcularTotalesPorRangos(List<ClientesPagarCobrar> listado, EntradaGenerarArchivoClientesPorPagarYCobrar in) throws ParseException {
	logger.info("INICIO::GenerarArchivoClientePorPagarYCobrar.calcularTotalesPorRangos");
	long ini = System.currentTimeMillis();
	List<ClientesPagarCobrar> listadoNegativo = listado.parallelStream().filter(cliente -> cliente.Saldo.signum() == -1).collect(Collectors.toList());
	List<ClientesPagarCobrar> listadoPositivo = listado.parallelStream().filter(cliente -> cliente.Saldo.signum() == 1).collect(Collectors.toList());

	Map<String, BigDecimal> totales = new HashMap<String, BigDecimal>();
	totales.put("totalPorPagar", listadoNegativo.stream().map(cliente -> cliente.Saldo).reduce(BigDecimal.ZERO, BigDecimal::add));
	totales.put("totalPorCobrar", listadoPositivo.stream().map(cliente -> cliente.Saldo).reduce(BigDecimal.ZERO, BigDecimal::add));

	TotalesPorRango totalPagar = new TotalesPorRango();

	totalPagar = sumarRango(in, listadoNegativo, totalPagar);
	totales.put("porPagar1a7", totalPagar.rango1a7);
	totales.put("porPagar8a9", totalPagar.rango8y9);
	totales.put("porPagar10a30", totalPagar.rango10a30);
	totales.put("porPagar31oMas", totalPagar.rango31oMas);

	TotalesPorRango totalCobrar = new TotalesPorRango();

		totalCobrar = sumarRango(in, listadoPositivo, totalCobrar);
		totales.put("porCobrar1a7", totalCobrar.rango1a7);
		totales.put("porCobrar8a9", totalCobrar.rango8y9);
		totales.put("porCobrar10a30", totalCobrar.rango10a30);
		totales.put("porCobrar31oMas", totalCobrar.rango31oMas);

		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarArchivoClientePorPagarYCobrar.calcularTotalesPorRangos", fin - ini);
		return totales;
	}

	private TotalesPorRango sumarRango(EntradaGenerarArchivoClientesPorPagarYCobrar in,	List<ClientesPagarCobrar> listado, TotalesPorRango totales) throws ParseException {
		logger.info("INICIO::GenerarArchivoClientePorPagarYCobrar.sumarRango");
		long ini = System.currentTimeMillis();

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date fechaDesde = formatter.parse(in.FechaSistema);
		long diff;
		for (ClientesPagarCobrar cliente : listado) {

			diff = fechaDesde.getTime() - cliente.FechaEfectivaMovimiento.getTime();
			TimeUnit time = TimeUnit.DAYS;
			long diferencia = time.convert(diff, TimeUnit.MILLISECONDS);

			if (diferencia >= 0 && diferencia <= 7) {
				totales.rango1a7 = totales.rango1a7.add(cliente.Saldo);
			} else if (8 <= diferencia && diferencia <= 9) {
				totales.rango8y9 = totales.rango8y9.add(cliente.Saldo);
			} else if (10 <= diferencia && diferencia <= 30) {
				totales.rango10a30 = totales.rango10a30.add(cliente.Saldo);
			} else if (31 <= diferencia) {
				totales.rango31oMas = totales.rango31oMas.add(cliente.Saldo);
			} else {
				logger.error("Saldos antes de la fecha de consulta");
			}
		}
		long fin = System.currentTimeMillis();
		logger.info("FIN::GenerarArchivoClientePorPagarYCobrar.sumarRango", fin - ini);

		return totales;

	}

	public boolean generarArchivo(Connection con, EntradaGenerarArchivoClientesPorPagarYCobrar in, String rutaCompleta,	List<ClientesPagarCobrar> datosClientes)
			throws FileNotFoundException, IOException, EncryptedDocumentException, InvalidFormatException {
		long ini = System.currentTimeMillis();
		logger.info("INICIO::GenerarArchivoClientePorPagarYCobrar.generarArchivo");
		boolean generarArchivo = false;
		try {

	    try (FileOutputStream outputStream = new FileOutputStream(rutaCompleta)) {

		Workbook archivoExcel = new HSSFWorkbook();
		Sheet hoja = archivoExcel.createSheet("Datos");

		/************************************/
		/** Formatos **/
		/************************************/
		hoja.setColumnWidth(0, 6000);
		hoja.setColumnWidth(1, 8000);
		hoja.setColumnWidth(2, 2500);
		hoja.setColumnWidth(3, 7500);
		hoja.setColumnWidth(4, 2300);
		hoja.setColumnWidth(5, 6000);
		hoja.setColumnWidth(6, 4000);
		hoja.setColumnWidth(7, 4000);
		hoja.setColumnWidth(8, 4000);
		hoja.setColumnWidth(9, 4000);
		hoja.setColumnWidth(10,4000 );
		hoja.setColumnWidth(11, 7000);
		hoja.setColumnWidth(12, 6500);
		hoja.setColumnWidth(13, 500);
		hoja.setColumnWidth(14, 500);
		
		CellStyle estiloFecha = archivoExcel.createCellStyle();
		CreationHelper createHelper = archivoExcel.getCreationHelper();
		estiloFecha.setDataFormat(createHelper.createDataFormat().getFormat("dd-mm-yyyy hh:mm:ss"));

		CellStyle estiloMoneda = archivoExcel.createCellStyle();
		DataFormat df = archivoExcel.createDataFormat();
		estiloMoneda.setDataFormat(df.getFormat("_ $* #,##0_ ;_ $* -#,##0_ ;_ $* \"-\"??_ ;_ @_ "));

		Font fuenteNegrita = archivoExcel.createFont();
		fuenteNegrita.setBold(true);
		CellStyle estiloMonedaNegrita = archivoExcel.createCellStyle();
		estiloMonedaNegrita.setDataFormat(df.getFormat("_ $* #,##0_ ;_ $* -#,##0_ ;_ $* \"-\"??_ ;_ @_ "));
		estiloMonedaNegrita.setFont(fuenteNegrita);

		CellStyle estiloNegrita = archivoExcel.createCellStyle();
		estiloNegrita.setFont(fuenteNegrita);
		estiloNegrita.setAlignment(CellStyle.ALIGN_CENTER);

		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		CellStyle estiloEncabezado = archivoExcel.createCellStyle();
		Font fuenteEncabezado = archivoExcel.createFont();
		fuenteEncabezado.setBold(true);
		fuenteEncabezado.setColor(IndexedColors.WHITE.getIndex());
		estiloEncabezado.setFont(fuenteEncabezado);
		estiloEncabezado.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		estiloEncabezado.setFillPattern(CellStyle.SOLID_FOREGROUND);
		estiloEncabezado.setAlignment(CellStyle.ALIGN_CENTER);
		estiloEncabezado.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		estiloEncabezado.setWrapText(true);

		/************************************/
		/** Encabezado **/
		/************************************/
		HashMap<String, String> codigo = new HashMap<String, String>();
		int rowCount = 0;
		int columnCount = 0;

		Row fila = hoja.createRow(rowCount);
		Cell celda = fila.createCell(columnCount);

		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_DOCUMENTO_IDENTIDAD", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_PORTAFOLIO", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_PAGAR_COBRAR_RELACION", codigo));
		celda.setCellStyle(estiloEncabezado);
		
		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_NOMBRE_CLIENTE", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_MONEDA", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_SALDO_DISPONIBLE", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_SALDO_RETENIDO", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_PAGAR_COBRAR_MONEYMARKET", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_PAGAR_COBRAR_GARANTIAS", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_PAGAR_COBRAR_SIMULTANEAS", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_PACTO", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_AGENTE", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_PAGAR_COBRAR_ULTIMAOPERACION", codigo));
		celda.setCellStyle(estiloEncabezado);

		/************************************/
		/** DETALLE **/
		/************************************/
		if (!rutaCompleta.isEmpty()) {
		    for (ClientesPagarCobrar datoCliente : datosClientes) {
			columnCount = 0;
			fila = hoja.createRow(++rowCount); // fila en donde se escribe

			celda = fila.createCell(columnCount);
			celda.setCellValue(datoCliente.DocumentoIdentidad);

			celda = fila.createCell(++columnCount);
			celda.setCellValue(datoCliente.CodigoProposito);

			celda = fila.createCell(++columnCount);
			celda.setCellValue(datoCliente.Relacion);
			
			celda = fila.createCell(++columnCount);
			celda.setCellValue(datoCliente.NombreCliente);

			celda = fila.createCell(++columnCount);
			celda.setCellValue(datoCliente.IdMoneda);

			celda = fila.createCell(++columnCount);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(datoCliente.Saldo.doubleValue());
			celda.setCellStyle(estiloMoneda);

			celda = fila.createCell(++columnCount);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(datoCliente.SaldoRetenido.doubleValue());
			celda.setCellStyle(estiloMoneda);

			celda = fila.createCell(++columnCount);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(datoCliente.MoneyMarket.doubleValue());
			celda.setCellStyle(estiloMoneda);

			celda = fila.createCell(++columnCount);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(datoCliente.Garantias.doubleValue());
			celda.setCellStyle(estiloMoneda);

			celda = fila.createCell(++columnCount);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(datoCliente.Simultanea.doubleValue());
			celda.setCellStyle(estiloMoneda);

			celda = fila.createCell(++columnCount);
			celda.setCellType(Cell.CELL_TYPE_NUMERIC);
			celda.setCellValue(datoCliente.Pacto.doubleValue());
			celda.setCellStyle(estiloMoneda);

			celda = fila.createCell(++columnCount);
			celda.setCellValue(datoCliente.NombreAgenteCliente);
			celda.setCellType(Cell.CELL_TYPE_STRING);

						if (dateFormat.format(datoCliente.FechaEfectivaMovimiento).equals("01-01-0001 00:00:00")) {
							celda = fila.createCell(++columnCount);
							celda.setCellValue("");
						} else {
							celda = fila.createCell(++columnCount);
							celda.setCellStyle(estiloFecha);
							celda.setCellValue(datoCliente.FechaEfectivaMovimiento);

						}
					}
				}
				
				/** RESUMEN **/
				// tratando de sumar por tramos
				List<ClientesPagarCobrar> noRelacionados = datosClientes.parallelStream().filter(cuenta -> "".equals(cuenta.Relacion)).collect(Collectors.toList());
				List<ClientesPagarCobrar> relacionados = datosClientes.parallelStream().filter(cuenta -> !"".equals(cuenta.Relacion)).collect(Collectors.toList());
				Map<String, BigDecimal> totalesNoRelacionados = calcularTotalesPorRangos(noRelacionados, in);
				Map<String, BigDecimal> totalesRelacionados = calcularTotalesPorRangos(relacionados, in);

		Sheet hojaResumen = archivoExcel.createSheet("Resumen");
		hojaResumen.setColumnWidth(0, 8000);
		hojaResumen.setColumnWidth(1, 9000);
		hojaResumen.setColumnWidth(2, 9000);

		rowCount = 0;
		columnCount = 0;

		/***  RELACIONADOS ***/
		fila = hojaResumen.createRow(rowCount);
		celda = fila.createCell(columnCount);

		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_FECHA_DESDE", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(in.FechaSistema);

		rowCount = 2;
		columnCount = -1;
		fila = hojaResumen.createRow(++rowCount);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_TOTAL_RELACIONADO", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_POR_PAGAR", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_POR_COBRAR", codigo));
		celda.setCellStyle(estiloEncabezado);

		fila = hojaResumen.createRow(++rowCount);
		columnCount = -1;

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_1_A_7_DIAS", codigo));
		celda.setCellStyle(estiloNegrita);

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesRelacionados.get("porPagar1a7").doubleValue());

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesRelacionados.get("porCobrar1a7").doubleValue());

		fila = hojaResumen.createRow(++rowCount);
		columnCount = -1;

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_8_y_9_DIAS", codigo));
		celda.setCellStyle(estiloNegrita);

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesRelacionados.get("porPagar8a9").doubleValue());

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesRelacionados.get("porCobrar8a9").doubleValue());

		fila = hojaResumen.createRow(++rowCount);
		columnCount = -1;

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_10_A_30_DIAS", codigo));
		celda.setCellStyle(estiloNegrita);

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesRelacionados.get("porPagar10a30").doubleValue());

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesRelacionados.get("porCobrar10a30").doubleValue());

		fila = hojaResumen.createRow(++rowCount);
		columnCount = -1;

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_MAS_DE_30_DIAS", codigo));
		celda.setCellStyle(estiloNegrita);

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesRelacionados.get("porPagar31oMas").doubleValue());

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesRelacionados.get("porCobrar31oMas").doubleValue());

		fila = hojaResumen.createRow(++rowCount);
		columnCount = -1;

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_TOTAL", codigo));
		celda.setCellStyle(estiloNegrita);

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMonedaNegrita);
		celda.setCellValue(totalesRelacionados.get("totalPorPagar").doubleValue());

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMonedaNegrita);
		celda.setCellValue(totalesRelacionados.get("totalPorCobrar").doubleValue());

		/***  NO RELACIONADO ***/
		rowCount = 11;
		columnCount = -1;
		fila = hojaResumen.createRow(++rowCount);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_TOTAL_NO_RELACIONADO", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_POR_PAGAR", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_POR_COBRAR", codigo));
		celda.setCellStyle(estiloEncabezado);

		fila = hojaResumen.createRow(++rowCount);
		columnCount = -1;

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_1_A_7_DIAS", codigo));
		celda.setCellStyle(estiloNegrita);

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesNoRelacionados.get("porPagar1a7").doubleValue());

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesNoRelacionados.get("porCobrar1a7").doubleValue());

		fila = hojaResumen.createRow(++rowCount);
		columnCount = -1;

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_8_y_9_DIAS", codigo));
		celda.setCellStyle(estiloNegrita);

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesNoRelacionados.get("porPagar8a9").doubleValue());

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesNoRelacionados.get("porCobrar8a9").doubleValue());

		fila = hojaResumen.createRow(++rowCount);
		columnCount = -1;

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_10_A_30_DIAS", codigo));
		celda.setCellStyle(estiloNegrita);

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesNoRelacionados.get("porPagar10a30").doubleValue());

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesNoRelacionados.get("porCobrar10a30").doubleValue());

		fila = hojaResumen.createRow(++rowCount);
		columnCount = -1;

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_MAS_DE_30_DIAS", codigo));
		celda.setCellStyle(estiloNegrita);

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesNoRelacionados.get("porPagar31oMas").doubleValue());

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		celda.setCellValue(totalesNoRelacionados.get("porCobrar31oMas").doubleValue());

		fila = hojaResumen.createRow(++rowCount);
		columnCount = -1;

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_TOTAL", codigo));
		celda.setCellStyle(estiloNegrita);

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMonedaNegrita);
		celda.setCellValue(totalesNoRelacionados.get("totalPorPagar").doubleValue());

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMonedaNegrita);
		celda.setCellValue(totalesNoRelacionados.get("totalPorCobrar").doubleValue());

		/***  TOTAL GENERAL ****/
		rowCount = 20;
		columnCount = -1;
		fila = hojaResumen.createRow(++rowCount);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_TOTAL_GENERAL", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_POR_PAGAR", codigo));
		celda.setCellStyle(estiloEncabezado);

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_POR_COBRAR", codigo));
		celda.setCellStyle(estiloEncabezado);

		fila = hojaResumen.createRow(++rowCount);
		columnCount = -1;

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_1_A_7_DIAS", codigo));
		celda.setCellStyle(estiloNegrita);

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		BigDecimal total1a7PorPagar = totalesNoRelacionados.get("porPagar1a7").add(totalesRelacionados.get("porPagar1a7"));
		celda.setCellValue(total1a7PorPagar.doubleValue());

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		BigDecimal total1a7PorCobrar = totalesNoRelacionados.get("porCobrar1a7").add(totalesRelacionados.get("porCobrar1a7"));
		celda.setCellValue(total1a7PorCobrar.doubleValue());

		fila = hojaResumen.createRow(++rowCount);
		columnCount = -1;

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_8_y_9_DIAS", codigo));
		celda.setCellStyle(estiloNegrita);

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		BigDecimal total8a9PorPagar = totalesNoRelacionados.get("porPagar8a9").add(totalesRelacionados.get("porPagar8a9"));
		celda.setCellValue(total8a9PorPagar.doubleValue());

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		BigDecimal total8a9PorCobrar = totalesNoRelacionados.get("porCobrar8a9").add(totalesRelacionados.get("porCobrar8a9"));
		celda.setCellValue(total8a9PorCobrar.doubleValue());

		fila = hojaResumen.createRow(++rowCount);
		columnCount = -1;

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_10_A_30_DIAS", codigo));
		celda.setCellStyle(estiloNegrita);

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		BigDecimal totalPorPagar10a30 = totalesNoRelacionados.get("porPagar10a30").add(totalesRelacionados.get("porPagar10a30"));
		celda.setCellValue(totalPorPagar10a30.doubleValue());

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		BigDecimal totalPorCobrar10a30 = totalesNoRelacionados.get("porCobrar10a30").add(totalesRelacionados.get("porCobrar10a30"));
		celda.setCellValue(totalPorCobrar10a30.doubleValue());

		fila = hojaResumen.createRow(++rowCount);
		columnCount = -1;

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_REPORTE_MAS_DE_30_DIAS", codigo));
		celda.setCellStyle(estiloNegrita);

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		BigDecimal totalPorPagar31oMas = totalesNoRelacionados.get("porPagar31oMas").add(totalesRelacionados.get("porPagar31oMas"));
		celda.setCellValue(totalPorPagar31oMas.doubleValue());

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMoneda);
		BigDecimal totaPorCobrar31oMas = totalesNoRelacionados.get("porCobrar31oMas").add(totalesRelacionados.get("porCobrar31oMas"));
		celda.setCellValue(totaPorCobrar31oMas.doubleValue());

		fila = hojaResumen.createRow(++rowCount);
		columnCount = -1;

		celda = fila.createCell(++columnCount);
		celda.setCellValue(Diccionario.getMensaje(con, in.p_IdEmpresa, "UI_TOTAL", codigo));
		celda.setCellStyle(estiloNegrita);

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMonedaNegrita);
		celda.setCellValue(totalesNoRelacionados.get("totalPorPagar").doubleValue());
		BigDecimal totalPorPagar = totalesNoRelacionados.get("totalPorPagar").add(totalesRelacionados.get("totalPorPagar"));
		celda.setCellValue(totalPorPagar.doubleValue());

		celda = fila.createCell(++columnCount);
		celda.setCellType(Cell.CELL_TYPE_NUMERIC);
		celda.setCellStyle(estiloMonedaNegrita);
		BigDecimal totalPorCobrar = totalesNoRelacionados.get("totalPorCobrar").add(totalesRelacionados.get("totalPorCobrar"));
		celda.setCellValue(totalPorCobrar.doubleValue());

		archivoExcel.write(outputStream);
		archivoExcel.close();
	    }
	    generarArchivo = true;
	} catch (Exception e) {
	    logger.error("Error en la creacion del reporte Clientes Por Pagar y Cobrar");
	    logger.error(e.getMessage());
//	    e.printStackTrace();
	    generarArchivo = false;
	}
	long fin = System.currentTimeMillis();
	logger.info("FIN::GenerarArchivoClientePorPagarYCobrar.generarArchivo", fin-ini);
	return generarArchivo;
    }

    private String generarNombreArchivo(String nombre, String extension) {
	String nombreArchivo = "";

	Date fechaActual = new Date();
	DateFormat formatoFecha = new SimpleDateFormat("yyyyMMdd_HHmmss");
	String fechaHora = formatoFecha.format(fechaActual);
	nombreArchivo = nombre + "_" + fechaHora + "." + extension;

	return nombreArchivo;
    }

    public static void main(String[] args) throws Exception {
	System.out.println("== INICIO PRUEBA LOCAL ==12.58");

	try (Connection con = PlataformaUtils.getDESAConnection()) {
	    Gson gson = Utils.getGsonBuilder();
	    String jsonExcel = "{\r\n"
	    	+ "    \"p_IdEmpresa\": 1,\r\n"
	    	+ "    \"p_IdUsuario\": 4,\r\n"
	    	+ "    \"p_IdModulo\": \"CTA\",\r\n"
	    	+ "    \"FechaSistema\": \"2020-10-28\",\r\n"
	    	+ "    \"EsBusqueda\": \"N\",\r\n"
	    	+ "    \"IdMoneda\": \"CLP\"\r\n"
	    	+ "}";


	    EntradaGenerarArchivoClientesPorPagarYCobrar Objin = gson.fromJson(jsonExcel, EntradaGenerarArchivoClientesPorPagarYCobrar.class);
	    GenerarArchivoClientesPorPagarYCobrar demo = new GenerarArchivoClientesPorPagarYCobrar();
	    System.out.println("ENTRADA:" + Objin.toString());

	    try {
		SalidaGenerarArchivoClientePorPagarYCobrar ObjOut = demo.logica(con, Objin);
		System.out.println(ObjOut.toString());
		con.rollback();
		// con.commit();
	    } catch (Exception er) {
		con.rollback();
		er.printStackTrace();
		// con.commit();
	    }

	    con.close();
	}

	System.out.println("== FIN PRUEBA LOCAL ==");
    }

}
