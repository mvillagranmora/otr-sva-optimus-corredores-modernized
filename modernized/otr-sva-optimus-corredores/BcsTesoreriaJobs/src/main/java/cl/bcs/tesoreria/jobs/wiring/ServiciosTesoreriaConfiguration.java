package cl.bcs.tesoreria.jobs.wiring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * BcsTesoreriaServicios as an in-process library (decision 2026-10-15): its services, domain and adapters, WITHOUT its web layer.
 *
 * <p>Excluded: {@code adapter.in} ({@code /internal/java-call}) and {@code infrastructure.web} (its {@code TokenInternoFilter}; this
 * module has its own in {@code cl.bcs.tesoreria.jobs.web}), which belong to the Servicios application. The Servicios
 * {@code @SpringBootApplication} class is outside the scanned packages. Everything runs on the one HANA {@code DataSource} that Spring
 * Boot auto-configures and in the transaction the {@code JobRunner} opens for each run.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(
        basePackages = {"cl.bcs.tesoreria.javacall", "cl.bcs.tesoreria.domain", "cl.bcs.tesoreria.infrastructure"},
        excludeFilters = @Filter(type = FilterType.REGEX,
                pattern = "cl\\.bcs\\.tesoreria\\.infrastructure\\.web\\..*"))
public class ServiciosTesoreriaConfiguration {
}
