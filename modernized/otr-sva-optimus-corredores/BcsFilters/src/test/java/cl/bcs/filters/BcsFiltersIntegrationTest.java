package cl.bcs.filters;

import cl.bcs.filters.filter.ServiceAccessFilter;
import cl.bcs.filters.filter.SessionValidationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * General integration tests for BcsFilters.
 *
 * Verifies basic filter wiring, whitelisting, and happy-path behaviour.
 * Security-fix-specific tests live in {@link SecurityFixesTest}.
 *
 * No @AutoConfigureMockMvc — MockMvc is built manually via webAppContextSetup.
 */
@SpringBootTest
@ActiveProfiles("test")
class BcsFiltersIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SessionValidationFilter sessionValidationFilter;

    @Autowired
    private ServiceAccessFilter serviceAccessFilter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilters(sessionValidationFilter, serviceAccessFilter)
                .build();
    }

    // --- whitelisted endpoints (no token required) ---

    @Test
    @DisplayName("POST /PLA/CrearSesion is accessible without a token (whitelisted)")
    void login_endpoint_is_whitelisted() throws Exception {
        // Sending wrong credentials — 401 from business logic, not 400 from filter
        mockMvc.perform(post("/PLA/CrearSesion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"nobody","clave":"bad"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /PLA/SSOInit is accessible without a token (whitelisted)")
    void sso_init_is_whitelisted() throws Exception {
        mockMvc.perform(get("/PLA/SSOInit"))
                .andExpect(status().isOk());
    }

    // --- protected endpoint ---

    @Test
    @DisplayName("GET /api/status without token returns 400 (SessionValidationFilter)")
    void status_without_token_returns_400() throws Exception {
        mockMvc.perform(get("/api/status").queryParam("t", "100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/status with valid token and no-validation service returns 200")
    void status_with_valid_token_and_open_service() throws Exception {
        // Service 100 has VALIDA_ACCESO_SERVICIO='N' → no per-user check
        mockMvc.perform(get("/api/status")
                        .queryParam("t", "100")
                        .header("X-SESSION-TOKEN", "valid-token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    @DisplayName("GET /api/status with valid token, service 200, and grant returns 200")
    void status_with_valid_token_and_service_grant() throws Exception {
        mockMvc.perform(get("/api/status")
                        .queryParam("t", "200")
                        .header("X-SESSION-TOKEN", "valid-token-123"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/status with valid token, service 300, no grant → 403")
    void status_with_valid_token_but_no_service_grant() throws Exception {
        mockMvc.perform(get("/api/status")
                        .queryParam("t", "300")
                        .header("X-SESSION-TOKEN", "valid-token-123"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("No service param (t=null) passes service-access filter through")
    void no_service_param_passes_filter() throws Exception {
        // When t is absent the ServiceAccessFilter treats it as no-validation-needed
        mockMvc.perform(get("/api/status")
                        .header("X-SESSION-TOKEN", "valid-token-123"))
                .andExpect(status().isOk());
    }

    // --- GlobalExceptionHandler ---

    @Test
    @DisplayName("POST /PLA/CrearSesion with missing required fields → 400")
    void missing_body_fields_returns_400() throws Exception {
        mockMvc.perform(post("/PLA/CrearSesion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
