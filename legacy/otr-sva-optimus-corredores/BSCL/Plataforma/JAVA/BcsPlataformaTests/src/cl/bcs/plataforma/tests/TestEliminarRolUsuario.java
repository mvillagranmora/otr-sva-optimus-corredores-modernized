package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.plataforma.acc.EliminarRolUsuario;
import cl.bcs.plataforma.acc.EliminarRolUsuario.EntradaEliminarRolUsuario;
import cl.bcs.plataforma.acc.EliminarRolUsuario.SalidaEliminarRolUsuario;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

class TestEliminarRolUsuario {

	@Test
	void testEliminarRolUsuarioExitoso() throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			EliminarRolUsuario servicio = new EliminarRolUsuario();

			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, 1L, "PLATAFORMA",
					"PLA.FechaSistema");

			EntradaEliminarRolUsuario entrada = new EntradaEliminarRolUsuario();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 3L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdUsuarioRol = 25L;
			entrada.p_IdRol = 5L;
			entrada.p_IdEmpresaRol = 1L;
			entrada.p_CodigoUsuario = "USUARIO_TEST";
			entrada.p_FechaSistema = fechaSistema;

			SalidaEliminarRolUsuario salida = servicio.logica(conn, entrada);

			assertNotNull(salida);
			assertTrue(salida.p_out_ErroresContador == 0 || salida.p_out_AdvertenciasContador > 0);

			conn.rollback();
		}
	}

	@Test
	void testEliminarRolUsuarioRolNoPertenece() throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			EliminarRolUsuario servicio = new EliminarRolUsuario();

			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, 1L, "PLATAFORMA",
					"PLA.FechaSistema");

			EntradaEliminarRolUsuario entrada = new EntradaEliminarRolUsuario();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 3L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdUsuarioRol = 25L;
			entrada.p_IdRol = 999999L; // Rol inexistente
			entrada.p_IdEmpresaRol = 1L;
			entrada.p_CodigoUsuario = "USUARIO_TEST";
			entrada.p_FechaSistema = fechaSistema;

			SalidaEliminarRolUsuario salida = servicio.logica(conn, entrada);

			assertNotNull(salida);
			assertTrue(salida.p_out_ErroresContador > 0);
			assertEquals("ROL_NO_PERTENECE_EMPRESA", salida.p_out_ErroresDetalle.get(0).Codigo);

			conn.rollback();
		}
	}

	@Test
	void testEliminarRolUsuarioNoTieneRol() throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			EliminarRolUsuario servicio = new EliminarRolUsuario();

			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, 1L, "PLATAFORMA",
					"PLA.FechaSistema");

			EntradaEliminarRolUsuario entrada = new EntradaEliminarRolUsuario();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 3L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdUsuarioRol = 999999L; // Usuario inexistente
			entrada.p_IdRol = 5L;
			entrada.p_IdEmpresaRol = 1L;
			entrada.p_CodigoUsuario = "USUARIO_TEST";
			entrada.p_FechaSistema = fechaSistema;

			SalidaEliminarRolUsuario salida = servicio.logica(conn, entrada);

			assertNotNull(salida);
			assertTrue(salida.p_out_ErroresContador > 0);

			conn.rollback();
		}
	}

	@Test
	void testEliminarRolUsuarioConJSON() throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			EliminarRolUsuario servicio = new EliminarRolUsuario();
			Gson gson = new Gson();

			String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, 1L, "PLATAFORMA",
					"PLA.FechaSistema");

			String json = "{\n" +
					"  \"p_IdEmpresa\": 1,\n" +
					"  \"p_IdUsuario\": 3,\n" +
					"  \"p_IdModulo\": \"PLA\",\n" +
					"  \"p_IdUsuarioRol\": 25,\n" +
					"  \"p_IdRol\": 5,\n" +
					"  \"p_IdEmpresaRol\": 1,\n" +
					"  \"p_CodigoUsuario\": \"USUARIO_TEST\",\n" +
					"  \"p_FechaSistema\": \"" + fechaSistema + "\"\n" +
					"}";

			EntradaEliminarRolUsuario entrada = gson.fromJson(json, EntradaEliminarRolUsuario.class);
			JsonObject out = servicio.ejecutar(conn, gson.toJsonTree(entrada).getAsJsonObject());

			assertNotNull(out);
			assertTrue(out.has("p_out_ErroresContador"));
			assertTrue(out.has("p_out_AdvertenciasContador"));

			conn.rollback();
		}
	}
}
