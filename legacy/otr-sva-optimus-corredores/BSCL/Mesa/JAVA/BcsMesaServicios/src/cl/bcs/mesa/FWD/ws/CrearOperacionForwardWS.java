package cl.bcs.mesa.FWD.ws;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.mesa.FWD.CrearOperacionForward;
import cl.bcs.mesa.FWD.CrearOperacionForward.CrearOperacionForwardEntrada;
import cl.bcs.mesa.FWD.CrearOperacionForward.CrearOperacionForwardSalida;
import cl.bcs.mesa.FWD.CrearOperacionForward.FormaPagoObjIn;
import cl.bcs.mesa.FWD.CrearOperacionForward.OperacionForwardObjIn;
import cl.bcs.mesa.FWD.CrearOperacionForward.OperacionForwardObjOut;
import cl.bcs.mesa.SPT.CrearOperacionSpot.Empleado;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.InObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.OutObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.OutObtenerDiaSiguienteMultiplesPaises.FechasOut;
import cl.bcs.plataforma.pla.pojo.FechasIn;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.portafolio.ws.ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio;
import cl.bcs.portafolio.ws.ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.EntradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio;
import cl.bcs.portafolio.ws.ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.EntradaDocumentoIdentidadCodigoPortafolio;
import cl.bcs.portafolio.ws.ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.Portafolio;
import cl.bcs.portafolio.ws.ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.SalidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.StringUtils;
import cl.bcs.utils.Utils;

