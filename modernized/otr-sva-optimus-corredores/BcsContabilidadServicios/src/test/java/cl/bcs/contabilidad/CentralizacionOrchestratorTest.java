package cl.bcs.contabilidad;

import cl.bcs.contabilidad.domain.model.ConfiguracionTipoCentralizacion;
import cl.bcs.contabilidad.domain.model.EmpresaInfo;
import cl.bcs.contabilidad.domain.model.EntradaMultiSeleccion;
import cl.bcs.contabilidad.domain.model.ResultadoCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaMultiSeleccion;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.CentralizacionOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Characterization tests for CentralizacionOrchestrator (replaces
 * CentralizaMultiSeleccion).
 *
 * Behaviours pinned:
 *   B-01  Orchestrator iterates all TipoCentralizacion ids and accumulates
 *         resultados from every successful delegate call.
 *   B-03  P_OUT_ERRORESCONTADOR starts at 0; an exception from a single type
 *         is caught without counting it (legacy) and does NOT abort the batch.
 *   B-05  Types with null Tipo are filtered with a warning message; unknown ids
 *         produce a different warning.
 *   B-06  The simulaOperacion flag is propagated unchanged into each delegate's
 *         EntradaCentralizacion.
 *
 * These tests are marked @Disabled until CentralizacionOrchestrator.ejecutar()
 * is implemented.  The stubs throw UnsupportedOperationException so each test
 * fails loudly (not silently green) in the meantime.
 */
@ExtendWith(MockitoExtension.class)
class CentralizacionOrchestratorTest {

    @Mock
    ContabilidadRepository repository;

    @Mock
    PlataformaPort plataforma;

    CentralizacionOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new CentralizacionOrchestrator(plataforma, repository);
    }

    // -----------------------------------------------------------------------
    // B-01: Iterate and accumulate
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-01: orchestrator iterates two type-J configs and accumulates resultados from both")
    void rule_b01_iteratesTwoJavaTypesAndAccumulatesResultados() {
        // Arrange
        long idTipo1 = 200L;
        long idTipo2 = 210L;

        ConfiguracionTipoCentralizacion cfg1 = configJ(idTipo1, "centralizaAnticipoPactos");
        ConfiguracionTipoCentralizacion cfg2 = configJ(idTipo2, "centralizaCarterasForward");

        when(repository.queryConfiguraciones(List.of(idTipo1, idTipo2)))
                .thenReturn(List.of(cfg1, cfg2));
        when(repository.queryCentralizacionesVigentes(any(), any(), any()))
                .thenReturn(List.of()); // none already posted
        when(plataforma.obtenerLocalizacionSistema(1L)).thenReturn("CL");
        when(plataforma.obtenerFechaSistema(1L)).thenReturn("2025-08-06");
        when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");
        when(plataforma.consultarEmpresa(1L))
                .thenReturn(new EmpresaInfo(1L, 56L, "76000001-1", "BROKER TEST"));

        // The repository will return one movement per type
        var mov1 = tabMovRef(idTipo1, 1001L, 2025L);
        var mov2 = tabMovRef(idTipo2, 1002L, 2025L);
        when(repository.queryMovimientos(eq(1L), eq("2025-08-06"), eq(idTipo1)))
                .thenReturn(List.of(mov1));
        when(repository.queryMovimientos(eq(1L), eq("2025-08-06"), eq(idTipo2)))
                .thenReturn(List.of(mov2));
        when(repository.queryEventosOperacion(any(), any(), any())).thenReturn(List.of());
        when(repository.queryEventosSubyacente(any(), any(), any(), any())).thenReturn(List.of());
        when(repository.queryDatosMovimientoAnticipo(any(), any())).thenReturn(List.of());

        var entrada = new EntradaMultiSeleccion(
                1L, 97L, "CON", "2025-08-06", "2025-08-05",
                2025L, "N", List.of(idTipo1, idTipo2));

        // Act
        SalidaMultiSeleccion salida = orchestrator.ejecutar(entrada);

        // Assert: results from both types are present — B-01
        assertThat(salida.resultados())
                .as("B-01: resultados must contain one entry per type")
                .hasSize(2);
        assertThat(salida.erroresContador())
                .as("B-01: no errors when both types succeed")
                .isZero();
    }

    @Test
    @DisplayName("B-01: orchestrator initialises erroresContador to 0 for an empty type list")
    void rule_b01_erroresContadorStartsAtZeroForEmptyInput() {
        // B-01 / B-03 baseline: empty list returns counter = 0
        when(repository.queryConfiguraciones(List.of())).thenReturn(List.of());
        when(plataforma.obtenerLocalizacionSistema(1L)).thenReturn("CL");
        when(plataforma.obtenerFechaSistema(1L)).thenReturn("2025-08-06");
        when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");
        when(plataforma.consultarEmpresa(1L))
                .thenReturn(new EmpresaInfo(1L, 56L, "76000001-1", "BROKER TEST"));

        var entrada = new EntradaMultiSeleccion(
                1L, 97L, "CON", "2025-08-06", null,
                2025L, "N", List.of());

        SalidaMultiSeleccion salida = orchestrator.ejecutar(entrada);

        assertThat(salida.erroresContador())
                .as("B-01 / B-03: counter must be 0 when no types are processed")
                .isZero();
        assertThat(salida.resultados())
                .as("B-01: empty resultados for empty input")
                .isEmpty();
    }

    // -----------------------------------------------------------------------
    // B-03: Error counter / batch continuation
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-03: exception from first type is not counted and second type is still processed")
    void rule_b03_singleExceptionNotCountedAndBatchContinues() {
        // Arrange: two types; first service throws, second succeeds
        long idTipo1 = 331L; // will throw
        long idTipo2 = 332L; // must still run

        ConfiguracionTipoCentralizacion cfg1 = configJ(idTipo1, "centralizaFails");
        ConfiguracionTipoCentralizacion cfg2 = configJ(idTipo2, "centralizaAnticipoPactos");

        when(repository.queryConfiguraciones(List.of(idTipo1, idTipo2)))
                .thenReturn(List.of(cfg1, cfg2));
        when(repository.queryCentralizacionesVigentes(any(), any(), any()))
                .thenReturn(List.of());
        when(plataforma.obtenerLocalizacionSistema(1L)).thenReturn("CL");
        when(plataforma.obtenerFechaSistema(1L)).thenReturn("2025-08-06");
        when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");
        when(plataforma.consultarEmpresa(1L))
                .thenReturn(new EmpresaInfo(
                        1L, 56L, "76000001-1", "BROKER TEST"));

        // Type 332 succeeds; type 331 bean is unknown and will cause the
        // Spring container to throw (simulating legacy Class.forName failure).
        var mov2 = tabMovRef(idTipo2, 9001L, 2025L);
        when(repository.queryMovimientos(eq(1L), eq("2025-08-06"), eq(idTipo2)))
                .thenReturn(List.of(mov2));
        when(repository.queryEventosOperacion(any(), any(), eq(idTipo2))).thenReturn(List.of());
        when(repository.queryEventosSubyacente(any(), any(), eq(idTipo2), any()))
                .thenReturn(List.of());
        when(repository.queryDatosMovimientoAnticipo(any(), any())).thenReturn(List.of());

        var entrada = new EntradaMultiSeleccion(
                1L, 97L, "CON", "2025-08-06", null,
                2025L, "N", List.of(idTipo1, idTipo2));

        // Act
        SalidaMultiSeleccion salida = orchestrator.ejecutar(entrada);

        // Assert — B-03. Legacy CentralizaMultiSeleccion.java:361-378 catches the
        // exception without counting it (decision 2026-10-14: follow legacy).
        assertThat(salida.erroresContador())
                .as("B-03: a failing type does not increment the counter (legacy)")
                .isZero();
        assertThat(salida.resultados())
                .as("B-03: second type still contributed results despite first failing")
                .hasSize(1);
    }

    // -----------------------------------------------------------------------
    // B-05: Filter unconfigured types
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-05: type with null Tipo is filtered and produces UI_CENTRALIZACION_CONFIGURACION_NO_EXISTE warning")
    void rule_b05_typeWithNullTipoIsFilteredWithWarning() {
        long idTipoUnconfigured = 999L;

        // Repository returns config but with null Tipo (meaning the type exists but
        // has no configured implementation)
        ConfiguracionTipoCentralizacion unconfigured = new ConfiguracionTipoCentralizacion(
                idTipoUnconfigured, "SIN TIPO", null, null, null,
                null, null, // Tipo = null
                null, null, null, null, null);

        when(repository.queryConfiguraciones(List.of(idTipoUnconfigured)))
                .thenReturn(List.of(unconfigured));
        when(plataforma.obtenerLocalizacionSistema(1L)).thenReturn("CL");
        when(plataforma.obtenerFechaSistema(1L)).thenReturn("2025-08-06");
        when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");
        when(plataforma.consultarEmpresa(1L))
                .thenReturn(new EmpresaInfo(
                        1L, 56L, "76000001-1", "BROKER TEST"));

        var entrada = new EntradaMultiSeleccion(
                1L, 97L, "CON", "2025-08-06", null,
                2025L, "N", List.of(idTipoUnconfigured));

        SalidaMultiSeleccion salida = orchestrator.ejecutar(entrada);

        // B-05: must produce a warning message; no results; no errors (warning ≠ error)
        assertThat(salida.mensajes())
                .as("B-05: must contain UI_CENTRALIZACION_CONFIGURACION_NO_EXISTE warning")
                .anyMatch(m -> m.contains("UI_CENTRALIZACION_CONFIGURACION_NO_EXISTE"));
        assertThat(salida.resultados())
                .as("B-05: unconfigured type produces zero accounting entries")
                .isEmpty();
    }

    @Test
    @DisplayName("B-05: unknown IdTipoCentralizacion (not in DB) produces UI_CENTRALIZACION_NO_EXISTE warning")
    void rule_b05_unknownIdProducesNoExisteWarning() {
        long unknownId = 77777L;

        // Repository returns empty list (id not found in configuration table)
        when(repository.queryConfiguraciones(List.of(unknownId)))
                .thenReturn(List.of());
        when(plataforma.obtenerLocalizacionSistema(1L)).thenReturn("CL");
        when(plataforma.obtenerFechaSistema(1L)).thenReturn("2025-08-06");
        when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");
        when(plataforma.consultarEmpresa(1L))
                .thenReturn(new EmpresaInfo(
                        1L, 56L, "76000001-1", "BROKER TEST"));

        var entrada = new EntradaMultiSeleccion(
                1L, 97L, "CON", "2025-08-06", null,
                2025L, "N", List.of(unknownId));

        SalidaMultiSeleccion salida = orchestrator.ejecutar(entrada);

        assertThat(salida.mensajes())
                .as("B-05: must contain UI_CENTRALIZACION_NO_EXISTE warning")
                .anyMatch(m -> m.contains("UI_CENTRALIZACION_NO_EXISTE"));
        assertThat(salida.resultados()).isEmpty();
    }

    @Test
    @DisplayName("B-05: duplicate IdTipoCentralizacion values in input are deduplicated (each runs once)")
    void rule_b05_duplicateIdsAreDeduplicatedBeforeDispatch() {
        // Legacy lines 197-198: if(!idTipoCentralizacion.contains(id.IdTipoCentralizacion))
        long idTipo = 200L;
        ConfiguracionTipoCentralizacion cfg = configJ(idTipo, "centralizaAnticipoPactos");

        when(repository.queryConfiguraciones(List.of(idTipo))).thenReturn(List.of(cfg));
        when(repository.queryCentralizacionesVigentes(any(), any(), any())).thenReturn(List.of());
        when(plataforma.obtenerLocalizacionSistema(1L)).thenReturn("CL");
        when(plataforma.obtenerFechaSistema(1L)).thenReturn("2025-08-06");
        when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");
        when(plataforma.consultarEmpresa(1L))
                .thenReturn(new EmpresaInfo(
                        1L, 56L, "76000001-1", "BROKER TEST"));
        when(repository.queryMovimientos(any(), any(), any())).thenReturn(List.of());
        when(repository.queryEventosOperacion(any(), any(), any())).thenReturn(List.of());
        when(repository.queryEventosSubyacente(any(), any(), any(), any())).thenReturn(List.of());
        when(repository.queryDatosMovimientoAnticipo(any(), any())).thenReturn(List.of());

        // Input contains same id three times
        var entrada = new EntradaMultiSeleccion(
                1L, 97L, "CON", "2025-08-06", null,
                2025L, "N", List.of(idTipo, idTipo, idTipo));

        SalidaMultiSeleccion salida = orchestrator.ejecutar(entrada);

        // The type's movements were queried exactly once (deduplicated)
        verify(repository, times(1)).queryMovimientos(eq(1L), eq("2025-08-06"), eq(idTipo));
    }

    // -----------------------------------------------------------------------
    // B-06: Simulation flag propagation
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-06: when simulaOperacion=S the flag is propagated to every delegate call")
    void rule_b06_simulaOperacionPropagatedToEachDelegate() {
        // B-06 + B-02 cross-check: orchestrator passes simulaOperacion="S"
        // through EntradaCentralizacion; repository writes must not happen.
        long idTipo = 200L;
        ConfiguracionTipoCentralizacion cfg = configJ(idTipo, "centralizaAnticipoPactos");

        when(repository.queryConfiguraciones(List.of(idTipo))).thenReturn(List.of(cfg));
        // Legacy CentralizaMultiSeleccion.java:240: vigentes are only checked on a real run (N) in CL, never in simulation.
        when(plataforma.obtenerLocalizacionSistema(1L)).thenReturn("CL");
        when(plataforma.obtenerFechaSistema(1L)).thenReturn("2025-08-06");
        when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");
        when(plataforma.consultarEmpresa(1L))
                .thenReturn(new EmpresaInfo(
                        1L, 56L, "76000001-1", "BROKER TEST"));
        when(repository.queryMovimientos(any(), any(), any())).thenReturn(List.of());
        when(repository.queryEventosOperacion(any(), any(), any())).thenReturn(List.of());
        when(repository.queryEventosSubyacente(any(), any(), any(), any())).thenReturn(List.of());
        when(repository.queryDatosMovimientoAnticipo(any(), any())).thenReturn(List.of());

        var entrada = new EntradaMultiSeleccion(
                1L, 97L, "CON", "2025-08-06", "2025-08-05",
                2025L, "S", List.of(idTipo)); // <-- simulaOperacion = "S"

        orchestrator.ejecutar(entrada);

        // B-02 + B-06: persistirCentralizacion must NEVER be called in simulation mode
        verify(repository, never())
                .persistirCentralizacion(any(), any(), any(), any(), any(),
                        any(), any(), any(), any());
    }

    @Test
    @DisplayName("B-06: simulation mode does NOT suppress movement computation — resultados are still populated")
    void rule_b06_simulationStillComputesResultados() {
        // Legacy: computation (Java service call) happens even in simulation;
        // only the CentralizaGenerico write step is skipped.
        long idTipo = 200L;
        ConfiguracionTipoCentralizacion cfg = configJ(idTipo, "centralizaAnticipoPactos");

        when(repository.queryConfiguraciones(List.of(idTipo))).thenReturn(List.of(cfg));
        // Legacy CentralizaMultiSeleccion.java:240: vigentes are only checked on a real run (N) in CL, never in simulation.
        when(plataforma.obtenerLocalizacionSistema(1L)).thenReturn("CL");
        when(plataforma.obtenerFechaSistema(1L)).thenReturn("2025-08-06");
        when(plataforma.obtenerMonedaSistema(1L)).thenReturn("CLP");
        when(plataforma.consultarEmpresa(1L))
                .thenReturn(new EmpresaInfo(
                        1L, 56L, "76000001-1", "BROKER TEST"));
        when(plataforma.obtenerDiaAnterior(56L, "2025-08-06")).thenReturn("2025-08-05");

        var mov = tabMovRef(idTipo, 5001L, 2025L);
        when(repository.queryMovimientos(eq(1L), eq("2025-08-06"), eq(idTipo)))
                .thenReturn(List.of(mov));
        when(repository.queryEventosOperacion(any(), any(), any())).thenReturn(List.of());
        when(repository.queryEventosSubyacente(any(), any(), any(), any())).thenReturn(List.of());
        when(repository.queryDatosMovimientoAnticipo(any(), any())).thenReturn(List.of());

        var entrada = new EntradaMultiSeleccion(
                1L, 97L, "CON", "2025-08-06", "2025-08-05",
                2025L, "S", List.of(idTipo));

        SalidaMultiSeleccion salida = orchestrator.ejecutar(entrada);

        // B-06 + B-02: resultados still present, writes never called
        assertThat(salida.resultados())
                .as("B-06: simulation still produces computed resultados")
                .isNotEmpty();
        verify(repository, never())
                .persistirCentralizacion(any(), any(), any(), any(), any(),
                        any(), any(), any(), any());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static ConfiguracionTipoCentralizacion configJ(long id, String beanName) {
        return new ConfiguracionTipoCentralizacion(
                id, "TIPO_" + id, null, null, null,
                beanName, "J", null, null, "N", null, null);
    }

    private static SalidaCentralizacion.TabMovimientoRef
    tabMovRef(long idTipo, long idMov, long ejercicio) {
        return new SalidaCentralizacion.TabMovimientoRef(
                idTipo, idMov, "MANTPAC", 1L, idMov, 1L,
                "2025-08-06", "12345678-9", idMov,
                ejercicio, "08", BigDecimal.valueOf(100_000), "CLP", "V");
    }
}
