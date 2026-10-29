package cl.bcs.plataforma.pla;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class AgregarLog implements JavaCall{	
	private final Logger logger = LogManager.getLogger(AgregarLog.class);
	
	//QUERIES
	private final String SELECT_PERSONA = loadResourceToString("cl/bcs/plataforma/pla/sql/AgregarLog_SelectPersona.sql");
	private final String SELECT_CLIENTE = loadResourceToString("cl/bcs/plataforma/pla/sql/AgregarLog_SelectCliente.sql");
	private final String SELECT_PORTAFOLIO = loadResourceToString("cl/bcs/plataforma/pla/sql/AgregarLog_SelectPortafolio.sql");
	
	private final String SQL_Insert_Log = loadResourceToString("cl/bcs/plataforma/pla/sql/AgregarLog_InsertLog.sql");
//	private final String SQL_Insert_Log = "insert into \"BSCL.Plataforma.BD::PLA.AgregarLogMasivo4Java_Entrada_Log\"(\"IdTrabajo\",\"IdCabeceraLog\",\"IdTipoLog\",\"IdModulo\",\"IdUsuario\",\"IdEmpresa\",\"FechaSistemaLog\",\"IdUsuarioAtendido\",\"IdCliente\",\"EstadoCliente\",\"NombreCliente\",\"IdTipoDocumentoIdentidad\",\"IdDocumentoIdentidad\",\"IdPortafolio\",\"EstadoPortafolio\",\"IdTipoRegistro\",\"IdRegistro\",\"EstadoRegistro\",\"IdTipoOperacion\",\"Folio\",\"Cantidad\",\"PrecioTasa\",\"Monto\",\"IdMonedaOrigen\",\"IdMonedaLiquidacion\",\"TipoCambio\",\"Nemo\",\"IdProceso\",\"EstadoProceso\",\"NombreProceso\")values (:IdTrabajo,:IdCabeceraLog,:IdTipoLog,:IdModulo,:IdUsuario,:IdEmpresa,:FechaSistemaLog,:IdUsuarioAtendido,:IdCliente,:EstadoCliente,:NombreCliente,:IdTipoDocumentoIdentidad,:IdDocumentoIdentidad,:IdPortafolio,:EstadoPortafolio,:IdTipoRegistro,:IdRegistro,:EstadoRegistro,:IdTipoOperacion,:Folio,:Cantidad,:PrecioTasa,:Monto,:IdMonedaOrigen,:IdMonedaLiquidacion,:TipoCambio,:Nemo,:IdProceso,:EstadoProceso,:NombreProceso)";
//	private final String SQL_Insert_DetalleLog = "insert into \"BSCL.Plataforma.BD::PLA.AgregarLogMasivo4Java_Entrada_LogDetalle\"(\"IdTrabajo\",\"IdCabeceraLog\",\"SubRegistro\",\"CampoRegistro\",\"ValorAntiguo\",\"ValorNuevo\")values (:IdTrabajo,:IdCabeceraLog,:SubRegistro,:CampoRegistro,:ValorAntiguo,:ValorNuevo)";
	private final String SQL_Insert_DetalleLog = loadResourceToString("cl/bcs/plataforma/pla/sql/AgregarLog_InsertDetalleLog.sql");
	private final String SQL_AgregarLogMasivo = "CALL \"BSCL.Plataforma.BD.Servicios::AgregarLogMasivo4Java\"(p_IdTrabajo=>:p_IdTrabajo)";
	
	//ENTRADA Y SALIDA DE LA CLASE
	public static class InAgregarLog{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_UsaReglasNegocio;
		public String p_IdEstadoAutorizacion;
		public Long p_IdTrabajo = 0L ;
		public List<InAgregarLog.Log> p_Log;
		public List<InAgregarLog.DetalleLog> p_LogDetalle;
		public InAgregarLog(){
			p_Log = new ArrayList<InAgregarLog.Log>();
			p_LogDetalle = new ArrayList<InAgregarLog.DetalleLog>();
		}
		
		public void agregarLogBasico(Long idEmpresa, Long idUsuario, String idModulo, Long idCabeceraLog, Long idTrabajo, String idTipoLog, String idTipoRegistro, String fechaLog, String nombreProceso) {
			InAgregarLog.Log l1 = new InAgregarLog.Log();
			l1.IdEmpresa = idEmpresa;
			l1.IdModulo = idModulo;
			l1.IdUsuario = idUsuario;
			
			l1.IdCabeceraLog = idCabeceraLog;
			l1.IdTrabajo = idTrabajo;
			l1.IdTipoLog = idTipoLog;
			l1.IdTipoRegistro= idTipoRegistro;
			l1.FechaSistemaLog = fechaLog;
			l1.NombreProceso= nombreProceso;
			p_Log.add(l1);
		}
		
		public void agregarDetalleLogBasico(Long idCabeceraLog, Long idTrabajo, String campoRegistro, String valorAntiguo, String valorNuevo) {
			InAgregarLog.DetalleLog dl2 = new InAgregarLog.DetalleLog();
			dl2.IdCabeceraLog = idCabeceraLog;
			dl2.IdTrabajo = idTrabajo;
			dl2.CampoRegistro = campoRegistro;
			dl2.ValorAntiguo = valorAntiguo;
			dl2.ValorNuevo = valorNuevo;
			p_LogDetalle.add(dl2);
		}
		
		public void agregarDetalleLogBasico(Long idCabeceraLog, Long idTrabajo, String campoRegistro, String valorAntiguo, String valorNuevo, String subRegistro) {
			InAgregarLog.DetalleLog dl2 = new InAgregarLog.DetalleLog();
			dl2.IdCabeceraLog = idCabeceraLog;
			dl2.IdTrabajo = idTrabajo;
			dl2.CampoRegistro = campoRegistro;
			dl2.ValorAntiguo = valorAntiguo;
			dl2.ValorNuevo = valorNuevo;
			dl2.SubRegistro = subRegistro;
			p_LogDetalle.add(dl2);
		}
		
		public static class Log {
			public Long			IdTrabajo; 
			public Long			IdCabeceraLog; 
			public String		IdTipoLog; 
			public String		IdModulo; 
			public Long			IdUsuario; 
			public Long			IdEmpresa; 
			public String		FechaSistemaLog; 
			public Long			IdUsuarioAtendido; 
			public Long			IdCliente; 
			public String		EstadoCliente; 
			public String		NombreCliente; 
			public String		IdTipoDocumentoIdentidad; 
			public String		IdDocumentoIdentidad; 
			public Long			IdPortafolio; 
			public String		EstadoPortafolio; 
			public String		IdTipoRegistro; 
			public String		IdRegistro; 
			public String		EstadoRegistro; 
			public String		IdTipoOperacion; 
			public Long			Folio; 
			public BigDecimal	Cantidad; 
			public BigDecimal	PrecioTasa; 
			public BigDecimal	Monto; 
			public String		IdMonedaOrigen; 
			public String		IdMonedaLiquidacion; 
			public String		TipoCambio; 
			public String		Nemo; 
			public Long			IdProceso; 
			public String		EstadoProceso; 
			public String		NombreProceso; 
			public Long			IdPersona;
			
			public Log() {
				 this.IdTrabajo = 0L;  
				 this.IdCliente = 0L; 
				 this.EstadoCliente = ""; 
				 this.NombreCliente = ""; 
				 this.IdTipoDocumentoIdentidad = ""; 
				 this.IdDocumentoIdentidad = ""; 
				 this.IdPortafolio = 0L; 
				 this.EstadoPortafolio = "";  
				 this.IdRegistro = ""; 
				 this.EstadoRegistro = ""; 
				 this.IdTipoOperacion = ""; 
				 this.Folio = 0L; 
				 this.Cantidad = BigDecimal.ZERO; 
				 this.PrecioTasa = BigDecimal.ZERO; 
				 this.Monto = BigDecimal.ZERO; 
				 this.IdMonedaOrigen = ""; 
				 this.IdMonedaLiquidacion = ""; 
				 this.TipoCambio = ""; 
				 this.Nemo = ""; 
				 this.IdProceso = 0L; 
				 this.EstadoProceso = ""; 
				 this.NombreProceso = ""; 
				 this.IdPersona = 0L;
			}
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		public static class DetalleLog {
			public Long			IdTrabajo; 
			public Long			IdCabeceraLog; 
			public String		SubRegistro; 
			public String		CampoRegistro; 
			public String		ValorAntiguo; 
			public String		ValorNuevo; 
			
			public String toString() {
				return Utils.printJson(this, false);
			}
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Persona{
		public Long IdPersona=0L;
		public String NombreCompleto="";
		public String DocumentoIdentidad="";
		public String TipoDocumentoIdentidad="";
	}
	
	public static class Cliente extends Persona{
		public Long IdCliente=0L;
		public String EstadoCliente="";
	}
	
	public static class Portafolio extends Cliente{
		public Long IdPortafolio=0L;
		public String CodigoPortafolio="";
		public String EstadoPortafolio="";
	}

	public static class OutAgregarLog{
		public Long P_OUT_ERRORESCONTADOR;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	/**********************
	 * métodos básicos
	 **********************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InAgregarLog in=gson.fromJson(jin.toString() ,InAgregarLog.class);
		OutAgregarLog out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}

	@Trace
	public OutAgregarLog logica(Connection con, InAgregarLog entrada) throws Exception {
		logger.info("INICIO:AgregarLog");
		long inicio = System.currentTimeMillis();
		
		OutAgregarLog salida 			= new OutAgregarLog();
		salida.P_OUT_ERRORESCONTADOR 	= new Long(0);
		
		SQL sql 						= new SQL();		

		if(entrada.p_Log.size() == 0) {
			return salida;
		}
		
		Long IdTrabajo = (long)(Math.random()*10000);
		
		ArrayList<InAgregarLog.Log> logCabeceras = prepararCabeceraLog(con, sql, entrada, IdTrabajo);
		HashMap<Long,Object> hCabeceraLog = Utils.List2Hash(logCabeceras, "IdCabeceraLog");
		ArrayList<InAgregarLog.DetalleLog> logDetalle = prepararDetalleLog(entrada, hCabeceraLog);
		
		HashMap<String,Class> parmssalidaSP	= new HashMap<String,Class>();
		
		try {
			//Insertamos en tablas temporales
			sql.batch(con,SQL_Insert_Log, logCabeceras, InAgregarLog.Log.class);
			if(!(logDetalle.isEmpty())) {
				sql.batch(con,SQL_Insert_DetalleLog, logDetalle, InAgregarLog.DetalleLog.class);
			}
			//Servicio envia a tablas fisicas
			HashMap<String,Object> parmsIn 		= new HashMap<String,Object>();	
			parmsIn.put("p_IdTrabajo", IdTrabajo);
			HashMap<String,Object> respuestaSP = sql.call(con, SQL_AgregarLogMasivo, parmsIn, parmssalidaSP);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		long fin = System.currentTimeMillis();
		logger.info("FIN:AgregarLog", fin - inicio);
		return salida;
	}	
	
	
	private HashMap<Long, Object> hListadoPersona(Connection con, SQL sql,  List<Long> idsPersona){
		logger.trace("INICIO::AgregarLog.hListadoPersona");
		long inicio = System.currentTimeMillis();
		HashMap<Long, Object> hPersona = new HashMap<Long, Object>();
		if(!(idsPersona.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdPersona", idsPersona);
			try {
				List<Persona> listadoPersona = sql.query(con, SELECT_PERSONA, params, Persona.class);
				if(!(listadoPersona.isEmpty())) {
					hPersona = Utils.List2Hash(listadoPersona, "IdPersona");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AgregarLog.hListadoPersona", fin - inicio);
		return hPersona;
	}
	
	private HashMap<Long, Object> hListadoCliente(Connection con, SQL sql, List<Long> idsCliente){
		logger.trace("INICIO::AgregarLog.hListadoCliente");
		long inicio = System.currentTimeMillis();
		HashMap<Long, Object> hCliente = new HashMap<Long, Object>();
		if(!(idsCliente.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdCliente", idsCliente);
			try {
				List<Cliente> listadoCliente = sql.query(con, SELECT_CLIENTE, params, Cliente.class);
				if(!(listadoCliente.isEmpty())) {
					hCliente = Utils.List2Hash(listadoCliente, "IdCliente");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AgregarLog.hListadoCliente", fin - inicio);
		return hCliente;
	}
	
	
	private HashMap<Long, Object> hListadoPortafolio(Connection con, SQL sql, List<Long> idsPortafolio){
		logger.trace("INICIO::AgregarLog.hListadoPortafolio");
		long inicio = System.currentTimeMillis();
		HashMap<Long, Object> hPortafolio = new HashMap<Long, Object>();
		if(!(idsPortafolio.isEmpty())) {
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("IdPortafolio", idsPortafolio);
			try {
				List<Portafolio> listadoPortafolio = sql.query(con, SELECT_PORTAFOLIO, params, Portafolio.class);
				if(!(listadoPortafolio.isEmpty())) {
					hPortafolio = Utils.List2Hash(listadoPortafolio, "IdPortafolio");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AgregarLog.hListadoPortafolio", fin - inicio);
		return hPortafolio;
	}
	
	private ArrayList<InAgregarLog.Log> prepararCabeceraLog(Connection con, SQL sql, InAgregarLog entrada, Long IdTrabajo){
		logger.trace("INICIO::AgregarLog.prepararCabeceraLog");
		long inicio = System.currentTimeMillis();
		
		ArrayList<InAgregarLog.Log> listadoCabeceraLog = new ArrayList<InAgregarLog.Log>();
		List<Long> idsPersona = new ArrayList<Long>();
		List<Long> idsCliente = new ArrayList<Long>();
		List<Long> idsPortafolios = new ArrayList<Long>();
		
		for(InAgregarLog.Log log : entrada.p_Log) {
			if(log.IdPersona!=null && !(log.IdPersona.compareTo(0L) == 0) && !(idsPersona.contains(log.IdPersona))) {
				idsPersona.add(log.IdPersona);
			}
			if(log.IdCliente!=null && !(log.IdCliente.compareTo(0L) == 0) && !(idsCliente.contains(log.IdCliente))) {
				idsCliente.add(log.IdCliente);
			}
			if(log.IdPortafolio!=null && !(log.IdPortafolio.compareTo(0L) == 0) && !(idsPortafolios.contains(log.IdPortafolio))) {
				idsPortafolios.add(log.IdPortafolio);
			}
		}
		
		HashMap<Long, Object> hPersona = hListadoPersona(con, sql, idsPersona);
		HashMap<Long, Object> hCliente = hListadoCliente(con, sql, idsCliente);
		HashMap<Long, Object> hPortafolio = hListadoPortafolio(con, sql, idsPortafolios);
		
		for(InAgregarLog.Log log : entrada.p_Log) {
			if(log.IdCabeceraLog!=null && !(log.IdCabeceraLog.compareTo(0L) == 0)) {
				InAgregarLog.Log Objectlog = new InAgregarLog.Log();
				Objectlog.IdCabeceraLog = log.IdCabeceraLog;
				Objectlog.IdTipoLog = (log.IdTipoLog!= null)?log.IdTipoLog: "";
				Objectlog.IdTipoRegistro = (log.IdTipoRegistro!= null)?log.IdTipoRegistro: "";
				if(verificarExistenciaCabeceraLog(Objectlog, listadoCabeceraLog) == false) {
					Objectlog.IdTrabajo = IdTrabajo;
					Objectlog.IdModulo = (entrada.p_IdModulo!=null)? entrada.p_IdModulo : "";
					Objectlog.IdModulo = (log.IdModulo!= null)? log.IdModulo : Objectlog.IdModulo;
					Objectlog.IdUsuario = (entrada.p_IdUsuario!=null)? entrada.p_IdUsuario: 0L;
					Objectlog.IdUsuario = (log.IdUsuario!= null)? log.IdUsuario : Objectlog.IdUsuario;
					Objectlog.IdEmpresa = (entrada.p_IdEmpresa!=null)? entrada.p_IdEmpresa: 0L;
					Objectlog.IdEmpresa = (log.IdEmpresa!=null) ? log.IdEmpresa: Objectlog.IdEmpresa;
					Objectlog.FechaSistemaLog = (log.FechaSistemaLog!=null)? log.FechaSistemaLog: "";
					Objectlog.IdUsuarioAtendido = (entrada.p_IdUsuario!=null)? entrada.p_IdUsuario : 0L;
					Objectlog.IdUsuarioAtendido = (log.IdUsuarioAtendido!=null)? log.IdUsuarioAtendido : Objectlog.IdUsuarioAtendido;
					if(log.IdPersona!=null && !(log.IdPersona.compareTo(0L)==0)){
						Objectlog.IdPersona = log.IdPersona;
						Persona persona = (Persona) hPersona.get(log.IdPersona);
						if(persona!=null) {
							Objectlog.NombreCliente = (persona.NombreCompleto!=null)? persona.NombreCompleto : "";
							Objectlog.IdDocumentoIdentidad = (persona.DocumentoIdentidad!=null) ? persona.DocumentoIdentidad : "";
							Objectlog.IdTipoDocumentoIdentidad = (persona.TipoDocumentoIdentidad!=null) ? persona.TipoDocumentoIdentidad : "";
						}
					}
					if(log.IdCliente!=null && !(log.IdCliente.compareTo(0L) == 0) && (Objectlog.IdPersona.compareTo(0L) == 0)){
						Objectlog.IdCliente = log.IdCliente;
						Cliente cliente = (Cliente) hCliente.get(log.IdCliente);
						if(cliente!=null) {
							Objectlog.NombreCliente = (cliente.NombreCompleto!=null)? cliente.NombreCompleto : "";
							Objectlog.IdDocumentoIdentidad = (cliente.DocumentoIdentidad!=null) ? cliente.DocumentoIdentidad : "";
							Objectlog.IdTipoDocumentoIdentidad = (cliente.TipoDocumentoIdentidad!=null) ? cliente.TipoDocumentoIdentidad : "";
							Objectlog.EstadoCliente = (cliente.EstadoCliente!=null)?cliente.EstadoCliente : "";
						}
					}
					if(log.IdPortafolio!=null && !(log.IdPortafolio.compareTo(0L) == 0) && (Objectlog.IdPersona.compareTo(0L) == 0) && (Objectlog.IdCliente.compareTo(0L) == 0)) {
						Objectlog.IdPortafolio =log.IdPortafolio;
						Portafolio portafolio = (Portafolio) hPortafolio.get(log.IdPortafolio);
						if(portafolio != null) {
							Objectlog.NombreCliente = (portafolio.NombreCompleto!=null)? portafolio.NombreCompleto : "";
							Objectlog.IdDocumentoIdentidad = (portafolio.DocumentoIdentidad!=null) ? portafolio.DocumentoIdentidad : "";
							Objectlog.IdTipoDocumentoIdentidad = (portafolio.TipoDocumentoIdentidad!=null) ? portafolio.TipoDocumentoIdentidad : "";
							Objectlog.EstadoPortafolio = (portafolio.EstadoPortafolio!=null) ? portafolio.EstadoPortafolio: "";
						}
					}
					else {
						if((log.IdPortafolio!=null && !(log.IdPortafolio.compareTo(0L) == 0))) {
							Objectlog.IdPortafolio =log.IdPortafolio;
						}
					}
					Objectlog.IdRegistro = log.IdRegistro;
					Objectlog.EstadoRegistro = (entrada.p_IdEstadoAutorizacion!=null && !(entrada.p_IdEstadoAutorizacion.trim().equals("")))? entrada.p_IdEstadoAutorizacion : ""; 
					Objectlog.EstadoRegistro = (!log.EstadoRegistro.trim().equalsIgnoreCase("")) ? log.EstadoRegistro : Objectlog.EstadoRegistro;
					Objectlog.IdTipoOperacion = log.IdTipoOperacion;
					Objectlog.Folio = log.Folio;
					Objectlog.Cantidad = log.Cantidad;
					Objectlog.PrecioTasa = log.PrecioTasa;
					Objectlog.Monto = log.Monto;
					Objectlog.IdMonedaOrigen = log.IdMonedaOrigen;
					Objectlog.IdMonedaLiquidacion = log.IdMonedaLiquidacion;
					Objectlog.TipoCambio = log.TipoCambio;
					Objectlog.Nemo = log.Nemo;
					Objectlog.IdProceso = log.IdProceso;
					Objectlog.EstadoProceso = (entrada.p_IdEstadoAutorizacion!=null && !(entrada.p_IdEstadoAutorizacion.trim().equals("")))? entrada.p_IdEstadoAutorizacion : ""; 
					Objectlog.EstadoProceso = (!log.EstadoProceso.trim().equals("")) ? log.EstadoProceso: Objectlog.EstadoProceso;
					Objectlog.NombreProceso = log.NombreProceso;
					listadoCabeceraLog.add(Objectlog);
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AgregarLog.prepararCabeceraLog", fin - inicio);
		return listadoCabeceraLog;
	}
	
	private boolean verificarExistenciaCabeceraLog(AgregarLog.InAgregarLog.Log log, List<AgregarLog.InAgregarLog.Log> ListaCabeceraLog) {
		logger.trace("INICIO::AgregarLog.verificarExistenciaCabeceraLog");
		long inicio = System.currentTimeMillis();
		boolean resultado = false;
		if(!(ListaCabeceraLog.isEmpty())) {
			for(AgregarLog.InAgregarLog.Log cabeceraLog : ListaCabeceraLog) {
				if( (log != null && log.IdCabeceraLog != null && log.IdCabeceraLog.compareTo(cabeceraLog.IdCabeceraLog)== 0) && (log.IdTipoLog.equalsIgnoreCase(cabeceraLog.IdTipoLog)) 
					&& (log.IdTipoRegistro.equalsIgnoreCase(cabeceraLog.IdTipoRegistro))) {
					resultado = true; 
					break;
				}
			}
		}
		long fin = System.currentTimeMillis();
		logger.trace("FIN::AgregarLog.verificarExistenciaCabeceraLog", fin - inicio);
		return resultado;
	}
	
	
	private ArrayList<InAgregarLog.DetalleLog> prepararDetalleLog(InAgregarLog entrada, HashMap<Long, Object> hashCabecerasLog){
		logger.trace("INICIO::AgregarLog.prepararDetalleLog");
		long inicio = System.currentTimeMillis();
		
		ArrayList<InAgregarLog.DetalleLog> listadoDetalleLog = new ArrayList<InAgregarLog.DetalleLog>();
		
		for(InAgregarLog.DetalleLog detalleLog : entrada.p_LogDetalle) {
			if(detalleLog.IdCabeceraLog!=null && !(detalleLog.IdCabeceraLog.compareTo(0L) == 0)) {
				InAgregarLog.Log log =  (AgregarLog.InAgregarLog.Log) hashCabecerasLog.get(detalleLog.IdCabeceraLog);
				if(log!=null) {
					InAgregarLog.DetalleLog ObjectdetalleLog = new InAgregarLog.DetalleLog();
					ObjectdetalleLog.IdTrabajo = log.IdTrabajo;
					ObjectdetalleLog.IdCabeceraLog = log.IdCabeceraLog;
					ObjectdetalleLog.SubRegistro = detalleLog.SubRegistro;
					ObjectdetalleLog.CampoRegistro = detalleLog.CampoRegistro;
					ObjectdetalleLog.ValorAntiguo = detalleLog.ValorAntiguo;
					if (detalleLog.ValorNuevo!=null && detalleLog.ValorNuevo.length()>5000)
						detalleLog.ValorNuevo = detalleLog.ValorNuevo.substring(0,5000);
					ObjectdetalleLog.ValorNuevo = detalleLog.ValorNuevo;
					listadoDetalleLog.add(ObjectdetalleLog);
				}
			}
		}
		
		long fin = System.currentTimeMillis();
		logger.trace("INICIO::AgregarLog.prepararDetalleLog", fin - inicio);
		return listadoDetalleLog;
	}
	/**********************
	 * main pruebas
	 **********************/
	public static void main(String[] args){
		AgregarLog o = new AgregarLog();
		//Settings by file
        //prueba de la clase
		try (Connection con = PlataformaUtils.getPropertiesConnection("fynsapre","connections.properties")){
			InAgregarLog entrada = new InAgregarLog();
			entrada.p_IdTrabajo =  1L;
			
			InAgregarLog.Log l1 = new InAgregarLog.Log();
			
			l1.IdCabeceraLog = 405358L;
			l1.IdTipoLog = "INGRESO";
			l1.IdEmpresa = 1L;
			l1.IdModulo = "FMM";
			l1.IdTipoRegistro="SOLICITUD_APORTE_RESCATE";
			l1.IdUsuario = 9L;
			l1.IdProceso = 0L;
			l1.IdTrabajo = entrada.p_IdTrabajo;
			l1.FechaSistemaLog = "2020-06-05";
			l1.NombreProceso="PRUEBA PABLO";
			
			InAgregarLog.DetalleLog dl1= new InAgregarLog.DetalleLog();
			dl1.IdCabeceraLog = 405358L;
			dl1.IdTrabajo = entrada.p_IdTrabajo;
			dl1.ValorNuevo = "Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 Ejemplo 1 ";
			
			InAgregarLog.Log l2 = new InAgregarLog.Log();
			
			l2.IdCabeceraLog = 405359L;
			l2.IdTipoLog = "INFORMATIVO";
			l2.IdEmpresa = 1L;
			l2.IdModulo = "FMM";
			l2.IdTipoRegistro="SOLICITUD_APORTE_RESCATE";
			l2.IdUsuario = 1L;
			l2.IdProceso = 0L;
			l2.IdTrabajo = entrada.p_IdTrabajo;
			l2.FechaSistemaLog = "2020-06-05";
			l2.NombreProceso="PRUEBA PABLO";
			
			InAgregarLog.DetalleLog dl2 = new InAgregarLog.DetalleLog();
			dl2.IdCabeceraLog = new Long(1);
			dl2.IdTrabajo = entrada.p_IdTrabajo;
			dl2.ValorNuevo = "Ejemplo 2";
			
			entrada.p_Log.add(l1);
			entrada.p_LogDetalle.add(dl1);
			
			entrada.p_Log.add(l2);
			entrada.p_LogDetalle.add(dl2);
			
			o.logger.debug("Entrada:");
			o.logger.debug(entrada);
			o.logger.debug("Salida:");
			o.logger.debug(o.logica(con,entrada));
			con.commit();
		}
		catch(Exception e){
			e.printStackTrace();
		}		
	}
}
