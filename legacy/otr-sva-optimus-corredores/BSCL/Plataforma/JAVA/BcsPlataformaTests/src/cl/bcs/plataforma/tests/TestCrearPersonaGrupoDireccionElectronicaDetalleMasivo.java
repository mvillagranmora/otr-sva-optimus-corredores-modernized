package cl.bcs.plataforma.tests;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.CrearPersonaGrupoDireccionElectronicaDetalleMasivo;
import cl.bcs.socionegocio.per.pojo.GrupoDireccionElectronica;
import cl.bcs.utils.SQL;

import static org.junit.jupiter.api.Assertions.*;

public class TestCrearPersonaGrupoDireccionElectronicaDetalleMasivo {
	
	String SELECT_ID_DIRECCION_ELECTRONICA = "SELECT TOP 3 \"IdDireccionElectronica\" FROM \"BSCL.SocioNegocio.BD::PER.DireccionElectronica\" WHERE \"IdEstado\" = 'ACT'";
    String SELECT_ID_GRUPO_DIRECCION = "SELECT TOP 1 \"IdGrupoDireccion\" FROM \"BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle\" WHERE \"IdEstado\" = 'ACT'";

    public static class IdDireccionElectronica {
        public Long IdDireccionElectronica;
    }

    public static class IdGrupoDireccion {
        public Long IdGrupoDireccion;
    }

    @Test
    void testCrearPersonaGrupoDireccionElectronicaDetalleMasivo_CasoBasico() throws Exception {
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            // Crear la instancia del servicio
            CrearPersonaGrupoDireccionElectronicaDetalleMasivo servicio = new CrearPersonaGrupoDireccionElectronicaDetalleMasivo();
            SQL sql = new SQL();

            // Obtener IDs de direcciones electrónicas de prueba
            List<IdDireccionElectronica> listaDirecciones = sql.query(conn, SELECT_ID_DIRECCION_ELECTRONICA, new HashMap<>(), IdDireccionElectronica.class);
            assertTrue(listaDirecciones.size() >= 3, "Debe haber al menos 3 direcciones electrónicas activas para la prueba");

            // Obtener un ID de grupo de prueba desde la base de datos
            List<IdGrupoDireccion> listaGrupos = sql.query(conn, SELECT_ID_GRUPO_DIRECCION, new HashMap<>(), IdGrupoDireccion.class);
            long IdGrupoDireccionObtenido = listaGrupos.get(0).IdGrupoDireccion;

            // Crear la entrada con datos de prueba
            CrearPersonaGrupoDireccionElectronicaDetalleMasivo.EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo entrada =
                new CrearPersonaGrupoDireccionElectronicaDetalleMasivo.EntradaCrearPersonaGrupoDireccionElectronicaDetalleMasivo();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdUsuario = 0L;
            entrada.p_IdModulo = "PER";
            entrada.p_UsaReglasNegocio = "N"; // No requiere reglas de negocio para esta prueba

            // Agregar detalles a la entrada
            for (int i = 0; i < 3; i++) {
                GrupoDireccionElectronica.Detalle detalle = new GrupoDireccionElectronica.Detalle();
                detalle.IdDireccionElectronica = listaDirecciones.get(i).IdDireccionElectronica;
                detalle.IdGrupoDireccion = IdGrupoDireccionObtenido;
                detalle.IdTipoDestinatario = "PARA";
                detalle.IdEstado = "ACT";
                entrada.p_Detalles.add(detalle);
            }

            // Llamar a la lógica del servicio
            CrearPersonaGrupoDireccionElectronicaDetalleMasivo.SalidaCrearPersonaGrupoDireccionElectronicaDetalleMasivo salida = servicio.logica(conn, entrada, true);

            // Validar la salida
            assertNotNull(salida, "La salida no debería ser nula");
            assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deberían existir errores en la creación de los detalles");

            // Validar que se generaron IDs de referencia para los detalles
            assertNotNull(salida.P_OUT_REFID, "Los IDs de referencia no deberían ser nulos");
            assertEquals(3, salida.P_OUT_REFID.size(), "Deberían haberse creado exactamente 3 detalles");

            // Validar los mensajes de salida
            assertTrue(salida.P_OUT_MENSAJES.isEmpty(), "No deberían existir mensajes de error en la creación de los detalles");

            // Revertir cambios
            conn.rollback();
        }
    }
	
}
