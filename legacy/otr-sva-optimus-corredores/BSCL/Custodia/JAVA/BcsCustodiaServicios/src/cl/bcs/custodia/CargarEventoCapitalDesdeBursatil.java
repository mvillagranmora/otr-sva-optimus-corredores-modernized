package cl.bcs.custodia;

/********************************
 *
 * Lee eventos de capital desde la Ruebas y los carga en optimus
 * Proceso agendado en pauta diaria
 ********************************/

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.custodia.CargarEventoCapitalDesdeBursatil.RegistroAInsertar;
import cl.bcs.custodia.BuscarEventosDeCapitalFaltantes;
import cl.bcs.custodia.evc.CrearEventoCapital;
import cl.bcs.custodia.evc.CrearEventoCapital.Evento;
import cl.bcs.custodia.evc.ModificarEventoCapital;
import cl.bcs.custodia.evc.ModificarEventoCapital.EventoCapital_OUT;
import cl.bcs.custodia.evc.ProcesarEventoCapital;
import cl.bcs.custodia.evc.ProcesarEventoCapital.ProcesarEventoCapitalEntrada;
import cl.bcs.custodia.evc.ProcesarEventoCapital.ProcesarEventoCapitalSalida;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.OutObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;




public class CargarEventoCapitalDesdeBursatil implements JavaCall {	
	
	private final String SQL_SELECT_EVENTOS_POR_PROCESAR = loadResourceToString("cl/bcs/custodia/sql/CargarEventoCapitalDesdeBursatil_SelectEventosPorProcesar.sql");
	
	static final Logger logger = LogManager.getLogger(CargarEventoCapitalDesdeBursatil.class.getName());
	private final String c_SI = "S";

	public static class Entrada {
		public Long 		p_IdEmpresa;
		public Long 		p_IdUsuario;
		public String 		p_IdModulo;
		public String 		p_FechaSistema;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Salida {		
		public List<Long>				P_OUT_EVCSCARGADOS;
		public List<Long>				P_OUT_EVENTOSCARGADOS;
		public List<MensajeSalida>		P_OUT_MENSAJES;
		public Long						P_OUT_ERRORESCONTADOR = 0L;
        public List<EventoCapital_Con_Error> P_OUT_EVENTOS_ERRORES;
		
		public Salida(){
			this.P_OUT_EVCSCARGADOS = new ArrayList<>();
			this.P_OUT_EVENTOSCARGADOS = new ArrayList<>();
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
            this.P_OUT_EVENTOS_ERRORES = new ArrayList<EventoCapital_Con_Error>();
		}
	}
	
    public static class EventoCapital_Con_Error {
        public Long p_Secuencia;
        public String p_Nemo;
        public String p_IdTipoEventoCapital;
        public String p_FechaLimite;
        public String p_FechaPago;
        public String p_Mensaje;
    }
	
	public static class LogEjecucion {
		public Long		TimeMillis;
		public String 	Mensaje;
		public String	EsError;
		public String 	EsInterno;
	}
	
