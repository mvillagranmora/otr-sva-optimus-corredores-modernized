package cl.bcs.facturacion;

import cl.bcs.facturacion.service.FacturacionServiciosService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-logic unit tests for FacturacionServiciosService.
 * No Spring context — fast, no HANA dependency.
 */
class FacturacionServiciosServiceTest {

    // ---------------------------------------------------------
    // esTipoFactura
    // ---------------------------------------------------------

    @Test
    void esTipoFactura_FAC_returnsTrue() {
        assertThat(FacturacionServiciosService.esTipoFactura("FAC")).isTrue();
    }

    @Test
    void esTipoFactura_NCE_returnsFalse() {
        assertThat(FacturacionServiciosService.esTipoFactura("NCE")).isFalse();
    }

    @Test
    void esTipoFactura_UNKNOWN_returnsFalse() {
        assertThat(FacturacionServiciosService.esTipoFactura("UNKNOWN")).isFalse();
    }

    // ---------------------------------------------------------
    // esTipoNotaCredito
    // ---------------------------------------------------------

    @Test
    void esTipoNotaCredito_NCE_returnsTrue() {
        assertThat(FacturacionServiciosService.esTipoNotaCredito("NCE")).isTrue();
    }

    @Test
    void esTipoNotaCredito_NCR_returnsTrue() {
        assertThat(FacturacionServiciosService.esTipoNotaCredito("NCR")).isTrue();
    }

    @Test
    void esTipoNotaCredito_FAC_returnsFalse() {
        assertThat(FacturacionServiciosService.esTipoNotaCredito("FAC")).isFalse();
    }

    // ---------------------------------------------------------
    // esTipoAnticipoSimultanea
    // ---------------------------------------------------------

    @Test
    void esTipoAnticipoSimultanea_ANSIMV_returnsTrue() {
        assertThat(FacturacionServiciosService.esTipoAnticipoSimultanea("ANSIMV")).isTrue();
    }

    @Test
    void esTipoAnticipoSimultanea_ANSIMC_returnsTrue() {
        assertThat(FacturacionServiciosService.esTipoAnticipoSimultanea("ANSIMC")).isTrue();
    }

    @Test
    void esTipoAnticipoSimultanea_FAC_returnsFalse() {
        assertThat(FacturacionServiciosService.esTipoAnticipoSimultanea("FAC")).isFalse();
    }

    // ---------------------------------------------------------
    // List sizes
    // ---------------------------------------------------------

    @Test
    void tiposFactura_sizeIs9() {
        assertThat(FacturacionServiciosService.TIPOS_FACTURA).hasSize(9);
    }

    @Test
    void tiposNotaCredito_sizeIs3() {
        assertThat(FacturacionServiciosService.TIPOS_NOTA_CREDITO).hasSize(3);
    }

    @Test
    void tiposAnticipoSimultanea_sizeIs2() {
        assertThat(FacturacionServiciosService.TIPOS_ANTICIPO_SIMULTANEA).hasSize(2);
    }

    // ---------------------------------------------------------
    // All 9 tipos factura individually
    // ---------------------------------------------------------

    @Test
    void tiposFactura_containsFAC() {
        assertThat(FacturacionServiciosService.TIPOS_FACTURA).contains("FAC");
    }

    @Test
    void tiposFactura_containsFAE() {
        assertThat(FacturacionServiciosService.TIPOS_FACTURA).contains("FAE");
    }

    @Test
    void tiposFactura_containsBOE() {
        assertThat(FacturacionServiciosService.TIPOS_FACTURA).contains("BOE");
    }

    @Test
    void tiposFactura_containsFOE() {
        assertThat(FacturacionServiciosService.TIPOS_FACTURA).contains("FOE");
    }

    @Test
    void tiposFactura_containsFOC() {
        assertThat(FacturacionServiciosService.TIPOS_FACTURA).contains("FOC");
    }

    @Test
    void tiposFactura_containsFEE() {
        assertThat(FacturacionServiciosService.TIPOS_FACTURA).contains("FEE");
    }

    @Test
    void tiposFactura_containsFOX() {
        assertThat(FacturacionServiciosService.TIPOS_FACTURA).contains("FOX");
    }

    @Test
    void tiposFactura_containsPOL() {
        assertThat(FacturacionServiciosService.TIPOS_FACTURA).contains("POL");
    }

    @Test
    void tiposFactura_containsBEE() {
        assertThat(FacturacionServiciosService.TIPOS_FACTURA).contains("BEE");
    }

    // ---------------------------------------------------------
    // All 3 tipos nota credito individually
    // ---------------------------------------------------------

    @Test
    void tiposNotaCredito_containsNCE() {
        assertThat(FacturacionServiciosService.TIPOS_NOTA_CREDITO).contains("NCE");
    }

    @Test
    void tiposNotaCredito_containsNCR() {
        assertThat(FacturacionServiciosService.TIPOS_NOTA_CREDITO).contains("NCR");
    }

    @Test
    void tiposNotaCredito_containsNCO() {
        assertThat(FacturacionServiciosService.TIPOS_NOTA_CREDITO).contains("NCO");
    }
}
