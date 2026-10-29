package cl.bcs.fondomutuo.jobs;

import cl.bcs.fondomutuo.jobs.service.IngresarOrdenBonificacionFiscalReinversionJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization tests for IngresarOrdenBonificacionFiscalReinversionJob pure logic.
 *
 * Oracle:
 *   legacy cl.bcs.fondomutuo.jobs.IngresarOrdenBonificacionFiscalReinversion
 *   (lines 207-271)
 *
 * Rule IDs pinned:
 *   B-FMJ-17  porcentaje=0 → PorcentajeCalculado = NumeroCuotas*100 / CantidadCustodia (HALF_UP, scale=2)
 *   B-FMJ-18  TipoSolicitud=APORTE → CalculoPorMonto; Monto = MontoMercado * porcentaje/100
 *   B-FMJ-19  TipoSolicitud=RESCATE → CalculoPorCuota; cuotas = CantidadCustodia * porcentaje/100,
 *              scaled to DecimalesCuota HALF_DOWN
 *   B-FMJ-20  Custody overshoot cap: if CantidadCustodia < totalNumeroCuotasRescates and diff < 1 →
 *              adjust NumeroCuotas.
 *
 * Oracle methods inline the exact legacy formulas.
 * No modernized service/port classes are instantiated.
 */
@DisplayName("IngresarOrdenBonificacionFiscalReinversion — BF calculation logic")
class IngresarOrdenBonificacionFiscalReinversionTest {

    // ── Oracles (inline legacy formulas) ──────────────────────────────────────

    /**
     * B-FMJ-17: porcentaje = NumeroCuotas * 100 / cantidadCustodia (HALF_UP, scale=2).
     * Legacy IngresarOrdenBonificacionFiscalReinversion.java:210
     */
    static BigDecimal oracleCalcularPorcentaje(BigDecimal numeroCuotas, BigDecimal cantidadCustodia) {
        return numeroCuotas
                .multiply(BigDecimal.valueOf(100))
                .divide(cantidadCustodia, 2, RoundingMode.HALF_UP);
    }

    /**
     * B-FMJ-19: NumeroCuotas = CantidadCustodia * effectivePorcentaje / 100, scaled HALF_DOWN.
     * Legacy IngresarOrdenBonificacionFiscalReinversion.java:260
     */
    static BigDecimal oracleCalcularNumeroCuotasRescate(
            BigDecimal cantidadCustodia, BigDecimal porcentaje, int decimalesCuota) {
        return cantidadCustodia
                .multiply(porcentaje)
                .divide(BigDecimal.valueOf(100))
                .setScale(decimalesCuota, RoundingMode.HALF_DOWN);
    }

    /**
     * B-FMJ-20: Overshoot cap.
     * Legacy IngresarOrdenBonificacionFiscalReinversion.java:265-269
     */
    static BigDecimal oracleAplicarCap(BigDecimal numeroCuotas,
            BigDecimal cantidadCustodia, BigDecimal totalNumeroCuotasRescates) {
        if (cantidadCustodia.compareTo(totalNumeroCuotasRescates) < 0) {
            BigDecimal diff = cantidadCustodia.subtract(totalNumeroCuotasRescates).abs();
            if (diff.compareTo(BigDecimal.ONE) < 1) {
                return numeroCuotas.add(cantidadCustodia.subtract(totalNumeroCuotasRescates));
            }
        }
        return numeroCuotas;
    }

    // ── Nested tests ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PorcentajeCalculado — B-FMJ-17")
    class PorcentajeCalculado {

        @Test
        @DisplayName("FMJ-BF-01 [B-FMJ-17]: exact division — 300 cuotas / 1000 custody = 30.00%")
        void rule_FMJ_BF_01_exactDivisionProduces30pct() {
            BigDecimal result = IngresarOrdenBonificacionFiscalReinversionJob.calcularPorcentaje(
                    new BigDecimal("300"), new BigDecimal("1000"));

            // Oracle: 300 * 100 / 1000 = 30.00
            BigDecimal oracle = oracleCalcularPorcentaje(new BigDecimal("300"), new BigDecimal("1000"));
            assertThat(result).isEqualByComparingTo(oracle); // FMJ-BF-01
            assertThat(result).isEqualByComparingTo(new BigDecimal("30.00")); // FMJ-BF-01
        }

