package cl.bcs.plataforma.jobs;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.normativa.circulares.ReportesBCC.EnviarTransaccion.ParamsApi;
import cl.bcs.plataforma.ins.pojo.Instrumento;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.scheduler.BcsJob;
import cl.bcs.utils.CallRestService;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
/**
 * Actualizar instrumentos Negociados Reference Data -> optimus
 * IdPrograma: ACTUALIZAR_INSTRUMENTOS_NEGOCIADOS
 * @author HBravo
 *
 */
public class ActualizaInstrumentosNegociadosJob extends BcsJob implements JavaCall {
	private static final Logger logger = LogManager.getLogger(ActualizaInstrumentosNegociadosJob.class);
	private static final SimpleDateFormat FORMATO_FECHA = new SimpleDateFormat("yyyy-MM-dd");
	
	private final String SQL_SELECT_INSTRUMENTO =  loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizaInstrumentosNegociados_SelectInstrumentos.sql");
	private final String SQL_SELECT_BOLSA_INSTRUMENTO =  loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizaInstrumentosNegociados_SelectBolsaInstrumentosRaiz.sql");
	private final String SQL_INSERT_INSTRUMENTOS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizaInstrumentosNegociados_InsertInstrumentos.sql");
	private final String SQL_UPDATE_INSTRUMENTOS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizaInstrumentosNegociados_UpdateInstrumentos.sql");
	private final String SQL_INSERT_BOLSA_INSTRUMENTOS = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizaInstrumentosNegociados_InsertBolsaInstrumentos.sql");
	private final String SQL_INSERT_BOLSA_INSTRUMENTOS_PRECIO = loadResourceToString("cl/bcs/plataforma/jobs/sql/ActualizaInstrumentosNegociados_InsertBolsaInstrumentoPrecio.sql");
	
	public static class RespuestaRD{
		public List<ContenidoReferenceData> content;
		public PageRD page;
	}
	public static class PageRD{
		public Long 	size;//catidad elementos
		public Long 	number;//page actual
		public Long 	totalElements;
		public Long 	totalPages;//page.legth 
	}
	
	public static class ContenidoReferenceData{
		public String getInstrument() {
			return instrument;
		}
		public void setInstrument(String instrument) {
			this.instrument = instrument;
		}
		
		public Long id = 0L;
		public String isin = "";
		public String currency = "";
		public String status = "";
		public Long issuerId = 0L;// Emisor
		public String issuerName = "";
		public String issuerCode = "";
		public String issuerSector = "";
		public String issuerSubSector = "";
		public String action = "";
		public String creationDate = "0001-01-01T00:00:00";
		public String creationUser = "";
		public String updateDate = "0001-01-01T00:00:00.000000";
		public String updateUser = "";
		public String updateUserIp = "";
		public String instrument = "";// Instrumento Raiz
		public String mnemonic = "";
		public String description = "";
		public String listingDate = "0001-01-01";
		public String retirementDate = null;
		public boolean instrumentBase = false;
		public boolean instrumentAsset = false;
		public boolean isOrderBook = false;
		public String listingCountry = "";
		public String issuedAmount = null;
		public Long outstandingShares = 0L;
		public BigDecimal nominalPrice = BigDecimal.valueOf(0);
		public String nominalPriceDate = null;
		public String classification = "";
		public String category = "";
		public String depositary = "";
		public String tradesOnLoan = null;
		public String tradesOnRepo = null;
		public String issuerDescription = "";
		public String marketType = "";
		public String regulatorCode = "";
		public String cfiCode = "";
		public String repoPenalty = null;
		public String settlementType = "";
		public String assetType = "";
		public Long assetSubclass = 0L;
		public Long idMmeAsset = 0L;
		public String assetSubClassName = "";
		public Long idMmeOrderBook = 0L;
		public Long idMmeParticipant = 0L;
		public String statusOB = "";
		public String bookValue = null;
		public String bookValueDate = null;
		public String loanPenalty = null;
		public Long marketSegment = 0L;
		public boolean mainOrderBook = true;
		public String repoTypeOB = null;
		public String outstandingSharesDate = null;
		public String hasADR = null;
		public Long settlementDays = 0L;
		public String negotiableAsset = null;
		public String high52WeekPrice = null;
		public String high52WeekDate = null;
		public String low52WeekPrice = null;
		public String low52WeekDate = null;
		public Long segmentId = 0L;
		public String segmentName = "";
		public String marketName = "";
		public String marketId = "";
		public String priceType = "";
		public String assetClassId = "";
		public String segmentMmeId = "";
	}

