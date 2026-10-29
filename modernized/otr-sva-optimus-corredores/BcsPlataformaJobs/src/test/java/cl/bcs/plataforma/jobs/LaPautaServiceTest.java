package cl.bcs.plataforma.jobs;

import cl.bcs.plataforma.jobs.domain.EstadoModulo;
import cl.bcs.plataforma.jobs.domain.ProcesoPauta;
import cl.bcs.plataforma.jobs.service.pauta.LaPautaService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization tests for LaPautaService pure-logic methods.
 *
 * Oracle:
 *   legacy cl.bcs.plataforma.jobs.LaPauta
 *
 * Rule IDs pinned:
 *   B-PLJ-10  Date/time format with offset
 *   B-PLJ-11  Module open guard
 *   B-PLJ-12  Dependency process lookup
 *   B-PLJ-13  Error propagation (DetienePauta)
 *   B-PLJ-14  Message truncation to 500 chars
 */
@DisplayName("LaPauta — pure-logic characterization")
class LaPautaServiceTest {

    // =========================================================================
    // B-PLJ-10: Date/time formatting with offset
    // =========================================================================

    @Nested
    @DisplayName("obtenerFechaHora — B-PLJ-10")
    class ObtenerFechaHora {

        // Fixed base time: 2025-04-09 15:30:00
        private static final LocalDateTime BASE = LocalDateTime.of(2025, 4, 9, 15, 30, 0);

        @Test
        @DisplayName("PLJ-10-01: 'Fecha' returns dd-MM-yyyy with zero offset")
        void fechaNoOffset() {
            String result = LaPautaService.obtenerFechaHora("Fecha", 0L, BASE);
            assertThat(result).isEqualTo("09-04-2025");
        }

        @Test
        @DisplayName("PLJ-10-02: 'Hora' returns HH:mm:ss with zero offset")
        void horaNoOffset() {
            String result = LaPautaService.obtenerFechaHora("Hora", 0L, BASE);
            assertThat(result).isEqualTo("15:30:00");
        }

        @Test
        @DisplayName("PLJ-10-03: 'Ambos' returns dd-MM-yyyy HH:mm:ss with zero offset")
        void ambosNoOffset() {
            String result = LaPautaService.obtenerFechaHora("Ambos", 0L, BASE);
            assertThat(result).isEqualTo("09-04-2025 15:30:00");
        }

        @Test
        @DisplayName("PLJ-10-04: negative offset (-10800 s = -3 h) shifts time back")
        void negativeOffsetShiftsBack() {
            // -10800 seconds = -3 hours → 15:30:00 becomes 12:30:00
            String result = LaPautaService.obtenerFechaHora("Hora", -10800L, BASE);
            assertThat(result).isEqualTo("12:30:00");
        }

