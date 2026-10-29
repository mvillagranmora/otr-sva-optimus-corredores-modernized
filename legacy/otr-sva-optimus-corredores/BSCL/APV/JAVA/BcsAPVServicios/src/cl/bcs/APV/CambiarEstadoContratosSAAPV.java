 package cl.bcs.APV;

import java.math.BigDecimal;
import java.sql.Connection;
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

import cl.bcs.APV.GrabarRecaudacionTraspaso.CambiarEstadoTraspaso;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo;
import cl.bcs.plataforma.aut.CrearEventoAutorizacionMasivo.ObjTABLAEVENTOS;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.tesoreria.AnulacionMandatoPAC;
import cl.bcs.tesoreria.RevocacionMandatoPAC;
import cl.bcs.tesoreria.AnulacionMandatoPAC.EntradaAnulacionContrato;
import cl.bcs.tesoreria.AnulacionMandatoPAC.SalidaAnulacionContrato;
import cl.bcs.tesoreria.RevocacionMandatoPAC.EntradaRevocacionMandato;
import cl.bcs.tesoreria.RevocacionMandatoPAC.InMandato;
import cl.bcs.tesoreria.RevocacionMandatoPAC.SalidaRevocacionMandato;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CambiarEstadoContratosSAAPV implements JavaCall{	
	
	static final Logger logger = LogManager.getLogger(CambiarEstadoContratosSAAPV.class);
	
	private final String SQL_UPDATE_REVOCAR_CONTRATOS 		= loadResourceToString("cl/bcs/APV/sql/CambiarEstadoContratosSAAPV.sql");
	private final String SQL_UPDATE_GLOSA_CONTRATOS 		= loadResourceToString("cl/bcs/APV/sql/CambiarGlosaContratosSAAPV.sql");
	private final String SQL_SELECT_CONSULTAR_DATOS_PERSONA = loadResourceToString("cl/bcs/APV/sql/ConsultarDatosPersona.sql");
	private final String SQL_CAMBIAR_ESTADO_TRASPASO 		= loadResourceToString("cl/bcs/APV/sql/CambiarEstadoTraspasoAPV.sql");
	private final String SQL_SELECT_CONTRATO_TRASPASO 		= loadResourceToString("cl/bcs/APV/sql/ConsultarExisteContratoTraspaso.sql");
	private final String SQL_SELECT_CONTRATO_TRASPASO_CERRAR= loadResourceToString("cl/bcs/APV/sql/CambiarEstadoContratosSAAPV_SelectContratoTraspasoCerrar.sql");
	private final String SQL_SELECT_CONSULTAR_ESTADO_GLOSA  = loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_ConsultarEstadoGlosa.sql");
	private final String SQL_UPDATE_INGRESO_EGRESO_APV      = loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_Modificar_Ingreso_Egreso.sql");	
	private final String SQL_SELECT_INGRESO_EGRESO_APV      = loadResourceToString("cl/bcs/APV/sql/ContratoSAAPV_Consulta_Ingreso_Egreso.sql");
	private final String SQL_SELECT_CONTRATO 				= loadResourceToString("cl/bcs/APV/sql/CambiarEstadoContratosSAAPV_SelectContratos.sql");

	public static class ContratosSAAPV {
		public Long					IdPersona;
		public Long					IdCliente;
		public Long					IdEmpleador;
		public String				IdFolioContratoSAAPV;
		public String				FechaRevocacion; 
		public String				IdEstadoEnvioNotificacion;
		public String				IdEstadoSolicitud;
		public String				IdEstado;
		
	    public Long 				IdGlosa;
	    public String 				DescripcionGlosa;
	    
	    public ContratosSAAPV() {
	    	IdGlosa = 0L;
	    	DescripcionGlosa = "";
	    }
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ConsultaContratoEstado {
		public  Long		IdContratoSAAPV;
	    public 	String		IdEstado;
	    public  Long		IdTraspaso;
	    public  String      FolioContratoSAAPV;
	    public String 		EstadoPAC;
	    public Long 		IdMandatoPAC;
		
	    public ConsultaContratoEstado(){
	    	EstadoPAC = "";
	    	IdMandatoPAC = 0L;
		}
	    
	    public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class MandatoPACRelacionado {
        public Long		IdMandatoPAC;
        public Long		IdContratoSAAPV;
        public String	FolioContratoSAAPV;
        public String	Estado="";
        
        
        public String toString() {
                return Utils.printJson(this, false);
        }
	};

	public static class ContratosSAAPV_Entrada {
	    public Long				p_IdEmpresa;
	    public Long				p_IdUsuario;
	    public String			p_IdModulo;
	    public String			p_Opcion; // REV = Revocacion, CER = Cerrar, COM = Completar 
	    public List<ContratosSAAPV>	p_ContratosSAAPV;
	    public String			p_DesdePac;
	    
	    public ContratosSAAPV_Entrada(){
	    	p_ContratosSAAPV = new ArrayList<ContratosSAAPV>();
	    	p_DesdePac		 = "N";
		}
	   
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	public static class ContratosSAAPV_Salida {		
		public List<MensajeSalida>	P_OUT_MENSAJES;
		public Long					P_OUT_ERRORESCONTADOR;
		public Long					P_OUT_ADVERTENCIASCONTADOR;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public ContratosSAAPV_Salida(){
			P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			P_OUT_ERRORESCONTADOR = 0L;
			P_OUT_ADVERTENCIASCONTADOR = 0L;
		}		
	};
	
	public static class ConsultarDatosPersona {
		public String DocumentoIdentidad;
		public String Nombres;
	};
	
	public static class ConsultarEstadoGlosa{
		public String				IdEstadoEnvioNotificacion;
		public String				IdEstadoSolicitud;
		public String				IdEstado;
	    public Long 				IdGlosa;
	    public String 				DescripcionGlosa;
	};
		
	public ContratosSAAPV_Salida logica(Connection con, ContratosSAAPV_Entrada in) throws Exception {
		logger.info("Inicio CambiarEstadoContratosSAAPV");
		ContratosSAAPV_Salida out = new ContratosSAAPV_Salida();
		SQL sql = new SQL();
		
		/*Parametro para saber si se autoriza por el blotter de autorizacion o no.*/
		String envioBlotterAutorizacion = ConfiguracionSistema.getParametroConfiguracionString(con,in.p_IdEmpresa, "PLATAFORMA","APV.AutorizaGestionContrato");

		try {
			//******************************************************************************************************************
			//													LOGICA
			//******************************************************************************************************************
			String modo = "";
			
			if(in.p_Opcion.compareTo("REV") == 0){
				modo = "REVOCA";
			}else if(in.p_Opcion.compareTo("CER") == 0){
				modo = "CERRAR";
			}else if(in.p_Opcion.compareTo("VIG") == 0){
				modo = "VIGENT";
			}else{
				modo = "COMPLE";
			}	
			
			if(envioBlotterAutorizacion.compareTo("S") == 0 && modo !=""){
				CrearEventoAutorizacionMasivo.ServiceEntrada inCrearAutorizacion = new CrearEventoAutorizacionMasivo.ServiceEntrada();
				List<ObjTABLAEVENTOS> tablaEventos = new ArrayList<ObjTABLAEVENTOS>();
				
				for (ContratosSAAPV row : in.p_ContratosSAAPV) {
					
					ObjTABLAEVENTOS evento = new ObjTABLAEVENTOS();
					
					evento.IdTipoEvento = modo;
					evento.IdModuloSolicita = "APV";
					evento.IdUsuarioSolicita = in.p_IdUsuario;
					evento.Fecha = row.FechaRevocacion;
					evento.IdTipoOperacion = null;
					evento.IdConcepto = modo;
					evento.NumeroOperacionConcepto = row.IdFolioContratoSAAPV;
					evento.FechaOperacionConcepto = row.FechaRevocacion;
					evento.IdCliente = row.IdCliente;
					evento.IdFuncionario = 0L;
					evento.IdProveedor = 0L;
					evento.IdTipoProducto = null;
					evento.IdClasificacionTipoInstrumento = null;
					evento.IdTipoInstrumento = null;
					evento.IdInstrumento = null;
					evento.Nemo = null;
					evento.Cantidad = null;
					evento.Monto = null;
					evento.Precio = null;
					evento.IdMoneda = "CLP";
					
					HashMap<String, Object> param = new HashMap<>();
					param.put("IdPersona", row.IdPersona);
					
					List<ConsultarDatosPersona> ConsultarDatosPersona = sql.query(con, SQL_SELECT_CONSULTAR_DATOS_PERSONA, param, ConsultarDatosPersona.class);
					
					if (ConsultarDatosPersona.size() > 0)
					{
						if(in.p_Opcion.compareTo("REV") == 0 || in.p_Opcion.compareTo("CER") == 0)
						{
							evento.Glosa = ConsultarDatosPersona.get(0).DocumentoIdentidad + " / " + ConsultarDatosPersona.get(0).Nombres + " / " + row.IdFolioContratoSAAPV + " / " + row.DescripcionGlosa;
						}
						else
						{
							evento.Glosa = ConsultarDatosPersona.get(0).DocumentoIdentidad + " / " + ConsultarDatosPersona.get(0).Nombres + " / " + row.IdFolioContratoSAAPV;
						}
					}else
					{
						evento.Glosa = row.IdFolioContratoSAAPV;
					}
					
					tablaEventos.add(evento);
				}
				
				if (tablaEventos.size() > 0) {
					CrearEventoAutorizacionMasivo crearAutorizacionService = new CrearEventoAutorizacionMasivo();
					inCrearAutorizacion.P_IDEMPRESA = in.p_IdEmpresa;
					inCrearAutorizacion.P_IDUSUARIO = in.p_IdUsuario;
					inCrearAutorizacion.P_IDMODULO = in.p_IdModulo;
					inCrearAutorizacion.P_SIMULAROPERACION = "N";
					inCrearAutorizacion.P_USAREGLASNEGOCIO = "N";
					inCrearAutorizacion.P_TABLAEVENTOS = tablaEventos;
					crearAutorizacionService.logica(con, inCrearAutorizacion);
				}
				
				ConsultarEstadoGlosa ValorAntiguoLog = ConsultarEstadoGlosa(con, sql, in.p_ContratosSAAPV.get(0).IdFolioContratoSAAPV);
				sql.batch(con,SQL_UPDATE_REVOCAR_CONTRATOS,in.p_ContratosSAAPV, ContratosSAAPV.class);
				sql.batch(con,SQL_UPDATE_GLOSA_CONTRATOS,in.p_ContratosSAAPV, ContratosSAAPV.class);
				AgregarLogSAAPV(con, in, ValorAntiguoLog, in.p_ContratosSAAPV.get(0).IdEstadoSolicitud, in.p_ContratosSAAPV.get(0).IdEstadoEnvioNotificacion);
				
			}else{
				List<ContratosSAAPV> Contratos = new ArrayList<ContratosSAAPV>();
				HashMap<String,Object> paramConsulta = new HashMap<>();
				List<String> listaIdFolioContratoSAAPV = new ArrayList<>();
				List<Long> listaIdPersona = new ArrayList<>();
				
				for (ContratosSAAPV row : in.p_ContratosSAAPV) {
					ContratosSAAPV contratosaapv = new ContratosSAAPV();
					contratosaapv.IdPersona = row.IdPersona;
					contratosaapv.IdCliente = row.IdCliente;
					contratosaapv.IdEmpleador = row.IdEmpleador;
					contratosaapv.IdFolioContratoSAAPV = row.IdFolioContratoSAAPV;
					contratosaapv.FechaRevocacion = row.FechaRevocacion; 
					contratosaapv.IdEstadoEnvioNotificacion = "";
					contratosaapv.IdEstadoSolicitud = "";
					contratosaapv.IdEstado = row.IdEstado;
					contratosaapv.IdGlosa = row.IdGlosa;
					contratosaapv.DescripcionGlosa = row.DescripcionGlosa;
					Contratos.add(contratosaapv);
					
					if (!listaIdFolioContratoSAAPV.contains(row.IdFolioContratoSAAPV)) {
						listaIdFolioContratoSAAPV.add(row.IdFolioContratoSAAPV);
				    }
					if (!listaIdPersona.contains(row.IdPersona)) {
						listaIdPersona.add(row.IdPersona);
				    }
				}				
					
				paramConsulta.put("IdFolioContratoSAAPV", listaIdFolioContratoSAAPV);
				paramConsulta.put("IdPersona", listaIdPersona);
				
				 if(in.p_Opcion.compareTo("ANU") == 0){
					List<ConsultaContratoEstado> ConsultaContratoTraspaso = sql.query(con, SQL_SELECT_CONTRATO_TRASPASO, paramConsulta, ConsultaContratoEstado.class);
					List<CambiarEstadoTraspaso> lstTraspaso = new ArrayList<CambiarEstadoTraspaso>();
					List<ContratosSAAPV> ContratosAnular = new ArrayList<ContratosSAAPV>();
					
					
					for (ConsultaContratoEstado traspaso: ConsultaContratoTraspaso) {
					
						if("PDI".equalsIgnoreCase(traspaso.IdEstado)){
							
							for (ContratosSAAPV row : in.p_ContratosSAAPV) {
								if(traspaso.FolioContratoSAAPV.equalsIgnoreCase(row.IdFolioContratoSAAPV)) {
									ContratosSAAPV contratosaapv2 = new ContratosSAAPV();
									contratosaapv2.IdPersona = row.IdPersona;
									contratosaapv2.IdCliente = row.IdCliente;
									contratosaapv2.IdEmpleador = row.IdEmpleador;
									contratosaapv2.IdFolioContratoSAAPV = row.IdFolioContratoSAAPV;
									contratosaapv2.FechaRevocacion = row.FechaRevocacion; 
									contratosaapv2.IdEstadoEnvioNotificacion = "";
									contratosaapv2.IdEstadoSolicitud = "";
									contratosaapv2.IdEstado = row.IdEstado;
									contratosaapv2.IdGlosa = row.IdGlosa;
									contratosaapv2.DescripcionGlosa = row.DescripcionGlosa;
									ContratosAnular.add(contratosaapv2);
								}
							}
						
						//validar si existe traspaso de ingreso para cambiarlo 
							if(traspaso.IdTraspaso > 0){
								CambiarEstadoTraspaso Traspaso = new CambiarEstadoTraspaso();
								Traspaso.IdContratoSAAPV = traspaso.IdContratoSAAPV;
								Traspaso.IdEmpresa = in.p_IdEmpresa;
								Traspaso.IdTraspaso = traspaso.IdTraspaso;
								Traspaso.IdEstado = "ANU";
								Traspaso.CierreSolicitud = in.p_ContratosSAAPV.get(0).FechaRevocacion;
								lstTraspaso.add(Traspaso);
							}	
						}else {
							if("S".equals(in.p_DesdePac)) {
								out.P_OUT_ERRORESCONTADOR += 1;
		                        String mensaje = "Contrato SAAPV con Folio "  + traspaso.FolioContratoSAAPV + " debe estar en estado Pendiente de Ingreso";
		                        MensajeSalida msg= new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"ERROR_ANULAR_CONTRATO",mensaje);                        
		                        out.P_OUT_MENSAJES.add(msg);
							}
						}
					}
					
					if (lstTraspaso.size()>0) {
						int[] result = sql.batch(con,SQL_CAMBIAR_ESTADO_TRASPASO,lstTraspaso, CambiarEstadoTraspaso.class);
					}
					
					 ConsultarEstadoGlosa ValorAntiguoLog = ConsultarEstadoGlosa(con, sql, in.p_ContratosSAAPV.get(0).IdFolioContratoSAAPV);
					 sql.batch(con,SQL_UPDATE_REVOCAR_CONTRATOS,ContratosAnular, ContratosSAAPV.class);
					 sql.batch(con,SQL_UPDATE_GLOSA_CONTRATOS,ContratosAnular, ContratosSAAPV.class);
					 
					 if(!ContratosAnular.isEmpty()) {
						 AgregarLogSAAPV(con, in, ValorAntiguoLog, ContratosAnular.get(0).IdEstadoSolicitud, ContratosAnular.get(0).IdEstadoEnvioNotificacion);
					 }
					 
					 logger.debug(out.P_OUT_MENSAJES);
					 //AnulacionMandatoPAC
					 if(!in.p_DesdePac.equals("S")) {
						 AnulacionMandatoPAC anuMandato = new AnulacionMandatoPAC();
		                 AnulacionMandatoPAC.EntradaAnulacionContrato anuMandatoIn = new  AnulacionMandatoPAC.EntradaAnulacionContrato();
		                 anuMandatoIn.p_IdEmpresa = in.p_IdEmpresa;
		                 anuMandatoIn.p_IdUsuario = in.p_IdUsuario;
		                 anuMandatoIn.p_IdModulo = "TES";
		                 anuMandatoIn.p_DesdeSAAPV ="S";
		                
		                 List<String> IdContratosSAAPV = in.p_ContratosSAAPV.stream().map(m -> m.IdFolioContratoSAAPV).collect(Collectors.toList());
		                
		                 HashMap<String,Object> params = new HashMap<String,Object>();
		                 params.put("FolioContratoSAAPV", IdContratosSAAPV);
	
		                 List<MandatoPACRelacionado> mandatosRelacionados = sql.query(con, SQL_SELECT_CONTRATO, params, CambiarEstadoContratosSAAPV.MandatoPACRelacionado.class);
	
		                 for (MandatoPACRelacionado mov : mandatosRelacionados) { 	
			            	 AnulacionMandatoPAC.InContrato AnularMandato = new AnulacionMandatoPAC.InContrato();
	                    	 AnularMandato.IdContrato = mov.IdMandatoPAC;
	                    	 AnularMandato.Estado = mov.Estado;
	                         anuMandatoIn.p_Contrato.add(AnularMandato);
		                 }
		                
		                 if(anuMandatoIn.p_Contrato.size() != 0) {
		                	 AnulacionMandatoPAC.SalidaAnulacionContrato anuMandatoOut = anuMandato.logica(con, anuMandatoIn);
		                    
		                     if(anuMandatoOut.P_OUT_ERRORESCONTADOR.longValue() > 0L) {
		                    	 out.P_OUT_ERRORESCONTADOR = anuMandatoOut.P_OUT_ERRORESCONTADOR;
		                         out.P_OUT_MENSAJES = anuMandatoOut.P_OUT_MENSAJES ;
		                         return out;
		                     }
		                   
		                     for(MensajeSalida mensajeMandato : anuMandatoOut.P_OUT_MENSAJES) {
		                    	out.P_OUT_MENSAJES.add(mensajeMandato);
		                     }
		                 }
	                 }
	                 
	                 for (ContratosSAAPV salida : ContratosAnular ) {
                         HashMap<String,String> valores = new HashMap<String,String>();
                         valores.put("IdContrato",salida.IdFolioContratoSAAPV);
                         MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"ANULACION_CONTRATO_SAAPV_EXITOSA",valores,con,in.p_IdEmpresa);
                         out.P_OUT_MENSAJES.add(msg);
    				 }
					 
				 }
				 else if("CER".equals(in.p_Opcion)){
					Map<String,ConsultaContratoEstado> ConsultaContratoTraspaso = consultarContratoTraspaso(con,paramConsulta);
					List<CambiarEstadoTraspaso> lstTraspaso = new ArrayList<CambiarEstadoTraspaso>();
					List<ContratosSAAPV> ContratosCerrar = new ArrayList<ContratosSAAPV>();
					AnulacionMandatoPAC servicioAnularMandato = new AnulacionMandatoPAC();
					EntradaAnulacionContrato entradaAnularMandatoPAC = new EntradaAnulacionContrato();
					
					for (ContratosSAAPV row : in.p_ContratosSAAPV) {
						ConsultaContratoEstado contratosTraspasos = ConsultaContratoTraspaso.get(row.IdFolioContratoSAAPV);
						
						if("PDI".equalsIgnoreCase(contratosTraspasos.IdEstado)||contratosTraspasos.IdEstado== null || "TPD".equalsIgnoreCase(contratosTraspasos.IdEstado)){
							
							if("TPD".equalsIgnoreCase(contratosTraspasos.IdEstado)){
								String descripcion = Diccionario.getMensaje(con, in.p_IdEmpresa, "ERROR_CERRAR_CONTRATO_SIN_INFO_CLIENTE").msg();
								descripcion = descripcion.replace("::IdContrato::",contratosTraspasos.IdContratoSAAPV.toString()).replace("::IdTraspaso::",contratosTraspasos.IdTraspaso.toString());
								out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "ERROR_CERRAR_CONTRATO_SIN_INFO_CLIENTE", descripcion));
								out.P_OUT_ADVERTENCIASCONTADOR += 1;
							}
							
							if(contratosTraspasos.FolioContratoSAAPV.equalsIgnoreCase(row.IdFolioContratoSAAPV)) {
								ContratosSAAPV contratosaapv2 = new ContratosSAAPV();
								contratosaapv2.IdPersona = row.IdPersona;
								contratosaapv2.IdCliente = row.IdCliente;
								contratosaapv2.IdEmpleador = row.IdEmpleador;
								contratosaapv2.IdFolioContratoSAAPV = row.IdFolioContratoSAAPV;
								contratosaapv2.FechaRevocacion = row.FechaRevocacion; 
								contratosaapv2.IdEstadoEnvioNotificacion = "";
								contratosaapv2.IdEstadoSolicitud = "";
								contratosaapv2.IdEstado = row.IdEstado;
								contratosaapv2.IdGlosa = row.IdGlosa;
								contratosaapv2.DescripcionGlosa = row.DescripcionGlosa;
								ContratosCerrar.add(contratosaapv2);
								
								AnulacionMandatoPAC.InContrato contrato = new AnulacionMandatoPAC.InContrato();
								contrato.IdContrato 		= contratosTraspasos.IdMandatoPAC;
								if(contratosTraspasos.IdMandatoPAC != null) {
									contrato.Estado = "ANU";
									entradaAnularMandatoPAC.p_Contrato.add(contrato);	
								}
							}
							
							//validar si existe traspaso de ingreso para cambiarlo 
							if(contratosTraspasos.IdTraspaso > 0){
								CambiarEstadoTraspaso Traspaso = new CambiarEstadoTraspaso();
								Traspaso.IdContratoSAAPV = contratosTraspasos.IdContratoSAAPV;
								Traspaso.IdEmpresa = in.p_IdEmpresa;
								Traspaso.IdTraspaso = contratosTraspasos.IdTraspaso;
								Traspaso.IdEstado = "CER";
								Traspaso.CierreSolicitud = row.FechaRevocacion;
								lstTraspaso.add(Traspaso);
							}
						}else if("TPD".equalsIgnoreCase(contratosTraspasos.IdEstado)){
							String descripcion = Diccionario.getMensaje(con, in.p_IdEmpresa, "ERROR_CERRAR_CONTRATO_SIN_INFO_CLIENTE").msg();
							descripcion = descripcion.replace("::IdContrato::",contratosTraspasos.IdContratoSAAPV.toString()).replace("::IdTraspaso::",contratosTraspasos.IdTraspaso.toString());
							out.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "ERROR_CERRAR_CONTRATO_SIN_INFO_CLIENTE", descripcion));
							out.P_OUT_ERRORESCONTADOR += 1;
						}
					}
					
					if (lstTraspaso.size()>0) {
						int[] result = sql.batch(con,SQL_CAMBIAR_ESTADO_TRASPASO,lstTraspaso, CambiarEstadoTraspaso.class);
						
					}
					
					//Anula Mandato PAC	
					if (entradaAnularMandatoPAC.p_Contrato.size() > 0){
						entradaAnularMandatoPAC.p_IdEmpresa = in.p_IdEmpresa;
						entradaAnularMandatoPAC.p_IdUsuario = in.p_IdUsuario;
						entradaAnularMandatoPAC.p_IdModulo = in.p_IdModulo;
						SalidaAnulacionContrato salidaAnularMandatoPAC = servicioAnularMandato.logica(con, entradaAnularMandatoPAC);
						
						out.P_OUT_MENSAJES.addAll(salidaAnularMandatoPAC.P_OUT_MENSAJES);
						if(salidaAnularMandatoPAC.P_OUT_ERRORESCONTADOR.compareTo(0L) != 0) {
							out.P_OUT_ERRORESCONTADOR += salidaAnularMandatoPAC.P_OUT_ERRORESCONTADOR;
						}
					}
					
				     ConsultarEstadoGlosa ValorAntiguoLog = ConsultarEstadoGlosa(con, sql, in.p_ContratosSAAPV.get(0).IdFolioContratoSAAPV);
					 sql.batch(con,SQL_UPDATE_REVOCAR_CONTRATOS,ContratosCerrar, ContratosSAAPV.class);
					 sql.batch(con,SQL_UPDATE_GLOSA_CONTRATOS,ContratosCerrar, ContratosSAAPV.class);
					 
					 if(!ContratosCerrar.isEmpty()) {
						 AgregarLogSAAPV(con, in, ValorAntiguoLog,  ContratosCerrar.get(0).IdEstadoSolicitud, ContratosCerrar.get(0).IdEstadoEnvioNotificacion);
					 }
					 
					 logger.debug(out.P_OUT_MENSAJES);
					 
					 
				 }
				 else if("REV".equals(in.p_Opcion)){
					 Map<String,ConsultaContratoEstado> ConsultaContratoTraspaso = consultarContratoTraspaso(con,paramConsulta);
					 List<ConsultaContratoEstado> ContratosRevocar = new ArrayList<ConsultaContratoEstado>();
						
						for (ContratosSAAPV row : in.p_ContratosSAAPV) {
							ConsultaContratoEstado contratosParaRevocar = ConsultaContratoTraspaso.get(row.IdFolioContratoSAAPV);			
								
								if(contratosParaRevocar.FolioContratoSAAPV.equalsIgnoreCase(row.IdFolioContratoSAAPV)) {
									ConsultaContratoEstado contratosaapv2 = new ConsultaContratoEstado();
									contratosaapv2.IdContratoSAAPV = contratosParaRevocar.IdContratoSAAPV;
									contratosaapv2.IdEstado ="ANU";    //row.IdEstado;
									contratosaapv2.IdTraspaso =contratosParaRevocar.IdTraspaso;
									contratosaapv2.FolioContratoSAAPV = row.IdFolioContratoSAAPV;				
									contratosaapv2.EstadoPAC="";
									contratosaapv2.IdMandatoPAC=0L;
									 
									ContratosRevocar.add(contratosaapv2);	
								}										
						}
										 
				     

					 ConsultarEstadoGlosa ValorAntiguoLog = ConsultarEstadoGlosa(con, sql, in.p_ContratosSAAPV.get(0).IdFolioContratoSAAPV);
					 sql.batch(con,SQL_UPDATE_REVOCAR_CONTRATOS,Contratos, ContratosSAAPV.class);
					 sql.batch(con,SQL_UPDATE_GLOSA_CONTRATOS,Contratos, ContratosSAAPV.class);
					 	
					 if (!Contratos.isEmpty()) {
						 AgregarLogSAAPV(con, in, ValorAntiguoLog, Contratos.get(0).IdEstadoSolicitud, Contratos.get(0).IdEstadoEnvioNotificacion);
					 }
					 			 
					 ConsultarEstadoGlosa ValorAntiguoTraspasoLog = ConsultarEstadoTraspaso(con, sql, in.p_ContratosSAAPV.get(0).IdFolioContratoSAAPV); 					 
					 if (ValorAntiguoTraspasoLog != null) {
						 sql.batch(con,SQL_UPDATE_INGRESO_EGRESO_APV,ContratosRevocar, ConsultaContratoEstado.class);				
						 ContratosSAAPV_Entrada  inn = new ContratosSAAPV_Entrada();		     	
						 Utils.copyPojo(in, inn) ;
						 inn.p_ContratosSAAPV.get(0).IdEstado = ContratosRevocar.get(0).IdEstado;
						 inn.p_ContratosSAAPV.get(0).DescripcionGlosa= ValorAntiguoTraspasoLog.DescripcionGlosa;
						 AgregarLogSAAPV(con, inn, ValorAntiguoTraspasoLog, ContratosRevocar.get(0).IdEstado, Contratos.get(0).IdEstadoEnvioNotificacion);							
					 }
					//RevocacionMandatoPAC
					 if(!in.p_DesdePac.equals("S")) {
						 SalidaRevocacionMandato revMandatoOut =  new SalidaRevocacionMandato();
			             RevocacionMandatoPAC revMandato = new RevocacionMandatoPAC();
	                     EntradaRevocacionMandato revMandatoIn = new EntradaRevocacionMandato();
	                     revMandatoIn.p_IdEmpresa = in.p_IdEmpresa;
	                     revMandatoIn.p_IdUsuario = in.p_IdUsuario;
	                     revMandatoIn.p_IdModulo = "TES";
	                     revMandatoIn.p_DesdeSAAPV ="S";
	                     
	                     List<String> IdContratosSAAPV = in.p_ContratosSAAPV.stream().map(m -> m.IdFolioContratoSAAPV).collect(Collectors.toList());
			             HashMap<String,Object> params = new HashMap<String,Object>();
			             params.put("FolioContratoSAAPV", IdContratosSAAPV); 
			             List<MandatoPACRelacionado> mandatosRelacionados = sql.query(con, SQL_SELECT_CONTRATO, params, CambiarEstadoContratosSAAPV.MandatoPACRelacionado.class);
	           
			             for (MandatoPACRelacionado mov : mandatosRelacionados) { 	
			            	 InMandato revocarMandato = new InMandato();
			            	 revocarMandato.IdMandato = mov.IdMandatoPAC;
			            	 revocarMandato.Estado = mov.Estado;
			            	 revMandatoIn.p_Mandato.add(revocarMandato);
		                 }
	
		                 if(revMandatoIn.p_Mandato.size() != 0) {
		                	 revMandatoOut =  revMandato.logica(con, revMandatoIn);
	    	                
			                 if(revMandatoOut.P_OUT_ERRORESCONTADOR.longValue() > 0L) {
		                         out.P_OUT_ERRORESCONTADOR = revMandatoOut.P_OUT_ERRORESCONTADOR;
		                         out.P_OUT_MENSAJES = revMandatoOut.P_OUT_MENSAJES ;
		                         return out;
			                 }
	
			                 for(MensajeSalida mensajeMandato : revMandatoOut.P_OUT_MENSAJES) {
			                	 out.P_OUT_MENSAJES.add(mensajeMandato);
			                 }	                	
		                 }
				 	 }
					 for (ConsultaContratoEstado salida : ContratosRevocar ) {
                         HashMap<String,String> valores = new HashMap<String,String>();
                         valores.put("IdContrato",salida.FolioContratoSAAPV);
                         MensajeSalida msg = new MensajeSalida(0L,MensajeSalida.MENSAJE_OK,"REVOCACION_CONTRATO_SAAPV_EXITOSA",valores,con,in.p_IdEmpresa);
                         out.P_OUT_MENSAJES.add(msg);
    				 }					 
				 }
				 else if("VIG".equals(in.p_Opcion)){
					 Map<String,ConsultaContratoEstado> ConsultaContratoTraspaso = consultarContratoTraspaso(con,paramConsulta);
					 List<ConsultaContratoEstado> ContratosCambioEstadoTraspaso = new ArrayList<ConsultaContratoEstado>();
						
						for (ContratosSAAPV row : in.p_ContratosSAAPV) {
							ConsultaContratoEstado contratosDejarVigente = ConsultaContratoTraspaso.get(row.IdFolioContratoSAAPV);			
								
								if(contratosDejarVigente.FolioContratoSAAPV.equalsIgnoreCase(row.IdFolioContratoSAAPV)) {
									ConsultaContratoEstado contratosaapv2 = new ConsultaContratoEstado();
									contratosaapv2.IdContratoSAAPV = contratosDejarVigente.IdContratoSAAPV;
									contratosaapv2.IdEstado ="PDI";    //row.IdEstado;
									contratosaapv2.IdTraspaso =contratosDejarVigente.IdTraspaso;
									contratosaapv2.FolioContratoSAAPV = row.IdFolioContratoSAAPV;				
									contratosaapv2.EstadoPAC="";
									contratosaapv2.IdMandatoPAC=0L;
									 
									ContratosCambioEstadoTraspaso.add(contratosaapv2);	
								}										
						}
										 
				     

					 ConsultarEstadoGlosa ValorAntiguoLog = ConsultarEstadoGlosa(con, sql, in.p_ContratosSAAPV.get(0).IdFolioContratoSAAPV);
					 sql.batch(con,SQL_UPDATE_REVOCAR_CONTRATOS,Contratos, ContratosSAAPV.class);
					 sql.batch(con,SQL_UPDATE_GLOSA_CONTRATOS,Contratos, ContratosSAAPV.class);
					 	
					 if (!Contratos.isEmpty()) {
						 AgregarLogSAAPV(con, in, ValorAntiguoLog, Contratos.get(0).IdEstadoSolicitud, Contratos.get(0).IdEstadoEnvioNotificacion);
					 }
					 			 
					 ConsultarEstadoGlosa ValorAntiguoTraspasoLog = ConsultarEstadoTraspaso(con, sql, in.p_ContratosSAAPV.get(0).IdFolioContratoSAAPV); 					 
					 if (ValorAntiguoTraspasoLog != null) {
						 sql.batch(con,SQL_UPDATE_INGRESO_EGRESO_APV,ContratosCambioEstadoTraspaso, ConsultaContratoEstado.class);				
						 ContratosSAAPV_Entrada  inn = new ContratosSAAPV_Entrada();		     	
						 Utils.copyPojo(in, inn) ;
						 inn.p_ContratosSAAPV.get(0).IdEstado = ContratosCambioEstadoTraspaso.get(0).IdEstado;
						 inn.p_ContratosSAAPV.get(0).DescripcionGlosa= ValorAntiguoTraspasoLog.DescripcionGlosa;
						 AgregarLogSAAPV(con, inn, ValorAntiguoTraspasoLog, ContratosCambioEstadoTraspaso.get(0).IdEstado, Contratos.get(0).IdEstadoEnvioNotificacion);							
					 }		 
					 

				 } else {						 

					 
					 ConsultarEstadoGlosa ValorAntiguoLog = ConsultarEstadoGlosa(con, sql, in.p_ContratosSAAPV.get(0).IdFolioContratoSAAPV);
					 sql.batch(con,SQL_UPDATE_REVOCAR_CONTRATOS,Contratos, ContratosSAAPV.class);
					 sql.batch(con,SQL_UPDATE_GLOSA_CONTRATOS,Contratos, ContratosSAAPV.class);
					 
					 if (!Contratos.isEmpty()) {
						 AgregarLogSAAPV(con, in, ValorAntiguoLog, Contratos.get(0).IdEstadoSolicitud, Contratos.get(0).IdEstadoEnvioNotificacion);
					 }
					 
					 logger.debug(out.P_OUT_MENSAJES);
				 }
			}
		}
		finally {
			//algo
		}
		logger.info("Fin CambiarEstadoContratosSAAPV");
		return out;
	}
	
	public Map<String,ConsultaContratoEstado> consultarContratoTraspaso(Connection con,HashMap<String,Object> paramConsulta) throws Exception{
		logger.debug("Inicio::CambiarEstadoContratosSAAPV.consultarContratoTraspaso");
		long inicioServicio = System.currentTimeMillis();
		
		SQL sql = new SQL();
		Map<String,ConsultaContratoEstado> traspasos = sql.query(con, SQL_SELECT_CONTRATO_TRASPASO_CERRAR, paramConsulta, ConsultaContratoEstado.class)
				.stream().collect(Collectors.toMap(traspaso -> traspaso.FolioContratoSAAPV, traspaso -> traspaso));
		
		long finServicio = System.currentTimeMillis();
		logger.debug("Fin::CambiarEstadoContratosSAAPV.consultarContratoTraspaso", finServicio - inicioServicio);
		return traspasos;
	}
	
	public void AgregarLogSAAPV(Connection con, ContratosSAAPV_Entrada in, ConsultarEstadoGlosa ValorAntiguo, String EstadoSolicitud, String EstadoEnvioNotificacion) {
		try {	
			String FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");	
			String FolioContratoSAAPV = in.p_ContratosSAAPV.get(0).IdFolioContratoSAAPV;
			String IdEstadoEnvioNotificacion = EstadoEnvioNotificacion;
			String IdEstadoSolicitud = EstadoSolicitud;
			String IdEstado = in.p_ContratosSAAPV.get(0).IdEstado;
			Long IdGlosa = in.p_ContratosSAAPV.get(0).IdGlosa;
			String DescripcionGlosa = in.p_ContratosSAAPV.get(0).DescripcionGlosa;
			Long IdCliente = in.p_ContratosSAAPV.get(0).IdCliente;
			Long IdPersona = in.p_ContratosSAAPV.get(0).IdPersona;
			Long IdCabeceraLog = (IdCliente == 0) ? IdPersona : IdCliente;
			
			String	ValorAntiguoIdEstadoEnvioNotificacion = "";
			String	ValorAntiguoIdEstadoSolicitud = "";
			String	ValorAntiguoIdEstado = "";
		    Long 	ValorAntiguoIdGlosa = 0L;
		    String 	ValorAntiguoDescripcionGlosa = "";
		    
		    if(ValorAntiguo != null){
		    	ValorAntiguoIdEstadoEnvioNotificacion = ValorAntiguo.IdEstadoEnvioNotificacion;
		    	ValorAntiguoIdEstadoSolicitud = ValorAntiguo.IdEstadoSolicitud;
				ValorAntiguoIdEstado = ValorAntiguo.IdEstado;
			    ValorAntiguoIdGlosa = ValorAntiguo.IdGlosa;
			    ValorAntiguoDescripcionGlosa = ValorAntiguo.DescripcionGlosa;
		    };
					
			{
				//CABECERA
				AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
				
				AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();
				
				log.IdCabeceraLog = IdCabeceraLog;
				log.IdTipoLog = "MODIFICACION";
				log.IdModulo = in.p_IdModulo;
				log.IdUsuario = in.p_IdUsuario;
				log.IdEmpresa = in.p_IdEmpresa;
				log.FechaSistemaLog = FechaSistema;
				log.IdUsuarioAtendido = 0L;
				log.IdCliente = IdCliente;
				log.EstadoCliente = "";
				log.NombreCliente = "";
				log.IdTipoDocumentoIdentidad = "";
				log.IdDocumentoIdentidad = "";
				log.IdPortafolio = 0L;
				log.EstadoPortafolio = "";
				log.IdTipoRegistro = "CONTRATO-SAAPV";
				log.EstadoRegistro = "";
				log.IdTipoOperacion = "";
				log.Folio = 0L;
				log.Cantidad = BigDecimal.ZERO;
				log.PrecioTasa = BigDecimal.ZERO;
				log.Monto = BigDecimal.ZERO;
				log.IdMonedaOrigen = "";
				log.IdMonedaLiquidacion = "";
				log.TipoCambio = "";
				log.Nemo = "";
				log.IdProceso = 0L;
				log.EstadoProceso = "ING";
				log.NombreProceso = "Cambiar Estado Contrato SAAPV";
				
				if (IdCliente == 0) {
					log.IdPersona = IdPersona;
				}
				
				entradaAgregarLog.p_Log.add(log);
				
				//DETALLE
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "FolioContratoSAAPV", "", FolioContratoSAAPV));
				
				if(!ValorAntiguoIdEstadoEnvioNotificacion.isEmpty() || !IdEstadoEnvioNotificacion.isEmpty()) {
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "IdEstadoEnvioNotificacion", ValorAntiguoIdEstadoEnvioNotificacion, IdEstadoEnvioNotificacion));
				}
				
				if(!ValorAntiguoIdEstadoSolicitud.isEmpty() || !IdEstadoSolicitud.isEmpty()) {
					entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "IdEstadoSolicitud", ValorAntiguoIdEstadoSolicitud, IdEstadoSolicitud));
				}
				
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "IdEstado", ValorAntiguoIdEstado, IdEstado));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "IdGlosa", Long.toString(ValorAntiguoIdGlosa),Long.toString(IdGlosa)));
				entradaAgregarLog.p_LogDetalle.add(agregarDetalleLogContratoSAAPV(IdCabeceraLog, "DescripcionGlosa", ValorAntiguoDescripcionGlosa, DescripcionGlosa));
				
				//REGISTRO
				AgregarLog agregarLogService = new AgregarLog();
				agregarLogService.logica(con, entradaAgregarLog);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public  InAgregarLog.DetalleLog agregarDetalleLogContratoSAAPV(Long IdCabeceraLog, String CampoRegistro, String ValorAntiguo, String ValorNuevo) {
		InAgregarLog.DetalleLog DetalleContratoSAAPV = new InAgregarLog.DetalleLog();
		DetalleContratoSAAPV.IdCabeceraLog = IdCabeceraLog; 
		DetalleContratoSAAPV.IdTrabajo = 0L;
		DetalleContratoSAAPV.SubRegistro = "CONTRATO-SAAPV"; 
		DetalleContratoSAAPV.CampoRegistro = CampoRegistro; 
		DetalleContratoSAAPV.ValorAntiguo = ValorAntiguo; 
		DetalleContratoSAAPV.ValorNuevo = ValorNuevo;
		return DetalleContratoSAAPV;
	}
	
	public ConsultarEstadoGlosa ConsultarEstadoGlosa(Connection con, SQL sql, String IdFolioContratoSAAPV) {
		ConsultarEstadoGlosa ValoresAntiguos = new ConsultarEstadoGlosa();
		try {
			HashMap<String, Object> ParamConsultarEstadoGlosa = new HashMap<>();
			ParamConsultarEstadoGlosa.put("IdFolioContratoSAAPV", IdFolioContratoSAAPV);
			List<ConsultarEstadoGlosa> resultadoConsulta = sql.query(con, SQL_SELECT_CONSULTAR_ESTADO_GLOSA, ParamConsultarEstadoGlosa, ConsultarEstadoGlosa.class);
			
			if(!resultadoConsulta.isEmpty()) {
				ValoresAntiguos.IdEstadoEnvioNotificacion = resultadoConsulta.get(0).IdEstadoEnvioNotificacion;
				ValoresAntiguos.IdEstadoSolicitud = resultadoConsulta.get(0).IdEstadoSolicitud;
				ValoresAntiguos.IdEstado = resultadoConsulta.get(0).IdEstado;
				ValoresAntiguos.IdGlosa = resultadoConsulta.get(0).IdGlosa;
				ValoresAntiguos.DescripcionGlosa = resultadoConsulta.get(0).DescripcionGlosa;
				return ValoresAntiguos;
			}
			
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ConsultarEstadoGlosa ConsultarEstadoTraspaso(Connection con, SQL sql, String IdFolioContratoSAAPV) {
		ConsultarEstadoGlosa ValoresAntiguos = new ConsultarEstadoGlosa();
		try {
			HashMap<String, Object> ParamConsultarEstadoGlosa = new HashMap<>();
			ParamConsultarEstadoGlosa.put("FolioContratoSAAPV", IdFolioContratoSAAPV);	
			
			List<ConsultaContratoEstado> resultadoExisteTraspaso = sql.query(con, SQL_SELECT_INGRESO_EGRESO_APV, ParamConsultarEstadoGlosa, ConsultaContratoEstado.class);
			
			
			if(!resultadoExisteTraspaso.isEmpty()) {
				ValoresAntiguos.IdEstadoEnvioNotificacion = "";
				ValoresAntiguos.IdEstadoSolicitud = resultadoExisteTraspaso.get(0).IdEstado;
				ValoresAntiguos.IdEstado = resultadoExisteTraspaso.get(0).IdEstado;
				ValoresAntiguos.IdGlosa = resultadoExisteTraspaso.get(0).IdMandatoPAC;
				ValoresAntiguos.DescripcionGlosa = "Modificacion traspaso asociado a contrato SAAPV " + IdFolioContratoSAAPV;
				return ValoresAntiguos;
			}
			
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		ContratosSAAPV_Entrada jin = gson.fromJson(in.toString() ,ContratosSAAPV_Entrada.class);
		ContratosSAAPV_Salida out = logica(conn,jin);		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			try {
				//Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
							
				Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();				//
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":0,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"CER\",\"p_ContratosSAAPV\":[{\"IdPersona\":2607,\"IdCliente\":2084,\"IdFolioContratoSAAPV\":\"1M4023\",\"IdEmpleador\":0,\"FechaRevocacion\":\"2023-04-14\",\"IdEstadoEnvioNotificacion\":\"\",\"IdEstadoSolicitud\":\"PDS\",\"IdEstado\":\"CER\",\"IdGlosa\":5,\"DescripcionGlosa\":\"F\"}]}";
				//anular
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"ANU\",\"p_ContratosSAAPV\":[{\"IdPersona\":44799,\"IdCliente\":61625,\"IdEmpleador\":0,\"IdFolioContratoSAAPV\":\"2026011301\",\"FechaRevocacion\":\"2025-12-16\",\"IdEstadoEnvioNotificacion\":\"\",\"IdEstadoSolicitud\":\"ANU\",\"IdEstado\":\"ANU\",\"IdGlosa\":7,\"DescripcionGlosa\":\"TEST\"}]}";
				//anular 2 con pac
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"ANU\",\"p_ContratosSAAPV\":[{\"IdPersona\":115355,\"IdCliente\":83363,\"IdEmpleador\":0,\"IdFolioContratoSAAPV\":\"2026011201\",\"FechaRevocacion\":\"2025-12-16\",\"IdEstadoEnvioNotificacion\":\"\",\"IdEstadoSolicitud\":\"ANU\",\"IdEstado\":\"ANU\",\"IdGlosa\":8,\"DescripcionGlosa\":\"TEST\"},{\"IdPersona\":44799,\"IdCliente\":61625,\"IdEmpleador\":0,\"IdFolioContratoSAAPV\":\"2026011301\",\"FechaRevocacion\":\"2025-12-16\",\"IdEstadoEnvioNotificacion\":\"\",\"IdEstadoSolicitud\":\"ANU\",\"IdEstado\":\"ANU\",\"IdGlosa\":7,\"DescripcionGlosa\":\"TEST\"}]}";
				//anular 2 - 1 sin pac
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"ANU\",\"p_ContratosSAAPV\":[{\"IdPersona\":115355,\"IdCliente\":83363,\"IdEmpleador\":0,\"IdFolioContratoSAAPV\":\"1000973871\",\"FechaRevocacion\":\"2025-12-16\",\"IdEstadoEnvioNotificacion\":\"\",\"IdEstadoSolicitud\":\"ANU\",\"IdEstado\":\"ANU\",\"IdGlosa\":8,\"DescripcionGlosa\":\"TEST\"},{\"IdPersona\":44799,\"IdCliente\":61625,\"IdEmpleador\":0,\"IdFolioContratoSAAPV\":\"2026011301\",\"FechaRevocacion\":\"2025-12-16\",\"IdEstadoEnvioNotificacion\":\"\",\"IdEstadoSolicitud\":\"ANU\",\"IdEstado\":\"ANU\",\"IdGlosa\":7,\"DescripcionGlosa\":\"TEST\"}]}";
				//revocar
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"REV\",\"p_ContratosSAAPV\":[{\"IdPersona\":44799,\"IdCliente\":61625,\"IdEmpleador\":14577,\"IdFolioContratoSAAPV\":\"1M235411\",\"FechaRevocacion\":\"2026-01-09\",\"IdEstadoEnvioNotificacion\":\"\",\"IdEstadoSolicitud\":\"PDS\",\"IdEstado\":\"REV\",\"IdGlosa\":3,\"DescripcionGlosa\":\"TEST\"}]}";
				//revocar 2 contratos - 1 sin pac -> 1M4058
				//String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"REV\",\"p_ContratosSAAPV\":[{\"IdPersona\":21784,\"IdCliente\":52606,\"IdEmpleador\":66272,\"IdFolioContratoSAAPV\":\"1M4058\",\"FechaRevocacion\":\"2025-12-16\",\"IdEstadoEnvioNotificacion\":\"\",\"IdEstadoSolicitud\":\"PDS\",\"IdEstado\":\"REV\",\"IdGlosa\":2,\"DescripcionGlosa\":\"TEST\"},{\"IdPersona\":44799,\"IdCliente\":61625,\"IdEmpleador\":14577,\"IdFolioContratoSAAPV\":\"1M235411\",\"FechaRevocacion\":\"2026-01-09\",\"IdEstadoEnvioNotificacion\":\"\",\"IdEstadoSolicitud\":\"PDS\",\"IdEstado\":\"REV\",\"IdGlosa\":3,\"DescripcionGlosa\":\"TEST\"}]}";
				//revocar 2 contrato - 2 con pav
				//String json ="{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"REV\",\"p_ContratosSAAPV\":[{\"IdPersona\":129912,\"IdCliente\":91921,\"IdEmpleador\":0,\"IdFolioContratoSAAPV\":\"0000433135\",\"FechaRevocacion\":\"2025-12-16\",\"IdEstadoEnvioNotificacion\":\"\",\"IdEstadoSolicitud\":\"PDS\",\"IdEstado\":\"REV\",\"IdGlosa\":2,\"DescripcionGlosa\":\"TEST\"},{\"IdPersona\":44799,\"IdCliente\":61625,\"IdEmpleador\":14577,\"IdFolioContratoSAAPV\":\"1M235411\",\"FechaRevocacion\":\"2026-01-09\",\"IdEstadoEnvioNotificacion\":\"\",\"IdEstadoSolicitud\":\"PDS\",\"IdEstado\":\"REV\",\"IdGlosa\":3,\"DescripcionGlosa\":\"TEST\"}]}";
				//anular
				//String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"ANU\",\"p_ContratosSAAPV\":[{\"IdPersona\":44799,\"IdCliente\":61625,\"IdEmpleador\":0,\"IdFolioContratoSAAPV\":\"2026011604\",\"FechaRevocacion\":\"2025-12-26\",\"IdEstadoEnvioNotificacion\":\"\",\"IdEstadoSolicitud\":\"ANU\",\"IdEstado\":\"ANU\",\"IdGlosa\":8,\"DescripcionGlosa\":\"PRUEBA\"}]}";
				//revocar
				String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":1,\"p_IdModulo\":\"APV\",\"p_Opcion\":\"REV\",\"p_ContratosSAAPV\":[{\"IdPersona\":35734,\"IdCliente\":39435,\"IdEmpleador\":0,\"IdFolioContratoSAAPV\":\"0000430941\",\"FechaRevocacion\":\"2025-12-26\",\"IdEstadoEnvioNotificacion\":\"\",\"IdEstadoSolicitud\":\"PDS\",\"IdEstado\":\"REV\",\"IdGlosa\":3,\"DescripcionGlosa\":\"TEST\"}]}";
				
				ContratosSAAPV_Entrada Objin2 = gson.fromJson(json, ContratosSAAPV_Entrada.class);
				
				ContratosSAAPV_Entrada Objin = new ContratosSAAPV_Entrada();
				ContratosSAAPV revocar = new ContratosSAAPV();
				List<ContratosSAAPV> revo = new ArrayList<ContratosSAAPV>();
				revocar.IdPersona = 894046L;
				revocar.IdCliente = 1423149L;
				revocar.IdEmpleador = 1773623L;
				revocar.IdFolioContratoSAAPV = "123457";
				revocar.FechaRevocacion = "2017-10-10"; 
				revocar.IdEstado = "CER";
				revocar.IdGlosa = 1L;
				revocar.DescripcionGlosa = "afdsfasdf";
				revo.add(revocar);
				
				Objin.p_IdEmpresa = 1L;
				Objin.p_IdUsuario = 17L;
				Objin.p_IdModulo = "APV";
				Objin.p_Opcion = "CER";
				Objin.p_ContratosSAAPV = revo;
				
				CambiarEstadoContratosSAAPV demo = new CambiarEstadoContratosSAAPV();	
				
	 			System.out.println("ENTRADA");
	 			System.out.println(Objin.toString());
	 			System.out.println("SALIDA");
	 			
	 			try {
	 				ContratosSAAPV_Salida ObjOut = demo.logica(con, Objin2);
	 				//con.commit();
	 				con.rollback();
	 				System.out.println(ObjOut.toString());
	 			}
	 			catch(Exception er) {
					er.printStackTrace();	
					con.rollback();
				}
	 			
		} catch (Exception e) {
			e.printStackTrace();			
		}	
		System.out.println("== END ==");
		};
	}
}