package cl.bcs.plataforma.tests;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearPersonaGrupoDireccionElectronicaConDetalle;
import cl.bcs.socionegocio.per.pojo.GrupoDireccionElectronica;
import cl.bcs.utils.SQL;

import static org.junit.jupiter.api.Assertions.*;

public class TestCrearPersonaGrupoDireccionElectronicaConDetalle {
	
	String SELECT_ID_PERSONA = "SELECT TOP 1 \"IdPersona\" FROM BSCL.\"BSCL.SocioNegocio.BD::PER.Persona\" ORDER BY \"IdPersona\" DESC";
    String SELECT_ID_DIRECCION_ELECTRONICA = "SELECT TOP 3 \"IdDireccionElectronica\" FROM \"BSCL.SocioNegocio.BD::PER.DireccionElectronica\" WHERE \"IdEstado\" = 'ACT'";

    public static class IdPersona {
        public Long IdPersona;
    }

    public static class IdDireccionElectronica {
        public Long IdDireccionElectronica;
    }

    @Test
    void testCrearPersonaGrupoDireccionElectronicaConDetalle_CasoBasico() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Crear la instancia del servicio
            CrearPersonaGrupoDireccionElectronicaConDetalle servicio = new CrearPersonaGrupoDireccionElectronicaConDetalle();
            SQL sql = new SQL();

            // Obtener un ID de persona de prueba desde la base de datos
            List<IdPersona> listaPersonas = sql.query(conn, SELECT_ID_PERSONA, new HashMap<>(), IdPersona.class);
            long IdPersonaObtenido = listaPersonas.get(0).IdPersona;

            // Obtener IDs de direcciones electrónicas de prueba
            List<IdDireccionElectronica> listaDirecciones = sql.query(conn, SELECT_ID_DIRECCION_ELECTRONICA, new HashMap<>(), IdDireccionElectronica.class);
            assertTrue(listaDirecciones.size() >= 3, "Debe haber al menos 3 direcciones electrónicas activas para la prueba");

            // Crear la entrada con datos de prueba
            CrearPersonaGrupoDireccionElectronicaConDetalle.EntradaCrearGrupoDireccionElectronicaConDetalle entrada = 
                new CrearPersonaGrupoDireccionElectronicaConDetalle.EntradaCrearGrupoDireccionElectronicaConDetalle();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdUsuario = 0L;
            entrada.p_IdModulo = "PER";
            entrada.p_UsaReglasNegocio = "N"; // No requiere reglas de negocio para esta prueba

            GrupoDireccionElectronica grupo = entrada.p_GrupoDireccionElectronica;
            grupo.Grupo = "Grupo Prueba JUnit";
            grupo.Comentario = "Prueba automática de creación de grupo";
            grupo.IdPersona = IdPersonaObtenido;
            grupo.IdEstado = "ACT";

            // Agregar detalles al grupo
            for (int i = 0; i < 3; i++) {
            	GrupoDireccionElectronica.Detalle detalle = new GrupoDireccionElectronica.Detalle();
                detalle.IdDireccionElectronica = listaDirecciones.get(i).IdDireccionElectronica;
                detalle.IdTipoDestinatario = "PARA";
                detalle.IdEstado = "ACT";
                grupo.Detalles.add(detalle);
            }

            // Llamar a la lógica del servicio
            CrearPersonaGrupoDireccionElectronicaConDetalle.SalidaCrearGrupoDireccionElectronicaConDetalle salida = servicio.logica(conn, entrada);

            // Validar la salida
            assertNotNull(salida, "La salida no debería ser nula");
            assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deberían existir errores en la creación del grupo");

            // Validar que se creó el grupo y se generó un ID de referencia
            assertNotNull(salida.P_OUT_REFID, "El ID de referencia del grupo creado no debería ser nulo");

            // Validar los mensajes de salida
            assertTrue(salida.P_OUT_MENSAJES.isEmpty(), "No deberían existir mensajes de error en la creación del grupo");

            // Revertir cambios
            conn.rollback();
        }
    }
	
}
