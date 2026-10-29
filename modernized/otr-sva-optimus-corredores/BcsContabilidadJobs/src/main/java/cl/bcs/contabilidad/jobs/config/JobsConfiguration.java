package cl.bcs.contabilidad.jobs.config;

import cl.bcs.contabilidad.domain.port.out.DiccionarioPort;
import cl.bcs.contabilidad.domain.port.out.InterfazContableRepository;
import cl.bcs.contabilidad.domain.port.out.InterfazContadorSource;
import cl.bcs.contabilidad.domain.port.out.InterfazFileTransfer;
import cl.bcs.contabilidad.domain.port.out.InterfazSapSource;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.port.out.SapGateway;
import cl.bcs.contabilidad.infrastructure.persistence.InterfazContadorJdbcSource;
import cl.bcs.contabilidad.infrastructure.sap.SapB1InterfazJdbcSource;
import cl.bcs.contabilidad.infrastructure.sap.SapB1Jdbc;
import cl.bcs.contabilidad.infrastructure.sftp.JschInterfazFileTransfer;
import cl.bcs.contabilidad.infrastructure.sftp.SftpSettings;
import cl.bcs.contabilidad.jobs.job.CentralizaMultiSeleccionJob;
import cl.bcs.contabilidad.jobs.job.ConexionSap;
import cl.bcs.contabilidad.jobs.job.CreaInterfazDemanda;
import cl.bcs.contabilidad.jobs.job.CreaInterfazDiaria;
import cl.bcs.contabilidad.jobs.job.GenerarInformeClienteRelacionadoBatch;
import cl.bcs.contabilidad.jobs.job.InterfazContable;
import cl.bcs.contabilidad.jobs.runtime.ContabilidadJob;
import cl.bcs.contabilidad.jobs.runtime.JobRunner;
import cl.bcs.contabilidad.jobs.runtime.JobStatusListener;
import cl.bcs.contabilidad.service.CentralizaMultiSeleccionService;
import cl.bcs.contabilidad.service.GenerarInformeClienteRelacionadoService;
import cl.bcs.contabilidad.service.GenerarJsonAsientoSapService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.util.List;
import java.util.function.Supplier;

/**
 * Wiring of the five migrated jobs and their {@link JobRunner} (decision 2026-10-15: in-process, one transaction
 * per run, BcsJob contract, no {@code @Scheduled}; the Job Manager calls {@code POST /internal/job-call}).
 *
 * <p>The collaborators are BcsContabilidadServicios beans ({@link ServiciosContabilidadConfiguration}); no SAP client
 * is duplicated here. The SAP B1 database ({@code SAP_B1_DB_*}), the Service Layer ({@code SAP_B1_*}) and SFTP
 * ({@code SFTP_*}) settings are read from the environment on first use, so the application starts without them and
 * a missing one only fails the job that needs it. The {@link Clock} is the Servicios one (JVM default zone, as the
 * legacy {@code new Date()} / {@code Calendar.getInstance()}).
 */
@Configuration(proxyBeanMethods = false)
public class JobsConfiguration {

    @Bean
    InterfazContadorSource interfazContadorSource(NamedParameterJdbcTemplate jdbc) {
        return new InterfazContadorJdbcSource(jdbc);
    }

    @Bean
    InterfazSapSource interfazSapSource(SapB1Jdbc sapB1Jdbc) {
        return new SapB1InterfazJdbcSource(sapB1Jdbc);
    }

    /** The legacy jobs took one connection of the platform SAP B1 pool; this takes one of {@code SapB1Jdbc}. */
    @Bean
    ConexionSap conexionSap(SapB1Jdbc sapB1Jdbc) {
        return () -> sapB1Jdbc.get().getJdbcTemplate().execute((ConnectionCallback<Void>) conexion -> null);
    }

    @Bean
    Supplier<SftpSettings> sftpSettings() {
        return SftpSettings::fromEnvironment;
    }

    @Bean
    InterfazFileTransfer interfazFileTransfer(Supplier<SftpSettings> sftpSettings) {
        return new JschInterfazFileTransfer(sftpSettings);
    }

    @Bean
    CentralizaMultiSeleccionJob centralizaMultiSeleccionJob(CentralizaMultiSeleccionService servicio) {
        return new CentralizaMultiSeleccionJob(servicio);
    }

    @Bean
    CreaInterfazDiaria creaInterfazDiaria(PlataformaPort plataforma, InterfazContadorSource contador,
                                          InterfazSapSource sap, ConexionSap conexionSap,
                                          InterfazFileTransfer transferencia, Supplier<SftpSettings> sftpSettings,
                                          Clock clock) {
        return new CreaInterfazDiaria(plataforma, contador, sap, conexionSap, transferencia, sftpSettings, clock);
    }

    @Bean
    CreaInterfazDemanda creaInterfazDemanda(PlataformaPort plataforma, InterfazContadorSource contador,
                                            InterfazSapSource sap, ConexionSap conexionSap,
                                            InterfazFileTransfer transferencia, Supplier<SftpSettings> sftpSettings,
                                            Clock clock) {
        return new CreaInterfazDemanda(plataforma, contador, sap, conexionSap, transferencia, sftpSettings, clock);
    }

    @Bean
    InterfazContable interfazContable(PlataformaPort plataforma, GenerarJsonAsientoSapService generador,
                                      SapGateway sap, DiccionarioPort diccionario,
                                      InterfazContableRepository repositorio, Clock clock) {
        return new InterfazContable(plataforma, InterfazContable.servicioConBitacora(generador, sap, diccionario),
                repositorio, clock);
    }

    @Bean
    GenerarInformeClienteRelacionadoBatch generarInformeClienteRelacionadoBatch(
            GenerarInformeClienteRelacionadoService servicio) {
        return new GenerarInformeClienteRelacionadoBatch(servicio);
    }

    /**
     * BcsJob.java:130-132 session statements on every transaction of a run (review HIGH-1). On by default for HANA;
     * {@code contabilidad.jobs.sesion-hana=false} only for a non-HANA database.
     */
    @Bean
    JobRunner jobRunner(PlatformTransactionManager transacciones, JdbcTemplate jdbc, List<ContabilidadJob> jobs,
                        JobStatusListener listener,
                        @Value("${contabilidad.jobs.sesion-hana:true}") boolean sesionHana) {
        return new JobRunner(transacciones, jdbc, jobs, listener, sesionHana ? JobRunner.SESION_HANA : List.of());
    }
}
