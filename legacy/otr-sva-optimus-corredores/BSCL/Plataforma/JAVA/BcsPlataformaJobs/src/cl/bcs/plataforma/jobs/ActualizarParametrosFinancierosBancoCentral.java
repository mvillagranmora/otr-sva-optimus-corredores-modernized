package cl.bcs.plataforma.jobs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.nuam.StoredProcedure;
import cl.bcs.plataforma.jobs.ActualizarParametrosFinancierosBancoCentral.ParametrosFinancierosBancoCentral.Series.Obs.ObsConSerie;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.CallRestService;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/** Job que actualiza los parametros financieros del sistema
 * UPD_PARAMS_FINANCIEROS_OPEN: Actualizar parametros financieros y paridades en apertura
 * UPD_PARAMS_FINANCIEROS_CLOSE: Actualizar parametros financieros y paridades en cierre
 * @author chernandez
 *
 */
public class ActualizarParametrosFinancierosBancoCentral extends BcsJob{
	
	private static final Logger logger = LogManager.getLogger(ActualizarParametrosFinancierosBancoCentral.class);
	private final String SQL_UPSERT_PARAMETRO_FINANCIERO = "UPSERT \"BSCL.Plataforma.BD::MER.ParametroFinanciero\"(\"IdParametroFinanciero\",\"Fecha\",\"AgnoMes\",\"Agno\",\"Valor\",\"FechaActualizacion\") VALUES(?,?,?,?,?,CURRENT_UTCTIMESTAMP) WHERE \"IdParametroFinanciero\"=? AND \"Fecha\"=? AND \"AgnoMes\"=? AND \"Agno\"=?";
	private final String SQL_SELECT_DEFINICION_PARAMETROS_FINANCIEROS = "SELECT \"IdParametroFinanciero\", \"Periodo\", \"IdMonedaParametro\", \"IdMonedaValor\", \"CodigoSerieBancoCentral\" FROM \"BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero\"";
	private final String CODIGO_DOLAR_OBSERVADO_WEBSERVICE = "F073.TCO.PRE.Z.D";
	private final String CODIGO_DOLAR_OBSERVADO = "F073.TCO.PRE.Z.D";
	private final String CODIGO_UF = "F073.UFF.PRE.Z.D";
	private final String CODIGO_EUR = "F072.CLP.EUR.N.O.D";
	private final String CODIGO_IPC = "F074.IPC.IND.Z.EP23.C.M";
	private final String IPC = "IPC";
	private HashMap<String,String> parametrosFinancierosMesa = null;
	
	private final SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat AGNO_MES_FORMAT = new SimpleDateFormat("yyyy-MM");
	private final SimpleDateFormat AGNO_FORMAT = new SimpleDateFormat("yyyy");
	
	public ActualizarParametrosFinancierosBancoCentral(){
		//Tipo de registro de paridad para codigos de moneda dolar.
		parametrosFinancierosMesa = new HashMap<>();
		parametrosFinancierosMesa.put("DO", "DOLAR OBSERVADO");
		parametrosFinancierosMesa.put("USDAC", "DOLAR_ACUERDO");
		parametrosFinancierosMesa.put("USDAP", "DOLAR_APE_BCHILE");
		parametrosFinancierosMesa.put("USDBC", "DOLAR_APE_12_BCHILE");
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
	
	public static class RangoFechas {
		public String firstdate;
		public String lastdate;
	}
	
	@Override
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		long idEmpresa = Long.parseLong(this.getOptionalParam("IdEmpresa").orElse("1"));
		return logica(conn,false, "", idEmpresa);
	}
	
