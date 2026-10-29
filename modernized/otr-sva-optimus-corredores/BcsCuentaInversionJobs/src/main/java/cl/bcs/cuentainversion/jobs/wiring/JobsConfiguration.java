package cl.bcs.cuentainversion.jobs.wiring;

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
import cl.bcs.cuentainversion.jobs.job.port.ConfiguracionJobs;
import cl.bcs.cuentainversion.jobs.job.port.LibroInventario;
import cl.bcs.cuentainversion.jobs.job.port.ProcedimientoArchivo;
import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.JobRunner;
import cl.bcs.cuentainversion.jobs.runtime.JobStatusListener;
import cl.bcs.cuentainversion.port.out.Colaboradores;
import cl.bcs.cuentainversion.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.util.List;

/**
 * Wiring of the migrated jobs and their {@link JobRunner} (decision 2026-10-15: in-process, one transaction per run, BcsJob contract, no
 * scheduler annotation (RULE-061); the Job Manager calls {@code POST /internal/job-call}). The collaborators are the services of
 * BcsCuentaInversionServicios ({@link ServiciosCuentaInversionConfiguration}) and the ports of {@code cl.bcs.cuentainversion.jobs.adapter}.
 * The four PAUTA steps are plain beans ({@code ProcesoPauta}); how the platform PAUTA runner reaches them is decided in a later phase.
 */
@Configuration(proxyBeanMethods = false)
@Import(ServiciosCuentaInversionConfiguration.class)
public class JobsConfiguration {

    /** The JVM clock in the JVM default zone, as the legacy {@code new Date()} (only used for wall-clock file names). */
    @Bean
    Clock relojDeArchivos() {
        return Clock.systemDefaultZone();
    }

    @Bean
    ActualizarSaldoCargoAbonoJob actualizarSaldoCargoAbonoJob(ActualizarSaldoCargoAbonoService servicio, JdbcTemplate jdbc) {
        return new ActualizarSaldoCargoAbonoJob(servicio, jdbc);
    }

    @Bean
    ActualizarSaldosReporteAntiguedadJob actualizarSaldosReporteAntiguedadJob(ActualizarSaldosReporteAntiguedadService servicio) {
        return new ActualizarSaldosReporteAntiguedadJob(servicio);
    }

    @Bean
    CargaInicialSaldosReporteAntiguedadJob cargaInicialSaldosReporteAntiguedadJob(CargaInicialSaldosReporteAntiguedadService servicio) {
        return new CargaInicialSaldosReporteAntiguedadJob(servicio);
    }

    @Bean
    BcsJobGenerarInformePatrimonioDiario bcsJobGenerarInformePatrimonioDiario(GenerarInformePatrimonioDiarioService servicio,
                                                                              ConfiguracionJobs configuracion, JdbcTemplate jdbc) {
        return new BcsJobGenerarInformePatrimonioDiario(servicio, configuracion, jdbc);
    }

    @Bean
    BcsJobGenerarInformeResumenCobro bcsJobGenerarInformeResumenCobro(GenerarInformeResumenCobroService servicio,
                                                                      ConfiguracionJobs configuracion, JdbcTemplate jdbc) {
        return new BcsJobGenerarInformeResumenCobro(servicio, configuracion, jdbc);
    }

    @Bean
    GenerarArchivoCargoAbonoJob generarArchivoCargoAbonoJob(GenerarArchivoCargoAbonoService servicio, JdbcTemplate jdbc) {
        return new GenerarArchivoCargoAbonoJob(servicio, jdbc);
    }

    @Bean
    GenerarArchivoClientesPorPagarYCobrarJob generarArchivoClientesPorPagarYCobrarJob(
            GenerarArchivoClientesPorPagarYCobrarService servicio, JdbcTemplate jdbc) {
        return new GenerarArchivoClientesPorPagarYCobrarJob(servicio, jdbc);
    }

    @Bean
    GenerarArchivoCuentasPorCobrarAnexo16Job generarArchivoCuentasPorCobrarAnexo16Job(
            GenerarArchivoCuentasPorCobrarAnexo16Service servicio, JdbcTemplate jdbc) {
        return new GenerarArchivoCuentasPorCobrarAnexo16Job(servicio, jdbc);
    }