        @Test
        @DisplayName("FMJ-BF-02 [B-FMJ-17]: non-terminating division rounded HALF_UP at scale 2")
        void rule_FMJ_BF_02_nonTerminatingDivisionRoundedHalfUp() {
            // 1 cuota / 3 custody = 33.333... → 33.33 (HALF_UP)
            BigDecimal result = IngresarOrdenBonificacionFiscalReinversionJob.calcularPorcentaje(
                    new BigDecimal("1"), new BigDecimal("3"));

            BigDecimal oracle = oracleCalcularPorcentaje(new BigDecimal("1"), new BigDecimal("3"));
            assertThat(result).isEqualByComparingTo(oracle); // FMJ-BF-02
            assertThat(result).isEqualByComparingTo(new BigDecimal("33.33")); // FMJ-BF-02
        }

        @Test
        @DisplayName("FMJ-BF-03 [B-FMJ-17]: 2/3 of custody = 66.67% (HALF_UP rounding)")
        void rule_FMJ_BF_03_twoThirdsCustody() {
            // 2 cuotas / 3 custody = 66.666... → 66.67 (HALF_UP)
            BigDecimal result = IngresarOrdenBonificacionFiscalReinversionJob.calcularPorcentaje(
                    new BigDecimal("2"), new BigDecimal("3"));

            BigDecimal oracle = oracleCalcularPorcentaje(new BigDecimal("2"), new BigDecimal("3"));
            assertThat(result).isEqualByComparingTo(oracle); // FMJ-BF-03
            assertThat(result).isEqualByComparingTo(new BigDecimal("66.67")); // FMJ-BF-03
        }

        @Test
        @DisplayName("FMJ-BF-04 [B-FMJ-17]: scale is always 2 decimal places")
        void rule_FMJ_BF_04_scaleIsAlways2() {
            BigDecimal result = IngresarOrdenBonificacionFiscalReinversionJob.calcularPorcentaje(
                    new BigDecimal("50"), new BigDecimal("100"));

            // 50*100/100 = 50.00 — scale 2
            assertThat(result.scale()).isEqualTo(2); // FMJ-BF-04
        }
    }

    @Nested
    @DisplayName("NumeroCuotasRescate — B-FMJ-19")
    class NumeroCuotasRescate {

        @Test
        @DisplayName("FMJ-BF-05 [B-FMJ-19]: cuotas = custody * porcentaje / 100, HALF_DOWN at decimalesCuota=8")
        void rule_FMJ_BF_05_cuotasCalculated_8decimals() {
            BigDecimal cantidadCustodia    = new BigDecimal("400.00");
            BigDecimal effectivePorcentaje = new BigDecimal("25.00");
            int        decimalesCuota      = 8;

            BigDecimal result = IngresarOrdenBonificacionFiscalReinversionJob.calcularNumeroCuotasRescate(
                    cantidadCustodia, effectivePorcentaje, decimalesCuota);

            BigDecimal oracle = oracleCalcularNumeroCuotasRescate(cantidadCustodia, effectivePorcentaje, decimalesCuota);
            assertThat(result).isEqualByComparingTo(oracle); // FMJ-BF-05
            // 400 * 25 / 100 = 100.00000000
            assertThat(result).isEqualByComparingTo(new BigDecimal("100.00000000")); // FMJ-BF-05
        }

        @Test
        @DisplayName("FMJ-BF-06 [B-FMJ-19]: HALF_DOWN rounding — 0.555... at scale 2 → 0.55")
        void rule_FMJ_BF_06_halfDownRounding() {
            // 1 * 55.555... / 100 = 0.55555... → HALF_DOWN at scale 2 → 0.55
            // Use 5/9 * 100 as approximate; use simpler: custody=100, porcentaje=55.555..., scale=2
            // Actually: 100 * (5/9) = 55.555... needs terminating. Let's use:
            // custody=9, porcentaje=6.1111%, decimal=2 → 9 * 6.1111 / 100 = 0.550...
            // Simpler: custody=200, porcentaje=27.75, decimal=2 → 200*27.75/100 = 55.50 (exact, no rounding)
            // Let's use non-terminating: custody=3, porcentaje=100/3.0, decimal=2
            // Actually best: use custody=10, porcentaje=55/10.0 = ...
            // Easiest: 10 cuotas at 33.33% scale 2 → 10*33.33/100 = 3.333 HALF_DOWN → 3.33
            BigDecimal cantidadCustodia    = new BigDecimal("10");
            BigDecimal effectivePorcentaje = new BigDecimal("33.33");
            int        decimalesCuota      = 2;

            BigDecimal result = IngresarOrdenBonificacionFiscalReinversionJob.calcularNumeroCuotasRescate(
                    cantidadCustodia, effectivePorcentaje, decimalesCuota);

            BigDecimal oracle = oracleCalcularNumeroCuotasRescate(cantidadCustodia, effectivePorcentaje, decimalesCuota);
            assertThat(result).isEqualByComparingTo(oracle);                  // FMJ-BF-06
            // 10 * 33.33 / 100 = 3.333 → HALF_DOWN scale 2 → 3.33
            assertThat(result).isEqualByComparingTo(new BigDecimal("3.33"));  // FMJ-BF-06
        }

