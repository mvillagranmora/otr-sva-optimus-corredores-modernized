package cl.bcs.custodia.prt;

import cl.bcs.custodia.pojo.Validacion;
import cl.bcs.plataforma.pla.RevisarRequisitosBasicos;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario; 
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.bck.RenovarPrestamosBlockChain;
import cl.bcs.socionegocio.pojo.ConstantesBCK;
import cl.bcs.utils.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RenovarPrestamoMasivo implements JavaCall {
	static final Logger logger = LogManager.getLogger(RenovarPrestamoMasivo.class);
	
	private final String SQL_SELECT_PRESTAMO = loadResourceToString("cl/bcs/custodia/sql/RenovarPrestamoMasivo_SelectPrestamo.sql");
	private final String SQL_UPDATE_CARTERA_PRESTAMO = loadResourceToString("cl/bcs/custodia/sql/RenovarPrestamoMasivo_UpdateCarteraPrestamo.sql");
	private final String SQL_UPDATE_FOLIOBLOCKCHAIN = loadResourceToString("cl/bcs/socionegocio/bck/sql/ReintentoPrestamosBlockChain_UpdateCarteraConFolioBlockChain.sql");  

	
	private ContextoLogica contexto = new ContextoLogica();
	
	public static class EntradaRenovarPrestamoMasivo extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;
		public List<SolicitudRenovacion> p_SolicitudesRenovacion;
		
		public EntradaRenovarPrestamoMasivo() {
			p_IdEmpresa = 0L;
			p_IdUsuario = 0L;
			p_IdModulo = "";
			p_UsaReglasNegocio = "N";
			p_SolicitudesRenovacion = new ArrayList<>();
		}
	}
	
	public static class SolicitudRenovacion extends BcsPojo {
		public Long IdInterno;
		public Long IdCartera;
		public Long IdEmpleadoDigitador;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public BigDecimal TasaInstrumento;
		public BigDecimal Monto;
		public BigDecimal MontoPrima;
		public BigDecimal TasaPrima;
		public BigDecimal MontoPlazo;
		public Long PlazoDiasVencimiento;
		public String IdMonedaSistema;
		public String IdCondicionLiquidacion;
		public String FechaLiquidacion;
		public String FechaVencimiento;
		
		public SolicitudRenovacion() {
			IdInterno = 0L;
			IdCartera = 0L;
			IdEmpleadoDigitador = 0L;
			Cantidad = BigDecimal.ZERO;
			Precio = BigDecimal.ZERO;
			TasaInstrumento = BigDecimal.ZERO;
			Monto = BigDecimal.ZERO;
			MontoPrima = BigDecimal.ZERO;
			TasaPrima = BigDecimal.ZERO;
			MontoPlazo = BigDecimal.ZERO;
			PlazoDiasVencimiento = 0L;
			IdMonedaSistema = "";
			IdCondicionLiquidacion = "";
			FechaLiquidacion = "0001-01-01";
			FechaVencimiento = "0001-01-01";
		}
	}
	
	public static class SalidaRenovarPrestamoMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Validacion> P_OUT_VALIDACIONES;
		public List<ResultadoRenovacion> p_out_ResultadosRenovacion;
		
		public SalidaRenovarPrestamoMasivo() {
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_MENSAJES = new ArrayList<>();
			P_OUT_VALIDACIONES = new ArrayList<>();
			p_out_ResultadosRenovacion = new ArrayList<>();
		}
		
		public void agregarValidacion(Validacion validacion) {
			if (validacion.EsError) {
				P_OUT_ERRORESCONTADOR++;
				P_OUT_MENSAJES.add(validacion.Mensaje);
			}
			P_OUT_VALIDACIONES.add(validacion);
		}
		
		public void agregarValidaciones(Collection<Validacion> validaciones) {
			for (Validacion validacion : validaciones) {
				agregarValidacion(validacion);
			}
		}
	}
	
	public static class Prestamo extends BcsPojo {
		public Long IdInterno;
		public Long IdCartera;
		public Long FolioCartera;
		public Long FolioCarteraPrestamoOriginal;
		public String IdEstado;
		public java.sql.Date FechaVencimiento;
		public String EsRenovable;
		public Long MaximoDiasPrestamo;
		public Long MinimoDiasPrestamo;
		public Long TotalDiasRenovados;
		public Long IdCliente;
		public Long IdPortafolio;
		public String IdBolsa;
		public Long IdInstrumentoPrestamo;
		public Long IdInstrumento;
		public BigDecimal INGPosicion;
		public BigDecimal EGRPosicion;
		public BigDecimal Cantidad;
		public BigDecimal Precio;
		public String UsaGarantiaEspecifica;
		public String FolioExternoReferencia;
		public String CobraPrimaAlInicio;
		public Long IdAgente;
		public Long IdSucursal;
		public String EsNominal;
		public String EsFungible;
		public String TipoCustodia;
		public String FolioSistemaExterno;
		public String Nemotecnico;
		public String IdMercado;
	}
	
	public static class Cartera extends BcsPojo {
		public Long IdInterno;
		public Long IdCartera;
		public Long FolioCartera;
		public Long FolioCarteraRelacionada;
		public String IdEstado;
	}
	
	public static class ResultadoRenovacion extends BcsPojo {
		public Long IdInterno;
		public Long IdCarteraSolicitudRenovacion;
		public Long FolioCarteraSolicitudRenovacion;
		public Long IdCarteraPrestamoNuevo;
		public Long FolioCarteraPrestamoNuevo;
		public transient Long FolioCarteraPrestamoOriginal;
	}
	
	private static class SalidaCrearPrestamos extends BcsPojo {
		public List<Validacion> P_OUT_VALIDACIONES;
		public Map<Long, ResultadoRenovacion> p_out_ResultadoRenovacionPorIdInterno;
		
		public SalidaCrearPrestamos() {
			P_OUT_VALIDACIONES = new ArrayList<>();
			p_out_ResultadoRenovacionPorIdInterno = new HashMap<>();
		}
	}
	
	private static class ContextoLogica extends BcsPojo {
		public transient Connection Connection;
		public EntradaRenovarPrestamoMasivo Entrada;
		public SalidaRenovarPrestamoMasivo Salida;
		public String FechaSistemaStr;
		public java.sql.Timestamp FechaSistema;
		public String prestamosRentaFija;
		
		public ContextoLogica() {
			Entrada = new EntradaRenovarPrestamoMasivo();
			Salida = new SalidaRenovarPrestamoMasivo();
		}
	}
	
	public static class CarteraBlockChain extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_FolioPrestamo;
		public String p_FolioSistemaExterno;
	}
	
	public static class ParametroRF {
		public String Valor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	} 
	
	@Override
	public JsonObject call(Connection conn, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaRenovarPrestamoMasivo in = gson.fromJson(jin.toString(), EntradaRenovarPrestamoMasivo.class);
		SalidaRenovarPrestamoMasivo out = logica(conn, in);
		return new JsonParser().parse(gson.toJson(out)).getAsJsonObject();
	}
	
	public SalidaRenovarPrestamoMasivo logica(Connection conn, EntradaRenovarPrestamoMasivo entrada) throws Exception {
		logger.info("INICIO::RenovarPrestamoMasivo");
		long inicio = System.currentTimeMillis();
		
		contexto = obtenerContexto(conn, entrada);
		
		List<Validacion> validacionesRequisitosBasicos = validarRequisitosBasicos();
		contexto.Salida.agregarValidaciones(validacionesRequisitosBasicos);
		if (contexto.Salida.P_OUT_ERRORESCONTADOR > 0) {
			return contexto.Salida;
		}
		
		List<Validacion> validacionesCriterioSolicitud = validarCriteriosSolicitud();
		contexto.Salida.agregarValidaciones(validacionesCriterioSolicitud);
		if (contexto.Salida.P_OUT_ERRORESCONTADOR > 0) {
			return contexto.Salida;
		}
		
		Map<Long, SolicitudRenovacion> solicitudRenovacionPorIdCartera = contexto.Entrada.p_SolicitudesRenovacion
				.stream()
				.collect(Collectors.toMap(u -> u.IdCartera, u -> u));
		List<Prestamo> prestamos = obtenerPrestamos(solicitudRenovacionPorIdCartera);
		
		if (prestamos.size() == 0) {
			return contexto.Salida;
		}
		
		//Verifica si se puede operar prestamos pertenecientes a mercado Renta Fija
		for (Prestamo prestamo : prestamos) {
			if ( prestamo.IdMercado.equals(ConstantesBCK.MERCADO_PRESTAMOS_BLOCKCHAIN_RENTA_FIJA) && contexto.prestamosRentaFija.equals(ConstantesBCK.NO) ) {
				contexto.Salida.P_OUT_ERRORESCONTADOR++;
				contexto.Salida.P_OUT_MENSAJES.add( new MensajeSalida( 0L, MensajeSalida.MENSAJE_ERROR, "PRESTAMOS_MERCADO_RENTA_FIJA_DESHABILITADO", new HashMap<String, String>(), contexto.Connection, entrada.p_IdEmpresa ) );
				return contexto.Salida;
			}
		}
		
		List<Validacion> validacionesCriteriosRenovacion = validarCriteriosRenovacion(prestamos, solicitudRenovacionPorIdCartera);
		contexto.Salida.agregarValidaciones(validacionesCriteriosRenovacion);
		if (contexto.Salida.P_OUT_ERRORESCONTADOR > 0) {
			return contexto.Salida;
		}
		
		SalidaCrearPrestamos salidaCrearPrestamos = crearPrestamos(prestamos, solicitudRenovacionPorIdCartera);
		contexto.Salida.agregarValidaciones(salidaCrearPrestamos.P_OUT_VALIDACIONES);
		if (contexto.Salida.P_OUT_ERRORESCONTADOR > 0) {
			return contexto.Salida;
		}
		
		marcarCarterasRenovacion(salidaCrearPrestamos.p_out_ResultadoRenovacionPorIdInterno);
		contexto.Salida.p_out_ResultadosRenovacion.addAll(salidaCrearPrestamos.p_out_ResultadoRenovacionPorIdInterno.values());
		
		List<MensajeSalida> mensajesRenovacion = obtenerMensajesRenovacion(salidaCrearPrestamos.p_out_ResultadoRenovacionPorIdInterno.values());
		contexto.Salida.P_OUT_MENSAJES.addAll(mensajesRenovacion);
		
		List<MensajeSalida> mensajesRenovacionPrestamoBlockChain = informarRenovacionPrestamoEnBlockChain(salidaCrearPrestamos.p_out_ResultadoRenovacionPorIdInterno.values(), entrada);
		contexto.Salida.P_OUT_MENSAJES.addAll(mensajesRenovacionPrestamoBlockChain);
		
		long fin = System.currentTimeMillis();
		logger.info("FIN::RenovarPrestamoMasivo {}", fin - inicio);
		
		return contexto.Salida;
	}
	
	private ContextoLogica obtenerContexto(Connection conn, EntradaRenovarPrestamoMasivo entrada) throws Exception {
		logger.debug("INICIO::RenovarPrestamoMasivo.obtenerContexto");
		long inicio = System.currentTimeMillis();
		
		ContextoLogica contextoLogica = new ContextoLogica();
		contextoLogica.Connection = conn;
		contextoLogica.FechaSistema = FechaDB.ObtenerFechaHoraSistemaTs(conn, entrada.p_IdEmpresa);
		contextoLogica.FechaSistemaStr = Fecha.toStringDate(contextoLogica.FechaSistema);
		contextoLogica.Entrada = entrada;
		contextoLogica.prestamosRentaFija = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "CUS.PrestamosRentaFija");
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RenovarPrestamoMasivo.obtenerContexto {}", fin - inicio);
		
		return contextoLogica;
	}
	
	private List<Validacion> validarRequisitosBasicos() throws Exception {
		logger.trace("INICIO::RenovarPrestamoMasivo.validarRequisitosBasicos");
		long inicio = System.currentTimeMillis();
		
		RevisarRequisitosBasicos.RevisarRequisitosBasicosSalida salidaRevisarRequisitosBasicos = new RevisarRequisitosBasicos()
				.logica(
						contexto.Connection,
						contexto.Entrada.p_IdEmpresa,
						contexto.Entrada.p_IdUsuario,
						contexto.Entrada.p_IdModulo
				);
		List<Validacion> validaciones = salidaRevisarRequisitosBasicos.p_out_Mensajes
				.stream()
				.map((mensaje) -> {
					Validacion validacion = new Validacion();
					validacion.IdInterno = 0L;
					validacion.EsError = MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo);
					validacion.Mensaje = mensaje;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::RenovarPrestamoMasivo.validarRequisitosBasicos {}", fin - inicio);
		
		return validaciones;
	}
	
	private List<Validacion> validarCriteriosSolicitud() {
		logger.trace("INICIO::RenovarPrestamoMasivo.validarCriteriosSolicitud");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = new ArrayList<>(3 * contexto.Entrada.p_SolicitudesRenovacion.size());
		Set<Long> idsInternoUsados = new HashSet<>(contexto.Entrada.p_SolicitudesRenovacion.size());
		
		Map<String, String> params = new HashMap<>(2);
		contexto.Entrada.p_SolicitudesRenovacion.forEach(solicitud -> {
			if (solicitud.IdInterno == null || solicitud.IdInterno <= 0) {
				params.clear();
				params.put("NombreCampo", "IdInterno");
				Validacion validacion = obtenerValidacion(solicitud.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CAMPO_REQUIERE_ENTERO_POSITIVO", params);
				validaciones.add(validacion);
			}
			if (Objects.nonNull(solicitud.IdInterno) && idsInternoUsados.add(solicitud.IdInterno) == false) {
				params.clear();
				Validacion validacion = obtenerValidacion(solicitud.IdInterno, MensajeSalida.MENSAJE_ERROR, "CORRELATIVO_INCORRECTO", params);
				validaciones.add(validacion);
			}
			if (solicitud.IdCartera == null || solicitud.IdCartera <= 0) {
				params.clear();
				params.put("NombreCampo", "IdCartera");
				Validacion validacion = obtenerValidacion(solicitud.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CAMPO_REQUIERE_ENTERO_POSITIVO", params);
				validaciones.add(validacion);
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::RenovarPrestamoMasivo.validarCriteriosSolicitud {}", fin - inicio);
		
		return validaciones;
	}
	
	private List<Prestamo> obtenerPrestamos(Map<Long, SolicitudRenovacion> solicitudRenovacionPorIdCartera) throws SQLException {
		logger.debug("INICIO::RenovarPrestamoMasivo.obtenerPrestamos");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsCartera = new ArrayList<>(solicitudRenovacionPorIdCartera.keySet());
		
		HashMap<String, Object> params = new HashMap<>(2);
		params.put("IdCartera", idsCartera);
		
		List<Prestamo> prestamos = new SQL().query(contexto.Connection, SQL_SELECT_PRESTAMO, params, Prestamo.class);
		for (Prestamo prestamo : prestamos) {
			SolicitudRenovacion solicitudRenovacion = solicitudRenovacionPorIdCartera.get(prestamo.IdCartera);
			prestamo.IdInterno = solicitudRenovacion.IdInterno;
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RenovarPrestamoMasivo.obtenerPrestamos {}", fin - inicio);
		
		return prestamos;
	}
	
	private List<Validacion> validarCriteriosRenovacion(List<Prestamo> prestamos, Map<Long, SolicitudRenovacion> solicitudRenovacionPorIdCartera) {
		logger.trace("INICIO::RenovarPrestamoMasivo.validarCriteriosRenovacion");
		long inicio = System.currentTimeMillis();
		
		final List<Validacion> validaciones = new ArrayList<>(3 * prestamos.size());
		final Map<String, String> params = new HashMap<>(4);
		
		prestamos.forEach(prestamo -> {

			boolean noEstaVencido = "VCD".equals(prestamo.IdEstado) == false;
			boolean noEstaTotalmenteAnticipado = prestamo.INGPosicion.compareTo(prestamo.EGRPosicion) > 0 && "ANT".equals(prestamo.IdEstado) == false;
			if (noEstaVencido && noEstaTotalmenteAnticipado) {
				params.clear();
				params.put("FolioCartera", String.valueOf(prestamo.FolioCartera));
				validaciones.add(obtenerValidacion(prestamo.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_PRESTAMO_NO_VENCIDO_NI_ANTICIPADO_TOTAL", params));
			}
			if ("S".equals(prestamo.EsRenovable) == false) {
				params.clear();
				params.put("FolioCartera", String.valueOf(prestamo.FolioCartera));
				validaciones.add(obtenerValidacion(prestamo.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_TIPO_PRESTAMO_NO_RENOVABLE", params));
			}
			if (prestamo.TotalDiasRenovados < prestamo.MinimoDiasPrestamo) {
				params.clear();
				params.put("FolioCartera", String.valueOf(prestamo.FolioCartera));
				params.put("CantidadDias", String.valueOf(prestamo.MinimoDiasPrestamo));
				validaciones.add(obtenerValidacion(prestamo.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_PRESTAMO_MENOR_PLAZO_MINIMO_NORMA", params));
			}
			if (prestamo.TotalDiasRenovados > prestamo.MaximoDiasPrestamo) {
				params.clear();
				params.put("FolioCartera", String.valueOf(prestamo.FolioCartera));
				params.put("CantidadDias", String.valueOf(prestamo.MaximoDiasPrestamo));
				validaciones.add(obtenerValidacion(prestamo.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_PRESTAMO_SUPERA_PLAZO_MAXIMO_NORMA", params));
			}
			SolicitudRenovacion solicitudRenovacion = solicitudRenovacionPorIdCartera.get(prestamo.IdCartera);
			if (solicitudRenovacion.Cantidad.compareTo(prestamo.INGPosicion) > 0) {
				params.clear();
				params.put("FolioCartera", String.valueOf(prestamo.FolioCartera));
				validaciones.add(obtenerValidacion(prestamo.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_PRESTAMO_RENOVACION_SUPERA_CANTIDAD_REMANENTE", params));
			}
			if ((prestamo.MaximoDiasPrestamo - prestamo.TotalDiasRenovados) < prestamo.MinimoDiasPrestamo) {
				params.clear();
				params.put("FolioCartera", String.valueOf(prestamo.FolioCartera));
				validaciones.add(obtenerValidacion(prestamo.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_NO_DISPONE_PLAZO_SUFICIENTE_RENOVACION", params));
			}
		});
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::RenovarPrestamoMasivo.validarCriteriosRenovacion {}", fin - inicio);
		
		return validaciones;
	}
	
	private SalidaCrearPrestamos crearPrestamos(List<Prestamo> prestamos, Map<Long, SolicitudRenovacion> solicitudRenovacionPorIdCartera) throws Exception {
		logger.trace("INICIO::RenovarPrestamoMasivo.crearRenovaciones");
		long inicio = System.currentTimeMillis();
		
		// TODO: Alguien masifique CrearPrestamo.java porfa
		SalidaCrearPrestamos salidaCrearPrestamos = new SalidaCrearPrestamos();
		CrearPrestamo crearPrestamo = new CrearPrestamo();
		for (Prestamo prestamo : prestamos) {
			SolicitudRenovacion solicitudRenovacion = solicitudRenovacionPorIdCartera.get(prestamo.IdCartera);
			CrearPrestamo.CrearPrestamo_Entrada entradaCrearPrestamo = obtenerEntradaCrearPrestamo(prestamo, solicitudRenovacion);
			CrearPrestamo.CrearPrestamo_Salida salidaCrearPrestamo = crearPrestamo.logica(contexto.Connection, entradaCrearPrestamo);
			
			List<Validacion> validacionesCrearPrestamo = salidaCrearPrestamo.P_OUT_MENSAJES.stream().map(this::obtenerValidacion).collect(Collectors.toList());
			salidaCrearPrestamos.P_OUT_VALIDACIONES.addAll(validacionesCrearPrestamo);
			
			ResultadoRenovacion resultadoRenovacion = new ResultadoRenovacion();
			resultadoRenovacion.IdInterno = prestamo.IdInterno;
			resultadoRenovacion.IdCarteraSolicitudRenovacion = prestamo.IdCartera;
			resultadoRenovacion.FolioCarteraSolicitudRenovacion = prestamo.FolioCartera;
			resultadoRenovacion.FolioCarteraPrestamoOriginal = prestamo.FolioCarteraPrestamoOriginal;
			resultadoRenovacion.IdCarteraPrestamoNuevo = salidaCrearPrestamo.P_OUT_IDCARTERA;
			resultadoRenovacion.FolioCarteraPrestamoNuevo = salidaCrearPrestamo.P_OUT_FOLIOCARTERA;
			salidaCrearPrestamos.p_out_ResultadoRenovacionPorIdInterno.put(resultadoRenovacion.IdInterno, resultadoRenovacion);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::RenovarPrestamoMasivo.crearRenovaciones {}", fin - inicio);
		
		return salidaCrearPrestamos;
	}
	
	/**
	 *
	 */
	
	private void marcarCarterasRenovacion(Map<Long, ResultadoRenovacion> resultadoRenovacionPorIdInterno) throws SQLException {
		logger.debug("INICIO::RenovarPrestamoMasivo.marcarCarterasRenovacion");
		long inicio = System.currentTimeMillis();
		
		Function<ResultadoRenovacion, Stream<Cartera>> obtenerCarteras = (resultadoRenovacion) -> Stream.of("VIG", "RNV").map(idEstado -> {
			Cartera cartera = new Cartera();
			cartera.IdInterno = resultadoRenovacion.IdInterno;
			cartera.IdCartera = "RNV".equals(idEstado) ? resultadoRenovacion.IdCarteraSolicitudRenovacion : resultadoRenovacion.IdCarteraPrestamoNuevo;
			cartera.FolioCarteraRelacionada = resultadoRenovacion.FolioCarteraPrestamoOriginal;
			cartera.IdEstado = idEstado;
			return cartera;
		});
		List<Cartera> carteras = resultadoRenovacionPorIdInterno.values()
				.stream()
				.flatMap(obtenerCarteras)
				.collect(Collectors.toList());
		new SQL().batch(contexto.Connection, SQL_UPDATE_CARTERA_PRESTAMO, carteras, Cartera.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::RenovarPrestamoMasivo.marcarCarterasRenovacion {}", fin - inicio);
	}
	
	private Validacion obtenerValidacion(Long id, String tipoMensaje, String codigoMensaje, Map<String, String> params) {
		logger.trace("INICIO::RenovarPrestamoMasivo.obtenerValidacion");
		long inicio = System.currentTimeMillis();
		
		Validacion validacion = new Validacion();
		validacion.IdInterno = id;
		validacion.EsError = MensajeSalida.MENSAJE_ERROR.equals(tipoMensaje);
		validacion.Mensaje = PlataformaUtils.obtenerMensajeSalida(contexto.Connection, contexto.Entrada.p_IdEmpresa, id, tipoMensaje, codigoMensaje, params);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::RenovarPrestamoMasivo.obtenerValidacion {}", fin - inicio);
		
		return validacion;
	}
	
	private Validacion obtenerValidacion(MensajeSalida mensaje) {
		logger.trace("INICIO::RenovarPrestamoMasivo.obtenerValidacion");
		long inicio = System.currentTimeMillis();
		
		Validacion validacion = new Validacion();
		validacion.IdInterno = mensaje.id;
		validacion.EsError = MensajeSalida.MENSAJE_ERROR.equals(mensaje.tipo);
		validacion.Mensaje = mensaje;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::RenovarPrestamoMasivo.obtenerValidacion {}", fin - inicio);
		
		return validacion;
	}
	
	private CrearPrestamo.CrearPrestamo_Entrada obtenerEntradaCrearPrestamo(Prestamo prestamo, SolicitudRenovacion solicitudRenovacion) {
		logger.trace("INICIO::RenovarPrestamoMasivo.obtenerEntradaCrearPrestamo");
		long inicio = System.currentTimeMillis();
		
		CrearPrestamo.CrearPrestamo_Entrada entradaCrearPrestamo = new CrearPrestamo.CrearPrestamo_Entrada();
		entradaCrearPrestamo.p_IdEmpresa = contexto.Entrada.p_IdEmpresa;
		entradaCrearPrestamo.p_IdUsuario = contexto.Entrada.p_IdUsuario;
		entradaCrearPrestamo.p_IdModulo = contexto.Entrada.p_IdModulo;
		entradaCrearPrestamo.p_UsaReglasNegocio = contexto.Entrada.p_UsaReglasNegocio;
		entradaCrearPrestamo.p_IdEmpleadoDigitador = solicitudRenovacion.IdEmpleadoDigitador;
		entradaCrearPrestamo.p_IdClienteSolicita = prestamo.IdCliente;
		entradaCrearPrestamo.p_IdPortafolioSolicita = prestamo.IdPortafolio;
		entradaCrearPrestamo.p_IdBolsa = prestamo.IdBolsa;
		entradaCrearPrestamo.p_FechaMovimiento = contexto.FechaSistemaStr;
		entradaCrearPrestamo.p_FechaVencimiento = solicitudRenovacion.FechaVencimiento;
		entradaCrearPrestamo.p_IdInstrumentoPrestamo = prestamo.IdInstrumentoPrestamo;
		entradaCrearPrestamo.p_IdInstrumento = prestamo.IdInstrumento;
		entradaCrearPrestamo.p_Nemo = prestamo.Nemotecnico;
		entradaCrearPrestamo.p_Cantidad = solicitudRenovacion.Cantidad;
		entradaCrearPrestamo.p_Precio = solicitudRenovacion.Precio;
		entradaCrearPrestamo.p_TasaInstrumento = solicitudRenovacion.TasaInstrumento;
		entradaCrearPrestamo.p_Monto = solicitudRenovacion.Monto;
		entradaCrearPrestamo.p_MontoPrima = solicitudRenovacion.MontoPrima;
		entradaCrearPrestamo.p_TasaPrima = solicitudRenovacion.TasaPrima;
		entradaCrearPrestamo.p_MontoAPlazo = solicitudRenovacion.MontoPlazo;
		entradaCrearPrestamo.p_ConGarantiaEspecifica = prestamo.UsaGarantiaEspecifica;
		entradaCrearPrestamo.p_FolioEntidad = prestamo.FolioExternoReferencia;
		entradaCrearPrestamo.p_FolioTransaccion = "";
		entradaCrearPrestamo.p_CobraPrimaAlInicio = prestamo.CobraPrimaAlInicio;
		entradaCrearPrestamo.p_IdAgente = prestamo.IdAgente;
		entradaCrearPrestamo.p_IdSucursal = prestamo.IdSucursal;
		entradaCrearPrestamo.p_PlazoEnDias = solicitudRenovacion.PlazoDiasVencimiento;
		entradaCrearPrestamo.p_esNominal = prestamo.EsNominal;
		entradaCrearPrestamo.p_IdTipoCanalIngreso = contexto.Entrada.p_IdTipoCanalIngreso;
		entradaCrearPrestamo.p_IdMonedaSistema = solicitudRenovacion.IdMonedaSistema;
		entradaCrearPrestamo.p_IdMercado = prestamo.IdMercado;
		entradaCrearPrestamo.p_EsFungible = prestamo.EsFungible;
		entradaCrearPrestamo.p_TipoCustodia = prestamo.TipoCustodia;
		entradaCrearPrestamo.p_TipoGarantia = "";
		entradaCrearPrestamo.p_TipoPrestamo_BChain = "";
		entradaCrearPrestamo.p_IdFolioSistemaExterno = prestamo.FolioSistemaExterno;
		entradaCrearPrestamo.p_IdCondicionLiquidacion = solicitudRenovacion.IdCondicionLiquidacion;
		entradaCrearPrestamo.p_FechaLiquidacion = solicitudRenovacion.FechaLiquidacion;
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::RenovarPrestamoMasivo.obtenerEntradaCrearPrestamo {}", fin - inicio);
		
		return entradaCrearPrestamo;
	}
	
	private List<MensajeSalida> obtenerMensajesRenovacion(Collection<ResultadoRenovacion> resultadosRenovacion) {
		logger.trace("INICIO::RenovarPrestamoMasivo.obtenerMensajesRenovacion");
		long inicio = System.currentTimeMillis();
		
		Map<String, String> params = new HashMap<>(4);
		List<MensajeSalida> mensajes = resultadosRenovacion
				.stream()
				.map(resultadoRenovacion -> {
					params.clear();
					params.put("FolioCartera", String.valueOf(resultadoRenovacion.FolioCarteraSolicitudRenovacion));
					params.put("FolioCarteraNueva", String.valueOf(resultadoRenovacion.FolioCarteraPrestamoNuevo));
					return PlataformaUtils.obtenerMensajeSalida(
							contexto.Connection,
							contexto.Entrada.p_IdEmpresa,
							resultadoRenovacion.IdInterno,
							MensajeSalida.MENSAJE_INFORMACION,
							"UI_RENOVACION_PRESTAMO_EXITOSA",
							params
					);
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::RenovarPrestamoMasivo.obtenerMensajesRenovacion {}", fin - inicio);
		
		return mensajes;
	}
	
	private List<MensajeSalida> informarRenovacionPrestamoEnBlockChain(Collection<ResultadoRenovacion> resultadosRenovacion, EntradaRenovarPrestamoMasivo entrada) throws Exception {
		logger.trace("INICIO::RenovarPrestamoMasivo.informarRenovacionPrestamoEnBlockChain");
		long inicio = System.currentTimeMillis();
		
		List<MensajeSalida> mensajes 																	= new ArrayList<>();
		RenovarPrestamosBlockChain renovarPrestamosBlockchain 											= new RenovarPrestamosBlockChain();
		RenovarPrestamosBlockChain.RenovarPrestamosBlockChain_Entrada renovarPrestamoBlockchainEntrada 	= new RenovarPrestamosBlockChain.RenovarPrestamosBlockChain_Entrada();
		RenovarPrestamosBlockChain.DatosPrestamo datosPrestamo 											= new RenovarPrestamosBlockChain.DatosPrestamo();
		
		for (ResultadoRenovacion resultadoRenovacionItem : resultadosRenovacion) {
			renovarPrestamoBlockchainEntrada.p_IdEmpresa 		= entrada.p_IdEmpresa;
			renovarPrestamoBlockchainEntrada.p_IdUsuario 		= entrada.p_IdUsuario;
			renovarPrestamoBlockchainEntrada.p_IdModulo 		= entrada.p_IdModulo;
			renovarPrestamoBlockchainEntrada.p_FechaSolicitud 	= contexto.FechaSistemaStr;
			datosPrestamo.FolioPrestamoSolicitud 				= resultadoRenovacionItem.FolioCarteraSolicitudRenovacion;
			datosPrestamo.FolioPrestamoNuevo 					= resultadoRenovacionItem.FolioCarteraPrestamoNuevo;
			renovarPrestamoBlockchainEntrada.p_DatosPrestamo	= datosPrestamo;
			
			RenovarPrestamosBlockChain.RenovarPrestamosBlockChain_Salida renovarPrestamoBlockchainSalida = renovarPrestamosBlockchain.logica(contexto.Connection, renovarPrestamoBlockchainEntrada);
			
			if (renovarPrestamoBlockchainSalida.P_OUT_ERRORESCONTADOR == 0 && !renovarPrestamoBlockchainSalida.P_OUT_FOLIOSISTEMAEXTERNO.isEmpty()) {
				
				List<CarteraBlockChain> listaCarteraBlockChain 	= new ArrayList<>();
				CarteraBlockChain carteraBlockChain 			= new CarteraBlockChain();
				
				carteraBlockChain.p_IdEmpresa 			= renovarPrestamoBlockchainEntrada.p_IdEmpresa;
				carteraBlockChain.p_FolioPrestamo 		= datosPrestamo.FolioPrestamoNuevo;
				carteraBlockChain.p_FolioSistemaExterno = renovarPrestamoBlockchainSalida.P_OUT_FOLIOSISTEMAEXTERNO;
				
				listaCarteraBlockChain.add(carteraBlockChain);
				
				new SQL().batch(contexto.Connection, SQL_UPDATE_FOLIOBLOCKCHAIN, listaCarteraBlockChain, CarteraBlockChain.class);
			}
			mensajes.addAll(renovarPrestamoBlockchainSalida.P_OUT_MENSAJES);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::RenovarPrestamoMasivo.informarRenovacionPrestamoEnBlockChain {}", fin - inicio);
		
		return mensajes;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== INICIO PRUEBA ==");
		
		try (Connection conn = PlataformaUtils.getDESAConnection()) {
			try {
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCanalIngreso\":\"OPT\",\"p_SolicitudesRenovacion\":[{\"IdInterno\":1,\"IdCartera\":1543750,\"IdEmpleadoDigitador\":7305,\"Cantidad\":308,\"Precio\":5691,\"TasaInstrumento\":1.09,\"Monto\":1752828,\"MontoPrima\":7005.469240000006,\"TasaPrima\":1.09,\"MontoPlazo\":1759833.46924,\"PlazoDiasVencimiento\":11,\"IdMonedaSistema\":\"CLP\",\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2023-03-10T03:00:00.000Z\",\"FechaVencimiento\":\"2023-03-21T03:00:00.000Z\"}]}";
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCanalIngreso\":\"OPT\",\"p_SolicitudesRenovacion\":[{\"IdInterno\":1,\"IdCartera\":1544457,\"IdEmpleadoDigitador\":7305,\"Cantidad\":50,\"Precio\":1002,\"TasaInstrumento\":0.2,\"Monto\":50100,\"MontoPrima\":13.360000000000582,\"TasaPrima\":0.2,\"MontoPlazo\":50113.36,\"PlazoDiasVencimiento\":4,\"IdMonedaSistema\":\"CLP\",\"IdCondicionLiquidacion\":\"T0\",\"FechaLiquidacion\":\"2023-03-13T03:00:00.000Z\",\"FechaVencimiento\":\"2023-03-17T03:00:00.000Z\"}]}";
				EntradaRenovarPrestamoMasivo entradaRenovarPrestamoMasivo = Utils.getGsonBuilder().fromJson(json, EntradaRenovarPrestamoMasivo.class);
				System.out.println("ENTRADA: " + entradaRenovarPrestamoMasivo);
				SalidaRenovarPrestamoMasivo salidaRenovarPrestamoMasivo = new RenovarPrestamoMasivo().logica(conn, entradaRenovarPrestamoMasivo);
				System.out.println("SALIDA: " + salidaRenovarPrestamoMasivo);
				if (salidaRenovarPrestamoMasivo.P_OUT_ERRORESCONTADOR > 0) {
					conn.rollback();
				} else {
					conn.commit();
				}
			} finally {
				conn.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("== FIN PRUEBA ==");
	}
	
}
