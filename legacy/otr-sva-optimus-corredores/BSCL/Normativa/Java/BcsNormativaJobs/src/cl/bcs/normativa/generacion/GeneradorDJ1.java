package cl.bcs.normativa.generacion;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import cl.bcs.normativa.dal.ConfiguracionDAO;
import cl.bcs.normativa.data.Registro;
import cl.bcs.normativa.registros.RegistroDJ1;
import cl.bcs.plataforma.pla.utils.FileLoader;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.SQL;

/**
 * Implementaci&oacute;n del generador de la DJ1.
 *
 */
public final class GeneradorDJ1 extends Generador {
	private static final List<String> S1_EVENTOREPORTERECTIFICA = Arrays.asList("RVE", "RVI", "RNU", "REL");
	private static final List<String> S2_INSTRUMENTOX = Arrays.asList("CAL", "CCS", "FLR", "FOB", "FUT", "FWD", "FXS","OTR", "PUT", "STN", "SWP");
	private static final List<String> S2_INSTRUMENTOR = Arrays.asList("CAP", "FUT", "FWD", "OTR", "STN");
	private static final List<String> S2_INSTRUMENTOI = Arrays.asList("FUT", "FWD", "OTR");

	
	private static FileLoader loader = new FileLoader();
	private final String SELECT_BOLSONES_BY_CABECERA_SECCION = loader.loadResourceToString("cl/bcs/normativa/jobs/sql/GeneradorDJ1_Select_Bolson_By_Cabecera_Seccion.sql");

	public static class Bolson extends BcsPojo {
		public Long KS4_XR_NroFlu;
		public String S4_XR_TipoFlujo;
		public BigDecimal S4_XR_FacTasa;
		public BigDecimal S4_XR_SpreadTasa;
		public BigDecimal S4_XR_TasaFija;
		public Date S4_XR_FecFixTasa;
		public Date S4_XR_FecIni;
		public Date S4_XR_FecTer;
		public BigDecimal S4_XR_MontoNoc;
		public BigDecimal S4_XR_MontoInt;
		public String S4_X_MonMonto;
		public BigDecimal KS5_CorrPago;
		public String KS5_IdPago;
		public Date S5_FecPago;
		public BigDecimal S5_MontoPago;
		public String S5_TipoPago;
		public String S5_CodMoneda;
		public String KS6_IdGar;
		public String S6_TipoGar;
		public String S6_MonVal;
		public BigDecimal S6_MontoUmb;
		public BigDecimal S6_ValGtia;
		public String S7_TipoGar;
		public String KS7_IdGar;
		public String S7_TipoActGar;
		public BigDecimal S7_PorcGar;
	}

	/**
	 * Registro resumen de la DJ.
	 */
	private RegistroDJ1 registroResumen;
	private long totalRegistros;
	private String delimitador = "";
	/**
	 * Constructor.
	 *
	 * @param contexto El contexto de generaci&oacute;n.
	 * @throws SQLException          En caso de error SQL.
	 * @throws IOException           En caso de error de I/O.
	 * @throws SerializadorException En caso de error al serializar.
	 */
	public GeneradorDJ1(final ContextoGeneracion contexto) throws SQLException, IOException, SerializadorException {
		super(contexto);
		this.totalRegistros = 0;
		this.registroResumen = new RegistroDJ1();
		this.delimitador = ConfiguracionDAO.buscarPorEmpresaMasCodigo(contexto.getConnection(), contexto.getSchema(), contexto.getEmpresa().getId(), "DJ1_pDelimitador");
	}

	@Override
	public void generarDetalle(final Registro registro) throws SerializadorException {
		// No se implementa
	}

	@Override
	public void generarEncabezado() throws SerializadorException {
		//No se implementa
	}

	@Override
	public String generarRegistroTipo0() {
		return "";
	}

	@Override
	public void generarEncabezadoRegistro(final Registro registro, String tipoArchivo, String periodicidad, String rutDeclarante, Date fechaHasta) throws SerializadorException {
		RegistroDJ1 registroDJ1 = (registro==null) ? null : new RegistroDJ1(registro);
		String fecha = "";
		if("D".equals(periodicidad)) {
			fecha = registroDJ1.getkPeriodoComercial().toString().replace("-","");
		}
		
		if("M".equals(periodicidad)){
			DateFormat formatoFecha = new SimpleDateFormat("yyyyMMdd");
			fecha = formatoFecha.format(fechaHasta);
		}
		
		String encabezado = rutDeclarante.replace("-", "") + periodicidad + tipoArchivo + fecha;
		println(encabezado);
	}
	
