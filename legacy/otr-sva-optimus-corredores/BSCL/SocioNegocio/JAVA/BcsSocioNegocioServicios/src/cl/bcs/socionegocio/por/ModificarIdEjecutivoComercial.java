package cl.bcs.socionegocio.por;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.portafolio.CrearPortafolio.DetalleLog;
import cl.bcs.portafolio.CrearPortafolio.EmpleadoEjecutivo;
import cl.bcs.portafolio.CrearPortafolio.PortafolioLogAuditoria;
import cl.bcs.socionegocio.per.ModificarConyuge.EntradaConyuge;
import cl.bcs.socionegocio.wbs.WebServiceUtils.SalidaWBS;
import cl.bcs.socionegocio.wbs.pojo.EntradaAdministrarWS;
import cl.bcs.socionegocio.wbs.pojo.SalidaAdministrarWS;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ModificarIdEjecutivoComercial implements JavaCall {
	static final Logger logger = LogManager.getLogger(ModificarIdEjecutivoComercial.class);
 
	public final String SQL_CONSULTAR_PORTAFOLIO_LOG_AUDITORIA = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectPortafolioLogAuditoria.sql");
	public final String SQL_INSERT_PORTAFOLIO_GLOB_DETALLE_LOG = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_InsertTempDetalleLog.sql");	
	public final String SQL_CALL_AGREGAR_LOG = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_CallAgregarLogGenerico.sql");
	

	private String SQL_SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/portafolio/sql/ModificarIdEjecutivoComercial_Select_portafolio.sql");
	private String SQL_SELECT_EMPLEADO = loadResourceToString("cl/bcs/portafolio/sql/ModificarIdEjecutivoComercial_SelectEmpleado.sql");
	private String SQL_SELECT_PORTAFOLIO_PERSONA = loadResourceToString("cl/bcs/portafolio/sql/ModificarIdEjecutivoComercial_Select_Empleado_persona.sql");
	private String SQL_UPDATE_IDEJECUTICOCOMERCIAL = loadResourceToString("cl/bcs/portafolio/sql/ModificarIdEjecutivoComercial_Update_Ejecutivo_comercial.sql");
	
	public final String SQL_CONSULTAR_EMPLEADO_EJECUTIVO = loadResourceToString("cl/bcs/portafolio/sql/CrearPortafolio_SelectEmpleadoEjecutivo.sql");
	
	public static class EntradaModificarIdEjecutivoComercial extends ModificarIdEjecutivoComercial {
		public Long p_IdPortafolio;
		public Long p_IdEjecutivoComercial;
		public Long p_IdEmpresa;
		public Long p_IdPersona;
		public Long p_IdUsuario;
		
		public EntradaModificarIdEjecutivoComercial() {
			this.p_IdPortafolio = 0L;
			this.p_IdEjecutivoComercial =0L;
			this.p_IdEmpresa =1L;
			this.p_IdPersona = 0L;
			
		}
	}
	
	public static class SalidaEjecutarCambioEjecutivo extends BcsPojo {
		public Long 				P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> 	P_OUT_MENSAJES;		
		public Long	 				P_OUT_REFID;
		
		public SalidaEjecutarCambioEjecutivo(){
			this.P_OUT_ERRORESCONTADOR 	= 0L;
			this.P_OUT_MENSAJES 		= new ArrayList<MensajeSalida>();			
		}
	}
	public static class PortafolioModificarIdEjecutivoComercial {
		public Long 	IdPortafolio;
		public String	CodigoPortafolio;
		public Long		IdEmpleadoEjecutivoComercial;
		public Long 	IdCliente;
		
	
	}
	
	public static class PortafolioModificarIdEmpleado {
		public Long     IdEmpleado;
		public String   IdEstado;
		public String   CodigoTipoCargoEmpleado;
		
	}
	
	public String toString() {
		return Utils.printJson(this, false);
		}	
	public static class PortafolioClienteEmp {
		public Long  IdPortafolio;
		public String  CodigoPortafolio ;
		public Long  IdCliente ;
		public Long  IdEmpleadoEjecutivo ;	
		public String  NombreCompleto;
		public String IdTipoDocumentoIdentidad;
		public String DocumentoIdentidad; 
		
	}
	
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();	
		EntradaModificarIdEjecutivoComercial jin = gson.fromJson(in.toString(), EntradaModificarIdEjecutivoComercial.class);
		SalidaEjecutarCambioEjecutivo out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	private SalidaAdministrarWS  validaPortafolio (Connection conn, Long idPortafolio, EntradaModificarIdEjecutivoComercial entrada) throws Exception {
		SalidaAdministrarWS salidaValidacion = new SalidaAdministrarWS();
		logger.debug("INICIO::validaPortafolio");
		long inicio = System.currentTimeMillis();

    	HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("idPortafolio", idPortafolio);
		
		SQL sql = new SQL();
		List<PortafolioModificarIdEjecutivoComercial> ExisteIdPortafolio = sql.query(conn, SQL_SELECT_PORTAFOLIO, params, PortafolioModificarIdEjecutivoComercial.class);

		if (ExisteIdPortafolio.isEmpty()){
			salidaValidacion.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",null,conn,entrada.p_IdEmpresa ));
			salidaValidacion.P_OUT_ERRORESCONTADOR ++;
			return salidaValidacion;
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::validaPortafolio", fin - inicio);
		return salidaValidacion;		
	}
	private SalidaAdministrarWS  validaEmpleado (Connection conn, Long idEmpleado, EntradaModificarIdEjecutivoComercial entrada) throws Exception {
		SalidaAdministrarWS salidaValidacion = new SalidaAdministrarWS();
		logger.debug("INICIO::validaPortafolio");
		long inicio = System.currentTimeMillis();

    	HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("idEmpleado", idEmpleado);
		
		SQL sql = new SQL();
		List<PortafolioModificarIdEmpleado> ExisteIdEmpleado = sql.query(conn, SQL_SELECT_EMPLEADO, params, PortafolioModificarIdEmpleado.class);
		if (ExisteIdEmpleado.isEmpty()){
			salidaValidacion.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"UI_ERROR_CARGAR_EJECUTIVO_COMERCIAL",null,conn,entrada.p_IdEmpresa ));
			salidaValidacion.P_OUT_ERRORESCONTADOR ++;
			return salidaValidacion;
		}
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::validaPortafolio", fin - inicio);
		return salidaValidacion;
	
	}
	

	private List<Long> BuscaIdCliente (Connection conn, Long idPortafolio, EntradaModificarIdEjecutivoComercial entrada) throws Exception {
		List <Long> salidaValidacion = new ArrayList<>();
		logger.debug("INICIO::validaPortafolio");
		long inicio = System.currentTimeMillis();

    	HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("idPortafolio", idPortafolio);
		
		SQL sql = new SQL();
		List<PortafolioModificarIdEjecutivoComercial> IdClientePortafolio = sql.query(conn, SQL_SELECT_PORTAFOLIO, params, PortafolioModificarIdEjecutivoComercial.class);
		for(PortafolioModificarIdEjecutivoComercial IdClientePort :  IdClientePortafolio ) {
			salidaValidacion.add(IdClientePort.IdCliente);
		}
		return salidaValidacion;		
	}
	private List<Long> BuscaIdEjecutuvoAnterior (Connection conn, Long idPortafolio, EntradaModificarIdEjecutivoComercial entrada) throws Exception {
		List <Long> salida = new ArrayList<>();
		logger.debug("INICIO::validaPortafolio");
		long inicio = System.currentTimeMillis();

    	HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("idPortafolio", idPortafolio);
		
		SQL sql = new SQL();
		List<PortafolioModificarIdEjecutivoComercial> IdClientePortafolio = sql.query(conn, SQL_SELECT_PORTAFOLIO, params, PortafolioModificarIdEjecutivoComercial.class);
		for(PortafolioModificarIdEjecutivoComercial IdClientePort :  IdClientePortafolio ) {
			salida.add(IdClientePort.IdEmpleadoEjecutivoComercial);
		}
		return salida;		
	}
	
	public SalidaEjecutarCambioEjecutivo logica(Connection conn, EntradaModificarIdEjecutivoComercial entrada)  throws Exception {
		logger.info("INICIO::ModificarIdEjecutivoComercial");
		long inicioServicio = System.currentTimeMillis();
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		SalidaEjecutarCambioEjecutivo salida =  new SalidaEjecutarCambioEjecutivo();
		SalidaAdministrarWS outPortafolio = new SalidaAdministrarWS();
		SalidaAdministrarWS outEmpleado = new SalidaAdministrarWS();		

		Long IdEjecutivoComenrcialAnerior=0L;
		String IdTipoLog ="MODIFICACION"; 
		
		HashMap<String, Object> params = new HashMap<String, Object>(2);
		params.put("idPersona", entrada.p_IdPersona);		
		SQL sql = new SQL();
		List<PortafolioClienteEmp> BuscaPortafolioPorPersona = sql.query(conn, SQL_SELECT_PORTAFOLIO_PERSONA, params, PortafolioClienteEmp.class);
	
		
		int [] res = null;
		//valida portafolio
		if ( entrada.p_IdPortafolio == null||entrada.p_IdPortafolio == 0) {	
			outPortafolio.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ENTIDAD_NO_EXISTE",null,conn,entrada.p_IdEmpresa ));
			outPortafolio.P_OUT_ERRORESCONTADOR ++;
		}else {
			outPortafolio= validaPortafolio(conn,entrada.p_IdPortafolio,entrada );
           
		}
		
      // valida ejeutivo comercial si no hay coloca el id del otro
		if (entrada.p_IdEjecutivoComercial == null || entrada.p_IdEjecutivoComercial == 0) {	
			for ( PortafolioClienteEmp EjecutivoEmpleado : BuscaPortafolioPorPersona  ) {
				if (EjecutivoEmpleado.IdPortafolio.equals(entrada.p_IdPortafolio))
					if (EjecutivoEmpleado.IdEmpleadoEjecutivo !=null) {
						entrada.p_IdEjecutivoComercial= EjecutivoEmpleado.IdEmpleadoEjecutivo;
					}else {
						entrada.p_IdEjecutivoComercial=0L;
					}
			}
				
		}else {
			outEmpleado= validaEmpleado(conn,entrada.p_IdEjecutivoComercial,entrada );				
		}
			
		List<PortafolioModificarIdEjecutivoComercial> parametros =  new ArrayList<PortafolioModificarIdEjecutivoComercial>();			
		
		if (outPortafolio.P_OUT_ERRORESCONTADOR == 0) {
			List<Long> ListaEmpAnterior	= BuscaIdEjecutuvoAnterior(conn,entrada.p_IdPortafolio,entrada );
			IdEjecutivoComenrcialAnerior =	ListaEmpAnterior.get(0);
		}else {
			IdEjecutivoComenrcialAnerior =0L;
			entrada.p_IdPortafolio =0L;
		}
				
		if (outPortafolio.P_OUT_ERRORESCONTADOR == 0 && outEmpleado.P_OUT_ERRORESCONTADOR== 0 ) {	
			PortafolioModificarIdEjecutivoComercial ModificarIdEjecutivoComercialPortafolio= new PortafolioModificarIdEjecutivoComercial();
				
			ModificarIdEjecutivoComercialPortafolio.IdPortafolio= entrada.p_IdPortafolio;
			ModificarIdEjecutivoComercialPortafolio.IdEmpleadoEjecutivoComercial= entrada.p_IdEjecutivoComercial;
			parametros.add(ModificarIdEjecutivoComercialPortafolio);		
						
			res = sql.batch(conn, SQL_UPDATE_IDEJECUTICOCOMERCIAL, parametros, PortafolioModificarIdEjecutivoComercial.class);
				
		 //log
			SQL sql2 = new SQL();
		
			HashMap<String,Object> paramsPortLogAud = new HashMap<>();		
			paramsPortLogAud.put("IdPortafolio", entrada.p_IdPortafolio);
			List<PortafolioLogAuditoria> portLogAuditoria = sql2.query(conn, SQL_CONSULTAR_PORTAFOLIO_LOG_AUDITORIA, paramsPortLogAud, PortafolioLogAuditoria.class);
			List<DetalleLog> lstDetalleLog = new ArrayList<DetalleLog>();
			String subregistro = "PORTAFOLIO";
			for(PortafolioLogAuditoria portLog :  portLogAuditoria ) { 
				DetalleLog detalleLog = new DetalleLog();		
				detalleLog.SubRegistro = subregistro;
				detalleLog.CampoRegistro = "IdEjecutivoComercial";
				detalleLog.ValorAntiguo = IdEjecutivoComenrcialAnerior.toString();
				detalleLog.ValorNuevo = portLog.IdEjecutivoComercial.toString();
				lstDetalleLog.add(detalleLog);
			}
	
			int[] resultInsert = sql2.batch(conn, SQL_INSERT_PORTAFOLIO_GLOB_DETALLE_LOG, lstDetalleLog, DetalleLog.class);
		
			HashMap<String,Object> paramsInAgregarLog = new HashMap<>();
			paramsInAgregarLog.put("IdCliente",BuscaPortafolioPorPersona.get(0).IdCliente );
			paramsInAgregarLog.put("NombreCliente", BuscaPortafolioPorPersona.get(0).NombreCompleto);
			paramsInAgregarLog.put("DocumentoIdentidad", BuscaPortafolioPorPersona.get(0).DocumentoIdentidad);
			paramsInAgregarLog.put("TipoDocumentoIdentidad", BuscaPortafolioPorPersona.get(0).IdTipoDocumentoIdentidad);
			paramsInAgregarLog.put("IdTipoLog", IdTipoLog);
			paramsInAgregarLog.put("IdModulo", "SOC");
			paramsInAgregarLog.put("IdEmpresa", entrada.p_IdEmpresa.toString());
			paramsInAgregarLog.put("IdUsuario", entrada.p_IdUsuario.toString());
			paramsInAgregarLog.put("FechaHoraSistema", fechaSistema);
			paramsInAgregarLog.put("TipoEntidad",subregistro );
			paramsInAgregarLog.put("DetalleLog", lstDetalleLog);
			paramsInAgregarLog.put("p_out_RefId","");
			paramsInAgregarLog.put("NombreProceso", "ModificarIdEjecutivoComercial");
			paramsInAgregarLog.put("idEstadoAutorizacion", "");
        
        
			HashMap<String, Class> out = new HashMap<>();
			sql2.call(conn, SQL_CALL_AGREGAR_LOG, paramsInAgregarLog, out);
			
		}
		
		long finServicio = System.currentTimeMillis();
		logger.info("FIN::ModificarIdEjecutivoComercial", finServicio - inicioServicio);
		MensajeSalida mensajeSalida;
		salida.P_OUT_ERRORESCONTADOR = outEmpleado.P_OUT_ERRORESCONTADOR + outPortafolio.P_OUT_ERRORESCONTADOR ;
		salida.P_OUT_MENSAJES.addAll(outPortafolio.P_OUT_MENSAJES);
		salida.P_OUT_MENSAJES.addAll(outEmpleado.P_OUT_MENSAJES);
		
		return salida;
	}
	
}