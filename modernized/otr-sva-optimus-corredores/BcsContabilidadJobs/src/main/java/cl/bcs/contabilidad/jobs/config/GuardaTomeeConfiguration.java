package cl.bcs.contabilidad.jobs.config;

import cl.bcs.contabilidad.domain.port.out.ProcesoBatchPort;
import cl.bcs.contabilidad.infrastructure.plataforma.TomEeProcesoBatchClient;
import cl.bcs.contabilidad.jobs.guarda.ContextoGuarda;
import cl.bcs.contabilidad.jobs.guarda.GuardaTomee;
import cl.bcs.contabilidad.jobs.guarda.MarcasRepositorio;
import cl.bcs.contabilidad.jobs.guarda.MarcasRepositorioJdbc;
import cl.bcs.contabilidad.jobs.programa.ProgramaModo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Step 6, fail-closed guard for the collaborators that still live in TomEE and write ({@code contabilidad.jobs.guarda-tomee}, default {@code true}
 * in {@code application.yml} and when the property is missing; {@code false} in the golden/test profile). Off: none of these beans exists
 * and the wiring is exactly the one of step 5. The marks go to the existing {@code BAT.LogPrograma} through their own connections.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = GuardaTomeeConfiguration.PROPIEDAD, havingValue = "true", matchIfMissing = true)
public class GuardaTomeeConfiguration {

    public static final String PROPIEDAD = "contabilidad.jobs.guarda-tomee";

    @Bean
    ContextoGuarda contextoGuarda(ProgramaModo modo) {
        return new ContextoGuarda(modo == ProgramaModo.PROGRAMA); // step 6b: only mode programa refuses a guarded call that has no scope
    }

    @Bean
    MarcasRepositorio marcasRepositorio(DataSource dataSource) {
        return new MarcasRepositorioJdbc(dataSource);
    }

    @Bean
    GuardaTomee guardaTomee(MarcasRepositorio marcas, ContextoGuarda contexto) {
        return new GuardaTomee(marcas, contexto);
    }

    /**
     * The batch program registration (HTTP, TW #17) becomes the guarded port: {@code TomEeProcesoBatchClient} stays a plain bean (the delegate) and
     * this one is the {@code @Primary} {@code ProcesoBatchPort}. {@code PlataformaPort} is NOT wrapped: both of its HTTP names are reads (see
     * {@code ClasificacionColaborador}); the in-process {@code PlataformaEnProceso} of step 5 is untouched.
     */
    @Bean
    @Primary
    ProcesoBatchPort procesoBatchGuardado(TomEeProcesoBatchClient http, GuardaTomee guarda) {
        return guarda.envolver(http);
    }
}
