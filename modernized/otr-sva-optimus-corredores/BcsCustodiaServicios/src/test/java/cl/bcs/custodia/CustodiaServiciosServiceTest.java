package cl.bcs.custodia;

import cl.bcs.custodia.adapter.out.CustodiaServiciosRepositoryAdapter;
import cl.bcs.custodia.service.CustodiaServiciosService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure-logic characterization tests for CustodiaServiciosService.
 * No Spring context — all methods under test are static.
 *
 * Rule IDs used:
 *   B-CUS-01..B-CUS-19  business-logic behaviors extracted from legacy sources
 *   RULE-076             no hardcoded credentials / main() method in service classes
 *
 * equivalence cases executed: 19
 */
class CustodiaServiciosServiceTest {

    // -----------------------------------------------------------------------
    // B-CUS-01 / B-CUS-02  cuentaDias
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-CUS-01: cuentaDias returns calendar days (tipo=C)")
    void bCus01_cuentaDias_calendarDays_tipo_C() {
        // B-CUS-01: calendar-day count between 2024-01-01 and 2024-01-08 is 7
        Long result = CustodiaServiciosService.cuentaDias("20240101", "20240108", "C");
        assertThat(result).isEqualTo(7L);
    }

    @Test
    @DisplayName("B-CUS-02: cuentaDias returns BUGGY working-day count (tipo=L) — legacy behavioral equivalence")
    void bCus02_cuentaDias_workingDays_legacyBug_tipo_L() {
        // B-CUS-02: Due to the legacy bug: ld = ld.plusDays(asd) instead of plusDays(1),
        // the loop skips Saturday and Sunday non-linearly.
        // 2024-01-01 (Mon) to 2024-01-08 (Mon) with the bug gives 6, not the correct 5.
        Long result = CustodiaServiciosService.cuentaDias("20240101", "20240108", "L");
        assertThat(result).isGreaterThan(0L);
        // Assert the exact BUGGY legacy output (6) for behavioral equivalence
        assertThat(result).isEqualTo(6L);
    }

    // -----------------------------------------------------------------------
    // B-CUS-03 / B-CUS-04  calculoInteres
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-CUS-03: calculoInteres returns correct simple interest (terminating decimals)")
    void bCus03_calculoInteres_terminatingDecimals() {
        // B-CUS-03: tasa=10, base=10, plazo=1, monto=1000
        // tasaDivCien = 10/100 = 0.1
        // plazoDivBase = 1/10 = 0.1
        // total = 1000 * (1 + 0.1 * 0.1) = 1000 * 1.01 = 1010
        // result = 1010 - 1000 = 10
        BigDecimal result = CustodiaServiciosService.calculoInteres(
                new BigDecimal("1000"),
                new BigDecimal("1"),
                new BigDecimal("10"),
                new BigDecimal("10"));
        assertThat(result.compareTo(new BigDecimal("10"))).isZero();
    }

    @Test
    @DisplayName("B-CUS-04: calculoInteres zero interest when tasa=0 (terminating decimals: plazo=1, base=10)")
    void bCus04_calculoInteres_zeroRate_returnsZero() {
        // B-CUS-04: tasa=0 -> tasaDivCien=0, plazo=1, base=10 -> plazoDivBase=0.1 (terminating)
        // total = 1000 * (1 + 0 * 0.1) = 1000 * 1 = 1000
        // result = 1000 - 1000 = 0
        BigDecimal result = CustodiaServiciosService.calculoInteres(
                new BigDecimal("1000"),
                new BigDecimal("1"),
                new BigDecimal("0"),
                new BigDecimal("10"));
        assertThat(result.compareTo(BigDecimal.ZERO)).isZero();
    }

    // -----------------------------------------------------------------------
    // B-CUS-05 / B-CUS-06  getHashBusqueda
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-CUS-05: getHashBusqueda constructs composite key correctly")
    void bCus05_getHashBusqueda_compositeKey() {
        // B-CUS-05: format is "idInstrumento-idPortafolio-idCliente-nemosvs"
        String hash = CustodiaServiciosService.getHashBusqueda(1L, 2L, 3L, "ABC");
        assertThat(hash).isEqualTo("1-2-3-ABC");
    }

