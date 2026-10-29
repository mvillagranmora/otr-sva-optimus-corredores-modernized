package cl.bcs.normativa.circulares.ReportesBCC;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.CallRestService;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/************************************************************
 * @version 	1.0
 * @author 		ajara
 * 
 * Enviar transacción BCCH
 * se utiliza archivo .properties para obtener los datos sensibles
 * se obtiene de BD los datos referentes al reporte y los datos a cargar.
 * se envia a servicio los datos para establecer auth y obtener jwt, ademas de enviar los datos del reporte a insertar
*************************************************************/

public class EnviarTransaccion implements JavaCall{
    static final Logger logger = LogManager.getLogger(EnviarTransaccion.class.getName()); 
    //SQL Para configuraciones generales del reporte
    private final String SQL_OBTENER_CONFIGURACION = loadResourceToString("cl/bcs/normativa/circulares/ReportesBCC/sql/ObtenerConfiguracion.sql");
    
    //SQL Para Reportes 3.1 SICAM
    private final String SQL_OBTENER_REGISTRO_REPO3P1 = loadResourceToString("cl/bcs/normativa/circulares/ReportesBCC/Reportes3p1/sql/BuscarRegistroREPO3P1_Select.sql");
    private final String SQL_UPDATE_ESTADO_SICAM_REPO3P1 = loadResourceToString("cl/bcs/normativa/circulares/ReportesBCC/Reportes3p1/sql/IngresarUpdateREPO3P1.sql");
    
    //SQL Para Reportes 2.1
    private final String SQL_UPDATE_ESTADO_SICAM_REPO2P1 = loadResourceToString("cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/IngresarUpdateREPO2P1.sql");
    private final String SQL_OBTENER_REGISTRO_REPO2P1 = loadResourceToString("cl/bcs/normativa/circulares/ReportesBCC/Reportes2p1/sql/BuscarRegistroREPO2P1_Select.sql");
    
    //SQL Insert en Bitacora Tributario
    private final String SQL_INSERT_BITACORA_TRIBUTARIO_SICAM = loadResourceToString("cl/bcs/normativa/circulares/ReportesBCC/sql/CrearBitacoraReportesBCCH_InsertBitacora.sql");

	//SQL Fecha habil anterior
	private final String SQL_FECHA_HABIL_ANTERIOR = loadResourceToString("cl/bcs/normativa/circulares/ReportesBCC/sql/Consultar_noHabiles.sql");
	
	//SQL para evitar envios simultaneos del mismo reporte
	private final String SQL_INSERT_BLOQUEO_REPORTE_BCCH = loadResourceToString("cl/bcs/normativa/circulares/ReportesBCC/sql/EnviarTransaccion_InsertBloqueoReporteBCCH.sql");
	private final String SQL_DELETE_BLOQUEO_REPORTE_BCCH = loadResourceToString("cl/bcs/normativa/circulares/ReportesBCC/sql/EnviarTransaccion_DeleteBloqueoReporteBCCH.sql");
	
    //Contador registros enviados anterior mente
    public Long RegistrosYaEnviados = 0L;

   //Clase de entrada
    public static class InTransaccion extends BcsPojo {
        public Long 				p_IdEmpresa;
		public Long 				p_IdUsuario;
		public String				p_IdModulo;
        public String               p_IdEsquema;
        public String				p_Version;
        public List<Registros>      p_Reporte;
        
        public static class Registros {
        	public Long				ID;
        	public String 			CodigoError;
        }
    }
    
    //Clase general para parametros necesarios en la api.
    public static class ParamsApi extends BcsPojo {
    	public String ip;
    	public String rutEntidad;
    	public String password;
    	public String code;
    	public String reportCode;
    	public String date;
    	public List<report> report;
    	
    	public static class report {
    	    public String data;

    	    public report(String data) {
    	        this.data = data;
    	    }
    	}
    }
    
    public static class Esquema extends BcsPojo {
    	public Long IdEmpresa;
   
    }
    
    //Clase base para Reportes con campos de control
    public static abstract class RepoBase extends BcsPojo{
       	public Long				ID;
       	public Long				IdDocumento;
       	public String			EstadoSICAM;
       	public String       	errorCode;
       	public String       	message;
       	
    	public String toString() {
	        return Utils.printJson(this, false);
	    }
    }
    
  //Clase generica para reutilizar
    public static class RepoConfig<T extends RepoBase> {
        public final Class<T> clase;
        public final String sqlSelect;
        public final String sqlUpdate;
        public RepoConfig(Class<T> clazz, String sqlSelect, String sqlUpdate) {
            this.clase = clazz;
            this.sqlSelect = sqlSelect;
            this.sqlUpdate = sqlUpdate;
        }
    }
    
