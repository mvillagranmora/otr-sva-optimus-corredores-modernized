package cl.bcs.tesoreria.jobs.wiring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import cl.bcs.tesoreria.jobs.adapter.ColaboradoresJobs;
import cl.bcs.tesoreria.jobs.job.BcsJobCargaMasivaIngresoEgresoDAPFFMM;
import cl.bcs.tesoreria.jobs.job.BcsJobProcesarDistribucion;
import cl.bcs.tesoreria.jobs.job.GenerarArchivoCargos;
import cl.bcs.tesoreria.jobs.job.GenerarArchivoPago;
import cl.bcs.tesoreria.jobs.runtime.JobRunner;
import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * The application starts with the real wiring (the library services of tesoreria-service, the job ports, the runner and the
 * controller) on an H2 database, and the obsolete pilot classes are NOT part of it: one runner with exactly the four migrated jobs,
 * the job module's collaborator port in front of the service one, no Spring Batch beans, one token filter.
 */
@SpringBootTest(properties = {"services.javacall.url=http://localhost:9", "tesoreria.jobs.sesion-hana=false",
        "tesoreria.jobs.colaboradores-en-proceso=false"}) // step 5: this test pins the HTTP-only wiring; ColaboradoresEnProcesoWiringTest pins the other
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
        assertThat(runner.clases()).containsExactlyInAnyOrder(BcsJobProcesarDistribucion.CLASE,
                BcsJobCargaMasivaIngresoEgresoDAPFFMM.CLASE, GenerarArchivoCargos.CLASE, GenerarArchivoPago.CLASE);
    }

    @Test
    void elPuertoDeColaboradoresEsElDelModuloDeJobs() {
        assertThat(colaboradores).isInstanceOf(ColaboradoresJobs.class);
        assertThat(contexto.getBeansOfType(Colaboradores.class)).hasSize(2);
    }

    @Test
    void elPilotoObsoletoNoEstaEnElContexto() {
        assertThat(contexto.getBeanDefinitionNames()).noneMatch(n -> n.toLowerCase().contains("tasklet")
                || n.equals("cargaMasivaJob") || n.equals("generarArchivoPagoJob") || n.equals("generarArchivoCargosJob")
                || n.equals("procesarDistribucionJob") || n.equals("moduloRestClient") || n.equals("tesoreriaJobsRepository")
                || n.equals("auditLogService") || n.equals("tesoreriaJobsConfiguration"));
        assertThat(contexto.getBeanDefinitionNames()).noneMatch(n -> n.toLowerCase().contains("jobrepository")
                || n.toLowerCase().contains("joblauncher"));
    }

    @Test
    void hayUnSoloFiltroDeTokenInterno() {
        assertThat(contexto.getBeansOfType(cl.bcs.tesoreria.jobs.web.TokenInternoFilter.class)).hasSize(1);
        assertThat(contexto.getBeansOfType(cl.bcs.tesoreria.jobs.web.JobCallController.class)).hasSize(1);
    }
}
