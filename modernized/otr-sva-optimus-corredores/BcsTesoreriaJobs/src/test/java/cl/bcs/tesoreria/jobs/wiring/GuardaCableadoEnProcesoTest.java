package cl.bcs.tesoreria.jobs.wiring;

import cl.bcs.tesoreria.jobs.adapter.ColaboradoresEnProceso;
import cl.bcs.tesoreria.jobs.guarda.ColaboradorTomEEGuardado;
import cl.bcs.tesoreria.jobs.guarda.GuardaTomee;
import cl.bcs.tesoreria.jobs.programa.EjecutorPrograma;
import cl.bcs.tesoreria.port.out.Colaboradores;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step 6 wiring on the production default of step 5 ({@code colaboradores-en-proceso=true}): the in-process collaborators join the job transaction
 * unguarded, and what still goes over HTTP is wrapped by the guard.
 */
@SpringBootTest(properties = {"services.javacall.url=http://localhost:9", "tesoreria.jobs.sesion-hana=false",
        "tesoreria.jobs.colaboradores-en-proceso=true", "tesoreria.jobs.guarda-tomee=true"})
@ActiveProfiles("test")
class GuardaCableadoEnProcesoTest {

    @Autowired
    ApplicationContext contexto;
    @Autowired
    Colaboradores colaboradores;
    @Autowired
    EjecutorPrograma ejecutor;

    @Test
    @DisplayName("G6-W02: guard on, step 5 on: ColaboradoresEnProceso is primary and its HTTP side is the guard around ColaboradoresJobs")
    void elLadoHttpEstaGuardado() {
        assertThat(colaboradores).isInstanceOf(ColaboradoresEnProceso.class);
        Object http = ReflectionTestUtils.getField(colaboradores, "http");
        assertThat(http).isInstanceOf(ColaboradorTomEEGuardado.class);
        assertThat(ReflectionTestUtils.getField(http, "delegado")).isSameAs(contexto.getBean("colaboradoresJobsHttp"));
        assertThat(ReflectionTestUtils.getField(ejecutor, "guarda")).isSameAs(contexto.getBean(GuardaTomee.class));
    }
}
