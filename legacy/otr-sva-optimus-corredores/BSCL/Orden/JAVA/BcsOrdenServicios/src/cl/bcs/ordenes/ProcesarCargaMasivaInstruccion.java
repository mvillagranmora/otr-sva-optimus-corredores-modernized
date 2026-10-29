package cl.bcs.ordenes;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.ordenes.CrearInstrucciones.EntradaCrearInstrucciones;
import cl.bcs.ordenes.CrearInstrucciones.SalidaCrearInstrucciones;
import cl.bcs.ordenes.pojo.Instruccion;
import cl.bcs.ordenes.pojo.Validacion;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ProcesarCargaMasivaInstruccion implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ProcesarCargaMasivaInstruccion.class);
	
	private final String SQL_SELECT_CLIENTE_PORTAFOLIO = loadResourceToString("cl/bcs/ordenes/sql/ProcesarCargaMasivaInstruccion_SelectClientePortafolio.sql");
	private final String SQL_SELECT_INSTRUMENTO = loadResourceToString("cl/bcs/ordenes/sql/ProcesarCargaMasivaInstruccion_SelectInstrumento.sql");
	private final String SQL_SELECT_INTERMEDIARIO = loadResourceToString("cl/bcs/ordenes/sql/ProcesarCargaMasivaInstruccion_SelectIntermediario.sql");
	private final String SQL_SELECT_EMISOR = loadResourceToString("cl/bcs/ordenes/sql/ProcesarCargaMasivaInstruccion_SelectEmisor.sql");
	private final String SQL_SELECT_CONDICION_LIQUIDACION = loadResourceToString("cl/bcs/ordenes/sql/ProcesarCargaMasivaInstruccion_SelectCondicionLiquidacion.sql");
	
	public static class EntradaProcesarCargaMasivaInstruccion extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdTipoCanalIngreso;

		public List<CargaInstruccion> p_Instrucciones;
		
		public EntradaProcesarCargaMasivaInstruccion() {
			this.p_Instrucciones = new ArrayList<>();
		}
	}
	
	public static class CargaInstruccion extends BcsPojo {
		public Long 		IdInterno;
		public String 		TipoInstruccion;
		public String 		Operacion;
		public String 		Nemotecnico;
		public String 		IdEmisor;
		public String 		FechaVencimiento;
		public BigDecimal 	Tasa;
		public BigDecimal 	ValorRescate;
		public String 		FechaTrade;
		public String 		CondicionLiquidacion;
		public Long 		Intermediario;
		public String 		NumeroFactura;
		public BigDecimal 	Cantidad;
		public BigDecimal 	MontoTotal;
		public String 		DocumentoIdentidad;
		public String 		CodigoPortafolio;
		public String		IdTipoReajuste;
		public BigDecimal	TotalCobros;
		public BigDecimal 	Precio;
	}
	
	public static class CargaInstruccionProceso extends CargaInstruccion {
		public java.sql.Date FechaLiquidacion;
		public String IdClasificacionTipoInstrumento;
		public String IdClasificacionTipoInstrumentoInstruccion;
		public String IdTipoInstrumento;
		public String IdTipoInstrumentoInstruccion;
		public String IdTipoOperacionInstruccion;
		public String IdCondicionDeLiquidacion;
		public Long IdInstrumento;
		public Long IdCliente;
		public String IdEstadoCliente;
		public Long IdPortafolio;
		public String IdEstadoPortafolio;
		public String NombreCompleto;
		public String IdBolsa;
		public String IdBoveda;
		public Long IdIntermediario;
		public String IdMonedaLiquidacion;
		public BigDecimal PrecioInstruccion;
		public BigDecimal MontoNeto;
		public BigDecimal TotalCobros;
		public String Glosa;
		public Long PlazoVencimiento;
		public BigDecimal ValorNominal;
		public String IdTipoReajuste;
		public String IdEstadoProceso;
		public String EsFungible;
		public BigDecimal CorteMinimo;
		public String CodMotivoInstruccion;
		public CargaInstruccionProceso() {
			this.IdEstadoProceso = "PEN";
		}
	}
	
	public static class SalidaProcesarCargaMasivaInstruccion extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long	P_OUT_ERRORESCONTADOR;
		public List<Entidad> P_OUT_REFID;
		
		public SalidaProcesarCargaMasivaInstruccion() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_REFID = new ArrayList<>();
		}

		public void agregarValidacion(Validacion validacion) {
			if (validacion.EsError) {
				P_OUT_ERRORESCONTADOR++;
			}
			P_OUT_MENSAJES.add(validacion.Mensaje);
		}

		public void agregarValidaciones(List<Validacion> validaciones) {
			for (Validacion validacion : validaciones) {
				agregarValidacion(validacion);
			}
		}
	}
	
	public static class ClientePortafolio extends BcsPojo {
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public Long IdCliente;
		public String IdEstadoCliente;
		public Long IdPortafolio;
		public String IdEstadoPortafolio;
		public String NombreCompleto;
	}
	
	public static class Instrumento extends BcsPojo {
		public String IdClasificacionTipoInstrumento;
		public String IdTipoInstrumento;
		public String Nemotecnico;
		public Long IdInstrumento;
		public String IdMonedaEmision;
		public BigDecimal CorteMinimo;
		public String IdEmisor;
	}
	
	public static class Intermediario extends BcsPojo {
		public Long IdIntermediario;
		public Long Intermediario;
	}
	
	public static class Emisor extends BcsPojo {
		public String IdEmisor;
		public String EmiteFungible;
	}
	
	public static class CondicionDeLiquidacion extends BcsPojo {
		public String IdCondicionDeLiquidacion;
		public Long PlazoEnDias;
		public String Codigo;
	}
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaProcesarCargaMasivaInstruccion in = gson.fromJson(jin.toString(), EntradaProcesarCargaMasivaInstruccion.class);
		SalidaProcesarCargaMasivaInstruccion out = logica(con, in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaProcesarCargaMasivaInstruccion logica(Connection con, EntradaProcesarCargaMasivaInstruccion entrada) throws Exception {
		logger.info("INICIO::ProcesarCargaMasivaInstruccion");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaProcesarCargaMasivaInstruccion out = new SalidaProcesarCargaMasivaInstruccion();
		
		List<CargaInstruccionProceso> instruccionesProceso = obtenerInstruccionesProceso(con, entrada.p_Instrucciones, entrada.p_IdTipoCanalIngreso, entrada.p_IdEmpresa);
		
		List<Validacion> validacionesInstruccciones = validarInstrucciones(con, instruccionesProceso, entrada.p_IdEmpresa, entrada.p_IdTipoCanalIngreso);
		out.agregarValidaciones(validacionesInstruccciones);
		
		instruccionesProceso = marcarInstruccionesConError(instruccionesProceso, out.P_OUT_MENSAJES);
		
		SalidaCrearInstrucciones salidaCrearInstrucciones = crearInstrucciones(con, entrada, instruccionesProceso);
		out.P_OUT_ERRORESCONTADOR += salidaCrearInstrucciones.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES.addAll(salidaCrearInstrucciones.P_OUT_MENSAJES);
		out.P_OUT_REFID.addAll(salidaCrearInstrucciones.P_OUT_REFID);
		
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ProcesarCargaMasivaInstruccion", finServicio - inicioServicio);
		return out;
	}
	
	private List<CargaInstruccionProceso> obtenerInstruccionesProceso(Connection con, List<CargaInstruccion> instrucciones, String canal, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ProcesarCargaMasivaInstruccion.obtenerInstruccionesProceso");
		long inicio = System.currentTimeMillis();
		
		List<ClientePortafolio> clientesPortafolio = obtenerClientePortafolio(con, instrucciones);
		List<Instrumento> instrumentos = obtenerInstrumento(con, instrucciones);
		List<Intermediario> intermediarios = obtenerIntermediario(con, instrucciones);
		List<Emisor> emisores = obtenerEmisor(con, instrucciones);
		List<CondicionDeLiquidacion> condicionesLiquidacion = obtenerCondicionLiquidacion(con, instrucciones);
		
		Map<String, ClientePortafolio> clientesPortafolioPorDocumentoIdentidadCodigoPortafolio = clientesPortafolio
				.stream()
				.collect(Collectors.toMap(clientePortafolio->clientePortafolio.DocumentoIdentidad + "_" + clientePortafolio.CodigoPortafolio, clientePortafolio->clientePortafolio));
		Map<String, Instrumento> instrumentoPorNemotecnico = instrumentos
				.stream()
				.collect(Collectors.toMap(instrumento->instrumento.Nemotecnico, instrumento->instrumento));
		Map<Long, Intermediario> intermediarioPorCodigoCorredora = intermediarios
				.stream()
				.collect(Collectors.toMap(intermedario->intermedario.Intermediario, intermedario->intermedario));
		Map<String, Emisor> emisoresPorIdEmisor = emisores
				.stream()
				.collect(Collectors.toMap(emisor->emisor.IdEmisor, emisor->emisor));
		Map<String, CondicionDeLiquidacion> condicionesLiquidacionPorCodigo = condicionesLiquidacion
				.stream()
				.collect(Collectors.toMap(condicionLiquidacion->condicionLiquidacion.Codigo, condicionLiquidacion->condicionLiquidacion));
		
		Map<String, java.sql.Date> fechasLiquidacion = obtenerFechaLiquidacion(con, instrucciones, condicionesLiquidacionPorCodigo, idEmpresa);
		
		List<CargaInstruccionProceso> instruccionesProceso = instrucciones
				.stream()
				.map(instruccion->{
					CargaInstruccionProceso instruccionProceso = new CargaInstruccionProceso();
					instruccionProceso.IdInterno = instruccion.IdInterno;
					instruccionProceso.TipoInstruccion = instruccion.TipoInstruccion;
					instruccionProceso.Operacion = instruccion.Operacion;
					instruccionProceso.Nemotecnico = instruccion.Nemotecnico;
					instruccionProceso.IdEmisor = instruccion.IdEmisor;
					instruccionProceso.FechaVencimiento = instruccion.FechaVencimiento;
					instruccionProceso.Tasa = instruccion.Tasa;					
					instruccionProceso.FechaTrade = instruccion.FechaTrade;
					instruccionProceso.Intermediario = instruccion.Intermediario;
					instruccionProceso.NumeroFactura = instruccion.NumeroFactura;					
					instruccionProceso.MontoTotal = instruccion.MontoTotal;
					instruccionProceso.DocumentoIdentidad = instruccion.DocumentoIdentidad;
					instruccionProceso.CodigoPortafolio = instruccion.CodigoPortafolio;
					instruccionProceso.IdTipoInstrumentoInstruccion = obtenerIdTipoInstrumentoInstruccion(instruccion.TipoInstruccion);
					instruccionProceso.IdTipoOperacionInstruccion = obtenerIdTipoOperacionInstruccion(instruccion.Operacion, instruccionProceso.IdTipoInstrumentoInstruccion);
					instruccionProceso.CodMotivoInstruccion = obtenerCodigoMotivoInstruccion(canal);
					instruccionProceso.PrecioInstruccion = "IRV".equals(instruccionProceso.IdTipoInstrumentoInstruccion) && instruccion.Precio.compareTo(BigDecimal.ZERO) == 0 ? obtenerPrecio(instruccion) : instruccion.Precio;
					instruccionProceso.TotalCobros = instruccion.TotalCobros;
					instruccionProceso.MontoNeto = instruccion.MontoTotal.subtract(instruccionProceso.TotalCobros, MathContext.DECIMAL128);
					instruccionProceso.Glosa = "";
					instruccionProceso.IdMonedaLiquidacion = "CLP";
					instruccionProceso.IdClasificacionTipoInstrumentoInstruccion = "IR";
					instruccionProceso.IdBolsa = "BCS";
					instruccionProceso.IdBoveda = "DCV";
					instruccionProceso.PlazoVencimiento = 0L;
					ClientePortafolio clientePortafolio = clientesPortafolioPorDocumentoIdentidadCodigoPortafolio.get(instruccion.DocumentoIdentidad + "_" + instruccion.CodigoPortafolio);
					if(clientePortafolio != null) {
						instruccionProceso.IdCliente = clientePortafolio.IdCliente;
						instruccionProceso.IdEstadoCliente = clientePortafolio.IdEstadoCliente;
						instruccionProceso.IdPortafolio = clientePortafolio.IdPortafolio;
						instruccionProceso.IdEstadoPortafolio = clientePortafolio.IdEstadoPortafolio;
						instruccionProceso.NombreCompleto = clientePortafolio.NombreCompleto;
					}
					Instrumento instrumento = instrumentoPorNemotecnico.get(instruccion.Nemotecnico);
					if(instrumento != null) {
						instruccionProceso.IdClasificacionTipoInstrumento = instrumento.IdClasificacionTipoInstrumento;
						instruccionProceso.IdTipoInstrumento = instrumento.IdTipoInstrumento;						
						instruccionProceso.IdInstrumento = instrumento.IdInstrumento;
						instruccionProceso.CorteMinimo = instrumento.CorteMinimo;
						instruccionProceso.IdTipoReajuste = "IFI".equals(instruccionProceso.IdTipoInstrumentoInstruccion) ? instruccion.IdTipoReajuste : instrumento.IdMonedaEmision;
					}
					Intermediario intermedario = intermediarioPorCodigoCorredora.get(instruccion.Intermediario);
					if(intermedario != null) {
						instruccionProceso.IdIntermediario = intermedario.IdIntermediario;
					}
					Emisor emisor = emisoresPorIdEmisor.get(instruccion.IdEmisor);
					if(emisor != null) {
						instruccionProceso.EsFungible = emisor.EmiteFungible;
					}
					CondicionDeLiquidacion condicionLiquidacion = condicionesLiquidacionPorCodigo.get(instruccion.CondicionLiquidacion);
					if(condicionLiquidacion != null) {
						instruccionProceso.IdCondicionDeLiquidacion = condicionLiquidacion.IdCondicionDeLiquidacion;
						instruccionProceso.FechaLiquidacion = fechasLiquidacion.get(instruccion.FechaTrade + "_" + condicionLiquidacion.PlazoEnDias);
					}
					instruccionProceso.ValorRescate = "IFI".equals(instruccionProceso.IdTipoInstrumentoInstruccion) ? instruccion.ValorRescate : BigDecimal.ZERO;
					instruccionProceso.ValorNominal = "IRF".equals(instruccionProceso.IdTipoInstrumentoInstruccion) ? instruccion.Cantidad : BigDecimal.ZERO;
					instruccionProceso.Cantidad = "IFI".equals(instruccionProceso.IdTipoInstrumentoInstruccion) ? obtenerCantidadCorte(instruccionProceso) : instruccion.Cantidad;					
					return instruccionProceso;
				})
				.collect(Collectors.toList());
		
		Map<Long, Long> plazosPorIdInterno = obtenerPlazo(con, instruccionesProceso, idEmpresa);
		instruccionesProceso
		.stream()
		.filter(instruccion->plazosPorIdInterno.get(instruccion.IdInterno) != null)
		.forEach(instruccion->instruccion.PlazoVencimiento = plazosPorIdInterno.get(instruccion.IdInterno));
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCargaMasivaInstruccion.obtenerInstruccionesProceso", fin - inicio);
		return instruccionesProceso;
	}
	
	private List<ClientePortafolio> obtenerClientePortafolio(Connection con, List<CargaInstruccion> instrucciones) throws Exception {
		logger.debug("INICIO::ProcesarCargaMasivaInstruccion.obtenerClientePortafolio");
		long inicio = System.currentTimeMillis();
		
		List<String> documentosIdentidad = instrucciones
				.stream()
				.map(instruccion->instruccion.DocumentoIdentidad)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("DocumentoIdentidad", documentosIdentidad);
		
		SQL sql = new SQL();
		List<ClientePortafolio> clientesPortafolio = sql.query(con, SQL_SELECT_CLIENTE_PORTAFOLIO, parametros, ClientePortafolio.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCargaMasivaInstruccion.obtenerClientePortafolio", fin - inicio);
		return clientesPortafolio;
	}

	private List<Instrumento> obtenerInstrumento(Connection con, List<CargaInstruccion> instrucciones) throws Exception {
		logger.debug("INICIO::ProcesarCargaMasivaInstruccion.obtenerInstrumento");
		long inicio = System.currentTimeMillis();
		
		List<String> nemotecnicos = instrucciones
				.stream()
				.map(instruccion->instruccion.Nemotecnico)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("Nemotecnico", nemotecnicos);
		
		SQL sql = new SQL();
		List<Instrumento> instrumentos = sql.query(con, SQL_SELECT_INSTRUMENTO, parametros, Instrumento.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCargaMasivaInstruccion.obtenerInstrumento", fin - inicio);
		return instrumentos;
	}
	
	private List<Intermediario> obtenerIntermediario(Connection con, List<CargaInstruccion> instrucciones) throws Exception {
		logger.debug("INICIO::ProcesarCargaMasivaInstruccion.obtenerIntermediario");
		long inicio = System.currentTimeMillis();
		
		List<Long> codigosCorredora = instrucciones
				.stream()
				.map(instruccion->instruccion.Intermediario)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("Intermediario", codigosCorredora);
		
		SQL sql = new SQL();
		List<Intermediario> intermediarios = sql.query(con, SQL_SELECT_INTERMEDIARIO, parametros, Intermediario.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCargaMasivaInstruccion.obtenerIntermediario", fin - inicio);
		return intermediarios;
	}
		 	
	private List<Emisor> obtenerEmisor(Connection con, List<CargaInstruccion> instrucciones) throws Exception {
		logger.debug("INICIO::ProcesarCargaMasivaInstruccion.obtenerEmisor");
		long inicio = System.currentTimeMillis();
		
		List<String> idEmisores = instrucciones
				.stream()
				.map(instruccion->instruccion.IdEmisor)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdEmisor", idEmisores);
		
		SQL sql = new SQL();
		List<Emisor> emisores = sql.query(con, SQL_SELECT_EMISOR, parametros, Emisor.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCargaMasivaInstruccion.obtenerEmisor", fin - inicio);
		return emisores;
	}
	
	private List<CondicionDeLiquidacion> obtenerCondicionLiquidacion(Connection con, List<CargaInstruccion> instrucciones) throws Exception {
		logger.debug("INICIO::ProcesarCargaMasivaInstruccion.obtenerCondicionLiquidacion");
		long inicio = System.currentTimeMillis();
		
		List<String> codigos = instrucciones
				.stream()
				.map(instruccion->instruccion.CondicionLiquidacion)
				.distinct()
				.collect(Collectors.toList());
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("Codigo", codigos);
		
		SQL sql = new SQL();
		List<CondicionDeLiquidacion> condicionesDeLiquidacion = sql.query(con, SQL_SELECT_CONDICION_LIQUIDACION, parametros, CondicionDeLiquidacion.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCargaMasivaInstruccion.obtenerCondicionLiquidacion", fin - inicio);
		return condicionesDeLiquidacion;
	}
	
	private String obtenerIdTipoInstrumentoInstruccion(String tipoInstruccion) {
		if("RV".equals(tipoInstruccion)) {
			return "IRV";
		}
		if("RF".equals(tipoInstruccion)) {
			return "IRF";
		}
		if("IF".equals(tipoInstruccion)) {
			return "IFI";
		}
		return null;
	}
	
	private String obtenerIdTipoOperacionInstruccion(String operacion, String idTipoInstrumentoInstruccion) {
		if("CO".equals(operacion)) {
			return "COM" + idTipoInstrumentoInstruccion;
		}
		if("VE".equals(operacion)) {
			return "VEN" + idTipoInstrumentoInstruccion;
		}
		if("EN".equals(operacion)) {
			return "ENT" + idTipoInstrumentoInstruccion;
		}
		if("RE".equals(operacion)) {
			return "REC" + idTipoInstrumentoInstruccion;
		}
		return null;
	}
	
	private String obtenerCodigoMotivoInstruccion(String canal) {
		if("CLI".equals(canal)) {
			return "CMCLI";
		}
		if("AGE".equals(canal)) {
			return "CMCUS";
		}
		return null;
	}
	
	private BigDecimal obtenerPrecio(CargaInstruccion instruccion) {
		if("RV".equals(instruccion.TipoInstruccion) && instruccion.Cantidad.compareTo(BigDecimal.ZERO) != 0 ) {
			return instruccion.MontoTotal.divide(instruccion.Cantidad, MathContext.DECIMAL128);
		}
		return BigDecimal.ZERO;
	}
	
	private Map<String, java.sql.Date> obtenerFechaLiquidacion(Connection con, List<CargaInstruccion> instrucciones, Map<String, CondicionDeLiquidacion> condicionesDeLiquidacionPorCodigo, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ProcesarCargaMasivaInstruccion.obtenerFechaLiquidacion");
		long inicio = System.currentTimeMillis();
		
		Map<String, java.sql.Date> fechasLiquidacion = new HashMap<String, java.sql.Date>(instrucciones.size() + 1);
		
		ObtenerDiaSiguiente.InObtenerDiaSiguiente inObtenerDiaSiguiente = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
		inObtenerDiaSiguiente.p_idPais = obtenerIdPaisSistema(con, idEmpresa);
		inObtenerDiaSiguiente.p_diaHabil = "S";
		inObtenerDiaSiguiente.p_soloCaeDiaHabil = "S";
		inObtenerDiaSiguiente.p_fechas = instrucciones
				.stream()
				.filter(instruccion->condicionesDeLiquidacionPorCodigo.get(instruccion.CondicionLiquidacion) != null)
				.map(instruccion->{
					ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn fecha = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
					fecha.fechaInicial = instruccion.FechaTrade;
					fecha.numeroDias = condicionesDeLiquidacionPorCodigo.get(instruccion.CondicionLiquidacion).PlazoEnDias;
					fecha.pais = inObtenerDiaSiguiente.p_idPais;
					return fecha;
				})
				.collect(Collectors.toList());
		
		if(inObtenerDiaSiguiente.p_fechas.size() > 0) {
			ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
			ObtenerDiaSiguiente.OutObtenerDiaSiguiente outObtenerDiaSiguiente = obtenerDiaSiguiente.logica(con, inObtenerDiaSiguiente);
			for(ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut fecha : outObtenerDiaSiguiente.p_out_fechas) {
				fechasLiquidacion.put(fecha.fechaInicial + "_" + fecha.numeroDiasInicial, Fecha.toDate(fecha.fechaCalculada));
			}
		}
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCargaMasivaInstruccion.obtenerFechaLiquidacion", fin - inicio);
		return fechasLiquidacion;
	}
	
	private Map<Long, Long> obtenerPlazo(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa) throws Exception{
		logger.debug("INICIO::ProcesarCargaMasivaInstruccion.obtenerPlazo");
		long inicio = System.currentTimeMillis();
		
		Map<Long, Long> plazosPorIdInterno = new HashMap<Long, Long>(1);
		
		ObtenerPlazo.InObtenerPlazo inObtenerPlazo = new ObtenerPlazo.InObtenerPlazo();
		inObtenerPlazo.p_IdPais = obtenerIdPaisSistema(con, idEmpresa);
		inObtenerPlazo.p_Fechas = instrucciones
				.stream()
				.filter(instruccion->esOperacionValidaParaPlazo(instruccion.TipoInstruccion) && instruccion.FechaLiquidacion != null)
				.map(instruccion->{
					ObtenerPlazo.FechasIn fecha = new ObtenerPlazo.FechasIn();
					fecha.IdInterno = instruccion.IdInterno;
					fecha.FechaDesde = Fecha.toStringDate(instruccion.FechaLiquidacion);
					fecha.FechaHasta = instruccion.FechaVencimiento;
					fecha.SoloDiasHabiles = "N";
					return fecha;
				})
				.collect(Collectors.toList());
		
		ObtenerPlazo obtenerPlazo = new ObtenerPlazo();
		
		if(inObtenerPlazo.p_Fechas.size() > 0) {
			ObtenerPlazo.OutObtenerPlazo outObtenerPlazo = obtenerPlazo.logica(con, inObtenerPlazo);
			plazosPorIdInterno = outObtenerPlazo.p_out_Fechas
					.stream()
					.collect(Collectors.toMap(fecha->fecha.IdInterno,fecha->fecha.Plazo));
		}
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCargaMasivaInstruccion.obtenerPlazo", fin - inicio);
		return plazosPorIdInterno;
	}
	
	private Long obtenerIdPaisSistema(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ProcesarCargaMasivaInstruccion.obtenerIdPaisSistema");
		long inicio = System.currentTimeMillis();
		Long idPais = ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCargaMasivaInstruccion.obtenerIdPaisSistema", fin - inicio);
		return idPais;
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::ProcesarCargaMasivaInstruccion.obtenerFechaSistema");
		long inicio = System.currentTimeMillis();		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA","PLA.FechaSistema");			
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCargaMasivaInstruccion.obtenerFechaSistema", fin - inicio);
		return fechaSistema;
	}
	
	private Boolean esOperacionValidaParaPlazo(String tipoInstruccion) {
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.esOperacionValidaParaPlazo");
		long inicio = System.currentTimeMillis();
		Boolean esOperacionValidaParaPlazo = "RF".equals(tipoInstruccion) || "IF".equals(tipoInstruccion);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ProcesarCargaMasivaInstruccion.esOperacionValidaParaPlazo", fin - inicio);
		return esOperacionValidaParaPlazo;
	}
	
	private List<Validacion> validarInstrucciones(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa, String canal) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarInstrucciones");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = new ArrayList<>();
		validaciones.addAll(validarCliente(con, instrucciones, idEmpresa));
		validaciones.addAll(validarTipoInstruccion(con, instrucciones, idEmpresa));
		validaciones.addAll(validarTipoOperacion(con, instrucciones, idEmpresa));
		validaciones.addAll(validarMotivo(con, instrucciones, idEmpresa, canal));
		validaciones.addAll(validarInstrumento(con, instrucciones, idEmpresa));
		validaciones.addAll(validarIntermediario(con, instrucciones, idEmpresa));
		validaciones.addAll(validarEmisor(con, instrucciones, idEmpresa));
		validaciones.addAll(validarCondicionDeLiquidacion(con, instrucciones, idEmpresa));
		validaciones.addAll(validarFechaVencimiento(con, instrucciones, idEmpresa));
		validaciones.addAll(validarValorRescate(con, instrucciones, idEmpresa));
		validaciones.addAll(validarCantidad(con, instrucciones, idEmpresa));
		validaciones.addAll(validarValorNominal(con, instrucciones, idEmpresa));
		validaciones.addAll(validarFechaTrade(con, instrucciones, idEmpresa));
		validaciones.addAll(validarMultiploCorte(con, instrucciones, idEmpresa));
		validaciones.addAll(validarCorteMinimo(con, instrucciones, idEmpresa));

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarInstrucciones", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarCliente(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa){
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarCliente");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->instruccion.IdCliente == null)
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(3);
					parametros.put("DocumentoIdentidad", instruccion.DocumentoIdentidad);
					parametros.put("CodigoPortafolio", instruccion.CodigoPortafolio);
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CARGA_INSTRUCCIONES_CLIENTE_NO_EXISTE", parametros, con, idEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_CARGA_INSTRUCCIONES_CLIENTE_NO_EXISTE sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarCliente", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarTipoInstruccion(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa){
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarTipoInstruccion");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->instruccion.IdTipoInstrumentoInstruccion == null)
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("TipoInstruccion", instruccion.TipoInstruccion);
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CARGA_INSTRUCCIONES_TIPO_INSTRUCCION_NO_EXISTE", parametros, con, idEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_CARGA_INSTRUCCIONES_TIPO_INSTRUCCION_NO_EXISTE sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarTipoInstruccion", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarTipoOperacion(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa){
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarTipoOperacion");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->instruccion.IdTipoOperacionInstruccion == null)
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("Operacion", instruccion.Operacion);
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CARGA_INSTRUCCIONES_OPERACION_NO_EXISTE", parametros, con, idEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_CARGA_INSTRUCCIONES_OPERACION_NO_EXISTE sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarTipoOperacion", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarMotivo(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa, String canal){
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarMotivo");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->instruccion.IdTipoOperacionInstruccion == null)
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("Canal", canal);
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CARGA_INSTRUCCIONES_MOTIVO_NO_EXISTE", parametros, con, idEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_CARGA_INSTRUCCIONES_MOTIVO_NO_EXISTE sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarMotivo", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarInstrumento(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa){
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarInstrumento");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->instruccion.IdInstrumento == null)
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("Nemotecnico", instruccion.Nemotecnico);
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CARGA_INSTRUCCIONES_INSTRUMENTO_NO_EXISTE", parametros, con, idEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_CARGA_INSTRUCCIONES_INSTRUMENTO_NO_EXISTE sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarInstrumento", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarIntermediario(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa){
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarIntermediario");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->instruccion.IdIntermediario == null)
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("Intermediario", String.valueOf(instruccion.Intermediario));
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CARGA_INSTRUCCIONES_INTERMEDIARIO_NO_EXISTE", parametros, con, idEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_CARGA_INSTRUCCIONES_INTERMEDIARIO_NO_EXISTE sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarIntermediario", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarEmisor(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa){
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarEmisor");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->instruccion.EsFungible == null)
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("IdEmisor", String.valueOf(instruccion.IdEmisor));
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CARGA_INSTRUCCIONES_EMISOR_NO_EXISTE", parametros, con, idEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_CARGA_INSTRUCCIONES_EMISOR_NO_EXISTE sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarEmisor", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarCondicionDeLiquidacion(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa){
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarCondicionDeLiquidacion");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->instruccion.IdCondicionDeLiquidacion == null)
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("CondicionLiquidacion", String.valueOf(instruccion.CondicionLiquidacion));
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CARGA_INSTRUCCIONES_CONDICION_LIQUIDACION_NO_EXISTE", parametros, con, idEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_CARGA_INSTRUCCIONES_CONDICION_LIQUIDACION_NO_EXISTE sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarCondicionDeLiquidacion", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarFechaVencimiento(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa){
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarFechaVencimiento");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->(instruccion.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF") || instruccion.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF")) )
				.filter(instruccion->instruccion.FechaVencimiento.equals("0001-01-01") )
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("FechaVencimiento", String.valueOf(instruccion.FechaVencimiento));
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CARGA_INSTRUCCIONES_FECHA_VENCIMIENTO_NO_EXISTE", parametros, con, idEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_CARGA_INSTRUCCIONES_FECHA_VENCIMIENTO_NO_EXISTE sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarFechaVencimiento", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarValorRescate(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa){
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarValorRescate");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->instruccion.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF"))
				.filter(instruccion->instruccion.ValorRescate.compareTo(BigDecimal.ZERO) == 0 )
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("ValorRescate", String.valueOf(instruccion.ValorRescate));
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CARGA_INSTRUCCIONES_VALOR_RESCATE_NO_INFORMADO", parametros, con, idEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_CARGA_INSTRUCCIONES_VALOR_RESCATE_NO_INFORMADO sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarValorRescate", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarCantidad(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa){
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarCantidad");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->instruccion.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV"))
				.filter(instruccion->instruccion.Cantidad.compareTo(BigDecimal.ZERO) == 0 )
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("Cantidad", String.valueOf(instruccion.Cantidad));
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CARGA_INSTRUCCIONES_CANTIDAD_NO_INFORMADO", parametros, con, idEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_CARGA_INSTRUCCIONES_CANTIDAD_NO_INFORMADO sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarCantidad", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarValorNominal(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa){
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarValorNominal");
		long inicio = System.currentTimeMillis();
		
		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->instruccion.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF"))
				.filter(instruccion->instruccion.ValorNominal.compareTo(BigDecimal.ZERO) == 0 )
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("ValorNominal", String.valueOf(instruccion.ValorNominal));
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CARGA_INSTRUCCIONES_VALOR_NOMINAL_NO_INFORMADO", parametros, con, idEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_CARGA_INSTRUCCIONES_VALOR_NOMINAL_NO_INFORMADO sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarValorNominal", fin - inicio);
		return validaciones;
	}	
	
	private List<Validacion> validarFechaTrade(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarFechaTrade");
		long inicio = System.currentTimeMillis();
		String  fechaSistema = obtenerFechaSistema(con, idEmpresa);

		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->validarFechaMayor(fechaSistema, instruccion.FechaTrade))
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("FechaTrade", String.valueOf(instruccion.FechaTrade));
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CARGA_INSTRUCCIONES_FECHA_TRADE_MAYOR_FECHA_SISTEMA", parametros, con, idEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_CARGA_INSTRUCCIONES_FECHA_TRADE_MAYOR_FECHA_SISTEMA sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarFechaTrade", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarMultiploCorte(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa){
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarMultiploCorte");
		long inicio = System.currentTimeMillis();

		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->!instruccion.IdClasificacionTipoInstrumento.equalsIgnoreCase("RV"))
				.filter(instruccion->!esMultiploCorte(instruccion))
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("Nemotecnico", String.valueOf(instruccion.Nemotecnico));
					parametros.put("IdEmisor", String.valueOf(instruccion.IdEmisor));
					parametros.put("CorteMinimo", String.valueOf(instruccion.CorteMinimo));
					parametros.put("tipoValor", String.valueOf(instruccion.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF") ? "Nominal" : "Rescate"));
					parametros.put("Valor", String.valueOf(instruccion.IdClasificacionTipoInstrumento.equalsIgnoreCase("RF") ? instruccion.ValorNominal : instruccion.ValorRescate));
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_CARGA_INSTRUCCIONES_NOES_MULTIPLO_CORTE", parametros, con, idEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "UI_CARGA_INSTRUCCIONES_NOES_MULTIPLO_CORTE sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarMultiploCorte", fin - inicio);
		return validaciones;
	}
	
	private List<Validacion> validarCorteMinimo(Connection con, List<CargaInstruccionProceso> instrucciones, Long idEmpresa){
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarCorteMinimo");
		long inicio = System.currentTimeMillis();

		List<Validacion> validaciones = instrucciones
				.stream()
				.filter(instruccion->
					("IF".equals(instruccion.TipoInstruccion) && "N".equals(instruccion.EsFungible) && instruccion.CorteMinimo.compareTo(BigDecimal.ZERO) == 0)
					||
					("RF".equals(instruccion.TipoInstruccion) && instruccion.CorteMinimo.compareTo(BigDecimal.ZERO) == 0)
				)
				.map(instruccion->{
					Validacion validacion = new Validacion();
					HashMap<String, String> parametros = new HashMap<String, String>(2);
					parametros.put("Nemotecnico", String.valueOf(instruccion.Nemotecnico));
					parametros.put("IdEmisor", String.valueOf(instruccion.IdEmisor));
					parametros.put("CorteMinimo", String.valueOf(instruccion.CorteMinimo));
					try {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "CORTE_MINIMO_INVALIDO", parametros, con, idEmpresa);
					} catch(Exception error) {
						validacion.Mensaje = new MensajeSalida(instruccion.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_MENSAJE_NO_EXISTE", "CORTE_MINIMO_INVALIDO sin traduccion.");
					}
					
					validacion.EsError = true;
					return validacion;
				})
				.collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarCorteMinimo", fin - inicio);
		return validaciones;
	}

	private boolean esMultiploCorte(CargaInstruccionProceso instruccion) {
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.esMultiploCorte");
		long inicio = System.currentTimeMillis();
		boolean isBefore = false;
		
		if ("RF".equals(instruccion.TipoInstruccion)){
			isBefore = instruccion.ValorNominal.remainder(instruccion.CorteMinimo).compareTo(BigDecimal.ZERO) == 0;
		}
		if ("IF".equals(instruccion.TipoInstruccion)){
			isBefore = instruccion.ValorRescate.remainder(instruccion.CorteMinimo).compareTo(BigDecimal.ZERO) == 0;
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.esMultiploCorte", fin - inicio);
		return isBefore; 
	}
	
	private BigDecimal obtenerCantidadCorte(CargaInstruccionProceso instruccion) {	
		if ("IF".equals(instruccion.TipoInstruccion)){
			if ("S".equals(instruccion.EsFungible) ) {
				return BigDecimal.ONE;
			}
			else if ("N".equals(instruccion.EsFungible) && instruccion.CorteMinimo.compareTo(BigDecimal.ZERO) != 0) {
				return instruccion.ValorRescate.divide(instruccion.CorteMinimo, MathContext.DECIMAL128);
			}
		}
		if ("RF".equals(instruccion.TipoInstruccion)){
			return instruccion.Cantidad;
		}
		
		return BigDecimal.ZERO;
	}
	
	private boolean validarFechaMayor (String FechaMenor , String FechaMayor) {
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.validarFechaMayor");
		long inicio = System.currentTimeMillis();
		boolean isBefore = false;
			
		SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd");
		try {
			Date dateMenor = dateFormat.parse(FechaMenor);
			Date dateMayor = dateFormat.parse(FechaMayor);
			isBefore = dateMenor.before(dateMayor);     
		} catch (Exception er) {
			er.printStackTrace();
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.validarFechaMayor", fin - inicio);
		return isBefore; 
	}
	
	private List<CargaInstruccionProceso> marcarInstruccionesConError(List<CargaInstruccionProceso> instruccionesProceso, List<MensajeSalida> mensajesSalida) throws Exception {
		logger.trace("INICIO::ProcesarCargaMasivaInstruccion.marcarInstruccionesConError");
		long inicio = System.currentTimeMillis();
		
		Map<Long, List<MensajeSalida>> validacionesPorIdInterno = mensajesSalida
				.stream()
				.filter(mensajeSalida->MensajeSalida.MENSAJE_ERROR.equals(mensajeSalida.tipo))
				.collect(Collectors.groupingBy(mensajeSalida->mensajeSalida.id));
		
		instruccionesProceso
			.stream()
			.filter(instruccion->validacionesPorIdInterno.get(instruccion.IdInterno) != null)
			.forEach(instruccion->{
				instruccion.IdEstadoProceso = "ERR";
			});
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.marcarInstruccionesConError", fin - inicio);
		return instruccionesProceso;
	}
	
	private SalidaCrearInstrucciones crearInstrucciones(Connection con, EntradaProcesarCargaMasivaInstruccion entrada, List<CargaInstruccionProceso> instruccionesProceso) throws Exception {
		logger.debug("INICIO::ProcesarCargaMasivaInstruccion.crearInstrucciones");
		long inicio = System.currentTimeMillis();
		
		List<CargaInstruccionProceso> instruccionesValidadas = instruccionesProceso
				.stream()
				.filter(instruccion->"PEN".equals(instruccion.IdEstadoProceso))
				.collect(Collectors.toList());
		
		SalidaCrearInstrucciones salidaCrearInstrucciones = new SalidaCrearInstrucciones();
		
		if(instruccionesValidadas.size() > 0) {
			EntradaCrearInstrucciones entradaCrearInstrucciones = new EntradaCrearInstrucciones();
			entradaCrearInstrucciones.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaCrearInstrucciones.p_IdUsuario = entrada.p_IdUsuario;
			entradaCrearInstrucciones.p_IdModulo = entrada.p_IdModulo;
			entradaCrearInstrucciones.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaCrearInstrucciones.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
			
			entradaCrearInstrucciones.p_Instrucciones = instruccionesValidadas
					.stream()
					.map(instruccionProceso->{
						Instruccion instruccion = new Instruccion();
						instruccion.IdInterno = instruccionProceso.IdInterno; 
						instruccion.FechaTrade = Fecha.toDate(instruccionProceso.FechaTrade); 
						instruccion.FechaLiquidacion = instruccionProceso.FechaLiquidacion; 
						instruccion.IdClasificacionTipoInstrumento = instruccionProceso.IdClasificacionTipoInstrumento;
						instruccion.IdClasificacionTipoInstrumentoInstruccion = instruccionProceso.IdClasificacionTipoInstrumentoInstruccion;
						instruccion.IdTipoInstrumentoInstruccion = instruccionProceso.IdTipoInstrumentoInstruccion;
						instruccion.IdTipoInstrumento = instruccionProceso.IdTipoInstrumento;
						instruccion.IdTipoOperacionInstruccion = instruccionProceso.IdTipoOperacionInstruccion;
						instruccion.IdInstrumento = instruccionProceso.IdInstrumento; 
						instruccion.IdEmisor = instruccionProceso.IdEmisor;
						instruccion.Nemotecnico = instruccionProceso.Nemotecnico;
						instruccion.IdCliente = instruccionProceso.IdCliente; 
						instruccion.IdEstadoCliente = instruccionProceso.IdEstadoCliente; 
						instruccion.IdPortafolioCliente = instruccionProceso.IdPortafolio;
						instruccion.IdEstadoPortafolio = instruccionProceso.IdEstadoPortafolio;
						instruccion.DocumentoIdentidad = instruccionProceso.DocumentoIdentidad;
						instruccion.NombreCompleto = instruccionProceso.NombreCompleto;
						instruccion.IdBolsa = instruccionProceso.IdBolsa; 
						instruccion.IdBoveda = instruccionProceso.IdBoveda; 
						instruccion.IdIntermediario = instruccionProceso.IdIntermediario;
						instruccion.FolioFactura = instruccionProceso.NumeroFactura;
						instruccion.IdMonedaLiquidacion = instruccionProceso.IdMonedaLiquidacion; 
						instruccion.IdCondicionDeLiquidacion = instruccionProceso.IdCondicionDeLiquidacion; 
						instruccion.Cantidad = instruccionProceso.Cantidad;
						instruccion.PrecioInstruccion = instruccionProceso.PrecioInstruccion;
						instruccion.MontoNeto = instruccionProceso.MontoNeto; 
						instruccion.TotalCobros = instruccionProceso.TotalCobros; 
						instruccion.MontoLiquidacion = instruccionProceso.MontoTotal; 
						instruccion.ValorRescate = instruccionProceso.ValorRescate;
						instruccion.ValorNominal = instruccionProceso.ValorNominal;
						if("IRF".equals(instruccion.IdTipoInstrumentoInstruccion) || "IFI".equals(instruccion.IdTipoInstrumentoInstruccion)) {
							instruccion.FechaVencimiento = Fecha.toDate(instruccionProceso.FechaVencimiento);
							instruccion.PlazoVencimiento = instruccionProceso.PlazoVencimiento;
							instruccion.TirTasa = instruccionProceso.Tasa;
						} else {
							instruccion.FechaVencimiento = Fecha.toDate("0001-01-01");
							instruccion.PlazoVencimiento = 0L;
							instruccion.TirTasa = BigDecimal.ZERO;
						}
						instruccion.IdTipoReajuste = instruccionProceso.IdTipoReajuste; 
						instruccion.Glosa = instruccionProceso.Glosa;
						instruccion.EsFungible = instruccionProceso.EsFungible;
						instruccion.CorteMinimo = instruccionProceso.CorteMinimo;
						instruccion.CodMotivoInstruccion = instruccionProceso.CodMotivoInstruccion;
						return instruccion;
					})
					.collect(Collectors.toList());
			
			CrearInstrucciones crearInstrucciones = new CrearInstrucciones();
			salidaCrearInstrucciones = crearInstrucciones.logica(con, entradaCrearInstrucciones);
			
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ProcesarCargaMasivaInstruccion.crearInstrucciones", fin - inicio);
		return salidaCrearInstrucciones;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			EntradaProcesarCargaMasivaInstruccion entradaProcesarCargaMasivaInstruccion = new EntradaProcesarCargaMasivaInstruccion();
			entradaProcesarCargaMasivaInstruccion.p_IdEmpresa = 1L;
			entradaProcesarCargaMasivaInstruccion.p_IdUsuario = 4L;
			entradaProcesarCargaMasivaInstruccion.p_IdModulo = "ORD";
			entradaProcesarCargaMasivaInstruccion.p_UsaReglasNegocio = "S";
			entradaProcesarCargaMasivaInstruccion.p_IdTipoCanalIngreso = "AGE";
			
			CargaInstruccion cargaInstruccion = new CargaInstruccion();
			cargaInstruccion.IdInterno = 1L;
			cargaInstruccion.TipoInstruccion = "IF";
			cargaInstruccion.Operacion = "CO";
			cargaInstruccion.Nemotecnico = "PAGARE R";
			cargaInstruccion.IdEmisor = "CENTRAL";
			cargaInstruccion.FechaVencimiento = "2021-12-01";					
			cargaInstruccion.FechaTrade = "2021-12-13";
			cargaInstruccion.CondicionLiquidacion = "PM";
			cargaInstruccion.Intermediario = 4L;
			cargaInstruccion.NumeroFactura = "789000";
			cargaInstruccion.Cantidad = new BigDecimal("210");	
			cargaInstruccion.Precio = new BigDecimal("20.36");
			cargaInstruccion.Tasa = new BigDecimal("1");
			cargaInstruccion.ValorRescate = new BigDecimal("800");
			cargaInstruccion.IdTipoReajuste = "CLP";
			cargaInstruccion.TotalCobros = new BigDecimal("1200");
			cargaInstruccion.MontoTotal = new BigDecimal("1679191.72");
			cargaInstruccion.DocumentoIdentidad = "89090100-K";
			cargaInstruccion.CodigoPortafolio = "1";
					
			
			entradaProcesarCargaMasivaInstruccion.p_Instrucciones.add(cargaInstruccion);
			
			ProcesarCargaMasivaInstruccion procesarCargaMasivaInstruccion = new ProcesarCargaMasivaInstruccion();
			try {
				SalidaProcesarCargaMasivaInstruccion salidaProcesarCargaMasivaInstruccion = procesarCargaMasivaInstruccion.logica(con, entradaProcesarCargaMasivaInstruccion);
				System.out.println(salidaProcesarCargaMasivaInstruccion.toString());
				con.commit();
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			} finally {
				con.rollback();
			}

    	}
		System.out.println("== END ==");
	}

}
