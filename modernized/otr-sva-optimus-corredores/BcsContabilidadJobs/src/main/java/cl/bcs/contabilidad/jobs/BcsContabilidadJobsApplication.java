package cl.bcs.contabilidad.jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of bcs-contabilidad-jobs: the legacy BcsContabilidadJobs jobs, run in-process on request through
 * {@code POST /internal/job-call} (no scheduler). RULE-076: this is the module's only {@code main()}.
 */
@SpringBootApplication
public class BcsContabilidadJobsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BcsContabilidadJobsApplication.class, args);
    }
}