    @Test
    @DisplayName("B-CUS-06: getHashBusqueda with empty nemosvs")
    void bCus06_getHashBusqueda_emptyNemosvs() {
        // B-CUS-06: trailing dash when nemosvs is empty string
        String hash = CustodiaServiciosService.getHashBusqueda(1L, 2L, 3L, "");
        assertThat(hash).isEqualTo("1-2-3-");
    }

    // -----------------------------------------------------------------------
    // B-CUS-07 / B-CUS-08  obtenerRangoDeFechas
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-CUS-07: obtenerRangoDeFechas returns inclusive date range over 3 days")
    void bCus07_obtenerRangoDeFechas_inclusiveRange() {
        // B-CUS-07: start=2024-01-01, end=2024-01-03 → 3 dates inclusive
        List<String> fechas = CustodiaServiciosService.obtenerRangoDeFechas("2024-01-01", "2024-01-03");
        assertThat(fechas).containsExactly("2024-01-01", "2024-01-02", "2024-01-03");
    }

    @Test
    @DisplayName("B-CUS-08: obtenerRangoDeFechas returns single date when start equals end")
    void bCus08_obtenerRangoDeFechas_singleDay() {
        // B-CUS-08: start == end → list with exactly one element
        List<String> fechas = CustodiaServiciosService.obtenerRangoDeFechas("2024-01-01", "2024-01-01");
        assertThat(fechas).containsExactly("2024-01-01");
    }

    // -----------------------------------------------------------------------
    // B-CUS-09 / B-CUS-10  TIPO_BUSQUEDAS constant
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-CUS-09: TIPO_BUSQUEDAS contains 15 elements")
    void bCus09_tipoBusquedas_has15Elements() {
        // B-CUS-09: legacy ConsultarCustodiaPortafolio.java defines 15 search types
        assertThat(CustodiaServiciosService.TIPO_BUSQUEDAS).hasSize(15);
    }

    @Test
    @DisplayName("B-CUS-10: TIPO_BUSQUEDAS contains CUS_NEG")
    void bCus10_tipoBusquedas_containsCusNeg() {
        // B-CUS-10: CUS_NEG (negative custody) must be present
        assertThat(CustodiaServiciosService.TIPO_BUSQUEDAS).contains("CUS_NEG");
    }

    // -----------------------------------------------------------------------
    // B-CUS-11  TIPO_PRESTAMO_VENTA_CORTA constant
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-CUS-11: TIPO_PRESTAMO_VENTA_CORTA equals PRTVTC")
    void bCus11_tipoPrestamo_ventaCorta_isPRTVTC() {
        // B-CUS-11: legacy AnticiparPrestamo.java short-sale loan type constant
        assertThat(CustodiaServiciosService.TIPO_PRESTAMO_VENTA_CORTA).isEqualTo("PRTVTC");
    }

    // -----------------------------------------------------------------------
    // B-CUS-12..B-CUS-17  determinarNuevoNemo
    // Legacy source: DeterminaNuevoNemo.NuevoNemo() (COBOL LMRUCANE.SRC)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-CUS-12: determinarNuevoNemo — '**' in nemo replaces with ' *' (first stage)")
    void bCus12_determinarNuevoNemo_doubleAsterisk_firstStage() throws Exception {
        // B-CUS-12: "**" branch — first put: ABNA12**02 → ABNA12 *02
        String result = CustodiaServiciosService.determinarNuevoNemo("ABNA12**02", "2018-12-15");
        assertThat(result).isEqualTo("ABNA12 *02");
    }

    @Test
    @DisplayName("B-CUS-13: determinarNuevoNemo — ' *' in nemo replaces with '01' (second stage)")
    void bCus13_determinarNuevoNemo_singleAsteriskSpace_secondStage() throws Exception {
        // B-CUS-13: " *" branch — second put: ABNA12 *02 → ABNA120102
        String result = CustodiaServiciosService.determinarNuevoNemo("ABNA12 *02", "2018-12-15");
        assertThat(result).isEqualTo("ABNA120102");
    }

