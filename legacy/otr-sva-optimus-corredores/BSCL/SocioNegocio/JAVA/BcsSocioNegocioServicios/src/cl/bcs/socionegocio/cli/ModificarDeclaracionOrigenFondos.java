package cl.bcs.socionegocio.cli;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import cl.bcs.socionegocio.cli.ConfirmarModificarDeclaracionOrigenFondosMasivo.EntradaConfirmarModificarDeclaracionOrigenFondosMasivo;
import cl.bcs.socionegocio.cli.ConfirmarModificarDeclaracionOrigenFondosMasivo.InDeclaracionOrigenFondos;
import cl.bcs.socionegocio.cli.ConfirmarModificarDeclaracionOrigenFondosMasivo.SalidaConfirmarModificarDeclaracionOrigenFondosMasivo;
import cl.bcs.socionegocio.cli.pojo.DeclaracionOrigenFondos;
import cl.bcs.socionegocio.cli.pojo.DetalleOrigenFondo;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarDeclaracionOrigenFondos implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarDeclaracionOrigenFondos.class);
	
	private final String SQL_SELECT_DECLARACION_ORIGEN_FONDOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarDeclaracionOrigenFondos_SelectDeclaracionOrigenFondos.sql");
	private final String SQL_SELECT_DETALLE_DECLARACION_ORIGEN_FONDOS = loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarDeclaracionOrigenFondos_SelectDetalleDeclaracionOrigenFondos.sql");
	private final String SQL_SELECT_TIPO_ORIGEN_FONDO = loadResourceToString("cl/bcs/socionegocio/cli/sql/ModificarDeclaracionOrigenFondos_SelectTipoOrigenFondo.sql");
	
	private final String ID_CONCEPTO = "MODDOF";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD = "CLIENTEDOF";
	
	public static class EntradaModificarDeclaracionOrigenFondos extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdDeclaracionOrigenFondos;
		public List<RegistroCambio> p_RegistroCambio;
	}
	
    public static class RegistroCambio extends BcsPojo {
		public String CampoModificado;
		public String ValorNuevo;
    }
    
    public static class SalidaModificarDeclaracionOrigenFondos extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;		
		public Long P_OUT_REFID;
    }
    
    public static class TipoOrigenFondo extends BcsPojo{
    	public String IdTipoOrigenFondo;
    }

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarDeclaracionOrigenFondos jin = gson.fromJson(in.toString(), EntradaModificarDeclaracionOrigenFondos.class);
		SalidaModificarDeclaracionOrigenFondos out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaModificarDeclaracionOrigenFondos logica(Connection con, EntradaModificarDeclaracionOrigenFondos entrada) throws Exception {
		logger.info("INICIO::ModificarDeclaracionOrigenFondos");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaModificarDeclaracionOrigenFondos salida = new SalidaModificarDeclaracionOrigenFondos();
		
		List<Long> idSolicitudesCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
		Long idSolicitudCambio = idSolicitudesCambio.get(0);
	
		Set<String> tipoOrigenFondo = obtenerTipoOrigenFondo(con, entrada.p_IdEmpresa);
		
		List<RegistroCambio> registroCambioDof = crearRegistroCambioDof(entrada.p_RegistroCambio, tipoOrigenFondo);
		List<RegistroCambio> registroCambioDetalleDof = crearRegistroCambioDetalleoDof(entrada.p_RegistroCambio, tipoOrigenFondo);
				
		Map<Long, DeclaracionOrigenFondos> declaracionOrigenFondosPorIdDof = obtenerDeclaracionesOrigenFondos(con, entrada.p_IdDeclaracionOrigenFondos);
		Map<String, DetalleOrigenFondo> detallePorIdDof = obtenerDetalleDeclaracionOrigenFondos(con, new ArrayList<>(declaracionOrigenFondosPorIdDof.keySet()));
		
		List<DeclaracionOrigenFondos> declaracionOrigenFondos = new ArrayList<>(declaracionOrigenFondosPorIdDof.values());

		if(declaracionOrigenFondos.size() == 0) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return salida;
		}
		
		List<RegistroSolicitudCambio> registrosSolicitudesCambiosDof = obtenerRegistroSolicitudesCambioDof(con, idSolicitudCambio, registroCambioDof, declaracionOrigenFondos, entrada);
		List<RegistroSolicitudCambio> registrosSolicitudesCambiosDetalleDof = obtenerRegistroSolicitudesCambioDetalleDof(con, idSolicitudCambio, registroCambioDetalleDof, detallePorIdDof, entrada);

		List<RegistroSolicitudCambio> registrosSolicitudesCambios = new ArrayList<>();
		registrosSolicitudesCambios.addAll(registrosSolicitudesCambiosDof);
		registrosSolicitudesCambios.addAll(registrosSolicitudesCambiosDetalleDof);

		if (entrada.p_RegistroCambio.isEmpty()){
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_REGISTRO_CAMBIO",null,con,entrada.p_IdEmpresa));
			return salida;		
		}
		
		CrearRegistroSolicitudCambio servicioCrearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
		EntradaCrearRegistroSolicitudCambio inCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();
		inCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio = registrosSolicitudesCambios;
		servicioCrearRegistroSolicitudCambio.logica(con, inCrearRegistroSolicitudCambio);
		
		Boolean requiereAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
		
		if(requiereAutorizacion) {
			salida = crearEventoAutorizacion(con, entrada, declaracionOrigenFondos.parallelStream().findFirst().orElse(null));
		} else {
			salida = confirmarModificarDeclaracionOrigenFondos(con, entrada);
		}
		
		salida.P_OUT_REFID = idSolicitudCambio;
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarDeclaracionOrigenFondos", finServicio - inicioServicio);
		return salida;
	}

	private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambioDetalleDof(Connection con, Long idSolicitudCambio, List<RegistroCambio> registroCambioDetalleDof, Map<String, DetalleOrigenFondo> detallePorIdDof, EntradaModificarDeclaracionOrigenFondos entrada) {
		logger.debug("INICIO::ModificarDeclaracionOrigenFondos.obtenerRegistroSolicitudesCambioDetalleDof");
		long inicio = System.currentTimeMillis();
		
		List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<RegistroSolicitudCambio>();
		
		for(RegistroCambio registroCambio : registroCambioDetalleDof) {
			String id = String.valueOf(entrada.p_IdDeclaracionOrigenFondos) + "-" + registroCambio.CampoModificado;
			DetalleOrigenFondo detalleOrigenFondo = detallePorIdDof.get(id);
		    RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
		    registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
		    registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
		    registroSolicitudCambio.IdEntidadOrigen = String.valueOf(entrada.p_IdDeclaracionOrigenFondos);
		    registroSolicitudCambio.CampoModificado = "IdTipoOrigenFondo";	
			registroSolicitudCambio.TipoDato = "NVARCHAR";
			registroSolicitudCambio.ValorAntiguo = String.valueOf(detalleOrigenFondo.Valor);
			registroSolicitudCambio.ValorNuevo = registroCambio.CampoModificado + "-" +registroCambio.ValorNuevo;
			registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
			
			registroSolicitudesCambio.add(registroSolicitudCambio);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDeclaracionOrigenFondos.obtenerRegistroSolicitudesCambioDetalleDof", fin - inicio);
		return registroSolicitudesCambio;
	}

	private List<RegistroCambio> crearRegistroCambioDof(List<RegistroCambio> registroCambio, Set<String> tipoOrigenFondo) {
			return registroCambio.stream().filter(r -> !tipoOrigenFondo.contains(r.CampoModificado)).collect(Collectors.toList());
	}

	private List<RegistroCambio> crearRegistroCambioDetalleoDof(List<RegistroCambio> registroCambio, Set<String> tipoOrigenFondo) {
		return registroCambio.stream().filter(r -> tipoOrigenFondo.contains(r.CampoModificado)).collect(Collectors.toList());
	}

	private SalidaModificarDeclaracionOrigenFondos confirmarModificarDeclaracionOrigenFondos(Connection con, EntradaModificarDeclaracionOrigenFondos entrada) throws Exception {
			logger.debug("INICIO::ModificarDeclaracionOrigenFondos.confirmarModificarDeclaracionOrigenFondos");
			long inicio 	= System.currentTimeMillis();
			EntradaConfirmarModificarDeclaracionOrigenFondosMasivo entradaConfirmarModificarDeclaracionOrigenFondosMasivo = new EntradaConfirmarModificarDeclaracionOrigenFondosMasivo();
			InDeclaracionOrigenFondos entradaClienteDOF = new InDeclaracionOrigenFondos(entrada.p_IdDeclaracionOrigenFondos);
			
			entradaConfirmarModificarDeclaracionOrigenFondosMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
			entradaConfirmarModificarDeclaracionOrigenFondosMasivo.p_IdUsuario = entrada.p_IdUsuario;
			entradaConfirmarModificarDeclaracionOrigenFondosMasivo.p_IdModulo = entrada.p_IdModulo;
			entradaConfirmarModificarDeclaracionOrigenFondosMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
			entradaConfirmarModificarDeclaracionOrigenFondosMasivo.p_DeclaracionOrigenFondos.add(entradaClienteDOF);
			entradaConfirmarModificarDeclaracionOrigenFondosMasivo.p_IdEstadoAutorizacion = "MOD";
				
			ConfirmarModificarDeclaracionOrigenFondosMasivo confirmarModificarDeclaracionOrigenFondosMasivo = new ConfirmarModificarDeclaracionOrigenFondosMasivo();
			
			SalidaConfirmarModificarDeclaracionOrigenFondosMasivo salidaConfirmarModificarDeclaracionOrigenFondosMasivo = confirmarModificarDeclaracionOrigenFondosMasivo.logica(con, entradaConfirmarModificarDeclaracionOrigenFondosMasivo);
			
			SalidaModificarDeclaracionOrigenFondos salida = new SalidaModificarDeclaracionOrigenFondos();
			salida.P_OUT_ERRORESCONTADOR = salidaConfirmarModificarDeclaracionOrigenFondosMasivo.P_OUT_ERRORESCONTADOR;
			salida.P_OUT_MENSAJES = salidaConfirmarModificarDeclaracionOrigenFondosMasivo.P_OUT_MENSAJES;
			
			long fin 	= System.currentTimeMillis();
			logger.debug("FIN::ModificarDeclaracionOrigenFondos.confirmarModificarDeclaracionOrigenFondos", fin - inicio);
			return salida;
	}

	private SalidaModificarDeclaracionOrigenFondos crearEventoAutorizacion(Connection con,EntradaModificarDeclaracionOrigenFondos entrada, DeclaracionOrigenFondos declaracionOrigenFondos) throws Exception {
		logger.debug("INICIO::ModificarDeclaracionOrigenFondos.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaModificarDeclaracionOrigenFondos salidaModificarDeclaracionOrigenFondos = new SalidaModificarDeclaracionOrigenFondos();
		
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
		evento.NumeroOperacionConcepto = String.valueOf(declaracionOrigenFondos.IdDeclaracionOrigenFondos);
		evento.FechaOperacionConcepto = fechaSistema;
		evento.IdCliente = declaracionOrigenFondos.IdCliente;
		//evento.Glosa = "Nombre Cliente: " + declaracionOrigenFondos.NombreCompleto + " / Documento Identidad : " + declaracionOrigenFondos.DocumentoIdentidad + " / ";

		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = Arrays.asList(evento);
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaModificarDeclaracionOrigenFondos.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaModificarDeclaracionOrigenFondos.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaModificarDeclaracionOrigenFondos.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaModificarDeclaracionOrigenFondos.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaModificarDeclaracionOrigenFondos.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDeclaracionOrigenFondos.crearEventoAutorizacion", fin - inicio);
		return salidaModificarDeclaracionOrigenFondos;	
	}

	private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambioDof(Connection con, Long idSolicitudCambio, List<RegistroCambio> registroCambioDof, List<DeclaracionOrigenFondos> declaracionOrigenFondos, EntradaModificarDeclaracionOrigenFondos entrada) throws Exception {
		logger.debug("INICIO::ModificarDeclaracionOrigenFondos.obtenerRegistroSolicitudesCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<RegistroSolicitudCambio>();
		
		Map<Long, DeclaracionOrigenFondos> declaracionOrigenFondosPorId = declaracionOrigenFondos
				.parallelStream()
				.collect(Collectors.toMap(dof -> dof.IdDeclaracionOrigenFondos, dof -> dof));
		
		for(RegistroCambio registroCambio : registroCambioDof) {
			DeclaracionOrigenFondos declaracion = declaracionOrigenFondosPorId.get(entrada.p_IdDeclaracionOrigenFondos);
		    RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
		    registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
		    registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
		    registroSolicitudCambio.IdEntidadOrigen = String.valueOf(declaracion.IdDeclaracionOrigenFondos);
		    registroSolicitudCambio.CampoModificado = registroCambio.CampoModificado;
		    
			ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(declaracion, DeclaracionOrigenFondos.class, registroCambio.CampoModificado);
			
			registroSolicitudCambio.TipoDato = valorAntiguo.TipoDato;
			registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
			registroSolicitudCambio.ValorNuevo = registroCambio.ValorNuevo;
			registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
			
			registroSolicitudesCambio.add(registroSolicitudCambio);
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDeclaracionOrigenFondos.obtenerRegistroSolicitudesCambio", fin - inicio);
		return registroSolicitudesCambio;
	}

	private Map<Long, DeclaracionOrigenFondos> obtenerDeclaracionesOrigenFondos(Connection con, Long idDeclaracionOrigenFondos) throws Exception {
		logger.debug("INICIO::ModificarDeclaracionOrigenFondos.obtenerDeclaracionesOrigenFondos");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdDeclaracionOrigenFondos", idDeclaracionOrigenFondos);
		
		Map<Long, DeclaracionOrigenFondos> declaracionOrigenFondos = sql.query(con, SQL_SELECT_DECLARACION_ORIGEN_FONDOS, params, DeclaracionOrigenFondos.class)
				.stream()
				.collect(Collectors.toMap(dof -> dof.IdDeclaracionOrigenFondos, dof -> dof));;
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDeclaracionOrigenFondos.obtenerDeclaracionesOrigenFondos", fin - inicio);
		return declaracionOrigenFondos;
	}
	
	private Map<String, DetalleOrigenFondo> obtenerDetalleDeclaracionOrigenFondos(Connection con, List<Long> idsDeclaracionOrigenFondos) throws Exception{
		logger.debug("INICIO::ModificarDeclaracionOrigenFondos.obtenerDetalleDeclaracionOrigenFondos");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdsDeclaracionOrigenFondos", idsDeclaracionOrigenFondos);
		
		Map<String, DetalleOrigenFondo> detalleDeclaracionOrigenFondos = sql.query(con, SQL_SELECT_DETALLE_DECLARACION_ORIGEN_FONDOS, parametros, DetalleOrigenFondo.class)
				.stream()
				.collect(Collectors.toMap(ddof -> String.valueOf(ddof.IdDeclaracionOrigenFondos) + "-" + ddof.IdTipoOrigenFondo, ddof -> ddof));
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDeclaracionOrigenFondos.obtenerDetalleDeclaracionOrigenFondos", fin - inicio);
		return detalleDeclaracionOrigenFondos;
	}
	
	private Set<String> obtenerTipoOrigenFondo(Connection con, Long idEmpresa) throws Exception{
		logger.debug("INICIO::ModificarDeclaracionOrigenFondos.obtenerTipoOrigenFondo");
		long inicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		
		HashMap<String, Object> parametros = new HashMap<String, Object>(2);
		parametros.put("IdEmpresa", idEmpresa);
		
		Set<String> tipoOrigenFondos = sql.query(con, SQL_SELECT_TIPO_ORIGEN_FONDO, parametros, TipoOrigenFondo.class)
												.stream()
												.map(t -> t.IdTipoOrigenFondo)
												.collect(Collectors.toSet());
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarDeclaracionOrigenFondos.obtenerTipoOrigenFondo", fin - inicio);
		return tipoOrigenFondos;
	}
	
	// --------------------------------------------------
		// METODO PARA PRUEBAS LOCALES
		// --------------------------------------------------	
		public static void main(String[] args) throws Exception {
			System.out.println("== TEST ==");
			
			try (Connection con = PlataformaUtils.getDESAConnection()) {
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"PLA\",\"p_IdDeclaracionOrigenFondos\":10155,\"p_RegistroCambio\":[{\"CampoModificado\":\"FechaDeclaracion\",\"ValorNuevo\":\"2025-05-12\"},{\"CampoModificado\":\"Comentario\",\"ValorNuevo\":\"ASDASDASDASD\"},{\"CampoModificado\":\"HER\",\"ValorNuevo\":543},{\"CampoModificado\":\"JDA\",\"ValorNuevo\":312},{\"CampoModificado\":\"VDP\",\"ValorNuevo\":123}]}";
				
				EntradaModificarDeclaracionOrigenFondos Objin = gson.fromJson(json, EntradaModificarDeclaracionOrigenFondos.class);
				
				ModificarDeclaracionOrigenFondos demo = new ModificarDeclaracionOrigenFondos();
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			SalidaModificarDeclaracionOrigenFondos ObjOut = demo.logica(con, Objin);
	 		//	con.commit();
	 			con.rollback();
	 			System.out.println(ObjOut.toString());

			}
			System.out.println("== END ==");
		}

}
