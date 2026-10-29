package cl.bcs.plataforma;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import cl.bcs.plataforma.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.plataforma.adapter.in.javacall.JavaCallUnitOfWorkFilter;
import cl.bcs.plataforma.adapter.in.javacall.TokenInternoFilter;
import cl.bcs.plataforma.service.JavaCallService;

/**
 * Real context (H2, test profile) with the real filters: D-4 (no stub in the context), the allow-list (one class, anything else 503), token,
 * generic 500 with correlationId (DEV-A3) and the JSON of the legacy on 200 (PLA-ESM-06).
 */
@SpringBootTest(properties = "services.internal-token=test-secret")
@ActiveProfiles("test")
class PlataformaCableadoRealTest {

    static final String CLASE = "cl.bcs.plataforma.cre.ConsultarEstadoModulo";

    @Autowired WebApplicationContext wac;
    @Autowired ApplicationContext ctx;
    @Autowired DataSource ds;
    @Autowired JavaCallDispatcher dispatcher;
    MockMvc mvc;

    @BeforeEach
    void preparar() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilters(ctx.getBean(TokenInternoFilter.class), ctx.getBean(JavaCallUnitOfWorkFilter.class)).build();
        try (Connection c = ds.getConnection()) {
            c.createStatement().execute("DROP TABLE IF EXISTS \"BSCL.Plataforma.BD::PAU.Modulo\"");
            cl.bcs.plataforma.cre.OraculoCasosAcceso.script(c);
            c.createStatement().execute("INSERT INTO \"BSCL.Plataforma.BD::PAU.Modulo\" (\"IdEmpresa\",\"CodigoModulo\",\"IdEstado\","
                    + "\"CodigoModuloDependenciaCierre1\",\"CodigoModuloDependenciaCierre2\",\"CodigoModuloDependenciaCierre3\","
                    + "\"CodigoModuloDependenciaCierre4\",\"CodigoModuloDependenciaCierre5\",\"CodigoUsuario\",\"FechaUltimaActualizacion\") "
                    + "VALUES (1,'SIS','ABT','','','','','','u',TIMESTAMP '2025-01-15 10:00:00')");
        }
    }

    private static String cuerpo(String clase) {
        return "{\"class\":\"" + clase + "\",\"params\":{\"p_IdEmpresa\":1,\"p_IdModulo\":\"\",\"p_IdUsuario\":97}}";
    }

    @Test
    void ningunStubEstaEnElContexto_D4() {
        assertThat(ctx.getBeansOfType(JavaCallService.class)).isEmpty();
        assertThat(dispatcher.servidas()).containsExactly(CLASE);
    }

    @Test
    void claseFueraDeLaListaBlanca_503_noEl200DelStub() throws Exception {
        for (String c : new String[] {"cl.bcs.plataforma.cre.CambiarEstadoModulo", "cl.bcs.plataforma.mer.CalcularParidad", "x.Y"}) {
            mvc.perform(post("/internal/java-call").header("X-Internal-Token", "test-secret")
                    .contentType("application/json").content(cuerpo(c))).andExpect(status().isServiceUnavailable());
        }
    }

    @Test
    void claseServida_200_conElJsonDelLegacy() throws Exception {
        String body = mvc.perform(post("/internal/java-call").header("X-Internal-Token", "test-secret")
                .contentType("application/json").content(cuerpo(CLASE))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(body).isEqualTo("{\"P_OUT_ADVERTENCIASCONTADOR\":0,\"P_OUT_ERRORESCONTADOR\":0,"
                + "\"p_out_ListaModulos\":[{\"IdEstadoModulo\":\"ABT\",\"NombreModulo\":\"SIS\"}]}");
    }

    @Test
    void sinTokenOTokenMalo_401_yConVariantesDeRuta() throws Exception {
        mvc.perform(post("/internal/java-call").contentType("application/json").content(cuerpo(CLASE))).andExpect(status().isUnauthorized());
        mvc.perform(post("//internal/java-call").header("X-Internal-Token", "x").contentType("application/json").content(cuerpo(CLASE)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jsonMalo_400() throws Exception {
        mvc.perform(post("/internal/java-call").header("X-Internal-Token", "test-secret").contentType("application/json").content("[1]"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/internal/java-call").header("X-Internal-Token", "test-secret").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void errorInterno_500GenericoConCorrelationId_sinEcoDeLaExcepcion_DEVA3() throws Exception {
        try (Connection c = ds.getConnection()) {
            c.createStatement().execute("DROP TABLE \"BSCL.Plataforma.BD::PAU.Modulo\"");
        }
        String body = mvc.perform(post("/internal/java-call").header("X-Internal-Token", "test-secret")
                .contentType("application/json").content(cuerpo(CLASE))).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.correlationId").isNotEmpty()).andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain("PAU.Modulo").doesNotContain("SELECT").doesNotContain("Query").doesNotContain("Parameters")
                .doesNotContain("Exception");
    }

    @Test
    void entradaInvalidaDelLegacy_500Generico_D_ESM5() throws Exception {
        String body = mvc.perform(post("/internal/java-call").header("X-Internal-Token", "test-secret").contentType("application/json")
                .content("{\"class\":\"" + CLASE + "\",\"params\":{\"p_IdEmpresa\":1.5}}")).andExpect(status().isInternalServerError())
                .andReturn().getResponse().getContentAsString();
        assertThat(body).doesNotContain("NumberFormat").doesNotContain("p_IdEmpresa");
    }

    @Test
    void esquemaDeTestExisteComoArchivo() throws Exception {
        assertThat(Files.exists(cl.bcs.plataforma.cre.OraculoCasosAcceso.esquema())).isTrue();
    }
}
