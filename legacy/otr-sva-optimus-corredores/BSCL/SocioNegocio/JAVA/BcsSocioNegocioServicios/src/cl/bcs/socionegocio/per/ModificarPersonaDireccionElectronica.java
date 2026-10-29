
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

import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.EntradaConfirmarEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.InEntidad;
import cl.bcs.socionegocio.per.ConfirmarEjecutarCambiosPersona.SalidaConfirmarEjecutarCambiosPersona;



public class ModificarPersonaDireccionElectronica implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ModificarPersonaDireccionElectronica.class);

	

	private String SQL_SELECT_DIRECCIONELECTRONICA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaDireccionElectronica_SelectDireccionElectronica.sql");	
	private String SQL_INSERT_REGISTRO_CAMBIO_TEMPORAL = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersona_InsertRegistroCambio.sql");	
	private String SQL_SELECT_REGISTRO_CAMBIO_TEMPORAL = "SELECT \"CampoModificado\",\"ValorNuevo\" FROM \"BSCL.SocioNegocio.BD::GBL.ModificarPersona_RegistroCambio\"";
	private String SQL_SELECT_REGISTRO_SOLICITUD_CAMBIO = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaDireccionElectronica_SelectRegistroSolicitudCambio.sql");
	

	
	private final String ID_CONCEPTO 			= "MODDIE";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD 		= "DIRECCIONELECTRONICA";
	

	
	// --------------------------------------------------------------------
	// CLASES DE ENTRADA
	// --------------------------------------------------------------------	


	public static class EntradaModificarPersonaDireccionElectronica extends BcsPojo {
		public Long 		p_IdEmpresa;
		public Long			p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_UsaReglasNegocio;
		public Long 		p_IdDireccionElectronica;		
		public List<RegistroCambio> p_RegistroCambio;
		
		
		public EntradaModificarPersonaDireccionElectronica() {
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

	public static class SalidaModificarPersonaDireccionElectronica extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;		
		public Long	 				P_OUT_REFID;
		
		public SalidaModificarPersonaDireccionElectronica(){
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();			
		}
	}
	
	
	// --------------------------------------------------------------------	
	// CLASES COMPLEMENTARIAS
	// --------------------------------------------------------------------	

	public static class DireccionElectronica extends BcsPojo
	{
		public Long 	IdPersona;	
		public String 	NombreCompleto;
		public String 	DocumentoIdentidad;
		public String 	Direcion;
		
	}

	
	// --------------------------------------------------
	// CALL
	// --------------------------------------------------	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarPersonaDireccionElectronica jin = gson.fromJson(in.toString(), EntradaModificarPersonaDireccionElectronica.class);
		SalidaModificarPersonaDireccionElectronica out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	// --------------------------------------------------
	// LOGICA
	// --------------------------------------------------
	public SalidaModificarPersonaDireccionElectronica logica(Connection con, EntradaModificarPersonaDireccionElectronica entrada) throws Exception {
		logger.info("INICIO::ModificarPersonaDireccionElectronica");
		long inicioServicio = System.currentTimeMillis();
		SalidaModificarPersonaDireccionElectronica out = new SalidaModificarPersonaDireccionElectronica();
		
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		
		
		List<Long> lstIdSolicitudCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
		Long idSolicitudCambio = lstIdSolicitudCambio.get(0);
		
		
		// ------------------------------------------------------------
		// Obtiene datos del direcciones electronicas a modificar	
		params.clear();		
		params.put("IdDireccionElectronica", entrada.p_IdDireccionElectronica);	
		
		List<DireccionElectronica> lstDireccionElectronica = sql.query(con, SQL_SELECT_DIRECCIONELECTRONICA, params, DireccionElectronica.class);
		DireccionElectronica direccionElectronica = new DireccionElectronica();
		
		if (lstDireccionElectronica.size() == 0)
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		else
		{
			direccionElectronica = lstDireccionElectronica.get(0);
		}
		
		
		
		// ------------------------------------------------------------
		// Inserta en tabla global los cambios a la direccion electronica
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
		params.put("IdDireccionElectronica", entrada.p_IdDireccionElectronica);
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
			out = crearEventoAutorizacion(con, entrada, direccionElectronica);
		}
		else 
		{
			out = confirmarModificarDireccionElectronica(con, entrada);
		}
		

		
		out.P_OUT_REFID = idSolicitudCambio;
		
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPersonaDireccionElectronica", finServicio - inicioServicio);
		return out;
	}
	
	
	
	
	
	
	private SalidaModificarPersonaDireccionElectronica crearEventoAutorizacion(Connection con, EntradaModificarPersonaDireccionElectronica entrada, DireccionElectronica direccionElectronica) throws Exception {
		logger.debug("INICIO::ModificarPersonaDireccionElectronica.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaModificarPersonaDireccionElectronica salidaModificarPersonaDireccionElectronica = new SalidaModificarPersonaDireccionElectronica();		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
						
		CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
		evento.IdTipoEvento 			= ID_CONCEPTO;
		evento.IdModuloSolicita 		= entrada.p_IdModulo;
		evento.IdUsuarioSolicita 		= entrada.p_IdUsuario;
		evento.Fecha 					= fechaSistema;
		evento.IdConcepto 				= ID_CONCEPTO;
		evento.NumeroOperacionConcepto 	= String.valueOf(entrada.p_IdDireccionElectronica); 
		evento.FechaOperacionConcepto 	= fechaSistema;
		evento.IdCliente 				= direccionElectronica.IdPersona;
		evento.Glosa = direccionElectronica.DocumentoIdentidad + " / "  + direccionElectronica.NombreCompleto + " / " +  direccionElectronica.Direcion + " / IdDir: " +  entrada.p_IdDireccionElectronica;
		
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS.add(evento);
		
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaModificarPersonaDireccionElectronica.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaModificarPersonaDireccionElectronica.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaModificarPersonaDireccionElectronica.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaModificarPersonaDireccionElectronica.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaModificarPersonaDireccionElectronica.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaDireccionElectronica.crearEventoAutorizacion", fin - inicio);
		return salidaModificarPersonaDireccionElectronica;	
	}
	
	

	private SalidaModificarPersonaDireccionElectronica confirmarModificarDireccionElectronica(Connection con, EntradaModificarPersonaDireccionElectronica entrada) throws Exception {
		logger.debug("INICIO::ModificarPersonaDireccionElectronica.confirmarModificarDireccionElectronica");
		long inicio = System.currentTimeMillis();

		EntradaConfirmarEjecutarCambiosPersona entradaConfirmarModificarPersonaDireccionElectronicaMasivo = new EntradaConfirmarEjecutarCambiosPersona();		
		InEntidad entradaDireccionElectronica = new InEntidad();
		entradaDireccionElectronica.IdEntidad = entrada.p_IdDireccionElectronica;
		
		entradaConfirmarModificarPersonaDireccionElectronicaMasivo.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfirmarModificarPersonaDireccionElectronicaMasivo.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfirmarModificarPersonaDireccionElectronicaMasivo.p_IdModulo = entrada.p_IdModulo;
		entradaConfirmarModificarPersonaDireccionElectronicaMasivo.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConfirmarModificarPersonaDireccionElectronicaMasivo.p_Entidad.add(entradaDireccionElectronica);
		entradaConfirmarModificarPersonaDireccionElectronicaMasivo.p_IdEstadoAutorizacion = "ING";	
	
		ConfirmarModificarPersonaDireccionElectronicaMasivo servicioConfirmarModificarPersonaDireccionElectronicaMasivo = new ConfirmarModificarPersonaDireccionElectronicaMasivo();		
		SalidaConfirmarEjecutarCambiosPersona salidaConfirmarModificarPersonaDireccionElectronicaMasivo = servicioConfirmarModificarPersonaDireccionElectronicaMasivo.logica(con, entradaConfirmarModificarPersonaDireccionElectronicaMasivo);
		
		SalidaModificarPersonaDireccionElectronica out = new SalidaModificarPersonaDireccionElectronica();
		out.P_OUT_ERRORESCONTADOR = salidaConfirmarModificarPersonaDireccionElectronicaMasivo.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaConfirmarModificarPersonaDireccionElectronicaMasivo.P_OUT_MENSAJES;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaDireccionElectronica.confirmarModificarDireccionElectronica", fin - inicio);
		return out;
	}

	
	// --------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// --------------------------------------------------	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdDireccionElectronica\":588086,\"p_RegistroCambio\":[{\"CampoModificado\":\"Direccion\",\"ValorNuevo\":\"JORGE.VILLAGRAN@HOTMAIL.COM\"},{\"CampoModificado\":\"CodigoProposito\",\"ValorNuevo\":\"COME\"},{\"CampoModificado\":\"Comentario\",\"ValorNuevo\":\"PRUEBA\"},{\"CampoModificado\":\"IdEstado\",\"ValorNuevo\":\"INA\"}]}";

			EntradaModificarPersonaDireccionElectronica Objin = gson.fromJson(json, EntradaModificarPersonaDireccionElectronica.class);
			
			ModificarPersonaDireccionElectronica demo = new ModificarPersonaDireccionElectronica();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 		
 			try {
 				SalidaModificarPersonaDireccionElectronica ObjOut = demo.logica(con, Objin);
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
