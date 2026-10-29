package cl.bcs.fondomutuo.jobs;

import cl.bcs.fondomutuo.jobs.service.CargaMasivaSolicitudesJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization tests for CargaMasivaSolicitudesJob.parseTransacciones().
 *
 * Oracle:
 *   legacy cl.bcs.fondomutuo.BcsJobCargaMasivaSolicitudes (lines 44-50)
 *   String[] transacciones = trans.split(";");
 *   if (transacciones.length == 0) → return false
 *   carga.IdCarga = Long.parseLong(transacciones[i]);
 *
 * Rule IDs pinned:
 *   B-FMJ-01  Transacciones split by ";" into individual IdCarga values.
 *   B-FMJ-02  Empty list (null/blank) → returns empty list, no service call.
 */
@DisplayName("CargaMasivaSolicitudes — parseTransacciones")
class CargaMasivaSolicitudesJobTest {

    @Nested
    @DisplayName("ParsingRules — B-FMJ-01 / B-FMJ-02")
    class ParsingRules {

        @Test
        @DisplayName("FMJ-CM-01 [B-FMJ-01]: semicolon-delimited string parsed into Long list")
        void rule_FMJ_CM_01_semicolonDelimitedStringParsedIntoLongList() {
            // B-FMJ-01: "123;456;789" → [123, 456, 789]
            List<Long> result = CargaMasivaSolicitudesJob.parseTransacciones("123;456;789");

            assertThat(result).hasSize(3);                    // FMJ-CM-01
            assertThat(result).containsExactly(123L, 456L, 789L); // FMJ-CM-01
        }

        @Test
        @DisplayName("FMJ-CM-02 [B-FMJ-01]: single ID without semicolons → single element list")
        void rule_FMJ_CM_02_singleIdWithoutSemicolons() {
            List<Long> result = CargaMasivaSolicitudesJob.parseTransacciones("99999");

            assertThat(result).hasSize(1);                 // FMJ-CM-02
            assertThat(result).containsExactly(99999L);    // FMJ-CM-02
        }

        @Test
        @DisplayName("FMJ-CM-03 [B-FMJ-02]: null input → empty list")
        void rule_FMJ_CM_03_nullInputReturnsEmptyList() {
            // B-FMJ-02: null → empty list
            List<Long> result = CargaMasivaSolicitudesJob.parseTransacciones(null);

            assertThat(result).isEmpty(); // FMJ-CM-03
        }

        @Test
        @DisplayName("FMJ-CM-04 [B-FMJ-02]: blank string → empty list")
        void rule_FMJ_CM_04_blankStringReturnsEmptyList() {
            // B-FMJ-02: blank → empty list
            List<Long> result = CargaMasivaSolicitudesJob.parseTransacciones("   ");

            assertThat(result).isEmpty(); // FMJ-CM-04
        }

        @Test
        @DisplayName("FMJ-CM-05 [B-FMJ-01]: whitespace around IDs is trimmed")
        void rule_FMJ_CM_05_whitespaceAroundIdsTrimmed() {
            // Tolerates leading/trailing spaces after split
            List<Long> result = CargaMasivaSolicitudesJob.parseTransacciones("100; 200 ;300");

            assertThat(result).containsExactly(100L, 200L, 300L); // FMJ-CM-05
        }

        @Test
        @DisplayName("FMJ-CM-06 [B-FMJ-01]: non-numeric token → NumberFormatException")
        void rule_FMJ_CM_06_nonNumericTokenThrowsNumberFormatException() {
            // Legacy uses Long.parseLong — invalid input throws NumberFormatException
            assertThatThrownBy(() -> CargaMasivaSolicitudesJob.parseTransacciones("123;abc;456"))
                    .isInstanceOf(NumberFormatException.class); // FMJ-CM-06
        }

        @Test
        @DisplayName("FMJ-CM-07 [B-FMJ-01]: large IdCarga values preserved exactly")
        void rule_FMJ_CM_07_largeIdCargaValuesPreserved() {
            String input = "17405965;17405964;17405966";

            List<Long> result = CargaMasivaSolicitudesJob.parseTransacciones(input);

            assertThat(result).containsExactly(17405965L, 17405964L, 17405966L); // FMJ-CM-07
        }
    }
}
