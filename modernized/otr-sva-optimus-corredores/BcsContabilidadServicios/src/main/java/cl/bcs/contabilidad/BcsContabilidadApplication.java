package cl.bcs.contabilidad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 4.1.1 entry point for BcsContabilidadServicios.
 *
 * Phase 1 of the strangler-fig migration from TomEE 8 / Java EE 8.
 * Accounting centralisation services for the Chilean broker-dealer platform.
 *
 * Runtime datasource (SAP HANA): read from HANA_URL, HANA_USER, HANA_PASSWORD env vars.
 * Legacy TomEE proxy: read from PLATAFORMA_BASE_URL env var.
 */
@SpringBootApplication
public class BcsContabilidadApplication {

    public static void main(String[] args) {
        SpringApplication.run(BcsContabilidadApplication.class, args);
    }
}