	public static class BolsaInstrumento{
		public Long IdBolsaInstrumento;
		public Long IdInstrumento;
		public String IdBolsa;
		public String Nemotecnico;
		public Date Fecha;
	}	
	public static class RD{
		public List<String> ListaSalidas;
		public RespuestaRD respuestaRD;
	}

	@Override
	public boolean transaction(Connection con) throws InterruptedException, Throwable {
		return logica(con,null, false);
	}
	
	public boolean logica(Connection con,List<ContenidoReferenceData> in, boolean debug) throws InterruptedException, Throwable {
		logger.info("Inicio job ActualizaInstrumentosNegociadosJob");
		
    	try {
    		Long idEmpresa = (debug) ? 1L : Long.parseLong(this.getParam("IdEmpresa"));
    		String fechaSistema	= ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa,"PLATAFORMA", "PLA.FechaSistema");
    		HashMap<String, HashMap<String, String>> configuraciones = Utils.getConfiguraciones(con, idEmpresa);

    		//Token
    		String UsaTokenRD = configuraciones.get("PLATAFORMA").get("PLA.RDataToken.UsaToken");
    		String token = "";
    		if("S".equalsIgnoreCase(UsaTokenRD)) {
    			String tokenJson = ObtenerToken(con, idEmpresa);
    			JSONObject json = new JSONObject(tokenJson);
    			token = json.getString("access_token");
    		}
    		
    		//REFERENCE DATA
    		List<String> ListaTotalSalidas = new ArrayList<String>();
    		RespuestaRD respuestaRD = new RespuestaRD();
    		RD rd = RespuestaReferenceData(con, idEmpresa, token);
    		ListaTotalSalidas = rd.ListaSalidas;
    		respuestaRD = rd.respuestaRD;
    		
			//RD solo INS negociados
			List<ContenidoReferenceData> listaInstrumentosReferenceData = new ArrayList<ContenidoReferenceData>();
			Set<String> nemotecnicosReferenceData = new HashSet<>();
			for(String salida :ListaTotalSalidas) {
				respuestaRD = Utils.getGsonBuilder().fromJson(salida, RespuestaRD.class);
				for(ContenidoReferenceData inst:respuestaRD.content) {
					if(!inst.mnemonic.equalsIgnoreCase(inst.instrument)) {
						listaInstrumentosReferenceData.add(inst);	
						nemotecnicosReferenceData.add(inst.mnemonic);
					}
				}				
			}
			//Optimus, ins raiz, ins existe, ins nuevos
			List<Instrumento> instrumentosRV = new ArrayList<Instrumento>();
			List<Instrumento> instrumentosRaiz = new ArrayList<Instrumento>();
			Set<String> nemotecnicosExisteRaiz = new HashSet<>();
			HashMap<String, Instrumento> instrumentoExiste = new HashMap<String,Instrumento>();
			
			List<BolsaInstrumento> bolsaInstrumentos = new ArrayList<BolsaInstrumento>();
			Set<Long> IdInsRaiz_InsExiste = new HashSet<>();
			Set<Long> IdInsRaiz_InsNuevo = new HashSet<>();

			List<Instrumento> listaInstrumentosExisteRD = new ArrayList<Instrumento>(); //Update
			List<Instrumento> listaInstrumentosNuevoRD = new ArrayList<Instrumento>();  //Create
			try {
            	instrumentosRV = DatosInstrumentos(con);
            	for (Instrumento ins:instrumentosRV) {
            		if(0 == ins.IdInstrumentoRaiz ) {
            			instrumentosRaiz.add(ins);
            			nemotecnicosExisteRaiz.add(ins.Nemotecnico);
            		}
            		if (nemotecnicosReferenceData.contains(ins.Nemotecnico)) {
            			instrumentoExiste.put(ins.Nemotecnico,ins);
            		}
            	}
            	Instrumento instrumentoRD  = new Instrumento();
            	for(ContenidoReferenceData insRD : listaInstrumentosReferenceData){
            		for(Instrumento insRaiz : instrumentosRaiz) {
            			if(insRaiz.Nemotecnico.equalsIgnoreCase(insRD.instrument)) {
            				instrumentoRD  = new Instrumento();
            				//datos RD
		            		instrumentoRD.Nemotecnico = insRD.mnemonic;
		            		instrumentoRD.ISIN = (insRD.isin != null) ? insRD.isin : "";
		            		instrumentoRD.RazonSocial = insRD.issuerName;
		            		instrumentoRD.FechaEmision = (insRD.listingDate!=null) ? new Date(FORMATO_FECHA.parse(insRD.listingDate).getTime()) : new Date(FORMATO_FECHA.parse("1990-01-01").getTime());
		            		instrumentoRD.IdMoneda = insRD.currency;
		            		instrumentoRD.IdCondicionLiquidacion = (insRD.settlementDays != null) ? "T"+insRD.settlementDays : "";
		            		instrumentoRD.CorteMinimo = insRaiz.CorteMinimo;
		            		instrumentoRD.EstaInscrito = insRaiz.EstaInscrito;
		            		instrumentoRD.FechaVencimiento = insRaiz.FechaVencimiento;
		            		instrumentoRD.IdEmisor = insRaiz.IdEmisor;
		            		instrumentoRD.IdTipoReajuste = insRaiz.IdTipoReajuste;
		            		instrumentoRD.IdTipoInstrumento = insRaiz.IdTipoInstrumento;
							instrumentoRD.IdTipoInstrumentoOrigen = insRaiz.IdTipoInstrumentoOrigen;
		            		instrumentoRD.IdEstado = insRaiz.IdEstado;
		            		instrumentoRD.Precio = insRaiz.Precio;
		            		instrumentoRD.PrecioReferencia = insRaiz.PrecioReferencia;
		            		instrumentoRD.FechaReferencia = insRaiz.FechaReferencia;
		            		instrumentoRD.FrecuenciaCupon = insRaiz.FrecuenciaCupon;
		            		instrumentoRD.NumeroCupones = insRaiz.NumeroCupones;
		            		instrumentoRD.InstrumentoAcreditado = insRaiz.InstrumentoAcreditado;
		            		instrumentoRD.ValorizaPor = insRaiz.ValorizaPor;
		            		instrumentoRD.IdInstrumentoRelacionado = insRaiz.IdInstrumentoRelacionado;
		            		instrumentoRD.TipoBonoIRF = insRaiz.TipoBonoIRF;
		            		instrumentoRD.OrigenInstrumento = insRaiz.OrigenInstrumento;
		            		instrumentoRD.TipoRenta = insRaiz.TipoRenta;
		            		instrumentoRD.RelacionadoGrupo = insRaiz.RelacionadoGrupo;
		            		instrumentoRD.FormadorMercado = insRaiz.FormadorMercado;
		            		instrumentoRD.PactoAndino = insRaiz.PactoAndino;
		            		instrumentoRD.FactorExposicion = insRaiz.FactorExposicion;
		            		instrumentoRD.LargoPlazo = insRaiz.LargoPlazo;
		            		instrumentoRD.Tasa = insRaiz.Tasa;
		            		instrumentoRD.EsDivisible = insRaiz.EsDivisible;
		            		instrumentoRD.Reajustable = insRaiz.Reajustable;
		            		instrumentoRD.DecimalesPrecio = insRaiz.DecimalesPrecio;
		            		instrumentoRD.FechaInicioVigencia = insRaiz.FechaInicioVigencia;
		            		instrumentoRD.IndArt104 = insRaiz.IndArt104;
		            		instrumentoRD.TasaFiscal = insRaiz.TasaFiscal;
		            		instrumentoRD.TipoValorizacion = insRaiz.TipoValorizacion;
		            		instrumentoRD.TieneCupon = insRaiz.TieneCupon;
		            		instrumentoRD.NumeroCuotasCirculacionMesActual = insRaiz.NumeroCuotasCirculacionMesActual;
		            		instrumentoRD.NumeroAccionesCirculacion = insRaiz.NumeroAccionesCirculacion;
		            		instrumentoRD.IndicadorLiquidaDCV = insRaiz.IndicadorLiquidaDCV;
		            		instrumentoRD.IdPaisEmisor = insRaiz.IdPaisEmisor;
		            		instrumentoRD.Serie = insRaiz.Serie;
		            		instrumentoRD.IndicadorBolson = insRaiz.IndicadorBolson;
		            		instrumentoRD.CodigoRubro = insRaiz.CodigoRubro;
		            		instrumentoRD.PresenciaBursatilAjustada = insRaiz.PresenciaBursatilAjustada;
		            		instrumentoRD.PorcentajeCastigo = insRaiz.PorcentajeCastigo;
		            		instrumentoRD.FechaHastaAcreditado = insRaiz.FechaHastaAcreditado;
		            		instrumentoRD.IdMonedaEmision = insRaiz.IdMonedaEmision;
		            		instrumentoRD.IdCarga = insRaiz.IdCarga;
		            		instrumentoRD.NumeroAmortizaciones = insRaiz.NumeroAmortizaciones;
		            		instrumentoRD.Tera = insRaiz.Tera;
		            		instrumentoRD.IdOrdenSerie = insRaiz.IdOrdenSerie;
		            		instrumentoRD.AfectoArt107 = insRaiz.AfectoArt107;
		            		instrumentoRD.AfectoArt104 = insRaiz.AfectoArt104;
		            		instrumentoRD.CobroComisionesCustodia = insRaiz.CobroComisionesCustodia;
		            		instrumentoRD.EsInstrumentoCalificado = insRaiz.EsInstrumentoCalificado;
		            		instrumentoRD.EsAutorizado = insRaiz.EsAutorizado;
		            		instrumentoRD.IdInstrumentoRaiz = insRaiz.IdInstrumento;
		            		instrumentoRD.Tamano = insRaiz.Tamano;
		            		instrumentoRD.EsPutEsCall = insRaiz.EsPutEsCall;
		            		instrumentoRD.PrecioStrike = insRaiz.PrecioStrike;
		            		instrumentoRD.EstiloOpcion = insRaiz.EstiloOpcion;
		            		instrumentoRD.ModalidadLiquidacion = insRaiz.ModalidadLiquidacion;
		            		instrumentoRD.ModalidadEjercicio = insRaiz.ModalidadEjercicio;
		            		
		            		if(instrumentoExiste.containsKey(instrumentoRD.Nemotecnico)){//para update
		            			instrumentoRD.IdInstrumento = instrumentoExiste.get(instrumentoRD.Nemotecnico).IdInstrumento;
		            			if(!instrumentoRD.equals(instrumentoExiste.get(instrumentoRD.Nemotecnico))) {//solo instrumentos con modificaciones
			            			listaInstrumentosExisteRD.add(instrumentoRD);
			            			IdInsRaiz_InsExiste.add(insRaiz.IdInstrumento);
		            			}
		            		}else {
		            			if(nemotecnicosExisteRaiz.contains(insRD.instrument)) {//para create
		            				listaInstrumentosNuevoRD.add(instrumentoRD);
		            				IdInsRaiz_InsNuevo.add(insRaiz.IdInstrumento);
		            			}
		            		}
            			}
            		}
            	}
            	//Bolsa Optimus
            	bolsaInstrumentos = DatosBolsaInstrumento(con);
            	List<BolsaInstrumento> BolsasidInsRaiz_InsNuevo = new ArrayList<BolsaInstrumento>();
            	for(BolsaInstrumento bolsa:bolsaInstrumentos) {
            		if(IdInsRaiz_InsNuevo.contains(bolsa.IdInstrumento)) {
            			BolsasidInsRaiz_InsNuevo.add(bolsa);
            		}
            	}
            	//secuencias y Bolsas nuevas
            	List<Long> ids = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::INSInstrumento", (long) listaInstrumentosNuevoRD.size());
            	int t= 0;
            	List<BolsaInstrumento> ListaBolsaCrear = new ArrayList<BolsaInstrumento>();//Create
            	BolsaInstrumento nuevaBolsa = new BolsaInstrumento();
            	for(Instrumento insNuevo:listaInstrumentosNuevoRD) {
            		insNuevo.IdInstrumento =  ids.get(t);
    				t++;
    				//Bolsas nuevas
    				for(BolsaInstrumento bolsaIns:BolsasidInsRaiz_InsNuevo) {
    					if(bolsaIns.IdInstrumento.equals(insNuevo.IdInstrumentoRaiz)) {
    						nuevaBolsa = new BolsaInstrumento();
    						nuevaBolsa.Nemotecnico = insNuevo.Nemotecnico;
	    					nuevaBolsa.IdBolsa = bolsaIns.IdBolsa;
	    					nuevaBolsa.IdBolsaInstrumento = null;
	    					nuevaBolsa.IdInstrumento = insNuevo.IdInstrumento;
	    					nuevaBolsa.Fecha = Fecha.toDate(fechaSistema);
	    					ListaBolsaCrear.add(nuevaBolsa);
    					}
    				}
            	}
            	List<Long> idsBolsa = Utils.getSecuencia(con, "BSCL.Plataforma.BD.Secuencias::INS.BolsaInstrumento", (long) ListaBolsaCrear.size());
            	t = 0;
            	for(BolsaInstrumento bolsaIns:ListaBolsaCrear) {
            		bolsaIns.IdBolsaInstrumento = idsBolsa.get(t);
            		t++;
            	}
            	//Procesar a BD
            	logger.debug("Procesando instrumentos Negocoiados");
            	SQL sql = new SQL();
        		if(!listaInstrumentosExisteRD.isEmpty()) {
        			sql.batch(con, SQL_UPDATE_INSTRUMENTOS, listaInstrumentosExisteRD, Instrumento.class);
        		}
        		sql.batch(con, SQL_INSERT_INSTRUMENTOS, listaInstrumentosNuevoRD, Instrumento.class);
        		sql.batch(con, SQL_INSERT_BOLSA_INSTRUMENTOS, ListaBolsaCrear, BolsaInstrumento.class);
        		sql.batch(con, SQL_INSERT_BOLSA_INSTRUMENTOS_PRECIO, ListaBolsaCrear, BolsaInstrumento.class);
            }
            catch(Exception e) {
            	e.printStackTrace();
            	con.rollback();
            }
    	}catch(Exception e){
        	logger.error(e);
        	throw new InterruptedException("Error, no se puede obtener conexion con Reference Data");
        }finally {
		}
    	logger.info("Fin job");
        return true;
	}
	
	private List<Instrumento> DatosInstrumentos(Connection con) throws Exception {
		logger.debug("Datos instrumento");
		SQL _sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.clear();
		List<Instrumento> resultInst = _sql.query(con, SQL_SELECT_INSTRUMENTO, params, Instrumento.class);
		return resultInst;
	}
	private List<BolsaInstrumento> DatosBolsaInstrumento(Connection con) throws Exception {
		logger.debug("Datos Bolsa instrumento");
		SQL _sql = new SQL();
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.clear();
		List<BolsaInstrumento> resultBolsaInst = _sql.query(con, SQL_SELECT_BOLSA_INSTRUMENTO, params, BolsaInstrumento.class);
		return resultBolsaInst;
	}
	private String ObtenerToken(Connection con,Long idEmpresa) throws Exception {
		logger.debug("Obtencion Token");
		String token = "";
		
		HashMap<String, HashMap<String, String>> configuraciones = Utils.getConfiguraciones(con, idEmpresa);
		String serverToken = "";
		String urlToken = configuraciones.get("PLATAFORMA").get("PLA.RDataToken.url");
		String input = configuraciones.get("PLATAFORMA").get("PLA.RDataToken.input");
		logger.info("iniciando conexion con "+urlToken+input);
		CallRestService call = new CallRestService(serverToken, urlToken);
		token = call.callRestPostOAuth2(input.toString());
		return token;
	}
	private RD RespuestaReferenceData(Connection con,Long idEmpresa,String token) throws Exception {
		logger.debug("Conectado a Reference Data");
		RD respuesta = new RD();
		//REFERENCE DATA
		RespuestaRD respuestaRD = new RespuestaRD();
		HashMap<String, HashMap<String, String>> configuraciones = Utils.getConfiguraciones(con, idEmpresa);
		String server = configuraciones.get("PLATAFORMA").get("PLA.RData.Servidor");
		String url = configuraciones.get("PLATAFORMA").get("PLA.RData.InstrumentosRV.url");
		if (server==null || url==null) throw new Exception("Parámetros de conexión no encontrados");
		CallRestService call = new CallRestService(server, url);
		
		String listingCountry = configuraciones.get("PLATAFORMA").get("PLA.RData.InstrumentosRV.listingCountry");
		String size = configuraciones.get("PLATAFORMA").get("PLA.RData.InstrumentosRV.size");
		long page = 0;
		String queryParams = String.format("listingCountry=%s&size=%s&page=%s",
				URLEncoder.encode(listingCountry, StandardCharsets.UTF_8.toString()),
				URLEncoder.encode(size, StandardCharsets.UTF_8.toString()),
				URLEncoder.encode(page+"", StandardCharsets.UTF_8.toString())
				);
		List<String> ListaTotalSalidas = new ArrayList<String>();
		String salidaWebservice = new String();

		try {
			
			logger.info("iniciando conexion con "+server+url);
			salidaWebservice = call.callRestGetBearerToken(queryParams,token);
			ListaTotalSalidas.add(salidaWebservice);
			if (salidaWebservice.isEmpty()) {
				throw new InterruptedException("Sin datos, Reference Data");
			}
			respuestaRD = Utils.getGsonBuilder().fromJson(salidaWebservice, RespuestaRD.class);
			if (respuestaRD.page.totalPages != 0) {
				Long p = respuestaRD.page.totalPages;
				for (int i = 1; i < p; i++) {
					String queryParams2 = String.format("listingCountry=%s&size=%s&page=%s",
							URLEncoder.encode(listingCountry, StandardCharsets.UTF_8.toString()),
							URLEncoder.encode(size, StandardCharsets.UTF_8.toString()),
							URLEncoder.encode(i + "", StandardCharsets.UTF_8.toString()));
					salidaWebservice = call.callRestGetBearerToken(queryParams2,token);
					ListaTotalSalidas.add(salidaWebservice);
				}
			}
		} catch (Exception e) {
			logger.error(e);
			throw new InterruptedException("NO SE PUEDE OBTENER CONEXION, Reference Data");
		}
		respuesta.ListaSalidas=ListaTotalSalidas;
		respuesta.respuestaRD=respuestaRD;
		return respuesta;
	}
	
	@Override
	public void onError(Throwable t) {
		// TODO Auto-generated method stub
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}

	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) throws Throwable {
		System.out.println("== INICIO ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("desa02","connections.properties"))
		{
			Gson gson = Utils.getGsonBuilder();
			String json = "{\r\n"
					+ "      \"id\": 1963,\r\n"
					+ "      \"isin\": \"CLP7847L1080\",\r\n"
					+ "      \"currency\": \"CLP\",\r\n"
					+ "      \"status\": \"0\",\r\n"
					+ "      \"issuerId\": 1033,\r\n"
					+ "      \"issuerName\": \"EMPRESAS COPEC S.A.\",\r\n"
					+ "      \"issuerCode\": \"CL0467\",\r\n"
					+ "      \"issuerSector\": \"Energia\",\r\n"
					+ "      \"issuerSubSector\": null,\r\n"
					+ "      \"action\": \"3\",\r\n"
					+ "      \"creationDate\": \"2025-03-01T00:00:00\",\r\n"
					+ "      \"creationUser\": \"system\",\r\n"
					+ "      \"updateDate\": \"2025-08-11T22:52:56.006777\",\r\n"
					+ "      \"updateUser\": \"\",\r\n"
					+ "      \"updateUserIp\": \"\",\r\n"
					+ "      \"instrument\": \"COPEC\",\r\n"
					+ "      \"mnemonic\": \"COPEC\",\r\n"
					+ "      \"description\": \"EMPRESAS COPEC S.A.\",\r\n"
					+ "      \"listingDate\": \"1935-10-02\",\r\n"
					+ "      \"retirementDate\": null,\r\n"
					+ "      \"instrumentBase\": true,\r\n"
					+ "      \"instrumentAsset\": true,\r\n"
					+ "      \"isOrderBook\": true,\r\n"
					+ "      \"listingCountry\": \"CL\",\r\n"
					+ "      \"issuedAmount\": null,\r\n"
					+ "      \"outstandingShares\": 1299853848,\r\n"
					+ "      \"nominalPrice\": 0.66,\r\n"
					+ "      \"nominalPriceDate\": null,\r\n"
					+ "      \"classification\": \"1\",\r\n"
					+ "      \"category\": \"\",\r\n"
					+ "      \"depositary\": \"4\",\r\n"
					+ "      \"tradesOnLoan\": null,\r\n"
					+ "      \"tradesOnRepo\": null,\r\n"
					+ "      \"issuerDescription\": \"Empresas Copec es una de las principales sociedades anónimas de Chile y está presente en dos grandes áreas de actividad: recursos naturales, donde se cuenta con claras ventajas competitivas, y energía, estrechamente ligada al crecimiento y desarrollo productivo. En cada uno de estos sectores, la Compañía está permanentemente impulsando importantes programas de inversión, con el objetivo de elevar la escala de sus operaciones, expandir sus fronteras, aprovechar sinergias y velar por la sustentabilidad en todas sus actividades. Actualmente, Empresas Copec comercializa sus productos en más de 80\",\r\n"
					+ "      \"marketType\": \"\",\r\n"
					+ "      \"regulatorCode\": \"COPEC\",\r\n"
					+ "      \"cfiCode\": \"\",\r\n"
					+ "      \"repoPenalty\": null,\r\n"
					+ "      \"settlementType\": \"2\",\r\n"
					+ "      \"assetType\": \"assetShare\",\r\n"
					+ "      \"assetSubclass\": 61,\r\n"
					+ "      \"idMmeAsset\": 4162,\r\n"
					+ "      \"assetSubClassName\": \"AC CLP CL\",\r\n"
					+ "      \"idMmeOrderBook\": 7136,\r\n"
					+ "      \"idMmeParticipant\": 137,\r\n"
					+ "      \"statusOB\": \"0\",\r\n"
					+ "      \"bookValue\": null,\r\n"
					+ "      \"bookValueDate\": null,\r\n"
					+ "      \"loanPenalty\": null,\r\n"
					+ "      \"marketSegment\": 35,\r\n"
					+ "      \"mainOrderBook\": true,\r\n"
					+ "      \"repoTypeOB\": null,\r\n"
					+ "      \"outstandingSharesDate\": null,\r\n"
					+ "      \"hasADR\": null,\r\n"
					+ "      \"settlementDays\": 2,\r\n"
					+ "      \"negotiableAsset\": null,\r\n"
					+ "      \"high52WeekPrice\": null,\r\n"
					+ "      \"high52WeekDate\": null,\r\n"
					+ "      \"low52WeekPrice\": null,\r\n"
					+ "      \"low52WeekDate\": null,\r\n"
					+ "      \"segmentId\": 35,\r\n"
					+ "      \"segmentName\": \"Continuo CL\",\r\n"
					+ "      \"marketName\": \"Renta Variable CL\",\r\n"
					+ "      \"marketId\": \"3\",\r\n"
					+ "      \"priceType\": \"M\",\r\n"
					+ "      \"assetClassId\": \"3\",\r\n"
					+ "      \"segmentMmeId\": \"19\"\r\n"
					+ "    }";
			
			ContenidoReferenceData Objin = gson.fromJson(json, ContenidoReferenceData.class);
			List<ContenidoReferenceData> Objins = new ArrayList<ContenidoReferenceData>();
			Objins.add(Objin);
			
			ActualizaInstrumentosNegociadosJob demo = new ActualizaInstrumentosNegociadosJob();				
 			try {
 				demo.logica(con,Objins,true);
 				con.rollback();
 			}
 			catch(Exception er) {
 				er.printStackTrace();
				con.rollback();
			}
 			//con.commit();
 			con.rollback();
	    }
		System.out.println("== FIN ==");
	}

}
