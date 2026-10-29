package cl.bcs.plataforma;

import cl.bcs.plataforma.adapter.in.rest.ConvertirMontoEscritoController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for BcsPlataformaServicios.
 * Uses H2 in-memory database; HANA-specific services are covered by unit tests.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlataformaApplicationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    void contextLoads() {
        // Verifies the Spring context starts successfully with H2 datasource
    }

    @Test
    void health_endpoint_returns_ok() throws Exception {
        mockMvc.perform(get("/api/plataforma/health"))
            .andExpect(status().isOk());
    }

    @Test
    void conversor_letras_retorna_resultado() throws Exception {
        mockMvc.perform(post("/api/plataforma/conversor-monto/letras")
                .contentType("application/json")
                .content("{\"valor\": \"1234\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.datos.valorEscrito").isNotEmpty());
    }

    @Test
    void conversor_letras_cero() throws Exception {
        mockMvc.perform(post("/api/plataforma/conversor-monto/letras")
                .contentType("application/json")
                .content("{\"valor\": \"0\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.datos.valorEscrito").value("Cero "));
    }

    @Test
    void conversor_romanos_retorna_resultado() throws Exception {
        mockMvc.perform(post("/api/plataforma/conversor-monto/romanos")
                .contentType("application/json")
                .content("{\"valor\": \"4\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.datos.valorRomano").value("IV"));
    }

    @Test
    void conversor_romanos_fuera_rango_retorna_400() throws Exception {
        mockMvc.perform(post("/api/plataforma/conversor-monto/romanos")
                .contentType("application/json")
                .content("{\"valor\": \"4000\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void conversor_letras_batch_retorna_lista() throws Exception {
        mockMvc.perform(post("/api/plataforma/conversor-monto/letras/batch")
                .contentType("application/json")
                .content("[{\"valor\": \"1\"}, {\"valor\": \"2\"}, {\"valor\": \"3\"}]"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.datos").isArray())
            .andExpect(jsonPath("$.datos.length()").value(3));
    }

    @Test
    void stub_endpoint_returns_503() throws Exception {
        mockMvc.perform(post("/api/plataforma/parametros/consultar")
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    void acceso_stub_returns_503() throws Exception {
        mockMvc.perform(post("/api/plataforma/acceso/usuario/crear")
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    void autorizacion_stub_returns_503() throws Exception {
        mockMvc.perform(post("/api/plataforma/autorizacion/evento/crear")
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    void comercial_stub_returns_503() throws Exception {
        mockMvc.perform(post("/api/plataforma/comercial/cobros/calcular")
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    void conciliacion_stub_returns_503() throws Exception {
        mockMvc.perform(post("/api/plataforma/conciliacion/facturacion/tesoreria")
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isServiceUnavailable());
    }
}
