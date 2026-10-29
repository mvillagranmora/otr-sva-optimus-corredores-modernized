package cl.bcs.plataforma.jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(excludeName = {
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration"
})
@EnableScheduling
public class PlataformaJobsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlataformaJobsApplication.class, args);
    }
}
