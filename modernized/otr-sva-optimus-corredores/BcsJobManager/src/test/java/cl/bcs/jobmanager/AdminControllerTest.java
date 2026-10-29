package cl.bcs.jobmanager;

import cl.bcs.jobmanager.config.TestDataSourceConfig;
import cl.bcs.jobmanager.port.out.NodoPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Characterization tests for AdminController.
 *
 * Uses MockMvcBuilders.webAppContextSetup (not @AutoConfigureMockMvc —
 * removed in Spring Boot 4.1.1).
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestDataSourceConfig.class)
class AdminControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @MockitoBean
    private NodoPort nodoPort;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void health_dbUp_returns200() throws Exception {
        when(nodoPort.pingDb()).thenReturn(true);

        mockMvc.perform(get("/api/admin/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.db").value("connected"));
    }

    @Test
    void health_dbDown_returns503() throws Exception {
        when(nodoPort.pingDb()).thenReturn(false);

        mockMvc.perform(get("/api/admin/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"));
    }

    @Test
    void zombies_returnsListAndCount() throws Exception {
        when(nodoPort.getZombieNodos(30, false)).thenReturn(List.of(1001, 1002));

        mockMvc.perform(get("/api/admin/zombies?timeoutSeconds=30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    void zombies_invalidTimeout_returns400() throws Exception {
        mockMvc.perform(get("/api/admin/zombies?timeoutSeconds=0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void retry_endpointNoLongerExists() throws Exception {
        mockMvc.perform(post("/api/admin/retry"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void globalExceptionHandler_npe_returns400() throws Exception {
        when(nodoPort.pingDb()).thenThrow(new NullPointerException("test npe"));

        mockMvc.perform(get("/api/admin/health"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("null value"));
    }
}
