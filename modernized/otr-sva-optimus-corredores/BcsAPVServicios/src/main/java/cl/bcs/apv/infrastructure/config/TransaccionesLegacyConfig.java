package cl.bcs.apv.infrastructure.config;

import org.springframework.boot.transaction.autoconfigure.TransactionManagerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * Transaction model of the legacy platform (architecture review H2): one connection, committed when the entry point returns and rolled back only when
 * the entry point throws. A legacy service that calls another one and SWALLOWS its exception (GirarComprobanteIngreso / GirarComprobantesEgreso around
 * CrearComprobante, the PAC and authorization services around their collaborators, ...) therefore commits the work done before the failure.
 *
 * <p>Spring's default is the opposite: every {@code @Transactional} collaborator that participates in the caller's transaction and throws marks the
 * shared transaction rollback-only, so the caller's own commit fails with {@code UnexpectedRollbackException} (HTTP 500, all the partial work lost)
 * even though the caller handled the exception. {@code globalRollbackOnParticipationFailure=false} leaves the decision to the outermost boundary, which
 * rolls back exactly when the exception reaches it. Nothing changes for an exception that propagates: the outermost {@code @Transactional} still
 * rolls back (rollbackFor = Exception.class).
 *
 * <p>This is applied to the transaction manager rather than to one collaborator because the failure can happen at any depth of the proxied call tree
 * (CrearComprobanteService -> CrearComprobanteJavaService -> ..., each {@code @Transactional}); a non-transactional entry point on the first collaborator
 * would not help once a deeper proxied service throws.
 */
@Configuration(proxyBeanMethods = false)
public class TransaccionesLegacyConfig {

    @Bean
    TransactionManagerCustomizer<DataSourceTransactionManager> participacionSinMarcaDeRollback() {
        return manager -> manager.setGlobalRollbackOnParticipationFailure(false);
    }
}
