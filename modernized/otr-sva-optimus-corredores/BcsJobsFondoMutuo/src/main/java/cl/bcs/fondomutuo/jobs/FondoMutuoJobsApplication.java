package cl.bcs.fondomutuo.jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = {
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration"
})
public class FondoMutuoJobsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FondoMutuoJobsApplication.class, args);
    }
}
