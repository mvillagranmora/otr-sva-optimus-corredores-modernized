package cl.bcs.oracle;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Pinned clock of the oracle: the copies of the legacy classes that read the wall clock (EnviarTablaReplica: LocalDateTime.now(); IniciarReplicaFull/Delta: System.currentTimeMillis()) call this instead, so that the goldens are reproducible. UTC. */
public final class Clock {
    private static LocalDateTime ahora = LocalDateTime.of(2026, 3, 2, 14, 30, 0);

    private Clock() { }

    public static void fijar(String yyyyMMddHHmmss) {
        ahora = Timestamp.valueOf(yyyyMMddHHmmss).toLocalDateTime();
    }

    public static LocalDateTime now() { return ahora; }

    public static long millis() { return Timestamp.valueOf(ahora).getTime(); }
}