    @Test
    @DisplayName("B-CUS-14: determinarNuevoNemo — '&&' in nemo replaces with ' &' (first stage)")
    void bCus14_determinarNuevoNemo_doubleAmpersand_firstStage() throws Exception {
        // B-CUS-14: "&&" branch — first put: ABNA12&&13 → ABNA12 &13
        String result = CustodiaServiciosService.determinarNuevoNemo("ABNA12&&13", "2018-12-15");
        assertThat(result).isEqualTo("ABNA12 &13");
    }

    @Test
    @DisplayName("B-CUS-15: determinarNuevoNemo — ' &', mesNemo(05) < mesProceso(12), uses anoProceso")
    void bCus15_determinarNuevoNemo_ampersandSpace_mesNemoLtMesProceso() throws Exception {
        // B-CUS-15: " &" branch, mesNemo < mesProceso path
        // nemo = "ABNA12 &05": chars[8..9]="05" → mesNemo="05"
        // fechaProceso = "2018-12-15": anoProceso="18", mesProceso="12"
        // 5 < 12 → anoProceso used directly
        // Step 1: replace(6,8,"05") on "ABNA12 &05" → "ABNA120505"
        // Step 2: replace(8,10,"18") → "ABNA120518"
        String result = CustodiaServiciosService.determinarNuevoNemo("ABNA12 &05", "2018-12-15");
        assertThat(result).isEqualTo("ABNA120518");
    }

    @Test
    @DisplayName("B-CUS-16: determinarNuevoNemo — ' &', anoProceso '00' yields '99' (century boundary)")
    void bCus16_determinarNuevoNemo_ampersandSpace_yearIs00or01() throws Exception {
        // B-CUS-16: " &" branch, mesNemo >= mesProceso, anoProceso "00" → "99"
        // nemo = "ABNA12 &13": chars[8..9]="13" → mesNemo="13"
        // fechaProceso = "2000-03-15": anoProceso="00", mesProceso="03"
        // 13 >= 3 → else branch; anoProceso "00" → "99"
        // Step 1: replace(6,8,"13") on "ABNA12 &13" → "ABNA121313"
        // Step 2: replace(8,10,"99") → "ABNA121399"
        String result = CustodiaServiciosService.determinarNuevoNemo("ABNA12 &13", "2000-03-15");
        assertThat(result).isEqualTo("ABNA121399");
    }

    @Test
    @DisplayName("B-CUS-17: determinarNuevoNemo — no marker, nemo returned unchanged")
    void bCus17_determinarNuevoNemo_noMarker_unchanged() throws Exception {
        // B-CUS-17: fall-through path — no "**", " *", "&&", " &" → identity
        String result = CustodiaServiciosService.determinarNuevoNemo("ABNA1234", "2018-12-15");
        assertThat(result).isEqualTo("ABNA1234");
    }

    // -----------------------------------------------------------------------
    // RULE-076  No main() in service classes; main() allowed only in Application
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("RULE-076: CustodiaServiciosService has no main() method (hardcoded credential vector removed)")
    void rule_RULE076_custodiaService_noMain() {
        // RULE-076: main() was a legacy entry point that contained hardcoded HANA credentials.
        // It must not exist in any modernized service class.
        boolean hasMain = Arrays.stream(CustodiaServiciosService.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("main"));
        assertThat(hasMain)
                .as("CustodiaServiciosService must not declare main()")
                .isFalse();
    }

    @Test
    @DisplayName("RULE-076: CustodiaServiciosRepositoryAdapter has no main() method")
    void rule_RULE076_repositoryAdapter_noMain() {
        // RULE-076: repository adapter classes must not declare main() methods
        boolean hasMain = Arrays.stream(CustodiaServiciosRepositoryAdapter.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("main"));
        assertThat(hasMain)
                .as("CustodiaServiciosRepositoryAdapter must not declare main()")
                .isFalse();
    }

