package cl.bcs.custodia.wbs;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.cartola.BcsCartola.Dato;
import cl.bcs.normativa.cartola.ResumenInversionBase;
import cl.bcs.normativa.cartola.ResumenInversionBase.EntradaResumenInversionBase;
import cl.bcs.normativa.cartola.ResumenInversionBase.OutResumenInversion;
import cl.bcs.normativa.cartola.ResumenInversionBase.SalidaResumenInversionBase;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;

public class ConsultarPatrimonioCustodia implements JavaCall {
	
	private static final Logger logger = LogManager.getLogger(ConsultarPatrimonioCustodia.class);
	
	private final String SQL_SELECT_DOLAR_OBSERVADO = loadResourceToString("cl/bcs/custodia/wbs/sql/ConsultarPatrimonioCustodia_SelectDolarObservado.sql");
	
	private static final String ID_PARAMETRO_FINANCIERO_DO = "DO";
	private static final String GRUPO_RENTA_VARIABLE = "RV";
	private static final String GRUPO_RENTA_FIJA = "RF";
	private static final String GRUPO_CAJA = "CAJA";
	private static final String GRUPO_INTERMEDIACION_FINANCIERA = "IF";
	private static final String GRUPO_DERIVADOS = "DV";
	private static final String GRUPO_FONDOS_MUTUOS = "FM";
	private static final String GRUPO_OPERACIONES_POR_LIQUIDAR = "OPL";
	private static final String MONEDA_USD = "USD";
	
	public static class EntradaConsultarPatrimonioCustodia extends BcsPojo{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
	    public String p_FechaConsulta;
		public String p_ExpresadoEnMoneda;
		public String p_IncluyeCuentaInversion;
		public String p_CodigoPortafolio;
	    public List<Dato> p_Datos;
	    
	    public EntradaConsultarPatrimonioCustodia() {
	    	this.p_CodigoPortafolio = "";
	    	this.p_Datos = new ArrayList<Dato>();
	    }
	}

	public class SalidaConsultarPatrimonioCustodia extends BcsPojo{
		public Long P_OUT_ERRORESCONTADOR;
		public List<MensajeSalida> P_OUT_MENSAJES;
		public String CodigoPortafolio;
		public BigDecimal TotalPatrimonio;
		public String ExpresadoEnMoneda;
		public List<Activo> Activos;
		public List<Pasivo> Pasivos;
		
		public SalidaConsultarPatrimonioCustodia() {
			this.CodigoPortafolio = "";
			this.P_OUT_ERRORESCONTADOR = 0L;
	    	this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
	    	this.CodigoPortafolio = "";
	    	this.TotalPatrimonio = BigDecimal.ZERO;
	    	this.ExpresadoEnMoneda = "";
	    	this.Activos = new ArrayList<Activo>();
	    	this.Pasivos = new ArrayList<Pasivo>();
		}
	}
	
	public static abstract class BaseGrupo extends BcsPojo {
	    public BigDecimal Caja = BigDecimal.ZERO;
	    public BigDecimal Derivados = BigDecimal.ZERO;
	    public BigDecimal FondosMutuos = BigDecimal.ZERO;
	    public BigDecimal IntermediacionFinanciera = BigDecimal.ZERO;
	    public BigDecimal OperacionesPorLiquidar = BigDecimal.ZERO;
	    public BigDecimal RentaFija = BigDecimal.ZERO;
	    public BigDecimal RentaVariable = BigDecimal.ZERO;

	    public void acumular(String grupo, BigDecimal monto, String incluyeCuentaInversion) {
	        if (monto == null) monto = BigDecimal.ZERO;

	        switch (grupo) {
	            case GRUPO_RENTA_VARIABLE: RentaVariable = RentaVariable.add(monto); break;
	            case GRUPO_RENTA_FIJA: RentaFija = RentaFija.add(monto); break;
	            case GRUPO_CAJA: Caja = "S".equals(incluyeCuentaInversion) ? Caja.add(monto) : BigDecimal.ZERO; break;
	            case GRUPO_DERIVADOS: Derivados = Derivados.add(monto); break;
	            case GRUPO_FONDOS_MUTUOS: FondosMutuos = FondosMutuos.add(monto); break;
	            case GRUPO_INTERMEDIACION_FINANCIERA: IntermediacionFinanciera = IntermediacionFinanciera.add(monto); break;
	            case GRUPO_OPERACIONES_POR_LIQUIDAR: OperacionesPorLiquidar = OperacionesPorLiquidar.add(monto); break;
	            default: break;
	        }
	    }

	    public BigDecimal calcularTotal() {
	        return Caja
	            .add(Derivados)
	            .add(FondosMutuos)
	            .add(IntermediacionFinanciera)
	            .add(OperacionesPorLiquidar)
	            .add(RentaVariable)
	            .add(RentaFija);
	    }
	    
