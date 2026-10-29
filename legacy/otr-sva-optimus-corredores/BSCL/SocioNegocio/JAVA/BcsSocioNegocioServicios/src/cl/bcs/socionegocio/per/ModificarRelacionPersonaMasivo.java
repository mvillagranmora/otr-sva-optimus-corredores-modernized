package cl.bcs.socionegocio.per;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarRelacionPersonaMasivo implements JavaCall, EjecutarCambiosPersona {

static final Logger logger = LogManager.getLogger(ModificarRelacionPersonaMasivo.class);
	
	private String SQL_SELECT_RELACION_PERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarRelacionPersonaMasivo_SelectRelacionPersona.sql");
	
	private final String ID_CONCEPTO 			= "MODREL";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD 		= "RELACION-PERSONA";
	
	public static class RelacionPersona extends BcsPojo {
		public Long 		IdRelacionPersona;
		public String 		IdTipoRelacion;
		public Long 		IdPersonaDestino;
		public String 		NombreCompleto;
		public String 		DocumentoIdentidad;		
		public Timestamp 	FechaInicioRelacion;
		public Timestamp 	FechaFinRelacion;
		public String 		IdEstado;
		public BigDecimal	IdCiudad;
		public String 		Ciudad;
		public BigDecimal 	PorcentajeParticipacion;
		public String 		RetiraDocumentos;
		public String 		FinalidadRelacion;
		public String 		CondicionesRelacion;
		public String 		Calle;
		public String 		Numero;
		public String 		Email;
		public String 		Telefono;
		public String 		Complemento;
		//wbs
		public String		EstaInsertada;
		public String 		NombresPersonaRelacion;
		public String 		ApellidoPaternoPersonaRelacion;
		public String 		ApellidoMaternoPersonaRelacion;
		public String 		DocumentoIdentidadRelacion;
		public String 		TipoDocumentoIdentidadRelacion;
		public RelacionPersona() {
			this.IdRelacionPersona = 0L;
			this.NombresPersonaRelacion = "";
			this.ApellidoPaternoPersonaRelacion = "";
			this.ApellidoMaternoPersonaRelacion = "";
			this.TipoDocumentoIdentidadRelacion = "R";
			this.EstaInsertada = "N";
		}

	}

	// --------------------------------------------------
	// CALL
	// --------------------------------------------------	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaEjecutarCambiosPersona jin = gson.fromJson(in.toString(), EntradaEjecutarCambiosPersona.class);
		SalidaEjecutarCambiosPersona out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	// --------------------------------------------------
	// LOGICA
	// --------------------------------------------------
	@Override
	public SalidaEjecutarCambiosPersona logica(Connection con, EntradaEjecutarCambiosPersona entrada) throws Exception {
		logger.info("INICIO::ModificarRelacionPersonaMasivo");
		long inicioServicio = System.currentTimeMillis();
		SalidaEjecutarCambiosPersona out = new SalidaEjecutarCambiosPersona();
		
		List<Long> lstIdSolicitudCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
		Long idSolicitudCambio = lstIdSolicitudCambio.get(0);

		List<RelacionPersona> lstRelacionPersona = obtenerDatosRelacionPersona(con, entrada);
		
		if (lstRelacionPersona.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		List<RegistroSolicitudCambio> registrosSolicitudesCambios = obtenerRegistroSolicitudesCambio(con, idSolicitudCambio, lstRelacionPersona, entrada);
		
		if (entrada.p_RegistroCambio.isEmpty()) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_REGISTRO_CAMBIO",null,con,entrada.p_IdEmpresa));
			return out;		
		}
		
		CrearRegistroSolicitudCambio servicioCrearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
		EntradaCrearRegistroSolicitudCambio inCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();
		
		inCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio = registrosSolicitudesCambios;
		
		servicioCrearRegistroSolicitudCambio.logica(con, inCrearRegistroSolicitudCambio);
		
		Boolean requiereAutorizacion = SocioNegocioUtils.esOperacionConAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO, entrada.p_UsaReglasNegocio);
				
		if(requiereAutorizacion) {
			out = crearEventoAutorizacion(con, entrada, lstRelacionPersona);
		} else {
			List<InEntidad> lstInEntidad = obtenerListaPersonaEntidad(lstRelacionPersona);
			ConfirmarCambiosPersonaEntidad confirmarCambiosEntidad = new ConfirmarCambiosPersonaEntidad();
			out = confirmarCambiosEntidad.logica(con, entrada, lstInEntidad, new ConfirmarModificarRelacionPersonaMasivo());
		}
		
		out.P_OUT_REFID = idSolicitudCambio;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarRelacionPersonaMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<InEntidad> obtenerListaPersonaEntidad(List<RelacionPersona> lstRelacionPersona) {
		logger.trace("INICIO::ModificarRelacionPersonaMasivo.obtenerListaPersonaEntidad");
		long inicio = System.currentTimeMillis();		
		List<InEntidad> lstInEntidad = lstRelacionPersona.stream()
				  .map(entidad -> new InEntidad(entidad.IdPersonaDestino,entidad.IdRelacionPersona))
				  .distinct()
				  .collect(Collectors.toList());	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarRelacionPersonaMasivo.obtenerListaPersonaEntidad", fin - inicio);
		return lstInEntidad;
	}

	private List<RelacionPersona> obtenerDatosRelacionPersona(Connection con, EntradaEjecutarCambiosPersona entrada) throws Exception {
		logger.debug("INICIO::ModificarRelacionPersonaMasivo.obtenerDatosRelacionPersona");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		List<Long> idEntidades = entrada.p_RegistroCambio.stream()
														 .map(entidad -> entidad.IdEntidad)
														 .distinct()
														 .collect(Collectors.toList());
		params.put("IdRelacionPersona", idEntidades);		
		List<RelacionPersona> listaRelacionPersona = sql.query(con, SQL_SELECT_RELACION_PERSONA, params, RelacionPersona.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarRelacionPersonaMasivo.obtenerDatosRelacionPersona", fin - inicio);
		return listaRelacionPersona;
	}
	
	private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio(Connection con, Long idSolicitudCambio, List<RelacionPersona> lstEntidad, EntradaEjecutarCambiosPersona entrada) throws Exception{
		logger.trace("INICIO::ModificarRelacionPersonaMasivo.obtenerRegistroSolicitudesCambio");
		long inicio 	= System.currentTimeMillis();
		List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<RegistroSolicitudCambio>();
		
		Map<Long, RelacionPersona> entidadesPorId = lstEntidad
				.parallelStream()
				.collect(Collectors.toMap(entidad->entidad.IdRelacionPersona, entidad->entidad));
		
		for(RegistroCambioMasivo registroCambio : entrada.p_RegistroCambio) {
			RelacionPersona entidadesId = entidadesPorId.get(registroCambio.IdEntidad);
			
			if(entidadesId != null) {
				RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
				registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
				registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
				registroSolicitudCambio.IdEntidadOrigen = String.valueOf(registroCambio.IdEntidad);
				registroSolicitudCambio.CampoModificado = registroCambio.CampoModificado;
				
				ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(entidadesId, RelacionPersona.class, registroCambio.CampoModificado);
				
				registroSolicitudCambio.TipoDato = valorAntiguo.TipoDato;
				registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
				registroSolicitudCambio.ValorNuevo = registroCambio.ValorNuevo;
				registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
				
				registroSolicitudesCambio.add(registroSolicitudCambio);
			}
			
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarRelacionPersonaMasivo.obtenerRegistroSolicitudesCambio", fin - inicio);
		return registroSolicitudesCambio;
	}	
	
	private SalidaEjecutarCambiosPersona crearEventoAutorizacion(Connection con, EntradaEjecutarCambiosPersona entrada, List<RelacionPersona> lstEntidad) throws Exception {
		logger.debug("INICIO::ModificarRelacionPersonaMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		
		for (RelacionPersona entidad : lstEntidad) {
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento 			= ID_CONCEPTO;
			evento.IdModuloSolicita 		= entrada.p_IdModulo;
			evento.IdUsuarioSolicita 		= entrada.p_IdUsuario;
			evento.Fecha 					= fechaSistema;
			evento.IdConcepto 				= ID_CONCEPTO;
			evento.NumeroOperacionConcepto 	= String.valueOf(entidad.IdRelacionPersona); 
			evento.FechaOperacionConcepto 	= fechaSistema;
			evento.IdCliente 				= entidad.IdPersonaDestino;
			evento.Glosa = entidad.DocumentoIdentidad + " / "  + entidad.NombreCompleto + " / " + " / IdRel: " +  entidad.IdRelacionPersona;
			entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS.add(evento);			
			
		}

		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		SalidaEjecutarCambiosPersona salidaModificarRelacionPersonaMasivo = new SalidaEjecutarCambiosPersona();
		salidaModificarRelacionPersonaMasivo.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaModificarRelacionPersonaMasivo.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaModificarRelacionPersonaMasivo.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaModificarRelacionPersonaMasivo.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaModificarRelacionPersonaMasivo.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarRelacionPersonaMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaModificarRelacionPersonaMasivo;	
	}
	
	
	public static void main(String[] args) throws SQLException {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_RegistroCambio\":[{\"CampoModificado\":\"FechaInicioRelacion\",\"ValorNuevo\":\"2016-05-25 00:00:00\",\"IdEntidad\":51}, {\"CampoModificado\":\"FechaInicioRelacion\",\"ValorNuevo\":\"2016-05-25 04:00:00\",\"IdEntidad\":52}]}";
			EntradaEjecutarCambiosPersona Objin = gson.fromJson(json, EntradaEjecutarCambiosPersona.class);
			
			ModificarRelacionPersonaMasivo demo = new ModificarRelacionPersonaMasivo();
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
