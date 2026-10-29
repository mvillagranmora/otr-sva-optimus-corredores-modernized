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

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ConfirmarModificarPersonaCuentaBancaria implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(ConfirmarModificarPersonaCuentaBancaria.class);
	
	
	private String SQL_INSERT_SOLICITUDES_CAMBIO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaCuentaBancariaMasivo_InsertSolicitudesCambio.sql");
	private String SQL_SELECT_SOLICITUDES_CAMBIO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaCuentaBancariaMasivo_SelectSolicitudesCambio.sql");
	private String SQL_INSERT_CUENTA_BANCARIA_TEMPORAL = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaCuentaBancariaMasivo_InsertCuentaBancaria.sql");
	private String SQL_UPDATE_CUENTA_BANCARIA_IDBANCO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaCuentaBancariaMasivo_UpdateCuentaBancaria_IdBanco.sql");
	private String SQL_UPDATE_CUENTA_BANCARIA_IDPLAZA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaCuentaBancariaMasivo_UpdateCuentaBancaria_IdPlaza.sql");
	private String SQL_UPDATE_CUENTA_BANCARIA_IDTIPOCUENTA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaCuentaBancariaMasivo_UpdateCuentaBancaria_IdTipoCuenta.sql");
	private String SQL_UPDATE_CUENTA_BANCARIA_NUMERO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaCuentaBancariaMasivo_UpdateCuentaBancaria_Numero.sql");
	private String SQL_UPDATE_CUENTA_BANCARIA_IDMONEDA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaCuentaBancariaMasivo_UpdateCuentaBancaria_IdMoneda.sql");
	private String SQL_UPDATE_CUENTA_BANCARIA_IDESTADO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaCuentaBancariaMasivo_UpdateCuentaBancaria_IdEstado.sql");
	private String SQL_UPDATE_CUENTA_BANCARIA_IDTIPOIDENTIFICADOR = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaCuentaBancariaMasivo_UpdateCuentaBancaria_IdTipoIdentificadorCuenta.sql");
	private String SQL_UPDATE_CUENTA_BANCARIA_IDPERSONA = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaCuentaBancariaMasivo_UpdateCuentaBancaria_IdPersonaRelacionada.sql");
	private String SQL_UPDATE_ESTADO_SOLICITUD_CAMBIO = loadResourceToString("cl/bcs/socionegocio/per/sql/ConfirmarModificarPersonaCuentaBancariaMasivo_UpdateEstadoSolicitudCambio.sql");
	private String SQL_SELECT_CUENTA_BANCARIA_TEMPORAL = "SELECT \"IdCuentaBancaria\" FROM \"BSCL.SocioNegocio.BD::GBL.ConfirmarModificarPersonaCuentaBancariaMasivo_CuentaBancaria\"";
	
	
	private final String ID_TIPO_ENTIDAD 		= "CUENTABANCARIA";
	

	// --------------------------------------------------------------------
	// CLASES DE ENTRADA
	// --------------------------------------------------------------------	
	public static class EntradaConfirmarModificarPersonaCuentaBancaria extends BcsPojo {
		public Long 		p_IdEmpresa;
		public Long			p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_UsaReglasNegocio;	
		public List<InCuentaBancaria> p_CuentaBancaria;		
		public String 		p_IdEstadoAutorizacion;	
	
		
		public EntradaConfirmarModificarPersonaCuentaBancaria() {
			this.p_CuentaBancaria = new ArrayList<InCuentaBancaria>();
		}
	}
	
	
	public static class InCuentaBancaria extends BcsPojo
	{
		public Long 		IdCuentaBancaria;		
	};
	
	
	
	
	// --------------------------------------------------------------------
	// CLASES DE SALIDA
	// --------------------------------------------------------------------	
	
	public static class SalidaConfirmarModificarPersonaCuentaBancaria extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;		
		
		public SalidaConfirmarModificarPersonaCuentaBancaria(){
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();			
		}
	}
	
	
	// --------------------------------------------------------------------	
	// CLASES COMPLEMENTARIAS
	// --------------------------------------------------------------------	

	public  static class SolicitudCambio extends BcsPojo
	{
	   public Long 		IdSolicitudCambio;
	   public String 	CampoModificado;
	   public String 	ValorAntiguo;
	   public String 	ValorNuevo;
	   public String 	IdEntidadOrigen;
	   public Long 		IdCuentaBancaria;	   
	   public String 	IdTipoEntidad;    // CUENTABANCARIA	   
	   public String 	IdEstadoAutorizacion = "";
	}  
	
	// --------------------------------------------------
	// CALL
	// --------------------------------------------------	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaConfirmarModificarPersonaCuentaBancaria jin = gson.fromJson(in.toString(), EntradaConfirmarModificarPersonaCuentaBancaria.class);
		SalidaConfirmarModificarPersonaCuentaBancaria out = logica(conn,jin);
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	// --------------------------------------------------
	// LOGICA
	// --------------------------------------------------
	public SalidaConfirmarModificarPersonaCuentaBancaria logica(Connection con, EntradaConfirmarModificarPersonaCuentaBancaria entrada) throws Exception {
		logger.info("INICIO::ConfirmarModificarPersonaCuentaBancariaMasivo");
		long inicioServicio = System.currentTimeMillis();
		SalidaConfirmarModificarPersonaCuentaBancaria out = new SalidaConfirmarModificarPersonaCuentaBancaria();
		
		SQL sql = new SQL();		
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		
		// --------------------------------------------------------------------------
		// Inserta en tabla global las cuentas bancarias de entrada
		if (entrada.p_CuentaBancaria.size() > 0)
		{
			sql.batch(con, SQL_INSERT_CUENTA_BANCARIA_TEMPORAL, entrada.p_CuentaBancaria, InCuentaBancaria.class);			
		}
		else
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"FALTA_ESPECIFICAR_CUENTA_BANCARIA",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		// Solo para revisar contenido de global
		List<InCuentaBancaria> paraRevisar = sql.query(con, SQL_SELECT_CUENTA_BANCARIA_TEMPORAL, new HashMap<>(), InCuentaBancaria.class);
		
		// --------------------------------------------------------------------------
		//Inserta en tabla global solicitudes de cambio
		List<SolicitudCambio> paramsSolicitudCambio = new ArrayList<SolicitudCambio>();
		SolicitudCambio objSolicitudCambio = new SolicitudCambio();
		objSolicitudCambio.IdTipoEntidad = ID_TIPO_ENTIDAD;
		paramsSolicitudCambio.add(objSolicitudCambio);		
		sql.batch(con, SQL_INSERT_SOLICITUDES_CAMBIO, paramsSolicitudCambio, SolicitudCambio.class);
		
		
		
		// --------------------------------------------------------------------------
		// Obtiene datos de solicitud de cambio
		List<SolicitudCambio> lstSolicitudCambio = sql.query(con, SQL_SELECT_SOLICITUDES_CAMBIO, new HashMap<>(), SolicitudCambio.class);
			
		if (lstSolicitudCambio.size() == 0)
		{
			out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"SOLICITUD_NO_EXISTE",null,con,entrada.p_IdEmpresa));
			return out;
		}
		
		
		// -------------------------------------------------------------
		// Actualiza los campos de la cuenta bancaria de acuerdo a las solicitudes de cambio 
		modificarCampoCuentaBancaria(con, sql, "NumeroCuentaBancaria", lstSolicitudCambio, SQL_UPDATE_CUENTA_BANCARIA_NUMERO);		
		modificarCampoCuentaBancaria(con, sql, "IdBanco", lstSolicitudCambio, SQL_UPDATE_CUENTA_BANCARIA_IDBANCO);
		modificarCampoCuentaBancaria(con, sql, "IdPlaza", lstSolicitudCambio, SQL_UPDATE_CUENTA_BANCARIA_IDPLAZA);
		modificarCampoCuentaBancaria(con, sql, "IdTipoCuentaBancaria", lstSolicitudCambio, SQL_UPDATE_CUENTA_BANCARIA_IDTIPOCUENTA);
		modificarCampoCuentaBancaria(con, sql, "IdMoneda", lstSolicitudCambio, SQL_UPDATE_CUENTA_BANCARIA_IDMONEDA);		
		modificarCampoCuentaBancaria(con, sql, "IdEstado", lstSolicitudCambio, SQL_UPDATE_CUENTA_BANCARIA_IDESTADO);
		modificarCampoCuentaBancaria(con, sql, "IdTipoIdentificadorCuenta", lstSolicitudCambio, SQL_UPDATE_CUENTA_BANCARIA_IDTIPOIDENTIFICADOR);
		modificarCampoCuentaBancaria(con, sql, "IdPersonaRelacionada", lstSolicitudCambio, SQL_UPDATE_CUENTA_BANCARIA_IDPERSONA);
		
		
		
		// -------------------------------------------------------------
		// Modifica el estado de la solicitud de cambio

		List<SolicitudCambio> paramsUpdateEstadoSolicitud = new ArrayList<SolicitudCambio>();
		SolicitudCambio objUpdateEstadoSolicitud = new SolicitudCambio();
		objUpdateEstadoSolicitud.IdEstadoAutorizacion = entrada.p_IdEstadoAutorizacion;
		paramsUpdateEstadoSolicitud.add(objUpdateEstadoSolicitud);		
		sql.batch(con, SQL_UPDATE_ESTADO_SOLICITUD_CAMBIO, paramsUpdateEstadoSolicitud, SolicitudCambio.class);
		
		// -------------------------------------------------------------
		// Grabar Log
		agregarLog(con, entrada, lstSolicitudCambio);
		
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ConfirmarModificarPersonaCuentaBancariaMasivo", finServicio - inicioServicio);
		return out;
	}
	
	
	
	private void modificarCampoCuentaBancaria(Connection con, SQL sql, String campoModificado,List<SolicitudCambio> solicitudCambio, String queryUpdate) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaCuentaBancariaMasivo.modificarCuentaBancaria");
		long inicio = System.currentTimeMillis();
		
		
		List<SolicitudCambio> solicitudCambioCuentaBancaria = solicitudCambio.stream().filter(c -> c.CampoModificado.equals(campoModificado)).collect(Collectors.toList());

		if (!solicitudCambioCuentaBancaria.isEmpty())
		{				
			List<SolicitudCambio> paramsUpdateCuentaBancaria = new ArrayList<SolicitudCambio>();
			SolicitudCambio objUpdateCuentaBancaria = new SolicitudCambio();			
			objUpdateCuentaBancaria.IdTipoEntidad = ID_TIPO_ENTIDAD;
			objUpdateCuentaBancaria.CampoModificado = campoModificado;			
			paramsUpdateCuentaBancaria.add(objUpdateCuentaBancaria);			
			sql.batch(con, queryUpdate, paramsUpdateCuentaBancaria, SolicitudCambio.class);
			
		}
				
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaCuentaBancariaMasivo.modificarCuentaBancaria", fin - inicio);
	}
	
	

	private void agregarLog(Connection con, EntradaConfirmarModificarPersonaCuentaBancaria entrada, List<SolicitudCambio> solicitudCambio) throws Exception {
		logger.debug("INICIO::ConfirmarModificarPersonaCuentaBancariaMasivo.agregarLog");
		long inicio = System.currentTimeMillis();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		
		entradaAgregarLog.p_Log = entrada.p_CuentaBancaria.parallelStream().map(cuenta->{
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
			log.IdCabeceraLog = cuenta.IdCuentaBancaria;
			log.IdTipoLog = "MODIFICACION";
			log.IdModulo = entrada.p_IdModulo;
			log.IdUsuario = entrada.p_IdUsuario;
			log.IdEmpresa = entrada.p_IdEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = entrada.p_IdUsuario;
			log.IdRegistro = cuenta.IdCuentaBancaria.toString();
			log.IdTipoRegistro = ID_TIPO_ENTIDAD;
			log.EstadoRegistro = entrada.p_IdEstadoAutorizacion;
			log.IdProceso = 0L;
			log.EstadoProceso = entrada.p_IdEstadoAutorizacion;			
			log.NombreProceso = "Confirmar Solicitud Cambio Cuenta Bancaria";
			return log;
		}).collect(Collectors.toList());
		
		
		entradaAgregarLog.p_LogDetalle = solicitudCambio.parallelStream().map(solicitud->{
			AgregarLog.InAgregarLog.DetalleLog detalleLog = new AgregarLog.InAgregarLog.DetalleLog();			
			detalleLog.IdTrabajo = 0L; 
			detalleLog.IdCabeceraLog = solicitud.IdCuentaBancaria;			
			detalleLog.SubRegistro = solicitud.IdTipoEntidad;
			detalleLog.CampoRegistro = solicitud.CampoModificado;
			detalleLog.ValorAntiguo = solicitud.ValorAntiguo; 
			detalleLog.ValorNuevo = solicitud.ValorNuevo;	
			return detalleLog;
		}).collect(Collectors.toList());
		
		
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::ConfirmarModificarPersonaCuentaBancariaMasivo.agregarLog", fin - inicio);
	}
	
	

	// --------------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	// --------------------------------------------------	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();
			//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CuentaBancaria\":[{\"IdCuentaBancaria\":387}],\"p_IdEstadoAutorizacion\":\"ING\"}";
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":5,\"p_IdModulo\":\"SOC\",\"p_UsaReglasNegocio\":\"S\",\"p_CuentaBancaria\":[{\"IdCuentaBancaria\":388},{\"IdCuentaBancaria\":387}],\"p_IdEstadoAutorizacion\":\"ING\"}";

			EntradaConfirmarModificarPersonaCuentaBancaria Objin = gson.fromJson(json, EntradaConfirmarModificarPersonaCuentaBancaria.class);
			
			ConfirmarModificarPersonaCuentaBancaria demo = new ConfirmarModificarPersonaCuentaBancaria();
 			System.out.println("ENTRADA");
 			System.out.println(Objin.toString());
 			System.out.println("SALIDA");
	 		
 			try {
 				SalidaConfirmarModificarPersonaCuentaBancaria ObjOut = demo.logica(con, Objin);
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
