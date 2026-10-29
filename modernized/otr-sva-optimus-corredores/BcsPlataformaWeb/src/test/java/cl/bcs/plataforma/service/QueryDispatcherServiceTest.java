package cl.bcs.plataforma.service;

import org.junit.jupiter.api.Test;

import static cl.bcs.plataforma.service.QueryDispatcherService.translateFilter;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for OData filter translation logic — no HANA required.
 */
class QueryDispatcherServiceTest {

    @Test
    void eq_translates_to_equals() {
        assertThat(translateFilter("IdFondo eq 1")).isEqualTo("\"IdFondo\" = 1");
    }

    @Test
    void ne_translates_to_not_equals() {
        assertThat(translateFilter("IdEstado ne 'I'")).isEqualTo("\"IdEstado\" <> 'I'");
    }

    @Test
    void gt_and_lt_translate() {
        assertThat(translateFilter("Monto gt 0")).isEqualTo("\"Monto\" > 0");
        assertThat(translateFilter("Monto lt 100")).isEqualTo("\"Monto\" < 100");
    }

    @Test
    void ge_le_translate() {
        assertThat(translateFilter("Cantidad ge 5")).isEqualTo("\"Cantidad\" >= 5");
        assertThat(translateFilter("Cantidad le 10")).isEqualTo("\"Cantidad\" <= 10");
    }

    @Test
    void and_logical_operator() {
        String result = translateFilter("IdEstado eq 'A' and IdFondo eq 1");
        assertThat(result).contains(" AND ").contains("\"IdEstado\" = 'A'").contains("\"IdFondo\" = 1");
    }

    @Test
    void or_logical_operator() {
        String result = translateFilter("IdEstado eq 'A' or IdEstado eq 'P'");
        assertThat(result).contains(" OR ");
    }

    @Test
    void startswith_translates_to_like() {
        assertThat(translateFilter("startswith(Nombre,'Juan')")).isEqualTo("\"Nombre\" LIKE 'Juan%'");
    }

    @Test
    void endswith_translates_to_like() {
        assertThat(translateFilter("endswith(Codigo,'01')")).isEqualTo("\"Codigo\" LIKE '%01'");
    }

    @Test
    void substringof_translates_to_like() {
        assertThat(translateFilter("substringof('Ana',Nombre)")).isEqualTo("\"Nombre\" LIKE '%Ana%'");
    }

    @Test
    void null_filter_returns_null() {
        assertThat(translateFilter(null)).isNull();
        assertThat(translateFilter("")).isNull();
        assertThat(translateFilter("   ")).isNull();
    }
}
