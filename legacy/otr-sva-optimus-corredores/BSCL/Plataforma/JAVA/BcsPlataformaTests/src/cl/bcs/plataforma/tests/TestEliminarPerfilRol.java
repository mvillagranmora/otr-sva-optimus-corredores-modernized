package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.Connection;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.acc.EliminarPerfilRol;
import cl.bcs.plataforma.acc.EliminarPerfilRol.EntradaEliminarPerfilRol;
import cl.bcs.plataforma.acc.EliminarPerfilRol.SalidaEliminarPerfilRol;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

class TestEliminarPerfilRol {

	@Test
	void testEliminarPerfilRol() throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			EliminarPerfilRol demo = new EliminarPerfilRol();

			EntradaEliminarPerfilRol entrada = new EntradaEliminarPerfilRol();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 1L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdRolPerfil = 1L;

			SalidaEliminarPerfilRol salida = demo.logica(conn, entrada);

			assertTrue(salida.p_out_ErroresContador >= 0);
			conn.rollback();
		}
	}
}
