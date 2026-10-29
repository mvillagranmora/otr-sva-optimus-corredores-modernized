package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;
import java.time.YearMonth;


import cl.bcs.custodia.CargarArchivoCircularCarteraCliente;
import cl.bcs.custodia.CargarArchivoCircularCarteraCliente.EntradaCargarArchivoCircularCarteraCliente;
import cl.bcs.custodia.CargarArchivoCircularCarteraCliente.SalidaCargarArchivoCircularCarteraCliente;
import cl.bcs.normativa.ValorizarNCG499;
import cl.bcs.normativa.ValorizarNCG499.ValorizarNCGEntrada;
import cl.bcs.normativa.ValorizarNCG499.ValorizarNCGSalida;
import cl.bcs.plataforma.jobs.BcsProcesoPauta.SalidaProcesoPauta;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ObtenerFeriado;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.ObtenerFeriado.ParametrosEntradaServicio;
import cl.bcs.plataforma.pla.utils.ObtenerFeriado.ParametrosSalidaServicio;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class PautaCustodiaCarteraCircular implements BcsProcesoPauta {

	public static class DatosNCG{
		//String FechaInforme;
		public Long 		IdValorizacion;
		public String 		LinDet;
		public String 		Producto;
		public String 		Mercado;
		public String 		FechaInicio;
		public String 		FechaVencimiento;
		public Long 		Plazo;
		public String 		RutCliente;
		public String 		RutEmisor;
		public String 		MonedaEmisor;
		public String 		MonedaPacto;
		public BigDecimal 	Tasatransferencia;
		public BigDecimal 	TasaTransaccion;
		public BigDecimal 	TasaOperacion;
		public String 		BASTAS;
		public String 		Sucursal;
		public BigDecimal 	MontoCompraVenta;
		public BigDecimal 	Reajuste;
		public BigDecimal 	MontoTransaccion;
		public BigDecimal 	Comision;
		public BigDecimal 	UtilidaPerdidaDiferenciaPrecio;
		public BigDecimal 	PrecioCompraVenta;
		public BigDecimal 	UtilidadPerdidaOperacionCambio;
		public BigDecimal 	UtilidadPerdidaSimultanea;
		
		public BigDecimal	ValMontoMonedaInstrumento;
		public BigDecimal 	ValMontoMonedaSistema;
		
		public BigDecimal 	ValMontoMonedaInstrumentoFechaVenta;
		public BigDecimal 	ValMontoMonedaSistemaFechaVenta;
		
		public BigDecimal 	ValMontoDevengadoAMonedaInstrumento;
		public BigDecimal 	ValMontoDevengadoAMonedaSistema;
		
		public String MensajeErrorFuente;
		
		public Long					IdInstrumento; 
		public String				IdClasificacionTipoInstrumento; 
		public String				IdTipoInstrumento; 
		public String				Nemotecnico; 
		public String				InsFechaVencimiento;
		public String				InsFechaEmision;
		public String				TieneCorteDeCupon;
		public String				IdMonedaInstrumento;
		public String				IdMonedaEmisionInstrumento;
		public String				IdTipoReajusteInstrumento;
		public String				Emisor;
		public String				IdCondicionDeLiquidacion;
		public String				FechaDeLiquidacion;	
		public BigDecimal			Cantidad; 
		//public BigDecimal			ValorRescate;		
		public BigDecimal			MontoCompra; // En moneda del instrumento ( se usa para el calculo de interes)
		public BigDecimal			MontoCompraMonedaSistema;
		public BigDecimal			MontoMercadoMonedaSistema;
		public BigDecimal			MontoCompraSubyacente;
		public BigDecimal			MontoCompraGarantia;
		public String				IdTipoOperacion;
		
		//public BigDecimal			PrecioCompra; 
		public BigDecimal			TasaCompra;
		public BigDecimal			PrecioMercado; 
		public BigDecimal			TasaMercado;		
		public BigDecimal			PrecioSubyacente; 
		public BigDecimal			TasaSubyacente; 
		public BigDecimal			PrecioGarantia; 
		public BigDecimal			TasaGarantia;
		public BigDecimal			TasaTIF;
		public BigDecimal			TasaCorredora;
		public String				FechaOperacion;
		public String				FechaValorizacion;
		public String			    FechaVencimientoOperacion;
		public String				Situacion;
		
		public String toString() {
			return Utils.printJson(this, true);
		}
		
		public DatosNCG() {};
	}

	public static class RespaldoNCG {
		public Long Respaldo;
	}

	public static class FechaNCG {
		public String Fecha;
	}

	private static FileLoader loader = new FileLoader();
	private static Log log = new Log("PLA.PautaCircularCarteraCliente");
	private static String SQL_SELECT_NCG = loader.loadResourceToString("cl/bcs/plataforma/jobs/sql/PautaCircularCarteraCliente_Consulta_Respaldo_NCG499.sql");
	private static String SQL_DELETE_NCG = loader.loadResourceToString("cl/bcs/plataforma/jobs/sql/PautaCircularCarteraCliente_Eliminar_Respaldo_NCG499.sql");   
	
	public static boolean esUltimoDiaMes(LocalDate fecha) {
		YearMonth yearMonth = YearMonth.from(fecha);
        LocalDate ultimoDiaDelMes = yearMonth.atEndOfMonth();
        
        return fecha.equals(ultimoDiaDelMes);
	}

	public static Boolean esDiaHabil(String fechaConsulta,Long idEmpresa, Connection con) throws Exception {
			//logger.debug("INICIO::esDiaHabil");
			log.write("Función es día hábil");
			Long inicioMetodo = System.currentTimeMillis();
			
			ObtenerFeriado obtenerFeriadoService = new ObtenerFeriado();
			ParametrosEntradaServicio inObtenerFeriado = new ParametrosEntradaServicio();
			inObtenerFeriado.fechaConsulta = fechaConsulta;
			inObtenerFeriado.IdEmpresa = idEmpresa;
			ParametrosSalidaServicio outObtenerFeriado = obtenerFeriadoService.logica(con, inObtenerFeriado);
			Long finMetodo = System.currentTimeMillis();
			//logger.debug("FIN::esDiaHabil", finMetodo - inicioMetodo);
			log.write("retorno de es día hábil: "+outObtenerFeriado.habil);
			return outObtenerFeriado.habil;
		
	}

	public static boolean esPrimerDiaHabil(LocalDate fecha, Long idEmpresa, Connection con) throws Exception {
		// Obtenemos el primer día del mes
		log.write("Función es primer día hábil");
		LocalDate primerDiaDelMes = fecha.with(TemporalAdjusters.firstDayOfMonth());
		
		// Iteramos a partir del primer día del mes hasta encontrar un día hábil
		LocalDate dia = primerDiaDelMes;
		while (!esDiaHabil(dia.toString(), idEmpresa, con)) {
			dia = dia.plusDays(1); // Avanzamos al siguiente día
		}
		
		// Si el día encontrado es igual a la fecha proporcionada, entonces es el primer día hábil del mes
		log.write("Retorno función primer día hábil: "+dia);
		return fecha.equals(dia);
	}

	public static boolean esUltimoDiaHabil(LocalDate fecha, Long idEmpresa, Connection con) throws Exception {
		// Obtenemos el último día del mes
		log.write("Función es último día hábil");
		LocalDate ultimoDiaDelMes = fecha.with(TemporalAdjusters.lastDayOfMonth());

		// Iteramos a partir del último día del mes hasta encontrar un día hábil
		LocalDate dia = ultimoDiaDelMes;
		while (!esDiaHabil(dia.toString(), idEmpresa, con)) {
			dia = dia.minusDays(1); // Retrocedemos al día anterior
		}

		// Si el día encontrado es igual a la fecha proporcionada, entonces es el último día hábil del mes
		log.write("Retorno función último día hábil: " + dia);
		return fecha.equals(dia);
	}

	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws Throwable {
		log.write("***** Inicio PautaCircularCarteraCliente *****");
		SalidaProcesoPauta salida = new SalidaProcesoPauta();
		
		try {

			SQL sql = new SQL();
			
			String confFechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"PLATAFORMA","PLA.FechaSistema");
			
			ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
			InformacionEmpresa empresa = consultarEmpresaService.logica(con, (Long)parameters.get("IdEmpresa"));
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			
			EntradaCargarArchivoCircularCarteraCliente entradaCargarArchivoCircularCarteraCliente = new EntradaCargarArchivoCircularCarteraCliente();
			entradaCargarArchivoCircularCarteraCliente.p_IdEmpresa =(Long)parameters.get("IdEmpresa");
			entradaCargarArchivoCircularCarteraCliente.p_IdUsuario =(Long)parameters.get("IdUsuario");
			entradaCargarArchivoCircularCarteraCliente.p_IdModulo = "CUS";
			entradaCargarArchivoCircularCarteraCliente.p_FechaCuadratura = (String)parameters.get("FechaSistema");
			
			Boolean esDiaHabil = true;
			Long idEmpresa = (Long)parameters.get("IdEmpresa");
			String zonaHorariaStgo = "America/Santiago";
			String fechaInhabil = "0001-01-01";
			LocalDate fechaSistema = LocalDate.parse(entradaCargarArchivoCircularCarteraCliente.p_FechaCuadratura, formatter);  
			LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of(zonaHorariaStgo));
			LocalDate currentDate = currentDateTime.toLocalDate();
			Long numeroRespaldo = 0L;
			Long numeroRespaldoInhabil = 0L;
			

			//LocalDate currentDate = LocalDate.parse("2024-03-28"); // Habilitar para test
			
			log.write("Zona Horaria Default Servidor: " + ZoneId.systemDefault());
			log.write("Zona Horaria Utilizada: " + zonaHorariaStgo);
			log.write("Current DateTime: " + currentDateTime);
			log.write("Current Date: " + currentDate);
			log.write("Fecha Sistema: " + fechaSistema);

			CargarArchivoCircularCarteraCliente CargarArchivoCircularCarteraCliente = new CargarArchivoCircularCarteraCliente();

		
			SQL _sql = new SQL();

			//si es primer día hábil del mes, borra los registros para evitar duplicidad
			log.write("Consulta primer día hábil");
			FechaNCG fechaNCG = new FechaNCG();
			fechaNCG.Fecha = LocalDate.parse(entradaCargarArchivoCircularCarteraCliente.p_FechaCuadratura, formatter).toString(); 
			
			log.write("Consulta primer día hábil: "+fechaNCG.Fecha);

			if(esPrimerDiaHabil(fechaSistema, idEmpresa, con)){
				log.write("Borrar Archivos duplicados");
				_sql.batch(con, SQL_DELETE_NCG, Arrays.asList(fechaNCG), FechaNCG.class);
			}

			//verifica que no exista respaldo
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("p_Fecha", LocalDate.parse(entradaCargarArchivoCircularCarteraCliente.p_FechaCuadratura, formatter).toString());

			log.write("Verificar que no hay respaldo");
			List<DatosNCG> data = sql.query(con, SQL_SELECT_NCG, params, DatosNCG.class);

			if(data.size() == 0) {
				log.write("No hay de registros respaldados");
			}else{
				log.write("Registros respaldados: "+data.size());
			}
			
			// proceso de respaldo dia hábil
			if(data.size() == 0){
				log.write("***** RUN DIA HABIL*****");
				SalidaCargarArchivoCircularCarteraCliente salidaProcesarCargaMasiva = CargarArchivoCircularCarteraCliente.logica(con, entradaCargarArchivoCircularCarteraCliente);
				salida.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(salidaProcesarCargaMasiva.P_OUT_MENSAJES);
				salida.P_OUT_ERRORESCONTADOR = salidaProcesarCargaMasiva.P_OUT_ERRORESCONTADOR;

			}else{
					log.write("***** PROCESO FINALIZADO *****");
					salida.P_OUT_MENSAJE = "PROCESO FINALIZADO";
					salida.P_OUT_ERRORESCONTADOR = 0L;
			}


			//Proceso para respaldo de fin de mes en día inhábil.
			if ((esUltimoDiaHabil(fechaSistema, idEmpresa, con) && !esUltimoDiaMes(fechaSistema)) || esPrimerDiaHabil(fechaSistema, idEmpresa, con) || esUltimoDiaMes(fechaSistema)) {
				log.write("***** Calcula fecha inhabil *****");
				LocalDate ultimoDiaMesAnterior =  LocalDate.now();


				if(esPrimerDiaHabil(fechaSistema, idEmpresa, con)){
					ultimoDiaMesAnterior = fechaSistema.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
					fechaInhabil =  ultimoDiaMesAnterior.format(formatter);
				}else if(esUltimoDiaHabil(fechaSistema, idEmpresa, con) && !esUltimoDiaMes(fechaSistema)){
					ultimoDiaMesAnterior = fechaSistema.plusDays(1).with(TemporalAdjusters.lastDayOfMonth());
					fechaInhabil =  ultimoDiaMesAnterior.format(formatter);
				}else{
					ultimoDiaMesAnterior = fechaSistema;
					fechaInhabil = fechaSistema.toString();
				}

				log.write("Fecha dia inhabil"+ fechaInhabil);
				log.write("Fecha ultimo día"+ ultimoDiaMesAnterior.toString());

				HashMap<String, Object> paramsInhabil = new HashMap<String, Object>();
				paramsInhabil.put("p_Fecha", fechaInhabil);

				List<DatosNCG> dataInhabil = sql.query(con, SQL_SELECT_NCG, paramsInhabil, DatosNCG.class);

				if(dataInhabil.size() == 0) {
					log.write("No hay de registros respaldados");
				}else{
					log.write("Registros respaldados: "+dataInhabil.size());
				}


				log.write("***** Fecha inhabil: " + fechaInhabil + " *****");

				if (!esDiaHabil(ultimoDiaMesAnterior.toString(), idEmpresa, con) && dataInhabil.size() == 0) {
					log.write("***** RUN DIA INHABIL FIN DE MES *****");
					entradaCargarArchivoCircularCarteraCliente.p_FechaCuadratura = fechaInhabil;
					SalidaCargarArchivoCircularCarteraCliente salidaProcesarCargaMasiva = CargarArchivoCircularCarteraCliente.logica(con, entradaCargarArchivoCircularCarteraCliente);
					
					salida.P_OUT_MENSAJE = PlataformaUtils.concatenarMensajesSalida(salidaProcesarCargaMasiva.P_OUT_MENSAJES);
					salida.P_OUT_ERRORESCONTADOR = salidaProcesarCargaMasiva.P_OUT_ERRORESCONTADOR;

					log.write("***** VALORIZA DIA INHABIL FIN DE MES *****");
					ValorizarNCG499 valorizarNcg = new ValorizarNCG499();
					
					ValorizarNCGEntrada valorizaEntrada = new ValorizarNCGEntrada();
					valorizaEntrada.p_IdEmpresa = (Long)parameters.get("IdEmpresa");
					valorizaEntrada.p_IdUsuario =(Long)parameters.get("IdUsuario");
					valorizaEntrada.p_IdPais = 0L;
					valorizaEntrada.p_IdModulo = "CUS";
					valorizaEntrada.p_Fecha = fechaInhabil;
					ValorizarNCGSalida salidaValorizaNcg = valorizarNcg.logica(con, valorizaEntrada);
					
					log.write("***** FIN VALORIZA DIA INHABIL FIN DE MES *****");
					
					salida.P_OUT_MENSAJE = salidaValorizaNcg.P_OUT_MENSAJE;
					salida.P_OUT_ERRORESCONTADOR = salidaValorizaNcg.P_OUT_ERRORESCONTADOR;					
					
				}else{
					log.write("***** PROCESO FINALIZADO *****");
					salida.P_OUT_MENSAJE = "PROCESO FINALIZADO";
					salida.P_OUT_ERRORESCONTADOR = 0L;
				}
			}
		
			
		} catch(Exception e) {
			log.write(e);
    		log.write("***** Error PautaCircularCarteraCliente *****");
    		throw e;
		}
		log.write("***** Fin PautaCircularCarteraCliente *****");
		return salida;
	} 
	
	public static void main(String[] args) throws Throwable {
		System.out.println("== TEST ==");
		
		 try (Connection con =  PlataformaUtils.getPropertiesConnection("desa01","connections.properties")) {  
			Map<String, Object> parameters = new HashMap<>();
	        parameters.put("IdEmpresa", 1L);
	        parameters.put("IdUsuario", 4L);
	        parameters.put("IdModulo", "CUS");
	        parameters.put("FechaSistema", "2024-10-30");
	        
	        try {
		        PautaCustodiaCarteraCircular pauta = new PautaCustodiaCarteraCircular();
		        SalidaProcesoPauta salida = pauta.transaction(con, parameters);

		        System.out.println("P_OUT_MENSAJE: " + salida.P_OUT_MENSAJE);
		        System.out.println("P_OUT_ERRORESCONTADOR: " + salida.P_OUT_ERRORESCONTADOR);
		        con.commit();
		        
	        } catch (Exception er) {
	        	er.printStackTrace();
	        	con.rollback();
	        }
		}
		
        System.out.println("== END ==");
	}
	
}
