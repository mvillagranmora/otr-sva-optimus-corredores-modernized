package cl.bcs.apv.jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import cl.bcs.apv.jobs.adapter.ColaboradoresJobs;
import cl.bcs.apv.jobs.web.JobCallController;
import cl.bcs.apv.jobs.wiring.JobsConfiguration;

/**
 * Entry point of apv-jobs: the legacy BcsAPVJobs jobs, run in-process on request through {@code POST /internal/job-call} (no scheduler, RULE-061).
 * RULE-076: this is the module's only {@code main()}.
 *
 * <p>The component scan is limited to the packages of the real wiring ({@code wiring}, {@code adapter}, {@code web}). The classes of the obsolete
 * first-pass pilot (packages {@code domain}, {@code infrastructure}, {@code port.out} and the four {@code *Job} classes of {@code job}) stay in the
 * source tree until the owner deletes them, but are NOT part of the application: they cannot add beans next to the real wiring.
 */
@SpringBootApplication(scanBasePackageClasses = {JobsConfiguration.class, ColaboradoresJobs.class, JobCallController.class})
public class ApvJobsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApvJobsApplication.class, args);
    }
}
