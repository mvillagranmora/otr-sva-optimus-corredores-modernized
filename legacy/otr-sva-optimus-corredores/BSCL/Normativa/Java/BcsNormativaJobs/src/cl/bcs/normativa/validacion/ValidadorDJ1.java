package cl.bcs.normativa.validacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.esquema.RangoFechas;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

/**
 * Implementacion del validador de la DJ1.
 *
 * 
 *
 */
public class ValidadorDJ1 extends Validador {
	private static FileLoader loader = new FileLoader();
	private final String SELECT_CABECERAS_BY_KEY = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/ValidadorDJ1_Select_Cabeceras_By_Key.sql");
	private final String SELECT_CAL_PAGOS_BY_KEY = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/ValidadorDJ1_Select_Cal_Pagos_By_Key.sql");
	
    private static final List<String> S1_NATURAINFO = Arrays.asList("Y", "N");
    private static final List<String> S1_EVENTOREPORTE = Arrays.asList("ACS", "ANU","AOC","CES","MCR","MCS","MRC","MVE","MVI","NUE","OMD");
    private static final List<String> S2_INSTRUMENTO = Arrays.asList("CAL","CAP","CCS","FLR","FOB","FUT","FWD","FXS","OTR","PUT","STN","SWP");
    
    private static final List<String> S2_INSTRUMENTOX = Arrays.asList("CAL","CCS","FLR","FOB","FUT","FWD","FXS","OTR","PUT","STN","SWP");
    private static final List<String> S2_INSTRUMENTOR = Arrays.asList("CAP","FUT","FWD","OTR","STN");
    private static final List<String> S2_INSTRUMENTOI = Arrays.asList("FUT","FWD","OTR");
    
    private static final List<String> S2_INSTRUMENTOOPCIONES = Arrays.asList("CAL","PUT","CAP","FLR");
    
    private static final List<String> S2_INSTRUMENTOSSWAPS = Arrays.asList("CCS","FXS","SWP");

    private static final List<String> S2_INSTRUMENTOSFECHAFIXING = Arrays.asList("FWD", "FUT", "CAP", "FLR", "STN", "CAL", "PUT", "FOB", "FXS");
    private static final List<String> S2_INSTRUMENTOSNOINFCALFLUJOS = Arrays.asList("FWD", "FUT", "CAP", "FLR", "CAL", "PUT", "FXS", "FOB");
    private static final List<String> S2_INSTRUMENTOSINFCALFLUJOS = Arrays.asList("CCS","OTR","STN","SWP");
    
    private static final List<String> S2_INSTRUMENTOSFECHAINICIOCON = Arrays.asList("FWD","FUT","CAP","FLR","STN");
    private static final List<String> S2_INSTRUMENTOSNOOPSCLASE = Arrays.asList("FWD","FUT","FOB","FXS","CCS","SWP");
    private static final List<String> S2_INSTRUMENTOSNOTIPOTASAENTRREC = Arrays.asList("FWD","FUT","FOB","FXS");
    
    private static final List<String> S2_OPSCLASE = Arrays.asList("AM","AS","BE","EU","OT");
    
    private static final List<String> S2_MODALIDADLIQ = Arrays.asList("CO","EF");
    private static final List<String> S2_OBJETIVOOPE = Arrays.asList("CFC","CIE","CPB","CVR","NEG");
    private static final List<String> S2_CONVENIOMARCO = Arrays.asList("CCGG","ISDA","NOSU","OTRO");
    private static final List<String> S2_GARANTIAS = Arrays.asList("CC","CG","PG","SA","SB","SC");
    private static final List<String> S2_CLAUSULARECOU = Arrays.asList("N","Y");
    private static final List<String> S2_CLAUSULATERMINO = Arrays.asList("AMP","AMS","C1S","C2S","CO1","CO2","NOT");
    private static final List<String> S2_COMPRESIONCARTERA = Arrays.asList("N","Y");
    private static final List<String> S3_XR_OPINSTRUSUB = Arrays.asList("CCS","SWP");
    
    private static final List<String> S3_XR_TIPOTASA = Arrays.asList("BP0001M","BP0001W","BP0002M","BP0003M","BP0006M","BP000ON","BP0012M",
															    		"EE0001M","EE0001W","EE0002M","EE0003M","EE0006M","EE000ON","EE0012M",
															    		"EONIAON","ESTERON","EUR001M","EUR001W","EUR002M","EUR002W","EUR003M",
															    		"EUR006M","EUR009M","EUR012M","FEDFUND","FIXEDRT","JY0001M","JY0001W",
															    		"JY0002M","JY0003M","JY0006M","JY000ON","JY0012M","OTHERRT","PRIMERT",
															    		"SF0001M","SF0001W","SF0002M","SF0003M","SF0006M","SF000ON","SF0012M",
															    		"SOFRATE","SONIAON","SWAVRON","TABN01M","TABN03M","TABN06M","TABN12M",
															    		"TABU12M","TABUF3M","TABUF6M","TADO01M","TADO02M","TADO03M","TADO06M",
															    		"TADO12M","TKAVRON","TNAICP0","TRAICP0","US0001M","US0001W","US0002M",
															    		"US0003M","US0006M","US000ON","US0012M","USOBFRT","USTN03Y","USTN10Y");
    
    private static final List<String> S3_XR_CONVTIPOCAMBIO = Arrays.asList("BRL/SEK","CAD/CLP","CAD/SEK","CLF/CLP","CLF/EUR","CLF/USD",
																    		"CLP/EUR","CLP/USD","COP/SEK","EUR/CLF","EUR/CLP","EUR/IEP","EUR/SEK",
																    		"EUR/USD","IEP/SEK","JPY/SEK","MXN/CLF","MXN/CLP","MXN/SEK","PEN/CLP",
																    		"PEN/SEK","SEK/CLF","SEK/CLP","USD/CLF","USD/CLP","USD/EUR");
    
    private static final List<String> S3_I_TIPOACTIVOSUB = Arrays.asList("AII","CUS","ISI","NEM","OTR","SED");
    
    private static final List<String> S1_PLATAFORMANEGO = Arrays.asList("OTC","XBCL","XSGO");
    
    private static final List<String> CODIGOPAIS = Arrays.asList("ABW","AFG","AGO","AIA","ALA","ALB","AND","ARE",
													    		"ARG","ARM","ASM","ATA","ATF","ATG","AUS","AUT",
													    		"AZE","BDI","BEL","BEN","BES","BFA","BGD","BGR",
													    		"BHR","BHS","BIH","BLM","BLR","BLZ","BMU","BOL",
													    		"BRA","BRB","BRN","BTN","BVT","BWA","CAF","CAN",
													    		"CCK","CHE","CHL","CHN","CIV","CMR","COD","COG",
													    		"COK","COL","COM","CPV","CRI","CUB","CUW","CXR",
													    		"CYM","CYP","CZE","DEU","DJI","DMA","DNK","DOM",
													    		"DZA","ECU","EGY","ERI","ESH","ESP","EST","ETH",
													    		"FIN","FJI","FLK","FRA","FRO","FSM","GAB","GBR",
													    		"GEO","GGY","GHA","GIB","GIN","GLP","GMB","GNB",
													    		"GNQ","GRC","GRD","GRL","GTM","GUF","GUM","GUY",
													    		"HKG","HMD","HND","HRV","HTI","HUN","IDN","IMN","IND","IOT",
													    		"IRL","IRN","IRQ","ISL","ISR","ITA","JAM","JEY",
													    		"JOR","JPN","KAZ","KEN","KGZ","KHM","KIR","KNA",
													    		"KOR","KWT","LAO","LBN","LBR","LBY","LCA","LIE","LKA",
													    		"LSO","LTU","LUX","LVA","MAC","MAF","MAR","MCO","MDA",
													    		"MDG","MDV","MEX","MHL","MKD","MLI","MLT","MMR",
													    		"MNE","MNG","MNP","MOZ","MRT","MSR","MTQ","MUS","MWI","MYS","MYT",
													    		"NAM","NCL","NER","NFK","NGA","NIC","NIU","NLD","NOR",
													    		"NPL","NRU","NZL","OMN","PAK","PAN","PCN","PER","PHL",
													    		"PLW","PNG","POL","PRI","PRK","PRT","PRY","PSE","PYF","QAT",
													    		"REU","ROU","RUS","RWA","SAU","SDN","SEN","SGP",
													    		"SGS","SHN","SJM","SLB","SLE","SLV","SMR","SOM",
													    		"SPM","SRB","SSD","STP","SUR","SVK","SVN","SWE","SWZ",
													    		"SXM","SYC","SYR","TCA","TCD","TGO","THA","TJK","TKL",
													    		"TKM","TLS","TON","TTO","TUN","TUR","TUV","TWN",
													    		"TZA","UGA","UKR","UMI","URY","USA","UZB",
													    		"VAT","VCT","VEN","VGB","VIR","VNM","VUT",
													    		"WLF","WSM","YEM","ZAF","ZMB","ZWE");
    
    private static final List<String> CODIGOMONEDA = Arrays.asList("BRL","CAD","CLF","CLP","COP","EUR",	"GBP","IEP","JPY","MXN","PEN","SEK");
    
    private static final List<String> CONTRAPARTE = Arrays.asList("90249000-0","96572920-8");
    
    private static final List<String> ESCESION = Arrays.asList("ACS","CES","MCS");
    
    private static final String RUT_CONTRAPARTE_CENTRAL = "96572920-8";
    
    private static Connection cnn;
    private static Long idEmpresa;
    /**
     * Constructor.
     *
     * @param contexto
     *            El contexto de validacion.
     * @throws SQLException
     *             En caso de error SQL.
     */
    
    public ValidadorDJ1(final ContextoValidacion contexto) throws SQLException {
        super(contexto);
        cnn = contexto.getConnection();
        idEmpresa = contexto.getEmpresa().getId();
    }
    
    public boolean validarE100(final Registro registro) {
    	final String KRutCtraparte1 = (String) registro.getPorNombre("KRutCtraparte1");
    	return validarRut(KRutCtraparte1) || !KRutCtraparte1.trim().isEmpty();
    }
    
    public boolean validarE101(final Registro registro) {
    	final String KRutCtraparte1 = (String) registro.getPorNombre("KRutCtraparte1");
        return KRutCtraparte1 != null && !KRutCtraparte1.trim().isEmpty(); 
    }
    