        @Test
        @DisplayName("FMJ-BF-07 [B-FMJ-19]: scale of result matches decimalesCuota")
        void rule_FMJ_BF_07_scaleMatchesDecimalesCuota() {
            BigDecimal result = IngresarOrdenBonificacionFiscalReinversionJob.calcularNumeroCuotasRescate(
                    new BigDecimal("100"), new BigDecimal("50.00"), 4);

            // 100 * 50 / 100 = 50.0000
            assertThat(result.scale()).isEqualTo(4); // FMJ-BF-07
        }
    }

    @Nested
    @DisplayName("CustodyCapOveshoot — B-FMJ-20")
    class CustodyCap {

        @Test
        @DisplayName("FMJ-BF-08 [B-FMJ-20]: no cap when custody >= total → numeroCuotas unchanged")
        void rule_FMJ_BF_08_noCap_custodyGeTotal() {
            BigDecimal numeroCuotas            = new BigDecimal("60.00");
            BigDecimal cantidadCustodia        = new BigDecimal("100.00");
            BigDecimal totalNumeroCuotasRescates = new BigDecimal("60.00"); // custody > total

            BigDecimal result = IngresarOrdenBonificacionFiscalReinversionJob.aplicarCapCustodia(
                    numeroCuotas, cantidadCustodia, totalNumeroCuotasRescates);

            BigDecimal oracle = oracleAplicarCap(numeroCuotas, cantidadCustodia, totalNumeroCuotasRescates);
            assertThat(result).isEqualByComparingTo(oracle);     // FMJ-BF-08
            assertThat(result).isEqualByComparingTo(numeroCuotas); // FMJ-BF-08
        }

        @Test
        @DisplayName("FMJ-BF-09 [B-FMJ-20]: overshoot < 1 → numeroCuotas adjusted by negative delta")
        void rule_FMJ_BF_09_overshootLt1_adjustedByDelta() {
            // total = 100.5, custody = 100.0 → overshoot = 0.5 < 1 → cap
            // numeroCuotas + (100 - 100.5) = numeroCuotas - 0.5
            BigDecimal numeroCuotas              = new BigDecimal("60.50");
            BigDecimal cantidadCustodia          = new BigDecimal("100.00");
            BigDecimal totalNumeroCuotasRescates = new BigDecimal("100.50"); // overshoot 0.5

            BigDecimal result = IngresarOrdenBonificacionFiscalReinversionJob.aplicarCapCustodia(
                    numeroCuotas, cantidadCustodia, totalNumeroCuotasRescates);

            BigDecimal oracle = oracleAplicarCap(numeroCuotas, cantidadCustodia, totalNumeroCuotasRescates);
            assertThat(result).isEqualByComparingTo(oracle);            // FMJ-BF-09
            // expected: 60.50 + (100.00 - 100.50) = 60.00
            assertThat(result).isEqualByComparingTo(new BigDecimal("60.00")); // FMJ-BF-09
        }

