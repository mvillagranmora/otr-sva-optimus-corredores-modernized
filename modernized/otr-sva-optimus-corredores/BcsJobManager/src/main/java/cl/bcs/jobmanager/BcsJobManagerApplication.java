package cl.bcs.jobmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot entry point for BcsJobManager.
 *
 * Migrated from legacy cl.bcs.scheduler.tools.BcsJobManagerSingleBean.main().
 * Configuration is supplied via application.yml / environment variables —
 * no hardcoded credentials.
 *
 * Port: 8106 (set in application.yml)
 */
@SpringBootApplication
@EnableScheduling
public class BcsJobManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BcsJobManagerApplication.class, args);
    }
}
