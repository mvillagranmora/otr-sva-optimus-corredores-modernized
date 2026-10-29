package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.Connection;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.acc.CrearRol;
import cl.bcs.plataforma.acc.CrearRol.EntradaCrearRol;
import cl.bcs.plataforma.acc.CrearRol.SalidaCrearRol;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

class TestCrearRol {

	@Test
	void testCrearRol() throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			CrearRol demo = new CrearRol();

			EntradaCrearRol entrada = new EntradaCrearRol();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 1L;
			entrada.p_IdModulo = "PLA";
			entrada.p_Nombre = "NuevoRol_" + System.currentTimeMillis();
			entrada.p_IdEmpresaRol = 1L;
			entrada.p_IdEstado = "ACT";

			SalidaCrearRol salida = demo.logica(conn, entrada);

			assertTrue(salida.p_out_new_rol_id > 0);
			assertEquals(0L, salida.p_out_ErroresContador);
			conn.rollback();
		}
	}

	@Test
	void testCrearRolRepetido() throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			CrearRol demo = new CrearRol();

			EntradaCrearRol entrada = new EntradaCrearRol();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 1L;
			entrada.p_IdModulo = "PLA";
			entrada.p_Nombre = "Administrador";
			entrada.p_IdEmpresaRol = 1L;
			entrada.p_IdEstado = "ACT";

			SalidaCrearRol salida = demo.logica(conn, entrada);

			assertTrue(salida.p_out_ErroresContador > 0);
			conn.rollback();
		}
	}
}
