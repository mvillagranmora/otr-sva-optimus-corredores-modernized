package cl.bcs.filters;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BcsFilters — authentication and authorisation gateway.
 *
 * Provides:
 *  - Session-token validation filter (SessionValidationFilter)
 *  - Service-access control filter (ServiceAccessFilter)
 *  - Login endpoint with security fixes (RULE-018, RULE-040)
 *  - SSO endpoints with security fixes (RULE-045, RULE-046)
 *
 * Security fixes applied intentionally; see TRANSFORMATION_NOTES.md.
 */
@SpringBootApplication(scanBasePackages = {"cl.bcs.filters", "cl.bcs.utils"})
public class BcsFiltersApplication {

    public static void main(String[] args) {
        SpringApplication.run(BcsFiltersApplication.class, args);
    }
}
