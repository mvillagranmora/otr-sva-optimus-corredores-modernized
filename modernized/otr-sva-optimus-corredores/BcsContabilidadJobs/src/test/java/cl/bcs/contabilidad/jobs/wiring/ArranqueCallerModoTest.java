package cl.bcs.contabilidad.jobs.wiring;

import cl.bcs.contabilidad.jobs.programa.EjecutorPrograma;
import cl.bcs.contabilidad.jobs.programa.ProgramaModo;
import cl.bcs.contabilidad.jobs.programa.ProgramaStatusListener;
import cl.bcs.contabilidad.jobs.runtime.JobStatusListener;
import cl.bcs.contabilidad.jobs.web.TokenInternoFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * The test profile (caller mode, fake token): the application starts on H2, the status listener is only a logger, and the real filter and
 * controller beans protect {@code /internal/job-call} together (no token: 401; the right token reaches the controller).
 */
@SpringBootTest(properties = {"contabilidad.jobs.sesion-hana=false"})
@ActiveProfiles("test")
class ArranqueCallerModoTest {

    @Autowired
    WebApplicationContext contexto;

    @Autowired
    EjecutorPrograma ejecutor;

    @Autowired
    JobStatusListener listener;

    @Autowired
    TokenInternoFilter filtro;

    private MockMvc mvc() {
        return MockMvcBuilders.webAppContextSetup(contexto).addFilters(filtro).build();
    }

    @Test
    @DisplayName("H4-W03: the test profile starts in caller mode with a logging listener")
    void modoLlamador() {
        assertThat(ejecutor.modo()).isEqualTo(ProgramaModo.PARAMETROS_DEL_LLAMADOR);
        assertThat(ejecutor.exigeClase()).isTrue();
        assertThat(listener).isNotInstanceOf(ProgramaStatusListener.class);
    }

    @Test
    @DisplayName("CONT-J-99e: with the real beans, no token is 401 and the configured token reaches the controller (400: no 'clase' in caller mode)")
    void filtroYControladorReales() throws Exception {
        String cuerpo = "{\"idPrograma\":1}";
        int sinToken = mvc().perform(post("/internal/job-call").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andReturn().getResponse().getStatus();
        int conToken = mvc().perform(post("/internal/job-call").contentType(MediaType.APPLICATION_JSON).content(cuerpo)
                .header(TokenInternoFilter.CABECERA, "fake-internal-token-0000")).andReturn().getResponse().getStatus();

        assertThat(sinToken).isEqualTo(401);
        assertThat(conToken).isEqualTo(400);
    }
}
