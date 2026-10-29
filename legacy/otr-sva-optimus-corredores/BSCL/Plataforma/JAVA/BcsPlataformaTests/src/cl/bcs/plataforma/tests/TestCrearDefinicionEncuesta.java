package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.enc.CrearDefinicionEncuesta;
import cl.bcs.plataforma.enc.CrearDefinicionEncuesta.CrearDefinicionEncuesta_Entrada;
import cl.bcs.plataforma.enc.CrearDefinicionEncuesta.CrearDefinicionEncuesta_Salida;
import cl.bcs.plataforma.enc.CrearDefinicionEncuesta.DefinicionPregunta_Entrada;
import cl.bcs.plataforma.enc.CrearDefinicionEncuesta.DefinicionRespuesta_Entrada;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

class TestCrearDefinicionEncuesta {
	@Test
    void testCrearEncuesta() throws Exception {
    	try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
    		CrearDefinicionEncuesta demo = new CrearDefinicionEncuesta();
			
    		DefinicionPregunta_Entrada pregunta = new DefinicionPregunta_Entrada();
    		DefinicionPregunta_Entrada preguntaExtra = new DefinicionPregunta_Entrada();
    		List<DefinicionPregunta_Entrada> preguntas = new ArrayList<DefinicionPregunta_Entrada>();
    		pregunta.IdPregunta = 0L;
    		pregunta.TextoPregunta = "¿En cuál de los siguientes tramos de edad se encuentra usted?";
    		preguntaExtra.IdPregunta = 1L;
    		preguntaExtra.TextoPregunta = "¿Tiene alguna experiencia como inversionista?";
    		preguntas.add(pregunta);
    		preguntas.add(preguntaExtra);
    		
    		DefinicionRespuesta_Entrada respuesta = new DefinicionRespuesta_Entrada();
    		DefinicionRespuesta_Entrada respuestaExtra = new DefinicionRespuesta_Entrada();
    		DefinicionRespuesta_Entrada respuestaAdicional = new DefinicionRespuesta_Entrada();
    		List<DefinicionRespuesta_Entrada> respuestas = new ArrayList<DefinicionRespuesta_Entrada>();
    		respuesta.IdPregunta = 0L;
    		respuesta.IdRespuesta = 0L;
    		respuesta.TextoRespuesta = "18 a 35";
    		respuesta.Puntaje = 1L;
    		respuestaAdicional.IdPregunta = 0L;
    		respuestaAdicional.IdRespuesta = 1L;
    		respuestaAdicional.TextoRespuesta = "36 a 65";
    		respuestaAdicional.Puntaje = 5L;
    		respuestaExtra.IdPregunta = 1L;
    		respuestaExtra.IdPregunta = 1L;
    		respuestaExtra.IdRespuesta = 2L;
    		respuestaExtra.TextoRespuesta = "Sí";
    		respuestaExtra.Puntaje = 5L;	
    		respuestas.add(respuesta);
    		respuestas.add(respuestaAdicional);
    		respuestas.add(respuestaExtra);
    		
    		CrearDefinicionEncuesta_Entrada encuestaEntrada = new CrearDefinicionEncuesta_Entrada();
    		encuestaEntrada.p_IdEmpresa = 1L;
    		encuestaEntrada.p_IdEstado = "ACT";
    		encuestaEntrada.p_IdModulo = "SOC";
    		encuestaEntrada.p_IdUsuario = 0L;
    		encuestaEntrada.p_Nombre= "Encuesta inversionista TEST";
    		encuestaEntrada.p_Preguntas = preguntas;
    		encuestaEntrada.p_Respuestas = respuestas;
    		encuestaEntrada.p_simularOperacion = "N";
    		encuestaEntrada.p_UsaReglasNegocio = "S";
    		
    		CrearDefinicionEncuesta_Salida salida = demo.logica(conn, encuestaEntrada);
					
			assertEquals("UI_ENCUESTA_CREADA_CORRECTAMENTE", salida.P_OUT_MENSAJES.get(0).codigo);
			
    		conn.rollback();
    	}
    }
    
}