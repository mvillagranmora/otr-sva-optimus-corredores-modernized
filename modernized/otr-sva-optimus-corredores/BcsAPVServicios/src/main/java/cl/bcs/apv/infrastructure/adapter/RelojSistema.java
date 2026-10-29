package cl.bcs.apv.infrastructure.adapter;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Component;

import cl.bcs.apv.domain.port.out.Reloj;

/**
 * Production {@link Reloj}: the UTC instant of the JVM clock. The legacy {@code FechaDB} read {@code CURRENT_UTCTIMESTAMP} from the
 * database; the platform runs the services and the database on synchronised UTC clocks, so the system clock is used and no query is needed.
 */
@Component
public class RelojSistema implements Reloj {

    private final Clock reloj = Clock.systemUTC();

    @Override
    public Instant ahora() {
        return reloj.instant();
    }
}