	    public void convertirADolar(BigDecimal dolarObservado) {
	        if (dolarObservado == null || BigDecimal.ZERO.compareTo(dolarObservado) == 0) return;
	        Caja = Caja.divide(dolarObservado, MathContext.DECIMAL128);
	        RentaVariable = RentaVariable.divide(dolarObservado, MathContext.DECIMAL128);
	        RentaFija = RentaFija.divide(dolarObservado, MathContext.DECIMAL128);
	        Derivados = Derivados.divide(dolarObservado, MathContext.DECIMAL128);
	        FondosMutuos = FondosMutuos.divide(dolarObservado, MathContext.DECIMAL128);
	        IntermediacionFinanciera = IntermediacionFinanciera.divide(dolarObservado, MathContext.DECIMAL128);
	        OperacionesPorLiquidar = OperacionesPorLiquidar.divide(dolarObservado, MathContext.DECIMAL128);
	    }
	}
	
	public static class Activo extends BaseGrupo {
	    public BigDecimal TotalActivo = BigDecimal.ZERO;

	    public void calcularTotalActivo() {
	        this.TotalActivo = super.calcularTotal();
	    }
	}

	public static class Pasivo extends BaseGrupo {
	    public BigDecimal TotalPasivo = BigDecimal.ZERO;

	    public void calcularTotalPasivo() {
	        this.TotalPasivo = super.calcularTotal();
	    }
	}
	
	public static class DolarObservado extends BcsPojo{
		public String IdParametroFinanciero;
		public Date Fecha;
		public BigDecimal Valor;
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaConsultarPatrimonioCustodia jin = gson.fromJson(in.toString(), EntradaConsultarPatrimonioCustodia.class);
		SalidaConsultarPatrimonioCustodia out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaConsultarPatrimonioCustodia logica(Connection con, EntradaConsultarPatrimonioCustodia entrada) throws Exception {
		logger.info("INICIO::ConsultarPatrimonioCustodia.logica");
		long inicio = System.currentTimeMillis();

		SalidaConsultarPatrimonioCustodia salida = new SalidaConsultarPatrimonioCustodia();
	
		String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");
		Date fechaConsulta = Fecha.toDate(entrada.p_FechaConsulta);
		Date fechaSistemaComparacion = Fecha.toDate(fechaSistema);
		
		if (fechaConsulta.after(fechaSistemaComparacion)) {
			salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_FECHA_CONSULTA_MAYOR_SISTEMA", new HashMap<>(0), con, entrada.p_IdEmpresa));
			salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
			return salida;
		}
		
		BigDecimal dolarObservado =  BigDecimal.ZERO;
		
		if(MONEDA_USD.equals(entrada.p_ExpresadoEnMoneda.toUpperCase())) {
			dolarObservado = obtenerDolarObservado(con, entrada);
		
			if(dolarObservado.compareTo(BigDecimal.ZERO) == 0) {
				salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "UI_NO_EXISTE_VALOR_DOLAR_OBSERVADO", new HashMap<>(0), con, entrada.p_IdEmpresa));
				salida.P_OUT_ERRORESCONTADOR = MensajeSalida.getNumeroErrores(salida.P_OUT_MENSAJES);
				return salida;
			}
		}
				
		SalidaResumenInversionBase resumenInversion = obtenerResumenInversion(con, entrada);

		salida = obtenerSalidaPatrimonioCustodia(con, resumenInversion, entrada, dolarObservado);
		
