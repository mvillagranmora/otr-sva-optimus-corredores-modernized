package cl.bcs.tesoreria.jobs.wiring;

import cl.bcs.cuentainversion.infrastructure.adapter.DiccionarioJdbc;
import cl.bcs.cuentainversion.service.ActualizarSaldoCargoAbonoService;
import cl.bcs.cuentainversion.service.AnularCargoAbonoService;
import cl.bcs.cuentainversion.service.CalendarioHabil;
import cl.bcs.cuentainversion.service.CrearCargoAbonoService;
import cl.bcs.cuentainversion.service.FormatoMoneda;
import cl.bcs.cuentainversion.service.JavaCallDispatcher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

/**
 * Step 5: ONLY the CuentaInversion beans that {@code CrearCargoAbono} and {@code AnularCargoAbono} (group M collaborators of the Tesoreria jobs) need,
 * as an in-process library, WITHOUT its web layer, its REST clients or the rest of its services (the same limited scan as
 * {@code ServiciosCuentaInversionConfiguration} of BcsCuentaInversionJobs).
 *
 * <p>The beans are named with their fully qualified class name: bcs-cuenta-inversion-servicios and tesoreria-service both have a
 * {@code JavaCallDispatcher} and a {@code DiccionarioJdbc}, and the default simple-name bean names would collide. Its {@code ConfiguracionSistemaPort}
 * and {@code Colaboradores} are provided by {@link EnProcesoConfiguration} (the library ones are REST clients and are not scanned). Everything
 * runs on the one {@code DataSource} of the job application and in the transaction of the run ({@code @Transactional} REQUIRED).
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = ColaboradoresJobsConfiguration.PROPIEDAD, havingValue = "true")
@ComponentScan(
        basePackages = "cl.bcs.cuentainversion",
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class,
        useDefaultFilters = false,
        includeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                CrearCargoAbonoService.class, AnularCargoAbonoService.class, ActualizarSaldoCargoAbonoService.class,
                CalendarioHabil.class, FormatoMoneda.class, DiccionarioJdbc.class, JavaCallDispatcher.class}))
public class ServiciosCuentaInversionEnProcesoConfiguration {
}
