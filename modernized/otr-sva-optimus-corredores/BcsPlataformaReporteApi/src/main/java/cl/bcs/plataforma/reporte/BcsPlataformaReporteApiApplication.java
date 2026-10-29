package cl.bcs.plataforma.reporte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Thin compatibility shim for BcsPlataformaReporteApi (Crystal Reports legacy).
 *
 * <p>This module is TEMPORARY. It exists only to serve any residual HTTP callers
 * that still point to the old TomEE endpoint during the canary transition window.
 * All callers have been migrated to use {@code CrystalReportsPort} from {@code bcs-utils},
 * which delegates directly to {@code BcsCrystalReports} on port 8100.</p>
 *
 * <p>Decommission this module when TomEE is shut down.</p>
 */
@SpringBootApplication
public class BcsPlataformaReporteApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BcsPlataformaReporteApiApplication.class, args);
    }
}
