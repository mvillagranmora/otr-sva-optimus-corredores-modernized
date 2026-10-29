package cl.bcs.jobmanager;

import cl.bcs.jobmanager.domain.Programa;
import cl.bcs.jobmanager.service.JobManagerService;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization tests for pure-logic JobManagerService methods.
 * No Spring context required.
 */
class JobManagerServiceTest {

    // -----------------------------------------------------------------------
    // computeDelayMillis
    // -----------------------------------------------------------------------

    @Test
    void computeDelayMillis_wildcardReturns5000() {
        assertThat(JobManagerService.computeDelayMillis("**")).isEqualTo(5_000L);
    }

    @Test
    void computeDelayMillis_wildcardCaseInsensitive() {
        assertThat(JobManagerService.computeDelayMillis("**")).isEqualTo(5_000L);
    }

    @Test
    void computeDelayMillis_numericMinutesConvertsToMs() {
        // 2 minutes = 120_000 ms
        assertThat(JobManagerService.computeDelayMillis("2")).isEqualTo(120_000L);
    }

    @Test
    void computeDelayMillis_zeroMinutesDefaultsTo5000() {
        assertThat(JobManagerService.computeDelayMillis("0")).isEqualTo(5_000L);
    }

    @Test
    void computeDelayMillis_negativeMinutesDefaultsTo5000() {
        assertThat(JobManagerService.computeDelayMillis("-1")).isEqualTo(5_000L);
    }

    @Test
    void computeDelayMillis_invalidStringDefaultsTo5000() {
        assertThat(JobManagerService.computeDelayMillis("abc")).isEqualTo(5_000L);
    }

    @Test
    void computeDelayMillis_nullThrowsNPE() {
        assertThatThrownBy(() -> JobManagerService.computeDelayMillis(null))
                .isInstanceOf(NullPointerException.class);
    }

    // -----------------------------------------------------------------------
    // truncateDescription
    // -----------------------------------------------------------------------

    @Test
    void truncateDescription_shortStringUnchanged() {
        String s = "OK";
        assertThat(JobManagerService.truncateDescription(s)).isEqualTo(s);
    }

    @Test
    void truncateDescription_exactly2000CharUnchanged() {
        String s = "x".repeat(2000);
        assertThat(JobManagerService.truncateDescription(s)).hasSize(2000);
    }

    @Test
    void truncateDescription_over2000CharTruncated() {
        String s = "y".repeat(2500);
        assertThat(JobManagerService.truncateDescription(s)).hasSize(2000);
    }

    @Test
    void truncateDescription_nullThrowsNPE() {
        assertThatThrownBy(() -> JobManagerService.truncateDescription(null))
                .isInstanceOf(NullPointerException.class);
    }

    // -----------------------------------------------------------------------
    // isExhausted
    // -----------------------------------------------------------------------

    @Test
    void isExhausted_whenIntentoEqualsMaximo_returnsTrue() {
        Programa p = new Programa();
        p.setIntento(3);
        p.setMaximoIntentos(3);
        assertThat(JobManagerService.isExhausted(p)).isTrue();
    }

    @Test
    void isExhausted_whenIntentoLessThanMaximo_returnsFalse() {
        Programa p = new Programa();
        p.setIntento(1);
        p.setMaximoIntentos(3);
        assertThat(JobManagerService.isExhausted(p)).isFalse();
    }

    @Test
    void isExhausted_nullThrowsNPE() {
        assertThatThrownBy(() -> JobManagerService.isExhausted(null))
                .isInstanceOf(NullPointerException.class);
    }

    // -----------------------------------------------------------------------
    // normalizeName
    // -----------------------------------------------------------------------

    @Test
    void normalizeName_stripsHyphens() {
        assertThat(JobManagerService.normalizeName("ORD-01")).isEqualTo("ORD01");
    }

    @Test
    void normalizeName_starBecomesEmpty() {
        assertThat(JobManagerService.normalizeName("*")).isEmpty();
    }

    @Test
    void normalizeName_dashOnlyBecomesEmpty() {
        assertThat(JobManagerService.normalizeName("-")).isEmpty();
    }

    @Test
    void normalizeName_emptyStringStaysEmpty() {
        assertThat(JobManagerService.normalizeName("")).isEmpty();
    }

    // -----------------------------------------------------------------------
    // sanitizeParams
    // -----------------------------------------------------------------------

    @Test
    void sanitizeParams_nullKeyAndValueReplacedWithEmpty() {
        Map<String, String> raw = new HashMap<>();
        raw.put(null, null);
        Map<String, String> result = JobManagerService.sanitizeParams(raw);
        assertThat(result).containsEntry("", "");
    }

    @Test
    void sanitizeParams_normalEntryPreserved() {
        Map<String, String> raw = Map.of("KEY", "VALUE");
        assertThat(JobManagerService.sanitizeParams(raw)).containsEntry("KEY", "VALUE");
    }

    @Test
    void sanitizeParams_nullMapThrowsNPE() {
        assertThatThrownBy(() -> JobManagerService.sanitizeParams(null))
                .isInstanceOf(NullPointerException.class);
    }
}