   //Sub clase Repo2.1
    public static class Repo2P1 extends RepoBase {
       	public String 			TipoRegistro;
       	public String 			EventoReporte;
       	public String 			TipoOperacion;
       	public String			FechaNegociacion;
       	public String			HoraNegociacion;
       	public String			FechaValutaMonedaRecibida;
       	public String			FechaValutaMonedaEntregada;
       	public String			Identificador;
       	public String			RUTContraparte1;
       	public String			LEIContraparte1;
       	public String			NombreContraparte1;
       	public String			RUTContraparte2;
       	public String			LEIContraparte2;
       	public String			NombreContraparte2;
       	public String			PaisContraparte2;
       	public String			RUTIntermediario;
       	public String			PlataformaNegociacion;
       	public String 			CodigoOperacionCambio;
       	public String 			PaisMonedaExtranjera;
       	public String			MonedaCompradaRecibida;
       	public BigDecimal		MontoMonedaCompradaRecibida;
       	public String			MonedaVendidaEntregada;
       	public BigDecimal		MontoMonedaVendidaEntregada;
       	public String			ConvencionTipoCambio;
       	public BigDecimal		TipoCambio;
       	public String			AfectaDerivados;
       	public String			IdentificadorContratoDerivados;
       	public String			FechaSuscripcionContrato;
       	public String			HoraSuscripcionContrato;
       	public String			OperacionEmpresaRelacionada;
       	public String			NumeroInscripcionObligacionExterior;
       	public String			NombreFinancistaAporante;
       	public String			LEIFinancistaAporante;
       	public String			NombreReceptorInversion;
       	public String			LEIReceptorInversion;
       	public String			ModalidadInversionCustodio;
       	
       	public String toString() {
    	        return Utils.printJson(this, false);
    	    }
       }
    
  //Sub clase Repo3.1
    public static class Repo3P1 extends RepoBase {
    	public String 			TipoRegistro;
    	public String 			RUTReportante;
    	public String 			FechaOperacion;
    	public String			HoraOperacion;
    	public String			TipoOperacion;
    	public String			EventoReporte;
    	public String			Identificador;
    	public String			RUTContraparte;
    	public String			LEIContraparte;
    	public String			NombreContraparte;
    	public String			PlataformaNegociacion;
    	public BigDecimal		MontoMonedaComprada;
    	public BigDecimal		MontoMonedaVendida;
    	public BigDecimal		TipoCambio;
    	
    	public String toString() {
	        return Utils.printJson(this, false);
	    }
    }
    
    public static class Config extends BcsPojo {
    	public String 			CodigoParametro;
    	public String			Valor;
    	
    	public String toString() {
	        return Utils.printJson(this, false);
	    }
    }

  //Salida
    public static class OutTransaccion {
        public Long P_OUT_ERRORESCONTADOR;
        public Long P_OUT_WARNINGCONTADOR;
        public Long P_OUT_NOENVIADOSCONTADOR;
		public Long P_OUT_EXECUTIONTIME;
		public String P_OUT_CODE;
		public String P_OUT_MESSAJE;
		public List<MapeoDetalle> P_OUT_DETALLE;

        public static class RespuestaWS {
            public String                   code;
            public String                   message;
            public String                   accessToken;
            public List<MapeoDetalle>       detail;
        }

        public static class MapeoDetalle {
            public String       errorCode;
            public String       registro;
            public String       message;
        }
    }
    
    
    //Clases utilitarias
	public static class ResumenUpdate extends BcsPojo{
		public Long 	ID;
		public String 	EstadoSICAM;
		public String 	SICAM;
		
		public ResumenUpdate(Long ID, String EstadoSICAM, String SICAM)
		{
			this.ID = ID;
			this.EstadoSICAM = EstadoSICAM;		
			this.SICAM = SICAM;
		}
	}
	
	public static class BitacoraTributario extends BcsPojo{
		public String 		IdDocumento;
		public String 		Esquema;
		public String 		Version;
		public String 		FolioOrigen;
		public String 		TipoOrigen;
		public Date 		FechaOrigen;
		public Long 		IDReferencia;
		public String 		IdEstado;
		public String 		Glosa;
		public String 		Codigo;
		public String 		IdUsuario;
		
