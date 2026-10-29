
package cl.bcs.ordenes.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.sql.Connection;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import cl.bcs.ordenes.CrearLibroOrdenes;
import cl.bcs.ordenes.CrearLibroOrdenes.Entrada;
import cl.bcs.ordenes.CrearLibroOrdenes.Salida;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;

public class CrearLibroOrdenesTest {

	@Test
	void testLogicaBasica() throws Exception {
		try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
			CrearLibroOrdenes servicio = new CrearLibroOrdenes();
			Entrada entrada = new Entrada();
			entrada.p_IdEmpresa = 1L;
			entrada.p_IdUsuario = 1L;
			entrada.p_IdModulo = "ORD";
			entrada.p_UsaReglasNegocio = "N";
			entrada.p_NombreLibroOrdenes = "Libro Test";
			entrada.p_IdTipoLibroOrden = "TIPO1";
			entrada.p_IdEstado = "ACT";
			entrada.p_AsignacionAutomatica = "S";
			entrada.p_IdTipoAsignacion = "TIPOA";
			entrada.p_Prioridad = 1L;
			entrada.p_Precio = 1000L;
			entrada.p_Bolsa = "BOLSA1";
			entrada.p_TipoSubasta = "SUBASTA";
			entrada.p_IdInstrumento = 10L;
			entrada.p_IdClienteOfertante = 20L;
			entrada.p_Cantidad = new BigDecimal("100.00");
			entrada.p_IdPlantillaCobros = "PLANTILLA1";
			entrada.p_FechaApertura = "2026-01-27";
			entrada.p_FechaCierre = "2026-01-28";
			Salida salida = servicio.logica(conn, entrada);
			assertNotNull(salida);
			conn.rollback();
		}
	}
}
