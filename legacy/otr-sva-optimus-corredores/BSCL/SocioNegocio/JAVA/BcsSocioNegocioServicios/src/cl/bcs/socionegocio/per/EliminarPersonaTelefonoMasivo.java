package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.RegistroSolicitudCambio;
import cl.bcs.plataforma.pla.pojo.ValorAntiguo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.cli.ModificarClienteDatosGenerales.EntidadLogGenerarContrato;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.InEntidad;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class EliminarPersonaTelefonoMasivo implements JavaCall, EjecutarCambiosPersona {
	
	static final Logger logger = LogManager.getLogger(EliminarPersonaTelefonoMasivo.class);
	
	private String SQL_SELECT_TELEFONO = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaTelefonoMasivo_SelectTelefono.sql");
	private String SQL_SELECT_TELEFONOS_ID_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaTelefonoMasivo_SelectTelefonosPorIdPersona.sql");
	public  String SQL_INSERT_LOG_GENERAR_DOCUMENTOS		= loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarClienteDatosGenerales_InsertLogGenerarContratos.sql");
	
	private final String ID_CONCEPTO 			= "BORTEL";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD 		= "TELEFONO-BORRAR";
	private final String CODIGO_CAMPO 			= "INGRESAR_TELEFONOS_TABLA";
	private final String ENTIDAD 				= "TELEFONO";
	
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
		logger.info("INICIO::EliminarPersonaTelefonoMasivo");
		long inicioServicio = System.currentTimeMillis();
		SalidaEjecutarCambiosPersona out = new SalidaEjecutarCambiosPersona();
		
		List<Long> lstIdSolicitudCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
		Long idSolicitudCambio = lstIdSolicitudCambio.get(0);
		
		ObtenerDatosCambiosEntidadPersonaEliminar obtenerDatosCambiosPersona = new ObtenerDatosCambiosEntidadPersonaEliminar();
		List<EntidadPersonaEliminar> lstEntidadPersonaEliminar = obtenerDatosCambiosPersona.logica(con, entrada, SQL_SELECT_TELEFONO);
		HashMap<String, String> parametrosMensaje = new HashMap<>(2);
	
		if (lstEntidadPersonaEliminar.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",parametrosMensaje,con,entrada.p_IdEmpresa));
			return out;
		}
		
		Long idPersona = lstEntidadPersonaEliminar.stream().findFirst().get().IdPersona;
		List<EntidadPersonaEliminar> entidadesPredeterminadas = lstEntidadPersonaEliminar.stream().filter(entidad -> "S".equals(entidad.Predeterminado)).collect(Collectors.toList());
		EliminarEntidadPersona eliminarEntidadPersona = new EliminarEntidadPersona(); 

		if(entidadesPredeterminadas.size() > 0 && entrada.p_EsCrearCliente.equals("N")) {
			List<MensajeSalida> mensajes = eliminarEntidadPersona.obtenerMensajesErrorEliminarEntidadesPredeterminadas(con,entrada.p_IdEmpresa,entidadesPredeterminadas, ENTIDAD);
			out.P_OUT_MENSAJES.addAll(mensajes);
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_MENSAJES.stream().filter(mensaje -> mensaje.tipo.equals(MensajeSalida.MENSAJE_ERROR)).count();
			out.P_OUT_REFID = idPersona;
			return out;
		}
		
		List<EntidadPersonaEliminar> registros = obtenerTelefonos(con , idPersona);
		
		if(eliminarEntidadPersona.alMenosUnRegistro(con, lstEntidadPersonaEliminar, registros, idPersona, CODIGO_CAMPO, entrada.p_IdTipoCanal)) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_TELEFONO_REQUERIDO",parametrosMensaje,con,entrada.p_IdEmpresa));
			out.P_OUT_ERRORESCONTADOR = out.P_OUT_MENSAJES.stream().filter(mensaje -> mensaje.tipo.equals(MensajeSalida.MENSAJE_ERROR)).count();
			out.P_OUT_REFID = idPersona;
			return out;
		}
		
		List<RegistroSolicitudCambio> registrosSolicitudesCambios = obtenerRegistroSolicitudesCambio(con, idSolicitudCambio, lstEntidadPersonaEliminar, entrada);
		
		if (entrada.p_RegistroCambio.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_REGISTRO_CAMBIO",parametrosMensaje,con,entrada.p_IdEmpresa));
			return out;
		}
		
		CrearRegistroSolicitudCambio servicioCrearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
		EntradaCrearRegistroSolicitudCambio inCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();
		
		inCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio = registrosSolicitudesCambios;
		servicioCrearRegistroSolicitudCambio.logica(con, inCrearRegistroSolicitudCambio);
		
		Boolean requiereAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
		
		if(requiereAutorizacion) {
			out = crearEventoAutorizacion(con, entrada, lstEntidadPersonaEliminar);
		} else {
			List<InEntidad> lstInEntidad = obtenerDatosCambiosPersona.obtenerListaPersonaEntidad(lstEntidadPersonaEliminar);
			ConfirmarCambiosPersonaEntidad confirmarCambiosEntidad = new ConfirmarCambiosPersonaEntidad();
			out = confirmarCambiosEntidad.logica(con, entrada, lstInEntidad, new ConfirmarEliminarPersonaTelefonoMasivo());
			
			List<EntidadLogGenerarContrato> params = new ArrayList<EntidadLogGenerarContrato>();	
			List<Long> idSecuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos", 1L);
			if(out.P_OUT_ERRORESCONTADOR==0) {
				
				EntidadLogGenerarContrato logGenerarContrato = new EntidadLogGenerarContrato();
				logGenerarContrato.Idlog = idSecuencias.get(0).longValue();
				logGenerarContrato.CodigoFormulario = "TELEFONO_";
				logGenerarContrato.CampoBackend = entrada.p_RegistroCambio.get(0).CampoModificado;
				logGenerarContrato.IdEmpresa = entrada.p_IdEmpresa;
				logGenerarContrato.IdCliente = 0L;
				logGenerarContrato.IdPersona = idPersona;
				logGenerarContrato.IdUsuario = entrada.p_IdUsuario;
				logGenerarContrato.FechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
				logGenerarContrato.IdPrograma = 0L;
				logGenerarContrato.Estado = "PEN";
				logGenerarContrato.Mensaje = "Persona - EliminarPersonaTelefonoMasivo.java";
				params.add(logGenerarContrato);
				
				SQL sql = new SQL();
				sql.batch(con, SQL_INSERT_LOG_GENERAR_DOCUMENTOS, params,EntidadLogGenerarContrato.class);
			}
		}
		
		out.P_OUT_REFID = idSolicitudCambio;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarPersonaTelefonoMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<EntidadPersonaEliminar> obtenerTelefonos(Connection con, Long idPersona) throws Exception {
		logger.debug("INICIO::EliminarPersonaTelefonoMasivo.obtenerTelefonos");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		List<Long> idEntidades = new ArrayList<Long>();
		idEntidades.add(idPersona);
		params.put("IdEntidad", idEntidades);
		List<EntidadPersonaEliminar> lstEntidad = sql.query(con, SQL_SELECT_TELEFONOS_ID_PERSONA, params, EntidadPersonaEliminar.class);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarPersonaTelefonoMasivo.obtenerTelefonos", finServicio - inicio);
		return lstEntidad;
	}


	private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio(Connection con, Long idSolicitudCambio, List<EntidadPersonaEliminar> entidadesPersonaEliminar, EntradaEjecutarCambiosPersona entrada) throws Exception{
		logger.trace("INICIO::EliminarPersonaTelefonoMasivo.obtenerRegistroSolicitudesCambio");
		long inicio 	= System.currentTimeMillis();
		List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<RegistroSolicitudCambio>();
		
		Map<Long, EntidadPersonaEliminar> entidadesPorId = entidadesPersonaEliminar
				.parallelStream()
				.collect(Collectors.toMap(entidad->entidad.IdEntidadPersonaEliminar, entidad->entidad));
		
		for(RegistroCambioMasivo registroCambio : entrada.p_RegistroCambio) {
			EntidadPersonaEliminar entidadPersonaEliminar = entidadesPorId.get(registroCambio.IdEntidad);
			RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
			registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
			registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
			registroSolicitudCambio.IdEntidadOrigen = String.valueOf(registroCambio.IdEntidad);
			registroSolicitudCambio.CampoModificado = registroCambio.CampoModificado; // "Borrado"
			
			ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(entidadPersonaEliminar, EntidadPersonaEliminar.class, registroCambio.CampoModificado);
			
			registroSolicitudCambio.TipoDato = valorAntiguo.TipoDato;
			registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
			registroSolicitudCambio.ValorNuevo = registroCambio.ValorNuevo;;
			registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
			
			registroSolicitudesCambio.add(registroSolicitudCambio);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::EliminarPersonaTelefonoMasivo.obtenerRegistroSolicitudesCambio", fin - inicio);
		
		return registroSolicitudesCambio;
	}
	
	private SalidaEjecutarCambiosPersona crearEventoAutorizacion(Connection con, EntradaEjecutarCambiosPersona entrada, List<EntidadPersonaEliminar> entidadPersonaEliminar) throws Exception {
		logger.debug("INICIO::EliminarPersonaTelefonoMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		
		for (EntidadPersonaEliminar telefono : entidadPersonaEliminar) {
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento 			= ID_CONCEPTO;
			evento.IdModuloSolicita 		= entrada.p_IdModulo;
			evento.IdUsuarioSolicita 		= entrada.p_IdUsuario;
			evento.Fecha 					= fechaSistema;
			evento.IdConcepto 				= ID_CONCEPTO;
			evento.NumeroOperacionConcepto 	= String.valueOf(telefono.IdEntidadPersonaEliminar); 
			evento.FechaOperacionConcepto 	= fechaSistema;
			evento.IdCliente 				= telefono.IdPersona;
			evento.Glosa = telefono.DocumentoIdentidad + " / "  + telefono.NombreCompleto + " / " + " / IdTelefono: " +  telefono.IdEntidadPersonaEliminar;
			entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS.add(evento);			
		}

		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		SalidaEjecutarCambiosPersona salidaEliminarPersonaTelefonoMasivo = new SalidaEjecutarCambiosPersona();
		salidaEliminarPersonaTelefonoMasivo.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaEliminarPersonaTelefonoMasivo.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaEliminarPersonaTelefonoMasivo.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaEliminarPersonaTelefonoMasivo.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaEliminarPersonaTelefonoMasivo.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarPersonaTelefonoMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaEliminarPersonaTelefonoMasivo;	
	}

	public static void main(String[] args) {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_RegistroCambio\":[{\"CampoModificado\": \"Borrado\",\"ValorNuevo\": \"S\",\"IdEntidad\": 2587546 }],\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"SOC\", \"p_UsaReglasNegocio\": \"N\", \"p_IdTipoCanal\":\"OPT\"}";
			EntradaEjecutarCambiosPersona Objin = gson.fromJson(json, EntradaEjecutarCambiosPersona.class);
			
			EliminarPersonaTelefonoMasivo demo = new EliminarPersonaTelefonoMasivo();
			System.out.println("ENTRADA");
			System.out.println(Objin.toString());
			
			try {
				SalidaEjecutarCambiosPersona ObjOut = demo.logica(con, Objin);
				con.rollback();
//				con.commit();
				System.out.println(ObjOut.toString());
				System.out.println("SALIDA");
			} catch (Exception er) {
				er.printStackTrace();
				con.rollback();
			}
			//con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}
}
