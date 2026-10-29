package cl.bcs.tesoreria.jobs;

import cl.bcs.tesoreria.jobs.adapter.ColaboradoresJobs;
import cl.bcs.tesoreria.jobs.web.JobCallController;
import cl.bcs.tesoreria.jobs.wiring.JobsConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of tesoreria-jobs: the legacy BcsTesoreriaJobs jobs, run in-process on request through
 * {@code POST /internal/job-call} (no scheduler, RULE-061). RULE-076: this is the module's only {@code main()}.
 *
 * <p>The component scan is limited to the packages of the real wiring ({@code wiring}, {@code adapter}, {@code web}). The classes
 * of the obsolete Step-0 pilot (packages {@code config}, {@code formatter}, {@code infrastructure}, {@code model}, {@code port.out},
 * {@code service} and the four {@code *Job} Spring Batch tasklets of {@code job}) stay in the source tree until the owner deletes
 * them, but are NOT part of the application: they cannot add beans next to the real wiring.
 */
@SpringBootApplication(scanBasePackageClasses = {JobsConfiguration.class, ColaboradoresJobs.class, JobCallController.class})
public class TesoreriaJobsApplication {

    public static void main(String[] args) {
        SpringApplication.run(TesoreriaJobsApplication.class, args);
    }
}
