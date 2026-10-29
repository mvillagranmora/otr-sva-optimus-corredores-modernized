package cl.bcs.mesa.MRF;


import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.utils.ValorizarInforme.ObjEntradaInstrumentos;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.custodia.utils.ValorizarInforme.ObjProcesamiento;
import cl.bcs.custodia.utils.ValorizarInforme.ObjResultadoValorizacion;
import cl.bcs.custodia.utils.ValorizarInforme;
import cl.bcs.custodia.utils.ValorizarInforme.ValorizarInformeEntrada;
import cl.bcs.custodia.utils.ValorizarInforme.ValorizarInformeSalida;
import cl.bcs.plataforma.pla.utils.Diccionario;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;
import cl.bcs.utils.BcsPojo;

import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog;
import cl.bcs.plataforma.pla.AgregarLog.InAgregarLog.Log;
import cl.bcs.plataforma.pla.AgregarLog.OutAgregarLog;


public class ConsultarValorizacionPatrimonioMRF implements JavaCall{
	private final String SQL_SELECT_CLIENTE_MESA = loadResourceToString("cl/bcs/mesa/sql/ConsultarValorizacionPatrimonioMRF_BuscarClienteMesa.sql");
	
	private final String SQL_SELECT_CARTERA_LIBRE = loadResourceToString("cl/bcs/mesa/sql/ConsultarValorizacionPatrimonioMRF_SelectCarteraLibre.sql");
	private final String SQL_SELECT_CARTERA_GARANTIA = loadResourceToString("cl/bcs/mesa/sql/ConsultarValorizacionPatrimonioMRF_SelectCarteraGarantia.sql");
	private final String SQL_SELECT_COMPRA_PACTOS = loadResourceToString("cl/bcs/mesa/sql/ConsultarValorizacionPatrimonioMRF_SelectCarteraCompraPactos.sql");
	private final String SQL_SELECT_VENTA_PACTOS = loadResourceToString("cl/bcs/mesa/sql/ConsultarValorizacionPatrimonioMRF_SelectCarteraVentaPactos.sql");
	private final String SQL_SELECT_CRV_PACTOS = loadResourceToString("cl/bcs/mesa/sql/ConsultarValorizacionPatrimonioMRF_SelectCarteraCRVPactos.sql");
	
	private final String SQL_SELECT_CARTERA_LIBRE_HISTORICO = loadResourceToString("cl/bcs/mesa/sql/ConsultarValorizacionPatrimonioMRF_SelectCarteraLibreHistorico.sql");
	private final String SQL_SELECT_CARTERA_GARANTIA_HISTORICO = loadResourceToString("cl/bcs/mesa/sql/ConsultarValorizacionPatrimonioMRF_SelectCarteraGarantiaHistorico.sql");
	private final String SQL_SELECT_COMPRA_PACTOS_HISTORICO = loadResourceToString("cl/bcs/mesa/sql/ConsultarValorizacionPatrimonioMRF_SelectCarteraCompraPactosHistorico.sql");
	private final String SQL_SELECT_VENTA_PACTOS_HISTORICO = loadResourceToString("cl/bcs/mesa/sql/ConsultarValorizacionPatrimonioMRF_SelectCarteraVentaPactosHistorico.sql");
	private final String SQL_SELECT_CRV_PACTOS_HISTORICO = loadResourceToString("cl/bcs/mesa/sql/ConsultarValorizacionPatrimonioMRF_SelectCarteraCRVPactosHistorico.sql");
	
	private final String SQL_SELECT_DATA_PACTOS_PATRIMONIO = loadResourceToString("cl/bcs/mesa/sql/ConsultarValorizacionPatrimonioMRF_SelectPactosCalzados.sql");

	private final String SQL_SELECT_FECHA_ULTIMO_RESPALDO = loadResourceToString("cl/bcs/mesa/sql/CrearValorizacionPatrimonioMRF_SelectFechaUltimoRespaldo.sql");
	
	private final int OPCION_CARTERAS = 1;
	
	private static final Logger logger = LogManager.getLogger(ConsultarValorizacionPatrimonioMRF.class);
	
	public static class ValorizarPatrimonio_IN extends BcsPojo {
		public Long 		p_IdEmpresa;
		public String 		p_FechaOperacion;
		public int	 		p_plazoDias;
		public Long	 		p_IdPortafolio;
		public String 		p_ValorizaAl;
		public int	 		p_opcion;
		public String 		p_NombreMesa;
		public int	 		p_TipoValor;
		public String 		p_FechaHoraSistema;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public Long			p_IdCliente;
		public String  		p_Informe;
		public Long  		p_IdPais;
	}
	
