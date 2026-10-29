package cl.bcs.cartolas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
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
 * Tests de integración para BcsCartolas con H2 in-memory.
 *
 * <p>Spring Boot 4.1.1 eliminó {@code @AutoConfigureMockMvc}.
 * MockMvc se construye manualmente en {@code @BeforeEach}
 * vía {@code MockMvcBuilders.webAppContextSetup}.</p>
 *
 * <p>Las operaciones que requieren SAP HANA están marcadas con
 * {@code @Disabled("requires HANA")}.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CartolasIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    // =========================================================================
    // B-CAR-01 Config por empresa
    // =========================================================================

    @Nested
    class B_CAR_01_ConfigPorEmpresa {

        @Test
        void getConfig_empresa1_returns4Items() throws Exception {
            mockMvc.perform(get("/cartolas/config").param("idEmpresa", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(4));
        }

        @Test
        void getConfig_empresa2_returns1Item() throws Exception {
            mockMvc.perform(get("/cartolas/config").param("idEmpresa", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        void getConfig_empresa999_returnsEmptyArray() throws Exception {
            mockMvc.perform(get("/cartolas/config").param("idEmpresa", "999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void getConfig_empresa1_tieneCallerBatchSize() throws Exception {
            mockMvc.perform(get("/cartolas/config").param("idEmpresa", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.nombre == 'CallerBatchSize')].valor").value("100"));
        }
    }

    // =========================================================================
    // B-CAR-02 Parámetros de nómina
    // =========================================================================

    @Nested
    class B_CAR_02_ParametrosPorNomina {

        @Test
        void getParametros_nomina10_returns3Items() throws Exception {
            mockMvc.perform(get("/cartolas/nominas/10/parametros"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3));
        }

        @Test
        void getParametros_nomina999_returnsEmptyArray() throws Exception {
            mockMvc.perform(get("/cartolas/nominas/999/parametros"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void getParametros_nomina10_tieneHabilitado() throws Exception {
            mockMvc.perform(get("/cartolas/nominas/10/parametros"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.nombre == 'Habilitado')].valor").value("true"));
        }
    }

    // =========================================================================
    // B-CAR-03 Nóminas
    // =========================================================================

    @Nested
    class B_CAR_03_Nominas {

        @Test
        void getNomina_id10_returnsNomina() throws Exception {
            mockMvc.perform(get("/cartolas/nominas/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(10))
                    .andExpect(jsonPath("$.periodo").value("2024-03"));
        }

        @Test
        void getNomina_id999_returns404() throws Exception {
            mockMvc.perform(get("/cartolas/nominas/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void getNominasProgramadas_returns1() throws Exception {
            mockMvc.perform(get("/cartolas/nominas/programadas"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].tipoProceso").value("PROGRAMADO"));
        }
    }

    // =========================================================================
    // B-CAR-04 Cartolas
    // =========================================================================

    @Nested
    class B_CAR_04_Cartolas {

        @Test
        void getCartola_id100_returnsCartola() throws Exception {
            mockMvc.perform(get("/cartolas/100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(100))
                    .andExpect(jsonPath("$.idNomina").value(10));
        }

        @Test
        void getCartola_id999_returns404() throws Exception {
            mockMvc.perform(get("/cartolas/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void getCartolas_nomina10_returns2() throws Exception {
            mockMvc.perform(get("/cartolas").param("idNomina", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void getCartolas_nomina10_soloReproceso_returns1() throws Exception {
            mockMvc.perform(get("/cartolas")
                            .param("idNomina", "10")
                            .param("soloReproceso", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].reprocesar").value("SI"));
        }
    }

    // =========================================================================
    // B-CAR-05 Datos de cartola
    // =========================================================================

    @Nested
    class B_CAR_05_DatosCartola {

        @Test
        void getDatos_cartola100_returns1Item() throws Exception {
            mockMvc.perform(get("/cartolas/100/datos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].nombre").value("DatosPortafolio"));
        }

        @Test
        void getDatos_cartola999_returnsEmptyArray() throws Exception {
            mockMvc.perform(get("/cartolas/999/datos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // =========================================================================
    // B-CAR-06 Resolución de nombres de archivo — pure-logic (HANA-free)
    // =========================================================================

    @Nested
    class B_CAR_06_ResolverNombreArchivo {

        @Test
        void resolverNombreArchivoCartola_conDocumentoCliente_endsWithPdf() throws Exception {
            mockMvc.perform(get("/cartolas/nombre-archivo/cartola")
                            .param("patron", "{DocumentoCliente}_{YYYYMMDD}")
                            .param("DocumentoCliente", "12345678"))
                    .andExpect(status().isOk())
                    .andExpect(result -> {
                        final String body = result.getResponse().getContentAsString();
                        assert body.endsWith(".pdf") : "Expected .pdf suffix, got: " + body;
                        assert body.startsWith("12345678_") : "Expected DocumentoCliente prefix: " + body;
                    });
        }

        @Test
        void resolverNombreArchivoNomina_endsWithTxt() throws Exception {
            mockMvc.perform(get("/cartolas/nombre-archivo/nomina")
                            .param("patron", "Nomina_{YYYYMMDD}")
                            .param("NombreEmpresa", "BCS"))
                    .andExpect(status().isOk())
                    .andExpect(result -> {
                        final String body = result.getResponse().getContentAsString();
                        assert body.endsWith(".txt") : "Expected .txt suffix, got: " + body;
                    });
        }

        @Test
        void resolverNombreArchivoZip_endsWithZip() throws Exception {
            mockMvc.perform(get("/cartolas/nombre-archivo/zip")
                            .param("patron", "Cartolas_{YYYYMMDD}"))
                    .andExpect(status().isOk())
                    .andExpect(result -> {
                        final String body = result.getResponse().getContentAsString();
                        assert body.endsWith(".zip") : "Expected .zip suffix, got: " + body;
                    });
        }
    }

    // =========================================================================
    // B-CAR-07 Operaciones que requieren HANA — deben retornar 503
    // =========================================================================

    @Nested
    class B_CAR_07_OperacionesHana {

        @Test
        void poblarNomina_sinHana_returns503() throws Exception {
            mockMvc.perform(post("/cartolas/nominas/10/poblar"))
                    .andExpect(status().isServiceUnavailable());
        }

        @Test
        void cargaDatos_sinHana_returns503() throws Exception {
            mockMvc.perform(post("/cartolas/carga-datos").param("idNomina", "10"))
                    .andExpect(status().isServiceUnavailable());
        }

        @Disabled("requires HANA — SP: CAR.GrabarCartola")
        @Test
        void guardarCartola_requiresHana() {
            // Validado por la restricción UnsupportedOperationException → 503
        }

        @Disabled("requires HANA — SP: CAR.GrabarNomina")
        @Test
        void guardarNomina_requiresHana() {
            // Validado por la restricción UnsupportedOperationException → 503
        }
    }
}
