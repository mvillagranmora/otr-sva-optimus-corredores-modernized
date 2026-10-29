package cl.bcs.fondomutuo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.sql.Connection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallUnitOfWorkFilter;
import cl.bcs.fondomutuo.adapter.in.javacall.TokenInternoFilter;
import cl.bcs.fondomutuo.kit.EscrituraConfirmada;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * M3 at the HTTP boundary: a {@code ServicioNoServidoException} is the repeatable 503 only while nothing of the request can be committed; after the lock DDL
 * committed a local write (HANA) or an HTTP write collaborator answered, the answer is the generic 500 (the router does not repeat in TomEE). Also pins the
 * RESIDUAL RISK that remains by design: a 503 for a gate evaluated after a DDL that committed nothing is still a 503.
 */
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ActiveProfiles("test")
@Import(JavaCallControllerEscrituraConfirmadaTest.Manejadores.class)
class JavaCallControllerEscrituraConfirmadaTest {

    @TestConfiguration
    static class Manejadores {
        /** params.modo: gate (503 with nothing written) | ddlTrasInsert (INSERT then lock DDL then 503) | remota (HTTP write answered then 503). */
        @Bean
        JavaCallHandler normalJavaCallHandler() {
            return new JavaCallHandler() {
                public String fqn() {
                    return JavaCallDispatcher.CREAR_SOLICITUD_NORMAL;
                }

                public JsonObject call(Connection con, JsonObject jin) throws Exception {
                    switch (jin.get("modo").getAsString()) {
                        case "ddlTrasInsert" -> {
                            EscrituraConfirmada.escrituraLocalPendiente();
                            EscrituraConfirmada.ddlConfirmaEscrituraPendiente();
                        }
                        case "ddlSinInsert" -> EscrituraConfirmada.ddlConfirmaEscrituraPendiente();
                        case "remota" -> EscrituraConfirmada.escrituraRemotaConfirmada();
                        default -> {
                        }
                    }
                    throw new ServicioNoServidoException("G5", "router 503 after something");
                }
            };
        }
    }

    @Autowired WebApplicationContext wac;
    @Autowired ApplicationContext ctx;
    MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).addFilters(ctx.getBean(TokenInternoFilter.class), ctx.getBean(cl.bcs.fondomutuo.adapter.in.javacall.JavaCallLimitesFilter.class), ctx.getBean(JavaCallUnitOfWorkFilter.class)).build();
    }

    private org.springframework.mock.web.MockHttpServletResponse llamar(String modo) throws Exception {
        return mvc.perform(post("/internal/java-call").header("X-Internal-Token", "test-secret").contentType("application/json")
                .content("{\"class\":\"" + JavaCallDispatcher.CREAR_SOLICITUD_NORMAL + "\",\"params\":{\"modo\":\"" + modo + "\"}}")).andReturn().getResponse();
    }

    @Test
    void sinNadaConfirmado_503_elRouterRepite() throws Exception {
        assertThat(llamar("gate").getStatus()).isEqualTo(503);
    }

    @Test
    void elDdlDelBloqueoNoConfirmaNada_cuandoNoHabiaEscrituraLocal_sigueSiendo503() throws Exception {
        assertThat(llamar("ddlSinInsert").getStatus()).isEqualTo(503);   // gates G1..G4: the DDL ran, nothing was written yet
    }

    @Test
    void ddlTrasUnInsertLocal_503SeVuelve500Generico_conCorrelationId_sinDetalle() throws Exception {
        var r = llamar("ddlTrasInsert");
        assertThat(r.getStatus()).isEqualTo(500);
        assertThat(r.getContentAsString()).contains("\"error\":\"internal error\"").contains("correlationId").doesNotContain("G5").doesNotContain("router");
    }

    @Test
    void escrituraRemotaConfirmada_503SeVuelve500Generico() throws Exception {
        assertThat(llamar("remota").getStatus()).isEqualTo(500);
    }

    @Test
    void laMarcaNoSeFiltraALaPeticionSiguiente_mismoHilo() throws Exception {
        assertThat(llamar("remota").getStatus()).isEqualTo(500);
        assertThat(llamar("gate").getStatus()).isEqualTo(503);
    }
}
