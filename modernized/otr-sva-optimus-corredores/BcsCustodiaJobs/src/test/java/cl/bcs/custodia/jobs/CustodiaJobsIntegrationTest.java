package cl.bcs.custodia.jobs;

import cl.bcs.custodia.jobs.domain.InstrumentoPrecio;
import cl.bcs.custodia.jobs.domain.PosicionCustodia;
import cl.bcs.custodia.jobs.port.out.CustodiaJobsDataPort;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for CustodiaJobsDataPort backed by H2 in-memory database.
 *
 * Schema: schema-custodia-jobs.sql
 * Data:   data-custodia-jobs.sql
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustodiaJobsIntegrationTest {

    @Autowired
    private CustodiaJobsDataPort dataPort;

    // -----------------------------------------------------------------------
    // obtenerFechaSistema
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("obtenerFechaSistema(1) returns '2024-01-15'")
    void test_obtenerFechaSistema() {
        String fecha = dataPort.obtenerFechaSistema(1L);
        assertThat(fecha).isEqualTo("2024-01-15");
    }

    // -----------------------------------------------------------------------
    // consultarPreciosInstrumentos
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("consultarPreciosInstrumentos(1, '2024-01-15') returns 2 instruments")
    void test_consultarPrecios_returnsTwo() {
        List<InstrumentoPrecio> precios = dataPort.consultarPreciosInstrumentos(1L, "2024-01-15");
        assertThat(precios).hasSize(2);
        assertThat(precios).extracting(InstrumentoPrecio::codigoNemo)
                .containsExactlyInAnyOrder("BESAB24", "BCREH25");
    }

    // -----------------------------------------------------------------------
    // actualizarPrecioInstrumento
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("actualizarPrecioInstrumento updates price and re-query confirms new value")
    void test_actualizarPrecio() {
        BigDecimal nuevoPrecio = new BigDecimal("99.9000");
        dataPort.actualizarPrecioInstrumento(1L, nuevoPrecio, "2024-01-15");

        List<InstrumentoPrecio> precios = dataPort.consultarPreciosInstrumentos(1L, "2024-01-15");
        InstrumentoPrecio besab = precios.stream()
                .filter(p -> "BESAB24".equals(p.codigoNemo()))
                .findFirst()
                .orElseThrow();
        // Compare using scale-insensitive compareTo
        assertThat(besab.precio().compareTo(nuevoPrecio)).isZero();
    }

    // -----------------------------------------------------------------------
    // consultarPosicionesCliente
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("consultarPosicionesCliente(1, '2024-01-15') returns 3 positions (2 for cliente 100, 1 for 101)")
    void test_consultarPosiciones_returnsThree() {
        List<PosicionCustodia> posiciones = dataPort.consultarPosicionesCliente(1L, "2024-01-15");
        assertThat(posiciones).hasSize(3);

        long countCliente100 = posiciones.stream().filter(p -> p.idCliente() == 100L).count();
        long countCliente101 = posiciones.stream().filter(p -> p.idCliente() == 101L).count();

        assertThat(countCliente100).isEqualTo(2);
        assertThat(countCliente101).isEqualTo(1);
    }

    // -----------------------------------------------------------------------
    // HANA SP methods — @Disabled
    // -----------------------------------------------------------------------

    @Test
    @Disabled("requires HANA SP: SP_PROCESAR_VENCIMIENTO_PRODUCTOS — not available in H2")
    @DisplayName("[HANA] procesarVencimientoProductos")
    void test_procesarVencimientoProductos_hana() {
        dataPort.procesarVencimientoProductos(1L, "IF", "2024-01-15");
    }

    @Test
    @Disabled("requires HANA SP: SP_TIPIFICAR_INSTRUMENTOS — not available in H2")
    @DisplayName("[HANA] tipificarInstrumentos")
    void test_tipificarInstrumentos_hana() {
        dataPort.tipificarInstrumentos(1L);
    }

    @Test
    @Disabled("requires HANA SP: SP_EJECUTAR_VENCIMIENTO — not available in H2")
    @DisplayName("[HANA] ejecutarVencimiento")
    void test_ejecutarVencimiento_hana() {
        dataPort.ejecutarVencimiento(1L, "2024-01-15");
    }

    // -----------------------------------------------------------------------
    // UnsupportedOperationException guard (even without @Disabled)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("procesarVencimientoProductos throws UnsupportedOperationException with SP name")
    void test_procesarVencimiento_throwsUOE() {
        assertThatThrownBy(() -> dataPort.procesarVencimientoProductos(1L, "IF", "2024-01-15"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("SP_PROCESAR_VENCIMIENTO_PRODUCTOS");
    }

    @Test
    @DisplayName("tipificarInstrumentos throws UnsupportedOperationException with SP name")
    void test_tipificar_throwsUOE() {
        assertThatThrownBy(() -> dataPort.tipificarInstrumentos(1L))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("SP_TIPIFICAR_INSTRUMENTOS");
    }

    @Test
    @DisplayName("ejecutarVencimiento throws UnsupportedOperationException with SP name")
    void test_ejecutarVencimiento_throwsUOE() {
        assertThatThrownBy(() -> dataPort.ejecutarVencimiento(1L, "2024-01-15"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("SP_EJECUTAR_VENCIMIENTO");
    }
}
