package cl.bcs.normativa.svc.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests para CircularesController.
 *
 * <p>Los endpoints de circulares son stubs que retornan 503.
 * Se verifica que el GlobalExceptionHandler mapea correctamente UnsupportedOperationException.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CircularesControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    @Disabled("requires HANA")
    @DisplayName("POST /generar-archivo-circular-1720 retorna 503 (stub pendiente HANA)")
    void generarArchivoCircular1720_retorna503() throws Exception {
        mockMvc.perform(post("/api/normativa/circulares/generar-archivo-circular-1720")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    @Disabled("requires HANA")
    @DisplayName("POST /generar-ncg-380 retorna 503 (stub pendiente HANA)")
    void generarNCG380_retorna503() throws Exception {
        mockMvc.perform(post("/api/normativa/circulares/generar-ncg-380")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isServiceUnavailable());
    }
}
