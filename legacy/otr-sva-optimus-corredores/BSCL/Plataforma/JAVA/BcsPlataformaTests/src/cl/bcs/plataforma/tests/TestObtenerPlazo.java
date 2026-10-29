package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.ObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.InObtenerPlazo;
import cl.bcs.plataforma.pla.ObtenerPlazo.FechasIn;
import cl.bcs.plataforma.pla.ObtenerPlazo.OutObtenerPlazo;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

class TestObtenerPlazo {
    
    @Test
    void testPlazo() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		ObtenerPlazo demo = new ObtenerPlazo();
			
    		InObtenerPlazo entrada = new InObtenerPlazo();
    		
    		List<FechasIn> fechas = new ArrayList<FechasIn>();
    		FechasIn fechaEntrada = new FechasIn();
    		fechaEntrada.IdInterno = 0L;
    		fechaEntrada.FechaDesde = "2024-08-01";
    		fechaEntrada.FechaHasta = "2024-08-07";
    		fechaEntrada.SoloDiasHabiles = "N";
    		fechas.add(fechaEntrada);
    		
			entrada.p_Fechas = fechas;
			entrada.p_IdPais = 44L;
			entrada.p_EnTablaTemporal = "N";
			
			OutObtenerPlazo salida = demo.logica(conn, entrada);
					
			assertEquals(6L, salida.p_out_Fechas.get(0).Plazo);
    		
    	}
    }
    
    @Test
    void testPlazoHabiles() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		ObtenerPlazo demo = new ObtenerPlazo();
			
    		InObtenerPlazo entrada = new InObtenerPlazo();
    		
    		List<FechasIn> fechas = new ArrayList<FechasIn>();
    		FechasIn fechaEntrada = new FechasIn();
    		fechaEntrada.IdInterno = 0L;
    		fechaEntrada.FechaDesde = "2024-08-01";
    		fechaEntrada.FechaHasta = "2024-08-07";
    		fechaEntrada.SoloDiasHabiles = "S";
    		fechas.add(fechaEntrada);
    		
			entrada.p_Fechas = fechas;
			entrada.p_IdPais = 44L;
			entrada.p_EnTablaTemporal = "N";
			
			OutObtenerPlazo salida = demo.logica(conn, entrada);
					
			assertEquals(4L, salida.p_out_Fechas.get(0).Plazo);
    		
    	}
    }
    
    @Disabled
    @Test
    void testPlazoHabilesTemp() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		ObtenerPlazo demo = new ObtenerPlazo();
			
    		InObtenerPlazo entrada = new InObtenerPlazo();
    		
    		List<FechasIn> fechas = new ArrayList<FechasIn>();
    		FechasIn fechaEntrada = new FechasIn();
    		fechaEntrada.IdInterno = 0L;
    		fechaEntrada.FechaDesde = "2024-08-01";
    		fechaEntrada.FechaHasta = "2024-08-07";
    		fechaEntrada.SoloDiasHabiles = "S";
    		fechas.add(fechaEntrada);
    		
			entrada.p_Fechas = fechas;
			entrada.p_IdPais = 44L;
			entrada.p_EnTablaTemporal = "S";
			
			OutObtenerPlazo salida = demo.logica(conn, entrada);
					
			assertEquals(4L, salida.p_out_Fechas.get(0).Plazo);
    		
    	}
    }
    
}