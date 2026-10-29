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

/** Step 6 OFF switch ({@code tesoreria.jobs.guarda-tomee=false}, what the test profile sets): the wiring is the one of step 5, untouched. */
@SpringBootTest(properties = {"services.javacall.url=http://localhost:9", "tesoreria.jobs.sesion-hana=false",
        "tesoreria.jobs.colaboradores-en-proceso=true"})
@ActiveProfiles("test")
class GuardaCableadoApagadoTest {

    @Autowired
    ApplicationContext contexto;
    @Autowired
    Colaboradores colaboradores;
    @Autowired
    EjecutorPrograma ejecutor;

    @Test
    @DisplayName("G6-W03: guard off (test profile): no guard beans, the HTTP side is the plain adapter, the executor has no guard")
    void sinGuardaTodoComoAntes() {
        assertThat(contexto.getBeansOfType(GuardaTomee.class)).isEmpty();
        assertThat(contexto.getBeansOfType(ColaboradorTomEEGuardado.class)).isEmpty();
        assertThat(ReflectionTestUtils.getField(colaboradores, "http")).isInstanceOf(ColaboradoresJobs.class);
        assertThat(ReflectionTestUtils.getField(ejecutor, "guarda")).isNull();
    }
}
