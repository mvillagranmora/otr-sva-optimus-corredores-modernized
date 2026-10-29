package cl.bcs.filters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration.
 *
 * Authentication and authorisation are handled entirely by the two custom servlet filters
 * ({@code SessionValidationFilter} and {@code ServiceAccessFilter}) which are registered
 * as {@code @Component} beans and applied before Spring Security's own filter chain.
 *
 * Spring Security is kept on the classpath so we can use the {@code PasswordEncoder}
 * abstraction (HybridPasswordEncoder from bcs-utils).  We disable its built-in
 * authentication mechanisms to avoid double-gating.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // REST API — no browser-based CSRF tokens needed
                .csrf(AbstractHttpConfigurer::disable)
                // Stateless — token carried in X-SESSION-TOKEN header
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // No form-login or HTTP-Basic — custom filters own authentication
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // Permit all at the Spring Security level; custom filters enforce auth
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