		public BitacoraTributario(String IdDocumento, String Esquema, String Version, String FolioOrigen, String TipoOrigen, Date FechaOrigen, Long IDReferencia,
				String IdEstado, String Glosa, String Codigo, String IdUsuario)
		{
			this.IdDocumento = IdDocumento;
			this.Esquema = Esquema;		
			this.Version = Version;
			this.FolioOrigen = FolioOrigen;
			this.TipoOrigen = TipoOrigen;		
			this.FechaOrigen = FechaOrigen;
			this.IDReferencia = IDReferencia;	
			this.IdEstado = IdEstado;
			this.Glosa = Glosa;
			this.Codigo = Codigo;		
			this.IdUsuario = IdUsuario;
		}
	}

	public static class QueryResult extends BcsPojo {
		public String FechaHabilAnterior;
	}
    
	public static class BloqueoReporteBCCH extends BcsPojo {
		public String IdBloqueoReporteBCCH;
	}
	/*******************
	 * método principal
	 *******************/
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {				
		Gson gson = Utils.getGsonBuilder();
		InTransaccion in=gson.fromJson(jin.toString() ,InTransaccion.class);
		OutTransaccion out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}


    /**********************
	 * logica del programa
	 **********************/
	@Trace
    public OutTransaccion logica(Connection con, InTransaccion entrada) throws Exception {
		logger.info("----Inicio Logica Envio Transaccion Sicam----");
        Long startTime = System.currentTimeMillis();
        OutTransaccion out = new OutTransaccion();
        SQL sql = new SQL();
        HashMap<String, HashMap<String, String>> configuraciones = Utils.getConfiguraciones(con, entrada.p_IdEmpresa);
		String server =  configuraciones.get("PLATAFORMA").get("NOR.ServidorSICAM");
		String urlToken = configuraciones.get("PLATAFORMA").get("NOR.UrlObtenerTokenSicam");
		String urlTransaccion = configuraciones.get("PLATAFORMA").get("NOR.UrlEnviarReporteSicam");
		ParamsApi apiParamsApi = new ParamsApi();
		apiParamsApi.rutEntidad = configuraciones.get("PLATAFORMA").get("NOR.RutEntidad");
		apiParamsApi.password = configuraciones.get("PLATAFORMA").get("NOR.PasswordSicam"); 
		apiParamsApi.ip = configuraciones.get("PLATAFORMA").get("NOR.IPSicam");	
		String fechaSistemaStr = configuraciones.get("PLATAFORMA").get("PLA.FechaSistema");
		if (fechaSistemaStr == null || fechaSistemaStr.trim().isEmpty()) {
			logger.info("Error obteniendo fecha sistema desde configuraciones.");
			throw new RuntimeException("PLA.FechaSistema viene null/vacía en configuraciones.");
		}
		List<BloqueoReporteBCCH> idsBloqueoReporteBCCH = new ArrayList<BloqueoReporteBCCH>();
		String borrarTablaBloqueo = "S";
        try{
            Gson gson = new Gson();
            RepoConfig<? extends RepoBase> rconfig = RepoConfig(entrada.p_IdEsquema);
            List<? extends RepoBase> registrosReporte = ObtenerRegistros(con, entrada.p_Reporte, rconfig.clase, rconfig.sqlSelect);
            try {
            	AplicarBloqueoReporteBCCH(con, entrada, idsBloqueoReporteBCCH, registrosReporte, sql);
			} catch (SQLException e) {
				if (e.getErrorCode() == 301) {
					borrarTablaBloqueo = "N";
					throw new RuntimeException("Ya se está ejecutando un envío del reporte seleccionado a SICAM.");
				} else {
					e.printStackTrace();
				}
			}
            logger.info("----Llamada ActualizarEstados (ENV)----");
            ActualizarEstados(con, entrada, "ENV", "ENV_BCCH_SICAM", "Enviado", registrosReporte, rconfig.sqlUpdate);
            trust();
            CallRestService call = new CallRestService(server, urlToken);
            HashMap<String,Object> p_config = new HashMap<>();
            p_config.clear();
            p_config.put("p_IdEsquema", entrada.p_IdEsquema);
            logger.info("----Llamada Obtener Configuracion SICAM----");
            List<Config> configs = sql.query(con, SQL_OBTENER_CONFIGURACION, p_config, Config.class);
            apiParamsApi.code = configs.stream().filter(c -> "BCC_SICAM_CODOPERACIONTOKEN".equals(c.CodigoParametro)).map(c -> c.Valor).findFirst().orElse("RQT01");
            JsonObject jsonBody = gson.toJsonTree(apiParamsApi).getAsJsonObject();
            logger.info("----Llamada Obtener JWT Sicam----");
            logger.info("----Parametros Body:"+ jsonBody);
            logger.info("----Parametros Server:"+ server);
            logger.info("----Parametros Enpoint:"+ urlToken);
            String respuesta  = call.callRestPost(jsonBody.toString());
            if (respuesta == null || respuesta.trim().isEmpty()) {
            	logger.info("Error autenticando con SICAM: No se recibió respuesta del servicio SICAM");
            	ErrorConexion(con, entrada, registrosReporte, rconfig.sqlUpdate, "Error al conectar SICAM");
                throw new RuntimeException("No se recibió respuesta del servicio SICAM.");
            }
            OutTransaccion.RespuestaWS respuestaAuth = gson.fromJson(respuesta, OutTransaccion.RespuestaWS.class);
            if (!"00".equals(respuestaAuth.code)) {
            	logger.info("Error autenticando con SICAM:"+ respuestaAuth.message);
            	String code = "auth_" + respuestaAuth.code;
            	ActualizarEstados(con, entrada, "RZD", code, respuestaAuth.message, registrosReporte, rconfig.sqlUpdate);
                throw new RuntimeException("Error autenticando con SICAM: " + respuestaAuth.message);
            }
            String jwt = respuestaAuth.accessToken;
            apiParamsApi.report = new ArrayList<>();
            logger.info("----Llamada Generar Cuerpo Reporte Sicam----");
            List<ParamsApi.report> registrosParseados = GenerarCuerpoReporte(con, entrada, registrosReporte);
            apiParamsApi.report.addAll(registrosParseados);
            apiParamsApi.code = configs.stream().filter(c -> "BCC_SICAM_CODOPERACIONTRANSACCION".equals(c.CodigoParametro)).map(c -> c.Valor).findFirst().orElse("RQT02");
			if ("REPO3P1".equals(entrada.p_IdEsquema)) {
				apiParamsApi.date = fechaSistemaStr;
			} else if ("REPO2P1".equals(entrada.p_IdEsquema)) {
				String anioStr = fechaSistemaStr.substring(0, 4);
				String anioPrevStr = String.valueOf(Integer.parseInt(anioStr) - 1);
				HashMap<String, Object> HabilParams = new HashMap<>();
				HabilParams.put("fechaSistema", fechaSistemaStr);
				HabilParams.put("anio", anioStr);
    			HabilParams.put("anioPrev", anioPrevStr);
				List<QueryResult> rows = sql.query(
						con,
						SQL_FECHA_HABIL_ANTERIOR,
						HabilParams,
						QueryResult.class);
			
				if (rows == null || rows.isEmpty()) {
					logger.info("Error obteniendo fecha habil anterior.");
					throw new RuntimeException("No se pudo obtener fecha habil anterior.");
				}
				apiParamsApi.date = rows.get(0).FechaHabilAnterior;
			} else {
				logger.info("Esquema no soportado: " + entrada.p_IdEsquema);
				throw new RuntimeException("Esquema no soportado: " + entrada.p_IdEsquema);
			}
            String reporte = "REPO3P1".equals(entrada.p_IdEsquema)? "BCC_REPO3P1_CODIGOREPORTE":"BCC_REPO2P1_CODIGOREPORTE";
            apiParamsApi.reportCode = configs.stream().filter(c -> reporte.equals(c.CodigoParametro)).map(c -> c.Valor).findFirst().orElse("");
            JsonObject registroBody = gson.toJsonTree(apiParamsApi).getAsJsonObject();
            logger.info("----Setear URL Servcio, url: "+ urlTransaccion);
            call.setUrl(urlTransaccion);
            logger.info("----Llamada Enviar Transaccion SICAM----");
            logger.info("----Parametros Body:"+ registroBody);
            logger.info("----Parametros JWT:"+ jwt);
            logger.info("----Parametros Enpoint:"+ urlTransaccion);
            String respuestaTransaccion = call.callRestPostBearerToken(registroBody.toString(), jwt);
			if (respuestaTransaccion == null || respuestaTransaccion.trim().isEmpty()) {
            	logger.info("Error autenticando con SICAM: No se recibió respuesta del servicio SICAM");
            	ErrorConexion(con, entrada, registrosReporte, rconfig.sqlUpdate, "Error al conectar SICAM");
                throw new RuntimeException("No se recibió respuesta del servicio SICAM.");
            }
            OutTransaccion.RespuestaWS respuestaOperacion = gson.fromJson(respuestaTransaccion, OutTransaccion.RespuestaWS.class);
            ProcesarRespuestaSicam(con, entrada, rconfig.sqlUpdate, registrosReporte, respuestaOperacion);
            out.P_OUT_CODE = respuestaOperacion.code;
            out.P_OUT_DETALLE = respuestaOperacion.detail;
            out.P_OUT_MESSAJE = respuestaOperacion.message;
            out.P_OUT_WARNINGCONTADOR = (respuestaOperacion.detail != null && !respuestaOperacion.detail.isEmpty())? (long)(respuestaOperacion.detail.size()): 0;
            out.P_OUT_ERRORESCONTADOR = "00".equals(respuestaOperacion.code)? 0L : "07".equals(respuestaOperacion.code)? 
            		(respuestaOperacion.detail != null ? (long) respuestaOperacion.detail.size() : 0L) : (long) entrada.p_Reporte.size();
            out.P_OUT_NOENVIADOSCONTADOR = RegistrosYaEnviados != 0L ? RegistrosYaEnviados : 0;
            out.P_OUT_EXECUTIONTIME = startTime;
            logger.info("----Fin Logica Envio Transaccion Sicam----");
        }catch (Exception e){
            logger.info("Error Logica Envio Transaccion: "+ e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MESSAJE = ("ERROR" + ":"+ e.getMessage());
            out.P_OUT_EXECUTIONTIME = startTime;
        } finally {
        	if("S".equals(borrarTablaBloqueo))
        		EliminarBloqueoReporteBCCH(con, idsBloqueoReporteBCCH, sql);
        }
        return out;

    }
	
	//METODOS UTILITARIOS
	public <T extends RepoBase> void ActualizarEstados(Connection con, InTransaccion entrada, String estado, String codigoSicam, String mensaje, List<T> registros, String sqlUpdate) throws SQLException{
		logger.info("--Inicio Actualizar Estado Sicam--");
        SQL sql = new SQL();
        List<BitacoraTributario> bt = new ArrayList<BitacoraTributario>();
        List<ResumenUpdate> param = new ArrayList<ResumenUpdate>();
        for (T reg : registros) {
	    	Date fechaOperacion = new Date();
	        String IdDocumento = "";
			String Identificador = "";
			String EventoReporte = "";
			Long ID = 0L;
	    	if (reg instanceof Repo2P1) {
	    		Repo2P1 repo2 = (Repo2P1) reg;
	            fechaOperacion = java.sql.Date.valueOf(repo2.FechaNegociacion);
	            IdDocumento = String.valueOf(repo2.IdDocumento);
				Identificador = repo2.Identificador;
				EventoReporte = repo2.EventoReporte;
				ID = repo2.ID;
	        } else if (reg instanceof Repo3P1) {
	        	Repo3P1 repo3 = (Repo3P1) reg;
	            fechaOperacion = java.sql.Date.valueOf(repo3.FechaOperacion);
	            IdDocumento = String.valueOf(repo3.IdDocumento);
				Identificador = repo3.Identificador;
				EventoReporte = repo3.EventoReporte;
				ID = repo3.ID;
	        } 
	    	String codigo = (reg.errorCode == null || reg.errorCode.isEmpty()) ? codigoSicam : reg.errorCode;
	    	bt.add(new BitacoraTributario(IdDocumento, entrada.p_IdEsquema, entrada.p_Version, Identificador,
	    			EventoReporte, fechaOperacion, ID, estado, mensaje, codigo, String.valueOf(entrada.p_IdUsuario)));
	        param.add(new ResumenUpdate(ID, estado, codigo));
	    }
        logger.info("--Inicio Actualizar Estado Sicam--");
        sql.batch(con, sqlUpdate, param,ResumenUpdate.class);
        logger.info("--Inicio Insertar Bitacora Tributario--");
        sql.batch(con, SQL_INSERT_BITACORA_TRIBUTARIO_SICAM, bt, BitacoraTributario.class);
        logger.info("--Fin Insertar Bitacora Tributario--");
	    con.commit();
	    logger.info("--Fin Actualizar Estado Sicam--");
	}
	
	public <T extends RepoBase> List<ParamsApi.report> GenerarCuerpoReporte (Connection con, InTransaccion entrada, List<T> registros) throws SQLException{
		logger.info("--Inicio Generar Cuerpo Reporte Sicam para Esquema: "+ entrada.p_IdEsquema);
		List<ParamsApi.report> reporte = new ArrayList<>();
		if(registros != null && !registros.isEmpty()) {
			T registro = registros.get(0);
			if(registro instanceof Repo2P1) {
				List<Repo2P1> registros2p1 = (List<Repo2P1>) registros;
				for(Repo2P1 r : registros2p1) {
					StringBuilder sb = new StringBuilder();
					String fechaNegociacion = (!"0001-01-01".equals(r.FechaNegociacion)) ? r.FechaNegociacion + "T" + r.HoraNegociacion : "";
					String fechaSuscripcionContrato = (!"0001-01-01".equals(r.FechaSuscripcionContrato)) ? r.FechaSuscripcionContrato + "T" + r.HoraSuscripcionContrato : "";
					sb.append(r.TipoRegistro != null ? r.TipoRegistro : "").append(";");
					sb.append(r.EventoReporte != null ? r.EventoReporte : "").append(";");
					sb.append(r.TipoOperacion != null ? r.TipoOperacion : "").append(";");
					sb.append(fechaNegociacion != null ? fechaNegociacion : "").append(";");
					sb.append(!"0001-01-01".equals(r.FechaValutaMonedaRecibida) ? r.FechaValutaMonedaRecibida : "").append(";");
					sb.append(!"0001-01-01".equals(r.FechaValutaMonedaEntregada)? r.FechaValutaMonedaEntregada : "").append(";");
					sb.append(r.Identificador != null ? r.Identificador : "").append(";");
					sb.append(r.RUTContraparte1 != null ? r.RUTContraparte1.replace("-", "") : "").append(";");
					sb.append(r.LEIContraparte1 != null ? r.LEIContraparte1 : "").append(";");
					sb.append(r.NombreContraparte1 != null ? r.NombreContraparte1 : "").append(";");
					sb.append(r.RUTContraparte2 != null ? r.RUTContraparte2.replace("-", "") : "").append(";");
					sb.append(r.LEIContraparte2 != null ? r.LEIContraparte2 : "").append(";");
					sb.append(r.NombreContraparte2 != null ? r.NombreContraparte2 : "").append(";");
					sb.append(r.PaisContraparte2 != null ? r.PaisContraparte2 : "").append(";");
					sb.append(r.RUTIntermediario != null ? r.RUTIntermediario.replace("-", "") : "").append(";");
					sb.append(r.PlataformaNegociacion != null ? r.PlataformaNegociacion : "").append(";");
					sb.append(r.CodigoOperacionCambio != null ? r.CodigoOperacionCambio : "").append(";");
					sb.append(r.PaisMonedaExtranjera != null ? r.PaisMonedaExtranjera : "").append(";");
					sb.append(r.MonedaCompradaRecibida != null ? r.MonedaCompradaRecibida : "").append(";");
					sb.append(r.MontoMonedaCompradaRecibida != null ?  formatearMontos(r.MontoMonedaCompradaRecibida, false) : "000000000000000.00000").append(";");
					sb.append(r.MonedaVendidaEntregada != null ? r.MonedaVendidaEntregada : "").append(";");
					sb.append(r.MontoMonedaVendidaEntregada != null ?  formatearMontos(r.MontoMonedaVendidaEntregada, false) : "000000000000000.00000").append(";");
					sb.append(r.ConvencionTipoCambio != null ? r.ConvencionTipoCambio : "").append(";");
					sb.append(r.TipoCambio != null ?  formatearMontos(r.TipoCambio, false) : "000000000000000.00000").append(";");
					sb.append(r.AfectaDerivados != null ? r.AfectaDerivados : "").append(";");
					sb.append(r.IdentificadorContratoDerivados != null ? r.IdentificadorContratoDerivados : "").append(";");
					sb.append(fechaSuscripcionContrato != null ? fechaSuscripcionContrato : "").append(";");
					sb.append(r.OperacionEmpresaRelacionada != null ? r.OperacionEmpresaRelacionada : "").append(";");
					sb.append(r.NumeroInscripcionObligacionExterior != null ? r.NumeroInscripcionObligacionExterior : "").append(";");
					sb.append(r.NombreFinancistaAporante != null ? r.NombreFinancistaAporante : "").append(";");
					sb.append(r.LEIFinancistaAporante != null ? r.LEIFinancistaAporante : "").append(";");
					sb.append(r.NombreReceptorInversion != null ? r.NombreReceptorInversion : "").append(";");
					sb.append(r.LEIReceptorInversion != null ? r.LEIReceptorInversion : "").append(";");
					sb.append(r.ModalidadInversionCustodio != null ? r.ModalidadInversionCustodio : "");
					reporte.add(new ParamsApi.report(sb.toString()));
				}			
			}else if (registro instanceof Repo3P1){
				List<Repo3P1> registros3p1 = (List<Repo3P1>) registros;
				for(Repo3P1 r : registros3p1) {
					String fechaHoraOperacion = (r.FechaOperacion != null && r.HoraOperacion != null) ? r.FechaOperacion + "T" + r.HoraOperacion : "";
		            StringBuilder sb = new StringBuilder();
		            sb.append(r.TipoRegistro != null ? r.TipoRegistro : "").append(";");
		            sb.append(r.RUTReportante != null ? r.RUTReportante.replace("-", "") : "").append(";");
		            sb.append(fechaHoraOperacion != null ? fechaHoraOperacion : "").append(";");
		            sb.append(r.TipoOperacion != null ? r.TipoOperacion : "").append(";");
		            sb.append(r.EventoReporte != null ? r.EventoReporte : "").append(";");
		            sb.append(r.Identificador != null ? r.Identificador : "").append(";");
		            sb.append(r.RUTContraparte != null ? r.RUTContraparte.replace("-", "") : "").append(";");
		            sb.append(r.LEIContraparte != null ? r.LEIContraparte : "").append(";");
		            sb.append(r.NombreContraparte != null ? r.NombreContraparte : "").append(";");
		            sb.append(r.PlataformaNegociacion != null ? r.PlataformaNegociacion : "").append(";");
		            sb.append(r.MontoMonedaComprada != null ? formatearMontos(r.MontoMonedaComprada, false) : "000000000000000.00000").append(";");
    	            sb.append(r.MontoMonedaVendida != null ? formatearMontos(r.MontoMonedaVendida, false) : "000000000000000.00000").append(";");
    	            sb.append(r.TipoCambio != null ? formatearMontos(r.TipoCambio, true) : "00000.00");
    	            reporte.add(new ParamsApi.report(sb.toString()));
				}					
			}
		}
		logger.info("--FIN Generar Cuerpo Reporte Sicam, registros:  "+ registros.size());
		return reporte;
		
	}
	
	public static String formatearMontos(BigDecimal monto, boolean esEspecial) {
	    if (monto == null) {
	        return "0";
	    }
	    int decimal = esEspecial ? 2 : 5;
	    int entero = esEspecial ? 5 : 15;
	    monto = monto.setScale(decimal, RoundingMode.HALF_UP);
	    String[] partes = monto.toPlainString().split("\\.");
	    String parteEntera = partes[0];
	    String parteDecimal = partes.length > 1 ? partes[1] : "";
	    if (parteEntera.length() > entero) {
	        parteEntera = parteEntera.substring(0, entero);
	    }
	    if (parteDecimal.length() > decimal) {
	        parteDecimal = parteDecimal.substring(0, decimal);
	    }
	    return parteDecimal.isEmpty() ? parteEntera : parteEntera + "." + parteDecimal;
	}
	
	public <T>List<T> ObtenerRegistros(Connection con, List<InTransaccion.Registros> IDRegistro, Class<T> reporte, String sqlBase) throws SQLException {
		logger.info("----Inicio Obtener Registros reporte-----");
		StringBuilder idsParam = new StringBuilder();
	    HashMap<String, Object> p2 = new HashMap<>();
	    List<Long> ids = IDRegistro.stream().map(r -> r.ID).collect(Collectors.toList());
	    for (int i = 0; i < ids.size(); i++) {
	        String paramName = "p_ID" + i;
	        p2.put(paramName, ids.get(i));
	        if (i > 0) idsParam.append(",");
	        idsParam.append(":").append(paramName);
	    }
	    String SQL_OBTENER_REGISTROS_REPORTE = sqlBase.replace(":p_ID", idsParam);
	    List<T> datosReporte = new SQL().query(con, SQL_OBTENER_REGISTROS_REPORTE, p2, reporte);
	    RegistrosYaEnviados = (long) (IDRegistro.size() - datosReporte.size());
	    logger.info("----Fin Obtener Registros reporte-----");
	    return datosReporte;
	}
	
	public static void trust() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts;
        trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };
        try { // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (GeneralSecurityException e) {
        }
    }
	
	//Metodo para obtener configuración del reporte
	private RepoConfig<? extends RepoBase> RepoConfig(String esquema) {
	    switch (esquema) {
	        case "REPO2P1":
	            return new RepoConfig<>(Repo2P1.class, SQL_OBTENER_REGISTRO_REPO2P1, SQL_UPDATE_ESTADO_SICAM_REPO2P1);
	        case "REPO3P1":
	            return new RepoConfig<>(Repo3P1.class, SQL_OBTENER_REGISTRO_REPO3P1, SQL_UPDATE_ESTADO_SICAM_REPO3P1);
	        default:
	            throw new IllegalArgumentException("Esquema no soportado: " + esquema);
	    }
	}
	
	//Metodo para actualizar errores genericos 400
	private <T extends RepoBase> void ErrorConexion(Connection con, InTransaccion entrada, List<T> registros, String sqlUpdate, String mensaje) throws SQLException {
	    ActualizarEstados(con, entrada, "RZD", "REPO_ERR_400", mensaje, registros, sqlUpdate);
	}
	
	//Metodo para procesar la respuesta sicam al reporte enviado
	private <T extends RepoBase> void ProcesarRespuestaSicam(Connection con, InTransaccion entrada, String sqlUpdate, List<T> registros,  OutTransaccion.RespuestaWS respuesta) throws SQLException{
	    logger.info("----Analizar respuesta SICAM----");
	    if (!"07".equals(respuesta.code)) {
	    	String codeSicam = "00".equals(respuesta.code)? "REC" : "RZD";
	        for (T reg : registros) {
	            reg.errorCode = respuesta.code;
	            reg.message = respuesta.message;
	        }
	        ActualizarEstados(con, entrada, codeSicam, respuesta.code, respuesta.message, registros, sqlUpdate);
	        return;
	    }
	    if ("07".equals(respuesta.code)) {
	        logger.info("--Errores puntuales detectados--");
	        for (OutTransaccion.MapeoDetalle resp : respuesta.detail) {
	            int indexRegistro = Integer.parseInt(resp.registro) - 1;
	            if (indexRegistro >= 0 && indexRegistro < registros.size()) {
	                T reg = registros.get(indexRegistro);
	                if ((resp.errorCode == null || resp.errorCode.isEmpty()) && resp.message != null) {
	                    Matcher m = Pattern.compile("^\\s*(\\d+)\\s*:").matcher(resp.message);
	                    if (m.find()) {
	                        reg.errorCode = m.group(1);
	                    }
	                } else {
	                    reg.errorCode = resp.errorCode;
	                }
	                reg.message = resp.message;
	            }
	        }
	        for (T reg : registros) {
	            if (reg.errorCode == null || reg.errorCode.isEmpty()) {
	                reg.errorCode = respuesta.code;
	                reg.message = respuesta.message;
	            }
	        }
	        ActualizarEstados(con, entrada, "RZD", respuesta.code, respuesta.message, registros, sqlUpdate);
	        return;
	    }
	}	
	
	private <T extends RepoBase> void AplicarBloqueoReporteBCCH(Connection con, InTransaccion entrada, List<BloqueoReporteBCCH> idsBloqueoReporteBCCH, List<T> registrosReporte, SQL sql) throws SQLException {
		logger.trace("INICIO::EnviarTransaccion.AplicarBloqueoReporteBCCH");
		List<BloqueoReporteBCCH> idsBloqueos = registrosReporte.stream()
														       .map(registro -> {
														           BloqueoReporteBCCH bloqueo = new BloqueoReporteBCCH();
														           bloqueo.IdBloqueoReporteBCCH = entrada.p_IdEsquema + "_" + registro.ID;
														           return bloqueo;
														       })
														       .collect(Collectors.toList());
		idsBloqueoReporteBCCH.addAll(idsBloqueos);
		logger.debug("INICIO::SQL_INSERT_BLOQUEO_REPORTE_BCCH");
		sql.batch(con, SQL_INSERT_BLOQUEO_REPORTE_BCCH, idsBloqueoReporteBCCH, BloqueoReporteBCCH.class);
		logger.debug("FIN::SQL_INSERT_BLOQUEO_REPORTE_BCCH");
		logger.trace("FIN::EnviarTransaccion.AplicarBloqueoReporteBCCH");
	}
	
	private void EliminarBloqueoReporteBCCH(Connection con, List<BloqueoReporteBCCH> idsBloqueoReporteBCCH, SQL sql) throws SQLException {
		logger.debug("INICIO::SQL_DELETE_BLOQUEO_REPORTE_BCCH");
		sql.batch(con, SQL_DELETE_BLOQUEO_REPORTE_BCCH, idsBloqueoReporteBCCH, BloqueoReporteBCCH.class);
		con.commit();
		logger.debug("FIN::SQL_DELETE_BLOQUEO_REPORTE_BCCH");
	}
	public static void main(String[] args) throws Exception, SQLException {
		Connection con = null;
		Gson gson = new Gson();

		try {
			System.out.println("== BEGIN ==");
			//(con = PlataformaUtils.getPRUEBAConnection();
			con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties");
		
			String json1 = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":9507,\"p_IdModulo\":\"NOR\",\"p_IdEsquema\":\"REPO2P1\",\"p_Version\":\"2025/12\",\"p_Reporte\":[{\"ID\":100},{\"ID\":101}]}";
			
			InTransaccion objIn = gson.fromJson(json1, InTransaccion.class);

			EnviarTransaccion demo = new EnviarTransaccion();

			OutTransaccion ObjOut = demo.logica(con, objIn);

			System.out.println(ObjOut.toString());
			//con.commit();
			con.rollback();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			con.close();
		}
		System.out.println("== END ==");

	};



}