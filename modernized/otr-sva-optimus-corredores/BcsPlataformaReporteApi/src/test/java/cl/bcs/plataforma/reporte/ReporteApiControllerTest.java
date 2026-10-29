package cl.bcs.plataforma.reporte;

import cl.bcs.plataforma.reporte.service.ReporteApiShimService;
import cl.bcs.utils.report.CrystalReportRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for BcsPlataformaReporteApi compatibility shim.
 *
 * <p>Note: Spring Boot 4.1.1 removed {@code @AutoConfigureMockMvc} — MockMvc is
 * built manually via {@code MockMvcBuilders.webAppContextSetup()} in {@code @BeforeEach}.</p>
 *
 * <p>{@code @MockitoBean ReporteApiShimService} replaces the real service so no
 * outbound HTTP connection to BcsCrystalReports is attempted during testing.</p>
 */
@SpringBootTest
class ReporteApiControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    // Replaces ReporteApiShimService so no real HTTP call is made to BcsCrystalReports:8100
    @MockitoBean
    ReporteApiShimService shimService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("GET /reporte-api/health returns 200 OK")
    void health_returns200() throws Exception {
        mockMvc.perform(get("/reporte-api/health"))
               .andExpect(status().isOk())
               .andExpect(content().string("BcsPlataformaReporteApi UP"));
    }

    @Test
    @DisplayName("POST /reporte-api/generate delegates to BcsCrystalReports via shimService")
    void generate_delegates_to_crystal_reports() throws Exception {
        // Stub: shimService returns fake PDF bytes when called
        byte[] fakePdf = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF header
        when(shimService.generateReport(any(CrystalReportRequest.class))).thenReturn(fakePdf);

        mockMvc.perform(post("/reporte-api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"templateId\":\"test-uuid\",\"format\":\"pdf\"}"))
               .andExpect(status().isOk());

        // Assert delegation occurred — shim must not process the report itself
        verify(shimService).generateReport(any(CrystalReportRequest.class));
    }

    @Test
    @Disabled("decommission: remove after TomEE shutdown")
    @DisplayName("Full integration against live BcsCrystalReports:8100 — disabled pending TomEE shutdown")
    void decommission_full_integration() {
        // This entire module (BcsPlataformaReporteApi) must be removed when TomEE is shut down.
        // All callers have already been migrated to CrystalReportsPort (bcs-utils),
        // which calls BcsCrystalReports directly on port 8100.
    }
}
