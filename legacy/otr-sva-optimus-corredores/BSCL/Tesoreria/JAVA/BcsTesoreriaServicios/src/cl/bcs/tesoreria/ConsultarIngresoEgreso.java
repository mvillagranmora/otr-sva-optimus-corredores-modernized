package cl.bcs.tesoreria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.SPT.ConsultarOperacionesCanalAgente.Portafolio;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo;
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConsultarIngresoEgreso implements JavaCall {

    private final String SQL_SELECT_INGRESO_EGRESO = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarIngresoEgreso_SelectIngresoEgreso.sql");
    private final String SQL_INSERT_ID_PORTAFOLIO_TABLA_PASO = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarIngresoEgreso_InsertIdPortafolioTablaPaso.sql");
    private final String SQL_INSERT_FULL_PORTAFOLIO_TABLA_PASO = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarIngresoEgreso_InsertFullIdPortafolioTablaPaso.sql");
    private final String SQL_DELETE_ID_PORTAFOLIO_TABLA_PASO = loadResourceToString("cl/bcs/tesoreria/sql/ConsultarIngresoEgreso_DeleteIdPortafolioTablaPaso.sql");
    static final Logger logger = LogManager.getLogger(ConsultarIngresoEgreso.class);

    public static class EntradaConsultarIngresoEgreso extends BcsPojo {
	public Long p_IdEmpresa;
	public Long p_IdUsuario;
	public String p_IdModulo;
	public Long p_IdEmpleadoConsulta;
	// filtros
	public Date p_FechaLiquidacionDesde;
	public Date p_FechaLiquidacionHasta;
	public String p_IdEstado;
	public String p_IdTipoMovimiento;
	public String p_IdConcepto;
	public String p_IdMoneda;
	public String p_EsIngresoAutomatico;
	public Long p_IdCaja;
	public Long p_IdPortafolioTitularPago;
	public Long p_IdPortafolio;

	public EntradaConsultarIngresoEgreso() {
	    this.p_IdEmpresa = 0L;
	    this.p_IdUsuario = 0L;
	    this.p_IdModulo = "";
	    this.p_IdEmpleadoConsulta = 0L;
	    this.p_IdEstado = "";
	    this.p_IdTipoMovimiento = "";
	    this.p_IdConcepto = "";
	    this.p_IdMoneda = "";
	    this.p_EsIngresoAutomatico = "";
	    this.p_IdCaja = 0L;
	    this.p_IdPortafolioTitularPago = 0L;
	    this.p_IdPortafolio = 0L;
	}
    }

    public static class IngresoEgreso extends BcsPojo {
	public Long IdIngresoEgreso;
	public Long IdEmpresa;
	public String NombreEmpresa;
	public String IdModulo;
	public Long IdUsuario;
	public String IdTipoMovimiento;
	public String NombreTipoMovimiento;
	public String IdConcepto;
	public String NombreConcepto;
	public Long IdCaja;
	public String NombreCaja;
	public String IdMoneda;
	public String NombreMoneda;
	public BigDecimal Monto;
	public BigDecimal MontoEgreso;
	public BigDecimal MontoIngreso;
	public BigDecimal MontoConSigno;
	public Date FechaLiquidacion;
	public Timestamp FechaIngresoRegistro;
	public Date FechaMovimiento;
	public String IdTipoPersona;
	public String NombreTipoSocioDeNegocio;
	public Long IdPersona;
	public Long IdCliente;
	public Long IdSocioDeNegocio;
	public String NombrePersona;
	public Long IdPortafolio;
	public String NombrePortafolio;
	public String IdTipoDocumentoIdentidad;
	public String DocumentoIdentidad;
	public String IdTipoPersonaTitularPago;
	public Long IdPersonaTitularPago;
	public String NombrePersonaTitularPago;
	public String MueveCuentaInversion;
	public Long IdPortafolioTitularPago;
	public String IdTipoDocumentoIdentidadTitularPago;
	public String DocumentoIdentidadTitularPago;
	public String NombrePortafolioTitularPago;
	public Long IdClienteTitularPago;
	public String SecuenciaAgrupacion;
	public Long SecuenciaRelacionada;
	public String Glosa;
	public Long IdEmpleadoDigitador;
	public Long IdSucursalEmpleadoDigitador;
	public String IdTipoComprobanteOrigen;
	public Long IdComprobanteOrigen;
	public Long FolioComprobanteDTE;
	public Timestamp FechaComprobanteOrigen;
	public Timestamp FechaLiquidacionComprobanteOrigen;
	public String IdTipoProducto;
	public String NombreTipoProducto;
	public String IdTipoOperacion;
	public String NombreTipoOperacion;
	public String IdBolsa;
	public String IdMonedaOperacion;
	public BigDecimal MontoOperacion;
	public Timestamp FechaParidad;
	public String IdTipoReajuste;
	public BigDecimal ValorParidad;
	public Long FolioCartera;
	public Long IdInstrumento;
	public String Nemotecnico;
	public String IdTipoInstrumento;
	public String IdClasificacionTipoInstrumento;
	public String EsBilateral;
	public String TitularPago;
	public Long IdUsuarioEmision;
	public String NombreUsuarioEmision;
	public Timestamp FechaHoraEmision;
	public Long IdUsuarioBloqueo;
	public Timestamp FechaHoraBloqueo;
	public Long IdCarga;
	public String IdEstado;
	public String NombreEstado;
	public Long IdComprobante;
	public Date FechaOperacion;
	public String IdCondicionLiquidacion;
	public Long IdOperacion;
	public String EsIngresoAutomatico;
	public String AgrupaComprobante;
	public String NeteaComprobante;
	public String NombreVentana;
	public String IdMedioDePago;
	public String NombreMedioDePago;
	public String IdModalidadDePago;
	public String NombreModalidadDePago;
	public Long IdCuentaBancoCorredora;
	public Long IdChequera;
	public String NombreBancoCorredora;
	public String NumeroCuentaBancariaCorredora;
	public String NombreTipoCuentaBancariaCorredora;
	public String NombreBancoSocio;
	public String NumeroCuentaBancariaSocio;
	public String NombreTipoCuentaBancariaSocio;
	public String NumeroSerieSocio;
	public Long NumeroDocumentoSocio;
	public Timestamp FechaDocumentoPagoSocio;
	public Long IdBancoSocio;
	public String IdEstadoComprobante;
	public String NombreEstadoComprobante;
	public Long IdUsuarioAnulacion;
	public Timestamp FechaHoraAnulacion;
	public String IdTipoEvento;
	public String Autorizacion;
	public String SolicitudAutorizacion;
	public String MotivoAutorizacion;
	public String IdEstadoAutorizacion;
	public String NombreEstadoAutorizacion;
	public String RegimenMatrizAgrupadorTitularPago;
	public String AliasBanco;
	public String IdTipoCanalIngreso;
	public String CuentaDCVOrigen;
	public String CuentaDCVDestino;
    }

    public static class SalidaConsultarIngresoEgreso extends BcsPojo {
	public Long P_OUT_ERRORESCONTADOR;
	public List<MensajeSalida> P_OUT_MENSAJES;
	public List<IngresoEgreso> P_OUT_RESULTADO;

	public SalidaConsultarIngresoEgreso() {
	    P_OUT_ERRORESCONTADOR = 0L;
	    P_OUT_MENSAJES = new ArrayList<>();
	    P_OUT_RESULTADO = new ArrayList<>();
	}

    }

    @Override
    public JsonObject call(Connection conn, JsonObject jin) throws Exception {
	Gson gson = Utils.getGsonBuilder();
	EntradaConsultarIngresoEgreso in = gson.fromJson(jin.toString(), EntradaConsultarIngresoEgreso.class);
	SalidaConsultarIngresoEgreso out = logica(conn, in);
	JsonParser parser = new JsonParser();
	JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
	return dataout;
    }

    private SalidaConsultarIngresoEgreso logica(Connection conn, EntradaConsultarIngresoEgreso entrada) throws Exception {
	logger.debug("INICIO::ConsultarIngresoEgreso");

	SalidaConsultarIngresoEgreso salida = new SalidaConsultarIngresoEgreso();
	SQL _sql = new SQL();
	Long idTrabajo = (long) (Math.random() * 10000000000L);

	// ingresa portafolios a tabla de paso de cliente correspondientes al ejecutivo
	insertTablaPaso(conn, entrada, idTrabajo);
	// logica consulta por cada filtro iria un tiene <nombreVariable>
	int ignorarIdEstado = 0;
	int ignorarIdTipoMovimiento = 0;
	int ignorarIdConcepto = 0;
	int ignorarIdMoneda = 0;
	int ignorarIngresoAutomatico = 0;
	int ignorarIdCaja = 0;
	int ignorarIdPortafolioTitular = 0;
	int ignorarIdPortafolio = 0;

	if ("".equals(entrada.p_IdEstado)) {
	    ignorarIdEstado = 1;
	}
	if ("".equals(entrada.p_IdTipoMovimiento)) {
	    ignorarIdTipoMovimiento = 1;
	}
	if ("".equals(entrada.p_IdConcepto)) {
	    ignorarIdConcepto = 1;
	}
	if ("".equals(entrada.p_IdMoneda)) {
	    ignorarIdMoneda = 1;
	}
	if ("".equals(entrada.p_EsIngresoAutomatico)) {
	    ignorarIngresoAutomatico = 1;
	}
	if (entrada.p_IdCaja <= 0L) {
	    ignorarIdCaja = 1;
	}
	if (entrada.p_IdPortafolioTitularPago <= 0L) {
	    ignorarIdPortafolioTitular = 1;
	}
	if (entrada.p_IdPortafolio <= 0L) {
	    ignorarIdPortafolio = 1;
	}

	HashMap<String, Object> params = new HashMap<>();
	params.put("IdTrabajo", idTrabajo);
	params.put("FechaDesde", entrada.p_FechaLiquidacionDesde);
	params.put("FechaHasta", entrada.p_FechaLiquidacionHasta);
	params.put("IgnorarEstado", ignorarIdEstado);
	params.put("IdEstado", entrada.p_IdEstado);
	params.put("IgnorarTipoMovimiento", ignorarIdTipoMovimiento);
	params.put("IdTipoMovimiento", entrada.p_IdTipoMovimiento);
	params.put("IgnorarIdConcepto", ignorarIdConcepto);
	params.put("IdConcepto", entrada.p_IdConcepto);
	params.put("IgnorarIdMoneda", ignorarIdMoneda);
	params.put("IdMoneda", entrada.p_IdMoneda);
	params.put("IgnorarIngresoAutomatico", ignorarIngresoAutomatico);
	params.put("IngresoAutomatico", entrada.p_EsIngresoAutomatico);
	params.put("IgnorarIdCaja", ignorarIdCaja);
	params.put("IdCaja", entrada.p_IdCaja);
	params.put("IgnorarIdPortafolioTitular", ignorarIdPortafolioTitular);
	params.put("IdPortafolioTitularPago", entrada.p_IdPortafolioTitularPago);
	params.put("IgnorarIdPortafolio", ignorarIdPortafolio);
	params.put("IdPortafolio", entrada.p_IdPortafolio);

	salida.P_OUT_RESULTADO = _sql.query(conn, SQL_SELECT_INGRESO_EGRESO, params, IngresoEgreso.class);

	borrarTablaPaso(conn,idTrabajo);
	logger.debug("FIN::ConsultarIngresoEgreso");
	return salida;
    }

    private void borrarTablaPaso(Connection conn, Long idTrabajo) throws SQLException {
	logger.debug("INICIO::ConsultarIngresoEgreso.borrarTablaPaso");
	SQL _sql = new SQL();
	Portafolio portafolio = new Portafolio();
	portafolio.IdPortafolio = null;
	portafolio.IdTrabajo = idTrabajo;
	List<Portafolio> idsPortafolioTablaPaso = new ArrayList<Portafolio>();
	idsPortafolioTablaPaso.add(portafolio);
	_sql.batch(conn, SQL_DELETE_ID_PORTAFOLIO_TABLA_PASO, idsPortafolioTablaPaso, Portafolio.class);
	logger.debug("FIN::ConsultarIngresoEgreso.borrarTablaPaso");
	
    }

    private void insertTablaPaso(Connection conn, EntradaConsultarIngresoEgreso entrada, Long idTrabajo) throws Exception {
	logger.debug("INICIO::ConsultarIngresoEgreso.insertTablaPaso");
	SQL _sql = new SQL();

	List<Long> listIdPortafolio = new ArrayList<Long>();
	if (entrada.p_IdPortafolio != 0) {
	    listIdPortafolio.add(entrada.p_IdPortafolio);
	    // se utiliza una tabla de paso para guardar idsPortafolio, optimiza consulta
	    List<Portafolio> idsPortafolioTablaPaso = listIdPortafolio.parallelStream().map(id -> {
		Portafolio portafolio = new Portafolio();
		portafolio.IdPortafolio = id;
		portafolio.IdTrabajo = idTrabajo;
		return portafolio;
	    }).collect(Collectors.toList());

	    _sql.batch(conn, SQL_INSERT_ID_PORTAFOLIO_TABLA_PASO, idsPortafolioTablaPaso, Portafolio.class);
	} else {
	    // Obtenemos los portafolios del ejecutivo
	    ObtenerPortafoliosEjecutivo portafoliosEjecutivo = new ObtenerPortafoliosEjecutivo();
	    ObtenerPortafoliosEjecutivo_entrada entradaConsulta = new ObtenerPortafoliosEjecutivo_entrada();
	    entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa;
	    entradaConsulta.p_IdUsuario = entrada.p_IdUsuario;
	    entradaConsulta.p_IdModulo = entrada.p_IdModulo;
	    entradaConsulta.p_IdEmpleadoConsulta = entrada.p_IdEmpleadoConsulta;
	    boolean restringeCartera = portafoliosEjecutivo.restringeCartera(conn, entradaConsulta);
	    if (restringeCartera) {
		listIdPortafolio = portafoliosEjecutivo.getPortafoliosEjecutivo(conn, entradaConsulta);
		List<Portafolio> idsPortafolioTablaPaso = listIdPortafolio.parallelStream().map(id -> {
		    Portafolio portafolio = new Portafolio();
		    portafolio.IdPortafolio = id;
		    portafolio.IdTrabajo = idTrabajo;
		    return portafolio;
		}).collect(Collectors.toList());

		_sql.batch(conn, SQL_INSERT_ID_PORTAFOLIO_TABLA_PASO, idsPortafolioTablaPaso, Portafolio.class);
	    } else {
		// no restringe cartera
		Portafolio portafolio = new Portafolio();
		portafolio.IdPortafolio = null;
		portafolio.IdTrabajo = idTrabajo;
		List<Portafolio> idsPortafolioTablaPaso = new ArrayList<Portafolio>();
		idsPortafolioTablaPaso.add(portafolio);

		_sql.batch(conn, SQL_INSERT_FULL_PORTAFOLIO_TABLA_PASO, idsPortafolioTablaPaso, Portafolio.class);

	    }

	}

    }

    public static void main(String[] args) throws SQLException {
	System.out.println("== TEST Consulta Ingreso Egreso ==");
	try (Connection con = PlataformaUtils.getDESAConnection()) {
	    String json ="{\r\n"
		    	+ "  \"p_IdEmpresa\": 1,\r\n"
		    	+ "  \"p_IdUsuario\": 21,\r\n"
		    	+ "  \"p_IdModulo\": \"TES\",\r\n"
		    	+ "  \"p_IdEmpleadoConsulta\": 0,\r\n"
		    	+ "  \"p_FechaLiquidacionDesde\": \"2021-04-13 00:00:00\",\r\n"
		    	+ "  \"p_FechaLiquidacionHasta\": \"2021-04-13 23:59:59\",\r\n"
		    	+ "  \"p_IdEstado\": \"\",\r\n"
		    	+ "  \"p_IdTipoMovimiento\": \"\",\r\n"
		    	+ "  \"p_IdConcepto\": \"\",\r\n"
		    	+ "  \"p_IdMoneda\": \"CLP\",\r\n"
		    	+ "  \"p_EsIngresoAutomatico\": \"\",\r\n"
		    	+ "  \"p_IdCaja\": 0,\r\n"
		    	+ "  \"p_IdPortafolioTitularPago\": 0,\r\n"
		    	+ "  \"p_IdPortafolio\": 0\r\n"
		    	+ "}\r\n";

	    EntradaConsultarIngresoEgreso entrada = Utils.getGsonBuilder().fromJson(json, EntradaConsultarIngresoEgreso.class);

	    System.out.println("== ENTRADA ==");
	    System.out.println(entrada);

	    try {
		SalidaConsultarIngresoEgreso salida = new ConsultarIngresoEgreso().logica(con, entrada);
		if (salida.P_OUT_ERRORESCONTADOR > 0) {
		    con.rollback();
		}
		System.out.println("== SALIDA ==");
		System.out.println(salida);
	    } catch (Exception er) {
		er.printStackTrace();
	    } finally {
		con.rollback();
	    }

	}
	System.out.println("== END ==");
    }

}
