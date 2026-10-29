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
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.InEntidad;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.per.pojo.Telefono;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPersonaTelefonoMasivo implements JavaCall, EjecutarCambiosPersona {
	
	static final Logger logger = LogManager.getLogger(ModificarPersonaTelefonoMasivo.class);
	
	private String SQL_SELECT_TELEFONO = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaTelefonoMasivo_SelectTelefono.sql");
	
	private final String ID_CONCEPTO 			= "MODTEL";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD 		= "TELEFONO";
	
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
		logger.info("INICIO::ModificarPersonaTelefonoMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona out = new SalidaEjecutarCambiosPersona();

		if (entrada.p_RegistroCambio.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_ESPECIFICAR_REGISTRO_CAMBIO", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
			return out;		
		}
		
		List<Telefono> telefonos = obtenerTelefonos(con, entrada.p_RegistroCambio);
		
		if (telefonos.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		Long idSolicitudCambio = CrearRegistroSolicitudCambio.obtenerIdSolicitudCambio(con);
		
		List<RegistroSolicitudCambio> registrosSolicitudesCambios =  obtenerRegistroSolicitudesCambio(con, idSolicitudCambio, telefonos, entrada);
		
		crearSolicitudCambio(con, registrosSolicitudesCambios);
		
		Boolean requiereAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
		
		if(requiereAutorizacion) {
			out = crearEventoAutorizacion(con, entrada, telefonos);
		} else {
			List<InEntidad> lstInEntidad = obtenerListaPersonaEntidad(telefonos);		
			ConfirmarCambiosPersonaEntidad confirmarCambiosEntidad = new ConfirmarCambiosPersonaEntidad();
			confirmarCambiosEntidad.logica(con, entrada, lstInEntidad, new ConfirmarModificarPersonaTelefonoMasivo());
		}
		
		out.P_OUT_REFID = idSolicitudCambio;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPersonaTelefonoMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<InEntidad> obtenerListaPersonaEntidad(List<Telefono> telefonos) {
		logger.trace("INICIO::ModificarPersonaTelefonoMasivo.obtenerListaPersonaEntidad");
		long inicio = System.currentTimeMillis();		
		List<InEntidad> lstInEntidad = telefonos.stream()
				  .map(entidad -> new InEntidad(entidad.IdPersona, entidad.IdTelefono))
				  .distinct()
				  .collect(Collectors.toList());	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPersonaTelefonoMasivo.obtenerListaPersonaEntidad", fin - inicio);
		return lstInEntidad;
	}
	
	private List<Telefono> obtenerTelefonos(Connection con, List<RegistroCambioMasivo> registrosCambio) throws Exception {
		logger.debug("INICIO::ModificarPersonaTelefonoMasivo.obtenerTelefonos");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsTelefono = registrosCambio.stream()
				  .map(telefono -> telefono.IdEntidad)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdTelefono", idsTelefono);
		
		SQL sql = new SQL();
		List<Telefono> telefonos = sql.query(con, SQL_SELECT_TELEFONO, params, Telefono.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaTelefonoMasivo.obtenerTelefonos", fin - inicio);
		return telefonos;
	}
	
	private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio(Connection con, Long idSolicitudCambio, List<Telefono> telefonos, EntradaEjecutarCambiosPersona entrada) throws Exception {
		logger.debug("INICIO::ModificarPersonaTelefonoMasivo.obtenerRegistroSolicitudesCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<>();
		
		Map<Long, Telefono> telefonosPorId = telefonos
				.parallelStream()
				.collect(Collectors.toMap(telefono->telefono.IdTelefono, telefono->telefono));
		
		for(RegistroCambioMasivo registroCambio : entrada.p_RegistroCambio) {
			
			Telefono telefono = telefonosPorId.get(registroCambio.IdEntidad);
			
			if( telefono != null ) {
				RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
				registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
				registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
				registroSolicitudCambio.IdEntidadOrigen = String.valueOf(registroCambio.IdEntidad);
				registroSolicitudCambio.CampoModificado = registroCambio.CampoModificado;
				
				ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(telefono, Telefono.class, registroCambio.CampoModificado);
				
				registroSolicitudCambio.TipoDato = valorAntiguo.TipoDato;
				registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
				registroSolicitudCambio.ValorNuevo = registroCambio.ValorNuevo;
				registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
				
				registroSolicitudesCambio.add(registroSolicitudCambio);
			}
			
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPersonaTelefonoMasivo.obtenerRegistroSolicitudesCambio", fin - inicio);
		return registroSolicitudesCambio;		
	}
	
	private void crearSolicitudCambio(Connection con, List<RegistroSolicitudCambio> registrosSolicitudesCambios) throws Exception {
		logger.debug("INICIO::ModificarPersonaTelefonoMasivo.crearSolicitudCambio");
		long inicio = System.currentTimeMillis();
		
		CrearRegistroSolicitudCambio servicioCrearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
		EntradaCrearRegistroSolicitudCambio entradaCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();
		
		entradaCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio = registrosSolicitudesCambios;
		
		servicioCrearRegistroSolicitudCambio.logica(con, entradaCrearRegistroSolicitudCambio);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaTelefonoMasivo.crearSolicitudCambio", fin - inicio);
		
	}
	
	private SalidaEjecutarCambiosPersona crearEventoAutorizacion(Connection con, EntradaEjecutarCambiosPersona entrada, List<Telefono> telefonos) throws Exception {
		logger.debug("INICIO::ModificarPersonaTelefonoMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		
		for (Telefono telefono : telefonos) {
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento 			= ID_CONCEPTO;
			evento.IdModuloSolicita 		= entrada.p_IdModulo;
			evento.IdUsuarioSolicita 		= entrada.p_IdUsuario;
			evento.Fecha 					= fechaSistema;
			evento.IdConcepto 				= ID_CONCEPTO;
			evento.NumeroOperacionConcepto 	= String.valueOf(telefono.IdTelefono);
			evento.FechaOperacionConcepto 	= fechaSistema;
			evento.IdCliente 				= telefono.IdPersona;
			evento.Glosa = telefono.DocumentoIdentidad + " / "  + telefono.NombreCompleto + " / " +  telefono.Numero + " / IdTel: " +  telefono.IdTelefono;
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
		logger.debug("FIN::ModificarPersonaTelefonoMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaEjecutarCambiosPersona;	
	}
}
