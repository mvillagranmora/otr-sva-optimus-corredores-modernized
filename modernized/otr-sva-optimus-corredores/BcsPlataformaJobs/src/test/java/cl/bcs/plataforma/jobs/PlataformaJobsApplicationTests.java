package cl.bcs.plataforma.jobs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Canary integration test — verifies application context loads and
 * health endpoint is reachable.
 *
 * Uses H2 in-memory DB (@ActiveProfiles("test")).
 * No @AutoConfigureMockMvc — uses MockMvcBuilders.webAppContextSetup per mandate.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("PlataformaJobs — application canary")
class PlataformaJobsApplicationTests {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    @DisplayName("PLJ-CANARY-01: Spring context loads without errors")
    void contextLoads() {
        assertThat(wac).isNotNull();
    }

    @Test
    @DisplayName("PLJ-CANARY-02: Health endpoint returns UP")
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/plataforma-jobs/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("plataforma-jobs"));
    }
}
