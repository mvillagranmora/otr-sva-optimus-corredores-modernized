package cl.bcs.cartolas.jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * BcsCartolasJobs — Spring Boot 4.1.1 / Java 25
 *
 * Modernización de BcsJobCartolas y BcsScheduledJobCartolas de Java EE 8 / TomEE.
 * Expone endpoints REST para disparar y programar el proceso de cartolas.
 *
 * Puerto: 8104
 */
@SpringBootApplication
@EnableScheduling
public class BcsCartolasJobsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BcsCartolasJobsApplication.class, args);
    }
}
