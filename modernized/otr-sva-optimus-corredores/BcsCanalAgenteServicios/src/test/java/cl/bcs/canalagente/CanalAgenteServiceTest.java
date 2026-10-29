package cl.bcs.canalagente;

import cl.bcs.canalagente.domain.HistorialMovimientosResponse;
import cl.bcs.canalagente.service.CanalAgenteService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-logic unit tests for CanalAgenteService static helpers.
 * No Spring context — fast, no HANA dependency.
 */
class CanalAgenteServiceTest {

    // ---------------------------------------------------------
    // buildClienteNoEncontradoResponse
    // ---------------------------------------------------------

    @Test
    void buildClienteNoEncontradoResponse_erroresContadorIs1() {
        HistorialMovimientosResponse resp = CanalAgenteService.buildClienteNoEncontradoResponse();
        assertThat(resp.erroresContador()).isEqualTo(1L);
    }

    @Test
    void buildClienteNoEncontradoResponse_erroresDetalleHasOneEntry() {
        HistorialMovimientosResponse resp = CanalAgenteService.buildClienteNoEncontradoResponse();
        assertThat(resp.erroresDetalle()).hasSize(1);
    }

    @Test
    void buildClienteNoEncontradoResponse_errorDescripcionMatchesLegacy() {
        HistorialMovimientosResponse resp = CanalAgenteService.buildClienteNoEncontradoResponse();
        assertThat(resp.erroresDetalle().get(0).descripcion()).isEqualTo("Cliente no encontrado.");
    }

    @Test
    void buildClienteNoEncontradoResponse_resultadoIsEmpty() {
        HistorialMovimientosResponse resp = CanalAgenteService.buildClienteNoEncontradoResponse();
        assertThat(resp.resultado()).isEmpty();
    }

    @Test
    void buildClienteNoEncontradoResponse_advertenciasContadorIsZero() {
        HistorialMovimientosResponse resp = CanalAgenteService.buildClienteNoEncontradoResponse();
        assertThat(resp.advertenciasContador()).isEqualTo(0L);
    }

    // ---------------------------------------------------------
    // HistorialMovimientosResponse.empty()
    // ---------------------------------------------------------

    @Test
    void emptyResponse_erroresContadorIsZero() {
        HistorialMovimientosResponse resp = HistorialMovimientosResponse.empty();
        assertThat(resp.erroresContador()).isEqualTo(0L);
    }

    @Test
    void emptyResponse_resultadoIsEmpty() {
        HistorialMovimientosResponse resp = HistorialMovimientosResponse.empty();
        assertThat(resp.resultado()).isEmpty();
    }

    @Test
    void emptyResponse_advertenciasIsEmpty() {
        HistorialMovimientosResponse resp = HistorialMovimientosResponse.empty();
        assertThat(resp.advertenciasDetalle()).isEmpty();
    }
}
