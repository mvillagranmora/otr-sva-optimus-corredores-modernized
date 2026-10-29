package cl.bcs.jobmanager.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * DataSource configuration for BcsJobManager.
 *
 * Uses environment variables for credentials — no hardcoded passwords.
 * See application.yml for property bindings.
 *
 * Skipped in @ActiveProfiles("test") — TestDataSourceConfig takes over.
 */
@Configuration
@Profile("!test")
public class DataSourceConfig {

    /**
     * Job Manager data source (schema/control tables).
     * Credentials sourced from ${HANA_URL}, ${HANA_USER}, ${HANA_PASSWORD}.
     */
    @Bean("jobManagerDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.jobmanager")
    public DataSource jobManagerDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * Jobs data source (business/execution tables).
     * Credentials sourced from ${HANA_JOBS_URL}, ${HANA_JOBS_USER}, ${HANA_JOBS_PASSWORD}.
     */
    @Bean("jobsDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.jobs")
    public DataSource jobsDataSource() {
        return DataSourceBuilder.create().build();
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