public class CrearOperacionForwardWS implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearOperacionForwardWS.class);
	
	private final String SQL_SELECT_INSTRUMENTO = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CrearOperacionForwardWS_SelectInstrumento.sql");
	private final String SQL_SELECT_EJECUTIVO = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CrearOperacionForwardWS_SelectEjecutivo.sql");
	private final String SQL_SELECT_MODALIDAD_LIQUIDACION = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CrearOperacionForwardWS_SelectModalidadLiquidacion.sql");
	private final String SQL_SELECT_FIXING = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CrearOperacionForwardWS_SelectFixing.sql");
	private final String SQL_SELECT_MESA = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CrearOperacionForwardWS_SelectMesa.sql");
	private final String SQL_SELECT_CLASIFICACION_CARTERA = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CrearOperacionForwardWS_SelectClasificacionCartera.sql");
	private final String SQL_SELECT_FORMA_PAGO = loadResourceToString("cl/bcs/mesa/FWD/ws/sql/CrearOperacionForwardWS_SelectFormaPago.sql");	
	private final String SQL_SELECT_CONSULTAR_MONEDAS_FERIADO_MONEDA_PRINCIPAL = loadResourceToString("cl/bcs/mesa/SPT/sql/ConsultarMonedasFeriadoMonedaPrincipal.sql");
	private final String SQL_SELECT_EMPLEADO_DIGITADOR = loadResourceToString("cl/bcs/mesa/SPT/sql/WSingresarOperacionSpot_ConsultarEjecutivoDigitador.sql");
	
	public static class EntradaCrearOperacionForwardWS extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<EntradaOperacionForward> p_Operaciones;
		
		public EntradaCrearOperacionForwardWS() {
			this.p_Operaciones = new ArrayList<EntradaOperacionForward>();
		}
	}
	
	public static class EntradaOperacionForward extends BcsPojo {
		public Long IdInterno;
		public String CodigoMesa;
		public String DocumentoIdentidad;
		public String CodigoPortafolio;
		public String IdTipoOperacion;
		public Long Plazo;
		public BigDecimal Monto;
		public BigDecimal TipoCambioForward;
		public BigDecimal TipoCambioCierre;
		public BigDecimal PuntosForward;
		public BigDecimal TasasDolar;
		public BigDecimal TasasPesos;
		public String TipoCalculo;
		public BigDecimal TipoCambioCosto;
		public String ModalidadLiquidacion;
		public String FormaPagoEntregamos;
		public String FormaPagoRecibimos;
		public String Observaciones;
		public String FechaPago;
		public String Fixing;
		public String FechaFixing;
		public String HoraFixing;
		public String ClasificacionCartera;
		public String FechaSuscripcion = "";
		public String HoraSuscripcion = "";
		public String MonedaCompensacion = "";
		public Long IdEmpleadoDigitador = 0L;
		public String CodigoEjecutivoDigitador = "";
	}
	
	public static class SalidaCrearOperacionForwardWS extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<OperacionForwardObjOut>	P_OUT_TBLIDFORWARD;
		
		public SalidaCrearOperacionForwardWS() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
	}
	
	public static class Instrumento extends BcsPojo {
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String IdTipoProducto;
	}
	
	public static class Ejecutivo extends BcsPojo {
		public Long IdUsuario;
		public Long IdEmpleado;
		public Long IdPersona;
	}
	
	public static class ModalidadLiquidacion extends BcsPojo {
		public Long IdParametro;
		public String ValorParametro;
	}
	
	public static class Fixing extends BcsPojo {
		public Long IdParametro;
		public String ValorParametro;
	}
	
	public static class ClasificacionCartera extends BcsPojo {
		public Long IdParametro;
		public String ValorParametro;
	}
	
	public static class Mesa extends BcsPojo {
		public Long IdMesa;
		public String Codigo;
	}
	
	public static class FormaPago extends BcsPojo {
		public String IdFormaDePago;
		public String RecibePagaPortafolio;
	}
	
	public static class ConfiguracionMesa extends BcsPojo {
		public Map<String,ModalidadLiquidacion> ModalidadesLiquidacionPorCodigo;
		public Map<String,Fixing> FixingPorCodigo;
		public Map<String,Mesa> MesaPorCodigo;
		public Map<String,ClasificacionCartera> ClasificacionCarteraPorCodigo;
		public Map<String,FormaPago> FormaPagoPorId;
	}
	
	public static class ValidarFechaHoraSuscripcionParams extends BcsPojo {
		public Long IdEmpresa;
		public String FechaSuscripcion;
		public String HoraSuscripcion;
		public String FechaSistema;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		EntradaCrearOperacionForwardWS jin = gson.fromJson(in.toString(), EntradaCrearOperacionForwardWS.class);
		SalidaCrearOperacionForwardWS out = logica(conn,jin);

		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearOperacionForwardWS logica(Connection con, EntradaCrearOperacionForwardWS entrada) throws Exception {
		logger.info("INICIO::CrearOperacionForwardWS");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearOperacionForwardWS out = new SalidaCrearOperacionForwardWS();
		
		ConfiguracionMesa configuracionMesa = obtenerConfiguracionMesa(con);
		
		out = validar(con, entrada, configuracionMesa);
		if(out.P_OUT_ERRORESCONTADOR > 0) {
			return out;
		}
		
		SalidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio salidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio = obtenerPortafolios(con, entrada);
		if(salidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.P_OUT_ERRORESCONTADOR > 0) {
			out.P_OUT_ERRORESCONTADOR = salidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(salidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.P_OUT_MENSAJES);
			return out;
		}
		
		CrearOperacionForwardSalida crearOperacionForwardSalida = crearOperacionForward(con, entrada, salidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.P_OUT_PORTAFOLIOS, configuracionMesa);
		out.P_OUT_MENSAJES.addAll(crearOperacionForwardSalida.P_OUT_MENSAJES);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		out.P_OUT_TBLIDFORWARD = crearOperacionForwardSalida.P_OUT_TBLIDFORWARD;
		

		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearOperacionForwardWS", finServicio - inicioServicio);
		return out;
	}
	
	private ConfiguracionMesa obtenerConfiguracionMesa(Connection con) throws Exception {
		logger.debug("INICIO::CrearOperacionForwardWS.obtenerConfiguracionMesa");
		long inicio = System.currentTimeMillis();
		ConfiguracionMesa configuracionMesa = new ConfiguracionMesa();
		configuracionMesa.ModalidadesLiquidacionPorCodigo = obtenerModalidadLiquidacion(con);
		configuracionMesa.FixingPorCodigo = obtenerFixing(con);
		configuracionMesa.MesaPorCodigo = obtenerMesa(con);
		configuracionMesa.ClasificacionCarteraPorCodigo = obtenerClasificacionCartera(con);
		configuracionMesa.FormaPagoPorId = obtenerFormaPago(con);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionForwardWS.obtenerConfiguracionMesa", fin - inicio);
		return configuracionMesa;
	}
	
	private SalidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio obtenerPortafolios(Connection con, EntradaCrearOperacionForwardWS entrada) throws Exception{
		logger.debug("INICIO::CrearOperacionForwardWS.obtenerPortafolios");
		long inicio = System.currentTimeMillis();
		
		EntradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio = new EntradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio();
		entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.p_IdModulo = "MFW";
		entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.p_UsaReglasNegocio = "S";
		
		entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio.p_DocumentosIdentidadCodigosPortafolio = entrada.p_Operaciones
				.stream()
				.map(operacionForward->{
					EntradaDocumentoIdentidadCodigoPortafolio entradaDocumentoIdentidadCodigoPortafolio = new EntradaDocumentoIdentidadCodigoPortafolio();
					entradaDocumentoIdentidadCodigoPortafolio.DocumentoIdentidad = operacionForward.DocumentoIdentidad;
					entradaDocumentoIdentidadCodigoPortafolio.CodigoPortafolio = operacionForward.CodigoPortafolio;
					return entradaDocumentoIdentidadCodigoPortafolio;
				})
				.collect(Collectors.toList());
		
		ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio consultarPortafolioPorDocumentoIdentidadCodigoPortafolio = new ConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio();
		SalidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio salidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio = consultarPortafolioPorDocumentoIdentidadCodigoPortafolio.logica(con, entradaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionForwardWS.obtenerPortafolios", fin - inicio);
		return salidaConsultarPortafolioPorDocumentoIdentidadCodigoPortafolio;
	}
	
	private Map<String,ModalidadLiquidacion> obtenerModalidadLiquidacion(Connection con) throws Exception {
		logger.debug("INICIO::CrearOperacionForwardWS.obtenerModalidadLiquidacion");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<ModalidadLiquidacion> modalidadesLiquidacion = sql.query(con, SQL_SELECT_MODALIDAD_LIQUIDACION, new HashMap<String, Object>(), ModalidadLiquidacion.class);
		Map<String,ModalidadLiquidacion> modalidadesLiquidacionPorCodigo = modalidadesLiquidacion.stream().collect(Collectors.toMap(modalidad->modalidad.ValorParametro, modalidad->modalidad));
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionForwardWS.obtenerModalidadLiquidacion", fin - inicio);
		return modalidadesLiquidacionPorCodigo;
	}
	
	private Map<String,Fixing> obtenerFixing(Connection con)  throws Exception {
		logger.debug("INICIO::CrearOperacionForwardWS.obtenerFixing");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<Fixing> fixing = sql.query(con, SQL_SELECT_FIXING, new HashMap<String, Object>(), Fixing.class);
		Map<String,Fixing> fixingPorCodigo = fixing.stream().collect(Collectors.toMap(fixingEncontrado->fixingEncontrado.ValorParametro, fixingEncontrado->fixingEncontrado));
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionForwardWS.obtenerFixing", fin - inicio);
		return fixingPorCodigo;
	}
	
	private Map<String, Mesa> obtenerMesa(Connection con) throws Exception {
		logger.debug("INICIO::CrearOperacionForwardWS.obtenerMesa");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<Mesa> mesas = sql.query(con, SQL_SELECT_MESA, new HashMap<String, Object>(), Mesa.class);
		Map<String,Mesa> mesasPorCodigo = mesas.stream().collect(Collectors.toMap(mesa->mesa.Codigo, mesa->mesa));
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionForwardWS.obtenerMesa", fin - inicio);
		return mesasPorCodigo;
	}
	
	private Map<String, ClasificacionCartera> obtenerClasificacionCartera(Connection con) throws Exception {
		logger.debug("INICIO::CrearOperacionForwardWS.obtenerClasificacionCartera");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<ClasificacionCartera> clasificacionesCartera = sql.query(con, SQL_SELECT_CLASIFICACION_CARTERA, new HashMap<String, Object>(), ClasificacionCartera.class);
		Map<String,ClasificacionCartera> clasificacionCarteraPorCodigo = clasificacionesCartera.stream().collect(Collectors.toMap(clasificacionCartera->clasificacionCartera.ValorParametro, clasificacionCartera->clasificacionCartera));
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionForwardWS.obtenerClasificacionCartera", fin - inicio);
		return clasificacionCarteraPorCodigo;
	}
	
	private Map<String, FormaPago> obtenerFormaPago(Connection con) throws Exception {
		logger.debug("INICIO::CrearOperacionForwardWS.obtenerFormaPago");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<FormaPago> formasPago = sql.query(con, SQL_SELECT_FORMA_PAGO, new HashMap<String, Object>(), FormaPago.class);
		Map<String,FormaPago> formaPagoPorId = formasPago.stream().collect(Collectors.toMap(formaPago->formaPago.IdFormaDePago, formaPago->formaPago));
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionForwardWS.obtenerFormaPago", fin - inicio);
		return formaPagoPorId;
	}
	
	private SalidaCrearOperacionForwardWS validar(Connection con, EntradaCrearOperacionForwardWS entrada, ConfiguracionMesa configuracionMesa) throws Exception {
		logger.trace("INICIO::CrearOperacionForwardWS.validar");
		long inicio = System.currentTimeMillis();
		SalidaCrearOperacionForwardWS out = new SalidaCrearOperacionForwardWS();
		
		List<MensajeSalida> validacionEntrada = validarEntrada(con, entrada);
		out.P_OUT_MENSAJES.addAll(validacionEntrada);
		
		List<MensajeSalida> validacionModalidadLiquidacion = validarModalidadLiquidacion(con, entrada, configuracionMesa.ModalidadesLiquidacionPorCodigo);
		out.P_OUT_MENSAJES.addAll(validacionModalidadLiquidacion);
		
		List<MensajeSalida> validacionFixing = validarFixing(con, entrada, configuracionMesa.FixingPorCodigo);
		out.P_OUT_MENSAJES.addAll(validacionFixing);
		
		List<MensajeSalida> validacionMesa = validarMesa(con, entrada, configuracionMesa.MesaPorCodigo);
		out.P_OUT_MENSAJES.addAll(validacionMesa);
		
		List<MensajeSalida> validacionClasificacionCartera = validarClasificacionCartera(con, entrada, configuracionMesa.ClasificacionCarteraPorCodigo);
		out.P_OUT_MENSAJES.addAll(validacionClasificacionCartera);
		
		List<MensajeSalida> validacionFormaPago = validarFormaPago(con, entrada, configuracionMesa.FormaPagoPorId);
		out.P_OUT_MENSAJES.addAll(validacionFormaPago);
			
		List<MensajeSalida> validacionFechaHoraSuscripcion = validarFechaHoraSuscripcion(con, entrada);
		out.P_OUT_MENSAJES.addAll(validacionFechaHoraSuscripcion);
		
		/**
		 * SVARESGIN-6910 Se agrega validacion a tasas de FW y TC Costo
		 */
		List<MensajeSalida> validacionTCBalanceado = validarTCBalanceado(con, entrada);
        out.P_OUT_MENSAJES.addAll(validacionTCBalanceado);
        
        List<MensajeSalida> validarEmpleadoDigitador = validarEmpleadoDigitador(con, entrada);
		out.P_OUT_MENSAJES.addAll(validarEmpleadoDigitador);
			
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.validar", fin - inicio);
		return out;
	}
	
	private List<MensajeSalida> validarEntrada(Connection con, EntradaCrearOperacionForwardWS entrada) throws Exception {
		logger.trace("INICIO::CrearOperacionForwardWS.validarEntrada");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		if(entrada.p_Operaciones.isEmpty()) {
			MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CREAROPERACIONFORWARDWS_NO_SE_AGREGARON_OPERACIONES", new HashMap<String,String>(1), con, entrada.p_IdEmpresa);
			mensajes.add(mensajeSalida);
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.validarEntrada", fin - inicio);
		return mensajes;
	}
	
	private List<MensajeSalida> validarModalidadLiquidacion(Connection con, EntradaCrearOperacionForwardWS entrada, Map<String,ModalidadLiquidacion> modalidadesLiquidacionPorCodigo) throws Exception{
		logger.trace("INICIO::CrearOperacionForwardWS.validarModalidadLiquidacion");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		for(EntradaOperacionForward operacion : entrada.p_Operaciones) {
			ModalidadLiquidacion modalidadLiquidacion = modalidadesLiquidacionPorCodigo.get(operacion.ModalidadLiquidacion);
			if(modalidadLiquidacion == null) {
				HashMap<String, String> parametros = new  HashMap<String,String>(2);
				parametros.put("ModalidadLiquidacion", operacion.ModalidadLiquidacion);
				MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CREAROPERACIONFORWARDWS_NO_EXISTE_MODALIDAD_LIQUIDACION", parametros, con, entrada.p_IdEmpresa);
				mensajes.add(mensajeSalida);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.validarModalidadLiquidacion", fin - inicio);
		return mensajes;
	}
	
	private List<MensajeSalida> validarFixing(Connection con, EntradaCrearOperacionForwardWS entrada, Map<String,Fixing> fixingPorCodigo) throws Exception{
		logger.trace("INICIO::CrearOperacionForwardWS.validarFixing");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		for(EntradaOperacionForward operacion : entrada.p_Operaciones) {
			Fixing fixing = fixingPorCodigo.get(operacion.Fixing);
			if(fixing == null) {
				HashMap<String, String> parametros = new  HashMap<String,String>(2);
				parametros.put("Fixing", operacion.Fixing);
				MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CREAROPERACIONFORWARDWS_NO_EXISTE_FIXING", parametros, con, entrada.p_IdEmpresa);
				mensajes.add(mensajeSalida);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.validarFixing", fin - inicio);
		return mensajes;
	}
	
	private List<MensajeSalida> validarMesa(Connection con, EntradaCrearOperacionForwardWS entrada, Map<String,Mesa> mesasPorCodigo) throws Exception{
		logger.trace("INICIO::CrearOperacionForwardWS.validarMesa");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		for(EntradaOperacionForward operacion : entrada.p_Operaciones) {
			Mesa mesa = mesasPorCodigo.get(operacion.CodigoMesa);
			if(mesa == null) {
				HashMap<String, String> parametros = new  HashMap<String,String>(2);
				parametros.put("Mesa", operacion.CodigoMesa);
				MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CREAROPERACIONFORWARDWS_NO_EXISTE_MESA", parametros, con, entrada.p_IdEmpresa);
				mensajes.add(mensajeSalida);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.validarMesa", fin - inicio);
		return mensajes;
	}
	
	private List<MensajeSalida> validarClasificacionCartera(Connection con, EntradaCrearOperacionForwardWS entrada, Map<String,ClasificacionCartera> clasificacionesCarteraPorCodigo) throws Exception{
		logger.trace("INICIO::CrearOperacionForwardWS.validarClasificacionCartera");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		for(EntradaOperacionForward operacion : entrada.p_Operaciones) {
			ClasificacionCartera clasificacionCartera = clasificacionesCarteraPorCodigo.get(operacion.ClasificacionCartera);
			if(clasificacionCartera == null) {
				HashMap<String, String> parametros = new  HashMap<String,String>(2);
				parametros.put("ClasificacionCartera", operacion.ClasificacionCartera);
				MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CREAROPERACIONFORWARDWS_NO_EXISTE_CLASIFICACION_CARTERA", parametros, con, entrada.p_IdEmpresa);
				mensajes.add(mensajeSalida);
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.validarClasificacionCartera", fin - inicio);
		return mensajes;
	}
	
	private List<MensajeSalida> validarFormaPago(Connection con, EntradaCrearOperacionForwardWS entrada, Map<String,FormaPago> formasPagoPorId) throws Exception{
		logger.trace("INICIO::CrearOperacionForwardWS.validarFormaPago");
		long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		for(EntradaOperacionForward operacion : entrada.p_Operaciones) {
			if("VENFWD".equals(operacion.IdTipoOperacion) || "ENTREGA_FISICA".equals(operacion.ModalidadLiquidacion)) {
				FormaPago formaPagoEntregamos = formasPagoPorId.get(operacion.FormaPagoEntregamos);
				if(formaPagoEntregamos == null) {
					HashMap<String, String> parametros = new  HashMap<String,String>(2);
					parametros.put("FormaPagoEntregamos", operacion.FormaPagoEntregamos);
					MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CREAROPERACIONFORWARDWS_NO_EXISTE_FORMA_PAGO_ENTREGAMOS", parametros, con, entrada.p_IdEmpresa);
					mensajes.add(mensajeSalida);
				}
			}
			
			if("COMFWD".equals(operacion.IdTipoOperacion) || "ENTREGA_FISICA".equals(operacion.ModalidadLiquidacion)) {
				FormaPago formaPagoRecibimos = formasPagoPorId.get(operacion.FormaPagoRecibimos);
				if(formaPagoRecibimos == null) {
					HashMap<String, String> parametros = new  HashMap<String,String>(2);
					parametros.put("FormaPagoRecibimos", operacion.FormaPagoRecibimos);
					MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CREAROPERACIONFORWARDWS_NO_EXISTE_FORMA_PAGO_RECIBIMOS", parametros, con, entrada.p_IdEmpresa);
					mensajes.add(mensajeSalida);
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.validarFormaPago", fin - inicio);
		return mensajes;
	}
	
	private List<MensajeSalida> validarEmpleadoDigitador(Connection con, EntradaCrearOperacionForwardWS entrada) throws Exception {
		logger.trace("INICIO::CrearOperacionForwardWS.validarEmpleadoDigitador");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		SocioNegocioUtils.Empleado empleadoUsuario = SocioNegocioUtils.obtenerEmpleadoUsuario(con, entrada.p_IdEmpresa, entrada.p_IdUsuario).orElse(null);
		
		List<String> codigosEmpleado = new ArrayList<String>();
		for(EntradaOperacionForward operacion : entrada.p_Operaciones) {
			if(!StringUtils.isNullOrBlank(operacion.CodigoEjecutivoDigitador)) {
				codigosEmpleado.add(operacion.CodigoEjecutivoDigitador);
			}
		}
		
		Map<String, Empleado> empleadosPorCodigo = new HashMap<String, Empleado>();
		
		if(!codigosEmpleado.isEmpty()) {
			HashMap<String, Object> parametros = new HashMap<String, Object>();
			parametros.put("CodigoEjecutivoDigitador", codigosEmpleado);
			List<Empleado> empleadosDigitador = sql.query(con, SQL_SELECT_EMPLEADO_DIGITADOR, parametros, Empleado.class);
			
			for(Empleado empleado : empleadosDigitador) {
				empleadosPorCodigo.put(empleado.CodigoEmpleado, empleado);
			}
		}
		
		for(EntradaOperacionForward operacion : entrada.p_Operaciones) {
			if(!StringUtils.isNullOrBlank(operacion.CodigoEjecutivoDigitador)) {
				Empleado empleado = empleadosPorCodigo.get(operacion.CodigoEjecutivoDigitador);
				if(empleado == null) {
					HashMap<String, String> parametrosHash = new HashMap<String, String>(2);
					parametrosHash.put("CodigoEjecutivoDigitador", operacion.CodigoEjecutivoDigitador);
					MensajeSalida mensajeSalida = new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "CODIGO_EJECUTIVO_DIGITADOR_NO_EXISTE", parametrosHash, con, entrada.p_IdEmpresa);
					mensajes.add(mensajeSalida);
					operacion.IdEmpleadoDigitador = 0L;
				} else {
					operacion.IdEmpleadoDigitador = empleado.IdEmpleadoDigitador;
				}
			} else {
				operacion.IdEmpleadoDigitador = empleadoUsuario != null ? empleadoUsuario.IdEmpleado : 0L;
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.validarEmpleadoDigitador", fin - inicio);
		return mensajes;
	}
	
	private Boolean aplicaFeriadoUsa(Connection con, Instrumento instrumento )  {
		SQL sql = new SQL();	
		HashMap<String,Object> parametros = new HashMap<String,Object>();
		parametros.put("IdInstrumento",instrumento.IdInstrumento);
		parametros.put("IdMonedaPrincipal", "USD");
		parametros.put("IdMonedaSecundaria", "CLP");
		try {
			Long aplicaFeriadoMonedaPrincipal = sql.countQuery(con, SQL_SELECT_CONSULTAR_MONEDAS_FERIADO_MONEDA_PRINCIPAL, parametros);
			return aplicaFeriadoMonedaPrincipal==0?false:true;
		} catch(SQLException e) {
			return false;
		}
	}
	
	private static boolean isForwardRateBalanced(BigDecimal tcCosto, BigDecimal puntos, BigDecimal tcForward) {
        return tcForward.compareTo(tcCosto.add(puntos)) == 0;
    }

	/**
	 * SVARESGIN-6920 Se agrega validacion TCForward = PUNTOS + TCCosto
	 * @param con
	 * @param entrada
	 * @return
	 * @throws Exception
	 */
    private List<MensajeSalida> validarTCBalanceado(Connection con, EntradaCrearOperacionForwardWS entrada) throws Exception {
        logger.trace("INICIO::CrearOperacionForwardWS.validarTCBalanceado");
        long inicio = System.currentTimeMillis();
        List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
        for (EntradaOperacionForward operacion : entrada.p_Operaciones) {
            if (!"PUNTOS".equals(operacion.TipoCalculo)) continue;
            
            boolean camposNulos = operacion.TipoCambioCosto == null
                    || operacion.PuntosForward == null
                    || operacion.TipoCambioForward == null;
            boolean noBalanceado = !camposNulos
                    && !isForwardRateBalanced(operacion.TipoCambioCosto, operacion.PuntosForward, operacion.TipoCambioForward);
            
            if (!camposNulos && !noBalanceado) continue;
            
            String clave = camposNulos
                    ? "CREAROPERACIONFORWARDWS_TC_PUNTOS_CAMPOS_NULOS"
                    : "CREAROPERACIONFORWARDWS_TC_FORWARD_NO_BALANCEADO";
            mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, clave, new HashMap<String, String>(), con, entrada.p_IdEmpresa));
        }
        long fin = System.currentTimeMillis();
        logger.trace("FIN::CrearOperacionForwardWS.validarTCBalanceado", fin - inicio);
        return mensajes;
    }
	
	private CrearOperacionForwardSalida crearOperacionForward(Connection con, EntradaCrearOperacionForwardWS entrada, List<Portafolio> portafolios, ConfiguracionMesa configuracionMesa) throws Exception {
		logger.debug("INICIO::CrearOperacionForwardWS.crearOperacionForward");
		long inicio = System.currentTimeMillis();
		
		Long idPais = obtenerIdPais(con, entrada.p_IdEmpresa);
		String fechaOperacion = obtenerFechaSistema(con, entrada.p_IdEmpresa);
		Instrumento instrumento = obtenerInstrumento(con, "SECANO");
		Ejecutivo ejecutivo = obtenerEjecutivo(con, entrada.p_IdUsuario);
		Map<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> fechasVencimiento = obtenerFechasVencimiento(con, entrada.p_Operaciones, idPais, fechaOperacion);
		Map<String, Portafolio> portafoliosPorDocumentoIdentidadCodigoPortafolio = portafolios.stream().collect(Collectors.toMap(portafolio->portafolio.DocumentoIdentidad + "_" + portafolio.CodigoPortafolio, portafolio->portafolio));
		List<MensajeSalida> mensajesSalida =new ArrayList<>();		
		CrearOperacionForwardEntrada crearOperacionForwardEntrada = new CrearOperacionForwardEntrada();
		crearOperacionForwardEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		crearOperacionForwardEntrada.p_IdPais = idPais;
		crearOperacionForwardEntrada.p_IdUsuario = entrada.p_IdUsuario;
		crearOperacionForwardEntrada.p_IdBolsa = obtenerIdBolsa(con, entrada.p_IdEmpresa);
		crearOperacionForwardEntrada.p_IdSucursal = 0L;
		crearOperacionForwardEntrada.p_IdModulo = entrada.p_IdModulo;
		crearOperacionForwardEntrada.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		crearOperacionForwardEntrada.p_DatosFormaPago = new ArrayList<>();
		crearOperacionForwardEntrada.p_tablaCalce = new ArrayList<>();
		crearOperacionForwardEntrada.p_tablaForward = entrada.p_Operaciones
				.stream()
				.map(operacion->{
					String fechaCalculada="";
					Long plazoNuevo=0L;
					//System.out.println(aplicaFeriadoUsa(con, instrumento) && operacion.ModalidadLiquidacion.equals("ENTREGA_FISICA") && entrada.p_IdModulo.equals("MFW"));
					Portafolio portafolio = portafoliosPorDocumentoIdentidadCodigoPortafolio.get(operacion.DocumentoIdentidad + "_" + operacion.CodigoPortafolio);
					if( aplicaFeriadoUsa(con, instrumento) && operacion.ModalidadLiquidacion.equals("ENTREGA_FISICA") && entrada.p_IdModulo.equals("MFW")) { 								
						FechasOut salidaFecha;
						try {
							salidaFecha = obtenerDiaSiguienteMultiplesPaises(con,operacion,fechaOperacion,idPais,entrada);
							plazoNuevo = salidaFecha.numeroDias;
							fechaCalculada = salidaFecha.fechaCalculada;
							String mensajeInformativo = Diccionario.getMensaje(con,entrada.p_IdEmpresa,"PLAZO_VENCIMIENTO_FWD_AJUSTADO")
									.add("fechaCalculada", salidaFecha.fechaCalculada)
									.msg();
							mensajesSalida.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "PLAZO_VENCIMIENTO_FWD_AJUSTADO", mensajeInformativo));
		
							
						} catch (Exception e) {
							logger.debug(e);
						}
						
					}

					ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut fechaVencimiento = fechasVencimiento.get(fechaOperacion + "_" + operacion.Plazo);
					OperacionForwardObjIn forward = new OperacionForwardObjIn();
					forward.FolioInterno = operacion.IdInterno;
					forward.IdTipoOperacion = operacion.IdTipoOperacion;
					forward.IdTipoInstrumento = instrumento.IdTipoInstrumento;
					forward.IdTipoProducto = instrumento.IdTipoProducto;
					forward.IdInstrumento = instrumento.IdInstrumento;
					forward.Nemotecnico = "SECANO";
					forward.IdCliente = portafolio.IdCliente; 
					forward.IdPortafolio = portafolio.IdPortafolio;
					forward.IdPersonaEjecutivo = ejecutivo.IdPersona;
					forward.IdTipoCanalIngreso = "WS";
					forward.IdMonedaPrincipal = "USD";	
					forward.IdMonedaSecundaria = "CLP";
					forward.MontoOperacion = operacion.Monto;
					forward.MontoInicial = BigDecimal.ZERO;
					forward.MontoFinal = BigDecimal.ZERO;
					forward.FechaOperacion = Fecha.toDate(fechaOperacion);
					forward.TCCierre = operacion.TipoCambioCierre;
					forward.ParidadCierre = BigDecimal.ZERO;
					forward.Plazo = (plazoNuevo!=0L)?plazoNuevo:(fechaVencimiento.numeroDias==operacion.Plazo)?operacion.Plazo:fechaVencimiento.numeroDias;
					
					forward.FechaVencimiento = fechaCalculada.equals("")?Fecha.toDate(fechaVencimiento.fechaCalculada): Fecha.toDate(fechaCalculada);
					
					//FECHA DE PAGO

					if("COMPENSADA".equals(operacion.ModalidadLiquidacion) && (operacion.FechaPago != null) ) {
						forward.FechaPago = Fecha.toDate(operacion.FechaPago.isEmpty()?fechaVencimiento.fechaCalculada:operacion.FechaPago);
					} else if("ENTREGA_FISICA".equals(operacion.ModalidadLiquidacion) && aplicaFeriadoUsa(con, instrumento) && !fechaCalculada.isEmpty()) {
						forward.FechaPago = Fecha.toDate(fechaCalculada);
					} else {
						forward.FechaPago = Fecha.toDate(fechaVencimiento.fechaCalculada);
					}
					
					forward.IdModalidadLiquidacion = configuracionMesa.ModalidadesLiquidacionPorCodigo.get(operacion.ModalidadLiquidacion).IdParametro;
					forward.IdReferenciaMercado = 0L;
					forward.IdPlaza = 0L;
					forward.TCCosto = operacion.TipoCambioCosto;
					forward.PuntosTasasForward = "PUNTOS".equals(operacion.TipoCalculo) ? "S" : "N";
					forward.PuntosForward = BigDecimal.ZERO;
					forward.TCForward = BigDecimal.ZERO;
					forward.TasasMonedaPrincipal = BigDecimal.ZERO;
					forward.TasasMonedaSecundaria = BigDecimal.ZERO;
					forward.MontoMargen = BigDecimal.ZERO;
					
					if("S".equals(forward.PuntosTasasForward)) {
						forward.PuntosForward = operacion.PuntosForward;
						forward.TCForward = operacion.TipoCambioForward;
						forward.MontoMargen = calcularMargen(operacion.Monto, operacion.TipoCambioCierre, operacion.TipoCambioForward, operacion.IdTipoOperacion);
					} else {
						forward.TasasMonedaPrincipal = operacion.TasasDolar;
						forward.TasasMonedaSecundaria = operacion.TasasPesos;
					}
					
					forward.MontoDiferido = calcularMontoDiferido(operacion.Monto, operacion.TipoCambioCierre, operacion.TipoCambioCosto, operacion.IdTipoOperacion);
					forward.IdFixing = configuracionMesa.FixingPorCodigo.get(operacion.Fixing).IdParametro;
					forward.FechaFixing = operacion.FechaFixing == null ? Fecha.toDate("0001-01-01") : Fecha.toDate(operacion.FechaFixing);
					forward.HoraFixing = operacion.HoraFixing == null ? "00:00:00" : operacion.HoraFixing;
					forward.IdMesa = configuracionMesa.MesaPorCodigo.get(operacion.CodigoMesa).IdMesa;
					forward.IdSpotAsociado = 0L;
					forward.IdClasificacionCartera = configuracionMesa.ClasificacionCarteraPorCodigo.get(operacion.ClasificacionCartera).IdParametro;
					forward.Observaciones = operacion.Observaciones;
					forward.IdEstado = "VIG";
					forward.IdContacto = 0L;
					forward.TipoDocumento = "";
					forward.FechaSuscripcion = operacion.FechaSuscripcion;
					forward.HoraSuscripcion = operacion.HoraSuscripcion;
					forward.IdMonedaCompensacion = operacion.MonedaCompensacion;
					forward.IdEmpleadoDigitador = operacion.IdEmpleadoDigitador;
					return forward;
				})
				.collect(Collectors.toList());
			
		for(EntradaOperacionForward operacion : entrada.p_Operaciones) {
			if("VENFWD".equals(operacion.IdTipoOperacion) || "ENTREGA_FISICA".equals(operacion.ModalidadLiquidacion)) {
				FormaPagoObjIn formaPagoEntregamos = new FormaPagoObjIn();
				formaPagoEntregamos.IdInternoPagoOperacion = 1L;
				formaPagoEntregamos.IdComprobantePagoOperacion = 0L;
				formaPagoEntregamos.IdTipoMovimiento = "COMFWD".equals(operacion.IdTipoOperacion) ? "MOVING" : "MOVEGR";
				formaPagoEntregamos.IdFormaDePago = operacion.FormaPagoEntregamos;
				formaPagoEntregamos.IdMoneda = "COMFWD".equals(operacion.IdTipoOperacion) ? "CLP" : "USD";
				formaPagoEntregamos.IdMonedaLiquidacion = "COMFWD".equals(operacion.IdTipoOperacion) ? "CLP" : "USD";
				formaPagoEntregamos.IdOperacion = operacion.IdInterno;
				formaPagoEntregamos.RecibePagaPortafolio = "P";
				formaPagoEntregamos.MontoFormaDePago = "COMFWD".equals(operacion.IdTipoOperacion) ? operacion.Monto.multiply(operacion.TipoCambioCierre) : operacion.Monto;
				formaPagoEntregamos.AfectarTesoreria = operacion.FormaPagoEntregamos.indexOf("_CIN_") > 0 ? "N" : "S";
				crearOperacionForwardEntrada.p_DatosFormaPago.add(formaPagoEntregamos);
			}
			if("COMFWD".equals(operacion.IdTipoOperacion) || "ENTREGA_FISICA".equals(operacion.ModalidadLiquidacion)) {
				FormaPagoObjIn formaPagoRecibimos = new FormaPagoObjIn();
				formaPagoRecibimos.IdInternoPagoOperacion = 2L;
				formaPagoRecibimos.IdComprobantePagoOperacion = 0L;
				formaPagoRecibimos.IdTipoMovimiento = "VENFWD".equals(operacion.IdTipoOperacion) ? "MOVING" : "MOVEGR";
				formaPagoRecibimos.IdFormaDePago = operacion.FormaPagoRecibimos;
				formaPagoRecibimos.IdMoneda = "VENFWD".equals(operacion.IdTipoOperacion) ? "CLP" : "USD";
				formaPagoRecibimos.IdMonedaLiquidacion = "VENFWD".equals(operacion.IdTipoOperacion) ? "CLP" : "USD";
				formaPagoRecibimos.IdOperacion = operacion.IdInterno;
				formaPagoRecibimos.RecibePagaPortafolio = "R";
				formaPagoRecibimos.MontoFormaDePago = "VENFWD".equals(operacion.IdTipoOperacion) ? operacion.Monto.multiply(operacion.TipoCambioCierre) : operacion.Monto;
				formaPagoRecibimos.AfectarTesoreria = operacion.FormaPagoRecibimos.indexOf("_CIN_") > 0 ? "N" : "S";
				crearOperacionForwardEntrada.p_DatosFormaPago.add(formaPagoRecibimos);
			}
		}
		
		CrearOperacionForward crearOperacionForward = new CrearOperacionForward();
		CrearOperacionForwardSalida crearOperacionForwardSalida = crearOperacionForward.logica(con, crearOperacionForwardEntrada);
		
		for(MensajeSalida mensajeSalida:mensajesSalida){
			crearOperacionForwardSalida.P_OUT_MENSAJES.add(mensajeSalida);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionForwardWS.crearOperacionForward", fin - inicio);
		return crearOperacionForwardSalida;
		
	}
	
	private String obtenerIdBolsa(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CrearOperacionForwardWS.obtenerIdBolsa");
		long inicio = System.currentTimeMillis();
		String idBolsa = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.BolsaDefectoSistema");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.obtenerIdBolsa", fin - inicio);
		return idBolsa;
	}
	
	private Long obtenerIdPais(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CrearOperacionForwardWS.obtenerIdPais");
		long inicio = System.currentTimeMillis();
		Long idPais = ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.obtenerIdPais", fin - inicio);
		return idPais;
	}
	
	private String obtenerFechaSistema(Connection con, Long idEmpresa) throws Exception {
		logger.trace("INICIO::CrearOperacionForwardWS.obtenerIdPais");
		long inicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.obtenerIdPais", fin - inicio);
		return fechaSistema;
	}
	
	private Instrumento obtenerInstrumento(Connection con, String nemotecnico) throws Exception {
		logger.debug("INICIO::CrearOperacionForwardWS.obtenerInstrumento");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("Nemotecnico", nemotecnico);
		
		SQL sql = new SQL();
		List<Instrumento> instrumentos = sql.query(con, SQL_SELECT_INSTRUMENTO, parametros, Instrumento.class);
		Instrumento instrumento = instrumentos.stream().findFirst().orElse(new Instrumento());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionForwardWS.obtenerInstrumento", fin - inicio);
		return instrumento;
	}
	
	private Ejecutivo obtenerEjecutivo(Connection con, Long idUsuario) throws Exception {
		logger.debug("INICIO::CrearOperacionForwardWS.obtenerInstrumento");
		long inicio = System.currentTimeMillis();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>();
		parametros.put("IdUsuario", idUsuario);
		
		SQL sql = new SQL();
		List<Ejecutivo> ejecutivos = sql.query(con, SQL_SELECT_EJECUTIVO, parametros, Ejecutivo.class);
		Ejecutivo ejecutivo = ejecutivos.stream().findFirst().orElse(new Ejecutivo());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionForwardWS.obtenerInstrumento", fin - inicio);
		return ejecutivo;
		
	}
	
	private Map<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> obtenerFechasVencimiento(Connection con, List<EntradaOperacionForward> operaciones, Long idPais, String fechaOperacion) throws Exception {
		logger.debug("INICIO::CrearOperacionForwardWS.obtenerFechaVencimiento");
		long inicio = System.currentTimeMillis();
		ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();
		ObtenerDiaSiguiente.InObtenerDiaSiguiente inObtenerDiaSiguiente = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
		inObtenerDiaSiguiente = new ObtenerDiaSiguiente.InObtenerDiaSiguiente();
		inObtenerDiaSiguiente.p_diaHabil = "N";
		inObtenerDiaSiguiente.p_soloCaeDiaHabil = "S";
		inObtenerDiaSiguiente.p_fechas = operaciones.stream()
				.map(operacion->{
					ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn fechaConsulta = new ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn();
					fechaConsulta.fechaInicial = fechaOperacion;
					fechaConsulta.numeroDias = operacion.Plazo;
					fechaConsulta.pais = idPais;
					return fechaConsulta;
				})
				.collect(Collectors.toList());
		ObtenerDiaSiguiente.OutObtenerDiaSiguiente outDiaSiguiente = obtenerDiaSiguiente.logica(con, inObtenerDiaSiguiente);
		Map<String,ObtenerDiaSiguiente.OutObtenerDiaSiguiente.FechasOut> fechasCalculadas = outDiaSiguiente.p_out_fechas
				.stream()
				.collect(Collectors.toMap(fecha->fecha.fechaInicial + "_" + fecha.numeroDiasInicial, fecha->fecha));
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionForwardWS.obtenerFechaVencimiento", fin - inicio);
		return fechasCalculadas;
	}
	
	private cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.OutObtenerDiaSiguienteMultiplesPaises.FechasOut obtenerDiaSiguienteMultiplesPaises(Connection con, EntradaOperacionForward ope, String fechaOperacion, Long idPais, EntradaCrearOperacionForwardWS entrada) throws Exception {
		cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.OutObtenerDiaSiguienteMultiplesPaises.FechasOut salidaFecha = new FechasOut();
		cl.bcs.plataforma.pla.pojo.FechasIn fechaIn = new FechasIn();
		fechaIn.idInterno = 0L;
		//LocalDate fecha = (Fecha.toDate(fechaOperacion)).toLocalDate();
		fechaIn.fechaInicial = fechaOperacion;// fecha.minus(1,ChronoUnit.DAYS).toString();
		fechaIn.numeroDias = ope.Plazo;
		ArrayList<Long> listaPais= new 	ArrayList<>();
		listaPais.add(idPais);
		listaPais.add(223L);
		fechaIn.paises = listaPais;
		ObtenerDiaSiguienteMultiplesPaises obtenerDiaSiguienteMulti = new ObtenerDiaSiguienteMultiplesPaises();
		InObtenerDiaSiguienteMultiplesPaises obtenerFechaConFeriado = new InObtenerDiaSiguienteMultiplesPaises();
		obtenerFechaConFeriado.p_diaHabil="N";
		obtenerFechaConFeriado.p_soloCaeDiaHabil ="S";
		obtenerFechaConFeriado.p_fechas.add(fechaIn);//.add(fechaIn);
		OutObtenerDiaSiguienteMultiplesPaises outObtenerFechaHabil = obtenerDiaSiguienteMulti.logica(con, obtenerFechaConFeriado);			
			
		salidaFecha = outObtenerFechaHabil.p_out_fechas.get(0);		
		return salidaFecha;
		
	}
	
	private BigDecimal calcularMontoDiferido(BigDecimal monto, BigDecimal tipoCambioCierre, BigDecimal tipoCambioCosto, String idTipoOperacion) {
		logger.debug("INICIO::CrearOperacionForwardWS.calcularMontoDiferido");
		long inicio = System.currentTimeMillis();
		BigDecimal diferenciaTipoCambios = tipoCambioCierre.subtract(tipoCambioCosto, MathContext.DECIMAL128);
		BigDecimal montoDiferido = monto.multiply(diferenciaTipoCambios, MathContext.DECIMAL128);
		if("VENFWD".equals(idTipoOperacion)) {
			montoDiferido = montoDiferido.negate(MathContext.DECIMAL128);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionForwardWS.calcularMontoDiferido", fin - inicio);
		return montoDiferido;
	}
	
	private BigDecimal calcularMargen(BigDecimal monto, BigDecimal tipoCambioCierre, BigDecimal tipoCambioForward, String idTipoOperacion) {
		logger.debug("INICIO::CrearOperacionForwardWS.calcularMargen");
		long inicio = System.currentTimeMillis();
		BigDecimal diferenciaTipoCambios = tipoCambioForward.subtract(tipoCambioCierre, MathContext.DECIMAL128);
		BigDecimal margen = monto.multiply(diferenciaTipoCambios, MathContext.DECIMAL128);
		if("VENFWD".equals(idTipoOperacion)) {
			margen = margen.negate(MathContext.DECIMAL128);
		}
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearOperacionForwardWS.calcularMargen", fin - inicio);
		return margen;
	}
	
	private List<MensajeSalida> validarFechaHoraSuscripcion(Connection con, EntradaCrearOperacionForwardWS entrada) throws Exception {
		logger.trace("INICIO::CrearOperacionForwardWS.validarFechaHoraSuscripcion");
		Long inicio = System.currentTimeMillis();
		List<MensajeSalida> mensajes = new ArrayList<MensajeSalida>();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		ValidarFechaHoraSuscripcionParams validarFechaHoraSuscripcionParams = new ValidarFechaHoraSuscripcionParams();
		validarFechaHoraSuscripcionParams.FechaSistema = fechaSistema;
		validarFechaHoraSuscripcionParams.IdEmpresa = entrada.p_IdEmpresa;
		
		for(EntradaOperacionForward operacion : entrada.p_Operaciones) {
			
			validarFechaHoraSuscripcionParams.FechaSuscripcion = operacion.FechaSuscripcion;
			validarFechaHoraSuscripcionParams.HoraSuscripcion = operacion.HoraSuscripcion;
			
			validarFechaSuscripcion(con, mensajes, validarFechaHoraSuscripcionParams);
			
			validarHoraSuscripcion(con, mensajes, validarFechaHoraSuscripcionParams);
		
		}
		
		Long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.validarFechaHoraSuscripcion", fin - inicio);
		
		return mensajes;
	}
	
	private void validarFechaSuscripcion(Connection con, List<MensajeSalida> mensajes, ValidarFechaHoraSuscripcionParams params) throws Exception{
		
		logger.trace("INICIO::CrearOperacionForwardWS.validarFechaSuscripcion");
		Long inicio = System.currentTimeMillis();
		
		String descripcionError = "";
		
		if(!StringUtils.isNullOrBlank(params.FechaSuscripcion)) {
			
			if(!esFormatoFechaValido(params.FechaSuscripcion)) {
				descripcionError = Diccionario.getMensaje(con, params.IdEmpresa, "UI_CREAROPERACIONFORWARD_FECHA_SUSCRIPCION_NO_VALIDA").msg();
				mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_CREAROPERACIONFORWARD_FECHA_SUSCRIPCION_NO_VALIDA", descripcionError));
			}
			
			if(!params.FechaSuscripcion.equals(params.FechaSistema)) {
				descripcionError = Diccionario.getMensaje(con, params.IdEmpresa, "FECHA_OPERACION_DISTINTA_FECHA_PROCESO").msg();
				mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FECHA_OPERACION_DISTINTA_FECHA_PROCESO", descripcionError));
			}
			
			if(StringUtils.isNullOrBlank(params.HoraSuscripcion)) {
				descripcionError = Diccionario.getMensaje(con, params.IdEmpresa, "UI_CREAROPERACIONFORWARD_HORA_SUSCRIPCION_NO_VALIDA").msg();
				mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_CREAROPERACIONFORWARD_HORA_SUSCRIPCION_NO_VALIDA", descripcionError));
			}
		}
		
		Long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.validarFechaSuscripcion", fin - inicio);
	}
	
	private Boolean esFormatoFechaValido(String fecha) { 
		
		logger.trace("INICIO::CrearOperacionForwardWS.esFormatoFechaValido");
		Long inicio = System.currentTimeMillis();
		
		Boolean esFormatoValido = true;
		DateTimeFormatter fechaFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
		
		try {
			LocalDate.parse(fecha, fechaFormatter);
		}catch(DateTimeParseException e) {
			esFormatoValido = false;
		}
		
		Long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.esFormatoFechaValido", fin - inicio);
		
		return esFormatoValido;
		
	}
	
	private void validarHoraSuscripcion(Connection con, List<MensajeSalida> mensajes, ValidarFechaHoraSuscripcionParams params) throws Exception{
		
		logger.trace("INICIO::CrearOperacionForwardWS.validarHoraSuscripcion");
		Long inicio = System.currentTimeMillis();
		
		String descripcionError = "";
		
		if(!StringUtils.isNullOrBlank(params.HoraSuscripcion)) {
			
			if(StringUtils.isNullOrBlank(params.FechaSuscripcion)) {
				descripcionError = Diccionario.getMensaje(con, params.IdEmpresa, "UI_CREAROPERACIONFORWARD_FECHA_SUSCRIPCION_NO_VALIDA").msg();
				mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_CREAROPERACIONFORWARD_FECHA_SUSCRIPCION_NO_VALIDA", descripcionError));
			}
			if(!esFormatoHoraValido(params.HoraSuscripcion)) {
				descripcionError = Diccionario.getMensaje(con, params.IdEmpresa, "UI_CREAROPERACIONFORWARD_HORA_SUSCRIPCION_NO_VALIDA").msg();
				mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_CREAROPERACIONFORWARD_HORA_SUSCRIPCION_NO_VALIDA", descripcionError));
			}
		}
		
		Long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.validarHoraSuscripcion", fin - inicio);
	}
	
	private Boolean esFormatoHoraValido(String hora) {
		
		logger.trace("INICIO::CrearOperacionForwardWS.esFormatoHoraValido");
		Long inicio = System.currentTimeMillis();
		
		Boolean esFormatoValido = hora.matches("^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$"); 
		
		Long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearOperacionForwardWS.esFormatoHoraValido", fin - inicio);
		
		return esFormatoValido;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties")) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
					
		String json = "{\r\n"
				+ "   \"p_IdEmpresa\":1,\r\n"
				+ "   \"p_IdUsuario\":97,\r\n"
				+ "   \"p_IdModulo\":\"MFW\",\r\n"
				+ "   \"p_UsaReglasNegocio\":\"S\",\r\n"
				+ "   \"p_Operaciones\":[\r\n"
				+ "      {\r\n"
				+ "         \"IdInterno\":0,\r\n"
				+ "         \"CodigoMesa\":\"MESA_FWD\",\r\n"
				+ "         \"DocumentoIdentidad\":\"15936077-6\",\r\n"
				+ "         \"CodigoPortafolio\":\"0\",\r\n"
				+ "         \"IdTipoOperacion\":\"VENFWD\",\r\n"
				+ "         \"Plazo\":2,\r\n"
				+ "         \"Monto\":100000,\r\n"
				+ "         \"TipoCambioForward\":908.15,\r\n"
				+ "         \"TipoCambioCierre\":908.15,\r\n"
				+ "         \"PuntosForward\":0,\r\n"
				+ "         \"TasasDolar\":0,\r\n"
				+ "         \"TasasPesos\":0,\r\n"
				+ "         \"TipoCalculo\":\"PUNTOS\",\r\n"
				+ "         \"TipoCambioCosto\":908.15,\r\n"
				+ "         \"ModalidadLiquidacion\":\"COMPENSADA\",\r\n"
				+ "         \"FormaPagoEntregamos\":\"VENFWD_CIN_USD_0\",\r\n"
				+ "         \"FormaPagoRecibimos\":\"VENFWD_CIN_CLP_0\",\r\n"
				+ "         \"Observaciones\":\"Web service Cristobal\",\r\n"
				+ "         \"FechaPago\":\"0001-01-01\",\r\n"
				+ "         \"Fixing\":\"BANCOCENTRAL\",\r\n"
				+ "         \"FechaFixing\":\"0001-01-01\",\r\n"
				+ "         \"HoraFixing\":null,\r\n"
				+ "         \"ClasificacionCartera\":\"EMPRESA\",\r\n"
				+ "         \"CodigoEjecutivoDigitador\":\"CDL\",\r\n"
				+ "         \"MonedaCompensacion\":\"CLP\"\r\n"
				+ "      }\r\n"
				+ "   ]\r\n"
				+ "}";

		EntradaCrearOperacionForwardWS Objin = gson.fromJson(json, EntradaCrearOperacionForwardWS.class);
		CrearOperacionForwardWS demo = new CrearOperacionForwardWS();				
		try {
			SalidaCrearOperacionForwardWS ObjOut = demo.logica(con, Objin);
			if(ObjOut.P_OUT_ERRORESCONTADOR.equals(0L)) {
				con.rollback();
			}else {
				con.rollback();
			}
			//con.commit();
			con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
			//con.commit();
    	}
		System.out.println("== END ==");
	}

}
