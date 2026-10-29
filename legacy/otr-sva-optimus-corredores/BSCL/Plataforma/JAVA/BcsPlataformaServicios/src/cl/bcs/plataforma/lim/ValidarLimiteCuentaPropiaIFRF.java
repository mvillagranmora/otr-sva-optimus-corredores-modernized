package cl.bcs.plataforma.lim;

import cl.bcs.plataforma.lim.ConsultarCustodiaPortafolioCuentaPropia.ClasificablePorCustodia;
import cl.bcs.plataforma.lim.ConsultarCustodiaPortafolioCuentaPropia.ClasificadorCustodia;
import cl.bcs.plataforma.lim.ConsultarCustodiaPortafolioCuentaPropia.Custodia;
import cl.bcs.plataforma.lim.pojo.*;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.*;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidarLimiteCuentaPropiaIFRF implements JavaCall {
	static final Logger logger = LogManager.getLogger(ValidarLimiteCuentaPropiaIFRF.class);
	
	private final String SQL_SELECT_INSTRUMENTO_CLASIFICACION_RIESGO = loadResourceToString("cl/bcs/plataforma/lim/sql/ValidarLimiteCuentaPropiaIFRF_SelectInstrumentoClasificacionRiesgo.sql");
	
	private static final class KeyClasificadorCustodia extends BcsPojo {
		public static final Long CLIENTE_TIPOINSTRUMENTO = 1L;
		public static final Long CLIENTE_TIPOINSTRUMENTO_EMISOR = 2L;
		public static final Long CLIENTE_TIPOINSTRUMENTO_MONEDAINSTRUMENTO = 3L;
		public static final Long CLIENTE = 4L;
		public static final Long CLIENTE_MONEDAINSTRUMENTO = 5L;
	}
	
	private static final Map<Long, ClasificadorCustodia> ClasificadorPorKey = Stream.of(
		Pair.of(KeyClasificadorCustodia.CLIENTE_TIPOINSTRUMENTO, new ConsultarCustodiaPortafolioCuentaPropia.ClasificadorCustodiaBuilder().porCliente().porTipoInstrumento().terminar()),
		Pair.of(KeyClasificadorCustodia.CLIENTE_TIPOINSTRUMENTO_EMISOR, new ConsultarCustodiaPortafolioCuentaPropia.ClasificadorCustodiaBuilder().porCliente().porTipoInstrumento().porEmisor().terminar()),
		Pair.of(KeyClasificadorCustodia.CLIENTE_TIPOINSTRUMENTO_MONEDAINSTRUMENTO, new ConsultarCustodiaPortafolioCuentaPropia.ClasificadorCustodiaBuilder().porCliente().porTipoInstrumento().porMonedaInstrumento().terminar()),
		Pair.of(KeyClasificadorCustodia.CLIENTE, new ConsultarCustodiaPortafolioCuentaPropia.ClasificadorCustodiaBuilder().porCliente().terminar()),
		Pair.of(KeyClasificadorCustodia.CLIENTE_MONEDAINSTRUMENTO, new ConsultarCustodiaPortafolioCuentaPropia.ClasificadorCustodiaBuilder().porCliente().porMonedaInstrumento().terminar())
	).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
	
	private final ConstantesLimites _cl = new ConstantesLimites();
	
	private ContextoLogica contexto = new ContextoLogica();
	
	public static class EntradaValidarLimiteCuentaPropiaIFRF extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoProceso; // ConstantesLimites::PROCESO_ASIGNAR / ConstantesLimites::PROCESO_DESASIGNAR
		
		public List<Operacion> p_Operaciones;
		
		public EntradaValidarLimiteCuentaPropiaIFRF() {
			p_IdEmpresa = 0L;
			p_IdUsuario = 0L;
			p_UsaReglasNegocio = "N";
			p_IdTipoProceso = "";
			p_Operaciones = new ArrayList<>();
		}
	}
	
	public static class SalidaValidarLimiteCuentaPropiaIFRF extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<Validacion> P_OUT_VALIDACIONES;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public Set<Long> p_out_IdInternoGruposRechazados;
		
		public SalidaValidarLimiteCuentaPropiaIFRF() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_VALIDACIONES = new ArrayList<>();
			P_OUT_MENSAJES = new ArrayList<>();
			p_out_IdInternoGruposRechazados = new HashSet<>();
		}
	}
	
	
	public static class Operacion extends BcsPojo {
		public Long IdInternoGrupo;
		public Long IdInterno;
		public Long IdOperacion;
		public String Folio;
		
		// Clasificacion limite / custodia
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String NemoSVS;
		public String IdTipoCanalIngreso;
		public String IdTipoOperacion;
		public String IdMonedaOperacion;
		
		// Validacion
		public BigDecimal Cantidad;
		public BigDecimal ValorRescate;
		public BigDecimal MontoCompraMonedaSistema;
		public BigDecimal Tasa;
		public java.sql.Date FechaVencimientoInstrumento;
		
		public Operacion() {
		}
	}
	
	private static class OperacionProceso extends Operacion {
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String Nemotecnico; // NemoSVS (IF) / Nemotecnico (Resto)
		public String IdEmisor;
		public BigDecimal PosicionCustodia;
		
		public OperacionProceso() {
		}
		
		public Long obtenerIdInternoGrupo() {
			return IdInternoGrupo;
		}
		
		public Long obtenerIdInterno() {
			return IdInterno;
		}
	}
	
	public static class Instrumento extends BcsPojo {
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String Nemotecnico; // NemoSVS (IF) / Nemotecnico (Resto)
		public String IdEmisor;
		public String IdMonedaInstrumento;
		public BigDecimal Tasa; // Para RF desde INS.Instrumento::Tasa ; Para IF falta definicion
		public java.sql.Date FechaVencimiento;
	}
	
	public static class InstrumentoClasificado extends BcsPojo {
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String Nemotecnico; // NemoSVS (IF) / Nemotecnico (Resto)
		public String IdEmisor;
		public String IdMonedaInstrumento;
		public java.sql.Date FechaVencimiento;
		public String IdEntidadClasificadoraRiesgo;
		public String ValorClasificacionRiesgo;
		public Integer NivelClasificacionRiesgo;
		public BigDecimal Tasa;
	}
	
	private static class ContextoValidacion extends BcsPojo {
		public OperacionProceso Operacion;
		public Limite Limite;
		public Boolean SuperaLimite;
	}
	
	private static class ContextoLogica extends BcsPojo {
		public EntradaValidarLimiteCuentaPropiaIFRF Entrada;
		public SalidaValidarLimiteCuentaPropiaIFRF Salida;
		public Boolean ValidacionLimitesHabilitada;
		public java.sql.Timestamp FechaSistema;
		public Map<String, Function<ContextoValidacion, Boolean>> ValidadorPorIdTipoLimite;
		public CuentaPropia CuentaPropia;
		public transient Connection Connection;
		public transient Map<Long, NullaryProcedure> RevertirCustodiaPorIdInternoOperacion;
		public transient BuscadorCustodia BuscadorCustodia;
		public transient BuscadorInstrumento BuscadorInstrumento;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public SalidaValidarLimiteCuentaPropiaIFRF logica(Connection conn, EntradaValidarLimiteCuentaPropiaIFRF entrada) throws Exception {
		logger.info("INICIO::ValidarLimiteCuentaPropiaIFRF");
		long inicio = System.currentTimeMillis();
		
		contexto = obtenerContexto(conn, entrada);
		if (contexto.ValidacionLimitesHabilitada) {
			contexto.BuscadorCustodia = obtenerCustodias();
			contexto.BuscadorInstrumento = obtenerInstrumentos();
			List<OperacionProceso> operacionesProceso = obtenerOperacionesProceso();
			Map<Long, List<Limite>> limitePorIdInternoOperacion = obtenerLimitesPorIdInternoOperacion(operacionesProceso);
			List<Validacion> validaciones = obtenerTareasValidacion(operacionesProceso, limitePorIdInternoOperacion);
			agregarValidacionesSalida(validaciones);
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ValidarLimiteCuentaPropiaIFRF {}", fin - inicio);
		
		return contexto.Salida;
	}
	
	private ContextoLogica obtenerContexto(Connection conn, EntradaValidarLimiteCuentaPropiaIFRF entrada) throws Exception {
		logger.debug("INICIO::ValidarLimiteCuentaPropiaIFRF.obtenerContexto");
		long inicio = System.currentTimeMillis();
		
		
		ContextoLogica contextoLogica = new ContextoLogica();
		contextoLogica.Entrada = entrada;
		contextoLogica.Salida = new SalidaValidarLimiteCuentaPropiaIFRF();
		contextoLogica.Connection = conn;
		contextoLogica.FechaSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);
		contextoLogica.CuentaPropia = LimiteUtils.obtenerCuentaPropia(conn, entrada.p_IdEmpresa);
		contextoLogica.ValidacionLimitesHabilitada = _cl.SI.equals(entrada.p_UsaReglasNegocio)
				&& _cl.SI.equals(ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "LIMITE", "LIM.HabilitarConfiguracionLimite"));
		
		contextoLogica.ValidadorPorIdTipoLimite = new HashMap<>(8);
		contextoLogica.ValidadorPorIdTipoLimite.put(_cl.TIPO_LIMITE_CUENTA_PROPIA_PLAZO_VENCIMIENTO, this::validarPlazoVencimiento);
		contextoLogica.ValidadorPorIdTipoLimite.put(_cl.TIPO_LIMITE_CUENTA_PROPIA_EXCLUIR_EMISOR, this::validarExclusionEmisor);
		contextoLogica.ValidadorPorIdTipoLimite.put(_cl.TIPO_LIMITE_CUENTA_PROPIA_CLASIFICACION_RIESGO, this::validarClasificacionRiesgo);
		contextoLogica.ValidadorPorIdTipoLimite.put(_cl.TIPO_LIMITE_CUENTA_PROPIA_MONEDA, this::validarMoneda);
		contextoLogica.ValidadorPorIdTipoLimite.put(_cl.TIPO_LIMITE_CUENTA_PROPIA_MARGEN_TASAS, this::validarMargenTasas);
		
		contextoLogica.RevertirCustodiaPorIdInternoOperacion = new HashMap<>();
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarLimiteCuentaPropiaIFRF.obtenerContexto {}", fin - inicio);
		
		return contextoLogica;
	}
	
	private BuscadorCustodia obtenerCustodias() throws Exception {
		logger.debug("INICIO::ValidarLimiteCuentaPropiaIFRF.obtenerCustodias");
		long inicio = System.currentTimeMillis();
		
		ConsultarCustodiaPortafolioCuentaPropia.EntradaConsultarCustodiaPortafolioCuentaPropia entradaConsultarCustodiaPortafolioCuentaPropia = new ConsultarCustodiaPortafolioCuentaPropia.EntradaConsultarCustodiaPortafolioCuentaPropia();
		entradaConsultarCustodiaPortafolioCuentaPropia.p_IdEmpresa = contexto.Entrada.p_IdEmpresa;
		entradaConsultarCustodiaPortafolioCuentaPropia.p_IdUsuario = contexto.Entrada.p_IdUsuario;
		entradaConsultarCustodiaPortafolioCuentaPropia.p_IdModulo = contexto.Entrada.p_IdModulo;
		entradaConsultarCustodiaPortafolioCuentaPropia.p_UsaReglasNegocio = contexto.Entrada.p_UsaReglasNegocio;
		
		entradaConsultarCustodiaPortafolioCuentaPropia.p_TipoFechaConsulta = _cl.TIPO_FECHA_CONSULTA_ADQUISICION;
		entradaConsultarCustodiaPortafolioCuentaPropia.p_FechaConsulta = Fecha.toStringDate(contexto.FechaSistema);
		entradaConsultarCustodiaPortafolioCuentaPropia.p_IncluirAsignacionesSinFacturar = _cl.SI;
		entradaConsultarCustodiaPortafolioCuentaPropia.p_CuentaPropia = contexto.CuentaPropia;
		entradaConsultarCustodiaPortafolioCuentaPropia.p_ClasificadorCustodiaPorIdInterno = ClasificadorPorKey;
		
		ConsultarCustodiaPortafolioCuentaPropia.SalidaConsultarCustodiaPortafolioCuentaPropia salidaConsultarCustodiaPortafolioCuentaPropia = new ConsultarCustodiaPortafolioCuentaPropia().logica(contexto.Connection, entradaConsultarCustodiaPortafolioCuentaPropia);
		BuscadorCustodia buscadorCustodia = new BuscadorCustodia(salidaConsultarCustodiaPortafolioCuentaPropia.BuscadorCustodia);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarLimiteCuentaPropiaIFRF.obtenerCustodias {}", fin - inicio);
		
		return buscadorCustodia;
	}
	
	private BuscadorInstrumento obtenerInstrumentos() throws SQLException {
		logger.debug("INICIO::ValidarLimiteCuentaPropiaIFRF.obtenerInstrumentos");
		long inicio = System.currentTimeMillis();
		
		int len = contexto.Entrada.p_Operaciones.size(); 
		Set<Long> nemotecnicos = new HashSet<>(len);
		Set<Long> instrumentoIds = new HashSet<>(len);
		contexto.Entrada.p_Operaciones.forEach(operacion -> {
			if (StringUtils.isNullOrBlank(operacion.NemoSVS) == false) {
				nemotecnicos.add(operacion.IdInstrumento);
			} else {
				instrumentoIds.add(operacion.IdInstrumento);
			}
		});
		
		HashMap<String, Object> params = new HashMap<>(8);
		params.put("IdEmpresa", contexto.Entrada.p_IdEmpresa);
		params.put("IdInstrumento", new ArrayList<>(instrumentoIds));
		params.put("Nemotecnico", new ArrayList<>(nemotecnicos));
		
		List<InstrumentoClasificado> instrumentos = new SQL().query(contexto.Connection, SQL_SELECT_INSTRUMENTO_CLASIFICACION_RIESGO, params, InstrumentoClasificado.class);
		BuscadorInstrumento buscador = new BuscadorInstrumento(instrumentos);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarLimiteCuentaPropiaIFRF.obtenerInstrumentos {}", fin - inicio);
		
		return buscador;
	}
	
	private List<OperacionProceso> obtenerOperacionesProceso() {
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.obtenerOperacionesProceso");
		long inicio = System.currentTimeMillis();
		
		List<OperacionProceso> operacionesProceso = contexto.Entrada.p_Operaciones.parallelStream()
				.filter(operacion -> {
					Instrumento instrumento = contexto.BuscadorInstrumento.obtenerInstrumentoOperacion(operacion);
					boolean clasificacionAceptada = _cl.CLASIFICACION_IF.equals(instrumento.IdClasificacionTipoInstrumento) || _cl.CLASIFICACION_RF.equals(instrumento.IdClasificacionTipoInstrumento);
					boolean contraCuentaPropia = contexto.CuentaPropia.IdCliente.equals(operacion.IdCliente) || contexto.CuentaPropia.PortafolioIds.contains(operacion.IdPortafolio);
					return clasificacionAceptada && contraCuentaPropia;
				})
				.map(operacion -> {
					Instrumento instrumento = contexto.BuscadorInstrumento.obtenerInstrumentoOperacion(operacion);
					OperacionProceso operacionProceso = new OperacionProceso();
					operacionProceso.Folio = operacion.Folio;
					operacionProceso.IdInternoGrupo = normalizarIdInternoGrupo(operacion.IdInternoGrupo);
					operacionProceso.IdInterno = operacion.IdInterno;
					operacionProceso.IdOperacion = operacion.IdOperacion;
					operacionProceso.IdTipoOperacion = operacion.IdTipoOperacion;
					operacionProceso.IdMonedaOperacion = operacion.IdMonedaOperacion;
					operacionProceso.IdTipoCanalIngreso = operacion.IdTipoCanalIngreso;
					operacionProceso.IdCliente = operacion.IdCliente;
					operacionProceso.IdPortafolio = operacion.IdPortafolio;
					operacionProceso.IdInstrumento = operacion.IdInstrumento;
					operacionProceso.NemoSVS = operacion.NemoSVS;
					operacionProceso.IdTipoInstrumento = instrumento.IdTipoInstrumento;
					operacionProceso.IdClasificacionTipoInstrumento = instrumento.IdClasificacionTipoInstrumento;
					operacionProceso.Nemotecnico = instrumento.Nemotecnico;
					operacionProceso.IdEmisor = instrumento.IdEmisor;
					operacionProceso.FechaVencimientoInstrumento = instrumento.FechaVencimiento;
					operacionProceso.Tasa = operacion.Tasa;
					
					BigDecimal factorAditivo = obtenerFactorAditivo(operacion.IdTipoOperacion);
					operacionProceso.MontoCompraMonedaSistema = factorAditivo.multiply(operacion.MontoCompraMonedaSistema);
					BigDecimal posicion = _cl.CLASIFICACION_IF.equals(instrumento.IdClasificacionTipoInstrumento) ? operacion.ValorRescate : operacion.Cantidad;
					operacionProceso.PosicionCustodia = factorAditivo.multiply(posicion);
					
					return operacionProceso;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimiteCuentaPropiaIFRF.obtenerOperacionesProceso {}", fin - inicio);
		
		return operacionesProceso;
	}
	
	private Map<Long, List<Limite>> obtenerLimitesPorIdInternoOperacion(List<OperacionProceso> operaciones) throws SQLException {
		logger.debug("INICIO::ValidarLimiteCuentaPropiaIFRF.obtenerLimitesPorIdInternoOperacion");
		long inicio = System.currentTimeMillis();
		
		ConsultarLimitesCuentaPropia.EntradaConsultarLimitesCuentaPropia entradaConsultarLimitesCuentaPropia = new ConsultarLimitesCuentaPropia.EntradaConsultarLimitesCuentaPropia();
		entradaConsultarLimitesCuentaPropia.p_IdEmpresa = contexto.Entrada.p_IdEmpresa;
		entradaConsultarLimitesCuentaPropia.Clasificaciones = operaciones.stream()
				.map((o) -> {
					ConsultarLimitesCuentaPropia.ClasificacionLimite clasificacionLimite = new ConsultarLimitesCuentaPropia.ClasificacionLimite();
					clasificacionLimite.IdInterno = o.IdInterno;
					clasificacionLimite.IdClasificacionTipoInstrumento = o.IdClasificacionTipoInstrumento;
					clasificacionLimite.IdTipoInstrumento = o.IdTipoInstrumento;
					clasificacionLimite.IdTipoOperacion = o.IdTipoOperacion;
					clasificacionLimite.IdMonedaOperacion = o.IdMonedaOperacion;
					clasificacionLimite.IdTipoCanalIngreso = o.IdTipoCanalIngreso;
					clasificacionLimite.IdEmisor = o.IdEmisor;
					return clasificacionLimite;
				})
				.collect(Collectors.toList());
		
		ConsultarLimitesCuentaPropia.SalidaConsultarLimitesCuentaPropia salidaConsultarLimitesCuentaPropia = new ConsultarLimitesCuentaPropia().logica(contexto.Connection, entradaConsultarLimitesCuentaPropia);
		Map<Long, List<Limite>> limitesPorIdInterno = salidaConsultarLimitesCuentaPropia.LimitesPorIdInterno;
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarLimiteCuentaPropiaIFRF.obtenerLimitesPorIdInternoOperacion {}", fin - inicio);
		
		return limitesPorIdInterno;
	}
	
	private List<Validacion> obtenerTareasValidacion(List<OperacionProceso> operaciones, Map<Long, List<Limite>> limitesPorIdInternoOperacion) {
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.obtenerTareasValidacion");
		long inicio = System.currentTimeMillis();
		
		Predicate<OperacionProceso> tieneGrupo = (operacion) -> Objects.nonNull(operacion.IdInternoGrupo);
		
		NullaryProcedure noop = () -> {};
		Set<Long> gruposRechazados = new HashSet<>();
		Map<Long, List<Long>> idsProcesadasPorGrupo = new HashMap<>(operaciones.size());
		Map<Long, List<OperacionProceso>> operacionesPorIdInternoGrupo = operaciones.stream().filter(tieneGrupo).collect(Collectors.groupingBy(u -> u.IdInternoGrupo));		
		
		List<OperacionProceso> operacionesOrdenadas = obtenerOperacionesOrdenadasParaEvaluacion(operaciones);
		
		List<Validacion> validaciones = new ArrayList<>();
		for (OperacionProceso operacion : operacionesOrdenadas) {
			if (gruposRechazados.contains(operacion.IdInternoGrupo)) {
				continue;
			}
			
			List<Limite> limites = limitesPorIdInternoOperacion.getOrDefault(operacion.IdInterno, Collections.emptyList());
			Validacion validacion = validarLimitesOperacion(operacion, limites)
					.filter(u -> !_cl.TIPO_VALIDACION_OMITIR.equals(u.TipoComportamiento))
					.orElse(null);
			
			if (validacion == null) {
				contexto.BuscadorCustodia.acumularCustodia(operacion, BigDecimal.ONE);
				contexto.RevertirCustodiaPorIdInternoOperacion.put(operacion.IdInterno, () -> contexto.BuscadorCustodia.acumularCustodia(operacion, BigDecimal.ONE.negate()));
				if (tieneGrupo.test(operacion)) {
					idsProcesadasPorGrupo.computeIfAbsent(operacion.IdInternoGrupo, k -> new ArrayList<>()).add(operacion.IdInterno);
				}
				continue;
			}
			
			validaciones.add(validacion);
			if (_cl.TIPO_VALIDACION_RECHAZAR.equals(validacion.TipoComportamiento)) {
				List<Long> idsRevertir = new ArrayList<>(Collections.singletonList(operacion.IdInterno));
				if (tieneGrupo.test(operacion)) {
					gruposRechazados.add(operacion.IdInternoGrupo);
					idsRevertir.addAll(idsProcesadasPorGrupo.getOrDefault(operacion.IdInternoGrupo, Collections.emptyList()));
					
					Validacion validacionGrupo = new Validacion();
					validacionGrupo.IdInternoOperacion = null;
					validacionGrupo.IdInternoGrupoOperacion = validacion.IdInternoGrupoOperacion;
					validacionGrupo.TipoComportamiento = validacion.TipoComportamiento;
					HashMap<String, String> params = new HashMap<>(2);
					params.put("GroupSize", String.valueOf(operacionesPorIdInternoGrupo.get(validacionGrupo.IdInternoGrupoOperacion).size()));
					validacionGrupo.Mensaje = LimiteUtils.obtenerMensajeSalida(
							contexto.Connection,
							contexto.Entrada.p_IdEmpresa,
							validacionGrupo.IdInternoGrupoOperacion,
							validacion.Mensaje.tipo,
							"UI_RECH_LIMCTP_GRUPO_OPERACIONES_RECHAZADO",
							params
					);
					validaciones.add(validacionGrupo);
				}
				idsRevertir.forEach(idAnterior -> contexto.RevertirCustodiaPorIdInternoOperacion.getOrDefault(idAnterior, noop).apply());
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimiteCuentaPropiaIFRF.obtenerTareasValidacion {}", fin - inicio);
		
		return validaciones;
	}
	
	private List<OperacionProceso> obtenerOperacionesOrdenadasParaEvaluacion(List<OperacionProceso> operaciones) {
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.obtenerOperacionesOrdenadasParaEvaluacion");
		long inicio = System.currentTimeMillis();
		
		Comparator<OperacionProceso> porIdGrupoLuegoIdInterno = Comparator
				.comparing(OperacionProceso::obtenerIdInternoGrupo, Comparator.nullsLast(Comparator.naturalOrder()))
				.thenComparing(OperacionProceso::obtenerIdInterno, Comparator.naturalOrder());
		
		List<OperacionProceso> operacionesOrdenadas = operaciones.stream()
				.sorted(porIdGrupoLuegoIdInterno)
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimiteCuentaPropiaIFRF.obtenerOperacionesOrdenadasParaEvaluacion {}", fin - inicio);
		
		return operacionesOrdenadas;
	}
	
	private Optional<Validacion> validarLimitesOperacion(OperacionProceso operacion, List<Limite> limites) {
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.validarLimitesOperacion");
		long inicio = System.currentTimeMillis();
		
		Optional<Validacion> maybeValidacion = limites.stream()
				.map(limite -> {
					ContextoValidacion contextoValidacion = new ContextoValidacion();
					contextoValidacion.Operacion = operacion;
					contextoValidacion.Limite = limite;
					Function<ContextoValidacion, Boolean> validador = contexto.ValidadorPorIdTipoLimite.computeIfAbsent(limite.IdTipoLimite, k -> t -> false);
					contextoValidacion.SuperaLimite = validador.apply(contextoValidacion);
					return contextoValidacion;
				})
				.filter(tarea -> tarea.SuperaLimite)
				.findFirst()
				.map(tarea -> {
					LimiteUtils.EntradaObtenerValidacionLimite entradaObtenerValidacionLimite = new LimiteUtils.EntradaObtenerValidacionLimite();
					entradaObtenerValidacionLimite.Connection = contexto.Connection;
					entradaObtenerValidacionLimite.IdEmpresa = contexto.Entrada.p_IdEmpresa;
					entradaObtenerValidacionLimite.IdInterno = tarea.Operacion.IdInterno;
					entradaObtenerValidacionLimite.IdInternoGrupo = tarea.Operacion.IdInternoGrupo;
					entradaObtenerValidacionLimite.Folio = tarea.Operacion.Folio;
					entradaObtenerValidacionLimite.NemoInstrumento = tarea.Operacion.Nemotecnico;
					entradaObtenerValidacionLimite.Limite = tarea.Limite;
					entradaObtenerValidacionLimite.Proceso = contexto.Entrada.p_IdTipoProceso;
					return entradaObtenerValidacionLimite;
				})
				.flatMap(LimiteUtils::obtenerValidacionLimite);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimiteCuentaPropiaIFRF.validarLimitesOperacion {}", fin - inicio);
		
		return maybeValidacion;
	}
	
	private Boolean validarPlazoVencimiento(ContextoValidacion tarea) {
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.validarPlazoVencimiento");
		long inicio = System.currentTimeMillis();
		
		LocalDate fechaVecimiento = tarea.Operacion.FechaVencimientoInstrumento.toLocalDate();
		LocalDate fechaSistema = contexto.FechaSistema.toLocalDateTime().toLocalDate();
		Optional<ChronoUnit> maybeUnidad = LimiteUtils.obtenerUnidadTemporal(tarea.Limite.UnidadPlazo);
		
		boolean noCumplePlazo = maybeUnidad
				.map(unidad -> unidad.between(fechaSistema, fechaVecimiento))
				.map(plazoVigencia -> tarea.Limite.Plazo > plazoVigencia)
				.orElse(false);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimiteCuentaPropiaIFRF.validarPlazoVencimiento {}", fin - inicio);
		
		return noCumplePlazo;
	}
	
	private Boolean validarExclusionEmisor(ContextoValidacion tarea) {
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.validarExclusionEmisor");
		long inicio = System.currentTimeMillis();
		
		Custodia custodiaSobreEmisorInstrumentoOperacion = contexto.BuscadorCustodia.obtenerCustodiaClienteTipoInstrumentoEmisor(tarea.Operacion);
		Custodia custodiaTotal = contexto.BuscadorCustodia.obtenerCustodiaClienteTipoInstrumento(tarea.Operacion);
		
		BigDecimal totalPosicion = custodiaTotal.TotalPosicion.add(tarea.Operacion.PosicionCustodia);
		BigDecimal totalPosicionConEmisor = custodiaSobreEmisorInstrumentoOperacion.TotalPosicion.add(tarea.Operacion.PosicionCustodia);
		
		BigDecimal porcentajeEmisor = obtenerPorcentaje(totalPosicionConEmisor, totalPosicion);
		BigDecimal porcentajeLimite = tarea.Limite.Porcentaje;
		if (porcentajeLimite == null) {
			String log = String.format("MontoTotal (Porcentaje) NULL para limite con ID = %d, ignorando validacion de limite excluir emisor", tarea.Limite.IdLimite);
			logger.warn(log);
		}
		boolean excedePorcentaje = Objects.nonNull(porcentajeLimite) && (porcentajeEmisor == null || (porcentajeEmisor.compareTo(porcentajeLimite) > 0));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimiteCuentaPropiaIFRF.validarExclusionEmisor {}", fin - inicio);
		
		return excedePorcentaje;
	}
	
	private Boolean validarMoneda(ContextoValidacion tarea) {
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.validarMoneda");
		long inicio = System.currentTimeMillis();
		
		Custodia custodiaSobreMonedaOperacion = contexto.BuscadorCustodia.obtenerCustodiaClienteMonedaInstrumento(tarea.Operacion);
		Custodia custodiaTotal = contexto.BuscadorCustodia.obtenerCustodiaCliente(tarea.Operacion);
		
		BigDecimal totalPosicion = custodiaTotal.TotalPosicion.add(tarea.Operacion.PosicionCustodia);
		BigDecimal totalPosicionSobreMonedaOperacion = custodiaSobreMonedaOperacion.TotalPosicion.add(tarea.Operacion.PosicionCustodia);
		
		BigDecimal porcentajeMoneda = obtenerPorcentaje(totalPosicionSobreMonedaOperacion, totalPosicion);
		BigDecimal porcentajeLimite = tarea.Limite.Porcentaje;
		if (porcentajeLimite == null) {
			String log = String.format("MontoTotal (Porcentaje) NULL para limite con ID = %d, ignorando validacion de limite moneda", tarea.Limite.IdLimite);
			logger.warn(log);
		}
		boolean excedePorcentaje = Objects.nonNull(porcentajeLimite) && (porcentajeMoneda == null || (porcentajeMoneda.compareTo(porcentajeLimite) > 0));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimiteCuentaPropiaIFRF.validarMoneda {}", fin - inicio);
		
		return excedePorcentaje;
	}
	
	private Boolean validarMargenTasas(ContextoValidacion tarea) {
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.validarMargenTasas");
		long inicio = System.currentTimeMillis();
		
		Instrumento instrumento = contexto.BuscadorInstrumento.obtenerInstrumentoOperacion(tarea.Operacion);
		boolean fueraDeRango = Optional.ofNullable(instrumento.Tasa)
				.map(tasa -> {
					BigDecimal tasaOperacion = tarea.Operacion.Tasa;
					BigDecimal cotaInferior = tasa.subtract(tarea.Limite.MargenInferior);
					BigDecimal cotaSuperior = tasa.add(tarea.Limite.MargenSuperior);
					return cotaInferior.compareTo(tasaOperacion) > 0 || cotaSuperior.compareTo(tasaOperacion) < 0;
				})
				.orElse(false);
		
		if (instrumento.Tasa == null) {
			HashMap<String, String> params = new HashMap<>(2);
			params.put("Nemotecnico", instrumento.Nemotecnico);
			PlataformaUtils.MensajeSalida mensajeSalida = LimiteUtils.obtenerMensajeSalida(
					contexto.Connection,
					contexto.Entrada.p_IdEmpresa,
					tarea.Operacion.IdInterno,
					PlataformaUtils.MensajeSalida.MENSAJE_ADVERTENCIA,
					"INSTRUMENTO_SIN_TASA_IGNORA_LIMCTPMARGENTASAS",
					params
			); 
			contexto.Salida.P_OUT_MENSAJES.add(mensajeSalida);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimiteCuentaPropiaIFRF.validarMargenTasas {}", fin - inicio);
		
		return fueraDeRango;
	}
	
	private Boolean validarClasificacionRiesgo(ContextoValidacion tarea) {
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.validarClasificacionRiesgo");
		long inicio = System.currentTimeMillis();
		
		Predicate<InstrumentoClasificado> filtroClasificacion = instrumentoClasificado -> {
			boolean entidadCoincide = _cl.ENTIDAD_CLASIFICADORA_RIESGO_TODOS.equals(tarea.Limite.IdEntidadClasificadoraRiesgo) || tarea.Limite.IdEntidadClasificadoraRiesgo.equals(instrumentoClasificado.IdEntidadClasificadoraRiesgo); 
			boolean valorCoincide = _cl.CLASIFICACION_RIESGO_TODOS.equals(tarea.Limite.ValorClasificacionRiesgo) || tarea.Limite.ValorClasificacionRiesgo.equals(instrumentoClasificado.ValorClasificacionRiesgo);
			return entidadCoincide && valorCoincide;
		};
		InstrumentoClasificado instrumentoClasificado = contexto.BuscadorInstrumento.obtenerClasificacionMenorRiesgoInstrumento(tarea.Operacion.IdInstrumento, filtroClasificacion);
		boolean excedeMonto = Optional.ofNullable(instrumentoClasificado)
				.map(u -> {
					Custodia custodiaTotal = contexto.BuscadorCustodia.obtenerCustodiaClienteTipoInstrumentoMonedaInstrumento(tarea.Operacion);
					BigDecimal montoTotalNuevo = custodiaTotal.MontoCompra.add(tarea.Operacion.MontoCompraMonedaSistema);
					return montoTotalNuevo.compareTo(tarea.Limite.MontoTotal) > 0;
				})
				.orElse(false);
		
		long fin = System.currentTimeMillis();
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.validarClasificacionRiesgo {}", fin - inicio);
		
		return excedeMonto;
	}
	
	private BigDecimal upperClamp(BigDecimal o, BigDecimal cotaSuperior) {
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.upperClamp");
		long inicio = System.currentTimeMillis();
		
		BigDecimal acotado = cotaSuperior.compareTo(o) < 0 ? cotaSuperior : o;

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimiteCuentaPropiaIFRF.upperClamp {}", fin - inicio);
		
		return acotado;
	}
	
	private BigDecimal obtenerPorcentaje(BigDecimal dividendo, BigDecimal divisor) {
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.obtenerPorcentaje");
		long inicio = System.currentTimeMillis();
		
		BigDecimal cien = new BigDecimal("100");
		BigDecimal cociente = BigDecimal.ZERO.compareTo(divisor) == 0
				? null
				: cien.multiply(dividendo, MathContext.DECIMAL128).divide(divisor, MathContext.DECIMAL128);
		
		BigDecimal porcentaje = upperClamp(cociente, cien);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimiteCuentaPropiaIFRF.obtenerPorcentaje {}", fin - inicio);
		
		return porcentaje;
	}
	
	private BigDecimal obtenerFactorAditivo(String tipoOperacion) {
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.obtenerFactorAditivo");
		long inicio = System.currentTimeMillis();
		
		BigDecimal factor = BigDecimal.ONE;
		String proceso = contexto.Entrada.p_IdTipoProceso;
		boolean esAsignacionVenta = _cl.PROCESO_ASIGNAR.equals(proceso) && tipoOperacion.startsWith(_cl.CODIGO_VENTA);
		boolean esDesasignacionCompra = _cl.PROCESO_DESASIGNAR.equals(proceso) && tipoOperacion.startsWith(_cl.CODIGO_COMPRA);
		if (esAsignacionVenta || esDesasignacionCompra) {
			factor = BigDecimal.ONE.negate();
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimiteCuentaPropiaIFRF.obtenerFactorAditivo {}", fin - inicio);
		
		return factor;
	}
	
	private Long normalizarIdInternoGrupo(Long id) {
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.normalizarIdInternoGrupo");
		long inicio = System.currentTimeMillis();
		
		Long nid = id == null || id == 0L ? null : id;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimiteCuentaPropiaIFRF.normalizarIdInternoGrupo {}", fin - inicio);
		
		return nid;
	}
	
	private void agregarValidacionSalida(Validacion validacion) {
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.agregarValidacionSalida");
		long inicio = System.currentTimeMillis();
		
		contexto.Salida.P_OUT_VALIDACIONES.add(validacion);
		contexto.Salida.P_OUT_MENSAJES.add(validacion.Mensaje);
		if (_cl.TIPO_VALIDACION_RECHAZAR.equals(validacion.TipoComportamiento) && Objects.nonNull(validacion.IdInternoGrupoOperacion)) {
			contexto.Salida.p_out_IdInternoGruposRechazados.add(validacion.IdInternoGrupoOperacion);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimiteCuentaPropiaIFRF.agregarValidacionSalida {}", fin - inicio);
	}
	
	private void agregarValidacionesSalida(List<Validacion> validaciones) {
		logger.trace("INICIO::ValidarLimiteCuentaPropiaIFRF.agregarValidacionesSalida");
		long inicio = System.currentTimeMillis();
		
		for (Validacion validacion : validaciones) {
			agregarValidacionSalida(validacion);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimiteCuentaPropiaIFRF.agregarValidacionesSalida {}", fin - inicio);
	}
	
	private static class BuscadorCustodia {
		private final ConsultarCustodiaPortafolioCuentaPropia.BuscadorCustodia BUSCADOR_CUSTODIA;
		public BuscadorCustodia(ConsultarCustodiaPortafolioCuentaPropia.BuscadorCustodia buscadorCustodia) {
			BUSCADOR_CUSTODIA = buscadorCustodia;
		}
		
		public ClasificablePorCustodia obtenerFiltroCustodia(OperacionProceso operacionProceso) {
			ClasificablePorCustodia clasificablePorCustodia = new ClasificablePorCustodia();
			clasificablePorCustodia.IdCliente = operacionProceso.IdCliente;
			clasificablePorCustodia.IdPortafolio = operacionProceso.IdPortafolio;
			clasificablePorCustodia.IdInstrumento = operacionProceso.IdInstrumento;
			clasificablePorCustodia.IdTipoInstrumento = operacionProceso.IdTipoInstrumento;
			clasificablePorCustodia.IdClasificacionTipoInstrumento = operacionProceso.IdClasificacionTipoInstrumento;
			clasificablePorCustodia.Nemotecnico = operacionProceso.Nemotecnico;
			clasificablePorCustodia.IdEmisor = operacionProceso.IdEmisor;
			clasificablePorCustodia.IdMonedaInstrumento = operacionProceso.IdMonedaOperacion; // TODO: Validar
			return clasificablePorCustodia;
		}
		
		public Custodia obtenerCustodia(Long idClasificador, OperacionProceso operacionProceso) {
			return BUSCADOR_CUSTODIA.obtenerCustodia(idClasificador, obtenerFiltroCustodia(operacionProceso));
		}
		
		public Custodia obtenerCustodiaClienteTipoInstrumento(OperacionProceso operacionProceso) {
			return obtenerCustodia(KeyClasificadorCustodia.CLIENTE_TIPOINSTRUMENTO, operacionProceso);
		}
		
		public Custodia obtenerCustodiaClienteTipoInstrumentoEmisor(OperacionProceso operacionProceso) {
			return obtenerCustodia(KeyClasificadorCustodia.CLIENTE_TIPOINSTRUMENTO_EMISOR, operacionProceso);
		}
		
		public Custodia obtenerCustodiaClienteTipoInstrumentoMonedaInstrumento(OperacionProceso operacionProceso) {
			return obtenerCustodia(KeyClasificadorCustodia.CLIENTE_TIPOINSTRUMENTO_MONEDAINSTRUMENTO, operacionProceso);
		}
		
		public Custodia obtenerCustodiaCliente(OperacionProceso operacionProceso) {
			return obtenerCustodia(KeyClasificadorCustodia.CLIENTE, operacionProceso);
		}
		
		public Custodia obtenerCustodiaClienteMonedaInstrumento(OperacionProceso operacionProceso) {
			return obtenerCustodia(KeyClasificadorCustodia.CLIENTE_MONEDAINSTRUMENTO, operacionProceso);
		}
		
		public void acumularCustodia(OperacionProceso operacionProceso, BigDecimal factorAdivito) {
			BigDecimal posicion = factorAdivito.multiply(operacionProceso.PosicionCustodia);
			BigDecimal montoCompra = factorAdivito.multiply(operacionProceso.MontoCompraMonedaSistema);
			ClasificablePorCustodia clasificable = obtenerFiltroCustodia(operacionProceso);
			BUSCADOR_CUSTODIA.acumularCustodia(clasificable, posicion, montoCompra);
		}
	}
	
	public static class BuscadorInstrumento {
		private final Map<Long, Instrumento> instrumentoPorId;
		private final Map<String, Instrumento> instrumentoPorNemoSVS;
		private final Map<Long, List<InstrumentoClasificado>> clasificacionesRiesgoPorIdInstrumento;
		
		public BuscadorInstrumento(List<InstrumentoClasificado> instrumentosClasificados) {
			instrumentoPorId = new HashMap<>(instrumentosClasificados.size());
			instrumentoPorNemoSVS = new HashMap<>(instrumentosClasificados.size());
			clasificacionesRiesgoPorIdInstrumento = new HashMap<>(instrumentosClasificados.size());
			for (InstrumentoClasificado instrumentoClasificado : instrumentosClasificados) {
				instrumentoPorId.computeIfAbsent(instrumentoClasificado.IdInstrumento, k -> obtenerInstrumentoDesdeClasificado(instrumentoClasificado));
				if ("IF".equals(instrumentoClasificado.IdClasificacionTipoInstrumento)) {
					instrumentoPorNemoSVS.computeIfAbsent(instrumentoClasificado.Nemotecnico, k -> instrumentoPorId.get(instrumentoClasificado.IdInstrumento));
				}
				clasificacionesRiesgoPorIdInstrumento.computeIfAbsent(instrumentoClasificado.IdInstrumento, k -> new ArrayList<>()).add(instrumentoClasificado);
			}
		}
		
		private Instrumento obtenerInstrumentoDesdeClasificado(InstrumentoClasificado instrumentoClasificado) {
			Instrumento instrumento = new Instrumento();
			instrumento.IdInstrumento = instrumentoClasificado.IdInstrumento;
			instrumento.IdTipoInstrumento = instrumentoClasificado.IdTipoInstrumento;
			instrumento.IdClasificacionTipoInstrumento = instrumentoClasificado.IdClasificacionTipoInstrumento;
			instrumento.Nemotecnico = instrumentoClasificado.Nemotecnico;
			instrumento.IdEmisor = instrumentoClasificado.IdEmisor;
			instrumento.IdMonedaInstrumento = instrumentoClasificado.IdMonedaInstrumento;
			instrumento.FechaVencimiento = instrumentoClasificado.FechaVencimiento;
			instrumento.Tasa = instrumentoClasificado.Tasa;
			return instrumento;
		}
		
		private Instrumento obtenerInstrumentoOperacion(Operacion operacion) {
			Instrumento base = instrumentoPorId.computeIfAbsent(operacion.IdInstrumento, k -> {
				Instrumento instrumento = new Instrumento();
				instrumento.IdInstrumento = operacion.IdInstrumento;
				return instrumento;
			});
			
			Instrumento svs = instrumentoPorNemoSVS.computeIfAbsent(operacion.NemoSVS, k -> {
				Instrumento instrumento = new Instrumento();
				instrumento.Nemotecnico = operacion.NemoSVS;
				instrumento.FechaVencimiento = operacion.FechaVencimientoInstrumento;
				return instrumento;
			});
			
			if ("IF".equals(base.IdClasificacionTipoInstrumento) && base.Nemotecnico.equals(svs.Nemotecnico) == false) {
				svs.IdClasificacionTipoInstrumento = base.IdClasificacionTipoInstrumento;
				svs.IdEmisor = base.IdEmisor;
				svs.IdMonedaInstrumento = base.IdMonedaInstrumento;
				svs.IdTipoInstrumento = base.IdTipoInstrumento;
				base = svs;
			}
			return base;
		}
		
		public InstrumentoClasificado obtenerClasificacionMenorRiesgoInstrumento(Long idInstrumento, Predicate<InstrumentoClasificado> predicado) {
			List<InstrumentoClasificado> clasificaciones = clasificacionesRiesgoPorIdInstrumento.getOrDefault(idInstrumento, Collections.emptyList());
			return clasificaciones.stream()
					.filter(predicado)
					.max(Comparator.comparingInt(t -> t.NivelClasificacionRiesgo)) // Si, a mayor nivel menor riesgo
					.orElse(null);
		}
	}
}
