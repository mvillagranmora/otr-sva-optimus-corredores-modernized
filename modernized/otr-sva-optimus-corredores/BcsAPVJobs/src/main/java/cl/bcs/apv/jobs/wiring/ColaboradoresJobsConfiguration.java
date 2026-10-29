package cl.bcs.apv.jobs.wiring;

import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.infrastructure.adapter.ColaboradoresJavaCall;
import cl.bcs.apv.jobs.adapter.ColaboradoresJobs;
import cl.bcs.apv.jobs.guarda.GuardaTomee;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * The {@code cl.bcs.apv.domain.port.out.Colaboradores} port of the job application, chosen by {@code apv.jobs.guarda-tomee} (step 6,
 * JOBS_PATTERN.md H-1 point 2):
 * <ul>
 *   <li>{@code false}: {@link ColaboradoresJobs} is the {@code @Primary} port, exactly as before step 6 (it used to be a scanned
 *       {@code @Component @Primary});</li>
 *   <li>{@code true} (or absent): the guard decorator is the {@code @Primary} port and {@link ColaboradoresJobs} (bean {@code colaboradoresJobsHttp})
 *       is its non-primary delegate: the guard wraps the HTTP side only. BcsAPVJobs has no in-process collaborator dispatch (step 5), so every
 *       collaborator call of a run goes through the guard.</li>
 * </ul>
 * The HTTP adapter is created here (not scanned) because being primary or not depends on that property.
 */
@Configuration(proxyBeanMethods = false)
public class ColaboradoresJobsConfiguration {

    @Bean
    @Primary
    @ConditionalOnProperty(name = GuardaTomeeConfiguration.PROPIEDAD, havingValue = "false")
    ColaboradoresJobs colaboradoresJobs(ColaboradoresJavaCall servicio, @Value("${services.javacall.url}") String urlRouter,
                                        @Value("${services.javacall.connect-timeout:PT2S}") Duration tiempoConexion,
                                        @Value("${services.javacall.read-timeout:PT60S}") Duration tiempoLectura,
                                        @Value("${services.internal-token:}") String tokenInterno) {
        return new ColaboradoresJobs(servicio, urlRouter, tiempoConexion, tiempoLectura, tokenInterno);
    }

    /** Same adapter, not primary: the delegate of the guard decorator. */
    @Bean
    @ConditionalOnProperty(name = GuardaTomeeConfiguration.PROPIEDAD, havingValue = "true", matchIfMissing = true)
    ColaboradoresJobs colaboradoresJobsHttp(ColaboradoresJavaCall servicio, @Value("${services.javacall.url}") String urlRouter,
                                            @Value("${services.javacall.connect-timeout:PT2S}") Duration tiempoConexion,
                                            @Value("${services.javacall.read-timeout:PT60S}") Duration tiempoLectura,
                                            @Value("${services.internal-token:}") String tokenInterno) {
        return new ColaboradoresJobs(servicio, urlRouter, tiempoConexion, tiempoLectura, tokenInterno);
    }

    /** Step 6: the primary port is the guard around the HTTP adapter. */
    @Bean
    @Primary
    @ConditionalOnProperty(name = GuardaTomeeConfiguration.PROPIEDAD, havingValue = "true", matchIfMissing = true)
    Colaboradores colaboradoresGuardados(ColaboradoresJobs colaboradoresJobsHttp, GuardaTomee guarda) {
        return guarda.envolver(colaboradoresJobsHttp);
    }
}
