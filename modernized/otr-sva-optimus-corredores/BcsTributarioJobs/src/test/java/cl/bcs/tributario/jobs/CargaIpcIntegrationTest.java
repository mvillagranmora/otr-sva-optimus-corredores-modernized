package cl.bcs.tributario.jobs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the tributario-jobs REST layer using an H2 in-memory
 * database.
 *
 * <p>HANA stored-procedure tests are {@code @Disabled} because they require a
 * live SAP HANA environment with the corresponding stored procedure installed.</p>
 *
 * <p>Note: {@code @AutoConfigureMockMvc} was removed in Spring Boot 4.1.1.
 * MockMvc is constructed manually via
 * {@link MockMvcBuilders#webAppContextSetup(WebApplicationContext)} in
 * {@code @BeforeEach}.</p>
 *
 * <p>Equivalence cases executed (enabled): 2</p>
 * <p>Equivalence cases disabled (HANA SP): 1</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CargaIpc Integration — H2 context / REST layer")
class CargaIpcIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    // =========================================================================
    // IT-TRI-01: SP endpoint returns 503 when HANA unavailable
    // =========================================================================

    @Test
    @DisplayName("IT-TRI-01: POST /carga-ipc with valid params → 503 (HANA SP unavailable in H2)")
    void cargaIpc_withValidParams_returns503WhenHanaUnavailable() throws Exception {
        mockMvc.perform(post("/api/tributario/jobs/carga-ipc")
                        .param("idEmpresa", "1")
                        .param("idUsuario", "3")
                        .param("idModulo", "TRI")
                        .param("simularOperacion", "")
                        .param("usaReglasNegocio", ""))
                .andExpect(status().isServiceUnavailable());
    }

    // =========================================================================
    // IT-TRI-02: missing required parameter → 400
    // =========================================================================

    @Test
    @DisplayName("IT-TRI-02: POST /carga-ipc without idModulo → 400 Bad Request")
    void cargaIpc_withoutIdModulo_returns400() throws Exception {
        mockMvc.perform(post("/api/tributario/jobs/carga-ipc")
                        .param("idEmpresa", "1")
                        .param("idUsuario", "3"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // IT-TRI-03 [DISABLED]: real HANA SP execution
    // =========================================================================

    @Test
    @Disabled("requires HANA SP: BSCL.Normativa.BD.Servicios::TRI.CargarValorFactorIpc")
    @DisplayName("IT-TRI-03 [DISABLED]: POST /carga-ipc → 200 with actual HANA SP execution")
    void cargaIpc_withLiveHana_returns200() throws Exception {
        mockMvc.perform(post("/api/tributario/jobs/carga-ipc")
                        .param("idEmpresa", "1")
                        .param("idUsuario", "3")
                        .param("idModulo", "TRI")
                        .param("simularOperacion", "")
                        .param("usaReglasNegocio", ""))
                .andExpect(status().isOk());
    }
}
