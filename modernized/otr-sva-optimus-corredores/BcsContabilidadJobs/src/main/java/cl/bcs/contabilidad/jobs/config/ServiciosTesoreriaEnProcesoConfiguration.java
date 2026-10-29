package cl.bcs.contabilidad.jobs.config;

import cl.bcs.tesoreria.infrastructure.adapter.ConfiguracionSistemaJdbc;
import cl.bcs.tesoreria.javacall.JavaCallDispatcher;
import cl.bcs.tesoreria.javacall.liq.ConsultarClearingFinancieroService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

/**
 * Step 5: ONLY the Tesoreria beans that {@code ConsultarClearingFinanciero} (the group M collaborator of the Contabilidad services) needs, as an
 * in-process library, WITHOUT its web layer, its REST clients, its JavaCall router adapter or the rest of its services: Tesoreria's
 * {@code JavaCallDispatcher} (the entry point; it looks the services up lazily, so only the one scanned here can be reached), the clearing service
 * and the JDBC {@code ConfiguracionSistema} it reads the system date and the chamber parameters with. Nothing of Tesoreria's
 * {@code infrastructure.config} is scanned, so Tesoreria's transaction-manager customizer does not change this application's transactions.
 *
 * <p>The beans are named with their fully qualified class name (several libraries have beans with the same simple name). Everything runs on the one
 * {@code DataSource} of the job application and in the transaction of the run ({@code @Transactional} REQUIRED).
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = ColaboradoresEnProcesoConfiguration.PROPIEDAD, havingValue = "true")
@ComponentScan(
        basePackages = "cl.bcs.tesoreria",
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class,
        useDefaultFilters = false,
        includeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                JavaCallDispatcher.class, ConsultarClearingFinancieroService.class, ConfiguracionSistemaJdbc.class}))
public class ServiciosTesoreriaEnProcesoConfiguration {
}
