package cl.bcs.plataforma.tests;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import cl.bcs.plataforma.pla.ObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.InObtenerDiaSiguiente;
import cl.bcs.plataforma.pla.ObtenerDiaSiguiente.OutObtenerDiaSiguiente;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;

public class TestObtenerDiaSiguiente {
	
	@Test
    void testLogicaBasico() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Instancia del servicio
            ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();

            // Entrada
            InObtenerDiaSiguiente entrada = new InObtenerDiaSiguiente();
            entrada.p_idPais = 44L;
            entrada.p_diaHabil = "S"; // Solo días hábiles
            entrada.p_soloCaeDiaHabil = "N";
            InObtenerDiaSiguiente.FechasIn fecha = new InObtenerDiaSiguiente.FechasIn("2024-12-25", 3L, 44L);
            List<InObtenerDiaSiguiente.FechasIn> fechasIn = new ArrayList<InObtenerDiaSiguiente.FechasIn>();
    		fechasIn.add(fecha);
            entrada.p_fechas = fechasIn;

            // Ejecutar lógica con la conexión real
            OutObtenerDiaSiguiente salida = obtenerDiaSiguiente.logica(conn, entrada);

            // Validar resultados
            assertNotNull(salida, "La salida no debe ser nula");
            assertEquals(1, salida.p_out_fechas.size(), "Debe calcular una fecha de salida");
            assertEquals("2024-12-30", salida.p_out_fechas.get(0).fechaCalculada,
                    "La fecha calculada debería ser 2024-12-30 considerando feriados y fines de semana");
        }
    }
	
	@Test
	void testLogicaConVariosDiasHabiles() throws Exception {
	    try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
	        // Instancia del servicio
	        ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();

	        // Entrada
	        InObtenerDiaSiguiente entrada = new InObtenerDiaSiguiente();
	        entrada.p_idPais = 44L;
	        entrada.p_diaHabil = "S"; // Solo días hábiles
	        entrada.p_soloCaeDiaHabil = "N";
	        InObtenerDiaSiguiente.FechasIn fecha = new InObtenerDiaSiguiente.FechasIn("2024-12-15", 10L, 44L);
	        List<InObtenerDiaSiguiente.FechasIn> fechasIn = new ArrayList<>();
	        fechasIn.add(fecha);
	        entrada.p_fechas = fechasIn;

	        // Ejecutar lógica con la conexión real
	        OutObtenerDiaSiguiente salida = obtenerDiaSiguiente.logica(conn, entrada);

	        // Validar resultados
	        assertNotNull(salida, "La salida no debe ser nula");
	        assertEquals(1, salida.p_out_fechas.size(), "Debe calcular una fecha de salida");
	        assertEquals("2024-12-30", salida.p_out_fechas.get(0).fechaCalculada,
	                "La fecha calculada debería ser 2024-12-30 considerando feriados y fines de semana");
	    }
	}
	
	@Test
	void testLogicaConDiasInhabiles() throws Exception {
	    try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
	        // Instancia del servicio
	        ObtenerDiaSiguiente obtenerDiaSiguiente = new ObtenerDiaSiguiente();

	        // Entrada
	        InObtenerDiaSiguiente entrada = new InObtenerDiaSiguiente();
	        entrada.p_idPais = 44L;
	        entrada.p_diaHabil = "N"; // Incluir días inhábiles
	        entrada.p_soloCaeDiaHabil = "N";
	        InObtenerDiaSiguiente.FechasIn fecha = new InObtenerDiaSiguiente.FechasIn("2024-12-25", 5L, 44L);
	        List<InObtenerDiaSiguiente.FechasIn> fechasIn = new ArrayList<>();
	        fechasIn.add(fecha);
	        entrada.p_fechas = fechasIn;

	        // Ejecutar lógica con la conexión real
	        OutObtenerDiaSiguiente salida = obtenerDiaSiguiente.logica(conn, entrada);

	        // Validar resultados
	        assertNotNull(salida, "La salida no debe ser nula");
	        assertEquals(1, salida.p_out_fechas.size(), "Debe calcular una fecha de salida");
	        assertEquals("2024-12-30", salida.p_out_fechas.get(0).fechaCalculada,
	                "La fecha calculada debería ser 2024-12-30 incluyendo días inhábiles");
	    }
	}
	
}
