package cl.bcs.tesoreria.jobs.wiring;

import cl.bcs.tesoreria.jobs.guarda.ContextoGuarda;
import cl.bcs.tesoreria.jobs.guarda.GuardaTomee;
import cl.bcs.tesoreria.jobs.guarda.MarcasRepositorio;
import cl.bcs.tesoreria.jobs.guarda.MarcasRepositorioJdbc;
import cl.bcs.tesoreria.jobs.programa.ProgramaModo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Step 6, fail-closed guard for the collaborators that still live in TomEE and write ({@code tesoreria.jobs.guarda-tomee}, default {@code true}
 * in {@code application.properties} and when the property is missing; {@code false} in the golden/test profile). Off: none of these beans exists
 * and the wiring is exactly the one of step 5. The marks go to the existing {@code BAT.LogPrograma} through their own connections.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = GuardaTomeeConfiguration.PROPIEDAD, havingValue = "true", matchIfMissing = true)
public class GuardaTomeeConfiguration {

    public static final String PROPIEDAD = "tesoreria.jobs.guarda-tomee";

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
}
