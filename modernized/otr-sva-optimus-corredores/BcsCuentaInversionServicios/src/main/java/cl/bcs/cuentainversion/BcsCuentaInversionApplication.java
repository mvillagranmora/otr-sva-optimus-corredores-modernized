package cl.bcs.cuentainversion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// JPA auto-configuration exclusions removed: this module uses spring-boot-starter-jdbc only (no JPA/Hibernate).
@SpringBootApplication
public class BcsCuentaInversionApplication {
    public static void main(String[] args) {
        SpringApplication.run(BcsCuentaInversionApplication.class, args);
    }
}
