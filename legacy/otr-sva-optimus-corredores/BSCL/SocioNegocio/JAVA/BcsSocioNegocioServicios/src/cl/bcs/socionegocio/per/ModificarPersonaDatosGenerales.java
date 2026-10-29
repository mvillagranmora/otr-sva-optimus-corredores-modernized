package cl.bcs.socionegocio.per;

import java.sql.Connection;
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
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.EntradaConfirmarEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.InEntidad;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.SalidaConfirmarEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ValidarConyuge.EntradaConyuge;
import cl.bcs.socionegocio.per.ValidarConyuge.EntradaValidarConyuge;
import cl.bcs.socionegocio.per.ValidarConyuge.SalidaValidarConyuge;
import cl.bcs.socionegocio.per.pojo.DatosPersonaDatosGenerales;
import cl.bcs.socionegocio.per.pojo.RegistroCambio;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPersonaDatosGenerales implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ModificarPersonaDatosGenerales.class);
	
	private String SQL_SELECT_PERSONA 					= loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaDatosGenerales_SelectPersonaDatosGenerales.sql");
	public  String SQL_INSERT_LOG_GENERAR_DOCUMENTOS	= loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaDatosGenerales_InsertLogGenerarContratos.sql");
	
	private final String ID_CONCEPTO 			= "MODPER";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD 		= "PERSONA";
	
	public static class EntradaModificarPersonaDatosGenerales extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdPersona;
		public List<RegistroCambio> p_RegistroCambio;
		
		public EntradaModificarPersonaDatosGenerales() {
			this.p_RegistroCambio = new ArrayList<RegistroCambio>();
		}
	}
	
	public static class OutPersonaDatosGenerales extends BcsPojo {
		public String Nombres;
		public String ApellidoPaterno;
		public String ApellidoMaterno;
		public String IdEstado;
		public Long IdPersona;
		public String IdEstadoAutorizacion;
	}
	
	public static class SalidaModificarPersonaDatosGenerales extends BcsPojo {
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_REFID;
		public List<OutPersonaDatosGenerales> P_OUT_DATOSGENERALES;
		
		public SalidaModificarPersonaDatosGenerales(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_DATOSGENERALES = new ArrayList<OutPersonaDatosGenerales>();
		}
	}
		
	public static class DatosGenerales extends BcsPojo {
		public Long IdPersona;	
		public String NombreCompleto;
		public String DocumentoIdentidad;
		
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
		EntradaModificarPersonaDatosGenerales jin = gson.fromJson(in.toString(), EntradaModificarPersonaDatosGenerales.class);
		SalidaModificarPersonaDatosGenerales out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarPersonaDatosGenerales logica(Connection con, EntradaModificarPersonaDatosGenerales entrada) throws Exception {
		logger.info("INICIO::ModificarPersonaDatosGenerales");
		long inicioServicio = System.currentTimeMillis();
		SalidaModificarPersonaDatosGenerales out = new SalidaModificarPersonaDatosGenerales();
	
		
		List<Long> idSolicitudesCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
		Long idSolicitudCambio = idSolicitudesCambio.get(0);
		
		List<DatosPersonaDatosGenerales> personaDatosGenerales = obtenerDatosPersona(con,entrada);
		
		if (personaDatosGenerales.size() == 0){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		List<RegistroSolicitudCambio> registrosSolicitudesCambios =  obtenerRegistroSolicitudesCambio(con, idSolicitudCambio, personaDatosGenerales, entrada);
		
		if (entrada.p_RegistroCambio.isEmpty()){
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_REGISTRO_CAMBIO",null,con,entrada.p_IdEmpresa));
			return out;		
		}
		
		List<MensajeSalida> validacionConyuge = validarConyuge(con, entrada.p_IdEmpresa, entrada.p_IdUsuario, registrosSolicitudesCambios);

		CrearRegistroSolicitudCambio servicioCrearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
		EntradaCrearRegistroSolicitudCambio inCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();
		
		inCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio = registrosSolicitudesCambios;
		
		servicioCrearRegistroSolicitudCambio.logica(con, inCrearRegistroSolicitudCambio);
		
		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO);
		
		if("S".equals(requiereAutorizacion)) {
			out = crearEventoAutorizacion(con, entrada, personaDatosGenerales.parallelStream().findFirst().orElse(null));
		} else {
			out = confirmarModificarPersonaDatosGenerales(con, entrada);			
		}
		
		out.P_OUT_MENSAJES.addAll(validacionConyuge);
		out.P_OUT_ERRORESCONTADOR += MensajeSalida.getNumeroErrores(validacionConyuge);
		out.P_OUT_REFID = idSolicitudCambio;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPersonaDatosGenerales", finServicio - inicioServicio);
		return out;
	}
	
	private List<DatosPersonaDatosGenerales> obtenerDatosPersona(Connection con, EntradaModificarPersonaDatosGenerales entrada) throws Exception {
		logger.debug("INICIO::ModificarPersonaDatosGenerales.obtenerDatosPersona");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
			
		params.put("IdPersona", entrada.p_IdPersona);		
		List<DatosPersonaDatosGenerales> persona = sql.query(con, SQL_SELECT_PERSONA, params, DatosPersonaDatosGenerales.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaDatosGenerales.obtenerDatosPersona", fin - inicio);
		return persona;
	}
	
	private SalidaModificarPersonaDatosGenerales crearEventoAutorizacion(Connection con, EntradaModificarPersonaDatosGenerales entrada, DatosPersonaDatosGenerales personaDatosGenerales) throws Exception {
		logger.debug("INICIO::ModificarPersonaDatosGenerales.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaModificarPersonaDatosGenerales salidaModificarPersonaDatosGenerales = new SalidaModificarPersonaDatosGenerales();
		
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
		evento.NumeroOperacionConcepto = String.valueOf(personaDatosGenerales.IdPersona);
		evento.FechaOperacionConcepto = fechaSistema;
		evento.IdCliente = personaDatosGenerales.IdPersona;
		evento.Glosa = "Nombre Persona: " + personaDatosGenerales.NombreCompleto	+ " / Documento Identidad : " + personaDatosGenerales.DocumentoIdentidad + " / ";

		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = Arrays.asList(evento);
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaModificarPersonaDatosGenerales.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaModificarPersonaDatosGenerales.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaModificarPersonaDatosGenerales.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaModificarPersonaDatosGenerales.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaModificarPersonaDatosGenerales.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaDatosGenerales.crearEventoAutorizacion", fin - inicio);
		return salidaModificarPersonaDatosGenerales;	
	}
	
	private SalidaModificarPersonaDatosGenerales confirmarModificarPersonaDatosGenerales(Connection con, EntradaModificarPersonaDatosGenerales entrada) throws Exception {
		logger.debug("INICIO::ModificarPersonaDatosGenerales.confirmarPersonaDatosGenerales");
		long inicio 	= System.currentTimeMillis();
		EntradaConfirmarEjecutarCambiosPersona entradaConfirmarModificarPersonaDatosGeneralesMasivo = new EntradaConfirmarEjecutarCambiosPersona();
		InEntidad entidad = new InEntidad();
		entidad.IdEntidad = entrada.p_IdPersona;
		
		entradaConfirmarModificarPersonaDatosGeneralesMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfirmarModificarPersonaDatosGeneralesMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfirmarModificarPersonaDatosGeneralesMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaConfirmarModificarPersonaDatosGeneralesMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConfirmarModificarPersonaDatosGeneralesMasivo.p_Entidad.add(entidad);
		entradaConfirmarModificarPersonaDatosGeneralesMasivo.p_IdEstadoAutorizacion = "MOD";
			
		ConfirmarModificarPersonaDatosGeneralesMasivo confirmarModificarPersonaDatosGeneralesMasivo = new ConfirmarModificarPersonaDatosGeneralesMasivo();
		
		SalidaConfirmarEjecutarCambiosPersona salidaConfirmarModificarPersonaDatosGeneralesMasivo = confirmarModificarPersonaDatosGeneralesMasivo.logica(con, entradaConfirmarModificarPersonaDatosGeneralesMasivo);
		
		SalidaModificarPersonaDatosGenerales out = new SalidaModificarPersonaDatosGenerales();
		out.P_OUT_ERRORESCONTADOR = salidaConfirmarModificarPersonaDatosGeneralesMasivo.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaConfirmarModificarPersonaDatosGeneralesMasivo.P_OUT_MENSAJES;
		
		long fin 	= System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaDatosGenerales.confirmarPersonaDatosGenerales", fin - inicio);
		return out;
	}
	
	private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio(Connection con, Long idSolicitudCambio, List<DatosPersonaDatosGenerales> personasDatosGenerales, EntradaModificarPersonaDatosGenerales entrada) throws Exception{
		logger.trace("INICIO::ModificarPersonaDatosGenerales.obtenerRegistroSolicitudesCambio");
		long inicio 	= System.currentTimeMillis();
		List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<RegistroSolicitudCambio>();
		
		Map<Long, DatosPersonaDatosGenerales> datosGeneralesPorId = personasDatosGenerales
				.parallelStream()
				.collect(Collectors.toMap(personaDatosGenerales->personaDatosGenerales.IdPersona, personaDatosGenerales->personaDatosGenerales));
		
		for(RegistroCambio registroCambio : entrada.p_RegistroCambio) {
			DatosPersonaDatosGenerales personaDatosGenerales = datosGeneralesPorId.get(entrada.p_IdPersona);
			RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
			registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
			registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
			registroSolicitudCambio.IdEntidadOrigen = String.valueOf(entrada.p_IdPersona);
			registroSolicitudCambio.CampoModificado = registroCambio.CampoModificado;
			
			ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(personaDatosGenerales, DatosPersonaDatosGenerales.class, registroCambio.CampoModificado);
			
			registroSolicitudCambio.TipoDato = valorAntiguo.TipoDato;
			registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
			registroSolicitudCambio.ValorNuevo = registroCambio.ValorNuevo;
			registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
			
			registroSolicitudesCambio.add(registroSolicitudCambio);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPersonaDatosGenerales.obtenerRegistroSolicitudesCambio", fin - inicio);
		return registroSolicitudesCambio;
	}
	
	private List<MensajeSalida> validarConyuge(Connection con, Long idEmpresa, Long idUsuario, List<RegistroSolicitudCambio> registrosSolicitudesCambios) throws Exception{
		logger.debug("INICIO::ModificarPersonaDatosGenerales.validarConyuge");
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
		logger.debug("FIN::ModificarPersonaDatosGenerales.validarConyuge", fin - inicio);
		return salidaValidarConyuge.P_OUT_MENSAJES;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdPersona\":1790842,\"p_RegistroCambio\":[{\"CampoModificado\":\"IdPaisResidencia\",\"ValorNuevo\":223},{\"CampoModificado\":\"IdTransparenciaFiscal\",\"ValorNuevo\":\"US-PERSON-FACTA\"},{\"CampoModificado\":\"ResidenciaTributariaExtranjera\",\"ValorNuevo\":\"S\"}]}";
			EntradaModificarPersonaDatosGenerales Objin = gson.fromJson(json, EntradaModificarPersonaDatosGenerales.class);
			
			ModificarPersonaDatosGenerales demo = new ModificarPersonaDatosGenerales();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaModificarPersonaDatosGenerales ObjOut = demo.logica(con, Objin);
 				con.rollback();
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
