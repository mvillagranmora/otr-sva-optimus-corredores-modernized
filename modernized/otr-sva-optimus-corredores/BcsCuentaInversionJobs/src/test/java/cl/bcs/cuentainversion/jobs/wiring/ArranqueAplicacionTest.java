package cl.bcs.cuentainversion.jobs.wiring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import cl.bcs.cuentainversion.jobs.job.ActualizarSaldoCargoAbonoJob;
import cl.bcs.cuentainversion.jobs.job.ActualizarSaldosReporteAntiguedadJob;
import cl.bcs.cuentainversion.jobs.job.BcsJobGenerarInformePatrimonioDiario;
import cl.bcs.cuentainversion.jobs.job.BcsJobGenerarInformeResumenCobro;
import cl.bcs.cuentainversion.jobs.job.CargaInicialSaldosReporteAntiguedadJob;
import cl.bcs.cuentainversion.jobs.job.GenerarArchivo;
import cl.bcs.cuentainversion.jobs.job.GenerarArchivoCargoAbonoJob;
import cl.bcs.cuentainversion.jobs.job.GenerarArchivoClientesPorPagarYCobrarJob;
import cl.bcs.cuentainversion.jobs.job.GenerarArchivoCuentasPorCobrarAnexo16Job;
import cl.bcs.cuentainversion.jobs.job.GenerarArchivoDiferenciaSaldoJob;
import cl.bcs.cuentainversion.jobs.job.GenerarArchivoSaldoCuentasJob;
import cl.bcs.cuentainversion.jobs.job.GenerarReporteAcreedoresJob;
import cl.bcs.cuentainversion.jobs.job.GenerarReporteAntiguedadDeudaBolsaCTAJob;
import cl.bcs.cuentainversion.jobs.job.GenerarReporteAntiguedadDeudaCTAJob;
import cl.bcs.cuentainversion.jobs.job.GuardarPatrimonioCajaJob;
import cl.bcs.cuentainversion.jobs.job.LibroInventarioJob;
import cl.bcs.cuentainversion.jobs.job.pauta.PautaActualizarSaldoCargoAbono;
import cl.bcs.cuentainversion.jobs.job.pauta.PautaGenerarReporteAcreedores;
import cl.bcs.cuentainversion.jobs.job.pauta.PautaGenerarReporteAntiguedadDeudaBolsaCTA;
import cl.bcs.cuentainversion.jobs.job.pauta.PautaGuardarSaldosReporteAntiguedad;
import cl.bcs.cuentainversion.jobs.runtime.JobRunner;
import cl.bcs.cuentainversion.jobs.runtime.ProcesoPauta;

/**
 * The application starts with the real wiring (the library services of bcs-cuenta-inversion-servicios, the job ports, the runner and the
 * controller) on an H2 database, and the obsolete pilot classes are NOT part of it: one runner with exactly the sixteen migrated BcsJob
 * classes, the four PAUTA steps as beans, one token filter, one controller. Step 1: fails until the wiring is done (JobsConfiguration).
 */
@SpringBootTest(properties = {"cuentainversion.jobs.sesion-hana=false"})
@ActiveProfiles("test")
class ArranqueAplicacionTest {

    @Autowired
    ApplicationContext contexto;

    @Autowired
    JobRunner runner;

    @Test
    void elRunnerTieneLosDieciseisJobsMigrados() {
        assertThat(runner.clases()).containsExactlyInAnyOrder(ActualizarSaldoCargoAbonoJob.CLASE, ActualizarSaldosReporteAntiguedadJob.CLASE,
                BcsJobGenerarInformePatrimonioDiario.CLASE, BcsJobGenerarInformeResumenCobro.CLASE, CargaInicialSaldosReporteAntiguedadJob.CLASE,
                GenerarArchivoCargoAbonoJob.CLASE, GenerarArchivoClientesPorPagarYCobrarJob.CLASE, GenerarArchivoCuentasPorCobrarAnexo16Job.CLASE,
                GenerarArchivoDiferenciaSaldoJob.CLASE, GenerarArchivoSaldoCuentasJob.CLASE, GenerarReporteAcreedoresJob.CLASE,
                GenerarReporteAntiguedadDeudaBolsaCTAJob.CLASE, GenerarReporteAntiguedadDeudaCTAJob.CLASE, GuardarPatrimonioCajaJob.CLASE,
                LibroInventarioJob.CLASE, GenerarArchivo.CLASE);
    }

    @Test
    void losPasosDePautaSonBeansPeroNoJobs() {
        assertThat(contexto.getBeansOfType(ProcesoPauta.class).values().stream().map(ProcesoPauta::clase)).containsExactlyInAnyOrder(
                PautaActualizarSaldoCargoAbono.CLASE, PautaGuardarSaldosReporteAntiguedad.CLASE, PautaGenerarReporteAcreedores.CLASE,
                PautaGenerarReporteAntiguedadDeudaBolsaCTA.CLASE);
        assertThat(runner.clases()).noneMatch(c -> c.contains("Pauta"));
    }

    @Test
    void noHayBeansDeNombresDelPilotoEliminado() {
        assertThat(contexto.getBeanDefinitionNames()).noneMatch(n -> n.toLowerCase().endsWith("jobservice") || n.toLowerCase().contains("clientstub")
                || n.equals("hanaJobRepository"));
    }

    @Test
    void hayUnSoloFiltroDeTokenInternoYUnControlador() {
        assertThat(contexto.getBeansOfType(cl.bcs.cuentainversion.jobs.web.TokenInternoFilter.class)).hasSize(1);
        assertThat(contexto.getBeansOfType(cl.bcs.cuentainversion.jobs.web.JobCallController.class)).hasSize(1);
    }
}
