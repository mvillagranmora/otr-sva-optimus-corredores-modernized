package cl.bcs.normativa.svc.service;

import cl.bcs.normativa.svc.service.ils.IlsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests de caracterización de la lógica pura del servicio ILS.
 * No requieren Spring.
 */
class IlsServiceTest {

    @Test
    @DisplayName("calcularFechaHasta suma 7 días a la fecha contable")
    void calcularFechaHasta_suma7Dias() {
        String resultado = IlsService.calcularFechaHasta("2018-04-04");
        assertThat(resultado).isEqualTo("2018-04-11");
    }

    @Test
    @DisplayName("calcularFechaHasta maneja fin de mes correctamente")
    void calcularFechaHasta_finDeMes() {
        String resultado = IlsService.calcularFechaHasta("2023-01-28");
        assertThat(resultado).isEqualTo("2023-02-04");
    }

    @Test
    @DisplayName("calcularFechaHasta maneja cambio de año")
    void calcularFechaHasta_cambioAnio() {
        String resultado = IlsService.calcularFechaHasta("2022-12-30");
        assertThat(resultado).isEqualTo("2023-01-06");
    }

    @Test
    @DisplayName("calcularFechaHasta lanza IAE con fecha null")
    void calcularFechaHasta_fechaNull_lanzaIAE() {
        assertThatThrownBy(() -> IlsService.calcularFechaHasta(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("calcularFechaHasta lanza IAE con fecha vacía")
    void calcularFechaHasta_fechaVacia_lanzaIAE() {
        assertThatThrownBy(() -> IlsService.calcularFechaHasta(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
