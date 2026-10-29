package cl.bcs.portalconfirmacion;

import cl.bcs.portalconfirmacion.adapter.out.NotificationJdbcAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for BcsPortalConfirmacionWeb backed by H2 in-memory database.
 *
 * Spring Boot 4.1.1 removed @AutoConfigureMockMvc — MockMvc is built manually
 * via MockMvcBuilders.webAppContextSetup() in @BeforeEach.
 *
 * HANA stored-procedure operations (validateToken, validateTokenAndId) are @Disabled.
 * H2-compatible operations (getConfirmationMacro, getUrlHana, acceptOrReject) are tested directly
 * via the adapter and also via the controller path where possible.
 *
 * Covers:
 *   B-CONF-INT-01  GET /validate without token → 400
 *   B-CONF-INT-02  POST /validate without token → 400
 *   B-CONF-INT-03  GET /validate with token → 503 (validateToken requires HANA) [disabled]
 *   B-CONF-INT-04  acceptOrReject() H2: updates EVT_NOTIFICACION
 *   B-CONF-INT-05  acceptOrReject() H2: returns -1 for unknown token
 *   B-CONF-INT-06  getConfirmationMacro() H2: returns stored value
 *   B-CONF-INT-07  getUrlHana() H2: returns configured URL
 *   B-CONF-INT-08  getUrlHana() H2: returns null for unknown empresa
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PortalConfirmacionIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private NotificationJdbcAdapter adapter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Spring Boot 4.1.1: no @AutoConfigureMockMvc — build manually
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // ----------------------------------------------------------
    // B-CONF-INT-01/02  Missing token → 400
    // ----------------------------------------------------------

    @Test
    void validate_get_missingToken_returns400() throws Exception {
        mockMvc.perform(get("/validate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    @Test
    void validate_post_missingToken_returns400() throws Exception {
        mockMvc.perform(post("/validate"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"));
    }

    // ----------------------------------------------------------
    // B-CONF-INT-03  Token provided → validateToken requires HANA → 503
    // ----------------------------------------------------------

    @Disabled("requires HANA: EVT.ValidarTokenNotificacion stored procedure")
    @Test
    void validate_withToken_requiresHana_returns503() throws Exception {
        mockMvc.perform(get("/validate").param("token", "any-token"))
                .andExpect(status().isServiceUnavailable());
    }

    @Disabled("requires HANA: full validate flow with rut and accion")
    @Test
    void validate_acceptNotification_requiresHana() throws Exception {
        mockMvc.perform(post("/validate")
                        .param("token", "TOKEN-001")
                        .param("rut", "12345678-9")
                        .param("accion", "1")
                        .param("observacion", "Aprobado"))
                .andExpect(status().isServiceUnavailable());
    }

    // ----------------------------------------------------------
    // B-CONF-INT-04  acceptOrReject() H2: updates notification
    // ----------------------------------------------------------

    @Test
    void acceptOrReject_existingToken_updatesAndReturns1() {
        int result = adapter.acceptOrReject("TOKEN-001", "Observacion de prueba", "ACP");
        assertThat(result).isEqualTo(1);
    }

    // ----------------------------------------------------------
    // B-CONF-INT-05  acceptOrReject() H2: unknown token → -1
    // ----------------------------------------------------------

    @Test
    void acceptOrReject_unknownToken_returnsMinus1() {
        int result = adapter.acceptOrReject("TOKEN-UNKNOWN", "obs", "RZD");
        assertThat(result).isEqualTo(-1);
    }

    // ----------------------------------------------------------
    // B-CONF-INT-06  getConfirmationMacro() H2
    // ----------------------------------------------------------

    @Test
    void getConfirmationMacro_returnsStoredValue() {
        String macro = adapter.getConfirmationMacro();
        assertThat(macro).isNotNull().isNotEmpty();
        assertThat(macro).contains("confirmacion");
    }

    // ----------------------------------------------------------
    // B-CONF-INT-07  getUrlHana() H2: known empresa
    // ----------------------------------------------------------

    @Test
    void getUrlHana_empresa1_returnsConfiguredUrl() {
        String url = adapter.getUrlHana(1L);
        assertThat(url).isEqualTo("http://bcs-platform-test.internal");
    }

    // ----------------------------------------------------------
    // B-CONF-INT-08  getUrlHana() H2: unknown empresa → null
    // ----------------------------------------------------------

    @Test
    void getUrlHana_unknownEmpresa_returnsNull() {
        String url = adapter.getUrlHana(9999L);
        assertThat(url).isNull();
    }
}
