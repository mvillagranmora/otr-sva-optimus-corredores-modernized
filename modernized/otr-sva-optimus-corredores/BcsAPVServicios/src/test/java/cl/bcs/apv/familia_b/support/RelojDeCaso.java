package cl.bcs.apv.familia_b.support;

import java.time.Instant;

import cl.bcs.apv.domain.port.out.Reloj;

/** The fixed clock of the cases (legacy-oracle FechaDB.UTC): 2025-04-01 10:15:30 UTC. */
public class RelojDeCaso implements Reloj {
    @Override
    public Instant ahora() {
        return Instant.parse("2025-04-01T10:15:30Z");
    }
}