	public boolean logica(Connection conn, boolean debug, String fechaSistemaDebug, long idEmpresa) throws InterruptedException, Throwable {
		logger.info(":ACTUALIZARPARAMETROSFINANCIEROS:  ****** INICIO  *******");
		SQL sql = new SQL();

		//obtener lsita de parametros financieros que seran actualizados
		List<DefinicionParametroFinanciero> listaDefinicionParametrosFinancieros = sql.query(conn, SQL_SELECT_DEFINICION_PARAMETROS_FINANCIEROS, new HashMap<>(), DefinicionParametroFinanciero.class);
				
		Map<String, DefinicionParametroFinanciero> definicionPorSerie = listaDefinicionParametrosFinancieros.stream()
				.filter(p -> p.CodigoSerieBancoCentral != null && !p.CodigoSerieBancoCentral.isEmpty())
				.collect(Collectors.toMap(p -> p.CodigoSerieBancoCentral, p -> p, (a, b) -> a));
		
		//obtener los parametros financieros a los cuales es necesario calcular su paridad en USD.
		final List<String> monedasEnUsd = listaDefinicionParametrosFinancieros.stream()
				.filter(param -> Objects.nonNull(param.IdMonedaValor))
				.filter(param -> param.IdMonedaValor.equals("USD"))
				.map(param -> param.IdMonedaParametro).collect(Collectors.toList());

		

		try(PreparedStatement pstmtSchema = conn.prepareStatement("SET SCHEMA "+Utils.getSchema());){
			pstmtSchema.execute();
			//obtener configuraciones
			HashMap<String, HashMap<String, String>> configuraciones = Utils.getConfiguraciones(conn, idEmpresa);
			long idPais = Long.valueOf(configuraciones.get("PLATAFORMA").get("PLA.PaisSistema"));
			String servidor = configuraciones.get("PLATAFORMA").get("MER.ServidorTecBancoCentral");
			String urlWebservice = configuraciones.get("PLATAFORMA").get("MER.UrlServicioParidadesBancoCentral");
			String user = configuraciones.get("PLATAFORMA").get("MER.UsuarioServidorTecBancoCentral");
			String passwd = configuraciones.get("PLATAFORMA").get("MER.ClaveServidorTecBancoCentral");
			boolean auth = true;
			String fechaSistema = (debug) ? fechaSistemaDebug :configuraciones.get("PLATAFORMA").get("PLA.FechaSistema"); //si esta en modo debug, utilizar la fecha de sistema pasada por parametros.
			
			//obtenr fecha anterior
			ObtenerDiaAnterior diaAnterior = new ObtenerDiaAnterior();
			InObtenerDiaAnterior inDiaAnteriro = new InObtenerDiaAnterior();
			inDiaAnteriro.diaHabil="S";
			inDiaAnteriro.idPais = idPais;
			inDiaAnteriro.fechas.add(new cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior.FechasIn(fechaSistema, 1L));
			String fechaSistemaAnterior = diaAnterior.logica(conn, inDiaAnteriro).fechas.stream().findFirst().get().fechaCalculada;
			
			//obtener siguiente dia habil
			ObtenerDiaSiguiente diaSgte = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente diaSgteIn = new InObtenerDiaSiguiente();
			diaSgteIn.p_idPais = idPais;
			diaSgteIn.p_diaHabil = "S";
			diaSgteIn.p_fechas.add(new FechasIn(fechaSistema, 1L, idPais));
			String fechaSistemaSiguiente = diaSgte.logica(conn, diaSgteIn).p_out_fechas.stream().findFirst().get().fechaCalculada;
						
			List<DefinicionParametroFinanciero> listaCodigos = listaDefinicionParametrosFinancieros.stream()
					.filter(p -> !p.CodigoSerieBancoCentral.isEmpty())
					.collect(Collectors.toList());
							
			//realizar llamada rest para obtener los parametros financieros y paridades tipo de cambio desde bursatil.
			CallRestService call = new CallRestService(servidor, urlWebservice, user, passwd, auth, debug);
			
			List<ParametrosFinancierosBancoCentral> parametrosFinancierosBancoCentral = new ArrayList<>();
				
			for (DefinicionParametroFinanciero param : listaCodigos) {
				
				String codigoSerie = param.CodigoSerieBancoCentral;
				RangoFechas rango = obtenerFechasConsulta(codigoSerie, fechaSistema, fechaSistemaAnterior, fechaSistemaSiguiente);
				
				 // Construir los query params codificados
			    String queryParams = String.format("user=%s&pass=%s&firstdate=%s&lastdate=%s&timeseries=%s&function=GetSeries",
			        URLEncoder.encode(user, StandardCharsets.UTF_8.toString()),
			        URLEncoder.encode(passwd, StandardCharsets.UTF_8.toString()),
			        URLEncoder.encode(rango.firstdate, StandardCharsets.UTF_8.toString()),
			        URLEncoder.encode(rango.lastdate, StandardCharsets.UTF_8.toString()),
			        URLEncoder.encode(codigoSerie, StandardCharsets.UTF_8.toString())
			    );
								
				try {
					String salidaWebservice = call.callRestGet(queryParams);
					
					ParametrosFinancierosBancoCentral respuesta = Utils.getGsonBuilder().fromJson(salidaWebservice, ParametrosFinancierosBancoCentral.class);
					parametrosFinancierosBancoCentral.add(respuesta);

				} catch (Exception e) {
					logger.error("Error al consultar serie " + codigoSerie, e);			
				}	
			}
			
			logger.debug("parametros financieros:" + parametrosFinancierosBancoCentral);
			logger.info("parametros financieros:" + parametrosFinancierosBancoCentral);
			
			
			//Actualizar parametros financieros en modulo Tipos de cambio e indices
			try(PreparedStatement pstmtParametro = conn.prepareStatement(SQL_UPSERT_PARAMETRO_FINANCIERO); ){
				
				//obtener los parametros financieros diarios y agruparlos por fecha (ayer, hoy, manana)
				HashMap<String, BigDecimal> dolarObservado = new HashMap<>();
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
						.map(obsConSerie -> {
							/* almacenar el dolar observado */
							if (obsConSerie.seriesId.equalsIgnoreCase(CODIGO_DOLAR_OBSERVADO_WEBSERVICE)) {
					            // No corregir la fecha, usar tal cual
					            dolarObservado.put(obsConSerie.indexDateString, obsConSerie.valor);
					            obsConSerie.seriesId = CODIGO_DOLAR_OBSERVADO;
					        }
					        return obsConSerie;
					    })
						.collect(Collectors.groupingBy(param -> param.indexDateString));
				
				//objeto de entrada para la actualizacion de parametros financieros en mesa
				JsonArray datosParidadYTipoCambio = new JsonArray();
				
				//obtener los parametros financieros de ayer para actualizar en la fecha de sistema
				logger.debug("Actualizar tipos de cambio e indices en fecha de sistema");
				
				parametrosFinancierosDiarosPorFecha.get(fechaSistema).stream()
				.filter(param -> definicionPorSerie.containsKey(param.seriesId) && !CODIGO_UF.equalsIgnoreCase(param.seriesId))
				.forEach(parametroFinancieroAyer -> {
					
					// Obtener definicion y mapear a IdParametroFinanciero
					DefinicionParametroFinanciero def = definicionPorSerie.get(parametroFinancieroAyer.seriesId);
					String idParametro = (def != null && def.IdParametroFinanciero != null && !def.IdParametroFinanciero.isEmpty()) ? def.IdParametroFinanciero : parametroFinancieroAyer.seriesId;
										
					//calcular valor para monedas en con paridad USD
					BigDecimal valorDolarObservado = dolarObservado.getOrDefault(fechaSistema, BigDecimal.ZERO);
					if(def != null && monedasEnUsd.contains(def.IdMonedaParametro) && !valorDolarObservado.equals(BigDecimal.ZERO) &&  !parametroFinancieroAyer.valor.equals(BigDecimal.ZERO)) {
						parametroFinancieroAyer.valor = parametroFinancieroAyer.valor.divide(valorDolarObservado, 8, RoundingMode.FLOOR);
					}
									
					//si es codigo WS dolar observado US$, reemplazarlo por DO; en caso contrario no realizar cambio.
					parametroFinancieroAyer.seriesId = (parametroFinancieroAyer.seriesId!=null && CODIGO_DOLAR_OBSERVADO.equals(parametroFinancieroAyer.seriesId)) ? CODIGO_DOLAR_OBSERVADO : parametroFinancieroAyer.seriesId;										
					agregarActualizacionParidadesIndices(pstmtParametro, "DI", idParametro, fechaSistema, parametroFinancieroAyer.valor);					
					
					//Si es dolar observado DO  evaluar si es necesario cargar su valor en dias no habiles y cargar en spot
					if(CODIGO_DOLAR_OBSERVADO.equals(parametroFinancieroAyer.seriesId)) {
						actualizarEnDiasNoHabiles(pstmtParametro, "DI", idParametro, fechaSistema, fechaSistemaSiguiente, parametroFinancieroAyer.valor);
						//agregar al dia siguiente en spot
						BigDecimal valorParidadYTipoCambio = parametroFinancieroAyer.valor != null ? parametroFinancieroAyer.valor : BigDecimal.ZERO;
						String nombreMoneda = parametrosFinancierosMesa.getOrDefault(idParametro, idParametro);
						datosParidadYTipoCambio.add(formatJsonParidadTipoCambio(0, idParametro, nombreMoneda, valorParidadYTipoCambio));
						
					}
					//si es eur evaluar si es necesario cargar su valor en dias no habiles
					if(CODIGO_EUR.equals(parametroFinancieroAyer.seriesId)) {
						DefinicionParametroFinanciero defEur = definicionPorSerie.get(parametroFinancieroAyer.seriesId);
						String idParametroEur = defEur != null ? defEur.IdParametroFinanciero : parametroFinancieroAyer.seriesId;
						actualizarEnDiasNoHabiles(pstmtParametro, "DI", idParametroEur, fechaSistema, fechaSistemaSiguiente, parametroFinancieroAyer.valor);						
					}
				});
				    
				

				logger.debug("Actualizar UF en fecha sistema");
				List<ObsConSerie> obsFechaSistema = parametrosFinancierosDiarosPorFecha.getOrDefault(fechaSistema, new ArrayList<>());
				obsFechaSistema.stream()
				.filter(param -> param.seriesId != null && CODIGO_UF.equalsIgnoreCase(param.seriesId))
				.forEach(parametroFinancieroFechaSistema -> {
					DefinicionParametroFinanciero def = definicionPorSerie.get(parametroFinancieroFechaSistema.seriesId);
					String idParametro = def != null ? def.IdParametroFinanciero : parametroFinancieroFechaSistema.seriesId;
					agregarActualizacionParidadesIndices(pstmtParametro, "DI", idParametro, fechaSistema, parametroFinancieroFechaSistema.valor);
					String nombreMoneda = parametrosFinancierosMesa.getOrDefault(idParametro, idParametro);
					datosParidadYTipoCambio.add(formatJsonParidadTipoCambio(0, idParametro, nombreMoneda, parametroFinancieroFechaSistema.valor));
				});
				
								
				
				logger.debug("Actualizar UF en paridades y tipo de cambio dias no habiles");
				LocalDate fechaActual = LocalDate.parse(fechaSistema, DateTimeFormatter.ISO_DATE);
				LocalDate fechaSiguiente = LocalDate.parse(fechaSistemaSiguiente, DateTimeFormatter.ISO_DATE);
				
				if(fechaActual.until(fechaSiguiente).getDays()>1) {	
					logger.debug("HAY DIAS NO HABILES");
					fechaActual = fechaActual.plusDays(1);
					while(fechaActual.compareTo(fechaSiguiente)<0) {
						List<ObsConSerie> obsEnFecha = parametrosFinancierosDiarosPorFecha.getOrDefault(fechaActual.format(DateTimeFormatter.ISO_DATE), new ArrayList<>());
						if (obsEnFecha != null) {
							obsEnFecha.stream().filter(parametro -> parametro.seriesId != null && CODIGO_UF.equalsIgnoreCase(parametro.seriesId))
							.forEach(parametroFinanciero ->{
								DefinicionParametroFinanciero def = definicionPorSerie.get(parametroFinanciero.seriesId);
								String idParametro = def != null ? def.IdParametroFinanciero : parametroFinanciero.seriesId;
								agregarActualizacionParidadesIndices(pstmtParametro, "DI", idParametro, parametroFinanciero.indexDateString, parametroFinanciero.valor);	
							});
						}
						
						fechaActual = fechaActual.plusDays(1);
					}
				}
							
				logger.debug("Actualizar parámetros financieros mensuales en paridad e índices");
				parametrosFinancierosBancoCentral.stream()
				    .flatMap(param -> {
				        String seriesId = param.getSeries().getSeriesId();

				        // Validamos que el seriesId sea no nulo y termine en ".M" (mensual)
				        if (seriesId != null && seriesId.endsWith(".M")) {
				            return param.getSeries().getObs().stream()
				                .map(obs -> new ParametrosFinancierosBancoCentral.Series.Obs.ObsConSerie(
				                    obs.getIndexDateString(),
				                    obs.value,
				                    obs.getStatusCode(),
				                    seriesId
				                ));
				        } else {
				            return Stream.empty();
				        }
				    })
				    .forEach(parametroFinancieroMensual -> {
				    	DefinicionParametroFinanciero def = definicionPorSerie.get(parametroFinancieroMensual.seriesId);
				    	String idParametro = def != null ? def.IdParametroFinanciero : parametroFinancieroMensual.seriesId;
				        agregarActualizacionParidadesIndices(
				            pstmtParametro,
				            "ME", // Indica periodo mensual
				            idParametro,
				            IPC.equalsIgnoreCase(idParametro) ? parametroFinancieroMensual.indexDateString : fechaSistema,
				            parametroFinancieroMensual.valor
				        );
				    });

				
				
				logger.debug("Actualizar parámetros financieros anuales en paridad e índices");
				parametrosFinancierosBancoCentral.stream()
				    .flatMap(param -> {
				        String seriesId = param.getSeries().getSeriesId();

				        // Filtramos solo los seriesId anuales
				        if (seriesId != null && seriesId.endsWith(".A")) {
				            return param.getSeries().getObs().stream()
				                .map(obs -> new ParametrosFinancierosBancoCentral.Series.Obs.ObsConSerie(
				                    obs.getIndexDateString(),
				                    obs.value,
				                    obs.getStatusCode(),
				                    seriesId
				                ));
				        } else {
				            return Stream.<ParametrosFinancierosBancoCentral.Series.Obs.ObsConSerie>empty();
				        }
				    })
				    .forEach(parametroFinancieroAnual -> {
				    	DefinicionParametroFinanciero def = definicionPorSerie.get(parametroFinancieroAnual.seriesId);
				    	String idParametro = def != null ? def.IdParametroFinanciero : parametroFinancieroAnual.seriesId;
				        agregarActualizacionParidadesIndices(
				            pstmtParametro,
				            "AN", // Indica periodo anual
				            idParametro,
				            fechaSistema,
				            parametroFinancieroAnual.valor
				        );
				    });

				
				//actualizar parametros financieros.
				int[] res = pstmtParametro.executeBatch();
				logger.debug( String.format("Actualizacion tabla tipo de cambio e indices: %s", (Arrays.stream(res).filter(r -> r==PreparedStatement.EXECUTE_FAILED).count()==0) ? "OK" : "NOK") );				
				if(datosParidadYTipoCambio.size()>0) {
					//Actualizar las paridades en el modulo mesa con el uf y do para la siguiente fecha habil.
					// no utiliza p_OrigenJob en "S" porque ya se realizo el calculo de la siguiente fecha
					JsonObject dataSRVIn = new JsonObject();
					dataSRVIn.addProperty("p_IdEmpresa", idEmpresa);
					dataSRVIn.addProperty("p_IdUsuario", 0);
					dataSRVIn.addProperty("p_IdModulo", "PLA");
					dataSRVIn.addProperty("p_Fecha", fechaSistema);
					dataSRVIn.addProperty("p_OrigenJob", "N");
					dataSRVIn.add("p_DatosParidadYTipoCambio", datosParidadYTipoCambio);
					logger.debug("Entrada paridad y tipo de cambio: " + dataSRVIn);
					StoredProcedure kHashSrv=new StoredProcedure("BSCL.Mesa.BD.Servicios::SPT.ModificarParidadYTipoCambio");
					JsonObject dataSRVOut=kHashSrv.call(conn, dataSRVIn);
					logger.debug("Actualizacion Precios cambiarios y costo de fondo spot: " + ((Long.valueOf(0).equals( dataSRVOut.get("P_OUT_ERRORESCONTADOR").getAsLong() )) ? "OK" : "NOK" ));
				}
				AgregarLog(conn,fechaSistemaDebug, idEmpresa);		
			}
			
		}
		
		return true;	
	}
	
