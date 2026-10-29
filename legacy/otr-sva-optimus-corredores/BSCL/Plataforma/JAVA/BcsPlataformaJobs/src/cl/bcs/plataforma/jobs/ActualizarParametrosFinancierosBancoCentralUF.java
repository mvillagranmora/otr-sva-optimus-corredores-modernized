package cl.bcs.plataforma.jobs;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.jobs.ActualizarParametrosFinancierosBancoCentralUF.ParametrosFinancierosBancoCentral.Series.Obs.ObsConSerie;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.CallRestService;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class ActualizarParametrosFinancierosBancoCentralUF extends BcsJob implements JavaCall{
	
	private static final Logger logger = LogManager.getLogger(ActualizarParametrosFinancierosBancoCentralUF.class);
	private final String SQL_UPSERT_PARAMETRO_FINANCIERO_UF = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarParametrosFinancierosBancoCentralUF_UpsertParametroFinancieroUF.sql");
	private final String SQL_SELECT_DEFINICION_PARAMETROS_FINANCIEROS_UF = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizarParametrosFinancierosBancoCentralUF_SelectDefinicionParametrosFinancierosUF.sql");
	private final String CODIGO_UF = "F073.UFF.PRE.Z.D";
	private HashMap<String,String> parametrosFinancierosMesa = null;
	
	public ActualizarParametrosFinancierosBancoCentralUF(){
		//Tipo de registro de paridad para codigos de moneda dolar.
		parametrosFinancierosMesa = new HashMap<>();
		parametrosFinancierosMesa.put("UF", "UF");
	}
	
	public static class DefinicionParametroFinanciero {
		public String IdParametroFinanciero;
		public String Periodo;
		public String IdMonedaParametro;
		public String IdMonedaValor;
		public String CodigoSerieBancoCentral;
		
	}
		
	public static class ParametroFinanciero extends BcsPojo {
	    public String IdParametroFinanciero;
	    public Date Fecha;
	    public String AgnoMes;
	    public String Agno;
	    public BigDecimal Valor;   
	}
	
	public static class ParametrosFinancierosBancoCentral {   
			public Long Codigo;
			public String Descripcion;
			public Series Series;
			public List<Object> SeriesInfos;

	        public Long getCodigo() {
	            return Codigo;
	        }

	        public String getDescripcion() {
	            return Descripcion;
	        }

	        public Series getSeries() {
	            return Series;
	        }

	        public List<Object> getSeriesInfos() {
	            return SeriesInfos;
	        }

	        @Override
	        public String toString() {
	            return Utils.printJson(this, false);
	        }

	        public static class Series {
	        	public String descripEsp;
	        	public String descripIng;
	        	public String seriesId;
	        	public List<Obs> Obs;

	            public String getDescripEsp() {
	                return descripEsp;
	            }

	            public String getDescripIng() {
	                return descripIng;
	            }

	            public String getSeriesId() {
	                return seriesId;
	            }

	            public List<Obs> getObs() {
	                return Obs;
	            }

	            @Override
	            public String toString() {
	                return Utils.printJson(this, false);
	            }

	            public static class Obs {
	            	public String indexDateString;
	            	public String value;
	            	public String statusCode;

	                public String getIndexDateString() {
	                    return indexDateString;
	                }

	                public BigDecimal getValue() {
	                    try {
	                        return new BigDecimal(value);
	                    } catch (NumberFormatException e) {
	                        return BigDecimal.ZERO;
	                    }
	                }

	                public String getStatusCode() {
	                    return statusCode;
	                }

	                @Override
	                public String toString() {
	                    return Utils.printJson(this, false);
	                }
	                
	                public static class ObsConSerie {
	                    public String indexDateString;
	                    public BigDecimal valor;
	                    public String statusCode;
	                    public String seriesId;

	                    public ObsConSerie(String indexDateString, String value, String statusCode, String seriesId) {
	                        this.indexDateString = indexDateString;
	                        this.statusCode = statusCode;
	                        this.seriesId = seriesId;
	                        try {
	                            this.valor = new BigDecimal(value);
	                        } catch (NumberFormatException e) {
	                            this.valor = BigDecimal.ZERO;
	                        }     
	                    }
	                    @Override
		                public String toString() {
		                    return Utils.printJson(this, false);
		                }
	                }
	           }
	      }
	}
	
	@Override
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		long idEmpresa = Long.parseLong(this.getOptionalParam("IdEmpresa").orElse("1"));
		return logica(conn,false, "", idEmpresa);
	}
	
	public boolean logica(Connection conn, boolean debug, String fechaSistemaDebug, long idEmpresa) throws InterruptedException, Throwable {
		logger.info(":ACTUALIZARPARAMETROSFINANCIEROS:  ****** INICIO  *******");
		SQL sql = new SQL();
		
		List<DefinicionParametroFinanciero> listaDefinicionParametrosFinancieros = sql.query(conn, SQL_SELECT_DEFINICION_PARAMETROS_FINANCIEROS_UF, new HashMap<>(), DefinicionParametroFinanciero.class);		
		Map<String, DefinicionParametroFinanciero> definicionPorSerie = listaDefinicionParametrosFinancieros.stream()
				.filter(p -> p.CodigoSerieBancoCentral != null && !p.CodigoSerieBancoCentral.isEmpty())
				.collect(Collectors.toMap(p -> p.CodigoSerieBancoCentral, p -> p, (a, b) -> a));
		
		try(PreparedStatement pstmtSchema = conn.prepareStatement("SET SCHEMA "+Utils.getSchema());){
			pstmtSchema.execute();
			//obtener configuraciones
			HashMap<String, HashMap<String, String>> configuraciones = Utils.getConfiguraciones(conn, idEmpresa);
			String servidor = configuraciones.get("PLATAFORMA").get("MER.ServidorTecBancoCentral");
			String urlWebservice = configuraciones.get("PLATAFORMA").get("MER.UrlServicioParidadesBancoCentral");
			String user = configuraciones.get("PLATAFORMA").get("MER.UsuarioServidorTecBancoCentral");
			String passwd = configuraciones.get("PLATAFORMA").get("MER.ClaveServidorTecBancoCentral");
			boolean auth = true;
			String fechaSistema = (debug) ? fechaSistemaDebug :configuraciones.get("PLATAFORMA").get("PLA.FechaSistema"); //si esta en modo debug, utilizar la fecha de sistema pasada por parametros.
			LocalDate fechaFinal = LocalDate.parse(fechaSistema, DateTimeFormatter.ISO_DATE).plusMonths(1);
			String fechaFinalString = fechaFinal.format(DateTimeFormatter.ISO_DATE);
							
			//realizar llamada rest para obtener los parametros financieros y paridades tipo de cambio desde bursatil.
			CallRestService call = new CallRestService(servidor, urlWebservice, user, passwd, auth, debug);
			
			List<ParametrosFinancierosBancoCentral> parametrosFinancierosBancoCentral = new ArrayList<>();
			String codigoSerie = listaDefinicionParametrosFinancieros.get(0).CodigoSerieBancoCentral;
				
			 // Construir los query params codificados
		    String queryParams = String.format("user=%s&pass=%s&firstdate=%s&lastdate=%s&timeseries=%s&function=GetSeries",
		        URLEncoder.encode(user, StandardCharsets.UTF_8.toString()),
		        URLEncoder.encode(passwd, StandardCharsets.UTF_8.toString()),
		        URLEncoder.encode(fechaSistema, StandardCharsets.UTF_8.toString()),
		        URLEncoder.encode(fechaFinalString, StandardCharsets.UTF_8.toString()),
		        URLEncoder.encode(codigoSerie, StandardCharsets.UTF_8.toString())
		    );
		    
			try {
				String salidaWebservice = call.callRestGet(queryParams);
				
				ParametrosFinancierosBancoCentral respuesta = Utils.getGsonBuilder().fromJson(salidaWebservice, ParametrosFinancierosBancoCentral.class);
				parametrosFinancierosBancoCentral.add(respuesta);

			} catch (Exception e) {
				logger.error("Error al consultar serie " + codigoSerie, e);			
			}	
			
			logger.debug("parametros financieros:" + parametrosFinancierosBancoCentral);
			//Actualizar parametros financieros UF en modulo Tipos de cambio e indices
			try(PreparedStatement pstmtParametro = conn.prepareStatement(SQL_UPSERT_PARAMETRO_FINANCIERO_UF); ){
				
				List<ObsConSerie> observaciones = parametrosFinancierosBancoCentral.stream()
					    .filter(param -> param.Codigo.equals(0L))
					    .flatMap(param -> {
					        String seriesId = param.Series.getSeriesId();
					        return param.Series.Obs.stream()
					        		.map(obs -> {
					        		    String fechaOriginal = obs.getIndexDateString();
					        		    String fechaNormalizada;
					        		    try {
					        		        DateTimeFormatter entrada = DateTimeFormatter.ofPattern("dd-MM-yyyy");
					        		        DateTimeFormatter salida = DateTimeFormatter.ofPattern("yyyy-MM-dd");
					        		        fechaNormalizada = LocalDate.parse(fechaOriginal, entrada).format(salida);
					        		    } catch (Exception e) {
					        		        logger.error("Error formateando indexDateString: {}", fechaOriginal, e);
					        		        fechaNormalizada = fechaOriginal; // deja como venía si falla
					        		    }
					        		    return new ObsConSerie(
					        		        fechaNormalizada,
					        		        obs.value,
					        		        obs.getStatusCode(),
					        		        seriesId
					        		    );
					        		});
					    })
					    .collect(Collectors.toList());
				
				Map<String,  List<ObsConSerie>> parametrosFinancierosDiarosPorFecha = observaciones.stream()					
						.collect(Collectors.groupingBy(param -> param.indexDateString));
				
				if(parametrosFinancierosDiarosPorFecha.isEmpty()) {
					return false; 
				}
						
				for (LocalDate fechaActualizacion = LocalDate.parse(fechaSistema, DateTimeFormatter.ISO_DATE); !fechaActualizacion.isAfter(fechaFinal); fechaActualizacion = fechaActualizacion.plusDays(1)) {
					String fechaActualizacionString = fechaActualizacion.format(DateTimeFormatter.ISO_DATE);
					logger.debug("Actualizar UF en tabla tipo de cambio e indices");
					List<ObsConSerie> obsFechaSistema = parametrosFinancierosDiarosPorFecha.getOrDefault(fechaActualizacionString, new ArrayList<>());
					obsFechaSistema.stream()
					.filter(param -> param.seriesId != null && CODIGO_UF.equalsIgnoreCase(param.seriesId))
					.forEach(parametroFinancieroFechaSistema -> {
						if (parametroFinancieroFechaSistema.valor != null && parametroFinancieroFechaSistema.valor.compareTo(BigDecimal.ZERO) != 0) {
							DefinicionParametroFinanciero def = definicionPorSerie.get(parametroFinancieroFechaSistema.seriesId);
							String idParametro = def != null ? def.IdParametroFinanciero : parametroFinancieroFechaSistema.seriesId;
							agregarActualizacionParidadesIndices(pstmtParametro, "DI", idParametro, fechaActualizacionString, parametroFinancieroFechaSistema.valor);
						}
					});
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
		logger.debug(":ACTUALIZARPARAMETROSFINANCIEROSWSBANCOCENTRAL:  ****** ERROR  *******");
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
			log.NombreProceso = "Actualizacion de Parametros Financieros desde WS Banco Central";
						
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
			ActualizarParametrosFinancierosBancoCentralUF apf = new ActualizarParametrosFinancierosBancoCentralUF();
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