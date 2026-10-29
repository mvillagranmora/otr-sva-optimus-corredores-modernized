package cl.bcs.apv.jobs.wiring;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import cl.bcs.apv.api.casouso.CargarArchivoPrevired;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.jobs.job.BcsJobCargaMasivaPrevired;
import cl.bcs.apv.jobs.job.IngresarOrdenBonificacionFiscal;
import cl.bcs.apv.jobs.job.IngresarOrdenFMBonificacionFiscal;
import cl.bcs.apv.jobs.job.IngresarOrdenRVBonificacionFiscal;
import cl.bcs.apv.jobs.runtime.ApvJob;
import cl.bcs.apv.jobs.runtime.JobRunner;
import cl.bcs.apv.jobs.runtime.JobStatusListener;

/**
 * Wiring of the four migrated jobs and their {@link JobRunner} (decision 2026-10-15: in-process, one transaction per run, BcsJob contract, no
 * {@code @Scheduled}; the Job Manager calls {@code POST /internal/job-call}). The collaborators are the beans of BcsAPVServicios
 * ({@link ServiciosApvConfiguration}) and the {@code Colaboradores} port of {@code cl.bcs.apv.jobs.adapter}.
 */
@Configuration(proxyBeanMethods = false)
public class JobsConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JobsConfiguration.class);

    @Bean
    BcsJobCargaMasivaPrevired bcsJobCargaMasivaPrevired(CargarArchivoPrevired cargarArchivoPrevired, ConfiguracionSistema configuracion, Reloj reloj) {
        return new BcsJobCargaMasivaPrevired(cargarArchivoPrevired, configuracion, reloj);
    }

    @Bean
    IngresarOrdenBonificacionFiscal ingresarOrdenBonificacionFiscal(NamedParameterJdbcTemplate jdbc, Colaboradores colaboradores) {
        return new IngresarOrdenBonificacionFiscal(jdbc, colaboradores);
    }

    @Bean
    IngresarOrdenFMBonificacionFiscal ingresarOrdenFMBonificacionFiscal(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion,
            Colaboradores colaboradores) {
        return new IngresarOrdenFMBonificacionFiscal(jdbc, configuracion, colaboradores);
    }

    @Bean
    IngresarOrdenRVBonificacionFiscal ingresarOrdenRVBonificacionFiscal(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion,
            Colaboradores colaboradores) {
        return new IngresarOrdenRVBonificacionFiscal(jdbc, configuracion, colaboradores);
    }

    // The JobStatusListener bean moved to ProgramaConfiguration (job pattern H4): ProgramaStatusListener in mode programa, logging otherwise.

    /**
     * BcsJob.java:130-131 session statements on every transaction of a run. On by default for HANA; {@code apv.jobs.sesion-hana=false} only for a
     * non-HANA database.
     */
    @Bean
    JobRunner jobRunner(PlatformTransactionManager transacciones, JdbcTemplate jdbc, List<ApvJob> jobs, JobStatusListener listener,
            @Value("${apv.jobs.sesion-hana:true}") boolean sesionHana) {
        return new JobRunner(transacciones, jdbc, jobs, listener, sesionHana ? JobRunner.SESION_HANA : List.of());
    }
}
