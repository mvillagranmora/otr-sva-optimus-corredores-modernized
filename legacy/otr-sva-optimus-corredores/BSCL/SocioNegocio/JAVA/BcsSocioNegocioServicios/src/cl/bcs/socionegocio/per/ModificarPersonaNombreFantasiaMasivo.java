package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.InEntidad;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.pojo.Entidad;
import cl.bcs.socionegocio.per.pojo.NombreFantasia;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPersonaNombreFantasiaMasivo implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ModificarPersonaNombreFantasiaMasivo.class);
	private final String SQL_SELECT_NOMBRE_FANTASIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaNombreFantasiaMasivo_SelectNombreFantasia.sql");
	private final String ID_CONCEPTO = "NUENFA";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD = "NOMBREFANTASIA";

	public static class EntradaModificarPersonaNombreFantasiaMasivo extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public List<NombreFantasia> p_NombresFantasia;
	}
	
	public static class SalidaModificarPersonaNombreFantasiaMasivo extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public List<Entidad> P_OUT_REFID;
		
		public SalidaModificarPersonaNombreFantasiaMasivo() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<>();
			this.P_OUT_REFID = new ArrayList<>();
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEjecutarCambiosPersona jin = gson.fromJson(in.toString(), EntradaEjecutarCambiosPersona.class);
		SalidaEjecutarCambiosPersona out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	public SalidaEjecutarCambiosPersona logica(Connection con,EntradaEjecutarCambiosPersona entrada) throws Exception {
		logger.info("INICIO::ModificarPersonaNombreFantasiaMasivo");
		long inicioServicio = System.currentTimeMillis();
		
		SalidaEjecutarCambiosPersona out = new SalidaEjecutarCambiosPersona();

		if (entrada.p_RegistroCambio.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "FALTA_ESPECIFICAR_REGISTRO_CAMBIO", new HashMap<String, String>(), con, entrada.p_IdEmpresa));
			return out;		
		}
		
		List<NombreFantasia> nombresFantasia = obtenerNombresFantasia(con, entrada.p_RegistroCambio);
		
		if (nombresFantasia.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		Long idSolicitudCambio = CrearRegistroSolicitudCambio.obtenerIdSolicitudCambio(con);
		
		List<RegistroSolicitudCambio> registrosSolicitudesCambios =  obtenerRegistroSolicitudesCambio(con, idSolicitudCambio, nombresFantasia, entrada);
		
		crearSolicitudCambio(con, registrosSolicitudesCambios);
		
		Boolean requiereAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
		
		if(requiereAutorizacion) {
			out = crearEventoAutorizacion(con, entrada, nombresFantasia);
		} else {
			List<InEntidad> lstInEntidad = obtenerListaPersonaEntidad(nombresFantasia);		
			ConfirmarCambiosPersonaEntidad confirmarCambiosEntidad = new ConfirmarCambiosPersonaEntidad();
			out = confirmarCambiosEntidad.logica(con, entrada, lstInEntidad, new ConfirmarModificarPersonaNombreFantasiaMasivo());
		}
		
		out.P_OUT_REFID = idSolicitudCambio;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPersonaNombreFantasiaMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private void crearSolicitudCambio(Connection con, List<RegistroSolicitudCambio> registrosSolicitudesCambios) throws Exception {
		logger.debug("INICIO::ModificarPersonaNombreFantasiaMasivo.crearSolicitudCambio");
		long inicio = System.currentTimeMillis();
		
		CrearRegistroSolicitudCambio servicioCrearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
		EntradaCrearRegistroSolicitudCambio entradaCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();
		
		entradaCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio = registrosSolicitudesCambios;
		
		servicioCrearRegistroSolicitudCambio.logica(con, entradaCrearRegistroSolicitudCambio);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaNombreFantasiaMasivo.crearSolicitudCambio", fin - inicio);	
	}

	private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio(Connection con, Long idSolicitudCambio,List<NombreFantasia> nombresFantasia, EntradaEjecutarCambiosPersona entrada) throws Exception {
		logger.debug("INICIO::ModificarPersonaNombreFantasiaMasivo.obtenerRegistroSolicitudesCambio");
		long inicio = System.currentTimeMillis();
		
		List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<>();
		
		Map<Long, NombreFantasia> nombresFantasiaPorId = nombresFantasia
				.parallelStream()
				.collect(Collectors.toMap(nombreFantasia->nombreFantasia.IdNombreFantasia, nombreFantasia->nombreFantasia));
		
		for(RegistroCambioMasivo registroCambio : entrada.p_RegistroCambio) {
			
			NombreFantasia nombreFantasia = nombresFantasiaPorId.get(registroCambio.IdEntidad);
			
			if( nombreFantasia != null ) {
				RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
				registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
				registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
				registroSolicitudCambio.IdEntidadOrigen = String.valueOf(registroCambio.IdEntidad);
				registroSolicitudCambio.CampoModificado = registroCambio.CampoModificado;
				
				ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(nombreFantasia, NombreFantasia.class, registroCambio.CampoModificado);
				
				registroSolicitudCambio.TipoDato = valorAntiguo.TipoDato;
				registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
				registroSolicitudCambio.ValorNuevo = registroCambio.ValorNuevo;
				registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
				
				registroSolicitudesCambio.add(registroSolicitudCambio);
			}
			
		}

		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPersonaNombreFantasiaMasivo.obtenerRegistroSolicitudesCambio", fin - inicio);
		return registroSolicitudesCambio;	
	}

	private List<NombreFantasia> obtenerNombresFantasia(Connection con, List<RegistroCambioMasivo> registroCambio) throws SQLException {
		logger.debug("INICIO::ModificarPersonaNombreFantasiaMasivo.obtenerNombresFantasia");
		long inicio = System.currentTimeMillis();
		
		List<Long> idsNombreFantasia = registroCambio.stream()
				  .map(nombreFantasia -> nombreFantasia.IdEntidad)
				  .distinct()
				  .collect(Collectors.toList());
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("IdNombreFantasia", idsNombreFantasia);
		
		SQL sql = new SQL();
		List<NombreFantasia> nombresFantasia = sql.query(con, SQL_SELECT_NOMBRE_FANTASIA, params, NombreFantasia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaNombreFantasiaMasivo.obtenerNombresFantasia", fin - inicio);
		return nombresFantasia;
	}

	private List<InEntidad> obtenerListaPersonaEntidad(List<NombreFantasia> nombresFantasia) {
		logger.trace("INICIO::ModificarPersonaNombreFantasiaMasivo.obtenerListaPersonaEntidad");
		long inicio = System.currentTimeMillis();		
		List<InEntidad> lstInEntidad = nombresFantasia.stream()
				  .map(entidad -> new InEntidad(entidad.IdPersona, entidad.IdNombreFantasia))
				  .distinct()
				  .collect(Collectors.toList());	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPersonaNombreFantasiaMasivo.obtenerListaPersonaEntidad", fin - inicio);
		return lstInEntidad;
	}

	private SalidaEjecutarCambiosPersona crearEventoAutorizacion(Connection con, EntradaEjecutarCambiosPersona entrada, List<NombreFantasia> nombresFantasia) throws Exception {
		logger.debug("INICIO::ModificarPersonaNombreFantasiaMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		List<Long> idsPersona = nombresFantasia
				.parallelStream()
				.map(nombreFantasia->nombreFantasia.IdPersona)
				.collect(Collectors.toList());
		Map<Long, SocioNegocioUtils.Persona> personasPorIdPersona = SocioNegocioUtils.obtenerDatosPersonaPorIdPersona(con, idsPersona);
		
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		
		for (NombreFantasia nombreFantasia : nombresFantasia) {
			SocioNegocioUtils.Persona persona = personasPorIdPersona.get(nombreFantasia.IdPersona);
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento 			= ID_CONCEPTO;
			evento.IdModuloSolicita 		= entrada.p_IdModulo;
			evento.IdUsuarioSolicita 		= entrada.p_IdUsuario;
			evento.Fecha 					= fechaSistema;
			evento.IdConcepto 				= ID_CONCEPTO;
			evento.NumeroOperacionConcepto 	= String.valueOf(nombreFantasia.IdNombreFantasia);
			evento.FechaOperacionConcepto 	= fechaSistema;
			evento.IdCliente 				= nombreFantasia.IdPersona;
			evento.Glosa = persona.DocumentoIdentidad + " / " + persona.NombreCompleto + " /NombreFantasia: " + nombreFantasia.DescripcionNombreFantasia;
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
		logger.debug("FIN::ModificarPersonaNombreFantasiaMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaEjecutarCambiosPersona;
	}
	
	public static void main(String[] args) throws SQLException {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdUsuario\": 9,\r\n"
					+ "  \"p_IdModulo\": \"SOC\",\r\n"
					+ "  \"p_UsaReglasNegocio\": \"S\",\r\n"
					+ "  \"p_RegistroCambio\": [\r\n"
					+ "    { \"CampoModificado\": \"DescripcionNombreFantasia\", \"ValorNuevo\": \"PRUEBABABABABXDDDDDD\", \"IdEntidad\": 10072 }\r\n"
					+ "  ]\r\n"
					+ "}\r\n"
					+ "";
			EntradaEjecutarCambiosPersona Objin = gson.fromJson(json, EntradaEjecutarCambiosPersona.class);
			
			ModificarPersonaNombreFantasiaMasivo demo = new ModificarPersonaNombreFantasiaMasivo();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
 			
			try {
				SalidaEjecutarCambiosPersona salida= demo.logica(con, Objin);
				con.commit();
				//con.rollback();
				con.close();
				System.out.println(salida.toString());
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
				con.close();
			}		
		}
		System.out.println("== END ==");
	}
}
