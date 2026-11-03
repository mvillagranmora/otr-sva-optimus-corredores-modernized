package cl.bcs.plataforma.login.dialecto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Chooses the SQL dialect of the login with {@code plataforma.db.dialect}: {@code hana} (the default, what the module has always done) or
 * {@code postgres}. Any other value stops the start. One data source, one active dialect per deployment.
 */
@Configuration
public class DialectoConfiguracion {

    public static final String PROPIEDAD = "plataforma.db.dialect";

    @Bean
    CatalogoEquivalencias catalogoEquivalencias() {
        return CatalogoEquivalencias.desdeRecursos();
    }

    @Bean
    SqlDialecto sqlDialecto(@Value("${" + PROPIEDAD + ":hana}") String valor, CatalogoEquivalencias catalogo) {
        return switch (valor) {
            case "hana" -> new HanaDialecto(catalogo);
            case "postgres" -> new PostgresDialecto(catalogo);
            default -> throw new IllegalStateException(PROPIEDAD + " must be 'hana' or 'postgres', got '" + valor + "'");
        };
    }

    @Bean
    SentenciasSql sentenciasSql(SqlDialecto dialecto) {
        return new SentenciasSql(dialecto);
    }
}
