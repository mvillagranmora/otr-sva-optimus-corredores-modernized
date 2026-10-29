package cl.bcs.apv.jobs.wiring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.jobs.adapter.ColaboradoresJobs;
import cl.bcs.apv.jobs.job.BcsJobCargaMasivaPrevired;
import cl.bcs.apv.jobs.job.IngresarOrdenBonificacionFiscal;
import cl.bcs.apv.jobs.job.IngresarOrdenFMBonificacionFiscal;
import cl.bcs.apv.jobs.job.IngresarOrdenRVBonificacionFiscal;
import cl.bcs.apv.jobs.runtime.JobRunner;
import cl.bcs.apv.jobs.web.JobCallController;
import cl.bcs.apv.jobs.web.TokenInternoFilter;

/**
 * The application starts with the real wiring (the library use cases of apv-service, the job ports, the runner and the controller) on an H2 database,
 * and the obsolete pilot classes are NOT part of it: one runner with exactly the four migrated jobs, the job module's collaborator port in front of the
 * service one, one token filter, no scheduler (RULE-061).
 */
@SpringBootTest(properties = {"services.javacall.url=http://localhost:9", "apv.jobs.sesion-hana=false"})
@ActiveProfiles("test")
class ArranqueAplicacionTest {

    @Autowired
    ApplicationContext contexto;

    @Autowired
    JobRunner runner;

    @Autowired
    Colaboradores colaboradores;

    @Test
    void elRunnerTieneLosCuatroJobsMigrados() {
        assertThat(runner.clases()).containsExactlyInAnyOrder(BcsJobCargaMasivaPrevired.CLASE, IngresarOrdenBonificacionFiscal.CLASE,
                IngresarOrdenFMBonificacionFiscal.CLASE, IngresarOrdenRVBonificacionFiscal.CLASE);
    }

    @Test
    void elPuertoDeColaboradoresEsElDelModuloDeJobs() {
        assertThat(colaboradores).isInstanceOf(ColaboradoresJobs.class);
        assertThat(contexto.getBeansOfType(Colaboradores.class)).hasSize(2);
    }

    @Test
    void elPilotoObsoletoNoEstaEnElContexto() {
        assertThat(contexto.getBeanDefinitionNames()).noneMatch(n -> n.toLowerCase().contains("tasklet") || n.equals("apvJobsRepository")
                || n.equals("cargaMasivaPreviredJob") || n.equals("ingresarOrdenBonificacionFiscalJob") || n.equals("ingresarOrdenFMBonificacionFiscalJob")
                || n.equals("ingresarOrdenRVBonificacionFiscalJob") || n.equals("tomEeClientStubs"));
        assertThat(contexto.getBeanNamesForType(org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor.class)).isEmpty();
    }

    @Test
    void hayUnSoloFiltroDeTokenInternoYUnControlador() {
        assertThat(contexto.getBeansOfType(TokenInternoFilter.class)).hasSize(1);
        assertThat(contexto.getBeansOfType(JobCallController.class)).hasSize(1);
    }
}