	@Override
    public void generarEncabezadoRegistro(String fechaPeriodoComercial, String tipoArchivo, String periodicidad, String rutDeclarante, Date fechaHasta) throws SerializadorException {
		String fecha = "";
		DateFormat formatoFecha = new SimpleDateFormat("yyyyMMdd");
		
		if("D".equals(periodicidad)) {
			fecha = fechaPeriodoComercial.replace("-", "");
		}
		
		if("M".equals(periodicidad) || "C".equals(periodicidad)){
			fecha = formatoFecha.format(fechaHasta);
		}
		
		String encabezado = rutDeclarante.replace("-", "") + periodicidad + tipoArchivo + fecha;
		println(encabezado);
    };
	
	@Override
	public List<SeccionLinea> generarDetalleDJ1(final Registro registro, String tipoArchivo, Connection conn, String periodicidad, Date fechaHasta)
			throws SerializadorException, SQLException {
		final RegistroDJ1 registroDJ1 = new RegistroDJ1(registro);
		
		List<SeccionLinea> lstSeccionLinea = new ArrayList<SeccionLinea>();
		
		String eventoReporte = registroDJ1.getS1_EventoReporte();
		
		if("M".equals(periodicidad)){
			eventoReporte = "MVI";
			
			if(("NUE".equals(registroDJ1.getS1_EventoReporte()) || "OMD".equals(registroDJ1.getS1_EventoReporte()))) {
				if(registroDJ1.getS2_FechaTermino().toString().equals("0001-01-01")) {
					eventoReporte = "MVI";
				}else if(registroDJ1.getS2_FechaTermino().before(fechaHasta) || registroDJ1.getS2_FechaTermino().equals(fechaHasta)) {
					eventoReporte = "MVE";
				}
			}
				
			if(("CES".equals(registroDJ1.getS1_EventoReporte()) || "AOC".equals(registroDJ1.getS1_EventoReporte()))) {
				eventoReporte = "MVE";
			}
			
			if (S1_EVENTOREPORTERECTIFICA.contains(registroDJ1.getS1_EventoReporte())) {
				eventoReporte = registroDJ1.getS1_EventoReporte();
			} else if ("S".equals(registroDJ1.getRectifica())) {
				eventoReporte = "MVI".equals(eventoReporte) ? "RVI" : "RVE";
			}
		}
		
		if (S2_INSTRUMENTOX.contains(registroDJ1.getS2_Instrumento()) && "FX".equals(tipoArchivo)) {
			lstSeccionLinea.add(new SeccionLinea(1, generarRegistroTipo1(registro, tipoArchivo, periodicidad, fechaHasta)));
			
			if (!("REL".equals(registroDJ1.getS1_EventoReporte())))
			{
				lstSeccionLinea.add(new SeccionLinea(2, generarRegistroTipo2(registro, tipoArchivo)));
				lstSeccionLinea.add(new SeccionLinea(3, generarRegistroTipo3(registro, tipoArchivo)));
				
				if(!("FUT").equals(registroDJ1.getS2_Instrumento()) && !("FWD").equals(registroDJ1.getS2_Instrumento()) 
						&& !("CAP").equals(registroDJ1.getS2_Instrumento()) && !("FLR").equals(registroDJ1.getS2_Instrumento())) {
					lstSeccionLinea.addAll(generarRegistroTipo4(registro, tipoArchivo, conn));
				}
				
				if("M".equals(periodicidad)) {
					lstSeccionLinea.addAll(generarRegistroTipo5(registro, tipoArchivo, conn));
					lstSeccionLinea.addAll(generarRegistroTipo6(registro, tipoArchivo, conn));
					lstSeccionLinea.addAll(generarRegistroTipo7(registro, tipoArchivo, conn));
					if(!(("MVE").equals(eventoReporte) || ("RVE").equals(eventoReporte)) && registroDJ1.getS8_ValorMercado()!=0) {
						lstSeccionLinea.add(new SeccionLinea(8, generarRegistroTipo8(registro, tipoArchivo)));
					}			
				}
			}
		}

		if (S2_INSTRUMENTOR.contains(registroDJ1.getS2_Instrumento()) && "IR".equals(tipoArchivo)) {
			lstSeccionLinea.add(new SeccionLinea(1, generarRegistroTipo1(registro, tipoArchivo, periodicidad, fechaHasta)));
			
			if (!("REL".equals(registroDJ1.getS1_EventoReporte())))
			{
				lstSeccionLinea.add(new SeccionLinea(2, generarRegistroTipo2(registro, tipoArchivo)));
				lstSeccionLinea.add(new SeccionLinea(3, generarRegistroTipo3(registro, tipoArchivo)));
				
				if(!("FUT").equals(registroDJ1.getS2_Instrumento()) && !("FWD").equals(registroDJ1.getS2_Instrumento()) 
						&& !("CAP").equals(registroDJ1.getS2_Instrumento()) && !("FLR").equals(registroDJ1.getS2_Instrumento())) {
					lstSeccionLinea.addAll(generarRegistroTipo4(registro, tipoArchivo, conn));
				}
				
				if("M".equals(periodicidad)) {
					lstSeccionLinea.addAll(generarRegistroTipo5(registro, tipoArchivo, conn));
					lstSeccionLinea.addAll(generarRegistroTipo6(registro, tipoArchivo, conn));
					lstSeccionLinea.addAll(generarRegistroTipo7(registro, tipoArchivo, conn));
					if(!(("MVE").equals(eventoReporte) || ("RVE").equals(eventoReporte)) && registroDJ1.getS8_ValorMercado()!=0) {
						lstSeccionLinea.add(new SeccionLinea(8, generarRegistroTipo8(registro, tipoArchivo)));
					}
				}
			}
		}

		if (S2_INSTRUMENTOI.contains(registroDJ1.getS2_Instrumento()) && "FI".equals(tipoArchivo)) {
			lstSeccionLinea.add(new SeccionLinea(1, generarRegistroTipo1(registro, tipoArchivo, periodicidad, fechaHasta)));
			
			if (!("REL".equals(registroDJ1.getS1_EventoReporte())))
			{
				lstSeccionLinea.add(new SeccionLinea(2, generarRegistroTipo2(registro, tipoArchivo)));
				lstSeccionLinea.add(new SeccionLinea(3, generarRegistroTipo3(registro, tipoArchivo)));
				if("M".equals(periodicidad)) {
					lstSeccionLinea.addAll(generarRegistroTipo5(registro, tipoArchivo, conn));
					lstSeccionLinea.addAll(generarRegistroTipo6(registro, tipoArchivo, conn));
					lstSeccionLinea.addAll(generarRegistroTipo7(registro, tipoArchivo, conn));
					if(!(("MVE").equals(eventoReporte) || ("RVE").equals(eventoReporte)) && registroDJ1.getS8_ValorMercado()!=0) {
						lstSeccionLinea.add(new SeccionLinea(8, generarRegistroTipo8(registro, tipoArchivo)));
					}
				}
			}
		}
		return lstSeccionLinea;
	}