		long fin= System.currentTimeMillis();
		logger.info("FIN::ConsultarPatrimonioCustodia.logica", fin - inicio);
		return salida;
	}
	
	private BigDecimal obtenerDolarObservado(Connection con, EntradaConsultarPatrimonioCustodia entrada) throws SQLException {
		logger.info("INICIO::ConsultarPatrimonioCustodia.obtenerDolarObservado");
		long inicio = System.currentTimeMillis();
		SQL sql = new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>(2);
		params.put("IdParametroFinanciero", ID_PARAMETRO_FINANCIERO_DO);
		params.put("FechaConsulta", entrada.p_FechaConsulta);
		List<DolarObservado> parametroDolarObservado = sql.query(con, SQL_SELECT_DOLAR_OBSERVADO, params, DolarObservado.class);
		
		BigDecimal dolarObservado = parametroDolarObservado.stream().map(x -> x.Valor).findFirst().orElse(BigDecimal.ZERO);       
		
		long fin= System.currentTimeMillis();
		logger.info("FIN::ConsultarPatrimonioCustodia.obtenerDolarObservado", fin - inicio);
		return dolarObservado;
	}

	private SalidaResumenInversionBase obtenerResumenInversion(Connection con, EntradaConsultarPatrimonioCustodia entrada) throws Exception {
		logger.info("INICIO::ConsultarPatrimonioCustodia.obtenerResumenInversion");
		long inicio = System.currentTimeMillis();
		
		ResumenInversionBase resumenInversionBase = new ResumenInversionBase();
		EntradaResumenInversionBase entradaResumenInversionBase = new EntradaResumenInversionBase();
		
		entradaResumenInversionBase.p_IdEmpresa = entrada.p_IdEmpresa;
		entradaResumenInversionBase.p_IdUsuario = entrada.p_IdUsuario;
		entradaResumenInversionBase.p_IdModulo = entrada.p_IdModulo;
		entradaResumenInversionBase.p_FechaDesde = Fecha.toDate(entrada.p_FechaConsulta);
		entradaResumenInversionBase.p_FechaHasta = Fecha.toDate(entrada.p_FechaConsulta);
		entradaResumenInversionBase.p_Datos = entrada.p_Datos;
	   
	    SalidaResumenInversionBase salidaResumenInversionBase = resumenInversionBase.logica(con, entradaResumenInversionBase);
	    
	    long fin= System.currentTimeMillis();
		logger.info("FIN::ConsultarPatrimonioCustodia.obtenerResumenInversion", fin - inicio);
	    return salidaResumenInversionBase;
	}
	
	private SalidaConsultarPatrimonioCustodia obtenerSalidaPatrimonioCustodia(Connection con, SalidaResumenInversionBase resumenInversion, EntradaConsultarPatrimonioCustodia entrada, BigDecimal dolarObservado) throws Exception {
		logger.info("INICIO::ConsultarPatrimonioCustodia.obtenerSalidaPatrimonioCustodia");
		long inicio = System.currentTimeMillis();
		SalidaConsultarPatrimonioCustodia salida = new SalidaConsultarPatrimonioCustodia();
		
		Activo resumenActivo = new Activo();
		Pasivo resumenPasivo = new Pasivo();

		for (OutResumenInversion item : resumenInversion.p_out_ResumenInversion) {
		    if (esPasivo(item.Item, item.MontoMonedaSistema)) {
		        resumenPasivo.acumular(item.Grupo, item.MontoMonedaSistema, entrada.p_IncluyeCuentaInversion);
		    } else {
		        resumenActivo.acumular(item.Grupo, item.MontoMonedaSistema, entrada.p_IncluyeCuentaInversion);
		    }
		}
		
		if (MONEDA_USD.equalsIgnoreCase(entrada.p_ExpresadoEnMoneda)) {
		    resumenActivo.convertirADolar(dolarObservado);
		    resumenActivo.calcularTotalActivo();
		    resumenPasivo.convertirADolar(dolarObservado);
		    resumenPasivo.calcularTotalPasivo();
		} else {
			resumenActivo.calcularTotalActivo();
			resumenPasivo.calcularTotalPasivo();
		}

		salida.Activos.add(resumenActivo);
		salida.Pasivos.add(resumenPasivo);
		salida.TotalPatrimonio = resumenActivo.TotalActivo.add(resumenPasivo.TotalPasivo);
	    salida.CodigoPortafolio =  entrada.p_CodigoPortafolio;
		salida.ExpresadoEnMoneda = entrada.p_ExpresadoEnMoneda;

		long fin = System.currentTimeMillis();
		logger.info("FIN::ConsultarPatrimonioCustodia.obtenerSalidaPatrimonioCustodia", fin - inicio);
		return salida;
	}
	
	private boolean esPasivo(String item, BigDecimal montoMonedaSistema) {
		if ("Préstamos".equals(item) || "Prestamos".equals(item) || "Pactos".equals(item)) {
			return true;
		}

		if (("Simultanea".equals(item) || "Simultánea".equals(item)) && montoMonedaSistema != null && montoMonedaSistema.compareTo(BigDecimal.ZERO) < 0) {
			return true;
		}

		return false;
	}
		
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPropertiesConnection("master2", "connections.properties")) {
			Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
			String json = "{\r\n"
					+ "  \"p_IdEmpresa\": 1,\r\n"
					+ "  \"p_IdModulo\": \"CUS\",\r\n"
					+ "  \"p_IdUsuario\": 97,\r\n"
					+ "  \"p_FechaConsulta\": \"2025-08-29\",\r\n"
					+ "  \"p_ExpresadoEnMoneda\": \"CLP\",\r\n"
					+ "  \"p_IncluyeCuentaInversion\": \"S\",\r\n"
					+ "  \"p_CodigoPortafolio\": \"\",\r\n"
					+ "  \"p_Datos\": [\r\n"
					+ "    {\r\n"
					+ "      \"IdCliente\": 13218,\r\n"
					+ "      \"IdPortafolio\": 14712\r\n"
					+ "    }\r\n"
					+ "  ]\r\n"
					+ "}";
			
			EntradaConsultarPatrimonioCustodia Objin= gson.fromJson(json, EntradaConsultarPatrimonioCustodia.class);
			ConsultarPatrimonioCustodia  demo= new ConsultarPatrimonioCustodia();				
			try {
				long inicio = System.currentTimeMillis();
				SalidaConsultarPatrimonioCustodia ObjOut= demo.logica(con, Objin);
				long fin = System.currentTimeMillis();
				System.out.println("TOTAL:ConsultarPatrimonioCustodia:" + (fin - inicio));
				System.out.println(ObjOut.toString());
				con.rollback();
			}
			catch(Exception er) {
				er.printStackTrace();	
				con.rollback();
			}			
    	}
		System.out.println("== END ==");
	}
}
