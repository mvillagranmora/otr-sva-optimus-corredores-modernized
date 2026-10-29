package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.ObtenerNemoSVS;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.Instrumento_Entrada;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.ObtenerNemoSVS_Entrada;
import cl.bcs.plataforma.pla.ObtenerNemoSVS.ObtenerNemoSVS_Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

class TestObtenerNemoSVS {
    
    @Test
    void testPDBCCLP() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		ObtenerNemoSVS demo = new ObtenerNemoSVS();
			
    		ObtenerNemoSVS_Entrada entrada = new ObtenerNemoSVS_Entrada();
    		Instrumento_Entrada insEntrada = new Instrumento_Entrada();
    		insEntrada.IdInterno = 0L;
    		insEntrada.IdEmisor = "CENTRAL";
    		insEntrada.FechaVencimiento = "2024-08-01";
    		insEntrada.Plazo = 0L;
    		insEntrada.Nemotecnico = "PDBC";
    		insEntrada.IdTipoReajuste = "CLP";
    		insEntrada.PagaderoEn = "CLP";
    		
			entrada.p_Instrumentos.add(insEntrada);
			
			
			ObtenerNemoSVS_Salida salida = demo.logica(conn, entrada);
					
			assertEquals("BNPDBC010824", salida.p_out_Instrumento.get(0).NemoSVS);
    		
    	}
    }
    
    @Test
    void testPagareCLP() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		ObtenerNemoSVS demo = new ObtenerNemoSVS();
			
    		ObtenerNemoSVS_Entrada entrada = new ObtenerNemoSVS_Entrada();
    		Instrumento_Entrada insEntrada = new Instrumento_Entrada();
    		insEntrada.IdInterno = 0L;
    		insEntrada.IdEmisor = "CHILE";
    		insEntrada.FechaVencimiento = "2024-05-08";
    		insEntrada.Plazo = 0L;
    		insEntrada.Nemotecnico = "PAGARE NR";
    		insEntrada.IdTipoReajuste = "CLP";
    		insEntrada.PagaderoEn = "CLP";
    		
			entrada.p_Instrumentos.add(insEntrada);
			
			
			ObtenerNemoSVS_Salida salida = demo.logica(conn, entrada);
			assertEquals("FNCHI-080524", salida.p_out_Instrumento.get(0).NemoSVS);
    		
    	}
    }
    
    @Test
    void testPagareUSD() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		ObtenerNemoSVS demo = new ObtenerNemoSVS();
			
    		ObtenerNemoSVS_Entrada entrada = new ObtenerNemoSVS_Entrada();
    		Instrumento_Entrada insEntrada = new Instrumento_Entrada();
    		insEntrada.IdInterno = 0L;
    		insEntrada.IdEmisor = "BICE";
    		insEntrada.FechaVencimiento = "2024-02-06";
    		insEntrada.Plazo = 0L;
    		insEntrada.Nemotecnico = "PAGARE R";
    		insEntrada.IdTipoReajuste = "USD";
    		insEntrada.PagaderoEn = "USD";
    		
			entrada.p_Instrumentos.add(insEntrada);
				
			ObtenerNemoSVS_Salida salida = demo.logica(conn, entrada);
			assertEquals("F*BIC-060224", salida.p_out_Instrumento.get(0).NemoSVS);
    		
    	}
    }
    
    @Test
    void testPagareEUR() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		ObtenerNemoSVS demo = new ObtenerNemoSVS();
			
    		ObtenerNemoSVS_Entrada entrada = new ObtenerNemoSVS_Entrada();
    		Instrumento_Entrada insEntrada = new Instrumento_Entrada();
    		insEntrada.IdInterno = 0L;
    		insEntrada.IdEmisor = "BICE";
    		insEntrada.FechaVencimiento = "2024-05-08";
    		insEntrada.Plazo = 0L;
    		insEntrada.Nemotecnico = "PAGARE R";
    		insEntrada.IdTipoReajuste = "EUR";
    		insEntrada.PagaderoEn = "EUR";
    		
			entrada.p_Instrumentos.add(insEntrada);
			
			
			ObtenerNemoSVS_Salida salida = demo.logica(conn, entrada);
			assertEquals("FRBIC-080524", salida.p_out_Instrumento.get(0).NemoSVS);
    		
    	}
    }
}