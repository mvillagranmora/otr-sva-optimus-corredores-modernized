package cl.bcs.contabilidad.jobs.wiring;

import cl.bcs.contabilidad.jobs.programa.EjecutorPrograma;
import cl.bcs.contabilidad.jobs.programa.ProgramaModo;
import cl.bcs.contabilidad.jobs.programa.ProgramaStatusListener;
import cl.bcs.contabilidad.jobs.runtime.JobRunner;
import cl.bcs.contabilidad.jobs.runtime.JobStatusListener;
import cl.bcs.contabilidad.jobs.web.JobCallController;
import cl.bcs.contabilidad.jobs.web.TokenInternoFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Job pattern H4 wiring with the switch set to {@code programa} (the production default) on top of the test profile (H2, fake token), and
 * the application context as a whole: one runner with the five migrated jobs, one controller, one token filter.
 */
@SpringBootTest(properties = {"contabilidad.jobs.sesion-hana=false", "contabilidad.jobs.programa.modo=programa"})
@ActiveProfiles("test")
class ProgramaArranqueTest {

    @Autowired
    EjecutorPrograma ejecutor;

    @Autowired
    JobStatusListener listener;

    @Autowired
    JobRunner runner;

    @Autowired
    ApplicationContext contexto;

    @Test
    @DisplayName("H4-W01: mode programa wires the real listener and an executor that does not require 'clase'")
    void modoProgramaCableaElListenerReal() {
        assertThat(ejecutor.modo()).isEqualTo(ProgramaModo.PROGRAMA);
        assertThat(ejecutor.exigeClase()).isFalse();
        assertThat(listener).isInstanceOf(ProgramaStatusListener.class);
    }

    @Test
    @DisplayName("H4-W02: one runner with the five jobs, one controller, one token filter, one status listener")
    void unaSolaInstanciaDeCadaPieza() {
        assertThat(runner.clases()).containsExactlyInAnyOrder(
                "cl.bcs.contabilidad.jobs.CentralizaMultiSeleccionJob", "cl.bcs.contabilidad.jobs.CreaInterfazDiaria",
                "cl.bcs.contabilidad.jobs.CreaInterfazDemanda", "cl.bcs.contabilidad.jobs.InterfazContable",
                "cl.bcs.contabilidad.jobs.GenerarInformeClienteRelacionadoBatch");
        assertThat(contexto.getBeansOfType(JobCallController.class)).hasSize(1);
        assertThat(contexto.getBeansOfType(TokenInternoFilter.class)).hasSize(1);
        assertThat(contexto.getBeansOfType(JobStatusListener.class)).hasSize(1);
        assertThat(contexto.getBeansOfType(EjecutorPrograma.class)).hasSize(1);
    }
}
