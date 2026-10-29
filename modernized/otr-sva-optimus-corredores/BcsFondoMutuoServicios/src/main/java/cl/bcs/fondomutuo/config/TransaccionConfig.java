package cl.bcs.fondomutuo.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * The transaction manager of the module (C1 / C1b of PLAN_FONDOMUTUO_F2 section 12): a {@link DataSourceTransactionManager} on THE module
 * {@code DataSource}, so the unit of work of a JavaCall request is a Spring transaction that an in-process library on the same {@code DataSource}
 * (Tesoreria's {@code CrearIngresoEgresoService}, {@code @Transactional(rollbackFor = Exception.class)}, REQUIRED) joins instead of opening its own.
 *
 * <p>{@code globalRollbackOnParticipationFailure = false}, exactly as {@code TransaccionesLegacyConfig} of BcsTesoreriaServicios: the legacy platform ran
 * one connection per call and rolled back only when the entry point threw. A collaborator that participates in the shared transaction, throws, and whose
 * exception the caller SWALLOWS must not leave the transaction rollback-only (Spring's default would turn the caller's own commit into
 * {@code UnexpectedRollbackException}); the outermost boundary ({@code JavaCallUnitOfWorkFilter}) decides, by the legacy counter rule. Nothing changes for an
 * exception that propagates: the unit of work still rolls back.
 */
@Configuration(proxyBeanMethods = false)
public class TransaccionConfig {

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        DataSourceTransactionManager manager = new DataSourceTransactionManager(dataSource);
        manager.setGlobalRollbackOnParticipationFailure(false);
        return manager;
    }
}
