package cl.bcs.apv.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import cl.bcs.apv.api.CasoUso;

/**
 * Registers every use case implementation of {@code cl.bcs.apv.application} (any class that implements a {@link CasoUso} interface of
 * {@code bcs-apv-api}) as a Spring bean, with no per-class annotation: the services are plain classes with ONE public constructor that declares its ports,
 * repository ports and peer use case interfaces by type. The same rule builds the in-process graph of {@code ApvCasosDeUso}.
 * A write use case carries {@code @Transactional(rollbackFor = Exception.class)} on its class.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "cl.bcs.apv.application", useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { CasoUso.class,
                cl.bcs.apv.api.logica.ValidarMandatosPACLogica.class }))
public class CasosDeUsoConfig {
}
