package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
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

public class EliminarPersonaDireccionElectronicaMasivo implements JavaCall, EjecutarCambiosPersona {

	static final Logger logger = LogManager.getLogger(EliminarPersonaDireccionElectronicaMasivo.class);
	
	private String SQL_SELECT_DIRECCION_ELECTRONICA 					= loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaDireccionElectronicaMasivo_SelectDireccionElectronica.sql");
	private String SQL_SELECT_DIRECCIONES_ELECTRONICAS_ID_PERSONA 		= loadResourceToString("cl/bcs/socionegocio/per/sql/EliminarPersonaDireccionElectronicaMasivo_SelectDireccionElectronicaPorIdPersona.sql");
	public  String SQL_INSERT_LOG_GENERAR_DOCUMENTOS					= loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarClienteDatosGenerales_InsertLogGenerarContratos.sql");
	
	private final String ID_CONCEPTO 			= "BORDIE";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD 		= "DIRECCIONELECTRONICA-BORRAR";
	private final String CODIGO_CAMPO 			= "INGRESAR_DIRECCIONES_ELECTRONICAS_TABLA";
	private final String ENTIDAD 				= "DIRECCION ELECTRONICA";

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
		logger.info("INICIO::EliminarPersonaDireccionElectronicaMasivo");
		long inicioServicio = System.currentTimeMillis();
		SalidaEjecutarCambiosPersona out = new SalidaEjecutarCambiosPersona();
		
		List<Long> lstIdSolicitudCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
		Long idSolicitudCambio = lstIdSolicitudCambio.get(0);
		HashMap<String, String> parametrosMensaje = new HashMap<>(2);
		
		ObtenerDatosCambiosEntidadPersonaEliminar obtenerDatosCambiosPersona = new ObtenerDatosCambiosEntidadPersonaEliminar();
		List<EntidadPersonaEliminar> lstEntidadPersonaEliminar = obtenerDatosCambiosPersona.logica(con, entrada, SQL_SELECT_DIRECCION_ELECTRONICA);
	
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
		
		List<EntidadPersonaEliminar> registros = obtenerDireccionesElectronicas(con , idPersona);
		
