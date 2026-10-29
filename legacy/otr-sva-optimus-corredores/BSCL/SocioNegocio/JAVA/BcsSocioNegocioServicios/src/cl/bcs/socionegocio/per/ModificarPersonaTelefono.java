
/**
 *    Proposito: Servicio que obtiene y evalua informacion de los telefonos de la persona a modificar, 
 *    para ser creado como una solicitud de Cambio. 
 */
package cl.bcs.socionegocio.per;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.RegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.SalidaCrearRegistroSolicitudCambio;
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaTelefonoMasivo;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.EntradaConfirmarEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.InEntidad;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.SalidaConfirmarEjecutarCambiosPersona;



public class ModificarPersonaTelefono implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ModificarPersonaTelefono.class);

	private String SQL_SELECT_TELEFONO = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaTelefono_SelectTelefono.sql");	
	private String SQL_INSERT_REGISTRO_CAMBIO_TEMPORAL = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersona_InsertRegistroCambio.sql");
	private String SQL_SELECT_REGISTRO_CAMBIO_TEMPORAL = "SELECT \"CampoModificado\",\"ValorNuevo\" FROM \"BSCL.SocioNegocio.BD::GBL.ModificarPersona_RegistroCambio\"";	
	private String SQL_SELECT_REGISTRO_SOLICITUD_CAMBIO = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaTelefono_SelectRegistroSolicitudCambio.sql");
	
	
	private final String ID_CONCEPTO 			= "MODTEL";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD 		= "TELEFONO";

	
	
	// --------------------------------------------------------------------
	// CLASES DE ENTRADA
	// --------------------------------------------------------------------	


	public static class EntradaModificarPersonaTelefono extends BcsPojo {
		public Long 		p_IdEmpresa;
		public Long			p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_UsaReglasNegocio;
		public Long 		p_IdTelefono;		
		public List<RegistroCambio> p_RegistroCambio;
		
		public EntradaModificarPersonaTelefono() {
			this.p_RegistroCambio = new ArrayList<RegistroCambio>();
		}
	}
	
	
	public static class RegistroCambio extends BcsPojo
	{
		public String 		CampoModificado;
		public String 		ValorNuevo;
	};
	
	


	
	
	// --------------------------------------------------------------------
	// CLASES DE SALIDA
	// --------------------------------------------------------------------	

	public static class SalidaModificarPersonaTelefono extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;		
		public Long	 				P_OUT_REFID;
		
		public SalidaModificarPersonaTelefono(){
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();			
		}
	}
	
	
	// --------------------------------------------------------------------	
	// CLASES COMPLEMENTARIAS
	// --------------------------------------------------------------------	

	public static class Telefono extends BcsPojo
	{
		public Long 	IdPersona;	
		public String 	NombreCompleto;
		public String 	DocumentoIdentidad;
		public String 	Numero;
		
	}

	
	// --------------------------------------------------
	// CALL
	// --------------------------------------------------	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarPersonaTelefono jin = gson.fromJson(in.toString(), EntradaModificarPersonaTelefono.class);
		SalidaModificarPersonaTelefono out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	// --------------------------------------------------
	// LOGICA
	// --------------------------------------------------
	public SalidaModificarPersonaTelefono logica(Connection con, EntradaModificarPersonaTelefono entrada) throws Exception {
		logger.info("INICIO::ModificarPersonaTelefono");
		long inicioServicio = System.currentTimeMillis();
		SalidaModificarPersonaTelefono out = new SalidaModificarPersonaTelefono();
		
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		
		
		List<Long> lstIdSolicitudCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
		Long idSolicitudCambio = lstIdSolicitudCambio.get(0);
		
		
		// ------------------------------------------------------------
		// Obtiene datos del telefono a modificar	
		params.clear();		
		params.put("IdTelefono", entrada.p_IdTelefono);	
		
		List<Telefono> lstTelefono = sql.query(con, SQL_SELECT_TELEFONO, params, Telefono.class);
		Telefono telefono = new Telefono();
		
		if (lstTelefono.size() == 0)
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		else
		{
			telefono = lstTelefono.get(0);
		}
		
		
		
		// ------------------------------------------------------------
		// Inserta en tabla global los cambios al telefono
		if (entrada.p_RegistroCambio.size() > 0)
		{
			sql.batch(con, SQL_INSERT_REGISTRO_CAMBIO_TEMPORAL, entrada.p_RegistroCambio, RegistroCambio.class);			
		}
		else
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_REGISTRO_CAMBIO",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		// Para revisar contenido de tabla global
		List<RegistroCambio> paraRevisar = sql.query(con, SQL_SELECT_REGISTRO_CAMBIO_TEMPORAL, new HashMap<>(), RegistroCambio.class);

		
		
		
		//-----------------------------------------------------------------		
		// CREAR SOLICITUD DE CAMBIO
		params.clear();		
		params.put("IdSolicitudCambio", idSolicitudCambio);		
		params.put("IdTipoEntidad", ID_TIPO_ENTIDAD);		
		params.put("IdTelefono", entrada.p_IdTelefono);		
		params.put("IdEstadoAutorizacion", ID_ESTADO_AUTORIZACION);		
		
		List<RegistroSolicitudCambio> lstRegistroSolicitudCambio = new ArrayList<RegistroSolicitudCambio>();
		lstRegistroSolicitudCambio = sql.query(con, SQL_SELECT_REGISTRO_SOLICITUD_CAMBIO, params, RegistroSolicitudCambio.class);
		
		

		CrearRegistroSolicitudCambio servicioCrearRegistroSolicitudCambio = new CrearRegistroSolicitudCambio();
		EntradaCrearRegistroSolicitudCambio inCrearRegistroSolicitudCambio = new EntradaCrearRegistroSolicitudCambio();		
		inCrearRegistroSolicitudCambio.p_RegistroSolicitudCambio = lstRegistroSolicitudCambio;		
		SalidaCrearRegistroSolicitudCambio outCrearRegistroSolicitudCambio = servicioCrearRegistroSolicitudCambio.logica(con, inCrearRegistroSolicitudCambio);
		


		
		//-----------------------------------------------------------------	
		// DETERMINA SI LA MODIFICACION  REQUIERE DE UNA AUTORIZACION PREVIA
		String requiereAutorizacion = Utils.getRequiereEventoAutorizacion(con, entrada.p_IdEmpresa, ID_CONCEPTO);

		
		if("S".equals(requiereAutorizacion)) 
		{
			out = crearEventoAutorizacion(con, entrada, telefono);
		}
		else 
		{
			out = confirmarModificarTelefono(con, entrada);
		}
		

		
		out.P_OUT_REFID = idSolicitudCambio;
		
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPersonaTelefono", finServicio - inicioServicio);
		return out;
	}
	
	
	
	
	
	
	private SalidaModificarPersonaTelefono crearEventoAutorizacion(Connection con, EntradaModificarPersonaTelefono entrada, Telefono telefono) throws Exception {
		logger.debug("INICIO::ModificarPersonaTelefono.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaModificarPersonaTelefono salidaModificarPersonaTelefono = new SalidaModificarPersonaTelefono();		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
						
		CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
		evento.IdTipoEvento 			= ID_CONCEPTO;
		evento.IdModuloSolicita 		= entrada.p_IdModulo;
		evento.IdUsuarioSolicita 		= entrada.p_IdUsuario;
		evento.Fecha 					= fechaSistema;
		evento.IdConcepto 				= ID_CONCEPTO;
		evento.NumeroOperacionConcepto 	= String.valueOf(entrada.p_IdTelefono); 
		evento.FechaOperacionConcepto 	= fechaSistema;
		evento.IdCliente 				= telefono.IdPersona;
		evento.Glosa = telefono.DocumentoIdentidad + " / "  + telefono.NombreCompleto + " / " +  telefono.Numero + " / IdTel: " +  entrada.p_IdTelefono;
		
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS.add(evento);
		
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaModificarPersonaTelefono.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaModificarPersonaTelefono.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaModificarPersonaTelefono.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaModificarPersonaTelefono.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaModificarPersonaTelefono.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaTelefono.crearEventoAutorizacion", fin - inicio);
		return salidaModificarPersonaTelefono;	
	}
	
	
	
	private SalidaModificarPersonaTelefono confirmarModificarTelefono(Connection con, EntradaModificarPersonaTelefono entrada) throws Exception {
		logger.debug("INICIO::ModificarPersonaTelefono.confirmarModificarTelefono");
		long inicio = System.currentTimeMillis();

		EntradaConfirmarEjecutarCambiosPersona entradaConfirmarModificarPersonaTelefonoMasivo = new EntradaConfirmarEjecutarCambiosPersona();		
		InEntidad entradaTelefono = new InEntidad();
		entradaTelefono.IdEntidad = entrada.p_IdTelefono;
		
		entradaConfirmarModificarPersonaTelefonoMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfirmarModificarPersonaTelefonoMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfirmarModificarPersonaTelefonoMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaConfirmarModificarPersonaTelefonoMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConfirmarModificarPersonaTelefonoMasivo.p_Entidad.add(entradaTelefono);
		entradaConfirmarModificarPersonaTelefonoMasivo.p_IdEstadoAutorizacion = "ING";	
	
		ConfirmarModificarPersonaTelefonoMasivo servicioConfirmarModificarPersonaTelefonoMasivo = new ConfirmarModificarPersonaTelefonoMasivo();		
		SalidaConfirmarEjecutarCambiosPersona salidaConfirmarModificarPersonaTelefonoMasivo = servicioConfirmarModificarPersonaTelefonoMasivo.logica(con, entradaConfirmarModificarPersonaTelefonoMasivo);
		
		SalidaModificarPersonaTelefono out = new SalidaModificarPersonaTelefono();
		out.P_OUT_ERRORESCONTADOR = salidaConfirmarModificarPersonaTelefonoMasivo.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaConfirmarModificarPersonaTelefonoMasivo.P_OUT_MENSAJES;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaTelefono.confirmarModificarTelefono", fin - inicio);
		return out;
	}

	
	// --------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// --------------------------------------------------	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTelefono\":739475,\"p_RegistroCambio\":[{\"CampoModificado\":\"Numero\",\"ValorNuevo\":\"11111\"},{\"CampoModificado\":\"CodigoProposito\",\"ValorNuevo\":\"COME\"},{\"CampoModificado\":\"IdTipoTelefono\",\"ValorNuevo\":\"MOVIL\"},{\"CampoModificado\":\"IdPais\",\"ValorNuevo\":36}]}";
			///String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdTelefono\":739475,\"p_RegistroCambio\":[{\"CampoModificado\":\"NumeroCuentaBancaria\",\"ValorNuevo\":\"7777777777\"}]}";
			
			
			EntradaModificarPersonaTelefono Objin = gson.fromJson(json, EntradaModificarPersonaTelefono.class);
			
			ModificarPersonaTelefono demo = new ModificarPersonaTelefono();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 		
 			try {
 				SalidaModificarPersonaTelefono ObjOut = demo.logica(con, Objin);
 				con.commit();
 				//con.rollback();
 				System.out.println(ObjOut.toString());
 			}
 			catch(Exception er) {
				er.printStackTrace();
			}
			finally {
				//con.rollback();
				con.close();
			}
		}
		System.out.println("== END ==");
	}
}
