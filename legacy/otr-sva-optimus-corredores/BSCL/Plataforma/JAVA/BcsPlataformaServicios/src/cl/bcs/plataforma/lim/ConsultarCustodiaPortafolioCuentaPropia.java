package cl.bcs.plataforma.lim;

import cl.bcs.plataforma.lim.pojo.ChainableMapper;
import cl.bcs.plataforma.lim.pojo.ConstantesLimites;
import cl.bcs.plataforma.lim.pojo.CuentaPropia;
import cl.bcs.plataforma.lim.pojo.Mergeable;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.*;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class ConsultarCustodiaPortafolioCuentaPropia implements JavaCall {
	static final Logger logger = LogManager.getLogger(ConsultarCustodiaPortafolioCuentaPropia.class);
	
	private final String SQL_SELECT_CARTERA = loadResourceToString("cl/bcs/plataforma/lim/sql/ConsultarCustodiaPortafolioCuentaPropia_SelectCartera.sql");
	private final String SQL_SELECT_GARANTIA = loadResourceToString("cl/bcs/plataforma/lim/sql/ConsultarCustodiaPortafolioCuentaPropia_SelectGarantia.sql");
	private final String SQL_SELECT_TRANSITO = loadResourceToString("cl/bcs/plataforma/lim/sql/ConsultarCustodiaPortafolioCuentaPropia_SelectTransito.sql");
	private final String SQL_SELECT_ASIGNACION_SIN_FACTURAR = loadResourceToString("cl/bcs/plataforma/lim/sql/ConsultarCustodiaPortafolioCuentaPropia_SelectAsignacionSinFacturar.sql");
	
	private final ConstantesLimites _cl = new ConstantesLimites();
	
	private ContextoLogica contexto;
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public static class EntradaConsultarCustodiaPortafolioCuentaPropia extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		
		public String p_ClasificacionTipoInstrumento;
		public String p_TipoFechaConsulta;
		public String p_FechaConsulta;
		public String p_IncluirAsignacionesSinFacturar;
		public CuentaPropia p_CuentaPropia;
		
		public transient Map<Long, ClasificadorCustodia> p_ClasificadorCustodiaPorIdInterno;
		
		public EntradaConsultarCustodiaPortafolioCuentaPropia() {
			p_IdEmpresa = 0L;
			p_IdUsuario = 0L;
			p_IdModulo = "";
			p_UsaReglasNegocio = "N";
			p_ClasificacionTipoInstrumento = "";
			p_TipoFechaConsulta = "";
			p_FechaConsulta = "0001-01-01";
			p_IncluirAsignacionesSinFacturar = "N";
			p_ClasificadorCustodiaPorIdInterno = new HashMap<>();
			p_CuentaPropia = null;
		}
	}
	
	public static class SalidaConsultarCustodiaPortafolioCuentaPropia extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<PlataformaUtils.MensajeSalida> P_OUT_MENSAJES;
		public ConcurrentMap<Long, ConcurrentMap<String, Custodia>> p_out_CustodiaPorClasificacionPorIdInterno;
		public transient BuscadorCustodia BuscadorCustodia;
		
		public SalidaConsultarCustodiaPortafolioCuentaPropia() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			p_out_CustodiaPorClasificacionPorIdInterno = new ConcurrentHashMap<>();
			BuscadorCustodia = new BuscadorCustodia(new ConcurrentHashMap<>(), new HashMap<>());
		}
	}
	
	public static class ClasificablePorCustodia extends BcsPojo {
		public Long IdCliente;
		public Long IdPortafolio;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String IdClasificacionTipoInstrumento;
		public String Nemotecnico;
		public String IdEmisor;
		public String IdMonedaInstrumento;
		
		public ClasificablePorCustodia() {
			IdCliente = 0L;
			IdPortafolio = 0L;
			IdInstrumento = 0L;
			IdTipoInstrumento = "";
			IdClasificacionTipoInstrumento = "";
			Nemotecnico = "";
			IdEmisor = "";
			IdMonedaInstrumento = "";
		}
	}
	
	public static class Custodia extends ClasificablePorCustodia implements Mergeable<Custodia> {
		public BigDecimal TotalPosicion;
		public BigDecimal MontoCompra;
		
		public Custodia() {
			TotalPosicion = BigDecimal.ZERO;
			MontoCompra = BigDecimal.ZERO;
		}
		
		public synchronized void syncAcumularCustodia(BigDecimal posicion, BigDecimal montoCompra) {
			acumularPosicion(posicion);
			acumularMontoCompra(montoCompra);
		}
		
		private void acumularPosicion(BigDecimal incremento) {
			TotalPosicion = TotalPosicion.add(incremento);
		}
		
		private void acumularMontoCompra(BigDecimal incremento) {
			MontoCompra = MontoCompra.add(incremento);
		}
		
		@Override
		public Custodia merge(Custodia rhs) {
			if (IdCliente == null || IdCliente == 0L) {
				IdCliente = rhs.IdCliente;
			}
			if (IdPortafolio == null || IdPortafolio == 0L) {
				IdPortafolio = rhs.IdPortafolio;
			}
			if (IdInstrumento == null || IdInstrumento == 0L) {
				IdInstrumento = rhs.IdInstrumento;
			}
			if (StringUtils.isNullOrBlank(Nemotecnico)) {
				Nemotecnico = rhs.Nemotecnico;
			}
			if (StringUtils.isNullOrBlank(IdMonedaInstrumento)) {
				IdMonedaInstrumento = rhs.IdMonedaInstrumento;
			}
			if (StringUtils.isNullOrBlank(IdEmisor)) {
				IdEmisor = rhs.IdEmisor;
			}
			if (StringUtils.isNullOrBlank(IdTipoInstrumento)) {
				IdTipoInstrumento = rhs.IdTipoInstrumento;
			}
			if (StringUtils.isNullOrBlank(IdClasificacionTipoInstrumento)) {
				IdClasificacionTipoInstrumento = rhs.IdClasificacionTipoInstrumento;
			}
			return this;
		}
	}
	
	public static class ClasificadorCustodiaBuilder {
		private final ClasificadorCustodia ClasificadorCustodia;
		
		public ClasificadorCustodiaBuilder() {
			ClasificadorCustodia = new ClasificadorCustodia();
		}
		
		public ClasificadorCustodiaBuilder porCliente() {
			ClasificadorCustodia.obtenerLlaveClasificacion = ClasificadorCustodia.obtenerLlaveClasificacion.andThen(custodia -> new LlaveClasificacion(String.valueOf(custodia.IdCliente)));
			ClasificadorCustodia.obtenerCustodiaClasificacion = ClasificadorCustodia.obtenerCustodiaClasificacion.andThen(clasificable -> {
				Custodia custodia = new Custodia();
				custodia.IdCliente = clasificable.IdCliente;
				return custodia;
			});
			return this;
		}
		
		public ClasificadorCustodiaBuilder porPortafolio() {
			ClasificadorCustodia.obtenerLlaveClasificacion = ClasificadorCustodia.obtenerLlaveClasificacion.andThen(custodia -> new LlaveClasificacion(String.valueOf(custodia.IdPortafolio)));
			ClasificadorCustodia.obtenerCustodiaClasificacion = ClasificadorCustodia.obtenerCustodiaClasificacion.andThen(clasificable -> {
				Custodia custodia = new Custodia();
				custodia.IdPortafolio = clasificable.IdPortafolio;
				return custodia;
			});
			return this;
		}
		
		public ClasificadorCustodiaBuilder porInstrumento() {
			ClasificadorCustodia.obtenerLlaveClasificacion = ClasificadorCustodia.obtenerLlaveClasificacion.andThen(custodia -> new LlaveClasificacion(String.valueOf(custodia.IdInstrumento)));
			ClasificadorCustodia.obtenerCustodiaClasificacion = ClasificadorCustodia.obtenerCustodiaClasificacion.andThen(clasificable -> {
				Custodia custodia = new Custodia();
				custodia.IdInstrumento = clasificable.IdInstrumento;
				return custodia;
			});
			return this;
		}
		
		public ClasificadorCustodiaBuilder porTipoInstrumento() {
			ClasificadorCustodia.obtenerLlaveClasificacion = ClasificadorCustodia.obtenerLlaveClasificacion.andThen(custodia -> new LlaveClasificacion(String.valueOf(custodia.IdTipoInstrumento)));
			ClasificadorCustodia.obtenerCustodiaClasificacion = ClasificadorCustodia.obtenerCustodiaClasificacion.andThen(clasificable -> {
				Custodia custodia = new Custodia();
				custodia.IdTipoInstrumento = clasificable.IdTipoInstrumento;
				return custodia;
			});
			return this;
		}
		
		public ClasificadorCustodiaBuilder porClasificacionTipoInstrumento() {
			ClasificadorCustodia.obtenerLlaveClasificacion = ClasificadorCustodia.obtenerLlaveClasificacion.andThen(custodia -> new LlaveClasificacion(String.valueOf(custodia.IdClasificacionTipoInstrumento)));
			ClasificadorCustodia.obtenerCustodiaClasificacion = ClasificadorCustodia.obtenerCustodiaClasificacion.andThen(clasificable -> {
				Custodia custodia = new Custodia();
				custodia.IdClasificacionTipoInstrumento = clasificable.IdClasificacionTipoInstrumento;
				return custodia;
			});
			return this;
		}
		
		public ClasificadorCustodiaBuilder porNemo() {
			ClasificadorCustodia.obtenerLlaveClasificacion = ClasificadorCustodia.obtenerLlaveClasificacion.andThen(custodia -> new LlaveClasificacion(String.valueOf(custodia.Nemotecnico)));
			ClasificadorCustodia.obtenerCustodiaClasificacion = ClasificadorCustodia.obtenerCustodiaClasificacion.andThen(clasificable -> {
				Custodia custodia = new Custodia();
				custodia.Nemotecnico = clasificable.Nemotecnico;
				return custodia;
			});
			return this;
		}
		
		public ClasificadorCustodiaBuilder porMonedaInstrumento() {
			ClasificadorCustodia.obtenerLlaveClasificacion = ClasificadorCustodia.obtenerLlaveClasificacion.andThen(custodia -> new LlaveClasificacion(custodia.IdMonedaInstrumento));
			ClasificadorCustodia.obtenerCustodiaClasificacion = ClasificadorCustodia.obtenerCustodiaClasificacion.andThen(clasificable -> {
				Custodia custodia = new Custodia();
				custodia.IdMonedaInstrumento = clasificable.IdMonedaInstrumento;
				return custodia;
			});
			return this;
		}
		
		public ClasificadorCustodiaBuilder porEmisor() {
			ClasificadorCustodia.obtenerLlaveClasificacion = ClasificadorCustodia.obtenerLlaveClasificacion.andThen(custodia -> new LlaveClasificacion(custodia.IdEmisor));
			ClasificadorCustodia.obtenerCustodiaClasificacion = ClasificadorCustodia.obtenerCustodiaClasificacion.andThen(clasificable -> {
				Custodia custodia = new Custodia();
				custodia.IdEmisor = clasificable.IdEmisor;
				return custodia;
			});
			return this;
		}
		
		public ClasificadorCustodia terminar() {
			return ClasificadorCustodia;
		}
	}
	
	public static class Cartera extends ClasificablePorCustodia {
		public String TablaOrigen;
		public String EsPagaderoEn;
		public String EsNominal;
		public String IdTipoOperacion;
		public String AccionCustodia;
		public String EsVentaDescubierta;
		public BigDecimal Cantidad;
		public BigDecimal CantidadNominal;
		public BigDecimal MontoCompra;
		
		public Cartera() {
			TablaOrigen = "";
			EsPagaderoEn = "";
			EsNominal = "";
			IdTipoOperacion = "";
			AccionCustodia = "";
			EsVentaDescubierta = "";
			Cantidad = BigDecimal.ZERO;
			CantidadNominal = BigDecimal.ZERO;
			MontoCompra = BigDecimal.ZERO;
		}
	}
	
	private static class LlaveClasificacion implements Mergeable<LlaveClasificacion> {
		private String Llave;
		
		public LlaveClasificacion() {
			Llave = "";
		}
		
		public LlaveClasificacion(String llave) {
			Llave = llave;
		}
		
		@Override
		public LlaveClasificacion merge(LlaveClasificacion rhs) {
			Llave += "+" + rhs.Llave;
			return this;
		}
	}
	
	public static class ClasificadorCustodia {
		private ChainableMapper<ClasificablePorCustodia, Custodia> obtenerCustodiaClasificacion;
		private ChainableMapper<ClasificablePorCustodia, LlaveClasificacion> obtenerLlaveClasificacion;
		
		public ClasificadorCustodia() {
			obtenerLlaveClasificacion = o -> new LlaveClasificacion();
			obtenerCustodiaClasificacion = o -> new Custodia();
		}
	}
	
	private static class ContextoLogica {
		private Connection Connection;
		private CuentaPropia CuentaPropia;
		private EntradaConsultarCustodiaPortafolioCuentaPropia Entrada;
		private SalidaConsultarCustodiaPortafolioCuentaPropia Salida;
		private Boolean IncluirAsignacionesSinFacturar;
		
		public ContextoLogica() {
			Entrada = new EntradaConsultarCustodiaPortafolioCuentaPropia();
			Salida = new SalidaConsultarCustodiaPortafolioCuentaPropia();
			CuentaPropia = new CuentaPropia();
		}
	}
	
	private static class ContextoClasificacion extends BcsPojo {
		public Long IdInterno;
		public ClasificadorCustodia ClasificadorCustodia;
		public List<Cartera> Carteras;
		public Cartera Cartera;
		public Custodia Custodia;
		
		public ContextoClasificacion() {
			IdInterno = 0L;
			ClasificadorCustodia = new ClasificadorCustodiaBuilder().terminar();
			Carteras = new ArrayList<>();
		}
	}
	
	private static class SumandosCartera extends BcsPojo {
		public BigDecimal Posicion;
		public BigDecimal MontoCompra;
		
		public SumandosCartera() {
			Posicion = BigDecimal.ZERO;
			MontoCompra = BigDecimal.ZERO;
		}
	}
	
	public static class BuscadorCustodia {
		private final ConcurrentMap<Long, ConcurrentMap<String, Custodia>> CustodiaPorClasificacionPorIdInterno;
		private final Map<Long, ClasificadorCustodia> ClasificadorPorIdInterno;
		
		public BuscadorCustodia(
				ConcurrentMap<Long, ConcurrentMap<String, Custodia>> custodiaPorClasificacionPorIdInterno,
				Map<Long, ClasificadorCustodia> clasificadorPorIdInterno
		) {
			CustodiaPorClasificacionPorIdInterno = custodiaPorClasificacionPorIdInterno;
			ClasificadorPorIdInterno = clasificadorPorIdInterno;
		}
		
		public Custodia obtenerCustodia(Long idInternoClasificador, ClasificablePorCustodia clasificable) {
			logger.trace("INICIO::ConsultarCustodiaPortafolioCuentaPropia.BuscadorCustodia.acumularCustodia");
			long inicio = System.currentTimeMillis();
			
			ClasificadorCustodia clasificador = ClasificadorPorIdInterno.get(idInternoClasificador);
			if (clasificador == null) {
				return null;
			}
			LlaveClasificacion clasificacion = clasificador.obtenerLlaveClasificacion.apply(clasificable);
			if (clasificacion == null) {
				return null;
			}
			Custodia custodia = CustodiaPorClasificacionPorIdInterno
					.computeIfAbsent(idInternoClasificador, k -> new ConcurrentHashMap<>())
					.computeIfAbsent(clasificacion.Llave, k -> new Custodia());
			
			long fin = System.currentTimeMillis();
			logger.trace("FIN::ConsultarCustodiaPortafolioCuentaPropia.BuscadorCustodia.acumularCustodia {}", fin - inicio);
			
			return custodia;
		}
		
		public void acumularCustodia(ClasificablePorCustodia clasificable, BigDecimal posicion, BigDecimal montoCompraMonedaSistema) {
			logger.trace("INICIO::ConsultarCustodiaPortafolioCuentaPropia.BuscadorCustodia.acumularCustodia");
			long inicio = System.currentTimeMillis();
			
			for (Long idInternoClasificador : ClasificadorPorIdInterno.keySet()) {
				obtenerCustodia(idInternoClasificador, clasificable).syncAcumularCustodia(posicion, montoCompraMonedaSistema);
			}
			
			long fin = System.currentTimeMillis();
			logger.trace("FIN::ConsultarCustodiaPortafolioCuentaPropia.BuscadorCustodia.acumularCustodia {}", fin - inicio);
		}
	}
	
	public SalidaConsultarCustodiaPortafolioCuentaPropia logica(Connection conn, EntradaConsultarCustodiaPortafolioCuentaPropia entrada) throws Exception {
		logger.info("INICIO::ConsultarCustodiaPortafolioCuentaPropia");
		long inicio = System.currentTimeMillis();
		
		contexto = obtenerContexto(conn, entrada);
		List<Cartera> carterasVigentes = obtenerCarteras();
		List<Cartera> garantias = obtenerGarantias();
		List<Cartera> transitos = obtenerTransitos();
		List<Cartera> asignacionesSinFacturar = Collections.emptyList();
		if (contexto.IncluirAsignacionesSinFacturar) {
			asignacionesSinFacturar = obtenerAsignacionesSinFacturar();
		}
		
		int totalCarteras = carterasVigentes.size() + garantias.size() + transitos.size() + asignacionesSinFacturar.size();
		List<Cartera> carteras = new ArrayList<>(totalCarteras);
		carteras.addAll(carterasVigentes);
		carteras.addAll(garantias);
		carteras.addAll(transitos);
		carteras.addAll(asignacionesSinFacturar);
		
		contexto.Salida.p_out_CustodiaPorClasificacionPorIdInterno = obtenerCustodiaClasificacion(carteras);
		contexto.Salida.BuscadorCustodia = new BuscadorCustodia(contexto.Salida.p_out_CustodiaPorClasificacionPorIdInterno, contexto.Entrada.p_ClasificadorCustodiaPorIdInterno);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarCustodiaPortafolioCuentaPropia {}", fin - inicio);
		
		return contexto.Salida;
	}
	
	private ContextoLogica obtenerContexto(Connection conn, EntradaConsultarCustodiaPortafolioCuentaPropia entrada) throws Exception {
		logger.debug("INICIO::ConsultarCustodiaPortafolioCuentaPropia.obtenerContexto");
		long inicio = System.currentTimeMillis();
		
		ContextoLogica contextoLogica = new ContextoLogica();
		contextoLogica.Connection = conn;
		contextoLogica.Entrada = entrada;
		contextoLogica.Salida = new SalidaConsultarCustodiaPortafolioCuentaPropia();
		contextoLogica.CuentaPropia = entrada.p_CuentaPropia == null ? LimiteUtils.obtenerCuentaPropia(conn, entrada.p_IdEmpresa) : entrada.p_CuentaPropia;
		contextoLogica.IncluirAsignacionesSinFacturar = _cl.SI.equals(entrada.p_IncluirAsignacionesSinFacturar);
		
		entrada.p_CuentaPropia = contextoLogica.CuentaPropia;
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioCuentaPropia.obtenerContexto {}", fin - inicio);
		
		return contextoLogica;
	}
	
	private List<Cartera> obtenerCarteras() throws SQLException {
		logger.debug("INICIO::ConsultarCustodiaPortafolioCuentaPropia.obtenerCarteras");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>(16);
		params.put("IdEmpresa", contexto.Entrada.p_IdEmpresa);
		params.put("IdCliente", contexto.CuentaPropia.IdCliente);
		params.put("IdPortafolio", null);
		params.put("IdClasificacionTipoInstrumento", contexto.Entrada.p_ClasificacionTipoInstrumento);
		params.put("IdTipoInstrumento", null);
		params.put("IdInstrumento", null);
		params.put("TipoFechaConsulta", contexto.Entrada.p_TipoFechaConsulta);
		params.put("FechaConsulta", contexto.Entrada.p_FechaConsulta);
		params.put("IdClienteCuentaPropia", contexto.CuentaPropia.IdCliente);
		
		List<Cartera> carteras = new SQL().query(contexto.Connection, SQL_SELECT_CARTERA, params, Cartera.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioCuentaPropia.obtenerCarteras {}", fin - inicio);
		
		return carteras;
	}
	
	private List<Cartera> obtenerGarantias() throws SQLException {
		logger.debug("INICIO::ConsultarCustodiaPortafolioCuentaPropia.obtenerGarantias");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>(16);
		params.put("IdEmpresa", contexto.Entrada.p_IdEmpresa);
		params.put("IdCliente", contexto.CuentaPropia.IdCliente);
		params.put("IdPortafolio", null);
		params.put("IdClasificacionTipoInstrumento", contexto.Entrada.p_ClasificacionTipoInstrumento);
		params.put("IdTipoInstrumento", null);
		params.put("IdInstrumento", null);
		params.put("TipoFechaConsulta", contexto.Entrada.p_TipoFechaConsulta);
		params.put("FechaConsulta", contexto.Entrada.p_FechaConsulta);
		
		List<Cartera> garantias = new SQL().query(contexto.Connection, SQL_SELECT_GARANTIA, params, Cartera.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioCuentaPropia.obtenerGarantias {}", fin - inicio);
		
		return garantias;
	}
	
	private List<Cartera> obtenerTransitos() throws SQLException {
		logger.debug("INICIO::ConsultarCustodiaPortafolioCuentaPropia.obtenerTransitos");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>(16);
		params.put("IdEmpresa", contexto.Entrada.p_IdEmpresa);
		params.put("IdCliente", contexto.CuentaPropia.IdCliente);
		params.put("IdPortafolio", null);
		params.put("IdClasificacionTipoInstrumento", contexto.Entrada.p_ClasificacionTipoInstrumento);
		params.put("IdTipoInstrumento", null);
		params.put("IdInstrumento", null);
		params.put("TipoFechaConsulta", contexto.Entrada.p_TipoFechaConsulta);
		params.put("FechaConsulta", contexto.Entrada.p_FechaConsulta);
		params.put("IdClienteCuentaPropia", contexto.CuentaPropia.IdCliente);
		
		List<Cartera> transitos = new SQL().query(contexto.Connection, SQL_SELECT_TRANSITO, params, Cartera.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioCuentaPropia.obtenerTransitos {}", fin - inicio);
		
		return transitos;
	}
	
	private List<Cartera> obtenerAsignacionesSinFacturar() throws SQLException {
		logger.debug("INICIO::ConsultarCustodiaPortafolioCuentaPropia.obtenerAsignacionesSinFacturar");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdEmpresa", contexto.Entrada.p_IdEmpresa);
		params.put("IdCliente", contexto.CuentaPropia.IdCliente);
		params.put("IdClasificacionTipoInstrumento", contexto.Entrada.p_ClasificacionTipoInstrumento);
		params.put("FechaConsulta", contexto.Entrada.p_FechaConsulta);
		
		List<Cartera> asignaciones = new SQL().query(contexto.Connection, SQL_SELECT_ASIGNACION_SIN_FACTURAR, params, Cartera.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioCuentaPropia.obtenerAsignacionesSinFacturar {}", fin - inicio);
		
		return asignaciones;
	}
	
	private ConcurrentMap<Long, ConcurrentMap<String, Custodia>> obtenerCustodiaClasificacion(List<Cartera> carteras) {
		logger.debug("INICIO::ConsultarCustodiaPortafolioCuentaPropia.obtenerCustodiaClasificacion");
		long inicio = System.currentTimeMillis();
		
		BiFunction<Cartera, Map.Entry<Long, ClasificadorCustodia>, ContextoClasificacion> obtenerContextoClasificacion = (cartera, entry) -> {
			ContextoClasificacion contextoClasificacion = new ContextoClasificacion();
			contextoClasificacion.IdInterno = entry.getKey();
			contextoClasificacion.ClasificadorCustodia = entry.getValue();
			contextoClasificacion.Cartera = cartera;
			contextoClasificacion.Custodia = entry.getValue().obtenerCustodiaClasificacion.apply(cartera);
			
			SumandosCartera sumandosCartera = obtenerSumandosCartera(cartera);
			contextoClasificacion.Custodia.TotalPosicion = sumandosCartera.Posicion;
			contextoClasificacion.Custodia.MontoCompra = sumandosCartera.MontoCompra;
			return contextoClasificacion;
		};
		
		Map<Long, ClasificadorCustodia> clasificadorPorIdInterno = contexto.Entrada.p_ClasificadorCustodiaPorIdInterno;
		List<ContextoClasificacion> tareas = StreamUtils.parallelFlatmap(carteras, clasificadorPorIdInterno.entrySet(), obtenerContextoClasificacion).collect(Collectors.toList());
		
		ConcurrentMap<Long, ConcurrentMap<String, Custodia>> custodiaPorClasificacionPorIdInterno = new ConcurrentHashMap<>(clasificadorPorIdInterno.size());
		logger.debug("INICIO::ConsultarCustodiaPortafolioCuentaPropia.obtenerCustodiaClasificacion.totalizacionPorClasificacion");
		long totalizerBeg = System.currentTimeMillis();
		
		tareas.parallelStream().forEach(tarea -> {
			ConcurrentMap<String, Custodia> custodiaPorClasificacion = custodiaPorClasificacionPorIdInterno.computeIfAbsent(tarea.IdInterno, k -> new ConcurrentHashMap<>());
			String clasificacion = tarea.ClasificadorCustodia.obtenerLlaveClasificacion.apply(tarea.Custodia).Llave;
			Custodia custodiaClasificacion = custodiaPorClasificacion.computeIfAbsent(clasificacion, k -> new Custodia().merge(tarea.Custodia));
			
			// TODO: Probar CAS en lugar de sync si hay mucha contencion
			// custodiaClasificacion.acumularPosicion(tarea.Custodia.TotalPosicion);
			// custodiaClasificacion.acumularMontoCompra(tarea.Custodia.MontoCompra);
			// custodiaClasificacion.acumularTasa(tarea.Custodia.TasaPromedio);
			custodiaClasificacion.syncAcumularCustodia(tarea.Custodia.TotalPosicion, tarea.Custodia.MontoCompra);
		});
		
		long totalizerEnd = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioCuentaPropia.obtenerCustodiaClasificacion.totalizacionPorClasificacion {}", totalizerEnd - totalizerBeg);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConsultarCustodiaPortafolioCuentaPropia.obtenerCustodiaClasificacion {}", fin - inicio);
		
		return custodiaPorClasificacionPorIdInterno;
	}
	
	private SumandosCartera obtenerSumandosCartera(Cartera cartera) {
		logger.trace("INICIO::ConsultarCustodiaPortafolioCuentaPropia.obtenerSumandosCartera");
		long inicio = System.currentTimeMillis();
		
		BigDecimal factorAditivo = BigDecimal.ZERO;
		BigDecimal posicion = BigDecimal.ZERO;
		BigDecimal factorAditivoNominal = BigDecimal.ZERO;
		BigDecimal posicionNominal = BigDecimal.ZERO;
		BigDecimal montoCompra = BigDecimal.ZERO;
		
		switch (cartera.TablaOrigen) {
			case "CtePactoCompraTotal":
				factorAditivoNominal = _cl.SI.equals(cartera.EsNominal) ? BigDecimal.ONE.negate() : BigDecimal.ZERO;
				posicionNominal = cartera.CantidadNominal;
				
				factorAditivo = BigDecimal.ONE.negate();
				posicion = cartera.Cantidad;
				montoCompra = cartera.MontoCompra;
				break;
			case "CteTransitoNominalCuentaPropiaTotal":
				factorAditivo = "RETIRO".equals(cartera.AccionCustodia) ? BigDecimal.ONE.negate() : BigDecimal.ONE;
				posicion = cartera.Cantidad;
				montoCompra = cartera.MontoCompra;
				break;
			case "CtePrestamosVentaCortaTotal":
				factorAditivo = BigDecimal.ONE.negate();
				posicion = cartera.Cantidad;
				montoCompra = cartera.MontoCompra;
				break;
			case "CteTransitoVentaDescubiertaTotal":
				factorAditivo = !"INGIT".equals(cartera.IdTipoOperacion) && _cl.SI.equals(cartera.EsVentaDescubierta) ? BigDecimal.ONE.negate() : BigDecimal.ONE;
				posicion = cartera.Cantidad.abs();
				montoCompra = cartera.MontoCompra.abs();
				break;
			case "CteSimultaneaCompraVentaTotal":
				factorAditivo = "VENSIM".equals(cartera.IdTipoOperacion) ? BigDecimal.ONE : BigDecimal.ONE.negate();
				posicion = cartera.Cantidad;
				montoCompra = cartera.MontoCompra;
				break;
			case "CteReporteCompraVentaTotal":
				factorAditivo = "COMRRV".equals(cartera.IdTipoOperacion) || "COMRRF".equals(cartera.IdTipoOperacion) ? BigDecimal.ONE.negate() : BigDecimal.ONE;
				posicion = cartera.Cantidad;
				montoCompra = cartera.MontoCompra;
				break;
			case "CteEmprestitoExternoTotal":
				factorAditivo = BigDecimal.ZERO;
				posicion = cartera.Cantidad;
				montoCompra = cartera.MontoCompra;
				break;
			case "CteAsignacionSinFacturarTotal":
				if (cartera.IdTipoOperacion.startsWith("COM")) {
					factorAditivo = BigDecimal.ONE;
				} else if (cartera.IdTipoOperacion.startsWith("VEN")) {
					factorAditivo = BigDecimal.ONE.negate();
				}
				posicion = cartera.Cantidad;
				montoCompra = cartera.MontoCompra;
				break;
			case "CteCustodiaTotal":
				factorAditivo = BigDecimal.ONE;
				posicion = cartera.Cantidad;
				montoCompra = cartera.MontoCompra;
				break; 
			default:
				factorAditivo = BigDecimal.ZERO;
				posicion = cartera.Cantidad;
				montoCompra = cartera.MontoCompra;
		}
		
		posicionNominal = factorAditivoNominal.multiply(posicionNominal);
		posicion = factorAditivo.multiply(posicion).add(posicionNominal);
		montoCompra = factorAditivo.multiply(montoCompra);
		
		SumandosCartera o = new SumandosCartera();
		o.Posicion = posicion;
		o.MontoCompra = montoCompra;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ConsultarCustodiaPortafolioCuentaPropia.obtenerSumandosCartera {}", fin - inicio);
		
		return o;
	}
	
	public static void main(String[] args) {
		System.out.println("== INICIO TEST ==");
		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			try {
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_ClasificacionTipoInstrumento\":\"RF\",\"p_TipoFechaConsulta\":\"ADQ\",\"p_FechaConsulta\":\"2022-06-28\",\"p_UsaAsignacionesSinFacturar\":\"N\"}";
				EntradaConsultarCustodiaPortafolioCuentaPropia entradaConsultarCustodiaPortafolioCuentaPropia = Utils.getGsonBuilder().fromJson(json, EntradaConsultarCustodiaPortafolioCuentaPropia.class);
				
				Map<Long, ClasificadorCustodia> clasificadorPorIdInterno = new HashMap<>();
				clasificadorPorIdInterno.put(1L, new ClasificadorCustodiaBuilder().porPortafolio().terminar());
				clasificadorPorIdInterno.put(2L, new ClasificadorCustodiaBuilder().porPortafolio().porInstrumento().terminar());
				clasificadorPorIdInterno.put(3L, new ClasificadorCustodiaBuilder().porPortafolio().porInstrumento().porNemo().terminar());
				clasificadorPorIdInterno.put(4L, new ClasificadorCustodiaBuilder().porPortafolio().porInstrumento().porNemo().porEmisor().terminar());
				clasificadorPorIdInterno.put(5L, new ClasificadorCustodiaBuilder().porPortafolio().porInstrumento().porNemo().porMonedaInstrumento().terminar());
				clasificadorPorIdInterno.put(6L, new ClasificadorCustodiaBuilder().porPortafolio().porTipoInstrumento().terminar());
				clasificadorPorIdInterno.put(7L, new ClasificadorCustodiaBuilder().porPortafolio().porClasificacionTipoInstrumento().terminar());
				clasificadorPorIdInterno.put(8L, new ClasificadorCustodiaBuilder().porTipoInstrumento().terminar());
				clasificadorPorIdInterno.put(9L, new ClasificadorCustodiaBuilder().porClasificacionTipoInstrumento().terminar());
				clasificadorPorIdInterno.put(10L, new ClasificadorCustodiaBuilder().porCliente().terminar());
				
				entradaConsultarCustodiaPortafolioCuentaPropia.p_ClasificadorCustodiaPorIdInterno = clasificadorPorIdInterno;
				
				SalidaConsultarCustodiaPortafolioCuentaPropia salidaConsultarCustodiaPortafolioCuentaPropia = new ConsultarCustodiaPortafolioCuentaPropia().logica(conn, entradaConsultarCustodiaPortafolioCuentaPropia);
				if (salidaConsultarCustodiaPortafolioCuentaPropia.P_OUT_ERRORESCONTADOR > 0) {
					conn.rollback();
				} else {
					BuscadorCustodia buscadorCustodia = salidaConsultarCustodiaPortafolioCuentaPropia.BuscadorCustodia;
					String clasificablesJson = "[{\"IdCliente\":249212,\"IdPortafolio\":1450028,\"IdInstrumento\":261979,\"IdClasificacionTipoInstrumento\":\"RF\",\"IdTipoInstrumento\":\"BEM\",\"Nemotecnico\":\"BACEN-A1\",\"IdEmisor\":\"AUTOCENTRA\",\"IdMonedaInstrumento\":\"CLP\"},{\"IdCliente\":249212,\"IdPortafolio\":1451214,\"IdInstrumento\":262017,\"IdClasificacionTipoInstrumento\":\"RF\",\"IdTipoInstrumento\":\"BEM\",\"Nemotecnico\":\"BAINT-A\",\"IdEmisor\":\"AUTOINTER\",\"IdMonedaInstrumento\":\"CLP\"},{\"IdCliente\":249212,\"IdPortafolio\":153981,\"IdInstrumento\":330242,\"IdClasificacionTipoInstrumento\":\"RF\",\"IdTipoInstrumento\":\"BBC\",\"Nemotecnico\":\"BCNO-U0915\",\"IdEmisor\":\"BBCONSORC\",\"IdMonedaInstrumento\":\"CLP\"},{\"IdCliente\":249212,\"IdPortafolio\":153981,\"IdInstrumento\":262000,\"IdClasificacionTipoInstrumento\":\"RF\",\"IdTipoInstrumento\":\"BEM\",\"Nemotecnico\":\"BAGUA-H\",\"IdEmisor\":\"AGUAS\",\"IdMonedaInstrumento\":\"CLP\"}]";
					List<ClasificablePorCustodia> filtros = Utils.getGsonBuilder().fromJson(clasificablesJson, new TypeToken<List<ClasificablePorCustodia>>() {}.getType());
					filtros.stream().flatMap(filtro -> clasificadorPorIdInterno.keySet().stream().map(id -> Pair.of(id, filtro))).forEach(pair -> {
						long id = pair.getLeft();
						ClasificablePorCustodia filtro = pair.getRight();
						System.out.println("\nFiltro = " + filtro);
						System.out.println("Custodia encontrada en clasificacion " + id + " = " + buscadorCustodia.obtenerCustodia(id, filtro));
					});
					
					clasificadorPorIdInterno.keySet().forEach(id -> System.out.println("Clasificacion " + id + " tiene " + salidaConsultarCustodiaPortafolioCuentaPropia.p_out_CustodiaPorClasificacionPorIdInterno.getOrDefault(id, new ConcurrentHashMap<>(0)).size() + " custodias"));
				}
				System.out.println(salidaConsultarCustodiaPortafolioCuentaPropia);
			} finally {
				conn.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN TEST ==");
	}
}
