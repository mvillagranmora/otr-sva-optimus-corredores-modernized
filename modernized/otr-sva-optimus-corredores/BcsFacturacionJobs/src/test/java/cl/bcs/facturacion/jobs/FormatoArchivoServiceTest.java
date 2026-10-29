package cl.bcs.facturacion.jobs;

import cl.bcs.facturacion.jobs.domain.CampoFormato;
import cl.bcs.facturacion.jobs.domain.ConfiguracionFormato;
import cl.bcs.facturacion.jobs.service.FormatoArchivoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-logic characterization tests for {@link FormatoArchivoService}.
 *
 * <p>No Spring context is loaded — all methods are {@code public static}
 * and are called directly.</p>
 *
 * <p>Oracle: legacy {@code cl.bcs.facturacion.jobs.generarArchivo.FormatoArchivo#formatear}
 * Legacy file: FormatoArchivo.java</p>
 *
 * <p>Rule IDs pinned: B-FAJ-01 through B-FAJ-18</p>
 *
 * <p>Equivalence cases executed: 18 (all enabled tests)</p>
 */
@DisplayName("FormatoArchivoService — fixed-width field formatting rules")
class FormatoArchivoServiceTest {

    // =========================================================================
    // Helper: build a ConfiguracionFormato for a single field
    // =========================================================================

    private static ConfiguracionFormato cfg(String tipoDato,
                                             int largo,
                                             int decimales,
                                             String valorDefecto,
                                             String formato,
                                             boolean rellenar,
                                             String separador) {
        CampoFormato campo = new CampoFormato(
                1,          // tipoRegistro
                "CAMPO",    // campo name
                tipoDato,
                largo,
                decimales,
                valorDefecto != null ? valorDefecto : "",
                formato != null ? formato : ""
        );
        return new ConfiguracionFormato(
                rellenar ? "S" : "N",
                separador,
                List.of(campo)
        );
    }

    /** Convenience: formatear with tipoRegistro=1, campo="CAMPO". */
    private static String fmt(ConfiguracionFormato config, String valor) {
        return FormatoArchivoService.formatear(config, 1, "CAMPO", valor);
    }

    // =========================================================================
    // CHAR tests  (B-FAJ-01 to B-FAJ-05)
    // =========================================================================

    @Nested
    @DisplayName("CHAR type")
    class CharTests {

        @Test
        @DisplayName("B-FAJ-01: value shorter than largo → padded with trailing spaces")
        void charPadRight() {
            // "HOLA" (4 chars) → "HOLA      " (large=10, rellenar=S)
            ConfiguracionFormato cfg = cfg("CHAR", 10, 0, null, null, true, ".");
            assertThat(fmt(cfg, "HOLA")).isEqualTo("HOLA      ");
        }

        @Test
        @DisplayName("B-FAJ-02: value longer than largo → truncated to largo")
        void charTruncate() {
            // "HOLA MUNDO" (10 chars) → "HOLA" (largo=4, rellenar=S)
            ConfiguracionFormato cfg = cfg("CHAR", 4, 0, null, null, true, ".");
            assertThat(fmt(cfg, "HOLA MUNDO")).isEqualTo("HOLA");
        }

        @Test
        @DisplayName("B-FAJ-03: rellenar=N → no padding, value returned as-is after trim")
        void charNoFill() {
            // "HOLA" (4 chars), largo=10, rellenar=N → "HOLA" (no padding)
            ConfiguracionFormato cfg = cfg("CHAR", 10, 0, null, null, false, ".");
            assertThat(fmt(cfg, "HOLA")).isEqualTo("HOLA");
        }

        @Test
        @DisplayName("B-FAJ-04: empty value → valorDefecto applied then padded")
        void charDefaultValue() {
            // valor="" → valorDefecto="DEF" → "DEF  " (largo=5, rellenar=S)
            ConfiguracionFormato cfg = cfg("CHAR", 5, 0, "DEF", null, true, ".");
            assertThat(fmt(cfg, "")).isEqualTo("DEF  ");
        }

        @Test
        @DisplayName("B-FAJ-05: value with surrounding spaces → trimmed first then padded")
        void charTrimThenPad() {
            // "  AB  " → trimmed to "AB" → "AB   " (largo=5, rellenar=S)
            ConfiguracionFormato cfg = cfg("CHAR", 5, 0, null, null, true, ".");
            assertThat(fmt(cfg, "  AB  ")).isEqualTo("AB   ");
        }
    }

    // =========================================================================
    // NUM tests  (B-FAJ-06 to B-FAJ-08)
    // =========================================================================

    @Nested
    @DisplayName("NUM type")
    class NumTests {

        @Test
        @DisplayName("B-FAJ-06: rellenar=S → right-aligned with leading zeros")
        void numLeadingZeros() {
            // "99" padded to 5 → "00099"
            ConfiguracionFormato cfg = cfg("NUM", 5, 0, null, null, true, ".");
            assertThat(fmt(cfg, "99")).isEqualTo("00099");
        }

        @Test
        @DisplayName("B-FAJ-07: value exactly largo → no change (rellenar=S)")
        void numExactLength() {
            // "999" padded to 3 → "999" (already exact)
            ConfiguracionFormato cfg = cfg("NUM", 3, 0, null, null, true, ".");
            assertThat(fmt(cfg, "999")).isEqualTo("999");
        }

        @Test
        @DisplayName("B-FAJ-08: rellenar=N → spaces replaced with zeros, no padding")
        void numNoFill() {
            // "5", largo=3, rellenar=N → "5"
            ConfiguracionFormato cfg = cfg("NUM", 3, 0, null, null, false, ".");
            assertThat(fmt(cfg, "5")).isEqualTo("5");
        }
    }

    // =========================================================================
    // DEC tests  (B-FAJ-09 to B-FAJ-13)
    // =========================================================================

    @Nested
    @DisplayName("DEC type")
    class DecTests {

        @Test
        @DisplayName("B-FAJ-09: full decimal value padded to largoParteEntera and decimales")
        void decNormalValue() {
            // largo=8, decimales=2 → largoParteEntera=6
            // "1234.56" → parteEntera="001234", parteDecimal="56" → "001234.56"
            ConfiguracionFormato cfg = cfg("DEC", 8, 2, null, null, true, ".");
            assertThat(fmt(cfg, "1234.56")).isEqualTo("001234.56");
        }

        @Test
        @DisplayName("B-FAJ-10: short decimal part → padded with trailing zeros")
        void decTrailingZerosOnDecimalPart() {
            // largo=6, decimales=2 → largoParteEntera=4
            // "9.5" → parteEntera="0009", parteDecimal="50" → "0009.50"
            ConfiguracionFormato cfg = cfg("DEC", 6, 2, null, null, true, ".");
            assertThat(fmt(cfg, "9.5")).isEqualTo("0009.50");
        }

        @Test
        @DisplayName("B-FAJ-11: non-numeric value with no dot → treated as 0 → all zeros")
        void decNoDotTreatedAsZero() {
            // "OE-80" has no "." → valor="0"
            // largo=6, decimales=2 → largoParteEntera=4
            // parteEntera="0"→"0000", parteDecimal="0"→"00" → "0000.00"
            ConfiguracionFormato cfg = cfg("DEC", 6, 2, null, null, true, ".");
            assertThat(fmt(cfg, "OE-80")).isEqualTo("0000.00");
        }

        @Test
        @DisplayName("B-FAJ-12: decimal part longer than decimales → truncated")
        void decTruncateDecimalPart() {
            // "12.3456" → parteDecimal truncated to 2 → "34"
            // largo=6, decimales=2 → largoParteEntera=4
            // parteEntera="0012", parteDecimal="34" → "0012,34" (comma separator)
            ConfiguracionFormato cfg = cfg("DEC", 6, 2, null, null, true, ",");
            assertThat(fmt(cfg, "12.3456")).isEqualTo("0012,34");
        }

        @Test
        @DisplayName("B-FAJ-13: rellenar=N → no padding, spaces replaced with zeros only")
        void decNoFill() {
            // "12.3", rellenar=N → parteEntera="12", parteDecimal="3" → "12.3"
            ConfiguracionFormato cfg = cfg("DEC", 6, 2, null, null, false, ".");
            assertThat(fmt(cfg, "12.3")).isEqualTo("12.3");
        }
    }

    // =========================================================================
    // DATE tests  (B-FAJ-14 to B-FAJ-16)
    // =========================================================================

    @Nested
    @DisplayName("DATE type")
    class DateTests {

        @Test
        @DisplayName("B-FAJ-14: format ddMMyyyy → reformatted then padded to largo")
        void dateDdMmYyyy() {
            // "2024-01-15" with formato="ddMMyyyy" → "15012024" (8 chars)
            // largo=10 → "15012024  " (2 trailing spaces)
            ConfiguracionFormato cfg = cfg("DATE", 10, 0, null, "ddMMyyyy", true, ".");
            assertThat(fmt(cfg, "2024-01-15")).isEqualTo("15012024  ");
        }

        @Test
        @DisplayName("B-FAJ-15: format yyyyMMdd → reformatted, fits exactly in largo=8")
        void dateYyyyMmDd() {
            // "2024-01-15" with formato="yyyyMMdd" → "20240115" (exactly 8)
            ConfiguracionFormato cfg = cfg("DATE", 8, 0, null, "yyyyMMdd", true, ".");
            assertThat(fmt(cfg, "2024-01-15")).isEqualTo("20240115");
        }

        @Test
        @DisplayName("B-FAJ-16: empty formato → raw value left-aligned within largo (no truncation)")
        void dateEmptyFormat() {
            // "2024-01-15" (10 chars), formato="", largo=8
            // String.format("%-8s","2024-01-15") = "2024-01-15" (no truncation in Java)
            ConfiguracionFormato cfg = cfg("DATE", 8, 0, null, "", true, ".");
            assertThat(fmt(cfg, "2024-01-15")).isEqualTo("2024-01-15");
        }
    }

    // =========================================================================
    // No matching field definition  (B-FAJ-17)
    // =========================================================================

    @Test
    @DisplayName("B-FAJ-17: unknown tipoRegistro → returns empty string")
    void noMatchReturnsEmpty() {
        ConfiguracionFormato cfg = cfg("CHAR", 10, 0, null, null, true, ".");
        // tipoRegistro=99 has no matching CampoFormato → ""
        String result = FormatoArchivoService.formatear(cfg, 99, "NOPE", "X");
        assertThat(result).isEmpty();
    }

    // =========================================================================
    // Canary  (B-FAJ-18)
    // =========================================================================

    @Test
    @DisplayName("B-FAJ-18 [CANARY]: static helper formatearDec reproduces exact legacy DEC logic")
    void canaryDecLegacyEdgeCase() {
        // DEC: no dot → "0" edge case (OE-80 scenario from legacy system)
        // largo=6, decimales=2, sep=".", rellenar=true
        // Expected: "0000.00"
        String result = FormatoArchivoService.formatearDec("OE-80", 6, 2, true, ".");
        assertThat(result).isEqualTo("0000.00");
    }
}