	@Override
	public void imprimirLineasDJ1(List<SeccionLinea> lstSeccionLinea) throws  SerializadorException, SQLException {
		List<SeccionLinea> sortedListSeccionLinea = lstSeccionLinea.stream()
	            .sorted(Comparator.comparingInt(x -> x.Seccion))
	            .collect(Collectors.toList());
		
		if(!sortedListSeccionLinea.isEmpty()) {
			sortedListSeccionLinea.forEach(x -> println(x.Linea));
		}
	}
	
	public String generarRegistroTipo1(final Registro registro, String tipoArchivo, String periodicidad, Date fechaHasta) throws SerializadorException {
		final RegistroDJ1 registroDJ1 = new RegistroDJ1(registro);

		String ret = "1";

		ret += delimitador + registroDJ1.getkRutCtraparte1().replace("-", "");
		ret += delimitador + registroDJ1.getkIdContrato();
		ret += delimitador + registroDJ1.getkFechaContrato() +  "T" + registroDJ1.getHoraContrato();
		if("FX".equals(tipoArchivo) || "IR".equals(tipoArchivo)) {
			ret += delimitador + registroDJ1.getkIdOpeEstruc();
		}
		
		if (!("REL".equals(registroDJ1.getS1_EventoReporte()))) {
			ret += delimitador + registroDJ1.getS1_NaturaInfo();	
		}
		else {
			ret += delimitador;	
		}
		
		String eventoReporte = registroDJ1.getS1_EventoReporte();
		
		if("M".equals(periodicidad)){
			eventoReporte = "MVI";
			
			if(("NUE".equals(registroDJ1.getS1_EventoReporte()) || "OMD".equals(registroDJ1.getS1_EventoReporte()))) {
				if(registroDJ1.getS2_FechaTermino().toString().equals("0001-01-01")) {
					eventoReporte = "MVI";
				}else if(registroDJ1.getS2_FechaTermino().before(fechaHasta) || registroDJ1.getS2_FechaTermino().equals(fechaHasta)) {
					eventoReporte = "MVE";
				}
			}
				
			if(("CES".equals(registroDJ1.getS1_EventoReporte()) || "AOC".equals(registroDJ1.getS1_EventoReporte()))) {
				eventoReporte = "MVE";
			}
			
			if (S1_EVENTOREPORTERECTIFICA.contains(registroDJ1.getS1_EventoReporte())) {
				eventoReporte = registroDJ1.getS1_EventoReporte();
			} else if ("S".equals(registroDJ1.getRectifica())) {
				eventoReporte = "MVI".equals(eventoReporte) ? "RVI" : "RVE";
			}
		}
		
		ret += delimitador + eventoReporte;
		
		if (!("REL".equals(eventoReporte)))
		{
			ret += delimitador + registroDJ1.getS1_LEICtraparte1();
			ret += delimitador + registroDJ1.getS1_RutCtraparte2().replace("-", "");
			ret += delimitador + registroDJ1.getS1_LEICtraparte2();
			ret += delimitador + registroDJ1.getS1_NomCtraparte2();
			ret += delimitador + registroDJ1.getS1_PaisCtraparte2();
			ret += delimitador + registroDJ1.getS1_RutEntidad().replace("-", "");
			ret += delimitador + registroDJ1.getS1_LEIEntidad();
			ret += delimitador + registroDJ1.getS1_RUTLiqCtraparte1().replace("-", "");
			ret += delimitador + registroDJ1.getS1_LEILiqCtraparte1();
			ret += delimitador + registroDJ1.getS1_RutAgenteCal().replace("-", "");
			ret += delimitador + registroDJ1.getS1_LEIAgenteCal();
			ret += delimitador + registroDJ1.getS1_NomAgenteCal();
			ret += delimitador + registroDJ1.getS1_RutCedente().replace("-", "");
			ret += delimitador + registroDJ1.getS1_LEICedente();
			ret += delimitador + registroDJ1.getS1_PlataformaNego();
			ret += delimitador + registroDJ1.getS1_RutIntermediario().replace("-", "");
			ret += delimitador + registroDJ1.getS1_LEIIntermediario();	
		} else {
			for (int i=0;i<17;i++) {
				ret += delimitador;	
			}
		}

		return ret;
	}

