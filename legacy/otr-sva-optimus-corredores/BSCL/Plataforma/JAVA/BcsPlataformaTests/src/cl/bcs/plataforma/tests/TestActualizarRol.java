package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.Connection;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.acc.ActualizarRol;
import cl.bcs.plataforma.acc.ActualizarRol.EntradaActualizarRol;
import cl.bcs.plataforma.acc.ActualizarRol.SalidaActualizarRol;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

class TestActualizarRol {

	@Test
	void testActualizarRol() throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			ActualizarRol demo = new ActualizarRol();

			EntradaActualizarRol entrada = new EntradaActualizarRol();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 1L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdRol = 1L;
			entrada.p_Nombre = "RolActualizado";
			entrada.p_IdEstado = "ACT";

			SalidaActualizarRol salida = demo.logica(conn, entrada);

			assertEquals(1L, salida.p_out_update);
			assertTrue(salida.p_out_ErroresContador >= 0);
			conn.rollback();
		}
	}
}
