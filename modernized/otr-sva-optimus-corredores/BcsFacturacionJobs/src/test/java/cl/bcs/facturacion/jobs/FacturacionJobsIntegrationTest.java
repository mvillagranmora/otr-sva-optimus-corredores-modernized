package cl.bcs.facturacion.jobs;

import cl.bcs.facturacion.jobs.domain.ResumenFacturacionCliente;
import cl.bcs.facturacion.jobs.port.out.FacturacionJobsDataPort;
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
 * Integration tests for the facturacion-jobs data port using an H2 in-memory database.
 *
 * <p>HANA stored-procedure tests are {@code @Disabled} because they require a live
 * SAP HANA environment with the corresponding stored procedures installed.</p>
 *
 * <p>Equivalence cases executed (enabled): 4</p>
 * <p>Equivalence cases disabled (HANA SP): 2</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("FacturacionJobs Integration — H2 adapter")
class FacturacionJobsIntegrationTest {

    @Autowired
    private FacturacionJobsDataPort dataPort;

    // =========================================================================
    // obtenerFechaSistema
    // =========================================================================

    @Test
    @DisplayName("IT-FAJ-01: obtenerFechaSistema returns configured value for empresa 1")
    void obtenerFechaSistema_returnsConfiguredValue() {
        String fecha = dataPort.obtenerFechaSistema(1L);
        assertThat(fecha).isEqualTo("2024-01-15");
    }

    // =========================================================================
    // consultarResumenFacturas
    // =========================================================================

    @Test
    @DisplayName("IT-FAJ-02: full date range returns all 3 test records")
    void consultarResumenFacturas_fullRange_returnsThreeRows() {
        List<ResumenFacturacionCliente> result =
                dataPort.consultarResumenFacturas(1L, "2024-01-10", "2024-01-15");
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("IT-FAJ-03: narrow date range returns only 1 record (2024-01-12)")
    void consultarResumenFacturas_narrowRange_returnsOneRow() {
        List<ResumenFacturacionCliente> result =
                dataPort.consultarResumenFacturas(1L, "2024-01-11", "2024-01-12");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).idCliente()).isEqualTo(101L);
    }

    @Test
    @DisplayName("IT-FAJ-04: unknown empresa returns empty list")
    void consultarResumenFacturas_unknownEmpresa_returnsEmpty() {
        List<ResumenFacturacionCliente> result =
                dataPort.consultarResumenFacturas(999L, "2024-01-01", "2024-12-31");
        assertThat(result).isEmpty();
    }

    // =========================================================================
    // HANA SP operations — disabled (require live HANA environment)
    // =========================================================================

    @Test
    @Disabled("requires HANA SP: SP_EMISION_MASIVA_COMPROBANTE")
    @DisplayName("IT-FAJ-05 [DISABLED]: emitirComprobantesMasivos — requires HANA SP")
    void emitirComprobantesMasivos_requiresHanaSP() {
        dataPort.emitirComprobantesMasivos(1L, 1L);
    }

    @Test
    @Disabled("requires HANA SP: SP_GENERAR_PREFACTURAS_MASIVAS")
    @DisplayName("IT-FAJ-06 [DISABLED]: generarPrefacturasMasivas — requires HANA SP")
    void generarPrefacturasMasivas_requiresHanaSP() {
        dataPort.generarPrefacturasMasivas(1L, 1L);
    }
}
