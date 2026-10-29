package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.SalidaEjecutarCambiosPersona;
import cl.bcs.socionegocio.per.ModificarPersonaTarjetaBancariaMasivo;
import cl.bcs.socionegocio.per.pojo.RegistroCambioMasivo;
import cl.bcs.utils.SQL;
import cl.bcs.socionegocio.per.EjecutarCambiosPersona.EntradaEjecutarCambiosPersona;

public class TestModificarPersonaTarjetaBancariaMasivo {
	
	public static final String SELECT_ID_TARJETA_BANCARIA = "SELECT TOP 3 \"IdTarjetaBancaria\", \"NumeroTarjetaBancaria\", \"FechaVencimiento\" FROM \"BSCL.SocioNegocio.BD::PER.TarjetaBancaria\" ORDER BY \"IdTarjetaBancaria\" DESC";
	        

	    public static class TarjetaBancaria {
	        public Long IdTarjetaBancaria;
	        public String NumeroTarjetaBancaria;
	        public java.sql.Date FechaVencimiento;
	    }

	    @Test
	    void testModificarPersonaTarjetaBancariaMasivo_CambiosValidos() throws Exception {
	        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
	            // Crear la instancia del servicio
	            ModificarPersonaTarjetaBancariaMasivo modificarPersonaTarjetaBancariaMasivo = new ModificarPersonaTarjetaBancariaMasivo();
	            SQL sql = new SQL();

	            // Obtener los datos de prueba desde la base de datos
	            List<TarjetaBancaria> listaTarjetasBancarias = sql.query(conn, SELECT_ID_TARJETA_BANCARIA, new HashMap<>(), TarjetaBancaria.class);
	            
	            assertTrue(listaTarjetasBancarias.size() >= 3, "Debe haber al menos 3 tarjetas bancarias para ejecutar la prueba masiva");

	            // Crear la entrada con datos de prueba
	            EntradaEjecutarCambiosPersona entrada = new EntradaEjecutarCambiosPersona();
	            entrada.p_IdEmpresa = 1L;
	            entrada.p_IdUsuario = 0L;
	            entrada.p_IdModulo = "PER";
	            entrada.p_UsaReglasNegocio = "S";

	            // Crear múltiples registros de cambio
	            for (int i = 0; i < 3; i++) {
	                RegistroCambioMasivo cambioFechaVencimiento = new RegistroCambioMasivo();
	                TarjetaBancaria tarjeta = listaTarjetasBancarias.get(i);

	                // Validar que la fecha de vencimiento es distinta a la que se va a actualizar
	                java.sql.Date nuevaFecha = java.sql.Date.valueOf("2025-12-31");
	                assertTrue(!nuevaFecha.equals(tarjeta.FechaVencimiento), 
	                    "La fecha previa es distinta a la fecha que se va a actualizar");

	                cambioFechaVencimiento.IdEntidad = tarjeta.IdTarjetaBancaria;
	                cambioFechaVencimiento.CampoModificado = "FechaVencimiento";
	                cambioFechaVencimiento.ValorNuevo = nuevaFecha.toString();

	                entrada.p_RegistroCambio.add(cambioFechaVencimiento);
	            }

	            // Llamar a la lógica del servicio
	            SalidaEjecutarCambiosPersona salida = modificarPersonaTarjetaBancariaMasivo.logica(conn, entrada);

	            // Validar que no hubo errores
	            assertNotNull(salida, "La salida no debería ser nula");
	            assertEquals(0L, salida.P_OUT_ERRORESCONTADOR, "No deberían existir errores en la modificación masiva");

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
	
}
