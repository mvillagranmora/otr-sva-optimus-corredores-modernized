package cl.bcs.plataforma.jobs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.nuam.StoredProcedure;
import cl.bcs.plataforma.jobs.ActualizarParametrosFinancieros.ParametrosFinancierosWebService.Cabecera.ConsultatbiepfResult;
import cl.bcs.plataforma.pla.AgregarLog;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente.FechasIn;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.CallRestService;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/** Job que actualiza los parametros financieros del sistema
 * UPD_PARAMS_FINANCIEROS_OPEN: Actualizar parametros financieros y paridades en apertura
 * UPD_PARAMS_FINANCIEROS_CLOSE: Actualizar parametros financieros y paridades en cierre
 * @author chernandez
 *
 */
public class ActualizarParametrosFinancieros extends BcsJob{
	
	private static final Logger logger = LogManager.getLogger(ActualizarParametrosFinancieros.class);
	private final String SQL_UPSERT_PARAMETRO_FINANCIERO = "UPSERT \"BSCL.Plataforma.BD::MER.ParametroFinanciero\"(\"IdParametroFinanciero\",\"Fecha\",\"AgnoMes\",\"Agno\",\"Valor\",\"FechaActualizacion\") VALUES(?,?,?,?,?,CURRENT_UTCTIMESTAMP) WHERE \"IdParametroFinanciero\"=? AND \"Fecha\"=? AND \"AgnoMes\"=? AND \"Agno\"=?";
	private final String SQL_SELECT_DEFINICION_PARAMETROS_FINANCIEROS = "SELECT \"IdParametroFinanciero\", \"Periodo\", \"IdMonedaParametro\", \"IdMonedaValor\" FROM \"BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero\"";
	private final String CODIGO_DOLAR_OBSERVADO_WEBSERVICE = "US$";
	private final String CODIGO_DOLAR_OBSERVADO = "DO";
	private final String CODIGO_UF = "UF";
	private final String CODIGO_EUR = "EUR";
	private final String CODIGO_IPC = "IPC";
	private HashMap<String,String> parametrosFinancierosMesa = null;
	
	private final SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat AGNO_MES_FORMAT = new SimpleDateFormat("yyyy-MM");
	private final SimpleDateFormat AGNO_FORMAT = new SimpleDateFormat("yyyy");
	
