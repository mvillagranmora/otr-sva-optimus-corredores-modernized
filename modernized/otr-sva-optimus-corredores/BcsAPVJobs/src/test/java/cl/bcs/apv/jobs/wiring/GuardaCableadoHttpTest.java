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

/** Step 6 wiring with the guard on: the guard is the primary port around the HTTP adapter, the executor holds the guard. */
@SpringBootTest(properties = {"services.javacall.url=http://localhost:9", "apv.jobs.sesion-hana=false", "apv.jobs.guarda-tomee=true"})
@ActiveProfiles("test")
class GuardaCableadoHttpTest {

    @Autowired
    ApplicationContext contexto;
    @Autowired
    Colaboradores colaboradores;
    @Autowired
    EjecutorPrograma ejecutor;

    @Test
    @DisplayName("G6-W01: guard on: the primary port is the guard; the HTTP adapter is its non-primary delegate; the executor has the guard")
    void laGuardaEsElPuertoPrimario() {
        assertThat(colaboradores).isInstanceOf(ColaboradorTomEEGuardado.class);
        assertThat(contexto.getBeansOfType(ColaboradoresJobs.class)).hasSize(1).containsKey("colaboradoresJobsHttp");
        assertThat(ReflectionTestUtils.getField(colaboradores, "delegado")).isSameAs(contexto.getBean("colaboradoresJobsHttp"));
        assertThat(contexto.getBeansOfType(GuardaTomee.class)).hasSize(1);
        assertThat(ReflectionTestUtils.getField(ejecutor, "guarda")).isSameAs(contexto.getBean(GuardaTomee.class));
    }

    @Test
    @DisplayName("G6-W02: the jobs receive the guarded port (the same bean as the primary Colaboradores)")
    void losJobsRecibenElPuertoGuardado() {
        for (String job : new String[] {"ingresarOrdenBonificacionFiscal", "ingresarOrdenFMBonificacionFiscal", "ingresarOrdenRVBonificacionFiscal"}) {
            assertThat(ReflectionTestUtils.getField(contexto.getBean(job), "colaboradores")).as(job).isSameAs(colaboradores);
        }
    }
}
