package cl.bcs.cuentainversion.jobs.wiring;

import cl.bcs.cuentainversion.jobs.adapter.ColaboradoresJobs;
import cl.bcs.cuentainversion.jobs.guarda.ColaboradorTomEEGuardado;
import cl.bcs.cuentainversion.jobs.guarda.GuardaTomee;
import cl.bcs.cuentainversion.jobs.programa.EjecutorPrograma;
import cl.bcs.cuentainversion.port.out.Colaboradores;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.env.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Step 6 OFF switch ({@code cuentainversion.jobs.guarda-tomee=false}, what the test profile sets): the wiring is the one before step 6. */
@SpringBootTest(properties = {"services.javacall.url=http://localhost:9", "cuentainversion.jobs.sesion-hana=false"})
@ActiveProfiles("test")
class GuardaCableadoApagadoTest {

    @Autowired
    ApplicationContext contexto;
    @Autowired
    Colaboradores colaboradores;
    @Autowired
    EjecutorPrograma ejecutor;

    @Test
    @DisplayName("G6-W03: guard off (test profile): no guard beans, the only port is ColaboradoresJobs, the executor has no guard")
    void sinGuardaTodoComoAntes() {
        assertThat(contexto.getBeansOfType(GuardaTomee.class)).isEmpty();
        assertThat(contexto.getBeansOfType(ColaboradorTomEEGuardado.class)).isEmpty();
        assertThat(colaboradores).isInstanceOf(ColaboradoresJobs.class);
        assertThat(contexto.getBeansOfType(Colaboradores.class)).hasSize(1);
        assertThat(ReflectionTestUtils.getField(ejecutor, "guarda")).isNull();
    }

    @Test
    @DisplayName("G6-W04: the production default is ON: application.yml sets guarda-tomee=true (and the configuration is ON when the property is missing)")
    void elValorDeProduccionEsTrue() throws IOException {
        List<PropertySource<?>> fuentes = new YamlPropertySourceLoader().load("application", new ClassPathResource("application.yml"));
        assertThat(fuentes.get(0).getProperty(GuardaTomeeConfiguration.PROPIEDAD)).isEqualTo(true);
        assertThat(GuardaTomeeConfiguration.class.getAnnotation(org.springframework.boot.autoconfigure.condition.ConditionalOnProperty.class)
                .matchIfMissing()).isTrue();
    }
}
