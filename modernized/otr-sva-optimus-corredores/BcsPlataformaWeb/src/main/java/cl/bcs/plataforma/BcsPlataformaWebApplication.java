package cl.bcs.plataforma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BcsPlataformaWeb — Spring Boot 4.1.1 / Java 25
 * Port: 8117
 *
 * Transformed from Java EE 8 / TomEE BcsPlataformaWeb.
 *
 * CallServlet dispatcher is implemented as a stub (@RestController POST /call)
 * because the PLA.Servicio registry (1 817+ entries) has not been exported.
 * See TRANSFORMATION_NOTES.md for details.
 */
@SpringBootApplication(scanBasePackages = {"cl.bcs.plataforma", "cl.bcs.utils"})
public class BcsPlataformaWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(BcsPlataformaWebApplication.class, args);
    }
}