	private boolean actualizarEnDiasNoHabiles(PreparedStatement pstmtParametro, String periodo, String codigo, String fechaSistema, String fechaSistemaSiguiente, BigDecimal valor) {
		LocalDate fechaActual = LocalDate.parse(fechaSistema, DateTimeFormatter.ISO_DATE);
		LocalDate fechaSiguiente = LocalDate.parse(fechaSistemaSiguiente, DateTimeFormatter.ISO_DATE);
		//1 si es el dia siguiente
		//+1 quiere decir que hay dias no habiles.
		if(fechaActual.until(fechaSiguiente).getDays()>1) {	
			logger.debug("HAY DIAS NO HABILES");
			fechaActual = fechaActual.plusDays(1);
			while(fechaActual.compareTo(fechaSiguiente)<0) {
				agregarActualizacionParidadesIndices(pstmtParametro, periodo, codigo, fechaActual.format(DateTimeFormatter.ISO_DATE), valor);
				fechaActual = fechaActual.plusDays(1);
			}
		}
		return true;
	}
	

	private boolean agregarActualizacionParidadesIndices(PreparedStatement pstmtParametro, String periodo, String codigo, String fechaActualizacion, BigDecimal valor) {
		try {

			if (IPC.equalsIgnoreCase(codigo)) {
				DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd-MM-yyyy");
				DateTimeFormatter formatoFinal = DateTimeFormatter.ISO_DATE;
				fechaActualizacion = LocalDate.parse(fechaActualizacion, formato).format(formatoFinal);
			}
			
			java.util.Date fecha = ISO_8601_FORMAT.parse(fechaActualizacion);
			String agnoMes = "";
			String agno = "";
			
			//si es diario, solo almacena el campo fecha y deja vacio agnoMes y agno
			if(periodo.equals("ME")){
				//get year-month only
				agnoMes = AGNO_MES_FORMAT.format(fecha);
				fechaActualizacion = agnoMes+"-01";
			}else if(periodo.equals("AN")){
				//get year only
				agno = AGNO_FORMAT.format(fecha);
				fechaActualizacion = agno+"-01-01";
			}
			
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
		} catch (ParseException e) {
			logger.error(e);
			return false;
		}		
	}
	
		
	/**
	 * Metodo utilizado para construir la tabla de entrada para el servicio que actualiza las paridades en MESA
	 * @param idMoneda
	 * @param idTipoRegistro
	 * @param valor
	 * @return
	 */
	private JsonObject formatJsonParidadTipoCambio(int index,String idMoneda, String nombreMoneda, BigDecimal valor) {
		JsonObject tipoCambio = new JsonObject();
		tipoCambio.addProperty("IdParidadYTipoCambio", index);
		tipoCambio.addProperty("IdMoneda", idMoneda);
		tipoCambio.addProperty("NombreMoneda", nombreMoneda);
		tipoCambio.addProperty("ParidadMercado", 0);
		tipoCambio.addProperty("ParidadBID", 0);
		tipoCambio.addProperty("ParidadASK", 0);
		tipoCambio.addProperty("LimiteSuperiorParidad", 0);
		tipoCambio.addProperty("LimiteInferiorParidad", 0);
		tipoCambio.addProperty("ParidadSBIF", 0);
		tipoCambio.addProperty("TipoCambioMercado", valor);
		tipoCambio.addProperty("LimiteSuperiorTipoCambio", 0);
		tipoCambio.addProperty("LimiteInferiorTipoCambio", 0);
		tipoCambio.addProperty("PrecioSpotCompra", 0);
		tipoCambio.addProperty("PrecioSpotVenta", 0);
		tipoCambio.addProperty("PrecioSpotPromedio", 0);
		logger.debug(String.format("UPDATE SPOT: {Codigo: %s, TipoRegistro: %s, Valor: %f}", idMoneda, nombreMoneda, valor));
		return tipoCambio;
	}