    public boolean validarE103(final Registro registro) {
    	SQL sql = new SQL();
    	Long cantidad = 0L;
    	HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("KRutCtraparte1", (String) registro.getPorNombre("KRutCtraparte1"));
		params.put("KIdContrato", (String) registro.getPorNombre("KIdContrato"));
		params.put("KFechaContrato", (Date) registro.getPorNombre("KFechaContrato"));
		params.put("KIdOpeEstruc", (String) registro.getPorNombre("KIdOpeEstruc"));
		params.put("kProducto", (String) registro.getPorNombre("kProducto"));
		params.put("kPeriodoComercial", (Date) registro.getPorNombre("kPeriodoComercial"));
		
		try {
			cantidad = sql.countQuery(cnn, SELECT_CABECERAS_BY_KEY, params);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
        return cantidad == 1;
    }
    
    public boolean validarE104(final Registro registro) throws Exception {
    	//Fecha suscripcion mayor a fecha del reporte
    	final Date kFechaContrato = (Date) registro.getPorNombre("KFechaContrato");
    	Date fechaSistema = getFechaSistema();
        return kFechaContrato.before(fechaSistema) || kFechaContrato.equals(fechaSistema);
    }
    
    public boolean validarE105(final Registro registro) {
    	//Identificador de operaciones estructuradas debe ser mayor o igual a cero.
    	final String KIdOpeEstruc = (String) registro.getPorNombre("KIdOpeEstruc");
        return !KIdOpeEstruc.trim().isEmpty() && KIdOpeEstruc != null;
    }
    
    public boolean validarE107(final Registro registro) {
        final String S1_NaturaInfo = (String) registro.getPorNombre("S1_NaturaInfo");
        return S1_NaturaInfo != null && S1_NATURAINFO.contains(S1_NaturaInfo); 
    }
    
    public boolean validarE108(final Registro registro) {
        final String S1_EventoReporte = (String) registro.getPorNombre("S1_EventoReporte");
        return S1_EventoReporte != null && S1_EVENTOREPORTE.contains(S1_EventoReporte);     
    }
    
    public boolean validarE109(final Registro registro) {
        final String S1_EventoReporte = (String) registro.getPorNombre("S1_EventoReporte");
        return S1_EventoReporte != null && S1_EVENTOREPORTE.contains(S1_EventoReporte);     
    }
    
    public boolean validarE111(final Registro registro) {
        final String S1_LEICtraparte1 = (String) registro.getPorNombre("S1_LEICtraparte1");
        return S1_LEICtraparte1 != null && !S1_LEICtraparte1.isEmpty() && S1_LEICtraparte1.trim().length() == 20;     
    }
    
    public boolean validarE112(final Registro registro) {
    	final String S1_RutCtraparte2 = (String) registro.getPorNombre("S1_RutCtraparte2");
    	return validarRut(S1_RutCtraparte2);
    }
    
    public boolean validarE113(final Registro registro) {
        final String S1_RutCtraparte2 = (String) registro.getPorNombre("S1_RutCtraparte2");
        final String S1_LEICtraparte2 = (String) registro.getPorNombre("S1_LEICtraparte2");
        return !S1_RutCtraparte2.isEmpty() || !S1_LEICtraparte2.isEmpty() 
        		|| !"0".equals(S1_RutCtraparte2) || !"0".equals(S1_LEICtraparte2);     
    }
    
    public boolean validarE114(final Registro registro) {
        final String S1_NomCtraparte2 = (String) registro.getPorNombre("S1_NomCtraparte2");
        return S1_NomCtraparte2 != null && !S1_NomCtraparte2.trim().isEmpty();     
    }
    
    public boolean validarE115(final Registro registro) {
        final String S1_PaisCtraparte2 = (String) registro.getPorNombre("S1_PaisCtraparte2");
        return S1_PaisCtraparte2 != null && CODIGOPAIS.contains(S1_PaisCtraparte2);    
    }
    
    public boolean validarE116(final Registro registro) {
        final String S1_RutEntidad = (String) registro.getPorNombre("S1_RutEntidad");
        return S1_RutEntidad != null && CONTRAPARTE.contains(S1_RutEntidad.trim());    
    }
    
    public boolean validarE118(final Registro registro) {
        final String S1_RUTLiqCtraparte1 = (String) registro.getPorNombre("S1_RUTLiqCtraparte1");
        return validarRut(S1_RUTLiqCtraparte1); 
    }
    
    public boolean validarE119(final Registro registro) {
    	final String S1_LEILiqCtraparte1 = (String) registro.getPorNombre("S1_LEILiqCtraparte1");
    	final String S1_RUTLiqCtraparte1 = (String) registro.getPorNombre("S1_RUTLiqCtraparte1");
        return S1_LEILiqCtraparte1 != null && !S1_LEILiqCtraparte1.trim().isEmpty() 
        	&& S1_RUTLiqCtraparte1 != null && !S1_RUTLiqCtraparte1.trim().isEmpty(); 
    }
    
    public boolean validarE120(final Registro registro) {
    	final String S1_RutAgenteCal = (String) registro.getPorNombre("S1_RutAgenteCal");
    	return validarRut(S1_RutAgenteCal); 
    }
    
    public boolean validarE121(final Registro registro) {
    	final String S1_LEIAgenteCal = (String) registro.getPorNombre("S1_LEILiqCtraparte1");
    	final String S1_RutAgenteCal = (String) registro.getPorNombre("S1_RUTLiqCtraparte1");
        return S1_LEIAgenteCal != null && !S1_LEIAgenteCal.trim().isEmpty() 
        	&& S1_RutAgenteCal != null && !S1_RutAgenteCal.trim().isEmpty();    
    }
    
    public boolean validarE122(final Registro registro) {
        final String S1_NomAgenteCal = (String) registro.getPorNombre("S1_NomAgenteCal");
        return S1_NomAgenteCal != null && !S1_NomAgenteCal.trim().isEmpty();     
    }
    
    public boolean validarE123(final Registro registro) {
        final String S1_EventoReporte = (String) registro.getPorNombre("S1_EventoReporte");
        final String S1_RutCedente = (String) registro.getPorNombre("S1_RutCedente");    
        return !ESCESION.contains(S1_EventoReporte) && S1_RutCedente.trim().isEmpty()
        	|| ESCESION.contains(S1_EventoReporte) && !S1_RutCedente.trim().isEmpty();    
    }

    public boolean validarE124(final Registro registro) {   
    	final String S1_EventoReporte = (String) registro.getPorNombre("S1_EventoReporte");
        final String S1_LEICedente = (String) registro.getPorNombre("S1_LEICedente");
        return !ESCESION.contains(S1_EventoReporte) && S1_LEICedente.trim().isEmpty()
			|| ESCESION.contains(S1_EventoReporte) && !S1_LEICedente.trim().isEmpty();    
    }
    
    public boolean validarE125(final Registro registro) {
    	final String S1_RutCedente = (String) registro.getPorNombre("S1_RutCedente");
    	boolean result1 = S1_RutCedente.trim().isEmpty();
    	boolean result2 = validarRut(S1_RutCedente);
    	boolean result3 = result1 || result2;
    	return  result3;
    }
    
    //COMENTARIO: preguntar por el caso de vacios
    public boolean validarE127(final Registro registro) {
    	final String S1_RutIntermediario = (String) registro.getPorNombre("S1_RutIntermediario");
    	boolean result1 = S1_RutIntermediario.trim().isEmpty();
    	boolean result2 = validarRut(S1_RutIntermediario);
    	boolean result3 = result1 || result2;
    	return result3;
    }
    
    //COMENTARIO: no entiendo la validacion
    public boolean validarE128(final Registro registro) {
        return true;     
    }
    
    public boolean validarE129(final Registro registro) {
    	final String S1_LEICtraparte2 = (String) registro.getPorNombre("S1_LEICtraparte2");
        return S1_LEICtraparte2 != null && !S1_LEICtraparte2.trim().isEmpty();         
    }
    
    public boolean validarE130(final Registro registro) {
    	final String S1_LEIEntidad = (String) registro.getPorNombre("S1_LEIEntidad");
        return S1_LEIEntidad != null && !S1_LEIEntidad.trim().isEmpty();         
    }
    
    public boolean validarE131(final Registro registro) {
    	final String S1_LEILiqCtraparte1 = (String) registro.getPorNombre("S1_LEILiqCtraparte1");
        return S1_LEILiqCtraparte1 != null && !S1_LEILiqCtraparte1.trim().isEmpty();         
    }
    
    public boolean validarE132(final Registro registro) {
    	final String S1_LEIAgenteCal = (String) registro.getPorNombre("S1_LEIAgenteCal");
        return S1_LEIAgenteCal != null && !S1_LEIAgenteCal.trim().isEmpty();         
    }
    
    public boolean validarE133(final Registro registro) {
    	final String S1_EventoReporte = (String) registro.getPorNombre("S1_EventoReporte");
    	final String S1_LEICedente = (String) registro.getPorNombre("S1_LEICedente");
        return !ESCESION.contains(S1_EventoReporte)
        	|| ESCESION.contains(S1_EventoReporte) && !S1_LEICedente.trim().isEmpty();         
    }
    
    public boolean validarE134(final Registro registro) {
    	final String S1_LEIIntermediario = (String) registro.getPorNombre("S1_LEIIntermediario");
        return S1_LEIIntermediario != null && !S1_LEIIntermediario.trim().isEmpty();         
    }
    
    public boolean validarE135(final Registro registro) {
    	final String S1_PlataformaNego = (String) registro.getPorNombre("S1_PlataformaNego");
        return S1_PlataformaNego != null && S1_PLATAFORMANEGO.contains(S1_PlataformaNego);         
    }
    
    public boolean validarE138(final Registro registro) {
    	final String S1_EventoReporte = (String) registro.getPorNombre("S1_EventoReporte");
    	final BigDecimal S8_ValorMercado = (BigDecimal) registro.getPorNombre("S8_ValorMercado");
        return !"MVI".equals(S1_EventoReporte)
        	|| "MVI".equals(S1_EventoReporte) && S8_ValorMercado.compareTo(BigDecimal.ZERO) == 0;         
    }
    
    public boolean validarE201(final Registro registro) {
        final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        return S2_Instrumento != null && S2_INSTRUMENTO.contains(S2_Instrumento);     
    }
    
    public boolean validarE205(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S2_OpsClase = (String) registro.getPorNombre("S2_OpsClase");
        String esInstrumentoOpciones = S2_INSTRUMENTOOPCIONES.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esInstrumentoOpciones)
        	|| "S".equals(esInstrumentoOpciones) && S2_OPSCLASE.contains(S2_OpsClase);
    }
    
