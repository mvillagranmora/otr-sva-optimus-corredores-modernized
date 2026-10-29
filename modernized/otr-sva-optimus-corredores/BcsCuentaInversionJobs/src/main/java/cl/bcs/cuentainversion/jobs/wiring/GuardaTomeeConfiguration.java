package cl.bcs.cuentainversion.jobs.wiring;

import cl.bcs.cuentainversion.jobs.adapter.ColaboradoresJobs;
import cl.bcs.cuentainversion.jobs.guarda.ContextoGuarda;
import cl.bcs.cuentainversion.jobs.guarda.GuardaTomee;
import cl.bcs.cuentainversion.jobs.guarda.MarcasRepositorio;
import cl.bcs.cuentainversion.jobs.guarda.MarcasRepositorioJdbc;
import cl.bcs.cuentainversion.jobs.programa.ProgramaModo;
import cl.bcs.cuentainversion.port.out.Colaboradores;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Step 6, fail-closed guard for the collaborators that still live in TomEE and write ({@code cuentainversion.jobs.guarda-tomee}, default
 * {@code true} in {@code application.yml} and when the property is missing; {@code false} in the golden/test profile). Off: none of these beans
 * exists and the wiring is exactly the one before step 6 ({@code ColaboradoresJobs} is the only {@code Colaboradores} bean). On: the guard
 * decorator is the {@code @Primary} port that the services and the PAUTA steps receive, and {@code ColaboradoresJobs} is its delegate. The
 * marks go to the existing {@code BAT.LogPrograma} through their own connections.
 *
 * <p>The guard wraps {@code ColaboradoresJobs} as a whole, which answers the {@code cl.bcs.cuentaInversion.*} classes in process; those names are
 * classified {@code MIGRADO_PROPIO} (not guarded), so only a call that goes on to the platform router can leave marks.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = GuardaTomeeConfiguration.PROPIEDAD, havingValue = "true", matchIfMissing = true)
public class GuardaTomeeConfiguration {

    public static final String PROPIEDAD = "cuentainversion.jobs.guarda-tomee";

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

    /** The primary port: the guard around the HTTP / in-process adapter of this module. */
    @Bean
    @Primary
    Colaboradores colaboradoresGuardados(ColaboradoresJobs colaboradoresJobs, GuardaTomee guarda) {
        return guarda.envolver(colaboradoresJobs);
    }
}
