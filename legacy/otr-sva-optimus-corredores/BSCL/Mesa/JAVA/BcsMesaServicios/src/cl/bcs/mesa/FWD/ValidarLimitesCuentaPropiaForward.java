package cl.bcs.mesa.FWD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cl.bcs.plataforma.lim.pojo.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.lim.ConsultarLimitesCuentaPropia;
import cl.bcs.plataforma.lim.LimiteUtils;
import cl.bcs.plataforma.pla.ObtenerDiasHabilesAnteriores;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ValidarLimitesCuentaPropiaForward implements JavaCall {	
	private static final Logger logger = LogManager.getLogger(ValidarLimitesCuentaPropiaForward.class);
	
	private final String SQL_SELECT_OPERACIONES_FORWARD = loadResourceToString("cl/bcs/mesa/FWD/sql/ValidarLimitesCuentaPropiaForward_SelectLimitesFWD.sql");
	
	private final ConstantesLimites _cl = new ConstantesLimites();
	private ContextoLogica contexto;

	public static class EntradaValidarLimitesCuentaPropiaForward{
		public Long			p_IdUsuario;
		public Long			p_IdEmpresa;
		public Long			p_IdPais;
		public String		p_IdTipoProceso;
		public String		p_UsaReglasNegocio;
		public List<OperacionForward> p_OperacionesFwd;
		
		public EntradaValidarLimitesCuentaPropiaForward() {
			p_OperacionesFwd = new ArrayList<>();
		}
		
	}
	
	public static class SalidaValidarLimitesCuentaPropiaForward extends BcsPojo{
		
		public List<Validacion> P_OUT_VALIDACIONES;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaValidarLimitesCuentaPropiaForward() {
			P_OUT_VALIDACIONES = new ArrayList<>();
			P_OUT_MENSAJES = new ArrayList<>();
		}
		
	}
	
	public static class OperacionForward extends BcsPojo{
		
		public Long IdInterno;
		public Long IdOperacionFwd;
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdAgenteOperacion;
		public String IdTipoProducto;
		public String IdEstado;
		
		// Clasificacion de limites
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String NemotecnicoInstrumento;
		public String CodigoOperador;
		public String IdTipoCanalIngreso;
		
		// Calculo de montos
		public String IdTipoOperacion; // Compra / Venta
		public String IdMonedaPrincipal;
		public BigDecimal MontoOperacion;
		public java.sql.Timestamp FechaOperacion;
		public java.sql.Timestamp FechaVencimiento;
		public java.sql.Timestamp FechaLiquidacion;
		public Long PlazoDias;
		public String Origen; // ENTRADA / BD
		
	}
	
	private static class MontosClasificacion extends BcsPojo {
		public String Clasificacion;
		public BigDecimal  MontoTotal;
		
		public MontosClasificacion() {
			Clasificacion = "";
			MontoTotal = BigDecimal.ZERO;
		}
		
		public MontosClasificacion(String clasificacion) {
			Clasificacion = clasificacion;
			MontoTotal = BigDecimal.ZERO;
		}
	}
	
	private static class ContextoLogica {
		public Connection Connection;
		public EntradaValidarLimitesCuentaPropiaForward Entrada;
		public String FechaSistema;
		public Boolean ValidacionLimitesHabilitada;
		public CuentaPropia CuentaPropia;
		public Map<String, LimiteUtils.Instrumento> InstrumentoPorNemotecnico;
		
		public ContextoLogica() {}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	private ContextoLogica obtenerContexto(Connection conn, EntradaValidarLimitesCuentaPropiaForward entrada) throws Exception{
		logger.info("INICIO::ValidarLimitesCuentaPropiaForward.obtenerContexto");
		long inicio = System.currentTimeMillis();
		
		ContextoLogica contexto = new ContextoLogica();
		contexto.Connection = conn;
		contexto.Entrada = entrada;
		contexto.FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		contexto.ValidacionLimitesHabilitada = _cl.SI.equals(entrada.p_UsaReglasNegocio) && _cl.SI.equals(ConfiguracionSistema.getParametroConfiguracionString(contexto.Connection, contexto.Entrada.p_IdEmpresa, "LIMITE", "LIM.HabilitarConfiguracionLimite"));
		contexto.CuentaPropia = LimiteUtils.obtenerCuentaPropia(conn, entrada.p_IdEmpresa);
		contexto.InstrumentoPorNemotecnico = LimiteUtils.obtenerInstrumentoPorNemotecnico(conn, _cl.TIPO_INSTRUMENTO_FWD);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ValidarLimitesCuentaPropiaForward.obtenerContexto {}", fin - inicio);
		
		return contexto;
	}
	
	public SalidaValidarLimitesCuentaPropiaForward logica(Connection conn, EntradaValidarLimitesCuentaPropiaForward entrada) throws Exception {		
		logger.info("INICIO::ValidarLimitesCuentaPropiaForward");
		long inicio = System.currentTimeMillis();
		
		contexto = obtenerContexto(conn, entrada);
		SalidaValidarLimitesCuentaPropiaForward salida = new SalidaValidarLimitesCuentaPropiaForward();
		
		if(ConfiguracionSistema.getParametroConfiguracionString(contexto.Connection, contexto.Entrada.p_IdEmpresa, "LIMITE", "LIM.HabilitarConfiguracionLimite").equals("S")) {
			
			List<OperacionForward> operacionesEntrada = entrada.p_OperacionesFwd.stream()
					.filter(op -> contexto.CuentaPropia.IdCliente.equals(op.IdCliente))
					.collect(Collectors.toList());
			List<OperacionForward> operacionesYaIngresadas = obtenerOperacionesYaIngresadas(operacionesEntrada);			
			Map<Long, List<Limite>> limitesPorIdInternoOperacionEntrante = obtenerLimitesCuentaPropia(entrada,operacionesEntrada);
			Map<Long, Set<String>> clasificacionesPorIdInternoOperacion = obtenerClasificacionesPorOperacion(operacionesEntrada);
			Map<String,MontosClasificacion> montosPorClasificacion = obtenerMontosPorClasificacion(operacionesEntrada, operacionesYaIngresadas);
			
			salida.P_OUT_VALIDACIONES  = validarOperaciones(operacionesEntrada, limitesPorIdInternoOperacionEntrante, montosPorClasificacion, clasificacionesPorIdInternoOperacion);
			
			salida.P_OUT_MENSAJES = salida.P_OUT_VALIDACIONES.stream()
					.map(v -> v.Mensaje)
					.collect(Collectors.toList());
										
		}		
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ValidarLimitesCuentaPropiaForward {}", fin - inicio);
		
		return salida;
	}
	
	private List<OperacionForward> obtenerOperacionesYaIngresadas(List<OperacionForward> operacionesEntrantes) throws Exception{
		logger.debug("INICIO::ValidarLimitesCuentaPropiaForward.obtenerOperacionesYaIngresadas");
		long inicio = System.currentTimeMillis();
		
		ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores entradaObtenerDiasHabilesAnteriores = new ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores();
		entradaObtenerDiasHabilesAnteriores.idPais = contexto.Entrada.p_IdPais;
		entradaObtenerDiasHabilesAnteriores.diaHabil = _cl.SI;
		entradaObtenerDiasHabilesAnteriores.soloCaeDiaHabil = _cl.NO;
		entradaObtenerDiasHabilesAnteriores.fechas = Stream.of(contexto.FechaSistema)
				.map(u -> {
					ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores.FechasIn o = new ObtenerDiasHabilesAnteriores.InObtenerDiasHabilesAnteriores.FechasIn();
					o.fechaInicial = u;
					o.numeroDias = 1L;
					return o;
				})
				.collect(Collectors.toList());
		ObtenerDiasHabilesAnteriores.OutObtenerDiasHabilesAnteriores salidaObtenerDiasHabilesAnteriores = new ObtenerDiasHabilesAnteriores().logica(contexto.Connection, entradaObtenerDiasHabilesAnteriores);
		Optional<java.sql.Timestamp> maybeDiaHabilAnteriorFechaSistema = salidaObtenerDiasHabilesAnteriores.fechas.stream()
				.filter(u -> u.numeroDiasInicial == 1L && contexto.FechaSistema.equals(u.fechaInicial))
				.map(u -> u.fechaCalculada + " 00:00:00")
				.findFirst()
				.map(Fecha::toTimeStamp);
		// TODO: Replace with handled error
		if (maybeDiaHabilAnteriorFechaSistema.isPresent() == false) {
			throw new Exception();
		}
		
		List<String> canalesEntrantes = operacionesEntrantes.stream().map(u -> u.IdTipoCanalIngreso).distinct().collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdTipoCanalIngreso", canalesEntrantes);
		params.put("FechaOperacion", contexto.FechaSistema);
		params.put("IdEstado", _cl.ESTADO_VIGENTE);
		
		List<OperacionForward> operaciones = new SQL().query(contexto.Connection, SQL_SELECT_OPERACIONES_FORWARD, params, OperacionForward.class);
		for(OperacionForward op : operaciones) {
			op.Origen = _cl.ORIGEN_BD;
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarLimitesCuentaPropiaForward.obtenerOperacionesYaIngresadas {}", fin - inicio);
		return operaciones;
	}
	
	private Map<Long, List<Limite>> obtenerLimitesCuentaPropia(EntradaValidarLimitesCuentaPropiaForward entrada, List<OperacionForward> operacionesEntrada) throws SQLException{
		logger.debug("INICIO::ValidarLimitesCuentaPropiaForward.obtenerLimitesCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		ConsultarLimitesCuentaPropia.EntradaConsultarLimitesCuentaPropia entradaConsultarLimitesCuentaPropia = new ConsultarLimitesCuentaPropia.EntradaConsultarLimitesCuentaPropia();
		entradaConsultarLimitesCuentaPropia.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarLimitesCuentaPropia.Clasificaciones = operacionesEntrada.stream()
				.map((o) -> {
					ConsultarLimitesCuentaPropia.ClasificacionLimite clasificacionLimite = new ConsultarLimitesCuentaPropia.ClasificacionLimite();
					clasificacionLimite.IdInterno = o.IdInterno;
					clasificacionLimite.CodigoOperador = o.CodigoOperador;
					clasificacionLimite.IdTipoInstrumento = _cl.TIPO_INSTRUMENTO_FWD;
					clasificacionLimite.IdClasificacionTipoInstrumento = _cl.CLASIFICACION_TIPO_INSTRUMENTO_FORWARD;
					clasificacionLimite.IdTipoOperacion = o.IdTipoOperacion; //null;
					clasificacionLimite.IdMonedaOperacion = o.IdMonedaPrincipal;
					clasificacionLimite.IdTipoCanalIngreso = o.IdTipoCanalIngreso;
					clasificacionLimite.IdTipoLimite = _cl.TIPO_LIMITE_FWD;
					clasificacionLimite.IdInstrumento = o.IdInstrumento;
					return clasificacionLimite;
				})
				.collect(Collectors.toList());

		ConsultarLimitesCuentaPropia.SalidaConsultarLimitesCuentaPropia salidaConsultarLimitesCuentaPropia = new ConsultarLimitesCuentaPropia().logica(contexto.Connection, entradaConsultarLimitesCuentaPropia);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ValidarLimitesCuentaPropiaForward.obtenerLimitesCuentaPropia {}", fin - inicio);
		
		return salidaConsultarLimitesCuentaPropia.LimitesPorIdInterno;
	}
	
	private Map<Long, Set<String>> obtenerClasificacionesPorOperacion(List<OperacionForward> operaciones) {
		logger.trace("INICIO::ValidarLimitesCuentaPropiaForward.obtenerClasificacionesPorOperacion");
		long inicio = System.currentTimeMillis();
		
		Map<Long, Set<String>> clasificacionesPorIdInterno = operaciones.stream()
				.map(operacion -> Pair.of(operacion, obtenerClasificacionesPosibles(operacion)))
				.collect(Collectors.toMap(
						par -> par.getLeft().IdInterno,
						Pair::getRight
				));
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimitesCuentaPropiaForward.obtenerClasificacionesPorOperacion {}", fin - inicio);
		
		return clasificacionesPorIdInterno;
	}
	
	private Set<String> obtenerClasificacionesPosibles(OperacionForward operacion) {
		logger.trace("INICIO::ValidarLimitesCuentaPropiaForward.obtenerClasificacionesPosibles");
		long inicio = System.currentTimeMillis();
		
		// clasificacion = IdTipoInstrumento+IdMonedaPrincipal+IdInstrumento?+IdCanalIngreso?+CodigoOperador?
		String base = _cl.TIPO_INSTRUMENTO_FWD + "-" + operacion.IdMonedaPrincipal;
		Stream<String> criteriosOpcionales = Stream.of(String.valueOf(operacion.IdInstrumento), operacion.IdTipoCanalIngreso, operacion.CodigoOperador);
		Set<String> clasificaciones = criteriosOpcionales.collect(
				() -> Stream.of(base).collect(Collectors.toSet()),
				(set, criterio) -> set.addAll(set.stream().map(u -> u + "-" + criterio).collect(Collectors.toList())),
				Set::addAll
		);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimitesCuentaPropiaForward.obtenerClasificacionesPosibles {}", fin - inicio);
		
		return clasificaciones;
	}
	
	private String obtenerClasificacionEvaluacion(OperacionForward operacion, Limite limite) {
		logger.trace("INICIO::ValidarLimitesCuentaPropiaForward.obtenerClasificacionEvaluacion");
		long inicio = System.currentTimeMillis();
		
		String clasificacion = operacion.IdTipoInstrumento;
		clasificacion += "-" + operacion.IdMonedaPrincipal;
		if (_cl.CODIGO_NUMERICO_NO_APLICA.equals(limite.IdInstrumento) == false) {
			clasificacion += "-" + (_cl.COMODIN_INSTRUMENTO.equals(limite.IdInstrumento) ? operacion.IdInstrumento : limite.IdInstrumento);
		}
		if (_cl.CODIGO_NO_APLICA.equals(limite.IdCanalIngreso) == false) {
			clasificacion += "-" + (_cl.COMODIN_CANAL.equals(limite.IdCanalIngreso) ? operacion.IdTipoCanalIngreso : limite.IdCanalIngreso);
		}
		if (_cl.CODIGO_NO_APLICA.equals(limite.CodigoOperador) == false) {
			clasificacion += "-" + ((_cl.COMODIN_OPERADOR.equals(limite.CodigoOperador) || (limite.CodigoOperador.equals("TODOS"))) ? operacion.CodigoOperador : limite.CodigoOperador);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimitesCuentaPropiaForward.obtenerClasificacionEvaluacion {}", fin - inicio);
		
		return clasificacion;
		
	}
	
	private BigDecimal obtenerFactorAditivoOperacion(OperacionForward operacion) {
		logger.trace("INICIO::ValidarLimitesCuentaPropiaForward.obtenerFactorAditivoOperacion");
		long inicio = System.currentTimeMillis();
		
		BigDecimal factor = BigDecimal.ONE;
		boolean esVentaIngresada = _cl.ORIGEN_BD.equals(operacion.Origen) && _cl.TIPO_OPERACION_VENTA_FWD.equals(operacion.IdTipoOperacion);
		boolean esVentaEntranteNuevaOModificada = _cl.ORIGEN_ENTRADA.equals(operacion.Origen) && (_cl.PROCESO_INGRESAR.equals(contexto.Entrada.p_IdTipoProceso) || _cl.PROCESO_MODIFICACION.equals(contexto.Entrada.p_IdTipoProceso)) && _cl.TIPO_OPERACION_VENTA_FWD.equals(operacion.IdTipoOperacion);
		boolean esCompraAnulada = _cl.ORIGEN_ENTRADA.equals(operacion.Origen) && _cl.TIPO_OPERACION_COMPRA_FWD.equals(operacion.IdTipoOperacion) && _cl.PROCESO_ANULAR.equals(contexto.Entrada.p_IdTipoProceso);
		if (esVentaIngresada || esVentaEntranteNuevaOModificada || esCompraAnulada) {
			factor = BigDecimal.ONE.negate();
		} 
		 
		long fin = System.currentTimeMillis();
		logger.trace("INICIO::ValidarLimitesCuentaPropiaForward.obtenerFactorAditivoOperacion {}", fin - inicio);
		
		return factor;
	}
	
	private Map<String, MontosClasificacion> obtenerMontosPorClasificacion(List<OperacionForward> operacionesEntrantes, List<OperacionForward> operacionesYaIngresadas){
		logger.trace("INICIO::ValidarLimitesCuentaPropiaForward.obtenerMontosPorClasificacion");
		long inicio = System.currentTimeMillis();
		
		Set<Long> idsOperacionesModificadas = operacionesEntrantes.stream()
				.filter(u -> Objects.nonNull(u.IdOperacionFwd) && u.IdOperacionFwd > 0L)
				.map(u -> u.IdOperacionFwd)
				.collect(Collectors.toSet());
		
		Map<String, MontosClasificacion> montosPorClasificacion = new HashMap<>(operacionesYaIngresadas.size() * 8); // Cada operacion genera 8 posibles clasificaciones
		operacionesYaIngresadas.stream()
				.filter(operacion -> idsOperacionesModificadas.contains(operacion.IdOperacionFwd) == false)
				.flatMap(operacion -> obtenerClasificacionesPosibles(operacion).stream().map(clasificacion -> Pair.of(operacion, clasificacion)))
				.forEach(par -> {
					OperacionForward operacion = par.getLeft();
					String clasificacion = par.getRight();
					
					BigDecimal factorAditivo = obtenerFactorAditivoOperacion(operacion);
					BigDecimal montoAcumular = factorAditivo.multiply(operacion.MontoOperacion);
					
					MontosClasificacion montos = montosPorClasificacion.computeIfAbsent(clasificacion, MontosClasificacion::new);
					montos.MontoTotal = montos.MontoTotal.add(montoAcumular);
				});
		
		long fin = System.currentTimeMillis();
		logger.trace("INICIO::ValidarLimitesCuentaPropiaForward.obtenerMontosPorClasificacion {}", fin - inicio);
		
		return montosPorClasificacion;
	}

	private List<Validacion> validarOperaciones(List<OperacionForward> operaciones, Map<Long, List<Limite>> limitesPorIdInterno, Map<String, MontosClasificacion> montosPorClasificacion, Map<Long, Set<String>> clasificacionesPorIdInternoOperacion){
		logger.trace("INICIO::ValidarLimitesCuentaPropiaForward.validarOperaciones");
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
		logger.trace("FIN::ValidarLimitesCuentaPropiaForward.validarOperaciones {}", fin - inicio);
		return validacionesLimites;
	}
	
	private Optional<Validacion> validarLimitesOperacion(OperacionForward operacion, List<Limite> limites, Set<String> clasificaciones, Map<String, MontosClasificacion> montosPorClasificacion) {
		logger.trace("INICIO::ValidarLimitesCuentaPropiaForward.validarLimitesOperacion");
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
		logger.trace("FIN::ValidarLimitesCuentaPropiaForward.validarLimitesOperacion {}", fin - inicio);
		
		return maybeValidacion;
	}
	
	private boolean superaLimite(OperacionForward operacion, Limite limite, Set<String> clasificaciones, Map<String, MontosClasificacion> montosPorClasificacion) {
		logger.trace("INICIO::ValidarLimitesCuentaPropiaForward.superaLimite");
		long inicio = System.currentTimeMillis();
		
		String clasificacionEvaluar = obtenerClasificacionEvaluacion(operacion, limite);
		MontosClasificacion montosEvaluar = montosPorClasificacion.computeIfAbsent(clasificacionEvaluar, MontosClasificacion::new);
		List<MontosClasificacion> montosActualizar = clasificaciones.stream().map(clasificacion -> montosPorClasificacion.computeIfAbsent(clasificacion, MontosClasificacion::new)).collect(Collectors.toList());
		
		BigDecimal montoLimite = BigDecimal.ZERO;
		BigDecimal montoUtilizado = BigDecimal.ZERO;
		BigDecimal factorSigno = obtenerFactorAditivoOperacion(operacion);
		
		montoLimite = limite.MontoTotal;
		montoUtilizado = montosEvaluar.MontoTotal;
		
		BigDecimal montoAcumular = factorSigno.multiply(operacion.MontoOperacion);
		BigDecimal nuevoMonto = montoAcumular.add(montoUtilizado);
		boolean limiteSuperado = Objects.nonNull(montoLimite) && nuevoMonto.compareTo(montoLimite) > 0;
		
		if(limite.Plazo > 0) {
			LocalDate fechaLiquidacionMaxima = operacion.FechaVencimiento.toLocalDateTime().toLocalDate().plusDays(limite.Plazo);
			LocalDate fechaLiquidacionOperacion= operacion.FechaLiquidacion.toLocalDateTime().toLocalDate();
			limiteSuperado = limiteSuperado || fechaLiquidacionMaxima.compareTo(fechaLiquidacionOperacion) <= 0;
		}
				
		if (limiteSuperado == false) {
			montosActualizar.forEach(montos -> montos.MontoTotal = montos.MontoTotal.add(montoAcumular));
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ValidarLimitesCuentaPropiaForward.superaLimite {}", fin - inicio);
		
		return limiteSuperado;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			try {
				String jsonDesa = "{\"p_IdUsuario\":9,\"p_IdEmpresa\":1,\"p_IdPais\":44,\"p_IdTipoProceso\":\"COMFWD\",\"p_UsaReglasNegocio\":\"S\",\"p_OperacionesFwd\":[{\"IdInterno\":1,\"IdCliente\":1423323,\"IdPortafolio\":1451591,\"IdTipoProducto\":\"MEFW\",\"IdEstado\":\"VIG\",\"IdInstrumento\":346370,\"IdTipoInstrumento\":\"FWD\",\"NemotecnicoInstrumento\":\"SECANO\",\"IdTipoCanalIngreso\":\"MES\",\"IdTipoOperacion\":\"COMFWD\",\"IdMonedaPrincipal\":\"USD\",\"MontoOperacion\":200,\"FechaOperacion\":\"2022-05-31\",\"IdPersonaEjecutivo\":1774985}]}";
				EntradaValidarLimitesCuentaPropiaForward entrada = Utils.getGsonBuilder().fromJson(jsonDesa, EntradaValidarLimitesCuentaPropiaForward.class);
				ValidarLimitesCuentaPropiaForward validarLimitesCuentaPropiaForward = new ValidarLimitesCuentaPropiaForward();
				SalidaValidarLimitesCuentaPropiaForward salidaConsultarLimitesCuentaPropia = validarLimitesCuentaPropiaForward.logica(conn, entrada);
				System.out.println(salidaConsultarLimitesCuentaPropia);
			} finally {
				conn.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN TEST ==");
	}

}
