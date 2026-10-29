package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class CalcularSpreadRescate implements JavaCall{
	
	private final Log log = new Log("FM.CalcularSpreadRescate");
	
	public static class EntradaCalcularSpreadRescate{
		
		public Long p_IdEmpresa;
		
		public List<InDatos> p_Datos;
		
		public EntradaCalcularSpreadRescate(){
			this.p_Datos = new ArrayList<InDatos>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class InDatos{
		public Long NumeroCalculoSpreadRescate;
		public String Fecha;
		public BigDecimal ValorFactorReajuste;
		public BigDecimal CostoFactorReajusteRescate;
		public BigDecimal CantidadRescate;
		public BigDecimal ValorCuota;
		public String IdMonedaFondo;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosProceso{
		public Long NumeroCalculoSpreadRescate;
		public String Fecha;
		public BigDecimal ValorFactorReajuste;
		public BigDecimal CostoFactorReajusteRescate;
		public BigDecimal CantidadRescate;
		public BigDecimal ValorCuota;
		public String IdMonedaFondo;
		public BigDecimal MontoReajuste;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCalcularSpreadRescate{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoCalcularSpreadRescate> P_OUT_RESULTADO;
		
		public SalidaCalcularSpreadRescate(){
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoCalcularSpreadRescate>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoCalcularSpreadRescate{
		
		public Long NumeroCalculoSpreadRescate;
		public BigDecimal ValorFactorReajuste;
		public BigDecimal CostoFactorReajusteRescate;
		public BigDecimal CantidadRescate;
		public BigDecimal ValorCuota;
		public BigDecimal Spread;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCalcularSpreadRescate jin = gson.fromJson(in.toString(), EntradaCalcularSpreadRescate.class);
		SalidaCalcularSpreadRescate out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCalcularSpreadRescate logica(Connection con, EntradaCalcularSpreadRescate entrada) throws Exception{
		log.write("INICIO SalidaCalcularSpreadRescate");
		SalidaCalcularSpreadRescate out = new SalidaCalcularSpreadRescate();
		List<DatosProceso> datosProceso = new ArrayList<DatosProceso>();
		String monedaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		for(InDatos dato : entrada.p_Datos){
			boolean error = false;
			if(dato.ValorFactorReajuste.compareTo(BigDecimal.ZERO) != 1){
				error = true;
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"VALOR_FACTOR_MAYOR_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			}
			if(dato.CostoFactorReajusteRescate.compareTo(BigDecimal.ZERO) != 1){
				error = true;
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"COSTO_FACTOR_MAYOR_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			}
			if(dato.CantidadRescate.compareTo(BigDecimal.ZERO) != 1){
				error = true;
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CANTIDAD_RESCATE_MAYOR_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			}
			if(dato.ValorCuota.compareTo(BigDecimal.ZERO) != 1){
				error = true;
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"VALOR_CUOTA_MAYOR_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			}
			if(!error){
				DatosProceso datoProceso = new DatosProceso();
				datoProceso = Utils.copyPojo(dato, datoProceso);
				datoProceso.MontoReajuste = datoProceso.ValorFactorReajuste.multiply(datoProceso.CostoFactorReajusteRescate, MathContext.DECIMAL128).setScale(8, RoundingMode.HALF_DOWN);
				datosProceso.add(datoProceso);
			}
		}
		
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 1L;
		for(DatosProceso dato : datosProceso){
			if(!monedaSistema.equalsIgnoreCase(dato.IdMonedaFondo)){
				CalculoEntrada calculoEntrada = new CalculoEntrada();
				calculoEntrada.IdTrabajo = 1L;
				calculoEntrada.IdInterno = dato.NumeroCalculoSpreadRescate;
				calculoEntrada.Fecha = dato.Fecha;
				calculoEntrada.IdMonedaOrigen = monedaSistema;
				calculoEntrada.IdMonedaDestino = dato.IdMonedaFondo;
				calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
				calculoEntrada.MontoOrigen =  dato.MontoReajuste;
				calculoEntrada.IdUso = "";
				calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
			}
		}
		
		CalcularParidadSalida calculoSalida = new CalcularParidadSalida();
		if(calcularParidadEntrada.p_CalculosEntrada.size() > 0 ){
			calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
		}
		
		HashMap<Long,Object> hcalculos = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
		
		//PREPARAMOS SALIDA
		for(DatosProceso dato : datosProceso){
			ResultadoCalcularSpreadRescate res = new ResultadoCalcularSpreadRescate();
			res.NumeroCalculoSpreadRescate = dato.NumeroCalculoSpreadRescate;
			res.ValorFactorReajuste = dato.ValorFactorReajuste;
			res.CostoFactorReajusteRescate = dato.CostoFactorReajusteRescate;
			res.CantidadRescate = dato.CantidadRescate;
			res.ValorCuota = dato.ValorCuota;
			if(!monedaSistema.equalsIgnoreCase(dato.IdMonedaFondo)){
				res.Spread = (dato.CantidadRescate.multiply(dato.ValorCuota, MathContext.DECIMAL128)).subtract(dato.MontoReajuste,  MathContext.DECIMAL128).setScale(8, RoundingMode.HALF_DOWN);
			}else{
				if(dato.NumeroCalculoSpreadRescate != 0) {
					CalculoSalida calculo = (CalculoSalida) hcalculos.get(dato.NumeroCalculoSpreadRescate);
					res.Spread = (dato.CantidadRescate.multiply(dato.ValorCuota, MathContext.DECIMAL128)).subtract(calculo.MontoDestino,  MathContext.DECIMAL128).setScale(8, RoundingMode.HALF_DOWN);	
				}else {
					res.Spread = BigDecimal.ZERO ;
				}
				
			}
			
			out.P_OUT_RESULTADO.add(res);
		}
		
		log.write("FIN SalidaCalcularSpreadRescate");
		return out;
	}
	
	
}
