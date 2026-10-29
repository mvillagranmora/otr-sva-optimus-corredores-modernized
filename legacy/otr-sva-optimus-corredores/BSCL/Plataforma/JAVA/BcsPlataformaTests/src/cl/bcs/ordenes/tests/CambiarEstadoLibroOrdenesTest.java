
package cl.bcs.ordenes.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.sql.Connection;
import org.junit.jupiter.api.Test;
import cl.bcs.ordenes.CambiarEstadoLibroOrdenes;
import cl.bcs.ordenes.CambiarEstadoLibroOrdenes.Entrada;
import cl.bcs.ordenes.CambiarEstadoLibroOrdenes.Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

public class CambiarEstadoLibroOrdenesTest {

	@Test
	void testLogicaBasica() throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			CambiarEstadoLibroOrdenes servicio = new CambiarEstadoLibroOrdenes();
			Entrada entrada = new Entrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 1L;
			entrada.p_IdModulo = "ORD";
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdLibroOrdenes = 123L;
			entrada.p_IdEstado = "ACT";
			entrada.p_IdTipo = "TIPO";
			entrada.p_Fecha = "2026-01-27";
			entrada.p_Comentario = "Comentario de prueba";
			Salida salida = servicio.logica(conn, entrada);
			assertNotNull(salida);
			conn.rollback();
		}
	}
}