	public static class RegistroAInsertar {
		public String 		ACF_MERCADO;
		public String 		ACF_NEMO;
		public Long			ACF_SEC_VC;
		public String		ACF_DESCRIP_VC;
		public BigDecimal 	ACF_FAC_ACC_ANT;
		public BigDecimal	ACF_FAC_ACC_NUE;
		public String		ACF_FEC_ACT_VC;
		public String		ACF_FEC_LIM;
		public String		ACF_FEC_PAGO;
		public String 		ACF_NEMO_2;
		public BigDecimal	ACF_NUM_ACC_ANT;
		public BigDecimal	ACF_NUM_ACC_DER;
		public BigDecimal	ACF_NUM_ACC_NUE;
		public Long			ACF_NUM_DIVDO;
		public BigDecimal	ACF_PRE_ANT_VC;
		public BigDecimal	ACF_PRE_EX_VC;
		public String 		ACF_TIPO_VC;
		public String		ACF_TIPO_DIV;
		public BigDecimal	ACF_VAL_ACC;
		public BigDecimal	ACF_VAL_AN_DS;
		public BigDecimal	ACF_VAL_EX_DS;
		public BigDecimal	ACF_VAL_LIB_AJU;
		public String		ACF_VIG_NAA;
		public String		ACF_VC_PROC;
		public String		ACF_MONEDA;
		public String		ACV_MERCADO;
		public String		ACV_NEMO;
		public Long			ACV_SEC_VC;
		public Long			ACV_INDICE;
		public String 		ACV_HORA_MOD;
		public String 		ACV_DESCRIP_VC;
		public BigDecimal	ACV_FAC_ACC_ANT;
		public BigDecimal	ACV_FAC_ACC_NUE;
		public String		ACV_FEC_ACT_VC;
		public String		ACV_FEC_LIM;
		public String		ACV_FEC_PAGO;
		public String		ACV_NEMO_2;
		public BigDecimal	ACV_NUM_ACC_ANT;
		public BigDecimal	ACV_NUM_ACC_DER;
		public BigDecimal	ACV_NUM_ACC_NUE;
		public BigDecimal	ACV_NUM_DIVDO;
		public BigDecimal	ACV_PRE_ANT_VC;
		public BigDecimal	ACV_PRE_EX_VC;
		public String		ACV_TIPO_VC;
		public String		ACV_TIPO_DIV;
		public BigDecimal 	ACV_VAL_ACC;
		public BigDecimal 	ACV_VAL_AN_DS;
		public BigDecimal 	ACV_VAL_EX_DS;
		public BigDecimal 	ACV_VAL_LIB_AJU;
		public String		ACV_VIG_NAA;
		public String		ACV_VC_PROC;
		public String		ACV_MONEDA;
		public String		ACV_USUARIO_MOD;
		public String		ACV_TERMINAL_MOD;
		public String		ACV_TIPO_MOD;
		public Long			ECV_EVENTO_CAPITAL;
		
