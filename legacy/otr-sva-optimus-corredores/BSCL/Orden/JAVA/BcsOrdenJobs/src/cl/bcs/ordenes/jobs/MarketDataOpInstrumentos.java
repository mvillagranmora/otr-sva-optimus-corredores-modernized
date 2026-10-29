package cl.bcs.ordenes.jobs;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import cl.bcs.ordenes.pojo.ControlMercado;
import cl.bcs.plataforma.ins.CrearInstrumentos;
import cl.bcs.plataforma.ins.CrearInstrumentos.BolsaEntrada;
import cl.bcs.plataforma.ins.CrearInstrumentos.CrearInstrumentoEntrada;
import cl.bcs.plataforma.ins.CrearInstrumentos.InstrumentoEntrada;
import cl.bcs.plataforma.ins.CrearInstrumentos.CrearInstrumentoSalida;
import cl.bcs.plataforma.ins.ModificarInstrumentos.ModificarInstrumentoEntrada;
import cl.bcs.plataforma.ins.ModificarInstrumentos.ModificarInstrumentoSalida;
import cl.bcs.plataforma.ins.ModificarInstrumentos;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa;
import cl.bcs.plataforma.pla.utils.ConsultarEmpresa.InformacionEmpresa;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;

public class MarketDataOpInstrumentos extends BcsJob implements JavaCall {
	
	AtomicBoolean running=new AtomicBoolean(false);
	String IdTipoPrograma = "MARKETDATAOP_INSTRUMENTOS";
	//private final Log log = new Log("ORD.MARKETDATAOP_INSTRUMENTOS");
	private static final Logger LOGGER = LogManager.getLogger(MarketDataOpInstrumentos.class);
	private static final SimpleDateFormat FORMATO_FECHA 	= new SimpleDateFormat("yyyy-MM-dd");
	private static final List<String> IDTIPO_PUT 	= Arrays.asList(new String[]{"M","N","O","P","Q","R","S","T","U","V","W","X"});
	private static final List<String> IDTIPO_CALL 	= Arrays.asList(new String[]{"A","B","C","D","E","F","G","H","I","J","K","L"});
	private final String SQL_OBTENER_MENSAJES_SIN_PROCESAR = loadResourceToString("cl/bcs/ordenes/jobs/sql/MarketDataOpInstrumentos_ObtenerMensajesSinProcesar.sql");
	private final String SQL_OBTENER_MENSAJES_INSTRUMENTO_ACTUALIZAR = loadResourceToString("cl/bcs/ordenes/jobs/sql/MarketDataOpInstrumentos_ObtenerMensajesInstrumentoActualizar.sql");
	private final String SQL_OBTENER_MENSAJES_INSTRUMENTO_ELIMINAR = loadResourceToString("cl/bcs/ordenes/jobs/sql/MarketDataOpInstrumentos_ObtenerMensajesInstrumentoEliminar.sql");
	private final String SQL_INSTRUMENTO_ELIMINAR = loadResourceToString("cl/bcs/ordenes/jobs/sql/MarketDataOpInstrumentos_UpdateInstrumentoEliminar.sql");
		
	
	//private final String SQL_ACTUALIZA_PRECIOS = loadResourceToString("cl/bcs/ordenes/jobs/sql/MarketDataRvInstrumentos_ActualizaPrecios.sql");
	private final String SQL_ACTUALIZA_CONTROL_MERCADO = loadResourceToString("cl/bcs/ordenes/jobs/sql/Globales_ActualizaControlMercado.sql");
	
	public boolean transaction(Connection conn) throws InterruptedException, Throwable {
		go(conn,false);
		return true;
	}
	
	
	public static class RegistroInstrumento {
		public String IdMensaje1;
		public String IdMensaje2;
		public Long IdInstrumento;
		public String IdTipoInstrumento;
		public String codTicker;
		public String timeTicker;
		public String codIsin;
		public String action;
		public String instruments;
		public BigDecimal imbalance;
		public BigDecimal tradeVolume;
		public BigDecimal avgPrice;
		public BigDecimal tradeAmount;
		public String trend;
		public String cfiCode;
		public String maturityDate;
		public String issuer;
		public BigDecimal eventType;
		public String settlement;
		public String exercise;
		public String openContract;
		public BigDecimal StrikePrice;
		public BigDecimal minPrice;
		public BigDecimal highPrice;
		public BigDecimal roundLot;
		public String securitySubType;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}
	
