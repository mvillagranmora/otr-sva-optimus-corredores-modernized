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
import cl.bcs.socionegocio.per.pojo.DireccionElectronica;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPersonaDireccionElectronicaMasivo implements JavaCall, EjecutarCambiosPersona {
	
	static final Logger logger = LogManager.getLogger(ModificarPersonaDireccionElectronicaMasivo.class);
	
	private String SQL_SELECT_DIRECCION_ELECTRONICA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaDireccionElectronicaMasivo_SelectDireccionElectronica.sql");
	
	private final String ID_CONCEPTO 			= "MODDIE";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD 		= "DIRECCIONELECTRONICA";
	
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
		logger.info("INICIO::ModificarPersonaDireccionElectronicaMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona out = new SalidaEjecutarCambiosPersona();
		
		if (entrada.p_RegistroCambio.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_ESPECIFICAR_REGISTRO_CAMBIO", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
			return out;		
		}
		
		List<DireccionElectronica> direccionesElectronica = obtenerDireccionesElectronica(con, entrada.p_RegistroCambio);
		
		if (direccionesElectronica.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		Long idSolicitudCambio = CrearRegistroSolicitudCambio.obtenerIdSolicitudCambio(con);
		
		List<RegistroSolicitudCambio> registrosSolicitudesCambios =  obtenerRegistroSolicitudesCambio(con, idSolicitudCambio, direccionesElectronica, entrada);
		
		crearSolicitudCambio(con, registrosSolicitudesCambios);
		
		Boolean requiereAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
				
		if(requiereAutorizacion) {
			out = crearEventoAutorizacion(con, entrada, direccionesElectronica);
		} else {
			List<InEntidad> lstInEntidad = obtenerListaPersonaEntidad(direccionesElectronica);
			ConfirmarCambiosPersonaEntidad confirmarCambiosEntidad = new ConfirmarCambiosPersonaEntidad();
			out = confirmarCambiosEntidad.logica(con, entrada, lstInEntidad, new ConfirmarModificarPersonaDireccionElectronicaMasivo());
		}
		
		out.P_OUT_REFID = idSolicitudCambio;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPersonaDireccionElectronicaMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<InEntidad> obtenerListaPersonaEntidad(List<DireccionElectronica> direccionesElectronica) {
		logger.trace("INICIO::ModificarPersonaDireccionElectronicaMasivo.obtenerListaPersonaEntidad");
		long inicio = System.currentTimeMillis();		
		List<InEntidad> lstInEntidad = direccionesElectronica.stream()
				  .map(entidad -> new InEntidad(entidad.IdPersona, entidad.IdDireccionElectronica))
				  .distinct()
				  .collect(Collectors.toList());	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPersonaDireccionElectronicaMasivo.obtenerListaPersonaEntidad", fin - inicio);
		return lstInEntidad;
	}
	
	private List<DireccionElectronica> obtenerDireccionesElectronica(Connection con, List<RegistroCambioMasivo> registrosCambio) throws Exception {
		logger.debug("INICIO::ModificarPersonaDireccionElectronicaMasivo.obtenerDireccionesElectronica");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsDireccionElectronica = registrosCambio.stream()
				  .map(direccionElectronica -> direccionElectronica.IdEntidad)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdDireccionElectronica", idsDireccionElectronica);
		
		SQL sql = new SQL();
		List<DireccionElectronica> direccionesElectronicas = sql.query(con, SQL_SELECT_DIRECCION_ELECTRONICA, params, DireccionElectronica.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaDireccionElectronicaMasivo.obtenerDireccionesElectronica", fin - inicio);
		return direccionesElectronicas;
	}
	
	private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio(Connection con, Long idSolicitudCambio, List<DireccionElectronica> direccionesElectronica, EntradaEjecutarCambiosPersona entrada) throws Exception {
		logger.debug("INICIO::ModificarPersonaDireccionElectronicaMasivo.obtenerRegistroSolicitudesCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<>();
		
		Map<Long, DireccionElectronica> direccionesElectronicaPorId = direccionesElectronica
				.parallelStream()
				.collect(Collectors.toMap(direccionElectronica->direccionElectronica.IdDireccionElectronica, direccionElectronica->direccionElectronica));
		
		for(RegistroCambioMasivo registroCambio : entrada.p_RegistroCambio) {
			
			DireccionElectronica direccionElectronica = direccionesElectronicaPorId.get(registroCambio.IdEntidad);
			
			if(direccionElectronica != null) {
				RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
				registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
				registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
				registroSolicitudCambio.IdEntidadOrigen = String.valueOf(registroCambio.IdEntidad);
				registroSolicitudCambio.CampoModificado = registroCambio.CampoModificado;
				
				ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(direccionElectronica, DireccionElectronica.class, registroCambio.CampoModificado);
				
				registroSolicitudCambio.TipoDato = valorAntiguo.TipoDato;
				registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
				registroSolicitudCambio.ValorNuevo = registroCambio.ValorNuevo;
				registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
				
				registroSolicitudesCambio.add(registroSolicitudCambio);
			}
		
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPersonaDireccionElectronicaMasivo.obtenerRegistroSolicitudesCambio", fin - inicio);
		return registroSolicitudesCambio;		
	}
	
	private void crearSolicitudCambio(Connection con, List<RegistroSolicitudCambio> registrosSolicitudesCambios) throws Exception {
		logger.debug("INICIO::ModificarPersonaDireccionElectronicaMasivo.crearSolicitudCambio");
		long inicio = System.currentTimeMillis();
		
		CrearRegistroSolicitudCambio servicioCrearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
		EntradaCrearRegistroSolicitudCambio entradaCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();
		
		entradaCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio = registrosSolicitudesCambios;
		
		servicioCrearRegistroSolicitudCambio.logica(con, entradaCrearRegistroSolicitudCambio);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaDireccionElectronicaMasivo.crearSolicitudCambio", fin - inicio);
		
	}
	
	private SalidaEjecutarCambiosPersona crearEventoAutorizacion(Connection con, EntradaEjecutarCambiosPersona entrada, List<DireccionElectronica> direccionesElectronica) throws Exception {
		logger.debug("INICIO::ModificarPersonaDireccionElectronicaMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		
		for (DireccionElectronica direccionElectronica : direccionesElectronica) {
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento 			= ID_CONCEPTO;
			evento.IdModuloSolicita 		= entrada.p_IdModulo;
			evento.IdUsuarioSolicita 		= entrada.p_IdUsuario;
			evento.Fecha 					= fechaSistema;
			evento.IdConcepto 				= ID_CONCEPTO;
			evento.NumeroOperacionConcepto 	= String.valueOf(direccionElectronica.IdDireccionElectronica);
			evento.FechaOperacionConcepto 	= fechaSistema;
			evento.IdCliente 				= direccionElectronica.IdPersona;
			evento.Glosa = direccionElectronica.DocumentoIdentidad + " / "  + direccionElectronica.NombreCompleto + " / " +  direccionElectronica.Direccion + " / IdDir: " +  direccionElectronica.IdDireccionElectronica;
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
		logger.debug("FIN::ModificarPersonaDireccionElectronicaMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaEjecutarCambiosPersona;	
	}
}
