package cl.bcs.facturacion;

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
 * Integration tests for BcsFacturacionServicios backed by H2 in-memory database.
 *
 * Spring Boot 4.1.1 removed @AutoConfigureMockMvc — MockMvc is built manually
 * via MockMvcBuilders.webAppContextSetup().
 *
 * HANA-dependent operations are @Disabled.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FacturacionServiciosIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getTiposFactura_returns200WithSize9() throws Exception {
        mockMvc.perform(get("/facturacion-servicios/tipos-factura"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(9));
    }

    @Test
    void getTiposNotaCredito_returns200WithSize3() throws Exception {
        mockMvc.perform(get("/facturacion-servicios/tipos-nota-credito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void esFactura_FAC_returnsTrue() throws Exception {
        mockMvc.perform(get("/facturacion-servicios/es-factura/FAC"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void esFactura_XXX_returnsFalse() throws Exception {
        mockMvc.perform(get("/facturacion-servicios/es-factura/XXX"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void getFechaSistema_empresa1_returns20240115() throws Exception {
        mockMvc.perform(get("/facturacion-servicios/fecha-sistema/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("2024-01-15"));
    }

    @Test
    void getComprobantes_empresa1_estadoPEN_returnsSize2() throws Exception {
        mockMvc.perform(get("/facturacion-servicios/comprobantes/1")
                        .param("estado", "PEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Disabled("requires HANA — ProcesarFacturacionCustodia 5,384 LOC god-object is 100% HANA-driven")
    @Test
    void procesarFacturacionCustodia_requiresHana() throws Exception {
        mockMvc.perform(post("/facturacion-servicios/procesar-facturacion-custodia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idEmpresa\":1,\"idPortafolio\":100}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Disabled("requires HANA — CrearComprobante god-object is 100% HANA-driven")
    @Test
    void crearComprobante_requiresHana() throws Exception {
        mockMvc.perform(post("/facturacion-servicios/crear-comprobante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idEmpresa\":1,\"tipoComprobante\":\"FAC\"}"))
                .andExpect(status().isServiceUnavailable());
    }
}
