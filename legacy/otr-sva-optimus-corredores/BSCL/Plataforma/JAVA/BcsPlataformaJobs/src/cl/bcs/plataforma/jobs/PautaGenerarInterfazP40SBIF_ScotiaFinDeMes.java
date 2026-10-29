package cl.bcs.plataforma.jobs;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.EntradaGenerarInterfazP40SBIF;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF.SalidaGenerarInterfazP40SBIF;
import cl.bcs.normativa.interfaces.ClienteConfig;
import cl.bcs.normativa.interfaces.ClienteConfigFactory;
import cl.bcs.normativa.interfaces.GenerarInterfazP40SBIF_Scotia;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.EntradaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.ParametroJenkins;
import cl.bcs.plataforma.pla.EjecutarTareaJenkins.SalidaEjecutarTareaJenkins;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PautaGenerarInterfazP40SBIF_ScotiaFinDeMes extends BcsJob implements BcsProcesoPauta {

	private static FileLoader loader = new FileLoader();
	static final Logger logger = LogManager.getLogger(PautaGenerarInterfazP40SBIF_ScotiaFinDeMes.class);
	
	private final String SQL_CODIGO_CORREDORA = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/BcsJobGenerarInterfazP40_SelectCorredoraCodigos.sql");
	private String SUBCARPETA = "NOR/"; 
	
	public static class DatoCodigoCorredora {
		public String 		CodigoCorredora;
	}
	
	@Override
	public SalidaProcesoPauta transaction(Connection con, Map<String, Object> parameters) throws InterruptedException, Throwable {
		logger.info("***** Inicio PautaGenerarInterfazP40SBIF_ScotiaFinDeMes *****");
		SimpleDateFormat formato 		= new SimpleDateFormat("yyyy-MM-dd");
		DateTimeFormatter formatter 	= DateTimeFormatter.ofPattern("yyyy-MM-dd");
		SQL sql 						= new SQL();		
		SalidaProcesoPauta salida 		= new SalidaProcesoPauta();
		salida.P_OUT_ERRORESCONTADOR 	= 0L;
		
		try {
			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString( con , Long.parseLong(this.getParam("IdEmpresa")), "PLATAFORMA", "PLA.FechaSistema");
			
            Date FechaSistemaDate			= formato.parse(fechaSistema); 
            LocalDate FechaSistemaLD 		= FechaSistemaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate fechaMenosUnMes 		= FechaSistemaLD.minusMonths(1);
            LocalDate FinMesAnterior 		= fechaMenosUnMes.withDayOfMonth(fechaMenosUnMes.lengthOfMonth());
            LocalDate DiaAnteriorFinMes		= FinMesAnterior.minusDays(1L);
       
			ObtenerDiaSiguiente 		DS 	= new ObtenerDiaSiguiente	();
			InObtenerDiaSiguiente entradaDS = new InObtenerDiaSiguiente	();				 
			
			entradaDS.p_idPais 				= new Long(44);
			entradaDS.p_diaHabil 			= "S";
			entradaDS.p_soloCaeDiaHabil 	= "S";
			
			InObtenerDiaSiguiente.FechasIn fechasInSig = new InObtenerDiaSiguiente.FechasIn();
			fechasInSig.numeroDias 		=  1L;	
			
			fechasInSig.fechaInicial = DiaAnteriorFinMes.format(formatter);				
			entradaDS.p_fechas.add(fechasInSig);
			
			OutObtenerDiaSiguiente SalidaDiaSiguiente = DS.logica(con, entradaDS);			
			String FechaCalculadaADS 		= SalidaDiaSiguiente.p_out_fechas.get(0).fechaCalculada ;
			
			if (!FechaCalculadaADS.equalsIgnoreCase(fechaSistema)) { // 
				logger.info("El proceso PautaGenerarInterfazP40SBIF_ScotiaFinDeMes No ejecutara la interfaz p40.");
			}else {
			HashMap<String, Object> paramsRut = new HashMap<String, Object>();
			paramsRut.put("IdEmpresa", (Long)parameters.get("IdEmpresa"));
			List<DatoCodigoCorredora> datoCodigo = sql.query(con, SQL_CODIGO_CORREDORA, paramsRut, DatoCodigoCorredora.class);
			            
			EntradaGenerarInterfazP40SBIF entrada = new EntradaGenerarInterfazP40SBIF();
			String rutaArchivo = Utils.getConfiguracion(con, Utils.getSchema(), (Long)parameters.get("IdEmpresa"), "PLA.RutaBaseArchivos");
			SUBCARPETA = ConfiguracionSistema.getParametroConfiguracionString(con,(Long)parameters.get("IdEmpresa"),"NORMATIVA","SubRutaArchivosModulo"); 
			//Se configura una subcarpeta para alojar los archivos.
			rutaArchivo = rutaArchivo.concat(SUBCARPETA);
			
			//Nota: la clase ClienteConfig configura a la entradap40 según el código de corredora que se envía, no se necesitan todos los parametros de entrada
			ClienteConfig clienteConfig = ClienteConfigFactory.getClienteConfig(datoCodigo.get(0).CodigoCorredora);
			clienteConfig.configurarParametros(entrada, parameters.get("IdEmpresa").toString(), parameters.get("IdUsuario").toString(), rutaArchivo);			
    		entrada.p_Fecha = FinMesAnterior.format(formatter);
			GenerarInterfazP40SBIF_Scotia generarInterfazP40SBIF_Scotia = new GenerarInterfazP40SBIF_Scotia();
			SalidaGenerarInterfazP40SBIF resp = generarInterfazP40SBIF_Scotia.logica(con, entrada);
			salida.P_OUT_ERRORESCONTADOR += resp.P_OUT_ERRORESCONTADOR;
			
			}
			
			

		} catch (Exception e) {
			salida.P_OUT_ERRORESCONTADOR += 1;
			logger.error(e.getMessage());
			logger.info("***** Error PautaGenerarInterfazP40SBIF_ScotiaFinDeMes *****");
    		throw e;
		}finally {
			if(salida.P_OUT_ERRORESCONTADOR == 0) {				
				salida.P_OUT_MENSAJE = "El proceso PautaGenerarInterfazP40SBIF_ScotiaFinDeMes se ha ejecutado correctamente";
			}else {
				salida.P_OUT_MENSAJE = "El proceso PautaGenerarInterfazP40SBIF_ScotiaFinDeMes ha finalizado con errores";
			}
			logger.info(salida.P_OUT_MENSAJE);
			logger.info("Errores: " + salida.P_OUT_ERRORESCONTADOR);
			logger.info("***** Fin PautaGenerarInterfazP40SBIF_ScotiaFinDeMes *****\n");
		}
		return salida;
	};
	

	public static void main(String[] args) throws Exception {
		/*
		 * Este proceso pauta es para cubrir un caso borde muy especifico. 
		 * Cuando el ultimo dia del mes es un dia inhabil y por lo tanto la pauta salta del dia anterior habil hasta el siguiente dia habil
		 * Solicitado por Scotia para generar la p40 historica al fin de mes anterior.
		 * 
		 */
		try (Connection con =  PlataformaUtils.getDESAConnection()   ) {
			SimpleDateFormat formato 	= new SimpleDateFormat("yyyy-MM-dd");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			
			String FechaSistema 		= "2025-11-03";    

               
            Date FechaSistemaDate		= formato.parse(FechaSistema); 
            LocalDate FechaSistemaLD 	= FechaSistemaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate fechaMenosUnMes 	= FechaSistemaLD.minusMonths(1);

            LocalDate FinMesAnterior 	= fechaMenosUnMes.withDayOfMonth(fechaMenosUnMes.lengthOfMonth());
            LocalDate DiaAnteriorFinMes	= FinMesAnterior.minusDays(1L);
 
            
			ObtenerDiaSiguiente DS = new ObtenerDiaSiguiente();
			InObtenerDiaSiguiente entradaDS = new InObtenerDiaSiguiente();			 
 		
			entradaDS.p_idPais 				= new Long(44);
			entradaDS.p_diaHabil 			= "S";					//al sumar dias, considera solo dias habiles
			entradaDS.p_soloCaeDiaHabil 	= "S";			//el resultado debe ser un dia habil
			
			InObtenerDiaSiguiente.FechasIn fechasInSig = new InObtenerDiaSiguiente.FechasIn();
			fechasInSig.numeroDias = 1L;		
				fechasInSig.fechaInicial = DiaAnteriorFinMes.format(formatter);
			entradaDS.p_fechas.add(fechasInSig);
			
			OutObtenerDiaSiguiente SalidaDiaSiguiente = DS.logica(con, entradaDS);
			
			String FechaCalculadaADS = SalidaDiaSiguiente.p_out_fechas.get(0).fechaCalculada ;
			
			if (!FechaCalculadaADS.equalsIgnoreCase(FechaSistema)) { // 
				System.out.println("No se ejecuta p40 fin de mes");				
			}else {				
				System.out.println("----------------------------------------------------------------------------");
				System.out.println("FechaSistema: " + FechaSistema);
				System.out.println(SalidaDiaSiguiente.p_out_fechas.get(0).fechaCalculada);
				
				System.out.println(SalidaDiaSiguiente.p_out_fechas.get(1).fechaCalculada);
				
				System.out.println("penultimo dia del mes anterior: " + DiaAnteriorFinMes); 
				System.out.println("FechaSistema: " + FechaCalculadaADS);
				System.out.println("Inicio de mes donde se salto el ultimo dia del mes anterior por ser no habil");
				
				Map<String,Object> params = new HashMap<>();
				params.put("IdEmpresa",1L);
				params.put("IdUsuario",1L);
				params.put("FechaSistema", "2024-11-13");
				
				PautaGenerarInterfazP40SBIF_ScotiaFinDeMes pgn = new PautaGenerarInterfazP40SBIF_ScotiaFinDeMes();
				try {
					pgn.transaction(con, params);
					con.rollback();
				} catch (Throwable e) {
					e.printStackTrace();
					con.rollback();
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();			
		}	

	}
	
	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		logger.info("Inicia ejecucion del job");
		SimpleDateFormat formato 	= new SimpleDateFormat("yyyy-MM-dd");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		SQL sql = new SQL();
		
		try {
			String FechaSistema 		= this.getParam("FechaEjecucion") == null ? "" : this.getParam("FechaEjecucion");
			if(FechaSistema.isEmpty()) { FechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, Long.parseLong(this.getParam("IdEmpresa")), "PLATAFORMA", "PLA.FechaSistema");	}
			
            Date FechaSistemaDate			= formato.parse(FechaSistema); 
            LocalDate FechaSistemaLD 		= FechaSistemaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate fechaMenosUnMes 		= FechaSistemaLD.minusMonths(1);
            LocalDate FinMesAnterior 		= fechaMenosUnMes.withDayOfMonth(fechaMenosUnMes.lengthOfMonth());
            LocalDate DiaAnteriorFinMes		= FinMesAnterior.minusDays(1L);
       
			ObtenerDiaSiguiente 		DS 	= new ObtenerDiaSiguiente	();
			InObtenerDiaSiguiente entradaDS = new InObtenerDiaSiguiente	();				 
			
			entradaDS.p_idPais 				= new Long(44);
			entradaDS.p_diaHabil 			= "S";
			entradaDS.p_soloCaeDiaHabil 	= "S";
			
			InObtenerDiaSiguiente.FechasIn fechasInSig = new InObtenerDiaSiguiente.FechasIn();
			fechasInSig.numeroDias 		=  1L;
			fechasInSig.fechaInicial 	= DiaAnteriorFinMes.format(formatter);				
			entradaDS.p_fechas.add(fechasInSig);
			
			OutObtenerDiaSiguiente SalidaDiaSiguiente = DS.logica(con, entradaDS);
			
			String FechaCalculadaADS 		= SalidaDiaSiguiente.p_out_fechas.get(0).fechaCalculada ;
			
			if (!FechaCalculadaADS.equalsIgnoreCase(FechaSistema)) { // 
				logger.info("El proceso PautaGenerarInterfazP40SBIF_ScotiaFinDeMes No ejecutara la interfaz p40.");
			}else {
				HashMap<String, Object> paramsRut = new HashMap<String, Object>();
				paramsRut.put("IdEmpresa", Long.parseLong(this.getParam("IdEmpresa")));
				List<DatoCodigoCorredora> datoCodigo = sql.query(con, SQL_CODIGO_CORREDORA, paramsRut, DatoCodigoCorredora.class);
				
				
				EntradaGenerarInterfazP40SBIF entrada = new EntradaGenerarInterfazP40SBIF();
				String rutaArchivo = Utils.getConfiguracion(con, Utils.getSchema(), Long.parseLong(this.getParam("IdEmpresa")), "PLA.RutaBaseArchivos");
				SUBCARPETA = ConfiguracionSistema.getParametroConfiguracionString(con,Long.parseLong(this.getParam("IdEmpresa")),"NORMATIVA","SubRutaArchivosModulo"); 
				//Se configura una subcarpeta para alojar los archivos.
				rutaArchivo = rutaArchivo.concat(SUBCARPETA);
				
				//Nota: la clase ClienteConfig configura a la entradap40 según el código de corredora que se envía, no se necesitan todos los parametros de entrada
				ClienteConfig clienteConfig = ClienteConfigFactory.getClienteConfig(datoCodigo.get(0).CodigoCorredora);
				clienteConfig.configurarParametros(entrada, this.getParam("IdEmpresa"), this.getParam("IdUsuario"), rutaArchivo);			
	    		entrada.p_Fecha = FinMesAnterior.format(formatter);
				GenerarInterfazP40SBIF_Scotia generarInterfazP40SBIF_Scotia = new GenerarInterfazP40SBIF_Scotia();
				SalidaGenerarInterfazP40SBIF resp = generarInterfazP40SBIF_Scotia.logica(con, entrada);
				
				SalidaEjecutarTareaJenkins outJenkins = new SalidaEjecutarTareaJenkins();				
				if(resp.P_OUT_ERRORESCONTADOR == 0) {// Envio del archivo P40 a la sftp 					
			    	Long idEmpresa = Long.valueOf(this.getParam("IdEmpresa"));
			    	String tipoTarea = "ENVIAR_ARCHIVO_P40";

					EntradaEjecutarTareaJenkins inJenkins = new EntradaEjecutarTareaJenkins();
					inJenkins.p_IdModulo = "PLA";
					inJenkins.p_IdUsuario = 0L;
					inJenkins.p_IdEmpresa = idEmpresa;
					inJenkins.p_TipoTarea = tipoTarea;
					inJenkins.p_Parametros.add(new ParametroJenkins("NOMBRE_ARCHIVO", resp.P_OUT_NOMBRE_ARCHIVO.replace(".txt", "")  ));	 
					EjecutarTareaJenkins 		jenkins = new EjecutarTareaJenkins();
					outJenkins = jenkins.logica(con, inJenkins);
				}				

				if (resp.P_OUT_ERRORESCONTADOR > 0 || outJenkins.P_OUT_ERRORESCONTADOR > 0 ) {
					logger.error("El proceso PautaGenerarInterfazP40SBIF_ScotiaFinDeMes ha finalizado con errores");
					if (resp.P_OUT_ERRORESCONTADOR > 0		) {	logger.error(resp.P_OUT_COMENTARIO		);  	}
					if (outJenkins.P_OUT_ERRORESCONTADOR > 0) {	logger.error(outJenkins.P_OUT_MENSAJES	);		}
					return false;
				}
			}
			logger.info("El proceso PautaGenerarInterfazP40SBIF_ScotiaFinDeMes se ha ejecutado correctamente");
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage());
    		return false;
		}
	}
	
	@Override
    public void onError(Throwable t) {
        logger.error(t);
    }

	@Override
	public void pause() {

	}
	
}