	public static class PatrimonioDetalle {  
		public Long				IdValorizacion;
	    public Long 			FolioCartera;    
	    public Long				FolioOperacion;
	    public Long 			IdCartera;  
	    public BigDecimal 		TIRMercado;
	    public String 			TipoCartera;
	    public String 			IdTipoInstrumento;
	    public String 			IdClasificacionTipoInstrumento;
	    public Long 			IdInstrumento;
	    public String 			FechaVencimientoInstrumento;
	    public String 			FechaEmisionInstrumento;
	    public String 			IdMonedaInstrumento;         
	    public BigDecimal 		ValorRescate;  
	    public BigDecimal 		Cantidad; 
	    public BigDecimal 		PrecioCompra;    
	    public BigDecimal 		TasaCompra; 
	    public BigDecimal 		MontoCompra;             
	    public Long 			IdPortafolio;
	    public String 			Nemotecnico;         
	    public String 			NemoSVS;              
	    public String 			IdEmisor;             
	    public String 			IdCondicionLiquidacion;  
	    public String 			TablaOrigen;
	    public String 			IdMonedaEmisionInstrumento;
	    public String 			IdTipoReajusteInstrumento;
	    public String 			Diferenciador;
	    public Long				FolioComprobante;
	    public String			FechaVencimientoOperacion;
	    public int				Plazo;
	    public int				PlazoPacto; 
	    public String 			TipoValorizacion;
	    public String			FechaOperacion;
	    public String 			CodigoIFRS;
	    public String			EsFungible;
	    public Long				PlazoEnDiasOperacion;
	    public BigDecimal		Duration;
	    public BigDecimal		MontoValorActual;
	    public BigDecimal		PrecioProximo;
	    public BigDecimal		ValorMercado;
	    public BigDecimal		TIR;
	    public Long				IdCliente;
	    public String			IdTipoOperacion;
	    public Long				IdOperacion;
	    public String 			FechaLiquidacion;
	    public BigDecimal		MontoCompraMonedaInstrumento;
	    public BigDecimal		MontoCompraMonedaAdicional;
	    public String			IdConcepto;
	    public Long				IdAgente;
	    public String 			TipoCarteraCorredor;
	    public String           IdUso;
	    public String          FechaUltimoRespaldo;
	    
	    public String toString() {
	        return Utils.printJson(this, false);
	    }
	}
	
	public static class ClienteMesa {  
		public Long				IdCliente;
	    public String toString() {
	        return Utils.printJson(this, false);
	    }
	}
	
	
	// --------------------------------------------------------------
	// CLASES DE out
	// --------------------------------------------------------------
	
	public static class ValorizarPatrimonio_OUT{
		public List<ObjResultadoValorizacion>	P_OUT_INSTRUMENTOSVALORIZADOS;
		public List<MensajeSalida>				P_OUT_MENSAJES;		
		public List<ObjProcesamiento>           P_OUT_ERRORVALORIZACIONES;
		public Long								P_OUT_ERRORESCONTADOR;
		public String							LlaveReporte;
		
		public ValorizarPatrimonio_OUT(){
			P_OUT_MENSAJES 					= new ArrayList<MensajeSalida>();
			P_OUT_INSTRUMENTOSVALORIZADOS	= new ArrayList<ObjResultadoValorizacion>();
			P_OUT_ERRORVALORIZACIONES 		= new ArrayList<ObjProcesamiento>();
			P_OUT_ERRORESCONTADOR			= 0L;
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	};
	
