package cl.bcs.tesoreria.jobs.wiring;

import cl.bcs.tesoreria.infrastructure.adapter.ColaboradoresJavaCall;
import cl.bcs.tesoreria.jobs.adapter.ColaboradoresEnProceso;
import cl.bcs.tesoreria.jobs.adapter.ColaboradoresJobs;
import cl.bcs.tesoreria.jobs.adapter.DespachadorEnProceso;
import cl.bcs.tesoreria.jobs.guarda.GuardaTomee;
import cl.bcs.tesoreria.port.out.Colaboradores;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

/**
 * The {@code cl.bcs.tesoreria.port.out.Colaboradores} port of the job application, chosen by
 * {@code tesoreria.jobs.colaboradores-en-proceso} (step 5, JOBS_PATTERN.md H-1):
 * <ul>
 *   <li>{@code false} (or absent): {@link ColaboradoresJobs} is the {@code @Primary} port, exactly as before step 5 (HTTP adapter for the
 *       collaborators outside Tesoreria, the service adapter of tesoreria-service for the rest);</li>
 *   <li>{@code true}: {@link ColaboradoresEnProceso} is the {@code @Primary} port; it answers the group M collaborators in process (see
 *       {@link EnProcesoConfiguration}) and delegates everything else to the same {@link ColaboradoresJobs}, which is then a plain bean.</li>
 * </ul>
 * The HTTP adapter is created here (not scanned) because being primary or not depends on that property.
 */
@Configuration(proxyBeanMethods = false)
public class ColaboradoresJobsConfiguration {

    static final String PROPIEDAD = "tesoreria.jobs.colaboradores-en-proceso";
    /** Step 5 off AND the step 6 guard off: the wiring that existed before both steps (the HTTP adapter is the primary port itself). */
    static final String SIN_EN_PROCESO_NI_GUARDA = "'${" + PROPIEDAD + ":false}' == 'false' and '${"
            + GuardaTomeeConfiguration.PROPIEDAD + ":true}' == 'false'";
    /** Step 5 off AND the guard on: the guard decorator is the primary port and the HTTP adapter is its (non-primary) delegate. */
    static final String SIN_EN_PROCESO_CON_GUARDA = "'${" + PROPIEDAD + ":false}' == 'false' and '${"
            + GuardaTomeeConfiguration.PROPIEDAD + ":true}' == 'true'";

    @Bean
    @Primary
    @ConditionalOnExpression(SIN_EN_PROCESO_NI_GUARDA)
    ColaboradoresJobs colaboradoresJobs(ColaboradoresJavaCall servicio, @Value("${services.javacall.url}") String urlRouter,
                                        @Value("${services.javacall.connect-timeout:PT2S}") Duration tiempoConexion,
                                        @Value("${services.javacall.read-timeout:PT60S}") Duration tiempoLectura,
                                        @Value("${services.internal-token:}") String tokenInterno) {
        return new ColaboradoresJobs(servicio, urlRouter, tiempoConexion, tiempoLectura, tokenInterno);
    }

    /** Same adapter, not primary: the delegate of {@link ColaboradoresEnProceso} (step 5) or of the guard decorator (step 6). */
    @Bean
    @ConditionalOnExpression("!(" + SIN_EN_PROCESO_NI_GUARDA + ")")
    ColaboradoresJobs colaboradoresJobsHttp(ColaboradoresJavaCall servicio, @Value("${services.javacall.url}") String urlRouter,
                                            @Value("${services.javacall.connect-timeout:PT2S}") Duration tiempoConexion,
                                            @Value("${services.javacall.read-timeout:PT60S}") Duration tiempoLectura,
                                            @Value("${services.internal-token:}") String tokenInterno) {
        return new ColaboradoresJobs(servicio, urlRouter, tiempoConexion, tiempoLectura, tokenInterno);
    }

    /** Step 6 with step 5 off: the primary port is the guard around the HTTP adapter. */
    @Bean
    @Primary
    @ConditionalOnExpression(SIN_EN_PROCESO_CON_GUARDA)
    Colaboradores colaboradoresGuardados(ColaboradoresJobs http, GuardaTomee guarda) {
        return guarda.envolver(http);
    }

    /** Step 5; with the step 6 guard on, only what still goes over HTTP (the non-migrated collaborators and the fallbacks) is guarded. */
    @Bean
    @Primary
    @ConditionalOnProperty(name = PROPIEDAD, havingValue = "true")
    ColaboradoresEnProceso colaboradoresEnProceso(ColaboradoresJobs http, DespachadorEnProceso despachador,
                                                  ObjectProvider<GuardaTomee> guarda) {
        GuardaTomee g = guarda.getIfAvailable();
        return new ColaboradoresEnProceso(g == null ? http : g.envolver(http), despachador);
    }
}
