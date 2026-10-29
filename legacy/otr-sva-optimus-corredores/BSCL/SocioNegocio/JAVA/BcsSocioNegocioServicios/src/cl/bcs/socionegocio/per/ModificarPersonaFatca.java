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
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaFatcaMasivo.EntradaConfirmarModificarPersonaFatcaMasivo;
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaFatcaMasivo.InPersonaFatca;
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaFatcaMasivo.SalidaConfirmarModificarPersonaFatcaMasivo;
import cl.bcs.socionegocio.per.pojo.Fatca;
import cl.bcs.socionegocio.per.pojo.RegistroCambio;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPersonaFatca implements JavaCall{
	static final Logger logger = LogManager.getLogger(ModificarPersonaFatca.class);

	private String SQL_SELECT_FATCA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaFatca_SelectFatca.sql");
	public  String SQL_INSERT_LOG_GENERAR_DOCUMENTOS	= loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaFatca_InsertLogGenerarContratos.sql");

	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD 		= "FATCA";
	private final String ID_CONCEPTO 			= "MODFAT";
	
	public static class EntradaModificarPersonaFatca extends BcsPojo {
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public Long p_IdPersona;
		public List<RegistroCambio> p_RegistroCambio;
		
		public EntradaModificarPersonaFatca() {
			this.p_RegistroCambio = new ArrayList<RegistroCambio>();
		}
	}
	
	public static class FatcaPaisResidencia extends BcsPojo {
		public Long 		IdPersona;
		public Long 		IdPaisResidencia;
		public String 		TIN;
		public String 		IdCausaNoTin;
		public String 		Observacion;
	}
	
	public static class OutPersonaFatca extends BcsPojo {
		public Long IdPaisResidencia;
		public String TIN;
		public String IdCausaNoTin;
		public String Observacion;
		public String IdEstado;
		public Long IdPersona;
		public String IdEstadoAutorizacion;
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
		
	
	public static class SalidaModificarPersonaFatca extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;		
		public Long P_OUT_REFID;
		public List<OutPersonaFatca> P_OUT_FATCA;
		
		public SalidaModificarPersonaFatca(){
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_FATCA = new ArrayList<OutPersonaFatca>();			
		}
	}
	
	public static class DatosPersonaFatca extends BcsPojo {
		public Long IdPersona;
		public String NombreCompletoPersona;
		public java.sql.Timestamp FechaIngreso;
		public String IdEstado;
		public String IdClasificacionFatca;
		public String IdClasificacionEmpresaFatca;
		public String NumeroGIIN;
		public java.sql.Timestamp FechaFirmaFormulario;
		public String AutorizacionInformarOrganismo;
		public String ExcepcionResidente;
		public String ExcepcionNacimiento;
		public String ExcepcionDomicilio;
		public String ExcepcionTelefono;
		public String ExcepcionOrdenPermanente;
		public String ExcepcionFirma;
		public String ParticipacionSociedadesUSA;
		public String OrdenPermanente;
		
	}
		
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarPersonaFatca jin = gson.fromJson(in.toString(), EntradaModificarPersonaFatca.class);
		SalidaModificarPersonaFatca out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaModificarPersonaFatca logica(Connection con, EntradaModificarPersonaFatca entrada) throws Exception {
		logger.info("INICIO::ModificarPersonaFatca");
		long inicioServicio = System.currentTimeMillis();
		SalidaModificarPersonaFatca salida = new SalidaModificarPersonaFatca();
		
		List<Long> idSolicitudesCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
		Long idSolicitudCambio = idSolicitudesCambio.get(0);

		List<Fatca> personaFatca = obtenerDatosFatca( con, entrada);
		
		if (personaFatca.size() == 0){
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return salida;
		}
		
		List<RegistroSolicitudCambio> registrosSolicitudesCambios =  obtenerRegistroSolicitudesCambio(con, idSolicitudCambio, personaFatca, entrada);
		
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
			salida = crearEventoAutorizacion(con, entrada, personaFatca.parallelStream().findFirst().orElse(null));
		} else {
			salida = confirmarModificarPersonaFatca(con, entrada);
			if(salida.P_OUT_ERRORESCONTADOR==0 ) {
				List<EntidadLogGenerarContrato> params = new ArrayList<EntidadLogGenerarContrato>();
				
				List<Long> idSecuencias = Utils.getSecuencia(con, "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos", (long) entrada.p_RegistroCambio.size());
				String fechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
		    	for(RegistroSolicitudCambio solicitud: registrosSolicitudesCambios) {
					if(!solicitud.ValorAntiguo.equals(solicitud.ValorNuevo)) {
						EntidadLogGenerarContrato logGenerarContrato = new EntidadLogGenerarContrato();
						logGenerarContrato.Idlog = idSecuencias.remove(0);
						logGenerarContrato.CodigoFormulario = "FATCA_";
						logGenerarContrato.CampoBackend = solicitud.CampoModificado;
						logGenerarContrato.IdEmpresa = entrada.p_IdEmpresa;
						logGenerarContrato.IdCliente = 0L;
						logGenerarContrato.IdPersona = entrada.p_IdPersona;
						logGenerarContrato.IdUsuario = entrada.p_IdUsuario;
						logGenerarContrato.FechaSistema = fechaSistema;
						logGenerarContrato.IdPrograma = 0L;
						logGenerarContrato.Estado = "PEN";
						logGenerarContrato.Mensaje = "Persona - ModificarPersonaFatca.java";
						params.add(logGenerarContrato);
					}
				}
				SQL sql = new SQL();
				
				System.out.println(params);
				
				sql.batch(con, SQL_INSERT_LOG_GENERAR_DOCUMENTOS, params,EntidadLogGenerarContrato.class);
			}				
			
		}
		
		salida.P_OUT_REFID = idSolicitudCambio;
		
		salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPersonaFatca", finServicio - inicioServicio);
		return salida;
	}
	
	private List<Fatca> obtenerDatosFatca(Connection con, EntradaModificarPersonaFatca entrada) throws Exception{
		logger.debug("INICIO::ModificarPersonaFatca.obtenerDatosFatca");
		long inicio 	= System.currentTimeMillis();
		
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
	
		params.put("IdPersona", entrada.p_IdPersona);		
		List<Fatca> fatcas = sql.query(con, SQL_SELECT_FATCA, params, Fatca.class);
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPersonaFatca.obtenerDatosFatca", fin - inicio);
		return fatcas;
	}
	
	private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio (Connection con, Long idSolicitudCambio, List<Fatca> personasFatca, EntradaModificarPersonaFatca entrada) throws Exception{
		logger.trace("INICIO::ModificarPersonaFatca.obtenerRegistroSolicitudesCambio");
		long inicio 	= System.currentTimeMillis();
		List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<RegistroSolicitudCambio>();
		
		Map<Long, Fatca> fatcaPorIdPersona = personasFatca
				.parallelStream()
				.collect(Collectors.toMap(personaFatca->personaFatca.IdPersona, personaFatca->personaFatca));
		
		for(RegistroCambio registroCambio : entrada.p_RegistroCambio) {
			Fatca personaFatca = fatcaPorIdPersona.get(entrada.p_IdPersona);
			RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
			registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
			registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
			registroSolicitudCambio.IdEntidadOrigen = String.valueOf(entrada.p_IdPersona);
			registroSolicitudCambio.CampoModificado = registroCambio.CampoModificado;

			ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(personaFatca, Fatca.class, registroCambio.CampoModificado);
			
			registroSolicitudCambio.TipoDato = valorAntiguo.TipoDato;
			registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
			registroSolicitudCambio.ValorNuevo = registroCambio.ValorNuevo;
			registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
			
			registroSolicitudesCambio.add(registroSolicitudCambio);
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPersonaFatca.obtenerRegistroSolicitudesCambio", fin - inicio);
		return registroSolicitudesCambio;
	}
	
	private SalidaModificarPersonaFatca crearEventoAutorizacion(Connection con, EntradaModificarPersonaFatca entrada, Fatca personaFatca) throws Exception {
		logger.debug("INICIO::ModificarPersonaFatca.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaModificarPersonaFatca salidaModificarPersonaFatca = new SalidaModificarPersonaFatca();
		
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
		evento.NumeroOperacionConcepto = String.valueOf(personaFatca.IdPersona);
		evento.FechaOperacionConcepto = fechaSistema;
		evento.IdCliente = personaFatca.IdPersona;
		evento.Glosa = "Nombre Persona: " + personaFatca.NombreCompleto	+ " / MODIFICAR FATCA: "+ personaFatca.IdPersona +" / ";

		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS = Arrays.asList(evento);
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaModificarPersonaFatca.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaModificarPersonaFatca.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaModificarPersonaFatca.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaModificarPersonaFatca.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaModificarPersonaFatca.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaFatca.crearEventoAutorizacion", fin - inicio);
		return salidaModificarPersonaFatca;	
	}

	private SalidaModificarPersonaFatca confirmarModificarPersonaFatca(Connection con, EntradaModificarPersonaFatca entrada) throws Exception {
		logger.debug("INICIO::ModificarPersonaFatca.confirmarModificarPersonaFatca");
		long inicio 	= System.currentTimeMillis();
		EntradaConfirmarModificarPersonaFatcaMasivo entradaConfirmarModificarPersonaFatcaMasivo = new EntradaConfirmarModificarPersonaFatcaMasivo();
		InPersonaFatca entradaPersonaFatca 	= new InPersonaFatca(entrada.p_IdPersona);
		entradaPersonaFatca.IdPersona 					= entrada.p_IdPersona;
		
		entradaConfirmarModificarPersonaFatcaMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfirmarModificarPersonaFatcaMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfirmarModificarPersonaFatcaMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaConfirmarModificarPersonaFatcaMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConfirmarModificarPersonaFatcaMasivo.p_PersonaFatca.add(entradaPersonaFatca);
		entradaConfirmarModificarPersonaFatcaMasivo.p_IdEstadoAutorizacion = "MOD";
			
		ConfirmarModificarPersonaFatcaMasivo confirmarModificarPersonaFatcaMasivo = new ConfirmarModificarPersonaFatcaMasivo();
		
		SalidaConfirmarModificarPersonaFatcaMasivo salidaConfirmarModificarPersonaFatcaMasivo = confirmarModificarPersonaFatcaMasivo.logica(con, entradaConfirmarModificarPersonaFatcaMasivo);
		
		SalidaModificarPersonaFatca salida = new SalidaModificarPersonaFatca();
		salida.P_OUT_ERRORESCONTADOR = salidaConfirmarModificarPersonaFatcaMasivo.P_OUT_ERRORESCONTADOR;
		salida.P_OUT_MENSAJES = salidaConfirmarModificarPersonaFatcaMasivo.P_OUT_MENSAJES;
		
		long fin 	= System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaFatca.confirmarModificarPersonaFatca", fin - inicio);
		return salida;
	}
	
	// --------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// --------------------------------------------------	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"N\",\"p_IdPersona\":1790842,\"p_RegistroCambio\":[{\"CampoModificado\":\"AutorizacionInformarOrganismo\",\"ValorNuevo\":\"N\"}]}";
					
			EntradaModificarPersonaFatca Objin = gson.fromJson(json, EntradaModificarPersonaFatca.class);
			
			ModificarPersonaFatca demo = new ModificarPersonaFatca();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 		
 			try {
 				SalidaModificarPersonaFatca ObjOut = demo.logica(con, Objin);
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
