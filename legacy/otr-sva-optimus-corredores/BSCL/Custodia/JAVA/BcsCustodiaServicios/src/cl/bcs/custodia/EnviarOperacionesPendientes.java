package cl.bcs.custodia;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.custodia.utils.ClientWS.asignacionGestion.IngresoAsignacion;
import cl.bcs.custodia.utils.ClientWS.asignacionGestion.InsertasignacionResult;
import cl.bcs.custodia.utils.ClientWS.asignacionGestion.SpingresarmodificarasignacionResponse;
import cl.bcs.custodia.utils.ClientWS.asignacionGestion.SpingresarmodificarasignaciongestionService;
import cl.bcs.custodia.utils.ClientWS.asignacionGestion.SpingresarmodificarasignaciongestionServiceService;
import cl.bcs.custodia.utils.ClientWS.bcs.pt.cus.ws.movimientos.CargaMasiva;
import cl.bcs.custodia.utils.ClientWS.bcs.pt.cus.ws.movimientos.CargaMasivaService;
import cl.bcs.custodia.utils.ClientWS.bcs.pt.cus.ws.movimientos.CargamasivaMensaje;
import cl.bcs.custodia.utils.ClientWS.bcs.pt.cus.ws.movimientos.DetalleGtia;
import cl.bcs.custodia.utils.ClientWS.bcs.pt.cus.ws.movimientos.Glosa;
import cl.bcs.custodia.utils.ClientWS.cclv.test.ws.TypeAsignaciones;
import cl.bcs.custodia.utils.ClientWS.cclv.test.ws.TypeRetorno;
import cl.bcs.custodia.utils.ClientWS.cclv.test.ws.WSConsumeUtils;
import cl.bcs.custodia.utils.ClientWS.cclv.test.ws.WSConsumeUtils_Service;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class EnviarOperacionesPendientes implements JavaCall {
	Configuracion conexion = new Configuracion();
	
	private static final Logger logger = LogManager.getLogger(EnviarOperacionesPendientes.class);


	private final String SQL_UPDATEOPERACIONPENDIENTE= loadResourceToString("cl/bcs/custodia/sql/EnviarOperacionesPendientes_UpdateOperacionesPendientes.sql");
	
	public static class InEnviarOperacionesPendientes extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public Long p_CodidoCorredor;
		public List<OperacionesPendientes> p_OperacionesPendientes;
		
		public InEnviarOperacionesPendientes(){
			p_OperacionesPendientes = new ArrayList<OperacionesPendientes>();
	    }
	}
	
	public static class OperacionesPendientes extends BcsPojo {
		public String			p_IdTipo;
		public String 			p_TipoOperacion;
		public String 			p_FolioOperacion;
		public Long				p_FolioTransaccion;
		public String			p_DocumentoIdentidad;
		public Long				p_Cantidad;
		public String			p_FechaIngresoRegistro;
		public String			p_IndDcv;
		public String			p_Categoria;
		public String			p_NemoSVS;
		public String			p_IdTipoGarantia;
		public String			p_IdBolsa;
		public String			p_FondoAfp;
		public Long				p_IdInterno;
		public String 			p_Producto;
		public String 			p_Nombre;
		public Integer			p_IdGestion;
	}

	public static class OutEnviarOperacionesPendientes extends BcsPojo {
		public List<MensajeSalida> P_OUT_MENSAJES;
		public Long P_OUT_ERRORESCONTADOR;
		public Long P_OUT_ADVERTENCIASCONTADOR;

		public OutEnviarOperacionesPendientes() {
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_ADVERTENCIASCONTADOR = 0L;
		}
	}

	public static class Configuracion extends BcsPojo {
		public String 	p_wsdl;
		public String 	p_nameSpace;
		public String 	p_localPart;
		public String 	p_usuario;
		public String 	p_pass;
		public String 	p_perfil;
		public String   p_fechaSistema;
		public String 	p_wsdlAsig;
		public String 	p_nameSpaceAsig;
		public String 	p_localPartAsig;
		public String 	p_perfilAsig;
		public String 	p_UsuarioSim;
		public String 	p_PasswordSim;
		public String	p_wsdlSim;
		public String 	p_nameSpaceSim;
		public String 	p_localPartSim;
		public String   p_CodigoCorredora;
	}
	
	public static class UpdateOperacionPendiente {
		public Long 	IdInterno;
		public String 	FolioOperacion;
		public String 	IdEstado;
		public String	EstadoCclv;
		public String 	EstadoCclvDesc;
		public String	FechaEjecucion;
		public Integer 	IdGestion;
	}
	
	public static class ProcesoOperacionesPendientes {
		List<DetalleGtia> garantias = new ArrayList<>();
		List<TypeAsignaciones> asginaciones = new ArrayList<>();
		List<IngresoAsignacion> asignacionesSimultaneas = new ArrayList<>();
	}
	
	@Trace
	public OutEnviarOperacionesPendientes logica(Connection con, InEnviarOperacionesPendientes in) throws Exception {
		logger.info("INICIO::EnviarOperacionesPendientes.logica");
		long inicio = System.currentTimeMillis();
		OutEnviarOperacionesPendientes out = new OutEnviarOperacionesPendientes();
		
		if (in.p_OperacionesPendientes.size() > 0) {
			conexion.p_CodigoCorredora = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.OperacionesPendientes.CodCorr");
			conexion.p_fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");	
			
			ProcesoOperacionesPendientes salidaProceso = procesamientoOperacionesPendientes(in.p_OperacionesPendientes);
			
			OutEnviarOperacionesPendientes salidaGarantias = envioGarantias(con, in.p_IdEmpresa, salidaProceso.garantias);
			OutEnviarOperacionesPendientes salidaAsginaciones = envioAsignaciones(con, in.p_IdEmpresa, salidaProceso.asginaciones);
			OutEnviarOperacionesPendientes salidaAsginacionesSIM = envioAsignacionesSimultaneas(con, in.p_IdEmpresa, salidaProceso.asignacionesSimultaneas);
			
			out.P_OUT_MENSAJES.addAll(salidaGarantias.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES.addAll(salidaAsginaciones.P_OUT_MENSAJES);
			out.P_OUT_MENSAJES.addAll(salidaAsginacionesSIM.P_OUT_MENSAJES);
		};
		long fin = System.currentTimeMillis();
		logger.info("FIN::EnviarOperacionesPendientes.logica {} ", fin - inicio);
		escribirLog(con,in, in.p_OperacionesPendientes);
		return out;
	}
	
	private OutEnviarOperacionesPendientes envioGarantias(Connection con, Long idEmpresa, List<DetalleGtia> listDetalleGtia) throws Exception {
		OutEnviarOperacionesPendientes salida = new OutEnviarOperacionesPendientes();
		logger.info("INICIO::EnviarOperacionesPendientes.envioGarantias");
		long inicio = System.currentTimeMillis();
		try {
			conexion.p_usuario 		= ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.EnvioOperacionesPendientes.Usuario");
			conexion.p_pass 		= ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.EnvioOperacionesPendientes.Password");
			conexion.p_perfil 		= ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.EnvioOperacionesPendientes.Profile");
			conexion.p_wsdl 		= ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.EnvioOperacionesPendientes.Wsdl");
			conexion.p_nameSpace 	= ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.EnvioOperacionesPendientes.NameSpace"); 
			conexion.p_localPart 	= ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.EnvioOperacionesPendientes.LocalPart");
			
			if (listDetalleGtia.size() > 0 ) {				
				CargaMasivaService wsClient = new CargaMasivaService(new URL(conexion.p_wsdl), new QName(conexion.p_nameSpace, conexion.p_localPart));
				CargaMasiva cclvService = wsClient.getCargaMasivaPort();			
						
				((BindingProvider) cclvService).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, conexion.p_usuario);
		        ((BindingProvider) cclvService).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, conexion.p_pass);
	
		        CargamasivaMensaje listDataWS = cclvService.paCargaretGtia(conexion.p_perfil, conexion.p_fechaSistema, conexion.p_CodigoCorredora, listDetalleGtia.size(), listDetalleGtia);
		        
		        listDataWS.getCantidadSolicitudesOk();
		        listDataWS.getCantidadSolicitudesNotok();
		        
		        SQL sql = new SQL();           
        		List<UpdateOperacionPendiente> oper = new ArrayList<UpdateOperacionPendiente>();
        		
        		logger.info("EnviarOperacionesPendientes.envioGarantias {} ", listDataWS);
			
	            for (Glosa glosa : listDataWS.getGlosa()){
	            	if (glosa != null){	            		
	            		UpdateOperacionPendiente operpen = new UpdateOperacionPendiente();
	            		operpen.FolioOperacion	= new String(glosa.getCorControl());
	
	            		logger.info("operpen.FolioOperacion: " + glosa.getCodigoError());
	            				
	            		if(glosa.getCodigoError().equals("0")){
	                    	operpen.IdEstado= new String("C");
	                    	String msg = "Garantia Secuencia: "+ operpen.FolioOperacion +" ENVIADA EXITOSAMENTE";
            				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, msg, msg));
	                    }
	            		else
	            		{ 	
							operpen.IdEstado = new String("R");
							
	            			if("CC37".equals(glosa.getCodigoError())) {
	            				HashMap<String, String> valores = new HashMap<>();
	            				valores.put("FolioOperacion",  operpen.FolioOperacion.toString());
	            				String mensaje = Diccionario.getMensaje(con, idEmpresa, "ERROR_GARANTIA_OPERACIONES_PENDIENTES", valores);
	            				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, "ERROR_GARANTIA_OPERACIONES_PENDIENTES", mensaje));
	            			}else {
		            			String msg = "Garantia Secuencia: "+ operpen.FolioOperacion + " Operación rechazada. " + glosa.getGlosaError();
	            				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, msg, msg));
	            			}
	            		};
	            		
	            		operpen.EstadoCclv		= new String(glosa.getCodigoError());
	            		operpen.EstadoCclvDesc	= new String(glosa.getGlosaExitoFracaso() + ": " + glosa.getGlosaError());
	            		operpen.FechaEjecucion  = new String(listDataWS.getFecha());
	            		
	            		List <DetalleGtia> detalle = listDetalleGtia.stream().filter(t->t.getCorControl().equalsIgnoreCase(glosa.getCorControl())).collect(Collectors.toList());
	            		for (DetalleGtia p :detalle) {
            				operpen.IdGestion 	= new Integer(p.getCorControl());
            				oper.add(operpen);
            			}
	            		
	    			}					    				
    			}
				sql.batch(con,SQL_UPDATEOPERACIONPENDIENTE, oper, UpdateOperacionPendiente.class);	
			}
		} catch (WebServiceException ews) {
			logger.error("EnviarOperacionesPendientes.envioGarantias() {}", ews);
			
    		String msjError = ews.getMessage(); 
    		Throwable causeError = ews.getCause();
    		if ((causeError != null && causeError.getMessage().contains("401")) || msjError.contains("401")) {
    			String msg = Diccionario.getMensaje(con, idEmpresa,"UI_CREDENCIALES_WS_INVALIDAS").msg();
    			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_CREDENCIALES_WS_GARANTIAS_INVALIDAS", msg));
    		} else {
    			String msg = Diccionario.getMensaje(con, idEmpresa,"UI_WS_ERROR").msg();
        		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_WS_ERROR", msg));
    		}
        }
		long fin = System.currentTimeMillis();
		logger.info("FIN::EnviarOperacionesPendientes.envioGarantias {} ", fin - inicio);
		return salida;
	}
	
	private OutEnviarOperacionesPendientes envioAsignaciones(Connection con, Long idEmpresa, List<TypeAsignaciones> listTypeAsignaciones) throws Exception {
		logger.info("INICIO::EnviarOperacionesPendientes.envioAsignaciones");
		long inicio = System.currentTimeMillis();
		OutEnviarOperacionesPendientes salida = new OutEnviarOperacionesPendientes();
		try {
	        if (listTypeAsignaciones.size() > 0) {	
	        	
				conexion.p_perfilAsig	 = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.EnvioAsignaciones.Profile");
				conexion.p_wsdlAsig		 = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.EnvioAsignaciones.Wsdl");
				conexion.p_nameSpaceAsig = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.EnvioAsignaciones.NameSpace"); 
				conexion.p_localPartAsig = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.EnvioAsignaciones.LocalPart");
				conexion.p_usuario 		= ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.EnvioOperacionesPendientes.Usuario");
				conexion.p_pass 		= ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.EnvioOperacionesPendientes.Password");
				
		        WSConsumeUtils_Service wsClientAsig = new WSConsumeUtils_Service(new URL(conexion.p_wsdlAsig), new QName(conexion.p_nameSpaceAsig, conexion.p_localPartAsig));	        
		        WSConsumeUtils cclvServiceAsig = wsClientAsig.getWSConsumeUtilsPort(); 
		        
				((BindingProvider) cclvServiceAsig).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, conexion.p_usuario);
		        ((BindingProvider) cclvServiceAsig).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, conexion.p_pass);
		        
		        List<TypeRetorno> listDataWSAsig = new ArrayList<TypeRetorno>();		        
		        listDataWSAsig	= cclvServiceAsig.paCargaAsignaciones(listTypeAsignaciones) ;	 
		    
		        if (listDataWSAsig.size() > 0 ) {
		        	
		    		List<UpdateOperacionPendiente> oper = new ArrayList<UpdateOperacionPendiente>();
		    		SQL sql=new SQL();	        	
		        	
		        	for (int j=0; j < listDataWSAsig.size(); j++){
		
		        		
		    			if (listDataWSAsig.get(j) != null) {	        			
		            		UpdateOperacionPendiente operpen = new UpdateOperacionPendiente();
		            		
		            		System.out.println("operpen.FolioOperacion: "+listDataWSAsig.get(j).getCorControl());	            		
		            		operpen.FolioOperacion	= listDataWSAsig.get(j).getCorControl().toString();	  
		            		
		            		String varFolio = listDataWSAsig.get(j).getCorControl().toString();
		            		
		            		System.out.println(listDataWSAsig.get(j).getCorControl());
		
		            		
		            		if(listDataWSAsig.get(j).getCodRetorno() == 0 ){
		                    	operpen.IdEstado= new String("C");
		                    	String msg = "Asignacion Secuencia "+ operpen.FolioOperacion +" ENVIADA EXITOSAMENTE";
		        				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, msg, msg));		                    	
		                    }
		            		else
		            		{ 	
		            			operpen.IdEstado	= new String("R");
		            			String msg = "Asignacion Secuencia: "+ operpen.FolioOperacion + ". " + listDataWSAsig.get(j).getDescripcion();
		        				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,  msg, msg));	            				
		            		};
		            		
		            		operpen.EstadoCclv		= new String("CC32");
		            		operpen.EstadoCclvDesc	= new String(listDataWSAsig.get(j).getDescripcion());
		            		operpen.FechaEjecucion  = new String(listDataWSAsig.get(j).getFecha());
		            		
		            		List <TypeAsignaciones> detalle = listTypeAsignaciones.stream().filter(t->t.getCorControl().toString().equalsIgnoreCase(varFolio)).collect(Collectors.toList());
		            		
		            		for (TypeAsignaciones d :detalle) {
		        				operpen.IdGestion   = d.getCorControl().intValue() ;																
		        				oper.add(operpen);            				
		        			}		        			
		        		}		        		
		        	}
		        	sql.batch(con,SQL_UPDATEOPERACIONPENDIENTE, oper, UpdateOperacionPendiente.class);
		        	
		        	System.out.println(MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES));
		        }	        
		    } 
		}catch (WebServiceException ews) {
			logger.error("ERROR::EnviarOperacionesPendientes.envioAsignaciones {} ", ews);
    		String msjError = ews.getMessage(); 
    		Throwable causeError = ews.getCause();
    		if ((causeError != null && causeError.getMessage().contains("401")) || msjError.contains("401")) {
    			String msg = Diccionario.getMensaje(con, idEmpresa,"UI_CREDENCIALES_WS_ASIGNACIONES_INVALIDAS").msg();
    			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_CREDENCIALES_WS_ASIGNACIONES_INVALIDAS", msg));
    		} else {
    			String msg = Diccionario.getMensaje(con, idEmpresa,"UI_WS_ERROR").msg();
        		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_WS_ERROR", msg));
    		}
        }
		long fin = System.currentTimeMillis();
		logger.info("FIN::EnviarOperacionesPendientes.envioAsignaciones {} ", fin - inicio);		
		return salida;		
	}
	
	private OutEnviarOperacionesPendientes envioAsignacionesSimultaneas(Connection con, Long idEmpresa, List<IngresoAsignacion> ingresoAsignaciones) throws Exception {
		logger.info("INICIO::EnviarOperacionesPendientes.envioAsignacionesSimultaneas");
		long inicio = System.currentTimeMillis();
		OutEnviarOperacionesPendientes salida = new OutEnviarOperacionesPendientes();
		try {
			if (ingresoAsignaciones.size() > 0) {
				conexion.p_UsuarioSim 	= ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.OperacionesPendientesSim.Usuario");
				conexion.p_PasswordSim 	= ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.OperacionesPendientesSim.Password");
				conexion.p_wsdlSim 		= ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.OperacionesPendientesSim.Wsdl");
				conexion.p_nameSpaceSim = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.OperacionesPendientesSim.NameSpace");
				conexion.p_localPartSim = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.OperacionesPendientesSim.LocalPart");
				SQL sql = new SQL();
	    		List<UpdateOperacionPendiente> operacionesPendientes = new ArrayList<UpdateOperacionPendiente>();
				
				SpingresarmodificarasignaciongestionServiceService modificarAsignacionGestion = new SpingresarmodificarasignaciongestionServiceService(new URL(conexion.p_wsdlSim), new QName(conexion.p_nameSpaceSim,conexion.p_localPartSim));
				SpingresarmodificarasignaciongestionService cclvService = modificarAsignacionGestion.getSpingresarmodificarasignaciongestionServicePort();
				
				((BindingProvider) cclvService).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, conexion.p_UsuarioSim);
				((BindingProvider) cclvService).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, conexion.p_PasswordSim);
					
				SpingresarmodificarasignacionResponse response = cclvService.spingresarmodificarasignaciongestionExecute(ingresoAsignaciones);	
				List<InsertasignacionResult> results = response.getInsertasignacionResult();
				
				logger.info("EnviarOperacionesPendientes.envioAsignacionesSimultaneas {} ", results);
				
				if (results.size() == 0L) {
					String msg = Diccionario.getMensaje(con, idEmpresa, "UI_ASIGNACIONES_SIM_NO_ENVIADAS").msg();
					salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_ASIGNACIONES_SIM_NO_ENVIADAS", msg));
				}
		
				for(InsertasignacionResult operacion : results) {
					if(operacion != null) {
	            		UpdateOperacionPendiente operacionPendiente = new UpdateOperacionPendiente();
	            		
	            		for (IngresoAsignacion op : ingresoAsignaciones) {
	            			if (op.getFolio().toString().equals(operacion.getFOLIO()) && op.getIdGestion().toString().equals(operacion.getIDGESTION())) {
	            		
			            		operacionPendiente.FolioOperacion = operacion.getFOLIO();
								if(operacion.getMSGID().compareTo(BigDecimal.ZERO) == 0) {
									operacionPendiente.IdEstado = new String("C");
									String msg = "Asignacion Secuencia  "+ operacionPendiente.FolioOperacion +" ENVIADA EXITOSAMENTE";
			        				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_OK, msg, msg));
								}else {
									operacionPendiente.IdEstado = new String("R");
									String msg = "Asignacion Secuencia: "+ operacionPendiente.FolioOperacion + ". " + operacion.getERRORTXT();
		            				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA,  msg, msg));	            				
			            		};
			            		
								operacionPendiente.EstadoCclv = operacion.getMSGID().toString();
								operacionPendiente.EstadoCclvDesc = operacion.getERRORTXT();
								operacionPendiente.FechaEjecucion = operacion.getFECHAOP();
								operacionPendiente.IdGestion = op.getIdGestion().intValue(); 
			            		operacionesPendientes.add(operacionPendiente);
	            			}
	            		}
					}
				}
				sql.batch(con, SQL_UPDATEOPERACIONPENDIENTE, operacionesPendientes, UpdateOperacionPendiente.class);
			
			}
		} catch (WebServiceException ews) {
			logger.error("ERROR::EnviarOperacionesPendientes.envioAsignacionesSimultaneas", ews);
    		String msjError = ews.getMessage(); 
    		Throwable causeError = ews.getCause();
    		if ((causeError != null && causeError.getMessage().contains("401")) || msjError.contains("401")) {
    			String msg = Diccionario.getMensaje(con,idEmpresa,"UI_CREDENCIALES_WS_ASIG_SIM__INVALIDAS").msg();
    			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "UI_CREDENCIALES_WS_ASIG_SIM__INVALIDAS", msg));
    		} else {
    			String msg = Diccionario.getMensaje(con,idEmpresa,"UI_WS_ERROR").msg();
        		salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_WS_ERROR", msg));
    		}
        }
		long fin = System.currentTimeMillis();
		logger.info("FIN::EnviarOperacionesPendientes.envioAsignacionesSimultaneas {}", fin - inicio);
		return salida;
	}
	
	private ProcesoOperacionesPendientes procesamientoOperacionesPendientes(List<OperacionesPendientes> operacionesPendientes) {
		logger.info("INICIO::EnviarOperacionesPendientes.procesamientoOperacionesPendientes ");
		ProcesoOperacionesPendientes proceso = new ProcesoOperacionesPendientes();
		long inicio = System.currentTimeMillis();
		for (OperacionesPendientes op: operacionesPendientes) {
			if (op.p_IdTipo.equals("ASIG") && (!op.p_Producto.equals("SIMULTANEA"))) {	
				TypeAsignaciones typeAsignaciones = new TypeAsignaciones();
				typeAsignaciones.setFecha(new String(op.p_FechaIngresoRegistro));			
				typeAsignaciones.setFolio(new BigDecimal (op.p_FolioOperacion)) ;			
				typeAsignaciones.setTipoOperacion(new String(op.p_TipoOperacion));			
				typeAsignaciones.setRutCliente(new String(op.p_DocumentoIdentidad));		
				typeAsignaciones.setFondo(new String(""));			
				typeAsignaciones.setCantidad(new BigDecimal(op.p_Cantidad));				
				typeAsignaciones.setCodCorr(new Integer(conexion.p_CodigoCorredora));														
				typeAsignaciones.setUsuario(new String((conexion.p_usuario == null) ? "": conexion.p_usuario));													
				typeAsignaciones.setCorControl(new BigDecimal (op.p_IdGestion.toString())) ;
				proceso.asginaciones.add(typeAsignaciones);
			}
			Set<String> operacionesSim = new HashSet<>(Arrays.asList("ASIG", "ANT", "VENC"));
			if ("SIMULTANEA".equals(op.p_Producto) && operacionesSim.contains(op.p_IdTipo))	{

				IngresoAsignacion ingresoAsignacion = new IngresoAsignacion();
				ingresoAsignacion.setFolio(new BigDecimal(op.p_FolioOperacion));
				ingresoAsignacion.setNemo(op.p_NemoSVS.trim());
				ingresoAsignacion.setFechaOp(op.p_FechaIngresoRegistro.trim());
				ingresoAsignacion.setLado(op.p_TipoOperacion.trim());
				ingresoAsignacion.setCodCorr(new BigDecimal(conexion.p_CodigoCorredora));
				ingresoAsignacion.setCantidadAsignada(new BigDecimal(op.p_Cantidad));
				ingresoAsignacion.setRutCliente(op.p_DocumentoIdentidad.trim());
				ingresoAsignacion.setRutClienteNuevo(op.p_DocumentoIdentidad.trim());
				ingresoAsignacion.setModo("I");
				ingresoAsignacion.setIdGestion(new BigDecimal(op.p_IdGestion));
				ingresoAsignacion.setNombreCli(op.p_Nombre.trim());
				proceso.asignacionesSimultaneas.add(ingresoAsignacion);
			}
			
			if (op.p_IdTipo.equals("GTIA"))	{
		    	DetalleGtia detalleGtia = new DetalleGtia();	    	
		    	detalleGtia.setCantidad(new BigDecimal(op.p_Cantidad));
				detalleGtia.setCorControl(new String (op.p_IdGestion.toString()));
				detalleGtia.setDcv(new String(op.p_IndDcv.equalsIgnoreCase("") ? "" : "D"));
				detalleGtia.setFondoafp(new String(op.p_FondoAfp));
				detalleGtia.setIngegr(new String(op.p_TipoOperacion));
				detalleGtia.setMercado(new String(op.p_Categoria));
				detalleGtia.setNemo(new String(op.p_NemoSVS));
				detalleGtia.setRutcli(new String(op.p_DocumentoIdentidad));
				detalleGtia.setSistema(new String(op.p_IdBolsa));			
				detalleGtia.setTipogtia(new String(op.p_IdTipoGarantia));	
				detalleGtia.setCodigoSerieOpc("");								

				detalleGtia.setCodigoPrestamo(0);								
				detalleGtia.setRutPrestamista(new String(""));					
				detalleGtia.setFondoPrestamista(new String(""));				
				
				detalleGtia.setCodigoSerieOpcDestino("");						
				detalleGtia.setCuentaDestino(new String(""));   				
				detalleGtia.setFondoPrestamistaDestino(new String(""));			
				detalleGtia.setRutPrestamistaDestino(new String(""));			
				detalleGtia.setCodigoPrestamoDestino(0);						
				detalleGtia.setRutClienteDestino(new String(""));				
				proceso.garantias.add(detalleGtia);
			}
		}
		long fin = System.currentTimeMillis();
		logger.info("FIN::EnviarOperacionesPendientes.procesamientoOperacionesPendientes {} ", fin - inicio);		
		return proceso; 
	}
	
	
	private void escribirLog(Connection con,InEnviarOperacionesPendientes entrada, List<OperacionesPendientes> operacionesPendientes) throws Exception {
		logger.debug("INICIO::EnviarOperacionesPendientes.escribirLog");
		long inicio = System.currentTimeMillis();
		
		Random random = new Random();
		long rlong= random.nextLong();
		long nlong= random.nextLong();
		
		AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
		entradaAgregarLog.p_IdEmpresa= entrada.p_IdEmpresa;
		entradaAgregarLog.p_IdModulo = entrada.p_IdModulo;
		entradaAgregarLog.p_IdUsuario= entrada.p_IdUsuario;		
		entradaAgregarLog.p_UsaReglasNegocio= "N";
		entradaAgregarLog.p_IdEstadoAutorizacion="";
		
		// Generamos el log
		AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
		log.IdCabeceraLog = rlong;
		log.IdTipoLog = "INFORMATIVO";
		log.IdModulo = entrada.p_IdModulo;
		log.IdUsuario = entrada.p_IdUsuario;
		log.IdEmpresa = entrada.p_IdEmpresa;
		log.FechaSistemaLog = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
		log.IdUsuarioAtendido = entrada.p_IdUsuario;
		log.IdCliente = 0L;
		log.IdPortafolio = 0L;
		log.IdTipoRegistro = "GARANTIA";
		log.IdProceso = 0L;
		
		entradaAgregarLog.p_Log.add(log);

		// Detalle log
	    for (OperacionesPendientes operacion : operacionesPendientes) {	
			InAgregarLog.DetalleLog detLog = new InAgregarLog.DetalleLog();	
			detLog.IdCabeceraLog = rlong;
			detLog.IdTrabajo= nlong;
			detLog.SubRegistro = operacion.p_Producto;
			detLog.CampoRegistro ="ENVIO OPERACIONES PENDIENTES: " + operacion.p_IdTipoGarantia + " Folio: "+ operacion.p_FolioOperacion;
			detLog.ValorAntiguo = "NEMO: " + operacion.p_NemoSVS;
			detLog.ValorNuevo = "CANTIDAD ENVIADA : " + operacion.p_Cantidad;;
			entradaAgregarLog.p_LogDetalle.add(detLog);	
			System.out.println(detLog);
	
	    }
		AgregarLog agregarLogService = new AgregarLog();
		agregarLogService.logica(con, entradaAgregarLog);
		long fin = System.currentTimeMillis();
		logger.debug("FIN::EnviarOperacionesPendientes.escribirLog", fin - inicio);
	    

	}
	
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = new Gson();		
		InEnviarOperacionesPendientes in = gson.fromJson(jin.toString() ,InEnviarOperacionesPendientes.class);
				
		OutEnviarOperacionesPendientes out = logica(con,in);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		long c = 0;
		for (MensajeSalida s : out.P_OUT_MENSAJES) {
			if (s.tipo.equals("ADVERTENCIA"))
				c++;
		}
		out.P_OUT_ADVERTENCIASCONTADOR = c;
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static void main(String[] args) {		
		System.out.println("== INICIO PRUEBA LOCAL ==");

		try {
			Connection con = PlataformaUtils.getPropertiesConnection("master02","connections.properties");			
			Gson gson = Utils.getGsonBuilder();
			String json = "{\\\"p_IdEmpresa\\\":1,\\\"p_IdUsuario\\\":97,\\\"p_IdModulo\\\":\\\"CUS\\\",\\\"p_CodidoCorredor\\\":\\\"62\\\",\\\"p_OperacionesPendientes\\\":[{\\\"p_IdTipo\\\":\\\"ASIG\\\",\\\"p_TipoOperacion\\\":\\\"V\\\",\\\"p_FolioOperacion\\\":244343,\\\"p_FolioTransaccion\\\":0,\\\"p_DocumentoIdentidad\\\":\\\"20319956-2\\\",\\\"p_Cantidad\\\":200000,\\\"p_FechaIngresoRegistro\\\":\\\"2024-01-31\\\",\\\"p_IndDcv\\\":\\\"\\\",\\\"p_Categoria\\\":\\\"RV\\\",\\\"p_NemoSVS\\\":\\\"COPEC\\\",\\\"p_IdTipoGarantia\\\":\\\"00\\\",\\\"p_IdBolsa\\\":\\\"BCS\\\",\\\"p_FondoAfp\\\":\\\"\\\",\\\"p_IdInterno\\\":6370,\\\"p_Producto\\\":\\\"SIMULTANEA\\\",\\\"p_Nombre\\\":\\\"LUCIANO PORTALES JAZME\\\"}]}\";";
			InEnviarOperacionesPendientes Objin = gson.fromJson(json, InEnviarOperacionesPendientes.class);
			EnviarOperacionesPendientes demo=new EnviarOperacionesPendientes();
			
			try {
				OutEnviarOperacionesPendientes ObjOut = demo.logica(con, Objin);
 				System.out.println(ObjOut.toString());
 				con.rollback();	
			} catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}
		
		System.out.println("== FIN PRUEBA LOCAL ==");
	}
	
	
    

};
