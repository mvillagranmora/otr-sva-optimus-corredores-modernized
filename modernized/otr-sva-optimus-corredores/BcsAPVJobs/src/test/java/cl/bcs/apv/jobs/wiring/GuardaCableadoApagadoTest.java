package cl.bcs.apv.jobs.wiring;

import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.jobs.adapter.ColaboradoresJobs;
import cl.bcs.apv.jobs.guarda.ColaboradorTomEEGuardado;
import cl.bcs.apv.jobs.guarda.GuardaTomee;
import cl.bcs.apv.jobs.programa.EjecutorPrograma;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/** Step 6 OFF switch ({@code apv.jobs.guarda-tomee=false}, what the test profile sets): the wiring is the one before step 6, untouched. */
@SpringBootTest(properties = {"services.javacall.url=http://localhost:9", "apv.jobs.sesion-hana=false"})
@ActiveProfiles("test")
class GuardaCableadoApagadoTest {

    @Autowired
    ApplicationContext contexto;
    @Autowired
    Colaboradores colaboradores;
    @Autowired
    EjecutorPrograma ejecutor;

    @Test
    @DisplayName("G6-W03: guard off (test profile): no guard beans, the primary port is the plain HTTP adapter, the executor has no guard")
    void sinGuardaTodoComoAntes() {
        assertThat(contexto.getBeansOfType(GuardaTomee.class)).isEmpty();
        assertThat(contexto.getBeansOfType(ColaboradorTomEEGuardado.class)).isEmpty();
        assertThat(colaboradores).isInstanceOf(ColaboradoresJobs.class);
        assertThat(contexto.getBeansOfType(ColaboradoresJobs.class)).hasSize(1).containsKey("colaboradoresJobs");
        assertThat(ReflectionTestUtils.getField(ejecutor, "guarda")).isNull();
    }
}