	public static class BolsaInstrumentoPrecio {
		public Long IdInstrumento;
		public String IdBolsa;
		public String Fecha;
		public String Nemotecnico;
		//public String FechaActualizacionMD; AUTOMATICO, CURRENT_TIMESTAMP
		public BigDecimal PrecioMayor;
		public BigDecimal PrecioMenor;
		public BigDecimal PrecioApertura;
		public BigDecimal PrecioCierre;
		public BigDecimal PrecioMedio;
		public BigDecimal UnidadesTransadas;
		public BigDecimal MontoTransado;
		public BigDecimal Variacion;
		
		public String toString() {
			return cl.bcs.utils.Utils.printJson(this,true);		
		}
	}
	public static class IdInstrumento {
		public Long IdInstrumento;
	}
	private String getTipo(String nemo, BigDecimal tipo) {
		Long tp = 0L;
		BigDecimal dos = new BigDecimal(2);
		if (tipo != null && BigDecimal.ONE.equals(tipo)) {
			return "PUT";
		}
		if (tipo != null && dos.equals(tipo)) {
			return "CALL";
		}
		tp = IDTIPO_PUT.stream().filter(t-> t.equalsIgnoreCase(nemo.substring(5,6))).count();
		if (tp > 0){
			return "PUT";
		}
		tp = IDTIPO_CALL.stream().filter(t-> t.equalsIgnoreCase(nemo.substring(5,6))).count();
		if (tp > 0){
			return "CALL";
		}
		return "";
	}
	public void go(Connection conn, boolean debug) throws InterruptedException, Throwable {
		SQL sql = new SQL();
		running.set(true);
		LOGGER.info("ORD.MARKETDATAOP_INSTRUMENTOS");
		Long idEmpresa = 0L;
        Long idUsuario = 0L;
        
		if (debug) {
        	idEmpresa = 1L;
        	idUsuario = 9L;
        } else{
        	 idEmpresa = Long.parseLong(this.getParam("IdEmpresa"));
             idUsuario = Long.parseLong(this.getParam("IdUsuario"));
        }
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, idEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		ConsultarEmpresa consultarEmpresaService = new ConsultarEmpresa();
		InformacionEmpresa empresa = consultarEmpresaService.logica(conn, idEmpresa);
		Long idPaisEmpresa = empresa.IdPais;
		//SELECT
		HashMap<String,Object> params = new HashMap<String,Object>();
		List<RegistroInstrumento> registros = sql.query(conn, SQL_OBTENER_MENSAJES_SIN_PROCESAR,params,RegistroInstrumento.class);
		List<RegistroInstrumento> instAct = sql.query(conn, SQL_OBTENER_MENSAJES_INSTRUMENTO_ACTUALIZAR, params, RegistroInstrumento.class);
		List<RegistroInstrumento> instElim = sql.query(conn, SQL_OBTENER_MENSAJES_INSTRUMENTO_ELIMINAR, params, RegistroInstrumento.class);
		
		//LOOP
		ArrayList<BolsaInstrumentoPrecio> precios = new ArrayList<BolsaInstrumentoPrecio>();
		ArrayList<ControlMercado> registrosControlMercado =  new ArrayList<>();
		
		CrearInstrumentoSalida instOut = new CrearInstrumentoSalida();
		ModificarInstrumentoSalida salidaModInst = new ModificarInstrumentoSalida();
		
		try {
			LOGGER.debug("INICIO MarketDataOpInstrumentos---------------------------- ");
			//instrumentos a actualizar (NO SE USA EN ESTE JAVA)
			//List<RegistroInstrumento> instrumentosActualizar = registros.stream().filter(f->f.IdInstrumento != null).collect(Collectors.toList());
			//List<String> nemoAct = instrumentosActualizar.stream().map(s -> s.instruments).collect(Collectors.toList());
			//instrumentos nuevos
			List<RegistroInstrumento> instrumentosNuevo = registros.stream().filter(f->f.IdInstrumento == null).collect(Collectors.toList());
						
			//Ingreso instrumento nuevos
			CrearInstrumentoEntrada instIn = new CrearInstrumentoEntrada();
			for(RegistroInstrumento ins: instrumentosNuevo) {
				InstrumentoEntrada inNew = new InstrumentoEntrada();
				inNew.IdInterno 			= new Long(instrumentosNuevo.indexOf(ins));
				inNew.Nemotecnico 			= ins.instruments;
				inNew.Serie		 			= ins.instruments;
				inNew.ISIN					= ins.codIsin;
				inNew.EstaInscrito			= "S";//ins.action;
				inNew.InstrumentoAcreditado = "S";//ins.action;
				inNew.RazonSocial			= ins.issuer;
				inNew.FechaEmision			= new java.sql.Date(FORMATO_FECHA.parse(ins.timeTicker.substring(0,4)+"-"+ins.timeTicker.substring(4,6)+"-"+ins.timeTicker.substring(6,8)).getTime());
				inNew.FechaVencimiento		= new java.sql.Date(FORMATO_FECHA.parse(ins.maturityDate).getTime());
				inNew.FechaInicioVigencia	= new java.sql.Date(FORMATO_FECHA.parse(ins.timeTicker.substring(0,4)+"-"+ins.timeTicker.substring(4,6)+"-"+ins.timeTicker.substring(6,8)).getTime());
				inNew.FechaHastaAcreditado	= new java.sql.Date(FORMATO_FECHA.parse(ins.maturityDate).getTime());
				inNew.IdEmisor				= "BSX";
				inNew.IdTipoReajuste		= monedaSistema;
				inNew.IdMoneda				= monedaSistema;
				inNew.IdMonedaEmision		= monedaSistema;
				//EO: Equity Options 
				//FO: Fixed Income Options 
				//CO: Currency Options
				inNew.IdTipoInstrumento		= ins.instruments.substring(0,2).equalsIgnoreCase("SP")?"IOI":ins.instruments.substring(0,3).equalsIgnoreCase("DOL")?"IOD":"IOA";//ins.securitySubType.equalsIgnoreCase("EO")?"IOA":ins.securitySubType.equalsIgnoreCase("CO")?"IOD":ins.securitySubType.equalsIgnoreCase("FO")?"IOI":"";////\":\"IOA\",\""//ver de donde lo saco
				inNew.IdEstado				= "ACT";
				inNew.Tamano				= ins.roundLot.longValue();//ins.instruments.substring(0,3).equalsIgnoreCase("SPI")?50000L:ins.instruments.substring(0,3).equalsIgnoreCase("DOL")?50000L:100L;//ver de donde lo saco
				inNew.Precio				= ins.StrikePrice.multiply(new BigDecimal(0.1));
				inNew.PrecioReferencia		= ins.StrikePrice.multiply(new BigDecimal(0.1));
				inNew.FechaReferencia		= new java.sql.Date(FORMATO_FECHA.parse(ins.timeTicker.substring(0,4)+"-"+ins.timeTicker.substring(4,6)+"-"+ins.timeTicker.substring(6,8)).getTime());
				inNew.OrigenInstrumento 	= "O";//telepregon Opciones (MarketData)
				inNew.IdInstrumentoRelacionado	= 0L;
				inNew.EsPutEsCall			= getTipo(ins.instruments, ins.eventType);
				inNew.PrecioStrike			= ins.StrikePrice;
				inNew.EstiloOpcion			= ins.cfiCode;//"A": americana "E":Europea; //ver de donde la saco
				inNew.ModalidadLiquidacion	= ins.settlement;//"P": phisical, "C": cash(financiera);//ver de donde la saco Fisica o Financiera
				inNew.ModalidadEjercicio	= ins.exercise;//ins.instruments.substring(0,3).equalsIgnoreCase("SP")?"E":ins.instruments.substring(0,3).equalsIgnoreCase("DOL")?"E":"A";//ver de conde la saco
				inNew.TipoValorizacion		= "64";
				inNew.IdPaisEmisor			= idPaisEmpresa;
				
				if (!instIn.p_Instrumentos.stream().anyMatch(t-> t.Nemotecnico.contentEquals(ins.instruments))) {
					instIn.p_Instrumentos.add(inNew);
					BolsaEntrada bol = new BolsaEntrada();
					bol.IdInterno 	= inNew.IdInterno;//"							BIGINT,
					bol.IdBolsa		= "BCS";//"							NVARCHAR(3)
					
					instIn.p_Bolsas.add(bol);
				}
				ControlMercado ControlMercado = new ControlMercado();
				ControlMercado.IdMensaje1 = ins.IdMensaje1;
				ControlMercado.IdMensaje2 = ins.IdMensaje2;
				ControlMercado.Procesado = "S";
				ControlMercado.Log = "INSTRUMENTO NUEVO " + ins.IdMensaje1 + " " +ins.IdMensaje2 + " CARGADO";
				registrosControlMercado.add(ControlMercado);
				LOGGER.debug("INGRESO " + ControlMercado.toString());
			}
			if (!instIn.p_Instrumentos.isEmpty()) {
				instIn.p_IdEmpresa 			= idEmpresa;
				instIn.p_IdUsuario			= idUsuario;
				instIn.p_IdModulo			= "ORD";
				instIn.p_SimularOperacion 	= "N";
				instIn.p_UsaReglasNegocio	= "S";
				//public List<InstrumentoIn> p_Instrumentos;
				//public List<ValorRiesgoIn> p_ValoresRiesgo;
				//public List<BolsaIn> p_Bolsas;
				LOGGER.debug("INGRESO INSTRUMENTO: " + instIn.toString());
				CrearInstrumentos inst = new CrearInstrumentos(); 
				instOut = inst.logica(conn, instIn);
			}	
			if (instOut.P_OUT_ERRORESCONTADOR  == 0) {
				sql.batch(conn,SQL_ACTUALIZA_CONTROL_MERCADO, registrosControlMercado,ControlMercado.class);
			}else {
				LOGGER.error("INGRESO INSTRUMENTO CON ERRORES: " + instOut.toString());
			}
		
			
			//info instrumento a actualizar
			if (!instAct.isEmpty()) {
				ModificarInstrumentoEntrada inModInst = new ModificarInstrumentoEntrada();
				inModInst.p_IdEmpresa 			= idEmpresa;
				inModInst.p_IdUsuario			= idUsuario;
				inModInst.p_IdModulo			= "ORD";
				inModInst.p_SimularOperacion 	= "N";
				inModInst.p_UsaReglasNegocio	= "S";
				//public List<InstrumentoIn> p_Instrumentos;
				//public List<ValorRiesgoIn> p_ValoresRiesgo;
				//public List<BolsaIn> p_Bolsas;
				for(RegistroInstrumento ins: instAct) {
					cl.bcs.plataforma.ins.ModificarInstrumentos.InstrumentoEntrada inMod = new cl.bcs.plataforma.ins.ModificarInstrumentos.InstrumentoEntrada();
					inMod.IdInstrumento			= ins.IdInstrumento;
					inMod.Serie		 			= ins.instruments;
					inMod.ISIN					= ins.codIsin;
					inMod.EstaInscrito			= "S";//ins.action;
					inMod.InstrumentoAcreditado = "S";//ins.action;
					inMod.RazonSocial			= ins.issuer;
					//inMod.FechaEmision			= new java.sql.Date(format.parse(ins.timeTicker.substring(0,4)+"-"+ins.timeTicker.substring(4,6)+"-"+ins.timeTicker.substring(6,8)).getTime());
					inMod.FechaVencimiento		= new java.sql.Date(FORMATO_FECHA.parse(ins.maturityDate).getTime());
					//inMod.FechaInicioVigencia	= new java.sql.Date(format.parse(ins.timeTicker.substring(0,4)+"-"+ins.timeTicker.substring(4,6)+"-"+ins.timeTicker.substring(6,8)).getTime());
					inMod.FechaHastaAcreditado	= new java.sql.Date(FORMATO_FECHA.parse(ins.maturityDate).getTime());
					inMod.IdEmisor				= "BSX";
					inMod.IdTipoReajuste		= monedaSistema;
					inMod.IdMoneda				= monedaSistema;
					inMod.IdMonedaEmision		= monedaSistema;
					inMod.IdTipoInstrumento		= ins.instruments.substring(0,2).equalsIgnoreCase("SP")?"IOI":ins.instruments.substring(0,3).equalsIgnoreCase("DOL")?"IOD":"IOA";//ins.securitySubType.equalsIgnoreCase("EO")?"IOA":ins.securitySubType.equalsIgnoreCase("CO")?"IOD":ins.securitySubType.equalsIgnoreCase("FO")?"IOI":"";//\":\"IOA\",\""//ver de donde lo saco
					inMod.IdEstado				= "ACT";
					inMod.Tamano				= ins.roundLot.longValue();//ins.instruments.substring(0,3).equalsIgnoreCase("SPI")?50000L:ins.instruments.substring(0,3).equalsIgnoreCase("DOL")?50000L:100L;//ver de donde lo saco
					inMod.Precio				= ins.StrikePrice.multiply(new BigDecimal(0.1));
					inMod.PrecioReferencia		= ins.StrikePrice.multiply(new BigDecimal(0.1));
					//inMod.FechaReferencia		= new java.sql.Date(format.parse(ins.timeTicker.substring(0,4)+"-"+ins.timeTicker.substring(4,6)+"-"+ins.timeTicker.substring(6,8)).getTime());
					inMod.OrigenInstrumento 	= "O";//telepregon Opciones (MarketData)
					inMod.IdInstrumentoRelacionado	= 0L;
					inMod.EsPutEsCall			= getTipo(ins.instruments, ins.eventType);
					inMod.PrecioStrike			= ins.StrikePrice;
					inMod.EstiloOpcion			= ins.cfiCode;//"A": americana "E":Europea; //ver de donde la saco
					inMod.ModalidadLiquidacion	= ins.settlement;//"P": phisical, "C": cash(financiera);//ver de donde la saco Fisica o Financiera
					inMod.ModalidadEjercicio	= ins.exercise;//ins.instruments.substring(0,3).equalsIgnoreCase("SP")?"E":ins.instruments.substring(0,3).equalsIgnoreCase("DOL")?"E":"A";//ver de conde la saco
					inMod.TipoValorizacion		= "64";
					inMod.IdPaisEmisor			= idPaisEmpresa;
					
					inModInst.p_Instrumentos.add(inMod);
					
					cl.bcs.plataforma.ins.ModificarInstrumentos.BolsaEntrada bol = new cl.bcs.plataforma.ins.ModificarInstrumentos.BolsaEntrada();
					bol.IdInstrumento 	= inMod.IdInstrumento;//"							BIGINT,
					bol.IdBolsa		= "BCS";//"							NVARCHAR(3)
					
					inModInst.p_Bolsas.add(bol);
					
				}
				LOGGER.debug("ACTUALIZAR INSTRUMENTO: " + inModInst.toString());
				ModificarInstrumentos modInst = new ModificarInstrumentos();
				salidaModInst = modInst.logica(conn, inModInst);
				
				if (salidaModInst.P_OUT_ERRORESCONTADOR  != 0) {
					LOGGER.error("ACTUALIZAR INSTRUMENTO CON ERRORES: " + salidaModInst.toString());
				}
				
			}
			//info instrumento a actualizar
			if (!instElim.isEmpty()) {
				LOGGER.debug("ELIMINAR INSTRUMENTOS: " + instElim.size());
				List<IdInstrumento> par= instElim.parallelStream().map(id->{
					IdInstrumento inst= new IdInstrumento();
					inst.IdInstrumento = id.IdInstrumento;
					return inst;
				}).collect(Collectors.toList());
				
				sql.batch(conn, SQL_INSTRUMENTO_ELIMINAR, par, IdInstrumento.class);
			}
			if (instOut.P_OUT_ERRORESCONTADOR  == 0 && salidaModInst.P_OUT_ERRORESCONTADOR  == 0) {
				conn.commit();
			}
			
			LOGGER.info("FIN MarketDataOpInstrumentos---------------------------- ");
		}
		catch(Exception e) {
			throw e;
		}		
		
		running.set(false);
		return;
	}
	
	@Override
    public void onError(Throwable t) {
        LOGGER.error(IdTipoPrograma + " ****** ERROR  *******");  
        t.printStackTrace();
    }

	@Override
	public void pause() {

	}
	
	public static void main(String[] args) throws InterruptedException, SQLException, Throwable{
	
		MarketDataOpInstrumentos p = new MarketDataOpInstrumentos();
		Connection conn = PlataformaUtils.getBolsaConnection();
		p.go(conn, true);
		conn.commit();
		
		
	}




	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	
}
