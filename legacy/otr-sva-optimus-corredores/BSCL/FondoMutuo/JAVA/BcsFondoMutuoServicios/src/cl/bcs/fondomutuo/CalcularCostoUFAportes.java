package cl.bcs.fondomutuo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.pojo.ConstantesFondoMutuo;
import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoSalida;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.Utils;

public class CalcularCostoUFAportes implements JavaCall {
	
	static final Logger logger = LogManager.getLogger(CalcularCostoUFAportes.class);
	private final ConstantesFondoMutuo _c = new ConstantesFondoMutuo();
	
	public static class EntradaCalcularCostoUFAportes{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		
		public List<CalculoCostoUF> p_Calculos;
		
		public EntradaCalcularCostoUFAportes() {
			this.p_Calculos = new ArrayList<CalculoCostoUF>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaCalcularCostoUFAportes{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		public List <ResultadoCalculoCostoUF> P_OUT_RESULTADO;
		
		public SalidaCalcularCostoUFAportes() {
			this.P_OUT_ERRORESCONTADOR = 0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
			this.P_OUT_RESULTADO = new ArrayList<ResultadoCalculoCostoUF>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CalculoCostoUF{
		public Long IdInterno;
		public BigDecimal Monto;
		public String IdMoneda;
		public String Fecha;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ResultadoCalculoCostoUF{
		public Long IdInterno;
		public BigDecimal CostoUF;
		public BigDecimal ValorUF;
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaCalcularCostoUFAportes jin = gson.fromJson(in.toString(), EntradaCalcularCostoUFAportes.class);
		SalidaCalcularCostoUFAportes out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	public SalidaCalcularCostoUFAportes logica (Connection con, EntradaCalcularCostoUFAportes entrada) throws Exception {
		logger.info("INICIO::CalcularCostoUFAportes");
		SalidaCalcularCostoUFAportes out = new SalidaCalcularCostoUFAportes();
		
		String idMoneda = ConfiguracionSistema.getParametroConfiguracionString(con, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.MonedaSistema");
		
		CalcularParidad calcularParidad = new CalcularParidad();
		CalcularParidadEntrada calcularParidadEntrada = new CalcularParidadEntrada();
		calcularParidadEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
		calcularParidadEntrada.p_IdTrabajo = 1L;
		
		for(CalculoCostoUF calculo : entrada.p_Calculos) {
			CalculoEntrada calculoEntrada = new CalculoEntrada();
			calculoEntrada.IdTrabajo = 1L;
			calculoEntrada.IdInterno = calculo.IdInterno;
			calculoEntrada.Fecha = calculo.Fecha;
			calculoEntrada.IdMonedaOrigen = calculo.IdMoneda;
			calculoEntrada.IdMonedaDestino = _c.UF;
			calculoEntrada.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntrada.MontoOrigen =  calculo.Monto;
			calculoEntrada.IdUso = "";
			calcularParidadEntrada.p_CalculosEntrada.add(calculoEntrada);
			CalculoEntrada calculoEntradaUF = new CalculoEntrada();
			calculoEntradaUF.IdTrabajo = 1L;
			calculoEntradaUF.IdInterno = calculo.IdInterno * -1;
			calculoEntradaUF.Fecha = calculo.Fecha;
			calculoEntradaUF.IdMonedaOrigen = _c.UF;
			calculoEntradaUF.IdMonedaDestino = idMoneda;
			calculoEntradaUF.ValorCambioLocalModificado = BigDecimal.ZERO;
			calculoEntradaUF.MontoOrigen = BigDecimal.ONE;
			calculoEntradaUF.IdUso = "";
			calcularParidadEntrada.p_CalculosEntrada.add(calculoEntradaUF);
		}
		
		CalcularParidadSalida calculoSalida = calcularParidad.logica(con, calcularParidadEntrada);
		HashMap<Long,Object> hCalculo = Utils.List2Hash(calculoSalida.p_out_CalculosSalida, "IdInterno");
		
		out.P_OUT_RESULTADO = entrada.p_Calculos.stream().map(calculo -> {
			ResultadoCalculoCostoUF resultadoCalculoCostoUF = new ResultadoCalculoCostoUF();
			CalculoSalida costoUF = (CalculoSalida) hCalculo.get(calculo.IdInterno);
			CalculoSalida valorUF = (CalculoSalida) hCalculo.get(calculo.IdInterno * -1);
			resultadoCalculoCostoUF.IdInterno = calculo.IdInterno;
			resultadoCalculoCostoUF.CostoUF = costoUF != null && costoUF.MontoDestino != null ? costoUF.MontoDestino : BigDecimal.ZERO;
			resultadoCalculoCostoUF.ValorUF = valorUF != null && valorUF.MontoDestino != null ? valorUF.MontoDestino : BigDecimal.ZERO;
			return resultadoCalculoCostoUF;
		}).collect(Collectors.toList());
		
		logger.info("FIN::CalcularCostoUFAportes");
		
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		try (Connection con = PlataformaUtils.getDESAConnection()) {
			CalcularCostoUFAportes demo = new CalcularCostoUFAportes();
			EntradaCalcularCostoUFAportes entrada = new EntradaCalcularCostoUFAportes();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 21L;
			entrada.p_IdModulo = "FMM";
			CalculoCostoUF calculo = new CalculoCostoUF();
			calculo.IdInterno = 1L;
			calculo.Fecha = "2018-10-26";
			calculo.IdMoneda = "CLP";
			calculo.Monto = new BigDecimal("45000");
			entrada.p_Calculos.add(calculo);
			System.out.println(demo.logica(con, entrada));
		}catch(Exception er) {
			er.printStackTrace();
		}
	}
	
}
