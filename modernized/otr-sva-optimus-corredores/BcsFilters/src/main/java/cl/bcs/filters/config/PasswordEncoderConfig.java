package cl.bcs.filters.config;

import cl.bcs.utils.security.HybridPasswordEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Exposes {@link HybridPasswordEncoder} (from bcs-utils) as the platform
 * {@link PasswordEncoder} bean consumed by {@link cl.bcs.filters.service.LoginService}.
 *
 * The scan base-package in BcsFiltersApplication already includes {@code cl.bcs.utils},
 * so {@code HybridPasswordEncoder} is picked up as a {@code @Component} with its
 * {@code @Value} bindings resolved from application.yml.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    PasswordEncoder passwordEncoder(HybridPasswordEncoder hybridPasswordEncoder) {
        return hybridPasswordEncoder;
    }
}
