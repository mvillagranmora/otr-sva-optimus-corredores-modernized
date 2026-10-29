package cl.bcs.plataforma.mer;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Timer;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CalcularParidad implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CalcularParidad.class.getName());
	
	private final String SQL_SELECT_PARAMETRO_FINANCIERO_FECHA_SOLICITADA = loadResourceToString("cl/bcs/plataforma/mer/sql/CalcularParidad_SelectParametroFinancieroFechaSolicitada.sql");
	private final String SQL_SELECT_PIVOTE_FECHA_SOLICITADA = loadResourceToString("cl/bcs/plataforma/mer/sql/CalcularParidad_SelectPivoteFechaSolicitada.sql");
	private final String SQL_SELECT_PIVOTE_DEFAULT = loadResourceToString("cl/bcs/plataforma/mer/sql/CalcularParidad_SelectPivoteDefault.sql");
	private final String SQL_SELECT_USOS = loadResourceToString("cl/bcs/plataforma/mer/sql/CalcularParidad_SelectUsos.sql");
	private final String SQL_SELECT_PARAMETROS_FINANCIEROS = loadResourceToString("cl/bcs/plataforma/mer/sql/CalcularParidad_SelectParametrosFinancieros.sql");
	private final String SQL_SELECT_PARAMETROS_FINANCIEROS_VALOR_CLP = loadResourceToString("cl/bcs/plataforma/mer/sql/CalcularParidad_SelectParametrosFinancierosValorCLP.sql");
	
	private final String ID_PARAMETRO_FINANCIERO_DEFAULT = "DO";
	private final String ID_MONEDA_CAMBIO = "USD";
	private final Long DIAS_DIFERENCIA_FECHA_MINIMA_DEFAULT = 1095L;
	
	private HashMap<String, Factor> FactoresEncontrados = new HashMap<String, Factor>();
	
	@Override
	public JsonObject call(Connection con, JsonObject jin) throws Exception {
		Gson gson = Utils.getGsonBuilder();
		CalcularParidadEntrada in=gson.fromJson(jin.toString() ,CalcularParidadEntrada.class);
		CalcularParidadSalida out=logica(con,in);
		JsonParser parser 	= new JsonParser();
        JsonObject dataout	= parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public static class CalcularParidadEntrada {
		public Long p_IdEmpresa;
		public Long p_IdTrabajo;
		public List<CalculoEntrada> p_CalculosEntrada;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CalcularParidadEntrada() {
			this.p_CalculosEntrada = new ArrayList<CalculoEntrada>();
		}
	}
	
	public static class CalculoEntrada {
		public Long IdTrabajo;
		public Long IdInterno;
		public String Fecha;
		public String IdMonedaOrigen;
		public String IdMonedaDestino;
		public BigDecimal ValorCambioLocalModificado = BigDecimal.ZERO;
		public BigDecimal MontoOrigen;
		public String IdUso;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CalculoEntrada(){
			this.ValorCambioLocalModificado = BigDecimal.ZERO;
			this.IdUso = "";
		}
	}
	
	public static class ParametroFinanciero{
		public String IdParametroFinanciero;
	    public String IdMonedaOrigen;
		public String IdMonedaDestino;
		public Date Fecha;
		public BigDecimal Valor;
		public BigDecimal FactorInverso;
		public String IdUso;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Factor{
		public String IdParametroFinanciero;
	    public String IdMonedaOrigen;
		public String IdMonedaDestino;
		public Date Fecha;
		public BigDecimal Factor;
		public String IdUso;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DefinicionParametro{
		public String IdParametroFinanciero;
		public String IdMonedaParametro;
		public String IdMonedaValor;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
		
	public static class CalculoSalida {
		public Long IdTrabajo;
		public Long IdInterno;
		public java.sql.Date Fecha;
		public String IdMonedaOrigen;
		public String IdMonedaDestino;
		public BigDecimal ValorCambioLocalModificado;
		public BigDecimal MontoOrigen;
		public String IdUso;
		
		public BigDecimal FactorOrigenDestino;
		public BigDecimal MontoDestino;
		public BigDecimal FactorOrigenLocal;
		public BigDecimal MontoLocal;
		
		public BigDecimal FactorOrigenCambio;
		public java.sql.Date FechaOrigenCambio;
		public String IdTipoRegistroOrigenCambio;
		
		public BigDecimal FactorCambioLocal;
		public java.sql.Date FechaCambioLocal;
		public String IdTipoRegistroCambioLocal;
		
		public BigDecimal FactorLocalCambio;
		public java.sql.Date FechaLocalCambio;
		public String IdTipoRegistroLocalCambio;
		
		public BigDecimal FactorCambioDestino;
		public java.sql.Date FechaCambioDestino;
		public String IdTipoRegistroCambioDestino;
		
		public String Resultado;
		public String CodigoMensaje;
		public BigDecimal ValorParametroFinanciero;
		public Date FechaParametroFinanciero;

		public String toString() {
			return Utils.printJson(this, false);
		}
		
		public CalculoSalida(){
			this.FactorOrigenDestino = BigDecimal.ZERO;
			this.FactorOrigenLocal = BigDecimal.ZERO;
			this.FactorOrigenCambio = BigDecimal.ZERO;
			this.IdTipoRegistroOrigenCambio = "";
			this.FactorCambioLocal = BigDecimal.ZERO;
			this.IdTipoRegistroCambioLocal = "";
			this.FactorLocalCambio = BigDecimal.ZERO;
			this.IdTipoRegistroLocalCambio = "";
			this.FactorCambioDestino = BigDecimal.ZERO;
			this.IdTipoRegistroCambioDestino = "";
			this.ValorParametroFinanciero =BigDecimal.ZERO;
		}
	
	}
	
	public static class CalcularParidadSalida {
		public List<CalculoSalida> p_out_CalculosSalida;
		public Long	 P_OUT_ERRORESCONTADOR = 0L;
		
		public CalcularParidadSalida(){
			this.p_out_CalculosSalida = new ArrayList<CalculoSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Uso{
		public String IdUso;
		public String NombreUso;
		public Long Cantidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Trace
	public CalcularParidadSalida logica(Connection con, CalcularParidadEntrada entrada) throws Exception {
		logger.info("INICIO::CalcularParidad");
		CalcularParidadSalida salida = new CalcularParidadSalida();
		SQL sql = new SQL();
		
		String idMonedaLocal = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		Long diasDiferenciaFechaMinima = DIAS_DIFERENCIA_FECHA_MINIMA_DEFAULT;
		try {
			 diasDiferenciaFechaMinima = ConfiguracionSistema.getParametroConfiguracionLong(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.DiasFechaMinimaCalculoParidad");
		}catch(Exception e) {
			logger.warn("ADVERTENCIA::Parametro PLA.DiasFechaMinimaCalculoParidad no existe, se toma valor por defecto de " + diasDiferenciaFechaMinima + " dias.");
		}
		
		
		//FIXEAMOS LA ENTRADA Y BUSCAMOS LAS ENTRADAS DISTINTAS A LA MONEDA DE CAMBIO, ADEMAS DE SACAR LOS MONEDA ORIGEN IGUAL A DESTINO E IGUAL AL SISTEMA
		List<String> usos = new ArrayList<String>();
		Date fechaMaxima = null;
		Date fechaMinima = null;
		List<String> monedas = new ArrayList<String>();
		List<CalculoEntrada> calculosAProcesar = new ArrayList<CalculoEntrada>();
		List<String> parametrosFinancieros = new ArrayList<String>();
		for (CalculoEntrada calc : entrada.p_CalculosEntrada) {
			Date fecha = Fecha.toDate(calc.Fecha);
			if (calc.Fecha.length() > 10)  calc.Fecha = calc.Fecha.substring(0,10);
			if(calc.IdUso == null) calc.IdUso = "";
			if(calc.ValorCambioLocalModificado == null) calc.ValorCambioLocalModificado = BigDecimal.ZERO;
			if(!idMonedaLocal.equals(calc.IdMonedaDestino) && !ID_MONEDA_CAMBIO.equals(calc.IdMonedaDestino) && !monedas.contains(calc.IdMonedaDestino)) monedas.add(calc.IdMonedaDestino);
			if(!idMonedaLocal.equals(calc.IdMonedaOrigen) && !ID_MONEDA_CAMBIO.equals(calc.IdMonedaOrigen) && !monedas.contains(calc.IdMonedaOrigen)) monedas.add(calc.IdMonedaOrigen);
			if(!parametrosFinancieros.contains("USD".equals(calc.IdMonedaOrigen) ? "DO" : calc.IdMonedaOrigen)) parametrosFinancieros.add("USD".equals(calc.IdMonedaOrigen) ? "DO" : calc.IdMonedaOrigen);
			if(!parametrosFinancieros.contains("USD".equals(calc.IdMonedaDestino) ? "DO" : calc.IdMonedaDestino)) parametrosFinancieros.add("USD".equals(calc.IdMonedaDestino) ? "DO" : calc.IdMonedaDestino);
			if(!usos.contains(calc.IdUso)) usos.add(calc.IdUso);
			if(fechaMaxima == null || fechaMaxima.compareTo(fecha) == -1) fechaMaxima = fecha;
			if(fechaMinima == null || fechaMinima.compareTo(fecha) == 1) fechaMinima = fecha;
			
			//BUSCAMOS PRIMER CASO MONEDA ORIGEN IGUAL A DESTINO E IGUAL AL SISTEMA, SINO SE INGRESA PARA PROCESAR
			if(calc.IdMonedaOrigen.equals(calc.IdMonedaDestino) && calc.IdMonedaDestino.equals(idMonedaLocal)){
				CalculoSalida calculoSalida = new CalculoSalida();
				
				calculoSalida.IdTrabajo = calc.IdTrabajo;
				calculoSalida.IdInterno = calc.IdInterno;
				calculoSalida.IdMonedaOrigen = calc.IdMonedaOrigen;
				calculoSalida.IdMonedaDestino = calc.IdMonedaDestino;
				calculoSalida.ValorCambioLocalModificado = calc.ValorCambioLocalModificado;
				calculoSalida.MontoOrigen = calc.MontoOrigen;
				calculoSalida.IdUso = calc.IdUso;
				
				calculoSalida.Fecha = fecha;
				calculoSalida.MontoDestino = calc.MontoOrigen;
				calculoSalida.MontoLocal = calc.MontoOrigen;
				calculoSalida.FactorOrigenDestino = BigDecimal.ONE;
				calculoSalida.FactorOrigenLocal = BigDecimal.ONE;
				calculoSalida.FactorOrigenCambio = BigDecimal.ONE;
				calculoSalida.FechaOrigenCambio = fecha;
				calculoSalida.IdTipoRegistroOrigenCambio = idMonedaLocal;
				calculoSalida.FactorCambioLocal = BigDecimal.ONE;
				calculoSalida.FechaCambioLocal = fecha;
				calculoSalida.IdTipoRegistroCambioLocal = idMonedaLocal;
				calculoSalida.FactorLocalCambio = BigDecimal.ONE;
				calculoSalida.FechaLocalCambio = fecha;
				calculoSalida.IdTipoRegistroLocalCambio = idMonedaLocal;
				calculoSalida.FactorCambioDestino = BigDecimal.ONE;
				calculoSalida.FechaCambioDestino = fecha;
				calculoSalida.IdTipoRegistroCambioDestino = "";
				calculoSalida.Resultado = "OK";
				calculoSalida.CodigoMensaje = "OK";
				calculoSalida.ValorParametroFinanciero = BigDecimal.ONE;
				calculoSalida.FechaParametroFinanciero = fecha;
				calculoSalida.Resultado = "OK";
				calculoSalida.CodigoMensaje = "OK";
				salida.p_out_CalculosSalida.add(calculoSalida);
			}else{
				calculosAProcesar.add(calc);
			}
		}
		
		if(fechaMinima != null && !fechaMinima.equals(Fecha.toDate("0001-01-01"))) {
			fechaMinima = Fecha.addDays(fechaMinima, diasDiferenciaFechaMinima.intValue() * -1);
		}
		
		Timer tm = Timer.createTimer().setLogger(logger).setTimeUnit(TimeUnit.MILLISECONDS);
		//COMENZAMOS A BUSCAR PARIDAD
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdMonedaParametro", monedas);
		params.put("FechaMinima", fechaMinima);
		params.put("FechaMaxima", fechaMaxima);
		tm.addMetric("SQL_SELECT_PARAMETRO_FINANCIERO_FECHA_SOLICITADA");
		List<ParametroFinanciero> parametroFinancieroFechaSolicitada = sql.query(con, SQL_SELECT_PARAMETRO_FINANCIERO_FECHA_SOLICITADA, params, ParametroFinanciero.class);
		params.clear();
		params.put("IdMonedaPivote", ID_MONEDA_CAMBIO);
		params.put("FechaMinima", fechaMinima);
		params.put("FechaMaxima",fechaMaxima);
		params.put("IdUso",usos);
		tm.addMetric("SQL_SELECT_PIVOTE_FECHA_SOLICITADA");
		List<ParametroFinanciero> pivoteFechaSolicitada = sql.query(con, SQL_SELECT_PIVOTE_FECHA_SOLICITADA, params, ParametroFinanciero.class);
		params.clear();
		params.put("IdParametroFinanciero", ID_PARAMETRO_FINANCIERO_DEFAULT);
		params.put("FechaMinima", fechaMinima);
		params.put("FechaMaxima",fechaMaxima);
		tm.addMetric("SQL_SELECT_PIVOTE_DEFAULT");
		List<ParametroFinanciero> pivoteDefault = sql.query(con, SQL_SELECT_PIVOTE_DEFAULT, params, ParametroFinanciero.class);
		params.clear();
		params.put("IdUso",usos);
		params.put("IdEmpresa",entrada.p_IdEmpresa);
		tm.addMetric("SQL_SELECT_USOS");
		List<Uso> usosExistentes = sql.query(con, SQL_SELECT_USOS, params, Uso.class);
		HashMap<String,Object> hUsos = Utils.List2Hash(usosExistentes, "IdUso");
		params.clear();
		params.put("IdParametroFinanciero", parametrosFinancieros);
		params.put("FechaMinima", fechaMinima);
		params.put("FechaMaxima", fechaMaxima);
		tm.addMetric("SQL_SELECT_PARAMETROS_FINANCIEROS");
		List<ParametroFinanciero> parametroFinancieroOriginales = sql.query(con, SQL_SELECT_PARAMETROS_FINANCIEROS, params, ParametroFinanciero.class);
		params.clear();
		params.put("IdMonedaLocal", idMonedaLocal);
		tm.addMetric("SQL_SELECT_PARAMETROS_FINANCIEROS_VALOR_CLP");
		List<DefinicionParametro> definicionParametros = sql.query(con, SQL_SELECT_PARAMETROS_FINANCIEROS_VALOR_CLP, params, DefinicionParametro.class);
		HashMap<String,Object> hDefinicionParametros = Utils.List2Hash(definicionParametros, "IdParametroFinanciero");
		
		Comparator<Factor> comparadorFechas = new Comparator<Factor>() {
		    @Override
		    public int compare(Factor mov1, Factor mov2) {
		        if(mov1.Fecha.compareTo(mov2.Fecha) == 0){
		            return 0;
		        }
		        return mov2.Fecha.compareTo(mov1.Fecha) == 1 ? 1 : -1;
		     }
		};
		
		tm.addMetric("AGREGAMOS TABLA DE FACTORES CON INVERSOS");
		List<Factor> factoresPrevios = parametroFinancieroFechaSolicitada.parallelStream().map(parametroFinanciero->{
			Factor factor = new Factor();
			factor.IdParametroFinanciero = parametroFinanciero.IdParametroFinanciero;
			factor.IdMonedaOrigen = parametroFinanciero.IdMonedaOrigen;
			factor.IdMonedaDestino = parametroFinanciero.IdMonedaDestino;
			factor.Fecha = parametroFinanciero.Fecha;
			factor.IdUso = parametroFinanciero.IdUso;
			factor.Factor = parametroFinanciero.Valor;
			return factor;
		}).collect(Collectors.toList());
		
		factoresPrevios.addAll(parametroFinancieroFechaSolicitada.parallelStream().map(parametroFinanciero->{
			Factor factorInverso = new Factor();
			
			factorInverso.IdParametroFinanciero = parametroFinanciero.IdParametroFinanciero;
			factorInverso.Fecha = parametroFinanciero.Fecha;
			factorInverso.IdUso = parametroFinanciero.IdUso;
			
			factorInverso.IdMonedaOrigen = parametroFinanciero.IdMonedaDestino;
			factorInverso.IdMonedaDestino = parametroFinanciero.IdMonedaOrigen;
			factorInverso.Factor = BigDecimal.ONE.divide(parametroFinanciero.Valor, MathContext.DECIMAL128);

			return factorInverso;
		}).collect(Collectors.toList()));
		
		List<Factor> factores = factoresPrevios.parallelStream().sorted(comparadorFechas).collect(Collectors.toList());
		
		tm.addMetric("AGREGAMOS TABLA DE FACTORES CON INVERSOS DE LOS CON USO");
		List<Factor> factoresPivoteUsoPrevios = pivoteFechaSolicitada.parallelStream().map(parametroFinanciero -> {
			Factor factor = new Factor();
			factor.IdParametroFinanciero = parametroFinanciero.IdParametroFinanciero;
			factor.IdMonedaOrigen = parametroFinanciero.IdMonedaOrigen;
			factor.IdMonedaDestino = parametroFinanciero.IdMonedaDestino;
			factor.Fecha = parametroFinanciero.Fecha;
			factor.IdUso = parametroFinanciero.IdUso;
			factor.Factor = BigDecimal.ONE.divide(parametroFinanciero.Valor, MathContext.DECIMAL128);
			return factor;
		}).collect(Collectors.toList());
		
		factoresPivoteUsoPrevios.addAll(pivoteFechaSolicitada.parallelStream().map(parametroFinanciero -> {
			Factor factorInverso = new Factor();

			factorInverso.IdParametroFinanciero = parametroFinanciero.IdParametroFinanciero;
			factorInverso.Fecha = parametroFinanciero.Fecha;
			factorInverso.IdUso = parametroFinanciero.IdUso;
			
			factorInverso.IdMonedaOrigen = parametroFinanciero.IdMonedaDestino;
			factorInverso.IdMonedaDestino = parametroFinanciero.IdMonedaOrigen;
			factorInverso.Factor = BigDecimal.ONE.divide(parametroFinanciero.Valor, MathContext.DECIMAL128);
			return factorInverso;
		}).collect(Collectors.toList()));
		
		List<Factor> factoresPivoteUso = factoresPivoteUsoPrevios.parallelStream().sorted(comparadorFechas).collect(Collectors.toList());
		
		tm.addMetric("AGREGAMOS TABLA DE FACTORES CON INVERSOS DE LOS CON USO DEFAULT");
		List<Factor> factoresPivoteDefaultPrevios = pivoteDefault.parallelStream().map(parametroFinanciero -> {
			Factor factor = new Factor();
			factor.IdParametroFinanciero = parametroFinanciero.IdParametroFinanciero;
			factor.IdMonedaOrigen = parametroFinanciero.IdMonedaOrigen;
			factor.IdMonedaDestino = parametroFinanciero.IdMonedaDestino;
			factor.Fecha = parametroFinanciero.Fecha;
			factor.IdUso = parametroFinanciero.IdUso;
			factor.Factor = parametroFinanciero.Valor;
			return factor;
		}).collect(Collectors.toList());
		
		factoresPivoteDefaultPrevios.addAll(pivoteDefault.parallelStream().map(parametroFinanciero -> {
			Factor factorInverso = new Factor();

			factorInverso.IdParametroFinanciero = parametroFinanciero.IdParametroFinanciero;
			factorInverso.Fecha = parametroFinanciero.Fecha;
			factorInverso.IdUso = parametroFinanciero.IdUso;
			
			factorInverso.IdMonedaOrigen = parametroFinanciero.IdMonedaDestino;
			factorInverso.IdMonedaDestino = parametroFinanciero.IdMonedaOrigen;
			factorInverso.Factor = BigDecimal.ONE.divide(parametroFinanciero.Valor, MathContext.DECIMAL128);
			return factorInverso;
		}).collect(Collectors.toList()));
		
		List<Factor> factoresPivoteDefault = factoresPivoteDefaultPrevios.parallelStream().sorted(comparadorFechas).collect(Collectors.toList());
		
		//log.write("FACTORES",factores);
		//log.write("FACTORES PIVOTE FECHA", factoresPivoteUso);
		//log.write("FACTORES PIVOTE DEFAULT",factoresPivoteDefault);
		//log.write("USOS",hUsos);
		tm.addMetric("CALCULO SALIDA");
		salida.p_out_CalculosSalida.addAll(calculosAProcesar.parallelStream().map(calc->{
			Date fecha = Fecha.toDate(calc.Fecha);
			CalculoSalida calculoSalida = new CalculoSalida();
			
			calculoSalida.IdTrabajo = calc.IdTrabajo;
			calculoSalida.IdInterno = calc.IdInterno;
			calculoSalida.IdMonedaOrigen = calc.IdMonedaOrigen;
			calculoSalida.IdMonedaDestino = calc.IdMonedaDestino;
			calculoSalida.ValorCambioLocalModificado = calc.ValorCambioLocalModificado;
			calculoSalida.MontoOrigen = calc.MontoOrigen;
			calculoSalida.IdUso = calc.IdUso;
			
			calculoSalida.Fecha = fecha;
			calculoSalida.FechaCambioLocal = fecha;
			
			String idMonedaOrigenOriginal = calc.IdMonedaOrigen;
			BigDecimal montoOrigenOriginal = calc.MontoOrigen;
			String idMonedaDestinoOriginal = calc.IdMonedaDestino;
			
			Factor factorCambioLocal = null;
			Factor factorLocalCambio = null;
			Factor factorOrigenCambio = null;
			Factor factorCambioDestino = null;
			Factor factorOrigenDestino = null;
			ParametroFinanciero parametroFinanciero = null;
			
			//BUSCAMOS SI EL PARAMETRO FINANCIERO ES CON VALOR MONEDA LOCAL (UF, UTM)
			DefinicionParametro parametroOrigen = (DefinicionParametro) hDefinicionParametros.get(calc.IdMonedaOrigen);
			DefinicionParametro parametroDestino = (DefinicionParametro) hDefinicionParametros.get(calc.IdMonedaDestino);
			
			//Primero debemos cambiar el monto origen a CLP cuando es UF, UTM, etc
			if(parametroOrigen != null && !calc.IdMonedaDestino.equals(parametroOrigen.IdMonedaValor)){
				Factor factorOrigen = null;
				factorOrigen = buscarFactor(factores, calc.IdMonedaOrigen, parametroOrigen.IdMonedaValor, fecha);
				calc.IdMonedaOrigen = parametroOrigen.IdMonedaValor;
				if(factorOrigen != null) calc.MontoOrigen = calc.MontoOrigen.multiply(factorOrigen.Factor, MathContext.DECIMAL128);
			}
			
			if(parametroDestino != null && !calc.IdMonedaOrigen.equals(parametroDestino.IdMonedaValor)){
				calc.IdMonedaDestino = parametroDestino.IdMonedaValor;
			}
			
			//BUSCAMOS EL TIPO CAMBIO 
			Uso uso = (Uso) hUsos.get(calc.IdUso);
			if(calc.ValorCambioLocalModificado.compareTo(BigDecimal.ZERO) != 0){
				factorCambioLocal = new Factor();
				factorCambioLocal.IdMonedaOrigen = ID_MONEDA_CAMBIO;
				factorCambioLocal.IdMonedaDestino = idMonedaLocal;
				factorCambioLocal.Factor = calc.ValorCambioLocalModificado;
				factorCambioLocal.Fecha = Fecha.toDate(calc.Fecha);
				
				factorLocalCambio = new Factor();
				factorLocalCambio.IdMonedaOrigen = idMonedaLocal;
				factorLocalCambio.IdMonedaDestino = ID_MONEDA_CAMBIO;
				factorLocalCambio.Factor = BigDecimal.ONE.divide(calc.ValorCambioLocalModificado, MathContext.DECIMAL128);
				factorLocalCambio.Fecha = Fecha.toDate(calc.Fecha);
			}
			else if(uso == null || uso.Cantidad.equals(0L)){
				for(Factor factor : factoresPivoteDefault){
					if(factor.IdMonedaOrigen.equals(idMonedaLocal) && factor.Fecha.compareTo(fecha) != 1){
						factorLocalCambio = factor;
					}
					if(factor.IdMonedaOrigen.equals(ID_MONEDA_CAMBIO) && factor.Fecha.compareTo(fecha) != 1){
						factorCambioLocal = factor;
					}
					if(factorCambioLocal != null && factorLocalCambio != null) break;
				}
			}else{
				for(Factor factor : factoresPivoteUso){
					if(factor.IdMonedaOrigen.equals(idMonedaLocal) && uso.IdUso.equals(factor.IdUso) && factor.Fecha.compareTo(fecha) != 1){
						factorLocalCambio = factor;
					}
					if(factor.IdMonedaOrigen.equals(ID_MONEDA_CAMBIO) && factor.Fecha.compareTo(fecha) != 1){
						factorCambioLocal = factor;
					}
					if(factorCambioLocal != null && factorLocalCambio != null) break;
				}
			}

			//BUSQUEDA DE FACTORES
			
			//CONVERSION DIRECTA
			factorOrigenDestino = buscarFactor(factores, calc.IdMonedaOrigen, calc.IdMonedaDestino, fecha);
			
			//FACTOR ORIGEN CAMBIO
			if(calc.IdMonedaOrigen.equals(idMonedaLocal)){
				factorOrigenCambio = factorLocalCambio;
			}
			else if(calc.IdMonedaOrigen.equals(ID_MONEDA_CAMBIO)){
				factorOrigenCambio = new Factor();
				factorOrigenCambio.Factor = BigDecimal.ONE;
				factorOrigenCambio.Fecha = Fecha.toDate(calc.Fecha);
				factorOrigenCambio.IdMonedaOrigen = "USD";
				factorOrigenCambio.IdMonedaDestino = "USD";
				factorOrigenCambio.IdParametroFinanciero = "";
			}
			else{
				factorOrigenCambio = buscarFactor(factores, calc.IdMonedaOrigen, ID_MONEDA_CAMBIO, fecha);
			}
			
			factorCambioDestino = buscarFactor(factores, ID_MONEDA_CAMBIO, calc.IdMonedaDestino, fecha);
			
			if(factorOrigenCambio != null){
				calculoSalida.FactorOrigenCambio = factorOrigenCambio.Factor;
				calculoSalida.FechaOrigenCambio = factorOrigenCambio.Fecha;
				calculoSalida.IdTipoRegistroOrigenCambio = factorOrigenCambio.IdParametroFinanciero;
			}
			
			if(factorCambioDestino != null){
				calculoSalida.FactorCambioDestino = factorCambioDestino.Factor;
				calculoSalida.FechaCambioDestino = factorCambioDestino.Fecha;
				calculoSalida.IdTipoRegistroCambioDestino = factorCambioDestino.IdParametroFinanciero;
			}
			
			//CALCULO MONTO LOCAL
			if(calc.IdMonedaOrigen.equals(idMonedaLocal)){
				calculoSalida.MontoLocal = calc.MontoOrigen;
			}
			else if (parametroOrigen != null && calc.IdMonedaDestino.equals(parametroOrigen.IdMonedaValor)) {
				Factor factorMontoLocal = buscarFactor(factores, calc.IdMonedaOrigen, idMonedaLocal, fecha);
				calculoSalida.MontoLocal = factorMontoLocal != null ? factorMontoLocal.Factor.multiply(calc.MontoOrigen) : null;
			}
			else if(factorCambioLocal != null && factorLocalCambio != null && !BigDecimal.ZERO.equals(calculoSalida.FactorOrigenCambio)){
				
				if(!calc.IdMonedaOrigen.equals(idMonedaLocal)) 
					calculoSalida.MontoLocal = calc.MontoOrigen.multiply(calculoSalida.FactorOrigenCambio, MathContext.DECIMAL128).multiply(factorCambioLocal.Factor, MathContext.DECIMAL128);
				
				
				
				calculoSalida.FactorCambioLocal = factorCambioLocal.Factor;
				calculoSalida.FechaCambioLocal =  factorCambioLocal.Fecha;
				calculoSalida.FactorLocalCambio = factorLocalCambio.Factor;
				calculoSalida.FechaLocalCambio =  factorLocalCambio.Fecha;
			}
			
			//CALCULO MONTO DESTINO
			// CASOS MISMA MONEDA EXCEPTO UF Y UTM
			if(calc.IdMonedaOrigen.equals(calc.IdMonedaDestino)){
				calculoSalida.MontoDestino = calc.MontoOrigen;
				calculoSalida.FactorOrigenDestino = BigDecimal.ONE;
			}
			//CASOS MISMA MONEDA PARA UTM-UTM Y UF-UF
			else if(calculoSalida.IdMonedaOrigen.equals(calculoSalida.IdMonedaDestino)) {
				calculoSalida.MontoDestino = montoOrigenOriginal;
				calculoSalida.FactorOrigenDestino = BigDecimal.ONE;
			}
			// CASOS MONEDA_LOCAL ---> MONEDA_EXTRANJERA (EJ: CLP-PEN)
			else if (calc.IdMonedaOrigen.equals(idMonedaLocal) && parametroOrigen == null
				&& (!calc.IdMonedaDestino.equals(idMonedaLocal) && !calc.IdMonedaDestino.equals(ID_MONEDA_CAMBIO) && parametroDestino == null)
				&& factorOrigenCambio != null && factorCambioDestino != null) {
				calculoSalida.MontoDestino = calc.MontoOrigen.multiply((calculoSalida.FactorOrigenCambio.multiply(calculoSalida.FactorCambioDestino)));
				calculoSalida.FactorOrigenDestino = calculoSalida.FactorCambioDestino.multiply(calculoSalida.FactorOrigenCambio);
			}
			// CASOS MONEDA_CAMBIO ---> MONEDA_EXTRANJERA (EJ: USD-PEN)
			else if (calc.IdMonedaOrigen.equals(ID_MONEDA_CAMBIO)
				&& (!calc.IdMonedaDestino.equals(idMonedaLocal) && !calc.IdMonedaDestino.equals(ID_MONEDA_CAMBIO) && parametroDestino == null)
				&& factorOrigenCambio != null && factorCambioDestino != null) {
				calculoSalida.MontoDestino = calc.MontoOrigen.multiply(calculoSalida.FactorCambioDestino);
				calculoSalida.FactorOrigenDestino = calculoSalida.FactorCambioDestino;
			}
			// CASOS MONEDA_EXTRANJERA -> MONEDA_EXTRANJERA (EJ: COP-PEN)
			else if((!calc.IdMonedaOrigen.equals(idMonedaLocal) && !calc.IdMonedaOrigen.equals(ID_MONEDA_CAMBIO) && parametroOrigen == null)
					&& (!calc.IdMonedaDestino.equals(idMonedaLocal) && !calc.IdMonedaDestino.equals(ID_MONEDA_CAMBIO) && parametroDestino == null)
					&& factorOrigenCambio != null && factorCambioDestino != null) {
				calculoSalida.MontoDestino = calc.MontoOrigen.multiply((calculoSalida.FactorOrigenCambio.multiply(calculoSalida.FactorCambioDestino)));
				calculoSalida.FactorOrigenDestino = calculoSalida.FactorOrigenCambio.multiply(calculoSalida.FactorCambioDestino);
			}
			// CASOS MONEDA_LOCAL o MONEDA_EXTRANJERA --> MONEDA_CAMBIO (EJ: CLP - USD)
			else if (calc.IdMonedaDestino.equals(ID_MONEDA_CAMBIO) && !calc.IdMonedaOrigen.equals(calc.IdMonedaDestino) && parametroOrigen == null){
				calculoSalida.MontoDestino = calc.MontoOrigen.multiply(calculoSalida.FactorOrigenCambio);
				calculoSalida.FactorOrigenDestino = calculoSalida.FactorOrigenCambio;
			}
			// CASOS MONEDA_CAMBIO --> MONEDA_LOCAL (USD - CLP)
			else if (calc.IdMonedaOrigen.equals(ID_MONEDA_CAMBIO) && calc.IdMonedaDestino.equals(idMonedaLocal) && parametroDestino == null) {
				calculoSalida.MontoDestino =  calculoSalida.MontoOrigen.multiply(calculoSalida.FactorCambioLocal);
				calculoSalida.FactorCambioDestino = calculoSalida.FactorCambioLocal;
				calculoSalida.FechaCambioDestino = calculoSalida.FechaCambioLocal;
				calculoSalida.FactorOrigenDestino = calculoSalida.FactorCambioLocal;
			}
			// CASOS MONEDA_LOCAL --> UF O UTM, UF O UTM --> MONEDA_LOCAL (EJ: CLP-UF, UF-CLP)
			else if (((calc.IdMonedaOrigen.equals(idMonedaLocal) && parametroOrigen == null && parametroDestino != null)
				|| (parametroOrigen != null && parametroDestino == null && calc.IdMonedaDestino.equals(idMonedaLocal)))
				&& factorOrigenDestino != null){
				calculoSalida.MontoDestino = calc.MontoOrigen.multiply(factorOrigenDestino.Factor, MathContext.DECIMAL128);
				calculoSalida.FactorOrigenDestino = factorOrigenDestino.Factor;	
			}
			// CASOS MONEDA_EXTRANJERA --> MONEDA_LOCAL  (EJ: EUR-CLP, PEN-CLP)
			else if (calc.IdMonedaDestino.equals(idMonedaLocal) && parametroDestino == null && parametroOrigen == null){
				calculoSalida.FactorCambioDestino = calculoSalida.FactorCambioLocal;
				calculoSalida.FechaCambioDestino = calculoSalida.FechaCambioLocal;
				calculoSalida.FactorOrigenDestino = calculoSalida.FactorOrigenCambio.multiply(calculoSalida.FactorCambioLocal);
				calculoSalida.MontoDestino = calc.MontoOrigen.multiply(calculoSalida.FactorOrigenDestino);
			}
			// CASOS MONEDA_CAMBIO --> UF O UTM 	(EJ: USD-UF)
			else if (calc.IdMonedaOrigen.equals(ID_MONEDA_CAMBIO) && parametroDestino != null) {
				calculoSalida.FactorCambioDestino = calculoSalida.FactorCambioLocal;
				calculoSalida.FechaCambioDestino = calculoSalida.FechaCambioLocal;
				Factor factorConsultado = buscarFactor(factores, idMonedaLocal, calculoSalida.IdMonedaDestino, fecha);
				calculoSalida.FactorOrigenDestino = factorConsultado != null ? calculoSalida.FactorCambioLocal.multiply(factorConsultado.Factor) : null;
				calculoSalida.MontoDestino = calc.MontoOrigen.multiply(calculoSalida.FactorOrigenDestino);
			}
			// CASOS UF O UTM --> MONEDA_CAMBIO		(EJ: UTM-USD)
			else if (parametroOrigen != null && calc.IdMonedaDestino.equals(ID_MONEDA_CAMBIO)) {
				calculoSalida.MontoDestino = calc.MontoOrigen.multiply(calculoSalida.FactorOrigenCambio);
				Factor factorConsultado = buscarFactor(factores, calculoSalida.IdMonedaOrigen, idMonedaLocal, fecha);
				calculoSalida.FactorOrigenDestino = factorConsultado != null? factorConsultado.Factor.multiply(BigDecimal.ONE.multiply(calculoSalida.FactorOrigenCambio)) : null;
			}
			// CASOS UF O UTM --> MONEDA EXTRANJERA		(EJ: UF-EUR)
			else if (parametroOrigen != null && (!calc.IdMonedaDestino.equals(ID_MONEDA_CAMBIO) && !calc.IdMonedaDestino.equals(idMonedaLocal) && parametroDestino == null)) {
				calculoSalida.MontoDestino = calc.MontoOrigen.multiply((calculoSalida.FactorOrigenCambio.multiply(calculoSalida.FactorCambioDestino)));
				Factor factorConsultado = buscarFactor(factores, calculoSalida.IdMonedaOrigen, idMonedaLocal, fecha);
				calculoSalida.FactorOrigenDestino = factorConsultado != null ? factorConsultado.Factor.multiply(calculoSalida.FactorOrigenCambio).multiply(calculoSalida.FactorCambioDestino): null;
			}
			// CASOS UF O UTM --> UF O UTM
			else if (parametroOrigen != null && parametroDestino != null && !calculoSalida.IdMonedaOrigen.equals(calculoSalida.IdMonedaDestino)) {
				calculoSalida.MontoDestino = calc.MontoOrigen.multiply(factorOrigenDestino.Factor, MathContext.DECIMAL128);
				Factor factorCambio = buscarFactor(factores, calculoSalida.IdMonedaOrigen, idMonedaLocal, fecha);
				Factor factorDestino = buscarFactor(factores, idMonedaLocal, calculoSalida.IdMonedaDestino, fecha);
				calculoSalida.FactorOrigenDestino = null;
				if (factorCambio != null && factorDestino != null)
					calculoSalida.FactorOrigenDestino = factorCambio.Factor.multiply(factorDestino.Factor);
			}
			// CASOS MONEDA_EXTRANJERA --> UF O UTM		(EJ: PEN-UF)
			else if ((!calc.IdMonedaOrigen.equals(ID_MONEDA_CAMBIO) && !calc.IdMonedaOrigen.equals(idMonedaLocal) && parametroOrigen == null) && parametroDestino != null) {
				calculoSalida.FactorCambioDestino = calculoSalida.FactorCambioLocal;
				calculoSalida.FechaCambioDestino = calculoSalida.FechaCambioLocal;
				Factor factorConsultado = buscarFactor(factores, idMonedaLocal, calculoSalida.IdMonedaDestino, fecha);
				calculoSalida.FactorOrigenDestino = factorConsultado != null ? factorConsultado.Factor.multiply(calculoSalida.FactorCambioLocal).multiply(calculoSalida.FactorOrigenCambio) : null;
				calculoSalida.MontoDestino = calc.MontoOrigen.multiply(calculoSalida.FactorOrigenDestino);
			}

			//PARA PARAMETROS FINANCIEROS COMO UF.
			for(ParametroFinanciero parametro : parametroFinancieroOriginales){
				if((parametro.IdMonedaOrigen.equals(calc.IdMonedaOrigen) || parametro.IdMonedaOrigen.equals(calc.IdMonedaDestino)) && parametro.Fecha.compareTo(fecha) != 1){
					parametroFinanciero = parametro;
					break;
				}
			}
			//COMPLETAMOS EL VALOR PARAMETRO FINANCIERO
			if(parametroFinanciero != null){
				calculoSalida.ValorParametroFinanciero = parametroFinanciero.Valor;
				calculoSalida.FechaParametroFinanciero = parametroFinanciero.Fecha;
			}else{
				calculoSalida.ValorParametroFinanciero = BigDecimal.ONE;
				calculoSalida.FechaParametroFinanciero = Fecha.toDate("0001-01-01");
			}
			
			//VOLVEMOS A LO ORIGINAL
			calc.IdMonedaOrigen = idMonedaOrigenOriginal;
			calc.IdMonedaDestino = idMonedaDestinoOriginal;
			calc.MontoOrigen = montoOrigenOriginal;
						
			//COMPLETAMOS OK O RECHAZO
			if(calculoSalida.MontoDestino == null){
				calculoSalida.Resultado = "RECHAZO";
				calculoSalida.CodigoMensaje = "ERROR_CALCULAR_PARIDAD_MONTO_DESTINO";
			}else if(calculoSalida.MontoLocal == null){
				calculoSalida.Resultado = "RECHAZO";
				calculoSalida.CodigoMensaje = "ERROR_CALCULAR_PARIDAD_MONTO_LOCAL";
			}else{
				calculoSalida.Resultado = "OK";
				calculoSalida.CodigoMensaje = "OK";
			}
			return calculoSalida;
		}).collect(Collectors.toList()));
		FactoresEncontrados.clear();
		tm.endMetric().clearMetrics();
		logger.info("FIN::CalcularParidad");
		return salida;
	}
	
	public Factor buscarFactor(List<Factor> factores, String IdMonedaOrigen, String IdMonedaDestino, Date Fecha) {
		String llave = IdMonedaOrigen+IdMonedaDestino+Fecha.toString();
		Factor factorEncontrado = FactoresEncontrados.get(llave);
		if (factorEncontrado != null) 
			return copiarFactor(factorEncontrado);
		for(Factor factor : factores){
			if(factor.IdMonedaOrigen.equals(IdMonedaOrigen) && factor.IdMonedaDestino.equals(IdMonedaDestino) && factor.Fecha.compareTo(Fecha) != 1){
				FactoresEncontrados.put(llave, factor);
				return copiarFactor(factor);
			}
		}
		return null;
	}
	
	public Factor copiarFactor(Factor antiguo) {
		Factor nuevo = new Factor();
		nuevo.IdParametroFinanciero = antiguo.IdParametroFinanciero;
		nuevo.IdMonedaOrigen = antiguo.IdMonedaOrigen;
		nuevo.IdMonedaDestino = antiguo.IdMonedaDestino;
		nuevo.Fecha = antiguo.Fecha;
		nuevo.Factor = antiguo.Factor;
		nuevo.IdUso = antiguo.IdUso;
		return nuevo;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection conn = PlataformaUtils.getPropertiesConnection("qa02","connections.properties")) {
			CalcularParidad demo = new CalcularParidad();
			
			CalcularParidadEntrada entrada = new CalcularParidadEntrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdTrabajo = 0L;
			
			
			CalculoEntrada arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-31";
			arg0.IdInterno = 0L;
			arg0.IdMonedaOrigen = "USD";
			arg0.IdMonedaDestino = "PEN";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "COMPRA MILA";
			arg0.MontoOrigen = BigDecimal.ONE;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);
			
			
			String json = "{\"p_IdEmpresa\":1,\"p_IdTrabajo\":0,\"p_CalculosEntrada\":[{\"IdTrabajo\":0,\"IdInterno\":3,\"Fecha\":\"2019-01-28\",\"IdMonedaOrigen\":\"EUR\",\"IdMonedaDestino\":\"CLP\",\"ValorCambioLocalModificado\":0,\"MontoOrigen\":1,\"IdUso\":\"CARTOLA\"},{\"IdTrabajo\":0,\"IdInterno\":4,\"Fecha\":\"2019-01-28\",\"IdMonedaOrigen\":\"CLP\",\"IdMonedaDestino\":\"EUR\",\"ValorCambioLocalModificado\":0,\"MontoOrigen\":870700076474.6439460100000000000000,\"IdUso\":\"CARTOLA\"}]}";
			Gson gson = Utils.getGsonBuilder();
			entrada = new CalcularParidadEntrada();
			entrada = gson.fromJson(json, CalcularParidadEntrada.class);
			
			//String json = "{\"p_IdEmpresa\":1,\"p_IdTrabajo\":0,\"p_CalculosEntrada\":[]}";
			
			/*arg0.Fecha = "2018-07-22";
			arg0.IdInterno = 0L;
			arg0.IdMonedaOrigen = "CLP";
			arg0.IdMonedaDestino = "CLP";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "";
			arg0.MontoOrigen = new BigDecimal("10000");
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);
			
			arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-31";
			arg0.IdInterno = 10L;
			arg0.IdMonedaOrigen = "CLP";
			arg0.IdMonedaDestino = "UF";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "";
			arg0.MontoOrigen = BigDecimal.ONE;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);
			
			
			arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-22";
			arg0.IdInterno = 1L;
			arg0.IdMonedaOrigen = "UF";
			arg0.IdMonedaDestino = "CLP";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "";
			arg0.MontoOrigen = BigDecimal.ONE;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);
			
			
			arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-31";
			arg0.IdInterno = 2L;
			arg0.IdMonedaOrigen = "CLP";
			arg0.IdMonedaDestino = "USD";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "";
			arg0.MontoOrigen = BigDecimal.ONE;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);
			
			
			arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-31";
			arg0.IdInterno = 3L;
			arg0.IdMonedaOrigen = "CLP";
			arg0.IdMonedaDestino = "USD";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "CARTERA";
			arg0.MontoOrigen = BigDecimal.ONE;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);
			
			arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-31";
			arg0.IdInterno = 4L;
			arg0.IdMonedaOrigen = "CLP";
			arg0.IdMonedaDestino = "EUR";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "";
			arg0.MontoOrigen = BigDecimal.ONE;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);
			
			
			arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-31";
			arg0.IdInterno = 5L;
			arg0.IdMonedaOrigen = "CLP";
			arg0.IdMonedaDestino = "EUR";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "CARTERA";
			arg0.MontoOrigen = BigDecimal.ONE;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);
			
			
			arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-31";
			arg0.IdInterno = 15L;
			arg0.IdMonedaOrigen = "EUR";
			arg0.IdMonedaDestino = "CLP";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "";
			arg0.MontoOrigen = BigDecimal.ONE;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);
			
			
			arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-31";
			arg0.IdInterno = 6L;
			arg0.IdMonedaOrigen = "USD";
			arg0.IdMonedaDestino = "UF";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "";
			arg0.MontoOrigen = BigDecimal.ONE;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);
			
			
			
			arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-31";
			arg0.IdInterno = 7L;
			arg0.IdMonedaOrigen = "USD";
			arg0.IdMonedaDestino = "CLP";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "";
			arg0.MontoOrigen = BigDecimal.ONE;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);
			
			
			arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-31";
			arg0.IdInterno = 8L;
			arg0.IdMonedaOrigen = "USD";
			arg0.IdMonedaDestino = "USD";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "";
			arg0.MontoOrigen = BigDecimal.ONE;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);

			arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-31";
			arg0.IdInterno = 8L;
			arg0.IdMonedaOrigen = "USD";
			arg0.IdMonedaDestino = "EUR";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "";
			arg0.MontoOrigen = BigDecimal.ONE;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);
			
			
			arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-31";
			arg0.IdInterno = 9L;
			arg0.IdMonedaOrigen = "PEN";
			arg0.IdMonedaDestino = "USD";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "";
			arg0.MontoOrigen = BigDecimal.ONE;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);

			arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-31";
			arg0.IdInterno = 9L;
			arg0.IdMonedaOrigen = "PEN";
			arg0.IdMonedaDestino = "EUR";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "";
			arg0.MontoOrigen = BigDecimal.ONE;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);
			
			
			arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-31";
			arg0.IdInterno = 10L;
			arg0.IdMonedaOrigen = "YEN";
			arg0.IdMonedaDestino = "USD";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "";
			arg0.MontoOrigen = BigDecimal.ONE;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);*/

			
			CalcularParidadSalida adf = demo.logica(conn, entrada );
			System.out.println(adf.toString());
 			
 		}
		 	
		System.out.println("== END ==");
	}
	
	public static class ParametroFinanciero_Proceso {
		public Long IdInterno;
		public String IdParametroFinanciero;
		public Date FechaParametroFinanciero;
		public String Nombre;
		public BigDecimal Valor;
		public String IdMoneda;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	

}