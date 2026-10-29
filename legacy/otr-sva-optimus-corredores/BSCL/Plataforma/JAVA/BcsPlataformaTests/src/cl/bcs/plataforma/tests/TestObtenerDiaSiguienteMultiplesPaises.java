package cl.bcs.plataforma.tests;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.InObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.ObtenerDiaSiguienteMultiplesPaises.OutObtenerDiaSiguienteMultiplesPaises;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

import static org.junit.jupiter.api.Assertions.*;
import cl.bcs.plataforma.pla.pojo.FechasIn;

public class TestObtenerDiaSiguienteMultiplesPaises {
	
	@Test
	void testLogicaSinSpot() throws Exception {
	    try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
	        // Instancia del servicio
	        ObtenerDiaSiguienteMultiplesPaises obtenerDiaSiguienteMultiplesPaises = new ObtenerDiaSiguienteMultiplesPaises();

	        // Entrada
	        InObtenerDiaSiguienteMultiplesPaises entrada = new InObtenerDiaSiguienteMultiplesPaises();
	        entrada.p_diaHabil = "S"; // Solo días hábiles
	        entrada.p_soloCaeDiaHabil = "N";
	        entrada.p_operacionSpot = "N"; // Sin modo SPOT
	        entrada.p_paisSistema = null;
	        List<Long> paises = Arrays.asList(44L, 223L); // Países involucrados
	        FechasIn fecha = new FechasIn(1L, "2024-12-20", 5L, paises);
	        entrada.p_fechas = Arrays.asList(fecha);

	        OutObtenerDiaSiguienteMultiplesPaises salida = obtenerDiaSiguienteMultiplesPaises.logica(conn, entrada);

	        // Validar resultados
	        assertNotNull(salida, "La salida no debe ser nula");
	        assertEquals(1, salida.p_out_fechas.size(), "Debe calcular una fecha de salida");

	        // Validar datos del resultado
	        OutObtenerDiaSiguienteMultiplesPaises.FechasOut resultado = salida.p_out_fechas.get(0);
	        assertEquals("2024-12-20", resultado.fechaInicial, "La fecha inicial debe coincidir con la entrada");
	        assertEquals(5L, resultado.numeroDiasInicial, "El número de días inicial debe coincidir con la entrada");
	        assertEquals("2024-12-30", resultado.fechaCalculada, 
	            "La fecha calculada debe ser 2024-12-30 considerando días hábiles y feriados");
	    }
	}
	
	@Test
	void testLogicaConSpot() throws Exception {
	    try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
	        // Instancia del servicio
	        ObtenerDiaSiguienteMultiplesPaises obtenerDiaSiguienteMultiplesPaises = new ObtenerDiaSiguienteMultiplesPaises();

	        // Entrada
	        InObtenerDiaSiguienteMultiplesPaises entrada = new InObtenerDiaSiguienteMultiplesPaises();
	        entrada.p_diaHabil = "S"; // Solo días hábiles
	        entrada.p_soloCaeDiaHabil = "S";
	        entrada.p_operacionSpot = "S"; // Activar modo SPOT
	        entrada.p_paisSistema = 44L; // País nacional
	        List<Long> paises = Arrays.asList(44L, 223L); // Países involucrados
	        FechasIn fecha = new FechasIn(1L, "2024-12-26", 5L, paises);
	        entrada.p_fechas = Arrays.asList(fecha);


	        OutObtenerDiaSiguienteMultiplesPaises salida = obtenerDiaSiguienteMultiplesPaises.logica(conn, entrada);

	        // Validar resultados
	        assertNotNull(salida, "La salida no debe ser nula");
	        assertEquals(1, salida.p_out_fechas.size(), "Debe calcular una fecha de salida");

	        // Validar datos del resultado
	        OutObtenerDiaSiguienteMultiplesPaises.FechasOut resultado = salida.p_out_fechas.get(0);
	        assertEquals("2024-12-26", resultado.fechaInicial, "La fecha inicial debe coincidir con la entrada");
	        assertEquals(5L, resultado.numeroDiasInicial, "El número de días inicial debe coincidir con la entrada");
	        assertEquals("2025-01-02", resultado.fechaCalculada, 
	            "La fecha calculada debe ser 2025-01-02 considerando reglas SPOT para días hábiles y feriados");
	    }
	}
	
}