	@Trace
	public ValorizarPatrimonio_OUT logica(Connection conn, ValorizarPatrimonio_IN in) throws Exception{
		logger.info("Inicio Valorizar Patrimonio");
		ValorizarPatrimonio_OUT out = new ValorizarPatrimonio_OUT();
		out.P_OUT_ERRORESCONTADOR = 0L;	
		HashMap<String,Object> params = new HashMap<>();
		HashMap<String,Object> paramsCliente = new HashMap<>();
		params.clear();
		paramsCliente.clear();

		try {
			SQL sql = new SQL();
			List<PatrimonioDetalle> patrimonioDetalle = new ArrayList<>();
			String tipoValorizacion = "";
			SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
			Date fechaSistema =  formatoFecha.parse(in.p_FechaHoraSistema);
			Date fechaOperacion =  formatoFecha.parse(in.p_FechaOperacion);
			String fechaSistemaLog = ConfiguracionSistema.getParametroConfiguracionString(conn, in.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");	
			Long IdCliente = in.p_IdCliente;
			
			if (in.p_IdCliente.equals(0L)) {
				logger.debug("INICIO::SQL_SELECT_CLIENTE_MESA");
				paramsCliente.put(tipoValorizacion, tipoValorizacion);
				Optional<ClienteMesa> cliente = sql.query(conn, SQL_SELECT_CLIENTE_MESA, new HashMap<>(), ClienteMesa.class).stream().findFirst();
				if (cliente.isPresent()) {
					params.put("p_IdCliente", cliente.get().IdCliente);
					IdCliente = cliente.get().IdCliente;
				}
				logger.debug("fin::SQL_SELECT_CLIENTE_MESA");
			}
			
			params.put("p_plazoDias", in.p_plazoDias);
			params.put("p_IdCliente", IdCliente);
			params.put("p_IdPortafolio", in.p_IdPortafolio);
			params.put("p_FechaOperacion", in.p_FechaOperacion);
			
			logger.debug("INICIO::SQL_SELECT_DATA");
			switch (in.p_opcion) {
			case OPCION_CARTERAS:
				if(fechaSistema.after(fechaOperacion)) {
					logger.debug("INICIO::SQL_SELECT_HISTORICO_CARTERA");	

					HashMap<String, Object> paramRespaldo = new HashMap<>(1);
					paramRespaldo.put("p_Fecha", fechaOperacion);
					String fechaRespaldo = sql.query(conn, SQL_SELECT_FECHA_ULTIMO_RESPALDO, paramRespaldo, PatrimonioDetalle.class).stream().findFirst().get().FechaUltimoRespaldo;
					params.put("p_FechaOperacion", fechaRespaldo);				

					patrimonioDetalle.addAll(sql.query(conn, SQL_SELECT_COMPRA_PACTOS_HISTORICO, params, PatrimonioDetalle.class));
					
					params.put("p_plazoDias", in.p_plazoDias);
					
					patrimonioDetalle.addAll(sql.query(conn, SQL_SELECT_VENTA_PACTOS_HISTORICO, params, PatrimonioDetalle.class));
					patrimonioDetalle.addAll(sql.query(conn, SQL_SELECT_CRV_PACTOS_HISTORICO, params, PatrimonioDetalle.class));						

					params.put("p_IdEmpresa", in.p_IdEmpresa);
					
					patrimonioDetalle.addAll(sql.query(conn, SQL_SELECT_CARTERA_LIBRE_HISTORICO, params, PatrimonioDetalle.class));
					patrimonioDetalle.addAll(sql.query(conn, SQL_SELECT_CARTERA_GARANTIA_HISTORICO, params, PatrimonioDetalle.class));					

					logger.debug("FIN::SQL_SELECT_HISTORICO_CARTERA");

				}else {
					logger.debug("INICIO::SQL_SELECT_DATA_CARTERA_PATRIMONIO");
					
					patrimonioDetalle.addAll(sql.query(conn, SQL_SELECT_COMPRA_PACTOS, params, PatrimonioDetalle.class));
					patrimonioDetalle.addAll(sql.query(conn, SQL_SELECT_VENTA_PACTOS, params, PatrimonioDetalle.class));
					patrimonioDetalle.addAll(sql.query(conn, SQL_SELECT_CRV_PACTOS, params, PatrimonioDetalle.class));	
					
					params.put("p_IdEmpresa", in.p_IdEmpresa);  
					
					patrimonioDetalle.addAll(sql.query(conn, SQL_SELECT_CARTERA_LIBRE, params, PatrimonioDetalle.class));
					patrimonioDetalle.addAll(sql.query(conn, SQL_SELECT_CARTERA_GARANTIA, params, PatrimonioDetalle.class));
					
					logger.debug("FIN::SQL_SELECT_DATA_CARTERA_PATRIMONIO");
				}
				break;
			default:
				logger.debug("INICIO::SQL_SELECT_DATA_PACTOS_PATRIMONIO");
				params.put("p_IdCliente", in.p_IdCliente);
				params.put("p_IdPortafolio", in.p_IdPortafolio);
				params.put("p_FechaOperacion", in.p_FechaOperacion);
				params.put("p_TipoValor", in.p_TipoValor);
				params.put("p_plazoDias", in.p_plazoDias);
				params.put("p_IdEmpresa", in.p_IdEmpresa);
				params.put("p_FechaHoraSistema", in.p_FechaHoraSistema);
				patrimonioDetalle = sql.query(conn, SQL_SELECT_DATA_PACTOS_PATRIMONIO, params, PatrimonioDetalle.class);
				logger.debug("FIN::SQL_SELECT_DATA_CARTERA_PATRIMONIO");
				break;
			}
			if(patrimonioDetalle.isEmpty()) {
				out.P_OUT_ERRORESCONTADOR++;
				String codigoError = "COBERTURA_PATRIMONIO_SIN_DATOS";
				MensajeSalida errorDataPatrimonio = new MensajeSalida(0L, "ERROR", codigoError, Diccionario.getMensaje(conn, in.p_IdEmpresa, codigoError).msg());
				out.P_OUT_MENSAJES.add(errorDataPatrimonio);
				logger.error(codigoError);
				return out;
			}else {
				logger.debug("INICIO::VALORIZAR INFORME");
			    ValorizarInforme  valorizarInforme = new ValorizarInforme();
				ValorizarInformeEntrada inf 	= 	new ValorizarInformeEntrada();
				inf.p_FechaFinal 				= 	in.p_ValorizaAl;
				inf.p_FechaInicial 				= 	in.p_ValorizaAl; //TODO Fecha valorizar inicial igual a fecha final para evitar error en valorizar informe
				inf.p_IdEmpresa 				= 	in.p_IdEmpresa;
				inf.p_IdPortafolio 				= 	in.p_IdPortafolio;
				inf.p_IdUsuario 				= 	in.p_IdUsuario;
				inf.p_IdPais                    =   in.p_IdPais;
				inf.p_IdModulo 					=	(in.p_IdModulo != null)? in.p_IdModulo : "MERF";
				inf.p_IdFechaHoraSistema		= 	FechaDB.ObtenerFechaHoraSistemaLocal(conn, in.p_IdEmpresa);
				inf.p_OrigenProcesamiento		= 	"COBERTURA.PATRIMONIAL";
				inf.p_IdTipoTasaValorizacion 	=	(in.p_TipoValor == 1)? "COMP": "MERC"; 
				inf.p_IdClienteSolicitante      =   IdCliente;
				List<ObjEntradaInstrumentos> instrumentos = new ArrayList<>();
			    for (PatrimonioDetalle item : patrimonioDetalle) {
			    	ObjEntradaInstrumentos instrumento = new ObjEntradaInstrumentos();
			        instrumento.IdValorizacion = item.IdValorizacion;
			        instrumento.IdUso = item.IdUso;
			        instrumento.FolioCartera = item.FolioCartera;
			        instrumento.FolioOperacion = item.FolioOperacion;
			        instrumento.IdCartera = item.IdCartera;
			        instrumento.TipoCartera = item.TipoCartera;
			        instrumento.IdTipoInstrumento = item.IdTipoInstrumento;
			        instrumento.IdClasificacionTipoInstrumento = item.IdClasificacionTipoInstrumento;
			        instrumento.IdInstrumento = item.IdInstrumento;
			        instrumento.FechaVencimientoInstrumento = item.FechaVencimientoInstrumento.toString();
			        instrumento.FechaEmisionInstrumento = item.FechaEmisionInstrumento.toString();
			        instrumento.IdMonedaInstrumento = item.IdMonedaInstrumento;
			        instrumento.ValorRescate = item.ValorRescate;
			        instrumento.Cantidad = item.Cantidad;
			        instrumento.PrecioCompra = item.PrecioCompra;
			        instrumento.TasaCompra = item.TasaCompra;
			        instrumento.TasaCompraValorizacionFechaOperacion = item.TasaCompra;
			        instrumento.MontoCompra = item.MontoCompra;
			        instrumento.IdPortafolio = item.IdPortafolio;
			        instrumento.Nemotecnico = item.Nemotecnico;
			        instrumento.NemoSVS = item.NemoSVS;
			        instrumento.IdCondicionLiquidacion = item.IdCondicionLiquidacion;
			        instrumento.TablaOrigen = item.TablaOrigen;
			        instrumento.IdMonedaEmisionInstrumento = item.IdMonedaEmisionInstrumento;
			        instrumento.IdTipoReajusteInstrumento = item.IdTipoReajusteInstrumento;
			        instrumento.Diferenciador = item.Diferenciador;
			        instrumento.FechaOperacion = item.FechaOperacion;
			        instrumento.Emisor = item.IdEmisor;
			        instrumento.CodigoIFRS = item.CodigoIFRS;
			        instrumento.FechaVencimientoInstrumento = item.FechaVencimientoInstrumento;
			        instrumento.FechaVencimientoOperacion = item.FechaVencimientoOperacion;
			        instrumento.PlazoEnDiasOperacion = item.PlazoEnDiasOperacion;
			        instrumento.TablaAfecta = item.TablaOrigen;
			        instrumento.IdCliente = item.IdCliente;
			        instrumento.IdTipoOperacion = item.IdTipoOperacion;
			        instrumento.IdOperacion = item.IdOperacion;
			        instrumento.FechaLiquidacion = item.FechaLiquidacion;
			        instrumento.MontoCompraMonedaInstrumento = item.MontoCompraMonedaInstrumento;
			        instrumento.MontoCompraMonedaAdicional = item.MontoCompraMonedaAdicional;
			        instrumento.IdConcepto = item.IdConcepto;
			        instrumento.IdAgente = item.IdAgente;
			        instrumento.FolioComprobante = item.FolioComprobante; 
			        instrumento.TipoCarteraCorredor = item.TipoCarteraCorredor;
			        instrumento.OrigenProcesamiento = "COBERTURA.PATRIMONIAL";
			        
			        instrumentos.add(instrumento);
			    }
			    inf.p_Instrumentos = instrumentos;
				ValorizarInformeSalida valorizarInformeSalida = valorizarInforme.logica(conn, inf);
				out.LlaveReporte = valorizarInformeSalida.LlaveReporte;
				out.P_OUT_ERRORESCONTADOR = valorizarInformeSalida.P_OUT_ERRORESCONTADOR;
				out.P_OUT_ERRORVALORIZACIONES = valorizarInformeSalida.P_OUT_ERRORVALORIZACIONES;
				out.P_OUT_INSTRUMENTOSVALORIZADOS = valorizarInformeSalida.P_OUT_INSTRUMENTOSVALORIZADOS;
				out.P_OUT_MENSAJES = valorizarInformeSalida.P_OUT_MENSAJES;

				if (out.P_OUT_ERRORESCONTADOR > 0L) {
					Long idValorizacion = valorizarInformeSalida.P_OUT_INSTRUMENTOSVALORIZADOS.stream()
					.max(Comparator.comparingLong(inst -> inst.IdValorizacion))
					.orElseThrow(() -> new IllegalStateException("No se encontro IdValorizacion"))
					.IdValorizacion;
					registrarLogGenerico(conn, in, valorizarInformeSalida, "ERROR", out, fechaSistemaLog, idValorizacion, IdCliente);
					return out;
				}

				if (valorizarInformeSalida.P_OUT_INSTRUMENTOSVALORIZADOS.size() > 0) {
					Long idValorizacion = valorizarInformeSalida.P_OUT_INSTRUMENTOSVALORIZADOS.stream()
							.max(Comparator.comparingLong(inst -> inst.IdValorizacion))
							.orElseThrow(() -> new IllegalStateException("No se encontro IdValorizacion"))
							.IdValorizacion;
					
					out.P_OUT_INSTRUMENTOSVALORIZADOS = valorizarInformeSalida.P_OUT_INSTRUMENTOSVALORIZADOS;
					registrarLogGenerico(conn, in, valorizarInformeSalida, "INFORMATIVO", out, fechaSistemaLog, idValorizacion, IdCliente);
					out.LlaveReporte = valorizarInformeSalida.LlaveReporte;
				}
				logger.debug("FIN::VALORIZAR INFORME");
				
			}
			logger.debug("FIN::VALORIZAR COBERTURA PATRIMONIAL");
		}catch (Exception e){
			out.P_OUT_ERRORESCONTADOR = 1L;
			logger.error(e);
			e.printStackTrace();
			throw e;
		}
		return out;
		
	}

	private void registrarLogGenerico(Connection con, ValorizarPatrimonio_IN entrada, ValorizarInformeSalida valorizarInformeSalida, String tipoLog, ValorizarPatrimonio_OUT out, String fechaSistema, Long idValorizacion, Long IdCliente) throws Exception {
		logger.info("INICIO::registrarLogGenerico ");
	    InAgregarLog agregarLogEntrada = new InAgregarLog();
		AgregarLog agregarLogService = new AgregarLog();
		InformacionEmpresa empresa = new ConsultarEmpresa().logica(con, entrada.p_IdEmpresa);
		
		agregarLogEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		agregarLogEntrada.p_IdUsuario = entrada.p_IdUsuario;
		agregarLogEntrada.p_IdModulo = entrada.p_IdModulo;
		agregarLogEntrada.p_UsaReglasNegocio = "Si";
		agregarLogEntrada.p_IdEstadoAutorizacion = "S";
		agregarLogEntrada.p_IdTrabajo = 0L;
		
		String subregistroLog = "";
		String valorAntiguo = "";
		String valorNuevo = "";			
		
	    Log logValorizacion = new Log();
	    logValorizacion.IdCabeceraLog = idValorizacion;
	    logValorizacion.IdTipoLog = tipoLog;
	    logValorizacion.IdModulo = entrada.p_IdModulo;
	    logValorizacion.IdUsuario = entrada.p_IdUsuario;
	    logValorizacion.IdPortafolio = entrada.p_IdPortafolio;
	    logValorizacion.IdEmpresa = entrada.p_IdEmpresa;
	    logValorizacion.FechaSistemaLog = fechaSistema;
	    logValorizacion.IdCliente = IdCliente;
		logValorizacion.NombreCliente = empresa.NombreCompleto;
	    logValorizacion.IdTipoRegistro = "INFORME_COBERTURA_PATRIMONIAL";
	    logValorizacion.NombreProceso = "Valorizacion Informe Cobertura Patrimonial";

	    agregarLogEntrada.p_Log.add(logValorizacion);
	    
	    agregarLogEntrada.agregarDetalleLogBasico(idValorizacion, 0L, "VALORIZACION", valorAntiguo, valorNuevo, subregistroLog);
	    OutAgregarLog agregarLogSalida = agregarLogService.logica(con, agregarLogEntrada);

	    if (agregarLogSalida.P_OUT_ERRORESCONTADOR > 0) {
	        logger.debug("No se pudo agregar log");
	        String codigoError = "ERROR_AGREGAR_LOG";
	        MensajeSalida errorValorizacion = new MensajeSalida(0L, "ERROR", codigoError,
	                Diccionario.getMensaje(con, entrada.p_IdEmpresa, codigoError).msg());
	        out.P_OUT_MENSAJES.add(errorValorizacion);
	    }

	    if ("INFORMATIVO".equals(tipoLog)) {
	        out.P_OUT_MENSAJES.add(new MensajeSalida(0L, "INFORMATIVO", "", "Proceso Valorizacion Finalizado"));
	        out.LlaveReporte = valorizarInformeSalida.LlaveReporte;
	    } else if ("ERROR".equals(tipoLog)) {
	        out.P_OUT_ERRORESCONTADOR++;
	        out.P_OUT_MENSAJES.add(new MensajeSalida(0L, "ERROR", "", "Error al Ejecutar Valorizacion"));
	    }
		logger.info("FIN::registrarLogGenerico ");
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		ValorizarPatrimonio_IN jin=gson.fromJson(in.toString() ,ValorizarPatrimonio_IN.class);
		ValorizarPatrimonio_OUT out= logica(conn,jin);
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
	
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	};
	
	
	public static void main(String[] args) throws Exception, SQLException {
		Connection conn = null;
		Gson gson = new Gson();

		try {
			System.out.println("== BEGIN ==");
			conn = PlataformaUtils.getPropertiesConnection("master02", "c:/optimus-corredores/connections.properties");
		
			String json1 ="{\"p_IdEmpresa\":1,\"p_FechaOperacion\":\"2025-09-01\",\"p_plazoDias\":3,\"p_opcion\":1,\"p_TipoValor\":2,\"p_IdCliente\":0,\"p_IdPortafolio\":0,\"p_NombreMesa\":\"TODOS\",\"p_ValorizaAl\":\"2025-09-02\",\"p_FechaHoraSistema\":\"2025-09-02\",\"p_IdUsuario\":97,\"p_IdModulo\":\"MRF\",\"p_IdPais\":44}";
			ValorizarPatrimonio_IN objIn = gson.fromJson(json1, ValorizarPatrimonio_IN.class);

			ConsultarValorizacionPatrimonioMRF demo = new ConsultarValorizacionPatrimonioMRF();

			ValorizarPatrimonio_OUT ObjOut = demo.logica(conn, objIn);

			System.out.println(ObjOut.toString());
			//conn.commit();
			conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		System.out.println("== END ==");

	};
	
}