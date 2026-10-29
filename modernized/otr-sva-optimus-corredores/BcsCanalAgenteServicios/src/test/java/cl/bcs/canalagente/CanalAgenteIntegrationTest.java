package cl.bcs.canalagente;

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
 * Integration tests for BcsCanalAgenteServicios backed by H2 in-memory database.
 *
 * Spring Boot 4.1.1 removed @AutoConfigureMockMvc — MockMvc is built manually
 * via MockMvcBuilders.webAppContextSetup() in @BeforeEach.
 *
 * HANA-dependent operations (consultarHistorialCompleto) are @Disabled.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CanalAgenteIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    // ---------------------------------------------------------
    // Health endpoint
    // ---------------------------------------------------------

    @Test
    void health_returns200() throws Exception {
        mockMvc.perform(get("/canal-agente/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("UP"));
    }

    // ---------------------------------------------------------
    // Cliente no encontrado (H2-testable)
    // ---------------------------------------------------------

    @Test
    void consultarHistorial_clienteNoEncontrado_returns200WithError() throws Exception {
        // Portfolio 99999 has no client in H2 test data
        mockMvc.perform(post("/canal-agente/historial-movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idEmpresa": 1,
                                  "idUsuario": 1646,
                                  "idModulo": "CTA",
                                  "idPortafolio": 99999,
                                  "fechaDesde": "2026-05-14",
                                  "fechaHasta": "2026-08-14",
                                  "idMoneda": "CLP",
                                  "idMatrizAgrupador": 0,
                                  "esADC": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.erroresContador").value(1))
                .andExpect(jsonPath("$.erroresDetalle[0].descripcion").value("Cliente no encontrado."));
    }

    @Test
    void consultarHistorial_clienteNoEncontrado_resultadoIsEmpty() throws Exception {
        mockMvc.perform(post("/canal-agente/historial-movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idEmpresa": 1,
                                  "idUsuario": 1646,
                                  "idModulo": "CTA",
                                  "idPortafolio": 99999,
                                  "fechaDesde": "2026-01-01",
                                  "fechaHasta": "2026-06-30",
                                  "idMoneda": "CLP"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").isArray())
                .andExpect(jsonPath("$.resultado.length()").value(0));
    }

    // ---------------------------------------------------------
    // Input validation -> 400
    // ---------------------------------------------------------

    @Test
    void consultarHistorial_missingIdPortafolio_returns400() throws Exception {
        mockMvc.perform(post("/canal-agente/historial-movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idEmpresa": 1,
                                  "idUsuario": 1646,
                                  "idModulo": "CTA",
                                  "fechaDesde": "2026-05-14",
                                  "fechaHasta": "2026-08-14",
                                  "idMoneda": "CLP"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarHistorial_missingIdEmpresa_returns400() throws Exception {
        mockMvc.perform(post("/canal-agente/historial-movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idUsuario": 1646,
                                  "idModulo": "CTA",
                                  "idPortafolio": 66720,
                                  "fechaDesde": "2026-05-14",
                                  "fechaHasta": "2026-08-14",
                                  "idMoneda": "CLP"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarHistorial_missingIdMoneda_returns400() throws Exception {
        mockMvc.perform(post("/canal-agente/historial-movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idEmpresa": 1,
                                  "idUsuario": 1646,
                                  "idModulo": "CTA",
                                  "idPortafolio": 66720,
                                  "fechaDesde": "2026-05-14",
                                  "fechaHasta": "2026-08-14"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------
    // HANA-dependent (full historial workflow)
    // ---------------------------------------------------------

    @Disabled("requires HANA — consultarHistorialCompleto uses ConsultarCartolaDeMovimiento SP chain + HANA temp table pattern")
    @Test
    void consultarHistorial_clienteEncontrado_delegatesToHana() throws Exception {
        // Portfolio 66720 → Client 1234 exists in H2 test data,
        // but the next step (consultarHistorialCompleto) requires HANA.
        mockMvc.perform(post("/canal-agente/historial-movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idEmpresa": 1,
                                  "idUsuario": 1646,
                                  "idModulo": "CTA",
                                  "idPortafolio": 66720,
                                  "fechaDesde": "2026-05-14",
                                  "fechaHasta": "2026-08-14",
                                  "idMoneda": "CLP",
                                  "idMatrizAgrupador": 0,
                                  "esADC": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.erroresContador").value(0));
    }

    @Disabled("requires HANA — full workflow returns 503 until HANA adapter is wired")
    @Test
    void consultarHistorial_clienteEncontrado_returns503WhenHanaUnavailable() throws Exception {
        mockMvc.perform(post("/canal-agente/historial-movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idEmpresa": 1,
                                  "idUsuario": 1646,
                                  "idModulo": "CTA",
                                  "idPortafolio": 66720,
                                  "fechaDesde": "2026-05-14",
                                  "fechaHasta": "2026-08-14",
                                  "idMoneda": "CLP"
                                }
                                """))
                .andExpect(status().isServiceUnavailable());
    }
}