	public String generarRegistroTipo2(final Registro registro, String tipoArchivo) throws SerializadorException {
		final RegistroDJ1 registroDJ1 = new RegistroDJ1(registro);
		DateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");

		String ret = "2";
		
		ret += delimitador + registroDJ1.getkRutCtraparte1().replace("-", "");
		ret += delimitador + registroDJ1.getkIdContrato();
		ret += delimitador + registroDJ1.getkFechaContrato() +  "T" + registroDJ1.getHoraContrato();
		if("FX".equals(tipoArchivo) || "IR".equals(tipoArchivo)) {
			ret += delimitador + registroDJ1.getkIdOpeEstruc();
		}
		ret += delimitador + registroDJ1.getS2_Instrumento();
		ret += delimitador + registroDJ1.getS2_IdContratoAgente();
		ret += delimitador + registroDJ1.getS2_OpsClase();
		ret += delimitador + registroDJ1.getS2_OpPosicionOp();
		ret += delimitador + registroDJ1.getS2_ModalidadLiq();
		String monedaComp = ("EF").equals(registroDJ1.getS2_ModalidadLiq()) ? "" : registroDJ1.getS2_MonedaComp(); 
		ret += delimitador + monedaComp;
		String getS2_FechaInicio = registroDJ1.getS2_FechaInicio().toString().equals("0001-01-01") ? "1900-01-01" : formatoFecha.format(registroDJ1.getS2_FechaInicio()).toString();
		ret += delimitador + getS2_FechaInicio;
		String getS2_FechaFixing1 = registroDJ1.getS2_FechaFixing1().toString().equals("0001-01-01") ? "" : formatoFecha.format(registroDJ1.getS2_FechaFixing1()).toString();
		ret += delimitador + getS2_FechaFixing1;
		String getS2_FechaTermino = registroDJ1.getS2_FechaTermino().toString().equals("0001-01-01") ? "1990-01-01" : formatoFecha.format(registroDJ1.getS2_FechaTermino()).toString();
		ret += delimitador + getS2_FechaTermino;
		String getS2_FechaPagOpe = registroDJ1.getS2_FechaPagOpe().toString().equals("0001-01-01") ? "1990-01-01" : formatoFecha.format(registroDJ1.getS2_FechaPagOpe()).toString();
		ret += delimitador + getS2_FechaPagOpe;
		String getS2_FechaInicioMod = registroDJ1.getS2_FechaInicioMod().toString().equals("0001-01-01") ? "" : formatoFecha.format(registroDJ1.getS2_FechaInicioMod()).toString();
		ret += delimitador + getS2_FechaInicioMod;
		String monedaPrima = ("FWD").equals(registroDJ1.getkProducto()) ? "" : registroDJ1.getS2_MonedaPrima();
		ret += delimitador + monedaPrima;
		String montoPrima = ("FWD").equals(registroDJ1.getkProducto()) ? "" : String.format("%.5f", registroDJ1.getS2_MontoPrima());
		ret += delimitador + montoPrima; //Monto Prima Entrega
		ret += delimitador + montoPrima; //Monto Prima Recibe
		ret += delimitador + registroDJ1.getS2_ObjetivoOpe();
		ret += delimitador + registroDJ1.getS2_ConvenioMarco();
		ret += delimitador + registroDJ1.getS2_PaisContrato();
		ret += delimitador + registroDJ1.getS2_Garantias();
		ret += delimitador + registroDJ1.getS2_ClausulaRecou();
		ret += delimitador + registroDJ1.getS2_ClausulaTermino();
		String getS2_FechaEfecTermino = registroDJ1.getS2_FechaEfecTermino().toString().equals("0001-01-01") ? "" : formatoFecha.format(registroDJ1.getS2_FechaEfecTermino()).toString();
		ret += delimitador + getS2_FechaEfecTermino;
		ret += delimitador + registroDJ1.getS2_CompresionCartera();
		ret += delimitador + registroDJ1.getS2_NumTotIntercam();
		ret += delimitador + registroDJ1.getS2_NumTotPagos();

		return ret;
	}

