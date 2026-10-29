package cl.bcs.apv.jobs.wiring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * BcsAPVServicios as an in-process library (decision 2026-10-15): its use cases (registered by its {@code CasosDeUsoConfig}), ports and adapters, WITHOUT
 * its web layer. Excluded: {@code infrastructure.web} (the controllers and its {@code TokenInternoFilter}: this module has its own in
 * {@code cl.bcs.apv.jobs.web}), which belong to the Servicios application. The Servicios {@code @SpringBootApplication} class is outside the scanned
 * packages. Everything runs on the one HANA {@code DataSource} that Spring Boot auto-configures and in the transaction the {@code JobRunner} opens for each run.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(basePackages = "cl.bcs.apv.infrastructure",
        excludeFilters = @Filter(type = FilterType.REGEX, pattern = "cl\\.bcs\\.apv\\.infrastructure\\.web\\..*"))
public class ServiciosApvConfiguration {
}
