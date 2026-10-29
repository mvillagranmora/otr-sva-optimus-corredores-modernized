package cl.bcs.plataforma.tests;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ModificarPersonaGrupoDireccionElectronicaDetalleMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.SQL;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;

import java.util.HashMap;
import java.util.List;

public class TestModificarPersonaGrupoDireccionElectronicaDetalleMasivo {
	
	
	private static final String SELECT_GRUPO_DIRECCION_DETALLE = "SELECT TOP 3 \"IdGrupoDireccionDetalle\", \"IdEstado\" FROM \"BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle\" ORDER BY \"IdGrupoDireccionDetalle\" DESC";

	    public static class GrupoDireccionDetalle {
	        public Long IdGrupoDireccionDetalle;
	        public String IdEstado;
	    }

	    @Test
	    void testModificarPersonaGrupoDireccionElectronicaDetalleMasivo_CambiosValidos() throws Exception {
	        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
	            // Instanciar el servicio
	            ModificarPersonaGrupoDireccionElectronicaDetalleMasivo modificarServicio = new ModificarPersonaGrupoDireccionElectronicaDetalleMasivo();
	            SQL sql = new SQL();

	            // Obtener datos de prueba desde la base de datos
	            List<GrupoDireccionDetalle> detalles = sql.query(
	                conn, SELECT_GRUPO_DIRECCION_DETALLE, new HashMap<>(), GrupoDireccionDetalle.class
	            );
	            
	            assertTrue(detalles.size() >= 3, "Debe haber al menos 3 registros para ejecutar la prueba masiva");

	            // Configurar la entrada para los cambios
	            EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
	            entrada.p_IdEmpresa = 1L;
	            entrada.p_IdUsuario = 0L;
	            entrada.p_IdModulo = "PLA";
	            entrada.p_UsaReglasNegocio = "N";

	            // Crear múltiples registros de cambio
	            for (int i = 0; i < 3; i++) {
	                RegistroCambioMasivo registroCambio = new RegistroCambioMasivo();
	                GrupoDireccionDetalle detalle = detalles.get(i);

	                assertNotEquals("INA", detalle.IdEstado, 
	                    "El estado previo debe ser distinto al estado que se va a actualizar");

	                registroCambio.IdEntidad = detalle.IdGrupoDireccionDetalle;
	                registroCambio.CampoModificado = "IdEstado";
	                registroCambio.ValorNuevo = "INA";

	                entrada.p_RegistroCambio.add(registroCambio);
	            }

	            // Ejecutar la lógica del servicio
	            SalidaEjecutarCambiosPersona salida = modificarServicio.logica(conn, entrada);

	            // Validar la salida
	            assertNotNull(salida, "La salida no debería ser nula");
	            assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deberían existir errores en la modificación masiva");
	            assertNotNull(salida.P_OUT_REFID, "El ID de referencia no debería ser nulo");
	            assertTrue(salida.P_OUT_REFID > 0, "El ID de referencia debería ser mayor a 0");
	            assertNotNull(salida.P_OUT_MENSAJES, "La lista de mensajes no debería ser nula");
	            assertTrue(salida.P_OUT_MENSAJES.isEmpty(), "No deberían existir mensajes de error en la salida");

	            // Revertir los cambios realizados en la base de datos
	            conn.rollback();
	        }
	    }
	    
}
