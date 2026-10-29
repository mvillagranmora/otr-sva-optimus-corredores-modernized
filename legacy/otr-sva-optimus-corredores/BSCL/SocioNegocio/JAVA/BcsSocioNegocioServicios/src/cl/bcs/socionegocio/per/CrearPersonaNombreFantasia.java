package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ConfirmarCrearPersonaNombreFantasia.EntradaConfirmarCrearPersonaNombreFantasia;
import cl.bcs.socionegocio.per.ConfirmarCrearPersonaNombreFantasia.SalidaConfirmarCrearPersonaNombreFantasia;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearPersonaNombreFantasia implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CrearPersonaNombreFantasia.class);
	
	private final String SQL_INSERT_NOMBRE_FANTASIA = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaNombreFantasia_InsertNombreFantasia.sql");
	private final String SQL_SELECT_DATOS_PERSONA = PlataformaUtils.loadResourceToString("cl/bcs/socionegocio/per/sql/CrearPersonaNombreFantasia_SelectDatosPersona.sql");
	
	private final String ID_CONCEPTO = "NUENFA";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	
	public static class EntradaCrearPersonaNombreFantasia extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdPersona;
		public String p_NombreFantasia;
		public String p_IdEstado;
	}
	
	public static class SalidaCrearPersonaNombreFantasia extends BcsPojo{
		public Long P_OUT_REFID;
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
	}
	
	public static class NombreFantasia extends BcsPojo{
		public Long IdNombreFantasia;
		public String DescripcionNombreFantasia;
		public Long IdPersona;
		public java.sql.Timestamp FechaIngreso;
		public String IdEstado;
		public String IdEstadoAutorizacion;
		public Long IdUsuarioCreador;
	}
	
	public static class Persona extends BcsPojo{
		public String DocumentoIdentidad;
		public String NombreCompleto;
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaCrearPersonaNombreFantasia jin = gson.fromJson(in.toString(), EntradaCrearPersonaNombreFantasia.class);
		SalidaCrearPersonaNombreFantasia out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCrearPersonaNombreFantasia logica(Connection con, EntradaCrearPersonaNombreFantasia entrada) throws Exception {
		logger.info("INICIO::CrearPersonaNombreFantasia");
		long inicioServicio = System.currentTimeMillis();
		SalidaCrearPersonaNombreFantasia out = new SalidaCrearPersonaNombreFantasia();
		
		Long idNombreFantasia = insertarNombreFantasia(con, entrada);
		
		String requiereAutorizacion = obtenerRequiereAutorizacion(con, entrada.p_IdEmpresa);

		// SP: if v_RequiereAutorizacion ='S' and p_UsaReglasNegocio != 'N' then
		if("S".equals(requiereAutorizacion) && !"N".equals(entrada.p_UsaReglasNegocio)) {
			out = crearEventoAutorizacion(con, entrada, idNombreFantasia);
		} else {
			out = confirmarNombreFantasia(con, entrada, idNombreFantasia);
		}
		
		out.P_OUT_REFID = idNombreFantasia;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::CrearPersonaNombreFantasia", finServicio - inicioServicio);
		return out;
	}
	
	private Long insertarNombreFantasia(Connection con, EntradaCrearPersonaNombreFantasia entrada) throws Exception {
		logger.debug("INICIO::CrearPersonaNombreFantasia.insertarNombreFantasia");
		long inicio = System.currentTimeMillis();
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaLocal(con, entrada.p_IdEmpresa);
		
		List<Long> secuenciasNombreFantasia = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::NombreFantasia", 1L);
		Long idNombreFantasia = secuenciasNombreFantasia.get(0);
		
		NombreFantasia nombreFantasia = new NombreFantasia();
		nombreFantasia.IdNombreFantasia = idNombreFantasia;
		nombreFantasia.DescripcionNombreFantasia = entrada.p_NombreFantasia;
		nombreFantasia.IdPersona = entrada.p_IdPersona;
		nombreFantasia.FechaIngreso = Fecha.toDateTime(fechaHoraSistema);
		nombreFantasia.IdEstado = entrada.p_IdEstado;
		nombreFantasia.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
		nombreFantasia.IdUsuarioCreador = entrada.p_IdUsuario;
		
		List<NombreFantasia> nombresFantasia = new ArrayList<NombreFantasia>();
		nombresFantasia.add(nombreFantasia);
		
		SQL sql = new SQL();
		sql.batch(con, SQL_INSERT_NOMBRE_FANTASIA, nombresFantasia, NombreFantasia.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaNombreFantasia.insertarNombreFantasia", fin - inicio);
		return idNombreFantasia;
	}
	
	private String obtenerRequiereAutorizacion(Connection con, Long idEmpresa) throws Exception {
		logger.debug("INICIO::CrearPersonaNombreFantasia.obtenerRequiereAutorizacion");
		long inicio = System.currentTimeMillis();
		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, idEmpresa, ID_CONCEPTO);
		if(requiereAutorizacion == null) requiereAutorizacion = "N";
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaNombreFantasia.obtenerRequiereAutorizacion", fin - inicio);
		return requiereAutorizacion;
	}
	
	private SalidaCrearPersonaNombreFantasia crearEventoAutorizacion(Connection con, EntradaCrearPersonaNombreFantasia entrada, Long idNombreFantasia) throws Exception {
		logger.debug("INICIO::CrearPersonaNombreFantasia.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		Persona datosPersona = obtenerDatosPersona(con, entrada.p_IdPersona);
		SalidaCrearPersonaNombreFantasia salidaCrearPersonaNombreFantasia = new SalidaCrearPersonaNombreFantasia();
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
		evento.NumeroOperacionConcepto = String.valueOf(idNombreFantasia);
		evento.FechaOperacionConcepto = fechaSistema;
		evento.IdCliente = entrada.p_IdPersona;
		evento.Glosa = datosPersona.DocumentoIdentidad + " / " + datosPersona.NombreCompleto + " / NombreFantasia: " + entrada.p_NombreFantasia;

		List<CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS> eventos = new ArrayList<CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS>();
		eventos.add(evento);
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = eventos;
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaCrearPersonaNombreFantasia.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaCrearPersonaNombreFantasia.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaCrearPersonaNombreFantasia.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaCrearPersonaNombreFantasia.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaCrearPersonaNombreFantasia.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaNombreFantasia.crearEventoAutorizacion", fin - inicio);
		return salidaCrearPersonaNombreFantasia;	
	}
	
	private Persona obtenerDatosPersona(Connection con, Long idPersona) throws Exception {
		logger.debug("INICIO::CrearPersonaNombreFantasia.obtenerDatosPersona");
		long inicio = System.currentTimeMillis();
		HashMap<String,Object> parametros = new HashMap<String,Object>(1);
		parametros.put("IdPersona", idPersona);
		SQL sql = new SQL();
		List<Persona> personas = sql.query(con, SQL_SELECT_DATOS_PERSONA, parametros, Persona.class);
		Persona persona = personas.parallelStream().findFirst().orElse(new Persona());
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaNombreFantasia.obtenerDatosPersona", fin - inicio);
		return persona;
	}
	
	private SalidaCrearPersonaNombreFantasia confirmarNombreFantasia(Connection con, EntradaCrearPersonaNombreFantasia entrada, Long idNombreFantasia) throws Exception {
		logger.debug("INICIO::CrearPersonaNombreFantasia.obtenerDatosPersona");
		long inicio = System.currentTimeMillis();
		EntradaConfirmarCrearPersonaNombreFantasia entradaConfirmarCrearPersonaNombreFantasia = new EntradaConfirmarCrearPersonaNombreFantasia();
		entradaConfirmarCrearPersonaNombreFantasia.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfirmarCrearPersonaNombreFantasia.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfirmarCrearPersonaNombreFantasia.p_IdModulo = entrada.p_IdModulo;
		entradaConfirmarCrearPersonaNombreFantasia.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConfirmarCrearPersonaNombreFantasia.p_IdNombreFantasia = idNombreFantasia;
		entradaConfirmarCrearPersonaNombreFantasia.p_IdEstadoAutorizacion = "ING";
		
		ConfirmarCrearPersonaNombreFantasia confirmarCrearPersonaNombreFantasia = new ConfirmarCrearPersonaNombreFantasia();
		SalidaConfirmarCrearPersonaNombreFantasia salidaConfirmarCrearPersonaNombreFantasia = confirmarCrearPersonaNombreFantasia.logica(con, entradaConfirmarCrearPersonaNombreFantasia);
		
		SalidaCrearPersonaNombreFantasia out = new SalidaCrearPersonaNombreFantasia();
		out.P_OUT_ERRORESCONTADOR = salidaConfirmarCrearPersonaNombreFantasia.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaConfirmarCrearPersonaNombreFantasia.P_OUT_MENSAJES;
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CrearPersonaNombreFantasia.obtenerDatosPersona", fin - inicio);
		return out;
	}

	public static void main(String[] args) throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN ==");
			Gson gson = Utils.getGsonBuilder();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPersona\":41162,\"p_NombreFantasia\":\"TEST NOMBRE FANTASIA\",\"p_IdEstado\":\"ACT\"}";
			EntradaCrearPersonaNombreFantasia entrada = gson.fromJson(json, EntradaCrearPersonaNombreFantasia.class);

			CrearPersonaNombreFantasia servicio = new CrearPersonaNombreFantasia();
			SalidaCrearPersonaNombreFantasia salida = servicio.logica(conn, entrada);
			System.out.println(salida.toString());

			conn.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("== END ==");
	}

}
