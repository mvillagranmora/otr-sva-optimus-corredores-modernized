package cl.bcs.plataforma.tests;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearPersonaGrupoDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.pojo.GrupoDireccionElectronica;
import cl.bcs.utils.SQL;
import cl.bcs.socionegocio.per.CrearPersonaGrupoDireccionElectronicaMasivo.EntradaCrearPersonaGrupoDireccionElectronicaMasivo;
import cl.bcs.socionegocio.per.CrearPersonaGrupoDireccionElectronicaMasivo.SalidaCrearPersonaGrupoDireccionElectronicaMasivo;

import static org.junit.jupiter.api.Assertions.*;

public class TestCrearPersonaGrupoDireccionElectronicaMasivo {
	
	String SELECT_ID_PERSONA = "SELECT TOP 1 \"IdPersona\" FROM BSCL.\"BSCL.SocioNegocio.BD::PER.Persona\" ORDER BY \"IdPersona\" DESC";
	
	public static class IdPersona {
        public Long IdPersona;
    }
	
	@Test
    void testCrearPersonaGrupoDireccionElectronicaMasivo_CasoBasico() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Instancia del servicio
            CrearPersonaGrupoDireccionElectronicaMasivo servicio = new CrearPersonaGrupoDireccionElectronicaMasivo();
            
            SQL sql = new SQL();

            // Obtener un ID de persona de prueba desde la base de datos
            List<IdPersona> listaPersonas = sql.query(conn, SELECT_ID_PERSONA, new HashMap<>(), IdPersona.class);
            long IdPersonaObtenido = listaPersonas.get(0).IdPersona;
            
            
            // Crear entrada con datos de prueba
            EntradaCrearPersonaGrupoDireccionElectronicaMasivo entrada = new EntradaCrearPersonaGrupoDireccionElectronicaMasivo();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdUsuario = 0L;
            entrada.p_IdModulo = "PLA";
            entrada.p_UsaReglasNegocio = "N";

            List<GrupoDireccionElectronica> grupos = new ArrayList<>();
            for (int i = 1; i <= 3; i++) {
                GrupoDireccionElectronica grupo = new GrupoDireccionElectronica();
                grupo.IdInterno = (long) i;
                grupo.Grupo = "Grupo Prueba " + i;
                grupo.Comentario = "Comentario del Grupo " + i;
                grupo.IdPersona = IdPersonaObtenido; 
                grupo.IdEstado = "ACT";
                grupos.add(grupo);
            }
            entrada.p_GruposDireccionesElectronicas = grupos;

            // Llamar a la lógica del servicio
            SalidaCrearPersonaGrupoDireccionElectronicaMasivo salida = servicio.logica(conn, entrada);

            // Validaciones de salida
            assertNotNull(salida, "La salida no debería ser nula");
            assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No debería haber errores en el procesamiento");
            assertNotNull(salida.P_OUT_REFID, "Debería haber referencias de los grupos creados");
            assertEquals(3, salida.P_OUT_REFID.size(), "Deberían haberse creado 3 grupos");

            // Validar que no hay mensajes de error
            assertTrue(salida.P_OUT_MENSAJES.isEmpty(), "No deberían existir mensajes de error en la creación");

            // Revertir los cambios realizados durante la prueba
            conn.rollback();
        }
    }
	
}
