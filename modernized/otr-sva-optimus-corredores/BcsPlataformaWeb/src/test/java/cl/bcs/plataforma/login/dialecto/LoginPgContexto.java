package cl.bcs.plataforma.login.dialecto;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;

/** Spring test plumbing shared by the wiring and 503 tests: a recording DataSource (counts the SQL handed to JDBC) and a replacement catalog bean. */
final class LoginPgContexto {

    /** every SQL text handed to a connection of the application DataSource since the last {@link #limpiar()} */
    static final List<String> SQL = new CopyOnWriteArrayList<>();

    private LoginPgContexto() {
    }

    static void limpiar() {
        SQL.clear();
    }

    /** wraps every DataSource bean of the context with the recording proxy (no second DataSource is added) */
    static final class Grabador implements BeanPostProcessor {
        @Override
        public Object postProcessAfterInitialization(Object bean, String nombre) throws BeansException {
            return bean instanceof DataSource ds ? LoginPgFlujos.grabador(ds, SQL) : bean;
        }
    }

    /** registers a primary CatalogoEquivalencias in which one HANA object is NO_DISPONIBLE (the rest equals the oracle) */
    static final class CatalogoMarcado implements BeanDefinitionRegistryPostProcessor {
        private final String marcado;

        CatalogoMarcado(String marcado) {
            this.marcado = marcado;
        }

        @Override
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            Class<Object> clase = (Class<Object>) ApiLoginPg.clase("CatalogoEquivalencias");
            String objetos = LoginPgRecursos.texto("/loginpg/oraculo-objetos.csv");
            StringBuilder sb = new StringBuilder();
            boolean encontrado = false;
            for (String linea : objetos.split("\n")) {
                String[] p = linea.split(",", -1);
                if (p.length >= 3 && p[1].equals(marcado)) {
                    p[2] = "NO_DISPONIBLE";
                    linea = String.join(",", p);
                    encontrado = true;
                }
                sb.append(linea).append('\n');
            }
            if (!encontrado) throw new IllegalStateException("object to mark not in the oracle: " + marcado);
            Object catalogo = ApiLoginPg.catalogoDeCsv(sb.toString(), LoginPgRecursos.texto("/loginpg/oraculo-columnas.csv"));
            RootBeanDefinition def = new RootBeanDefinition(clase, () -> catalogo);
            def.setPrimary(true);
            registry.registerBeanDefinition("catalogoMarcadoDePrueba", def);
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        }
    }
}
