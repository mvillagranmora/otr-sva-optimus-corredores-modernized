package cl.bcs.cuentainversion.jobs.wiring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import cl.bcs.cuentainversion.jobs.programa.EjecutorPrograma;
import cl.bcs.cuentainversion.jobs.programa.ProgramaModo;
import cl.bcs.cuentainversion.jobs.programa.ProgramaStatusListener;
import cl.bcs.cuentainversion.jobs.runtime.JobStatusListener;

/** Job pattern H4 wiring with the switch set to {@code programa} (the production default) on top of the test profile. */
@SpringBootTest(properties = {"services.javacall.url=http://localhost:9", "cuentainversion.jobs.sesion-hana=false",
        "cuentainversion.jobs.programa.modo=programa"})
@ActiveProfiles("test")
class ProgramaArranqueTest {

    @Autowired
    EjecutorPrograma ejecutor;

    @Autowired
    JobStatusListener listener;

    @Test
    @DisplayName("H4-W01: mode programa wires the real listener and an executor that does not require 'clase'")
    void modoProgramaCableaElListenerReal() {
        assertThat(ejecutor.modo()).isEqualTo(ProgramaModo.PROGRAMA);
        assertThat(ejecutor.exigeClase()).isFalse();
        assertThat(listener).isInstanceOf(ProgramaStatusListener.class);
    }
}
