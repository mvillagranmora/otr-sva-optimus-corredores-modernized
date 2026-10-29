package cl.bcs.apv.domain.port.out;

import java.time.Instant;

/** INTENDED API: the database/system clock (legacy FechaDB / CURRENT_UTCTIMESTAMP). The cases pin it at 2025-04-01T10:15:30Z. */
public interface Reloj {
    Instant ahora();
}
