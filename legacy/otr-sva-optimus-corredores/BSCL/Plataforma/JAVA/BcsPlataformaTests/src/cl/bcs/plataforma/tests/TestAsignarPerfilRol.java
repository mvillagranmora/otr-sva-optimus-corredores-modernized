package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.sql.Connection;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.acc.AsignarPerfilRol;
import cl.bcs.plataforma.acc.AsignarPerfilRol.EntradaAsignarPerfilRol;
import cl.bcs.plataforma.acc.AsignarPerfilRol.SalidaAsignarPerfilRol;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

class TestAsignarPerfilRol {

	@Test
	void testAsignarPerfilRol() throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			AsignarPerfilRol demo = new AsignarPerfilRol();

			EntradaAsignarPerfilRol entrada = new EntradaAsignarPerfilRol();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 1L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdPerfil = 1L;
			entrada.p_IdRol = 1L;
			entrada.p_IdEstado = "ACT";

			SalidaAsignarPerfilRol salida = demo.logica(conn, entrada);

			assertTrue(salida.p_IdRolPerfil > 0 || salida.p_out_ErroresContador > 0);
			conn.rollback();
		}
	}
}