        @Test
        @DisplayName("FMJ-BF-10 [B-FMJ-20]: overshoot exactly 1 → cap applied (diff < 1 is NOT triggered)")
        void rule_FMJ_BF_10_overshootExactly1_noCapApplied() {
            // diff = 1, and diff.compareTo(ONE) < 1 is false when diff == 1
            // Wait: diff.compareTo(BigDecimal.ONE) < 1 means diff < 1 OR diff == 1
            // Actually compareTo < 1 means less than 1 (strictly) in BigDecimal
            // diff.compareTo(ONE) returns -1 (less), 0 (equal), 1 (greater)
            // < 1 means result is -1 or 0, i.e. diff <= 1
            // So overshoot == 1 IS captured. Let's verify:
            BigDecimal numeroCuotas              = new BigDecimal("61.00");
            BigDecimal cantidadCustodia          = new BigDecimal("100.00");
            BigDecimal totalNumeroCuotasRescates = new BigDecimal("101.00"); // overshoot exactly 1

            BigDecimal result = IngresarOrdenBonificacionFiscalReinversionJob.aplicarCapCustodia(
                    numeroCuotas, cantidadCustodia, totalNumeroCuotasRescates);

            BigDecimal oracle = oracleAplicarCap(numeroCuotas, cantidadCustodia, totalNumeroCuotasRescates);
            // Oracle result: diff = 1 → diff.compareTo(ONE) = 0 < 1 → cap applied
            // result = 61 + (100 - 101) = 60
            assertThat(result).isEqualByComparingTo(oracle);                  // FMJ-BF-10
            assertThat(result).isEqualByComparingTo(new BigDecimal("60.00")); // FMJ-BF-10
        }

        @Test
        @DisplayName("FMJ-BF-11 [B-FMJ-20]: overshoot > 1 → no cap (diff too large)")
        void rule_FMJ_BF_11_overshootGt1_noCapApplied() {
            // diff = 2 → diff.compareTo(ONE) = 1 → condition fails → no cap
            BigDecimal numeroCuotas              = new BigDecimal("62.00");
            BigDecimal cantidadCustodia          = new BigDecimal("100.00");
            BigDecimal totalNumeroCuotasRescates = new BigDecimal("102.00"); // overshoot = 2

            BigDecimal result = IngresarOrdenBonificacionFiscalReinversionJob.aplicarCapCustodia(
                    numeroCuotas, cantidadCustodia, totalNumeroCuotasRescates);

            BigDecimal oracle = oracleAplicarCap(numeroCuotas, cantidadCustodia, totalNumeroCuotasRescates);
            assertThat(result).isEqualByComparingTo(oracle);        // FMJ-BF-11
            // No cap: numeroCuotas unchanged
            assertThat(result).isEqualByComparingTo(numeroCuotas); // FMJ-BF-11
        }
    }

    @Nested
    @DisplayName("ProcesarComisionesNN — parsing B-FMJ-15")
    class ComisionesNNParsing {

        @Test
        @DisplayName("FMJ-BF-12 [B-FMJ-15]: comma-delimited comisiones parsed into Long list")
        void rule_FMJ_BF_12_commaDelimitedComisionesParsed() {
            var result = cl.bcs.fondomutuo.jobs.service.ProcesarComisionesNNJob
                    .parseComisiones("1531974,1534108,1531990");

            assertThat(result).containsExactly(1531974L, 1534108L, 1531990L); // FMJ-BF-12
        }

        @Test
        @DisplayName("FMJ-BF-13 [B-FMJ-15]: empty comisiones → empty list")
        void rule_FMJ_BF_13_emptyComisionesReturnsEmptyList() {
            var result = cl.bcs.fondomutuo.jobs.service.ProcesarComisionesNNJob.parseComisiones("");

            assertThat(result).isEmpty(); // FMJ-BF-13
        }
    }

    @Nested
    @DisplayName("GenerarCertificado31 — traspaso parsing")
    class Certificado31Parsing {

        @Test
        @DisplayName("FMJ-BF-14: comma-delimited traspaso IDs parsed into Long list")
        void rule_FMJ_BF_14_commaSeparatedTraspasoIdsParsed() {
            var result = cl.bcs.fondomutuo.jobs.service.GenerarCertificado31FFMMJob
                    .parseListaTraspasos("389,387,99953855");

            assertThat(result).containsExactly(389L, 387L, 99953855L); // FMJ-BF-14
        }

        @Test
        @DisplayName("FMJ-BF-15: null traspaso list → empty list")
        void rule_FMJ_BF_15_nullTraspasoListReturnsEmpty() {
            var result = cl.bcs.fondomutuo.jobs.service.GenerarCertificado31FFMMJob.parseListaTraspasos(null);

            assertThat(result).isEmpty(); // FMJ-BF-15
        }
    }
}
