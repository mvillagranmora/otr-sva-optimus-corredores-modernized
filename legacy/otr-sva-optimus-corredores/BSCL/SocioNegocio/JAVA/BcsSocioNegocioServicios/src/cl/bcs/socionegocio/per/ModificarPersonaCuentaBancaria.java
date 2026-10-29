
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
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.EntradaCrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.RegistroSolicitudCambio;
import cl.bcs.plataforma.pla.CrearRegistroSolicitudCambio.SalidaCrearRegistroSolicitudCambio;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaCuentaBancaria.EntradaConfirmarModificarPersonaCuentaBancaria;
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaCuentaBancaria.InCuentaBancaria;
import cl.bcs.socionegocio.per.ConfirmarModificarPersonaCuentaBancaria.SalidaConfirmarModificarPersonaCuentaBancaria;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;



public class ModificarPersonaCuentaBancaria implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(ModificarPersonaCuentaBancaria.class);

	private String SQL_SELECT_CUENTA_BANCARIA = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaCuentaBancaria_SelectCuentaBancaria.sql");	
	private String SQL_INSERT_REGISTRO_CAMBIO_TEMPORAL = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersona_InsertRegistroCambio.sql");
	private String SQL_SELECT_REGISTRO_SOLICITUD_CAMBIO = loadResourceToString("cl/bcs/socionegocio/per/sql/ModificarPersonaCuentaBancaria_SelectRegistroSolicitudCambio.sql");
	private String SQL_SELECT_REGISTRO_CAMBIO_TEMPORAL = "SELECT \"CampoModificado\",\"ValorNuevo\" FROM \"BSCL.SocioNegocio.BD::GBL.ModificarPersona_RegistroCambio\"";
	
	
	private final String ID_CONCEPTO 			= "MODCTB";
	private final String ID_ESTADO_AUTORIZACION = "PEN";
	private final String ID_TIPO_ENTIDAD 		= "CUENTABANCARIA";
	
	
	
	// --------------------------------------------------------------------
	// CLASES DE ENTRADA
	// --------------------------------------------------------------------	

	public static class EntradaModificarPersonaCuentaBancaria extends BcsPojo {
		public Long 		p_IdEmpresa;
		public Long			p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_UsaReglasNegocio;
		public Long 		p_IdCuentaBancaria;		
		public List<RegistroCambio> p_RegistroCambio;
		
		public EntradaModificarPersonaCuentaBancaria() {
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
	
	public static class SalidaModificarPersonaCuentaBancaria extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;		
		public Long	 				P_OUT_REFID;
		
		public SalidaModificarPersonaCuentaBancaria(){
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();			
		}
	}
	
	
	// --------------------------------------------------------------------	
	// CLASES COMPLEMENTARIAS
	// --------------------------------------------------------------------	
	public static class CuentaBancaria extends BcsPojo
	{
		public Long 	IdPersona;	
		public String 	NombreCompleto;
		public String 	DocumentoIdentidad;
		public String 	NumeroCuentaBancaria;
		
	}
	
	// --------------------------------------------------
	// CALL
	// --------------------------------------------------	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarPersonaCuentaBancaria jin = gson.fromJson(in.toString(), EntradaModificarPersonaCuentaBancaria.class);
		SalidaModificarPersonaCuentaBancaria out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	// --------------------------------------------------
	// LOGICA
	// --------------------------------------------------
	public SalidaModificarPersonaCuentaBancaria logica(Connection con, EntradaModificarPersonaCuentaBancaria entrada) throws Exception {
		logger.info("INICIO::ModificarPersonaCuentaBancaria");
		long inicioServicio = System.currentTimeMillis();
		SalidaModificarPersonaCuentaBancaria out = new SalidaModificarPersonaCuentaBancaria();
		
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		
		List<Long> lstIdSolicitudCambio = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::PLA.IdSolicitudCambio", 1L);
		Long idSolicitudCambio = lstIdSolicitudCambio.get(0);
		
		
		// ------------------------------------------------------------
		// Obtiene datos de la cuenta bancaria a modificar	
		params.clear();		
		params.put("IdCuentaBancaria", entrada.p_IdCuentaBancaria);		
		List<CuentaBancaria> lstCuentaBancaria = sql.query(con, SQL_SELECT_CUENTA_BANCARIA, params, CuentaBancaria.class);
		CuentaBancaria cuentaBancaria = new CuentaBancaria();
		
		if (lstCuentaBancaria.size() == 0)
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		else
		{
			cuentaBancaria = lstCuentaBancaria.get(0);
		}
		
		
		
		// ------------------------------------------------------------
		// Inserta en tabla global registro cambio
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
		params.put("IdCuentaBancaria", entrada.p_IdCuentaBancaria);		
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
			out = crearEventoAutorizacion(con, entrada, cuentaBancaria);
		} 
		else 
		{
			out = confirmarModificarCuentaBancaria(con, entrada);
		}
		

		
		out.P_OUT_REFID = idSolicitudCambio;
		
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarPersonaCuentaBancaria", finServicio - inicioServicio);
		return out;
	}
	
	
	
	
	
	private SalidaModificarPersonaCuentaBancaria crearEventoAutorizacion(Connection con, EntradaModificarPersonaCuentaBancaria entrada, CuentaBancaria cuentaBancaria) throws Exception {
		logger.debug("INICIO::ModificarPersonaCuentaBancaria.crearEventoAutorizacion");
		long inicio = System.currentTimeMillis();
		SalidaModificarPersonaCuentaBancaria salidaModificarPersonaCuentaBancaria = new SalidaModificarPersonaCuentaBancaria();
		
		CrearEventoAutorizacionMasivo crearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo();
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		CrearEventoAutorizacionMasivo.ServiceEntrada entradaCrearEventoAutorizacionMasivo = new CrearEventoAutorizacionMasivo.ServiceEntrada();
						
		CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS evento = new CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS();
		evento.IdTipoEvento 			= ID_CONCEPTO;
		evento.IdModuloSolicita 		= entrada.p_IdModulo;
		evento.IdUsuarioSolicita 		= entrada.p_IdUsuario;
		evento.Fecha 					= fechaSistema;
		evento.IdConcepto 				= ID_CONCEPTO;
		evento.NumeroOperacionConcepto 	= String.valueOf(entrada.p_IdCuentaBancaria); 
		evento.FechaOperacionConcepto 	= fechaSistema;
		evento.IdCliente 				= cuentaBancaria.IdPersona;
		evento.Glosa = cuentaBancaria.DocumentoIdentidad + " / "  + cuentaBancaria.NombreCompleto + " / " +  cuentaBancaria.NumeroCuentaBancaria + " / IdCta: " +  entrada.p_IdCuentaBancaria;
		
		entradaCrearEventoAutorizacionMasivo.P_IDEMPRESA = entrada.p_IdEmpresa;
		entradaCrearEventoAutorizacionMasivo.P_IDMODULO = entrada.p_IdModulo;
		entradaCrearEventoAutorizacionMasivo.P_IDUSUARIO = entrada.p_IdUsuario;
		entradaCrearEventoAutorizacionMasivo.P_SIMULAROPERACION = "N";
		entradaCrearEventoAutorizacionMasivo.P_USAREGLASNEGOCIO = "N";
		entradaCrearEventoAutorizacionMasivo.P_TABLAEVENTOS.add(evento);
		
		
		CrearEventoAutorizacionMasivo.ServiceSalida salidaCrearEventoAutorizacionMasivo = crearEventoAutorizacionMasivo.logica(con, entradaCrearEventoAutorizacionMasivo);
		
		salidaModificarPersonaCuentaBancaria.P_OUT_ERRORESCONTADOR = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESCONTADOR;
		
		List<MensajeSalida> mensajesAdvertencia = salidaCrearEventoAutorizacionMasivo.P_OUT_ADVERTENCIASDETALLE.parallelStream().map(advertencia->new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, advertencia.Codigo, advertencia.Descripcion)).collect(Collectors.toList());
		List<MensajeSalida> mensajesErrores = salidaCrearEventoAutorizacionMasivo.P_OUT_ERRORESDETALLE.parallelStream().map(error->new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, error.Codigo, error.Descripcion)).collect(Collectors.toList());
		
		salidaModificarPersonaCuentaBancaria.P_OUT_MENSAJES.addAll(mensajesAdvertencia);
		salidaModificarPersonaCuentaBancaria.P_OUT_MENSAJES.addAll(mensajesErrores);
		
		if(salidaModificarPersonaCuentaBancaria.P_OUT_ERRORESCONTADOR.equals(0L)) {
			salidaModificarPersonaCuentaBancaria.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_AUTORIZACION,"REQUIERE_AUTORIZACION",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaCuentaBancaria.crearEventoAutorizacion", fin - inicio);
		return salidaModificarPersonaCuentaBancaria;	
	}
	
	
	
	private SalidaModificarPersonaCuentaBancaria confirmarModificarCuentaBancaria(Connection con, EntradaModificarPersonaCuentaBancaria entrada) throws Exception {
		logger.debug("INICIO::ModificarPersonaCuentaBancaria.confirmarModificarCuentaBancaria");
		long inicio = System.currentTimeMillis();

		EntradaConfirmarModificarPersonaCuentaBancaria entradaConfirmarModificarPersonaCuentaBancaria = new EntradaConfirmarModificarPersonaCuentaBancaria();		
		InCuentaBancaria entradaCuentaBancaria = new InCuentaBancaria();
		entradaCuentaBancaria.IdCuentaBancaria = entrada.p_IdCuentaBancaria;
		
		entradaConfirmarModificarPersonaCuentaBancaria.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaConfirmarModificarPersonaCuentaBancaria.p_IdUsuario = entrada.p_IdUsuario;
		entradaConfirmarModificarPersonaCuentaBancaria.p_IdModulo = entrada.p_IdModulo;
		entradaConfirmarModificarPersonaCuentaBancaria.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
		entradaConfirmarModificarPersonaCuentaBancaria.p_CuentaBancaria.add(entradaCuentaBancaria);
		entradaConfirmarModificarPersonaCuentaBancaria.p_IdEstadoAutorizacion = "ING";	
		
		ConfirmarModificarPersonaCuentaBancaria servicioConfirmarModificarPersonaCuentaBancariaMasivo = new ConfirmarModificarPersonaCuentaBancaria();		
		SalidaConfirmarModificarPersonaCuentaBancaria salidaConfirmarModificarPersonaCuentaBancariaMasivo = servicioConfirmarModificarPersonaCuentaBancariaMasivo.logica(con, entradaConfirmarModificarPersonaCuentaBancaria);
		
		SalidaModificarPersonaCuentaBancaria out = new SalidaModificarPersonaCuentaBancaria();
		out.P_OUT_ERRORESCONTADOR = salidaConfirmarModificarPersonaCuentaBancariaMasivo.P_OUT_ERRORESCONTADOR;
		out.P_OUT_MENSAJES = salidaConfirmarModificarPersonaCuentaBancariaMasivo.P_OUT_MENSAJES;

		long fin = System.currentTimeMillis();
		logger.debug("FIN::ModificarPersonaCuentaBancaria.confirmarModificarCuentaBancaria", fin - inicio);
		return out;
	}
	
	

	// --------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// --------------------------------------------------	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdCuentaBancaria\":387,\"p_RegistroCambio\":[{\"CampoModificado\":\"NumeroCuentaBancaria\",\"ValorNuevo\":\"999999\"},{\"CampoModificado\":\"IdMoneda\",\"ValorNuevo\":\"USD\"},{\"CampoModificado\":\"IdTipoIdentificadorCuenta\",\"ValorNuevo\":\"OK\"}]}";
			///String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_IdCuentaBancaria\":388,\"p_RegistroCambio\":[{\"CampoModificado\":\"NumeroCuentaBancaria\",\"ValorNuevo\":\"7777777777\"}]}";
			
			EntradaModificarPersonaCuentaBancaria Objin = gson.fromJson(json, EntradaModificarPersonaCuentaBancaria.class);
			
			ModificarPersonaCuentaBancaria demo = new ModificarPersonaCuentaBancaria();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 		
 			try {
 				SalidaModificarPersonaCuentaBancaria ObjOut = demo.logica(con, Objin);
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
