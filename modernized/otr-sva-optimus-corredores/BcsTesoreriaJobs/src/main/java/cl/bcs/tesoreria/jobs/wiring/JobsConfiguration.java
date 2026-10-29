package cl.bcs.tesoreria.jobs.wiring;

import cl.bcs.tesoreria.javacall.ActivarMandatosArchivoCargaService;
import cl.bcs.tesoreria.javacall.CargaMasivaIngresoEgresoDAPFFMMService;
import cl.bcs.tesoreria.javacall.ConsultarFormatoArchivoService;
import cl.bcs.tesoreria.javacall.ProcesarDistribucionService;
import cl.bcs.tesoreria.jobs.job.BcsJobCargaMasivaIngresoEgresoDAPFFMM;
import cl.bcs.tesoreria.jobs.job.BcsJobProcesarDistribucion;
import cl.bcs.tesoreria.jobs.job.GenerarArchivoCargos;
import cl.bcs.tesoreria.jobs.job.GenerarArchivoPago;
import cl.bcs.tesoreria.jobs.job.port.CabecerasCarga;
import cl.bcs.tesoreria.jobs.job.port.ProcedimientoArchivo;
import cl.bcs.tesoreria.jobs.runtime.JobRunner;
import cl.bcs.tesoreria.jobs.runtime.JobStatusListener;
import cl.bcs.tesoreria.jobs.runtime.TesoreriaJob;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.tesoreria.port.out.Reloj;
import cl.bcs.tesoreria.port.out.Secuencias;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.util.List;

/**
 * Wiring of the four migrated jobs and their {@link JobRunner} (decision 2026-10-15: in-process, one transaction per run, BcsJob
 * contract, no {@code @Scheduled}; the Job Manager calls {@code POST /internal/job-call}). The collaborators are the beans of
 * BcsTesoreriaServicios ({@link ServiciosTesoreriaConfiguration}) and the ports of {@code cl.bcs.tesoreria.jobs.adapter}.
 */
@Configuration(proxyBeanMethods = false)
public class JobsConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JobsConfiguration.class);

    /** The JVM clock in the JVM default zone, as the legacy {@code new Date()} (only used for wall-clock file names). */
    @Bean
    Clock relojDeArchivos() {
        return Clock.systemDefaultZone();
    }

    @Bean
    BcsJobProcesarDistribucion bcsJobProcesarDistribucion(ProcesarDistribucionService distribucion,
                                                          Colaboradores colaboradores, ConfiguracionSistema configuracion,
                                                          Reloj reloj) {
        return new BcsJobProcesarDistribucion(distribucion, colaboradores, configuracion, reloj);
    }

    @Bean
    BcsJobCargaMasivaIngresoEgresoDAPFFMM bcsJobCargaMasivaIngresoEgresoDAPFFMM(
            CargaMasivaIngresoEgresoDAPFFMMService carga, CabecerasCarga cabeceras, Colaboradores colaboradores,
            ConfiguracionSistema configuracion, DiccionarioMensajes diccionario, Reloj reloj, JdbcTemplate jdbc) {
        return new BcsJobCargaMasivaIngresoEgresoDAPFFMM(carga, cabeceras, colaboradores, configuracion, diccionario, reloj,
                jdbc);
    }

    @Bean
    GenerarArchivoCargos generarArchivoCargos(ConsultarFormatoArchivoService formatos,
                                              ActivarMandatosArchivoCargaService mandatos,
                                              ProcedimientoArchivo procedimientos, Secuencias secuencias, JdbcTemplate jdbc,
                                              Clock relojDeArchivos) {
        return new GenerarArchivoCargos(formatos, mandatos, procedimientos, secuencias, jdbc, relojDeArchivos);
    }

    @Bean
    GenerarArchivoPago generarArchivoPago(ProcedimientoArchivo procedimientos, ConfiguracionSistema configuracion,
                                          JdbcTemplate jdbc, Clock relojDeArchivos) {
        return new GenerarArchivoPago(procedimientos, configuracion, jdbc, relojDeArchivos);
    }

    /* The JobStatusListener bean now lives in ProgramaConfiguration (job pattern H4): it writes the final state to BAT.Programa. */

    /**
     * BcsJob.java:130-132 session statements on every transaction of a run. On by default for HANA;
     * {@code tesoreria.jobs.sesion-hana=false} only for a non-HANA database.
     */
    @Bean
    JobRunner jobRunner(PlatformTransactionManager transacciones, JdbcTemplate jdbc, List<TesoreriaJob> jobs,
                        JobStatusListener listener, @Value("${tesoreria.jobs.sesion-hana:true}") boolean sesionHana) {
        return new JobRunner(transacciones, jdbc, jobs, listener, sesionHana ? JobRunner.SESION_HANA : List.of());
    }
}
