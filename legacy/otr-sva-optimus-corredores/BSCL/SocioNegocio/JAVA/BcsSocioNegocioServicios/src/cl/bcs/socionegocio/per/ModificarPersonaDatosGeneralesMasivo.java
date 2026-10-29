package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.RegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.ValorAntiguo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.EntradaConfirmarEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.InEntidad;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.SalidaConfirmarEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ValidarConyuge.EntradaConyuge;
import cl.bcs.socionegocio.per.ValidarConyuge.EntradaValidarConyuge;
import cl.bcs.socionegocio.per.ValidarConyuge.SalidaValidarConyuge;
import cl.bcs.socionegocio.per.pojo.DatosPersonaDatosGenerales;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPersonaDatosGeneralesMasivo implements JavaCall, EjecutarCambiosPersona {
	
	static final Logger logger = LogManager.getLogger(ModificarPersonaDatosGeneralesMasivo.class);
	
	private String SQL_SELECT_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaDatosGeneralesMasivo_SelectPersonaDatosGenerales.sql");
	
	private final String ID_CONCEPTO 			= "MODPER";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD 		= "PERSONA";
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEjecutarCambiosPersona jin = gson.fromJson(in.toString(), EntradaEjecutarCambiosPersona.class);
		SalidaEjecutarCambiosPersona out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	@Override
	public SalidaEjecutarCambiosPersona logica(Connection con, EntradaEjecutarCambiosPersona entrada) throws Exception {
		logger.info("INICIO::ModificarPersonaDatosGeneralesMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona out = new SalidaEjecutarCambiosPersona();
		
		if (entrada.p_RegistroCambio.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_ESPECIFICAR_REGISTRO_CAMBIO", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
			return out;		
		}
		
		List<DatosPersonaDatosGenerales> datosPersonaDatosGenerales = obtenerDatosPersonaDatosGenerales(con, entrada.p_RegistroCambio);
		
		if (datosPersonaDatosGenerales.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		Long idSolicitudCambio = CrearRegistroSolicitudCambio.obtenerIdSolicitudCambio(con);
		
		List<RegistroSolicitudCambio> registrosSolicitudesCambios =  obtenerRegistroSolicitudesCambio(con, idSolicitudCambio, datosPersonaDatosGenerales, entrada);
		
		List<MensajeSalida> validacionConyuge = validarConyuge(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, registrosSolicitudesCambios);
		
		crearSolicitudCambio(con, registrosSolicitudesCambios);
		
		Boolean requiereAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
		
		if(requiereAutorizacion) {
			out = crearEventoAutorizacion(con, entrada, datosPersonaDatosGenerales);
		} else {
			out = confirmarModificarPersonaDatosGenerales(con, entrada);
		}
		
		out.P_OUT_MENSAJES.addAll(validacionConyuge);
		out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(validacionConyuge);
		out.P_OUT_REFID = idSolicitudCambio;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPersonaDatosGeneralesMasivo", finServicio - inicioServicio);
		return out;
	}
		
	private List<DatosPersonaDatosGenerales> obtenerDatosPersonaDatosGenerales(Connection con, List<RegistroCambioMasivo> registrosCambio) throws Exception {
		logger.debug("INICIO::ModificarPersonaDatosGeneralesMasivo.obtenerDatosPersonaDatosGenerales");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsPersona = registrosCambio.stream()
				  .map(persona -> persona.IdEntidad)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdPersona", idsPersona);
		
		SQL sql = new SQL();
		List<DatosPersonaDatosGenerales> datosPersonaDatosGenerales = sql.query(con, SQL_SELECT_PERSONA, params, DatosPersonaDatosGenerales.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaDatosGeneralesMasivo.obtenerDatosPersonaDatosGenerales", fin - inicio);
		return datosPersonaDatosGenerales;
	}
	
	private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio(Connection con, Long idSolicitudCambio, List<DatosPersonaDatosGenerales> datosPersonaDatosGenerales, EntradaEjecutarCambiosPersona entrada) throws Exception {
		logger.trace("INICIO::ModificarPersonaDatosGeneralesMasivo.obtenerRegistroSolicitudesCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<>();
		
		Map<Long, DatosPersonaDatosGenerales> datosPersonaDatosGeneralesPorId = datosPersonaDatosGenerales
				.parallelStream()
				.collect(Collectors.toMap(datoPersonaDatosGenerales->datoPersonaDatosGenerales.IdPersona, datoPersonaDatosGenerales->datoPersonaDatosGenerales));
		
		for(RegistroCambioMasivo registroCambio : entrada.p_RegistroCambio) {
			
			DatosPersonaDatosGenerales datoPersonaDatosGenerales = datosPersonaDatosGeneralesPorId.get(registroCambio.IdEntidad);
			RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
			registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
			registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
			registroSolicitudCambio.IdEntidadOrigen = String.valueOf(registroCambio.IdEntidad);
			registroSolicitudCambio.CampoModificado = registroCambio.CampoModificado;
			
			ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(datoPersonaDatosGenerales, DatosPersonaDatosGenerales.class, registroCambio.CampoModificado);
			
			registroSolicitudCambio.TipoDato = valorAntiguo.TipoDato;
			registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
			registroSolicitudCambio.ValorNuevo = registroCambio.ValorNuevo;
			registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
			
			registroSolicitudesCambio.add(registroSolicitudCambio);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPersonaDatosGeneralesMasivo.obtenerRegistroSolicitudesCambio", fin - inicio);
		return registroSolicitudesCambio;		
	}
	
	private List<MensajeSalida> validarConyuge(Connection con, Long idEmpresa, Long idUsuario, List<RegistroSolicitudCambio> registrosSolicitudesCambios) throws Exception{
		logger.debug("INICIO::ModificarPersonaDatosGeneralesMasivo.validarConyuge");
		long inicio = System.currentTimeMillis();
		
		EntradaValidarConyuge entradaValidarConyuge = new EntradaValidarConyuge();
		entradaValidarConyuge.p_IdEmpresa = idEmpresa;
		entradaValidarConyuge.p_IdUsuario = idUsuario;
		entradaValidarConyuge.p_IdModulo = "SOC";
		entradaValidarConyuge.p_UsaReglasNegocio = "S";
		entradaValidarConyuge.p_Conyuges = registrosSolicitudesCambios
				.stream()
				.filter(solicitudCambio->"IdPersonaConyuge".equals(solicitudCambio.CampoModificado))
				.map(solicitudCambio->{
					EntradaConyuge entradaConyuge = new EntradaConyuge();
					entradaConyuge.IdInterno = Long.valueOf(solicitudCambio.IdEntidadOrigen);
					entradaConyuge.IdPersona = Long.valueOf(solicitudCambio.IdEntidadOrigen);
					entradaConyuge.IdPersonaConyuge = Long.valueOf(solicitudCambio.ValorNuevo);
					return entradaConyuge;
				})
				.collect(Collectors.toList());
		
		ValidarConyuge validarConyuge = new ValidarConyuge();
		SalidaValidarConyuge salidaValidarConyuge = validarConyuge.logica(con, entradaValidarConyuge);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaDatosGeneralesMasivo.validarConyuge", fin - inicio);
		return salidaValidarConyuge.P_OUT_MENSAJES;
	}
	
	private void crearSolicitudCambio(Connection con, List<RegistroSolicitudCambio> registrosSolicitudesCambios) throws Exception {
		logger.debug("INICIO::ModificarPersonaDatosGeneralesMasivo.crearSolicitudCambio");
		long inicio = System.currentTimeMillis();
		
		CrearRegistroSolicitudCambio servicioCrearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
		EntradaCrearRegistroSolicitudCambio entradaCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();
		
		entradaCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio = registrosSolicitudesCambios;
		
		servicioCrearRegistroSolicitudCambio.logica(con, entradaCrearRegistroSolicitudCambio);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaDatosGeneralesMasivo.crearSolicitudCambio", fin - inicio);
		
	}
	
	private SalidaEjecutarCambiosPersona crearEventoAutorizacion(Connection con, EntradaEjecutarCambiosPersona entrada, List<DatosPersonaDatosGenerales> datosPersonaDatosGenerales) throws Exception {
		logger.debug("INICIO::ModificarPersonaDatosGeneralesMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		
		for (DatosPersonaDatosGenerales datoPersonaDatosGenerales : datosPersonaDatosGenerales) {
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento 			= ID_CONCEPTO;
			evento.IdModuloSolicita 		= entrada.p_IdModulo;
			evento.IdUsuarioSolicita 		= entrada.p_IdUsuario;
			evento.Fecha 					= fechaSistema;
			evento.IdConcepto 				= ID_CONCEPTO;
			evento.NumeroOperacionConcepto 	= String.valueOf(datoPersonaDatosGenerales.IdPersona);
			evento.FechaOperacionConcepto 	= fechaSistema;
			evento.IdCliente 				= datoPersonaDatosGenerales.IdPersona;
			evento.Glosa = "Nombre Persona: " + datoPersonaDatosGenerales.NombreCompleto	+ " / Documento Identidad : " + datoPersonaDatosGenerales.DocumentoIdentidad + " / ";
			entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS.add(evento);
		}

		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		SalidaEjecutarCambiosPersona salidaEjecutarCambiosPersona = new SalidaEjecutarCambiosPersona();
		salidaEjecutarCambiosPersona.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		
		salidaEjecutarCambiosPersona.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		salidaEjecutarCambiosPersona.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesAdvertenciaAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE));
		salidaEjecutarCambiosPersona.P_OUT_MENSAJES.addAll(SocioNegocioUtils.obtenerMensajesErroresAutorizacion(salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE));
		
		if(salidaEjecutarCambiosPersona.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaEjecutarCambiosPersona.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_AUTORIZACION, "REQUIERE_AUTORIZACION", new HashMap<String,String>(), con, entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaDatosGeneralesMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaEjecutarCambiosPersona;	
	}
	
	private SalidaEjecutarCambiosPersona confirmarModificarPersonaDatosGenerales(Connection con, EntradaEjecutarCambiosPersona entrada) throws Exception {
		logger.debug("INICIO::ModificarPersonaDatosGeneralesMasivo.confirmarModificarPersonaDatosGenerales");
		long inicio = System.currentTimeMillis();

		EntradaConfirmarEjecutarCambiosPersona entradaConfirmarModificarPersonaDatosGeneralesMasivo = new EntradaConfirmarEjecutarCambiosPersona();		
		entradaConfirmarModificarPersonaDatosGeneralesMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfirmarModificarPersonaDatosGeneralesMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfirmarModificarPersonaDatosGeneralesMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaConfirmarModificarPersonaDatosGeneralesMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConfirmarModificarPersonaDatosGeneralesMasivo.p_IdEstadoAutorizacion = "ING";	
		entradaConfirmarModificarPersonaDatosGeneralesMasivo.p_Entidad = entrada.p_RegistroCambio.stream()
																				  .map(datosPersona -> datosPersona.IdEntidad)
																				  .distinct()
																				  .map(idPersona -> {
																					  InEntidad entradaPersonaDatosGenerales = new InEntidad();
																					  entradaPersonaDatosGenerales.IdEntidad = idPersona;
																					  return entradaPersonaDatosGenerales;
																					  })
																				  .collect(Collectors.toList());

		ConfirmarModificarPersonaDatosGeneralesMasivo confirmarModificarPersonaDatosGeneralesMasivo = new ConfirmarModificarPersonaDatosGeneralesMasivo();		
		SalidaConfirmarEjecutarCambiosPersona salidaConfirmarModificarPersonaDatosGeneralesMasivo = confirmarModificarPersonaDatosGeneralesMasivo.logica(con, entradaConfirmarModificarPersonaDatosGeneralesMasivo);
		
		SalidaEjecutarCambiosPersona out = new SalidaEjecutarCambiosPersona();
		out.P_OUT_ERRORESCONTADOR = salidaConfirmarModificarPersonaDatosGeneralesMasivo.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaConfirmarModificarPersonaDatosGeneralesMasivo.P_OUT_MENSAJES;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaDatosGeneralesMasivo.confirmarModificarPersonaDatosGenerales", fin - inicio);
		return out;
	}
	
}
