package cl.bcs.filters;

import cl.bcs.filters.filter.ServiceAccessFilter;
import cl.bcs.filters.filter.SessionValidationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Characterisation tests for the 6 mandatory security fixes.
 *
 * Each nested class documents the legacy bug, the fix applied, and verifies
 * the corrected behaviour is in effect.
 *
 * Database: H2 in-memory (profile "test") seeded by schema-filters.sql + data-filters.sql.
 * No @AutoConfigureMockMvc — MockMvc built manually via webAppContextSetup.
 */
@SpringBootTest
@ActiveProfiles("test")
class SecurityFixesTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private SessionValidationFilter sessionValidationFilter;

    @Autowired
    private ServiceAccessFilter serviceAccessFilter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Filters are registered as @Component but MockMvcBuilders.webAppContextSetup
        // does not apply them automatically — must be added explicitly.
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilters(sessionValidationFilter, serviceAccessFilter)
                .build();
    }

    // ------------------------------------------------------------------
    // RULE-004: channel bypass fix
    // Legacy bug: `ch != null` bypassed ALL service-access validation,
    // so any request with ANY channel value (e.g. ch=HACKER) skipped auth.
    // Fix: only channels in {WS, CLI, AGE} are whitelisted.
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("RULE-004 — channel bypass fix")
    class Rule004 {

        @Test
        @DisplayName("WS channel (whitelisted) passes through service-access check without grant")
        void ws_channel_bypasses_service_access() throws Exception {
            // Service 300 requires access check; valid-token-123 has NO grant for 300.
            // But WS is a trusted integration channel → service-access filter skips.
            // SessionValidationFilter still validates the token (valid-token-123 is valid).
            mockMvc.perform(get("/api/status")
                            .queryParam("t", "300")
                            .queryParam("ch", "WS")
                            .header("X-SESSION-TOKEN", "valid-token-123"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Unknown channel (FOO) does NOT bypass service-access check — fix confirmed")
        void unknown_channel_does_not_bypass_service_access() throws Exception {
            // RULE-004 FIX: ch=FOO is not in {WS, CLI, AGE} → access check is performed.
            // valid-token-123 has NO grant for service 300 → 403 Forbidden.
            // Legacy bug would have returned 200 because ch != null was the condition.
            mockMvc.perform(get("/api/status")
                            .queryParam("t", "300")
                            .queryParam("ch", "FOO")
                            .header("X-SESSION-TOKEN", "valid-token-123"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("No channel — service requiring access check enforces it")
        void no_channel_with_valid_grant_is_allowed() throws Exception {
            // Service 200; valid-token-123 HAS a grant for 200 → allowed
            mockMvc.perform(get("/api/status")
                            .queryParam("t", "200")
                            .header("X-SESSION-TOKEN", "valid-token-123"))
                    .andExpect(status().isOk());
        }
    }

    // ------------------------------------------------------------------
    // RULE-005: fail-closed on exceptions (tested via unit test)
    // Integration test verifies baseline: invalid token → 401 (not a pass).
    // The unit test in ServiceAccessFilterUnitTest covers the exception path.
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("RULE-005 — fail-closed on exceptions")
    class Rule005 {

        @Test
        @DisplayName("Invalid token returns 401 (not 200)")
        void invalid_token_denied() throws Exception {
            mockMvc.perform(get("/api/status")
                            .queryParam("t", "100")
                            .header("X-SESSION-TOKEN", "invalid-token"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Missing token returns 400")
        void missing_token_returns_400() throws Exception {
            mockMvc.perform(get("/api/status").queryParam("t", "100"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ------------------------------------------------------------------
    // RULE-018: entities with estado BLO cannot log in
    // Legacy bug: only checked if user existed, not that estado == "ACT".
    // Fix: throw DisabledException when estado != "ACT".
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("RULE-018 — BLO entities cannot log in")
    class Rule018 {

        @Test
        @DisplayName("Estado ACT — login succeeds and returns a token")
        void estado_ACT_login_succeeds() throws Exception {
            mockMvc.perform(post("/PLA/CrearSesion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"codigo":"usuario_act","clave":"secret123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty());
        }

        @Test
        @DisplayName("Estado BLO — login is denied with 401 (RULE-018 fix)")
        void estado_BLO_login_denied() throws Exception {
            // RULE-018 FIX: BLO users cannot authenticate.
            // Legacy allowed them through because it did not check the estado field.
            mockMvc.perform(post("/PLA/CrearSesion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"codigo":"usuario_blo","clave":"secret123"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ------------------------------------------------------------------
    // RULE-025: COR sessions expire after configurable timeout
    // Legacy created COR sessions with no expiry.
    // Fix: SessionValidationFilter checks fechaCreacion + timeout.
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("RULE-025 — COR session timeout")
    class Rule025 {

        @Test
        @DisplayName("Recent COR session (within timeout) is accepted")
        void recent_cor_session_accepted() throws Exception {
            mockMvc.perform(get("/api/status")
                            .queryParam("t", "100")
                            .header("X-SESSION-TOKEN", "valid-cor-token"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Expired COR session (> 30 min old) is rejected with 401 (RULE-025 fix)")
        void expired_cor_session_rejected() throws Exception {
            // RULE-025 FIX: token "expired-cor-token" was created 61 min ago.
            // Legacy would accept it (no timeout logic). Fix returns 401.
            mockMvc.perform(get("/api/status")
                            .queryParam("t", "100")
                            .header("X-SESSION-TOKEN", "expired-cor-token"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ------------------------------------------------------------------
    // RULE-040: provisional password off-by-one fix
    // Legacy locked at intentos > maxIntentos (i.e. at attempt max+1).
    // Fix: lock at intentos >= maxIntentos.
    // Test user "usuario_max" has intentos=3 and maxIntentos=3 (from config).
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("RULE-040 — password attempts off-by-one fix")
    class Rule040 {

        @Test
        @DisplayName("intentos == maxIntentos → account locked (>= fix prevents extra attempt)")
        void intentos_equal_max_is_locked() throws Exception {
            // RULE-040 FIX: usuario_max has intentos=3, maxIntentos=3.
            // Legacy (> max) would allow one more attempt. Fix (>= max) locks immediately.
            mockMvc.perform(post("/PLA/CrearSesion")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"codigo":"usuario_max","clave":"secret123"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ------------------------------------------------------------------
    // RULE-045: SSO login path validates FechaVigencia
    // Legacy only checked FechaVigencia in the local login path.
    // Fix: SsoService.handleCallback() also validates expiry.
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("RULE-045 — SSO path validates FechaVigencia")
    class Rule045 {

        @Test
        @DisplayName("SSO callback with active valid account succeeds")
        void sso_active_account_succeeds() throws Exception {
            // First get a valid state
            MvcResult initResult = mockMvc.perform(get("/PLA/SSOInit"))
                    .andExpect(status().isOk())
                    .andReturn();
            String body = initResult.getResponse().getContentAsString();
            String state = extractJsonValue(body, "state");

            // Callback with active non-expired user
            mockMvc.perform(get("/PLA/SSOLogon")
                            .queryParam("code", "dummy-oauth-code")
                            .queryParam("state", state)
                            .queryParam("codigoUsuario", "usuario_act"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isNotEmpty());
        }

        @Test
        @DisplayName("SSO callback with expired account returns 401 (RULE-045 fix)")
        void sso_expired_account_rejected() throws Exception {
            // First get a valid state
            MvcResult initResult = mockMvc.perform(get("/PLA/SSOInit"))
                    .andExpect(status().isOk())
                    .andReturn();
            String body = initResult.getResponse().getContentAsString();
            String state = extractJsonValue(body, "state");

            // RULE-045 FIX: usuario_exp has FechaVigencia yesterday → must be rejected.
            // Legacy SSO path skipped this check.
            mockMvc.perform(get("/PLA/SSOLogon")
                            .queryParam("code", "dummy-oauth-code")
                            .queryParam("state", state)
                            .queryParam("codigoUsuario", "usuario_exp"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ------------------------------------------------------------------
    // RULE-046: SSO CSRF state is random UUID, validated in callback
    // Legacy used a hardcoded state string ("fee7e77...").
    // Fix: UUID.randomUUID().toString(); callback validates and consumes.
    // ------------------------------------------------------------------
    @Nested
    @DisplayName("RULE-046 — SSO CSRF state is UUID, validated in callback")
    class Rule046 {

        private static final Pattern UUID_PATTERN =
                Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");

        @Test
        @DisplayName("SSOInit returns a valid UUID state (not a hardcoded constant)")
        void ssoinit_returns_uuid_state() throws Exception {
            MvcResult result = mockMvc.perform(get("/PLA/SSOInit"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.state").isNotEmpty())
                    .andReturn();
            String state = extractJsonValue(result.getResponse().getContentAsString(), "state");
            // RULE-046 FIX: state must be a UUID, not a hardcoded "fee7e77..." string
            assertThat(UUID_PATTERN.matcher(state).matches())
                    .as("state must be a UUID; legacy used a hardcoded constant")
                    .isTrue();
        }

        @Test
        @DisplayName("Two SSOInit calls produce different state values (randomness)")
        void ssoinit_produces_unique_states() throws Exception {
            MvcResult r1 = mockMvc.perform(get("/PLA/SSOInit")).andReturn();
            MvcResult r2 = mockMvc.perform(get("/PLA/SSOInit")).andReturn();
            String s1 = extractJsonValue(r1.getResponse().getContentAsString(), "state");
            String s2 = extractJsonValue(r2.getResponse().getContentAsString(), "state");
            assertThat(s1).isNotEqualTo(s2);
        }

        @Test
        @DisplayName("Callback with wrong state returns 401 (RULE-046 CSRF validation)")
        void sso_callback_wrong_state_rejected() throws Exception {
            // RULE-046 FIX: any state value not previously stored via SSOInit is rejected.
            mockMvc.perform(get("/PLA/SSOLogon")
                            .queryParam("code", "any-code")
                            .queryParam("state", "hardcoded-or-forged-state")
                            .queryParam("codigoUsuario", "usuario_act"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("State is single-use — replay of a consumed state returns 401")
        void sso_state_is_single_use() throws Exception {
            MvcResult initResult = mockMvc.perform(get("/PLA/SSOInit"))
                    .andExpect(status().isOk()).andReturn();
            String state = extractJsonValue(initResult.getResponse().getContentAsString(), "state");

            // First use — succeeds
            mockMvc.perform(get("/PLA/SSOLogon")
                            .queryParam("code", "code1")
                            .queryParam("state", state)
                            .queryParam("codigoUsuario", "usuario_act"))
                    .andExpect(status().isOk());

            // Replay — must be rejected (state was consumed)
            mockMvc.perform(get("/PLA/SSOLogon")
                            .queryParam("code", "code1")
                            .queryParam("state", state)
                            .queryParam("codigoUsuario", "usuario_act"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // --- helpers ---

    private static String extractJsonValue(String json, String key) {
        // Minimal JSON field extractor — avoids a Jackson dependency in test helpers.
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) throw new AssertionError("Key '" + key + "' not found in: " + json);
        start += search.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }
}
