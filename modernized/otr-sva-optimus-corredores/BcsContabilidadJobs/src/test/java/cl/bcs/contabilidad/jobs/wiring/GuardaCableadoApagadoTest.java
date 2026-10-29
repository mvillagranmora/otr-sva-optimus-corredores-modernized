package cl.bcs.contabilidad.jobs.wiring;

import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.port.out.ProcesoBatchPort;
import cl.bcs.contabilidad.infrastructure.plataforma.TomEeProcesoBatchClient;
import cl.bcs.contabilidad.jobs.adapter.PlataformaEnProceso;
import cl.bcs.contabilidad.jobs.guarda.GuardaTomee;
import cl.bcs.contabilidad.jobs.guarda.ProcesoBatchGuardado;
import cl.bcs.contabilidad.jobs.programa.EjecutorPrograma;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/** Step 6 OFF switch ({@code contabilidad.jobs.guarda-tomee=false}, what the test profile sets): the wiring is the one of step 5, untouched. */
@SpringBootTest(properties = {"contabilidad.jobs.sesion-hana=false", "contabilidad.jobs.colaboradores-en-proceso=true"})
@ActiveProfiles("test")
class GuardaCableadoApagadoTest {

    @Autowired
    ApplicationContext contexto;
    @Autowired
    ProcesoBatchPort procesoBatch;
    @Autowired
    PlataformaPort plataforma;
    @Autowired
    EjecutorPrograma ejecutor;

    @Test
    @DisplayName("G6-W03: guard off (test profile): no guard beans, the batch port is the plain HTTP adapter, the executor has no guard, step 5 intact")
    void sinGuardaTodoComoAntes() {
        assertThat(contexto.getBeansOfType(GuardaTomee.class)).isEmpty();
        assertThat(contexto.getBeansOfType(ProcesoBatchGuardado.class)).isEmpty();
        assertThat(contexto.getBeansOfType(ProcesoBatchPort.class)).hasSize(1);
        assertThat(procesoBatch).isInstanceOf(TomEeProcesoBatchClient.class);
        assertThat(plataforma).isInstanceOf(PlataformaEnProceso.class);
        assertThat(ReflectionTestUtils.getField(ejecutor, "guarda")).isNull();
    }

    @Test
    @DisplayName("G6-W04: the property is true in application.yml and the test profile overrides it to false")
    void laPropiedadDelPerfilDePrueba() {
        assertThat(contexto.getEnvironment().getProperty("contabilidad.jobs.guarda-tomee")).isEqualTo("false");
    }
}
