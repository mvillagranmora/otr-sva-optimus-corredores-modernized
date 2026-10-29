package cl.bcs.tesoreria;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import cl.bcs.tesoreria.javacall.JavaCallDispatcher;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.tesoreria.port.out.Reloj;

/**
 * Wiring of the whole application: the context starts (every service finds the ports it injects, no bean is defined twice), the production adapters
 * are the ones that answer the shared ports, and every JavaCall class of the dispatcher table has exactly one bean to run it. The database is an
 * in-memory H2 (no HANA, no credential); the router URL and the internal token are fake values of the same shape as the real variables.
 */
@SpringBootTest(classes = TesoreriaServiceApplication.class, properties = {
        "HANA_URL=jdbc:h2:mem:contexto;MODE=LEGACY;DB_CLOSE_DELAY=-1",
        "HANA_USER=sa",
        "HANA_PASSWORD=",
        "JAVACALL_URL=http://127.0.0.1:9",
        "INTERNAL_TOKEN=token-de-prueba",
        "spring.autoconfigure.exclude=" })
class TesoreriaContextoTest {

    @Autowired
    private ApplicationContext contexto;

    @Autowired
    private JavaCallDispatcher dispatcher;

    @Test
    void losPuertosCompartidosTienenUnAdaptadorDeProduccion() {
        assertThat(contexto.getBean(Colaboradores.class).getClass().getSimpleName()).isEqualTo("ColaboradoresJavaCall");
        assertThat(contexto.getBean(DiccionarioMensajes.class).getClass().getSimpleName()).isEqualTo("DiccionarioJdbc");
        assertThat(contexto.getBean(ConfiguracionSistema.class).getClass().getSimpleName()).isEqualTo("ConfiguracionSistemaJdbc");
        assertThat(contexto.getBean(Reloj.class).getClass().getSimpleName()).isEqualTo("RelojSistema");
    }

    @Test
    void cadaClaseJavaCallDelDespachadorTieneExactamenteUnBean() {
        assertThat(dispatcher.servicios()).isNotEmpty().allSatisfy((clase, tipo) -> assertThat(contexto.getBeanNamesForType(tipo))
                .as("beans for " + clase + " (" + tipo.getSimpleName() + ")").hasSize(1));
    }
}