    @Bean
    GenerarArchivoDiferenciaSaldoJob generarArchivoDiferenciaSaldoJob(ConsultaComparacionSaldoService servicio, JdbcTemplate jdbc) {
        return new GenerarArchivoDiferenciaSaldoJob(servicio, jdbc);
    }

    @Bean
    GenerarArchivoSaldoCuentasJob generarArchivoSaldoCuentasJob(GenerarArchivoSaldoCuentasService servicio, JdbcTemplate jdbc) {
        return new GenerarArchivoSaldoCuentasJob(servicio, jdbc);
    }

    @Bean
    GenerarReporteAcreedoresJob generarReporteAcreedoresJob(GenerarReporteAcreedoresService servicio, JdbcTemplate jdbc) {
        return new GenerarReporteAcreedoresJob(servicio, jdbc);
    }

    @Bean
    GenerarReporteAntiguedadDeudaBolsaCTAJob generarReporteAntiguedadDeudaBolsaCTAJob(
            GenerarReporteAntiguedadDeudaBolsaCTAService servicio, JdbcTemplate jdbc) {
        return new GenerarReporteAntiguedadDeudaBolsaCTAJob(servicio, jdbc);
    }

    @Bean
    GenerarReporteAntiguedadDeudaCTAJob generarReporteAntiguedadDeudaCTAJob(GenerarReporteAntiguedadDeudaCTAService servicio,
                                                                             JdbcTemplate jdbc) {
        return new GenerarReporteAntiguedadDeudaCTAJob(servicio, jdbc);
    }

    @Bean
    GuardarPatrimonioCajaJob guardarPatrimonioCajaJob(GuardarPatrimonioCajaService servicio, ConfiguracionJobs configuracion) {
        return new GuardarPatrimonioCajaJob(servicio, configuracion);
    }

    @Bean
    LibroInventarioJob libroInventarioJob(LibroInventario libroInventario) {
        return new LibroInventarioJob(libroInventario);
    }

    @Bean
    GenerarArchivo generarArchivo(ProcedimientoArchivo procedimientos, JdbcTemplate jdbc, Clock relojDeArchivos) {
        return new GenerarArchivo(procedimientos, jdbc, relojDeArchivos);
    }

    @Bean
    PautaActualizarSaldoCargoAbono pautaActualizarSaldoCargoAbono(ConfiguracionJobs configuracion, ActualizarSaldoCargoAbonoService servicio) {
        return new PautaActualizarSaldoCargoAbono(configuracion, servicio);
    }

    @Bean
    PautaGuardarSaldosReporteAntiguedad pautaGuardarSaldosReporteAntiguedad(ConfiguracionJobs configuracion,
                                                                            GuardarSaldosReporteAntiguedadService servicio) {
        return new PautaGuardarSaldosReporteAntiguedad(configuracion, servicio);
    }

    @Bean
    PautaGenerarReporteAcreedores pautaGenerarReporteAcreedores(ConfiguracionJobs configuracion, Colaboradores colaboradores) {
        return new PautaGenerarReporteAcreedores(configuracion, colaboradores);
    }

    @Bean
    PautaGenerarReporteAntiguedadDeudaBolsaCTA pautaGenerarReporteAntiguedadDeudaBolsaCTA(ConfiguracionJobs configuracion,
                                                                                         Colaboradores colaboradores) {
        return new PautaGenerarReporteAntiguedadDeudaBolsaCTA(configuracion, colaboradores);
    }

    // The JobStatusListener bean lives in ProgramaConfiguration (job pattern H4: real listener in mode programa, logging in caller mode).

    /**
     * BcsJob.java:130-132 session statements on every transaction of a run. On by default for HANA;
     * {@code cuentainversion.jobs.sesion-hana=false} only for a non-HANA database.
     */
    @Bean
    JobRunner jobRunner(PlatformTransactionManager transacciones, JdbcTemplate jdbc, List<CuentaInversionJob> jobs,
                        JobStatusListener listener, @Value("${cuentainversion.jobs.sesion-hana:true}") boolean sesionHana) {
        return new JobRunner(transacciones, jdbc, jobs, listener, sesionHana ? JobRunner.SESION_HANA : List.of());
    }
}