    public boolean validarE206(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S2_OpsClase = (String) registro.getPorNombre("S2_OpsClase");
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        String esInstrumentoOpciones = S2_INSTRUMENTOOPCIONES.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileMoneda)
        	|| "S".equals(esFileMoneda) && "S".equals(esInstrumentoOpciones) && S2_OPSCLASE.contains(S2_OpsClase) 
        	|| "S".equals(esFileMoneda) && "N".equals(esInstrumentoOpciones) && !S2_OPSCLASE.contains(S2_OpsClase);     
    }
    
    public boolean validarE207(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S2_OpsClase = (String) registro.getPorNombre("S2_OpsClase");
        String esFileRf = S2_Instrumento != null && S2_INSTRUMENTOR.contains(S2_Instrumento) ? "S" : "N";
        String esInstrumentoOpciones = S2_Instrumento != null && S2_INSTRUMENTOOPCIONES.contains(S2_Instrumento) ? "S" : "N";
        String tieneOpsClase = S2_OpsClase != null && S2_OPSCLASE.contains(S2_OpsClase) ? "S" : "N";
        String esInstrumentoOtros = S2_Instrumento != null && S2_INSTRUMENTO.contains(S2_Instrumento) ? "S" : "N";
        return ("S".equals(esFileRf) && "S".equals(esInstrumentoOpciones) && "S".equals(tieneOpsClase))
        	|| ("S".equals(esFileRf) && "S".equals(esInstrumentoOtros) && "N".equals(tieneOpsClase));
    }
    
    public boolean validarE208(final Registro registro) {
     	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S2_OpPosicionOp = (String) registro.getPorNombre("S2_OpPosicionOp");
        String esInstrumentoOpciones = S2_INSTRUMENTOOPCIONES.contains(S2_Instrumento) ? "S" : "N";
        return ("N".equals(esInstrumentoOpciones) || !"STN".equals(S2_Instrumento))
        	|| ("S".equals(esInstrumentoOpciones) || "STN".equals(S2_Instrumento)) && !S2_OpPosicionOp.trim().isEmpty();
    }
    
    public boolean validarE209(final Registro registro) {
     	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S2_OpPosicionOp = (String) registro.getPorNombre("S2_OpPosicionOp");
        String esInstrumentoOpciones = S2_INSTRUMENTOOPCIONES.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esInstrumentoOpciones) 
        	|| "S".equals(esInstrumentoOpciones) && !S2_OpPosicionOp.trim().isEmpty();
    }
    
    public boolean validarE210(final Registro registro) {
        final String S2_ModalidadLiq = (String) registro.getPorNombre("S2_ModalidadLiq");
        return S2_ModalidadLiq != null && S2_MODALIDADLIQ.contains(S2_ModalidadLiq);     
    }
    
    public boolean validarE211(final Registro registro) {
    	 final String S2_MonedaComp = (String) registro.getPorNombre("S2_MonedaComp");
    	return S2_MonedaComp != null && CODIGOMONEDA.contains(S2_MonedaComp);
    }
    
    public boolean validarE212(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S1_EventoReporte = (String) registro.getPorNombre("S1_EventoReporte");
    	final Date S2_FechaInicio = (Date) registro.getPorNombre("S2_FechaInicio");
    	final Date KFechaContrato = (Date) registro.getPorNombre("KFechaContrato");   	
    	String esSwap = S2_INSTRUMENTOSSWAPS.contains(S2_Instrumento) ? "S" : "N";  	
    	return 	!"NUE".equals(S1_EventoReporte) 
    			|| "NUE".equals(S1_EventoReporte)  && "N".equals(esSwap)
    			|| "NUE".equals(S1_EventoReporte)  && "S".equals(esSwap) && KFechaContrato.before(S2_FechaInicio);
    }
    
    public boolean validarE213(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S2_ModalidadLiq = (String) registro.getPorNombre("S2_ModalidadLiq");   
        final Date S2_FechaFixing1 = (Date) registro.getPorNombre("S2_FechaFixing1");
        final Date S2_FechaInicio =  (Date) registro.getPorNombre("S2_FechaInicio");
        return (!"EF".equals(S2_ModalidadLiq) || !"CO".equals(S2_ModalidadLiq)) 
        	|| ("EF".equals(S2_ModalidadLiq) && S2_INSTRUMENTOSFECHAFIXING.contains(S2_Instrumento) && (("0001-01-01").equals(S2_FechaInicio.toString()) || S2_FechaFixing1.toString().isEmpty()))
        	|| ("CO".equals(S2_ModalidadLiq) && (!("0001-01-01").equals(S2_FechaInicio.toString()) || !S2_FechaInicio.toString().isEmpty()));     
    }
    
    public boolean validarE214(final Registro registro) throws ParseException {
    	final Date S2_FechaInicio = (Date) registro.getPorNombre("S2_FechaInicio");
        final Date S2_FechaTermino = (Date) registro.getPorNombre("S2_FechaTermino");
        return "0001-01-01".equals(S2_FechaInicio.toString()) || S2_FechaInicio.before(S2_FechaTermino) || S2_FechaInicio.equals(S2_FechaTermino);     
    }
    
    public boolean validarE215(final Registro registro) throws ParseException {
    	final Date S2_FechaInicio = (Date) registro.getPorNombre("S2_FechaInicio");
        final Date S2_FechaPagOpe = (Date) registro.getPorNombre("S2_FechaPagOpe");
        return "0001-01-01".equals(S2_FechaInicio.toString()) || S2_FechaPagOpe.compareTo(S2_FechaInicio) >= 0;     
    }
    
    public boolean validarE216(final Registro registro) throws ParseException {
    	final Date S2_FechaInicioMod = (Date) registro.getPorNombre("S2_FechaInicioMod");
        final Date KFechaContrato = (Date) registro.getPorNombre("KFechaContrato");
        return "0001-01-01".equals(S2_FechaInicioMod.toString()) || S2_FechaInicioMod.compareTo(KFechaContrato) > 0;     
    }
    
    //REVISAR - COMO OBTENGO CALENDARIO DE PAGOS
    public boolean validarE217(final Registro registro) throws ParseException {
    	SQL sql = new SQL();
    	Long cantidad = 0L;
    	HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("KRutCtraparte1", (String) registro.getPorNombre("KRutCtraparte1"));
		params.put("KIdContrato", (String) registro.getPorNombre("KIdContrato"));
		params.put("KFechaContrato", (Date) registro.getPorNombre("KFechaContrato"));
		params.put("KIdOpeEstruc", (String) registro.getPorNombre("KIdOpeEstruc"));
		params.put("kProducto", (String) registro.getPorNombre("kProducto"));
		params.put("kPeriodoComercial", (Date) registro.getPorNombre("kPeriodoComercial"));

		try {
			cantidad = sql.countQuery(cnn, SELECT_CAL_PAGOS_BY_KEY, params);
		} catch (SQLException e) {
			e.printStackTrace();
		}

    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";  
        
        return "N".equals(esFileMoneda) && "N".equals(esFileTasa)
        	|| ("S".equals(esFileMoneda) || "S".equals(esFileTasa)) && !S2_INSTRUMENTOSNOINFCALFLUJOS.contains(S2_Instrumento)
        	|| ("S".equals(esFileMoneda) || "S".equals(esFileTasa)) && S2_INSTRUMENTOSNOINFCALFLUJOS.contains(S2_Instrumento) && cantidad == 0;     
    }
    
    //REVISAR - COMO OBTENGO CALENDARIO DE PAGOS
    public boolean validarE218(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";  
        return true;     
    }

    public boolean validarE219(final Registro registro) {
        final String S2_MonedaPrima = (String) registro.getPorNombre("S2_MonedaPrima");
        return S2_MonedaPrima != null && CODIGOMONEDA.contains(S2_MonedaPrima);     
    }
    
    public boolean validarE220(final Registro registro) {
        final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S2_MonedaPrima = (String) registro.getPorNombre("S2_MonedaPrima");
        return  !S2_INSTRUMENTOOPCIONES.contains(S2_Instrumento)
        	  || S2_INSTRUMENTOOPCIONES.contains(S2_Instrumento) && !S2_MonedaPrima.trim().isEmpty();     
    }
    
    public boolean validarE221(final Registro registro) {
        final String S2_MonedaPrima = (String) registro.getPorNombre("S2_MonedaPrima");
        final BigDecimal S2_MontoPrima = (BigDecimal) registro.getPorNombre("S2_MontoPrima");
        return S2_MonedaPrima.trim().isEmpty() && (S2_MontoPrima.compareTo(BigDecimal.ZERO) == 0)
           || !S2_MonedaPrima.trim().isEmpty() && (S2_MontoPrima.compareTo(BigDecimal.ZERO) != 0);    
    }

    public boolean validarE223(final Registro registro) {
        final String S2_ObjetivoOpe = (String) registro.getPorNombre("S2_ObjetivoOpe");
        return S2_ObjetivoOpe != null && S2_OBJETIVOOPE.contains(S2_ObjetivoOpe);     
    }

    public boolean validarE224(final Registro registro) {
        final String S2_ConvenioMarco = (String) registro.getPorNombre("S2_ConvenioMarco");
        String tramo1 = S2_ConvenioMarco.substring(0, 4);
        return S2_ConvenioMarco != null && S2_CONVENIOMARCO.contains(tramo1);     
    }
    
    public boolean validarE225(final Registro registro) {
        final String S2_PaisContrato = (String) registro.getPorNombre("S2_PaisContrato");
        return S2_PaisContrato != null && CODIGOPAIS.contains(S2_PaisContrato);     
    }
      
    public boolean validarE226(final Registro registro) {
        final String S2_Garantias = (String) registro.getPorNombre("S2_Garantias");
        return S2_Garantias != null && S2_GARANTIAS.contains(S2_Garantias);     
    }
    
    public boolean validarE227(final Registro registro) {
        final String S2_ClausulaRecou = (String) registro.getPorNombre("S2_ClausulaRecou");
        return S2_ClausulaRecou != null && S2_CLAUSULARECOU.contains(S2_ClausulaRecou);     
    }
    
    public boolean validarE228(final Registro registro) {
        final String S2_ClausulaTermino = (String) registro.getPorNombre("S2_ClausulaTermino");
        return S2_ClausulaTermino != null && S2_CLAUSULATERMINO.contains(S2_ClausulaTermino);     
    }
    
    public boolean validarE229(final Registro registro) throws ParseException {
    	final Date S2_FechaInicio = (Date) registro.getPorNombre("S2_FechaInicio");
        final Date S2_FechaEfecTermino = (Date) registro.getPorNombre("S2_FechaEfecTermino");
        return S2_FechaInicio.before(S2_FechaEfecTermino);         
    }
    
    public boolean validarE230(final Registro registro) {
        final String S2_CompresionCartera = (String) registro.getPorNombre("S2_CompresionCartera");
        return S2_CompresionCartera != null && S2_COMPRESIONCARTERA.contains(S2_CompresionCartera);     
    }
    
    public boolean validarE231(final Registro registro) {
        final String S2_ConvenioMarco = (String) registro.getPorNombre("S2_ConvenioMarco");
        String tramo1 = S2_ConvenioMarco.substring(0, 4);
        int largo = S2_ConvenioMarco.length();
        String tramo2 = S2_ConvenioMarco.substring(4, largo);
        return !tramo1.trim().isEmpty() && !tramo2.trim().isEmpty();     
    }
    
    //REVISAR - DE DONDE SACO PAGOS INFORMADOS
    public boolean validarE232(final Registro registro) {
      return true;  
    }
    
    //COMENTARIO: En sql aparece KRutCtraparte1 y en datos esta el S1_RutCtraparte2
    public boolean validarE234(final Registro registro) {
        final String S2_ConvenioMarco = (String) registro.getPorNombre("S2_ConvenioMarco");
        ///final String KRutCtraparte1 = (String) registro.getPorNombre("KRutCtraparte1");
        final String S1_RutCtraparte2 = (String) registro.getPorNombre("S1_RutCtraparte2");
        int largo = S2_ConvenioMarco.length();
        String rutConvenioMarco = S2_ConvenioMarco.substring(4, largo);
        String rutContraparteSinGuion = S1_RutCtraparte2.substring(0, S1_RutCtraparte2.length()-2);
        return rutContraparteSinGuion.equals(rutConvenioMarco.trim()); 
    }
    
    //REVISAR - NUMERO DE GARANTIAS
    public boolean validarE235(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S2_Garantias = (String) registro.getPorNombre("S2_Garantias");
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";  
        return true; 
    }
    
    //REVISAR - NUMERO DETALLE DE GARANTIAS
    public boolean validarE236(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S2_Garantias = (String) registro.getPorNombre("S2_Garantias");
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";  
        return true;  
    }
    
    //REVISAR - NUMERO TOTAL DE INTERCAMBIOS
    public boolean validarE239(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final long S2_NumTotIntercam = (Long) registro.getPorNombre("S2_NumTotIntercam");
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";  
        return true;  
    }
    
    public boolean validarE240(final Registro registro) throws ParseException {
    	final String S2_ModalidadLiq = (String) registro.getPorNombre("S2_ModalidadLiq");
        final Date S2_FechaFixing1 = (Date) registro.getPorNombre("S2_FechaFixing1");
        final Date S2_FechaTermino = (Date) registro.getPorNombre("S2_FechaTermino");
        
        return 	!"CO".equals(S2_ModalidadLiq) 
        		|| "CO".equals(S2_ModalidadLiq) && S2_FechaFixing1.before(S2_FechaTermino);         
    }
    
    public boolean validarE242(final Registro registro) {
    	final BigDecimal S2_MontoPrima = (BigDecimal) registro.getPorNombre("S2_MontoPrima");
        final String S2_MonedaPrima = (String) registro.getPorNombre("S2_MonedaPrima");      
        return (S2_MontoPrima.compareTo(BigDecimal.ZERO) != 0) && !S2_MonedaPrima.trim().isEmpty()
        	|| (S2_MontoPrima.compareTo(BigDecimal.ZERO) == 0) && S2_MonedaPrima.trim().isEmpty();     
    }
   
    public boolean validarE245(final Registro registro) {
    	final String S1_LEIEntidad = (String) registro.getPorNombre("S1_LEIEntidad");
        final String S1_RutEntidad = (String) registro.getPorNombre("S1_RutEntidad");
    	return RUT_CONTRAPARTE_CENTRAL.equals(S1_RutEntidad.trim()) && !S1_LEIEntidad.trim().isEmpty();  
    }    
    
    public boolean validarE247(final Registro registro) {
        final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final long S2_NumTotIntercam = (Long) registro.getPorNombre("S2_NumTotIntercam");
        
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";
        
    	return ("S".equals(esFileMoneda) || "S".equals(esFileTasa)) && S2_NumTotIntercam >= 1;  
    }    
    
    public boolean validarE248(final Registro registro) {
        final Date S2_FechaTermino = (Date) registro.getPorNombre("S2_FechaTermino");      
    	return S2_FechaTermino != null && (!S2_FechaTermino.toString().isEmpty() || !"0001-01-01".equals(S2_FechaTermino.toString()));  
    }       
    
    public boolean validarE249(final Registro registro) {
        final Date S2_FechaPagOpe = (Date) registro.getPorNombre("S2_FechaPagOpe");      
    	return S2_FechaPagOpe != null && (!S2_FechaPagOpe.toString().isEmpty() || !"0001-01-01".equals(S2_FechaPagOpe.toString()));  
    }   
    
    public boolean validarE250(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S2_OpPosicionOp = (String) registro.getPorNombre("S2_OpPosicionOp"); 
        final BigDecimal S2_MontoPrima = (BigDecimal) registro.getPorNombre("S2_MontoPrima");
        String esInstrumentoOpciones = S2_INSTRUMENTOOPCIONES.contains(S2_Instrumento) ? "S" : "N";
    	return  "N".equals(esInstrumentoOpciones)
    		||	"S".equals(esInstrumentoOpciones) && ("BYER".equals(S2_OpPosicionOp) || "SLLR".equals(S2_OpPosicionOp)) && S2_MontoPrima.compareTo(BigDecimal.ZERO) == 0; 
    } 
    
    public boolean validarE251(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final long S2_NumTotIntercam = (Long) registro.getPorNombre("S2_NumTotIntercam");
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";  
        return ("S".equals(esFileMoneda) || "S".equals(esFileTasa))
        	&& S2_INSTRUMENTOSFECHAFIXING.contains(S2_Instrumento)
        	&& S2_NumTotIntercam == 1;
    } 
    
    public boolean validarE252(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S2_FechaInicio = (String) registro.getPorNombre("S2_Instrumento");  
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";  
        return "S".equals(esFileTasa) 
        	&& ((S2_INSTRUMENTOSFECHAINICIOCON.contains(S2_Instrumento) && !S2_FechaInicio.trim().isEmpty())
        			|| ("SWP".equals(S2_Instrumento) && S2_FechaInicio.trim().isEmpty()));
    } 
    

    public boolean validarE253(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S2_ModalidadLiq = (String) registro.getPorNombre("S2_ModalidadLiq");
    	final Date S2_FechaFixing1 = (Date) registro.getPorNombre("S2_FechaFixing1");  
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";  
        return "N".equals(esFileTasa) || 
        		("S".equals(esFileTasa) && (!"CO".equals(S2_ModalidadLiq) || !"EF".equals(S2_ModalidadLiq)))
        	 ||	("S".equals(esFileTasa) && "CO".equals(S2_ModalidadLiq) && (!S2_FechaFixing1.toString().isEmpty() || !"0001-01-01".equals(S2_FechaFixing1.toString()))
        	 || ("EF".equals(S2_ModalidadLiq) && !"OTR".equals(S2_Instrumento) && (S2_FechaFixing1.toString().isEmpty() || "0001-01-01".equals(S2_FechaFixing1.toString()))));
    } 
    
    //REVISAR - TIENEN MAS DE UN FLUJO
    public boolean validarE256(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";  
    	return true;
    }
    
    //REVISAR - FLUJOS SIN REGISTRO TIPO R
    public boolean validarE257(final Registro registro) {
       return true;
    }
    
   //REVISAR - FLUJOS SIN REGISTRO TIPO E
    public boolean validarE258(final Registro registro) {
        return true;
    }
    
    public boolean validarE260(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S2_OpsClase = (String) registro.getPorNombre("S2_OpsClase");
        return S2_INSTRUMENTOSNOOPSCLASE.contains(S2_Instrumento) && S2_OpsClase.isEmpty();
    }
    
    public boolean validarE261(final Registro registro) throws Exception {
    	final String S1_EventoReporte = (String) registro.getPorNombre("S1_EventoReporte");
    	final Date S2_FechaTermino = (Date) registro.getPorNombre("S2_FechaTermino");
        return  !"MVE".equals(S1_EventoReporte)
        	|| "MVE".equals(S1_EventoReporte) && S2_FechaTermino.before(getFechaSistema());
    }
    
    public boolean validarE262(final Registro registro) throws Exception {
    	final String S1_EventoReporte = (String) registro.getPorNombre("S1_EventoReporte");
    	final Date S2_FechaTermino = (Date) registro.getPorNombre("S2_FechaTermino");
        return !"MVI".equals(S1_EventoReporte)
        	|| "MVI".equals(S1_EventoReporte) && S2_FechaTermino.before(getFechaSistema());
    }
    
    //REVISAR - NUMRO DE CONTRATOS DUPLICADOS
    public boolean validarE263(final Registro registro) {
        return true;  
    }
    
    public boolean validarE267(final Registro registro) {
        final String S2_ModalidadLiq = (String) registro.getPorNombre("S2_ModalidadLiq");
        final Date S2_FechaFixing1 = (Date) registro.getPorNombre("S2_FechaFixing1");        
        return !"CO".equals(S2_ModalidadLiq) ||!"EF".equals(S2_ModalidadLiq)
        	|| ("CO".equals(S2_ModalidadLiq) && (S2_FechaFixing1.toString().isEmpty() || "0001-01-01".equals(S2_FechaFixing1.toString())))
        	|| ("EF".equals(S2_ModalidadLiq) && (!S2_FechaFixing1.toString().isEmpty() || !"0001-01-01".equals(S2_FechaFixing1.toString())));     
    }
    
    public boolean validarE301(final Registro registro) {
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        return S3_XR_OpInstruSub != null && S3_XR_OPINSTRUSUB.contains(S3_XR_OpInstruSub);     
    }
    
    public boolean validarE302(final Registro registro) {
   	 	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
   	 	final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
   	 	final String S3_MonedaComprada = (String) registro.getPorNombre("S3_MonedaComprada");
   	 	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
//   	 	return "N".equals(esFileMoneda) 
//   	 		|| "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub)
//   	 		|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && S3_MonedaComprada.trim().isEmpty();
   	 	return true;
    }
    

    public boolean validarE303(final Registro registro) {
    	 final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	 final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
    	 final BigDecimal S3_MontoComprada = (BigDecimal) registro.getPorNombre("S3_MontoComprada");
    	 String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
//    	 return "N".equals(esFileMoneda) 
//    	   	 || "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub)
//    		 || "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && S3_MontoComprada.compareTo(BigDecimal.ZERO) == 0;
    	 return true;
    }
    
    public boolean validarE304(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
    	final String S3_MonedaComprada = (String) registro.getPorNombre("S3_MonedaComprada");
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
    	return "N".equals(esFileMoneda)
    		|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub)
    		|| "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub) && CODIGOMONEDA.contains(S3_MonedaComprada);
    }
    
    public boolean validarE305(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
    	final BigDecimal S3_MontoComprada = (BigDecimal) registro.getPorNombre("S3_MontoComprada");
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
    	return "N".equals(esFileMoneda)
        	|| "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub)
    		|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && S3_MontoComprada.compareTo(BigDecimal.ZERO) != 0;
    } 
    
    public boolean validarE306(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
    	final String S3_XR_MonedaVendida = (String) registro.getPorNombre("S3_XR_MonedaVendida"); 	
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
    	return "N".equals(esFileMoneda)
    		|| "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub)
    		|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && CODIGOMONEDA.contains(S3_XR_MonedaVendida);
    }
    
    public boolean validarE307(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
    	final BigDecimal S3_XR_MontoVendida = (BigDecimal) registro.getPorNombre("S3_XR_MontoVendida");
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
    	return "N".equals(esFileMoneda)
        	|| "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub)
    		|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && S3_XR_MontoVendida.compareTo(BigDecimal.ZERO) != 0;
    }

    public boolean validarE308(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_XR_TipoTasaRec = (String) registro.getPorNombre("S3_XR_TipoTasaRec");
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileMoneda)
            || "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub)
            || "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && S3_XR_TIPOTASA.contains(S3_XR_TipoTasaRec);        
    }
    
    public boolean validarE309(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
    	final String S3_XR_TipoTasaEnt = (String) registro.getPorNombre("S3_XR_TipoTasaEnt");
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return  "N".equals(esFileMoneda)
        	|| "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && S3_XR_TIPOTASA.contains(S3_XR_TipoTasaEnt);     
    }   

    public boolean validarE310(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S2_OpsClase = (String) registro.getPorNombre("S2_OpsClase");
        final Date S3_XR_FechaFixing2 = (Date) registro.getPorNombre("S3_XR_FechaFixing2");
        final Date S2_FechaInicio = (Date) registro.getPorNombre("S2_FechaInicio");

        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileMoneda)
        	|| "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) 
        	|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && (!"AM".equals(S2_OpsClase) || !"BE".equals(S2_OpsClase) || !"AS".equals(S2_OpsClase))
        	|| "S".equals(esFileMoneda) && ("AM".equals(S2_OpsClase) || "BE".equals(S2_OpsClase) || "AS".equals(S2_OpsClase))
        		&& S2_FechaInicio.before(S3_XR_FechaFixing2);     
    }
    
    public boolean validarE311(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S2_OpsClase = (String) registro.getPorNombre("S2_OpsClase");
        final Date S3_XR_FechaFixing2 = (Date) registro.getPorNombre("S3_XR_FechaFixing2");
        
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileMoneda)
        	|| "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub) 
        	|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && (!"AM".equals(S2_OpsClase) || !"BE".equals(S2_OpsClase) || !"AS".equals(S2_OpsClase))
        	|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) 
        		&& ("AM".equals(S2_OpsClase) || "BE".equals(S2_OpsClase) || "AS".equals(S2_OpsClase))
        		&& (!S3_XR_FechaFixing2.toString().isEmpty() || !"0001-01-01".equals(S3_XR_FechaFixing2.toString()));     
    }
       
    public boolean validarE312(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_XR_ConvTipoCambio = (String) registro.getPorNombre("S3_XR_ConvTipoCambio");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        String tramo1 = S3_XR_ConvTipoCambio.isEmpty() ? "" : S3_XR_ConvTipoCambio.substring(0, 3);
        String tramo2 = S3_XR_ConvTipoCambio.isEmpty() ? "" : S3_XR_ConvTipoCambio.substring(4, 7);
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileMoneda) 
        	|| "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) 
        		&& (CODIGOMONEDA.contains(tramo1) || CODIGOMONEDA.contains(tramo2));     
    }
    
    public boolean validarE313(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_XR_ConvTipoCambio = (String) registro.getPorNombre("S3_XR_ConvTipoCambio");
         
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileMoneda)
        	|| "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub) 
        	|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && S3_XR_CONVTIPOCAMBIO.contains(S3_XR_ConvTipoCambio);     
    }
    
    public boolean validarE314(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final BigDecimal S3_XR_PuntosForward = (BigDecimal) registro.getPorNombre("S3_XR_PuntosForward");
        
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileMoneda)
        	|| "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && !"FWD".equals(S2_Instrumento)
        	|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && "FWD".equals(S2_Instrumento) && S3_XR_PuntosForward.compareTo(BigDecimal.ZERO) > 0;     
    }
    
    public boolean validarE315(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final BigDecimal S3_R_TasaFija = (BigDecimal) registro.getPorNombre("S3_R_TasaFija");
        
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileMoneda)
            || "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && S3_R_TasaFija.compareTo(BigDecimal.ZERO) == 0;     
    }
    
    public boolean validarE317(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final BigDecimal S3_R_Spread = (BigDecimal) registro.getPorNombre("S3_R_Spread");
        
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileMoneda)
            || "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub)
            || "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && S3_R_Spread.compareTo(BigDecimal.ZERO) == 0;     
    }
    
    public boolean validarE319(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final BigDecimal S3_XR_PuntosForward = (BigDecimal) registro.getPorNombre("S3_XR_PuntosForward");
        
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileMoneda)
            || "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub)
            || "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && (!"FXS".equals(S2_Instrumento) || !"FUT".equals(S2_Instrumento)) 
        	|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && ("FXS".equals(S2_Instrumento) || "FUT".equals(S2_Instrumento)) && S3_XR_PuntosForward.compareTo(BigDecimal.ZERO) != 0;     
    }
    
    public boolean validarE323(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_I_TipoActivoSub = (String) registro.getPorNombre("S3_I_TipoActivoSub");
        
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileMoneda)
        	|| "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub) 
        	|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && S3_I_TipoActivoSub.trim().isEmpty();     
    }
    
    public boolean validarE324(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_I_IdActivoSuby = (String) registro.getPorNombre("S3_I_IdActivoSuby");
        
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileMoneda)
            || "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub) 
            || "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && S3_I_IdActivoSuby.trim().isEmpty();     
    }
    
    public boolean validarE325(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final BigDecimal S3_I_TasaPactada = (BigDecimal) registro.getPorNombre("S3_I_TasaPactada");
        
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileMoneda)
            || "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub) 
        	|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && S3_I_TasaPactada.compareTo(BigDecimal.ZERO) == 0;     
    }
    
    public boolean validarE326(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final BigDecimal S3_PrecioFuturo = (BigDecimal) registro.getPorNombre("S3_PrecioFuturo");
        
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileMoneda)
            || "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub) 
        	|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && S3_PrecioFuturo.compareTo(BigDecimal.ZERO) == 0;     
    }
    
    public boolean validarE327(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_I_PosicionOpe = (String) registro.getPorNombre("S3_I_PosicionOpe");
        
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileMoneda)
            || "S".equals(esFileMoneda) && !"CCS".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileMoneda) && "CCS".equals(S3_XR_OpInstruSub) && S3_I_PosicionOpe.trim().isEmpty();     
    }
    
    public boolean validarE328(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_MonedaComprada = (String) registro.getPorNombre("S3_MonedaComprada");  
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && CODIGOMONEDA.contains(S3_MonedaComprada);     
    }
    
    //REVISAR : Debiese ser monto moneda nocional
    public boolean validarE329(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final BigDecimal S3_MontoComprada = (BigDecimal) registro.getPorNombre("S3_MontoComprada");
        
//        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
//        return "N".equals(esFileTasa)
//            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
//            || "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && S3_MontoComprada.compareTo(BigDecimal.ZERO) != 0;    
        return true;
    }
    
    public boolean validarE330(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_MonedaComprada = (String) registro.getPorNombre("S3_MonedaComprada");        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && CODIGOMONEDA.contains(S3_MonedaComprada);     
    }
    
    public boolean validarE331(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final BigDecimal S3_MontoComprada = (BigDecimal) registro.getPorNombre("S3_MontoComprada");
        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && S3_MontoComprada.compareTo(BigDecimal.ZERO) == 0;     
    }
    
    public boolean validarE332(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_XR_MonedaVendida = (String) registro.getPorNombre("S3_XR_MonedaVendida");
        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && S3_XR_MonedaVendida.trim().isEmpty();     
    }
    
    public boolean validarE333(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final BigDecimal S3_XR_MontoVendida = (BigDecimal) registro.getPorNombre("S3_XR_MontoVendida");
        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && S3_XR_MontoVendida.compareTo(BigDecimal.ZERO) == 0;     
    }
    
    public boolean validarE334(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_XR_TipoTasaRec = (String) registro.getPorNombre("S3_XR_TipoTasaRec");       
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && S3_XR_TIPOTASA.contains(S3_XR_TipoTasaRec);     
    }
    
    public boolean validarE335(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_XR_TipoTasaEnt = (String) registro.getPorNombre("S3_XR_TipoTasaEnt");
        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && S3_XR_TIPOTASA.contains(S3_XR_TipoTasaEnt);     
    }
    
    public boolean validarE336(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final Date S3_XR_FechaFixing2 = (Date) registro.getPorNombre("S3_XR_FechaFixing2");
        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && (S3_XR_FechaFixing2.toString().isEmpty() || "0001-01-01".equals(S3_XR_FechaFixing2.toString()));     
    }
    
    public boolean validarE337(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_XR_ConvTipoCambio = (String) registro.getPorNombre("S3_XR_ConvTipoCambio");
        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && S3_XR_ConvTipoCambio.trim().isEmpty();     
    }
    
    public boolean validarE338(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final BigDecimal S3_PrecioFuturo = (BigDecimal) registro.getPorNombre("S3_PrecioFuturo");
        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && S3_PrecioFuturo.compareTo(BigDecimal.ZERO) == 0;     
    }
    
    public boolean validarE339(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final BigDecimal S3_XR_PuntosForward = (BigDecimal) registro.getPorNombre("S3_XR_PuntosForward");
        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && S3_XR_PuntosForward.compareTo(BigDecimal.ZERO) == 0;     
    }
    
    public boolean validarE340(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_XR_TipoTasaRec = (String) registro.getPorNombre("S3_XR_TipoTasaRec");
        final BigDecimal S3_R_TasaFija = (BigDecimal) registro.getPorNombre("S3_R_TasaFija");
        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
            || "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && !"FIXEDRT".equals(S3_XR_TipoTasaRec.trim()) 
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && "FIXEDRT".equals(S3_XR_TipoTasaRec.trim()) 
        		&& S3_R_TasaFija.compareTo(BigDecimal.ZERO) > 0;     
    }
    
    public boolean validarE341(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_XR_TipoTasaRec = (String) registro.getPorNombre("S3_XR_TipoTasaRec");
        final BigDecimal S3_R_TasaFija = (BigDecimal) registro.getPorNombre("S3_R_TasaFija");
        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
            || "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && "FIXEDRT".equals(S3_XR_TipoTasaRec.trim()) 
            || "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && (!"FIXEDRT".equals(S3_XR_TipoTasaRec.trim()) && S3_XR_TipoTasaRec.isEmpty())
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && (!"FIXEDRT".equals(S3_XR_TipoTasaRec.trim()) && !S3_XR_TipoTasaRec.isEmpty())
        		&& S3_R_TasaFija.compareTo(BigDecimal.ZERO) == 0;     
    }
    
    public boolean validarE342(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_XR_TipoTasaEnt = (String) registro.getPorNombre("S3_XR_TipoTasaEnt");
        final BigDecimal S3_R_TasaFija = (BigDecimal) registro.getPorNombre("S3_R_TasaFija");
        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
            || "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && !"FIXEDRT".equals(S3_XR_TipoTasaEnt.trim()) 
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && "FIXEDRT".equals(S3_XR_TipoTasaEnt.trim())
        	    && S3_R_TasaFija.compareTo(BigDecimal.ZERO) > 0; 
    }
    
    public boolean validarE343(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_XR_TipoTasaEnt = (String) registro.getPorNombre("S3_XR_TipoTasaEnt");
        final BigDecimal S3_R_TasaFija = (BigDecimal) registro.getPorNombre("S3_R_TasaFija");
        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
            || "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && "FIXEDRT".equals(S3_XR_TipoTasaEnt.trim())
            || "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && (!"FIXEDRT".equals(S3_XR_TipoTasaEnt.trim()) && S3_XR_TipoTasaEnt.isEmpty())
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && (!"FIXEDRT".equals(S3_XR_TipoTasaEnt.trim()) && !S3_XR_TipoTasaEnt.isEmpty())
        		&& S3_R_TasaFija.compareTo(BigDecimal.ZERO) == 0;     
    }
    
    public boolean validarE344(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_XR_TipoTasaRec= (String) registro.getPorNombre("S3_XR_TipoTasaRec");
        final String S3_XR_TipoTasaEnt = (String) registro.getPorNombre("S3_XR_TipoTasaEnt");
        final BigDecimal S3_R_Spread = (BigDecimal) registro.getPorNombre("S3_R_Spread");
        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
            || "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && "FIXEDRT".equals(S3_XR_TipoTasaEnt.trim())
            || "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && (!"FIXEDRT".equals(S3_XR_TipoTasaEnt.trim()) && S3_XR_TipoTasaEnt.isEmpty())
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && (!"FIXEDRT".equals(S3_XR_TipoTasaRec.trim()) && !S3_XR_TipoTasaEnt.trim().isEmpty())
        		&& S3_R_Spread.compareTo(BigDecimal.ZERO) >= 0;     
    }
    
    public boolean validarE345(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_XR_TipoTasaRec= (String) registro.getPorNombre("S3_XR_TipoTasaRec");
        final String S3_XR_TipoTasaEnt = (String) registro.getPorNombre("S3_XR_TipoTasaEnt");
        final BigDecimal S3_R_Spread = (BigDecimal) registro.getPorNombre("S3_R_Spread");
        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)
            || "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && "FIXEDRT".equals(S3_XR_TipoTasaEnt.trim())
            || "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && (!"FIXEDRT".equals(S3_XR_TipoTasaEnt.trim()) && S3_XR_TipoTasaEnt.isEmpty())
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && (!"FIXEDRT".equals(S3_XR_TipoTasaRec.trim()) && !S3_XR_TipoTasaEnt.isEmpty())
        		&& S3_R_Spread.compareTo(BigDecimal.ZERO) == 0;     
    }
    
    public boolean validarE348(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
        final String S3_XR_TipoTasaRec = (String) registro.getPorNombre("S3_XR_TipoTasaRec");
        final String S3_XR_TipoTasaEnt = (String) registro.getPorNombre("S3_XR_TipoTasaEnt");
        
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileTasa)
        	|| "S".equals(esFileTasa) && !"SWP".equals(S3_XR_OpInstruSub)	
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && !"FIXEDRT".equals(S3_XR_TipoTasaRec.trim())
        	|| "S".equals(esFileTasa) && "SWP".equals(S3_XR_OpInstruSub) && "FIXEDRT".equals(S3_XR_TipoTasaRec.trim())
        		&& !S3_XR_TipoTasaRec.trim().equals(S3_XR_TipoTasaEnt.trim());     
    }

    public boolean validarE350(final Registro registro) {
        final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final BigDecimal S3_XR_PrecioEntrada = (BigDecimal) registro.getPorNombre("S3_XR_PrecioEntrada");
        final String S3_I_TipoActivoSub = (String) registro.getPorNombre("S3_I_TipoActivoSub");
        
        String esInstrumentoOpciones = S2_INSTRUMENTOOPCIONES.contains(S2_Instrumento) ? "S" : "N";
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";  
        String esFileRf =  S2_Instrumento != null && S2_INSTRUMENTOR.contains(S2_Instrumento) ? "S" : "N";  
        return "N".equals(esInstrumentoOpciones)
        	|| ("S".equals(esInstrumentoOpciones) && ("S".equals(esFileMoneda) || "S".equals(esFileTasa)) && S3_XR_PrecioEntrada.compareTo(BigDecimal.ZERO) != 0)
        	|| ("S".equals(esFileRf) && S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub) && S3_XR_PrecioEntrada.compareTo(S3_XR_PrecioEntrada) == 0);
    }
    
    public boolean validarE351(final Registro registro) {
        final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final BigDecimal S3_XR_PrecioSalida = (BigDecimal) registro.getPorNombre("S3_XR_PrecioSalida");
        final String S3_I_TipoActivoSub = (String) registro.getPorNombre("S3_I_TipoActivoSub");
        
        String esInstrumentoOpciones = S2_INSTRUMENTOOPCIONES.contains(S2_Instrumento) ? "S" : "N";
        String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";  
        String esFileRf =  S2_Instrumento != null && S2_INSTRUMENTOR.contains(S2_Instrumento) ? "S" : "N";  
        return "N".equals(esInstrumentoOpciones)
        	|| ("S".equals(esInstrumentoOpciones) && ("S".equals(esFileMoneda) || "S".equals(esFileTasa)) && S3_XR_PrecioSalida.compareTo(BigDecimal.ZERO) != 0)
        	|| ("S".equals(esFileRf) && S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub) && S3_XR_PrecioSalida.compareTo(BigDecimal.ZERO) == 0);
    }
    
    //REVISAR: Distinguir filetasa
    public boolean validarE352(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_I_TipoActivoSub = (String) registro.getPorNombre("S3_I_TipoActivoSub");
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";
//        return "N".equals(esFileTasa)
//        	|| "S".equals(esFileTasa) && S3_I_TipoActivoSub.trim().isEmpty();    
        return true;
    }
    
    public boolean validarE353(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
        final String S3_I_IdActivoSuby = (String) registro.getPorNombre("S3_I_IdActivoSuby");
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileTasa)
        	|| "S".equals(esFileTasa) && S3_I_IdActivoSuby.trim().isEmpty();     
    }
    
    public boolean validarE354(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_I_TipoActivoSub = (String) registro.getPorNombre("S3_I_TipoActivoSub");
        final BigDecimal S3_I_TasaPactada = (BigDecimal) registro.getPorNombre("S3_I_TasaPactada");
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileTasa)
        	|| "S".equals(esFileTasa) && !S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub)
        	|| "S".equals(esFileTasa) && S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub) && S3_I_TasaPactada.compareTo(BigDecimal.ZERO) == 0;     
    }
    
    public boolean validarE355(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_I_TipoActivoSub = (String) registro.getPorNombre("S3_I_TipoActivoSub");
    	final BigDecimal S3_PrecioFuturo = (BigDecimal) registro.getPorNombre("S3_PrecioFuturo");
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileTasa)
        	|| "S".equals(esFileTasa) && !S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub)
        	|| "S".equals(esFileTasa) && S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub) && S3_PrecioFuturo.compareTo(BigDecimal.ZERO) == 0;     
    }
    
    public boolean validarE356(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_I_TipoActivoSub = (String) registro.getPorNombre("S3_I_TipoActivoSub");
    	final String S3_I_PosicionOpe = (String) registro.getPorNombre("S3_I_PosicionOpe");
        String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileTasa)
            || "S".equals(esFileTasa) && !S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub)
        	|| "S".equals(esFileTasa) && S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub) && S3_I_PosicionOpe.trim().isEmpty();     
    }
    
    public boolean validarE357(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_I_TipoActivoSub = (String) registro.getPorNombre("S3_I_TipoActivoSub");
    	final String S3_XR_OpInstruSub = (String) registro.getPorNombre("S3_XR_OpInstruSub");
    	String esFileRf =  S2_Instrumento != null && S2_INSTRUMENTOR.contains(S2_Instrumento) ? "S" : "N"; 
        return "N".equals(esFileRf)
        	|| "S".equals(esFileRf) && !S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub)
        	|| "S".equals(esFileRf) && S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub) && S3_XR_OpInstruSub.trim().isEmpty();     
    }
    
    public boolean validarE358(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_MonedaComprada = (String) registro.getPorNombre("S3_MonedaComprada");    	
    	String esFileRf =  S2_Instrumento != null && S2_INSTRUMENTOR.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileRf)
        	|| "S".equals(esFileRf) && S3_MonedaComprada.isEmpty()
        	|| "S".equals(esFileRf) && CODIGOMONEDA.contains(S3_MonedaComprada);     
    }
    
    //REVISAR : Debiese ser monto moneda nocional
    public boolean validarE359(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final BigDecimal S3_MontoComprada = (BigDecimal) registro.getPorNombre("S3_MontoComprada");
    	String esFileRf =  S2_Instrumento != null && S2_INSTRUMENTOR.contains(S2_Instrumento) ? "S" : "N";
//        return "N".equals(esFileRf) 
//        	|| "S".equals(esFileRf) && S3_MontoComprada.compareTo(S3_MontoComprada) != 0;     
    	return true;
    }
    
    public boolean validarE370(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_XR_ConvTipoCambio = (String) registro.getPorNombre("S3_XR_ConvTipoCambio");
    	String esFileMoneda =  S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
        return "N".equals(esFileMoneda)
        	|| "S".equals(esFileMoneda)	&& !"FOB".equals(S2_Instrumento)
        	|| "S".equals(esFileMoneda) && "FOB".equals(S2_Instrumento) && "USD/CLP".equals(S3_XR_ConvTipoCambio);     
    }
    
    public boolean validarE376(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_I_TipoActivoSub = (String) registro.getPorNombre("S3_I_TipoActivoSub");
    	String esFileRf =  S2_Instrumento != null && S2_INSTRUMENTOR.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileRf)
        	|| "S".equals(esFileRf) && S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub);     
    }
    
    public boolean validarE377(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_I_IdActivoSuby = (String) registro.getPorNombre("S3_I_IdActivoSuby");
    	String esFileRf =  S2_Instrumento != null && S2_INSTRUMENTOR.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileRf)
        	|| "S".equals(esFileRf) && !S3_I_IdActivoSuby.trim().isEmpty();     
    }
    
    public boolean validarE378(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_I_TipoActivoSub = (String) registro.getPorNombre("S3_I_TipoActivoSub");
    	final BigDecimal S3_I_TasaPactada = (BigDecimal) registro.getPorNombre("S3_I_TasaPactada");
    	String esFileRf =  S2_Instrumento != null && S2_INSTRUMENTOR.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileRf) && S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub)
        	|| "S".equals(esFileRf) && !S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub)
        	|| "S".equals(esFileRf) && S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub) && S3_I_TasaPactada.compareTo(BigDecimal.ZERO) != 0;     
    }
    
    public boolean validarE379(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_I_TipoActivoSub = (String) registro.getPorNombre("S3_I_TipoActivoSub");
    	final BigDecimal S3_PrecioFuturo = (BigDecimal) registro.getPorNombre("S3_PrecioFuturo");
    	String esFileRf =  S2_Instrumento != null && S2_INSTRUMENTOR.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileRf) && S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub)
            || "S".equals(esFileRf) && !S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub)
        	|| "S".equals(esFileRf) && S3_I_TIPOACTIVOSUB.contains(S3_I_TipoActivoSub) && S3_PrecioFuturo.compareTo(BigDecimal.ZERO) != 0;     
    }
    
    public boolean validarE380(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S2_OpPosicionOp = (String) registro.getPorNombre("S2_OpPosicionOp");
    	String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";
        String esInstrumentoOpciones = S2_INSTRUMENTOOPCIONES.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileTasa)
        	|| "N".equals(esInstrumentoOpciones)	
        	|| "S".equals(esInstrumentoOpciones) && "S".equals(esFileTasa) && ("BYER".equals(S2_OpPosicionOp) || "SLLR".equals(S2_OpPosicionOp));
        		    
    }
    
    public boolean validarE381(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S2_OpPosicionOp = (String) registro.getPorNombre("S2_OpPosicionOp");
    	String esFileRf =  S2_Instrumento != null && S2_INSTRUMENTOR.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileRf)
        	|| "S".equals(esFileRf) && !S2_OpPosicionOp.trim().isEmpty();     
    }
    
    public boolean validarE382(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final BigDecimal S3_MontoComprada = (BigDecimal) registro.getPorNombre("S3_MontoComprada");
    	String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileTasa)
        	|| "N".equals(esFileTasa) && !S2_INSTRUMENTOSFECHAINICIOCON.contains(S2_Instrumento)
        	||	"S".equals(esFileTasa) && S2_INSTRUMENTOSFECHAINICIOCON.contains(S2_Instrumento) && S3_MontoComprada.compareTo(BigDecimal.ZERO) != 0;     
    }
    
    public boolean validarE383(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final BigDecimal S3_MontoComprada = (BigDecimal) registro.getPorNombre("S3_MontoComprada");
    	String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileTasa)
        	|| "S".equals(esFileTasa) && !"SWP".equals(S2_Instrumento)
        	|| "S".equals(esFileTasa) && "SWP".equals(S2_Instrumento) && S3_MontoComprada.compareTo(BigDecimal.ZERO) == 0;
    }
    
    //REVISAR - FALTA CAL PAGO
    public boolean validarE384(final Registro registro) {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final Double S3_MontoComprada = ((Number) registro.getPorNombre("S3_MontoComprada")).doubleValue();
    	String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";
        return true;     
    }
    
    public boolean validarE387(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S2_OpsClase = (String) registro.getPorNombre("S2_OpsClase");
    	final Date S2_FechaFixing1 = (Date) registro.getPorNombre("S2_FechaFixing1");
    	final Date S3_XR_FechaFixing2 = (Date) registro.getPorNombre("S3_XR_FechaFixing2");
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
        return "N".equals(esFileMoneda) 
        	|| "S".equals(esFileMoneda) && (!"AM".equals(S2_OpsClase) || !"BE".equals(S2_OpsClase) || !"AS".equals(S2_OpsClase))
        	|| "S".equals(esFileMoneda) && ("AM".equals(S2_OpsClase) || "BE".equals(S2_OpsClase) || "AS".equals(S2_OpsClase))
        		&& S3_XR_FechaFixing2.before(S2_FechaFixing1);     
    }
    
    public boolean validarE389(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_XR_TipoTasaRec = (String) registro.getPorNombre("S3_XR_TipoTasaRec");
    	final String S3_XR_TipoTasaEnt = (String) registro.getPorNombre("S3_XR_TipoTasaEnt");
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
        return "N".equals(esFileMoneda)
        	|| "S".equals(esFileMoneda) && !S2_INSTRUMENTOSNOTIPOTASAENTRREC.contains(S2_Instrumento)
        	|| "S".equals(esFileMoneda) && S2_INSTRUMENTOSNOTIPOTASAENTRREC.contains(S2_Instrumento)
        		&& (S3_XR_TipoTasaRec.isEmpty() || S3_XR_TipoTasaEnt.isEmpty());     
    }
    
    public boolean validarE390(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_MonedaComprada = (String) registro.getPorNombre("S3_MonedaComprada");
    	final String S3_XR_MonedaVendida = (String) registro.getPorNombre("S3_XR_MonedaVendida");    	
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
        return "N".equals(esFileMoneda)
        	|| "S".equals(esFileMoneda) && !"FOB".equals(S2_Instrumento)  
        	|| "S".equals(esFileMoneda) && "FOB".equals(S2_Instrumento)  
        		&& (("CLP".equals(S3_MonedaComprada) || "USD".equals(S3_MonedaComprada))
        		|| ("CLP".equals(S3_XR_MonedaVendida) || "USD".equals(S3_XR_MonedaVendida)));
    }
    
    public boolean validarE392(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final BigDecimal S3_MontoComprada = (BigDecimal) registro.getPorNombre("S3_MontoComprada");
    	final BigDecimal S3_XR_MontoVendida = (BigDecimal) registro.getPorNombre("S3_XR_MontoVendida");  	
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";  
        return "N".equals(esFileMoneda) && "FOB".equals(S2_Instrumento)  
        	|| "S".equals(esFileMoneda) && !"FOB".equals(S2_Instrumento)
        	|| "S".equals(esFileMoneda) && "FOB".equals(S2_Instrumento)  
        		&& S3_MontoComprada.compareTo(BigDecimal.ZERO) != 0 && S3_XR_MontoVendida.compareTo(BigDecimal.ZERO) != 0;
    }
    
    //REVISAR - DJAUX
    public boolean validarE394(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";  
        return true;
    }
    
    //REVISAR - calrspread y calespread
    public boolean validarE405(final Registro registro) throws ParseException {
    	return true;
    }
    
    //REVISAR - calmonedainter
    public boolean validarE406(final Registro registro) throws ParseException {
    	return true;
    }
    
    //REVISAR - calrec y calent
    public boolean validarE412(final Registro registro) throws ParseException {
    	return true;
    }
    
    public boolean validarE417(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S2_ModalidadLiq = (String) registro.getPorNombre("S2_ModalidadLiq");
    	final String S2_FechaFixing1 = (String) registro.getPorNombre("S2_Instrumento");
    	String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";  
        return "N".equals(esFileTasa)
        	|| "S".equals(esFileTasa) && !"OTR".equals(S2_Instrumento)
        	|| "S".equals(esFileTasa) && "OTR".equals(S2_Instrumento)
        	|| "S".equals(esFileTasa) && "OTR".equals(S2_Instrumento) && (!"CO".equals(S2_ModalidadLiq) && !"EF".equals(S2_ModalidadLiq))
        	|| "S".equals(esFileTasa) && "OTR".equals(S2_Instrumento)
        		&& (("CO".equals(S2_ModalidadLiq) && !S2_FechaFixing1.trim().isEmpty()) || ("EF".equals(S2_ModalidadLiq) && S2_FechaFixing1.trim().isEmpty()));
    }
    
    public boolean validarE418(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S2_ModalidadLiq = (String) registro.getPorNombre("S2_ModalidadLiq");
    	final String S2_FechaFixing1 = (String) registro.getPorNombre("S2_Instrumento");
    	String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";  
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return "N".equals(esFileTasa) && "N".equals(esFileMoneda)
        	|| ("S".equals(esFileTasa) || "S".equals(esFileMoneda)) && !"SWP".equals(S2_Instrumento)
        	|| ("S".equals(esFileTasa) || "S".equals(esFileMoneda)) && "SWP".equals(S2_Instrumento) && (!"CO".equals(S2_ModalidadLiq) && !"EF".equals(S2_ModalidadLiq))
        	|| ("S".equals(esFileTasa) || "S".equals(esFileMoneda)) && "SWP".equals(S2_Instrumento) 
        		&& (("CO".equals(S2_ModalidadLiq) && !S2_FechaFixing1.trim().isEmpty()) || ("EF".equals(S2_ModalidadLiq) && S2_FechaFixing1.trim().isEmpty()));
    }
    
    //REVISAR - VALIDACION RARA
    public boolean validarE419(final Registro registro) throws ParseException {
    	final String S2_Instrumento = (String) registro.getPorNombre("S2_Instrumento");
    	final String S3_MonedaComprada = (String) registro.getPorNombre("S3_MonedaComprada");
    	String esFileTasa = S2_Instrumento != null && S2_INSTRUMENTOI.contains(S2_Instrumento) ? "S" : "N";  
    	String esFileMoneda = S2_Instrumento != null && S2_INSTRUMENTOX.contains(S2_Instrumento) ? "S" : "N";
        return true;
    }
    
    //REVISAR - calsinfechainicio
    public boolean validarE420(final Registro registro) throws ParseException {
    	return true;
    }
    
    //REVISAR - calsinfechatermino
    public boolean validarE421(final Registro registro) throws ParseException {
    	return true;
    }
    
    //REVISAR - caler + pager + garaner + detgarer
    public boolean validarE422(final Registro registro) throws ParseException {
    	return true;
    }
    
    //REVISAR - caltasafent
    public boolean validarE424(final Registro registro) throws ParseException {
    	return true;
    }
    
    //REVISAR - caltasafrec
    public boolean validarE425(final Registro registro) throws ParseException {
    	return true;
    }
    
    //REVISAR - caltasavent
    public boolean validarE426(final Registro registro) throws ParseException {
    	return true;
    }
    
    //REVISAR - caltasavrec
    public boolean validarE427(final Registro registro) throws ParseException {
    	return true;
    }
    
    //REVISAR - calfactorvent
    public boolean validarE430(final Registro registro) throws ParseException {
    	return true;
    }
    
    //REVISAR - calfactorvrec
    public boolean validarE431(final Registro registro) throws ParseException {
    	return true;
    }
    
    //REVISAR - cal
    public boolean validarE432(final Registro registro) throws ParseException {
    	return true;
    }
    
    //REVISAR - cal
    public boolean validarE433(final Registro registro) throws ParseException {
    	return true;
    }
    
 	//REVISAR - calfecinimayorfecter
    public boolean validarE435(final Registro registro) throws ParseException {
    	return true;
    }
    
    //REVISAR - calfecfixmayorfecter
    public boolean validarE436(final Registro registro) throws ParseException {
    	return true;
    }

    public boolean validarE437(final Registro registro) throws ParseException {
    	final String S3_MonedaComprada = (String) registro.getPorNombre("S3_MonedaComprada"); 
        final String S3_XR_MonedaVendida = (String) registro.getPorNombre("S3_XR_MonedaVendida");
        final BigDecimal S3_MontoComprada = (BigDecimal) registro.getPorNombre("S3_MontoComprada");
        final BigDecimal S3_XR_MontoVendida = (BigDecimal) registro.getPorNombre("S3_XR_MontoVendida");

        if ("CLP".equalsIgnoreCase(S3_MonedaComprada) && "USD".equalsIgnoreCase(S3_XR_MonedaVendida)) {
            return S3_MontoComprada.compareTo(S3_XR_MontoVendida) > 0;
        }
        return true;
    }
    
    public boolean validarE438(final Registro registro) throws ParseException {
    	final String S3_MonedaComprada = (String) registro.getPorNombre("S3_MonedaComprada"); 
        final String S3_XR_MonedaVendida = (String) registro.getPorNombre("S3_XR_MonedaVendida");
        final BigDecimal S3_MontoComprada = (BigDecimal) registro.getPorNombre("S3_MontoComprada");
        final BigDecimal S3_XR_MontoVendida = (BigDecimal) registro.getPorNombre("S3_XR_MontoVendida");

         if ("USD".equalsIgnoreCase(S3_MonedaComprada) && "CLP".equalsIgnoreCase(S3_XR_MonedaVendida)) {
            return S3_XR_MontoVendida.compareTo(S3_MontoComprada) > 0;
        }
        return true;
    }

    public boolean validarE439(final Registro registro) throws ParseException {
    	final String S3_MonedaComprada = (String) registro.getPorNombre("S3_MonedaComprada"); 
        final String S3_XR_MonedaVendida = (String) registro.getPorNombre("S3_XR_MonedaVendida");
        final BigDecimal S3_MontoComprada = (BigDecimal) registro.getPorNombre("S3_MontoComprada");
        final BigDecimal S3_XR_MontoVendida = (BigDecimal) registro.getPorNombre("S3_XR_MontoVendida");

        if ("CLP".equalsIgnoreCase(S3_MonedaComprada) && "CLF".equalsIgnoreCase(S3_XR_MonedaVendida)) {
            return S3_MontoComprada.compareTo(S3_XR_MontoVendida) > 0;
        }
        return true;
    }
    
    public boolean validarE440(final Registro registro) throws ParseException {
    	final String S3_MonedaComprada = (String) registro.getPorNombre("S3_MonedaComprada"); 
        final String S3_XR_MonedaVendida = (String) registro.getPorNombre("S3_XR_MonedaVendida");
        final BigDecimal S3_MontoComprada = (BigDecimal) registro.getPorNombre("S3_MontoComprada");
        final BigDecimal S3_XR_MontoVendida = (BigDecimal) registro.getPorNombre("S3_XR_MontoVendida");

        if ("CLF".equalsIgnoreCase(S3_MonedaComprada) && "CLP".equalsIgnoreCase(S3_XR_MonedaVendida)) {
            return S3_XR_MontoVendida.compareTo(S3_MontoComprada) > 0;
        }
        return true;
    }

    public boolean validarE441(final Registro registro) throws ParseException {
    	final String S3_MonedaComprada = (String) registro.getPorNombre("S3_MonedaComprada"); 
        final String S3_XR_TipoTasaEnt = (String) registro.getPorNombre("S3_XR_TipoTasaEnt");
        final String S3_XR_TipoTasaRec = (String) registro.getPorNombre("S3_XR_TipoTasaRec");

        return !((("TNAICP0".equalsIgnoreCase(S3_XR_TipoTasaRec) && "FIXEDRT".equalsIgnoreCase(S3_XR_TipoTasaEnt))
                    || ("TNAICP0".equalsIgnoreCase(S3_XR_TipoTasaEnt) && "FIXEDRT".equalsIgnoreCase(S3_XR_TipoTasaRec)))
                ) || "CLP".equalsIgnoreCase(S3_MonedaComprada);
    }

    public boolean validarE442(final Registro registro) throws ParseException {
    	final String S3_MonedaComprada = (String) registro.getPorNombre("S3_MonedaComprada"); 
        final String S3_XR_TipoTasaEnt = (String) registro.getPorNombre("S3_XR_TipoTasaEnt");
        final String S3_XR_TipoTasaRec = (String) registro.getPorNombre("S3_XR_TipoTasaRec");

        return !((("TRAICP0".equalsIgnoreCase(S3_XR_TipoTasaRec) && "FIXEDRT".equalsIgnoreCase(S3_XR_TipoTasaEnt))
                    || ("TRAICP0".equalsIgnoreCase(S3_XR_TipoTasaEnt) && "FIXEDRT".equalsIgnoreCase(S3_XR_TipoTasaRec)))
                ) || "CLF".equalsIgnoreCase(S3_MonedaComprada);
    }

    public boolean validarE443(final Registro registro) throws ParseException {
    	final String S3_MonedaComprada = (String) registro.getPorNombre("S3_MonedaComprada"); 
        final String S3_XR_TipoTasaEnt = (String) registro.getPorNombre("S3_XR_TipoTasaEnt");
        final String S3_XR_TipoTasaRec = (String) registro.getPorNombre("S3_XR_TipoTasaRec");

        return !((("SOFRATE".equalsIgnoreCase(S3_XR_TipoTasaRec) && "FIXEDRT".equalsIgnoreCase(S3_XR_TipoTasaEnt))
                    || ("SOFRATE".equalsIgnoreCase(S3_XR_TipoTasaEnt) && "FIXEDRT".equalsIgnoreCase(S3_XR_TipoTasaRec)))
                ) || "USD".equalsIgnoreCase(S3_MonedaComprada);
    }

    public boolean validarE444(final Registro registro) throws ParseException {
    	final Date S2_FechaEfecTermino = (Date) registro.getPorNombre("S2_FechaEfecTermino");
        final Date S2_FechaTermino = (Date) registro.getPorNombre("S2_FechaTermino");

        if (!("0001-01-01").equals(S2_FechaEfecTermino.toString())){
            return !S2_FechaEfecTermino.equals(S2_FechaTermino);
        }
        return true;    
    }

    public Date getFechaSistema() throws Exception {
    	String fechaSistemaString = ConfiguracionSistema.getParametroConfiguracionString(cnn, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
    	Date fechaSistemaDate = Fecha.toDate(fechaSistemaString);
        return fechaSistemaDate;
    }
    
    public boolean validarRut(String rutValidacion) {
    	final String[] rut = rutValidacion.split("-"); 	
    	if(rut.length != 2) return false;
    	List<String> numeros = Arrays.asList(rut[0].split(""));
    	String verificador = rut[1];
    	Collections.reverse(numeros);
    	int a = 2;
    	int resultado = 0;
    	for(String numero : numeros){
    		if(numero.isEmpty()) continue;
    		int num = Integer.parseInt(numero);
    		resultado += num * a;
    		if(a == 7){
    			a = 1;
    		}
    		a++;
    	}
    	int resto = resultado % 11;
    	int digito = 11 - resto;
    	String resultadoVerificador = String.valueOf(digito);
    	if(digito == 11) resultadoVerificador = "0";
    	if(digito == 10) resultadoVerificador = "k";
    	return resultadoVerificador.equalsIgnoreCase(verificador);
    }
}
