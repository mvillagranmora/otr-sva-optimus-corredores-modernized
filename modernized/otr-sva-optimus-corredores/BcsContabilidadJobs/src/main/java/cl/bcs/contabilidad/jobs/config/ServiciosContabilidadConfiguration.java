package cl.bcs.contabilidad.jobs.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * BcsContabilidadServicios as an in-process library (decision 2026-10-15): its services, domain and adapters,
 * WITHOUT its web layer and its own daily job.
 *
 * <p>Excluded: {@code adapter.in} ({@code /internal/java-call}) and {@code infrastructure.web} (the JavaCall unit of
 * work filter), which belong to the Servicios application, and {@code infrastructure.batch} (its
 * {@code CreaInterfazDiariaJob} wiring; the interface ports are wired by {@link JobsConfiguration} instead). The
 * Servicios {@code @SpringBootApplication} class is outside the scanned packages. Everything runs on the one HANA
 * {@code DataSource} that Spring Boot auto-configures; the SAP B1 database pool ({@code SapB1Jdbc}) is deliberately
 * not a {@code DataSource} bean.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(
        basePackages = {"cl.bcs.contabilidad.service", "cl.bcs.contabilidad.domain",
                "cl.bcs.contabilidad.infrastructure"},
        excludeFilters = @Filter(type = FilterType.REGEX,
                pattern = "cl\\.bcs\\.contabilidad\\.infrastructure\\.(web|batch)\\..*"))
public class ServiciosContabilidadConfiguration {
}
