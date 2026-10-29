package cl.bcs.normativa.jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BcsNormativaJobs — Spring Boot 4.1.1 / Java 25 modernization.
 *
 * Exposes HTTP endpoints for triggering CMF regulatory reporting jobs:
 *   - Generación / Validación / Carga / Cuadratura / Exportación
 *   - Generación de interfaces regulatorias (C08, C40, P40, DCI, DPD, etc.)
 *   - Generación de certificados tributarios
 *   - Valorización (NCG 499, Declaración DJ1874)
 *   - Procesamiento de servicios (NOR.ProcesarTarea HANA SP)
 *   - Generación de reportes APV y cartolas
 *
 * Port: 8112 (configurable via SERVER_PORT env var)
 */
@SpringBootApplication
public class NormativaJobsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NormativaJobsApplication.class, args);
    }
}