		if(eliminarEntidadPersona.alMenosUnRegistro(con, lstEntidadPersonaEliminar, registros, idPersona, CODIGO_CAMPO, entrada.p_IdTipoCanal)) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_DIRECCION_ELECTRONICA_REQUERIDA",parametrosMensaje,con,entrada.p_IdEmpresa));
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
		
		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, entrada.p_IdEmpresa, "BORDIE");
		
		if(requiereAutorizacion.equals("S")) {
			out = crearEventoAutorizacion(con, entrada, lstEntidadPersonaEliminar);
		} else {
			List<InEntidad> lstInEntidad = obtenerDatosCambiosPersona.obtenerListaPersonaEntidad(lstEntidadPersonaEliminar);
			ConfirmarCambiosPersonaEntidad confirmarCambiosEntidad = new ConfirmarCambiosPersonaEntidad();
			out = confirmarCambiosEntidad.logica(con, entrada, lstInEntidad, new ConfirmarEliminarPersonaDireccionElectronicaMasivo());
			
			List<EntidadLogGenerarContrato> params = new ArrayList<EntidadLogGenerarContrato>();	
			List<Long> idSecuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos", 1L);
			if(out.P_OUT_ERRORESCONTADOR==0) {
				
				EntidadLogGenerarContrato logGenerarContrato = new EntidadLogGenerarContrato();
				logGenerarContrato.Idlog = idSecuencias.get(0).longValue();
				logGenerarContrato.CodigoFormulario = "DIRECCION_ELECTRONICA_";
				logGenerarContrato.CampoBackend = entrada.p_RegistroCambio.get(0).CampoModificado;
				logGenerarContrato.IdEmpresa = entrada.p_IdEmpresa;
				logGenerarContrato.IdCliente = 0L;
				logGenerarContrato.IdPersona = idPersona;
				logGenerarContrato.IdUsuario = entrada.p_IdUsuario;
				logGenerarContrato.FechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
				logGenerarContrato.IdPrograma = 0L;
				logGenerarContrato.Estado = "PEN";
				logGenerarContrato.Mensaje = "Persona - EliminarPersonaDireccionElectronicaMasivo.java";
				params.add(logGenerarContrato);
				
				SQL sql = new SQL();
				sql.batch(con, SQL_INSERT_LOG_GENERAR_DOCUMENTOS, params,EntidadLogGenerarContrato.class);
			}
		}
		
		out.P_OUT_REFID = idSolicitudCambio;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarPersonaDireccionElectronicaMasivo", finServicio - inicioServicio);
		return out;
	}	

	private List<EntidadPersonaEliminar> obtenerDireccionesElectronicas(Connection con, Long idPersona) throws Exception {
		logger.debug("INICIO::EliminarPersonaDireccionElectronicaMasivo.obtenerDireccionesElectronicas");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		List<Long> idEntidades = new ArrayList<Long>();
		idEntidades.add(idPersona);
		params.put("IdEntidad", idEntidades);
		List<EntidadPersonaEliminar> lstEntidad = sql.query(con, SQL_SELECT_DIRECCIONES_ELECTRONICAS_ID_PERSONA, params, EntidadPersonaEliminar.class);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::EliminarPersonaDireccionElectronicaMasivo.obtenerDireccionesElectronicas", finServicio - inicio);
		return lstEntidad;
	}
	
	private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio(Connection con, Long idSolicitudCambio, List<EntidadPersonaEliminar> entidadesPersonaEliminar, EntradaEjecutarCambiosPersona entrada) throws Exception{
		logger.trace("INICIO::EliminarPersonaDireccionElectronicaMasivo.obtenerRegistroSolicitudesCambio");
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
		logger.trace("FIN::EliminarPersonaDireccionElectronicaMasivo.obtenerRegistroSolicitudesCambio", fin - inicio);
		
		return registroSolicitudesCambio;
	}
	
	private SalidaEjecutarCambiosPersona crearEventoAutorizacion(Connection con, EntradaEjecutarCambiosPersona entrada, List<EntidadPersonaEliminar> entidadPersonaEliminar) throws Exception {
		logger.debug("INICIO::EliminarPersonaDireccionElectronicaMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		
		for (EntidadPersonaEliminar direccion : entidadPersonaEliminar) {
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento 			= ID_CONCEPTO;
			evento.IdModuloSolicita 		= entrada.p_IdModulo;
			evento.IdUsuarioSolicita 		= entrada.p_IdUsuario;
			evento.Fecha 					= fechaSistema;
			evento.IdConcepto 				= ID_CONCEPTO;
			evento.NumeroOperacionConcepto 	= String.valueOf(direccion.IdEntidadPersonaEliminar); 
			evento.FechaOperacionConcepto 	= fechaSistema;
			evento.IdCliente 				= direccion.IdPersona;
			evento.Glosa = direccion.DocumentoIdentidad + " / "  + direccion.NombreCompleto + " / " + " / IdDirElect: " +  direccion.IdEntidadPersonaEliminar;
			entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS.add(evento);			
		}

		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		SalidaEjecutarCambiosPersona salidaEliminarPersonaDireccionElectronicaMasivo = new SalidaEjecutarCambiosPersona();
		salidaEliminarPersonaDireccionElectronicaMasivo.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaEliminarPersonaDireccionElectronicaMasivo.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaEliminarPersonaDireccionElectronicaMasivo.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaEliminarPersonaDireccionElectronicaMasivo.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaEliminarPersonaDireccionElectronicaMasivo.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EliminarPersonaDireccionElectronicaMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaEliminarPersonaDireccionElectronicaMasivo;	
	}
	

	public static void main(String[] args) throws SQLException, ConfigurationException {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTipoCanal\":\"AGE\",\"p_RegistroCambio\":[{\"CampoModificado\":\"Borrado\",\"ValorNuevo\":\"S\",\"IdEntidad\":876161}]}";
			EntradaEjecutarCambiosPersona Objin = gson.fromJson(json, EntradaEjecutarCambiosPersona.class);
			
			EliminarPersonaDireccionElectronicaMasivo demo = new EliminarPersonaDireccionElectronicaMasivo();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaEjecutarCambiosPersona ObjOut = demo.logica(con, Objin);
 				con.rollback();
// 				con.commit();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();
			}
			finally {
				con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}
