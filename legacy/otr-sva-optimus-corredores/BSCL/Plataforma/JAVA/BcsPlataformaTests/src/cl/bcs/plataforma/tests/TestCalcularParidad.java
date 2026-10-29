package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;



public class TestCalcularParidad {
	
	public static String SELECT_PARAMETRO_FINANCIERO = "SELECT TOP 1 * FROM \"BSCL.Plataforma.BD::MER.ParametroFinanciero\" ORDER BY \"Fecha\" DESC";
	
	public static class ParametroFinanciero{
		public String IdParametroFinanciero;
		public Date Fecha;
		public String AgnoMes;
		public String Agno;
		public BigDecimal Valor;
		public Timestamp FechaActualizacion;
		
	}
	
	
	@Test
    void TestCLPToMoneda() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		CalcularParidad demo = new CalcularParidad();
			
    		SQL sql = new SQL();
    		List<ParametroFinanciero> Parametro = sql.query(conn, SELECT_PARAMETRO_FINANCIERO, new HashMap<String,Object>(), ParametroFinanciero.class);

    		String MonedaObtenida = Parametro.get(0).IdParametroFinanciero;
    		BigDecimal ValorObtenido = Parametro.get(0).Valor;
    		Date Fecha = Parametro.get(0).Fecha;
    		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    		String FechaString = formatter.format(Fecha);
    		
    		
			CalcularParidadEntrada entrada = new CalcularParidadEntrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdTrabajo = 0L;
			
			BigDecimal ValorInicialCLP = new BigDecimal("10000");
			
			BigDecimal resultEsperado = ValorInicialCLP.divide(ValorObtenido, 2, RoundingMode.HALF_UP);
			
			CalculoEntrada arg0 = new CalculoEntrada();
			arg0.Fecha = FechaString;
			arg0.IdInterno = 0L;
			arg0.IdMonedaOrigen = "CLP";
			arg0.IdMonedaDestino = MonedaObtenida;
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "";
			arg0.MontoOrigen = ValorInicialCLP;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);
						 
			
    		CalcularParidadSalida asdf = demo.logica(conn, entrada);
    		BigDecimal Result = asdf.p_out_CalculosSalida.get(0).MontoLocal;
    		BigDecimal roundedResult = Result.setScale(2, RoundingMode.HALF_UP);
    		assertEquals(resultEsperado, roundedResult, "Monto correcto es x");
    	}catch(Exception e){
    		throw e;
    	}
}
	
	@Test
    void TestMonedaToCLP() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		CalcularParidad demo = new CalcularParidad();
			
    		SQL sql = new SQL();
    		List<ParametroFinanciero> Parametro = sql.query(conn, SELECT_PARAMETRO_FINANCIERO, new HashMap<String,Object>(), ParametroFinanciero.class);

    		String MonedaObtenida = Parametro.get(0).IdParametroFinanciero;
    		BigDecimal ValorObtenido = Parametro.get(0).Valor;
    		Date Fecha = Parametro.get(0).Fecha;
    		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    		String FechaString = formatter.format(Fecha);
    		
    		
			CalcularParidadEntrada entrada = new CalcularParidadEntrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdTrabajo = 0L;
			
			BigDecimal ValorInicial = new BigDecimal("10000");
			
			BigDecimal resultEsperado = ValorInicial.multiply(ValorObtenido);
			resultEsperado = resultEsperado.setScale(2, RoundingMode.HALF_UP);
			
			CalculoEntrada arg0 = new CalculoEntrada();
			arg0.Fecha = FechaString;
			arg0.IdInterno = 0L;
			arg0.IdMonedaOrigen = MonedaObtenida;
			arg0.IdMonedaDestino = "CLP";
			arg0.IdTrabajo = 0L;
			arg0.IdUso = "";
			arg0.MontoOrigen = ValorInicial;
			arg0.ValorCambioLocalModificado = BigDecimal.ZERO;
			
			entrada.p_CalculosEntrada.add(arg0);
					
    		CalcularParidadSalida asdf = demo.logica(conn, entrada);
    		BigDecimal Result = asdf.p_out_CalculosSalida.get(0).MontoLocal;
    		BigDecimal roundedResult = Result.setScale(2, RoundingMode.HALF_UP);
    		assertEquals(resultEsperado, roundedResult, "Monto correcto es x");
    	}catch(Exception e){
    		throw e;
    	}
}
}
