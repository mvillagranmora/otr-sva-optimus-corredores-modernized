package cl.bcs.plataforma.tests;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearTelefonoMasivo;
import cl.bcs.utils.SQL;



public class TestCrearTelefonoMasivo {
	
	public static String SELECT_ID_PERSONA = "SELECT \"IdPersona\" FROM BSCL.\"BSCL.SocioNegocio.BD::PER.Persona\" ORDER BY \"IdPersona\" DESC";
	
	public static class IdPersona {
		public long IdPersona;
	}
	
	@Test
    void testCrearTelefonosMasivos() throws Exception {
        // Usamos la conexión de prueba desde PlataformaUtils
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            
            // Crear la instancia del servicio
            CrearTelefonoMasivo crearTelefonoMasivo = new CrearTelefonoMasivo();
            
            SQL sql = new SQL();
    		List<IdPersona> IdPersona = sql.query(conn, SELECT_ID_PERSONA, new HashMap<String,Object>(), IdPersona.class);
            
    		long IdPersonaObtenido = IdPersona.get(0).IdPersona; 
            
            // Crear la entrada para el servicio de creación masiva de teléfonos
            CrearTelefonoMasivo.EntradaCrearTelefonoMasivo entrada = new CrearTelefonoMasivo.EntradaCrearTelefonoMasivo();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdUsuario = 0L;
            entrada.p_IdModulo = "PER";
            entrada.p_UsaReglasNegocio = "S";
            
            // Lista de teléfonos a crear
            CrearTelefonoMasivo.Telefono telefono1 = new CrearTelefonoMasivo.Telefono();
            telefono1.IdInterno = 1L;
            telefono1.IdPais = 44L; 
            telefono1.IdTipoTelefono = "MOVIL";
            telefono1.Numero = "987654321";
            telefono1.Anexo = "123";
            telefono1.CodigoProposito = "PERS";
            telefono1.Comentario = "Teléfono personal";
            telefono1.IdEstado = "ACT";
            telefono1.IdPersona = IdPersonaObtenido;
            telefono1.Predeterminado = "N";
            telefono1.IdUsuarioCreador = 100L;
            telefono1.IdEstadoAutorizacion = "AUTORIZADO";

            CrearTelefonoMasivo.Telefono telefono2 = new CrearTelefonoMasivo.Telefono();
            telefono2.IdInterno = 2L;
            telefono2.IdPais = 44L;
            telefono2.IdTipoTelefono = "TRABAJO";
            telefono2.Numero = "987654322";
            telefono2.Anexo = "124";
            telefono2.CodigoProposito = "TRABAJO";
            telefono2.Comentario = "Teléfono de trabajo";
            telefono2.IdEstado = "ACT";
            telefono2.IdPersona = IdPersonaObtenido;
            telefono2.Predeterminado = "N";
            telefono2.IdUsuarioCreador = 100L;
            telefono2.IdEstadoAutorizacion = "AUTORIZADO";

            // Añadir los teléfonos a la entrada
            entrada.p_Telefonos.add(telefono1);
            entrada.p_Telefonos.add(telefono2);

            // Llamar al servicio de creación masiva de teléfonos
            CrearTelefonoMasivo.SalidaCrearTelefonoMasivo salida = crearTelefonoMasivo.logica(conn, entrada);

            // Validar que no hubo errores en la creación
            assertNotNull(salida, "La salida del servicio no debe ser nula.");
            assertTrue(salida.P_OUT_REFID != null && salida.P_OUT_REFID.size() > 0, "Debe haber referencias de teléfonos creados.");
            assertTrue(salida.P_OUT_REFID.stream().allMatch(ref -> ref.IdEntidad != null && ref.IdEntidad > 0), "Todos los IDs de teléfono deben ser mayores que 0");

            // También puedes validar el contador de errores si existe en la respuesta
            assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deben haber errores en la creación de teléfonos.");
            
            conn.rollback();
        } catch (Exception e) {
            fail("La prueba falló con la excepción: " + e.getMessage());
        }
    }
}
