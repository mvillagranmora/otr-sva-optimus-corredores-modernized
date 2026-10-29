package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.mer.CalcularParidad;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadEntrada;
import cl.bcs.plataforma.mer.CalcularParidad.CalcularParidadSalida;
import cl.bcs.plataforma.mer.CalcularParidad.CalculoEntrada;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

class TestPlataforma {

    @Test
    void myFirstTest() {
        assertEquals(2, 1 + 1);
    }
    
    @Test
    void test1() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		CalcularParidad demo = new CalcularParidad();
			
			CalcularParidadEntrada entrada = new CalcularParidadEntrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdTrabajo = 0L;
			
			
			CalculoEntrada arg0 = new CalculoEntrada();
			arg0.Fecha = "2018-07-22";
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
			
			entrada.p_CalculosEntrada.add(arg0);
    		
    		CalcularParidadSalida asdf = demo.logica(conn, entrada);
    		assertEquals(new BigDecimal("10000"), asdf.p_out_CalculosSalida.get(0).MontoLocal);
    	}
    }
    
    

}