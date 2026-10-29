package cl.bcs.cuentainversion.jobs;

import cl.bcs.cuentainversion.jobs.adapter.ColaboradoresJobs;
import cl.bcs.cuentainversion.jobs.web.JobCallController;
import cl.bcs.cuentainversion.jobs.wiring.JobsConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of bcs-cuenta-inversion-jobs: the legacy BcsCuentaInversionJobs jobs, run in-process on request through
 * {@code POST /internal/job-call} (no scheduler, RULE-061). RULE-076: this is the module's only {@code main()}.
 *
 * <p>The component scan is limited to the packages of the real wiring ({@code wiring}, {@code adapter}, {@code web}). The {@code job},
 * {@code job.pauta}, {@code job.port} and {@code runtime} classes are created by {@link JobsConfiguration}, never by scanning.
 */
@SpringBootApplication(scanBasePackageClasses = {JobsConfiguration.class, ColaboradoresJobs.class, JobCallController.class})
public class BcsCuentaInversionJobsApplication {
    public static void main(String[] args) {
        SpringApplication.run(BcsCuentaInversionJobsApplication.class, args);
    }
}
