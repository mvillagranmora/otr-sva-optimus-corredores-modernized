package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.ConvertirNumeroTexto;

import java.math.BigDecimal;

public class TestConvertirNumeroTexto {
	@Test
    void testNumeroCero() {
        assertEquals("cero", ConvertirNumeroTexto.convert(0), "El numero 0 debe convertirse a 'cero'");
    }

    @Test
    void testNumeroSimple() {
        assertEquals("uno", ConvertirNumeroTexto.convert(1).trim(),"El numero 1 debe convertirse a 'uno'");
        assertEquals("veinte", ConvertirNumeroTexto.convert(20).trim(), "El numero 20 debe convertirse a 'veinte'");
        assertEquals("cien", ConvertirNumeroTexto.convert(100).trim(), "El numero 100 debe convertirse a 'cien'");
    }

    @Test
    void testNumeroGrande() {
        assertEquals("un millon", ConvertirNumeroTexto.convert(1000000).trim(), "El numero 1,000,000 debe convertirse a 'un millon'");
        assertEquals("diez billones", ConvertirNumeroTexto.convert(10000000000L).trim(), "El numero 10,000,000,000 debe convertirse a 'diez billones'");
    }

    @Test
    void testDecimalesSinFraccion() {
        BigDecimal numero = new BigDecimal("1234.56");
        assertEquals("mil doscientos treinta y cuatro", ConvertirNumeroTexto.convert(numero).trim(), "Los decimales deben ser ignorados sin fraccion");
    }

    @Test
    void testDecimalesConFraccion() {
        BigDecimal numero = new BigDecimal("1234.56");
        String resultado = ConvertirNumeroTexto.convert(numero, true, "pesos").trim();
        assertEquals("mil doscientos treinta y cuatro con 56/100 pesos", resultado, "Los decimales deben representarse como fraccion");
    }

    @Test
    void testTextoMoneda() {
        assertEquals("cien dolares", ConvertirNumeroTexto.convert(100, "dolares").trim(), "Debe agregar el texto de la moneda al resultado");
    }

    @Test
    void testEspaciosExtras() {
        assertEquals("un millon doscientos mil", ConvertirNumeroTexto.convert(1200000).trim(), "El resultado no debe tener espacios extras");
    }
}

