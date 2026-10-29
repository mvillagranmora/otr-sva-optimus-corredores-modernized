package cl.bcs.custodia;

import cl.bcs.custodia.domain.PosicionCustodia;
import cl.bcs.custodia.port.out.CustodiaServiciosDataPort;
import cl.bcs.custodia.service.CustodiaServiciosService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for BcsCustodiaServicios backed by H2 in-memory database.
 *
 * Schema: schema-custodia-servicios.sql
 * Data:   data-custodia-servicios.sql
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CustodiaServiciosIntegrationTest {

    @Autowired
    private CustodiaServiciosDataPort dataPort;

    // -----------------------------------------------------------------------
    // Pure-logic via service (no DB call needed)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TIPO_BUSQUEDAS constant has 15 elements")
    void test_getTipoBusquedas_size() {
        assertThat(CustodiaServiciosService.TIPO_BUSQUEDAS).hasSize(15);
    }

    @Test
    @DisplayName("getHashBusqueda returns correct composite key")
    void test_getHashBusqueda() {
        String hash = CustodiaServiciosService.getHashBusqueda(1L, 2L, 3L, "ABC");
        assertThat(hash).isEqualTo("1-2-3-ABC");
    }

    // -----------------------------------------------------------------------
    // Data port — plain SQL (H2-testable)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("consultarPosicion(101) returns position with nemotecnico CFIPGTPC-E")
    void test_consultarPosicion() {
        List<PosicionCustodia> posiciones = dataPort.consultarPosicion(101L);
        assertThat(posiciones).hasSizeGreaterThanOrEqualTo(1);
        assertThat(posiciones.get(0).nemotecnico()).isEqualTo("CFIPGTPC-E");
        assertThat(posiciones.get(0).cantidad()).isNotNull();
    }

    @Test
    @DisplayName("obtenerFechaSistema(1) returns '2024-01-15' from H2 (PLA.FechaSistema key)")
    void test_obtenerFechaSistema() {
        // H2 test data: CodigoParametro='PLA.FechaSistema' → Valor='2024-01-15'
        // HANA QA confirmed: same key returns '2026-09-14' (QA business date)
        String fecha = dataPort.obtenerFechaSistema(1L);
        assertThat(fecha).isEqualTo("2024-01-15");
    }

    // -----------------------------------------------------------------------
    // UnsupportedOperationException guards (HANA SP operations)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("liquidarEmprestito throws UnsupportedOperationException with SP name")
    void test_liquidarEmprestito_throwsUOE() {
        assertThatThrownBy(() -> dataPort.liquidarEmprestito(1L, 101L, 201L))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("SP_LIQUIDAR_CUSTODIA_EMPRESTITO");
    }

    @Test
    @DisplayName("anticiparPrestamo throws UnsupportedOperationException with SP name")
    void test_anticiparPrestamo_throwsUOE() {
        assertThatThrownBy(() -> dataPort.anticiparPrestamo(1L, 101L, "PRTVTC"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("SP_ANTICIPAR_PRESTAMO");
    }

    @Test
    @DisplayName("guardarPatrimonio throws UnsupportedOperationException with SP name")
    void test_guardarPatrimonio_throwsUOE() {
        assertThatThrownBy(() -> dataPort.guardarPatrimonio(1L, "2024-01-01", "2024-01-31"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("SP_GUARDAR_PATRIMONIO_RETROACTIVO");
    }

    // -----------------------------------------------------------------------
    // HANA SP endpoints — @Disabled (require HANA stored procedures)
    // -----------------------------------------------------------------------

    @Test
    @Disabled("requires HANA SP: SP_LIQUIDAR_CUSTODIA_EMPRESTITO — not available in H2")
    @DisplayName("[HANA] liquidarEmprestito")
    void test_liquidarEmprestito_hana() {
        dataPort.liquidarEmprestito(1L, 101L, 201L);
    }

    @Test
    @Disabled("requires HANA SP: SP_ANTICIPAR_PRESTAMO — not available in H2")
    @DisplayName("[HANA] anticiparPrestamo")
    void test_anticiparPrestamo_hana() {
        dataPort.anticiparPrestamo(1L, 101L, "PRTVTC");
    }

    @Test
    @Disabled("requires HANA SP: SP_GUARDAR_PATRIMONIO_RETROACTIVO — not available in H2")
    @DisplayName("[HANA] guardarPatrimonio")
    void test_guardarPatrimonio_hana() {
        dataPort.guardarPatrimonio(1L, "2024-01-01", "2024-01-31");
    }
}
