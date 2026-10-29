package cl.bcs.normativa.jobs;

import cl.bcs.normativa.jobs.infrastructure.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GlobalExceptionHandler error mappings.
 *
 * Mandatory pattern: NPE→400, IAE→400, NFE→400, UOE→503
 */
@DisplayName("GlobalExceptionHandler — error mappings")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("NJB-EX-01: NullPointerException → 400 BAD_REQUEST")
    void npe_returns400() {
        var response = handler.handleNpe(new NullPointerException("null value"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsKey("error");
        assertThat(response.getBody().get("error")).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("NJB-EX-02: IllegalArgumentException → 400 BAD_REQUEST")
    void iae_returns400() {
        var response = handler.handleIae(new IllegalArgumentException("bad param"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().get("message")).isEqualTo("bad param");
    }

    @Test
    @DisplayName("NJB-EX-03: NumberFormatException → 400 BAD_REQUEST")
    void nfe_returns400() {
        var response = handler.handleNfe(new NumberFormatException("abc"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("NJB-EX-04: UnsupportedOperationException → 503 SERVICE_UNAVAILABLE")
    void uoe_returns503() {
        var response = handler.handleUoe(new UnsupportedOperationException("requires HANA"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().get("error")).isEqualTo("SERVICE_UNAVAILABLE");
        assertThat(response.getBody().get("message")).contains("HANA");
    }

    @Test
    @DisplayName("NJB-EX-05: NullPointerException with null message returns fallback message")
    void npe_nullMessage_returnsFallback() {
        var response = handler.handleNpe(new NullPointerException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).isNotBlank();
    }
}
