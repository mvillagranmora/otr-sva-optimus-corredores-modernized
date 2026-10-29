package cl.bcs.plataforma.tests;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.ObtenerDiaAnterior;
import cl.bcs.plataforma.pla.ObtenerDiaAnterior.InObtenerDiaAnterior;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;


public class TestObtenerDiaAnterior {
	


   

    @Test
    void testLogicaBase() throws Exception {
    	System.out.println("Working Directory = " + System.getProperty("user.dir"));
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
        // Instancia del servicio
        ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();

        // Entrada
        ObtenerDiaAnterior.InObtenerDiaAnterior entrada = new ObtenerDiaAnterior.InObtenerDiaAnterior();
        entrada.idPais = 44L;
        entrada.diaHabil = "S"; // Solo días hábiles
        InObtenerDiaAnterior.FechasIn f1 = new InObtenerDiaAnterior.FechasIn("2024-12-27", 2L);
		List<InObtenerDiaAnterior.FechasIn> fechasIn = new ArrayList<InObtenerDiaAnterior.FechasIn>();
		fechasIn.add(f1);
		entrada.fechas = fechasIn;;

        ObtenerDiaAnterior.OutObtenerDiaAnterior salida = obtenerDiaAnterior.logica(conn, entrada);

        // Validar resultados
        assertEquals("2024-12-27", salida.fechas.get(0).fechaInicial, "La fecha inicial de salida no coincide con la entrada");
        assertEquals(2, salida.fechas.get(0).numeroDiasInicial, "El número de días inicial debe coincidir con la entrada");
        assertEquals(1, salida.fechas.size(), "Debe calcular una fecha de salida");
        assertEquals("2024-12-24", salida.fechas.get(0).fechaCalculada, "La fecha debe retroceder excluyendo feriados y fines de semana");
    	}catch(Exception e) {
    		throw e;
    	}
    }
    
    @Test
    void testLogicaConVariosDiasHabiles() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Instancia del servicio
            ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();

            // Entrada
            ObtenerDiaAnterior.InObtenerDiaAnterior entrada = new ObtenerDiaAnterior.InObtenerDiaAnterior();
            entrada.idPais = 44L;
            entrada.diaHabil = "S"; // Solo días hábiles
            InObtenerDiaAnterior.FechasIn f1 = new InObtenerDiaAnterior.FechasIn("2024-12-27", 5L);
            List<InObtenerDiaAnterior.FechasIn> fechasIn = new ArrayList<>();
            fechasIn.add(f1);
            entrada.fechas = fechasIn;

            ObtenerDiaAnterior.OutObtenerDiaAnterior salida = obtenerDiaAnterior.logica(conn, entrada);

            // Validar resultados
            assertEquals("2024-12-27", salida.fechas.get(0).fechaInicial, "La fecha inicial de salida no coincide con la entrada");
            assertEquals(5, salida.fechas.get(0).numeroDiasInicial, "El número de días inicial debe coincidir con la entrada");
            assertEquals(1, salida.fechas.size(), "Debe calcular una fecha de salida");
            assertEquals("2024-12-19", salida.fechas.get(0).fechaCalculada,
                    "La fecha debe retroceder excluyendo feriados y fines de semana");
        }
    }
    
    @Test
    void testLogicaConDiasInhabiles() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Instancia del servicio
            ObtenerDiaAnterior obtenerDiaAnterior = new ObtenerDiaAnterior();

            // Entrada
            ObtenerDiaAnterior.InObtenerDiaAnterior entrada = new ObtenerDiaAnterior.InObtenerDiaAnterior();
            entrada.idPais = 44L;
            entrada.diaHabil = "N"; // Incluir días inhábiles
            InObtenerDiaAnterior.FechasIn f1 = new InObtenerDiaAnterior.FechasIn("2024-12-27", 3L);
            List<InObtenerDiaAnterior.FechasIn> fechasIn = new ArrayList<>();
            fechasIn.add(f1);
            entrada.fechas = fechasIn;

            ObtenerDiaAnterior.OutObtenerDiaAnterior salida = obtenerDiaAnterior.logica(conn, entrada);

            // Validar resultados
            assertEquals("2024-12-27", salida.fechas.get(0).fechaInicial, "La fecha inicial de salida no coincide con la entrada");
            assertEquals(3, salida.fechas.get(0).numeroDiasInicial, "El número de días inicial debe coincidir con la entrada");
            assertEquals(1, salida.fechas.size(), "Debe calcular una fecha de salida");
            assertEquals("2024-12-24", salida.fechas.get(0).fechaCalculada,
                    "La fecha debe retroceder incluyendo días inhábiles");
        }
    }
}
