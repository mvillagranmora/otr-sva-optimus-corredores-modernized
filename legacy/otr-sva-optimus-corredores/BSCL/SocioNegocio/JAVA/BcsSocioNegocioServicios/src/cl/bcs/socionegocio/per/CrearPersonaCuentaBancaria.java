package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ConfirmarCrearPersonaCuentaBancariaMasivo.EntradaConfirmarCrearPersonaCuentaBancariaMasivo;
import cl.bcs.socionegocio.per.ConfirmarCrearPersonaCuentaBancariaMasivo.SalidaConfirmarCrearPersonaCuentaBancariaMasivo;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearPersonaCuentaBancaria implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(CrearPersonaCuentaBancaria.class);
	
	private String SQL_INSERT_CUENTA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaCuentaBancaria_InsertCuentaBancaria.sql");
	private String SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaCuentaBancaria_UpdatePreferenciaPagoPortafolio.sql");
	private String SQL_INSERT_PREFERENCIA_PAGO_PORTAFOLIO = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaCuentaBancaria_InsertPreferenciaPagoPortafolio.sql");
	private String SQL_SELECT_CUENTA_BANCARIA_EXISTENTE = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaCuentaBancaria_Select_CuentaBancariaExistente.sql");
	private String SQL_INSERT_LOG_GENERAR_CONTRATOS = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaCuentaBancaria_Insert_LogGenerarContratos.sql");
	private String SQL_SELECT_DATOS_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaCuentaBancaria_Select_DatosPersona.sql");
	
	private final String ID_CONCEPTO = "NUECTB";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	
	public static class InCuentaBancaria extends BcsPojo {
		public Long IdInterno;
		public Long IdBanco;
		public String IdTipoCuentaBancaria;
		public String IdMoneda;
		public String NumeroCuentaBancaria;
		public Long IdPersona;
		public String IdEstado;
		public Long IdPlaza;
		public String CrearPreferenciaPago;
		public Long IdCliente;
		public String EsPac;
		public String Predeterminado;
		//PERU
		public String IdTipoIdentificadorCuenta;
		public String IdentificadorCuenta;
		public Long IdPersonaRelacionada;
		public String CodigoCuentaInterbancario;
		public String CodigoAba;
		public String OtroCodigoBancoExtranjero;
		public String CodigoIban;
		public String OtroCodigoCuentaExtranjero;
		
		public InCuentaBancaria() {
			this.CrearPreferenciaPago = "N";
			this.EsPac = "N";
			this.Predeterminado = "N";
		}
	}
	
	public static class EntradaCrearPersonaCuentaBancaria extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<InCuentaBancaria> p_InCuentaBancaria;

		public EntradaCrearPersonaCuentaBancaria() {
			this.p_InCuentaBancaria = new ArrayList<InCuentaBancaria>();
		}
	}
	
	public static class OutCuentaBancaria extends BcsPojo {
		public Long IdInterno;
		public Long IdCuentaBancaria;
		public String IdMoneda;
		public String NumeroCuentaBancaria;
		public Long IdPersona;
		public String IdEstado;
		public String IdEstadoAutorizacion;
		public Long IdUsuarioCreador;
		public String CrearPreferenciaPago;
		public Long IdCliente;
	}
	
	public static class SalidaCrearPersonaCuentaBancaria extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Long> P_OUT_REFID;
		public List<OutCuentaBancaria> P_OUT_CUENTABANCARIA;
		
		public SalidaCrearPersonaCuentaBancaria(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_REFID = new ArrayList<Long>();
			this.P_OUT_CUENTABANCARIA = new ArrayList<OutCuentaBancaria>();
		}
	}
	
	public static class DatosCuentaBancaria extends BcsPojo {
		public Long IdInterno;
		public Long IdCuentaBancaria;
		public Long IdBanco;
		public String IdTipoCuentaBancaria;
		public String IdMoneda;
		public String NumeroCuentaBancaria;
		public Long IdPersona;
		public java.sql.Timestamp FechaIngreso;
		public String IdEstado;
		public String IdEstadoAutorizacion;
		public Long IdUsuarioCreador;
		public Long IdPlaza;
		public String IdTipoIdentificadorCuenta;
		public String IdentificadorCuenta;
		public Long IdPersonaRelacionada;
		public String CodigoCuentaInterbancario;
		public String CodigoAba;
		public String OtroCodigoBancoExtranjero;
		public String CodigoIban;
		public String OtroCodigoCuentaExtranjero;
		public String EsPac;
		public String Predeterminado;
		public String CrearPreferenciaPago;
		public Long IdCliente;
	}
	
	public static class DatosPreferenciaPago extends BcsPojo {
		public Long IdCliente;
		public String IdMoneda;
		public Long IdEmpresa;
		public Long IdCuentaBancaria;
		public Long IdUsuario;
		public java.sql.Timestamp FechaIngreso;
		public String IdEstadoAutorizacion;
	}

	public static class DatosPersona extends BcsPojo {
		public Long IdPersona;
		public String DocumentoIdentidad;
		public String NombreCompleto;
	}

	public static class CuentaBancariaExistente extends BcsPojo {
		public Long IdBanco;
		public String NumeroCuentaBancaria;
		public Long IdPersona;
	}

	public static class InsertLogGenerarContratos extends BcsPojo {
		public Long Idlog;
		public String CodigoFormulario;
		public String CampoBackend;
		public Long IdEmpresa;
		public Long IdCliente;
		public Long IdPersona;
		public Long IdUsuario;
		public String FechaSistema;
		public Long IdPrograma;
		public String Estado;
		public String Mensaje;
	}

	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearPersonaCuentaBancaria jin = gson.fromJson(in.toString(), EntradaCrearPersonaCuentaBancaria.class);
		SalidaCrearPersonaCuentaBancaria out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearPersonaCuentaBancaria logica(Connection con, EntradaCrearPersonaCuentaBancaria entrada) throws Exception {
		logger.info("INICIO::CrearPersonaCuentaBancaria");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearPersonaCuentaBancaria out = new SalidaCrearPersonaCuentaBancaria();
		SQL sql = new SQL();

		try {
			if (entrada.p_InCuentaBancaria == null || entrada.p_InCuentaBancaria.isEmpty()) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_ESPECIFICAR_CUENTA_BANCARIA", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
				out.P_OUT_ERRORESCONTADOR = Long.valueOf(out.P_OUT_MENSAJES.size());
				return out;
			}

			List<InCuentaBancaria> cuentasBancariasValidas = validarCuentasBancariasDuplicadas(con, sql, entrada.p_IdEmpresa, entrada.p_InCuentaBancaria, out);

			List<OutCuentaBancaria> cuentasBancariasCreadas = insertarCuentaBancaria(con, sql, entrada.p_IdEmpresa, entrada.p_IdUsuario, cuentasBancariasValidas);
			asignarPreferenciaPagoPortafolioPorDefecto(con, sql, entrada.p_IdEmpresa, cuentasBancariasCreadas);

			String requiereAutorizacionConcepto = Utils.getRequiereEventoAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO);
			Boolean requiereAutorizacion = "S".equals(requiereAutorizacionConcepto) && !"N".equals(entrada.p_UsaReglasNegocio);

			SalidaCrearPersonaCuentaBancaria salidaConfirmacion;
			if(requiereAutorizacion) {
				salidaConfirmacion = crearEventoAutorizacion(con, sql, entrada, cuentasBancariasCreadas);
			} else {
				salidaConfirmacion = confirmarCuentaBancaria(con, sql, entrada, cuentasBancariasCreadas);
			}
			out.P_OUT_ERRORESCONTADOR += salidaConfirmacion.P_OUT_ERRORESCONTADOR;
			out.P_OUT_MENSAJES.addAll(salidaConfirmacion.P_OUT_MENSAJES);

			agregarLog(con, entrada, cuentasBancariasCreadas, requiereAutorizacion);
			out.P_OUT_CUENTABANCARIA = cuentasBancariasCreadas;
			out.P_OUT_REFID = obtenerIdsCuentasBancariasCreadas(cuentasBancariasCreadas);

		} catch (Exception ex) {
			logger.error("Error en CrearPersonaCuentaBancaria: " + ex.getMessage());
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_CREAR_PERSONA_CUENTA_BANCARIA", ex.getMessage()));
			out.P_OUT_ERRORESCONTADOR = Long.valueOf(out.P_OUT_MENSAJES.size());
		}

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearPersonaCuentaBancaria", finServicio - inicioServicio);
		return out;
	}

	private List<InCuentaBancaria> validarCuentasBancariasDuplicadas(Connection con, SQL sql, Long idEmpresa, List<InCuentaBancaria> cuentasBancarias, SalidaCrearPersonaCuentaBancaria out) throws Exception {
		logger.debug("INICIO::CrearPersonaCuentaBancaria.validarCuentasBancariasDuplicadas");
		long inicio = System.currentTimeMillis();

		for (InCuentaBancaria cuentaBancaria : cuentasBancarias) {
			cuentaBancaria.NumeroCuentaBancaria = (cuentaBancaria.NumeroCuentaBancaria == null || cuentaBancaria.NumeroCuentaBancaria.isEmpty())
				? "0" : cuentaBancaria.NumeroCuentaBancaria;
		}

		List<Long> idsPersona = cuentasBancarias.stream().map(cuentaBancaria -> cuentaBancaria.IdPersona).distinct().collect(Collectors.toList());
		HashMap<String, Object> params = new HashMap<>();
		params.put("IdPersona", idsPersona);
		List<CuentaBancariaExistente> cuentasBancariasExistentes = idsPersona.isEmpty()
			? new ArrayList<>()
			: sql.query(con, SQL_SELECT_CUENTA_BANCARIA_EXISTENTE, params, CuentaBancariaExistente.class);

		List<InCuentaBancaria> cuentasBancariasValidas = new ArrayList<>();
		for (InCuentaBancaria cuentaBancaria : cuentasBancarias) {
			boolean existe = cuentasBancariasExistentes.stream().anyMatch(existente ->
				existente.IdBanco.equals(cuentaBancaria.IdBanco)
				&& existente.NumeroCuentaBancaria.equals(cuentaBancaria.NumeroCuentaBancaria)
				&& existente.IdPersona.equals(cuentaBancaria.IdPersona));

			if (existe) {
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_INSERTAR_CUENTA_BANCARIA_REPETIDA", new HashMap<String, String>(), con, idEmpresa));
			} else {
				cuentasBancariasValidas.add(cuentaBancaria);
			}
		}
		out.P_OUT_ERRORESCONTADOR = Long.valueOf(out.P_OUT_MENSAJES.size());

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaCuentaBancaria.validarCuentasBancariasDuplicadas", fin - inicio);
		return cuentasBancariasValidas;
	}

	private List<OutCuentaBancaria> insertarCuentaBancaria(Connection con, SQL sql, Long idEmpresa, Long idUsuario, List<InCuentaBancaria> cuentasBancarias) throws Exception {
		logger.debug("INICIO::CrearPersonaCuentaBancaria.insertarCuentaBancaria");
		long inicio = System.currentTimeMillis();

		if (cuentasBancarias.isEmpty()) {
			return new ArrayList<>();
		}

		List<Long> secuenciaCuentaBancaria = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::BAN.IdCuentaBancaria", Long.valueOf(cuentasBancarias.size()));
		String fechaHoraSistemaSecondate = FechaDB.ObtenerFechaHoraSistemaUTC(con,idEmpresa);
		AtomicInteger index = new AtomicInteger(0);
		
		List<DatosCuentaBancaria> datosCuentasBancarias = cuentasBancarias.parallelStream().map(cuentaBancaria->{
			DatosCuentaBancaria datosCuentaBancaria = new DatosCuentaBancaria();
			datosCuentaBancaria.IdInterno = cuentaBancaria.IdInterno;
			datosCuentaBancaria.IdCuentaBancaria = secuenciaCuentaBancaria.get(index.getAndIncrement());
			datosCuentaBancaria.IdBanco = cuentaBancaria.IdBanco;
			datosCuentaBancaria.IdTipoCuentaBancaria = cuentaBancaria.IdTipoCuentaBancaria;
			datosCuentaBancaria.IdMoneda = cuentaBancaria.IdMoneda;
			datosCuentaBancaria.NumeroCuentaBancaria = cuentaBancaria.NumeroCuentaBancaria;
			datosCuentaBancaria.IdPersona = cuentaBancaria.IdPersona;
			datosCuentaBancaria.FechaIngreso = Fecha.toTimeStamp(fechaHoraSistemaSecondate);
			datosCuentaBancaria.IdEstado = cuentaBancaria.IdEstado;
			datosCuentaBancaria.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
			datosCuentaBancaria.IdUsuarioCreador = idUsuario;
			datosCuentaBancaria.IdPlaza = cuentaBancaria.IdPlaza;
			datosCuentaBancaria.IdTipoIdentificadorCuenta = cuentaBancaria.IdTipoIdentificadorCuenta;
			datosCuentaBancaria.IdentificadorCuenta = cuentaBancaria.IdentificadorCuenta;
			datosCuentaBancaria.IdPersonaRelacionada = cuentaBancaria.IdPersonaRelacionada;
			datosCuentaBancaria.CodigoCuentaInterbancario = cuentaBancaria.CodigoCuentaInterbancario;
			datosCuentaBancaria.CodigoAba = cuentaBancaria.CodigoAba;
			datosCuentaBancaria.OtroCodigoBancoExtranjero = cuentaBancaria.OtroCodigoBancoExtranjero;
			datosCuentaBancaria.CodigoIban = cuentaBancaria.CodigoIban;
			datosCuentaBancaria.OtroCodigoCuentaExtranjero = cuentaBancaria.OtroCodigoCuentaExtranjero;
			datosCuentaBancaria.EsPac = cuentaBancaria.EsPac;
			datosCuentaBancaria.Predeterminado = cuentaBancaria.Predeterminado;
			return datosCuentaBancaria;
		}).collect(Collectors.toList());

		sql.batch(con, SQL_INSERT_CUENTA_BANCARIA, datosCuentasBancarias, DatosCuentaBancaria.class);
		
		List<OutCuentaBancaria> cuentasBancariasCreadas = datosCuentasBancarias.parallelStream().map(cuentaBancaria->{
			OutCuentaBancaria cuentaBancariaCreada = new OutCuentaBancaria();
			cuentaBancariaCreada.IdInterno = cuentaBancaria.IdInterno;
			cuentaBancariaCreada.IdCuentaBancaria = cuentaBancaria.IdCuentaBancaria;
			cuentaBancariaCreada.IdMoneda = cuentaBancaria.IdMoneda;
			cuentaBancariaCreada.NumeroCuentaBancaria = cuentaBancaria.NumeroCuentaBancaria;
			cuentaBancariaCreada.IdPersona = cuentaBancaria.IdPersona;
			cuentaBancariaCreada.IdEstado = cuentaBancaria.IdEstado;
			cuentaBancariaCreada.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
			cuentaBancariaCreada.IdUsuarioCreador = idUsuario;
			cuentaBancariaCreada.CrearPreferenciaPago = cuentaBancaria.CrearPreferenciaPago;
			cuentaBancariaCreada.IdCliente = cuentaBancaria.IdCliente;
			return cuentaBancariaCreada;
		}).collect(Collectors.toList());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaCuentaBancaria.insertarCuentaBancaria", fin - inicio);
		return cuentasBancariasCreadas;
	}
	
	private void asignarPreferenciaPagoPortafolioPorDefecto(Connection con, SQL sql, Long idEmpresa, List<OutCuentaBancaria> cuentasBancariasCreadas) throws Exception {
		logger.debug("INICIO::CrearPersonaCuentaBancaria.insertarPreferenciaPagoPortafolio");
		long inicio = System.currentTimeMillis();
		List<OutCuentaBancaria> cuentasBancariasConPreferenciaPago = cuentasBancariasCreadas.parallelStream().filter(cuentaBancaria->esCreacionPreferenciaPago(cuentaBancaria.CrearPreferenciaPago, cuentaBancaria.IdCliente)).collect(Collectors.toList());
		
		String fechaHoraSistemaSecondate = FechaDB.ObtenerFechaHoraSistemaUTC(con,idEmpresa);
		
		List<DatosPreferenciaPago> preferenciasDePago = cuentasBancariasConPreferenciaPago.parallelStream().map(cuentaBancaria->{
			DatosPreferenciaPago datosPreferenciaPago = new DatosPreferenciaPago();
			datosPreferenciaPago.IdCliente = cuentaBancaria.IdCliente;
			datosPreferenciaPago.IdMoneda = cuentaBancaria.IdMoneda;
			datosPreferenciaPago.IdEmpresa = idEmpresa;
			datosPreferenciaPago.IdCuentaBancaria = cuentaBancaria.IdCuentaBancaria;
			datosPreferenciaPago.IdUsuario = cuentaBancaria.IdUsuarioCreador;
			datosPreferenciaPago.FechaIngreso = Fecha.toTimeStamp(fechaHoraSistemaSecondate);
			datosPreferenciaPago.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
			return datosPreferenciaPago;
		}).collect(Collectors.toList());

		sql.batch(con, SQL_UPDATE_PREFERENCIA_PAGO_PORTAFOLIO, preferenciasDePago, DatosPreferenciaPago.class);
		sql.batch(con, SQL_INSERT_PREFERENCIA_PAGO_PORTAFOLIO, preferenciasDePago, DatosPreferenciaPago.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaCuentaBancaria.insertarPreferenciaPagoPortafolio", fin - inicio);
	}
	
	private Boolean esCreacionPreferenciaPago(String crearPreferenciaPago, Long idCliente) {
		logger.trace("INICIO::CrearPersonaCuentaBancaria.esCreacionPreferenciaPago");
		long inicio = System.currentTimeMillis();
		Boolean esCreacionPreferenciaPago = "S".equals(crearPreferenciaPago) && idCliente != null && idCliente.longValue() != 0L;
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPersonaCuentaBancaria.esCreacionPreferenciaPago", fin - inicio);
		return esCreacionPreferenciaPago;
	}
	
	private SalidaCrearPersonaCuentaBancaria crearEventoAutorizacion(Connection con, SQL sql, EntradaCrearPersonaCuentaBancaria entrada, List<OutCuentaBancaria> cuentasBancariasCreadas) throws Exception {
		logger.debug("INICIO::CrearPersonaCuentaBancaria.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaCrearPersonaCuentaBancaria salidaCrearPersonaCuentaBancaria = new SalidaCrearPersonaCuentaBancaria();
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

		List<Long> idsPersona = cuentasBancariasCreadas.stream().map(cuentaBancaria -> cuentaBancaria.IdPersona).distinct().collect(Collectors.toList());
		HashMap<String, Object> paramsPersona = new HashMap<>();
		paramsPersona.put("IdPersona", idsPersona);
		List<DatosPersona> datosPersonas = idsPersona.isEmpty() ? new ArrayList<>() : sql.query(con, SQL_SELECT_DATOS_PERSONA, paramsPersona, DatosPersona.class);
		Map<Long, DatosPersona> datosPersonaPorId = datosPersonas.stream().collect(Collectors.toMap(datosPersona -> datosPersona.IdPersona, datosPersona -> datosPersona));

		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";

		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = cuentasBancariasCreadas.parallelStream().map(cuentaBancaria->{
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento = ID_CONCEPTO;
			evento.IdModuloSolicita = entrada.p_IdModulo;
			evento.IdUsuarioSolicita = entrada.p_IdUsuario;
			evento.Fecha = fechaSistema;
			evento.IdConcepto = ID_CONCEPTO;
			evento.NumeroOperacionConcepto = String.valueOf(cuentaBancaria.IdCuentaBancaria);
			evento.FechaOperacionConcepto = fechaSistema;
			evento.IdCliente = cuentaBancaria.IdPersona;
			DatosPersona datosPersona = datosPersonaPorId.get(cuentaBancaria.IdPersona);
			String documentoIdentidad = datosPersona != null && datosPersona.DocumentoIdentidad != null ? datosPersona.DocumentoIdentidad : "";
			String nombreCompleto = datosPersona != null && datosPersona.NombreCompleto != null ? datosPersona.NombreCompleto : "";
			evento.Glosa = documentoIdentidad + " / " + nombreCompleto + " / NroCta: " + cuentaBancaria.NumeroCuentaBancaria;
			return evento;
		}).collect(Collectors.toList());
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaCrearPersonaCuentaBancaria.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaCrearPersonaCuentaBancaria.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaCrearPersonaCuentaBancaria.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaCrearPersonaCuentaBancaria.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaCrearPersonaCuentaBancaria.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaCuentaBancaria.crearEventoAutorizacion", fin - inicio);
		return salidaCrearPersonaCuentaBancaria;	
	}
	
	private void insertarLogGenerarContratos(Connection con, SQL sql, EntradaCrearPersonaCuentaBancaria entrada, List<OutCuentaBancaria> cuentasBancariasCreadas) throws Exception {
		logger.debug("INICIO::CrearPersonaCuentaBancaria.insertarLogGenerarContratos");
		long inicio = System.currentTimeMillis();

		if (cuentasBancariasCreadas.isEmpty()) {
			return;
		}

		String fechaHoraSistemaSecondate = FechaDB.ObtenerFechaHoraSistemaUTC(con, entrada.p_IdEmpresa);
		String fechaSistema = fechaHoraSistemaSecondate.substring(0, 10);
		List<Long> secuenciaLog = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos", Long.valueOf(cuentasBancariasCreadas.size()));
		AtomicInteger index = new AtomicInteger(0);

		List<InsertLogGenerarContratos> logsGenerarContratos = cuentasBancariasCreadas.stream().map(cuentaBancaria -> {
			InsertLogGenerarContratos log = new InsertLogGenerarContratos();
			log.Idlog = secuenciaLog.get(index.getAndIncrement());
			log.CodigoFormulario = "CUENTA_BANCARIA_";
			log.CampoBackend = "Creado";
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.IdCliente = 0L;
			log.IdPersona = cuentaBancaria.IdPersona;
			log.IdUsuario = entrada.p_IdUsuario;
			log.FechaSistema = fechaSistema;
			log.IdPrograma = 0L;
			log.Estado = "PEN";
			log.Mensaje = "Persona - CrearPersonaCuentaBancaria.sql";
			return log;
		}).collect(Collectors.toList());

		sql.batch(con, SQL_INSERT_LOG_GENERAR_CONTRATOS, logsGenerarContratos, InsertLogGenerarContratos.class);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaCuentaBancaria.insertarLogGenerarContratos", fin - inicio);
	}

	private SalidaCrearPersonaCuentaBancaria confirmarCuentaBancaria(Connection con, SQL sql, EntradaCrearPersonaCuentaBancaria entrada, List<OutCuentaBancaria> cuentasBancariasCreadas) throws Exception {
		logger.debug("INICIO::CrearPersonaCuentaBancaria.confirmarCuentaBancaria");
		long inicio = System.currentTimeMillis();

		insertarLogGenerarContratos(con, sql, entrada, cuentasBancariasCreadas);

		EntradaConfirmarCrearPersonaCuentaBancariaMasivo entradaConfirmarCrearPersonaCuentaBancariaMasivo = new EntradaConfirmarCrearPersonaCuentaBancariaMasivo();
		entradaConfirmarCrearPersonaCuentaBancariaMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfirmarCrearPersonaCuentaBancariaMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfirmarCrearPersonaCuentaBancariaMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaConfirmarCrearPersonaCuentaBancariaMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConfirmarCrearPersonaCuentaBancariaMasivo.p_CuentaBancaria = cuentasBancariasCreadas.parallelStream().map(cuentaBancaria->{
			ConfirmarCrearPersonaCuentaBancariaMasivo.CuentaBancaria cuentaBancariaActualizar = new ConfirmarCrearPersonaCuentaBancariaMasivo.CuentaBancaria();
			cuentaBancariaActualizar.IdCuentaBancaria = cuentaBancaria.IdCuentaBancaria;
			return cuentaBancariaActualizar;
		}).collect(Collectors.toList());
		entradaConfirmarCrearPersonaCuentaBancariaMasivo.p_IdEstadoAutorizacion = "ING";
		
		ConfirmarCrearPersonaCuentaBancariaMasivo confirmarCrearPersonaCuentaBancariaMasivo = new ConfirmarCrearPersonaCuentaBancariaMasivo();
		SalidaConfirmarCrearPersonaCuentaBancariaMasivo salidaConfirmarCrearPersonaCuentaBancariaMasivo = confirmarCrearPersonaCuentaBancariaMasivo.logica(con, entradaConfirmarCrearPersonaCuentaBancariaMasivo);
		
		SalidaCrearPersonaCuentaBancaria out = new SalidaCrearPersonaCuentaBancaria();
		out.P_OUT_ERRORESCONTADOR = salidaConfirmarCrearPersonaCuentaBancariaMasivo.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaConfirmarCrearPersonaCuentaBancariaMasivo.P_OUT_MENSAJES;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaCuentaBancaria.confirmarCuentaBancaria", fin - inicio);
		return out;
	}
	
	private void agregarLog(Connection con, EntradaCrearPersonaCuentaBancaria entrada, List<OutCuentaBancaria> cuentasBancariasCreadas, Boolean requiereAutorizacion) throws Exception {
		logger.debug("INICIO::CrearPersonaCuentaBancaria.agregarLog");
		long inicio = System.currentTimeMillis();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		entradaAgregarLog.p_Log = cuentasBancariasCreadas.parallelStream().map(cuentaBancaria->{
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = cuentaBancaria.IdCuentaBancaria;
			log.IdTipoLog = "INGRESO";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdPersona = cuentaBancaria.IdPersona;
			log.IdRegistro = cuentaBancaria.IdCuentaBancaria.toString();
			log.IdTipoRegistro = "CUENTABANCARIA-CREAR";
			log.EstadoRegistro = cuentaBancaria.IdEstado;
			log.IdProceso = 0L;
			log.EstadoProceso = requiereAutorizacion ? ID_ESTADO_AUTORIZACION : "ING";
			log.NombreProceso = "CrearCuentaBancaria";
			return log;
		}).collect(Collectors.toList());
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaCuentaBancaria.agregarLog", fin - inicio);
	}
	
	private List<Long> obtenerIdsCuentasBancariasCreadas(List<OutCuentaBancaria> cuentasBancariasCreadas){
		logger.trace("INICIO::CrearPersonaCuentaBancaria.obtenerIdsCuentasBancariasCreadas");
		long inicio = System.currentTimeMillis();
		List<Long> idsCuentaBancarias = cuentasBancariasCreadas.parallelStream().map(cuentaBancaria->cuentaBancaria.IdCuentaBancaria).collect(Collectors.toList());
		long fin = System.currentTimeMillis();
		logger.trace("FIN::CrearPersonaCuentaBancaria.obtenerIdsCuentasBancariasCreadas", fin - inicio);
		return idsCuentaBancarias;
	}
	
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":33,\"p_IdModulo\":\"SOC\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_InCuentaBancaria\":[{\"IdInterno\":200007,\"IdBanco\":1,\"IdTipoCuentaBancaria\":\"CUCO\",\"IdMoneda\":\"CLP\",\"NumeroCuentaBancaria\":\"0000002\",\"IdPersona\":41162,\"IdEstado\":\"ACT\",\"IdPlaza\":0,\"CrearPreferenciaPago\":\"N\",\"IdCliente\":130826,\"EsPac\":\"N\",\"Predeterminado\":\"S\",\"IdTipoIdentificadorCuenta\":\"\",\"IdentificadorCuenta\":\"0\",\"IdPersonaRelacionada\":0,\"CodigoCuentaInterbancario\":\"\",\"CodigoAba\":\"\",\"OtroCodigoBancoExtranjero\":\"\",\"CodigoIban\":\"\",\"OtroCodigoCuentaExtranjero\":\"\"}]}";
			EntradaCrearPersonaCuentaBancaria Objin = gson.fromJson(json, EntradaCrearPersonaCuentaBancaria.class);
			
			CrearPersonaCuentaBancaria demo = new CrearPersonaCuentaBancaria();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaCrearPersonaCuentaBancaria ObjOut = demo.logica(con, Objin);
 				//con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				//er.printStackTrace();	
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}