	public String generarRegistroTipo3(final Registro registro, String tipoArchivo) throws SerializadorException {
		final RegistroDJ1 registroDJ1 = new RegistroDJ1(registro);
		DateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		
		String ret = "3";
		
		ret += delimitador + registroDJ1.getkRutCtraparte1().replace("-", "");
		ret += delimitador + registroDJ1.getkIdContrato();
		ret += delimitador + registroDJ1.getkFechaContrato() +  "T" + registroDJ1.getHoraContrato();
		
		if("FX".equals(tipoArchivo) || "IR".equals(tipoArchivo)) {
			ret += delimitador + registroDJ1.getkIdOpeEstruc();
			ret += delimitador + registroDJ1.getS3_XR_OpInstruSub();
		}
		
		String monedaComprada = registroDJ1.getS3_MonedaComprada().equals("UF")?"CLF":registroDJ1.getS3_MonedaComprada();
		ret += delimitador + monedaComprada;
		ret += delimitador + String.format("%.5f", registroDJ1.getS3_MontoComprada());
		
		if("FX".equals(tipoArchivo)) {
			String monedaVendida = registroDJ1.getS3_XR_MonedaVendida().equals("UF")?"CLF":registroDJ1.getS3_XR_MonedaVendida();
			ret += delimitador + monedaVendida;
			ret += delimitador + String.format("%.5f", registroDJ1.getS3_XR_MontoVendida());
			
			String tipoTasaRecibe = registroDJ1.getS3_XR_TipoTasaRec();
			String tipoTasaEntrega = registroDJ1.getS3_XR_TipoTasaEnt();
			
			if(("FWD").equals(registroDJ1.getS2_Instrumento()) || ("FUT").equals(registroDJ1.getS2_Instrumento()) 
				|| ("FOB").equals(registroDJ1.getS2_Instrumento()) || ("FXS").equals(registroDJ1.getS2_Instrumento())) {
					
					tipoTasaRecibe = "";
					tipoTasaEntrega = "";
			}
			
			ret += delimitador + tipoTasaRecibe;
			ret += delimitador + tipoTasaEntrega;			
			String getS3_XR_FechaFixing2 = registroDJ1.getS3_XR_FechaFixing2().toString().equals("0001-01-01") ? "" : formatoFecha.format(registroDJ1.getS3_XR_FechaFixing2()).toString();
			ret += delimitador + getS3_XR_FechaFixing2;
			ret += delimitador + registroDJ1.getS3_XR_ConvTipoCambio();
			
			String precioFuturo = "";
			
			if(("FWD").equals(registroDJ1.getS2_Instrumento()) || ("FUT").equals(registroDJ1.getS2_Instrumento()) 
				|| ("CAL").equals(registroDJ1.getS2_Instrumento()) || ("PUT").equals(registroDJ1.getS2_Instrumento())
				|| ("FXS").equals(registroDJ1.getS2_Instrumento())) {
					
				precioFuturo = String.format("%.5f", registroDJ1.getS3_PrecioFuturo());
			}
			
			ret += delimitador + precioFuturo;
			
			ret += delimitador + String.format("%.10f", registroDJ1.getS3_XR_PuntosForward());
		}
		
		if("IR".equals(tipoArchivo)) {
			ret += delimitador + registroDJ1.getS3_XR_TipoTasaRec();
			ret += delimitador + registroDJ1.getS3_XR_TipoTasaEnt();		
			ret += delimitador + String.format("%.10f", registroDJ1.getS3_R_TasaFija());
			ret += delimitador + String.format("%.10f", registroDJ1.getS3_R_TasaFija());
			ret += delimitador + String.format("%.10f", registroDJ1.getS3_R_Spread());
			ret += delimitador + String.format("%.10f", registroDJ1.getS3_R_Spread());
		}
		
		if("FI".equals(tipoArchivo)) {
			ret += delimitador + registroDJ1.getS3_I_TipoActivoSub();
			ret += delimitador + registroDJ1.getS3_I_IdActivoSuby();
			ret += delimitador + String.format("%.10f", registroDJ1.getS3_I_TasaPactada());
			ret += delimitador + String.format("%.5f", registroDJ1.getS3_PrecioFuturo());
			ret += delimitador + registroDJ1.getS3_I_PosicionOpe();
		}
		
		if("FX".equals(tipoArchivo) || "IR".equals(tipoArchivo)) {
			ret += delimitador + String.format("%.5f", registroDJ1.getS3_XR_PrecioEntrada());
			ret += delimitador + String.format("%.5f", registroDJ1.getS3_XR_PrecioSalida());
		}
	
		return ret;
	}
	
