package cl.bcs.plataforma.login.dialecto;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import cl.bcs.plataforma.login.ProcedimientoAlmacenado;
import cl.bcs.plataforma.service.ProcedureDispatcherService;

/**
 * The {@link ProcedimientoAlmacenado} the login services receive: the selector by dialect over the generic dispatcher (HANA) and the Postgres
 * implementation. It is {@code @Primary}; the dispatcher itself is not changed and keeps answering 503 in Postgres mode for every name.
 */
@Configuration
public class ProcedimientosConfiguracion {

    @Bean
    @Primary
    ProcedimientoAlmacenado procedimientosPorDialecto(SqlDialecto dialecto, ProcedureDispatcherService hana) {
        return new ProcedimientosPorDialecto(dialecto, hana, new ProcedimientosPostgres());
    }
}
