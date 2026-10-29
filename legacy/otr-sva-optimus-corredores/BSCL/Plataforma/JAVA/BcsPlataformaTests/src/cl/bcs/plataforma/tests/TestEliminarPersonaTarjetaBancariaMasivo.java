package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.EliminarPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.SQL;

import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;



public class TestEliminarPersonaTarjetaBancariaMasivo {
	
	public static String SELECT_ID_TARJETA_BANCARIA = 
	        "SELECT TOP 3 \"IdTarjetaBancaria\" FROM \"BSCL.SocioNegocio.BD::PER.TarjetaBancaria\" ORDER BY \"IdTarjetaBancaria\" DESC";

	    public static class IdTarjetaBancaria {
	        public long IdTarjetaBancaria;
	    }

	    @Test
	    void testEliminarPersonaTarjetaBancariaMasivo_CambiosValidos() throws Exception {
	        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
	            // Crear la instancia del servicio
	            EliminarPersonaTarjetaBancariaMasivo eliminarPersonaTarjetaBancariaMasivo = new EliminarPersonaTarjetaBancariaMasivo();
	            SQL sql = new SQL();

	            // Obtener los datos de prueba desde la base de datos
	            List<IdTarjetaBancaria> listaTarjetasBancarias = sql.query(conn, SELECT_ID_TARJETA_BANCARIA, new HashMap<>(), IdTarjetaBancaria.class);
	            
	            assertTrue(listaTarjetasBancarias.size() >= 3, "Debe haber al menos 3 tarjetas bancarias para ejecutar la prueba masiva");

	            // Crear la entrada con datos de prueba
	            EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
	            entrada.p_IdEmpresa = 1L;
	            entrada.p_IdUsuario = 0L;
	            entrada.p_IdModulo = "PER";
	            entrada.p_UsaReglasNegocio = "S";

	            // Crear múltiples registros de cambio
	            for (int i = 0; i < 3; i++) {
	                RegistroCambioMasivo cambioNumeroTarjeta = new RegistroCambioMasivo();
	                IdTarjetaBancaria tarjeta = listaTarjetasBancarias.get(i);

	                cambioNumeroTarjeta.IdEntidad = tarjeta.IdTarjetaBancaria;
	                cambioNumeroTarjeta.CampoModificado = "Borrado";
	                cambioNumeroTarjeta.ValorNuevo = "S";

	                entrada.p_RegistroCambio.add(cambioNumeroTarjeta);
	            }

	            // Llamar a la lógica del servicio
	            SalidaEjecutarCambiosPersona salida = eliminarPersonaTarjetaBancariaMasivo.logica(conn, entrada);

	            // Validar que no hubo errores
	            assertNotNull(salida, "La salida no debería ser nula");
	            assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deberían existir errores en la eliminación masiva");

	            // Validar que los ID de referencia sean válidos
	            assertNotNull(salida.P_OUT_REFID, "El ID de referencia no debería ser nulo");
	            assertTrue(salida.P_OUT_REFID > 0, "El ID de referencia debería ser mayor a 0");

	            // Validar los mensajes de salida
	            assertNotNull(salida.P_OUT_MENSAJES, "La lista de mensajes no debería ser nula");
	            assertTrue(salida.P_OUT_MENSAJES.isEmpty(), "No deberían existir mensajes de error en la salida");

	            // Revertir los cambios realizados en la BD
	            conn.rollback();
	        }
	    }

	    @Test
	    void testEliminarPersonaTarjetaBancariaMasivo_ErrorEntidadNoExiste() throws Exception {
	        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
	            // Crear la instancia del servicio
	            EliminarPersonaTarjetaBancariaMasivo eliminarPersonaTarjetaBancariaMasivo = new EliminarPersonaTarjetaBancariaMasivo();

	            // Crear la entrada con datos de prueba
	            EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
	            entrada.p_IdEmpresa = 1L;
	            entrada.p_IdUsuario = 0L;
	            entrada.p_IdModulo = "PER";
	            entrada.p_UsaReglasNegocio = "S";

	            // Crear un cambio para una tarjeta no existente
	            RegistroCambioMasivo cambioNumeroTarjeta = new RegistroCambioMasivo();
	            cambioNumeroTarjeta.IdEntidad = -4L; // ID de entidad inexistente
	            cambioNumeroTarjeta.CampoModificado = "Borrado";
	            cambioNumeroTarjeta.ValorNuevo = "S";
	            entrada.p_RegistroCambio.add(cambioNumeroTarjeta);

	            // Llamar a la lógica del servicio
	            SalidaEjecutarCambiosPersona salida = eliminarPersonaTarjetaBancariaMasivo.logica(conn, entrada);

	            // Validar que haya un mensaje de error indicando que la entidad no existe
	            assertNotNull(salida.P_OUT_MENSAJES, "La lista de mensajes no debería ser nula");
	            assertEquals("ENTIDAD_NO_EXISTE", salida.P_OUT_MENSAJES.get(0).codigo,
	                "Debería haber un mensaje de error indicando que la entidad no existe");
	        }
	    }
	
}