	public List<SeccionLinea> generarRegistroTipo4(final Registro registro,String tipoArchivo, Connection conn)
			throws SerializadorException, SQLException {
		SQL sql = new SQL();
		
		final RegistroDJ1 registroDJ1 = new RegistroDJ1(registro);
		List<SeccionLinea> lstSeccionLinea4 = new ArrayList<SeccionLinea>();
		DateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("KRutCtraparte1", registroDJ1.getkRutCtraparte1());
		params.put("KIdContrato", registroDJ1.getkIdContrato());
		params.put("KFechaContrato", registroDJ1.getkFechaContrato());
		params.put("KIdOpeEstruc", registroDJ1.getkIdOpeEstruc());
		params.put("kProducto", registroDJ1.getkProducto());
		params.put("kPeriodoComercial", registroDJ1.getkPeriodoComercial());
		params.put("seccion", 4);

		List<Bolson> lstCalendarioPagos = sql.query(conn, SELECT_BOLSONES_BY_CABECERA_SECCION, params, Bolson.class);
		
		for (Bolson calendarioPago : lstCalendarioPagos) {
			String ret = "4";
			ret += delimitador + registroDJ1.getkRutCtraparte1().replace("-", "");
			ret += delimitador + registroDJ1.getkIdContrato();
			ret += delimitador + registroDJ1.getkFechaContrato() + "T" + registroDJ1.getHoraContrato();
			ret += delimitador + registroDJ1.getkIdOpeEstruc();
			ret += delimitador + calendarioPago.KS4_XR_NroFlu;
			ret += delimitador + calendarioPago.S4_XR_TipoFlujo;	
			ret += delimitador + String.format("%.3f", calendarioPago.S4_XR_FacTasa);
			ret += delimitador + String.format("%.10f", calendarioPago.S4_XR_SpreadTasa);
			ret += delimitador + String.format("%.10f", calendarioPago.S4_XR_TasaFija);
			String S4_XR_FecFixTasa = calendarioPago.S4_XR_FecFixTasa.toString().equals("0001-01-01") ? "1900-01-01" : formatoFecha.format(calendarioPago.S4_XR_FecFixTasa).toString();
			ret += delimitador + S4_XR_FecFixTasa;
			String S4_XR_FecIni = calendarioPago.S4_XR_FecIni.toString().equals("0001-01-01") ? "1900-01-01" : formatoFecha.format(calendarioPago.S4_XR_FecIni).toString();
			ret += delimitador + S4_XR_FecIni;
			String S4_XR_FecTer = calendarioPago.S4_XR_FecTer.toString().equals("0001-01-01") ? "1990-01-01" : formatoFecha.format(calendarioPago.S4_XR_FecTer).toString();
			ret += delimitador + S4_XR_FecTer;
			ret += delimitador + String.format("%.5f", calendarioPago.S4_XR_MontoNoc);
			ret += delimitador + String.format("%.5f", calendarioPago.S4_XR_MontoInt);
			if("FX".equals(tipoArchivo)) {
				ret += delimitador + calendarioPago.S4_X_MonMonto;
			}
			
			lstSeccionLinea4.add(new SeccionLinea(4, ret));
		}
		
		return lstSeccionLinea4;
	}

