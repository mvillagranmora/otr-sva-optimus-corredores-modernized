package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.aut.CrearEventoAutorizacion;
import cl.bcs.plataforma.aut.CrearEventoAutorizacion.Entrada;
import cl.bcs.plataforma.aut.CrearEventoAutorizacion.Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Utils;

class TestCrearEventoAutorizacion {
	
	@Test
	void testCrearEventoAutorizacion() throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN TestCrearEventoAutorizacion ==");
			Gson gson = Utils.getGsonBuilder();
			
			String json = "{\n" +
				"  \"p_IdEmpresa\": 1,\n" +
				"  \"p_IdUsuario\": 3,\n" +
				"  \"p_IdModulo\": \"PLA\",\n" +
				"  \"p_simularOperacion\": \"N\",\n" +
				"  \"p_UsaReglasNegocio\": \"N\",\n" +
				"  \"p_IdTipoEvento\": \"ENROLU\",\n" +
				"  \"p_IdModuloSolicita\": \"PLA\",\n" +
				"  \"p_IdUsuarioSolicita\": 3,\n" +
				"  \"p_Fecha\": \"2024-01-15\",\n" +
				"  \"p_IdTipoOperacion\": \"\",\n" +
				"  \"p_IdConcepto\": \"ENROLU\",\n" +
				"  \"p_NumeroOperacionConcepto\": \"12345\",\n" +
				"  \"p_FechaOperacionConcepto\": \"2024-01-15\",\n" +
				"  \"p_IdCliente\": 0,\n" +
				"  \"p_IdFuncionario\": 0,\n" +
				"  \"p_IdProveedor\": 0,\n" +
				"  \"p_IdTipoProducto\": \"\",\n" +
				"  \"p_IdClasificacionTipoInstrumento\": \"\",\n" +
				"  \"p_IdTipoInstrumento\": \"\",\n" +
				"  \"p_IdInstrumento\": 0,\n" +
				"  \"p_Nemo\": \"\",
			"  \"p_Cantidad\": 0,\n" +
				"  \"p_Monto\": 0,\n" +
				"  \"p_Precio\": 0,\n" +
				"  \"p_Tasa\": 0,\n" +
				"  \"p_IdMoneda\": \"\",\n" +
				"  \"p_Glosa\": \"Prueba de creacion de evento de autorizacion\",\n" +
				"  \"p_IdCanal\": \"OPT\"\n" +
				"}";
			
			Entrada entrada = gson.fromJson(json, Entrada.class);
			
			CrearEventoAutorizacion servicio = new CrearEventoAutorizacion();
			JsonObject out = servicio.ejecutar(conn, gson.toJsonTree(entrada).getAsJsonObject());
			
			System.out.println("Resultado: " + out.toString());
			
			// Verificar que no haya errores
			Long errorCount = out.get("P_OUT_ERRORESCONTADOR").getAsLong();
			assertEquals(0L, errorCount, "No debe haber errores");
			
			// Verificar que se haya creado el evento
			Long idEvento = out.get("P_OUT_IDEVENTO").getAsLong();
			assertTrue(idEvento > 0, "Debe haberse creado un evento con ID mayor a 0");
			
			System.out.println("Evento creado con ID: " + idEvento);
			
			conn.rollback();
			System.out.println("== END TestCrearEventoAutorizacion ==");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
	
	@Test
	void testCrearEventoAutorizacionConErrores() throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			System.out.println("== BEGIN TestCrearEventoAutorizacionConErrores ==");
			Gson gson = Utils.getGsonBuilder();
			
			// JSON con datos inválidos para provocar errores
			String json = "{\n" +
				"  \"p_IdEmpresa\": 1,\n" +
				"  \"p_IdUsuario\": 3,\n" +
				"  \"p_IdModulo\": \"PLA\",\n" +
				"  \"p_simularOperacion\": \"N\",\n" +
				"  \"p_UsaReglasNegocio\": \"N\",\n" +
				"  \"p_IdTipoEvento\": \"NOEXISTE\",\n" +
				"  \"p_IdModuloSolicita\": \"PLA\",\n" +
				"  \"p_IdUsuarioSolicita\": 3,\n" +
				"  \"p_Fecha\": \"2024-01-15\",\n" +
				"  \"p_IdTipoOperacion\": \"\",\n" +
				"  \"p_IdConcepto\": \"ENROLU\",\n" +
				"  \"p_NumeroOperacionConcepto\": \"12345\",\n" +
				"  \"p_FechaOperacionConcepto\": \"2024-01-15\",\n" +
				"  \"p_IdCliente\": 0,\n" +
				"  \"p_IdFuncionario\": 0,\n" +
				"  \"p_IdProveedor\": 0,\n" +
				"  \"p_IdTipoProducto\": \"\",\n" +
				"  \"p_IdClasificacionTipoInstrumento\": \"\",\n" +
				"  \"p_IdTipoInstrumento\": \"\",\n" +
				"  \"p_IdInstrumento\": 0,\n" +
				"  \"p_Nemo\": \"\",\n" +
				"  \"p_Cantidad\": 0,\n" +
				"  \"p_Monto\": 0,\n" +
				"  \"p_Precio\": 0,\n" +
				"  \"p_Tasa\": 0,\n" +
				"  \"p_IdMoneda\": \"\",\n" +
				"  \"p_Glosa\": \"Prueba con tipo de evento inexistente\",\n" +
				"  \"p_IdCanal\": \"OPT\"\n" +
				"}";
			
			Entrada entrada = gson.fromJson(json, Entrada.class);
			
			CrearEventoAutorizacion servicio = new CrearEventoAutorizacion();
			JsonObject out = servicio.ejecutar(conn, gson.toJsonTree(entrada).getAsJsonObject());
			
			System.out.println("Resultado: " + out.toString());
			
			// Verificar que haya errores
			Long errorCount = out.get("P_OUT_ERRORESCONTADOR").getAsLong();
			assertTrue(errorCount > 0, "Debe haber al menos un error");
			
			System.out.println("Errores detectados correctamente: " + errorCount);
			
			conn.rollback();
			System.out.println("== END TestCrearEventoAutorizacionConErrores ==");
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
}