	@Override
	public void onError(Throwable t) {
		logger.debug(":ACTUALIZARPARAMETROSFINANCIEROSWSBANCOCENTRAL:  ****** ERROR  *******");
		t.printStackTrace();
	}

	@Override
	public void pause() {

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
	
	public RangoFechas obtenerFechasConsulta(String codigoSerie, String fechaSistema, String fechaSistemaAnterior, String fechaSistemaSiguiente) {
		RangoFechas rango = new RangoFechas();
		
		if (CODIGO_IPC.equalsIgnoreCase(codigoSerie)) {
			LocalDate fechaSistemaDate  = LocalDate.parse(fechaSistema, DateTimeFormatter.ISO_DATE);
			rango.firstdate = fechaSistemaDate.minusMonths(1).withDayOfMonth(1).format(DateTimeFormatter.ISO_DATE);
			rango.lastdate = fechaSistema;
		} else {
			rango.firstdate = fechaSistemaAnterior;
			rango.lastdate = fechaSistemaSiguiente;
		}
		
		return rango;
	}
	
	//solo para test
	public static void main(String[] args) throws Throwable {
		
		logger.info("== START ==");
		try(Connection con = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")){
			ActualizarParametrosFinancierosBancoCentral apf = new ActualizarParametrosFinancierosBancoCentral();
			apf.logica(con, true, "2025-09-25", 1);
			String desdeFechaSistema = "2026-04-24";
			String hastaFechaSistema = "2026-04-24";
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
			//con.commit();
		}
		logger.debug("== END ==");
	}
}


