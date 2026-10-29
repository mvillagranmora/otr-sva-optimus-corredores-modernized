package cl.bcs.cartolas.master;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para BcsCartolasMaster con H2 in-memory.
 *
 * <p>Spring Boot 4.1.1 eliminó {@code @AutoConfigureMockMvc}. MockMvc se construye
 * manualmente en {@code @BeforeEach} vía {@code MockMvcBuilders.webAppContextSetup}.</p>
 *
 * <p>Las operaciones que requieren SAP HANA están marcadas con
 * {@code @Disabled("requires HANA")}.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartolaMasterIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // =========================================================================
    // GET /cartolas-master/configuracion/{idEmpresa}
    // =========================================================================

    @Test
    void getConfiguracion_empresa1_returns4Items() throws Exception {
        mockMvc.perform(get("/cartolas-master/configuracion/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void getConfiguracion_empresa2_returns1Item() throws Exception {
        mockMvc.perform(get("/cartolas-master/configuracion/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getConfiguracion_empresa999_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/cartolas-master/configuracion/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getConfiguracion_empresa1_firstItemHasNombreThreadPoolSize() throws Exception {
        mockMvc.perform(get("/cartolas-master/configuracion/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre == 'threadPoolSize')].valor").value("4"));
    }

    // =========================================================================
    // GET /cartolas-master/parametros/{idNomina}
    // =========================================================================

    @Test
    void getParametros_nomina100_returns4Items() throws Exception {
        mockMvc.perform(get("/cartolas-master/parametros/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void getParametros_nomina200_returns1Item() throws Exception {
        mockMvc.perform(get("/cartolas-master/parametros/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getParametros_nomina999_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/cartolas-master/parametros/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getParametros_nomina100_contienePRV_incCartera() throws Exception {
        mockMvc.perform(get("/cartolas-master/parametros/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre == 'PRV_incCartera')].valor").value("true"));
    }

    // =========================================================================
    // POST /cartolas-master/generar-online  — requiere HANA
    // =========================================================================

    @Disabled("requires HANA — generarOnline depende de JasperReports + stored procedures HANA")
    @Test
    void generarOnline_requiresHana() throws Exception {
        mockMvc.perform(post("/cartolas-master/generar-online")
                        .param("idCliente", "123570")
                        .param("idPortafolio", "512345")
                        .param("fechaDesde", "2017-04-01")
                        .param("fechaHasta", "2017-06-30")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isServiceUnavailable());
    }

    // =========================================================================
    // POST /cartolas-master/generar/{idCartola}  — requiere HANA
    // =========================================================================

    @Disabled("requires HANA — generarPorIdCartola depende de JasperReports + HANA")
    @Test
    void generarPorIdCartola_requiresHana() throws Exception {
        mockMvc.perform(post("/cartolas-master/generar/12345"))
                .andExpect(status().isServiceUnavailable());
    }

    // =========================================================================
    // GlobalExceptionHandler — 503 por UnsupportedOperationException
    // =========================================================================

    @Test
    void generarOnline_sinHana_returns503() throws Exception {
        // Sin @Disabled para verificar que el handler retorna 503 correctamente
        mockMvc.perform(post("/cartolas-master/generar-online")
                        .param("idCliente", "1")
                        .param("fechaDesde", "2024-01-01")
                        .param("fechaHasta", "2024-03-31"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void generarPorId_sinHana_returns503() throws Exception {
        mockMvc.perform(post("/cartolas-master/generar/99"))
                .andExpect(status().isServiceUnavailable());
    }
}
