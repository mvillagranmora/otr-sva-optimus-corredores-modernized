package cl.bcs.tesoreria;

// to be created in Step 2 — domain model and service interfaces
import cl.bcs.tesoreria.domain.model.ConfiguracionServicioTesoreria;
import cl.bcs.tesoreria.domain.model.EntradaOrquestadorTesoreria;
import cl.bcs.tesoreria.domain.model.SalidaOrquestadorTesoreria;
import cl.bcs.tesoreria.domain.port.out.TesoreriaRepository;
import cl.bcs.tesoreria.domain.port.out.PlataformaPort;
import cl.bcs.tesoreria.domain.service.BcsTesoreraOrchestrator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Characterization tests for BcsTesoreraOrchestrator (T-07).
 *
 * Oracle pattern: same as CentralizacionOrchestratorTest (Phase 1 reference).
 * All 62 legacy services implement JavaCall → will be mapped to BcsTesoreraService
 * beans dispatched by name from this orchestrator.
 *
 * Rules pinned:
 *   B-01  Orchestrator dispatches to the correct bean by name and accumulates
 *         results from each successful service call.
 *   B-03  An exception from a single service increments erroresContador by exactly
 *         1 and does NOT abort the batch (remaining services still execute).
 *   B-06  simulaOperacion is propagated verbatim into each service call; no writes
 *         occur when it is "S".
 *
 * Note: These tests mirror the Phase 1 CentralizacionOrchestratorTest pattern
 * (see modernized/BcsContabilidadServicios/…/CentralizacionOrchestratorTest.java).
 */
@ExtendWith(MockitoExtension.class)
class TesoreriaOrchestratorTest {

    @Mock
    TesoreriaRepository repository;    // to be created in Step 2

    @Mock
    PlataformaPort plataforma;         // to be created in Step 2

    BcsTesoreraOrchestrator orchestrator; // to be created in Step 2

    @BeforeEach
    void setUp() {
        orchestrator = new BcsTesoreraOrchestrator(plataforma, repository);
    }

    // -----------------------------------------------------------------------
    // B-01: Dispatch to correct bean by name and accumulate results
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-01: orchestrator dispatches to two services by name and accumulates both results")
    void rule_b01_dispatchesByNameAndAccumulatesResults() {
        // Arrange: two known service names mapped to beans
        ConfiguracionServicioTesoreria cfg1 = configServicio("crearIngresoEgreso");
        ConfiguracionServicioTesoreria cfg2 = configServicio("liquidarComprobante");

        when(repository.consultarConfiguracionServicios(List.of("crearIngresoEgreso", "liquidarComprobante")))
                .thenReturn(List.of(cfg1, cfg2));
        when(plataforma.obtenerFechaSistema(1L)).thenReturn("2025-08-06");

        // Both service calls succeed and return one result each
        when(repository.ejecutarServicioTesoreria(eq("crearIngresoEgreso"), any()))
                .thenReturn(List.of(resultadoServicio("crearIngresoEgreso", "OK")));
        when(repository.ejecutarServicioTesoreria(eq("liquidarComprobante"), any()))
                .thenReturn(List.of(resultadoServicio("liquidarComprobante", "LIQ")));

        EntradaOrquestadorTesoreria entrada = new EntradaOrquestadorTesoreria(
                1L, 100L, "TES", "N",
                List.of("crearIngresoEgreso", "liquidarComprobante")
        );

        // Act
        SalidaOrquestadorTesoreria salida = orchestrator.ejecutar(entrada);

        // Assert — B-01
        assertThat(salida.resultados())
                .as("B-01: results from both services must be accumulated")
                .hasSize(2);
        assertThat(salida.erroresContador())
                .as("B-01: no errors when both services succeed")
                .isZero();
    }

    @Test
    @DisplayName("B-01: erroresContador starts at 0 for an empty service list")
    void rule_b01_erroresContadorStartsAtZeroForEmptyInput() {
        when(repository.consultarConfiguracionServicios(List.of())).thenReturn(List.of());
        when(plataforma.obtenerFechaSistema(1L)).thenReturn("2025-08-06");

        EntradaOrquestadorTesoreria entrada = new EntradaOrquestadorTesoreria(
                1L, 100L, "TES", "N", List.of()
        );

        SalidaOrquestadorTesoreria salida = orchestrator.ejecutar(entrada);

        assertThat(salida.erroresContador())
                .as("B-01 / B-03: counter must be 0 when no services are processed")
                .isZero();
        assertThat(salida.resultados()).isEmpty();
    }