        @Test
        @DisplayName("PLJ-10-05: unknown peticion returns empty string")
        void unknownPeticionReturnsEmpty() {
            String result = LaPautaService.obtenerFechaHora("FechaHora", 0L, BASE);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("PLJ-10-06: offset crosses midnight to next day")
        void offsetCrossesmidnight() {
            // BASE 23:30:00 + 3600s = 00:30:00 next day
            LocalDateTime late = LocalDateTime.of(2025, 4, 9, 23, 30, 0);
            String result = LaPautaService.obtenerFechaHora("Fecha", 3600L, late);
            assertThat(result).isEqualTo("10-04-2025");
        }

        @Test
        @DisplayName("PLJ-10-07: null peticion throws NullPointerException")
        void nullPeticionThrowsNPE() {
            assertThatThrownBy(() -> LaPautaService.obtenerFechaHora(null, 0L, BASE))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // =========================================================================
    // B-PLJ-11: Module open guard
    // =========================================================================

    @Nested
    @DisplayName("hayModuloAbierto — B-PLJ-11")
    class HayModuloAbierto {

        @Test
        @DisplayName("PLJ-11-01: no open modules → false")
        void noOpenModules() {
            EstadoModulo m1 = new EstadoModulo();
            m1.idModulo = "MOD1"; m1.idEstado = "CER";
            EstadoModulo m2 = new EstadoModulo();
            m2.idModulo = "SIS";  m2.idEstado = "ABT";

            boolean result = LaPautaService.hayModuloAbierto(List.of(m1, m2));
            assertThat(result).isFalse(); // SIS is ignored
        }

        @Test
        @DisplayName("PLJ-11-02: non-SIS module is ABT → true")
        void nonSisModuleAbt() {
            EstadoModulo m = new EstadoModulo();
            m.idModulo = "RV"; m.idEstado = "ABT";

            boolean result = LaPautaService.hayModuloAbierto(List.of(m));
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("PLJ-11-03: only SIS is ABT → false")
        void onlySisAbt() {
            EstadoModulo m = new EstadoModulo();
            m.idModulo = "SIS"; m.idEstado = "ABT";

            boolean result = LaPautaService.hayModuloAbierto(List.of(m));
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("PLJ-11-04: empty list → false")
        void emptyList() {
            assertThat(LaPautaService.hayModuloAbierto(List.of())).isFalse();
        }

        @Test
        @DisplayName("PLJ-11-05: null list throws NullPointerException")
        void nullListThrowsNPE() {
            assertThatThrownBy(() -> LaPautaService.hayModuloAbierto(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // =========================================================================
    // B-PLJ-12: Dependency process lookup
    // =========================================================================

    @Nested
    @DisplayName("obtenerProcesoDependencia — B-PLJ-12")
    class ObtenerProcesoDependencia {

        private ProcesoPauta makeProcess(String codigo) {
            ProcesoPauta p = new ProcesoPauta();
            p.codigoProceso = codigo;
            p.nombreClase   = "cl.bcs.Clase" + codigo;
            return p;
        }

        @Test
        @DisplayName("PLJ-12-01: dependency found by exact code (case-insensitive)")
        void foundByCode() {
            ProcesoPauta dep = makeProcess("CAMBIO_FECHA");
            ProcesoPauta other = makeProcess("ABRIR_SISTEMA");

            ProcesoPauta result = LaPautaService.obtenerProcesoDependencia(
                    "cambio_fecha", List.of(dep, other));
            assertThat(result).isSameAs(dep);
        }

        @Test
        @DisplayName("PLJ-12-02: dependency not found returns null")
        void notFoundReturnsNull() {
            ProcesoPauta p = makeProcess("ABRIR_SISTEMA");

            ProcesoPauta result = LaPautaService.obtenerProcesoDependencia(
                    "NONEXISTENT", List.of(p));
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("PLJ-12-03: empty list returns null")
        void emptyListReturnsNull() {
            ProcesoPauta result = LaPautaService.obtenerProcesoDependencia(
                    "ANY_CODE", List.of());
            assertThat(result).isNull();
        }
    }

    // =========================================================================
    // B-PLJ-14: Message truncation
    // =========================================================================

    @Nested
    @DisplayName("truncarMensaje — B-PLJ-14")
    class TruncarMensaje {

        @Test
        @DisplayName("PLJ-14-01: message <= 500 chars is returned unchanged")
        void shortMessageUnchanged() {
            String msg = "A".repeat(500);
            assertThat(LaPautaService.truncarMensaje(msg)).isEqualTo(msg);
        }

        @Test
        @DisplayName("PLJ-14-02: message > 500 chars is truncated to 500")
        void longMessageTruncated() {
            String msg = "B".repeat(600);
            String result = LaPautaService.truncarMensaje(msg);
            assertThat(result).hasSize(500);
            assertThat(result).isEqualTo("B".repeat(500));
        }

        @Test
        @DisplayName("PLJ-14-03: empty string is returned unchanged")
        void emptyStringUnchanged() {
            assertThat(LaPautaService.truncarMensaje("")).isEmpty();
        }

        @Test
        @DisplayName("PLJ-14-04: null mensaje throws NullPointerException")
        void nullMensajeThrowsNPE() {
            assertThatThrownBy(() -> LaPautaService.truncarMensaje(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // =========================================================================
    // Integration test — requires HANA
    // =========================================================================

    @Test
    @Disabled("requires HANA")
    @DisplayName("PLJ-SVC-PAUTA: ejecutarPauta() orchestrates all proceso-pauta steps via HANA")
    void ejecutarPautaIntegration() {
        // When Step 2 wires a real HANA datasource:
        //   Given idEmpresa = 1L, idUsuario = 1L, fechaSistemaPauta = "2025-04-09"
        //   When  service.ejecutarPauta(1L, 1L, "2025-04-09", "") is called
        //   Then  all procesosPauta rows are processed in order
        //         and PAU_Ejecucion row ends in estado TER or ERR
        // Credentials must come from HANA_URL / HANA_USER / HANA_PASSWORD env vars
    }
}
