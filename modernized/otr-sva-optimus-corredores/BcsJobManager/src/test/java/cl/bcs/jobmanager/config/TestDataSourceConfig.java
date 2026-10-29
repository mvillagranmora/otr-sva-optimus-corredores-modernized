package cl.bcs.jobmanager.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Test-only DataSource configuration backed by H2 in-memory.
 * Initializes schema-h2.sql automatically via EmbeddedDatabaseBuilder.
 */
@TestConfiguration
public class TestDataSourceConfig {

    @Bean("jobManagerDataSource")
    @Primary
    public DataSource jobManagerDataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("jm_test")
                .addScript("classpath:schema-h2.sql")
                .build();
    }

    @Bean("jobsDataSource")
    public DataSource jobsDataSource() {
        // Reuse same H2 instance as jobManager for tests
        return jobManagerDataSource();
    }

    @Bean("jobManagerJdbcTemplate")
    public NamedParameterJdbcTemplate jobManagerJdbcTemplate(
            @Qualifier("jobManagerDataSource") DataSource ds) {
        return new NamedParameterJdbcTemplate(ds);
    }

    @Bean("jobsJdbcTemplate")
    public NamedParameterJdbcTemplate jobsJdbcTemplate(
            @Qualifier("jobsDataSource") DataSource ds) {
        return new NamedParameterJdbcTemplate(ds);
    }

    @Bean
    public PlatformTransactionManager transactionManager(
            @Qualifier("jobManagerDataSource") DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }
}
