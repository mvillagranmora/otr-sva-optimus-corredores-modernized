package cl.bcs.normativa.jobs;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for BcsNormativaJobs Spring Boot application.
 *
 * Uses H2 in-memory database (@ActiveProfiles("test")).
 * HANA-specific endpoints are NOT tested here — they use @Disabled("requires HANA").
 *
 * Pattern: MockMvcBuilders.webAppContextSetup(wac).build() in @BeforeEach
 * (no @AutoConfigureMockMvc per project conventions)
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("NormativaJobs — integration tests (H2)")
class NormativaJobsApplicationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    @DisplayName("NJB-IT-01: application context loads successfully")
    void contextLoads() {
        // If @SpringBootTest succeeds, the context loaded
    }

    @Test
    @DisplayName("NJB-IT-02: GET /api/normativa/jobs/health returns UP")
    void health_returnsUp() throws Exception {
        mockMvc.perform(get("/api/normativa/jobs/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("normativa-jobs"));
    }

    // ─── HANA-dependent endpoints — all @Disabled ─────────────────────────────

    @Test
    @Disabled("requires HANA: ProcesoGeneracion calls HANA table scan and file generation")
    @DisplayName("NJB-IT-HANA-01: POST /api/normativa/jobs/generacion executes generacion process")
    void generacion_requiresHana() {
        // Given: valid parameters {idDocumento, idUsuario, schema}
        // When: POST /api/normativa/jobs/generacion
        // Then: resultadoJob.exitoso = true
        // HANA_URL, HANA_USER, HANA_PASSWORD from environment — never inline
    }

    @Test
    @Disabled("requires HANA: ProcesoValidacion")
    @DisplayName("NJB-IT-HANA-02: POST /api/normativa/jobs/validacion executes validacion process")
    void validacion_requiresHana() {
    }

    @Test
    @Disabled("requires HANA: ProcesoCarga")
    @DisplayName("NJB-IT-HANA-03: POST /api/normativa/jobs/carga executes carga process")
    void carga_requiresHana() {
    }

    @Test
    @Disabled("requires HANA: ProcesoCuadratura")
    @DisplayName("NJB-IT-HANA-04: POST /api/normativa/jobs/cuadratura executes cuadratura process")
    void cuadratura_requiresHana() {
    }

    @Test
    @Disabled("requires HANA: ProcesoExportacion")
    @DisplayName("NJB-IT-HANA-05: POST /api/normativa/jobs/exportacion executes exportacion process")
    void exportacion_requiresHana() {
    }

    @Test
    @Disabled("requires HANA: NOR.ProcesarTarea stored procedure")
    @DisplayName("NJB-IT-HANA-06: POST /api/normativa/jobs/procesar-serv calls NOR.ProcesarTarea")
    void procesarServ_requiresHana() {
    }

    @Test
    @Disabled("requires HANA: ValorizarNCG499 stored procedure")
    @DisplayName("NJB-IT-HANA-07: POST /api/normativa/jobs/valorizar-ncg499 executes NCG499 valuation")
    void valorizarNcg499_requiresHana() {
    }

    @Test
    @Disabled("requires HANA: ValorizarDeclaracion stored procedure")
    @DisplayName("NJB-IT-HANA-08: POST /api/normativa/jobs/valorizar-declaracion executes declaration valuation")
    void valorizarDeclaracion_requiresHana() {
    }

    @Test
    @Disabled("requires HANA: GenerarCertificadoXxMasivo stored procedures")
    @DisplayName("NJB-IT-HANA-09: POST /api/normativa/jobs/certificados-tributarios generates tax certificates")
    void certificadosTributarios_requiresHana() {
    }

    @Test
    @Disabled("requires HANA: GenerarReporteAPV stored procedure")
    @DisplayName("NJB-IT-HANA-10: POST /api/normativa/jobs/reporte-apv generates APV report")
    void reporteApv_requiresHana() {
    }
}