    @Test
    @DisplayName("RULE-076: BcsCustodiaServiciosApplication MUST have main() (Spring Boot entry point)")
    void rule_RULE076_application_hasMain() {
        // RULE-076: main() IS allowed (and required) in the Spring Boot application class only
        boolean hasMain = Arrays.stream(BcsCustodiaServiciosApplication.class.getDeclaredMethods())
                .anyMatch(m -> m.getName().equals("main"));
        assertThat(hasMain)
                .as("BcsCustodiaServiciosApplication must declare main()")
                .isTrue();
    }

    // -----------------------------------------------------------------------
    // RULE-077  No hardcoded static cipher key (3DES key removed)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("RULE-077: CustodiaServiciosService has no hardcoded static byte[] cipher-key field")
    void rule_RULE077_custodiaService_noHardcodedCipherKey() {
        // RULE-077: legacy code carried a static 3DES key in service classes.
        // The modernized code must not declare any static byte[] field whose name
        // suggests a cipher key (contains "key" or "des", case-insensitive).
        boolean hasCipherKeyField = Arrays.stream(CustodiaServiciosService.class.getDeclaredFields())
                .anyMatch(f -> f.getType().equals(byte[].class)
                        && (f.getName().toLowerCase().contains("key")
                                || f.getName().toLowerCase().contains("des")));
        assertThat(hasCipherKeyField)
                .as("CustodiaServiciosService must not declare a hardcoded cipher key field (RULE-077)")
                .isFalse();
    }

    @Test
    @DisplayName("RULE-077: CustodiaServiciosRepositoryAdapter has no hardcoded static byte[] cipher-key field")
    void rule_RULE077_repositoryAdapter_noHardcodedCipherKey() {
        // RULE-077: same check on the adapter (which handles HANA I/O and could otherwise carry a key)
        boolean hasCipherKeyField = Arrays.stream(CustodiaServiciosRepositoryAdapter.class.getDeclaredFields())
                .anyMatch(f -> f.getType().equals(byte[].class)
                        && (f.getName().toLowerCase().contains("key")
                                || f.getName().toLowerCase().contains("des")));
        assertThat(hasCipherKeyField)
                .as("CustodiaServiciosRepositoryAdapter must not declare a hardcoded cipher key field (RULE-077)")
                .isFalse();
    }

    // -----------------------------------------------------------------------
    // B-CUS-18 / B-CUS-19  BLOCKCHAIN_ERROR_CODES constant
    // Legacy source: ConstantesCustodia.getCodigoErroresBlockChain()
    //
    // DISCREPANCY NOTE: task description says "23 entries" but the legacy code
    // executes 41 HashMap.put() calls with one duplicate key
    // ("Missing field: Arbitrator1" → BC_518 then overwritten by BC_529).
    // Runtime HashMap size is therefore 40, not 23. The oracle is the code.
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("B-CUS-18: BLOCKCHAIN_ERROR_CODES has 40 entries (41 puts, 1 duplicate key)")
    void bCus18_blockchainErrorCodes_has40Entries() {
        // B-CUS-18: legacy HashMap has 41 puts but "Missing field: Arbitrator1"
        // appears twice; the second put (BC_529) silently overwrites BC_518.
        // Oracle size = 40. Task description value of 23 is wrong (first batch only).
        assertThat(CustodiaServiciosService.BLOCKCHAIN_ERROR_CODES).hasSize(40);
    }

    @Test
    @DisplayName("B-CUS-19: BLOCKCHAIN_ERROR_CODES maps 'timeout expired while executing transaction' to BC_504")
    void bCus19_blockchainErrorCodes_timeoutMapsToBC504() {
        // B-CUS-19: specific entry — BC_504 is the timeout error code
        assertThat(CustodiaServiciosService.BLOCKCHAIN_ERROR_CODES)
                .containsEntry("timeout expired while executing transaction", "BC_504");
    }
}
