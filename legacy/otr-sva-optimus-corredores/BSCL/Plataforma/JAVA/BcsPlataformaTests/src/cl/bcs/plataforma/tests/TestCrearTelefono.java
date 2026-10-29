package cl.bcs.plataforma.tests;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearTelefono;
import cl.bcs.utils.SQL;

import static org.junit.jupiter.api.Assertions.*;

public class TestCrearTelefono {
	
	public static String SELECT_ID_PERSONA = "SELECT \"IdPersona\" FROM BSCL.\"BSCL.SocioNegocio.BD::PER.Persona\" ORDER BY \"IdPersona\" DESC";
	
	public static class IdPersona {
		public long IdPersona;
	}
	
	
	@Test
    void testCrearTelefono() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Crear la instancia del servicio
            CrearTelefono crearTelefono = new CrearTelefono();
            
            SQL sql = new SQL();
    		List<IdPersona> IdPersona = sql.query(conn, SELECT_ID_PERSONA, new HashMap<String,Object>(), IdPersona.class);
            
    		long IdPersonaObtenido = IdPersona.get(0).IdPersona; 
    		
    		
            // Crear la entrada con datos de prueba
            CrearTelefono.EntradaCrearTelefono entradaCrearTelefono = new CrearTelefono.EntradaCrearTelefono();
            entradaCrearTelefono.p_IdEmpresa = 1L;
            entradaCrearTelefono.p_IdUsuario = 0L;
            entradaCrearTelefono.p_IdModulo = "PER";
            entradaCrearTelefono.p_UsaReglasNegocio = "S";
            entradaCrearTelefono.p_IdPais = 44L;
            entradaCrearTelefono.p_IdTipoTelefono = "MOVIL";
            entradaCrearTelefono.p_Numero = "987654321";
            entradaCrearTelefono.p_Anexo = "123";
            entradaCrearTelefono.p_CodigoProposito = "PERS";
            entradaCrearTelefono.p_Comentario = "Prueba de creación de teléfono";
            entradaCrearTelefono.p_IdEstado = "ACT";
            entradaCrearTelefono.p_IdPersona = IdPersonaObtenido;
            entradaCrearTelefono.p_Predeterminado = "N";

            // Llamar a la lógica del servicio para crear el teléfono
            CrearTelefono.SalidaCrearTelefono salidaCrearTelefono = crearTelefono.logica(conn, entradaCrearTelefono);

            // Validar que el ID del teléfono creado sea válido
            assertNotNull(salidaCrearTelefono.p_out_RefId, "El ID del teléfono no debería ser nulo");
            assertTrue(salidaCrearTelefono.p_out_RefId > 0, "El ID del teléfono debería ser mayor a 0");
            conn.rollback();
        }
    }
	
}
