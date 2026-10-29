package cl.bcs.plataforma.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Transaction manager over the module DataSource, explicit (the application excludes the JDBC transaction auto-configuration). It
 * backs {@code JavaCallUnitOfWorkFilter}: one connection and one transaction per JavaCall request.
 */
@Configuration
public class JavaCallTransaccionConfig {

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