    // -----------------------------------------------------------------------
    // B-03: Error counter / batch continuation
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-03: exception from first service increments counter by 1 and second service still executes")
    void rule_b03_singleExceptionIncrementsCounterAndBatchContinues() {
        // Arrange: two services; first throws, second succeeds
        ConfiguracionServicioTesoreria cfg1 = configServicio("servicioQueFalla");
        ConfiguracionServicioTesoreria cfg2 = configServicio("crearIngresoEgreso");

        when(repository.consultarConfiguracionServicios(List.of("servicioQueFalla", "crearIngresoEgreso")))
                .thenReturn(List.of(cfg1, cfg2));
        when(plataforma.obtenerFechaSistema(1L)).thenReturn("2025-08-06");

        // First service throws; second succeeds
        when(repository.ejecutarServicioTesoreria(eq("servicioQueFalla"), any()))
                .thenThrow(new RuntimeException("Simulated service failure"));
        when(repository.ejecutarServicioTesoreria(eq("crearIngresoEgreso"), any()))
                .thenReturn(List.of(resultadoServicio("crearIngresoEgreso", "OK")));

        EntradaOrquestadorTesoreria entrada = new EntradaOrquestadorTesoreria(
                1L, 100L, "TES", "N",
                List.of("servicioQueFalla", "crearIngresoEgreso")
        );

        // Act
        SalidaOrquestadorTesoreria salida = orchestrator.ejecutar(entrada);

        // Assert — B-03
        assertThat(salida.erroresContador())
                .as("B-03: exactly one failed service must increment counter by 1")
                .isEqualTo(1L);
        assertThat(salida.resultados())
                .as("B-03: second service still contributed results despite first failing")
                .hasSize(1);
    }

    // -----------------------------------------------------------------------
    // B-06: simulaOperacion flag propagation
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-06: simulaOperacion=S is propagated verbatim to every service call")
    void rule_b06_simulaOperacionPropagatedToEachService() {
        ConfiguracionServicioTesoreria cfg = configServicio("crearIngresoEgreso");

        when(repository.consultarConfiguracionServicios(List.of("crearIngresoEgreso")))
                .thenReturn(List.of(cfg));
        // obtenerFechaSistema is NOT stubbed: H6 scopes it inside simulation guard, so it is
        // never called when simulaOperacion="S"
        when(repository.ejecutarServicioTesoreria(eq("crearIngresoEgreso"), any()))
                .thenReturn(List.of());

        EntradaOrquestadorTesoreria entrada = new EntradaOrquestadorTesoreria(
                1L, 100L, "TES",
                "S",   // simulaOperacion
                List.of("crearIngresoEgreso")
        );

        orchestrator.ejecutar(entrada);

        // B-06: the service must have been invoked with simulaOperacion="S"
        verify(repository, atLeastOnce())
                .ejecutarServicioTesoreria(eq("crearIngresoEgreso"),
                        argThat(ctx -> "S".equals(ctx.simulaOperacion())));
    }

    @Test
    @DisplayName("B-06: simulaOperacion=S does not suppress result computation — resultados still populated")
    void rule_b06_simulationStillComputesResultados() {
        ConfiguracionServicioTesoreria cfg = configServicio("liquidarComprobante");

        when(repository.consultarConfiguracionServicios(List.of("liquidarComprobante")))
                .thenReturn(List.of(cfg));
        // obtenerFechaSistema not stubbed: guarded behind simulaOperacion != "S" after H6
        when(repository.ejecutarServicioTesoreria(eq("liquidarComprobante"), any()))
                .thenReturn(List.of(resultadoServicio("liquidarComprobante", "SIM")));

        EntradaOrquestadorTesoreria entrada = new EntradaOrquestadorTesoreria(
                1L, 100L, "TES", "S", List.of("liquidarComprobante")
        );

        SalidaOrquestadorTesoreria salida = orchestrator.ejecutar(entrada);

        // B-06: resultados still present in simulation mode
        assertThat(salida.resultados())
                .as("B-06: simulation mode must still compute and return resultados")
                .isNotEmpty();
    }

    @Test
    @DisplayName("B-06: simulaOperacion=S prevents TesoreriaRepository#persistirResultado from being called")
    void rule_b06_simulationPreventsRepositoryPersistCall() {
        ConfiguracionServicioTesoreria cfg = configServicio("crearIngresoEgreso");

        when(repository.consultarConfiguracionServicios(List.of("crearIngresoEgreso")))
                .thenReturn(List.of(cfg));
        // obtenerFechaSistema not stubbed: guarded behind simulaOperacion != "S" after H6
        when(repository.ejecutarServicioTesoreria(any(), any())).thenReturn(List.of());

        EntradaOrquestadorTesoreria entrada = new EntradaOrquestadorTesoreria(
                1L, 100L, "TES", "S", List.of("crearIngresoEgreso")
        );

        orchestrator.ejecutar(entrada);

        // B-06: no persistence write in simulation mode
        verify(repository, never()).persistirResultado(any());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static ConfiguracionServicioTesoreria configServicio(String beanName) {
        return new ConfiguracionServicioTesoreria(beanName, "J"); // to be created in Step 2
    }

    private static cl.bcs.tesoreria.domain.model.ResultadoServicioTesoreria
    resultadoServicio(String servicio, String resultado) {
        return new cl.bcs.tesoreria.domain.model.ResultadoServicioTesoreria(
                servicio, resultado); // to be created in Step 2
    }
}
