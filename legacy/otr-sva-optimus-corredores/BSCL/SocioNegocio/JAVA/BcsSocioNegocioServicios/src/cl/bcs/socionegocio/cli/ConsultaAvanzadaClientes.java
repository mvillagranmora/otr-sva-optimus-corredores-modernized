package cl.bcs.socionegocio.cli;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import cl.bcs.custodia.ConsultarFlujoCustodia;
import cl.bcs.custodia.ConsultarFlujoCustodia.FlujoCustodiaDetallado;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.EntradaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.PortafolioAConsultar;
import cl.bcs.plataforma.pla.ConsultarFlujoCaja.SalidaConsultarFlujoCaja;
import cl.bcs.plataforma.pla.SaldoFlujoCaja.ObjFLUJOCAJA;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.cli.ObtenerPortafoliosEjecutivo.ObtenerPortafoliosEjecutivo_entrada;
import cl.bcs.socionegocio.pojo.AdvertenciaDetalle;
import cl.bcs.socionegocio.pojo.ErrorDetalle;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaView;
import cl.bcs.utils.JavaViewCache;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class ConsultaAvanzadaClientes implements JavaView {
	private static final Logger logger = LogManager.getLogger(ConsultaAvanzadaClientes.class);

	private final String SQL_SELECT_CLIENTES = loadResourceToString(
			"cl/bcs/socionegocio/cli/sql/ConsultaAvanzadaClientes_SelectClientes.sql");
	private final String SQL_INSERT_ID_CLIENTE_TABLA_PASO = loadResourceToString(
			"cl/bcs/socionegocio/cli/sql/ConsultaAvanzadaClientes_InsertIdClienteTablaPaso.sql");
	private final String SQL_SELECT_FECHA_ULTIMA_OPERACION_CLIENTES = loadResourceToString(
			"cl/bcs/socionegocio/cli/sql/ConsultaAvanzadaClientes_SelectFechaUltimaOpClientes.sql");
	private final String SQL_SELECT_FECHA_ULTIMA_MODIFICACION_CLIENTES = loadResourceToString(
			"cl/bcs/socionegocio/cli/sql/ConsultaAvanzadaClientes_SelectFechaUltimaModClientes.sql");
	private final String SQL_SELECT_CONTRATOS_FIRMADOS = loadResourceToString(
			"cl/bcs/socionegocio/cli/sql/ConsultaAvanzadaClientes_SelectContratosFirmados.sql");
	// private final String SQL_SELECT_CTA_CUSTODIO_PORTAFOLIO =
	// loadResourceToString("cl/bcs/socionegocio/cli/sql/ConsultaAvanzadaClientes_SelectCuentaCustodioPortafolio.sql");
	private final String SQL_SELECT_INSTRUMENTOS = loadResourceToString(
			"cl/bcs/socionegocio/cli/sql/ConsultaAvanzadaClientes_SelectInstrumentos.sql");
	private final String SQL_INSERT_FILTROS = loadResourceToString(
			"cl/bcs/socionegocio/cli/sql/ConsultaAvanzadaClientes_InsertFiltros.sql");
	private final String SQL_INSERT_ID_PORTAFOLIO = loadResourceToString(
			"cl/bcs/socionegocio/cli/sql/ConsultaAvanzadaClientes_InsertIdPortafolio.sql");

	private static final String FECHA_INICIO = "0001-01-01";
	private static final String TIMESTAMP_INICIO = "0001-01-01 00:00:00";
	private static final String SI = "S";
	private static final String NO = "N";
	private static final String MENOR_QUE = "MENOR_QUE";
	private static final String MAYOR_QUE = "MAYOR_QUE";

	public static class EntradaConsultaAvanzadaClientes {
		public Long p_IdEmpresa; // BIGINT, --ID Empresa destino
		public Long p_IdUsuario; // BIGINT, --ID del usuario que solicita el servicio
		public String p_IdModulo; // NVARCHAR(3), --ID del Modulo que lo invoca
		public Long p_IdEmpleadoConsulta; // BIGINT,

		public String p_Nombre; // NVARCHAR(5000) DEFAULT NULL,
		public String p_DocumentoIdentidad; // NVARCHAR(40) DEFAULT NULL,
		public String p_TipoDocumentoIdentidad; // NVARCHAR(40) DEFAULT NULL,
		public String p_IdTipoPersona; // NVARCHAR(2) DEFAULT NULL,
		public String p_IdTipoCliente; // NVARCHAR(40) DEFAULT NULL,
		public Long p_IdSegmentoCliente; // BIGINT DEFAULT NULL,
		public String p_IdEstadoCliente; // NVARCHAR(3) DEFAULT NULL,
		public String p_IdTipoBloqueoCliente; // NVARCHAR(40) DEFAULT NULL,
		public String p_IdTipoBloqueoPortafolio; // NVARCHAR(40) DEFAULT NULL,
		public Long p_IdEmpleadoFiltro; // BIGINT DEFAULT NULL,
		public String p_IdCategoriaInversionista; // NVARCHAR(40) DEFAULT NULL,
		public String p_IdPropositoPortafolio; // NVARCHAR(40) DEFAULT NULL,

		public String p_IdEstadoPortafolio; // NVARCHAR(3) DEFAULT NULL,
		public String p_IdPerfilComercial; // NVARCHAR(40) DEFAULT NULL,
		public String p_IdPerfilProductos; // NVARCHAR(40) DEFAULT NULL,
		public String p_IdPerfilCobros; // NVARCHAR(40) DEFAULT NULL,
		public String p_FechaNacimientoDesde; // NVARCHAR(10) DEFAULT '0001-01-01',
		public String p_FechaNacimientoHasta; // NVARCHAR(10) DEFAULT '0001-01-01',
		public String p_FechaCreacionDesde; // SECONDDATE DEFAULT '0001-01-01 00:00:00',
		public String p_FechaCreacionHasta; // SECONDDATE DEFAULT '0001-01-01 00:00:00',
		public String p_FechaDefuncionDesde; // SECONDDATE DEFAULT '0001-01-01 00:00:00',
		public String p_FechaDefuncionHasta; // SECONDDATE DEFAULT '0001-01-01 00:00:00',
		public String p_TipoEnrolamiento; // NVARCHAR(10) DEFAULT NULL,
		public String p_AliasPortafolio; // NVARCHAR(255) DEFAULT NULL,
		public String p_ClienteFinal; // NVARCHAR(1) DEFAULT NULL,
		public String p_Broker; // NVARCHAR(255) DEFAULT NULL,

		public Long p_IdPersonaBancoCustodio; // BIGINT DEFAULT NULL,
		public Long p_IdPersonaBrokerExtranjero; // BIGINT DEFAULT NULL,

		public String p_BuscaPorFlujoCaja = "N"; // NVARCHAR(1) DEFAULT 'N',
		public String p_UsoFlujoCaja; // NVARCHAR(40) DEFAULT NULL,
		public BigDecimal p_SaldoFlujoCaja; // DECIMAL(26,8) DEFAULT NULL,
		public String p_IdMonedaFlujoCaja; // NVARCHAR(3) DEFAULT NULL,
		public String p_CriterioFlujoCaja; // NVARCHAR(40) DEFAULT NULL, --MAYOR QUE, MENOR QUE

		public String p_BuscaPorFlujoCustodia = "N"; // NVARCHAR(1) DEFAULT 'N',
		public String p_TipoOperacionCustodia; // NVARCHAR(6) DEFAULT NULL,
		public BigDecimal p_SaldoFlujoCustodia; // DECIMAL(26,8) DEFAULT NULL,
		public String p_CriterioFlujoCustodia; // NVARCHAR(40) DEFAULT NULL, --MAYOR QUE, MENOR QUE
		public Long p_IdInstrumento; // BIGINT DEFAULT NULL,

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class FiltrosBusqueda {
		public Long IdEmpresa;
		public String ClienteFinal;
		public String DocumentoIdentidad;
		public String TipoDocumentoIdentidad;
		public String Nombre;
		public String AliasPortafolio;
		public String IdTipoPersona;
		public String IdTipoCliente;
		public Long IdPersonaBrokerExtranjero;
		public Long IdSegmentoCliente;
		public String IdEstadoCliente;
		public String IdCategoriaInversionista;
		public String IdTipoBloqueoCliente;
		public String IdTipoBloqueoPortafolio;
		public String IdPropositoPortafolio;
		public String IdEstadoPortafolio;
		public String IdPerfilComercial;
		public String IdPerfilProductos;
		public String IdPerfilCobros;
		public Long IdEmpleadoFiltro;
		public java.sql.Timestamp FechaCreacionDesde;
		public java.sql.Timestamp FechaCreacionHasta;
		public java.sql.Timestamp FechaDefuncionDesde;
		public java.sql.Timestamp FechaDefuncionHasta;
		public String FechaNacimientoDesde;
		public String FechaNacimientoHasta;
		public String TipoEnrolamiento;
		public int FiltroClienteFinal;
		public int FiltroDocumentoIdentidad;
		public int FiltroTipoDocumentoIdentidad;
		public int FiltroNombre;
		public int FiltroAliasPortafolio;
		public int FiltroIdTipoPersona;
		public int FiltroIdTipoCliente;
		public int FiltroIdPersonaBrokerExtranjero;
		public int FiltroIdSegmentoCliente;
		public int FiltroIdEstadoCliente;
		public int FiltroIdCategoriaInversionista;
		public int FiltroIdTipoBloqueoCliente;
		public int FiltroIdTipoBloqueoPortafolio;
		public int FiltroIdPropositoPortafolio;
		public int FiltroIdEstadoPortafolio;
		public int FiltroIdPerfilComercial;
		public int FiltroIdPerfilProductos;
		public int FiltroIdPerfilCobros;
		public int FiltroIdEmpleadoFiltro;
		public int FiltroFechaCreacionDesde;
		public int FiltroFechaCreacionHasta;
		public int FiltroFechaDefuncionDesde;
		public int FiltroFechaDefuncionHasta;
		public int FiltroFechaNacimientoDesde;
		public int FiltroFechaNacimientoHasta;
		public int FiltroTipoEnrolamiento;
		public String FiltroAreaCliente;

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class InformacionConsultaAvanzadaClientes extends BcsPojo {
		public String TipoDocumentoIdentidad; // NVARCHAR(40),
		public String DocumentoIdentidad; // NVARCHAR(40),
		public String DocumentoIdentidadRutCavali; // NVARCHAR(40),
		public String NombreCompleto; // NVARCHAR(5000),
		public Date FechaNacimiento; // DATE,
		public Timestamp FechaCreacion; // DATE,
		public Timestamp FechaModificacionCliente; // DATE,
		public Timestamp FechaModificacionPersona; // DATE,
		public Timestamp FechaModificacionPortafolio; // DATE,
		public Date FechaUltimaModificacion; // DATE,
		public Date FechaUltimaOperacion; // DATE,
		public Date FechaDefuncion; // DATE,
		public String IdTipoCliente; // NVARCHAR(40),
		public Long IdCliente; // BIGINT,
		public Long IdPortafolio; // BIGINT,
		public String AreaCliente;
		public String NombreTipoCliente; // NVARCHAR(255),
		public String ClienteFinal; // NVARCHAR(1),
		public String NombreCompletoBroker; // NVARCHAR(100),
		public String IdTipoPersona; // NVARCHAR(2),
		public String NombreTipoPersona; // NVARCHAR(255),
		public Long IdSegmentoCliente; // NVARCHAR(40),
		public String NombreSegmentoCliente; // NVARCHAR(255),
		public String DescripcionSegmentoCliente; // NVARCHAR(520),
		public Long IdEmpleadoEjecutivo; // BIGINT,
		public String CodigoEmpleadoEjecutivo; // NVARCHAR(40),
		public String NombreEmpleadoEjecutivo; // NVARCHAR(400),
		public String IdCategoriaInversionista; // NVARCHAR(40),
		public String NombreCategoriaInversionista; // NVARCHAR(100),
		public String IdEstadoPersona; // NVARCHAR(40),
		public String IdEstadoCliente; // NVARCHAR(40),
		public String IdEstadoPortafolio; // NVARCHAR(40),
		public String NombreEstadoPersona; // NVARCHAR(100),
		public String NombreEstadoCliente; // NVARCHAR(100),
		public String NombreEstadoPortafolio; // NVARCHAR(100),
		public String CodigoPortafolio; // NVARCHAR(100),
		public String DescripcionPortafolio; // NVARCHAR(255),
		public String PropositoPortafolio; // NVARCHAR(520),
		public String IdTipoBloqueoPersona; // NVARCHAR(40),
		public String NombreTipoBloqueoPersona; // NVARCHAR(100),
		public String IdTipoBloqueoCliente; // NVARCHAR(40),
		public String NombreTipoBloqueoCliente; // NVARCHAR(100),
		public String IdTipoBloqueoPortafolio; // NVARCHAR(40),
		public String NombreTipoBloqueoPortafolio; // NVARCHAR(100),
		public String IdPerfilComercial; // NVARCHAR(40),
		public String IdPerfilProductos; // NVARCHAR(40),
		public String IdPerfilCobros; // NVARCHAR(40),
		public String IdPerfilLimites; // NVARCHAR(40),
		public BigDecimal SaldoFlujoCaja; // DECIMAL(26,8),
		public String IdMonedaFlujoCaja; // NVARCHAR(3),
		public BigDecimal SaldoFlujoCustodia; // DECIMAL(28,8),
		public Long IdInstrumento; // BIGINT,
		public String IdMonedaInstrumento; // NVARCHAR(3),
		public String Nemotecnico; // NVARCHAR(40),
		public Long IdPersona; // BIGINT,
		public String TipoEnrolamiento; // NVARCHAR(10),
		public String ContratosFirmados; // NVARHCAR(1)
		public String IdTipoCanalIngresoCorredor; // NVARCHAR(3)
		
		public InformacionConsultaAvanzadaClientes() {
			this.FechaModificacionCliente = Timestamp.valueOf(TIMESTAMP_INICIO);
			this.FechaModificacionPersona = Timestamp.valueOf(TIMESTAMP_INICIO);
			this.FechaModificacionPortafolio = Timestamp.valueOf(TIMESTAMP_INICIO);
			this.FechaUltimaModificacion = Date.valueOf(FECHA_INICIO);
			this.FechaUltimaOperacion = Date.valueOf(FECHA_INICIO);
		}
	}

	public static class SalidaConsultaAvanzadaClientes implements Cloneable {
		public List<InformacionConsultaAvanzadaClientes> P_OUT_CLIENTES;
		public int P_OUT_ADVERTENCIACONTADOR;
		public int P_OUT_ERRORESCONTADOR;
		public List<AdvertenciaDetalle> P_OUT_ADVERTENCIADETALLE;
		public List<ErrorDetalle> P_OUT_ERRORESDETALLE;

		SalidaConsultaAvanzadaClientes() {
			P_OUT_CLIENTES = new ArrayList<>();
			P_OUT_ADVERTENCIACONTADOR = 0;
			P_OUT_ERRORESCONTADOR = 0;
			P_OUT_ADVERTENCIADETALLE = new ArrayList<>();
			P_OUT_ERRORESDETALLE = new ArrayList<>();
		}

		public Object clone() {
			try {
				return super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}

		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}

	public static class FlujoCustodiaPorPortafolioIdInstrumento {
		public Long IdPortafolio;
		public Long IdInstrumento;
		public BigDecimal Saldo;
		public String Nemotecnico;
	}

	public static class CuentaCustodioPortafolio extends BcsPojo {
		public Long IdPortafolio;
	}

	public static class Instrumento extends BcsPojo {
		public Long IdInstrumento;
		public String IdMoneda;
	}

	public static class Portafolio extends BcsPojo {
		public Long IdPortafolio;
	}

	public static class FechaSistemaCliente extends BcsPojo {
		public Long IdCliente;
		public Date FechaSistema;
	}

	public static class ClienteFirmaContrato extends BcsPojo {
		public Long IdCliente;
		public Long ContratosFirmados;
		public Long ContratosNoFirmados;
	}
	
	public static class ClienteTablaPaso extends BcsPojo {
		public Long IdCliente;
		public Long IdTrabajo;
	}

	@Trace
	public SalidaConsultaAvanzadaClientes logica(Connection con, EntradaConsultaAvanzadaClientes entrada)
			throws Exception {
		Long init = System.currentTimeMillis();
		long startTime = System.nanoTime();
		logger.info("Inicio servicio ConsultaAvanzadaClientes");

		SQL _sql = new SQL();
		SalidaConsultaAvanzadaClientes salida = new SalidaConsultaAvanzadaClientes();

		// implementar logica
		String v_FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,
				"PLATAFORMA", "PLA.FechaSistema");
		String v_IdMonedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa,
				"PLATAFORMA", "PLA.MonedaSistema");

		// se obtienen los portafolios de ejecutivo
		ObtenerPortafoliosEjecutivo portafoliosEjecutivo = new ObtenerPortafoliosEjecutivo();
		ObtenerPortafoliosEjecutivo_entrada entradaConsulta = new ObtenerPortafoliosEjecutivo_entrada();
		entradaConsulta.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsulta.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsulta.p_IdModulo = entrada.p_IdModulo;
		entradaConsulta.p_IdEmpleadoConsulta = entrada.p_IdEmpleadoConsulta;

		List<Long> listIdPortafolio = portafoliosEjecutivo.getPortafoliosEjecutivo(con, entradaConsulta);

		// se utiliza una tabla de paso para guardar idsPortafolio
		List<Portafolio> idsPortafolioTablaPaso = listIdPortafolio.parallelStream().map(id -> {
			Portafolio portafolioInsert = new Portafolio();
			portafolioInsert.IdPortafolio = id;
			return portafolioInsert;
		}).collect(Collectors.toList());

		_sql.batch(con, SQL_INSERT_ID_PORTAFOLIO, idsPortafolioTablaPaso, Portafolio.class);

		List<FiltrosBusqueda> filtros = new ArrayList<>();
		FiltrosBusqueda filtro = new FiltrosBusqueda();
		filtro.IdEmpresa = entrada.p_IdEmpresa;
		filtro.ClienteFinal = entrada.p_ClienteFinal;
		filtro.FiltroClienteFinal = entrada.p_ClienteFinal != null ? 0 : 1;
		filtro.DocumentoIdentidad = entrada.p_DocumentoIdentidad;
		filtro.FiltroDocumentoIdentidad = entrada.p_DocumentoIdentidad != null ? 0 : 1;
		filtro.TipoDocumentoIdentidad = entrada.p_TipoDocumentoIdentidad;
		filtro.FiltroTipoDocumentoIdentidad = entrada.p_TipoDocumentoIdentidad != null ? 0 : 1;
		filtro.Nombre = entrada.p_Nombre;
		filtro.FiltroNombre = entrada.p_Nombre != null ? 0 : 1;
		filtro.AliasPortafolio = entrada.p_AliasPortafolio;
		filtro.FiltroAliasPortafolio = entrada.p_AliasPortafolio != null ? 0 : 1;

		filtro.IdTipoPersona = entrada.p_IdTipoPersona;
		filtro.FiltroIdTipoPersona = entrada.p_IdTipoPersona != null ? 0 : 1;
		filtro.IdTipoCliente = entrada.p_IdTipoCliente != null ? entrada.p_IdTipoCliente : "";
		filtro.FiltroIdTipoCliente = entrada.p_IdTipoCliente != null ? 0 : 1;
		filtro.IdPersonaBrokerExtranjero = entrada.p_IdPersonaBrokerExtranjero != null
				? entrada.p_IdPersonaBrokerExtranjero
				: 0;
		filtro.FiltroIdPersonaBrokerExtranjero = entrada.p_IdPersonaBrokerExtranjero != null ? 0 : 1;
		filtro.IdSegmentoCliente = entrada.p_IdSegmentoCliente != null ? entrada.p_IdSegmentoCliente : 0;
		filtro.FiltroIdSegmentoCliente = entrada.p_IdSegmentoCliente != null ? 0 : 1;
		filtro.IdEstadoCliente = entrada.p_IdEstadoCliente != null ? entrada.p_IdEstadoCliente : "";
		filtro.FiltroIdEstadoCliente = entrada.p_IdEstadoCliente != null ? 0 : 1;
		filtro.IdCategoriaInversionista = entrada.p_IdCategoriaInversionista != null
				? entrada.p_IdCategoriaInversionista
				: "";
		filtro.FiltroIdCategoriaInversionista = entrada.p_IdCategoriaInversionista != null ? 0 : 1;
		filtro.IdTipoBloqueoCliente = entrada.p_IdTipoBloqueoCliente != null ? entrada.p_IdTipoBloqueoCliente : "";
		filtro.FiltroIdTipoBloqueoCliente = entrada.p_IdTipoBloqueoCliente != null ? 0 : 1;
		filtro.IdTipoBloqueoPortafolio = entrada.p_IdTipoBloqueoPortafolio != null ? entrada.p_IdTipoBloqueoPortafolio
				: "";
		filtro.FiltroIdTipoBloqueoPortafolio = entrada.p_IdTipoBloqueoPortafolio != null ? 0 : 1;
		filtro.IdPropositoPortafolio = entrada.p_IdPropositoPortafolio != null ? entrada.p_IdPropositoPortafolio : "";
		filtro.FiltroIdPropositoPortafolio = entrada.p_IdPropositoPortafolio != null ? 0 : 1;
		filtro.IdEstadoPortafolio = entrada.p_IdEstadoPortafolio != null ? entrada.p_IdEstadoPortafolio : "";
		filtro.FiltroIdEstadoPortafolio = entrada.p_IdEstadoPortafolio != null ? 0 : 1;
		filtro.IdPerfilComercial = entrada.p_IdPerfilComercial != null ? entrada.p_IdPerfilComercial : "";
		filtro.FiltroIdPerfilComercial = entrada.p_IdPerfilComercial != null ? 0 : 1;
		filtro.IdPerfilProductos = entrada.p_IdPerfilProductos != null ? entrada.p_IdPerfilProductos : "";
		filtro.FiltroIdPerfilProductos = entrada.p_IdPerfilProductos != null ? 0 : 1;
		filtro.IdPerfilCobros = entrada.p_IdPerfilCobros != null ? entrada.p_IdPerfilCobros : "";
		filtro.FiltroIdPerfilCobros = entrada.p_IdPerfilCobros != null ? 0 : 1;
		filtro.IdEmpleadoFiltro = entrada.p_IdEmpleadoFiltro != null ? entrada.p_IdEmpleadoFiltro : 0;
		filtro.FiltroIdEmpleadoFiltro = entrada.p_IdEmpleadoFiltro != null ? 0 : 1;
		filtro.FechaNacimientoDesde = entrada.p_FechaNacimientoDesde != null ? entrada.p_FechaNacimientoDesde
				: FECHA_INICIO;
		filtro.FiltroFechaNacimientoDesde = entrada.p_FechaNacimientoDesde != null ? 0 : 1;
		filtro.FechaNacimientoHasta = entrada.p_FechaNacimientoHasta != null ? entrada.p_FechaNacimientoHasta
				: FECHA_INICIO;
		filtro.FiltroFechaNacimientoHasta = entrada.p_FechaNacimientoHasta != null ? 0 : 1;
		filtro.FechaCreacionDesde = entrada.p_FechaCreacionDesde != null
				? Timestamp.valueOf(entrada.p_FechaCreacionDesde)
				: Timestamp.valueOf(TIMESTAMP_INICIO);
		filtro.FiltroFechaCreacionDesde = entrada.p_FechaCreacionDesde != null ? 0 : 1;
		filtro.FechaCreacionHasta = entrada.p_FechaCreacionHasta != null
				? Timestamp.valueOf(entrada.p_FechaCreacionHasta)
				: Timestamp.valueOf(TIMESTAMP_INICIO);
		filtro.FiltroFechaCreacionHasta = entrada.p_FechaCreacionHasta != null ? 0 : 1;
		filtro.FechaDefuncionDesde = entrada.p_FechaDefuncionDesde != null
				? Timestamp.valueOf(entrada.p_FechaDefuncionDesde)
				: Timestamp.valueOf(TIMESTAMP_INICIO);
		filtro.FiltroFechaDefuncionDesde = entrada.p_FechaDefuncionDesde != null ? 0 : 1;
		filtro.FechaDefuncionHasta = entrada.p_FechaDefuncionHasta != null
				? Timestamp.valueOf(entrada.p_FechaDefuncionHasta)
				: Timestamp.valueOf(TIMESTAMP_INICIO);
		filtro.FiltroFechaDefuncionHasta = entrada.p_FechaDefuncionHasta != null ? 0 : 1;
		filtro.TipoEnrolamiento = entrada.p_TipoEnrolamiento != null ? entrada.p_TipoEnrolamiento : "";
		filtro.FiltroTipoEnrolamiento = entrada.p_TipoEnrolamiento != null
				&& StringUtils.isNotEmpty(entrada.p_TipoEnrolamiento) ? 0 : 1;
		filtros.add(filtro);

		_sql.batch(con, SQL_INSERT_FILTROS, filtros, FiltrosBusqueda.class);

		logger.debug("Inicio SQL_SELECT_CLIENTES");
		startTime = System.nanoTime();
		HashMap<String, Object> params = new HashMap<>();
		List<InformacionConsultaAvanzadaClientes> informacionClientes = _sql.query(con, SQL_SELECT_CLIENTES, params,
				InformacionConsultaAvanzadaClientes.class);
		logger.debug("(" + (System.nanoTime() - startTime) / 1000000 + ") Fin SQL_SELECT_CLIENTES, COUNT: "
				+ informacionClientes.size());

		List<Long> idsPortafolio = informacionClientes.parallelStream().map(p -> p.IdPortafolio).distinct()
				.collect(Collectors.toList());

		HashMap<String, FlujoCustodiaPorPortafolioIdInstrumento> v_FlujoCustodia = new HashMap<>();
		HashMap<String, ObjFLUJOCAJA> v_out_FlujoCaja = new HashMap<>();
		HashMap<String, Instrumento> v_out_Instrumento = new HashMap<>();
		List<FlujoCustodiaPorPortafolioIdInstrumento> flujoCustodiaDetalladoSaldo = new ArrayList<>();
		if (SI.equals(entrada.p_BuscaPorFlujoCustodia)) {

			List<ConsultarFlujoCustodia.FlujoUso> p_Uso = new ArrayList<>();

			ConsultarFlujoCustodia.FlujoUso datosFlujoUso = new ConsultarFlujoCustodia.FlujoUso();
			datosFlujoUso.UsoTipoOperacion = entrada.p_TipoOperacionCustodia;
			datosFlujoUso.IdCliente = null;
			datosFlujoUso.IdPortafolio = null;
			datosFlujoUso.BusquedaEspecifica = "";
			datosFlujoUso.IdTipoInstrumentoEspecifico = null;

			p_Uso.add(datosFlujoUso);

			List<ConsultarFlujoCustodia.FlujoFiltrosBusqueda> p_FiltrosBusqueda = new ArrayList<>();
			ConsultarFlujoCustodia.FlujoFiltrosBusqueda datosFlujoFiltrosBusqueda = new ConsultarFlujoCustodia.FlujoFiltrosBusqueda();
			datosFlujoFiltrosBusqueda.IdClasificacionTipoInstrumento = null;
			datosFlujoFiltrosBusqueda.IdTipoInstrumento = null;
			datosFlujoFiltrosBusqueda.IdInstrumento = entrada.p_IdInstrumento;
			datosFlujoFiltrosBusqueda.IdEmisor = null;
			datosFlujoFiltrosBusqueda.FechaVencimiento = null;
			datosFlujoFiltrosBusqueda.IdTipoReajuste = null;
			datosFlujoFiltrosBusqueda.EsFungible = null;
			datosFlujoFiltrosBusqueda.IdBoveda = null;
			p_FiltrosBusqueda.add(datosFlujoFiltrosBusqueda);

			List<ConsultarFlujoCustodia.FlujoEsAgrupadoPor> p_EsAgrupadoPor = new ArrayList<>();
			ConsultarFlujoCustodia.FlujoEsAgrupadoPor datosFlujoEsAgrupadoPor = new ConsultarFlujoCustodia.FlujoEsAgrupadoPor();
			datosFlujoEsAgrupadoPor.ClasificacionTipoInstrumento = null;
			datosFlujoEsAgrupadoPor.TipoInstrumento = null;
			datosFlujoEsAgrupadoPor.Instrumento = SI;
			datosFlujoEsAgrupadoPor.Emisor = null;
			datosFlujoEsAgrupadoPor.Nemotecnico = null;
			datosFlujoEsAgrupadoPor.NemoSVS = null;
			datosFlujoEsAgrupadoPor.FechaEmisionInstrumento = null;
			datosFlujoEsAgrupadoPor.FechaVencimientoInstrumento = null;
			datosFlujoEsAgrupadoPor.FechaLiquidacionInstrumento = null;
			datosFlujoEsAgrupadoPor.TipoReajuste = null;
			datosFlujoEsAgrupadoPor.EsFungible = null;
			datosFlujoEsAgrupadoPor.CodigoIFRS = null;
			datosFlujoEsAgrupadoPor.Boveda = null;
			datosFlujoEsAgrupadoPor.Bolsa = null;
			datosFlujoEsAgrupadoPor.TipoOperacion = null;
			datosFlujoEsAgrupadoPor.Mesa = null;
			datosFlujoEsAgrupadoPor.FolioRegistro = null;
			datosFlujoEsAgrupadoPor.ItemOrigen = null;
			p_EsAgrupadoPor.add(datosFlujoEsAgrupadoPor);

			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa EMP = consultarEmpresaService.logica(con, entrada.p_IdEmpresa);

			ConsultarFlujoCustodia llamadoFlujoCustodia = new ConsultarFlujoCustodia();
			ConsultarFlujoCustodia.FlujoCustodiaEntrada entradaFlujoCustodia = new ConsultarFlujoCustodia.FlujoCustodiaEntrada();
			entradaFlujoCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaFlujoCustodia.p_IdUsuario = entrada.p_IdUsuario;
			entradaFlujoCustodia.p_IdModulo = entrada.p_IdModulo;
			entradaFlujoCustodia.p_IdPais = EMP.IdPais;
			entradaFlujoCustodia.p_FechaSaldo = v_FechaSistema;
			entradaFlujoCustodia.p_DiasProyeccion = 0;
			entradaFlujoCustodia.p_Uso = p_Uso;
			entradaFlujoCustodia.p_FiltrosBusqueda = p_FiltrosBusqueda;
			entradaFlujoCustodia.p_EsAgrupadoPor = p_EsAgrupadoPor;
			entradaFlujoCustodia.p_ValorizarFlujo = NO;
			// logger.debug(entradaFlujoCustodia);//TODO quitar
			ConsultarFlujoCustodia.FlujoCustodiaSalida salidaFlujoCustodia = llamadoFlujoCustodia.logica(con,
					entradaFlujoCustodia);
			List<FlujoCustodiaDetallado> flujoCustodiaDetallado = salidaFlujoCustodia.p_out_FlujoDetallado;

			flujoCustodiaDetallado = flujoCustodiaDetallado.parallelStream()
					.filter(s -> s.IdInstrumento.equals(entrada.p_IdInstrumento)).collect(Collectors.toList());

			FlujoCustodiaPorPortafolioIdInstrumento objetoInsertarFlujoCustodia;
			flujoCustodiaDetalladoSaldo = new ArrayList<>();
			for (FlujoCustodiaDetallado flujoCustodia : flujoCustodiaDetallado) {
				BigDecimal saldo = flujoCustodiaDetallado.stream()
						.filter(x -> x.IdPortafolio.equals(flujoCustodia.IdPortafolio)
								&& x.Nemotecnico.equals(flujoCustodia.Nemotecnico)
								&& x.IdInstrumento.equals(flujoCustodia.IdInstrumento))
						.map(y -> y.Cantidad).reduce(BigDecimal.ZERO, BigDecimal::add);
				// logger.debug("saldo obtenido: " + saldo);
				objetoInsertarFlujoCustodia = new FlujoCustodiaPorPortafolioIdInstrumento();
				objetoInsertarFlujoCustodia.IdPortafolio = flujoCustodia.IdPortafolio;
				objetoInsertarFlujoCustodia.IdInstrumento = flujoCustodia.IdInstrumento;
				objetoInsertarFlujoCustodia.Nemotecnico = flujoCustodia.Nemotecnico;
				objetoInsertarFlujoCustodia.Saldo = saldo;
				flujoCustodiaDetalladoSaldo.add(objetoInsertarFlujoCustodia);
			}

			for (FlujoCustodiaPorPortafolioIdInstrumento filtrado : flujoCustodiaDetalladoSaldo) {
				String keyFlujoCustodia = filtrado.IdPortafolio + "-" + filtrado.IdInstrumento;
				if (!v_FlujoCustodia.containsKey(keyFlujoCustodia)) {
					v_FlujoCustodia.put(keyFlujoCustodia, filtrado);
				}
			}
		}

		if (flujoCustodiaDetalladoSaldo.size() > 0) {
			List<Long> idsInstrumento = flujoCustodiaDetalladoSaldo.parallelStream().map(s -> s.IdInstrumento)
					.collect(Collectors.toList());
			List<Instrumento> instrumentos;
			params.clear();
			params.put("idsInstrumento", idsInstrumento);
			instrumentos = _sql.query(con, SQL_SELECT_INSTRUMENTOS, params, Instrumento.class);
			for (Instrumento instrumento : instrumentos) {
				String keyInstrumento = instrumento.IdInstrumento + "";
				v_out_Instrumento.put(keyInstrumento, instrumento);
			}
		}

		/*
		 * if (entrada.p_IdPersonaBancoCustodio != null &&
		 * entrada.p_IdPersonaBancoCustodio > 0) { params.clear();
		 * params.put("IdPersonaBancoCustodio", entrada.p_IdPersonaBancoCustodio);
		 * List<CuentaCustodioPortafolio> cuentaCustodioPortafolio = _sql.query(con,
		 * SQL_SELECT_CTA_CUSTODIO_PORTAFOLIO, params, CuentaCustodioPortafolio.class);
		 * }
		 */

		if (SI.equals(entrada.p_BuscaPorFlujoCaja)) {
			List<PortafolioAConsultar> portafoliosConsultar = idsPortafolio.parallelStream().map(idPortafolio -> {
				PortafolioAConsultar portafolio = new PortafolioAConsultar();
				portafolio.IdMoneda = entrada.p_IdMonedaFlujoCaja;
				portafolio.IdPortafolio = idPortafolio;
				return portafolio;
			}).collect(Collectors.toList());

			ConsultarFlujoCaja llamadoFlujoCaja = new ConsultarFlujoCaja();
			EntradaConsultarFlujoCaja entradaFlujoCaja = new EntradaConsultarFlujoCaja();
			entradaFlujoCaja.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaFlujoCaja.p_IdUsuario = entrada.p_IdUsuario;
			entradaFlujoCaja.p_IdModulo = entrada.p_IdModulo;
			entradaFlujoCaja.p_FechaDesde = v_FechaSistema;
			entradaFlujoCaja.p_FechaHasta = v_FechaSistema;
			entradaFlujoCaja.p_Uso = entrada.p_UsoFlujoCaja;
			entradaFlujoCaja.p_PortafolioAConsultar = portafoliosConsultar;
			// logger.debug("entradaFlujoCaja : " + entradaFlujoCaja);
			SalidaConsultarFlujoCaja salidaFlujoCaja = llamadoFlujoCaja.logica(con, entradaFlujoCaja);
			List<ObjFLUJOCAJA> flujoCaja = salidaFlujoCaja.P_OUT_FLUJOCAJA;
			flujoCaja = flujoCaja.stream()
					.filter(s -> s.FechaSaldo.equals(v_FechaSistema) && entrada.p_IdMonedaFlujoCaja.equals(s.IdMoneda))
					.collect(Collectors.toList());

			for (ObjFLUJOCAJA flujo : flujoCaja) {
				String keyFlujoCaja = flujo.IdPortafolio + "";
				if (!v_out_FlujoCaja.containsKey(keyFlujoCaja)) {
					v_out_FlujoCaja.put(keyFlujoCaja, flujo);
				}
			}
		}

		List<Long> listadoIdClientes = informacionClientes.parallelStream().map(listClientes -> listClientes.IdCliente)
				.distinct().collect(Collectors.toList());

		logger.debug("Inicio SQL_INSERT_ID_CLIENTE_TABLA_PASO");
		startTime = System.nanoTime();
		@SuppressWarnings("deprecation")
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		List<ClienteTablaPaso> idsClienteTablaPaso = listadoIdClientes.parallelStream().map(id -> {
			ClienteTablaPaso clienteTablaPaso = new ClienteTablaPaso();
			clienteTablaPaso.IdCliente = id;
			clienteTablaPaso.IdTrabajo = idTrabajo;
			return clienteTablaPaso;
		}).collect(Collectors.toList());
		_sql.batch(con, SQL_INSERT_ID_CLIENTE_TABLA_PASO, idsClienteTablaPaso, ClienteTablaPaso.class);
		logger.debug("(" + (System.nanoTime() - startTime) / 1000000
				+ ") Fin SQL_INSERT_ID_CLIENTE_TABLA_PASO, COUNT: " + idsClienteTablaPaso.size());
		logger.debug("Inicio SQL_SELECT_FECHA_ULTIMA_OPERACION_CLIENTES");
		startTime = System.nanoTime();
		HashMap<String, Object> paramsFechaUltOpeClientes = new HashMap<>();
		List<FechaSistemaCliente> fechaOperacionesClientes = _sql.query(con, SQL_SELECT_FECHA_ULTIMA_OPERACION_CLIENTES,
				paramsFechaUltOpeClientes, FechaSistemaCliente.class);
		logger.debug("(" + (System.nanoTime() - startTime) / 1000000
				+ ") Fin SQL_SELECT_FECHA_ULTIMA_OPERACION_CLIENTES, COUNT: " + fechaOperacionesClientes.size());

		HashMap<Long, Date> fechaOperacionesClientesHash = new HashMap<>();
		fechaOperacionesClientes.stream().forEach(fechaOperacionCliente -> {
			fechaOperacionesClientesHash.put(fechaOperacionCliente.IdCliente, fechaOperacionCliente.FechaSistema);
		});

		logger.debug("Inicio SQL_SELECT_FECHA_ULTIMA_MODIFICACION_CLIENTES");
		startTime = System.nanoTime();
		HashMap<String, Object> paramsFechaUltModClientes = new HashMap<>();
		List<FechaSistemaCliente> fechaModificacionesClientes = _sql.query(con,
				SQL_SELECT_FECHA_ULTIMA_MODIFICACION_CLIENTES, paramsFechaUltModClientes, FechaSistemaCliente.class);
		logger.debug("(" + (System.nanoTime() - startTime) / 1000000
				+ ") Fin SQL_SELECT_FECHA_ULTIMA_MODIFICACION_CLIENTES, COUNT: " + fechaModificacionesClientes.size());

		HashMap<Long, Date> fechaModificacionesClientesHash = new HashMap<>();
		fechaModificacionesClientes.stream().forEach(fechaModificacionCliente -> {
			fechaModificacionesClientesHash.put(fechaModificacionCliente.IdCliente,
					fechaModificacionCliente.FechaSistema);
		});

		logger.debug("Inicio SQL_SELECT_CONTRATOS_FIRMADOS");
		startTime = System.nanoTime();
		HashMap<String, Object> paramsContratosFirmados = new HashMap<String, Object>(2);
		List<ClienteFirmaContrato> clientesFirmaContratos = _sql.query(con, SQL_SELECT_CONTRATOS_FIRMADOS,
				paramsContratosFirmados, ClienteFirmaContrato.class);
		logger.debug("(" + (System.nanoTime() - startTime) / 1000000 + ") Fin SQL_SELECT_CONTRATOS_FIRMADOS, COUNT: "
				+ clientesFirmaContratos.size());

		HashMap<Long, ClienteFirmaContrato> firmaContratosPorIdCliente = new HashMap<>();
		clientesFirmaContratos.stream().forEach(firmaContrato -> {
			firmaContratosPorIdCliente.put(firmaContrato.IdCliente, firmaContrato);
		});

		informacionClientes.parallelStream().forEach(info -> {
			String keyFlujoCustodia = info.IdPortafolio + "-" + entrada.p_IdInstrumento;
			FlujoCustodiaPorPortafolioIdInstrumento objFlujoCustodia = v_FlujoCustodia.get(keyFlujoCustodia);
			if (objFlujoCustodia != null) {
				info.IdInstrumento = objFlujoCustodia.IdInstrumento;
				info.SaldoFlujoCustodia = objFlujoCustodia.Saldo != null ? objFlujoCustodia.Saldo : null;
				info.Nemotecnico = objFlujoCustodia.Nemotecnico;
			}
			String keyFlujoCaja = info.IdPortafolio + "";
			ObjFLUJOCAJA objFlujoCaja = v_out_FlujoCaja.get(keyFlujoCaja);
			if (objFlujoCaja != null) {
				info.SaldoFlujoCaja = objFlujoCaja.Saldo;
				info.IdMonedaFlujoCaja = objFlujoCaja.IdMoneda != null ? objFlujoCaja.IdMoneda : v_IdMonedaSistema;
			} else {
				info.IdMonedaFlujoCaja = v_IdMonedaSistema;
			}
			String keyInstrumento = info.IdInstrumento + "";
			Instrumento objInstrumento = v_out_Instrumento.get(keyInstrumento);
			if (objInstrumento != null) {
				info.IdMonedaInstrumento = objInstrumento.IdMoneda != null ? objInstrumento.IdMoneda
						: v_IdMonedaSistema;
			} else {
				info.IdMonedaInstrumento = v_IdMonedaSistema;
			}
			if (!fechaOperacionesClientesHash.isEmpty()) {
				if (fechaOperacionesClientesHash.get(info.IdCliente) != null)
					info.FechaUltimaOperacion = fechaOperacionesClientesHash.get(info.IdCliente);
			}
			if (!fechaModificacionesClientesHash.isEmpty()) {
				if (fechaModificacionesClientesHash.get(info.IdCliente) != null)
					info.FechaUltimaModificacion = fechaModificacionesClientesHash.get(info.IdCliente);
			}
			if (!firmaContratosPorIdCliente.isEmpty()) {
				if (firmaContratosPorIdCliente.get(info.IdCliente) != null)
					info.ContratosFirmados = firmaContratosPorIdCliente.get(info.IdCliente).ContratosNoFirmados > 0
							? "N"
							: "S";
			}

		});

		informacionClientes = informacionClientes.parallelStream()
				.filter(s -> ((MAYOR_QUE.equals(entrada.p_CriterioFlujoCaja) && (entrada.p_SaldoFlujoCaja != null
						&& s.SaldoFlujoCaja != null && s.SaldoFlujoCaja.compareTo(entrada.p_SaldoFlujoCaja) >= 0))
						|| (MENOR_QUE.equals(entrada.p_CriterioFlujoCaja)
								&& (entrada.p_SaldoFlujoCaja != null && s.SaldoFlujoCaja != null
										&& s.SaldoFlujoCaja.compareTo(entrada.p_SaldoFlujoCaja) <= 0))
						|| NO.equals(entrada.p_BuscaPorFlujoCaja))
						&& ((MAYOR_QUE.equals(entrada.p_CriterioFlujoCustodia)
								&& (entrada.p_SaldoFlujoCustodia != null && s.SaldoFlujoCustodia != null
										&& s.SaldoFlujoCustodia.compareTo(entrada.p_SaldoFlujoCustodia) >= 0))
								|| (MENOR_QUE.equals(entrada.p_CriterioFlujoCustodia)
										&& (entrada.p_SaldoFlujoCustodia != null && s.SaldoFlujoCustodia != null
												&& s.SaldoFlujoCustodia.compareTo(entrada.p_SaldoFlujoCustodia) <= 0))
								|| NO.equals(entrada.p_BuscaPorFlujoCustodia)))
				.collect(Collectors.toList());

		// informacionClientes.
		salida.P_OUT_CLIENTES.addAll(informacionClientes);
		return salida;
	}

	@Trace
	@Override
	public JsonObject call(Connection conn, JsonObject jin, Integer skip, Integer take, String sort, String filters)
			throws Exception {
		Gson gson = Utils.getGsonBuilder();
		logger.debug("HASSH ENTRADA");
		String hash = DigestUtils.sha256Hex(jin.toString());
		logger.debug("Obtener CACHE");
		SalidaConsultaAvanzadaClientes out = JavaViewCache.getCache(hash, SalidaConsultaAvanzadaClientes.class);
		logger.debug("fin");
		if (out == null) {
			EntradaConsultaAvanzadaClientes in = gson.fromJson(jin.toString(), EntradaConsultaAvanzadaClientes.class);
			out = logica(conn, in);
			JavaViewCache.setCache(hash, out);
		} else {
			logger.debug("HASH: " + hash + ", HIT CACHE!");
		}

		Integer total = out.P_OUT_CLIENTES.size();
		logger.debug("limit");
		out.P_OUT_CLIENTES = this.limitResponse(out.P_OUT_CLIENTES, skip, take,
				InformacionConsultaAvanzadaClientes.class);
		logger.debug("end limit");
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		dataout.add("total", new JsonPrimitive(total));
		return dataout;
	}

	public static void main(String[] args) {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = Utils.getGsonBuilder();

			String jsonClientes = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_IdEmpleadoConsulta\":7293,\"p_DocumentoIdentidad\":\"1-9\"}";

			EntradaConsultaAvanzadaClientes entrada = gson.fromJson(jsonClientes,
					EntradaConsultaAvanzadaClientes.class);
			ConsultaAvanzadaClientes consultaAvanzadaClientes = new ConsultaAvanzadaClientes();
			try {

				SalidaConsultaAvanzadaClientes salida = consultaAvanzadaClientes.logica(con, entrada);

				System.out.println(salida.P_OUT_CLIENTES);

			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
