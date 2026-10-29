package cl.bcs.socionegocio.cli;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.bat.AgregarPrograma.ParametrosPrograma;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.RegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.ValorAntiguo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.ConfirmarModificarClienteDatosGeneralesMasivo.EntradaConfirmarModificarClienteDatosGeneralesMasivo;
import cl.bcs.socionegocio.cli.ConfirmarModificarClienteDatosGeneralesMasivo.InClienteDatosGenerales;
import cl.bcs.socionegocio.cli.ConfirmarModificarClienteDatosGeneralesMasivo.SalidaConfirmarModificarClienteDatosGeneralesMasivo;
import cl.bcs.socionegocio.cli.CrearCliente.EntidadCliente;
import cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales;
import cl.bcs.socionegocio.pojo.RequisitoEnrolamientoFull;
import cl.bcs.socionegocio.pojo.Validacion;
import cl.bcs.socionegocio.soc.ConsultarFichaSocioNegocio;
import cl.bcs.socionegocio.soc.ConsultarFichaSocioNegocio.EntradaConsultarFichaSocioNegocio;
import cl.bcs.socionegocio.soc.ConsultarFichaSocioNegocio.SalidaConsultarFichaSocioNegocio;
import cl.bcs.socionegocio.soc.ValidarRequisitosEnrolamientoFull;
import cl.bcs.socionegocio.soc.ValidarRequisitosEnrolamientoFull.EntradaValidarRequisitosEnrolamientoFull;
import cl.bcs.socionegocio.soc.ValidarRequisitosEnrolamientoFull.SalidaValidarRequisitosEnrolamientoFull;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModificarClienteDatosGenerales implements JavaCall {
    static final Logger logger = LogManager.getLogger(ModificarClienteDatosGenerales.class);

    private String SQL_SELECT_CLIENTE 						= loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarClienteDatosGenerales_SelectClienteDatosGenerales.sql");
    
    private final String ID_CONCEPTO = "MODCLI";
    private final String ID_ESTADO_AUTORIZACION = "PEN";
    private final String ID_TIPO_ENTIDAD = "CLIENTE";

    public static class EntradaModificarClienteDatosGenerales extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdCliente;
		public String p_IdCanal;
		public Boolean p_DesdeEnrolamiento;
		public List<RegistroCambio> p_RegistroCambio;
	
		public EntradaModificarClienteDatosGenerales() {
		    this.p_RegistroCambio = new ArrayList<RegistroCambio>();
		    this.p_DesdeEnrolamiento = false;
		}
    }

    public static class OutClienteDatosGenerales extends BcsPojo {
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String IdEstado;
		public Long IdCliente;
		public String IdEstadoAutorizacion;
    }

	public static class SalidaModificarClienteDatosGenerales extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_REFID;
		public List<OutClienteDatosGenerales> P_OUT_DATOSGENERALES;
		public List<RequisitoEnrolamientoFull> P_OUT_REQUISITOS_ENR_FULL;

		public SalidaModificarClienteDatosGenerales() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_REQUISITOS_ENR_FULL = new ArrayList<>();
		}

		public void agregarValidacionesSalida(List<Validacion> validaciones) {
			for (Validacion validacion : validaciones) {
				agregarValidacionSalida(validacion);
			}
		}

		public void agregarValidacionSalida(Validacion validacion) {
			if (validacion.EsError) {
				this.P_OUT_ERRORESCONTADOR += 1L;
				this.P_OUT_MENSAJES.add(validacion.MensajeError);
			}
		}
	}

    public static class RegistroCambio extends BcsPojo {
		public String CampoModificado;
		public String ValorNuevo;
    };

    public static class DatosGenerales extends BcsPojo {
    	public Long IdCliente;
		public String NombreCompleto;
		public String DocumentoIdentidad;

    }

    public static class ResultadoValidacionCambioExpressAFull extends BcsPojo {
    	public List<Validacion> validaciones;
    	public List<RequisitoEnrolamientoFull> requisitosEnrolamientoFull;

		public ResultadoValidacionCambioExpressAFull() {
			validaciones = new ArrayList<>();
			requisitosEnrolamientoFull = new ArrayList<>();
		}
	}
    
    public static class EntidadLogGenerarContrato extends BcsPojo{
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
		EntradaModificarClienteDatosGenerales jin = gson.fromJson(in.toString(), EntradaModificarClienteDatosGenerales.class);
		SalidaModificarClienteDatosGenerales out = logica(conn, jin);out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
    }

    public SalidaModificarClienteDatosGenerales logica(Connection con, EntradaModificarClienteDatosGenerales entrada) throws Exception {
		logger.info("INICIO::ModificarClienteDatosGenerales.logica");
		long inicioServicio = System.currentTimeMillis();
		SalidaModificarClienteDatosGenerales out = new SalidaModificarClienteDatosGenerales();
	
		List<Long> idSolicitudesCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
		Long idSolicitudCambio = idSolicitudesCambio.get(0);
	
		List<cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales> clienteDatosGenerales = obtenerDatosCliente(con, entrada);
	
		if (clienteDatosGenerales.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ENTIDAD_NO_EXISTE", null, con, entrada.p_IdEmpresa));
			return out;
		}

		List<RegistroSolicitudCambio> registrosSolicitudesCambios = obtenerRegistroSolicitudesCambio(con, idSolicitudCambio, clienteDatosGenerales, entrada);

		if (entrada.p_RegistroCambio.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_ESPECIFICAR_REGISTRO_CAMBIO", null, con, entrada.p_IdEmpresa));
			return out;
		}

		List<Validacion> validacionesBloqueoCambioFullAExpress = bloquearCambiosFullAExpress(con, entrada, clienteDatosGenerales);
		if (validacionesBloqueoCambioFullAExpress.size() > 0) {
			out.agregarValidacionesSalida(validacionesBloqueoCambioFullAExpress);

			return out;
		}

		ResultadoValidacionCambioExpressAFull resultadoValidacionCambioExpressAFull = validarCambioEnrolamientoExpressAFull(con, entrada, clienteDatosGenerales);
		if (resultadoValidacionCambioExpressAFull.validaciones.size() > 0) {
			out.agregarValidacionesSalida(resultadoValidacionCambioExpressAFull.validaciones);
			out.P_OUT_REQUISITOS_ENR_FULL.addAll(resultadoValidacionCambioExpressAFull.requisitosEnrolamientoFull);

			return out;
		}


		CrearRegistroSolicitudCambio servicioCrearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
		EntradaCrearRegistroSolicitudCambio inCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();

		inCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio = registrosSolicitudesCambios;

		servicioCrearRegistroSolicitudCambio.logica(con, inCrearRegistroSolicitudCambio);

		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO);
	
		if ("S".equals(requiereAutorizacion)) {
			out = crearEventoAutorizacion(con, entrada, clienteDatosGenerales.parallelStream().findFirst().orElse(null));
		} else {
			out = confirmarModificarClienteDatosGenerales(con, entrada);
		}

		out.P_OUT_REFID = idSolicitudCambio;

		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarClienteDatosGenerales.logica", finServicio - inicioServicio);
		return out;
	}

	private List<Validacion> bloquearCambiosFullAExpress(Connection con, EntradaModificarClienteDatosGenerales entrada, List<DatosClienteDatosGenerales> clienteDatosGenerales) throws Exception {
		logger.debug("INICIO::ModificarClienteDatosGenerales.bloquearCambiosFullAExpress");
		long inicio = System.currentTimeMillis();

		List<Validacion> validaciones = new ArrayList<>();

		for (DatosClienteDatosGenerales datoCliente : clienteDatosGenerales) {
			Optional<RegistroCambio> registroCambio = entrada.p_RegistroCambio.stream().filter(u -> "TipoEnrolamiento".equals(u.CampoModificado)).findFirst();
			boolean cambiaAExpress = registroCambio.isPresent() && "EXPRESS".equals(registroCambio.get().ValorNuevo);

			if ("FULL".equals(datoCliente.TipoEnrolamiento) && cambiaAExpress) {
				Validacion validacion = new Validacion();
				validacion.EsError = true;
				validacion.IdEntidad = datoCliente.IdCliente;
				validacion.IdTipoEntidad = "CLIENTE";
				validacion.MensajeError = new MensajeSalida(datoCliente.IdCliente, MensajeSalida.MENSAJE_ERROR, "CAMBIO_FULL_A_EXPRESS_NO_PERMITIDO",  new HashMap<>(), con, entrada.p_IdEmpresa);

				validaciones.add(validacion);
			}
		}

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteDatosGenerales.bloquearCambiosFullAExpress", fin - inicio);

		return validaciones;
	}

	private ResultadoValidacionCambioExpressAFull validarCambioEnrolamientoExpressAFull(Connection con, EntradaModificarClienteDatosGenerales entrada, List<DatosClienteDatosGenerales> clienteDatosGenerales) throws Exception {
		logger.debug("INICIO::ModificarClienteDatosGenerales.validarCambioEnrolamientoExpressAFull");
		long inicio = System.currentTimeMillis();

		ResultadoValidacionCambioExpressAFull resultado = new ResultadoValidacionCambioExpressAFull();

		EntradaConsultarFichaSocioNegocio entradaConsultarFichaSocioNegocio = new EntradaConsultarFichaSocioNegocio();
		entradaConsultarFichaSocioNegocio.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConsultarFichaSocioNegocio.p_IdUsuario = entrada.p_IdUsuario;
		entradaConsultarFichaSocioNegocio.entidades = clienteDatosGenerales.stream()
				.filter(datoCliente -> {
					Optional<RegistroCambio> registroCambio = entrada.p_RegistroCambio.stream().filter(u -> "TipoEnrolamiento".equals(u.CampoModificado)).findFirst();
					boolean cambiaAFull = registroCambio.isPresent() && "FULL".equals(registroCambio.get().ValorNuevo);

					return "EXPRESS".equals(datoCliente.TipoEnrolamiento) && cambiaAFull;
				})
				.map(datoCliente -> {
					ConsultarFichaSocioNegocio.ClienteEntidad clienteEntidad = new ConsultarFichaSocioNegocio.ClienteEntidad();
					clienteEntidad.IdCliente = datoCliente.IdCliente;

					return clienteEntidad;
				})
				.collect(Collectors.toList());

		if (entradaConsultarFichaSocioNegocio.entidades.isEmpty()) {
			return resultado;
		}

		ConsultarFichaSocioNegocio consultarFichaSocioNegocio = new ConsultarFichaSocioNegocio();
		SalidaConsultarFichaSocioNegocio salidaConsultarFichaSocioNegocio = consultarFichaSocioNegocio.logica(con, entradaConsultarFichaSocioNegocio);

		EntradaValidarRequisitosEnrolamientoFull entradaValidarRequisitosEnrolamientoFull = new EntradaValidarRequisitosEnrolamientoFull();
		entradaValidarRequisitosEnrolamientoFull.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaValidarRequisitosEnrolamientoFull.p_IdModulo = entrada.p_IdModulo;
		entradaValidarRequisitosEnrolamientoFull.p_IdUsuario = entrada.p_IdUsuario;
		entradaValidarRequisitosEnrolamientoFull.p_IdTipoCanalIngreso = entrada.p_IdCanal;
		entradaValidarRequisitosEnrolamientoFull.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaValidarRequisitosEnrolamientoFull.clientes = salidaConsultarFichaSocioNegocio.P_OUT_RESULTADOPROCESO;
		ValidarRequisitosEnrolamientoFull validarRequisitosEnrolamientoFull = new ValidarRequisitosEnrolamientoFull();
		SalidaValidarRequisitosEnrolamientoFull salidaValidarRequisitosEnrolamientoFull = validarRequisitosEnrolamientoFull.logica(con, entradaValidarRequisitosEnrolamientoFull);

		resultado.requisitosEnrolamientoFull.addAll(salidaValidarRequisitosEnrolamientoFull.P_OUT_REQUISITOS_ENR_FULL);
		resultado.validaciones.addAll(salidaValidarRequisitosEnrolamientoFull.P_OUT_VALIDACIONES);

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteDatosGenerales.validarCambioEnrolamientoExpressAFull", fin - inicio);

		return resultado;
	}

	private List<cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales> obtenerDatosCliente(Connection con, EntradaModificarClienteDatosGenerales entrada) throws Exception {
		logger.debug("INICIO::ModificarClienteDatosGenerales.obtenerDatosCliente");
		long inicio = System.currentTimeMillis();
	
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("IdCliente", entrada.p_IdCliente);
	
		List<cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales> cliente = sql.query(con, SQL_SELECT_CLIENTE, params, cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales.class);
	
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteDatosGenerales.obtenerDatosCliente", fin - inicio);
	
		return cliente;
    }

    private SalidaModificarClienteDatosGenerales crearEventoAutorizacion(Connection con, EntradaModificarClienteDatosGenerales entrada, cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales clienteDatosGenerales) throws Exception {
		logger.debug("INICIO::ModificarClienteDatosGenerales.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaModificarClienteDatosGenerales salidaModificarClienteDatosGenerales = new SalidaModificarClienteDatosGenerales();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
		evento.IdTipoEvento = ID_CONCEPTO;
		evento.IdModuloSolicita = entrada.p_IdModulo;
		evento.IdUsuarioSolicita = entrada.p_IdUsuario;
		evento.Fecha = fechaSistema;
		evento.IdConcepto = ID_CONCEPTO;
		evento.IdCliente = clienteDatosGenerales.IdCliente;
		evento.NumeroOperacionConcepto = clienteDatosGenerales.IdCliente.toString();
		evento.FechaOperacionConcepto = fechaSistema;
		evento.Glosa = "Nombre Cliente: " + clienteDatosGenerales.NombreCompleto + " / Documento Identidad : " + clienteDatosGenerales.DocumentoIdentidad + " / ";
		
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = Arrays.asList(evento);
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaModificarClienteDatosGenerales.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error -> new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaModificarClienteDatosGenerales.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaModificarClienteDatosGenerales.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if (salidaModificarClienteDatosGenerales.P_OUT_ERRORESCONTADOR.equals(0L)) {
		    salidaModificarClienteDatosGenerales.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_AUTORIZACION, "REQUIERE_AUTORIZACION", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteDatosGenerales.crearEventoAutorizacion", fin - inicio);
		return salidaModificarClienteDatosGenerales;
    }
    
    private SalidaModificarClienteDatosGenerales confirmarModificarClienteDatosGenerales(Connection con, EntradaModificarClienteDatosGenerales entrada) throws Exception {
		logger.debug("INICIO::ModificarClienteDatosGenerales.confirmarModificarClienteDatosGenerales");
		long inicio = System.currentTimeMillis();
		EntradaConfirmarModificarClienteDatosGeneralesMasivo entradaConfirmarModificarClienteDatosGeneralesMasivo = new EntradaConfirmarModificarClienteDatosGeneralesMasivo();
		InClienteDatosGenerales entradaClienteDatosGenerales = new InClienteDatosGenerales(entrada.p_IdCliente);
		entradaClienteDatosGenerales.IdCliente = entrada.p_IdCliente;
		
		entradaConfirmarModificarClienteDatosGeneralesMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfirmarModificarClienteDatosGeneralesMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfirmarModificarClienteDatosGeneralesMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaConfirmarModificarClienteDatosGeneralesMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConfirmarModificarClienteDatosGeneralesMasivo.p_ClienteDatosGenerales.add(entradaClienteDatosGenerales);
		entradaConfirmarModificarClienteDatosGeneralesMasivo.p_IdEstadoAutorizacion = "MOD";
		
		ConfirmarModificarClienteDatosGeneralesMasivo confirmarModificarClienteDatosGeneralesMasivo = new ConfirmarModificarClienteDatosGeneralesMasivo();
		
		SalidaConfirmarModificarClienteDatosGeneralesMasivo salidaConfirmarModificarClienteDatosGeneralesMasivo = confirmarModificarClienteDatosGeneralesMasivo.logica(con, entradaConfirmarModificarClienteDatosGeneralesMasivo);
		
		SalidaModificarClienteDatosGenerales out = new SalidaModificarClienteDatosGenerales();
		out.P_OUT_ERRORESCONTADOR = salidaConfirmarModificarClienteDatosGeneralesMasivo.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaConfirmarModificarClienteDatosGeneralesMasivo.P_OUT_MENSAJES;
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarClienteDatosGenerales.confirmarModificarClienteDatosGenerales", fin - inicio);
		return out;
    }
    
    private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio(Connection con, Long idSolicitudCambio, List<cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales> clientesDatosGenerales, EntradaModificarClienteDatosGenerales entrada) throws Exception {
		logger.trace("INICIO::ModificarClienteDatosGenerales.obtenerRegistroSolicitudesCambio");
		long inicio = System.currentTimeMillis();
		List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<RegistroSolicitudCambio>();
		
		Map<Long, cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales> datosGeneralesPorId = clientesDatosGenerales
				.parallelStream()
				.collect(Collectors.toMap(clienteDatosGenerales->clienteDatosGenerales.IdCliente, clienteDatosGenerales->clienteDatosGenerales));
		
		for (RegistroCambio registroCambio : entrada.p_RegistroCambio) {
			cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales clienteDatosGenerales = datosGeneralesPorId.get(entrada.p_IdCliente);
			
			ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(clienteDatosGenerales, cl.bcs.socionegocio.cli.pojo.DatosClienteDatosGenerales.class, registroCambio.CampoModificado);
			if (valorAntiguo.Valor.equalsIgnoreCase(registroCambio.ValorNuevo)) {
				if (entrada.p_DesdeEnrolamiento) {
					valorAntiguo.Valor = "";
				} else {
					continue;
				}
			} 
			
		    RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
		    registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
		    registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
		    registroSolicitudCambio.IdEntidadOrigen = String.valueOf(entrada.p_IdCliente);
		    registroSolicitudCambio.CampoModificado = registroCambio.CampoModificado;
		    
		    
			
		    registroSolicitudCambio.TipoDato = valorAntiguo.TipoDato;
		    registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
		    registroSolicitudCambio.ValorNuevo = registroCambio.ValorNuevo;
		    registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
	
		    registroSolicitudesCambio.add(registroSolicitudCambio);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarClienteDatosGenerales.obtenerRegistroSolicitudesCambio", fin - inicio);
		return registroSolicitudesCambio;
    }

    public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
	
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
	
		    String json="{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdCliente\":1424304,\"p_IdCanal\":\"OPT\",\"p_RegistroCambio\":[{\"CampoModificado\":\"IdSegmentoCliente\",\"ValorNuevo\":\"\"},{\"CampoModificado\":\"TipoEnrolamiento\",\"ValorNuevo\":\"FULL\"}]}";
		    EntradaModificarClienteDatosGenerales Objin = gson.fromJson(json, EntradaModificarClienteDatosGenerales.class);
	
		    ModificarClienteDatosGenerales demo = new ModificarClienteDatosGenerales();
		    System.out.println("ENTRADA");
		    System.out.println(Objin.toString());
		    System.out.println("SALIDA");
	
		    try {
			SalidaModificarClienteDatosGenerales ObjOut = demo.logica(con, Objin);
			con.rollback();
			System.out.println(ObjOut.toString());
		    } catch (Exception er) {
			er.printStackTrace();
		    } finally {
			con.rollback();
			con.close();
		    }
		}
		System.out.println("== END ==");
    }
}