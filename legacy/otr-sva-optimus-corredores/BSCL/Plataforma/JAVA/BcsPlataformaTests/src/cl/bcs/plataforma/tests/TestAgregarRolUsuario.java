package cl.bcs.plataforma.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

import cl.bcs.plataforma.acc.AgregarRolUsuario;
import cl.bcs.plataforma.acc.AgregarRolUsuario.EntradaAgregarRolUsuario;
import cl.bcs.plataforma.acc.AgregarRolUsuario.SalidaAgregarRolUsuario;
import cl.bcs.plataforma.pla.utils.ConfiguracionSistema;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

class TestAgregarRolUsuario {

	@Test
	void testAgregarRolUsuario() throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			AgregarRolUsuario servicio = new AgregarRolUsuario();

			EntradaAgregarRolUsuario entrada = new EntradaAgregarRolUsuario();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 1L;
			entrada.p_IdModulo = "PLA";
			entrada.p_IdUsuarioRol = 2L;
			entrada.p_IdRol = 10L;
			entrada.p_IdEmpresaRol = 1L;
			entrada.p_CodigoUsuario = "SISTEMA";
			entrada.p_FechaSistema = ConfiguracionSistema.getParametroConfiguracionString(conn, 1L, "PLATAFORMA", "PLA.FechaSistema");

			SalidaAgregarRolUsuario salida = servicio.logica(conn, entrada);

			assertTrue(salida.p_out_ErroresContador.equals(0));
			conn.rollback();
		}
	}
}