	public List<SeccionLinea> generarRegistroTipo5(final Registro registro,String tipoArchivo, Connection conn)
			throws SerializadorException, SQLException {
		SQL sql = new SQL();
		
		final RegistroDJ1 registroDJ1 = new RegistroDJ1(registro);
		List<SeccionLinea> lstSeccionLinea5 = new ArrayList<SeccionLinea>();
		DateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("KRutCtraparte1", registroDJ1.getkRutCtraparte1());
		params.put("KIdContrato", registroDJ1.getkIdContrato());
		params.put("KFechaContrato", registroDJ1.getkFechaContrato());
		params.put("KIdOpeEstruc", registroDJ1.getkIdOpeEstruc());
		params.put("kProducto", registroDJ1.getkProducto());
		params.put("kPeriodoComercial", registroDJ1.getkPeriodoComercial());
		params.put("seccion", 5);

		List<Bolson> lstPagosPeriodo = sql.query(conn, SELECT_BOLSONES_BY_CABECERA_SECCION, params, Bolson.class);

		for (Bolson pago : lstPagosPeriodo) {
			String ret = "5";
			ret += delimitador + registroDJ1.getkRutCtraparte1().replace("-", "");
			ret += delimitador + registroDJ1.getkIdContrato();
			ret += delimitador + registroDJ1.getkFechaContrato() +  "T" + registroDJ1.getHoraContrato();
			if("FX".equals(tipoArchivo) || "IR".equals(tipoArchivo)) {
				ret += delimitador + registroDJ1.getkIdOpeEstruc();
			}
			ret += delimitador + String.format("%.0f", pago.KS5_CorrPago);
			ret += delimitador + pago.KS5_IdPago;
			String S5_FecPago = pago.S5_FecPago.toString().equals("0001-01-01") ? "1900-01-01" : formatoFecha.format(pago.S5_FecPago).toString();
			ret += delimitador + S5_FecPago;
			ret += delimitador + String.format("%.5f", pago.S5_MontoPago);
			ret += delimitador + pago.S5_TipoPago;
			String monedaPago = pago.S5_CodMoneda.equals("UF")?"CLF":pago.S5_CodMoneda;
			ret += delimitador + monedaPago;

			lstSeccionLinea5.add(new SeccionLinea(5, ret));
		}
		
		return lstSeccionLinea5;
	}

	public List<SeccionLinea> generarRegistroTipo6(final Registro registro, String tipoArchivo, Connection conn)
			throws SerializadorException, SQLException {
		SQL sql = new SQL();
		
		final RegistroDJ1 registroDJ1 = new RegistroDJ1(registro);
		List<SeccionLinea> lstSeccionLinea6 = new ArrayList<SeccionLinea>();
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("KRutCtraparte1", registroDJ1.getkRutCtraparte1());
		params.put("KIdContrato", registroDJ1.getkIdContrato());
		params.put("KFechaContrato", registroDJ1.getkFechaContrato());
		params.put("KIdOpeEstruc", registroDJ1.getkIdOpeEstruc());
		params.put("kProducto", registroDJ1.getkProducto());
		params.put("kPeriodoComercial", registroDJ1.getkPeriodoComercial());
		params.put("seccion", 6);

		List<Bolson> lstGarantias = sql.query(conn, SELECT_BOLSONES_BY_CABECERA_SECCION, params, Bolson.class);

		for (Bolson garantia : lstGarantias) {
			String ret = "6";
			
			ret += delimitador + registroDJ1.getkRutCtraparte1().replace("-", "");
			ret += delimitador + registroDJ1.getkIdContrato();
			ret += delimitador + registroDJ1.getkFechaContrato() +  "T" + registroDJ1.getHoraContrato();
			if("FX".equals(tipoArchivo) || "IR".equals(tipoArchivo)) {
				ret += delimitador + registroDJ1.getkIdOpeEstruc();
			}
			ret += delimitador + garantia.S6_TipoGar;
			ret += delimitador + garantia.S6_MonVal;
			ret += delimitador + garantia.KS6_IdGar;
			ret += delimitador + String.format("%.5f", garantia.S6_MontoUmb);
			ret += delimitador + String.format("%.5f", garantia.S6_ValGtia);

			lstSeccionLinea6.add(new SeccionLinea(6, ret));
		}
		
		return lstSeccionLinea6;
	}

