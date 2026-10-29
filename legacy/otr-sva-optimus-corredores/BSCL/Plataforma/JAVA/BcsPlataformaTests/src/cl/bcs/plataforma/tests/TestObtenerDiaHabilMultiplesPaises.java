package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.ObtenerDiaHabilMultiplesPaises;
import cl.bcs.plataforma.pla.ObtenerDiaHabilMultiplesPaises.EntradaObtenerDiaHabilMultiplesPaises;
import cl.bcs.plataforma.pla.ObtenerDiaHabilMultiplesPaises.SalidaObtenerDiaHabilMultiplesPaises;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

public class TestObtenerDiaHabilMultiplesPaises {
	
	@Test
	void testLogicaConUnPaisYDiasHabiles() throws Exception {
	    try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
	        // Instancia del servicio
	        ObtenerDiaHabilMultiplesPaises obtenerDiaHabilMultiplesPaises = new ObtenerDiaHabilMultiplesPaises();

	        // Entrada
	        EntradaObtenerDiaHabilMultiplesPaises entrada = new EntradaObtenerDiaHabilMultiplesPaises();
	        entrada.p_diaHabil = "S"; // Solo días hábiles
	        entrada.p_soloCaeDiaHabil = "N";
	        List<Long> paises = Arrays.asList(44L);
	        EntradaObtenerDiaHabilMultiplesPaises.FechasIn fecha = new EntradaObtenerDiaHabilMultiplesPaises.FechasIn(1L, "2024-12-20", 5L, paises);
	        entrada.p_fechas = Arrays.asList(fecha);

	        SalidaObtenerDiaHabilMultiplesPaises salida = obtenerDiaHabilMultiplesPaises.logica(conn, entrada);

	        // Validar resultados
	        assertNotNull(salida, "La salida no debe ser nula");
	        assertEquals(1, salida.p_out_fechas.size(), "Debe calcular una fecha de salida");
	        assertEquals("2024-12-20", salida.p_out_fechas.get(0).fechaInicial, "La fecha inicial debe coincidir con la entrada");
	        assertEquals("2024-12-30", salida.p_out_fechas.get(0).fechaCalculada, 
	            "La fecha calculada debe ser 2024-12-30 considerando días hábiles y feriados");
	    }
	}
	
	@Test
	void testLogicaConMultiplesPaisesYDiasInhabiles() throws Exception {
	    try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
	        // Instancia del servicio
	        ObtenerDiaHabilMultiplesPaises obtenerDiaHabilMultiplesPaises = new ObtenerDiaHabilMultiplesPaises();

	        // Entrada
	        EntradaObtenerDiaHabilMultiplesPaises entrada = new EntradaObtenerDiaHabilMultiplesPaises();
	        entrada.p_diaHabil = "N"; // Incluir días inhábiles
	        entrada.p_soloCaeDiaHabil = "N";
	        List<Long> paises = Arrays.asList(44L, 55L);
	        EntradaObtenerDiaHabilMultiplesPaises.FechasIn fecha = new EntradaObtenerDiaHabilMultiplesPaises.FechasIn(1L, "2024-12-20", 7L, paises);
	        entrada.p_fechas = Arrays.asList(fecha);

	        SalidaObtenerDiaHabilMultiplesPaises salida = obtenerDiaHabilMultiplesPaises.logica(conn, entrada);

	        // Validar resultados
	        assertNotNull(salida, "La salida no debe ser nula");
	        assertEquals(1, salida.p_out_fechas.size(), "Debe calcular una fecha de salida");
	        assertEquals("2024-12-20", salida.p_out_fechas.get(0).fechaInicial, "La fecha inicial debe coincidir con la entrada");
	        assertEquals("2024-12-27", salida.p_out_fechas.get(0).fechaCalculada, 
	            "La fecha calculada debe incluir días inhábiles considerando feriados en múltiples países");
	    }
	}
	
	@Test
	void testLogicaSinSumarDias() throws Exception {
	    try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
	        // Instancia del servicio
	        ObtenerDiaHabilMultiplesPaises obtenerDiaHabilMultiplesPaises = new ObtenerDiaHabilMultiplesPaises();

	        // Entrada
	        EntradaObtenerDiaHabilMultiplesPaises entrada = new EntradaObtenerDiaHabilMultiplesPaises();
	        entrada.p_diaHabil = "S"; // Solo días hábiles
	        entrada.p_soloCaeDiaHabil = "S";
	        List<Long> paises = Arrays.asList(44L);
	        EntradaObtenerDiaHabilMultiplesPaises.FechasIn fecha = new EntradaObtenerDiaHabilMultiplesPaises.FechasIn(1L, "2024-12-20", 0L, paises);
	        entrada.p_fechas = Arrays.asList(fecha);

	        SalidaObtenerDiaHabilMultiplesPaises salida = obtenerDiaHabilMultiplesPaises.logica(conn, entrada);

	        // Validar resultados
	        assertNotNull(salida, "La salida no debe ser nula");
	        assertEquals(1, salida.p_out_fechas.size(), "Debe calcular una fecha de salida");
	        assertEquals("2024-12-20", salida.p_out_fechas.get(0).fechaInicial, "La fecha inicial debe coincidir con la entrada");
	        assertEquals("2024-12-20", salida.p_out_fechas.get(0).fechaCalculada, 
	            "La fecha calculada debe ser igual a la inicial si no se suman días");
	    }
	}
	
}
