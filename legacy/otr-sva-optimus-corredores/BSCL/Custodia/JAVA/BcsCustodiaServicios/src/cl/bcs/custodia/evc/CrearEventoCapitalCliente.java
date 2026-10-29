package cl.bcs.custodia.evc;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.ConsultarFlujoCustodia;
import cl.bcs.custodia.pojo.EventoCapital;
import cl.bcs.custodia.pojo.EventoCapitalCliente;
import cl.bcs.custodia.pojo.EventoCapitalClienteFormaDePago;
import cl.bcs.custodia.pojo.PreferenciaPago;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.DetalleLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CrearEventoCapitalCliente implements JavaCall{
	private final Log log = new Log("CUS.CrearEventoCapitalCliente");
	static final Logger logger	= LogManager.getLogger(CrearEventoCapitalCliente.class);
	
	private final String SQL_SELECT_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapitalCliente_SelectEventoCapital.sql");
	private final String SQL_SELECT_CLIENTE = loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapitalCliente_SelectCliente.sql");
	private final String SQL_SELECT_EMPLEADO = loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapitalCliente_SelectEmpleado.sql");
	private final String SQL_SELECT_PREFERENCIA_DE_PAGO = loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapitalCliente_SelectPreferenciaPago.sql");	
	private final String SQL_INSERT_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapitalCliente_InsertEventoCapital.sql");
	private final String SQL_INSERT_EVENTO_CAPITAL_FORMA_DE_PAGO = loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapitalCliente_InsertEventoCapitalFormaDePago.sql");	

	private final String SQL_UPDATE_EVENTO_CAPITAL = loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapitalCliente_UpdateEventoCapital.sql");
	private final String SQL_SELECT_CONTRAPARTE = loadResourceToString("cl/bcs/custodia/sql/CrearEventoCapitalCliente_SelectContraparte.sql") ;
	
	
	
	public static class EventoCapitalCliente_IN {
		public Long p_IdEmpresa;
	    public Long	p_IdUsuario;
	    public String p_IdModulo;
	    public String p_UsaReglasNegocio;
	    public List<EventoCL> p_EventoCapital;
	    	    
	    public EventoCapitalCliente_IN() {
			this.p_EventoCapital = new ArrayList<EventoCL>();
		}

		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class EventoCapitalCliente_OUT {
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public List<Long>			P_OUT_SECUENCIA;
		
		public EventoCapitalCliente_OUT() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_SECUENCIA = new ArrayList<Long>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class EventoCL extends BcsPojo{
		public String 	p_IdTipoEventoCapital;//				NVARCHAR(2) 	default '',
		public String 	p_IdClasificacionTipoInstrumento;//						NVARCHAR(3) 	default '',
		public String	p_IdTipoInstrumento;//					NVARCHAR(3) 	default '',  
		public Long		p_IdInstrumento;//						BIGINT 			default 0,
		public String	p_Nemo;//								NVARCHAR(40) 	default '',
		public String	p_FechaPago;//							NVARCHAR(10) 	default '0001-01-01',     
		public Long		p_NumeroDividendo;//					BIGINT 			default 0,
		public Long		p_Secuencia;//							BIGINT 			default 0,
		public BigDecimal	p_ValorUnitario;//						BIGINT 			default 0,		
		public BigDecimal	p_Factor ;//								DECIMAL(26,8) 	default 0,
		public String 	p_Descripcion;//						NVARCHAR(100) 	default '',			
		public Long		p_IdCliente;//							BIGINT			default 0,
		public Long		p_IdProveedor;
		public Long		p_IdEmpleado;
		public Long		p_IdContraparte ;
		public Long		p_IdPortafolio;//						BIGINT			default 0,
		public String 	p_IdMoneda;//							NVARCHAR(3) 	default '',
		public BigDecimal	p_CantidadDerecho;//					BIGINT			default 0,
		public BigDecimal	p_CantidadNueva;//						BIGINT			default 0,
		public BigDecimal	p_MontoCapital;//						BIGINT			default 0,
		public BigDecimal	p_MontoInteres ;//						BIGINT			default 0,
		public String 	p_TipoCartera;//						NVARCHAR(3)		default '',
		public String 	p_IdBoveda;//							NVARCHAR(3)		default '',
		public String 	p_IdOrigen;                                                      //  Origen
		public String	p_IdTipoOperacion;//SORLET, DIVDIN
		//--------------------
		public Long 	p_IdEventoCapitalCliente;                                       //  IdEventoCapitalCliente
		public Long 	p_IdEventoCapital;                                              //  IdEventoCapital
		public Long 	p_IdSocioNegocio;                                               //  IdSocioNegocio
		public String 	p_TipoSocioNegocio;                                             //  TipoSocioNegocio
		public String 	p_IdTipoDocumentoIdentidad;                                     //  IdTipoDocumentoIdentidad
		public String 	p_DocumentoIdentidad;                                           //  DocumentoIdentidad          
		public String 	p_NombreCliente;   
		public String 	p_NombreCompleto;//  NombreCompleto segun tipo socio
		//public String 	p_IdClasificacionTipoInstrumento;                                 //  IdClasificacionTipoInstrumento
		public Long 	p_IdInstrumentoNuevo;                                           //  IdInstrumentoNuevo
		public String 	p_NemoNuevo;                                                    //  NemoNuevo
		public String 	p_FechaCierre;                                                  //  FechaCierre
		public BigDecimal	p_Total;                                                        //  Total
		public Long 	p_SecuenciaCupon;                                               //  SecuenciaCupon
		public String 	p_CodigoUsuarioAgente;                                          //  CodigoUsuarioAgente
		public String 	p_NombreAgente;                                                 //  NombreAgente
		public String 	p_CodigoSucursal;                                               //  CodigoSucursal
		public String 	p_FechaHoraIngresoRegistro;                                     //  FechaHoraIngresoRegistro
		public String 	p_FechaHoraUltimaActualizacion;                                 //  FechaHoraUltimaActualizacion
		public Long 	p_IdUsuario;                                                    //  IdUsuario
		public String 	p_IdEstado ;                                                     //  IdEstado		
		public String 	p_IdBolsa;
		public String 	p_IdTipoOperacionGarantia;
		public String	p_OperacionesVigentes;
		public BigDecimal p_Precio;									//Precio
		public String p_ISFUT;															//ISFUT 
		public String p_RetencionISFUT;													//RetencionISFUT
		public BigDecimal p_MontoRetencionISFUT;										//MontoRetencionISFUT
		public String p_SecuenciaRelacionada;
		public String p_SuscripcionComplementaria;
		
		public EventoCL(){
			this.p_IdTipoInstrumento = "";//	
			this.p_IdInstrumentoNuevo = 0L;      
			this.p_NemoNuevo = ""; 
			this.p_IdEstado = "VIG";	
			this.p_IdBolsa = "BCS";
			this.p_IdTipoOperacionGarantia = "";
			this.p_OperacionesVigentes = "N";
			this.p_TipoCartera="";//						NVARCHAR(3)		default '',
			this.p_IdBoveda="";//							NVARCHAR(3)		default '',
			this.p_IdOrigen = "MAN";  
			this.p_Precio = BigDecimal.ZERO;			
			this.p_ValorUnitario = BigDecimal.ZERO;
			this.p_Factor = BigDecimal.ZERO;			
			this.p_IdCliente = 0L;
			this.p_IdProveedor = 0L;
			this.p_IdEmpleado = 0L;
			this.p_IdContraparte = 0L;	
			this.p_CantidadDerecho = BigDecimal.ZERO;
			this.p_CantidadNueva = BigDecimal.ZERO;
			this.p_MontoInteres = BigDecimal.ZERO;
			
		}
	}
	public static class EventoExiste extends BcsPojo{
		public Long 		IdEventoCapital;
		public Long			NumeroDividendo;
		public Long			Secuencia;
		public String		Nemo;
		public String		IdEstado;
		public String		IdTipoOperacion;
		public BigDecimal 	TasaEfectiva;
		public String		ISFUT;
			    
	  
	}
	public static class Cliente extends BcsPojo{
		public Long		IdCliente;
		public Long		IdSocioDeNegocio;
		public String 	DocumentoIdentidad;
		public String	NombreCompleto;
		public Long		IdSucursalEjecutivo;
		public String	CodigoEmpleadoEjecutivo;
		public String	NombreEmpleadoEjecutivo;
		
	    
	}
	
	public static class Empleado extends BcsPojo{
		public Long		IdEmpleado;
		public Long		IdSocioDeNegocio;
		public String 	DocumentoIdentidad;
		public String	NombreCompleto;
		public Long		IdSucursalEjecutivo;
		public String	CodigoEmpleadoEjecutivo;
		public String	NombreEmpleadoEjecutivo;		
	}
	
	public static class Contraparte extends BcsPojo{
		public Long		IdContraparte;
		public Long		IdSocioNegocio;
		public Long		IdCliente;
		public String 	TipoSocioNegocio;
		public Long		IdPortafolio;
		public String	IdTipoDocumentoIdentidad;
		public String 	DocumentoIdentidad;
		public String	NombreCompleto;
		public String	IdEstado;
		public Long		IdSucursalEjecutivo;
		public String	CodigoEmpleadoEjecutivo;
		public String	NombreEmpleadoEjecutivo;
	}
	
	public static class ActualizarEventoCapital extends BcsPojo{
		public Long		IdEventoCapital;
	}
	
	public EventoCapitalCliente_OUT logica (Connection conn, EventoCapitalCliente_IN in) throws Exception {
		SQL _sql = new SQL();
		Long Id = 0L;
		//Long countInsertReserva = 0L;
		HashMap<String,String> mapMsg = new HashMap<>();
		log.write("INICIO::CUS.CrearEventoCapitalCliente");
		EventoCapitalCliente_OUT out = new EventoCapitalCliente_OUT();
		HashMap<String, Object> params = new HashMap<>();
		List<Long> numeroDividendo = new ArrayList<>();
		List<Long> cliente= new ArrayList<>();
		List<Long> empleado= new ArrayList<>();
		List<Long> listContraparte= new ArrayList<>();
		List<EventoExiste> eventoExistente = new ArrayList<>();
		List<Cliente> clientes = new ArrayList<>();
		List<Empleado> empleados = new ArrayList<>();
		List<Contraparte> contrapartes = new ArrayList<>();
		List<Long> idPortafolio = new ArrayList<>();
		List<Long> idPortafolioCNT = new ArrayList<>();
		List<String> idTipoOperacion = new ArrayList<>();
		List<String> idMoneda = new ArrayList<>();
		MultiKeyMap eventoExistenteMap = new MultiKeyMap();
		MultiKeyMap preferenciaPagoPortafolio = new MultiKeyMap();
		MultiKeyMap preferenciaPagoPortafolioCNT = new MultiKeyMap();
		Map<String, Object> clienteMap = new HashMap<>();
		Map<String, Object> contraparteMap = new HashMap<>();
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;
		
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		String fechaHoraSistema = FechaDB.ObtenerFechaHoraSistemaUTC(conn,in.p_IdEmpresa);
		String MonedaSistema    = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		for (EventoCL ev: in.p_EventoCapital){
			//secuenciasEvento.add(ev.p_Secuencia);
			numeroDividendo.add(ev.p_NumeroDividendo);
			cliente.add(ev.p_IdCliente);
			empleado.add(ev.p_IdEmpleado);
			if(!ev.p_IdContraparte.equals(-1L) && !ev.p_IdContraparte.equals(0L)) {
				listContraparte.add(ev.p_IdContraparte);
			}
			//*****
			if (ev.p_TipoSocioNegocio.equalsIgnoreCase("CLI")){
				idPortafolio.add(ev.p_IdPortafolio);
			}
			idMoneda.add(ev.p_IdMoneda);
			//*****
		}
		
		if (numeroDividendo.size() > 0){
			params = new HashMap<>();
			params.put("p_IdEmpresa", in.p_IdEmpresa);
			//params.put("p_Secuencia", secuenciasEvento);
			params.put("p_NumeroDividendo", numeroDividendo);
			eventoExistente = _sql.query(conn, SQL_SELECT_EVENTO_CAPITAL, params, EventoExiste.class);
			for (EventoExiste ev: eventoExistente){
				eventoExistenteMap.put(ev.Secuencia, ev.NumeroDividendo, ev);
			}
		}
		for (EventoCL ev: in.p_EventoCapital){
			idTipoOperacion.add(((EventoExiste) eventoExistenteMap.get(ev.p_Secuencia, ev.p_NumeroDividendo)).IdTipoOperacion);
		}		
		if (cliente.size() > 0){
			params = new HashMap<>();
			params.put("p_IdCliente", cliente);
			clientes = _sql.query(conn, SQL_SELECT_CLIENTE, params, Cliente.class);
			clienteMap = Utils.List2Hash(clientes, "IdCliente");
		}
		if (empleado.size() > 0){
			params = new HashMap<>();
			params.put("p_IdEmpleado", empleado);
			empleados = _sql.query(conn, SQL_SELECT_EMPLEADO, params, Empleado.class);
				
		}
		if(listContraparte.size()>0) {
			params = new HashMap<>();
			params.put("p_IdContraparte",listContraparte);
			params.put("p_IdEmpresa", in.p_IdEmpresa);
			contrapartes = _sql.query(conn, SQL_SELECT_CONTRAPARTE, params, Contraparte.class);
		}
		
		//Para eventos cerrados y de tipo dividendo en dinero se obtiene el portafolio
		if(!contrapartes.isEmpty()) {
			contraparteMap = Utils.List2Hash(contrapartes, "IdContraparte");
			for(Contraparte cnt: contrapartes) {
				idPortafolioCNT.add(cnt.IdPortafolio);
			}
		}
		
		//**********Busco las preferencias de pago de los clientes
		List<PreferenciaPago>  preferenciaPago = this.buscarPreferenciaPagoPortafolio(conn, in.p_IdEmpresa,idPortafolio,idTipoOperacion,idMoneda);
		
		//Preferencias de Pago contraparte flujo para eventos cerrados o reabiertos
		List<PreferenciaPago>  preferenciaPagoContraparte = this.buscarPreferenciaPagoPortafolio(conn, in.p_IdEmpresa,idPortafolioCNT,idTipoOperacion,idMoneda);
		
		if (preferenciaPago.size() > 0)
		{
			for (EventoCL ev: in.p_EventoCapital){
				for (PreferenciaPago pPag: preferenciaPago){
					if (pPag.IdPortafolio.equals(ev.p_IdPortafolio)){
						if (pPag.IdMoneda == null){
							pPag.IdMoneda = ev.p_IdMoneda;
						}
						if (pPag.IdTipoOperacion == null){
							pPag.IdTipoOperacion = ((EventoExiste) eventoExistenteMap.get(ev.p_Secuencia, ev.p_NumeroDividendo)).IdTipoOperacion;
						}
					}
					preferenciaPagoPortafolio.put(pPag.IdPortafolio, pPag.IdTipoOperacion, pPag.IdMoneda, pPag);
				}
			}
		}
		
		
		if (preferenciaPagoContraparte.size() > 0 )
		{
			for (EventoCL ev: in.p_EventoCapital){
				for (PreferenciaPago pPag: preferenciaPagoContraparte){
					Long idPortafolioContraparte = ((Contraparte) contraparteMap.get(ev.p_IdContraparte)).IdPortafolio;
					if (pPag.IdPortafolio.equals(idPortafolioContraparte)){
						if (pPag.IdMoneda == null){
							pPag.IdMoneda = ev.p_IdMoneda;
						}
						if (pPag.IdTipoOperacion == null){
							pPag.IdTipoOperacion = ((EventoExiste) eventoExistenteMap.get(ev.p_Secuencia, ev.p_NumeroDividendo)).IdTipoOperacion;
						}
					}
					preferenciaPagoPortafolioCNT.put(pPag.IdPortafolio, pPag.IdTipoOperacion, pPag.IdMoneda, pPag);
				}
			}
		}
		
		//**********
		//::Validaciones
		this.validaciones(conn,in,out,eventoExistenteMap);		
		if (MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES) > 0){
			return out;
		}
				
		{
			//obtengo secuencias
			List<Long> secuenciaEventoCapital= Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente", new Long(in.p_EventoCapital.size()));
			List<Long> secuenciaEventoCapitalFormaDePago= Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::EventoCapitalClienteFormaDePago", new Long(in.p_EventoCapital.size()));
			List<EventoCL> datosInsert = new ArrayList<>();
			List<EventoCapitalClienteFormaDePago> datosInsertEventoFormaPago = new ArrayList<>();
			List<ActualizarEventoCapital> actualizarEventoCapital	=	new ArrayList<ActualizarEventoCapital>();
			//List<AgregarLog.InAgregarLog.DetalleLog> detalleLogList = new ArrayList<>();
			List<AgregarLog.InAgregarLog.Log> logList = new ArrayList<>();
			List<AgregarLog.InAgregarLog.DetalleLog> detalleLogList = new ArrayList<>();
			for (EventoCL evCap: in.p_EventoCapital){
				
				EventoCL inEv = this.crearEventoCapitalCliente(conn,in,evCap,clienteMap,empleados,secuenciaEventoCapital,eventoExistenteMap,fechaSistema,fechaHoraSistema);				
			    datosInsert.add(inEv);			    		    
			    
			    if (in.p_EventoCapital.indexOf(evCap) == 0){
				    mapMsg.clear();
				    mapMsg.put("p_NumeroDividendo", evCap.p_NumeroDividendo.toString());
					out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_OK,"UI_EVENTO_CAPITAL_CREADO",mapMsg,conn,in.p_IdEmpresa));
					out.P_OUT_SECUENCIA.add(inEv.p_IdEventoCapitalCliente);
					Id++;
			    }
				//forma de pago cliente
			    if (preferenciaPago.size() > 0) {	
			    if (inEv.p_IdTipoEventoCapital.equalsIgnoreCase("SL") || inEv.p_IdTipoEventoCapital.equalsIgnoreCase("CC") || inEv.p_IdTipoEventoCapital.equalsIgnoreCase("DV") || inEv.p_IdTipoEventoCapital.equalsIgnoreCase("RC") || inEv.p_IdTipoEventoCapital.equalsIgnoreCase("SU")){
			    	
				    EventoCapitalClienteFormaDePago evCliFormaPago = this.crearFormaDePagoCliente(in,in.p_EventoCapital.indexOf(evCap),inEv,preferenciaPagoPortafolio,secuenciaEventoCapitalFormaDePago,fechaHoraSistema);
				    datosInsertEventoFormaPago.add(evCliFormaPago);
				    }
			    }
			    
			    //Flujo nuevo para eventos cerrados o reabiertos IMPBESTOPT-769
			    String estadoEvento = ((EventoExiste) eventoExistenteMap.get(evCap.p_Secuencia,evCap.p_NumeroDividendo)).IdEstado;
			    if((estadoEvento.equalsIgnoreCase("CER") ||  estadoEvento.equalsIgnoreCase("REA"))) {
			    	
			    	Boolean tieneDerecho	=	this.tieneCantidadConDerecho(evCap, conn,in.p_IdEmpresa,"CUS", in.p_IdUsuario, fechaSistema);
					
					//Si no tiene cantidad con derecho se agrega una advertencia
					if(!tieneDerecho) {
						out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_EVENTO_CAPITAL_CLIENTE_SIN_DERECHO",new HashMap<>(),conn,in.p_IdEmpresa));
						Id++;
					}
			    	
			    	//Dividendo en Dinero
				    if(evCap.p_IdTipoEventoCapital.equalsIgnoreCase("DV")) {
						
						//Se crea la contraparte y su forma de pago. Si la contraparte no tiene forma de pago se descarta
						if(!contraparteMap.isEmpty()) {
							if(preferenciaPagoContraparte.size() > 0) {
								List <Long> secuenciaEventoCapitalContraparte			=	Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente", 1L);
								List<Long> secuenciaEventoCapitalContraparteFormaDePago	=	Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::EventoCapitalClienteFormaDePago",1L);
								EventoCL inEvCNT										=	this.crearContraparteEventoCapitalCliente(evCap, contraparteMap, secuenciaEventoCapitalContraparte.get(0), eventoExistenteMap, in.p_IdUsuario,fechaHoraSistema);
								EventoCapitalClienteFormaDePago evCntFormaPago 			=	this.crearFormaDePagoCliente(in,0,inEvCNT,preferenciaPagoPortafolioCNT,secuenciaEventoCapitalContraparteFormaDePago,fechaHoraSistema);
								datosInsert.add(inEvCNT);
							    datosInsertEventoFormaPago.add(evCntFormaPago);
							}else {
								out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ADVERTENCIA,"UI_EVENTO_CAPITAL_CLIENTE_CONTRAPARTE_DESCARTADA",new HashMap<>(),conn,in.p_IdEmpresa));
								Id++;
							}
							
						}
						
					//Reparto Capital
					}else if (evCap.p_IdTipoEventoCapital.equalsIgnoreCase("RC")) {
						
						//Se crea la contraparte aunque no tenga formas de pago
						if(!contraparteMap.isEmpty()) {
							List <Long> secuenciaEventoCapitalContraparte			=	Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente", 1L);
							EventoCL inEvCNT										=	this.crearContraparteEventoCapitalCliente(evCap, contraparteMap, secuenciaEventoCapitalContraparte.get(0), eventoExistenteMap, in.p_IdUsuario, fechaHoraSistema);
							datosInsert.add(inEvCNT);
							
							if(preferenciaPagoContraparte.size() > 0) {
								List<Long> secuenciaEventoCapitalContraparteFormaDePago	=	Utils.getSecuencia(conn, "BSCL.Custodia.BD.Secuencias::EventoCapitalClienteFormaDePago",1L);
								EventoCapitalClienteFormaDePago evCntFormaPago 			=	this.crearFormaDePagoCliente(in,0,inEvCNT,preferenciaPagoPortafolioCNT,secuenciaEventoCapitalContraparteFormaDePago,fechaHoraSistema);
							    datosInsertEventoFormaPago.add(evCntFormaPago);
							}
						}

					}
				    
				  //Si el evento esta cerrado se vuelve a abrir
				    if(estadoEvento.equalsIgnoreCase("CER")) {
				    	ActualizarEventoCapital	updEventoCapital	=	new ActualizarEventoCapital();
					    updEventoCapital.IdEventoCapital			=	((EventoExiste) eventoExistenteMap.get(evCap.p_Secuencia, evCap.p_NumeroDividendo)).IdEventoCapital;
					    actualizarEventoCapital.add(updEventoCapital);
				    }
			    	
			    }
			    
			    AgregarLog.InAgregarLog.Log log = this.crearListaLog(in,inEv,fechaSistema,entradaAgregarLog.p_IdTrabajo); 
				logList.add(log);
				detalleLogList.addAll(this.crearListaDetalleLog(in,inEv,fechaSistema,entradaAgregarLog.p_IdTrabajo));
								
			}			
			
			this.crearLogEventoCapitalCliente(conn,datosInsert,datosInsertEventoFormaPago,logList, detalleLogList);
			
			//INSERT
			log.write(datosInsert);
			_sql.batch(conn, SQL_INSERT_EVENTO_CAPITAL, datosInsert, EventoCL.class);
			_sql.batch(conn, SQL_INSERT_EVENTO_CAPITAL_FORMA_DE_PAGO, datosInsertEventoFormaPago, EventoCapitalClienteFormaDePago.class);
			//Actualizar estado del evento
			if(!actualizarEventoCapital.isEmpty()) {
				_sql.batch(conn, SQL_UPDATE_EVENTO_CAPITAL, actualizarEventoCapital, ActualizarEventoCapital.class);
			};
			
		}
					
		log.write("FIN::CUS.CrearEventoCapitalCliente");
		return out;
	};

	private void crearLogEventoCapitalCliente(Connection conn, List<EventoCL> datosInsert,List<EventoCapitalClienteFormaDePago> datosInsertEventoFormaPago,	List<cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.Log> logList, List<cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.DetalleLog> detalleLogList) throws Exception {
		logger.debug("INICIO::CrearEventoCapitalCliente.crearLogEventoCapitalCliente");
		long inicioMetodo = System.currentTimeMillis();	
	
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdTrabajo = 1L;		
		entradaAgregarLog.p_Log = logList;	
		entradaAgregarLog.p_LogDetalle = detalleLogList;
		
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(conn, entradaAgregarLog);	
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::CrearEventoCapitalCliente.crearLogEventoCapitalCliente :" , finMetodo- inicioMetodo);
		
	}

	private EventoCapitalClienteFormaDePago crearFormaDePagoCliente(EventoCapitalCliente_IN in, int index,EventoCL inEv,MultiKeyMap preferenciaPagoPortafolio, List<Long> secuenciaEventoCapitalFormaDePago,	String fechaHoraSistema) {
		logger.trace("INICIO::CrearEventoCapitalCliente.crearFormaDePagoCliente");
		long inicioMetodo = System.currentTimeMillis();	
		
		EventoCapitalClienteFormaDePago evCliFormaPago = new EventoCapitalClienteFormaDePago();
		evCliFormaPago.IdEventoCapitalClienteFormaDePago = secuenciaEventoCapitalFormaDePago.get(index);
	    evCliFormaPago.IdEventoCapitalCliente 		= inEv.p_IdEventoCapitalCliente;
	    evCliFormaPago.IdTipoMovimiento 			= "I"; 
	    evCliFormaPago.NumeroMedioDePago 			= "";
	    evCliFormaPago.Sucursal 					= inEv.p_CodigoSucursal.toString();
	    evCliFormaPago.FechaVctoDocumento 			= Timestamp.valueOf(inEv.p_FechaPago+" 00:00:00"); 
	    evCliFormaPago.Monto 						= inEv.p_Total; 
	    evCliFormaPago.FechaHoraIngresoRegistro 	= Timestamp.valueOf(fechaHoraSistema);
	    evCliFormaPago.IdUsuarioIngreso 			= in.p_IdUsuario; 
	    evCliFormaPago.IdEstado 					= inEv.p_IdEstado;
	    evCliFormaPago.IdPreferenciaPagoPortafolio 	= 0L;
	    evCliFormaPago.IdModalidadPago 				= "";
	    evCliFormaPago.IdBanco 						= 0L; 
	    evCliFormaPago.NombreBanco 					= "";
	    evCliFormaPago.IdTipoCuentaBancaria 		= "";
	    evCliFormaPago.NombreTipoCuentaBancaria 	= "";
	    evCliFormaPago.IdCuentaBancaria 			= 0L;
	    evCliFormaPago.NombreCuentaBancaria 		= "";
	    evCliFormaPago.NumeroCuentaBancaria 		= "";
	    evCliFormaPago.IdTipoTarjetaBancaria 		= "";
	    evCliFormaPago.NumeroTarjetaBancaria 		= "";
	    
	    if (preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda) != null && ((PreferenciaPago) preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).IdMedioDePago.equalsIgnoreCase("CIN")){
	    	evCliFormaPago.IdMedioDePago 				= ((PreferenciaPago)preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).IdMedioDePago;
		    evCliFormaPago.NombreMedioDePago 			= ((PreferenciaPago)preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).NombreMedioDePago;					    
	    }
	    else if (preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda) != null && !((PreferenciaPago) preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).IdMedioDePago.equalsIgnoreCase("CIN")){
	    	evCliFormaPago.IdPreferenciaPagoPortafolio 	= ((PreferenciaPago)preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).IdPreferenciaPagoPortafolio;
		    evCliFormaPago.IdModalidadPago 				= ((PreferenciaPago)preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).IdModalidadPago;
		    evCliFormaPago.IdMedioDePago 				= ((PreferenciaPago)preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).IdMedioDePago;
		    evCliFormaPago.NombreMedioDePago 			= ((PreferenciaPago)preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).NombreMedioDePago;
		    evCliFormaPago.IdBanco 						= ((PreferenciaPago)preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).IdBanco; 
		    evCliFormaPago.NombreBanco 					= ((PreferenciaPago)preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).NombreBanco;
		    evCliFormaPago.IdTipoCuentaBancaria 		= ((PreferenciaPago)preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).IdTipoCuentaBancaria;
		    evCliFormaPago.NombreTipoCuentaBancaria 	= ((PreferenciaPago)preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).NombreTipoCuentaBancaria;
		    evCliFormaPago.IdCuentaBancaria 			= ((PreferenciaPago)preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).IdCuentaBancaria;
		    evCliFormaPago.NombreCuentaBancaria 		= ((PreferenciaPago)preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).NombreCuentaBancaria;
		    evCliFormaPago.NumeroCuentaBancaria 		= ((PreferenciaPago)preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).NumeroCuentaBancaria;
		    evCliFormaPago.IdTipoTarjetaBancaria 		= ((PreferenciaPago)preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).IdTipoTarjetaBancaria;
		    evCliFormaPago.NumeroTarjetaBancaria 		= ((PreferenciaPago)preferenciaPagoPortafolio.get(inEv.p_IdPortafolio, inEv.p_IdTipoOperacion, inEv.p_IdMoneda)).NumeroTarjetaBancaria;
	    }
	
	
	
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::CrearEventoCapitalCliente.crearFormaDePagoCliente :" , finMetodo- inicioMetodo);
		return evCliFormaPago;
	}


	private AgregarLog.InAgregarLog.Log crearListaLog(EventoCapitalCliente_IN in,EventoCL inEv, String fechaSistema,Long IdTrabajo) {
		logger.debug("INICIO::CrearEventoCapitalCliente.crearListaLog");
		long inicioMetodo = System.currentTimeMillis();	
		
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		log.IdTrabajo		= IdTrabajo; 
		log.IdCabeceraLog	= inEv.p_IdEventoCapitalCliente; 
		log.IdTipoLog		= "INGRESO"; 
		log.IdModulo		= in.p_IdModulo; 
		log.IdUsuario		= in.p_IdUsuario; 
		log.IdEmpresa		= in.p_IdEmpresa; 
		log.FechaSistemaLog	= fechaSistema; 
		log.IdUsuarioAtendido	= in.p_IdUsuario; 
		log.IdCliente			= in.p_EventoCapital.get(0).p_IdCliente; 
		log.EstadoCliente		= ""; 
		log.NombreCliente		= in.p_EventoCapital.get(0).p_NombreCliente; 
		log.IdTipoDocumentoIdentidad	= inEv.p_IdTipoDocumentoIdentidad; 
		log.IdDocumentoIdentidad	= inEv.p_DocumentoIdentidad; 
		log.IdPortafolio			= inEv.p_IdPortafolio; 
		log.EstadoPortafolio		= ""; 
		log.IdTipoRegistro			= "INGRESAREVENTOCAPITALCLIENTE"; 
		log.IdRegistro				= inEv.p_IdEventoCapitalCliente.toString(); 
		log.EstadoRegistro			= ""; 
		log.IdTipoOperacion			= ""; 
		log.Folio					= 0L; 
		log.Cantidad				= inEv.p_CantidadDerecho; 
		log.PrecioTasa				= BigDecimal.ZERO; 
		log.Monto					= BigDecimal.ZERO; 
		log.IdMonedaOrigen			= inEv.p_IdMoneda; 
		log.IdMonedaLiquidacion		= ""; 
		log.TipoCambio				= ""; 
		log.Nemo					= in.p_EventoCapital.get(0).p_Nemo; 
		log.IdProceso				= 0L; 
		log.EstadoProceso			= ""; 
		log.NombreProceso			= "cl.bcs.custodia.evc.CrearEventoCapitalCliente";
		
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::CrearEventoCapitalCliente.crearListaLog :" , finMetodo- inicioMetodo);		
		
		return log;
	}
	
	private List<DetalleLog> crearListaDetalleLog(EventoCapitalCliente_IN in,EventoCL inEv, String fechaSistema,Long IdTrabajo) {
		logger.debug("INICIO::CrearEventoCapitalCliente.crearListaDetalleLog");
		long inicioMetodo = System.currentTimeMillis();	
		
		List<DetalleLog> detalleLog = new ArrayList<>();
		detalleLog.add(CrearEventoCapital.agregarDetalleLog(inEv.p_IdEventoCapitalCliente, "INGRESAREVENTOCAPITALCLIENTE", "IdTipoEventoCapital", "", inEv.p_IdTipoEventoCapital));
		detalleLog.add(CrearEventoCapital.agregarDetalleLog(inEv.p_IdEventoCapitalCliente, "INGRESAREVENTOCAPITALCLIENTE", "Secuencia", "", inEv.p_Secuencia.toString()));
		detalleLog.add(CrearEventoCapital.agregarDetalleLog(inEv.p_IdEventoCapitalCliente, "INGRESAREVENTOCAPITALCLIENTE", "NumeroDividendo", "", inEv.p_NumeroDividendo.toString()));
		detalleLog.add(CrearEventoCapital.agregarDetalleLog(inEv.p_IdEventoCapitalCliente, "INGRESAREVENTOCAPITALCLIENTE", "TipoCartera", "", inEv.p_TipoCartera));
		detalleLog.add(CrearEventoCapital.agregarDetalleLog(inEv.p_IdEventoCapitalCliente, "INGRESAREVENTOCAPITALCLIENTE", "IdBoveda", "", inEv.p_IdBoveda));
		detalleLog.add(CrearEventoCapital.agregarDetalleLog(inEv.p_IdEventoCapitalCliente, "INGRESAREVENTOCAPITALCLIENTE", "ISFUT", "", inEv.p_ISFUT));
		
		if(inEv.p_ISFUT != null && inEv.p_ISFUT.equalsIgnoreCase("S")) {
			detalleLog.add(CrearEventoCapital.agregarDetalleLog(inEv.p_IdEventoCapitalCliente, "INGRESAREVENTOCAPITALCLIENTE", "RetencionISFUT", "", inEv.p_RetencionISFUT));
			if(inEv.p_RetencionISFUT != null && inEv.p_RetencionISFUT.equalsIgnoreCase("S"))
			detalleLog.add(CrearEventoCapital.agregarDetalleLog(inEv.p_IdEventoCapitalCliente, "INGRESAREVENTOCAPITALCLIENTE", "MontoRetencionISFUT", "", inEv.p_MontoRetencionISFUT.toString()));
		}
		
		detalleLog.add(CrearEventoCapital.agregarDetalleLog(inEv.p_IdEventoCapitalCliente, "INGRESAREVENTOCAPITALCLIENTE", "CantidadDerecho", "", inEv.p_CantidadDerecho.toString()));
		detalleLog.add(CrearEventoCapital.agregarDetalleLog(inEv.p_IdEventoCapitalCliente, "INGRESAREVENTOCAPITALCLIENTE", "Total", "", inEv.p_Total.toString()));
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::CrearEventoCapitalCliente.crearListaDetalleLog :" , finMetodo- inicioMetodo);		
		
		return detalleLog;
	}

	private EventoCL crearEventoCapitalCliente(Connection conn, EventoCapitalCliente_IN in,EventoCL evCap, Map<String, Object> clienteMap, List<Empleado> empleados,List<Long> secuenciaEventoCapital
			, MultiKeyMap eventoExistenteMap,String fechaSistema,String fechaHoraSistema) throws Exception {
		logger.debug("INICIO::CrearEventoCapitalCliente.crearEventoCapitalCliente");
		long inicioMetodo = System.currentTimeMillis();		
		
		EventoCL inEv = new EventoCL();				
		if("CLI".equalsIgnoreCase(evCap.p_TipoSocioNegocio)) {
			inEv.p_IdSocioNegocio = ((Cliente) clienteMap.get(evCap.p_IdCliente)).IdSocioDeNegocio;
			inEv.p_DocumentoIdentidad = ((Cliente) clienteMap.get(evCap.p_IdCliente)).DocumentoIdentidad;
			inEv.p_NombreCliente = ((Cliente) clienteMap.get(evCap.p_IdCliente)).NombreCompleto;
			inEv.p_CodigoUsuarioAgente = ((Cliente) clienteMap.get(evCap.p_IdCliente)).CodigoEmpleadoEjecutivo != null ? ((Cliente) clienteMap.get(evCap.p_IdCliente)).CodigoEmpleadoEjecutivo : "";
			inEv.p_NombreAgente = ((Cliente) clienteMap.get(evCap.p_IdCliente)).NombreEmpleadoEjecutivo != null ? ((Cliente) clienteMap.get(evCap.p_IdCliente)).NombreEmpleadoEjecutivo : "";
			inEv.p_CodigoSucursal = ((Cliente) clienteMap.get(evCap.p_IdCliente)).IdSucursalEjecutivo.toString() != null ? ((Cliente) clienteMap.get(evCap.p_IdCliente)).IdSucursalEjecutivo.toString() : "0";
		}
		if("EMP".equalsIgnoreCase(evCap.p_TipoSocioNegocio) ) {
			inEv.p_IdSocioNegocio = evCap.p_IdSocioNegocio;
			inEv.p_DocumentoIdentidad = evCap.p_DocumentoIdentidad;
			inEv.p_NombreCliente = evCap.p_NombreCompleto;
			if (empleados.size() > 0)
			{
				if(empleados.get(0).CodigoEmpleadoEjecutivo == null) {
					inEv.p_CodigoUsuarioAgente = " ";
				}else{
					inEv.p_CodigoUsuarioAgente = empleados.get(0).CodigoEmpleadoEjecutivo;
				} 
				if(empleados.get(0).NombreEmpleadoEjecutivo == null) {
					inEv.p_NombreAgente = " ";
				}else{
					inEv.p_NombreAgente = empleados.get(0).NombreEmpleadoEjecutivo;
				}
				if(empleados.get(0).IdSucursalEjecutivo == null) {
					inEv.p_CodigoSucursal = "0";
				}else {
					inEv.p_CodigoSucursal = empleados.get(0).IdSucursalEjecutivo.toString();
				} 
			}
			inEv.p_CodigoUsuarioAgente = " ";
			inEv.p_NombreAgente = " ";
			inEv.p_CodigoSucursal = "0";
		}
		if(!("CLI".equalsIgnoreCase(evCap.p_TipoSocioNegocio) || "EMP".equalsIgnoreCase(evCap.p_TipoSocioNegocio))) {
			inEv.p_IdSocioNegocio = evCap.p_IdSocioNegocio;
			inEv.p_DocumentoIdentidad = evCap.p_DocumentoIdentidad;
			inEv.p_NombreCliente = evCap.p_NombreCompleto;
			inEv.p_CodigoUsuarioAgente = " ";
			inEv.p_NombreAgente = " ";
			inEv.p_CodigoSucursal = "0";
		}
		inEv.p_IdEventoCapitalCliente	= secuenciaEventoCapital.get(in.p_EventoCapital.indexOf(evCap));
		inEv.p_IdEventoCapital = ((EventoExiste) eventoExistenteMap.get(evCap.p_Secuencia,evCap.p_NumeroDividendo)).IdEventoCapital; 
		inEv.p_IdTipoEventoCapital = evCap.p_IdTipoEventoCapital;
		inEv.p_NumeroDividendo = evCap.p_NumeroDividendo;
		inEv.p_Secuencia = evCap.p_Secuencia;
		inEv.p_TipoSocioNegocio = evCap.p_TipoSocioNegocio;
		inEv.p_IdPortafolio = evCap.p_IdPortafolio;
		inEv.p_IdTipoDocumentoIdentidad = "R";
		inEv.p_IdClasificacionTipoInstrumento = evCap.p_IdClasificacionTipoInstrumento;
		inEv.p_IdTipoInstrumento = evCap.p_IdTipoInstrumento;
		inEv.p_IdInstrumento = evCap.p_IdInstrumento;
		inEv.p_Nemo	= evCap.p_Nemo;
		inEv.p_IdInstrumentoNuevo = evCap.p_IdInstrumentoNuevo;
		inEv.p_NemoNuevo = evCap.p_NemoNuevo;
		inEv.p_FechaCierre = evCap.p_FechaPago;
		inEv.p_FechaPago  = evCap.p_FechaPago;
		inEv.p_CantidadDerecho = evCap.p_CantidadDerecho;
		inEv.p_TipoCartera = evCap.p_TipoCartera;
		if("DV".equalsIgnoreCase(inEv.p_IdTipoEventoCapital) || "RC".equalsIgnoreCase(inEv.p_IdTipoEventoCapital)) {
			inEv.p_CantidadNueva = BigDecimal.ZERO;
		}else {
			inEv.p_CantidadNueva = evCap.p_CantidadNueva;
		}
		if(!evCap.p_IdContraparte.equals(-1L) && !evCap.p_IdContraparte.equals(0L)) {
			inEv.p_IdContraparte = evCap.p_IdContraparte;
		}
		inEv.p_IdTipoOperacion = ((EventoExiste) eventoExistenteMap.get(evCap.p_Secuencia,evCap.p_NumeroDividendo)).IdTipoOperacion;
		inEv.p_Precio = evCap.p_Precio;
		BigDecimal monto = new BigDecimal(evCap.p_MontoCapital.longValue());
		inEv.p_MontoCapital = evCap.p_IdTipoEventoCapital.equals("SU")|| evCap.p_IdTipoEventoCapital.equals("CC") ? monto : monto.multiply(evCap.p_Factor);
		inEv.p_MontoInteres = evCap.p_MontoInteres;
		BigDecimal factor = BigDecimal.ZERO;
		BigDecimal total = BigDecimal.ZERO;
		total = (evCap.p_CantidadDerecho == null?BigDecimal.ZERO:evCap.p_CantidadDerecho);
		factor = (evCap.p_Factor == null?BigDecimal.ZERO:evCap.p_Factor);
		if (evCap.p_IdTipoEventoCapital.equalsIgnoreCase("DV") || evCap.p_IdTipoEventoCapital.equalsIgnoreCase("RC")){
			total = total.multiply(factor);
			total = MonedaFormato.formatear(conn, total, MonedaFormato.MONTO, evCap.p_IdMoneda, "","").valor;
		}
		else if (evCap.p_IdTipoEventoCapital.equalsIgnoreCase("SL")){
			total = evCap.p_Total;
		}
		else {
			total = evCap.p_Total != null ? evCap.p_Total : BigDecimal.ZERO;
		}
		inEv.p_Total = total;
		inEv.p_IdMoneda = evCap.p_IdMoneda;
		inEv.p_SecuenciaCupon = evCap.p_Secuencia;
		inEv.p_FechaHoraIngresoRegistro = fechaHoraSistema;
		inEv.p_FechaHoraUltimaActualizacion = fechaHoraSistema;
		inEv.p_IdUsuario = in.p_IdUsuario;
		inEv.p_IdEstado = this.evaluaEstadoEC(evCap,eventoExistenteMap);
		inEv.p_IdBoveda = evCap.p_IdBoveda;
		inEv.p_IdOrigen = evCap.p_IdOrigen;
		inEv.p_IdBolsa  = evCap.p_IdBolsa;
		inEv.p_IdTipoOperacionGarantia  = evCap.p_IdTipoOperacionGarantia;
		inEv.p_OperacionesVigentes = evCap.p_OperacionesVigentes;
		/** Nuevos Datos agregados **/
		inEv.p_Descripcion = evCap.p_Descripcion;
		if(evCap.p_ISFUT == null) {evCap.p_ISFUT = "N";}
		inEv.p_ISFUT = evCap.p_ISFUT;
		if(evCap.p_RetencionISFUT == null) {evCap.p_RetencionISFUT = "N";}
		inEv.p_RetencionISFUT =  evCap.p_RetencionISFUT;
		inEv.p_MontoRetencionISFUT = BigDecimal.ZERO;
		if(evCap.p_ISFUT != null && ((evCap.p_ISFUT.equalsIgnoreCase("S") || evCap.p_RetencionISFUT.equalsIgnoreCase("S") ) && 
				((EventoExiste) eventoExistenteMap.get(evCap.p_Secuencia,evCap.p_NumeroDividendo)).TasaEfectiva.compareTo(BigDecimal.ZERO) == 1))
		{
			inEv.p_MontoRetencionISFUT = (((EventoExiste) eventoExistenteMap.get(evCap.p_Secuencia,evCap.p_NumeroDividendo))
					.TasaEfectiva.multiply(total, MathContext.DECIMAL128).divide(new BigDecimal(100), MathContext.DECIMAL128) );
		}
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::CrearEventoCapitalCliente.crearEventoCapitalCliente :" , finMetodo- inicioMetodo);
		return inEv;
	}


	private List<PreferenciaPago> buscarPreferenciaPagoPortafolio(Connection conn, Long p_IdEmpresa,List<Long> idPortafolio, List<String> idTipoOperacion, List<String> idMoneda) throws Exception {
		logger.debug("INICIO::CrearEventoCapitalCliente.buscarPreferenciaPagoPortafolio");
		long inicioMetodo = System.currentTimeMillis();
		SQL sql = new SQL();
		
		HashMap<String ,Object>params = new HashMap<>(5);
		params.put("p_IdEmpresa",p_IdEmpresa);
		params.put("p_IdPortafolio",idPortafolio);
		params.put("p_IdTipoOperacion",idTipoOperacion);
		params.put("p_IdMoneda",idMoneda);
		List<PreferenciaPago> preferenciaPago = sql.query(conn, SQL_SELECT_PREFERENCIA_DE_PAGO, params, PreferenciaPago.class);		
		
		long finMetodo = System.currentTimeMillis();
		logger.debug("FIN::CrearEventoCapitalCliente.buscarPreferenciaPagoPortafolio :" , finMetodo- inicioMetodo);
		return preferenciaPago;
	}

	private void validaciones(Connection conn, EventoCapitalCliente_IN in, EventoCapitalCliente_OUT out,MultiKeyMap eventoExistenteMap) throws Exception {
		logger.trace("INICIO::CrearEventoCapitalCliente.validaciones");
		long inicioMetodo = System.currentTimeMillis();		
		
		Long Id = 0L;
		
		if (in.p_EventoCapital.size() <= 0){
			out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"SIN_DATOS_A_PROCESAR",new HashMap<>(),conn,in.p_IdEmpresa));
			Id++;
		}
		for (EventoCL ev: in.p_EventoCapital){
			EventoExiste evEx = (EventoExiste) eventoExistenteMap.get(ev.p_Secuencia, ev.p_NumeroDividendo);
			if (evEx == null){				
				out.P_OUT_MENSAJES.add(new MensajeSalida(Id,MensajeSalida.MENSAJE_ERROR,"UI_EVENTO_CAPITAL_Y_O_SECUENCIA_NO_EXISTE",new HashMap<>(),conn,in.p_IdEmpresa));
				Id++;
			}
		}		
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::CrearEventoCapitalCliente.validaciones :" , finMetodo- inicioMetodo);
		
	}

	private String evaluaEstadoEC(EventoCL evCap, MultiKeyMap eventoExistenteMap) {
		logger.trace("INICIO::CrearEventoCapitalCliente.evaluaEstadoEC");
		long inicioMetodo = System.currentTimeMillis();
		String estado = "VIG";
		
		if(("SU").equals(evCap.p_IdTipoEventoCapital)) {
			estado = evCap.p_IdEstado;
		}else {
			if("CER".equals(((EventoExiste) eventoExistenteMap.get(evCap.p_Secuencia,evCap.p_NumeroDividendo)).IdEstado) || "REA".equals(((EventoExiste) eventoExistenteMap.get(evCap.p_Secuencia,evCap.p_NumeroDividendo)).IdEstado)) {
				estado = "PRO";
			}else {
				estado = ((EventoExiste) eventoExistenteMap.get(evCap.p_Secuencia,evCap.p_NumeroDividendo)).IdEstado;
			}			
		}				
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::CrearEventoCapitalCliente.evaluaEstadoEC :" , finMetodo- inicioMetodo);	
		return estado ;
	}
	
	private Boolean tieneCantidadConDerecho (EventoCL ev, Connection conn,Long idEmpresa, String modulo, Long idUsuario, String fechasistema) throws Exception{
		logger.debug("INICIO::CrearEventoCapitalCliente.tieneCantidadConDerecho");
		long inicioMetodo = System.currentTimeMillis();	
	    Boolean result = false;
	    ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
	    InformacionEmpresa empresa = consultarEmpresaService.logica(conn, idEmpresa);
	    //Llamado a Flujo de Custodia
	    List<ConsultarFlujoCustodia.FlujoUso> dato_p_uso = new ArrayList<ConsultarFlujoCustodia.FlujoUso>();
	    ConsultarFlujoCustodia.FlujoUso datosFlujoUso = new ConsultarFlujoCustodia.FlujoUso();
	    datosFlujoUso.UsoTipoOperacion				= "FLEVCP";
	    datosFlujoUso.IdCliente						= ev.p_IdCliente;
	    datosFlujoUso.IdPortafolio					= null;
	    datosFlujoUso.BusquedaEspecifica			= "FLUJO_CLIENTES_DERECHO_CAPITAL";
	    datosFlujoUso.IdTipoInstrumentoEspecifico	= null;
	    dato_p_uso.add(datosFlujoUso);
	    
	    List<ConsultarFlujoCustodia.FlujoFiltrosBusqueda> dato_p_FiltrosBusqueda = new ArrayList<ConsultarFlujoCustodia.FlujoFiltrosBusqueda>();
	    ConsultarFlujoCustodia.FlujoFiltrosBusqueda datosFlujoFiltrosBusqueda = new ConsultarFlujoCustodia.FlujoFiltrosBusqueda();
	    datosFlujoFiltrosBusqueda.IdClasificacionTipoInstrumento	= ev.p_IdClasificacionTipoInstrumento;
	    datosFlujoFiltrosBusqueda.IdTipoInstrumento					= ev.p_IdTipoInstrumento;
	    datosFlujoFiltrosBusqueda.IdInstrumento						= ev.p_IdInstrumento;
	    datosFlujoFiltrosBusqueda.IdEmisor							= null;
	    datosFlujoFiltrosBusqueda.FechaVencimiento					= null;
	    datosFlujoFiltrosBusqueda.IdTipoReajuste					= null;
	    datosFlujoFiltrosBusqueda.EsFungible						= null;
	    datosFlujoFiltrosBusqueda.IdBoveda							= null;
	    dato_p_FiltrosBusqueda.add(datosFlujoFiltrosBusqueda);
	    
	    List<ConsultarFlujoCustodia.FlujoEsAgrupadoPor> dato_p_EsAgrupadoPor = new ArrayList<ConsultarFlujoCustodia.FlujoEsAgrupadoPor>();
	    ConsultarFlujoCustodia.FlujoEsAgrupadoPor datosFlujoEsAgrupadoPor = new ConsultarFlujoCustodia.FlujoEsAgrupadoPor();
	    datosFlujoEsAgrupadoPor.ClasificacionTipoInstrumento	= "S";
	    datosFlujoEsAgrupadoPor.TipoInstrumento					= "S";
	    datosFlujoEsAgrupadoPor.Instrumento						= "S";
	    datosFlujoEsAgrupadoPor.Emisor							= "N";
	    datosFlujoEsAgrupadoPor.Nemotecnico						= "N";
	    datosFlujoEsAgrupadoPor.NemoSVS							= "N";
	    datosFlujoEsAgrupadoPor.FechaEmisionInstrumento			= "N";
	    datosFlujoEsAgrupadoPor.FechaVencimientoInstrumento		= "N";
	    datosFlujoEsAgrupadoPor.FechaLiquidacionInstrumento		= "N";
	    datosFlujoEsAgrupadoPor.TipoReajuste					= "N";
	    datosFlujoEsAgrupadoPor.EsFungible						= "N";
	    datosFlujoEsAgrupadoPor.CodigoIFRS						= "N";
	    datosFlujoEsAgrupadoPor.Boveda							= "N";
	    datosFlujoEsAgrupadoPor.Bolsa							= "N";
	    datosFlujoEsAgrupadoPor.TipoOperacion					= "N";
	    datosFlujoEsAgrupadoPor.Mesa							= "N";
	    datosFlujoEsAgrupadoPor.FolioRegistro					= "N";
	    datosFlujoEsAgrupadoPor.ItemOrigen						= "N";
	    dato_p_EsAgrupadoPor.add(datosFlujoEsAgrupadoPor);
	    
	    ConsultarFlujoCustodia llamadoFlujoCustodia = new ConsultarFlujoCustodia();
	    ConsultarFlujoCustodia.FlujoCustodiaEntrada paramInSP = new ConsultarFlujoCustodia.FlujoCustodiaEntrada(); 
	    paramInSP.p_IdEmpresa			= empresa.IdEmpresa;
	    paramInSP.p_IdUsuario			= idUsuario;
	    paramInSP.p_IdModulo			= modulo;
	    
	    paramInSP.p_FechaSaldo			= fechasistema;
	    
	    paramInSP.p_IdPais				= empresa.IdPais;
	    paramInSP.p_DiasProyeccion		= 0;
	    paramInSP.p_DiaHabil			= "S";
	    paramInSP.p_ValorizarFlujo		= "S";
	    paramInSP.p_Uso					= dato_p_uso;
	    paramInSP.p_FiltrosBusqueda		= dato_p_FiltrosBusqueda;
	    paramInSP.p_EsAgrupadoPor		= dato_p_EsAgrupadoPor;
	    
	    ConsultarFlujoCustodia.FlujoCustodiaSalida salidaFlujo = llamadoFlujoCustodia.logica(conn, paramInSP);
	    if(salidaFlujo.p_out_FlujoDetallado.size() > 0) {
	    	result= true;
	    }
	    long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::CrearEventoCapitalCliente.tieneCantidadConDerecho :" , finMetodo- inicioMetodo);
	    return result;
	}
	
	public EventoCL crearContraparteEventoCapitalCliente(EventoCL ev,Map<String, Object> contraparteMap, Long secuenciaEventoCapitalContraparte,MultiKeyMap eventoExistenteMap, Long idUsuario, String fechaHoraSistema) throws Exception {
		logger.debug("INICIO::CrearEventoCapitalCliente.crearContraparteEventoCapitalCliente");
		long inicioMetodo = System.currentTimeMillis();
		
		EventoCL datosInsertContraparte = new EventoCL();
		Utils.copyPojo(ev, datosInsertContraparte);
		datosInsertContraparte.p_IdEventoCapitalCliente			=	secuenciaEventoCapitalContraparte;
		datosInsertContraparte.p_TipoSocioNegocio				=	"CNT";
		datosInsertContraparte.p_CantidadDerecho				=	datosInsertContraparte.p_CantidadDerecho.multiply(new BigDecimal(-1));
		datosInsertContraparte.p_CantidadNueva					=	BigDecimal.ZERO;
		datosInsertContraparte.p_MontoCapital					=	datosInsertContraparte.p_MontoCapital;
		datosInsertContraparte.p_MontoInteres					=	datosInsertContraparte.p_MontoInteres;
		datosInsertContraparte.p_Total							=	datosInsertContraparte.p_Total.multiply(new BigDecimal(-1));
		datosInsertContraparte.p_DocumentoIdentidad				=	((Contraparte) contraparteMap.get(datosInsertContraparte.p_IdContraparte)).DocumentoIdentidad;
		datosInsertContraparte.p_IdTipoDocumentoIdentidad		=	((Contraparte) contraparteMap.get(datosInsertContraparte.p_IdContraparte)).IdTipoDocumentoIdentidad;
		datosInsertContraparte.p_NombreCliente					=	((Contraparte) contraparteMap.get(datosInsertContraparte.p_IdContraparte)).NombreCompleto;
		datosInsertContraparte.p_IdPortafolio					=	((Contraparte) contraparteMap.get(datosInsertContraparte.p_IdContraparte)).IdPortafolio;
		datosInsertContraparte.p_CodigoUsuarioAgente			= 	((Contraparte) contraparteMap.get(datosInsertContraparte.p_IdContraparte)).CodigoEmpleadoEjecutivo;
		datosInsertContraparte.p_NombreAgente					= 	((Contraparte) contraparteMap.get(datosInsertContraparte.p_IdContraparte)).NombreEmpleadoEjecutivo;
		datosInsertContraparte.p_CodigoSucursal					= 	((Contraparte) contraparteMap.get(datosInsertContraparte.p_IdContraparte)).IdSucursalEjecutivo.toString();
		datosInsertContraparte.p_IdSocioNegocio					=	((Contraparte) contraparteMap.get(datosInsertContraparte.p_IdContraparte)).IdSocioNegocio;
		datosInsertContraparte.p_IdCliente						=	((Contraparte) contraparteMap.get(datosInsertContraparte.p_IdContraparte)).IdCliente;
		datosInsertContraparte.p_NombreCompleto					= 	((Contraparte) contraparteMap.get(datosInsertContraparte.p_IdContraparte)).NombreCompleto;
		datosInsertContraparte.p_IdTipoOperacion				=   ((EventoExiste) eventoExistenteMap.get(ev.p_Secuencia,ev.p_NumeroDividendo)).IdTipoOperacion;
		datosInsertContraparte.p_IdEventoCapital				=	((EventoExiste) eventoExistenteMap.get(ev.p_Secuencia,ev.p_NumeroDividendo)).IdEventoCapital;
		datosInsertContraparte.p_IdUsuario						=	idUsuario;
		datosInsertContraparte.p_SecuenciaCupon					=	ev.p_Secuencia;
		datosInsertContraparte.p_FechaHoraIngresoRegistro		=	fechaHoraSistema;
		datosInsertContraparte.p_FechaHoraUltimaActualizacion 	=	fechaHoraSistema;
		datosInsertContraparte.p_FechaCierre					=	ev.p_FechaPago;
		datosInsertContraparte.p_FechaPago  					=	ev.p_FechaPago;
		datosInsertContraparte.p_IdEstado  						=	"PRO";
		datosInsertContraparte.p_IdPortafolio					= 	((Contraparte) contraparteMap.get(ev.p_IdContraparte)).IdPortafolio	;
		
		long finMetodo = System.currentTimeMillis();
		logger.trace("FIN::CrearEventoCapitalCliente.crearContraparteEventoCapitalCliente :" , finMetodo- inicioMetodo);
		return datosInsertContraparte;
	}
	

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		Gson gson = Utils.getGsonBuilder();
		EventoCapitalCliente_IN jin = gson.fromJson(in.toString(), EventoCapitalCliente_IN.class);
		EventoCapitalCliente_OUT out = logica(conn, jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser = new JsonParser();
		JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args)  throws Exception,SQLException {
		// TODO Auto-generated method stub
		Connection conn = null;
		Gson gson = new Gson();
		
		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties");
			//datos entrada
			String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_EventoCapital\":[{\"p_IdTipoEventoCapital\":\"DF\",\"p_IdClasificacionTipoInstrumento\":\"FM\",\"p_IdTipoInstrumento\":\"FFM\",\"p_IdInstrumento\":471742,\"p_Nemo\":\"00097CCLP\",\"p_FechaPago\":\"2019-08-26\",\"p_NumeroDividendo\":115,\"p_Secuencia\":2,\"p_ValorUnitario\":0,\"p_Factor\":2,\"p_Descripcion\":\"PRUEBA 15:04\",\"p_IdCliente\":1423323,\"p_IdPortafolio\":1450339,\"p_IdMoneda\":\"CLP\",\"p_CantidadDerecho\":5555,\"p_CantidadNueva\":5555,\"p_Total\":0,\"p_MontoCapital\":555,\"p_MontoInteres\":555,\"p_TipoCartera\":\"CUS\",\"p_IdBoveda\":\"DCV\",\"p_IdOrigen\":\"MAN\",\"p_TipoSocioNegocio\":\"CLI\"}]}";
			String json2= "{\"p_IdEmpresa\":1,\"p_IdUsuario\":4,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_EventoCapital\":[{\"p_IdTipoEventoCapital\":\"CC\",\"p_IdClasificacionTipoInstrumento\":\"RF\",\"p_IdTipoInstrumento\":\"BEM\",\"p_IdInstrumento\":262000,\"p_Nemo\":\"BAGUA-H\",\"p_FechaPago\":\"2021-04-01\",\"p_NumeroDividendo\":0,\"p_Secuencia\":26,\"p_ValorUnitario\":0,\"p_Factor\":0,\"p_Descripcion\":\"CORTE CUPON BAGUA-H\",\"p_IdCliente\":1424051,\"p_IdPortafolio\":1451612,\"p_IdMoneda\":\"CLP\",\"p_CantidadDerecho\":10000,\"p_CantidadNueva\":11000,\"p_Total\":12000,\"p_MontoCapital\":13000,\"p_MontoInteres\":14000,\"p_TipoCartera\":\"CUS\",\"p_IdBoveda\":\"DCV\",\"p_IdOrigen\":\"MAN\",\"p_TipoSocioNegocio\":\"CLI\"}]}";
			String json3= "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"CUS\",\"p_UsaReglasNegocio\":\"S\",\"p_EventoCapital\":[{\"p_IdTipoEventoCapital\":\"DV\",\"p_IdClasificacionTipoInstrumento\":\"RV\",\"p_IdTipoInstrumento\":\"ACN\",\"p_IdInstrumento\":1078,\"p_Nemo\":\"HABITAT\",\"p_FechaPago\":\"2024-07-09\",\"p_NumeroDividendo\":23232,\"p_Secuencia\":11321,\"p_ValorUnitario\":0,\"p_Factor\":1.2,\"p_Descripcion\":\"12122\",\"p_IdCliente\":61625,\"p_IdEmpleado\":836,\"p_IdProveedor\":-1,\"p_IdContraparte\":-1,\"p_IdPortafolio\":206341,\"p_IdMoneda\":\"CLP\",\"p_CantidadDerecho\":100,\"p_CantidadNueva\":120,\"p_Total\":120,\"p_MontoCapital\":0,\"p_MontoInteres\":0,\"p_TipoCartera\":\"CUS\",\"p_IdBoveda\":\"DCV\",\"p_IdOrigen\":\"MAN\",\"p_TipoSocioNegocio\":\"CLI\",\"p_ISFUT\":\"N\",\"p_RetencionISFUT\":\"N\",\"p_MontoRetencionISFUT\":0,\"p_DocumentoIdentidad\":\"16657034-4\",\"p_NombreCompleto\":\"MACARENA SUSANA MANCILLA LOPEZ\",\"p_IdSocioNegocio\":30480}]}";
			EventoCapitalCliente_IN objIn = gson.fromJson(json3,EventoCapitalCliente_IN.class);
			//mientras pruebo hare el rollback
			CrearEventoCapitalCliente demo = new CrearEventoCapitalCliente();
			
			EventoCapitalCliente_OUT ObjOut=demo.logica(conn, objIn);
				
			System.out.println(ObjOut.toString());
			conn.rollback();
			//conn.commit();
			
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			conn.close();
		}
			System.out.println("== END ==");

	};
}
