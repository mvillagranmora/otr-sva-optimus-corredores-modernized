package cl.bcs.plataforma.tests;


import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearPersonaNombreFantasia;
import cl.bcs.utils.SQL;

import static org.junit.jupiter.api.Assertions.*;

public class TestCrearPersonaNombreFantasia {
	
	String SELECT_ID_PERSONA = "SELECT TOP 1 \"IdPersona\" FROM BSCL.\"BSCL.SocioNegocio.BD::PER.Persona\" ORDER BY \"IdPersona\" DESC";

    public static class IdPersona {
        public Long IdPersona;
    }
	
	@Test
	   void testCrearPersonaNombreFantasia_CasoBasico() throws Exception {
	       try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
	            // Crear la instancia del servicio
	           CrearPersonaNombreFantasia crearPersonaNombreFantasia = new CrearPersonaNombreFantasia();
	           SQL sql = new SQL();
	            
	           List<IdPersona> listaPersonas = sql.query(conn, SELECT_ID_PERSONA, new HashMap<String,Object>(), IdPersona.class);
	            
	           long IdPersonaObtenido = listaPersonas.get(0).IdPersona;
	            
	            // Crear la entrada con datos de prueba
	           CrearPersonaNombreFantasia.EntradaCrearPersonaNombreFantasia entrada = new CrearPersonaNombreFantasia.EntradaCrearPersonaNombreFantasia();
	           entrada.p_IdEmpresa = 1L;
	           entrada.p_IdUsuario = 0L;
	           entrada.p_IdModulo = "PER";
	           entrada.p_UsaReglasNegocio = "S";
	           entrada.p_IdPersona = IdPersonaObtenido;
	           entrada.p_NombreFantasia = "MiNuevoNombreFantasia";
	           entrada.p_IdEstado = "ACT";
	            
	            // Llamar a la lógica del servicio para crear el nombre de fantasía
	           CrearPersonaNombreFantasia.SalidaCrearPersonaNombreFantasia salida = crearPersonaNombreFantasia.logica(conn, entrada);

	            // Validar la salida
	           assertNotNull(salida, "La salida no debería ser nula");
	            
	           assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deberían existir errores en la modificación");
	            
	            // Validar que el ID de referencia sea válido
	           assertNotNull(salida.P_OUT_REFID, "El ID de referencia no debería ser nulo");
	           assertTrue(salida.P_OUT_REFID > 0, "El ID de referencia debería ser mayor a 0");
	           conn.rollback();
	        }
	    }
	    
}

