package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EliminarPersonaTelefonoMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.SQL;

public class TestEliminarPersonaTelefonoMasivo {
	
	private static final String SELECT_IDS_TELEFONOS = "SELECT TOP 3 \"IdTelefono\"  FROM \"BSCL.SocioNegocio.BD::PER.Telefono\" WHERE \"Predeterminado\" = 'N' ORDER BY \"IdTelefono\" DESC";

    public static class Telefono {
        public long IdTelefono;
    }

    @Test
    void TestEliminarTelefonos() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Crear instancia del servicio
        	EliminarPersonaTelefonoMasivo eliminarPersonaTelefonoMasivo = new EliminarPersonaTelefonoMasivo();
            SQL sql = new SQL();

            // Obtener los datos de prueba desde la base de datos
            List<Telefono> listaTelefonos = sql.query(conn, SELECT_IDS_TELEFONOS, new HashMap<>(), Telefono.class);

            assertTrue(listaTelefonos.size() >= 3, "Debe haber al menos 3 personas activas para ejecutar la prueba");

            // Crear la entrada con datos de prueba
            EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdUsuario = 0L;
            entrada.p_IdModulo = "PER";
            entrada.p_UsaReglasNegocio = "S";

            // Crear múltiples registros de cambio
            for (int i = 0; i < 3; i++) {
                RegistroCambioMasivo cambioEstado = new RegistroCambioMasivo();
                Telefono persona = listaTelefonos.get(i);

                cambioEstado.IdEntidad = persona.IdTelefono;
                cambioEstado.CampoModificado = "Borrado";
                cambioEstado.ValorNuevo = "S";

                entrada.p_RegistroCambio.add(cambioEstado);
            }

            // Llamar a la lógica del servicio
            SalidaEjecutarCambiosPersona salida = eliminarPersonaTelefonoMasivo.logica(conn, entrada);

            // Validar que no hubo errores
            assertNotNull(salida, "La salida no debería ser nula");
            assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deberían existir errores en la ejecución de cambios");

            // Validar los mensajes de salida
            assertNotNull(salida.P_OUT_MENSAJES, "La lista de mensajes no debería ser nula");
            assertTrue(salida.P_OUT_MENSAJES.isEmpty(), "No deberían existir mensajes de error en la salida");

            // Validar que los ID de referencia sean válidos
            assertNotNull(salida.P_OUT_REFID, "El ID de referencia no debería ser nulo");
            assertTrue(salida.P_OUT_REFID > 0, "El ID de referencia debería ser mayor a 0");

            // Revertir los cambios realizados en la BD
            conn.rollback();
        }
    }

    @Test
    void TestEliminarTelefonoInexistente() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Crear instancia del servicio
            EliminarPersonaTelefonoMasivo eliminarPersonaTelefonoMasivo = new EliminarPersonaTelefonoMasivo();

            // Crear la entrada con datos de prueba
            EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdUsuario = 0L;
            entrada.p_IdModulo = "PER";
            entrada.p_UsaReglasNegocio = "S";

            // Crear un cambio para una persona no existente
            RegistroCambioMasivo cambioEstado = new RegistroCambioMasivo();
            cambioEstado.IdEntidad = -1L; // ID de entidad inexistente
            cambioEstado.CampoModificado = "Borrado";
            cambioEstado.ValorNuevo = "S";
            entrada.p_RegistroCambio.add(cambioEstado);

            // Llamar a la lógica del servicio
            SalidaEjecutarCambiosPersona salida = eliminarPersonaTelefonoMasivo.logica(conn, entrada);

            // Validar que haya un mensaje de error indicando que la entidad no existe
            assertNotNull(salida.P_OUT_MENSAJES, "La lista de mensajes no debería ser nula");
            assertEquals("ENTIDAD_NO_EXISTE", salida.P_OUT_MENSAJES.get(0).codigo,
                "Debería haber un mensaje de error indicando que la entidad no existe");
        }
    }
	
}
