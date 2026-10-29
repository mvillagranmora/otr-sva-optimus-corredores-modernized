package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.enc.CrearDefinicionPuntaje;
import cl.bcs.plataforma.enc.CrearDefinicionPuntaje.CrearDefinicionPuntaje_Entrada;
import cl.bcs.plataforma.enc.CrearDefinicionPuntaje.CrearDefinicionPuntaje_Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

class TestCrearDefinicionPuntaje {
	
    @Test
    void testCrearPuntaje() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		CrearDefinicionPuntaje demo = new CrearDefinicionPuntaje();
			
    		CrearDefinicionPuntaje_Entrada puntajeEntrada = new CrearDefinicionPuntaje_Entrada();
    		puntajeEntrada.p_IdEmpresa = 1L;
    		puntajeEntrada.p_IdUsuario = 0L;
    		puntajeEntrada.p_IdModulo = "PLA";
    		puntajeEntrada.p_simularOperacion = "N";
    		puntajeEntrada.p_UsaReglasNegocio = "S";
    		puntajeEntrada.p_PuntajeHasta = 3500L;
    		puntajeEntrada.p_IdCategoriaInversionista = "MODERADO";
    		
    		CrearDefinicionPuntaje_Salida salida = demo.logica(conn, puntajeEntrada);
					
			assertEquals("UI_PUNTAJE_CREADO_CORRECTAMENTE", salida.P_OUT_MENSAJES.get(0).codigo);
			
    		conn.rollback();
    	}
    }
    
}