package cl.bcs.plataforma.jobs;

import cl.bcs.plataforma.jobs.service.parametros.ActualizarParametrosFinancierosService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization tests for ActualizarParametrosFinancierosService pure-logic methods.
 *
 * Oracle:
 *   legacy cl.bcs.plataforma.jobs.ActualizarParametrosFinancieros
 *
 * Rule IDs pinned:
 *   B-PLJ-01  IPC date correction: subtract 1 month, set day 1
 *   B-PLJ-02  Dolar observado date shift
 *   B-PLJ-03  Paridad USD = rawValor / dolarObservado (8 dp FLOOR)
 *   B-PLJ-04  Non-trading-day fill: gap > 1 calendar day
 */
@DisplayName("ActualizarParametrosFinancieros — pure-logic characterization")
class ActualizarParametrosFinancierosServiceTest {

    // =========================================================================
    // B-PLJ-01: IPC date correction
    // =========================================================================

    @Nested
    @DisplayName("IPC date correction — B-PLJ-01")
    class IPCDateCorrection {

        @Test
        @DisplayName("PLJ-01-01: April IPC → March (subtract 1 month, day 1)")
        void aprilIpcBecomesMarc() {
            // B-PLJ-01: Bursatil delivers April → corrected to March 1
            String result = ActualizarParametrosFinancierosService.corregirFechaIPC("2025-04-01");
            assertThat(result).isEqualTo("2025-03-01");
        }

        @Test
        @DisplayName("PLJ-01-02: January IPC → December of previous year")
        void januaryIpcBecomesDecemberPreviousYear() {
            // B-PLJ-01: January crosses year boundary
            String result = ActualizarParametrosFinancierosService.corregirFechaIPC("2025-01-01");
            assertThat(result).isEqualTo("2024-12-01");
        }

        @Test
        @DisplayName("PLJ-01-03: Any day in month is normalised to day 1")
        void anyDayIsNormalisedToDay1() {
            // B-PLJ-01: legacy sets withDayOfMonth(1), so day is always 1
            String result = ActualizarParametrosFinancierosService.corregirFechaIPC("2025-06-15");
            assertThat(result).isEqualTo("2025-05-01");
        }

        @Test
        @DisplayName("PLJ-01-04: Unparseable date is returned unchanged")
        void unparseableReturnsOriginal() {
            // B-PLJ-01: catch DateTimeParseException → return fecha
            String result = ActualizarParametrosFinancierosService.corregirFechaIPC("not-a-date");
            assertThat(result).isEqualTo("not-a-date");
        }

