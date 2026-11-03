package cl.bcs.plataforma.login.dialecto;

import org.slf4j.LoggerFactory;
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
    SqlDialecto sqlDialecto(@Value("${" + PROPIEDAD + ":hana}") String valor, @Value("${spring.datasource.url:}") String urlJdbc) {
        return switch (valor) {
            case "hana" -> new HanaDialecto();
            case "postgres" -> {
                if (urlJdbc.startsWith("jdbc:postgresql:") && !urlJdbc.contains("escapeSyntaxCallMode=callIfNoReturn")) {
                    LoggerFactory.getLogger(DialectoConfiguracion.class).warn(
                            "plataforma.db.dialect=postgres but the JDBC URL has no escapeSyntaxCallMode=callIfNoReturn: pgjdbc turns {call p(...)} into a SELECT and the Postgres procedures (PROCEDURE) fail with 42809");
                }
                if (urlJdbc.startsWith("jdbc:postgresql:") && !urlJdbc.contains("stringtype=unspecified")) {
                    LoggerFactory.getLogger(DialectoConfiguracion.class).warn(
                            "plataforma.db.dialect=postgres but the JDBC URL has no stringtype=unspecified: text and null binds are sent as varchar and writes to bigint, date or timestamp columns can fail");
                }
                yield new PostgresDialecto();
            }
            default -> throw new IllegalStateException(PROPIEDAD + " must be 'hana' or 'postgres', got '" + valor + "'");
        };
    }

    @Bean
    SentenciasSql sentenciasSql(SqlDialecto dialecto) {
        return new SentenciasSql(dialecto);
    }
}
