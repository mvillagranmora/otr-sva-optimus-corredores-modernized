
package cl.bcs.ordenes.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.sql.Connection;
import org.junit.jupiter.api.Test;
import cl.bcs.ordenes.CrearLibrosPorDefecto;
import cl.bcs.ordenes.CrearLibrosPorDefecto.Entrada;
import cl.bcs.ordenes.CrearLibrosPorDefecto.Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

public class CrearLibrosPorDefectoTest {

	@Test
	void testLogicaBasica() throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			CrearLibrosPorDefecto servicio = new CrearLibrosPorDefecto();
			Entrada entrada = new Entrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 1L;
			entrada.p_IdModulo = "ORD";
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_IdLibroOrdenes = 123L;
			Salida salida = servicio.logica(conn, entrada);
			assertNotNull(salida);
			conn.rollback();
		}
	}
}
