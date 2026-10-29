package cl.bcs.mesa.SPT;

import cl.bcs.mesa.pojo.ConstantesMesa;
import cl.bcs.plataforma.lim.ConsultarLimitesCuentaPropia;
import cl.bcs.plataforma.lim.LimiteUtils;
import cl.bcs.plataforma.lim.pojo.*;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.*;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidarLimitesCuentaPropiaSpot implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ValidarLimitesCuentaPropiaSpot.class);
	
	private final String SQL_SELECT_OPERACION_SPOT_INGRESADA = loadResourceToString("cl/bcs/mesa/SPT/sql/ValidarLimitesCuentaPropiaSpot_SelectOperacionSpotIngresada.sql");
	
	private final ConstantesLimites _cl = new ConstantesLimites();
	private final ConstantesMesa _cm = new ConstantesMesa();
	
	
	private ContextoLogica contexto;
	
	public static class EntradaValidarLimitesCuentaPropiaSpot extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_UsaReglasNegocio;
		public String p_IdTipoProceso; // INGRESO / MODIDIFACION / ANULACION
		public List<OperacionSpot> p_OperacionesSpot;
		
		public EntradaValidarLimitesCuentaPropiaSpot() {
			p_OperacionesSpot = new ArrayList<>();
		}
	}
	
	public static class SalidaValidarLimitesCuentaPropiaSpot extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<Validacion> P_OUT_VALIDACIONES;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaValidarLimitesCuentaPropiaSpot() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_VALIDACIONES = new ArrayList<>();
			P_OUT_MENSAJES = new ArrayList<>();
		}
	}
	
	public static class OperacionSpot extends BcsPojo {
		public Long IdInterno;
		public Long IdOperacionSpot;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdMesa;
		public Long IdAgenteOperacion;
		public String IdTipoSpot;
		public String Estado;
		public String EstadoConfirmacion;
		
		// Clasificacion de limites
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String NemotecnicoInstrumento;
		public String CodigoOperador;
		public String IdTipoCanalIngreso;
		
		// Calculo de montos
		public String IdTipoOperacion; // Compra / Venta
		public String IdMonedaPrincipal;
		public BigDecimal MontoPrincipal;
		public String Origen; // ENTRADA / BD
	}
	
	private static class MontosClasificacion extends BcsPojo {
		public String Clasificacion;
		public BigDecimal MontoIntraday;
		public BigDecimal MontoOvernight;
		
		public MontosClasificacion() {
			Clasificacion = "";
			MontoIntraday = BigDecimal.ZERO;
			MontoOvernight = BigDecimal.ZERO;
		}
		
		public MontosClasificacion(String clasificacion) {
			Clasificacion = clasificacion;
			MontoIntraday = BigDecimal.ZERO;
			MontoOvernight = BigDecimal.ZERO;
		}
	}
	
	private static class ContextoLogica {
		public Connection Connection;
		public EntradaValidarLimitesCuentaPropiaSpot Entrada;
		public SalidaValidarLimitesCuentaPropiaSpot Salida;
		public java.sql.Timestamp FechaSistema;
		public java.sql.Timestamp FechaSistemaHabilAnterior;
		public Long IdPaisSistema;
		public Boolean ValidacionLimitesHabilitada;
		public CuentaPropia CuentaPropia;
		public Map<String, LimiteUtils.Instrumento> InstrumentoPorNemotecnico;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public SalidaValidarLimitesCuentaPropiaSpot logica(Connection conn, EntradaValidarLimitesCuentaPropiaSpot entrada) throws Exception {
		logger.info("INICIO::ValidarLimitesCuentaPropiaSpot");
		long inicio = System.currentTimeMillis();
		
		contexto = obtenerContexto(conn, entrada);
		if (contexto.ValidacionLimitesHabilitada) {
			List<OperacionSpot> operacionesEntrantes = obtenerOperacionesEntrantes();
			List<OperacionSpot> operacionesIngresadas = obtenerOperacionesIngresadas();
			
			Map<Long, List<Limite>> limitesPorIdInternoOperacionEntrante = obtenerLimitesCuentaPropia(operacionesEntrantes);
			Map<Long, Set<String>> clasificacionesPorIdInternoOperacion = obtenerClasificacionesPorOperacion(operacionesEntrantes);
			Map<String, MontosClasificacion> montosPorClasificacion = obtenerMontosPorClasificacion(operacionesIngresadas, operacionesEntrantes);
			
			contexto.Salida.P_OUT_VALIDACIONES = validarOperaciones(operacionesEntrantes, limitesPorIdInternoOperacionEntrante, montosPorClasificacion, clasificacionesPorIdInternoOperacion);
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ValidarLimitesCuentaPropiaSpot {}", fin - inicio);
		
		return contexto.Salida;
	}
	
	private ContextoLogica obtenerContexto(Connection conn, EntradaValidarLimitesCuentaPropiaSpot entrada) throws Exception {
		logger.debug("INICIO::ValidarLimitesCuentaPropiaSpot.obtenerContexto");
		long inicio = System.currentTimeMillis();
		
		ContextoLogica contexto = new ContextoLogica();
		contexto.Connection = conn;
		contexto.Entrada = entrada;
		contexto.IdPaisSistema = ConfiguracionSistema.getParametroConfiguracionLong(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		contexto.FechaSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);
		contexto.FechaSistemaHabilAnterior = obtenerDiaHabilAnterior(conn, contexto.IdPaisSistema, contexto.FechaSistema);
		contexto.ValidacionLimitesHabilitada = _cl.SI.equals(entrada.p_UsaReglasNegocio) && _cl.SI.equals(ConfiguracionSistema.getParametroConfiguracionString(contexto.Connection, contexto.Entrada.p_IdEmpresa, "LIMITE", "LIM.HabilitarConfiguracionLimite"));
		contexto.CuentaPropia = LimiteUtils.obtenerCuentaPropia(conn, entrada.p_IdEmpresa);
		contexto.InstrumentoPorNemotecnico = LimiteUtils.obtenerInstrumentoPorNemotecnico(conn, _cl.TIPO_INSTRUMENTO_SPOT);
		contexto.Salida = new SalidaValidarLimitesCuentaPropiaSpot();
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarLimitesCuentaPropiaSpot.obtenerContexto {}", fin - inicio);
		
		return contexto;
	}
	
	private java.sql.Timestamp obtenerDiaHabilAnterior(Connection conn, Long idPais, java.sql.Timestamp fecha) throws Exception {
		logger.debug("INICIO::ValidarLimitesCuentaPropiaSpot.obtenerDiaHabilAnterior");
		long inicio = System.currentTimeMillis();
		
		ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores entradaObtenerDiasHabilesAnteriores = new ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores();
		entradaObtenerDiasHabilesAnteriores.idPais = idPais;
		entradaObtenerDiasHabilesAnteriores.diaHabil = _cl.SI;
		entradaObtenerDiasHabilesAnteriores.soloCaeDiaHabil = _cl.NO;
		entradaObtenerDiasHabilesAnteriores.fechas = Stream.of(fecha)
				.map(u -> {
					ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores.FechasIn o = new ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores.FechasIn();
					o.fechaInicial = Fecha.toStringDate(u);
					o.numeroDias = 1L;
					return o;
				})
				.collect(Collectors.toList());
		ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores salidaObtenerDiasHabilesAnteriores = new ObtenerDiasHabilesAnteriores().logica(conn, entradaObtenerDiasHabilesAnteriores);
		java.sql.Timestamp maybeFechaAnterior = salidaObtenerDiasHabilesAnteriores.fechas.stream()
				.filter(u -> u.numeroDiasInicial == 1L && Fecha.toStringDate(fecha).equals(u.fechaInicial))
				.map(u -> u.fechaCalculada + " 00:00:00")
				.findFirst()
				.map(Fecha::toTimeStamp)
				.orElse(null);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarLimitesCuentaPropiaSpot.obtenerDiaHabilAnterior {}", fin - inicio);
		
		return maybeFechaAnterior;
	}
	
	private List<OperacionSpot> obtenerOperacionesEntrantes() {
		logger.trace("INICIO::ValidarLimitesCuentaPropiaSpot.obtenerOperacionesEntrantes");
		long inicio = System.currentTimeMillis();
		
		List<OperacionSpot> operaciones = contexto.Entrada.p_OperacionesSpot.stream()
				.filter(u -> contexto.CuentaPropia.IdCliente.equals(u.IdCliente))
				.map(u -> {
					OperacionSpot o = new OperacionSpot();
					o.IdInterno = u.IdInterno;
					o.IdOperacionSpot = u.IdOperacionSpot;
					o.IdCliente = u.IdCliente;
					o.IdPortafolio = u.IdPortafolio;
					o.IdMesa = u.IdMesa;
					o.IdAgenteOperacion = u.IdAgenteOperacion;
					o.IdTipoSpot = u.IdTipoSpot;
					o.Estado = u.Estado;
					o.EstadoConfirmacion = u.EstadoConfirmacion;
					o.IdInstrumento = u.IdInstrumento;
					o.IdTipoInstrumento = u.IdTipoInstrumento;
					o.NemotecnicoInstrumento = u.NemotecnicoInstrumento;
					o.CodigoOperador = u.CodigoOperador;
					o.IdTipoCanalIngreso = u.IdTipoCanalIngreso;
					o.IdTipoOperacion = u.IdTipoOperacion;
					o.IdMonedaPrincipal = u.IdMonedaPrincipal;
					o.MontoPrincipal = u.MontoPrincipal;
					o.Origen = _cl.ORIGEN_ENTRADA;
					return o;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimitesCuentaPropiaSpot.obtenerOperacionesEntrantes {}", fin - inicio);
		
		return operaciones;
	}
	
	
	private List<OperacionSpot> obtenerOperacionesIngresadas() throws Exception {
		logger.debug("INICIO::ValidarLimitesCuentaPropiaSpot.obtenerOperacionesIngresadas");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("FechaOperacionDesde", contexto.FechaSistemaHabilAnterior);
		params.put("FechaOperacionHasta", contexto.FechaSistema);
		params.put("Estado", _cl.ESTADO_VIGENTE);
		params.put("IdCliente", contexto.CuentaPropia.IdCliente);
		
		List<OperacionSpot> operaciones = new SQL().query(contexto.Connection, SQL_SELECT_OPERACION_SPOT_INGRESADA, params, OperacionSpot.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarLimitesCuentaPropiaSpot.obtenerOperacionesIngresadas {}", fin - inicio);
		
		return operaciones;
	}
	
	private Map<Long, List<Limite>> obtenerLimitesCuentaPropia(List<OperacionSpot> operacionesEntrada) throws SQLException {
		logger.debug("INICIO::ValidarLimitesCuentaPropiaSpot.obtenerLimitesCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		ConsultarLimitesCuentaPropia.EntradaConsultarLimitesCuentaPropia entradaConsultarLimitesCuentaPropia = new ConsultarLimitesCuentaPropia.EntradaConsultarLimitesCuentaPropia();
		entradaConsultarLimitesCuentaPropia.p_IdEmpresa = contexto.Entrada.p_IdEmpresa;
		entradaConsultarLimitesCuentaPropia.Clasificaciones = operacionesEntrada.stream()
				.map((o) -> {
					ConsultarLimitesCuentaPropia.ClasificacionLimite clasificacionLimite = new ConsultarLimitesCuentaPropia.ClasificacionLimite();
					clasificacionLimite.IdInterno = o.IdInterno;
					clasificacionLimite.CodigoOperador = o.CodigoOperador;
					clasificacionLimite.IdInstrumento = o.IdInstrumento;
					clasificacionLimite.IdTipoInstrumento = _cl.TIPO_INSTRUMENTO_SPOT;
					clasificacionLimite.IdClasificacionTipoInstrumento = _cl.CLASIFICACION_TIPO_INSTRUMENTO_SPOT;
					clasificacionLimite.IdTipoOperacion = o.IdTipoOperacion; //null;
					clasificacionLimite.IdMonedaOperacion = o.IdMonedaPrincipal;
					clasificacionLimite.IdTipoCanalIngreso = o.IdTipoCanalIngreso;
					clasificacionLimite.IdTipoLimite = _cl.TIPO_LIMITE_SPOT;
					return clasificacionLimite;
				})
				.collect(Collectors.toList());
		
		ConsultarLimitesCuentaPropia.SalidaConsultarLimitesCuentaPropia salidaConsultarLimitesCuentaPropia = new ConsultarLimitesCuentaPropia().logica(contexto.Connection, entradaConsultarLimitesCuentaPropia);
		Map<Long, List<Limite>> limitesPorIdInterno = salidaConsultarLimitesCuentaPropia.LimitesPorIdInterno;
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarLimitesCuentaPropiaSpot.obtenerLimitesCuentaPropia {}", fin - inicio);
		
		return limitesPorIdInterno;
	}
	
	private String obtenerClasificacionEvaluacion(OperacionSpot operacionSpot, Limite limite) {
		logger.trace("INICIO::ValidarLimitesCuentaPropiaSpot.obtenerClasificacionEvaluacion");
		long inicio = System.currentTimeMillis();
		
		String clasificacion = operacionSpot.IdTipoInstrumento;
		clasificacion += "-" + operacionSpot.IdMonedaPrincipal;
		if (_cl.CODIGO_NUMERICO_NO_APLICA.equals(limite.IdInstrumento) == false) {
			clasificacion += "-" + (_cl.COMODIN_INSTRUMENTO.equals(limite.IdInstrumento) ? operacionSpot.IdInstrumento : limite.IdInstrumento);
		}
		if (_cl.CODIGO_NO_APLICA.equals(limite.IdCanalIngreso) == false) {
			clasificacion += "-" + (_cl.COMODIN_CANAL.equals(limite.IdCanalIngreso) ? operacionSpot.IdTipoCanalIngreso : limite.IdCanalIngreso);
		}
		if (_cl.CODIGO_NO_APLICA.equals(limite.CodigoOperador) == false) {
			clasificacion += "-" + ((_cl.COMODIN_OPERADOR.equals(limite.CodigoOperador) || (limite.CodigoOperador.equals("TODOS"))) ? operacionSpot.CodigoOperador : limite.CodigoOperador);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimitesCuentaPropiaSpot.obtenerClasificacionEvaluacion {}", fin - inicio);
		
		return clasificacion;
	}
	
	private Set<String> obtenerClasificacionesPosibles(OperacionSpot operacion) {
		logger.trace("INICIO::ValidarLimitesCuentaPropiaSpot.obtenerClasificacionesPosibles");
		long inicio = System.currentTimeMillis();
		
		// clasificacion = IdTipoInstrumento+IdMonedaPrincipal+IdInstrumento?+IdCanalIngreso?+CodigoOperador?
		String base = _cl.TIPO_INSTRUMENTO_SPOT + "-" + operacion.IdMonedaPrincipal;
		Stream<String> criteriosOpcionales = Stream.of(String.valueOf(operacion.IdInstrumento), operacion.IdTipoCanalIngreso, operacion.CodigoOperador);
		Set<String> clasificaciones = criteriosOpcionales.collect(
				() -> Stream.of(base).collect(Collectors.toSet()),
				(set, criterio) -> set.addAll(set.stream().map(u -> u + "-" + criterio).collect(Collectors.toList())),
				Set::addAll
		);
		
		return clasificaciones;
	}
	
	private Map<Long, Set<String>> obtenerClasificacionesPorOperacion(List<OperacionSpot> operaciones) {
		logger.trace("INICIO::ValidarLimitesCuentaPropiaSpot.obtenerClasificacionesPorOperacion");
		long inicio = System.currentTimeMillis();
		
		Map<Long, Set<String>> clasificacionesPorIdInterno = operaciones.stream()
				.map(operacion -> Pair.of(operacion, obtenerClasificacionesPosibles(operacion)))
				.collect(Collectors.toMap(par -> par.getLeft().IdInterno, Pair::getRight));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimitesCuentaPropiaSpot.obtenerClasificacionesPorOperacion {}", fin - inicio);
		
		return clasificacionesPorIdInterno;
	}
	
	private BigDecimal obtenerFactorAditivoOperacion(OperacionSpot operacion) {
		logger.trace("INICIO::ValidarLimitesCuentaPropiaSpot.obtenerFactorAditivoOperacion");
		long inicio = System.currentTimeMillis();
		
		BigDecimal factor = BigDecimal.ONE;
		boolean esVentaIngresada = _cl.ORIGEN_BD.equals(operacion.Origen) && _cl.TIPO_OPERACION_VENTA.equals(operacion.IdTipoOperacion);
		boolean esVentaEntranteNuevaOModificada = _cl.ORIGEN_ENTRADA.equals(operacion.Origen) && (_cl.PROCESO_INGRESAR.equals(contexto.Entrada.p_IdTipoProceso) || _cl.PROCESO_MODIFICACION.equals(contexto.Entrada.p_IdTipoProceso)) && _cl.TIPO_OPERACION_VENTA.equals(operacion.IdTipoOperacion);
		boolean esCompraAnulada = _cl.ORIGEN_ENTRADA.equals(operacion.Origen) && _cl.TIPO_OPERACION_COMPRA.equals(operacion.IdTipoOperacion) && _cl.PROCESO_ANULAR.equals(contexto.Entrada.p_IdTipoProceso);
		if (esVentaIngresada || esVentaEntranteNuevaOModificada || esCompraAnulada) {
			factor = BigDecimal.ONE.negate();
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimitesCuentaPropiaSpot.obtenerFactorAditivoOperacion {}", fin - inicio);
		
		return factor;
	}
	
	private Map<String, MontosClasificacion> obtenerMontosPorClasificacion(List<OperacionSpot> operacionesIngresadas, List<OperacionSpot> operacionesEntrantes) {
		logger.trace("INICIO::ValidarLimitesCuentaPropiaSpot.obtenerMontosPorClasificacion");
		long inicio = System.currentTimeMillis();
		
		Set<Long> idsOperacionesModificadas = operacionesEntrantes.stream()
				.filter(u -> Objects.nonNull(u.IdOperacionSpot) && u.IdOperacionSpot > 0L)
				.map(u -> u.IdOperacionSpot)
				.collect(Collectors.toSet());
		
		Map<String, MontosClasificacion> montosPorClasificacion = new HashMap<>(operacionesIngresadas.size() * 8); // Cada operacion genera 8 posibles clasificaciones
		operacionesIngresadas.stream()
				.filter(operacion -> idsOperacionesModificadas.contains(operacion.IdOperacionSpot) == false)
				.flatMap(operacion -> obtenerClasificacionesPosibles(operacion).stream().map(clasificacion -> Pair.of(operacion, clasificacion)))
				.forEach(par -> {
					OperacionSpot operacion = par.getLeft();
					String clasificacion = par.getRight();
					
					BigDecimal factorAditivo = obtenerFactorAditivoOperacion(operacion);
					BigDecimal montoAcumular = factorAditivo.multiply(operacion.MontoPrincipal);
					
					MontosClasificacion montos = montosPorClasificacion.computeIfAbsent(clasificacion, MontosClasificacion::new);
					if (_cm.TIPO_SPOT_INTRADAY.equals(operacion.IdTipoSpot)) {
						montos.MontoIntraday = montos.MontoIntraday.add(montoAcumular);
					} else if (_cm.TIPO_SPOT_OVERNIGHT.equals(operacion.IdTipoSpot)) {
						montos.MontoOvernight = montos.MontoOvernight.add(montoAcumular);
					}
				});
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimitesCuentaPropiaSpot.obtenerMontosPorClasificacion {}", fin - inicio);
		
		return montosPorClasificacion;
	}
	
	private List<Validacion> validarOperaciones(List<OperacionSpot> operaciones, Map<Long, List<Limite>> limitesPorIdInterno, Map<String, MontosClasificacion> montosPorClasificacion, Map<Long, Set<String>> clasificacionesPorIdInternoOperacion) {
		logger.trace("INICIO::ValidarLimitesCuentaPropiaSpot.validarOperaciones");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validacionesLimites = operaciones.stream()
				.map(operacionEntrante -> {
					List<Limite> limites = limitesPorIdInterno.getOrDefault(operacionEntrante.IdInterno, Collections.emptyList());
					Set<String> clasificaciones = clasificacionesPorIdInternoOperacion.getOrDefault(operacionEntrante.IdInterno, Collections.emptySet());
					return validarLimitesOperacion(operacionEntrante, limites, clasificaciones, montosPorClasificacion);
				})
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimitesCuentaPropiaSpot.validarOperaciones {}", fin - inicio);
		
		return validacionesLimites;
	}
	
	private Optional<Validacion> validarLimitesOperacion(OperacionSpot operacion, List<Limite> limites, Set<String> clasificaciones, Map<String, MontosClasificacion> montosPorClasificacion) {
		logger.trace("INICIO::ValidarLimitesCuentaPropiaSpot.validarLimitesOperacion");
		long inicio = System.currentTimeMillis();
		
		Optional<Validacion> maybeValidacion = limites.stream()
				.map(limite -> {
					LimiteUtils.EntradaObtenerValidacionLimite entradaObtenerValidacionLimite = null;
					if (superaLimite(operacion, limite, clasificaciones, montosPorClasificacion)) {
						entradaObtenerValidacionLimite = new LimiteUtils.EntradaObtenerValidacionLimite();
						entradaObtenerValidacionLimite.Folio = null;
						entradaObtenerValidacionLimite.Limite = limite;
						entradaObtenerValidacionLimite.NemoInstrumento = Optional.ofNullable(contexto.InstrumentoPorNemotecnico.get(operacion.NemotecnicoInstrumento)).map(u -> u.RazonSocial).orElse(operacion.NemotecnicoInstrumento);
						entradaObtenerValidacionLimite.IdInterno = operacion.IdInterno;
						entradaObtenerValidacionLimite.Connection = contexto.Connection;
						entradaObtenerValidacionLimite.Proceso = contexto.Entrada.p_IdTipoProceso;
						entradaObtenerValidacionLimite.IdEmpresa = contexto.Entrada.p_IdEmpresa;
					}
					return entradaObtenerValidacionLimite;
				})
				.filter(Objects::nonNull)
				.findFirst()
				.flatMap(LimiteUtils::obtenerValidacionLimite);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimitesCuentaPropiaSpot.validarLimitesOperacion {}", fin - inicio);
		
		return maybeValidacion;
	}
	
	private boolean superaLimite(OperacionSpot operacion, Limite limite, Set<String> clasificaciones, Map<String, MontosClasificacion> montosPorClasificacion) {
		logger.trace("INICIO::ValidarLimitesCuentaPropiaSpot.superaLimite");
		long inicio = System.currentTimeMillis();
		
		String clasificacionEvaluar = obtenerClasificacionEvaluacion(operacion, limite);
		MontosClasificacion montosEvaluar = montosPorClasificacion.computeIfAbsent(clasificacionEvaluar, MontosClasificacion::new);
		List<MontosClasificacion> montosActualizar = clasificaciones.stream().map(clasificacion -> montosPorClasificacion.computeIfAbsent(clasificacion, MontosClasificacion::new)).collect(Collectors.toList());
		UnaryProcedure<BigDecimal> actualizarMontos = (montoAcumular) -> {};
		
		BigDecimal montoLimite = null;
		BigDecimal montoUtilizado = BigDecimal.ZERO;
		
		if (_cm.TIPO_SPOT_INTRADAY.equals(operacion.IdTipoSpot)) {
			montoLimite = limite.MontoIntraday;
			montoUtilizado = montosEvaluar.MontoIntraday;
			actualizarMontos = (montoAcumular) -> montosActualizar.forEach(montos -> montos.MontoIntraday = montos.MontoIntraday.add(montoAcumular));
		} else if (_cm.TIPO_SPOT_OVERNIGHT.equals(operacion.IdTipoSpot)) {
			montoLimite = limite.MontoOvernight;
			montoUtilizado = montosEvaluar.MontoOvernight;
			actualizarMontos = (montoAcumular) -> montosActualizar.forEach(montos -> montos.MontoOvernight = montos.MontoOvernight.add(montoAcumular));
		}
		
		BigDecimal factorAditivoOperacion = obtenerFactorAditivoOperacion(operacion);
		BigDecimal montoAcumular = factorAditivoOperacion.multiply(operacion.MontoPrincipal); 
		BigDecimal nuevoMonto = montoAcumular.add(montoUtilizado);
		boolean limiteSuperado = Objects.nonNull(montoLimite) && nuevoMonto.compareTo(montoLimite) > 0;
		if (limiteSuperado == false) {
			actualizarMontos.apply(montoAcumular);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimitesCuentaPropiaSpot.superaLimite {}", fin - inicio);
		
		return limiteSuperado;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			try {
				String json = "{\"p_IdEmpresa\":1, \"p_IdPais\":44,\"p_OperacionesSpot\":[]}";
				EntradaValidarLimitesCuentaPropiaSpot entradaValidarLimitesCuentaPropiaSpot = Utils.getGsonBuilder().fromJson(json, EntradaValidarLimitesCuentaPropiaSpot.class);
				SalidaValidarLimitesCuentaPropiaSpot salidaValidarLimitesCuentaPropiaSpot = new ValidarLimitesCuentaPropiaSpot().logica(conn, entradaValidarLimitesCuentaPropiaSpot);
				System.out.println(salidaValidarLimitesCuentaPropiaSpot);
			} finally {
				conn.rollback();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN TEST ==");
	}
}
