package cl.bcs.jobmanager.port.out;

import cl.bcs.jobmanager.domain.Programa;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

/**
 * Output port for the program queue (BAT.Programa), READ ONLY.
 *
 * The dispatcher never claims, never updates the state of a program and never retries: the job module that receives
 * POST /internal/job-call owns the claim (Bloqueo='S', ASI) and the final state (TER/ERR). That is why this port has
 * no write method (decision of 2026-10-21; before, lockPrograma/updateIntent/updatePrograma/retryFailedPrograms
 * lived here).
 */
public interface ProgramaPort {

    /**
     * Programs eligible for dispatch, equal to the legacy TransactionManager.readProgramm predicate:
     * Bloqueo='N', idEstado='PEN', Nodo = node, Intento &lt; MaximoIntentos, fechaEjecucion between the start of
     * the current UTC day and now, and IdTipoPrograma in the given types. Oldest fechaCreacion first.
     * No FOR UPDATE, no SKIP LOCKED: nothing is locked here.
     *
     * @param node  normalized node name ('' for the default node)
     * @param tipos enabled IdTipoPrograma values; an empty collection selects nothing (and runs no query)
     * @param now   reference instant (the end of the date window)
     * @return eligible programs, never null
     */
    List<Programa> readEligiblePrograms(String node, Collection<String> tipos, Instant now);
}
