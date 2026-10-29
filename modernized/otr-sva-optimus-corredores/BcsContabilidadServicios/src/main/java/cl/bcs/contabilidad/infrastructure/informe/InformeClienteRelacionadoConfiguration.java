package cl.bcs.contabilidad.infrastructure.informe;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Supplies the clock that names the related-client report file (legacy {@code LocalDateTime.now()},
 * GenerarInformeClienteRelacionado.java:154), unless the application already defines one.
 */
@Configuration(proxyBeanMethods = false)
public class InformeClienteRelacionadoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Clock relojSistema() {
        return Clock.systemDefaultZone();
    }
}
