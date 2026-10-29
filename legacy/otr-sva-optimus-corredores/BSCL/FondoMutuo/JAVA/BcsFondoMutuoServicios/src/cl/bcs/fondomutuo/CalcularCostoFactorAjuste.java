package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Log;
import cl.bcs.utils.Utils;

public class CalcularCostoFactorAjuste implements JavaCall{
	
	private final Log log = new Log("FM.CalcularSpreadRescate");
	
	public static class EntradaCalcularCostoFactorAjuste{
		public Long p_IdEmpresa;
		List<InDatosCartera> p_DatosCartera;
		
		public EntradaCalcularCostoFactorAjuste(){
			this.p_DatosCartera = new ArrayList<InDatosCartera>();
		}
	}
	
	public static class InDatosCartera{
		
		public Long NumeroCalculoCostoFactorAjuste;
		public BigDecimal CantidadRescate;
		public BigDecimal CantidadTotalCartera;
		public BigDecimal CostoFactorAjusteTotalCartera;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatosProceso{
		public Long NumeroCalculoCostoFactorAjuste;
		public BigDecimal CantidadRescate;
		public BigDecimal CantidadTotalCartera;
		public BigDecimal CostoFactorAjusteTotalCartera;
		public BigDecimal CostoFactorAjusteRescate;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCalcularCostoFactorAjuste{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoCalcularCostoFactorAjuste> P_OUT_RESULTADO;
		
		public SalidaCalcularCostoFactorAjuste(){
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoCalcularCostoFactorAjuste>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoCalcularCostoFactorAjuste{
		
		public Long NumeroCalculoCostoFactorAjuste;
		public BigDecimal CantidadRescate;
		public BigDecimal CantidadTotalCartera;
		public BigDecimal CostoFactorAjusteTotalCartera;
		public BigDecimal CostoFactorAjusteRescate;
		public BigDecimal CostoFactorAjusteRestante;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCalcularCostoFactorAjuste jin = gson.fromJson(in.toString(), EntradaCalcularCostoFactorAjuste.class);
		SalidaCalcularCostoFactorAjuste out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCalcularCostoFactorAjuste logica(Connection con, EntradaCalcularCostoFactorAjuste entrada) throws Exception{
		log.write("INICIO CalcularCostoFactorAjuste");
		SalidaCalcularCostoFactorAjuste out = new SalidaCalcularCostoFactorAjuste();
		List<DatosProceso> datosProceso = new ArrayList<DatosProceso>();
		for(InDatosCartera cartera : entrada.p_DatosCartera){
			boolean error = false;
			if(cartera.CantidadRescate.compareTo(BigDecimal.ZERO) != 1){
				error = true;
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CANTIDAD_RESCATE_MAYOR_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			}
			if(cartera.CantidadTotalCartera.compareTo(BigDecimal.ZERO) != 1){
				error = true;
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"CANTIDAD_TOTAL_CARTERA_MAYOR_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			}
			if(cartera.CostoFactorAjusteTotalCartera.compareTo(BigDecimal.ZERO) != 1){
				error = true;
				out.P_OUT_ERRORESCONTADOR++;
				out.P_OUT_MENSAJES.add(new MensajeSalida(0L,MensajeSalida.MENSAJE_ERROR,"COSTO_FACTOR_REAJUSTE_MAYOR_CERO",new HashMap<String,String>(),con,entrada.p_IdEmpresa));
			}
			
			if(!error){
				DatosProceso datoProceso = new DatosProceso();
				datoProceso = Utils.copyPojo(cartera, datoProceso);
				datoProceso.CostoFactorAjusteRescate = (datoProceso.CantidadRescate.multiply(datoProceso.CostoFactorAjusteTotalCartera, MathContext.DECIMAL128)).divide(datoProceso.CantidadTotalCartera, MathContext.DECIMAL128);
				datosProceso.add(datoProceso);
			}
		}
		
		for(DatosProceso dato : datosProceso){
			ResultadoCalcularCostoFactorAjuste res = new ResultadoCalcularCostoFactorAjuste();
			res = Utils.copyPojo(dato, res);
			res.CostoFactorAjusteRestante = dato.CostoFactorAjusteTotalCartera.subtract(dato.CostoFactorAjusteRescate, MathContext.DECIMAL128);
			out.P_OUT_RESULTADO.add(res);
		}
		
		log.write("FIN CalcularCostoFactorAjuste");
		return out;
	}
}
