package cl.bcs.plataforma.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cl.bcs.plataforma.jobs.ActualizarParametrosFinancierosUF.ParametrosFinancierosWebService.Cabecera.ConsultatbiepfResult;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.CallRestService;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class ActualizarParametrosFinancierosUF extends BcsJob implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(ActualizarParametrosFinancierosUF.class);
	private final String SQL_UPSERT_PARAMETRO_FINANCIERO_UF = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarParametrosFinancierosUF_UpsertParametroFinancieroUF.sql");
	private final String CODIGO_UF = "UF";
	private HashMap<String,String> parametrosFinancierosMesa = null;
	
	public ActualizarParametrosFinancierosUF(){
		//Tipo de registro de paridad para codigos de moneda dolar.
		parametrosFinancierosMesa = new HashMap<>();
		parametrosFinancierosMesa.put("UF", "UF");
	}
	
	public static class DefinicionParametroFinanciero{
		public String IdParametroFinanciero;
		public String Periodo;
		public String IdMonedaParametro;
		public String IdMonedaValor;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class ParametroFinanciero{
	    public String IdParametroFinanciero;
	    public Date Fecha;
	    public String AgnoMes;
	    public String Agno;
	    public BigDecimal Valor;
	    @Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public  class ParametrosFinancierosWebService{
		public class Cabecera{
			public class ConsultatbiepfResult{
				public String codigo;
				public String fecha;
				public BigDecimal valor;
				@Override
				public String toString() {
					return Utils.printJson(this, false);
				}
				public String getCodigo() {
					return codigo;
				}
				public String getFecha() {
					return fecha;
				}
				public BigDecimal getValor() {
					return valor;
				}
			}
			List<ConsultatbiepfResult> consultatbiepfResult;
			@Override
			public String toString() {
				return Utils.printJson(this, false);
			}
			public List<ConsultatbiepfResult> getConsultatbiepfResult() {
				return consultatbiepfResult;
			}
		}
		public Cabecera cabecera;
		public Long id;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
		public Cabecera getCabecera() {
			return cabecera;
		}
		public Long getId() {
			return id;
		}
	}
	
	@Override
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		long idEmpresa = Long.parseLong(this.getOptionalParam("IdEmpresa").orElse("1"));
		return logica(conn,false, "", idEmpresa);
	}
	
	public boolean logica(Connection conn, boolean debug, String fechaSistemaDebug, long idEmpresa) throws InterruptedException, Throwable {
		logger.info(":ACTUALIZARPARAMETROSFINANCIEROS:  ****** INICIO  *******");
		
		try(PreparedStatement pstmtSchema = conn.prepareStatement("SET SCHEMA "+Utils.getSchema());){
			pstmtSchema.execute();
			//obtener configuraciones
			HashMap<String, HashMap<String, String>> configuraciones = Utils.getConfiguraciones(conn, idEmpresa);
			String servidor = configuraciones.get("PLATAFORMA").get("MER.ServidorTec");
			String urlWebservice = configuraciones.get("PLATAFORMA").get("MER.UrlServicioParidades");
			String user = configuraciones.get("PLATAFORMA").get("MER.UsuarioServidorTec");
			String passwd = configuraciones.get("PLATAFORMA").get("MER.ClaveServidorTec");
			boolean auth = true;
			String fechaSistema = (debug) ? fechaSistemaDebug :configuraciones.get("PLATAFORMA").get("PLA.FechaSistema"); //si esta en modo debug, utilizar la fecha de sistema pasada por parametros.
			
			LocalDate fechaFinal = LocalDate.parse(fechaSistema, DateTimeFormatter.ISO_DATE).plusMonths(1);
			String fechaFinalString = fechaFinal.format(DateTimeFormatter.ISO_DATE);		
			
			JsonArray entradaWebservice = new JsonArray();
			JsonObject periodo = new JsonObject();
			periodo.addProperty("id", 0);
			periodo.addProperty("fechaini", fechaSistema);
			periodo.addProperty("fechafin", fechaFinalString);
			periodo.addProperty("periodo", "DI"); 
			periodo.addProperty("codigo", "UF");
			entradaWebservice.add(periodo);
					
			//realizar llamada rest para obtener los parametros financieros UF desde bursatil.
			CallRestService call = new CallRestService(servidor, urlWebservice, user, passwd, auth, debug);
			String salidaWebservice = call.callRestPost(entradaWebservice.toString());
			List<ParametrosFinancierosWebService> parametrosFinancierosWebserviceUF = Arrays.asList(Utils.getGsonBuilder().fromJson(salidaWebservice, ParametrosFinancierosWebService[].class));
			logger.debug("parametros financieros:" + parametrosFinancierosWebserviceUF);
			
			//Actualizar parametros financieros UF en modulo Tipos de cambio e indices
			try(PreparedStatement pstmtParametro = conn.prepareStatement(SQL_UPSERT_PARAMETRO_FINANCIERO_UF); ){
				
				Map<String, List<ConsultatbiepfResult>> parametrosFinancierosDiarosPorFecha = parametrosFinancierosWebserviceUF.stream()
						.filter(param -> param.id.equals(0L))
						.map(param -> param.getCabecera().getConsultatbiepfResult())
						.findFirst()
						.get()
						.stream()
						.map(parametroFinanciero -> {
							return parametroFinanciero;
						})
						.collect(Collectors.groupingBy(param -> param.fecha));
							
				for (LocalDate fechaActualizacion = LocalDate.parse(fechaSistema, DateTimeFormatter.ISO_DATE); !fechaActualizacion.isAfter(fechaFinal); fechaActualizacion = fechaActualizacion.plusDays(1)) {
					String fechaActualizacionString = fechaActualizacion.format(DateTimeFormatter.ISO_DATE);
					List<ActualizarParametrosFinancierosUF.ParametrosFinancierosWebService.Cabecera.ConsultatbiepfResult> valoresActualizar = parametrosFinancierosDiarosPorFecha.get(fechaActualizacionString);
					if (valoresActualizar != null) {
						valoresActualizar.stream()
						.filter(param -> CODIGO_UF.equals(param.codigo.toUpperCase()))
						.forEach(parametroFinancieroActualizar -> {
							if (parametroFinancieroActualizar.valor != null && parametroFinancieroActualizar.valor.compareTo(BigDecimal.ZERO) != 0) {
							    agregarActualizacionParidadesIndices(pstmtParametro, "DI", parametroFinancieroActualizar.codigo, fechaActualizacionString, parametroFinancieroActualizar.valor);
							}
						});	
					}	
				}

				int[] res = pstmtParametro.executeBatch();
				logger.debug( String.format("Actualizacion tabla tipo de cambio e indices: %s", (Arrays.stream(res).filter(r -> r==PreparedStatement.EXECUTE_FAILED).count()==0) ? "OK" : "NOK") );
			
				AgregarLog(conn,fechaSistema, idEmpresa);		
			}
		}
		
		return true;	
	}
	
	private boolean agregarActualizacionParidadesIndices(PreparedStatement pstmtParametro, String periodo, String codigo, String fechaActualizacion, BigDecimal valor) {
		try {
			String agnoMes = "";
			String agno = "";
			
			pstmtParametro.setString(1, codigo);
			pstmtParametro.setString(2, fechaActualizacion);
			pstmtParametro.setString(3, agnoMes);
			pstmtParametro.setString(4, agno);
			pstmtParametro.setBigDecimal(5, valor);
			pstmtParametro.setString(6, codigo);
			pstmtParametro.setString(7, fechaActualizacion);
			pstmtParametro.setString(8, agnoMes);
			pstmtParametro.setString(9, agno);
			pstmtParametro.addBatch();
			logger.debug(String.format("UPDATE TIPO DE CAMBIO E INDICES: {Codigo: %s, Fecha: %s, Valor: %f}", codigo, fechaActualizacion, valor));
			return true;
		} catch (SQLException e) {
			logger.error(e);
			return false;
		} 	
	}
		
	@Override
	public void onError(Throwable t) {
		logger.debug(":ACTUALIZARPARAMETROSFINANCIEROS:  ****** ERROR  *******");
		t.printStackTrace();
	}

	@Override
	public void pause() {

	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		return null;
	}
	
	public void AgregarLog (Connection conn, String fechaSistema, long idEmpresa) {
		try {
			
			//CABECERA
			AgregarLog.InAgregarLog entradaAgregarLog = new AgregarLog.InAgregarLog();
			AgregarLog.InAgregarLog.Log log = new AgregarLog.InAgregarLog.Log();			
			
			entradaAgregarLog.p_IdEmpresa = idEmpresa;
			entradaAgregarLog.p_IdModulo = "PLA";
			entradaAgregarLog.p_IdUsuario = 1L;
			entradaAgregarLog.p_UsaReglasNegocio = "N";
										
			log.IdCabeceraLog = 1L;
			log.IdModulo = "PLA";
			log.IdUsuario = 1L;
			log.IdEmpresa = idEmpresa;
			log.FechaSistemaLog = fechaSistema;
			log.IdUsuarioAtendido = 0L;
			log.IdCliente = 1L;
			log.EstadoCliente = "";
			log.NombreCliente = "";
			log.IdTipoDocumentoIdentidad = "";
			log.IdDocumentoIdentidad = "";
			log.IdPortafolio = 0L;
			log.EstadoPortafolio = "";
			log.IdTipoRegistro = "PARIDADYTIPOCAMBIO";
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
			log.EstadoProceso = "";
			log.IdPersona = 1L;
			log.IdTipoLog = "MODIFICADOR";
			log.NombreProceso = "Actualizacion de Parametros Financieros desde WS Bursatil";
						
			//AÑADIR AL LOG
			entradaAgregarLog.p_Log.add(log);
			
			//REGISTRO
			AgregarLog agregarLogService = new AgregarLog();
			agregarLogService.logica(conn, entradaAgregarLog);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
			
	//solo para test
	public static void main(String[] args) throws Throwable {
		
		logger.info("== START ==");
		try(Connection con = PlataformaUtils.getPropertiesConnection("qa02", "connections.properties")){
			ActualizarParametrosFinancierosUF apf = new ActualizarParametrosFinancierosUF();
			apf.logica(con, false, "", 1);
			String desdeFechaSistema = "2026-02-02";
			String hastaFechaSistema = "2026-02-02";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date fechaInicio = sdf.parse(desdeFechaSistema);
			java.util.Date fechaTermino = sdf.parse(hastaFechaSistema);
			Calendar cal = Calendar.getInstance();
			while(fechaInicio.compareTo(fechaTermino)<=0) {
				
				logger.debug(apf.logica(con,true, desdeFechaSistema, 1L));
				logger.debug("----------------------------------------------------------------------");
				cal.setTime(fechaInicio);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				fechaInicio = cal.getTime();
				desdeFechaSistema = sdf.format(fechaInicio);
			}
			con.rollback();
		}
		logger.debug("== END ==");
	}
	
}