		public String 		IdClasificacionTipoInstrumento;
		public String 		IdTipoInstrumento;
		public String 		IdEmisor;
		public Long 		IdInstrumento;
		public String 		Nemotecnico;
		public String 		IdMoneda;
	
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Reg {
		public String 		ACF_MERCADO;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	public static class RegistroCargado{
		public int 			ACF_SEC_VC;
		public String		ACF_DESCRIP_VC;
		public Long 		IdEventoCapital;
		
	}
	
	public static class EventoPorProcesar extends BcsPojo{
		public Long IdEventoCapital;
		public String FechaLimite;
	}


	//-------------------------------------------
	// CALL
	//-------------------------------------------
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {	

		
		Gson gson = new Gson();		
		Entrada in=gson.fromJson(jin.toString() ,Entrada.class);
		Salida out=logica(con,in);
		
		out.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(out.P_OUT_MENSAJES);
		
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();

		return dataout;
	}

	
	//-------------------------------------------
	// LOGICA
	//-------------------------------------------
	public Salida logica(Connection con, Entrada entrada) throws Exception {
		
		logger.info("INICIO :: CargarEventoCapitalDesdeBursatil");
		BuscarEventosDeCapitalFaltantes eventos = new BuscarEventosDeCapitalFaltantes();
		
		Salida out = new Salida();
		
		
		List<RegistroAInsertar> RegistrosRuebas = eventos.obtenerEventosRuebas(con,entrada);
		
		List<CrearEventoCapital.Evento> nuevosRegistros = new ArrayList<>();
		List<ModificarEventoCapital.Evento> modificablesRegistros = new ArrayList<>();
	
		logger.info("Eventos a crear en optimus: " + RegistrosRuebas.size());

		for (RegistroAInsertar reg : RegistrosRuebas){
			
			ModificarEventoCapital.Evento regMod = new ModificarEventoCapital.Evento();
			CrearEventoCapital.Evento regNewCreate = new CrearEventoCapital.Evento();
			
			logger.info("Creando Evento : " + reg.Nemotecnico +" Tipo:"+ reg.ACF_TIPO_VC.toString() +" Secuencia:"+ reg.ACF_SEC_VC.toString() +" Fec Lim:"+ reg.ACF_FEC_LIM +" Fec Pag:"+ reg.ACF_FEC_PAGO );
			
			regMod.p_IdEmpresa = entrada.p_IdEmpresa;
			regMod.p_IdTipoEventoCapital = reg.ACF_TIPO_VC;
			regMod.p_Secuencia = reg.ACF_SEC_VC;
			regMod.p_Descripcion = reg.ACF_DESCRIP_VC;
			regMod.p_EsInscrito = "S";
			regMod.p_IdClasificacionTipoInstrumento = reg.IdClasificacionTipoInstrumento;
			regMod.p_IdTipoInstrumento = reg.IdTipoInstrumento;
			regMod.p_Nemo = reg.Nemotecnico;
			
			

			regMod.p_Factor = ("DV".equalsIgnoreCase(reg.ACF_TIPO_VC) || "RC".equalsIgnoreCase(reg.ACF_TIPO_VC)) ? reg.ACF_VAL_ACC:("EP".equalsIgnoreCase(reg.ACF_TIPO_VC) && reg.ACF_FAC_ACC_NUE.doubleValue() == 1.00000000 ? reg.ACF_FAC_ACC_NUE.divide(reg.ACF_FAC_ACC_ANT, 8, BigDecimal.ROUND_HALF_UP) : reg.ACF_FAC_ACC_NUE);

			
			
			regMod.p_Valor = reg.ACF_VAL_ACC;
			regMod.p_IdMoneda = reg.IdMoneda;
			regMod.p_IdInstrumento = reg.IdInstrumento;
			regMod.p_IdInstrumentoNuevo = reg.IdInstrumento;
			regMod.p_NemoNuevo = reg.Nemotecnico;
			
			

			regMod.p_FactorNuevo = ("DV".equalsIgnoreCase(reg.ACF_TIPO_VC) || "RC".equalsIgnoreCase(reg.ACF_TIPO_VC)) ? reg.ACF_VAL_ACC : ("EP".equalsIgnoreCase(reg.ACF_TIPO_VC) ? reg.ACF_VAL_ACC : (reg.ACF_FAC_ACC_NUE.doubleValue() == 1.00000000 ? reg.ACF_FAC_ACC_NUE.divide(reg.ACF_FAC_ACC_ANT, 8, BigDecimal.ROUND_HALF_UP) : reg.ACF_FAC_ACC_NUE));

			
			
			regMod.p_FechaActualizacion = (entrada.p_FechaSistema != null ? entrada.p_FechaSistema : reg.ACF_FEC_ACT_VC);
			regMod.p_FechaLimite = reg.ACF_FEC_LIM;
			regMod.p_FechaPago = reg.ACF_TIPO_VC.equalsIgnoreCase("EL") ? reg.ACF_FEC_LIM :reg.ACF_FEC_PAGO;
			regMod.p_NumeroDividendo = reg.ACF_NUM_DIVDO;
			regMod.p_IdTipoDividendo = reg.ACF_TIPO_DIV;
			regMod.p_PrecioAnterior = reg.ACF_PRE_ANT_VC;
			regMod.p_FechaHoraIngresoRegistro = entrada.p_FechaSistema;
			regMod.p_TipoIngreso = "C";
			regMod.p_IdUsuario = entrada.p_IdUsuario;
			regMod.p_IdEstado = "VIG";
			regMod.p_IdEmisor = reg.IdEmisor;

			regMod.p_Paridad = BigDecimal.ONE;
			regMod.p_IdMonedaLiquidacion = reg.IdMoneda;
			regMod.p_IdEmisorNuevo = "";
			//regMod.p_Indice = 0L;
			regMod.p_Origen = "RUE";
			
			regMod.p_Impuesto = BigDecimal.ZERO;
			
			logger.info("Creando Evento : " + reg.Nemotecnico +" Tipo:"+ reg.ACF_TIPO_VC.toString() +" Secuencia:"+ reg.ACF_SEC_VC.toString() +" Fec Lim:"+ reg.ACF_FEC_LIM +" Fec Pag:"+ reg.ACF_FEC_PAGO );
			
			regNewCreate.p_IdEmpresa = entrada.p_IdEmpresa;
			regNewCreate.p_IdTipoEventoCapital = reg.ACF_TIPO_VC;
			regNewCreate.p_Secuencia = reg.ACF_SEC_VC;
			regNewCreate.p_Descripcion = reg.ACF_DESCRIP_VC;
			regNewCreate.p_EsInscrito = "S";
			regNewCreate.p_IdClasificacionTipoInstrumento = reg.IdClasificacionTipoInstrumento;
			regNewCreate.p_IdTipoInstrumento = reg.IdTipoInstrumento;
			regNewCreate.p_Nemo = reg.Nemotecnico;
			
			

			regNewCreate.p_Factor = ("DV".equalsIgnoreCase(reg.ACF_TIPO_VC) || "RC".equalsIgnoreCase(reg.ACF_TIPO_VC))? reg.ACF_VAL_ACC:("EP".equalsIgnoreCase(reg.ACF_TIPO_VC) && reg.ACF_FAC_ACC_NUE.doubleValue() == 1.00000000 ? reg.ACF_FAC_ACC_NUE.divide(reg.ACF_FAC_ACC_ANT, 8, BigDecimal.ROUND_HALF_UP) : reg.ACF_FAC_ACC_NUE);
			
			
			regNewCreate.p_Valor = reg.ACF_VAL_ACC;
			regNewCreate.p_IdMoneda = reg.IdMoneda;
			regNewCreate.p_IdInstrumento = reg.IdInstrumento;
			regNewCreate.p_IdInstrumentoNuevo = reg.IdInstrumento;
			regNewCreate.p_NemoNuevo = reg.Nemotecnico;
			
			

			regNewCreate.p_FactorNuevo = ("DV".equalsIgnoreCase(reg.ACF_TIPO_VC) || "RC".equalsIgnoreCase(reg.ACF_TIPO_VC)) ? reg.ACF_VAL_ACC : ("EP".equalsIgnoreCase(reg.ACF_TIPO_VC) ? reg.ACF_VAL_ACC : (reg.ACF_FAC_ACC_NUE.doubleValue() == 1.00000000 ? reg.ACF_FAC_ACC_NUE.divide(reg.ACF_FAC_ACC_ANT, 8, BigDecimal.ROUND_HALF_UP) : reg.ACF_FAC_ACC_NUE));
			
			
			regNewCreate.p_FechaActualizacion = (entrada.p_FechaSistema != null ? entrada.p_FechaSistema : reg.ACF_FEC_ACT_VC);
			regNewCreate.p_FechaLimite = reg.ACF_FEC_LIM;
			regNewCreate.p_FechaPago = reg.ACF_FEC_PAGO;
			regNewCreate.p_NumeroDividendo = reg.ACF_NUM_DIVDO;
			regNewCreate.p_IdTipoDividendo = reg.ACF_TIPO_DIV;
			regNewCreate.p_PrecioAnterior = reg.ACF_PRE_ANT_VC;
			regNewCreate.p_FechaHoraIngresoRegistro = entrada.p_FechaSistema;
			regNewCreate.p_TipoIngreso = "C";
			regNewCreate.p_IdUsuario = entrada.p_IdUsuario;
			regNewCreate.p_IdEstado = "VIG";
			regNewCreate.p_IdEmisor = reg.IdEmisor;

			regNewCreate.p_Paridad = BigDecimal.ONE;
			regNewCreate.p_IdMonedaLiquidacion = reg.IdMoneda;
			regNewCreate.p_IdEmisorNuevo = "";
			regNewCreate.p_Indice = 0L;
			regNewCreate.p_Origen = "RUE";
			
			regNewCreate.p_Impuesto = BigDecimal.ZERO;
			
			if(reg.ECV_EVENTO_CAPITAL != null) { //SE VERIFICA QUE EL EVENTO CAPITAL TENGO p_IdEventoCapital YA QUE SI ES NULL SIGNFICA QUE ES REGISTRO NUEVO
				regMod.p_IdEventoCapital = reg.ECV_EVENTO_CAPITAL;
				modificablesRegistros.add(regMod);
			}else {
				regNewCreate.p_IdEventoCapital = reg.ECV_EVENTO_CAPITAL;
				nuevosRegistros.add(regNewCreate) ; //SE DEBE AGREGAR EL EVENTO CAPITAL SI TRAE NULL ES NUEVO
				
			}
			//nuevosRegistros.add(regMod);
			//nuevosRegistroMod.add(regMod);
		}

		ModificarEventoCapital.EventoCapital_IN paramsLlamadaMod = new ModificarEventoCapital.EventoCapital_IN();
		paramsLlamadaMod.p_IdEmpresa = entrada.p_IdEmpresa;
		paramsLlamadaMod.p_IdModulo = entrada.p_IdModulo;
		paramsLlamadaMod.p_IdUsuario = entrada.p_IdUsuario;
		paramsLlamadaMod.p_UsaReglasNegocio = "N";
		paramsLlamadaMod.p_EventoCapital = modificablesRegistros;
		
		
		//aqui verificar si los nuevosRegistros estan para modificar
		if(modificablesRegistros.size() > 0) {
			ModificarEventoCapital llamadaModificar = new ModificarEventoCapital();
			ModificarEventoCapital.EventoCapital_OUT resultaadoModificacion = llamadaModificar.logica(con, paramsLlamadaMod);
			
			out.P_OUT_ERRORESCONTADOR = resultaadoModificacion.P_OUT_ERRORESCONTADOR;
			logger.info("P_OUT_ERRORESCONTADOR de CrearEventoCapital: " + resultaadoModificacion.P_OUT_ERRORESCONTADOR.toString());
			
			if (resultaadoModificacion.P_OUT_ERRORESCONTADOR == 0){
				out.P_OUT_EVCSCARGADOS = resultaadoModificacion.P_OUT_SECUENCIA;	
				
				for (Long secuencia : resultaadoModificacion.P_OUT_SECUENCIA)
				{
					logger.info("Evento modificado con secuencia: " + secuencia);				
				}
			}
			out.P_OUT_MENSAJES = resultaadoModificacion.P_OUT_MENSAJES;
			
			logger.info("P_OUT_MENSAJES de ModificarEventoCapital: " + resultaadoModificacion.P_OUT_MENSAJES.toString());
		}

		CrearEventoCapital.EventoCapital_IN paramsLlamada = new CrearEventoCapital.EventoCapital_IN();
		paramsLlamada.p_IdEmpresa = entrada.p_IdEmpresa;
		paramsLlamada.p_IdModulo = entrada.p_IdModulo;
		paramsLlamada.p_IdUsuario = entrada.p_IdUsuario;
		paramsLlamada.p_UsaReglasNegocio = "N";
		paramsLlamada.p_EventoCapital = nuevosRegistros;
		
		if(nuevosRegistros.size() > 0) {
			CrearEventoCapital llamadaCrear = new CrearEventoCapital();
			CrearEventoCapital.EventoCapital_OUT resultadoCrear = llamadaCrear.logica(con, paramsLlamada);
			
			out.P_OUT_ERRORESCONTADOR = resultadoCrear.P_OUT_ERRORESCONTADOR;
			logger.info("P_OUT_ERRORESCONTADOR de CrearEventoCapital: " + resultadoCrear.P_OUT_ERRORESCONTADOR.toString());
			
			if (resultadoCrear.P_OUT_ERRORESCONTADOR == 0){
				out.P_OUT_EVCSCARGADOS = resultadoCrear.P_OUT_SECUENCIA;
				List<EventoPorProcesar> eventosPorProcesar = new ArrayList<>(); 
				
				if(resultadoCrear.P_OUT_SECUENCIA != null && !resultadoCrear.P_OUT_SECUENCIA.isEmpty()) {
					eventosPorProcesar = this.obtenerEventosPorProcesar(con, resultadoCrear.P_OUT_SECUENCIA);
					
					if(!eventosPorProcesar.isEmpty()) {
						procesarEventosCapital(con, entrada, eventosPorProcesar, logger);
					}else {
						logger.info("No hay eventos por procesar");
					}
				}
				
				if(resultadoCrear.P_OUT_EVENTOS_ERRORES != null && !resultadoCrear.P_OUT_EVENTOS_ERRORES.isEmpty()) {
					resultadoCrear.P_OUT_EVENTOS_ERRORES.forEach(msg ->{
						logger.info(String.format("Error al crear evento capital Nemotecnico: %s Tipo: %s Secuencia: %s Fec Lim: %s Fec Pag: %s ", msg.p_Nemo, msg.p_IdTipoEventoCapital, msg.p_Secuencia.toString(), msg.p_FechaLimite, msg.p_FechaPago));
					});
				}
			}
			
			out.P_OUT_MENSAJES = resultadoCrear.P_OUT_MENSAJES;
			
			logger.info("P_OUT_MENSAJES de CrearEventoCapital: " + resultadoCrear.P_OUT_MENSAJES.toString());
			
			logger.info("TERMINO :: CargarEventoCapitalDesdeBursatil");
		}

		return out;
	}
	
	private void procesarEventosCapital(Connection con, Entrada entrada, List<EventoPorProcesar> eventosPorProcesar, Logger logger) throws Exception {
	    LocalDate fechaSistemaDate = LocalDate.parse(entrada.p_FechaSistema);
	    List<Long> secuenciasProcesar = new ArrayList<>();
	    List<String> fechaLimiteProcesar = new ArrayList<>();

	    for (EventoPorProcesar evento :eventosPorProcesar) {
	        String fechaLimiteStr = evento.FechaLimite;
	        LocalDate fechaLimite = LocalDate.parse(fechaLimiteStr);

	        logger.info("Evento capital creado con secuencia: " + evento.IdEventoCapital);

	        if (fechaLimite.isBefore(fechaSistemaDate)) {
				Long secuencia = evento.IdEventoCapital;
	            secuenciasProcesar.add(secuencia);
	            fechaLimiteProcesar.add(fechaLimiteStr);
	        }
	    }

	    if (secuenciasProcesar.size() == 0) return;

	    ProcesarEventoCapital procesarEventoCapital = new ProcesarEventoCapital();
	    ProcesarEventoCapitalEntrada procesarEventoCapitalEntrada = new ProcesarEventoCapitalEntrada();

	    procesarEventoCapitalEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
	    procesarEventoCapitalEntrada.p_IdModulo = entrada.p_IdModulo;
	    procesarEventoCapitalEntrada.p_IdUsuario = entrada.p_IdUsuario;
	    procesarEventoCapitalEntrada.p_FechaLimite = fechaLimiteProcesar;
	    procesarEventoCapitalEntrada.p_IdEventoCapital = secuenciasProcesar;

		logger.info("Llamando a procesar eventos de capital con secuencias: " + secuenciasProcesar.toString() + " y fechas limite: " + fechaLimiteProcesar.toString());
	    
		ProcesarEventoCapitalSalida salida = procesarEventoCapital.logica(con, procesarEventoCapitalEntrada);

	    for (Long secuencia : salida.P_OUT_SECUENCIA) {
	        logger.info("Evento capital procesado con secuencia: " + secuencia);
	    }
	}
	
	private List<EventoPorProcesar> obtenerEventosPorProcesar (Connection con, List<Long> idEventosCapital) throws SQLException{
		logger.debug("INICIO::CargarEventoCapitalDesdeBursatil.obtenerEventosPorProcesar");
		long inicio = System.currentTimeMillis();
		
		SQL _sql = new SQL();
		
		HashMap<String, Object> params = new HashMap<>(1);
		params.put("IdEventoCapital", idEventosCapital);
		
		List<EventoPorProcesar> eventosPorProcesar =_sql.query(con, SQL_SELECT_EVENTOS_POR_PROCESAR, params, EventoPorProcesar.class); 
		
		long fin = System.currentTimeMillis();
		logger.debug("FIN::CargarEventoCapitalDesdeBursatil.obtenerEventosPorProcesar tiempo: " + (fin - inicio));
		return eventosPorProcesar;
	}	
	
	/**********************
	 * métodos del programa
	 **********************/
	
	public void logEjecucion(List<LogEjecucion> logej,String mensaje,String esError,String esInterno){
		LogEjecucion aux = new LogEjecucion();
		aux.TimeMillis = System.currentTimeMillis();
		aux.Mensaje = mensaje;
		aux.EsError = esError;
		aux.EsInterno = esInterno;
		logej.add(aux);
	}

	
	//-------------------------------------------
	// METODO PARA PRUEBAS LOCALES
	//-------------------------------------------
	public static void main(String[] args) {
        
		try {
			
			Connection con = PlataformaUtils.getPropertiesConnection("desa02", "connections.properties");
			Entrada entrada = new Entrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 97L;
			entrada.p_IdModulo = "CUS";
			entrada.p_FechaSistema = "2026-04-06";
			
			System.out.println(entrada.toString());

			Gson gson = new Gson();
 			JsonParser parser 		= new JsonParser();
	        JsonObject dataJsonIn	= parser.parse(gson.toJson(entrada)).getAsJsonObject();
	        CargarEventoCapitalDesdeBursatil demo=new CargarEventoCapitalDesdeBursatil();			
	        Salida resp=gson.fromJson(demo.call(con, dataJsonIn),Salida.class);
	    	System.out.println(resp);	

	    	//con.commit();
	    	con.rollback();
	    	con.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
        System.out.println("== END ==");
	}

}