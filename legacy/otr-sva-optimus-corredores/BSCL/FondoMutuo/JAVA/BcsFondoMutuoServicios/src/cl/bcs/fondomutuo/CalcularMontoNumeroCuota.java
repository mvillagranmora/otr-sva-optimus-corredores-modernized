package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.fondomutuo.pojo.SerieFondoSolicitudes;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.MonedaFormato;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.tas.EntregarPreciosTasas;
import cl.bcs.plataforma.tas.EntregarPreciosTasas.EntregarPreciosTasas_Salida_PreciosTasas;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;
import datadog.trace.api.Trace;

public class CalcularMontoNumeroCuota implements JavaCall {
	
	private final static Logger logger = LogManager.getLogger(CalcularMontoNumeroCuota.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	private final String SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO = loadResourceToString("cl/bcs/fondomutuo/sql/CalcularMontoNumeroCuota_SelectValorCuotaTipoCambio.sql");
	private final String SQL_SELECT_SERIE_FONDO = loadResourceToString("cl/bcs/fondomutuo/sql/ComunesSolicitudes_SelectSerieFondo.sql");
		
	public static class EntradaCalcularMontoNumeroCuota {
		public Long p_IdEmpresa;
		public List<Serie> p_Series;
		
		public EntradaCalcularMontoNumeroCuota() {
			this.p_Series = new ArrayList<Serie>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Serie extends BcsPojo{
		public Long IdInterno;
		public String Fecha;
		public String IdSerieFondo;
	 	public String CalculoPorMontoCuota;
	 	public BigDecimal Monto;
	 	public BigDecimal NumeroCuotas;
	 	public String IdMonedaFondo;
	 	public String IdMonedaInversion;
	 	public String TipoSolicitud;
	 	public String TipoFactorAjuste;
	 	public Long IdEmpresa;
	 	public BigDecimal Porcentaje;
	 	public String IdTipoMovimientoFondoMutuo;
	 	
	 	public Serie() {
	 		this.IdTipoMovimientoFondoMutuo = "";
	 	}
	 	
	 	
	}
	
	public static class SalidaCalcularMontoNumeroCuota{
		public List<Resultado> P_OUT_RESULTADO;
		
		public SalidaCalcularMontoNumeroCuota(){
			this.P_OUT_RESULTADO = new ArrayList<Resultado>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ValorCuotaTipoParidad{
		public String IdSerieFondo;
		public BigDecimal ValorCuota;
		public BigDecimal ValorDolarAporte;
		public BigDecimal ValorDolarRescate;
		public Long DecimalesCuota;
		public Long DecimalesValorCuota;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SeriesValorCuotaTipoParidad{
		public Long IdInterno;
		public String Fecha;
		public String IdSerieFondo;
	 	public String CalculoPorMontoCuota;
	 	public BigDecimal Monto;
	 	public BigDecimal NumeroCuotas;
	 	public String IdMonedaFondo;
	 	public String IdMonedaInversion;
	 	public String TipoSolicitud;
	 	public String TipoFactorAjuste;
	 	public Long IdEmpresa;
	 	public BigDecimal MontoPorCuota;
	 	public BigDecimal ValorCuota;
	 	public BigDecimal TipoCambio;
	 	public Long DecimalesCuota;
	 	public Long DecimalesValorCuota;
	 	public String Diferido;
	 	public BigDecimal ValorParidad;
	 	public BigDecimal MontoDestino;
	 	public BigDecimal MontoMonedaSistema;
	 	public BigDecimal Porcentaje;
	 	public String FechaValorCuota;
	 	
	 	public SeriesValorCuotaTipoParidad(){
	 		this.ValorCuota = BigDecimal.ZERO;
	 		this.TipoCambio = BigDecimal.ZERO;
	 		this.ValorParidad = new BigDecimal(-1);
	 		this.MontoDestino = BigDecimal.ZERO;
	 		this.Porcentaje = BigDecimal.ZERO;
	 		this.DecimalesCuota = 0L;
	 		this.FechaValorCuota = "0001-01-01";
	 	}
	 	
	 	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Resultado{
		public Long IdInterno;
		public String TipoSolicitud;
	 	public BigDecimal Monto;
	 	public BigDecimal NumeroCuotas;
	 	public BigDecimal ValorCuota;
	 	public BigDecimal MontoDestino;
	 	public BigDecimal ValorParidad;
	 	public BigDecimal ValorFactorAjuste;
	 	public BigDecimal CostoEnFactorAjuste;
	 	public String IdMonedaSistema;
	 	public BigDecimal MontoMonedaSistema;
	 	public String Resultado;
	 	public String CodigoMensaje;
	 	public BigDecimal Porcentaje;
	 	public Long DecimalesValorCuota;
	 	public Long DecimalesNumeroCuotas;
	 	public String FechaValorCuota;
	 	
	 	public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCalcularMontoNumeroCuota jin = gson.fromJson(in.toString(), EntradaCalcularMontoNumeroCuota.class);
		SalidaCalcularMontoNumeroCuota out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	@Trace	
	public SalidaCalcularMontoNumeroCuota logica(Connection con, EntradaCalcularMontoNumeroCuota entrada) throws Exception {
		logger.info("INICIO CalcularMontoNumeroCuota"); 
		long inicioCalcularMontoNumeroCuota = System.currentTimeMillis();
		SalidaCalcularMontoNumeroCuota out = new SalidaCalcularMontoNumeroCuota();
		
		SQL sql=new SQL();
		
		HashMap<String,Object> params = new HashMap<String,Object>();

		String usaDolarObservado = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "FONDOMUTUO", "UsaDolarObservado");
		String idMoneda = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		List<String> series = new ArrayList<String>();
		for(Serie serie : entrada.p_Series){
			if(!series.contains(serie.IdSerieFondo)){
				series.add(serie.IdSerieFondo);
			}
		}
		Map<Long,Object> hashVCTP = obtenerPrecios(con,entrada);
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", series);
		long inicioSelectValorCuotaTipoCambio = System.currentTimeMillis();
		logger.debug("INICIO::SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO");
		List<ValorCuotaTipoParidad> valorCuotaTipoParidad = sql.query(con, SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO, params, ValorCuotaTipoParidad.class);
		logger.debug("FIN::SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO");
		long finSelectValorCuotaTipoCambio = System.currentTimeMillis();
		logger.debug("Tiempo SQL_SELECT_VALOR_CUOTA_TIPO_CAMBIO: " + (finSelectValorCuotaTipoCambio - inicioSelectValorCuotaTipoCambio));
		
		if (valorCuotaTipoParidad.size() == 0) {
			
			for(Serie serie : entrada.p_Series){
				Resultado resultadoErroneo = new Resultado();
				resultadoErroneo.IdInterno = serie.IdInterno;
				resultadoErroneo.CodigoMensaje = "ERROR_VALORCUOTA";
				resultadoErroneo.Resultado = "Error al obtener Valor Cuota: " + serie.IdSerieFondo;
				out.P_OUT_RESULTADO.add(resultadoErroneo);
			}
			return out;
		}
		
		HashMap<String, Object> hashVCTPDecimales = Utils.List2Hash(valorCuotaTipoParidad, "IdSerieFondo");
		
		if (hashVCTP.size() == 0 || hashVCTPDecimales.size() == 0) {
			
			for(String serie : series){
				Resultado resultadoErroneo = new Resultado();
				resultadoErroneo.CodigoMensaje = "ERROR_VALORCUOTA";
				resultadoErroneo.Resultado = "Error al obtener Valor Cuota: " + serie;
				out.P_OUT_RESULTADO.add(resultadoErroneo);
			}
			return out;
		}
		
		List<SeriesValorCuotaTipoParidad> seriesValorCuotaTipoParidad = new ArrayList<SeriesValorCuotaTipoParidad>();
		
		
		HashMap<Long,Object> paridades = new HashMap<Long,Object>();
		HashMap<Long,Object> montosParidad = new HashMap<Long,Object>();
		
		CalcularParidad calcularParidad = new CalcularParidad();
		
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 1L;
		for(Serie serie : entrada.p_Series){
			SeriesValorCuotaTipoParidad serieValorCuotaTipoParidad = new SeriesValorCuotaTipoParidad();
			Utils.copyPojo(serie, serieValorCuotaTipoParidad);
			EntregarPreciosTasas_Salida_PreciosTasas VCTPValorCuota = (EntregarPreciosTasas_Salida_PreciosTasas) hashVCTP.get(serie.IdInterno);
			ValorCuotaTipoParidad valorCuota =(ValorCuotaTipoParidad) hashVCTPDecimales.get(serie.IdSerieFondo);
			if(valorCuota != null && VCTPValorCuota != null){
				valorCuota.IdSerieFondo = serie.IdSerieFondo;
				valorCuota.ValorCuota = VCTPValorCuota.Precio;
				serieValorCuotaTipoParidad.FechaValorCuota = VCTPValorCuota.FechaFuente != null ? Fecha.toFormat(VCTPValorCuota.FechaFuente,"yyyy-MM-dd") : "0001-01-01";
				serieValorCuotaTipoParidad.ValorCuota = valorCuota.ValorCuota != null ? valorCuota.ValorCuota : BigDecimal.ZERO;
				serieValorCuotaTipoParidad.DecimalesCuota = valorCuota.DecimalesCuota;
				serieValorCuotaTipoParidad.DecimalesValorCuota = valorCuota.DecimalesValorCuota;
				serieValorCuotaTipoParidad.TipoCambio = _c.TipoSolicitudAporte.equalsIgnoreCase(serieValorCuotaTipoParidad.TipoSolicitud) ? valorCuota.ValorDolarAporte : valorCuota.ValorDolarRescate;
			}
			serieValorCuotaTipoParidad.Diferido = _c.NO;
			
			if (serieValorCuotaTipoParidad.NumeroCuotas != null ) {
				serieValorCuotaTipoParidad.MontoPorCuota = serieValorCuotaTipoParidad.ValorCuota.multiply(serieValorCuotaTipoParidad.NumeroCuotas, MathContext.DECIMAL128);
			}
			else {
				//serieValorCuotaTipoParidad.MontoPorCuota = serieValorCuotaTipoParidad.ValorCuota.multiply(serie.Monto.divide(valorCuota.ValorCuota), MathContext.DECIMAL128);
				serieValorCuotaTipoParidad.MontoPorCuota = serie.Monto;
			}
			
			serieValorCuotaTipoParidad.Porcentaje = serie.Porcentaje;
			if(!serieValorCuotaTipoParidad.IdMonedaInversion.equalsIgnoreCase(serieValorCuotaTipoParidad.IdMonedaFondo)){
				serieValorCuotaTipoParidad.Diferido = _c.SI;
				CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.IdTrabajo = 1L;
				calculoEntrada.IdInterno = serieValorCuotaTipoParidad.IdInterno;
				calculoEntrada.Fecha = serieValorCuotaTipoParidad.Fecha;
				calculoEntrada.IdMonedaOrigen = _c.CalculoPorMonto.equalsIgnoreCase(serieValorCuotaTipoParidad.CalculoPorMontoCuota) ? serieValorCuotaTipoParidad.IdMonedaInversion : serieValorCuotaTipoParidad.IdMonedaFondo;
				calculoEntrada.IdMonedaDestino = _c.CalculoPorMonto.equalsIgnoreCase(serieValorCuotaTipoParidad.CalculoPorMontoCuota) ? serieValorCuotaTipoParidad.IdMonedaFondo : serieValorCuotaTipoParidad.IdMonedaInversion;
				calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntrada.MontoOrigen = _c.CalculoPorMonto.equalsIgnoreCase(serieValorCuotaTipoParidad.CalculoPorMontoCuota) ? serieValorCuotaTipoParidad.Monto : serieValorCuotaTipoParidad.MontoPorCuota;
				calculoEntrada.IdUso = "";
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
			}
			seriesValorCuotaTipoParidad.add(serieValorCuotaTipoParidad);
		}
		
		if(calcularParidadEntrada.p_CalculosEntrada.size() > 0){
			long inicioCalculoParidad = System.currentTimeMillis();
			CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
			long finCalculoParidad = System.currentTimeMillis();
			logger.debug("Tiempo CalcularParidad: " + (finCalculoParidad - inicioCalculoParidad));
			paridades = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
		}
		
		//CALCULO MONEDA SISTEMA
		calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 2L;
				
		for(SeriesValorCuotaTipoParidad serie : seriesValorCuotaTipoParidad){
			if (serie.ValorCuota.equals(BigDecimal.ZERO)) {
				continue;
			}
			
			if(_c.SI.equalsIgnoreCase(serie.Diferido)){
				CalculoSalida paridad = (CalculoSalida) paridades.get(serie.IdInterno);
				BigDecimal montoDestino = BigDecimal.ZERO;
				if(_c.NO.equalsIgnoreCase(usaDolarObservado)){
					if(_c.Dolar.equalsIgnoreCase(serie.IdMonedaFondo)){
						serie.ValorParidad = serie.TipoCambio;
						montoDestino = _c.CalculoPorMonto.equalsIgnoreCase(serie.CalculoPorMontoCuota) ? serie.Monto.divide(serie.ValorParidad, MathContext.DECIMAL128) : serie.MontoPorCuota.multiply(serie.ValorParidad, MathContext.DECIMAL128);
					}else if(idMoneda.equalsIgnoreCase(serie.IdMonedaFondo) && _c.Dolar.equalsIgnoreCase(serie.IdMonedaInversion)){
						serie.ValorParidad = serie.TipoCambio;
						montoDestino = _c.CalculoPorMonto.equalsIgnoreCase(serie.CalculoPorMontoCuota) ?  serie.Monto.multiply(serie.ValorParidad, MathContext.DECIMAL128) : serie.MontoPorCuota.divide(serie.ValorParidad, MathContext.DECIMAL128);
					}else if(paridad != null){
						serie.ValorParidad = paridad.FactorCambioLocal;
						montoDestino = paridad.MontoDestino;
					}
				}else if(paridad != null){
					serie.ValorParidad = paridad.FactorCambioLocal;
					montoDestino = paridad.MontoDestino;
				}
				
				if(_c.CalculoPorMonto.equalsIgnoreCase(serie.CalculoPorMontoCuota)){
					serie.NumeroCuotas = montoDestino.divide(serie.ValorCuota, MathContext.DECIMAL128).setScale(serie.DecimalesCuota.intValue(), RoundingMode.HALF_DOWN);
					BigDecimal monto = serie.Monto;
					serie.Monto = MonedaFormato.formatear(con, montoDestino, MonedaFormato.MONTO, serie.IdMonedaFondo).valor;
					serie.MontoDestino = monto;
					
				}else{
					serie.Monto = MonedaFormato.formatear(con, serie.MontoPorCuota, MonedaFormato.MONTO, serie.IdMonedaFondo).valor;
					serie.MontoDestino = montoDestino;
				}
			}else{
				serie.ValorParidad = BigDecimal.ONE;
				if(_c.CalculoPorMonto.equalsIgnoreCase(serie.CalculoPorMontoCuota)){
					serie.NumeroCuotas = serie.Monto.divide(serie.ValorCuota, MathContext.DECIMAL128).setScale(serie.DecimalesCuota.intValue(), RoundingMode.HALF_DOWN);
					serie.Monto = MonedaFormato.formatear(con, serie.Monto, MonedaFormato.MONTO, serie.IdMonedaFondo).valor;
				}else{
					if (serie.MontoPorCuota.compareTo(BigDecimal.ZERO)==0 && serie.ValorCuota.compareTo(BigDecimal.ZERO) != 0) {
						serie.NumeroCuotas = serie.Monto.divide(serie.ValorCuota, MathContext.DECIMAL128).setScale(serie.DecimalesCuota.intValue(), RoundingMode.HALF_DOWN);
						serie.Monto = MonedaFormato.formatear(con, serie.Monto, MonedaFormato.MONTO, serie.IdMonedaFondo).valor;
					
					}else {
						serie.Monto = MonedaFormato.formatear(con, serie.MontoPorCuota, MonedaFormato.MONTO, serie.IdMonedaFondo).valor;
					}
					
				}
				serie.MontoDestino = serie.Monto;
				serie.MontoMonedaSistema = serie.Monto;
			}
			
			int s = serie.IdInterno > 0 ? 1 : -1;
			if(idMoneda.equalsIgnoreCase(serie.IdMonedaFondo)){
				serie.MontoMonedaSistema = serie.Monto;
			}else{
				CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.IdTrabajo = 2L;
				calculoEntrada.IdInterno = (serie.IdInterno * 4) + (1*s);
				calculoEntrada.Fecha = serie.Fecha;
				calculoEntrada.IdMonedaOrigen = serie.IdMonedaFondo;
				calculoEntrada.IdMonedaDestino = idMoneda;
				calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntrada.MontoOrigen =  serie.Monto;
				calculoEntrada.IdUso = "";
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
			}
			
			CalculoEntrada calculoEntrada = new CalculoEntrada();
			calculoEntrada.IdTrabajo = 2L;
			calculoEntrada.IdInterno = (serie.IdInterno * 4) + (2*s);
			calculoEntrada.Fecha = serie.Fecha;
			calculoEntrada.IdMonedaOrigen = serie.IdMonedaFondo;
			calculoEntrada.IdMonedaDestino = serie.TipoFactorAjuste;
			calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntrada.MontoOrigen =  serie.Monto;
			calculoEntrada.IdUso = "";
			calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
			
			CalculoEntrada calculoEntradaUF = new CalculoEntrada();
			calculoEntradaUF.IdTrabajo = 2L;
			calculoEntradaUF.IdInterno = (serie.IdInterno * 4) + (3*s);
			calculoEntradaUF.Fecha = serie.Fecha;
			calculoEntradaUF.IdMonedaOrigen = serie.TipoFactorAjuste;
			calculoEntradaUF.IdMonedaDestino = idMoneda;
			calculoEntradaUF.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntradaUF.MontoOrigen =  BigDecimal.ONE;
			calculoEntradaUF.IdUso = "";
			calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaUF);
			
		}
		
		long inicioCalculoParidad = System.currentTimeMillis();
		CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
		long finCalculoParidad = System.currentTimeMillis();
		logger.debug("Tiempo CalcularParidad: " + (finCalculoParidad - inicioCalculoParidad));
		
		if(calcularParidadEntrada.p_CalculosEntrada.size() > 0){
			montosParidad = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
		}
		
		for(SeriesValorCuotaTipoParidad serie : seriesValorCuotaTipoParidad){
			int s = serie.IdInterno > 0 ? 1 : -1;
			CalculoSalida montoMonedaSistema = (CalculoSalida) montosParidad.get((serie.IdInterno * 4) + (1*s));
			CalculoSalida montoEnUF = (CalculoSalida) montosParidad.get((serie.IdInterno * 4) + (2*s));
			CalculoSalida uf = (CalculoSalida) montosParidad.get((serie.IdInterno * 4) + (3*s));
			Resultado resultado = new Resultado();
			resultado.IdInterno = serie.IdInterno;
			resultado.TipoSolicitud = serie.TipoSolicitud;
			//EN CASO DE NO DEFINIRSE EL NUMERO DE DE CUOTAS SE PROCEDE A SU CALCULO
		 	resultado.Monto= MonedaFormato.formatear(con, serie.Monto,  MonedaFormato.MONTO, serie.IdMonedaFondo).valor;
		 	resultado.DecimalesNumeroCuotas = serie.DecimalesCuota != null ? serie.DecimalesCuota : 0L;
		 	resultado.DecimalesValorCuota = serie.DecimalesValorCuota != null ? serie.DecimalesValorCuota : 0L;
		 	if (serie.NumeroCuotas != null) {
		 		resultado.NumeroCuotas = serie.NumeroCuotas.setScale(resultado.DecimalesNumeroCuotas.intValue(), RoundingMode.HALF_DOWN);
		 	}
		 	else {
		 		resultado.NumeroCuotas = serie.Monto.divide(serie.ValorCuota, MathContext.DECIMAL128).setScale(resultado.DecimalesNumeroCuotas.intValue(), RoundingMode.HALF_DOWN);
		 	}
		 	if (serie.NumeroCuotas.compareTo(BigDecimal.ZERO)==0) {
				resultado.NumeroCuotas = serie.Monto.divide(serie.ValorCuota, MathContext.DECIMAL128).setScale(serie.DecimalesCuota.intValue()+1, RoundingMode.HALF_UP);
			} 
		 	entrada.p_Series.parallelStream().forEach(serieEntrada -> {
		 		if ("RESREINV".equalsIgnoreCase(serieEntrada.IdTipoMovimientoFondoMutuo) && BigDecimal.ZERO.compareTo(serie.NumeroCuotas) == 0) { 
		 			if(serieEntrada.IdInterno.equals(serie.IdInterno)) {
		 				resultado.NumeroCuotas = serie.Monto.divide(serie.ValorCuota, MathContext.DECIMAL128).setScale(serie.DecimalesCuota.intValue()+1, RoundingMode.HALF_UP);			 				 				
		 			}
		 		}
		 	});
		 	resultado.ValorCuota = serie.ValorCuota;
		 	resultado.MontoDestino = MonedaFormato.formatear(con, serie.MontoDestino, MonedaFormato.MONTO, serie.IdMonedaInversion).valor;
		 	resultado.ValorParidad = serie.ValorParidad;
		 	resultado.ValorFactorAjuste = uf != null && uf.FactorOrigenDestino != null ? uf.FactorOrigenDestino : BigDecimal.ZERO;
		 	resultado.CostoEnFactorAjuste = montoEnUF != null && montoEnUF.MontoDestino != null ? montoEnUF.MontoDestino : BigDecimal.ZERO;
		 	resultado.IdMonedaSistema = idMoneda;
		 	resultado.MontoMonedaSistema = idMoneda.equalsIgnoreCase(serie.IdMonedaFondo) ? serie.Monto : montoMonedaSistema != null ? MonedaFormato.formatear(con, montoMonedaSistema.MontoDestino, MonedaFormato.MONTO, idMoneda).valor : BigDecimal.ZERO;
		 	resultado.Porcentaje = serie.Porcentaje; //entrada.p_Series.get(0).Porcentaje; 
		 	resultado.FechaValorCuota = serie.FechaValorCuota;
		 	resultado.Resultado = "OK";
		 	resultado.CodigoMensaje = "OK";
		 	if(resultado.ValorCuota.compareTo(BigDecimal.ZERO) == 0) {
		 		resultado.CodigoMensaje = "ERROR_VALOR_CUOTA";
		 		resultado.Resultado = "Error al obtener Valor Cuota : " +serie.IdSerieFondo;
		 	}
		 	out.P_OUT_RESULTADO.add(resultado);
		}
		long finCalcularMontoNumeroCuota = System.currentTimeMillis();
		logger.debug("Tiempo CalcularMontoNumeroCuota: " + (finCalcularMontoNumeroCuota - inicioCalcularMontoNumeroCuota));
		logger.info("FIN CalcularMontoNumeroCuota");
				
		return out;
	}
	
	

	
	/**
	 * Retorna los precios de los instrumentos.
	 * @param con Conexion a base de datos
	 * @param entrada Entrada del servicio ActualizarCarterasFondosMutuos con la empresa, modulo y usuario
	 * @param series List con las series de fondo que se requiere obtener el precio
	 * @return Map con el IdSerieFondo como key y el objeto EntregarPreciosTasas_Salida_PreciosTasas como valor el cual contiene la fecha de la fuente, nombre de la fuente y precio.
	 */
	private Map<Long,Object> obtenerPrecios(Connection con, EntradaCalcularMontoNumeroCuota entrada) throws Exception {
		logger.info("INICIO::ActualizarCarterasFondosMutuos.obtenerPrecios");
		//Obtiene datos Fondos

		List<String> series = new ArrayList<String>();
		for(Serie mov : entrada.p_Series){
			if(!series.contains(mov.IdSerieFondo)) series.add(mov.IdSerieFondo);
		}	
		SQL sql=new SQL();
		HashMap<String,Object> params = new HashMap<String,Object>();
		params.put("IdEmpresa", entrada.p_IdEmpresa);
		params.put("IdSerieFondo", series);
		logger.debug("INICIO::SQL_SELECT_SERIE_FONDO");
		List<SerieFondoSolicitudes> seriesSolicitudes = sql.query(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class);
		logger.debug("FIN::SQL_SELECT_SERIE_FONDO");
		params.clear();
		if(seriesSolicitudes.size() == 0)
			return null;
		HashMap<String,Object> hseries = Utils.List2Hash(seriesSolicitudes, "IdSerieFondo");
		EntregarPreciosTasas precioTasa = new EntregarPreciosTasas();
		EntregarPreciosTasas.EntregarPreciosTasas_Entrada  paramsPrecioTasa = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada();
		paramsPrecioTasa.p_IdEmpresa = entrada.p_IdEmpresa;
		paramsPrecioTasa.p_IdModulo = null;
		paramsPrecioTasa.p_IdPais = obtenerPaisSistema(con,entrada.p_IdEmpresa);
		paramsPrecioTasa.p_IdUsuario = null;
		paramsPrecioTasa.p_DatosInstrumentos = entrada.p_Series.parallelStream()
		.map(dato->{
			SerieFondoSolicitudes serie = (SerieFondoSolicitudes) hseries.get(dato.IdSerieFondo);
			EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos paramsRegIn = new EntregarPreciosTasas.EntregarPreciosTasas_Entrada_DatosInstrumentos();
			paramsRegIn.FechaConsulta = Fecha.utilDateToSqlDate(Fecha.toDate(dato.Fecha));
			paramsRegIn.IdClasificacionTipoInstrumento = "FM";
			paramsRegIn.IdInstrumento = serie.IdInstrumento;
			paramsRegIn.IdMoneda = dato.IdMonedaFondo;
			if(dato.IdInterno != 0 && dato.IdInterno != null)
				paramsRegIn.IdRegistro = dato.IdInterno;
			else
				paramsRegIn.IdRegistro = 0L;
			paramsRegIn.IdTipoCriterioTasa = "0";
			paramsRegIn.IdTipoInstrumento = serie.IdTipoInstrumento;
			paramsRegIn.IdUso = "INGGTA".equals(dato.IdTipoMovimientoFondoMutuo)?"GARANTIA":"CARTERA";
			paramsRegIn.Nemotecnico = dato.IdSerieFondo;
			paramsRegIn.PlazoResidual = 0L;
			paramsRegIn.TipoCartera = "0";
			return paramsRegIn;
		})
		.collect(Collectors.toList());
		EntregarPreciosTasas.EntregarPreciosTasas_Salida salidaPrecioTasa = precioTasa.logica(con, paramsPrecioTasa);
		Map<Long,Object> precios = salidaPrecioTasa.P_OUT_PRECIOSTASAS.parallelStream().filter(precio -> precio.IdInstrumento != null).collect(Collectors.toMap(instrumento->instrumento.IdRegistro, precio-> precio));
		logger.info("FIN::ActualizarCarterasFondosMutuos.obtenerPrecios");
		return precios;
	}
	
	/**
	 * Retorna el id del pais
	 * @param con Conexion a base de datos
	 * @param idEmpresa Id de la empresa
	 * @return Id del pais
	 * @throws Exception 
	 */
	private Long obtenerPaisSistema(Connection con, Long idEmpresa) throws Exception {
		logger.info("INICIO::ActualizarCarterasFondosMutuos.obtenerPaisSistema");
		Long paisSistema = ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "PLATAFORMA", "PLA.PaisSistema");
		logger.info("FIN::ActualizarCarterasFondosMutuos.obtenerPaisSistema");
		return paisSistema;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getPRESURAConnection()) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();			
		String jsonRF = "{\"p_IdEmpresa\":1,\"p_Series\":[{\"IdInterno\":4,\"Fecha\":\"2020-03-03\",\"IdSerieFondo\":\"00313CUSD\",\"CalculoPorMontoCuota\":\"M\",\"Monto\":120000,\"NumeroCuotas\":0,\"IdMonedaFondo\":\"USD\",\"IdMonedaInversion\":\"CLP\",\"TipoSolicitud\":\"APO\",\"TipoFactorAjuste\":\"UF\",\"IdEmpresa\":1,\"Porcentaje\":null}]}";
		EntradaCalcularMontoNumeroCuota Objin = gson.fromJson(jsonRF, EntradaCalcularMontoNumeroCuota.class);
		CalcularMontoNumeroCuota  demo=new CalcularMontoNumeroCuota();				
		try {
			SalidaCalcularMontoNumeroCuota ObjOut=demo.logica(con, Objin);
			//con.rollback();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}
	

}
