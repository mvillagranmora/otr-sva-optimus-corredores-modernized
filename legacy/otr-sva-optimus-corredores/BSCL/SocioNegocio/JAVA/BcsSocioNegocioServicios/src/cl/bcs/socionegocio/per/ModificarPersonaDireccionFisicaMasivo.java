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
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.socionegocio.soc.SocioNegocioUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarPersonaDireccionFisicaMasivo implements JavaCall, EjecutarCambiosPersona {

	static final Logger logger = LogManager.getLogger(ModificarPersonaDireccionFisicaMasivo.class);
	
	private String SQL_SELECT_DIRECCION_FISICA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaDireccionFisicaMasivo_SelectDireccionFisica.sql");	
	
	private final String ID_CONCEPTO 			= "MODDIF";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD 		= "DIRECCIONFISICA";
	
	public static class DireccionFisica extends BcsPojo {
		public Long 	IdDireccionFisica;
		public Long 	IdPersona;
		public String 	NombreCompleto;
		public String 	DocumentoIdentidad;
		public String 	Calle;
		public String 	Numero;
		public String 	Departamento;
		public String 	Sector;
		public String 	CodigoPostal;
		public Long 	IdCiudad;
		public String 	CodigoProposito;
		public String 	Comentario;
		public String 	IdEstado;
		public Long 	IdDivisionPais;
		public String 	CodigoCompuesto;
		public String 	Predeterminado;
		
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
		logger.info("INICIO::ModificarPersonaDireccionFisicaMasivo");
		long inicioServicio = System.currentTimeMillis();
		SalidaEjecutarCambiosPersona out = new SalidaEjecutarCambiosPersona();
		
		List<Long> lstIdSolicitudCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
		Long idSolicitudCambio = lstIdSolicitudCambio.get(0);

		List<DireccionFisica> lstDireccionFisica = obtenerDatosDireccionFisica(con, entrada);

		if (lstDireccionFisica.size() == 0) {
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		List<RegistroSolicitudCambio> registrosSolicitudesCambios =  obtenerRegistroSolicitudesCambio(con, idSolicitudCambio, lstDireccionFisica, entrada);
		
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
			out = crearEventoAutorizacion(con, entrada, lstDireccionFisica);
		} else {
			List<InEntidad> lstInEntidad = obtenerListaPersonaEntidad(lstDireccionFisica);
			ConfirmarCambiosPersonaEntidad confirmarCambiosEntidad = new ConfirmarCambiosPersonaEntidad();
			out = confirmarCambiosEntidad.logica(con, entrada, lstInEntidad, new ConfirmarModificarPersonaDireccionFisicaMasivo());
		}
		
		out.P_OUT_REFID = idSolicitudCambio;
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPersonaDireccionFisicaMasivo", finServicio - inicioServicio);
		return out;
	}
	
	private List<InEntidad> obtenerListaPersonaEntidad(List<DireccionFisica> lstDireccionFisica) {
		logger.trace("INICIO::ModificarPersonaDireccionFisicaMasivo.obtenerListaPersonaEntidad");
		long inicio = System.currentTimeMillis();		
		List<InEntidad> lstInEntidad = lstDireccionFisica.stream()
				  .map(entidad -> new InEntidad(entidad.IdPersona,entidad.IdDireccionFisica))
				  .distinct()
				  .collect(Collectors.toList());	
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPersonaDireccionFisicaMasivo.obtenerListaPersonaEntidad", fin - inicio);
		return lstInEntidad;
	}

	private List<DireccionFisica> obtenerDatosDireccionFisica(Connection con, EntradaEjecutarCambiosPersona entrada) throws Exception {
		logger.debug("INICIO::ModificarPersonaDireccionFisicaMasivo.obtenerDatosDireccionFisica");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		List<Long> idEntidades = entrada.p_RegistroCambio.stream()
														 .map(entidad -> entidad.IdEntidad)
														 .distinct()
														 .collect(Collectors.toList());
		params.put("IdDireccionFisica", idEntidades);		
		List<DireccionFisica> listaDirecciones = sql.query(con, SQL_SELECT_DIRECCION_FISICA, params, DireccionFisica.class);
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaDireccionFisicaMasivo.obtenerDatosDireccionFisica", fin - inicio);
		return listaDirecciones;
	}
	
	private SalidaEjecutarCambiosPersona crearEventoAutorizacion(Connection con, EntradaEjecutarCambiosPersona entrada, List<DireccionFisica> lstEntidad) throws Exception {
		logger.debug("INICIO::ModificarPersonaDireccionFisicaMasivo.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
		
		for (DireccionFisica entidad : lstEntidad) {
			CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
			evento.IdTipoEvento 			= ID_CONCEPTO;
			evento.IdModuloSolicita 		= entrada.p_IdModulo;
			evento.IdUsuarioSolicita 		= entrada.p_IdUsuario;
			evento.Fecha 					= fechaSistema;
			evento.IdConcepto 				= ID_CONCEPTO;
			evento.NumeroOperacionConcepto 	= String.valueOf(entidad.IdDireccionFisica); 
			evento.FechaOperacionConcepto 	= fechaSistema;
			evento.IdCliente 				= entidad.IdPersona;
			evento.Glosa = entidad.DocumentoIdentidad + " / "  + entidad.NombreCompleto + " / " +  entidad.Calle + " / IdDirFis: " +  entidad.IdDireccionFisica;
			entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS.add(evento);			
			
		}

		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		SalidaEjecutarCambiosPersona salidaModificarPersonaDireccionFisica = new SalidaEjecutarCambiosPersona();
		salidaModificarPersonaDireccionFisica.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaModificarPersonaDireccionFisica.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaModificarPersonaDireccionFisica.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaModificarPersonaDireccionFisica.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaModificarPersonaDireccionFisica.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaDireccionFisicaMasivo.crearEventoAutorizacion", fin - inicio);
		return salidaModificarPersonaDireccionFisica;	
	}
	
	private List<RegistroSolicitudCambio> obtenerRegistroSolicitudesCambio(Connection con, Long idSolicitudCambio, List<DireccionFisica> lstEntidad, EntradaEjecutarCambiosPersona entrada) throws Exception{
		logger.trace("INICIO::ModificarPersonaDireccionFisicaMasivo.obtenerRegistroSolicitudesCambio");
		long inicio 	= System.currentTimeMillis();
		List<RegistroSolicitudCambio> registroSolicitudesCambio = new ArrayList<RegistroSolicitudCambio>();
		
		Map<Long, DireccionFisica> entidadesPorId = lstEntidad
				.parallelStream()
				.collect(Collectors.toMap(entidad->entidad.IdDireccionFisica, entidad->entidad));
		
		for(RegistroCambioMasivo registroCambio : entrada.p_RegistroCambio) {
			DireccionFisica entidadesId = entidadesPorId.get(registroCambio.IdEntidad);
			if(entidadesId != null) {
				RegistroSolicitudCambio registroSolicitudCambio = new CrearRegistroSolicitudCambio.RegistroSolicitudCambio();
				registroSolicitudCambio.IdSolicitudCambio = idSolicitudCambio;
				registroSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
				registroSolicitudCambio.IdEntidadOrigen = String.valueOf(registroCambio.IdEntidad);
				registroSolicitudCambio.CampoModificado = registroCambio.CampoModificado;
				
				ValorAntiguo valorAntiguo = ValorAntiguo.obtenerValorAntiguo(entidadesId, DireccionFisica.class, registroCambio.CampoModificado);
				
				registroSolicitudCambio.TipoDato = valorAntiguo.TipoDato;
				registroSolicitudCambio.ValorAntiguo = valorAntiguo.Valor;
				registroSolicitudCambio.ValorNuevo = registroCambio.ValorNuevo;
				registroSolicitudCambio.IdEstadoAutorizacion = ID_ESTADO_AUTORIZACION;
				
				registroSolicitudesCambio.add(registroSolicitudCambio);
			}
			
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::ModificarPersonaDireccionFisicaMasivo.obtenerRegistroSolicitudesCambio", fin - inicio);
		return registroSolicitudesCambio;
	}	
	
	// --------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// --------------------------------------------------	
	public static void main(String[] args) throws SQLException {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_RegistroCambio\":[{\"CampoModificado\":\"Numero\",\"ValorNuevo\":1031,\"IdEntidad\":159005}, {\"CampoModificado\":\"Numero\",\"ValorNuevo\":1031,\"IdEntidad\":159004}]}";
			EntradaEjecutarCambiosPersona Objin = gson.fromJson(json, EntradaEjecutarCambiosPersona.class);
			
			ModificarPersonaDireccionFisicaMasivo demo = new ModificarPersonaDireccionFisicaMasivo();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 			
 			try {
 				SalidaEjecutarCambiosPersona ObjOut = demo.logica(con, Objin);
 				con.commit();
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
