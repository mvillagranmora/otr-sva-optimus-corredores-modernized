package cl.bcs.tesoreria.jobs.wiring;

import cl.bcs.tesoreria.jobs.adapter.ColaboradoresJobs;
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

/** Step 6 wiring with step 5 off ({@code colaboradores-en-proceso=false}) and the guard on: the guard is the primary port around the HTTP adapter. */
@SpringBootTest(properties = {"services.javacall.url=http://localhost:9", "tesoreria.jobs.sesion-hana=false",
        "tesoreria.jobs.colaboradores-en-proceso=false", "tesoreria.jobs.guarda-tomee=true"})
@ActiveProfiles("test")
class GuardaCableadoHttpTest {

    @Autowired
    ApplicationContext contexto;
    @Autowired
    Colaboradores colaboradores;
    @Autowired
    EjecutorPrograma ejecutor;

    @Test
    @DisplayName("G6-W01: guard on, step 5 off: the primary port is the guard; the HTTP adapter is its non-primary delegate; the executor has the guard")
    void laGuardaEsElPuertoPrimario() {
        assertThat(colaboradores).isInstanceOf(ColaboradorTomEEGuardado.class);
        assertThat(contexto.getBeansOfType(ColaboradoresJobs.class)).hasSize(1).containsKey("colaboradoresJobsHttp");
        assertThat(ReflectionTestUtils.getField(colaboradores, "delegado")).isSameAs(contexto.getBean("colaboradoresJobsHttp"));
        assertThat(contexto.getBeansOfType(GuardaTomee.class)).hasSize(1);
        assertThat(ReflectionTestUtils.getField(ejecutor, "guarda")).isSameAs(contexto.getBean(GuardaTomee.class));
    }
}