	public ActualizarParametrosFinancieros(){
		//Tipo de registro de paridad para codigos de moneda dolar.
		parametrosFinancierosMesa = new HashMap<>();
		parametrosFinancierosMesa.put("DO", "DOLAR OBSERVADO");
		parametrosFinancierosMesa.put("USDAC", "DOLAR_ACUERDO");
		parametrosFinancierosMesa.put("USDAP", "DOLAR_APE_BCHILE");
		parametrosFinancierosMesa.put("USDBC", "DOLAR_APE_12_BCHILE");
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
		SQL sql = new SQL();
		
		//obtener lsita de parametros financieros que seran actualizados
		List<DefinicionParametroFinanciero> listaDefinicionParametrosFinancieros = sql.query(conn, SQL_SELECT_DEFINICION_PARAMETROS_FINANCIEROS, new HashMap<>(), DefinicionParametroFinanciero.class);
		Map<String, Object> definicionParametrosFinancieros = Utils.List2Hash(listaDefinicionParametrosFinancieros, "IdParametroFinanciero");
		
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
			String servidor = configuraciones.get("PLATAFORMA").get("MER.ServidorTec");
			String urlWebservice = configuraciones.get("PLATAFORMA").get("MER.UrlServicioParidades");
			String user = configuraciones.get("PLATAFORMA").get("MER.UsuarioServidorTec");
			String passwd = configuraciones.get("PLATAFORMA").get("MER.ClaveServidorTec");
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
			
			JsonArray entradaWebservice = new JsonArray();
			//Periodos disponibles para consultar en el WS
			String[] periodos = new String[] { "DI","ME","AN"};
			for (int i = 0; i < periodos.length; i++) {
				JsonObject periodo = new JsonObject();
				periodo.addProperty("id", i);
				periodo.addProperty("fechaini", fechaSistemaAnterior);
				periodo.addProperty("fechafin", fechaSistemaSiguiente);
				periodo.addProperty("periodo", periodos[i]); 
				periodo.addProperty("codigo", "?");
				entradaWebservice.add(periodo);
			}
			
			//realizar llamada rest para obtener los parametros financieros y paridades tipo de cambio desde bursatil.
			CallRestService call = new CallRestService(servidor, urlWebservice, user, passwd, auth, debug);
			String salidaWebservice = call.callRestPost(entradaWebservice.toString());
			List<ParametrosFinancierosWebService> parametrosFinancierosWebservice = Arrays.asList(Utils.getGsonBuilder().fromJson(salidaWebservice, ParametrosFinancierosWebService[].class));
			logger.debug("parametros financieros:" + parametrosFinancierosWebservice);
			
			//Actualizar parametros financieros en modulo Tipos de cambio e indices
			try(PreparedStatement pstmtParametro = conn.prepareStatement(SQL_UPSERT_PARAMETRO_FINANCIERO); ){
				
				//obtener los parametros financieros diarios y agruparlos por fecha (ayer, hoy, manana)
				HashMap<String, BigDecimal> dolarObservado = new HashMap<>();
				Map<String, List<ConsultatbiepfResult>> parametrosFinancierosDiarosPorFecha = parametrosFinancierosWebservice.stream()
						.filter(param -> param.id.equals(0L))
						.map(param -> param.getCabecera().getConsultatbiepfResult())
						.findFirst()
						.get()
						.stream()
						.map(parametroFinanciero -> {
							/* almacenar el dolar observado
							 * corregir fecha porque en el servicio el dolar observado viene con un dia adelantado
							 */
							if(parametroFinanciero.codigo.equalsIgnoreCase(CODIGO_DOLAR_OBSERVADO_WEBSERVICE)) {
								String fechaDolarCorregida = parametroFinanciero.fecha;
								if(fechaSistemaAnterior.equals(parametroFinanciero.fecha)) {
									fechaDolarCorregida = fechaSistema;
								} else if (fechaSistema.equals(parametroFinanciero.fecha)) {
									fechaDolarCorregida = fechaSistemaSiguiente;
								}
								//si no es igual a la siguiente fecha de sistema guardar dolar observado
								if(!fechaSistemaSiguiente.equals(parametroFinanciero.fecha)) {
									dolarObservado.put(fechaDolarCorregida, parametroFinanciero.valor);	
								}
								parametroFinanciero.codigo = CODIGO_DOLAR_OBSERVADO;
								
							}
							return parametroFinanciero;
						})
						.collect(Collectors.groupingBy(param -> param.fecha));
				
				//objeto de entrada para la actualizacion de parametros financieros en mesa
				JsonArray datosParidadYTipoCambio = new JsonArray();
				
				//obtener los parametros financieros de ayer para actualizar en la fecha de sistema
				logger.debug("Actualizar tipos de cambio e indices en fecha de sistema");
				parametrosFinancierosDiarosPorFecha.get(fechaSistemaAnterior).stream()
				.filter(param -> definicionParametrosFinancieros.containsKey(param.codigo) && !CODIGO_UF.equalsIgnoreCase(param.codigo))
				.forEach(parametroFinancieroAyer -> {
					//calcular valor para monedas en con paridad USD
					BigDecimal valorDolarObservado = dolarObservado.getOrDefault(fechaSistema, BigDecimal.ZERO);
					if(monedasEnUsd.contains(parametroFinancieroAyer.codigo) && !valorDolarObservado.equals(BigDecimal.ZERO) &&  !parametroFinancieroAyer.valor.equals(BigDecimal.ZERO)) {
						parametroFinancieroAyer.valor = parametroFinancieroAyer.valor.divide(valorDolarObservado, 8, RoundingMode.FLOOR);
					}
					//si es codigo WS dolar observado US$, reemplazarlo por DO; en caso contrario no realizar cambio.
					parametroFinancieroAyer.codigo = (parametroFinancieroAyer.codigo!=null && CODIGO_DOLAR_OBSERVADO.equals(parametroFinancieroAyer.codigo)) ? CODIGO_DOLAR_OBSERVADO : parametroFinancieroAyer.codigo;
					agregarActualizacionParidadesIndices(pstmtParametro, periodos[0], parametroFinancieroAyer.codigo, fechaSistema, parametroFinancieroAyer.valor);
					
					//Si es dolar observado DO  evaluar si es necesario cargar su valor en dias no habiles y cargar en spot
					if(CODIGO_DOLAR_OBSERVADO.equals(parametroFinancieroAyer.codigo)) {
						actualizarEnDiasNoHabiles(pstmtParametro, periodos[0], parametroFinancieroAyer.codigo, fechaSistema, fechaSistemaSiguiente, parametroFinancieroAyer.valor);
						//agregar al dia siguiente en spot
						BigDecimal valorParidadYTipoCambio = parametroFinancieroAyer.valor != null ? parametroFinancieroAyer.valor : BigDecimal.ZERO;
						datosParidadYTipoCambio.add(formatJsonParidadTipoCambio(0, parametroFinancieroAyer.codigo, parametrosFinancierosMesa.get(parametroFinancieroAyer.codigo), valorParidadYTipoCambio));
					}
					//si es eur evaluar si es necesario cargar su valor en dias no habiles
					if(CODIGO_EUR.equals(parametroFinancieroAyer.codigo)) {
						actualizarEnDiasNoHabiles(pstmtParametro, periodos[0], parametroFinancieroAyer.codigo, fechaSistema, fechaSistemaSiguiente, parametroFinancieroAyer.valor);
					}
				});
				

				logger.debug("Actualizar UF en fecha sistema");
				parametrosFinancierosDiarosPorFecha.get(fechaSistema).stream()
				.filter(param -> CODIGO_UF.equals(param.codigo.toUpperCase()))
				.forEach(parametroFinancieroFechaSistema -> {
					agregarActualizacionParidadesIndices(pstmtParametro, periodos[0], parametroFinancieroFechaSistema.codigo, fechaSistema, parametroFinancieroFechaSistema.valor);
					datosParidadYTipoCambio.add(formatJsonParidadTipoCambio(0, CODIGO_UF, parametrosFinancierosMesa.get(parametroFinancieroFechaSistema.codigo), parametroFinancieroFechaSistema.valor));
				});
				
				
				logger.debug("Actualizar UF en paridades y tipo de cambio dias no habiles");
				LocalDate fechaActual = LocalDate.parse(fechaSistema, DateTimeFormatter.ISO_DATE);
				LocalDate fechaSiguiente = LocalDate.parse(fechaSistemaSiguiente, DateTimeFormatter.ISO_DATE);
				if(fechaActual.until(fechaSiguiente).getDays()>1) {	
					logger.debug("HAY DIAS NO HABILES");
					fechaActual = fechaActual.plusDays(1);
					while(fechaActual.compareTo(fechaSiguiente)<0) {
						if (parametrosFinancierosDiarosPorFecha.get(fechaActual.format(DateTimeFormatter.ISO_DATE)) != null) {
							parametrosFinancierosDiarosPorFecha.get(fechaActual.format(DateTimeFormatter.ISO_DATE)).stream().filter(parametro -> CODIGO_UF.equalsIgnoreCase(parametro.codigo))
							.forEach(parametroFinanciero ->{
								agregarActualizacionParidadesIndices(pstmtParametro, periodos[0], parametroFinanciero.codigo, parametroFinanciero.fecha, parametroFinanciero.valor);	
							});
						}
						
						fechaActual = fechaActual.plusDays(1);
					}
				}
				
				
				logger.debug("Actualizar parametros financieros mensuales en paridad e indices");
				parametrosFinancierosWebservice.stream()
						.filter(param -> param.id.equals(1L))
						.map(param -> param.getCabecera().getConsultatbiepfResult())
						.findFirst()
						.get()
						.stream()
						.filter(parametroMensual -> {
							try {
								if (CODIGO_IPC.equalsIgnoreCase(parametroMensual.codigo) && parametroMensual.valor.compareTo(BigDecimal.ZERO) != 0)
									return true;
								//en caso que se este consultado fecha sistema fin de mes.
								return AGNO_MES_FORMAT.parse(parametroMensual.fecha).equals(AGNO_MES_FORMAT.parse(fechaSistema)) && !CODIGO_IPC.equalsIgnoreCase(parametroMensual.codigo);
							} catch (ParseException e) {
								e.printStackTrace();
							}
							return false;
						})
						.forEach(parametroFinancieroMensual ->{
							String fechaActualizacion = "";
							if (CODIGO_IPC.equalsIgnoreCase(parametroFinancieroMensual.codigo)) {
								fechaActualizacion = corregirFechaIPC(parametroFinancieroMensual.fecha);
							} else {
								fechaActualizacion = fechaSistema;
							}
							agregarActualizacionParidadesIndices(pstmtParametro, periodos[1], parametroFinancieroMensual.codigo, fechaActualizacion, parametroFinancieroMensual.valor);
						});
				
				
				logger.debug("Actualizar parametros financieros anuales en paridad e indices");
				parametrosFinancierosWebservice.stream()
						.filter(param -> param.id.equals(2L))
						.map(param -> param.getCabecera().getConsultatbiepfResult())
						.findFirst()
						.get()
						.forEach(parametroFinancuieroAnual -> {
							agregarActualizacionParidadesIndices(pstmtParametro, periodos[2], parametroFinancuieroAnual.codigo, fechaSistema, parametroFinancuieroAnual.valor);
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
					logger.debug("Entrada paridad y tipo de camibio: " + dataSRVIn);
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
	
	private String corregirFechaIPC(String fecha) {
		//bursatil entrega la fecha del ipc con un mes adicional
	    try {
	    	LocalDate fechaSistemaDate  = LocalDate.parse(fecha, DateTimeFormatter.ISO_DATE);
	        String fechaCorregida = fechaSistemaDate.minusMonths(1).withDayOfMonth(1).format(DateTimeFormatter.ISO_DATE);
	        return fechaCorregida;
	    } catch (DateTimeParseException e) {
	        e.printStackTrace();
	        return fecha;
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
		logger.debug(":ACTUALIZARPARAMETROSFINANCIEROS:  ****** ERROR  *******");
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
		try(Connection con = PlataformaUtils.getMerrillConnection()){
			ActualizarParametrosFinancieros apf = new ActualizarParametrosFinancieros();
			apf.logica(con, false, "", 1);
			String desdeFechaSistema = "2025-08-29";
			String hastaFechaSistema = "2025-08-29";
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


