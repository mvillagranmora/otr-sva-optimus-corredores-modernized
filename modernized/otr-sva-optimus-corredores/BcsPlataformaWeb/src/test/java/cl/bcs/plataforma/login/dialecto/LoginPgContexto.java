package cl.bcs.plataforma.login.dialecto;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Spring test plumbing shared by the wiring and 503 tests: a recording DataSource (counts the SQL handed to JDBC) and a test double of
 * {@link SentenciasSql} in which some statements have NO Postgres text (the NO_DISPONIBLE catalog mark of the earlier rounds is gone: a statement without
 * text is the whole 503 mechanism, A3-3).
 */
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

    /**
     * The test double of {@link SentenciasSql}: it behaves as the production class for every statement, except that the given ids have no text when the
     * dialect is Postgres (it answers what production answers for a missing resource: {@link cl.bcs.plataforma.config.NoDisponibleException} naming the id and the dialect).
     * Both {@code texto} and {@code exigirDisponibles} are overridden (A3-3 makes them public and non-final for this purpose), so the double cuts the
     * pre-flight and the statement itself whatever way the services reach them.
     */
    static class SinTextoPostgres extends SentenciasSql {
        private final Set<String> sinTexto;

        SinTextoPostgres(SqlDialecto dialecto, String... ids) {
            super(dialecto);
            this.sinTexto = Set.of(ids);
        }

        @Override
        public String texto(String id) {
            if (sinTexto.contains(id) && "postgres".equals(dialecto().nombre())) {
                throw new cl.bcs.plataforma.config.NoDisponibleException("Statement " + id + " has no postgres text (test double)");
            }
            return super.texto(id);
        }

        @Override
        public void exigirDisponibles(Collection<String> ids) {
            for (String id : ids) texto(id);
        }
    }
}