        @Test
        @DisplayName("PLJ-01-05: null fecha throws NullPointerException")
        void nullFechaThrowsNPE() {
            assertThatThrownBy(() ->
                    ActualizarParametrosFinancierosService.corregirFechaIPC(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // =========================================================================
    // B-PLJ-02: Dolar observado date correction
    // =========================================================================

    @Nested
    @DisplayName("Dolar observado date correction — B-PLJ-02")
    class DolarObservadoCorrection {

        private static final String ANT  = "2025-04-08";  // fechaSistemaAnterior
        private static final String HOY  = "2025-04-09";  // fechaSistema
        private static final String SGTE = "2025-04-10";  // fechaSistemaSiguiente

        @Test
        @DisplayName("PLJ-02-01: wsDate == fechaSistemaAnterior → corrected to fechaSistema")
        void anteriorBecomesHoy() {
            String result = ActualizarParametrosFinancierosService
                    .corregirFechaDolarObservado(ANT, ANT, HOY, SGTE);
            assertThat(result).isEqualTo(HOY);
        }

        @Test
        @DisplayName("PLJ-02-02: wsDate == fechaSistema → corrected to fechaSistemaSiguiente")
        void hoyBecomesSignificant() {
            String result = ActualizarParametrosFinancierosService
                    .corregirFechaDolarObservado(HOY, ANT, HOY, SGTE);
            assertThat(result).isEqualTo(SGTE);
        }

        @Test
        @DisplayName("PLJ-02-03: wsDate == fechaSistemaSiguiente → null (skip record)")
        void siguienteReturnsNull() {
            String result = ActualizarParametrosFinancierosService
                    .corregirFechaDolarObservado(SGTE, ANT, HOY, SGTE);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("PLJ-02-04: unrelated wsDate is returned unchanged")
        void unrelatedDateReturnedAsIs() {
            String result = ActualizarParametrosFinancierosService
                    .corregirFechaDolarObservado("2025-03-15", ANT, HOY, SGTE);
            assertThat(result).isEqualTo("2025-03-15");
        }
    }

    // =========================================================================
    // B-PLJ-03: Paridad en USD
    // =========================================================================

    @Nested
    @DisplayName("Paridad en USD calculation — B-PLJ-03")
    class ParidadEnUSD {

        @Test
        @DisplayName("PLJ-03-01: standard division with 8 decimal places FLOOR")
        void standardDivision() {
            // B-PLJ-03: 1000 / 950 = 1.05263157... FLOOR to 8dp
            BigDecimal result = ActualizarParametrosFinancierosService.calcularParidadEnUSD(
                    new BigDecimal("1000"), new BigDecimal("950"));
            assertThat(result).isEqualByComparingTo("1.05263157");
        }

        @Test
        @DisplayName("PLJ-03-02: rawValor = 0 returns 0 unchanged (guard)")
        void zeroRawValorReturnsZero() {
            BigDecimal result = ActualizarParametrosFinancierosService.calcularParidadEnUSD(
                    BigDecimal.ZERO, new BigDecimal("950"));
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("PLJ-03-03: dolarObservado = 0 returns rawValor unchanged")
        void zeroDolarReturnsRaw() {
            BigDecimal raw = new BigDecimal("500");
            BigDecimal result = ActualizarParametrosFinancierosService.calcularParidadEnUSD(
                    raw, BigDecimal.ZERO);
            assertThat(result).isEqualByComparingTo(raw);
        }

        @Test
        @DisplayName("PLJ-03-04: null dolarObservado returns rawValor unchanged")
        void nullDolarReturnsRaw() {
            BigDecimal raw = new BigDecimal("500");
            BigDecimal result = ActualizarParametrosFinancierosService.calcularParidadEnUSD(
                    raw, null);
            assertThat(result).isEqualByComparingTo(raw);
        }

        @Test
        @DisplayName("PLJ-03-05: null rawValor throws NullPointerException")
        void nullRawThrowsNPE() {
            assertThatThrownBy(() ->
                    ActualizarParametrosFinancierosService.calcularParidadEnUSD(null, new BigDecimal("950")))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // =========================================================================
    // B-PLJ-04: Non-trading-day fill detection
    // =========================================================================

    @Nested
    @DisplayName("Non-trading-day fill detection — B-PLJ-04")
    class DiasNoHabiles {

        @Test
        @DisplayName("PLJ-04-01: consecutive days → no non-trading day")
        void consecutiveDaysNonTrading() {
            // B-PLJ-04: gap == 1 → false
            boolean result = ActualizarParametrosFinancierosService
                    .hayDiasNoHabiles("2025-04-09", "2025-04-10");
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("PLJ-04-02: gap of 3 (Friday to Monday) → non-trading days exist")
        void fridayToMondayHasNonTradingDays() {
            // B-PLJ-04: 2025-04-11 (Fri) → 2025-04-14 (Mon), gap = 3
            boolean result = ActualizarParametrosFinancierosService
                    .hayDiasNoHabiles("2025-04-11", "2025-04-14");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("PLJ-04-03: gap of 2 → non-trading day exists")
        void gapOf2HasNonTradingDay() {
            boolean result = ActualizarParametrosFinancierosService
                    .hayDiasNoHabiles("2025-04-09", "2025-04-11");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("PLJ-04-04: null fechaSistema throws NullPointerException")
        void nullFechaSistemaThrowsNPE() {
            assertThatThrownBy(() ->
                    ActualizarParametrosFinancierosService.hayDiasNoHabiles(null, "2025-04-10"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // =========================================================================
    // Integration test — requires HANA
    // =========================================================================

    @Test
    @Disabled("requires HANA")
    @DisplayName("PLJ-SVC-01: ejecutar() updates ParametroFinanciero table in HANA")
    void ejecutarUpdatesHANA() {
        // When Step 2 wires a real HANA datasource:
        //   Given idEmpresa = 1L, HANA credentials from env
        //   When  service.ejecutar(1L) is called
        //   Then  MER.ParametroFinanciero rows are upserted for today's fechaSistema
        // Credentials must come from HANA_URL / HANA_USER / HANA_PASSWORD env vars
    }
}