	public List<SeccionLinea> generarRegistroTipo7(final Registro registro, String tipoArchivo, Connection conn)
			throws SerializadorException, SQLException {
		SQL sql = new SQL();
		
		final RegistroDJ1 registroDJ1 = new RegistroDJ1(registro);
		List<SeccionLinea> lstSeccionLinea7 = new ArrayList<SeccionLinea>();
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("KRutCtraparte1", registroDJ1.getkRutCtraparte1());
		params.put("KIdContrato", registroDJ1.getkIdContrato());
		params.put("KFechaContrato", registroDJ1.getkFechaContrato());
		params.put("KIdOpeEstruc", registroDJ1.getkIdOpeEstruc());
		params.put("kProducto", registroDJ1.getkProducto());
		params.put("kPeriodoComercial", registroDJ1.getkPeriodoComercial());
		params.put("seccion", 7);

		List<Bolson> lstTipoActivoGarantia = sql.query(conn, SELECT_BOLSONES_BY_CABECERA_SECCION, params, Bolson.class);

		for (Bolson tipoActivoGarantia : lstTipoActivoGarantia) {
			String ret = "7";
			
			ret += delimitador + registroDJ1.getkRutCtraparte1().replace("-", "");
			ret += delimitador + registroDJ1.getkIdContrato();
			ret += delimitador + registroDJ1.getkFechaContrato() +  "T" + registroDJ1.getHoraContrato();
			
			if("FX".equals(tipoArchivo) || "IR".equals(tipoArchivo)) {
				ret += delimitador + registroDJ1.getkIdOpeEstruc();
			}
			
			ret += delimitador + tipoActivoGarantia.S7_TipoGar;
			ret += delimitador + tipoActivoGarantia.KS7_IdGar;
			ret += delimitador + tipoActivoGarantia.S7_TipoActGar;
			ret += delimitador + String.format("%.0f", tipoActivoGarantia.S7_PorcGar);

			lstSeccionLinea7.add(new SeccionLinea(7, ret));
		}
		
		return lstSeccionLinea7;
	}

	public String generarRegistroTipo8(final Registro registro, String tipoArchivo) throws SerializadorException {
		final RegistroDJ1 registroDJ1 = new RegistroDJ1(registro);
		
		String ret = "8";

		ret += delimitador + registroDJ1.getkRutCtraparte1().replace("-", "");
		ret += delimitador + registroDJ1.getkIdContrato();
		ret += delimitador + registroDJ1.getkFechaContrato() +  "T" + registroDJ1.getHoraContrato();
		if("FX".equals(tipoArchivo) || "IR".equals(tipoArchivo)) {
			ret += delimitador + registroDJ1.getkIdOpeEstruc();
		}
		ret += delimitador + registroDJ1.getS8_MtodoValoriza();
		ret += delimitador + registroDJ1.getS8_MonedaValoriza();
		ret += delimitador + String.format("%.5f", registroDJ1.getS8_ValorMercado());
		String opVolatilidad = "INICIAL".equals(registroDJ1.getfuenteDatos()) ? "" : String.format("%.10f", registroDJ1.getS8_OpVolatilidad());
		ret += delimitador + opVolatilidad;
		String opDelta = "INICIAL".equals(registroDJ1.getfuenteDatos()) ? "" : String.format("%.10f", registroDJ1.getS8_OpDelta());
		ret += delimitador + opDelta;
		String opGamma = "INICIAL".equals(registroDJ1.getfuenteDatos()) ? "" : String.format("%.10f", registroDJ1.getS8_OpGamma());
		ret += delimitador + opGamma;
		String opVega = "INICIAL".equals(registroDJ1.getfuenteDatos()) ? "" : String.format("%.10f", registroDJ1.getS8_OpVega());
		ret += delimitador + opVega;
		String ajusteRiesgo = "INICIAL".equals(registroDJ1.getfuenteDatos()) ? "" : String.format("%.5f", registroDJ1.getS8_AjusteRiesgo());
		ret += delimitador + ajusteRiesgo;
		String ajusteBid = "INICIAL".equals(registroDJ1.getfuenteDatos()) ? "" : String.format("%.5f", registroDJ1.getS8_AjusteBID());
		ret += delimitador + ajusteBid;
		String otrosAjustes = "INICIAL".equals(registroDJ1.getfuenteDatos()) ? "" : String.format("%.5f", registroDJ1.getS8_OtrosAjustes());
		ret += delimitador + otrosAjustes;
		ret += delimitador + String.format("%.5f", registroDJ1.getS8_ValorMercadoActivo());
		ret += delimitador + String.format("%.5f", registroDJ1.getS8_ValorMercadoPasivo());

		return ret;
	}

	@Override
	public void generarResumen() throws SerializadorException {
		// No se implementa
	}
}
