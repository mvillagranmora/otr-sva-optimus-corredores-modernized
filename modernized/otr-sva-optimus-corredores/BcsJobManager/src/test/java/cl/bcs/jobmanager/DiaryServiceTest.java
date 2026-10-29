package cl.bcs.jobmanager;

import cl.bcs.jobmanager.domain.Agenda;
import cl.bcs.jobmanager.service.DiaryService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Characterization tests for pure-logic DiaryService methods.
 * No Spring context required.
 */
class DiaryServiceTest {

    // -----------------------------------------------------------------------
    // compareDates
    // -----------------------------------------------------------------------

    @Test
    void compareDates_nextBeforeLast_returnsTrue() {
        // next execution (2024-01-01) is before last execution (2024-01-02) → due
        assertThat(DiaryService.compareDates("2024-01-01 00:00:00", "2024-01-02 00:00:00"))
                .isTrue();
    }

    @Test
    void compareDates_nextAfterLast_returnsFalse() {
        // next execution (2024-01-03) is after last execution (2024-01-02) → not due
        assertThat(DiaryService.compareDates("2024-01-03 00:00:00", "2024-01-02 00:00:00"))
                .isFalse();
    }

    @Test
    void compareDates_nextEqualsLast_returnsTrue() {
        // equal timestamps → considered due (matches legacy behavior)
        assertThat(DiaryService.compareDates("2024-01-01 12:00:00", "2024-01-01 12:00:00"))
                .isTrue();
    }

    @Test
    void compareDates_nullFecha1ThrowsNPE() {
        assertThatThrownBy(() -> DiaryService.compareDates(null, "2024-01-01 00:00:00"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void compareDates_nullFechaActualThrowsNPE() {
        assertThatThrownBy(() -> DiaryService.compareDates("2024-01-01 00:00:00", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void compareDates_invalidFormatReturnsFalse() {
        // Bad format should not throw — returns false gracefully
        assertThat(DiaryService.compareDates("not-a-date", "also-not-a-date"))
                .isFalse();
    }

    // -----------------------------------------------------------------------
    // shouldTrigger
    // -----------------------------------------------------------------------

    @Test
    void shouldTrigger_nullDates_returnsFalse() {
        Agenda a = new Agenda();
        a.setHabilitado("S");
        a.setSiguienteFechaEjecucion(null);
        a.setUltimaFechaEjecucion(null);
        assertThat(DiaryService.shouldTrigger(a)).isFalse();
    }

    @Test
    void shouldTrigger_nextBeforeLast_returnsTrue() {
        Agenda a = new Agenda();
        a.setHabilitado("S");
        a.setSiguienteFechaEjecucion("2024-01-01 00:00:00");
        a.setUltimaFechaEjecucion("2024-01-02 00:00:00");
        assertThat(DiaryService.shouldTrigger(a)).isTrue();
    }

    @Test
    void shouldTrigger_nextAfterLast_returnsFalse() {
        Agenda a = new Agenda();
        a.setHabilitado("S");
        a.setSiguienteFechaEjecucion("2024-12-31 00:00:00");
        a.setUltimaFechaEjecucion("2024-01-01 00:00:00");
        assertThat(DiaryService.shouldTrigger(a)).isFalse();
    }

    @Test
    void shouldTrigger_nullAgendaThrowsNPE() {
        assertThatThrownBy(() -> DiaryService.shouldTrigger(null))
                .isInstanceOf(NullPointerException.class);
    }

    // -----------------------------------------------------------------------
    // isDaemon / isEnabled
    // -----------------------------------------------------------------------

    @Test
    void isDaemon_esDemonioS_returnsTrue() {
        Agenda a = new Agenda();
        a.setEsDemonio("S");
        assertThat(DiaryService.isDaemon(a)).isTrue();
    }

    @Test
    void isDaemon_esDemonioN_returnsFalse() {
        Agenda a = new Agenda();
        a.setEsDemonio("N");
        assertThat(DiaryService.isDaemon(a)).isFalse();
    }

    @Test
    void isEnabled_habilitadoS_returnsTrue() {
        Agenda a = new Agenda();
        a.setHabilitado("S");
        assertThat(DiaryService.isEnabled(a)).isTrue();
    }

    @Test
    void isEnabled_habilitadoN_returnsFalse() {
        Agenda a = new Agenda();
        a.setHabilitado("N");
        assertThat(DiaryService.isEnabled(a)).isFalse();
    }
}
